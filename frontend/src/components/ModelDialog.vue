<script setup lang="ts">
import { ref, computed } from 'vue'
import { useModelStore } from '@/stores/model'

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{ close: [] }>()

const store = useModelStore()
const detailId = ref<string | null>(null)

const detail = computed(() => detailId.value ? store.models.find(m => m.model_id === detailId.value) : null)

function statusClass(s: string) {
  return { deployed: 'on', testing: 'warn', development: 'dev', deprecated: 'off' }[s] || ''
}

/** 模型种类 / 分类 → 线性图标名 */
const KIND_ICON: Record<string, string> = { model: 'model', algorithm: 'settings', rule: 'ruler' }
const CAT_EMOJI_ICON: Record<string, string> = {
  '🔊': 'signal', '🔍': 'search', '🔗': 'link', '🏷️': 'tag',
  '⚠️': 'alert', '🗺️': 'map', '📋': 'list', '🧪': 'flask', '📦': 'box',
}
</script>

<template>
  <Teleport to="body">
    <Transition name="md-fade">
      <div v-if="visible" class="md-overlay" @click.self="emit('close')">
        <div class="md-dialog">
          <!-- 标题 -->
          <div class="md-header">
            <span class="md-icon"><Icon name="model" :size="18" /></span>
            <span>模型管理</span>
            <span class="md-count">{{ store.stats.total }}个模型</span>
            <div class="md-header-right">
              <span class="md-stat">已部署 <strong class="on">{{ store.stats.by_status['deployed'] || 0 }}</strong></span>
              <button class="md-close" @click="emit('close')">✕</button>
            </div>
          </div>

          <!-- 工具栏 -->
          <div class="md-toolbar">
            <div class="md-cats">
              <button class="md-cat" :class="{ active: !store.filterCategory }" @click="store.setFilterCategory(null)">全部</button>
              <button v-for="g in store.groupedByCategory" :key="g.key"
                      class="md-cat" :class="{ active: store.filterCategory === g.key }"
                      @click="store.setFilterCategory(store.filterCategory === g.key ? null : g.key)">
                {{ g.label }}
              </button>
            </div>
            <input v-model="store.searchText" class="md-search" placeholder="搜索模型..." />
          </div>

          <!-- 列表 -->
          <div class="md-body">
            <template v-if="detail">
              <!-- 详情视图 -->
              <button class="md-back" @click="detailId = null">← 返回列表</button>
              <div class="md-detail">
                <div class="mdd-head">
                  <span class="mdd-kind"><Icon :name="KIND_ICON[detail.kind] || 'model'" :size="13" />{{ detail.kind === 'model' ? '模型' : detail.kind === 'algorithm' ? '算法' : '规则引擎' }}</span>
                  <span class="mdd-ver">v{{ detail.version }}</span>
                  <span class="mdd-status" :class="statusClass(detail.status)">{{ store.STATUS_LABELS[detail.status] }}</span>
                </div>
                <h3>{{ detail.name }} <small>{{ detail.name_en }}</small></h3>
                <p>{{ detail.description }}</p>

                <div v-if="Object.values(detail.metrics).some(v => v !== undefined && v !== -1)" class="mdd-section">
                  <div class="mdd-st"><Icon name="chart" :size="13" />性能指标</div>
                  <div class="mdd-metrics">
                    <div v-if="detail.metrics.accuracy" class="mdd-m"><span class="mdd-mv">{{ (detail.metrics.accuracy*100).toFixed(1) }}%</span><span>准确率</span></div>
                    <div v-if="detail.metrics.precision" class="mdd-m"><span class="mdd-mv">{{ (detail.metrics.precision*100).toFixed(1) }}%</span><span>精确率</span></div>
                    <div v-if="detail.metrics.recall" class="mdd-m"><span class="mdd-mv">{{ (detail.metrics.recall*100).toFixed(1) }}%</span><span>召回率</span></div>
                    <div v-if="detail.metrics.f1_score" class="mdd-m"><span class="mdd-mv">{{ (detail.metrics.f1_score*100).toFixed(1) }}%</span><span>F1</span></div>
                    <div v-if="typeof detail.metrics.latency_ms === 'number' && detail.metrics.latency_ms > 0" class="mdd-m"><span class="mdd-mv">{{ detail.metrics.latency_ms }}ms</span><span>延迟</span></div>
                  </div>
                </div>

                <div v-if="detail.parameters.length" class="mdd-section">
                  <div class="mdd-st"><Icon name="sliders" :size="13" />参数</div>
                  <div v-for="p in detail.parameters" :key="p.key" class="mdd-param">
                    <span class="mdd-pk">{{ p.label }}</span>
                    <template v-if="p.type === 'number'">
                      <input type="range" :min="p.min" :max="p.max" :step="p.step" :value="p.value" class="mdd-range"
                             @input="store.updateModelParam(detail.model_id, p.key, parseFloat(($event.target as HTMLInputElement).value))" />
                      <span class="mdd-pv">{{ p.value }}</span>
                    </template>
                    <template v-else-if="p.type === 'select'">
                      <select :value="p.value" class="mdd-sel" @change="store.updateModelParam(detail.model_id, p.key, ($event.target as HTMLSelectElement).value)">
                        <option v-for="o in p.options" :key="o.value" :value="o.value">{{ o.label }}</option>
                      </select>
                    </template>
                    <template v-else-if="p.type === 'boolean'">
                      <input type="checkbox" :checked="p.value" @change="store.updateModelParam(detail.model_id, p.key, ($event.target as HTMLInputElement).checked)" />
                    </template>
                  </div>
                </div>

                <div v-if="detail.dependencies.length" class="mdd-section">
                  <div class="mdd-st"><Icon name="link" :size="13" />依赖</div>
                  <div class="mdd-deps">
                    <span v-for="d in detail.dependencies" :key="d" class="mdd-dep">{{ store.models.find(m=>m.model_id===d)?.name || d }}</span>
                  </div>
                </div>
              </div>
            </template>

            <template v-else>
              <!-- 列表视图 -->
              <div v-for="g in store.groupedByCategory" :key="g.key" class="md-group"
                   v-show="!store.filterCategory || store.filterCategory === g.key">
                <div class="md-group-title"><Icon :name="CAT_EMOJI_ICON[g.icon] || 'box'" :size="13" />{{ g.label }} <span class="g-n">{{ g.items.length }}</span></div>
                <div v-for="m in g.items.filter(x => !store.searchText || x.name.includes(store.searchText) || x.name_en.toLowerCase().includes(store.searchText.toLowerCase()))"
                     :key="m.model_id" class="md-card" @click="detailId = m.model_id">
                  <div class="mdc-left">
                    <span class="mdc-kind"><Icon :name="KIND_ICON[m.kind] || 'model'" :size="15" /></span>
                    <div class="mdc-info">
                      <div class="mdc-name">{{ m.name }}</div>
                      <div class="mdc-en">{{ m.name_en }}</div>
                    </div>
                  </div>
                  <div class="mdc-right">
                    <span class="mdc-stat" :class="statusClass(m.status)">{{ store.STATUS_LABELS[m.status] }}</span>
                    <span class="mdc-acc" v-if="m.metrics.accuracy">{{ (m.metrics.accuracy*100).toFixed(0) }}%</span>
                  </div>
                </div>
              </div>
            </template>
          </div>

          <div class="md-footer">
            <button @click="emit('close')">关闭</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* ===== Overlay & Dialog ===== */
.md-overlay {
  position:fixed; inset:0; background:rgba(0,0,0,0.4); backdrop-filter:blur(8px) saturate(1.1);
  -webkit-backdrop-filter:blur(8px) saturate(1.1);
  display:flex; align-items:center; justify-content:center; z-index:9999;
}
.md-dialog {
  width:min(800px,92vw); max-height:85vh;
  background:var(--bg-surface-3); border:1px solid var(--stroke-divider); border-radius:var(--radius-xl);
  box-shadow:var(--shadow-64);
  display:flex; flex-direction:column; overflow:hidden;
}

/* Header */
.md-header {
  display:flex; align-items:center; gap:8px;
  padding:var(--sp-l) var(--sp-xl); border-bottom:1px solid var(--divider);
  background:var(--bg-surface-2); flex-shrink:0;
  font-size:16px; font-weight:700; color:var(--fg-1);
}
.md-icon { font-size:20px; }
.md-count { padding:1px 10px; background:var(--brand-subtle); border-radius:var(--radius-circular); font-size:12px; font-weight:600; color:var(--brand-fg); }
.md-header-right { margin-left:auto; display:flex; align-items:center; gap:10px; font-size:12px; color:var(--fg-3); }
.md-stat strong.on { color:var(--success); }
.md-close {
  width:28px; height:28px; display:flex; align-items:center; justify-content:center;
  border:1px solid transparent; border-radius:var(--radius-md);
  background:transparent; color:var(--fg-3); font-size:14px; cursor:pointer;
  transition:background var(--dur-normal) var(--ease-fluent);
}
.md-close:hover { background:var(--danger-bg); color:var(--danger); }

/* Toolbar */
.md-toolbar { display:flex; gap:var(--sp-s); padding:var(--sp-m) var(--sp-xl); border-bottom:1px solid var(--stroke-divider); background:var(--bg-surface); flex-shrink:0; }
.md-cats { display:flex; flex-wrap:wrap; gap:var(--sp-xs); }
.md-cat {
  padding:4px var(--sp-m); border:1px solid var(--stroke-control); border-radius:var(--radius-md);
  background:transparent; color:var(--fg-3); font-size:12px; cursor:pointer; white-space:nowrap;
  transition:background var(--dur-normal) var(--ease-fluent), color var(--dur-normal) var(--ease-fluent);
}
.md-cat:hover { background:var(--bg-subtle-hover); color:var(--fg-1); }
.md-cat.active { background:var(--brand-selected); border-color:transparent; color:var(--brand-fg); }
.md-search { flex:1; max-width:200px; height:32px; padding:0 var(--sp-m); background:var(--bg-surface); border:1px solid var(--stroke-control); border-bottom-color:var(--fg-4); border-radius:var(--radius-md); color:var(--fg-1); font-size:13px; outline:none; margin-left:auto; font-family:var(--font-base); transition:border-color var(--dur-normal) var(--ease-fluent); }
.md-search:focus { border-color:var(--stroke-control); border-bottom:2px solid var(--brand-fg); }
.md-search::placeholder { color:var(--fg-4); }

/* Body */
.md-body { flex:1; overflow-y:auto; padding:var(--sp-s) var(--sp-l); }
.md-body::-webkit-scrollbar { width:8px; }
.md-body::-webkit-scrollbar-thumb { background:var(--stroke-control); border-radius:var(--radius-circular); border:2px solid transparent; background-clip:padding-box; }

.md-group-title { font-size:12px; font-weight:600; color:var(--fg-2); padding:var(--sp-s) 0 var(--sp-xs); display:flex; align-items:center; gap:6px; }
.g-n { font-size:10px; color:var(--fg-4); font-weight:400; }

.md-card {
  display:flex; align-items:center; justify-content:space-between;
  padding:var(--sp-s) 10px; margin-bottom:2px; background:var(--bg-surface-2);
  border:1px solid var(--stroke-divider); border-radius:var(--radius-md); cursor:pointer; transition:background var(--dur-normal) var(--ease-fluent), border-color var(--dur-normal) var(--ease-fluent);
}
.md-card:hover { background:var(--bg-subtle-hover); border-color:var(--stroke-control); }
.mdc-left { display:flex; align-items:center; gap:8px; }
.mdc-kind { font-size:16px; }
.mdc-name { font-size:12px; font-weight:600; color:var(--fg-1); }
.mdc-en { font-size:10px; color:var(--fg-4); font-style:italic; }
.mdc-right { display:flex; align-items:center; gap:6px; }
.mdc-stat { padding:2px var(--sp-s); border-radius:var(--radius-md); font-size:10px; font-weight:600; }
.mdc-stat.on { background:var(--success-bg); color:var(--success); }
.mdc-stat.warn { background:var(--warning-bg); color:var(--warning); }
.mdc-stat.dev { background:var(--brand-subtle); color:var(--brand-fg); }
.mdc-stat.off { background:var(--danger-bg); color:var(--danger); }
.mdc-acc { font-size:11px; font-weight:600; color:var(--brand-fg); }

/* Detail */
.md-back { height:28px; padding:0 var(--sp-m); margin:var(--sp-xs) 0; background:var(--bg-surface-3); border:1px solid var(--stroke-control); border-radius:var(--radius-md); color:var(--fg-1); font-size:12px; font-weight:600; cursor:pointer; transition:background var(--dur-normal) var(--ease-fluent); }
.md-back:hover { background:var(--bg-subtle-hover); }
.md-detail h3 { font-size:16px; color:var(--fg-1); margin:8px 0; }
.md-detail h3 small { font-size:11px; color:var(--fg-4); font-style:italic; }
.md-detail p { font-size:12px; color:var(--fg-3); line-height:1.5; }
.mdd-head { display:flex; gap:8px; align-items:center; }
.mdd-kind { display:inline-flex; align-items:center; gap:4px; }
.mdd-ver { font-size:11px; color:var(--fg-4); font-family:var(--font-mono); }
.mdd-status { padding:2px var(--sp-s); border-radius:var(--radius-md); font-size:10px; font-weight:600; }
.mdd-status.on { background:var(--success-bg); color:var(--success); }
.mdd-status.warn { background:var(--warning-bg); color:var(--warning); }
.mdd-status.dev { background:var(--brand-subtle); color:var(--brand-fg); }
.mdd-status.off { background:var(--danger-bg); color:var(--danger); }

.mdd-section { margin-top:14px; }
.mdd-st { display:flex; align-items:center; gap:5px; font-size:12px; font-weight:600; color:var(--fg-2); margin-bottom:6px; }
.mdd-metrics { display:flex; gap:6px; flex-wrap:wrap; }
.mdd-m { padding:6px var(--sp-m); background:var(--bg-surface-2); border:1px solid var(--stroke-divider); border-radius:var(--radius-md); text-align:center; font-size:10px; color:var(--fg-3); }
.mdd-mv { display:block; font-size:15px; font-weight:700; color:var(--brand-fg); }

.mdd-param { display:flex; align-items:center; gap:6px; padding:4px 0; }
.mdd-pk { font-size:11px; color:var(--fg-2); min-width:70px; flex-shrink:0; }
.mdd-range { flex:1; accent-color:var(--brand-fg); height:3px; }
.mdd-pv { font-size:12px; font-weight:600; color:var(--brand-fg); min-width:32px; text-align:right; }
.mdd-sel { flex:1; height:28px; padding:0 6px; background:var(--bg-surface); border:1px solid var(--stroke-control); border-radius:var(--radius-md); color:var(--fg-1); font-size:11px; outline:none; font-family:var(--font-base); }

.mdd-deps { display:flex; gap:4px; flex-wrap:wrap; }
.mdd-dep { padding:2px var(--sp-s); background:var(--brand-subtle); border-radius:var(--radius-md); font-size:10px; color:var(--brand-fg); }

/* Footer */
.md-footer { display:flex; justify-content:flex-end; padding:var(--sp-m) var(--sp-xl); border-top:1px solid var(--divider); background:var(--bg-surface-2); flex-shrink:0; }
.md-footer button { height:32px; padding:0 var(--sp-l); border:1px solid var(--stroke-control); border-radius:var(--radius-md); background:var(--bg-surface-3); color:var(--fg-1); font-size:14px; font-weight:600; cursor:pointer; transition:background var(--dur-normal) var(--ease-fluent); }
.md-footer button:hover { background:var(--bg-subtle-hover); }

.md-fade-enter-active,.md-fade-leave-active { transition:opacity var(--dur-slow) var(--ease-fluent); }
.md-fade-enter-from,.md-fade-leave-to { opacity:0; }
.md-fade-enter-active .md-dialog { transition:transform var(--dur-slow) var(--ease-decel); }
.md-fade-enter-from .md-dialog { transform:scale(0.98) translateY(8px); }
</style>
