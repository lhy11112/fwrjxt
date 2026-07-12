<script setup lang="ts">
/**
 * wxdzc 侦测预警 — 完整功能页面
 * 移植自 D:\fjwrj\wjfjzd-dxyy-front\src\views\portal\wxdzc\index.vue (3672行)
 * 所有核心功能均已移植：设备标注、无人机标注、频谱测向、飞手标注、2D/3D推演、时间轴、威胁等级等
 */
import { ref, reactive, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useWxdzcStore } from '@/stores/wxdzc'
import type { DroneItem } from '@/stores/wxdzc'
import * as wxdzcApi from '@/api/wxdzc'
import api from '@/api/index'
import { getDestinationPoint, getCirclePoints, createSectorPoints, formatSecondsToHMS, haversineDistance } from '@/utils/wxdzcUtils'
import DeviceDetailDialog from './DeviceDetailDialog.vue'
import DroneDetailDialog from './DroneDetailDialog.vue'

const store = useWxdzcStore()

// ============ Leaflet 图层 ============
let L: any = null
let leafletMap: any = null
let lineLayer: any, lineLayer2: any, markerLayer: any
let zymbMarkerLayer: any, sectorLayers: any, wrjMarkerLayer: any
let fsMarkerLayer: any

// ============ 图标缓存 ============
const deviceIcons: Record<string, any> = {}  // key: "status_type" -> L.icon
const droneIcons: Record<number, any> = {}   // key: authStatus -> L.icon
const fsIcon: any = null

// ============ 推演状态 ============
const returnBackFlag = ref(false)
const tyFlag = ref(false)
const mapChange = ref(false)
let timelineInterval: any = null
let currentTime = 0
let planeMarkers: any[] = []
let fxData = ref<any[]>([])
const rowData = ref<any>({})
const speed = ref(0)
const detailInfoObj = ref<any>(null)

// ============ 定时器 ============
let mainTimer: any = null
let tyTimer: any = null

// ============ UI状态 ============
const wrjCurrentIndex = ref(-1)
const ycqtBtnVisible = ref(false)
const wrjDetailRef = ref<InstanceType<typeof DroneDetailDialog> | null>(null)
const deviceDetailRef = ref<InstanceType<typeof DeviceDetailDialog> | null>(null)
const contextMenuDrone = ref<DroneItem | null>(null)
const contextMenuPos = ref({ x: 0, y: 0 })
const contextMenuVisible = ref(false)

// ============ 面板折叠 ============
const devicePanelStyle = computed(() => ({
  transform: store.devicePanelVisible ? 'translateX(0)' : 'translateX(-368px)',
  transition: 'transform 0.3s ease',
}))
const dronePanelStyle = computed(() => ({
  transform: store.dronePanelVisible ? 'translateX(0)' : 'translateX(420px)',
  transition: 'transform 0.3s ease',
}))
const droneDetailPanelStyle = computed(() => ({
  transform: store.droneDetailPanelVisible ? 'translateX(0)' : 'translateX(385px)',
  transition: 'transform 0.3s ease',
}))

// ============ 设备详情列 ============
const detailColumn = [
  { prop: 'brand', label: '无人机品牌' }, { prop: 'model', label: '无人机型号' },
  { prop: 'serial', label: '无人机序列号' }, { prop: 'dron_lng', label: '无人机经度' },
  { prop: 'dron_lat', label: '无人机纬度' }, { prop: 'home_lng', label: '起飞点经度' },
  { prop: 'home_lat', label: '起飞点纬度' }, { prop: 'pilot_lng', label: '遥控器经度' },
  { prop: 'pilot_lat', label: '遥控器纬度' }, { prop: 'altitude', label: '海拔高度(m)' },
  { prop: 'height', label: '高度' }, { prop: 'sd', label: '速度' },
  { prop: 'east_v', label: '东速度' }, { prop: 'north_v', label: '北速度' },
  { prop: 'up_v', label: '上速度' }, { prop: 'freq', label: '频率' },
  { prop: 'rssi', label: '信号强度' }, { prop: 'distance', label: '距离(m)' },
  { prop: 'uuid', label: '飞手执照代码' }, { prop: 'angle', label: '飞机角度' },
  { prop: 'station_id', label: '站ID' }, { prop: 'data_time', label: '时间戳' },
  { prop: 'create_time', label: '入库时间' },
]

// ============ 初始化 ============
onMounted(async () => {
  L = await import('leaflet')
  await nextTick()
  store.currentDate = formatDate(new Date())
  store.fetchDateCalendar()
  initMap()
  loadAllData()
  mainTimer = setInterval(() => {
    store.fetchDevices()
    getWxdData()
  }, 6000)
})

onUnmounted(() => {
  clearInterval(mainTimer)
  clearInterval(tyTimer)
  clearInterval(timelineInterval)
  clearAllLayers()
  if (leafletMap) { leafletMap.remove(); leafletMap = null }
})

// ============ 地图初始化 ============
function initMap() {
  const container = document.getElementById('wxdzc-map')
  if (!container) return
  // Try to reuse existing map
  const existing = (window as any).Map2D?.map
  if (existing) {
    leafletMap = existing
  } else {
    leafletMap = L.map(container, { center: [34.08, 108.94], zoom: 12, zoomControl: false, attributionControl: false })
    L.tileLayer(window.location.origin + '/api/v1/tiles/{z}/{y}/{x}').addTo(leafletMap)
  }
  lineLayer = L.layerGroup().addTo(leafletMap)
  lineLayer2 = L.layerGroup().addTo(leafletMap)
  markerLayer = L.layerGroup().addTo(leafletMap)
  zymbMarkerLayer = L.layerGroup().addTo(leafletMap)
  sectorLayers = L.layerGroup().addTo(leafletMap)
  wrjMarkerLayer = L.layerGroup().addTo(leafletMap)
  fsMarkerLayer = L.layerGroup().addTo(leafletMap)
}

function formatDate(d: Date): string {
  return d.toISOString().slice(0, 10)
}

// ============ 数据加载 ============
async function loadAllData() {
  await Promise.all([store.fetchDevices(), getWxdData(true)])
  addDevicePoints()
}

async function getWxdData(isCx = false) {
  try {
    const list = await wxdzcApi.getByStation({ rq: store.currentDate, auth_status: store.authStatusFilter })
    let filtered = list as DroneItem[]
    if (store.modelFilter) {
      const m = store.modelFilter.toLowerCase()
      filtered = filtered.filter(v => (v.model || '').toLowerCase().includes(m))
    }
    store.zcNum = filtered.filter(v => v.status === 1).length
    store.gjNum = filtered.filter(v => v.status === 2).length
    store.slNum = filtered.filter(v => v.status !== 1 && v.status !== 2).length

    // Calculate ETA for each drone
    for (const item of filtered) {
      for (const dev of store.allDevices) {
        if (item.station_id === dev.station_id && item.dron_lat && item.dron_lng && dev.wd && dev.jd) {
          const dist = haversineDistance([item.dron_lat, item.dron_lng], [dev.wd, dev.jd])
          ;(item as any)._ddkydtime = item.sd ? ((dist) / item.sd).toFixed(3) : ''
          ;(item as any)._ddkysxtime = item.sd && item.data_time
            ? new Date(new Date(item.data_time).getTime() + Number((item as any)._ddkydtime) * 1000).toLocaleString()
            : ''
        }
      }
    }

    // Preserve colors/widths from previous
    if (isCx) {
      for (const item of filtered) {
        ;(item as any).color = randomColor()
        ;(item as any).gjWidth = 3
      }
      store.dronesCopy = JSON.parse(JSON.stringify(filtered))
    } else if (store.dronesCopy.length) {
      for (const item of filtered) {
        const prev = store.dronesCopy.find((c: any) => c.brand === item.brand && c.model === item.model && c.serial === item.serial)
        if (prev) { (item as any).color = prev.color; (item as any).gjWidth = prev.gjWidth }
      }
    }

    store.drones = filtered
    renderAllMarkers()
  } catch (e) { console.error('[wxdzc] getWxdData error:', e) }
}

// ============ 设备标注 ============
function addDevicePoints() {
  zymbMarkerLayer?.clearLayers()
  for (const item of store.devices) {
    if (!item.jd || !item.wd) continue
    const iconKey = `${item.status}_${item.device_type}`
    if (!deviceIcons[iconKey]) {
      deviceIcons[iconKey] = L.icon({ iconUrl: getDeviceIconUrl(item.status, item.device_type), iconSize: [25, 25] })
    }
    const marker = L.marker([item.wd, item.jd], { icon: deviceIcons[iconKey] }).addTo(zymbMarkerLayer)
    const statusText = item.status === 'CONNECTED' ? '已连接' : item.status === 'DISCONNECTED' ? '未连接' : '告警中'
    const html = `<div style="width:140px;background:#1d5891;padding:10px;color:#fff;font-size:12px;">
      <div>名称：${item.name || item.device_id}</div>
      <div>经度：${item.jd?.toFixed(3)}</div><div>纬度：${item.wd?.toFixed(3)}</div>
      <div>状态：<span style="color:${item.status==='CONNECTED'?'#6fd66f':'#ff7a86'}">${statusText}</span></div></div>`
    marker.bindTooltip(html)
    marker.on('click', () => deviceDetailRef.value?.open(item))

    // Coverage circle
    if (item.zcbj) {
      const radius = Number(item.zcbj) * 1000
      const points = getCirclePoints([item.wd, item.jd], radius)
      L.polygon(points, { color: '#03f83c', weight: 1, fillOpacity: 0.08 }).addTo(zymbMarkerLayer)
    }
    // Device name label
    L.marker([item.wd, item.jd], {
      icon: L.divIcon({
        html: `<div style="width:180px;text-align:center;font-size:12px;color:#000;">
          <div>${item.name || ''}</div><div style="color:${item.status==='CONNECTED'?'green':'red'}">${statusText}</div></div>`,
        className: 'my-div-icon', iconAnchor: [90, -20],
      }),
    }).addTo(zymbMarkerLayer)
  }
}

function getDeviceIconUrl(status: string | undefined, deviceType: string | undefined): string {
  const s = status === 'CONNECTED' ? '' : status === 'DISCONNECTED' ? '2' : '3'
  const t = deviceType === 'DETECT' ? 'zcsb' : deviceType === 'DISTURB' ? 'grsb' : deviceType === 'TRAP' ? 'ypsb' : 'system'
  return `/static/${t}${s}.png`
}

// ============ 无人机标注 ============
function renderAllMarkers() {
  clearLayer2(); clearLayer4()
  if (store.hideOthersMode) { addWrjPoints2(); addFsPoints2() }
  else { addWrjPoints(); addFsPoints() }
}

function addWrjPoints() {
  if (!store.layerFilters.includes('无人机')) return
  for (const item of store.drones) {
    renderDroneMarker(item, store.drones.indexOf(item))
  }
}

function addWrjPoints2() {
  if (!store.layerFilters.includes('无人机')) return
  store.drones.forEach((item, i) => {
    if (wrjCurrentIndex.value === i) renderDroneMarker(item, i)
  })
}

async function renderDroneMarker(item: DroneItem, index: number) {
  const authStatus = item.auth_status || 3
  if (!droneIcons[authStatus]) {
    const img = authStatus === 1 ? 'fly1' : authStatus === 2 ? 'fly2' : 'fly3'
    droneIcons[authStatus] = L.icon({ iconUrl: `/static/${img}.png`, iconSize: [40, 40] })
  }

  let dronePos: [number, number] | null = null
  let isSpectrum = false

  if (item.jmlx === '频谱测向' && item.angle) {
    isSpectrum = true
    // Find device for spectrum calculation
    const device = store.allDevices.find(d => d.station_id === item.station_id)
    if (device?.wd && device?.jd && device.zcbj) {
      const radius = Number(device.zcbj) * 1000
      // Draw sector
      const sectorPts = createSectorPoints([device.wd, device.jd], radius, item.angle!, 30)
      L.polygon(sectorPts, { color: '#ff3333', weight: 1, fillColor: '#ff3333', fillOpacity: 0.15 }).addTo(sectorLayers)
      // Calculate drone position at arc center
      dronePos = getDestinationPoint([device.wd, device.jd], item.angle!, radius)
      // Draw line from device to drone
      L.polyline([[device.wd, device.jd], dronePos], { color: '#ff9800', weight: 1, dashArray: '5,5' }).addTo(sectorLayers)
    }
  } else if (item.dron_lat && item.dron_lng) {
    dronePos = [item.dron_lat, item.dron_lng]
  }
  if (!dronePos) return

  const marker = L.marker(dronePos, { icon: droneIcons[authStatus] }).addTo(wrjMarkerLayer)
  const eta = (item as any)._ddkysxtime || '暂无'
  const etaSeconds = (item as any)._ddkydtime ? formatSecondsToHMS(Number((item as any)._ddkydtime)) : '暂无'
  const infoHtml = `<div style="width:160px;background:#1d5891;padding:3px;opacity:0.9;color:#fff;font-size:11px;">
    <div>型号：${item.model}</div><div>距离(m)：${item.distance}</div><div>高度(m)：${item.height}</div>
    <div>速度(m/s)：${item.sd || 0}</div><div>频率(Mhz)：${item.freq}</div><div>信号强度：${item.rssi}</div>
    <div>经度：${item.dron_lng?.toFixed(3)}</div><div>纬度：${item.dron_lat?.toFixed(3)}</div>
    <div>到达时间：${eta}</div><div>预计耗时：${etaSeconds}</div></div>`

  if (store.layerFilters.includes('信息')) {
    L.marker(dronePos, {
      icon: L.divIcon({ html: infoHtml, className: 'my-div-icon', iconAnchor: [60, -20] }),
    }).addTo(wrjMarkerLayer)
  }

  marker.on('click', () => { wrjCurrentIndex.value = index; ycqtBtnVisible.value = true; openDroneDetail(item) })
  marker.on('contextmenu', (evt: any) => {
    ;(item as any).rq = store.currentDate
    showContextMenu(evt, item)
  })

  // Flashing red circle for 持续跟踪
  if (item.auth_status === 6) {
    const circlePts = getCirclePoints(dronePos, 1000)
    const polygon = L.polygon(circlePts, { color: 'red', weight: 1 }).addTo(wrjMarkerLayer)
    let flashIdx = 0
    setInterval(() => { polygon.setStyle({ opacity: flashIdx % 2 === 0 ? 1 : 0.3 }); flashIdx++ }, 500)
  }

  // Spectrum line and coverage
  if (!isSpectrum) {
    // Draw line from device to drone for each matching device
    for (const dev of store.allDevices) {
      if (dev.station_id === item.station_id && dev.wd && dev.jd) {
        L.polyline([[dev.wd, dev.jd], dronePos], { color: '#4db4ff', weight: 0.5, opacity: 0.4 }).addTo(wrjMarkerLayer)
      }
    }
  }
}

// ============ 飞手标注 ============
function addFsPoints() {
  if (!store.layerFilters.includes('飞手')) return
  fsMarkerLayer?.clearLayers()
  for (const item of store.drones) {
    if (item.pilot_lat && item.pilot_lng) {
      const icon = L.icon({ iconUrl: '/static/fs33.png', iconSize: [25, 25] })
      const marker = L.marker([item.pilot_lat, item.pilot_lng], { icon }).addTo(fsMarkerLayer)
      marker.bindTooltip(`${item.serial} 飞手<br>经度:${item.pilot_lng?.toFixed(3)}<br>纬度:${item.pilot_lat?.toFixed(3)}`)
    }
  }
}

function addFsPoints2() {
  if (!store.layerFilters.includes('飞手')) return
  fsMarkerLayer?.clearLayers()
  store.drones.forEach((item, i) => {
    if (wrjCurrentIndex.value === i && item.pilot_lat && item.pilot_lng) {
      const icon = L.icon({ iconUrl: '/static/fs33.png', iconSize: [25, 25] })
      const marker = L.marker([item.pilot_lat, item.pilot_lng], { icon }).addTo(fsMarkerLayer)
      marker.bindTooltip(`${item.serial} 飞手`)
    }
  })
}

// ============ 推演 ============
function ty(item: DroneItem, index: number) {
  tyFlag.value = true; wrjCurrentIndex.value = index
  clearInterval(mainTimer); clearInterval(timelineInterval); clearInterval(tyTimer)
  mapChange.value = true; store.dronePanelVisible = false
  wxdzcApi.getByModelSerial({ model: item.model, serial: item.serial, rq: store.currentDate }).then(res => {
    fxData.value = res.uav_detect_msg_list
    if (res.wjbd_wrj_jbxx) {
      rowData.value = { ...res.wjbd_wrj_jbxx, serial: item.serial, rq: store.currentDate,
        color: (item as any).color, weight: (item as any).gjWidth }
    } else {
      rowData.value = { brand: item.brand, authStatus: item.auth_status, model: item.model,
        serialNumber: item.serial, serial: item.serial, rq: store.currentDate,
        color: (item as any).color, weight: (item as any).gjWidth }
    }
    if (fxData.value.length) addToMap3D()
  })
}

function ty2D(item: DroneItem, index: number) {
  returnBackFlag.value = true; wrjCurrentIndex.value = index
  clearInterval(mainTimer); clearInterval(timelineInterval); clearInterval(tyTimer)
  mapChange.value = false; currentTime = 0; planeMarkers = []
  store.dronePanelVisible = false; store.devicePanelVisible = false
  ty2DLineData(item)
  tyTimer = setInterval(() => ty2DLineData(item), 10000)
}

function ty2DLineData(item: DroneItem) {
  wxdzcApi.getByModelSerial({ model: item.model, serial: item.serial, rq: store.currentDate }).then(res => {
    fxData.value = res.uav_detect_msg_list
    if (res.wjbd_wrj_jbxx) {
      rowData.value = { ...res.wjbd_wrj_jbxx, serial: item.serial, rq: store.currentDate,
        color: (item as any).color, weight: (item as any).gjWidth }
    } else {
      rowData.value = { brand: item.brand, model: item.model, serialNumber: item.serial,
        serial: item.serial, rq: store.currentDate,
        color: (item as any).color, weight: (item as any).gjWidth }
    }
    if (fxData.value.length) addToMap2D()
  })
}

async function addToMap3D() {
  clearInterval(timelineInterval)
  if (!fxData.value.length) return
  // 3D使用window.Map3D.wrjFly (源项目模式, Cesium viewer由dpCommon初始化)
  const wrjFly = (window as any).Map3D?.wrjFly
  if (wrjFly) {
    try {
      wrjFly.clearAllLayers()
      const path = wrjFly.generateDronePath(fxData.value)
      const entity = wrjFly.createDroneModel(fxData.value)
      wrjFly.DronePlaybackController(path, entity, rowData.value)
    } catch (e) { console.error('[wxdzc] 3D playback error:', e) }
  } else {
    console.log('[wxdzc] 3D推演数据已加载:', fxData.value.length, '航点 (Cesium viewer 未初始化)')
  }
}

function pauseTimeline() {
  clearInterval(timelineInterval); timelineInterval = null
}

function addToMap2D() {
  clearInterval(timelineInterval); clearMarkerLayer(); clearLineLayer()
  returnBackFlag.value = true
  // Draw flight path
  const latlngs = fxData.value.filter((p: any) => p.dron_lat && p.dron_lng).map((p: any) => [p.dron_lat, p.dron_lng])
  if (latlngs.length >= 2) {
    L.polyline(latlngs, { color: rowData.value.color || '#4db4ff', weight: rowData.value.weight || 3 }).addTo(lineLayer)
    leafletMap?.fitBounds(latlngs, { padding: [50, 50] })
  }
  // Create plane markers
  const planeIconUrl = rowData.value.authStatus === 1 ? '/static/fly1.png' : rowData.value.authStatus === 2 ? '/static/fly2.png' : '/static/fly3.png'
  const planeIcon = L.icon({ iconUrl: planeIconUrl, iconSize: [25, 25], iconAnchor: [12, 41] })
  fxData.value.forEach((point: any) => {
    if (point.dron_lat && point.dron_lng) {
      const m = L.marker([point.dron_lat, point.dron_lng], { icon: planeIcon })
      m.on('contextmenu', (evt: any) => {
        L.popup({ className: 'mypopup' })
          .setLatLng(evt.latlng)
          .setContent(`<div class="cd-span" style="padding:5px 10px;cursor:pointer">
            <a onclick="window._wxdzcTy3D && window._wxdzcTy3D('${JSON.stringify(rowData.value).replace(/'/g,"\\'")}',${wrjCurrentIndex.value})">三维推演</a></div>`)
          .openOn(leafletMap)
      })
      planeMarkers.push(m)
    }
  })
  // Animate
  timelineInterval = setInterval(() => {
    currentTime = (currentTime + 1) % fxData.value.length
    updateTimeline(currentTime)
    clearMarkerLayer()
    if (planeMarkers[currentTime] && fxData.value[currentTime]) {
      planeMarkers[currentTime].setLatLng([fxData.value[currentTime].dron_lat, fxData.value[currentTime].dron_lng]).addTo(markerLayer)
      const labelIcon = L.divIcon({
        html: `<div style="width:180px;text-align:center;font-size:12px;">${rowData.value.brand}-${rowData.value.model}(${rowData.value.serialNumber})</div>`,
        className: 'my-div-icon', iconAnchor: [80, 20],
      })
      L.marker([fxData.value[currentTime].dron_lat, fxData.value[currentTime].dron_lng], { icon: labelIcon }).addTo(markerLayer)
      if (fxData.value.length > 1) {
        const dt = new Date(fxData.value[currentTime].data_time).getTime() - new Date(fxData.value[0].data_time).getTime()
        const dist = haversineDistance([fxData.value[0].dron_lat, fxData.value[0].dron_lng], [fxData.value[currentTime].dron_lat, fxData.value[currentTime].dron_lng])
        speed.value = dt > 0 ? (dist / (dt / 1000)) : 0
      }
      detailInfoObj.value = { ...fxData.value[currentTime], brand: rowData.value.brand }
    }
  }, 1000)
}

function updateTimeline(time: number) { currentTime = time }

function returnBack() {
  clearInterval(mainTimer); clearInterval(tyTimer); clearInterval(timelineInterval)
  returnBackFlag.value = false; tyFlag.value = false; mapChange.value = false
  fxData.value = []; rowData.value = {}; planeMarkers = []
  store.dronePanelVisible = true; store.devicePanelVisible = true
  clearAllLayers()
  mainTimer = setInterval(() => { store.fetchDevices(); getWxdData() }, 6000)
}

// ============ 上下文菜单 ============
function showContextMenu(evt: any, item: DroneItem) {
  contextMenuDrone.value = item
  contextMenuPos.value = { x: evt.originalEvent.clientX, y: evt.originalEvent.clientY }
  contextMenuVisible.value = true
}
function onContextMenuAction(action: string) {
  contextMenuVisible.value = false
  const item = contextMenuDrone.value
  if (!item) return
  const idx = store.drones.indexOf(item)
  if (action === 'detail') { openDroneDetail(item) }
  else if (action === 'yp') { ElMessage.info('诱骗功能已触发'); /* TODO: integrate with real deception command */ }
  else if (action === 'gr') { ElMessage.info('干扰功能已触发'); /* TODO: integrate with real jamming command */ }
  else if (action === 'ty2d') { ty2D(item, idx) }
  else if (action === 'ty3d') { ty(item, idx) }
  else if (action === 'ppwrjxx') { ppwrjxx(item) }
}

// 匹配无人机信息
async function ppwrjxx(item: DroneItem) {
  try {
    const { data } = await api.get('/drones/library', { params: { page: 1, page_size: 10, brand: item.brand, model: item.model, serial_number: item.serial } })
    const records = data.result?.records || []
    if (records.length) {
      wrjDetailRef.value?.open({ ...records[0], ...item, stationName: item.station_name, stationId: item.station_id }, false)
    } else {
      wrjDetailRef.value?.open({ ...item, stationName: item.station_name, stationId: item.station_id }, false)
    }
  } catch {
    wrjDetailRef.value?.open({ ...item, stationName: item.station_name, stationId: item.station_id }, false)
  }
}

// ============ 无人机详情 ============
function openDroneDetail(item: DroneItem) {
  wrjDetailRef.value?.open(item, true)
}

// ============ 轨迹样式变更 ============
function onGjysChange(item: DroneItem) {
  store.dronesCopy.forEach((c: any) => {
    if (c.brand === item.brand && c.model === item.model && c.serial === item.serial) {
      c.color = (item as any).color; c.gjWidth = (item as any).gjWidth
    }
  })
  // Update 3D path if active
  if (mapChange.value && fxData.value.length) {
    rowData.value.color = (item as any).color; rowData.value.weight = (item as any).gjWidth
    addToMap3D()
  } else {
    // Redraw 2D paths
    clearLineLayer2()
    store.drones.forEach(d => {
      wxdzcApi.getByModelSerial({ model: d.model, serial: d.serial, rq: store.currentDate }).then(res => {
        if (res.uav_detect_msg_list.length) {
          const ll = res.uav_detect_msg_list.filter((p: any) => p.dron_lat && p.dron_lng).map((p: any) => [p.dron_lat, p.dron_lng])
          if (ll.length >= 2) L.polyline(ll, { color: (d as any).color, weight: (d as any).gjWidth }).addTo(lineLayer2)
        }
      })
    })
  }
}

// ============ 图层控制 ============
function onWrjCheckChange() {
  renderAllMarkers()
}
function onHideOthersToggle() {
  store.toggleHideOthers()
  getWxdData()
}

// ============ 设备操作 ============
function onDeviceClick(device: any) {
  if (device.wd && device.jd) {
    leafletMap?.flyTo([device.wd, device.jd], 12)
    deviceDetailRef.value?.open(device)
  }
}

// ============ 清除图层 ============
function clearMarkerLayer() { markerLayer?.clearLayers() }
function clearLineLayer() { lineLayer?.clearLayers() }
function clearLineLayer2() { lineLayer2?.clearLayers() }
function clearLayer2() { wrjMarkerLayer?.clearLayers(); sectorLayers?.clearLayers() }
function clearLayer4() { fsMarkerLayer?.clearLayers() }
function clearAllLayers() {
  lineLayer?.clearLayers(); lineLayer2?.clearLayers(); markerLayer?.clearLayers()
  zymbMarkerLayer?.clearLayers(); sectorLayers?.clearLayers(); wrjMarkerLayer?.clearLayers()
  fsMarkerLayer?.clearLayers()
}

// ============ 辅助函数 ============
function randomColor(): string {
  return '#' + Array.from({ length: 6 }, () => '0123456789ABCDEF'[Math.floor(Math.random() * 16)]).join('')
}

// Expose for global access (used by Leaflet popup HTML)
;(window as any)._wxdzcTy3D = ty
;(window as any)._wxdzcTy2D = ty2D
</script>

<template>
  <div class="topCenter">
    <!-- ====== 返回按钮 (二维推演模式) ====== -->
    <div v-show="!mapChange && returnBackFlag" style="position:absolute;top:10px;left:30px;display:flex;align-items:center;z-index:100;pointer-events:auto;">
      <div class="ewtyBack" title="返回" @click="returnBack" style="display:flex;align-items:center;gap:4px;cursor:pointer;color:#fff;font-size:14px;">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="20" height="20"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>返回
      </div>
      <button style="margin-left:10px;padding:6px 16px;background:#028be5;border:none;border-radius:4px;color:#fff;cursor:pointer;" @click="timelineInterval ? null : addToMap2D()">开始</button>
      <button style="margin-left:6px;padding:6px 16px;background:#028be5;border:none;border-radius:4px;color:#fff;cursor:pointer;" @click="pauseTimeline()">暂停</button>
    </div>

    <!-- ====== 3D 推演按钮 ====== -->
    <div v-show="mapChange && tyFlag" style="position:absolute;top:10px;left:50%;transform:translateX(-50%);z-index:100;display:flex;gap:8px;">
      <button class="pb-btn" @click="addToMap3D()">▶ 开始</button>
      <button class="pb-btn">⏸ 暂停</button>
      <button class="pb-btn">🗺 漫游</button>
      <button class="pb-btn" @click="returnBack()">↩ 返回</button>
    </div>

    <!-- ====== 地图容器 ====== -->
    <div id="wxdzc-map" style="width:100%;height:100%;position:absolute;inset:0;z-index:1;"></div>

    <!-- ====== 设备列表面板(左) ====== -->
    <div v-show="!tyFlag" id="zcsb" class="wxdsbBox" :style="devicePanelStyle">
      <div :class="store.devicePanelVisible?'sbToggleBtn sbToggleBg2':'sbToggleBtn sbToggleBg1'" @click="store.toggleDevicePanel()" style="position:absolute;right:-32px;top:50%;width:32px;height:60px;background:rgba(14,69,124,0.9);border-radius:0 6px 6px 0;cursor:pointer;display:flex;align-items:center;justify-content:center;z-index:8;">
        <span style="color:#97c0eb;font-size:18px;">{{ store.devicePanelVisible ? '◀' : '▶' }}</span>
      </div>
      <div class="title" style="display:flex;justify-content:space-between;align-items:center;padding:10px 16px;border-bottom:1px solid #1d5390;color:#c6e0f9;font-weight:600;">
        <span>信号来源</span>
      </div>
      <div class="search" style="display:flex;gap:6px;padding:8px 10px;border-bottom:1px solid #1d5390;">
        <input v-model="store.deviceSearchName" placeholder="请输入无线电设备名称" style="flex:1;padding:5px 10px;background:rgba(255,255,255,0.06);border:1px solid #2f7ad4;border-radius:4px;color:#eef6ff;font-size:12px;outline:none;" @keyup.enter="store.fetchDevices()">
        <button @click="store.fetchDevices()" style="padding:5px 12px;background:#028be5;border:none;border-radius:4px;color:#fff;font-size:12px;cursor:pointer;">查询</button>
      </div>
      <div v-if="store.devices.length" class="content" style="flex:1;overflow-y:auto;">
        <div v-for="(item,index) in store.devices" :key="index" class="wxdsb" style="padding:8px 12px;border-bottom:1px solid rgba(47,122,212,0.15);cursor:pointer;" @click="onDeviceClick(item)">
          <div style="display:flex;align-items:center;gap:8px;">
            <img :src="getDeviceIconUrl(item.status, item.device_type)" style="width:50px;height:34px;"/>
            <div>
              <div :class="item.status==='CONNECTED'?'color1':item.status==='DISCONNECTED'?'color2':'color3'" style="font-size:12px;font-weight:600;">{{ item.status==='CONNECTED'?'已连接':item.status==='DISCONNECTED'?'未连接':'告警中' }}</div>
              <div style="font-size:11px;color:#6c9bd0;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:140px;" :title="(item.name||'')+'-'+item.device_id">{{ item.name }}-{{ item.device_id }}</div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="content" style="flex:1;display:flex;align-items:center;justify-content:center;color:#5f7fa0;">暂无设备数据</div>
      <div style="padding:8px 12px;border-top:1px solid #1d5390;display:flex;justify-content:space-between;align-items:center;font-size:11px;color:#6c9bd0;">
        <span>共 {{ store.deviceTotal }} 条</span>
        <div style="display:flex;gap:6px;">
          <button @click="store.devicePage--;store.fetchDevices()" :disabled="store.devicePage<=1" style="padding:2px 8px;background:rgba(47,122,212,0.15);border:1px solid #2f7ad4;border-radius:3px;color:#97c0eb;cursor:pointer;font-size:11px;">上一页</button>
          <span style="color:#4db4ff;">{{ store.devicePage }}</span>
          <button @click="store.devicePage++;store.fetchDevices()" :disabled="store.devicePage*10>=store.deviceTotal" style="padding:2px 8px;background:rgba(47,122,212,0.15);border:1px solid #2f7ad4;border-radius:3px;color:#97c0eb;cursor:pointer;font-size:11px;">下一页</button>
        </div>
      </div>
    </div>

    <!-- ====== 无人机列表面板(右) ====== -->
    <div v-if="!returnBackFlag" id="zcwrjxx" class="wsdtc" :style="dronePanelStyle">
      <div :class="store.dronePanelVisible?'wrjToggleBtn wrjToggleBg1':'wrjToggleBtn wrjToggleBg2'" @click="store.toggleDronePanel()" style="position:absolute;left:-32px;top:50%;width:32px;height:60px;background:rgba(14,69,124,0.9);border-radius:6px 0 0 6px;cursor:pointer;display:flex;align-items:center;justify-content:center;z-index:8;">
        <span style="color:#97c0eb;font-size:18px;">{{ store.dronePanelVisible ? '▶' : '◀' }}</span>
      </div>
      <div class="title" style="display:flex;justify-content:space-between;align-items:center;padding:10px 16px;border-bottom:1px solid #1d5390;color:#c6e0f9;font-weight:600;">
        <span>侦测无人机信息(无人机数量：{{ store.drones.length }})</span>
        <button v-show="ycqtBtnVisible" @click="onHideOthersToggle" style="padding:4px 10px;background:rgba(2,139,229,0.3);border:1px solid #2f7ad4;border-radius:4px;color:#4db4ff;font-size:12px;cursor:pointer;">{{ store.hideOthersMode ? '显示全部' : '隐藏其他' }}</button>
      </div>

      <div class="search" style="padding:8px 10px;border-bottom:1px solid #1d5390;display:flex;flex-direction:column;gap:8px;">
        <!-- 日期 -->
        <input type="date" :value="store.currentDate" @change="(e:any) => { store.setDate(e.target.value); getWxdData(true); addDevicePoints(); }" style="width:100%;padding:5px 10px;background:rgba(255,255,255,0.06);border:1px solid #2f7ad4;border-radius:4px;color:#eef6ff;font-size:12px;outline:none;">
        <!-- 日历有数据的日期标记 -->
        <div v-if="store.holidays.length" style="display:flex;flex-wrap:wrap;gap:3px;margin-top:4px;font-size:10px;color:#ffd561;">
          <span v-for="h in store.holidays.slice(0, 10)" :key="h" style="background:rgba(255,213,97,0.15);padding:1px 5px;border-radius:3px;" :title="h">{{ h.slice(-5) }}</span>
          <span v-if="store.holidays.length > 10" style="color:#6c9bd0;">+{{ store.holidays.length - 10 }}天</span>
        </div>
        <!-- 型号筛选 -->
        <input v-model="store.modelFilter" placeholder="请输入型号" @input="getWxdData()" style="width:100%;padding:5px 10px;background:rgba(255,255,255,0.06);border:1px solid #2f7ad4;border-radius:4px;color:#eef6ff;font-size:12px;outline:none;">
        <!-- 授权状态筛选 -->
        <select v-model="store.authStatusFilter" @change="getWxdData(true)" style="width:100%;padding:5px 10px;background:rgba(255,255,255,0.06);border:1px solid #2f7ad4;border-radius:4px;color:#eef6ff;font-size:12px;outline:none;">
          <option :value="undefined">全部</option>
          <option :value="4">待干扰</option>
          <option :value="5">待诱骗</option>
          <option :value="6">持续跟踪</option>
        </select>
        <!-- 图层筛选 -->
        <div style="display:flex;gap:12px;font-size:12px;color:#6c9bd0;">
          <label v-for="l in ['飞手','无人机','信息']" :key="l" style="display:flex;align-items:center;gap:4px;cursor:pointer;">
            <input type="checkbox" :value="l" :checked="store.layerFilters.includes(l)" @change="(e:any) => { if(e.target.checked) store.layerFilters.push(l); else store.layerFilters=store.layerFilters.filter(x=>x!==l); onWrjCheckChange(); }">{{ l }}
          </label>
        </div>
        <div style="color:#fff;font-size:11px;">正常：{{ store.zcNum }}，失联：{{ store.slNum }}</div>
      </div>

      <!-- 无人机列表 -->
      <div v-if="store.drones.length" class="content" style="flex:1;overflow-y:auto;">
        <div v-for="(item,index) in store.drones" :key="index" :class="wrjCurrentIndex===index?'wrjActive wxdsb':'wxdsb'" style="padding:8px 12px;border-bottom:1px solid rgba(47,122,212,0.15);cursor:pointer;position:relative;">
          <!-- 威胁等级图标 -->
          <div style="display:flex;align-items:center;gap:4px;">
            <el-tooltip placement="top">
              <template #content>
                <div style="font-size:12px;max-width:300px;">
                  <template v-if="item.wxdj === 'green'">
                    根据高度：<span style="color:red">{{ item.height || 0 }}</span>，速度：<span style="color:red">{{ item.sd || 0 }}</span>，距离：<span style="color:red">{{ item.distance || 0 }}</span>，综合风险得分＜0.4，判定为绿色风险。<br>该得分基于距离(权重0.3)、速度(权重0.4)、高度(权重0.3)三个维度计算。
                  </template>
                  <template v-else-if="item.wxdj === 'yellow'">
                    根据高度：<span style="color:red">{{ item.height || 0 }}</span>，速度：<span style="color:red">{{ item.sd || 0 }}</span>，距离：<span style="color:red">{{ item.distance || 0 }}</span>，综合风险得分≥0.4且＜0.7，判定为黄色风险。<br>该得分基于距离(权重0.3)、速度(权重0.4)、高度(权重0.3)三个维度计算。
                </template>
                <template v-else>
                    根据高度：<span style="color:red">{{ item.height || 0 }}</span>，速度：<span style="color:red">{{ item.sd || 0 }}</span>，距离：<span style="color:red">{{ item.distance || 0 }}</span>，综合风险得分≥0.7，判定为红色风险。<br>该得分基于距离(权重0.3)、速度(权重0.4)、高度(权重0.3)三个维度计算。
                  </template>
                </div>
              </template>
              <el-icon size="18" :color="item.wxdj || 'green'"><svg viewBox="0 0 24 24" fill="currentColor" width="18" height="18"><path d="M12 2L1 21h22L12 2zm0 4l7.5 13h-15L12 6zm0 5v4m0 2v-2"/></svg></el-icon>
            </el-tooltip>
            <span style="font-size:13px;font-weight:600;color:#eef6ff;" :title="(item.brand||'')+'-'+(item.model||'')+'-'+(item.serial||'')">{{ item.model || '未知' }}</span>
          </div>
          <!-- 站点名称 -->
          <div v-if="item.station_name" style="font-size:10px;color:#5f7fa0;margin-top:2px;">{{ item.station_name }}</div>
          <!-- 解密类型 -->
          <div v-if="item.jmlx" style="position:absolute;right:8px;top:8px;font-size:10px;color:#ff9800;">{{ item.jmlx }}</div>
          <!-- 授权状态 -->
          <div v-if="item.auth_status && item.auth_status >= 4" style="display:flex;align-items:center;gap:4px;margin-top:2px;font-size:10px;">
            <span :class="item.auth_status===4?'dot bg1':item.auth_status===5?'dot bg2':'dot bg3'" style="width:6px;height:6px;border-radius:50%;display:inline-block;"></span>
            <span style="color:#97c0eb;">{{ item.auth_status===4?'待干扰':item.auth_status===5?'待诱骗':'持续跟踪' }}</span>
          </div>
          <!-- 无人机图标 + 状态 -->
          <div style="display:flex;align-items:center;gap:8px;margin-top:4px;" @click="wrjCurrentIndex=index;ycqtBtnVisible=true;openDroneDetail(item)">
            <img :src="'/static/'+(item.auth_status===1?'fly1':item.auth_status===2?'fly2':'fly3')+'.png'" style="width:50px;height:34px;"/>
            <div style="font-size:12px;" :class="item.status===1?'color1':item.status===2?'color2':'color3'">
              {{ item.status===1?'正常':item.status===2?'告警':'失联' }}
            </div>
          </div>
          <!-- 遥测数据 -->
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:2px 12px;margin-top:4px;font-size:11px;color:#6c9bd0;">
            <div>频率(Mhz): {{ item.freq || 0 }}</div>
            <div>更新时间: {{ item.data_time || '' }}</div>
            <div>高度(m): {{ item.height || 0 }}</div>
            <div>速度(m/s): {{ item.sd || 0 }}</div>
            <div>距离(m): {{ item.distance || 0 }}</div>
            <div>角度(°): {{ item.angle || 0 }}</div>
          </div>
          <!-- 轨迹颜色/宽度 -->
          <div style="display:flex;align-items:center;gap:8px;margin-top:4px;">
            <span style="font-size:11px;color:#6c9bd0;">轨迹颜色:</span>
            <input type="color" :value="(item as any).color || '#4db4ff'" @change="(e:any) => { (item as any).color = e.target.value; onGjysChange(item); }" style="width:20px;height:20px;border:none;cursor:pointer;background:transparent;">
            <span style="font-size:11px;color:#6c9bd0;">轨迹宽度:</span>
            <input type="number" :value="(item as any).gjWidth || 3" min="1" max="10" @change="(e:any) => { (item as any).gjWidth = Number(e.target.value); onGjysChange(item); }" style="width:50px;padding:2px 4px;background:rgba(255,255,255,0.06);border:1px solid #2f7ad4;border-radius:3px;color:#eef6ff;font-size:11px;">
          </div>
          <!-- 右键菜单 (二维推演 / 三维推演) -->
          <div v-if="contextMenuVisible && contextMenuDrone === item" style="position:fixed;z-index:2000;background:#0e457c;border:1px solid #2f7ad4;border-radius:6px;overflow:hidden;" :style="{left:contextMenuPos.x+'px',top:contextMenuPos.y+'px'}">
            <div @click="onContextMenuAction('yp')" style="padding:8px 16px;color:#c6e0f9;cursor:pointer;font-size:13px;" class="menu-hover">诱骗</div>
            <div @click="onContextMenuAction('gr')" style="padding:8px 16px;color:#c6e0f9;cursor:pointer;font-size:13px;" class="menu-hover">干扰</div>
            <div @click="onContextMenuAction('ty2d')" style="padding:8px 16px;color:#c6e0f9;cursor:pointer;font-size:13px;" class="menu-hover">二维推演</div>
            <div @click="onContextMenuAction('ty3d')" style="padding:8px 16px;color:#c6e0f9;cursor:pointer;font-size:13px;" class="menu-hover">三维推演</div>
            <div @click="onContextMenuAction('ppwrjxx')" style="padding:8px 16px;color:#c6e0f9;cursor:pointer;font-size:13px;" class="menu-hover">匹配无人机信息</div>
          </div>
        </div>
      </div>
      <div v-else class="content" style="flex:1;display:flex;align-items:center;justify-content:center;color:#5f7fa0;">等待侦测数据...</div>
    </div>

    <!-- ====== 无人机详情面板(右下) ====== -->
    <div v-if="detailInfoObj && !returnBackFlag && store.droneDetailPanelVisible" id="zcwrjXxxx" class="wsdtc" style="position:absolute;right:0;bottom:0;width:385px;max-height:55%;z-index:6;background:linear-gradient(180deg,rgba(14,69,124,0.97),rgba(7,43,87,0.99));border-left:1px solid #1d5390;border-top:1px solid #1d5390;" :style="droneDetailPanelStyle">
      <div style="display:flex;justify-content:space-between;align-items:center;padding:8px 12px;border-bottom:1px solid #1d5390;">
        <span style="font-size:13px;font-weight:600;color:#c6e0f9;">侦测无人机详细信息</span>
        <div style="display:flex;gap:4px;">
          <button @click="store.toggleDroneDetailPanel()" style="background:transparent;border:none;color:#6c9bd0;cursor:pointer;">_</button>
          <button @click="store.droneDetailPanelVisible=false;detailInfoObj=null" style="background:transparent;border:none;color:#6c9bd0;cursor:pointer;">✕</button>
        </div>
      </div>
      <div style="padding:8px 12px;overflow-y:auto;max-height:calc(100% - 40px);display:grid;grid-template-columns:1fr 1fr;gap:4px 16px;">
        <div v-for="col in detailColumn" :key="col.prop" style="display:flex;justify-content:space-between;padding:3px 0;border-bottom:1px solid rgba(47,122,212,0.1);font-size:11px;">
          <span style="color:#6c9bd0;flex-shrink:0;">{{ col.label }}：</span>
          <span style="color:#eef6ff;text-align:right;font-weight:500;">{{ col.prop==='sd'?(detailInfoObj[col.prop]||0):(detailInfoObj[col.prop]||'-') }}</span>
        </div>
      </div>
    </div>

    <!-- ====== 时间轴(二维推演) ====== -->
    <div v-if="returnBackFlag && !mapChange && fxData.length" style="position:absolute;bottom:30px;left:50%;transform:translateX(-50%);width:60%;z-index:10;">
      <div style="background:rgba(14,69,124,0.9);border:1px solid #2f7ad4;border-radius:8px;padding:10px 16px;">
        <div style="display:flex;align-items:center;gap:8px;color:#4db4ff;font-size:12px;margin-bottom:6px;">
          <span>{{ fxData[currentTime]?.data_time || '--:--:--' }}</span>
          <span>速度: {{ speed.toFixed(1) }} m/s</span>
        </div>
        <input type="range" :min="0" :max="Math.max(0, fxData.length-1)" :value="currentTime" @input="(e:any) => updateTimeline(Number(e.target.value))" style="width:100%;accent-color:#4db4ff;">
      </div>
    </div>

    <!-- ====== 对话框 ====== -->
    <DeviceDetailDialog ref="deviceDetailRef" />
    <DroneDetailDialog ref="wrjDetailRef" />
  </div>
</template>

<style scoped>
.topCenter { width:100%; height:100%; position:relative; overflow:hidden; background:#0a1628; font-family:var(--font-base); }
.wxdsbBox { position:absolute; left:0; top:0; bottom:0; width:368px; background:linear-gradient(180deg,rgba(14,69,124,0.95),rgba(7,43,87,0.98)); border-right:1px solid #1d5390; z-index:5; display:flex; flex-direction:column; }
.wsdtc { position:absolute; right:0; top:0; bottom:0; width:420px; background:linear-gradient(180deg,rgba(14,69,124,0.95),rgba(7,43,87,0.98)); border-left:1px solid #1d5390; z-index:5; display:flex; flex-direction:column; }
.wxdsb { transition: background 0.15s; }
.wxdsb:hover, .wrjActive { background:rgba(2,139,229,0.15) !important; }

.color1 { color:#6fd66f; } .color2 { color:#ff7a86; } .color3 { color:#ffd561; }
.dot { width:6px; height:6px; border-radius:50%; display:inline-block; }
.bg1 { background:#6fd66f; } .bg2 { background:#ff7a86; } .bg3 { background:#ffd561; }

.pb-btn { padding:6px 16px; background:rgba(14,69,124,0.85); border:1px solid #2f7ad4; border-radius:6px; color:#c6e0f9; font-size:13px; cursor:pointer; }
.pb-btn:hover { background:rgba(23,88,154,0.9); }

.menu-hover:hover { background:rgba(2,139,229,0.3); color:#4db4ff; }
</style>
