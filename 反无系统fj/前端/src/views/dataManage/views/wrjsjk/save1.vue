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
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" :placeholder="'请输入'" clearable>
              <el-option v-for="(item,index) in statusOption" :key="index" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="无人机类型" prop="type">
            <el-input v-model="form.type" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最大续航时间" prop="zdxhsj">
            <el-input v-model="form.zdxhsj" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最大飞行速度" prop="zdfxsd">
            <el-input v-model="form.zdfxsd" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最大控制距离" prop="zdkzjl">
            <el-input v-model="form.zdkzjl" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最大飞行速度" prop="zdfxgd">
            <el-input v-model="form.zdfxgd" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="抗风等级" prop="kfdj">
            <el-input v-model="form.kfdj" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最大荷载重量" prop="zdhzzl">
            <el-input v-model="form.zdhzzl" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="机身尺寸" prop="jscc">
            <el-input v-model="form.jscc" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="机身重量" prop="jszl">
            <el-input v-model="form.jszl" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="动力系统" prop="dlxt">
            <el-input v-model="form.dlxt" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="定位系统" prop="dwxt">
            <el-input v-model="form.dwxt" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="相机参数" prop="xjcs">
            <el-input v-model="form.xjcs" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="图传系统" prop="tcxt">
            <el-input v-model="form.tcxt" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="电池规格" prop="dcgg">
            <el-input v-model="form.dcgg" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="三维模型地址" prop="mxdz">
            <el-input v-model="form.mxdz" :placeholder="'请输入'" clearable>
            </el-input>
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
          <el-form-item label="当前经纬度" prop="zb">
              <!-- <el-input v-model="form.rwdd" :placeholder="'请输入'" clearable> -->
              <el-input
                style="width: 41%; margin-right: 10px"
                v-model="form.currentLongitude"
                :placeholder="'请输入'"
                clearable
                disabled
              >
              </el-input>
              <el-input
                style="width: 41%; margin-right: 10px"
                v-model="form.currentLatitude"
                :placeholder="'请输入'"
                clearable
                disabled
              >
              </el-input>
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
        <el-col :span="24">
          <el-form-item label="弹药" prop="dy">
            <el-input v-model="form.dy" type="textarea" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="探测能力" prop="tcnl">
            <el-input v-model="form.tcnl" type="textarea" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="作战能力" prop="zznl">
            <el-input v-model="form.zznl" type="textarea" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="编配及部署" prop="bpjbs">
            <el-input v-model="form.bpjbs" type="textarea" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="简介" prop="jj">
            <el-input v-model="form.jj" type="textarea" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="24">
            <el-form-item label="缩略图" prop="sltDz">
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
  <el-dialog title="预览" v-model="dialogVisible">
    <div
      style="
        min-height: 500px;
        display: flex;
        justify-content: space-evenly;
        align-items: center;
        padding-bottom: 20px;
      "
    >
      <img style="width: 100%" w-full :src="dialogImageUrl" alt="加载失败" />
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
        serialNumber: [{ required: true, message: "请输入" }],
        brand:[{ required: true, message: "请输入" }],
        model: [{ required: true, message: "请输入" }],
        status: [{ required: true, message: "请输入" }],
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
      ],
      rwData:[]
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