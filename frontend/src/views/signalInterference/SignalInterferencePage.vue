<script setup lang="ts">
/**
 * 信号干扰 (Signal Interference) — 移植自源项目 portal/signalInterference
 */
import { ref } from 'vue'

const bands = [
  { key: '900M', freq: '900MHz', active: false },
  { key: '1.4G', freq: '1.4GHz', active: false },
  { key: '2.4G', freq: '2.4GHz', active: false },
  { key: '5.2G', freq: '5.2GHz', active: false },
  { key: '5.8G', freq: '5.8GHz', active: false },
]

const attackMode = ref(0) // 0=返航, 1=迫降
</script>

<template>
  <div class="si-page">
    <div class="page-header">
      <h2 class="page-title">信号干扰</h2>
      <p class="page-desc">多频段干扰设备控制</p>
    </div>

    <div class="page-body">
      <div class="card">
        <div class="card-title">干扰频段选择</div>
        <div class="band-grid">
          <div
            v-for="b in bands"
            :key="b.key"
            class="band-chip"
            :class="{ active: b.active }"
            @click="b.active = !b.active"
          >
            {{ b.freq }}
          </div>
        </div>
      </div>

      <div class="card" style="margin-top: 16px">
        <div class="card-title">攻击模式</div>
        <el-radio-group v-model="attackMode">
          <el-radio :value="0">返航</el-radio>
          <el-radio :value="1">迫降</el-radio>
        </el-radio-group>
      </div>
    </div>
  </div>
</template>

<style scoped>
.si-page {
  width: 100%; height: 100%; display: flex; flex-direction: column;
  background: var(--bg-canvas); color: var(--fg-1);
}
.page-header { padding: 20px 24px 12px; border-bottom: 1px solid var(--divider); }
.page-title { font-size: 18px; font-weight: 700; margin: 0; }
.page-desc { font-size: 13px; color: var(--fg-3); margin: 4px 0 0; }
.page-body { flex: 1; padding: 16px 24px; overflow-y: auto; }
.card { background: var(--bg-surface); border: 1px solid var(--divider); border-radius: var(--radius-md); padding: 16px; }
.card-title { font-size: 14px; font-weight: 600; color: var(--fg-2); margin-bottom: 12px; }
.band-grid { display: flex; gap: 10px; flex-wrap: wrap; }
.band-chip { padding: 8px 18px; border: 1px solid var(--divider); border-radius: 20px; cursor: pointer; font-size: 13px; font-weight: 600; transition: all 0.2s; }
.band-chip:hover { border-color: var(--brand-fg); }
.band-chip.active { background: var(--brand-selected); border-color: var(--brand-fg); color: var(--brand-fg); box-shadow: 0 0 10px var(--glow-accent); }
</style>
