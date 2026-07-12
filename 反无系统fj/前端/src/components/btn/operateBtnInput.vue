<template>
  <div
    :class="type + ' ' + size + ' ' + className + ' operate-btn'"
    :style="btnStyle"
  >
    <div>
      <el-input @change="search" :size="size" :placeholder="placeholder" v-model="putValue" />
    </div>
    <span class="operate-btn-text" @click="search">{{ btnText }}</span>
  </div>
</template>
<script setup>
import { ref, defineProps, unref } from "vue";
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
  /***
  *  type : default 默认灰 hover 亮色  primary 透明背景良边框 hover同default
  */
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
  placeholder: {
    type: String,
    default: "自定义",
  }
});
console.log(props);

const putValue = ref("");

const emits = defineEmits("ok");

const search = () => {
  if (unref(putValue)) {
    emits("ok", unref(putValue));
  }
}
</script>
<style lang="less" scoped>
.operate-btn {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0 0.63vw;
  height: 2.96vh;
  cursor: pointer;
  font-size: 14px;
  color: #fff;
  border-radius: 3px;
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
    background-color: rgba(255, 255, 255, 0.2);
    border-radius: 0.19rem;
    border: solid 0.06rem rgba(255, 255, 255, 0.2);
}


.operate-btn.default:hover,
.operate-btn.default.active {
  background-color: #30805d;
        border-radius: 0.16vw;
}


::v-deep .el-input__wrapper {
  padding: 0 !important;
  border: none !important;
  box-shadow: none !important;
}

::v-deep .el-input__inner {
  width: 40px;
  cursor: pointer;
  color: #fff;
  border-radius: 3px;
  background: transparent;
  padding-top: 1px;
}
</style>
