use axum::{
    extract::{
        ws::{Message, WebSocket},
        State, WebSocketUpgrade,
    },
    response::IntoResponse,
};
use futures_util::{SinkExt, StreamExt};
use serde::Serialize;
use std::sync::Arc;

use super::AppState;
use crate::bus::{AlertEvent, SystemEvent};
use crate::models::*;

/// WebSocket 升级处理
pub async fn websocket_handler(
    ws: WebSocketUpgrade,
    State(state): State<AppState>,
) -> impl IntoResponse {
    ws.on_upgrade(move |socket| handle_websocket(socket, state))
}

/// WebSocket 连接处理
async fn handle_websocket(socket: WebSocket, state: AppState) {
    let (mut sender, mut receiver) = socket.split();

    // 订阅各数据通道
    let mut situation_rx = state.bus.subscribe_situation();
    let mut mission_rx = state.bus.subscribe_mission();
    let mut alert_rx = state.bus.subscribe_alert();
    let mut event_rx = state.bus.subscribe_event();

    // 向客户端发送初始态势快照
    let targets = state.tracker.get_active_tracks().await;
    let snapshot = WsEvent::TargetUpdated(targets);
    if let Ok(msg) = serde_json::to_string(&snapshot) {
        let _ = sender.send(Message::Text(msg.into())).await;
    }

    // 任务：将接收的消息转换为事件并回复
    let send_task = async move {
        loop {
            tokio::select! {
                // 态势更新
                result = situation_rx.recv() => {
                    if let Ok(targets) = result {
                        let event = WsEvent::TargetUpdated(targets);
                        if let Ok(msg) = serde_json::to_string(&event) {
                            if sender.send(Message::Text(msg.into())).await.is_err() {
                                break;
                            }
                        }
                    }
                }
                // 任务更新
                result = mission_rx.recv() => {
                    if let Ok(mission) = result {
                        let event = WsEvent::MissionUpdate(mission);
                        if let Ok(msg) = serde_json::to_string(&event) {
                            if sender.send(Message::Text(msg.into())).await.is_err() {
                                break;
                            }
                        }
                    }
                }
                // 告警
                result = alert_rx.recv() => {
                    if let Ok(alert) = result {
                        // 告警转换为威胁告警事件
                        if let Ok(msg) = serde_json::to_string(&alert) {
                            if sender.send(Message::Text(format!(r#"{{"event":"Alert","payload":{}}}"#, msg)).into()).await.is_err() {
                                break;
                            }
                        }
                    }
                }
                // 系统事件
                result = event_rx.recv() => {
                    if let Ok(event) = result {
                        if let Ok(msg) = serde_json::to_string(&event) {
                            if sender.send(Message::Text(format!(r#"{{"event":"SystemEvent","payload":{}}}"#, msg)).into()).await.is_err() {
                                break;
                            }
                        }
                    }
                }
                // 健康状态心跳 (每 5 秒)
                _ = tokio::time::sleep(tokio::time::Duration::from_secs(5)) => {
                    let health = WsEvent::SystemHealth(SystemHealth {
                        cpu_usage: 0.5,
                        memory_usage: 0.4,
                        active_targets: state.tracker.track_count().await as u32,
                        active_missions: 0,
                        sensor_count: state.sensors.online_count().await as u32,
                        uptime_seconds: 0,
                    });
                    if let Ok(msg) = serde_json::to_string(&health) {
                        if sender.send(Message::Text(msg.into())).await.is_err() {
                            break;
                        }
                    }
                }
            }
        }
    };

    // 任务：处理客户端发来的消息
    let recv_task = async move {
        while let Some(Ok(msg)) = receiver.next().await {
            match msg {
                Message::Text(text) => {
                    // 处理客户端命令（如修改态势、下达指令等）
                    tracing::debug!("WS received: {}", text);
                    // TODO: 命令解析与分发
                }
                Message::Close(_) => break,
                _ => {}
            }
        }
    };

    // 并行运行收发任务
    tokio::select! {
        _ = send_task => {},
        _ = recv_task => {},
    }
}
