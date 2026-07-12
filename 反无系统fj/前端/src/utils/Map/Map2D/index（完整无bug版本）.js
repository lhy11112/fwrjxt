import { cfg2D } from '../config';
import { useCounterStoreMethods } from "@/store/modules/jwdu";
import { useCoordStore } from "@/store/modules/coord";
import measure from './measure';
import marker from "./marker";
//添加聚合点
import aggregation from "./aggregation";
// 鹰眼
import eagleEye from './eagleEye';
// 路径规划
import linePath from './linePath';
// 标出二级页面的坐标点
import secondPoint from './secondPoint';
// 周边力量标点
import aroundPower from "./aroundPower"
import config from "@/config"
import { useDynamicPlottingStore } from "@/store/modules/dynamicPlotting";
import zbmbApi from "@/api/zbmb";

// 获取到地图的模式
import { mapModeEnum, getCurrentMapMode } from "@/utils/Map/mapMode";
import axios from 'axios';
import { ImageLayer } from "./ImageLayer";

const countMessage = useCounterStoreMethods();
const coordStore = useCoordStore();
const dynamicPlottingStore=useDynamicPlottingStore()
var resultData = {
    result:[]
};
let map = null
let ewtyFlag = false;
export default {
    map: null,
    markerLayer: null,
    taskmarkerLayer: null,
    // 省界图层
    boundaryLayer: null,
    tiledMapLayer: null,
    measure,
    marker,
    aggregation,
    eagleEye,
    linePath,
    secondPoint,
    aroundPower,
    listData: [],
    collectDataList: [],
    clusterMarkers: null,
    plottingLayer: null,
    drawControl: null,
    editControl: null,
    latlng: null,
    rect:null,
    kxFlag:false,
    kyFlag:false,
    xdFlag:false,
    // ewtyFlag:false,
    serverUrl: `${cfg2D.server.baseUrl1}/plot-WJ/rest/plot`,
    init(containerId) {
        if(config.VUE_APP_SUPERMAP_BASE_FLAG){
            this.map = L.map(containerId, {
                center: [26.0753, 119.3062],
                crs: L.CRS.EPSG4326,
                maxZoom: 20,
                minZoom: 1,
                zoom: 8,
                zoomControl: false,
                logoControl: false,
                attributionControl: false
            });
        }else{
            this.map = L.map(containerId, {
                center: [26.0753, 119.3062],
                // crs: L.CRS.EPSG4326,
                maxZoom: 25,
                minZoom: 1,
                zoom: 8,
                zoomControl: false,
                logoControl: false,
                attributionControl: false
            });
        }


        map = this.map;

        

        this.imageLayer = new ImageLayer(this.map); // 影像图层
        //调取标绘，测距初始化
        this.measure.init(this.map);
        this.marker.init(this.map);
        //调用鹰眼js
        this.eagleEye.init(this.map);
        // 聚合解聚
        this.aggregation.init(this.map);
        // 路径规划
        this.linePath.init(this.map);
        this.addEventsToMap();
        //地图右键菜单
        // if(taskStore.taskDetail && Object.keys(taskStore.taskDetail).length>0 && taskStore.taskDetail.ztmc){
        //     this.map.on('contextmenu', this.rightClick)
        // }
        this.map.on('contextmenu', this.rightClick)
        //在地图上标出二级页面的坐标点
        this.secondPoint.init(this.map);
        // 周边力量在地图上标点
        this.aroundPower.init(this.map);

        window.sxrwLayer = L.layerGroup().addTo(this.map); // 遂行任务图层
        window.bydzLayer = L.layerGroup();
        window.bydzLayer.addTo(this.map);

        window.markerLayer = L.layerGroup([]);
        window.markerLayer.addTo(this.map);
        window.bdmarkerLayer = L.layerGroup([]);
        window.bdmarkerLayer.addTo(this.map);


        // window.wrjMarkerLayer = L.layerGroup([]);
        // window.wrjMarkerLayer.addTo(this.map);

        window.fsMarkerLayer = window.L.layerGroup([]);
        window.fsMarkerLayer.addTo(this.map);

        window.xdMarkerLayer = window.L.layerGroup([]);
        window.xdMarkerLayer.addTo(this.map);

        window.fkqyLayer = window.L.layerGroup([]);
        window.fkqyLayer.addTo(this.map);

        
        

        
        // let clusterIcon = window.L.divIcon({
        // className: "cluster-icon",
        // iconSize: [39, 60],
        // iconAnchor: [20, 40],
        // });
        // window.clusterMarkerLayer = window.L.markerClusterGroup({
        // iconCreateFunction: (cluster) => {
        //     let count = cluster.getChildCount();
        //     clusterIcon.options.html = `<div class="custom-cluster-num">${count}</div>`;
        //     return clusterIcon;
        // },
        // });
        // window.clusterMarkerLayer.addTo(this.map);
        // L.Tooltip.prototype._animateZoom = function(e){
        //     if(this._map){
        //         var pos=this._map._latLngToNewLayerPoint(this._latlng,e.zoom,e.center),
        //         anchor=this._getAnchor()
        //         L.DomUtil.setPosition(this._container,pos.add(anchor))
        //     }else{
        //         return
        //     }
        // }
        
        // L.Popup.prototype._animateZoom = function(e){
        //     if(this._map){
        //         var pos=this._map._latLngToNewLayerPoint(this._latlng,e.zoom,e.center),
        //         anchor=this._getAnchor()
        //         L.DomUtil.setPosition(this._container,pos.add(anchor))
        //     }else{
        //         return
        //     }
        // }

        // L.Circle.prototype._animateZoom = function(e){
        //     if(this._map){
        //         var pos=this._map._latLngToNewLayerPoint(this._latlng,e.zoom,e.center),
        //         anchor=this._getAnchor()
        //         L.DomUtil.setPosition(this._container,pos.add(anchor))
        //     }else{
        //         return
        //     }
        // }
        // L.Marker.prototype._animateZoom = function(e){
        //     if(this._map){
        //         var pos=this._map._latLngToNewLayerPoint(this._latlng,e.zoom,e.center)
        //     }else{
        //         return
        //     }
        // }
        if(config.VUE_APP_SUPERMAP_BASE_FLAG){
            this.InitPlot()
        }
        
        
    },
    addCityToMap(typeName="省界", filterName=""){
        
        /**
         * 加载省市边界图
         * @typeName 边界图的类型
         * @filterName 显示的行政区域
        * **/
        const that = this;
        // window.Map2D.marker.map.on('zoom', handleMapZoom);
        // mapScale.value = window.Map2D.marker.map.getZoom();
        const url = config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services" + "/data-wj_gis_ZhongGuo_XingZhengJingJie/rest/data";
        /*** 动态设置请求参数 */
        const queryParameter = {
          name: typeName + "@wj_gis_中国_行政境界"
        }
        if (filterName) {
          queryParameter.attributeFilter = "PROVINCE = '" + filterName + "'";
        }
        const datasetNames = ["wj_gis_中国_行政境界:"+ typeName];
        const sqlParam = new L.supermap.GetFeaturesBySQLParameters({
          // queryParameter: {
          //   // name: "BorderA_L@wj_gis_中国_行政境界",
          //   name: "省界@wj_gis_中国_行政境界",
          //   // attributeFilter: "PROVINCE = '四川省'",
          //   attributeFilter: "NAME = '四川省'",
          // },
          queryParameter: queryParameter,
          datasetNames: datasetNames,
          toIndex: -1
        });
        new L.supermap.FeatureService(url).getFeaturesBySQL(sqlParam, (serviceResult) => {
          if (!serviceResult.result) {
            return;
          }
          var arr = []
          for(var i of serviceResult.result.features.features){
            arr.push({
                id: i.id,
                pac: i.properties.PAC,
                label: i.properties.NAME,
                value: i.properties.NAME
            })
          }
        //   const queryParameter2 = {
        //     name: typeName + "@wj_gis_中国_行政境界",
        //     attributeFilter: `NAME = '${arr[0].label}'`
        //   }
        //   const sqlParam2 = new L.supermap.GetFeaturesBySQLParameters({
        //     // queryParameter: {
        //     //   // name: "BorderA_L@wj_gis_中国_行政境界",
        //     //   name: "省界@wj_gis_中国_行政境界",
        //     //   // attributeFilter: "PROVINCE = '四川省'",
        //     //   attributeFilter: "NAME = '四川省'",
        //     // },
        //     queryParameter: queryParameter2,
        //     datasetNames: datasetNames,
        //     toIndex: -1
        //   });
        //   new L.supermap.FeatureService(url).getFeaturesBySQL(sqlParam2, (serviceResult) => {
        //     if (!serviceResult.result) {
        //       return;
        //     }
        //     L.geoJSON(serviceResult.result.features, {
        //       style: function(feature) {
        //         // //console.log(feature);
        //         return {
        //           fillColor: 'yellow',
        //           fillOpacity: 0.1,
        //           weight: 1
        //         }
        //       },
        //     }).addTo(that.map);
        //   })
          L.geoJSON(serviceResult.result.features, {
            style: function() {
              // //console.log(feature);
              return {
                fillColor: 'blue',
                fillOpacity: 0.1,
                weight: 1,
                borderColor: 'red'
              }
            },
          }).addTo(that.map);
        })
    },
    taskRemoves(){
        // map.eachLayer(function(layer) {
        //     //console.log(layer);
        //     if(!layer._url){
        //         map.removeLayer(layer);
        //     }
                
        // });
        const that = this;
        // 清除图层
        if (that.markerLayer !== null) {
            that.map.removeLayer(that.markerLayer)
        }
        if (window.markerLayer !== null) {
            that.map.removeLayer(window.markerLayer)
        }
        if (window.bdmarkerLayer !== null) {
            that.map.removeLayer(window.bdmarkerLayer)
        }
        
    },
    fkqyLayerRemove(){
        if (
            window.fkqyLayer != undefined &&
            window.fkqyLayer != null &&
            window.fkqyLayer != ""
          ) {
            // 清空图层
            window.fkqyLayer.clearLayers();
          }
    },
    xdMarkerLayerRemove(){
        if (
            window.xdMarkerLayer != undefined &&
            window.xdMarkerLayer != null &&
            window.xdMarkerLayer != ""
          ) {
            // 清空图层
            window.xdMarkerLayer.clearLayers();
          }
    },
    // 设置地图显示点和中心定位点
    addTiledMapLayer(url) {
        //console.log(url)
        const that = this
        let reqUrl = "";
        if (url.indexOf("http") === -1) {
            reqUrl = window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services" + url;
        } else {
            reqUrl = url;
        }
        
        that.tiledMapLayer = new L.supermap.tiledMapLayer(reqUrl,{
            transparent: true,
            subdomains: window.config.VUE_APP_SUPERMAPZYDK_BASE_URL,
            format:"webp"
        })
        
        that.tiledMapLayer.addTo(that.map)
        // 添加地图上的文字
        new L.supermap.tiledMapLayer(
            window.config.VUE_APP_SUPERMAP_BASE_URL +
            "/iserver/services/map-ugcv5-wj_gis_ZhongGuo_HangZhengJingJie/rest/maps/wj_gis_%E4%B8%AD%E5%9B%BD_%E8%A1%8C%E6%94%BF%E5%A2%83%E7%95%8C"
        ).addTo(that.map);
        // 添加地图上的公路线
        new L.supermap.tiledMapLayer(
            window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-ugcv5-wj_gis_ZhongGuo_GongLu/rest/maps/wj_gis_中国_公路"
            , {
                format: 'webp',
                attribution: false,
                subdomains: [8091, 8092, 8093]
            }
        ).addTo(that.map);


        // 添加地图上的铁路线
        // new L.supermap.tiledMapLayer(
        //     window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-ugcv5-wjgisZhongGuoTieLu/rest/maps/wj_gis_中国_铁路"
        //     , {
        //         format: 'webp',
        //         attribution: false,
        //         subdomains: [8091, 8092, 8093]
        //     }
        // ).addTo(that.map);
        // 添加地图上的机场线
        // new L.supermap.tiledMapLayer(
        //     window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-ugcv5-wjgisZhongGuoJiChang/rest/maps/wj_gis_中国_机场"
        //     , {
        //         format: 'webp',
        //         attribution: false,
        //         subdomains: [8091, 8092, 8093]
        //     }
        // ).addTo(that.map);
        // 添加地图上的火车站线
        // new L.supermap.tiledMapLayer(
        //     window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-ugcv5-wjgisZhongGuoHuoCheZhan/rest/maps/wj_gis_中国_火车站"
        //     , {
        //         format: 'webp',
        //         attribution: false,
        //         subdomains: [8091, 8092, 8093]
        //     }
        // ).addTo(that.map);
        // 添加地图上的汽车站线
        // new L.supermap.tiledMapLayer(
        //     window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-ugcv5-wjgisZhongGuoQiCheZhan/rest/maps/wj_gis_中国_汽车站"
        //     , {
        //         format: 'webp',
        //         attribution: false,
        //         subdomains: [8091, 8092, 8093]
        //     }
        // ).addTo(that.map);
        // 添加地图上的加油站线
        //   new L.supermap.tiledMapLayer(
        //     window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-ugcv5-wjgisZhongGuoJiaYouZhan/rest/maps/wj_gis_中国_加油站"
        //     , {
        //         format: 'webp',
        //         attribution: false,
        //         subdomains: [8091, 8092, 8093]
        //     }
        // ).addTo(that.map);
        // 添加地图上的桥梁线
        // new L.supermap.tiledMapLayer(
        //     window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-ugcv5-wjgisZhongGuoQiaoLiang/rest/maps/wj_gis_中国_桥梁"
        //     , {
        //         format: 'webp',
        //         attribution: false,
        //         subdomains: [8091, 8092, 8093]
        //     }
        // ).addTo(that.map);



    },
    // 设置地图显示点和中心定位点
    // addTiledMapLayer(url) {
        
    //     //console.log(url)
    //     const that = this
    //     let reqUrl = "";
    //     if (url.indexOf("http") === -1) {
    //         // reqUrl = window.config.VUE_APP_SUPERMAPZYIP_BASE_URL + ":{s}/iserver/services" + url;
    //         // window.config.VUE_APP_SUPERMAPZYDK_BASE_URL + 
    //         reqUrl = "/iserver/services" + url;
    //     } else {
    //         reqUrl = url;
    //     }
        
    //     that.tiledMapLayer = new L.supermap.tiledMapLayer(reqUrl,{
    //         transparent: true,
    //         subdomains: window.config.VUE_APP_SUPERMAPZYDK_BASE_URL,
    //         format:"webp"
    //     })
        
    //     that.tiledMapLayer.addTo(that.map)
    //     // 添加地图上的文字
    //     new L.supermap.tiledMapLayer("/iserver/services/map-ugcv5-wj_gis_ZhongGuo_HangZhengJingJie/rest/maps/wj_gis_%E4%B8%AD%E5%9B%BD_%E8%A1%8C%E6%94%BF%E5%A2%83%E7%95%8C"
    //     ).addTo(that.map);
    //     // 添加地图上的公路线
    //     new L.supermap.tiledMapLayer("/iserver/services/map-ugcv5-wj_gis_ZhongGuo_GongLu/rest/maps/wj_gis_中国_公路"
    //         , {
    //             format: 'webp',
    //             attribution: false,
    //             subdomains: [8091, 8092, 8093]
    //         }
    //     ).addTo(that.map);


    //     // 添加地图上的铁路线
    //     // new L.supermap.tiledMapLayer(
    //     //     window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-ugcv5-wjgisZhongGuoTieLu/rest/maps/wj_gis_中国_铁路"
    //     //     , {
    //     //         format: 'webp',
    //     //         attribution: false,
    //     //         subdomains: [8091, 8092, 8093]
    //     //     }
    //     // ).addTo(that.map);
    //     // 添加地图上的机场线
    //     // new L.supermap.tiledMapLayer(
    //     //     window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-ugcv5-wjgisZhongGuoJiChang/rest/maps/wj_gis_中国_机场"
    //     //     , {
    //     //         format: 'webp',
    //     //         attribution: false,
    //     //         subdomains: [8091, 8092, 8093]
    //     //     }
    //     // ).addTo(that.map);
    //     // 添加地图上的火车站线
    //     // new L.supermap.tiledMapLayer(
    //     //     window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-ugcv5-wjgisZhongGuoHuoCheZhan/rest/maps/wj_gis_中国_火车站"
    //     //     , {
    //     //         format: 'webp',
    //     //         attribution: false,
    //     //         subdomains: [8091, 8092, 8093]
    //     //     }
    //     // ).addTo(that.map);
    //     // 添加地图上的汽车站线
    //     // new L.supermap.tiledMapLayer(
    //     //     window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-ugcv5-wjgisZhongGuoQiCheZhan/rest/maps/wj_gis_中国_汽车站"
    //     //     , {
    //     //         format: 'webp',
    //     //         attribution: false,
    //     //         subdomains: [8091, 8092, 8093]
    //     //     }
    //     // ).addTo(that.map);
    //     // 添加地图上的加油站线
    //     //   new L.supermap.tiledMapLayer(
    //     //     window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-ugcv5-wjgisZhongGuoJiaYouZhan/rest/maps/wj_gis_中国_加油站"
    //     //     , {
    //     //         format: 'webp',
    //     //         attribution: false,
    //     //         subdomains: [8091, 8092, 8093]
    //     //     }
    //     // ).addTo(that.map);
    //     // 添加地图上的桥梁线
    //     // new L.supermap.tiledMapLayer(
    //     //     window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-ugcv5-wjgisZhongGuoQiaoLiang/rest/maps/wj_gis_中国_桥梁"
    //     //     , {
    //     //         format: 'webp',
    //     //         attribution: false,
    //     //         subdomains: [8091, 8092, 8093]
    //     //     }
    //     // ).addTo(that.map);
    // },
    // 删除图层
    removeAllLayer() {
        if (this.tiledMapLayer != null) {
            this.map.removeLayer(this.tiledMapLayer)
        }
    },
    setEwtyFlag(flag){
        console.log(flag);
        ewtyFlag = flag;
    },
    //地图右键菜单
    rightClick(evt) {
        //console.log(evt.latlng);
        // 判断是否地图上右击
        // if (!evt.originalEvent.target.id) {
        //     return
        // }
        window.zzwdEditObj = null
        window.zdjgEditObj = null
        window.qbxxEditObj = null

        
        var rigList = []
        if(window.location.href.includes("simulatedExercise")){
            rigList = [
                { text: '目标分析', iconname: 'zymb', click: `mbfx(${evt.latlng.lng},${evt.latlng.lat})` },
                // { text: '综合兵要', iconname: 'zymb', click: `dxdm(${evt.latlng.lng},${evt.latlng.lat})` },
                { text: '分析预测', iconname: 'zymb', click: `gjfx(${evt.latlng.lng},${evt.latlng.lat},${ewtyFlag})` },
                { text: '框选分析(显示框内目标兵要信息)', iconname: 'zymb', click: `kx()` },
                { text: '增加空域', iconname: 'zymb', click: `addKy()` },
                { text: '标绘', iconname: 'zymb', click: `Intelligence(${evt.latlng.lng},${evt.latlng.lat})` },
                // { text: '选点(地图选择一点)', iconname: 'zymb', click: `xd(${evt.latlng.lng},${evt.latlng.lat})` },
            ]
        }else{
            rigList = [
                { text: '目标分析', iconname: 'zymb', click: `mbfx(${evt.latlng.lng},${evt.latlng.lat})` },
                // { text: '综合兵要', iconname: 'zymb', click: `dxdm(${evt.latlng.lng},${evt.latlng.lat})` },
                { text: '分析预测', iconname: 'zymb', click: `gjfx(${evt.latlng.lng},${evt.latlng.lat},${ewtyFlag})` },
                { text: '框选分析(显示框内目标兵要信息)', iconname: 'zymb', click: `kx()` },
                { text: '增加空域', iconname: 'zymb', click: `addKy()` },
                
            ]
        }
        //console.log();
        var mapRigMenuHtm = function (o) {
            return `
          <div class='cd-span' style="padding:5px 10px; cursor: pointer;">
            <a onclick='${o.click}'>
             <img src='/static/map_img/${o.iconname}.png' style='vertical-align: middle;'>${o.text}</a></a>
          </div>`
        }

        var rigHtm = ''
        for (let i = 0; i < rigList.length; i++) {
            rigHtm += mapRigMenuHtm(rigList[i])
        }

        console.log(rigHtm, map);
        //添加地图弹出框
        L.popup({
            className: 'mypopup',
        }).setLatLng(evt.latlng).setContent(rigHtm).openOn(map)
    },

    InitPlot(){
        this.plottingLayer = L.supermap.plotting.plottingLayer("plot", this.serverUrl);
        this.plottingLayer.addTo(this.map);
        this.drawControl = L.supermap.plotting.drawControl(this.plottingLayer);
        this.drawControl.addTo(this.map);
        this.editControl = L.supermap.plotting.editControl();
        this.editControl.addTo(this.map);
        // 标绘符号随地图层级变化
        this.plottingLayer.enableSymScaleDefinition(false);
        this.drawControl.setDrawingLayer(this.plottingLayer);
        // L.supermap.plotting.initPlotPanel("plotPanel", this.serverUrl, this.drawControl);
        // L.supermap.plotting.initStylePanel("stylePanel", this.serverUrl, this.editControl);


        this.plotting = L.supermap.plotting.getControl(this.map, this.serverUrl);
        // this.map.on('contextmenu', function (event) {
        //     this.drawControl.handler.disable();
        // })
    },

    imgMarker(position, info) {
        let marker = '', iconUrl = '', pulseIcon = '';
        const markers = [];
        if (info.infoType == 'ZGZS') {
            iconUrl = '/static/map_img/icon/mapzgzs.png'
        } else if (info.infoType == 'ZDJG') {
            iconUrl = '/static/map_img/icon/mapzdjg.png'
        } else if (info.infoType == 'QB') {
            iconUrl = '/static/map_img/icon/qbxx.png'
        }
        pulseIcon = L.icon({
            iconUrl: iconUrl,
            iconSize: [40, 40],
        });
        //console.log(pulseIcon)
        var latLng = L.latLng(position[0], position[1])
        marker = L.marker(latLng, { icon: pulseIcon }).addTo(this.map)
        //console.log('11111', info);
        marker.bindPopup(`
            <div>类型：${info.infoType == 'ZGZS' ? '战果战损' : info.infoType == 'ZDJG' ? '战斗经过' : '情报'}</div>
            <div>时间：${info.SJSJ}</div>
            <div>坐标：${info.JD},${info.WD}</div>
            <div>${info.infoType == 'ZGZS' ? '战果战损简述' : info.infoType == 'ZDJG' ? '战斗经过' : '情报简述'}：${info.ZDMS || info.QBJS || info.ZDJG}</div>
        `).openPopup(marker.getLatLng())
        markers.push(marker);
        this.map.flyTo(position, 9)
    },

    // 加载省界，市界
    // filterName为显示的行政区域
    // typeName为边界图的类型

    loadingProvice(filterName, typeName) {
        const that = this;
        // 清除图层
        // if (that.markerLayer !== null) {
        //     that.map.removeLayer(that.markerLayer)
        // }
        // // 添加图层
        that.boundaryLayer = L.layerGroup([]);
        that.map.addLayer(that.boundaryLayer)
        var baseUrl = cfg2D.server.baseUrl1 + "/map-world/rest/maps/World";
        // var url = cfg2D.server.baseUrl + "iserver/services/data-wj_gis_ZhongGuo_XingZhengJingJie/rest/data";
        new L.supermap.TiledMapLayer(baseUrl).addTo(that.boundaryLayer);
        // var filterName = "天水市"
        // 显示的行政区域
        // var filterName = "甘肃省"
        // var typeName = "省界"
        var sqlParam = new L.supermap.GetFeaturesBySQLParameters({
            queryParameter: {
                name: typeName + "@wj_gis_中国_行政境界",
                // name: "市界@wj_gis_中国_行政境界",
                attributeFilter: "NAME = '" + filterName + "'"
            },
            datasetNames: ["wj_gis_中国_行政境界:" + typeName],
            // datasetNames: ["wj_gis_中国_行政境界:市界"],
            toIndex: -1
        });
        new L.supermap
            .FeatureService(process.env.VUE_APP_SUPERMAP_BASE_URL + '/iserver/services/data-wj_gis_ZhongGuo_XingZhengJingJie/rest/data')
            .getFeaturesBySQL(sqlParam, function (serviceResult) {
                //console.log(serviceResult.result.features, "9879878978979878")
                if (!serviceResult.result) {
                    return;
                }
                L.geoJSON(serviceResult.result.features, {
                    style: function () {
                        //console.log(feature);
                        return {
                            fillColor: 'blue',
                            fillOpacity: 0.1,
                            weight: 1,
                            borderColor: 'red',
                        }
                    },
                }).addTo(that.boundaryLayer)
            });
    },


    // 创建任务添加地图上的点
    AddTaskPoint(dataList) {

        const that = this;
        // 清除图层
        if (that.taskmarkerLayer !== null) {
            that.map.removeLayer(that.taskmarkerLayer)
        }
        // 添加图层
        that.taskmarkerLayer = L.layerGroup([]);
        that.map.addLayer(that.taskmarkerLayer)
        // this.loadingProvice("新疆维吾尔自治区", "省界")
        dataList.forEach(item => {
            if(item.jd && item.wd){
                var marker = L.marker([item.wd, item.jd],{ icon: L.icon.pulse({
                    iconSize: [20, 20],
                    color: '#f22'
                }) }).addTo(that.taskmarkerLayer);
                marker.bindPopup(item.ztmc, { closeOnClick: false, autoClose: false,className: 'special' }).openPopup(marker.getLatLng())
            }
            
        })

    },
    // 点击任务将当前任务的点，显示到18级
    getZoomList(content) {
        const that = this;
        // 清除图层
        if (that.taskmarkerLayer !== null) {
            that.map.removeLayer(that.taskmarkerLayer)
        }
        // 添加图层
        that.taskmarkerLayer = L.layerGroup([]);
        that.map.addLayer(that.taskmarkerLayer)
        // 修改地图层级
        // that.map.setZoom(18)
        // // 修改地图中心点
        that.map.setView([Number(content.wd), Number(content.jd)], 16);
        // 添加点在图层上
        var marker = L.marker([Number(content.wd), Number(content.jd)],{icon:L.icon.pulse({
            iconSize: [20, 20],
            color: '#f22'
        })}).addTo(that.taskmarkerLayer);
        marker.bindPopup(content.ztmc).openPopup(marker.getLatLng())
    },

    getZoomLists(content) {
        //console.log(content);
        if (content.级别 == "支队") {
            var greenIcon = L.icon({
                iconUrl: '/static/zhid.png',
                iconSize: [40, 40],
            });
        } else if (content.级别 == "中队") {
            var greenIcon = L.icon({
                iconUrl: '/static/zhongd.png',
                iconSize: [40, 40],
            });
        } else if (content.级别 == "总队") {
            var greenIcon = L.icon({
                iconUrl: '/static/zongd.png',
                iconSize: [40, 40],
            });
        } else if (content.级别 == "0") {
            var greenIcon = L.icon({
                iconUrl: '/static/zongd.png',
                iconSize: [40, 40],
            });
        } else if (content.级别 == "大队") {
            var greenIcon = L.icon({
                iconUrl: '/static/dad.png',
                iconSize: [40, 40],
            });
        }
        const that = this;
        // 清除图层
        // if (that.markerLayer !== null) {
        //     that.map.removeLayer(that.markerLayer)
        // }
        
        // 修改地图层级
        // that.map.setZoom(10)
        // // 修改地图中心点
        that.map.setView([content.wd, content.jd], 10);
        // 添加点在图层上
        var marker = L.marker([content.wd, content.jd],{icon:greenIcon}).addTo(window.bdmarkerLayer);
        marker.bindPopup(content.部队番号.replaceAll('中国人民武装警察部队新疆维吾尔自治区','')).openPopup(marker.getLatLng())
    },
    // 清除所有图层
    clearLayer() {
        const that = this;
        // 清除图层
        if (that.markerLayer !== null) {
            that.map.removeLayer(that.markerLayer)
        }
    },

    // 绘制圆圈，添加范围内的部队显示点
    drawLine(url, data) {
        const that = this;
        // 清除图层
        if (that.markerLayer !== null) {
            that.map.removeLayer(that.markerLayer)
        }
        if(that.clusterMarkers !==null){
            that.map.removeLayer(that.clusterMarkers)
        }
        // 添加图层
        that.markerLayer = L.layerGroup([]);
        that.map.addLayer(that.markerLayer)
        that.clusterMarkers = L.layerGroup([]);
        that.map.addLayer(that.clusterMarkers)
        // 将图层组添加到地图上
        // that.map.addControl(that.markerLayer);
        // 移动地图到当前坐标点，并到当前的地图级别
        that.map.setZoom(data[0].zoom)
        setTimeout(() => {

            that.map.flyTo([data[0].wd, data[0].jd])
            this.getCircleDataPoint(url, data)


        }, 200)
    },
    // 获取圆圈内的对应数据
    getCircleDataPoint(url, data) {
        //console.log(url, data);
        const that = this;
        var center = [data[0].wd, data[0].jd];//圆心点
        var radius = Number(data[0].km) * 1000;//圆的半径
        var bound = this.getCriclePoints(center, radius)

        var polygon = L.polygon(bound, { color: "blue" }).addTo(that.clusterMarkers)
        let reqUrl = "";
        if (url.indexOf("http") === -1) {
            reqUrl = cfg2D.server.baseUrl1 + url;
        } else {
            reqUrl = url;
        }
        var geometryParam = new L.supermap.GetFeaturesByGeometryParameters({
            datasetNames: ["wj_gis_bdfh:bdfh"],
            geometry: polygon,
            spatialQueryMode: "INTERSECT"
        });
        new L.supermap
            .FeatureService(reqUrl)
            .getFeaturesByGeometry(geometryParam, function (serviceResult) {
                var pointLine = serviceResult.result.features.features;
                //console.log(data[0], "cengsh")
                if (data[0].point && data[0].point == 0) {
                    return
                } else {
                    //console.log("data[0]")
                }
                // 存储范围内的全部部队信息
                const buduiMessage = pointLine.filter(item => {
                    var list = item.properties.部队番号.slice(-2)

                    if (list == "中队" || list == "支队") {
                        return item
                    }
                })

               
                countMessage.bduiDataList(buduiMessage)
                let greenIcon = null;
                // 添加图层，分别展示中队和支队
                buduiMessage.forEach(item => {
                    that.pathLengthLenght(item.properties, data)
                    if (item.properties.级别 == "支队") {
                         greenIcon = L.icon({
                            iconUrl: '/static/zhid.png',
                            iconSize: [40, 40],
                        });
                    } else if (item.properties.级别 == "中队") {
                         greenIcon = L.icon({
                            iconUrl: '/static/zhongd.png',
                            iconSize: [40, 40],
                        });
                    } else if (item.properties.级别 == "总队") {
                         greenIcon = L.icon({
                            iconUrl: '/static/zongd.png',
                            iconSize: [40, 40],
                        });
                    } else if (item.properties.级别 == "0") {
                         greenIcon = L.icon({
                            iconUrl: '/static/zongd.png',
                            iconSize: [40, 40],
                        });
                    } else if (item.properties.级别 == "大队") {
                         greenIcon = L.icon({
                            iconUrl: '/static/dad.png',
                            iconSize: [40, 40],
                        });
                    }
                    if (item.properties.部队番号.slice(-2) == "中队") {
                        var marker = L.marker(L.latLng(Number(item.properties.维度), Number(item.properties.经度)), { icon: greenIcon }).addTo(that.clusterMarkers);
                        marker.bindPopup(item.properties.部队番号).openPopup(marker.getLatLng())
                        marker.on('click', function (e) {
                            that.pathLength(e.latlng, data)
                        })
                    } else if (item.properties.部队番号.slice(-2) == "支队") {
                        var marker = L.marker(L.latLng(Number(item.properties.维度), Number(item.properties.经度)), { icon: greenIcon }).addTo(that.clusterMarkers);
                        marker.bindPopup(item.properties.部队番号).openPopup(marker.getLatLng())
                        marker.on('click', function (e) {
                            that.pathLength(e.latlng, data)
                            // findPathProcess("length", {sfd:e.latlng.lng+','+e.latlng.lat,mdd:data[0].jd+','+data[0].wd}, {hcqjl: 55},false,"rgb(0,55,255)");
                            // findPathProcess("Graed",  {sfd:item.维度+','+item.经度,mdd:data[0].jd+','+data[0].wd}, {hcqjl: 55},false,"rgb(255,0,47)");
                            // findPathProcess("time", {sfd:item.维度+','+item.经度,mdd:data[0].jd+','+data[0].wd,}, {hcqjl: 55},false,"rgb(0,222,255)");                
                        })
                    }
                })
            });
        setTimeout(() => {
            var marker1 = L.marker(L.latLng(data[0].wd, data[0].jd)).addTo(that.markerLayer)
            marker1.bindPopup(data[0].name).openPopup(marker1.getLatLng())
            //console.log(data[0], "79798789797897")
            // 存储结束位置
            countMessage.getendName(data[0])
        }, 100)
    },
   
    // 点击其他点时，调用，形成两个点之间的线段
    otherLength(start, end) {
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
            nodes: [L.point(Number(start.经度), Number(start.维度)), L.point(end[1], end[0])],
            parameter: analystParameter,
        });
        //进行查找
        findPathService.findPath(findPathParameter, function (serviceResult) {
            var result = serviceResult.result;
            result.pathList.map(function (result) {
                var num = 0;
                result.edgeFeatures.features.forEach(item => {
                    if (item.properties.SMLENGTH) {
                        num += Number(item.properties.SMLENGTH)
                    }
                })
                var geojson = L.geoJSON(result.route, { length: num, start: start.部队番号, points: { x: Number(start.经度), y: Number(start.维度) } });
                geojson.addTo(that.markerLayer);
                geojson.on('click', function () {
                })
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
    // 路线规划方法
    
 findPathProcess(
    weightFieldName,
    itemData,
    modelObj,
    hasLeastEdgeCount,
    color
  ) {
    if(resultData.result.length>=3){
        resultData.result= []
        countMessage.IsshowLine = false;
        
        if (window.markerLayer !== null) {
            this.map.removeLayer(window.markerLayer)
        }
        window.markerLayer = L.layerGroup([]);
        this.map.addLayer(window.markerLayer)
    }
    //创建最佳路径分析服务实例
    var findPathService = new window.L.supermap.NetworkAnalystService(
      window.config.VUE_APP_SUPERMAP_BASE_URL +
        "/iserver/services/transportationAnalyst-wj_gis_road/rest/networkanalyst/wj_gis_road_Network_new@wj_gis_road"
    );
    //创建最佳路径分析参数实例
    var resultSetting = new window.L.supermap.TransportationAnalystResultSetting({
      returnEdgeFeatures: false,
      returnEdgeGeometry: false,
      returnEdgeIDs: false,
      returnNodeFeatures: false,
      returnNodeGeometry: false,
      returnNodeIDs: false,
      returnPathGuides: true,
      returnRoutes: true,
    });
    var analystParameter = new window.L.supermap.TransportationAnalystParameter({
      resultSetting: resultSetting,
      hasLeastEdgeCount: hasLeastEdgeCount,
      weightFieldName: weightFieldName,
    });
    var pointArr = []; // 标记点参数集合
    pointArr.push(
      window.L.point(
        Number(itemData.sfd.split(",")[1]),
        Number(itemData.sfd.split(",")[0])
      )
    );

    pointArr.push(
      window.L.point(
        Number(itemData.mdd.split(",")[1]),
        Number(itemData.mdd.split(",")[0])
      )
    );
    var findPathParameter = new window.L.supermap.FindPathParameters({
      isAnalyzeById: false,
      nodes: pointArr,
      parameter: analystParameter,
    });
    // var myIcon = window.L.icon({
    //   // iconUrl: "../img/walk.png",
    //   iconSize: [20, 20],
    // });
    //进行查找
    findPathService.findPath(findPathParameter,  (serviceResult) => {
        var result =  serviceResult.result;
        
        var geojson;
        
        result.pathList.map(function (result) {
            result.weightFieldName = weightFieldName;
            result.color = color;
            // if(weightFieldName == 'time'){
            //     resultData.result[0]=result
            // }else if(weightFieldName == 'length'){
            //     resultData.result[1]=result
            // }else if(weightFieldName == 'Graed'){
            //     resultData.result[2]=result
            // }
            resultData.result.push(result)
          geojson = window.L.geoJSON(result.route, {
            color: color?color:"rgb(0,222,255)",
            weight: 5,
            noClip: false,
          }).addTo(window.markerLayer);
        });
        
        if(resultData.result.length==3){
            //console.log('xxxx',resultData.result)
          countMessage.resultData = resultData;
          countMessage.IsshowLine = true;
          countMessage.startName = itemData.startName
          countMessage.endName = window.TOOL.data.get("POSITION")?window.TOOL.data.get("POSITION").ztdd:window.TOOL.data.get("PKTY_USER_INFO").ztdd
          
        //   resultData.result= []
        }
        
      //   setTimeout(()=>{
      //     countMessage.resultData = resultData;
      //   countMessage.IsshowLine = true;
      //   },1000)
      //   if(weightFieldName == 'time'){
      //     resultData.jielun1='推荐路线时间最短,为：'+(result.pathList[0].weight / 60).toFixed(2)+'小时'
      //   }else if(weightFieldName == 'length'){
      //     resultData.jielun2='推荐路线路程最短,为：'+(result.pathList[0].weight / 1000).toFixed(2)+'公里'
      //   }else if(weightFieldName == 'Graed'){
      //     resultData.jielun3='推荐路线大路优先,为：'+(result.pathList[0].weight / 2).toFixed(2)+'公里'
      //   }
      // geojson.on('click',(res)=>{
      //     countMessage.start =Number(itemData.sfd.split(",")[1])+","+ Number(itemData.sfd.split(",")[0]);
      //     countMessage.end = Number(itemData.mdd.split(",")[1])+","+ Number(itemData.mdd.split(",")[0]);
      //     countMessage.resultData = resultData;
      //     countMessage.IsshowLine = true;
      //     lineSearch()
      // })
    
        
        var geoBufferAnalystParams =
          new window.L.supermap.GeometryBufferAnalystParameters({
            sourceGeometry: geojson,
            bufferSetting: new window.L.supermap.BufferSetting({
              endType: window.L.supermap.BufferEndType.ROUND,
              leftDistance: new window.L.supermap.BufferDistance({
                value: (modelObj.hcqjl / 11100).toFixed(4),
              }), // 需要进行换算，一度约等于11100米
              rightDistance: new window.L.supermap.BufferDistance({
                value: (modelObj.hcqjl / 11100).toFixed(4),
              }),
              semicircleLineSegment: 10,
            }),
          });
      });
  },
    // 添加两个点之间的路径
    pathLength(start, end) {
        //console.log('xxxxxxx',start, end);
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
            returnPathGuides: true,
            returnRoutes: true
        });
        var analystParameter = new L.supermap.TransportationAnalystParameter({
            resultSetting: resultSetting,
            weightFieldName: "SmLength"//权重
        });
        var findPathParameter = new L.supermap.FindPathParameters({
            isAnalyzeById: false,
            nodes: [L.point(start.lng, start.lat), L.point(end[0].jd, end[0].wd)],
            parameter: analystParameter,
        });
        
        var geojson = null;
        var resultData = null;
        //进行查找
        findPathService.findPath(findPathParameter, function (serviceResult) {

            var result = serviceResult.result;
            result.pathList.map(function (result) {
                // countMessage.zuiyouDataList(result.edgeFeatures.features, start)
                
                
                resultData = result;
                var num = 0;
                result.edgeFeatures.features.forEach(item => {
                    if (item.properties.SMLENGTH) {
                        num += Number(item.properties.SMLENGTH)
                    }
                })
                //console.log(num, "798798121232323312")
                countMessage.lineLengthList(num)
                geojson = L.geoJSON(result.route,);
                
                geojson.addTo(window.markerLayer);
                
            })
            geojson.on('click',()=>{
                countMessage.start =start.lng+","+ start.lat;
                countMessage.end = end[0].jd+","+ end[0].wd;
                countMessage.resultData = resultData;
                countMessage.IsshowLine = true;
                lineSearch()
            })
        });

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
    // 创建最优路径线段（最近设施分析）
    findClosetFacilitiesProcess(one, two, weightFieldName, hasLeastEdgeCount, color) {

        const that = this;
        var fileterDate = [];

        one.forEach(item => {
            fileterDate.push(L.latLng(item.geometry.coordinates[1], item.geometry.coordinates[0]))

        })
        //创建最近设施分析服务实例
        var findPathService = new L.supermap.NetworkAnalystService

            (window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/transportationAnalyst-wj_gis_road/rest/networkanalyst/wj_gis_road_Network_new@wj_gis_road");
        //创建最近设施分析参数实例
        var resultSetting = new L.supermap.TransportationAnalystResultSetting({
            returnEdgeFeatures: false,
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
            // turnWeightField: "TurnCost",
            weightFieldName: weightFieldName,
            hasLeastEdgeCount:hasLeastEdgeCount
            //length,time
            //  道路等级：lv

        });
        var findClosetFacilitiesParameter = new L.supermap.FindClosestFacilitiesParameters({
            //事件点,必设参数
            event: L.latLng(two[0], two[1]),
            //要查找的设施点数量。默认值为1
            expectFacilityCount: 1,
            //设施点集合,必设
            facilities: fileterDate,
            isAnalyzeById: false,
            parameter: analystParameter
        });
        //进行查找
        findPathService.findClosestFacilities(findClosetFacilitiesParameter, function (serviceResult) {

            var rersult = serviceResult.result;

            rersult.facilityPathList.map(function (result) {
                map2D.map.setView([two[1], two[0]], 15);
                // 添加最优路径的线段
                L.geoJSON(result.route, { color: color }).addTo(that.markerLayer);

                // 存储最优路径的数据
                //console.log(result, "787979798789797987987")
                countMessage.pointList({ x: result.facility.x, y: result.facility.y })
                // that.pathLength(result.facility, two, weightFieldName, hasLeastEdgeCount, color, result.weight)
                var arr = [];
                arr.push(result.facility.y);
                arr.push(result.facility.x)



            });
        });
    },
    /**
    * 添加地图监听事件()
    */
    addEventsToMap() {
        const that = this
        // 框选功能
        var startPoint;
        var tempRectangle;
        var isDrawing = false;
        window.kxRectLayer = L.layerGroup([]);
        window.kxRectLayer.addTo(this.map);

        // 框选功能
        var kystartPoint;
        window.ky = L.layerGroup([]);
        window.ky.addTo(this.map);
        that.map.off('click')
        that.map.off('mousedown')
        that.map.off('mousemove')
        that.map.off('mouseup')
        that.map.off('drag')
        that.map.off('dragend')
        
        that.map.on({
            "click": (e) => {
                if(!that.kxFlag && that.kyFlag){
                    if (!kystartPoint) {
                        // 第一次点击，记录起点
                        kystartPoint = e.latlng;
                    } else {
                        // 第二次点击，计算终点并绘制矩形
                        var endPoint = e.latlng;

                        // 使用turf.js计算距离
                        var distance = turf.distance([kystartPoint.lat, kystartPoint.lng], [endPoint.lat, endPoint.lng]);
                        console.log(distance);

                        eventBus.emit("addKy",{kystartPoint:kystartPoint,distance:distance});
                        // 重置起点
                        kystartPoint = null;
                        that.kyFlag = false;
                    }
                }else if(!that.kxFlag && that.xdFlag){
                    if (window.xdMarkerLayer != null) {
                        window.xdMarkerLayer.clearLayers()
                    }
                    L.marker([e.latlng.lat, e.latlng.lng]).addTo(window.xdMarkerLayer)
                    eventBus.emit("xd",{lat:e.latlng.lat,lng:e.latlng.lng});
                    that.xdFlag = false;
                }
                
            },
            "mousedown": (e) => {
                if(that.kxFlag && e.originalEvent && e.originalEvent.button === 0){
                    console.log('开始框选 - 起点:', e.latlng);
                    isDrawing = true;
                    startPoint = e.latlng;
                    that.map.dragging.disable();
                    e.originalEvent.preventDefault();
                    e.originalEvent.stopPropagation();
                }
            },
            "mouseup": (e) => {
                if(that.kxFlag && isDrawing && startPoint){
                    isDrawing = false;
                    that.map.dragging.enable();
                    // 清除临时矩形
                    if (tempRectangle) {
                        window.kxRectLayer.removeLayer(tempRectangle);
                        tempRectangle = null;
                    }
                    // 绘制最终矩形
                    window.rect = L.rectangle([startPoint, e.latlng], {
                        color: '#00ff00',
                        fillColor: '#00ff00',
                        fillOpacity: 0.15,
                        weight: 3
                    });
                    window.kxRectLayer.addLayer(window.rect);

                    // 获取四个角的经纬度
                    const bounds = window.rect.getBounds();
                    const corners = {
                        northWest: [bounds.getNorth(), bounds.getWest()],
                        northEast: [bounds.getNorth(), bounds.getEast()],
                        southEast: [bounds.getSouth(), bounds.getEast()],
                        southWest: [bounds.getSouth(), bounds.getWest()]
                    };
                    console.log('框选四个角的经纬度:', corners);

                    // 存储四个角的经纬度
                    window.kxCorners = corners;

                    // 调用框选查询接口
                    zbmbApi.queryBySquareArea({
                        northWest: corners.northWest,
                        northEast: corners.northEast,
                        southEast: corners.southEast,
                        southWest: corners.southWest
                    }).then(res => {
                        console.log('框选查询结果:', res);
                        if (res.success && res.result && res.result.length) {
                            // 清除之前的标记
                            if (this.clusterMarkers) {
                                this.clusterMarkers.clearLayers();
                            }
                            // 初始化标记图层
                            if (!this.kxMarkerLayer) {
                                this.kxMarkerLayer = window.L.layerGroup([]);
                                this.kxMarkerLayer.addTo(this.map);
                            } else {
                                this.kxMarkerLayer.clearLayers();
                            }
                            
                            // 遍历数据并添加标记
                            res.result.forEach((item) => {
                                let zbmbIcon;
                                if(item.mc && item.mc.indexOf('医院')!=-1){
                                    zbmbIcon = window.L.icon({
                                        iconUrl: "/static/map_img/医院.png",
                                        iconSize: [40, 40],
                                    });
                                }else if(item.mc && item.mc.indexOf('学校')!=-1){
                                    zbmbIcon = window.L.icon({
                                        iconUrl: "/static/map_img/高等院校.png",
                                        iconSize: [40, 40],
                                    });
                                }else if(item.mc && item.mc.indexOf('加油站')!=-1){
                                    zbmbIcon = window.L.icon({
                                        iconUrl: "/static/map_img/加油站.png",
                                        iconSize: [40, 40],
                                    });
                                }else{
                                    zbmbIcon = window.L.icon({
                                        iconUrl: "/static/map_img/重要目标.png",
                                        iconSize: [40, 40],
                                    });
                                }
                                
                                if(item.jd && item.wd){
                                    const marker = window.L.marker(
                                        window.L.latLng(Number(item.wd), Number(item.jd)),
                                        {
                                            icon: zbmbIcon,
                                        }
                                    ).addTo(this.kxMarkerLayer);
                                    
                                    const html = `<div style="width:140px;background:rgba(30, 32, 44);padding:10px">
                                                  <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                                                    <div style="width:100%;margin: 4px 0;color:#fff;">名称：<span style="color:#fff;">${item.mc}</span></div>
                                                    <div style="width:100%;margin: 4px 0;color:#fff;">经度：<span style="color:#fff;">${item.jd}</span></div>
                                                    <div style="width:100%;margin: 4px 0;color:#fff;">纬度：<span style="color:#fff;">${item.wd}</span></div>
                                                  </div>
                                                </div>`;
                                    marker
                                        .bindPopup(item.mc)
                                        .bindTooltip(html);
                                    marker.on("click", function (e) {
                                        if(window.kxDetail){
                                            window.kxDetail(item);
                                        }
                                    });
                                }
                            });
                        }
                    }).catch(err => {
                        console.error('框选查询失败:', err);
                    });

                    window.rect.on('contextmenu', function(evt){
                        console.log('layer',evt.layer);
                        var rigList = [
                            { text: '清除', iconname: 'gr', click: `clearRect(${evt.latlng.lng},${evt.latlng.lat})` },
                        ]
                        //console.log();
                        var mapRigMenuHtm = function (o) {
                            return `
                              <div class='cd-span' style="padding:5px 10px; cursor: pointer;">
                                <a onclick='${o.click}'>
                                 <img src='/static/map_img/${o.iconname}.png' style='vertical-align: middle;'>${o.text}</a></a>
                              </div>`
                        }
                    
                        var rigHtm = ''
                        for (let i = 0; i < rigList.length; i++) {
                            rigHtm += mapRigMenuHtm(rigList[i])
                        }
                            
                        //添加地图弹出框
                        L.popup({
                            className: 'mypopup',
                        }).setLatLng(evt.latlng).setContent(rigHtm).openOn(that.map)
                        evt.stopPropagation(); // 阻止触发关联级回调事件
                    })
                    // 重置起点
                    startPoint = null;
                    that.kxFlag = false;
                }
            },
            "pm:create": () => {
                //console.log(e, "79878787798")
            },
            // 监听鼠标移入地图。获取鼠标在当前点的经纬度，以及高度
            "mousemove": e => {
                const curMapMode = getCurrentMapMode();
                if (curMapMode === mapModeEnum["2D"]) {
                    coordStore.setCoord({ x: e.latlng.lng, y: e.latlng.lat, z: 0,zoom:e.sourceTarget._zoom });
                }
                // 框选功能：拖拽时显示临时矩形
                if(that.kxFlag && isDrawing && startPoint){
                    console.log('拖拽中 - 当前点:', e.latlng);
                    if (tempRectangle) {
                        window.kxRectLayer.removeLayer(tempRectangle);
                    }
                    tempRectangle = L.rectangle([startPoint, e.latlng], {
                        color: '#ff0000',
                        fillColor: '#ff6b6b',
                        fillOpacity: 0.4,
                        weight: 5,
                        dashArray: '10, 5',
                        opacity: 1
                    });
                    window.kxRectLayer.addLayer(tempRectangle);
                }
            },
            "moveend": () => {
                // if (center_point != null) {
                //     center_point.clearLayers()
                //   }
                //   var icon = L.icon({
                //     iconUrl: '/static/mapDataIcon/mapCenterIcon.png',
                //     iconSize: [64, 64],
                //     iconAnchor:[32, 32]
                //   })
                //   // //console.log([that.map2d.getCenter().lat, that.map2d.getCenter().lng]);
                //   L.marker([that.map.getCenter().lat, that.map.getCenter().lng], { icon: icon }).addTo(center_point)
                //     // .openPopup()

                //     window.TOOL.data.set("CENTER_POINT",{jd: that.map.getCenter().lng,wd: that.map.getCenter().lat})
                //     //console.log({jd: that.map.getCenter().lng,wd: that.map.getCenter().lat});
                //     countMessage.getMapCenterPoint({jd: that.map.getCenter().lng,wd: that.map.getCenter().lat})
            },

        })
    },
    // 获取重要目标
    getZymbData(){
        this.map.closePopup()
        // getJBSJ_WJJBSJ(this.rightLayer).then(res => {
        //   this.addMarkerJh(res[0].result.features.features)
        // })
        this.clearLayer(true)
        window.API.gisDataService.getCollectDataList({"tagNameParamList":["综合保障"]}).then(res=>{
          if(res.success){
            this.zymbTableData = []
            var arr = res.data; // .filter((row,index)=>{return row.regionCodeName == this.$TOOL.data.get("DEPARTS").address})
            this.collectDataList = arr
            for(var i of arr){
              var obj = i.dataServiceList.filter(row=>{return row.supplierServiceTypeCode == "SUPERMAP-REST-DATA"})[0]
              if(obj == undefined) continue
              // if((i.dataName.split("_")[i.dataName.split("_").length-1]).indexOf("公路段") > -1) continue
              if(i.dataName.indexOf("公路段") > -1) continue
              var path = "/iserver/services" + obj.serviceProxyUrl.split("/iserver/services")[1] + "/data"
              // this.getDataPoint(path,i.datasource,i.datasetName,i.dataName.split("_")[i.dataName.split("_").length-1],arr.indexOf(i))
              if(i.dataName.split("_").length>1){
                this.getDataPoint(path,i.datasource,i.datasetName,i.dataName.split("_")[1],arr.indexOf(i))
              }else{
                this.getDataPoint(path,i.datasource,i.datasetName,i.dataName.split("_")[0],arr.indexOf(i))
              }
            }
          }
        })
      },
      async getDataPoint(path,datasource,datasetName,name,endNum){
        const url = window.config.VUE_APP_SUPERMAP_BASE_URL + path;
        const geometryParam = new window.SuperMap.GetFeaturesByGeometryParameters({
          datasetNames: [`${datasource}:${datasetName}`],
          geometry: window.rect,
          spatialQueryMode: "INTERSECT",
          fromIndex: 0,
          toIndex: 10000,
        })
        await window.L.supermap.featureService(url).getFeaturesByGeometry(geometryParam, serviceResult => {
            console.log('serviceResult',serviceResult);
          for(var i of serviceResult.result.features.features){
            i.properties.type = name
            this.zymbTableData.push(i.properties)
          }
          // 当查询到最后一组时调用显示标记点
          if(endNum == this.collectDataList.length-1){
            setTimeout(() => {
              this.addMarkerJh(this.zymbTableData)
            }, 200);
          }
        })
      },
      addMarkerJh(arr) {
        this.clearLayer(true)
        
        // 初始化 clusterMarkers
        if (!this.clusterMarkers) {
            this.clusterMarkers = L.layerGroup([]);
            this.map.addLayer(this.clusterMarkers);
        }
  
        for(var i of arr){
          var iconName = i.type;
          if(i.type.indexOf("加油站")>-1) iconName = "加油站"
          if(i.type.indexOf("高等院校")>-1) iconName = "高等院校"
          if(i.type.indexOf("物资")>-1) iconName = "重要目标"
          if(i.type.indexOf("服务区")>-1) iconName = "服务区"
          if(i.type.indexOf("机场")>-1) iconName = "飞机场"
          var myIcon = window.L.icon({
            iconUrl: '/static/mapDataIcon/'+iconName+'.png',
            data: i,
            // iconSize: [30, 30],
            iconAnchor: [16, 20]//文字标注相对位置
          });
          // console.log(row);
          i.JD = i.JD || (i['坐标经度']?this.DegreeConvertBack(i['坐标经度'].split(":")[i['坐标经度'].indexOf(":")>-1?1:0]):'') || (i['经度']?this.DegreeConvertBack(i['经度'].split(":")[i['经度'].indexOf(":")>-1?1:0]):'')
          i.WD = i.WD || (i['坐标纬度']?this.DegreeConvertBack(i['坐标纬度'].split(":")[i['坐标纬度'].indexOf(":")>-1?1:0]):'') || (i['纬度']?this.DegreeConvertBack(i['纬度'].split(":")[i['纬度'].indexOf(":")>-1?1:0]):'')
          
          var innerHTML = `<div style="background:#262716e0;background-size:100% 100%;color: #fff;padding: 8px;max-width:300px;">
          <div style="width:100%;display:flex;flex-wrap: wrap;">
              <div style="width:100%;margin: 4px 0;">名称：<span style="color:orange;">${i.MC || i.NAME || i['名称'] || i['机场名称'] || i['服务区名称'] || i['车站名称'] || i['渡口名称'] || i['地名'] || i.mc}</span></div>
              <div style="width:100%;margin: 4px 0;">类别：<span style="color:orange;">${i.type || i.subtype}</span></div>
              <div style="width:100%;margin: 4px 0;">经度：<span style="color:orange;">${i.JD || i.jd}</span></div>
              <div style="width:100%;margin: 4px 0;">纬度：<span style="color:orange;">${i.WD || i.wd}</span></div>`;
          
          if(i.rylx) {
              innerHTML += `<div style="width:100%;margin: 4px 0;">人员类型：<span style="color:orange;">${i.rylx}</span></div>`;
          }
          if(i.sl) {
              innerHTML += `<div style="width:100%;margin: 4px 0;">数量：<span style="color:orange;">${i.sl}</span></div>`;
          }
          if(i.zb) {
              innerHTML += `<div style="width:100%;margin: 4px 0;">装备：<span style="color:orange;">${i.zb}</span></div>`;
          }
          if(i.ssgk) {
              innerHTML += `<div style="width:100%;margin: 4px 0;">设施概况：<span style="color:orange;">${i.ssgk}</span></div>`;
          }
          
          innerHTML += `</div></div>`;
          var points = [Number(i.WD), Number(i.JD)];
          window.L.marker(points, {
            icon: myIcon,
          }).addTo(this.clusterMarkers)
            .bindPopup(innerHTML)
            // .bindTooltip(innerHTML)
            // .openPopup().on("click",(evt=>{
            //   console.log(evt.target.options.icon.options.data)
            // }))
        }
      },
      /**
     * 度分秒转经纬度
     */
    DegreeConvertBack(value, len) {
        len = len > 6 || typeof len == 'undefined' ? 6 : len
        if (value == '') {
          return value
        }
        var du = parseFloat(value.split('°')[0])
        var fen = parseFloat(value.split('°')[1].split('′')[0]) / 60
        var miao = parseFloat(value.split('°')[1].split('′')[1].split('″')[0]) / 3600
        var digital = du + fen + miao
        return digital.toFixed(len)
      },
      clearLayer(flag=false){
        // 清除其他目标点
        if (this.clusterMarkers !== undefined && this.clusterMarkers !== '' && this.clusterMarkers !== null) {
          this.clusterMarkers.clearLayers()
        }
      },
    clearRect(){
        const that = this;
        // 清除图层
        if (window.kxRectLayer) {
            window.kxRectLayer.clearLayers();
        }
        // 清除标记
        if (this.kxMarkerLayer) {
            this.kxMarkerLayer.clearLayers();
        }
    },
    // 根据分析的数据在地图上标点
    getPonit(jd, wd, name) {
        const that = this;
        // 清除图层
        // if (that.markerLayer !== null) {
        //     that.map.removeLayer(that.markerLayer)
        // }
        // 添加图层
        that.markerLayer = L.layerGroup([]);
        that.map.addLayer(that.markerLayer)
        var marker1 = L.marker(L.latLng(wd, jd)).addTo(that.markerLayer)
        marker1.bindPopup(name).openPopup(marker1.getLatLng())
    },
    // 获取图层的点
    getPointList(item, name) {
        axios({
            url: config.VUE_APP_API_BASE_URL + window.config.API_URL+"/battledamage/zzll_gjgwry_zhy/getPathInfo?bdfh=" + name,
            method: "get",
        }).then((res) => {
            if (res.data.code == 200) {
                //console.log(item, "46454564", countMessage.endPoint)

                var arr = { lng: item.x, lat: item.y }
                var arr1 = [{ jd: countMessage.endPoint[1], wd: countMessage.endPoint[0] }]
                this.clusterMarkers.eachLayer(layer => {
                    if (layer instanceof L.Marker && layer.getLatLng().lng == item.x && layer.getLatLng().lat == item.y) {
                      
                            let currentContent = layer.getPopup().getContent()
                            //console.log(layer, "9879798789789")
                            layer.bindPopup(`<div>
                            <p style="margin:10px 0">指挥员:${res.data.result.zhy?res.data.result.zhy:''}</p>
                            <p style="margin:10px 0">编制数:${res.data.result.bzs?res.data.result.bzs:''}人</p>
                            <p style="margin:10px 0">在位数:${res.data.result.zws?res.data.result.zws:''}人</p>
                            <p style="margin:10px 0">可出动数:${res.data.result.kcds?res.data.result.kcds:''}人</p>
                            </div>`).openPopup(layer.getLatLng(),)
                        
                    }
                })
            }
        })
    }



    //  pulseIcon = L.icon.pulse({
    //     iconSize: [12, 12],
    //     color: 'red'
    // });
    // 添加的图标为闪烁的动图
    //   markers.push(L.marker(latLng, {icon: pulseIcon}));
}