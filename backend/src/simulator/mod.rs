use std::sync::Arc;
use tokio::sync::RwLock;
use uuid::Uuid;
use rand::Rng;

use crate::bus::MessageBus;
use crate::models::*;

/// 传感器仿真引擎
///
/// 自动产生多类型传感器模拟数据，用于：
/// - 前端可视化展示
/// - 融合/跟踪/威胁评估算法验证
/// - 系统集成测试
pub struct SensorSimulator {
    config: SimulatorConfig,
    bus: Arc<MessageBus>,
    running: Arc<RwLock<bool>>,
    targets: Arc<RwLock<Vec<SimulatedTarget>>>,
    sensors: Vec<SimulatedSensor>,
    frame_count: Arc<RwLock<u64>>,
}

/// 仿真配置
#[derive(Debug, Clone)]
pub struct SimulatorConfig {
    pub update_interval_ms: u64,
    pub num_targets: usize,
    pub spawn_interval_secs: u64,
    pub radar_range_km: f64,
    pub center_lat: f64,
    pub center_lon: f64,
    pub area_radius_km: f64,
}

impl Default for SimulatorConfig {
    fn default() -> Self {
        Self {
            update_interval_ms: 200,        // 5次/秒更新
            num_targets: 8,                 // 初始目标数
            spawn_interval_secs: 15,        // 每15秒产生新目标
            radar_range_km: 8.0,
            center_lat: 34.08,             // 西安秦岭北侧
            center_lon: 108.94,
            area_radius_km: 6.0,            // 覆盖 12km 直径的攻防演练区域
        }
    }
}

/// 模拟传感器
#[derive(Debug, Clone)]
pub struct SimulatedSensor {
    pub sensor_id: Uuid,
    pub sensor_type: SensorType,
    pub name: String,
    pub position: GeoPosition,
}

/// 模拟目标（带运动模型）
#[derive(Debug, Clone)]
pub struct SimulatedTarget {
    pub target_id: Uuid,
    pub classification: TargetClass,
    pub position: GeoPosition,
    pub velocity: Velocity3D,
    pub heading: f64,
    pub speed: f64,
    pub altitude: f64,
    pub behavior: BehaviorType,
    pub threat_level: ThreatLevel,
    pub rcs: f64,
    pub rf_freq: f64,
    pub is_active: bool,
    pub spawn_time: chrono::DateTime<chrono::Utc>,
    pub waypoints: Vec<(f64, f64)>, // 路径点 (lat, lon)
    pub current_wp: usize,
}

impl SensorSimulator {
    pub fn new(config: SimulatorConfig, bus: Arc<MessageBus>) -> Self {
        // 创建模拟传感器
        let sensors = vec![
            SimulatedSensor {
                sensor_id: Uuid::parse_str("00000000-0000-0000-0000-000000000001").unwrap(),
                sensor_type: SensorType::Radar,
                name: "仿真-相控阵雷达".into(),
                position: GeoPosition { latitude: 34.08, longitude: 108.94, altitude: 50.0 },
            },
            SimulatedSensor {
                sensor_id: Uuid::parse_str("00000000-0000-0000-0000-000000000002").unwrap(),
                sensor_type: SensorType::Rf,
                name: "仿真-SDR频谱监测".into(),
                position: GeoPosition { latitude: 34.082, longitude: 108.942, altitude: 30.0 },
            },
            SimulatedSensor {
                sensor_id: Uuid::parse_str("00000000-0000-0000-0000-000000000003").unwrap(),
                sensor_type: SensorType::EoIr,
                name: "仿真-光电追踪".into(),
                position: GeoPosition { latitude: 34.078, longitude: 108.938, altitude: 20.0 },
            },
            SimulatedSensor {
                sensor_id: Uuid::parse_str("00000000-0000-0000-0000-000000000004").unwrap(),
                sensor_type: SensorType::Acoustic,
                name: "仿真-声学阵列".into(),
                position: GeoPosition { latitude: 34.085, longitude: 108.945, altitude: 10.0 },
            },
        ];

        Self {
            config,
            bus,
            running: Arc::new(RwLock::new(false)),
            targets: Arc::new(RwLock::new(Vec::new())),
            sensors,
            frame_count: Arc::new(RwLock::new(0)),
        }
    }

    /// 启动仿真引擎（后台任务）
    pub async fn start(&self) {
        let mut running = self.running.write().await;
        *running = true;
        drop(running);

        let running = self.running.clone();
        let targets = self.targets.clone();
        let sensors = self.sensors.clone();
        let bus = self.bus.clone();
        let frame_count = self.frame_count.clone();
        let config = self.config.clone();

        tracing::info!("🧪 传感器仿真引擎启动: {}个初始目标", config.num_targets);

        // 1. 初始产生目标
        {
            let mut tgt = targets.write().await;
            for _ in 0..config.num_targets {
                tgt.push(Self::spawn_random_target(&config));
            }
        }

        tokio::spawn(async move {
            let mut interval = tokio::time::interval(
                tokio::time::Duration::from_millis(config.update_interval_ms)
            );
            let mut spawn_timer = 0u64;

            loop {
                // 检查是否停止
                if !*running.read().await {
                    break;
                }

                // 定期生成新目标
                spawn_timer += 1;
                if spawn_timer >= (config.spawn_interval_secs * 1000 / config.update_interval_ms) {
                    spawn_timer = 0;
                    let mut tgt = targets.write().await;
                    if tgt.len() < 20 {
                        tgt.push(Self::spawn_random_target(&config));
                        tracing::info!("🧪 生成新仿真目标, 当前总数: {}", tgt.len());
                    }
                }

                // 更新每个目标的运动状态
                let dt = config.update_interval_ms as f64 / 1000.0;
                {
                    let mut tgt = targets.write().await;
                    for target in tgt.iter_mut() {
                        Self::update_target_motion(target, dt, &config);
                    }
                }

                // 为每个传感器生成数据帧
                let current_targets = targets.read().await.clone();
                for sensor in &sensors {
                    for target in &current_targets {
                        if !target.is_active { continue; }

                        // 计算距离
                        let dist = Self::haversine_distance(
                            &sensor.position, &target.position
                        );
                        if dist > config.radar_range_km * 1000.0 { continue; }

                        // 根据传感器类型生成帧
                        let frame = match sensor.sensor_type {
                            SensorType::Radar => Self::gen_radar_frame(sensor, target),
                            SensorType::Rf => Self::gen_rf_frame(sensor, target),
                            SensorType::EoIr => Self::gen_eoir_frame(sensor, target),
                            SensorType::Acoustic => Self::gen_acoustic_frame(sensor, target),
                            _ => continue,
                        };

                        // 发送到消息总线
                        let _ = bus.send_sensor_frame(frame).await;

                        // 更新帧计数
                        *frame_count.write().await += 1;
                    }
                }

                interval.tick().await;
            }
        });
    }

    /// 停止仿真引擎
    pub async fn stop(&self) {
        let mut running = self.running.write().await;
        *running = false;
        tracing::info!("🧪 传感器仿真引擎停止");
    }

    pub async fn is_running(&self) -> bool {
        *self.running.read().await
    }

    pub async fn frame_count(&self) -> u64 {
        *self.frame_count.read().await
    }

    pub async fn target_count(&self) -> usize {
        self.targets.read().await.len()
    }

    pub async fn get_targets(&self) -> Vec<SimulatedTarget> {
        self.targets.read().await.clone()
    }

    pub async fn add_target(&self, target: SimulatedTarget) {
        self.targets.write().await.push(target);
    }

    /// 产生随机目标
    fn spawn_random_target(config: &SimulatorConfig) -> SimulatedTarget {
        let mut rng = rand::thread_rng();

        let behaviors: &[BehaviorType] = &[
            BehaviorType::Reconnaissance,
            BehaviorType::Attack,
            BehaviorType::Decoy,
            BehaviorType::Surveillance,
            BehaviorType::Transport,
        ];
        let classes: &[TargetClass] = &[
            TargetClass::Dji,
            TargetClass::Autel,
            TargetClass::Multicopter,
            TargetClass::FixedWing,
            TargetClass::Unknown,
        ];
        let threats: &[ThreatLevel] = &[ThreatLevel::Green, ThreatLevel::Yellow, ThreatLevel::Red];

        let heading = rng.gen_range(0.0f64..360.0);
        let speed = rng.gen_range(8.0f64..35.0);
        let vn = speed * f64::cos(f64::to_radians(heading));
        let ve = speed * f64::sin(f64::to_radians(heading));

        // 随机位置（在中心点周围）
        let angle = rng.gen_range(0.0f64..std::f64::consts::TAU);
        let radius_deg = config.area_radius_km / 111.0 * rng.gen_range(0.3..1.0);
        let lat = config.center_lat + radius_deg * angle.cos();
        let lon = config.center_lon + radius_deg * angle.sin();
        let alt = rng.gen_range(50.0..500.0);

        // 生成几个路径点，让目标沿路径飞行
        let mut waypoints = Vec::new();
        let num_wp = rng.gen_range(3..8);
        for _ in 0..num_wp {
            let wa = rng.gen_range(0.0..std::f64::consts::TAU);
            let wr = config.area_radius_km / 111.0 * rng.gen_range(0.2..1.0);
            waypoints.push((config.center_lat + wr * wa.cos(), config.center_lon + wr * wa.sin()));
        }

        let behavior = behaviors[rng.gen_range(0..behaviors.len())].clone();
        let threat = match behavior {
            BehaviorType::Attack => ThreatLevel::Red,
            BehaviorType::Decoy => ThreatLevel::Yellow,
            BehaviorType::Reconnaissance => ThreatLevel::Yellow,
            _ => {
                if rng.gen_bool(0.3) { ThreatLevel::Yellow } else { ThreatLevel::Green }
            }
        };

        SimulatedTarget {
            target_id: Uuid::new_v4(),
            classification: classes[rng.gen_range(0..classes.len())].clone(),
            position: GeoPosition { latitude: lat, longitude: lon, altitude: alt },
            velocity: Velocity3D { vn, ve, vd: 0.0 },
            heading,
            speed,
            altitude: alt,
            behavior,
            threat_level: threat,
            rcs: rng.gen_range(0.01..0.5),
            rf_freq: if rng.gen_bool(0.7) { rng.gen_range(2_400_000_000.0..2_485_000_000.0) } else { rng.gen_range(5_725_000_000.0..5_850_000_000.0) },
            is_active: true,
            spawn_time: chrono::Utc::now(),
            waypoints,
            current_wp: 0,
        }
    }

    /// 更新目标运动（沿路径点飞行）
    fn update_target_motion(target: &mut SimulatedTarget, dt: f64, config: &SimulatorConfig) {
        if target.waypoints.is_empty() { return; }

        // 获取当前目标路径点
        let wp = target.waypoints[target.current_wp];

        // 计算到路径点的方向
        let d_lat = wp.0 - target.position.latitude;
        let d_lon = wp.1 - target.position.longitude;
        let dist = (d_lat * d_lat + d_lon * d_lon).sqrt();

        if dist < 0.0005 {
            // 到达路径点，切换下一个
            target.current_wp = (target.current_wp + 1) % target.waypoints.len();
            // 速度微调
            let mut rng = rand::thread_rng();
            target.speed = rng.gen_range(8.0..35.0);
            return;
        }

        // 朝路径点移动
        let heading = d_lon.atan2(d_lat);
        let speed_ms = target.speed * dt / 111_000.0; // 度/秒

        target.position.latitude += heading.cos() * speed_ms;
        target.position.longitude += heading.sin() * speed_ms;
        target.velocity.vn = target.speed * heading.cos();
        target.velocity.ve = target.speed * heading.sin();

        // 高度随机波动
        let mut rng = rand::thread_rng();
        target.position.altitude += rng.gen_range(-2.0..2.0);

        // 攻击行为：下降高度
        if target.behavior == BehaviorType::Attack {
            target.position.altitude -= rng.gen_range(0.5..2.0);
            target.position.altitude = target.position.altitude.max(10.0);
        }

        // 更新威胁等级（距中心越近越危险）
        let center_dist = Self::haversine_distance(
            &target.position,
            &GeoPosition { latitude: config.center_lat, longitude: config.center_lon, altitude: 0.0 }
        );
        if center_dist < 1000.0 && target.threat_level != ThreatLevel::Red {
            target.threat_level = ThreatLevel::Red;
        } else if center_dist < 2000.0 && target.threat_level == ThreatLevel::Green {
            target.threat_level = ThreatLevel::Yellow;
        }
    }

    /// 生成雷达探测帧
    fn gen_radar_frame(sensor: &SimulatedSensor, target: &SimulatedTarget) -> SensorFrame {
        let mut rng = rand::thread_rng();
        let dist = Self::haversine_distance(&sensor.position, &target.position);

        SensorFrame {
            frame_id: Uuid::new_v4(),
            sensor_id: sensor.sensor_id,
            sensor_type: SensorType::Radar,
            timestamp: chrono::Utc::now(),
            position: target.position.clone(),
            confidence: rng.gen_range(0.75..0.99),
            payload: SensorPayload::Radar(RadarDetection {
                range: dist,
                azimuth: rng.gen_range(0.0..360.0),
                elevation: rng.gen_range(-10.0..60.0),
                rcs: target.rcs + rng.gen_range(-0.02..0.02),
                radial_velocity: target.speed * rng.gen_range(0.8..1.2),
                snr: rng.gen_range(15.0..30.0),
            }),
        }
    }

    /// 生成RF频谱帧
    fn gen_rf_frame(sensor: &SimulatedSensor, target: &SimulatedTarget) -> SensorFrame {
        let mut rng = rand::thread_rng();

        SensorFrame {
            frame_id: Uuid::new_v4(),
            sensor_id: sensor.sensor_id,
            sensor_type: SensorType::Rf,
            timestamp: chrono::Utc::now(),
            position: target.position.clone(),
            confidence: rng.gen_range(0.65..0.92),
            payload: SensorPayload::Rf(RfSpectrum {
                center_freq_hz: target.rf_freq + rng.gen_range(-5_000_000.0..5_000_000.0),
                bandwidth_hz: rng.gen_range(10_000_000.0..40_000_000.0),
                power_db: rng.gen_range(-60.0..-30.0),
                protocol_type: Some(if rng.gen_bool(0.6) { "OcuSync".into() } else { "Lightbridge".into() }),
                modulation: Some("OFDM".into()),
            }),
        }
    }

    /// 生成光电帧
    fn gen_eoir_frame(sensor: &SimulatedSensor, target: &SimulatedTarget) -> SensorFrame {
        let mut rng = rand::thread_rng();

        SensorFrame {
            frame_id: Uuid::new_v4(),
            sensor_id: sensor.sensor_id,
            sensor_type: SensorType::EoIr,
            timestamp: chrono::Utc::now(),
            position: target.position.clone(),
            confidence: rng.gen_range(0.7..0.95),
            payload: SensorPayload::Image(ImageFrame {
                width: 1920,
                height: 1080,
                format: "jpeg".into(),
                data_uri: format!("data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCI+PHJlY3Qgd2lkdGg9IjEwMCIgaGVpZ2h0PSIxMDAiIGZpbGw9IiMwMDAiLz48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI4IiBmaWxsPSIjZmYwMDAwIi8+PC9zdmc+"),
                detections: vec![BoundingBox {
                    x: rng.gen_range(0.2..0.8),
                    y: rng.gen_range(0.2..0.8),
                    w: rng.gen_range(0.05..0.15),
                    h: rng.gen_range(0.05..0.15),
                    class: "UAV".into(),
                    confidence: rng.gen_range(0.7..0.98),
                }],
            }),
        }
    }

    /// 生成声学帧
    fn gen_acoustic_frame(sensor: &SimulatedSensor, target: &SimulatedTarget) -> SensorFrame {
        let mut rng = rand::thread_rng();
        let base_freq = 150.0 + target.speed * 5.0;

        SensorFrame {
            frame_id: Uuid::new_v4(),
            sensor_id: sensor.sensor_id,
            sensor_type: SensorType::Acoustic,
            timestamp: chrono::Utc::now(),
            position: target.position.clone(),
            confidence: rng.gen_range(0.55..0.85),
            payload: SensorPayload::Acoustic(AcousticFeatures {
                frequency_peak_hz: base_freq + rng.gen_range(-10.0..10.0),
                harmonic_profile: vec![
                    base_freq,
                    base_freq * 2.0,
                    base_freq * 3.0,
                    base_freq * 4.0,
                ],
                mfcc: (0..13).map(|_| rng.gen_range(-1.0..1.0)).collect(),
            }),
        }
    }

    fn haversine_distance(p1: &GeoPosition, p2: &GeoPosition) -> f64 {
        let r = 6371000.0f64;
        let d_lat = f64::to_radians(p2.latitude - p1.latitude);
        let d_lon = f64::to_radians(p2.longitude - p1.longitude);
        let a = f64::sin(d_lat / 2.0f64).powi(2)
            + f64::cos(f64::to_radians(p1.latitude))
            * f64::cos(f64::to_radians(p2.latitude))
            * f64::sin(d_lon / 2.0f64).powi(2);
        r * 2.0 * f64::asin(f64::sqrt(a))
    }
}
