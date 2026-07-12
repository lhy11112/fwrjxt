<template>
<AnalysisMoveDlg
    title="目标信息"
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
          <el-form-item label="目标识别码" prop="mbsbm">
            <div>
              {{form.mbsbm}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="目标名称：" prop="mbmc">
            <div>
              {{form.mbmc}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="目标别称：" prop="mbbc">
            <div>
              {{form.mbbc}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="目标分类：" prop="mbfl">
            <div>
              {{form.mbfl}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="所属目标体系：" prop="ssmbtx">
            <div>
              {{form.ssmbtx}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="目标等级：" prop="mbdj">
            <div>
              {{form.mbdj}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="国家地区：" prop="gjdq">
            <div>
              {{form.gjdq}}
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="具体位置：" prop="jtwz">
            <div>
              {{form.jtwz}}
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
          <el-form-item label="海拔高：" prop="hbg">
            <div>
              {{form.hbg}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="占地面积：" prop="zdmj">
            <div>
              {{form.zdmj}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="长度：" prop="cd">
            <div>
              {{form.cd}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="宽度：" prop="kd">
            <div>
              {{form.kd}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="其他：" prop="qt">
            <div>
              {{form.qt}}
            </div>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="地位作用：" prop="dwzy">
            <div>
              {{form.dwzy}}
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="重要防护部位：" prop="zyfhbw">
            <div>
              {{form.zyfhbw}}
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="其他功能：" prop="qtgn">
            <div>
              {{form.qtgn}}
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="警戒力量：" prop="jjll">
            <div>
              {{form.jjll}}
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="气候特点主要灾害：" prop="qhtdzyzh">
            <div>
              {{form.qhtdzyzh}}
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="气候特点出现月份：" prop="qhtdcxyf">
            <div>
              {{form.qhtdcxyf}}
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
