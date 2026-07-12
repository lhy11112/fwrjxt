pub mod ekf;

use std::collections::HashMap;
use tokio::sync::RwLock;
use uuid::Uuid;

use crate::models::*;

/// JDL 融合模型实现：
/// - L0: 信号级融合
/// - L1: 特征级融合 (分布式 EKF/UKF + IMM)
/// - L2: 态势级融合
/// - L3: 威胁级融合 (委托给 threat 模块)
pub struct FusionEngine {
    level_0: SignalFusion,
    level_1: FeatureFusion,
    level_2: SituationFusion,
}

/// 信号级融合 (L0) — 多传感器点迹关联
pub struct SignalFusion;

/// 特征级融合 (L1)
pub struct FeatureFusion;

/// 态势级融合 (L2)
pub struct SituationFusion;

impl FusionEngine {
    pub fn new() -> Self {
        Self {
            level_0: SignalFusion,
            level_1: FeatureFusion,
            level_2: SituationFusion,
        }
    }

    /// 融合多传感器帧数据 → 统一目标
    pub async fn fuse(
        &self,
        frames: Vec<SensorFrame>,
        existing_tracks: &[TrackedTarget],
    ) -> anyhow::Result<Vec<TrackedTarget>> {
        // L0: 信号级关联 — 同一传感器的多帧关联
        let associated = self.level_0.associate(frames)?;

        // L1: 特征级融合 — 异类传感器特征合并
        let fused = self.level_1.fuse(associated, existing_tracks)?;

        // L2: 态势级融合 — 多目标关系建模
        let situation = self.level_2.build_situation(fused)?;

        Ok(situation)
    }

    /// 获取引擎统计
    pub fn statistics(&self) -> FusionStats {
        FusionStats {
            total_fusions: 0,
            avg_latency_ms: 0.0,
        }
    }
}

#[derive(Debug, Clone)]
pub struct FusionStats {
    pub total_fusions: u64,
    pub avg_latency_ms: f64,
}

impl SignalFusion {
    /// 同类型传感器信号级关联（如多雷达点迹融合）
    pub fn associate(&self, frames: Vec<SensorFrame>) -> anyhow::Result<Vec<SensorFrame>> {
        // TODO: GNN/JPDA 点迹关联
        Ok(frames)
    }
}

impl FeatureFusion {
    /// 异类多传感器特征融合（雷达RF+EOIR+声学）
    pub fn fuse(
        &self,
        frames: Vec<SensorFrame>,
        _existing_tracks: &[TrackedTarget],
    ) -> anyhow::Result<Vec<TrackedTarget>> {
        let mut targets = Vec::new();

        // 将传感器帧转换为统一目标
        for frame in frames {
            let target = TrackedTarget {
                target_id: Uuid::new_v4(),
                track_id: Uuid::new_v4(),
                position: frame.position.clone(),
                velocity: Velocity3D {
                    vn: 0.0,
                    ve: 0.0,
                    vd: 0.0,
                },
                acceleration: None,
                classification: TargetClass::Unknown,
                threat_level: ThreatLevel::Green,
                confidence: frame.confidence,
                first_seen: frame.timestamp,
                last_update: frame.timestamp,
                track_history: vec![TrackPoint {
                    timestamp: frame.timestamp,
                    position: frame.position.clone(),
                    velocity: Velocity3D { vn: 0.0, ve: 0.0, vd: 0.0 },
                }],
            };
            targets.push(target);
        }

        Ok(targets)
    }
}

impl SituationFusion {
    /// 态势级融合：多目标关系建模、态势图生成
    pub fn build_situation(&self, targets: Vec<TrackedTarget>) -> anyhow::Result<Vec<TrackedTarget>> {
        // 排序并去重
        let mut targets = targets;
        targets.sort_by(|a, b| a.last_update.cmp(&b.last_update));
        Ok(targets)
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use chrono::Utc;

    #[tokio::test]
    async fn test_fusion_engine() {
        let engine = FusionEngine::new();
        let frames = vec![SensorFrame {
            frame_id: Uuid::new_v4(),
            sensor_id: Uuid::new_v4(),
            sensor_type: SensorType::Radar,
            timestamp: Utc::now(),
            position: GeoPosition { latitude: 39.9, longitude: 116.4, altitude: 100.0 },
            confidence: 0.9,
            payload: SensorPayload::Radar(RadarDetection {
                range: 1000.0, azimuth: 45.0, elevation: 30.0,
                rcs: 0.1, radial_velocity: -15.0, snr: 20.0,
            }),
        }];

        let targets = engine.fuse(frames, &[]).await.unwrap();
        assert_eq!(targets.len(), 1);
    }
}
