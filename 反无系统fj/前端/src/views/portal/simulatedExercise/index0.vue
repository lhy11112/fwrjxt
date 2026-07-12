<template>
  <div class="topCenter">
    <div class="buttons">
      <el-button type="primary" @click="setting">设置</el-button>
      <el-button  type="primary" @click="historyData">历史数据</el-button>
      <el-button v-if="btnFlag" type="primary" @click="flyKz(true)">开始</el-button>
      <el-button v-if="btnFlag" type="primary" @click="flyKz(false)">暂停</el-button>
      <el-button v-if="btnFlag" type="primary" @click="flyMy()">漫游</el-button>
    </div>
    <div class="bottomTitle">
    <div class="coord">
      <span>
        经度:
        {{ lat }}
      </span>
      <span>
        纬度:
        {{ lng }}
      </span>
      <span>
        速度:
        {{ speed }}m/s
      </span>
    </div>
  </div>
  </div>
  
  <div class="settingBox" v-show="settingVisible">
    <div class="content">
      <div class="title">
        <div>基本信息</div>
        <el-icon @click="settingClose" style="cursor:pointer;"><Close /></el-icon>
      </div>
      <div class="flex_box">
        <el-form-item label="推演计划名称:">
          <el-input v-model="trailParam.mc" style="width:100%" :placeholder="'请输入'" clearable></el-input>
        </el-form-item>
      </div>

      <div class="flex_box">
        <el-form-item label="推演计划日期:">
          <el-date-picker v-model="trailParam.rq" type="date" value-format="YYYY-MM-DD" format="YYYY-MM-DD" style="width:100%" :placeholder="'请输入'" clearable></el-date-picker>
        </el-form-item>
      </div>

       <div class="flex_box">
        <el-form-item label="无人机:">
          <el-select v-model="trailParam.wrj" placeholder="请选择" clearable filterable @change="wrjChange">
            <el-option v-for="(item,index) in wrjData" :key="index" :label="item.brand+item.serialNumber" :value="item.serialNumber"></el-option>
          </el-select>
        </el-form-item>
      </div>
      <!-- <div class="flex_box">
        <el-form-item label="无人机型号:">
          <el-input v-model="trailParam.model" style="width:100%" :placeholder="'请输入'" clearable></el-input>
        </el-form-item>
      </div>
      <div class="flex_box">
        <el-form-item label="无人机标识:">
          <el-input v-model="trailParam.serial" style="width:100%" :placeholder="'请输入'" clearable></el-input>
        </el-form-item>
      </div>
      <div class="flex_box">
        <el-form-item label="无人机品牌:">
          <el-input v-model="trailParam.brand" style="width:100%" :placeholder="'请输入'" clearable></el-input>
        </el-form-item>
      </div> -->
      <div class="flex_box">
        <el-form-item label="计划开始:">
          <el-date-picker v-model="trailParam.jhks" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm:ss" style="width:100%" :placeholder="'请输入'" clearable></el-date-picker>
        </el-form-item>
      </div>
      <div class="flex_box">
        <el-form-item label="计划结束:">
          <el-date-picker v-model="trailParam.jhjs" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm:ss" style="width:100%" :placeholder="'请输入'" clearable></el-date-picker>
        </el-form-item>
      </div>
      <div class="title">
        <div>路线情况</div>
      </div>
      <div class="flex_box" style="width:100%">
        <el-form-item label="起点:">
          <el-input v-model="trailParam.uavDatectMsgDto1.start.longitude" style="width:22%" :placeholder="'请输入经度'" clearable></el-input>
          <el-input v-model="trailParam.uavDatectMsgDto1.start.latitude" style="width:22%;margin-left:calc(4% / 4)" :placeholder="'请输入纬度'" clearable></el-input>
          <el-input v-model="trailParam.uavDatectMsgDto1.start.altitude" style="width:22%;margin-left:calc(4% / 4)" :placeholder="'请输入高度'" clearable></el-input>
          <el-input v-model="trailParam.uavDatectMsgDto1.start.stayTime" style="width:22%;margin-left:calc(4% / 4)" :placeholder="'请输入停留时间'" clearable></el-input>
                  <el-icon title="地图选点" style="font-size: 24px;cursor: pointer;margin-left:10px" @click="route_input"><Location /></el-icon>
        </el-form-item>
      </div>
      <div class="flex_box" style="width:100%">
        <el-form-item label="终点:">
          <el-input v-model="trailParam.uavDatectMsgDto1.end.longitude" style="width:22%" :placeholder="'请输入经度'" clearable></el-input>
          <el-input v-model="trailParam.uavDatectMsgDto1.end.latitude" style="width:22%;margin-left:calc(4% / 4)" :placeholder="'请输入纬度'" clearable></el-input>
          <el-input v-model="trailParam.uavDatectMsgDto1.end.altitude" style="width:22%;margin-left:calc(4% / 4)" :placeholder="'请输入高度'" clearable></el-input>
          <el-input v-model="trailParam.uavDatectMsgDto1.end.stayTime" style="width:22%;margin-left:calc(4% / 4)" :placeholder="'请输入停留时间'" clearable></el-input>
                  <el-icon title="地图选点" style="font-size: 24px;cursor: pointer;margin-left:10px" @click="route_input_end"><Location /></el-icon>
        </el-form-item>
      </div>
      <div class="flex_box" style="width:100%;flex-wrap:wrap;">
        <div class="title" style="width:100%">
          途经点
        </div>
      <el-row v-for="(item, index) in trailParam.uavDatectMsgDto1.waypoints" :key="index">
                <el-col :span="5">
                  <el-form-item label="经度">
                   <el-input v-model="item.longitude" placeholder="请输入"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="5">
                  <el-form-item label="纬度">
                    <el-input v-model="item.latitude" placeholder="请输入"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="5">
                  <el-form-item label="高度">
                    <el-input v-model="item.altitude" placeholder="请输入"></el-input>
                  </el-form-item>
                </el-col>
                 <el-col :span="5">
                  <el-form-item label="时间">
                    <el-input v-model="item.stayTime" placeholder="请输入"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="4" style="text-align: center;line-height: 45px;">
                  <el-icon style="font-size: 24px;margin-right: 10px;cursor: pointer;" @click="addWayPoints"><CirclePlus /></el-icon>
                  <el-icon style="font-size: 24px;cursor: pointer;" @click="delWayPoints(index)"><Remove /></el-icon>
                  <el-icon title="地图选点" style="font-size: 24px;cursor: pointer;margin-left:10px" @click="route_input_tjd(index)"><Location /></el-icon>
                </el-col>
            </el-row>
      </div>
      <div class="title">
        <div>参数设置</div>
      </div>
      <div class="flex_box">
        <el-form-item label="最大速度(米/秒):">
          <el-input v-model="trailParam.uavDatectMsgDto1.maxSpeed" style="width:100%" :placeholder="'请输入'" clearable></el-input>
        </el-form-item>
      </div>
      <div class="flex_box">
        <el-form-item label="最大加速度(米/秒²):">
          <el-input v-model="trailParam.uavDatectMsgDto1.acceleration" style="width:100%" :placeholder="'请输入'" clearable></el-input>
        </el-form-item>
      </div>
      <div class="flex_box">
        <el-form-item label="最小转弯半径(米):">
          <el-input v-model="trailParam.uavDatectMsgDto1.turnRadius" style="width:100%" :placeholder="'请输入'" clearable></el-input>
        </el-form-item>
      </div>
      </div>
      <div class="setting-button">
        <el-button @click="startTy">开始推演</el-button>
      </div>

    </div>

    <div class="historyDataBox" v-show="historyVisible">
      <div class="tableBox">
              <el-table
                :data="tableData"
                style="width: 100%; height: 100%"
                class="custom-table"
                @selectionChange="selectionChange"
                @row-click="rowClickChange"
                v-loading="loading"
              >
                <el-table-column type="selection" width="50"></el-table-column>
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
                      @click.prevent="getTyjhData(scope.row,0)"
                    >
                      开始推演
                    </el-button>
                    <el-button
                      style="color: #fff"
                      link
                      type="primary"
                      size="small"
                      @click.prevent="getTyjhData(scope.row,1)"
                    >
                      结束推演
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <div>
              <el-pagination
                class="pagination"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="pageOption.pageNo"
                :page-size="pageOption.pageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                :page-sizes="[8, 10, 20, 50,100]"
              >
              </el-pagination>
            </div>
    </div>

    <!-- <div class="c-timeLine" v-show="showTimeLine">
      <timeLine ref="timeLineRef" :showTimeLine="showTimeLine" :tyjhData="tyjhData" @handlezztimeChange="handlezztimeChange"></timeLine>
    </div> -->
</template>

<script setup>
// 模拟推演
import { useRouter } from "vue-router";
import microApp from "@/components/microApp/index.vue";
import { ref, onMounted,onUnmounted,nextTick } from "vue";
import icon1 from "@/assets/leftTitle/leftImg.png"
import wxdzcsb from "@/assets/allImage/wxdzcsb.png"
import {ElMessage} from "element-plus";
import { useMap3DStore } from "@/store/modules/map3D";
import { useMap2DStore } from "@/store/modules/map2D";
import { mapModeEnum, getCurrentMapMode } from "@/utils/Map/mapMode";
// import timeLine from "@/components/timeline/index.vue";
// 定义路由
const router = useRouter();
const settingVisible = ref(false)
const queryInfo = ref({
  isValid:1,
  name:""
})
const wxdsbData = ref([])
const trailParam = ref({
  uavDatectMsgDto1:{
    start:{},
    end:{},
    waypoints:[{

    }]
  }
})
const pageOption = ref({
  pageNo:1,
  pageSize:10
})
const total = ref(0)
let markerLayer = null;
let lineLayer = null;
const mapChange = ref(false)
// 初始化
onMounted(()=>{
  getWrj()
  
  nextTick(()=>{
    window.Map2D.map.on('click', evt => {
      console.log(evt);
      markerToMap(evt)
    })
    
    markerLayer = L.layerGroup([]);
    markerLayer.addTo(window.Map2D.map);
    lineLayer = L.layerGroup([]);
    lineLayer.addTo(window.Map2D.map);
  })
  
  window.eventBus.on("mapChange",function(e){
    mapChange.value = !mapChange.value;
    speed.value = 0;
    lng.value = 0;
    lat.value = 0;
    window.eventBus.off("wrjData")
    clearInterval(timelineInterval);
    clearLayer1()
    clearLayer2()
    timelineInterval = null;
    const element = document.getElementById('timeline');
    console.log(element);
    if(element){
      element.remove()
    }
    addToMap()
  })
   
})
onUnmounted(()=>{
  const element = document.getElementById('timeline');
  console.log(element);
  if(element){
    element.remove()
  }
  clearLayer1()
  clearLayer2()
  window.eventBus.off("mapChange")
})
const markerToMap = (evt) => {
  console.log(dotStatus.value);
  if(dotStatus.value == "始发地"){
      const x = evt.latlng.lng
      const y = evt.latlng.lat
      trailParam.value.uavDatectMsgDto1.start.longitude = x.toFixed(8);
      trailParam.value.uavDatectMsgDto1.start.latitude = y.toFixed(8);
    }else if(dotStatus.value == "目的地"){
      
      const x = evt.latlng.lng
      const y = evt.latlng.lat
      trailParam.value.uavDatectMsgDto1.end.longitude = x;
      trailParam.value.uavDatectMsgDto1.end.latitude = y;
    }else if(dotStatus.value == "途经点"){
      const x = evt.latlng.lng
      const y = evt.latlng.lat
      trailParam.value.uavDatectMsgDto1.waypoints[tjdNum.value].longitude = x;
      trailParam.value.uavDatectMsgDto1.waypoints[tjdNum.value].latitude = y;
    }
}
const dotStatus = ref(null)
const tjdNum = ref(0)
const route_input_tjd = (i) =>{
  tjdNum.value=i;
  dotStatus.value = "途经点"
  console.log(dotStatus.value);
}
const route_input = () => {
  dotStatus.value = "始发地"
}

const route_input_end = () => {
  dotStatus.value = "目的地"
}

const settingClose = () => {
  settingVisible.value = false;
}
// const timeLineRef = ref(null);
const flyKz = (data) => {
  if(mapChange.value){
    if (curMapMode === mapModeEnum["2D"]) {
      window.Map3D.wrjFly.togglePlayPause(data)
    }else if (curMapMode === mapModeEnum["3D"]){
      if (data) {
        timelineInterval = setInterval(function() {
          currentTime = (currentTime + 1) % tyjhData.value.length;
          updateTimeline(currentTime);
          clearLayer1()
          console.log(tyjhData.value[currentTime]);
          planeMarkers[currentTime].setLatLng([tyjhData.value[currentTime].dronLat, tyjhData.value[currentTime].dronLng]).addTo(markerLayer);
          const  markerIcon = L.divIcon({
                html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>${rowData.value.brand}${rowData.value.model}</div>`,//marker标注
                className: 'my-div-icon',
                iconAnchor: [80, -20]//文字标注相对位置
              });
          window.L.marker(
            window.L.latLng(Number(tyjhData.value[currentTime].dronLat), Number(tyjhData.value[currentTime].dronLng)),
            {
              icon: markerIcon,
            }
          ).addTo(markerLayer);
          // // 使用turf.js计算距离
                const fxTime = (new Date(tyjhData.value[currentTime].dataTime).getTime() - new Date(tyjhData.value[0].dataTime).getTime()) * 1000
                const distance = turf.distance([Number(tyjhData.value[0].dronLat),Number(tyjhData.value[0].dronLng)], [Number(tyjhData.value[currentTime].dronLat), Number(tyjhData.value[currentTime].dronLng)]);
                speed.value =(distance * 1000 / fxTime).toFixed(8)
                lat.value = tyjhData.value[currentTime].dronLat
                lng.value = tyjhData.value[currentTime].dronLng
        }, 1000);
        // timeLineRef.value.autoPlayTimer(1)
      } else {
        clearInterval(timelineInterval);
        timelineInterval = null;
        // timeLineRef.value.autoPlayTimer(2)
      }
    }
  }else{
    if (curMapMode === mapModeEnum["2D"]) {
      if (data) {
        timelineInterval = setInterval(function() {
          currentTime = (currentTime + 1) % tyjhData.value.length;
          updateTimeline(currentTime);
          clearLayer1()
          console.log(tyjhData.value[currentTime]);
          planeMarkers[currentTime].setLatLng([tyjhData.value[currentTime].dronLat, tyjhData.value[currentTime].dronLng]).addTo(markerLayer);
          const  markerIcon = L.divIcon({
                html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>${rowData.value.brand}${rowData.value.model}</div>`,//marker标注
                className: 'my-div-icon',
                iconAnchor: [80, -20]//文字标注相对位置
              });
          window.L.marker(
            window.L.latLng(Number(tyjhData.value[currentTime].dronLat), Number(tyjhData.value[currentTime].dronLng)),
            {
              icon: markerIcon,
            }
          ).addTo(markerLayer);
          // // 使用turf.js计算距离
                const fxTime = (new Date(tyjhData.value[currentTime].dataTime).getTime() - new Date(tyjhData.value[0].dataTime).getTime()) * 1000
                const distance = turf.distance([Number(tyjhData.value[0].dronLat),Number(tyjhData.value[0].dronLng)], [Number(tyjhData.value[currentTime].dronLat), Number(tyjhData.value[currentTime].dronLng)]);
                speed.value =(distance * 1000 / fxTime).toFixed(8)
                lat.value = tyjhData.value[currentTime].dronLat
                lng.value = tyjhData.value[currentTime].dronLng
        }, 1000);
        // timeLineRef.value.autoPlayTimer(1)
      } else {
        clearInterval(timelineInterval);
        timelineInterval = null;
        // timeLineRef.value.autoPlayTimer(2)
      }
    }else if (curMapMode === mapModeEnum["3D"]){
      window.Map3D.wrjFly.togglePlayPause(data)
    }
  }
   
  
}
const myFlag = ref(true)
const flyMy = () =>{
  myFlag.value = !myFlag.value
  if(myFlag.value){
    window.Map3D.wrjFly.setSj(3)
  }else{
    window.Map3D.wrjFly.setSj(1)
  }
  
}
const setting = () =>{
  settingVisible.value = !settingVisible.value;
  historyVisible.value = false;
  trailParam.value = {
    uavDatectMsgDto1:{
      start:{},
      end:{},
      waypoints:[{

      }]
    }
  }
}
//添加途经点
const addWayPoints = () =>{
      trailParam.value.uavDatectMsgDto1.waypoints.push({
        longitude: '',
        latitude: '',
        altitude: '',
        stayTime: ''
      })
    }
const delWayPoints = (index) =>{
  if(trailParam.value.uavDatectMsgDto1.waypoints.length==1){
    ElMessage({
          type: "info",
          message: "至少保留一个",
        });
        return
  }
     trailParam.value.uavDatectMsgDto1.waypoints.splice(index,1)
    }
const startTy = () => {
//   let data={
//     "mc": "20251027推演计划1111",
//     "rq": "2025-09-27",
//     "model": "DJI-Inspire",
//     "serial": "SERIAL-1583",
//     "jhks":"2025-09-27 08:30:00",
//     "jhjs":"2025-09-27 10:00:00",
//     "brand":"大疆",
//     "uavDatectMsgDto1": {
//         "model": "DJI-Inspire",
//         "serial": "SERIAL-1583",
//         "rq": "2025-09-27",
//         "stationId": 0,
//         "start": {
//             "longitude": 119.3062,
//             "latitude": 26.0745,
//             "altitude": "0",
//             "stayTime": "0"
//         },
//         "end": {
//             "longitude": 118.0894,
//             "latitude": 24.4798,
//             "altitude": "0",
//             "stayTime": "0"
//         },
//         "waypoints": [
//             {
//                 "longitude": "119.0098",
//                 "latitude": "25.4480",
//                 "altitude": "150",
//                 "stayTime": "5"
//             },
//             {
//                 "longitude": "118.5895",
//                 "latitude": "24.9088",
//                 "altitude": "200",
//                 "stayTime": "5"
//             }
//         ]
//     }
// }
console.log(trailParam.value);
trailParam.value.uavDatectMsgDto1.model = trailParam.value.model;
trailParam.value.uavDatectMsgDto1.serial = trailParam.value.serial;
trailParam.value.uavDatectMsgDto1.rq = trailParam.value.rq;
trailParam.value.uavDatectMsgDto1.stationId = trailParam.value.stationId;

  window.API.mnty.TYJHSC(trailParam.value).then(res=>{
    console.log(res);
    if(res.success){
      settingVisible.value = false;
      trailParam.value ={};
      getTyjhData({id:res.result,authStatus:trailParam.value.authStatus,brand:trailParam.value.brand,model:trailParam.value.model},0)
    }else{
      ElMessage.error('推演失败')
    }
  })
}

//推演计划状态
const getTYJHCZ = (row) => {
  window.API.wrjtyjh.TYJHCZ({
    id:row.id,
    zt:row.zt
  }).then(res=>{
    console.log(res);
  })
}

const historyVisible = ref(false)
const columnData = ref([
  {
    prop:"mc",
    label:"推演计划名称",
    width:'120px'
  },
  {
    prop:"rq",
    label:"推演计划日期",
    width:'120px'
  },
  {
    prop:"jhks",
    label:"计划开始",
    width:'120px'
  },
  {
    prop:"jhjs",
    label:"计划结束",
    width:'120px'
  },
  {
    prop:"serial",
    label:"无人机序列号",
    width:'120px'
  },
  {
    prop:"brand",
    label:"品牌"
  },
  {
    prop:"tyzt",
    label:"状态"
  },
])
const tableData = ref([])
const historyData = () =>{
  historyVisible.value = !historyVisible.value;
  settingVisible.value = false;
  getData()
}
const getData = () => {
  window.API.wrjtyjh.list({
    pageNo:pageOption.value.pageNo,
    pageSize:pageOption.value.pageSize,
  }).then(res=>{
    if(res.success){
      tableData.value = res.result.records;
      total.value = res.result.total;
    }
  })
}
//条数切换
const  handleSizeChange = (val) =>{
  pageOption.value.pageSize = val;
  getData();
}
//页数切换
const  handleCurrentChange = (val) =>{
  pageOption.value.pageNo = val;
  getData();
}

const wrjData = ref([])
const getWrj = () => {
  window.API.wrj.list({
    pageNo:1,
    pageSize:10000
  }).then(res=>{
    if(res.success){
      wrjData.value = res.result.records;
    }
  })
}

const wrjChange = (e) =>{
  let values = wrjData.value.filter(v=>v.serialNumber==e);
  if(values && values.length){
    trailParam.value.model = values[0].model;
    trailParam.value.serial = values[0].serialNumber;
    trailParam.value.brand = values[0].brand;
    trailParam.value.stationId = values[0].stationId;
    trailParam.value.authStatus = values[0].authStatus;
  }
}

const tyjhData = ref([])
const map3dStore = useMap3DStore();
const map2dStore = useMap2DStore();
const btnFlag = ref(false);
let timelineInterval = null;
// 初始化当前时间点
let currentTime = 0;
let planeMarkers = [];
// 创建时间点
let timelinePoints = [];
const curMapMode = getCurrentMapMode();

const speed = ref(0)
const lat = ref(0)
const lng = ref(0)
const getTyjhData = (row,zt) => {
  rowData.value = row;
  btnFlag.value = zt==1?false:true;
  // window.Map3D.viewer.on("click",function(e){
  //   console.log(e);
  // })
  
  getTYJHCZ({id:row.id,zt:zt})
  const element = document.getElementById('timeline');
  console.log(element);
  if(element){
    element.remove()
  }
  clearLayer1()
  clearLayer2()
  clearInterval(timelineInterval);
  timelineInterval = null;
  window.API.wrjtyjhgj.list({
    pageNo:1,
    pageSize:10000,
    tyjhId:row.id
  }).then(res=>{
    if(res.success){
      tyjhData.value = res.result.records;
      addToMap()
      getData()
    }
  })
}
const handlezztimeChange = (e) => {
  console.log(e);
}
const rowData = ref({})
const timeline = ref(null);
const showTimeLine  =ref(false)
const addToMap = () => {
  
  console.log(curMapMode);
  if(tyjhData.value && tyjhData.value.length){
    if(mapChange.value){
        if (curMapMode === mapModeEnum["3D"]) {
          window.eventBus.off("wrjData")
          speed.value = 0;
          lng.value = 0;
          lat.value = 0;
          // 创建时间轴
          // 创建ID为timeline的div元素
          timeline.value = document.createElement('div');
          timeline.value.id = 'timeline';
          var largeContent = document.getElementsByClassName('largeContent')[0];
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
          timelinePoints = tyjhData.value.map(function(item, index) {
              var point = document.createElement('div');
              point.className = 'timeline-point';
              point.style.left = (index / (tyjhData.value.length - 1) * 100) + '%';
              point.dataset.index = index;
              timelineElement.appendChild(point);
              return point;
          });

          // 创建时间刻度
          var timelineElement = document.getElementById('timeline');
          var timelineLine = timelineElement.querySelector('.timeline-line');
          // 创建时间刻度
          var lastTime = null;

          // 根据时间点数量创建时间刻度
          // tyjhData.value.forEach(function(item, index) {
          //   console.log(new Date(item.dataTime) - new Date(lastTime));
          //     // 计算时间差，如果大于10分钟，则创建刻度
          //     if (lastTime === null || (new Date(item.dataTime) - new Date(lastTime)) >= 600000) { // 600000毫秒等于10分钟
          //         var tick = document.createElement('div');
          //         tick.className = 'timeline-tick';
          //         tick.style.left = (index / (tyjhData.value.length - 1) * 100) + '%';
                  
          //         timelineLine.appendChild(tick);
          //         var timeKd = document.createElement('div');
          //         timeKd.className = 'timeKd';
          //         timeKd.innerHTML = item.dataTime; // 显示时间
          //         tick.appendChild(timeKd);
          //         console.log(tick);
          //         lastTime = item.dataTime;
          //     }
          // });
          // 首先获取所有时间点
const times = tyjhData.value.map(item => new Date(item.dataTime));
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
tyjhData.value.forEach((item, index) => {
    const currentTimes = new Date(item.dataTime);
    
    // 如果当前时间超过了当前刻度时间加上间隔时间
    while (currentTimes >= currentTick + interval) {
        // 创建刻度
        const tick = document.createElement('div');
        tick.className = 'timeline-tick';
        tick.style.left = ((index) / (tyjhData.value.length - 1) * 100) + '%';
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
const kdLastTime = new Date(tyjhData.value[tyjhData.value.length - 1].dataTime);
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
              var time = (x / timelineElement.offsetWidth) * (tyjhData.value.length - 1);
              currentTime = Math.round(time);
              updateTimeline(currentTime);
          });

          showTimeLine.value = true;

          // 添加飞行轨迹
          var polyline = L.polyline(tyjhData.value.map(p => [p.dronLat, p.dronLng] )).addTo(lineLayer);
          // 添加飞机图标
          var planeIcon = L.icon({
              iconUrl: '/static/fly1.png',
              iconSize: [25, 25],
              iconAnchor: [12, 41],
              popupAnchor: [-3, -73]
          });

        
          tyjhData.value.forEach(function(point) {
              var marker = L.marker([point.dronLat, point.dronLng], { icon: planeIcon });
              planeMarkers.push(marker);
          });

          // 时间轴更新逻辑
          timelineInterval = setInterval(function() {
              currentTime = (currentTime + 1) % tyjhData.value.length;
              updateTimeline(currentTime);
              // polyline.setLatLngs([tyjhData.value[currentTime].dronLat, tyjhData.value[currentTime].dronLng]);
              clearLayer1()
              console.log(tyjhData.value[currentTime]);
              planeMarkers[currentTime].setLatLng([tyjhData.value[currentTime].dronLat, tyjhData.value[currentTime].dronLng]).addTo(markerLayer);
              const  markerIcon = L.divIcon({
                html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>${rowData.value.brand}${rowData.value.model}</div>`,//marker标注
                className: 'my-div-icon',
                iconAnchor: [80, -20]//文字标注相对位置
              });
          window.L.marker(
            window.L.latLng(Number(tyjhData.value[currentTime].dronLat), Number(tyjhData.value[currentTime].dronLng)),
            {
              icon: markerIcon,
            }
          ).addTo(markerLayer);
              // // 使用turf.js计算距离
              const fxTime = (new Date(tyjhData.value[currentTime].dataTime).getTime() - new Date(tyjhData.value[0].dataTime).getTime()) * 1000
               const distance = turf.distance([Number(tyjhData.value[0].dronLat),Number(tyjhData.value[0].dronLng)], [Number(tyjhData.value[currentTime].dronLat), Number(tyjhData.value[currentTime].dronLng)]);
              speed.value =(distance * 1000 / fxTime).toFixed(8)
              lat.value = tyjhData.value[currentTime].dronLat
              lng.value = tyjhData.value[currentTime].dronLng
          }, 1000);
        }else if (curMapMode === mapModeEnum["2D"]) {
          clearInterval(timelineInterval);
          timelineInterval = null;
          speed.value = 0;
          lng.value = 0;
          lat.value = 0;
          const path = Map3D.wrjFly.generateDronePath(tyjhData.value);
          const droneEntity = Map3D.wrjFly.createDroneModel(tyjhData.value);
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
        }
  }else{
    
        if (curMapMode === mapModeEnum["2D"]) {
          window.eventBus.off("wrjData")
          speed.value = 0;
          lng.value = 0;
          lat.value = 0;
          markerLayer = L.layerGroup([]);
          markerLayer.addTo(window.Map2D.map);
          // 创建时间轴
          // 创建ID为timeline的div元素
          timeline.value = document.createElement('div');
          timeline.value.id = 'timeline';
          var largeContent = document.getElementsByClassName('largeContent')[0];
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
          timelinePoints = tyjhData.value.map(function(item, index) {
              var point = document.createElement('div');
              point.className = 'timeline-point';
              point.style.left = (index / (tyjhData.value.length - 1) * 100) + '%';
              point.dataset.index = index;
              timelineElement.appendChild(point);
              return point;
          });

          // 创建时间刻度
          var timelineElement = document.getElementById('timeline');
          var timelineLine = timelineElement.querySelector('.timeline-line');
          // 创建时间刻度
          var lastTime = null;

          // 根据时间点数量创建时间刻度
          // tyjhData.value.forEach(function(item, index) {
          //     // 计算时间差，如果大于10分钟，则创建刻度
          //     if (lastTime === null || (new Date(item.dataTime) - new Date(lastTime)) >= 600000) { // 600000毫秒等于10分钟
          //         var tick = document.createElement('div');
          //         tick.className = 'timeline-tick';
          //         tick.style.left = (index / (tyjhData.value.length - 1) * 100) + '%';
          //         timelineLine.appendChild(tick);
          //         var timeKd = document.createElement('div');
          //         timeKd.className = 'timeKd';
          //         timeKd.innerHTML = item.dataTime; // 显示时间
          //         tick.appendChild(timeKd);
          //         lastTime = item.dataTime;
          //     }
          // });
          // 首先获取所有时间点
// 首先获取所有时间点
const times = tyjhData.value.map(item => new Date(item.dataTime));
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
tyjhData.value.forEach((item, index) => {
    const currentTimes = new Date(item.dataTime);
    
    // 如果当前时间超过了当前刻度时间加上间隔时间
    while (currentTimes >= currentTick + interval) {
        // 创建刻度
        const tick = document.createElement('div');
        tick.className = 'timeline-tick';
        tick.style.left = ((index) / (tyjhData.value.length - 1) * 100) + '%';
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
const kdLastTime = new Date(tyjhData.value[tyjhData.value.length - 1].dataTime);
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
              var time = (x / timelineElement.offsetWidth) * (tyjhData.value.length - 1);
              currentTime = Math.round(time);
              updateTimeline(currentTime);
          });
          showTimeLine.value = true;

          // 添加飞行轨迹
          var polyline = L.polyline(tyjhData.value.map(p => [p.dronLat, p.dronLng] )).addTo(lineLayer);
          // 添加飞机图标
          var planeIcon = L.icon({
              iconUrl: '/static/fly1.png',
              iconSize: [25, 25],
              iconAnchor: [12, 41],
              popupAnchor: [-3, -73]
          });

        
          tyjhData.value.forEach(function(point) {
              var marker = L.marker([point.dronLat, point.dronLng], { icon: planeIcon });
              marker.on('click',(e)=>{
                console.log(e);
              })
              planeMarkers.push(marker);
          });

          // 时间轴更新逻辑
          timelineInterval = setInterval(function() {
              currentTime = (currentTime + 1) % tyjhData.value.length;
              updateTimeline(currentTime);
              // polyline.setLatLngs([tyjhData.value[currentTime].dronLat, tyjhData.value[currentTime].dronLng]);
              clearLayer1()
              // console.log(tyjhData.value[currentTime]);
              planeMarkers[currentTime].setLatLng([tyjhData.value[currentTime].dronLat, tyjhData.value[currentTime].dronLng]).addTo(markerLayer);
              const  markerIcon = L.divIcon({
                html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>${rowData.value.brand}${rowData.value.model}</div>`,//marker标注
                className: 'my-div-icon',
                iconAnchor: [80, 20]//文字标注相对位置
              });
          window.L.marker(
            window.L.latLng(Number(tyjhData.value[currentTime].dronLat), Number(tyjhData.value[currentTime].dronLng)),
            {
              icon: markerIcon,
            }
          ).addTo(markerLayer);
              
              // // 使用turf.js计算距离
              const fxTime = (new Date(tyjhData.value[currentTime].dataTime).getTime() - new Date(tyjhData.value[0].dataTime).getTime()) * 1000
               const distance = turf.distance([Number(tyjhData.value[0].dronLat),Number(tyjhData.value[0].dronLng)], [Number(tyjhData.value[currentTime].dronLat), Number(tyjhData.value[currentTime].dronLng)]);
              speed.value =(distance * 1000 / fxTime).toFixed(8)
              lat.value = tyjhData.value[currentTime].dronLat
              lng.value = tyjhData.value[currentTime].dronLng
         }, 1000);
        }else if (curMapMode === mapModeEnum["3D"]) {
          clearInterval(timelineInterval);
          timelineInterval = null;
          speed.value = 0;
          lng.value = 0;
          lat.value = 0;
          const path = Map3D.wrjFly.generateDronePath(tyjhData.value);
          const droneEntity = Map3D.wrjFly.createDroneModel(tyjhData.value);
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
        }
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
        }
// 清除图层
const clearLayer1 = () => {
  if (
    markerLayer != undefined &&
    markerLayer != null &&
    markerLayer != ""
  ) {
    // 清空图层
    markerLayer.clearLayers();
  }
};
const clearLayer2 = () => {
  if (
    lineLayer != undefined &&
    lineLayer != null &&
    lineLayer != ""
  ) {
    // 清空图层
    lineLayer.clearLayers();
  }
};
</script>

<style scoped lang="less">
.topCenter {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  pointer-events: none;
  
  .buttons{
    position: absolute;
    top: 30px;
    left: 20px;
    pointer-events: auto;
  }
}
.settingBox{
    width: 600px;
    height: 650px;
    background: url("@/assets/allImage/dialogBg.png") no-repeat;
    background-size: 100% 100%;
    position: absolute;
    left: 20px;
    top:68px;
    padding: 10px;
    pointer-events: auto;
    
    .title{
      width: 100%;
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 14px;
      color: #fff;
      padding-left: 8px;
      box-sizing: border-box;
      border-left: 4px solid #108ee9;
    }
    .content{
      width: 100%;
      height: calc(100% - 60px);
      overflow: hidden;
      overflow-y: auto;
    }
    .setting-button{
      height: 60px;
      line-height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      :deep(.el-button){
        // background-color: rgba(97, 137, 177,0.8) !important;
        color: #fff  !important;
        margin-left:10px;
        background: url(@/assets/allImage/btnBg.png) no-repeat;
        background-size: 100% 100%;
        border:none;
      }
    }
  }
  .historyDataBox{
    width: 600px;
    height: 650px;
    background: url("@/assets/allImage/dialogBg.png") no-repeat;
    background-size: 100% 100%;
    position: absolute;
    left: 20px;
    top:68px;
    padding: 10px;
    pointer-events: auto;
    .tableBox{
      height: calc(100% - 32px);
    }
  }
:deep(.el-textarea.is-disabled .el-textarea__inner){
  color:#fff !important;
}
:deep(.el-input-group__append){
  padding:0 !important;
}
.flex_box {
  display: flex;
  position: relative;
  color: #fff;
  width: 48.7%;
  float: left;
}
.el-form-item{
  width:100%;
}
.line-box {
  position: relative;
  /* z-index: 99; */
  /* padding: 20px; */
  box-sizing: border-box;
  font-size: 14px;
  color:#fff;
}
:deep(.el-form-item__label){
  color: #fff;
  font-size: 14px;
  margin-left:10px;
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
      :deep(.el-table .el-table__body td.el-table-fixed-column--right.el-table__cell){
        background: #27598c  !important;
      }
      :deep(.el-table th.el-table-fixed-column--right.el-table__cell){
        background: #27598c  !important;
      }
      

.bottomTitle {
  z-index:1;
  position: absolute;
  top: 0;
  right: 0;
  width: 100%;
  height: 30px;
  padding: 0 20px;
  box-sizing: border-box;
  color: #95caff;
  font-size: 14px;
  display:flex;
  align-items:center;
    pointer-events: auto;
  
  .coord {
    span {
      padding: 0 10px;
    }
  }
}
.c-timeLine {
      width: 42%;
      height: 79px;
      padding: 15px 30px 1px 20px;
      position: absolute;
      bottom: 40px;
      left: 50.5%;
      transform: translateX(-50%);
      z-index: 9;
      pointer-events: auto;
      border-radius: 100px;
      border: 1px solid rgba(255, 153, 12, 0.39);
      background: rgba(41, 47, 66, 0.51);
    }
</style>