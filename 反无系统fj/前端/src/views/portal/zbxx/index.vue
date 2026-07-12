<template>
  <div class="powerPage">
    <div class="leftContent">
      <div class="topList">
        <lay-box
          v-show="isGuarantee"
          :height="48"
          closePosition="none"
          @showBox="isGuarantee = !isGuarantee"
        >
          <template #title>
            <span><img src="@/assets/allImage/titleIcon.png" style="width:20px;height:20px;vertical-align: middle;margin-right: 3px"/></span>
            <span class="c-title-text">{{getTitle(1)}}</span>
          </template>
          <template #tools>
            <el-icon class="iconBig" @click="toggleFullScreen(1)"
              ><FullScreen
            /></el-icon>
          </template>
          <template #content>
            <component :is="getComponentName(1)"></component>
          </template>
        </lay-box>
      </div>
      <div class="middleList">
        <!-- <lay-box
          v-show="isGuarantee"
          :height="31"
          closePosition="none"
          @showBox="isGuarantee = !isGuarantee"
        >
          <template #title>
            <span><img src="@/assets/allImage/titleIcon.png" style="width:20px;height:20px;vertical-align: middle;margin-right: 3px"/></span>
            <span class="c-title-text">{{getTitle(2)}}</span>
          </template>
          <template #content>
            <component :is="getComponentName(2)"></component>
          </template>
        </lay-box> -->
        <lay-box
          v-show="isGuarantee"
          :height="48"
          closePosition="none"
          @showBox="isGuarantee = !isGuarantee"
        >
          <template #title>
            <span><img src="@/assets/allImage/titleIcon.png" style="width:20px;height:20px;vertical-align: middle;margin-right: 3px"/></span>
            <span class="c-title-text">{{getTitle(2)}}</span>
          </template>
          <template #tools>
            <el-icon class="iconBig" @click="toggleFullScreen(2)"
              ><FullScreen
            /></el-icon>
          </template>
          <template #content>
            <component :is="getComponentName(2)"></component>
          </template>
        </lay-box>
      </div>
      <!-- <div class="bottomList">
        <lay-box
          v-show="isGuarantee"
          :height="31"
          closePosition="none"
          @showBox="isGuarantee = !isGuarantee"
          style="right: -1px"
        >
          <template #title>
            <span><img src="@/assets/allImage/titleIcon.png" style="width:20px;height:20px;vertical-align: middle;margin-right: 3px"/></span>
            <span class="c-title-text">{{getTitle(6)}}</span>
          </template>
          <template #content>
            <component :is="getComponentName(6)"></component>
          </template>
        </lay-box>
      </div> -->
    </div>
    <div class="rightContent">
      <div class="topList">
        <lay-box
          v-show="isGuarantee"
          :height="48"
          closePosition="none"
          @showBox="isGuarantee = !isGuarantee"
          style="right: -1px"
        >
          <template #title>
            <span><img src="@/assets/allImage/titleIcon.png" style="width:20px;height:20px;vertical-align: middle;margin-right: 3px"/></span>
            <span class="c-title-text">{{getTitle(3)}}</span>
          </template>
          <template #tools>
            <el-icon class="iconBig" @click="toggleFullScreen(3)"
              ><FullScreen
            /></el-icon>
          </template>
          <template #content>
            <component :is="getComponentName(3)"></component>
          </template>
        </lay-box>
      </div>
      <div class="middleList">
        <lay-box
          v-show="isGuarantee"
          :height="48"
          closePosition="none"
          @showBox="isGuarantee = !isGuarantee"
          style="right: -1px"
        >
          <template #title>
            <span><img src="@/assets/allImage/titleIcon.png" style="width:20px;height:20px;vertical-align: middle;margin-right: 3px"/></span>
            <span class="c-title-text">{{getTitle(4)}}</span>
          </template>
          <template #tools>
            <el-icon class="iconBig" @click="toggleFullScreen(4)"
              ><FullScreen
            /></el-icon>
          </template>
          <template #content>
            <component :is="getComponentName(4)"></component>
          </template>
        </lay-box>
      </div>
      <!-- <div class="bottomList">
        
      </div> -->
    </div>
  </div>
  
  <deviceDetailDialog ref="deviceDetailRef"></deviceDetailDialog>
  <pkPage v-if="pkPageVisible" ref="pkPageRef"></pkPage>
</template>

<script setup>
import deviceDetailDialog from "../wxdzc/deviceDetail.vue"
import wrjgjqstj from "../../components/wrjgjqstj/index.vue"
import bjlxfb from "../../components/bjlxfb/index.vue"
import wrjgjhf from "../../components/wrjgjhf/index.vue"
import ppxhfx from "../../components/ppxhfx/index.vue"
import jcfxwztj from "../../components/jcfxwztj/index.vue"
import jcljltj from "../../components/jcljltj/index.vue"
import sbtj from "../../components/sbtj/index.vue"
import kygjqk from "../../components/kygjqk/index.vue"
import zcsbxx from "../../components/zcsbxx/index.vue"
import grsbxx from "../../components/grsbxx/index.vue"
import fzsbxx from "../../components/fzsbxx/index.vue"
import pkPage from "../../components/pkPage/index.vue"
import { useRouter, useRoute } from "vue-router";


import { ref, onMounted,onUnmounted,onBeforeUnmount,nextTick } from "vue";

let sbMarkerLayer = null;
onMounted(()=>{
  // getDevice()
  // nextTick(()=>{
  //   sbMarkerLayer = window.L.layerGroup([]);
  //   sbMarkerLayer.addTo(window.Map2D.map);
  // })
})
onUnmounted(()=>{
  window.eventBus.emit("gjMap",true)
})
const router = useRouter();
const pkPageVisible = ref(false);
const pkPageRef = ref(null);
const toggleFullScreen = (index) => {
  pkPageVisible.value = true;
  nextTick(()=>{
    pkPageRef.value.open(index,"zbxx")
  })
};

const isGuarantee = ref(true);
const menuData = ref([
        {
          name:'无人机信息',
          indexbh:1,
          componentName:wrjgjhf
        },
        {
          name:'侦察设备信息',
          indexbh:2,
          componentName:zcsbxx
        },
        {
          name:'干扰设备信息',
          indexbh:3,
          componentName:grsbxx
        },
        {
          name:'反制设备信息',
          indexbh:4,
          componentName:fzsbxx
        },
      ])
const getTitle = (indexbh) =>{
  const values=menuData.value.filter(v=>v.indexbh==indexbh)
  if(values.length>0){
    return values[0].name
  }else{
    return ''
  }
};
const getComponentName = (indexbh) => {
  const values=menuData.value.filter(v=>v.indexbh==indexbh)
  if(values.length>0){
    return values[0].componentName
  }else{
    return ''
  }
};
</script>

<style scoped>
@import "@/style/page.css";
.iconBig {
  position: absolute !important;
  top: 10px !important;
  right: 10px !important;
  color: #fff !important;
  z-index: 999 !important;
  cursor:pointer;
}
</style>