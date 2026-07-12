<template>
  <div style="width:100%;height:100%;overflow:hidden;">
    <div
      :id="mapId"
      v-show="map2dStore.show"
      :style="{
        width: map2dStore.width,
        height: map2dStore.height,
      }"
      :class="currentMapIndex == 0 ? 'mapListContentYx' : 'mapListContent'"
    ></div>
  </div>
</template>

<script setup>
import { onMounted, nextTick, watch, ref } from "vue";
import { useMap2DStore } from "@/store/modules/map2D";
import Map2D from "@/utils/Map/Map2D";
import mapCenterPoint from "@/components/mapCenterPoint.json";
const mapId = "map2D";
const map2dStore = useMap2DStore();

const initMap = () => {
  Map2D.init(mapId);
  if (window.config.VUE_APP_SUPERMAP_BASE_FLAG) {
    // Map2D.addTiledMapLayer("/map-multiTiles/rest/maps/wj_gis_聚合影像");  // /map-china400/rest/maps/China
    if (window.TOOL.data.get("PROVINCE")) {
      Map2D.addCityToMap("市界", window.TOOL.data.get("PROVINCE"));
    }
  } else {
    // 添加 TMS 瓦片图层
    L.tileLayer(
      window.config.VUE_APP_API_FRONT_BASE_URL + window.config.VUE_APP_MAP_WP,
      {
        tms: true, // 启用 TMS 模式（自动翻转 Y）
        // attribution: '© 福建省瓦片数据',
        maxZoom: 25,
        minZoom: 1,
      }
    ).addTo(window.Map2D.map);
  }
  // Map2D.InitPlot()

  if (
    window.TOOL.data.get("PKTY_USER_INFO") &&
    window.TOOL.data.get("PKTY_USER_INFO").wd &&
    window.TOOL.data.get("PKTY_USER_INFO").jd
  ) {
    Map2D.map.setView(
      [
        window.TOOL.data.get("PKTY_USER_INFO").wd,
        window.TOOL.data.get("PKTY_USER_INFO").jd,
      ],
      window.TOOL.data.get("PKTY_USER_INFO").mapLevel
    );
  } else {
    for (var i of mapCenterPoint) {
      if (i.province == window.TOOL.data.get("PROVINCE")) {
        // setTimeout(() => {
        Map2D.map.setView([i.y, i.x], 8);
        // }, 500);
        break;
      }
    }
  }
};
const currentMapIndex = ref(0);
eventBus.on("mapType", (data) => {
  currentMapIndex.value = data;
});
// 监听数据
// watch(
//   map2dStore,
//   (newValue) => {
//     console.log('xxxxxx00',newValue);
//     if (newValue && newValue.province) {
//      Map2D.addCityToMap('市界',newValue.province)
//     }
//     if(newValue && newValue.PKTY_USER_INFO && newValue.PKTY_USER_INFO.jd && newValue.PKTY_USER_INFO.wd){
//       setTimeout(() => {
//         Map2D.map.setView([newValue.PKTY_USER_INFO.wd, newValue.PKTY_USER_INFO.jd], 8);
//       }, 500);
//     }else if(newValue && newValue.province){
//       for (var i of mapCenterPoint) {
//         if (i.province == newValue.province) {
//           Map2D.map.setView([i.y, i.x], 8);
//           break;
//         }
//       }
//     }
//   },
//   { deep: true, immediate: true }
// );
onMounted(() => {
  nextTick(() => {
    initMap();
  });
});
</script>

<style>
#map2D {
  margin: 0;
  padding: 0;
  position: absolute;
  z-index: 2;
  overflow:hidden;
}
.leaflet-bottom {
  bottom: 16%;
}
.leaflet-div-icon {
  color: #fff;
  background-color: transparent;
}
#timeline {
  /* position: absolute;
    bottom: 16px;
    left: 50%;
    transform: translateX(-50%);
    width: 80%;
    height: 100px;
    background: rgba(255, 255, 255, 0.8);
    border-radius: 5px;
    padding: 10px;
    box-shadow: 0 0 10px rgb(0 0 0 / 10%);
    z-index: 30;
    background: #27598c;
    color: #fff; */
  width: 60%;
  height: 79px;
  padding: 15px 0px 1px 0px;
  position: absolute;
  bottom: 40px;
  left: 50.5%;
  transform: translateX(-50%);
  z-index: 9;
  pointer-events: auto;
  border-radius: 100px;
  border: 1px solid #1498f7;
  background: rgb(41 51 84 / 51%);
}
.timeline {
  height: 100%;
  display: flex;
  align-items: center;
  position: relative;
}
.timeline-line {
  width: 100%;
  height: 4px;
  background: #ddd;
  margin: 0 auto;
}
.timeline-point {
  position: absolute;
  width: 3px;
  height: 10px;
  border-radius: 50%;
  left: 0;
  cursor: pointer;
}
.timeline-current {
  z-index: 1;
  background: #4caf50;
  border-color: #4caf50;
}
.timeline-tick {
  position: absolute;
  top: 41%;
  transform: translateY(-50%);
  width: 3px;
  height: 18px;
  background-color: #fff;
  z-index: 10;
  cursor: pointer;
}

#timeline .play-control {
  position: absolute;
  right: 40px;
  top: -26px;
}
#timeline #speedSelect {
  background: #2c4066;
  color: #95caff;
}
.mapListContent {
  width: 100%;
  height: 100%;
  background: url("@/assets/allImage/mapBg.png") no-repeat !important;
  background-size: 100% 100% !important;
}
.mapListContentYx {
  width: 100%;
  height: 100%;
  /* background: url("@/assets/allImage/bg.png") no-repeat !important;
  background-size: 100% 100% !important; */
}
/* 
.timeline-tick::after {
    content: attr(data-time);
    position: absolute;
    top: -20px;
    left: 50%;
    transform: translateX(-50%);
    color: #fff;
    font-size: 10px;
    background-color: #333;
    padding: 2px 4px;
    border-radius: 4px;
    white-space: nowrap;
} */
.timeKd {
  position: absolute;
  width: 140px;
  margin-top: 20px;
  color: #95caff;
}
.timeline-line .timeline-tick .timeKd {
  left: -35px;
}

/* 关闭按钮核心样式 */
.map-close-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%; /* 圆形按钮，更美观 */
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15); /* 轻微阴影，提升层次感 */
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease; /* 过渡动画，交互更丝滑 */
  box-sizing: border-box;
}

/* 关闭图标样式 */
.close-icon {
  color: #606266;
  font-size: 20px;
  font-weight: bold;
  line-height: 1;
  user-select: none; /* 禁止选中文字 */
}

/* 悬浮交互效果 */
.map-close-btn:hover {
  background: red; /* 主题色高亮 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.map-close-btn:hover .close-icon {
  color: #ffffff; /* 悬浮时图标变白 */
}

/* 点击按压效果 */
.map-close-btn:active {
  transform: scale(0.95); /* 轻微缩小，模拟按压 */
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}
</style>
