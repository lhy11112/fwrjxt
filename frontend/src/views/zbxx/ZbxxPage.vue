<script setup lang="ts">
/**
 * 装备信息 (Equipment Info) — 移植自源项目 portal/zbxx
 */
import { ref, onMounted } from 'vue'
import { listDevices } from '@/api/wxdzc'

const devices = ref<any[]>([])

onMounted(async () => {
  try {
    const r = await listDevices({ page: 1, page_size: 50 })
    devices.value = r.records
  } catch {}
})
</script>

<template>
  <div class="zbxx-page">
    <div class="page-header">
      <h2 class="page-title">装备信息</h2>
      <p class="page-desc">反无人机设备与传感器装备清单</p>
    </div>

    <div class="page-body">
      <el-table :data="devices" border stripe size="small">
        <el-table-column prop="name" label="设备名称" />
        <el-table-column prop="device_id" label="设备ID" />
        <el-table-column prop="device_type" label="类型" />
        <el-table-column prop="status" label="状态" />
        <el-table-column prop="device_ip" label="IP" />
        <el-table-column prop="jd" label="经度" />
        <el-table-column prop="wd" label="纬度" />
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.zbxx-page {
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
</style>
