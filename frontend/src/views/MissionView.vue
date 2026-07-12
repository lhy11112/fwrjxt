<script setup lang="ts">
import { ref } from 'vue'
import { useMissionStore } from '@/stores/mission'
import { useSituationStore } from '@/stores/situation'
import { planIntercept, planMultiIntercept, cancelMission } from '@/api'
import { MissionStatus } from '@/types'

const mission = useMissionStore()
const situation = useSituationStore()
const planning = ref(false)

async function handleSinglePlan(targetId: string) {
  planning.value = true
  try {
    const result = await planIntercept({ target_id: targetId, interceptor_id: '00000000-0000-0000-0000-000000000001' })
    if (result.mission_id) mission.addMission(result)
  } catch (e) { console.error(e) }
  finally { planning.value = false }
}

async function handleMultiPlan() {
  const redTargets = situation.redThreats.map(t => t.target_id)
  if (!redTargets.length) return
  planning.value = true
  try {
    const result = await planMultiIntercept({ target_ids: redTargets, interceptor_ids: [] })
    if (Array.isArray(result)) result.forEach(m => mission.addMission(m))
  } catch (e) { console.error(e) }
  finally { planning.value = false }
}

async function handleCancel(id: string) {
  await cancelMission(id)
  mission.updateMissionStatus(id, MissionStatus.Cancelled)
}
</script>

<template>
  <div class="mission-center">
    <div class="mission-content">
      <div class="mission-toolbar">
        <button class="btn-primary" @click="handleMultiPlan" :disabled="planning">
          <Icon name="target" :size="15" />{{ planning ? '规划中...' : '自动拦截高危目标' }}
        </button>
        <div class="strategy-tabs">
          <span class="strategy-tab active">集中式分配</span>
          <span class="strategy-tab">分布式协商</span>
          <span class="strategy-tab">分层混合</span>
        </div>
      </div>
      <div class="mission-table-wrap">
        <table class="mission-table" v-if="mission.missions.length">
          <thead><tr><th>目标ID</th><th>状态</th><th>优先级</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="m in mission.missions" :key="m.mission_id" @click="mission.selectMission(m.mission_id)"
                :class="{ sel: mission.selectedMissionId === m.mission_id }">
              <td class="mono">{{ m.target_id.slice(0,8) }}..</td>
              <td><span class="tag" :class="m.status.toLowerCase()">{{ m.status }}</span></td>
              <td>P{{ m.priority }}</td>
              <td><button class="btn-sm danger" @click.stop="handleCancel(m.mission_id)" v-if="m.status==='ACTIVE'||m.status==='PLANNING'">取消</button></td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-msg"><Icon name="target" :size="16" /> 暂无任务 — 点击"自动拦截高危目标"生成拦截方案</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.mission-center { height: 100%; display: flex; background: var(--bg-canvas); }
.mission-content { flex:1; display:flex; flex-direction:column; padding:var(--sp-l); }
.mission-toolbar { display:flex; align-items:center; gap:var(--sp-m); margin-bottom:var(--sp-m); }
.btn-primary {
  display:inline-flex; align-items:center; justify-content:center; gap:6px;
  height:32px; padding:0 var(--sp-l); border:1px solid transparent; border-radius:var(--radius-md);
  background:var(--brand-rest);
  color:#fff; font-family:var(--font-base); font-size:14px; font-weight:600; cursor:pointer;
  box-shadow:0 0 14px var(--glow-accent);
  transition:background var(--dur-normal) var(--ease-fluent), box-shadow var(--dur-normal) var(--ease-fluent);
}
.btn-primary:hover { background:var(--brand-hover); box-shadow:0 0 20px var(--glow-accent); }
.btn-primary:active { background:var(--brand-pressed); }
.btn-primary:disabled { opacity:.4; cursor:not-allowed; }
.strategy-tabs { display:flex; gap:var(--sp-xs); }
.strategy-tab {
  padding:6px var(--sp-m); border-radius:var(--radius-md); font-size:13px; color:var(--fg-3); cursor:pointer;
  background:transparent; border:1px solid var(--stroke-control);
  transition:background var(--dur-normal) var(--ease-fluent), color var(--dur-normal) var(--ease-fluent);
}
.strategy-tab:hover { background:var(--bg-subtle-hover); color:var(--fg-1); }
.strategy-tab.active { background:var(--brand-selected); color:var(--brand-fg); border-color:transparent; }
.mission-table-wrap { flex:1; overflow-y:auto; }
.mission-table { width:100%; border-collapse:collapse; font-size:13px; }
.mission-table th { padding:var(--sp-s) var(--sp-m); text-align:left; color:var(--fg-3); border-bottom:1px solid var(--divider); font-weight:600; }
.mission-table td { padding:var(--sp-s) var(--sp-m); border-bottom:1px solid var(--stroke-divider); border-left:2px solid transparent; }
.mission-table tr { cursor:pointer; }
.mission-table tr:hover td { background:var(--brand-subtle); }
.mission-table tr.sel td { background:var(--brand-selected); }
.mission-table tr.sel td:first-child { border-left-color:var(--brand-fg); }
.mono { font-family:var(--font-mono); color:var(--fg-3); }
.tag { padding:2px var(--sp-s); border-radius:var(--radius-md); font-size:11px; font-weight:600; }
.tag.active { background:var(--danger-bg); color:var(--danger); }
.tag.planning { background:var(--warning-bg); color:var(--warning); }
.tag.completed { background:var(--success-bg); color:var(--success); }
.btn-sm { padding:4px var(--sp-m); border:1px solid transparent; border-radius:var(--radius-md); cursor:pointer; font-size:12px; font-weight:600; transition:background var(--dur-normal) var(--ease-fluent); }
.btn-sm.danger { background:var(--danger-bg); color:var(--danger); }
.btn-sm.danger:hover { background:var(--danger); color:#fff; }
.empty-msg { text-align:center; padding:60px 20px; color:var(--fg-4); font-size:14px; }
</style>
