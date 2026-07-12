<script setup lang="ts">
/**
 * 右键上下文菜单
 */
import { onMounted, onUnmounted } from 'vue'

interface Props {
  visible: boolean
  x: number
  y: number
}

defineProps<Props>()
const emit = defineEmits<{
  close: []
  detail: []
  'deduction-2d': []
  'deduction-3d': []
}>()

function onDocumentClick() {
  emit('close')
}

onMounted(() => document.addEventListener('click', onDocumentClick))
onUnmounted(() => document.removeEventListener('click', onDocumentClick))
</script>

<template>
  <div
    v-if="visible"
    class="context-menu"
    :style="{ left: x + 'px', top: y + 'px' }"
    @click.stop
  >
    <div class="menu-item" @click="emit('detail')">无人机详情</div>
    <div class="menu-item" @click="emit('deduction-2d')">二维推演</div>
    <div class="menu-item" @click="emit('deduction-3d')">三维推演</div>
    <div class="menu-item disabled">诱骗</div>
    <div class="menu-item disabled">干扰</div>
  </div>
</template>

<style scoped>
.context-menu {
  position: fixed;
  z-index: 1000;
  background: #0e457c;
  border: 1px solid #2f7ad4;
  border-radius: 6px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.4);
  min-width: 140px;
  overflow: hidden;
}

.menu-item {
  padding: 8px 16px;
  font-size: 13px;
  color: #c6e0f9;
  cursor: pointer;
  transition: background 0.15s;
}
.menu-item:hover { background: rgba(2, 139, 229, 0.3); color: #4db4ff; }
.menu-item.disabled { color: #5f7fa0; cursor: default; }
.menu-item.disabled:hover { background: transparent; color: #5f7fa0; }
</style>
