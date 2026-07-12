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
          <el-form-item label="所属任务" prop="rwid">
            <el-select v-model="form.rwid" :placeholder="'请输入'" filterable clearable>
              <el-option v-for="(item,index) in rwData" :label="item.ztmc" :value="item.id">

              </el-option>
            </el-select>
          </el-form-item>
        </el-col>

         <el-col :span="12">
          <el-form-item label="人员类别" prop="rylb">
            <el-input v-model="form.rylb" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="人员名称" prop="rymc">
            <el-input v-model="form.rymc" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="使用武器类型" prop="sylx">
            <el-input v-model="form.sylx" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

         <el-col :span="24">
          <el-form-item label="作战能力" prop="zznl">
            <el-input v-model="form.zznl" type="textarea" :placeholder="'请输入'" clearable autosize maxlength="800" show-word-limit>
            </el-input>
          </el-form-item>
        </el-col>


        <el-col :span="24">
          <el-form-item label="人员作战技能" prop="ryjn">
            <el-input v-model="form.ryjn" type="textarea" :placeholder="'请输入'" clearable autosize maxlength="800" show-word-limit>
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
      visibleDialog: false,
      form: {
       
      },
      rules: {
        rwid: [{ required: true, message: "请选择" }],
        rylb: [{ required: true, validator: checkSpaceTss,trigger: 'blur' }],
        rymc: [{ required: true, validator: checkSpaceTss,trigger: 'blur' }],
        sylx: [{ required: true, validator: checkSpaceTss,trigger: 'blur' }]
      },
      mode: "add",
      titleMap: {
        add: "新增",
        edit: "编辑",
        show: "查看",
      },
      rwData:[]
    };
  },
  created() {
    
  },
  methods: {
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
    },
    submit() {
      this.$refs.dialogForm.validate(async (valid) => {
        if (valid) {
          let data={}
          data=Object.assign(data,this.form)
          // data.mrcs=JSON.stringify(data.mrcs)
          if (this.mode == "add") {
            window.API.dfry.add(data).then((res) => {
              if (res.code == 200) {
                this.visibleDialog = false;
                this.$message.success("操作成功")
                this.$emit("successClick");
              }
            });
            
          } else if (this.mode == "edit") {

            window.API.dfry.edit(data).then((res) => {
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