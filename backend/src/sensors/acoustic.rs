use async_trait::async_trait;
use uuid::Uuid;

use super::SensorAdapter;
use crate::models::*;

/// 声学传感器适配器
pub struct AcousticAdapter {
    sensor_id: Uuid,
    config: AcousticConfig,
}

#[derive(Debug, Clone)]
pub struct AcousticConfig {
    pub connection_string: String,
    pub sample_rate_hz: u32,
    pub array_mic_count: u8,
}

impl Default for AcousticConfig {
    fn default() -> Self {
        Self {
            connection_string: "tcp://acoustic-sim:9003".into(),
            sample_rate_hz: 48000,
            array_mic_count: 4,
        }
    }
}

impl AcousticAdapter {
    pub fn new(sensor_id: Uuid, config: AcousticConfig) -> Self {
        Self { sensor_id, config }
    }
}

#[async_trait]
impl SensorAdapter for AcousticAdapter {
    fn sensor_type(&self) -> SensorType {
        SensorType::Acoustic
    }

    fn sensor_id(&self) -> Uuid {
        self.sensor_id
    }

    async fn connect(&self) -> anyhow::Result<()> {
        tracing::info!("Acoustic sensor connected: {}", self.sensor_id);
        Ok(())
    }

    async fn disconnect(&self) -> anyhow::Result<()> {
        tracing::info!("Acoustic sensor disconnected: {}", self.sensor_id);
        Ok(())
    }

    async fn read_frame(&self) -> anyhow::Result<SensorFrame> {
        Ok(SensorFrame {
            frame_id: Uuid::new_v4(),
            sensor_id: self.sensor_id,
            sensor_type: SensorType::Acoustic,
            timestamp: chrono::Utc::now(),
            position: GeoPosition {
                latitude: 39.9042,
                longitude: 116.4074,
                altitude: 10.0,
            },
            confidence: 0.75,
            payload: SensorPayload::Acoustic(AcousticFeatures {
                frequency_peak_hz: 320.0,
                harmonic_profile: vec![320.0, 640.0, 960.0, 1280.0],
                mfcc: vec![0.1, -0.2, 0.3, -0.1, 0.05, -0.15, 0.2, -0.05, 0.1, -0.1],
            }),
        })
    }

    async fn status(&self) -> anyhow::Result<SensorStatus> {
        Ok(SensorStatus::Online)
    }
}
