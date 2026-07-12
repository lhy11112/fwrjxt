<template>
  <div class="sim-page">
    <div class="sim-hdr">
      <div><h2>模拟推演</h2><p>无人机航迹推演计划：设定起止点自动生成飞行轨迹</p></div>
      <el-button type="primary" @click="openGenerate">新建推演计划</el-button>
    </div>

    <div class="sim-bd">
      <el-table :data="plans" border stripe size="small" v-loading="loading">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="mc" label="计划名称" min-width="140" />
        <el-table-column prop="serial" label="无人机序列号" width="130" />
        <el-table-column prop="model" label="型号" width="120" />
        <el-table-column prop="rq" label="推演日期" width="120" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.tyzt === '1' ? 'success' : 'warning'">{{ row.tyzt === '1' ? '已完成' : '进行中' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewTrajectory(row)">查看轨迹</el-button>
            <el-button size="small" type="success" @click="toggleStatus(row)">{{ row.tyzt === '1' ? '重开' : '完成' }}</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无推演计划" /></template>
      </el-table>
      <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize" small
        layout="total, prev, pager, next" @change="fetchData" style="margin-top:12px; justify-content:flex-end" />
    </div>

    <!-- 新建推演 -->
    <el-dialog v-model="genVisible" title="新建推演计划" width="640px" append-to-body>
      <el-form :model="form" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="计划名称" required><el-input v-model="form.mc" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="推演日期"><el-input v-model="form.rq" placeholder="YYYY-MM-DD" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="无人机序列号"><el-input v-model="form.serial" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="型号"><el-input v-model="form.model" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="起点经度"><el-input-number v-model="form.start_jd" :precision="5" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="起点纬度"><el-input-number v-model="form.start_wd" :precision="5" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="起点高度"><el-input-number v-model="form.start_gd" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="终点经度"><el-input-number v-model="form.end_jd" :precision="5" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="终点纬度"><el-input-number v-model="form.end_wd" :precision="5" controls-position="right" style="width:100%" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="终点高度"><el-input-number v-model="form.end_gd" controls-position="right" style="width:100%" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="genVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGenerate">生成轨迹</el-button>
      </template>
    </el-dialog>

    <!-- 轨迹查看 -->
    <el-dialog v-model="trajVisible" title="推演轨迹点" width="720px" append-to-body>
      <el-table :data="trajectory" border size="small" height="420">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="data_time" label="时间" width="170" />
        <el-table-column prop="dron_lng" label="经度" />
        <el-table-column prop="dron_lat" label="纬度" />
        <el-table-column prop="altitude" label="高度" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { tyjhApi } from '@/api/wrj'

const loading = ref(false)
const plans = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const genVisible = ref(false)
const trajVisible = ref(false)
const trajectory = ref<any[]>([])

const defaultForm = () => ({ mc: '', rq: new Date().toISOString().slice(0, 10), serial: '', model: '', start_jd: 108.94, start_wd: 34.26, start_gd: 100, end_jd: 109.0, end_wd: 34.32, end_gd: 120 })
const form = reactive<any>(defaultForm())

async function fetchData() {
  loading.value = true
  try {
    const data = await tyjhApi.list({ page: page.value, page_size: pageSize.value })
    plans.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function openGenerate() {
  Object.assign(form, defaultForm())
  genVisible.value = true
}

async function submitGenerate() {
  if (!form.mc) { ElMessage.warning('请输入计划名称'); return }
  try {
    await tyjhApi.generate(form)
    ElMessage.success('推演计划已生成')
    genVisible.value = false
    fetchData()
  } catch (e: any) {
    ElMessage.error(e.message || '生成失败')
  }
}

async function viewTrajectory(row: any) {
  try {
    const data = await tyjhApi.data({ tyjh_id: row.id, page: 1, page_size: 500 })
    trajectory.value = data.records || []
    trajVisible.value = true
  } catch (e: any) {
    ElMessage.error(e.message || '加载轨迹失败')
  }
}

async function toggleStatus(row: any) {
  const zt = row.tyzt === '1' ? '0' : '1'
  await tyjhApi.changeStatus(row.id, zt)
  ElMessage.success('状态已更新')
  fetchData()
}

async function remove(row: any) {
  try {
    await ElMessageBox.confirm('删除计划将同时删除其轨迹数据，确认？', '提示', { type: 'warning' })
    await tyjhApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

onMounted(fetchData)
</script>

<style scoped>
.sim-page { width: 100%; height: 100%; display: flex; flex-direction: column; background: var(--bg-canvas); color: var(--fg-1); }
.sim-hdr { display: flex; align-items: center; justify-content: space-between; padding: 16px 24px 12px; border-bottom: 1px solid var(--divider); }
.sim-hdr h2 { font-size: 18px; font-weight: 700; margin: 0; }
.sim-hdr p { font-size: 13px; color: var(--fg-3); margin: 4px 0 0; }
.sim-bd { flex: 1; padding: 16px 24px; overflow-y: auto; }
</style>
