use async_trait::async_trait;
use uuid::Uuid;

use super::SensorAdapter;
use crate::models::*;

/// RF/频谱传感器适配器
pub struct RfAdapter {
    sensor_id: Uuid,
    config: RfConfig,
}

#[derive(Debug, Clone)]
pub struct RfConfig {
    pub connection_string: String,
    pub freq_range_hz: (f64, f64),
    pub bandwidth_hz: f64,
}

impl Default for RfConfig {
    fn default() -> Self {
        Self {
            connection_string: "tcp://rf-sim:9001".into(),
            freq_range_hz: (2_400_000_000.0, 5_850_000_000.0),
            bandwidth_hz: 20_000_000.0,
        }
    }
}

impl RfAdapter {
    pub fn new(sensor_id: Uuid, config: RfConfig) -> Self {
        Self { sensor_id, config }
    }
}

#[async_trait]
impl SensorAdapter for RfAdapter {
    fn sensor_type(&self) -> SensorType {
        SensorType::Rf
    }

    fn sensor_id(&self) -> Uuid {
        self.sensor_id
    }

    async fn connect(&self) -> anyhow::Result<()> {
        tracing::info!("RF Sensor connected: {}", self.sensor_id);
        Ok(())
    }

    async fn disconnect(&self) -> anyhow::Result<()> {
        tracing::info!("RF Sensor disconnected: {}", self.sensor_id);
        Ok(())
    }

    async fn read_frame(&self) -> anyhow::Result<SensorFrame> {
        Ok(SensorFrame {
            frame_id: Uuid::new_v4(),
            sensor_id: self.sensor_id,
            sensor_type: SensorType::Rf,
            timestamp: chrono::Utc::now(),
            position: GeoPosition {
                latitude: 39.9042,
                longitude: 116.4074,
                altitude: 30.0,
            },
            confidence: 0.88,
            payload: SensorPayload::Rf(RfSpectrum {
                center_freq_hz: 2_450_000_000.0,
                bandwidth_hz: 20_000_000.0,
                power_db: -45.0,
                protocol_type: Some("OcuSync".into()),
                modulation: Some("OFDM".into()),
            }),
        })
    }

    async fn status(&self) -> anyhow::Result<SensorStatus> {
        Ok(SensorStatus::Online)
    }
}
