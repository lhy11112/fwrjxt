<script setup lang="ts">
withDefaults(defineProps<{ name: string; size?: number | string }>(), { size: 16 })
</script>

<template>
  <svg :width="size" :height="size" viewBox="0 0 24 24" fill="none" stroke="currentColor"
       stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" class="ico">
    <!-- 外壳 / 导航 -->
    <template v-if="name === 'shield'"><path d="M12 3l7 2.6v5.9c0 4.2-3 7-7 8.5-4-1.5-7-4.3-7-8.5V5.6z"/></template>
    <template v-else-if="name === 'map'"><path d="M9 4 3 6.2v13.8l6-2.2 6 2.2 6-2.2V4l-6 2.2z"/><path d="M9 4v13.8M15 6.2V20"/></template>
    <template v-else-if="name === 'target'"><circle cx="12" cy="12" r="8"/><circle cx="12" cy="12" r="3.2"/><path d="M12 1.5v3.5M12 19v3.5M1.5 12h3.5M19 12h3.5"/></template>
    <template v-else-if="name === 'device'"><rect x="3" y="4" width="18" height="13" rx="1.5"/><path d="M8.5 21h7M12 17v4"/></template>
    <template v-else-if="name === 'model'"><rect x="7" y="7" width="10" height="10" rx="1"/><path d="M10 3.5v3.5M14 3.5v3.5M10 17v3.5M14 17v3.5M3.5 10h3.5M3.5 14h3.5M17 10h3.5M17 14h3.5"/></template>
    <template v-else-if="name === 'chart'"><path d="M4 20h16"/><path d="M6.5 20v-5M11 20V8M15.5 20v-8"/></template>
    <template v-else-if="name === 'sensor'"><circle cx="12" cy="12" r="1.6" fill="currentColor" stroke="none"/><path d="M8.8 8.8a4.5 4.5 0 0 0 0 6.4M15.2 8.8a4.5 4.5 0 0 1 0 6.4M6.3 6.3a8 8 0 0 0 0 11.4M17.7 6.3a8 8 0 0 1 0 11.4"/></template>
    <template v-else-if="name === 'chat'"><path d="M4.5 5h15v10h-9l-4 3.5V15h-2z"/><path d="M8.5 9h7M8.5 12h4.5"/></template>
    <template v-else-if="name === 'list'"><path d="M8 6.5h12M8 12h12M8 17.5h12"/><circle cx="4.5" cy="6.5" r="1" fill="currentColor" stroke="none"/><circle cx="4.5" cy="12" r="1" fill="currentColor" stroke="none"/><circle cx="4.5" cy="17.5" r="1" fill="currentColor" stroke="none"/></template>
    <template v-else-if="name === 'pulse'"><path d="M3 12h4l2.5-7 4 14 2.5-7H21"/></template>
    <template v-else-if="name === 'clock'"><circle cx="12" cy="12" r="8.5"/><path d="M12 7.5V12l3 2"/></template>
    <template v-else-if="name === 'mouse'"><rect x="7" y="3" width="10" height="18" rx="5"/><path d="M12 7v3.5"/></template>
    <!-- 主题 -->
    <template v-else-if="name === 'palette'"><path d="M12 3a9 9 0 1 0 0 18c1.4 0 2-1 2-2s-.6-1.4-.6-2.4.6-1.6 2.1-1.6H18a3 3 0 0 0 3-3c0-4.4-4-6-9-6z"/><circle cx="7.5" cy="11" r="1.1" fill="currentColor" stroke="none"/><circle cx="12" cy="7.8" r="1.1" fill="currentColor" stroke="none"/><circle cx="16" cy="10" r="1.1" fill="currentColor" stroke="none"/></template>
    <template v-else-if="name === 'bolt'"><path d="M13 2 4 14h6l-1 8 9-12h-6z"/></template>
    <template v-else-if="name === 'sun'"><circle cx="12" cy="12" r="4.5"/><path d="M12 2v2.5M12 19.5V22M2 12h2.5M19.5 12H22M4.9 4.9l1.7 1.7M16.4 16.4l1.7 1.7M4.9 19.1l1.7-1.7M16.4 7.6l1.7-1.7"/></template>
    <template v-else-if="name === 'leaf'"><path d="M4 20c0-8 6-14 16-14 0 10-6 16-14 16-2 0-2-2-2-2z"/><path d="M9 15c2-3 5-5 8-6"/></template>
    <template v-else-if="name === 'wave'"><path d="M2 8c2-2 4-2 6 0s4 2 6 0 4-2 6 0M2 13c2-2 4-2 6 0s4 2 6 0 4-2 6 0M2 18c2-2 4-2 6 0s4 2 6 0 4-2 6 0"/></template>
    <!-- 设备类型 -->
    <template v-else-if="name === 'radar'"><circle cx="12" cy="12" r="9"/><circle cx="12" cy="12" r="1.6" fill="currentColor" stroke="none"/><path d="M12 12l6.5-3.5"/><path d="M12 3a9 9 0 0 1 8 5"/></template>
    <template v-else-if="name === 'signal'"><path d="M4 18a12 12 0 0 1 16 0M7 15a8 8 0 0 1 10 0M9.5 12.2a4.5 4.5 0 0 1 5 0"/><circle cx="12" cy="18.5" r="1.3" fill="currentColor" stroke="none"/></template>
    <template v-else-if="name === 'camera'"><rect x="3" y="7" width="18" height="12" rx="2"/><circle cx="12" cy="13" r="3.2"/><path d="M8.5 7 10 4.5h4L15.5 7"/></template>
    <template v-else-if="name === 'mic'"><rect x="9" y="3" width="6" height="11" rx="3"/><path d="M6 11a6 6 0 0 0 12 0M12 17v4"/></template>
    <template v-else-if="name === 'bulb'"><path d="M9.5 18h5M10.5 21h3"/><path d="M12 3a6 6 0 0 0-3.7 10.7c.7.6 1.2 1.3 1.2 2.3h5c0-1 .5-1.7 1.2-2.3A6 6 0 0 0 12 3z"/></template>
    <template v-else-if="name === 'satellite'"><path d="M4 20a10 10 0 0 1 10-10M4 20a6 6 0 0 1 6-6"/><circle cx="5" cy="19" r="1.4" fill="currentColor" stroke="none"/><path d="M13.5 3.5 20 10l-3.5 1.2-3.7-3.7z"/></template>
    <template v-else-if="name === 'scope'"><circle cx="12" cy="12" r="8.5"/><circle cx="12" cy="12" r="2.6"/><path d="M12 3.5v3M12 17.5v3M3.5 12h3M17.5 12h3"/></template>
    <template v-else-if="name === 'thermal'"><path d="M14 14.8V5a2 2 0 0 0-4 0v9.8a4 4 0 1 0 4 0z"/><path d="M12 9v5.5"/></template>
    <template v-else-if="name === 'refresh'"><path d="M20 11a8 8 0 0 0-14-4.6L4 8M4 4v4h4"/><path d="M4 13a8 8 0 0 0 14 4.6L20 16M20 20v-4h-4"/></template>
    <template v-else-if="name === 'box'"><path d="M12 3l8 4.5v9L12 21l-8-4.5v-9z"/><path d="M4 7.5l8 4.5 8-4.5M12 12v9"/></template>
    <!-- 通用符号 -->
    <template v-else-if="name === 'settings'"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/></template>
    <template v-else-if="name === 'search'"><circle cx="10.5" cy="10.5" r="6.5"/><path d="M20 20l-4.6-4.6"/></template>
    <template v-else-if="name === 'flask'"><path d="M9 3h6M10 3v6l-4.6 8.5A1.5 1.5 0 0 0 6.8 20h10.4a1.5 1.5 0 0 0 1.3-2.5L14 9V3"/><path d="M8 15h8"/></template>
    <template v-else-if="name === 'link'"><path d="M9.5 14.5l5-5"/><path d="M11 6.5 12.5 5a4 4 0 0 1 5.5 5.5L16.5 12M7.5 12 6 13.5A4 4 0 0 0 11.5 19L13 17.5"/></template>
    <template v-else-if="name === 'tag'"><path d="M3 12V4h8l9 9-8 8-9-9z"/><circle cx="7.5" cy="7.5" r="1.3" fill="currentColor" stroke="none"/></template>
    <template v-else-if="name === 'alert'"><path d="M12 3 2 20h20L12 3z"/><path d="M12 10v4"/><circle cx="12" cy="17.2" r="0.9" fill="currentColor" stroke="none"/></template>
    <template v-else-if="name === 'sliders'"><path d="M6 4v16M12 4v16M18 4v16"/><rect x="4" y="7" width="4" height="3" rx="1" fill="currentColor" stroke="none"/><rect x="10" y="13" width="4" height="3" rx="1" fill="currentColor" stroke="none"/><rect x="16" y="5.5" width="4" height="3" rx="1" fill="currentColor" stroke="none"/></template>
    <template v-else-if="name === 'ruler'"><rect x="2" y="8" width="20" height="8" rx="1"/><path d="M6 8v3M10 8v4M14 8v3M18 8v4"/></template>
    <template v-else-if="name === 'inbox'"><path d="M4 13l2.5-8h11L20 13v6H4z"/><path d="M4 13h5l1.5 2.5h3L15 13h5"/></template>
    <template v-else-if="name === 'user'"><circle cx="12" cy="8" r="3.6"/><path d="M5 20c0-3.6 3-5.5 7-5.5s7 1.9 7 5.5"/></template>
    <template v-else-if="name === 'flow'"><path d="M4 8h11M15 8l-3-3M15 8l-3 3M20 16H9M9 16l3-3M9 16l3 3"/></template>
    <template v-else-if="name === 'book'"><path d="M5 4h11a2 2 0 0 1 2 2v14H7a2 2 0 0 1-2-2z"/><path d="M5 17.5a2 2 0 0 1 2-1.5h11"/></template>
    <template v-else-if="name === 'robot'"><rect x="5" y="8" width="14" height="10" rx="2"/><circle cx="9.5" cy="13" r="1.2" fill="currentColor" stroke="none"/><circle cx="14.5" cy="13" r="1.2" fill="currentColor" stroke="none"/><path d="M12 4v4M9.5 4h5"/></template>
  </svg>
</template>

<style scoped>
.ico { display: inline-block; vertical-align: middle; flex-shrink: 0; }
</style>
