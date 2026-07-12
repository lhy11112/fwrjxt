import { useLayerManageStore } from "@/store/modules/layerManage";
const layerRight = useLayerManageStore();
export default {
  // 初始化测量工具
  map: null,
  lineGroup: null,
  resultLayer: null,
  rightLayer: null,
  zymbMarkerLayer: null,
  clusterMarkers: null,
  init(map) {
    this.map = map;
    this.initDrawGroup();
    // 聚和解聚的功能
    const clusterIcon = L.divIcon({
      className: "cluster-icon",
      iconSize: [39, 60],
      iconAnchor: [20, 40],
    })
    this.zymbMarkerLayer = L.markerClusterGroup({
      iconCreateFunction: (cluster) => {
        const count = cluster.getChildCount();
        clusterIcon.options.html = `<div class="custom-cluster-num">${count}</div>`
        return clusterIcon
      }
    });
    //刷新更改count
    this.map.on("zoomend", () => {
      this.zymbMarkerLayer.refreshClusters()
    })
    // 将当前图层添加到this.map上面
    this.zymbMarkerLayer.addTo(this.map)
  },
  initDrawGroup() {
    const that = this;
    // 添加所需要的图层到地图上
    that.lineGroup = L.layerGroup([]);
    that.map.addLayer(that.lineGroup);
    that.clusterMarkers = L.layerGroup([]);
    that.map.addLayer(that.clusterMarkers);

  },
  // 路线规划方法
  findPathProcess(weightFieldName, itemData, modelObj, hasLeastEdgeCount = false,) {
    const that = this;
    that.clearLayer()
    //添加站点
    var marker1 = L.marker([Number(itemData.sfd.split(",")[0]), Number(itemData.sfd.split(",")[1])]).addTo(that.lineGroup);
    marker1.bindPopup('始发点');

    for (var i of itemData.tjd) {
      if (i.zbd) {
        // 获取途经点索引
        var index = itemData.tjd.indexOf(i);

        // 定义图标
        var icon2 = L.icon({
          iconUrl: '/static/map_img/way.png',
          iconSize: [25, 41],
        });
        // 在地图上标点
        var marker2 = L.marker([Number(i.zbd.split(",")[0]), Number(i.zbd.split(",")[1])], { icon: icon2 }).addTo(that.lineGroup);
        marker2.bindPopup('途经点');
      }
    }
    // 定义图标
    var icon3 = L.icon({
      iconUrl: '/static/map_img/destination.png',
      iconSize: [25, 41],
    });
    // 在地图上标点
    var marker3 = L.marker([Number(itemData.mdd.split(",")[0]), Number(itemData.mdd.split(",")[1])], { icon: icon3 }).addTo(that.lineGroup);
    marker3.bindPopup('目的地');

    //创建最佳路径分析服务实例
    var findPathService = new L.supermap.NetworkAnalystService(window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/transportationAnalyst-wj_gis_road/rest/networkanalyst/wj_gis_road_Network_new@wj_gis_road");
    //创建最佳路径分析参数实例
    var resultSetting = new L.supermap.TransportationAnalystResultSetting({
      returnEdgeFeatures: false,
      returnEdgeGeometry: false,
      returnEdgeIDs: false,
      returnNodeFeatures: false,
      returnNodeGeometry: false,
      returnNodeIDs: false,
      returnPathGuides: true,
      returnRoutes: true
    });
    var analystParameter = new L.supermap.TransportationAnalystParameter({
      resultSetting: resultSetting,
      hasLeastEdgeCount: hasLeastEdgeCount,
      weightFieldName: weightFieldName
    });
    var pointArr = [] // 标记点参数集合
    pointArr.push(L.point(Number(itemData.sfd.split(",")[1]), Number(itemData.sfd.split(",")[0])))
    for (var a of itemData.tjd) {
      if (a.zbd) {
        pointArr.push(L.point(Number(a.zbd.split(",")[1]), Number(a.zbd.split(",")[0])))
      }
    }
    pointArr.push(L.point(Number(itemData.mdd.split(",")[1]), Number(itemData.mdd.split(",")[0])))
    var findPathParameter = new L.supermap.FindPathParameters({
      isAnalyzeById: false,
      nodes: pointArr,
      parameter: analystParameter
    });
    var myIcon = L.icon({
      // iconUrl: "../img/walk.png",
      iconSize: [20, 20]
    });
    //进行查找
    findPathService.findPath(findPathParameter, (serviceResult) => {
      var result = serviceResult.result;
      var geojson;
      result.pathList.map(function (result) {
        geojson = L.geoJSON(result.route, { color: 'rgb(0,222,255)', weight: 2, noClip: false }).addTo(that.lineGroup);

      })

      //对生成的线路进行缓冲区分析
      var bufferAnalystService = new L.supermap.SpatialAnalystService(window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/spatialAnalysis-wj_gis_ZhongGuo_XingZhengJingJie/restjsr/spatialanalyst");
      var geoBufferAnalystParams = new L.supermap.GeometryBufferAnalystParameters({
        sourceGeometry: geojson,
        bufferSetting: new L.supermap.BufferSetting({
          endType: L.supermap.BufferEndType.ROUND,
          leftDistance: new L.supermap.BufferDistance({ value: (modelObj.hcqjl / 11100).toFixed(4) }), // 需要进行换算，一度约等于11100米
          rightDistance: new L.supermap.BufferDistance({ value: (modelObj.hcqjl / 11100).toFixed(4) }),
          semicircleLineSegment: 10
        })
      });

      bufferAnalystService.bufferAnalysis(geoBufferAnalystParams, (serviceResult) => {
        this.resultLayer = new L.supermap.FGBLayer(serviceResult.result.newResourceLocation, {
          strategy: 'all'
        }).on('contextmenu', function (evt) { // 图层右键方法
          // 保存面范围实例，在右键面板中使用
          // evt.originalEvent.preventDefault();
          that.rightLayer = evt.layer
          layerRight.getLayerRightClick(that.rightLayer)
          // 点击图层右键出现的数据
          var rigList = [
            { text: '缓冲区距离', iconname: 'zymb', click: 'hcqjlClick()' },
            { text: '沿途资源', iconname: 'zymb', click: 'getZymbDataJdlx()' },
            { text: '沿途天气', iconname: 'zymb', click: 'getWeatherJdlx()' },
            // { text: '周边力量', iconname: 'zbll', click: 'getZzllDataJdlx()' },
          ]
          var mapRigMenuHtm = function (o) {
            return `
                  <div class='cd-span' style=" cursor: pointer;">
                    <a onclick='${o.click}'>
                      <img  src='/static/map_img/icon/中国银行.png' style='vertical-align: middle;width:25px;height:25px'>${o.text}</a></a>
                  </div>`
          }

          var rigHtm = ''
          for (let i = 0; i < rigList.length; i++) {
            rigHtm += mapRigMenuHtm(rigList[i])
          }

          //添加地图弹出框
          L.popup({
            className: 'mypopup',
          }).setLatLng(evt.latlng).setContent(rigHtm).openOn(that.lineGroup)
        }).addTo(that.lineGroup);

      }, 'FGB')


      
    })
  },
  // 获取沿途天气
  getWeather() {
    // 清除部队点以及路线图层
    var param = new L.supermap.QueryByGeometryParameters({

      queryParams: { name: "市界@wj_gis_中国_行政境界" },
      geometry: this.rightLayer,
    
    });
    new L.supermap.QueryService(window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-wj_gis_ZhongGuo_XingZhengJingJie/rest/maps/wj_gis_%E4%B8%AD%E5%9B%BD_%E8%A1%8C%E6%94%BF%E5%A2%83%E7%95%8C").queryByGeometry(param, (serviceResult2) => {
      // //leaflet 绘制数据默认为 4326，服务器返回数据为 3857，因此在加载到地图前，先进行坐标系转换：
      var result = L.supermap.Util.transform(serviceResult2.result.recordsets[0].features, L.CRS.EPSG3857, L.CRS.EPSG4326);
      var cs = []
      for (var city of result.features) {
        cs.push(city.properties.NAME)
      }

      window.API.thirdPartyData.weatherInformation({ city: cs.join(), }).then(res => {
        if (res.code == 200) {

          // 添加天气的数据在地图上，
          var tooltip = L.tooltip({ direction: 'top', permanent: true, className: 'map_weather_tooltip' })
            .setLatLng(L.latLng(Number(res.result.wd), Number(res.result.jd)))
            .setContent(
              `<div class="map_weather_popup_box" style="text-align:center;"><img style="width:30px;height:30px;" src="${require(`@/assets/weather/${this.getTypeIcon(
                res.result.wea
              )}.svg`)}" class="map_weather_icon"/><p  class="map_weather_tip">${res.result.min_temperature + '℃'}~ ${res.result.max_temperature + '℃'
              }</p><p  class="map_weather_tip">${res.result.city_name}</p></div>`
            )
            .addTo(this.clusterMarkers)

          tooltip.openTooltip()
          setTimeout(() => {

            for (var i in document.querySelectorAll(".map_weather_tooltip")) {
              try {
                document.querySelectorAll(".map_weather_tooltip")[i].style.background = "#025583";
                document.querySelectorAll(".map_weather_tooltip")[i].style.borderColor = "#025583";
                document.querySelectorAll(".map_weather_tooltip")[i].style.color = "#fff";
              } catch (error) {

              }
            }

          }, 10);
        }
      })

    })
  },
  //根据类型拿取天气的图标
  getTypeIcon(type) {
    var typeclass = 'wi-day-sunny-overcast'
    type = type.split('转')
    type = type[0]

    if (type.includes('晴')) {
      typeclass = 'wi-day-sunny'
    } else if (type.includes('云')) {
      typeclass = 'wi-day-cloudy'
    } else if (type.includes('阴')) {
      typeclass = 'wi-cloudy'
    } else if (type.includes('大雨') || type.includes('暴雨')) {
      typeclass = 'wi-storm-showers'
    } else if (type.includes('雷雨') || type.includes('雷阵雨')) {
      typeclass = 'wi-thunderstorm'
    } else if (type.includes('雨夹雪')) {
      typeclass = 'wi-sleet'
    } else if (type.includes('雨')) {
      typeclass = 'wi-showers'
    } else if (type.includes('冰雹')) {
      typeclass = 'wi-hail'
    } else if (type.includes('扬沙')) {
      typeclass = 'wi-dust'
    } else if (type.includes('雾')) {
      typeclass = 'wi-fog'
    } else if (type.includes('霾')) {
      typeclass = 'wi-day-haze'
    } else if (type.includes('风')) {
      typeclass = 'wi-day-windy'
    } else if (type.includes('中雪') || type.includes('大雪') || type.includes('暴雪')) {
      typeclass = 'wi-snowflake-cold'
    } else if (type.includes('雪')) {
      typeclass = 'wi-snow'
    }
    return typeclass
  },
  clearLayer(flag = false) {
    if (!flag) {
      // 清除图层
      if (this.lineGroup !== undefined && this.lineGroup !== '' && this.lineGroup !== null) {
        this.lineGroup.clearLayers()
      }
      // 清除缓冲区图层
      if (this.resultLayer !== undefined && this.resultLayer !== '' && this.resultLayer !== null) {
        this.resultLayer.curLayer.clearLayers()
      }
    }
    // 清除部队点以及路线图层
    if (this.clusterMarkers !== undefined && this.clusterMarkers !== '' && this.clusterMarkers !== null) {
      this.clusterMarkers.clearLayers()
    }
    // // 清除其他目标点
    if (this.zymbMarkerLayer !== undefined && this.zymbMarkerLayer !== '' && this.zymbMarkerLayer !== null) {
      this.zymbMarkerLayer.clearLayers()
    }



  },
  // 标点添加聚合解聚
  getPonit(features, jcsjIconObj) {
    if (this.zymbMarkerLayer !== undefined && this.zymbMarkerLayer !== '' && this.zymbMarkerLayer !== null) {
      this.zymbMarkerLayer.clearLayers()
    }

    features.forEach(item => {
      const data = item.properties
      // 根据当前的名称，去拿取对应的图标名称
      var icon = jcsjIconObj[data.BDMC]
      // 标注点的图标
      if (icon) {
        data.iconName = '/static/mapDataIcon/' + icon + '.png'
      } else {
        data.iconName = '/static/mapDataIcon/默认.png'
      }
      var myIcon = L.icon({
        iconUrl: data.iconName,
        // iconSize: [30, 35],
        // iconAnchor: [20, 5]//文字标注相对位置
      });

      //坐标信息
      const coordinates = item.geometry.coordinates
      data.point0 = coordinates[0]
      data.point1 = coordinates[1]
      // 定义坐标点的显示数据
      var innerHTML = '名称: ' + data.MC + '<br>'
      innerHTML += '经度: ' + data.point0 + '<br>'
      innerHTML += '纬度: ' + data.point1 + '<br>'
      var points = [Number(data.point1), Number(data.point0)];
      L.marker(points, {
        icon: myIcon,
      }).addTo(this.zymbMarkerLayer)
        .bindTooltip(innerHTML)
        // .bindPopup(innerHTML)

        // .bindPopup(innerHTML)
        .openPopup()

    })
  }


}