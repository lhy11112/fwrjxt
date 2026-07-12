<template>
  <micro-app
    class="microView"
    :name="componentsItem.bm"
    style="width: 100%; height: 100%"
    @mounted="mounted"
    :url="componentsItem.url"
    @datachange="handleDataChange"
    router-mode="pure"
  >
  </micro-app>
</template>

<script setup>
import { sendGlobalData } from "@/components/js/getSJiD.js";
import { ref, onMounted, defineProps, watch } from "vue";
import micro, { EventCenterForMicroApp } from "@micro-zoe/micro-app";
const props = defineProps({
  componentsItem: {
    type: Object,
    default() {
      return {
        modelNeedCb: false,
      };
    },
  },
});
onMounted(() => {
  // 主应用向主应用发送消息通过EventCenterEorMicroApp
  window.eventCenterForAppNameVite = new EventCenterForMicroApp(name);
  sendGlobalData()
});
//监听
watch([() => props.componentsItem], (val) => {
  if (val[0]) {
    forceSetDataFun();
  }
});
const forceSetDataFun = () => {
  micro.forceSetData(
    props.componentsItem.bm, //配块编码
    {
      bm: props.componentsItem.bm, //配块编码
      pkmc: props.componentsItem.pkmc, //配块名称
    }
  );
};
forceSetDataFun();
// 初始化
const mounted = () => {
  // micro.setData(
  //   props.componentsItem.bm,
  //   {token: getToken(),
  //   userInfo: useUserConfig.userInfo,
  //   updateType: "first"
  // });
};
</script>

<style>
</style>