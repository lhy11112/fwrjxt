<template>
  <!-- <el-dialog
    title="设备操作日志"
    v-model="visibleDialog"
    width="51%"
    style="height: 60%; margin-top: 10%"
    destroy-on-close
    close-on-click-modal
    @close="closed"
  > -->
  <AnalysisMoveDlg
    title="设备操作日志"
    style="width: 60%;height:60vh"
    :visibleDialog="visibleDialog"
    @close="closed"
    isModal="true"
  >
  <div class="top-input">
              <el-row>
                <el-col :span="6">
                  <label>命令名称:</label>
                  <el-input v-model="queryInfo.cmdName" size="small" clearable></el-input>
                </el-col>
                <el-button type="primary" @click="inquires" style="margin-left:10px"
                  ><el-icon><Search /></el-icon>查询</el-button
                >
                <el-button @click="reset" type="info" plain
                  ><el-icon><Refresh /></el-icon>重置</el-button
                >
              </el-row>
            </div>
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
                <el-table-column
                  v-for="(item, index) in czColumnData"
                  :prop="item.prop"
                  :label="item.label"
                  :key="index"
                  :width="item.width"
                >
                
                <template v-if="item.prop == 'deviceType'" #default="scope">
                  <div>{{nodeTypeFilter(scope.row.deviceType,deviceTypeOption)}}</div>
                </template>
                </el-table-column>
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

      
  </AnalysisMoveDlg>
</template>

<script>
export default {
  props:{
    toDp:{
      type:Boolean,
      default:false
    }
  },
  data() {
    return {
        czColumnData:[
        // { prop: "stationId", label: "站IsD",width:100 },
        // { prop: "deviceType", label: "设备类型" },
        { prop: "cmdType", label: "命令类型" },
        { prop: "cmdName", label: "命令名称" },
        { prop: "cmdParam", label: "命令参数" },
        { prop: "result", label: "执行结果" },
        { prop: "operateTime", label: "操作时间",width:180 },
      ],
      pageOption:{
        pageNo:1,
        pageSize:10,
        order: "descs",
        column: "operateTime"
      },
      queryInfo:{},
      mapDialog: false,
      visibleDialog: false,
      form: {
       
      },
      rules: {
        
      },
      deviceTypeOption:[
        {
          label:'侦测',
          value:'DETECT'
        },
        {
          label:'诱骗',
          value:'TRAP'
        },
        {
          label:'干扰',
          value:'DISTURB'
        },
        {
          label:'系统',
          value:'System'
        },
      ],
      mode: "add",
      titleMap: {
        add: "新增",
        edit: "编辑",
        show: "查看",
      },
      tableData:[],
      total:0,
      mapDialog:false
    };
  },
  created() {
    
  },
  methods: {
    nodeTypeFilter(val,data){
      const values = data.find(item => item.value == val)
      if (values?.label)  return values.label
      else return val
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
    open(data) {
      this.visibleDialog = true;
      this.row = data;
      this.getData()
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
      params.stationId = this.row.stationId;
      window.API.sbgl.uavOperateLogList(params).then(res=>{
        if(res.code == 200){
          this.tableData = res.result.records;
          this.total = res.result.total;
        }
        this.loading=false;
      })
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
    closed(){
      this.visibleDialog = false;
      this.$emit('closed')
    },
    setData(data) {
      this.form = Object.assign(this.form, data);
      if(this.form.wd && this.form.jd){
        this.form.wd = this.ToDegrees(this.form.wd);
        this.form.jd = this.ToDegrees(this.form.jd);
        this.form.zb = this.ToDegrees(data.wd) + ',' + this.ToDegrees(data.jd)
       }
    },
    submit() {
      this.$refs.dialogForm.validate(async (valid) => {
        if (valid) {
          let data={}
          data=Object.assign(data,this.form)
          if(data.jd){
            data.jd=this.DegreeConvertBack(data.jd)
          }
          if(data.wd){
            data.wd=this.DegreeConvertBack(data.wd)
          }
          // data.mrcs=JSON.stringify(data.mrcs)
          if (this.mode == "add") {
            window.API.sbgl.add(data).then((res) => {
              if (res.code == 200) {
                this.visibleDialog = false;
                this.$message.success("操作成功")
                this.$emit("successClick");
              }
            });
            
          } else if (this.mode == "edit") {

            window.API.sbgl.edit(data).then((res) => {
              if (res.code == 200) {
                this.visibleDialog = false;
                this.$message.success("操作成功")
                this.$emit("successClick");
              }
            });
          }
        }
      });
    },
  },
};
</script>

<style scoped lang="less">
*{
  box-sizing: border-box;
}
.tableBox{
  height:calc(100% - 100px)
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

// :deep(.el-form-item__label) {
//   color: #000 !important;
// }
.upload-demo {
  width: 100% !important;
}
.el-select-dropdown__item.is-selected{
  color:var(--el-color-primary) !important;
}
.create-bottom {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
}
:deep(.el-select){
  height:32px !important;
}
:deep(.el-select__wrapper){
  height:32px !important;
}
:deep(.el-input.is-disabled .el-input__wrapper){
  background-color: rgba(54, 108, 161, 0.1);
}
</style>