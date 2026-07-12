<script setup lang="ts">
import { ref, computed } from 'vue'
import { useModelStore } from '@/stores/model'

const store = useModelStore()
const detailModelId = ref<string | null>(null)

const filtered = computed(() => store.getFilteredModels())
const detail = computed(() =>
  detailModelId.value ? store.models.find(m => m.model_id === detailModelId.value) : null
)

function openDetail(id: string) { detailModelId.value = id }
function closeDetail() { detailModelId.value = null }

function statusClass(s: string) {
  return { deployed: 'on', testing: 'warn', development: 'dev', deprecated: 'off' }[s] || ''
}

const KIND_ICON: Record<string, string> = { model: 'model', algorithm: 'settings', rule: 'ruler' }
const CAT_EMOJI_ICON: Record<string, string> = {
  '🔊': 'signal', '🔍': 'search', '🔗': 'link', '🏷️': 'tag',
  '⚠️': 'alert', '🗺️': 'map', '📋': 'list', '🧪': 'flask', '📦': 'box',
}
</script>

<template>
  <div class="mv-page">
    <!-- 头部 -->
    <div class="mv-header">
      <h2><Icon name="model" :size="18" />模型管理</h2>
      <div class="mv-stats">
        <span>共 <strong>{{ store.stats.total }}</strong> 个模型</span>
        <span class="sep">|</span>
        <span>已部署 <strong class="on">{{ store.stats.by_status['deployed'] || 0 }}</strong></span>
        <span class="sep">|</span>
        <span>测试中 <strong class="warn">{{ store.stats.by_status['testing'] || 0 }}</strong></span>
        <span class="sep">|</span>
        <span>开发中 <strong class="dev">{{ store.stats.by_status['development'] || 0 }}</strong></span>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="mv-toolbar">
      <div class="mv-cats">
        <button class="mv-cat" :class="{ active: !store.filterCategory }" @click="store.setFilterCategory(null)">全部</button>
        <button v-for="g in store.groupedByCategory" :key="g.key"
                class="mv-cat"
                :class="{ active: store.filterCategory === g.key }"
                @click="store.setFilterCategory(store.filterCategory === g.key ? null : g.key)">
          {{ g.icon }} {{ g.label }}
          <span class="cat-n">{{ g.items.length }}</span>
        </button>
      </div>
      <div class="mv-search">
        <input v-model="store.searchText" class="mv-search-input" placeholder="搜索模型名称/英文名/描述..." />
      </div>
    </div>

    <!-- 模型列表 -->
    <div class="mv-body">
      <template v-for="g in store.groupedByCategory" :key="g.key">
        <div v-if="!store.filterCategory || store.filterCategory === g.key" class="mv-group">
          <div class="mv-group-title">
            <span><Icon :name="CAT_EMOJI_ICON[g.icon] || 'box'" :size="14" /> {{ g.label }}</span>
            <span class="g-n">{{ g.items.length }}个</span>
          </div>
          <div class="mv-grid">
            <div v-for="m in store.getFilteredModels().filter(x => x.category === g.key)" :key="m.model_id"
                 class="mv-card" :class="{ selected: detailModelId === m.model_id }"
                 @click="openDetail(m.model_id)">
              <div class="mvc-head">
                <span class="mvc-kind" :class="m.kind"><Icon :name="KIND_ICON[m.kind] || 'model'" :size="15" /></span>
                <div class="mvc-info">
                  <div class="mvc-name">{{ m.name }}</div>
                  <div class="mvc-en">{{ m.name_en }}</div>
                </div>
                <span class="mvc-status" :class="statusClass(m.status)">{{ store.STATUS_LABELS[m.status] || m.status }}</span>
              </div>
              <div class="mvc-body">
                <p class="mvc-desc">{{ m.description }}</p>
                <div class="mvc-meta">
                  <span>v{{ m.version }}</span>
                  <span v-if="m.metrics.accuracy">准确率 {{ (m.metrics.accuracy * 100).toFixed(0) }}%</span>
                  <span v-if="typeof m.metrics.latency_ms === 'number' && m.metrics.latency_ms > 0">延迟 {{ m.metrics.latency_ms }}ms</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </template>

      <div v-if="filtered.length === 0" class="mv-empty">暂无匹配的模型</div>
    </div>

    <!-- 模型详情抽屉 -->
    <Transition name="drawer">
      <div v-if="detail" class="mv-drawer-overlay" @click.self="closeDetail">
        <div class="mv-drawer">
          <div class="mvd-header">
            <span class="mvd-kind"><Icon :name="KIND_ICON[detail.kind] || 'model'" :size="13" />{{ detail.kind === 'model' ? '模型' : detail.kind === 'algorithm' ? '算法' : '规则引擎' }}</span>
            <span class="mvd-ver">v{{ detail.version }}</span>
            <span class="mvd-status" :class="statusClass(detail.status)">{{ store.STATUS_LABELS[detail.status] }}</span>
            <button class="mvd-close" @click="closeDetail">✕</button>
          </div>

          <div class="mvd-body">
            <h3>{{ detail.name }} <small>{{ detail.name_en }}</small></h3>
            <p>{{ detail.description }}</p>

            <!-- 性能指标 -->
            <div v-if="Object.keys(detail.metrics).length" class="mvd-section">
              <div class="mvd-section-title"><Icon name="chart" :size="13" />性能指标</div>
              <div class="mvd-metrics">
                <div v-if="detail.metrics.accuracy" class="mvd-m">
                  <span class="mvd-mv">{{ (detail.metrics.accuracy * 100).toFixed(1) }}%</span><span class="mvd-ml">准确率</span>
                </div>
                <div v-if="detail.metrics.precision" class="mvd-m">
                  <span class="mvd-mv">{{ (detail.metrics.precision * 100).toFixed(1) }}%</span><span class="mvd-ml">精确率</span>
                </div>
                <div v-if="detail.metrics.recall" class="mvd-m">
                  <span class="mvd-mv">{{ (detail.metrics.recall * 100).toFixed(1) }}%</span><span class="mvd-ml">召回率</span>
                </div>
                <div v-if="detail.metrics.f1_score" class="mvd-m">
                  <span class="mvd-mv">{{ (detail.metrics.f1_score * 100).toFixed(1) }}%</span><span class="mvd-ml">F1</span>
                </div>
                <div v-if="detail.metrics.latency_ms && detail.metrics.latency_ms > 0" class="mvd-m">
                  <span class="mvd-mv">{{ detail.metrics.latency_ms }}ms</span><span class="mvd-ml">延迟</span>
                </div>
              </div>
            </div>

            <!-- 输入/输出 -->
            <div class="mvd-section" v-if="detail.input_desc || detail.output_desc">
              <div class="mvd-section-title"><Icon name="flow" :size="13" />数据流</div>
              <div class="mvd-io">
                <div><strong>输入：</strong>{{ detail.input_desc }}</div>
                <div><strong>输出：</strong>{{ detail.output_desc }}</div>
              </div>
            </div>

            <!-- 可调参数 -->
            <div v-if="detail.parameters.length" class="mvd-section">
              <div class="mvd-section-title"><Icon name="sliders" :size="13" />参数配置</div>
              <div class="mvd-params">
                <div v-for="p in detail.parameters" :key="p.key" class="mvd-param">
                  <div class="mvd-phead">
                    <span class="mvd-pkey">{{ p.label }}</span>
                    <span class="mvd-pdesc" v-if="p.description">{{ p.description }}</span>
                  </div>
                  <div class="mvd-pctrl">
                    <!-- number -->
                    <template v-if="p.type === 'number'">
                      <input type="range" :min="p.min" :max="p.max" :step="p.step"
                             :value="p.value" @input="store.updateModelParam(detail.model_id, p.key, parseFloat(($event.target as HTMLInputElement).value))" />
                      <span class="mvd-pval">{{ p.value }}</span>
                    </template>
                    <!-- select -->
                    <template v-else-if="p.type === 'select'">
                      <select :value="p.value" @change="store.updateModelParam(detail.model_id, p.key, ($event.target as HTMLSelectElement).value)">
                        <option v-for="o in p.options" :key="o.value" :value="o.value">{{ o.label }}</option>
                      </select>
                    </template>
                    <!-- boolean -->
                    <template v-else-if="p.type === 'boolean'">
                      <input type="checkbox" :checked="p.value" @change="store.updateModelParam(detail.model_id, p.key, ($event.target as HTMLInputElement).checked)" />
                    </template>
                    <!-- string -->
                    <template v-else>
                      <input type="text" :value="p.value" @input="store.updateModelParam(detail.model_id, p.key, ($event.target as HTMLInputElement).value)" />
                    </template>
                  </div>
                </div>
              </div>
            </div>

            <!-- 依赖 -->
            <div v-if="detail.dependencies.length" class="mvd-section">
              <div class="mvd-section-title"><Icon name="link" :size="13" />依赖模型</div>
              <div class="mvd-deps">
                <span v-for="depId in detail.dependencies" :key="depId" class="mvd-dep">
                  {{ store.models.find(m => m.model_id === depId)?.name || depId }}
                </span>
              </div>
            </div>

            <!-- 参考文献 -->
            <div v-if="detail.references.length" class="mvd-section">
              <div class="mvd-section-title"><Icon name="book" :size="13" />参考文献</div>
              <div class="mvd-refs">
                <div v-for="ref in detail.references" :key="ref" class="mvd-ref">{{ ref }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.mv-page { height:100%; display:flex; flex-direction:column; overflow:hidden; background:rgba(6,11,24,0.5); }

.mv-header { display:flex; align-items:center; gap:16px; padding:16px 20px; flex-shrink:0; }
.mv-header h2 { font-size:16px; font-weight:700; color:#e0e8f0; margin:0; }
.mv-stats { font-size:11px; color:#607d8b; display:flex; gap:8px; }
.mv-stats strong.on { color:#66bb6a; }
.mv-stats strong.warn { color:#ffd54f; }
.mv-stats strong.dev { color:#4fc3f7; }
.sep { color:#37474f; }

.mv-toolbar { padding:0 20px 10px; flex-shrink:0; }
.mv-cats { display:flex; flex-wrap:wrap; gap:4px; margin-bottom:8px; }
.mv-cat {
  padding:3px 10px; border:1px solid rgba(255,255,255,0.04); border-radius:4px;
  background:transparent; color:#78909c; font-size:11px; cursor:pointer; white-space:nowrap;
  display:flex; align-items:center; gap:3px; transition:all 0.15s;
}
.mv-cat:hover { border-color:rgba(79,195,247,0.12); color:#b0bec5; }
.mv-cat.active { background:rgba(79,195,247,0.08); border-color:rgba(79,195,247,0.18); color:#4fc3f7; }
.cat-n { padding:0 5px; background:rgba(79,195,247,0.08); border-radius:6px; font-size:10px; color:#4fc3f7; }

.mv-search-input {
  width:100%; padding:6px 12px; background:rgba(0,0,0,0.2); border:1px solid rgba(255,255,255,0.06);
  border-radius:4px; color:#b0bec5; font-size:12px; outline:none;
}
.mv-search-input:focus { border-color:rgba(79,195,247,0.2); }

.mv-body { flex:1; overflow-y:auto; padding:0 20px 20px; }
.mv-body::-webkit-scrollbar { width:4px; }
.mv-body::-webkit-scrollbar-thumb { background:rgba(79,195,247,0.08); border-radius:2px; }
.mv-empty { text-align:center; padding:60px; color:#546e7a; }

.mv-group-title { font-size:13px; font-weight:600; color:#90a4ae; margin:12px 0 8px; display:flex; align-items:center; gap:8px; }
.g-n { font-size:10px; color:#546e7a; font-weight:400; }

.mv-grid { display:grid; grid-template-columns:repeat(auto-fill,minmax(300px,1fr)); gap:6px; margin-bottom:8px; }
.mv-card {
  background:rgba(13,25,48,0.5); border:1px solid rgba(79,195,247,0.05); border-radius:6px;
  cursor:pointer; transition:all 0.15s; padding:10px 12px;
}
.mv-card:hover { border-color:rgba(79,195,247,0.12); background:rgba(79,195,247,0.03); }
.mv-card.selected { border-color:rgba(79,195,247,0.2); background:rgba(79,195,247,0.05); }

.mvc-head { display:flex; align-items:center; gap:8px; margin-bottom:6px; }
.mvc-kind { font-size:16px; }
.mvc-info { flex:1; min-width:0; }
.mvc-name { font-size:13px; font-weight:600; color:#e0e0e0; }
.mvc-en { font-size:10px; color:#546e7a; font-style:italic; }
.mvc-status { padding:2px 8px; border-radius:3px; font-size:10px; flex-shrink:0; }
.mvc-status.on { background:rgba(76,175,80,0.12); color:#66bb6a; }
.mvc-status.warn { background:rgba(255,213,79,0.12); color:#ffd54f; }
.mvc-status.dev { background:rgba(79,195,247,0.12); color:#4fc3f7; }
.mvc-status.off { background:rgba(239,83,80,0.12); color:#ef5350; }

.mvc-desc { font-size:11px; color:#78909c; margin:0 0 4px; line-height:1.4; }
.mvc-meta { display:flex; gap:10px; font-size:10px; color:#546e7a; }

/* ---- 详情抽屉 ---- */
.mv-drawer-overlay { position:fixed; inset:0; background:rgba(0,0,0,0.4); z-index:10000; display:flex; justify-content:flex-end; }
.mv-drawer {
  width:min(480px,90vw); height:100%; background:#0f1b2e;
  border-left:1px solid rgba(79,195,247,0.1); display:flex; flex-direction:column;
  box-shadow:-8px 0 32px rgba(0,0,0,0.4); overflow:hidden;
}
.mvd-header { display:flex; align-items:center; gap:8px; padding:14px 16px; border-bottom:1px solid rgba(79,195,247,0.08); flex-shrink:0; }
.mvd-ver { font-size:11px; color:#546e7a; }
.mvd-close { margin-left:auto; width:28px; height:28px; border:1px solid rgba(255,255,255,0.08); border-radius:4px; background:transparent; color:#78909c; cursor:pointer; }
.mvd-close:hover { background:rgba(239,83,80,0.12); color:#ef5350; }

.mvd-body { flex:1; overflow-y:auto; padding:16px; }
.mvd-body h3 { font-size:16px; color:#e0e8f0; margin:0 0 8px; }
.mvd-body h3 small { font-size:11px; color:#546e7a; font-style:italic; }
.mvd-body p { font-size:12px; color:#78909c; line-height:1.5; }

.mvd-section { margin-top:16px; }
.mvd-section-title { font-size:12px; font-weight:600; color:#90a4ae; margin-bottom:8px; }

.mvd-metrics { display:flex; flex-wrap:wrap; gap:6px; }
.mvd-m { text-align:center; background:rgba(0,0,0,0.2); border-radius:4px; padding:8px 12px; min-width:60px; }
.mvd-mv { display:block; font-size:16px; font-weight:700; color:#4fc3f7; }
.mvd-ml { font-size:10px; color:#546e7a; }

.mvd-io { font-size:11px; color:#607d8b; }
.mvd-io div { margin:4px 0; }

.mvd-params { display:flex; flex-direction:column; gap:8px; }
.mvd-param { background:rgba(0,0,0,0.15); border-radius:4px; padding:6px 10px; }
.mvd-phead { display:flex; gap:8px; align-items:baseline; margin-bottom:4px; }
.mvd-pkey { font-size:11px; font-weight:600; color:#b0bec5; }
.mvd-pdesc { font-size:10px; color:#546e7a; }
.mvd-pctrl { display:flex; align-items:center; gap:8px; }
.mvd-pctrl input[type="range"] { flex:1; accent-color:#4fc3f7; height:4px; }
.mvd-pctrl select { flex:1; padding:4px 8px; background:rgba(0,0,0,0.3); border:1px solid rgba(255,255,255,0.06); border-radius:3px; color:#b0bec5; font-size:11px; outline:none; }
.mvd-pctrl input[type="text"] { flex:1; padding:4px 8px; background:rgba(0,0,0,0.3); border:1px solid rgba(255,255,255,0.06); border-radius:3px; color:#b0bec5; font-size:11px; outline:none; }
.mvd-pval { font-size:12px; font-weight:600; color:#4fc3f7; min-width:36px; text-align:right; }

.mvd-deps { display:flex; flex-wrap:wrap; gap:4px; }
.mvd-dep { padding:2px 8px; background:rgba(79,195,247,0.06); border-radius:3px; font-size:10px; color:#4fc3f7; }

.mvd-refs { font-size:10px; color:#546e7a; }
.mvd-ref { padding:2px 0; }

.drawer-enter-active, .drawer-leave-active { transition: all 0.25s ease; }
.drawer-enter-from, .drawer-leave-to { transform: translateX(100%); opacity:0; }
</style>
