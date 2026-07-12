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
          <el-form-item label="空域名称" prop="mc">
            <el-input v-model="form.mc" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="空域类型" prop="lx">
            <el-select v-model="form.lx" :placeholder="'请输入'" clearable>
              <el-option v-for="(item,index) in lxOption" :key="index" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="区域形状" prop="xz">
            <el-select v-model="form.xz" :placeholder="'请选择'" clearable>
              <el-option v-for="(item,index) in xzOption" :key="index" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
        </el-col>

        
        <el-col :span="24">
          <el-form-item label="中心点坐标" prop="zb">
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
                v-model="form.zxdwd"
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
          <el-form-item label="中心点经度" prop="zxdjd">
            <el-input v-model="form.zxdjd" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="中心点纬度" prop="zxdwd">
            <el-input v-model="form.zxdwd" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col> -->

        <el-col :span="12">
          <el-form-item label="最小高度(米)" prop="zxgd">
            <el-input v-model="form.zxgd" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="最大高度(米)" prop="zdgd">
            <el-input v-model="form.zdgd" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="预警圈半径(米)" prop="bj">
            <el-input v-model="form.bj" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="预警圈颜色" prop="ys">
            <el-color-picker ref="brightColorRef" v-model="form.ys" />
            <!-- <div class="color-select" @click="selectColor()">
              <div :style="`background: ${form.ys}`" class="color-line"></div>
              <el-color-picker ref="brightColorRef" v-model="form.ys" />
              <div class="color-text">{{ form.ys }}</div>
              <div class="color-icon">
                <el-icon><ArrowDownBold /></el-icon>
              </div>
            </div> -->
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="反制圈半径(米)" prop="jfqbj">
            <el-input v-model="form.jfqbj" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="反制圈颜色" prop="jfqys">
            <el-color-picker ref="brightColorRef" v-model="form.jfqys" />
            <!-- <div class="color-select" @click="selectColor()">
              <div :style="`background: ${form.ys}`" class="color-line"></div>
              <el-color-picker ref="brightColorRef" v-model="form.ys" />
              <div class="color-text">{{ form.ys }}</div>
              <div class="color-icon">
                <el-icon><ArrowDownBold /></el-icon>
              </div>
            </div> -->
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="警戒圈半径(米)" prop="yjqbj">
            <el-input v-model="form.yjqbj" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="警戒圈颜色" prop="yjqys">
            <el-color-picker ref="brightColorRef" v-model="form.yjqys" />
            <!-- <div class="color-select" @click="selectColor()">
              <div :style="`background: ${form.ys}`" class="color-line"></div>
              <el-color-picker ref="brightColorRef" v-model="form.ys" />
              <div class="color-text">{{ form.ys }}</div>
              <div class="color-icon">
                <el-icon><ArrowDownBold /></el-icon>
              </div>
            </div> -->
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="备注" prop="bz">
            <el-input type="textarea" v-model="form.bz" :placeholder="'请输入'" clearable>
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
       ys:'#008000',
       lx:"restricted",
       xz:"circle"
      },
      rules: {
        mc: [{ required: true, message: "请输入空域名称" }],
        xz: [{ required: true, message: "请选择区域形状" }],
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
        // {
        //   label:"矩形",
        //   value:"rectangle"
        // },
        // {
        //   label:"多边形",
        //   value:"polygon"
        // },
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
          console.log('ssssssssss',this.form.zb);
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
        // this.form.zb = this.ToDegrees(data.zxdwd) + ',' + this.ToDegrees(data.zxdjd)
        this.getAddress(data.zxdjd,data.zxdwd);
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