<template>
  <div
    :id="mapId"
    v-show="map2dStore.show"
    :style="{
      width: map2dStore.width,
      height: map2dStore.height,
      left: map2dStore.left,
      top: map2dStore.top,
    }"
  ></div>
</template>

<script setup>
import { onMounted } from "vue";
import { useMap2DStore } from "@/store/modules/map2D";
import Map2D from "@/utils/Map/Map2D";
const mapId = "map2D";
const map2dStore = useMap2DStore();

const initMap = () => {
  // 1. 添加影像地图
  Map2D.init(mapId);
  // Map2D.addBaseLayer();
  Map2D.addTiledMapLayer(
    window.config.VUE_APP_SUPERMAP_BASE_URL +
      "/iserver/services/map-multiTiles/rest/maps/wj_gis_%E4%B8%AD%E5%9B%BD_%E8%81%9A%E5%90%88%E5%BD%B1%E5%83%8F"
  );
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
}
</style>
