<template>
  <div class="headerTitle">
    <div class="headerLeftContent">
      <div
        style="padding: 10px; color: #30a4ff; cursor: pointer"
        @click="scEvent"
      >
        收藏夹
      </div>
      <img
        class="line"
        src="../../assets/allImage/line.png"
        v-if="isServerFlag"
        alt=""
      />
      <el-popover
        v-if="isServerFlag"
        :visible="mapShow"
        placement="bottom"
        width="atuo"
        trigger="click"
      >
        <template #reference>
          <div
            style="padding: 10px; color: #30a4ff; cursor: pointer"
            @click="taskClickPicture"
          >
            <span>地图</span>
          </div>
        </template>
        <div style="max-height: 500px; overflow: auto" class="tc">
          <el-radio-group
            @change="mapEvent"
            v-model="mapType"
            style="margin-left: 5px"
          >
            <el-radio-button label="影像地图">影像地图</el-radio-button>
            <el-radio-button label="矢量地图">矢量地图</el-radio-button>
          </el-radio-group>
        </div>
      </el-popover>
      <div
        style="margin-left: 15px; margin-right: 10px"
        :class="currentIndex == 0 ? 'activeIndex' : 'activeNo'"
        @click="buttonClick(0)"
      >
        综合态势
      </div>
      <div
        :class="currentIndex == 1 ? 'activeIndex' : 'activeNo'"
        @click="buttonClick(1)"
      >
        指挥控制
      </div>
    </div>
    <div class="middleTop">{{ title }}</div>
    <div class="headerRightContent">
      <div
        :class="currentIndex == 2 ? 'activeIndex' : 'activeNo'"
        @click="buttonClick(2)"
      >
        模拟推演
      </div>
      <div
        style="margin-left: 10px"
        :class="currentIndex == 3 ? 'activeIndex' : 'activeNo'"
        @click="buttonClick(3)"
      >
        数据管理
      </div>
      <img class="lineOne" src="../../assets/allImage/line.png" alt="" />
      <!-- <div style="padding:10px;color:#30a4ff;cursor:pointer;" title="空域显示隐藏">
        <el-switch v-model="kyFlag" @change="kyChange"  inline-prompt active-text="显示空域" inactive-text="隐藏空域"></el-switch>
      </div> -->
      <div
        @click="getKy"
        style="padding: 10px; color: #30a4ff; cursor: pointer"
      >
        防控区域
      </div>

      <img class="line" src="../../assets/allImage/line.png" alt="" />
      <div
        style="padding: 10px; color: #30a4ff; cursor: pointer"
        @click="tcEvent"
      >
        图层
      </div>
      <img class="line" src="../../assets/allImage/line.png" alt="" />
      <div
        style="padding: 10px; color: #30a4ff; cursor: pointer"
        v-if="isMap2d"
        @click="change2dMap"
      >
        转换3D
      </div>
      <div
        style="padding: 10px; color: #30a4ff; cursor: pointer"
        v-if="!isMap2d"
        @click="change2dMap"
      >
        转换2D
      </div>
      <img class="line" src="../../assets/allImage/line.png" alt="" />
      <img
        class="image"
        src="../../assets/allImage/all.png"
        alt=""
        title="全屏"
        @click="toggleFullScreen"
      />
      <img class="line" src="../../assets/allImage/line.png" alt="" />
      <img
        class="image"
        src="../../assets/allImage/shouye.png"
        alt=""
        title="返回首页"
        @click="shouyeEvent"
      />
      <img class="line" src="../../assets/allImage/line.png" alt="" />
      <!-- <img class="imageLogin" @click="houtaiGanli" src="../../assets/allImage/close.png" alt="" /> -->
      <el-popover
        popper-class="theme-light"
        placement="bottom-start"
        :width="270"
        trigger="hover"
      >
        <template #reference>
          <div class="yh-btn" style="margin: 0 24px 0 20px">
            <el-icon size="20" color="#046fc5"><User /></el-icon>
            <!-- <span>{{ userInfo.deptName }}</span> -->
          </div>
        </template>
        <template #default>
          <div class="m-box">
            <div class="m-line">用户名：{{ userInfo.xm }}</div>
            <div class="m-line">单位：{{ userInfo.orgCodeTxt }}</div>
            <div class="m-line-btn-analysis" @click="houtaiGanli">
              <img
                @click="houtaiGanli"
                src="../../assets/allImage/close.png"
                alt=""
              />
              <span>退出</span>
            </div>
          </div>
        </template>
      </el-popover>
    </div>
  </div>

  <tcDialog v-if="tcVisible" ref="tcRef" @closed="tcClose"></tcDialog>
  <kyDialog v-if="kyVisible" ref="kyRef" @closed="kyClose"></kyDialog>
</template>

<script setup>
import { ref, onMounted, nextTick, watch,defineEmits } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { useMap3DStore } from "@/store/modules/map3D";
import { useMap2DStore } from "@/store/modules/map2D";
import { mapModeEnum, getCurrentMapMode } from "@/utils/Map/mapMode";
import { controlS3MLayer } from "@/utils/Map/layerOpHook.js";
import tcDialog from "./tc.vue";
import kyDialog from "./ky.vue";
import mapCenterPoint from "@/components/mapCenterPoint.json";
import { useCounterStoreMethods } from "@/store/modules/jwdu";

const countMessage = useCounterStoreMethods();
const emits = defineEmits("mapChangeEvent");
const router = useRouter();
const currentIndex = ref(0);
const props = defineProps({
  title: {
    default: "",
    type: String,
  },
});
const userInfo = ref({});
const isServerFlag = ref(false);
onMounted(() => {
  isServerFlag.value = window.config.VUE_APP_SUPERMAP_BASE_FLAG;
  
  
  nextTick(() => {
    // --- 初始加载 ---
  mapclusterMarkers = L.layerGroup().addTo(Map2D.map);
  if (window.config.VUE_APP_SUPERMAP_BASE_FLAG) {
    mapEvent();
  }
    

  });
  if (window.TOOL.data.get("USER_INFO")) {
    userInfo.value = window.TOOL.data.get("USER_INFO");
  }
  getTc();
  window.eventBus.on("tcChange", () => {
    pictureDataChange();
  });
  window.eventBus.on("hfKy", (data) => {
    kyFlag.value = data;
    kyChange();
  });

  window.eventBus.on("isMap2dEvent", (data) => {
    isMap2d.value = data;
  });
});

const scEvent = () => {
  window.eventBus.emit("shoucang");
};

const kyFlag = ref(false);
const kyChange = () => {
  window.eventBus.emit("kyChange", kyFlag.value);
};
// // 点击切换页面
const buttonClick = (index) => {
  currentIndex.value = index;
  eventBus.emit("clearAboutTc", true);
  window.TOOL.data.remove("wrjData");
  if (currentIndex.value == 0) {
    // 日常专题门户
    router.push({
      path: `/portal`,
    });
  } else if (currentIndex.value == 1) {
    // 演习演训专题门户
    router.push({
      path: `/zbxx`,
    });
  } else if (currentIndex.value == 2) {
    router.push({
      path: `/simulatedExercise`,
    });
  } else if (currentIndex.value == 3) {
    router.push({
      path: `/dataManage`,
    });
  }
};
// 判断地址栏路由
if (
  window.location.href.includes("portal") ||
  window.location.href.includes("wxdzc") ||
  window.location.href.includes("signalInterference") ||
  window.location.href.includes("navigationDeception")
) {
  currentIndex.value = 0;
} else if (
  window.location.href.includes("commandControl") ||
  window.location.href.includes("zbxx") ||
  window.location.href.includes("fwzf") ||
  window.location.href.includes("bxpz") ||
  window.location.href.includes("cesiumAir") ||
  window.location.href.includes("zymb") ||
  window.location.href.includes("zhby")
) {
  currentIndex.value = 1;
} else if (window.location.href.includes("simulatedExercise")) {
  currentIndex.value = 2;
} else if (window.location.href.includes("dataManage")) {
  currentIndex.value = 3;
}

const mapShow = ref(false);
const taskClickPicture = () => {
  mapShow.value = !mapShow.value;
};
const mapType = ref("影像地图");
const mapEvent = () => {
  clearLayer();
  Map2D.removeAllLayer();
  if (mapType.value == "影像地图") {
    eventBus.emit("mapType",0)
     new L.supermap.tiledMapLayer(window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-multiTiles/rest/maps/wj_gis_聚合影像",{
            transparent: true,
            subdomains: window.config.VUE_APP_SUPERMAPZYDK_BASE_URL,
            format:"webp"
        }).addTo(mapclusterMarkers)
        // 添加地图上的文字
        new L.supermap.tiledMapLayer(
            window.config.VUE_APP_SUPERMAP_BASE_URL +
            "/iserver/services/map-ugcv5-wj_gis_ZhongGuo_HangZhengJingJie/rest/maps/wj_gis_%E4%B8%AD%E5%9B%BD_%E8%A1%8C%E6%94%BF%E5%A2%83%E7%95%8C"
        ).addTo(mapclusterMarkers);
        // 添加地图上的公路线
        new L.supermap.tiledMapLayer(
            window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-ugcv5-wj_gis_ZhongGuo_GongLu/rest/maps/wj_gis_中国_公路"
            , {
                format: 'webp',
                attribution: false,
                subdomains: [8091, 8092, 8093]
            }
        ).addTo(mapclusterMarkers);
  } else {
    eventBus.emit("mapType",1)
    loadCities();
  }
};
const clearLayer = () => {
  if (
    mapclusterMarkers !== undefined &&
    mapclusterMarkers !== "" &&
    mapclusterMarkers !== null
  ) {
    mapclusterMarkers.clearLayers();
  }
};
let countyLayer = null;
let mapclusterMarkers = null;
let allCountiesData;
const mapStyle = {
  color: "#20e4fe", // 边框颜色
  weight: 2, // 边框粗细 (加粗)
  fillColor: "#0085f2", // 统一的填充色
  fillOpacity: 0.5, // 填充透明度
  // dashArray: "5,5,5,5", // 阴影样式，这里设置为5像素的实线和虚线交替
  // opacity: 0.5, // 阴影透明度
  // fill: true, // 确保阴影是填充的
  // dashOffset: [10,10,10,10], // 阴影的偏移量，这里设置为0
};
let cityLayer = null;
/**
 * 加载并显示市级地图
 */
const loadCities = () => {
  Map2D.map.setView([26, 119], 6);

  if (countyLayer) {
    mapclusterMarkers.removeLayer(countyLayer);
  }

  fetch("fujian_simplified.geojson")
    .then((response) => response.json())
    .then((data) => {
      cityLayer = L.geoJSON(data, {
        style: mapStyle, // 应用统一的样式
        onEachFeature: function (feature, layer) {
          layer
            .bindTooltip(feature.properties.Name, {
              permanent: true,
              direction: "center",
              className: "label-style",
            })
            .openTooltip();

            layer.on("dblclick", function (e) {
              console.log(e);
              
              const cityCode = e.target.feature.properties.code;
              console.log(cityCode);
              loadCounties(cityCode)
              setTimeout(()=>{
                try {
                  mapclusterMarkers.fitBounds(e.target.getBounds());
                } catch (error) {
                  console.log(error)
                }
              },20)
              
              
              // loadCounties(cityCode);
            });
        },
      }).addTo(mapclusterMarkers);

      // Map2D.map.fitBounds(cityLayer.getBounds());
      // 设置地图的最大层级和最小层级
    })
    .catch((error) => console.error("加载市级数据失败：", error));
  fetch("taiwan_very_simplified.geojson")
    .then((response) => response.json())
    .then((data) => {
      cityLayer = L.geoJSON(data, {
        style: mapStyle, // 应用统一的样式
        onEachFeature: function (feature, layer) {
          layer
            .bindTooltip(feature.properties.Name, {
              permanent: true,
              direction: "center",
              className: "label-style",
            })
            .openTooltip();

          layer.on("dblclick", function (e) {
            console.log(e);
            
            L.DomEvent.stopPropagation(e);
            Map2D.map.fitBounds(e.target.getBounds());
            const cityCode = e.target.feature.properties.code;
            console.log(cityCode);
            
            // loadCounties(cityCode);
          });
        },
      }).addTo(mapclusterMarkers);

      // Map2D.map.fitBounds(cityLayer.getBounds());
    })
    .catch((error) => console.error("加载市级数据失败：", error));
  // 控制地图的缩放和拖拽
  nextTick(() => {
    if (Map2D.map) {
      // 让地图禁止缩放
      // Map2D.map.dragging.disable();
      // Map2D.map.scrollWheelZoom.disable();
      // Map2D.map.doubleClickZoom.disable();
      //  // 控制图层
      // Map2D.map.options.maxZoom = 6;
      // Map2D.map.options.minZoom = 6;
    }
  });
};


/**
 * 加载并显示指定城市的县级地图
 * @param {string|number} cityCode - 城市编码，用于匹配对应的县级GeoJSON数据
 */
const loadCounties = (cityCode) => {
  clearLayer();
  if(cityLayer){
    mapclusterMarkers.removeLayer(cityLayer)
  }
  if(countyLayer){
    mapclusterMarkers.removeLayer(countyLayer);
  }
  const cityPrefix = cityCode.substring(0,4);
  const displayCounties = () => {
    const filteredData ={
      ...allCountiesData,
      features:allCountiesData.features.filter(
        (feature)=>
          feature.properties.code && feature.properties.code.startsWith(cityPrefix)
      ),
    };
    countyLayer = L.geoJSON(filteredData,{
      style:mapStyle,
      onEachFeature:function(feature,layer){
        layer.bindTooltip(feature.properties.Name,{
          permanent: true,
          direction: "center",
          className: "label-style",
        })
        .openTooltip();

        layer.on("dblclick", function (e) {
              console.log(e);
              loadCities()
              setTimeout(()=>{
                Map2D.map.setView([26,119],6)
              },300)
              return
              
              // loadCounties(cityCode);
            });
        },
    }).addTo(mapclusterMarkers);
    console.log(countyLayer);
      
    setTimeout(()=>{
      try {
        mapclusterMarkers.fitBounds(countyLayer.getBounds())
      } catch (error) {
        console.log(error)
      }
    },20)
  }


  console.log(allCountiesData);
  
    if(allCountiesData){
      displayCounties()
    }else{
      fetch("fujian_area_simplified.geojson").then(response=>response.json())
      .then((data)=>{
        allCountiesData = data;
        displayCounties()
      }).catch ((error) =>{
        console.log(error)
      })
    }
  
};

const isFullScreen = ref(false);
const toggleFullScreen = () => {
  const element = document.documentElement;
  const requestMethod =
    element.requestFullscreen ||
    element.webkitRequestFullscreen ||
    element.mozRequestFullScreen ||
    element.msRequestFullscreen;
  const exitMethod =
    document.exitFullscreen ||
    document.webkitExitFullscreen ||
    document.mozCancelFullScreen ||
    document.msExitFullscreen;

  if (!isFullScreen.value) {
    if (requestMethod) {
      requestMethod.call(element);
    }
    isFullScreen.value = true;
  } else {
    if (exitMethod) {
      exitMethod.call(document);
    }
    isFullScreen.value = false;
  }
};
// 首页
const shouyeEvent = () => {
  router.push({
    path: `/portal`,
  });
  currentIndex.value = 0;
  if (
    window.TOOL.data.get("PKTY_USER_INFO") &&
    window.TOOL.data.get("PKTY_USER_INFO").wd &&
    window.TOOL.data.get("PKTY_USER_INFO").jd
  ) {
    Map2D.map.setView(
      [
        window.TOOL.data.get("PKTY_USER_INFO").wd,
        window.TOOL.data.get("PKTY_USER_INFO").jd,
      ],
      window.TOOL.data.get("PKTY_USER_INFO").mapLevel
    );
  } else {
    for (var i of mapCenterPoint) {
      if (i.province == window.TOOL.data.get("PROVINCE")) {
        // setTimeout(() => {
        Map2D.map.setView([i.y, i.x], 8);
        // }, 500);
        break;
      }
    }
  }
};
// 点击退出登录按钮
const houtaiGanli = () => {
  ElMessageBox.confirm("请确定是否要退出登录？", "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    type: "warning",
  })
    .then(() => {
      window.TOOL.data.clear();
      window.location.href = window.config.VUE_APP_CAS_BASE; //+ "/logout?service=" + serviceUrl;
    })
    .catch(() => {});
  // nextTick(()=>{
  //     document.querySelector(".el-message-box__title").style.color = '#fff';
  //     document.querySelector(".el-message-box__message").style.color = '#fff';
  //   })
};

const tcRef = ref(null);
const tcVisible = ref(false);
const tcEvent = () => {
  tcVisible.value = true;
  nextTick(() => {
    tcRef.value.open();
  });
};

const tcClose = () => {
  tcVisible.value = false;
};

const kyRef = ref(null);
const kyVisible = ref(false);
const getKy = () => {
  kyVisible.value = true;
  nextTick(() => {
    kyRef.value.open();
  });
};

const map3dStore = useMap3DStore();
const map2dStore = useMap2DStore();

const isMap2d = ref(true);

// 点击转为3d地图
const change2dMap = () => {
  const element = document.getElementById("timeline");
  if (element) {
    element.remove();
  }

  const curMapMode = getCurrentMapMode();
  console.log('xxxxxxxxxx',curMapMode);
  
  window.eventBus.emit("mapChange", curMapMode);
  emits("mapChangeEvent", curMapMode);
  if (curMapMode === mapModeEnum["3D"]) {
    map3dStore.changeWidthAndHeight("0", "0"); // 隐藏三维地图
    map2dStore.changeWidthAndHeight("100%", "100%"); // 展示二维地图
    isMap2d.value = true;
    window.Map2D.map.setView([countMessage.mapCenterPoint.wd,countMessage.mapCenterPoint.jd],9);
    // eventBus.emit("changeMapMode");
  } else if (curMapMode === mapModeEnum["2D"]) {
    map2dStore.changeWidthAndHeight("0", "0"); // 隐藏二维地图
    map3dStore.changeWidthAndHeight("100%", "100%"); // 展示三维地图
    // window.Map3D.map.flyTo([countMessage.mapCenterPoint.wd,countMessage.mapCenterPoint.jd]);
    // eventBus.emit("changeMapMode");
    // window.Map3D.flyToPos([countMessage.mapCenterPoint.jd,countMessage.mapCenterPoint.wd],1000,50)
    window.Map3D.setSCeneView(countMessage.mapCenterPoint.jd,countMessage.mapCenterPoint.wd)
    
    isMap2d.value = false;
    window.Map3D.setTimelineToDefaultRange();
    pictureDataChange();
  }
};
const pictureData = ref([]);
//获取图层数据
const getTc = () => {
  window.API.gisDataService
    .getCollectDataList({ dataAssetTypeList: [3, 4, 5, 6, 7, 8] })
    .then((res) => {
      if (res.code == 200) {
        const curMapMode = getCurrentMapMode();
        if (curMapMode === mapModeEnum["3D"]) {
          pictureData.value = [
            // {
            //   id: 3,
            //   label: "矢量",
            //   children: [],

            // },
            {
              id: 4,
              label: "倾斜摄影",
              children: [],
            },
            // {
            //   id: 5,
            //   label: "影像",
            //   children: [],

            // },
            // {
            //   id: 6,
            //   label: "三维",
            //   children: [],

            // },
            // {
            //   id: 7,
            //   label: "海图",
            //   children: [],

            // },
            // {
            //   id: 8,
            //   label: "其他",
            //   children: [],

            // },
          ];
        } else if (curMapMode === mapModeEnum["2D"]) {
          pictureData.value = [
            // {
            //   id: 3,
            //   label: "矢量",
            //   children: [],

            // },
            {
              id: 4,
              label: "倾斜摄影",
              children: [],
            },
            // {
            //   id: 5,
            //   label: "影像",
            //   children: [],

            // },
            // {
            //   id: 6,
            //   label: "三维",
            //   children: [],

            // },
            // {
            //   id: 7,
            //   label: "海图",
            //   children: [],

            // },
            // {
            //   id: 8,
            //   label: "其他",
            //   children: [],

            // },
          ];
        }

        for (var i of res.data) {
          if (i.dataServiceList && i.dataServiceList.length) {
            var obj = i.dataServiceList.filter((row) => {
              return row.supplierServiceTypeCode == "SUPERMAP-REST-MAP";
            })[0];
            if (obj == undefined) {
              obj = i.dataServiceList.filter((row) => {
                return row.supplierServiceTypeCode == "SUPERMAP-REST-DATA";
              })[0];
              if (obj == undefined) {
                obj = i.dataServiceList.filter((row) => {
                  return row.supplierServiceTypeCode == "SUPERMAP-REST-3D";
                })[0];
                if (obj == undefined) {
                  continue;
                } else {
                  i.serviceProxyUrl = obj.serviceProxyUrl;
                  i.supplierServiceTypeCode = obj.supplierServiceTypeCode;
                }
              } else {
                i.serviceProxyUrl = obj.serviceProxyUrl;
                i.supplierServiceTypeCode = obj.supplierServiceTypeCode;
              }
            } else {
              i.serviceProxyUrl = obj.serviceProxyUrl;
              i.supplierServiceTypeCode = obj.supplierServiceTypeCode;
            }
          }
          if (
            i.supplierServiceTypeCode &&
            i.supplierServiceTypeCode != "SUPERMAP-REST-DATA"
          ) {
            // if(i.dataAssetType == 3){
            //   pictureData.value[0].children.push(i)
            // }else if(i.dataAssetType == 4){
            //   pictureData.value[1].children.push(i)
            // }else if(i.dataAssetType == 5){
            //   pictureData.value[2].children.push(i)
            // }else if(i.dataAssetType == 6){
            //   pictureData.value[3].children.push(i)
            // }else if(i.dataAssetType == 7){
            //   pictureData.value[4].children.push(i)
            // }else if(i.dataAssetType == 8){
            //   pictureData.value[5].children.push(i)
            // }
            if (i.dataAssetType == 4) {
              pictureData.value[0].children.push(i);
            }
          }
        }
        pictureData.value = JSON.parse(
          JSON.stringify(pictureData.value)
            .replaceAll("dataName", "label")
            .replaceAll("serviceProxyUrl", "id")
        );
      }
    });
};
//加载图层
const pictureDataChange = () => {
  var layerInfo = window.SERVER_ADDRESS.layerInfo;
  if (window.TOOL.data.get("tcData") && window.TOOL.data.get("tcData").length) {
    window.TOOL.data.get("tcData").forEach((item) => {
      let obj = {
        url: item,
      };
      layerInfo.push(obj);
    });
  } else if (
    pictureData.value &&
    pictureData.value.length &&
    pictureData.value[0].children &&
    pictureData.value[0].children.length
  ) {
    layerInfo = [];
    pictureData.value[0].children.forEach((item) => {
      let obj = {
        url: item.id,
        name: item.label,
      };
      layerInfo.push(obj);
    });
  }

  console.log(layerInfo);
  // 倾斜摄影或三维时
  // if(layerInfo && Object.keys(layerInfo).length){
  // }
  if (layerInfo && layerInfo.length) {
    layerInfo.forEach((item) => {
      controlS3MLayer(true, item, true);
    });
  }
};
</script>

<style scoped lang="less">
.headerTitle {
  width: 100vw;
  height: 6.6vh;
  background: url("../../assets/allImage/top.png") no-repeat;
  background-size: 100% 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  pointer-events: auto;
  position: relative;
  .middleTop {
    width: 33%;
    font-size: 36px;
    color: #fff;
    font-family: Milky Han Mono CN;
    text-align: center;
  }
  .headerLeftContent {
    display: flex;
    color: #fff;
    width: 33.5%;
    padding-left: 225px;
    box-sizing: border-box;
    align-items: center;
    .title {
      margin-left: 20px;
      display: flex;
      align-items: center;
    }
    .line,
    .lineOne {
      height: 19px;
      margin: 0px;
    }
    .lineOne {
      margin-left: 10px;
    }
  }
  .headerRightContent {
    display: flex;
    color: #fff;
    width: 33.5%;
    justify-content: flex-end;
    align-items: center;

    .image,
    .imageLogin {
      width: 19px;
      height: 19px;
      cursor: pointer;
    }
    .imageLogin {
      margin: 0 24px 0 10px;
    }
    .image {
      margin: 0 10px;
    }
    .line,
    .lineOne {
      height: 19px;
      margin: 0px;
    }
    .lineOne {
      margin-left: 10px;
    }
  }
  .activeIndex {
    background: url("../../assets/allImage/hover.png") no-repeat;
    background-size: 100% 100%;
    padding: 6px 30px;
    cursor: pointer;
  }
  .activeNo {
    background: url("../../assets/allImage/noHover.png") no-repeat;
    background-size: 100% 100%;
    padding: 6px 30px;
    cursor: pointer;
  }
}

.m-box {
  .m-line {
    height: 40px;
    line-height: 40px;
    font-family: "SourceHanSansSC-Regular";
    font-size: 14px;
    letter-spacing: 0px;
    color: #fff;
    border-bottom: 1px solid #ebebeb;
  }
  .m-line-btn-analysis {
    width: 100%;
    height: 32px;
    border-radius: 5px;
    background-color: #ececec;
    margin-top: 7px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #000;
    & > img {
      width: 12px;
      height: auto;
      margin-right: 5px;
    }
    &:hover {
      background-color: #dee9f9;
    }
  }
}
</style>
