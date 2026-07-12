<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { deviceApi } from '@/api/dataManage'
import axios from 'axios'
import type { DeviceConfig, DroneDetectMsg } from '@/types'

let Cesium: any = (window as any).Cesium

// ==================== STATE ====================
const loading = ref(true)
const mapContainer = ref<HTMLDivElement>()
const panelLeftVisible = ref(true)
const panelRightVisible = ref(true)

// Device data
const devices = ref<DeviceConfig[]>([])
const devicePage = ref({ pageNo: 1, pageSize: 10 })
const deviceTotal = ref(0)
const deviceSearch = ref('')

// UAV detection data
const detectMsgs = ref<DroneDetectMsg[]>([])
const detectMsgsCopy = ref<any[]>([])
const wrjFilter = ref({ rq: '', model: '', authStatus: '' })
const layerChecks = ref(['飞手', '无人机', '信息'])
const zcNum = ref(0); const gjNum = ref(0); const slNum = ref(0)
const selectedUavIdx = ref(-1)

// Map
let viewer: any = null
let entityCollection: any = null
let deviceMarkerEntities: any[] = []
let uavMarkerEntities: any[] = []
let pilotMarkerEntities: any[] = []
let sectorEntities: any[] = []
let pollTimer: number | undefined

// Cesium init
onMounted(async () => {
  let retries = 0
  while (!(window as any).Cesium && retries < 10) { await new Promise(r => setTimeout(r, 300)); retries++ }
  if (!(window as any).Cesium) { loading.value = false; return }
  Cesium = (window as any).Cesium

  const TILE_URL = window.location.origin + '/api/v1/tiles/{z}/{y}/{x}'

  viewer = new Cesium.Viewer(mapContainer.value!, {
    baseLayer: false,
    animation: false, timeline: false, fullscreenButton: false,
    baseLayerPicker: false, geocoder: false, homeButton: false,
    sceneModePicker: false, navigationHelpButton: false,
    infoBox: false, selectionIndicator: false,
    terrainProvider: new Cesium.EllipsoidTerrainProvider(),
    backgroundColor: Cesium.Color.fromCssColorString('#060b18'),
    scene3DOnly: true,
  })
  viewer.imageryLayers.addImageryProvider(
    new Cesium.UrlTemplateImageryProvider({ url: TILE_URL, maximumLevel: 19 })
  )
  viewer.scene.globe.baseColor = Cesium.Color.fromCssColorString('#0a1628')
  viewer.scene.globe.enableLighting = false
  viewer.scene.skyAtmosphere.show = true
  viewer.scene.fog.enabled = false
  viewer.scene.skyBox.show = false
  viewer.scene.sun.show = false
  entityCollection = new Cesium.CustomDataSource('detection')
  viewer.dataSources.add(entityCollection)
  viewer.camera.setView({ destination: Cesium.Cartesian3.fromDegrees(108.94, 34.08, 15000) })

  loading.value = false
  refreshAll()
  pollTimer = window.setInterval(refreshAll, 6000)

  // Click handler
  const handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas)
  handler.setInputAction((click: any) => {
    const picked = viewer.scene.pick(click.position)
    if (picked?.id?._id?.startsWith?.('uav-')) {
      const idx = parseInt(picked.id._id.split('-')[1])
      selectedUavIdx.value = idx
    }
  }, Cesium.ScreenSpaceEventType.LEFT_CLICK)
})

onUnmounted(() => { clearInterval(pollTimer); viewer?.destroy() })

// ==================== DATA FETCHING ====================
async function refreshAll() {
  await Promise.all([fetchDevices(), fetchDetectionData()])
}

async function fetchDevices() {
  try {
    const data = await deviceApi.list({ page: devicePage.value.pageNo, page_size: devicePage.value.pageSize })
    devices.value = data.records || []
    deviceTotal.value = data.total || 0
    updateDeviceMarkers()
  } catch {}
}

async function fetchDetectionData() {
  try {
    const resp = await axios.get('/api/v1/flight-data/detect-msg', {
      params: { page: 1, page_size: 200 }
    })
    const msgs = resp.data?.result?.records || []
    // Apply local filters
    let filtered = msgs
    if (wrjFilter.value.model) {
      filtered = filtered.filter((v: any) => (v.model || '').toLowerCase().includes(wrjFilter.value.model.toLowerCase()))
    }
    detectMsgs.value = filtered
    zcNum.value = filtered.length
    updateUavMarkers()
  } catch {}
}

// ==================== RISK SCORING ====================
function calcRisk(t: any): { score: number; level: string; color: string } {
  const speed = Math.sqrt((t.east_v || 0) ** 2 + (t.north_v || 0) ** 2) || (t.sd || 0)
  const alt = t.altitude || t.height || 100
  const dist = t.distance || 1
  const speedNorm = Math.min(speed / 50, 1)
  const altNorm = 1 - Math.min(alt / 500, 1)
  const distNorm = Math.min(dist / 10, 1)
  const score = distNorm * 0.3 + speedNorm * 0.4 + altNorm * 0.3
  const level = score >= 0.7 ? 'RED' : score >= 0.4 ? 'YELLOW' : 'GREEN'
  const color = level === 'RED' ? '#ef5350' : level === 'YELLOW' ? '#ffd54f' : '#66bb6a'
  return { score: Math.round(score * 10000) / 10000, level, color }
}

// ==================== DEVICE MARKERS ====================
function getDeviceIcon(deviceType: string, status: string): string {
  const base = { DETECT: 'radar', TRAP: 'spoof', DISTURB: 'jammer', System: 'system' }[deviceType] || 'device'
  const suffix = status === 'CONNECTED' ? '' : status === 'WARN' ? '_warn' : '_off'
  return base + suffix
}
function createDeviceCanvasIcon(deviceType: string, isOnline: boolean): string {
  const c = document.createElement('canvas'); c.width = 56; c.height = 56
  const ctx = c.getContext('2d')!
  const colors: Record<string, string> = { DETECT: '#4db4ff', TRAP: '#ffb74d', DISTURB: '#ef5350', System: '#81c784' }
  const color = isOnline ? (colors[deviceType] || '#999') : '#666'
  // Antenna tower
  ctx.fillStyle = color; ctx.beginPath(); ctx.arc(28, 36, 14, 0, Math.PI * 2); ctx.fill()
  ctx.strokeStyle = isOnline ? '#fff' : '#888'; ctx.lineWidth = 2; ctx.stroke()
  ctx.fillStyle = isOnline ? '#fff' : '#888'
  ctx.fillRect(26, 8, 4, 30)
  ctx.fillRect(20, 16, 16, 3)
  // Signal arcs
  ctx.strokeStyle = color; ctx.lineWidth = 2
  ctx.beginPath(); ctx.arc(28, 22, 10, -Math.PI * 0.7, -Math.PI * 0.3); ctx.stroke()
  ctx.beginPath(); ctx.arc(28, 22, 15, -Math.PI * 0.75, -Math.PI * 0.25); ctx.stroke()
  if (isOnline) {
    ctx.fillStyle = '#0f0'; ctx.beginPath(); ctx.arc(48, 8, 5, 0, Math.PI * 2); ctx.fill()
    ctx.fillStyle = 'rgba(0,255,0,0.3)'; ctx.beginPath(); ctx.arc(48, 8, 9, 0, Math.PI * 2); ctx.fill()
  }
  return c.toDataURL('image/png')
}

function updateDeviceMarkers() {
  if (!entityCollection) return
  deviceMarkerEntities.forEach(e => { if (!e.isDestroyed?.()) entityCollection.entities.remove(e) })
  deviceMarkerEntities = []
  devices.value.filter(d => d.status === 'CONNECTED' && d.jd && d.wd).forEach(d => {
    const lon = d.jd!, lat = d.wd!, r = parseFloat(d.zcbj || '5') * 1000
    const icon = createDeviceCanvasIcon(d.device_type, d.status === 'CONNECTED')
    const ent = entityCollection.entities.add({
      id: `dev-${d.device_id}`,
      position: Cesium.Cartesian3.fromDegrees(lon, lat, d.gd || 10),
      billboard: { image: icon, scale: 0.8, verticalOrigin: Cesium.VerticalOrigin.BOTTOM, disableDepthTestDistance: 50000 },
      label: { text: d.name || d.device_id, font: '11px "Microsoft YaHei"', fillColor: Cesium.Color.WHITE, outlineColor: Cesium.Color.BLACK, outlineWidth: 1, pixelOffset: new Cesium.Cartesian2(0, -28), disableDepthTestDistance: 50000 },
    })
    // Detection radius circle
    const circle = entityCollection.entities.add({
      id: `dev-circle-${d.device_id}`,
      position: Cesium.Cartesian3.fromDegrees(lon, lat, 1),
      ellipse: { semiMinorAxis: r, semiMajorAxis: r, material: Cesium.Color.fromCssColorString('#4db4ff').withAlpha(0.04), outline: true, outlineColor: Cesium.Color.fromCssColorString('#4db4ff').withAlpha(0.25), outlineWidth: 1 },
    })
    deviceMarkerEntities.push(ent, circle)
  })
}

// ==================== UAV MARKERS ====================
function createUavIcon(isRisk: boolean, riskColor: string): string {
  const c = document.createElement('canvas'); c.width = 40; c.height = 40
  const ctx = c.getContext('2d')!
  ctx.fillStyle = isRisk ? riskColor : '#aaa'
  ctx.beginPath(); ctx.moveTo(20, 4); ctx.lineTo(30, 18); ctx.lineTo(24, 18)
  ctx.lineTo(28, 34); ctx.lineTo(20, 28); ctx.lineTo(12, 34)
  ctx.lineTo(16, 18); ctx.lineTo(10, 18); ctx.closePath(); ctx.fill()
  ctx.strokeStyle = '#fff'; ctx.lineWidth = 1.5; ctx.stroke()
  if (isRisk) {
    ctx.strokeStyle = riskColor; ctx.lineWidth = 2; ctx.beginPath(); ctx.arc(20, 20, 16, 0, Math.PI * 2); ctx.stroke()
  }
  return c.toDataURL('image/png')
}

function updateUavMarkers() {
  if (!entityCollection) return
  uavMarkerEntities.forEach(e => { if (!e.isDestroyed?.()) entityCollection.entities.remove(e) })
  pilotMarkerEntities.forEach(e => { if (!e.isDestroyed?.()) entityCollection.entities.remove(e) })
  uavMarkerEntities = []; pilotMarkerEntities = []

  detectMsgs.value.forEach((d, i) => {
    if (!d.dron_lat || !d.dron_lng) return
    const risk = calcRisk(d); const isHigh = risk.level === 'RED'; const isMed = risk.level === 'YELLOW'
    const icon = createUavIcon(isHigh || isMed, risk.color)
    const alt = d.altitude || d.height || 100

    // UAV marker
    const uav = entityCollection.entities.add({
      id: `uav-${i}`,
      position: Cesium.Cartesian3.fromDegrees(d.dron_lng, d.dron_lat, alt),
      billboard: { image: icon, scale: isHigh ? 1.1 : isMed ? 0.9 : 0.7, verticalOrigin: Cesium.VerticalOrigin.CENTER, disableDepthTestDistance: 50000 },
      label: { text: `${d.model || 'UAV'} [${isHigh ? '高危' : isMed ? '中危' : '低危'}]`, font: '12px "Microsoft YaHei"', fillColor: Cesium.Color.WHITE, outlineColor: Cesium.Color.BLACK, outlineWidth: 1, pixelOffset: new Cesium.Cartesian2(0, -24), disableDepthTestDistance: 50000, showBackground: true, backgroundColor: Cesium.Color.fromCssColorString('#001529').withAlpha(0.7) },
    })
    uavMarkerEntities.push(uav)

    // Pilot marker
    if (layerChecks.value.includes('飞手') && d.pilot_lat && d.pilot_lng) {
      const pilot = entityCollection.entities.add({
        id: `pilot-${i}`,
        position: Cesium.Cartesian3.fromDegrees(d.pilot_lng, d.pilot_lat, 5),
        billboard: { image: createPilotIcon(), scale: 0.6, verticalOrigin: Cesium.VerticalOrigin.CENTER, disableDepthTestDistance: 50000 },
        label: { text: '飞手', font: '10px "Microsoft YaHei"', fillColor: Cesium.Color.fromCssColorString('#ffb74d'), outlineColor: Cesium.Color.BLACK, outlineWidth: 1, pixelOffset: new Cesium.Cartesian2(0, -16) },
      })
      pilotMarkerEntities.push(pilot)
    }
  })
}

function createPilotIcon(): string {
  const c = document.createElement('canvas'); c.width = 32; c.height = 32
  const ctx = c.getContext('2d')!
  ctx.fillStyle = '#ffb74d'; ctx.beginPath(); ctx.arc(16, 12, 8, 0, Math.PI * 2); ctx.fill()
  ctx.fillStyle = '#ffb74d'; ctx.beginPath(); ctx.ellipse(16, 24, 6, 8, 0, Math.PI, 0); ctx.fill()
  ctx.strokeStyle = '#fff'; ctx.lineWidth = 1.5; ctx.stroke()
  return c.toDataURL('image/png')
}

// ==================== CONTEXT MENU ACTIONS ====================
function actionSpoof(msg: any) { console.log('诱骗:', msg) }
function actionJam(msg: any) { console.log('干扰:', msg) }
function action2DPush(msg: any) { console.log('二维推演:', msg) }
function action3DPush(msg: any) { console.log('三维推演:', msg) }
</script>

<template>
  <div class="wxdzc-page">
    <!-- Loading -->
    <div v-if="loading" class="loading-overlay"><div class="loader-spin"></div><span>加载中...</span></div>

    <!-- CESIUM MAP -->
    <div ref="mapContainer" class="cesium-map"></div>

    <!-- ===== LEFT: Device Panel ===== -->
    <div class="panel-left" :class="{ collapsed: !panelLeftVisible }">
      <div class="panel-toggle" @click="panelLeftVisible = !panelLeftVisible">
        {{ panelLeftVisible ? '◀' : '▶' }}
      </div>
      <template v-if="panelLeftVisible">
        <div class="panel-header">📡 信号来源</div>
        <div class="search-bar">
          <input v-model="deviceSearch" placeholder="搜索设备..." @keyup.enter="fetchDevices" />
          <button @click="fetchDevices">查询</button>
        </div>
        <div class="device-grid">
          <div v-for="d in devices" :key="d.device_id" class="device-card"
               :class="{ online: d.status === 'CONNECTED', warn: d.status === 'WARN' }"
               @click="viewer?.camera.flyTo({ destination: Cesium.Cartesian3.fromDegrees(d.jd || 108.94, d.wd || 34.08, 5000) })">
            <div class="dc-icon">
              <span class="dc-dot" :class="{ on: d.status === 'CONNECTED', off: d.status !== 'CONNECTED' }"></span>
            </div>
            <div class="dc-info">
              <div class="dc-name">{{ d.name || d.device_id }}</div>
              <div class="dc-type">{{ { DETECT: '侦测', TRAP: '诱骗', DISTURB: '干扰', System: '系统' }[d.device_type] || d.device_type }}</div>
              <div class="dc-status" :class="d.status === 'CONNECTED' ? 'green' : d.status === 'WARN' ? 'yellow' : 'red'">
                {{ d.status === 'CONNECTED' ? '已连接' : d.status === 'WARN' ? '告警中' : '未连接' }}
              </div>
            </div>
          </div>
        </div>
        <div class="pager">
          <button @click="devicePage.pageNo--; fetchDevices()" :disabled="devicePage.pageNo <= 1">◀</button>
          <span>{{ devicePage.pageNo }} / {{ Math.ceil(deviceTotal / devicePage.pageSize) || 1 }}</span>
          <button @click="devicePage.pageNo++; fetchDevices()" :disabled="devicePage.pageNo * devicePage.pageSize >= deviceTotal">▶</button>
        </div>
      </template>
    </div>

    <!-- ===== RIGHT: UAV Detection Panel ===== -->
    <div class="panel-right" :class="{ collapsed: !panelRightVisible }">
      <div class="panel-toggle" @click="panelRightVisible = !panelRightVisible">
        {{ panelRightVisible ? '▶' : '◀' }}
      </div>
      <template v-if="panelRightVisible">
        <div class="panel-header">
          🚁 侦测目标
          <span class="count-badge">{{ detectMsgs.length }}</span>
        </div>
        <!-- Filters -->
        <div class="filters">
          <input type="date" v-model="wrjFilter.rq" @change="fetchDetectionData" class="date-input" />
          <input v-model="wrjFilter.model" placeholder="型号筛选" @change="fetchDetectionData" class="model-input" />
          <select v-model="wrjFilter.authStatus" @change="fetchDetectionData" class="auth-select">
            <option value="">全部</option><option value="4">待干扰</option><option value="5">待诱骗</option><option value="6">持续跟踪</option>
          </select>
        </div>
        <div class="layer-checks">
          <label><input type="checkbox" value="飞手" v-model="layerChecks" @change="updateUavMarkers" /> 飞手</label>
          <label><input type="checkbox" value="无人机" v-model="layerChecks" @change="updateUavMarkers" checked /> 无人机</label>
          <label><input type="checkbox" value="信息" v-model="layerChecks" @change="updateUavMarkers" /> 信息</label>
          <span class="counts">正常:{{ zcNum }} 失联:{{ slNum }}</span>
        </div>
        <!-- UAV List -->
        <div class="uav-list">
          <div v-for="(d, i) in detectMsgs.slice(0, 50)" :key="i" class="uav-item"
               :class="{ selected: selectedUavIdx === i }"
               @click="selectedUavIdx = i; viewer?.camera.flyTo({ destination: Cesium.Cartesian3.fromDegrees(d.dron_lng || 108.94, d.dron_lat || 34.08, 3000) })">
            <span class="risk-dot" :style="{ background: calcRisk(d).color }"></span>
            <div class="uav-info">
              <div class="uav-model">{{ d.model || 'Unknown' }}</div>
              <div class="uav-telemetry">
                高度{{ (d.altitude || d.height || 0).toFixed(0) }}m
                · {{ (d.sd || 0).toFixed(1) }}m/s
                · {{ (d.distance || 0).toFixed(1) }}km
                · {{ (d.freq || 0) / 1e6 }}MHz
              </div>
            </div>
            <span class="risk-label" :style="{ color: calcRisk(d).color, background: calcRisk(d).color + '22' }">
              {{ calcRisk(d).level === 'RED' ? '高危' : calcRisk(d).level === 'YELLOW' ? '中危' : '低危' }}
            </span>
            <!-- Actions -->
            <div class="uav-actions">
              <button @click.stop="actionSpoof(d)" title="诱骗">🛡️</button>
              <button @click.stop="actionJam(d)" title="干扰">⚡</button>
              <button @click.stop="action2DPush(d)" title="推演">📊</button>
            </div>
          </div>
          <div v-if="!detectMsgs.length" class="empty">等待侦测数据...</div>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.wxdzc-page { width: 100%; height: 100%; position: relative; overflow: hidden; background: #060b18; }
.loading-overlay { position: absolute; inset: 0; display: flex; flex-direction: column; gap: 12px; align-items: center; justify-content: center; background: #060b18; color: #8ba0b5; z-index: 999; }
.loader-spin { width: 32px; height: 32px; border: 3px solid #1e3a5c; border-top-color: #4db4ff; border-radius: 50%; animation: spin 1s infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.cesium-map { width: 100%; height: 100%; }
/* ===== LEFT PANEL ===== */
.panel-left, .panel-right { position: absolute; top: 10px; bottom: 10px; width: 340px; background: rgba(10, 22, 40, 0.92); border: 1px solid #1e3a5c; backdrop-filter: blur(12px); z-index: 10; display: flex; flex-direction: column; transition: transform 0.3s; }
.panel-left { left: 10px; border-radius: 0 12px 12px 0; }
.panel-left.collapsed { transform: translateX(-330px); }
.panel-right { right: 10px; border-radius: 12px 0 0 12px; }
.panel-right.collapsed { transform: translateX(330px); }
.panel-toggle { position: absolute; top: 12px; width: 24px; height: 48px; display: flex; align-items: center; justify-content: center; background: rgba(30, 58, 92, 0.8); cursor: pointer; color: #4db4ff; font-size: 12px; border-radius: 4px; z-index: 2; }
.panel-left .panel-toggle { right: -24px; border-radius: 0 6px 6px 0; }
.panel-right .panel-toggle { left: -24px; border-radius: 6px 0 0 6px; }
.panel-header { padding: 10px 14px; font-size: 14px; font-weight: 600; color: #c6e0f9; border-bottom: 1px solid #1e3a5c; }
.count-badge { margin-left: auto; font-size: 11px; background: #4db4ff22; color: #4db4ff; padding: 2px 8px; border-radius: 10px; }
.search-bar { padding: 8px 10px; display: flex; gap: 6px; }
.search-bar input { flex: 1; padding: 4px 8px; border: 1px solid #1e3a5c; border-radius: 4px; background: #071526; color: #c6e0f9; font-size: 12px; }
.search-bar button { padding: 4px 10px; background: #4db4ff; border: none; border-radius: 4px; color: #fff; cursor: pointer; font-size: 12px; }
.device-grid { flex: 1; overflow-y: auto; padding: 8px; display: grid; grid-template-columns: 1fr 1fr; gap: 6px; }
.device-card { padding: 8px; border: 1px solid #1e3a5c; border-radius: 8px; cursor: pointer; font-size: 11px; transition: all 0.2s; }
.device-card:hover { border-color: #4db4ff; background: rgba(77, 180, 255, 0.08); }
.device-card.online { border-left: 3px solid #6fd66f; }
.device-card.warn { border-left: 3px solid #ffd561; }
.dc-dot { display: inline-block; width: 8px; height: 8px; border-radius: 50%; }
.dc-dot.on { background: #6fd66f; box-shadow: 0 0 6px #6fd66f; }
.dc-dot.off { background: #666; }
.dc-name { color: #c6e0f9; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.dc-type { color: #6c9bd0; font-size: 10px; }
.dc-status { font-size: 10px; margin-top: 2px; }
.dc-status.green { color: #6fd66f; } .dc-status.yellow { color: #ffd561; } .dc-status.red { color: #ff7a86; }
.pager { padding: 6px; display: flex; justify-content: center; gap: 8px; align-items: center; font-size: 11px; color: #8ba0b5; }
.pager button { padding: 2px 8px; background: #1e3a5c; border: none; border-radius: 4px; color: #c6e0f9; cursor: pointer; }
.pager button:disabled { opacity: 0.4; }
/* ===== RIGHT PANEL ===== */
.filters { padding: 6px 10px; display: flex; gap: 4px; flex-wrap: wrap; }
.date-input, .model-input, .auth-select { padding: 3px 6px; border: 1px solid #1e3a5c; border-radius: 4px; background: #071526; color: #c6e0f9; font-size: 11px; }
.date-input { width: 120px; } .model-input { flex: 1; min-width: 60px; } .auth-select { width: 80px; }
.layer-checks { padding: 4px 10px; display: flex; gap: 8px; align-items: center; font-size: 11px; color: #8ba0b5; }
.layer-checks label { display: flex; align-items: center; gap: 2px; cursor: pointer; }
.counts { margin-left: auto; font-size: 10px; }
.uav-list { flex: 1; overflow-y: auto; padding: 4px 8px; }
.uav-item { padding: 6px 8px; border: 1px solid #1e3a5c; border-radius: 6px; margin-bottom: 4px; display: flex; align-items: center; gap: 8px; cursor: pointer; transition: all 0.15s; position: relative; }
.uav-item:hover { border-color: #4db4ff; background: rgba(77, 180, 255, 0.05); }
.uav-item.selected { border-color: #4db4ff; background: rgba(77, 180, 255, 0.12); }
.risk-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.uav-info { flex: 1; min-width: 0; }
.uav-model { font-size: 12px; color: #c6e0f9; font-weight: 500; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.uav-telemetry { font-size: 9px; color: #6c9bd0; }
.risk-label { font-size: 10px; font-weight: 600; padding: 2px 6px; border-radius: 3px; }
.uav-actions { display: none; gap: 2px; }
.uav-item:hover .uav-actions { display: flex; }
.uav-actions button { width: 24px; height: 24px; border: none; background: #1e3a5c; border-radius: 4px; cursor: pointer; font-size: 12px; padding: 0; }
.uav-actions button:hover { background: #4db4ff; }
.empty { padding: 20px; text-align: center; color: #6c9bd0; font-size: 12px; }
</style>
