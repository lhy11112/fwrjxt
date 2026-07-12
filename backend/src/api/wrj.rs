/// wrj 武警业务 API —— 通用 CRUD（基于动态 JSON 引擎）+ 关键自定义端点
///
/// 迁移自 JeecgBoot 后端 org.jeecg.modules.wrj / dxyy / system 等模块的 controller。
/// 所有列表返回真实数据，新增/编辑真正绑定请求体字段。
use axum::{
    extract::{Path, Query, State},
    Json,
};
use serde_json::{json, Value};

use super::generic;
use super::AppState;
use crate::models::{ApiResponse, PageQuery, PageResult};

// ===================== 列定义（与 database.rs 一致）=====================

const COLS_FWZF: &[&str] = &["id", "fl", "mc", "zfgs", "zzcj", "jbzf", "llbshtxgj", "jtxdff", "zfyzqk", "zfyyxybwwt", "wj", "remark", "cjr", "cjrid", "cjsj", "czr", "czrid", "czsj"];
const COLS_FZZB: &[&str] = &["id", "fl", "mc", "gntd", "jszb", "tp", "sl", "zbly", "remark", "cjr", "cjrid", "cjsj", "czr", "czrid", "czsj", "mxdz"];
const COLS_BXPZ: &[&str] = &["id", "mc", "pid", "nr", "remark", "cjr", "cjrid", "cjsj", "czr", "czrid", "czsj"];
const COLS_ZYMB: &[&str] = &["id", "mc", "jd", "wd", "rylx", "sl", "zb", "ssgk", "type", "subtype"];
const COLS_KYSQ: &[&str] = &["id", "kyid", "wrjid", "sqsj", "sqgqsj", "cjr", "cjrid", "cjsj", "czr", "czrid", "czsj"];
const COLS_CZSP: &[&str] = &["id", "spfl", "mc", "splj", "cjr", "cjrid", "cjsj", "czr", "czrid", "czsj"];
const COLS_SCJ: &[&str] = &["id", "pid", "mc", "type", "yh_id", "csz", "chjr_mc", "chjr", "rksj"];
const COLS_JKGL: &[&str] = &["id", "url", "client", "secret", "bm", "bz", "lx"];
const COLS_TYJH: &[&str] = &["id", "mc", "rq", "jhks", "jhjs", "serial", "model", "brand", "tyzt", "cjr", "cjrid", "cjsj", "czr", "czrid", "czsj", "start_jd", "start_wd", "start_gd", "tjdjwd", "end_jd", "end_wd", "end_gd", "jhcs", "mbfxfw"];
const COLS_TYJH_DATA: &[&str] = &["id", "tyjh_id", "serial", "model", "dron_lng", "dron_lat", "home_lng", "home_lat", "pilot_lng", "pilot_lat", "altitude", "height", "east_v", "north_v", "up_v", "freq", "rssi", "distance", "rid_ssid", "sd", "data_time", "create_time"];
const COLS_JBXX_NEW: &[&str] = &["id", "serial_number", "brand", "model", "type", "jj", "dy", "tcnl", "zznl", "bpjbs", "zdxhsj", "zdfxsd", "zdkzjl", "zdfxgd", "kfdj", "zdhzzl", "jscc", "jszl", "dlxt", "dwxt", "xjcs", "tcxt", "dcgg", "status", "auth_status", "current_longitude", "current_latitude", "current_altitude", "last_seen_time", "remark", "cjr", "cjrid", "cjsj", "czr", "czrid", "czsj", "tp", "mxdz", "dqfl", "zlfl"];
const COLS_ZSK: &[&str] = &["id", "zsk_mc", "zsk_nm", "zsk_ms", "chjr_mc", "chjr", "rksj", "zsk_lx"];
const COLS_ZSK_WJ: &[&str] = &["id", "zsk_id", "wj_mc", "wj_nm", "wjdx", "fwq_wjlj", "chjr_mc", "chjr", "rksj"];
const COLS_ZSK_WJ_NR: &[&str] = &["id", "zsk_id", "zsk_wj_id", "nr"];
const COLS_SPECTRUM: &[&str] = &["id", "station_id", "channel", "data_type", "start_freq", "stop_freq", "step_freq", "data_len", "p_data", "create_time"];
const COLS_DXYY_BH: &[&str] = &["id", "bhmc", "tsmc", "bhsj", "rwsj", "ztm", "dwid", "dwmc", "jsdwid", "gisjson", "cjrid", "cjsj", "yw_id"];
const COLS_LPWJ: &[&str] = &["id", "mc", "qdwjml", "yhid"];
const COLS_DXDM: &[&str] = &["id", "dmmc", "jd", "wd", "dxdmlx", "mj", "dlwz"];
const COLS_YHGL_CS: &[&str] = &["id", "yh_id", "cs_mc", "cs_bm", "csz", "bz", "chjr_mc", "chjr", "rksj", "bdnm"];
const COLS_ZZLL_BD: &[&str] = &["bdnm", "bdhfnm", "bdxh", "bdfh", "bdjc", "bzxh", "bzfh", "bzjc"];
const COLS_ZZLL_BD_BS: &[&str] = &["bslbnm", "bdnm", "twsj", "bsxsnm", "dmnm", "kzdm", "jd", "wd", "gc", "zzsj"];
const COLS_OP_LOG: &[&str] = &["id", "yymc", "yymk", "rzlx", "rznr", "czyhm", "czyhxm", "czip", "cjrid", "cjsj", "czrid", "czsj", "ljsc"];

type JsonPage = Json<ApiResponse<PageResult<Value>>>;
type JsonVal = Json<ApiResponse<Value>>;
type JsonStr = Json<ApiResponse<String>>;

// ===================== 反无战法 fwzf =====================
pub async fn list_fwzf(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_wrj_fwzf", "cjsj DESC", &q, &["fl", "mc"]).await
}
pub async fn add_fwzf(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_wrj_fwzf", COLS_FWZF, &b, Some("id"), true).await
}
pub async fn edit_fwzf(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_wrj_fwzf", COLS_FWZF, "id", &b).await
}
pub async fn get_fwzf(State(s): State<AppState>, Path(id): Path<String>) -> JsonVal {
    generic::get_by_id(s.db.pool(), "hsim_wrj_fwzf", "id", &id).await
}
pub async fn delete_fwzf(State(s): State<AppState>, Path(id): Path<String>) -> JsonStr {
    generic::delete(s.db.pool(), "hsim_wrj_fwzf", "id", &id).await
}
pub async fn batch_delete_fwzf(State(s): State<AppState>, Json(b): Json<Value>) -> JsonStr {
    generic::batch_delete(s.db.pool(), "hsim_wrj_fwzf", "id", &b).await
}

// ===================== 反制装备 fzzb =====================
pub async fn list_fzzb(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_wrj_fzzb", "cjsj DESC", &q, &["fl", "mc"]).await
}
pub async fn add_fzzb(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_wrj_fzzb", COLS_FZZB, &b, Some("id"), true).await
}
pub async fn edit_fzzb(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_wrj_fzzb", COLS_FZZB, "id", &b).await
}
pub async fn get_fzzb(State(s): State<AppState>, Path(id): Path<String>) -> JsonVal {
    generic::get_by_id(s.db.pool(), "hsim_wrj_fzzb", "id", &id).await
}
pub async fn delete_fzzb(State(s): State<AppState>, Path(id): Path<String>) -> JsonStr {
    generic::delete(s.db.pool(), "hsim_wrj_fzzb", "id", &id).await
}
pub async fn batch_delete_fzzb(State(s): State<AppState>, Json(b): Json<Value>) -> JsonStr {
    generic::batch_delete(s.db.pool(), "hsim_wrj_fzzb", "id", &b).await
}

// ===================== 编携配装 bxpz =====================
pub async fn list_bxpz(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_wrj_bxpz", "id", &q, &["mc"]).await
}
pub async fn add_bxpz(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_wrj_bxpz", COLS_BXPZ, &b, Some("id"), true).await
}
pub async fn edit_bxpz(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_wrj_bxpz", COLS_BXPZ, "id", &b).await
}
pub async fn get_bxpz(State(s): State<AppState>, Path(id): Path<String>) -> JsonVal {
    generic::get_by_id(s.db.pool(), "hsim_wrj_bxpz", "id", &id).await
}
pub async fn delete_bxpz(State(s): State<AppState>, Path(id): Path<String>) -> JsonStr {
    generic::delete(s.db.pool(), "hsim_wrj_bxpz", "id", &id).await
}
pub async fn batch_delete_bxpz(State(s): State<AppState>, Json(b): Json<Value>) -> JsonStr {
    generic::batch_delete(s.db.pool(), "hsim_wrj_bxpz", "id", &b).await
}
/// 编携配装树（从 pid 组装嵌套结构，根节点 pid 为 "0"/null/空）
pub async fn list_bxpz_tree(State(s): State<AppState>) -> Json<ApiResponse<Vec<Value>>> {
    match generic::list_all(s.db.pool(), "hsim_wrj_bxpz", "id").await {
        Ok(rows) => Json(ApiResponse::ok(build_tree(rows, "id", "pid", "0"))),
        Err(e) => Json(ApiResponse::error(e)),
    }
}

// ===================== 重要目标 zymb =====================
pub async fn list_zymb(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_wrj_zymb", "id DESC", &q, &["mc", "rylx", "type"]).await
}
pub async fn add_zymb(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_wrj_zymb", COLS_ZYMB, &b, Some("id"), true).await
}
pub async fn edit_zymb(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_wrj_zymb", COLS_ZYMB, "id", &b).await
}
pub async fn get_zymb(State(s): State<AppState>, Path(id): Path<String>) -> JsonVal {
    generic::get_by_id(s.db.pool(), "hsim_wrj_zymb", "id", &id).await
}
pub async fn delete_zymb(State(s): State<AppState>, Path(id): Path<String>) -> JsonStr {
    generic::delete(s.db.pool(), "hsim_wrj_zymb", "id", &id).await
}
pub async fn batch_delete_zymb(State(s): State<AppState>, Json(b): Json<Value>) -> JsonStr {
    generic::batch_delete(s.db.pool(), "hsim_wrj_zymb", "id", &b).await
}

#[derive(serde::Deserialize)]
pub struct GeoQuery {
    pub jd: f64,
    pub wd: f64,
    pub jl: f64, // 半径，单位米
    #[serde(default)]
    pub r#type: Option<String>,
}
/// 以经纬度中心 + 半径（米）查询范围内的重要目标（Haversine）
pub async fn zymb_by_geo(State(s): State<AppState>, Query(g): Query<GeoQuery>) -> Json<ApiResponse<Vec<Value>>> {
    let rows = match generic::list_all(s.db.pool(), "hsim_wrj_zymb", "id").await {
        Ok(r) => r,
        Err(e) => return Json(ApiResponse::error(e)),
    };
    let types: Option<Vec<String>> = g
        .r#type
        .as_ref()
        .filter(|t| !t.is_empty())
        .map(|t| t.split(',').map(|x| x.trim().to_string()).collect());
    let filtered: Vec<Value> = rows
        .into_iter()
        .filter(|r| {
            let jd = str_to_f64(r.get("jd"));
            let wd = str_to_f64(r.get("wd"));
            match (jd, wd) {
                (Some(jd), Some(wd)) => {
                    let d = haversine(g.wd, g.jd, wd, jd);
                    let type_ok = match &types {
                        Some(ts) => r
                            .get("type")
                            .and_then(|v| v.as_str())
                            .map(|t| ts.iter().any(|x| x == t))
                            .unwrap_or(false),
                        None => true,
                    };
                    d <= g.jl && type_ok
                }
                _ => false,
            }
        })
        .collect();
    Json(ApiResponse::ok(filtered))
}

// ===================== 空域授权 kysq =====================
pub async fn list_kysq(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_wrj_kysq", "id", &q, &["kyid", "wrjid"]).await
}
pub async fn add_kysq(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_wrj_kysq", COLS_KYSQ, &b, Some("id"), true).await
}
pub async fn delete_kysq(State(s): State<AppState>, Path(id): Path<String>) -> JsonStr {
    generic::delete(s.db.pool(), "hsim_wrj_kysq", "id", &id).await
}
/// 批量授权：{kyid, wrjids:[...]} → 为每架无人机生成一条授权记录
pub async fn save_kysq(State(s): State<AppState>, Json(b): Json<Value>) -> JsonStr {
    let kyid = b.get("kyid").and_then(|v| v.as_str()).unwrap_or("").to_string();
    let wrjids: Vec<String> = b
        .get("wrjids")
        .and_then(|v| v.as_array())
        .map(|a| a.iter().filter_map(|x| x.as_str().map(String::from)).collect())
        .unwrap_or_default();
    if kyid.is_empty() {
        return Json(ApiResponse::error("kyid required".to_string()));
    }
    for wrjid in wrjids {
        let body = json!({"kyid": kyid, "wrjid": wrjid});
        let _ = generic::insert(s.db.pool(), "hsim_wrj_kysq", COLS_KYSQ, &body, Some("id"), true).await;
    }
    Json(ApiResponse::ok("saved".to_string()))
}

// ===================== 操作视频 czsp =====================
pub async fn list_czsp(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_wrj_czsp", "cjsj DESC", &q, &["spfl", "mc"]).await
}
pub async fn add_czsp(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_wrj_czsp", COLS_CZSP, &b, Some("id"), true).await
}
pub async fn edit_czsp(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_wrj_czsp", COLS_CZSP, "id", &b).await
}
pub async fn get_czsp(State(s): State<AppState>, Path(id): Path<String>) -> JsonVal {
    generic::get_by_id(s.db.pool(), "hsim_wrj_czsp", "id", &id).await
}
pub async fn delete_czsp(State(s): State<AppState>, Path(id): Path<String>) -> JsonStr {
    generic::delete(s.db.pool(), "hsim_wrj_czsp", "id", &id).await
}
pub async fn batch_delete_czsp(State(s): State<AppState>, Json(b): Json<Value>) -> JsonStr {
    generic::batch_delete(s.db.pool(), "hsim_wrj_czsp", "id", &b).await
}

// ===================== 收藏夹 scj =====================
pub async fn list_scj(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_wrj_scj", "id DESC", &q, &["mc", "type", "yh_id"]).await
}
pub async fn add_scj(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_wrj_scj", COLS_SCJ, &b, Some("id"), false).await
}
pub async fn edit_scj(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_wrj_scj", COLS_SCJ, "id", &b).await
}
pub async fn get_scj(State(s): State<AppState>, Path(id): Path<i64>) -> JsonVal {
    generic::get_by_id(s.db.pool(), "hsim_wrj_scj", "id", &id.to_string()).await
}
pub async fn delete_scj(State(s): State<AppState>, Path(id): Path<i64>) -> JsonStr {
    generic::delete(s.db.pool(), "hsim_wrj_scj", "id", &id.to_string()).await
}
pub async fn batch_delete_scj(State(s): State<AppState>, Json(b): Json<Value>) -> JsonStr {
    generic::batch_delete(s.db.pool(), "hsim_wrj_scj", "id", &b).await
}
pub async fn list_scj_tree(State(s): State<AppState>) -> Json<ApiResponse<Vec<Value>>> {
    match generic::list_all(s.db.pool(), "hsim_wrj_scj", "id").await {
        Ok(rows) => Json(ApiResponse::ok(build_tree(rows, "id", "pid", "0"))),
        Err(e) => Json(ApiResponse::error(e)),
    }
}

// ===================== 接口管理 jkgl =====================
pub async fn list_jkgl(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_wrj_jkgl", "id DESC", &q, &["bm", "url"]).await
}
pub async fn add_jkgl(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_wrj_jkgl", COLS_JKGL, &b, Some("id"), false).await
}
pub async fn edit_jkgl(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_wrj_jkgl", COLS_JKGL, "id", &b).await
}
pub async fn get_jkgl(State(s): State<AppState>, Path(id): Path<i64>) -> JsonVal {
    generic::get_by_id(s.db.pool(), "hsim_wrj_jkgl", "id", &id.to_string()).await
}
pub async fn delete_jkgl(State(s): State<AppState>, Path(id): Path<i64>) -> JsonStr {
    generic::delete(s.db.pool(), "hsim_wrj_jkgl", "id", &id.to_string()).await
}
pub async fn batch_delete_jkgl(State(s): State<AppState>, Json(b): Json<Value>) -> JsonStr {
    generic::batch_delete(s.db.pool(), "hsim_wrj_jkgl", "id", &b).await
}

// ===================== 推演计划 tyjh =====================
pub async fn list_tyjh(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_wrj_tyjh", "cjsj DESC", &q, &["mc", "serial", "model"]).await
}
pub async fn get_tyjh(State(s): State<AppState>, Path(id): Path<String>) -> JsonVal {
    generic::get_by_id(s.db.pool(), "hsim_wrj_tyjh", "id", &id).await
}
pub async fn delete_tyjh(State(s): State<AppState>, Path(id): Path<String>) -> JsonStr {
    let _ = sqlx::query("DELETE FROM hsim_wrj_tyjh_data WHERE tyjh_id=?")
        .bind(&id)
        .execute(s.db.pool())
        .await;
    generic::delete(s.db.pool(), "hsim_wrj_tyjh", "id", &id).await
}
pub async fn batch_delete_tyjh(State(s): State<AppState>, Json(b): Json<Value>) -> JsonStr {
    if let Some(arr) = b.get("ids").and_then(|v| v.as_array()) {
        for id in arr.iter().filter_map(|v| v.as_str()) {
            let _ = sqlx::query("DELETE FROM hsim_wrj_tyjh_data WHERE tyjh_id=?").bind(id).execute(s.db.pool()).await;
        }
    }
    generic::batch_delete(s.db.pool(), "hsim_wrj_tyjh", "id", &b).await
}
/// 生成推演计划 + 直线插值轨迹数据（迁移自 TYJHSC）
pub async fn generate_tyjh(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    let id = b.get("id").and_then(|v| v.as_str()).map(String::from).unwrap_or_else(generic::new_id);
    let mut plan = b.clone();
    if let Some(o) = plan.as_object_mut() {
        o.insert("id".to_string(), Value::String(id.clone()));
        o.entry("tyzt").or_insert(Value::String("0".to_string()));
    }
    // 保存计划
    let ins = generic::insert(s.db.pool(), "hsim_wrj_tyjh", COLS_TYJH, &plan, Some("id"), false).await;
    if !ins.0.success {
        return ins;
    }
    // 生成直线插值轨迹
    let start_jd = num(b.get("start_jd"));
    let start_wd = num(b.get("start_wd"));
    let end_jd = num(b.get("end_jd"));
    let end_wd = num(b.get("end_wd"));
    let start_gd = num(b.get("start_gd")).unwrap_or(100.0);
    let end_gd = num(b.get("end_gd")).unwrap_or(100.0);
    let serial = b.get("serial").and_then(|v| v.as_str()).unwrap_or("SIM").to_string();
    let model = b.get("model").and_then(|v| v.as_str()).unwrap_or("").to_string();
    if let (Some(sj), Some(sw), Some(ej), Some(ew)) = (start_jd, start_wd, end_jd, end_wd) {
        let steps = 60;
        let base = chrono::Local::now();
        for i in 0..=steps {
            let t = i as f64 / steps as f64;
            let jd = sj + (ej - sj) * t;
            let wd = sw + (ew - sw) * t;
            let gd = start_gd + (end_gd - start_gd) * t;
            let dt = (base + chrono::Duration::seconds(i * 2)).format("%Y-%m-%d %H:%M:%S").to_string();
            let row = json!({
                "tyjh_id": id, "serial": serial, "model": model,
                "dron_lng": jd, "dron_lat": wd, "altitude": gd, "height": gd,
                "home_lng": sj, "home_lat": sw, "data_time": dt
            });
            let _ = generic::insert(s.db.pool(), "hsim_wrj_tyjh_data", COLS_TYJH_DATA, &row, Some("id"), false).await;
        }
    }
    Json(ApiResponse::ok(json!({ "id": id })))
}
/// 变更推演计划状态
pub async fn change_tyjh_status(State(s): State<AppState>, Query(q): Query<std::collections::HashMap<String, String>>) -> JsonStr {
    let id = q.get("id").cloned().unwrap_or_default();
    let zt = q.get("zt").cloned().unwrap_or_default();
    match sqlx::query("UPDATE hsim_wrj_tyjh SET tyzt=? WHERE id=?").bind(&zt).bind(&id).execute(s.db.pool()).await {
        Ok(_) => Json(ApiResponse::ok("ok".to_string())),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

// ===================== 推演计划数据 tyjh_data =====================
pub async fn list_tyjh_data(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_wrj_tyjh_data", "data_time ASC", &q, &["tyjh_id", "serial"]).await
}
pub async fn add_tyjh_data(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_wrj_tyjh_data", COLS_TYJH_DATA, &b, Some("id"), false).await
}
pub async fn delete_tyjh_data(State(s): State<AppState>, Path(id): Path<i64>) -> JsonStr {
    generic::delete(s.db.pool(), "hsim_wrj_tyjh_data", "id", &id.to_string()).await
}

// ===================== 无人机详细信息 jbxx_new =====================
pub async fn list_jbxx_new(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_wrj_jbxx_new", "czsj DESC", &q, &["serial_number", "brand", "model"]).await
}
pub async fn add_jbxx_new(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_wrj_jbxx_new", COLS_JBXX_NEW, &b, Some("id"), true).await
}
pub async fn edit_jbxx_new(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_wrj_jbxx_new", COLS_JBXX_NEW, "id", &b).await
}
pub async fn get_jbxx_new(State(s): State<AppState>, Path(id): Path<String>) -> JsonVal {
    generic::get_by_id(s.db.pool(), "hsim_wrj_jbxx_new", "id", &id).await
}
pub async fn delete_jbxx_new(State(s): State<AppState>, Path(id): Path<String>) -> JsonStr {
    generic::delete(s.db.pool(), "hsim_wrj_jbxx_new", "id", &id).await
}
pub async fn batch_delete_jbxx_new(State(s): State<AppState>, Json(b): Json<Value>) -> JsonStr {
    generic::batch_delete(s.db.pool(), "hsim_wrj_jbxx_new", "id", &b).await
}

// ===================== 知识库 zsk =====================
pub async fn list_zsk(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_hs_zsk", "id DESC", &q, &["zsk_mc", "zsk_nm"]).await
}
pub async fn add_zsk(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_hs_zsk", COLS_ZSK, &b, Some("id"), false).await
}
pub async fn edit_zsk(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_hs_zsk", COLS_ZSK, "id", &b).await
}
pub async fn get_zsk(State(s): State<AppState>, Path(id): Path<i64>) -> JsonVal {
    generic::get_by_id(s.db.pool(), "hsim_hs_zsk", "id", &id.to_string()).await
}
pub async fn delete_zsk(State(s): State<AppState>, Path(id): Path<i64>) -> JsonStr {
    generic::delete(s.db.pool(), "hsim_hs_zsk", "id", &id.to_string()).await
}

// ===================== 知识库文件 zsk_wj =====================
pub async fn list_zsk_wj(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_hs_zsk_wj", "id DESC", &q, &["wj_mc", "zsk_id"]).await
}
pub async fn add_zsk_wj(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_hs_zsk_wj", COLS_ZSK_WJ, &b, Some("id"), false).await
}
pub async fn edit_zsk_wj(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_hs_zsk_wj", COLS_ZSK_WJ, "id", &b).await
}
pub async fn get_zsk_wj(State(s): State<AppState>, Path(id): Path<i64>) -> JsonVal {
    generic::get_by_id(s.db.pool(), "hsim_hs_zsk_wj", "id", &id.to_string()).await
}
pub async fn delete_zsk_wj(State(s): State<AppState>, Path(id): Path<i64>) -> JsonStr {
    generic::delete(s.db.pool(), "hsim_hs_zsk_wj", "id", &id.to_string()).await
}

// ===================== 知识库文件内容 zsk_wj_nr =====================
pub async fn list_zsk_wj_nr(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_hs_zsk_wj_nr", "id DESC", &q, &["zsk_id", "zsk_wj_id"]).await
}
pub async fn add_zsk_wj_nr(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_hs_zsk_wj_nr", COLS_ZSK_WJ_NR, &b, Some("id"), false).await
}

// ===================== 频谱结果 spectrum =====================
pub async fn list_spectrum_result(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_spectrum", "create_time DESC", &q, &["station_id", "channel"]).await
}
pub async fn add_spectrum_result(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_spectrum", COLS_SPECTRUM, &b, Some("id"), false).await
}
pub async fn edit_spectrum_result(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_spectrum", COLS_SPECTRUM, "id", &b).await
}
pub async fn get_spectrum_result(State(s): State<AppState>, Path(id): Path<i64>) -> JsonVal {
    generic::get_by_id(s.db.pool(), "hsim_spectrum", "id", &id.to_string()).await
}

// ===================== 标绘 dxyy_bh =====================
pub async fn list_dxyy_bh(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_dxyy_bh", "id DESC", &q, &["bhmc", "yw_id"]).await
}
pub async fn add_dxyy_bh(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_dxyy_bh", COLS_DXYY_BH, &b, Some("id"), false).await
}
pub async fn edit_dxyy_bh(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_dxyy_bh", COLS_DXYY_BH, "id", &b).await
}
pub async fn get_dxyy_bh(State(s): State<AppState>, Path(id): Path<i64>) -> JsonVal {
    generic::get_by_id(s.db.pool(), "hsim_dxyy_bh", "id", &id.to_string()).await
}

// ===================== 录屏文件 lpwj =====================
pub async fn list_lpwj(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_wrj_lpwj", "id DESC", &q, &["mc", "yhid"]).await
}
pub async fn add_lpwj(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_wrj_lpwj", COLS_LPWJ, &b, Some("id"), true).await
}
pub async fn edit_lpwj(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_wrj_lpwj", COLS_LPWJ, "id", &b).await
}

// ===================== 地形地貌 dxdm =====================
pub async fn list_dxdm(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_dxyy_dxdm", "id", &q, &["dmmc", "dxdmlx"]).await
}
pub async fn add_dxdm(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_dxyy_dxdm", COLS_DXDM, &b, Some("id"), true).await
}
pub async fn edit_dxdm(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_dxyy_dxdm", COLS_DXDM, "id", &b).await
}
pub async fn get_dxdm(State(s): State<AppState>, Path(id): Path<String>) -> JsonVal {
    generic::get_by_id(s.db.pool(), "hsim_dxyy_dxdm", "id", &id).await
}
pub async fn delete_dxdm(State(s): State<AppState>, Path(id): Path<String>) -> JsonStr {
    generic::delete(s.db.pool(), "hsim_dxyy_dxdm", "id", &id).await
}
pub async fn batch_delete_dxdm(State(s): State<AppState>, Json(b): Json<Value>) -> JsonStr {
    generic::batch_delete(s.db.pool(), "hsim_dxyy_dxdm", "id", &b).await
}
/// 地形地貌地理半径查询
pub async fn dxdm_by_geo(State(s): State<AppState>, Query(g): Query<GeoQuery>) -> Json<ApiResponse<Vec<Value>>> {
    let rows = match generic::list_all(s.db.pool(), "hsim_dxyy_dxdm", "id").await {
        Ok(r) => r,
        Err(e) => return Json(ApiResponse::error(e)),
    };
    let filtered: Vec<Value> = rows
        .into_iter()
        .filter(|r| {
            let jd = str_to_f64(r.get("jd"));
            let wd = str_to_f64(r.get("wd"));
            match (jd, wd) {
                (Some(jd), Some(wd)) => haversine(g.wd, g.jd, wd, jd) <= g.jl,
                _ => false,
            }
        })
        .collect();
    Json(ApiResponse::ok(filtered))
}

// ===================== 用户管理参数 yhgl_cs =====================
pub async fn list_yhgl_cs(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_wrj_yhgl_cs", "id", &q, &["yh_id", "cs_bm"]).await
}
pub async fn add_yhgl_cs(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_wrj_yhgl_cs", COLS_YHGL_CS, &b, Some("id"), false).await
}
pub async fn edit_yhgl_cs(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_wrj_yhgl_cs", COLS_YHGL_CS, "id", &b).await
}

// ===================== 作战力量部队 zzll_bd =====================
pub async fn list_zzll_bd(State(s): State<AppState>) -> Json<ApiResponse<Vec<Value>>> {
    match generic::list_all(s.db.pool(), "hsim_zzll_bd", "bdnm").await {
        Ok(rows) => Json(ApiResponse::ok(build_tree(rows, "bdnm", "sjbdnm", ""))),
        Err(e) => Json(ApiResponse::error(e)),
    }
}
pub async fn add_zzll_bd(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_zzll_bd", COLS_ZZLL_BD, &b, None, false).await
}
pub async fn edit_zzll_bd(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_zzll_bd", COLS_ZZLL_BD, "bdnm", &b).await
}

// ===================== 作战力量部署 zzll_bd_bs =====================
pub async fn list_zzll_bd_bs(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_zzll_bd_bs", "bslbnm", &q, &["bdnm", "dmnm"]).await
}
pub async fn add_zzll_bd_bs(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_zzll_bd_bs", COLS_ZZLL_BD_BS, &b, None, false).await
}

// ===================== 操作日志 operation_log =====================
pub async fn list_operation_log(State(s): State<AppState>, Query(q): Query<PageQuery>) -> JsonPage {
    generic::list(s.db.pool(), "hsim_mh_yycz_log", "cjsj DESC", &q, &["yymc", "yymk", "czyhm"]).await
}
pub async fn add_operation_log(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::insert(s.db.pool(), "hsim_mh_yycz_log", COLS_OP_LOG, &b, Some("id"), true).await
}
pub async fn edit_operation_log(State(s): State<AppState>, Json(b): Json<Value>) -> JsonVal {
    generic::update(s.db.pool(), "hsim_mh_yycz_log", COLS_OP_LOG, "id", &b).await
}

// ===================== 辅助函数 =====================

/// 从扁平记录列表组装树（id_col=当前节点标识, parent_col=父标识, root_val=根节点父值）
fn build_tree(rows: Vec<Value>, id_col: &str, parent_col: &str, root_val: &str) -> Vec<Value> {
    fn key(v: Option<&Value>) -> String {
        match v {
            Some(Value::String(s)) => s.clone(),
            Some(Value::Number(n)) => n.to_string(),
            _ => String::new(),
        }
    }
    let mut children_map: std::collections::HashMap<String, Vec<Value>> = std::collections::HashMap::new();
    for r in &rows {
        let pid = key(r.get(parent_col));
        children_map.entry(pid).or_default().push(r.clone());
    }
    fn attach(node: &mut Value, id_col: &str, map: &std::collections::HashMap<String, Vec<Value>>) {
        let id = key(node.get(id_col));
        let mut kids = map.get(&id).cloned().unwrap_or_default();
        for k in kids.iter_mut() {
            attach(k, id_col, map);
        }
        if let Some(o) = node.as_object_mut() {
            o.insert("children".to_string(), Value::Array(kids));
        }
    }
    let mut roots = children_map.get(root_val).cloned().unwrap_or_default();
    // 兼容 root 为 null / 空字符串
    if roots.is_empty() && root_val == "0" {
        roots = children_map.get("").cloned().unwrap_or_default();
    }
    for r in roots.iter_mut() {
        attach(r, id_col, &children_map);
    }
    roots
}

fn num(v: Option<&Value>) -> Option<f64> {
    match v {
        Some(Value::Number(n)) => n.as_f64(),
        Some(Value::String(s)) => s.parse().ok(),
        _ => None,
    }
}
fn str_to_f64(v: Option<&Value>) -> Option<f64> {
    num(v)
}

/// Haversine 距离（米）
fn haversine(lat1: f64, lon1: f64, lat2: f64, lon2: f64) -> f64 {
    let r = 6_371_000.0_f64;
    let dlat = (lat2 - lat1).to_radians();
    let dlon = (lon2 - lon1).to_radians();
    let a = (dlat / 2.0).sin().powi(2)
        + lat1.to_radians().cos() * lat2.to_radians().cos() * (dlon / 2.0).sin().powi(2);
    2.0 * r * a.sqrt().asin()
}
