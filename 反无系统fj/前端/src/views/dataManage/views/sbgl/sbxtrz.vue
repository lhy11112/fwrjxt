<template>
  <!-- <el-dialog
    :title="titleMap[mode]"
    v-model="visibleDialog"
    width="51%"
    style="height: 60%; margin-top: 10%"
    destroy-on-close
    close-on-click-modal
    @close="closed"
  > -->
  <AnalysisMoveDlg
    title="设备心跳日志"
    style="width: 60%;height:60vh"
    :visibleDialog="visibleDialog"
    @close="closed"
    isModal="true"
  >
  <div class="top-input">
              <el-row v-if="row.deviceType == 'System'">
                <el-col :span="6">
                  <label>授权状态:</label>
                  <el-select v-model="queryInfo.authNormal" size="small" clearable>
                    <el-option v-for="(item,index) in authNormalOption" :key="index" :label="item.label" :value="item.value"></el-option>
                  </el-select>
                </el-col>
                
                <el-button type="primary" @click="inquires" style="margin-left:10px"
                  ><el-icon><Search /></el-icon>查询</el-button
                >
                <el-button @click="reset" type="info" plain
                  ><el-icon><Refresh /></el-icon>重置</el-button
                >
              </el-row>
              <el-row v-else>
                <el-col :span="6">
                  <label>主板模块:</label>
                  <el-select v-model="queryInfo.mainCard" size="small" clearable>
                    <el-option v-for="(item,index) in mainCardOption" :key="index" :label="item.label" :value="item.value"></el-option>
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
                    <template v-if="item.prop == 'mainCard'" #default="scope">
                        <div>{{scope.row.mainCard==1?'异常':'正常'}}</div>
                    </template>
                    <template v-else-if="item.prop == 'compass'" #default="scope">
                        <div>{{scope.row.compass==1?'异常':'正常'}}</div>
                    </template>
                    <template v-else-if="item.prop == 'workState'" #default="scope">
                        <div>{{scope.row.workState==1?'告警':'不告警'}}</div>
                    </template>
                     <!-- 授权状态 -->
                    <template v-else-if="item.prop == 'authNormal'" #default="scope">
                      <div>{{scope.row.authNormal==1?'正常':'未授权'}}</div>
                    </template>

                    <!-- 探测功能 -->
                    <template v-else-if="item.prop == 'detectEnabled'" #default="scope">
                      <div>{{scope.row.detectEnabled==1?'开启':'停止'}}</div>
                    </template>

                    <!-- 反制功能 -->
                    <template v-else-if="item.prop == 'counterEnabled'" #default="scope">
                      <div>{{scope.row.counterEnabled==1?'已开启':'未开启'}}</div>
                    </template>

                    <!-- 探测设备 -->
                    <template v-else-if="item.prop == 'detectorOnline'" #default="scope">
                      <div>{{scope.row.detectorOnline==1?'正常':'异常'}}</div>
                    </template>

                    <!-- 反制设备 -->
                    <template v-else-if="item.prop == 'counterOnline'" #default="scope">
                      <div>{{scope.row.counterOnline==1?'正常':'异常'}}</div>
                    </template>

                    <!-- 无人值守 -->
                    <template v-else-if="item.prop == 'unattendedMode'" #default="scope">
                      <div>{{scope.row.unattendedMode==1?'开启':'关闭'}}</div>
                    </template>

                    <!-- 诱骗设备 -->
                    <template v-else-if="item.prop == 'deceiverOnline'" #default="scope">
                      <div>{{scope.row.deceiverOnline==1?'正常':'异常'}}</div>
                    </template>

                    <!-- 干扰模式 -->
                    <template v-else-if="item.prop == 'jammingMode'" #default="scope">
                      <div>{{scope.row.jammingMode==1?'迫降':'返航'}}</div>
                    </template>

                    <!-- 5.8G频段 -->
                    <template v-else-if="item.prop == 'band58g'" #default="scope">
                      <div>{{scope.row.band58g==1?'开启':'关闭'}}</div>
                    </template>

                    <!-- 2.4G频段 -->
                    <template v-else-if="item.prop == 'band24g'" #default="scope">
                      <div>{{scope.row.band24g==1?'开启':'关闭'}}</div>
                    </template>

                    <!-- 900M频段 -->
                    <template v-else-if="item.prop == 'band900m'" #default="scope">
                      <div>{{scope.row.band900m==1?'开启':'关闭'}}</div>
                    </template>

                    <!-- 1.4G频段 -->
                    <template v-else-if="item.prop == 'band14g'" #default="scope">
                      <div>{{scope.row.band14g==1?'开启':'关闭'}}</div>
                    </template>

                    <!-- 5.2G频段 -->
                    <template v-else-if="item.prop == 'band52g'" #default="scope">
                      <div>{{scope.row.band52g==1?'开启':'关闭'}}</div>
                    </template>

                    <!-- GNSS联动状态 -->
                    <template v-else-if="item.prop == 'gnssLinkStatus'" #default="scope">
                      <div>{{scope.row.gnssLinkStatus==1?'开启':'关闭'}}</div>
                    </template>

                    <!-- 系统诱骗状态（多值枚举） -->
                    <template v-else-if="item.prop == 'deceptionStatus'" #default="scope">
                      <div>
                        {{ 
                          scope.row.deceptionStatus === 0x0000 ? '空闲' :
                          scope.row.deceptionStatus === 0x0001 ? '驱离' :
                          scope.row.deceptionStatus === 0x0002 ? '迫降' :
                          scope.row.deceptionStatus === 0x0003 ? '导航压制' : ''
                        }}
                      </div>
                    </template>

                    <!-- GNSS诱骗模式（多值枚举） -->
                    <template v-else-if="item.prop == 'gnssDeceptionMode'" #default="scope">
                      <div>
                        {{ 
                          scope.row.gnssDeceptionMode === 0x0000 ? '定向驱逐' :
                          scope.row.gnssDeceptionMode === 0x0001 ? '定点迫降' :
                          scope.row.gnssDeceptionMode === 0x0002 ? '禁飞' :
                          scope.row.gnssDeceptionMode === 0x0003 ? '导航压制' : ''
                        }}
                      </div>
                    </template>

                    <!-- GNSS诱导方式 -->
                    <template v-else-if="item.prop == 'gnssInduceMode'" #default="scope">
                      <div>{{scope.row.gnssInduceMode==1?'拉近':'驱离'}}</div>
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
      czColumnData:[],
      pageOption:{
        pageNo:1,
        pageSize:10,
        order: "descs",
        column: "dataTime"
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
      mapDialog:false,
      mainCardOption:[
        {
            label:'正常',
            value:'0'
        },
        {
            label:'异常',
            value:'1'
        },
      ],
      authNormalOption:[
        {
            label:'未授权',
            value:'0'
        },
        {
            label:'正常',
            value:'1'
        },
      ],
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
      
      this.row = data;
      if(this.row.deviceType == "System"){
        this.pageOption.column = "createTime"
        this.czColumnData = [
            { prop: "authNormal", label: "授权状态" },
            { prop: "detectEnabled", label: "探测功能" },
            { prop: "counterEnabled", label: "反制功能" },
            { prop: "detectorOnline", label: "探测设备" },
            { prop: "counterOnline", label: "反制设备" },
            { prop: "unattendedMode", label: "无人值守" },
            { prop: "deceiverOnline", label: "诱骗设备" },
            { prop: "jammingMode", label: "干扰模式" },
            { prop: "band58g", label: "5.8G频段" },
            { prop: "band24g", label: "2.4G频段" },
            { prop: "band900m", label: "900M频段" },
            { prop: "band14g", label: "1.4G频段" },
            { prop: "band52g", label: "5.2G频段" },
            { prop: "ptzControlMode", label: "云台控制模式" },
            { prop: "attackCountdown", label: "攻击倒计时" },
            { prop: "deceptionStatus", label: "系统诱骗状态" },
            { prop: "gnssLinkStatus", label: "GNSS联动状态" },
            { prop: "gnssDeceptionMode", label: "GNSS诱骗模式" },
            { prop: "gnssInduceMode", label: "GNSS诱导方式" },
            { prop: "noFlyLat", label: "禁飞区纬度" },
            { prop: "noFlyLng", label: "禁飞区经度" },
            { prop: "noFlyAlt", label: "禁飞区海拔" },
            { prop: "forcedLandLat", label: "定点迫降区纬度" },
            { prop: "forcedLandLng", label: "定点迫降区经度" },
            { prop: "forcedLandAlt", label: "定点迫降区海拔" },
            { prop: "forcedLandRadius", label: "定点迫降区半径" },
            { prop: "checksum", label: "校验和" },
            { prop: "createTime", label: "创建时间" },
            { prop: "updateTime", label: "更新时间" },
            { prop: "stationId", label: "站ID" }
        ]
      }else{
        this.pageOption.column = "dataTime"
        this.czColumnData = [
            { prop: "mainCard", label: "主板模块",width:100 },
            { prop: "trapCard", label: "诱骗模块",width:100 },
            { prop: "compass", label: "电子罗盘",width:100 },
            { prop: "disturbCard", label: "干扰模块",width:100 },
            { prop: "longitude", label: "经度（度）",width:120 },
            { prop: "latitude", label: "纬度（度）",width:120 },
            { prop: "altitude", label: "海拔（米）",width:120 },
            { prop: "angle", label: "罗盘方位（度）" ,width:120},
            { prop: "cpuRate", label: "CPU使用率（%）",width:120 },
            { prop: "diskUsage", label: "硬盘已用空间（MB）",width:120 },
            { prop: "cardTemp", label: "板卡温度（度）",width:120 },
            { prop: "ampTemp", label: "功放温度（度）",width:120 },
            { prop: "dataTime", label: "数据时间戳",width:160 },
            { prop: "createTime", label: "入库时间" ,width:160},
            { prop: "workState", label: "设备工作状态" },
        ]
      }
      this.visibleDialog = true;
      this.getData()
    },
    async getData(){
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
      let res=null;
      if(this.row.deviceType == "System"){
        res = await window.API.sbgl.uavActiveHeartbeatList(params)
      }else{
        res = await window.API.sbgl.uavDeviceHeartbeatList(params)
      }
      // window.API.sbgl.uavDeviceHeartbeatList(params).then(res=>{
        if(res.code == 200){
          this.tableData = res.result.records;
          this.total = res.result.total;
        }
        this.loading=false;
      // })
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