<template>
  <el-dialog
    class="ct_dialog ct_footer_dialog" 
    title="选择坐标点"
    :width="'70%'"
    destroy-on-close
    v-model="visible"
    :close-on-click-modal="false"
    @closed="handleCancel">
      <div id="pointMap" v-loading="isLoading">
        
      </div>
      <div class="text">
        <!-- <el-input v-model="cityNameVal" style="width: 200px;" @keyup.enter="addressFit(cityNameVal)"></el-input> -->
        <el-input v-if="searchFlag" v-model="cityNameVal" style="width: 200px;" @keyup.enter="handleInput" @input="handleInput"></el-input>
        <div class="selectOption" v-show="searcList.length>0 && flag">
          <div v-for="(item,index) in searcList" :key="index" style="cursor:pointer;" @click="selectClick(item)">{{item.address}}</div>
        </div>
        
        <br>
        纬度: {{ToDegrees(latlng.lat)}}
        <br>
        经度: {{ToDegrees(latlng.lng)}}
      </div>
    <template #footer>
      <el-button style="background: transparent;color: #fff;" @click="handleCancel">取 消</el-button>
      <el-button :loading="isSaveing" style="background: #CF8A00;color: #fff;" @click="handleSubmit">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script>
  import axios from 'axios'
  import mapCenterPoint from "./mapCenterPoint.json"
  import icon from "@/assets/images/marker-icon.png";
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
        searchFlag:false
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
    mounted(){
      this.searchFlag = window.config.VUE_APP_SUPERMAP_BASE_FLAG
    },
    methods:{
      initMap(){
        // if(this.map) return
        // this.map = Map2D.init("pointMap");
        if(window.config.VUE_APP_SUPERMAP_BASE_FLAG){
          this.map = L.map("pointMap", {
              crs: L.CRS.EPSG4326,
              center: [26.0753, 119.3062],
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
        }else{
            this.map = L.map("pointMap", {
                center: [26.0753, 119.3062],
                // crs: L.CRS.EPSG4326,
                maxZoom: 20,
                minZoom: 1,
                zoom: 8,
                zoomControl: false,
                logoControl: false,
                attributionControl: false
            });
            // 添加 TMS 瓦片图层
            L.tileLayer(window.config.VUE_APP_API_FRONT_BASE_URL+window.config.VUE_APP_MAP_WP, {
                tms: true,               // 启用 TMS 模式（自动翻转 Y）
                // attribution: '© 福建省瓦片数据',
                maxZoom: 25,
                minZoom: 1
            }).addTo(this.map);
        }
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
                  this.map.setView(this.pointValue.split(","), 8)
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
    height: 60vh;
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
</style>