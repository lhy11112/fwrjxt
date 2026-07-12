/**
 * wxdzc 侦测预警 API 客户端
 * 对应 Java 后端 wxdzc.js 的 5 个端点
 */
import api from './index'
import type {
  PageResult,
  DeviceConfig,
  UavDetectMsgVo,
  UavDetectMsgVo1,
  UavDetectMsgDateVo,
  FlightRouteRequest,
} from '@/types'

/** 设备列表（分页 + 名称搜索） */
export async function listDevices(params: {
  page?: number
  page_size?: number
  name?: string
}): Promise<PageResult<DeviceConfig>> {
  const { data } = await api.get('/wxdzc/devices/list', { params })
  return data.result
}

/** 按站点分组获取最新侦测消息 */
export async function getByStation(params: {
  rq?: string
  auth_status?: number
}): Promise<UavDetectMsgVo[]> {
  const { data } = await api.get('/wxdzc/detect-msg/by-station', { params })
  return data.result
}

/** 按型号/序列号查询飞行路径 */
export async function getByModelSerial(params: {
  model?: string
  serial?: string
  rq?: string
}): Promise<UavDetectMsgVo1> {
  const { data } = await api.get('/wxdzc/detect-msg/by-model-serial', { params })
  return data.result
}

/** 获取日期日历（有数据的日期） */
export async function getDateCalendar(params: {
  nf?: string
  yf?: string
}): Promise<UavDetectMsgDateVo[]> {
  const { data } = await api.get('/wxdzc/detect-msg/date-calendar', { params })
  return data.result
}

/** 生成飞行路线 */
export async function generateFlightRoute(
  body: FlightRouteRequest,
): Promise<string> {
  const { data } = await api.post('/wxdzc/flight-route/generate', body)
  return data.result
}
