<script setup lang="ts">
/**
 * 2D 时间轴播放控制
 */
interface Props {
  currentTime: string
  points: any[]
}

defineProps<Props>()
const emit = defineEmits<{
  seek: [index: number]
}>()

function onSliderInput(e: Event) {
  const val = Number((e.target as HTMLInputElement).value)
  emit('seek', val)
}
</script>

<template>
  <div class="timeline-bar">
    <div class="tl-time">{{ currentTime || '--:--:--' }}</div>
    <input
      type="range"
      class="tl-slider"
      :min="0"
      :max="Math.max(0, points.length - 1)"
      :value="0"
      step="1"
      @input="onSliderInput"
    />
    <div class="tl-ticks">
      <span
        v-for="(p, i) in points.slice(0, 8)"
        :key="i"
        class="tl-tick"
        @click="emit('seek', Math.floor(i * points.length / 8))"
      >
        {{ (p as any).data_time?.slice(-8) || i }}
      </span>
    </div>
  </div>
</template>

<style scoped>
.timeline-bar {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  width: 60%;
  max-width: 800px;
  background: rgba(14, 69, 124, 0.9);
  border: 1px solid #2f7ad4;
  border-radius: 8px;
  padding: 10px 16px;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 12px;
}

.tl-time {
  font-size: 13px;
  color: #4db4ff;
  font-family: monospace;
  white-space: nowrap;
  min-width: 70px;
}

.tl-slider {
  flex: 1;
  accent-color: #4db4ff;
  height: 4px;
}

.tl-ticks {
  display: flex;
  gap: 4px;
}

.tl-tick {
  font-size: 10px;
  color: #6c9bd0;
  cursor: pointer;
  padding: 2px 4px;
  border-radius: 2px;
}
.tl-tick:hover { background: rgba(47, 122, 212, 0.2); }
</style>
