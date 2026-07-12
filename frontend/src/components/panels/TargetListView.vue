<script setup lang="ts">
import { useSituationStore } from '@/stores/situation'

const situation = useSituationStore()
</script>

<template>
  <div class="panel target-list">
    <div class="panel-header">
      威胁目标列表
      <span class="target-count">共 {{ situation.totalTargets }} 个</span>
    </div>
    <div class="panel-body">
      <table class="data-table" v-if="situation.targets.length">
        <thead>
          <tr>
            <th>ID</th>
            <th>类型</th>
            <th>距离</th>
            <th>意图</th>
            <th>等级</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="target in situation.targets" :key="target.target_id"
              :class="{ selected: situation.selectedTargetId === target.target_id }"
              @click="situation.selectTarget(target.target_id)">
            <td class="cell-id">{{ target.target_id.slice(0, 6) }}</td>
            <td>{{ target.classification }}</td>
            <td>{{ target.position.altitude.toFixed(0) }}m</td>
            <td>
              <span class="intent-tag">{{ target.threat_level === 'RED' ? '攻击' : target.threat_level === 'YELLOW' ? '侦察' : '巡航' }}</span>
            </td>
            <td>
              <span class="badge" :class="{
                'badge-red': target.threat_level === 'RED',
                'badge-yellow': target.threat_level === 'YELLOW',
                'badge-green': target.threat_level === 'GREEN',
              }">
                {{ target.threat_level === 'RED' ? '高危' : target.threat_level === 'YELLOW' ? '中危' : '低危' }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty-state">
        <div class="empty-icon"><Icon name="radar" :size="32" /></div>
        <p>等待传感器数据...</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.target-list {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.target-list .panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 0;
}

.target-count {
  font-weight: 400;
  font-size: 11px;
}

.data-table th {
  position: sticky;
  top: 0;
  background: var(--bg-panel);
  z-index: 1;
}

.data-table tr {
  cursor: pointer;
}

.data-table tr:hover td {
  background: rgba(79, 195, 247, 0.05);
}

.data-table tr.selected td {
  background: rgba(79, 195, 247, 0.1);
  border-left: 2px solid var(--accent-blue);
}

.cell-id {
  font-family: monospace;
  font-size: 12px;
  color: var(--text-secondary);
}

.intent-tag {
  color: var(--accent-red);
  font-size: 12px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: var(--text-secondary);
}

.empty-icon {
  font-size: 36px;
  margin-bottom: 8px;
}
</style>
