/// 设备管理 API: 设备配置 CRUD + 设备日志查询
use axum::{
    extract::{Path, Query, State},
    Json,
};

use super::AppState;
use crate::models::*;

// ==================== 设备配置 CRUD ====================

pub async fn list_devices(
    State(state): State<AppState>,
    Query(query): Query<PageQuery>,
) -> Json<ApiResponse<PageResult<DeviceConfig>>> {
    let page = query.page.unwrap_or(1);
    let page_size = query.page_size.unwrap_or(20).min(100);
    match state.db.list_device_configs(page as i64, page_size as i64, &query.filters).await {
        Ok((records, total)) => Json(ApiResponse::ok(PageResult { records, total, page, page_size })),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

pub async fn add_device(
    State(state): State<AppState>,
    Json(mut device): Json<DeviceConfig>,
) -> Json<ApiResponse<DeviceConfig>> {
    // id 由数据库自增生成，无需手动设置
    match state.db.insert_device_config(&device).await {
        Ok(_) => Json(ApiResponse::ok(device)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

pub async fn edit_device(
    State(state): State<AppState>,
    Json(device): Json<DeviceConfig>,
) -> Json<ApiResponse<DeviceConfig>> {
    match state.db.update_device_config(&device).await {
        Ok(_) => Json(ApiResponse::ok(device)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

pub async fn delete_device(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> Json<ApiResponse<String>> {
    match state.db.delete_device_config(id).await {
        Ok(_) => Json(ApiResponse::ok("deleted".into())),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

pub async fn batch_delete_devices(
    State(state): State<AppState>,
    Json(ids): Json<Vec<i64>>,
) -> Json<ApiResponse<String>> {
    for id in ids {
        let _ = state.db.delete_device_config(id).await;
    }
    Json(ApiResponse::ok("batch_deleted".into()))
}

pub async fn get_device(
    State(state): State<AppState>,
    Path(id): Path<i64>,
) -> Json<ApiResponse<Option<DeviceConfig>>> {
    match state.db.get_device_config(id).await {
        Ok(d) => Json(ApiResponse::ok(d)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

// ==================== 设备心跳日志 ====================

pub async fn list_heartbeats(
    State(state): State<AppState>,
    Query(query): Query<PageQuery>,
) -> Json<ApiResponse<PageResult<DeviceHeartbeat>>> {
    let page = query.page.unwrap_or(1);
    let page_size = query.page_size.unwrap_or(20).min(100);
    match state.db.list_device_heartbeats(page as i64, page_size as i64, &query.filters).await {
        Ok((records, total)) => Json(ApiResponse::ok(PageResult { records, total, page, page_size })),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

// ==================== 设备连接日志 ====================

pub async fn list_connect_logs(
    State(state): State<AppState>,
    Query(query): Query<PageQuery>,
) -> Json<ApiResponse<PageResult<ConnectLog>>> {
    let page = query.page.unwrap_or(1);
    let page_size = query.page_size.unwrap_or(20).min(100);
    match state.db.list_connect_logs(page as i64, page_size as i64, &query.filters).await {
        Ok((records, total)) => Json(ApiResponse::ok(PageResult { records, total, page, page_size })),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

// ==================== 设备操作日志 ====================

pub async fn list_operate_logs(
    State(state): State<AppState>,
    Query(query): Query<PageQuery>,
) -> Json<ApiResponse<PageResult<OperateLog>>> {
    let page = query.page.unwrap_or(1);
    let page_size = query.page_size.unwrap_or(20).min(100);
    match state.db.list_operate_logs(page as i64, page_size as i64, &query.filters).await {
        Ok((records, total)) => Json(ApiResponse::ok(PageResult { records, total, page, page_size })),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

// ==================== 设备主动心跳 ====================

pub async fn list_active_heartbeats(
    State(state): State<AppState>,
    Query(query): Query<PageQuery>,
) -> Json<ApiResponse<PageResult<ActiveHeartbeat>>> {
    let page = query.page.unwrap_or(1);
    let page_size = query.page_size.unwrap_or(20).min(100);
    match state.db.list_active_heartbeats(page as i64, page_size as i64, &query.filters).await {
        Ok((records, total)) => Json(ApiResponse::ok(PageResult { records, total, page, page_size })),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

// ==================== 设备统计 ====================

pub async fn device_stats(
    State(state): State<AppState>,
) -> Json<ApiResponse<serde_json::Value>> {
    match state.db.device_stats().await {
        Ok(stats) => Json(ApiResponse::ok(stats)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
