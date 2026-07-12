<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import type { DeviceInfo, UnifiedDeviceType } from '@/types'

// ---- Props & Emits ----
const props = defineProps<{
  visible: boolean
  device: DeviceInfo | null
}>()
const emit = defineEmits<{
  close: []
  test: [deviceId: string, cmd: string, params: Record<string, any>]
}>()

// ---- 状态 ----
const activeSection = ref<string>('')

// 设备类型 → 线性图标名
const DEVICE_ICON: Record<string, string> = {
  RADAR: 'radar', RF: 'signal', EO_IR: 'camera', ACOUSTIC: 'mic', LIDAR: 'bulb',
  CUAS_RADAR: 'satellite', CUAS_RADIO_FREQ: 'signal', CUAS_OPTICAL: 'scope',
  CUAS_INFRARED: 'thermal', CUAS_JAMMER: 'bolt', CUAS_SPOOFER: 'refresh', CUAS_CONTROL_BOX: 'box',
}
function deviceIcon(kind: string) { return DEVICE_ICON[kind] || 'device' }

// ---- 各设备类型参数配置 ----
// 干扰频段
const jamBands = ref({
  band58G: false, band24G: false, band900M: false, band14G: false, band52G: false,
})
// 攻击模式
const attackMode = ref<'rtn' | 'land'>('rtn')  // 返航/迫降
// GNSS诱骗
const gnssEnabled = ref(false)
const gnssMode = ref<'expel' | 'land' | 'nofly' | 'navsuppress'>('expel')
const gnssDirection = ref<'away' | 'near'>('away')
const gnssAzimuth = ref(0)
const gnssSpeed = ref(10)
// 禁飞区
const noflyLat = ref(34.08)
const noflyLon = ref(108.94)
const noflyAlt = ref(0)
// 迫降区
const landLat = ref(34.08)
const landLon = ref(108.94)
const landAlt = ref(0)
const landRadius = ref(100)
// 云台
const ptzMode = ref<'eo' | 'jam' | 'disable'>('eo')
const ptzPan = ref(0)
const ptzTilt = ref(-15)
const cameraOp = ref<string>('stop')
const camHSpeed = ref(5)
const camVSpeed = ref(5)
const camZoom = ref(3)
// 探测
const detectionOn = ref(false)
// 白名单
const whitelistEnabled = ref(false)
const whitelistMode = ref<'signal' | 'droneId'>('signal')
const whitelistText = ref('')
// 无人值守
const autoMode = ref(false)
const autoLink = ref(false)
// 箱仓
const boxState = ref<'close' | 'open'>('close')
// 透传
const passthroughData = ref('')

// ---- 测试日志 ----
interface TestLog { time: string; cmd: string; result: string; ok: boolean }
const testLogs = ref<TestLog[]>([])
const testRunning = ref(false)

// ---- 根据设备类型激活默认配置区 ----
const deviceKind = computed(() => props.device?.device_kind ?? '')

watch(() => props.device?.device_id, () => {
  activeSection.value = ''
  testLogs.value = []
})

const isCuas = computed(() => props.device?.source === 'cuas_system')

// ---- 操作 ----
function addLog(cmd: string, result: string, ok: boolean) {
  testLogs.value.unshift({
    time: new Date().toLocaleTimeString('zh-CN'),
    cmd, result, ok,
  })
}

/** 发送测试指令（模拟，参数回显） */
async function runTest(cmdName: string, cmdCode: string, params: Record<string, any>) {
  testRunning.value = true
  addLog(cmdName, `发送中... (0x${cmdCode})`, true)
  await new Promise(r => setTimeout(r, 600))

  const paramStr = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null)
    .map(([k, v]) => `${k}=${typeof v === 'boolean' ? (v ? '开启' : '关闭') : v}`)
    .join(', ')
  addLog(cmdName, `执行成功 → ${paramStr || '无参数'}`, true)

  emit('test', props.device!.device_id, cmdCode, params)
  testRunning.value = false
}

/** 通用开关测试 */
async function testToggle(cmd: string, name: string, val: boolean) {
  await runTest(name, cmd, { enable: val })
}
</script>

<template>
  <Teleport to="body">
    <Transition name="cfg-fade">
      <div v-if="visible && device" class="cfg-overlay" @click.self="emit('close')">
        <div class="cfg-dialog">
          <!-- 标题 -->
          <div class="cfg-header">
            <span class="cfg-icon"><Icon :name="deviceIcon(device.device_kind)" :size="20" /></span>
            <div class="cfg-title">
              <span class="cfg-name">{{ device.name }}</span>
              <span class="cfg-model">{{ device.model }}</span>
              <span class="cfg-src" :class="device.source">{{ device.source === 'cuas_system' ? '反无系统' : 'HSimC2' }}</span>
            </div>
            <div class="cfg-status">
              <span class="cfg-led" :class="device.online ? 'on' : 'off'"></span>
              {{ device.status_label }}
            </div>
            <button class="cfg-close" @click="emit('close')">✕</button>
          </div>

          <!-- 配置/测试 双栏 -->
          <div class="cfg-body">
            <!-- ====== 左栏：参数配置 ====== -->
            <div class="cfg-left">
              <div class="cfg-section-title"><Icon name="settings" :size="14" />参数配置</div>

              <!-- ====== 雷达探测设备(0) ====== -->
              <template v-if="deviceKind === 'CUAS_RADAR'">
                <div class="cfg-block">
                  <div class="cfg-block-title">探测控制 (0x20)</div>
                  <label class="cfg-row"><span>目标探测</span><input type="checkbox" v-model="detectionOn" /></label>
                </div>
              </template>

              <!-- ====== 无线电侦测(1) ====== -->
              <template v-if="deviceKind === 'CUAS_RADIO_FREQ'">
                <div class="cfg-block">
                  <div class="cfg-block-title">探测控制 (0x20)</div>
                  <label class="cfg-row"><span>目标探测</span><input type="checkbox" v-model="detectionOn" /></label>
                </div>
                <div class="cfg-block">
                  <div class="cfg-block-title">电磁白名单 (0xC0)</div>
                  <label class="cfg-row"><span>启用白名单</span><input type="checkbox" v-model="whitelistEnabled" /></label>
                  <label class="cfg-row">
                    <span>模式</span>
                    <select v-model="whitelistMode" class="cfg-select">
                      <option value="signal">电磁信号</option>
                      <option value="droneId">飞行器ID</option>
                    </select>
                  </label>
                  <label class="cfg-row col">
                    <span>白名单</span>
                    <textarea v-model="whitelistText" class="cfg-input" placeholder="{Phantom 3/4:[(2446,2446)]}" rows="2"/>
                  </label>
                </div>
              </template>

              <!-- ====== 光电跟踪(2) ====== -->
              <template v-if="deviceKind === 'CUAS_OPTICAL'">
                <div class="cfg-block">
                  <div class="cfg-block-title">云台控制模式 (0x10)</div>
                  <label class="cfg-row">
                    <span>控制权</span>
                    <select v-model="ptzMode" class="cfg-select">
                      <option value="eo">光电设备控制</option>
                      <option value="jam">干扰设备控制</option>
                      <option value="disable">禁用联动</option>
                    </select>
                  </label>
                </div>
                <div class="cfg-block">
                  <div class="cfg-block-title">转台定位 (0x71)</div>
                  <div class="cfg-row-2">
                    <label class="cfg-row"><span>水平°</span><input type="number" v-model.number="ptzPan" class="cfg-num" step="0.1"/></label>
                    <label class="cfg-row"><span>俯仰°</span><input type="number" v-model.number="ptzTilt" class="cfg-num" step="0.1"/></label>
                  </div>
                </div>
                <div class="cfg-block">
                  <div class="cfg-block-title">云镜控制 (0x72)</div>
                  <label class="cfg-row">
                    <span>方向</span>
                    <select v-model="cameraOp" class="cfg-select">
                      <option value="stop">停止</option><option value="up">上</option><option value="down">下</option>
                      <option value="left">左</option><option value="right">右</option>
                      <option value="leftup">左上</option><option value="leftdown">左下</option>
                      <option value="rightup">右上</option><option value="rightdown">右下</option>
                      <option value="zoomin">放大</option><option value="zoomout">缩小</option>
                    </select>
                  </label>
                  <div class="cfg-row-3">
                    <label class="cfg-row"><span>水平速</span><input type="number" v-model.number="camHSpeed" class="cfg-num" min="0" max="255"/></label>
                    <label class="cfg-row"><span>俯仰速</span><input type="number" v-model.number="camVSpeed" class="cfg-num" min="0" max="255"/></label>
                    <label class="cfg-row"><span>变倍</span><input type="number" v-model.number="camZoom" class="cfg-num" min="0" max="255"/></label>
                  </div>
                </div>
              </template>

              <!-- ====== 红外设备(3) ====== -->
              <template v-if="deviceKind === 'CUAS_INFRARED'">
                <div class="cfg-block">
                  <div class="cfg-block-title">转台定位 (0x71)</div>
                  <div class="cfg-row-2">
                    <label class="cfg-row"><span>水平°</span><input type="number" v-model.number="ptzPan" class="cfg-num"/></label>
                    <label class="cfg-row"><span>俯仰°</span><input type="number" v-model.number="ptzTilt" class="cfg-num"/></label>
                  </div>
                </div>
              </template>

              <!-- ====== 干扰设备(4) ====== -->
              <template v-if="deviceKind === 'CUAS_JAMMER'">
                <div class="cfg-block">
                  <div class="cfg-block-title">攻击控制 (0x30 / 0x31 / 0x32)</div>
                  <label class="cfg-row"><span>攻击模式</span>
                    <select v-model="attackMode" class="cfg-select">
                      <option value="rtn">返航 (驱离)</option>
                      <option value="land">迫降</option>
                    </select>
                  </label>
                  <div class="cfg-block-sub">攻击频段</div>
                  <label class="cfg-row"><span>5.8GHz</span><input type="checkbox" v-model="jamBands.band58G"/></label>
                  <label class="cfg-row"><span>2.4GHz</span><input type="checkbox" v-model="jamBands.band24G"/></label>
                  <label class="cfg-row"><span>900MHz</span><input type="checkbox" v-model="jamBands.band900M"/></label>
                  <label class="cfg-row"><span>1.4GHz</span><input type="checkbox" v-model="jamBands.band14G"/></label>
                  <label class="cfg-row"><span>5.2GHz</span><input type="checkbox" v-model="jamBands.band52G"/></label>
                </div>
              </template>

              <!-- ====== 诱骗设备(6) ====== -->
              <template v-if="deviceKind === 'CUAS_SPOOFER'">
                <div class="cfg-block">
                  <div class="cfg-block-title">GNSS 诱骗 (0x36 / 0x37 / 0x38)</div>
                  <label class="cfg-row">
                    <span>诱骗模式</span>
                    <select v-model="gnssMode" class="cfg-select">
                      <option value="expel">定向驱逐</option>
                      <option value="land">定点迫降</option>
                      <option value="nofly">禁飞</option>
                      <option value="navsuppress">导航压制</option>
                    </select>
                  </label>
                  <label class="cfg-row">
                    <span>诱导方向</span>
                    <select v-model="gnssDirection" class="cfg-select">
                      <option value="away">驱离</option>
                      <option value="near">拉近</option>
                    </select>
                  </label>
                  <div class="cfg-row-2">
                    <label class="cfg-row"><span>方位°</span><input type="number" v-model.number="gnssAzimuth" class="cfg-num"/></label>
                    <label class="cfg-row"><span>速度 m/s</span><input type="number" v-model.number="gnssSpeed" class="cfg-num"/></label>
                  </div>
                </div>
                <div class="cfg-block">
                  <div class="cfg-block-title">禁飞区 (0x42)</div>
                  <div class="cfg-row-3">
                    <label class="cfg-row"><span>纬度</span><input type="number" v-model.number="noflyLat" class="cfg-num" step="0.0001"/></label>
                    <label class="cfg-row"><span>经度</span><input type="number" v-model.number="noflyLon" class="cfg-num" step="0.0001"/></label>
                    <label class="cfg-row"><span>海拔</span><input type="number" v-model.number="noflyAlt" class="cfg-num"/></label>
                  </div>
                </div>
                <div class="cfg-block">
                  <div class="cfg-block-title">定点迫降区 (0x43)</div>
                  <div class="cfg-row-2">
                    <label class="cfg-row"><span>纬度</span><input type="number" v-model.number="landLat" class="cfg-num" step="0.0001"/></label>
                    <label class="cfg-row"><span>经度</span><input type="number" v-model.number="landLon" class="cfg-num" step="0.0001"/></label>
                  </div>
                  <div class="cfg-row-2">
                    <label class="cfg-row"><span>海拔 m</span><input type="number" v-model.number="landAlt" class="cfg-num"/></label>
                    <label class="cfg-row"><span>半径 m</span><input type="number" v-model.number="landRadius" class="cfg-num"/></label>
                  </div>
                </div>
                <div class="cfg-block">
                  <div class="cfg-block-title">自动联动 (0x40 / 0x41)</div>
                  <label class="cfg-row"><span>无人值守</span><input type="checkbox" v-model="autoMode"/></label>
                  <label class="cfg-row"><span>诱骗联动</span><input type="checkbox" v-model="autoLink"/></label>
                </div>
              </template>

              <!-- ====== 智能控制箱(8) ====== -->
              <template v-if="deviceKind === 'CUAS_CONTROL_BOX'">
                <div class="cfg-block">
                  <div class="cfg-block-title">箱仓操作 (0x80)</div>
                  <label class="cfg-row">
                    <span>箱仓状态</span>
                    <select v-model="boxState" class="cfg-select">
                      <option value="close">关闭</option>
                      <option value="open">敞开</option>
                    </select>
                  </label>
                </div>
              </template>

              <!-- ====== HSimC2 传感器 ====== -->
              <template v-if="device.source === 'hsimc2_sensor'">
                <div class="cfg-block">
                  <div class="cfg-block-sub">校准参数</div>
                  <label class="cfg-row"><span>自动校准</span><input type="checkbox" v-model="detectionOn"/></label>
                </div>
              </template>

              <!-- 空状态 -->
              <div v-if="!deviceKind" class="cfg-empty">暂无可配置参数</div>
            </div>

            <!-- ====== 右栏：测试面板 ====== -->
            <div class="cfg-right">
              <div class="cfg-section-title"><Icon name="flask" :size="14" />设备测试</div>

              <!-- 测试按钮组（按设备类型） -->
              <div class="cfg-test-btns">

                <!-- 雷达 -->
                <template v-if="deviceKind === 'CUAS_RADAR'">
                  <button class="cfg-test-btn" @click="testToggle('20','开启探测',true)" :disabled="testRunning">开启探测 (0x20)</button>
                  <button class="cfg-test-btn" @click="testToggle('20','关闭探测',false)" :disabled="testRunning">关闭探测 (0x20)</button>
                </template>

                <!-- 无线电侦测 -->
                <template v-if="deviceKind === 'CUAS_RADIO_FREQ'">
                  <button class="cfg-test-btn" @click="testToggle('20','开启探测',true)" :disabled="testRunning">开启探测 (0x20)</button>
                  <button class="cfg-test-btn" @click="runTest('设置白名单','C0',{enabled:whitelistEnabled,mode:whitelistMode,list:whitelistText})" :disabled="testRunning">下发白名单 (0xC0)</button>
                </template>

                <!-- 光电跟踪 -->
                <template v-if="deviceKind === 'CUAS_OPTICAL'">
                  <button class="cfg-test-btn" @click="runTest('设置云台模式','10',{mode:ptzMode})" :disabled="testRunning">设置云台模式 (0x10)</button>
                  <button class="cfg-test-btn" @click="runTest('转台定位','71',{pan:ptzPan,tilt:ptzTilt})" :disabled="testRunning">转台定位 (0x71)</button>
                  <template v-if="cameraOp !== 'stop'">
                    <button class="cfg-test-btn" @click="runTest('云镜控制','72',{op:cameraOp,hs:camHSpeed,vs:camVSpeed,zoom:camZoom})" :disabled="testRunning">
                      云镜控制 (0x72): {{ cameraOp }}
                    </button>
                  </template>
                  <button class="cfg-test-btn" @click="runTest('停止转动','72',{op:'stop',hs:0,vs:0,zoom:0})" :disabled="testRunning">停止转动 (0x72)</button>
                  <button class="cfg-test-btn" @click="runTest('预览抓图','70',{deviceId:device.device_id})" :disabled="testRunning">预览抓图 (0x70)</button>
                </template>

                <!-- 红外 -->
                <template v-if="deviceKind === 'CUAS_INFRARED'">
                  <button class="cfg-test-btn" @click="runTest('转台定位','71',{pan:ptzPan,tilt:ptzTilt})" :disabled="testRunning">转台定位 (0x71)</button>
                </template>

                <!-- 干扰 -->
                <template v-if="deviceKind === 'CUAS_JAMMER'">
                  <button class="cfg-test-btn" @click="testToggle('30','开启攻击',true)" :disabled="testRunning">开启攻击 (0x30)</button>
                  <button class="cfg-test-btn" @click="testToggle('30','关闭攻击',false)" :disabled="testRunning">关闭攻击 (0x30)</button>
                  <button class="cfg-test-btn" @click="runTest('设置攻击模式','31',{mode:attackMode})" :disabled="testRunning">设置模式 (0x31)</button>
                  <button class="cfg-test-btn" @click="runTest('设置频段','32',{bands:jamBands})" :disabled="testRunning">设置频段 (0x32)</button>
                </template>

                <!-- 诱骗 -->
                <template v-if="deviceKind === 'CUAS_SPOOFER'">
                  <button class="cfg-test-btn" @click="runTest('开启GNSS诱骗','36',{mode:gnssMode,enable:true})" :disabled="testRunning">开启诱骗 (0x36)</button>
                  <button class="cfg-test-btn" @click="runTest('关闭GNSS诱骗','36',{mode:gnssMode,enable:false})" :disabled="testRunning">关闭诱骗 (0x36)</button>
                  <button class="cfg-test-btn" @click="runTest('设置诱骗模式','37',{mode:gnssMode,direction:gnssDirection})" :disabled="testRunning">设置模式 (0x37)</button>
                  <button class="cfg-test-btn" @click="runTest('设置方位速度','38',{azimuth:gnssAzimuth,speed:gnssSpeed})" :disabled="testRunning">方位速度 (0x38)</button>
                  <button class="cfg-test-btn" @click="runTest('设置禁飞区','42',{lat:noflyLat,lon:noflyLon,alt:noflyAlt})" :disabled="testRunning">设禁飞区 (0x42)</button>
                  <button class="cfg-test-btn" @click="runTest('设置迫降区','43',{lat:landLat,lon:landLon,alt:landAlt,radius:landRadius})" :disabled="testRunning">设迫降区 (0x43)</button>
                  <button class="cfg-test-btn" @click="testToggle('40','无人值守',autoMode)" :disabled="testRunning">无人值守 (0x40)</button>
                  <button class="cfg-test-btn" @click="testToggle('41','诱骗联动',autoLink)" :disabled="testRunning">联动开关 (0x41)</button>
                </template>

                <!-- 控制箱 -->
                <template v-if="deviceKind === 'CUAS_CONTROL_BOX'">
                  <button class="cfg-test-btn" @click="runTest('箱仓操作','80',{state:boxState})" :disabled="testRunning">
                    {{ boxState === 'open' ? '敞开箱仓' : '关闭箱仓' }} (0x80)
                  </button>
                </template>

                <!-- HSimC2 传感器 -->
                <template v-if="device.source === 'hsimc2_sensor'">
                  <button class="cfg-test-btn" @click="runTest('校准','12',{})" :disabled="testRunning">一键校准 (0x12)</button>
                  <button class="cfg-test-btn" @click="runTest('状态查询','A4',{})" :disabled="testRunning">状态查询 (0xA4)</button>
                </template>
              </div>

              <!-- 测试日志 -->
              <div class="cfg-log-title"><Icon name="list" :size="13" />测试日志</div>
              <div class="cfg-log">
                <div v-if="testLogs.length === 0" class="cfg-log-empty">点击上方按钮进行设备测试</div>
                <div v-for="(log, i) in testLogs" :key="i" class="cfg-log-entry" :class="{ fail: !log.ok }">
                  <span class="log-time">{{ log.time }}</span>
                  <span class="log-cmd">{{ log.cmd }}</span>
                  <span class="log-result">{{ log.result }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 底部 -->
          <div class="cfg-footer">
            <button class="cfg-btn-primary" @click="emit('close')">完成</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* ---- Overlay ---- */
.cfg-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.4);
  backdrop-filter: blur(8px) saturate(1.1);
  -webkit-backdrop-filter: blur(8px) saturate(1.1);
  display: flex; align-items: center; justify-content: center;
  z-index: 10001;
}
.cfg-dialog {
  width: min(860px, 94vw);
  max-height: 88vh;
  background: var(--bg-surface-3);
  border: 1px solid var(--stroke-divider);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-64);
  display: flex; flex-direction: column; overflow: hidden;
}

/* ---- Header ---- */
.cfg-header {
  display: flex; align-items: center; gap: 10px;
  padding: var(--sp-l) var(--sp-l);
  border-bottom: 1px solid var(--divider);
  background: var(--bg-surface-2);
  flex-shrink: 0;
}
.cfg-icon { font-size: 22px; }
.cfg-title { flex: 1; display: flex; align-items: center; gap: 8px; }
.cfg-name { font-size: 15px; font-weight: 700; color: var(--fg-1); }
.cfg-model { font-size: 11px; color: var(--fg-3); }
.cfg-src { padding: 1px 7px; border-radius: var(--radius-sm); font-size: 10px; font-weight: 600; }
.cfg-src.hsimc2_sensor { background: var(--brand-subtle); color: var(--brand-fg); }
.cfg-src.cuas_system { background: var(--warning-bg); color: var(--warning); }
.cfg-status { display: flex; align-items: center; gap: 5px; font-size: 11px; color: var(--fg-3); }
.cfg-led { width: 7px; height: 7px; border-radius: var(--radius-circular); }
.cfg-led.on { background: var(--success); box-shadow: 0 0 0 3px var(--success-bg); }
.cfg-led.off { background: var(--fg-4); }
.cfg-close {
  width: 28px; height: 28px; font-size: 14px;
  border: 1px solid transparent; border-radius: var(--radius-md);
  background: transparent; color: var(--fg-3); cursor: pointer;
  transition: background var(--dur-normal) var(--ease-fluent);
}
.cfg-close:hover { background: var(--danger-bg); color: var(--danger); }

/* ---- Body (双栏) ---- */
.cfg-body {
  flex: 1; display: flex; overflow: hidden; min-height: 0;
}
.cfg-left {
  width: 46%; padding: var(--sp-m) var(--sp-l); overflow-y: auto;
  border-right: 1px solid var(--stroke-divider);
}
.cfg-right {
  flex: 1; padding: var(--sp-m) var(--sp-l); overflow-y: auto;
  display: flex; flex-direction: column;
}
.cfg-left::-webkit-scrollbar, .cfg-right::-webkit-scrollbar { width: 8px; }
.cfg-left::-webkit-scrollbar-thumb, .cfg-right::-webkit-scrollbar-thumb { background:var(--stroke-control); border-radius:var(--radius-circular); border:2px solid transparent; background-clip:padding-box; }

.cfg-section-title { display: flex; align-items: center; gap: 5px; font-size: 13px; font-weight: 700; color: var(--fg-2); margin-bottom: 8px; }

/* ---- 配置块 ---- */
.cfg-block {
  background: var(--bg-surface-2); border: 1px solid var(--stroke-divider);
  border-radius: var(--radius-lg); padding: var(--sp-s) 10px; margin-bottom: var(--sp-s);
}
.cfg-block-title { font-size: 11px; font-weight: 600; color: var(--brand-fg); margin-bottom: 6px; }
.cfg-block-sub { font-size: 10px; color: var(--fg-3); margin: 4px 0 4px; }

.cfg-row { display: flex; align-items: center; justify-content: space-between; padding: 2px 0; gap: 8px; }
.cfg-row.col { flex-direction: column; align-items: stretch; }
.cfg-row span { font-size: 11px; color: var(--fg-3); min-width: 48px; }
.cfg-row-2 { display: grid; grid-template-columns: 1fr 1fr; gap: 4px; }
.cfg-row-3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 4px; }

input[type="checkbox"] { width: 32px; height: 18px; accent-color: var(--brand-fg); cursor: pointer; }
.cfg-select {
  flex: 1; padding: 4px 6px;
  background: var(--bg-surface); border: 1px solid var(--stroke-control);
  border-radius: var(--radius-md); color: var(--fg-1); font-size: 11px; outline: none;
  font-family: var(--font-base);
}
.cfg-num {
  width: 60px; padding: 4px 6px;
  background: var(--bg-surface); border: 1px solid var(--stroke-control);
  border-radius: var(--radius-md); color: var(--fg-1); font-size: 11px; outline: none; text-align: right;
  font-family: var(--font-base);
}
.cfg-num:focus, .cfg-select:focus { border-color: var(--brand-fg); }
.cfg-input {
  flex: 1; padding: 4px 8px;
  background: var(--bg-surface); border: 1px solid var(--stroke-control);
  border-radius: var(--radius-md); color: var(--fg-1); font-size: 11px; outline: none; resize: vertical;
  font-family: var(--font-mono);
}
.cfg-input:focus { border-color: var(--brand-fg); }
.cfg-empty { text-align: center; padding: 30px; color: var(--fg-4); font-size: 12px; }

/* ---- 测试按钮 ---- */
.cfg-test-btns { display: flex; flex-direction: column; gap: var(--sp-xs); margin-bottom: var(--sp-m); }
.cfg-test-btn {
  width: 100%; padding: 8px var(--sp-m); text-align: left;
  background: var(--bg-surface-2); border: 1px solid var(--stroke-divider);
  border-radius: var(--radius-md); color: var(--fg-2); font-size: 12px; font-weight: 600;
  cursor: pointer; transition: background var(--dur-normal) var(--ease-fluent), border-color var(--dur-normal) var(--ease-fluent);
}
.cfg-test-btn:hover:not(:disabled) {
  background: var(--bg-subtle-hover); border-color: var(--stroke-control); color: var(--fg-1);
}
.cfg-test-btn:disabled { opacity: 0.4; cursor: default; }

/* ---- 日志 ---- */
.cfg-log-title { display: flex; align-items: center; gap: 5px; font-size: 11px; font-weight: 600; color: var(--fg-3); margin-bottom: 6px; margin-top: 4px; }
.cfg-log {
  flex: 1; overflow-y: auto; min-height: 60px;
  background: var(--bg-canvas); border: 1px solid var(--stroke-divider);
  border-radius: var(--radius-md); padding: 6px 8px; font-family: var(--font-mono);
}
.cfg-log-empty { color: var(--fg-4); font-size: 11px; text-align: center; padding: 16px 0; }
.cfg-log-entry { display: flex; gap: 6px; padding: 2px 0; font-size: 10px; border-bottom: 1px solid var(--stroke-divider); }
.cfg-log-entry.fail { color: var(--danger); }
.log-time { color: var(--fg-4); flex-shrink: 0; width: 60px; }
.log-cmd { color: var(--brand-fg); flex-shrink: 0; min-width: 80px; }
.log-result { color: var(--fg-2); word-break: break-all; }

/* ---- Footer ---- */
.cfg-footer {
  display: flex; justify-content: flex-end; gap: 8px;
  padding: var(--sp-m) var(--sp-l); border-top: 1px solid var(--divider);
  background: var(--bg-surface-2); flex-shrink: 0;
}
.cfg-btn-primary {
  height: 32px; padding: 0 var(--sp-xxl); border: 1px solid transparent;
  border-radius: var(--radius-md); background: var(--brand-rest);
  color: #fff; font-size: 14px; font-weight: 600; cursor: pointer;
  box-shadow: 0 0 14px var(--glow-accent);
  transition: background var(--dur-normal) var(--ease-fluent), box-shadow var(--dur-normal) var(--ease-fluent);
}
.cfg-btn-primary:hover { background: var(--brand-hover); box-shadow: 0 0 20px var(--glow-accent); }

/* ---- Transition ---- */
.cfg-fade-enter-active, .cfg-fade-leave-active { transition: opacity var(--dur-slow) var(--ease-fluent); }
.cfg-fade-enter-from, .cfg-fade-leave-to { opacity: 0; }
.cfg-fade-enter-active .cfg-dialog { transition: transform var(--dur-slow) var(--ease-decel); }
.cfg-fade-enter-from .cfg-dialog { transform: scale(0.98) translateY(8px); }
</style>
