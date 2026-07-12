<template>
  <div class="drone-library">
    <el-card>
      <template #header><div class="card-header"><span>无人机特征库</span><el-button type="primary" @click="handleAdd">新增机型</el-button></div></template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="品牌"><el-input v-model="searchForm.brand" placeholder="品牌" clearable /></el-form-item>
        <el-form-item label="型号"><el-input v-model="searchForm.model" placeholder="型号" clearable /></el-form-item>
        <el-form-item label="类型"><el-select v-model="searchForm.type" placeholder="全部" clearable><el-option label="军用" value="军用" /><el-option label="民用" value="民用" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="fetchData">查询</el-button><el-button @click="resetSearch">重置</el-button></el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="brand" label="品牌" width="80" />
        <el-table-column prop="model" label="型号" width="120" />
        <el-table-column prop="mc" label="名称" min-width="120" />
        <el-table-column prop="type" label="类型" width="60" />
        <el-table-column prop="dqfl" label="地区" width="70" />
        <el-table-column prop="zlfl" label="种类" width="80" />
        <el-table-column prop="jc" label="机长" width="70" />
        <el-table-column prop="yz" label="翼展" width="70" />
        <el-table-column prop="xhsj" label="续航时间" width="80" />
        <el-table-column prop="zdsd" label="最大速度" width="80" />
        <el-table-column prop="kzbj" label="控制半径" width="80" />
        <el-table-column prop="scdw" label="生产单位" min-width="120" />
        <el-table-column prop="serial_number" label="序列号" width="130" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right"><template #default="{row}">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template></el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total"
        layout="total, sizes, prev, pager, next, jumper" @change="fetchData" style="margin-top:16px" />
    </el-card>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="700px" @close="resetForm">
      <el-form :model="form" ref="formRef" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="名称" prop="mc"><el-input v-model="form.mc" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="序列号" prop="serial_number"><el-input v-model="form.serial_number" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="品牌" prop="brand"><el-input v-model="form.brand" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="型号" prop="model"><el-input v-model="form.model" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="类型"><el-select v-model="form.type"><el-option label="军用" value="军用" /><el-option label="民用" value="民用" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="地区分类"><el-input v-model="form.dqfl" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="种类分类"><el-input v-model="form.zlfl" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="机长"><el-input v-model="form.jc" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="机高"><el-input v-model="form.jg" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="翼展"><el-input v-model="form.yz" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="机重"><el-input v-model="form.jz" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="载重"><el-input v-model="form.zz" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="空重"><el-input v-model="form.kz" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="升限"><el-input v-model="form.sx" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="实用升限"><el-input v-model="form.sysx" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="巡航速度"><el-input v-model="form.xhsd" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="巡航高度"><el-input v-model="form.xhgd" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="续航时间"><el-input v-model="form.xhsj" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="最大速度"><el-input v-model="form.zdsd" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="最大航程"><el-input v-model="form.zdhc" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="控制半径"><el-input v-model="form.kzbj" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="作战半径"><el-input v-model="form.zzbj" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="发动机数量"><el-input v-model="form.fdjsl" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="生产单位"><el-input v-model="form.scdw" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="企业名称"><el-input v-model="form.qymc" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="简介"><el-input v-model="form.jj" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="特点"><el-input v-model="form.td" type="textarea" :rows="2" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { droneLibraryApi } from '@/api/dataManage'
import type { DroneFeatureLib } from '@/types'

const loading = ref(false); const tableData = ref<DroneFeatureLib[]>([]); const total = ref(0)
const page = ref(1); const pageSize = ref(20)
const searchForm = reactive({ brand: '', model: '', type: '' })
const dialogVisible = ref(false); const dialogTitle = ref('新增机型'); const formRef = ref()
const form = reactive<DroneFeatureLib>({}) as DroneFeatureLib
let editId: string | undefined

async function fetchData() {
  loading.value = true
  try { const data = await droneLibraryApi.list({ page: page.value, page_size: pageSize.value, ...searchForm }); tableData.value = data.records || []; total.value = data.total || 0 } finally { loading.value = false }
}
function resetSearch() { searchForm.brand = ''; searchForm.model = ''; searchForm.type = ''; fetchData() }
function handleAdd() { dialogTitle.value = '新增机型'; editId = undefined; resetForm(); dialogVisible.value = true }
function handleEdit(row: DroneFeatureLib) { dialogTitle.value = '编辑机型'; editId = row.id; Object.assign(form, row); dialogVisible.value = true }
function resetForm() { const empty: any = {}; Object.assign(form, empty); formRef.value?.resetFields() }
async function handleSubmit() {
  try {
    if (editId) { form.id = editId; await droneLibraryApi.edit(form); ElMessage.success('更新成功') }
    else { await droneLibraryApi.add(form); ElMessage.success('新增成功') }
    dialogVisible.value = false; fetchData()
  } catch (e: any) { ElMessage.error(e.response?.data?.message || e.message) }
}
async function handleDelete(row: DroneFeatureLib) {
  await ElMessageBox.confirm('确认删除？', '警告', { type: 'warning' })
  try { await droneLibraryApi.delete(row.id!); ElMessage.success('删除成功'); fetchData() } catch {}
}
onMounted(() => fetchData())
</script>
<style scoped>.card-header { display: flex; justify-content: space-between; align-items: center; } .search-form { margin-bottom: 12px; }</style>
