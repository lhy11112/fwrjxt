<template>
  <div :id="mapId" style="width: 100%; height: 100%; left: 0; top: 0"></div>
</template>

<script setup>
import { onMounted } from "vue";
import Map2D from "@/utils/Map/Map2D";
import mapCenterPoint from "@/components/mapCenterPoint.json"

const mapId = "map2D";
const initMap = () => {
  // 1. 添加影像地图
  Map2D.init(mapId);
  // Map2D.addBaseLayer();
  Map2D.addTiledMapLayer(
    `${window.config.VUE_APP_SUPERMAP_BASE_URL}/iserver/services/map-multiTiles/rest/maps/wj_gis_聚合影像`
  );
  // Map2D.addTiledMapLayer(
  //   `${window.config.VUE_APP_SUPERMAP_BASE_URL}/gisData/1805161878149844993/iserver/services/map-multiTiles/rest/maps/wj_gis_聚合影像`
  // );
  Map2D.addTiledMapLayer(
    `${window.config.VUE_APP_SUPERMAP_BASE_URL}/iserver/services/map-ugcv5-wj_gis_ZhongGuo_HangZhengJingJie/rest/maps/wj_gis_%E4%B8%AD%E5%9B%BD_%E8%A1%8C%E6%94%BF%E5%A2%83%E7%95%8C`
  );
  if(window.TOOL.data.get("PROVINCE")){
    Map2D.addCityToMap('市界',window.TOOL.data.get("PROVINCE"))
  }
  if(window.TOOL.data.get("PKTY_USER_INFO") && window.TOOL.data.get("PKTY_USER_INFO").wd && window.TOOL.data.get("PKTY_USER_INFO").jd){
    Map2D.map.setView([window.TOOL.data.get("PKTY_USER_INFO").wd, window.TOOL.data.get("PKTY_USER_INFO").jd], window.TOOL.data.get("PKTY_USER_INFO").mapLevel);
  }else{
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

onMounted(() => {
  initMap();
});
</script>

<style lang="less">
#map2D {
  margin: 0;
  padding: 0;
  position: absolute;
  z-index: 2;
}
</style>
