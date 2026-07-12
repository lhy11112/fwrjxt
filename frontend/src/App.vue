<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useSituationStore } from '@/stores/situation'
import { useSensorStore } from '@/stores/sensor'
import { createWebSocket } from '@/api'
import { getSystemMetrics } from '@/api'
import ChatPanel from '@/components/ChatPanel.vue'
import DetectionPanel from '@/components/panels/DetectionPanel.vue'
import DeviceDialog from '@/components/DeviceDialog.vue'
import ModelDialog from '@/components/ModelDialog.vue'
import ThemePicker from '@/components/ThemePicker.vue'

const router = useRouter()
const route = useRoute()
const situation = useSituationStore()
const sensor = useSensorStore()

let ws: WebSocket | null = null
const metrics = ref({ active_tracks: 0, sensors_online: 0 })
const currentTime = ref('')
const currentDate = ref('')
const rightPanelTab = ref<'detection' | 'targets' | 'chat'>('detection')

// 业务功能菜单（迁移自反无系统的 portal 各模块）
const businessMenus = [
  { path: '/daily-topic', title: '综合态势' },
  { path: '/gjhf', title: '告警回放' },
  { path: '/command-control', title: '指挥控制' },
  { path: '/cesium-air', title: '空域管理' },
  { path: '/zymb', title: '重要目标' },
  { path: '/zhby', title: '综合兵要' },
  { path: '/zbxx', title: '装备信息' },
  { path: '/fwzf', title: '反无战法' },
  { path: '/bxpz', title: '编携配装' },
  { path: '/signal-interference', title: '信号干扰' },
  { path: '/navigation-deception', title: '导航诱骗' },
  { path: '/simulated-exercise', title: '模拟推演' },
]
const businessActive = computed(() => businessMenus.some(m => m.path === route.path))

// 对话框状态
const showDeviceDialog = ref(false)
const showModelDialog = ref(false)
const showAlerts = ref(false)
const showThemePicker = ref(false)

// 主题系统
const themes = ['skyblue', 'light', 'green', 'blue'] as const
const THEME_KEY = 'hsimc2-theme'
const savedTheme = localStorage.getItem(THEME_KEY)
const savedIdx = savedTheme ? themes.indexOf(savedTheme as any) : -1
const themeIdx = ref(savedIdx >= 0 ? savedIdx : 0)
const currentTheme = computed(() => themes[themeIdx.value])

function selectTheme(themeId: string) {
  themeIdx.value = themes.indexOf(themeId as any)
}

// 将主题类挂到 <html>，令 Teleport 到 body 的弹窗也能继承主题令牌
watch(currentTheme, (t, old) => {
  const cl = document.documentElement.classList
  if (old) cl.remove('skin-' + old)
  cl.add('skin-' + t)
  localStorage.setItem(THEME_KEY, t)
}, { immediate: true })

let timeInterval: number | undefined
onMounted(() => {
  updateTime()
  timeInterval = window.setInterval(updateTime, 1000)
  initWebSocket()
  loadMetrics()
})
onUnmounted(() => {
  ws?.close()
  clearInterval(timeInterval)
})

function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
  currentDate.value = now.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', weekday: 'long' })
}

async function loadMetrics() {
  try { const m = await getSystemMetrics(); metrics.value = m } catch {}
}

function initWebSocket() {
  ws = createWebSocket()
  ws.onmessage = (event: MessageEvent) => {
    try {
      const msg = JSON.parse(event.data)
      if (msg.event === 'TargetUpdated') situation.updateTargets(msg.payload)
    } catch {}
  }
  ws.onclose = () => setTimeout(() => initWebSocket(), 3000)
}

/** 通过全局函数触发地图重置视角 */
function handleResetView() {
  const fn = (window as any).__hsimc2_resetView
  if (typeof fn === 'function') fn()
}


</script>

<template>
  <div class="bigscreen-layout" :class="'skin-' + currentTheme">
    <!-- ===== 顶部状态栏 ===== -->
    <header class="top-bar">
      <div class="top-left">
        <div class="system-brand">
          <Icon name="shield" :size="20" class="brand-icon" />
          <span class="brand-title">HSimC2 反无人机一体化指控平台</span>
        </div>
      </div>

      <div class="top-center">
        <div class="nav-tabs">
          <router-link to="/" class="nav-tab" :class="{ active: route.path === '/' }">
            <Icon name="map" :size="16" class="tab-icon" /><span class="tab-label">态势总览</span>
          </router-link>
          <router-link to="/detection" class="nav-tab" :class="{ active: route.path === '/detection' }">
            <Icon name="target" :size="16" class="tab-icon" /><span class="tab-label">侦测预警</span>
          </router-link>
          <router-link to="/wxdzc" class="nav-tab" :class="{ active: route.path === '/wxdzc' }">
            <Icon name="target" :size="16" class="tab-icon" /><span class="tab-label">侦测预警2.0</span>
          </router-link>
          <router-link to="/mission" class="nav-tab" :class="{ active: route.path === '/mission' }">
            <Icon name="target" :size="16" class="tab-icon" /><span class="tab-label">任务规划</span>
          </router-link>
          <a class="nav-tab" @click.prevent="showDeviceDialog = true" href="#">
            <Icon name="device" :size="16" class="tab-icon" /><span class="tab-label">设备管理</span>
          </a>
          <router-link to="/data-manage2" class="nav-tab" :class="{ active: route.path.startsWith('/data-manage') }">
            <Icon name="device" :size="16" class="tab-icon" /><span class="tab-label">数据管理</span>
          </router-link>
          <el-dropdown trigger="hover" @command="(c: string) => router.push(c)">
            <span class="nav-tab" :class="{ active: businessActive }">
              <Icon name="list" :size="16" class="tab-icon" /><span class="tab-label">业务功能</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="m in businessMenus" :key="m.path" :command="m.path">{{ m.title }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <a class="nav-tab" @click.prevent="showModelDialog = true" href="#">
            <Icon name="model" :size="16" class="tab-icon" /><span class="tab-label">模型管理</span>
          </a>
        </div>
      </div>

      <div class="top-right">
        <button class="tr-icon-btn" title="重置视角" @click="handleResetView">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 10.5 12 3l9 7.5"/><path d="M5 9.5V20h5v-6h4v6h5V9.5"/></svg>
        </button>
        <button class="tr-icon-btn" title="告警信息" @click="showAlerts = !showAlerts">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8a6 6 0 1 0-12 0c0 7-2 8-2 8h16s-2-1-2-8"/><path d="M10.5 20a1.8 1.8 0 0 0 3 0"/></svg>
          <span class="tr-badge" v-if="situation.redThreats.length">{{ situation.redThreats.length }}</span>
        </button>
        <button class="tr-icon-btn" title="切换皮肤" @click="showThemePicker = true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 3a9 9 0 0 1 0 18z" fill="currentColor" stroke="none"/></svg>
        </button>
        <button class="tr-icon-btn" title="系统设置">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
        </button>
        <div class="tr-avatar" title="管理员">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8" r="3.6"/><path d="M5 20c0-3.6 3-5.5 7-5.5s7 1.9 7 5.5"/></svg>
        </div>
        <span class="time-clock">{{ currentTime }}</span>
      </div>
    </header>

    <!-- ===== 主区域 ===== -->
    <div class="main-area">
      <aside class="panel-left">
        <div class="info-card">
          <div class="card-title"><Icon name="chart" :size="14" />系统概览</div>
          <div class="card-body">
            <div class="metric-grid cols-2">
              <div class="metric-box"><div class="m-value red">{{ situation.redThreats.length }}</div><div class="m-label">高危目标</div></div>
              <div class="metric-box"><div class="m-value yellow">{{ situation.yellowThreats.length }}</div><div class="m-label">中危目标</div></div>
              <div class="metric-box"><div class="m-value green">{{ situation.totalTargets }}</div><div class="m-label">跟踪总数</div></div>
              <div class="metric-box"><div class="m-value blue">{{ metrics.sensors_online }}</div><div class="m-label">在线传感器</div></div>
            </div>
            <div class="threat-bar-container">
              <div class="threat-bar">
                <div class="t-seg red" :style="{flex: situation.redThreats.length || 0.01}"></div>
                <div class="t-seg yellow" :style="{flex: situation.yellowThreats.length || 0.01}"></div>
                <div class="t-seg green" :style="{flex: situation.greenThreats.length || 0.01}"></div>
              </div>
            </div>
          </div>
        </div>
        <div class="info-card">
          <div class="card-title"><Icon name="sensor" :size="14" />传感器仿真</div>
          <div class="card-body">
            <div class="sim-sensor-list">
              <div class="sim-sensor" v-for="s in ['相控阵雷达','频谱监测','光电追踪','声学阵列']" :key="s">
                <span class="sim-led on"></span><span class="sim-name">{{ s }}</span><span class="sim-status">运行中</span>
              </div>
            </div>
            <div class="sim-metrics">
              <div class="sim-stat"><span class="sim-stat-value">200ms</span><span class="sim-stat-label">更新周期</span></div>
              <div class="sim-stat"><span class="sim-stat-value">5km</span><span class="sim-stat-label">仿真半径</span></div>
              <div class="sim-stat"><span class="sim-stat-value">8-12</span><span class="sim-stat-label">目标数量</span></div>
            </div>
          </div>
        </div>
      </aside>

      <div class="gis-area"><router-view /></div>

      <aside class="panel-right">
        <div class="panel-tabs">
          <button class="panel-tab" :class="{ active: rightPanelTab === 'detection' }" @click="rightPanelTab = 'detection'">
            <Icon name="sensor" :size="14" /> 侦测
          </button>
          <button class="panel-tab" :class="{ active: rightPanelTab === 'targets' }" @click="rightPanelTab = 'targets'">
            <Icon name="target" :size="14" /> 目标<span class="tab-count" v-if="situation.totalTargets">{{ situation.totalTargets }}</span>
          </button>
          <button class="panel-tab" :class="{ active: rightPanelTab === 'chat' }" @click="rightPanelTab = 'chat'">
            <Icon name="chat" :size="14" /> 智能交互
          </button>
        </div>
        <template v-if="rightPanelTab === 'detection'">
          <DetectionPanel />
        </template>
        <template v-if="rightPanelTab === 'targets'">
          <div class="info-card flex-1">
            <div class="card-body p-0" style="padding:0">
              <div class="target-list-scroll">
                <div v-for="t in situation.targets" :key="t.target_id"
                     class="target-item" :class="{ selected: situation.selectedTargetId === t.target_id }"
                     @click="situation.selectTarget(t.target_id)">
                  <div class="t-icon"><span class="t-dot" :class="t.threat_level === 'RED' ? 'red' : t.threat_level === 'YELLOW' ? 'yellow' : 'green'"></span></div>
                  <div class="t-info">
                    <div class="t-head"><span class="t-type">{{ t.classification }}</span><span class="t-behavior">{{ t.threat_level === 'RED' ? '攻击' : t.threat_level === 'YELLOW' ? '侦察' : '巡航' }}</span></div>
                    <div class="t-meta">高度{{ t.position.altitude.toFixed(0) }}m 速度{{ Math.sqrt(t.velocity.vn**2 + t.velocity.ve**2).toFixed(0) }}m/s</div>
                  </div>
                </div>
                <div v-if="!situation.targets.length" class="empty-hint">等待仿真数据...</div>
              </div>
            </div>
          </div>
          <div class="info-card" v-if="situation.selectedTarget">
            <div class="card-title"><Icon name="list" :size="14" />目标详情</div>
            <div class="card-body compact">
              <div class="detail-row"><span class="dl">类型</span><span class="dv">{{ situation.selectedTarget.classification }}</span></div>
              <div class="detail-row"><span class="dl">高度</span><span class="dv">{{ situation.selectedTarget.position.altitude.toFixed(0) }} m</span></div>
              <div class="detail-row"><span class="dl">速度</span><span class="dv">{{ Math.sqrt(situation.selectedTarget.velocity.vn**2 + situation.selectedTarget.velocity.ve**2).toFixed(1) }} m/s</span></div>
              <div class="detail-row"><span class="dl">经纬</span><span class="dv">{{ situation.selectedTarget.position.latitude.toFixed(4) }}, {{ situation.selectedTarget.position.longitude.toFixed(4) }}</span></div>
              <div class="detail-row"><span class="dl">置信度</span><span class="dv">{{ (situation.selectedTarget.confidence * 100).toFixed(0) }}%</span></div>
            </div>
          </div>
        </template>
        <div v-if="rightPanelTab === 'chat'" class="info-card flex-1"><ChatPanel /></div>
      </aside>
    </div>

    <footer class="bottom-bar">
      <div class="bottom-left"><span class="event-log"><Icon name="pulse" :size="13" />系统就绪 · {{ metrics.active_tracks }} 目标跟踪中</span></div>
      <div class="bottom-center"><span class="fps-indicator"><Icon name="clock" :size="13" />更新间隔 500ms</span></div>
      <div class="bottom-right"><span class="version-tag">v0.1.0</span></div>
    </footer>

    <!-- 弹窗 -->
    <DeviceDialog :visible="showDeviceDialog" @close="showDeviceDialog = false" />
    <ModelDialog :visible="showModelDialog" @close="showModelDialog = false" />
    <ThemePicker :visible="showThemePicker" :current="currentTheme" @close="showThemePicker = false" @select="selectTheme" />
  </div>
</template>

<style scoped>
.bigscreen-layout { display:flex; flex-direction:column; height:100vh; background:var(--bg-canvas); color:var(--fg-1); overflow:hidden; font-family:var(--font-base); }

/* === 顶部栏 === */
.top-bar { display:flex; align-items:center; justify-content:space-between; height:52px; padding:0 var(--sp-l); background:linear-gradient(180deg, var(--bg-surface-2), var(--bg-surface)); border-bottom:1px solid var(--divider); box-shadow:var(--shadow-4); flex-shrink:0; position:relative; z-index:10; }
.top-bar::after { content:''; position:absolute; left:0; right:0; bottom:-1px; height:1px; background:linear-gradient(90deg, transparent, var(--brand-fg), transparent); box-shadow:0 0 10px var(--glow-accent); opacity:0.85; }
.top-bar::before { content:''; position:absolute; bottom:-1px; left:0; width:18%; height:1px; background:linear-gradient(90deg, transparent, var(--brand-fg-hover), transparent); box-shadow:0 0 12px var(--glow-accent); animation:barScan 5.5s linear infinite; }
@keyframes barScan { 0% { transform:translateX(-100%); } 100% { transform:translateX(650%); } }
.system-brand { display:flex; align-items:center; gap:var(--sp-s); }
.brand-icon { font-size:20px; filter:drop-shadow(0 0 6px var(--glow-soft)); }
.brand-title { font-size:15px; font-weight:700; letter-spacing:0.6px; white-space:nowrap; background:linear-gradient(90deg, var(--fg-1), var(--brand-fg)); -webkit-background-clip:text; background-clip:text; -webkit-text-fill-color:transparent; filter:drop-shadow(0 0 7px var(--glow-soft)); }

.top-center { flex:1; display:flex; align-items:center; justify-content:center; }
.top-right { display:flex; align-items:center; gap:var(--sp-s); flex-shrink:0; margin-left:auto; }
.nav-tabs { display:flex; gap:var(--sp-xs); }
.nav-tab { display:flex; align-items:center; gap:6px; padding:6px var(--sp-m); border-radius:var(--radius-md); color:var(--fg-3); text-decoration:none; font-size:14px; font-weight:600; cursor:pointer; transition:background var(--dur-normal) var(--ease-fluent), color var(--dur-normal) var(--ease-fluent); }
.nav-tab:hover { background:var(--bg-subtle-hover); color:var(--fg-1); }
.nav-tab.active { background:var(--brand-selected); color:var(--brand-fg); box-shadow:inset 0 0 0 1px var(--brand-subtle), 0 0 14px var(--glow-soft); }

/* === 右区：图标按钮 === */
.tr-icon-btn { position:relative; width:32px; height:32px; display:flex; align-items:center; justify-content:center; border:1px solid transparent; border-radius:var(--radius-md); background:transparent; color:var(--fg-2); cursor:pointer; transition:background var(--dur-normal) var(--ease-fluent), color var(--dur-normal) var(--ease-fluent); }
.tr-icon-btn:hover { background:var(--bg-subtle-hover); color:var(--brand-fg); }
.tr-icon-btn:active { background:var(--bg-subtle-pressed); }
.tr-icon-btn svg { width:18px; height:18px; display:block; }
.tr-badge { position:absolute; top:-4px; right:-4px; min-width:16px; height:16px; padding:0 4px; background:var(--danger); border-radius:var(--radius-circular); color:#fff; font-size:10px; font-weight:700; line-height:16px; text-align:center; box-shadow:0 0 8px var(--danger); }
.tr-avatar { width:32px; height:32px; display:flex; align-items:center; justify-content:center; border-radius:var(--radius-circular); background:var(--brand-subtle); color:var(--brand-fg); cursor:pointer; transition:background var(--dur-normal) var(--ease-fluent); }
.tr-avatar:hover { background:var(--brand-selected); }
.tr-avatar svg { width:18px; height:18px; display:block; }
.time-clock { font-size:14px; font-weight:600; color:var(--fg-2); font-family:var(--font-mono); min-width:64px; text-align:right; }

/* === 主区域 === */
.main-area { display:flex; flex:1; overflow:hidden; gap:1px; background:var(--divider); }
.panel-left { width:224px; display:flex; flex-direction:column; gap:1px; flex-shrink:0; overflow-y:auto; background:var(--bg-canvas); }
.gis-area { flex:1; position:relative; overflow:hidden; min-width:0; }
.panel-right { width:264px; display:flex; flex-direction:column; gap:0; background:var(--bg-canvas); flex-shrink:0; overflow:hidden; }
.panel-tabs { display:flex; flex-shrink:0; border-bottom:1px solid var(--divider); background:var(--bg-surface); }
.panel-tab { flex:1; display:flex; align-items:center; justify-content:center; gap:var(--sp-xs); padding:10px 0; background:transparent; border:none; border-bottom:2px solid transparent; color:var(--fg-3); font-size:13px; font-weight:600; cursor:pointer; transition:color var(--dur-normal) var(--ease-fluent); }
.panel-tab:hover { color:var(--fg-1); }
.panel-tab.active { color:var(--brand-fg); border-bottom-color:var(--brand-fg); box-shadow:0 3px 10px -3px var(--glow-accent); text-shadow:0 0 10px var(--glow-soft); }
.tab-count { padding:0 6px; background:var(--brand-subtle); border-radius:var(--radius-circular); font-size:10px; color:var(--brand-fg); font-weight:600; }

.info-card { position:relative; background:var(--bg-surface); border:none; box-shadow:var(--surface-shadow); }
.info-card.flex-1 { flex:1; overflow:hidden; display:flex; flex-direction:column; }
.card-title { display:flex; align-items:center; gap:6px; padding:10px var(--sp-m); font-size:12px; font-weight:600; color:var(--fg-2); border-bottom:1px solid var(--divider); letter-spacing:0.2px; }
.card-title::before { content:''; width:3px; height:13px; border-radius:2px; background:var(--brand-fg); box-shadow:0 0 8px var(--glow-accent); flex-shrink:0; }
.card-body { padding:var(--sp-m); font-size:12px; }
.card-body.p-0 { padding:0; }
.card-body.compact { padding:var(--sp-s) var(--sp-m); }
.metric-grid { display:grid; gap:var(--sp-s); }
.metric-grid.cols-2 { grid-template-columns:1fr 1fr; }
.metric-box { text-align:center; padding:var(--sp-s) var(--sp-xs); background:linear-gradient(180deg, var(--bg-surface-2), var(--bg-surface)); border:1px solid var(--stroke-divider); border-radius:var(--radius-md); box-shadow:var(--surface-shadow); transition:border-color var(--dur-normal) var(--ease-fluent); }
.metric-box:hover { border-color:var(--brand-fg); }
.m-value { font-size:24px; font-weight:700; line-height:1.2; text-shadow:0 0 16px color-mix(in srgb, currentColor 45%, transparent); }
.m-value.red { color:var(--danger); } .m-value.yellow { color:var(--warning); } .m-value.green { color:var(--success); } .m-value.blue { color:var(--brand-fg); }
.m-label { font-size:10px; color:var(--fg-3); margin-top:2px; }
.threat-bar-container { margin-top:var(--sp-s); }
.threat-bar { position:relative; display:flex; height:4px; border-radius:var(--radius-circular); overflow:hidden; gap:1px; }
.threat-bar::after { content:''; position:absolute; inset:0; background:linear-gradient(90deg, transparent, rgba(255,255,255,0.45), transparent); transform:translateX(-100%); animation:threatShimmer 3.2s var(--ease-fluent) infinite; pointer-events:none; }
@keyframes threatShimmer { 0% { transform:translateX(-100%); } 55%, 100% { transform:translateX(300%); } }
.t-seg.red { background:var(--danger); } .t-seg.yellow { background:var(--warning); } .t-seg.green { background:var(--success); }
.sim-sensor-list { display:flex; flex-direction:column; gap:var(--sp-xs); }
.sim-sensor { display:flex; align-items:center; gap:var(--sp-s); padding:var(--sp-xs) 6px; border-radius:var(--radius-sm); font-size:11px; }
.sim-led { width:6px; height:6px; border-radius:var(--radius-circular); flex-shrink:0; }
.sim-led.on { background:var(--success); animation:ledPulse 2.2s var(--ease-fluent) infinite; }
@keyframes ledPulse {
  0%, 100% { box-shadow:0 0 0 0 var(--success-bg), 0 0 4px var(--success); }
  50% { box-shadow:0 0 0 3px var(--success-bg), 0 0 10px var(--success); }
}
.sim-name { flex:1; color:var(--fg-2); }
.sim-status { color:var(--success); font-size:10px; }
.sim-metrics { display:flex; justify-content:space-around; margin-top:var(--sp-s); padding-top:var(--sp-s); border-top:1px solid var(--stroke-divider); }
.sim-stat { text-align:center; }
.sim-stat-value { display:block; font-size:14px; font-weight:600; color:var(--brand-fg); }
.sim-stat-label { font-size:9px; color:var(--fg-4); }
.target-list-scroll { overflow-y:auto; max-height:calc(100vh - 300px); }
.target-item { display:flex; align-items:center; gap:var(--sp-s); padding:8px var(--sp-m); cursor:pointer; border-bottom:1px solid var(--stroke-divider); border-left:2px solid transparent; transition:background var(--dur-normal) var(--ease-fluent); }
.target-item:hover { background:var(--bg-subtle-hover); }
.target-item.selected { background:var(--brand-selected); border-left-color:var(--brand-fg); box-shadow:inset 3px 0 12px -4px var(--glow-accent); }
.t-icon { flex-shrink:0; width:16px; display:flex; align-items:center; justify-content:center; }
.t-dot { width:10px; height:10px; border-radius:var(--radius-circular); flex-shrink:0; }
.t-dot.red { background:var(--danger); box-shadow:0 0 7px var(--danger), 0 0 0 3px var(--danger-bg); animation:dotPulse 1.6s var(--ease-fluent) infinite; }
.t-dot.yellow { background:var(--warning); box-shadow:0 0 6px var(--warning), 0 0 0 3px var(--warning-bg); }
.t-dot.green { background:var(--success); box-shadow:0 0 5px var(--success), 0 0 0 3px var(--success-bg); }
@keyframes dotPulse {
  0%, 100% { box-shadow:0 0 4px var(--danger), 0 0 0 2px var(--danger-bg); }
  50% { box-shadow:0 0 11px var(--danger), 0 0 0 4px var(--danger-bg); }
}
.t-info { flex:1; min-width:0; }
.t-head { display:flex; gap:6px; align-items:center; }
.t-type { font-size:12px; font-weight:600; color:var(--fg-1); }
.t-behavior { font-size:10px; color:var(--fg-3); }
.t-meta { font-size:10px; color:var(--fg-4); white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.empty-hint { padding:30px; text-align:center; color:var(--fg-4); font-size:13px; }
.detail-row { display:flex; justify-content:space-between; padding:var(--sp-xs) 0; font-size:11px; }
.dl { color:var(--fg-3); } .dv { color:var(--fg-1); font-weight:600; }

.bottom-bar { position:relative; display:flex; align-items:center; justify-content:space-between; height:28px; padding:0 var(--sp-l); background:linear-gradient(0deg, var(--bg-surface-2), var(--bg-surface)); border-top:1px solid var(--divider); font-size:11px; color:var(--fg-3); flex-shrink:0; }
.bottom-bar::before { content:''; position:absolute; top:-1px; left:0; right:0; height:1px; background:linear-gradient(90deg, transparent, var(--brand-fg), transparent); box-shadow:0 0 10px var(--glow-accent); opacity:0.7; }
.bottom-left,.bottom-center,.bottom-right { display:flex; align-items:center; gap:var(--sp-l); }
.event-log,.fps-indicator { display:inline-flex; align-items:center; gap:5px; }
.version-tag { padding:1px var(--sp-s); background:var(--brand-subtle); border:1px solid var(--brand-subtle); border-radius:var(--radius-md); font-family:var(--font-mono); color:var(--brand-fg); box-shadow:0 0 8px var(--glow-soft); }

/* ===== 主题皮肤（挂在 <html> 上，令 teleport 弹窗也继承令牌） ===== */
/* 亮蓝主题 — 亮宝蓝科技底 + 主色 #028BE5 电光高亮，默认 */
:global(html.skin-skyblue) {
  --bg-canvas: #072b57; --bg-surface: #0e457c; --bg-surface-2: #104a84; --bg-surface-3: #124e8a;
  --bg-subtle-hover: #17589a; --bg-subtle-pressed: #0d4070;
  --stroke-control: #2f7ad4; --stroke-divider: #1d5390; --divider: #26629f;
  --fg-1: #eef6ff; --fg-2: #c6e0f9; --fg-3: #97c0eb; --fg-4: #6c9bd0;
  --brand-rest: #028be5; --brand-hover: #2ba3f2; --brand-pressed: #0277c4;
  --brand-fg: #4db4ff; --brand-fg-hover: #7cc8ff;
  --brand-subtle: rgba(2,139,229,0.18); --brand-selected: rgba(2,139,229,0.30);
  --danger: #ff7a86; --danger-bg: rgba(255,122,134,0.18);
  --warning: #ffd561; --warning-bg: rgba(255,213,97,0.18);
  --success: #6fd66f; --success-bg: rgba(111,214,111,0.18);
  --glow-accent: rgba(77,180,255,0.60); --glow-soft: rgba(77,180,255,0.30);
}
/* 极简浅色主题 — 洁净白面 + #028BE5 强调 */
:global(html.skin-light) {
  --bg-canvas: #e8eff7; --bg-surface: #ffffff; --bg-surface-2: #f2f7fd; --bg-surface-3: #ffffff;
  --bg-subtle-hover: #e6eefb; --bg-subtle-pressed: #d6e4f5;
  --stroke-control: #bcd0e6; --stroke-divider: #dde8f3; --divider: #d0deee;
  --fg-1: #14263a; --fg-2: #35506b; --fg-3: #5a748f; --fg-4: #8ba0b5;
  --brand-rest: #028be5; --brand-hover: #0277c4; --brand-pressed: #0266a8;
  --brand-fg: #0277c4; --brand-fg-hover: #028be5;
  --brand-subtle: rgba(2,139,229,0.08); --brand-selected: rgba(2,139,229,0.14);
  --danger: #c50f1f; --danger-bg: rgba(197,15,31,0.10);
  --warning: #9a6700; --warning-bg: rgba(240,180,40,0.16);
  --success: #0e700e; --success-bg: rgba(14,112,14,0.10);
  --surface-shadow: 0 1px 3px rgba(16,40,70,0.10), 0 0 1px rgba(16,40,70,0.10);
  --glow-accent: rgba(2,139,229,0.14); --glow-soft: rgba(2,139,229,0.08);
}
/* 军绿夜视主题 */
:global(html.skin-green) {
  --bg-canvas: #0a0f0a; --bg-surface: #111811; --bg-surface-2: #151d15; --bg-surface-3: #1a231a;
  --bg-subtle-hover: #202a20; --bg-subtle-pressed: #141c14;
  --stroke-control: #3f5a3a; --stroke-divider: #253025; --divider: #2e3a2e;
  --fg-1: #d8ecd0; --fg-2: #aecfa4; --fg-3: #86a67e; --fg-4: #5e735a;
  --brand-rest: #4a8a3e; --brand-hover: #5aa54c; --brand-pressed: #3c7233;
  --brand-fg: #86d97a; --brand-fg-hover: #a3e896;
  --brand-subtle: rgba(134,217,122,0.14); --brand-selected: rgba(134,217,122,0.22);
  --glow-accent: rgba(134,217,122,0.60); --glow-soft: rgba(134,217,122,0.35);
}
/* 深海蔚蓝主题 */
:global(html.skin-blue) {
  --bg-canvas: #071526; --bg-surface: #0e2440; --bg-surface-2: #12294a; --bg-surface-3: #162f54;
  --bg-subtle-hover: #1a3559; --bg-subtle-pressed: #102037;
  --stroke-control: #2d5a8f; --stroke-divider: #1e3a5c; --divider: #274866;
  --fg-1: #e8f6ff; --fg-2: #b8d6ee; --fg-3: #8bafce; --fg-4: #5f7fa0;
  --brand-rest: #1585d6; --brand-hover: #2b9ae8; --brand-pressed: #0f6cb5;
  --brand-fg: #35c4f0; --brand-fg-hover: #5fd4f5;
  --brand-subtle: rgba(53,196,240,0.12); --brand-selected: rgba(53,196,240,0.20);
  --glow-accent: rgba(53,196,240,0.55); --glow-soft: rgba(53,196,240,0.30);
}
</style>