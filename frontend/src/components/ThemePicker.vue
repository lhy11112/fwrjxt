<script setup lang="ts">
import { ref, computed } from 'vue'

const props = defineProps<{ visible: boolean; current: string }>()
const emit = defineEmits<{ close: []; select: [theme: string] }>()

const themes = [
  {
    id: 'skyblue', name: '电光亮蓝', icon: 'bolt',
    desc: '亮宝蓝科技底 + #028BE5 电光高亮，指挥大屏风格',
    colors: ['#072b57', '#0e457c', '#028be5', '#4db4ff'],
  },
  {
    id: 'light', name: '极简浅色', icon: 'sun',
    desc: '冷蓝白面板 + 阴影层次 + #028BE5 强调',
    colors: ['#e8eff7', '#ffffff', '#028be5', '#3aa8c0'],
  },
  {
    id: 'green', name: '军绿夜视', icon: 'leaf',
    desc: '暗哑墨绿底 + 磷光绿辉光，夜视仪通透感',
    colors: ['#0a0f0a', '#1a231a', '#4a8a3e', '#86d97a'],
  },
  {
    id: 'blue', name: '深海蔚蓝', icon: 'wave',
    desc: '深邃海蓝底 + 青蓝荧光高亮',
    colors: ['#071526', '#162f54', '#1585d6', '#35c4f0'],
  },
]

function select(t: typeof themes[0]) {
  emit('select', t.id)
  emit('close')
}
</script>

<template>
  <Teleport to="body">
    <Transition name="tp-fade">
      <div v-if="visible" class="tp-overlay" @click.self="emit('close')">
        <div class="tp-dialog">
          <div class="tp-header">
            <span class="tp-h-title"><Icon name="palette" :size="17" />切换皮肤主题</span>
            <button class="tp-close" @click="emit('close')">✕</button>
          </div>
          <div class="tp-grid">
            <div v-for="t in themes" :key="t.id"
                 class="tp-card"
                 :class="{ active: current === t.id }"
                 @click="select(t)">
              <div class="tpc-preview" :style="{ background: t.colors[0] }">
                <div class="tpc-bar" :style="{ background: t.colors[1] }"></div>
                <div class="tpc-dots">
                  <span :style="{ background: t.colors[2] }"></span>
                  <span :style="{ background: t.colors[3] }"></span>
                </div>
              </div>
              <div class="tpc-info">
                <span class="tpc-icon"><Icon :name="t.icon" :size="16" /></span>
                <span class="tpc-name">{{ t.name }}</span>
              </div>
              <div class="tpc-desc">{{ t.desc }}</div>
              <div v-if="current === t.id" class="tpc-active">✓ 当前</div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.tp-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.4); backdrop-filter: blur(8px) saturate(1.1);
  -webkit-backdrop-filter: blur(8px) saturate(1.1);
  display: flex; align-items: center; justify-content: center;
  z-index: 10002;
}
.tp-dialog {
  width: min(520px, 90vw);
  background: var(--bg-surface-3); border: 1px solid var(--stroke-divider);
  border-radius: var(--radius-xl); box-shadow: var(--shadow-64);
  overflow: hidden;
}
.tp-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: var(--sp-l) var(--sp-xl); font-size: 16px; font-weight: 700; color: var(--fg-1);
  border-bottom: 1px solid var(--divider);
}
.tp-h-title { display: inline-flex; align-items: center; gap: 8px; }
.tp-h-title :deep(.ico) { color: var(--brand-fg); }
.tp-close {
  width: 28px; height: 28px; border: 1px solid transparent; border-radius: var(--radius-md);
  background: transparent; color: var(--fg-3); cursor: pointer;
  transition: background var(--dur-normal) var(--ease-fluent);
}
.tp-close:hover { background: var(--danger-bg); color: var(--danger); }

.tp-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: var(--sp-m); padding: var(--sp-l);
}
.tp-card {
  background: var(--bg-surface-2); border: 1px solid var(--stroke-divider);
  border-radius: var(--radius-lg); padding: 0; overflow: hidden;
  cursor: pointer; transition: border-color var(--dur-normal) var(--ease-fluent), box-shadow var(--dur-normal) var(--ease-fluent); position: relative;
}
.tp-card:hover { border-color: var(--stroke-control); box-shadow: var(--shadow-8); }
.tp-card.active { border-color: var(--brand-fg); box-shadow: 0 0 0 1px var(--brand-fg); }

.tpc-preview { height: 60px; display: flex; flex-direction: column; justify-content: flex-end; padding: 8px 10px; gap: 4px; }
.tpc-bar { height: 8px; border-radius: var(--radius-sm); width: 70%; }
.tpc-dots { display: flex; gap: 4px; }
.tpc-dots span { width: 10px; height: 10px; border-radius: var(--radius-circular); }

.tpc-info { display: flex; align-items: center; gap: 6px; padding: var(--sp-s) var(--sp-m) 0; }
.tpc-icon { font-size: 18px; }
.tpc-name { font-size: 13px; font-weight: 600; color: var(--fg-1); }
.tpc-desc { font-size: 10px; color: var(--fg-3); padding: 2px var(--sp-m) 10px; }
.tpc-active {
  position: absolute; top: 6px; right: 6px;
  padding: 2px var(--sp-s); background: var(--success-bg);
  border-radius: var(--radius-md); font-size: 10px; color: var(--success); font-weight: 600;
}

.tp-fade-enter-active, .tp-fade-leave-active { transition: opacity var(--dur-slow) var(--ease-fluent); }
.tp-fade-enter-from, .tp-fade-leave-to { opacity: 0; }
.tp-fade-enter-active .tp-dialog { transition: transform var(--dur-slow) var(--ease-decel); }
.tp-fade-enter-from .tp-dialog { transform: scale(0.98) translateY(8px); }
</style>
