<template>
  <div class="powerPage">
    <div class="leftContent">
      <div class="topList" id="topList">
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
            <span class="c-title-text">{{getTitle(6)}}</span>
          </template>
          <template #tools>
            <el-icon class="iconBig" @click="toggleFullScreen(6)"
              ><FullScreen
            /></el-icon>
          </template>
          <template #content>
            <component :is="getComponentName(6)"></component>
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
            <span class="c-title-text">{{getTitle(5)}}</span>
          </template>
          <template #tools>
            <el-icon class="iconBig" @click="toggleFullScreen(5)"
              ><FullScreen
            /></el-icon>
          </template>
          <template #content>
            <component :is="getComponentName(5)"></component>
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
import pkPage from "../../components/pkPage/index.vue"
import { useRouter, useRoute } from "vue-router";


import { ref, onMounted,onUnmounted,onBeforeUnmount,nextTick } from "vue";

let sbMarkerLayer = null;
const timer = ref(null)
onMounted(()=>{
  getDevice()
  timer.value =setInterval(()=>{
    getDevice()
  },1000 *10)
  nextTick(()=>{
    sbMarkerLayer = window.L.layerGroup([]);
    sbMarkerLayer.addTo(window.Map2D.map);
  })
})
onUnmounted(()=>{
  clearInterval(timer.value)
  timer.value=null;
  setTimeout(()=>{
    clearLayer1()
  },200)
  window.eventBus.emit("gjMap",true)
})
const router = useRouter();
const pkPageVisible = ref(false);
const pkPageRef = ref(null);
const toggleFullScreen = (index) => {
  pkPageVisible.value = true;
  nextTick(()=>{
    pkPageRef.value.open(index,"zhzs")
  })
};

const wxdsbData = ref([])
// 获取设备
const getDevice = () =>{
  window.API.wxdzc.list({
    pageNo:1,
    pageSize:100000
  }).then(res=>{
    console.log(res);
    if(res.success){
      wxdsbData.value = res.result.records;
      addPoint()
    }
    
  })
}

const greenIcon = ref(null);
const deviceDetailRef = ref(null)

const addPoint = () => {
  clearLayer1();
  

  wxdsbData.value.forEach((item) => {
    // if (item.status === 'CONNECTED') {
    //   greenIcon.value = window.L.icon({
    //     iconUrl: require('@/assets/allImage/sbZc.png'),
    //     iconSize: [25, 25],
    //   });
    // } else {
    //   greenIcon.value = window.L.icon({
    //     iconUrl: require('@/assets/allImage/sbYc.png'),
    //     iconSize: [25, 25],
    //   });
    // }
    if (item.status === 'CONNECTED') {
      if (item.deviceType === 'DETECT') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/zcsb.png',
          iconSize: [25, 25],
        });
      } else if (item.deviceType === 'DISTURB') {
        greenIcon.value = window.L.icon({
          iconUrl: '/static/grsb.png',
          iconSize: [25, 25],
        });
      }else if (item.deviceType === 'TRAP') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/ypsb.png',
          iconSize: [25, 25],
        });
      }else if (item.deviceType === 'System') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/system2.png',
          iconSize: [25, 25],
        });
      }
    }else{
      if (item.deviceType === 'DETECT') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/zcsb2.png',
          iconSize: [25, 25],
        });
      } else if (item.deviceType === 'DISTURB') {
        greenIcon.value = window.L.icon({
          iconUrl: '/static/grsb2.png',
          iconSize: [25, 25],
        });
      }else if (item.deviceType === 'TRAP') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/ypsb2.png',
          iconSize: [25, 25],
        });
      }else if (item.deviceType === 'System') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/system2.png',
          iconSize: [25, 25],
        });
      }
    }
    if (item.jd && item.wd) {
      const marker = window.L.marker(
        window.L.latLng(Number(item.wd), Number(item.jd)),
        {
          icon: greenIcon.value,
        }
      ).addTo(sbMarkerLayer);

      const html = `<div style="width:140px;background:rgba(30, 32, 44);padding:10px">
                  <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                    <div style="width:100%;margin: 2px 0;color:#fff;display:flex;">名称：<div style="width：calc(100% - 60px);color:#fff;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;">${item.name}</div></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">经度：<span style="color:#fff;">${item.jd.toFixed(3)}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">纬度：<span style="color:#fff;">${item.wd.toFixed(3)}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">状态：<span style="color:#fff;">${item.status=='CONNECTED'?'已连接':'未连接'}</span></div>
                  </div>
                </div>`;
      marker
        .bindPopup(item.name)
        .bindTooltip(html);
      marker.on("click", function () {
        nextTick(()=>{
          deviceDetailRef.value.open(item)
        })
      });
      const center = [item.wd, item.jd];
      const radius = item.zcbj * 1000; // 圆的半径
      const bound = getCriclePoints(center, radius);
      const circleMarker = window.L.polygon(bound, { color: "#03f83c" }).addTo(sbMarkerLayer);
      var  markerIcon = L.divIcon({
                html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>
                <div>${item.name}</div>
                <div style="color:${item.status=='CONNECTED'?'rgb(0,211,0)':'red'}">${item.status=='CONNECTED'?'已连接':'未连接'}</div>
                </div>`,//marker标注
                className: 'my-div-icon',
                iconAnchor: [80, -20]//文字标注相对位置
              });
      window.L.marker(
        window.L.latLng(Number(item.wd), Number(item.jd)),
        {
          icon: markerIcon,
        }
      ).addTo(sbMarkerLayer);
      // const circleMarker = window.L.circle(center, radius, {
      //           color: '#03f83c',
      //           weight: 2
      //       }).addTo(sbMarkerLayer);
      // animateCircle(circleMarker,radius);
    }
  });
};
// 清除图层
const clearLayer1 = () => {
  if (
    sbMarkerLayer != undefined &&
    sbMarkerLayer != null &&
    sbMarkerLayer != ""
  ) {
    // 清空图层
    sbMarkerLayer.clearLayers();
  }
};
const getCriclePoints = (center, radius) => {
  const bound = [];
  const earthRadius = 6378137; // 地球的半径
  const dlat = (radius / earthRadius) * (180 / Math.PI);
  const dlng = dlat / Math.cos((center[0] * Math.PI) / 180);
  for (let i = 0; i < 360; i++) {
    const red = (i * Math.PI) / 180;
    const lat = center[0] + dlat * Math.sin(red);
    const lng = center[1] + dlng * Math.cos(red);
    bound.push([lat, lng]);
  }
  return bound;
};
const isGuarantee = ref(true);
const menuData = ref([
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