<template>
  <div class="dm-page">
    <div class="dm-hdr">
      <h2>数据管理</h2>
      <p>反无人机业务数据统一管理</p>
    </div>
    <el-tabs v-model="active" class="dm-tabs" tab-position="left">
      <el-tab-pane v-for="t in tabs" :key="t.name" :label="t.label" :name="t.name">
        <div class="dm-content">
          <CrudPage v-if="active === t.name" v-bind="t.props" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, markRaw } from 'vue'
import CrudPage from '@/components/CrudPage.vue'
import {
  jbxxNewApi, czspApi, jkglApi, lpwjApi, yhglCsApi, zskApi, dxyyBhApi, opLogApi, spectrumResultApi,
} from '@/api/wrj'
import { dictApi } from '@/api/wrj'

const active = ref('jbxxNew')

const tabs: any[] = markRaw([
  {
    name: 'jbxxNew', label: '无人机详情',
    props: {
      title: '无人机详细信息', api: jbxxNewApi,
      columns: [
        { prop: 'serial_number', label: '序列号', minWidth: 140 },
        { prop: 'brand', label: '品牌', width: 120 },
        { prop: 'model', label: '型号', width: 120 },
        { prop: 'type', label: '类型', width: 100 },
        { prop: 'remark', label: '备注', minWidth: 160 },
      ],
      searchFields: [{ prop: 'serial_number', label: '序列号' }, { prop: 'brand', label: '品牌' }],
      formFields: [
        { prop: 'serial_number', label: '序列号', required: true },
        { prop: 'brand', label: '品牌' }, { prop: 'model', label: '型号' }, { prop: 'type', label: '类型' },
        { prop: 'jj', label: '价格' }, { prop: 'dy', label: '电压' },
        { prop: 'remark', label: '备注', type: 'textarea', span: 24 },
      ],
    },
  },
  {
    name: 'czsp', label: '操作视频',
    props: {
      title: '操作视频', api: czspApi,
      columns: [
        { prop: 'mc', label: '名称', minWidth: 160 }, { prop: 'spfl', label: '视频分类', width: 140 },
        { prop: 'splj', label: '视频路径', minWidth: 220 },
      ],
      searchFields: [{ prop: 'mc', label: '名称' }],
      formFields: [
        { prop: 'mc', label: '名称', required: true }, { prop: 'spfl', label: '视频分类' },
        { prop: 'splj', label: '视频路径', span: 24 },
      ],
    },
  },
  {
    name: 'jkgl', label: '接口管理',
    props: {
      title: '接口管理', api: jkglApi, idField: 'id',
      columns: [
        { prop: 'bm', label: '编码', width: 140 }, { prop: 'url', label: '接口地址', minWidth: 220 },
        { prop: 'client', label: '客户端', width: 140 }, { prop: 'bz', label: '备注', minWidth: 160 },
      ],
      searchFields: [{ prop: 'bm', label: '编码' }],
      formFields: [
        { prop: 'bm', label: '编码' }, { prop: 'url', label: '接口地址', span: 24 },
        { prop: 'client', label: '客户端' }, { prop: 'secret', label: '密钥' },
        { prop: 'bz', label: '备注', type: 'textarea', span: 24 },
      ],
    },
  },
  {
    name: 'lpwj', label: '录屏文件',
    props: {
      title: '录屏文件', api: lpwjApi,
      columns: [
        { prop: 'mc', label: '名称', minWidth: 160 }, { prop: 'qdwjml', label: '文件目录', minWidth: 220 },
        { prop: 'yhid', label: '用户', width: 140 },
      ],
      searchFields: [{ prop: 'mc', label: '名称' }],
      formFields: [
        { prop: 'mc', label: '名称', required: true }, { prop: 'qdwjml', label: '文件目录', span: 24 },
        { prop: 'yhid', label: '用户ID' },
      ],
    },
  },
  {
    name: 'zsk', label: '知识库',
    props: {
      title: '知识库', api: zskApi, idField: 'id',
      columns: [
        { prop: 'zsk_mc', label: '知识库名称', minWidth: 180 }, { prop: 'zsk_ms', label: '描述', minWidth: 220 },
        { prop: 'chjr_mc', label: '创建人', width: 120 }, { prop: 'rksj', label: '入库时间', width: 170 },
      ],
      searchFields: [{ prop: 'zsk_mc', label: '名称' }],
      formFields: [
        { prop: 'zsk_mc', label: '知识库名称', required: true }, { prop: 'zsk_nm', label: '内部编码' },
        { prop: 'zsk_ms', label: '描述', type: 'textarea', span: 24 },
      ],
    },
  },
  {
    name: 'dxyyBh', label: '标绘管理',
    props: {
      title: '标绘管理', api: dxyyBhApi, idField: 'id',
      columns: [
        { prop: 'bhmc', label: '标绘名称', minWidth: 160 }, { prop: 'tsmc', label: '态势名称', width: 140 },
        { prop: 'dwmc', label: '单位', width: 140 }, { prop: 'cjsj', label: '创建时间', width: 170 },
      ],
      searchFields: [{ prop: 'bhmc', label: '标绘名称' }],
      formFields: [
        { prop: 'bhmc', label: '标绘名称', required: true }, { prop: 'tsmc', label: '态势名称' },
        { prop: 'dwmc', label: '单位名称' }, { prop: 'gisjson', label: 'GIS数据', type: 'textarea', span: 24 },
      ],
    },
  },
  {
    name: 'spectrum', label: '频谱结果',
    props: {
      title: '频谱结果', api: spectrumResultApi, idField: 'id', formFields: [],
      columns: [
        { prop: 'station_id', label: '站ID', width: 90 }, { prop: 'channel', label: '通道', width: 100 },
        { prop: 'start_freq', label: '起始频率', width: 120 }, { prop: 'stop_freq', label: '终止频率', width: 120 },
        { prop: 'create_time', label: '时间', width: 170 },
      ],
      searchFields: [{ prop: 'channel', label: '通道' }],
    },
  },
  {
    name: 'yhglCs', label: '用户参数',
    props: {
      title: '用户参数', api: yhglCsApi, idField: 'id',
      columns: [
        { prop: 'cs_mc', label: '参数名称', minWidth: 160 }, { prop: 'cs_bm', label: '参数编码', width: 160 },
        { prop: 'csz', label: '参数值', minWidth: 160 }, { prop: 'yh_id', label: '用户', width: 120 },
      ],
      searchFields: [{ prop: 'cs_bm', label: '参数编码' }],
      formFields: [
        { prop: 'cs_mc', label: '参数名称', required: true }, { prop: 'cs_bm', label: '参数编码' },
        { prop: 'csz', label: '参数值' }, { prop: 'yh_id', label: '用户ID' },
        { prop: 'bz', label: '备注', type: 'textarea', span: 24 },
      ],
    },
  },
  {
    name: 'dict', label: '数据字典',
    props: {
      title: '数据字典', api: dictApi, idField: 'id',
      columns: [
        { prop: 'dict_name', label: '字典名称', minWidth: 160 }, { prop: 'dict_code', label: '字典编码', width: 180 },
        { prop: 'description', label: '描述', minWidth: 200 },
      ],
      searchFields: [{ prop: 'dict_name', label: '名称' }, { prop: 'dict_code', label: '编码' }],
      formFields: [
        { prop: 'dict_name', label: '字典名称', required: true }, { prop: 'dict_code', label: '字典编码', required: true },
        { prop: 'description', label: '描述', type: 'textarea', span: 24 },
      ],
    },
  },
  {
    name: 'opLog', label: '操作日志',
    props: {
      title: '操作日志', api: opLogApi, selectable: false, formFields: [],
      columns: [
        { prop: 'yymc', label: '应用名称', width: 140 }, { prop: 'yymk', label: '模块', width: 120 },
        { prop: 'rznr', label: '日志内容', minWidth: 220 }, { prop: 'czyhxm', label: '操作人', width: 120 },
        { prop: 'cjsj', label: '时间', width: 170 },
      ],
      searchFields: [{ prop: 'yymc', label: '应用' }, { prop: 'czyhm', label: '操作人' }],
    },
  },
])
</script>

<style scoped>
.dm-page { width: 100%; height: 100%; display: flex; flex-direction: column; background: var(--bg-canvas); color: var(--fg-1); }
.dm-hdr { padding: 16px 24px 12px; border-bottom: 1px solid var(--divider); }
.dm-hdr h2 { font-size: 18px; font-weight: 700; margin: 0; }
.dm-hdr p { font-size: 13px; color: var(--fg-3); margin: 4px 0 0; }
.dm-tabs { flex: 1; overflow: hidden; }
.dm-content { height: 100%; }
:deep(.el-tabs__content) { height: 100%; padding: 0; }
:deep(.el-tab-pane) { height: 100%; }
</style>
