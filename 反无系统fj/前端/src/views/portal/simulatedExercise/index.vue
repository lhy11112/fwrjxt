<template>
  <div class="topCenter">
    <div class="buttons">
      <el-button type="primary" @click="setting">设置</el-button>
      <el-button  type="primary" @click="historyData">历史数据</el-button>
      <el-button v-if="btnFlag" type="primary" @click="flyKz(true)">开始</el-button>
      <el-button v-if="btnFlag" type="primary" @click="flyKz(false)">暂停</el-button>
      

      <el-dropdown v-if="btnFlag" @command="handleViewModeChange" popper-class="high-zindex-dropdown"  style="margin:0 10px;">
      <el-button type="primary">
        漫游视角<el-icon class="el-icon--right"><arrow-down /></el-icon>
      </el-button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="1">第一人称</el-dropdown-item>
          <el-dropdown-item command="3">第三人称</el-dropdown-item>
          <el-dropdown-item command="2">不锁定 (自由视角)</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>

      <el-button v-if="btnFlag" type="primary" @click="endTy">结束推演</el-button>
      <div style="margin-left:10px;">
      <span>
        轨迹颜色:
        <el-color-picker ref="brightColorRef" v-model="color" @change="gjysChange"/>
      </span>
      <span>
        轨迹宽度:
        <el-input-number class="gjkd" style="width:90px" :min="0" controls-position="right" v-model="gjWidth"  @change="gjysChange"></el-input-number>
      </span>
    </div>
    </div>
    <div class="bottomTitle">
    <div class="coord">
      <span>
        经度:
        {{ lng }}
      </span>
      <span>
        纬度:
        {{ lat }}
      </span>
      <span>
        速度:
        {{ speed=='NaN'?0:speed }}m/s
      </span>
      
    </div>
    
  </div>
  <div class="mapTools" v-if="!mapChange">
    <div  v-for="(item,index) in mbfxToolData" :key="index" style="margin-right:5px;">
      <div v-if="mbfxTool['mbfxFlag'+item]" style="display: flex;align-items: center;background: #253c8e;">
        目标分析{{item + 1}}
        <el-icon style="cursor:pointer" @click="mbfxMapClose(item,index)"><Close /></el-icon>
      </div>
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
      <!-- <div class="flex_box">
        <el-form-item label="计划开始:">
          <el-date-picker v-model="trailParam.jhks" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm:ss" style="width:100%" :placeholder="'请输入'" clearable></el-date-picker>
        </el-form-item>
      </div>
      <div class="flex_box">
        <el-form-item label="计划结束:">
          <el-date-picker v-model="trailParam.jhjs" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" format="YYYY-MM-DD HH:mm:ss" style="width:100%" :placeholder="'请输入'" clearable></el-date-picker>
        </el-form-item>
      </div> -->
      <!-- <div class="flex_box">
        <el-form-item label="推演时间:">
          <el-input-number style="width:42%" controls-position="right" v-model="trailParam.minutes"></el-input-number>分
          <el-input-number style="width:42%;margin-left:3px" controls-position="right" v-model="trailParam.seconds"></el-input-number>秒
        </el-form-item>
      </div> -->
       <div style="width:100%;margin-bottom:10px;float:left;">
        <el-radio-group v-model="type" style="margin-left: 10px;" @change="typeChange">
          <el-radio-button label="路径参数"></el-radio-button>
          <el-radio-button label="地图规划"></el-radio-button>
        </el-radio-group>
       </div>
      <div v-if="type =='路径参数'">
        <div class="title">
          <div>路线参数</div>
        </div>
        <div class="flex_box" style="width:100%">
          <el-form-item label="起点:">
            <el-input v-model="trailParam.uavDatectMsgDto1.start.longitude" style="width:22%" :placeholder="'请输入经度'" clearable></el-input>
            <el-input v-model="trailParam.uavDatectMsgDto1.start.latitude" style="width:22%;margin-left:calc(4% / 4)" :placeholder="'请输入纬度'" clearable></el-input>
            <el-input v-model="trailParam.uavDatectMsgDto1.start.altitude" @inut="altitudeChange" style="width:19%;margin-left:calc(4% / 4)" :placeholder="'请输入高度(m)'" clearable></el-input>(m)
            <el-input v-model="trailParam.uavDatectMsgDto1.start.stayTime" style="width:18%;margin-left:calc(4% / 4)" :placeholder="'停留时间(s)'" clearable></el-input>(s)
                    <el-icon title="地图选点" style="font-size: 24px;cursor: pointer;margin-left:10px" @click="route_input"><Location /></el-icon>
          </el-form-item>
        </div>
        
        <div class="flex_box" style="width:100%;flex-wrap:wrap;">
          <div class="title2" style="width:100%;margin-bottom:5px;">
            <span>
              途经点:
              <el-icon style="font-size: 24px;margin-right: 10px;cursor: pointer;vertical-align: middle;" @click="addWayPoints"><CirclePlus /></el-icon>
            </span>
          </div>
          <div style="width:100%">
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
                    <el-form-item label="高度(m)">
                      <el-input v-model="item.altitude" @inut="altitudeChange" placeholder="请输入"></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="5">
                    <el-form-item label="停留时间(s)">
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
        </div>

        <div class="flex_box" style="width:100%">
          <el-form-item label="终点:">
            <el-input v-model="trailParam.uavDatectMsgDto1.end.longitude" style="width:22%" :placeholder="'请输入经度'" clearable></el-input>
            <el-input v-model="trailParam.uavDatectMsgDto1.end.latitude" style="width:22%;margin-left:calc(4% / 4)" :placeholder="'请输入纬度'" clearable></el-input>
            <el-input v-model="trailParam.uavDatectMsgDto1.end.altitude" @inut="altitudeChange" style="width:19%;margin-left:calc(4% / 4)" :placeholder="'请输入高度(m)'" clearable></el-input>(m)
            <el-input v-model="trailParam.uavDatectMsgDto1.end.stayTime" style="width:18%;margin-left:calc(4% / 4)" :placeholder="'停留时间(s)'" clearable></el-input>(s)
                    <el-icon title="地图选点" style="font-size: 24px;cursor: pointer;margin-left:10px" @click="route_input_end"><Location /></el-icon>
          </el-form-item>
        </div>
        
      </div>
      <div v-else  class="flex_box" style="margin-bottom:10px;">
        <el-button type="primary" @click="mapClick">地图选点</el-button>
        <el-button type="primary" @click="deleteSyPoint">删除上一个点</el-button>
        <el-button type="primary" @click="deleteAllPoint">清除所有点</el-button>
        <el-button type="primary" @click="endPoint">保存路径</el-button>
      </div>

      <div class="title">
        <div>目标分析</div>
      </div>
       <!-- <div class="flex_box">
        <el-form-item label="速度(米/秒):">
          <el-input v-model="trailParam.uavDatectMsgDto1.speed" style="width:100%" :placeholder="'请输入'" clearable></el-input>
        </el-form-item>
      </div> -->
      <!--<div class="flex_box">
        <el-form-item label="最大加速度(米/秒²):">
          <el-input v-model="trailParam.uavDatectMsgDto1.acceleration" style="width:100%" :placeholder="'请输入'" clearable></el-input>
        </el-form-item>
      </div>
      <div class="flex_box">
        <el-form-item label="最小转弯半径(米):">
          <el-input v-model="trailParam.uavDatectMsgDto1.turnRadius" style="width:100%" :placeholder="'请输入'" clearable></el-input>
        </el-form-item>
      </div> -->
      <div class="flex_box">
        <el-form-item label="目标分析范围(km):">
          <el-input v-model="trailParam.mbfxfw" style="width:100%" :placeholder="'请输入'" clearable></el-input>
        </el-form-item>
      </div>
      <!-- <div class="flex_box">
        <el-form-item label="飞行预测时间(分钟):">
          <el-input v-model="trailParam.uavDatectMsgDto1.fxycSj" style="width:100%" :placeholder="'请输入'" clearable></el-input>
        </el-form-item>
      </div> -->

      <div>
        <div class="title" style="width:100%;">航段</div>
        <el-row v-for="(item, index) in trailParam.uavDatectMsgDto1.hdcs" :key="index">
          <el-col :span="11">
                    <el-form-item label="速度(m/s):">
                      <el-input v-model="item.sd" @input="sdInput(item)" :placeholder="'请输入速度(m/s)'" clearable></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="11">
                    <el-form-item label="时间(s):">
                      <el-input v-model="item.sj" style="margin-left:calc(4% / 4)" :placeholder="'时间(s)'" clearable></el-input>
                    </el-form-item>
                  </el-col>
         
          
          <!-- <el-input v-model="trailParam.uavDatectMsgDto1.end.altitude" style="width:22%;margin-left:calc(4% / 4)" :placeholder="'请输入高度'" clearable></el-input> -->
        </el-row>
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
                 ref="multipleTable"
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
                <el-table-column fixed="right" label="操作" width="160">
                  <template #default="scope">
                    <el-button
                      style="color: #fff"
                      
                      type="primary"
                      size="small"
                      @click.prevent="edit(scope.row)"
                    >
                      编辑
                    </el-button>
                    <el-button
                      style="color: #fff"
                     
                      type="primary"
                      size="small"
                      @click.prevent="getTyjhData(scope.row,0)"
                    >
                      开始推演
                    </el-button>
                    <!-- <el-button
                      style="color: #fff"
                      
                      type="primary"
                      size="small"
                      @click.prevent="getTyjhData(scope.row,1)"
                    >
                      结束推演
                    </el-button> -->
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

    <div>
      <el-button
        type="primary"
        class="sdsk-position"
        v-show="DSControl.isMinus"
        @click="setSDSKMinus"
        >知识库</el-button
      >
      <knowSeacher
        v-if="!DSControl.isMinus"
        @setTLGCDlgMinus="setTLGCDlgMinus"
      ></knowSeacher>
    </div>

    <div>
      <el-button
        type="primary"
        class="tb-position"
        v-show="TBControl.isMinus"
        @click="setTBSKMinus"
        >图表</el-button
      >
      <tbDialog
        v-if="!TBControl.isMinus"
        :mbtjxxData="mbtjxxData"
        @setTBDlgMinus="setTBDlgMinus"
      ></tbDialog>


      <div class="checkBoxs" style="position:absolute;left:37%;top:75px;pointer-events: auto;">
        <el-checkbox-group v-model="checkList" @change="checkChange">
        <el-checkbox v-for="(t,i) in checkBoxOptions" :key="i" :label="t.value">{{t.label}}</el-checkbox>
      </el-checkbox-group>
      </div>
    </div>
    <!-- <div class="c-timeLine" v-show="showTimeLine">
      <timeLine ref="timeLineRef" :showTimeLine="showTimeLine" :tyjhData="tyjhData" @handlezztimeChange="handlezztimeChange"></timeLine>
    </div> -->
    <historySaveDialog v-if="historySaveVisible" ref="historySaveRef" @closed="closedEvent" @successClick="successEvent"></historySaveDialog>

    <!-- 标绘页面 -->
    <DynamicPlotting2D ref="dynamicPlotting2DRef"></DynamicPlotting2D>
    <mbfxDetailDialog ref="mbfxDetailRef"></mbfxDetailDialog>
</template>

<script setup>
import knowSeacher from "@/components/know/knowSeacher.vue";
import tbDialog from "./tb.vue";
import mbfxDetailDialog from "../../dpCommon/mbfxDetail.vue"
// 模拟推演
import { useRouter } from "vue-router";
import microApp from "@/components/microApp/index.vue";
import { ref, onMounted,onUnmounted,nextTick,reactive } from "vue";
import icon1 from "@/assets/leftTitle/leftImg.png"
import wxdzcsb from "@/assets/allImage/wxdzcsb.png"
import {ElMessage} from "element-plus";
import { useMap3DStore } from "@/store/modules/map3D";
import { useMap2DStore } from "@/store/modules/map2D";
import { mapModeEnum, getCurrentMapMode } from "@/utils/Map/mapMode";
import historySaveDialog from "./historySave.vue"
import startmarker from "/public/static/startmarker.png"
import endmarker from "/public/static/endmarker.png"
import passmarker from "/public/static/passmarker.png"

import { useDynamicPlottingStore } from "@/store/modules/dynamicPlotting";
// 标绘组件
import DynamicPlotting2D from "@/components/MapTools/DynamicPlotting2D/index.vue";
 
// import timeLine from "@/components/timeline/index.vue";
const checkList = ref([])

const checkBoxOptions = ref([])
// 定义路由
const router = useRouter();
const type = ref("路径参数")
const settingVisible = ref(true)
const queryInfo = ref({
  isValid:1,
  name:""
})
const wxdsbData = ref([])
const trailParam = ref({
  uavDatectMsgDto1:{
    start:{},
    end:{},
    waypoints:[],
    hdcs:[{

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
const gjWidth = ref(3);
const color = ref("#ffff00");
// 筛选设置
const DSControl = reactive({
  isMinus: false,
});
const TBControl = reactive({
  isMinus: true,
});
let minTime ="";
let totalSpan = "";
let maxTime = "";

// 初始化
onMounted(()=>{
  console.log(trailParam.value.uavDatectMsgDto1.waypoints.length);
  getWrj()
  if(curMapMode === mapModeEnum["3D"]){
    mapChange.value = true
    type.value="地图规划"
  }else if(curMapMode === mapModeEnum["2D"]){
    mapChange.value = false
    type.value="路径参数"
  }
  
  nextTick(()=>{
    typeChange()

    eventBus.on("addClick",()=>{
      typeChange()
    })

    eventBus.on("mbfxTargetClick",(data)=>{
      mbfxDetail(data.targetData)
    })
    
    
    markerLayer = L.layerGroup([]);
    markerLayer.addTo(window.Map2D.map);
    lineLayer = L.layerGroup([]);
    lineLayer.addTo(window.Map2D.map);

    wrjLineMarkerLayer = L.layerGroup([]);
    wrjLineMarkerLayer.addTo(window.Map2D.map);

    wrjPointMarkerLayer = L.layerGroup([]);
    wrjPointMarkerLayer.addTo(window.Map2D.map);
  })
  
  window.eventBus.on("mapChange",function(e){
    mapChange.value = !mapChange.value;
    console.log(mapChange.value);
    dotStatus.value=""
    // if(mapChange.value){
    //   type.value="地图规划"
    // }
    speed.value = 0;
    lng.value = 0;
    lat.value = 0;
    window.eventBus.off("wrjData")
    
  window.eventBus.off("mbtjxxEvent")
  
    window.TOOL.data.remove('wrjData')
    clearInterval(timelineInterval);
    clearLayer1()
    clearLayer2()
    markerClear()
           wrjLineClearLayer()
    zbmbClearLayer()
    timelineInterval = null;
    const element = document.getElementById('timeline');
      console.log(element);
      if(element){
        element.remove()
      }
      if(mapChange.value && selectData.value && selectData.value.length){
        timelineInterval = null;
    if(timelineIntervalArr && timelineIntervalArr.length){
              timelineIntervalArr.forEach(item=>{
                clearInterval(item);
              })
            }
            clearLayerArr()
            clearAll()
        return
      }
    nextTick(()=>{
      
      setTimeout(()=>{
        addToMap()
      },100)
    })
  })

  window.Intelligence = Intelligence;

  eventBus.on("closePlot",()=>{
    dynamicPlottingStore.show = false;
    settingVisible.value = true;
    DSControl.isMinus = false;
  })
   
})
onUnmounted(()=>{
  window.eventBus.off("wrjData")
  window.eventBus.off("mbtjxxEvent")
   window.eventBus.off("addClick")
  window.eventBus.off("mbfxTargetClick")
  window.TOOL.data.remove('wrjData')
  window.TOOL.data.remove('mbfxfw')
    clearInterval(timelineInterval);
    clearLayer1()
    clearLayer2()

    timelineInterval = null;
    if(timelineIntervalArr && timelineIntervalArr.length){
              timelineIntervalArr.forEach(item=>{
                clearInterval(item);
              })
            }
            
  clearLayerArr()
    const element = document.getElementById('timeline');
      console.log(element);
      if(element){
        element.remove()
      }
  window.eventBus.off("mapChange")
  
  //清除三维推演
  window.Map3D.wrjFly.clearAllLayers()

  markerClear()
           wrjLineClearLayer()
           zbmbClearLayer()
           mbfxToolData.value = []
           checkBoxOptions.value = []
           checkFlag.value=true;
          mbtjxxData.value = []
  dynamicPlottingStore.show = false;
  window.Map2D.map.off('click');
  clearAll()
})

const checkChange = () => {
  zbmbClearLayer()
  let newArr = []
  checkList.value.forEach((item,index)=>{
    newArr = [...newArr,...zymbxx.value.filter(v=>v.subtype == item)]
    if(index == checkList.value.length-1){
      const waypoints = JSON.parse(rowData.value.jhcs).waypoints;
      mbfxToolData.value.forEach((item2,index2)=>{
        zbmbAddMap(newArr,waypoints[index2].longitude,waypoints[index2].latitude,index2)
      })
      
    }
  })
}

let pathPoint = null;
let pathPoints = [];
let handler = null;
const  drawIconPoint = (item) => {
  if (!window.Map3D.viewer) return;

  // 1. 清理旧的事件处理器（关键：避免重复监听）
  if (handler) {
    handler.removeInputAction(window.Cesium.ScreenSpaceEventType.LEFT_CLICK);
    handler.destroy(); // 销毁旧处理器
    handler = null;
  }

  // 2. 复用数据源，避免重复创建
  if (!pathPoint) {
    pathPoint = new window.Cesium.CustomDataSource("pathPoint");
    window.Map3D.viewer.dataSources.add(pathPoint);
    pathPoints.push(pathPoint);
  }
  // pathPoint = new window.Cesium.CustomDataSource("pathPoint");
  // window.Map3D.viewer.dataSources.add(pathPoint);
  // pathPoints.push(pathPoint)
  handler = new window.Cesium.ScreenSpaceEventHandler(window.Map3D.viewer.scene.canvas);
  handler.setInputAction((movement) => {
    let cartestian = window.Map3D.viewer.scene.pickPosition(movement.position);
    if (!cartestian) return;
    let cartographic = window.Cesium.Cartographic.fromCartesian(cartestian);
    console.log(cartographic);
    let lon = window.Cesium.Math.toDegrees(cartographic.longitude);
    let lat = window.Cesium.Math.toDegrees(cartographic.latitude);
    let alt = window.Cesium.Math.toDegrees(cartographic.height);

    if(dotStatus.value=="始发地"){
      trailParam.value.uavDatectMsgDto1.start.longitude = Number(lon.toFixed(8));
      trailParam.value.uavDatectMsgDto1.start.latitude = Number(lat.toFixed(8));
      // trailParam.value.uavDatectMsgDto1.start.altitude = alt.toFixed(8);
    }else if(dotStatus.value=="途经点"){
      trailParam.value.uavDatectMsgDto1.waypoints[tjdNum.value].longitude = Number(lon.toFixed(8));
      trailParam.value.uavDatectMsgDto1.waypoints[tjdNum.value].latitude = Number(lat.toFixed(8));
      // trailParam.value.uavDatectMsgDto1.waypoints[tjdNum.value].altitude = alt.toFixed(8);
    }else if(dotStatus.value=="目的地"){
      trailParam.value.uavDatectMsgDto1.end.longitude = Number(lon.toFixed(8));
      trailParam.value.uavDatectMsgDto1.end.latitude = Number(lat.toFixed(8));
      // trailParam.value.uavDatectMsgDto1.end.altitude = alt.toFixed(8);
    }
    console.log('xxxxxxxxxx')
    generalPurposeAdd(item, cartestian);
    addHdsj()
    
    
   
  }, window.Cesium.ScreenSpaceEventType.LEFT_CLICK);
}

const generalPurposeAdd = (item, cartestian) => {
  if (isEntity(item.id)) {
    pathPointRemoveId(item.id);
  }
  let imageUrl = "";
  if (dotStatus.value=="始发地") {
    imageUrl =startmarker;
  } else if (dotStatus.value=="途经点") {
    imageUrl =passmarker;
  } else if (dotStatus.value=="目的地") {
    imageUrl =endmarker;
  } 
  pathPoint.entities.add({
    id: item.id,
    position: cartestian,
    billboard: {
      image: imageUrl,
      width: 32,
      height: 32,
      verticalOrigin: window.Cesium.VerticalOrigin.BOTTOM,
      heightReference: window.Cesium.HeightReference.CLAMP_TO_GROUND,
    },
  });
  console.log(imageUrl, "drawIconPoint");
}

// 绘制路线
// const addHdsj = () => {
  
//       addPoints=[]
//       addPoints.push(trailParam.value.uavDatectMsgDto1.start)
//       addPoints.push(...trailParam.value.uavDatectMsgDto1.waypoints)
//       addPoints.push(trailParam.value.uavDatectMsgDto1.end)
//       addPoints = addPoints.filter(v=>v.latitude && v.longitude)
//       console.log(trailParam.value.uavDatectMsgDto1)
//       if (addPoints.length > 1) {
//         // if(!lineFlag){
//         //   polyline = L.polyline(addPoints.map(p => [p.latitude, p.longitude])).addTo(wrjLineMarkerLayer);
//         // }

//         // 计算每段距离并添加标注
//         for (let i = 0; i < addPoints.length - 1; i++) {
          
//             const p1 = addPoints[i];
//             const p2 = addPoints[i + 1];
//             const point1 = Cesium.Cartesian3.fromDegrees(Number(p1.longitude),Number(p1.latitude),Number(p1.altitude))
//             const point2 = Cesium.Cartesian3.fromDegrees(Number(p2.longitude),Number(p2.latitude),Number(p2.altitude))
//             console.log(point1,point2);
            
//             const distance = Cesium.Cartesian3.distance(point1, point2);
//             console.log('123',distance);
            
//             trailParam.value.uavDatectMsgDto1.hdcs[i].lc = distance;
//               if(trailParam.value.uavDatectMsgDto1.hdcs[i].sd){
//                 trailParam.value.uavDatectMsgDto1.hdcs[i].sj = parseInt(Number(trailParam.value.uavDatectMsgDto1.hdcs[i].lc / trailParam.value.uavDatectMsgDto1.hdcs[i].sd));
//               }
//         }
//       }
// }
const altitudeChange = () => {
  if(mapChange.value){
    addHdsj()
  }
}
const addHdsj = () => {
  // 1. 重新组装点集合
  let addPoints = [];
  if(trailParam.value?.uavDatectMsgDto1) {
      addPoints.push(trailParam.value.uavDatectMsgDto1.start);
      addPoints.push(...trailParam.value.uavDatectMsgDto1.waypoints);
      addPoints.push(trailParam.value.uavDatectMsgDto1.end);
  }
  
  // 2. 过滤掉没有经纬度的无效点
  addPoints = addPoints.filter(v => v.latitude && v.longitude);

  console.log("有效点位数量:", addPoints.length);

  // 3. 只有当点位大于1个时才能计算线段
  if (addPoints.length > 1) {
    // 遍历计算每一段的距离
    for (let i = 0; i < addPoints.length - 1; i++) {
      const p1 = addPoints[i];
      const p2 = addPoints[i + 1];

      // 确保高度存在，如果没有（因为被注释了），默认为 0，防止计算出错
      const h1 = p1.altitude || 0;
      const h2 = p2.altitude || 0;

      // 将经纬度转换为笛卡尔坐标
      const point1 = window.Cesium.Cartesian3.fromDegrees(Number(p1.longitude), Number(p1.latitude), Number(h1));
      const point2 = window.Cesium.Cartesian3.fromDegrees(Number(p2.longitude), Number(p2.latitude), Number(h2));

      // 【核心修改】使用 distance 方法计算两点间直线距离 (单位：米)
      const distanceMeters = window.Cesium.Cartesian3.distance(point1, point2);
      
      // 如果需要公里，可以除以 1000
      // const distanceKm = distanceMeters / 1000; 

      // 4. 更新数据模型
      // 确保 hdcs[i] 存在，防止报错
      if (trailParam.value.uavDatectMsgDto1.hdcs[i]) {
          // 这里假设你需要存储公里数，如果需要米数请用 distanceMeters
          trailParam.value.uavDatectMsgDto1.hdcs[i].lc = Number(distanceMeters.toFixed(2)); // 保留两位小数

          // 计算时间：如果有速度(sd)
          if (trailParam.value.uavDatectMsgDto1.hdcs[i].sd) {
              // 时间 = 距离 / 速度
              // 注意单位统一：如果距离是km，速度是km/h，结果是小时。
              // 原代码用了 parseInt，这里保持一致，或者根据需要改为 toFixed
              const timeHours = trailParam.value.uavDatectMsgDto1.hdcs[i].lc / trailParam.value.uavDatectMsgDto1.hdcs[i].sd;
              trailParam.value.uavDatectMsgDto1.hdcs[i].sj = parseInt(timeHours); 
          }
      }
    }
  }
  
  // 触发视图更新 (如果是 Vue3 响应式对象通常不需要，但如果是深层嵌套有时需要)
  // trailParam.value = { ...trailParam.value }; 
};

const isEntity = (id) => {
  console.log(pathPoint);
    if (pathPoint.entities.getById(id)) {
      return true;
    }
  
  return false;
}

const clearAll = () => {
  console.log(pathPoints);
  if(pathPoints && pathPoints.length){
    pathPoints.forEach(item=>{
      item.entities.removeAll();
    })
  }
  
  dotStatus.value=""
  // handler.removeInputAction(window.Cesium.ScreenSpaceEventType.LEFT_CLICK);
}

const pathPointRemoveId = (id) => {
  pathPoint.entities.remove(pathPoint.entities.getById(id));
}

const dynamicPlotting2DRef =ref(null);
const dynamicPlottingStore = useDynamicPlottingStore();
const Intelligence = () => {
  //关闭右键弹框
  window.Map2D.map.closePopup();

   // 标绘
  dynamicPlottingStore.show = !dynamicPlottingStore.show;
  if (dynamicPlottingStore.show == true) {
    settingVisible.value = false;
    window.Map2D.map.off("click");
    historyVisible.value = false;
    historySaveVisible.value = false;
    DSControl.isMinus = true;
  } else {
    settingVisible.value = true;
    DSControl.isMinus = false;
  }
};

/**** 设置深度思考弹窗最小化 */
const setTLGCDlgMinus = () => {
  DSControl.isMinus = true;
};
/**** 思维链弹窗最大最小化 */
const setSDSKMinus = () => {
  DSControl.isMinus = !DSControl.isMinus;
  TBControl.isMinus = true;
};

/**** 设置深度思考弹窗最小化 */
const setTBDlgMinus = () => {
  TBControl.isMinus = true;
};
/**** 思维链弹窗最大最小化 */
const setTBSKMinus = () => {
  TBControl.isMinus = !TBControl.isMinus;
  DSControl.isMinus = true;
};
const mapClickFlag = ref(false)
let points = [];
let markers = [];
let polyline = null;
let wrjLineMarkerLayer = null;
let wrjPointMarkerLayer = null;
 
const typeChange = () => {
  clearAll()
  wrjPointClearLayer()
  wrjLineClearLayer()
  markerClear()
  trailParam.value = {
    uavDatectMsgDto1:{
      start:{},
      end:{},
      waypoints:[],
      hdcs:[{

      }]
    }
  }
  if(type.value=="路径参数"){
    window.Map2D.map.off('click')
    window.Map2D.map.on('click', evt => {
      markerToMap(evt)
    })
  }else{
    window.Map2D.map.off('click')
    markers = []
    points = []
    window.Map2D.map.on('click', evt => {
      addPoint(evt)
    })
  }
  
}
const mapClick = () => {
  if(mapChange.value){
    ElMessage.info('请点击右上角转换2D模式！')
    return;
  }
  mapClickFlag.value = true;
  
}
 // 添加点
const addPoint = (e) => {
  if(!mapClickFlag.value){
    return
  }
   
            const latitude = e.latlng.lat;
            const longitude = e.latlng.lng;
            const point = { latitude, longitude, marker: null };
            
            // 添加标记到地图
            point.marker = L.marker([latitude, longitude]).addTo(wrjPointMarkerLayer);
            markers.push(point.marker);
            points.push({latitude:latitude, longitude:longitude})
           
            
            // 绘制路线
            drawPath();
            
          
}

const wrjLineClearLayer = () => {
  if (
      wrjLineMarkerLayer != undefined &&
      wrjLineMarkerLayer != null &&
      wrjLineMarkerLayer != ""
  ) {
    // 清空图层
    wrjLineMarkerLayer.clearLayers();
  }
}

const wrjPointClearLayer = () => {
  if (
      wrjPointMarkerLayer != undefined &&
      wrjPointMarkerLayer != null &&
      wrjPointMarkerLayer != ""
  ) {
    // 清空图层
    wrjPointMarkerLayer.clearLayers();
  }
}
        // 绘制路线
const drawPath =() =>{
  wrjLineClearLayer()
  if(points && points.length){
    points = points.filter(v=>v.latitude && v.longitude)
  }
  if (points.length > 1) {
    polyline = L.polyline(points.map(p => [p.latitude, p.longitude])).addTo(wrjLineMarkerLayer);
    trailParam.value.uavDatectMsgDto1.hdcs = [];
      
    // 计算每段距离并添加标注
        for (let i = 0; i < points.length - 1; i++) {
          trailParam.value.uavDatectMsgDto1.hdcs.push({
            sd:"",
            sj:"",
            lc:"",
          })
            const p1 = points[i];
            const p2 = points[i + 1];
            const coords = [[p1.longitude, p1.latitude], [p2.longitude, p2.latitude]];
            const distance = turf.distance(coords[0], coords[1]) * 1000;
            trailParam.value.uavDatectMsgDto1.hdcs[i].lc = distance;
              if(trailParam.value.uavDatectMsgDto1.hdcs[i].sd){
                trailParam.value.uavDatectMsgDto1.hdcs[i].sj = Number(trailParam.value.uavDatectMsgDto1.hdcs[i].lc / trailParam.value.uavDatectMsgDto1.hdcs[i].sd).toFixed(0)
              }
            
            // 计算中点
            const midpoint = {};
            // 计算中间点坐标
            midpoint.lat = Number(p1.latitude) + (Number(p2.latitude) - Number(p1.latitude)) / 2,
            midpoint.lng = Number(p1.longitude) + (Number(p2.longitude) - Number(p1.longitude)) / 2;
            console.log(midpoint);
            // 添加标注
            const  markerIcon = L.divIcon({
                html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>${distance} 米</div>`,//marker标注
                className: 'my-div-icon',
                iconAnchor: [80, 20]//文字标注相对位置
              });
          window.L.marker(
            window.L.latLng(Number(midpoint.lat), Number(midpoint.lng)),
            {
              icon: markerIcon,
            }
          ).addTo(wrjLineMarkerLayer);
        }
  }
}
const deleteSyPoint = () =>{
  if (points.length === 0) {
    ElMessage.info('没有可清除的点！')
    return;
  }
  points.pop();
  markers.pop().remove();
  drawPath();
}
const deleteAllPoint = () => {
  points = [];
  markers.forEach(marker => window.Map2D.map.removeLayer(marker));
  markers = [];
  trailParam.value.uavDatectMsgDto1.hdcs = []
  trailParam.value.uavDatectMsgDto1.hdcs.push({
            sd:"",
            sj:"",
            lc:"",
          })
  drawPath();
}

const savePointFlag = ref(false);
const endPoint = () =>{
  mapClickFlag.value = false;
  savePointFlag.value = true;
  console.log(points);
  if(points.length<2){
    ElMessage.info('请至少选择两个点！')
  }else if(points.length==2){
    trailParam.value.uavDatectMsgDto1.start.longitude = points[0].longitude.toFixed(8);
    trailParam.value.uavDatectMsgDto1.start.latitude = points[0].latitude.toFixed(8);
    trailParam.value.uavDatectMsgDto1.start.altitude = 10;
    trailParam.value.uavDatectMsgDto1.end.longitude = points[1].longitude.toFixed(8);
    trailParam.value.uavDatectMsgDto1.end.latitude = points[1].latitude.toFixed(8);
    trailParam.value.uavDatectMsgDto1.end.altitude = 10;
    ElMessage.success('保存成功')
  }else{
    trailParam.value.uavDatectMsgDto1.start.longitude = points[0].longitude.toFixed(8);
    trailParam.value.uavDatectMsgDto1.start.latitude = points[0].latitude.toFixed(8);
    trailParam.value.uavDatectMsgDto1.start.altitude = 10;
    trailParam.value.uavDatectMsgDto1.end.longitude = points[points.length-1].longitude.toFixed(8);
    trailParam.value.uavDatectMsgDto1.end.latitude = points[points.length-1].latitude.toFixed(8);
    trailParam.value.uavDatectMsgDto1.end.altitude = 10;
    let tjdArr = points.slice(1,points.length-1);
    console.log(tjdArr);
    tjdArr.forEach((item,index)=>{
      trailParam.value.uavDatectMsgDto1.waypoints.push({
          longitude: '',
          latitude: '',
          altitude: '',
          stayTime: ''
        })
      tjdNum.value = index;
      trailParam.value.uavDatectMsgDto1.waypoints[tjdNum.value].longitude = item.longitude.toFixed(8);
      trailParam.value.uavDatectMsgDto1.waypoints[tjdNum.value].latitude = item.latitude.toFixed(8);
      trailParam.value.uavDatectMsgDto1.waypoints[tjdNum.value].altitude = 10;
    })
    
    ElMessage.success('保存成功')
  }
}
let searchRouteTjLayer = null;
let searchRouteMddLayer = null;
let searchRouteTjdLayer = {};
//地图选点
const markerToMap = (evt) => {
  console.log(dotStatus.value);
  if(dotStatus.value == "始发地"){
      if(searchRouteTjLayer && searchRouteTjLayer!=null){
        // searchRouteTjLayer.clearLayers()
        window.Map2D.map.removeLayer(searchRouteTjLayer)
      }
      // 图标
      const searchRoute = {
        routeTj: window.L.icon({ iconSize: [32, 48], iconUrl: startmarker }),
      }
      searchRouteTjLayer = window.L.layerGroup([]).addTo(window.Map2D.map)
      const x = evt.latlng.lng
      const y = evt.latlng.lat
      trailParam.value.uavDatectMsgDto1.start.longitude = x.toFixed(8);
      trailParam.value.uavDatectMsgDto1.start.latitude = y.toFixed(8);

      const marketMarker = window.L.marker([y, x], { icon: searchRoute.routeTj })
      marketMarker.addTo(searchRouteTjLayer)

    }else if(dotStatus.value == "目的地"){
      if(searchRouteMddLayer && searchRouteMddLayer!=null){
        // searchRouteMddLayer.clearLayers()
        window.Map2D.map.removeLayer(searchRouteMddLayer)
      }
      // 图标
      const searchRoute = {
        routeTj: window.L.icon({ iconSize: [32, 48], iconUrl: endmarker }),
      }
      searchRouteMddLayer = window.L.layerGroup([]).addTo(window.Map2D.map)
      const x = evt.latlng.lng
      const y = evt.latlng.lat
      trailParam.value.uavDatectMsgDto1.end.longitude = x;
      trailParam.value.uavDatectMsgDto1.end.latitude = y;

      const marketMarker = window.L.marker([y, x], { icon: searchRoute.routeTj })
      marketMarker.addTo(searchRouteMddLayer)
    }else if(dotStatus.value == "途经点"){
      if(searchRouteTjdLayer['tjdLayer'+tjdNum.value] && searchRouteTjdLayer['tjdLayer'+tjdNum.value]!=null){
        // searchRouteTjdLayer['tjdLayer'+tjdNum.value].clearLayers()
        window.Map2D.map.removeLayer(searchRouteTjdLayer['tjdLayer'+tjdNum.value])
      }
      // 图标
      const searchRoute = {
        routeTj: window.L.icon({ iconSize: [32, 48], iconUrl: passmarker,data:tjdNum.value }),
      }
      searchRouteTjdLayer['tjdLayer'+tjdNum.value] = window.L.layerGroup([]).addTo(window.Map2D.map)
      const x = evt.latlng.lng
      const y = evt.latlng.lat
      trailParam.value.uavDatectMsgDto1.waypoints[tjdNum.value].longitude = x;
      trailParam.value.uavDatectMsgDto1.waypoints[tjdNum.value].latitude = y;
      const marketMarker = {}
      marketMarker['marker'+tjdNum.value]= window.L.marker([y, x], { icon: searchRoute.routeTj })
      marketMarker['marker'+tjdNum.value].addTo(searchRouteTjdLayer['tjdLayer'+tjdNum.value])
    }
    addDrawPath()
}

const sdInput = (data) => {
  console.log(data);
  if(data.lc && data.sd){
    data.sj = parseInt(Number(data.lc / data.sd));
  }else{
    data.sj = ''
  }
}

let addPoints = [];
const mbfxToolData = ref([])
// 绘制路线
const addDrawPath = (lineFlag) => {
      markerClear()
      wrjLineClearLayer()
      addPoints=[]
      addPoints.push(trailParam.value.uavDatectMsgDto1.start)
      addPoints.push(...trailParam.value.uavDatectMsgDto1.waypoints)
      addPoints.push(trailParam.value.uavDatectMsgDto1.end)
      addPoints = addPoints.filter(v=>v.latitude && v.longitude)
      if (addPoints.length > 1) {
        if(!lineFlag){
          polyline = L.polyline(addPoints.map(p => [p.latitude, p.longitude])).addTo(wrjLineMarkerLayer);
        }

        // 计算每段距离并添加标注
        for (let i = 0; i < addPoints.length - 1; i++) {
            const p1 = addPoints[i];
            const p2 = addPoints[i + 1];
            const coords = [[p1.longitude, p1.latitude], [p2.longitude, p2.latitude]];
            const distance = turf.distance(coords[0], coords[1]) * 1000;
            if(!lineFlag){
              trailParam.value.uavDatectMsgDto1.hdcs[i].lc = distance;
              if(trailParam.value.uavDatectMsgDto1.hdcs[i].sd){
                trailParam.value.uavDatectMsgDto1.hdcs[i].sj = parseInt(Number(trailParam.value.uavDatectMsgDto1.hdcs[i].lc / trailParam.value.uavDatectMsgDto1.hdcs[i].sd));
              }

            }
            
            // 计算中点
            const midpoint = {};
            // 计算中间点坐标
            midpoint.lat = Number(p1.latitude) + (Number(p2.latitude) - Number(p1.latitude)) / 2,
            midpoint.lng = Number(p1.longitude) + (Number(p2.longitude) - Number(p1.longitude)) / 2;
            console.log(midpoint);
            // 添加标注
            const  markerIcon = L.divIcon({
                html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>${distance} 米</div>`,//marker标注
                className: 'my-div-icon',
                iconAnchor: [80, 20]//文字标注相对位置
              });
          window.L.marker(
            window.L.latLng(Number(midpoint.lat), Number(midpoint.lng)),
            {
              icon: markerIcon,
            }
          ).addTo(wrjLineMarkerLayer);
        }
      }
      
      addPointEvent()
}
const mbfxFlag = ref(true)
const mbtjxxData = ref([])
const zymbxx = ref([])
const zbmbJd = ref('')
const zbmbWd = ref('')
const checkFlag = ref(true)
const getMbfx = (jd,wd,index) => {
  zbmbJd.value = jd;
  zbmbWd.value = wd;
  window.API.zbmb.getWrjZymbByJwdAndJlS({
          jd:jd,
          wd:wd,
          jl:rowData.value.mbfxfw && rowData.value.mbfxfw!=undefined?rowData.value.mbfxfw:30,
          type:'执勤目标,民生目标,友邻信息'
        }).then((res) => {
          if (res.code == 200) {
            let data = res.result.zymbxx;
            zymbxx.value = res.result.zymbxx;
            if(checkFlag.value){
              checkList.value = res.result.mbtjxx.filter(v => v.type).map(v => v.type);
              checkFlag.value=false;
            }
              
            
            console.log(checkList.value);
            checkBoxOptions.value=[]
            res.result.mbtjxx.forEach(item=>{
              if(item.type){
                let obj={}
                obj.label = item.type;
                obj.value = item.type;
                checkBoxOptions.value.push(obj)
              }
            })
            console.log(checkBoxOptions.value);
            
            let newArr = []
            checkList.value.forEach((item,indexs)=>{
              newArr = [...newArr,...zymbxx.value.filter(v=>v.subtype == item)]

              console.log(newArr);
              if(indexs == checkList.value.length-1){
                zbmbAddMap(newArr,jd,wd,index)
              }
            })
            mbtjxxData.value.push(res.result)
          }
        });
}

let mbfx2dMarkerLayer = {};
const zbmbIcon = ref(null);
const mbfxTool = ref({});
const zbmbAddMap = (data,jd,wd,index) => {
  console.log(data,index);
 mbfxTool.value["mbfxFlag"+index] = true;
    mbfx2dMarkerLayer["mbfxLayer"+index] = window.L.layerGroup([]);
  mbfx2dMarkerLayer["mbfxLayer"+index].addTo(window.Map2D.map);


const center = [Number(wd), Number(jd)];
    const jl = rowData.value.mbfxfw && rowData.value.mbfxfw!=undefined?rowData.value.mbfxfw:30
      const radius =jl * 1000; // 圆的半径
      const bound = getCriclePoints(center, radius);
      const circleMarker = window.L.polygon(bound, { color: "#ef0303" }).addTo(mbfx2dMarkerLayer["mbfxLayer"+index]);
      window.L.marker(window.L.latLng(Number(wd), Number(jd))).addTo(mbfx2dMarkerLayer["mbfxLayer"+index]);
 
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
      ).addTo(mbfx2dMarkerLayer["mbfxLayer"+index]);
      // const innerHTML = "名称: " + item.MC + "<br>";
      // innerHTML += "经度: " + item.JD + "<br>";
      // innerHTML += "纬度: " + item.WD + "<br>";
      const html = `<div style="width:140px;background:rgba(30, 32, 44);padding:10px">
                  <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                    <div style="width:100%;margin: 4px 0;color:#fff;">名称：<span style="color:#fff;">${item.mc || item.dmmc}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">经度：<span style="color:#fff;">${item.jd}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">纬度：<span style="color:#fff;">${item.wd}</span></div>
                  </div>
                </div>`;
      marker
        // bindTooltip
        .bindPopup(item.mc || item.dmmc)
        .bindTooltip(html)
        // .openPopup(marker.getLatLng());
      marker.on("click", function (e) {
        mbfxDetail(item);
      });

      // // 使用turf.js计算距离
      const distance = turf.distance([Number(item.wd), Number(item.jd)], [wd, jd]);
      // console.log(distance);
      
      const polyLine = L.polyline([[Number(item.wd), Number(item.jd)], [Number(wd), Number(jd)]], {color: "red"}).addTo(mbfx2dMarkerLayer["mbfxLayer"+index]);
      // 计算中间点坐标
        const midpointWd = Number(item.wd) + (Number(wd) - Number(item.wd)) / 2,
            midpointJd = Number(item.jd) + (Number(jd) - Number(item.jd)) / 2;
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
      ).addTo(mbfx2dMarkerLayer["mbfxLayer"+index]);
    }
  });

    
}

const mbfxDetailRef = ref(null)
//无人机详细数据
const mbfxDetail = (data) => {
  nextTick(()=>{
    mbfxDetailRef.value.open(data)
  })
}

const mbfxMapClose = (item,index) =>{
  console.log(item);
  mbfxTool.value["mbfxFlag"+item] = false;
  zbmbClearLayer(item)
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

// 清除图层
const zbmbClearLayer = (index) => {
  if(index || index==0){
    if (
      mbfx2dMarkerLayer["mbfxLayer"+index] != undefined &&
      mbfx2dMarkerLayer["mbfxLayer"+index] != null &&
      mbfx2dMarkerLayer["mbfxLayer"+index] != ""
    ) {
      window.Map2D.map.removeLayer(mbfx2dMarkerLayer["mbfxLayer"+index])
    }
  }else{
    for (const key in mbfx2dMarkerLayer) {
      if (mbfx2dMarkerLayer[key] != null) {
        window.Map2D.map.removeLayer(mbfx2dMarkerLayer[key])
      }
    }
    
              
  }
  
};

const marketMarker3 = {}
// 添加点
const addPointEvent = () => {
      if(searchRouteTjLayer && searchRouteTjLayer!=null){
        // searchRouteTjLayer.clearLayers()
        window.Map2D.map.removeLayer(searchRouteTjLayer)
      }
      // 图标
      const searchRoute = {
        routeTj: window.L.icon({ iconSize: [32, 48], iconUrl: startmarker }),
      }
      searchRouteTjLayer = window.L.layerGroup([]).addTo(window.Map2D.map)

      const marketMarker = window.L.marker([trailParam.value.uavDatectMsgDto1.start.latitude, trailParam.value.uavDatectMsgDto1.start.longitude], { icon: searchRoute.routeTj })
      marketMarker.addTo(searchRouteTjLayer)
                
      if(searchRouteMddLayer && searchRouteMddLayer!=null){
        // searchRouteMddLayer.clearLayers()
        window.Map2D.map.removeLayer(searchRouteMddLayer)
      }
      // 图标
      const searchRoute2 = {
        routeTj: window.L.icon({ iconSize: [32, 48], iconUrl: endmarker }),
      }
      searchRouteMddLayer = window.L.layerGroup([]).addTo(window.Map2D.map)

      const marketMarker2 = window.L.marker([trailParam.value.uavDatectMsgDto1.end.latitude, trailParam.value.uavDatectMsgDto1.end.longitude], { icon: searchRoute2.routeTj })
      marketMarker2.addTo(searchRouteMddLayer)

      
      trailParam.value.uavDatectMsgDto1.waypoints.forEach((item,index)=>{
        if(searchRouteTjdLayer['tjdLayer'+index] && searchRouteTjdLayer['tjdLayer'+index]!=null){
          // searchRouteTjdLayer['tjdLayer'+this.tjdNum].clearLayers()
          window.Map2D.map.removeLayer(searchRouteTjdLayer['tjdLayer'+index])
        }
        // 图标
        const searchRoute3 = {
          routeTj: window.L.icon({ iconSize: [32, 48], iconUrl: passmarker,data:index}),
        }
        searchRouteTjdLayer['tjdLayer'+index] = window.L.layerGroup([]).addTo(window.Map2D.map)
      
        
        
        marketMarker3['marker'+index]= window.L.marker([trailParam.value.uavDatectMsgDto1.waypoints[index].latitude,trailParam.value.uavDatectMsgDto1.waypoints[index].longitude], { icon: searchRoute3.routeTj })
        marketMarker3['marker'+index].addTo(searchRouteTjdLayer['tjdLayer'+index])
      })
      
              
    }
const markerClear = () => {
    if(searchRouteTjLayer && searchRouteTjLayer!=null){
        // searchRouteTjLayer.clearLayers()
        window.Map2D.map.removeLayer(searchRouteTjLayer)
      }
    if(searchRouteMddLayer && searchRouteMddLayer!=null){
        // searchRouteMddLayer.clearLayers()
        window.Map2D.map.removeLayer(searchRouteMddLayer)
      }

      for (const key in searchRouteTjdLayer) {
        if (searchRouteTjdLayer[key] != null) {
          window.Map2D.map.removeLayer(searchRouteTjdLayer[key])
        }
      }
}
const dotStatus = ref(null)
const tjdNum = ref(0)
const route_input_tjd = (i) =>{
  tjdNum.value=i;
  dotStatus.value = "途经点"
  console.log(dotStatus.value);
  window.Map2D.map.off('click')
    window.Map2D.map.on('click', evt => {
      markerToMap(evt)
    })
    drawIconPoint({id:"tjd"+i})
}
const route_input = () => {
  dotStatus.value = "始发地"
  window.Map2D.map.off('click')
    window.Map2D.map.on('click', evt => {
      markerToMap(evt)
    })
    drawIconPoint({id:'sfd'+1})
}

const route_input_end = () => {
  dotStatus.value = "目的地"
  window.Map2D.map.off('click')
    window.Map2D.map.on('click', evt => {
      markerToMap(evt)
    })
    drawIconPoint({id:"mdd"+1})
}



const settingClose = () => {
  settingVisible.value = false;
  window.Map2D.map.off("click");
}

const endFlag = ref(false);
// const timeLineRef = ref(null);
//开始 暂停
const flyKz = (data) => {
  if(mapChange.value){
      window.Map3D.wrjFly.togglePlayPause(data)
  }else{
      if (data) {
        if(selectData.value && selectData.value.length){
          if(timelineIntervalArr && timelineIntervalArr.length){
              timelineIntervalArr.forEach(item=>{
                clearInterval(item);
              })
            }
          selectData.value.forEach((item,droneIndex)=>{
            item.serialNumber = item.serial;
            item.color = color.value;
            item.weight = gjWidth.value;
            timelineIntervalArr[droneIndex] = setInterval(function () {
              currentTimeArr[droneIndex] = (currentTimeArr[droneIndex] + 1) % wrjArr.value[droneIndex].length;
              updateTimelines(droneIndex, currentTimeArr[droneIndex]);
              handleDroneFlight(wrjArr.value[droneIndex], droneIndex,item);
            }, 1000);
          })

          return
        }
        clearInterval(timelineInterval);
        timelineInterval = null;
        timelineInterval = setInterval(function() {
          if(endFlag.value){
            zbmbClearLayer()
            endFlag.value = false;
            mbfxToolData.value = []
            checkBoxOptions.value = []
            checkFlag.value=true;
            
          mbtjxxData.value = []
          }
          currentTime = (currentTime + 1) % tyjhData.value.length;
          updateTimeline(currentTime);
          clearLayer1()
          console.log(tyjhData.value[currentTime]);
          planeMarkers[currentTime].setLatLng([tyjhData.value[currentTime].dronLat, tyjhData.value[currentTime].dronLng]).addTo(markerLayer);
          const  markerIcon = L.divIcon({
                html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>${rowData.value.brand || '未知'}-${rowData.value.model || '未知'}(${rowData.value.serialNumber || '未知'})</div>`,//marker标注
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
                speed.value = tyjhData.value[currentTime].sd //(distance * 1000 / fxTime).toFixed(8)
                lat.value = tyjhData.value[currentTime].dronLat
                lng.value = tyjhData.value[currentTime].dronLng
                window.TOOL.data.set('wrjData',{longitude:Number(tyjhData.value[currentTime].dronLng),latitude:Number(tyjhData.value[currentTime].dronLat),altitude:0,speed:speed.value})
                addIcon(currentTime,minTime,totalSpan)
               
               
                const waypoints = JSON.parse(rowData.value.jhcs).waypoints;
                for(let i = 0;i<waypoints.length;i++){
                if(waypoints[i].longitude == tyjhData.value[currentTime].dronLng && waypoints[i].latitude == tyjhData.value[currentTime].dronLat){
                    zbmbClearLayer()
                    mbfxToolData.value=[]
                    if(mbfxToolData.value.indexOf(i)==-1){
                      mbfxToolData.value.push(i);
                      getMbfx(waypoints[i].longitude,waypoints[i].latitude,i);
                    }
                    
                  }
              }
              if(new Date(tyjhData.value[currentTime].dataTime).getTime() == new Date(tyjhData.value[tyjhData.value.length-1].dataTime).getTime()){
                
                endFlag.value = true;
              }
        }, 1000);
        // timeLineRef.value.autoPlayTimer(1)
      } else {
        clearInterval(timelineInterval);
        timelineInterval = null;
        // timeLineRef.value.autoPlayTimer(2)
        if(timelineIntervalArr && timelineIntervalArr.length){
              timelineIntervalArr.forEach(item=>{
                clearInterval(item);
              })
            }
      }
  }
   
  
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
// 处理下拉菜单选择
const  handleViewModeChange = (command)  => {
    // command 的值是字符串，setSj 可能需要数字，建议转换一下
    const mode = Number(command);
    
    console.log(`切换视角模式为：${mode}`);
    
    window.Map3D.wrjFly.setSj(mode)
  }

//结束推演
const endTy = () => {
  map2dStore.changeWidthAndHeight("100%", "100%"); // 展示二维地图
  map3dStore.changeWidthAndHeight("0", "0"); // 隐藏三维地图
  mapChange.value = false;
  window.eventBus.emit("isMap2dEvent",true);
  
  clearInterval(timelineInterval);
  clearLayer1()
    clearLayer2()
  timelineInterval = null;
  window.Map3D.wrjFly.clearAllLayers()
  tyjhData.value = []
  const element = document.getElementById('timeline');
      console.log(element);
      if(element){
        element.remove()
      }
  window.eventBus.off("wrjData")
  window.eventBus.off("mbtjxxEvent")
          window.TOOL.data.remove('wrjData')
          speed.value = 0;
          lng.value = 0;
          lat.value = 0;
          btnFlag.value = false;
           markerClear()
           wrjLineClearLayer()
           zbmbClearLayer()
           clearAllIcon();
           mbfxToolData.value = []
           checkBoxOptions.value = []
           checkFlag.value=true;
           getTyjhData(rowData.value,1)
          mbtjxxData.value = []
            for (var i = 0; i < Map2D.map.getPlottingLayers().length; i++) {
              Map2D.map.getPlottingLayers()[i].removeAllFeatures();
            }
            
  window.Map2D.setEwtyFlag(false)

  if(timelineIntervalArr && timelineIntervalArr.length){
              timelineIntervalArr.forEach(item=>{
                clearInterval(item);
              })
            }
  clearLayerArr()
  multipleTable.value.clearSelection();
            
}
//设置事件
const setting = () =>{
  settingVisible.value = !settingVisible.value;
   // 标绘
  dynamicPlottingStore.show = false;
  historyVisible.value = false;
  trailParam.value = {};

  trailParam.value.uavDatectMsgDto1 = {
      start:{},
      end:{},
      waypoints:[],
      hdcs:[{

      }]
    }

    window.Map2D.map.on('click', evt => {
      console.log(evt);
      markerToMap(evt)
      addPoint(evt)
    })
}
//添加途经点
const addWayPoints = () =>{
      trailParam.value.uavDatectMsgDto1.waypoints.push({
        longitude: '',
        latitude: '',
        altitude: '',
        stayTime: ''
      })
      
      trailParam.value.uavDatectMsgDto1.hdcs = [];
      for(let i =0;i<=trailParam.value.uavDatectMsgDto1.waypoints.length;i++){
        trailParam.value.uavDatectMsgDto1.hdcs.push({
          sd:"",
          sj:"",
          lc:"",
        })
      }
      
    }
    //删除途经点
const delWayPoints = (index) =>{
  // if(trailParam.value.uavDatectMsgDto1.waypoints.length==1){
  //   // ElMessage({
  //   //       type: "info",
  //   //       message: "至少保留一个",
  //   //     });
  //   if(searchRouteTjdLayer['tjdLayer'+index] && searchRouteTjdLayer['tjdLayer'+index]!=null){
  //     console.log(index);
  //     window.Map2D.map.removeLayer(searchRouteTjdLayer['tjdLayer'+index])
  //   }
  //   trailParam.value.uavDatectMsgDto1.waypoints.splice(index,1)
  //       tjdNum.value = 0;
  //       addWayPoints()
  //       return
  // }
  
  if(searchRouteTjdLayer['tjdLayer'+index] && searchRouteTjdLayer['tjdLayer'+index]!=null){
    console.log(index);
    window.Map2D.map.removeLayer(searchRouteTjdLayer['tjdLayer'+index])
  }
     trailParam.value.uavDatectMsgDto1.waypoints.splice(index,1)
      // trailParam.value.uavDatectMsgDto1.hdcs.splice(index,1)
      trailParam.value.uavDatectMsgDto1.hdcs = [];
      for(let i =0;i<=trailParam.value.uavDatectMsgDto1.waypoints.length;i++){
        trailParam.value.uavDatectMsgDto1.hdcs.push({
          sd:"",
          sj:"",
          lc:"",
        })
      }
     tjdNum.value = index -1<0 ?0 : index -1;
     addDrawPath()
      // const pointId = "tjd"+index;
  
      // if (window.Map3D && window.Map3D.viewer) {
      //   // 检查该实体是否存在，存在则移除
      //   if (window.Map3D.viewer.entities.getById(pointId)) {
      //     console.log(window.Map3D.viewer.entities.getById(pointId));
          
      //     window.Map3D.viewer.entities.removeById(pointId);
      //   }
      // }

      // addHdsj();
}
//开始推演
const startTy = () => {
  if(type.value == "地图规划" && !savePointFlag.value){
    ElMessage({
      type:"info",
      message:"请先保存路径"
    })
    return
  }
// trailParam.value.jhks = window.TOOL.dateFormat(new Date(trailParam.value.rq),"yyyy-MM-dd")+' '+window.TOOL.dateFormat(new Date(),"hh:mm:ss");
// trailParam.value.jhjs = window.TOOL.dateFormat(new Date(new Date(trailParam.value.jhks).getTime() + trailParam.value.minutes * 60 * 1000 + trailParam.value.seconds *1000),"yyyy-MM-dd hh:mm:ss");
trailParam.value.uavDatectMsgDto1.model = trailParam.value.model;
trailParam.value.uavDatectMsgDto1.serial = trailParam.value.serial;
trailParam.value.uavDatectMsgDto1.rq = trailParam.value.rq;
trailParam.value.uavDatectMsgDto1.stationId = trailParam.value.stationId;
if(trailParam.value.uavDatectMsgDto1.hdcs && trailParam.value.uavDatectMsgDto1.hdcs.length){
  trailParam.value.uavDatectMsgDto1.segmentSpeeds =trailParam.value.uavDatectMsgDto1.hdcs.map(v=>v.sd);
}
console.log(trailParam.value);
  window.API.mnty.TYJHSC(trailParam.value).then(res=>{
    console.log(res);
    if(res.success){
      ElMessage.success('推演成功')
      deleteAllPoint()
      markerClear()
      wrjLineClearLayer()
           zbmbClearLayer()
      settingVisible.value = false;
      window.Map2D.map.off("click");
      getTyjhData({id:res.result,mbfxfw:trailParam.value.mbfxfw,authStatus:trailParam.value.authStatus,brand:trailParam.value.brand,model:trailParam.value.model,serial:trailParam.value.serial,jhcs:JSON.stringify(trailParam.value.uavDatectMsgDto1)},0,true);
    }else{
      ElMessage.error(res.message)
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
  window.Map2D.map.off("click");
  dynamicPlottingStore.show = false;
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
    pageSize:100000
  }).then(res=>{
    if(res.success){
      wrjData.value = res.result.records;
    }
  })
}

const wrjChange = (e) =>{
  let values = wrjData.value.filter(v=>v.serialNumber==e);
  console.log(values[0]);
  if(values && values.length){
    trailParam.value.model = values[0].model;
    trailParam.value.serial = values[0].serialNumber;
    trailParam.value.brand = values[0].brand;
    trailParam.value.stationId = values[0].stationId;
    trailParam.value.authStatus = values[0].authStatus;
  }
}

const historySaveVisible = ref(false)
const historySaveRef = ref(null)
const edit = (row) => {
  console.log(row);
      historySaveVisible.value = true;
      nextTick(() => {
        historySaveRef.value.open('edit')
        historySaveRef.value.setData(row)
      })
}

const closedEvent = () =>{
  historySaveVisible.value = false;
}
const successEvent = () => {
  historySaveVisible.value = false;
  getData()
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
console.log(curMapMode);
const speed = ref(0)
const lat = ref(0)
const lng = ref(0)
const getTyjhData = (row,zt,flag) => {
  console.log(row);
  rowData.value = row;
  rowData.value.serialNumber = row.serial;
  rowData.value.color = color.value;
  rowData.value.weight = gjWidth.value;
  btnFlag.value = zt==1?false:true;
  currentTime = 0;
  mbfxToolData.value = []
  mbtjxxData.value = []
  checkBoxOptions.value = []
  checkFlag.value=true;
  // window.Map3D.viewer.on("click",function(e){
  //   console.log(e);
  // })
  // if(!flag){
  //   window.TOOL.data.remove('startTyParams')
  // }
  

  window.TOOL.data.set("mbfxfw",trailParam.value.mbfxfw)
  window.TOOL.data.set('TYJHID',row.id);
  getTYJHCZ({id:row.id,zt:zt})
  const element = document.getElementById('timeline');
  console.log(element);
  if(element){
    element.remove()
  }
  clearLayer1()
  clearLayer2()
  markerClear()
           wrjLineClearLayer()
           zbmbClearLayer()
           speed.value = 0;
          lng.value = 0;
          lat.value = 0;
  clearInterval(timelineInterval);
  timelineInterval = null;
  window.Map3D.wrjFly.clearAllLayers()
  window.API.wrjtyjhgj.list({
    pageNo:1,
    pageSize:10000000,
    tyjhId:row.id
  }).then(res=>{
    if(res.success){
      tyjhData.value = res.result.records;
      if(zt!=1){
        addToMap()
      }else{
        tyjhData.value = []
      }
      ElMessage.success('操作成功')
      getData()
    }
  })
}

const selectData = ref([])

// 初始化辅助数组
const currentTimeArr = []; // 存储每架无人机的当前时间
const timelineIntervalArr = []; // 存储每架无人机的时间轴间隔
// const timelinePoints = []; // 存储每架无人机的时间点
const minTimes = []; // 存储每架无人机的最小时间
// const maxTime = []; // 存储每架无人机的最大时间
const totalSpans = []; // 存储每架无人机的时间跨度
const planeMarkerArr = []; // 存储每架无人机的标记
const selectionChange = (e) => {
  wrjArr.value = []
    if(timelineIntervalArr && timelineIntervalArr.length){
              timelineIntervalArr.forEach(item=>{
                clearInterval(item);
              })
            }
            
            clearAll()
            clearLayerArr()
            clearLayer2()
  selectData.value = e;
  selectData.value.forEach(item=>{
    getTyjhDatas(item,0)
  })
}
const wrjArr = ref([])
const getTyjhDatas = (row, zt, flag) => {
  console.log(row);
  let rowData = row;
  rowData.serialNumber = row.serial;
  rowData.color = color.value;
  rowData.weight = gjWidth.value;
  btnFlag.value = zt === 1 ? false : true;
  currentTime = 0;

  window.TOOL.data.set("mbfxfw", row.mbfxfw);
  window.TOOL.data.set('TYJHID', row.id);
  getTYJHCZ({ id: row.id, zt: zt });

  window.API.wrjtyjhgj.list({
    pageNo: 1,
    pageSize: 10000000,
    tyjhId: row.id
  }).then(res => {
    if (res.success) {
      let data = res.result.records;
      wrjArr.value.push(data)
      if (zt !== 1) {
        addToMaps(data, rowData, selectData.value.indexOf(row)); // 传递无人机索引
      }
    }
  });
}
const addToMaps = (data, rowData, droneIndex) => {
  console.log('xxx', mapChange.value, data);
  if (data && data.length) {
    if (mapChange.value) {
      console.log(mapChange.value);
    } else {
      // window.eventBus.off("wrjData");
      // window.TOOL.data.remove('wrjData');
      speed.value = 0;
      lng.value = 0;
      lat.value = 0;
      Map2D.setEwtyFlag(true);

      // // 创建时间轴容器
      // timeline.value = document.createElement('div');
      // timeline.value.id = `timeline-${droneIndex}`;
      // const largeContent = document.getElementsByClassName('largeContent')[0];
      // largeContent.appendChild(timeline.value);

      // // 创建时间线元素
      // const timelineElement = document.createElement('div');
      // timelineElement.className = 'timeline';
      // timeline.value.appendChild(timelineElement);

      // // 添加播放控制
      // const controlDiv = document.createElement('div');
      // controlDiv.className = 'play-control';
      // timeline.value.appendChild(controlDiv);

      // const speedSelect = document.createElement('select');
      // speedSelect.id = `speedSelect-${droneIndex}`;
      // speedSelect.innerHTML = `
      //   <option value="0.5">0.5x</option>
      //   <option value="1">1x</option>
      //   <option value="2">2x</option>
      //   <option value="4">4x</option>
      // `;
      // speedSelect.selectedIndex = 1;
      // controlDiv.appendChild(speedSelect);

      // document.getElementById(`speedSelect-${droneIndex}`).addEventListener('change', function (e) {
      //   const speedMultiplier = parseFloat(this.value);
      //   clearInterval(timelineInterval[droneIndex]);
      //   timelineInterval[droneIndex] = setInterval(function () {
      //     // 更新当前无人机的时间和状态
      //     updateTimelines(droneIndex, (currentTime[droneIndex] + 1) % data.length);
      //     // 处理无人机的飞行状态更新
      //     handleDroneFlight(data, droneIndex,rowData);
      //   }, 1000 / speedMultiplier);
      //   e.stopPropagation();
      // });

      // // 创建时间线元素和刻度
      // const iconElement = document.createElement('div');
      // iconElement.className = 'c-icon-container';
      // timelineElement.appendChild(iconElement);

      // const timelineLine = document.createElement('div');
      // timelineLine.className = 'timeline-line';
      // timelineElement.appendChild(timelineLine);

      // timelinePoints[droneIndex] = data.map(function (item, index) {
      //   const point = document.createElement('div');
      //   point.className = 'timeline-point';
      //   point.style.left = (index / (data.length - 1) * 100) + '%';
      //   point.dataset.index = index;
      //   timelineElement.appendChild(point);
      //   return point;
      // });

      // // 创建时间刻度
      // const times = data.map(item => new Date(item.dataTime));
      // if (times.length === 0) return;

      // minTimes[droneIndex] = Math.min(...times);
      // minTimes[droneIndex] = Math.max(...times);
      // totalSpans[droneIndex] = maxTime[droneIndex] - minTimes[droneIndex];

      // let currentTick = minTimes[droneIndex];
      // const initialTick = document.createElement('div');
      // initialTick.className = 'timeline-tick';
      // initialTick.style.left = '0%';
      // timelineLine.appendChild(initialTick);

      // const initialTimeKd = document.createElement('div');
      // initialTimeKd.className = 'timeKd';
      // initialTimeKd.innerHTML = new Date(minTime[droneIndex]).toLocaleString();
      // initialTick.appendChild(initialTimeKd);

      // data.forEach((item, index) => {
      //   const currentTimes = new Date(item.dataTime);
      //   // 确定所需的刻度数，例如5个
      //     const numTicks = 8;
      //     const interval = totalSpans[index] / (numTicks - 1);
      //   while (currentTimes >= currentTick + interval) {
      //     const tick = document.createElement('div');
      //     tick.className = 'timeline-tick';
      //     tick.style.left = ((index) / (data.length - 1) * 100) + '%';
      //     timelineLine.appendChild(tick);

      //     const timeKd = document.createElement('div');
      //     timeKd.className = 'timeKd';
      //     timeKd.innerHTML = currentTimes.toLocaleString();
      //     tick.appendChild(timeKd);

      //     currentTick += interval;
      //   }
      // });

      // if (new Date(data[data.length - 1].dataTime) > currentTick) {
      //   const tick = document.createElement('div');
      //   tick.className = 'timeline-tick';
      //   tick.style.left = '100%';
      //   timelineLine.appendChild(tick);

      //   const timeKd = document.createElement('div');
      //   timeKd.className = 'timeKd';
      //   timeKd.innerHTML = new Date(data[data.length - 1].dataTime).toLocaleString();
      //   tick.appendChild(timeKd);
      // }

      currentTimeArr[droneIndex] = 0;
      timelineIntervalArr[droneIndex] = setInterval(function () {
        currentTimeArr[droneIndex] = (currentTimeArr[droneIndex] + 1) % data.length;
        updateTimelines(droneIndex, currentTimeArr[droneIndex]);
        handleDroneFlight(data, droneIndex,rowData);
      }, 1000);

      showTimeLine.value = true;

      // 添加飞行轨迹和标记
      const polyline = L.polyline(data.map(p => [p.dronLat, p.dronLng]), {
        color: rowData.color,
        weight: rowData.weight
      }).addTo(lineLayer);

      let planeIcon = L.icon({
        iconUrl: rowData.authStatus === 1 ? '/static/fly1.png' :
                 rowData.authStatus === 2 ? '/static/fly2.png' :
                 '/static/fly3.png',
        iconSize: [25, 25],
        iconAnchor: [12, 41],
        popupAnchor: [-3, -73]
      });

      window.Map2D.map.flyTo([Number(data[0].dronLat), Number(data[0].dronLng)], 11);

      planeMarkerArr[droneIndex] = data.map(function (point) {
        return L.marker([point.dronLat, point.dronLng], { icon: planeIcon });
      });
console.log(planeMarkerArr[droneIndex]);
      data.forEach(function (point) {
        console.log(data.indexOf(point));
        const marker = planeMarkerArr[droneIndex][data.indexOf(point)];
        marker.on('click', (e) => {
          console.log(e);
        });
      });
    }
  }
}


// 更新时间轴显示
const updateTimelines = (droneIndex, index) => {
  // const timelineElement = document.getElementById(`timeline-${droneIndex}`);
  // const currentPoint = timelineElement.querySelector('.timeline-point[data-index="' + index + '"]');
  // currentPoint.classList.add('timeline-current');
};
const markerLayerArr = {}
// 处理无人机飞行状态
const handleDroneFlight = (data, droneIndex,rowData) => {
  const point = data[currentTimeArr[droneIndex]];
  // speed.value = point.sd;
  // lat.value = point.dronLat;
  // lng.value = point.dronLng;
  // window.TOOL.data.set('wrjData', {
  //   longitude: Number(point.dronLng),
  //   latitude: Number(point.dronLat),
  //   altitude: 0,
  //   speed: point.sd
  // });
  if(markerLayerArr['markerLayerArr'+droneIndex] && markerLayerArr['markerLayerArr'+droneIndex]!=null){
        // searchRouteTjdLayer['tjdLayer'+tjdNum.value].clearLayers()
        window.Map2D.map.removeLayer(markerLayerArr['markerLayerArr'+droneIndex])
      }
      
      markerLayerArr['markerLayerArr'+droneIndex] = window.L.layerGroup([]).addTo(window.Map2D.map)

  // 更新标记位置
  planeMarkerArr[droneIndex][currentTimeArr[droneIndex]].setLatLng([point.dronLat, point.dronLng]).addTo(markerLayerArr['markerLayerArr'+droneIndex]);
  const  markerIcon = L.divIcon({
                        html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>${rowData.brand}-${rowData.model}(${rowData.serialNumber})</div>`,//marker标注
                        className: 'my-div-icon',
                        iconAnchor: [80, 20]//文字标注相对位置
                      });
                  window.L.marker(
                    window.L.latLng(Number(point.dronLat), Number( point.dronLng)),
                    {
                      icon: markerIcon,
                    }
                  ).addTo(markerLayerArr['markerLayerArr'+droneIndex]);

  // 处理任务点
  // const waypoints = JSON.parse(rowData.jhcs).waypoints;
  // for (let i = 0; i < waypoints.length; i++) {
  //   if (waypoints[i].longitude === point.dronLng && waypoints[i].latitude === point.dronLat) {
  //     if (mbfxToolData.value.indexOf(i) === -1) {
  //       mbfxToolData.value.push(i);
  //       getMbfx(waypoints[i].longitude, waypoints[i].latitude, i);
  //     }
  //   }
  // }

  if (new Date(point.dataTime).getTime() === new Date(data[data.length - 1].dataTime).getTime()) {
    endFlag.value = true;
  }
}


const clearLayerArr = () => {
  for (const key in markerLayerArr) {
      if (markerLayerArr[key] != null) {
        window.Map2D.map.removeLayer(markerLayerArr[key])
      }
    }
}

const removeDrone = (droneId) => {
  const index = selectData.value.findIndex(drone => drone.id === droneId);
  if (index !== -1) {
    selectData.value.splice(index, 1);
    // 清理相关资源
    clearInterval(timelineInterval.value[index]);
    timelineInterval.value.splice(index, 1);
    currentTime.value.splice(index, 1);
    // 其他清理操作
  }
}



const handlezztimeChange = (e) => {
  console.log(e);
}
const gjysChange = () =>{
  rowData.value.color = color.value;
  rowData.value.weight = gjWidth.value;
  addToMap()
}
const rowData = ref({})
const timeline = ref(null);
const showTimeLine  =ref(false)
const addToMap = () => {
  console.log('xxx',mapChange.value,tyjhData.value);
  if(tyjhData.value && tyjhData.value.length){
    if(mapChange.value){
          clearInterval(timelineInterval);
          timelineInterval = null;
          speed.value = 0;
          lng.value = 0;
          lat.value = 0;
          window.Map3D.wrjFly.clearAllLayers()
          Map3D.wrjFly.setMissionConfig(JSON.parse(rowData.value.jhcs).waypoints, { radius: rowData.value.mbfxfw && rowData.value.mbfxfw!=undefined?rowData.value.mbfxfw:30 });
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
          window.eventBus.on("mbtjxxEvent",function(data){
            if(data=='clear'){
              mbtjxxData.value=[]
            }else{
              mbtjxxData.value.push(data)
            }
            
          })
    }else{
          window.eventBus.off("wrjData")
           window.eventBus.off("mbtjxxEvent")
          window.TOOL.data.remove('wrjData')
          speed.value = 0;
          lng.value = 0;
          lat.value = 0;
          Map2D.setEwtyFlag(true)
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

          // 添加倍数下拉框和播放控制
          var controlDiv = document.createElement('div');
          controlDiv.className = 'play-control';
          timeline.value.appendChild(controlDiv);

          var speedSelect = document.createElement('select');
          speedSelect.id = 'speedSelect';
          speedSelect.innerHTML = `
            <option value="0.5">0.5x</option>
            <option value="1">1x</option>
            <option value="2">2x</option>
            <option value="4">4x</option>
          `;
          speedSelect.selectedIndex = 1; // 默认选中1x
          controlDiv.appendChild(speedSelect);

         


          // 为下拉框和按钮添加事件监听
          document.getElementById('speedSelect').addEventListener('change', function(e) {
            
              const speedMultiplier = parseFloat(this.value);
              clearInterval(timelineInterval);
              timelineInterval = setInterval(function() {
                if(endFlag.value){
                  zbmbClearLayer()
                  endFlag.value = false;
                  
                  mbfxToolData.value = []
                  mbtjxxData.value = []
                  checkBoxOptions.value = []
                  checkFlag.value=true;
                }
                  currentTime = (currentTime + 1) % tyjhData.value.length;
                  updateTimeline(currentTime);
                  clearLayer1()
                  // console.log(tyjhData.value[currentTime]);
                  planeMarkers[currentTime].setLatLng([tyjhData.value[currentTime].dronLat, tyjhData.value[currentTime].dronLng]).addTo(markerLayer);
                  const  markerIcon = L.divIcon({
                        html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>${rowData.value.brand || '未知'}-${rowData.value.model || '未知'}(${rowData.value.serialNumber || '未知'})</div>`,//marker标注
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
                        // speed.value =(distance * 1000 / fxTime).toFixed(8)
                        speed.value = tyjhData.value[currentTime].sd
                        lat.value = tyjhData.value[currentTime].dronLat
                        lng.value = tyjhData.value[currentTime].dronLng
                        window.TOOL.data.set('wrjData',{longitude:Number(tyjhData.value[currentTime].dronLng),latitude:Number(tyjhData.value[currentTime].dronLat),altitude:0,speed:speed.value})
                        addIcon(currentTime,minTime,totalSpan)
                        const waypoints = JSON.parse(rowData.value.jhcs).waypoints
                        for(let i = 0;i<waypoints.length;i++){
                          if(waypoints[i].longitude == tyjhData.value[currentTime].dronLng && waypoints[i].latitude == tyjhData.value[currentTime].dronLat){
                              zbmbClearLayer()
                              mbfxToolData.value=[]
                              if(mbfxToolData.value.indexOf(i)==-1){
                                mbfxToolData.value.push(i);
                                getMbfx(waypoints[i].longitude,waypoints[i].latitude,i);
                              }
                              
                            }
                        }
                        if(new Date(tyjhData.value[currentTime].dataTime).getTime() == new Date(tyjhData.value[tyjhData.value.length-1].dataTime).getTime()){
                          endFlag.value = true;
                        }
              }, 1000 / speedMultiplier);
              e.stopPropagation();
          });


          //创建图标容器
          var iconElement = document.createElement('div');
          iconElement.className = 'c-icon-container';
          timelineElement.appendChild(iconElement);

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
          // var timelineIdElement = document.getElementById('timeline');
          // var timelineLine = timelineIdElement.querySelector('.timeline-line');
          // 创建时间刻度
          // var lastTime = null;

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
          const times = tyjhData.value.map(item => new Date(item.dataTime));
          if (times.length === 0) return; // 避免空数据处理

          // 计算最小和最大时间
          minTime = Math.min(...times);
          maxTime = Math.max(...times);
          totalSpan = maxTime - minTime;

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
              e.stopPropagation()
              var rect = timelineElement.getBoundingClientRect();
              var x = e.clientX - rect.left;
              var time = (x / timelineElement.offsetWidth) * (tyjhData.value.length - 1);
              currentTime = Math.round(time);
              updateTimeline(currentTime);
              addIcon(currentTime,minTime,totalSpan);
              pdTime(currentTime)

          });
          showTimeLine.value = true;

          // 添加飞行轨迹
          var polyline = L.polyline(tyjhData.value.map(p => [p.dronLat, p.dronLng]),{color:color.value,weight:gjWidth.value}).addTo(lineLayer);
          // 添加飞机图标
          var planeIcon = L.icon({
              iconUrl: '/static/fly1.png',
              iconSize: [25, 25],
              iconAnchor: [12, 41],
              popupAnchor: [-3, -73]
          });
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

          window.Map2D.map.flyTo([Number(tyjhData.value[0].dronLat),Number(tyjhData.value[0].dronLng)],11)
        
          tyjhData.value.forEach(function(point) {
              var marker = L.marker([point.dronLat, point.dronLng], { icon: planeIcon });
              marker.on('click',(e)=>{
                console.log(e);
              })
              planeMarkers.push(marker);
          });

          // 时间轴更新逻辑
          timelineInterval = setInterval(function() {
              if(endFlag.value){
                zbmbClearLayer()
                endFlag.value = false;
                mbfxToolData.value = []
                mbtjxxData.value = []
                checkBoxOptions.value = []
                checkFlag.value=true;
              }
              currentTime = (currentTime + 1) % tyjhData.value.length;
              updateTimeline(currentTime);
              // polyline.setLatLngs([tyjhData.value[currentTime].dronLat, tyjhData.value[currentTime].dronLng]);
              clearLayer1()
              // console.log(tyjhData.value[currentTime]);
              planeMarkers[currentTime].setLatLng([tyjhData.value[currentTime].dronLat, tyjhData.value[currentTime].dronLng]).addTo(markerLayer);
              const  markerIcon = L.divIcon({
                html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>${rowData.value.brand || '未知'}-${rowData.value.model || '未知'}(${rowData.value.serialNumber || '未知'})</div>`,//marker标注
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
              // if(distance && fxTime){
              //   speed.value = (distance * 1000 / fxTime).toFixed(8)
              // }
              speed.value = tyjhData.value[currentTime].sd
              lat.value = tyjhData.value[currentTime].dronLat
              lng.value = tyjhData.value[currentTime].dronLng
              window.TOOL.data.set('wrjData',{longitude:Number(tyjhData.value[currentTime].dronLng),latitude:Number(tyjhData.value[currentTime].dronLat),altitude:0,speed:tyjhData.value[currentTime].sd})

              addIcon(currentTime,minTime,totalSpan)
              const waypoints = JSON.parse(rowData.value.jhcs).waypoints
              for(let i = 0;i<waypoints.length;i++){
                if(waypoints[i].longitude == tyjhData.value[currentTime].dronLng && waypoints[i].latitude == tyjhData.value[currentTime].dronLat){
                    zbmbClearLayer()
                    mbfxToolData.value=[]
                    if(mbfxToolData.value.indexOf(i)==-1){
                      mbfxToolData.value.push(i);
                      getMbfx(waypoints[i].longitude,waypoints[i].latitude,i);
                    }
                    
                  }
              }
              if(new Date(tyjhData.value[currentTime].dataTime).getTime() == new Date(tyjhData.value[tyjhData.value.length-1].dataTime).getTime()){
                endFlag.value = true;
              }
                
         }, 1000);

          
          trailParam.value.uavDatectMsgDto1.start = JSON.parse(rowData.value.jhcs).start
          trailParam.value.uavDatectMsgDto1.waypoints = JSON.parse(rowData.value.jhcs).waypoints
          trailParam.value.uavDatectMsgDto1.end = JSON.parse(rowData.value.jhcs).end
         addDrawPath(true)
         
    }
  }
  

}
// 添加时间轴动画
        const updateTimeline = (currentTime) => {
            timelinePoints.forEach(function(point, index) {
                if (index === currentTime) {
                    point.classList.add('timeline-current');
                } else {
                    point.classList.remove('timeline-current');
                }
            });
        }

        //  let uniqueIdCounter = 10000; // 全局变量，用于生成唯一的ID

         const pdTime = (currentTime) => {
          const waypoints = JSON.parse(rowData.value.jhcs).waypoints;
          zbmbClearLayer()
          mbfxToolData.value = []
          mbtjxxData.value = []
          checkBoxOptions.value = []
          console.log(tyjhData.value,waypoints);
          for(let i = 0;i<waypoints.length;i++){
            
            //第一个途经点
            const firstwaypointData = tyjhData.value.filter(v=>waypoints[0].longitude == v.dronLng && waypoints[0].latitude == v.dronLat)
            
            const firstwaypointTime = firstwaypointData[0].dataTime;

           

            const waypointFirstData = tyjhData.value.filter(v=>waypoints[i].longitude == v.dronLng && waypoints[i].latitude == v.dronLat)
            console.log(waypointFirstData);
            
            const waypointFirstTime = waypointFirstData[0].dataTime;
            if(new Date(tyjhData.value[currentTime].dataTime).getTime()>new Date(waypointFirstTime).getTime()){
              
              for(let j=0;j<=i;j++){
                  if(mbfxToolData.value.indexOf(j)==-1){
                    mbfxToolData.value.push(j);  
                    getMbfx(waypoints[j].longitude,waypoints[j].latitude,j); 
                  }
              }
            }else if(new Date(tyjhData.value[currentTime].dataTime).getTime()<new Date(firstwaypointTime).getTime()){
              zbmbClearLayer()
              mbfxToolData.value = []
              mbtjxxData.value = []
            }
          }
          
        }

        const BH = ref({
          timeKeyAttr: 'BHSJ',
          iconSrc: '/static/map_img/icon/bjbh.png',
          iconSrc2: '/static/map_img/icon/bjbh2.png',
          mapMarker: '/static/map_img/icon/bjbh.png',
          nameAttr: '',
          createUerAttr: '',
          departmentAttr: ''
        })

        const addIcon = (currentTime,minTime,totalSpan) => {
          window.API.bigScreenDataFx.getBhByYwIdDataTime({
            ywid:rowData.value.id,
            datatime:tyjhData.value[currentTime].dataTime
          }).then(res=>{
            if(res.code==200){
              timeLineShowIcons(res.result,minTime,totalSpan);
            }
          })
          // let data = [
          //   {
          //     bhsj: "2025-12-03 19:46:01",
          //     gisjson:["{\"scaleByMap\":true,\"surroundLineWidth2D\":0.7937499999999998,\"algoMaxEditPts\":1,\"libID\":123,\"code\":30001,\"anchorPoint\":{\"x\":0,\"y\":26},\"symbolIsCanFill\":false,\"middleMarkBounds\":{\"top\":0,\"left\":0,\"bottom\":0,\"leftBottom\":{\"x\":0,\"y\":0},\"right\":0,\"rightTop\":{\"x\":0,\"y\":0}},\"annotationPosition\":4,\"algoMinEditPts\":1,\"type\":\"GRAPHICOBJECT\",\"surroundLineColor\":{\"red\":255,\"green\":255,\"blue\":0,\"alpha\":255},\"symbolRanks\":[],\"surroundLineType\":0,\"symbolName\":\"恐怖分子\",\"innerCells\":[{\"positionPoints\":[{\"x\":0.636914,\"y\":49.52815},{\"x\":0.636914,\"y\":99.029241}],\"surroundLineWidth\":1,\"fontColorLimit\":false,\"polybezierClose\":false,\"textContent\":\"\",\"type\":29,\"surroundLineColor\":{\"red\":255,\"green\":255,\"blue\":0,\"alpha\":255},\"lineWidthLimit\":false,\"surroundLineFlag\":false,\"lineColorLimit\":true,\"fillColorLimit\":false,\"surroundLineLimit\":false,\"fillLimit\":false,\"lineTypeLimit\":false,\"surroundLineType\":0,\"style\":{\"fillGradientOffsetRatioX\":0,\"markerSize\":2.4,\"fillForeColor\":{\"red\":255,\"green\":0,\"blue\":0,\"alpha\":255},\"fillGradientOffsetRatioY\":0,\"markerWidth\":0,\"markerAngle\":0,\"fillSymbolID\":1,\"lineColor\":{\"red\":0,\"green\":0,\"blue\":128,\"alpha\":255},\"markerSymbolID\":0,\"lineWidth\":0.5,\"markerHeight\":0,\"fillOpaqueRate\":100,\"fillBackOpaque\":true,\"fillBackColor\":{\"red\":255,\"green\":0,\"blue\":0,\"alpha\":255},\"fillGradientMode\":\"NONE\",\"lineSymbolID\":0,\"fixedColorOfSVG\":false,\"fillGradientAngle\":0},\"textStyle\":{\"italicAngle\":0,\"shadow\":false,\"sizeFixed\":true,\"underline\":false,\"rotation\":0,\"backOpaque\":false,\"bold\":false,\"align\":\"MIDDLECENTER\",\"foreColor\":{\"red\":0,\"green\":0,\"blue\":0,\"alpha\":255},\"italic\":false,\"strikeout\":false,\"fontName\":\"黑体\",\"outline\":false,\"borderSpacingWidth\":4,\"backColor\":{\"red\":0,\"green\":0,\"blue\":0,\"alpha\":255},\"outlineWidth\":1,\"fontHeight\":7,\"fontWidth\":0,\"opaqueRate\":100,\"stringAlignment\":\"LEFT\",\"fontScale\":1,\"fontWeight\":0}}],\"partTopo\":null,\"middleMarkExist\":false,\"scaleValues\":[],\"symbolSize\":{\"x\":97.89583333333333,\"y\":97.89583333333333},\"limitWidthHeight\":true,\"textContent\":\"\",\"symbolType\":1,\"negativeImage\":false,\"dynamicToken\":\"496e05e1aea0a9c4655800e8a7b9ea28\",\"rotate2D\":{\"x\":0,\"y\":0,\"z\":0},\"scale2D\":{\"x\":1,\"y\":1,\"z\":0},\"style\":{\"fillGradientOffsetRatioX\":0,\"markerSize\":2.4,\"fillForeColor\":{\"red\":255,\"green\":0,\"blue\":0,\"alpha\":255},\"fillGradientOffsetRatioY\":0,\"markerWidth\":0,\"markerAngle\":0,\"fillSymbolID\":1,\"lineColor\":{\"red\":255,\"green\":0,\"blue\":0,\"alpha\":255},\"markerSymbolID\":0,\"lineWidth\":0.5291666666666667,\"markerHeight\":0,\"fillOpaqueRate\":30,\"fillBackOpaque\":1,\"fillBackColor\":{\"red\":255,\"green\":0,\"blue\":0,\"alpha\":255},\"fillGradientMode\":0,\"lineSymbolID\":0,\"fixedColorOfSVG\":false,\"fillGradientAngle\":0,\"strokeGradientMode\":0,\"strokeBackColor\":{\"red\":255,\"green\":0,\"blue\":0,\"alpha\":255},\"fillAngle\":0,\"fillCenterOffsetX\":0,\"fillCenterOffsetY\":0,\"dashArray\":null,\"fontSpace\":0,\"fontPercent\":100,\"fontWeight\":\"normal\"},\"strCode\":\"\",\"scalePoints\":[],\"textStyle2D\":{\"italicAngle\":0,\"shadow\":false,\"sizeFixed\":true,\"underline\":false,\"rotation\":0,\"backOpaque\":false,\"bold\":false,\"align\":10,\"foreColor\":{\"red\":0,\"green\":0,\"blue\":0,\"alpha\":255},\"italic\":false,\"strikeout\":false,\"fontName\":\"黑体\",\"outline\":false,\"borderSpacingWidth\":4,\"backColor\":{\"red\":0,\"green\":0,\"blue\":0},\"outlineWidth\":1,\"fontHeight\":3.1749999999999994,\"fontWidth\":0,\"opaqueRate\":100,\"stringAlignment\":\"LEFT\",\"fontScale\":1,\"fontWeight\":0,\"fontStrokeColor\":{\"red\":0,\"green\":0,\"blue\":0},\"fontBackgroundColor\":{\"red\":0,\"green\":0,\"blue\":0},\"shadowColor\":{\"red\":255,\"green\":0,\"blue\":0},\"shadowOffsetX\":0,\"shadowOffsetY\":0},\"subSymbols\":[],\"symbolRank\":0,\"succeed\":true,\"version\":2,\"isEdit\":true,\"uuid\":\"053d4923-61ce-42f6-aa99-a841437f8c56\",\"associatedUuid\":\"\",\"dashLines\":[],\"textDisplay\":true,\"resolution\":null,\"isLocked\":false,\"note\":\"\",\"custom\":null,\"extendProperty\":[],\"avoidRegions\":[],\"visible\":true,\"localePoints\":[{\"x\":116.36650085449219,\"y\":39.1523551940918,\"z\":0}],\"horizontalMirror\":null,\"verticalMirror\":null,\"space\":1.852083333333333,\"flagTextSize\":60,\"maxScale\":5,\"minScale\":1,\"actualScale\":1,\"fontSize\":12,\"symbolSizeInLib\":{\"x\":97.89583333333333,\"y\":97.89583333333333},\"positionOffset\":false,\"positionOffsetType\":0,\"positionOffsetX\":0,\"positionOffsetY\":0,\"symbolTexts\":[],\"bloodVolumes\":[],\"pictureFrames\":[],\"symbolAnnotations\":[],\"isIntegrate\":false}"],
              
          //   }
          // ]
          // let data2 = [
          //   {
          //     bhsj: "2025-12-03 19:39:01",
          //     gisjson:["{\"scaleByMap\":true,\"surroundLineWidth2D\":0.7937499999999998,\"algoMaxEditPts\":1,\"libID\":123,\"code\":30001,\"anchorPoint\":{\"x\":0,\"y\":26},\"symbolIsCanFill\":false,\"middleMarkBounds\":{\"top\":0,\"left\":0,\"bottom\":0,\"leftBottom\":{\"x\":0,\"y\":0},\"right\":0,\"rightTop\":{\"x\":0,\"y\":0}},\"annotationPosition\":4,\"algoMinEditPts\":1,\"type\":\"GRAPHICOBJECT\",\"surroundLineColor\":{\"red\":255,\"green\":255,\"blue\":0,\"alpha\":255},\"symbolRanks\":[],\"surroundLineType\":0,\"symbolName\":\"恐怖分子\",\"innerCells\":[{\"positionPoints\":[{\"x\":0.636914,\"y\":49.52815},{\"x\":0.636914,\"y\":99.029241}],\"surroundLineWidth\":1,\"fontColorLimit\":false,\"polybezierClose\":false,\"textContent\":\"\",\"type\":29,\"surroundLineColor\":{\"red\":255,\"green\":255,\"blue\":0,\"alpha\":255},\"lineWidthLimit\":false,\"surroundLineFlag\":false,\"lineColorLimit\":true,\"fillColorLimit\":false,\"surroundLineLimit\":false,\"fillLimit\":false,\"lineTypeLimit\":false,\"surroundLineType\":0,\"style\":{\"fillGradientOffsetRatioX\":0,\"markerSize\":2.4,\"fillForeColor\":{\"red\":255,\"green\":0,\"blue\":0,\"alpha\":255},\"fillGradientOffsetRatioY\":0,\"markerWidth\":0,\"markerAngle\":0,\"fillSymbolID\":1,\"lineColor\":{\"red\":0,\"green\":0,\"blue\":128,\"alpha\":255},\"markerSymbolID\":0,\"lineWidth\":0.5,\"markerHeight\":0,\"fillOpaqueRate\":100,\"fillBackOpaque\":true,\"fillBackColor\":{\"red\":255,\"green\":0,\"blue\":0,\"alpha\":255},\"fillGradientMode\":\"NONE\",\"lineSymbolID\":0,\"fixedColorOfSVG\":false,\"fillGradientAngle\":0},\"textStyle\":{\"italicAngle\":0,\"shadow\":false,\"sizeFixed\":true,\"underline\":false,\"rotation\":0,\"backOpaque\":false,\"bold\":false,\"align\":\"MIDDLECENTER\",\"foreColor\":{\"red\":0,\"green\":0,\"blue\":0,\"alpha\":255},\"italic\":false,\"strikeout\":false,\"fontName\":\"黑体\",\"outline\":false,\"borderSpacingWidth\":4,\"backColor\":{\"red\":0,\"green\":0,\"blue\":0,\"alpha\":255},\"outlineWidth\":1,\"fontHeight\":7,\"fontWidth\":0,\"opaqueRate\":100,\"stringAlignment\":\"LEFT\",\"fontScale\":1,\"fontWeight\":0}}],\"partTopo\":null,\"middleMarkExist\":false,\"scaleValues\":[],\"symbolSize\":{\"x\":97.89583333333333,\"y\":97.89583333333333},\"limitWidthHeight\":true,\"textContent\":\"\",\"symbolType\":1,\"negativeImage\":false,\"dynamicToken\":\"496e05e1aea0a9c4655800e8a7b9ea28\",\"rotate2D\":{\"x\":0,\"y\":0,\"z\":0},\"scale2D\":{\"x\":1,\"y\":1,\"z\":0},\"style\":{\"fillGradientOffsetRatioX\":0,\"markerSize\":2.4,\"fillForeColor\":{\"red\":255,\"green\":0,\"blue\":0,\"alpha\":255},\"fillGradientOffsetRatioY\":0,\"markerWidth\":0,\"markerAngle\":0,\"fillSymbolID\":1,\"lineColor\":{\"red\":255,\"green\":0,\"blue\":0,\"alpha\":255},\"markerSymbolID\":0,\"lineWidth\":0.5291666666666667,\"markerHeight\":0,\"fillOpaqueRate\":30,\"fillBackOpaque\":1,\"fillBackColor\":{\"red\":255,\"green\":0,\"blue\":0,\"alpha\":255},\"fillGradientMode\":0,\"lineSymbolID\":0,\"fixedColorOfSVG\":false,\"fillGradientAngle\":0,\"strokeGradientMode\":0,\"strokeBackColor\":{\"red\":255,\"green\":0,\"blue\":0,\"alpha\":255},\"fillAngle\":0,\"fillCenterOffsetX\":0,\"fillCenterOffsetY\":0,\"dashArray\":null,\"fontSpace\":0,\"fontPercent\":100,\"fontWeight\":\"normal\"},\"strCode\":\"\",\"scalePoints\":[],\"textStyle2D\":{\"italicAngle\":0,\"shadow\":false,\"sizeFixed\":true,\"underline\":false,\"rotation\":0,\"backOpaque\":false,\"bold\":false,\"align\":10,\"foreColor\":{\"red\":0,\"green\":0,\"blue\":0,\"alpha\":255},\"italic\":false,\"strikeout\":false,\"fontName\":\"黑体\",\"outline\":false,\"borderSpacingWidth\":4,\"backColor\":{\"red\":0,\"green\":0,\"blue\":0},\"outlineWidth\":1,\"fontHeight\":3.1749999999999994,\"fontWidth\":0,\"opaqueRate\":100,\"stringAlignment\":\"LEFT\",\"fontScale\":1,\"fontWeight\":0,\"fontStrokeColor\":{\"red\":0,\"green\":0,\"blue\":0},\"fontBackgroundColor\":{\"red\":0,\"green\":0,\"blue\":0},\"shadowColor\":{\"red\":255,\"green\":0,\"blue\":0},\"shadowOffsetX\":0,\"shadowOffsetY\":0},\"subSymbols\":[],\"symbolRank\":0,\"succeed\":true,\"version\":2,\"isEdit\":true,\"uuid\":\"053d4923-61ce-42f6-aa99-a841437f8c56\",\"associatedUuid\":\"\",\"dashLines\":[],\"textDisplay\":true,\"resolution\":null,\"isLocked\":false,\"note\":\"\",\"custom\":null,\"extendProperty\":[],\"avoidRegions\":[],\"visible\":true,\"localePoints\":[{\"x\":116.36650085449219,\"y\":39.1523551940918,\"z\":0}],\"horizontalMirror\":null,\"verticalMirror\":null,\"space\":1.852083333333333,\"flagTextSize\":60,\"maxScale\":5,\"minScale\":1,\"actualScale\":1,\"fontSize\":12,\"symbolSizeInLib\":{\"x\":97.89583333333333,\"y\":97.89583333333333},\"positionOffset\":false,\"positionOffsetType\":0,\"positionOffsetX\":0,\"positionOffsetY\":0,\"symbolTexts\":[],\"bloodVolumes\":[],\"pictureFrames\":[],\"symbolAnnotations\":[],\"isIntegrate\":false}"],
              
          //   }
          // ]
          // timeLineShowIcons(data,minTime,totalSpan);
          // timeLineShowIcons(data2,minTime,totalSpan);
        }

    const timeLineShowIcons = (items,minTime,totalSpan) => {
      clearAllIcon();
      if (items.length) {
        var arr = []
        for(var i of items){
          var obj = {}
          for(var key in i){
            obj[key.toUpperCase()] = i[key];
            obj.infoType = "BH"
            obj.timeKeyAttr = i.timeKeyAttr
          }
          arr.push(obj)
        }
        console.log(arr);
        items = arr
        // 生成图标 映射到时间轴上
        items.forEach((item) => {
          
      console.log('items:',(new Date(item['BHSJ']).getTime() - new Date(minTime).getTime()), totalSpan)
          const { iconSrc, timeKeyAttr } = BH.value;
          console.log(((new Date(item['BHSJ']).getTime() - new Date(minTime).getTime()) / new Date(totalSpan).getTime()) * 100 + '%');
          // 动态创建图标
            const flag = document.createElement("img");
            flag.setAttribute(
              "style",
              `position:absolute;bottom:40px;left:${
                ((new Date(item['BHSJ']).getTime() - new Date(minTime).getTime()) / totalSpan) * 100 + '%'
              }; cursor: pointer;width:20px;transform: translateX(-50%);`
            );
            flag.setAttribute("src", iconSrc);
            flag.setAttribute("class", "c-BH");
            flag.setAttribute("alt", JSON.stringify(item));
            flag.onclick = handleIconPosition;
            // flag.onmouseenter = showIconTips;
            // flag.onmouseover = showIconTips;
            // flag.onmouseout = hiddenIconTips;
            // flag.onmouseleave = hiddenIconTips;
            
            let iconContainer = document.querySelector(".c-icon-container")
            iconContainer.appendChild(flag);

        });
          
            for (var i = 0; i < Map2D.map.getPlottingLayers().length; i++) {
              Map2D.map.getPlottingLayers()[i].removeAllFeatures();
            }
          
          
          Map2D.drawControl.handler.disable();

          var arr22 = items.filter(row=>{return row.infoType == "BH"})
          console.log('biaohui',arr22);
          // .map(item=>{return item.TSMC})
          for(var i of arr22){
            // Map2D.plotting.getSitDataManager().openSmlFileOnServer(i, function (evt) {
            // 叠加标绘
            Map2D.plotting.getSitDataManager().addSmlFileOnServer(i.TSMC, function (evt) {
              console.log(evt);
              Map2D.drawControl.setDrawingLayer(evt.sitDataLayers[0]);
              Map2D.plottingLayer = evt.sitDataLayers[0];
            });
          }
        
        
      }
    }

    /**
     * 点击时间轴图标在地图中地位中心点
     * @param {object} item
     */
    const handleIconPosition = (e) => {
      e.stopPropagation()
      const intelligenceInfo = JSON.parse(e.target.alt);
      // console.log(intelligenceInfo);
      const position = [intelligenceInfo.WD, intelligenceInfo.JD];
      if (position[0] && position[1]) {
        console.log(position);
        window.Map2D.map.flyTo(position, 9)
      }
      
    }

     const iconTipContent = ref("")
    // 显示图标提示 获取dom元素 alt 值
    // const showIconTips = (e) => {
    //   event.stopPropagation();
    //   iconTipContent.value = JSON.parse(e.target.alt);
    //   const elPositionLeft = e.target.style.left;
    //   this.$refs.iconTipContainer.style.left = elPositionLeft;
    // },

    // // 关闭图标提示
    // const hiddenIconTips = () => {
    //   // await sleep(2000)
    //   iconTipContent.value = {};
    // }
    // 清除图标
    const clearAllIcon = () => {
      const nodes = document.querySelectorAll(".c-icon-container>img");
      const iconContainer = document.querySelector(".c-icon-container")
      // console.log("清除", nodes);
      if (nodes && nodes.length) {
        for (let i = nodes.length - 1; i >= 0; i--) {
          iconContainer.removeChild(nodes[i]);
          $(".c-icon-container img").eq(i).remove();
        }
      }
    }
// 清除图层
const clearLayer1 = () => {
  // console.log(markerLayer);
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
    display: flex;
    align-items: center;
    color:#1489ff;
  }
}
.settingBox{
    width: 650px;
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
    .title2{
      width: 100%;
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 14px;
      color: #fff;
      padding-left: 8px;
      box-sizing: border-box;
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
  // right: 0;
  // width: 100%;
  height: 30px;
  padding: 0 20px;
  box-sizing: border-box;
  color: #1489ff;
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
    :deep(.gjkd .el-input__inner){
      color:#1489ff !important;
    }
    .sdsk-position {
      position: absolute;
      right: 0;
      top: 30px;
      pointer-events: auto;
    }
    .tb-position {
      position: absolute;
      right: 80px;
      top: 30px;
      pointer-events: auto;
    }
    .mapTools{
      position: absolute;
    // bottom: 225px;
    // right: 368px;
    top:-4px;
    right:0;
    z-index: 111;
    display: flex;
    color: #fff;
    align-items:center;
    padding:5px 10px;
   pointer-events: auto;
   &>div{

    padding:5px;
   }
}

::v-deep .high-zindex-dropdown {
  z-index: 9999 !important;
}
:deep(.checkBoxs .el-checkbox){
  display: block;
}
:deep(.el-checkbox__input.is-checked+.el-checkbox__label){
        color:#50b9f3;
      }
</style>