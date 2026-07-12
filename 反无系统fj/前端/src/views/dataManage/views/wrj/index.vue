<template>
  <div style="width:100%;height:100%;">
            <div class="top-input">
              <el-row>
                <el-col :span="6">
                  <label>型号:</label>
                  <el-input v-model="queryInfo.model" size="small" clearable></el-input>
                </el-col>
                <el-col :span="6" style="margin-left:10px">
                  <label>序列号:</label>
                  <el-input v-model="queryInfo.serialNumber" size="small" clearable></el-input>
                </el-col>
                <el-button type="primary" @click="inquires" style="margin-left:10px"
                  ><el-icon><Search /></el-icon>查询</el-button
                >
                <el-button @click="reset" type="info" plain
                  ><el-icon><Refresh /></el-icon>重置</el-button
                >
              </el-row>
            </div>
            <el-header style="padding:0;">
              <div class="left-panel">
                <el-button type="primary" @click="table_add"
                  ><el-icon><Plus /></el-icon>增加</el-button
                >
                <el-button
                  type="info"
                  :disabled="selection.length == 0"
                  @click="table_edit(selection)"
                >
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-button
                  type="danger"
                  :disabled="selection.length == 0"
                  @click="batch_del"
                >
                  <el-icon><Delete /></el-icon>删除
                </el-button>
                
                <el-upload
                  style="display: inline-block;margin: 0 10px;position: relative;top: 4px;"
                  :headers="headers"
                  :action="url.import"
                  :show-file-list="false"
                  accept=".xls,.xlsx"
                  :onSuccess="handleSuccess"
                  >
                  <el-button type="primary"><el-icon><Download /></el-icon>导入
                  </el-button>
                </el-upload>
                <el-button
                  type="primary"
                  @click="table_export('无人机')"
                >
                  <el-icon><Upload /></el-icon>导出
                </el-button>
                <el-button
                  type="primary"
                  @click="table_export('无人机_导入模板','template')"
                >
                  <el-icon><Download /></el-icon>下载模板
                </el-button>
              </div>
            </el-header>
            <div class="tableBox">
              <el-table
                :data="tableData"
                style="width: 100%; height: 100%"
                class="custom-table"
                @selectionChange="selectionChange"
                @row-click="rowClickChange"
                :row-class-name="tableRowClassName"
                v-loading="loading"
              >
                <el-table-column type="selection" width="50"></el-table-column>
                <el-table-column
                  v-for="(item, index) in columnData"
                  :prop="item.prop"
                  :label="item.label"
                  :key="index"
                  :width="item.width"
                >
                 <template v-if="item.prop == 'status'" #default="scope">
                  {{scope.row.status==1?'正常':scope.row.status==2?'告警':'失联'}}
                </template>
                <template v-else-if="item.prop == 'authStatus'" #default="scope">
                  {{scope.row.authStatus==1?'白名单':scope.row.authStatus==2?'黑名单':scope.row.authStatus==3?'未授权':scope.row.authStatus==4?'待干扰':scope.row.authStatus==5?'待诱骗':'持续跟踪'}}
                </template>
                </el-table-column>
                <!-- <el-table-column fixed="right" label="操作" align="center" width="90">
                  <template #default="scope">
                    <el-button
                      style="color: #409eff !important;"
                      link
                      type="primary"
                      size="small"
                      @click.prevent="setMd(scope.row)"
                    >
                      设置{{scope.row.authStatus == 1 ? '黑名单' : '白名单'}}
                    </el-button>
                    <el-button
                      style="color: #fff"
                      link
                      type="primary"
                      size="small"
                      @click.prevent="pzEvent(scope.row)"
                    >
                      配置
                    </el-button>
                  </template>
                </el-table-column> -->
              </el-table>
            </div>
            <div>
              <el-pagination
                class="pagination"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="pageOption.pageNo"
                :page-size="pageOption.pageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                :page-sizes="[10, 20, 50,100]"
              >
              </el-pagination>
            </div>

          </div>
          <saveDialog v-if="dialog.saveVisible" ref="saveRef" @closed="closedEvent" @successClick="successEvent"></saveDialog>
</template>

<script>
import http from "@/utils/request.js"
import saveDialog from "./save.vue"
import { ElNotification, ElMessageBox, ElMessage } from "element-plus";
import { h } from "vue";
// import pkDialog from "./pkDialog.vue"
export default {
  components: {
    saveDialog
  },
  data() {
    return {
      currentIndex: 0,
      columnData:[
        { prop: "serialNumber", label: "序列号", width: 180 },
        { prop: "brand", label: "品牌" },
        { prop: "model", label: "型号" },
        { prop: "ssdw", label: "所属单位" },
        { prop: "rwlx", label: "任务类型" },
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
        order: "descs",
        column: "cjsj"
      },
      total:0,
      queryInfo:{},
      selection:[],
      dialog:{
        saveVisible:false,
        bindVisible:false,
      },
      loading:false,
      headers: {
        'x-access-token': this.$TOOL.data.get("TOKEN"),
      },
      url:{
        export:'/wrj/wjbdWrjJbxx/exportXls',
        import:`${config.API_URL}/wrj/wjbdWrjJbxx/importExcel`,
      }
    };
  },
  
  created() {
    this.getData()
  },
  methods: {
    setMd(row){
      console.log(row);
    },
    handleSuccess(file){
      if(file.success){
        this.$message.success(file.message)
        this.getData()
      }else{
        this.$message.error(file.message)
      }
    },
    table_export(fileName,type){
      const params={}
      if(type){
        params.type=type;
      }else{
        params.selections=this.selection.map(v=>v.id).join(',')
      }
      http.getPost(`${config.API_URL}`+this.url.export,params).then(res=>{
        if(res.status == 200){
          this.$message.success("操作成功");
          const url = window.URL.createObjectURL(new Blob([res.data],{type: 'application/vnd.ms-excel'}))
          const link = document.createElement('a')
          link.style.display = 'none'
          link.href = url
          // let fileName = res.headers["content-disposition"].split("filename=")[1]
          link.setAttribute('download', fileName+'.xls')
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link); //下载完成移除元素
          window.URL.revokeObjectURL(url); //释放掉blob对象
        }
      })
    },
    //配置
    pzEvent(row){
      this.dialog.bindVisible = true;
      this.$nextTick(() => {
        this.$refs.bindRef.open(row.id)
      })
    },
    //新增
    table_add(){
      this.dialog.saveVisible = true;
      this.$nextTick(() => {
        this.$refs.saveRef.open('add')
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
      this.dialog.saveVisible = true;
      this.$nextTick(() => {
        this.$refs.saveRef.open('edit')
        this.$refs.saveRef.setData(row[0])
      })
    },
    //删除
    async table_del(row) {
      // var reqData = {id: row.id}
      var res = await this.$API.system.user.remove(row.id);
      if (res.code == 200) {
        //这里选择刷新整个表格 OR 插入/编辑现有表格数据
        // this.$refs.table.tableData.splice(index, 1);
        this.inquires();
        this.$message.success("删除成功");
      } else {
        this.$alert(res.message, "提示", {
          type: "error",
        });
      }
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
            ids.push(this.selection[i].id);
          }
          window.API.wrj.delete({ids:ids.join()}).then(res=>{
            if(res.code == 200){
              this.$message.success("操作成功");
              
              this.inquires();
            }
          })
        })
        .catch(() => {});
      // this.$confirm(`确定删除选中的 ${this.selection.length} 项吗？`, "提示", {
      //   type: "warning",
      // }).then(async () => {
      //     for (var i = 0; i < this.selection.length; i++) {
      //       ids.push(this.selection[i].id);
      //     }
      //     window.API.wjbdDxyyPkgl.remove(ids.join()).then(res=>{
      //       if(res.code == 200){
      //          this.$message.success("操作成功");
      //       this.inquires();
      //       }
      //     })
      //   })
      //   .catch(() => {});
    },
    closedEvent(){
      this.dialog.saveVisible = false;
      this.dialog.bindVisible = false;
    },
    successEvent(){
      this.dialog.saveVisible = false;
      this.inquires()
    },
    //查询
    inquires(){
      // const regex = /^[\u4e00-\u9fa5a-zA-Z0-9]{1,20}$/; 
      // if (this.queryInfo.model && !regex.test(this.queryInfo.model)) {
      //   ElMessage.warning('型号不能包含特殊字符，且长度不能超过20位')
      //   return
      // }
      this.pageOption.pageNo = 1;
      this.getData()
    },
    //重置
    reset(){
      this.queryInfo = {}
      this.getData()
    },
    selectionChange(e){
      console.log(e);
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
    leftClick(index){
      this.currentIndex = index;
    },
    getData(){
      this.loading=true;
      const params = JSON.parse(JSON.stringify(this.pageOption)); // Object.assign(this.queryInfo,this.pageOption)
      var arr = [];
      for(var key in this.queryInfo){
        if(this.queryInfo[key]){
          arr.push({
            "rule": "like",
            "type": "input",
            "val": this.queryInfo[key],
            "field": key
          })
        }
      }
      if(arr.length){
        params.superQueryParams = JSON.stringify(arr)
        params.superQueryMatchType = 'and'
      }
      window.API.wrj.list(params).then(res=>{
        if(res.code == 200){
          this.tableData = res.result.records;
          this.total = res.result.total;
        }
        this.loading=false;
      })
    },

  },
};
</script>

<style scoped lang="less">
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
  width: 100px;
  line-height: 33px;
  color:#fff;
}
</style>
