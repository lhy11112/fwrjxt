/// 空域管理 API (hsim_wrj_ky)
use axum::{extract::{Path, Query, State}, Json};
use super::AppState;
use crate::models::*;

pub async fn list(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<Airspace>>> {
    let page = query.page.unwrap_or(1); let page_size = query.page_size.unwrap_or(20).min(100);
    match state.db.list_airspaces(page as i64, page_size as i64, &query.filters).await {
        Ok((r, t)) => Json(ApiResponse::ok(PageResult { records: r, total: t, page, page_size })),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn add(State(state): State<AppState>, Json(mut d): Json<Airspace>) -> Json<ApiResponse<Airspace>> {
    if d.id.is_none() { d.id = Some(uuid::Uuid::new_v4().to_string()); }
    match state.db.insert_airspace(&d).await {
        Ok(_) => Json(ApiResponse::ok(d)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn edit(State(state): State<AppState>, Json(d): Json<Airspace>) -> Json<ApiResponse<Airspace>> {
    match state.db.update_airspace(&d).await {
        Ok(_) => Json(ApiResponse::ok(d)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn delete(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<String>> {
    match state.db.delete_airspace(&id).await {
        Ok(_) => Json(ApiResponse::ok("deleted".into())),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn batch_delete(State(state): State<AppState>, Json(ids): Json<Vec<String>>) -> Json<ApiResponse<String>> {
    for id in ids { let _ = state.db.delete_airspace(&id).await; }
    Json(ApiResponse::ok("batch_deleted".into()))
}
pub async fn get_by_id(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<Option<Airspace>>> {
    match state.db.get_airspace(&id).await {
        Ok(d) => Json(ApiResponse::ok(d)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
