use axum::{
    extract::{Path, State},
    Json,
};
use uuid::Uuid;

use super::AppState;
use crate::models::*;

/// 获取所有传感器列表
pub async fn list_sensors(
    State(state): State<AppState>,
) -> Json<Vec<SensorMetadata>> {
    // 优先从内存 SensorManager 读取；若为空则从 SQLite 加载
    let mut sensors = state.sensors.list().await;
    if sensors.is_empty() {
        match state.db.list_sensors().await {
            Ok(db_sensors) => {
                // 将数据库中的传感器同步到内存管理器
                for s in &db_sensors {
                    let _ = state.sensors.register(s.clone()).await;
                }
                sensors = db_sensors;
            }
            Err(e) => {
                tracing::warn!("从数据库加载传感器列表失败: {}", e);
            }
        }
    }
    Json(sensors)
}

/// 获取单个传感器详情
pub async fn get_sensor(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
) -> Json<Option<SensorMetadata>> {
    // 先查内存
    let sensor = state.sensors.get(&id).await;
    if sensor.is_some() {
        return Json(sensor);
    }
    // 回退到数据库
    match state.db.get_sensor(&id).await {
        Ok(db_sensor) => {
            if let Some(ref s) = db_sensor {
                let _ = state.sensors.register(s.clone()).await;
            }
            Json(db_sensor)
        }
        Err(e) => {
            tracing::warn!("数据库查询传感器 {} 失败: {}", id, e);
            Json(None)
        }
    }
}

/// 获取传感器状态
pub async fn get_sensor_status(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
) -> Json<serde_json::Value> {
    let sensor = state.sensors.get(&id).await;
    match sensor {
        Some(s) => Json(serde_json::json!({
            "sensor_id": s.sensor_id,
            "name": s.name,
            "status": s.status,
            "last_heartbeat": s.last_heartbeat,
        })),
        None => Json(serde_json::json!({"error": "Sensor not found"})),
    }
}

/// 校准传感器
pub async fn calibrate_sensor(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
) -> Json<serde_json::Value> {
    // 更新内存状态
    let _ = state.sensors.update_status(&id, SensorStatus::Calibrating).await;
    // 持久化到 SQLite
    let db = state.db.clone();
    let id_clone = id;
    let _ = tokio::spawn(async move {
        if let Err(e) = db.update_sensor_status(&id_clone, &SensorStatus::Calibrating).await {
            tracing::warn!("传感器状态持久化失败: {}", e);
        }
    });
    Json(serde_json::json!({
        "status": "calibrating",
        "sensor_id": id,
    }))
}
