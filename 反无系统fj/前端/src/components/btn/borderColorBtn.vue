<template>
  <div class="operate-btn" :style="btnStyle">
    <img :src="imgSrc" :style="iconStyle" alt="" />
    <span class="operate-btn-text">{{ btnText }}</span>
  </div>
</template>
<script setup>
import { defineProps } from "vue";
const props = defineProps({
  imgSrc: {
    type: Proxy,
    default: "",
  },
  btnText: {
    type: String,
    default: "",
  },
  iconStyle: {
    type: Object,
    default() {
      return {
        width: "1.02vh",
      };
    },
  },
  btnStyle: {
    type: Object,
    default() {
      return {};
    },
  },
});
console.log(props);
</script>
<style lang="less" scoped>
.operate-btn {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0 0.57vw;
  height: 2.41vh;
  border: 1px solid rgba(29, 173, 124, 1);
  cursor: pointer;
  img {
    margin-right: 4px;
    // width: 1.02vh;
  }
  .operate-btn-text {
    margin-left: 0.21vw;
  }
}
.operate-btn:hover {
  background: rgba(29, 173, 124, 1);
}
</style>
