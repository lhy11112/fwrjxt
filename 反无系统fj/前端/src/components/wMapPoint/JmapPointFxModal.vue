<template>
    <AnalysisMoveDlg
    title="选择坐标点"
    style="width: 70%;height:60vh"
    :visibleDialog="visible"
    @close="handleCancel"
    isModal="true"
  >
      <div id="pointMap" v-loading="isLoading">
        
      </div>
      <div class="text">
        <!-- <el-input v-model="cityNameVal" style="width: 200px;" @keyup.enter="addressFit(cityNameVal)"></el-input> -->
        <el-input v-model="cityNameVal" style="width: 200px;" @keyup.enter="handleInput" @input="handleInput"></el-input>
        <div class="selectOption" v-show="searcList.length>0 && flag">
          <div v-for="(item,index) in searcList" :key="index" style="cursor:pointer;" @click="selectClick(item)">{{item.address}}</div>
        </div>
        <el-switch
          v-if="army"
          v-model="armyVal"
          @change="armyChange"
          inline-prompt
          style="--el-switch-on-color: #cf8a00;"
          active-text="部队"
          inactive-text="部队"
        />
        <br>
        纬度: {{ToDegrees(latlng.lat)}}
        <br>
        经度: {{ToDegrees(latlng.lng)}}
      </div>
     <div class="create-bottom">
      <el-button style="background: transparent;color: #000;" @click="handleCancel">取 消</el-button>
      <el-button :loading="isSaveing" style="background: #CF8A00;color: #fff;" @click="handleSubmit">确 定</el-button>
    </div>
    </AnalysisMoveDlg>
</template>

<script>
  import axios from 'axios'
  import mapCenterPoint from "./mapCenterPoint.json"
  import icon from "@/assets/images/marker-icon.png";
  import { getZZLL_BDBS, getJBSJ_WJJBSJ,getImageUrl,getJdlxImageUrl } from '@/api/common'
  export default {
    name: 'JmapPointModal',
    props:['modalWidth','army','pointValue'],
    data(){
      return {
        map: null,
        markerLayer: null,
        latlng: {
          lat: "",
          lng: ""
        },
        visible:false,
        isLoading: false,
        searchValue:"",
        pointValue2: "",
        cityNameVal: "",
        armyVal: false,
        armyLayer: null,
        searcList:[],
        flag:false,
      }
    },
    // watch:{
    //   pointValue:{
    //     deep: true,
    //     immediate: true,
    //     handler(val){
    //       console.log(val);
    //     }
    //   }
    // },
    methods:{
      initMap(){
        if(this.map) return
        // this.map = Map2D.init("pointMap");
        this.map = L.map("pointMap", {
            crs: L.CRS.EPSG4326,
            center: [40.92278125,101.5980283],
            maxZoom: 18,
            minZoom: 4,
            zoom: 4,
            // 放大缩小的控制按钮
            zoomControl: false,
            // 控制右下角的图标显示
            attributionControl: false,
            logoControl: false,
        });
        new L.supermap.tiledMapLayer(window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-multiTiles/rest/maps/wj_gis_聚合影像").addTo(this.map)
        new L.supermap.tiledMapLayer(window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-ugcv5-wj_gis_ZhongGuo_HangZhengJingJie/rest/maps/wj_gis_%E4%B8%AD%E5%9B%BD_%E8%A1%8C%E6%94%BF%E5%A2%83%E7%95%8C").addTo(this.map)
        
        // Map2D.addTiledMapLayer("/map-multiTiles/rest/maps/wj_gis_聚合影像"); 
        
        this.map.on("click",(e)=>{
          const x = e.latlng.lng
          const y = e.latlng.lat
          if (x < -180.0 || x > 180.0 || y < -90 || y > 90) {
            return
          }

          //经纬度转换度分秒
          const thisx = this.ToDegrees(x)
          const thisy = this.ToDegrees(y)
          //初始化定位点显示内容
          let showLocation = ''
          showLocation = '经度：' + thisx + '<br>' + '纬度：' + thisy
          // console.log(e);
          this.cityNameVal = ""
          this.latlng = e.latlng
          this.pointValue2 = Object.values(this.latlng).join()
          if (this.markerLayer !== undefined && this.markerLayer !== '' && this.markerLayer !== null) {
            this.markerLayer.clearLayers()
          }
          this.markerLayer = L.layerGroup().addTo(this.map);
          var greenIcon = L.icon({
            iconUrl: icon,
            iconSize: [40, 40],
          });
          var marker = L.marker(L.latLng(Number(e.latlng.lat), Number(e.latlng.lng)), { icon: greenIcon }).addTo(this.markerLayer).bindPopup(showLocation).openPopup();
          this.map.addLayer(this.markerLayer)
          
          
        })

        this.armyLayer = L.layerGroup().addTo(this.map);

        // this.addCityToMap()
      },
      // 经纬度转度分秒
      ToDegrees(val) {
        if (typeof val == 'undefined' || val == '') {
          return ''
        }
        // 把num类型转换成为string
        val = val + ''
        let i = val.indexOf('.')
        var strDu = i < 0 ? val : val.substring(0, i)
        let strFen = 0
        let strMiao = 0
        if (i > 0) {
          strFen = '0' + val.substring(i)
          strFen = Number(strFen) * 60 + ''
          i = strFen.indexOf('.')
          if (i > 0) {
            strMiao = '0' + strFen.substring(i)
            strFen = strFen.substring(0, i)
            strMiao = Number(strMiao ) * 60 + ''
            i = strMiao.indexOf('.')
            strMiao = strMiao.substring(0, i + 4)
            strMiao = parseFloat(strMiao).toFixed(2)
          }
        }
        return strDu + '°' + strFen + '′' + strMiao + '″'
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
      // 添加地图边界
      addCityToMap(typeName, filterName){
          /**
           * 加载省市边界图
           * @typeName 边界图的类型
           * @filterName 显示的行政区域
          * **/
          const that = this;
          // window.Map2D.marker.map.on('zoom', handleMapZoom);
          // mapScale.value = window.Map2D.marker.map.getZoom();
          const url = window.config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services" + "/data-wj_gis_ZhongGuo_XingZhengJingJie/rest/data";
          /*** 动态设置请求参数 */
          const queryParameter = {
            name: typeName + "@wj_gis_中国_行政境界",
            // attributeFilter: "NAME = '四川省'",
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
            L.geoJSON(serviceResult.result.features, {
              style: function() {
                // console.log(feature);
                return {
                  fillColor: 'blue',
                  fillOpacity: 0.1,
                  weight: 1,
                  borderColor: 'red',
                }
              },
            }).addTo(that.map);
          })
        },
      show(){
        this.visible=true
        this.$nextTick(()=>{
          this.initMap()
          this.markerLayer = L.layerGroup().addTo(this.map);
          var greenIcon = L.icon({
            iconUrl: icon,
            iconSize: [40, 40],
          });
          if(this.pointValue){
            this.latlng = {
              lat: this.pointValue.split(",")[0],
              lng: this.pointValue.split(",")[1]
            }
            this.$nextTick(()=>{
              L.marker(L.latLng(Number(this.pointValue.split(",")[0]), Number(this.pointValue.split(",")[1])), { icon: greenIcon }).addTo(this.markerLayer);
              this.map.addLayer(this.markerLayer)
              // 自动定位到坐标点位置
              if(this.pointValue){
                this.$nextTick(()=>{
                  this.map.setView(this.pointValue.split(","), 4)
                })
              }
            })
          }
        })
      },
      handleSubmit(){
        if(this.pointValue2){
          const pinitArr = this.pointValue2.split(',');
          console.log(pinitArr);
          pinitArr[0] = Number(pinitArr[0]).toFixed(8)
          pinitArr[1] = Number(pinitArr[1]).toFixed(8)
          this.pointValue2 = pinitArr.join(',')
          this.$emit("ok",this.pointValue2,this.cityNameVal)
          this.handleClear()
        }else{
          this.$message.warning("请选择坐标点")
        }
      },
      handleCancel(){
        this.handleClear()
        this.$emit("handleCancel")
      },
      handleClear(){
        if (this.markerLayer !== undefined && this.markerLayer !== '' && this.markerLayer !== null) {
          this.markerLayer.clearLayers()
        }
        this.visible=false
      },
      // 地址匹配
      async addressFit(address) {
        this.isLoading = true
        const sendReq = await axios({
          url: window.config.VUE_APP_MODEL + "/v1/chat/completions",
          method: 'post',
          timeout: 100000,
          data: {
            model: 'chatglm3-6b-32k',
            messages: [
              {
                role: 'system',
                content:
                  '你叫艾武，你是一位地理向导工作人员，接下来我会向你询问地址的位置信息，请根据我询问的内容提取地址名称(address)，整理出该地址所在的省(province)、市(city)详细名称（例如新疆的详细名称为新疆维吾尔自治区），以Json格式输出结果，如{"address":"XXX", "province":"XX", "city":"XX"}',
              },
              {
                role: 'user',
                content: '乌鲁木齐地窝堡机场发生劫持人质事件，请派遣附近20公里范围内的人员立即前往支援，地图显示14级',
              },
              {
                role: 'assistant',
                content:
                  '{ "province": "新疆维吾尔自治区", "city": "乌鲁木齐市", "address": "地窝堡机场", "distance": "20000", "level": "14" }',
              },
              {
                role: 'user',
                content:
                  '西安已经连续三天下暴雨，三桥地铁站被淹没，请派遣附近10公里范围内的人员立即前往救灾，地图显示13级',
              },
              {
                role: 'assistant',
                content:
                  '{ "province": "陕西省", "city": "西安市", "address": "三桥地铁站", "distance": "10000", "level": "13"  }',
              },
              {
                role: 'user',
                content: address,
              },
            ],
            stream: false,
            max_tokens: 300,
            temperature: 0.8,
            top_p: 0.8,
          },
        }).then(res => {
          return res
        })
        const recv = sendReq.data.choices[0].message.content
        // const info = JSON.parse(recv.slice(recv.indexOf('{'),recv.indexOf('}')+1));
        const info = JSON.parse(recv)
        const geoCodeParam = {
          address: info.address, // 地址
          fromIndex: 0, // 设置返回对象的起始索引值
          toIndex: 10, // 设置返回对象的结束索引值
          filters: '[' + info.province + ',' + info.city + ']', // 过滤条件
          maxReturn: 1, // 最大返回结果数
        }
        axios({
          url: '/iserver/services/addressMatch-Index/restjsr/v1/address/geocoding',
          method: 'get',
          headers: {
            'Access-Control-Allow-Origin': '*',
            'Content-Type': "application/json;charset=UTF-8"
          },
          timeout: 10000,
          params: geoCodeParam,
        }).then(res => {
          this.latlng.lat = res.data[0].location.y
          this.latlng.lng = res.data[0].location.x
          this.pointValue2 = Object.values(this.latlng).join()
          if (this.markerLayer !== null) {
            this.map.removeLayer(this.markerLayer)
          }
          this.markerLayer = L.layerGroup([]);
          var greenIcon = L.icon({
            iconUrl: icon,
            iconSize: [40, 40],
          });
          var marker = L.marker(L.latLng(res.data[0].location.y, res.data[0].location.x), { icon: greenIcon }).addTo(this.markerLayer);
          this.map.addLayer(this.markerLayer)
          this.map.setView([res.data[0].location.y, res.data[0].location.x], 6)
          // that.findPathProcess(
          //   [this.currentTask.lat, this.currentTask.lng],
          //   [res.data[0].location.y, res.data[0].location.x],
          //   'SmLength',
          //   'red'
          // )
          this.isLoading = false
        }).catch(()=>{
          this.isLoading = false
        })
      },
      armyChange(val){
        if(val){
          this.selectArmy()
        }else{
          if (this.armyLayer !== undefined && this.armyLayer !== '' && this.armyLayer !== null) {
            this.armyLayer.clearLayers()
          }
        }
      },
      // 选择部队方法
      selectArmy(){
        var address = this.$TOOL.data.get("USER_INFO").address
        var point = {}
        for(var i of mapCenterPoint){
          if(i.province == address){
            point = i
              this.map.setView([i.y,i.x], 7)
            break;
          }
        }
        if(point.y){
          var center = [Number(point.y), Number(point.x)];//圆心点
          var radius = Number(100) * 1000;//圆的半径
          var bound = Map2D.getCriclePoints(center, radius)
          var polygon = L.polygon(bound, { color: "blue" })
          getZZLL_BDBS(polygon).then(res => {
            if (this.armyLayer !== undefined && this.armyLayer !== '' && this.armyLayer !== null) {
              this.armyLayer.clearLayers()
            }
            var pointLine = res[0].result.features.features;
            pointLine.forEach(item => {
              var greenIcon = L.icon({
                iconUrl: getImageUrl(item.properties.级别),
                iconSize: [40, 40],
              });
              var marker = L.marker(L.latLng(Number(item.properties.维度), Number(item.properties.经度)), { icon: greenIcon }).addTo(this.armyLayer);
              marker.bindPopup(item.properties.部队番号) //.openPopup(marker.getLatLng())
              marker.on('click',e=>{
                console.log(e.target._popup._content);
                this.cityNameVal = e.target._popup._content
                this.latlng = e.latlng
                this.pointValue2 = Object.values(this.latlng).join()
                // 清除地图点击添加的其他标记点
                if (this.markerLayer !== undefined && this.markerLayer !== '' && this.markerLayer !== null) {
                  this.markerLayer.clearLayers()
                }
              })
            })
          })
        }
      },
      handleInput(val){
        if(val){
          this.flag=true;
          this.searcList = []
          //调用服务根据名称查询地点信息；
          var address = val
          var geoCodeParam = new SuperMap.GeoCodingParameter({
            address: address,
            fromIndex: 0,
            toIndex: 20,
          })
          var addressMatchService = L.supermap.addressMatchService(
            window.config.VUE_APP_SUPERMAP_BASE_URL+'/iserver/services/addressMatch-Index/restjsr/v1/address'
          )
          addressMatchService.code(geoCodeParam, this.codeMatchBest)
        }else{
          this.flag = false;
        }
      },
      selectClick(e){
        this.flag=false;
        this.cityNameVal = e.address
        this.latlng.lat = e.y
        this.latlng.lng = e.x
        this.pointValue2 = Object.values(this.latlng).join()
        if (this.markerLayer !== null) {
          this.map.removeLayer(this.markerLayer)
        }
        this.markerLayer = L.layerGroup([]);
        var greenIcon = L.icon({
          iconUrl: icon,
          iconSize: [40, 40],
        });
        var marker = L.marker(L.latLng(this.latlng.lat, this.latlng.lng), { icon: greenIcon }).addTo(this.markerLayer);
        this.map.addLayer(this.markerLayer)
        this.map.setView([this.latlng.lat, this.latlng.lng], 6)
      },
      codeMatchBest(obj){
        var queryResult = obj.result
        if (queryResult && queryResult.length > 0) {
          //console.log('queryResult', queryResult)
          let item = {}
          for (var i = 0; i < queryResult.length; i++) {
            item = queryResult[i]
            const address = this.beautySub(item.address, 30)
            // const address = item.address
            this.searcList.push({
              address: address,
              x: item.location.x,
              y: item.location.y,
              score: item.score,
            })
          }
        } else {
          //htm += '<div class="res-row no-dd-box"><span>无匹配的坐标点</span></div>';
        }
      },
      beautySub(str, len){
        //匹配中文字符
        var reg = /[\u4e00-\u9fa5]/g
        var slice = str.substring(0, len)
        var chineseCharNum = ~~(slice.match(reg) && slice.match(reg).length)
        var realen = slice.length * 2 - chineseCharNum
        return str.substr(0, realen) + (realen < str.length ? '…' : '')
      },
    }
  }
</script>
<style scoped>
@import "@/style/dialog.css";
  #pointMap{
    height: 47vh;
    max-height: 500px;
    position: relative;
  }
  .text{
    position: absolute;
    top: 100px;
    left: 30px;
    z-index: 419;
    color: #fff;
    padding: 10px;
    font-size: 18px;
  }
  .selectOption{
    position: absolute;
    background: #686444 !important;
    color: #fff !important;
    height: 100px;
    font-size: 12px;
    overflow: auto;
    z-index: 111;
  }
  :deep(.el-input__inner){
    color:#fff !important;
  }
  .create-bottom {
    width: 100%;
    display: flex;
    justify-content: center;
    margin-bottom: 10px;
  }
</style>