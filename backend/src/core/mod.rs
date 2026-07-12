pub mod pipeline;

use chrono::{DateTime, Utc};
use uuid::Uuid;

use crate::models::*;

/// HSimC2 核心引擎：管理传感器数据流、融合、跟踪、威胁评估和任务规划的协调
#[derive(Debug)]
pub struct CoreEngine {
    pub engine_id: Uuid,
    pub started_at: DateTime<Utc>,
    running: bool,
    metrics: EngineMetrics,
}

#[derive(Debug, Clone)]
pub struct EngineMetrics {
    pub total_frames_processed: u64,
    pub total_targets_tracked: u64,
    pub total_missions_planned: u64,
    pub avg_fusion_latency_ms: f64,
    pub avg_tracking_latency_ms: f64,
    pub avg_planning_latency_ms: f64,
}

impl Default for EngineMetrics {
    fn default() -> Self {
        Self {
            total_frames_processed: 0,
            total_targets_tracked: 0,
            total_missions_planned: 0,
            avg_fusion_latency_ms: 0.0,
            avg_tracking_latency_ms: 0.0,
            avg_planning_latency_ms: 0.0,
        }
    }
}

impl CoreEngine {
    pub fn new() -> Self {
        Self {
            engine_id: Uuid::new_v4(),
            started_at: Utc::now(),
            running: false,
            metrics: EngineMetrics::default(),
        }
    }

    pub fn start(&mut self) -> anyhow::Result<()> {
        self.running = true;
        tracing::info!("HSimC2 Core Engine started: {}", self.engine_id);
        Ok(())
    }

    pub fn stop(&mut self) -> anyhow::Result<()> {
        self.running = false;
        tracing::info!("HSimC2 Core Engine stopped");
        Ok(())
    }

    pub fn is_running(&self) -> bool {
        self.running
    }

    pub fn metrics(&self) -> &EngineMetrics {
        &self.metrics
    }

    pub fn record_frame_processed(&mut self, latency_ms: f64) {
        self.metrics.total_frames_processed += 1;
        // Exponential moving average
        self.metrics.avg_fusion_latency_ms = self.metrics.avg_fusion_latency_ms * 0.95 + latency_ms * 0.05;
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_engine_start_stop() {
        let mut engine = CoreEngine::new();
        assert!(!engine.is_running());
        engine.start().unwrap();
        assert!(engine.is_running());
        engine.stop().unwrap();
        assert!(!engine.is_running());
    }
}
