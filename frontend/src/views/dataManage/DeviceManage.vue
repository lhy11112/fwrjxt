<template>
  <div class="device-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>设备管理</span>
          <el-button type="primary" @click="handleAdd">新增设备</el-button>
        </div>
      </template>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="设备名称"><el-input v-model="searchForm.name" placeholder="设备名称" clearable /></el-form-item>
        <el-form-item label="设备类型"><el-select v-model="searchForm.device_type" placeholder="全部" clearable>
          <el-option label="侦测" value="DETECT" /><el-option label="诱骗" value="TRAP" />
          <el-option label="干扰" value="DISTURB" /><el-option label="系统" value="System" />
        </el-select></el-form-item>
        <el-form-item label="连接状态"><el-select v-model="searchForm.status" placeholder="全部" clearable>
          <el-option label="已连接" value="CONNECTED" /><el-option label="未连接" value="DISCONNECTED" /><el-option label="告警中" value="WARN" />
        </el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="fetchData">查询</el-button><el-button @click="resetSearch">重置</el-button></el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" border stripe v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="name" label="设备名称" min-width="120" />
        <el-table-column prop="device_id" label="设备标识" width="140" />
        <el-table-column prop="station_id" label="站ID" width="70" />
        <el-table-column prop="device_type" label="设备类型" width="80"><template #default="{row}">
          <el-tag :type="typeTag(row.device_type)">{{ typeLabel(row.device_type) }}</el-tag>
        </template></el-table-column>
        <el-table-column prop="device_ip" label="IP地址" width="130" />
        <el-table-column prop="device_port" label="端口" width="70" />
        <el-table-column prop="status" label="状态" width="90"><template #default="{row}">
          <el-tag :type="statusTag(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template></el-table-column>
        <el-table-column prop="is_valid" label="有效性" width="70"><template #default="{row}">
          <el-tag :type="row.is_valid===1?'success':'danger'">{{ row.is_valid===1?'正常':'禁用' }}</el-tag>
        </template></el-table-column>
        <el-table-column prop="zcbj" label="侦测半径" width="90" />
        <el-table-column prop="type" label="协议类型" width="120" />
        <el-table-column label="操作" width="260" fixed="right"><template #default="{row}">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="info" @click="showConnectLogs(row)">连接日志</el-button>
          <el-button size="small" type="warning" @click="showOperateLogs(row)">操作日志</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template></el-table-column>
      </el-table>

      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total"
        layout="total, sizes, prev, pager, next, jumper" @change="fetchData" style="margin-top:16px" />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="设备名称" prop="name"><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备标识" prop="device_id"><el-input v-model="form.device_id" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="站ID" prop="station_id"><el-input-number v-model="form.station_id" :min="1" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="设备类型" prop="device_type"><el-select v-model="form.device_type">
            <el-option label="侦测" value="DETECT" /><el-option label="诱骗" value="TRAP" />
            <el-option label="干扰" value="DISTURB" /><el-option label="系统" value="System" />
          </el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="IP地址" prop="device_ip"><el-input v-model="form.device_ip" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="端口" prop="device_port"><el-input-number v-model="form.device_port" :min="1" :max="65535" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="协议类型"><el-select v-model="form.type"><el-option label="UDP" value="UDP" /><el-option label="TCP Socket" value="TCPSocket" /><el-option label="TCP Server" value="TCPServerSocket" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="UDP端口"><el-input-number v-model="form.udp_port" :min="1" :max="65535" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="有效性"><el-switch v-model="form.is_valid" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="经度"><el-input-number v-model="form.jd" :precision="6" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="纬度"><el-input-number v-model="form.wd" :precision="6" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="高度"><el-input-number v-model="form.gd" :precision="2" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="侦测半径(km)"><el-input v-model="form.zcbj" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="弹药情况"><el-input v-model="form.dyqk" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>

    <!-- 连接日志弹窗 -->
    <el-dialog title="连接日志" v-model="connectLogVisible" width="800px">
      <el-table :data="connectLogs" border size="small"><el-table-column prop="device_ip" label="IP" /><el-table-column prop="event_type" label="事件类型" /><el-table-column prop="event_time" label="时间" /><el-table-column prop="reason" label="原因" /></el-table>
    </el-dialog>

    <!-- 操作日志弹窗 -->
    <el-dialog title="操作日志" v-model="operateLogVisible" width="800px">
      <el-table :data="operateLogs" border size="small"><el-table-column prop="cmd_name" label="命令" /><el-table-column prop="cmd_type" label="命令码" /><el-table-column prop="result" label="结果" /><el-table-column prop="operate_time" label="时间" /></el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deviceApi } from '@/api/dataManage'
import type { DeviceConfig, ConnectLog, OperateLog } from '@/types'

const loading = ref(false)
const tableData = ref<DeviceConfig[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const searchForm = reactive({ name: '', device_type: '', status: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('新增设备')
const formRef = ref()
const selectedIds = ref<number[]>([])
const connectLogVisible = ref(false); const connectLogs = ref<ConnectLog[]>([])
const operateLogVisible = ref(false); const operateLogs = ref<OperateLog[]>([])

const form = reactive<DeviceConfig>({ device_id: '', station_id: 1, device_type: 'DETECT', device_ip: '', device_port: 8888, is_valid: 1, status: 'DISCONNECTED' } as DeviceConfig)
let editId: number | undefined

const rules = {
  name: [{ required: true, message: '请输入设备名称' }],
  device_id: [{ required: true, message: '请输入设备标识' }],
  station_id: [{ required: true }], device_type: [{ required: true }],
  device_ip: [{ required: true, message: '请输入IP地址' }],
  device_port: [{ required: true }],
}

const typeTag = (t: string) => ({ DETECT: 'success', TRAP: 'warning', DISTURB: 'danger', System: 'info' }[t] || '')
const typeLabel = (t: string) => ({ DETECT: '侦测', TRAP: '诱骗', DISTURB: '干扰', System: '系统' }[t] || t)
const statusTag = (s: string) => ({ CONNECTED: 'success', DISCONNECTED: 'info', WARN: 'danger' }[s] || '')
const statusLabel = (s: string) => ({ CONNECTED: '已连接', DISCONNECTED: '未连接', WARN: '告警中' }[s] || s)

async function fetchData() {
  loading.value = true
  try {
    const data = await deviceApi.list({ page: page.value, page_size: pageSize.value, ...searchForm })
    tableData.value = data.records || []; total.value = data.total || 0
  } finally { loading.value = false }
}

function resetSearch() { searchForm.name = ''; searchForm.device_type = ''; searchForm.status = ''; fetchData() }
function handleSelectionChange(rows: DeviceConfig[]) { selectedIds.value = rows.map(r => r.id!).filter(Boolean) }

function handleAdd() { dialogTitle.value = '新增设备'; editId = undefined; resetForm(); dialogVisible.value = true }
function handleEdit(row: DeviceConfig) { dialogTitle.value = '编辑设备'; editId = row.id; Object.assign(form, row); dialogVisible.value = true }
function resetForm() { Object.assign(form, { device_id: '', station_id: 1, device_type: 'DETECT', device_ip: '', device_port: 8888, is_valid: 1, status: 'DISCONNECTED' }); formRef.value?.resetFields() }

async function handleSubmit() {
  await formRef.value?.validate()
  try {
    if (editId) { form.id = editId; await deviceApi.edit(form); ElMessage.success('更新成功') }
    else { await deviceApi.add(form); ElMessage.success('新增成功') }
    dialogVisible.value = false; fetchData()
  } catch (e: any) { ElMessage.error(e.response?.data?.message || e.message) }
}

async function handleDelete(row: DeviceConfig) {
  await ElMessageBox.confirm('确认删除该设备？', '警告', { type: 'warning' })
  try { await deviceApi.delete(row.id!); ElMessage.success('删除成功'); fetchData() } catch (e) {}
}

async function showConnectLogs(row: DeviceConfig) {
  connectLogVisible.value = true
  try { const data = await deviceApi.connectLogs({ page: 1, page_size: 100, station_id: row.station_id }); connectLogs.value = data.records || [] } catch {}
}

async function showOperateLogs(row: DeviceConfig) {
  operateLogVisible.value = true
  try { const data = await deviceApi.operateLogs({ page: 1, page_size: 100, station_id: row.station_id }); operateLogs.value = data.records || [] } catch {}
}

onMounted(() => fetchData())
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.search-form { margin-bottom: 12px; }
</style>
