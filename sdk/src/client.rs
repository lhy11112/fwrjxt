use crate::types::{SdkConfig, SensorFrame, SensorType};
use reqwest::Client as HttpClient;
use uuid::Uuid;

/// HSimC2 传感器 SDK 客户端
pub struct HSimC2Client {
    config: SdkConfig,
    http_client: HttpClient,
    sensor_id: Uuid,
}

impl HSimC2Client {
    /// 创建新的 SDK 客户端
    pub fn new(config: SdkConfig, sensor_id: Uuid) -> Self {
        let http_client = HttpClient::builder()
            .timeout(std::time::Duration::from_secs(30))
            .build()
            .unwrap_or_default();

        Self {
            config,
            http_client,
            sensor_id,
        }
    }

    /// 发送传感器数据帧到 HSimC2 服务器
    pub async fn send_frame(&self, frame: SensorFrame) -> anyhow::Result<()> {
        let url = format!("{}/api/v1/sensors/{}/data", self.config.server_url, self.sensor_id);
        let resp = self.http_client
            .post(&url)
            .json(&frame)
            .send()
            .await?;

        if !resp.status().is_success() {
            anyhow::bail!("Failed to send frame: {}", resp.status());
        }
        Ok(())
    }

    /// 批量发送传感器数据帧
    pub async fn send_frames_batch(&self, frames: Vec<SensorFrame>) -> anyhow::Result<()> {
        for chunk in frames.chunks(self.config.batch_size) {
            let url = format!("{}/api/v1/sensors/{}/data/batch", self.config.server_url, self.sensor_id);
            let resp = self.http_client
                .post(&url)
                .json(&chunk)
                .send()
                .await?;

            if !resp.status().is_success() {
                anyhow::bail!("Batch send failed at chunk: {}", resp.status());
            }
        }
        Ok(())
    }

    /// 获取服务器健康状态
    pub async fn health_check(&self) -> anyhow::Result<bool> {
        let url = format!("{}/api/v1/health", self.config.server_url);
        let resp = self.http_client.get(&url).send().await?;
        Ok(resp.status().is_success())
    }

    /// 获取传感器 ID
    pub fn sensor_id(&self) -> Uuid {
        self.sensor_id
    }

    /// 创建雷达数据帧
    pub fn create_radar_frame(
        &self,
        latitude: f64,
        longitude: f64,
        altitude: f64,
        range: f64,
        azimuth: f64,
        elevation: f64,
        rcs: f64,
    ) -> SensorFrame {
        let payload = serde_json::json!({
            "range": range,
            "azimuth": azimuth,
            "elevation": elevation,
            "rcs": rcs,
        });

        SensorFrame {
            frame_id: Uuid::new_v4(),
            sensor_id: self.sensor_id,
            sensor_type: SensorType::Radar,
            timestamp: chrono::Utc::now().to_rfc3339(),
            latitude,
            longitude,
            altitude,
            confidence: 0.95,
            payload_json: payload.to_string(),
        }
    }
}
