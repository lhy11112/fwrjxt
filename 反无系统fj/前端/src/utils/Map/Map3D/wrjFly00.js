export default {
  viewer: {},
  wrjData: [],
  sj: '',
  firstPersonListener: null,
  circleEntity: null,
  timePoints: [],
  
  // --- 新增：目标分析相关状态 ---
  mbfxEntities: [],          // 存储所有分析产生的实体，用于清理
  processedWaypointIndices: [], // 已处理过的航点索引，防止重复请求
  missionWaypoints: [],      // 任务航点数据
  mbfxConfig: {},            // 分析配置 (如半径)

  init(viewer) {
    this.viewer = viewer;
    this.mbfxEntities = [];
    this.processedWaypointIndices = [];
  },

  // --- 新增：设置任务航点数据 (对应二维的 rowData.value.jhcs) ---
  setMissionConfig(waypoints, config) {
    this.missionWaypoints = waypoints || [];
    this.mbfxConfig = config || { radius: 30 }; // 默认半径 30km
  },

  // 生成模拟的无人机轨迹数据
  generateDronePath(data) {
    this.wrjData = data;
    this.timePoints = []; // 重置时间点在重新生成时
    const path = [];
    for (let i = 0; i < data.length; i++) {
      // 兼容时间格式
      const timeStr = data[i].dataTime.replace(" ", "T") + "Z";
      const time = Cesium.JulianDate.fromIso8601(timeStr);
      this.timePoints.push(time);
      
      const lon = data[i].dronLng;
      const lat = data[i].dronLat;
      const height = data[i].height || data[i].altitude || 0;
      
      path.push({
        time: time,
        position: Cesium.Cartesian3.fromDegrees(lon, lat, height),
        properties: data[i]
      });
    }
    return path;
  },

  setSj(data) {
    let that = this;
    that.sj = data;
    
    // 清理旧的监听器
    if (that.firstPersonListener) {
      that.viewer.scene.postUpdate.removeEventListener(that.firstPersonListener);
      that.firstPersonListener = null;
    }

    if (that.sj == 1) {
      that.firstPersonListener = that.viewer.scene.postUpdate.addEventListener(updateFirstPersonCamera);
    } else if (that.sj == 3) {
      that.firstPersonListener = that.viewer.scene.postUpdate.addEventListener(updateFirstPersonCamera3);
      that.viewer.trackedEntity = that.droneEntity;
    }

    function updateFirstPersonCamera() {
      const position = that.droneEntity.position.getValue(that.viewer.clock.currentTime);
      const orientation = that.droneEntity.orientation.getValue(that.viewer.clock.currentTime);
      if (Cesium.defined(position) && Cesium.defined(orientation)) {
        const transform = Cesium.Matrix4.fromRotationTranslation(
          Cesium.Matrix3.fromQuaternion(orientation, new Cesium.Matrix3()),
          position
        );
        const offset = new Cesium.Cartesian3(-1, 0, 0);
        that.viewer.camera.lookAtTransform(transform, offset);
      }
    }

    function updateFirstPersonCamera3() {
      const position = that.droneEntity.position.getValue(that.viewer.clock.currentTime);
      const orientation = that.droneEntity.orientation.getValue(that.viewer.clock.currentTime);
      if (Cesium.defined(position) && Cesium.defined(orientation)) {
        const transform = Cesium.Matrix4.fromRotationTranslation(
          Cesium.Matrix3.fromQuaternion(orientation, new Cesium.Matrix3()),
          position
        );
        const offset = new Cesium.Cartesian3(-100.0, 0, 20.0);
        that.viewer.camera.lookAtTransform(transform, offset);
      }
    }
  },

  // 创建无人机模型
  createDroneModel(data) {
    // 如果已存在先移除
    if (this.droneEntity) {
      this.viewer.entities.remove(this.droneEntity);
    }
    this.droneEntity = this.viewer.entities.add({
      name: '无人机',
      model: {
        uri: '/wrjmodel/scene.gltf',
        scale: 2.0,
        minimumPixelSize: 64
      },
      position: Cesium.Cartesian3.fromDegrees(data[0].dronLng, data[0].dronLat, data[0].height || 0),
      orientation: new Cesium.VelocityOrientationProperty(),
      properties: new Cesium.SampledProperty(Number)
    });
    return this.droneEntity;
  },

  // 轨迹回放控制器类
  DronePlaybackController(path, droneEntity, wrjJbxx) {
    this.path = path;
    this.droneEntity = droneEntity;
    this.isPlaying = true;
    this.playbackSpeed = 5.0;
    
    // 重置分析状态
    this.mbfxEntities = [];
    this.processedWaypointIndices = [];

    if (this.path.length <= 1) {
      if (wrjJbxx) {
        this.circleEntity = this.createCircle(this.droneEntity, wrjJbxx);
      }
      this.viewer.trackedEntity = this.droneEntity;
      return;
    }

    // 设置位置属性
    this.positionProperty = new Cesium.SampledPositionProperty();
    this.propertiesProperty = new Cesium.SampledProperty(Number);

    this.path.forEach(point => {
      this.positionProperty.addSample(point.time, point.position, point.properties);
      this.propertiesProperty.addSample(point.time, point.properties);
    });

    this.droneEntity.properties = this.propertiesProperty;
    this.droneEntity.position = this.positionProperty;
    this.droneEntity.orientation = new Cesium.VelocityOrientationProperty(this.positionProperty);

    // 设置轨迹线
    this.createPathLine(wrjJbxx);

    // 设置时钟范围
    this.viewer.clock.startTime = this.path[0].time.clone();
    this.viewer.clock.stopTime = this.path[this.path.length - 1].time.clone();
    this.viewer.clock.currentTime = this.path[0].time.clone();

    this.viewer.timeline.zoomTo(this.path[0].time.clone(), this.path[this.path.length - 1].time.clone());
    this.viewer.timeline.container.style.display = 'block';
    this.viewer.clock.clockRange = Cesium.ClockRange.LOOP_STOP;
    this.viewer.clock.multiplier = this.playbackSpeed;

    // 相机跟踪无人机
    this.viewer.trackedEntity = this.droneEntity;
    let that = this;

    // 添加扇形视野 (原有功能)
    this.viewer.entities.add({
      polygon: {
        hierarchy: new Cesium.CallbackProperty(function(time) {
          const position = that.droneEntity.position.getValue(time);
          if (!position) return undefined;
          const orientation = that.droneEntity.orientation.getValue(time);
          if (!orientation) return undefined;

          const rotationMatrix = Cesium.Matrix3.fromQuaternion(orientation, new Cesium.Matrix3());
          const modelMatrix = Cesium.Matrix4.fromRotationTranslation(rotationMatrix, position, new Cesium.Matrix4());
          const hpr = Cesium.Transforms.fixedFrameToHeadingPitchRoll(modelMatrix);
          if (!hpr) return undefined;
          const heading = hpr.heading;

          const points = [position];
          const radius = 5000.0;
          const angle = Cesium.Math.toRadians(45);
          const offsetAngle = Cesium.Math.toRadians(90);
          const eastNorthUpTransform = Cesium.Transforms.eastNorthUpToFixedFrame(position);

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
    if (wrjJbxx) {
      this.circleEntity = this.createCircle(this.droneEntity, wrjJbxx);
    }

    // 绑定时钟事件 (包含目标分析触发)
    this.bindClockEvents();
  },

  // --- 新增：目标分析可视化 (3D 版 zbmbAddMap) ---
  createMbfxVisualization(data, jd, wd, height, index) {
    if (!data || !Array.isArray(data)) return;
    
    const centerPos = Cesium.Cartesian3.fromDegrees(Number(jd), Number(wd), Number(height));
    const jl = this.mbfxConfig.radius || 30;
    const radius = jl * 1000;
    const colorCode = this.wrjJbxx?.authStatus == 1 ? '#57c943' : this.wrjJbxx?.authStatus == 2 ? '#FF0000' : '#d7b931';
    const color = Cesium.Color.fromCssColorString(colorCode).withAlpha(0.3);

    // 1. 创建分析范围圆 (Ellipse)
    const circleEntity = this.viewer.entities.add({
      position: centerPos,
      ellipse: {
        semiMinorAxis: radius,
        semiMajorAxis: radius,
        material: color,
        outline: true,
        outlineColor: Cesium.Color.BLACK,
        height: 0 // 贴地
      }
    });
    this.mbfxEntities.push(circleEntity);

    // 2. 创建中心点标记
    const centerMarker = this.viewer.entities.add({
      position: centerPos,
      point: {
        pixelSize: 10,
        color: Cesium.Color.RED,
        outlineColor: Cesium.Color.WHITE,
        outlineWidth: 2
      }
    });
    this.mbfxEntities.push(centerMarker);

    // 3. 遍历目标数据创建标记和连线
    data.forEach((item) => {
      const itemLat = Number(item.wd || item.WD);
      const itemLng = Number(item.jd || item.JD);
      const itemName = item.mc || item.dmmc || '未知目标';
      
      if (!itemLat || !itemLng) return;

      const targetPos = Cesium.Cartesian3.fromDegrees(itemLng, itemLat, 0); // 目标默认在地面
      
      // 获取图标 URL
      let iconUrl = '/static/map_img/重要目标.png';
      if (itemName.indexOf('医院') !== -1) iconUrl = '/static/map_img/医院.png';
      else if (itemName.indexOf('学校') !== -1) iconUrl = '/static/map_img/高等院校.png';
      else if (itemName.indexOf('加油站') !== -1) iconUrl = '/static/map_img/加油站.png';

      // 创建目标 Billboard
      const marker = this.viewer.entities.add({
        position: targetPos,
        billboard: {
          image: iconUrl,
          scale: 0.8,
          verticalOrigin: Cesium.VerticalOrigin.BOTTOM
        },
        label: {
          text: itemName,
          font: '12px sans-serif',
          pixelOffset: new Cesium.Cartesian2(0, -40),
          showBackground: true,
          backgroundColor: Cesium.Color.WHITE.withAlpha(0.7)
        }
      });
      this.mbfxEntities.push(marker);

      // 计算距离 (3D 空间距离)
      const distance = Cesium.Cartesian3.distance(centerPos, targetPos);
      const distanceText = (distance).toFixed(0) + 'm';

      // 创建连线 (无人机航点 -> 目标)
      const polyline = this.viewer.entities.add({
        polyline: {
          positions: [centerPos, targetPos],
          width: 2,
          material: new Cesium.PolylineDashMaterialProperty({
            color: Cesium.Color.RED
          })
        }
      });
      this.mbfxEntities.push(polyline);

      // 创建距离标签 (中点)
      // 简单计算中点 (对于短距离足够)
      const midX = (centerPos.x + targetPos.x) / 2;
      const midY = (centerPos.y + targetPos.y) / 2;
      const midZ = (centerPos.z + targetPos.z) / 2;
      const midPos = new Cesium.Cartesian3(midX, midY, midZ);

      const label = this.viewer.entities.add({
        position: midPos,
        label: {
          text: distanceText,
          font: 'bold 12px sans-serif',
          fillColor: Cesium.Color.BLACK,
          backgroundColor: Cesium.Color.WHITE.withAlpha(0.8),
          pixelOffset: new Cesium.Cartesian2(0, 0),
          disableDepthTestDistance: Number.POSITIVE_INFINITY // 防止被地形遮挡
        }
      });
      this.mbfxEntities.push(label);
    });
  },

  // --- 新增：检查是否触发目标分析 ---
  checkAndTriggerMbfx(currentLon, currentLat, currentTime) {
    if (!this.missionWaypoints || this.missionWaypoints.length === 0) return;

    // 遍历航点
    this.missionWaypoints.forEach((wp, index) => {
      // 如果该航点已处理过，跳过
      if (this.processedWaypointIndices.includes(index)) return;

      // 计算当前无人机位置与航点的距离
      const dist = turf.distance([currentLon, currentLat], [wp.longitude, wp.latitude]);
      
      // 阈值：距离航点 0.0001 度 (约 10 米) 以内视为到达
      // 或者使用 Cesium.Cartesian3.distance 更精确，但 turf 足够快
      if (dist < 0.0002) { 
        this.processedWaypointIndices.push(index);
        
        // 调用 API 获取数据
        this.requestMbfxData(wp.longitude, wp.latitude, index);
      }
    });
  },

  // --- 新增：请求 API 数据 ---
  requestMbfxData(jd, wd, index) {
    const jl = this.mbfxConfig.radius || 30;
    // 获取当前高度 (近似)
    const height = this.droneEntity ? this.droneEntity.position.getValue(this.viewer.clock.currentTime) : 0;
    const cartographic = height ? Cesium.Cartographic.fromCartesian(height) : { height: 0 };

    window.API.zbmb.getWrjZymbByJwdAndJlS({
      jd: jd,
      wd: wd,
      jl: jl,
      type: '执勤目标,民生目标,友邻信息'
    }).then((res) => {
      if (res.code == 200 && res.result) {
        // 3D 可视化
        this.createMbfxVisualization(res.result.zymbxx, jd, wd, cartographic.height, index);
      }
    }).catch(err => {
      console.error("MBFX API Error", err);
    });
  },

  createCircle(droneEntity, wrjJbxx) {
    let color = wrjJbxx.authStatus == 1 ? '#57c943' : wrjJbxx.authStatus == 2 ? '#FF0000' : '#d7b931';
    const that = this;
    
    return this.viewer.entities.add({
      position: new Cesium.CallbackProperty(function(time) {
        const position = droneEntity.position.getValue(time, new Cesium.Cartesian3());
        return position;
      }, false),
      ellipse: {
        semiMinorAxis: 20,
        semiMajorAxis: 20,
        material: window.Cesium.Color.fromCssColorString(color).withAlpha(0.15),
        outline: true,
        outlineColor: window.Cesium.Color.BLACK
      },
      label: {
        text: `${wrjJbxx.brand}-${wrjJbxx.model}(${wrjJbxx.serialNumber})`,
        show: true,
        font: '14px sans-serif',
        pixelOffset: new window.Cesium.Cartesian2(0, -60)
      },
    });
  },

  togglePlayPause(data) {
    this.isPlaying = data;
    this.viewer.clock.shouldAnimate = this.isPlaying;
  },

  updateSpeed() {
    this.viewer.clock.multiplier = this.playbackSpeed;
  },

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
    // 保存 wrjJbxx 引用以便在 tick 中使用 (假设在 DronePlaybackController 中 this.wrjJbxx 已赋值)
    // 如果未赋值，请确保在调用 DronePlaybackController 时保存该引用
    // 这里假设 wrjJbxx 挂载在 this 上，或者通过闭包获取
    // 修正：DronePlaybackController 参数里有 wrjJbxx，需保存
    // this.wrjJbxx = wrjJbxx; // 确保在 Controller 里保存了

    that.viewer.clock.onTick.addEventListener((clock) => {
      const position = that.droneEntity.position.getValue(clock.currentTime, new Cesium.Cartesian3());
      if(!position) return;

      const cartographic = Cesium.Cartographic.fromCartesian(position);
      const longitude = Cesium.Math.toDegrees(cartographic.longitude);
      const latitude = Cesium.Math.toDegrees(cartographic.latitude);
      const altitude = cartographic.height;

      // --- 新增：触发目标分析检查 ---
      that.checkAndTriggerMbfx(longitude, latitude, clock.currentTime);

      const totalSeconds = Cesium.JulianDate.secondsDifference(
        that.viewer.clock.stopTime,
        that.viewer.clock.startTime
      );
      const currentSeconds = Cesium.JulianDate.secondsDifference(
        that.viewer.clock.currentTime,
        that.viewer.clock.startTime
      );

      const percent = Math.min(100, Math.max(0, (currentSeconds / totalSeconds) * 100));
      that.progress = percent;

      const totalSecondsElapsed = Math.floor(currentSeconds);
      const hours = Math.floor(totalSecondsElapsed / 3600);
      const minutes = Math.floor((totalSecondsElapsed % 3600) / 60);
      const seconds = totalSecondsElapsed % 60;

      that.formattedTime = `${hours.toString().padStart(2, '0')}:${
        minutes.toString().padStart(2, '0')}:${
        seconds.toString().padStart(2, '0')}`;

      if (Cesium.JulianDate.equals(clock.currentTime, clock.stopTime)) {
        that.isPlaying = false;
        // 播放结束时可选择是否清理分析图层，这里保留
      }

      // 数据同步
      for (let i = 0; i < that.timePoints.length - 1; i++) {
        if (Cesium.JulianDate.greaterThanOrEquals(clock.currentTime, that.timePoints[i]) &&
          Cesium.JulianDate.lessThanOrEquals(clock.currentTime, that.timePoints[i + 1])) {
          
          // 发送当前帧数据
          const speed = that.wrjData[i].sd || 0;
          window.TOOL.data.set('wrjData', {
            longitude: Number(longitude),
            latitude: Number(latitude),
            altitude: Number(altitude),
            speed: speed
          });
          window.eventBus.emit("wrjData", {
            longitude: Number(longitude),
            latitude: Number(latitude),
            altitude: Number(altitude),
            speed: speed
          });
          window.eventBus.emit("wrjDetailData", that.wrjData[i]);
          break;
        }
      }
    });
  },

  createPathLine(wrjJbxx) {
    // 保存引用以便清理
    this.wrjJbxx = wrjJbxx; 
    const positions = this.path.map(point => point.position);

    this.viewer.entities.add({
      name: '飞行轨迹',
      polyline: {
        positions: positions,
        width: wrjJbxx && wrjJbxx.weight ? Number(wrjJbxx.weight) : 3,
        material: wrjJbxx && wrjJbxx.color ? new Cesium.Color(
          parseInt(wrjJbxx.color.slice(1, 3), 16) / 255,
          parseInt(wrjJbxx.color.slice(3, 5), 16) / 255,
          parseInt(wrjJbxx.color.slice(5, 7), 16) / 255,
          1.0
        ) : Cesium.Color.YELLOW,
        clampToGround: false
      }
    });
  },

  setFirstView(dronePosition) {
    if (!dronePosition) return;
    const cameraOffets = new Cesium.Cartesian3(20, 0, 2);
    const heading = 10;
    const hpr = new Cesium.HeadingPitchRoll(heading, 0, 0);
    const rotation = Cesium.Matrix3.fromHeadingPitchRoll(hpr);
    const offset = Cesium.Matrix3.multiplyByVector(rotation, cameraOffets, new Cesium.Cartesian3());
    const cameraPosition = Cesium.Cartesian3.add(dronePosition, offset, new Cesium.Cartesian3());
    this.viewer.camera.setView({
      destination: cameraPosition,
      orientation: {
        heading: heading,
        pitch: Cesium.Math.toRadians(-5.0),
        roll: 0
      },
    });
  },

  setThirdView(dronePosition) {
    if (!dronePosition) return;
    const cameraOffets = new Cesium.Cartesian3(-50, 0, 30);
    const heading = 0;
    const hpr = new Cesium.HeadingPitchRoll(heading, 0, 0);
    const rotation = Cesium.Matrix3.fromHeadingPitchRoll(hpr);
    const offset = Cesium.Matrix3.multiplyByVector(rotation, cameraOffets, new Cesium.Cartesian3());
    const cameraPosition = Cesium.Cartesian3.add(dronePosition, offset, new Cesium.Cartesian3());
    this.viewer.camera.setView({
      destination: cameraPosition,
      orientation: {
        heading: heading,
        pitch: Cesium.Math.toRadians(-30.0),
        roll: 0
      },
    });
  },

  // --- 优化：清理所有图层 (包含目标分析) ---
  clearAllLayers() {
    if (this.viewer.entities) {
      this.viewer.entities.removeAll();
    }
    this.mbfxEntities = [];
    this.processedWaypointIndices = [];
    this.missionWaypoints = [];
  },

  // --- 优化：仅清理无人机相关 (保留底图等) ---
  clearWrjLayers() {
    // 1. 清理目标分析实体
    this.mbfxEntities.forEach(entity => {
      if(this.viewer.entities.contains(entity)) {
        this.viewer.entities.remove(entity);
      }
    });
    this.mbfxEntities = [];
    this.processedWaypointIndices = [];

    // 2. 清理无人机实体
    if (this.droneEntity) {
      this.viewer.entities.remove(this.droneEntity);
      this.droneEntity = null;
    }
    
    // 3. 清理跟踪
    this.viewer.trackedEntity = undefined;
    
    // 4. 清理监听器
    if (this.firstPersonListener) {
      this.viewer.scene.postUpdate.removeEventListener(this.firstPersonListener);
      this.firstPersonListener = null;
    }
  }
}
