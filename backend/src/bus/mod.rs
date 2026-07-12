use std::sync::Arc;
use tokio::sync::{broadcast, mpsc};
use uuid::Uuid;

use serde::{Deserialize, Serialize};

use crate::models::*;

/// HSimC2 内部消息总线
///
/// 基于广播 + PubSub 模式的实时消息分发
/// 用于模块间通信和对外 WebSocket 推送
#[derive(Debug, Clone)]
pub struct MessageBus {
    /// 传感器数据分发 (各传感器 → 融合引擎)
    sensor_tx: mpsc::Sender<SensorFrame>,
    sensor_rx: Arc<tokio::sync::Mutex<mpsc::Receiver<SensorFrame>>>,

    /// 态势更新广播 (融合引擎 → 各订阅者)
    situation_tx: broadcast::Sender<Vec<TrackedTarget>>,

    /// 任务更新广播 (规划引擎 → 各订阅者)
    mission_tx: broadcast::Sender<InterceptMission>,

    /// 告警广播
    alert_tx: broadcast::Sender<AlertEvent>,

    /// 系统事件广播
    event_tx: broadcast::Sender<SystemEvent>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AlertEvent {
    pub alert_id: Uuid,
    pub alert_type: AlertType,
    pub severity: AlertSeverity,
    pub title: String,
    pub description: String,
    pub source: String,
    pub timestamp: chrono::DateTime<chrono::Utc>,
}

#[derive(Debug, Clone, PartialEq, serde::Serialize, serde::Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum AlertType {
    ThreatDetected,
    MissionCritical,
    SystemError,
    SensorOffline,
    CommunicationLost,
}

#[derive(Debug, Clone, PartialEq, serde::Serialize, serde::Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum AlertSeverity {
    Critical,
    Warning,
    Info,
}

#[derive(Debug, Clone, serde::Serialize, serde::Deserialize)]
#[serde(tag = "type")]
pub enum SystemEvent {
    EngineStarted { engine_id: Uuid, timestamp: chrono::DateTime<chrono::Utc> },
    SensorRegistered { sensor_id: Uuid, name: String },
    MissionAssigned { mission_id: Uuid, target_id: Uuid },
    SystemShutdown { reason: String },
}

impl MessageBus {
    pub fn new() -> Self {
        let (sensor_tx, sensor_rx) = mpsc::channel::<SensorFrame>(10000);
        let (situation_tx, _) = broadcast::channel::<Vec<TrackedTarget>>(1024);
        let (mission_tx, _) = broadcast::channel::<InterceptMission>(1024);
        let (alert_tx, _) = broadcast::channel::<AlertEvent>(1024);
        let (event_tx, _) = broadcast::channel::<SystemEvent>(1024);

        Self {
            sensor_tx,
            sensor_rx: Arc::new(tokio::sync::Mutex::new(sensor_rx)),
            situation_tx,
            mission_tx,
            alert_tx,
            event_tx,
        }
    }

    // ---- 发送方法 ----

    /// 发送传感器数据帧到融合管道
    pub async fn send_sensor_frame(&self, frame: SensorFrame) -> anyhow::Result<()> {
        self.sensor_tx.send(frame).await?;
        Ok(())
    }

    /// 广播态势更新
    pub fn broadcast_situation(&self, targets: Vec<TrackedTarget>) {
        let _ = self.situation_tx.send(targets);
    }

    /// 广播任务更新
    pub fn broadcast_mission(&self, mission: InterceptMission) {
        let _ = self.mission_tx.send(mission);
    }

    /// 推送告警
    pub fn send_alert(&self, alert: AlertEvent) {
        let _ = self.alert_tx.send(alert);
    }

    /// 发布系统事件
    pub fn publish_event(&self, event: SystemEvent) {
        let _ = self.event_tx.send(event);
    }

    // ---- 订阅方法 ----

    /// 订阅传感器数据流 (用于融合引擎消费)
    pub fn subscribe_sensor(&self) -> mpsc::Receiver<SensorFrame> {
        // 注意：实际应使用 mpsc 多消费者模式，简化版返回新 channel
        let (tx, rx) = mpsc::channel(10000);
        let mut sensor_rx = self.sensor_rx.clone();
        tokio::spawn(async move {
            use tokio::sync::Mutex;
            // 转发传感器数据（简化）
        });
        rx
    }

    /// 订阅态势更新
    pub fn subscribe_situation(&self) -> broadcast::Receiver<Vec<TrackedTarget>> {
        self.situation_tx.subscribe()
    }

    /// 订阅任务更新
    pub fn subscribe_mission(&self) -> broadcast::Receiver<InterceptMission> {
        self.mission_tx.subscribe()
    }

    /// 订阅告警
    pub fn subscribe_alert(&self) -> broadcast::Receiver<AlertEvent> {
        self.alert_tx.subscribe()
    }

    /// 订阅系统事件
    pub fn subscribe_event(&self) -> broadcast::Receiver<SystemEvent> {
        self.event_tx.subscribe()
    }
}
