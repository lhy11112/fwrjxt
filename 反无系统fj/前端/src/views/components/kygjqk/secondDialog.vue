<template>
  <el-dialog :title="title" v-model="visible" destroy-on-close width="1100" class="ct_dialog" @close="visible = false">
    <el-table :data="tableData" height="550" v-loading="loading" :show-overflow-tooltip="{effect: 'light'}">
      <el-table-column v-for="(t, i) in column" :key="i" :prop="t.prop" :label="t.label" align="center" :width="t.width"></el-table-column>
       <el-table-column fixed="right" label="操作" width="80" align="center">
          <template #default="scope">
            <el-button
              style="color: #b7d2ff !important"
          
              type="primary"
              size="small"
              @click.prevent="detailOpen(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
    </el-table>
    <div style="margin-top: 5px;line-height: 32px;display: flex;justify-content: flex-end;color: #fff;">
      <span style="margin-right: 10px;">共{{pagination.total}}条</span>
      <el-pagination
        :page-size="10"
        :current-page="pagination.pageNo"
        layout="prev, pager, next"
        @size-change="sizeChange"
        @current-change="currentChange"
        :total="pagination.total"
      />
    </div>
  </el-dialog>
  <detailInfoDialog ref="detailInfoDialog" :column="column"></detailInfoDialog>
</template>

<script>
import http from "@/utils/request.js"
import detailInfoDialog from './detailInfoDialog.vue'
export default {
  name: 'secondDialog',
  components: {
    detailInfoDialog
  },
  props: {
    title: {
      type: String,
      default: '详情'
    },
    apiUrl: {
      type: String,
      default: ''
    },
    // 是否显示
    visibleFlag: {
      type: Boolean,
      default: false
    },
    params: {
      type: Object,
      default: ()=>{}
    },
    column: {
      type: Array,
      default: ()=>{return []}
    },
    bdnm: String
  },
  data() {
    return {
      visible: false,
      loading: false,
      pagination: {
        pageNo: 1,
        pageSize: 10,
        total: 0
      },
      tableData: []
    }
  },
  mounted(){
    
  },
  watch:{
    visibleFlag: {
      deep: true,
      immediate: true,
      handler(x){
        this.visible = x
      }
    }
  },
  methods: {
    open(data){
      this.visible = true
      this.pagination = {
        pageNo: 1,
        pageSize: 10,
        total: 0,
        order: "descs",
        column: "gjfssj"
      }
      this.getData()
    },
    getData(){
      this.loading = true;
      http.get(this.apiUrl,Object.assign(this.pagination,this.params)).then(res=>{
        if(res.success){
          this.tableData = res.result.records;
          this.pagination.total = res.result.total;
        };
        this.loading = false;
      })
    },
    sizeChange(val){
      this.pagination.pageSize = val
      this.getData()
    },
    currentChange(val){
      this.pagination.pageNo = val
      this.getData()
    },
    detailOpen(item){
      this.$nextTick(()=>{
        this.$refs.detailInfoDialog.open(item)
      })
    }
  }
}
</script>

<style lang="less" scoped>
@import "@/style/dialog.css";
.detailInfo_con{
  min-height: 62vh;
  font-size: 15px;
  height: 62vh;
    overflow-y: auto;
  .el-col{
    margin-top: 10px;
    display: flex;
    padding: 5px 0;
  }
  .detailInfo_label{
    color: rgba(255, 255, 255, 0.7);
    font-weight: bold;
    width: 120px;
    text-align: right;
  }
  .detailInfo_text{
    width: calc(100% - 120px);
  }
}
:deep(.el-table .el-table__body td.el-table-fixed-column--right.el-table__cell){
        background: #27598c  !important;
      }
      :deep(.el-table th.el-table-fixed-column--right.el-table__cell){
        background: #27598c  !important;
      }
</style>
