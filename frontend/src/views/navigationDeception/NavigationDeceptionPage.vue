<script setup lang="ts">
/**
 * 导航诱骗 (Navigation Deception) — 移植自源项目 portal/navigationDeception
 */
import { ref } from 'vue'

const deceptionModes = [
  { id: 0, name: '待机', desc: 'GNSS诱骗设备待命状态' },
  { id: 1, name: '定向驱逐', desc: '生成虚假GPS信号，引导无人机偏离目标方向' },
  { id: 2, name: '定点迫降', desc: '引导无人机降落到指定安全区域' },
  { id: 3, name: '导航压制', desc: '全面压制无人机导航信号' },
]

const selectedMode = ref(0)
const forcedLandLat = ref(34.08)
const forcedLandLng = ref(108.94)
const forcedLandAlt = ref(420)
</script>

<template>
  <div class="nd-page">
    <div class="page-header">
      <h2 class="page-title">导航诱骗</h2>
      <p class="page-desc">GNSS诱骗设备控制与任务管理</p>
    </div>

    <div class="page-body">
      <div class="card">
        <div class="card-title">诱骗模式</div>
        <div class="mode-grid">
          <div
            v-for="m in deceptionModes"
            :key="m.id"
            class="mode-card"
            :class="{ active: selectedMode === m.id }"
            @click="selectedMode = m.id"
          >
            <div class="mode-name">{{ m.name }}</div>
            <div class="mode-desc">{{ m.desc }}</div>
          </div>
        </div>
      </div>

      <div v-if="selectedMode === 2" class="card" style="margin-top: 16px">
        <div class="card-title">迫降目标位置</div>
        <el-row :gutter="12">
          <el-col :span="8">
            <label class="field-label">纬度</label>
            <el-input-number v-model="forcedLandLat" :precision="6" size="small" controls-position="right" />
          </el-col>
          <el-col :span="8">
            <label class="field-label">经度</label>
            <el-input-number v-model="forcedLandLng" :precision="6" size="small" controls-position="right" />
          </el-col>
          <el-col :span="8">
            <label class="field-label">高度(m)</label>
            <el-input-number v-model="forcedLandAlt" :precision="0" size="small" controls-position="right" />
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<style scoped>
.nd-page {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg-canvas);
  color: var(--fg-1);
}

.page-header {
  padding: 20px 24px 12px;
  border-bottom: 1px solid var(--divider);
}

.page-title { font-size: 18px; font-weight: 700; margin: 0; }
.page-desc { font-size: 13px; color: var(--fg-3); margin: 4px 0 0; }

.page-body {
  flex: 1;
  padding: 16px 24px;
  overflow-y: auto;
}

.card {
  background: var(--bg-surface);
  border: 1px solid var(--divider);
  border-radius: var(--radius-md);
  padding: 16px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--fg-2);
  margin-bottom: 12px;
}

.mode-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.mode-card {
  padding: 16px;
  border: 1px solid var(--divider);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
}
.mode-card:hover { border-color: var(--brand-fg); }
.mode-card.active {
  border-color: var(--brand-fg);
  background: var(--brand-selected);
  box-shadow: 0 0 12px var(--glow-accent);
}

.mode-name { font-size: 15px; font-weight: 600; }
.mode-desc { font-size: 12px; color: var(--fg-3); margin-top: 4px; }

.field-label {
  display: block;
  font-size: 12px;
  color: var(--fg-3);
  margin-bottom: 4px;
}
</style>
