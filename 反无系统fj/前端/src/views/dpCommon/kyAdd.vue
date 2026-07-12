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
    :title="titleMap[mode]"
    style="width: 40%;height:40vh"
    :visibleDialog="visibleDialog"
    @close="closed"
    isModal="true"
  >
    <el-form
      :model="form"
      :rules="rules"
      :disabled="mode === 'show'"
      ref="dialogForm"
      label-width="120px"
      style="height: calc(100% - 40px);overflow: auto;"
    >
      <el-row>

         <el-col :span="12">
          <el-form-item label="空域名称" prop="mc">
            <el-input v-model="form.mc" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <!-- <template #footer> -->
      <div class="create-bottom">
      
        <el-button @click="closed">取 消</el-button>

        <el-button :loading="isSaveing" type="primary" @click="submit()"
          >保 存</el-button
        >
      </div>
    <!-- </template> -->
  <!-- </el-dialog> -->
  </AnalysisMoveDlg>
  <j-map-point-modal
    v-if="mapDialog"
    ref="innerMapPointModal"
    :modal-width="modalWidth"
    :army="false"
    :pointValue="pointValue"
    @ok="handleOK"
    @handleCancel="handleCancel"
  />
</template>

<script>
import JMapPointModal from "@/components/wMapPoint/JmapPointModals";
import { checkSpaceTss} from "@/utils/index.js"
export default {
  props:{
    toDp:{
      type:Boolean,
      default:false
    }
  },
  components:{
    JMapPointModal
  },
  data() {
    return {
      mapDialog: false,
      visibleDialog: false,
      form: {
       ys:'#008000'
      },
      rules: {
        mc: [{ required: true, message: "请输入" }],
        lc: [{ required: true, message: "请输入" }],
        xz: [{ required: true, message: "请输入" }],
      },
      mode: "add",
      titleMap: {
        add: "新增",
        edit: "编辑",
        show: "查看",
      },
      rwData:[],
      xzOption:[
        {
          label:"圆形",
          value:"circle"
        },
        {
          label:"矩形",
          value:"rectangle"
        },
        {
          label:"多边形",
          value:"polygon"
        },
      ],
      lxOption:[
        {
          label:"限制区",
          value:"restricted"
        },
        // {
        //   label:"禁飞区",
        //   value:"prohibited"
        // },
        // {
        //   label:"预警区",
        //   value:"danger"
        // },
        // {
        //   label:"允许飞行区域",
        //   value:"allAir"
        // },
      ],
    };
  },
  created() {
    
  },
  methods: {
    // 点击选择坐标点
    selectPoint() {
      this.mapDialog = true;
      this.$nextTick(() => {
        this.$refs.innerMapPointModal.show();
      });
    },
    handleOK(item1) {
      this.form.zxdwd = this.ToDegrees(item1.split(",")[0]);
      this.form.zxdjd = this.ToDegrees(item1.split(",")[1]);
      this.form.zb = this.ToDegrees(item1.split(",")[0]) + ',' +this.ToDegrees(item1.split(",")[1]);
    },
    // 经纬度转度分秒
      ToDegrees(val) {
        if (typeof val == 'undefined' || val == '') {
          return ''
        }
        // 把num类型转换成为string
        val = val + ''
        let i = val.indexOf('.')
        var strDu = i < 0 ? val : val.substring(0, i)
        let strFen = 0
        let strMiao = 0
        if (i > 0) {
          strFen = '0' + val.substring(i)
          strFen = Number(strFen) * 60 + ''
          i = strFen.indexOf('.')
          if (i > 0) {
            strMiao = '0' + strFen.substring(i)
            strFen = strFen.substring(0, i)
            strMiao = Number(strMiao ) * 60 + ''
            i = strMiao.indexOf('.')
            strMiao = strMiao.substring(0, i + 4)
            strMiao = parseFloat(strMiao).toFixed(2)
          }
        }
        return strDu + '°' + strFen + '′' + strMiao + '″'
      },
      /**
       * 度分秒转经纬度
       */
      DegreeConvertBack(value, len) {
        len = len > 6 || typeof len == 'undefined' ? 6 : len
        if (value == '') {
          return value
        }
        var du = parseFloat(value.split('°')[0])
        var fen = parseFloat(value.split('°')[1].split('′')[0]) / 60
        var miao = parseFloat(value.split('°')[1].split('′')[1].split('″')[0]) / 3600
        var digital = du + fen + miao
        return digital.toFixed(len)
      },
    selectColor (){
      this.$ref.brightColorRef.show();
    },
    getRwData(){
      window.API.model.wjfkyw.ztLists({
        pageNo: 1,
        pageSize: 1000,
        levels: '',
        dxyy:1
      }).then(res=>{
        if(res.code==200){
          this.rwData = res.result.records;
        }
      })
    },
    open(item) {
      this.mode = item;
      this.visibleDialog = true;
      return this;
    },
    closed(){
      this.visibleDialog = false;
      this.$emit('closed')
    },
    setData(data) {
      this.form = Object.assign(this.form, data);
      if(this.form.zxdwd && this.form.zxdjd){
        this.form.zxdwd = this.ToDegrees(this.form.zxdwd);
        this.form.zxdjd = this.ToDegrees(this.form.zxdjd);
        this.form.zb = this.ToDegrees(data.zxdwd) + ',' + this.ToDegrees(data.zxdjd)
       }
    },
    submit() {
      this.$refs.dialogForm.validate(async (valid) => {
        if (valid) {
          let data={}
          data=Object.assign(data,this.form)
          if(data.zxdjd){
            data.zxdjd=this.DegreeConvertBack(data.zxdjd)
          }
          if(data.zxdwd){
            data.zxdwd=this.DegreeConvertBack(data.zxdwd)
          }
          data.lx = "restricted";
          data.xz = "circle";
          data.zxgd = 30;
          data.zdgd = 90;
          data.ys="#008000";
          // data.mrcs=JSON.stringify(data.mrcs)
          if (this.mode == "add") {
            window.API.wrjky.add(data).then((res) => {
              if (res.code == 200) {
                this.visibleDialog = false;
                this.$message.success("操作成功")
                this.$emit("successClick");
              }
            });
            
          } else if (this.mode == "edit") {

            window.API.wrjky.edit(data).then((res) => {
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
.color-select {
  flex: 1;
  border: 1px solid #dddddd90;
  height: 32px;
  border-radius: 3px;
  display: flex;
  align-items: center;
  color: #ffffff;
  padding: 0px 10px;
  box-sizing: border-box;

  .color-line {
    width: 110px;
    height: 15px;
    border-radius: 3px;
    margin-right: 10px;
  }
  .color-text {
    flex: 1;
    font-size: 14px;
  }
  .color-icon {
    width: 40px;
    display: flex;
    align-items: center;
    justify-content: flex-end;
  }
}
:deep(.el-input.is-disabled .el-input__wrapper){
  background-color: rgba(54, 108, 161, 0.1);
}
</style>