<template>
  <div
    :class="type + ' ' + size + ' ' + className + ' operate-btn'"
    :style="btnStyle"
  >
    <img
      v-show="imgSrc ? true : false"
      :src="imgSrc"
      :style="iconStyle"
      alt=""
    />
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
  // type : default primary等 (有无边框等)
  type: {
    type: String,
    default: "primary",
  },
  //size : small large 等 （按钮大小）
  size: {
    typp: String,
    default: "",
  },
  className: {
    type: String,
    default: "'",
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
  cursor: pointer;
  font-size: 14px;
  color: #fff;
  img {
    margin-right: 4px;
    margin-right: 0.21vw;
    // width: 1.02vh;
  }
  .operate-btn-text {
  }
}
.operate-btn.primary {
  border: 1px solid rgba(29, 173, 124, 1);
}
.operate-btn.primary:hover,
.operate-btn.primary.active {
  background: rgba(29, 173, 124, 1);
}
.operate-btn.small {
  font-size: 12px;
  padding: 0 0.47vw;
  height: 1.95vh;
  border-radius: 3px;
}
.operate-btn.middle {
  font-size: 14px;
  padding: 0 0.7vw;
  height: 2.5vh;
  border-radius: 3px;
}
.operate-btn.default {
  color: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.5);
}
.operate-btn.default:hover,
.operate-btn.default.active {
  // color: rgba(0, 233, 216, 1);
  border-color: rgba(0, 233, 216, 1);
}
</style>
