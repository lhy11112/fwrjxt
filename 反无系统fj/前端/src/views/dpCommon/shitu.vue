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
    style="width: 40%;height:40vh"
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

         <el-col :span="24">
          <el-form-item label="名称" prop="mc">
            <el-input v-model="form.mc" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="上级目录" prop="pid">
            <el-cascader
              v-model="form.pid"
              style="width: 100%;"
              placeholder="请选择"
              :options="options"
              :props="{label: 'mc',value: 'id',checkStrictly: true}"
              filterable
              :show-all-levels="false"
              @change="pidChange"
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
  data() {
    return {
      mapDialog: false,
      visibleDialog: false,
      form: {},
      rules: {
        mc: [{ required: true, message: "请输入" }],
        lc: [{ required: true, message: "请输入" }],
        xz: [{ required: true, message: "请输入" }],
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
        {
          label:"矩形",
          value:"rectangle"
        },
        {
          label:"多边形",
          value:"polygon"
        },
      ],
      options:[],
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
    open(item) {
      this.mode = item;
      this.visibleDialog = true;
      this.gettreeData()
      return this;
    },
    gettreeData() {
      let params={}
      if(!window.config.VUE_CAS_FLAG){
        params.yhId = window.config.VUE_CAS_YHID
      }
      window.API.scj.tree(params).then(res=>{
        if(res.code==200){
          this.options = [{
            id:'0',
            mc:'顶级',
            children:res.result
          }];
          console.log(this.options);
        }
      })
    },
    pidChange(e){
      this.form.pid = e[e.length-1];
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
          if(this.mode == "add"){
            // this.$message.success("操作成功")
            this.visibleDialog = false;
            this.$emit("successClick",data,this.mode);
          }else if(this.mode == "edit"){
            // this.$message.success("操作成功")
            this.visibleDialog = false;
            this.$emit("successClick",data,this.mode);
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