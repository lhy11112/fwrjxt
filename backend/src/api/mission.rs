use axum::{
    extract::{Path, State},
    Json,
};
use serde::{Deserialize, Serialize};
use uuid::Uuid;

use super::AppState;
use crate::models::*;

/// 任务列表
pub async fn list_missions(
    State(state): State<AppState>,
) -> Json<Vec<InterceptMission>> {
    match state.db.list_missions().await {
        Ok(missions) => Json(missions),
        Err(e) => {
            tracing::warn!("查询任务列表失败: {}", e);
            Json(vec![])
        }
    }
}

/// 创建任务
#[derive(Deserialize)]
pub struct CreateMissionRequest {
    pub target_id: Uuid,
    pub interceptor_ids: Vec<Uuid>,
    pub priority: Option<u32>,
}

pub async fn create_mission(
    State(state): State<AppState>,
    Json(req): Json<CreateMissionRequest>,
) -> Json<InterceptMission> {
    let mission = InterceptMission {
        mission_id: Uuid::new_v4(),
        target_id: req.target_id,
        assigned_interceptors: req.interceptor_ids,
        trajectory: vec![],
        priority: req.priority.unwrap_or(3),
        status: MissionStatus::Planning,
        created_at: chrono::Utc::now(),
        updated_at: chrono::Utc::now(),
    };

    // 持久化到 SQLite
    let db = state.db.clone();
    let mission_clone = mission.clone();
    tokio::spawn(async move {
        if let Err(e) = db.upsert_mission(&mission_clone).await {
            tracing::warn!("任务持久化失败: {}", e);
        }
    });

    // 广播任务更新
    state.bus.broadcast_mission(mission.clone());

    Json(mission)
}

/// 获取任务详情
pub async fn get_mission(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
) -> Json<Option<InterceptMission>> {
    match state.db.get_mission(&id).await {
        Ok(mission) => Json(mission),
        Err(e) => {
            tracing::warn!("查询任务 {} 失败: {}", id, e);
            Json(None)
        }
    }
}

/// 取消任务
pub async fn cancel_mission(
    State(state): State<AppState>,
    Path(id): Path<Uuid>,
) -> Json<serde_json::Value> {
    // 持久化状态更新
    let db = state.db.clone();
    let id_clone = id;
    tokio::spawn(async move {
        if let Err(e) = db.update_mission_status(&id_clone, &MissionStatus::Cancelled).await {
            tracing::warn!("任务状态持久化失败: {}", e);
        }
    });

    Json(serde_json::json!({"status": "cancelled", "mission_id": id}))
}

/// 单目标拦截规划
#[derive(Deserialize)]
pub struct PlanInterceptRequest {
    pub target_id: Uuid,
    pub interceptor_id: Uuid,
}

pub async fn plan_intercept(
    State(state): State<AppState>,
    Json(req): Json<PlanInterceptRequest>,
) -> Json<serde_json::Value> {
    let targets = state.tracker.get_active_tracks().await;
    let target = targets.iter().find(|t| t.target_id == req.target_id);

    match target {
        Some(t) => {
            let interceptor = InterceptorResource {
                resource_id: req.interceptor_id,
                resource_type: ResourceType::KineticInterceptor,
                name: "Interceptor".into(),
                position: t.position.clone(),
                status: ResourceStatus::Available,
                capabilities: vec![],
                fuel_remaining: 100.0,
                max_range: 5000.0,
            };

            match state.planner.plan_single_intercept(t, &interceptor, &[]) {
                Ok(mission) => {
                    // 持久化新任务
                    let db = state.db.clone();
                    let mission_clone = mission.clone();
                    tokio::spawn(async move {
                        if let Err(e) = db.upsert_mission(&mission_clone).await {
                            tracing::warn!("任务持久化失败: {}", e);
                        }
                    });
                    // 广播任务更新
                    state.bus.broadcast_mission(mission.clone());
                    Json(serde_json::json!(mission))
                }
                Err(e) => Json(serde_json::json!({"error": e.to_string()})),
            }
        }
        None => Json(serde_json::json!({"error": "Target not found"})),
    }
}

/// 多目标拦截规划
#[derive(Deserialize)]
pub struct PlanMultiInterceptRequest {
    pub target_ids: Vec<Uuid>,
    pub interceptor_ids: Vec<Uuid>,
}

pub async fn plan_multi_intercept(
    State(state): State<AppState>,
    Json(req): Json<PlanMultiInterceptRequest>,
) -> Json<serde_json::Value> {
    let targets = state.tracker.get_active_tracks().await;

    let selected_targets: Vec<&TrackedTarget> = targets.iter()
        .filter(|t| req.target_ids.contains(&t.target_id))
        .collect();

    let interceptors: Vec<InterceptorResource> = req.interceptor_ids.iter().map(|id| {
        InterceptorResource {
            resource_id: *id,
            resource_type: ResourceType::KineticInterceptor,
            name: "Interceptor".into(),
            position: GeoPosition { latitude: 0.0, longitude: 0.0, altitude: 0.0 },
            status: ResourceStatus::Available,
            capabilities: vec![],
            fuel_remaining: 100.0,
            max_range: 5000.0,
        }
    }).collect();

    let owned_targets: Vec<TrackedTarget> = selected_targets.into_iter().cloned().collect();
    match state.planner.plan_multi_intercept(&owned_targets, &interceptors, &[]) {
        Ok(missions) => {
            // 持久化所有任务
            let db = state.db.clone();
            let bus = state.bus.clone();
            let missions_clone = missions.clone();
            tokio::spawn(async move {
                for mission in &missions_clone {
                    if let Err(e) = db.upsert_mission(mission).await {
                        tracing::warn!("任务持久化失败: {}", e);
                    }
                    bus.broadcast_mission(mission.clone());
                }
            });
            Json(serde_json::json!(missions))
        }
        Err(e) => Json(serde_json::json!({"error": e.to_string()})),
    }
}
