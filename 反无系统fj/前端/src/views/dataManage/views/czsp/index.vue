<template>
  <div style="width:100%;height:100%;">
            <div class="top-input">
              <el-row>
                <el-col :span="6">
                  <label>视频分类:</label>
                  <el-select v-model="queryInfo.spfl" filterable  allow-create clearable>
                    <el-option v-for="(item,index) in spflOptions" :key="index" :label="item.label" :value="item.value"></el-option>
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
                
                <!-- <el-upload
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
                  @click="table_export('敌方人员')"
                >
                  <el-icon><Upload /></el-icon>导出
                </el-button>
                <el-button
                  type="primary"
                  @click="table_export('敌方人员_导入模板','template')"
                >
                  <el-icon><Download /></el-icon>下载模板
                </el-button> -->
              </div>
            </el-header>
            <div class="tableBox">
              <!-- <el-table
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
                </el-table-column>
                
              </el-table> -->
              <template v-if="tableData && tableData.length>0">
                <div
                  class="modelOne"
                  v-for="(item, index) in tableData"
                  :class="{ modelList: spanIndex.indexOf(index) > -1 }"
                  @click="clickCurrent(item,index)"
                  :key="index"
                >
                  <div class="contentTop">
                    <video controls style="width: 100%;height:150px;cursor: pointer;" :src="wjUrlHeader+item.splj" alt=""></video>
                  </div>
                  <div style="display:flex;justify-content: space-between;">
                    <span>{{ item.mc }}</span>
                    <span>{{ item.spfl }}</span>
                  </div>
                </div>
              </template>
              
              <el-empty style="width:100%" v-else description="暂无数据" image-size="120" />
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
      currentModelLick:null,
      wjUrlHeader:window.config.VUE_APP_API_BASE_URL + window.config.API_URL + "/sys/common/static/",
      currentIndex: 0,
      columnData:[
        { prop: "spfl", label: "视频分类" },
        { prop: "mc", label: "名称" },
        { prop: "splj", label: "视频路径" }
        
      ],
      spflOptions:[
        {
          label:"培训视频",
          value:"培训视频"
        },
        {
          label:"操作视频",
          value:"操作视频"
        }
      ],
      tableData:[],
      spanIndex:[],
      strs:[],
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
        export:'/wrj/wjbdWrjCzsp/exportXls',
        import:`${config.API_URL}/wrj/wjbdWrjCzsp/importExcel`,
      }
    };
  },
  
  created() {
    this.getData()
  },
  methods: {
    // 点击模型内容
    clickCurrent(v,i) {
      let arrIndex = this.spanIndex.indexOf(i);
      if (arrIndex > -1) {
        this.spanIndex.splice(arrIndex, 1);
        this.strs.splice(arrIndex, 1);
      } else {
        this.spanIndex.push(i);
        this.strs.push(v);
      }
      this.selectionChange(this.strs)
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
          window.API.czsp.delete({ids:ids.join()}).then(res=>{
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
      window.API.czsp.list(params).then(res=>{
        if(res.code == 200){
          this.tableData = res.result.records;
          console.log(this.tableData);
          this.total = res.result.total;
        }
        this.loading=false;
      })
    },

  },
};
</script>

<style scoped lang="less">
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
.tableBox {
    width: 100%;
    height:calc(100% - 155px);
    display: flex;
    justify-content: start;
    flex-wrap: wrap;
    overflow: auto;
    // flex-direction: column;
    &::-webkit-scrollbar {
      display: none !important;
    }

    .modelList {
      border: 1px solid #0c8ced !important;
    }
    .modelOne:hover {
      //    transform: scale(1.05);
      //  transition: 0.5s;
    }
    .modelOne:nth-child(4n) {
      margin-right: 0;
    }
    .modelOne {
      width: 24.5%;
      height: 200px;
      margin-top: 20px;
      padding: 10px;
      box-sizing: border-box;
      position: relative;
      flex-shrink: 0;
      margin-right: 10px;
      box-shadow: 0 0 0 1px rgba(219,197,197, 0.35) inset;
      color: #fff;

      .contentTop {
        display: flex;
        width: 100%;
        .top {
          width: 15%;
          height: 15%;
        }
        .contentMiddle {
          display: flex;
          flex-direction: column;
          margin-left: 10px;
          color: #fff;
          span:nth-child(1) {
            font-size: 18px;
            font-weight: bold;
            // color:var(--el-color-primary-light-2);
          }
          span:nth-child(2) {
            font-size: 14px;
            margin-top: 5px;
          }
        }
        .isNoStauts {
          height: 30px;
          display: flex;
          align-items: center;
          padding: 0px 15px;
          color: #fff;
          background: rgb(255, 153, 12);
          border: 1px solid rgb(255, 203, 131);
          border-radius: 25px;
          font-size: 15px;
        }
        .NoStauts {
          height: 30px;
          display: flex;
          align-items: center;
          padding: 0px 15px;
          color: rgb(114, 118, 124);
          background: rgb(58, 65, 72);
          border: 1px solid rgb(109, 114, 120);
          border-radius: 25px;
          font-size: 15px;
        }
      }
      .contentAuth {
        display: flex;
        color: #fff;
        width: 100%;
        margin-top: 10px;
        span {
          width: 50%;
        }
      }
      .contentAuthOne {
        display: flex;
        color: #fff;
        width: 100%;
        margin-top: 10px;
        span {
          width: 100%;
        }
      }
      .contentAuthtwo {
        color: #fff;
        width: 100%;
        margin-top: 10px;
        // overflow: hidden;
        // text-overflow: ellipsis;
        // white-space: nowrap;
        display: -webkit-box;
        -webkit-line-clamp: 6;
        -webkit-box-orient: vertical;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      .contentAuthtHree {
        color: #fff;
        width: 100%;
        // margin-top: 10px;
        display: flex;
        justify-content: flex-end;
        position: absolute;
        right: 0;
        bottom: 20px;
        padding-right: 10px;
        div {
          display: flex;
          align-items: center;
          img {
            width: 20px;
            height: 20px;
            margin-right: 10px;
          }
          .special {
            color: rgb(226, 132, 9);
          }
          .quxiao {
            color: rgba(161, 164, 169);
          }
        }
      }
    }
  }
</style>
