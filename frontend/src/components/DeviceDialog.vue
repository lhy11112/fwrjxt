<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useDeviceStore, DEVICE_TYPE_META } from '@/stores/device'
import { listAllDevices } from '@/api'
import DeviceConfigDialog from './DeviceConfigDialog.vue'
import type { DeviceInfo } from '@/types'

const device = useDeviceStore()

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{ close: [] }>()

const loading = ref(true)
const activeTab = ref<'all' | 'hsimc2_sensor' | 'cuas_device'>('all')
const configDevice = ref<DeviceInfo | null>(null)
const showConfig = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const list = await listAllDevices()
    device.setDevices(list)
  } catch (e) {
    console.warn('设备加载失败:', e)
  }
  loading.value = false
})

watch(activeTab, (tab) => {
  device.setFilterGroup(tab === 'all' ? null : tab)
})

const filteredList = computed(() => device.getFilteredDevices())

function getTypeMeta(kind: string) {
  return DEVICE_TYPE_META[kind] || { label: kind, icon: '📟', group: 'unknown' }
}

/** 设备类型 → 线性图标名 */
const DEVICE_ICON: Record<string, string> = {
  RADAR: 'radar', RF: 'signal', EO_IR: 'camera', ACOUSTIC: 'mic', LIDAR: 'bulb',
  CUAS_RADAR: 'satellite', CUAS_RADIO_FREQ: 'signal', CUAS_OPTICAL: 'scope',
  CUAS_INFRARED: 'thermal', CUAS_JAMMER: 'bolt', CUAS_SPOOFER: 'refresh', CUAS_CONTROL_BOX: 'box',
}
function deviceIcon(kind: string) { return DEVICE_ICON[kind] || 'device' }

/** 打开配置对话框 */
function openConfig(d: DeviceInfo, event: MouseEvent) {
  event.stopPropagation()
  configDevice.value = d
  showConfig.value = true
}

/** 测试处理 */
function handleTest(deviceId: string, cmd: string, params: Record<string, any>) {
  console.log('[设备测试]', deviceId, cmd, params)
}

/** 快速测试：模拟心跳查询 */
function quickTest(d: DeviceInfo, event: MouseEvent) {
  event.stopPropagation()
  configDevice.value = d
  showConfig.value = true
}
</script>

<template>
  <Teleport to="body">
    <Transition name="device-fade">
      <div v-if="visible" class="device-overlay" @click.self="emit('close')">
        <div class="device-dialog">
          <!-- 标题栏 -->
          <div class="dd-header">
            <div class="dd-title">
              <span class="dd-icon"><Icon name="device" :size="18" /></span>
              <span>设备管理</span>
              <span class="dd-count" v-if="device.stats.total">{{ device.stats.total }}台</span>
            </div>
            <div class="dd-actions">
              <span class="dd-stat">在线 <strong class="online">{{ device.stats.online }}</strong></span>
              <span class="dd-stat-sep">|</span>
              <span class="dd-stat">离线 <strong class="offline">{{ device.stats.offline }}</strong></span>
              <button class="dd-close" @click="emit('close')" title="关闭">✕</button>
            </div>
          </div>

          <!-- 搜索 + 分类标签 -->
          <div class="dd-toolbar">
            <div class="dd-search">
              <span class="search-icon"><Icon name="search" :size="14" /></span>
              <input
                v-model="device.searchText"
                class="search-input"
                placeholder="搜索设备名称/型号/能力..."
              />
            </div>
            <div class="dd-tabs">
              <button class="tab-btn" :class="{ active: activeTab === 'all' }" @click="activeTab = 'all'">
                全部<span class="tab-badge">{{ device.stats.total }}</span>
              </button>
              <button class="tab-btn" :class="{ active: activeTab === 'hsimc2_sensor' }" @click="activeTab = 'hsimc2_sensor'">
                <Icon name="satellite" :size="13" /> HSimC2<span class="tab-badge" v-if="device.hsimc2Sensors.length">{{ device.hsimc2Sensors.length }}</span>
              </button>
              <button class="tab-btn" :class="{ active: activeTab === 'cuas_device' }" @click="activeTab = 'cuas_device'">
                <Icon name="shield" :size="13" /> 反无系统<span class="tab-badge" v-if="device.cuasDevices.length">{{ device.cuasDevices.length }}</span>
              </button>
            </div>
          </div>

          <!-- 设备列表 -->
          <div class="dd-body">
            <div v-if="loading" class="dd-loading">
              <div class="loader-spin-sm"></div>
              <span>加载设备列表...</span>
            </div>

            <template v-else-if="filteredList.length === 0">
              <div class="dd-empty">
                <span class="empty-icon"><Icon name="inbox" :size="30" /></span>
                <span>暂无匹配的设备</span>
              </div>
            </template>

            <template v-else>
              <div
                v-for="d in filteredList"
                :key="d.device_id"
                class="device-card"
                :class="{ selected: device.selectedDeviceId === d.device_id, offline: !d.online }"
                @click="device.selectDevice(d.device_id)"
              >
                <div class="dc-icon-wrap">
                  <span class="dc-icon"><Icon :name="deviceIcon(d.device_kind)" :size="18" /></span>
                </div>

                <div class="dc-info">
                  <div class="dc-head">
                    <span class="dc-name">{{ d.name }}</span>
                    <span class="dc-model">{{ d.model }}</span>
                    <span class="dc-source-tag" :class="d.source">
                      {{ d.source === 'cuas_system' ? '反无' : '传感' }}
                    </span>
                  </div>
                  <div class="dc-meta">
                    <span class="dc-type">{{ getTypeMeta(d.device_kind).label }}</span>
                    <span class="dc-dot">·</span>
                    <span class="dc-pos">{{ d.latitude.toFixed(4) }}, {{ d.longitude.toFixed(4) }}</span>
                  </div>
                  <div class="dc-caps" v-if="d.capabilities.length">
                    <span v-for="cap in d.capabilities.slice(0, 4)" :key="cap" class="cap-tag">{{ cap }}</span>
                    <span v-if="d.capabilities.length > 4" class="cap-tag more">+{{ d.capabilities.length - 4 }}</span>
                  </div>
                  <div class="dc-cuas" v-if="d.source === 'cuas_system' && d.cuas_ip">
                    <span class="cuas-ip">IP: {{ d.cuas_ip }}:{{ d.cuas_port }}</span>
                    <span v-if="d.cuas_ptz" class="cuas-ptz">· 云台</span>
                    <span v-if="d.cuas_rtsp_url" class="cuas-rtsp">· RTSP</span>
                  </div>
                </div>

                <div class="dc-status">
                  <span class="status-led" :class="d.online ? 'on' : 'off'"></span>
                  <span class="status-text" :class="d.online ? 'on' : 'off'">{{ d.status_label }}</span>
                </div>

                <!-- 操作按钮 -->
                <div class="dc-actions">
                  <button class="dc-act-btn cfg" @click="openConfig(d, $event)" title="参数配置"><Icon name="settings" :size="12" />配置</button>
                  <button class="dc-act-btn test" @click="quickTest(d, $event)" title="设备测试"><Icon name="flask" :size="12" />测试</button>
                </div>
              </div>
            </template>
          </div>

          <!-- 底部 -->
          <div class="dd-footer">
            <span class="footer-hint"><Icon name="bulb" :size="13" />点击"配置"打开参数面板 · "测试"查看实时状态</span>
            <button class="footer-btn" @click="emit('close')">关闭</button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 设备配置对话框 -->
    <DeviceConfigDialog
      :visible="showConfig"
      :device="configDevice"
      @close="showConfig = false"
      @test="handleTest"
    />
  </Teleport>
</template>

<style scoped>
/* ---- 遮罩层 ---- */
.device-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.4);
  backdrop-filter: blur(8px) saturate(1.1);
  -webkit-backdrop-filter: blur(8px) saturate(1.1);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.device-dialog {
  width: min(840px, 92vw);
  max-height: 85vh;
  background: var(--bg-surface-3);
  border: 1px solid var(--stroke-divider);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-64);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ---- 标题 ---- */
.dd-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--sp-l) var(--sp-xl);
  border-bottom: 1px solid var(--divider);
  background: var(--bg-surface-2);
  flex-shrink: 0;
}
.dd-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 700; color: var(--fg-1); }
.dd-icon { font-size: 20px; }
.dd-count { padding: 1px 10px; background: var(--brand-subtle); border-radius: var(--radius-circular); font-size: 12px; font-weight: 600; color: var(--brand-fg); }
.dd-actions { display: flex; align-items: center; gap: 10px; font-size: 12px; color: var(--fg-3); }
.dd-stat strong.online { color: var(--success); }
.dd-stat strong.offline { color: var(--danger); }
.dd-stat-sep { color: var(--stroke-control); }
.dd-close {
  width: 28px; height: 28px; display: flex; align-items: center; justify-content: center;
  border: 1px solid transparent; border-radius: var(--radius-md);
  background: transparent; color: var(--fg-3); font-size: 14px; cursor: pointer;
  transition: background var(--dur-normal) var(--ease-fluent);
}
.dd-close:hover { background: var(--danger-bg); color: var(--danger); }

/* ---- 工具栏 ---- */
.dd-toolbar {
  display: flex; align-items: center; gap: var(--sp-m);
  padding: var(--sp-m) var(--sp-xl); border-bottom: 1px solid var(--stroke-divider);
  background: var(--bg-surface); flex-shrink: 0;
}
.dd-search { display: flex; align-items: center; gap: 6px; flex: 1; max-width: 220px; padding: 0 var(--sp-m); height: 32px; background: var(--bg-surface); border: 1px solid var(--stroke-control); border-bottom-color: var(--fg-4); border-radius: var(--radius-md); transition: border-color var(--dur-normal) var(--ease-fluent); }
.dd-search:focus-within { border-color: var(--stroke-control); border-bottom: 2px solid var(--brand-fg); }
.search-icon { font-size: 13px; color: var(--fg-3); }
.search-input { flex: 1; border: none; background: transparent; color: var(--fg-1); font-size: 13px; outline: none; font-family: var(--font-base); }
.search-input::placeholder { color: var(--fg-4); }
.dd-tabs { display: flex; gap: var(--sp-xs); }
.tab-btn { display: flex; align-items: center; gap: 4px; padding: 6px var(--sp-m); border: 1px solid transparent; border-radius: var(--radius-md); background: transparent; color: var(--fg-3); font-size: 13px; font-weight: 600; cursor: pointer; transition: background var(--dur-normal) var(--ease-fluent), color var(--dur-normal) var(--ease-fluent); white-space: nowrap; }
.tab-btn:hover { background: var(--bg-subtle-hover); color: var(--fg-1); }
.tab-btn.active { background: var(--brand-selected); color: var(--brand-fg); }
.tab-badge { padding: 0 6px; background: var(--brand-subtle); border-radius: var(--radius-circular); font-size: 10px; color: var(--brand-fg); }

/* ---- 列表区 ---- */
.dd-body { flex: 1; overflow-y: auto; padding: var(--sp-s) var(--sp-l); min-height: 200px; }
.dd-body::-webkit-scrollbar { width: 8px; }
.dd-body::-webkit-scrollbar-thumb { background: var(--stroke-control); border-radius: var(--radius-circular); border: 2px solid transparent; background-clip: padding-box; }

.dd-loading { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 60px 0; color: var(--fg-3); font-size: 13px; }
.loader-spin-sm { width: 24px; height: 24px; border: 2px solid var(--stroke-divider); border-top-color: var(--brand-fg); border-radius: 50%; animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.dd-empty { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 60px 0; color: var(--fg-4); }
.empty-icon { font-size: 32px; }

/* ---- 设备卡片 ---- */
.device-card {
  display: flex; align-items: flex-start; gap: 10px;
  padding: var(--sp-m); margin-bottom: var(--sp-xs);
  background: var(--bg-surface-2); border: 1px solid var(--stroke-divider);
  border-left: 2px solid transparent;
  border-radius: var(--radius-lg); cursor: pointer; transition: background var(--dur-normal) var(--ease-fluent), border-color var(--dur-normal) var(--ease-fluent);
}
.device-card:hover { background: var(--bg-subtle-hover); border-color: var(--stroke-control); }
.device-card.selected { background: var(--brand-selected); border-color: var(--stroke-divider); border-left-color: var(--brand-fg); }
.device-card.offline { opacity: 0.6; }

.dc-icon-wrap { width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; background: var(--brand-subtle); border-radius: var(--radius-md); flex-shrink: 0; }
.dc-icon { font-size: 18px; }

.dc-info { flex: 1; min-width: 0; }
.dc-head { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.dc-name { font-size: 13px; font-weight: 600; color: var(--fg-1); }
.dc-model { font-size: 10px; color: var(--fg-3); }
.dc-source-tag { padding: 1px 6px; border-radius: var(--radius-sm); font-size: 9px; font-weight: 600; }
.dc-source-tag.hsimc2_sensor { background: var(--brand-subtle); color: var(--brand-fg); }
.dc-source-tag.cuas_system { background: var(--warning-bg); color: var(--warning); }

.dc-meta { display: flex; align-items: center; gap: 4px; margin-top: 2px; font-size: 11px; color: var(--fg-3); }
.dc-dot { color: var(--stroke-control); }
.dc-caps { display: flex; flex-wrap: wrap; gap: 3px; margin-top: 3px; }
.cap-tag {
  padding: 0 6px; background: var(--bg-surface-3);
  border: 1px solid var(--stroke-divider); border-radius: var(--radius-sm);
  font-size: 10px; color: var(--fg-2); line-height: 18px;
}
.cap-tag.more { background: transparent; color: var(--fg-4); }
.dc-cuas { display: flex; gap: 6px; margin-top: 3px; font-size: 10px; color: var(--fg-4); }
.cuas-ip { color: var(--fg-3); font-family: var(--font-mono); }

.dc-status {
  display: flex; flex-direction: column; align-items: center; gap: 4px;
  flex-shrink: 0; min-width: 36px;
}
.status-led { width: 8px; height: 8px; border-radius: var(--radius-circular); }
.status-led.on { background: var(--success); box-shadow: 0 0 0 3px var(--success-bg); }
.status-led.off { background: var(--fg-4); }
.status-text { font-size: 10px; }
.status-text.on { color: var(--success); }
.status-text.off { color: var(--fg-4); }

/* ---- 操作按钮 ---- */
.dc-actions {
  display: flex; flex-direction: column; gap: var(--sp-xs); flex-shrink: 0;
}
.dc-act-btn {
  display: inline-flex; align-items: center; justify-content: center; gap: 4px;
  padding: 4px var(--sp-s); border-radius: var(--radius-md); font-size: 10px; font-weight: 600;
  cursor: pointer; border: 1px solid transparent; transition: background var(--dur-normal) var(--ease-fluent);
  white-space: nowrap;
}
.dc-act-btn.cfg {
  background: var(--brand-subtle); color: var(--brand-fg);
}
.dc-act-btn.cfg:hover { background: var(--brand-selected); }
.dc-act-btn.test {
  background: var(--warning-bg); color: var(--warning);
}
.dc-act-btn.test:hover { background: var(--warning-bg); filter: brightness(1.2); }

/* ---- 底部 ---- */
.dd-footer {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--sp-m) var(--sp-xl); border-top: 1px solid var(--divider);
  background: var(--bg-surface-2); flex-shrink: 0;
}
.footer-hint { display: inline-flex; align-items: center; gap: 5px; font-size: 11px; color: var(--fg-4); }
.footer-btn {
  height: 32px; padding: 0 var(--sp-l); border: 1px solid var(--stroke-control);
  border-radius: var(--radius-md); background: var(--bg-surface-3); color: var(--fg-1);
  font-size: 14px; font-weight: 600; cursor: pointer;
  transition: background var(--dur-normal) var(--ease-fluent);
}
.footer-btn:hover { background: var(--bg-subtle-hover); }

/* ---- 过渡动画 ---- */
.device-fade-enter-active, .device-fade-leave-active { transition: opacity var(--dur-slow) var(--ease-fluent); }
.device-fade-enter-from, .device-fade-leave-to { opacity: 0; }
.device-fade-enter-active .device-dialog { transition: transform var(--dur-slow) var(--ease-decel); }
.device-fade-enter-from .device-dialog { transform: scale(0.98) translateY(8px); }
</style>
