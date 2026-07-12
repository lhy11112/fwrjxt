import { cfg2D } from '../config';
export default {
  // 初始化测量工具
  map: null,
  resultLayer: null,
  clusterMarkersAround: null,

  init(map) {
    this.map = map;
    // 聚和解聚的功能
    const clusterIcon = L.divIcon({
      className: "cluster-icon",
      iconSize: [39, 60],
      iconAnchor: [20, 40],
    })
    this.clusterMarkersAround = L.markerClusterGroup({
      iconCreateFunction: (cluster) => {
        const count = cluster.getChildCount();
        clusterIcon.options.html = `<div class="custom-cluster-num">${count}</div>`
        return clusterIcon
      }
    });
    //刷新更改count
    this.map.on("zoomend", () => {
      this.clusterMarkersAround.refreshClusters()
    })
    // 将当前图层添加到this.map上面
    this.clusterMarkersAround.addTo(this.map)
  },
  addPoint(itemData) {

    this.clearLayer()
    //console.log(itemData, "879897978978978978")
    itemData.forEach(item => {
      if (item.bdfh.includes("支队")) {
        var greenIcon = L.icon({
          iconUrl: '/static/zhid.png',
          iconSize: [40, 40],
        });
      } else if (item.bdfh.includes("中队")) {
        var greenIcon = L.icon({
          iconUrl: '/static/zhongd.png',
          iconSize: [40, 40],
        });
      } else if (item.bdfh.includes("总队")) {
        var greenIcon = L.icon({
          iconUrl: '/static/zongd.png',
          iconSize: [40, 40],
        });
      } else if (item.bdfh.includes("大队")) {
        var greenIcon = L.icon({
          iconUrl: '/static/dad.png',
          iconSize: [40, 40],
        });
      } else if (item.bdfh.includes("医院")) {
        var greenIcon = L.icon({
          iconUrl: '/static/map_img/icon/yiyuan.png',
          iconSize: [40, 40],
        });
      } else if (item.bdfh.includes("院校")) {
        var greenIcon = L.icon({
          iconUrl: '/static/map_img/icon/xuexiao.png',
          iconSize: [40, 40],
        });
      } else if (item.bdfh.includes("机场")) {
        var greenIcon = L.icon({
          iconUrl: '/static/map_img/icon/jichang.png',
          iconSize: [40, 40],
        });
      }
      else if (item.bdfh.includes("车站")) {
        var greenIcon = L.icon({
          iconUrl: '/static/map_img/icon/jiaotong.png',
          iconSize: [40, 40],
        });
      } else if (item.bdfh.includes("口岸")) {
        var greenIcon = L.icon({
          iconUrl: '/static/map_img/icon/gangko.png',
          iconSize: [40, 40],
        });
      } else if (item.bdfh.includes("机构")) {
        var greenIcon = L.icon({
          iconUrl: '/static/map_img/icon/jigou.png',
          iconSize: [40, 40],
        });
      } else {
        var greenIcon = L.icon({
          iconUrl: '/static/map_img/icon/jiguan.png',
          iconSize: [40, 40],
        });
      }
      var marker = L.marker(L.latLng(Number(item.wd), Number(item.jd)), { icon: greenIcon }).addTo(this.clusterMarkersAround);;
      marker.bindPopup(item.bdfh).openPopup(marker.getLatLng())
    })
  },
  // 点击单个数据
  clickSingle() {
    this.clearLayer();

  },
  // 清空图层
  clearLayer() {
    if (this.clusterMarkersAround != undefined || this.clusterMarkersAround != null || this.clusterMarkersAround != "") {
      // 清空图层
      this.clusterMarkersAround.clearLayers()
    }
  },
  // 绘制圆圈，添加范围内的部队显示点
  drawLine(url, data) {
    const that = this;
    that.map.flyTo([data[0].wd, data[0].jd], 10)
    that.getCircleDataPoint(url, data)
  },
  // 获取圆圈内的对应数据
  getCircleDataPoint(url, data) {
    const that = this;
    var center = [data[0].wd, data[0].jd];//圆心点
    var radius = data[0].km * 1000;//圆的半径
    //console.log(data[0].km,"897777777")
    var bound = this.getCriclePoints(center, radius)

    var polygon = L.polygon(bound, { color: "blue" }).addTo(that.clusterMarkersAround)
    let reqUrl = "";
    if (url.indexOf("http") === -1) {
      reqUrl = window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services" + url;
    } else {
      reqUrl = url;
    }
    var geometryParam = new L.supermap.GetFeaturesByGeometryParameters({
      datasetNames: ["wj_gis_bdfh:bdfh"],
      geometry: polygon,
      toIndex: 10000,
      // spatialQueryMode: "INTERSECT"
    });
    new L.supermap
      .FeatureService(reqUrl)
      .getFeaturesByGeometry(geometryParam, function (serviceResult) {
        var pointLine = serviceResult.result.features.features;
        //console.log(pointLine, "测试123与哦拍【】、")
        // if (data[0].point && data[0].point == 0) {
        //   return
        // } else {
        //   //console.log("data[0]")
        // }
        // 存储范围内的全部部队信息
        const buduiMessage = pointLine
        // .filter(item => {
        //   var list = item.properties.部队番号.slice(-2)

        //   if (list == "中队" || list == "支队") {
        //     return item
        //   }
        // })
        // 添加图层，分别展示中队和支队
        buduiMessage.forEach(item => {
          // that.pathLengthLenght(item.properties, data)
          if (item.properties.级别 == "支队") {
            var greenIcon = L.icon({
              iconUrl: '/static/zhid.png',
              iconSize: [40, 40],
            });
          } else if (item.properties.级别 == "中队") {
            var greenIcon = L.icon({
              iconUrl: '/static/zhongd.png',
              iconSize: [40, 40],
            });
          } else if (item.properties.级别 == "总队") {
            var greenIcon = L.icon({
              iconUrl: '/static/zongd.png',
              iconSize: [40, 40],
            });
          } else if (item.properties.级别 == "0") {
            var greenIcon = L.icon({
              iconUrl: '/static/zongd.png',
              iconSize: [40, 40],
            });
          } else if (item.properties.级别 == "大队") {
            var greenIcon = L.icon({
              iconUrl: '/static/dad.png',
              iconSize: [40, 40],
            });
          }
          //console.log(item.properties, "9877777777777777")
          var marker = L.marker(L.latLng(Number(item.properties.维度), Number(item.properties.经度)), { icon: greenIcon }).addTo(that.clusterMarkersAround);
          marker.bindPopup(item.properties.部队番号).openPopup(marker.getLatLng())
         
        })
      });
    setTimeout(() => {
      var marker1 = L.marker(L.latLng(data[0].wd, data[0].jd)).addTo(that.clusterMarkersAround)
      marker1.bindPopup(data[0].name).openPopup(marker1.getLatLng())
    }, 100)
  },
  pathLengthLenght(start, end) {
    const that = this
    //创建最佳路径分析服务实例
    var findPathService = new L.supermap.NetworkAnalystService
      (window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/transportationAnalyst-wj_gis_road/rest/networkanalyst/wj_gis_road_Network_new@wj_gis_road");
    //创建最佳路径分析参数实例
    var resultSetting = new L.supermap.TransportationAnalystResultSetting({
      returnEdgeFeatures: true,
      returnEdgeGeometry: false,
      returnEdgeIDs: false,
      returnNodeFeatures: false,
      returnNodeGeometry: false,
      returnNodeIDs: false,
      returnPathGuides: false,
      returnRoutes: true
    });
    var analystParameter = new L.supermap.TransportationAnalystParameter({
      resultSetting: resultSetting,
      weightFieldName: "SmLength"//权重
    });
    var findPathParameter = new L.supermap.FindPathParameters({
      isAnalyzeById: false,
      nodes: [L.point(Number(start.经度), Number(start.维度)), L.point(end[0].jd, end[0].wd)],
      parameter: analystParameter,
    });

    //进行查找
    findPathService.findPath(findPathParameter, function (serviceResult) {
      var result = serviceResult.result;
      result.pathList.map(function () {
      })
    });
  },
  // 计算形成圆圈的点
  getCriclePoints(center, radius) {
    var bound = [];
    var earthRadius = 6378137;//地球的半径
    var dlat = (radius / earthRadius) * (180 / Math.PI);
    var dlng = dlat / Math.cos(center[0] * Math.PI / 180);
    for (var i = 0; i < 360; i++) {
      var red = i * Math.PI / 180;
      var lat = center[0] + dlat * Math.sin(red);
      var lng = center[1] + dlng * Math.cos(red);
      bound.push([lat, lng])
    }
    return bound
  },
}