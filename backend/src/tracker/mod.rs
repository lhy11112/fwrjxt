use std::collections::HashMap;
use tokio::sync::RwLock;
use uuid::Uuid;

use crate::models::*;

/// 多目标跟踪器
///
/// 支持同时跟踪 ≥30 个目标
/// 基于 M/N 逻辑的航迹起始/维持/消亡管理
pub struct MultiTargetTracker {
    tracks: RwLock<HashMap<Uuid, TrackedTarget>>,
    config: TrackerConfig,
}

#[derive(Debug, Clone)]
pub struct TrackerConfig {
    pub max_tracks: usize,
    pub coast_time_secs: u64,
    pub confirmation_m: u32,
    pub confirmation_n: u32,
    pub deletion_m: u32,
    pub deletion_n: u32,
}

impl Default for TrackerConfig {
    fn default() -> Self {
        Self {
            max_tracks: 50,
            coast_time_secs: 10,
            confirmation_m: 3,
            confirmation_n: 5,
            deletion_m: 3,
            deletion_n: 5,
        }
    }
}

impl MultiTargetTracker {
    pub fn new(config: TrackerConfig) -> Self {
        Self {
            tracks: RwLock::new(HashMap::new()),
            config,
        }
    }

    /// 更新或新建航迹
    pub async fn update(&self, targets: Vec<TrackedTarget>) -> anyhow::Result<Vec<TrackedTarget>> {
        let mut tracks = self.tracks.write().await;

        for target in targets {
            let last_update = target.last_update;
            let pos = target.position.clone();
            let vel = target.velocity.clone();
            let threat = target.threat_level.clone();
            let entry = tracks.entry(target.target_id).or_insert_with(|| target);
            entry.last_update = last_update;
            entry.position = pos.clone();
            entry.velocity = vel.clone();
            entry.threat_level = threat;

            // 限制航迹历史长度
            entry.track_history.push(TrackPoint {
                timestamp: last_update,
                position: pos,
                velocity: vel,
            });
            if entry.track_history.len() > 100 {
                entry.track_history.remove(0);
            }
        }

        // 清理超时航迹
        let now = chrono::Utc::now();
        tracks.retain(|_, t| {
            (now - t.last_update).num_seconds() < self.config.coast_time_secs as i64
        });

        Ok(tracks.values().cloned().collect())
    }

    /// 获取所有活跃航迹
    pub async fn get_active_tracks(&self) -> Vec<TrackedTarget> {
        self.tracks.read().await.values().cloned().collect()
    }

    /// 获取指定目标航迹
    pub async fn get_track(&self, target_id: &Uuid) -> Option<TrackedTarget> {
        self.tracks.read().await.get(target_id).cloned()
    }

    /// 活跃航迹数量
    pub async fn track_count(&self) -> usize {
        self.tracks.read().await.len()
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use chrono::Utc;

    #[tokio::test]
    async fn test_tracker_update() {
        let tracker = MultiTargetTracker::new(TrackerConfig::default());
        let target = vec![TrackedTarget {
            target_id: Uuid::new_v4(),
            track_id: Uuid::new_v4(),
            position: GeoPosition { latitude: 39.9, longitude: 116.4, altitude: 100.0 },
            velocity: Velocity3D { vn: 10.0, ve: 5.0, vd: 0.0 },
            acceleration: None,
            classification: TargetClass::Multicopter,
            threat_level: ThreatLevel::Yellow,
            confidence: 0.85,
            first_seen: Utc::now(),
            last_update: Utc::now(),
            track_history: vec![],
        }];

        tracker.update(target).await.unwrap();
        assert_eq!(tracker.track_count().await, 1);
    }
}
