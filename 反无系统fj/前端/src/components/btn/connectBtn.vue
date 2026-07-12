<template>
    <div class="connect-btn">
        <div
            @click="clickFun(item, idx)"
            v-for="(item, idx) in btnData"
            :class="item.isActive ? 'active' : ''"
            :key="idx">
            {{item.text}}
        </div>
    </div>
</template>
<script setup>
import { defineProps, defineEmits } from "vue";


const emit = defineEmits(["select"]);
const props = defineProps({
    btnData: {
        type: Array,
        default() {
            return [];
        },
    }
})

const clickFun = (item, idx) => {
    emit("select", item, idx)
}

</script>
<style lang="less" scoped>
.connect-btn {
    border-radius: 3px;
    font-size: 14px;
    display: flex;
    overflow: hidden;
    height: 2.96vh;

    >div {
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 0 0.63vw;
        border: solid 0.06rem rgba(255, 255, 255, 0.2);
        border-left: 0;
        cursor: pointer;
    }
    
    >div:nth-child(1) {
        border-left: solid 0.06rem rgba(255, 255, 255, 0.2);
    }

    >div:hover,
    >div.active {
        background-color: var(--wj-color-primary--);
    }
}
</style>