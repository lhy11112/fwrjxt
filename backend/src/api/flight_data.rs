/// 飞行数据查询 API (hsim_wrj_fxsj_zcbw, hsim_wrj_fxsj_jx, hsim_wrj_fxsj_remote)
use axum::{extract::{Path, Query, State}, Json};
use super::AppState;
use crate::models::*;

// ---- 报文协议 ----
pub async fn list_detect_msgs(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<DroneDetectMsg>>> {
    let page = query.page.unwrap_or(1); let page_size = query.page_size.unwrap_or(20).min(100);
    match state.db.list_drone_detect_msgs(page as i64, page_size as i64, &query.filters).await {
        Ok((r, t)) => Json(ApiResponse::ok(PageResult { records: r, total: t, page, page_size })),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn get_detect_msg(State(state): State<AppState>, Path(id): Path<i64>) -> Json<ApiResponse<Option<DroneDetectMsg>>> {
    match state.db.get_drone_detect_msg(id).await {
        Ok(d) => Json(ApiResponse::ok(d)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

// ---- 解析协议 ----
pub async fn list_df_data(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<DroneDfData>>> {
    let page = query.page.unwrap_or(1); let page_size = query.page_size.unwrap_or(20).min(100);
    match state.db.list_drone_df_data(page as i64, page_size as i64, &query.filters).await {
        Ok((r, t)) => Json(ApiResponse::ok(PageResult { records: r, total: t, page, page_size })),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

// ---- Remote协议 ----
pub async fn list_remote_data(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<DroneRemoteData>>> {
    let page = query.page.unwrap_or(1); let page_size = query.page_size.unwrap_or(20).min(100);
    match state.db.list_drone_remote_data(page as i64, page_size as i64, &query.filters).await {
        Ok((r, t)) => Json(ApiResponse::ok(PageResult { records: r, total: t, page, page_size })),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
