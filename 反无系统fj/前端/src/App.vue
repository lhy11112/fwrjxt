<template>
  <el-config-provider>
    <div id="app" @contextmenu.prevent="">
      <!-- <TOPTITLE></TOPTITLE>
      <div class="cont">
        <router-view />
        <Map2DComp />
      </div> -->
      <router-view />
    </div>
  </el-config-provider>
</template>

<script setup>
import { onMounted } from "vue"
import webscoket from "@/utils/webscoket";
onMounted(() => {
  try {
    webscoket.initWebscoket()
  } catch (error) {
    
  }
})
</script>

<style>
@import "@/style/dialog.css";
* {
  margin: 0;
  padding: 0;
}
#app {
  height: 100vh;
  width: 100vw;
  /* background: url("./assets/allImage/bigBack.png") no-repeat;
  background-size: 100% 100%; */
  overflow: hidden;
}
</style>
