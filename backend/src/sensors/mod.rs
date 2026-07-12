use std::collections::HashMap;
use tokio::sync::RwLock;
use uuid::Uuid;

use crate::models::*;

/// 标准传感器抽象层 (SSAL)
///
/// 提供统一的多传感器注册、管理和数据接入接口。
/// 所有传感器通过此层注册并提供标准化数据帧。
#[derive(Debug)]
pub struct SensorManager {
    sensors: RwLock<HashMap<Uuid, SensorRegistration>>,
}

#[derive(Debug, Clone)]
pub struct SensorRegistration {
    pub metadata: SensorMetadata,
    pub connected_since: chrono::DateTime<chrono::Utc>,
    pub frames_received: u64,
}

impl SensorManager {
    pub fn new() -> Self {
        Self {
            sensors: RwLock::new(HashMap::new()),
        }
    }

    /// 注册传感器
    pub async fn register(&self, metadata: SensorMetadata) -> anyhow::Result<()> {
        let sensor_name = metadata.name.clone();
        let sensor_id = metadata.sensor_id;
        let reg = SensorRegistration {
            metadata,
            connected_since: chrono::Utc::now(),
            frames_received: 0,
        };
        self.sensors.write().await.insert(sensor_id, reg);
        tracing::info!("Sensor registered: {} ({})", sensor_name, sensor_id);
        Ok(())
    }

    /// 注销传感器
    pub async fn unregister(&self, sensor_id: &Uuid) -> anyhow::Result<()> {
        self.sensors.write().await.remove(sensor_id);
        tracing::info!("Sensor unregistered: {}", sensor_id);
        Ok(())
    }

    /// 获取所有传感器
    pub async fn list(&self) -> Vec<SensorMetadata> {
        self.sensors.read().await
            .values()
            .map(|r| r.metadata.clone())
            .collect()
    }

    /// 获取指定传感器
    pub async fn get(&self, sensor_id: &Uuid) -> Option<SensorMetadata> {
        self.sensors.read().await
            .get(sensor_id)
            .map(|r| r.metadata.clone())
    }

    /// 更新传感器状态
    pub async fn update_status(&self, sensor_id: &Uuid, status: SensorStatus) -> anyhow::Result<()> {
        let mut sensors = self.sensors.write().await;
        if let Some(reg) = sensors.get_mut(sensor_id) {
            reg.metadata.status = status;
            Ok(())
        } else {
            anyhow::bail!("Sensor {} not found", sensor_id)
        }
    }

    /// 记录接收到的帧数
    pub async fn record_frame(&self, sensor_id: &Uuid) {
        if let Some(reg) = self.sensors.write().await.get_mut(sensor_id) {
            reg.frames_received += 1;
        }
    }

    /// 获取在线传感器数量
    pub async fn online_count(&self) -> usize {
        self.sensors.read().await
            .values()
            .filter(|r| r.metadata.status == SensorStatus::Online)
            .count()
    }
}

// ==================== 传感器适配器 Trait ====================

/// 传感器适配器接口
#[async_trait::async_trait]
pub trait SensorAdapter: Send + Sync {
    /// 传感器类型
    fn sensor_type(&self) -> SensorType;

    /// 传感器ID
    fn sensor_id(&self) -> Uuid;

    /// 连接传感器
    async fn connect(&self) -> anyhow::Result<()>;

    /// 断开连接
    async fn disconnect(&self) -> anyhow::Result<()>;

    /// 读取数据帧
    async fn read_frame(&self) -> anyhow::Result<SensorFrame>;

    /// 获取传感器状态
    async fn status(&self) -> anyhow::Result<SensorStatus>;

    /// 校准传感器
    async fn calibrate(&self) -> anyhow::Result<()> {
        Ok(())
    }
}

// ==================== 具体传感器实现 ====================

pub mod radar;
pub mod rf;
pub mod eoir;
pub mod acoustic;

/// 创建默认传感器管理器并注册预定义传感器
pub async fn create_default_sensor_manager() -> SensorManager {
    let mgr = SensorManager::new();

    // 示例：注册默认雷达传感器
    let radar = SensorMetadata {
        sensor_id: Uuid::new_v4(),
        sensor_type: SensorType::Radar,
        name: "AESA 相控阵雷达-01".into(),
        model: "HSimC2-RDR-A1".into(),
        location: GeoPosition {
            latitude: 34.08,
            longitude: 108.94,
            altitude: 80.0,
        },
        status: SensorStatus::Online,
        last_heartbeat: chrono::Utc::now(),
        capabilities: vec![
            "3D_Tracking".into(),
            "MonoPulse".into(),
            "MTI".into(),
            "SAR".into(),
        ],
        config: serde_json::json!({
            "frequency_ghz": 9.4,
            "range_km": 20.0,
            "update_rate_hz": 10.0,
        }),
    };
    mgr.register(radar).await.unwrap();

    // 示例：注册默认RF传感器
    let rf = SensorMetadata {
        sensor_id: Uuid::new_v4(),
        sensor_type: SensorType::Rf,
        name: "SDR 频谱监测-01".into(),
        model: "HSimC2-RF-S1".into(),
        location: GeoPosition {
            latitude: 34.082,
            longitude: 108.942,
            altitude: 50.0,
        },
        status: SensorStatus::Online,
        last_heartbeat: chrono::Utc::now(),
        capabilities: vec![
            "Spectrum_Analysis".into(),
            "Protocol_Identification".into(),
            "Direction_Finding".into(),
        ],
        config: serde_json::json!({
            "freq_range_hz": [2400000000.0, 5850000000.0],
            "bandwidth_hz": 20000000,
        }),
    };
    mgr.register(rf).await.unwrap();

    mgr
}
