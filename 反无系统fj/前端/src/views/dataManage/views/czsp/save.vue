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
          <el-form-item label="视频分类" prop="spfl">
            <el-select v-model="form.spfl" :placeholder="'请选择'"  filterable  allow-create clearable>
              <el-option v-for="(item,index) in spflOptions" :key="index" :label="item.label" :value="item.value">

              </el-option>
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
          <el-form-item label="文件" prop="splj">
            <w-upload-file style="width: 100%;" limit="1" accept=".avi,.mp4,.MKV" color="#fff" v-model="fileList" @uploadModel="ztUploadModel"></w-upload-file>
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
</template>

<script>
import { checkSpaceTss} from "@/utils/index.js"
export default {
  props:{
    toDp:{
      type:Boolean,
      default:false
    }
  },
  data() {
    return {
      spflOptions:[
        {
          label:"培训视频",
          value:"培训视频"
        },
        {
          label:"操作视频",
          value:"操作视频"
        },
      ],
      visibleDialog: false,
      form: {
       
      },
      rules: {
        spfl: [{ required: true, message: "请选择视频分类" }],
        mc: [{ required: true,  message: "请输入名称" }],
        splj: [{ required: true, message: "请上传文件"}]
      },
      mode: "add",
      titleMap: {
        add: "新增",
        edit: "编辑",
        show: "查看",
      },
      fileList:[]
    };
  },
  created() {
  },
  methods: {
    ztUploadModel(fileList,file){
      console.log(fileList,file);
      // 删除操作
      if(file.message){
        this.fileList = fileList
      }else{ // 新增操作
        // 处理字段不存在或不为数组时异常问题
        if(this.fileList == undefined || !this.fileList) this.fileList = []
        this.fileList.push({
          "fjdx": file.size,
          "fjlx": file.name.split(".")[file.name.split(".").length-1],
          "wjlj": file.response.message || file.response.data.message,
          "wjmc": file.name,
          "fjmc": file.name,
          "message": file.response.message || file.response.data.message
        })
      }
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
      if(data.splj){
        this.fileList.push({
          "wjlj": data.splj,
          "wjmc": data.splj,
          "fjmc": data.splj,
          "message": data.splj
        })
      }
      this.form = Object.assign(this.form, data);
    },
    submit() {
      // 获取上传的文件路径
      if(this.fileList.length){
        this.form.splj = this.fileList.map(item=>{return item.wjlj}).join();
      }
      this.$refs.dialogForm.validate(async (valid) => {
        if (valid) {
          
          let data={}
          data=Object.assign(data,this.form)
          // data.mrcs=JSON.stringify(data.mrcs)
          if (this.mode == "add") {
            window.API.czsp.add(data).then((res) => {
              if (res.code == 200) {
                this.visibleDialog = false;
                this.$message.success("操作成功")
                this.$emit("successClick");
              }
            });
            
          } else if (this.mode == "edit") {

            window.API.czsp.edit(data).then((res) => {
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
</style>