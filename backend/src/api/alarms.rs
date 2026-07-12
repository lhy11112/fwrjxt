/// 告警管理 API (hsim_wrj_gjjl)
use axum::{extract::{Path, Query, State}, Json};
use super::AppState;
use crate::models::*;

pub async fn list(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<AlarmRecord>>> {
    let page = query.page.unwrap_or(1); let page_size = query.page_size.unwrap_or(20).min(100);
    match state.db.list_alarm_records(page as i64, page_size as i64, &query.filters).await {
        Ok((r, t)) => Json(ApiResponse::ok(PageResult { records: r, total: t, page, page_size })),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn add(State(state): State<AppState>, Json(mut d): Json<AlarmRecord>) -> Json<ApiResponse<AlarmRecord>> {
    if d.id.is_none() { d.id = Some(uuid::Uuid::new_v4().to_string()); }
    match state.db.insert_alarm_record(&d).await {
        Ok(_) => Json(ApiResponse::ok(d)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn edit(State(state): State<AppState>, Json(d): Json<AlarmRecord>) -> Json<ApiResponse<AlarmRecord>> {
    match state.db.update_alarm_record(&d).await {
        Ok(_) => Json(ApiResponse::ok(d)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn delete(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<String>> {
    match state.db.delete_alarm_record(&id).await {
        Ok(_) => Json(ApiResponse::ok("deleted".into())),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn batch_delete(State(state): State<AppState>, Json(ids): Json<Vec<String>>) -> Json<ApiResponse<String>> {
    for id in ids { let _ = state.db.delete_alarm_record(&id).await; }
    Json(ApiResponse::ok("batch_deleted".into()))
}
pub async fn get_by_id(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<Option<AlarmRecord>>> {
    match state.db.get_alarm_record(&id).await {
        Ok(d) => Json(ApiResponse::ok(d)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
/// 告警统计（按类型/时间）
pub async fn alarm_stats(State(state): State<AppState>, Query(params): Query<std::collections::HashMap<String, String>>) -> Json<ApiResponse<serde_json::Value>> {
    match state.db.alarm_stats(&params).await {
        Ok(s) => Json(ApiResponse::ok(s)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
