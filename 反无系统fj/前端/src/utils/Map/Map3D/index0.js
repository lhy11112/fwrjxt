import { cfg3D } from '../config';
import measure3D from './measure3D';
import imageLayer from "./layer/imageLayer";
import s3mLayer from "./layer/s3mLayer";
import sceneLayer from "./layer/sceneLayer";
import vectorTileLayer from "./layer/vectorTileLayer";
import sightLine from './sightLine';
import viewShed3D from "./viewShed3D";
import viewDome from "./viewDome";
import slopeAndAspect from "./slopeAndAspect";
import floodModel from "./floodModel";
import floodTerrain from "./floodTerrain";
import event from './event';
import coord from "./coord";
import wrjFly from "./wrjFly";
import utcHider from '../../cesiumUTCHider';
let popup2=null;

export default {
    viewer: null,
    scene: null,
    measure3D,
    imageLayer,
    s3mLayer,
    sceneLayer,
    vectorTileLayer,
    sightLine,
    viewShed3D,
    viewDome,
    slopeAndAspect,
    floodModel,
    floodTerrain,
    event,
    coord,
    wrjFly,
    init(domId) {
        const that = this;

        window.viewer = new Cesium.Viewer(domId, Object.assign(cfg3D.viewerOptions,{
            //创建地形服务提供者的实例，url为SuperMap iServer发布的TIN地形服务 SuperMap3D.SuperMapTerrainProvider
            // terrainProvider : new Cesium.CesiumTerrainProvider({
            //     url : window.config.PUBLIC_DILI_SWDX + "/iserver/services/3D-local3DCache-SiChuanWGS84SiChuanDEM/rest/realspace/datas/%E5%9B%9B%E5%B7%9DWGS84@SiChuanDEM",
            //     isSct : true,//地形服务源自SuperMap iServer发布时需设置isSct为true
            //     invisibility:true
            // }),
        }));
        // 加载3DTiles数据
       
        this.viewer = window.viewer;

        this.scene = this.viewer.scene;
        const credit = this.viewer.scene.frameState.creditDisplay;
        credit && credit.container.removeChild(credit._cesiumCreditContainer);
        credit && credit.container.removeChild(credit._expandLink);
        // 隐藏地球
        // that.viewer.scene.globe.show = true;
        // 关闭天空盒, 否则会显示天空颜色
        that.viewer.scene.skyBox.show = false;
        // 关闭大气
        that.scene.skyAtmosphere.show = false;
        // 抗锯齿`
        that.scene.fxaa = true;
        that.scene.postProcessStages.fxaa.enabled = true;
        // 隐藏月亮
        that.scene.moon.show = false;
        that.setSceneColor("#000");
        // 开启深度检测
        that.scene.globe.depthTestAgainstTerrain = true; // 开启深度检测

        that.measure3D.init(that.viewer);
        that.imageLayer.init(that.viewer);
        that.s3mLayer.init(that.viewer);
        that.sceneLayer.init(that.viewer);
        that.vectorTileLayer.init(that.vectorTileLayer);
        this.sightLine.init(that.scene, that.viewer);
        this.viewShed3D.init(that.viewer, that.scene);
        this.viewDome.init(that.viewer, that.scene);
        this.slopeAndAspect.init(that.viewer);
        this.floodModel.init(that.viewer);
        this.floodTerrain.init(that.viewer);
        that.event.init(that.viewer);
        that.coord.init(that.viewer, that.scene);
        that.wrjFly.init(that.viewer)
        console.log(document.getElementById('test'));
        popup2 = new Popup({
            viewer: that.viewer,
            element: document.getElementById('test'),
            pixelOffset: new Cesium.Cartesian2(0, 6),
            // scaleByDistance: new Cesium.NearFarScalar(1000, 1, 10000, 0.2),
            // distanceDisplayCondition: new Cesium.DistanceDisplayCondition(0, 11000),
        });
        // 添加鼠标右键点击事件
        that.viewer.screenSpaceEventHandler.setInputAction((movement) => {
            var worldPosition = that.viewer.scene.pickPosition(movement.position);
            popup2.setPosition(worldPosition);
            popup2.show()
        }, window.Cesium.ScreenSpaceEventType.RIGHT_CLICK);
        

        // 点击指北针复原到初始方向
        // document.getElementsByClassName('sm-compass-outer-ring')[0].addEventListener('click', function() {
        //     console.log('xxss');
        //     that.updateNorthArrow(0);
        // });
        
          // 加载 3D Tileset
        // const tileset = await Cesium.Cesium3DTileset.fromUrl(window.config.VUE_APP_API_FRONT_BASE_URL+'/3dtiles/tileset.json', {
        //     maximumScreenSpaceError: 16,
        //     maximumNumberOfLoadedTiles: 1000,
        //     skipLevelOfDetail: true,
        //     baseScreenSpaceError: 1024,
        //     skipScreenSpaceErrorFactor: 16,
        //     skipLevels: 1,
        //     immediatelyLoadDesiredLevelOfDetail: false,
        //     loadSiblings: false,
        //     cullWithChildrenBounds: true
        //   });
        if(!window.config.VUE_APP_SUPERMAP_BASE_FLAG){
            // 加载3D Tiles模型
          const tileset = new Cesium.Cesium3DTileset({
            url: window.config.VUE_APP_API_FRONT_BASE_URL+'/3dtiles/tileset.json',  // 确保URL正确
            maximumScreenSpaceError: 16,
            loadOptions: {
                minimumLOD: 0,
                maximumLOD: 3
            }
          });
  
          // 添加到场景
          this.viewer.scene.primitives.add(tileset);
          
          // 调整相机视角（确保能看到模型）
            tileset.readyPromise.then(() => {
                this.viewer.zoomTo(tileset);
                var heightOffset = 4400.0;
                var boundingSphere = tileset.boundingSphere;
                var cartographic = Cesium.Cartographic.fromCartesian(boundingSphere.center);
                var surface = Cesium.Cartesian3.fromRadians(cartographic.longitude, cartographic.latitude, 0.0);
                var offset = Cesium.Cartesian3.fromRadians(cartographic.longitude, cartographic.latitude, heightOffset);
                var translation = Cesium.Cartesian3.subtract(offset, surface, new Cesium.Cartesian3());
                tileset.modelMatrix = Cesium.Matrix4.fromTranslation(translation);
                
                // that.viewer.scene.camera.setView({
                //     destination: new Cesium.Cartesian3.fromRadians(4.172808574034371 * 0.5, 0.9309333611670909 * 0.5, 2000),
                //     orientation: {
                //         heading: 0,
                //         roll: 0,
                //     },
                // });
            });

        }
          
        // 2. 【核心步骤】汉化时间轴
        // Cesium 默认的时间轴标签是英文的，我们需要重写 makeLabel 方法
        this.viewer.timeline.makeLabel = function(tickDate, isCenter) {
            // 将 Cesium 的 JulianDate 转换为 JavaScript 的 Date 对象
            const gregorianDate = Cesium.JulianDate.toGregorianDate(tickDate);
            const jsDate = new Date(
                gregorianDate.year,
                gregorianDate.month - 1, // JS 月份从 0 开始，Cesium 从 1 开始
                gregorianDate.day,
                gregorianDate.hour,
                gregorianDate.minute,
                gregorianDate.second
            );

            // 使用 JavaScript 原生的国际化 API 格式化时间
            // 格式示例：2023/10/27 14:30:05
            const options = {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit',
                hour12: false // 使用 24 小时制
            };

            // 返回中文字符串
            return jsDate.toLocaleString('zh-CN', options);
        };

        // 强制刷新时间轴视图以应用更改
        // this.viewer.timeline.updateView();
        // 5. 设置时间轴初始时间为当前时间，并让时间开始流动（演示用）
        const now = Cesium.JulianDate.now();
        // 第三个参数 new Cesium.JulianDate() 用于存储结果，避免创建过多临时对象
        const timePlus8Hours = Cesium.JulianDate.addSeconds(now, 8 * 3600, new Cesium.JulianDate());

        this.viewer.clock.currentTime = timePlus8Hours;
        this.viewer.clock.shouldAnimate = true; // 让时间轴动起来
        // this.viewer.clock.multiplier = 60; // 时间流速加快 60 倍，方便观察时间轴变化
    
        this.translateAnimationWidget();
        
    },

    // ========== 汉化动画仪表 (SVG 结构) ==========
    translateAnimationWidget() {
        setTimeout(() => {
            const animationContainer = this.viewer.animation?.container;
            
            if (!animationContainer) {
                console.warn('⚠ 未找到动画容器');
                return;
            }
            
            console.log('✓ 找到动画容器，开始汉化...');
            
            // 实时格式化 SVG 内的时间显示
            const translateInterval = setInterval(() => {
                // 获取所有 SVG 文本元素 (根据你的 DOM 结构)
                const svgTexts = animationContainer.querySelectorAll('.cesium-animation-svgText tspan');
                
                svgTexts.forEach(tspan => {
                    const originalText = tspan.textContent;
                    
                    // 汉化日期格式 (如 "Feb 26 2026" → "2026 年 2 月 26 日")
                    if (/[A-Za-z]{3}\s+\d{1,2}\s+\d{4}/.test(originalText)) {
                        const dateObj = new Date(originalText);
                        if (!isNaN(dateObj.getTime())) {
                            tspan.textContent = dateObj.toLocaleString('zh-CN', {
                                year: 'numeric',
                                month: '2-digit',
                                day: '2-digit'
                            });
                        }
                    }
                    
                    // 汉化时间格式 (如 "09:16:24 UTC" → "09:16:24")
                    if (/\d{2}:\d{2}:\d{2}\s*UTC/.test(originalText)) {
                        tspan.textContent = originalText.replace(/\s*UTC/, '');
                    }
                    
                    // 3. 汉化速度倍数 (支持整数、小数、负数)
                    // 匹配：1X, 60X, 0.5X, -0.6X, -99X, 1.5X 等
                    // if (/^-?\d+(\.\d+)?X$/i.test(originalText)) {
                    //     tspan.textContent = originalText.replace(/X$/i, '倍');
                    // }

                });
                
            }, 200);
            
            // 保存定时器引用，方便后续清理
            this.viewer._animationTranslateInterval = translateInterval;

         }, 800);
           
    },
    
    // updateNorthArrow(heading) {
    //     var northArrow = document.getElementsByClassName('sm-compass-outer-ring')[0];
    //     northArrow.style.transform = 'rotate(' + (heading) + 'deg)';
    // },
    popupShow(){
        popup2.show()
    },
    popupClose(){
        popup2.close()
    },
    // 初始化场景事件
    initSceneEvent(eventType, result) {
        this.event.initSceneEvent(eventType, result)
    },
    // 设置场景背景颜色
    setSceneColor(color) {
        this.scene.backgroundColor = new Cesium.Color.fromCssColorString(color);
    },
    //设置场景视角
    setSCeneView(longitude,latitude){
        this.viewer.camera.setView({
            destination: Cesium.Cartesian3.fromDegrees(longitude, latitude, 1000.0),
            orientation: {
                heading: Cesium.Math.toRadians(0.0),
                pitch: Cesium.Math.toRadians(-90.0),
                roll: Cesium.Math.toRadians(0.0)
            },
        });
    },
    /**
     * 跳转给定视角
     */
    flyToPos(pos, orientation, duration) {
        if (orientation && orientation.pitch) {
            this.viewer.camera.flyTo({
                destination: Cesium.Cartesian3.fromDegrees(...pos),
                orientation: orientation,
                duration: duration
            });
        } else {
            this.viewer.camera.flyTo({
                destination: Cesium.Cartesian3.fromDegrees(...pos),
                duration: duration
            });
        }
    },
    getRealitivePoint(A, B, proportion = 0.25) {
        const CartesianA = Cesium.Cartesian3.fromDegrees(A[0], A[1]);
        const CartesianB = Cesium.Cartesian3.fromDegrees(B[0], B[1]);
        const AtoB = Cesium.Cartesian3.subtract(CartesianB, CartesianA, new Cesium.Cartesian3());
        const CartesianProportion = new Cesium.Cartesian3(AtoB.x * proportion, AtoB.y * proportion, AtoB.z * proportion);
        const CartesianC = Cesium.Cartesian3.add(CartesianProportion, CartesianA, new Cesium.Cartesian3());
        // 笛卡尔坐标转经纬度
        const position = Cesium.Ellipsoid.WGS84.cartesianToCartographic(
            new Cesium.Cartesian3(CartesianC.x, CartesianC.y, CartesianC.z)
        )
        const lon = Cesium.Math.toDegrees(position.longitude);
        const lat = Cesium.Math.toDegrees(position.latitude);
        const alt = position.height;
        return [lon, lat, alt];
    }
    // getHeightByLonLat(posArr) {
    //     const that = this;
    //     return new Promise((resolve, reject) => {
    //         const lonlatArr =  [];
    //         posArr.forEach((item => {
    //             lonlatArr.push(new Cesium.Cartographic(Cesium.Math.toRadians(item.lon), Cesium.Math.toRadians(item.lat)))
    //         }));
    //         const resArr = [];
    //         // 模型上取点
    //         const promise1 = that.scene.sampleHeightMostDetailed(lonlatArr);
    //         promise1.then(data => {
    //             data.forEach((obj, index) => {
    //                 if(obj && obj.height) {
    //                     resArr[index] = Cesium.Cartesian3.fromDegrees(posArr[index], posArr[lat], obj.height)
    //                 } else {
    //                     resArr[index] = [];
    //                 }
    //             })
    //         })
    //         // 地形上取点
    //         const promise2 = Cesium.sampleTerrainMostDetailed(that.viewer.terrainProvider, lonlatArr);
    //         promise2.then(data => {
    //             data.forEach((obj, index) => {
    //                 if (resArr[index] && resArr[index].height) { // 判断是否已在模型上取到点了

    //                 } else {
    //                     resArr[index] = Cesium.Cartesian3.fromDegrees(posArr[index], posArr[lat], obj.height)
    //                 }
    //             })
    //             resolve(resArr);
    //         })
    //     })
    // }
}