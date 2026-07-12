<template>
  <div class="topCenter">
    <el-radio-group @change="grChange" v-model="grType" style="pointer-events: auto;margin-left:30px">
          <el-radio-button label="福建总队">福建总队</el-radio-button>
          <el-radio-button label="指控平台">指控平台</el-radio-button>
        </el-radio-group>
    <div class="gr1" v-show="grType=='福建总队'">
    <div class="left">
      <div class="sbqk">
        <div>
          <el-form-item label="干扰设备">
                 <el-select v-model="stationId" placeholder="请选择" clearable filterable @change="deviceChange">
                      <el-option v-for="(item,index) in deviceData" :key="index" :label="item.name+'('+ item.pjName +')'" :value="item.stationId"></el-option>
                    </el-select>
              </el-form-item>
        </div>
        <div class="fxKdp">
          <img src="@/assets/allImage/kdp.png" style="width:145px;height:145px;"/>
          <img src="@/assets/allImage/zz.png" class="zz" :style="{'transform': 'rotateZ('+deviceDetail.angle+'deg)'}"/>
          <div>方位：{{deviceDetail.angle}}</div>
        </div>
        <!-- <div style="text-align: center;
          margin-top: 10px;
          padding-top: 10px;
          border-top: 1px dotted #fff;">
          工作时长：12:52
        </div> -->
        <div class="sbxx">
          <div class="progress-container">
            <div class="progress-title">CPU使用</div>
            <div class="progress-wrapper">
              <el-progress :percentage="deviceDetail.cpuRate" :stroke-width="15" striped />
            </div>
          </div>

          <div class="progress-container">
            <div class="progress-title">设备温度</div>
            <div class="progress-wrapper">
              <el-progress :percentage="deviceDetail.cardTemp" :stroke-width="15" striped :format="format"/>
            </div>
          </div>

          <div class="progress-container">
            <div class="progress-title">硬盘使用</div>
            <div class="progress-wrapper">
              <el-progress :percentage="deviceDetail.diskUsage?(deviceDetail.diskUsage / 100).toFixed(0):0" :stroke-width="15" striped />
            </div>
          </div>
        </div>

        <div class="btns">
          <div v-for="(item,index) in btns" :key="index" @click="getBtns(index)" :class="btnIndex == index?'btnActive btnClass':'btnClass'">{{item}}</div>
        </div>
      </div>
    </div>
    <div class="right">
      <!-- <div class="tianxian">
        <div class="title">
          天线

          <el-button @click="controlDevice">确认</el-button>
        </div>
        <div class="content">
          <div class="radar"></div>
          <div class="content-right">
            <div>
              <el-form-item label="干扰时长">
                    <el-input v-model="txForm.grsc" placeholder="请输入"></el-input>
              </el-form-item>
            </div>
             <div>
              <el-form-item label="扫描方式">
                 <el-select v-model="txForm.controlTxDto.dirLoop " placeholder="请输入" style="width:140px;">
                      <el-option v-for="(item,index) in smfsOption" :key="index" :label="item.label" :value="item.value"></el-option>
                    </el-select>
              </el-form-item>
            </div>
            <div>
              <el-form-item label="角度选择" v-if="txForm.controlTxDto.dirLoop==0">
                    
                    <el-input v-model="txForm.controlTxDto.direction" placeholder="请输入"></el-input>
              </el-form-item>
            </div>
            <div>
              <el-form-item label="当前天线">
                    <el-input v-model="txForm.dqtx" placeholder="请输入"></el-input>
              </el-form-item>
            </div>
          </div>
        </div>
      </div> -->
      <div class="xhgr">
        <div class="title">
          通道状态
          <el-button @click="controlDevice2">确认</el-button>
        </div>
        <div class="content">
          <div>
              <el-form-item label="干扰设备序号">
                 <el-select v-model="txForm.disturbType" placeholder="请输入" style="width:100%;">
                      <el-option v-for="(item,index) in grfsOption" :key="index" :label="item.label" :value="item.value"></el-option>
                    </el-select>
              </el-form-item>
              <el-switch  :active-value="1" :inactive-value="0" v-model="txForm.enable"></el-switch>
          </div>
          <!-- <div class="tdDataClass" v-if="txForm.disturbType==0">
            <div v-for="(item,index) in  tdData" :key="index" @click="tdClick(index,item)" :class="tdIndex==index?'tdItem tdActiveItem':'tdItem'">
              {{item.name}}
            </div>
          </div>
          <div class="td" v-if="txForm.disturbType==0">
            <div>
              <el-form-item label="当前通道">
                    <el-input v-model="txForm.channelName" disabled placeholder="请输入"></el-input>
              </el-form-item>
              <el-switch  :active-value="1" :inactive-value="0" v-model="txForm.channelEnable"></el-switch>
            </div>
            <div>
              <el-form-item label="工作模式">
                    <el-select v-model="txForm.workMode " placeholder="请输入" style="width:100%;">
                      <el-option v-for="(item,index) in workModeOption" :key="index" :label="item.label" :value="item.value"></el-option>
                    </el-select>
              </el-form-item>
            </div>
            <div>
              <el-form-item label="干扰频率">
                    <el-input v-model="txForm.freq" placeholder="请输入"></el-input>
              </el-form-item>
              <div>
                <div style="margin-bottom:8px;">MHZ</div>
              </div>
            </div>
            <div>
              <el-form-item label="扫描带宽">
                    <el-input v-model="txForm.bw" placeholder="请输入"></el-input>
              </el-form-item>
              <div>
                <div style="margin-bottom:8px;">MHZ</div>
              </div>
            </div>
            <div>
              <el-form-item label="扫描点数">
                    <el-input v-model="txForm.scanPoint" placeholder="请输入"></el-input>
              </el-form-item>
              <div>
                <div style="margin-bottom:8px;">MHZ</div>
              </div>
            </div>
            <div>
              <el-form-item label="扫描速度">
                    <el-select v-model="txForm.scanSpeed " placeholder="请输入" style="width:100%;">
                      <el-option v-for="(item,index) in scanSpeedOption" :key="index" :label="item.label" :value="item.value"></el-option>
                    </el-select>
              </el-form-item>
            </div>
            <div>
              <el-form-item label="功率衰减">
                    <el-input v-model="txForm.att" placeholder="请输入"></el-input>
              </el-form-item>
              <div style="width:70px">
                <div style="margin-bottom:8px;">(0-63db)</div>
              </div>
            </div>
          </div> -->
        </div>
      </div>
    </div>
    </div>

    <div class="gr2" v-show="grType=='指控平台'">
      <div class="grkz">
        <div class="title">
          无线电干扰控制
        </div>
        <div class="content">
          <div>
          <el-form-item label="第三方指控平台">
                 <el-select v-model="zkptStationId" placeholder="请选择" clearable filterable @change="deviceChange">
                      <el-option v-for="(item,index) in zkptDeviceData" :key="index" :label="item.name+'('+ item.pjName +')'" :value="item.stationId"></el-option>
                    </el-select>
              </el-form-item>
        </div>
          <div>
            <el-checkbox-group
              v-model="checkTypeList"
              @change="handleTimeChange"
              size="default"
              fill="#5498db"
              text-color="#5498db"
            >
              <el-checkbox v-for="(t,i) in checkBoxOptions" :key="i" :label="t.value">{{t.label}}</el-checkbox>
            </el-checkbox-group>
          </div>

          <!-- <div>
            <el-checkbox-group
              v-model="checkFxList"
              @change="handleTimeChange"
              size="default"
              fill="#5498db"
              text-color="#5498db"
            >
              <el-checkbox v-for="(t,i) in checkBoxFxOptions" :key="i" :label="t.value">{{t.label}}</el-checkbox>
            </el-checkbox-group>
          </div> -->

          <div  style="text-align:center;margin-top:10px;padding-bottom:10px;display: flex;justify-content: space-between;align-items: center;flex-wrap: wrap;border-bottom:1px solid #1582bf;">
              <el-button type="success" @click="tcEvent">探测</el-button>
              <el-button type="success" :disabled="flyStatusFlag" @click="flyStatusEvent('返航')">返航</el-button>
              <el-button type="success" :disabled="!flyStatusFlag" @click="flyStatusEvent('迫降')">迫降</el-button>
              <el-button type="success" @click="gjEvent">攻击</el-button>
              <el-button type="success" @click="wrzsEvent">无人值守</el-button>
            </div>
          <div style="color:#fff;padding-top:10px">
            &nbsp;&nbsp;在无人机反制与无线电干扰场景中，各频段的典型用途如下：<br/>
            &nbsp;&nbsp;5.8 GHz：主要针对消费级航拍无人机（如大疆、飞米等主流机型），该频段是其图传与遥控信号的核心工作频段，干扰后可切断无人机与遥控器的通信链路，使其失去控制或自动返航。<br/>
            &nbsp;&nbsp;2.4 GHz：覆盖多数入门级无人机、FPV 穿越机及部分消费级机型，同时也是 Wi-Fi、蓝牙等民用无线设备的共用频段，干扰后可同时影响无人机遥控、图传及周边无线通信。<br/>
            &nbsp;&nbsp;1.4 GHz：主要针对工业级 / 行业级无人机（如测绘、巡检、植保无人机），部分长航时、远距离作业机型会采用此频段进行遥控或数据传输，干扰后可阻断其远程控制与作业指令。<br/>
            &nbsp;&nbsp;<1 GHz（含 1 GHz 以下至 1.4 GHz 区间）：重点针对军用 / 特种无人机、长距离侦察无人机，这类机型常使用低频段实现超视距通信与抗干扰能力，干扰此频段可有效压制其远程操控与数据回传。
          </div>
          
        </div>
      </div>
    </div>

    
  </div>
</template>

<script setup>
// 模拟推演
import { useRouter } from "vue-router";
import microApp from "@/components/microApp/index.vue";
import { ref, onMounted,onUnmounted } from "vue";
import icon1 from "@/assets/leftTitle/leftImg.png"
import wxdzcsb from "@/assets/allImage/wxdzcsb.png"
import { ElNotification, ElMessageBox, ElMessage } from "element-plus";
// 定义路由
const router = useRouter();
const grType = ref("福建总队")
const checkTypeList = ref([])
const flyStatusFlag = ref(false);
const checkBoxOptions = ref([
  {
    label:"5.8GHz",
    value:1
  },
  {
    label:"2.4GHz",
    value:2
  },
  {
    label:"1.4GHz",
    value:8
  },
  {
    label:"<1GHz",
    value:4
  },
])
const checkFxList = ref([])
const checkBoxFxOptions= ref([
  {
    label:"定向干扰",
    value:"定向干扰"
  },
  {
    label:"全向干扰",
    value:"全向干扰"
  },
])
const wxdsbVisible = ref(false)
const queryInfo = ref({
  isValid:1,
  name:""
})
const wxdsbData = ref([])
const txForm = ref({
  smfs:"定向",
  jd:"0-60°",
  grfs:"自定义",
  channelName:"通道1",
  channel:"1",
  controlTxDto:{
    direction:0
  },
  channelEnable:false
})
const pageOption = ref({
  pageNo:1,
  pageSize:10
})
const total = ref(0)
const tdData = ref([
  {name:"通道1",value:"1"},
  {name:"通道2",value:"2"},
  {name:"通道3",value:"3"},
  {name:"通道4",value:"4"},
  {name:"通道5",value:"5"},
])
const workModeOption = ref([
  {label:"点频",value:"0"},
  {label:"连续正线性扫频",value:"1"},
  {label:"连续负线性扫频",value:"2"},
  {label:"FSK 调制",value:"3"},
  {label:"PSK 调制",value:"4"},
  {label:"DK 调制",value:"5"},
])
const scanSpeedOption = ref([
  {label:"A",value:"1"},
  {label:"2×A",value:"2"},
  {label:"3×A",value:"3"},
  {label:"4×A",value:"4"},
])
const grfsOption = ref([
  // {label:'自定义',value:'0'},
  // {label:'迫降',value:'1'},
  // {label:'返航',value:'2'},
  {label:'第一台',value:'0'},
  {label:'第二台',value:'1'},
  {label:'第三台',value:'2'},
  {label:'第四台',value:'3'},
])
const smfsOption = ref([
  {label:'定向',value:'0'},
  {label:'全向扫描',value:'1'},
  {label:'交叉扫描',value:'2'},
])
const jdOption = ref([
  {label:'0-60°',value:'0-60°'},
  {label:'60°-120°',value:'60°-120°'},
  {label:'120°-180°',value:'120°-180°'},
  {label:'180°-240°',value:'180°-240°'},
  {label:'240°-300°',value:'240°-300°'},
  {label:'300°-360°',value:'300°-360°'},
])
const btns = ref(["黑白名单","训练录屏"]) //"历史记录","无人机库",
// 初始化
onMounted(()=>{
  getDevice()
  getzkptDevice()
  window.eventBus.on("sb",(data)=>{
    console.log(data);
  })
})
onUnmounted(()=>{
  window.eventBus.off('stationId')
  window.eventBus.off('sb')
})
const attackMode = ref(null)
const targetDetectionEnable = ref(false) //探测
const deviceAttackEnable = ref(false) //攻击
const unattendedModeEnable = ref(false) //无人值守

const tcEvent = () =>{
  targetDetectionEnable.value = !targetDetectionEnable.value
  ControlDeviceTcpServerSocketServerEvent()
}

const gjEvent = () =>{
  deviceAttackEnable.value = !deviceAttackEnable.value
  ControlDeviceTcpServerSocketServerEvent()
}

const wrzsEvent = () =>{
  unattendedModeEnable.value = !unattendedModeEnable.value
  ControlDeviceTcpServerSocketServerEvent()
}

const flyStatusEvent = (e) => {
  flyStatusFlag.value = !flyStatusFlag.value
  if(e=="返航"){
    attackMode.value = 0;
  }else if(e=="迫降"){
    attackMode.value = 1;
  }
  ControlDeviceTcpServerSocketServerEvent()
}
const ControlDeviceTcpServerSocketServerEvent = () => {
  window.API.device.ControlDeviceTcpServerSocketServer({
    azimuth:1,
    speed:1,
    jl:1,
    gnssSpoofMode:0,
    gnssInduceType:1,
    gnssSpoofLinkageEnable:false,
    attackFrequencyBand:checkTypeList.value?checkTypeList.value[checkTypeList.value.length-1]:1,
    attackMode:attackMode.value?attackMode.value:0,
    targetDetectionEnable:targetDetectionEnable.value,
    deviceAttackEnable:deviceAttackEnable.value,
    unattendedModeEnable:unattendedModeEnable.value,
    stationId:zkptStationId.value
  }).then(res=>{
    if(res.code==200){
      ElMessage.success("操作成功")
    }else{
      ElMessage.error(res.message)
    }
  })
}
const grChange = (e) => {
  console.log(e);
}
const tdIndex = ref(0)
const tdClick = (index,item) =>{
  tdIndex.value = index;  
  txForm.value.channel = item.value;
  txForm.value.channelName = item.name;
  txForm.value.channelEnable = false;
  txForm.value.workMode="";
  txForm.value.freq="";
  txForm.value.bw="";
  txForm.value.scanSpeed="";
  txForm.value.scanPoint="";
  txForm.value.att="";
}
const format = (percentage) => (`${percentage}°`)
const btnIndex = ref(-1)
const getBtns = (index) =>{
  btnIndex.value= index;
  if(index==0){
    router.push({
      path:"/dataManage",
      query:{dz:'hbmd'}
    })
  }
  // else if(index==1){
  //   router.push({
  //     path:"/dataManage",
  //     query:{dz:'czrz'}
  //   })
  // }else if(index==2){
  //   router.push({
  //     path:"/dataManage",
  //     query:{dz:'wrj'}
  //   })
  // }
  else if(index==1){
    router.push({
      path:"/dataManage",
      query:{dz:'lprjcs'}
    })
  }
}
const deviceData = ref([])
const stationId = ref("")
const getDevice = () =>{
      window.API.sbgl.list({
        pageNo:1,
        pageSize:10000,
        deviceType:"DETECT"
      }).then(res=>{
        if(res.code == 200){
          deviceData.value = res.result.records;
          if(deviceData.value && deviceData.value.length){
            deviceData.value.forEach(item=>{
              item.pjName = item.status=='CONNECTED'?'已连接':'未连接'
            })
            stationId.value = deviceData.value[0].stationId;
            deviceChange()
          }
        }
      })
}

const zkptDeviceData = ref([])
const zkptStationId = ref("")
const getzkptDevice = () =>{
      window.API.sbgl.list({
        pageNo:1,
        pageSize:10000,
        deviceType:'System'
      }).then(res=>{
        if(res.code == 200){
          zkptDeviceData.value = res.result.records;
          if(zkptDeviceData.value && zkptDeviceData.value.length){
            zkptDeviceData.value.forEach(item=>{
              item.pjName = item.status=='CONNECTED'?'已连接':'未连接'
            })
            zkptStationId.value = zkptDeviceData.value[0].stationId;
            deviceChange()
          }
        }
      })
}

const deviceDetail = ref({
  diskUsage:0
})
const deviceChange = () =>{
  window.eventBus.emit('stationId',stationId.value)
  window.API.xhgr.selectLatestHeartbeat({
    stationId:stationId.value
  }).then(res=>{
        if(res.code == 200){
          if(res.result){
            deviceDetail.value = res.result;
          }else{
            deviceDetail.value = {}
          }
          
        }
      })
}
const  controlDevice= () =>{
  window.API.xhgr.ControlDevice({
    stationId:stationId.value,
    "type":"天线控制",
    "controlTxDto":{
      "dirLoop":txForm.value.controlTxDto.dirLoop,
      "direction":txForm.value.controlTxDto.direction
    }
  }).then(res=>{
        if(res.success){
          ElMessage.success("操作成功")
        }
      })

}
const controlDevice2 = () =>{
  let params={}
  // if(txForm.value.disturbType == "0"){
  //   params={
  //     stationId:stationId.value,
  //     type:"自定义",
  //     disturbType:txForm.value.disturbType,
  //     enable:txForm.value.enable,
  //     disturbSelfParam:{
  //       enable:txForm.value.channelEnable,
  //       channel:txForm.value.channel,
  //       workMode:txForm.value.workMode,
  //       freq:txForm.value.freq,
  //       bw:txForm.value.bw,
  //       scanSpeed:txForm.value.scanSpeed,
  //       scanPoint:txForm.value.scanPoint,
  //       att:txForm.value.att
  //     }
  //   }
  // }else{
    params={
      stationId:stationId.value,
      type:"快速干扰",
      disturbType:txForm.value.disturbType,
      enable:txForm.value.enable,
    }
  // }
  window.API.xhgr.ControlDevice(params).then(res=>{
        if(res.success){
          ElMessage.success("操作成功")
        }
      })
}
</script>

<style scoped lang="less">
.topCenter {
  width: 100%;
  height: 100%;
  pointer-events: none;
  .gr1{
    width: 100%;
    height: calc(100% - 50px);
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: relative;
    pointer-events: none;
    margin-top:10px;
    :deep(.el-button){
        // background-color: rgba(97, 137, 177,0.8) !important;
        color: #fff  !important;
        margin-left:10px;
        background: url(@/assets/allImage/btnBg.png) no-repeat;
        background-size: 100% 100%;
        border:none;
      }
  }
  .left{
    width: 400px;
    height: 100%;
    position: absolute;
    left: 30px;
    top: 0;
    pointer-events: auto;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    color:#fff;
    .sbqk{
      height: 88%;
      padding: 10px;
      // margin-top: calc(23% / 2);
      background: url("@/assets/allImage/dialogBg.png") no-repeat;
      background-size: 100% 100%;
      box-sizing: border-box;
      .fxKdp{
        text-align: center;
        .zz{
          width: 10px;
          height: 140px;
          position: absolute;
          left: 50%;
          transition: tr;
          transform: translateX(-50%);
        }
      }
      .sbxx{
        height: 265px;
        display: flex;
        flex-wrap: wrap;
        .progress-container {
          display: flex;
          flex-direction: column;
          align-items: center;
          margin: 10px 0;
          width:33%;
        }

        .progress-title {
          width: 100%;
          text-align: center;
          margin-bottom: 10px;
        }

        .progress-wrapper {
          position: relative;
          width: 125px;
          height: 150px;
          transform: rotate(90deg);
          margin: 0 auto;
        }

        .progress-wrapper .el-progress {
          // transform: rotate(-90deg);
          width: 100%;
          height: 100%;
        }

        .el-progress {
          position: absolute;
          top: 0;
          left: 0;
        }
        :deep(.el-progress-bar__outer){
          width: 120px;
        }
        :deep(.el-progress__text){
          transform: rotate(-90deg);
          min-width: auto;
        }
        :deep(.progress-container:nth-of-type(2) .el-progress__text){
          margin-left: 12px;
        }
        // :deep(.progress-container:nth-of-type(3) .el-progress__text){
        //   margin-left: 10px;
        // }
      }
      .btns{
        display: flex;
        align-items: center;
        justify-content: space-between;
        width: 50%;
        margin: 0 auto;
        .btnClass{
           background: url("@/assets/allImage/btn-bg.png") no-repeat;
          background-size: 100% 100%;
          padding: 5px 10px;
          cursor: pointer;
          color: #fff;
        }
        .btnActive{
           background: url("@/assets/allImage/btn-active-bg.png") no-repeat;
          background-size: 100% 100%;
          padding: 5px 10px;
        }
      }
    }
  }
  .right{
    width: 400px;
    height: 100%;
    position: absolute;
    right: 30px;
    top: 0;
    pointer-events: auto;
    // display: flex;
    // flex-direction: column;
    // justify-content: space-between;
  }
  .tianxian{
    height: 35%;
    background: url("@/assets/allImage/dialogBg.png") no-repeat;
    background-size: 100% 100%;
    box-sizing: border-box;
    .content{
      display: flex;
      justify-content: space-between;
      color: #fff;
      font-size: 14px;
      padding:10px;
    box-sizing: border-box;
      height: calc(100% - 37px);
      .radar{
          background: url("@/assets/allImage/radar.png") no-repeat;
          background-size: 100% 100%;
        }
      &>div:nth-of-type(1){
        width: 45%;
      }
      &>div:nth-of-type(2){
        width: 55%;
      }
      .content-right{
        &>div{
          display: flex;
          align-items: center;
        }
        .label{
          width:90px;
        }
      }
    }
  }

  .xhgr{
    height:42%;
    background: url("@/assets/allImage/dialogBg.png") no-repeat;
    background-size: 100% 100%;
    box-sizing: border-box;
    // margin-top:30px;
     .content{
        color: #fff;
        font-size: 14px;
        padding:10px;
        box-sizing: border-box;
      height: calc(100% - 37px);
        &>div:nth-of-type(1){
          display: flex;
          justify-content: space-between;
          align-items: center;
        }
        .tdDataClass{
          display: flex;
          align-items: center;
          margin-left: 10px;
          margin-bottom: 8px;
          .tdItem{
            background: url("@/assets/allImage/tdBg.png") no-repeat;
            background-size: 100% 100%;
            padding: 5px 10px;
            cursor: pointer;
          }
          .tdActiveItem{
            background: url("@/assets/allImage/tdActiveBg.png") no-repeat;
            background-size: 100% 100%;
          }
        }

        .td{
          &>div{
            display: flex;
            justify-content: space-between;
            align-items: center;
          }
        }
     }
  }
  .title{
    padding-left: 8px;
    height: 37px;
    padding-left: 23px;
    color: #fff;
        display: flex;
    align-items: center;
    justify-content: space-between;
    background: url("@/assets/allImage/header-bg.png") no-repeat;
            background-size: 100% 100%;
  }

  .gr2{
    width: 100%;
    height: calc(100% - 50px);
    position: relative;
    pointer-events: none;
    margin-top:10px;
    .grkz{
      width: 525px;
      height: 100%;
      pointer-events: auto;
      background: url("@/assets/allImage/dialogBg.png") no-repeat;
      background-size: 100% 100%;
      box-sizing: border-box;
       position: absolute;
      left: 30px;
      top: 0;
      .content{
        padding:10px;
        width: 100%;
        height: calc(100% - 40px);
        box-sizing: border-box;
        &>div:nth-of-type(1){
          border-bottom:1px solid #1582bf;
        }
         &>div:nth-of-type(2){
          border-bottom:1px solid #1582bf;
        }
      }
    }
    button{
      width:46%;
      padding: 5px 15px;
      height:50px;
      margin-top:8px;
    }
    :deep(.el-button+.el-button){
      margin-left:0;
    }
    :deep(.el-checkbox__input.is-checked+.el-checkbox__label){
      color:#0083cb;
    }
    
  }
  
:deep(.el-form-item__label){
  color: #fff;
  font-size: 14px;
  margin-left:10px;
}
:deep(.el-select__wrapper){
  background: transparent;
  box-shadow: 0 0 0 1px #4a66a5 inset;
}
:deep(.el-select__placeholder){
  color: #fff  !important;
}
:deep(.el-input__wrapper){
  background: transparent;
  box-shadow: 0 0 0 1px #4a66a5 inset;
}
:deep(.el-input__wrapper){
        // background-color: rgba(97, 137, 177,0.8) !important;
        // border-color: rgb(97, 137, 177)
        background: url(@/assets/allImage/inputBg.png) no-repeat;
        background-size: 100% 100%;
        border:none;
      }
      :deep(.el-input__inner){
        color: #fff  !important;
        border:none;
      }
      
      :deep(.el-switch){
        margin-bottom: 8px;
      }
      :deep(.el-form-item){
        margin-bottom: 8px;
        width: 100%;
        margin-right: 5px;
      }
}
</style>