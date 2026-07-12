use async_trait::async_trait;
use uuid::Uuid;

use super::SensorAdapter;
use crate::models::*;

/// 光电/红外传感器适配器
pub struct EoIrAdapter {
    sensor_id: Uuid,
    config: EoIrConfig,
}

#[derive(Debug, Clone)]
pub struct EoIrConfig {
    pub connection_string: String,
    pub resolution: (u32, u32),
    pub fps: u32,
}

impl Default for EoIrConfig {
    fn default() -> Self {
        Self {
            connection_string: "tcp://eoir-sim:9002".into(),
            resolution: (1920, 1080),
            fps: 30,
        }
    }
}

impl EoIrAdapter {
    pub fn new(sensor_id: Uuid, config: EoIrConfig) -> Self {
        Self { sensor_id, config }
    }
}

#[async_trait]
impl SensorAdapter for EoIrAdapter {
    fn sensor_type(&self) -> SensorType {
        SensorType::EoIr
    }

    fn sensor_id(&self) -> Uuid {
        self.sensor_id
    }

    async fn connect(&self) -> anyhow::Result<()> {
        tracing::info!("EO/IR Sensor connected: {}", self.sensor_id);
        Ok(())
    }

    async fn disconnect(&self) -> anyhow::Result<()> {
        tracing::info!("EO/IR Sensor disconnected: {}", self.sensor_id);
        Ok(())
    }

    async fn read_frame(&self) -> anyhow::Result<SensorFrame> {
        Ok(SensorFrame {
            frame_id: Uuid::new_v4(),
            sensor_id: self.sensor_id,
            sensor_type: SensorType::EoIr,
            timestamp: chrono::Utc::now(),
            position: GeoPosition {
                latitude: 39.9042,
                longitude: 116.4074,
                altitude: 30.0,
            },
            confidence: 0.92,
            payload: SensorPayload::Image(ImageFrame {
                width: 1920,
                height: 1080,
                format: "jpeg".into(),
                data_uri: "data:image/jpeg;base64,/9j/...simulated...".into(),
                detections: vec![
                    BoundingBox {
                        x: 0.3, y: 0.4, w: 0.1, h: 0.1,
                        class: "UAV".into(),
                        confidence: 0.89,
                    },
                ],
            }),
        })
    }

    async fn status(&self) -> anyhow::Result<SensorStatus> {
        Ok(SensorStatus::Online)
    }
}
