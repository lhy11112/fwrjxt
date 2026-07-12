<template>
  <div class="topCenter">

    <el-radio-group @change="ypChange" v-model="ypType" style="pointer-events: auto;margin-left:30px">
          <el-radio-button label="福建总队">福建总队</el-radio-button>
          <el-radio-button label="指控平台">指控平台</el-radio-button>
        </el-radio-group>
    <div class="yp1" v-show="ypType=='福建总队'">
      <div class="left">
        <div class="sbqk">
          <div>
            <el-form-item label="诱骗设备">
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
        <div class="dhyp">
          <div class="title">诱骗</div>
          <div class="content">
            <!-- <div>
                <el-form-item label="自动">
                      <el-input v-model="txForm.zd1" placeholder="请输入" style="width:130px;margin-right:20px"></el-input>
                      <el-input v-model="txForm.zd2" placeholder="请输入" style="width:130px;"></el-input>
                </el-form-item>
                <el-switch v-model="txForm.zdFlag"></el-switch>
            </div> -->
            <div>
                <el-form-item label="衰减">
                      <el-input v-model="txForm.amp" @blur="ampChange" placeholder="请输入" ></el-input>
                </el-form-item>
                
                <div style="width:90px;margin-right:5px;">
                  <div style="margin-bottom:8px;">(0-80db)</div>
                </div>
                <!-- <el-switch v-model="txForm.sjEnable"></el-switch> -->
            </div>
            <div>
                <el-form-item label="诱骗模式">
                      <el-select v-model="txForm.trapType"  placeholder="请输入" style="width:100%;" @change="trapTypeChange">
                        <el-option v-for="(item,index) in ypmsOption" :key="index" :label="item.label" :value="item.value"></el-option>
                      </el-select>
                </el-form-item>
                <el-switch v-model="txForm.ypEnable" :active-value="1" :inactive-value="0" @change="trapTypeChange"></el-switch>
            </div>
            <div>
                <el-form-item label="禁飞功能">
                  <el-switch v-model="txForm.jfEnable" :active-value="1" :inactive-value="0" @change="jfEnableChange"></el-switch>
                </el-form-item>
            </div>
            <div>
                <el-form-item label="驱离功能">
                  <el-switch v-model="txForm.qlEnable" :active-value="1" :inactive-value="0" @change="qlEnableChange"></el-switch>
                </el-form-item>
            </div>

            <div>
                <el-form-item label="是否诱骗">
                  <el-switch v-model="txForm.isEnable" :active-value="1" :inactive-value="0" @change="isEnableChange"></el-switch>
                </el-form-item>
            </div>

            <div>
                <el-form-item label="无人机位置">
                  <el-input placeholder="请点击选择无人机位置" v-model="txForm.wrjwz" readOnly :disabled="true" clearable>
                    <template #append><el-icon style="cursor: pointer;" @click="wrjMange"><Location /></el-icon></template>
                  </el-input>
                </el-form-item>
            </div>

            <div>
                <el-form-item label="诱骗位置">
                  <el-input placeholder="请点击选择诱骗位置" v-model="txForm.ypwz" readOnly :disabled="true" clearable>
                    <template #append><el-icon style="cursor: pointer;" @click="closeDialog"><Location /></el-icon></template>
                  </el-input>
                </el-form-item>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="yp2" v-show="ypType=='指控平台'">
      <div class="ypkz">
        <div class="title">
          诱骗控制
        </div>
        <div style="padding:0 10px;">
          <el-form-item label="第三方指控平台">
                 <el-select v-model="zkptStationId" placeholder="请选择" clearable filterable @change="deviceChange">
                      <el-option v-for="(item,index) in zkptDeviceData" :key="index" :label="item.name+'('+ item.pjName +')'" :value="item.stationId"></el-option>
                    </el-select>
              </el-form-item>
        </div>
        <div class="content" style="position:relative">
            <div style="position: absolute;
              color: #0ba4bd;
              top: 0;
              left: 50%;
              transform: translateX(-50%);">N</div>
            <!-- 雷达界面 -->
            <div id="radar" @click="handleRadarClick">
              
              <!-- 雷达刻度圈（4个同心圆） -->
              <div class="circle" style="width: 40px; height: 40px;"></div>
              <div class="circle" style="width: 80px; height: 80px;"></div>
              <div class="circle" style="width: 120px; height: 120px;"></div>
              <div class="circle" style="width: 160px; height: 160px;"></div>
              <div class="circle" style="width: 200px; height: 200px;"></div>
              <!-- 坐标轴（垂直+水平） -->
              <div class="axis vertical"></div>
              <div class="axis horizontal"></div>
              <!-- 方位指针 - 绑定旋转角度 -->
              <div 
                class="pointer" 
                :style="{ transform: `translate(-50%, -100%) rotate(${azimuth}deg)` }"
              ></div>
            </div>

            <!-- 数据与交互区域 -->
            <div id="radarData">
              <div>
                方位(°): <el-input-number style="width:100px" :min="0" :max="360" controls-position="right" v-model="azimuth"  @change="gjysChange"></el-input-number>
                速度: <el-input-number style="width:100px" :min="0" controls-position="right" v-model="speed"  @change="gjysChange"></el-input-number>
              </div>
              <div>
                目标距离: <el-input-number style="width:100px" :min="0" controls-position="right" v-model="jl"  @change="gjysChange"></el-input-number>
              </div>
            </div>

            <el-radio-group v-model="type" @change="typeChange">
              <el-radio v-for="(item, index) in typeOption" :key="index" :label="item.value">{{ item.label }}</el-radio>
            </el-radio-group>

            <!-- <div class="btns" style="text-align:center;margin-top:10px;">
              <el-button type="info">诱骗无人值守</el-button>
              <el-button type="info" disabled>诱骗攻击</el-button>
            </div> -->
        </div>

      </div>
    </div>
  </div>
  <wrjPage v-if="wrjDialog" @success="wrjSuccess" ref="wrjRef"></wrjPage>
</template>

<script setup>
// 模拟推演
import { useRouter } from "vue-router";
import { ref, onMounted,onUnmounted,watch,nextTick } from "vue";
import { ElNotification, ElMessageBox, ElMessage } from "element-plus";
import wrjPage from "./wrjPage.vue";

import startmarker from "/public/static/startmarker.png"
import endmarker from "/public/static/endmarker.png"
import passmarker from "/public/static/passmarker.png"

// 定义路由
const router = useRouter();
const ypType = ref("福建总队")
const type = ref([])
const typeOption = ref([
  {
    label:'禁飞',
    value:'2'
  },
  {
    label:'驱离',
    value:'4'
  },
  {
    label:'拉近',
    value:'5'
  },
  {
    label:'导航压制',
    value:'3'
  },
  {
    label:'定点迫降',
    value:'1'
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
  controlTxDto:{
    direction:0
  },
  // ypEnable:false,
  // jfEnable:false,
  // qlEnable:false,
})
const pageOption = ref({
  pageNo:1,
  pageSize:10
})
const total = ref(0)
const ypmsOption = ref([
  {label:'盘旋',value:'0'},
  {label:'坠机',value:'1'},
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

const btns = ref(["黑白名单","训练录屏"])
let mddLayer = null;
let wrjMarkerLayer = null;
let lineLayer = null;
// 初始化
onMounted(()=>{
  getDevice()
  getzkptDevice()
  window.eventBus.on("sb",(data)=>{
    console.log(data);
  })

  nextTick(()=>{
    mddLayer = L.layerGroup()
    mddLayer.addTo(window.Map2D.map)
    wrjMarkerLayer = L.layerGroup([]);
    wrjMarkerLayer.addTo(window.Map2D.map);
    lineLayer = L.layerGroup([]);
    lineLayer.addTo(window.Map2D.map);
    if(window.TOOL.data.get('ypData')){
      let dataArr = []
      dataArr.push(window.TOOL.data.get('ypData'))
      wrjSuccess(dataArr)
    }
  })

  
})
onUnmounted(()=>{
  clearLayer1()
  clearLayer2()
  clearLayer3()
  window.eventBus.off('stationId')
  window.eventBus.off('sb')
  window.TOOL.data.remove('ypData')
})

const clearLayer2 = () => {
  if (
      wrjMarkerLayer != undefined &&
      wrjMarkerLayer != null &&
      wrjMarkerLayer != ""
  ) {
    // 清空图层
    wrjMarkerLayer.clearLayers();
  }
 
};
const clearLayer1 = () => {
  
  if (
      mddLayer != undefined &&
      mddLayer != null &&
      mddLayer != ""
  ) {
    // 清空图层
    mddLayer.clearLayers();
  }
};

const clearLayer3 = () => {
  
  if (
      lineLayer != undefined &&
      lineLayer != null &&
      lineLayer != ""
  ) {
    // 清空图层
    lineLayer.clearLayers();
  }
};

const isFlag = ref(false);
  
const isEnableChange = () => {
  if(!isFlag.value){
    isFlag.value = true;
    return
  }
  startYp()
}
const wrjDialog = ref(false);
const wrjRef = ref(null);
const wrjMange = () => {
  wrjDialog.value = true;
  nextTick(() => {
    wrjRef.value.open();
  });
};
const wrjIcon =ref(null)
let dronePos = null;
const wrjSuccess = (dataArr) =>{
  console.log(dataArr)
  clearLayer2()
  if(dataArr && dataArr.length){
    
    if(dataArr[0].authStatus==1){
       wrjIcon.value = window.L.icon({
        iconUrl: "/static/fly1.png",
        iconSize: [40, 40],
      });
    }else if(dataArr[0].authStatus==2){
       wrjIcon.value = window.L.icon({
        iconUrl: "/static/fly2.png",
        iconSize: [40, 40],
      });
    }else{
       wrjIcon.value = window.L.icon({
        iconUrl: "/static/fly3.png",
        iconSize: [40, 40],
      });
    }
    if(dataArr[0].jmlx=="频谱测向"){
      window.API.sbgl.list({
              stationId:dataArr[0].stationId
            }).then(res=>{
              if(res.success){
                let data =res.result.records;
                if(data && data.length){
                  deviceData = data[0];
                  // 2. 计算扇形多边形的新坐标
                  var sectorPoints = createSectorPoints({lat:Number(deviceData.wd),lng:Number(deviceData.jd)}, deviceData.zcbj *1000, dataArr[0].uavDetectMsg.angle, 30);
                  // console.log('sectorPoints',sectorPoints);
                  L.polygon(sectorPoints, {
                      color: '#ff3333',
                      weight: 1,
                      fillColor: '#ff3333',
                      fillOpacity: 0.15
                  }).addTo(sectorLayers);
      
                  // 3. 计算无人机位置 (在扇形弧边的正中心)
                  // 也就是距离中心 radius 米，角度为 currentDirection 的点
                  dronePos = getDestinationPoint({lat:Number(deviceData.wd),lng:Number(deviceData.jd)}, dataArr[0].uavDetectMsg.angle, deviceData.zcbj *1000);
                  txForm.value.wrjwz = dronePos[0] +','+ dronePos[1]
                  txForm.value.uavLng = dronePos[1]
                  txForm.value.uavLat = dronePos[0]
                  window.Map2D.map.flyTo([Number(dronePos[1]),Number(dronePos[0])],10)
                  window.L.marker(
                      dronePos,
                      {
                        icon: wrjIcon.value,
                      }
                    ).addTo(wrjMarkerLayer);

                  const  markerIcon = L.divIcon({
                        html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;background: #0083cb;opacity: 0.8;'>${dataArr[0].brand}-${dataArr[0].model}(${dataArr[0].serial})</div>`,//marker标注
                        className: 'my-div-icon',
                        iconAnchor: [80, -20]//文字标注相对位置
                      });
                  window.L.marker(
                    dronePos,
                    {
                      icon: markerIcon,
                    }
                  ).addTo(wrjMarkerLayer);
                }
              }
            })
    }else if(dataArr[0].uavDetectMsg && dataArr[0].uavDetectMsg.dronLat && dataArr[0].uavDetectMsg.dronLng){
      txForm.value.wrjwz = dataArr[0].uavDetectMsg.dronLng +','+dataArr[0].uavDetectMsg.dronLat
      txForm.value.uavLng = dataArr[0].uavDetectMsg.dronLng 
      txForm.value.uavLat = dataArr[0].uavDetectMsg.dronLat
      dronePos = [Number(dataArr[0].uavDetectMsg.dronLat), Number(dataArr[0].uavDetectMsg.dronLng)];
      window.Map2D.map.flyTo([dataArr[0].uavDetectMsg.dronLat,dataArr[0].uavDetectMsg.dronLng],10)
          const marker = window.L.marker(
            dronePos,
            {
              icon: wrjIcon.value,
            }
          ).addTo(wrjMarkerLayer);

          const  markerIcon = L.divIcon({
                  html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;background: #0083cb;opacity: 0.8;'>${dataArr[0].brand}-${dataArr[0].model}(${dataArr[0].serial})</div>`,//marker标注
                  className: 'my-div-icon',
                  iconAnchor: [80, -20]//文字标注相对位置
                });
            window.L.marker(
              dronePos,
              {
                icon: markerIcon,
              }
            ).addTo(wrjMarkerLayer);
    }


    startYp()
    
  }
  
}

const getDestinationPoint = (latlng, bearing, distance) => {
        var R = 6378137; // 地球半径 (米)
        var brng = toRad(bearing);
        var d = distance;
        var lat1 = toRad(latlng.lat);
        var lon1 = toRad(latlng.lng);

        var lat2 = Math.asin(Math.sin(lat1) * Math.cos(d / R) +
            Math.cos(lat1) * Math.sin(d / R) * Math.cos(brng));

        var lon2 = lon1 + Math.atan2(Math.sin(brng) * Math.sin(d / R) * Math.cos(lat1),
            Math.cos(d / R) - Math.sin(lat1) * Math.sin(lat2));

        return [toDeg(lat2), toDeg(lon2)];
    };
    // 将角度转换为弧度
    const toRad = (degree) => {
        return degree * Math.PI / 180;
    };

    // 将弧度转换为角度
    const toDeg = (radian) => {
        return radian * 180 / Math.PI;
    };

const closeDialog = () => {
       
      if(window.Map2D.map){
        var myTooltip = null;
        window.Map2D.map.on('mousemove',(e)=>{
          if(myTooltip) myTooltip.close();
          // 创建并添加弹出框
          myTooltip = L.tooltip()
              .setLatLng(e.latlng) // 设置弹出框的位置
              .setContent("点击选择坐标点") // 设置弹出框的内容
              .openOn(window.Map2D.map); // 在地图上打开弹出框
        })
        window.Map2D.map.on('click',(e)=>{
          clearLayer1()
            txForm.value.ypwz = e.latlng.lng+','+e.latlng.lat;
            txForm.value.tagLat = e.latlng.lat;
            txForm.value.tagLng = e.latlng.lng;

            const marketMarker = window.L.marker([e.latlng.lat, e.latlng.lng], { icon: L.icon({ iconSize: [32, 48], iconUrl: endmarker }) })
            marketMarker.addTo(mddLayer)

            // 清除关闭地图事件
            
            window.Map2D.map.off('mousemove')
            window.Map2D.map.off('click')
            if(myTooltip) myTooltip.close();

            startYp()

          
        })
      }
    };
// 选中项
// watch(
//   () => azimuth.value,
//   (newVal) => {
//     nextTick(() => {
//       inputAzimuth.value = newVal;
//     });
//   },
//   { immediate: false }
// );

const  startYp= () => {

  if( txForm.value.uavLat && txForm.value.uavLng && txForm.value.tagLat && txForm.value.tagLng){
    clearLayer3()
    L.polyline([[Number(txForm.value.uavLat), Number(txForm.value.uavLng)], [Number(txForm.value.tagLat), Number(txForm.value.tagLng)]], {color: "red"}).addTo(lineLayer);
  }
  window.API.xhgr.ControlDevice({
              stationId:stationId.value,
              type:"指定诱骗",
              enable:txForm.value.ypEnable,
              uavLng:txForm.value.uavLng,
              uavLat:txForm.value.uavLat,
              tagLat:txForm.value.tagLat,
              tagLng:txForm.value.tagLng
            }).then(res=>{
                  if(res.success){
                    ElMessage.success("操作成功")
                  }
                })
}

const ypChange = (e) => {
  console.log(e);
  clearLayer1()
  clearLayer2()
  clearLayer3()
  txForm.value = {
    smfs:"定向",
    jd:"0-60°",
    controlTxDto:{
      direction:0
    },
  }
}

const typeChange = (e) => {
  console.log(e);
  if(e==4){
    gnssSpoofMode.value = 0;
    gnssInduceType.value = 0;
  }else if(e==5){
    gnssSpoofMode.value = 0;
    gnssInduceType.value = 1;
  }else{
    gnssSpoofMode.value =e;
    gnssInduceType.value = 1;
  }
  ControlDeviceTcpServerSocketServerEvent()
}


const azimuth = ref(0);
const speed = ref(null);
const jl = ref(null);
const gnssSpoofLinkageEnable = ref(false)
const gnssSpoofMode = ref(null)
const gnssInduceType = ref(null)

const ControlDeviceTcpServerSocketServerEvent = () => {
  window.API.device.ControlDeviceTcpServerSocketServer({
    azimuth:azimuth.value,
    speed:speed.value,
    jl:jl.value,
    gnssSpoofMode:Number(gnssSpoofMode.value),
    gnssInduceType:gnssInduceType.value,
    gnssSpoofLinkageEnable:gnssSpoofLinkageEnable.value,
    attackFrequencyBand:1,
    attackMode:0,
    targetDetectionEnable:false,
    deviceAttackEnable:false,
    unattendedModeEnable:false,
    stationId:zkptStationId.value
  }).then(res=>{
    if(res.code==200){
      ElMessage.success("操作成功")
    }else{
      ElMessage.error(res.message)
    }
  })
}
const switchChange = (e) => {
  if(e){
    ControlDeviceTcpServerSocketServerEvent()
  }
}
// 雷达容器固定尺寸（与样式保持一致）
const radarSize = 200;
const radarCenter = radarSize / 2;


// 点击雷达计算方位角
const handleRadarClick = (e) => {
  const radar = document.getElementById('radar');
  const rect = radar.getBoundingClientRect();
  // 计算点击位置相对雷达容器的坐标
  const clickXInRadar = e.clientX - rect.left;
  const clickYInRadar = e.clientY - rect.top;
  
  // 计算相对中心的偏移量
  const dx = clickXInRadar - radarCenter;
  const dy = clickYInRadar - radarCenter;

  // 计算方位角（垂直向上为0°，顺时针递增）
  let angle = Math.atan2(dx, -dy) * (180 / Math.PI);
  // 转换为0-360°范围
  angle = angle < 0 ? angle + 360 : angle;
  // 保留1位小数
  angle = Math.round(angle * 10) / 10;

  // 更新响应式数据
  azimuth.value = angle;
};



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
const  controlDevice= () =>{
  window.API.xhgr.ControlDevice({
    stationId:stationId.value,
    "type":"天线控制",
    "controlTxDto":{
      "dirLoop":txForm.value.controlTxDto.dirLoop,
      "direction":txForm.value.controlTxDto.direction
    },
  }).then(res=>{
        if(res.success){
          ElMessage.success("操作成功")
        }
      })

}
const ampChange = () => {
  window.API.xhgr.ControlDevice({
    stationId:stationId.value,
    type:"衰减",
    amp:txForm.value.amp
  }).then(res=>{
        if(res.success){
          ElMessage.success("操作成功")
        }
      })

}
const jfFlag = ref(false);
const jfEnableChange = () =>{
  if(!jfFlag.value){
    jfFlag.value = true;
    return
  }
  
  window.API.xhgr.ControlDevice({
    stationId:stationId.value,
    type:"禁飞开关",
    enable:txForm.value.jfEnable
  }).then(res=>{
        if(res.success){
          ElMessage.success("操作成功")
        }
      })
}
const ypFlag = ref(false)
const trapTypeChange= () =>{
  if(!ypFlag.value){
    ypFlag.value = true;
    return
  }
  
  window.API.xhgr.ControlDevice({
    stationId:stationId.value,
    type:"诱骗模式",
    enable:txForm.value.ypEnable,
    trapType:txForm.value.trapType
  }).then(res=>{
        if(res.success){
          ElMessage.success("操作成功")
        }
      })
}
const qlFlag = ref(false)
const qlEnableChange= () =>{
  if(!qlFlag.value){
    qlFlag.value = true;
    return
  }
  
  window.API.xhgr.ControlDevice({
    stationId:stationId.value,
    type:"驱离功能控制",
    enable:txForm.value.qlEnable
  }).then(res=>{
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
  .yp1{
    width: 100%;
    height: calc(100% - 50px);
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: relative;
    pointer-events: none;
    margin-top:10px;
    :deep(.el-button){
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
        width:  50%;
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
    display: flex;
    flex-direction: column;
    justify-content: space-between;
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

  .dhyp{
    height:60%;
    background: url("@/assets/allImage/dialogBg.png") no-repeat;
    background-size: 100% 100%;
    box-sizing: border-box;
     .content{
        color: #fff;
        font-size: 14px;
        padding:10px;
        box-sizing: border-box;
      height: calc(100% - 37px);
        &>div{
          display: flex;
          justify-content: space-between;
          align-items: center;
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
  .yp2{
    width: 100%;
    height: calc(100% - 50px);
    position: relative;
    pointer-events: none;
    margin-top:10px;
    .ypkz{
      width: 400px;
      height: 79%;
      pointer-events: auto;
      background: url("@/assets/allImage/dialogBg.png") no-repeat;
      background-size: 100% 100%;
      box-sizing: border-box;
       position: absolute;
      left: 30px;
      top: 0;
      :deep(.el-radio__input.is-checked+.el-radio__label){
        color:#0083cb;
      }
      .content{
        padding:10px;
        height:calc(100% - 76px);
        /* 雷达容器样式 - 固定尺寸，确保中心计算准确 */
        #radar {
          position: relative;
          width: 200px;
          height: 200px;
          margin: 20px auto;
          // background: #0a1a2a; /* 雷达背景色，可替换为你的图片 */
          border-radius: 50%;
          overflow: hidden;
          /* 确保容器本身没有偏移 */
          box-sizing: border-box;
        }
        /* 雷达刻度圈 - 基于中心精准定位 */
        #radar .circle {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          border: 2px solid rgba(0, 255, 255, 0.5);
          border-radius: 50%;
          box-sizing: border-box;
        }
        /* 雷达坐标轴 - 严格居中 */
        #radar .axis {
          position: absolute;
          background: rgba(0, 255, 255, 0.5);
          box-sizing: border-box;
        }
        /* 垂直轴：完全居中，从顶部到底部 */
        #radar .axis.vertical {
          top: 0;
          left: 50%;
          width: 2px;
          height: 100%;
          transform: translateX(-50%); /* 修正1px偏移 */
        }
        /* 水平轴：完全居中，从左到右 */
        #radar .axis.horizontal {
          top: 50%;
          left: 0;
          width: 100%;
          height: 2px;
          transform: translateY(-50%); /* 修正1px偏移 */
        }
        /* 雷达指针 - 旋转原点在容器正中心，长度为半径 */
        #radar .pointer {
          position: absolute;
          top: 50%;
          left: 50%;
          width: 2px;
          height: 250px; /* 等于雷达半径（500/2） */
          background: red;
          transform-origin: bottom center; /* 旋转原点在指针底部（即容器中心） */
          z-index: 10;
        }
        /* 数据显示区域 */
        #radarData {
          margin-top: 20px;
          font-size: 18px;
          color:#fff;
          &>div:nth-of-type(1){
            display:flex;
            justify-content: space-between;
            align-items: center;
          }
          &>div:nth-of-type(2){
            margin-top:10px;
            padding-bottom:10px;
            border-bottom:1px solid #1582bf;
          }
        }
        #azimuthInput {
          margin: 0 10px;
          padding: 5px;
          width: 80px;
        }
        button {
          width:98%;
          padding: 5px 15px;
          height:60px;
        }
      }
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
  background: url(@/assets/allImage/inputBg.png) no-repeat;
  background-size: 100% 100%;
  border:none;
}
:deep(.el-input__inner){
  color: #fff  !important;
  border:none;
}

:deep(.el-switch){
  margin-bottom:8px;
}
:deep(.el-form-item){
        margin-bottom: 8px;
        width: 100%;
        margin-right: 5px;
      }
}
</style>