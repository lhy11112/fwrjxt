use std::sync::Arc;

use axum::serve;
use tokio::net::TcpListener;
use tower_http::cors::CorsLayer;
use tower_http::trace::TraceLayer;
use tracing_subscriber::EnvFilter;

use hsimc2_backend::api::{self, AppState};
use hsimc2_backend::bus::{AlertEvent, AlertSeverity, AlertType, MessageBus};
use hsimc2_backend::config::AppConfig;
use hsimc2_backend::core::CoreEngine;
use hsimc2_backend::fusion::FusionEngine;
use hsimc2_backend::planner::{MissionPlanner, PlannerConfig};
use hsimc2_backend::simulator::{SensorSimulator, SimulatorConfig};
use hsimc2_backend::storage::{init_database, FileStorage, Repository};
use hsimc2_backend::threat::{ThreatAssessor, ThreatConfig};
use hsimc2_backend::tracker::{MultiTargetTracker, TrackerConfig};

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    // 初始化日志
    tracing_subscriber::fmt()
        .with_env_filter(EnvFilter::try_from_default_env().unwrap_or_else(|_| "info".into()))
        .json()
        .init();

    tracing::info!("╔══════════════════════════════════════════════╗");
    tracing::info!("║  HSimC2 反无人机一体化指挥控制平台 v0.1.0     ║");
    tracing::info!("╚══════════════════════════════════════════════╝");
    tracing::info!("正在初始化系统...");

    // 加载配置（优先 config.toml，不存在则从环境变量 + 默认值）
    let config = AppConfig::from_file("config.toml")
        .or_else(|_| AppConfig::from_env())
        .unwrap_or_default();
    tracing::info!("配置加载完成");

    // ---- Phase 1: 初始化 SQLite 数据库 ----
    let db_pool = init_database(&config.database).await?;
    let db = Repository::new(db_pool);
    tracing::info!("SQLite 数据库初始化完成");

    // ---- Phase 1: 初始化本地文件存储 ----
    let file_storage = Arc::new(FileStorage::new(config.storage.clone()));
    file_storage.init()?;
    tracing::info!("本地文件存储初始化完成");

    // ---- 保存启动配置快照 ----
    if let Err(e) = file_storage.save_config_snapshot(&config).await {
        tracing::warn!("配置快照保存失败: {}", e);
    }

    // 初始化核心引擎
    let mut engine = CoreEngine::new();
    engine.start()?;

    // 初始化消息总线
    let bus = Arc::new(MessageBus::new());
    tracing::info!("消息总线初始化完成");

    // 初始化融合引擎
    let fusion = Arc::new(FusionEngine::new());

    // 初始化传感器管理器并从 SQLite 加载已注册传感器
    let sensors = Arc::new(hsimc2_backend::sensors::SensorManager::new());
    match db.list_sensors().await {
        Ok(db_sensors) => {
            for s in &db_sensors {
                if let Err(e) = sensors.register(s.clone()).await {
                    tracing::warn!("从数据库加载传感器 {} 失败: {}", s.sensor_id, e);
                }
            }
            if !db_sensors.is_empty() {
                tracing::info!("从数据库加载了 {} 个传感器", db_sensors.len());
            }
        }
        Err(e) => tracing::warn!("加载传感器列表失败: {}", e),
    }

    // 初始化多目标跟踪器
    let tracker = Arc::new(MultiTargetTracker::new(TrackerConfig::default()));

    // 初始化威胁评估器
    let threat = Arc::new(ThreatAssessor::new(ThreatConfig::default()));

    // 初始化任务规划器
    let planner = Arc::new(MissionPlanner::new(PlannerConfig::default()));

    // ===== 启动传感器仿真引擎 =====
    let simulator = Arc::new(SensorSimulator::new(
        SimulatorConfig::default(),
        bus.clone(),
    ));
    simulator.start().await;
    tracing::info!("🧪 传感器仿真引擎已启动");

    // ===== 后台数据融合任务 =====
    let bus_clone = bus.clone();
    let tracker_clone = tracker.clone();
    let simulator_clone = simulator.clone();
    let db_clone = db.clone();

    tokio::spawn(async move {
        let mut interval = tokio::time::interval(tokio::time::Duration::from_millis(500));
        tracing::info!("🔄 融合管道后台任务已启动");

        loop {
            // 从仿真器获取当前所有模拟目标
            let sim_targets = simulator_clone.get_targets().await;

            // 转换为 TrackedTarget
            let tracked: Vec<_> = sim_targets.iter().filter(|t| t.is_active).map(|t| {
                hsimc2_backend::models::TrackedTarget {
                    target_id: t.target_id,
                    track_id: t.target_id,
                    position: t.position.clone(),
                    velocity: t.velocity.clone(),
                    acceleration: None,
                    classification: t.classification.clone(),
                    threat_level: t.threat_level.clone(),
                    confidence: 0.85,
                    first_seen: t.spawn_time,
                    last_update: chrono::Utc::now(),
                    track_history: vec![hsimc2_backend::models::TrackPoint {
                        timestamp: chrono::Utc::now(),
                        position: t.position.clone(),
                        velocity: t.velocity.clone(),
                    }],
                }
            }).collect();

            if !tracked.is_empty() {
                // 更新跟踪器
                let updated = tracker_clone.update(tracked).await.unwrap_or_default();
                // 广播态势更新
                bus_clone.broadcast_situation(updated.clone());

                // ---- Phase 3: 异步持久化目标数据 ----
                let db = db_clone.clone();
                let targets_for_db = updated.clone();
                tokio::spawn(async move {
                    for target in &targets_for_db {
                        if let Err(e) = db.upsert_target(target).await {
                            tracing::warn!("目标持久化失败 {}: {}", target.target_id, e);
                        }
                        // 持久化最新航迹点
                        if let Some(last_point) = target.track_history.last() {
                            if let Err(e) = db.append_track_point(&target.target_id, last_point).await {
                                tracing::warn!("航迹点持久化失败 {}: {}", target.target_id, e);
                            }
                        }
                    }
                });

                // 检查高危目标 → 发送告警
                for t in &updated {
                    if t.threat_level == hsimc2_backend::models::ThreatLevel::Red {
                        let alert = AlertEvent {
                            alert_id: uuid::Uuid::new_v4(),
                            alert_type: AlertType::ThreatDetected,
                            severity: AlertSeverity::Critical,
                            title: format!("高危目标入侵: {:?}", t.classification),
                            description: format!(
                                "位置: {:.4},{:.4} 高度:{:.0}m 速度:{:.0}m/s",
                                t.position.latitude, t.position.longitude,
                                t.position.altitude,
                                (t.velocity.vn.powi(2) + t.velocity.ve.powi(2)).sqrt()
                            ),
                            source: "仿真引擎".into(),
                            timestamp: chrono::Utc::now(),
                        };
                        // ---- 持久化告警 ----
                        let db = db_clone.clone();
                        let alert_clone = alert.clone();
                        tokio::spawn(async move {
                            if let Err(e) = db.insert_alert(&alert_clone).await {
                                tracing::warn!("告警持久化失败: {}", e);
                            }
                        });

                        bus_clone.send_alert(alert);
                    }
                }

                // 记录融合统计
                let count = updated.len();
                if count > 0 {
                    tracing::debug!("🔄 融合管道: {} 个目标已更新", count);
                }
            }

            interval.tick().await;
        }
    });

    // ===== 后台文件清理任务（每天一次） =====
    let file_storage_clone = file_storage.clone();
    tokio::spawn(async move {
        loop {
            // 每天凌晨执行一次清理
            tokio::time::sleep(tokio::time::Duration::from_secs(86400)).await;
            match file_storage_clone.cleanup_old_files().await {
                Ok(stats) => {
                    if stats.files_deleted > 0 {
                        tracing::info!(
                            "🧹 文件清理完成: {} 文件已删除, {} 字节已释放",
                            stats.files_deleted,
                            stats.bytes_freed
                        );
                    }
                }
                Err(e) => tracing::warn!("文件清理失败: {}", e),
            }
        }
    });

    // ===== 后台数据库清理任务（每小时一次） =====
    let db_cleanup = db.clone();
    tokio::spawn(async move {
        loop {
            tokio::time::sleep(tokio::time::Duration::from_secs(3600)).await;
            match db_cleanup.cleanup_stale_targets(60).await {
                Ok(count) => {
                    if count > 0 {
                        tracing::info!("🧹 数据库清理: {} 个过期目标已标记为非活跃", count);
                    }
                }
                Err(e) => tracing::warn!("数据库清理失败: {}", e),
            }
        }
    });

    // 构建统一的应用状态
    let state = AppState {
        config: config.clone(),
        db,
        file_storage,
        engine: Arc::new(engine),
        bus: bus.clone(),
        sensors,
        fusion,
        tracker,
        threat,
        planner,
        simulator,
    };

    // 构建路由
    let app = api::build_routes(state)
        .layer(TraceLayer::new_for_http())
        .layer(CorsLayer::permissive());

    // 启动 HTTP 服务
    let addr = format!("{}:{}", config.server.host, config.server.port);
    tracing::info!("╔══════════════════════════════════════════════╗");
    tracing::info!("║  HSimC2 服务已启动                          ║");
    tracing::info!("║  HTTP API: http://{}                  ║", addr);
    tracing::info!("║  WebSocket: ws://{}/api/v1/ws        ║", addr);
    tracing::info!("║  SQLite DB: {}              ║", config.database.path);
    tracing::info!("╚══════════════════════════════════════════════╝");

    let listener = TcpListener::bind(&addr).await?;
    serve(listener, app).await?;

    Ok(())
}
