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
    style="width: 45%;height:60vh"
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
          <el-form-item label="序列号" prop="serialNumber">
            <el-input v-model="form.serialNumber" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="品牌" prop="brand">
            <el-input v-model="form.brand" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="型号" prop="model">
            <el-input v-model="form.model" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="所属单位" prop="ssdw">
            <el-input v-model="form.ssdw" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="任务类型" prop="rwlx">
            <el-input v-model="form.rwlx" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

         <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" :placeholder="'请选择'" clearable>
              <el-option v-for="(item,index) in statusOption" :key="index" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
        </el-col>


        <el-col :span="12">
          <el-form-item label="授权状态" prop="authStatus">
            <el-select v-model="form.authStatus" :placeholder="'请输入'" clearable>
              <el-option v-for="(item,index) in authStatusOption" :key="index" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="当前位置" prop="zb">
              <!-- <el-input v-model="form.rwdd" :placeholder="'请输入'" clearable> -->
              <el-input
                style="width: 82%; margin-right: 10px"
                v-model="form.zb"
                :placeholder="'请选择'"
                clearable
                disabled
              >
              </el-input>
              <!-- <el-input
                style="width: 41%; margin-right: 10px"
                v-model="form.currentLatitude"
                :placeholder="'请选择'"
                clearable
                disabled
              >
              </el-input> -->
              <img
                @click="selectPoint"
                style="width: 30px"
                src="/static/selectPoint.png"
                alt=""
              />
            </el-form-item>
          </el-col>

         <!-- <el-col :span="12">
          <el-form-item label="当前经度" prop="currentLongitude">
            <el-input v-model="form.currentLongitude" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

         <el-col :span="12">
          <el-form-item label="当前纬度" prop="currentLatitude">
            <el-input v-model="form.currentLatitude" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col> -->
        
         <el-col :span="12">
          <el-form-item label="当前高度" prop="currentAltitude">
            <el-input v-model="form.currentAltitude" :placeholder="'请输入'" clearable>
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
      visibleDialog: false,
      form: {
        authStatus: 3
      },
      rules: {
        serialNumber: [{ required: true, message: "请输入序列号" }],
        brand:[{ required: true, message: "请输入品牌" }],
        model: [{ required: true, message: "请输入型号" }],
        status: [{ required: true, message: "请选择状态" }],
      },
      mode: "add",
      titleMap: {
        add: "新增",
        edit: "编辑",
        show: "查看",
      },
      
      statusOption:[
        {
          label:'正常',
          value:1
        },
        {
          label:'告警',
          value:2
        },
        {
          label:'失联',
          value:3
        },
      ],
      authStatusOption:[
        {
          label:'白名单',
          value:1
        },
        {
          label:'黑名单',
          value:2
        },
        {
          label:'未授权',
          value:3
        },
        {
          label:'待干扰',
          value:4
        },
        {
          label:'待诱骗',
          value:5
        },
        {
          label:'持续跟踪',
          value:6
        },
      ],
      rwData:[],
      mapDialog:false
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
      this.form.currentLatitude = this.ToDegrees(item1.split(",")[0]);
      this.form.currentLongitude = this.ToDegrees(item1.split(",")[1]);
      this.getAddress(item1.split(",")[1],item1.split(",")[0]);
    },
    getAddress(x,y){
      const geoCodeParam = new SuperMap.GeoDecodingParameter({
                  x: x,
                  y:y,
                  geoDecodingRadius:-1,
                  fromIndex: 0,
                  toIndex: 20,
      })
      const addressMatchService = L.supermap.addressMatchService(
        window.config.VUE_APP_SUPERMAP_BASE_URL+'/iserver/services/addressMatch-Index/restjsr/v1/address'
      )
      addressMatchService.decode(geoCodeParam, (res)=>{
        if(res.result && res.result.length>0){
          const addresses = res.result[0].addresses;
          if(addresses.length && addresses.length==3){
            this.form.zb = addresses[addresses.length-1]+addresses[addresses.length-2]+addresses[addresses.length-3];
          }else{
            this.form.zb = res.result[0].address;
          }
        }else{
          this.form.zb = this.ToDegrees(x) + ',' +this.ToDegrees(y);
        }
      })
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
    open(item) {
      this.mode = item;
      this.visibleDialog = true;
    },
    closed(){
      this.visibleDialog = false;
      this.$emit('closed')
    },
    setData(data) {
      this.form = Object.assign(this.form, data);
      if(this.form.currentLatitude && this.form.currentLongitude){
        this.form.currentLatitude = this.ToDegrees(this.form.currentLatitude);
        this.form.currentLongitude = this.ToDegrees(this.form.currentLongitude);
        this.getAddress(data.currentLongitude,data.currentLatitude);
       }
    },
    submit() {
      this.$refs.dialogForm.validate(async (valid) => {
        if (valid) {
          let data={}
          data=Object.assign(data,this.form)
          if(data.currentLongitude){
            data.currentLongitude=this.DegreeConvertBack(data.currentLongitude)
          }
          if(data.currentLatitude){
            data.currentLatitude=this.DegreeConvertBack(data.currentLatitude)
          }
          // data.mrcs=JSON.stringify(data.mrcs)
          if (this.mode == "add") {
            window.API.wrj.add(data).then((res) => {
              if (res.code == 200) {
                this.visibleDialog = false;
                this.$message.success("操作成功")
                this.$emit("successClick");
              }
            });
            
          } else if (this.mode == "edit") {

            window.API.wrj.edit(data).then((res) => {
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
:deep(.el-input.is-disabled .el-input__wrapper){
  background-color: rgba(54, 108, 161, 0.1);
}
</style>