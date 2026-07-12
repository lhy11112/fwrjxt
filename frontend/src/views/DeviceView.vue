<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useDeviceStore, DEVICE_TYPE_META } from '@/stores/device'
import { listAllDevices } from '@/api'
import DeviceConfigDialog from '@/components/DeviceConfigDialog.vue'
import type { DeviceInfo } from '@/types'

const device = useDeviceStore()
const loading = ref(true)
const error = ref('')
const configDevice = ref<DeviceInfo | null>(null)
const showConfig = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const list = await listAllDevices()
    device.setDevices(list)
  } catch (e: any) {
    error.value = e?.message || '加载失败'
  }
  loading.value = false
})

function getMeta(kind: string) {
  return DEVICE_TYPE_META[kind] || { label: kind, icon: '📟', group: '' }
}

const DEVICE_ICON: Record<string, string> = {
  RADAR: 'radar', RF: 'signal', EO_IR: 'camera', ACOUSTIC: 'mic', LIDAR: 'bulb',
  CUAS_RADAR: 'satellite', CUAS_RADIO_FREQ: 'signal', CUAS_OPTICAL: 'scope',
  CUAS_INFRARED: 'thermal', CUAS_JAMMER: 'bolt', CUAS_SPOOFER: 'refresh', CUAS_CONTROL_BOX: 'box',
}
function deviceIcon(kind: string) { return DEVICE_ICON[kind] || 'device' }

function openConfig(d: DeviceInfo, event: MouseEvent) {
  event.stopPropagation()
  configDevice.value = d
  showConfig.value = true
}

function quickTest(d: DeviceInfo, event: MouseEvent) {
  event.stopPropagation()
  configDevice.value = d
  showConfig.value = true
}

function handleTest(deviceId: string, cmd: string, params: Record<string, any>) {
  console.log('[设备测试]', deviceId, cmd, params)
}
</script>

<template>
  <div class="device-page">
    <div class="dp-header">
      <h2><Icon name="device" :size="18" />设备管理</h2>
      <div class="dp-stat" v-if="!loading">
        <span>共 <strong>{{ device.stats.total }}</strong> 台设备</span>
        <span class="sep">|</span>
        <span>在线 <strong class="on">{{ device.stats.online }}</strong></span>
        <span class="sep">|</span>
        <span>离线 <strong class="off">{{ device.stats.offline }}</strong></span>
      </div>
    </div>

    <div v-if="loading" class="dp-loading">加载中...</div>
    <div v-else-if="error" class="dp-error">{{ error }}</div>

    <template v-else>
      <!-- HSimC2 传感器分组 -->
      <div class="dp-group" v-if="device.hsimc2Sensors.length">
        <div class="dp-group-title"><Icon name="satellite" :size="15" />HSimC2 传感器 <span class="g-count">{{ device.hsimc2Sensors.length }}</span></div>
        <div class="dp-grid">
          <div v-for="d in device.hsimc2Sensors" :key="d.device_id" class="dp-card">
            <div class="dpc-head">
              <span class="dpc-icon"><Icon :name="deviceIcon(d.device_kind)" :size="18" /></span>
              <div class="dpc-info">
                <div class="dpc-name">{{ d.name }}</div>
                <div class="dpc-model">{{ d.model }}</div>
              </div>
              <span class="dpc-status" :class="d.online ? 'on' : 'off'">{{ d.status_label }}</span>
            </div>
            <div class="dpc-body">
              <div class="dpc-row"><span>位置</span><span>{{ d.latitude.toFixed(4) }}, {{ d.longitude.toFixed(4) }}</span></div>
              <div class="dpc-row"><span>能力</span><span>{{ d.capabilities.join(', ') }}</span></div>
            </div>
            <div class="dpc-actions">
              <button class="dpc-act-btn cfg" @click="openConfig(d, $event)"><Icon name="settings" :size="12" />配置</button>
              <button class="dpc-act-btn test" @click="quickTest(d, $event)"><Icon name="flask" :size="12" />测试</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 反无设备分组 -->
      <div class="dp-group" v-if="device.cuasDevices.length">
        <div class="dp-group-title"><Icon name="shield" :size="15" />反无人机系统设备 <span class="g-count">{{ device.cuasDevices.length }}</span></div>
        <div class="dp-grid">
          <div v-for="d in device.cuasDevices" :key="d.device_id" class="dp-card">
            <div class="dpc-head">
              <span class="dpc-icon"><Icon :name="deviceIcon(d.device_kind)" :size="18" /></span>
              <div class="dpc-info">
                <div class="dpc-name">{{ d.name }}</div>
                <div class="dpc-model">{{ d.model }}</div>
              </div>
              <span class="dpc-status" :class="d.online ? 'on' : 'off'">{{ d.status_label }}</span>
            </div>
            <div class="dpc-body">
              <div class="dpc-row"><span>位置</span><span>{{ d.latitude.toFixed(4) }}, {{ d.longitude.toFixed(4) }}</span></div>
              <div class="dpc-row"><span>能力</span><span>{{ d.capabilities.join(', ') }}</span></div>
              <div class="dpc-row cuas" v-if="d.cuas_ip"><span>网络</span><span>{{ d.cuas_ip }}:{{ d.cuas_port }}</span></div>
              <div class="dpc-row cuas" v-if="d.cuas_ptz"><span>云台</span><span>水平 {{ d.cuas_pan_angle ?? '-' }}° / 俯仰 {{ d.cuas_tilt_angle ?? '-' }}°</span></div>
              <div class="dpc-row cuas" v-if="d.cuas_rtsp_url"><span>视频</span><span>{{ d.cuas_rtsp_url }}</span></div>
            </div>
            <div class="dpc-actions">
              <button class="dpc-act-btn cfg" @click="openConfig(d, $event)"><Icon name="settings" :size="12" />配置</button>
              <button class="dpc-act-btn test" @click="quickTest(d, $event)"><Icon name="flask" :size="12" />测试</button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 设备配置对话框 -->
    <DeviceConfigDialog
      :visible="showConfig"
      :device="configDevice"
      @close="showConfig = false"
      @test="handleTest"
    />
  </div>
</template>

<style scoped>
.device-page { height:100%; padding:20px; overflow-y:auto; background:rgba(6,11,24,0.5); }
.dp-header { display:flex; align-items:center; gap:16px; margin-bottom:16px; }
.dp-header h2 { font-size:16px; font-weight:700; color:#e0e8f0; margin:0; }
.dp-stat { font-size:12px; color:#607d8b; display:flex; gap:8px; }
.dp-stat strong.on { color:#66bb6a; }
.dp-stat strong.off { color:#ef5350; }
.sep { color:#37474f; }
.dp-loading, .dp-error { padding:60px; text-align:center; color:#607d8b; }

.dp-group { margin-bottom:24px; }
.dp-group-title { font-size:14px; font-weight:600; color:#90a4ae; margin-bottom:10px; display:flex; align-items:center; gap:8px; }
.g-count { padding:0 8px; background:rgba(79,195,247,0.08); border-radius:8px; font-size:11px; color:#4fc3f7; }

.dp-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(340px,1fr)); gap:10px; }
.dp-card {
  background:rgba(13,25,48,0.6); border:1px solid rgba(79,195,247,0.06);
  border-radius:6px; overflow:hidden; transition:all .15s;
  display:flex; flex-direction:column;
}
.dp-card:hover { border-color:rgba(79,195,247,0.15); }
.dpc-head { display:flex; align-items:center; gap:10px; padding:10px 14px; border-bottom:1px solid rgba(255,255,255,0.03); }
.dpc-icon { font-size:20px; }
.dpc-info { flex:1; }
.dpc-name { font-size:13px; font-weight:600; color:#e0e0e0; }
.dpc-model { font-size:10px; color:#546e7a; }
.dpc-status { padding:2px 10px; border-radius:3px; font-size:11px; }
.dpc-status.on { background:rgba(76,175,80,0.12); color:#66bb6a; }
.dpc-status.off { background:rgba(239,83,80,0.12); color:#ef5350; }
.dpc-body { padding:8px 14px; flex:1; }
.dpc-row { display:flex; justify-content:space-between; padding:2px 0; font-size:12px; color:#607d8b; }
.dpc-row span:last-child { color:#b0bec5; }
.dpc-row.cuas { font-size:11px; }
.dpc-row.cuas span:last-child { font-family:monospace; font-size:10px; }

/* ---- 操作按钮 ---- */
.dpc-actions {
  display: flex; gap: 6px; padding: 8px 14px;
  border-top: 1px solid rgba(255,255,255,0.03);
}
.dpc-act-btn {
  flex: 1; padding: 5px 0; text-align: center;
  border-radius: 3px; font-size: 11px;
  cursor: pointer; border: 1px solid transparent; transition: all 0.15s;
}
.dpc-act-btn.cfg {
  background: rgba(79,195,247,0.06); border-color: rgba(79,195,247,0.1);
  color: #4fc3f7;
}
.dpc-act-btn.cfg:hover { background: rgba(79,195,247,0.14); }
.dpc-act-btn.test {
  background: rgba(255,213,79,0.06); border-color: rgba(255,213,79,0.1);
  color: #ffd54f;
}
.dpc-act-btn.test:hover { background: rgba(255,213,79,0.14); }
</style>
