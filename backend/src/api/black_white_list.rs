/// 黑白名单管理 API (hsim_wrj_hbmdsq)
use axum::{extract::{Path, Query, State}, Json};
use super::AppState;
use crate::models::*;

pub async fn list(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<BlackWhiteList>>> {
    let page = query.page.unwrap_or(1); let page_size = query.page_size.unwrap_or(20).min(100);
    match state.db.list_black_white_lists(page as i64, page_size as i64, &query.filters).await {
        Ok((r, t)) => Json(ApiResponse::ok(PageResult { records: r, total: t, page, page_size })),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
/// 获取黑白名单分组列表 { 白名单: [...], 黑名单: [...] }
pub async fn grouped_list(State(state): State<AppState>) -> Json<ApiResponse<serde_json::Value>> {
    match state.db.get_bw_list_grouped().await {
        Ok(data) => Json(ApiResponse::ok(data)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn add(State(state): State<AppState>, Json(mut d): Json<BlackWhiteList>) -> Json<ApiResponse<BlackWhiteList>> {
    if d.id.is_none() { d.id = Some(uuid::Uuid::new_v4().to_string()); }
    match state.db.insert_black_white_list(&d).await {
        Ok(_) => Json(ApiResponse::ok(d)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
/// 批量更新授权: { ids: [...], wrjids: [...], mdlx: "白名单"|"黑名单" }
pub async fn edit_batch_auth(State(state): State<AppState>, Json(body): Json<serde_json::Value>) -> Json<ApiResponse<String>> {
    let wrjids: Vec<String> = body["wrjids"].as_array().map(|a| a.iter().filter_map(|v| v.as_str().map(String::from)).collect()).unwrap_or_default();
    let mdlx = body["mdlx"].as_str().unwrap_or("白名单");
    let auth_status: i32 = if mdlx == "白名单" { 1 } else { 2 };
    for wid in &wrjids {
        let _ = state.db.update_drone_auth_status(wid, auth_status).await;
    }
    Json(ApiResponse::ok("batch_updated".into()))
}
pub async fn delete(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<String>> {
    match state.db.delete_black_white_list(&id).await {
        Ok(_) => Json(ApiResponse::ok("deleted".into())),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn batch_delete(State(state): State<AppState>, Json(ids): Json<Vec<String>>) -> Json<ApiResponse<String>> {
    for id in ids { let _ = state.db.delete_black_white_list(&id).await; }
    Json(ApiResponse::ok("batch_deleted".into()))
}
