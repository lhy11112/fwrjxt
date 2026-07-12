<script setup lang="ts">
/**
 * 模拟推演 (Simulated Exercise) — 移植自源项目 portal/simulatedExercise
 */
import { ref } from 'vue'

const scenarios = ref<any[]>([])
</script>

<template>
  <div class="se-page">
    <div class="page-header">
      <h2 class="page-title">模拟推演</h2>
      <p class="page-desc">推演计划管理与飞行路线生成</p>
    </div>

    <div class="page-body">
      <el-button type="primary" size="small">新建推演</el-button>
      <el-table :data="scenarios" border stripe size="small" style="margin-top: 12px">
        <el-table-column prop="name" label="推演名称" />
        <el-table-column prop="create_time" label="创建时间" />
        <el-table-column label="操作" />
      </el-table>
      <el-empty v-if="!scenarios.length" description="暂无推演计划" />
    </div>
  </div>
</template>

<style scoped>
.se-page {
  width: 100%; height: 100%; display: flex; flex-direction: column;
  background: var(--bg-canvas); color: var(--fg-1);
}
.page-header { padding: 20px 24px 12px; border-bottom: 1px solid var(--divider); }
.page-title { font-size: 18px; font-weight: 700; margin: 0; }
.page-desc { font-size: 13px; color: var(--fg-3); margin: 4px 0 0; }
.page-body { flex: 1; padding: 16px 24px; overflow-y: auto; }
</style>
