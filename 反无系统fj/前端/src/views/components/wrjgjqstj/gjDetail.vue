<template>
<AnalysisMoveDlg
    title="告警信息"
    style="width: 55%;height:60vh"
    :visibleDialog="visibleDialog"
    @close="closed"
    isModal="true"
  >
    <el-form
      :model="form"
      :rules="rules"
      ref="dialogForm"
      label-width="120px"
      style="height: calc(100% - 40px);overflow: auto;"
    >
      <el-row style="display:flex;justify-content: flex-end;">
        <el-button type="primary" @click="hfEvent">回放</el-button>
      </el-row>
      <el-row>
         <el-col :span="12">
          <el-form-item label="空域：" prop="kyid_dictText">
            <div>
              {{form.kyid_dictText}}
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="无人机：" prop="wrjid_dictText">
            <div>
              {{form.wrjid_dictText}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="告警类型：" prop="gjlx">
            <div>
              {{form.gjlx}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="告警发生经度：" prop="gjfsjd">
            <div>
              {{form.gjfsjd}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="告警发生纬度：" prop="gjfswd">
            <div>
              {{form.gjfswd}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="告警发生时间：" prop="gjfssj">
            <div>
              {{form.gjfssj}}
            </div>
          </el-form-item>
        </el-col>


        <el-col :span="12">
          <el-form-item label="处理状态：" prop="clzt">
            <div>
              {{form.clzt}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="处理时间：" prop="clsj">
            <div>
              {{form.clsj}}
            </div>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <!-- <div class="create-bottom">
      
        <el-button @click="closed">取 消</el-button>

        <el-button  type="primary" @click="submit()"
          >确定</el-button
        >
      </div> -->
  </AnalysisMoveDlg>
</template>
<script setup>
import { ref, onMounted,defineExpose,watch,nextTick } from "vue";
import { useRouter,useRoute } from "vue-router";

const visibleDialog = ref(false);
const router = useRouter() 
const route = useRoute() 
const form = ref({})
const open = (data) =>{
  console.log(data);
  visibleDialog.value = true;
  form.value = data;
}
defineExpose({ open });

onMounted(()=>{

})
const dialogForm = ref({})

const closed = () => {
  visibleDialog.value = false;
}
const hfEvent = () => {
  router.push({
    path:'/gjhf',
    query:form.value
  })
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
