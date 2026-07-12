<script setup lang="ts">
/**
 * 无人机列表面板 — 侦测预警右侧无人机列表
 */
import { ref } from 'vue'
import { useWxdzcStore } from '@/stores/wxdzc'
import type { DroneItem } from '@/stores/wxdzc'

interface Props {
  drones: DroneItem[]
  zcNum: number
  gjNum: number
  slNum: number
  selectedSerial: string | null
  visible: boolean
  hideOthers: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  toggle: []
  'click-drone': [drone: DroneItem]
  'context-drone': [event: MouseEvent, drone: DroneItem]
  'toggle-hide-others': []
}>()

const store = useWxdzcStore()
const showDatePicker = ref(false)
const showAuthFilter = ref(false)

const authStatusOptions = [
  { value: undefined, label: '全部' },
  { value: 4, label: '待干扰' },
  { value: 5, label: '待诱骗' },
  { value: 6, label: '持续跟踪' },
]

const statusLabels: Record<number, { text: string; cls: string }> = {
  1: { text: '正常', cls: 's-normal' },
  2: { text: '告警', cls: 's-warn' },
  3: { text: '失联', cls: 's-lost' },
}

const threatLabels: Record<string, { text: string; cls: string }> = {
  red: { text: '高危', cls: 't-red' },
  yellow: { text: '中危', cls: 't-yellow' },
  green: { text: '低危', cls: 't-green' },
}

function onDateChange(e: Event) {
  const input = e.target as HTMLInputElement
  if (input.value) store.setDate(input.value)
  showDatePicker.value = false
}

function onAuthFilterChange(val: number | undefined) {
  store.authStatusFilter = val
  showAuthFilter.value = false
}

function onModelInput(e: Event) {
  store.modelFilter = (e.target as HTMLInputElement).value
}

function onColorChange(serial: string, color: string) {
  store.updateTrajectoryStyle(serial, color)
}

function onWidthChange(serial: string, w: number) {
  store.updateTrajectoryStyle(serial, undefined, w)
}
</script>

<template>
  <div class="drone-panel" :class="{ collapsed: !visible }">
    <div class="panel-header">
      <span class="panel-title">
        侦测无人机信息
        <span class="drone-count">({{ drones.length }})</span>
      </span>
      <button class="toggle-btn" @click="emit('toggle')">
        {{ visible ? '▶' : '◀' }}
      </button>
    </div>

    <div class="panel-body">
      <!-- Filter bar -->
      <div class="filter-bar">
        <div class="filter-row">
          <button class="filter-btn" @click="showDatePicker = !showDatePicker">
            {{ store.currentDate }}
          </button>
          <input
            type="date"
            v-if="showDatePicker"
            :value="store.currentDate"
            @change="onDateChange"
            class="date-input"
          />
          <input
            type="text"
            class="model-input"
            placeholder="型号筛选..."
            @input="onModelInput"
          />
          <div class="auth-filter">
            <button class="filter-btn" @click="showAuthFilter = !showAuthFilter">
              {{ authStatusOptions.find(o => o.value === store.authStatusFilter)?.label || '全部' }}
            </button>
            <div v-if="showAuthFilter" class="auth-dropdown">
              <div
                v-for="opt in authStatusOptions"
                :key="String(opt.value)"
                class="auth-option"
                :class="{ active: store.authStatusFilter === opt.value }"
                @click="onAuthFilterChange(opt.value)"
              >{{ opt.label }}</div>
            </div>
          </div>
          <button
            class="hide-others-btn"
            :class="{ active: hideOthers }"
            @click="emit('toggle-hide-others')"
          >隐藏其他</button>
        </div>

        <!-- Layer filter checkboxes -->
        <div class="layer-checks">
          <label v-for="l in ['飞手', '无人机', '信息']" :key="l" class="check-label">
            <input type="checkbox" :value="l" v-model="store.layerFilters" />
            {{ l }}
          </label>
        </div>

        <!-- Status counts -->
        <div class="status-counts">
          <span class="count-item s-normal">正常 {{ zcNum }}</span>
          <span class="count-item s-warn">告警 {{ gjNum }}</span>
          <span class="count-item s-lost">失联 {{ slNum }}</span>
        </div>
      </div>

      <!-- Drone list -->
      <div class="drone-list">
        <div
          v-for="d in drones"
          :key="d.serial || d.id"
          class="drone-item"
          :class="{ selected: selectedSerial === d.serial }"
          @click="emit('click-drone', d)"
          @contextmenu="emit('context-drone', $event, d)"
        >
          <!-- Threat level indicator -->
          <div class="threat-dot" :class="threatLabels[d.wxdj || 'green']?.cls">
            <span class="threat-text">{{ threatLabels[d.wxdj || 'green']?.text }}</span>
          </div>

          <div class="drone-info">
            <div class="drone-head">
              <span class="drone-model">{{ d.model || '未知' }}</span>
              <span class="drone-station">{{ d.station_name || '' }}</span>
              <span :class="statusLabels[d.status || 1]?.cls" class="drone-status">
                {{ statusLabels[d.status || 1]?.text }}
              </span>
            </div>
            <div class="drone-telemetry">
              <span>{{ d.freq ? (d.freq / 1000).toFixed(0) + 'MHz' : '-' }}</span>
              <span>{{ d.altitude ? d.altitude.toFixed(0) + 'm' : '-' }}</span>
              <span>{{ d.sd ? d.sd.toFixed(1) + 'm/s' : '-' }}</span>
              <span>{{ d.distance ? (d.distance / 1000).toFixed(2) + 'km' : '-' }}</span>
              <span>{{ d.angle ? d.angle.toFixed(0) + '°' : '-' }}</span>
            </div>
            <div class="drone-time">{{ d.data_time || '' }}</div>
          </div>

          <!-- Trajectory controls -->
          <div class="drone-controls">
            <input
              type="color"
              :value="d.trajectoryColor || '#4db4ff'"
              class="color-picker"
              title="轨迹颜色"
              @input="(e: any) => onColorChange(d.serial!, e.target.value)"
            />
            <input
              type="number"
              :value="d.trajectoryWidth || 3"
              min="1"
              max="10"
              class="width-input"
              title="轨迹宽度"
              @change="(e: any) => onWidthChange(d.serial!, Number(e.target.value))"
            />
          </div>
        </div>
        <div v-if="!drones.length" class="empty-hint">等待侦测数据...</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.drone-panel {
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, rgba(14, 69, 124, 0.95), rgba(7, 43, 87, 0.98));
  border-left: 1px solid #1d5390;
  display: flex;
  flex-direction: column;
}
.drone-panel.collapsed { width: 0; overflow: hidden; }

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #1d5390;
  flex-shrink: 0;
}

.panel-title {
  font-size: 15px;
  font-weight: 700;
  color: #c6e0f9;
}
.drone-count { font-size: 12px; color: #6c9bd0; margin-left: 4px; }

.toggle-btn {
  background: transparent;
  border: none;
  color: #6c9bd0;
  font-size: 12px;
  cursor: pointer;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.filter-bar {
  padding: 10px;
  border-bottom: 1px solid #1d5390;
  flex-shrink: 0;
}

.filter-row {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  align-items: center;
}

.filter-btn {
  padding: 4px 10px;
  background: rgba(47, 122, 212, 0.15);
  border: 1px solid #2f7ad4;
  border-radius: 4px;
  color: #97c0eb;
  font-size: 12px;
  cursor: pointer;
}

.model-input {
  flex: 1;
  min-width: 80px;
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid #2f7ad4;
  border-radius: 4px;
  color: #eef6ff;
  font-size: 12px;
  outline: none;
}

.date-input {
  position: absolute;
  z-index: 2;
  margin-top: 4px;
  padding: 4px;
  background: #0e457c;
  border: 1px solid #2f7ad4;
  border-radius: 4px;
  color: #eef6ff;
}

.auth-filter { position: relative; }
.auth-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  z-index: 10;
  margin-top: 4px;
  background: #0e457c;
  border: 1px solid #2f7ad4;
  border-radius: 4px;
  overflow: hidden;
}
.auth-option {
  padding: 6px 14px;
  font-size: 12px;
  color: #97c0eb;
  cursor: pointer;
  white-space: nowrap;
}
.auth-option:hover, .auth-option.active { background: rgba(2, 139, 229, 0.3); color: #4db4ff; }

.hide-others-btn {
  padding: 4px 10px;
  background: transparent;
  border: 1px solid #2f7ad4;
  border-radius: 4px;
  color: #97c0eb;
  font-size: 12px;
  cursor: pointer;
}
.hide-others-btn.active { background: rgba(2, 139, 229, 0.3); color: #4db4ff; }

.layer-checks {
  display: flex;
  gap: 12px;
  margin-top: 6px;
  font-size: 11px;
  color: #6c9bd0;
}
.check-label { display: flex; align-items: center; gap: 4px; cursor: pointer; }

.status-counts {
  display: flex;
  gap: 12px;
  margin-top: 4px;
  font-size: 11px;
}
.count-item { font-weight: 600; }
.s-normal { color: #6fd66f; }
.s-warn { color: #ffd561; }
.s-lost { color: #ff7a86; }

.drone-list { flex: 1; overflow-y: auto; }

.drone-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  border-bottom: 1px solid rgba(47, 122, 212, 0.12);
  cursor: pointer;
  transition: background 0.15s;
}
.drone-item:hover { background: rgba(47, 122, 212, 0.1); }
.drone-item.selected { background: rgba(2, 139, 229, 0.2); border-left: 2px solid #4db4ff; }

.threat-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 4px;
  position: relative;
}
.threat-dot .threat-text { position: absolute; left: 14px; top: -2px; font-size: 9px; white-space: nowrap; display: none; }
.threat-dot:hover .threat-text { display: block; }
.t-red { background: #ff7a86; box-shadow: 0 0 8px #ff7a86; }
.t-yellow { background: #ffd561; box-shadow: 0 0 6px #ffd561; }
.t-green { background: #6fd66f; box-shadow: 0 0 5px #6fd66f; }

.drone-info { flex: 1; min-width: 0; }
.drone-head { display: flex; gap: 6px; align-items: center; }
.drone-model { font-size: 13px; font-weight: 600; color: #eef6ff; }
.drone-station { font-size: 11px; color: #5f7fa0; }
.drone-status { font-size: 11px; font-weight: 600; }
.s-normal { color: #6fd66f; }
.s-warn { color: #ffd561; }
.s-lost { color: #ff7a86; }

.drone-telemetry {
  display: flex;
  gap: 6px;
  margin-top: 3px;
  font-size: 11px;
  color: #6c9bd0;
  flex-wrap: wrap;
}

.drone-time { font-size: 10px; color: #4a6a8a; margin-top: 2px; }

.drone-controls {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex-shrink: 0;
}
.color-picker {
  width: 20px;
  height: 20px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  padding: 0;
  background: transparent;
}
.width-input {
  width: 36px;
  padding: 1px 4px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid #2f7ad4;
  border-radius: 3px;
  color: #eef6ff;
  font-size: 11px;
  text-align: center;
}

.empty-hint {
  padding: 40px 20px;
  text-align: center;
  color: #5f7fa0;
  font-size: 13px;
}
</style>
