<template>
  <div id="electron-map"></div>
</template>
<script setup>
import { ref, onMounted, defineExpose, defineEmits, nextTick } from "vue";


onMounted(() => {
  // 注册 EPSG:4326 投影
  // proj4.register(proj4.EPSG4326);

  const url = window.config.VUE_APP_API_FRONT_BASE_URL + window.config.VUE_APP_MAP_WP;

  // 初始化地图
  const map = L.map("electron-map", {
    center: [26.0753, 119.3062],
    zoom: 10,
    crs: L.CRS.EPSG4326,
    maxZoom: 18,
  });

  // 添加图层
  L.tileLayer(url, {
    projection: "EPSG:4490",
  }).addTo(map);

  
});

</script>

<style scoped>
#electron-map {
  width: 100%;
  height: 100%;
}
</style>