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
import { useCounterStoreMethods } from "@/store/modules/jwdu";

let popup2 = null;
// 【新增】存储 MutationObserver 引用
let animationObserver = null;
// 监听地球拖动/相机移动事件
let moveEndHandler = null; // 用于存储事件句柄，方便销毁
let isDragging = false;    // 标记是否正在拖动
const countMessage = useCounterStoreMethods();

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

        window.viewer = new Cesium.Viewer(domId, Object.assign(cfg3D.viewerOptions, {
            // terrainProvider : new Cesium.CesiumTerrainProvider({
            //     url : window.config.PUBLIC_DILI_SWDX + "/iserver/services/3D-local3DCache-SiChuanWGS84SiChuanDEM/rest/realspace/datas/%E5%9B%9B%E5%B7%9DWGS84@SiChuanDEM",
            //     isSct : true,
            //     invisibility:true
            // }),
        }));
       
        this.viewer = window.viewer;
        this.scene = this.viewer.scene;
        
        // 移除版权信息
        const credit = this.viewer.scene.frameState.creditDisplay;
        credit && credit.container.removeChild(credit._cesiumCreditContainer);
        credit && credit.container.removeChild(credit._expandLink);
        
        // 场景配置
        that.viewer.scene.skyBox.show = false;
        that.scene.skyAtmosphere.show = false;
        that.scene.fxaa = true;
        that.scene.postProcessStages.fxaa.enabled = true;
        that.scene.moon.show = false;
        that.setSceneColor("#000");
        that.scene.globe.depthTestAgainstTerrain = true;

        // 初始化各模块
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
        that.wrjFly.init(that.viewer);

        // Popup 初始化
        popup2 = new Popup({
            viewer: that.viewer,
            element: document.getElementById('test'),
            pixelOffset: new Cesium.Cartesian2(0, 6),
        });
        
        // 右键点击事件
        that.viewer.screenSpaceEventHandler.setInputAction((movement) => {
            var worldPosition = that.viewer.scene.pickPosition(movement.position);
            popup2.setPosition(worldPosition);
            popup2.show()
        }, window.Cesium.ScreenSpaceEventType.RIGHT_CLICK);

        // 加载 3D Tiles 模型
        if(!window.config.VUE_APP_SUPERMAP_BASE_FLAG){
            const tileset = new Cesium.Cesium3DTileset({
                url: window.config.VUE_APP_API_FRONT_BASE_URL+'/3dtiles/tileset.json',
                maximumScreenSpaceError: 16,
                loadOptions: {
                    minimumLOD: 0,
                    maximumLOD: 3
                }
            });

            this.viewer.scene.primitives.add(tileset);
            
            tileset.readyPromise.then(() => {
                this.viewer.zoomTo(tileset);
                var heightOffset = 4400.0;
                var boundingSphere = tileset.boundingSphere;
                var cartographic = Cesium.Cartographic.fromCartesian(boundingSphere.center);
                var surface = Cesium.Cartesian3.fromRadians(cartographic.longitude, cartographic.latitude, 0.0);
                var offset = Cesium.Cartesian3.fromRadians(cartographic.longitude, cartographic.latitude, heightOffset);
                var translation = Cesium.Cartesian3.subtract(offset, surface, new Cesium.Cartesian3());
                tileset.modelMatrix = Cesium.Matrix4.fromTranslation(translation);
            });
        }
          
        // 汉化时间轴
        this.viewer.timeline.makeLabel = function(tickDate, isCenter) {
            const gregorianDate = Cesium.JulianDate.toGregorianDate(tickDate);
            const jsDate = new Date(
                gregorianDate.year,
                gregorianDate.month - 1,
                gregorianDate.day,
                gregorianDate.hour,
                gregorianDate.minute,
                gregorianDate.second
            );

            const options = {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit',
                hour12: false
            };

            return jsDate.toLocaleString('zh-CN', options);
        };
    
        // 设置时钟
        // const now = Cesium.JulianDate.now();
        // const timePlus8Hours = Cesium.JulianDate.addSeconds(now, 8 * 3600, new Cesium.JulianDate());  //Cesium.JulianDate.addSeconds(now, -8 * 3600, new Cesium.JulianDate())
        // this.viewer.clock.currentTime = timePlus8Hours;
        // this.viewer.clock.shouldAnimate = true;
        
         // 【修改】设置时间轴范围为当前时间前 1 小时至后 8 小时
        this.setTimelineToDefaultRange();
        // 【修改】调用汉化动画仪表（使用新方案）
        this.translateAnimationWidget();

        
        // 1. 监听鼠标按下（开始拖动）
        // const mouseDownHandler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas);
        // mouseDownHandler.setInputAction(() => {
        //     isDragging = true;
        // }, Cesium.ScreenSpaceEventType.LEFT_DOWN);

        // // 2. 监听鼠标松开（结束拖动）
        // mouseDownHandler.setInputAction(() => {
        //     isDragging = false;
        // }, Cesium.ScreenSpaceEventType.LEFT_UP);

        // 3. 监听相机移动结束（拖动/缩放/旋转后触发）
        moveEndHandler = this.viewer.camera.moveEnd.addEventListener(() => {
            
            // console.log(isDragging);
            
            // 仅在拖动结束后获取中心点（避免缩放/旋转时触发）
            // if (!isDragging) return;
            
            const center = this.getMapCenter(this.viewer);
            if (center) {
                // console.log('地图中心点坐标：', center);
                // 这里可以将坐标渲染到页面DOM中
                // document.getElementById('center-coord').innerText = `经度：${center.lon}，纬度：${center.lat}`;
                // window.TOOL.data.set("CENTER_POINT",{jd: that.map.getCenter().lng,wd: that.map.getCenter().lat})
                    //console.log({jd: that.map.getCenter().lng,wd: that.map.getCenter().lat});
                    countMessage.getMapCenterPoint({jd: center.lon,wd: center.lat})
            }
        });
    },
    // 核心函数：获取地图视口中心点的经纬度
 getMapCenter(viewer) {
    
    if (!viewer || !viewer.scene) return null;

    // 1. 获取画布中心点的屏幕坐标
    const canvas = viewer.scene.canvas;
    const centerX = canvas.clientWidth / 2;
    const centerY = canvas.clientHeight / 2;

    // 2. 将屏幕坐标转换为笛卡尔坐标（三维坐标）
    // pickPosition：拾取地形/模型表面；camera.pickEllipsoid：拾取椭球面（无地形时用）
    let cartesian = viewer.scene.pickPosition(new Cesium.Cartesian2(centerX, centerY));
    console.log(cartesian);
    
    // 容错：如果拾取不到地形，使用椭球面拾取
    if (!cartesian) {
        cartesian = viewer.camera.pickEllipsoid(
            new Cesium.Cartesian2(centerX, centerY),
            viewer.scene.globe.ellipsoid
        );
    }

    if (!cartesian) return null;

    // 3. 将笛卡尔坐标转换为经纬度（弧度转角度）
    const cartographic = Cesium.Cartographic.fromCartesian(cartesian);
    const longitude = Cesium.Math.toDegrees(cartographic.longitude).toFixed(8); // 经度
    const latitude = Cesium.Math.toDegrees(cartographic.latitude).toFixed(8);   // 纬度
    const height = cartographic.height.toFixed(2);                             // 高度（米）

    return {
        lon: parseFloat(longitude),
        lat: parseFloat(latitude),
        alt: parseFloat(height)
    };
},




// 4. （可选）实时监听相机移动（拖动过程中实时获取）
// viewer.camera.move.addEventListener(() => {
//     if (isDragging) {
//         const center = getMapCenter(viewer);
//         if (center) {
//             console.log('实时中心点：', center);
//         }
//     }
// });

// 销毁事件监听（页面卸载时调用，避免内存泄漏）
 destroyHandlers () {
    if (mouseDownHandler) {
        mouseDownHandler.destroy();
    }
    if (moveEndHandler) {
        viewer.camera.moveEnd.removeEventListener(moveEndHandler);
    }
},


    // ========== 【修改】设置时间轴默认范围：前 1 小时至后 8 小时 ==========
    setTimelineToDefaultRange(hoursBefore = 1, hoursAfter = 8) {
        const now = new Date(new Date().getTime() + 8 * 60 * 60 * 1000);
        console.log('xxxx',now);
        
        // 计算开始时间：当前时间前 N 小时
        const startTime = new Date(now.getTime() - hoursBefore * 60 * 60 * 1000);
        
        // 计算结束时间：当前时间后 N 小时
        const endTime = new Date(now.getTime() + hoursAfter * 60 * 60 * 1000);
        
        // 转换为 Cesium JulianDate
        const startJulian = Cesium.JulianDate.fromDate(startTime);
        const stopJulian = Cesium.JulianDate.fromDate(endTime);
        const currentJulian = Cesium.JulianDate.fromDate(now);
        
        // 设置时钟范围
        this.viewer.clock.startTime = startJulian;
        this.viewer.clock.stopTime = stopJulian;
        this.viewer.clock.currentTime = currentJulian;
        
        // 设置时钟行为
        this.viewer.clock.clockRange = Cesium.ClockRange.CLAMPED; // 到终点停止
        this.viewer.clock.clockStep = Cesium.ClockStep.SYSTEM_CLOCK_MULTIPLIER;
        this.viewer.clock.multiplier = 1; // 实时速度
        this.viewer.clock.shouldAnimate = true;
        
        // 更新时间轴视图
        setTimeout(() => {
            if (this.viewer.timeline) {
                // this.viewer.timeline.updateView();
                // 缩放时间轴到指定范围
                this.viewer.timeline.zoomTo(startJulian, stopJulian);
            }
        }, 500);
        
        // console.log('✓ 时间轴已设置为默认范围:', {
        //     start: startTime.toLocaleString('zh-CN'),
        //     current: now.toLocaleString('zh-CN'),
        //     end: endTime.toLocaleString('zh-CN'),
        //     totalHours: hoursBefore + hoursAfter
        // });
        
        return {
            start: startTime,
            current: now,
            end: endTime
        };
    },

    // ========== 【新增】设置时间轴为指定日期范围 ==========
    setTimelineToDateRange(startDate, endDate, currentTime = null) {
        // 参数格式支持：Date 对象 或 'YYYY-MM-DD' 字符串 或 'YYYY-MM-DD HH:mm:ss' 字符串
        const start = this.parseDate(startDate);
        const end = this.parseDate(endDate);
        const current = currentTime ? this.parseDate(currentTime) : new Date();
        
        if (!start || !end) {
            console.error('❌ 日期格式错误');
            return false;
        }
        
        const startTime = Cesium.JulianDate.fromDate(start);
        const stopTime = Cesium.JulianDate.fromDate(end);
        const currentTimeJulian = Cesium.JulianDate.fromDate(current);
        
        this.viewer.clock.startTime = startTime;
        this.viewer.clock.stopTime = stopTime;
        this.viewer.clock.currentTime = currentTimeJulian;
        this.viewer.clock.clockRange = Cesium.ClockRange.CLAMPED;
        this.viewer.clock.multiplier = 1;
        this.viewer.clock.shouldAnimate = true;
        
        setTimeout(() => {
            if (this.viewer.timeline) {
                this.viewer.timeline.updateView();
                this.viewer.timeline.zoomTo(startTime, stopTime);
            }
        }, 500);
        
        console.log('✓ 时间轴已设置为指定范围:', {
            start: start.toLocaleString('zh-CN'),
            end: end.toLocaleString('zh-CN'),
            current: current.toLocaleString('zh-CN')
        });
        
        return true;
    },
    
    // 【新增】解析日期字符串
    parseDate(dateStr) {
        if (dateStr instanceof Date) {
            return dateStr;
        }
        
        if (typeof dateStr === 'string') {
            // 支持格式：'YYYY-MM-DD' 或 'YYYY-MM-DD HH:mm:ss'
            const parts = dateStr.split(' ');
            const dateParts = parts[0].split('-');
            const timeParts = parts[1] ? parts[1].split(':') : [0, 0, 0];
            
            return new Date(
                parseInt(dateParts[0]),
                parseInt(dateParts[1]) - 1,
                parseInt(dateParts[2]),
                parseInt(timeParts[0] || 0),
                parseInt(timeParts[1] || 0),
                parseInt(timeParts[2] || 0)
            );
        }
        
        return null;
    },

    // ========== 【优化】汉化动画仪表 (无闪动方案) ==========
    translateAnimationWidget() {
        const that = this;
        
        // 1. 先添加 CSS 样式隐藏 UTC（防止闪动）
        this.addHideUTCCSS();
        
        setTimeout(() => {
            const animationContainer = this.viewer.animation?.container;
            
            if (!animationContainer) {
                console.warn('⚠ 未找到动画容器');
                return;
            }
            
            console.log('✓ 找到动画容器，开始汉化...');
            
            // 2. 使用 MutationObserver 监听 DOM 变化（替代定时器）
            this.setupMutationObserver(animationContainer);
            
            // 3. 立即处理一次
            this.hideUTCElements(animationContainer);
            
        }, 500);
    },
    
    // 【新增】添加 CSS 隐藏 UTC
    addHideUTCCSS() {
        const styleId = 'cesium-hide-utc-style';
        
        // 防止重复添加
        if (document.getElementById(styleId)) {
            return;
        }

        const style = document.createElement('style');
        style.id = styleId;
        style.textContent = `
            /* 隐藏 UTC 时间标签 */
            .cesium-animation-utcLabel {
                display: none !important;
            }
            
            /* 隐藏 SVG 中的 UTC 文本 */
            .cesium-animation-svgText tspan {
                /* 不直接隐藏，通过 JS 处理内容 */
            }
            
            /* 隐藏包含 UTC 字样的文本元素 */
            .cesium-animation-svgText[text*="UTC"],
            .cesium-animation-timeLabel[text*="UTC"] {
                display: none !important;
            }
            
            /* 优化动画控件布局 */
            .cesium-animation {
                bottom: 10px !important;
            }
        `;
        document.head.appendChild(style);
        
        console.log('✓ UTC 隐藏 CSS 已添加');
    },
    
    // 【新增】设置 MutationObserver 监听 DOM 变化
    setupMutationObserver(container) {
        const that = this;
        
        // 创建 Observer 实例
        animationObserver = new MutationObserver((mutations) => {
            mutations.forEach((mutation) => {
                if (mutation.type === 'childList' || mutation.type === 'attributes') {
                    // DOM 变化时处理 UTC 隐藏
                    that.hideUTCElements(container);
                }
            });
        });
        
        // 配置观察选项
        const config = {
            childList: true,          // 监听子节点变化
            attributes: true,         // 监听属性变化
            characterData: true,      // 监听文本内容变化
            subtree: true             // 监听后代节点
        };
        
        // 开始观察
        animationObserver.observe(container, config);
        
        console.log('✓ MutationObserver 已启动');
        
        // 保存引用以便清理
        this.viewer._animationObserver = animationObserver;
    },
    
    // 【新增】隐藏 UTC 元素（一次性处理，不循环）
    hideUTCElements(container) {
        if (!container) return;
        
        // 1. 隐藏 UTC 标签
        const utcLabels = container.querySelectorAll('.cesium-animation-utcLabel');
        utcLabels.forEach(label => {
            label.style.display = 'none';
        });
        
        // 2. 处理 SVG 文本中的 UTC
        const svgTexts = container.querySelectorAll('.cesium-animation-svgText tspan');
        svgTexts.forEach(tspan => {
            const originalText = tspan.textContent;
            
            // 如果包含 UTC，移除 UTC 字样
            if (originalText && originalText.includes('UTC')) {
                tspan.textContent = originalText.replace(/\s*UTC\s*/g, '').trim();
            }
            
            // 汉化日期格式 (如 "Feb 26 2026" → "2026-02-26")
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
        });
        
        // 3. 隐藏时间标签（可选）
        const timeLabels = container.querySelectorAll('.cesium-animation-timeLabel');
        timeLabels.forEach(label => {
            if (label.textContent && label.textContent.includes('UTC')) {
                label.style.display = 'none';
            }
        });
    },
    
    // 【新增】清理 Observer
    cleanupAnimationObserver() {
        if (animationObserver) {
            animationObserver.disconnect();
            animationObserver = null;
            console.log('✓ MutationObserver 已清理');
        }
        
        if (this.viewer && this.viewer._animationObserver) {
            this.viewer._animationObserver = null;
        }
    },
    
    
    popupShow() {
        popup2.show()
    },
    
    popupClose() {
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
    setSCeneView(longitude, latitude) {
        this.viewer.camera.setView({
            destination: Cesium.Cartesian3.fromDegrees(longitude, latitude, 2000.0),
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
        const position = Cesium.Ellipsoid.WGS84.cartesianToCartographic(
            new Cesium.Cartesian3(CartesianC.x, CartesianC.y, CartesianC.z)
        )
        const lon = Cesium.Math.toDegrees(position.longitude);
        const lat = Cesium.Math.toDegrees(position.latitude);
        const alt = position.height;
        return [lon, lat, alt];
    },

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
    
    // 【新增】销毁时清理资源
    destroy() {
        this.cleanupAnimationObserver();
        
        if (this.viewer) {
            this.viewer.destroy();
            this.viewer = null;
        }
        
        if (popup2) {
            popup2.destroy();
            popup2 = null;
        }
        
        console.log('✓ Cesium 资源已清理');
    }
}
