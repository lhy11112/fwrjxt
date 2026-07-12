<!-- 移动弹窗 -->
<template>
  <el-dialog
    v-model="props.visibleDialog"
    :draggable="true"
    align-center
    :close-on-click-modal="false"
    destroy-on-close
    :class="['custom-dialog', { 'custom-dialog-dark': isDarkMode }]"
    @closed="closed"
    :title="title"
    :modal="isModal" 
  >
  <!-- 插槽 -->
    <slot></slot>
  </el-dialog>
</template>

<script setup>
import { defineProps, defineEmits, toRef, ref, onMounted } from "vue";
const isDarkMode = ref(true); // 根据实际状态来设置
const emit = defineEmits(["close"]);
// 父组件传过来的参数
const props = defineProps({
  title: {
    type: String,
    default: "",
  },
  visibleDialog: {
    type: Boolean,
    default: false,
  },
  isModal: {
    type: Boolean,
    default: true,
  },
});
// 挂载期间
onMounted(() => {});
// 点击关闭按钮
const closed = () => {
  emit("close");
};
const visibleDialog = toRef(props.visibleDialog);
</script>
<style lang="less" scoped>
// 引入css样式
// @import "@/style/other.css";
</style>
