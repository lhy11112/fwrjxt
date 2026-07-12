export default {
  viewer: {},
  wrjData: [],
  sj: '',
  firstPersonListener: null,
  circleEntity: null,
  timePoints: [],
  droneEntity: null,
  wrjJbxx: null,
  path: [],

  // --- 目标分析状态管理 ---
  missionWaypoints: [],
  mbfxConfig: {},
  waypointTimeMap: new Map(),
  mbfxGroupMap: new Map(),
  isMbfxInitialized: false,
  lastProcessedTime: null,
  isSeeking: false,

  // 【新增】存储点击事件处理器
  clickHandler: null,
  checkBoxOptions:[],

  init(viewer) {
    this.viewer = viewer;
    this.resetMbfxState();
    this.initClickHandler();
  },
  initClickHandler() {
    if (!this.viewer || !this.viewer.scene) return;
    if (this.clickHandler) {
      return;
    }
    this.clickHandler = new Cesium.ScreenSpaceEventHandler(this.viewer.scene.canvas);
    this.clickHandler.setInputAction((click) => {
      const pickedObject = this.viewer.scene.pick(click.position);
      if (Cesium.defined(pickedObject) && pickedObject.id) {
        const entityId = pickedObject.id._id;
        const entity = this.viewer.entities.getById(entityId);
        console.log(entity);
        
        if (entity && entity.properties) {
          const type = entity.properties.getValue(this.viewer.clock.currentTime)?.type;
          if (type === 'mbfx_target') {
            const data = entity.properties.getValue(this.viewer.clock.currentTime)?.data;
            window.eventBus.emit("mbfxTargetClick", { targetData: data });
          }
        }else if(entity && entity._wrjDataItem){
          window.eventBus.emit("wrjTargetClick", { targetData: entity._wrjDataItem });
        }
      }
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK);
  },

  resetMbfxState() {
    this.missionWaypoints = [];
    this.mbfxConfig = {};
    this.waypointTimeMap.clear();
    this.clearMbfxEntities();
    this.mbfxGroupMap.clear();
    this.isMbfxInitialized = false;
    this.lastProcessedTime = null;
    this.isSeeking = false;
  },

  clearMbfxEntities() {
    if (!this.viewer || !this.viewer.entities) return;
    const count = this.mbfxGroupMap.size;
    this.mbfxGroupMap.forEach((group) => {
      if (group && Array.isArray(group.entities)) {
        group.entities.forEach(entity => {
          try {
            if (entity && this.viewer.entities.contains(entity)) {
              this.viewer.entities.remove(entity);
            }
          } catch (e) {
            console.warn('移除实体失败', e);
          }
        });
      }
    });
    this.mbfxGroupMap.clear();
  },

  setMissionConfig(waypoints, config) {
    this.missionWaypoints = waypoints || [];
    this.mbfxConfig = config || { radius: 30 };
  },

  generateDronePath(data) {
    this.wrjData = data;
    this.timePoints = [];
    const path = [];
    for (let i = 0; i < data.length; i++) {
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
    this.path = path;
    return path;
  },

  // --- 【核心修改】相机更新逻辑提取为独立方法 ---
  // 这样可以确保 removeEventListener 时引用一致
  _updateFirstPersonCamera() {
    if (!this.droneEntity || !this.viewer) return;
    const position = this.droneEntity.position.getValue(this.viewer.clock.currentTime);
    const orientation = this.droneEntity.orientation.getValue(this.viewer.clock.currentTime);
    if (Cesium.defined(position) && Cesium.defined(orientation)) {
      const transform = Cesium.Matrix4.fromRotationTranslation(
        Cesium.Matrix3.fromQuaternion(orientation, new Cesium.Matrix3()),
        position
      );
      const offset = new Cesium.Cartesian3(-1, 0, 0);
      this.viewer.camera.lookAtTransform(transform, offset);
    }
  },

  _updateFirstPersonCamera3() {
    if (!this.droneEntity || !this.viewer) return;
    const position = this.droneEntity.position.getValue(this.viewer.clock.currentTime);
    const orientation = this.droneEntity.orientation.getValue(this.viewer.clock.currentTime);
    if (Cesium.defined(position) && Cesium.defined(orientation)) {
      const transform = Cesium.Matrix4.fromRotationTranslation(
        Cesium.Matrix3.fromQuaternion(orientation, new Cesium.Matrix3()),
        position
      );
      const offset = new Cesium.Cartesian3(-100.0, 0, 20.0);
      this.viewer.camera.lookAtTransform(transform, offset);
    }
  },

  // --- 【核心修改】优化 setSj 方法 ---
  setSj(data) {
    if (!this.viewer || !this.viewer.scene) return;
    
    this.sj = data;

    // 1. 清除旧的监听器 (确保引用一致)
    if (this.firstPersonListener) {
      this.viewer.scene.postUpdate.removeEventListener(this.firstPersonListener);
      this.firstPersonListener = null;
    }

    // 2. 【关键】清除 trackedEntity，防止与 postUpdate 冲突
    // 除非是 sj=3 (第三人称跟随)，否则必须取消 Cesium 默认的实体跟踪
    if (this.sj != 3) {
      this.viewer.trackedEntity = undefined;
    }

    // 3. 根据模式设置新逻辑
    if (this.sj == 1) {
      // 第一人称：使用 postUpdate 强制更新相机
      this.firstPersonListener = this._updateFirstPersonCamera.bind(this);
      this.viewer.scene.postUpdate.addEventListener(this.firstPersonListener);
    } else if (this.sj == 3) {
      // 第三人称：使用 Cesium 原生 trackedEntity + postUpdate 微调
      this.viewer.trackedEntity = this.droneEntity;
      this.firstPersonListener = this._updateFirstPersonCamera3.bind(this);
      this.viewer.scene.postUpdate.addEventListener(this.firstPersonListener);
    } else {
      // 其他模式 (如 sj=2 自由视角)：不绑定监听，跟踪实体
      this.viewer.trackedEntity = this.droneEntity;
    }
    
    console.log(`视角模式已切换为：${this.sj}, trackedEntity:`, this.viewer.trackedEntity);
  },

  createDroneModel(data) {
    if (this.droneEntity) {
      this.viewer.entities.remove(this.droneEntity);
      this.droneEntity = null;
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

  DronePlaybackController(path, droneEntity, wrjJbxx) {
    this.path = path;
    this.droneEntity = droneEntity;
    this.wrjJbxx = wrjJbxx;
    this.isPlaying = true;
    this.playbackSpeed = 5.0;

    this.forceClearMbfx();
    this.calculateWaypointTimes();

    if (this.path.length <= 1) {
      if (wrjJbxx) {
        this.circleEntity = this.createCircle(this.droneEntity, wrjJbxx);
      }
      // 【修改】这里不要强制设置 trackedEntity，交给 setSj 管理
      this.viewer.trackedEntity = this.droneEntity; 
      return;
    }

    this.positionProperty = new Cesium.SampledPositionProperty();
    this.propertiesProperty = new Cesium.SampledProperty(Number);

    this.path.forEach(point => {
      this.positionProperty.addSample(point.time, point.position, point.properties);
      this.propertiesProperty.addSample(point.time, point.properties);
    });

    this.droneEntity.properties = this.propertiesProperty;
    this.droneEntity.position = this.positionProperty;
    this.droneEntity.orientation = new Cesium.VelocityOrientationProperty(this.positionProperty);

    this.createPathLine(wrjJbxx);

    this.viewer.clock.startTime = this.path[0].time.clone();
    this.viewer.clock.stopTime = this.path[this.path.length - 1].time.clone();
    this.viewer.clock.currentTime = this.path[0].time.clone();

    this.viewer.timeline.zoomTo(this.path[0].time.clone(), this.path[this.path.length - 1].time.clone());
    this.viewer.timeline.container.style.display = 'block';
    this.viewer.clock.clockRange = Cesium.ClockRange.LOOP_STOP;
    this.viewer.clock.multiplier = this.playbackSpeed;

    // 【修改】这里不要强制设置 trackedEntity，交给 setSj 管理
    this.viewer.trackedEntity = this.droneEntity;
    
    let that = this;
    // 添加扇形视野... (保持原有逻辑)
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

    if (wrjJbxx) {
      this.circleEntity = this.createCircle(this.droneEntity, wrjJbxx);
    }

    this.bindClockEvents();
  },

  forceClearMbfx() {
    window.eventBus.emit("mbtjxxEvent", 'clear');
    this.isMbfxInitialized = false;
    this.clearMbfxEntities();
    this.waypointTimeMap.clear();
    this.mbfxGroupMap.clear();
    this.lastProcessedTime = null;
    this.isSeeking = false;
  },

  calculateWaypointTimes() {
    if (!this.missionWaypoints || !this.path) return;
    this.missionWaypoints.forEach((wp, index) => {
      let minDist = Infinity;
      let closestTime = null;
      let closestIndex = -1;
      for (let i = 0; i < this.path.length; i++) {
        const p = this.path[i];
        const cartographic = Cesium.Cartographic.fromCartesian(p.position);
        const lon = Cesium.Math.toDegrees(cartographic.longitude);
        const lat = Cesium.Math.toDegrees(cartographic.latitude);
        const dist = turf.distance([lon, lat], [wp.longitude, wp.latitude]);
        if (dist < minDist) {
          minDist = dist;
          closestTime = p.time;
          closestIndex = i;
        }
      }
      if (minDist < 0.0005 && closestTime) {
        this.waypointTimeMap.set(index, {
          time: closestTime,
          wp: wp,
          index: closestIndex
        });
      }
    });
    this.isMbfxInitialized = true;
  },

  createMbfxVisualization(data, jd, wd, height, index) {
    if (!data.zymbxx || !Array.isArray(data.zymbxx)) return;
    if (!this.isMbfxInitialized) return;
    if (this.mbfxGroupMap.has(index)) return;

    const centerPos = Cesium.Cartesian3.fromDegrees(Number(jd), Number(wd), Number(height));
    const jl = this.mbfxConfig.radius || 30;
    const radius = jl * 1000;
    const color = Cesium.Color.fromCssColorString('#FF0000').withAlpha(0.3);
    const entities = [];

    const circleEntity = this.viewer.entities.add({
      position: centerPos,
      ellipse: {
        semiMinorAxis: radius,
        semiMajorAxis: radius,
        material: color,
        outline: true,
        outlineColor: Cesium.Color.BLACK,
        height: 0.9
      }
    });
    entities.push(circleEntity);

    const centerMarker = this.viewer.entities.add({
      position: centerPos,
      point: {
        pixelSize: 10,
        color: Cesium.Color.RED,
        outlineColor: Cesium.Color.WHITE,
        outlineWidth: 2
      }
    });
    entities.push(centerMarker);

    data.zymbxx.forEach((item, itemIndex) => {
      const itemLat = Number(item.wd || item.WD);
      const itemLng = Number(item.jd || item.JD);
      const itemName = item.mc || item.dmmc || '未知目标';
      if (!itemLat || !itemLng) return;

      const targetPos = Cesium.Cartesian3.fromDegrees(itemLng, itemLat, 0);
      let iconUrl = '/static/map_img/重要目标.png';
      if (itemName.indexOf('医院') !== -1) iconUrl = '/static/map_img/医院.png';
      else if (itemName.indexOf('学校') !== -1) iconUrl = '/static/map_img/高等院校.png';
      else if (itemName.indexOf('加油站') !== -1) iconUrl = '/static/map_img/加油站.png';

      const targetId = `mbfx_target_${index}_${itemIndex}_${itemLng}_${itemLat}`;
      const marker = this.viewer.entities.add({
        id: targetId,
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
        },
        properties: {
          type: 'mbfx_target',
          data: item,
          waypointIndex: index
        },
      });
      entities.push(marker);

      const distance = Cesium.Cartesian3.distance(centerPos, targetPos);
      const distanceText = (distance).toFixed(0) + 'm';
      const polyline = this.viewer.entities.add({
        polyline: {
          positions: [centerPos, targetPos],
          width: 2,
          material: new Cesium.PolylineDashMaterialProperty({ color: Cesium.Color.RED })
        }
      });
      entities.push(polyline);

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
          pixelOffset: new Cesium.Cartesian2(0, 0),
          disableDepthTestDistance: Number.POSITIVE_INFINITY
        }
      });
      entities.push(label);
    });

    this.mbfxGroupMap.set(index, { entities: entities, apiCalled: true });
    window.eventBus.emit("mbtjxxEvent", data);
  },

  updateMbfxVisibility(currentTime) {
    if (!this.isMbfxInitialized) return;
    if (this.lastProcessedTime && Cesium.JulianDate.lessThan(currentTime, this.lastProcessedTime)) {
      const timeDiff = Cesium.JulianDate.secondsDifference(this.lastProcessedTime, currentTime);
      if (timeDiff > 10) {
        this.forceClearMbfx();
        this.calculateWaypointTimes();
      }
    }
    this.lastProcessedTime = currentTime.clone();
    this.waypointTimeMap.forEach((info, index) => {
      const group = this.mbfxGroupMap.get(index);
      const waypointTime = info.time;
      const isReached = Cesium.JulianDate.greaterThanOrEquals(currentTime, waypointTime);
      if (isReached) {
        if (!group) {
          const pathPoint = this.path[info.index];
          const cartographic = Cesium.Cartographic.fromCartesian(pathPoint.position);
          const height = cartographic.height;
          this.requestMbfxData(info.wp.longitude, info.wp.latitude, index, height);
        } else {
          group.entities.forEach(entity => {
            if (entity) entity.show = true;
          });
        }
      } else {
        if (group) {
          group.entities.forEach(entity => {
            if (entity) entity.show = false;
          });
        }
      }
    });
  },

  requestMbfxData(jd, wd, index, height) {
    if (!this.isMbfxInitialized) return;
    if (this.mbfxGroupMap.has(index)) return;
    const jl = this.mbfxConfig.radius || 30;
    window.API.zbmb.getWrjZymbByJwdAndJlS({
      jd: jd,
      wd: wd,
      jl: jl,
      type: '执勤目标,民生目标,友邻信息'
    }).then((res) => {
      if (!this.isMbfxInitialized) return;
      if (res.code == 200 && res.result) {
        this.createMbfxVisualization(res.result, jd, wd, height, index);
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
        text: `${wrjJbxx.brand  || '未知'}-${wrjJbxx.model || '未知'}(${wrjJbxx.serialNumber || '未知'})`,
        show: true,
        font: '14px sans-serif',
        pixelOffset: new window.Cesium.Cartesian2(0, -60)
      },
    });
  },

  togglePlayPause(data) {
    this.isPlaying = data;
    this.viewer.clock.shouldAnimate = this.isPlaying;
    if (this.isPlaying) {
      const timeDiff = Cesium.JulianDate.secondsDifference(
        this.viewer.clock.currentTime,
        this.viewer.clock.startTime
      );
      if (Math.abs(timeDiff) < 1.0) {
        this.forceClearMbfx();
        this.calculateWaypointTimes();
      }
    }
  },

  updateSpeed() {
    this.viewer.clock.multiplier = this.playbackSpeed;
  },

  seekToProgress(progress) {
    const totalSeconds = Cesium.JulianDate.secondsDifference(
      this.viewer.clock.stopTime,
      this.viewer.clock.startTime
    );
    const targetSeconds = totalSeconds * (progress / 100);
    const targetTime = Cesium.JulianDate.addSeconds(
      this.viewer.clock.startTime,
      targetSeconds,
      new Cesium.JulianDate()
    );
    this.isSeeking = true;
    this.viewer.clock.currentTime = targetTime;
    setTimeout(() => {
      this.isSeeking = false;
    }, 500);
  },

  bindClockEvents() {
    const that = this;
    if (this.clockTickListener) {
      this.viewer.clock.onTick.removeEventListener(this.clockTickListener);
    }
    this.clockTickListener = (clock) => {
      const position = that.droneEntity.position.getValue(clock.currentTime, new Cesium.Cartesian3());
      if (!position) return;
      const cartographic = Cesium.Cartographic.fromCartesian(position);
      const longitude = Cesium.Math.toDegrees(cartographic.longitude).toFixed(6);
      const latitude = Cesium.Math.toDegrees(cartographic.latitude).toFixed(6);
      const altitude = cartographic.height;
      that.updateMbfxVisibility(clock.currentTime);
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
      }
      for (let i = 0; i < that.timePoints.length - 1; i++) {
        if (Cesium.JulianDate.greaterThanOrEquals(clock.currentTime, that.timePoints[i]) &&
          Cesium.JulianDate.lessThanOrEquals(clock.currentTime, that.timePoints[i + 1])) {
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
    };
    that.viewer.clock.onTick.addEventListener(this.clockTickListener);
  },

  createPathLine(wrjJbxx) {
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

  clearAllLayers() {
    this.forceClearMbfx();
    if (this.viewer.entities) {
      this.viewer.entities.removeAll();
    }
    // 【修改】清理监听器和 trackedEntity
    if (this.firstPersonListener) {
      this.viewer.scene.postUpdate.removeEventListener(this.firstPersonListener);
      this.firstPersonListener = null;
    }
    if (this.viewer) {
      this.viewer.trackedEntity = undefined;
    }
    // if (this.clickHandler) {
    //   this.clickHandler.destroy();
    //   this.clickHandler = null;
    // }
  },

  // 【修改】增强清理方法，确保视角重置
  clearWrjLayers() {
    this.forceClearMbfx();
    if (this.droneEntity) {
      this.viewer.entities.remove(this.droneEntity);
      this.droneEntity = null;
    }
    
    // 【关键】清除跟踪实体，防止影响下一次推演
    if (this.viewer) {
      this.viewer.trackedEntity = undefined;
    }

    // 【关键】清除视角监听器
    if (this.firstPersonListener) {
      this.viewer.scene.postUpdate.removeEventListener(this.firstPersonListener);
      this.firstPersonListener = null;
    }

    if (this.clockTickListener) {
      this.viewer.clock.onTick.removeEventListener(this.clockTickListener);
      this.clockTickListener = null;
    }
    
    // 重置 sj 状态
    this.sj = '';
  }
}
