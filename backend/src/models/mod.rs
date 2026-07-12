use serde::{Deserialize, Serialize};
use chrono::{DateTime, Utc};
use uuid::Uuid;

// ==================== 传感器数据类型 ====================

/// 传感器类型
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum SensorType {
    Radar,
    Rf,
    EoIr,
    Acoustic,
    Lidar,
    Other(String),
}

/// 传感器状态
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum SensorStatus {
    Online,
    Offline,
    Degraded,
    Calibrating,
}

/// 传感器元数据
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SensorMetadata {
    pub sensor_id: Uuid,
    pub sensor_type: SensorType,
    pub name: String,
    pub model: String,
    pub location: GeoPosition,
    pub status: SensorStatus,
    pub last_heartbeat: DateTime<Utc>,
    pub capabilities: Vec<String>,
    pub config: serde_json::Value,
}

/// 传感器数据帧 (统一接口)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SensorFrame {
    pub frame_id: Uuid,
    pub sensor_id: Uuid,
    pub sensor_type: SensorType,
    pub timestamp: DateTime<Utc>,
    pub position: GeoPosition,
    pub confidence: f64,
    pub payload: SensorPayload,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct GeoPosition {
    pub latitude: f64,
    pub longitude: f64,
    pub altitude: f64,
}

/// 传感器载荷数据
#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(tag = "type", content = "data")]
pub enum SensorPayload {
    Radar(RadarDetection),
    Rf(RfSpectrum),
    Image(ImageFrame),
    Acoustic(AcousticFeatures),
    Lidar(PointCloud),
}

/// 雷达探测数据
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct RadarDetection {
    pub range: f64,
    pub azimuth: f64,
    pub elevation: f64,
    pub rcs: f64,
    pub radial_velocity: f64,
    pub snr: f64,
}

/// RF 频谱数据
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct RfSpectrum {
    pub center_freq_hz: f64,
    pub bandwidth_hz: f64,
    pub power_db: f64,
    pub protocol_type: Option<String>,
    pub modulation: Option<String>,
}

/// 图像数据
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ImageFrame {
    pub width: u32,
    pub height: u32,
    pub format: String,
    pub data_uri: String,
    pub detections: Vec<BoundingBox>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct BoundingBox {
    pub x: f32,
    pub y: f32,
    pub w: f32,
    pub h: f32,
    pub class: String,
    pub confidence: f32,
}

/// 声学特征
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AcousticFeatures {
    pub frequency_peak_hz: f64,
    pub harmonic_profile: Vec<f64>,
    pub mfcc: Vec<f64>,
}

/// 点云数据
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct PointCloud {
    pub points: Vec<Point3D>,
    pub intensity: Vec<f64>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Point3D {
    pub x: f64,
    pub y: f64,
    pub z: f64,
}

// ==================== 目标数据类型 ====================

/// 目标状态
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TrackedTarget {
    pub target_id: Uuid,
    pub track_id: Uuid,
    pub position: GeoPosition,
    pub velocity: Velocity3D,
    pub acceleration: Option<Acceleration3D>,
    pub classification: TargetClass,
    pub threat_level: ThreatLevel,
    pub confidence: f64,
    pub first_seen: DateTime<Utc>,
    pub last_update: DateTime<Utc>,
    pub track_history: Vec<TrackPoint>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Velocity3D {
    pub vn: f64,
    pub ve: f64,
    pub vd: f64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Acceleration3D {
    pub an: f64,
    pub ae: f64,
    pub ad: f64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TrackPoint {
    pub timestamp: DateTime<Utc>,
    pub position: GeoPosition,
    pub velocity: Velocity3D,
}

/// 目标分类
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum TargetClass {
    Dji,         // 大疆
    Autel,       // 道通
    FixedWing,   // 固定翼
    Multicopter, // 多旋翼
    Bird,        // 鸟类（虚警）
    Helicopter,  // 直升机
    Unknown,     // 未知
    Other(String),
}

/// 行为类型
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum BehaviorType {
    Reconnaissance,
    Attack,
    Decoy,
    Surveillance,
    Transport,
    Loitering,
    Unknown,
}

/// 威胁等级
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq, PartialOrd)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum ThreatLevel {
    Green,   // 常规监视
    Yellow,  // 密切监视
    Red,     // 立即拦截
}

// ==================== 任务规划类型 ====================

/// 拦截任务
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct InterceptMission {
    pub mission_id: Uuid,
    pub target_id: Uuid,
    pub assigned_interceptors: Vec<Uuid>,
    pub trajectory: Vec<Waypoint>,
    pub priority: u32,
    pub status: MissionStatus,
    pub created_at: DateTime<Utc>,
    pub updated_at: DateTime<Utc>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Waypoint {
    pub position: GeoPosition,
    pub arrival_time: DateTime<Utc>,
    pub speed: f64,
    pub action: WaypointAction,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum WaypointAction {
    FlyTo,
    Engage,
    Jam,
    Decoy,
    Return,
    Loiter,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum MissionStatus {
    Planning,
    Active,
    Executing,
    Completed,
    Failed,
    Cancelled,
}

/// 资源管理
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct InterceptorResource {
    pub resource_id: Uuid,
    pub resource_type: ResourceType,
    pub name: String,
    pub position: GeoPosition,
    pub status: ResourceStatus,
    pub capabilities: Vec<String>,
    pub fuel_remaining: f64,
    pub max_range: f64,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum ResourceType {
    ElectronicJammer,
    HighPowerMicrowave,
    KineticInterceptor,
    NetCapture,
    DecoyDrone,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum ResourceStatus {
    Available,
    Deployed,
    Recovering,
    Maintenance,
    Depleted,
}

// ==================== 态势类型 ====================

/// 全局态势
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SituationalAwareness {
    pub timestamp: DateTime<Utc>,
    pub targets: Vec<TrackedTarget>,
    pub missions: Vec<InterceptMission>,
    pub sensors: Vec<SensorMetadata>,
    pub resources: Vec<InterceptorResource>,
    pub airspace_zones: Vec<AirspaceZone>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AirspaceZone {
    pub zone_id: Uuid,
    pub zone_type: ZoneType,
    pub boundary: Vec<GeoPosition>,
    pub altitude_min: f64,
    pub altitude_max: f64,
    pub active: bool,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum ZoneType {
    NoFly,         // 禁飞区
    Restricted,    // 限制区
    Danger,        // 危险区
    Engagement,    // 交战区
    Protected,     // 防护区
}

// ==================== WebSocket 消息 ====================

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(tag = "event", content = "payload")]
pub enum WsEvent {
    TargetUpdated(Vec<TrackedTarget>),
    ThreatAlert(TrackedTarget),
    MissionUpdate(InterceptMission),
    SensorStatus(SensorMetadata),
    SystemHealth(SystemHealth),
    ChatMessage(ChatMessage),
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SystemHealth {
    pub cpu_usage: f64,
    pub memory_usage: f64,
    pub active_targets: u32,
    pub active_missions: u32,
    pub sensor_count: u32,
    pub uptime_seconds: u64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ChatMessage {
    pub message_id: Uuid,
    pub sender: String,
    pub content: String,
    pub timestamp: DateTime<Utc>,
    pub message_type: ChatMessageType,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum ChatMessageType {
    User,
    Assistant,
    Command,
    System,
}

// ==================== 算法管理类型 ====================

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AlgorithmVersion {
    pub algo_id: Uuid,
    pub name: String,
    pub version: String,
    pub status: AlgorithmStatus,
    pub metrics: AlgorithmMetrics,
    pub deployed_at: DateTime<Utc>,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
#[serde(rename_all = "SCREAMING_SNAKE_CASE")]
pub enum AlgorithmStatus {
    Development,
    Testing,
    Deployed,
    RolledBack,
    Deprecated,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AlgorithmMetrics {
    pub accuracy: f64,
    pub precision: f64,
    pub recall: f64,
    pub latency_ms: f64,
    pub throughput: f64,
}

// ==================== 设备管理类型 (hsim_sb_*) ====================

/// 设备配置 (hsim_sb_pz)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DeviceConfig {
    pub id: Option<i64>,
    pub name: Option<String>,
    pub device_id: String,
    pub station_id: i32,
    pub device_type: String,       // DETECT/TRAP/DISTURB/System
    pub device_ip: String,
    pub device_port: i32,
    pub protocol_version: Option<String>,
    pub is_valid: i32,
    pub status: Option<String>,    // CONNECTED/DISCONNECTED/WARN
    pub update_time: Option<String>,
    pub jd: Option<f64>,
    pub wd: Option<f64>,
    pub gd: Option<f64>,
    pub zcbj: Option<String>,
    pub dyqk: Option<String>,
    pub mac: Option<String>,
    pub xh: Option<String>,
    pub sccj: Option<String>,
    pub ccrq: Option<String>,
    pub r#type: Option<String>,    // UDP/TCPSocket/TCPServerSocket
    pub udp_port: Option<i32>,
}

/// 设备心跳 (hsim_sb_xt)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DeviceHeartbeat {
    pub id: Option<i64>,
    pub station_id: Option<i32>,
    pub data_time: Option<String>,
    pub main_card: Option<i32>,
    pub trap_card: Option<i32>,
    pub compass: Option<i32>,
    pub disturb_card: Option<i32>,
    pub longitude: Option<f64>,
    pub latitude: Option<f64>,
    pub altitude: Option<i32>,
    pub angle: Option<f64>,
    pub cpu_rate: Option<f64>,
    pub disk_usage: Option<f64>,
    pub card_temp: Option<f64>,
    pub amp_temp: Option<f64>,
    pub create_time: Option<String>,
    pub work_state: Option<i32>,
}

/// 设备连接日志 (hsim_sb_lj)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ConnectLog {
    pub id: Option<i64>,
    pub station_id: i32,
    pub device_ip: String,
    pub device_port: i32,
    pub event_type: String,        // connect/disconnect
    pub event_time: Option<String>,
    pub reason: Option<String>,
}

/// 侦测频谱数据 (hsim_sb_zcpp)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DetectSpectrum {
    pub id: Option<i64>,
    pub station_id: i32,
    pub model: Option<String>,
    pub freq: Option<i64>,
    pub rssi: Option<f64>,
    pub bandwidth: Option<i32>,
    pub data_time: Option<String>,
    pub create_time: Option<String>,
}

/// 设备操作日志 (hsim_sb_czrz)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct OperateLog {
    pub id: Option<i64>,
    pub station_id: Option<i32>,
    pub device_type: Option<String>,
    pub cmd_type: Option<String>,
    pub cmd_name: Option<String>,
    pub cmd_param: Option<String>,
    pub result: Option<String>,
    pub operate_time: Option<String>,
}

/// 设备主动心跳 (hsim_sb_xt_zd)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ActiveHeartbeat {
    pub id: Option<i64>,
    pub start_code: Option<String>,
    pub source_addr: Option<i32>,
    pub dest_addr: Option<i32>,
    pub command: Option<i32>,
    pub param_length: Option<i32>,
    pub terminal_code: Option<String>,
    pub station_id: Option<i32>,
    pub data_time: Option<String>,
    pub auth_normal: Option<i32>,
    pub detect_enabled: Option<i32>,
    pub counter_enabled: Option<i32>,
    pub detector_online: Option<i32>,
    pub counter_online: Option<i32>,
    pub unattended_mode: Option<i32>,
    pub deceiver_online: Option<i32>,
    pub jamming_mode: Option<i32>,
    pub band_58g: Option<i32>,
    pub band_24g: Option<i32>,
    pub band_900m: Option<i32>,
    pub band_14g: Option<i32>,
    pub band_52g: Option<i32>,
    pub ptz_control_mode: Option<i32>,
    pub attack_countdown: Option<i32>,
    pub deception_status: Option<i32>,
    pub gnss_link_status: Option<i32>,
    pub gnss_deception_mode: Option<i32>,
    pub gnss_induce_mode: Option<i32>,
    pub no_fly_lat: Option<f64>,
    pub no_fly_lng: Option<f64>,
    pub no_fly_alt: Option<f64>,
    pub forced_land_lat: Option<f64>,
    pub forced_land_lng: Option<f64>,
    pub forced_land_alt: Option<f64>,
    pub forced_land_radius: Option<f64>,
    pub checksum: Option<i32>,
    pub create_time: Option<String>,
    pub update_time: Option<String>,
}

// ==================== 无人机管理类型 (hsim_wrj_*) ====================

/// 无人机特征库 (hsim_wrj_tzk) — 50+ 字段
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DroneFeatureLib {
    pub id: Option<String>,
    pub mc: Option<String>,
    pub serial_number: Option<String>,
    pub brand: Option<String>,
    pub model: Option<String>,
    pub r#type: Option<String>,
    pub dqfl: Option<String>,
    pub zlfl: Option<String>,
    pub jj: Option<String>,
    pub jc: Option<String>,
    pub xtczy: Option<String>,
    pub sx: Option<String>,
    pub sysx: Option<String>,
    pub zdsx: Option<String>,
    pub yxsx: Option<String>,
    pub jg: Option<String>,
    pub zzbj: Option<String>,
    pub kzbj: Option<String>,
    pub zcfw: Option<String>,
    pub yz: Option<String>,
    pub jz: Option<String>,
    pub rwzb: Option<String>,
    pub yxzh: Option<String>,
    pub zdsd: Option<String>,
    pub zdqfzl: Option<String>,
    pub xhsd: Option<String>,
    pub xhgd: Option<String>,
    pub xhsj: Option<String>,
    pub xhjl: Option<String>,
    pub qdbj: Option<String>,
    pub fdjsl: Option<String>,
    pub fxsd: Option<String>,
    pub zz: Option<String>,
    pub kz: Option<String>,
    pub zdhc: Option<String>,
    pub scdw: Option<String>,
    pub qymc: Option<String>,
    pub fdj: Option<String>,
    pub dlzz: Option<String>,
    pub jzcl: Option<String>,
    pub dmczry: Option<String>,
    pub dwjd: Option<String>,
    pub yxcsjl: Option<String>,
    pub cd: Option<String>,
    pub wx: Option<String>,
    pub hdfs: Option<String>,
    pub zj: Option<String>,
    pub nyzl: Option<String>,
    pub hs: Option<String>,
    pub gzqs: Option<String>,
    pub zcl: Option<String>,
    pub hc: Option<String>,
    pub dzpt: Option<String>,
    pub dy: Option<String>,
    pub dzsb: Option<String>,
    pub hldjnl: Option<String>,
    pub ldjdzsb: Option<String>,
    pub td: Option<String>,
    pub jgtd: Option<String>,
    pub zczb: Option<String>,
    pub zznl: Option<String>,
    pub zcjsnl: Option<String>,
    pub bpjbs: Option<String>,
    pub remark: Option<String>,
    pub tp: Option<String>,
    pub mxdz: Option<String>,
    pub cjr: Option<String>,
    pub cjrid: Option<String>,
    pub cjsj: Option<String>,
    pub czr: Option<String>,
    pub czrid: Option<String>,
    pub czsj: Option<String>,
    pub zdxhsd: Option<String>,
    pub wrjxhsj: Option<String>,
    pub tcnl: Option<String>,
}

/// 无人机基本信息 (hsim_wrj_jbxx)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DroneBasicInfo {
    pub id: Option<String>,
    pub serial_number: Option<String>,
    pub brand: Option<String>,
    pub model: Option<String>,
    pub status: Option<i32>,         // 1=正常 2=告警 3=失联
    pub auth_status: Option<i32>,    // 1=白名单 2=黑名单 3=未授权
    pub current_longitude: Option<f64>,
    pub current_latitude: Option<f64>,
    pub current_altitude: Option<i32>,
    pub last_seen_time: Option<String>,
    pub remark: Option<String>,
    pub rwlx: Option<String>,
    pub ssdw: Option<String>,
    pub tp: Option<String>,
    pub mxdz: Option<String>,
    pub cjr: Option<String>,
    pub cjrid: Option<String>,
    pub cjsj: Option<String>,
    pub czr: Option<String>,
    pub czrid: Option<String>,
    pub czsj: Option<String>,
}

/// 黑白名单授权 (hsim_wrj_hbmdsq)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct BlackWhiteList {
    pub id: Option<String>,
    pub mdlx: Option<String>,        // 黑名单/白名单
    pub wrjid: Option<String>,
    pub sqsj: Option<String>,
    pub sqgqsj: Option<String>,
    pub cjr: Option<String>,
    pub cjrid: Option<String>,
    pub cjsj: Option<String>,
    pub czr: Option<String>,
    pub czrid: Option<String>,
    pub czsj: Option<String>,
}

/// 告警记录 (hsim_wrj_gjjl)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AlarmRecord {
    pub id: Option<String>,
    pub kymc: Option<String>,
    pub kyid: Option<String>,
    pub wrjid: Option<String>,
    pub wrjpp: Option<String>,
    pub wrjxh: Option<String>,
    pub wrjxlh: Option<String>,
    pub gjlx: Option<String>,        // 1=闯入禁飞区 2=超高度 3=黑名单入侵 4=未授权飞行
    pub gjfsjd: Option<f64>,
    pub gjfsgd: Option<f64>,
    pub gjfswd: Option<f64>,
    pub gjfssj: Option<String>,
    pub clzt: Option<String>,        // 0=未处理 1=已处理 2=忽略
    pub clsj: Option<String>,
    pub clrid: Option<String>,
    pub clr: Option<String>,
    pub clbz: Option<String>,
    pub fsjd: Option<f64>,
    pub fswd: Option<f64>,
    pub zdid: Option<String>,
    pub zdmc: Option<String>,
    pub gjys: Option<String>,
    pub diff_seconds: Option<i32>,
    pub alarm_group_id: Option<i64>,
    pub cjr: Option<String>,
    pub cjrid: Option<String>,
    pub cjsj: Option<String>,
    pub czr: Option<String>,
    pub czrid: Option<String>,
    pub czsj: Option<String>,
}

/// 空域 (hsim_wrj_ky)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Airspace {
    pub id: Option<String>,
    pub mc: Option<String>,
    pub lx: Option<String>,          // 限制区/禁飞区/预警区/允许飞行区域
    pub xz: Option<String>,          // 1=圆形 2=多边形 3=矩形
    pub zxdjd: Option<f64>,
    pub zxdwd: Option<f64>,
    pub bj: Option<f64>,
    pub zxgd: Option<i32>,
    pub zdgd: Option<i32>,
    pub ddzbjh: Option<String>,      // 顶点坐标集合
    pub cd: Option<f64>,
    pub kd: Option<f64>,
    pub kssj: Option<String>,
    pub jssj: Option<String>,
    pub sfqy: Option<i32>,
    pub bz: Option<String>,
    pub ys: Option<String>,
    pub jfqbj: Option<f64>,
    pub jfqys: Option<String>,
    pub yjqbj: Option<f64>,
    pub yjqys: Option<String>,
    pub cjr: Option<String>,
    pub cjrid: Option<String>,
    pub cjsj: Option<String>,
    pub czr: Option<String>,
    pub czrid: Option<String>,
    pub czsj: Option<String>,
}

// ==================== 飞行数据类型 (hsim_wrj_fxsj_*) ====================

/// 飞行数据-报文协议 (hsim_wrj_fxsj_zcbw)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DroneDetectMsg {
    pub id: Option<i64>,
    pub station_id: i32,
    pub serial: Option<String>,
    pub model: Option<String>,
    pub dron_lng: Option<f64>,
    pub dron_lat: Option<f64>,
    pub home_lng: Option<f64>,
    pub home_lat: Option<f64>,
    pub pilot_lng: Option<f64>,
    pub pilot_lat: Option<f64>,
    pub altitude: Option<f64>,
    pub height: Option<f64>,
    pub east_v: Option<f64>,
    pub north_v: Option<f64>,
    pub up_v: Option<f64>,
    pub freq: Option<i64>,
    pub rssi: Option<f64>,
    pub distance: Option<f64>,
    pub uuid: Option<String>,
    pub angle: Option<f64>,
    pub data_time: Option<String>,
    pub create_time: Option<String>,
    pub sd: Option<f64>,
    pub mac: Option<String>,
    pub jmlx: Option<String>,
    pub data_type: Option<i32>,
    pub fused_flag: Option<i32>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub wxdj: Option<String>,
}

/// 飞行数据-解析协议 (hsim_wrj_fxsj_jx)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DroneDfData {
    pub id: Option<i64>,
    pub station_id: Option<i32>,
    pub data_time: Option<String>,
    pub target_type: Option<i32>,
    pub detect_type: Option<i32>,
    pub freq: Option<i64>,
    pub dk: Option<i32>,
    pub longitude: Option<f64>,
    pub latitude: Option<f64>,
    pub angle: Option<f64>,
    pub signal_level: Option<f64>,
    pub compass: Option<f64>,
    pub distance: Option<f64>,
    pub uav_model: Option<String>,
    pub uav_id: Option<String>,
    pub device_id: Option<String>,
    pub create_time: Option<String>,
    pub speed: Option<f64>,
    pub height: Option<f64>,
}

/// 飞行数据-Remote协议 (hsim_wrj_fxsj_remote)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DroneRemoteData {
    pub id: Option<String>,
    pub ris_ssid: Option<String>,
    pub serial: Option<String>,
    pub model: Option<String>,
    pub ua_type: Option<String>,
    pub dron_lng: Option<f64>,
    pub dron_lat: Option<f64>,
    pub pilot_lng: Option<f64>,
    pub pilot_lat: Option<f64>,
    pub speed: Option<f64>,
    pub vspeed: Option<f64>,
    pub direc: Option<f64>,
    pub altitudep: Option<f64>,
    pub altitudeg: Option<f64>,
    pub height_agl: Option<f64>,
    pub mac: Option<String>,
    pub rssi: Option<f64>,
    pub freq: Option<String>,
    pub angle: Option<f64>,
    pub distance: Option<i32>,
    pub date: Option<String>,
    pub station_id: Option<i32>,
}

// ==================== 通用分页响应 ====================

/// 分页查询请求
#[derive(Debug, Clone, Deserialize)]
pub struct PageQuery {
    pub page: Option<u32>,
    pub page_size: Option<u32>,
    #[serde(default)]
    pub filters: std::collections::HashMap<String, String>,
}

impl Default for PageQuery {
    fn default() -> Self {
        Self { page: Some(1), page_size: Some(20), filters: Default::default() }
    }
}

/// 分页查询响应
#[derive(Debug, Clone, Serialize)]
pub struct PageResult<T: Serialize> {
    pub records: Vec<T>,
    pub total: i64,
    pub page: u32,
    pub page_size: u32,
}

/// API 统一响应
#[derive(Debug, Clone, Serialize)]
pub struct ApiResponse<T: Serialize> {
    pub success: bool,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub result: Option<T>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub message: Option<String>,
}

impl<T: Serialize> ApiResponse<T> {
    pub fn ok(result: T) -> Self {
        Self { success: true, result: Some(result), message: None }
    }
    pub fn error(msg: impl Into<String>) -> Self {
        Self { success: false, result: None, message: Some(msg.into()) }
    }
}

// ==================== wxdzc 侦测预警专用类型 ====================

/// 按站点分组的无人机侦测消息 VO (对应 getUavDetectMsgByStationId)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UavDetectMsgVo {
    pub model: Option<String>,
    pub serial: Option<String>,
    pub brand: Option<String>,
    pub status: Option<i32>,         // 1=正常 2=告警 3=失联
    pub auth_status: Option<i32>,    // 1=白名单 2=黑名单 3=未授权 4=待定
    pub station_id: Option<i32>,
    pub station_name: Option<String>,
    pub wxdj: Option<String>,        // red/yellow/green
    pub jmlx: Option<String>,
    #[serde(flatten)]
    pub uav_detect_msg: DroneDetectMsg,
}

/// 按型号序列号查询的侦测消息 VO (对应 getUavDetectMsgByModelSerialRq)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UavDetectMsgVo1 {
    pub uav_detect_msg_list: Vec<DroneDetectMsg>,
    pub wjbd_wrj_jbxx: Option<DroneBasicInfo>,
}

/// 日期日历 VO (对应 getUavDetectMsgDateByNfYf)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UavDetectMsgDateVo {
    pub rq: String,
    #[serde(rename = "type")]
    pub r_type: String,
}

/// 生成飞行路线请求 (对应 generateFlightRoute)
#[derive(Debug, Clone, Deserialize)]
pub struct FlightRouteRequest {
    pub station_id: Option<i32>,
    pub model: Option<String>,
    pub serial: Option<String>,
    pub rq: Option<String>,
    pub start: Option<serde_json::Value>,
    pub end: Option<serde_json::Value>,
    pub waypoints: Option<Vec<serde_json::Value>>,
}

/// wxdzc 设备查询参数
#[derive(Debug, Clone, Deserialize)]
pub struct WxdzcDeviceQuery {
    pub page: Option<u32>,
    pub page_size: Option<u32>,
    #[serde(default)]
    pub name: Option<String>,
}

/// wxdzc 站点侦测查询参数
#[derive(Debug, Clone, Deserialize)]
pub struct StationDetectQuery {
    pub rq: Option<String>,          // 日期 YYYY-MM-DD
    pub auth_status: Option<i32>,    // 授权状态过滤
}

/// wxdzc 型号序列号查询参数
#[derive(Debug, Clone, Deserialize)]
pub struct ModelSerialQuery {
    pub model: Option<String>,
    pub serial: Option<String>,
    pub rq: Option<String>,
}

/// wxdzc 日期日历查询参数
#[derive(Debug, Clone, Deserialize)]
pub struct DateCalendarQuery {
    pub nf: Option<String>,          // 年份
    pub yf: Option<String>,          // 月份
}

// ==================== wrj 武警业务新增类型 ====================

/// 无人机详细信息 (hsim_wrj_jbxx_new) — 39列
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DroneDetailInfo {
    pub id: Option<String>, pub serial_number: Option<String>, pub brand: Option<String>,
    pub model: Option<String>, pub r#type: Option<String>, pub jj: Option<String>,
    pub dy: Option<String>, pub tcnl: Option<String>, pub zznl: Option<String>,
    pub bpjbs: Option<String>, pub zdxhsj: Option<String>, pub zdfxsd: Option<String>,
    pub zdkzjl: Option<String>, pub zdfxgd: Option<String>, pub kfdj: Option<String>,
    pub zdhzzl: Option<String>, pub jscc: Option<String>, pub jszl: Option<String>,
    pub dlxt: Option<String>, pub dwxt: Option<String>, pub xjcs: Option<String>,
    pub tcxt: Option<String>, pub dcgg: Option<String>, pub status: Option<i32>,
    pub auth_status: Option<i32>, pub current_longitude: Option<f64>, pub current_latitude: Option<f64>,
    pub current_altitude: Option<i32>, pub last_seen_time: Option<String>, pub remark: Option<String>,
    pub cjr: Option<String>, pub cjrid: Option<String>, pub cjsj: Option<String>,
    pub czr: Option<String>, pub czrid: Option<String>, pub czsj: Option<String>,
    pub tp: Option<String>, pub mxdz: Option<String>, pub dqfl: Option<String>, pub zlfl: Option<String>,
}

/// 空域授权 (hsim_wrj_kysq)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AirspaceAuth {
    pub id: Option<String>, pub kyid: Option<String>, pub wrjid: Option<String>,
    pub sqsj: Option<String>, pub sqgqsj: Option<String>,
    pub cjr: Option<String>, pub cjrid: Option<String>, pub cjsj: Option<String>,
    pub czr: Option<String>, pub czrid: Option<String>, pub czsj: Option<String>,
}

/// 反无战法 (hsim_wrj_fwzf)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AntiUavTactics {
    pub id: Option<String>, pub fl: Option<String>, pub mc: Option<String>,
    pub zfgs: Option<String>, pub zzcj: Option<String>, pub jbzf: Option<String>,
    pub llbshtxgj: Option<String>, pub jtxdff: Option<String>, pub zfyzqk: Option<String>,
    pub zfyyxybwwt: Option<String>, pub wj: Option<String>, pub remark: Option<String>,
    pub cjr: Option<String>, pub cjrid: Option<String>, pub cjsj: Option<String>,
    pub czr: Option<String>, pub czrid: Option<String>, pub czsj: Option<String>,
}

/// 反制装备 (hsim_wrj_fzzb)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct CounterEquip {
    pub id: Option<String>, pub fl: Option<String>, pub mc: Option<String>,
    pub gntd: Option<String>, pub jszb: Option<String>, pub tp: Option<String>,
    pub sl: Option<String>, pub zbly: Option<String>, pub remark: Option<String>,
    pub cjr: Option<String>, pub cjrid: Option<String>, pub cjsj: Option<String>,
    pub czr: Option<String>, pub czrid: Option<String>, pub czsj: Option<String>,
    pub mxdz: Option<String>,
}

/// 编携配装 (hsim_wrj_bxpz)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct EquipConfig {
    pub id: Option<String>, pub mc: Option<String>, pub pid: Option<String>,
    pub nr: Option<String>, pub remark: Option<String>,
    pub cjr: Option<String>, pub cjrid: Option<String>, pub cjsj: Option<String>,
    pub czr: Option<String>, pub czrid: Option<String>, pub czsj: Option<String>,
}

/// 重要目标 (hsim_wrj_zymb)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ImportantTarget {
    pub id: Option<String>, pub mc: Option<String>, pub jd: Option<String>,
    pub wd: Option<String>, pub rylx: Option<String>, pub sl: Option<String>,
    pub zb: Option<String>, pub ssgk: Option<String>, pub r#type: Option<String>,
    pub subtype: Option<String>,
}

/// 操作视频 (hsim_wrj_czsp)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct OperationVideo {
    pub id: Option<String>, pub spfl: Option<String>, pub mc: Option<String>,
    pub splj: Option<String>, pub cjr: Option<String>, pub cjrid: Option<String>,
    pub cjsj: Option<String>, pub czr: Option<String>, pub czrid: Option<String>,
    pub czsj: Option<String>,
}

/// 收藏夹 (hsim_wrj_scj)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Bookmark {
    pub id: Option<i64>, pub pid: Option<i64>, pub mc: Option<String>,
    pub r#type: Option<String>, pub yh_id: Option<String>, pub csz: Option<String>,
    pub chjr_mc: Option<String>, pub chjr: Option<String>, pub rksj: Option<String>,
}

/// 接口管理 (hsim_wrj_jkgl)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ApiManage {
    pub id: Option<i64>, pub url: Option<String>, pub client: Option<String>,
    pub secret: Option<String>, pub bm: Option<String>, pub bz: Option<String>,
    pub lx: Option<i32>,
}

/// 录屏文件 (hsim_wrj_lpwj)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ScreenRecord {
    pub id: Option<String>, pub mc: Option<String>, pub qdwjml: Option<String>,
    pub yhid: Option<String>,
}

/// 推演计划 (hsim_wrj_tyjh)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SimPlan {
    pub id: Option<String>, pub mc: Option<String>, pub rq: Option<String>,
    pub jhks: Option<String>, pub jhjs: Option<String>, pub serial: Option<String>,
    pub model: Option<String>, pub brand: Option<String>, pub tyzt: Option<String>,
    pub cjr: Option<String>, pub cjrid: Option<String>, pub cjsj: Option<String>,
    pub czr: Option<String>, pub czrid: Option<String>, pub czsj: Option<String>,
    pub start_jd: Option<f64>, pub start_wd: Option<f64>, pub start_gd: Option<f64>,
    pub tjdjwd: Option<String>, pub end_jd: Option<f64>, pub end_wd: Option<f64>,
    pub end_gd: Option<f64>, pub jhcs: Option<String>, pub mbfxfw: Option<String>,
}

/// 推演计划数据 (hsim_wrj_tyjh_data)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SimPlanData {
    pub id: Option<i64>, pub tyjh_id: Option<String>, pub serial: Option<String>,
    pub model: Option<String>, pub dron_lng: Option<f64>, pub dron_lat: Option<f64>,
    pub home_lng: Option<f64>, pub home_lat: Option<f64>, pub pilot_lng: Option<f64>,
    pub pilot_lat: Option<f64>, pub altitude: Option<f64>, pub height: Option<f64>,
    pub east_v: Option<f64>, pub north_v: Option<f64>, pub up_v: Option<f64>,
    pub freq: Option<i64>, pub rssi: Option<f64>, pub distance: Option<f64>,
    pub rid_ssid: Option<String>, pub sd: Option<f64>, pub data_time: Option<String>,
    pub create_time: Option<String>,
}

/// 地形影响标绘 (hsim_dxyy_bh)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TerrainPlot {
    pub id: Option<i64>, pub bhmc: Option<String>, pub tsmc: Option<String>,
    pub bhsj: Option<String>, pub rwsj: Option<String>, pub ztm: Option<i32>,
    pub dwid: Option<String>, pub dwmc: Option<String>, pub jsdwid: Option<String>,
    pub gisjson: Option<String>, pub cjrid: Option<String>, pub cjsj: Option<String>,
    pub yw_id: Option<i64>,
}

/// 频谱结果 (hsim_spectrum)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct SpectrumResult {
    pub id: Option<i64>, pub station_id: Option<i32>, pub channel: Option<String>,
    pub data_type: Option<String>, pub start_freq: Option<f64>, pub stop_freq: Option<f64>,
    pub step_freq: Option<f64>, pub data_len: Option<String>, pub p_data: Option<String>,
    pub create_time: Option<String>,
}

/// 知识库 (hsim_hs_zsk)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct KnowledgeBase {
    pub id: Option<i64>, pub zsk_mc: Option<String>, pub zsk_nm: Option<String>,
    pub zsk_ms: Option<String>, pub chjr_mc: Option<String>, pub chjr: Option<String>,
    pub rksj: Option<String>, pub zsk_lx: Option<i32>,
}

/// 知识库文件 (hsim_hs_zsk_wj)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct KnowledgeFile {
    pub id: Option<i64>, pub zsk_id: Option<i64>, pub wj_mc: Option<String>,
    pub wj_nm: Option<String>, pub wjdx: Option<i64>, pub fwq_wjlj: Option<String>,
    pub chjr_mc: Option<String>, pub chjr: Option<String>, pub rksj: Option<String>,
}

/// 知识库文件内容 (hsim_hs_zsk_wj_nr)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct KnowledgeFileContent {
    pub id: Option<i64>, pub zsk_id: Option<i64>, pub zsk_wj_id: Option<i64>,
    pub nr: Option<String>,
}

// ==================== dxyy 地形影响类型 ====================

/// 地形地貌 (hsim_dxyy_dxdm)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TerrainLandform {
    pub id: Option<String>, pub dmmc: Option<String>, pub jd: Option<String>,
    pub wd: Option<String>, pub dxdmlx: Option<String>, pub mj: Option<String>,
    pub dlwz: Option<String>,
}

/// 用户管理参数 (hsim_wrj_yhgl_cs)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct UserConfig {
    pub id: Option<i64>, pub yh_id: Option<String>, pub cs_mc: Option<String>,
    pub cs_bm: Option<String>, pub csz: Option<String>, pub bz: Option<String>,
    pub chjr_mc: Option<String>, pub chjr: Option<String>, pub rksj: Option<String>,
    pub bdnm: Option<String>,
}

/// 作战力量部队 (hsim_zzll_bd)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ForceUnit {
    pub bdnm: Option<String>, pub bdhfnm: Option<String>, pub bdxh: Option<String>,
    pub bdfh: Option<String>, pub bdjc: Option<String>, pub bzxh: Option<String>,
    pub bzfh: Option<String>, pub bzjc: Option<String>,
}

/// 作战力量部署 (hsim_zzll_bd_bs)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ForceDeployment {
    pub bslbnm: Option<String>, pub bdnm: Option<String>, pub twsj: Option<String>,
    pub bsxsnm: Option<String>, pub dmnm: Option<String>, pub kzdm: Option<String>,
    pub jd: Option<String>, pub wd: Option<String>, pub gc: Option<f64>,
    pub zzsj: Option<String>,
}

// ==================== system 系统类型 ====================

/// 操作日志 (hsim_mh_yycz_log)
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct OperationLog {
    pub id: Option<String>, pub yymc: Option<String>, pub yymk: Option<String>,
    pub rzlx: Option<i32>, pub rznr: Option<String>, pub czyhm: Option<String>,
    pub czyhxm: Option<String>, pub czip: Option<String>, pub cjrid: Option<String>,
    pub cjsj: Option<String>, pub czrid: Option<String>, pub czsj: Option<String>,
    pub ljsc: Option<i32>,
}
