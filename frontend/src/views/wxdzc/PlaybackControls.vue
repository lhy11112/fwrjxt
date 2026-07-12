<script setup lang="ts">
/**
 * 3D 推演播放控制
 */
const emit = defineEmits<{
  play: []
  pause: []
  roam: []
  back: []
}>()
</script>

<template>
  <div class="playback-bar">
    <button class="pb-btn" @click="emit('play')">▶ 开始</button>
    <button class="pb-btn" @click="emit('pause')">⏸ 暂停</button>
    <button class="pb-btn" @click="emit('roam')">🗺 漫游</button>
    <button class="pb-btn" @click="emit('back')">↩ 返回</button>
  </div>
</template>

<style scoped>
.playback-bar {
  position: absolute;
  top: 12px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  display: flex;
  gap: 8px;
}

.pb-btn {
  padding: 6px 16px;
  background: rgba(14, 69, 124, 0.85);
  border: 1px solid #2f7ad4;
  border-radius: 6px;
  color: #c6e0f9;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}
.pb-btn:hover { background: rgba(23, 88, 154, 0.9); }
</style>
