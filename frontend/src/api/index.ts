import axios from 'axios'
import type {
  TrackedTarget,
  SensorMetadata,
  InterceptMission,
  InterceptorResource,
  ThreatAssessment,
  SystemHealth,
  ChatMessage,
} from '@/types'

const api = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// ==================== 系统 ====================

export async function healthCheck() {
  const { data } = await api.get('/health')
  return data
}

export async function getSystemMetrics() {
  const { data } = await api.get('/metrics')
  return data as {
    total_frames: number
    active_tracks: number
    active_missions: number
    sensors_online: number
    avg_fusion_latency_ms: number
  }
}

// ==================== 传感器 ====================

export async function listSensors() {
  const { data } = await api.get('/sensors')
  return data as SensorMetadata[]
}

export async function getSensor(id: string) {
  const { data } = await api.get(`/sensors/${id}`)
  return data as SensorMetadata
}

// ==================== 态势 ====================

export async function getTargets() {
  const { data } = await api.get('/situation/targets')
  return data as { count: number; targets: TrackedTarget[] }
}

export async function getThreats() {
  const { data } = await api.get('/situation/threats')
  return data as { count: number; threats: TrackedTarget[] }
}

export async function getTrackHistory(id: string) {
  const { data } = await api.get(`/situation/history/${id}`)
  return data
}

// ==================== 任务 ====================

export async function listMissions() {
  const { data } = await api.get('/missions')
  return data as InterceptMission[]
}

export async function createMission(params: {
  target_id: string
  interceptor_ids: string[]
  priority?: number
}) {
  const { data } = await api.post('/missions', params)
  return data as InterceptMission
}

export async function planIntercept(params: {
  target_id: string
  interceptor_id: string
}) {
  const { data } = await api.post('/missions/plan', params)
  return data
}

export async function planMultiIntercept(params: {
  target_ids: string[]
  interceptor_ids: string[]
}) {
  const { data } = await api.post('/missions/plan-multi', params)
  return data
}

export async function cancelMission(id: string) {
  const { data } = await api.post(`/missions/${id}/cancel`)
  return data
}

// ==================== 资源 ====================

export async function listResources() {
  const { data } = await api.get('/resources')
  return data as InterceptorResource[]
}

// ==================== 对话 ====================

export async function sendChatMessage(message: string, context?: any) {
  const { data } = await api.post('/chat', { message, context })
  return data as { reply: string; actions: any[] }
}

// ==================== WebSocket ====================

export function createWebSocket(): WebSocket {
  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const host = window.location.host
  const ws = new WebSocket(`${protocol}//${host}/api/v1/ws`)

  ws.onopen = () => console.log('[HSimC2] WebSocket connected')
  ws.onclose = () => {
    console.log('[HSimC2] WebSocket disconnected, reconnecting in 3s...')
    setTimeout(() => createWebSocket(), 3000)
  }
  ws.onerror = (err) => console.error('[HSimC2] WebSocket error', err)

  return ws
}

// ==================== 仿真引擎 ====================

export interface SimulatorStatus {
  running: boolean
  target_count: number
  frame_count: number
  config: {
    update_interval_ms: number
    num_targets: number
    area_radius_km: number
    center_lat: number
    center_lon: number
  }
}

export interface SimTargetInfo {
  target_id: string
  classification: string
  latitude: number
  longitude: number
  altitude: number
  speed: number
  heading: number
  behavior: string
  threat_level: string
  active: boolean
}

export async function getSimulatorStatus() {
  const { data } = await api.get('/simulator')
  return data as SimulatorStatus
}

export async function getSimulatorTargets() {
  const { data } = await api.get('/simulator/targets')
  return data as SimTargetInfo[]
}

export async function restartSimulator() {
  const { data } = await api.post('/simulator/restart')
  return data
}

// ==================== 设备管理（传感器 + 反无设备） ====================

import type { DeviceInfo, DeviceSource } from '@/types'
import { UnifiedDeviceType } from '@/types'

/** 模拟的反无人机系统设备列表数据（基于 API 文档 0x50 命令） */
const CUAS_MOCK_DEVICES: DeviceInfo[] = [
  {
    device_id: 'cuas-rd-001', device_kind: UnifiedDeviceType.CuasRadar,
    name: '主动雷达探测设备', model: 'CUAS-RDR-A1',
    source: 'cuas_system',
    latitude: 34.082, longitude: 108.942, altitude: 85,
    online: true, status_label: '已展开',
    capabilities: ['3D跟踪', '脉冲多普勒', '目标检测'],
    install_address: '西安秦岭北侧阵地1',
    cuas_device_kind: 0, cuas_comm_mode: 1,
    cuas_ip: '192.168.175.41', cuas_port: 8101,
  },
  {
    device_id: 'cuas-rf-001', device_kind: UnifiedDeviceType.CuasRadioFreq,
    name: '无线电侦测设备', model: 'CUAS-RF-S1',
    source: 'cuas_system',
    latitude: 34.081, longitude: 108.941, altitude: 82,
    online: true, status_label: '已展开',
    capabilities: ['频谱分析', '协议识别', '测向定位'],
    install_address: '西安秦岭北侧阵地1',
    cuas_device_kind: 1, cuas_comm_mode: 1,
    cuas_ip: '192.168.175.49', cuas_port: 8100,
  },
  {
    device_id: 'cuas-eo-001', device_kind: UnifiedDeviceType.CuasOptical,
    name: '光电跟踪设备', model: 'CUAS-EO-P1',
    source: 'cuas_system',
    latitude: 34.080, longitude: 108.940, altitude: 80,
    online: true, status_label: '已展开',
    capabilities: ['可见光跟踪', '激光测距', '自动聚焦'],
    install_address: '西安秦岭北侧阵地1',
    cuas_device_kind: 2, cuas_comm_mode: 3,
    cuas_ip: '192.168.175.50', cuas_port: 8000,
    cuas_rtsp_url: 'rtsp://192.168.175.50:554/stream1',
    cuas_ptz: true, cuas_pan_angle: 45.0, cuas_tilt_angle: -15.0,
  },
  {
    device_id: 'cuas-ir-001', device_kind: UnifiedDeviceType.CuasInfrared,
    name: '红外热像设备', model: 'CUAS-IR-T1',
    source: 'cuas_system',
    latitude: 34.083, longitude: 108.943, altitude: 85,
    online: false, status_label: '已收拢',
    capabilities: ['红外成像', '热源追踪', '夜间探测'],
    install_address: '西安秦岭北侧阵地2',
    cuas_device_kind: 3, cuas_comm_mode: 3,
    cuas_ip: '192.168.175.52', cuas_port: 8001,
    cuas_ptz: true,
  },
  {
    device_id: 'cuas-jam-001', device_kind: UnifiedDeviceType.CuasJammer,
    name: '多频段干扰设备', model: 'CUAS-JAM-M1',
    source: 'cuas_system',
    latitude: 34.078, longitude: 108.938, altitude: 78,
    online: false, status_label: '待命',
    capabilities: ['2.4G干扰', '5.8G干扰', '900M干扰', '1.4G干扰'],
    install_address: '西安秦岭北侧阵地1',
    cuas_device_kind: 4, cuas_comm_mode: 1,
    cuas_ip: '192.168.175.51', cuas_port: 55566,
  },
  {
    device_id: 'cuas-gnss-001', device_kind: UnifiedDeviceType.CuasSpoofer,
    name: 'GNSS诱骗设备', model: 'CUAS-GNS-G1',
    source: 'cuas_system',
    latitude: 34.079, longitude: 108.939, altitude: 79,
    online: true, status_label: '待命',
    capabilities: ['定向驱逐', '定点迫降', '禁飞区', '导航压制'],
    install_address: '西安秦岭北侧阵地1',
    cuas_device_kind: 6, cuas_comm_mode: 1,
    cuas_ip: '192.168.175.53', cuas_port: 55567,
    cuas_pan_angle: 90.0, cuas_tilt_angle: 30.0,
  },
  {
    device_id: 'cuas-box-001', device_kind: UnifiedDeviceType.CuasControlBox,
    name: '智能控制箱', model: 'CUAS-BOX-C1',
    source: 'cuas_system',
    latitude: 34.080, longitude: 108.940, altitude: 80,
    online: true, status_label: '已展开',
    capabilities: ['设备供电', '通信中继', '环境监控'],
    install_address: '西安秦岭北侧阵地1',
    cuas_device_kind: 8, cuas_comm_mode: 2,
  },
]

/** 获取所有设备列表（HSimC2 传感器 + 反无设备） */
export async function listAllDevices(): Promise<DeviceInfo[]> {
  try {
    // 获取后端传感器列表
    const sensors = await listSensors()
    const hsimc2Devices: DeviceInfo[] = sensors.map(s => ({
      device_id: s.sensor_id,
      device_kind: s.sensor_type as unknown as UnifiedDeviceType,
      name: s.name,
      model: s.model,
      source: 'hsimc2_sensor' as DeviceSource,
      latitude: s.location.latitude,
      longitude: s.location.longitude,
      altitude: s.location.altitude,
      online: s.status === 'ONLINE',
      status_label: s.status === 'ONLINE' ? '在线' : s.status === 'OFFLINE' ? '离线' : '降级',
      capabilities: s.capabilities,
      last_heartbeat: s.last_heartbeat,
      config: s.config,
    }))

    // 合并反无系统设备
    return [...hsimc2Devices, ...CUAS_MOCK_DEVICES]
  } catch {
    // 后端不可用时只返回反无系统设备
    return CUAS_MOCK_DEVICES
  }
}

export default api
