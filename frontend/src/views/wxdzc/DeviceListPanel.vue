<script setup lang="ts">
/**
 * 设备列表面板 — 侦测预警左侧设备列表
 */
import type { DeviceConfig } from '@/types'

interface Props {
  devices: DeviceConfig[]
  total: number
  page: number
  visible: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  toggle: []
  'click-device': [device: DeviceConfig]
}>()

const deviceTypeLabels: Record<string, string> = {
  DETECT: '侦测',
  TRAP: '诱骗',
  DISTURB: '干扰',
}

const statusLabels: Record<string, { text: string; cls: string }> = {
  CONNECTED: { text: '已连接', cls: 'status-connected' },
  DISCONNECTED: { text: '未连接', cls: 'status-disconnected' },
  WARN: { text: '告警', cls: 'status-warn' },
}
</script>

<template>
  <div class="device-panel" :class="{ collapsed: !visible }">
    <div class="panel-header">
      <span class="panel-title">无线电设备</span>
      <button class="toggle-btn" @click="emit('toggle')">
        {{ visible ? '◀' : '▶' }}
      </button>
    </div>

    <div class="panel-body">
      <!-- Search -->
      <div class="search-bar">
        <input
          type="text"
          class="search-input"
          placeholder="搜索设备名称..."
        />
        <button class="search-btn">查询</button>
      </div>

      <!-- Device list -->
      <div class="device-list">
        <div
          v-for="d in devices"
          :key="d.id"
          class="device-item"
          @click="emit('click-device', d)"
        >
          <div class="device-icon" :class="d.status?.toLowerCase() || 'disconnected'">
            <span class="type-badge">{{ deviceTypeLabels[d.device_type] || d.device_type }}</span>
          </div>
          <div class="device-info">
            <div class="device-name">{{ d.name || d.device_id }}</div>
            <div class="device-meta">
              <span>{{ d.device_id }}</span>
              <span :class="statusLabels[d.status || 'DISCONNECTED']?.cls">
                {{ statusLabels[d.status || 'DISCONNECTED']?.text || d.status }}
              </span>
            </div>
          </div>
        </div>
        <div v-if="!devices.length" class="empty-hint">暂无设备数据</div>
      </div>

      <!-- Pagination -->
      <div class="pagination">
        <span>共 {{ total }} 条</span>
        <div class="pager">
          <button :disabled="page <= 1">上一页</button>
          <span class="page-num">{{ page }}</span>
          <button :disabled="page * 10 >= total">下一页</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.device-panel {
  width: 100%;
  height: 100%;
  background: linear-gradient(180deg, rgba(14, 69, 124, 0.95), rgba(7, 43, 87, 0.98));
  border-right: 1px solid #1d5390;
  display: flex;
  flex-direction: column;
}
.device-panel.collapsed { width: 0; overflow: hidden; }

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

.toggle-btn {
  background: transparent;
  border: none;
  color: #6c9bd0;
  font-size: 12px;
  cursor: pointer;
  padding: 2px 8px;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.search-bar {
  display: flex;
  gap: 6px;
  padding: 10px;
  border-bottom: 1px solid #1d5390;
}

.search-input {
  flex: 1;
  padding: 5px 10px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid #2f7ad4;
  border-radius: 4px;
  color: #eef6ff;
  font-size: 12px;
  outline: none;
}

.search-btn {
  padding: 5px 12px;
  background: #028be5;
  border: none;
  border-radius: 4px;
  color: #fff;
  font-size: 12px;
  cursor: pointer;
}

.device-list {
  flex: 1;
  overflow-y: auto;
}

.device-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-bottom: 1px solid rgba(47, 122, 212, 0.15);
  cursor: pointer;
  transition: background 0.15s;
}
.device-item:hover { background: rgba(47, 122, 212, 0.12); }

.device-icon {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 10px;
  font-weight: 600;
}
.device-icon.connected { background: rgba(111, 214, 111, 0.2); border: 1px solid #6fd66f; }
.device-icon.disconnected { background: rgba(255, 122, 134, 0.15); border: 1px solid #ff7a86; }
.device-icon.warn { background: rgba(255, 213, 97, 0.15); border: 1px solid #ffd561; }

.type-badge { color: #97c0eb; font-size: 10px; }

.device-info { flex: 1; min-width: 0; }
.device-name { font-size: 13px; font-weight: 600; color: #eef6ff; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.device-meta { display: flex; gap: 8px; font-size: 11px; color: #6c9bd0; margin-top: 2px; }

.status-connected { color: #6fd66f; }
.status-disconnected { color: #ff7a86; }
.status-warn { color: #ffd561; }

.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  font-size: 11px;
  color: #6c9bd0;
  border-top: 1px solid #1d5390;
  flex-shrink: 0;
}

.pager {
  display: flex;
  align-items: center;
  gap: 6px;
}
.pager button {
  padding: 2px 8px;
  background: rgba(47, 122, 212, 0.15);
  border: 1px solid #2f7ad4;
  border-radius: 3px;
  color: #97c0eb;
  font-size: 11px;
  cursor: pointer;
}
.pager button:disabled { opacity: 0.4; cursor: default; }

.page-num { color: #4db4ff; font-weight: 600; }

.empty-hint {
  padding: 40px 20px;
  text-align: center;
  color: #5f7fa0;
  font-size: 13px;
}
</style>
