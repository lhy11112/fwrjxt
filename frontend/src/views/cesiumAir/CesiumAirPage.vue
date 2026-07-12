<template>
  <CrudPage
    title="空域管理"
    subtitle="管控空域定义与管理（禁飞区/预警区/管控区）"
    :api="airspaceApi"
    :columns="columns"
    :form-fields="formFields"
    :search-fields="[{ prop: 'mc', label: '空域名称' }]"
    :default-form="{ lx: 'restricted', xz: 'circle', sfqy: 1 }"
  />
</template>

<script setup lang="ts">
import CrudPage from '@/components/CrudPage.vue'
import { airspaceApi } from '@/api/dataManage'

const lxOptions = [
  { label: '禁飞区', value: 'restricted' },
  { label: '预警区', value: 'warning' },
  { label: '管控区', value: 'control' },
]
const xzOptions = [
  { label: '圆形', value: 'circle' },
  { label: '多边形', value: 'polygon' },
  { label: '矩形', value: 'rectangle' },
]
const lxLabel = (v: string) => lxOptions.find(o => o.value === v)?.label || v

const columns = [
  { prop: 'mc', label: '名称', minWidth: 150 },
  { prop: 'lx', label: '类型', width: 100, format: (v: string) => lxLabel(v) },
  { prop: 'xz', label: '形状', width: 90 },
  { prop: 'zxdjd', label: '中心经度', width: 110 },
  { prop: 'zxdwd', label: '中心纬度', width: 110 },
  { prop: 'bj', label: '半径(m)', width: 100 },
  { prop: 'zxgd', label: '最低高度', width: 90 },
  { prop: 'zdgd', label: '最高高度', width: 90 },
  { prop: 'sfqy', label: '启用', width: 70, format: (v: number) => (v === 1 ? '是' : '否') },
]
const formFields = [
  { prop: 'mc', label: '空域名称', required: true },
  { prop: 'lx', label: '类型', type: 'select' as const, options: lxOptions },
  { prop: 'xz', label: '形状', type: 'select' as const, options: xzOptions },
  { prop: 'zxdjd', label: '中心经度', type: 'number' as const, precision: 6 },
  { prop: 'zxdwd', label: '中心纬度', type: 'number' as const, precision: 6 },
  { prop: 'bj', label: '半径(m)', type: 'number' as const },
  { prop: 'zxgd', label: '最低高度(m)', type: 'number' as const },
  { prop: 'zdgd', label: '最高高度(m)', type: 'number' as const },
  { prop: 'sfqy', label: '是否启用', type: 'switch' as const },
  { prop: 'bz', label: '备注', type: 'textarea' as const, span: 24 },
]
</script>
