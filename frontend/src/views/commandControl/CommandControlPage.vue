<script setup lang="ts">
/** 指挥控制 — 完整功能 */
import { ref, onMounted } from 'vue'
import api from '@/api/index'
import * as wxdzcApi from '@/api/wxdzc'

const devices = ref<any[]>([])
const drones = ref<any[]>([])
const opLogs = ref<any[]>([])
const today = new Date().toISOString().slice(0, 10)

onMounted(async () => {
  try { const r = await wxdzcApi.listDevices({ page: 1, page_size: 100 }); devices.value = r.records } catch {}
  try { drones.value = await wxdzcApi.getByStation({ rq: today }) } catch {}
  try { const { data } = await api.get('/devices/operate-logs', { params: { page: 1, page_size: 20 } }); opLogs.value = data.result?.records || [] } catch {}
})
</script>

<template>
  <div class="cmd-page">
    <div class="page-header"><h2 class="page-title">指挥控制</h2><p class="page-desc">反制指令下发与设备控制</p></div>
    <div class="page-body">
      <div class="grid-2">
        <div class="card">
          <div class="card-title">在线设备 ({{ devices.filter((d:any)=>d.status==='CONNECTED').length }}/{{ devices.length }})</div>
          <div v-for="d in devices" :key="d.id" class="dev-row" :class="{ online: d.status==='CONNECTED' }">
            <span>{{ d.name || d.device_id }}</span>
            <span>{{ d.device_type }}</span>
            <span :class="d.status==='CONNECTED'?'on':'off'">{{ d.status==='CONNECTED'?'在线':'离线' }}</span>
          </div>
        </div>
        <div class="card">
          <div class="card-title">今日侦测 ({{ drones.length }} 架)</div>
          <div v-for="d in drones.slice(0, 8)" :key="d.id" class="dev-row">
            <span>{{ d.model }}</span>
            <span :class="d.wxdj==='red'?'t-red':d.wxdj==='yellow'?'t-yellow':'t-green'">{{ d.wxdj }}</span>
            <span>{{ d.data_time }}</span>
          </div>
        </div>
      </div>
      <div class="card" style="margin-top:16px">
        <div class="card-title">操作日志</div>
        <div v-for="l in opLogs.slice(0,10)" :key="l.id" class="log-row">
          <span>{{ l.cmd_name }}</span>
          <span>{{ l.result }}</span>
          <span>{{ l.operate_time }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cmd-page { width:100%; height:100%; display:flex; flex-direction:column; background:var(--bg-canvas); color:var(--fg-1); }
.page-header { padding:20px 24px 12px; border-bottom:1px solid var(--divider); }
.page-title { font-size:18px; font-weight:700; margin:0; } .page-desc { font-size:13px; color:var(--fg-3); margin:4px 0 0; }
.page-body { flex:1; padding:16px 24px; overflow-y:auto; }
.grid-2 { display:grid; grid-template-columns:1fr 1fr; gap:16px; }
.card { background:var(--bg-surface); border:1px solid var(--divider); border-radius:8px; padding:16px; }
.card-title { font-size:14px; font-weight:600; margin-bottom:10px; }
.dev-row { display:flex; justify-content:space-between; padding:6px 0; font-size:12px; border-bottom:1px solid rgba(47,122,212,0.1); }
.dev-row.online { background:rgba(111,214,111,0.05); }
.log-row { display:flex; justify-content:space-between; padding:6px 0; font-size:12px; color:var(--fg-3); }
.on { color:var(--success); } .off { color:var(--danger); }
.t-red { color:var(--danger); } .t-yellow { color:var(--warning); } .t-green { color:var(--success); }
</style>
