import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { InterceptMission, MissionStatus } from '@/types'

export const useMissionStore = defineStore('mission', () => {
  const missions = ref<InterceptMission[]>([])
  const selectedMissionId = ref<string | null>(null)

  const activeMissions = computed(() =>
    missions.value.filter(m =>
      m.status === 'ACTIVE' || m.status === 'EXECUTING'
    )
  )

  const completedMissions = computed(() =>
    missions.value.filter(m => m.status === 'COMPLETED')
  )

  const plannedMissions = computed(() =>
    missions.value.filter(m => m.status === 'PLANNING')
  )

  const selectedMission = computed(() =>
    missions.value.find(m => m.mission_id === selectedMissionId.value) ?? null
  )

  function setMissions(newMissions: InterceptMission[]) {
    missions.value = newMissions
  }

  function addMission(mission: InterceptMission) {
    const idx = missions.value.findIndex(m => m.mission_id === mission.mission_id)
    if (idx >= 0) {
      missions.value[idx] = mission
    } else {
      missions.value.push(mission)
    }
  }

  function updateMissionStatus(missionId: string, status: MissionStatus) {
    const mission = missions.value.find(m => m.mission_id === missionId)
    if (mission) {
      mission.status = status
    }
  }

  function selectMission(id: string | null) {
    selectedMissionId.value = id
  }

  return {
    missions,
    selectedMissionId,
    activeMissions,
    completedMissions,
    plannedMissions,
    selectedMission,
    setMissions,
    addMission,
    updateMissionStatus,
    selectMission,
  }
})
