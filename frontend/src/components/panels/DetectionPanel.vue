<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useSituationStore } from '@/stores/situation'
import { deviceApi } from '@/api/dataManage'
import type { DeviceConfig } from '@/types'

const situation = useSituationStore()
const devices = ref<DeviceConfig[]>([])
const detectionCount = ref(0)
const deviceCount = ref(0)
const lastRefresh = ref('')
let timer: number | undefined

// 设备在线/离线统计
const onlineDevices = computed(() => devices.value.filter(d => d.status === 'CONNECTED'))
const offlineDevices = computed(() => devices.value.filter(d => d.status !== 'CONNECTED'))

// 按威胁等级排序的目标
const sortedTargets = computed(() => {
  const threatScore: Record<string, number> = { RED: 3, YELLOW: 2, GREEN: 1 }
  const score = (t: any) => (threatScore[t.threat_level] || 0)
  return [...situation.targets].sort((a, b) => score(b) - score(a))
})

// 风险评分计算（参考福建项目）
function calcRiskScore(t: any): { score: number; level: string; factors: string[] } {
  const speed = Math.sqrt((t.velocity?.vn || 0) ** 2 + (t.velocity?.ve || 0) ** 2)
  const alt = t.position?.altitude || 100
  const dist = 1.0 // 默认距离系数
  // 速度归一化 (0-50m/s → 0-1)
  const speedNorm = Math.min(speed / 50, 1)
  // 高度归一化 (低空更危险, 0-500m)
  const altNorm = 1 - Math.min(alt / 500, 1)
  // 距离归一化
  const distNorm = Math.min(dist / 10, 1)
  // 加权综合
  const score = distNorm * 0.4 + speedNorm * 0.3 + altNorm * 0.3
  const level = score >= 0.7 ? 'RED' : score >= 0.4 ? 'YELLOW' : 'GREEN'
  const factors: string[] = []
  if (distNorm > 0.5) factors.push('距离近')
  if (speedNorm > 0.5) factors.push('速度高')
  if (altNorm > 0.5) factors.push('低空飞行')
  return { score: Math.round(score * 100) / 100, level, factors }
}

async function refreshDevices() {
  try {
    const data = await deviceApi.list({ page: 1, page_size: 50 })
    devices.value = data.records || []
    deviceCount.value = data.total || 0
    detectionCount.value = situation.totalTargets
    lastRefresh.value = new Date().toLocaleTimeString('zh-CN')
  } catch {}
}

// 设备类型标签
const typeLabels: Record<string, string> = { DETECT: '侦测', TRAP: '诱骗', DISTURB: '干扰', System: '系统' }
const typeColors: Record<string, string> = { DETECT: 'success', TRAP: 'warning', DISTURB: 'danger', System: 'info' }

onMounted(() => { refreshDevices(); timer = window.setInterval(refreshDevices, 6000) }) // 6秒刷新
onUnmounted(() => clearInterval(timer))
</script>

<template>
  <div class="detection-panel">
    <!-- 设备概览 -->
    <div class="section">
      <div class="section-title">
        <span class="dot online"></span> 探测设备
        <span class="badge">{{ onlineDevices.length }}/{{ devices.length }}</span>
      </div>
      <div class="device-list" v-if="devices.length">
        <div v-for="d in devices" :key="d.device_id" class="device-item" :class="{ online: d.status === 'CONNECTED', offline: d.status !== 'CONNECTED' }">
          <span class="d-status-dot" :class="{ on: d.status === 'CONNECTED' }"></span>
          <span class="d-name">{{ d.name || d.device_id }}</span>
          <el-tag size="small" :type="typeColors[d.device_type] || 'info'">{{ typeLabels[d.device_type] || d.device_type }}</el-tag>
        </div>
      </div>
      <div v-else class="empty-text">暂无设备</div>
    </div>

    <!-- 侦测目标 -->
    <div class="section flex-1">
      <div class="section-title">
        <span class="dot warn"></span> 侦测目标
        <span class="badge">{{ detectionCount }}</span>
      </div>
      <div class="target-list" v-if="sortedTargets.length">
        <div v-for="t in sortedTargets.slice(0, 20)" :key="t.target_id"
             class="target-item"
             :class="{ selected: situation.selectedTargetId === t.target_id }"
             @click="situation.selectTarget(t.target_id)">
          <span class="t-threat-dot" :class="t.threat_level === 'RED' ? 'red' : t.threat_level === 'YELLOW' ? 'yellow' : 'green'"></span>
          <div class="t-info">
            <div class="t-model">{{ t.classification }}</div>
            <div class="t-telemetry">
              高度{{ t.position.altitude?.toFixed(0) }}m
              · {{ Math.sqrt((t.velocity?.vn||0)**2 + (t.velocity?.ve||0)**2).toFixed(0) }}m/s
            </div>
          </div>
          <span class="t-risk" :class="calcRiskScore(t).level">
            {{ calcRiskScore(t).level === 'RED' ? '高危' : calcRiskScore(t).level === 'YELLOW' ? '中危' : '低危' }}
          </span>
        </div>
      </div>
      <div v-else class="empty-text">等待侦测数据...</div>
    </div>

    <!-- 底部状态 -->
    <div class="section-footer">
      <span>刷新: {{ lastRefresh || '--' }}</span>
      <span>设备: {{ deviceCount }} | 目标: {{ detectionCount }}</span>
    </div>
  </div>
</template>

<style scoped>
.detection-panel { display: flex; flex-direction: column; height: 100%; color: var(--fg-2); font-size: 12px; overflow: hidden; }
.section { padding: 8px 10px; border-bottom: 1px solid var(--stroke-divider); }
.section.flex-1 { flex: 1; overflow-y: auto; }
.section-title { display: flex; align-items: center; gap: 6px; font-size: 11px; font-weight: 600; color: var(--fg-1); margin-bottom: 6px; letter-spacing: 0.3px; }
.dot { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
.dot.online { background: var(--success); box-shadow: 0 0 6px var(--success); }
.dot.warn { background: var(--warning); box-shadow: 0 0 6px var(--warning); }
.badge { margin-left: auto; font-size: 10px; color: var(--fg-3); background: var(--bg-subtle-hover); padding: 1px 6px; border-radius: 10px; }
.device-list { display: flex; flex-direction: column; gap: 3px; max-height: 140px; overflow-y: auto; }
.device-item { display: flex; align-items: center; gap: 6px; padding: 3px 6px; border-radius: 4px; font-size: 11px; }
.device-item.online { color: var(--fg-1); }
.device-item.offline { color: var(--fg-3); opacity: 0.6; }
.d-status-dot { width: 5px; height: 5px; border-radius: 50%; flex-shrink: 0; background: var(--fg-4); }
.d-status-dot.on { background: var(--success); animation: ledPulse 2s infinite; }
@keyframes ledPulse { 0%, 100% { box-shadow: 0 0 0 0 var(--success-bg); } 50% { box-shadow: 0 0 0 4px var(--success-bg); } }
.d-name { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.target-list { display: flex; flex-direction: column; gap: 2px; }
.target-item { display: flex; align-items: center; gap: 6px; padding: 4px 6px; border-radius: 4px; cursor: pointer; transition: background 0.15s; }
.target-item:hover { background: var(--bg-subtle-hover); }
.target-item.selected { background: var(--brand-selected); border-left: 2px solid var(--brand-fg); }
.t-threat-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.t-threat-dot.red { background: var(--danger); box-shadow: 0 0 6px var(--danger); }
.t-threat-dot.yellow { background: var(--warning); box-shadow: 0 0 4px var(--warning); }
.t-threat-dot.green { background: var(--success); }
.t-info { flex: 1; min-width: 0; }
.t-model { font-size: 11px; font-weight: 500; color: var(--fg-1); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.t-telemetry { font-size: 9px; color: var(--fg-4); }
.t-risk { font-size: 10px; font-weight: 600; flex-shrink: 0; padding: 1px 5px; border-radius: 3px; }
.t-risk.RED { color: var(--danger); background: var(--danger-bg); }
.t-risk.YELLOW { color: var(--warning); background: var(--warning-bg); }
.t-risk.GREEN { color: var(--success); background: var(--success-bg); }
.section-footer { padding: 6px 10px; font-size: 9px; color: var(--fg-4); display: flex; justify-content: space-between; border-top: 1px solid var(--stroke-divider); }
.empty-text { padding: 10px; text-align: center; color: var(--fg-4); font-size: 11px; }
</style>
