<template>
  <div id="electron-map"></div>
</template>
<script setup>
import { ref, onMounted, defineExpose, defineEmits, nextTick } from "vue";


onMounted(() => {
  // 注册 EPSG:4326 投影
  // proj4.register(proj4.EPSG4326);
  Proj4js.defs("EPSG:4490", "+proj=longlat +ellps=GRS80 +no_defs");
        const crs = L.Proj.CRS("EPSG:4490", {
            origin: [102.1, 35.82],
            // origin: [73.5, 53.56],
            resolutions:
                [0.7031249999999999, 0.35156249999999994, 0.17578124999999997, 0.08789062499999999, 0.04394531249999999, 0.021972656249999997, 0.010986328124999998, 0.005493164062499999, 0.0027465820312499996, 0.0013732910156249998, 0.0006866455078124999, 0.00034332275390624995, 0.00017166137695312497, 0.00008583068847656249, 0.00004291534423828124, 0.00002145767211914062, 0.00001072883605957031, 0.000005364418029785155],
            dpi: 96
        });

  const url = window.config.VUE_APP_API_FRONT_BASE_URL + window.config.VUE_APP_MAP_WP;

  // 初始化地图
  const map = L.map("electron-map", {
    center: [26.0753, 119.3062],
    zoom: 10,
    crs: crs,
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