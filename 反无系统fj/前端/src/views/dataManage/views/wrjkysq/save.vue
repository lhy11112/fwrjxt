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
          <el-form-item label="空域" prop="kyid">
            <el-select
              style="width: 100%;"
              filterable
              v-model="form.kyid"
              placeholder="请选择"
              clearable
            >
              <el-option v-for="(t,i) in kyDataList" :key="i" :value="t.id" :label="t.mc"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="无人机" prop="wrjid">
            <el-select
              style="width: 100%;"
              filterable
              v-model="form.wrjid"
              placeholder="请选择"
              clearable
            >
              <el-option v-for="(t,i) in wrjDataList" :key="i" :value="t.id" :label="t.brand+`(${t.serialNumber})`"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="授权时间" prop="sqsj">
            <el-date-picker
              style="width: 100%;"
              v-model="form.sqsj"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择"
              @change="form.sqgqsj=''"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="授权过期时间" prop="sqgqsj">
            <el-date-picker
              style="width: 100%;"
              v-model="form.sqgqsj"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="请选择"
            />
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
        kyid: [{ required: true, message: "请选择" }],
        wrjid: [{ required: true, message: "请选择" }],
        sqsj: [{ required: true, message: "请选择" }]
      },
      mode: "add",
      titleMap: {
        add: "新增",
        edit: "编辑",
        show: "查看",
      },
      rwData:[],
      wrjDataList: [],
      kyDataList: []
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
    // 空域数据
    window.API.wrjky.list({
      pageNo: 1,
      pageSize: 99
    }).then(res=>{
      if(res.code == 200){
        this.kyDataList = res.result.records;
      }
    })
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
            window.API.wrjkysq.add(data).then((res) => {
              if (res.code == 200) {
                this.visibleDialog = false;
                this.$message.success("操作成功")
                this.$emit("successClick");
              }
            });
            
          } else if (this.mode == "edit") {

            window.API.wrjkysq.edit(data).then((res) => {
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