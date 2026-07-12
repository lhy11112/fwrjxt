<template>
  <div id="topCenter">
    <div class="historyDataBox">
      <div class="tableBox">
              <el-table
                :data="tableData"
                style="width: 100%; height: 100%"
                class="custom-table"
                @selectionChange="selectionChange"
                @row-click="rowClickChange"
                v-loading="loading"
              >
                <el-table-column type="selection" width="50"></el-table-column>
                <el-table-column
                  v-for="(item, index) in columnData"
                  :prop="item.prop"
                  :label="item.label"
                  :key="index"
                  :width="item.width"
                >
                  <template v-if="item.prop == 'lx'" #default="scope">
                    {{scope.row.lx=="restricted"?'限制区':scope.row.lx=="prohibited"?'禁飞区':scope.row.lx=="danger"?'预警区':'允许飞行区域'}}
                  </template>
                  <template v-else-if="item.prop == 'xz'" #default="scope">
                    {{scope.row.xz=="circle"?'圆形':scope.row.xz=="rectangle"?'矩形':scope.row.xz=="polygon"?'多边形':scope.row.xz}}
                  </template>
                </el-table-column>
                <!-- <el-table-column fixed="right" label="操作" width="100">
                  <template #default="scope">
                    <el-button
                      style="color: #fff"
                      link
                      type="primary"
                      size="small"
                      @click.prevent="lookKy(scope.row)"
                    >
                      查看
                    </el-button>
                    <el-button
                      style="color: #fff"
                      link
                      type="primary"
                      size="small"
                      @click.prevent="clearAllKy()"
                    >
                      清除
                    </el-button>
                  </template>
                </el-table-column> -->
              </el-table>
            </div>
            <div>
              <el-pagination
                class="pagination"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="pageOption.pageNo"
                :page-size="pageOption.pageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                :page-sizes="[8, 10, 20, 50,100]"
              >
              </el-pagination>
            </div>
    </div>
    
  </div>

  
</template>

<script>

let clusterMarkersRadius = null;
import { ElMessageBox,ElMessage } from "element-plus"
import { useKyStore } from "@/store/modules/ky";
const kyStore = useKyStore();
export default {
  name: 'CesiumAirspaceSystem',
  data() {
    return {
      viewer: null,
      entities: [],
      selectedEntity: null,
      drawingMode: false,
      activeShapeType: 'circle',
      imageryLayers: null,
      currentImageryIndex: 0,
      drones: [],
      droneEntities: [],
      queryRadius: 1000,
      droneStatusFilter: 'all',
      droneResults: [],
      loading: true,
      // 用于临时存储编辑前的实体状态
      tempEntity: null,
      viewer: null,
      polygonArr:[],
      visibleDialog:false,
      wrjRadio:1,
      form:{},
      
      columnData:[
         { prop: "mc", label: "空域名称"},
        { prop: "lx", label: "空域类型" },
        { prop: "xz", label: "区域形状" },
        { prop: "zxdjd", label: "中心点经度" },
        { prop: "zxdwd", label: "中心点纬度" },
        { prop: "bj", label: "半径" },
        { prop: "zxgd", label: "最小高度" },
        { prop: "zdgd", label: "最大高度" },
        { prop: "ys", label: "颜色" },
      ],
      tableData:[],
      queryInfo:{},
      pageOption:{
        pageNo:1,
        pageSize:10
      }
    }
  },
  mounted() {
    
    // const centerArr = window.config.centerArr;
    // const kmArr = window.config.kmArr;
    // const colorArr= window.config.colorArr;
    // const textArr= window.config.textArr;
    // centerArr.forEach((item,index)=>{
    //   const center = item
    //   const radius = kmArr[index] * 1000; //圆的半径
    //   const bound = this.getCriclePoints(center, radius);
    //   window.L.polygon(bound, { color: colorArr[index]}).addTo(clusterMarkersRadius);
    //   // 创建带有文字的标记
    //   const marker = window.L.marker(center, {
    //       icon: window.L.divIcon({
    //           html: `<div style="width: 80px;transform: translateX(-28%);">${textArr[index]}</div>`,
    //           iconSize: [20, 20],
    //           iconAnchor: [10, 25], // 设置图标在标记点的显示位置
    //       }),
    //   }).addTo(clusterMarkersRadius);
    // })

    
    // this.initDroneQuery();
    this.$nextTick(()=>{
        let that = this;
        clusterMarkersRadius = window.L.layerGroup([]);
        clusterMarkersRadius.addTo(window.Map2D.map);
        that.initEventListeners();
        window.eventBus.on('kyChange',function(e){
          console.log(e);
          if(e){
            that.getKy();
          }else{
            that.clearAllKy()
          }
        })
        this.getData()
    })
    
     
    // window.eventBus.on("gj",(msg_txt)=>{
    //   console.log('111');
    //   console.log(msg_txt);
      
    //   this.animateCircle(msg_txt)
    //   this.createAndPlayAudio('/static/audio.wav');
    // })
    
    // let entities =[{"id":1761614303513,"type":"circle","name":"限制区","lon":119.53209209651278,"lat":26.66584286023294,"radius":500,"minHeight":0,"maxHeight":1000,"shapeType":"restricted","color":"#ff0000","showLabel":true},{"id":1761614311384,"type":"circle","name":"禁飞区","lon":119.55648779774516,"lat":26.6741477145405,"radius":500,"minHeight":0,"maxHeight":1000,"shapeType":"restricted","color":"#ff0000","showLabel":true},{"id":1761614330151,"type":"circle","name":"预警区","lon":119.5510958486831,"lat":26.640635376977954,"radius":500,"minHeight":0,"maxHeight":1000,"shapeType":"restricted","color":"#ff0000","showLabel":true},{"id":1761614330231,"type":"circle","name":"允许飞行区域","lon":119.570585,"lat":26.659916,"radius":500,"minHeight":0,"maxHeight":1000,"shapeType":"restricted","color":"#ff0000","showLabel":true}];
    
  },
  beforeDestroy() {
    console.log('beforeDestroy');
     this.clearKy()
     window.eventBus.off('kyChange')
     window.eventBus.off('gj')
  },
  onUnmounted() {
    console.log('onUnmounted');
     this.clearKy()
     window.eventBus.off('kyChange')
     window.eventBus.off('gj')
  },
  methods: {
    clearKy(){
      this.radiusClearLayer()
      this.polygonArr=[]

      
      this.entities.forEach(entity => {
        window.Map3D.viewer.entities.remove(entity);
      });
      
      // this.entities =[];
      
    },
    selectionChange(e){
      console.log(e);
      this.selection = e;
      this.lookKy()
    },
    lookKy(){
      this.radiusClearLayer()
      this.polygonArr=[]

      this.entities =[];
      let kyData = [];
      let data=[];
      // data.push(row);
      data = this.selection;
        if(data && data.length){
          data.forEach(item=>{
            for(let i=0;i<3;i++){
              if(i==0){
                let obj={}
                Object.assign(obj,item);
                obj.mc="禁飞区";
                obj.lx="prohibited";
                obj.bj=3000;
                obj.ys="#ff0000";
                kyData.push(obj)
              }else if(i==1){
                let obj={}
                Object.assign(obj,item);
                // obj.sjmc = item.mc;
                obj.mc="预警区";
                obj.lx="prohibited";
                obj.bj=5000;
                obj.ys="#ffff00";
                kyData.push(obj)
              }else if(i==2){
                kyData.push(item)
              }
            }
          })
        }
        kyData.forEach((item,index)=>{
          if(index==0){
            window.Map2D.map.flyTo([item.zxdwd,item.zxdjd],12)
          }
          const center = [item.zxdwd,item.zxdjd]
          const radius = item.bj; //圆的半径
          const bound = this.getCriclePoints(center, radius);
          
          const polygon = window.L.polygon(bound, { color: item.ys}).addTo(clusterMarkersRadius);
          if(index%3==1){
            this.polygonArr.push({sjmc:item.id,polygon:polygon})
          }
          
          
          // 创建带有文字的标记
          const marker = window.L.marker(bound[0], {
              icon: window.L.divIcon({
                  html: `<div>${item.mc}</div>`,
                  iconSize: [20, 20],
                  iconAnchor: [20, 25], // 设置图标在标记点的显示位置
              }),
          }).addTo(clusterMarkersRadius);
        })

        kyData.forEach((item,index)=>{
          let obj={};
          obj.type=item.xz;
          obj.name=item.mc;
          obj.lon=item.zxdjd;
          obj.lat=item.zxdwd;
          obj.radius=item.bj;
          obj.minHeight=item.zxgd;
          obj.maxHeight=item.zdgd;
          obj.shapeType=item.lx;
          obj.color=item.ys;
          obj.id = item.id;
          // obj.pixelOffset=item.bj;
          if(index%3==2){
            obj.showLabel=true;
          }else{
            obj.showLabel=false;
          }
          this.entities.push(obj);
        })
        console.log(this.entities);
        // 创建Cesium实体并添加到地图
        this.entities.forEach(newEntity=>{
          this.addEntityToMap(newEntity);
        })
    },
    getData(){
      this.loading=true;
      const params = JSON.parse(JSON.stringify(this.pageOption)); // Object.assign(this.queryInfo,this.pageOption)
      var arr = [];
      for(var key in this.queryInfo){
        // if(this.queryInfo[key] && !(/^[\u4e00-\u9fa5a-zA-Z0-9]{1,100}$/.test(this.queryInfo[key]))){
        //   ElMessage.warning("查询内容不能包含特殊字符");
        //   return;
        // }
        if(this.queryInfo[key]){
          arr.push({
            "rule": "like",
            "type": "input",
            "val": this.queryInfo[key],
            "field": key
          })
        }
      }
      if(arr.length){
        params.superQueryParams = JSON.stringify(arr)
        params.superQueryMatchType = 'and'
      }
      window.API.wrjky.list(params).then(res=>{
        if(res.code == 200){
          this.tableData = res.result.records;
          this.total = res.result.total;
        }
        this.loading=false;
      })
    },
    //空域
    getKy(){
      this.radiusClearLayer()
      this.polygonArr=[]
      if(this.entities && this.entities.length){
        this.entities.forEach(entity => {
          window.Map3D.viewer.entities.remove(entity);
        });
      }
      
      this.entities =[];
      window.API.wrjky.list({
        pageNo:1,
        pageSize:1000
      }).then(res=>{
        let data = res.result.records;
        let kyData = [];
        if(data && data.length){
          data.forEach(item=>{
            for(let i=0;i<3;i++){
              if(i==0){
                let obj={}
                Object.assign(obj,item);
                obj.mc="禁飞区";
                obj.lx="prohibited";
                obj.bj=3000;
                obj.ys="#ff0000";
                kyData.push(obj)
              }else if(i==1){
                let obj={}
                Object.assign(obj,item);
                // obj.sjmc = item.mc;
                obj.mc="预警区";
                obj.lx="prohibited";
                obj.bj=5000;
                obj.ys="#ffff00";
                kyData.push(obj)
              }else if(i==2){
                kyData.push(item)
              }
            }
          })
        }
        kyData.forEach((item,index)=>{
          // if(index==0){
          //   window.Map2D.map.flyTo([item.zxdwd,item.zxdjd])
          // }
          const center = [item.zxdwd,item.zxdjd]
          const radius = item.bj; //圆的半径
          const bound = this.getCriclePoints(center, radius);
          
          const polygon = window.L.polygon(bound, { color: item.ys}).addTo(clusterMarkersRadius);
          if(index%3==1){
            this.polygonArr.push({sjmc:item.id,polygon:polygon})
          }
          
          
          // 创建带有文字的标记
          const marker = window.L.marker(bound[0], {
              icon: window.L.divIcon({
                  html: `<div>${item.mc}${item.bj / 1000}Km</div>`,
                  iconSize: [20, 20],
                  iconAnchor: [20, 25], // 设置图标在标记点的显示位置
              }),
          }).addTo(clusterMarkersRadius);
        })

        kyData.forEach((item,index)=>{
          let obj={};
          obj.type=item.xz;
          obj.name=item.mc;
          obj.lon=item.zxdjd;
          obj.lat=item.zxdwd;
          obj.radius=item.bj;
          obj.minHeight=item.zxgd;
          obj.maxHeight=item.zdgd;
          obj.shapeType=item.lx;
          obj.color=item.ys;
          obj.id = item.id;
          // obj.pixelOffset=item.bj;
          if(index%3==2){
            obj.showLabel=true;
          }else{
            obj.showLabel=false;
          }
          this.entities.push(obj);
        })
        console.log(this.entities);
        // 创建Cesium实体并添加到地图
        this.entities.forEach(newEntity=>{
          this.addEntityToMap(newEntity);
        })
      })
    },
    createAndPlayAudio(audioFilePath) {
      // 创建一个新的audio元素
      var audio = document.createElement('audio');
      
      // 设置音频文件的路径
      audio.src = audioFilePath;
      
      // 设置音频元素是否自动播放
      audio.autoplay = true;
      
      // 设置音频元素是否循环播放
      audio.loop = false;
      
      // 将audio元素添加到body中，以便它可以在页面上显示
      document.body.appendChild(audio);
      // 播放音频
      audio.play();
    },
    animateCircle(msg_txt){
      // 设置初始透明度为完全不透明
        this.polygonArr.forEach(item=>{
          item.polygon.setStyle({ opacity:1 });
        })
        

        // 定义闪烁参数：间隔时间（ms）和闪烁次数
        const flashInterval = 500; // 每500毫秒闪烁一次
        const flashCount = 20; // 闪烁20次

        // 创建闪烁动画
        let flashIndex = 0;
        const flashIntervalId = setInterval(() => {
            // 在每次间隔中，改变透明度
            if (flashIndex < flashCount) {
              // console.log(this.polygon);
              this.polygonArr.forEach(item=>{
                item.polygon.setStyle({ opacity:flashIndex % 2 === 0 ? 1 : 0.5 });
              })
                flashIndex++;
            } else {
                // 停止间隔并恢复初始透明度
                clearInterval(flashIntervalId);
                this.polygonArr.forEach(item=>{
                  item.polygon.setStyle({ opacity:1 });
                })
            }
        }, flashInterval);
    },
    
    // 清除图层
    radiusClearLayer(){
      if (
          clusterMarkersRadius != undefined &&
          clusterMarkersRadius != null &&
          clusterMarkersRadius != ""
        ) {
          // 清空图层
          clusterMarkersRadius.clearLayers();
        }
    },
    // 获取地图上的范围圈
    getCriclePoints(center, radius){
      const bound = [];
      const earthRadius = 6378137; //地球的半径
      const dlat = (radius / earthRadius) * (180 / Math.PI);
      const dlng = dlat / Math.cos((center[0] * Math.PI) / 180);
      for (let i = 0; i < 360; i++) {
        const red = (i * Math.PI) / 180;
        const lat = center[0] + dlat * Math.sin(red);
        const lng = center[1] + dlng * Math.cos(red);
        bound.push([lat, lng]);
      }
      return bound;
    },
    initEventListeners() {
      // 地图点击事件 - 用于绘制形状
      const handler = new window.Cesium.ScreenSpaceEventHandler(window.viewer.scene.canvas);
      
      handler.setInputAction((click) => {
        if (this.drawingMode) {
          const cartesian = window.viewer.camera.pickEllipsoid(click.position, window.viewer.scene.globe.ellipsoid);
          if (cartesian) {
            const cartographic = window.Cesium.Cartographic.fromCartesian(cartesian);
            const lon = window.Cesium.Math.toDegrees(cartographic.longitude);
            const lat = window.Cesium.Math.toDegrees(cartographic.latitude);
            
            this.createNewEntity(lon, lat);
            this.drawingMode = false;
          }
        } else {
          // 处理实体选择
          const pickedObject = window.viewer.scene.pick(click.position);
          if (window.Cesium.defined(pickedObject) && pickedObject.id) {
            this.selectEntity(pickedObject.id);
          } else {
            this.selectedEntity = null;
          }
        }
      }, window.Cesium.ScreenSpaceEventType.LEFT_CLICK);
    },
    
    setActiveShape(type) {
      this.activeShapeType = type;
    },
    
    handleAddBtnClick() {
      this.drawingMode = true;
    },
    
    createNewEntity(lon, lat) {
      let newEntity;
      
      switch (this.activeShapeType) {
        case 'circle':
          newEntity = {
            id: Date.now(),
            type: 'circle',
            name: `圆形空域 ${this.entities.length + 1}`,
            lon,
            lat,
            radius: 500,
            minHeight: 0,
            maxHeight: 1000,
            shapeType: 'restricted',
            color: '#ff0000',
            showLabel: true,
            cesiumEntity: null
          };
          break;
          
        case 'rectangle':
          newEntity = {
            id: Date.now(),
            type: 'rectangle',
            name: `矩形空域 ${this.entities.length + 1}`,
            lon,
            lat,
            width: 1000,
            length: 1000,
            rotation: 0,
            minHeight: 0,
            maxHeight: 1000,
            shapeType: 'restricted',
            showLabel: true,
            cesiumEntity: null
          };
          break;
          
        case 'polygon':
          newEntity = {
            id: Date.now(),
            type: 'polygon',
            name: `多边形空域 ${this.entities.length + 1}`,
            vertices: [
              { lon, lat },
              { lon: lon + 0.01, lat },
              { lon: lon + 0.005, lat: lat + 0.01 }
            ],
            minHeight: 0,
            maxHeight: 1000,
            shapeType: 'restricted',
            color: '#00ff00',
            showLabel: true,
            cesiumEntity: null
          };
          break;
      }
      
      // 创建Cesium实体并添加到地图
      this.addEntityToMap(newEntity);
      
      // 添加到实体列表
      this.entities.push(newEntity);
      
      // 选中新创建的实体
      this.selectEntity(newEntity);
    },
    
    addEntityToMap(entity) {
      // 根据实体类型创建不同的Cesium实体
      let cesiumEntity;
      
      switch (entity.type) {
        case 'circle':
          cesiumEntity = window.viewer.entities.add({
            position: window.Cesium.Cartesian3.fromDegrees(entity.lon, entity.lat),
            name: entity.name,
            ellipse: {
              semiMinorAxis: entity.radius,
              semiMajorAxis: entity.radius,
              height: entity.minHeight,
              extrudedHeight: entity.maxHeight,
              material: window.Cesium.Color.fromCssColorString(entity.color).withAlpha(0.15),
              outline: false,
              outlineColor: window.Cesium.Color.BLACK
            },
            label: {
              text: entity.name,
              show: entity.showLabel,
              font: '14px sans-serif',
              pixelOffset: new window.Cesium.Cartesian2(0,-20)
            },
            properties: {
              id: entity.id,
              type: entity.type
            }
          });
          break;
          
        case 'rectangle':
          // 计算矩形的西南和东北坐标
          var rectangleCoords = this.calculateRectangleCoordinates(
            entity.lon, entity.lat, entity.width, entity.length, entity.rotation
          );
          
          cesiumEntity = window.viewer.entities.add({
            name: entity.name,
            rectangle: {
              coordinates: window.Cesium.Rectangle.fromDegrees(
                rectangleCoords.west, rectangleCoords.south,
                rectangleCoords.east, rectangleCoords.north
              ),
              height: entity.minHeight,
              extrudedHeight: entity.maxHeight,
              material: window.Cesium.Color.RED.withAlpha(0.15),
              outline: true,
              outlineColor: window.Cesium.Color.BLACK,
              rotation: window.Cesium.Math.toRadians(entity.rotation)
            },
            label: {
              text: entity.name,
              show: entity.showLabel,
              font: '14px sans-serif',
              pixelOffset: new window.Cesium.Cartesian2(0, -20),
              position: window.Cesium.Cartesian3.fromDegrees(entity.lon, entity.lat)
            },
            properties: {
              id: entity.id,
              type: entity.type
            }
          });
          break;
          
        case 'polygon':
          var positions = entity.vertices.map(v => 
            window.Cesium.Cartesian3.fromDegrees(v.lon, v.lat)
          );
          
          cesiumEntity = window.viewer.entities.add({
            name: entity.name,
            polygon: {
              hierarchy: new window.Cesium.PolygonHierarchy(positions),
              height: entity.minHeight,
              extrudedHeight: entity.maxHeight,
              material: window.Cesium.Color.fromCssColorString(entity.color).withAlpha(0.15),
              outline: true,
              outlineColor: window.Cesium.Color.BLACK
            },
            label: {
              text: entity.name,
              show: entity.showLabel,
              font: '14px sans-serif',
              pixelOffset: new window.Cesium.Cartesian2(0, -20)
            },
            properties: {
              id: entity.id,
              type: entity.type
            }
          });
          break;
      }
      
      entity.cesiumEntity = cesiumEntity;
    },
    
    calculateRectangleCoordinates(centerLon, centerLat, width, length, rotation) {
        console.log(rotation)
      // 简化的矩形坐标计算，实际项目中可能需要更精确的计算
      const widthDeg = width / 111319.9; // 米转度的近似值
      const lengthDeg = length / 111319.9;
      
      return {
        west: centerLon - widthDeg / 2,
        east: centerLon + widthDeg / 2,
        south: centerLat - lengthDeg / 2,
        north: centerLat + lengthDeg / 2
      };
    },
    
    selectEntity(entity) {
      // 如果是Cesium实体，找到对应的应用实体
      if (entity.properties) {
        const id = entity.properties.id.getValue();
        entity = this.entities.find(e => e.id === id);
      }
      
      this.selectedEntity = entity;
      // 保存当前状态用于取消编辑
      this.tempEntity = entity; //JSON.parse(JSON.stringify(entity));
    },
    
    applyChanges() {
      // 更新Cesium实体
      if (this.selectedEntity) {
        // 先移除旧实体
        if (this.selectedEntity.cesiumEntity) {
          window.viewer.entities.remove(this.selectedEntity.cesiumEntity);
        }
        
        // 添加更新后的实体
        this.addEntityToMap(this.selectedEntity);
      }
    },
    
    cancelEdit() {
      if (this.tempEntity && this.selectedEntity) {
        // 恢复到编辑前的状态
        const index = this.entities.findIndex(e => e.id === this.selectedEntity.id);
        if (index !== -1) {
          // 移除当前Cesium实体
          window.viewer.entities.remove(this.selectedEntity.cesiumEntity);
          
          // 恢复临时实体
          this.entities[index] = this.tempEntity;
          this.selectedEntity = this.tempEntity;
          
          // 重新添加到地图
          this.addEntityToMap(this.selectedEntity);
        }
      }
    },
    
    copyEntity() {
      if (this.selectedEntity) {
        const newEntity = JSON.parse(JSON.stringify(this.selectedEntity));
        newEntity.id = Date.now();
        newEntity.name = `${newEntity.name} (复制)`;
        newEntity.cesiumEntity = null;
        
        this.addEntityToMap(newEntity);
        this.entities.push(newEntity);
        this.selectEntity(newEntity);
      }
    },
    
    deleteEntity() {
      if (this.selectedEntity) {
        // 从地图移除
        if (this.selectedEntity.cesiumEntity) {
          window.viewer.entities.remove(this.selectedEntity.cesiumEntity);
        }
        
        // 从列表移除
        this.entities = this.entities.filter(e => e.id !== this.selectedEntity.id);
        this.selectedEntity = null;
      }
    },
    
    addVertex() {
      if (this.selectedEntity && this.selectedEntity.type === 'polygon') {
        // 在最后一个顶点附近添加新顶点
        const lastVertex = this.selectedEntity.vertices[this.selectedEntity.vertices.length - 1];
        this.selectedEntity.vertices.push({
          lon: lastVertex.lon + 0.005,
          lat: lastVertex.lat + 0.005
        });
      }
    },
    
    removeVertex(index) {
      if (this.selectedEntity && this.selectedEntity.type === 'polygon' && this.selectedEntity.vertices.length > 3) {
        this.selectedEntity.vertices.splice(index, 1);
      }
    },
    
    
    saveConfig() {
      // 保存配置到本地存储
      const config = {
        entities: this.entities.map(e => {
          const entityCopy = {...e};
          delete entityCopy.cesiumEntity; // 移除Cesium对象
          return entityCopy;
        })
      };
      
      localStorage.setItem('airspaceConfig', JSON.stringify(config));
      this.$message.success('配置已保存');
    },
    
    clearAll() {
      ElMessageBox.confirm(`确定要清除所有空域吗？`, "提示", {
        type: "warning",
      }).then(async () => {
          // 从地图移除所有实体
          this.entities.forEach(entity => {
            if (entity.cesiumEntity) {
              window.viewer.entities.remove(entity.cesiumEntity);
            }
          });
          
          // 清空实体列表
          this.entities = [];
          this.selectedEntity = null;
        })
        .catch(() => {});
    },
    clearAllKy(){
      this.radiusClearLayer()
      // 从地图移除所有实体
          this.entities.forEach(entity => {
            if (entity.cesiumEntity) {
              window.viewer.entities.remove(entity.cesiumEntity);
            }
          });
          
          // 清空实体列表
          this.entities = [];
          this.selectedEntity = null;
    },
    zoomIn() {
      window.viewer.camera.zoomIn(5000);
    },
    
    zoomOut() {
      window.viewer.camera.zoomOut(5000);
    },
    
    resetView() {
      window.viewer.camera.setView({
        destination: window.Cesium.Cartesian3.fromDegrees(116.3974, 39.9088, 15000)
      });
    },
    
    toggleFullscreen() {
      const container = document.getElementById('cesiumContainer');
      if (!document.fullscreenElement) {
        container.requestFullscreen().catch(err => {
          console.error(`全屏错误: ${err.message}`);
        });
      } else {
        if (document.exitFullscreen) {
          document.exitFullscreen();
        }
      }
    },
    
    // queryDrones() {
    //   // 模拟无人机查询
    //   this.loading = true;
      
    //   setTimeout(() => {
    //     // 生成模拟数据
    //     this.droneResults = [];
    //     const count = Math.floor(Math.random() * 5) + 1;
        
    //     for (let i = 0; i < count; i++) {
    //       const statuses = ['flying', 'hovering', 'landed'];
    //       const status = this.droneStatusFilter === 'all' 
    //         ? statuses[Math.floor(Math.random() * statuses.length)]
    //         : this.droneStatusFilter;
          
    //       // 在选中的空域附近生成无人机
    //       let lon, lat;
    //       if (this.selectedEntity) {
    //         const offset = (Math.random() - 0.5) * (this.queryRadius / 100000);
    //         lon = this.selectedEntity.lon + offset;
    //         lat = this.selectedEntity.lat + offset;
    //       } else {
    //         lon = 116.3974 + (Math.random() - 0.5) * 0.1;
    //         lat = 39.9088 + (Math.random() - 0.5) * 0.1;
    //       }
          
    //       this.droneResults.push({
    //         id: `DR-${Math.floor(Math.random() * 1000)}`,
    //         lon,
    //         lat,
    //         altitude: Math.floor(Math.random() * 500) + 100,
    //         status
    //       });
    //     }
        
    //     this.loading = false;
    //   }, 800);
    // },

    // 判断点是否在圆形空域内
 isPointInCircle(pointLon, pointLat, circleLon, circleLat, radius) {
    // 计算两点间距离（米）
    const R = 6371000; // 地球半径（米）
    const φ1 = window.Cesium.Math.toRadians(pointLat);
    const φ2 = window.Cesium.Math.toRadians(circleLat);
    const Δφ = window.Cesium.Math.toRadians(circleLat - pointLat);
    const Δλ = window.Cesium.Math.toRadians(circleLon - pointLon);

    const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
              Math.cos(φ1) * Math.cos(φ2) *
              Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    
    const distance = R * c; // 两点间距离（米）
    return distance <= radius;
},

// 判断点是否在矩形空域内（简化版，未考虑旋转）
 isPointInRectangle(pointLon, pointLat, rectCenterLon, rectCenterLat, width, length) {
    // 将米转换为经纬度大致偏移量（1度≈111319米）
    const meterToDegree = 1 / 111319;
    const halfWidth = width * meterToDegree / 2;
    const halfLength = length * meterToDegree / 2;
    
    return pointLon >= rectCenterLon - halfWidth &&
           pointLon <= rectCenterLon + halfWidth &&
           pointLat >= rectCenterLat - halfLength &&
           pointLat <= rectCenterLat + halfLength;
},

// 判断点是否在多边形内（射线法）
 isPointInPolygon(pointLon, pointLat, polygonVertices) {
    let inside = false;
    for (let i = 0, j = polygonVertices.length - 1; i < polygonVertices.length; j = i++) {
        const xi = polygonVertices[i].lon, yi = polygonVertices[i].lat;
        const xj = polygonVertices[j].lon, yj = polygonVertices[j].lat;
        
        const intersect = ((yi > pointLat) !== (yj > pointLat)) &&
            (pointLon < (xj - xi) * (pointLat - yi) / (yj - yi) + xi);
        if (intersect) inside = !inside;
    }
    return inside;
},
// 初始化无人机查询
    initDroneQuery() {
      // 模拟无人机数据
      this.drones = [
        { id: 'drone-001', name: '测试无人机1', lon:  119.55513, lat: 26.665, altitude: 300, status: 'flying',heading:'220' },
        { id: 'drone-002', name: '测试无人机2', lon:  119.533, lat:26.645, altitude: 500, status: 'hovering' ,heading:'10'},
        { id: 'drone-003', name: '测试无人机3', lon: 119.512, lat: 26.667, altitude: 0, status: 'landed' ,heading:'70'},
        { id: 'drone-004', name: '测试无人机4', lon: 119.599, lat: 26.673, altitude: 200, status: 'flying',heading:'80' },
        { id: 'drone-005', name: '测试无人机5', lon: 119.589, lat: 26.659, altitude: 400, status: 'flying',heading:'40' }
      ];
      // window.API.wrj.list({
      //   pageNo:1,
      //   pageSize:10000
      // }).then(res=>{
      //   if(res.success){
      //     let data = res.result.records;
      //     console.log(data);
      //     data.forEach(item=>{
      //       item.lon = item.currentLongitude
      //       item.lat = item.currentLatitude
      //       item.altitude = item.currentAltitude
      //     })

      //     this.drones=data;
      //   }
        
      // })
      
      // 在地图上添加无人机标记
      this.droneEntities.forEach(entity => {
        window.viewer.entities.remove(entity);
      });
      this.droneEntities = [];
      
      this.drones.forEach(drone => {
        const color = drone.status === 'flying' ? window.Cesium.Color.GREEN : 
                      drone.status === 'hovering' ? window.Cesium.Color.YELLOW : 
                      window.Cesium.Color.GRAY;
                      
        const entity = window.viewer.entities.add({
             id: drone.id,
          position: window.Cesium.Cartesian3.fromDegrees(drone.lon, drone.lat, drone.altitude),
          
          point: {
            pixelSize: 10,
            color: color,
            outlineColor: window.Cesium.Color.BLACK,
            outlineWidth: 2
          },
          model: {
                uri: './wrjmodel/scene.gltf', // GLTF模型路径
                scale: 1.0, // 缩放比例，根据模型大小调整
                minimumPixelSize: 64, // 最小像素大小，确保远处也能看到
                maximumScale: 2000 // 最大缩放比例
            },
          label: {
            text: drone.name,
            font: '12px sans-serif',
            verticalOrigin: window.Cesium.VerticalOrigin.BOTTOM,
            pixelOffset: new window.Cesium.Cartesian2(0, -15)
          },
          properties: {
            type: { _value: 'drone' },
            id: { _value: drone.id },
            status: { _value: drone.status },
            altitude: { _value: drone.altitude }
          }
        });
        
        this.droneEntities.push(entity);

        // 如果无人机处于飞行状态，添加简单的移动动画
        if (drone.status === 'flying') {
            this.startDroneAnimation(entity, drone);
        }
      });
    },

    
// 为飞行中的无人机添加简单动画
 startDroneAnimation(droneEntity, droneData) {
    // 每300毫秒更新一次位置
    const interval = setInterval(() => {
        // 如果无人机已被移除，则清除定时器
        if (!window.viewer.entities.contains(droneEntity)) {
            clearInterval(interval);
            return;
        }
        
        // 根据朝向和速度计算新位置
        const headingRad = window.Cesium.Math.toRadians(droneData.heading);
        const distance = droneData.speed * 0.3; // 300ms内移动的距离
        
        // 计算经纬度变化（简化计算）
        const lonDelta = (distance * Math.sin(headingRad)) / 111319.9;
        const latDelta = (distance * Math.cos(headingRad)) / 111319.9;
        
        // 更新位置
        droneData.lon += lonDelta;
        droneData.lat += latDelta;
        
        // 随机微调朝向，模拟真实飞行
        droneData.heading = (droneData.heading + (Math.random() * 10 - 5)) % 360;
        
        // 更新实体位置和朝向
        const newPosition = window.Cesium.Cartesian3.fromDegrees(
            droneData.lon, 
            droneData.lat, 
            droneData.alt
        );
        
        droneEntity.position = newPosition;
        droneEntity.orientation = window.Cesium.Transforms.headingPitchRollQuaternion(
            newPosition,
            new window.Cesium.HeadingPitchRoll(
                window.Cesium.Math.toRadians(droneData.heading),
                0,
                0
            )
        );
    }, 300);
},
// 修改查询无人机函数，添加空域内判断
 queryDrones() {
    this.loading = true;
    
    setTimeout(() => {
        // 生成模拟数据
        // let allDrones = [];
        // const count = Math.floor(Math.random() * 15) + 5; // 生成更多潜在无人机
        
        // for (let i = 0; i < count; i++) {
        //     const statuses = ['flying', 'hovering', 'landed'];
        //     const status = this.droneStatusFilter === 'all' 
        //         ? statuses[Math.floor(Math.random() * statuses.length)]
        //         : this.droneStatusFilter;
            
        //     // 在选中的空域附近生成无人机
        //     let lon, lat;
        //     if (this.selectedEntity) {
        //         // 扩大生成范围，确保有不在空域内的无人机
        //         const offset = (Math.random() - 0.5) * (this.queryRadius / 50000);
        //         lon = this.selectedEntity.lon + offset;
        //         lat = this.selectedEntity.lat + offset;
        //     } else {
        //         lon = 116.3974 + (Math.random() - 0.5) * 0.1;
        //         lat = 39.9088 + (Math.random() - 0.5) * 0.1;
        //     }
            
        //     allDrones.push({
        //         id: `DR-${Math.floor(Math.random() * 1000)}`,
        //         lon,
        //         lat,
        //         altitude: Math.floor(Math.random() * 500) + 100,
        //         status
        //     });
        // }
        
        // 过滤出空域内的无人机
        let inAirspaceDrones = [];
        if (this.selectedEntity) {
            switch(this.selectedEntity.type) {
                case 'circle':
                    inAirspaceDrones = this.drones.filter(drone => 
                        this.isPointInCircle(
                            drone.lon, drone.lat,
                            this.selectedEntity.lon, this.selectedEntity.lat,
                            this.selectedEntity.radius
                        ) && 
                        drone.altitude >= this.selectedEntity.minHeight &&
                        drone.altitude <= this.selectedEntity.maxHeight
                    );
                    break;
                case 'rectangle':
                    inAirspaceDrones = this.drones.filter(drone => 
                        this.isPointInRectangle(
                            drone.lon, drone.lat,
                            this.selectedEntity.lon, this.selectedEntity.lat,
                            this.selectedEntity.width, this.selectedEntity.length
                        ) && 
                        drone.altitude >= this.selectedEntity.minHeight &&
                        drone.altitude <= this.selectedEntity.maxHeight
                    );
                    break;
                case 'polygon':
                    inAirspaceDrones = this.drones.filter(drone => 
                        this.isPointInPolygon(
                            drone.lon, drone.lat,
                            this.selectedEntity.vertices
                        ) && 
                        drone.altitude >= this.selectedEntity.minHeight &&
                        drone.altitude <= this.selectedEntity.maxHeight
                    );
                    break;
            }
        } else {
            // 未选择空域时返回所有无人机
            inAirspaceDrones = this.drones;
        }
        
        this.droneResults = inAirspaceDrones;
        this.loading = false;
    }, 800);
},
    
    getDroneStatusClass(status) {
      switch (status) {
        case 'flying': return 'text-blue';
        case 'hovering': return 'text-green';
        case 'landed': return 'text-gray';
        default: return '';
      }
    },
    
    getDroneStatusText(status) {
      switch (status) {
        case 'flying': return '飞行中';
        case 'hovering': return '悬停中';
        case 'landed': return '已降落';
        default: return '';
      }
    },
    
    getEntityDescription(entity) {
      if (!entity) return '';
      
      switch (entity.type) {
        case 'circle':
          return `半径: ${entity.radius}m, 高度: ${entity.minHeight}-${entity.maxHeight}m`;
        case 'rectangle':
          return `尺寸: ${entity.width}x${entity.length}m, 高度: ${entity.minHeight}-${entity.maxHeight}m`;
        case 'polygon':
          return `顶点数: ${entity.vertices.length}, 高度: ${entity.minHeight}-${entity.maxHeight}m`;
        default:
          return '';
      }
    },
    
    getDrawingHintText() {
      switch (this.activeShapeType) {
        case 'circle': return '点击地图添加圆形空域';
        case 'rectangle': return '点击地图添加矩形空域';
        case 'polygon': return '点击地图添加多边形顶点，双击完成';
        default: return '';
      }
    },
    
    showNotification(message, type = 'success') {
      const notification = document.createElement('div');
      notification.className = `notification ${type}`;
      notification.textContent = message;
      document.body.appendChild(notification);
      
      setTimeout(() => {
        notification.remove();
      }, 3000);
    }
  }
}
</script>

<style lang="less" scoped>
html, body {
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}
.topCenter {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  pointer-events: none;
}
.hidden {
  display: none !important;
}
.panel {
  position: absolute;
  // background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 15px;
  z-index: 10;
  pointer-events: auto;
  background: url("@/assets/allImage/dialogBg.png") no-repeat;
      background-size: 100% 100%;
      color: #fff;
}
.toolbar {
  top: 15px;
  left: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.properties-panel {
  top: 15px;
  right: 15px;
  width: 300px;
  max-height: calc(100% - 30px);
  overflow-y: auto;
}
.drawing-hint {
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  background: #1a425e;
  color: white;
  padding: 8px 15px;
  border-radius: 20px;
}
.btn {
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: background 0.2s;
}
.btn:hover {
  background: #0056b3;
}
.shape-btn {
  justify-content: center;
  padding: 10px;
}
.shape-active {
  background: #28a745;
}
.shape-inactive {
  background: #6c757d;
}
.form-group {
  margin-bottom: 12px;
}
label {
  display: block;
  margin-bottom: 5px;
  font-size: 14px;
  color: #fff;
}
input, select {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}
.slider-group {
  margin-bottom: 20px;
}
.slider {
  width: 100%;
}
.action-buttons {
  display: flex;
  gap: 8px;
  margin-top: 15px;
}
.loading-indicator {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 15px 30px;
  border-radius: 8px;
  z-index: 100;
}
.notification {
  position: absolute;
  top: 20px;
  right: 20px;
  padding: 12px 20px;
  border-radius: 4px;
  color: white;
  z-index: 1000;
  animation: fadein 0.5s, fadeout 0.5s 2.5s;
}
.success {
  background: #28a745;
}
.error {
  background: #dc3545;
}
@keyframes fadein {
  from {top: 0; opacity: 0;}
  to {top: 20px; opacity: 1;}
}
@keyframes fadeout {
  from {top: 20px; opacity: 1;}
  to {top: 0; opacity: 0;}
}
.map-controls {
  position: absolute;
  bottom: 15px;
  left: 15px;
  display: flex;
  gap: 10px;
  z-index: 10;
}
.map-control-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  border: none;
}
.vertex-item {
  margin-bottom: 10px;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 4px;
}
/* 无人机查询相关样式 */
.text-blue { color: #007bff; }
.text-green { color: #28a745; }
.text-gray { color: #6c757d; }
.text-danger { color: #dc3545; }



.historyDataBox{
    width: 600px;
    height: 650px;
    background: url("@/assets/allImage/dialogBg.png") no-repeat;
    background-size: 100% 100%;
    position: absolute;
    left: 20px;
    top:0;
    padding: 10px;
    pointer-events: auto;
    .tableBox{
      height: calc(100% - 32px);
    }
  }
  :deep(.el-table .el-table__body td.el-table-fixed-column--right.el-table__cell){
        background: #27598c  !important;
      }
      :deep(.el-table th.el-table-fixed-column--right.el-table__cell){
        background: #27598c  !important;
      }
</style>