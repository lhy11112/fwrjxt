<template>
<AnalysisMoveDlg
    title="防控区域"
    style="width: 60%;height:60vh"
    :visibleDialog="visibleDialog"
    @close="closed"
    :isModal="false"
  >
    <div class="tableBox">
              <el-table
                :data="tableData"
                style="width: 100%; height: 100%"
                class="custom-table"
                @selectionChange="selectionChange"
                @row-click="rowClickChange"
                v-loading="loading"
                ref="multipleTableRef"
              >
                <el-table-column type="selection" width="50"></el-table-column>
                <el-table-column
                  v-for="(item, index) in columnData"
                  :prop="item.prop"
                  :label="item.label"
                  :key="index"
                  :width="item.width"
                >
                  <template v-if="item.prop == 'lx'" #default="scope">
                    {{scope.row.lx=="restricted"?'预警圈':scope.row.lx=="prohibited"?'反制圈':scope.row.lx=="danger"?'警戒圈':'允许飞行区域'}}
                  </template>
                  <template v-else-if="item.prop == 'xz'" #default="scope">
                    {{scope.row.xz=="circle"?'圆形':scope.row.xz=="rectangle"?'矩形':scope.row.xz=="polygon"?'多边形':scope.row.xz}}
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <!-- <div>
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
            </div> -->
            <!-- <div class="create-bottom">
      
        <el-button @click="closed">取 消</el-button>

        <el-button  type="primary" @click="lookKy()"
          >确定</el-button
        >
      </div> -->
  </AnalysisMoveDlg>
</template>
<script setup>
import { ref, onMounted,defineExpose,defineEmits,nextTick } from "vue";
import { useMap3DStore } from "@/store/modules/map3D";
import { useMap2DStore } from "@/store/modules/map2D";
import { mapModeEnum, getCurrentMapMode } from "@/utils/Map/mapMode";

const map3dStore = useMap3DStore();
const map2dStore = useMap2DStore();
const visibleDialog = ref(false);
const form = ref({})
const emits = defineEmits(["closed","success"]);

const multipleTableRef = ref(null)
const open = () =>{
  visibleDialog.value = true;
  getData()
  
}
defineExpose({ open });


onMounted(()=>{
  nextTick(()=>{
    eventBus
  })
})
const closed = () => {
  visibleDialog.value = false;
  emits("closed");
}

const selection = ref([])
const selectionChange = (e) =>{
  selection.value = e;
  
  window.TOOL.data.set("fkqyData",selection.value)
  lookKy()
}

const polygonArr = ref([])
const entities = ref([])
const lookKy = () => {
     
      entities.value.forEach(entity => {
            if (entity.cesiumEntity) {
              window.Map3D.viewer.entities.remove(entity.cesiumEntity);
            }
            if (entity.labelEntity) {
              window.Map3D.viewer.entities.remove(entity.labelEntity);
            }
          });
      entities.value =[];
      window.Map2D.fkqyLayerRemove()
      polygonArr.value=[]
      let kyData = [];
      let data=[];
      // data.push(row);
      data = selection.value;
        if(data && data.length){
          data.forEach(item=>{
            for(let i=0;i<3;i++){
              if(i==0){
                let obj={}
                Object.assign(obj,item);
                obj.mc="反制圈";
                obj.lx="prohibited";
                obj.bj=item.jfqbj;
                obj.ys=item.jfqys;
                kyData.push(obj)
              }else if(i==1){
                let obj={}
                Object.assign(obj,item);
                // obj.sjmc = item.mc;
                obj.mc="警戒圈";
                obj.lx="prohibited";
                obj.bj=item.yjqbj;
                obj.ys=item.yjqys;
                kyData.push(obj)
              }else if(i==2){
                let obj={}
                Object.assign(obj,item);
                obj.mc="预警圈";
                kyData.push(obj)
              }
            }
          })
        }

        const curMapMode = getCurrentMapMode();

        // if (curMapMode === mapModeEnum["3D"]) {
          
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
            // if(index%3==2){
              obj.showLabel=true;
            // }else{
            //   obj.showLabel=false;
            // }
            entities.value.push(obj);
          })
          console.log(entities.value);
          // 创建Cesium实体并添加到地图
          entities.value.forEach((newEntity,index)=>{
            addEntityToMap(newEntity);
            if(index==entities.value.length-1){
              // 视角调整：垂直向下看
              window.viewer.camera.flyTo({
                  destination: Cesium.Cartesian3.fromDegrees(newEntity.lon, newEntity.lat, 5000),
                  orientation: {
                      heading: Cesium.Math.toRadians(0.0),
                      pitch: Cesium.Math.toRadians(-90.0), 
                      roll: 0.0
                  }
              });
            }
          })

          

        // }else if (curMapMode === mapModeEnum["2D"]) {
          
          kyData.forEach((item,index)=>{
            if(index==kyData.length-1){
              window.Map2D.map.flyTo([item.zxdwd,item.zxdjd],12)
            }
            const center = [item.zxdwd,item.zxdjd]
            const radius = item.bj; //圆的半径
            const bound = getCriclePoints(center, radius);
            
            const polygon = window.L.polygon(bound, { color: item.ys}).addTo(window.fkqyLayer);
            if(index%3==1){
              polygonArr.value.push({sjmc:item.id,polygon:polygon})
            }
            
            
            // 创建带有文字的标记
            const marker = window.L.marker(bound[0], {
                icon: window.L.divIcon({
                    html: `<div>${item.mc}${item.bj / 1000}Km</div>`,
                    iconSize: [20, 20],
                    iconAnchor: [20, 25], // 设置图标在标记点的显示位置
                }),
            }).addTo(window.fkqyLayer);
          })
        }

        

        
    // }
    const addNewLineBetweenChars = (str) => {
        // 先校验输入，避免非字符串类型报错
        if (typeof str !== 'string') {
            return '';
        }
        // 拆分每个字符，再用\n拼接
        return str.split('').join('\n');
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

    const columnData = ref([
        { prop: "mc", label: "空域名称"},
        { prop: "lx", label: "空域类型" },
        { prop: "xz", label: "区域形状" },
        { prop: "zxdjd", label: "中心点经度" },
        { prop: "zxdwd", label: "中心点纬度" },
        { prop: "bj", label: "半径" },
        { prop: "jfqbj", label: "反制圈半径(米)" },
        { prop: "jfqys", label: "反制圈颜色" },
        { prop: "yjqbj", label: "警戒圈半径(米)" },
        { prop: "yjqys", label: "警戒圈颜色" },
        { prop: "ys", label: "颜色" },
    ])
    const tableData = ref([])
    const pageOption = ref({
        pageNo:1,
        pageSize:10000
      })
      const loading = ref(false)
      const total = ref(0)
    const getData = () => {
      loading.value=true;
      const params = Object.assign({},pageOption.value)
      window.API.wrjky.list(params).then(res=>{
        if(res.code == 200){
          tableData.value = res.result.records;
          total.value = res.result.total;
          let fkqyData = window.TOOL.data.get("fkqyData");
          if(fkqyData && fkqyData.length){
            nextTick(()=>{
                fkqyData.forEach(item => {
                  // 确保item存在于tableData中
                  const rowIndex = tableData.value.findIndex(row => row.id === item.id);
                  if (rowIndex !== -1) {
                    multipleTableRef.value.toggleRowSelection(tableData.value[rowIndex], true);
                  }
                });
            })
          }
        }
        loading.value=false;
      })
    }
    

    const addEntityToMap = (entity) => {
      // 根据实体类型创建不同的Cesium实体
      let cesiumEntity;
      let labelEntity;
      
            const centerPosition = window.Cesium.Cartesian3.fromDegrees(entity.lon, entity.lat)
            const centerCartographic = Cesium.Cartographic.fromCartesian(centerPosition);
      // 2. 计算文字位置 (在圆圈内部右侧)
            // 策略：距离中心的距离 = 半径 * 0.85 (即在圆圈 85% 的位置，靠右)
            const distanceFromCenter = Number(entity.radius) * 0.85;
            // 计算经度偏移量 (考虑纬度对经度距离的影响)
            // 地球平均半径约 6378137 米
            const longitudeOffset = distanceFromCenter / (6378137.0 * Math.cos(centerCartographic.latitude));
            console.log(centerCartographic.longitude);
            
            const labelPosition = Cesium.Cartesian3.fromRadians(
                centerCartographic.longitude + longitudeOffset, 
                centerCartographic.latitude
            );

      
      switch (entity.type) {
        case 'circle':
          cesiumEntity = window.viewer.entities.add({
            position: centerPosition,
            ellipse: {
              semiMinorAxis: entity.radius,
              semiMajorAxis: entity.radius,
              // height: entity.minHeight,
              // extrudedHeight: entity.maxHeight,
              material: new Cesium.ColorMaterialProperty(
                window.Cesium.Color.fromCssColorString(entity.color).withAlpha(0.15)
              ),
              outline: true,
                    outlineColor: Cesium.Color.fromCssColorString(entity.color).withAlpha(1),
                    outlineWidth: 2,
                    height: 1000,
            }
          });

          labelEntity = window.viewer.entities.add({
            position: labelPosition,
            
            label: {
              text: `${addNewLineBetweenChars(entity.name)}\n${entity.radius / 1000}km`,
              show: entity.showLabel,
             font: 'bold 14px Microsoft YaHei', // 加粗字体更像截图
                    fillColor: Cesium.Color.WHITE,
                    outlineColor: Cesium.Color.BLACK,
                    outlineWidth: 3,
                    style: Cesium.LabelStyle.FILL_AND_OUTLINE,
                    verticalOrigin: Cesium.VerticalOrigin.CENTER,
                    horizontalOrigin: Cesium.HorizontalOrigin.LEFT, // 以文字左侧为基准
                    heightReference: Cesium.HeightReference.CLAMP_TO_GROUND,
                    disableDepthTestDistance: Number.POSITIVE_INFINITY
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
      entity.labelEntity = labelEntity;
      
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
</script>

<style scoped>
.create-bottom {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
}
.tableBox{
  width:100%;
  height:calc(100% - 50px);
}
</style>
