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
          <el-form-item label="名称" prop="mc">
            <el-input v-model="form.mc" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="序列号" prop="serialNumber">
            <el-input v-model="form.serialNumber" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="品牌" prop="brand">
            <!-- <el-input v-model="form.brand" @blur="brandBlur" :placeholder="'请输入'" clearable>
            </el-input> -->
            <el-select v-model="form.brand" :placeholder="'请输入'" clearable @change="brandBlur" filterable allowCreate>
              <el-option v-for="(item,index) in brandOption" :label="item.lable" :value="item.label"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="型号" prop="model">
            <el-input v-model="form.model" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="无人机类型" prop="type">
            <el-select v-model="form.type" :placeholder="'请输入'" clearable filterable allowCreate>
              <el-option v-for="(item,index) in typeOption" :label="item.lable" :value="item.label"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="地区分类" prop="dqfl">
            <el-select v-model="form.dqfl" :placeholder="'请输入'" clearable filterable allowCreate>
              <el-option v-for="(item,index) in dqflOption" :label="item.lable" :value="item.label"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="种类分类" prop="zlfl">
            <el-select v-model="form.zlfl" :placeholder="'请输入'" clearable filterable allowCreate>
              <el-option v-for="(item,index) in zlflOption" :label="item.lable" :value="item.label"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col v-for="(item,index) in wrjInfo" :key="index" :span="item.span">
          <el-form-item :label="item.label" :prop="item.value">
            <template v-if="item.span=='12'">
              <el-input v-model="form[item.value]" :placeholder="'请输入'" clearable>
              </el-input>
            </template>
            <template v-else>
              <el-input v-model="form[item.value]" type="textarea" :placeholder="'请输入'" clearable>
              </el-input>
            </template>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" type="textarea" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="24">
            <el-form-item label="图片" prop="tp">
              <el-upload
                ref="pictureUploadRef"
                :headers="headers"
                :fileList="fileList"
                action="/wrj-api/sys/common/upload"
                :disabled="fileList.length > 0"
                :limit="1"
                :data="{ biz: 'wrjImage' }"
                list-type="picture-card"
                :auto-upload="true"
                :onSuccess="handleSuccess"
              >
                <el-icon><Plus /></el-icon>
                <template #file="{ file }">
                  <div>
                    <img
                      class="el-upload-list__item-thumbnail"
                      :src="getImgView(file.url)"
                      alt=""
                    />
                    <span class="el-upload-list__item-actions">
                      <span
                        class="el-upload-list__item-preview"
                        @click="handlePictureCardPreview(file)"
                      >
                        <el-icon><zoom-in /></el-icon>
                      </span>
                      <span
                        v-if="!disabled"
                        class="el-upload-list__item-delete"
                        @click="handleDownload(file)"
                      >
                        <el-icon><Download /></el-icon>
                      </span>
                      <span
                        v-if="!disabled"
                        class="el-upload-list__item-delete"
                        @click="handleRemove(file)"
                      >
                        <el-icon><Delete /></el-icon>
                      </span>
                    </span>
                  </div>
                </template>
              </el-upload>
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
  <el-dialog title="预览" :draggable="true" style="width: 45%;height:60vh;margin-top:10%" v-model="dialogVisible">
    <div
      style="
        height: 100%;
        display: flex;
        justify-content: space-evenly;
        align-items: center;
        padding-bottom: 20px;
      "
    >
      <img style="width: 100%;height:100%;" w-full :src="dialogImageUrl" alt="加载失败" />
    </div>
  </el-dialog>
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
      headers: {
        "x-access-token": this.$TOOL.data.get("TOKEN"),
      },
      fileList: [],
      dialogVisible: false,
      dialogImageUrl: "",
      visibleDialog: false,
      form: {
        authStatus: 3
      },
      rules: {
        mc: [{ required: true, message: "请输入名称" }],
        serialNumber: [{ required: true, message: "请输入序列号" }],
        brand:[{ required: true, message: "请输入品牌" }],
        model: [{ required: true, message: "请输入型号" }],
        //status: [{ required: true, message: "请输入" }],
      },
      mode: "add",
      titleMap: {
        add: "新增",
        edit: "编辑",
        show: "查看",
      },
      
      typeOption:[
        {
          label:'民用',
          value:'民用'
        },
        {
          label:'军用',
          value:'军用'
        }
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
      ],
      rwData:[],
      wrjInfo:[],
      brandOption:[]
    };
  },
  created() {
    this.dqflOption = window.WRJXX.dqflOption;
    this.zlflOption = window.WRJXX.zlflOption;
    this.brandOption = window.WRJXX.brandOption;
  },
  methods: {
    brandBlur(){
      let brands = this.brandOption.map(v=>v.label);
      if(!brands.includes(this.form.brand)){
        this.wrjInfo = window.WRJXX.allzd;
      }else if(this.form.brand.indexOf("彩虹")!=-1 || this.form.brand.indexOf("凤翎")!=-1 || this.form.brand.indexOf("翼神")!=-1 ||  this.form.brand.indexOf("SY-450H")!=-1 || this.form.brand.indexOf("八旋翼")!=-1 ){
        this.wrjInfo = window.WRJXX.myggzd;
      }else if(this.form.brand.indexOf("牵牛星")!=-1 || this.form.brand.indexOf("海盗")!=-1 || this.form.brand.indexOf("猎户座")!=-1 ||  this.form.brand.indexOf("海雕")!=-1 || this.form.brand.indexOf("猎人")!=-1 ){
        this.wrjInfo = window.WRJXX.ejwrjggzd
      }else {
         this.wrjInfo = window.WRJXX[this.form.brand];
      }
    },
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
    open(item) {
      this.mode = item;
      this.visibleDialog = true;
    },
    closed(){
      this.visibleDialog = false;
      this.$emit('closed')
    },
    setData(data) {
      if (data.tp) {
        this.fileList = [
          {
            url: "/wrj-api/sys/common/static/" + data.tp,
            filePath: data.tp,
          },
        ];
      } else {
        this.fileList = [];
      }
      this.form = Object.assign(this.form, data);
      if(this.form.currentLatitude && this.form.currentLongitude){
        this.form.currentLatitude = this.ToDegrees(this.form.currentLatitude);
        this.form.currentLongitude = this.ToDegrees(this.form.currentLongitude);
        this.form.zb = this.ToDegrees(data.currentLatitude) + ',' + this.ToDegrees(data.currentLongitude)
       }
       this.brandBlur()
    },
    submit() {
      this.$refs.dialogForm.validate(async (valid) => {
        if (valid) {
          if (this.fileList.length) {
            this.form.tp = this.fileList[0].url.split("static/")[this.fileList[0].url.split("static/").length-1];
          } else {
            this.form.tp = "";
          }
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
            window.API.wrjsjk.add(data).then((res) => {
              if (res.code == 200) {
                this.visibleDialog = false;
                this.$message.success("操作成功")
                this.$emit("successClick");
              }
            });
            
          } else if (this.mode == "edit") {

            window.API.wrjsjk.edit(data).then((res) => {
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
    handlePictureCardPreview(file) {
      this.dialogImageUrl = file.url;
      this.dialogVisible = true;
    },
    // 缩略图下载
    handleDownload(file){
      const url = file.url;
      const link = document.createElement("a");
      link.style.display = "none";
      link.href = url;
      link.setAttribute("download", file.name);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link); //下载完成移除元素
      window.URL.revokeObjectURL(url); //释放掉blob对象
    },
    // 缩略图删除
    handleRemove() {
      this.fileList = [];
      this.$refs.pictureUploadRef.clearFiles();
    },
    // 上传成功回调
    handleSuccess(uploadFile) {
      console.log(uploadFile);
      this.fileList.push({
        url: uploadFile.message
      });
    },
    getImgView(text) {
      if (text && text.indexOf(",") >= 0) {
        text = text.substring(0, text.indexOf(","));
      }
      return this.getFileAccessHttpUrl(text);
    },
    /* 图片预览开始 */
    getFileAccessHttpUrl(avatar, subStr) {
      if (avatar && avatar.startsWith(subStr)) {
        return avatar;
      } else {
        if (avatar && avatar.length > 0 && avatar.indexOf("[") == -1) {
          return avatar // window.SERVER_ADDRESS.VUE_APP_API_BASE_URL + "/wrj-api/sys/common/static/" + avatar;
        }
      }
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
:deep .el-upload--picture-card {
  background: transparent;
}
:deep(.el-input.is-disabled .el-input__wrapper){
  background-color: rgba(54, 108, 161, 0.1);
}
</style>