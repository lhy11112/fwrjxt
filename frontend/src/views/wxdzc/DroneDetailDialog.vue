<script setup lang="ts">
/**
 * 无人机详情对话框 — 移植自源项目 wrjDetailDialog.vue
 */
import { ref, reactive } from 'vue'

interface DroneForm {
  ssdw?: string; rwlx?: string; model?: string; serial?: string
  dron_lng?: number; dron_lat?: number; altitude?: number; station_name?: string
  home_lng?: number; home_lat?: number; pilot_lng?: number; pilot_lat?: number
  height?: number; east_v?: number; north_v?: number; up_v?: number
  freq?: number; rssi?: number; distance?: number; uuid?: string
  angle?: number; data_time?: string; create_time?: string
}

const visible = ref(false)
const form = reactive<DroneForm>({})
const flag = ref(false)

function open(data: any, hideExtra?: boolean) {
  flag.value = hideExtra ?? false
  Object.assign(form, {
    ssdw: data.ssdw || '',
    rwlx: data.rwlx || '',
    model: data.model || '',
    serial: data.serial || '',
    dron_lng: data.dron_lng,
    dron_lat: data.dron_lat,
    altitude: data.altitude,
    station_name: data.station_name || '',
    home_lng: data.home_lng,
    home_lat: data.home_lat,
    pilot_lng: data.pilot_lng,
    pilot_lat: data.pilot_lat,
    height: data.height,
    east_v: data.east_v,
    north_v: data.north_v,
    up_v: data.up_v,
    freq: data.freq,
    rssi: data.rssi,
    distance: data.distance ? Number((data.distance / 1000).toFixed(2)) : undefined,
    uuid: data.uuid || '',
    angle: data.angle,
    data_time: data.data_time || '',
    create_time: data.create_time || '',
  })
  visible.value = true
}

function close() {
  visible.value = false
}

function fmt(v: any, p = 3): string {
  if (v === null || v === undefined) return '-'
  if (typeof v === 'number') return Number(v.toFixed(p)).toString()
  return String(v)
}

defineExpose({ open })
</script>

<template>
  <el-dialog v-model="visible" title="无人机信息" width="90%" :max-width="1000" :append-to-body="true">
    <el-form :model="form" label-width="120px" size="small">
      <el-row :gutter="16">
        <el-col v-if="!flag" :span="12">
          <el-form-item label="所属单位"><span class="form-value">{{ form.ssdw }}</span></el-form-item>
        </el-col>
        <el-col v-if="!flag" :span="12">
          <el-form-item label="任务类型"><span class="form-value">{{ form.rwlx }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="型号"><span class="form-value">{{ form.model }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="序列号"><span class="form-value">{{ form.serial }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="经度"><span class="form-value">{{ fmt(form.dron_lng) }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="纬度"><span class="form-value">{{ fmt(form.dron_lat) }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="海拔高度"><span class="form-value">{{ fmt(form.altitude) }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="发现站点"><span class="form-value">{{ form.station_name }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="起飞点经度"><span class="form-value">{{ fmt(form.home_lng) }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="起飞点纬度"><span class="form-value">{{ fmt(form.home_lat) }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="遥控器经度"><span class="form-value">{{ fmt(form.pilot_lng) }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="遥控器纬度"><span class="form-value">{{ fmt(form.pilot_lat) }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="高度"><span class="form-value">{{ fmt(form.height) }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="东速度"><span class="form-value">{{ fmt(form.east_v) }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="北速度"><span class="form-value">{{ fmt(form.north_v) }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="上速度"><span class="form-value">{{ fmt(form.up_v) }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="频率"><span class="form-value">{{ form.freq ? (form.freq / 1000000).toFixed(2) + ' MHz' : '-' }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="信号强度"><span class="form-value">{{ fmt(form.rssi) }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="距离(Km)"><span class="form-value">{{ form.distance ?? '-' }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="飞手执照代码"><span class="form-value">{{ form.uuid || '-' }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="飞机角度"><span class="form-value">{{ fmt(form.angle) }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="时间戳"><span class="form-value">{{ form.data_time }}</span></el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="入库时间"><span class="form-value">{{ form.create_time }}</span></el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="close">关闭</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.form-value { color: #c6e0f9; font-weight: 500; }
</style>
