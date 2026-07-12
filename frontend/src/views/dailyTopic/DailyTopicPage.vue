<script setup lang="ts">
/**
 * 综合态势 (Daily Topic / Portal) — 移植自源项目 portal/dailyTopic
 */
import { ref, onMounted } from 'vue'
import { listDevices, getByStation } from '@/api/wxdzc'

const deviceCount = ref(0)
const droneCount = ref(0)
const today = new Date().toISOString().slice(0, 10)

onMounted(async () => {
  try {
    const devices = await listDevices({ page: 1, page_size: 1 })
    deviceCount.value = devices.total
  } catch {}
  try {
    const drones = await getByStation({ rq: today })
    droneCount.value = drones.length
  } catch {}
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
        <div class="stat-card">
          <div class="stat-value" style="color: var(--brand-fg)">{{ deviceCount }}</div>
          <div class="stat-label">在线设备</div>
        </div>
        <div class="stat-card">
          <div class="stat-value" style="color: var(--success)">{{ droneCount }}</div>
          <div class="stat-label">今日侦测</div>
        </div>
        <div class="stat-card">
          <div class="stat-value" style="color: var(--warning)">0</div>
          <div class="stat-label">待处理告警</div>
        </div>
        <div class="stat-card">
          <div class="stat-value" style="color: var(--danger)">0</div>
          <div class="stat-label">高危目标</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dt-page {
  width: 100%; height: 100%; display: flex; flex-direction: column;
  background: var(--bg-canvas); color: var(--fg-1);
}
.page-header { padding: 20px 24px 12px; border-bottom: 1px solid var(--divider); }
.page-title { font-size: 18px; font-weight: 700; margin: 0; }
.page-desc { font-size: 13px; color: var(--fg-3); margin: 4px 0 0; }
.page-body { flex: 1; padding: 16px 24px; overflow-y: auto; }
.stats-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px; }
.stat-card {
  background: var(--bg-surface); border: 1px solid var(--divider);
  border-radius: var(--radius-md); padding: 24px; text-align: center;
}
.stat-value { font-size: 36px; font-weight: 700; text-shadow: 0 0 16px currentColor; }
.stat-label { font-size: 13px; color: var(--fg-3); margin-top: 4px; }
</style>
