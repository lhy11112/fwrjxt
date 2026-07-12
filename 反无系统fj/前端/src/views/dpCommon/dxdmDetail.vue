<template>
<AnalysisMoveDlg
    title="综合兵要信息"
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
      <el-row>
         <el-col :span="12">
          <el-form-item label="名称：" prop="dmmc">
            <div>
              {{form.dmmc}}
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="类型：" prop="dxdmlx">
            <div>
              {{form.dxdmlx}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="经度：" prop="jd">
            <div>
              {{form.jd}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="纬度：" prop="wd">
            <div>
              {{form.wd}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="基本信息：" prop="mj">
            <div>
              {{form.mj}}
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
import { ref, onMounted,defineExpose } from "vue";

const visibleDialog = ref(false);
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
</script>

<style scoped>
.create-bottom {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
}
</style>
