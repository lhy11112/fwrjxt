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
                node-key="mbsbm"
                :props="defaultztProps"
                @node-click="handleNodeClick"
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
    <mbfxDetailDialog title="重要目标信息" ref="mbfxDetailRef"></mbfxDetailDialog>
    <mbfxZtDetailDialog title="重要目标信息" ref="mbfxZtDetailRef"></mbfxZtDetailDialog>
    
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import microApp from "@/components/microApp/index.vue";
import { ref, onMounted,onUnmounted,nextTick } from "vue";
import icon1 from "@/assets/leftTitle/leftImg.png"
import wxdzcsb from "@/assets/allImage/wxdzcsb.png"
import scEditor from "@/components/scEditor/index1.vue";
import mbfxDetailDialog from "../../dpCommon/mbfxDetail.vue"
import mbfxZtDetailDialog from "./mbfxZtDetail.vue"
import { ElMessage } from "element-plus";
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
  label: "mc",
  id: "id",
});
const defaultExpandedkeys = ref([])
const defaultztProps = ref({
  children: "children",
  label: "mbmc",
  id: "mbsbm",
});
const defaultZtExpandedkeys = ref([])
const treeBdData = ref([])
const treeZtData = ref([])
let mbfx2dMarkerLayer = null;
// 初始化
onMounted(()=>{
  getZtData()
  getAllZtData()
  nextTick(()=>{
    mbfx2dMarkerLayer = window.L.markerClusterGroup([]);
    mbfx2dMarkerLayer.addTo(window.Map2D.map);
  })
})
onUnmounted(()=>{
  clearLayerMap2D()
  zbmbEntities.value.forEach(entity => {
    window.Map3D.viewer.entities.remove(entity);
  });
})
const handleNodeClick = (data) => {
  console.log(data);
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
  // else{
  //   ElMessage.warning('该目标没有经纬度！')
  // }
  
};
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
  window.API.ztzymb.leftTree().then(res=>{
    if (res.code == 200) {
      var list = Object.keys(res.result);
      treeZtData.value=[]
      var data = []
      list.forEach((item, index) => {
        data.push({
          mbmc: item,
          mbsbm: `FJ${index}`,
          children: res.result[item],
        });
      });

      treeZtData.value = [
        {
          mbmc: '福建省',
          mbsbm: `FJ`,
          children:data
        }
      ]
      defaultZtExpandedkeys.value = ['FJ']
      // console.log(treeZtData.value);
    }
  })
}
const getAllZtData = () =>{
  window.API.ztzymb.list({
    pageNo:1,
    pageSize:100000
  }).then(res=>{
    if (res.code == 200) {
      zbmbZtAddMap2D(res.result.records)
      zbmb3DAddMap(res.result.records,"zt")
    }
  })
}
// 点击上图
const zbmbZtAddMap2D = (data) => {
    clearLayerMap2D();
    for (var i of data) {
      var myIcon = L.icon({
        iconUrl: "/static/map_img/重要目标.png", // '/static/mapDataIcon/' + iconName + '.png',
        // iconSize: [30, 20],
        iconAnchor: [16, 20],
        data: i,
      });
      var innerHTML = `
        <div style="width:100%;display:flex;flex-wrap: wrap;">
          <div style="width:100%;margin: 4px 0;">目标名称：<span style="color:orange;">${i.mbmc}</span></div>
          <div style="width:100%;margin: 4px 0;">经度：<span style="color:orange;">${i.jd}</span></div>
          <div style="width:100%;margin: 4px 0;">纬度：<span style="color:orange;">${i.wd}</span></div>
        </div>`;
      var points = [Number(i.wd), Number(i.jd)];
      L.marker(points, {
          icon: myIcon,
        })
          .bindTooltip(innerHTML)
          .addTo(mbfx2dMarkerLayer)
          .on("click", (e) => {
            console.log(e.target.options.icon.options.data);
            mbfxZtDetail(e.target.options.icon.options.data)
          });
    }
 
};
const mbfxZtDetailRef = ref(null)
//无人机详细数据
const mbfxZtDetail = (data) => {
  nextTick(()=>{
    mbfxZtDetailRef.value.open(data)
  })
}
const zbmbIcon = ref(null)
const zbmbAddMap2D = (data) => {
       clearLayerMap2D()

 
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
      ).addTo(mbfx2dMarkerLayer);
      // const innerHTML = "名称: " + item.MC + "<br>";
      // innerHTML += "经度: " + item.JD + "<br>";
      // innerHTML += "纬度: " + item.WD + "<br>";
      const html = `<div style="width:140px;background:rgba(30, 32, 44);padding:10px">
                  <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                    <div style="width:100%;margin: 4px 0;color:#fff;">名称：<span style="color:#fff;">${item.mc}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">经度：<span style="color:#fff;">${item.jd}</span></div>
                    <div style="width:100%;margin: 4px 0;color:#fff;">纬度：<span style="color:#fff;">${item.wd}</span></div>
                  </div>
                </div>`;
      marker
        // bindTooltip
        .bindPopup(item.mc)
        .bindTooltip(html)
        // .openPopup(marker.getLatLng());
      marker.on("click", function (e) {
        console.log(e);
        mbfxDetail(item);
      });

    }
  });
}

// 清除图层
const clearLayerMap2D = () => {
  if (
    mbfx2dMarkerLayer != undefined &&
    mbfx2dMarkerLayer != null &&
    mbfx2dMarkerLayer != ""
  ) {
    // 清空图层
    mbfx2dMarkerLayer.clearLayers();
  }
};
const mbfxDetailRef = ref(null)
//无人机详细数据
const mbfxDetail = (data) => {
  nextTick(()=>{
    mbfxDetailRef.value.open(data)
  })
}
// 获取页面数据
const loading = ref(false);
const getData = () => {
  loading.value = true;
  window.API.zbmb.list({
    pageNo:1,
    pageSize:1000000,
  }).then((res) => {
    if (res.code == 200) {
      loading.value = false;
      treeBdData.value = [
        {
          id:'1',
          mc:'福建省',
          children:res.result.records
        }
      ];
      defaultExpandedkeys.value = ['1']
      zbmbAddMap2D(res.result.records)
      zbmb3DAddMap(res.result.records,"bd")
    }
  });
};

const zbmbEntities = ref([])
// 创建一个集合来存储所有无人机的 ID
const droneIds = new Set();
const zbmb3DAddMap = (data,type) => {
       // 在地图上添加无人机标记
      zbmbEntities.value.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
      
      zbmbEntities.value=[]
      let zbmbData = [];
      data.forEach(item=>{
        let obj={}
        obj.id=item.id;
        
        obj.lon = Number(item.jd);
        obj.lat = Number(item.wd);
        obj.altitude = item.altitude?Number(item.altitude):20;
        if(type=="bd"){
          obj.name = item.mc;
          obj.icon = item.mc.indexOf('医院')!=-1?'/static/map_img/医院.png':item.mc.indexOf('学校')!=-1?'/static/map_img/高等院校.png':item.mc.indexOf('加油站')!=-1?'/static/map_img/加油站.png':'/static/map_img/重要目标.png';
        }else{
          obj.name = item.mbmc;
          obj.icon = '/static/map_img/重要目标.png';
        }
        
        zbmbData.push(obj)
      })
      

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
        
        // 将无人机 ID 存入集合中
        droneIds.add(drone.id);

        
        
      }) 
      // // 监听 viewer 的点击事件
      //   window.Map3D.viewer.onClick.addEventListener((event) => {
      //       // 检查被点击的实体是否是无人机
      //       if (event.entity && droneIds.has(event.entity.id)) {
      //           console.log('Clicked on drone:', event.entity.id);
      //           // 在这里添加你想要执行的代码
      //       }
      //   });
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