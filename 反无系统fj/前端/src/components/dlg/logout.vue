<template>
  <div></div>
</template>
<script setup>
import { ref, watch } from "vue";
import operateBtn from "../btn/operateBtn3.vue";
import { Logout } from "@/api/location.js";
// import operateBtn from "../btn/operateBtn2.vue";
// import { manageLink } from "@/utils/const.js";
import { removeToken, getToken } from "@/utils/auth.js";
import { useUserConfigStore } from "@/store/modules/user";
import { ElMessage, ElMessageBox } from 'element-plus'
const showDlg = ref(false);
const userConfigStore = useUserConfigStore(); //用户信息的store
const onClose = () => {
  userConfigStore.updateLogoutDlgFlag(!userConfigStore.logoutDlgFlag);
};

const setDlgFLag = () => {
  Logout().then(() => {
    onClose();
    removeToken();
    window.location.href = window.SERVER_ADDRESS.PUBLIC_LOGIN + "/login";
  });
};

const open = () => {
  ElMessageBox.confirm(
    "确认退出系统？",
    "提示",
    {   
      buttonSize: "default",
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      customClass: "theme-light",
      type: "warning",
      icon: "QuestionFilled"
    }
  )
    .then(() => {
      setDlgFLag()
    })
    .catch(() => {
      onClose()
    });
};

watch(
  () => userConfigStore.logoutDlgFlag,
  (newValue) => {
    showDlg.value = newValue;
    if (newValue) {
        open();
    }
  },
  { immediate: true } // 立即执行一次，而不是等到第一次变化时才执行
);

</script>
<style lang="less" scoped>
.logout-box {
  padding-left: 20px;
  p {
    color: white;
  }
  .footer {
    display: flex;
    padding-bottom: 2vh;
    flex-direction: row-reverse;
    > div {
      margin-right: 20px;
    }
  }
}

:deep(.el-message-box) {
    padding: 25px !important;
}
</style>