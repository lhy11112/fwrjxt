<template>
<el-dialog 
    title="飞行预测"
    v-model="visibleDialogs"
    width="25%"
    style="height: 30%; margin-top: 10%"
    destroy-on-close
    :draggable="true"
    :modal="false"
    :close-on-click-modal="false"
    @close="closed">
    <div>
      {{ms}}
    </div>
  </el-dialog>
</template>
<script setup>
import { ref, onMounted,defineExpose } from "vue";
import startmarker from "/public/static/startmarker.png"
import endmarker from "/public/static/endmarker.png"

const visibleDialogs = ref(false);
const ms = ref("");
const closed = () => {
  visibleDialogs.value=false;
  // entities.value.forEach(entity => {
  //   if (entity.cesiumEntity) {
  //     window.Map3D.viewer.entities.remove(entity.cesiumEntity);
  //   }
  // });
  if(lineEntities.value && lineEntities.value.length){
   lineEntities.value.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
  }
  clearLayerGjfxMap2D()
  window.Map2D.xdMarkerLayerRemove()
}
const entities = ref([])
const lineEntities = ref([])
let gjfx2dMarkerLayer = null;
const open = (data,data2,data3,data4,data5,data6,data7) =>{
  const wrjData = window.TOOL.data.get('wrjData')?window.TOOL.data.get('wrjData'):{};
  visibleDialogs.value = true;
  let speed = 0;
  if(data3){
    speed = data2;
  }else{
    speed = wrjData.speed;
  }
  console.log(data,data2,data3,data4,data5,data6,data7);
  const distance = turf.distance([Number(data5),Number(data4)], [Number(data7), Number(data6)]);
   
  ms.value = `根据无人机当前速度${speed}m/s，从起点(${data5},${data4})到终点(${data7},${data6}),预测飞行总时间为${(distance * 1000 / speed).toFixed(2)}秒（${(distance * 1000 / speed / 60 / 60).toFixed(2)}时）；`

  // 从地图移除所有实体
  // if(entities.value && entities.value.length){
  //   entities.value.forEach(entity => {
  //     if (entity.cesiumEntity) {
  //       window.Map3D.viewer.entities.remove(entity.cesiumEntity);
  //     }
  //   });
  // }
  if(lineEntities.value && lineEntities.value.length){
   lineEntities.value.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
  }
          
    if(data3){
      clearLayerGjfxMap2D()
      gjfx2dMarkerLayer = window.L.layerGroup([]);
      gjfx2dMarkerLayer.addTo(window.Map2D.map);
      // const center = [Number(data5), Number(data4)];
      // const radius = data * 60 * speed; // 圆的半径
      // const bound = getCriclePoints(center, radius);
      // const circleMarker = window.L.polygon(bound, { color: "#ef0303" }).addTo(gjfx2dMarkerLayer);
      // window.L.marker(window.L.latLng(Number(data5), Number(data4))).addTo(gjfx2dMarkerLayer);
      window.L.marker([Number(data5),Number(data4)], { icon: L.icon({ iconSize: [32, 48], iconUrl: startmarker }) }).addTo(gjfx2dMarkerLayer)
      window.L.marker([Number(data7), Number(data6)], { icon: L.icon({ iconSize: [32, 48], iconUrl: endmarker }) }).addTo(gjfx2dMarkerLayer)

      L.polyline([[Number(data5),Number(data4)],[Number(data7), Number(data6)]]).addTo(gjfx2dMarkerLayer);

      const polyLine = L.polyline([[Number(data5), Number(data4)], [Number(data7), Number(data6)]], {color: "red"}).addTo(gjfx2dMarkerLayer);
      // 计算中间点坐标
        const midpointWd = Number(data5) + (Number(data7) - Number(data5)) / 2,
            midpointJd = Number(data4) + (Number(data6) - Number(data4)) / 2;
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
      ).addTo(gjfx2dMarkerLayer);
    }else{
      // entities.value =[{"id":1761614303513,"type":"circle","name":"活动范围","lon":wrjData?wrjData.longitude:0,"lat":wrjData?wrjData.latitude:0,"radius":data * 60 * speed,"minHeight":0,"maxHeight":0,"shapeType":"restricted","color":"#ff0000","showLabel":true}];
      // // 创建Cesium实体并添加到地图
      // entities.value.forEach(newEntity=>{
      //   addEntityToMap(newEntity);
      // })
      // 定义两个点的经纬度和高度（单位：米）
        const position1 = Cesium.Cartesian3.fromDegrees(Number(data4), Number(data5),0);
        const position2 = Cesium.Cartesian3.fromDegrees(Number(data6), Number(data7),0);

        // 添加连接两点的线
        const lineEntity=window.Map3D.viewer.entities.add({
          polyline: {
            positions: [position1, position2],
            width: 2,
            material: Cesium.Color.RED
          }
        });
        lineEntities.value.push(lineEntity)
    }
    
    
}

// 清除图层
const clearLayerGjfxMap2D = () => {
  if (
    gjfx2dMarkerLayer != undefined &&
    gjfx2dMarkerLayer != null &&
    gjfx2dMarkerLayer != ""
  ) {
    // 清空图层
    gjfx2dMarkerLayer.clearLayers();
  }
};
defineExpose({ open });

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
</script>

<style scoped>

</style>
