use serde::{Deserialize, Serialize};
use uuid::Uuid;

/// 传感器类型
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum SensorType {
    Radar,
    Rf,
    EoIr,
    Acoustic,
    Lidar,
}

/// 传感器数据帧
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SensorFrame {
    pub frame_id: Uuid,
    pub sensor_id: Uuid,
    pub sensor_type: SensorType,
    pub timestamp: String,
    pub latitude: f64,
    pub longitude: f64,
    pub altitude: f64,
    pub confidence: f64,
    pub payload_json: String,
}

/// SDK 配置
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SdkConfig {
    pub server_url: String,
    pub api_key: Option<String>,
    pub reconnect_interval_secs: u64,
    pub batch_size: usize,
}

impl Default for SdkConfig {
    fn default() -> Self {
        Self {
            server_url: "http://127.0.0.1:8080".into(),
            api_key: None,
            reconnect_interval_secs: 5,
            batch_size: 100,
        }
    }
}
