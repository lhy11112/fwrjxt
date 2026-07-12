

export default {
  viewer:{},
  wrjData:[],
  sj:'',
  firstPersonListener:null,
  circleEntity:null,
  timePoints:[],
 init(viewer){
  this.viewer = viewer;
 },
 // 生成模拟的无人机轨迹数据
 generateDronePath(data) {
  this.wrjData=data;
  const path = [];
  // const startTime = Cesium.JulianDate.fromIso8601('2023-01-01T08:00:00Z');
  // console.log(startTime);
  console.log(data);
  // 生成一个包含100个点的轨迹
  for (let i = 0; i < data.length; i++) {
    this.timePoints.push(Cesium.JulianDate.fromIso8601(data[i].dataTime.replace(" ", "T") + "Z"))
   
    // const time = Cesium.JulianDate.addSeconds(
    //   Cesium.JulianDate.fromIso8601(data[i].dataTime.replace(" ", "T") + "Z"), 
    //   0, 
    //   new Cesium.JulianDate()
    // );
    const time = Cesium.JulianDate.fromIso8601(data[i].dataTime.replace(" ", "T") + "Z");
    const lon = data[i].dronLng; // 围绕北京附近
    const lat = data[i].dronLat;
    const height = data[i].height; // 高度逐渐增加
    path.push({
      time: time,
      position: Cesium.Cartesian3.fromDegrees(lon, lat, height),
      properties: data[i]
    });

  }
  return path;
},

setSj(data){
  let that = this;
  that.sj = data;
  if(that.sj==1){
    // that.setFirstView();
     // 取消实体跟踪，以便我们手动控制相机
    //  that.viewer.trackedEntity = undefined;

     // 确保只有一个监听器在运行
     if (that.firstPersonListener) {
      that.viewer.scene.postUpdate.removeEventListener(that.firstPersonListener);
      that.firstPersonListener = null;
  }
    // 添加第一人称的事件监听
    that.firstPersonListener = that.viewer.scene.postUpdate.addEventListener(updateFirstPersonCamera);
  }else if(that.sj==3){
    // that.setThirdView();
    
// console.log('333,',that.firstPersonListener);
  // 确保只有一个监听器在运行
  if (that.firstPersonListener) {
    that.viewer.scene.postUpdate.removeEventListener(that.firstPersonListener);
    that.firstPersonListener = null;
  }
  // 添加第一人称的事件监听
  that.firstPersonListener = that.viewer.scene.postUpdate.addEventListener(updateFirstPersonCamera3);
  // 相机跟踪无人机
  that.viewer.trackedEntity = that.droneEntity;
  
  }

  
  // **第一人称 (追逐) 视角更新函数**
  function updateFirstPersonCamera() {
  const position = that.droneEntity.position.getValue(that.viewer.clock.currentTime);
  const orientation = that.droneEntity.orientation.getValue(that.viewer.clock.currentTime);
  // console.log(orientation);
  if (Cesium.defined(position) && Cesium.defined(orientation)) {
      const transform = Cesium.Matrix4.fromRotationTranslation(
          Cesium.Matrix3.fromQuaternion(orientation, new Cesium.Matrix3()),
          position
      );

      // **关键：定义相机在无人机后方的偏移量**
      // new Cesium.Cartesian3(X, Y, Z)
      // X: 负数代表后方, 正数代表前方
      // Y: 负数代表左方, 正数代表右方
      // Z: 负数代表下方, 正数代表上方
      const offset = new Cesium.Cartesian3(-1,0,0);
      
      that.viewer.camera.lookAtTransform(transform, offset);
  }
  }

  // **第一人称 (追逐) 视角更新函数**
  function updateFirstPersonCamera3() {
  const position = that.droneEntity.position.getValue(that.viewer.clock.currentTime);
  const orientation = that.droneEntity.orientation.getValue(that.viewer.clock.currentTime);
  if (Cesium.defined(position) && Cesium.defined(orientation)) {
      const transform = Cesium.Matrix4.fromRotationTranslation(
          Cesium.Matrix3.fromQuaternion(orientation, new Cesium.Matrix3()),
          position
      );

      // **关键：定义相机在无人机后方的偏移量**
      // new Cesium.Cartesian3(X, Y, Z)
      // X: 负数代表后方, 正数代表前方
      // Y: 负数代表左方, 正数代表右方
      // Z: 负数代表下方, 正数代表上方
      const offset = new Cesium.Cartesian3(-100.0, 0, 20.0);
      
      that.viewer.camera.lookAtTransform(transform, offset);
  }
  }
},

// 创建无人机模型
createDroneModel(data) {
  return this.viewer.entities.add({
    name: '无人机',
    model: {
      uri: '/wrjmodel/scene.gltf',
      scale: 2.0,
      minimumPixelSize: 64
    },
    position: Cesium.Cartesian3.fromDegrees(data[0].dronLng, data[0].dronLat, data[0].height),
    orientation: new Cesium.VelocityOrientationProperty(),
    properties:new Cesium.SampledProperty(Number)
  });
  
},

// 轨迹回放控制器类
DronePlaybackController(path, droneEntity,wrjJbxx) {
  this.path = path;
  this.droneEntity = droneEntity;
  this.isPlaying = true;
  this.playbackSpeed = 5.0;
  
  if(this.path.length<=1){
    // 创建无人机下方的圆形
    if(wrjJbxx){
      this.circleEntity = this.createCircle(this.droneEntity,wrjJbxx);
    }
    // 相机跟踪无人机
    this.viewer.trackedEntity = this.droneEntity;
    return;
  }
  
  // 设置位置属性
  this.positionProperty = new Cesium.SampledPositionProperty();
  this.propertiesProperty = new Cesium.SampledProperty(Number);

 
  this.path.forEach(point => {
    this.positionProperty.addSample(point.time, point.position,point.properties);
  });
  this.path.forEach(point => {
      this.propertiesProperty.addSample(point.time, point.properties);
  });
  // console.log(this.propertiesProperty);
  this.droneEntity.properties = this.propertiesProperty;

  this.droneEntity.position = this.positionProperty;
  this.droneEntity.orientation = new Cesium.VelocityOrientationProperty(this.positionProperty);
  
  // 设置轨迹线
  this.createPathLine(wrjJbxx);
  
  
// console.log(this.droneEntity.position);
  // 设置时钟范围

    this.viewer.clock.startTime = this.path[0].time.clone();
    this.viewer.clock.stopTime = this.path[this.path.length - 1].time.clone();
    this.viewer.clock.currentTime = this.path[0].time.clone();

  

  
  
  console.log('配置时间轴的初始时间范围',this.path[0].time.clone(),this.path[this.path.length - 1].time.clone());
  // 配置时间轴的初始时间范围
  this.viewer.timeline.zoomTo(this.path[0].time.clone(),this.path[this.path.length - 1].time.clone());
  this.viewer.timeline.container.style.display = 'block';

  this.viewer.clock.clockRange = Cesium.ClockRange.LOOP_STOP;
  this.viewer.clock.multiplier = this.playbackSpeed;
  console.log(this.droneEntity);

  // 相机跟踪无人机
  this.viewer.trackedEntity = this.droneEntity;
  let that=this;
  this.viewer.entities.add({
    polygon: {
      hierarchy: new Cesium.CallbackProperty(function(time) {
          const position = that.droneEntity.position.getValue(time);
          if (!position) { return undefined; }
          const orientation = that.droneEntity.orientation.getValue(time);
          if (!orientation) { return undefined; }

          const rotationMatrix = Cesium.Matrix3.fromQuaternion(orientation, new Cesium.Matrix3());
          const modelMatrix = Cesium.Matrix4.fromRotationTranslation(rotationMatrix, position, new Cesium.Matrix4());
          const hpr = Cesium.Transforms.fixedFrameToHeadingPitchRoll(modelMatrix);
          if (!hpr) { return undefined; }
          const heading = hpr.heading;

          const points = [position];
          const radius = 5000.0;
          const angle = Cesium.Math.toRadians(45);
          
          const offsetAngle = Cesium.Math.toRadians(90); // 偏移角度
          const eastNorthUpTransform = Cesium.Transforms.eastNorthUpToFixedFrame(position);
          
         // 创建一个基于航向的笛卡尔坐标偏移量
          // const headingOffset = Cesium.Cartesian3.fromRadians(heading, 0, 0);
          // // 计算扇形的中心点
          // const centerPoint = Cesium.Cartesian3.add(position, headingOffset, new Cesium.Cartesian3());

          // // 创建一个东北地平坐标系变换
          // const eastNorthUpTransform = Cesium.Transforms.eastNorthUpToFixedFrame(centerPoint);

          for (let i = 0; i <= 45; i++) {
              const currentAngle = heading - angle / 2 + Cesium.Math.toRadians(i) + offsetAngle;
              const localPoint = new Cesium.Cartesian3(
                  radius * Math.sin(currentAngle),
                  radius * Math.cos(currentAngle),
                  0
              );
              points.push(
                  Cesium.Matrix4.multiplyByPoint(eastNorthUpTransform, localPoint, new Cesium.Cartesian3())
              );
          }
          return new Cesium.PolygonHierarchy(points);
      }, false),
      material: Cesium.Color.RED.withAlpha(0.2),
      outline: true,
      outlineColor: Cesium.Color.BLACK,
      perPositionHeight: true,
  },
});

// 创建无人机下方的圆形
if(wrjJbxx){
  this.circleEntity = this.createCircle(this.droneEntity,wrjJbxx);
}

  // 绑定时钟事件
  this.bindClockEvents();

  // 创建无人机下方的圆形
  if(wrjJbxx){
    this.circleEntity = this.createCircle(this.droneEntity,wrjJbxx);
  }
},
createCircle(droneEntity,wrjJbxx) {
  const that = this;
  // console.log(droneEntity,wrjJbxx);
  let color = wrjJbxx.authStatus==1?'#57c943': wrjJbxx.authStatus==2?'#FF0000':'#d7b931';
  

 
// console.log(color,wrjJbxx);
  // 使用 CallbackProperty 动态计算圆形位置
  return this.viewer.entities.add({
    position: new Cesium.CallbackProperty(function(time) {
      const position = droneEntity.position.getValue(time, new Cesium.Cartesian3());
      
  
      // const model = droneEntity.model;
      // const boundingSphere = model.boundingSphere;
      // const modelHeight = boundingSphere.radius * model.scale;

      // console.log(model,boundingSphere,position)
  
      // 计算圆形底部位置（假设模型中心在顶部）
      // const circleHeight = position.z - modelHeight;
  
      // 返回新的位置（x, y 保持不变，z 调整）
      return new Cesium.Cartesian3(position.x, position.y, position.z);
    }, false),
    ellipse: {
      semiMinorAxis:20,
      semiMajorAxis: 20,
      material:window.Cesium.Color.fromCssColorString(color).withAlpha(0.15),
      outline: true,
      outlineColor: window.Cesium.Color.BLACK
    },
    label: {
      text: `${wrjJbxx.brand}-${wrjJbxx.model}(${wrjJbxx.serialNumber})`,
      show: true,
      font: '14px sans-serif',
      pixelOffset: new window.Cesium.Cartesian2(0,-60)
    },
  });
},

// 切换播放/暂停
togglePlayPause(data) {
  this.isPlaying = data;
  this.viewer.clock.shouldAnimate = this.isPlaying;
},

// 更新播放速度
updateSpeed() {
  this.viewer.clock.multiplier = this.playbackSpeed;
},

// 根据进度条定位
seekToProgress() {
  const totalSeconds = Cesium.JulianDate.secondsDifference(
    this.viewer.clock.stopTime, 
    this.viewer.clock.startTime
  );
  const targetSeconds = totalSeconds * (this.progress / 100);
  const targetTime = Cesium.JulianDate.addSeconds(
    this.viewer.clock.startTime, 
    targetSeconds, 
    new Cesium.JulianDate()
  );
  
  this.viewer.clock.currentTime = targetTime;
},

// 绑定时钟事件
bindClockEvents() {
  const that = this;
  that.viewer.clock.onTick.addEventListener((clock) => {
    // console.log(clock);
    const position = that.droneEntity.position.getValue(clock.currentTime, new Cesium.Cartesian3());
    const cartographic = Cesium.Cartographic.fromCartesian(position);
    const longitude = Cesium.Math.toDegrees(cartographic.longitude).toFixed(6);
    const latitude = Cesium.Math.toDegrees(cartographic.latitude).toFixed(6);
    const altitude = cartographic.height.toFixed(2);
    
    // console.log(position,longitude,latitude,altitude);
    // window.eventBus.emit("wrjWz", {longitude:longitude,latitude:latitude,altitude:altitude});
    // 更新圈的位置
    // const circlePosition = Cesium.Cartesian3.fromDegrees(position.x, position.y, position.z - 50.0);
    // that.circleEntity.position.setValue(circlePosition);

    const totalSeconds = Cesium.JulianDate.secondsDifference(
      that.viewer.clock.stopTime, 
      that.viewer.clock.startTime
    );
    const currentSeconds = Cesium.JulianDate.secondsDifference(
      that.viewer.clock.currentTime, 
      that.viewer.clock.startTime
    );
     
    
    // 更新进度
    const percent = Math.min(100, Math.max(0, (currentSeconds / totalSeconds) * 100));
    that.progress = percent;
    
    // 格式化时间显示
    const totalSecondsElapsed = Math.floor(currentSeconds);
    const hours = Math.floor(totalSecondsElapsed / 3600);
    const minutes = Math.floor((totalSecondsElapsed % 3600) / 60);
    const seconds = totalSecondsElapsed % 60;

    // 定义两个点的经纬度和高度（单位：米）
    const position1 = Cesium.Cartesian3.fromDegrees(Number(longitude), Number(latitude),Number(altitude));
    const position2 = Cesium.Cartesian3.fromDegrees(Number(this.wrjData[0].dronLng), Number(this.wrjData[0].dronLat), Number(this.wrjData[0].altitude));

   // 计算两点之间的三维空间距离（单位：米）
   const distance = Cesium.Cartesian3.distance(position1, position2).toFixed(2);
   const speed = (distance / totalSecondsElapsed).toFixed(8)
  //  window.TOOL.data.set('wrjData',{longitude:Number(longitude),latitude:Number(latitude),altitude:Number(altitude),speed:speed})
  //  window.eventBus.emit("wrjData",{longitude:Number(longitude),latitude:Number(latitude),altitude:Number(altitude),speed:speed})
    
    that.formattedTime = `${hours.toString().padStart(2, '0')}:${
      minutes.toString().padStart(2, '0')}:${
      seconds.toString().padStart(2, '0')}`;
    // console.log('xxxxxx',that.formattedTime);
    // 更新播放状态
    if (Cesium.JulianDate.equals(clock.currentTime, clock.stopTime)) {
      that.isPlaying = false;
    }

    // const orientation = droneEntity.orientation.getValue(clock.currentTime);
    // if (Cesium.defined(position) && Cesium.defined(orientation)) {
    //     const transform = Cesium.Matrix4.fromRotationTranslation(
    //         Cesium.Matrix3.fromQuaternion(orientation, new Cesium.Matrix3()),
    //         position
    //     );
    //     const offset = new Cesium.Cartesian3(-800.0, 0, 300.0);
    //     this.viewer.camera.lookAtTransform(transform, offset);
    // }
    // const droneProperties = that.droneEntity.properties.getValue(clock.currentTime, new Cesium.SampledProperty(Number))
    // console.log(droneProperties); // Outputs the properties of the drone
    for (let i = 0; i < that.timePoints.length - 1; i++) {
      if (Cesium.JulianDate.greaterThanOrEquals(clock.currentTime, that.timePoints[i]) && 
          Cesium.JulianDate.lessThanOrEquals(clock.currentTime, that.timePoints[i + 1])) {
          // console.log(that.wrjData[i]);
          window.TOOL.data.set('wrjData',{longitude:Number(longitude),latitude:Number(latitude),altitude:Number(altitude),speed:that.wrjData[i].sd})
          window.eventBus.emit("wrjData",{longitude:Number(longitude),latitude:Number(latitude),altitude:Number(altitude),speed:that.wrjData[i].sd})
          window.eventBus.emit("wrjDetailData",that.wrjData[i])
          break;
      }
    }
  });

  
},
// 创建轨迹线
createPathLine(wrjJbxx) {
  console.log('xxxxxxx',wrjJbxx,wrjJbxx.color.slice(1,3));
  const positions = this.path.map(point => point.position);
  
  this.viewer.entities.add({
    name: '飞行轨迹',
    polyline: {
      positions: positions,
      width: wrjJbxx.weight?Number(wrjJbxx.weight):3,
      // material: new Cesium.PolylineGlowMaterialProperty({
      //   glowPower: 0.2,
      //   color: wrjJbxx.color? new Cesium.Color(
      //     parseInt(wrjJbxx.color.slice(1,3), 16)/255,
      //     parseInt(wrjJbxx.color.slice(3,5), 16)/255,
      //     parseInt(wrjJbxx.color.slice(5,7), 16)/255,
      //     1.0
      //   ) :Cesium.Color.YELLOW
      // }),
      material: wrjJbxx.color? new Cesium.Color(
        parseInt(wrjJbxx.color.slice(1,3), 16)/255,
        parseInt(wrjJbxx.color.slice(3,5), 16)/255,
        parseInt(wrjJbxx.color.slice(5,7), 16)/255,
        1.0
      ) :Cesium.Color.YELLOW,
      clampToGround: false
    }
  });
},
setFirstView(dronePosition){
  if(!dronePosition) return;
  const cameraOffets = new Cesium.Cartesian3(20,0,2);
  const heading = 10;
  const hpr = new Cesium.HeadingPitchRoll(heading,0,0);
  const rotation = Cesium.Matrix3.fromHeadingPitchRoll(hpr);
  const offset = Cesium.Matrix3.multiplyByVector(rotation,cameraOffets,new Cesium.Cartesian3())
  const cameraPosition = Cesium.Cartesian3.add(dronePosition,offset,new Cesium.Cartesian3());
  this.viewer.camera.setView({
    destination: cameraPosition,
            orientation: {
                heading: heading,
                pitch: Cesium.Math.toRadians(-5.0),
                roll: 0
            },
  })
},
setThirdView(dronePosition){
  if(!dronePosition) return;
  const cameraOffets = new Cesium.Cartesian3(-50,0,30);
  const heading = 0;
  const hpr = new Cesium.HeadingPitchRoll(heading,0,0);
  const rotation = Cesium.Matrix3.fromHeadingPitchRoll(hpr);
  const offset = Cesium.Matrix3.multiplyByVector(rotation,cameraOffets,new Cesium.Cartesian3())
  const cameraPosition = Cesium.Cartesian3.add(dronePosition,offset,new Cesium.Cartesian3());
  this.viewer.camera.setView({
    destination: cameraPosition,
            orientation: {
                heading: heading,
                pitch: Cesium.Math.toRadians(-30.0),
                roll: 0
            },
  })
},
clearAllLayers() {
  // 清空Viewer中的所有实体
  if(this.viewer.entities){
    this.viewer.entities.removeAll();
  }
},
clearWrjLayers() {
  // 清空Viewer中的所有实体
  if(this.droneEntity){
    this.viewer.entities.remove(this.droneEntity);
  }
}
}