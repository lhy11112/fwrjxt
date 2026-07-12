<template>
  <div class="toggleBottom" id="bottomMenu">
    <div class="tabsBottom">
      <span @click="tabsIndex=1" :class="tabsIndex==1?'tabsActive':''">导航栏</span>
      <span v-if="ewVisible" @click="tabsIndex=2" :class="tabsIndex==2?'tabsActive':''">时间轴</span>
    </div>
       <div :class="bottomToggleVisible?'bottomToggleBtn bottomToggleBg1':'bottomToggleBtn bottomToggleBg2'" @click="wrjToggleEvent()">
        <img style="width:40px;" :src="bottomToggleVisible?'static/toggle/toggle-icon1.png':'static/toggle/toggle-icon2.png'"/>
      </div>
    <div class="bottomBox" v-show="tabsIndex==1">
      <div class="bottom-left" :style="{width:bottomleftToggle?'100%':'38%'}">
        <div class="bottom-title">

          <div>
            <img src="@/assets/allImage/leftJt.png" style="width:21px;height:21px;vertical-align: middle;"/>
            品牌
          </div>
          <div>
            型号
          </div>
          <div>
            序列号
          </div>
          <div>
            经度
          </div>
          <div>
            纬度
          </div>
          <div>
            高度（m）
          </div>
          <div>
            飞手位置
          </div>
          <div>
            发现站点
          </div>
          <div>
            飞入空域
          </div>
          <div>
            告警类型
          </div>
          <div>
            告警时间
          </div>
          <div style="width:100%;">
            <img src="@/assets/allImage/bottom-img.png" style="width:100%;height:3px"/>
          </div>
        </div>
        <div class="bottom-content">
          <div v-for="(item,index) in kydtData" :key="index" class="kydt-item">
            <div>
              <div class="dot"></div>
              <div style="width:calc(100% - 15px)">{{item.wrjpp}}</div>
            </div>
            <div>{{item.wrjxh}}</div>
            <div>{{item.wrjxlh}}</div>
            <div>{{item.gjfsjd?item.gjfsjd.toFixed(3):''}}</div>
            <div>{{item.gjfswd?item.gjfswd.toFixed(3):''}}</div>
            <div>{{item.gjfsgd?item.gjfsgd.toFixed(3):''}}</div>
            <div>{{item.fswd?item.fswd.toFixed(3):''}},{{item.fsjd?item.fsjd.toFixed(3):''}}</div>
            <div class="text-ellipsis" :title="item.zdmc">{{item.zdmc}}</div>
            <div>{{item.kyid_dictText}}</div>
            <div>{{item.gjlx}}</div>
            <div>{{item.gjfssj}}</div>
          </div>
        </div>
        <div class="bottom-left-btn" :style="{right:bottomleftToggle?'0':'-15px'}" @click="leftMenuToggle">
          <el-icon v-show="bottomleftToggle" title="收起"><Fold /></el-icon>
          <el-icon v-show="!bottomleftToggle" title="展开"><Expand /></el-icon>
        </div>
      </div>
      <div class="bottom-center" v-show="!bottomleftToggle">
        <div class="menus">
          <div :class="currentMenuIndex==index?'menu-item menu-item-active':'menu-item'" v-for="(item,index) in centerData" :key="index" @click="menusClick(index)">
            <div>
              <img :src="item.icon"/>
            </div>
            <div>{{item.name}}</div>
          </div>
        </div>
        <img src="@/assets/bottomTitle/icons/icon.png" class="iconImg"/>
      </div>
      <div class="bottom-right" v-show="!bottomleftToggle">
        <div v-show="currentMenuIndex==0">
          <div class="bottom-title">
          
            <div :class="currentMdIndex==0?'mdActive':''" @click="getMd('0')">
              黑名单
            </div>
            <div :class="currentMdIndex==1?'mdActive':''" @click="getMd('1')">
              白名单
            </div>
          </div>
          <div class="bottom-content">
            <div class="hbmd-header">
              <div>无人机</div>
              <div>名单类型</div>
            </div>
            <div class="hbmdItems">
              <div v-for="(item,index) in hbmdData" :key="index" class="hbmd-item">
                <div>{{item.name}}</div>
                <div>{{item.mdlx}}</div>
              </div>
            </div>
            
          </div>
        </div>

         <div v-show="currentMenuIndex==1">
          <div class="bottom-content" style="height:100%;">
            <el-table
                :data="tableData"
                style="width: 100%; height: 100%"
                @selectionChange="selectionChange"
                @row-click="rowClickChange"
                v-loading="loading"
              >
                <el-table-column
                  v-for="(item, index) in columnData"
                  :prop="item.prop"
                  :label="item.label"
                  :key="index"
                  :width="item.width"
                >
                </el-table-column>
                <el-table-column fixed="right" label="操作" width="150">
                  <template #default="scope">
                    <el-button
                      style="color: #fff"
                      link
                      type="primary"
                      size="small"
                      @click.prevent="startCx(scope.row)"
                    >
                      开始测向
                    </el-button>
                    <el-button
                      style="color: #fff"
                      link
                      type="primary"
                      size="small"
                      @click.prevent="endCx(scope.row)"
                    >
                      结束测向
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            
          </div>
        </div>

        <div v-show="currentMenuIndex==2 || currentMenuIndex==3">
          <div class="bottom-title2">
            <div>
              开始频率(Mhz):
              <el-input type="number" style="width:120px" @blur="blurEvent" v-model="beginFreq"></el-input>
            </div>
            <div>
              结束频率(Mhz):
              <el-input type="number" style="width:120px" @blur="blurEvent" v-model="endFreq"></el-input>
            </div>
            <div>
              是否启用:
              <el-switch v-model="enable" :active-value="1" :inactive-value="0" @change="enableChange"></el-switch>
            </div>
          </div>
          <div class="bottom-content">
            <div v-if="ppData && ppData.length" id="ppEcharts" ref="ppEcharts" style="width:100%;height:180px;"></div>
            <!-- 频谱展示区 -->
            <!-- <div v-if="ppData && ppData.length" class="spectrum-container">
                <div class="frequency-axis" id="frequency-axis">
                </div>
                <canvas id="spectrum" class="spectrum-canvas"></canvas>
            </div> -->
            <div v-else>
              <img
                style="
                  width: 170px;
                  height: 105px;
                  margin-left: 50%;
                  margin-top: 4%;
                  transform: translate(-50%, 0);
                "
                src="@/assets/noData.png"
                alt=""
              />
            </div>
          </div>
        </div>
      </div>

      
      
    </div>
    <div class="bottomTimeLine" v-show="tabsIndex==2">

    </div>
  </div>
  
  
  <deviceDetailDialog ref="deviceDetailRef"></deviceDetailDialog>
  <AnalysisMoveDlg
    title="提示"
    style="width: 30%;height:30vh"
    :visibleDialog="visibleDialog"
    @close="closed"
    isModal="true"
  >
    <el-form
      :model="form"
      :rules="rules"
      :disabled="mode === 'show'"
      ref="dialogForm"
      label-width="120px"
      style="height: calc(100% - 40px);overflow: auto;"
    >
      <el-row>
         <el-col :span="24">
          {{wrjObj.wrjpp}}-{{wrjObj.wrjxh}}-{{wrjObj.wrjxlh}}进入空域告警
        </el-col>
        <el-col :span="24">
          <el-radio-group v-model="form.authStatus">
            <el-radio :label="1">白名单</el-radio>
            <el-radio :label="2">黑名单</el-radio>
            <el-radio :label="3">未授权</el-radio>
            <el-radio :label="4">待干扰</el-radio>
            <el-radio :label="5">待诱骗</el-radio>
            <el-radio :label="6">持续跟踪</el-radio>
          </el-radio-group>
        </el-col>
      </el-row>
    </el-form>
    <div class="create-bottom">
      
        <!-- <el-button @click="closed">取 消</el-button> -->

        <el-button  type="primary" @click="submit()"
          >确定</el-button
        >
      </div>
  </AnalysisMoveDlg>
</template>

<script setup>
import { useRoute,useRouter } from "vue-router";
import microApp from "@/components/microApp/index.vue";
import { ref, onMounted,nextTick,onUnmounted,watch } from "vue";
import icon1 from "@/assets/bottomTitle/icons/icon1.png"
import icon2 from "@/assets/bottomTitle/icons/icon2.png"
import icon3 from "@/assets/bottomTitle/icons/icon3.png"
import icon4 from "@/assets/bottomTitle/icons/icon4.png"
import icon5 from "@/assets/bottomTitle/icons/icon5.png"
import icon6 from "@/assets/bottomTitle/icons/icon6.png"
import icon7 from "@/assets/bottomTitle/icons/icon7.png"
import deviceDetailDialog from "../portal/wxdzc/deviceDetail.vue"
import { ElMessage } from "element-plus";
// 定义路由
const route = useRoute();
const router = useRouter();

const columnData = ref([
  {
    prop:"model",
    label:"型号"
  },
  {
    prop:"rssi",
    label:"信号强度",
    width:'100px'
  },
  {
    prop:"freq",
    label:"频率(Mhz)",
    width:'100px'
  },
  {
    prop:"dataTime",
    label:"更新时间",
    width:'180px'
  },
])
const tableData = ref([])
const centerData = ref([
  {
    name:"综合统计",
    icon:icon5
  },
  {
    name:"侦测预警",
    icon:icon1
  },
  // {
  //   name:"指挥控制",
  //   icon:icon2
  // },
  {
    name:"信号干扰",
    icon:icon3
  },
  {
    name:"导航诱骗",
    icon:icon5
  },
])
const kydtData = ref([
  // {
  //   kygj:'D01巡逻空域发现可疑无人机黑飞',
  //   ycgj:'125.152.253',
  //   sj:'2025-08-20 12:25:25'
  // },
  // {
  //   kygj:'D03巡逻空域发现可疑无人机黑飞',
  //   ycgj:'125.152.253',
  //   sj:'2025-09-20 12:25:25'
  // },
  // {
  //   kygj:'D05巡逻空域发现可疑无人机黑飞',
  //   ycgj:'125.152.253',
  //   sj:'2025-09-25 12:25:25'
  // },
  // {
  //   kygj:'D07巡逻空域发现可疑无人机黑飞',
  //   ycgj:'125.152.253',
  //   sj:'2025-10-02 12:25:25'
  // },
  // {
  //   kygj:'D02巡逻空域发现可疑无人机黑飞',
  //   ycgj:'125.152.253',
  //   sj:'2025-10-20 12:25:25'
  // },
])
const fxjhData = ref([
  {
    title:'计划总数量',
    wrjNum:'12',
    thNum:'7',
    qtNum:'3'
  },
  {
    title:'执行中数量',
    wrjNum:'18',
    thNum:'4',
    qtNum:'3'
  },
  {
    title:'待执行数量',
    wrjNum:'21',
    thNum:'3',
    qtNum:'2'
  },
])
const timer = ref(null)
const wrjid = ref("")
const wrjObj = ref({})
let borders =null;
let intervalId = null;
let isFlashing = false;
const resultColor = ref("")
const xdData = ref({})
const tabsIndex = ref(1);
const currentMenuIndex = ref(0)
const stationId = ref("")
const ppData = ref([])
const aa= ref(true)
const xData = ref([])
const ppEcharts = ref(null)
let listEcharts =null;
const beginFreq = ref('')
const endFreq = ref('');
const enable = ref(0)
const rq = ref("")
const rqTimer = ref(null);
const ewVisible = ref(false);

onMounted(()=>{
  gjInfo()
  getHbmd()
  wrjppData()
  timer.value = setInterval(()=>{
    gjInfo()
  },1000*10)
  //  window.eventBus.on("gj",(msg_txt)=>{
  //   console.log(msg_txt);
  //     wrjid.value = msg_txt.wrjid;
  //     wrjObj.value.wrjpp = msg_txt.wrjpp; 
  //     wrjObj.value.wrjxh = msg_txt.wrjxh; 
  //     wrjObj.value.wrjxlh = msg_txt.wrjxlh; 
  //     visibleDialog.value = true;
  //     // window.API.wrjgj.qetGjys({
  //     //   id:msg_txt.id,
  //     //   kyid:msg_txt.kyid
  //     // }).then(res=>{
  //     //   if(res.code==200){
  //     //     resultColor.value = res.result;
  //     //     startFlash()
  //     //   }
  //     // })
  //     resultColor.value = msg_txt.gjys;
  //     startFlash()
      
  //   })
  // 获取所有闪烁层
    borders = document.querySelectorAll('.flash-border');
    console.log(borders);
   window.eventBus.on("xd",(data)=>{
    xdData.value = data;
   })
   window.eventBus.on("timeline",(data)=>{
    
    console.log(data);
    tabsIndex.value = data.index;
    ewVisible.value = data.flag;
   })
   window.eventBus.on("addDevice",(data)=>{
    clearInterval(timer2.value)
    timer2.value=null;
    clearLayer1()
    timer2.value =setInterval(()=>{
      getData()
    },1000 *10)
    getData()
   })
  window.eventBus.on('stationId',(data)=>{
    stationId.value = data;

    closePp()
  })

  window.eventBus.on('rq',(data)=>{
      rq.value = data;
      wrjppData()
      clearInterval(rqTimer.value)
      rqTimer.value = null;
      rqTimer.value = setInterval(()=>{
        wrjppData()
      },1000*3)
    })

    try {
      window.WEBSCOKET.initWebscoket()
      console.log(window.WEBSCOKET.ws)
      if(window.WEBSCOKET.ws){
        window.WEBSCOKET.ws.addEventListener('message', (event) => {
          try {
            let data = JSON.parse(event.data);
            let msg_txt = JSON.parse(data.msg_txt);
            let msg_cmd = data.msg_cmd;
            console.log('webscoket',data,msg_txt,msg_cmd);
            if(msg_cmd && msg_cmd=="CMD_SpectrumData"){
              ppData.value = [...ppData.value,...JSON.parse(msg_txt.pData)];
              nextTick(()=>{
                let ppLen = Number(endFreq.value) - Number(beginFreq.value);
                let addPoint = parseInt(Number(ppLen / (60000 / 1000000)));
                
                xData.value = [];
                for(var i =0;i<=addPoint;i++){
                  xData.value.push(Number(beginFreq.value)+Number(i*(60000 / 1000000)))
                }

                console.log(ppData.value.length,addPoint);
                if(ppData.value.length>addPoint){
                  // ppData.value.splice(0,addPoint)
                  // console.log('ppData.value',ppData.value);
                  let chbfLength =ppData.value.length - addPoint;
                  ppData.value.splice(0,chbfLength)
                }
                
                console.log('xxxxxxxx',ppData.value.length);
                if(aa.value){
                  ppEchartsEvent()
                  aa.value=false;
                }
                updateSpectrogram()
              //  
              })
            }else if(msg_cmd && msg_cmd=="cmd_topic"){
              // window.eventBus.emit("gj",msg_txt)
              console.log('webscoket-gj',msg_cmd);
              wrjid.value = msg_txt.wrjid;
              wrjObj.value.wrjpp = msg_txt.wrjpp; 
              wrjObj.value.wrjxh = msg_txt.wrjxh; 
              wrjObj.value.wrjxlh = msg_txt.wrjxlh; 
              visibleDialog.value = true;
              resultColor.value = msg_txt.gjys;
              startFlash()
              createAndPlayAudio('/static/audio.wav');
            }else if(msg_cmd && msg_cmd=="CMD_HEARTBEAT_PASSIVE"){
              window.eventBus.emit("sb",msg_txt)
            }

          } catch (error) {
            console.log(error);
          }
        });
      }
    } catch (error) {
      console.log(error);
    }
  
    
})

watch(
  route,
  (newVal) => {
    if(newVal){
      if(newVal.name=="portal"){
        currentMenuIndex.value = 0;
      }else if(newVal.name=="wxdzc"){
        currentMenuIndex.value = 1;
      }else if(newVal.name=="signalInterference"){
        currentMenuIndex.value = 2;
      }else if(newVal.name=="navigationDeception"){
        currentMenuIndex.value = 3;
      }
    }
  }
)
if (window.location.href.includes("portal")) {
  currentMenuIndex.value = 0;
}else if(window.location.href.includes("wxdzc")){
  currentMenuIndex.value = 1;
}else if(window.location.href.includes("signalInterference")){
  currentMenuIndex.value = 2;
}else if(window.location.href.includes("navigationDeception")){
  currentMenuIndex.value = 3;
}
onUnmounted(()=>{
  clearInterval(timer.value)
  timer.value=null;
  clearInterval(timer2.value)
  timer2.value=null;
  clearInterval(rqTimer.value)
  timer.value=null;
  clearLayer1()
   window.eventBus.off("xd")
   window.eventBus.off("gj")
   window.eventBus.off("timeline")
   window.eventBus.off("addDevice")
   window.eventBus.off("rq")
   window.WEBSCOKET.ws.close()
})

const wrjppData = () => {
  window.API.wrjppsj.listNow({
    pageNo:1,
    pageSize:10,
    rq:rq.value
  }).then(res=>{
    // console.log(res);
    if(res.success){
      tableData.value = res.result;
    }
  })
}

const startCx = (data) => {
  window.API.device.ControlTypeDfFreqParam({
    "enable":"1",
    "dirType":"0",
    "freq":data.freq,
    "bw":"10",
    "id":data.model,
    "model":data.model,
    "stationId":data.stationId
  }).then(res=>{
    if(res.success){
      ElMessage.success(res.message)
    }
  })
}

const endCx = (data) => {
  window.API.device.ControlTypeDfFreqParam({
    "enable":"0",
    "dirType":"0",
    "freq":data.freq,
    "bw":"10",
    "id":data.model,
    "model":data.model,
    "stationId":data.stationId
  }).then(res=>{
    if(res.success){
      ElMessage.success(res.message)
    }
  })
}


const ppEchartsEvent = () => {
    if (window.echarts.init(document.getElementById('ppEcharts'))) {
        window.echarts.init(document.getElementById('ppEcharts')).clear();
    }
        
    listEcharts = window.echarts.init(document.getElementById('ppEcharts'));
    listEcharts.off("click");

    const option = {
      grid:{
        top: "10px",
          left: "10px",
          right: "30px",
          bottom: "50px",
          containLabel: true,
      },
      tooltip: {
        trigger: "axis"
      },
      xAxis: {
        type: 'category',
        min:beginFreq.value,
        max:endFreq.value,
        interval:xData.value.length?parseInt(xData.value.length/3):'1',
        data:xData.value,
        splitLine: {
          show: false
        },
        axisLine: {
            lineStyle: {
              color: "#fff",
            },
          },
          axisLabel: {
            // 坐标轴文字设置
            // rotate: 20, //值>0向右倾斜，值<0则向左倾斜
            textStyle: {
              color: "#fff",
            },
            formatter:function(data){
              return Math.floor(data)
            }
          },
      },
      yAxis: {
        // min:80,
        // max:-100,
        // interval:20,
        axisLine: {
            lineStyle: {
              color: "#fff",
            },
          },
          axisLabel: {
            // 坐标轴文字设置
            // rotate: 20, //值>0向右倾斜，值<0则向左倾斜
            textStyle: {
              color: "#fff",
            },
          },
      },
      series: [
        {
          type: 'line',
          smooth: true, // 设置平滑
          data: ppData.value,
          emphasis: {
            focus: 'series'
          },
          areaStyle: {
            normal: {
              color: new window.echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: "#0090FF" },
                { offset: 1, color: "rgba(38, 82, 125)" },
              ]),
            },
          },
        },
      
      ],
    };
    
    listEcharts.setOption(option);
//   listEcharts.on('ready', function () {
//     const chart = listEcharts;
//     const dom = chart.getDom();
//     const bars = chart.getZones().filter(z => z.type === 'series-bar');
    
//     // 使用 GSAP 实现放大缩小动画
//     gsap.from(bars, {
//         duration: 1,
//         scale: 0.5,
//         opacity: 0,
//         stagger: 0.1,
//         ease: 'power2.out'
//     });
// });

}
const updateSpectrogram = () => {
  if(listEcharts){
    listEcharts.setOption({
                series: [
        {
         
          type: 'line',
          smooth: true, // 设置平滑
          data: ppData.value,
          emphasis: {
            focus: 'series'
          },
          areaStyle: {
            normal: {
              color: new window.echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: "#0090FF" },
                { offset: 1, color: "rgba(38, 82, 125)" },
              ]),
            },
          },
        },
      
      ],
      // animationEasing: 'elasticOut',
      // animationDelayUpdate: function (idx) {
      //   return idx * 5;
      // }
            });
  }
            
        }
const bottomleftToggle = ref(false)
const leftMenuToggle = () => {
  bottomleftToggle.value = !bottomleftToggle.value
}

const bottomToggleVisible = ref(false)
const wrjToggleEvent = () => {
  bottomToggleVisible.value = !bottomToggleVisible.value
  if(bottomToggleVisible.value){
    $('#bottomMenu').css("transform","translateY(20vh)");
  }else{
    $('#bottomMenu').css("transform","translateY(0)");
  }
  
}

const visibleDialog = ref(false);
const form = ref({
  authStatus:1
})

const closed = () =>{
      visibleDialog.value = false;
      form.value.id = wrjid.value;
      window.API.wrj.editHbmd(form.value).then(res=>{
        if(res.code==200){
          stopFlash()
        }
      })
    };
    const submit = () => {
      visibleDialog.value = false;
      form.value.id = wrjid.value;
      window.API.wrj.editHbmd(form.value).then(res=>{
        if(res.code==200){
          stopFlash()
        }
      })
    }
    const createAndPlayAudio = (audioFilePath) => {
      // 创建一个新的audio元素
      var audio = document.createElement('audio');
      
      // 设置音频文件的路径
      audio.src = audioFilePath;
      
      // 设置音频元素是否自动播放
      audio.autoplay = true;
      
      // 设置音频元素是否循环播放
      audio.loop = false;
      
      // 将audio元素添加到body中，以便它可以在页面上显示
      document.body.appendChild(audio);
      // 播放音频
      audio.play();
    }
    // 启动闪烁
const startFlash = () => {
      console.log('启动闪烁',isFlashing);
      if (isFlashing) return;
      isFlashing = true;

      // 设置闪烁频率（单位：毫秒）
      const flashInterval = 500;

      // 启动定时器
      intervalId = setInterval(() => {
        borders.forEach(border => {
          border.style.backgroundColor = resultColor.value
          border.style.opacity = border.style.opacity === '1' ? '0' : '1';
        });
      }, flashInterval);
    };

    // 停止闪烁
const stopFlash = () => {
      console.log(isFlashing);
      if (!isFlashing) return;
      isFlashing = false;

      // 清除定时器
      clearInterval(intervalId);
      intervalId = null;

      // 重置透明度
      borders.forEach(border => {
        border.style.opacity = '0';
      });
    };

const gjfxcsRef = ref(null)
const mbfxRef = ref(null)
const mbfxVisible = ref(false)
const gjInfo = () =>{
  window.API.wrjgj.list({
    pageNo: 1,
    pageSize: 100,
    order:'descs',
    column:'gjfssj'
  }).then(res=>{
        if(res.code == 200){
          kydtData.value = res.result.records;
        }
      })
}
const hbmdData = ref([])
const hmdData = ref([])
const bmdData = ref([])
const getHbmd = () => {
  window.API.hbmd.hbmdList().then(res=>{
    if(res.code==200){
      hmdData.value =res.result['黑名单']
      bmdData.value =res.result['白名单']
      if(currentMdIndex.value==0){
        hbmdData.value =hmdData.value 
      }else if(currentMdIndex.value==1){
        hbmdData.value =bmdData.value 
      }
    }
  })
}
const currentMdIndex = ref(0)
const getMd = (index) => {
  currentMdIndex.value =index;
  getHbmd()
}
const gjfxcsVisible = ref(false);
const mbfxFlag = ref(false)
const timer2 = ref(null)
const menusClick = (index) => {
  console.log(index);
  currentMenuIndex.value = index;
  if(index==0){
    router.push('/portal')
    clearInterval(timer2.value)
    timer2.value=null;
    clearInterval(rqTimer.value)
      rqTimer.value = null;
    clearLayer1()
    closePp()
  }else if(index==1){
    clearInterval(timer2.value)
    timer2.value=null;
    clearLayer1()
    router.push('/wxdzc')// 侦测预警
    closePp()
  }else if(index==2){
    router.push('/signalInterference') //信号干扰
    clearInterval(timer2.value)
    timer2.value=null;
    clearLayer1()
    timer2.value =setInterval(()=>{
      getData()
    },1000 *10)
    clearInterval(rqTimer.value)
      rqTimer.value = null;
    getData()
    closePp()
    
  }else if(index==3){
    router.push('/navigationDeception')
    clearInterval(timer2.value)
    timer2.value=null;
    clearLayer1()
    timer2.value =setInterval(()=>{
      getData()
    },1000 *10)
    clearInterval(rqTimer.value)
      rqTimer.value = null;
    getData()
    closePp()
  }
}

const closePp =() => {
  enable.value = 0;
  window.API.device.ControlDeviceSpectrumParam({
      beginFreq:beginFreq.value,
      endFreq:endFreq.value,
      enable:enable.value,
      stationId:stationId.value,
      step:60000
    }).then(res=>{})

    
    beginFreq.value = "";
    endFreq.value = "";
    ppData.value = [];
}

const blurEvent = () => {
  enable.value=0
}

const enableChange = () => {
  if(enable.value==1){
    ppData.value = []
  }
  ControlDeviceSpectrumParam()
}

const ControlDeviceSpectrumParam = () => {
  window.API.device.ControlDeviceSpectrumParam({
      beginFreq:beginFreq.value,
      endFreq:endFreq.value,
      enable:enable.value,
      stationId:stationId.value,
      step:60000
    }).then(res=>{
      if(res.success){
        ElMessage.success(res.message)
        aa.value = true;
        ppData.value = []
      }else{
        ElMessage.info(res.message)
        enable.value = false;
      }
        // setInterval(()=>{
        //   window.API.device.SendWebsocketData().then(res=>{
        //       console.log(res);
        //     })
        // },1000)
   
    })
}

const mbfxEntities = ref([])
const mbfxJl = ref(0)
const mbfxJd = ref('')
const mbfxWd = ref('')
const mbfxSuccess = (e,e2,e3,e4) => {
  mbfxVisible.value = false;
  mbfxJl.value = e;
  mbfxJd.value = e3;
  mbfxWd.value = e4;
  // 从地图移除所有实体
          mbfxEntities.value.forEach(entity => {
            console.log('xxxxx111',entity);
            if (entity.cesiumEntity) {
              window.Map3D.viewer.entities.remove(entity.cesiumEntity);
            }
          });
    const wrjData = window.TOOL.data.get('wrjData')?window.TOOL.data.get('wrjData'):{};
    if(mbfxFlag.value){
      zbmbAddMap2D(e2)
    }else{
      mbfxEntities.value =[{"id":2345,"type":"circle","name":"目标分析范围","lon":wrjData.longitude?Number(wrjData.longitude):0,"lat":wrjData.latitude?Number(wrjData.latitude):0,"radius":Number(e) *1000,"minHeight":0,"maxHeight":1,"shapeType":"restricted","color":"#ff0000","showLabel":true}];
      console.log(mbfxEntities.value);
      // 创建Cesium实体并添加到地图
      mbfxEntities.value.forEach(newEntity=>{
        addEntityToMap(newEntity);
      })
      zbmbAddMap(e2)
    }
    
}
let mbfx2dMarkerLayer = null;
const zbmbIcon = ref(null);
const mbfxTool = ref(false);
const zbmbAddMap2D = (data) => {
  console.log(data);
  mbfxTool.value = true;
       clearLayerMap2D()
    mbfx2dMarkerLayer = window.L.layerGroup([]);
  mbfx2dMarkerLayer.addTo(window.Map2D.map);

 
  data.forEach((item) => {
   if(item.mc.indexOf('医院')!=-1){
       zbmbIcon.value = window.L.icon({
        iconUrl: "/static/map_img/医院.png",
        iconSize: [40, 40],
      });
    }else if(item.mc.indexOf('学校')!=-1){
       zbmbIcon.value = window.L.icon({
        iconUrl: "/static/map_img/高等院校.png",
        iconSize: [40, 40],
      });
    }else if(item.mc.indexOf('加油站')!=-1){
       zbmbIcon.value = window.L.icon({
        iconUrl: "/static/map_img/加油站.png",
        iconSize: [40, 40],
      });
    }else{
      zbmbIcon.value = window.L.icon({
        iconUrl: "/static/map_img/重要目标.png",
        iconSize: [40, 40],
      });
    }
    if(item.jd && item.wd){
      const marker = window.L.marker(
        window.L.latLng(Number(item.wd), Number(item.jd)),
        {
          icon: zbmbIcon.value,
        }
      ).addTo(mbfx2dMarkerLayer);
      // const innerHTML = "名称: " + item.MC + "<br>";
      // innerHTML += "经度: " + item.JD + "<br>";
      // innerHTML += "纬度: " + item.WD + "<br>";
      const html = `<div style="width:140px;background:rgba(30, 32, 44);padding:10px">
                  <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                    <div style="width:100%;margin: 4px 0;color:#fff;">名称：<span style="color:#fff;">${item.mc}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">经度：<span style="color:#fff;">${item.jd}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">纬度：<span style="color:#fff;">${item.wd}</span></div>
                  </div>
                </div>`;
      marker
        // bindTooltip
        .bindPopup(item.mc)
        .bindTooltip(html)
        // .openPopup(marker.getLatLng());
      marker.on("click", function (e) {
        console.log(e);
        mbfxDetail(item);
      });

      // // 使用turf.js计算距离
      const distance = turf.distance([Number(item.wd), Number(item.jd)], [mbfxWd.value, mbfxJd.value]);
      console.log(distance);
      
      const polyLine = L.polyline([[Number(item.wd), Number(item.jd)], [Number(mbfxWd.value), Number(mbfxJd.value)]], {color: "red"}).addTo(mbfx2dMarkerLayer);
      // 计算中间点坐标
        const midpointWd = Number(item.wd) + (Number(mbfxWd.value) - Number(item.wd)) / 2,
            midpointJd = Number(item.jd) + (Number(mbfxJd.value) - Number(item.jd)) / 2;
      const  markerIcon = L.divIcon({
                html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>${(distance*1000).toFixed(2)}米</div>`,//marker标注
                className: 'my-div-icon',
                iconAnchor: [80, -20]//文字标注相对位置
              });
      window.L.marker(
        window.L.latLng(Number(midpointWd), Number(midpointJd)),
        {
          icon: markerIcon,
        }
      ).addTo(mbfx2dMarkerLayer);
    }
  });

    const center = [Number(mbfxWd.value), Number(mbfxJd.value)];
    console.log(center);
      const radius = mbfxJl.value * 1000; // 圆的半径
      const bound = getCriclePoints(center, radius);
      const circleMarker = window.L.polygon(bound, { color: "#ef0303" }).addTo(mbfx2dMarkerLayer);
      window.L.marker(window.L.latLng(Number(mbfxWd.value), Number(mbfxJd.value))).addTo(mbfx2dMarkerLayer);
}
// 清除图层
const clearLayerMap2D = () => {
  if (
    mbfx2dMarkerLayer != undefined &&
    mbfx2dMarkerLayer != null &&
    mbfx2dMarkerLayer != ""
  ) {
    // 清空图层
    mbfx2dMarkerLayer.clearLayers();
  }
};
const mbfxMapClose = () =>{
  mbfxTool.value = false;
  clearLayerMap2D()
  window.Map2D.xdMarkerLayerRemove()
}
const addEntityToMap = (entity) => {
      // 根据实体类型创建不同的Cesium实体
      let cesiumEntity;
      
      switch (entity.type) {
        case 'circle':
          cesiumEntity = window.viewer.entities.add({
            position: window.Cesium.Cartesian3.fromDegrees(entity.lon, entity.lat),
            name: entity.name,
            ellipse: {
              semiMinorAxis: entity.radius,
              semiMajorAxis: entity.radius,
              height: entity.minHeight,
              extrudedHeight: entity.maxHeight,
              material: window.Cesium.Color.fromCssColorString(entity.color).withAlpha(0.15),
              outline: true,
              outlineColor: window.Cesium.Color.BLACK
            },
            label: {
              text: entity.name,
              show: entity.showLabel,
              font: '14px sans-serif',
              pixelOffset: new window.Cesium.Cartesian2(0, -20)
            },
            properties: {
              id: entity.id,
              type: entity.type
            }
          });
          break;
          
        case 'rectangle':
          // 计算矩形的西南和东北坐标
          var rectangleCoords = calculateRectangleCoordinates(
            entity.lon, entity.lat, entity.width, entity.length, entity.rotation
          );
          
          cesiumEntity = window.viewer.entities.add({
            name: entity.name,
            rectangle: {
              coordinates: window.Cesium.Rectangle.fromDegrees(
                rectangleCoords.west, rectangleCoords.south,
                rectangleCoords.east, rectangleCoords.north
              ),
              height: entity.minHeight,
              extrudedHeight: entity.maxHeight,
              material: window.Cesium.Color.RED.withAlpha(0.15),
              outline: true,
              outlineColor: window.Cesium.Color.BLACK,
              rotation: window.Cesium.Math.toRadians(entity.rotation)
            },
            label: {
              text: entity.name,
              show: entity.showLabel,
              font: '14px sans-serif',
              pixelOffset: new window.Cesium.Cartesian2(0, -20),
              position: window.Cesium.Cartesian3.fromDegrees(entity.lon, entity.lat)
            },
            properties: {
              id: entity.id,
              type: entity.type
            }
          });
          break;
          
        case 'polygon':
          var positions = entity.vertices.map(v => 
            window.Cesium.Cartesian3.fromDegrees(v.lon, v.lat)
          );
          
          cesiumEntity = this.viewer.entities.add({
            name: entity.name,
            polygon: {
              hierarchy: new window.Cesium.PolygonHierarchy(positions),
              height: entity.minHeight,
              extrudedHeight: entity.maxHeight,
              material: window.Cesium.Color.fromCssColorString(entity.color).withAlpha(0.15),
              outline: true,
              outlineColor: window.Cesium.Color.BLACK
            },
            label: {
              text: entity.name,
              show: entity.showLabel,
              font: '14px sans-serif',
              pixelOffset: new window.Cesium.Cartesian2(0, -20)
            },
            properties: {
              id: entity.id,
              type: entity.type
            }
          });
          break;
      }
      
      entity.cesiumEntity = cesiumEntity;
    }
   const calculateRectangleCoordinates = (centerLon, centerLat, width, length, rotation) => {
      // 简化的矩形坐标计算，实际项目中可能需要更精确的计算
      const widthDeg = width / 111319.9; // 米转度的近似值
      const lengthDeg = length / 111319.9;
      
      return {
        west: centerLon - widthDeg / 2,
        east: centerLon + widthDeg / 2,
        south: centerLat - lengthDeg / 2,
        north: centerLat + lengthDeg / 2
      };
    };
const zbmbEntities = ref([])
const lineEntities = ref([])
const labelEntities = ref([])
const zbmbAddMap = (data) => {
       // 在地图上添加无人机标记
      zbmbEntities.value.forEach(entity => {
        console.log('xxxxx222',entity);
        window.Map3D.viewer.entities.remove(entity);
      });
      lineEntities.value.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
      labelEntities.value.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
      

      let zbmbData = [];
      data.forEach(item=>{
        let obj={}
        obj.id=item.id;
        obj.name = item.mc;
        obj.lon = Number(item.jd);
        obj.lat = Number(item.wd);
        obj.altitude = item.altitude?Number(item.altitude):20;
        obj.icon = item.mc.indexOf('医院')!=-1?'/static/map_img/医院.png':item.mc.indexOf('学校')!=-1?'/static/map_img/高等院校.png':item.mc.indexOf('加油站')!=-1?'/static/map_img/加油站.png':'/static/map_img/重要目标.png';
        zbmbData.push(obj)
      })
      
     
      const wrjData = window.TOOL.data.get('wrjData')?window.TOOL.data.get('wrjData'):{};

      zbmbData.forEach(drone => {
        const entity = window.Map3D.viewer.entities.add({
          id: drone.id,
          position: window.Cesium.Cartesian3.fromDegrees(drone.lon, drone.lat, drone.altitude),
          billboard: {
            position: window.Cesium.Cartesian3.fromDegrees(drone.lon, drone.lat, drone.altitude),
            orientation: {
              heading: 0,
              pitch: 0,
              roll: 0,
            },
            image: drone.icon,
            scale: 0.8,
            pixelSize: 40,
          },
          label: {
            text: drone.name,
            font: '14px sans-serif',
            verticalOrigin: window.Cesium.VerticalOrigin.BOTTOM,
            pixelOffset: new window.Cesium.Cartesian2(0, -15),
          },
          properties: {
            type: { _value: 'drone' },
            id: { _value: drone.id },
          },
        });

        zbmbEntities.value.push(entity);

        // 定义两个点的经纬度和高度（单位：米）
        const position1 = Cesium.Cartesian3.fromDegrees(wrjData.longitude, wrjData.latitude,wrjData.altitude);
        const position2 = Cesium.Cartesian3.fromDegrees(drone.lon, drone.lat, drone.altitude);

        // 添加连接两点的线
        const lineEntity=window.Map3D.viewer.entities.add({
          polyline: {
            positions: [position1, position2],
            width: 2,
            material: Cesium.Color.RED
          }
        });
        lineEntities.value.push(lineEntity)

         // 计算两点之间的三维空间距离（单位：米）
    const distance = Cesium.Cartesian3.distance(position1, position2).toFixed(2);
    // 计算中间点的Cartesian3坐标
    const midpoint = Cesium.Cartesian3.lerp(position1, position2, 0.5, new Cesium.Cartesian3());

   console.log(distance,midpoint,position1);


        const labelEntity=window.Map3D.viewer.entities.add({
          id:`${drone.id}-label`,
          position: midpoint,
          label: {
            text: `${distance} 米`,
            font: '14px sans-serif',
            verticalOrigin: window.Cesium.VerticalOrigin.BOTTOM,
            pixelOffset: new window.Cesium.Cartesian2(0, -15),
          },
        });
        
        labelEntities.value.push(labelEntity)
      });

    
}
const mbfxClose = () => {
  mbfxVisible.value = false;
}

const gjfxRef = ref(null)
const gjfxcsSuccess = (sj,sd,sdFlag,jd,wd) => {
  nextTick(()=>{
    gjfxRef.value.open(sj,sd,sdFlag,jd,wd)
  })
}
const gjfxcsClose = () => {
  gjfxcsVisible.value = false;
}
const pageOption = ref({
  pageNo:1,
  pageSize:10000
})
const queryInfo = ref({})
const wxdsbData = ref([])
const total = ref(0)

const getData = () =>{
  const params = JSON.parse(JSON.stringify(pageOption.value)); // Object.assign(this.queryInfo,this.pageOption)
      var arr = [];
      for(var key in queryInfo.value){
        if(queryInfo.value[key] && !(/^[\u4e00-\u9fa5a-zA-Z0-9]{1,100}$/.test(queryInfo.value[key]))){
          ElMessage.warning("查询内容不能包含特殊字符");
          return;
        }
        if(queryInfo.value[key]){
          arr.push({
            "rule": "like",
            "type": "input",
            "val": queryInfo.value[key],
            "field": key
          })
        }
      }
      if(arr.length){
        params.superQueryParams = JSON.stringify(arr)
        params.superQueryMatchType = 'and'
      }
  window.API.wxdzc.list(params).then(res=>{
    // console.log(res);
    if(res.success){
      wxdsbData.value = res.result.records;
      total.value = res.result.total;
      addPoint()
    }
    
  })
}
const getCriclePoints = (center, radius) => {
  const bound = [];
  const earthRadius = 6378137; // 地球的半径
  const dlat = (radius / earthRadius) * (180 / Math.PI);
  const dlng = dlat / Math.cos((center[0] * Math.PI) / 180);
  for (let i = 0; i < 360; i++) {
    const red = (i * Math.PI) / 180;
    const lat = center[0] + dlat * Math.sin(red);
    const lng = center[1] + dlng * Math.cos(red);
    bound.push([lat, lng]);
  }
  return bound;
};
const greenIcon = ref(null);
const deviceDetailRef = ref(null)
let zymbMarkerLayer = null;

const addPoint = () => {
  clearLayer1();
  zymbMarkerLayer = window.L.layerGroup([]);
  zymbMarkerLayer.addTo(window.Map2D.map);

  wxdsbData.value.forEach((item) => {
    // if (item.status === 'CONNECTED') {
    //   greenIcon.value = window.L.icon({
    //     iconUrl: require('@/assets/allImage/sbZc.png'),
    //     iconSize: [25, 25],
    //   });
    // } else {
    //   greenIcon.value = window.L.icon({
    //     iconUrl: require('@/assets/allImage/sbYc.png'),
    //     iconSize: [25, 25],
    //   });
    // }
    if (item.status === 'CONNECTED') {
      if (item.deviceType === 'DETECT') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/zcsb.png',
          iconSize: [25, 25],
        });
      } else if (item.deviceType === 'DISTURB') {
        greenIcon.value = window.L.icon({
          iconUrl: '/static/grsb.png',
          iconSize: [25, 25],
        });
      }else if (item.deviceType === 'TRAP') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/ypsb.png',
          iconSize: [25, 25],
        });
      }else if (item.deviceType === 'System') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/system2.png',
          iconSize: [25, 25],
        });
      }
    }else if(item.status === 'DISCONNECTED'){
      if (item.deviceType === 'DETECT') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/zcsb2.png',
          iconSize: [25, 25],
        });
      } else if (item.deviceType === 'DISTURB') {
        greenIcon.value = window.L.icon({
          iconUrl: '/static/grsb2.png',
          iconSize: [25, 25],
        });
      }else if (item.deviceType === 'TRAP') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/ypsb2.png',
          iconSize: [25, 25],
        });
      }else if (item.deviceType === 'System') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/system2.png',
          iconSize: [25, 25],
        });
      }
    }else{
      if (item.deviceType === 'DETECT') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/zcsb3.png',
          iconSize: [25, 25],
        });
      } else if (item.deviceType === 'DISTURB') {
        greenIcon.value = window.L.icon({
          iconUrl: '/static/grsb3.png',
          iconSize: [25, 25],
        });
      }else if (item.deviceType === 'TRAP') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/ypsb3.png',
          iconSize: [25, 25],
        });
      }else if (item.deviceType === 'System') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/system2.png',
          iconSize: [25, 25],
        });
      }
    }
    if (item.jd && item.wd) {
      const marker = window.L.marker(
        window.L.latLng(Number(item.wd), Number(item.jd)),
        {
          icon: greenIcon.value,
        }
      ).addTo(zymbMarkerLayer);

      const html = `<div style="width:140px;background:rgba(30, 32, 44);padding:10px">
                  <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                    <div style="width:100%;margin: 2px 0;color:#fff;display:flex;">名称：<div style="width：calc(100% - 60px);color:#fff;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;">${item.name}</div></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">经度：<span style="color:#fff;">${item.jd.toFixed(3)}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">纬度：<span style="color:#fff;">${item.wd.toFixed(3)}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">状态：<span style="color:#fff;">${item.status=='CONNECTED'?'已连接':item.status=='DISCONNECTED'?'未连接':'告警中'}</span></div>
                  </div>
                </div>`;
      marker
        .bindPopup(item.name)
        .bindTooltip(html);
      marker.on("click", function () {
        nextTick(()=>{
          deviceDetailRef.value.open(item)
        })
      });
      const center = [item.wd, item.jd];
      const radius = item.zcbj * 1000; // 圆的半径
      const bound = getCriclePoints(center, radius);
      const circleMarker = window.L.polygon(bound, { color: "#03f83c" }).addTo(zymbMarkerLayer);
      var  markerIcon = L.divIcon({
                html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>
                <div>${item.name}</div>
                <div style="color:${item.status=='CONNECTED'?'rgb(0,211,0)':'red'}">${item.status=='CONNECTED'?'已连接':'未连接'}</div>
                </div>`,//marker标注
                className: 'my-div-icon',
                iconAnchor: [80, -20]//文字标注相对位置
              });
      window.L.marker(
        window.L.latLng(Number(item.wd), Number(item.jd)),
        {
          icon: markerIcon,
        }
      ).addTo(zymbMarkerLayer);
      // const circleMarker = window.L.circle(center, radius, {
      //           color: '#03f83c',
      //           weight: 2
      //       }).addTo(zymbMarkerLayer);
      // animateCircle(circleMarker,radius);
    }
  });
};

// 清除图层
const clearLayer1 = () => {
  if (
    zymbMarkerLayer != undefined &&
    zymbMarkerLayer != null &&
    zymbMarkerLayer != ""
  ) {
    // 清空图层
    zymbMarkerLayer.clearLayers();
  }
};
</script>

<style scoped lang="less">
.toggleBottom{
  width: 100vw;
  height:25vh;
  background: url("@/assets/bottomTitle/bottom-bg.png") no-repeat;
  background-size: 100% 100%;
  position:absolute;
  bottom:0;
  pointer-events: auto;
  color: #fff;    
  padding-top: 50px;
  box-sizing: border-box;
}
#bottomMenu{
  transform: translateY(0);
  transition: transform 0.3s ease;
}
.tabsBottom{
  position:absolute;
  top:20px;
  span{
    padding:5px 10px;
    cursor:pointer;
  }
  span:nth-of-type(2){
    // margin-left:10px;
  }
  .tabsActive{
    border-bottom: 3px solid #0083cb;
    color: #0083cb;
  }
}
.bottomToggleBtn{
  position:absolute;
  top:28px;
  right:15px;
  cursor:pointer;
      width: 25px;
      padding: 2px;
}
// .bottomToggleBg1{
//       background:url('/public/static/toggle/toggle-bg1.jpg') no-repeat;
//       background-size:100% 100%;
//     }
//     .bottomToggleBg2{
//       background:url('/public/static/toggle/toggle-bg2.jpg') no-repeat;
//       background-size:100% 100%;
//     }
.bottomBox {
  width:100%;
  height:100%;
  display: flex;
  justify-content: space-between;
  // background-color: rgba(9, 26, 58,0.6);
  .bottom-left{
    width:38%;
    position:relative;
    transition: width 0.5s ease-in-out;
    .bottom-title{
      display: flex;
      align-items:center;
      flex-wrap: wrap;
      font-size: 14px;
      font-weight: bold;
      &>div{
          width:calc(100% / 11);
          text-align: center;
      }
      // &>div:nth-of-type(1){
      //   width: 40%;
      //   text-align: left;
      // }
      // &>div:nth-of-type(2){
      //   width: 18%;
      //   text-align: center;
      // }
      // &>div:nth-of-type(3){
      //   width: 18%;
      //   text-align: center;
      // }
      // &>div:nth-of-type(4){
      //   width: 24%;
      //   text-align: center;
      // }
    }
    .bottom-content{
      width: 100%;
      height: calc(100% - 54px);
      background: url("@/assets/allImage/bottom-content-bg.png") no-repeat;
      background-size: 100% 100%;
      overflow: hidden;
      overflow-y: auto;
      font-size: 14px;
      .kydt-item{
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin: 10px 0;
        &>div:nth-of-type(1){
          // width: 40%;
          text-align: left;
          display: flex;
          align-items: center;
          padding-left: 10px;
          box-sizing: border-box;
          .dot{
            width: 5px;
            height: 5px;
            // border-radius: 50%;
            background: #016DA3;
            margin: 0 3px;
          }
        }
        &>div{
          width:calc(100% / 11);
          text-align: center;
          overflow-wrap: anywhere;
          word-break: break-word;
        }
        .text-ellipsis {
          display: -webkit-box;
          -webkit-box-orient: vertical;
          -webkit-line-clamp: 3;
          overflow: hidden;
          text-overflow: ellipsis;
          word-break: break-word;
          line-height: 1.4;
          max-height: 4.2em;
        }
        // &>div:nth-of-type(2){
        //   width: 18%;
        //   text-align: center;
        // }
        // &>div:nth-of-type(3){
        //   width: 18%;
        //   text-align: center;
        // }
        // &>div:nth-of-type(4){
        //   width: 24%;
        //   text-align: center;
        // }
      }
    }

    .bottom-left-btn{
      position:absolute;
      top:0;//50%;
      // transform:translateY(-50%);
      // right:-15px;
      cursor:pointer;
      z-index:5;
    }
  }
  .bottom-center{
    width:24%;
    position:relative;
    .menus{
      width:100%;
      height: 100%;
      display: flex;
      justify-content: space-between;
      color:#fff;
    }
    .menu-item{
      font-size: 14px;
      &>div{
        text-align: center;
      }
      img{
        width: 20px;
        height: 20px;
        margin-bottom: 5px;
      }
      
    }
    .menu-item:hover{
      background:#09327c;
    }
    .menu-item-active{
      color:#67c9ff;
      background:#09327c;
    }
    // .menu-item:nth-of-type(3) img{
    //   width: 30px;
    //   height: 30px;
    //   margin-bottom: 0;
    // }
    .menu-item:nth-of-type(1){
      position: absolute;
      left: 50px;
      bottom: 30px;
    }
    
    .menu-item:nth-of-type(2){
      position: absolute;
      left: 90px;
      bottom: 85px;
    }
    // .menu-item:nth-of-type(3){
    //   position: absolute;
    //   left: 145px;
    //   bottom: 138px;
    // }

    // .menu-item:nth-of-type(4){
    //   position: absolute;
    //   right: 145px;
    //   bottom: 138px;
    // }

    .menu-item:nth-of-type(3){
      position: absolute;
      right: 90px;
      bottom: 85px;
    }

    .menu-item:nth-of-type(4){
      position: absolute;
      right: 50px;
      bottom: 30px;
    }

    .iconImg{
      width: 155px;
      height: 155px;
      position: absolute;
      bottom:0;
      left:50%;
      transform: translateX(-50%);
    }
  }
  .bottom-right{
    width:38%;
    &>div{
      width: 100%;
      height: 100%;
    }
    .bottom-title{
      display: flex;
      align-items:center;
      flex-wrap: wrap;
      font-size: 16px;
      font-weight: bold;
      .mdActive{
        border-bottom:3px solid #3c6085;
      }
      &>div:nth-of-type(1){
        width: 50%;
        height:45px;
        line-height:45px;
        text-align: center;
        cursor:pointer;
      }
      &>div:nth-of-type(2){
        width: 50%;
        height:45px;
        line-height:45px;
        text-align: center;
        cursor:pointer;
      }
      // &>div:nth-of-type(3){
      //   width: 33%;
      //   text-align: center;
      // }
    }
    .bottom-title2{
      display: flex;
      align-items:center;
      flex-wrap: wrap;
      &>div{
        display: flex;
        align-items:center;
        margin-right: 20px;
      }
    }
    .bottom-content{
      width: 100%;
      height: calc(100% - 45px);
      box-sizing: border-box;
      padding: 10px 0;
      // display: flex;
      // align-items: center;
      // justify-content: space-between;
      font-size: 14px;
      .hbmdItems{
        height:calc(100% - 30px);
        overflow:auto;
      }
      .hbmd-header,.hbmd-item{
        display:flex;
        align-items:center;
        &>div:nth-of-type(1){
          width:50%;
          text-align:center;
        }
        &>div:nth-of-type(2){
          width:50%;
          text-align:center;
        }
      }
      .fxjh-item{
        margin: 5px 0;
        width: 33%;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-wrap: wrap;
        &>div{
          width: 100%;
          text-align: center;
        }
        &>div:nth-of-type(1){
          width: 80px;
          height: 80px;
          background: url("@/assets/allImage/fxjhIcon.png") no-repeat;
          background-size: 100% 100%;
          display: flex;
          align-items: center;
        }
      }
    }
  }
}

.create-bottom {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
}
.mapTool{
      position: absolute;
    // bottom: 225px;
    // right: 368px;
    top:51px;
    right:60px;
    z-index: 111;
    display: flex;
    background: #253c8e;
    color: #fff;
    align-items:center;
    padding:5px 10px;
  pointer-events: auto;
}


/* 频谱容器样式 */
        .spectrum-container {
            flex: 1;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 2rem;
            position: relative;
        }

        .frequency-axis {
            display: flex;
            justify-content: space-between;
            width: 90%;
            max-width: 1200px;
            margin-bottom: 1rem;
        }

        .frequency-mark {
            font-size: 0.9rem;
            opacity: 0.7;
            width: 40px;
            text-align: center;
        }

        .spectrum-canvas {
            width: 90%;
            max-width: 1200px;
            height: 400px;
            background: rgba(0, 0, 0, 0.2);
            border-radius: 10px;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
            backdrop-filter: blur(4px);
            border: 1px solid rgba(255, 255, 255, 0.1);
        }

        .light-mode .spectrum-canvas {
            background: rgba(255, 255, 255, 0.8);
            border: 1px solid rgba(0, 0, 0, 0.1);
        }

        footer {
            padding: 1.5rem;
            text-align: center;
            font-size: 0.9rem;
            opacity: 0.7;
        }

        /* 响应式适配 */
        @media (max-width: 768px) {
            h1 {
                font-size: 2rem;
            }

            .spectrum-canvas {
                height: 300px;
            }

            .controls, .data-filter {
                gap: 1rem;
            }

            .control-group, .filter-group {
                flex-direction: column;
                gap: 0.5rem;
                text-align: center;
            }

            .data-info {
                gap: 1rem;
            }
        }

        @media (max-width: 480px) {
            .spectrum-canvas {
                height: 250px;
            }

            .frequency-mark {
                font-size: 0.8rem;
            }

            .data-filter {
                margin: 0 1rem 2rem;
                padding: 1rem;
            }
        }
</style>