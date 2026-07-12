<template>
  <el-dialog
    class="ct_dialog"
    title="选择坐标点"
    :width="'70%'"
    destroy-on-close
    append-to-body
    v-model="visible"
    :close-on-click-modal="false"
    @closed="handleCancel"
  >
    <div id="pointMap" v-loading="isLoading"></div>
    <div class="text">
      <el-input
        v-model="cityNameVal"
        style="width: 200px"
        @keyup.enter="addressFit(cityNameVal)"
      ></el-input>
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
      经度: {{ latlng.lat }}
      <br />
      纬度: {{ latlng.lng }}
    </div>
    <template #footer>
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
    </template>
  </el-dialog>
</template>

<script>
import axios from "axios";
import mapCenterPoint from "./mapCenterPoint.json";
import icon from "@/assets/images/marker-icon.png";
export default {
  name: "JmapPointModal",
  props: ["modalWidth", "army", "pointValue"],
  data() {
    return {
      map: null,
      markerLayer: null,
      latlng: {
        lat: "",
        lng: "",
      },
      visible: false,
      isLoading: false,
      searchValue: "",
      pointValue2: "",
      cityNameVal: "",
      armyVal: false,
      armyLayer: null,
    };
  },
  methods: {
    initMap() {
      if (this.map) return;
      this.map = L.map("pointMap", {
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
        window.config.VUE_APP_SUPERMAP_BASE_URL +
          "/iserver/services/map-multiTiles/maps/wj_gis_聚合影像"
      ).addTo(this.map);
      // 添加地图上的文字
      new L.supermap.tiledMapLayer(
        window.config.VUE_APP_SUPERMAP_BASE_URL +
          "/iserver/services/map-ugcv5-wj_gis_ZhongGuo_HangZhengJingJie/rest/maps/wj_gis_%E4%B8%AD%E5%9B%BD_%E8%A1%8C%E6%94%BF%E5%A2%83%E7%95%8C"
      ).addTo(this.map);

      // 地图的点击事件
      this.map.on("click", (e) => {
        // console.log(e);
        this.cityNameVal = "";
        this.latlng = e.latlng;
        this.pointValue2 = Object.values(this.latlng).join();
        // 判断图层中是否存在东西，清空图层
        if (
          this.markerLayer !== undefined &&
          this.markerLayer !== "" &&
          this.markerLayer !== null
        ) {
          this.markerLayer.clearLayers();
        }
        // 添加图层到地图上面
        this.markerLayer = L.layerGroup().addTo(this.map);
        // 定图点击坐标点的图标
        var greenIcon = L.icon({
          iconUrl: icon,
          iconSize: [40, 40],
        });
        // 将点击的坐标点添加到地图上
        var marker = L.marker(
          L.latLng(Number(e.latlng.lat), Number(e.latlng.lng)),
          { icon: greenIcon }
        ).addTo(this.markerLayer);
        this.map.addLayer(this.markerLayer);
      });

      this.armyLayer = L.layerGroup().addTo(this.map);

      // this.addCityToMap()
    },
    // 添加地图边界
    addCityToMap(typeName, filterName) {
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
    },
    // 父组件调用，控制显示当前弹框
    show() {
      this.visible = true;
      this.$nextTick(() => {
        this.initMap();
        this.markerLayer = L.layerGroup().addTo(this.map);
        var greenIcon = L.icon({
          iconUrl: icon,
          iconSize: [40, 40],
        });
        if (this.pointValue) {
          this.latlng = {
            lat: this.pointValue.split(",")[0],
            lng: this.pointValue.split(",")[1],
          };
          this.$nextTick(() => {
            L.marker(
              L.latLng(
                Number(this.pointValue.split(",")[0]),
                Number(this.pointValue.split(",")[1])
              ),
              { icon: greenIcon }
            ).addTo(this.markerLayer);
            this.map.addLayer(this.markerLayer);
            // 自动定位到坐标点位置
            if (this.pointValue) {
              this.$nextTick(() => {
                this.map.setView(this.pointValue.split(","), 4);
              });
            }
          });
        }
      });
    },
    // 点击确定按钮
    handleSubmit() {
      if (this.pointValue2) {
        this.$emit("ok", this.pointValue2, this.cityNameVal);
        // 调用页面的方法
        this.handleClear();
      } else {
        // 判断如果坐标点不存在，则提示
        this.$message.warning("请选择坐标点");
      }
    },
    // 点击取消按钮
    handleCancel() {
      this.handleClear();
      this.$emit("handleCancel");
    },
    handleClear() {
      // 清空图层
      if (
        this.markerLayer !== undefined &&
        this.markerLayer !== "" &&
        this.markerLayer !== null
      ) {
        this.markerLayer.clearLayers();
      }
      // 关闭弹框
      this.visible = false;
    },
    // 地址匹配
    addressFit(address) {
      this.isLoading = true;
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
                this.latlng.lat = JSON.parse(res1.result)[0].location.y;
                this.latlng.lng = JSON.parse(res1.result)[0].location.x;
                this.pointValue2 = Object.values(this.latlng).join();
                // 判断图层是否为空
                if (this.markerLayer !== null) {
                  this.map.removeLayer(this.markerLayer);
                }
                //重新定义图层
                this.markerLayer = L.layerGroup([]);
                // 定义图标
                var greenIcon = L.icon({
                  iconUrl: icon,
                  iconSize: [40, 40],
                });
                // 在地图上标点
                var marker = L.marker(
                  L.latLng(JSON.parse(res1.result)[0].location.y, JSON.parse(res1.result)[0].location.x),
                  { icon: greenIcon }
                ).addTo(this.markerLayer);
                // 将图层添加到地图上
                this.map.addLayer(this.markerLayer);
                // 放大地图的层级
                this.map.setView(
                  [JSON.parse(res1.result)[0].location.y, JSON.parse(res1.result)[0].location.x],
                  6
                );
                // that.findPathProcess(
                //   [this.currentTask.lat, this.currentTask.lng],
                //   [res.data[0].location.y, res.data[0].location.x],
                //   'SmLength',
                //   'red'
                // )
                this.isLoading = false;
              }
            });
        }

        return res;
      });
      // const recv = sendReq.data.choices[0].message.content;
      // // const info = JSON.parse(recv.slice(recv.indexOf('{'),recv.indexOf('}')+1));
      // const info = JSON.parse(recv);
      // console.log(info,"897777777777777")

      // const geoCodeParam = {
      //   address: info.address, // 地址
      //   fromIndex: 0, // 设置返回对象的起始索引值
      //   toIndex: 10, // 设置返回对象的结束索引值
      //   filters: "[" + info.province + "," + info.city + "]", // 过滤条件
      //   maxReturn: 1, // 最大返回结果数
      // };
      //  var urlPath= window.config.VUE_APP_SUPERMAP_BASE_URL +
      //       "/iserver/services/addressmatch-DiMingDiZhiKu/restjsr/v1/address/geocoding"
      // L.supermap
      //   .FeatureService(
      //     window.config.VUE_APP_SUPERMAP_BASE_URL +
      //       "/iserver/services/addressmatch-DiMingDiZhiKu/restjsr/v1/address/geocoding"
      //   )
      //   .getFeaturesByGeometry(geoCodeParam, function (serviceResult) {
      //     console.log(serviceResult,"789789879789789897")
      //   });
      // axios({
      //   url:
      //     ,
      //   method: "get",
      //   params: geoCodeParam,
      // })
      //   .then((res) => {
      //     this.latlng.lat = res.data[0].location.y;
      //     this.latlng.lng = res.data[0].location.x;
      //     this.pointValue2 = Object.values(this.latlng).join();
      //     if (this.markerLayer !== null) {
      //       this.map.removeLayer(this.markerLayer);
      //     }
      //     this.markerLayer = L.layerGroup([]);
      //     var greenIcon = L.icon({
      //       iconUrl: icon,
      //       iconSize: [40, 40],
      //     });
      //     var marker = L.marker(
      //       L.latLng(res.data[0].location.y, res.data[0].location.x),
      //       { icon: greenIcon }
      //     ).addTo(this.markerLayer);
      //     this.map.addLayer(this.markerLayer);
      //     this.map.setView([res.data[0].location.y, res.data[0].location.x], 6);
      //     // that.findPathProcess(
      //     //   [this.currentTask.lat, this.currentTask.lng],
      //     //   [res.data[0].location.y, res.data[0].location.x],
      //     //   'SmLength',
      //     //   'red'
      //     // )
      //     this.isLoading = false;
      //   })
      //   .catch((err) => {
      //     this.isLoading = false;
      //   });
    },
    // 是否显示部队
    armyChange(val) {
      if (val) {
        // 调用在地图上标点的方法
        this.selectArmy();
      } else {
        // 清空图层
        if (
          this.armyLayer !== undefined &&
          this.armyLayer !== "" &&
          this.armyLayer !== null
        ) {
          this.armyLayer.clearLayers();
        }
      }
    },
    // 选择部队方法
    selectArmy() {
      var address = this.$TOOL.data.get("USER_INFO").address;
      var point = {};
      for (var i of mapCenterPoint) {
        if (i.province == address) {
          point = i;
            this.map.setView([i.y, i.x], 7);
          
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
            this.armyLayer !== undefined &&
            this.armyLayer !== "" &&
            this.armyLayer !== null
          ) {
            this.armyLayer.clearLayers();
          }
          var pointLine = res[0].result.features.features;
          pointLine.forEach((item) => {
            var greenIcon = L.icon({
              iconUrl: getImageUrl(item.properties.级别),
              iconSize: [40, 40],
            });
            var marker = L.marker(
              L.latLng(
                Number(item.properties.维度),
                Number(item.properties.经度)
              ),
              { icon: greenIcon }
            ).addTo(this.armyLayer);
            marker.bindPopup(item.properties.部队番号); //.openPopup(marker.getLatLng())
            marker.on("click", (e) => {
              console.log(e.target._popup._content);
              this.cityNameVal = e.target._popup._content;
              this.latlng = e.latlng;
              this.pointValue2 = Object.values(this.latlng).join();
              // 清除地图点击添加的其他标记点
              if (
                this.markerLayer !== undefined &&
                this.markerLayer !== "" &&
                this.markerLayer !== null
              ) {
                this.markerLayer.clearLayers();
              }
            });
          });
        });
      }
    },
  },
};
</script>
<style scoped>
/* @import "@/style/dialog.css"; */
#pointMap {
  height: 60vh;
  max-height: 600px;
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
