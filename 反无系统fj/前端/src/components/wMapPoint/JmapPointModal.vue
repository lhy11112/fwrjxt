<template>
  <el-dialog
    class="ct_dialog"
    title="选择坐标点"
    width="51%"
    destroy-on-close
    v-model="visible"
    :close-on-click-modal="false"
    @closed="handleCancel"
    style="height: 60%; margin-top: 10%"
  >
    <!-- 地图标签 -->
    <div id="pointMap" v-loading="isLoading"></div>
    <div class="text">
      <!-- 输入框 -->
      <el-input
        v-model="cityNameVal"
        style="width: 200px"
        @keyup.enter="addressFit(cityNameVal)"
      ></el-input>
      <!-- 切换是否在地图上显示部队 -->
      <el-switch
        v-if="army"
        v-model="armyVal"
        @change="armyChange"
        inline-prompt
        style="--el-switch-on-color: #cf8a00"
        active-text="部队"
        inactive-text="部队"
      />
      <br />
      <!-- 经纬度 -->
      经度: {{ latlng.lng }}
      <br />
      纬度: {{ latlng.lat }}
    </div>
    <!-- dialog的地步的按钮 -->
    <!-- <template #footer>
      <el-button
        style="background: transparent; color: #fff"
        @click="handleCancel"
        >取 消</el-button
      >
      <el-button
        :loading="isSaveing"
        style="background: #cf8a00; color: #fff"
        @click="handleSubmit"
        >确 定</el-button
      >
    </template> -->
    <div class="footerList">
      <el-button type="prmiary" @click="handleSubmit()">确 定</el-button>
      <el-button @click="handleCancel">取消</el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import axios from "axios";
import mapCenterPoint from "./mapCenterPoint.json";
import icon from "@/assets/images/marker-icon.png";
import { ref, toRef, nextTick, getCurrentInstance } from "vue";
import { ElNotification, ElMessageBox, ElMessage } from "element-plus";
// 定义相当于vue2中的this
const { ctx } = getCurrentInstance();
// 定义地图
const map = ref(null);
// 图层变量
const markerLayer = ref(null);
// 存储经纬度的变量
const latlng = ref({
  lat: "",
  lng: "",
});
// 控制弹框是否显示
const visible = ref(false);
// loading是否显示
const isLoading = ref(false);

const searchValue = ref("");
const pointValue2 = ref("");
const cityNameVal = ref("");
const armyVal = ref(false);
const armyLayer = ref(null);
// 定义需要调用的父组件的方法
const emits = defineEmits(["ok", "handleCancel"]);
const props = defineProps({
  pointValue: {
    type: String,
    default: "",
  },
});
const pointValue = toRef(props);
// props: ["modalWidth", "army", "pointValue"],
const initMap = () => {
  // if (map.value) return;
  map.value = L.map("pointMap", {
    crs: L.CRS.EPSG4326,
    center: [40.92278125, 101.5980283],
    maxZoom: 18,
    minZoom: 4,
    zoom: 4,
    // 放大缩小的控制按钮
    zoomControl: false,
    // 控制右下角的图标显示
    attributionControl: false,
    logoControl: false,
  });
  // 添加地图的地址
  new L.supermap.tiledMapLayer(
    window.config.VUE_APP_SUPERMAPZYIP_BASE_URL +
      ":{s}/iserver/services/map-multiTiles/rest/maps/wj_gis_聚合影像",{
          transparent: true,
          subdomains: window.config.VUE_APP_SUPERMAPZYDK_BASE_URL,
  			  format:"webp"
      }
  ).addTo(map.value);
  // 添加地图上的文字
  new L.supermap.tiledMapLayer(
    window.config.VUE_APP_SUPERMAP_BASE_URL +
      "/iserver/services/map-ugcv5-wj_gis_ZhongGuo_HangZhengJingJie/rest/maps/wj_gis_%E4%B8%AD%E5%9B%BD_%E8%A1%8C%E6%94%BF%E5%A2%83%E7%95%8C"
  ).addTo(map.value);

  // 地图的点击事件
  map.value.on("click", (e) => {
    // console.log(e);
    cityNameVal.value = "";
    latlng.value = e.latlng;
    pointValue2.value = Object.values(latlng.value).join();
    // 判断图层中是否存在东西，清空图层
    if (
      markerLayer.value !== undefined &&
      markerLayer.value !== "" &&
      markerLayer.value !== null
    ) {
      markerLayer.value.clearLayers();
    }
    // 添加图层到地图上面
    markerLayer.value = L.layerGroup().addTo(map.value);
    // 定图点击坐标点的图标
    var greenIcon = L.icon({
      iconUrl: icon,
      iconSize: [40, 40],
    });
    // 将点击的坐标点添加到地图上
    var marker = L.marker(
      L.latLng(Number(e.latlng.lat), Number(e.latlng.lng)),
      { icon: greenIcon }
    ).addTo(markerLayer.value);
    map.value.addLayer(markerLayer.value);
  });

  armyLayer.value = L.layerGroup().addTo(map.value);

  // this.addCityToMap()
};
// 添加地图边界
const addCityToMap = (typeName, filterName) => {
  /**
   * 加载省市边界图
   * @typeName 边界图的类型
   * @filterName 显示的行政区域
   * **/
  const that = this;
  const url =
    window.config.VUE_APP_SUPERMAP_BASE_URL +
    "/iserver/services" +
    "/data-wj_gis_ZhongGuo_XingZhengJingJie/rest/data";
  /*** 动态设置请求参数 */
  const queryParameter = {
    name: typeName + "@wj_gis_中国_行政境界",
  };
  if (filterName) {
    queryParameter.attributeFilter = "PROVINCE = '" + filterName + "'";
  }
  const datasetNames = ["wj_gis_中国_行政境界:" + typeName];
  const sqlParam = new L.supermap.GetFeaturesBySQLParameters({
    queryParameter: queryParameter,
    datasetNames: datasetNames,
    toIndex: -1,
  });
  new L.supermap.FeatureService(url).getFeaturesBySQL(
    sqlParam,
    (serviceResult) => {
      if (!serviceResult.result) {
        return;
      }
      L.geoJSON(serviceResult.result.features, {
        style: function () {
          // console.log(feature);
          // 添加省界，线显示的颜色
          return {
            fillColor: "blue",
            fillOpacity: 0.1,
            weight: 1,
            borderColor: "red",
          };
        },
      }).addTo(that.map);
    }
  );
};
// 父组件调用，控制显示当前弹框
const show = () => {
  visible.value = true;
  nextTick(() => {
    initMap();
    markerLayer.value = L.layerGroup().addTo(map.value);
    var greenIcon = L.icon({
      iconUrl: icon,
      iconSize: [40, 40],
    });
    if (pointValue.value.mbList) {
      latlng.value = {
        lat: pointValue.value.mbList.split(",")[0],
        lng: pointValue.value.mbList.split(",")[1],
      };
      nextTick(() => {
        L.marker(
          L.latLng(
            Number(pointValue.value.mbList.split(",")[0]),
            Number(pointValue.value.mbList.split(",")[1])
          ),
          { icon: greenIcon }
        ).addTo(markerLayer.value);
        map.value.addLayer(markerLayer.value);
        // 自动定位到坐标点位置
        if (pointValue.value.mbList) {
          nextTick(() => {
            map.value.setView(pointValue.value.mbList.split(","), 4);
          });
        }
      });
    }
  });
};
// 抛出show方法，能够让父组件调用
defineExpose({ show });
// 点击确定按钮
const handleSubmit = () => {
  if (pointValue2.value) {
    const pinitArr = pointValue2.value.split(',');
    pinitArr[0] = Number(pinitArr[0]).toFixed(8)
    pinitArr[1] = Number(pinitArr[1]).toFixed(8)
    pointValue2.value = pinitArr.join(',')
    emits("ok", pointValue2.value, cityNameVal.value);
    // 调用页面的方法
    handleClear();
  } else {
    // 判断如果坐标点不存在，则提示
    ElMessage({
      type: "warning",
      message: h("p", null, [h("span", null, "请选择坐标点")]),
    });
  }
};
// 点击取消按钮
const handleCancel = () => {
  handleClear();
  emits("handleCancel");
};
const handleClear = () => {
  // 清空图层
  if (
    markerLayer.value !== undefined &&
    markerLayer.value !== "" &&
    markerLayer.value !== null
  ) {
    markerLayer.value.clearLayers();
  }
  // 关闭弹框
  visible.value = false;
};
// 地址匹配
const addressFit = (address) => {
  isLoading.value = true;
  axios({
    url: window.config.VUE_APP_ZHLCMX_BASE_URL + "/v1/chat/completions",
    method: "post",
    timeout: 100000,
    data: {
      model: "chatglm3-6b-32k",
      // 给大模型增加提示对
      messages: [
        {
          role: "system",
          content:
            '你叫艾武，你是一位地理向导工作人员，接下来我会向你询问地址的位置信息，请根据我询问的内容提取地址名称(address)，整理出该地址所在的省(province)、市(city)详细名称（例如新疆的详细名称为新疆维吾尔自治区），以Json格式输出结果，如{"address":"XXX", "province":"XX", "city":"XX"}',
        },
        {
          role: "user",
          content:
            "乌鲁木齐地窝堡机场发生劫持人质事件，请派遣附近20公里范围内的人员立即前往支援，地图显示14级",
        },
        {
          role: "assistant",
          content:
            '{ "province": "新疆维吾尔自治区", "city": "乌鲁木齐市", "address": "地窝堡机场", "distance": "20000", "level": "14" }',
        },
        {
          role: "user",
          content:
            "西安已经连续三天下暴雨，三桥地铁站被淹没，请派遣附近10公里范围内的人员立即前往救灾，地图显示13级",
        },
        {
          role: "assistant",
          content:
            '{ "province": "陕西省", "city": "西安市", "address": "三桥地铁站", "distance": "10000", "level": "13"  }',
        },
        {
          role: "user",
          content: address,
        },
      ],
      stream: false,
      max_tokens: 300,
      temperature: 0.8,
      top_p: 0.8,
    },
  }).then((res) => {
    if (res.status == 200) {
      // 拿到大模型返回的接口
      const recv = JSON.parse(res.data.choices[0].message.content);
      // 根据大模型拿取到的数据去调取后台代理的超图接口
      window.API.model.battledamage
        .getPointByarea({
          area: recv.address,
          city: recv.city,
          province:
            recv.province == '"新疆"' ? "新疆维吾尔自治区" : recv.province,
        })
        .then((res1) => {
          if (res1.code == 200) {
            // 拿到的输入地址信息，在地图上进行标点
            latlng.value.lat = JSON.parse(res1.result)[0].location.y;
            latlng.value.lng = JSON.parse(res1.result)[0].location.x;
            pointValue2.value = Object.values(latlng.value).join();
            // 判断图层是否为空
            if (markerLayer.value !== null) {
              map.value.removeLayer(markerLayer.value);
            }
            //重新定义图层
            markerLayer.value = L.layerGroup([]);
            // 定义图标
            var greenIcon = L.icon({
              iconUrl: icon,
              iconSize: [40, 40],
            });
            // 在地图上标点
            var marker = L.marker(
              L.latLng(
                JSON.parse(res1.result)[0].location.y,
                JSON.parse(res1.result)[0].location.x
              ),
              { icon: greenIcon }
            ).addTo(markerLayer.value);
            // 将图层添加到地图上
            map.value.addLayer(markerLayer.value);
            // 放大地图的层级
            map.value.setView(
              [
                JSON.parse(res1.result)[0].location.y,
                JSON.parse(res1.result)[0].location.x,
              ],
              6
            );
            // that.findPathProcess(
            //   [this.currentTask.lat, this.currentTask.lng],
            //   [res.data[0].location.y, res.data[0].location.x],
            //   'SmLength',
            //   'red'
            // )
            isLoading.value = false;
          }
        });
    }
  });
};
// 是否显示部队
const armyChange = (val) => {
  if (val) {
    // 调用在地图上标点的方法
    selectArmy();
  } else {
    // 清空图层
    if (
      armyLayer.value !== undefined &&
      armyLayer.value !== "" &&
      armyLayer.value !== null
    ) {
      armyLayer.value.clearLayers();
    }
  }
};
// 选择部队方法
const selectArmy = () => {
  var address = window.TOOL.data.get("USER_INFO").address;
  var point = {};
  for (var i of mapCenterPoint) {
    if (i.province == address) {
      point = i;
        map.value.setView([i.y, i.x], 7);
    
      break;
    }
  }
  if (point.y) {
    var center = [Number(point.y), Number(point.x)]; //圆心点
    var radius = Number(100) * 1000; //圆的半径
    var bound = Map2D.getCriclePoints(center, radius);
    var polygon = L.polygon(bound, { color: "blue" });
    getZZLL_BDBS(polygon).then((res) => {
      if (
        armyLayer.value !== undefined &&
        armyLayer.value !== "" &&
        armyLayer.value !== null
      ) {
        armyLayer.value.clearLayers();
      }
      var pointLine = res[0].result.features.features;
      pointLine.forEach((item) => {
        var greenIcon = L.icon({
          iconUrl: getImageUrl(item.properties.级别),
          iconSize: [40, 40],
        });
        var marker = L.marker(
          L.latLng(Number(item.properties.维度), Number(item.properties.经度)),
          { icon: greenIcon }
        ).addTo(armyLayer.value);
        marker.bindPopup(item.properties.部队番号); //.openPopup(marker.getLatLng())
        marker.on("click", (e) => {
          console.log(e.target._popup._content);
          cityNameVal.value = e.target._popup._content;
          latlng.value = e.latlng;
          pointValue2.value = Object.values(this.latlng).join();
          // 清除地图点击添加的其他标记点
          if (
            markerLayer.value !== undefined &&
            markerLayer.value !== "" &&
            markerLayer.value !== null
          ) {
            markerLayer.value.clearLayers();
          }
        });
      });
    });
  }
};
</script>
<style scoped>
/* @import "@/style/dialog.css"; */
#pointMap {
  height: 94%;
  position: relative;
}
.text {
  position: absolute;
  top: 100px;
  left: 30px;
  z-index: 419;
  color: #fff;
  padding: 10px;
  font-size: 18px;
}
</style>
