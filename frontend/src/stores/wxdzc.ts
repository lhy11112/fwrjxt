/**
 * wxdzc 侦测预警 Pinia Store — 完整版
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as wxdzcApi from '@/api/wxdzc'
import type { DeviceConfig, UavDetectMsgVo } from '@/types'

export interface DroneItem extends UavDetectMsgVo {
  trajectoryColor?: string
  trajectoryWidth?: number
}

export const useWxdzcStore = defineStore('wxdzc', () => {
  const devices = ref<DeviceConfig[]>([])
  const allDevices = ref<DeviceConfig[]>([])     // 所有设备（不分页）
  const deviceTotal = ref(0)
  const devicePage = ref(1)
  const deviceSearchName = ref('')

  const drones = ref<DroneItem[]>([])
  const dronesCopy = ref<any[]>([])
  const selectedSerial = ref<string | null>(null)

  const currentDate = ref(new Date().toISOString().slice(0, 10))
  const modelFilter = ref('')
  const authStatusFilter = ref<number | undefined>(undefined)

  const holidays = ref<string[]>([])

  const devicePanelVisible = ref(true)
  const dronePanelVisible = ref(true)
  const droneDetailPanelVisible = ref(true)

  const hideOthersMode = ref(false)
  const layerFilters = ref(['飞手', '无人机', '信息'])

  const zcNum = ref(0); const gjNum = ref(0); const slNum = ref(0)

  let pollTimer: any = null

  async function fetchDevices() {
    try {
      const r = await wxdzcApi.listDevices({ page: devicePage.value, page_size: 10, name: deviceSearchName.value || undefined })
      devices.value = r.records; deviceTotal.value = r.total
    } catch {}
  }

  async function fetchAllDevices() {
    try {
      const r = await wxdzcApi.listDevices({ page: 1, page_size: 500 })
      allDevices.value = r.records
    } catch {}
  }

  async function fetchDrones() {
    try {
      const list = await wxdzcApi.getByStation({ rq: currentDate.value, auth_status: authStatusFilter.value })
      drones.value = list as DroneItem[]
    } catch {}
  }

  async function fetchDateCalendar(year?: string, month?: string) {
    try {
      const r = await wxdzcApi.getDateCalendar({ nf: year || currentDate.value.slice(0, 4), yf: month || currentDate.value.slice(5, 7) })
      holidays.value = r.map(d => d.rq)
    } catch {}
  }

  function setDate(date: string) { currentDate.value = date; fetchDateCalendar() }
  function toggleHideOthers() { hideOthersMode.value = !hideOthersMode.value }
  function toggleDevicePanel() { devicePanelVisible.value = !devicePanelVisible.value }
  function toggleDronePanel() { dronePanelVisible.value = !dronePanelVisible.value }
  function toggleDroneDetailPanel() { droneDetailPanelVisible.value = !droneDetailPanelVisible.value }
  function selectDrone(s: string | null) { selectedSerial.value = s }

  function updateTrajectoryStyle(serial: string, color?: string, width?: number) {
    const drone = drones.value.find((d: any) => d.serial === serial) as any
    if (drone) { if (color) drone.color = color; if (width !== undefined) drone.gjWidth = width }
    const copy = dronesCopy.value.find((c: any) => c.serial === serial) as any
    if (copy) { if (color) copy.color = color; if (width !== undefined) copy.gjWidth = width }
  }

  return {
    devices, allDevices, deviceTotal, devicePage, deviceSearchName,
    drones, dronesCopy, selectedSerial,
    currentDate, modelFilter, authStatusFilter, holidays,
    devicePanelVisible, dronePanelVisible, droneDetailPanelVisible,
    hideOthersMode, layerFilters,
    zcNum, gjNum, slNum,
    fetchDevices, fetchAllDevices, fetchDrones, fetchDateCalendar,
    setDate, toggleHideOthers,
    toggleDevicePanel, toggleDronePanel, toggleDroneDetailPanel,
    selectDrone, updateTrajectoryStyle,
  }
})
