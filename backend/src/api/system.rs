/// 系统管理 API —— 数据字典 / 用户 / 文件上传
///
/// 迁移自 JeecgBoot 的 SysDictController / SysDictItemController / CommonController。
use axum::{
    extract::{Multipart, Path, Query, State},
    Json,
};
use serde_json::{json, Value};

use super::generic;
use super::AppState;
use crate::models::{ApiResponse, PageQuery, PageResult};

const COLS_DICT: &[&str] = &["id", "dict_name", "dict_code", "description", "del_flag", "type", "create_by", "create_time", "update_by", "update_time"];
const COLS_DICT_ITEM: &[&str] = &["id", "dict_id", "item_text", "item_value", "description", "sort_order", "status", "create_by", "create_time", "update_by", "update_time"];
const COLS_USER: &[&str] = &["id", "username", "realname", "password", "salt", "avatar", "birthday", "sex", "email", "phone", "org_code", "status", "del_flag", "bdnm", "yhjbnm", "post", "create_time", "update_time"];

type JsonPage = Json<ApiResponse<PageResult<Value>>>;
type JsonVal = Json<ApiResponse<Value>>;
type JsonStr = Json<ApiResponse<String>>;

// ===================== 数据字典 =====================
pub async fn list_dict(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_wwct_zdgl", "create_time DESC", &q, &["dict_name", "dict_code"]).await
}
pub async fn add_dict(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_wwct_zdgl", COLS_DICT, &b, Some("id"), true).await
}
pub async fn edit_dict(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_wwct_zdgl", COLS_DICT, "id", &b).await
}
pub async fn delete_dict(State(s): State<AppState>, Path(id): Path<String>) -> JsonStr {
    let _ = sqlx::query("DELETE FROM hsim_wwct_zdxq WHERE dict_id=?").bind(&id).execute(s.db.pool()).await;
    generic::delete(s.db.pool(), "hsim_wwct_zdgl", "id", &id).await
}

/// 根据字典编码返回字典项（{text, value}），供前端下拉使用
pub async fn get_dict_items(State(s): State<AppState>, Path(code): Path<String>) -> Json<ApiResponse<Vec<Value>>> {
    let sql = "SELECT xq.item_text as text, xq.item_value as value FROM hsim_wwct_zdxq xq \
               JOIN hsim_wwct_zdgl gl ON xq.dict_id = gl.id \
               WHERE gl.dict_code = ? AND xq.status = 1 ORDER BY xq.sort_order";
    match sqlx::query(sql).bind(&code).fetch_all(s.db.pool()).await {
        Ok(rows) => Json(ApiResponse::ok(rows.iter().map(generic::row_to_json).collect())),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

// ===================== 字典明细 =====================
pub async fn list_dict_item(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_wwct_zdxq", "sort_order", &q, &["dict_id", "item_text"]).await
}
pub async fn add_dict_item(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_wwct_zdxq", COLS_DICT_ITEM, &b, Some("id"), true).await
}
pub async fn edit_dict_item(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_wwct_zdxq", COLS_DICT_ITEM, "id", &b).await
}
pub async fn delete_dict_item(State(s): State<AppState>, Path(id): Path<String>) -> JsonStr {
    generic::delete(s.db.pool(), "hsim_wwct_zdxq", "id", &id).await
}

// ===================== 用户 =====================
pub async fn list_users(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_mh_yh", "create_time DESC", &q, &["username", "realname"]).await
}
pub async fn add_user(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_mh_yh", COLS_USER, &b, Some("id"), true).await
}
pub async fn edit_user(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_mh_yh", COLS_USER, "id", &b).await
}
pub async fn delete_user(State(s): State<AppState>, Path(id): Path<String>) -> JsonStr {
    generic::delete(s.db.pool(), "hsim_mh_yh", "id", &id).await
}

// ===================== 文件上传 =====================
/// 保存上传文件到 file_storage 的 upload 目录，返回可访问相对路径。
pub async fn upload_file(State(s): State<AppState>, mut multipart: Multipart) -> JsonVal {
    let upload_dir = std::path::Path::new(&s.config.storage.data_dir).join("upload");
    if let Err(e) = std::fs::create_dir_all(&upload_dir) {
        return Json(ApiResponse::error(format!("mkdir failed: {}", e)));
    }
    while let Ok(Some(field)) = multipart.next_field().await {
        let orig = field.file_name().map(|s| s.to_string()).unwrap_or_else(|| "file".to_string());
        let ext = std::path::Path::new(&orig).extension().and_then(|e| e.to_str()).unwrap_or("bin");
        let fname = format!("{}.{}", generic::new_id(), ext);
        match field.bytes().await {
            Ok(data) => {
                let path = upload_dir.join(&fname);
                if let Err(e) = std::fs::write(&path, &data) {
                    return Json(ApiResponse::error(format!("write failed: {}", e)));
                }
                let rel = format!("upload/{}", fname);
                return Json(ApiResponse::ok(json!({"message": rel, "url": rel, "name": orig})));
            }
            Err(e) => return Json(ApiResponse::error(format!("read field failed: {}", e))),
        }
    }
    Json(ApiResponse::error("no file field".to_string()))
}
