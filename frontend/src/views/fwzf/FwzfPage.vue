<template>
  <CrudPage
    title="反无战法"
    subtitle="反无人机战术战法知识库"
    :api="fwzfApi"
    :columns="columns"
    :form-fields="formFields"
    :search-fields="[{ prop: 'mc', label: '战法名称' }, { prop: 'fl', label: '分类' }]"
  />
</template>

<script setup lang="ts">
import CrudPage from '@/components/CrudPage.vue'
import { fwzfApi } from '@/api/wrj'

const columns = [
  { prop: 'mc', label: '战法名称', minWidth: 150 },
  { prop: 'fl', label: '分类', width: 120 },
  { prop: 'zfgs', label: '战法概述', minWidth: 200 },
  { prop: 'zzcj', label: '组织程序', minWidth: 160 },
  { prop: 'jbzf', label: '具体战法', minWidth: 160 },
  { prop: 'remark', label: '备注', minWidth: 140 },
]
const formFields = [
  { prop: 'mc', label: '战法名称', required: true },
  { prop: 'fl', label: '分类' },
  { prop: 'zfgs', label: '战法概述', type: 'textarea' as const, span: 24 },
  { prop: 'zzcj', label: '组织程序', type: 'textarea' as const, span: 24 },
  { prop: 'jbzf', label: '具体战法', type: 'textarea' as const, span: 24 },
  { prop: 'jtxdff', label: '具体行动方法', type: 'textarea' as const, span: 24 },
  { prop: 'zfyzqk', label: '战法运用情况', type: 'textarea' as const, span: 24 },
  { prop: 'remark', label: '备注', type: 'textarea' as const, span: 24 },
]
</script>
