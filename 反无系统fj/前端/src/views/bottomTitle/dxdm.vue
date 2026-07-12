<template>
<AnalysisMoveDlg
    title="综合兵要"
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
          <el-form-item label="分析范围(KM)" prop="jl">
            <el-input v-model="form.jl" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <div class="create-bottom">
      
        <el-button @click="closed">取 消</el-button>

        <el-button  type="primary" @click="submit()"
          >确定</el-button
        >
      </div>
  </AnalysisMoveDlg>
</template>
<script setup>
import { ref, onMounted,defineExpose,defineEmits } from "vue";

const visibleDialog = ref(false);
const form = ref({})
const emits = defineEmits(["closed","success"]);
const open = () =>{
  visibleDialog.value = true;
}
defineExpose({ open });

onMounted(()=>{

})
const dialogForm = ref({})
const zbmbData = ref([])
const submit = () => {
  dialogForm.value.validate(async (valid) => {
    if (valid) {
      window.API.dxdm.getDxdmByJwdAndJl({
        jd:window.TOOL.data.get('wrjData')?window.TOOL.data.get('wrjData').longitude:0,
        wd:window.TOOL.data.get('wrjData')?window.TOOL.data.get('wrjData').latitude:0,
        jl:form.value.jl
      }).then((res) => {
        if (res.code == 200) {
          visibleDialog.value = false;
          zbmbData.value = res.result;
          emits("success",form.value.jl,zbmbData.value)
          // dxdmAddMap(zbmbData.value)
        }
      });
    }
  });
}
const dxdmEntities = ref([])
const dxdmlineEntities = ref([])
const dxdmlabelEntities = ref([])
const dxdmAddMap = (data) => {
   // 在地图上添加无人机标记
      dxdmEntities.value.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
      dxdmlineEntities.value.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
      dxdmlabelEntities.value.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
      dxdmEntities.value = [];

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

        dxdmEntities.value.push(entity);

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

const closed = () => {
  visibleDialog.value = false;
  emits("closed",form.value.jl);
}

// window.eventBus.on("wrjWz",(e)=>{
//         zbmbData.value.forEach(drone =>{
//           // 定义两个点的坐标
//           const position1 = Cesium.Cartesian3.fromDegrees(e.longitude, e.latitude); // 例如，纽约市的坐标
//           const position2 = Cesium.Cartesian3.fromDegrees(drone.lon, drone.lat);
//           // 创建连接这两点的线
//           const polyline = window.Map3D.viewer.entities.add({
//               polyline: {
//                   positions: Cesium.Cartesian3.fromDegreesArrayHeights([
//                       position1.x, position1.y, position1.z,
//                       position2.x, position2.y, position2.z
//                   ]),
//                   width: 5,
//                   material: Cesium.Color.RED.withAlpha(0.5)
//               }
//           });

          
//           // 计算两点之间的距离
//           const distance = Cesium.Cartesian3.distance(position1, position2);
//           console.log(distance);
//         })
//       })
</script>

<style scoped>
.create-bottom {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
}
</style>
