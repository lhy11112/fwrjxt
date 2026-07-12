import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { SensorMetadata, SensorStatus } from '@/types'

export const useSensorStore = defineStore('sensor', () => {
  const sensors = ref<SensorMetadata[]>([])

  const onlineSensors = computed(() =>
    sensors.value.filter(s => s.status === 'ONLINE')
  )

  const offlineSensors = computed(() =>
    sensors.value.filter(s => s.status === 'OFFLINE')
  )

  const radarSensors = computed(() =>
    sensors.value.filter(s => s.sensor_type === 'RADAR')
  )

  const rfSensors = computed(() =>
    sensors.value.filter(s => s.sensor_type === 'RF')
  )

  function setSensors(newSensors: SensorMetadata[]) {
    sensors.value = newSensors
  }

  function updateSensorStatus(id: string, status: SensorStatus) {
    const sensor = sensors.value.find(s => s.sensor_id === id)
    if (sensor) {
      sensor.status = status
    }
  }

  return {
    sensors,
    onlineSensors,
    offlineSensors,
    radarSensors,
    rfSensors,
    setSensors,
    updateSensorStatus,
  }
})
