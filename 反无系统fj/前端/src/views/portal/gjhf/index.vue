<template>
</template>

<script setup>
import microApp from "@/components/microApp/index.vue";
import { ref, onMounted,watch,nextTick,onUnmounted } from "vue";
import icon1 from "@/assets/leftTitle/leftImg.png"
import wxdzcsb from "@/assets/allImage/wxdzcsb.png"
import {ElMessage} from "element-plus";
import { useMap3DStore } from "@/store/modules/map3D";
import { useMap2DStore } from "@/store/modules/map2D";
import { mapModeEnum, getCurrentMapMode } from "@/utils/Map/mapMode";
import { useRouter,useRoute } from "vue-router";

const route = useRoute() 
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
const map3dStore = useMap3DStore();
const map2dStore = useMap2DStore();
const gjInfo = ref({})
let clusterMarkersRadius = null;
const polygonArr = ref([])
watch(
  () => route,
  (newVal) => {
    nextTick(() => {
      console.log(newVal);
      if(newVal.query){
        gjInfo.value = newVal.query;
        console.log(gjInfo.value);
        nextTick(()=>{
          // addPoint()
          // getKy()
          getHf()
          gjAddMap()
        })
      }
      
    });
  },
  { immediate: true }
);
// 初始化
onMounted(()=>{
  map2dStore.changeWidthAndHeight("0", "0"); // 隐藏二维地图
  map3dStore.changeWidthAndHeight("100%", "100%"); // 展示三维地图
  // window.eventBus.on("gj",(msg_txt)=>{
  //     // console.log(msg_txt);
  //     animateCircle(msg_txt)
  //     createAndPlayAudio('/static/audio.wav');
  //   })
  window.eventBus.emit('hfKy',true)
})
onUnmounted(()=>{
  radiusClearLayer()
  Map3D.wrjFly.clearAllLayers()
  window.eventBus.emit('hfKy',false)
})

//空域
   const getKy = () => {
      radiusClearLayer()
      polygonArr.value=[]

      clusterMarkersRadius = window.L.layerGroup([]);
      clusterMarkersRadius.addTo(window.Map2D.map);
      let entities =[];
      window.API.wrjky.list({
        pageNo:1,
        pageSize:1000,
        id:gjInfo.value.kyid
      }).then(res=>{
        let data = res.result.records;
        console.log(data);
        let kyData = [];
        if(data && data.length){
          data.forEach(item=>{
            for(let i=0;i<3;i++){
              if(i==0){
                let obj={}
                Object.assign(obj,item);
                obj.mc="禁飞区";
                obj.lx="prohibited";
                obj.bj=3000;
                obj.ys="#ff0000";
                kyData.push(obj)
              }else if(i==1){
                let obj={}
                Object.assign(obj,item);
                // obj.sjmc = item.mc;
                obj.mc="预警区";
                obj.lx="prohibited";
                obj.bj=5000;
                obj.ys="#ffff00";
                kyData.push(obj)
              }else if(i==2){
                kyData.push(item)
              }
            }
          })
        }
        console.log(kyData);
        kyData.forEach((item,index)=>{
          // if(index==0){
          //   window.Map2D.map.flyTo([item.zxdwd,item.zxdjd])
          // }
          const center = [item.zxdwd,item.zxdjd]
          const radius = item.bj; //圆的半径
          const bound = getCriclePoints(center, radius);
          
          const polygon = window.L.polygon(bound, { color: item.ys}).addTo(clusterMarkersRadius);
          if(index%3==1){
            polygonArr.value.push({sjmc:item.id,polygon:polygon})
          }
          
          
          // 创建带有文字的标记
          const marker = window.L.marker(bound[0], {
              icon: window.L.divIcon({
                  html: `<div>${item.mc}</div>`,
                  iconSize: [20, 20],
                  iconAnchor: [20, 25], // 设置图标在标记点的显示位置
              }),
          }).addTo(clusterMarkersRadius);
        })

        kyData.forEach((item,index)=>{
          let obj={};
          obj.type=item.xz;
          obj.name=item.mc;
          obj.lon=item.zxdjd;
          obj.lat=item.zxdwd;
          obj.radius=item.bj;
          obj.minHeight=item.zxgd;
          obj.maxHeight=item.zdgd;
          obj.shapeType=item.lx;
          obj.color=item.ys;
          obj.id = item.id;
          // obj.pixelOffset=item.bj;
          if(index%3==2){
            obj.showLabel=true;
          }else{
            obj.showLabel=false;
          }
          entities.push(obj);
        })
console.log(entities);
        // 创建Cesium实体并添加到地图
        entities.forEach(newEntity=>{
          addEntityToMap(newEntity);
        })
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
    const animateCircle = (msg_txt) => {
      // 设置初始透明度为完全不透明
        polygonArr.value.forEach(item=>{
          item.polygon.setStyle({ opacity:1 });
        })
        

        // 定义闪烁参数：间隔时间（ms）和闪烁次数
        const flashInterval = 500; // 每500毫秒闪烁一次
        const flashCount = 20; // 闪烁20次

        // 创建闪烁动画
        let flashIndex = 0;
        const flashIntervalId = setInterval(() => {
            // 在每次间隔中，改变透明度
            if (flashIndex < flashCount) {
              // console.log(this.polygon);
              polygonArr.value.forEach(item=>{
                item.polygon.setStyle({ opacity:flashIndex % 2 === 0 ? 1 : 0.5 });
              })
                flashIndex++;
            } else {
                // 停止间隔并恢复初始透明度
                clearInterval(flashIntervalId);
                polygonArr.value.forEach(item=>{
                  item.polygon.setStyle({ opacity:1 });
                })
            }
        }, flashInterval);
    }
    
    // 清除图层
    const radiusClearLayer = () =>{
      if (
          clusterMarkersRadius != undefined &&
          clusterMarkersRadius != null &&
          clusterMarkersRadius != ""
        ) {
          // 清空图层
          clusterMarkersRadius.clearLayers();
        }
    }
    // 获取地图上的范围圈
    const getCriclePoints = (center, radius) => {
      const bound = [];
      const earthRadius = 6378137; //地球的半径
      const dlat = (radius / earthRadius) * (180 / Math.PI);
      const dlng = dlat / Math.cos((center[0] * Math.PI) / 180);
      for (let i = 0; i < 360; i++) {
        const red = (i * Math.PI) / 180;
        const lat = center[0] + dlat * Math.sin(red);
        const lng = center[1] + dlng * Math.cos(red);
        bound.push([lat, lng]);
      }
      return bound;
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
              pixelOffset: new window.Cesium.Cartesian2(0,-50)
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
              pixelOffset: new window.Cesium.Cartesian2(0, -50),
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
          
          cesiumEntity = window.viewer.entities.add({
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
              pixelOffset: new window.Cesium.Cartesian2(0, -50)
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
        console.log(rotation)
      // 简化的矩形坐标计算，实际项目中可能需要更精确的计算
      const widthDeg = width / 111319.9; // 米转度的近似值
      const lengthDeg = length / 111319.9;
      
      return {
        west: centerLon - widthDeg / 2,
        east: centerLon + widthDeg / 2,
        south: centerLat - lengthDeg / 2,
        north: centerLat + lengthDeg / 2
      };
    }
const fxData = ref([])
const path = ref(null)
const droneEntity = ref([])
const rowData = ref({})
const getHf = () => {
  
  window.API.wrj.list({
    pageNo:1,
    pageSize:100,
    id:gjInfo.value.wrjid
  }).then(ress=>{
    if(ress.code==200){
      let data = ress.result.records;
      if(data.length==0){
        return
      }
      window.API.wxdzc.getUavDetectMsgByModelSerialRq({
        model:data[0].model,
        serial:data[0].serialNumber
      }).then(res=>{
        if(res.success){
          fxData.value = res.result.uavDetectMsgList;
          if(res.result.wjbdWrjJbxx){
            rowData.value = res.result.wjbdWrjJbxx;
            rowData.value.serial = data.serial;
            // rowData.value.rq = rq.value
            
            rowData.value.color = 'yellow';
            rowData.value.weight = 3;
          }else{
            rowData.value = {
              brand:data.brand,
              authStatus:data.authStatus,
              model:data.model,
              serialNumber:data.serial,
              serial:data.serial,
              // rq:rq.value
              color:'yellow',
              weight:3
            }
          }
          // 初始化无人机轨迹和模型
          if(fxData.value && fxData.value.length){
            // fxData.value.forEach(item=>{
            //   item.heading = 115;
            // })
            // Map3D.wrjFly.getAirRoute(fxData.value);
            path.value = Map3D.wrjFly.generateDronePath(fxData.value);
            droneEntity.value = Map3D.wrjFly.createDroneModel(fxData.value);
            Map3D.wrjFly.DronePlaybackController(
              path.value, 
              droneEntity.value,
              rowData.value
            );
            window.Map3D.wrjFly.togglePlayPause(true)
          }
        }
      })
    }
  })
  
}

const gjMarkerLayer = ref(null)
const greenIcon = ref(null)
const addPoint = () => {
    clearLayer1();
    gjMarkerLayer.value = window.L.layerGroup([]);
    gjMarkerLayer.value.addTo(window.Map2D.map);
    const item = gjInfo.value;
   
      if (item.clzt == "已处理") {
          greenIcon.value = window.L.icon({
            iconUrl:'/static/gj1.png',
            iconSize: [25, 25],
          });
      }else{
          greenIcon.value = window.L.icon({
            iconUrl:'/static/gj2.png',
            iconSize: [25, 25],
          });
      }
      if (item.gjfsjd && item.gjfswd) {
        const marker = window.L.marker(
          window.L.latLng(Number(item.gjfswd), Number(item.gjfsjd)),
          {
            icon: greenIcon.value,
          }
        ).addTo(gjMarkerLayer.value);

        const html = `<div style="width:240px;background:rgba(30, 32, 44);padding:10px">
                    <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                      <div style="width:100%;margin: 4px 0;color:#fff;">空域：<span style="color:#fff;">${item.kyid_dictText}</span></div>
                      <div style="width:100%;margin: 4px 0;color:#fff;">无人机：<span style="color:#fff;">${item.wrjid_dictText}</span></div>
                      <div style="width:100%;margin: 4px 0;color:#fff;">告警类型：<span style="color:#fff;">${item.gjlx}</span></div>
                      <div style="width:100%;margin: 4px 0;color:#fff;">经度：<span style="color:#fff;">${item.gjfsjd.toFixed(3)}</span></div>
                      <div style="width:100%;margin: 4px 0;color:#fff;">纬度：<span style="color:#fff;">${item.gjfswd.toFixed(3)}</span></div>
                      <div style="width:100%;margin: 4px 0;color:#fff;">处理状态：<span style="color:#fff;">${item.clzt}</span></div>
                      <div style="width:100%;margin: 4px 0;color:#fff;">处理时间：<span style="color:#fff;">${item.clsj}</span></div>
                    </div>
                  </div>`;
        marker
          .bindPopup(item.gjlx)
          .bindTooltip(html);
        //   let that=this;
        // marker.on("click", function () {
        //   that.$refs.gjDetailRef.open(item)
        // });
        var  markerIcon = L.divIcon({
                  html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>${ite.wrjid_dictText+item.gjlx}</div>`,//marker标注
                  className: 'my-div-icon',
                  iconAnchor: [80, -20]//文字标注相对位置
                });
        window.L.marker(
          window.L.latLng(Number(item.gjfswd), Number(item.gjfsjd)),
          {
            icon: markerIcon,
          }
        ).addTo(gjMarkerLayer.value);
      }
  }
  // 清除图层
 const clearLayer1 = () => {
    if (
      gjMarkerLayer.value != undefined &&
      gjMarkerLayer.value != null &&
      gjMarkerLayer.value != ""
    ) {
      // 清空图层
      gjMarkerLayer.value.clearLayers();
    }
  }

  const zbmbEntities = ref([])
const gjAddMap = () => {
   // 在地图上添加无人机标记
      zbmbEntities.value.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
      zbmbEntities.value = [];

      let zbmbData = [];
      // if(data && data.length){
      //   data.forEach(item=>{
      //     let obj={}
      //     obj.id=item.id;
      //     obj.name = item.dmmc;
      //     obj.lon = Number(item.jd);
      //     obj.lat = Number(item.wd);
      //     obj.altitude = item.altitude?Number(item.altitude):20;
      //     obj.icon = item.dxdmlx =='地形'?'/static/map_img/地形.png':'/static/map_img/地貌.png';
      //     zbmbData.push(obj)
      //   })
      // }
      let obj={}
          obj.id=gjInfo.value.id;
          obj.name = gjInfo.value.wrjid_dictText+gjInfo.value.gjlx;
          obj.lon = Number(gjInfo.value.gjfsjd);
          obj.lat = Number(gjInfo.value.gjfswd);
          obj.altitude = gjInfo.value.altitude?Number(gjInfo.value.altitude):20;
          obj.icon = gjInfo.value.clzt == "已处理"?'/static/gj1.png':'/static/gj2.png';
          zbmbData.push(obj)
      


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
      });

       
}

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
      

</style>