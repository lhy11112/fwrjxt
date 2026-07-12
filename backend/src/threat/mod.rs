use uuid::Uuid;

use crate::models::*;

/// 威胁态势估计模块
///
/// 实现 OODA 环的 Orient + Decide 阶段：
/// 1. 意图识别（侦察/攻击/诱饵等）
/// 2. 威胁等级评估（红/黄/绿）
/// 3. 动态排序
pub struct ThreatAssessor {
    config: ThreatConfig,
}

#[derive(Debug, Clone)]
pub struct ThreatConfig {
    pub red_threshold: f64,
    pub yellow_threshold: f64,
    pub weights: ThreatWeights,
}

#[derive(Debug, Clone)]
pub struct ThreatWeights {
    pub intent: f64,
    pub distance: f64,
    pub speed: f64,
    pub altitude: f64,
    pub model_threat: f64,
    pub maneuver: f64,
}

impl Default for ThreatWeights {
    fn default() -> Self {
        Self {
            intent: 0.30,
            distance: 0.25,
            speed: 0.15,
            altitude: 0.10,
            model_threat: 0.10,
            maneuver: 0.10,
        }
    }
}

impl Default for ThreatConfig {
    fn default() -> Self {
        Self {
            red_threshold: 0.75,
            yellow_threshold: 0.4,
            weights: ThreatWeights::default(),
        }
    }
}

impl ThreatAssessor {
    pub fn new(config: ThreatConfig) -> Self {
        Self { config }
    }

    /// 评估目标威胁等级
    pub fn assess(&self, target: &TrackedTarget, assets: &[GeoPosition]) -> ThreatAssessment {
        let intent = self.estimate_intent(target);
        let score = self.calculate_threat_score(target, &intent, assets);
        let level = self.classify_threat_level(score);

        ThreatAssessment {
            target_id: target.target_id,
            intent,
            score,
            level,
            assessed_at: chrono::Utc::now(),
        }
    }

    /// 意图识别（基于运动特征 + 通信模式）
    fn estimate_intent(&self, target: &TrackedTarget) -> BehaviorType {
        let speed = (target.velocity.vn.powi(2)
            + target.velocity.ve.powi(2)
            + target.velocity.vd.powi(2))
            .sqrt();

        let altitude = target.position.altitude;

        // 基于运动特征的启发式规则
        // 快速下降 + 低速 → 攻击意图
        if target.velocity.vd < -5.0 && speed < 15.0 {
            return BehaviorType::Attack;
        }

        // 持续盘旋 → 侦察或监视
        if speed < 5.0 && altitude > 50.0 {
            // 简单检测长期盘旋：检查轨迹是否在小范围内
            if let Some((min_pos, max_pos)) = self.bounds_of_history(target) {
                let span = (
                    (max_pos.latitude - min_pos.latitude).abs(),
                    (max_pos.longitude - min_pos.longitude).abs(),
                );
                if span.0 < 0.001 && span.1 < 0.001 {
                    return BehaviorType::Surveillance;
                }
            }
            return BehaviorType::Reconnaissance;
        }

        // 高机动 + 不规则路径 → 诱饵
        if let Some(acc) = &target.acceleration {
            let acc_mag = (acc.an.powi(2) + acc.ae.powi(2) + acc.ad.powi(2)).sqrt();
            if acc_mag > 10.0 {
                return BehaviorType::Decoy;
            }
        }

        // 低速巡航
        if speed < 10.0 {
            return BehaviorType::Transport;
        }

        BehaviorType::Unknown
    }

    /// 计算综合威胁评分
    fn calculate_threat_score(
        &self,
        target: &TrackedTarget,
        intent: &BehaviorType,
        assets: &[GeoPosition],
    ) -> f64 {
        let w = &self.config.weights;

        // 意图系数
        let intent_coeff = match intent {
            BehaviorType::Attack => 1.0,
            BehaviorType::Decoy => 0.7,
            BehaviorType::Reconnaissance => 0.5,
            BehaviorType::Surveillance => 0.6,
            BehaviorType::Loitering => 0.3,
            BehaviorType::Transport => 0.2,
            BehaviorType::Unknown => 0.4,
        };

        // 距离系数（离最近保护资产的倒数）
        let min_dist = assets.iter()
            .map(|a| self.haversine_distance(&target.position, a))
            .fold(f64::MAX, f64::min);
        let distance_coeff = 1.0 - (min_dist / 10000.0).clamp(0.0, 1.0);

        // 速度系数
        let speed = (target.velocity.vn.powi(2)
            + target.velocity.ve.powi(2)
            + target.velocity.vd.powi(2))
            .sqrt();
        let speed_coeff = (speed / 50.0).clamp(0.0, 1.0);

        // 高度系数（低空更危险）
        let altitude = target.position.altitude;
        let altitude_coeff = 1.0 - (altitude / 500.0).clamp(0.0, 1.0);

        // 机型威胁基线
        let model_threat_coeff = match target.classification {
            TargetClass::FixedWing => 0.7,   // 固定翼一般航程远，威胁大
            TargetClass::Multicopter => 0.5,
            TargetClass::Dji => 0.4,
            TargetClass::Autel => 0.4,
            TargetClass::Bird => 0.0,
            TargetClass::Helicopter => 0.6,
            TargetClass::Unknown => 0.3,
            TargetClass::Other(_) => 0.3,
        };

        // 机动系数
        let maneuver_coeff = if let Some(acc) = &target.acceleration {
            let acc_mag = (acc.an.powi(2) + acc.ae.powi(2) + acc.ad.powi(2)).sqrt();
            (acc_mag / 20.0).clamp(0.0, 1.0)
        } else {
            0.0
        };

        // 综合评分
        w.intent * intent_coeff
            + w.distance * distance_coeff
            + w.speed * speed_coeff
            + w.altitude * altitude_coeff
            + w.model_threat * model_threat_coeff
            + w.maneuver * maneuver_coeff
    }

    /// 威胁等级分类
    fn classify_threat_level(&self, score: f64) -> ThreatLevel {
        if score >= self.config.red_threshold {
            ThreatLevel::Red
        } else if score >= self.config.yellow_threshold {
            ThreatLevel::Yellow
        } else {
            ThreatLevel::Green
        }
    }

    /// 对目标列表进行威胁排序
    pub fn sort_by_threat(&self, targets: &[TrackedTarget], assets: &[GeoPosition]) -> Vec<ThreatAssessment> {
        let mut assessments: Vec<_> = targets
            .iter()
            .map(|t| self.assess(t, assets))
            .collect();

        assessments.sort_by(|a, b| b.score.partial_cmp(&a.score).unwrap_or(std::cmp::Ordering::Equal));
        assessments
    }

    /// Haversine 距离计算（米）
    fn haversine_distance(&self, p1: &GeoPosition, p2: &GeoPosition) -> f64 {
        let r = 6371000.0;
        let d_lat = (p2.latitude - p1.latitude).to_radians();
        let d_lon = (p2.longitude - p1.longitude).to_radians();
        let a = (d_lat / 2.0).sin().powi(2)
            + p1.latitude.to_radians().cos()
            * p2.latitude.to_radians().cos()
            * (d_lon / 2.0).sin().powi(2);
        r * 2.0 * a.sqrt().asin()
    }

    /// 获取航迹历史边界
    fn bounds_of_history(&self, target: &TrackedTarget) -> Option<(GeoPosition, GeoPosition)> {
        if target.track_history.is_empty() {
            return None;
        }
        let mut min_lat = f64::MAX;
        let mut max_lat = f64::MIN;
        let mut min_lon = f64::MAX;
        let mut max_lon = f64::MIN;

        for pt in &target.track_history {
            min_lat = min_lat.min(pt.position.latitude);
            max_lat = max_lat.max(pt.position.latitude);
            min_lon = min_lon.min(pt.position.longitude);
            max_lon = max_lon.max(pt.position.longitude);
        }

        Some((
            GeoPosition { latitude: min_lat, longitude: min_lon, altitude: 0.0 },
            GeoPosition { latitude: max_lat, longitude: max_lon, altitude: 0.0 },
        ))
    }
}

/// 威胁评估结果
#[derive(Debug, Clone)]
pub struct ThreatAssessment {
    pub target_id: Uuid,
    pub intent: BehaviorType,
    pub score: f64,
    pub level: ThreatLevel,
    pub assessed_at: chrono::DateTime<chrono::Utc>,
}

#[cfg(test)]
mod tests {
    use super::*;
    use chrono::Utc;

    #[test]
    fn test_threat_assessment() {
        let assessor = ThreatAssessor::new(ThreatConfig::default());

        let target = TrackedTarget {
            target_id: Uuid::new_v4(),
            track_id: Uuid::new_v4(),
            position: GeoPosition { latitude: 39.9, longitude: 116.4, altitude: 50.0 },
            velocity: Velocity3D { vn: -5.0, ve: 0.0, vd: -8.0 },
            acceleration: None,
            classification: TargetClass::Multicopter,
            threat_level: ThreatLevel::Green,
            confidence: 0.85,
            first_seen: Utc::now(),
            last_update: Utc::now(),
            track_history: vec![
                TrackPoint {
                    timestamp: Utc::now(),
                    position: GeoPosition { latitude: 39.901, longitude: 116.401, altitude: 100.0 },
                    velocity: Velocity3D { vn: 0.0, ve: 0.0, vd: 0.0 },
                },
            ],
        };

        let assets = vec![GeoPosition { latitude: 39.905, longitude: 116.410, altitude: 0.0 }];
        let assessment = assessor.assess(&target, &assets);

        assert_eq!(assessment.intent, BehaviorType::Attack);
        assert!(assessment.score > 0.0);
    }
}
