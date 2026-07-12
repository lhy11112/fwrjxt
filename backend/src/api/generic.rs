/// 通用 JSON CRUD 引擎 —— 基于 sqlx 动态列读写，覆盖所有 hsim_* 业务表。
///
/// 说明：JeecgBoot 后端的绝大多数 controller 都是标准 CRUD（list/add/edit/delete）。
/// 为避免为每张表编写重复的类型化 SQL，这里用动态列反射实现通用的增删改查：
/// - list: SELECT * 分页，行 → JSON 对象；支持基于白名单列的等值/模糊过滤
/// - get:  按主键查询单行
/// - insert/update: 仅绑定请求体中出现且在白名单列内的字段
use axum::Json;
use serde_json::{json, Map, Value};
use sqlx::{Column, Row, SqlitePool, TypeInfo, ValueRef};

use crate::models::{ApiResponse, PageQuery, PageResult};

/// 将一行 SQLite 结果转换为 JSON 对象（根据运行期列类型选择合适的 Rust 类型）
pub fn row_to_json(row: &sqlx::sqlite::SqliteRow) -> Value {
    let mut map = Map::new();
    for col in row.columns() {
        let idx = col.ordinal();
        let name = col.name().to_string();
        let value = match row.try_get_raw(idx) {
            Ok(raw) => {
                if raw.is_null() {
                    Value::Null
                } else {
                    let type_name = raw.type_info().name().to_uppercase();
                    if type_name.contains("INT") {
                        row.try_get::<i64, _>(idx).map(Value::from).unwrap_or(Value::Null)
                    } else if type_name.contains("REAL")
                        || type_name.contains("FLOA")
                        || type_name.contains("DOUB")
                    {
                        row.try_get::<f64, _>(idx).map(Value::from).unwrap_or(Value::Null)
                    } else if type_name.contains("BOOL") {
                        row.try_get::<bool, _>(idx).map(Value::from).unwrap_or(Value::Null)
                    } else {
                        // TEXT / NUMERIC / 其他统一按字符串读取，读不到再尝试数值
                        match row.try_get::<String, _>(idx) {
                            Ok(s) => Value::from(s),
                            Err(_) => row
                                .try_get::<f64, _>(idx)
                                .map(Value::from)
                                .or_else(|_| row.try_get::<i64, _>(idx).map(Value::from))
                                .unwrap_or(Value::Null),
                        }
                    }
                }
            }
            Err(_) => Value::Null,
        };
        map.insert(name, value);
    }
    Value::Object(map)
}

/// 绑定一个 JSON 值到查询（依据值的 JSON 类型选择绑定类型）
fn bind_value<'q>(
    query: sqlx::query::Query<'q, sqlx::Sqlite, sqlx::sqlite::SqliteArguments<'q>>,
    v: &Value,
) -> sqlx::query::Query<'q, sqlx::Sqlite, sqlx::sqlite::SqliteArguments<'q>> {
    match v {
        Value::Null => query.bind(None::<String>),
        Value::Bool(b) => query.bind(if *b { 1_i64 } else { 0_i64 }),
        Value::Number(n) => {
            if let Some(i) = n.as_i64() {
                query.bind(i)
            } else {
                query.bind(n.as_f64().unwrap_or(0.0))
            }
        }
        Value::String(s) => query.bind(s.clone()),
        // 数组/对象序列化为 JSON 字符串存储
        other => query.bind(other.to_string()),
    }
}

/// 通用分页列表
pub async fn list(
    pool: &SqlitePool,
    table: &str,
    order_by: &str,
    query: &PageQuery,
    filter_cols: &[&str],
) -> Json<ApiResponse<PageResult<Value>>> {
    let page = query.page.unwrap_or(1).max(1) as i64;
    let page_size = query.page_size.unwrap_or(20).clamp(1, 1000) as i64;
    let offset = (page - 1) * page_size;

    // 组装过滤条件（仅接受白名单列，值全部参数化绑定）
    let mut where_clauses: Vec<String> = Vec::new();
    let mut bind_vals: Vec<String> = Vec::new();
    for (k, v) in &query.filters {
        if v.is_empty() {
            continue;
        }
        if filter_cols.contains(&k.as_str()) {
            where_clauses.push(format!("{} LIKE ?", k));
            bind_vals.push(format!("%{}%", v));
        }
    }
    let where_sql = if where_clauses.is_empty() {
        String::new()
    } else {
        format!(" WHERE {}", where_clauses.join(" AND "))
    };

    let count_sql = format!("SELECT COUNT(*) FROM {}{}", table, where_sql);
    let mut cq = sqlx::query_scalar::<_, i64>(&count_sql);
    for b in &bind_vals {
        cq = cq.bind(b);
    }
    let total = match cq.fetch_one(pool).await {
        Ok(t) => t,
        Err(e) => return Json(ApiResponse::error(format!("DB count error: {}", e))),
    };

    let list_sql = format!(
        "SELECT * FROM {}{} ORDER BY {} LIMIT {} OFFSET {}",
        table, where_sql, order_by, page_size, offset
    );
    let mut lq = sqlx::query(&list_sql);
    for b in &bind_vals {
        lq = lq.bind(b);
    }
    match lq.fetch_all(pool).await {
        Ok(rows) => {
            let records: Vec<Value> = rows.iter().map(row_to_json).collect();
            Json(ApiResponse::ok(PageResult {
                records,
                total,
                page: page as u32,
                page_size: page_size as u32,
            }))
        }
        Err(e) => Json(ApiResponse::error(format!("DB list error: {}", e))),
    }
}

/// 返回全部记录（不分页），用于 tree / listAll 之类接口
pub async fn list_all(pool: &SqlitePool, table: &str, order_by: &str) -> Result<Vec<Value>, String> {
    let sql = format!("SELECT * FROM {} ORDER BY {}", table, order_by);
    match sqlx::query(&sql).fetch_all(pool).await {
        Ok(rows) => Ok(rows.iter().map(row_to_json).collect()),
        Err(e) => Err(e.to_string()),
    }
}

/// 按主键查询单行
pub async fn get_by_id(
    pool: &SqlitePool,
    table: &str,
    id_col: &str,
    id: &str,
) -> Json<ApiResponse<Value>> {
    let sql = format!("SELECT * FROM {} WHERE {} = ?", table, id_col);
    match sqlx::query(&sql).bind(id).fetch_optional(pool).await {
        Ok(Some(row)) => Json(ApiResponse::ok(row_to_json(&row))),
        Ok(None) => Json(ApiResponse::ok(Value::Null)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

/// 生成 UUID（无连字符），用于字符串主键
pub fn new_id() -> String {
    uuid::Uuid::new_v4().simple().to_string()
}

fn now_str() -> String {
    chrono::Local::now().format("%Y-%m-%d %H:%M:%S").to_string()
}

/// 通用插入。
/// - `columns`：允许写入的列白名单
/// - `id_col`：若为字符串主键（如 "id"）且请求体缺失，将自动生成 UUID
/// - `auto_id`：主键是否需要自动填充（false 表示自增，跳过）
/// 返回生成/使用的主键值。
pub async fn insert(
    pool: &SqlitePool,
    table: &str,
    columns: &[&str],
    body: &Value,
    id_col: Option<&str>,
    auto_id: bool,
) -> Json<ApiResponse<Value>> {
    let obj = match body.as_object() {
        Some(o) => o.clone(),
        None => Map::new(),
    };

    let mut cols: Vec<String> = Vec::new();
    let mut vals: Vec<Value> = Vec::new();
    let mut generated_id: Option<String> = None;

    for &c in columns {
        if let Some(v) = obj.get(c) {
            cols.push(c.to_string());
            vals.push(v.clone());
        } else if auto_id && Some(c) == id_col {
            let id = new_id();
            generated_id = Some(id.clone());
            cols.push(c.to_string());
            vals.push(Value::String(id));
        }
    }

    // 自动补充创建时间类字段
    let now = now_str();
    for tcol in ["cjsj", "CJSJ", "create_time", "rksj", "czsj", "CZSJ"] {
        if columns.contains(&tcol) && !cols.iter().any(|c| c == tcol) {
            cols.push(tcol.to_string());
            vals.push(Value::String(now.clone()));
        }
    }

    if cols.is_empty() {
        return Json(ApiResponse::error("no valid columns to insert".to_string()));
    }

    let placeholders: Vec<&str> = cols.iter().map(|_| "?").collect();
    let sql = format!(
        "INSERT INTO {} ({}) VALUES ({})",
        table,
        cols.join(","),
        placeholders.join(",")
    );
    let mut q = sqlx::query(&sql);
    for v in &vals {
        q = bind_value(q, v);
    }
    match q.execute(pool).await {
        Ok(res) => {
            let id_val = generated_id
                .map(Value::String)
                .or_else(|| id_col.and_then(|ic| obj.get(ic).cloned()))
                .unwrap_or_else(|| json!(res.last_insert_rowid()));
            Json(ApiResponse::ok(id_val))
        }
        Err(e) => Json(ApiResponse::error(format!("Insert error: {}", e))),
    }
}

/// 通用更新（按主键）。仅更新请求体中出现的白名单列。
pub async fn update(
    pool: &SqlitePool,
    table: &str,
    columns: &[&str],
    id_col: &str,
    body: &Value,
) -> Json<ApiResponse<Value>> {
    let obj = match body.as_object() {
        Some(o) => o.clone(),
        None => return Json(ApiResponse::error("body must be an object".to_string())),
    };
    let id_val = match obj.get(id_col) {
        Some(v) if !v.is_null() => v.clone(),
        _ => return Json(ApiResponse::error(format!("missing id field '{}'", id_col))),
    };

    let mut sets: Vec<String> = Vec::new();
    let mut vals: Vec<Value> = Vec::new();
    for &c in columns {
        if c == id_col {
            continue;
        }
        if let Some(v) = obj.get(c) {
            sets.push(format!("{}=?", c));
            vals.push(v.clone());
        }
    }
    // 自动更新时间
    let now = now_str();
    for tcol in ["czsj", "CZSJ", "update_time"] {
        if columns.contains(&tcol) && !sets.iter().any(|s| s.starts_with(&format!("{}=", tcol))) {
            sets.push(format!("{}=?", tcol));
            vals.push(Value::String(now.clone()));
        }
    }
    if sets.is_empty() {
        return Json(ApiResponse::ok(id_val));
    }

    let sql = format!("UPDATE {} SET {} WHERE {}=?", table, sets.join(","), id_col);
    let mut q = sqlx::query(&sql);
    for v in &vals {
        q = bind_value(q, v);
    }
    q = bind_value(q, &id_val);
    match q.execute(pool).await {
        Ok(_) => Json(ApiResponse::ok(id_val)),
        Err(e) => Json(ApiResponse::error(format!("Update error: {}", e))),
    }
}

/// 通用删除（按主键，主键类型不限）
pub async fn delete(pool: &SqlitePool, table: &str, id_col: &str, id: &str) -> Json<ApiResponse<String>> {
    let sql = format!("DELETE FROM {} WHERE {} = ?", table, id_col);
    match sqlx::query(&sql).bind(id).execute(pool).await {
        Ok(_) => Json(ApiResponse::ok("deleted".to_string())),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

/// 通用批量删除。请求体形如 {"ids": [...]}，或直接为数组。
pub async fn batch_delete(
    pool: &SqlitePool,
    table: &str,
    id_col: &str,
    body: &Value,
) -> Json<ApiResponse<String>> {
    let ids: Vec<Value> = body
        .get("ids")
        .and_then(|v| v.as_array())
        .cloned()
        .or_else(|| body.as_array().cloned())
        .unwrap_or_default();
    if ids.is_empty() {
        return Json(ApiResponse::ok("ok".to_string()));
    }
    let placeholders: Vec<&str> = ids.iter().map(|_| "?").collect();
    let sql = format!(
        "DELETE FROM {} WHERE {} IN ({})",
        table,
        id_col,
        placeholders.join(",")
    );
    let mut q = sqlx::query(&sql);
    for v in &ids {
        q = bind_value(q, v);
    }
    match q.execute(pool).await {
        Ok(_) => Json(ApiResponse::ok("deleted".to_string())),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
