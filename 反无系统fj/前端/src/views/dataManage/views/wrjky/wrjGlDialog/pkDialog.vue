<template>
  <AnalysisMoveDlg
    :title="titleMap[mode]"
    style="width: 50vw;height:auto"
    :visibleDialog="visibleDialog"
    @close="closed"
    isModal="true"
  >
    <el-table
      :data="wrjDataList"
      style="width: 100%; height: 100%"
      class="custom-table"
      height="520"
      @selectionChange="selectionChange"
      @row-click="rowClickChange"
      :row-class-name="tableRowClassName"
    >
      <el-table-column type="selection" width="50"></el-table-column>
      <el-table-column
        v-for="(item, index) in columnData"
        :prop="item.prop"
        :label="item.label"
        :key="index"
        :width="item.width"
        show-overflow-tooltip	
      >
      </el-table-column>
    </el-table>

    <div class="create-bottom">
      <el-button @click="closed">取 消</el-button>
      <el-button :loading="isSaveing" type="primary" @click="submit()"
        >确 定</el-button
      >
    </div>
  </AnalysisMoveDlg>
</template>

<script>
import config from "@/config"
import http from "@/utils/request"
import { ElNotification, ElMessageBox, ElMessage } from "element-plus";
export default {
  props:{
    toDp:{
      type:Boolean,
      default:false
    },
    kyid: {
      type: String,
      default: ""
    }
  },
  components:{
  },
  data() {
    return {
      visibleDialog: false,
      columnData:[
        { prop: "serialNumber", label: "序列号", width: 180 },
        { prop: "brand", label: "品牌" },
        { prop: "model", label: "型号" },
        { prop: "status", label: "状态" },
        { prop: "authStatus", label: "授权状态" },
        { prop: "currentLongitude", label: "当前经度" },
        { prop: "currentLatitude", label: "当前纬度" },
        { prop: "currentAltitude", label: "当前高度" },
      ],
      mode: "add",
      rowObj: {},
      titleMap: {
        add: "新增",
        edit: "编辑",
        show: "查看"
      },
      selection: [],
      wrjDataList: []
    };
  },
  mounted() {
    var params = {
      // order: 'desc',
      // pageNo: 1,
      pageSize: 999,
      kyid: this.kyid
    }
    http.get(config.API_URL+"/wrj/wjbdWrjJbxx/listByKyid",params).then(res=>{
      if(res.success){
        this.wrjDataList = res.result.records;
      }
    })
  },
  methods: {
    selectionChange(e){
      this.selection = e;
    },
    open(item) {
      this.mode = item;
      this.visibleDialog = true;
    },
    closed(){
      this.visibleDialog = false;
      this.$emit('closed')
    },
    setData(data) {
      if(data.jd){
        data.zbd = data.wd + "," + data.jd;
      }
      this.form = Object.assign(this.form, data);
    },
    submit() {
      console.log(this.selection);
      if(!this.selection.length){
        ElMessage.warning("至少选择一条数据");
        return
      }
      http.post(config.API_URL+"/wrj/wjbdWrjKysq/saveKysq",{kyid: this.kyid,wrjids: this.selection.map(item=>{return item.id})}).then(res=>{
        if(res.success){
          this.visibleDialog = false;
          ElMessage.success("操作成功");
          this.$emit("successClick");
        }
      })
    },
  },
};
</script>

<style scoped lang="less">
*{
  box-sizing: border-box;
}
.el-form-item__label {
  color: #fff !important;
}
.upload-demo {
  width: 100% !important;
}
.create-bottom {
  width: 100%;
  display: flex;
  justify-content: center;
  margin: 10px 0;
}
</style>
