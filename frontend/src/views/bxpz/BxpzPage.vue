<template>
  <CrudPage
    title="编携配装"
    subtitle="反无人机装备编携配装方案"
    :api="bxpzApi"
    :columns="columns"
    :form-fields="formFields"
    :search-fields="[{ prop: 'mc', label: '名称' }]"
  />
</template>

<script setup lang="ts">
import CrudPage from '@/components/CrudPage.vue'
import { bxpzApi } from '@/api/wrj'

const columns = [
  { prop: 'mc', label: '名称', minWidth: 180 },
  { prop: 'pid', label: '父节点', width: 160 },
  { prop: 'nr', label: '内容', minWidth: 240 },
  { prop: 'remark', label: '备注', minWidth: 160 },
]
const formFields = [
  { prop: 'mc', label: '名称', required: true },
  { prop: 'pid', label: '父节点ID' },
  { prop: 'nr', label: '内容', type: 'textarea' as const, span: 24 },
  { prop: 'remark', label: '备注', type: 'textarea' as const, span: 24 },
]
</script>
