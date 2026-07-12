<template>
  <div style="width: 100%">
    <el-upload
      class="upload-demo"
      drag
      action=""
      multiple
      :limit="limit"
      :accept="accept"
      :http-request="request"
      :before-upload="beforeUpload"
      :onSuccess="handleSuccess"
    >
      <!-- <el-upload
      ref="wUploadFile"
      class="upload-demo"
      drag
      :fileList="fileList"
      :headers="headers"
      :data="params"
      :action="$CONFIG.VUE_APP_API_BASE_URL + action"
      :accept="accept"
      :multiple="multiple"
      :limit="limit"
      :auto-upload="autoUpload"
      :disabled="disabled"
      :onRemove="handleRemove"
      :onSuccess="handleSuccess"
      :onError="handleError"
      :onChange="handleChange"
      :onProgress="handleProgress"
      :onExceed="handleExceed"
      :on-preview="handlePreview"
    > -->
      <!-- <el-icon class="el-icon--upload"><upload-filled /></el-icon> -->
      <!-- <img src="@/assets/images/uploadIcon.png" alt=""> -->
      <div class="el-upload__text" :style="{'color': color}">
        拖拽或 <em>点击上传</em>
        <div style="font-size: 12px;">
          <span v-if="accept">只能上传{{accept.replaceAll('.','').replaceAll(',','/')}}文件，</span>
          <span v-if="accept">且大小不超过{{size}}MB</span>
          <span v-else>上传大小不超过{{size}}MB</span>
           <span v-if="accept && limit">,只能上传{{limit}}个文件</span>
          <span v-else-if="limit">,只能上传{{limit}}个文件</span>
        </div>
      </div>
      <template #tip>
        <div class="el-upload__tip"></div>
      </template>
      <!-- <template #file="{ file }">
        <div style="padding: 0 5px;color: #fff;">
          {{file.name}}
          <span class="el-upload-list__item-actions" style="float: right;">
            <span
              @click="handlePreview2(file)"
            >
              <el-icon><View /></el-icon>
            </span>
            <span
              v-if="!disabled"
              @click="handleDownload(file)"
            >
              <el-icon><Download /></el-icon>
            </span>
            <span
              v-if="!disabled"
              @click="handleRemove2(file)"
            >
              <el-icon><Delete /></el-icon>
            </span>
          </span>
        </div>
      </template> -->
    </el-upload>
    <div
      v-for="(t, i) in fileList"
      :key="i"
      :style="`padding: 0 5px;color: ${fileList.value?'#000':color};font-size: 14px;line-height: 30px;`"
    >
      <span
        style="
          display: inline-block;
          width: calc(100% - 80px);
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
        "
        >{{ t.name || t.wjmc }}</span
      >
      <span class="el-upload-list__item-actions" style="float: right">
        <!-- <span @click="filePreview(t)">
          <el-icon :style="{'color': color}"><View /></el-icon>
        </span> -->
        <span v-if="!disabled" @click="fileDownload(t)">
          <el-icon :style="{'color': color}"><Download /></el-icon>
        </span>
        <span v-if="!disabled" @click="fileRemove(t)">
          <el-icon :style="{'color': color}"><Delete /></el-icon>
        </span>
      </span>
    </div>

    <el-dialog
      title="预览"
      v-model="wjylFlag"
      :draggable="true"
      align-center
      :close-on-click-modal="false"
      destroy-on-close
      width="1284"
      class="ct_dialog"
      @close="wjylFlag = false"
    >
      <iframe
        :src="wjylUrl"
        width="100%"
        style="height: 60vh"
        frameborder="0"
      ></iframe>
    </el-dialog>
  </div>
</template>

<script>
import axios from "axios";
import config from "@/config";
export default {
  name: "",
  props: {
    color: {
      type: String,
      default: "#fff",
    },
    action: {
      type: String,
      default: config.API_URL+"/sys/common/upload",
    },
    accept: {
      type: String,
      default: "", // .xls,.xlsx,.doc,.docs,.ppt,.txt,.html
    },
    multiple: {
      type: Boolean,
      default: false,
    },
    limit: {
      type: Number,
      default: null,
    },
    size: {
      type: Number,
      default: 200,
    },
    autoUpload: {
      type: Boolean,
      default: true,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    modelValue: {
      type: Array,
      default: [],
    },
    params: {
      type: Object,
      default: () => {
        return { biz: "temp" };
      },
    },
  },
  data() {
    return {
      headers: {
        "x-access-token": window.TOOL.data.get("TOKEN"),
      },
      fileList: [],
      wjylFlag: false,
      wjylUrl: "",
    };
  },
  watch: {
    modelValue: {
      deep: true,
      immediate: true,
      handler(val) {
        console.log(val);
        this.fileList = val;
      },
    },
  },
  mounted() {},
  methods: {
    request(param) {
      const data = new FormData();
      var file = param.file;
      data.append("file", file);
      data.append("biz", this.params.biz);
  
      axios({
        url: window.config.API_URL+"/sys/common/upload", //Local
        data,
        method:"post",
        headers:{
          'Content-Type': 'multipart/form-data',  // 文件上传
        }
      }).then((res) => {
        if (res.data.success) {
          param.onSuccess(res);
        }
      });
    },
    success(res, file, fileList) {
      console.log(res, file,fileList, "879879879897897");
      const obj ={}
      // obj.wjmc = file.name;
      obj.name = file.name;
      obj.wjlj = file.response.data.result.message;
      this.fileList.push(obj);
      this.$emit("success", this.fileList);
      this.$emit("uploadModel", this.fileList);
    },
    beforeUpload(file) {
      if(!file.size || file.size == 0){
        this.$message.error(`上传文件内容不能为空!`);
        return false;
      }
      const fileType = file.name.split('.')[file.name.split('.').length-1];
      if(this.accept && this.accept.indexOf(fileType) == -1){
        this.$message.error(`上传文件不符合格式!`);
        return false;
      }
      const isLt500kb = file.size / 1024 / 1024 < this.size; // 0.5 限制为500KB
      if (!isLt500kb) {
        this.$message.error(`上传文件大小不能超过 ${this.size}MB!`);
      }
      return isLt500kb;
    },
    // handleDownload(file){
    //   const url = window.URL.createObjectURL(file.raw)
    //   const link = document.createElement('a')
    //   link.style.display = 'none'
    //   link.href = url
    //   link.setAttribute('download', file.name)
    //   document.body.appendChild(link)
    //   link.click()
    //   document.body.removeChild(link); //下载完成移除元素
    //   window.URL.revokeObjectURL(url); //释放掉blob对象
    // },
    // handleRemove2(file){
    //   if(this.fileList.length){
    //     this.fileList = this.fileList.filter(row=>{
    //       return row.message != file.response.message
    //     })
    //     this.$emit("uploadModel",this.fileList)
    //   }
    //   this.$refs.wUploadFile.handleRemove(file)
    // },
    // handlePreview2(file){
    //   this.wjylUrl =  process.env.VUE_APP_API_BASE_URL + '/onlinePreview?url='+encodeURIComponent(btoa(encodeURI(process.env.VUE_APP_CAS_BASE_URL_MHWZ_DH + "/jeecg-boot/sys/common/static/" + file.response.message)))

    //   this.wjylFlag = true
    // },
    // 文件下载
    fileDownload(file) {
      const url =
        window.config.VUE_APP_API_BASE_URL +
        window.config.API_URL+"/sys/common/static/" +
        file.wjlj;
      const link = document.createElement("a");
      link.style.display = "none";
      link.href = url;
      link.setAttribute("download", file.name);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link); //下载完成移除元素
      window.URL.revokeObjectURL(url); //释放掉blob对象
    },
    // 文件预览
    filePreview(file) {
      this.wjylUrl =
        window.config.VUE_APP_API_BASE_URL_WDYL +
        "/onlinePreview?url=" +
        encodeURIComponent(
          btoa(
            encodeURI(
              window.config.VUE_APP_API_BASE_URL +
                window.config.API_URL+"/sys/common/static/" +
                file.wjlj
            )
          )
        );

      this.wjylFlag = true;
    },
    // 文件删除
    fileRemove(file) {
      console.log(file);
      if(file.id){
        if (this.fileList.length) {
          this.fileList = this.fileList.filter((row) => {
            return row.id != file.id;
          });
          this.$emit("uploadModel", this.fileList,file);
        }
      }else{
        if (this.fileList.length) {
          this.fileList = this.fileList.filter((row) => {
            return row.message != file.message;
          });
          this.$emit("uploadModel", this.fileList,file);
        }
      }
    },
    handleRemove(uploadFile, uploadFiles) {
      if (this.fileList.length) {
        this.fileList = this.fileList.filter((row) => {
          return row.message != uploadFile.response.message;
        });
        this.$emit("uploadModel", this.fileList);
      }
      this.$emit("remove", uploadFile, uploadFiles, this.fileList);
    },
    handleSuccess(uploadFile, uploadFiles) {
      // this.fileList.push(uploadFile);
      // console.log(this.fileList);
      this.$emit("success", uploadFile, uploadFiles, this.fileList);
      this.$emit("uploadModel", this.fileList,uploadFiles);
    },
    handleError(uploadFile, uploadFiles) {
      this.$emit("error", uploadFile, uploadFiles);
    },
    handleChange(uploadFile, uploadFiles) {
      this.$emit("change", uploadFile, uploadFiles);
    },
    handleProgress(uploadFile, uploadFiles) {
      this.$emit("progress", uploadFile, uploadFiles);
    },
    handleExceed(files, uploadFiles) {
      this.$emit("exceed", files, uploadFiles);
    },
    handlePreview(uploadFile) {
      console.log(uploadFile);
    },
  },
};
</script>

<style scoped>
/* @import "@/style/dialog.css"; */
:deep .el-upload-dragger {
  background: transparent;
  padding: 10px;
}
.el-upload__text,
.el-upload__tip {
  color: #fff;
}
.el-upload-list__item-actions > span {
  margin: 0 5px;
  cursor: pointer;
}
:deep .el-upload-list__item:hover {
  background: #7d714154;
}
:deep .el-upload-list {
  display: none;
}
</style>