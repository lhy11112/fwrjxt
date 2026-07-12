<template>
  <div style="width:100%;height:100%;">
            <div class="top-input">
              <el-row>
                <el-col :span="6">
                  <label>战法名称:</label>
                  <el-input v-model="queryInfo.mc" size="small" clearable></el-input>
                </el-col>
                <el-col :span="6" style="margin-left:10px">
                  <label>分类:</label>
                  <el-select v-model="queryInfo.fl" clearable>
                    <el-option v-for="(item,index) in flOption" :key="index" :label="item.label" :value="item.value"></el-option>
                  </el-select>
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
                  @click="table_export('设备管理')"
                >
                  <el-icon><Upload /></el-icon>导出
                </el-button>
                <el-button
                  type="primary"
                  @click="table_export('设备管理_导入模板','template')"
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
                :show-overflow-tooltip="{effect: 'light'}"
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
                <template v-if="item.prop == 'isValid'" #default="scope">
                  {{scope.row.isValid==1?'正常':'禁用'}}
                </template>
                <template v-else-if="item.prop == 'status'" #default="scope">
                  <div :class="scope.row.status=='CONNECTED'?'color1':scope.row.status=='DISCONNECTED'?'color2':'color3'">{{scope.row.status=='CONNECTED'?'已连接':scope.row.status=='DISCONNECTED'?'未连接':'告警中'}}</div>
                </template>
                <template v-else-if="item.prop == 'deviceType'" #default="scope">
                  <div>{{scope.row.deviceType=='DETECT'?'侦测':scope.row.deviceType=='TRAP'?'诱骗':'干扰'}}</div>
                </template>
                </el-table-column>
                <!-- <el-table-column fixed="right" label="操作" width="60">
                  <template #default="scope">
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
        { prop: "fl", label: "分类", width: 180 },
        { prop: "mc", label: "战法名称" },
        { prop: "zfgs", label: "战法概述" },
        { prop: "zzcj", label: "作战场景" },
        { prop: "jbzf", label: "基本战法" },
        { prop: "llbshtxgj", label: "力量部署和体系构建" },
        { prop: "jtxdff", label: "具体行动方法" },
        { prop: "zfyzqk", label: "战法验证情况" },
        { prop: "zfyyxybwwt", label: "战法应用需要把握的问题" },
      ],
      flOption:[
        {
          label:'执勤',
          value:'执勤'
        },
        {
          label:'处置突发社会安全事件',
          value:'处置突发社会安全事件'
        },
        {
          label:'防范和处置恐怖活动',
          value:'防范和处置恐怖活动'
        },
        {
          label:'防卫作战',
          value:'防卫作战'
        },
        {
          label:'海上维权执法',
          value:'海上维权执法'
        },
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
        export:'/wrj/wjbdWrjFwzf/exportXls',
        import:`${config.API_URL}/wrj/wjbdWrjFwzf/importExcel`,
      }
    };
  },
  
  created() {
    this.getData()
  },
  methods: {
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
          window.API.fwzf.delete({ids:ids.join()}).then(res=>{
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
        // if(this.queryInfo[key] && !(/^[\u4e00-\u9fa5a-zA-Z0-9]{1,100}$/.test(this.queryInfo[key]))){
        //   ElMessage.warning("查询内容不能包含特殊字符");
        //   return;
        // }
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
      window.API.fwzf.list(params).then(res=>{
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
.color1{
          color: #09eb09;
        }
        .color2{
          color: red;;
        }
        .color3{
          color:yellow;
        }
</style>
