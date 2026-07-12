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
          <el-form-item label="分类" prop="fl">
            <el-select v-model="form.fl" :placeholder="'请选择'" clearable>
              <el-option v-for="(item,index) in flOption" :key="index" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
        </el-col>

         <el-col :span="12">
          <el-form-item label="名称" prop="mc">
            <el-input v-model="form.mc" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="功能特点" prop="gntd">
            <el-input type="textarea" v-model="form.gntd" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="技术指标" prop="jszb">
            <el-input type="textarea" v-model="form.jszb" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

         <el-col :span="12">
          <el-form-item label="装备数量" prop="sl">
            <el-input v-model="form.sl" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="装备来源" prop="zbly">
            <el-input type="textarea" v-model="form.zbly" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input type="textarea" v-model="form.remark" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>


        <el-col :span="24">
          <el-form-item label="装备图册" prop="tp">
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
      mapDialog: false,
      visibleDialog: false,
      dialogVisible: false,
      dialogImageUrl:'',
      form: {
       
      },
      rules: {
        fl: [{ required: true,trigger: 'blur', message: "请选择分类" }],
        mc: [{ required: true,trigger: 'blur', message: "请输入名称" }],
      },
      flOption:[
        {
          label:"侦察设备",
          value:'侦察设备'
        },
        {
          label:"干扰设备",
          value:'干扰设备'
        },
        {
          label:"反制设备",
          value:'反制设备'
        },
      ],
      mode: "add",
      titleMap: {
        add: "新增",
        edit: "编辑",
        show: "查看",
      },
      rwData:[],
      mapDialog:false,
      headers: {
        "x-access-token": this.$TOOL.data.get("TOKEN"),
      },
      fileList: [],
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
      this.form.wd = this.ToDegrees(item1.split(",")[0]);
      this.form.jd = this.ToDegrees(item1.split(",")[1]);
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
      if(this.form.wd && this.form.jd){
        this.form.wd = this.ToDegrees(this.form.wd);
        this.form.jd = this.ToDegrees(this.form.jd);
        this.form.zb = this.ToDegrees(data.wd) + ',' + this.ToDegrees(data.jd)
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
          if(data.jd){
            data.jd=this.DegreeConvertBack(data.jd)
          }
          if(data.wd){
            data.wd=this.DegreeConvertBack(data.wd)
          }
          // data.mrcs=JSON.stringify(data.mrcs)
          if (this.mode == "add") {
            window.API.fzzb.add(data).then((res) => {
              if (res.code == 200) {
                this.visibleDialog = false;
                this.$message.success("操作成功")
                this.$emit("successClick");
              }
            });
            
          } else if (this.mode == "edit") {

            window.API.fzzb.edit(data).then((res) => {
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
:deep(.el-input.is-disabled .el-input__wrapper){
  background-color: rgba(54, 108, 161, 0.1);
}
:deep .el-upload--picture-card {
  background: transparent;
}
:deep(.el-input.is-disabled .el-input__wrapper){
  background-color: rgba(54, 108, 161, 0.1);
}
</style>