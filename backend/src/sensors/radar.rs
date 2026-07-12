use async_trait::async_trait;
use uuid::Uuid;

use super::SensorAdapter;
use crate::models::*;

/// 雷达传感器适配器
pub struct RadarAdapter {
    sensor_id: Uuid,
    config: RadarConfig,
}

#[derive(Debug, Clone)]
pub struct RadarConfig {
    pub connection_string: String,
    pub frequency_ghz: f64,
    pub range_km: f64,
    pub update_rate_hz: f64,
}

impl Default for RadarConfig {
    fn default() -> Self {
        Self {
            connection_string: "tcp://radar-sim:9000".into(),
            frequency_ghz: 9.4,
            range_km: 20.0,
            update_rate_hz: 10.0,
        }
    }
}

impl RadarAdapter {
    pub fn new(sensor_id: Uuid, config: RadarConfig) -> Self {
        Self { sensor_id, config }
    }
}

#[async_trait]
impl SensorAdapter for RadarAdapter {
    fn sensor_type(&self) -> SensorType {
        SensorType::Radar
    }

    fn sensor_id(&self) -> Uuid {
        self.sensor_id
    }

    async fn connect(&self) -> anyhow::Result<()> {
        tracing::info!("Radar connected: {} at {}", self.sensor_id, self.config.connection_string);
        Ok(())
    }

    async fn disconnect(&self) -> anyhow::Result<()> {
        tracing::info!("Radar disconnected: {}", self.sensor_id);
        Ok(())
    }

    async fn read_frame(&self) -> anyhow::Result<SensorFrame> {
        // TODO: 实际读取雷达硬件数据
        // 现在返回模拟数据
        Ok(SensorFrame {
            frame_id: Uuid::new_v4(),
            sensor_id: self.sensor_id,
            sensor_type: SensorType::Radar,
            timestamp: chrono::Utc::now(),
            position: GeoPosition {
                latitude: 39.9042,
                longitude: 116.4074,
                altitude: 50.0,
            },
            confidence: 0.95,
            payload: SensorPayload::Radar(RadarDetection {
                range: 1500.0,
                azimuth: 60.0,
                elevation: 15.0,
                rcs: 0.05,
                radial_velocity: -20.0,
                snr: 25.0,
            }),
        })
    }

    async fn status(&self) -> anyhow::Result<SensorStatus> {
        Ok(SensorStatus::Online)
    }
}
