<template>
  <div class="pageCon">
    <div style="position:absolute;color:#fff;z-index:1">默认显示区域配置:</div>
    <div style="width: 100%;height: 100%;">
      <el-form :model="queryInfo" label-width="auto">
        <el-row :gutter="20" justify="center">
          <el-col :span="15">
            <el-form-item label="单位名称">
              <el-tree-select
                v-model="queryInfo.bdjc"
                ref="unitTreeSelect"
                :data="unitDataList"
                node-key="label"
                :props="{label: 'label',value: 'label'}"
                check-strictly
                :render-after-expand="false"
                placeholder="请输入参战单位"
                show-checkbox
                check-on-click-node
                style="width: 100%;"
                @check="unitDataChange"
                clearable
              >
              </el-tree-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" justify="center">
          <el-col :span="5">
            <el-form-item label="省">
              <el-select v-model="queryInfo.sfDmnm" style="width: 100%;" filterable placeholder="请选择省" @change="sfChange" clearable>
                <el-option v-for="(t, i) in options" :key="i" :value="t.id" :label="t.text"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="市区">
              <el-select v-model="queryInfo.sqDmnm" style="width: 100%;" filterable placeholder="请选择市区" @change="sqChange" clearable>
                <el-option v-for="(t, i) in options2" :key="i" :value="t.id" :label="t.text"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="区县">
              <el-select v-model="queryInfo.qxDmnm" style="width: 100%;" filterable placeholder="请选择区县" @change="qxChange" clearable>
                <el-option v-for="(t, i) in options3" :key="i" :value="t.id" :label="t.text"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" justify="center">
          <el-col :span="5">
            <el-form-item label="地图级别">
              <el-input-number v-model="queryInfo.mapLevel" style="width: 100%;"  @change="mapLevelChange" :min="1" :max="18" clearable></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="经度">
              <el-input v-model="queryInfo.jd" @input="getJd" type="text" maxlength="20" show-word-limit clearable></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="纬度">
              <el-input v-model="queryInfo.wd" @input="getWd" type="text" maxlength="20" show-word-limit clearable></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" justify="center" style="width:100%">
          <el-col :span="15">
            <el-form-item label="查询地址">
              <el-input v-model="queryInfo.dmmc" @input="handleInput" clearable>
                <template #append>
                  <el-icon @click="handleInput"><Search /></el-icon>
                </template>
              </el-input>
              <div class="selectOption" v-show="searcList.length>0 && flag">
                <div v-for="(item,index) in searcList" :key="index" style="cursor:pointer;" @click="selectClick(item)">{{item.address}}</div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <el-row :gutter="20" justify="center">
        <el-col :span="20">
          <div id="main_map"></div>
        </el-col>
      </el-row>
      <el-row :gutter="20" justify="center" style="margin-top: 20px;">
        <el-button type="warning" @click="submit()">提 交</el-button>
      </el-row>
    </div>

  </div>
</template>

<script>
import Area from '@/utils/Area'
import config from "@/config"
import http from "@/utils/request"
let markerLayer = null;
export default {
  props:["current","item"],
  components: {
  },
  data() {
    return {
      map: null,
      searcList: [],
      flag:false,
      pageList: [],
      queryInfo: {
        "bdfh": "",
        "bdjc": "",
        "bdnm": "",
        "bdxh": "",
        "jd": "",
        "wd": "",
        "mapLevel": 8,
        "qxDmmc": "",
        "qxDmnm": "",
        "qxDmxh": "",
        "sfDmmc": "",
        "sfDmnm": "",
        "sfDmxh": "",
        "sqDmmc": "",
        "sqDmnm": "",
        "sqDmxh": "",
      },
      unitDataList: [],
      options: [], // 省份数据
      options2: [], // 市区数据
      options3: [], // 区县数据
      url: {
        list: "/dxyy/wjbdWrjYhglCs/list",
        add: "/dxyy/wjbdWrjYhglCs/add",
        edit: "/dxyy/wjbdWrjYhglCs/edit",
        delete: "/dxyy/wjbdWrjYhglCs/deleteBatch",
        query: "/dxyy/wjbdWrjYhglCs/queryById"
      },
    };
  },
  created() {
    
    // {"bdfh":"中国人民武装警察部队四川省总队","bdjc":"四川总队","bdnm":"912400000","bdxh":"009124","jd":102.6233,"mapLevel":5,"qxDmmc":"","qxDmnm":"","qxDmxh":"","sfDmmc":"四川省","sfDmnm":"510000","sfDmxh":"51","sqDmmc":"成都市","sqDmnm":"510100","sqDmxh":"5101","wd":29.8938}
  },
  mounted() {
    setTimeout(() => {
      this.initMap()
      this.initCityData()
      this.getResult()
    })
    
  },
  onUnmounted(){
    this.map = null;
  },
  // 改变时刷新
  // watch:{
  //   current(newVal){
  //     if (newVal) {
  //       // console.log(newVal,this.item);
  //       if (newVal == this.item.name) {
  //         this.reload();
  //       }
  //     } else {
  //       // 隐藏
  //     }
  //   }
  // },
  methods: {
    getJd(e){
      if(e < -180 || e > 180){
        this.$message.info("经度不在地图上")
      }
    },
    getWd(e){
      if(e < -90 || e > 90){
        this.$message.info("纬度不在地图上")
      }
    },
    getResult(){
      
      const params = {
        yhId: window.config.VUE_CAS_FLAG && this.$TOOL.data.get("USER_INFO")?this.$TOOL.data.get("USER_INFO").id:window.config.VUE_CAS_YHID,
        csBm: "sys_user_info"
      }
      http.get(`${config.API_URL}`+this.url.list,params).then(res=>{
        if(res.success && res.result.records.length){
          this.pageList = res.result.records;
          // 获取参数值数据
          if(this.pageList[0].csz) this.queryInfo = JSON.parse(this.pageList[0].csz);
          if(this.queryInfo.sfDmnm){
            this.options2 = this.options.filter(row=>{return row.id == this.queryInfo.sfDmnm})[0].children;
          }
          if(this.queryInfo.sqDmnm){
            this.options3 = this.options2.filter(row=>{return row.id == this.queryInfo.sqDmnm})[0].children;
          }
          if(this.queryInfo.jd && this.queryInfo.wd){
            this.$nextTick(()=>{
              this.map.setView([this.queryInfo.wd,this.queryInfo.jd], this.queryInfo.mapLevel);
            })
          }
        }else{
          console.log('xx');
          this.queryInfo.wd = 25.95706719;
          this.queryInfo.jd =117.93823242;
          this.map.setView([25.95706719,117.93823242], 8);
           
        }
      })
    },
    mapLevelChange (){
      this.map.setZoom(this.queryInfo.mapLevel);
    },
    initMap(){
      if(this.map) return

      // Map2D.addTiledMapLayer("/iserver/services/map-ugcv5-wj_gis_ZhongGuo_HangZhengJingJie/rest/maps/wj_gis_中国_行政境界");
      if(config.VUE_APP_SUPERMAP_BASE_FLAG){
           this.map = L.map("main_map", {
                center: [26.0753, 119.3062],
                crs: L.CRS.EPSG4326,
                maxZoom: 20,
                minZoom: 1,
                zoom: 8,
                zoomControl: false,
                logoControl: false,
                attributionControl: false
            });
            
            new L.supermap.tiledMapLayer(this.$CONFIG.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-multiTiles/rest/maps/wj_gis_聚合影像").addTo(this.map)
            new L.supermap.tiledMapLayer(this.$CONFIG.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services/map-ugcv5-wj_gis_ZhongGuo_HangZhengJingJie/rest/maps/wj_gis_%E4%B8%AD%E5%9B%BD_%E8%A1%8C%E6%94%BF%E5%A2%83%E7%95%8C").addTo(this.map)
        }else{
            this.map = L.map("main_map", {
                center: [25.95706719,117.93823242],
                // crs: L.CRS.EPSG4326,
                maxZoom: 25,
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

      // L.Tooltip.prototype._animateZoom = function(e){
      //       if(this._map){
      //           var pos=this._map._latLngToNewLayerPoint(this._latlng,e.zoom,e.center),
      //           anchor=this._getAnchor()
      //           L.DomUtil.setPosition(this._container,pos.add(anchor))
      //       }else{
      //           return
      //       }
      //   }
      //   L.Tooltip.prototype._updatePosition = function(e){
      //       var pos=this._map._latlngToNewLayerPoint(this._latlng,e.zoom,e.center)
      //       this._setPosition(pos)
      //   }
        
      //   L.Popup.prototype._animateZoom = function(e){
      //       if(this._map){
      //           var pos=this._map._latLngToNewLayerPoint(this._latlng,e.zoom,e.center),
      //           anchor=this._getAnchor()
      //           L.DomUtil.setPosition(this._container,pos.add(anchor))
      //       }else{
      //           return
      //       }
      //   }

      //   L.Circle.prototype._animateZoom = function(e){
      //       if(this._map){
      //           var pos=this._map._latLngToNewLayerPoint(this._latlng,e.zoom,e.center),
      //           anchor=this._getAnchor()
      //           L.DomUtil.setPosition(this._container,pos.add(anchor))
      //       }else{
      //           return
      //       }
      //   }
      //   L.Marker.prototype._animateZoom = function(e){
      //       if(this._map){
      //           var pos=this._map._latLngToNewLayerPoint(this._latlng,e.zoom,e.center)
      //       }else{
      //           return
      //       }
      //   }
      
      this.map.on("click",(e)=>{
        const x = e.latlng.lng
        const y = e.latlng.lat
        // console.log(y.toFixed(5)+','+x.toFixed(5));
        if (x < -180.0 || x > 180.0 || y < -90 || y > 90) {
          return
        }
        this.queryInfo.jd = x;
        this.queryInfo.wd = y;
        // console.log(e);
        if (markerLayer !== undefined && markerLayer !== '' && markerLayer !== null) {
          markerLayer.clearLayers()
        }
        markerLayer = L.layerGroup().addTo(this.map);
        var greenIcon = L.icon({
          iconUrl: '/static/map_img/destination.png',
          iconSize: [40, 40],
        });
        let showLocation = ''
        showLocation = '经度：' + x + '<br>' + '纬度：' + y
        L.marker(L.latLng(Number(e.latlng.lat), Number(e.latlng.lng)), { icon: greenIcon }).addTo(markerLayer).bindPopup(showLocation).openPopup();

      })
    },
    selectClick(e){
      this.queryInfo.jd = e.x;
      this.queryInfo.wd = e.y;
      this.queryInfo.dmmc = e.address;
      this.flag=false;
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
    codeMatchBest(obj){
      var queryResult = obj.result
      if (queryResult && queryResult.length > 0) {
        //console.log('queryResult', queryResult)
        let item = {}
        for (var i = 0; i < queryResult.length; i++) {
          item = queryResult[i]
          const address = this.beautySub(item.address, 30)
          // let address = item.address
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
    // 初始化城市数据
    initCityData(){
      var arr = (new Area()).all;
      this.options = arr.filter(row=>{return row.pid == '86'})
      for(var i of this.options){
        i.children = arr.filter(row=>{return row.pid == i.id})
        for(var j of i.children){
          j.children = arr.filter(row=>{return row.pid == j.id})
        }
      }
      // console.log(this.options);
      // 获取单位数据
      // this.$API.model.taskData.getLlbcUnit().then(res=>{
      //   if(res.success){
      //     this.unitDataList = JSON.parse(JSON.stringify(res.result).replaceAll("bdjc","label").replaceAll("bdnm","value"))
      //     // [
      //     //   {
      //     //     label: this.$TOOL.data.get("USER_INFO").orgCodeTxt,
      //     //     value: this.$TOOL.data.get("USER_INFO").departIds || this.$TOOL.data.get("USER_INFO").orgCode,
      //     //     children: JSON.parse(JSON.stringify(res.result).replaceAll("bdjc","label").replaceAll("bdnm","value"))
      //     //   }
      //     // ]
      //   }
      // })
      window.API.bigScreenDataFx.queryChildUnit({
        yhBdnm:window.TOOL.data.get("USER_INFO")?window.TOOL.data.get("USER_INFO").orgCode:''
      }).then(res=>{
          if(res.success){
            this.unitDataList = JSON.parse(JSON.stringify(res.result).replaceAll("bdjc","label").replaceAll("bdnm","value"))
              // this.unitDataList = [
              //   {
              //     label: window.TOOL.data.get("USER_INFO").orgCodeTxt,
              //     value: window.TOOL.data.get("USER_INFO").departIds || window.TOOL.data.get("USER_INFO").orgCode,
              //     children: JSON.parse(JSON.stringify(res.result).replaceAll("bdjc","label").replaceAll("bdnm","value"))
              //   }
              // ]
          }
        })
    },
    // 单位选择
    unitDataChange(e){
      console.log(this.queryInfo.bdjc);
      console.log(this.$refs.unitTreeSelect.getCheckedNodes());
      this.queryInfo.bdfh = e.bdfh;
      this.queryInfo.bdjc = e.label;
      this.queryInfo.bdnm = e.value;
      this.queryInfo.bdxh = e.bdxh;
    },
    // 省份切换
    sfChange(val){
      // 获取省份对应市区数据
      this.options2 = this.options.filter(row=>{return row.id == val})[0].children;
      // 获取省份对应省份名称
      this.queryInfo.sfDmmc = this.options.filter(row=>{return row.id == val})[0].text;
      // 获取省份对应内码
      this.queryInfo.sfDmnm = this.options.filter(row=>{return row.id == val})[0].id;
      // 获取省份对应区号
      this.queryInfo.sfDmxh = this.options.filter(row=>{return row.id == val})[0].id.substring(0,2);
      // 重置数据
      this.queryInfo.sqDmmc = "";
      this.queryInfo.sqDmnm = "";
      this.queryInfo.sqDmxh = "";
      this.queryInfo.qxDmmc = "";
      this.queryInfo.qxDmnm = "";
      this.queryInfo.qxDmxh = "";
    },
    // 市区切换
    sqChange(val){
      // 获取市区对应区县数据
      this.options3 = this.options2.filter(row=>{return row.id == val})[0].children;
      // 获取市区对应省份名称
      this.queryInfo.sqDmmc = this.options2.filter(row=>{return row.id == val})[0].text;
      // 获取市区对应内码
      this.queryInfo.sqDmnm = this.options2.filter(row=>{return row.id == val})[0].id;
      // 获取市区对应区号
      this.queryInfo.sqDmxh = this.options2.filter(row=>{return row.id == val})[0].id.substring(0,4);
      // 重置数据
      this.queryInfo.qxDmmc = "";
      this.queryInfo.qxDmnm = "";
      this.queryInfo.qxDmxh = "";
    },
    // 区县切换
    qxChange(val){
      // 获取区县对应省份名称
      this.queryInfo.qxDmmc = this.options3.filter(row=>{return row.id == val})[0].text;
      // 获取区县对应内码
      this.queryInfo.qxDmnm = this.options3.filter(row=>{return row.id == val})[0].id;
      // 获取区县对应区号
      this.queryInfo.qxDmxh = this.options3.filter(row=>{return row.id == val})[0].id;
    },
    submit(){
      console.log(this.queryInfo);
      const params = {
        bdnm: this.queryInfo.bdnm,
        yhId:  window.config.VUE_CAS_FLAG && this.$TOOL.data.get("USER_INFO")?this.$TOOL.data.get("USER_INFO").id:window.config.VUE_CAS_YHID,
        bz: "",
        csMc: "用户登录基本信息",
        csBm: "sys_user_info",
        csz: JSON.stringify(this.queryInfo)
      }
      // 存在用户参数数据时为编辑更新操作
      if(this.pageList.length){
        params.id = this.pageList[0].id;
        http.put(`${config.API_URL}`+this.url.edit,params).then(res=>{
          if(res.code == 200){
            this.$message.success("更新成功");
          }
        })
      }else{ // 否则为添加操作
        http.post(`${config.API_URL}`+this.url.add,params).then(res=>{
          if(res.code == 200){
            this.$message.success("添加成功");
            this.getResult()
          }
        })
      }
    },

  },
};
</script>

<style scoped lang="less">
.pageCon {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  // background: url(@/assets/firstPage/sixDesign/system.png) no-repeat;
  // background-size: 100% 100%;
  box-sizing: border-box;
  // background: #594a0e;
}
#main_map{
  width: 100%;
  height: 550px;
}
.selectOption{
  position: absolute;
  top: 32px;
  background: #686444 !important;
  color: #fff !important;
  height: 128px;
  font-size: 12px;
  overflow: auto;
  z-index: 999;
}
:deep(.el-select__wrapper){
  height:32px;
}
</style>
