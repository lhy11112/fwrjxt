<template>
  <div class="pageBox">
    <div class="pkgl" style="width:100%;height:100%;">
      <!-- <div class="top-input">
        <el-row :gutter="20">
          <el-col :span="6">
            <label>目标名称:</label>
            <el-input v-model="queryInfo.mbmc" :placeholder="'请输入目标名称'" clearable>
            </el-input>
          </el-col>
          <el-button type="primary" @click="inquires(1)"
            ><el-icon><Search /></el-icon>查询</el-button
          >
          <el-button @click="reset"
            ><el-icon><Refresh /></el-icon>重置</el-button
          >
        </el-row>
      </div> -->
      <el-header style="padding:0;">
        <div class="left-panel">
          <el-button type="primary" @click="table_add"
            ><el-icon><Plus /></el-icon>增加</el-button
          >
          <!-- <el-button
            type="info"
            :disabled="selection.length == 0"
            @click="table_edit(selection)"
          >
            <el-icon><Edit /></el-icon>编辑
          </el-button> -->
          <el-button
            type="danger"
            :disabled="selection.length == 0"
            @click="batch_del"
          >
            <el-icon><Delete /></el-icon>删除
          </el-button>
          <!-- <el-button
            type="primary"
            :disabled="selection.length == 0"
            @click="table_check(selection)"
          >
            <el-icon><ZoomIn /></el-icon>查看
          </el-button> -->
        </div>
      </el-header>
      <div class="tableBox">
        <el-table
          :data="tableData"
          style="width: 100%; height: 100%"
          class="custom-table"
          height="500"
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
      </div>
      <!-- <div style="margin-top: 10px;display: flex;justify-content: flex-end;">
        <el-pagination
          class="pagination"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageOption.pageNo"
          :page-size="pageOption.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-sizes="[8, 10, 20, 50,100]"
        >
        </el-pagination>
      </div> -->

    </div>
  </div>
  <pkDialog v-if="dialog.pkVisible" ref="pkRef" @closed="closedEvent" @successClick="successEvent" :kyid="kyid"></pkDialog>
</template>

<script>
import { ElNotification, ElMessageBox, ElMessage } from "element-plus";
import { h } from "vue";
import config from "@/config"
import http from "@/utils/request"
import pkDialog from "./pkDialog.vue"
export default {
  props: {
    
  },
  components: {
    pkDialog
  },
  data() {
    return {
      headers: {
        'x-access-token': this.$TOOL.data.get("TOKEN")
      },
      currentIndex: 0,
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
      tableData:[],
      pageOption:{
        pageNo:1,
        pageSize:10,
      },
      total:0,
      queryInfo:{},
      selection:[],
      wuziFlag: false,
      kyid: "", // 查看物资时仓库的标识
      dialog:{
        pkVisible:false,
      },
      url: {
        list: "/wrj/wjbdWrjKysq/list",
        add: "/wrj/wjbdWrjKysq/add",
        edit: "/wrj/wjbdWrjKysq/edit",
        delete: "/wrj/wjbdWrjKysq/deleteBatch",
        import: config.API_URL+"/wrj/wjbdWrjKysq/importExcel",
        export: "/wrj/wjbdWrjKysq/exportXls"
      },
    };
  },
  created() {
    
  },
  methods: {
    //新增
    table_add(){
      this.dialog.pkVisible = true;
      this.$nextTick(() => {
        this.$refs.pkRef.open('add')
      })
    },
    //编辑
    table_edit(row){
      if(this.selection.length!=1){
        ElMessage({
          type: "info",
          message: h("p", null, [h("span", null, '请选择一项数据')]),
        });
        return
      }
      this.dialog.pkVisible = true;
      this.$nextTick(() => {
        this.$refs.pkRef.open('edit')
        this.$refs.pkRef.setData(row[0])
      })
    },
    // 查看
    table_check(row){
      if(this.selection.length!=1){
        ElMessage({
          type: "info",
          message: h("p", null, [h("span", null, '请选择一项数据')]),
        });
        return
      }
      this.dialog.pkVisible = true;
      this.$nextTick(() => {
        this.$refs.pkRef.open('show')
        this.$refs.pkRef.setData(row[0])
      })
    },
    //删除
    async table_del(row) {
      // var reqData = {id: row.id}
      http.delete(`${config.API_URL}`+this.url.delete,null,{params:{id: row.id}}).then(res=>{
        if(res.code == 200){
          this.$message.success("操作成功")
          this.inquires();
        }
      })
    },
    //批量删除
    batch_del() {
      var ids = [];
      ElMessageBox.confirm(`确定删除选中的 ${this.selection.length} 项吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
      .then(() => {
        for (var i = 0; i < this.selection.length; i++) {
          ids.push(this.selection[i].zjbid);
        }
        http.delete(`${config.API_URL}`+this.url.delete,null,{params:{ids: ids.join()}}).then(res=>{
          if(res.code == 200){
            ElMessage.success("操作成功");
            this.inquires();
          }
        })
      })
      .catch(() => {});
    },
    closedEvent(){
      this.dialog.pkVisible = false;
    },
    successEvent(){
      this.dialog.pkVisible = false;
      this.inquires()
    },
    //查询
    inquires(type){
      if(type) this.pageOption.pageNo = 1;
      this.getData(this.kyid)
    },
    //重置
    reset(){
      this.queryInfo = {}
      this.getData()
    },
    selectionChange(e){
      this.selection = e;
    },
    //条数切换
    handleSizeChange(val){
      this.pageOption.pageSize = val;
      this.getData();
    },
    //页数切换
    handleCurrentChange(val){
      this.pageOption.pageNo = val;
      this.getData();
    },
    getData(id=""){
      this.kyid = id
      http.get(config.API_URL+"/wrj/wjbdWrjKy/listAllByKyid",{kyid: this.kyid,pageSize: 999}).then(res=>{
        if(res.success){
          this.tableData = res.result.records;
        }
      })
    },

  },
};
</script>

<style scoped lang="less">
.pageBox {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  // background: url(@/assets/firstPage/sixDesign/system.png) no-repeat;
  // background-size: 100% 100%;
  box-sizing: border-box;
  // background: #594a0e;
}
.powerPage {
  width: 100%;
  height: calc(100% - 80px);
  top: 8%;
  position: absolute;
  left: 0;
  z-index: 20;
  display: flex;
  flex-direction: column;
}
.topBoxList {
  width: 100%;
  height: 4%;
  display: flex;
  padding: 0 20px;
  box-sizing: border-box;
  font-size: 17px;
}
.contentListBox {
  width: 100%;
  height: 100%;
  display: flex;
  box-sizing: border-box;
}

.left{
  width: 20%;
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  background-color: #756940;
  position: relative;
}
.title {
  width: 100%;
  height: 3%;
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.title img {
  width: 233px;
  height: 28px;
}

.right{
  width: 80%;
  height: 100%;
  background-color: #756940;
  margin-left: 10px;
  box-sizing: border-box;
  padding: 15px;
  color: #fff;
}
.tableBox{
  height:calc(100% - 155px)
}

.top-input {
  padding: 10px 0 10px 0px;
}

.top-input .el-col {
  display: flex;
}

.top-input .el-col label {
  width: auto;
  line-height: 33px;
  // text-align: right;
}
</style>
