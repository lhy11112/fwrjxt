<template>
  <div class="topCenter">
    <!-- <el-form>
      <el-col>
        <el-form-item>
          <scEditor v-model="form.nr" style="width: 100%; height: 435px;pointer-events: auto;background-color:#2d81d0;" />
        </el-form-item>
      </el-col>
    </el-form> -->
    <div class="page-tree">
      <!-- <div>
          <el-input
            v-model="treeMc"
            style="width: 98%; margin-bottom: 10px"
            @keyup.enter="search"
            :placeholder="'请输入'"
          />
        </div> -->
       
          <el-tabs v-model="activeName" type="border-card" style="height:100%;" @tab-click="tabClick">
            <el-tab-pane label="中台" key="zt" name="zt">
              <el-tree
                ref="treeZtRef"
                :data="treeZtData"
                node-key="id"
                :props="defaultztProps"
                @node-click="handleNodeClick"
                @node-expand="handleExpandClick"
                :check-strictly="true"
                :highlight-current="true"
                :default-expanded-keys="defaultZtExpandedkeys"
                clearable
              >
                <!-- default-expand-all -->
                <!--         :check-on-click-node="true" -->
              </el-tree>
            </el-tab-pane>
            <el-tab-pane label="本地" key="bd" name="bd">
              <el-tree
                ref="treeBdRef"
                :data="treeBdData"
                node-key="id"
                :props="defaultProps"
                @node-click="handleNodeClick"
                :check-strictly="true"
                :highlight-current="true"
                :default-expanded-keys="defaultExpandedkeys"
                clearable
              >
                <!-- default-expand-all -->
                <!--         :check-on-click-node="true" -->
              </el-tree>
            </el-tab-pane>
          </el-tabs>
        
    </div>
    <!-- <div class="pageContent-box">
      <scEditor v-model="form.nr" style="width: 100%; height:100%;pointer-events: auto;background-color:transparent;" />
    </div> -->
  </div>
  <dxdmDetailDialog ref="dxdmDetailRef"></dxdmDetailDialog>
</template>

<script setup>
import { useRouter } from "vue-router";
import microApp from "@/components/microApp/index.vue";
import { ref, onMounted,onUnmounted,nextTick } from "vue";
import icon1 from "@/assets/leftTitle/leftImg.png"
import wxdzcsb from "@/assets/allImage/wxdzcsb.png"
import scEditor from "@/components/scEditor/index1.vue";
import dxdmDetailDialog from "../../dpCommon/dxdmDetail.vue"
// 定义路由
const router = useRouter();
const wxdsbVisible = ref(false)
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
const titles = ref([
  {title:'',key:'bxpz'}
])
const form = ref({})
const activeName = ref("zt")

const treeMc = ref("");
// 获取数据
const treeBdRef = ref(null);
const treeZtRef = ref(null);
// tree数据显示处理
const defaultProps = ref({
  children: "children",
  label: "dmmc",
  id: "id",
});
const defaultExpandedkeys = ref([])
const defaultztProps = ref({
  children: "children",
  label: "dmmc",
  id: "id",
});
const defaultZtExpandedkeys = ref([])
const treeBdData = ref([])
const treeZtData = ref([])
let dxdm2dMarkerLayer = null;
// 初始化
onMounted(()=>{
  getZtData()
  getAllZtData()
  nextTick(()=>{
    dxdm2dMarkerLayer = window.L.markerClusterGroup([]);
    dxdm2dMarkerLayer.addTo(window.Map2D.map);
  })
})
onUnmounted(()=>{
  clearLayer2()
  dxdmEntities.value.forEach(entity => {
    window.Map3D.viewer.entities.remove(entity);
  });
})
const handleNodeClick = (data) => {
  if(data.wd && data.jd){
    window.Map2D.map.flyTo([Number(data.wd),Number(data.jd)],14)

    window.Map3D.viewer.scene.camera.setView({
          destination: window.Cesium.Cartesian3.fromDegrees(Number(data.jd), Number(data.wd), 1000),
          orientation: {
              heading: 0,
              roll: 0,
          },
      });
  }
};
const handleExpandClick = (data,data2) => {
  if(data2.level==2 && data){
    nextTick(()=>{
      getZtChildData(data)
    })
  }
}
const tabClick  = (node) => {
  console.log(node.props);
  if(node.props.name=='bd'){
    getData()
  }else{
    getZtData()
    getAllZtData()
  }
};
const getZtData = () =>{
  window.API.ztdxdm.listType().then(res=>{
    if (res.code == 200) {
      treeZtData.value = []
      let data = []
      res.result.forEach((item, index) => {
        data.push({
          dmmc: item.dxdmlx,
          id: `Dx${index}`,
          children: [{}],
        });
      });
      treeZtData.value = [
        {
          dmmc:'福建省',
          id:`Dx`,
          children:data
        }
      ]
      defaultZtExpandedkeys.value = ['Dx']
      console.log(treeZtData.value);
    }
  })
}

const getZtChildData = (data) => {
  data.children=[]
  window.API.ztdxdm.list({ pageNo:1,pageSize:10000,dxdmlx: data.dmmc }).then((res) => {
          if (res.code == 200) {
            res.result.records.forEach(item=>{
              let obj={}
              obj.dmmc=item.dmmc;
              obj.id = item.id;
              obj.jd=item.jd;
              obj.wd=item.wd;
              obj.children = [];
              data.children.push(obj)
            })
          }
        });
}
const getAllZtData = () =>{
  window.API.ztdxdm.list({
    pageNo:1,
    pageSize:100000
  }).then(res=>{
    if (res.code == 200) {
      dxdmZtAddMap(res.result.records)
      dxdm3DAddMap(res.result.records,'zt');
    }
  })
}

// 点击上图
const dxdmZtAddMap = (data) => {
    clearLayer2();
    
    for (var i of data) {
      var myIcon = L.icon({
        iconUrl: `/static/mapDataIcon/${
          i.dxdmlx == "水库"
            ? "地形地貌"
            : i.dxdmlx == "河流"
            ? "河流段"
            : i.dxdmlx == "山地"
            ? "山脉"
            : "山脉"
        }.png`,
        // iconSize: [30, 20],
        iconAnchor: [16, 20],
        data: i,
      });
      var innerHTML = `
        <div style="width:100%;display:flex;flex-wrap: wrap;">
            <div style="width:100%;margin: 4px 0;">名称：<span style="color:orange;">${i.dmmc}</span></div>
            <div style="width:100%;margin: 4px 0;">类型：<span style="color:orange;">${i.dxdmlx}</span></div>
            <div style="width:100%;margin: 4px 0;">经度：<span style="color:orange;">${i.jd}</span></div>
            <div style="width:100%;margin: 4px 0;">纬度：<span style="color:orange;">${i.wd}</span></div>
        </div>`;
      var points = [Number(i.wd), Number(i.jd)];
      L.marker(points, {
        icon: myIcon,
      })
        .bindTooltip(innerHTML)
        .addTo(dxdm2dMarkerLayer)
        .on("click", (e) => {
          let detailData = e.target.options.icon.options.data;
          detailData.mj = detailData.ms;
          dxdmDetail(detailData);
        });
    }
  
};
// 获取页面数据
const loading = ref(false);
const getData = () => {
  loading.value = true;
  window.API.dxdm.list({
    pageNo:1,
    pageSize:1000000,
  }).then((res) => {
    if (res.code == 200) {
      loading.value = false;
      treeBdData.value = [
        {
          id:'1',
          dmmc:'福建省',
          children:res.result.records
        }
      ];
      defaultExpandedkeys.value = ['1']
      dxdmAddMap(res.result.records);
      dxdm3DAddMap(res.result.records,'bd');
    }
  });
};


const dxdmIcon = ref(null)
const dxdmAddMap = (data) => {
       clearLayer2()
    
 
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
                    <div style="width:100%;margin: 4px 0;color:#fff;">名称：<span style="color:orange;">${item.dmmc}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">类型：<span style="color:orange;">${item.dxdmlx}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">经度：<span style="color:orange;">${item.jd}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">纬度：<span style="color:orange;">${item.wd}</span></div>
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

    }
  });
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

const dxdmDetailRef = ref(null)
//无人机详细数据
const dxdmDetail = (data) => {
  nextTick(()=>{
    dxdmDetailRef.value.open(data)
  })
}



const dxdmEntities = ref([])
const dxdm3DAddMap = (data,type) => {
   // 在地图上添加无人机标记
      dxdmEntities.value.forEach(entity => {
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
          if(type=="bd"){
            obj.icon = item.dxdmlx =='地形'?'/static/map_img/地形.png':'/static/map_img/地貌.png';
          }else{
            obj.icon = `/static/mapDataIcon/${
              item.dxdmlx == "水库"
                ? "地形地貌"
                : item.dxdmlx == "河流"
                ? "河流段"
                : item.dxdmlx == "山地"
                ? "山脉"
                : "山脉"
            }.png`;
          }
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

       
      });

       
}
</script>

<style scoped lang="less">
// @import "@/style/dialog2.css";
.topCenter {
  width: 100vw;
  height: 80.4vh;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  pointer-events: none;
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

  .page-tree{
    width: 30%;
    height:100%;
    background: url("@/assets/allImage/dialogBg.png") no-repeat;
    background-size: 100% 100%;
    position: absolute;
    left: 30px;
    top:20px;
    padding: 10px;
    pointer-events: auto;
  }
  .pageContent-box{
     width:74%;
    height:100%;
    background: url("@/assets/allImage/dialogBg.png") no-repeat;
    background-size: 100% 100%;
    position: absolute;
    left: calc(22% + 48px);
    top:20px;
    padding: 10px;
    pointer-events: auto;
    color:#fff;
    overflow:hidden;
    overflow-y:auto;
  }


}
/* 分页样式 */
.el-pager li {
  background: transparent;
  border: 1px solid rgba(115, 116, 117);
  color: #fff;
  margin: 0 5px;
}

.el-pager li.is-active {
  background: rgba(255, 153, 12);
  border: 1px solid rgba(255, 153, 12);
  color: #fff;
}

.el-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 5px;
}
.el-pagination__total{
  color:#fff;
}
.el-pagination__jump{
  color:#fff;
}
.el-pagination button{
  background: transparent;
  border: 1px solid #fff;
}
.el-pagination .btn-next .el-icon, .el-pagination .btn-prev .el-icon{
  color:#fff;
}
.el-pagination button.is-disabled, .el-pagination button:disabled{
  background: transparent;
  border: 1px solid #fff;
}
.el-pagination button:hover,.el-pagination button:hover .el-icon{
  color:#409eff;
}
:deep(.el-tree-node__content){
  background:transparent;
}
:deep(.el-tree--highlight-current .el-tree-node.is-current>.el-tree-node__content){
  background-color: #44678f;
}
:deep(.el-tree){
  font-size:16px;
}
:deep(.el-tabs--border-card){
  background-color:#1b4d7e;
}
:deep(.el-tabs__content){
  height:calc(100% - 60px);
  overflow:hidden;
  overflow-y:auto;
}
// :deep(.el-tabs--border-card>.el-tabs__content){
//   background-color:#1b4d7e;
// }
</style>