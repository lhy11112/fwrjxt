<template>
  <div id="electron-map"></div>
</template>
<script setup>
import { ref, onMounted,defineExpose,defineEmits,nextTick } from "vue";

onMounted(()=>{
  initMap()
})
const initMap = () => {
  const url = window.config.VUE_APP_API_FRONT_BASE_URL+window.config.VUE_APP_MAP_WP;
  const map = new Map({
    target:"electron-map",
    layers:[
      new TileLayer({
        source:new XYZ({
          url,
          projection:"EPSG:4490",
        }),
      }),
    ],
    view:new View({
      projection:"EPSG:4490",
      center:[26.0753, 119.3062],
      // proj4("EPSG:4326","EPSG:4490",[
      //   26.0753, 
      //   119.3062,
      // ]),
      zoom:10,
      maxZoom:18,
      
    })
  })
}


</script>

<style scoped>
#electron-map{
  width:100%;
  height:100%;
}
</style>
