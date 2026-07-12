<script setup lang="ts">
/** 告警回放 — 完整功能 */
import { ref, onMounted } from 'vue'
import api from '@/api/index'
import * as wxdzcApi from '@/api/wxdzc'

const alarms = ref<any[]>([])
const loading = ref(false)
const currentDate = ref(new Date().toISOString().slice(0, 10))
const selectedAlarm = ref<any>(null)
const flightPath = ref<any[]>([])

async function loadAlarms() {
  loading.value = true
  try {
    const { data } = await api.get('/alarms', { params: { page: 1, page_size: 50 } })
    alarms.value = data.result?.records || []
  } catch { alarms.value = [] }
  loading.value = false
}

async function replayAlarm(alarm: any) {
  selectedAlarm.value = alarm
  if (alarm.wrjxlh) {
    try {
      const r = await wxdzcApi.getByModelSerial({ serial: alarm.wrjxlh, rq: currentDate.value })
      flightPath.value = r.uav_detect_msg_list
    } catch { flightPath.value = [] }
  }
}

onMounted(loadAlarms)
</script>

<template>
  <div class="gjhf-page">
    <div class="page-header">
      <h2 class="page-title">告警回放</h2>
      <p class="page-desc">告警记录查询与飞行轨迹回放</p>
    </div>
    <div class="page-body">
      <div class="filter-bar">
        <input type="date" v-model="currentDate" class="date-input" />
        <button class="btn" @click="loadAlarms" :disabled="loading">查询</button>
      </div>
      <div v-if="alarms.length" class="alarm-list">
        <div v-for="a in alarms" :key="a.id" class="alarm-card" :class="{ selected: selectedAlarm?.id === a.id }" @click="replayAlarm(a)">
          <div class="alarm-head">
            <span class="alarm-type">{{ a.gjlx || '未知' }}</span>
            <span :class="a.clzt === '0' ? 'badge-warn' : 'badge-ok'">{{ a.clzt === '0' ? '未处理' : '已处理' }}</span>
          </div>
          <div class="alarm-info">
            <div>空域: {{ a.kymc || '-' }}</div>
            <div>型号: {{ a.wrjxh || '-' }}</div>
            <div>序列号: {{ a.wrjxlh || '-' }}</div>
            <div>时间: {{ a.gjfssj || '-' }}</div>
          </div>
        </div>
      </div>
      <div v-else class="empty">暂无告警数据</div>
      <div v-if="selectedAlarm && flightPath.length" class="flight-info">
        <h3>飞行路径数据 ({{ flightPath.length }} 个航点)</h3>
        <div class="path-points">
          <div v-for="(p, i) in flightPath.slice(0, 10)" :key="i" class="path-point">
            [{{ p.dron_lat?.toFixed(4) }}, {{ p.dron_lng?.toFixed(4) }}] @ {{ p.data_time }}
          </div>
          <div v-if="flightPath.length > 10">... 共 {{ flightPath.length }} 个航点</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.gjhf-page { width:100%; height:100%; display:flex; flex-direction:column; background:var(--bg-canvas); color:var(--fg-1); }
.page-header { padding:20px 24px 12px; border-bottom:1px solid var(--divider); }
.page-title { font-size:18px; font-weight:700; margin:0; } .page-desc { font-size:13px; color:var(--fg-3); margin:4px 0 0; }
.page-body { flex:1; padding:16px 24px; overflow-y:auto; }
.filter-bar { display:flex; gap:8px; margin-bottom:12px; }
.date-input { padding:6px 10px; background:var(--bg-surface); border:1px solid var(--divider); border-radius:4px; color:var(--fg-1); }
.btn { padding:6px 16px; background:var(--brand-rest); border:none; border-radius:4px; color:#fff; cursor:pointer; }
.alarm-list { display:flex; flex-direction:column; gap:8px; }
.alarm-card { padding:12px; background:var(--bg-surface); border:1px solid var(--divider); border-radius:8px; cursor:pointer; }
.alarm-card:hover, .alarm-card.selected { border-color:var(--brand-fg); background:var(--brand-selected); }
.alarm-head { display:flex; justify-content:space-between; align-items:center; margin-bottom:6px; }
.alarm-type { font-weight:600; } .badge-warn { font-size:11px; padding:2px 8px; border-radius:10px; background:var(--danger-bg); color:var(--danger); }
.badge-ok { font-size:11px; padding:2px 8px; border-radius:10px; background:var(--success-bg); color:var(--success); }
.alarm-info { font-size:12px; color:var(--fg-3); display:grid; grid-template-columns:1fr 1fr; gap:4px; }
.flight-info { margin-top:16px; padding:12px; background:var(--bg-surface); border:1px solid var(--divider); border-radius:8px; }
.flight-info h3 { font-size:14px; margin:0 0 8px; } .path-points { font-size:12px; color:var(--fg-3); }
.empty { text-align:center; padding:60px; color:var(--fg-4); }
</style>
