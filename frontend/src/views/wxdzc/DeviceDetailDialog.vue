<script setup lang="ts">
/**
 * 设备详情对话框 — 移植自源项目 deviceDetail.vue
 */
import { ref, reactive } from 'vue'

interface DeviceForm {
  name?: string
  device_id?: string
  station_id?: number
  device_type?: string
  device_ip?: string
  device_port?: number
  is_valid?: number
  status?: string
  jd?: number
  wd?: number
  zcbj?: string
  dyqk?: string
}

const visible = ref(false)
const form = reactive<DeviceForm>({})

const deviceTypeMap: Record<string, string> = {
  DETECT: '侦测',
  TRAP: '诱骗',
  DISTURB: '干扰',
}

function open(data: any) {
  Object.assign(form, {
    name: data.name || data.device_id,
    device_id: data.device_id,
    station_id: data.station_id,
    device_type: deviceTypeMap[data.device_type] || data.device_type,
    device_ip: data.device_ip,
    device_port: data.device_port,
    is_valid: data.is_valid,
    status: data.status === 'CONNECTED' ? '已连接' : '未连接',
    jd: data.jd,
    wd: data.wd,
    zcbj: data.zcbj,
    dyqk: data.dyqk,
  })
  visible.value = true
}

function close() {
  visible.value = false
}

defineExpose({ open })
</script>

<template>
  <el-dialog v-model="visible" title="设备信息" width="90%" :max-width="1000" :append-to-body="true">
    <el-form :model="form" label-width="120px" size="small">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="设备名称">
            <span class="form-value">{{ form.name }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="设备唯一标识">
            <span class="form-value">{{ form.device_id }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="站ID">
            <span class="form-value">{{ form.station_id }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="设备类型">
            <span class="form-value">{{ form.device_type }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="设备IP地址">
            <span class="form-value">{{ form.device_ip }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="设备端口">
            <span class="form-value">{{ form.device_port }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="有效性">
            <span :class="form.is_valid === 1 ? 'value-ok' : 'value-bad'">
              {{ form.is_valid === 1 ? '正常' : '禁用' }}
            </span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="连接状态">
            <span :class="form.status === '已连接' ? 'value-ok' : 'value-bad'">
              {{ form.status }}
            </span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="经度">
            <span class="form-value">{{ form.jd ?? '-' }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="纬度">
            <span class="form-value">{{ form.wd ?? '-' }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="侦测半径">
            <span class="form-value">{{ form.zcbj || '-' }}</span>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="弹药情况">
            <span class="form-value">{{ form.dyqk || '-' }}</span>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="close">关闭</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.form-value {
  color: #c6e0f9;
  font-weight: 500;
}
.value-ok { color: #6fd66f; font-weight: 600; }
.value-bad { color: #ff7a86; font-weight: 600; }
</style>
