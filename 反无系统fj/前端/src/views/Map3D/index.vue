<template>
    <div :id="mapId" v-show="map3dStore.show" :style="{'width': map3dStore.width, 'height': map3dStore.height, 'left': map3dStore.left, 'top': map3dStore.top,'overflow':'hidden'}"></div>
    
    <!-- 移动气泡 -->
		<div id="test" class="sm-div-graphic" >
			<div class="divpoint">
        <el-icon style="position:absolute;right:0;top:0;cursor:pointer;" @click="popupClose"><Close /></el-icon>
				<div class="label-wrap">
					<div class="label-content">
						<div class="data-li">
              <img  src="/static/map_img/gjfx.png" style="width:20px;height:20px;margin-right:5px;"/>
							<div class="data-label" @click="gjfx">飞行预测</div>
						</div>
						<div class="data-li">
              <img  src="/static/map_img/zymb.png" style="width:20px;height:20px;margin-right:5px;"/>
							<div class="data-label" @click="mbfx">目标分析</div>
						</div>
            <div class="data-li">
              <img  src="/static/map_img/zymb.png" style="width:20px;height:20px;margin-right:5px;"/>
							<div class="data-label" @click="dxdm">综合兵要</div>
						</div>
					</div>
				</div>
			</div>
		</div>

    <div class="mapTool" v-if="mbfxTool">
    目标分析
    <el-icon style="cursor:pointer" @click="mbfxMapClose"><Close /></el-icon>
  </div>
    <div class="mapTool" v-if="dxdmTool" style="right:199px">
      综合兵要
      <el-icon style="cursor:pointer" @click="dxdmMapClose"><Close /></el-icon>
    </div>
  <div style="pointer-events: none;">
    <gjfxcsDialog v-if="gjfxcsVisible" ref="gjfxcsRef" @success="gjfxcsSuccess"  @closed="gjfxcsClose"></gjfxcsDialog>
  <gjfxDialog ref="gjfxRef"></gjfxDialog>
  <mbfxDialog v-if="mbfxVisible" ref="mbfxRef" @success="mbfxSuccess" @closed="mbfxClose"></mbfxDialog>
  <dxdmDialog v-if="dxdmVisible" ref="dxdmRef" @success="dxdmSuccess" @closed="dxdmClose"></dxdmDialog>
  </div>
</template>

<script setup>
import gjfxcsDialog from "../bottomTitle/gjfxcs.vue";
import gjfxDialog from "../bottomTitle/gjfx.vue";
import mbfxDialog from "../bottomTitle/mbfx.vue";
import dxdmDialog from "../bottomTitle/dxdm.vue";
import { onMounted,nextTick,ref} from "vue";
import { useMap3DStore } from "@/store/modules/map3D";
const mapId = "map3D";
const map3dStore = useMap3DStore();

const initMap = () => {
    Map3D.init(mapId);
    if(window.config.VUE_APP_SUPERMAP_BASE_FLAG){
        Map3D.imageLayer.addLayer("/map-multiTiles/rest/maps/wj_gis_聚合影像", "影像", {autoSetView: false});
    }
    // if(window.TOOL.data.get("PKTY_USER_INFO") && window.TOOL.data.get("PKTY_USER_INFO").wd && window.TOOL.data.get("PKTY_USER_INFO").jd){
    //     Map3D.setSCeneView(Number(window.TOOL.data.get("PKTY_USER_INFO").jd),Number(window.TOOL.data.get("PKTY_USER_INFO").wd));
    // }
}
const popupClose = () => {
  Map3D.popupClose()
}
const entities = ref([])
const gjfxcsRef = ref(null)
const gjfxcsVisible = ref(false);
const gjfx = () => {
    Map3D.popupClose()
    gjfxcsVisible.value = true;
    nextTick(()=>{
      gjfxcsRef.value.open()
    })
    // 从地图移除所有实体
    //       entities.value.forEach(entity => {
    //         if (entity.cesiumEntity) {
    //           window.Map3D.viewer.entities.remove(entity.cesiumEntity);
    //         }
    //       });
    // const wrjData = window.TOOL.data.get('wrjData')?window.TOOL.data.get('wrjData'):{};
    // entities.value =[{"id":1761614303513,"type":"circle","name":"活动范围","lon":wrjData.longitude?Number(wrjData.longitude):0,"lat":wrjData.latitude?Number(wrjData.latitude):0,"radius":500,"minHeight":0,"maxHeight":1,"shapeType":"restricted","color":"#ff0000","showLabel":true}];
    // // 创建Cesium实体并添加到地图
    // entities.value.forEach(newEntity=>{
    //   addEntityToMap(newEntity);
    // })
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
const mbfxRef = ref(null)
const mbfxVisible = ref(false)
const mbfxEntities = ref([])
const mbfx = () => {
    Map3D.popupClose()
    mbfxVisible.value = true;
    nextTick(()=>{
      mbfxRef.value.open()
    })
    
}
const mbfxSuccess = (e,e2) => {
  mbfxVisible.value = false;
  mbfxTool.value = true;
  // 从地图移除所有实体
          mbfxEntities.value.forEach(entity => {
            console.log('xxxxx111',entity);
            if (entity.cesiumEntity) {
              window.Map3D.viewer.entities.remove(entity.cesiumEntity);
            }
          });
    const wrjData = window.TOOL.data.get('wrjData')?window.TOOL.data.get('wrjData'):{};
    console.log(wrjData);
    mbfxEntities.value =[{"id":2345,"type":"circle","name":"目标分析范围","lon":wrjData.longitude?Number(wrjData.longitude):0,"lat":wrjData.latitude?Number(wrjData.latitude):0,"radius":Number(e) *1000,"minHeight":0,"maxHeight":1,"shapeType":"restricted","color":"#ff0000","showLabel":true}];
    console.log(mbfxEntities.value);
    // 创建Cesium实体并添加到地图
    mbfxEntities.value.forEach(newEntity=>{
      addEntityToMap(newEntity);
    })
    zbmbAddMap(e2)
}
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

const mbfxTool = ref(false)
const mbfxMapClose = () =>{
  mbfxTool.value = false;
  clearLayer1()
}
// 清除图层
const clearLayer1 = () => {
   mbfxEntities.value.forEach(entity => {
            console.log('xxxxx111',entity);
            if (entity.cesiumEntity) {
              window.Map3D.viewer.entities.remove(entity.cesiumEntity);
            }
          });
          zbmbEntities.value.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
      lineEntities.value.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
      labelEntities.value.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
};
const dxdmRef = ref(null)
const dxdmVisible = ref(false)
const dxdmEntities = ref([])
const dxdmJl = ref(30)
const dxdm = () => {
  Map3D.popupClose()
    dxdmVisible.value = true;
    nextTick(()=>{
      dxdmRef.value.open()
    })
     
}
const dxdmSuccess = (e,e2) => {
  console.log(e);
  dxdmVisible.value = false;
  dxdmTool.value = true;
  // 从地图移除所有实体
if( dxdmEntities.value &&  dxdmEntities.value.length){
  dxdmEntities.value.forEach(entity => {
            if (entity.cesiumEntity) {
              window.Map3D.viewer.entities.remove(entity.cesiumEntity);
            }
          });
}
          
    const wrjData = window.TOOL.data.get('wrjData')?window.TOOL.data.get('wrjData'):{};
    dxdmEntities.value =[{"id":1234,"type":"circle","name":"综合兵要范围","lon":wrjData.longitude?Number(wrjData.longitude):0,"lat":wrjData.latitude?Number(wrjData.latitude):0,"radius":Number(e) *1000,"minHeight":0,"maxHeight":1,"shapeType":"restricted","color":"#ff0000","showLabel":true}];
    // 创建Cesium实体并添加到地图
    dxdmEntities.value.forEach(newEntity=>{
      addEntityToMap(newEntity);
    })
    dxdmAddMap(e2)
}


const dxdmDataEntities = ref([])
const dxdmlineEntities = ref([])
const dxdmlabelEntities = ref([])
const dxdmAddMap = (data) => {
   // 在地图上添加无人机标记
      dxdmDataEntities.value.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
      dxdmlineEntities.value.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
      dxdmlabelEntities.value.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
      dxdmDataEntities.value = [];

      let zbmbData = [];
      if(data && data.length){
        data.forEach(item=>{
          let obj={}
          obj.id=item.id;
          obj.name = item.dmmc;
          obj.lon = Number(item.jd);
          obj.lat = Number(item.wd);
          obj.altitude = item.altitude?Number(item.altitude):20;
          obj.icon = item.dxdmlx =='地形'?'/static/map_img/地形.png':'/static/map_img/地貌.png';
          zbmbData.push(obj)
        })
      }
      


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

        dxdmDataEntities.value.push(entity);

        const wrjData = window.TOOL.data.get('wrjData')?window.TOOL.data.get('wrjData'):{};
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
        dxdmlineEntities.value.push(lineEntity)

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
        
        dxdmlabelEntities.value.push(labelEntity)
      });

       
}


const dxdmTool = ref(false)
const dxdmMapClose = () =>{
  dxdmTool.value = false;
  clearLayer2()
}
const clearLayer2 = () => {
    // 从地图移除所有实体
if( dxdmEntities.value &&  dxdmEntities.value.length){
  dxdmEntities.value.forEach(entity => {
            if (entity.cesiumEntity) {
              window.Map3D.viewer.entities.remove(entity.cesiumEntity);
            }
          });
}
  dxdmDataEntities.value.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
      dxdmlineEntities.value.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
      dxdmlabelEntities.value.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
}
const dxdmClose = (e) => {
  dxdmVisible.value = false;
  dxdmJl.value = e;
}
onMounted(() => {
  
   nextTick(()=>{
    initMap();
   })
})
</script>

<style>
#map3D {
    margin: 0;
    padding: 0;
    position: absolute;
}
.sm-div-graphic {
	position: absolute;
	color: #fff;
	font-size: 14px;
    z-index:1 !important;
    pointer-events: auto !important;
    background:#0e2d4e;
    border-radius:10px;
}
#test .divpoint {
	padding:10px;
  box-sizing: border-box;
}
.data-li{
  display: flex;
    align-items: center;
}
.data-li{
  margin-top:10px;
}
.data-li:nth-of-type(1){
  margin-top:0;
}
.data-label{
  cursor:pointer;
}
.data-label:hover{
  color:#108ee9;
}
.sm-compass{
  top:52px;
}
.sm-zoom{
  top:180px;
}
.mapTool{
      position: absolute;
    top:51px;
    right:100px;
    z-index: 111;
    display: flex;
    background: #253c8e;
    color: #fff;
    align-items:center;
    padding:5px 10px;
}
</style>
