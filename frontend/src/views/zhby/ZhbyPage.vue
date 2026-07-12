<template>
  <CrudPage
    title="综合兵要"
    subtitle="地形地貌兵要地理信息管理"
    :api="dxdmApi"
    :columns="columns"
    :form-fields="formFields"
    :search-fields="[{ prop: 'dmmc', label: '地貌名称' }, { prop: 'dxdmlx', label: '类型' }]"
  />
</template>

<script setup lang="ts">
import CrudPage from '@/components/CrudPage.vue'
import { dxdmApi } from '@/api/wrj'

const columns = [
  { prop: 'dmmc', label: '地貌名称', minWidth: 160 },
  { prop: 'dxdmlx', label: '地形地貌类型', width: 140 },
  { prop: 'jd', label: '经度', width: 110 },
  { prop: 'wd', label: '纬度', width: 110 },
  { prop: 'mj', label: '面积', width: 100 },
  { prop: 'dlwz', label: '地理位置', minWidth: 180 },
]
const formFields = [
  { prop: 'dmmc', label: '地貌名称', required: true },
  { prop: 'dxdmlx', label: '地形地貌类型' },
  { prop: 'jd', label: '经度' },
  { prop: 'wd', label: '纬度' },
  { prop: 'mj', label: '面积' },
  { prop: 'dlwz', label: '地理位置', type: 'textarea' as const, span: 24 },
]
</script>
