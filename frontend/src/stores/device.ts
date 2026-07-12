import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { DeviceInfo, UnifiedDeviceType, DeviceSource } from '@/types'

/** 设备类型 → 显示名、图标、分组 */
export const DEVICE_TYPE_META: Record<string, { label: string; icon: string; group: string }> = {
  // HSimC2 传感器
  RADAR:      { label: '相控阵雷达',     icon: '📡', group: 'hsimc2_sensor' },
  RF:         { label: '频谱监测',       icon: '📻', group: 'hsimc2_sensor' },
  EO_IR:      { label: '光电/红外',      icon: '📷', group: 'hsimc2_sensor' },
  ACOUSTIC:   { label: '声学阵列',       icon: '🎤', group: 'hsimc2_sensor' },
  LIDAR:      { label: '激光雷达',       icon: '💡', group: 'hsimc2_sensor' },
  // 反无设备
  CUAS_RADAR:     { label: '主动雷达探测',  icon: '🛰️', group: 'cuas_device' },
  CUAS_RADIO_FREQ:{ label: '无线电侦测',   icon: '📻', group: 'cuas_device' },
  CUAS_OPTICAL:   { label: '光电跟踪',     icon: '🔭', group: 'cuas_device' },
  CUAS_INFRARED:  { label: '红外设备',     icon: '🌡️', group: 'cuas_device' },
  CUAS_JAMMER:    { label: '干扰设备',     icon: '⚡', group: 'cuas_device' },
  CUAS_SPOOFER:   { label: '诱骗设备',     icon: '🔄', group: 'cuas_device' },
  CUAS_CONTROL_BOX:{ label: '智能控制箱',   icon: '📦', group: 'cuas_device' },
}

export const useDeviceStore = defineStore('device', () => {
  const devices = ref<DeviceInfo[]>([])
  const selectedDeviceId = ref<string | null>(null)
  const filterGroup = ref<string | null>(null)
  const filterOnline = ref<boolean | null>(null)
  const searchText = ref('')

  // ---- 计算属性 ----

  const onlineDevices = computed(() => devices.value.filter(d => d.online))
  const offlineDevices = computed(() => devices.value.filter(d => !d.online))

  /** 按来源分组统计 */
  const hsimc2Sensors = computed(() =>
    devices.value.filter(d => d.source === 'hsimc2_sensor')
  )
  const cuasDevices = computed(() =>
    devices.value.filter(d => d.source === 'cuas_system')
  )

  /** 按设备类型分组 */
  const groupedByType = computed(() => {
    const groups: Record<string, DeviceInfo[]> = {}
    for (const d of devices.value) {
      const key = d.device_kind
      if (!groups[key]) groups[key] = []
      groups[key].push(d)
    }
    return groups
  })

  /** 分组列表（用于对话框展示） */
  const deviceGroups = computed(() => [
    {
      key: 'hsimc2_sensor',
      label: 'HSimC2 传感器',
      icon: '🛰️',
      items: hsimc2Sensors.value,
    },
    {
      key: 'cuas_device',
      label: '反无人机系统设备',
      icon: '🛡️',
      items: cuasDevices.value,
    },
  ])

  const selectedDevice = computed(() =>
    devices.value.find(d => d.device_id === selectedDeviceId.value) ?? null
  )

  const stats = computed(() => {
    const by_type: Record<string, number> = {}
    for (const d of devices.value) {
      const key = d.device_kind
      by_type[key] = (by_type[key] || 0) + 1
    }
    return {
      total: devices.value.length,
      online: onlineDevices.value.length,
      offline: offlineDevices.value.length,
      by_type,
    }
  })

  // ---- 动作 ----

  function setDevices(list: DeviceInfo[]) {
    devices.value = list
  }

  function addDevice(device: DeviceInfo) {
    const idx = devices.value.findIndex(d => d.device_id === device.device_id)
    if (idx >= 0) {
      devices.value[idx] = device
    } else {
      devices.value.push(device)
    }
  }

  function removeDevice(id: string) {
    devices.value = devices.value.filter(d => d.device_id !== id)
  }

  function updateDeviceStatus(id: string, online: boolean, status_label?: string) {
    const d = devices.value.find(x => x.device_id === id)
    if (d) {
      d.online = online
      if (status_label !== undefined) d.status_label = status_label
    }
  }

  function selectDevice(id: string | null) {
    selectedDeviceId.value = id
  }

  function setFilterGroup(group: string | null) {
    filterGroup.value = group
  }

  function setFilterOnline(val: boolean | null) {
    filterOnline.value = val
  }

  function setSearchText(text: string) {
    searchText.value = text
  }

  /** 获取筛选后的设备列表 */
  function getFilteredDevices(): DeviceInfo[] {
    let list = devices.value
    if (filterGroup.value === 'hsimc2_sensor') {
      list = list.filter(d => d.source === 'hsimc2_sensor')
    } else if (filterGroup.value === 'cuas_device') {
      list = list.filter(d => d.source === 'cuas_system')
    }
    if (filterOnline.value === true) list = list.filter(d => d.online)
    if (filterOnline.value === false) list = list.filter(d => !d.online)
    if (searchText.value) {
      const q = searchText.value.toLowerCase()
      list = list.filter(d =>
        d.name.toLowerCase().includes(q) ||
        d.model.toLowerCase().includes(q) ||
        d.capabilities.some(c => c.toLowerCase().includes(q))
      )
    }
    return list
  }

  return {
    devices,
    selectedDeviceId,
    filterGroup,
    filterOnline,
    searchText,
    onlineDevices,
    offlineDevices,
    hsimc2Sensors,
    cuasDevices,
    groupedByType,
    deviceGroups,
    selectedDevice,
    stats,
    setDevices,
    addDevice,
    removeDevice,
    updateDeviceStatus,
    selectDevice,
    setFilterGroup,
    setFilterOnline,
    setSearchText,
    getFilteredDevices,
  }
})
