<template>
<AnalysisMoveDlg
    title="综合兵要"
    style="width: 30%;height:30vh"
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
          <el-form-item label="分析范围(KM)" prop="jl">
            <el-input v-model="form.jl" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
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
import { ref, onMounted,defineExpose,defineEmits,nextTick } from "vue";

const visibleDialog = ref(false);
const form = ref({})
const emits = defineEmits(["closed","success"]);
const jd =ref(0)
const wd = ref(0)
const open = (data1,data2) =>{
  visibleDialog.value = true;
  jd.value = Number(data1);
  wd.value = Number(data2);
}
defineExpose({ open });

onMounted(()=>{

})
const dialogForm = ref({})
const zbmbData = ref([])
const submit = () => {
  dialogForm.value.validate(async (valid) => {
    if (valid) {
      window.API.dxdm.getDxdmByJwdAndJl({
        jd:jd.value,
        wd:wd.value,
        jl:form.value.jl
      }).then((res) => {
        if (res.code == 200) {
          visibleDialog.value = false;
          zbmbData.value = res.result;
          emits("success",form.value.jl,zbmbData.value)
        }
      });
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
</style>
