<template>
  <CrudPage
    title="重要目标"
    subtitle="重点防护重要目标管理（支持地理半径检索）"
    :api="zymbApi"
    :columns="columns"
    :form-fields="formFields"
    :search-fields="[{ prop: 'mc', label: '目标名称' }, { prop: 'rylx', label: '人员类型' }]"
  />
</template>

<script setup lang="ts">
import CrudPage from '@/components/CrudPage.vue'
import { zymbApi } from '@/api/wrj'

const columns = [
  { prop: 'mc', label: '目标名称', minWidth: 160 },
  { prop: 'jd', label: '经度', width: 110 },
  { prop: 'wd', label: '纬度', width: 110 },
  { prop: 'rylx', label: '类别', width: 100 },
  { prop: 'type', label: '类型', width: 90 },
  { prop: 'subtype', label: '子类型', width: 100 },
  { prop: 'ssgk', label: '所属概况', minWidth: 160 },
]
const formFields = [
  { prop: 'mc', label: '目标名称', required: true },
  { prop: 'jd', label: '经度' },
  { prop: 'wd', label: '纬度' },
  { prop: 'rylx', label: '类别' },
  { prop: 'type', label: '类型' },
  { prop: 'subtype', label: '子类型' },
  { prop: 'sl', label: '数量' },
  { prop: 'zb', label: '装备' },
  { prop: 'ssgk', label: '所属概况', type: 'textarea' as const, span: 24 },
]
</script>
