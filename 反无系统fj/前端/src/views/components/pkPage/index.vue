<template>
  <el-dialog
    v-model="dialogcontent"
    width="99.7%"
    title=""
    append-to-body
    :modal="false"
    class="big-Page"
    style="
      margin-left: 0%;
      margin-top: 3%;
      margin-bottom: 0px;
      margin-right: 5px;
      height:93vh;
    "
  >
  <div class="bigContent">
    <div>
    <lay-box
          v-show="isGuarantee"
          :height="100"
          closePosition="none"
          @showBox="isGuarantee = !isGuarantee"
        >
          <template #title>
            <span><img src="@/assets/allImage/titleIcon.png" style="width:20px;height:20px;vertical-align: middle;margin-right: 3px"/></span>
            <span class="c-title-text">{{getTitle(index)}}</span>
          </template>
          <template #tools>
            <el-icon class="iconBig" @click="sxClcik()"><Minus /></el-icon>
          </template>
          <template #content>
            <component :is="getComponentName(index)"></component>
          </template>
        </lay-box>
        </div>
  </div>
  </el-dialog>
</template>

<script setup>
import dayjs from "dayjs";
import microApp from "@/components/microApp/index.vue";
import { ref, onMounted, watch, nextTick, reactive } from "vue";
import { useRouter, useRoute } from "vue-router";
import wrjgjqstj from "../wrjgjqstj/index.vue"
import bjlxfb from "../bjlxfb/index.vue"
import wrjgjhf from "../wrjgjhf/index.vue"
import ppxhfx from "../ppxhfx/index.vue"
import jcfxwztj from "../jcfxwztj/index.vue"
import jcljltj from "../jcljltj/index.vue"
import sbtj from "../sbtj/index.vue"
import kygjqk from "../kygjqk/index.vue"
import zcsbxx from "../zcsbxx/index.vue"
import grsbxx from "../grsbxx/index.vue"
import fzsbxx from "../fzsbxx/index.vue"
const route = useRoute();
const isGuarantee = ref(true);
import Cookies from "js-cookie";
// 获取地址栏路由，和按钮进行对应
const routerUrl = route;
const router = useRouter();
const menuData = ref([])
const menuData1 = ref([
        {
          name:'无人机告警趋势统计',
          indexbh:1,
          componentName:wrjgjqstj
        },
        {
          name:'无人机统计情况',
          indexbh:2,
          componentName:ppxhfx
        },
        // {
        //   name:'诱骗设备统计情况',
        //   indexbh:3,
        //   componentName:bjlxfb
        // },
        {
          name:'设备统计',
          indexbh:3,
          componentName:sbtj
        },
        {
          name:'无人机信息',
          indexbh:4,
          componentName:wrjgjhf
        },
        {
          name:'无人机设备侦测统计',//无人机空域告警统计
          indexbh:5,
          componentName:jcfxwztj
        },
        // {
        //   name:'侦测设备统计情况',
        //   indexbh:6,
        //   componentName:jcljltj
        // },
        {
          name:'告警情况统计',
          indexbh:6,
          componentName:kygjqk
        },
      ])
      const menuData2 = ref([
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
const index = ref(null)
const name = ref("")
const dialogcontent = ref(false)
onMounted(()=>{})
const open = (data,data2) => {
  dialogcontent.value = true;
  index.value = data;
    name.value  = data2;
    if(name.value=="zhzs"){
      menuData.value = menuData1.value
    }else{
      menuData.value = menuData2.value
    }
}
// 抛出该方法
defineExpose({ open });
const sxClcik = () => {
  dialogcontent.value = false;
};

const getTitle = (indexbh) =>{
  console.log(indexbh);
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

<style scoepd lang="less">

@import "@/style/page.css";
.iconBig {
  position: absolute;
  top: 10px;
  right: 10px;
  color: #fff;
  z-index: 999;
  cursor:pointer;
}
.bigContent {
    width: 100%;
    height: calc(99%);
    position: absolute;
    top:1%;
    left:0;
    z-index: 10;
    pointer-events: auto;
    &>div{
      width:100%;
      height:100%;
    }
}
</style>