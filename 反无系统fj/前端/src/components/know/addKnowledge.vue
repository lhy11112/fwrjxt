<!-- 配快管理组件 -->
<template>
  <AnalysisMoveDlg
    :title="dlgTitle"
    style="width: 50%; height: 30vh; pointer-events: auto"
    :visibleDialog="showDlg"
    @close="onClose"
    :isModal="true"
  >
    <div style="padding-top: 10px">
      <el-form
        ref="ruleFormRef"
        :model="ruleForm"
        :rules="rules"
        label-width="100px"
        size="default"
        status-icon
      >
        <el-form-item label="知识库名称" prop="zskMc">
          <el-input size="default" v-model="ruleForm.zskMc" />
        </el-form-item>
        <el-form-item label="知识库描述" prop="zskMs">
          <el-input v-model="ruleForm.zskMs" type="textarea" rows="4" />
        </el-form-item>
      </el-form>
    </div>
    <div class="create-bottom">
      
        <el-button @click="onClose">取 消</el-button>

        <el-button :loading="isSaveing" type="primary" @click="createForm()"
          >保 存</el-button
        >
      </div>
   
  </AnalysisMoveDlg>
</template>
<script setup>
import {
  ref,
  defineProps,
  onMounted,
  reactive,
  defineEmits,
  nextTick,
} from "vue";

import { ElMessage } from "element-plus";
const emit = defineEmits(["getListDataKnow"]);
// 表单校验
const checkZskMc = (rule, value, callback) => {
  if (value.length.length == 0) {
    return callback(new Error("知识库名称不能为空"));
  }
  if (value.length > 16) {
    return callback(new Error("知识库名称长度不能超过16"));
  } else if (
    !/^[A-Za-z]$/.test(value[0]) &&
    !/^[\u4e00-\u9fff]$/.test(value[0])
  ) {
    return callback(new Error("首字母必须是英文或中文"));
  }
  callback();
};
const rules = reactive({
  zskMc: [
    {
      required: true,
      validator: checkZskMc,
      trigger: "blur",
    },
  ],
  zskMs: [
    {
      required: true,
      message: "描述不能为空",
      trigger: "change",
    },
  ],
});
const dlgTitle = ref("");

const ruleForm = reactive({});
const showDlg = ref(false);
const modelist = ref("");
// 定义refruleFormRef
const ruleFormRef = ref(null);

const open = (mode) => {
  console.log(mode);
  ruleForm.zskMc = "";
  ruleForm.zskMs = "";
  ruleForm.zskNm = "";
  ruleForm.id = "";
  modelist.value = mode;
  if (modelist.value == "add") {
    dlgTitle.value = "新建知识库";
  } else {
    dlgTitle.value = "编辑知识库";
  }
  showDlg.value = true;
  return this;
};
// 关闭弹框
const onClose = () => {
  showDlg.value = false;
};
const setData = (item) => {
  console.log(item);
  ruleForm.zskMc = item.zskMc;
  ruleForm.zskMs = item.zskMs;
  ruleForm.zskNm = item.zskNm;
  ruleForm.id = item.id;
};
const createForm = () => {
  ruleFormRef.value.validate(async (valid) => {
    if (valid) {
      if (modelist.value == "add") {
        window.API.wjbdHsZsk.add(ruleForm).then((res) => {
          if (res.code == 200) {
            ElMessage({
              type: "success",
              message: "操作成功",
            });
            nextTick(() => {
              emit("getListDataKnow");
            });
          } else {
            ElMessage({
              type: "error",
              message: res.message,
            });
          }
          showDlg.value = false;
        });
      } else if (modelist.value == "edit") {
        window.API.wjbdHsZsk.edit(ruleForm).then((res) => {
          if (res.code == 200) {
            ElMessage({
              type: "success",
              message: "操作成功",
            });
            nextTick(() => {
              emit("getListDataKnow");
            });
          } else {
            ElMessage({
              type: "error",
              message: res.message,
            });
          }
          showDlg.value = false;
        });
      }
    }
  });
};
defineExpose({
  open,
  setData,
});
</script>
<style lang="less" scoped>
.create-bottom {
  padding-bottom: 4vh;
  display: flex;
  justify-content: center;
  align-items: center;

  > div {
    width: 5.21vw;
    height: 3.43vh;
    padding: 0;
  }

  > div:nth-child(2) {
    margin-left: 0.94vw;
  }
}
:deep(.el-input__inner){
  color:#fff !important;
}
</style>
