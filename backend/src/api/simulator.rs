use axum::{extract::State, Json};
use serde::Serialize;
use uuid::Uuid;

use super::AppState;

#[derive(Serialize)]
pub struct SimulatorStatus {
    pub running: bool,
    pub target_count: usize,
    pub frame_count: u64,
    pub config: SimConfigInfo,
}

#[derive(Serialize)]
pub struct SimConfigInfo {
    pub update_interval_ms: u64,
    pub num_targets: usize,
    pub area_radius_km: f64,
    pub center_lat: f64,
    pub center_lon: f64,
}

#[derive(Serialize)]
pub struct SimTargetInfo {
    pub target_id: Uuid,
    pub classification: String,
    pub latitude: f64,
    pub longitude: f64,
    pub altitude: f64,
    pub speed: f64,
    pub heading: f64,
    pub behavior: String,
    pub threat_level: String,
    pub active: bool,
}

/// 获取仿真引擎状态
pub async fn get_simulator_status(
    State(state): State<AppState>,
) -> Json<SimulatorStatus> {
    let running = state.simulator.is_running().await;
    let target_count = state.simulator.target_count().await;
    let frame_count = state.simulator.frame_count().await;
    let config = crate::simulator::SimulatorConfig::default();

    Json(SimulatorStatus {
        running,
        target_count,
        frame_count,
        config: SimConfigInfo {
            update_interval_ms: config.update_interval_ms,
            num_targets: config.num_targets,
            area_radius_km: config.area_radius_km,
            center_lat: config.center_lat,
            center_lon: config.center_lon,
        },
    })
}

/// 获取仿真目标列表
pub async fn get_simulator_targets(
    State(state): State<AppState>,
) -> Json<Vec<SimTargetInfo>> {
    let sim_targets = state.simulator.get_targets().await;

    let targets: Vec<SimTargetInfo> = sim_targets.iter().map(|t| {
        SimTargetInfo {
            target_id: t.target_id,
            classification: format!("{:?}", t.classification),
            latitude: t.position.latitude,
            longitude: t.position.longitude,
            altitude: t.position.altitude,
            speed: t.speed,
            heading: t.heading,
            behavior: format!("{:?}", t.behavior),
            threat_level: format!("{:?}", t.threat_level),
            active: t.is_active,
        }
    }).collect();

    Json(targets)
}

/// 重启仿真引擎
pub async fn restart_simulator(
    State(state): State<AppState>,
) -> Json<serde_json::Value> {
    state.simulator.stop().await;
    state.simulator.start().await;

    Json(serde_json::json!({
        "status": "restarted",
        "running": true,
    }))
}
