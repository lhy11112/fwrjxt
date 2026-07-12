<template>
  <!-- <el-dialog
    :title="titleMap[mode]"
    v-model="visibleDialog"
    width="51%"
    style="height: 60%; margin-top: 10%"
    destroy-on-close
    close-on-click-modal
    @close="closed"
  > -->
  <AnalysisMoveDlg
    :title="titleMap[mode]"
    style="width: 45%;height:60vh"
    :visibleDialog="visibleDialog"
    @close="closed"
    :isModal="false"
  >
    <el-form
      :model="form"
      :rules="rules"
      :disabled="mode === 'show'"
      ref="dialogForm"
      label-width="140px"
      style="height: calc(100% - 40px);overflow: auto;"
    >
      <el-row>


         <el-col :span="12">
          <el-form-item label="推演计划名称" prop="mc">
            <el-input v-model="form.mc" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="推演计划日期" prop="rq">
            <el-date-picker v-model="form.rq" type="date" value-format="YYYY-MM-DD" format="YYYY-MM-DD" style="width:100%" :placeholder="'请输入'" clearable></el-date-picker>
          </el-form-item>
        </el-col>


        <el-col :span="12">
          <!-- <el-form-item label="无人机序列号" prop="serial">
            <el-select
              style="width: 100%;"
              filterable
              v-model="form.serial"
              placeholder="请选择"
              @change="wrjChange"
              clearable
            >
              <el-option v-for="(t,i) in wrjDataList" :key="i" :value="t.serialNumber" :label="t.serialNumber"></el-option>
            </el-select>
          </el-form-item> -->
          <el-form-item label="无人机:">
            <el-select v-model="form.wrj" placeholder="请选择" clearable filterable @change="wrjChange">
              <el-option v-for="(item,index) in wrjData" :key="index" :label="item.brand+item.serialNumber" :value="item.serialNumber"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        
        <!-- <el-col :span="12">
          <el-form-item label="推演时间:">
            <el-input-number style="width:42%" controls-position="right" v-model="form.minutes"></el-input-number>分
            <el-input-number style="width:42%;margin-left:3px" controls-position="right" v-model="form.seconds"></el-input-number>秒
          </el-form-item>
        </el-col> -->

        <!-- <el-col :span="12">
          <el-form-item label="机型" prop="model">
            <el-input v-model="form.model" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="品牌" prop="brand">
            <el-input v-model="form.brand" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col> -->

        <el-col :span="12">
          <el-form-item label="推演状态" prop="tyzt">
            <el-input v-model="form.tyzt" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>

        <el-col :span="24">
         <div class="flex_box" style="width:100%">
          <el-form-item label="起点:">
            <el-input v-model="form.uavDatectMsgDto1.start.longitude" style="width:22%" :placeholder="'请输入经度'" clearable></el-input>
            <el-input v-model="form.uavDatectMsgDto1.start.latitude" style="width:22%;margin-left:calc(4% / 4)" :placeholder="'请输入纬度'" clearable></el-input>
            <el-input v-model="form.uavDatectMsgDto1.start.altitude" style="width:19%;margin-left:calc(4% / 4)" :placeholder="'请输入高度(m)'" clearable></el-input>(m)
            <el-input v-model="form.uavDatectMsgDto1.start.stayTime" style="width:18%;margin-left:calc(4% / 4)" :placeholder="'停留时间(s)'" clearable></el-input>(s)
                    <el-icon title="地图选点" style="font-size: 24px;cursor: pointer;margin-left:10px" @click="route_input"><Location /></el-icon>
          </el-form-item>
        </div>
        <div class="flex_box" style="width:100%">
          <el-form-item label="终点:">
            <el-input v-model="form.uavDatectMsgDto1.end.longitude" style="width:22%" :placeholder="'请输入经度'" clearable></el-input>
            <el-input v-model="form.uavDatectMsgDto1.end.latitude" style="width:22%;margin-left:calc(4% / 4)" :placeholder="'请输入纬度'" clearable></el-input>
            <el-input v-model="form.uavDatectMsgDto1.end.altitude" style="width:19%;margin-left:calc(4% / 4)" :placeholder="'请输入高度(m)'" clearable></el-input>(m)
            <el-input v-model="form.uavDatectMsgDto1.end.stayTime" style="width:18%;margin-left:calc(4% / 4)" :placeholder="'停留时间(s)'" clearable></el-input>(s)
                    <el-icon title="地图选点" style="font-size: 24px;cursor: pointer;margin-left:10px" @click="route_input_end"><Location /></el-icon>
          </el-form-item>
        </div>

        <div class="flex_box tjd" style="width:100%;flex-wrap:wrap;">
          <div class="title" style="width:100%;padding-left: 20px;margin-bottom:10px;">
            途经点
             <el-icon style="font-size: 24px;margin-right: 10px;cursor: pointer;vertical-align: middle;" @click="addWayPoints"><CirclePlus /></el-icon>
          </div>
          <el-row v-for="(item, index) in form.uavDatectMsgDto1.waypoints" :key="index">
                  <el-col :span="5">
                    <el-form-item label="经度">
                    <el-input v-model="item.longitude" placeholder="请输入"></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="5">
                    <el-form-item label="纬度">
                      <el-input v-model="item.latitude" placeholder="请输入"></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="5" class="gd">
                    <el-form-item label="高度(m)">
                      <el-input v-model="item.altitude" placeholder="请输入"></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="5" class="tlsj">
                    <el-form-item label="停留时间(s)">
                      <el-input v-model="item.stayTime" placeholder="请输入"></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="4" style="text-align: center;line-height: 45px;">
                    <el-icon style="font-size: 24px;margin-right: 10px;cursor: pointer;" @click="addWayPoints"><CirclePlus /></el-icon>
                    <el-icon style="font-size: 24px;cursor: pointer;" @click="delWayPoints(index)"><Remove /></el-icon>
                    <el-icon title="地图选点" style="font-size: 24px;cursor: pointer;margin-left:10px" @click="route_input_tjd(index)"><Location /></el-icon>
                  </el-col>
              </el-row>
        </div>
        </el-col>

        <el-col :span="12" style="padding-left:5px">
        <el-form-item label="目标分析范围(km):">
          <el-input v-model="form.mbfxfw" style="width:100%" :placeholder="'请输入'" clearable></el-input>
        </el-form-item>
      </el-col>

      <el-col :span="24">
        <div style="padding-left:17px" class="hdClass">
          <div class="title" style="width:100%;">航段</div>
          <el-row v-for="(item, index) in form.uavDatectMsgDto1.hdcs" :key="index">
            <el-col :span="11">
                    <el-form-item label="速度(m/s):">
                      <el-input v-model="item.sd" @input="sdInput(item)" :placeholder="'请输入速度(m/s)'" clearable></el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="11">
                    <el-form-item label="时间(s):">
                      <el-input v-model="item.sj" style="margin-left:calc(4% / 4)" :placeholder="'时间(s)'" clearable></el-input>
                    </el-form-item>
                  </el-col>
            <!-- <el-input v-model="trailParam.uavDatectMsgDto1.end.altitude" style="width:22%;margin-left:calc(4% / 4)" :placeholder="'请输入高度'" clearable></el-input> -->
          </el-row>
        </div>
      </el-col>
        
      </el-row>
    </el-form>

    <!-- <template #footer> -->
      <div class="create-bottom">
        <el-button @click="closed">取 消</el-button>
        <el-button :loading="isSaveing" type="primary" @click="submit()"
          >保 存</el-button
        >
      </div>
    <!-- </template> -->
  <!-- </el-dialog> -->
  </AnalysisMoveDlg>
</template>

<script>
let wrjLineMarkerLayer = null;
let points = [];
let polyline = null;
let searchRouteTjLayer = null;
let searchRouteMddLayer = null;
let searchRouteTjdLayer = {};
let marketMarker3 = {}
import { checkSpaceTss} from "@/utils/index.js";
import {ElMessage} from "element-plus";
import startmarker from "/public/static/startmarker.png"
import endmarker from "/public/static/endmarker.png"
import passmarker from "/public/static/passmarker.png"
export default {
  props:{
    toDp:{
      type:Boolean,
      default:false
    }
  },
  data() {
    return {
      visibleDialog: false,
      form: {
       uavDatectMsgDto1:{
        start:{},
        end:{},
        waypoints:[{

        }],
      }
      },
      rules: {
        mc: [{ required: true, message: "请输入" }],
        rq: [{ required: true, message: "请选择" }],
      },
      mode: "add",
      titleMap: {
        add: "新增",
        edit: "编辑",
        show: "查看",
      },
      rwData:[],
      wrjData: [],
      dotStatus:null,
      tjdNum:0
    };
  },
  created() {
    // 无人机数据
    window.API.wrj.list({
      pageNo: 1,
      pageSize: 10000
    }).then(res=>{
      if(res.code == 200){
        this.wrjData = res.result.records;
      }
    })
  },
  mounted(){
    wrjLineMarkerLayer = L.layerGroup([]);
    wrjLineMarkerLayer.addTo(window.Map2D.map);
    window.Map2D.map.off('click')
    window.Map2D.map.on('click', evt => {
      console.log(evt);
      this.markerToMap(evt)
    })
  },
  methods: {
     sdInput(data){
      if(data.lc && data.sd){
        data.sj = parseInt(Number(data.lc / data.sd));
      }
    },
    wrjChange(val){
      var row = this.wrjDataList.filter(row=>{return row.serialNumber == val});
      if(row.length){
        this.form.model = row[0].model
        this.form.brand = row[0].brand
      }
    },
    getRwData(){
      window.API.model.wjfkyw.ztLists({
        pageNo: 1,
        pageSize: 1000,
        levels: '',
        dxyy:1
      }).then(res=>{
        if(res.code==200){
          this.rwData = res.result.records;
        }
      })
    },
    open(item) {
      this.mode = item;
      this.visibleDialog = true;
      return this;
    },
    closed(){
      this.visibleDialog = false;
      this.wrjLineClearLayer()
      this.markerClear()
      window.Map2D.map.off('click')
      this.$emit('closed')
    },
    setData(data) {
      this.form = Object.assign(this.form, data);
      console.log(JSON.parse(this.form.jhcs));
      this.form.uavDatectMsgDto1 = JSON.parse(this.form.jhcs);
      //测试
      // this.form.uavDatectMsgDto1.hdcs = []
      // for(let i =0;i<=this.form.uavDatectMsgDto1.waypoints.length;i++){
      //   this.form.uavDatectMsgDto1.hdcs.push({
      //     sd:"",
      //     sj:"",
      //     lc:"",
      //   })
      // }
      this.drawPath()
      if(this.form.uavDatectMsgDto1 && this.form.uavDatectMsgDto1.start && this.form.uavDatectMsgDto1.start.latitude && this.form.uavDatectMsgDto1.start.longitude){
        window.Map2D.map.flyTo([Number(this.form.uavDatectMsgDto1.start.latitude),Number(this.form.uavDatectMsgDto1.start.longitude)],12)
      }
      this.form.wrj = this.form.brand+this.form.serial;
      // if(this.form.jhks && this.form.jhjs){
      //   const { minutes, seconds } = this.millisecondsToMinutesAndSeconds(new Date(this.form.jhjs).getTime() - new Date(this.form.jhks).getTime());
      //   this.form.minutes = minutes;
      //   this.form.seconds = seconds;
      // }
      
    },
     millisecondsToMinutesAndSeconds(totalMilliseconds) {
        const minutes = Math.floor(totalMilliseconds / 60000); // 60000 毫秒 = 1 分钟
        const seconds = Math.floor((totalMilliseconds % 60000) / 1000); // 1000 毫秒 = 1 秒

        return {
            minutes,
            seconds
        };
    },
    // 绘制路线
    drawPath(lineFlag) {
      this.wrjLineClearLayer()
      this.markerClear()
      points=[]
      points.push(this.form.uavDatectMsgDto1.start)
      points.push(...this.form.uavDatectMsgDto1.waypoints)
      points.push(this.form.uavDatectMsgDto1.end)
      points = points.filter(v=>v.latitude && v.longitude)
      if (points.length > 1) {
        polyline = L.polyline(points.map(p => [p.latitude, p.longitude])).addTo(wrjLineMarkerLayer);

        // 计算每段距离并添加标注
        for (let i = 0; i < points.length - 1; i++) {
            const p1 = points[i];
            const p2 = points[i + 1];
            const coords = [[p1.longitude, p1.latitude], [p2.longitude, p2.latitude]];
            const distance = turf.distance(coords[0], coords[1]) * 1000;

            this.form.uavDatectMsgDto1.hdcs[i].lc = distance;
              if(this.form.uavDatectMsgDto1.hdcs[i].sd){
                this.form.uavDatectMsgDto1.hdcs[i].sj = parseInt(Number(this.form.uavDatectMsgDto1.hdcs[i].lc / this.form.uavDatectMsgDto1.hdcs[i].sd))
              }
            
            // 计算中点
            const midpoint = {};
            // 计算中间点坐标
            midpoint.lat = Number(p1.latitude) + (Number(p2.latitude) - Number(p1.latitude)) / 2,
            midpoint.lng = Number(p1.longitude) + (Number(p2.longitude) - Number(p1.longitude)) / 2;
            console.log(midpoint);
            // 添加标注
            const  markerIcon = L.divIcon({
                html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>${distance} 米</div>`,//marker标注
                className: 'my-div-icon',
                iconAnchor: [80, 20]//文字标注相对位置
              });
          window.L.marker(
            window.L.latLng(Number(midpoint.lat), Number(midpoint.lng)),
            {
              icon: markerIcon,
            }
          ).addTo(wrjLineMarkerLayer);
        }
      }
      this.addPoint()
      
    },
    // 添加点
    addPoint() {
      if(searchRouteTjLayer && searchRouteTjLayer!=null){
        // searchRouteTjLayer.clearLayers()
        window.Map2D.map.removeLayer(searchRouteTjLayer)
      }
      // 图标
      const searchRoute = {
        routeTj: window.L.icon({ iconSize: [32, 48], iconUrl: startmarker }),
      }
      searchRouteTjLayer = window.L.layerGroup([]).addTo(window.Map2D.map)

      const marketMarker = window.L.marker([this.form.uavDatectMsgDto1.start.latitude, this.form.uavDatectMsgDto1.start.longitude], { icon: searchRoute.routeTj })
      marketMarker.addTo(searchRouteTjLayer)
                
      if(searchRouteMddLayer && searchRouteMddLayer!=null){
        // searchRouteMddLayer.clearLayers()
        window.Map2D.map.removeLayer(searchRouteMddLayer)
      }
      // 图标
      const searchRoute2 = {
        routeTj: window.L.icon({ iconSize: [32, 48], iconUrl: endmarker }),
      }
      searchRouteMddLayer = window.L.layerGroup([]).addTo(window.Map2D.map)

      const marketMarker2 = window.L.marker([this.form.uavDatectMsgDto1.end.latitude, this.form.uavDatectMsgDto1.end.longitude], { icon: searchRoute2.routeTj })
      marketMarker2.addTo(searchRouteMddLayer)

      
      this.form.uavDatectMsgDto1.waypoints.forEach((item,index)=>{
        if(searchRouteTjdLayer['tjdLayer'+index] && searchRouteTjdLayer['tjdLayer'+index]!=null){
          // searchRouteTjdLayer['tjdLayer'+this.tjdNum].clearLayers()
          window.Map2D.map.removeLayer(searchRouteTjdLayer['tjdLayer'+index])
        }
        // 图标
        const searchRoute3 = {
          routeTj: window.L.icon({ iconSize: [32, 48], iconUrl: passmarker,data:index}),
        }
        searchRouteTjdLayer['tjdLayer'+index] = window.L.layerGroup([]).addTo(window.Map2D.map)
      
        
        
        marketMarker3['marker'+index]= window.L.marker([this.form.uavDatectMsgDto1.waypoints[index].latitude,this.form.uavDatectMsgDto1.waypoints[index].longitude], { icon: searchRoute3.routeTj })
        marketMarker3['marker'+index].addTo(searchRouteTjdLayer['tjdLayer'+index])
      })
      
              
    },
    wrjLineClearLayer(){
      if (
          wrjLineMarkerLayer != undefined &&
          wrjLineMarkerLayer != null &&
          wrjLineMarkerLayer != ""
      ) {
        // 清空图层
        wrjLineMarkerLayer.clearLayers();
      }
    },
    
//添加途经点
 addWayPoints() {
      this.form.uavDatectMsgDto1.waypoints.push({
        longitude: '',
        latitude: '',
        altitude: '',
        stayTime: ''
      })
      this.form.uavDatectMsgDto1.hdcs = [];
      for(let i =0;i<=this.form.uavDatectMsgDto1.waypoints.length;i++){
        this.form.uavDatectMsgDto1.hdcs.push({
          sd:"",
          sj:"",
          lc:"",
        })
      }
    },
    //删除途经点
 delWayPoints(index){
  
    // if(searchRouteTjdLayer['tjdLayer'+index] && searchRouteTjdLayer['tjdLayer'+index]!=null){
    //   window.Map2D.map.removeLayer(searchRouteTjdLayer['tjdLayer'+index])
    // }
     this.form.uavDatectMsgDto1.waypoints.splice(index,1)
     this.tjdNum =  index -1<0 ?0 : index -1;

    this.form.uavDatectMsgDto1.hdcs = [];
     for(let i =0;i<=this.form.uavDatectMsgDto1.waypoints.length;i++){
        this.form.uavDatectMsgDto1.hdcs.push({
          sd:"",
          sj:"",
          lc:"",
        })
      }

     this.drawPath()
    },

    

//地图选点
 markerToMap(evt){
  if(this.dotStatus == "始发地"){
      // if(searchRouteTjLayer && searchRouteTjLayer!=null){
      //   // searchRouteTjLayer.clearLayers()
      //   window.Map2D.map.removeLayer(searchRouteTjLayer)
      // }
      // // 图标
      // const searchRoute = {
      //   routeTj: window.L.icon({ iconSize: [32, 48], iconUrl: startmarker }),
      // }
      // searchRouteTjLayer = window.L.layerGroup([]).addTo(window.Map2D.map)
      const x = evt.latlng.lng
      const y = evt.latlng.lat
      this.form.uavDatectMsgDto1.start.longitude = x.toFixed(8);
      this.form.uavDatectMsgDto1.start.latitude = y.toFixed(8);

      // const marketMarker = window.L.marker([y, x], { icon: searchRoute.routeTj })
      // marketMarker.addTo(searchRouteTjLayer)

    }else if(this.dotStatus == "目的地"){
      // if(searchRouteMddLayer && searchRouteMddLayer!=null){
      //   // searchRouteMddLayer.clearLayers()
      //   window.Map2D.map.removeLayer(searchRouteMddLayer)
      // }
      // // 图标
      // const searchRoute = {
      //   routeTj: window.L.icon({ iconSize: [32, 48], iconUrl: endmarker }),
      // }
      // searchRouteMddLayer = window.L.layerGroup([]).addTo(window.Map2D.map)
      const x = evt.latlng.lng
      const y = evt.latlng.lat
      this.form.uavDatectMsgDto1.end.longitude = x;
      this.form.uavDatectMsgDto1.end.latitude = y;

      // const marketMarker = window.L.marker([y, x], { icon: searchRoute.routeTj })
      // marketMarker.addTo(searchRouteMddLayer)
    }else if(this.dotStatus == "途经点"){
      // if(searchRouteTjdLayer['tjdLayer'+this.tjdNum] && searchRouteTjdLayer['tjdLayer'+this.tjdNum]!=null){
      //   // searchRouteTjdLayer['tjdLayer'+this.tjdNum].clearLayers()
      //   window.Map2D.map.removeLayer(searchRouteTjdLayer['tjdLayer'+this.tjdNum])
      // }
      // // 图标
      // const searchRoute = {
      //   routeTj: window.L.icon({ iconSize: [32, 48], iconUrl: passmarker,data:this.tjdNum }),
      // }
      // searchRouteTjdLayer['tjdLayer'+this.tjdNum] = window.L.layerGroup([]).addTo(window.Map2D.map)
      const x = evt.latlng.lng
      const y = evt.latlng.lat
      this.form.uavDatectMsgDto1.waypoints[this.tjdNum].longitude = x;
      this.form.uavDatectMsgDto1.waypoints[this.tjdNum].latitude = y;
      // const marketMarker = {}
      // marketMarker['marker'+this.tjdNum]= window.L.marker([y, x], { icon: searchRoute.routeTj })
      // marketMarker['marker'+this.tjdNum].addTo(searchRouteTjdLayer['tjdLayer'+this.tjdNum])
    }

    this.drawPath()
},
 markerClear(){
    if(searchRouteTjLayer && searchRouteTjLayer!=null){
        // searchRouteTjLayer.clearLayers()
        window.Map2D.map.removeLayer(searchRouteTjLayer)
      }
    if(searchRouteMddLayer && searchRouteMddLayer!=null){
        // searchRouteMddLayer.clearLayers()
        window.Map2D.map.removeLayer(searchRouteMddLayer)
      }
      // 清理途经点标记
    for (const key in searchRouteTjdLayer) {
      if (searchRouteTjdLayer[key] != null) {
        window.Map2D.map.removeLayer(searchRouteTjdLayer[key])
      }
    }
      
},
 route_input_tjd (i){
  this.tjdNum=i;
  this.dotStatus = "途经点"
  console.log(this.dotStatus);
},
 route_input (){
  this.dotStatus = "始发地"
},

 route_input_end () {
  this.dotStatus = "目的地"
},
    submit() {
      this.$refs.dialogForm.validate(async (valid) => {
        if (valid) {
          let data={}
          data=Object.assign(data,this.form)
          // data.jhcs = JSON.stringify(data.uavDatectMsgDto1)
          // data.jhjs = window.TOOL.dateFormat(new Date(new Date(data.jhks).getTime() + data.minutes * 60 * 1000 + data.seconds *1000),"yyyy-MM-dd hh:mm:ss");
          if(data.uavDatectMsgDto1.hdcs && data.uavDatectMsgDto1.hdcs.length){
            data.uavDatectMsgDto1.segmentSpeeds =data.uavDatectMsgDto1.hdcs.map(v=>v.sd);
          }
          
          window.TOOL.data.set("startTyParams",{mbFw:this.form.uavDatectMsgDto1.mbFw})
          // data.mrcs=JSON.stringify(data.mrcs)
          if (this.mode == "add") {
            window.API.wrjtyjh.add(data).then((res) => {
              if (res.code == 200) {
                this.visibleDialog = false;
                this.$message.success("操作成功")
                this.$emit("successClick");
              }
            });
            
          } else if (this.mode == "edit") {

            window.API.wrjtyjh.edit(data).then((res) => {
              if (res.code == 200) {
                this.visibleDialog = false;
                 this.wrjLineClearLayer()
                  this.markerClear()
                  window.Map2D.map.off('click')
                this.$message.success("操作成功")
                this.$emit("successClick");
              }
            });
          }
        }
      });
    },
  },
};
</script>

<style scoped lang="less">
*{
  box-sizing: border-box;
}
// :deep(.el-form-item__label) {
//   color: #000 !important;
// }
.upload-demo {
  width: 100% !important;
}
.el-select-dropdown__item.is-selected{
  color:var(--el-color-primary) !important;
}
.create-bottom {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
}
:deep(.el-select){
  height:32px !important;
}
:deep(.el-select__wrapper){
  height:32px !important;
}
:deep(.tjd .el-form-item__label){
  width:60px !important;
}
:deep(.tjd .gd .el-form-item__label){
  width:80px !important;
}
:deep(.tjd .tlsj .el-form-item__label){
  width:100px !important;
}

:deep(.hdClass .el-form-item__label){
  width:80px !important;
}
</style>