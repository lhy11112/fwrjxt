/**
 * wxdzc 侦测预警 工具函数
 * 移植自源项目 window.TOOL 和全局函数
 */

/** 日期格式化 */
export function dateFormat(date: Date | string, fmt: string): string {
  const d = typeof date === 'string' ? new Date(date) : date
  const o: Record<string, number> = {
    'M+': d.getMonth() + 1,
    'd+': d.getDate(),
    'H+': d.getHours(),
    'm+': d.getMinutes(),
    's+': d.getSeconds(),
    'q+': Math.floor((d.getMonth() + 3) / 3),
    S: d.getMilliseconds(),
  }
  let result = fmt
  if (/(y+)/.test(result)) {
    result = result.replace(RegExp.$1, (d.getFullYear() + '').substring(4 - RegExp.$1.length))
  }
  for (const k in o) {
    if (new RegExp('(' + k + ')').test(result)) {
      result = result.replace(RegExp.$1, RegExp.$1.length === 1 ? o[k] + '' : ('00' + o[k]).slice(-('' + o[k]).length))
    }
  }
  return result
}

/** 度转弧度 */
function toRad(deg: number): number {
  return (deg * Math.PI) / 180
}

/** 弧度转度 */
function toDeg(rad: number): number {
  return (rad * 180) / Math.PI
}

/** Haversine 公式：根据起点、方位角、距离计算目标点坐标 */
export function getDestinationPoint(
  latlng: [number, number],
  bearing: number,
  distance: number,
): [number, number] {
  const R = 6371000 // 地球半径（米）
  const lat1 = toRad(latlng[0])
  const lng1 = toRad(latlng[1])
  const brng = toRad(bearing)

  const lat2 = Math.asin(
    Math.sin(lat1) * Math.cos(distance / R) +
      Math.cos(lat1) * Math.sin(distance / R) * Math.cos(brng),
  )
  const lng2 =
    lng1 +
    Math.atan2(
      Math.sin(brng) * Math.sin(distance / R) * Math.cos(lat1),
      Math.cos(distance / R) - Math.sin(lat1) * Math.sin(lat2),
    )

  return [toDeg(lat2), toDeg(lng2)]
}

/** 生成圆形边界点（用于 Leaflet polygon） */
export function getCirclePoints(
  center: [number, number],
  radius: number,
  numPoints = 360,
): [number, number][] {
  const points: [number, number][] = []
  for (let i = 0; i < numPoints; i++) {
    points.push(getDestinationPoint(center, i, radius))
  }
  return points
}

/** 生成扇形边界点 */
export function createSectorPoints(
  center: [number, number],
  radius: number,
  direction: number,
  spread: number,
): [number, number][] {
  const points: [number, number][] = [center] // include center for polygon
  const startAngle = direction - spread / 2
  const endAngle = direction + spread / 2
  const step = 2 // 2-degree steps
  for (let a = startAngle; a <= endAngle; a += step) {
    points.push(getDestinationPoint(center, a, radius))
  }
  return points
}

/** 秒数格式化：X小时XX分钟XX秒 */
export function formatSecondsToHMS(totalSeconds: number): string {
  if (totalSeconds < 0) return '0秒'
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = Math.floor(totalSeconds % 60)
  if (hours > 0) {
    return `${hours}小时${minutes}分钟${seconds}秒`
  }
  if (minutes > 0) {
    return `${minutes}分钟${seconds}秒`
  }
  return `${seconds}秒`
}

/** 客户端威胁等级计算（与 Rust 后端公式一致） */
export function computeThreatLevel(
  sd: number | undefined | null,
  distance: number | undefined | null,
  height: number | undefined | null,
): 'red' | 'yellow' | 'green' {
  const sdVal = clamp(sd ?? 0, 0, 150)
  const distVal = clamp(distance ?? 0, -100, 1000)
  const heightVal = clamp(height ?? 0, 0, 10000)

  const speedScore = 1 - Math.exp(-0.005 * sdVal)
  const distScore = 1 / (1 + Math.exp(0.02 * (distVal - 30)))
  const heightScore = 1 / (1 + Math.exp(0.005 * (heightVal - 120)))

  const total = 0.4 * speedScore + 0.3 * distScore + 0.3 * heightScore
  if (total >= 0.7) return 'red'
  if (total >= 0.4) return 'yellow'
  return 'green'
}

function clamp(val: number, min: number, max: number): number {
  return Math.max(min, Math.min(max, val))
}

/** 计算两点距离（Haversine，单位：米） */
export function haversineDistance(
  latlng1: [number, number],
  latlng2: [number, number],
): number {
  const R = 6371000
  const dLat = toRad(latlng2[0] - latlng1[0])
  const dLng = toRad(latlng2[1] - latlng1[1])
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(latlng1[0])) *
      Math.cos(toRad(latlng2[0])) *
      Math.sin(dLng / 2) *
      Math.sin(dLng / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return R * c
}
