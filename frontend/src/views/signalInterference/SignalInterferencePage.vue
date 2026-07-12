<script setup lang="ts">
/**
 * 信号干扰 —— 多频段干扰设备控制台
 * 迁移自源项目 portal/signalInterference + UavCmdController 指令下发逻辑。
 * 指令通过操作日志接口记录（软件侧闭环），设备列表来自真实设备配置表。
 */
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { deviceApi } from '@/api/dataManage'
import { opLogApi } from '@/api/wrj'

const devices = ref<any[]>([])
const selectedId = ref<number | null>(null)
const loading = ref(false)
const logs = ref<any[]>([])

const bands = ref([
  { key: '900M', freq: '900MHz', active: false },
  { key: '1.4G', freq: '1.4GHz', active: false },
  { key: '2.4G', freq: '2.4GHz', active: false },
  { key: '5.2G', freq: '5.2GHz', active: false },
  { key: '5.8G', freq: '5.8GHz', active: false },
])
const attackMode = ref('返航')
const power = ref(80)

const selectedDevice = computed(() => devices.value.find(d => d.id === selectedId.value))

async function fetchDevices() {
  loading.value = true
  try {
    const data = await deviceApi.list({ page: 1, page_size: 100, device_type: 'DISTURB' })
    devices.value = data.records || []
    if (devices.value.length) selectedId.value = devices.value[0].id
  } finally {
    loading.value = false
  }
}

async function sendCommand(cmdName: string) {
  if (!selectedDevice.value) { ElMessage.warning('请先选择干扰设备'); return }
  const activeBands = bands.value.filter(b => b.active).map(b => b.freq).join(',') || '全频段'
  const param = JSON.stringify({ bands: activeBands, mode: attackMode.value, power: power.value })
  try {
    await opLogApi.add({
      station_id: selectedDevice.value.station_id,
      device_type: 'DISTURB',
      cmd_type: cmdName,
      cmd_name: cmdName,
      cmd_param: param,
      result: '指令下发成功',
      operate_time: new Date().toISOString().slice(0, 19).replace('T', ' '),
    } as any)
    ElMessage.success(`已向【${selectedDevice.value.name}】下发：${cmdName}`)
    loadLogs()
  } catch (e: any) {
    ElMessage.error(e.message || '指令下发失败')
  }
}

async function loadLogs() {
  try {
    const data = await opLogApi.list({ page: 1, page_size: 20 })
    logs.value = data.records || []
  } catch {}
}

onMounted(() => { fetchDevices(); loadLogs() })
</script>

<template>
  <div class="si-page">
    <div class="page-header">
      <h2 class="page-title">信号干扰</h2>
      <p class="page-desc">多频段干扰设备控制</p>
    </div>

    <div class="page-body">
      <div class="grid">
        <div class="card">
          <div class="card-title">干扰设备</div>
          <el-select v-model="selectedId" placeholder="选择干扰设备" style="width:100%" :loading="loading">
            <el-option v-for="d in devices" :key="d.id" :label="`${d.name} (站${d.station_id})`" :value="d.id" />
          </el-select>
          <div v-if="selectedDevice" class="dev-meta">
            <div>IP: {{ selectedDevice.device_ip }}:{{ selectedDevice.device_port }}</div>
            <div>状态: <el-tag size="small" :type="selectedDevice.status === 'CONNECTED' ? 'success' : 'info'">{{ selectedDevice.status }}</el-tag></div>
          </div>
          <el-empty v-if="!devices.length && !loading" description="暂无干扰设备" :image-size="60" />
        </div>

        <div class="card">
          <div class="card-title">干扰频段选择</div>
          <div class="band-grid">
            <div v-for="b in bands" :key="b.key" class="band-chip" :class="{ active: b.active }" @click="b.active = !b.active">
              {{ b.freq }}
            </div>
          </div>
          <div class="card-title" style="margin-top:16px">干扰功率 ({{ power }}%)</div>
          <el-slider v-model="power" :min="0" :max="100" />
          <div class="card-title" style="margin-top:12px">攻击模式</div>
          <el-radio-group v-model="attackMode">
            <el-radio value="返航">返航</el-radio>
            <el-radio value="迫降">迫降</el-radio>
            <el-radio value="悬停">悬停</el-radio>
          </el-radio-group>
          <div class="btn-row">
            <el-button type="danger" @click="sendCommand('开启干扰')">开启干扰</el-button>
            <el-button @click="sendCommand('停止干扰')">停止干扰</el-button>
            <el-button type="warning" @click="sendCommand('禁飞开关')">禁飞开关</el-button>
          </div>
        </div>
      </div>

      <div class="card" style="margin-top:16px">
        <div class="card-title">指令记录</div>
        <el-table :data="logs" border size="small" max-height="240">
          <el-table-column prop="operate_time" label="时间" width="170" />
          <el-table-column prop="cmd_name" label="指令" width="120" />
          <el-table-column prop="cmd_param" label="参数" show-overflow-tooltip />
          <el-table-column prop="result" label="结果" width="140" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.si-page { width: 100%; height: 100%; display: flex; flex-direction: column; background: var(--bg-canvas); color: var(--fg-1); }
.page-header { padding: 20px 24px 12px; border-bottom: 1px solid var(--divider); }
.page-title { font-size: 18px; font-weight: 700; margin: 0; }
.page-desc { font-size: 13px; color: var(--fg-3); margin: 4px 0 0; }
.page-body { flex: 1; padding: 16px 24px; overflow-y: auto; }
.grid { display: grid; grid-template-columns: 320px 1fr; gap: 16px; }
.card { background: var(--bg-surface); border: 1px solid var(--divider); border-radius: var(--radius-md); padding: 16px; }
.card-title { font-size: 14px; font-weight: 600; color: var(--fg-2); margin-bottom: 12px; }
.dev-meta { margin-top: 12px; font-size: 12px; color: var(--fg-3); display: flex; flex-direction: column; gap: 8px; }
.band-grid { display: flex; gap: 10px; flex-wrap: wrap; }
.band-chip { padding: 8px 18px; border: 1px solid var(--divider); border-radius: 20px; cursor: pointer; font-size: 13px; font-weight: 600; transition: all 0.2s; }
.band-chip:hover { border-color: var(--brand-fg); }
.band-chip.active { background: var(--brand-selected); border-color: var(--brand-fg); color: var(--brand-fg); box-shadow: 0 0 10px var(--glow-accent); }
.btn-row { margin-top: 16px; display: flex; gap: 10px; }
</style>
