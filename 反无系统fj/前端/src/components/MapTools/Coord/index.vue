<template>
    <div class="coord-container">经度: {{ coordStore.x.toFixed(4) }},&nbsp;&nbsp;纬度: {{ coordStore.y.toFixed(4) }},&nbsp;&nbsp;高度: {{ coordStore.z.toFixed(4) }}</div>
</template>

<script setup>
    import { useCoordStore } from "@/store/modules/coord";
    const coordStore = useCoordStore();
</script>

<style lang="less" scoped>
.coord-container {
    // width: 100%;
    font-size: .875rem;
    color: #fff;
    // position: absolute;
    // left: 0;
    // bottom: 0;
    // text-align: center;
    // z-index: 99999;
}
</style>