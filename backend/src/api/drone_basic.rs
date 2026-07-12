/// 无人机基本信息 API (hsim_wrj_jbxx)
use axum::{extract::{Path, Query, State}, Json};
use super::AppState;
use crate::models::*;

pub async fn list(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<DroneBasicInfo>>> {
    let page = query.page.unwrap_or(1); let page_size = query.page_size.unwrap_or(20).min(100);
    match state.db.list_drone_basic_infos(page as i64, page_size as i64, &query.filters).await {
        Ok((r, t)) => Json(ApiResponse::ok(PageResult { records: r, total: t, page, page_size })),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn add(State(state): State<AppState>, Json(mut d): Json<DroneBasicInfo>) -> Json<ApiResponse<DroneBasicInfo>> {
    if d.id.is_none() { d.id = Some(uuid::Uuid::new_v4().to_string()); }
    match state.db.insert_drone_basic_info(&d).await {
        Ok(_) => Json(ApiResponse::ok(d)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn edit(State(state): State<AppState>, Json(d): Json<DroneBasicInfo>) -> Json<ApiResponse<DroneBasicInfo>> {
    match state.db.update_drone_basic_info(&d).await {
        Ok(_) => Json(ApiResponse::ok(d)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn edit_auth_status(State(state): State<AppState>, Json(body): Json<serde_json::Value>) -> Json<ApiResponse<String>> {
    let id = body["id"].as_str().unwrap_or("");
    let auth_status: i32 = body["auth_status"].as_i64().unwrap_or(3) as i32;
    match state.db.update_drone_auth_status(id, auth_status).await {
        Ok(_) => {
            // 同步黑白名单
            if auth_status == 1 || auth_status == 2 {
                let mdlx = if auth_status == 1 { "白名单" } else { "黑名单" };
                let bw = BlackWhiteList {
                    id: Some(uuid::Uuid::new_v4().to_string()), mdlx: Some(mdlx.into()), wrjid: Some(id.into()),
                    sqsj: Some(chrono::Utc::now().to_rfc3339()), sqgqsj: None,
                    cjr: None, cjrid: None, cjsj: None, czr: None, czrid: None, czsj: None,
                };
                let _ = state.db.insert_black_white_list(&bw).await;
            }
            Json(ApiResponse::ok("updated".into()))
        }
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn delete(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<String>> {
    match state.db.delete_drone_basic_info(&id).await {
        Ok(_) => Json(ApiResponse::ok("deleted".into())),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn batch_delete(State(state): State<AppState>, Json(ids): Json<Vec<String>>) -> Json<ApiResponse<String>> {
    for id in ids { let _ = state.db.delete_drone_basic_info(&id).await; }
    Json(ApiResponse::ok("batch_deleted".into()))
}
pub async fn get_by_id(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<Option<DroneBasicInfo>>> {
    match state.db.get_drone_basic_info(&id).await {
        Ok(d) => Json(ApiResponse::ok(d)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn get_stats(State(state): State<AppState>) -> Json<ApiResponse<serde_json::Value>> {
    match state.db.drone_basic_stats().await {
        Ok(s) => Json(ApiResponse::ok(s)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
