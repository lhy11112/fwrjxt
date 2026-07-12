use serde::{Deserialize, Serialize};
use std::path::Path;

/// 系统配置
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AppConfig {
    pub server: ServerConfig,
    pub database: DatabaseConfig,
    pub storage: StorageConfig,
    pub map: MapConfig,
    pub sensor: SensorConfig,
    pub planner: PlannerConfig,
    pub threat: ThreatConfig,
    pub log: LogConfig,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ServerConfig {
    pub host: String,
    pub port: u16,
    pub ws_port: u16,
    pub grpc_port: u16,
    pub enable_tls: bool,
    pub cert_path: Option<String>,
    pub key_path: Option<String>,
}

impl Default for ServerConfig {
    fn default() -> Self {
        Self {
            host: "0.0.0.0".into(),
            port: 8080,
            ws_port: 8081,
            grpc_port: 50051,
            enable_tls: false,
            cert_path: None,
            key_path: None,
        }
    }
}

/// SQLite 数据库配置
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DatabaseConfig {
    /// SQLite 数据库文件路径
    pub path: String,
    /// 连接池大小（WAL 模式下支持多读）
    pub max_connections: u32,
    /// 启动时自动执行 Schema 迁移
    pub auto_migrate: bool,
}

impl Default for DatabaseConfig {
    fn default() -> Self {
        Self {
            path: "./data/hsimc2.db".into(),
            max_connections: 10,
            auto_migrate: true,
        }
    }
}

/// 本地文件存储配置
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct StorageConfig {
    /// 统一数据根目录
    pub data_dir: String,
    /// 传感器原始数据存储
    pub sensor_data_dir: String,
    /// 日志归档目录
    pub log_dir: String,
    /// 数据导出目录
    pub export_dir: String,
    /// 配置快照备份目录
    pub config_snapshot_dir: String,
    /// 传感器数据保留天数
    pub sensor_retention_days: u32,
}

impl Default for StorageConfig {
    fn default() -> Self {
        Self {
            data_dir: "./data".into(),
            sensor_data_dir: "./data/sensors".into(),
            log_dir: "./data/logs".into(),
            export_dir: "./data/exports".into(),
            config_snapshot_dir: "./data/config_snapshots".into(),
            sensor_retention_days: 30,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SensorConfig {
    pub data_dir: String,
    pub max_frame_buffer: usize,
    pub sample_rate_hz: f64,
    pub time_sync_protocol: String,
}

impl Default for SensorConfig {
    fn default() -> Self {
        Self {
            data_dir: "./data/sensors".into(),
            max_frame_buffer: 10000,
            sample_rate_hz: 10.0,
            time_sync_protocol: "PTP".into(),
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct PlannerConfig {
    pub max_interceptors: u32,
    pub rrt_max_iterations: u32,
    pub replan_interval_ms: u64,
    pub collision_radius_m: f64,
    pub min_safe_distance_m: f64,
}

impl Default for PlannerConfig {
    fn default() -> Self {
        Self {
            max_interceptors: 20,
            rrt_max_iterations: 5000,
            replan_interval_ms: 500,
            collision_radius_m: 10.0,
            min_safe_distance_m: 50.0,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ThreatConfig {
    pub red_threshold: f64,
    pub yellow_threshold: f64,
    pub intent_model_path: String,
    pub max_eval_time_ms: u64,
}

impl Default for ThreatConfig {
    fn default() -> Self {
        Self {
            red_threshold: 0.75,
            yellow_threshold: 0.4,
            intent_model_path: "./models/intent.onnx".into(),
            max_eval_time_ms: 500,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct MapConfig {
    pub tile_server_url: String,
    pub cache_dir: String,
    pub cache_ttl_days: u32,
    pub center_lat: f64,
    pub center_lon: f64,
    pub default_zoom: u32,
}

impl Default for MapConfig {
    fn default() -> Self {
        Self {
            tile_server_url: "https://tile.openstreetmap.org/{z}/{x}/{y}.png".into(),
            cache_dir: "./data/map_tiles".into(),
            cache_ttl_days: 30,
            center_lat: 34.08,
            center_lon: 108.94,
            default_zoom: 12,
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct LogConfig {
    pub level: String,
    pub format: String,
    pub output_file: Option<String>,
}

impl Default for LogConfig {
    fn default() -> Self {
        Self {
            level: "info".into(),
            format: "json".into(),
            output_file: None,
        }
    }
}

impl Default for AppConfig {
    fn default() -> Self {
        Self {
            server: ServerConfig::default(),
            database: DatabaseConfig::default(),
            storage: StorageConfig::default(),
            map: MapConfig::default(),
            sensor: SensorConfig::default(),
            planner: PlannerConfig::default(),
            threat: ThreatConfig::default(),
            log: LogConfig::default(),
        }
    }
}

impl AppConfig {
    pub fn from_file(path: impl AsRef<Path>) -> anyhow::Result<Self> {
        let content = std::fs::read_to_string(path)?;
        let config: AppConfig = toml::from_str(&content)?;
        Ok(config)
    }

    pub fn from_env() -> anyhow::Result<Self> {
        // 加载 .env 文件（如果存在）
        let _ = dotenvy::dotenv();

        Ok(AppConfig {
            server: ServerConfig {
                port: std::env::var("HSIMC2_PORT")
                    .ok()
                    .and_then(|v| v.parse().ok())
                    .unwrap_or(8080),
                ..Default::default()
            },
            database: DatabaseConfig {
                path: std::env::var("HSIMC2_DB_PATH")
                    .unwrap_or_else(|_| "./data/hsimc2.db".into()),
                ..Default::default()
            },
            storage: StorageConfig {
                data_dir: std::env::var("HSIMC2_DATA_DIR")
                    .unwrap_or_else(|_| "./data".into()),
                ..Default::default()
            },
            ..Default::default()
        })
    }
}
