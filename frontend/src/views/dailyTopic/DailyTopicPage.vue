<script setup lang="ts">
/**
 * 综合态势 (Daily Topic / Portal) — 综合统计仪表盘
 */
import { ref, onMounted } from 'vue'
import { deviceApi, alarmApi, airspaceApi, droneBasicApi } from '@/api/dataManage'
import { getByStation } from '@/api/wxdzc'

const stats = ref({ device: 0, detect: 0, disturb: 0, trap: 0, connected: 0, todayDetect: 0, alarm: 0, airspace: 0, drone: 0 })
const recentAlarms = ref<any[]>([])
const today = new Date().toISOString().slice(0, 10)

onMounted(async () => {
  try {
    const { data } = await deviceApi.stats() as any
    if (data?.result) {
      stats.value.detect = data.result.detect || 0
      stats.value.disturb = data.result.disturb || 0
      stats.value.trap = data.result.trap || 0
      stats.value.connected = data.result.connected || 0
      stats.value.device = stats.value.detect + stats.value.disturb + stats.value.trap
    }
  } catch {}
  try { const d = await getByStation({ rq: today }); stats.value.todayDetect = d.length } catch {}
  try { const a = await alarmApi.list({ page: 1, page_size: 5 }); stats.value.alarm = a.total; recentAlarms.value = a.records || [] } catch {}
  try { const k = await airspaceApi.list({ page: 1, page_size: 1 }); stats.value.airspace = k.total } catch {}
  try { const dr = await droneBasicApi.list({ page: 1, page_size: 1 }); stats.value.drone = dr.total } catch {}
})
</script>

<template>
  <div class="dt-page">
    <div class="page-header">
      <h2 class="page-title">综合态势</h2>
      <p class="page-desc">反无人机系统综合态势总览</p>
    </div>

    <div class="page-body">
      <div class="stats-grid">
        <div class="stat-card"><div class="stat-value" style="color: var(--brand-fg)">{{ stats.device }}</div><div class="stat-label">设备总数</div></div>
        <div class="stat-card"><div class="stat-value" style="color: var(--success)">{{ stats.connected }}</div><div class="stat-label">在线设备</div></div>
        <div class="stat-card"><div class="stat-value" style="color: var(--brand-fg)">{{ stats.detect }}</div><div class="stat-label">侦测设备</div></div>
        <div class="stat-card"><div class="stat-value" style="color: var(--warning)">{{ stats.disturb }}</div><div class="stat-label">干扰设备</div></div>
        <div class="stat-card"><div class="stat-value" style="color: var(--danger)">{{ stats.trap }}</div><div class="stat-label">诱骗设备</div></div>
        <div class="stat-card"><div class="stat-value" style="color: var(--success)">{{ stats.todayDetect }}</div><div class="stat-label">今日侦测</div></div>
        <div class="stat-card"><div class="stat-value" style="color: var(--warning)">{{ stats.alarm }}</div><div class="stat-label">告警记录</div></div>
        <div class="stat-card"><div class="stat-value" style="color: var(--brand-fg)">{{ stats.airspace }}</div><div class="stat-label">管控空域</div></div>
        <div class="stat-card"><div class="stat-value" style="color: var(--brand-fg)">{{ stats.drone }}</div><div class="stat-label">登记无人机</div></div>
      </div>

      <div class="panel">
        <div class="panel-title">最近告警</div>
        <el-table :data="recentAlarms" border size="small">
          <el-table-column prop="wrjpp" label="无人机品牌" width="140" />
          <el-table-column prop="wrjxh" label="型号" width="140" />
          <el-table-column prop="gjlx" label="告警类型" width="120" />
          <el-table-column prop="gjfssj" label="发生时间" width="180" />
          <el-table-column prop="kymc" label="空域" show-overflow-tooltip />
          <template #empty><el-empty description="暂无告警" :image-size="60" /></template>
        </el-table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dt-page { width: 100%; height: 100%; display: flex; flex-direction: column; background: var(--bg-canvas); color: var(--fg-1); }
.page-header { padding: 20px 24px 12px; border-bottom: 1px solid var(--divider); }
.page-title { font-size: 18px; font-weight: 700; margin: 0; }
.page-desc { font-size: 13px; color: var(--fg-3); margin: 4px 0 0; }
.page-body { flex: 1; padding: 16px 24px; overflow-y: auto; }
.stats-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 16px; }
.stat-card { background: var(--bg-surface); border: 1px solid var(--divider); border-radius: var(--radius-md); padding: 20px; text-align: center; }
.stat-value { font-size: 32px; font-weight: 700; text-shadow: 0 0 16px currentColor; }
.stat-label { font-size: 13px; color: var(--fg-3); margin-top: 4px; }
.panel { margin-top: 20px; background: var(--bg-surface); border: 1px solid var(--divider); border-radius: var(--radius-md); padding: 16px; }
.panel-title { font-size: 14px; font-weight: 600; color: var(--fg-2); margin-bottom: 12px; }
</style>
