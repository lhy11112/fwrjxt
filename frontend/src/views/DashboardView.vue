<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useSituationStore } from '@/stores/situation'
import { getTargets } from '@/api'
import { deviceApi } from '@/api/dataManage'
import type { DeviceConfig } from '@/types'
// Cesium 通过 index.html 的 CDN script 标签注入到全局
// Cesium 在运行时通过 window.Cesium 获取
let Cesium: any = null

const situation = useSituationStore()
const mapContainer = ref<HTMLDivElement>()
const loading = ref(true)

// 导出重置视角函数供 App.vue 顶部按钮调用
if (typeof window !== 'undefined') {
  (window as any).__hsimc2_resetView = () => flyToAllTargets()
}

let viewer: any = null
let entityCollection: any = null
let pollTimer: number | undefined
let devicePollTimer: number | undefined
let entityUpdateTimer: number | undefined
let deviceEntities: any[] = [] // 设备标记集合
let postRenderRemove: (() => void) | null = null
let activeImageryLayer: any = null
let _lastEntityUpdate = 0

// ---- 初始化 ----
onMounted(async () => {
  // 等待 Cesium 脚本加载（本地文件，最多等1秒）
  let retries = 0
  while (!(window as any).Cesium && retries < 5) {
    await new Promise(r => setTimeout(r, 200))
    retries++
  }
  if (!(window as any).Cesium) {
    loading.value = false
    console.error('Cesium 引擎加载失败')
    return
  }
  Cesium = (window as any).Cesium

  try {
    await initCesium()
  } catch (e) {
    console.error('Cesium 初始化失败:', e)
    loading.value = false
    return
  }
  loading.value = false


  // 加载地图配置
  await loadMapConfig()

  // 自动飞往配置中心点
  setTimeout(() => {
    if (viewer) {
      const [clat, clon] = mapConfig.value.center
      viewer.camera.flyTo({
        destination: Cesium.Cartesian3.fromDegrees(clon, clat, 12000),
        orientation: {
          heading: Cesium.Math.toRadians(0),
          pitch: Cesium.Math.toRadians(-35),
          roll: 0,
        },
        duration: 3,
      })
    }
  }, 800)

  pollTimer = window.setInterval(pollTargets, 1500)
  devicePollTimer = window.setInterval(pollDevices, 6000)
  pollDevices()
  // 使用 Cesium scene.postRender 事件驱动实体更新
  // postRender 在每个渲染帧完成之后触发，确保更新不会与渲染循环冲突
  // 这是 Cesium 官方推荐的实体更新方式，彻底避免 race condition
  const removePostRender = viewer.scene.postRender.addEventListener(() => {
    // 诱因3防御：对象已销毁则跳过
    if (!entityCollection || entityCollection.isDestroyed?.()) return
    if (!viewer || viewer.isDestroyed?.()) return
    // 节流：最多每 800ms 更新一次
    const now = Date.now()
    if (now - _lastEntityUpdate < 800) return
    _lastEntityUpdate = now
    updateEntities()
  })
  postRenderRemove = removePostRender
})
onUnmounted(() => {
  clearInterval(pollTimer)
  clearInterval(devicePollTimer)
  if (postRenderRemove) { postRenderRemove(); postRenderRemove = null }
  if (viewer && viewer.destroy) viewer.destroy()
})

async function pollTargets() {
  try { const r = await getTargets(); if (r.targets.length) situation.updateTargets(r.targets) } catch {}
}

// ---- 设备标记 ----
let devices: DeviceConfig[] = []
async function pollDevices() {
  try {
    const data = await deviceApi.list({ page: 1, page_size: 50 })
    devices = data.records || []
    updateDeviceMarkers()
  } catch {}
}

function createDeviceIcon(deviceType: string): string {
  const c = document.createElement('canvas'); c.width = 48; c.height = 48
  const ctx = c.getContext('2d')!
  // 雷达/侦测设备图标
  const colors: Record<string, string> = { DETECT: '#4db4ff', TRAP: '#ffb74d', DISTURB: '#ef5350', System: '#81c784' }
  const color = colors[deviceType] || '#90a4ae'
  // 圆形底座
  ctx.fillStyle = color; ctx.beginPath(); ctx.arc(24, 24, 16, 0, Math.PI * 2); ctx.fill()
  ctx.strokeStyle = 'white'; ctx.lineWidth = 2; ctx.stroke()
  // 天线/标志
  ctx.fillStyle = 'white'; ctx.beginPath(); ctx.arc(24, 18, 5, 0, Math.PI * 2); ctx.fill()
  ctx.strokeStyle = 'white'; ctx.lineWidth = 2.5
  ctx.beginPath(); ctx.moveTo(24, 13); ctx.lineTo(24, 4); ctx.stroke()
  // 信号波纹
  ctx.strokeStyle = color; ctx.lineWidth = 1.5
  ctx.beginPath(); ctx.arc(24, 18, 10, -Math.PI * 0.7, -Math.PI * 0.3); ctx.stroke()
  ctx.beginPath(); ctx.arc(24, 18, 13, -Math.PI * 0.75, -Math.PI * 0.25); ctx.stroke()
  return c.toDataURL('image/png')
}

function updateDeviceMarkers() {
  if (!viewer || !entityCollection) return
  const cs = entityCollection.entities
  // 清除旧设备标记
  deviceEntities.forEach(e => { if (!e.isDestroyed?.()) cs.remove(e) })
  deviceEntities = []

  devices.filter(d => d.status === 'CONNECTED' && d.jd && d.wd).forEach(d => {
    if (!d.jd || !d.wd) return
    const lon = d.jd, lat = d.wd, radius = parseFloat(d.zcbj || '5') * 1000
    const prefix = `device-${d.device_id}`

    // 设备位置标记
    const icon = createDeviceIcon(d.device_type)
    const billEnt = cs.add({
      id: `${prefix}-marker`,
      position: Cesium.Cartesian3.fromDegrees(lon, lat, d.gd || 10),
      billboard: { image: icon, scale: 0.9, verticalOrigin: Cesium.VerticalOrigin.CENTER, horizontalOrigin: Cesium.HorizontalOrigin.CENTER, heightReference: Cesium.HeightReference.RELATIVE_TO_GROUND, disableDepthTestDistance: 50000 },
    })
    // 设备名称标签
    const labelEnt = cs.add({
      id: `${prefix}-label`,
      position: Cesium.Cartesian3.fromDegrees(lon, lat, (d.gd || 10) + 5),
      label: { text: d.name || d.device_id, font: '11px "Microsoft YaHei", sans-serif', fillColor: Cesium.Color.WHITE, outlineColor: Cesium.Color.fromCssColorString('#001529').withAlpha(0.5), outlineWidth: 1, showBackground: true, backgroundColor: Cesium.Color.fromCssColorString('#0f6cbd').withAlpha(0.85), backgroundPadding: new Cesium.Cartesian2(6, 4), pixelOffset: new Cesium.Cartesian2(0, -24), verticalOrigin: Cesium.VerticalOrigin.BOTTOM, horizontalOrigin: Cesium.HorizontalOrigin.CENTER, disableDepthTestDistance: 50000 },
    })
    // 侦测半径圆（雷达扫描圈）
    const circleEnt = cs.add({
      id: `${prefix}-radius`,
      position: Cesium.Cartesian3.fromDegrees(lon, lat, 1),
      ellipse: { semiMinorAxis: radius, semiMajorAxis: radius, material: Cesium.Color.fromCssColorString('#4db4ff').withAlpha(0.06), outline: true, outlineColor: Cesium.Color.fromCssColorString('#4db4ff').withAlpha(0.3), outlineWidth: 1, heightReference: Cesium.HeightReference.NONE },
    })
    deviceEntities.push(billEnt, labelEnt, circleEnt)
  })
}

// ---- Cesium 初始化 ----
async function initCesium() {
  if (!mapContainer.value || !Cesium) return

  try {
    // 使用 Cesium 内置的 EllipsoidTerrainProvider（最小稳定地形）
    const terrain = new Cesium.EllipsoidTerrainProvider()

    viewer = new Cesium.Viewer(mapContainer.value, {
      baseLayer: false,
      animation: false, timeline: false, fullscreenButton: false,
      baseLayerPicker: false, geocoder: false, homeButton: false,
      sceneModePicker: false, navigationHelpButton: false,
      navigationInstructionsInitiallyVisible: false,
      infoBox: false, selectionIndicator: false,
      terrainProvider: terrain,
      shadows: false,
      backgroundColor: Cesium.Color.fromCssColorString('#060b18'),
      orderIndependentTranslucency: false,
      scene3DOnly: true,
      msaa: 4,
    })

    // 手动添加底图（参考 JmisMap 组件模式）
    viewer.imageryLayers.addImageryProvider(createImageryProvider('dark'))

    const scene = viewer.scene
    scene.globe.baseColor = Cesium.Color.fromCssColorString('#0a1628')
    scene.globe.enableLighting = false
    scene.globe.depthTestAgainstTerrain = false
    scene.globe.showGroundAtmosphere = true
    scene.globe.atmosphereBrightnessShift = 0.3
    scene.highDynamicRange = false
    scene.backgroundColor = Cesium.Color.fromCssColorString('#060b18')
    scene.skyAtmosphere.show = true
    scene.skyAtmosphere.brightnessShift = 0.3
    scene.fog.enabled = false
    scene.moon.show = false
    scene.skyBox.show = false
    scene.sun.show = false
    scene.postProcessStages.fxaa.enabled = false
    if (scene.postProcessStages.ambientOcclusion) scene.postProcessStages.ambientOcclusion.enabled = false
    if (scene.postProcessStages.bloom) scene.postProcessStages.bloom.enabled = false

  // 默认视角 — 西安秦岭北侧（反无人机攻防演练区域）
  // 中心: 34.08°N, 108.94°E | 覆盖约 15km × 15km 范围
  viewer.camera.setView({
    destination: Cesium.Cartesian3.fromDegrees(mapConfig.value.center[1], mapConfig.value.center[0], 12000),
    orientation: {
      heading: Cesium.Math.toRadians(0),
      pitch: Cesium.Math.toRadians(-35),
      roll: 0,
    },
  })

  // 实体集合（用于目标标记）
  entityCollection = new Cesium.CustomDataSource('targets')
  viewer.dataSources.add(entityCollection)

  // 注册渲染错误监听（按排查指南-诱因3：定位崩溃的实体/图元数据）
  viewer.scene.renderError.addEventListener((scene: any, err: any) => {
    console.error('[Cesium] 渲染崩溃详细信息：', err)
    try {
      const cs = entityCollection?.entities
      if (cs) {
        cs.values.forEach((e: any) => {
          if (!e || e.isDestroyed?.()) return
          try {
            if (e.model) console.warn('[Cesium] 模型实体:', e.id, 'modelMatrix:', e.model.modelMatrix)
            if (e.billboard && !e.billboard.image) console.warn('[Cesium] billboard缺图:', e.id)
          } catch (_) {}
        })
      }
    } catch (_) {}
  })

  // 加载行政区划边界（后台执行，不阻塞初始化）
  loadAdminBoundaries()

  // 设置点击拾取事件
  setupPickHandler()

  // 鼠标移动显示坐标
  const coordsEl = document.getElementById('map-coords')
  if (coordsEl) {
    viewer.canvas.addEventListener('mousemove', (e: MouseEvent) => {
      const cartesian = viewer.camera.pickEllipsoid(
        new Cesium.Cartesian2(e.clientX, e.clientY),
      )
      if (cartesian) {
        const carto = Cesium.Cartographic.fromCartesian(cartesian)
        const lat = Cesium.Math.toDegrees(carto.latitude).toFixed(4)
        const lon = Cesium.Math.toDegrees(carto.longitude).toFixed(4)
        coordsEl.textContent = `${lat}°N, ${lon}°E`
      }
    })
  }

  // 窗口变化重置
  window.addEventListener('resize', () => viewer.resize())

  // catch 块：try 中的错误在这里捕获，不阻止 loading 关闭
  } catch (e) {
    console.error('Cesium 初始化失败:', e)
    throw e
  }
}

// ---- 地图配置（从后端拉取） ----
const mapConfig = ref({ center: [34.08, 108.94], default_zoom: 12, tile_server_url: '' })

async function loadMapConfig() {
  try {
    const resp = await fetch('/api/v1/map/config')
    const data = await resp.json()
    if (data.success && data.result) {
      mapConfig.value = data.result
    }
  } catch { /* 使用默认配置 */ }
}

// ---- ArcGIS ESRI 卫星影像 (通过后端代理+缓存) ----
const TILE_URL = window.location.origin + '/api/v1/tiles/{z}/{y}/{x}'

function createImageryProvider(_mode: string): any {
  return new Cesium.UrlTemplateImageryProvider({
    url: TILE_URL,
    maximumLevel: 19,
  })
}

// ---- 行政区划边界 ----
async function loadAdminBoundaries() {
  try {
    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 5000)
    const resp = await fetch(
      'https://raw.githubusercontent.com/nvkelso/natural-earth-vector/master/geojson/ne_50m_admin_0_boundary_lines_land.geojson',
      { signal: controller.signal }
    )
    clearTimeout(timeoutId)
    const geojson = await resp.json()
    const ds = await Cesium.GeoJsonDataSource.load(geojson, {
      stroke: Cesium.Color.fromCssColorString('#2a3a4a'),
      strokeWidth: 1.5,
      fill: Cesium.Color.fromCssColorString('#0a1628').withAlpha(0.0),
      clampToGround: true,
    })
    viewer.dataSources.add(ds)
  } catch (e) {
    console.warn('行政区划边界加载失败（不影响核心功能）', e)
  }
}

// 目标分类中文名映射
const targetClassCN: Record<string, string> = {
  'DJI': '大疆无人机',
  'AUTEL': '道通无人机',
  'FIXED_WING': '固定翼无人机',
  'MULTICOPTER': '多旋翼无人机',
  'BIRD': '鸟类',
  'HELICOPTER': '直升机',
  'UNKNOWN': '未知目标',
  'Multicopter': '多旋翼无人机',
  'FixedWing': '固定翼无人机',
  'Dji': '大疆无人机',
  'Autel': '道通无人机',
  'Bird': '鸟类',
  'Unknown': '未知目标',
  'Helicopter': '直升机',
}

// 颜色转 rgba 辅助函数
function colorToRgbaFn(hex: string, alpha: number): string {
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  return `rgba(${r},${g},${b},${alpha})`
}

// 机型图标 Canvas 绘制器 → 返回 DataURL，用于 billboard
// 每个机型画一个 64x64 的彩色图标，缓存复用
const _droneIconCache: Record<string, string> = {}

function createDroneIcon(classification: string, colorHex: string): string {
  const c = document.createElement('canvas')
  c.width = 64; c.height = 64
  const ctx = c.getContext('2d')!
  const cx = 32, cy = 32

  // 机身颜色
  const bodyColor = colorHex

  // 根据机型选择形状
  switch ((classification || '').toUpperCase()) {
    case 'FIXED_WING':
    case 'FixedWing':
      // 固定翼 — 流线型三角形 + 后掠翼
      ctx.fillStyle = bodyColor
      ctx.beginPath()
      ctx.moveTo(cx + 20, cy)       // 机头
      ctx.lineTo(cx - 8, cy - 16)   // 左翼尖
      ctx.lineTo(cx - 18, cy)       // 尾部
      ctx.lineTo(cx - 8, cy + 16)   // 右翼尖
      ctx.closePath()
      ctx.fill()
      break

    case 'HELICOPTER':
    case 'Helicopter':
      // 直升机 — 椭圆机身 + 主旋翼
      ctx.fillStyle = bodyColor
      ctx.beginPath()
      ctx.ellipse(cx, cy, 10, 18, 0, 0, Math.PI * 2)
      ctx.fill()
      // 主旋翼横杆
      ctx.strokeStyle = '#333'
      ctx.lineWidth = 3
      ctx.beginPath()
      ctx.moveTo(cx - 22, cy - 14); ctx.lineTo(cx + 22, cy - 14)
      ctx.stroke()
      break

    default:
      // 多旋翼（DJI/Autel/Multicopter/Unknown）— X型四旋翼
      // 4个旋翼臂
      ctx.strokeStyle = '#444'
      ctx.lineWidth = 2.5
      ctx.beginPath(); ctx.moveTo(cx, cy); ctx.lineTo(cx + 18, cy - 18); ctx.stroke()
      ctx.beginPath(); ctx.moveTo(cx, cy); ctx.lineTo(cx + 18, cy + 18); ctx.stroke()
      ctx.beginPath(); ctx.moveTo(cx, cy); ctx.lineTo(cx - 18, cy - 18); ctx.stroke()
      ctx.beginPath(); ctx.moveTo(cx, cy); ctx.lineTo(cx - 18, cy + 18); ctx.stroke()
      // 中央机身
      ctx.fillStyle = bodyColor
      ctx.beginPath()
      ctx.arc(cx, cy, 10, 0, Math.PI * 2)
      ctx.fill()
      // 4个电机
      ctx.fillStyle = '#333'
      ctx.beginPath(); ctx.arc(cx + 20, cy - 20, 4.5, 0, Math.PI * 2); ctx.fill()
      ctx.beginPath(); ctx.arc(cx + 20, cy + 20, 4.5, 0, Math.PI * 2); ctx.fill()
      ctx.beginPath(); ctx.arc(cx - 20, cy - 20, 4.5, 0, Math.PI * 2); ctx.fill()
      ctx.beginPath(); ctx.arc(cx - 20, cy + 20, 4.5, 0, Math.PI * 2); ctx.fill()
      // 白色边框
      ctx.strokeStyle = 'rgba(255,255,255,0.5)'
      ctx.lineWidth = 1.5
      ctx.beginPath(); ctx.arc(cx, cy, 10, 0, Math.PI * 2); ctx.stroke()
      break
  }

  // 底部虚线 — 地面投影暗示
  ctx.strokeStyle = 'rgba(255,255,255,0.25)'
  ctx.lineWidth = 1
  ctx.setLineDash([3, 3])
  ctx.beginPath(); ctx.moveTo(cx - 12, cy + 22); ctx.lineTo(cx + 12, cy + 22); ctx.stroke()
  ctx.setLineDash([])

  return c.toDataURL('image/png')
}

// 首次聚焦标记（防止重复聚焦）
let followTargetId: string | null = null

// ---- 更新目标实体（由 scene.postRender 驱动，与 Cesium 渲染循环同步） ----
function updateEntities() {
  if (!viewer || !entityCollection) return
  const targets = situation.targets
  const cs = entityCollection.entities

  try {
    // 1) 收集现有实体 ID，清理已消失目标
    const activeTargetIds = new Set(targets.map(t => t.target_id))
    const toRemove: any[] = []
    cs.values.forEach((e: any) => {
      if (!e.id) return
      const m = e.id.match(/^(?:model|label|ellipse|box)-target-([a-f0-9-]+)$/)
      if (m && !activeTargetIds.has(m[1])) toRemove.push(e)
    })
    toRemove.forEach(e => cs.remove(e))

    if (!targets.length) return

    targets.forEach(t => {
      try {
        // === 位置验证：任何坐标无效则跳过整个实体 ===
        const lon = t.position?.longitude
        const lat = t.position?.latitude
        const alt = t.position?.altitude ?? 100
        if (typeof lon !== 'number' || typeof lat !== 'number') return
        if (!isFinite(lon) || !isFinite(lat)) return
        // 经纬度范围校验
        if (lon < -180 || lon > 180 || lat < -90 || lat > 90) return

        const isHigh = t.threat_level === 'RED'
        const isMed = t.threat_level === 'YELLOW'
        const color = isHigh ? '#ef5350' : isMed ? '#ffd54f' : '#66bb6a'
        const height = (typeof alt === 'number' && isFinite(alt) && alt >= 0) ? alt : 100
        const vn = t.velocity?.vn || 0
        const ve = t.velocity?.ve || 0
        const headingRad = (vn !== 0 || ve !== 0)
          ? Cesium.Math.toRadians(Math.atan2(ve, vn) * 180 / Math.PI)
          : Cesium.Math.toRadians(0)
        const cnName = targetClassCN[t.classification] || t.classification
        const thCN = isHigh ? '高危' : isMed ? '中危' : '低危'
        const speed = Math.sqrt(vn * vn + ve * ve).toFixed(1)
        const prefix = `target-${t.target_id}`

        // 创建位置对象并验证有效性
        const pos = Cesium.Cartesian3.fromDegrees(lon, lat, height)
        if (!pos || typeof pos.x !== 'number' || !isFinite(pos.x)) return

        // === 高危目标地面光晕 ===
        let ellipseEnt = cs.getById(`ellipse-${prefix}`)
        if (isHigh) {
          if (!ellipseEnt) {
            const canvas = document.createElement('canvas')
            canvas.width = 128; canvas.height = 128
            const ctx = canvas.getContext('2d')!
            const grad = ctx.createRadialGradient(64, 64, 0, 64, 64, 64)
            grad.addColorStop(0, colorToRgbaFn(color, 0.25))
            grad.addColorStop(0.4, colorToRgbaFn(color, 0.10))
            grad.addColorStop(1, colorToRgbaFn(color, 0))
            ctx.fillStyle = grad; ctx.beginPath(); ctx.arc(64, 64, 64, 0, Math.PI * 2); ctx.fill()
            ellipseEnt = cs.add({
              id: `ellipse-${prefix}`,
              position: Cesium.Cartesian3.fromDegrees(lon, lat, 1),
              billboard: {
                image: canvas.toDataURL(),
                scale: 1.5,
                verticalOrigin: Cesium.VerticalOrigin.CENTER,
                horizontalOrigin: Cesium.HorizontalOrigin.CENTER,
                heightReference: Cesium.HeightReference.NONE,
                disableDepthTestDistance: 5000,
              },
            })
          } else {
            ellipseEnt.position = new Cesium.Cartesian3.fromDegrees(lon, lat, 1)
          }
        } else if (ellipseEnt) {
          cs.remove(ellipseEnt)
        }

        // === 无人机图标（Canvas画布绘制 billboard，彻底避开 Model 矩阵管线） ===
        // Cesium 的 3D glTF Model 图元在 buildDrawCommands → multiplyByPoint
        // 路径上有无法通过防御代码修复的崩溃问题（可能涉及 glTF accessor 对齐），
        // 改用 Canvas 绘制的 2D billboard 替代，billboard 不经过 Model 渲染管线。

        const iconKey = `${t.classification}_${color}`
        if (!_droneIconCache[iconKey]) {
          _droneIconCache[iconKey] = createDroneIcon(t.classification, color)
        }
        const iconImage = _droneIconCache[iconKey]

        let modelEnt = cs.getById(`model-${prefix}`)
        if (!modelEnt) {
          modelEnt = cs.add({
            id: `model-${prefix}`,
            position: Cesium.Cartesian3.clone(pos),
            billboard: {
              image: iconImage,
              scale: isHigh ? 1.0 : isMed ? 0.85 : 0.7,
              verticalOrigin: Cesium.VerticalOrigin.CENTER,
              horizontalOrigin: Cesium.HorizontalOrigin.CENTER,
              heightReference: Cesium.HeightReference.RELATIVE_TO_GROUND,
              disableDepthTestDistance: 50000,
              distanceDisplayCondition: new Cesium.DistanceDisplayCondition(0, 100000),
              pixelOffsetScaleByDistance: new Cesium.NearFarScalar(100, 1.2, 5000, 0.5),
            },
          })
        } else {
          modelEnt.position = new Cesium.Cartesian3.fromDegrees(lon, lat, height)
          if (modelEnt.billboard) {
            modelEnt.billboard.image = iconImage
            modelEnt.billboard.rotation = -headingRad
            modelEnt.billboard.scale = isHigh ? 1.0 : isMed ? 0.85 : 0.7
          }
        }

        // === 中文标牌 ===
        const isExpanded = situation.isLabelExpanded(t.target_id)
        const briefText = `${cnName} ${thCN}`
        const fullText = `${cnName} [${thCN}]\n${lat.toFixed(4)}°N, ${lon.toFixed(4)}°E\n${height.toFixed(0)}m · ${speed}m/s`
        const labelText = isExpanded ? fullText : briefText

        let labelEnt = cs.getById(`label-${prefix}`)
        if (!labelEnt) {
          labelEnt = cs.add({
            id: `label-${prefix}`,
            position: Cesium.Cartesian3.clone(pos),
            label: {
              text: labelText,
              font: isExpanded ? '13px "PingFang SC", "Microsoft YaHei", sans-serif' : '14px "PingFang SC", "Microsoft YaHei", sans-serif',
              fillColor: Cesium.Color.WHITE,
              outlineColor: Cesium.Color.fromCssColorString('#001529').withAlpha(0.3),
              outlineWidth: 0.5,
              showBackground: true,
              backgroundColor: Cesium.Color.fromCssColorString('#0f6cbd').withAlpha(0.88),
              backgroundPadding: new Cesium.Cartesian2(isExpanded ? 10 : 8, isExpanded ? 8 : 5),
              pixelOffset: new Cesium.Cartesian2(0, isExpanded ? -60 : -45),
              verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
              horizontalOrigin: Cesium.HorizontalOrigin.CENTER,
              heightReference: Cesium.HeightReference.RELATIVE_TO_GROUND,
              disableDepthTestDistance: 50000,
              distanceDisplayCondition: new Cesium.DistanceDisplayCondition(0, 100000),
              scale: 1.0,
            },
          })
        } else {
          labelEnt.position = new Cesium.Cartesian3.fromDegrees(lon, lat, height)
          if (labelEnt.label) {
            labelEnt.label.text = labelText
            labelEnt.label.font = isExpanded ? '13px "PingFang SC", "Microsoft YaHei", sans-serif' : '14px "PingFang SC", "Microsoft YaHei", sans-serif'
            labelEnt.label.pixelOffset = new Cesium.Cartesian2(0, isExpanded ? -60 : -45)
            labelEnt.label.backgroundPadding = new Cesium.Cartesian2(isExpanded ? 10 : 8, isExpanded ? 8 : 5)
          }
        }

        // === 选中框 ===
        const selBox = cs.getById(`box-${prefix}`)
        if (selBox && situation.selectedTargetId === t.target_id) {
          selBox.position = new Cesium.Cartesian3.fromDegrees(lon, lat, height)
        }
      } catch (e) {
        console.warn('更新实体跳过:', e)
      }
    })

    // 首次自动聚焦
    if (!followTargetId && targets.length > 0) {
      followTargetId = 'all'
      flyToAllTargets()
    }
  } catch (_e) {
    // 外层错误不中断渲染
  }
}

// 鼠标点击拾取目标
let pickHandler: any = null
let prevSelectedId: string | null = null

function setupPickHandler() {
  if (!viewer || pickHandler) return
  pickHandler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)
  pickHandler.setInputAction((click: any) => {
    const picked = viewer.scene.pick(click.position)
    if (!picked || !picked.id) return

    // 从 id 中提取 target_id: 格式 "model-target-<uuid>" 或 "label-target-<uuid>"
    const idStr: string = picked.id._id || picked.id.id || ''
    const match = idStr.match(/target-([a-f0-9-]+)$/)
    if (match) {
      situation.selectTarget(match[1])
      return
    }

    // fallback: 通过位置匹配
    const pos = picked.id.position?.getValue(Cesium.JulianDate.now())
    if (!pos) return
    const carto = Cesium.Cartographic.fromCartesian(pos)
    const lon = Cesium.Math.toDegrees(carto.longitude)
    const lat = Cesium.Math.toDegrees(carto.latitude)
    const target = situation.targets.find(t =>
      Math.abs(t.position.latitude - lat) < 0.02 &&
      Math.abs(t.position.longitude - lon) < 0.02
    )
    if (target) situation.selectTarget(target.target_id)
  }, Cesium.ScreenSpaceEventType.LEFT_CLICK)

  // 双击展开/折叠标牌详情
  let dblTimer: any = null
  pickHandler.setInputAction((click: any) => {
    const picked = viewer.scene.pick(click.position)
    if (!picked || !picked.id) return
    const idStr: string = picked.id._id || picked.id.id || ''
    const match = idStr.match(/target-([a-f0-9-]+)$/)
    if (match) {
      situation.toggleExpandLabel(match[1])
    }
  }, Cesium.ScreenSpaceEventType.LEFT_DOUBLE_CLICK)
}

// 监听选中状态变化 → 切换选中框
watch(() => situation.selectedTargetId, (newId, oldId) => {
  if (!viewer || !entityCollection) return
  const cs = entityCollection.entities

  // 移除旧选中目标的框
  if (oldId) {
    const oldBox = cs.getById(`box-target-${oldId}`)
    if (oldBox) cs.remove(oldBox)
  }

  // 为新选中目标添加框
  if (newId) {
    const t = situation.targets.find(x => x.target_id === newId)
    if (t) {
      const isHigh = t.threat_level === 'RED'
      const isMed = t.threat_level === 'YELLOW'
      const color = isHigh ? '#ef5350' : isMed ? '#ffd54f' : '#66bb6a'
      const boxSize = isHigh ? 1.2 : isMed ? 1.0 : 0.8
      const height = t.position.altitude || 100
      const boxId = `box-target-${newId}`
      const existing = cs.getById(boxId)
      if (!existing) {
        cs.add({
          id: boxId,
          position: Cesium.Cartesian3.fromDegrees(t.position.longitude, t.position.latitude, height),
          box: {
            dimensions: new Cesium.Cartesian3(boxSize, boxSize, boxSize),
            material: Cesium.Color.fromCssColorString(color).withAlpha(0.0),
            outline: true,
            outlineColor: Cesium.Color.fromCssColorString(color).withAlpha(0.9),
            outlineWidth: 3,
          },
        })
      }
    }
  }
})
// ---- 重置视角（回到西安秦岭北侧演练区） ----
function resetView() {
  flyToAllTargets()
}

// ---- 聚焦所有目标（让所有无人机都在视野内） ----
function flyToAllTargets() {
  if (!viewer) return
  const targets = situation.targets
  if (!targets.length) {
    viewer.camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(mapConfig.value.center[1], mapConfig.value.center[0], 12000),
      orientation: { heading: 0, pitch: Cesium.Math.toRadians(-35), roll: 0 },
      duration: 2,
    })
    return
  }

  // 计算所有目标的包围球
  const points = targets
    .filter(t => isFinite(t.position.longitude) && isFinite(t.position.latitude))
    .map(t =>
      Cesium.Cartesian3.fromDegrees(t.position.longitude, t.position.latitude, t.position.altitude || 100)
    )
  if (!points.length) {
    viewer.camera.flyTo({
      destination: Cesium.Cartesian3.fromDegrees(mapConfig.value.center[1], mapConfig.value.center[0], 12000),
      orientation: { heading: 0, pitch: Cesium.Math.toRadians(-35), roll: 0 },
      duration: 2,
    })
    return
  }
  const sphere = Cesium.BoundingSphere.fromPoints(points)
  // 放大包围球确保边缘目标也可见
  sphere.radius = Math.max(sphere.radius * 1.5, 500)

  viewer.camera.flyToBoundingSphere(sphere, {
    duration: 2,
    offset: new Cesium.HeadingPitchRange(
      Cesium.Math.toRadians(0),
      Cesium.Math.toRadians(-35),
      sphere.radius * 2.5
    ),
  })
}

// ---- 聚焦单个目标 ----
function flyToTarget() {
  const t = situation.selectedTarget
  if (!t || !viewer) return
  viewer.camera.flyTo({
    destination: Cesium.Cartesian3.fromDegrees(
      t.position.longitude,
      t.position.latitude,
      Math.max(t.position.altitude + 500, 2000)
    ),
    orientation: { heading: 0, pitch: Cesium.Math.toRadians(-15), roll: 0 },
    duration: 1.5,
  })
}
</script>

<template>
  <div class="cesium-wrap">
    <!-- Cesium 容器 -->
    <div ref="mapContainer" class="cesium-container">
      <div v-if="loading" class="loading-overlay">
        <div class="loader-spin"></div>
        <span>Cesium 引擎加载中...</span>
      </div>
    </div>

    <!-- ===== 左上：空域态势 ===== -->
    <div class="ui-top-left">
      <div class="glass-card">
        <div class="card-head">空域态势</div>
        <div class="stat-row">
          <div class="stat-item">
            <span class="stat-val red">{{ situation.redThreats.length }}</span>
            <span class="stat-lbl">高危</span>
          </div>
          <div class="stat-div"></div>
          <div class="stat-item">
            <span class="stat-val yellow">{{ situation.yellowThreats.length }}</span>
            <span class="stat-lbl">中危</span>
          </div>
          <div class="stat-div"></div>
          <div class="stat-item">
            <span class="stat-val green">{{ situation.greenThreats.length }}</span>
            <span class="stat-lbl">低危</span>
          </div>
          <div class="stat-div"></div>
          <div class="stat-item">
            <span class="stat-val blue">{{ situation.totalTargets }}</span>
            <span class="stat-lbl">总计</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== 左下：图例 + 坐标 ===== -->
    <div class="ui-bottom-left">
      <div class="glass-card compact">
        <div class="legend-row"><span class="lg-dot red"></span> 高危目标</div>
        <div class="legend-row"><span class="lg-dot yellow"></span> 中危目标</div>
        <div class="legend-row"><span class="lg-dot green"></span> 低危目标</div>
      </div>
    </div>

    <!-- ===== 右下：坐标 + 模式信息 ===== -->
    <div class="ui-bottom-right">
      <div class="glass-card compact" style="flex-direction:row; gap:12px; align-items:center">
        <span style="color:var(--fg-3);font-size:11px;display:inline-flex;align-items:center;gap:4px"><Icon name="mouse" :size="12" /><span id="map-coords">移动鼠标查看坐标</span></span>
        <span style="color:var(--fg-4);font-size:10px">| 边界 ✓</span>
      </div>
    </div>
  </div>
  <!-- ===== 设备管理对话框 ===== -->
  
  
</template>

<style>
/* Cesium 基础样式由 CDN 的 <link> 标签提供 */
</style>

<style scoped>
.cesium-wrap { width:100%; height:100%; position:relative; overflow:hidden;
  background:
    repeating-radial-gradient(circle at center, transparent 0 79px, color-mix(in srgb, var(--brand-rest) 16%, transparent) 79px 80px),
    repeating-linear-gradient(0deg, transparent 0 47px, color-mix(in srgb, var(--stroke-control) 45%, transparent) 47px 48px),
    repeating-linear-gradient(90deg, transparent 0 47px, color-mix(in srgb, var(--stroke-control) 45%, transparent) 47px 48px),
    radial-gradient(ellipse at center, color-mix(in srgb, var(--brand-rest) 14%, transparent), transparent 62%),
    var(--bg-canvas);
}
/* 雷达扫描光（Cesium 出图后被其画布遮挡，空图时作科技底衬） */
.cesium-wrap::before { content:''; position:absolute; top:50%; left:50%; width:720px; height:720px; margin:-360px 0 0 -360px; border-radius:50%; background:conic-gradient(from 0deg, transparent 0deg, var(--glow-soft) 46deg, transparent 66deg); animation:radarSweep 7s linear infinite; opacity:0.55; pointer-events:none; }
@keyframes radarSweep { to { transform:rotate(360deg); } }
.cesium-container { width:100%; height:100%; position:absolute; inset:0; }
.loading-overlay { position:absolute; inset:0; display:flex; flex-direction:column; gap:16px; align-items:center; justify-content:center; background:var(--bg-canvas); color:var(--fg-3); font-size:14px; z-index:9999; }
.loader-spin { width:36px; height:36px; border:3px solid var(--stroke-divider); border-top-color:var(--brand-fg); border-radius:50%; animation:spin 1s linear infinite; }
@keyframes spin { to { transform:rotate(360deg); } }

.ui-top-left { position:absolute; top:16px; left:16px; z-index:100; }
.ui-bottom-left { position:absolute; bottom:16px; left:16px; z-index:100; }
.ui-bottom-right { position:absolute; bottom:16px; right:16px; z-index:100; }

/* 科技感亚克力 HUD 叠层 */
.glass-card { position:relative; background:color-mix(in srgb, var(--bg-surface) 74%, transparent); border:1px solid var(--stroke-control); border-radius:var(--radius-xl); padding:12px 16px; backdrop-filter:blur(20px) saturate(1.3); -webkit-backdrop-filter:blur(20px) saturate(1.3); box-shadow:var(--shadow-16), inset 0 0 24px -12px var(--glow-accent); }
/* HUD 角标（左上 + 右下） */
.glass-card::before, .glass-card::after { content:''; position:absolute; width:12px; height:12px; border:1.5px solid var(--brand-fg); pointer-events:none; filter:drop-shadow(0 0 4px var(--glow-accent)); opacity:0.9; }
.glass-card::before { top:6px; left:6px; border-right:none; border-bottom:none; border-top-left-radius:var(--radius-sm); }
.glass-card::after { bottom:6px; right:6px; border-left:none; border-top:none; border-bottom-right-radius:var(--radius-sm); }
.glass-card.compact { padding:8px 12px; }
.card-head { font-size:11px; color:var(--brand-fg); text-transform:uppercase; letter-spacing:0.8px; font-weight:600; }

.stat-row { display:flex; align-items:center; gap:0; margin-top:8px; }
.stat-item { text-align:center; flex:1; min-width:40px; }
.stat-val { display:block; font-size:22px; font-weight:700; line-height:1.2; text-shadow:0 0 16px color-mix(in srgb, currentColor 50%, transparent); }
.stat-lbl { display:block; font-size:10px; color:var(--fg-3); margin-top:2px; }
.stat-val.red { color:var(--danger); }
.stat-val.yellow { color:var(--warning); }
.stat-val.green { color:var(--success); }
.stat-val.blue { color:var(--brand-fg); }
.stat-div { width:1px; height:28px; background:var(--stroke-divider); }

.legend-row { display:flex; align-items:center; gap:8px; font-size:11px; color:var(--fg-2); padding:2px 0; }
.lg-dot { width:8px; height:8px; border-radius:var(--radius-circular); flex-shrink:0; }
.lg-dot.red { background:var(--danger); box-shadow:0 0 0 3px var(--danger-bg); }
.lg-dot.yellow { background:var(--warning); box-shadow:0 0 0 3px var(--warning-bg); }
.lg-dot.green { background:var(--success); box-shadow:0 0 0 3px var(--success-bg); }

:deep(.cesium-viewer) { background:var(--bg-canvas); }
:deep(.cesium-widget-credits) { display:none !important; }
:deep(.cesium-viewer-bottom) { display:none; }
:deep(.cesium-performanceDisplay) { display:none !important; }
</style>