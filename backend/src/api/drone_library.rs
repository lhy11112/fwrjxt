/// 无人机特征库 API (hsim_wrj_tzk)
use axum::{extract::{Path, Query, State}, Json};
use super::AppState;
use crate::models::*;

pub async fn list(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<DroneFeatureLib>>> {
    let page = query.page.unwrap_or(1); let page_size = query.page_size.unwrap_or(20).min(100);
    match state.db.list_drone_feature_libs(page as i64, page_size as i64, &query.filters).await {
        Ok((r, t)) => Json(ApiResponse::ok(PageResult { records: r, total: t, page, page_size })),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn add(State(state): State<AppState>, Json(mut d): Json<DroneFeatureLib>) -> Json<ApiResponse<DroneFeatureLib>> {
    if d.id.is_none() { d.id = Some(uuid::Uuid::new_v4().to_string()); }
    match state.db.insert_drone_feature_lib(&d).await {
        Ok(_) => Json(ApiResponse::ok(d)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn edit(State(state): State<AppState>, Json(d): Json<DroneFeatureLib>) -> Json<ApiResponse<DroneFeatureLib>> {
    match state.db.update_drone_feature_lib(&d).await {
        Ok(_) => Json(ApiResponse::ok(d)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn delete(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<String>> {
    match state.db.delete_drone_feature_lib(&id).await {
        Ok(_) => Json(ApiResponse::ok("deleted".into())),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn batch_delete(State(state): State<AppState>, Json(ids): Json<Vec<String>>) -> Json<ApiResponse<String>> {
    for id in ids { let _ = state.db.delete_drone_feature_lib(&id).await; }
    Json(ApiResponse::ok("batch_deleted".into()))
}
pub async fn get_by_id(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<Option<DroneFeatureLib>>> {
    match state.db.get_drone_feature_lib(&id).await {
        Ok(d) => Json(ApiResponse::ok(d)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
