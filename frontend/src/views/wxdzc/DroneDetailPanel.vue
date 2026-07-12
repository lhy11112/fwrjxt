<script setup lang="ts">
/**
 * 无人机详情面板
 */
import type { DroneItem } from '@/stores/wxdzc'

interface Props {
  drone: DroneItem
}

defineProps<Props>()
const emit = defineEmits<{
  toggle: []
  close: []
}>()

const fields: { key: string; label: string }[] = [
  { key: 'model', label: '型号' },
  { key: 'serial', label: '序列号' },
  { key: 'brand', label: '品牌' },
  { key: 'station_name', label: '站点' },
  { key: 'dron_lng', label: '经度' },
  { key: 'dron_lat', label: '纬度' },
  { key: 'home_lng', label: '起飞点经度' },
  { key: 'home_lat', label: '起飞点纬度' },
  { key: 'pilot_lng', label: '遥控器经度' },
  { key: 'pilot_lat', label: '遥控器纬度' },
  { key: 'altitude', label: '高度(m)' },
  { key: 'height', label: '相对高度(m)' },
  { key: 'east_v', label: '东速度' },
  { key: 'north_v', label: '北速度' },
  { key: 'up_v', label: '上速度' },
  { key: 'freq', label: '频率(MHz)' },
  { key: 'rssi', label: '信号强度' },
  { key: 'distance', label: '距离(m)' },
  { key: 'uuid', label: '飞手执照' },
  { key: 'angle', label: '角度' },
  { key: 'data_time', label: '数据时间' },
  { key: 'create_time', label: '入库时间' },
]
</script>

<template>
  <div class="detail-panel">
    <div class="panel-header">
      <span class="panel-title">侦测无人机详细信息</span>
      <div class="header-actions">
        <button class="toggle-btn" @click="emit('toggle')">_</button>
        <button class="close-btn" @click="emit('close')">✕</button>
      </div>
    </div>

    <div class="panel-body">
      <div class="detail-grid">
        <div v-for="f in fields" :key="f.key" class="detail-row">
          <span class="detail-label">{{ f.label }}</span>
          <span class="detail-value">{{ (drone as any)[f.key] ?? '-' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail-panel {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 385px;
  max-height: 60%;
  background: linear-gradient(180deg, rgba(14, 69, 124, 0.97), rgba(7, 43, 87, 0.99));
  border-left: 1px solid #1d5390;
  border-top: 1px solid #1d5390;
  z-index: 6;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-bottom: 1px solid #1d5390;
  flex-shrink: 0;
}

.panel-title { font-size: 13px; font-weight: 600; color: #c6e0f9; }

.header-actions { display: flex; gap: 4px; }
.toggle-btn, .close-btn {
  background: transparent;
  border: none;
  color: #6c9bd0;
  font-size: 14px;
  cursor: pointer;
  padding: 2px 6px;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 8px 12px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px 16px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  border-bottom: 1px solid rgba(47, 122, 212, 0.12);
  font-size: 12px;
}
.detail-label { color: #6c9bd0; flex-shrink: 0; }
.detail-value { color: #eef6ff; font-weight: 500; text-align: right; }
</style>
