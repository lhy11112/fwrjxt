<template>
  <div class="topCenter">
    <!-- <div class="left">
      <div class="menus" v-for="(item,index) in menu" :key="index" @click="menuClick(index)">
        <div>
          <img :src="item.icon" style="width:34px;height:24px"/>
        </div>
        <div>{{item.name}}</div>
      </div>
    </div>
    <div class="right">
      
    </div> -->
    <div v-show="mapChange"  class="buttons">
      <el-button type="primary" @click="flyKz(true)">开始</el-button>
      <el-button type="primary" @click="flyKz(false)">暂停</el-button>
      <el-button type="primary" @click="flyMy()">漫游</el-button>
    </div>
     <!-- v-show="wxdsbVisible" -->
    <div class="wxdsbBox" v-show="!mapChange">
      <div class="title">
        <div>无线电设备</div>
        <!-- <el-icon @click="wxdsbVisible = false;" style="cursor:pointer;"><Close /></el-icon> -->
      </div>
      <div class="search">
        <el-input v-model="queryInfo.name" placeholder="请输入无线电设备名称"></el-input>
        <el-button @click="inquires">查询</el-button>
      </div>
      <div class="content">
        <div class="wxdsb" v-for="(item,index) in wxdsbData" :key="index">
          <div style="cursor:pointer;" @click="flyDevice(item)">
            <div class="content-top">
              <img :src="item.status === 'CONNECTED' &&item.deviceType === 'DETECT'?'/static/zcsb.png':item.status === 'CONNECTED' &&item.deviceType === 'DISTURB'?'/static/grsb.png':item.status === 'CONNECTED' &&item.deviceType === 'TRAP'?'/static/ypsb.png':item.status === 'CONNECTED' &&item.deviceType === 'System'?'/static/system.png':item.status === 'DISCONNECTED' &&item.deviceType === 'DETECT'?'/static/zcsb2.png':item.status === 'DISCONNECTED' &&item.deviceType === 'DISTURB'?'/static/grsb2.png':item.status === 'DISCONNECTED' &&item.deviceType === 'TRAP'?'/static/ypsb2.png':item.status === 'DISCONNECTED' &&item.deviceType === 'System'?'/static/system2.png':item.status === 'WARN' &&item.deviceType === 'DETECT'?'/static/zcsb3.png':item.status === 'WARN' &&item.deviceType === 'DISTURB'?'/static/grsb3.png':item.status === 'WARN' &&item.deviceType === 'TRAP'?'/static/ypsb3.png':item.status === 'WARN' &&item.deviceType === 'System'?'/static/system3.png':''" style="width:80px;height:50px;"/>
              <div :class="item.status=='CONNECTED'?'color1':item.status=='DISCONNECTED'?'color2':'color3'">{{item.status=='CONNECTED'?'已连接':item.status=='DISCONNECTED'?'未连接':'告警中'}}</div>
            </div>
            <div class="content-bottom">
              <div :class="item.status=='CONNECTED'?'dotStatus bg1':'dotStatus bg2'"></div>
              <div :title="item.name+'-'+item.deviceId">{{item.name}}-{{item.deviceId}}</div>
            </div>
          </div>
        </div>
      </div>
      <div>
              <el-pagination
                class="pagination"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="pageOption.pageNo"
                :page-size="pageOption.pageSize"
                layout="total,prev, pager, next"
                :total="total"
              >
              </el-pagination>
            </div>


    </div>

     <!-- v-show="wxdtcVisible" -->
    <div class="wsdtc" id="zcwrjxx">
      <div :class="wrjToggleVisible?'wrjToggleBtn wrjToggleBg1':'wrjToggleBtn wrjToggleBg2'" @click="wrjToggleEvent()">
        <img style="width:24px;":src="wrjToggleVisible?'static/toggle/toggle-icon4.png':'static/toggle/toggle-icon5.png'"/>
      </div>
      <div class="title">
        
        <div>
          <img src="/static/toggle/toggle-icon3.png" style="width:20px;vertical-align: middle;"/>
          侦测无人机信息
        </div>
        <!-- <el-icon @click="wxdtcVisible = false;" style="cursor:pointer;"><Close /></el-icon> -->
      </div>
      <div class="search">
        <el-date-picker
          v-model="rq"
          type="date"
          style="width: 100%"
          value-format="YYYY-MM-DD"
          :placeholder="'请选择时间'"
          @change="rqChange"
        />
      </div>
      <div class="content">
        <div :class="wrjCurrentIndex==index?'wrjActive wxdsb':'wxdsb'" id="contextMenuDiv" v-for="(item,index) in wrjData" :key="index"  @contextmenu="showMenu">
          <div>
            <div class="content-top"  @click="wrjFly(item,index)">
              <img :src="item.authStatus =='1'?'/static/fly1.png':item.authStatus =='2'?'/static/fly2.png':'/static/fly3.png'" style="width:80px;height:50px;"/>
              <div :class="item.status=='1'?'color1':item.status=='2'?'color2':'color3'">{{item.status=='1'?'正常':item.status=='2'?'告警':'失联'}}</div>
            </div>
            <div class="content-bottom"  @click="wrjDetail(item)">
              <div :class="item.status=='1'?'dotStatus bg1':item.status=='2'?'dotStatus bg2':'dotStatus bg3'"></div>
              <div :title="item.brand+'-'+item.model+'-'+item.serial">{{item.brand}}-{{item.model}}-{{item.serial}}</div>
            </div>
          </div>

          <div v-if="showMenuFlags['flag'+index]" id="contextMenu" :style="{left:menuPosition.x+'px',top:menuPosition.y+'px'}">
            <el-icon style="position:absolute;right:0;top:0;cursor:pointer;" @click="rightMenuClose()"><Close /></el-icon>
            <div @click="flyKz(true)">开始</div>
            <div @click="flyKz(false)">暂停</div>
          </div>
        </div>
      </div>
    </div>

    <div class="wsdtc" id="zcwrjXxxx">
      <div class="title">
        <div>
          <img src="/static/toggle/toggle-icon3.png" style="width:20px;vertical-align: middle;"/>
          侦测无人机详细信息
        </div>
        <!-- <el-icon @click="wxdtcVisible = false;" style="cursor:pointer;"><Close /></el-icon> -->
      </div>
      <div class="content" style="height:calc(100% - 25px);">
        <el-row v-if="detailInfoObj">
          <el-col :span="24" v-for="(item, index) in column" :key="index" v-show="item.label != '序号'">
            <!-- v-show="item.label != '图片'" -->
            <div  class="detailInfo_label" style="width:120px;">{{item.label}}：</div>
            <div class="detailInfo_text" style="text-indent: 0rem;">
              <span>{{detailInfoObj[item.prop]}}</span>
            </div>
          </el-col>
        </el-row>
        <el-row v-else>
          <img
            style="
              width: 170px;
              height: 105px;
              margin-left: 50%;
              margin-top: 50%;
              transform: translate(-50%,0);
            "
            src="@/assets/noData.png"
            alt=""
          />
        </el-row>
      </div>
    </div>
  </div>

  <wrjDetailDialog ref="wrjDetailRef"></wrjDetailDialog>
  <deviceDetailDialog ref="deviceDetailRef"></deviceDetailDialog>
</template>

<script setup>
import { ElMessage } from "element-plus";
import { useRouter,useRoute } from "vue-router";
import microApp from "@/components/microApp/index.vue";
import { ref, onMounted,nextTick,onUnmounted } from "vue";
import wrjDetailDialog from "./wrjDetailDialog.vue"
import deviceDetailDialog from "./deviceDetail.vue"
import icon1 from "@/assets/leftTitle/leftImg.png"
import wxdzcsb from "@/assets/allImage/wxdzcsb.png"
import wrj from "@/assets/allImage/wrj.png"
import { mapModeEnum, getCurrentMapMode } from "@/utils/Map/mapMode";
// 定义路由
const router = useRouter();
const route = useRoute();
const showMenuFlags=ref({})
const menu = ref([
  {
    name:"空域动态",
    icon:icon1
  },
  {
    name:"设备动态",
    icon:icon1
  },
  {
    name:"告警信息",
    icon:icon1
  }
])
const detailInfoObj = ref(null)
const column = [
  { prop: "brand", label: "无人机品牌", width: 120 },
  { prop: "model", label: "无人机型号", width: 120 },
  { prop: "serial", label: "无人机序列号", width: 180 },
  { prop: "dronLng", label: "无人机经度" },
  { prop: "dronLat", label: "无人机纬度" },
  { prop: "homeLng", label: "起飞点经度" },
  { prop: "homeLat", label: "起飞点纬度" },
  { prop: "pilotLng", label: "遥控器经度" },
  { prop: "pilotLat", label: "遥控器纬度" },
  { prop: "altitude", label: "海拔高度(米)" },
  { prop: "height", label: "高度" },
  { prop: "eastV", label: "东速度" },
  { prop: "northV", label: "北速度" },
  { prop: "upV", label: "上速度" },
  { prop: "freq", label: "频率(U64)" },
  { prop: "rssi", label: "信号强度" },
  { prop: "distance", label: "距离(Km)" },
  { prop: "uuid", label: "飞手执照代码" },
  { prop: "angle", label: "飞机角度", width: 100 },
  { prop: "stationId", label: "站ID" },
  { prop: "dataTime", label: "时间戳(U64转换)", width: 150 },
  { prop: "createTime", label: "入库时间", width: 150 },
]
const wxdsbVisible = ref(false)
const wxdtcVisible = ref(true)
const queryInfo = ref({
  isValid:1,
  name:""
})
const wxdsbData = ref([])
const pageOption = ref({
  pageNo:1,
  pageSize:10
})
const total = ref(0)
const timer = ref(null)
const mapChange = ref(false)
let lineLayer = null;
let markerLayer = null;
// 初始化
onMounted(()=>{
  nextTick(()=>{
    getData()
    rq.value =window.TOOL.dateFormat(new Date(),"yyyy-MM-dd");
    getWxdData()
    lineLayer = L.layerGroup([]);
    lineLayer.addTo(window.Map2D.map);
    markerLayer = L.layerGroup([]);
    markerLayer.addTo(window.Map2D.map);
  })
  timer.value =setInterval(()=>{
    getWxdData()
    getData()
  },1000 *10)


  window.WEBSCOKET.ws.addEventListener('message', (event) => {
      try {
        let data = JSON.parse(event.data);
        let msg_txt = JSON.parse(data.msg_txt);
        console.log(msg_txt);
        window.eventBus.emit("gj",msg_txt)
      } catch (error) {
        
      }
    });
  if(curMapMode === mapModeEnum["3D"]){
    mapChange.value = true
    eventBus.emit("bottomFlag",mapChange.value)
  }else if(curMapMode === mapModeEnum["2D"]){
    mapChange.value = false
    eventBus.emit("bottomFlag",mapChange.value)
  }

  window.eventBus.on("mapChange",function(e){
    mapChange.value = !mapChange.value;
    eventBus.emit("bottomFlag",mapChange.value)
    speed.value = 0;
    lng.value = 0;
    lat.value = 0;
    window.eventBus.off("wrjData")
    clearInterval(timelineInterval);
    clearMarkerLayer()
    clearLineLayer()
    timelineInterval = null;
    const element = document.getElementById('timeline');
      console.log(element);
      if(element){
        element.remove()
      }
    nextTick(()=>{
      
      setTimeout(()=>{
        addToMap()
      },100)
    })
  })


  

})
onUnmounted(()=>{
  clearInterval(timer.value)
    timer.value=null;

    window.eventBus.off("wrjData")
    clearInterval(timelineInterval);
    clearMarkerLayer()
    clearLineLayer()
    timelineInterval = null;
    const element = document.getElementById('timeline');
      console.log(element);
      if(element){
        element.remove()
      }
  window.eventBus.off("mapChange")
  window.eventBus.off("wrjDetailData")
  //清除三维推演
  window.Map3D.wrjFly.clearAllLayers()
})
const menuPosition = ref({})
const showMenu = (event) =>{
  console.log(event);
  // event.preventDefault();
      showMenuFlags.value['flag'+wrjCurrentIndex.value] = true;
      const rect = event.target.getBoundingClientRect();
      menuPosition.value.x = event.clientX - rect.left;
      menuPosition.value.y = event.clientY - rect.top;
}
const wrjToggleVisible = ref(false)
const wrjToggleEvent = () => {
  wrjToggleVisible.value = !wrjToggleVisible.value
  if(wrjToggleVisible.value){
    $('#zcwrjxx').css("transform","translateX(350px)");
    $('#zcwrjXxxx').css("transform","translateX(0)");
  }else{
    $('#zcwrjxx').css("transform","translateX(0)");
    $('#zcwrjXxxx').css("transform","translateX(350px)");
  }
  
}
const menuClick = (index) =>{
  if(index==1){
    wxdsbVisible.value = !wxdsbVisible.value;
    wxdtcVisible.value = !wxdtcVisible.value;
    if(wxdsbVisible.value){
      getData()
    }
    if(wxdtcVisible.value){
      rq.value =window.TOOL.dateFormat(new Date(),"yyyy-MM-dd");
      getWxdData()
    }
  }
}

const wrjCurrentIndex = ref(-1)
const rqChange = () => {
  wrjCurrentIndex.value =-1;
  getWxdData()
}

const rq=ref("")
const wrjData = ref([])
const getWxdData = () => {
  window.API.wxdzc.getUavDetectMsgByStationId({
    rq:rq.value
  }).then(res=>{
    // console.log(res);
    if(res.success){
      wrjData.value = res.result;
      // if(wrjData.value && wrjData.value.length){
      //   window.Map2D.map.flyTo([Number(wrjData.value[0].uavDetectMsg.dronLat),Number(wrjData.value[0].uavDetectMsg.dronLng)],14)
      // }
      
      wrjAddPoint()
      fsAddPoint()
    }
  })
}

//开始 暂停
const flyKz = (data) => {
  if(mapChange.value){
      window.Map3D.wrjFly.togglePlayPause(data)
  }else{
      if (data) {
        clearInterval(timelineInterval);
        timelineInterval = null;
        timelineInterval = setInterval(function() {
          currentTime = (currentTime + 1) % fxData.value.length;
          updateTimeline(currentTime);
          clearMarkerLayer()
          console.log(fxData.value[currentTime]);
          planeMarkers[currentTime].setLatLng([fxData.value[currentTime].dronLat, fxData.value[currentTime].dronLng]).addTo(markerLayer);
          const  markerIcon = L.divIcon({
                html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>${rowData.value.brand}-${rowData.value.model}(${rowData.value.serialNumber})</div>`,//marker标注
                className: 'my-div-icon',
                iconAnchor: [80, 20]//文字标注相对位置
              });
          window.L.marker(
            window.L.latLng(Number(fxData.value[currentTime].dronLat), Number(fxData.value[currentTime].dronLng)),
            {
              icon: markerIcon,
            }
          ).addTo(markerLayer);
          // // 使用turf.js计算距离
                // const fxTime = (new Date(fxData.value[currentTime].dataTime).getTime() - new Date(fxData.value[0].dataTime).getTime()) * 1000
                // const distance = turf.distance([Number(fxData.value[0].dronLat),Number(fxData.value[0].dronLng)], [Number(fxData.value[currentTime].dronLat), Number(fxData.value[currentTime].dronLng)]);
                // speed.value =(distance * 1000 / fxTime).toFixed(8)
                // lat.value = fxData.value[currentTime].dronLat
                // lng.value = fxData.value[currentTime].dronLng
        }, 1000);
        // timeLineRef.value.autoPlayTimer(1)
      } else {
        clearInterval(timelineInterval);
        timelineInterval = null;
        // timeLineRef.value.autoPlayTimer(2)
      }
      showMenuFlags.value['flag'+wrjCurrentIndex.value] = false;
  }
   
  
}
const rightMenuClose = () =>{
  showMenuFlags.value['flag'+wrjCurrentIndex.value] = false;
}
//视角切换
const myFlag = ref(true)
const flyMy = () =>{
  myFlag.value = !myFlag.value
  if(myFlag.value){
    window.Map3D.wrjFly.setSj(3)
  }else{
    window.Map3D.wrjFly.setSj(1)
  }
  
}

const fxData = ref([])
const path = ref([])
const droneEntity = ref(null)
const curMapMode = getCurrentMapMode();
let timelineInterval = null;
// 初始化当前时间点
let currentTime = 0;
let planeMarkers = [];
// 创建时间点
let timelinePoints = [];
//无人机飞行详细数据
const wrjFly = (data,index) => {
  wrjCurrentIndex.value = index;
  const element = document.getElementById('timeline');
  console.log(element);
  if(element){
    element.remove()
  }
  clearMarkerLayer()
  clearLineLayer()
  clearInterval(timelineInterval);
  timelineInterval = null;

  window.API.wxdzc.getUavDetectMsgByModelSerialRq({
      model:data.model,
      rq:rq.value,
      serial:data.serial
    }).then(res=>{
      if(res.success){
        fxData.value = res.result.uavDetectMsgList;
        if(res.result.wjbdWrjJbxx){
          rowData.value = res.result.wjbdWrjJbxx;
        }else{
          rowData.value = {
            brand:data.brand,
            authStatus:data.authStatus,
            model:data.model,
            serialNumber:data.serial
          }
        }
        
        eventBus.emit("timeline",2)
        addToMap(data)
        wrjToggleEvent()
      }
    })

  
}
const rowData = ref({})
const timeline = ref(null);
const showTimeLine  =ref(false)
const speed = ref(0)
const lng = ref('')
const lat = ref('')
const addToMap = (data) => {
  console.log('xxx',mapChange.value);
  if(fxData.value && fxData.value.length){
    if(mapChange.value){
          clearInterval(timelineInterval);
          timelineInterval = null;
          speed.value = 0;
          lng.value = 0;
          lat.value = 0;
          window.Map3D.wrjFly.clearAllLayers()
          const path = Map3D.wrjFly.generateDronePath(fxData.value);
          const droneEntity = Map3D.wrjFly.createDroneModel(fxData.value);
          Map3D.wrjFly.DronePlaybackController(
            path, 
            droneEntity,
            rowData.value
          );
          window.eventBus.on("wrjData",function(e){
            lat.value = e.latitude;
            lng.value = e.longitude;
            speed.value = e.speed;
          })
          window.eventBus.on("wrjDetailData",function(e){
            console.log(e);
            detailInfoObj.value = e;
            detailInfoObj.value.brand = rowData.value.brand;
          })
    }else{
          wxdtcVisible.value = false;
          window.eventBus.off("wrjData")
          window.eventBus.off("wrjDetailData")
          speed.value = 0;
          lng.value = 0;
          lat.value = 0;
          window.Map2D.map.flyTo([Number(data.uavDetectMsg.dronLat),Number(data.uavDetectMsg.dronLng)],14)
          // 创建时间轴
          // 创建ID为timeline的div元素
          timeline.value = document.createElement('div');
          timeline.value.id = 'timeline';
          var largeContent = document.getElementsByClassName('bottomTimeLine')[0];
          console.log(largeContent);
          largeContent.appendChild(timeline.value)
          // document.body.appendChild(timeline.value);
          var timelineElement = document.createElement('div');
          timelineElement.className = 'timeline';
          timeline.value.appendChild(timelineElement);

          // 创建时间线
          var timelineLine = document.createElement('div');
          timelineLine.className = 'timeline-line';
          timelineElement.appendChild(timelineLine);

          // 创建时间点
          timelinePoints = fxData.value.map(function(item, index) {
              var point = document.createElement('div');
              point.className = 'timeline-point';
              point.style.left = (index / (fxData.value.length - 1) * 100) + '%';
              point.dataset.index = index;
              timelineElement.appendChild(point);
              return point;
          });

          // 创建时间刻度
          var timelineElement = document.getElementById('timeline');
          var timelineLine = timelineElement.querySelector('.timeline-line');
          // 创建时间刻度
          // var lastTime = null;

          // 根据时间点数量创建时间刻度
          // fxData.value.forEach(function(item, index) {
          //     // 计算时间差，如果大于10分钟，则创建刻度
          //     if (lastTime === null || (new Date(item.dataTime) - new Date(lastTime)) >= 600000) { // 600000毫秒等于10分钟
          //         var tick = document.createElement('div');
          //         tick.className = 'timeline-tick';
          //         tick.style.left = (index / (fxData.value.length - 1) * 100) + '%';
          //         timelineLine.appendChild(tick);
          //         var timeKd = document.createElement('div');
          //         timeKd.className = 'timeKd';
          //         timeKd.innerHTML = item.dataTime; // 显示时间
          //         tick.appendChild(timeKd);
          //         lastTime = item.dataTime;
          //     }
          // });
          // 首先获取所有时间点
          const times = fxData.value.map(item => new Date(item.dataTime));
          if (times.length === 0) return; // 避免空数据处理

          // 计算最小和最大时间
          const minTime = Math.min(...times);
          const maxTime = Math.max(...times);
          const totalSpan = maxTime - minTime;

          // 确定所需的刻度数，例如5个
          const numTicks = 8;
          const interval = totalSpan / (numTicks - 1);

          // 初始化当前刻度时间为最小时间，并在开始时创建第一个刻度
          let currentTick = minTime;

          // 创建初始刻度
          const initialTick = document.createElement('div');
          initialTick.className = 'timeline-tick';
          initialTick.style.left = '0%'; // 初始时间显示在最左边
          timelineLine.appendChild(initialTick);

          const initialTimeKd = document.createElement('div');
          initialTimeKd.className = 'timeKd';
          initialTimeKd.innerHTML = new Date(minTime).toLocaleString(); // 格式化时间显示
          initialTick.appendChild(initialTimeKd);

          // 遍历数据点，生成刻度
          fxData.value.forEach((item, index) => {
              const currentTimes = new Date(item.dataTime);
              
              // 如果当前时间超过了当前刻度时间加上间隔时间
              while (currentTimes >= currentTick + interval) {
                  // 创建刻度
                  const tick = document.createElement('div');
                  tick.className = 'timeline-tick';
                  tick.style.left = ((index) / (fxData.value.length - 1) * 100) + '%';
                  timelineLine.appendChild(tick);
                  
                  const timeKd = document.createElement('div');
                  timeKd.className = 'timeKd';
                  timeKd.innerHTML = currentTimes.toLocaleString(); // 根据需要格式化时间
                  tick.appendChild(timeKd);
                  
                  // 更新当前刻度时间
                  currentTick += interval;
              }
          });

          // 确保最后一个时间点被显示
          const kdLastTime = new Date(fxData.value[fxData.value.length - 1].dataTime);
          if (kdLastTime > currentTick) {
              const tick = document.createElement('div');
              tick.className = 'timeline-tick';
              tick.style.left = '100%'; // 最后时间显示在最右边
              timelineLine.appendChild(tick);

              const timeKd = document.createElement('div');
              timeKd.className = 'timeKd';
              timeKd.innerHTML = kdLastTime.toLocaleString(); // 格式化时间显示
              tick.appendChild(timeKd);
          }
          
          var currentPoint = timelinePoints[currentTime];
          currentPoint.classList.add('timeline-current');

          

          // 添加时间轴拖拽功能
          timelineElement.addEventListener('mousedown', function(e) {
              var rect = timelineElement.getBoundingClientRect();
              var x = e.clientX - rect.left;
              var time = (x / timelineElement.offsetWidth) * (fxData.value.length - 1);
              currentTime = Math.round(time);
              updateTimeline(currentTime);
          });
          showTimeLine.value = true;

          // 添加飞行轨迹
          var polyline = L.polyline(fxData.value.map(p => [p.dronLat, p.dronLng] )).addTo(lineLayer);
          // 添加飞机图标
          var planeIcon = L.icon({
              iconUrl: '/static/fly1.png',
              iconSize: [25, 25],
              iconAnchor: [12, 41],
              popupAnchor: [-3, -73]
          });
          console.log(rowData.value);
          if(rowData.value){
            if(rowData.value.authStatus==1){
              planeIcon =L.icon({
                iconUrl: '/static/fly1.png',
                iconSize: [25, 25],
                iconAnchor: [12, 41],
                popupAnchor: [-3, -73]
            });
            }else if(rowData.value.authStatus==2){
              planeIcon =L.icon({
                  iconUrl: '/static/fly2.png',
                  iconSize: [25, 25],
                  iconAnchor: [12, 41],
                  popupAnchor: [-3, -73]
              });
            }else{
              planeIcon =L.icon({
                  iconUrl: '/static/fly3.png',
                  iconSize: [25, 25],
                  iconAnchor: [12, 41],
                  popupAnchor: [-3, -73]
              });
            }
          }

          window.Map2D.map.flyTo([Number(fxData.value[0].dronLat),Number(fxData.value[0].dronLng)],11)
        
          fxData.value.forEach(function(point) {
              var marker = L.marker([point.dronLat, point.dronLng], { icon: planeIcon });
              marker.on('contextmenu', function(evt){
                var rigList = [
                    { text: '诱骗', iconname: 'yp', click: `yp(${evt.latlng.lng},${evt.latlng.lat})` },
                    { text: '干扰', iconname: 'gr', click: `gr(${evt.latlng.lng},${evt.latlng.lat})` },
                    { text: '推演', iconname: 'ty', click: `ty(${JSON.stringify(rowData.value)})` },
                ]
                //console.log();
                var mapRigMenuHtm = function (o) {
                    return `
                  <div class='cd-span' style="padding:5px 10px; cursor: pointer;">
                    <a onclick='${o.click}'>
                    <img src='/static/map_img/${o.iconname}.png' style='vertical-align: middle;'>${o.text}</a></a>
                  </div>`
                }

                var rigHtm = ''
                for (let i = 0; i < rigList.length; i++) {
                    rigHtm += mapRigMenuHtm(rigList[i])
                }

                //添加地图弹出框
                L.popup({
                    className: 'mypopup',
                }).setLatLng(evt.latlng).setContent(rigHtm).openOn(window.Map2D.map)
              })
              planeMarkers.push(marker);
          });

          // 时间轴更新逻辑
          timelineInterval = setInterval(function() {
              currentTime = (currentTime + 1) % fxData.value.length;
              updateTimeline(currentTime);
              // polyline.setLatLngs([fxData.value[currentTime].dronLat, fxData.value[currentTime].dronLng]);
              clearMarkerLayer()
              // console.log(fxData.value[currentTime]);
              planeMarkers[currentTime].setLatLng([fxData.value[currentTime].dronLat, fxData.value[currentTime].dronLng]).addTo(markerLayer);
              const  markerIcon = L.divIcon({
                html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>${rowData.value.brand}-${rowData.value.model}(${rowData.value.serialNumber})</div>`,//marker标注
                className: 'my-div-icon',
                iconAnchor: [80, 20]//文字标注相对位置
              });
          window.L.marker(
            window.L.latLng(Number(fxData.value[currentTime].dronLat), Number(fxData.value[currentTime].dronLng)),
            {
              icon: markerIcon,
            }
          ).addTo(markerLayer);
              
              // // 使用turf.js计算距离
              const fxTime = (new Date(fxData.value[currentTime].dataTime).getTime() - new Date(fxData.value[0].dataTime).getTime()) * 1000
               const distance = turf.distance([Number(fxData.value[0].dronLat),Number(fxData.value[0].dronLng)], [Number(fxData.value[currentTime].dronLat), Number(fxData.value[currentTime].dronLng)]);
              if(distance && fxTime){
                speed.value =(distance * 1000 / fxTime).toFixed(8)
              }
              lat.value = fxData.value[currentTime].dronLat
              lng.value = fxData.value[currentTime].dronLng
         }, 1000);
    }
  }
  

}
// 添加时间轴动画
        const updateTimeline = (time) => {
            timelinePoints.forEach(function(point, index) {
                if (index === currentTime) {
                    point.classList.add('timeline-current');
                } else {
                    point.classList.remove('timeline-current');
                }
            });
            detailInfoObj.value = fxData.value[currentTime];
            detailInfoObj.value.brand = rowData.value.brand;
        }
        // 清除图层
const clearMarkerLayer = () => {
  if (
    markerLayer != undefined &&
    markerLayer != null &&
    markerLayer != ""
  ) {
    // 清空图层
    markerLayer.clearLayers();
  }
};
//清除线图层
const clearLineLayer = () => {
  if (
    lineLayer != undefined &&
    lineLayer != null &&
    lineLayer != ""
  ) {
    // 清空图层
    lineLayer.clearLayers();
  }
};
const wrjDetailRef = ref({})
//无人机详细数据
const wrjDetail = (data) => {
  window.API.wrj.list({
    serialNumber:data.serial
  }).then(res=>{
    if(res.code == 200){
      let detailData = res.result.records[0];
      detailData.currentLongitude=data.uavDetectMsg.dronLng;
      detailData.currentLatitude=data.uavDetectMsg.dronLat;
      detailData.currentAltitude=data.uavDetectMsg.altitude;
      detailData.stationName=data.stationName;
      detailData.stationId=data.stationId;
      nextTick(()=>{
        wrjDetailRef.value.open(detailData)
      })
    }
  })
}


//条数切换
const handleSizeChange = (val) => {
  pageOption.value.pageSize = val;
  getData();
};
//页数切换
const handleCurrentChange = (val) => {
  pageOption.value.pageNo = val;
  getData();
}
const inquires =() =>{ 
  getData()
}
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
      // addPoint()
    }
    
  })
}
//定位设备
const flyDevice = (data) => {
  if(data.wd && data.jd){
    window.Map2D.map.flyTo([data.wd,data.jd],10)
  }else{
    ElMessage.warning('该设备没有经纬度！')
  }
  
}

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

const animateCircle = (circle, targetRadius, duration = 1000) => {
    let currentRadius = 0;
    const startTime = Date.now();
    
    function update() {
        const elapsed = Date.now() - startTime;
        const progress = elapsed / duration;
        
        if (progress < 1) {
            // 使用正弦函数实现波动效果
            const waveProgress = Math.sin(progress * Math.PI) * targetRadius;
            circle.setRadius(waveProgress);
            requestAnimationFrame(update);
        } else {
            circle.setRadius(0);
            requestAnimationFrame(() => {
                animateCircle(circle, targetRadius); // 循环动画
            });
        }
    }
    
    requestAnimationFrame(update);
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
const wrjIcon = ref(null);
// 在地图上标点
const wrjAddPoint = () => {
  clearLayer2()
  
  
 
  wrjData.value.forEach((item) => {
    if(item.authStatus==1){
       wrjIcon.value = window.L.icon({
        iconUrl: "/static/fly1.png",
        iconSize: [40, 40],
      });
    }else if(item.authStatus==2){
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
    if(item.uavDetectMsg.dronLat && item.uavDetectMsg.dronLng){
      const marker = window.L.marker(
        window.L.latLng(Number(item.uavDetectMsg.dronLat), Number(item.uavDetectMsg.dronLng)),
        {
          icon: wrjIcon.value,
        }
      ).addTo(window.wrjMarkerLayer);
      // const innerHTML = "名称: " + item.MC + "<br>";
      // innerHTML += "经度: " + item.JD + "<br>";
      // innerHTML += "纬度: " + item.WD + "<br>";
      const html = `<div style="width:140px;background:rgba(30, 32, 44);padding:10px">
                  <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                    <div style="width:100%;margin: 4px 0;color:#fff;">序列号：<span style="color:#fff;">${item.serial}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">品牌：<span style="color:#fff;">${item.brand}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">型号：<span style="color:#fff;">${item.model}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">经度：<span style="color:#fff;">${item.uavDetectMsg.dronLng.toFixed(3)}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">纬度：<span style="color:#fff;">${item.uavDetectMsg.dronLat.toFixed(3)}</span></div>
                  </div>
                </div>`;
      marker
        // bindTooltip
        .bindPopup(item.brand+'-'+item.model+'-'+item.serial)
        .bindTooltip(html)
        // .openPopup(marker.getLatLng());
        var  markerIcon = L.divIcon({
                html: `<div style='width:160px;color: #fff;text-align: center;font-family: SimHei;font-size:14px;'>${item.brand}-${item.model}-${item.serial}</div>`,//marker标注
                className: 'my-div-icon',
                iconAnchor: [60, -20]//文字标注相对位置
              });
               window.L.marker(
        window.L.latLng(Number(item.uavDetectMsg.dronLat), Number(item.uavDetectMsg.dronLng)),
        {
          icon: markerIcon,
        }
      ).addTo(window.wrjMarkerLayer);
      marker.on("click", function (e) {
        console.log(e);
        wrjDetail(item);
      });
      let csData={}
      Object.assign(csData,item)
      csData.rq = rq.value;
      marker.on('contextmenu', function(evt){
        var rigList = [
            { text: '诱骗', iconname: 'yp', click: `yp(${evt.latlng.lng},${evt.latlng.lat})` },
            { text: '干扰', iconname: 'gr', click: `gr(${evt.latlng.lng},${evt.latlng.lat})` },
            { text: '推演', iconname: 'ty', click: `ty(${JSON.stringify(csData)})` },
        ]
        //console.log();
        var mapRigMenuHtm = function (o) {
            return `
          <div class='cd-span' style="padding:5px 10px; cursor: pointer;">
            <a onclick='${o.click}'>
             <img src='/static/map_img/${o.iconname}.png' style='vertical-align: middle;'>${o.text}</a></a>
          </div>`
        }

        var rigHtm = ''
        for (let i = 0; i < rigList.length; i++) {
            rigHtm += mapRigMenuHtm(rigList[i])
        }

        //添加地图弹出框
        L.popup({
            className: 'mypopup',
        }).setLatLng(evt.latlng).setContent(rigHtm).openOn(window.Map2D.map)
      })
    }
  });
};




const fsIcon = ref(null);
// 在地图上标点
const fsAddPoint = () => {
  clearLayer3()
  
  window.fsMarkerLayer = window.L.layerGroup([]);
  window.fsMarkerLayer.addTo(window.Map2D.map);
  fsIcon.value = window.L.icon({
    iconUrl: "/static/fs33.png",
    iconSize: [40, 40],
  });
  wrjData.value.forEach((item) => {
    // console.log(item.uavDetectMsg.pilotLat ,item.uavDetectMsg.pilotLng);
    if(item.uavDetectMsg.pilotLat && item.uavDetectMsg.pilotLng){
      const marker = window.L.marker(
        window.L.latLng(Number(item.uavDetectMsg.pilotLat), Number(item.uavDetectMsg.pilotLng)),
        {
          icon: fsIcon.value,
        }
      ).addTo(window.fsMarkerLayer);
      // const innerHTML = "名称: " + item.MC + "<br>";
      // innerHTML += "经度: " + item.JD + "<br>";
      // innerHTML += "纬度: " + item.WD + "<br>";
      const html = `<div style="width:140px;background:rgba(30, 32, 44);padding:10px">
                  <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                    <div style="width:100%;margin: 4px 0;color:#fff;">序列号：<span style="color:#fff;">${item.serial}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">品牌：<span style="color:#fff;">${item.brand}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">经度：<span style="color:#fff;">${item.uavDetectMsg.pilotLng}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">纬度：<span style="color:#fff;">${item.uavDetectMsg.pilotLat}</span></div>
                  </div>
                </div>`;
      marker
        // bindTooltip
        .bindPopup(item.brand+'-'+item.serial+'-'+item.serial)
        .bindTooltip(html)

        var  markerIcon = L.divIcon({
                html: `<div style='width:160px;color: #fff;text-align: center;font-family: SimHei;font-size:14px;'>所属飞机：${item.brand}-${item.model}-${item.serial}</div>`,//marker标注
                className: 'my-div-icon',
                iconAnchor: [60, -20]//文字标注相对位置
              });
               window.L.marker(
        window.L.latLng(Number(item.uavDetectMsg.pilotLat), Number(item.uavDetectMsg.pilotLng)),
        {
          icon: markerIcon,
        }
      ).addTo(window.fsMarkerLayer);
        // .openPopup(marker.getLatLng());
      // marker.on("click", function (e) {
      
      // });
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
// 清除图层
const clearLayer2 = () => {
  if (
      window.wrjMarkerLayer != undefined &&
      window.wrjMarkerLayer != null &&
      window.wrjMarkerLayer != ""
  ) {
    // 清空图层
    window.wrjMarkerLayer.clearLayers();
  }
};
// 清除图层
const clearLayer3 = () => {
  if (
      window.fsMarkerLayer != undefined &&
      window.fsMarkerLayer != null &&
      window.fsMarkerLayer != ""
  ) {
    // 清空图层
    window.fsMarkerLayer.clearLayers();
  }
};
</script>

<style scoped lang="less">
.topCenter {
  width: 100vw;
  height: 68.4vh;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  .buttons{
    position: absolute;
    top: 30px;
    left: 20px;
    pointer-events: auto;
    z-index:1;
  }
  // pointer-events: none;
  .left{
    width: 120px;
    height:140px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 12px;
    color: #fff;
    justify-content: space-around;
    pointer-events: auto;
    .menus{
      width: 100%;
      margin-top: 20px;
      cursor: pointer;
      &>div{
        text-align: center;
      }
    }
  }
  


  .wxdsbBox{
    width: 320px;
    height: 600px;
    background: url("@/assets/allImage/dialogBg.png") no-repeat;
    background-size: 100% 100%;
    position: absolute;
    left: 5px;
    top:20px;
    padding: 10px;
    pointer-events: auto;
    .title{
      width: 100%;
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 14px;
      color: #fff;
    }
    .search{
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 10px;
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
      :deep(.el-button){
        // background-color: rgba(97, 137, 177,0.8) !important;
        color: #fff  !important;
        margin-left:10px;
        background: url(@/assets/allImage/btnBg.png) no-repeat;
        background-size: 100% 100%;
        border:none;
      }
    }
    .content{
      width: 100%;
      color:#fff;
      // display: flex;
      // justify-content: space-between;
      // align-items: center;
      // flex-wrap: wrap;
      overflow-y: auto;
      height: calc(100% - 95px);
      .wxdsb{
        width: 48%;
        height: 80px;
        margin-top: 15px;
        float: left;
      }
      .wxdsb:nth-of-type(2n+2){
        margin-left:10px;
      }
      .content-top{
        background-color: rgb(81, 127, 173);
        text-align: center;
        position: relative;
        &>div:nth-of-type(1){
          position: absolute;
          bottom:0;
          right: 0;
          font-size: 12px;
        }
        .color1{
          color: #09eb09;
        }
        .color2{
          color: red;;
        }
        .color3{
          color:yellow;
        }
      }
      .content-bottom{
        height: 30px;
        line-height: 30px;
        font-size:12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: rgb(0, 40, 93);
        &>div:nth-of-type(2){
          width:calc(100% - 20px);
          height:100%;
          white-space: nowrap; /* 不换行 */
          text-overflow: ellipsis; /* 省略符显示为省略号 */
          overflow: hidden; /* 隐藏溢出的文本 */
        }
        .dotStatus{
          width: 5px;
          height: 5px;
          border-radius: 50%;
          margin-right:5px;
        }
        .bg1{
          background-color: #09eb09;
        }
        .bg2{
          background-color: red;
        }
      }
    }
  }
  #zcwrjxx{
    transform: translateX(0);
    transition: transform 0.3s ease;
  }
  #zcwrjXxxx{
    transform: translateX(350px);
    transition: transform 0.3s ease;
  }
  #contextMenu {
    position: absolute;
    background-color: #1f5891;
    border: 1px solid #1f5891;
    border-radius: 4px;
    box-shadow: 0 2px 4px rgb(0 0 0 / 10%);
    z-index: 1;
    padding: 10px 10px;
  }
  .wsdtc{
    width: 300px;
    height: 600px;
    background: url("@/assets/allImage/dialogBg.png") no-repeat;
    background-size: 100% 100%;
    position: absolute;
    right: 25px;
    top:20px;
    padding: 10px;
    pointer-events: auto;
    .wrjToggleBtn{
      position:absolute;
      top:50%;
      transform:translateY(-50%);
      cursor:pointer;
      width: 25px;
      padding: 2px;
    }
    .wrjToggleBg1{
      background:url('/public/static/toggle/toggle-bg1.png') no-repeat;
      background-size:100% 100%;
      left:-30px;
     
    }
    .wrjToggleBg2{
      background:url('/public/static/toggle/toggle-bg2.png') no-repeat;
      background-size:100% 100%; 
      right:-28px;
    }
    .title{
      width: 100%;
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 14px;
      color: #fff;
    }
    .search{
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 10px;
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
      :deep(.el-button){
        // background-color: rgba(97, 137, 177,0.8) !important;
        color: #fff  !important;
        margin-left:10px;
        background: url(@/assets/allImage/btnBg.png) no-repeat;
        background-size: 100% 100%;
        border:none;
      }
    }

     .content{
      color:#fff;
      // display: flex;
      // justify-content: space-between;
      // // align-items: center;
      // flex-wrap: wrap;
      overflow-y: auto;
      height: calc(100% - 65px);
      .wxdsb{
        width: 100%;
        height: 90px;
        margin-top: 10px;
        cursor: pointer;
        box-sizing: border-box;
        position:relative;
      }
      .content-top{
        background-color: rgb(81, 127, 173);
        text-align: center;
        position: relative;
        &>div:nth-of-type(1){
          position: absolute;
          bottom:0;
          right: 0;
          font-size: 12px;
        }
        .color1{
          color: #09eb09;
        }
        .color2{
          color: red;;
        }
        .color3{
          color: rgb(71, 70, 70);
        }
      }
      .content-bottom{
        height: 30px;
        line-height: 30px;
        font-size:12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: rgb(0, 40, 93);
        .dotStatus{
          width: 5px;
          height: 5px;
          border-radius: 50%;
          margin-right:5px;
        }
        &>div:nth-of-type(2){
          width:calc(100% - 15px);
          white-space: nowrap; /* 不换行 */
          text-overflow: ellipsis; /* 省略符显示为省略号 */
          overflow: hidden; /* 隐藏溢出的文本 */
        }
        .bg1{
          background-color: #09eb09;
        }
        .bg2{
          background-color: red;
        }
        .bg3{
          background-color: rgb(71, 70, 70);
        }
      }
    }


    .el-col{
        margin-top: 10px;
        display: flex;
        padding: 5px 0;
        font-size:14px;
      }
      .detailInfo_label{
        color: rgba(255, 255, 255, 0.7);
        font-weight: bold;
        width: 125px;
        text-align: right;
      }
      .detailInfo_text{
        width: calc(100% - 125px);
      }
  }
  
}
.wrjActive{
  border:2px solid #00a5ff;
}
/* 分页样式 */
:deep(.el-pager li) {
  background: transparent;
  border: 1px solid rgba(115, 116, 117);
  color: #fff;
  margin: 0 5px;
}

:deep(.el-pager li.is-active) {
  background: #0e417c;
  border: 1px solid #fff;
  color: #fff;
}

:deep(.el-pagination) {
  display: flex;
  justify-content: flex-end;
  margin-top: 5px;
}
:deep(.el-pagination__total){
  color:#fff;
}
:deep(.el-pagination__jump){
  color:#fff;
}
:deep(.el-pagination button){
  background: transparent;
  border: 1px solid #fff;
}
:deep(.el-pagination .btn-next .el-icon), :deep(.el-pagination .btn-prev .el-icon){
  color:#fff !important;
}
:deep(.el-pagination button.is-disabled), :deep(.el-pagination button:disabled){
  background: transparent;
  border: 1px solid #fff;
}
:deep(.el-pagination button:hover), :deep(.el-pagination button:hover .el-icon){
  color:#409eff !important;
}
</style>