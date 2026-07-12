<template>
    <div :id="mapId" v-show="map3dStore.show" :style="{'width': map3dStore.width, 'height': map3dStore.height, 'left': map3dStore.left, 'top': map3dStore.top}"></div>
</template>

<script setup>
import { onMounted } from "vue";
import { useMap3DStore } from "@/store/modules/map3D";
const mapId = "map3D";
const map3dStore = useMap3DStore();

const initMap = () => {
    Map3D.init(mapId);
}

onMounted(() => {
    initMap();
})
</script>

<style lang="less">
#map3D {
    margin: 0;
    padding: 0;
    position: absolute;
    // z-index: 998;
}
</style>
