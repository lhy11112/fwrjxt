<template>
<AnalysisMoveDlg
    :title="title"
    style="width: 30%;height:32vh"
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

      <el-tabs v-model="activeName" type="border-card" @tab-click="tabClick">
        <el-tab-pane label="周边目标" name="周边目标">
          <el-row>
          <el-col :span="24">
          <el-form-item label="类型" prop="type">
            <el-checkbox-group
              v-model="checkTypeList"
              @change="handleTimeChange"
              size="default"
              fill="#5498db"
              text-color="#5498db"
            >
              <el-checkbox v-for="(t,i) in checkBoxOptions" :key="i" :label="t.value">{{t.label}}</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-col>
         <el-col :span="24">
          <el-form-item label="分析范围(KM)" prop="jl">
            <el-input v-model="form.jl" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
        </el-tab-pane>
        <el-tab-pane label="综合兵要" name="综合兵要">
          <el-row>
            <el-col :span="24">
              <el-form-item label="分析范围(KM)" prop="jl">
                <el-input v-model="form.jl" :placeholder="'请输入'" clearable>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>

      
    </el-form>
    <div class="create-bottom">
      
        <el-button @click="closed">取 消</el-button>

        <el-button  type="primary" @click="submit()"
          >确定</el-button
        >
      </div>
  </AnalysisMoveDlg>
</template>
<script setup>
import { ref, onMounted,defineExpose,defineProps,defineEmits } from "vue";

const props = defineProps({
  title: {
    default: "目标分析",
    type: String,
  },
});

const checkTypeList = ref([])
const checkBoxOptions = ref([
  {
    label:"执勤目标",
    value:"执勤目标"
  },
  {
    label:"民生目标",
    value:"民生目标"
  },
  {
    label:"友邻信息",
    value:"友邻信息"
  },
])

const activeName = ref("周边目标")
const visibleDialog = ref(false);
const form = ref({})
const emits = defineEmits(["closed","success"]);
const jd =ref(0)
const wd = ref(0)
const open = (data1,data2) =>{
  visibleDialog.value = true;
  jd.value = Number(data1);
  wd.value = Number(data2);
  if(window.TOOL.data.get('mbfxfw')){
    form.value.jl = window.TOOL.data.get('mbfxfw')?window.TOOL.data.get('mbfxfw'):30;
  }
}
defineExpose({ open });

onMounted(()=>{

})
const dialogForm = ref({})
const zbmbData = ref([])
const submit = () => {
  dialogForm.value.validate(async (valid) => {
    if (valid) {
      if(activeName.value == "综合兵要"){
        window.API.dxdm.getDxdmByJwdAndJl({
          jd:jd.value,
          wd:wd.value,
          jl:form.value.jl
        }).then((res) => {
          if (res.code == 200) {
            visibleDialog.value = false;
            zbmbData.value = res.result;
            emits("success",form.value.jl,zbmbData.value,activeName.value)
          }
        });
      }else{
        window.API.zbmb.getWrjZymbByJwdAndJlS({
          jd:jd.value,
          wd:wd.value,
          jl:form.value.jl,
          type:checkTypeList.value && checkTypeList.value.length?checkTypeList.value.join(','):''
        }).then((res) => {
          if (res.code == 200) {
            visibleDialog.value = false;
            zbmbData.value = res.result;
            emits("success",form.value.jl,zbmbData.value,activeName.value)
          }
        });
      }
    }
      
  });
}
const closed = () => {
  visibleDialog.value = false;
  
  emits("closed");
}

</script>

<style scoped>
.create-bottom {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
}
:deep(.el-tabs--border-card){
  background:transparent;
}
</style>
