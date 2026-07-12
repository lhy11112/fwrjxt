<template>
<div class="largeContent">
  
    <!-- 二维地图 -->
        <Map2DComp></Map2DComp>
        <!-- 三维地图 -->
        <Map3DComp></Map3DComp>
    <div id="bigScreen">
      <TOPTITLE title="数据统计分析"></TOPTITLE>
      <div class="cont">
        <dailyTopic></dailyTopic>
      </div>
      
    </div>
</div>
</template>

<script setup>
import { sendGlobalData } from "@/components/js/getSJiD.js";
import {onMounted,ref} from "vue"
import dailyTopic from "../portal/dailyTopic/index.vue";
onMounted(()=>{
    sendGlobalData()
})
</script>

<style scoped lang="less">
* {
  margin: 0;
  padding: 0;
}
.largeContent {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  #bigScreen {
    width: 100%;
    height: 100%;
    // background: url("@/assets/allImage/bigBack.png") no-repeat;
    // background-size: 100% 100%;
    overflow: hidden;
    z-index: 5;
    pointer-events: none;
    position: absolute;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    .cont {
      width: 100vw;
      height: 93.4vh;
      position: relative;
    }
  }
}

</style>
