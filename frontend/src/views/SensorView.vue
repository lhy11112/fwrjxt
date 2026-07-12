<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { listSensors } from '@/api'
import { useSensorStore } from '@/stores/sensor'

const sensor = useSensorStore()

onMounted(async () => {
  try { sensor.setSensors(await listSensors()) } catch {}
})
</script>

<template>
  <div class="sensor-scene">
    <div class="sensor-grid">
      <div v-for="s in sensor.sensors" :key="s.sensor_id" class="s-card">
        <div class="sc-header">
          <span class="sc-icon"><Icon :name="s.sensor_type === 'RADAR' ? 'radar' : s.sensor_type === 'RF' ? 'signal' : 'camera'" :size="20" /></span>
          <div class="sc-info">
            <div class="sc-name">{{ s.name }}</div>
            <div class="sc-model">{{ s.model }}</div>
          </div>
          <span class="sc-status" :class="s.status.toLowerCase()">{{ s.status }}</span>
        </div>
        <div class="sc-body">
          <div class="sc-row"><span class="sc-l">位置</span><span class="sc-v">{{ s.location.latitude.toFixed(4) }}, {{ s.location.longitude.toFixed(4) }}</span></div>
          <div class="sc-row"><span class="sc-l">能力</span><span class="sc-v">{{ s.capabilities.join(', ') }}</span></div>
          <div class="sc-row"><span class="sc-l">状态</span><span class="sc-v">{{ s.last_heartbeat ? '心跳正常' : '等待数据' }}</span></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.sensor-scene { height:100%; padding:20px; overflow-y:auto; background:rgba(6,11,24,0.5); }
.sensor-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(280px,1fr)); gap:12px; }
.s-card {
  background:rgba(13,25,48,0.7); border:1px solid rgba(79,195,247,0.08); border-radius:4px;
  transition:all .2s;
}
.s-card:hover { border-color:rgba(79,195,247,0.2); }
.sc-header { display:flex; align-items:center; gap:10px; padding:12px 14px; border-bottom:1px solid rgba(255,255,255,0.04); }
.sc-icon { font-size:22px; }
.sc-info { flex:1; }
.sc-name { font-size:13px; font-weight:600; color:#e0e0e0; }
.sc-model { font-size:10px; color:#546e7a; }
.sc-status { padding:2px 10px; border-radius:3px; font-size:11px; }
.sc-status.online { background:rgba(76,175,80,0.12); color:#66bb6a; }
.sc-status.offline { background:rgba(239,83,80,0.12); color:#ef5350; }
.sc-body { padding:10px 14px; }
.sc-row { display:flex; justify-content:space-between; padding:3px 0; font-size:12px; }
.sc-l { color:#607d8b; }
.sc-v { color:#b0bec5; }
</style>
