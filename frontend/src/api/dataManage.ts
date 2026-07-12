import axios from 'axios'
import type {
  DeviceConfig, DeviceHeartbeat, ConnectLog, OperateLog, ActiveHeartbeat,
  DetectSpectrum, DroneFeatureLib, DroneBasicInfo, BlackWhiteList, AlarmRecord, Airspace,
  DroneDetectMsg, DroneDfData, DroneRemoteData,
} from '@/types'

// 后端统一响应格式: { success: bool, result: T, message?: string }
interface ApiResponse<T> { success: boolean; result?: T; message?: string }
interface PageResult<T> { records: T[]; total: number; page: number; page_size: number }

const api = axios.create({ baseURL: '/api/v1', timeout: 15000, headers: { 'Content-Type': 'application/json' } })

// 辅助: 自动解包 ApiResponse, 返回 result 或 throw
async function unwrap<T>(req: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  const { data } = await req
  if (!data.success) throw new Error(data.message || 'Request failed')
  return data.result as T
}

// ==================== 设备管理 ====================
export const deviceApi = {
  list: (params?: any) => unwrap<PageResult<DeviceConfig>>(api.get('/devices', { params })),
  add: (d: DeviceConfig) => unwrap<DeviceConfig>(api.post('/devices', d)),
  edit: (d: DeviceConfig) => unwrap<DeviceConfig>(api.put('/devices', d)),
  delete: (id: number) => api.delete(`/devices/${id}`),
  batchDelete: (ids: number[]) => api.post('/devices/batch-delete', ids),
  getById: (id: number) => unwrap<DeviceConfig>(api.get(`/devices/${id}`)),
  stats: () => api.get('/devices/stats'),
  heartbeats: (params?: any) => unwrap<PageResult<DeviceHeartbeat>>(api.get('/devices/heartbeats', { params })),
  connectLogs: (params?: any) => unwrap<PageResult<ConnectLog>>(api.get('/devices/connect-logs', { params })),
  operateLogs: (params?: any) => unwrap<PageResult<OperateLog>>(api.get('/devices/operate-logs', { params })),
  activeHeartbeats: (params?: any) => unwrap<PageResult<ActiveHeartbeat>>(api.get('/devices/active-heartbeats', { params })),
}

// ==================== 无人机特征库 ====================
export const droneLibraryApi = {
  list: (params?: any) => unwrap<PageResult<DroneFeatureLib>>(api.get('/drones/library', { params })),
  add: (d: DroneFeatureLib) => unwrap<DroneFeatureLib>(api.post('/drones/library', d)),
  edit: (d: DroneFeatureLib) => unwrap<DroneFeatureLib>(api.put('/drones/library', d)),
  delete: (id: string) => api.delete(`/drones/library/${id}`),
  batchDelete: (ids: string[]) => api.post('/drones/library/batch-delete', ids),
  getById: (id: string) => unwrap<DroneFeatureLib>(api.get(`/drones/library/${id}`)),
}

// ==================== 无人机基本信息 ====================
export const droneBasicApi = {
  list: (params?: any) => unwrap<PageResult<DroneBasicInfo>>(api.get('/drones/basic', { params })),
  add: (d: DroneBasicInfo) => unwrap<DroneBasicInfo>(api.post('/drones/basic', d)),
  edit: (d: DroneBasicInfo) => unwrap<DroneBasicInfo>(api.put('/drones/basic', d)),
  editAuth: (id: string, auth_status: number) => api.put('/drones/basic/edit-auth', { id, auth_status }),
  delete: (id: string) => api.delete(`/drones/basic/${id}`),
  batchDelete: (ids: string[]) => api.post('/drones/basic/batch-delete', ids),
  getById: (id: string) => unwrap<DroneBasicInfo>(api.get(`/drones/basic/${id}`)),
  stats: () => api.get('/drones/basic/stats'),
}

// ==================== 黑白名单 ====================
export const bwListApi = {
  list: (params?: any) => unwrap<PageResult<BlackWhiteList>>(api.get('/drones/bwlist', { params })),
  grouped: () => unwrap<{ '白名单': BlackWhiteList[]; '黑名单': BlackWhiteList[] }>(api.get('/drones/bwlist/grouped')),
  add: (d: BlackWhiteList) => unwrap<BlackWhiteList>(api.post('/drones/bwlist', d)),
  editBatchAuth: (wrjids: string[], mdlx: string) => api.put('/drones/bwlist/edit-auth', { wrjids, mdlx }),
  delete: (id: string) => api.delete(`/drones/bwlist/${id}`),
  batchDelete: (ids: string[]) => api.post('/drones/bwlist/batch-delete', ids),
}

// ==================== 告警管理 ====================
export const alarmApi = {
  list: (params?: any) => unwrap<PageResult<AlarmRecord>>(api.get('/alarms', { params })),
  add: (d: AlarmRecord) => unwrap<AlarmRecord>(api.post('/alarms', d)),
  edit: (d: AlarmRecord) => unwrap<AlarmRecord>(api.put('/alarms', d)),
  delete: (id: string) => api.delete(`/alarms/${id}`),
  batchDelete: (ids: string[]) => api.post('/alarms/batch-delete', ids),
  getById: (id: string) => unwrap<AlarmRecord>(api.get(`/alarms/${id}`)),
  stats: (params?: any) => api.get('/alarms/stats', { params }),
}

// ==================== 空域管理 ====================
export const airspaceApi = {
  list: (params?: any) => unwrap<PageResult<Airspace>>(api.get('/airspace', { params })),
  add: (d: Airspace) => unwrap<Airspace>(api.post('/airspace', d)),
  edit: (d: Airspace) => unwrap<Airspace>(api.put('/airspace', d)),
  delete: (id: string) => api.delete(`/airspace/${id}`),
  batchDelete: (ids: string[]) => api.post('/airspace/batch-delete', ids),
  getById: (id: string) => unwrap<Airspace>(api.get(`/airspace/${id}`)),
}

// ==================== 飞行数据 ====================
export const flightDataApi = {
  detectMsgs: (params?: any) => unwrap<PageResult<DroneDetectMsg>>(api.get('/flight-data/detect-msg', { params })),
  getDetectMsg: (id: number) => unwrap<DroneDetectMsg>(api.get(`/flight-data/detect-msg/${id}`)),
  dfData: (params?: any) => unwrap<PageResult<DroneDfData>>(api.get('/flight-data/df-data', { params })),
  remoteData: (params?: any) => unwrap<PageResult<DroneRemoteData>>(api.get('/flight-data/remote', { params })),
}

// ==================== 频谱数据 ====================
export const spectrumApi = {
  list: (params?: any) => unwrap<PageResult<DetectSpectrum>>(api.get('/spectrum', { params })),
  today: (params?: any) => unwrap<PageResult<DetectSpectrum>>(api.get('/spectrum/today', { params })),
}

export default { deviceApi, droneLibraryApi, droneBasicApi, bwListApi, alarmApi, airspaceApi, flightDataApi, spectrumApi }
