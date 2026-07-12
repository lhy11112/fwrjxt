// ==================== 传感器类型 ====================

export enum SensorType {
  Radar = 'RADAR',
  Rf = 'RF',
  EoIr = 'EO_IR',
  Acoustic = 'ACOUSTIC',
  Lidar = 'LIDAR',
}

export enum SensorStatus {
  Online = 'ONLINE',
  Offline = 'OFFLINE',
  Degraded = 'DEGRADED',
  Calibrating = 'CALIBRATING',
}

export interface SensorMetadata {
  sensor_id: string
  sensor_type: SensorType
  name: string
  model: string
  location: GeoPosition
  status: SensorStatus
  last_heartbeat: string
  capabilities: string[]
  config: any
}

// ==================== 位置/地理 ====================

export interface GeoPosition {
  latitude: number
  longitude: number
  altitude: number
}

export interface Velocity3D {
  vn: number
  ve: number
  vd: number
}

// ==================== 目标类型 ====================

export enum TargetClass {
  Dji = 'DJI',
  Autel = 'AUTEL',
  FixedWing = 'FIXED_WING',
  Multicopter = 'MULTICOPTER',
  Bird = 'BIRD',
  Helicopter = 'HELICOPTER',
  Unknown = 'UNKNOWN',
}

export enum BehaviorType {
  Reconnaissance = 'RECONNAISSANCE',
  Attack = 'ATTACK',
  Decoy = 'DECOY',
  Surveillance = 'SURVEILLANCE',
  Transport = 'TRANSPORT',
  Loitering = 'LOITERING',
  Unknown = 'UNKNOWN',
}

export enum ThreatLevel {
  Green = 'GREEN',
  Yellow = 'YELLOW',
  Red = 'RED',
}

export interface TrackPoint {
  timestamp: string
  position: GeoPosition
  velocity: Velocity3D
}

export interface TrackedTarget {
  target_id: string
  track_id: string
  position: GeoPosition
  velocity: Velocity3D
  acceleration?: { an: number; ae: number; ad: number }
  classification: TargetClass
  threat_level: ThreatLevel
  confidence: number
  first_seen: string
  last_update: string
  track_history: TrackPoint[]
}

export interface ThreatAssessment {
  target_id: string
  intent: BehaviorType
  score: number
  level: ThreatLevel
  assessed_at: string
}

// ==================== 任务规划 ====================

export enum MissionStatus {
  Planning = 'PLANNING',
  Active = 'ACTIVE',
  Executing = 'EXECUTING',
  Completed = 'COMPLETED',
  Failed = 'FAILED',
  Cancelled = 'CANCELLED',
}

export enum ResourceType {
  ElectronicJammer = 'ELECTRONIC_JAMMER',
  HighPowerMicrowave = 'HIGH_POWER_MICROWAVE',
  KineticInterceptor = 'KINETIC_INTERCEPTOR',
  NetCapture = 'NET_CAPTURE',
  DecoyDrone = 'DECOY_DRONE',
}

export enum ResourceStatus {
  Available = 'AVAILABLE',
  Deployed = 'DEPLOYED',
  Recovering = 'RECOVERING',
  Maintenance = 'MAINTENANCE',
  Depleted = 'DEPLETED',
}

export interface Waypoint {
  position: GeoPosition
  arrival_time: string
  speed: number
  action: string
}

export interface InterceptMission {
  mission_id: string
  target_id: string
  assigned_interceptors: string[]
  trajectory: Waypoint[]
  priority: number
  status: MissionStatus
  created_at: string
  updated_at: string
}

export interface InterceptorResource {
  resource_id: string
  resource_type: ResourceType
  name: string
  position: GeoPosition
  status: ResourceStatus
  capabilities: string[]
  fuel_remaining: number
  max_range: number
}

// ==================== 态势 ====================

export interface SituationalAwareness {
  timestamp: string
  targets: TrackedTarget[]
  missions: InterceptMission[]
  sensors: SensorMetadata[]
  resources: InterceptorResource[]
}

export interface SystemHealth {
  cpu_usage: number
  memory_usage: number
  active_targets: number
  active_missions: number
  sensor_count: number
  uptime_seconds: number
}

// ==================== WebSocket 事件 ====================

export type WsEvent =
  | { event: 'TargetUpdated'; payload: TrackedTarget[] }
  | { event: 'ThreatAlert'; payload: TrackedTarget }
  | { event: 'MissionUpdate'; payload: InterceptMission }
  | { event: 'SensorStatus'; payload: SensorMetadata }
  | { event: 'SystemHealth'; payload: SystemHealth }
  | { event: 'ChatMessage'; payload: ChatMessage }

export interface ChatMessage {
  message_id: string
  sender: string
  content: string
  timestamp: string
  message_type: 'USER' | 'ASSISTANT' | 'COMMAND' | 'SYSTEM'
}

// ==================== 统一设备管理（传感器 + 反无设备） ====================

/** 设备来源分类 */
export type DeviceSource = 'hsimc2_sensor' | 'cuas_system'

/** 统一设备类型枚举 */
export enum UnifiedDeviceType {
  // HSimC2 传感器
  Radar = 'RADAR',
  Rf = 'RF',
  EoIr = 'EO_IR',
  Acoustic = 'ACOUSTIC',
  Lidar = 'LIDAR',

  // 反无人机系统设备（源自 API 文档 0x50 命令）
  CuasRadar = 'CUAS_RADAR',           // 0-主动雷达探测设备
  CuasRadioFreq = 'CUAS_RADIO_FREQ',  // 1-无线电侦测设备
  CuasOptical = 'CUAS_OPTICAL',       // 2-光电跟踪设备
  CuasInfrared = 'CUAS_INFRARED',     // 3-红外设备
  CuasJammer = 'CUAS_JAMMER',         // 4-干扰设备
  CuasSpoofer = 'CUAS_SPOOFER',       // 6-诱骗设备
  CuasControlBox = 'CUAS_CONTROL_BOX',// 8-智能控制箱
}

/** 设备通信模式（反无系统：1-Socket 2-串口 3-相机 4-WebAPI） */
export type CuasCommMode = 1 | 2 | 3 | 4

/** 统一设备信息 */
export interface DeviceInfo {
  device_id: string
  device_kind: UnifiedDeviceType
  name: string
  model: string
  source: DeviceSource

  latitude: number
  longitude: number
  altitude: number

  online: boolean
  status_label: string

  capabilities: string[]
  install_address?: string

  // 反无系统特有字段
  cuas_device_kind?: number
  cuas_comm_mode?: CuasCommMode
  cuas_ip?: string
  cuas_port?: number
  cuas_rtsp_url?: string
  cuas_ptz?: boolean
  cuas_pan_angle?: number
  cuas_tilt_angle?: number
  cuas_sensor_status?: number

  // HSimC2 传感器特有
  last_heartbeat?: string
  config?: any
}

/** 设备统计 */
export interface DeviceStats {
  total: number
  online: number
  offline: number
  by_type: Record<string, number>
}

// ==================== 模型管理（算法/模型全生命周期） ====================

/** 模型类别 */
export enum ModelCategory {
  SignalProcessing = 'SIGNAL_PROCESSING',       // 信号处理与预处理
  DetectionRecognition = 'DETECTION_RECOGNITION', // 检测与识别
  FusionAssociation = 'FUSION_ASSOCIATION',       // 融合与关联
  Classification = 'CLASSIFICATION',              // 分类与识别
  IntentThreat = 'INTENT_THREAT',                 // 意图与威胁评估
  PathPlanning = 'PATH_PLANNING',                 // 路径规划
  TaskScheduling = 'TASK_SCHEDULING',             // 任务分配与调度
  Simulation = 'SIMULATION',                      // 仿真与验证
}

/** 模型类型 */
export type ModelKind = 'algorithm' | 'model' | 'rule_engine'

/** 模型状态 */
export type ModelStatus = 'deployed' | 'testing' | 'development' | 'deprecated'

/** 模型参数定义 */
export interface ModelParameter {
  key: string
  label: string
  type: 'number' | 'boolean' | 'string' | 'select'
  default: any
  value: any
  options?: { label: string; value: any }[]  // for select type
  min?: number
  max?: number
  step?: number
  description?: string
}

/** 模型性能指标 */
export interface ModelMetrics {
  accuracy?: number     // 准确率
  precision?: number    // 精确率
  recall?: number       // 召回率
  latency_ms?: number   // 响应延迟(ms)
  throughput?: number   // 吞吐量
  f1_score?: number     // F1分数
}

/** 模型条目 */
export interface ModelEntry {
  model_id: string
  name: string
  name_en: string
  category: ModelCategory
  kind: ModelKind
  description: string
  status: ModelStatus
  version: string
  parameters: ModelParameter[]
  metrics: ModelMetrics
  // 依赖的其他模型
  dependencies: string[]
  // 适用的设备类型
  applicable_devices: string[]
  // 输入/输出说明
  input_desc: string
  output_desc: string
  // 参考文献
  references: string[]
}

/** 模型统计 */
export interface ModelStats {
  total: number
  by_category: Record<string, number>
  by_status: Record<string, number>
}

// ==================== 界面状态 ====================

export interface MapViewState {
  center: [number, number]
  zoom: number
  pitch: number
  selectedTargetId: string | null
  showHeatmap: boolean
  showTerrain: boolean
  layers: MapLayer[]
}

export interface MapLayer {
  id: string
  name: string
  visible: boolean
  type: 'target' | 'mission' | 'sensor' | 'no_fly' | 'threat'
}

// ==================== API 响应 ====================

export interface ApiResponse<T> {
  data: T
  error?: string
}

export interface ListResponse<T> {
  count: number
  items: T[]
}

// ==================== Data Management Types ====================

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  page_size: number
}

export interface ApiResponse<T> {
  success: boolean
  result?: T
  message?: string
}

// 设备配置
export interface DeviceConfig {
  id?: number
  name?: string
  device_id: string
  station_id: number
  device_type: string          // DETECT/TRAP/DISTURB/System
  device_ip: string
  device_port: number
  protocol_version?: string
  is_valid: number
  status?: string              // CONNECTED/DISCONNECTED/WARN
  update_time?: string
  jd?: number
  wd?: number
  gd?: number
  zcbj?: string
  dyqk?: string
  mac?: string
  xh?: string
  sccj?: string
  ccrq?: string
  type?: string                // UDP/TCPSocket/TCPServerSocket
  udp_port?: number
}

// 设备心跳
export interface DeviceHeartbeat {
  id?: number; station_id?: number; data_time?: string
  main_card?: number; trap_card?: number; compass?: number; disturb_card?: number
  longitude?: number; latitude?: number; altitude?: number; angle?: number
  cpu_rate?: number; disk_usage?: number; card_temp?: number; amp_temp?: number
  create_time?: string; work_state?: number
}

// 连接日志
export interface ConnectLog {
  id?: number; station_id: number; device_ip: string; device_port: number
  event_type: string; event_time?: string; reason?: string
}

// 操作日志
export interface OperateLog {
  id?: number; station_id?: number; device_type?: string
  cmd_type?: string; cmd_name?: string; cmd_param?: string
  result?: string; operate_time?: string
}

// 主动心跳
export interface ActiveHeartbeat {
  id?: number; start_code?: string; source_addr?: number; dest_addr?: number
  command?: number; param_length?: number; terminal_code?: string
  station_id?: number; data_time?: string
  auth_normal?: number; detect_enabled?: number; counter_enabled?: number
  detector_online?: number; counter_online?: number; unattended_mode?: number
  deceiver_online?: number; jamming_mode?: number
  band_58g?: number; band_24g?: number; band_900m?: number; band_14g?: number; band_52g?: number
  ptz_control_mode?: number; attack_countdown?: number; deception_status?: number
  gnss_link_status?: number; gnss_deception_mode?: number; gnss_induce_mode?: number
  no_fly_lat?: number; no_fly_lng?: number; no_fly_alt?: number
  forced_land_lat?: number; forced_land_lng?: number; forced_land_alt?: number
  forced_land_radius?: number; checksum?: number; create_time?: string; update_time?: string
}

// 侦测频谱
export interface DetectSpectrum {
  id?: number; station_id: number; model?: string; freq?: number
  rssi?: number; bandwidth?: number; data_time?: string; create_time?: string
}

// 无人机特征库
export interface DroneFeatureLib {
  id?: string; mc?: string; serial_number?: string; brand?: string; model?: string
  type?: string; dqfl?: string; zlfl?: string; jj?: string
  jc?: string; xtczy?: string; sx?: string; sysx?: string; zdsx?: string; yxsx?: string
  jg?: string; zzbj?: string; kzbj?: string; zcfw?: string; yz?: string; jz?: string
  rwzb?: string; yxzh?: string; zdsd?: string; zdqfzl?: string
  xhsd?: string; xhgd?: string; xhsj?: string; xhjl?: string; qdbj?: string; fdjsl?: string
  fxsd?: string; zz?: string; kz?: string; zdhc?: string; scdw?: string; qymc?: string
  fdj?: string; dlzz?: string; jzcl?: string; dmczry?: string; dwjd?: string; yxcsjl?: string
  cd?: string; wx?: string; hdfs?: string; zj?: string; nyzl?: string; hs?: string
  gzqs?: string; zcl?: string; hc?: string; dzpt?: string; dy?: string; dzsb?: string
  hldjnl?: string; ldjdzsb?: string; td?: string; jgtd?: string; zczb?: string
  zznl?: string; zcjsnl?: string; bpjbs?: string; remark?: string; tp?: string; mxdz?: string
  cjr?: string; cjrid?: string; cjsj?: string; czr?: string; czrid?: string; czsj?: string
  zdxhsd?: string; wrjxhsj?: string; tcnl?: string
}

// 无人机基本信息
export interface DroneBasicInfo {
  id?: string; serial_number?: string; brand?: string; model?: string
  status?: number; auth_status?: number
  current_longitude?: number; current_latitude?: number; current_altitude?: number
  last_seen_time?: string; remark?: string; rwlx?: string; ssdw?: string
  tp?: string; mxdz?: string
  cjr?: string; cjrid?: string; cjsj?: string; czr?: string; czrid?: string; czsj?: string
}

// 黑白名单
export interface BlackWhiteList {
  id?: string; mdlx?: string; wrjid?: string; sqsj?: string; sqgqsj?: string
  cjr?: string; cjrid?: string; cjsj?: string; czr?: string; czrid?: string; czsj?: string
}

// 告警记录
export interface AlarmRecord {
  id?: string; kymc?: string; kyid?: string; wrjid?: string
  wrjpp?: string; wrjxh?: string; wrjxlh?: string; gjlx?: string
  gjfsjd?: number; gjfsgd?: number; gjfswd?: number; gjfssj?: string
  clzt?: string; clsj?: string; clrid?: string; clr?: string; clbz?: string
  fsjd?: number; fswd?: number; zdid?: string; zdmc?: string; gjys?: string
  diff_seconds?: number; alarm_group_id?: number
  cjr?: string; cjrid?: string; cjsj?: string; czr?: string; czrid?: string; czsj?: string
}

// 空域
export interface Airspace {
  id?: string; mc?: string; lx?: string; xz?: string
  zxdjd?: number; zxdwd?: number; bj?: number; zxgd?: number; zdgd?: number
  ddzbjh?: string; cd?: number; kd?: number; kssj?: string; jssj?: string
  sfqy?: number; bz?: string; ys?: string
  jfqbj?: number; jfqys?: string; yjqbj?: number; yjqys?: string
  cjr?: string; cjrid?: string; cjsj?: string; czr?: string; czrid?: string; czsj?: string
}

// 飞行数据 - 报文
export interface DroneDetectMsg {
  id?: number; station_id: number; serial?: string; model?: string
  dron_lng?: number; dron_lat?: number; home_lng?: number; home_lat?: number
  pilot_lng?: number; pilot_lat?: number; altitude?: number; height?: number
  east_v?: number; north_v?: number; up_v?: number; freq?: number; rssi?: number
  distance?: number; uuid?: string; angle?: number; data_time?: string; create_time?: string
  sd?: number; mac?: string; jmlx?: string; data_type?: number; fused_flag?: number
}

// 飞行数据 - 解析
export interface DroneDfData {
  id?: number; station_id?: number; data_time?: string
  target_type?: number; detect_type?: number; freq?: number; dk?: number
  longitude?: number; latitude?: number; angle?: number; signal_level?: number
  compass?: number; distance?: number; uav_model?: string; uav_id?: string
  device_id?: string; create_time?: string; speed?: number; height?: number
}

// 飞行数据 - Remote
export interface DroneRemoteData {
  id?: string; ris_ssid?: string; serial?: string; model?: string; ua_type?: string
  dron_lng?: number; dron_lat?: number; pilot_lng?: number; pilot_lat?: number
  speed?: number; vspeed?: number; direc?: number; altitudep?: number; altitudeg?: number
  height_agl?: number; mac?: string; rssi?: number; freq?: string; angle?: number
  distance?: number; date?: string; station_id?: number
}

// ==================== wxdzc 侦测预警专用类型 ====================

/** 按站点分组的无人机侦测消息 */
export interface UavDetectMsgVo {
  model?: string
  serial?: string
  brand?: string
  status?: number           // 1=正常 2=告警 3=失联
  auth_status?: number      // 1=白名单 2=黑名单 3=未授权 4=待定
  station_id?: number
  station_name?: string
  wxdj?: string             // red/yellow/green
  jmlx?: string
  // Flattened DroneDetectMsg fields
  id?: number
  dron_lng?: number
  dron_lat?: number
  home_lng?: number
  home_lat?: number
  pilot_lng?: number
  pilot_lat?: number
  altitude?: number
  height?: number
  east_v?: number
  north_v?: number
  up_v?: number
  freq?: number
  rssi?: number
  distance?: number
  uuid?: string
  angle?: number
  data_time?: string
  create_time?: string
  sd?: number
  mac?: string
  data_type?: number
  fused_flag?: number
}

/** 按型号序列号查询的侦测消息 */
export interface UavDetectMsgVo1 {
  uav_detect_msg_list: DroneDetectMsg[]
  wjbd_wrj_jbxx?: DroneBasicInfo | null
}

/** 日期日历数据 */
export interface UavDetectMsgDateVo {
  rq: string
  type: string
}

/** 飞行路线生成请求 */
export interface FlightRouteRequest {
  station_id?: number
  model?: string
  serial?: string
  rq?: string
  start?: any
  end?: any
  waypoints?: any[]
}
