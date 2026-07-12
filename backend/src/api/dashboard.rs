use axum::{
    extract::State,
    Json,
};
use serde::{Deserialize, Serialize};
use uuid::Uuid;

use super::AppState;

// ==================== 健康检查 ====================

#[derive(Serialize)]
pub struct HealthResponse {
    pub status: String,
    pub version: String,
    pub uptime_seconds: u64,
    pub engine_running: bool,
    pub db_connected: bool,
}

pub async fn health_check(
    State(state): State<AppState>,
) -> Json<HealthResponse> {
    // 简单检查数据库连接
    let db_connected = sqlx::query("SELECT 1")
        .execute(state.db.pool())
        .await
        .is_ok();

    Json(HealthResponse {
        status: "ok".into(),
        version: "0.1.0".into(),
        uptime_seconds: 0,
        engine_running: state.engine.is_running(),
        db_connected,
    })
}

// ==================== 系统指标 ====================

#[derive(Serialize)]
pub struct MetricsResponse {
    pub total_frames: u64,
    pub active_tracks: u32,
    pub active_missions: u32,
    pub sensors_online: usize,
    pub avg_fusion_latency_ms: f64,
}

pub async fn system_metrics(
    State(state): State<AppState>,
) -> Json<MetricsResponse> {
    let metrics = state.engine.metrics();
    let active_tracks = state.tracker.track_count().await;
    let sensors_online = state.sensors.online_count().await;

    Json(MetricsResponse {
        total_frames: metrics.total_frames_processed,
        active_tracks: active_tracks as u32,
        active_missions: 0,
        sensors_online,
        avg_fusion_latency_ms: metrics.avg_fusion_latency_ms,
    })
}

// ==================== 配置管理 ====================

#[derive(Serialize)]
pub struct ConfigResponse {
    pub server: serde_json::Value,
    pub sensor: serde_json::Value,
    pub planner: serde_json::Value,
}

pub async fn get_config(
    State(state): State<AppState>,
) -> Json<ConfigResponse> {
    Json(ConfigResponse {
        server: serde_json::to_value(&state.config.server).unwrap_or_default(),
        sensor: serde_json::to_value(&state.config.sensor).unwrap_or_default(),
        planner: serde_json::to_value(&state.config.planner).unwrap_or_default(),
    })
}

#[derive(Deserialize)]
pub struct ConfigUpdate {
    pub key: String,
    pub value: serde_json::Value,
}

pub async fn update_config(
    State(state): State<AppState>,
    Json(update): Json<ConfigUpdate>,
) -> Json<serde_json::Value> {
    tracing::info!("配置更新请求: {} = {}", update.key, update.value);
    // 保存配置快照
    let file_storage = state.file_storage.clone();
    let config = state.config.clone();
    tokio::spawn(async move {
        if let Err(e) = file_storage.save_config_snapshot(&config).await {
            tracing::warn!("配置快照保存失败: {}", e);
        }
    });
    Json(serde_json::json!({"status": "updated"}))
}

// ==================== 态势数据 ====================

#[derive(Serialize)]
pub struct TargetsResponse {
    pub count: usize,
    pub targets: Vec<crate::models::TrackedTarget>,
}

pub async fn get_targets(
    State(state): State<AppState>,
) -> Json<TargetsResponse> {
    // 优先从内存 tracker 获取，如果为空则从数据库加载
    let mut targets = state.tracker.get_active_tracks().await;
    if targets.is_empty() {
        match state.db.get_active_targets().await {
            Ok(db_targets) => {
                targets = db_targets;
            }
            Err(e) => {
                tracing::warn!("从数据库加载目标列表失败: {}", e);
            }
        }
    }
    let count = targets.len();
    Json(TargetsResponse { count, targets })
}

#[derive(Serialize)]
pub struct ThreatsResponse {
    pub count: usize,
    pub threats: Vec<crate::models::TrackedTarget>,
}

pub async fn get_threats(
    State(state): State<AppState>,
) -> Json<ThreatsResponse> {
    let targets = state.tracker.get_active_tracks().await;
    // 按威胁等级排序：Red > Yellow > Green
    let mut threats = targets;
    threats.sort_by(|a, b| {
        let score = |t: &crate::models::TrackedTarget| -> u8 {
            match t.threat_level {
                crate::models::ThreatLevel::Red => 3,
                crate::models::ThreatLevel::Yellow => 2,
                crate::models::ThreatLevel::Green => 1,
            }
        };
        score(b).cmp(&score(a))
    });
    let count = threats.len();
    Json(ThreatsResponse { count, threats })
}

#[derive(Serialize)]
pub struct TrackHistoryResponse {
    pub target_id: Uuid,
    pub track_points: Vec<crate::models::TrackPoint>,
}

pub async fn get_track_history(
    State(state): State<AppState>,
    axum::extract::Path(id): axum::extract::Path<Uuid>,
) -> Json<TrackHistoryResponse> {
    // 先从 tracker 内存获取
    let target = state.tracker.get_track(&id).await;
    let track_points = match target {
        Some(t) if !t.track_history.is_empty() => t.track_history,
        _ => {
            // 回退到 SQLite 历史记录
            state.db.get_target_history(&id).await.unwrap_or_default()
        }
    };

    Json(TrackHistoryResponse {
        target_id: id,
        track_points,
    })
}

// ==================== 资源管理 ====================

pub async fn list_resources(
    State(_state): State<AppState>,
) -> Json<Vec<crate::models::InterceptorResource>> {
    // TODO: 从资源管理器读取
    Json(vec![])
}

pub async fn get_resource(
    State(_state): State<AppState>,
    axum::extract::Path(_id): axum::extract::Path<Uuid>,
) -> Json<Option<crate::models::InterceptorResource>> {
    Json(None)
}

// ==================== 对话式交互 ====================

#[derive(Deserialize)]
pub struct ChatRequest {
    pub message: String,
    pub context: Option<serde_json::Value>,
}

#[derive(Serialize)]
pub struct ChatResponse {
    pub reply: String,
    pub actions: Vec<ChatAction>,
}

#[derive(Serialize)]
pub struct ChatAction {
    pub action_type: String,
    pub params: serde_json::Value,
}

pub async fn chat_message(
    State(state): State<AppState>,
    Json(req): Json<ChatRequest>,
) -> Json<ChatResponse> {
    let ai_client = crate::ai::DeepSeekClient::new(crate::ai::DeepSeekConfig::default());

    // 从 context 中提取对话历史（如果有）
    let history: Vec<(String, String)> = req
        .context
        .as_ref()
        .and_then(|ctx| ctx.get("history"))
        .and_then(|h| serde_json::from_value::<Vec<[String; 2]>>(h.clone()).ok())
        .map(|h| h.into_iter().map(|a| (a[0].clone(), a[1].clone())).collect())
        .unwrap_or_default();

    match ai_client.chat(&req.message, &history).await {
        Ok(reply) => {
            // 尝试从回复中提取指令（简单关键词匹配）
            let mut actions = vec![];
            let lower = reply.to_lowercase();
            if lower.contains("干扰") || lower.contains("jam") {
                actions.push(ChatAction {
                    action_type: "jam".into(),
                    params: serde_json::json!({"target": "selected"}),
                });
            }
            if lower.contains("诱骗") || lower.contains("spoof") {
                actions.push(ChatAction {
                    action_type: "spoof".into(),
                    params: serde_json::json!({"mode": "gnss"}),
                });
            }
            if lower.contains("拦截") || lower.contains("intercept") {
                actions.push(ChatAction {
                    action_type: "intercept".into(),
                    params: serde_json::json!({"priority": "high"}),
                });
            }

            Json(ChatResponse { reply, actions })
        }
        Err(e) => {
            tracing::error!("DeepSeek API 调用失败: {}", e);
            Json(ChatResponse {
                reply: format!("AI 服务暂时不可用：{}。请检查 API Key 配置或网络连接。", e),
                actions: vec![],
            })
        }
    }
}

// ==================== 数据导出 ====================

#[derive(Deserialize)]
pub struct ExportQuery {
    pub format: Option<String>,  // "json" | "csv", default "json"
}

#[derive(Serialize)]
pub struct ExportResponse {
    pub file_path: String,
    pub record_count: usize,
}

/// 导出目标数据
pub async fn export_targets(
    State(state): State<AppState>,
    axum::extract::Query(query): axum::extract::Query<ExportQuery>,
) -> Json<serde_json::Value> {
    let targets = state.tracker.get_active_tracks().await;
    let format = query.format.as_deref().unwrap_or("json");
    let count = targets.len();

    let result = match format {
        "csv" => state.file_storage.export_targets_csv(&targets).await,
        _ => state.file_storage.export_targets_json(&targets).await,
    };

    match result {
        Ok(path) => Json(serde_json::json!({
            "file_path": path,
            "record_count": count,
            "format": format,
        })),
        Err(e) => Json(serde_json::json!({
            "error": e.to_string()
        })),
    }
}

/// 导出任务数据
pub async fn export_missions(
    State(state): State<AppState>,
    axum::extract::Query(_query): axum::extract::Query<ExportQuery>,
) -> Json<serde_json::Value> {
    let missions = match state.db.list_missions().await {
        Ok(m) => m,
        Err(e) => {
            return Json(serde_json::json!({"error": e.to_string()}));
        }
    };
    let count = missions.len();

    match state.file_storage.export_missions_json(&missions).await {
        Ok(path) => Json(serde_json::json!({
            "file_path": path,
            "record_count": count,
            "format": "json",
        })),
        Err(e) => Json(serde_json::json!({
            "error": e.to_string()
        })),
    }
}
