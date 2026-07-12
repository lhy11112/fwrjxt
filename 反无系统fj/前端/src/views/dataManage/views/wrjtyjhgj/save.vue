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
          <el-form-item label="推演计划" prop="tyjhId">
            <el-select v-model="form.tyjhId" :placeholder="'请选择'" clearable filterable>
              <el-option v-for="(item,index) in tyjhOption" :key="index" :label="item.mc" :value="item.id"></el-option>
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="无人机序列号" prop="serial">
            <el-select
              style="width: 100%;"
              filterable
              v-model="form.serial"
              placeholder="请选择"
              @change="wrjChange"
              clearable
            >
              <el-option v-for="(t,i) in wrjDataList" :key="i" :value="t.serialNumber" :label="t.serialNumber"></el-option>
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="机型" prop="model">
            <el-input v-model="form.model" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="无人机位置" prop="zb">
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
                v-model="form.dronLat"
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
          <el-form-item label="无人机经度" prop="dronLng">
            <el-input v-model="form.dronLng" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="无人机纬度" prop="dronLat">
            <el-input v-model="form.dronLat" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col> -->

        <el-col :span="24">
          <el-form-item label="起飞点位置" prop="zb2">
              <!-- <el-input v-model="form.rwdd" :placeholder="'请输入'" clearable> -->
              <el-input
                style="width: 82%; margin-right: 10px"
                v-model="form.zb2"
                :placeholder="'请选择'"
                clearable
                disabled
              >
              </el-input>
              <!-- <el-input
                style="width: 41%; margin-right: 10px"
                v-model="form.homeLat"
                :placeholder="'请选择'"
                clearable
                disabled
              >
              </el-input> -->
              <img
                @click="selectPoint2"
                style="width: 30px"
                src="/static/selectPoint.png"
                alt=""
              />
            </el-form-item>
          </el-col>

        <!-- <el-col :span="12">
          <el-form-item label="起飞点经度" prop="homeLng">
            <el-input v-model="form.homeLng" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="起飞点纬度" prop="homeLat">
            <el-input v-model="form.homeLat" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col> -->

        <el-col :span="12">
          <el-form-item label="频率" prop="freq">
            <el-input v-model="form.freq" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="信号强度" prop="rssi">
            <el-input v-model="form.rssi" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="距离" prop="distance">
            <el-input v-model="form.distance" :placeholder="'请输入'" clearable>
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
  <j-map-point-modal
    v-if="mapDialog2"
    ref="innerMapPointModal2"
    :modal-width="modalWidth"
    :army="false"
    :pointValue="pointValue"
    @ok="handleOK2"
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
       
      },
      rules: {
        tyjhId: [{ required: true, message: "请选择推演计划" }],
        serial: [{ required: true, message: "请选择无人机序列号" }],
        model: [{ required: true, message: "请输入机型" }],
      },
      mode: "add",
      titleMap: {
        add: "新增",
        edit: "编辑",
        show: "查看",
      },
      tyjhOption:[],
      rwData:[],
      wrjDataList: [],
      mapDialog:false,
      mapDialog2:false,
    };
  },
  created() {
    // 无人机数据
    window.API.wrj.list({
      pageNo: 1,
      pageSize: 99
    }).then(res=>{
      if(res.code == 200){
        this.wrjDataList = res.result.records;
      }
    })
    this.getTyjhData()
  },
  methods: {
    // 点击选择坐标点
    selectPoint2() {
      this.mapDialog2 = true;
      this.$nextTick(() => {
        this.$refs.innerMapPointModal2.show();
      });
    },
    handleOK2(item1) {
      this.form.homeLat = this.ToDegrees(item1.split(",")[0]);
      this.form.homeLng = this.ToDegrees(item1.split(",")[1]);
      this.getAddress2(item1.split(",")[1],item1.split(",")[0]);
    },
    // 点击选择坐标点
    selectPoint() {
      this.mapDialog = true;
      this.$nextTick(() => {
        this.$refs.innerMapPointModal.show();
      });
    },
    handleOK(item1) {
      this.form.dronLat = this.ToDegrees(item1.split(",")[0]);
      this.form.dronLng = this.ToDegrees(item1.split(",")[1]);
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
    getAddress2(x,y){
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
            this.form.zb2 = addresses[addresses.length-1]+addresses[addresses.length-2]+addresses[addresses.length-3];
          }else{
            this.form.zb2 = res.result[0].address;
          }
        }else{
          this.form.zb2 = this.ToDegrees(x) + ',' +this.ToDegrees(y);
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
    wrjChange(val){
      var row = this.wrjDataList.filter(row=>{return row.serialNumber == val});
      if(row.length){
        this.form.model = row[0].model
      }
    },
    getTyjhData(){
       window.API.wrjtyjh.list({
        pageNo: 1,
        pageSize: 10000
      }).then(res=>{
        if(res.code == 200){
          this.tyjhOption = res.result.records;
        }
      })
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
      if(this.form.dronLat && this.form.dronLng){
        this.form.dronLat = this.ToDegrees(this.form.dronLat);
        this.form.dronLng = this.ToDegrees(this.form.dronLng);
        // this.form.zb = this.ToDegrees(data.dronLat) + ',' + this.ToDegrees(data.dronLng)
        this.getAddress(data.dronLng,data.dronLat);
       }
       if(this.form.homeLat && this.form.homeLng){
        this.form.homeLat = this.ToDegrees(this.form.homeLat);
        this.form.homeLng = this.ToDegrees(this.form.homeLng);
        // this.form.zb2 = this.ToDegrees(data.homeLat) + ',' + this.ToDegrees(data.homeLng)
        this.getAddress2(data.homeLng,data.homeLat);
       }
    },
    submit() {
      this.$refs.dialogForm.validate(async (valid) => {
        if (valid) {
          let data={}
          data=Object.assign(data,this.form)
          if(data.dronLng){
            data.dronLng=this.DegreeConvertBack(data.dronLng)
          }
          if(data.dronLat){
            data.dronLat=this.DegreeConvertBack(data.dronLat)
          }
          if(data.homeLng){
            data.homeLng=this.DegreeConvertBack(data.homeLng)
          }
          if(data.homeLat){
            data.homeLat=this.DegreeConvertBack(data.homeLat)
          }
          // data.mrcs=JSON.stringify(data.mrcs)
          if (this.mode == "add") {
            window.API.wrjtyjhgj.add(data).then((res) => {
              if (res.code == 200) {
                this.visibleDialog = false;
                this.$message.success("操作成功")
                this.$emit("successClick");
              }
            });
            
          } else if (this.mode == "edit") {

            window.API.wrjtyjhgj.edit(data).then((res) => {
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