<template>
  <div class="crud-page">
    <div class="crud-hdr">
      <div>
        <h2>{{ title }}</h2>
        <p v-if="subtitle">{{ subtitle }}</p>
      </div>
      <div class="crud-actions">
        <slot name="toolbar" />
        <el-button v-if="formFields.length" type="primary" @click="openAdd">新增</el-button>
        <el-button v-if="selectable && selectedIds.length" type="danger" @click="handleBatchDelete">
          批量删除({{ selectedIds.length }})
        </el-button>
      </div>
    </div>

    <div class="crud-bd">
      <el-form v-if="searchFields.length" :inline="true" :model="searchForm" class="crud-search">
        <el-form-item v-for="f in searchFields" :key="f.prop" :label="f.label">
          <el-input v-model="searchForm[f.prop]" :placeholder="f.label" clearable @keyup.enter="fetchData" style="width:160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="() => { page = 1; fetchData() }">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" border stripe size="small" v-loading="loading" @selection-change="onSelect">
        <el-table-column v-if="selectable" type="selection" width="42" />
        <el-table-column type="index" label="#" width="50" />
        <el-table-column v-for="c in columns" :key="c.prop" :prop="c.prop" :label="c.label"
          :width="c.width" :min-width="c.minWidth || 120" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="c.format">{{ c.format(row[c.prop], row) }}</span>
            <span v-else>{{ row[c.prop] }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="formFields.length || $slots.rowActions" label="操作" :width="actionWidth" fixed="right">
          <template #default="{ row }">
            <slot name="rowActions" :row="row" />
            <el-button v-if="formFields.length" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无数据" /></template>
      </el-table>

      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total" small
        layout="total, sizes, prev, pager, next, jumper" :page-sizes="[10, 20, 50, 100]"
        @change="fetchData" style="margin-top:12px; justify-content:flex-end" />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" :width="dialogWidth" append-to-body>
      <el-form :model="form" label-width="110px">
        <el-row :gutter="16">
          <el-col v-for="f in formFields" :key="f.prop" :span="f.span || 12">
            <el-form-item :label="f.label" :required="f.required">
              <el-input v-if="!f.type || f.type === 'input'" v-model="form[f.prop]" clearable />
              <el-input v-else-if="f.type === 'textarea'" v-model="form[f.prop]" type="textarea" :rows="2" />
              <el-input-number v-else-if="f.type === 'number'" v-model="form[f.prop]" :precision="f.precision" controls-position="right" style="width:100%" />
              <el-select v-else-if="f.type === 'select'" v-model="form[f.prop]" clearable style="width:100%">
                <el-option v-for="o in f.options || []" :key="o.value" :label="o.label" :value="o.value" />
              </el-select>
              <el-switch v-else-if="f.type === 'switch'" v-model="form[f.prop]" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

interface Column { prop: string; label: string; width?: number; minWidth?: number; format?: (v: any, row: any) => string }
interface FormField { prop: string; label: string; type?: 'input' | 'textarea' | 'number' | 'select' | 'switch'; options?: { label: string; value: any }[]; span?: number; required?: boolean; precision?: number }
interface SearchField { prop: string; label: string }
interface CrudApi { list: (p?: any) => Promise<any>; add: (d: any) => Promise<any>; edit: (d: any) => Promise<any>; delete: (id: any) => Promise<any>; batchDelete?: (ids: any[]) => Promise<any> }

const props = withDefaults(defineProps<{
  title: string
  subtitle?: string
  api: CrudApi
  columns: Column[]
  formFields?: FormField[]
  searchFields?: SearchField[]
  idField?: string
  selectable?: boolean
  dialogWidth?: string
  actionWidth?: number
  defaultForm?: Record<string, any>
}>(), {
  formFields: () => [],
  searchFields: () => [],
  idField: 'id',
  selectable: true,
  dialogWidth: '640px',
  actionWidth: 150,
  defaultForm: () => ({}),
})

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const searchForm = reactive<Record<string, any>>({})
const selectedIds = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = reactive<Record<string, any>>({})
let isEdit = false

async function fetchData() {
  loading.value = true
  try {
    const params: any = { page: page.value, page_size: pageSize.value }
    for (const k in searchForm) if (searchForm[k]) params[k] = searchForm[k]
    const data = await props.api.list(params)
    tableData.value = data.records || []
    total.value = data.total || 0
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function resetSearch() {
  for (const k in searchForm) searchForm[k] = ''
  page.value = 1
  fetchData()
}

function onSelect(rows: any[]) {
  selectedIds.value = rows.map(r => r[props.idField]).filter(v => v !== undefined && v !== null)
}

function openAdd() {
  isEdit = false
  dialogTitle.value = '新增' + props.title
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, JSON.parse(JSON.stringify(props.defaultForm)))
  dialogVisible.value = true
}

function openEdit(row: any) {
  isEdit = true
  dialogTitle.value = '编辑' + props.title
  Object.keys(form).forEach(k => delete form[k])
  Object.assign(form, JSON.parse(JSON.stringify(row)))
  dialogVisible.value = true
}

async function handleSubmit() {
  try {
    if (isEdit) { await props.api.edit(form); ElMessage.success('更新成功') }
    else { await props.api.add(form); ElMessage.success('新增成功') }
    dialogVisible.value = false
    fetchData()
  } catch (e: any) {
    ElMessage.error(e.message || '保存失败')
  }
}

async function handleDelete(row: any) {
  try {
    await ElMessageBox.confirm('确认删除该记录？', '提示', { type: 'warning' })
    await props.api.delete(row[props.idField])
    ElMessage.success('删除成功')
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

async function handleBatchDelete() {
  if (!props.api.batchDelete) return
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 条记录？`, '提示', { type: 'warning' })
    await props.api.batchDelete(selectedIds.value)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

defineExpose({ fetchData })
onMounted(fetchData)
</script>

<style scoped>
.crud-page { width: 100%; height: 100%; display: flex; flex-direction: column; background: var(--bg-canvas); color: var(--fg-1); }
.crud-hdr { display: flex; align-items: center; justify-content: space-between; padding: 16px 24px 12px; border-bottom: 1px solid var(--divider); }
.crud-hdr h2 { font-size: 18px; font-weight: 700; margin: 0; }
.crud-hdr p { font-size: 13px; color: var(--fg-3); margin: 4px 0 0; }
.crud-actions { display: flex; gap: 8px; }
.crud-bd { flex: 1; padding: 16px 24px; overflow-y: auto; }
.crud-search { margin-bottom: 8px; }
</style>
