import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { TrackedTarget, ThreatLevel } from '@/types'

export const useSituationStore = defineStore('situation', () => {
  // 状态
  const targets = ref<TrackedTarget[]>([])
  const selectedTargetId = ref<string | null>(null)
  const lastUpdate = ref<string>('')
  const expandedLabels = ref<Set<string>>(new Set())  // 双击展开详情的目标ID

  // 计算属性
  const redThreats = computed(() =>
    targets.value.filter(t => t.threat_level === 'RED')
  )

  const yellowThreats = computed(() =>
    targets.value.filter(t => t.threat_level === 'YELLOW')
  )

  const greenThreats = computed(() =>
    targets.value.filter(t => t.threat_level === 'GREEN')
  )

  const totalTargets = computed(() => targets.value.length)

  const selectedTarget = computed(() =>
    targets.value.find(t => t.target_id === selectedTargetId.value) ?? null
  )

  // 动作
  function updateTargets(newTargets: TrackedTarget[]) {
    targets.value = newTargets
    lastUpdate.value = new Date().toISOString()
  }

  function selectTarget(id: string | null) {
    selectedTargetId.value = id
  }

  function toggleExpandLabel(id: string) {
    const s = new Set(expandedLabels.value)
    if (s.has(id)) s.delete(id); else s.add(id)
    expandedLabels.value = s
  }

  function isLabelExpanded(id: string): boolean {
    return expandedLabels.value.has(id)
  }

  function clearTargets() {
    targets.value = []
  }

  return {
    targets,
    selectedTargetId,
    lastUpdate,
    expandedLabels,
    redThreats,
    yellowThreats,
    greenThreats,
    totalTargets,
    selectedTarget,
    updateTargets,
    selectTarget,
    toggleExpandLabel,
    isLabelExpanded,
    clearTargets,
  }
})
