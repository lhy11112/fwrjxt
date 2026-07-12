<script setup lang="ts">
import { computed } from 'vue'
import { useSituationStore } from '@/stores/situation'

const situation = useSituationStore()

const selectedDetails = computed(() => {
  const target = situation.selectedTarget
  if (!target) return null

  return {
    id: target.target_id.slice(0, 8),
    type: target.classification,
    confidence: (target.confidence * 100).toFixed(1),
    altitude: target.position.altitude.toFixed(0),
    speed: Math.sqrt(target.velocity.vn ** 2 + target.velocity.ve ** 2).toFixed(1),
    heading: (Math.atan2(target.velocity.ve, target.velocity.vn) * 180 / Math.PI).toFixed(0),
    firstSeen: new Date(target.first_seen).toLocaleTimeString(),
    trackPoints: target.track_history.length,
  }
})
</script>

<template>
  <div class="panel threat-panel">
    <div class="panel-header">目标详情</div>
    <div class="panel-body">
      <template v-if="selectedDetails">
        <div class="detail-section">
          <div class="detail-header">
            <h3 class="target-title">{{ selectedDetails.type }}</h3>
            <span class="badge" :class="{
              'badge-red': situation.selectedTarget?.threat_level === 'RED',
              'badge-yellow': situation.selectedTarget?.threat_level === 'YELLOW',
              'badge-green': situation.selectedTarget?.threat_level === 'GREEN',
            }">
              {{ situation.selectedTarget?.threat_level === 'RED' ? '高危' : situation.selectedTarget?.threat_level === 'YELLOW' ? '中危' : '低危' }}
            </span>
          </div>
          <div class="detail-id">ID: {{ selectedDetails.id }}</div>
        </div>

        <div class="detail-section">
          <h4 class="section-title">运动参数</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">高度</span>
              <span class="value">{{ selectedDetails.altitude }} m</span>
            </div>
            <div class="detail-item">
              <span class="label">速度</span>
              <span class="value">{{ selectedDetails.speed }} m/s</span>
            </div>
            <div class="detail-item">
              <span class="label">航向</span>
              <span class="value">{{ selectedDetails.heading }}°</span>
            </div>
            <div class="detail-item">
              <span class="label">置信度</span>
              <span class="value">{{ selectedDetails.confidence }}%</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4 class="section-title">航迹信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">首次发现</span>
              <span class="value">{{ selectedDetails.firstSeen }}</span>
            </div>
            <div class="detail-item">
              <span class="label">航迹点数</span>
              <span class="value">{{ selectedDetails.trackPoints }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4 class="section-title">操作</h4>
          <div class="action-buttons">
            <button class="btn btn-primary btn-block">生成拦截方案</button>
            <button class="btn btn-block">标注为虚警</button>
            <button class="btn btn-danger btn-block">紧急拦截</button>
          </div>
        </div>
      </template>

      <template v-else>
        <div class="no-selection">
          <div class="empty-icon"><Icon name="target" :size="32" /></div>
          <p>点击地图或左侧列表</p>
          <p>查看目标详情</p>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>
.threat-panel {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.threat-panel .panel-body {
  flex: 1;
  overflow-y: auto;
}

.detail-section {
  padding: 12px 0;
  border-bottom: 1px solid rgba(42, 58, 74, 0.5);
}

.detail-section:last-child {
  border-bottom: none;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.target-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.detail-id {
  font-family: monospace;
  font-size: 11px;
  color: var(--text-secondary);
}

.section-title {
  font-size: 11px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.label {
  font-size: 11px;
  color: var(--text-secondary);
}

.value {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.btn-block {
  width: 100%;
  justify-content: center;
}

.no-selection {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.empty-icon {
  font-size: 40px;
  margin-bottom: 12px;
}
</style>
