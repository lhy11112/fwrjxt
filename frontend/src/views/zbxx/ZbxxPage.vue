<template>
  <CrudPage
    title="装备信息"
    subtitle="反制装备信息管理"
    :api="fzzbApi"
    :columns="columns"
    :form-fields="formFields"
    :search-fields="[{ prop: 'mc', label: '装备名称' }, { prop: 'fl', label: '分类' }]"
  />
</template>

<script setup lang="ts">
import CrudPage from '@/components/CrudPage.vue'
import { fzzbApi } from '@/api/wrj'

const columns = [
  { prop: 'mc', label: '装备名称', minWidth: 160 },
  { prop: 'fl', label: '分类', width: 120 },
  { prop: 'gntd', label: '功能特点', minWidth: 200 },
  { prop: 'jszb', label: '技术指标', minWidth: 180 },
  { prop: 'sl', label: '数量', width: 80 },
  { prop: 'zbly', label: '装备来源', width: 130 },
]
const formFields = [
  { prop: 'mc', label: '装备名称', required: true },
  { prop: 'fl', label: '分类' },
  { prop: 'sl', label: '数量' },
  { prop: 'zbly', label: '装备来源' },
  { prop: 'gntd', label: '功能特点', type: 'textarea' as const, span: 24 },
  { prop: 'jszb', label: '技术指标', type: 'textarea' as const, span: 24 },
  { prop: 'remark', label: '备注', type: 'textarea' as const, span: 24 },
]
</script>
