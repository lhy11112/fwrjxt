use std::collections::HashMap;
use uuid::Uuid;

use crate::models::*;

/// 反无人机任务规划引擎
///
/// 实现：
/// - 单目标拦截轨迹规划 (RRT*-Connect / Hybrid A*)
/// - 多目标拦截任务分配与协同轨迹规划
/// - 资源调度与冲突消解
/// - 策略动态调整
pub struct MissionPlanner {
    config: PlannerConfig,
}

#[derive(Debug, Clone)]
pub struct PlannerConfig {
    pub max_interceptors: u32,
    pub rrt_max_iterations: u32,
    pub replan_interval_ms: u64,
    pub collision_radius_m: f64,
    pub min_safe_distance_m: f64,
    pub terrain_model_path: Option<String>,
}

impl Default for PlannerConfig {
    fn default() -> Self {
        Self {
            max_interceptors: 20,
            rrt_max_iterations: 5000,
            replan_interval_ms: 500,
            collision_radius_m: 10.0,
            min_safe_distance_m: 50.0,
            terrain_model_path: None,
        }
    }
}

impl MissionPlanner {
    pub fn new(config: PlannerConfig) -> Self {
        Self { config }
    }

    /// 单目标拦截轨迹规划
    ///
    /// 输入：目标当前状态、拦截器位置、环境约束
    /// 输出：拦截航路点序列
    /// 响应时间 ≤ 0.01s
    pub fn plan_single_intercept(
        &self,
        target: &TrackedTarget,
        interceptor: &InterceptorResource,
        no_fly_zones: &[AirspaceZone],
    ) -> anyhow::Result<InterceptMission> {
        let mission_id = Uuid::new_v4();

        // 简化的直线拦截轨迹（实际应用 RRT*-Connect）
        let waypoints = self.generate_intercept_waypoints(target, interceptor)?;

        Ok(InterceptMission {
            mission_id,
            target_id: target.target_id,
            assigned_interceptors: vec![interceptor.resource_id],
            trajectory: waypoints,
            priority: self.calculate_priority(target),
            status: MissionStatus::Planning,
            created_at: chrono::Utc::now(),
            updated_at: chrono::Utc::now(),
        })
    }

    /// 多目标拦截 — 任务分配 + 协同轨迹规划
    ///
    /// 支持 ≥10 架拦截器协同
    /// 响应时间 ≤ 3s
    pub fn plan_multi_intercept(
        &self,
        targets: &[TrackedTarget],
        interceptors: &[InterceptorResource],
        no_fly_zones: &[AirspaceZone],
    ) -> anyhow::Result<Vec<InterceptMission>> {
        // 1. 目标-拦截器分配（匈牙利算法/PSO）
        let assignment = self.assign_targets(targets, interceptors)?;

        // 2. 为每个分配对生成轨迹
        let mut missions = Vec::new();
        for (target_idx, interceptor_idx) in assignment {
            let mission = self.plan_single_intercept(
                &targets[target_idx],
                &interceptors[interceptor_idx],
                no_fly_zones,
            )?;

            // 3. 冲突消解
            let deconflicted = self.deconflict(&mission, &missions)?;
            missions.push(deconflicted);
        }

        Ok(missions)
    }

    /// 目标分配（基于贪心的简化实现，TODO: 替换为匈牙利算法）
    fn assign_targets(
        &self,
        targets: &[TrackedTarget],
        interceptors: &[InterceptorResource],
    ) -> anyhow::Result<Vec<(usize, usize)>> {
        let mut assignment = Vec::new();
        let mut used_interceptors = vec![false; interceptors.len()];

        // 按威胁等级排序目标，优先分配高威胁目标
        let mut sorted_targets: Vec<(usize, &TrackedTarget)> = targets.iter().enumerate().collect();
        // TODO: 按威胁等级排序

        for (target_idx, _target) in sorted_targets {
            // 找最近的可用拦截器
            let best = interceptors.iter().enumerate()
                .filter(|(i, r)| !used_interceptors[*i] && r.status == ResourceStatus::Available)
                .min_by(|(_, a), (_, b)| {
                    let dist_a = self.haversine_distance(
                        &targets[target_idx].position,
                        &a.position,
                    );
                    let dist_b = self.haversine_distance(
                        &targets[target_idx].position,
                        &b.position,
                    );
                    dist_a.partial_cmp(&dist_b).unwrap_or(std::cmp::Ordering::Equal)
                });

            if let Some((interceptor_idx, _)) = best {
                used_interceptors[interceptor_idx] = true;
                assignment.push((target_idx, interceptor_idx));
            }
        }

        Ok(assignment)
    }

    /// 生成拦截航路点（简化的 Dubins 曲线 / 直线段）
    fn generate_intercept_waypoints(
        &self,
        target: &TrackedTarget,
        interceptor: &InterceptorResource,
    ) -> anyhow::Result<Vec<Waypoint>> {
        let mut waypoints = Vec::new();

        // 拦截点预测（简化的纯追踪法）
        let dx = target.position.longitude - interceptor.position.longitude;
        let dy = target.position.latitude - interceptor.position.latitude;
        let dist = (dx * dx + dy * dy).sqrt();

        if dist < 0.001 {
            return Ok(waypoints);
        }

        // 中间航路点
        let mid_lat = interceptor.position.latitude + dy * 0.5;
        let mid_lon = interceptor.position.longitude + dx * 0.5;

        waypoints.push(Waypoint {
            position: GeoPosition {
                latitude: mid_lat,
                longitude: mid_lon,
                altitude: (interceptor.position.altitude + target.position.altitude) / 2.0,
            },
            arrival_time: chrono::Utc::now() + chrono::Duration::seconds(10),
            speed: 15.0,
            action: WaypointAction::FlyTo,
        });

        // 截获点
        waypoints.push(Waypoint {
            position: target.position.clone(),
            arrival_time: chrono::Utc::now() + chrono::Duration::seconds(20),
            speed: 15.0,
            action: WaypointAction::Engage,
        });

        Ok(waypoints)
    }

    /// 冲突消解（速度调节 + 高度分离）
    fn deconflict(
        &self,
        mission: &InterceptMission,
        existing: &[InterceptMission],
    ) -> anyhow::Result<InterceptMission> {
        let mut mission = mission.clone();
        for other in existing {
            for wp in &mut mission.trajectory {
                for other_wp in &other.trajectory {
                    let dist = self.haversine_distance(&wp.position, &other_wp.position);
                    if dist < self.config.min_safe_distance_m {
                        // 高度分离：将冲突航点提升 50m
                        wp.position.altitude += 50.0;
                    }
                }
            }
        }
        Ok(mission)
    }

    /// 计算任务优先级
    fn calculate_priority(&self, target: &TrackedTarget) -> u32 {
        match target.threat_level {
            ThreatLevel::Red => 1,
            ThreatLevel::Yellow => 2,
            ThreatLevel::Green => 3,
        }
    }

    /// Haversine 距离
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

    /// 策略热更新（不停机加载新策略）
    pub fn hot_reload(&mut self, new_config: PlannerConfig) -> anyhow::Result<()> {
        self.config = new_config;
        tracing::info!("Planner config hot-reloaded");
        Ok(())
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_single_intercept() {
        let planner = MissionPlanner::new(PlannerConfig::default());
        let target = TrackedTarget {
            target_id: Uuid::new_v4(),
            track_id: Uuid::new_v4(),
            position: GeoPosition { latitude: 39.9, longitude: 116.4, altitude: 100.0 },
            velocity: Velocity3D { vn: 10.0, ve: 5.0, vd: 0.0 },
            acceleration: None,
            classification: TargetClass::Multicopter,
            threat_level: ThreatLevel::Red,
            confidence: 0.9,
            first_seen: chrono::Utc::now(),
            last_update: chrono::Utc::now(),
            track_history: vec![],
        };
        let interceptor = InterceptorResource {
            resource_id: Uuid::new_v4(),
            resource_type: ResourceType::KineticInterceptor,
            name: "Interceptor-01".into(),
            position: GeoPosition { latitude: 39.8, longitude: 116.3, altitude: 0.0 },
            status: ResourceStatus::Available,
            capabilities: vec!["Kinetic".into()],
            fuel_remaining: 100.0,
            max_range: 5000.0,
        };

        let mission = planner.plan_single_intercept(&target, &interceptor, &[]).unwrap();
        assert_eq!(mission.assigned_interceptors.len(), 1);
        assert!(mission.trajectory.len() >= 1);
    }

    #[test]
    fn test_hot_reload() {
        let mut planner = MissionPlanner::new(PlannerConfig::default());
        let new_config = PlannerConfig {
            max_interceptors: 30,
            ..Default::default()
        };
        assert!(planner.hot_reload(new_config).is_ok());
    }
}
