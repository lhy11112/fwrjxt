<template>
  <div>
    <el-upload
      ref="wUploadFile"
      class="upload-demo"
      drag
      :fileList="fileList"
      :headers="headers"
      :data="params"
      :action="action"
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
    >
      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
      <!-- <img src="@/assets/images/uploadIcon.png" alt=""> -->
      <div class="el-upload__text" :style="{'color': color}">
        拖拽或 <em>点击上传</em>
      </div>
      <template #tip>
        <div class="el-upload__tip" :style="{'color': color}">
          
        </div>
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
    <div v-for="(t,i) in (fileList.value || fileList)" :key="i" :style="`padding: 0 5px;color: ${fileList.value?'#000':color};font-size: 14px;line-height: 30px;`">
      <span style="display: inline-block;width: calc(100% - 80px);overflow: hidden;white-space: nowrap;text-overflow: ellipsis;">{{t.wjmc}}</span>
      <span class="el-upload-list__item-actions" style="float: right;">
        <span
          @click="filePreview(t)"
        >
          <el-icon :style="{'color': color}"><View /></el-icon>
        </span>
        <span
          v-if="!disabled"
          @click="fileDownload(t)"
        >
          <el-icon :style="{'color': color}"><Download /></el-icon>
        </span>
        <span
          v-if="!disabled"
          @click="fileRemove(t)"
        >
          <el-icon :style="{'color': color}"><Delete /></el-icon>
        </span>
      </span>
    </div>

    <el-dialog title="预览" v-model="wjylFlag" destroy-on-close width="1284px" class="ct_dialog" @close="wjylFlag = false">
      <iframe :src="wjylUrl" width="100%" style="height: 60vh;" frameborder="0"></iframe>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: '',
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
      default: ()=> {return {"biz": "temp"}},
    },
  },
  data(){
    return{
      headers: {
        'x-access-token': this.$TOOL.data.get("TOKEN")
      },
      fileList: [],
      wjylFlag: false,
      wjylUrl: ""
    }
  },
  watch:{
    modelValue: {
      deep: true,
      immediate: true,
      handler(val){
        // console.log(val);
        this.fileList = val
        // for(var i of val){
        //   this.fileList.push({
        //     code: 0,
        //     message: i,
        //     result: null,
        //     success: true
        //   })
        // }
      }
    }
  },
  mounted(){
    
  },
  methods: {
    handleDownload(file){
      const url = window.URL.createObjectURL(file.raw)
      const link = document.createElement('a')
      link.style.display = 'none'
      link.href = url
      link.setAttribute('download', file.name)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link); //下载完成移除元素
      window.URL.revokeObjectURL(url); //释放掉blob对象
    },
    handleRemove2(file){
      if(this.fileList.length){
        this.fileList = this.fileList.filter(row=>{
          return row.message != file.response.message
        })
        this.$emit("uploadModel",this.fileList)
      }
      this.$refs.wUploadFile.handleRemove(file)
    },
    handlePreview2(file){
      this.wjylUrl = this.$CONFIG.VUE_APP_API_BASE_URL_WDYL + '/onlinePreview?url='+encodeURIComponent(btoa(encodeURI(this.$CONFIG.VUE_APP_CAS_BASE_URL_MHWZ_DH + "/wwct-api/sys/common/static/" + file.response.message)))
      this.wjylFlag = true
    },
    // 文件下载
    fileDownload(file){
      const url = this.$CONFIG.VUE_APP_CAS_BASE_URL_MHWZ_DH + "/wwct-api/sys/common/static/" + file.message
      const link = document.createElement('a')
      link.style.display = 'none'
      link.href = url
      link.setAttribute('download', file.name)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link); //下载完成移除元素
      window.URL.revokeObjectURL(url); //释放掉blob对象
    },
    // 文件预览
    filePreview(file){
      this.wjylUrl = this.$CONFIG.VUE_APP_API_BASE_URL_WDYL + '/onlinePreview?url='+encodeURIComponent(btoa(encodeURI(this.$CONFIG.VUE_APP_CAS_BASE_URL_MHWZ_DH + "/wwct-api/sys/common/static/" + file.message)))
      this.wjylFlag = true
    },
    // 文件删除
    fileRemove(file){
      if(this.fileList.length){
        this.fileList = this.fileList.filter(row=>{
          return row.message != file.message
        })
        this.$emit("uploadModel",this.fileList,file)
      }
    },
    handleRemove(uploadFile,uploadFiles){
      if(this.fileList.length){
        this.fileList = this.fileList.filter(row=>{
          return row.message != uploadFile.response.message
        })
        this.$emit("uploadModel",this.fileList)
      }
      this.$emit("remove",uploadFile,uploadFiles,this.fileList)
    },
    handleSuccess(uploadFile,uploadFiles){
      // this.fileList.push(uploadFile)
      console.log(this.fileList);
      this.$emit("success",uploadFile,uploadFiles,this.fileList)
      this.$emit("uploadModel",this.fileList,uploadFiles)
    },
    handleError(uploadFile,uploadFiles){
      this.$emit("error",uploadFile,uploadFiles)
    },
    handleChange(uploadFile,uploadFiles){
      this.$emit("change",uploadFile,uploadFiles)
    },
    handleProgress(uploadFile,uploadFiles){
      this.$emit("progress",uploadFile,uploadFiles)
    },
    handleExceed(files,uploadFiles){
      this.$emit("exceed",files,uploadFiles)
    },
    handlePreview(uploadFile){
      console.log(uploadFile);
    }
  },
}
</script>

<style scoped>
@import "@/style/dialog.css";
:deep .el-upload-dragger{
  background: transparent;
  padding: 0px;
}
.el-upload__text,.el-upload__tip{
  color: #fff;
}
.el-upload-list__item-actions>span{
  margin: 0 5px;
  cursor: pointer;
}
:deep .el-upload-list__item:hover{
  background: #7d714154;
}
:deep .el-upload-list{
  display: none;
}
</style>