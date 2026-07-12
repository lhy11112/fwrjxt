<template>
<div class="largeContent">
  
    <!-- 二维地图 -->
        <Map2DComp></Map2DComp>
        <!-- 三维地图 -->
        <Map3DComp></Map3DComp>
    <div id="bigScreen">
      <TOPTITLE title="目标防卫场景反无人机应用" @mapChangeEvent="mapChangeEvent"></TOPTITLE>
      <div class="cont" v-if="!scFlag" :style="{height:bottomFlag || zhtsBottomFlag?'69.4vh':'93.4vh'}">
        <!-- 四周闪烁层 -->
        <div class="flash-border top"></div>
        <div class="flash-border bottom"></div>
        <div class="flash-border left"></div>
        <div class="flash-border right"></div>
        <div class="clTools">
          <div @click="measuredDistance">测距</div>
          <div @click="measuredArea">测面积</div>
          <div @click="measuredHeight">测高</div>
          <div @click="clear">清除</div>

        </div>
        <router-view />
      </div>
      <div class="scBox" v-show="scFlag">
        <div class="title">
          收藏夹
          <el-icon style="cursor: pointer;position: absolute;top: 8px;right: 8px;" @click.stop="closeSc()" title="关闭"><Close /></el-icon>
        </div>
        <div class="icons">
          <el-icon @click="savePoint('视点')" title="保存当前视角"><View /></el-icon>
          <el-icon @click="savePoint('目录')" title="保存目录"><FolderChecked /></el-icon>
          <!-- <el-icon @click="removeSelect" title="删除"><Delete /></el-icon> -->
        </div>
        <div class="scContent">
          
              <!-- @contextmenu="handleContextMenu" -->
            <el-tree
              :data="treeData"
              :props="defaultProps"
              @node-click="handleNodeClick"
            >
            <template #default="{ node, data }">
              <span class="custom-tree-node" style="display:flex;justify-content: space-between;width: 100%;padding-right: 10px;">
                <div @click="goSt(node,data)" :title="node.label" style="width:200px;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;">{{ node.label }}</div>
                <span>
                 <a @click.stop="remove(data)">删除</a>
                  <a style="margin-left: 8px" @click.stop="edit(data)">编辑</a>
                </span>
              </span>
            </template>
            </el-tree>
            
          
        </div>
      </div>

      <bottomTitle v-if="bottomFlag && !scFlag"></bottomTitle>
      <zhtsBottomTitle v-if="zhtsBottomFlag && !scFlag"></zhtsBottomTitle>
    </div>
    
  <mbfxDialog v-if="mbfxVisible" ref="mbfxRef" @closed="mbfxClose" @success="mbfxSuccess"></mbfxDialog>
  <dxdmDialog v-if="dxdmVisible" ref="dxdmRef" @closed="dxdmClose" @success="dxdmSuccess"></dxdmDialog>
  
  <dxdmDetailDialog ref="dxdmDetailRef"></dxdmDetailDialog>
  <mbfxDetailDialog ref="mbfxDetailRef"></mbfxDetailDialog>
  <cesiumAirDialog v-show="false"></cesiumAirDialog>
  <kyAddDialog v-if="kyAddVisible" ref="kyAddRef"></kyAddDialog>
  
  <shituDialog v-if="stVisible" ref="stRef" @successClick="shituSuccess" @closed="shituClosed"></shituDialog>
  <gjfxcsDialog v-if="gjfxcsVisible" ref="gjfxcsRef" type="menu" @success="gjfxcsSuccess" @closed="gjfxcsClose"></gjfxcsDialog>
  <div style="pointer-events: none;">
    <gjfxDialog ref="gjfxRef"></gjfxDialog>
  </div>
  <div class="mapTool" v-if="mbfxTool">
    目标分析
    <el-icon style="cursor:pointer" @click="mbfxMapClose"><Close /></el-icon>
  </div>
    <div class="mapTool" v-if="dxdmTool" style="right:159px">
      综合兵要
      <el-icon style="cursor:pointer" @click="dxdmMapClose"><Close /></el-icon>
    </div>
    <tbDialog ref="tbRef" v-if="tbVisible" @close="tbClose"></tbDialog>
</div>
</template>

<script setup>
import tbDialog from "./tb.vue";
import { useRouter,useRoute, createWebHashHistory } from "vue-router";
import {onMounted,watch,ref,nextTick,onUnmounted} from "vue"
import mbfxDialog from "./mbfx.vue"
import dxdmDialog from "./dxdm.vue"
import dxdmDetailDialog from "./dxdmDetail.vue"
import mbfxDetailDialog from "./mbfxDetail.vue"
import kyAddDialog from "./kyAdd.vue"
import shituDialog from "./shitu.vue"
import cesiumAirDialog from "../portal/cesiumAir/index.vue"
import { mapModeEnum, getCurrentMapMode } from "@/utils/Map/mapMode";
import { useMap3DStore } from "@/store/modules/map3D";
import { useMap2DStore } from "@/store/modules/map2D";
import zhtsBottomTitle from "../zhtsBottomTitle/index.vue"
import gjfxcsDialog from "../bottomTitle/gjfxcs.vue"
import gjfxDialog from "../bottomTitle/gjfx.vue"
import { ElMessage,ElMessageBox } from "element-plus";
// 路由定义
const route = useRoute();
const router = useRouter();
const bottomFlag = ref(false);
const zhtsBottomFlag = ref(false);
const scFlag = ref(false);
const mapChange = ref(false);
const defaultProps= ref({
  children: 'children',
  label: 'mc'
})

const showBtns = ref(false)
watch(
  route,
  (newVal) => {
    if(newVal){
     if(newVal.name=="commandControl" || newVal.name=="zbxx" || newVal.name=="fwzf" || newVal.name=="bxpz" || newVal.name=="cesiumAir" || newVal.name=="zymb" || newVal.name=="zhby"){
        bottomFlag.value = true;
        zhtsBottomFlag.value = false;
      }else if(newVal.name=="portal"|| newVal.name=="wxdzc" ||newVal.name=="signalInterference" ||newVal.name=="navigationDeception"){
        zhtsBottomFlag.value = true;
        bottomFlag.value = false;
      }else{
        zhtsBottomFlag.value = false;
        bottomFlag.value = false;
      }
    }
  }
)
// 判断地址栏路由
if (window.location.href.includes("commandControl") || window.location.href.includes("zbxx")|| window.location.href.includes("fwzf")|| window.location.href.includes("bxpz")|| window.location.href.includes("cesiumAir")|| window.location.href.includes("zymb")|| window.location.href.includes("zhby")) {
  bottomFlag.value = true;
}else{
  bottomFlag.value = false;
}

// 判断地址栏路由
if (window.location.href.includes("portal") || window.location.href.includes("wxdzc") || window.location.href.includes("signalInterference") || window.location.href.includes("navigationDeception")) {
  
  zhtsBottomFlag.value = true;
}else{
  zhtsBottomFlag.value = false;
}
onMounted(()=>{
  window.mbfx = getMbfx;
  window.dxdm = getDxdm;
  window.gjfx = getGjfx;
  window.kx = getKx;
  window.yp = yp;
  window.gr = gr;
  window.clearRect =clearRect;
  // window.ty = ty;
  window.addKy = addKy;
  window.xd = getXd;
  window.kxDetail = mbfxDetail;
  eventBus.on("addKy",function(data){
    console.log(data);
    let obj = {};
    obj.zxdwd = data.kystartPoint.lat;
    obj.zxdjd = data.kystartPoint.lng;
    obj.bj = parseInt(data.distance * 1000);
    openKyAddDialog(obj)
  });

  eventBus.on("zhtsBottomFlag",data=>{
    zhtsBottomFlag.value = !data;
  })

  eventBus.on("clearAboutTc",data=>{
    clearLayer1()
    clearLayer2()
    mbfxMapClose()
    dxdmMapClose()
  })

  window.eventBus.on('shoucang',() =>{
    scFlag.value = !scFlag.value;
    if(scFlag.value){
      gettreeData()
    }
  })

  if(curMapMode === mapModeEnum["3D"]){
    mapChange.value = true
    // eventBus.emit("zhtsBottomFlag",mapChange.value)
  }else if(curMapMode === mapModeEnum["2D"]){
    mapChange.value = false
    // eventBus.emit("zhtsBottomFlag",mapChange.value)
  }

  
})
onUnmounted(()=>{
  eventBus.off("addKy")
  eventBus.off("zhtsBottomFlag")
  eventBus.off("clearAboutTc")
  eventBus.off("shoucang")
})

const treeData = ref([])
const gettreeData = () => {
  let params={}
  if(!window.config.VUE_CAS_FLAG){
    params.yhId = window.config.VUE_CAS_YHID
  }
  window.API.scj.tree(params).then(res=>{
    if(res.code==200){
      treeData.value = res.result;
    }
  })
}


const mapChangeEvent = () => {
  console.log('xxxxxxxxxxxxxxx',mapChange.value);
    mapChange.value = !mapChange.value
    console.log('xxxxxxxxxxxxxxx',mapChange.value);
    
    if(scFlag.value){
      gettreeData()
    }
}
const closeSc = () => {
  scFlag.value = false;
}
const scData = ref([])
const stRef  = ref(null);
const stVisible = ref(false);
const checkList = ref([])
const scjColumnData = ref([{
  label:'名称',
  prop:'mc'
}])

const selectScjData = ref([])
const selectionScChange = (e) => {
  console.log(e);
  selectScjData.value = e;
}

const removeSelect = () => {
  if(selectScjData.value.length==0){
    ElMessage({
      type:'info',
      message:'至少选择一项数据'
    })
    return
  }
  ElMessageBox.confirm(`确定删除选中的 ${selectScjData.value.length} 项吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          selectScjData.value.forEach((item,index)=>{
            if(scData.value.indexOf(item)!=-1){
              scData.value.splice(scData.value.indexOf(item), 1);
            }
          })
        })
        .catch(() => {});
  
}
const remove = (data,index) => {
  if(data.children && data.children.length){
    ElMessage({
                type:'info',
                message:'该节点有子级，不可删除！'
              })
              return
  }
  ElMessageBox.confirm(`确定删除该数据吗？`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          window.API.scj.delete({ids:data.id}).then(res=>{
            if(res.code == 200){
              ElMessage({
                type:'success',
                message:'操作成功'
              })
              gettreeData();
            }
          })
        })
        .catch(() => {});
 
}

const edit = (data) => {
  console.log(data);
  if(!data.pid){
    data.pid=0;
  }
  stVisible.value = true;
  nextTick(()=>{
    stRef.value.open('edit').setData(data)
  })
}

const saveParams = ref({
  csz:{
    scData1:[],
    scData2:[]
  }
})
// const saveSubmit = () => {
 
  
// }


const goSt = (node,data) => {
  console.log(node,data);
  if(data.csz){
    const csz = JSON.parse(data.csz);
    console.log(csz,mapChange.value);
    if (mapChange.value) {
      viewer.camera.setView({
              destination: csz.scData2.position,
              orientation: {
                  heading: csz.scData2.heading,
                  pitch: csz.scData2.pitch,
                  roll: csz.scData2.roll
              }
      });
    }else {
      window.Map2D.map.flyTo([Number(csz.scData1.wd),Number(csz.scData1.jd)],csz.scData1.zoom);
    }
  }    
  
}
const saveType= ref("")
const savePoint = (data) => {
  saveType.value = data;
  stVisible.value = true;
  if(data == '视点'){
    if (mapChange.value) {
      nextTick(()=>{
        const camera = window.Map3D.viewer.camera;
        const position = camera.position.clone();
        const cartographic = Cesium.Cartographic.fromCartesian(position);
        const longitude = Cesium.Math.toDegrees(cartographic.longitude).toFixed(6);
        const latitude = Cesium.Math.toDegrees(cartographic.latitude).toFixed(6);
        const altitude = cartographic.height.toFixed(2);
        const heading = camera.heading;
        const pitch = camera.pitch;
        const roll = camera.roll;
        const view = {
            mc:`视角X:${latitude},视角Y:${longitude},视角Z:${altitude}`,
            position: position,
            heading: heading,
            pitch: pitch,
            roll: roll,
            type:data
            // id:Math.random() * 10000000
        };
        
        console.log(view,longitude,latitude,altitude);
        stRef.value.open('add').setData(view)
      })
    }else{
      nextTick(()=>{
        let obj={}
        obj.mc = `视角X:${window.Map2D.map.getCenter().lat},视角Y:${window.Map2D.map.getCenter().lng},视角Z:${window.Map2D.map.getZoom()}`
        obj.jd= window.Map2D.map.getCenter().lng;
        obj.wd = window.Map2D.map.getCenter().lat;
        obj.zoom = window.Map2D.map.getZoom();
        obj.type = data;
        // obj.id = Math.random() * 10000000;
        stRef.value.open('add').setData(obj)
      })
    }
  }else{
    stRef.value.open('add').setData()
  }
  
  
}

const shituSuccess = (data,mode) => {
  stVisible.value = false;
  console.log(data,mode);
  saveParams.value = Object.assign(saveParams.value,data);
  saveParams.value.type = saveType.value
  saveParams.value.csz = {}
  if(!window.config.VUE_CAS_FLAG){
      saveParams.value.yhId = window.config.VUE_CAS_YHID
    }
  if(saveType.value == '视点'){
    console.log(saveParams.value);
    
    if (mapChange.value) {
      console.log('xxxxxxx');
      saveParams.value.csz.scData2 = data;
    }else{
      console.log('aaaaaaaaa');
      saveParams.value.csz.scData1 = data;
    }
    saveParams.value.csz= JSON.stringify(saveParams.value.csz);
  }else{
    delete saveParams.value.csz
  }
  
  console.log(saveParams.value);
  if(saveParams.value.id){
    window.API.scj.edit(saveParams.value).then(res=>{
      if(res.code==200){
        ElMessage({
          type: "success",
          message: "保存成功",
        })
        gettreeData()
      }else{
        ElMessage({
          type: "success",
          message: "保存失败",
        })
      }
    })
  }else{
    window.API.scj.add(saveParams.value).then(res=>{
      if(res.code==200){
        ElMessage({
          type: "success",
          message: "保存成功",
        })

        gettreeData()
      }else{
        ElMessage({
          type: "success",
          message: "保存失败",
        })
      }
    })
  }
  
}

const shituClosed = () => {
  stVisible.value = false;
}

// 测距
const measuredDistance = () => {
  const curMapMode = getCurrentMapMode();
  if (curMapMode === mapModeEnum["3D"]) {
    Map3D.measure3D.activateMeasure3DControl("DIS");
  } else if (curMapMode === mapModeEnum["2D"]) {
    Map2D.measure.clear();
    Map2D.measure.drawPolyline();
  }
};
// 测面积
const measuredArea = () => {
  const curMapMode = getCurrentMapMode();
  if (curMapMode === mapModeEnum["3D"]) {
    Map3D.measure3D.activateMeasure3DControl("AREA");
  } else if (curMapMode === mapModeEnum["2D"]) {
    Map2D.measure.clear();
    Map2D.measure.drawPolygon();
  }
};

// 测高
const measuredHeight = () => {
  const curMapMode = getCurrentMapMode();
  if (curMapMode === mapModeEnum["3D"]) {
    Map3D.measure3D.activateMeasure3DControl("HEIGHT");
  } else {
    ElMessage({
      type: "info",
      message: "二维地图不支持测高",
    });
  }
};

// 清除
const clear = () => {
  const curMapMode = getCurrentMapMode();
  if (curMapMode === mapModeEnum["3D"]) {
    Map3D.measure3D.deActiveAllMeasure3DControl();
  } else if (curMapMode === mapModeEnum["2D"]) {
    Map2D.measure.clear();
  }
};

const kyAddRef = ref(null)
const kyAddVisible = ref(false)
const openKyAddDialog = (data) => {
  kyAddVisible.value = true;
  nextTick(()=>{
    kyAddRef.value.open('add').setData(data)
  })
  
}
const addKy = () =>{
  //关闭右键弹框
  Map2D.map.closePopup();
  Map2D.kyFlag = true;
  Map2D.addEventsToMap();
}
const yp = (data) =>{
  console.log(data);
  window.TOOL.data.set('ypData',data)
  window.Map2D.clearRect()
  wrjClearLayer()
  fsClearLayer()
  eventBus.emit("timeline",{index:1})
  eventBus.emit("addDevice",true)
  //关闭右键弹框
  Map2D.map.closePopup();
  router.push('navigationDeception')
}
const clearRect = () => {
   window.Map2D.clearRect()
   //关闭右键弹框
  Map2D.map.closePopup();
}
const gr = () =>{
  window.Map2D.clearRect()
  wrjClearLayer()
  fsClearLayer()
  eventBus.emit("timeline",{index:1})
  eventBus.emit("addDevice",true)
  //关闭右键弹框
  Map2D.map.closePopup();
  router.push('signalInterference')
}

const curMapMode = getCurrentMapMode();
const map3dStore = useMap3DStore();
const map2dStore = useMap2DStore();
const fxData = ref([])
const path = ref([])
const droneEntity = ref(null)
// const ty = (data) => {
//   //关闭右键弹框
//   Map2D.map.closePopup();
//   map2dStore.changeWidthAndHeight("0", "0"); // 隐藏二维地图
//   map3dStore.changeWidthAndHeight("100%", "100%"); // 展示三维地图
//   //清除三维推演
//     window.Map3D.wrjFly.clearAllLayers()
//     window.eventBus.emit("mapChange",true)
//     window.eventBus.emit("tcChange",true)
//   window.API.wxdzc.getUavDetectMsgByModelSerialRq({
//     model:data.model,
//     rq:data.rq,
//     serial:data.serial
//   }).then(res=>{
//     if(res.success){
//       fxData.value = res.result.uavDetectMsgList;
//       console.log(curMapMode);
//       // 初始化无人机轨迹和模型
//       if(fxData.value && fxData.value.length){
//         // fxData.value.forEach(item=>{
//         //   item.heading = 115;
//         // })
//         // Map3D.wrjFly.getAirRoute(fxData.value);
//         path.value = Map3D.wrjFly.generateDronePath(fxData.value);
//         droneEntity.value = Map3D.wrjFly.createDroneModel(fxData.value);
//         Map3D.wrjFly.DronePlaybackController(
//           path.value, 
//           droneEntity.value,
//           res.result.wjbdWrjJbxx
//         );
//         window.Map3D.wrjFly.togglePlayPause(true)
//       }
//     }
//   })
// }

// 清除图层
const wrjClearLayer = () => {
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
const fsClearLayer = () => {
  if (
      window.fsMarkerLayer != undefined &&
      window.fsMarkerLayer != null &&
      window.fsMarkerLayer != ""
  ) {
    // 清空图层
    window.fsMarkerLayer.clearLayers();
  }
};

const mbfxVisible = ref(false)
const mbfxTool = ref(false)
const mbfxRef = ref(null)
const mbfxJd = ref(0)
const mbfxWd = ref(0)
const getMbfx = (jd,wd) =>{
  //关闭右键弹框
  Map2D.map.closePopup();
  mbfxJd.value = jd;
  mbfxWd.value = wd;
  mbfxVisible.value = true;
  nextTick(()=>{
    mbfxRef.value.open(jd,wd)
  })
}
const mbfxJl = ref(0)
const mbfxType = ref("")
const mbfxSuccess = (jl,data,type) =>{
  mbfxJl.value = jl;
  mbfxType.value = type;
  zbmbAddMap(data)
}
let mbfx2dMarkerLayer = null;
const zbmbIcon = ref(null)
const tbVisible = ref(false)
const tbRef = ref(null)
const zbmbAddMap = (data) => {
  console.log(data);
  mbfxTool.value = true;
       clearLayer1()
    mbfx2dMarkerLayer = window.L.layerGroup([]);
  mbfx2dMarkerLayer.addTo(window.Map2D.map);

 
  data.zymbxx.forEach((item) => {
    if(mbfxType.value=="综合兵要"){
      if(item.dxdmlx=="地形"){
        zbmbIcon.value = window.L.icon({
          iconUrl: "/static/map_img/地形.png",
          iconSize: [40, 40],
        });
      }else if(item.dxdmlx=="地貌"){
        zbmbIcon.value = window.L.icon({
          iconUrl: "/static/map_img/地貌.png",
          iconSize: [40, 40],
        });
      }
    }else{
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
      const html = `<div style="width:160px;background:rgba(30, 32, 44);padding:10px">
                  <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                    <div style="width:100%;margin: 4px 0;color:#fff;display:flex;">名称：<div style="width：calc(100% - 60px);color:#fff;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;">${item.mc || item.dmmc}</div></div>
                
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
        console.log(e);
        if(mbfxType.value == "综合兵要"){
          dxdmDetail(item);
        }else{
          mbfxDetail(item);
        }
        
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
      const radius = mbfxJl.value * 1000; // 圆的半径
      const bound = getCriclePoints(center, radius);
      window.L.polygon(bound, { color: "#ef0303" }).addTo(mbfx2dMarkerLayer);
      const circleMarker = window.L.marker(window.L.latLng(Number(mbfxWd.value), Number(mbfxJd.value))).addTo(mbfx2dMarkerLayer);
      circleMarker.on("click", function (e) {
        console.log(e);
        
        if(mbfxType.value == "周边目标"){
          tbVisible.value = true;
          nextTick(()=>{
            tbRef.value.open([data])
          })
        }
        
      });
}
const tbClose = () => {
  tbVisible.value = false;
}

const mbfxMapClose = () =>{
  mbfxTool.value = false;
  clearLayer1()
}
const mbfxDetailRef = ref(null)
//无人机详细数据
const mbfxDetail = (data) => {
  nextTick(()=>{
    mbfxDetailRef.value.open(data)
  })
}
// 清除图层
const clearLayer1 = () => {
  console.log(mbfx2dMarkerLayer);
  if (
    mbfx2dMarkerLayer != undefined &&
    mbfx2dMarkerLayer != null &&
    mbfx2dMarkerLayer != ""
  ) {
    // 清空图层
    mbfx2dMarkerLayer.clearLayers();
  }
};
const mbfxClose = () =>{
  mbfxVisible.value = false;
}

const getKx = () =>{
  //关闭右键弹框
  Map2D.map.closePopup();
  //清除之前的框选
  if(Map2D.clearRect){
    Map2D.clearRect();
  }
  //清除框选标记图层
  if(Map2D.kxMarkerLayer){
    Map2D.kxMarkerLayer.clearLayers();
  }
  Map2D.kxFlag = true;
}

const getXd = () =>{
  //关闭右键弹框
  Map2D.map.closePopup();
  Map2D.xdFlag = true;
}

const gjfxcsVisible = ref(false);
const gjfxcsRef = ref(null)
const gjfxJd = ref("")
const gjfxWd = ref("")
const getGjfx = (jd,wd,flag) => {
  console.log(flag);
  //关闭右键弹框
  Map2D.map.closePopup();
  gjfxJd.value = jd;
  gjfxWd.value = wd;
  
  gjfxcsVisible.value = true;
  nextTick(()=>{
    gjfxcsRef.value.open(!flag)
  })
}
const gjfxRef = ref(null)
// const gjfxcsSuccess = (sj,sd,sdFlag) => {
//   let jd="",wd="";
//   if(!sdFlag){
//     const wrjData = window.TOOL.data.get('wrjData')?window.TOOL.data.get('wrjData'):{};
//     sd=wrjData.speed;
//     jd=wrjData.longitude;
//     wd=wrjData.latitude;
//   }else{
//     jd = gjfxJd.value;
//     wd = gjfxWd.value;
//   }
//   nextTick(()=>{
//     gjfxRef.value.open(sj,sd,true,jd,wd)
//   })
// }
const gjfxcsSuccess = (sj,sd,sdFlag,jd,wd,jd2,wd2) => {
  nextTick(()=>{
    gjfxRef.value.open(sj,sd,sdFlag,jd,wd,jd2,wd2)
  })
}
const gjfxcsClose = () => {
  gjfxcsVisible.value = false;
}


const dxdmVisible = ref(false)
const dxdmRef = ref(null)
const dxdmJd = ref(0)
const dxdmWd = ref(0)
const getDxdm = (jd,wd) =>{
  //关闭右键弹框
  Map2D.map.closePopup();
  dxdmJd.value = jd;
  dxdmWd.value = wd;

  dxdmVisible.value = true;
  nextTick(()=>{
    dxdmRef.value.open(jd,wd)
  })
}
const dxdmClose = () =>{
  dxdmVisible.value = false;
}
const dxdmJl = ref(0)
const dxdmSuccess = (jl,data) =>{
  dxdmJl.value = jl;
  dxdmAddMap(data)
}

const dxdmTool = ref(false)
const dxdmMapClose = () =>{
  dxdmTool.value = false;
  clearLayer2()
}

let dxdm2dMarkerLayer = null;
const dxdmIcon = ref(null)
const dxdmAddMap = (data) => {
  dxdmTool.value = true;
       clearLayer2()
    dxdm2dMarkerLayer = window.L.layerGroup([]);
  dxdm2dMarkerLayer.addTo(window.Map2D.map);
 
  data.forEach((item) => {
   if(item.dxdmlx=="地形"){
       dxdmIcon.value = window.L.icon({
        iconUrl: "/static/map_img/地形.png",
        iconSize: [40, 40],
      });
    }else{
      dxdmIcon.value = window.L.icon({
        iconUrl: "/static/map_img/地貌.png",
        iconSize: [40, 40],
      });
    }
    if(item.jd && item.wd){
      const marker = window.L.marker(
        window.L.latLng(Number(item.wd), Number(item.jd)),
        {
          icon: dxdmIcon.value,
        }
      ).addTo(dxdm2dMarkerLayer);
      // const innerHTML = "名称: " + item.MC + "<br>";
      // innerHTML += "经度: " + item.JD + "<br>";
      // innerHTML += "纬度: " + item.WD + "<br>";
      const html = `<div style="width:140px;background:rgba(30, 32, 44);padding:10px">
                  <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                    <div style="width:100%;margin: 4px 0;color:#fff;">名称：<span style="color:#fff;">${item.dmmc}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">类型：<span style="color:#fff;">${item.dxdmlx}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">经度：<span style="color:#fff;">${item.jd}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">纬度：<span style="color:#fff;">${item.wd}</span></div>
                  </div>
                </div>`;
      marker
        // bindTooltip
        .bindPopup(item.dmmc)
        .bindTooltip(html)
        // .openPopup(marker.getLatLng());
      marker.on("click", function (e) {
        console.log(e);
        dxdmDetail(item);
      });

       // // 使用turf.js计算距离
      const distance = turf.distance([Number(item.wd), Number(item.jd)], [dxdmWd.value, dxdmJd.value]);
      console.log(distance);
      
      const polyLine = L.polyline([[Number(item.wd), Number(item.jd)], [Number(dxdmWd.value), Number(dxdmJd.value)]], {color: "red"}).addTo(dxdm2dMarkerLayer);
      // 计算中间点坐标
        const midpointWd = Number(item.wd) + (Number(dxdmWd.value) - Number(item.wd)) / 2,
            midpointJd = Number(item.jd) + (Number(dxdmJd.value) - Number(item.jd)) / 2;
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
      ).addTo(dxdm2dMarkerLayer);
    }
  });

  
    const center = [Number(dxdmWd.value), Number(dxdmJd.value)];
      const radius = dxdmJl.value * 1000; // 圆的半径
      const bound = getCriclePoints(center, radius);
      const circleMarker = window.L.polygon(bound, { color: "#ef0303" }).addTo(dxdm2dMarkerLayer);
      window.L.marker(window.L.latLng(Number(dxdmWd.value), Number(dxdmJd.value))).addTo(dxdm2dMarkerLayer);
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

const dxdmDetailRef = ref(null)
//无人机详细数据
const dxdmDetail = (data) => {
  nextTick(()=>{
    dxdmDetailRef.value.open(data)
  })
}
// 清除图层
const clearLayer2 = () => {
  console.log(dxdm2dMarkerLayer);
  if (
    dxdm2dMarkerLayer != undefined &&
    dxdm2dMarkerLayer != null &&
    dxdm2dMarkerLayer != ""
  ) {
    // 清空图层
    dxdm2dMarkerLayer.clearLayers();
  }
};
</script>

<style scoped lang="less">
* {
  margin: 0;
  padding: 0;
}
.largeContent {
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  #bigScreen {
    width: 100%;
    height: 100%;
    // background: url("@/assets/allImage/bigBack.png") no-repeat;
    // background-size: 100% 100%;
    overflow: hidden;
    z-index: 5;
    pointer-events: none;
    position: absolute;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    top:0;
    .cont {
      width: 100vw;
      height: 100vh;
      position: relative;
      z-index: 5;
      pointer-events: none;
    }
  }
}
.clTools{
  display: flex;
    position: absolute;
    top: -10px;
    right: 330px;
    width: 160px;
    justify-content: space-around;
    align-items: center;
    cursor:pointer;
    pointer-events:auto;
    z-index:8;
    color:#00a5ff;
}
/* 四周闪烁层样式 */
    .flash-border {
      position: fixed;
      background-color: red;
      z-index: 9999;
      pointer-events: none;
      opacity: 0;
      transition: opacity 0.25s ease-in-out;
    }

    /* 上边框 */
    .top {
      top: 0;
      left: 0;
      width: 100%;
      height: 20px;
    }

    /* 下边框 */
    .bottom {
      bottom: 0;
      left: 0;
      width: 100%;
      height: 20px;
    }

    /* 左边框 */
    .left {
      top: 0;
      left: 0;
      height: 100%;
      width: 20px;
    }

    /* 右边框 */
    .right {
      top: 0;
      right: 0;
      height: 100%;
      width: 20px;
    }
    ::v-deep .leaflet-popup-content-wrapper {
  // background: transparent;
  
  background: rgba(48, 54, 72, 0.85);
  border: 1px solid #707070 !important;
}
// ::v-deep .leaflet-popup-content{
//   width:150px !important;
// }
::v-deep .leaflet-popup-tip {
  background: rgba(48, 54, 72, 0.85);
  border: 1px solid #707070 !important;
}
::v-deep .leaflet-container a {
  color: #fff !important;
}
::v-deep .leaflet-container a:hover {
  color: #ff990c !important;
  cursor: pointer;
}
::v-deep .leaflet-popup-content-wrapper {
  color: #fff !important;
}
::v-deep .cd-span {
  padding: 8px 0 !important;
  display: flex;
}
::v-deep .cd-span img {
  width: 20px !important;
  padding-right: 5px;
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
}


.scBox{
    width: 450px;
    height: 800px;
    background: url("@/assets/allImage/dialogBg.png") no-repeat;
    background-size: 100% 100%;
    position: absolute;
    left: 20px;
    top:68px;
    padding: 10px;
    pointer-events: auto;
    color:#fff;
  }
  .icons{
    color:#fff;
    font-size:20px;
  }
  :deep(.icons .el-icon){
    margin-left:10px;
    cursor:pointer;
  }
</style>
