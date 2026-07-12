import marker from "./marker";
export default {
    // 初始化测量工具
    map: null,
    // 图层
    drawGroup: null,
    textGroup: [],
    editGodCircle: null,
    editGodPolygon: null,
    cehuiIndex: 0,
    layerControlObj: {

    }, //单个图层移除时的管理对象
    textGroupControlObj: {

    }, //单个地图文本移除时的管理对象
    init(map) {
        this.map = map;
        this.initDrawGroup();
        // 添加画图提示信息
        L.drawLocal.draw.handlers.polyline = {
            tooltip: {
                start: "点击地图开始画线",
                cont: "继续选择",
                end: "双击完成绘制"
            }
        }
        L.drawLocal.draw.handlers.polygon = {
            tooltip: {
                start: "点击地图开始绘制多边形",
                cont: "继续选择",
                end: "点击第一个顶点完成绘制"
            }
        };
        this.initEvent();
    },
    initDrawGroup() {
        this.drawGroup = new L.FeatureGroup();
        this.map.addLayer(this.drawGroup);
    },
    // 画画创建事件
    initEvent() {
        this.map.on(L.Draw.Event.CREATED, event => {
            this.cehuiIndex  = new Date().getTime();
            const layerName = "测绘" + this.cehuiIndex;
            const { layer, layerType } = event;
            let addMarker = null;
            if(layerType === "polygon") {
                //存储的内容
                const latlng = layer.getLatLngs()[0]; // getLatLngs 得到的是多边形的经纬度集合, 多边形得到的是二维数组, 这里要取的是里面的数组
                const area = this.fromatArea(latlng);
                const centerPos = this.getPolygonCenter(layer._latlngs);
                // addMarker = marker.addDivIcon({ 
                //     html: area, 
                //     pos: [centerPos[0], centerPos[1]]
                // })
                // this.textGroup.push(addMarker);
                layer.bindTooltip(area, {
                    permanent: true
                    }).openTooltip()

                //将数据发送到测绘管理弹窗
                eventBus.emit("setCehuiData", { 
                    latlng: latlng, 
                    area: area, 
                    layerType: "polygon", 
                    type: "2d", 
                    layerName: layerName, 
                    typeName: "测面", 
                    pos:  [centerPos[0], centerPos[1]]
                });

            } else if(layerType === "polyline") {
                //存储的内容
                const latlng = layer.getLatLngs();
                const distance = this.fromatDistance(latlng);
                // addMarker = marker.addDivIcon({ html: distance, pos: [latlng[0].lat, latlng[0].lng] });
                // this.textGroup.push(addMarker);
                layer.bindTooltip(distance, {
                    permanent: true
                }).openTooltip()

                //将数据发送到测绘管理弹窗
                eventBus.emit("setCehuiData", {
                    latlng: latlng, 
                    distance: distance, 
                    layerType: "polyline", 
                    type: "2d", 
                    layerName: layerName, 
                    typeName: "测距",
                    pos: [latlng[0].lat, latlng[0].lng]
                });
            }
            // 把画图的图层添加到图层组方便管理
            this.drawGroup.addLayer(layer);
            this.layerControlObj[layerName] = layer; //将现标的图层和历史的图层分开存储 -- 用于清空管理
            this.textGroupControlObj[layerName] = addMarker; //将现标的图层和历史的图层分开存储 -- 用于清空管理
        })
    },
    getPolygonCenter(points) {
        // 示例多边形数据
        const polygon = {
            type: 'Feature',
            geometry: {
                type: 'Polygon',
                coordinates: [
                    [[-73.98542, 40.74881], [-73.98542, 40.74881], [-73.98542, 40.74881], [-73.98542, 40.74881], [-73.98542, 40.74881]]
                ]
            }
        };
        const arr = [];
        points[0].forEach(item => {
            arr.push([item.lat, item.lng]);
        })
        polygon.geometry.coordinates = [arr];
        const centerOfMass = window.turf.centerOfMass(polygon);
        return centerOfMass.geometry.coordinates;
    },
    // 绘制多边形
    drawPolygon() {
        const polygon = new L.Draw.Polygon(this.map, {
            shapeOptions: {
                weight: 1,
                color: "#51ff00",
                opacity: 1,
                fillColor: "#eb963f"
            }
        });
        polygon.enable();
    },
    // 绘制线段
    drawPolyline() {
        const polyline = new L.Draw.Polyline(this.map, {
            shapeOptions: {
                weight: 1,
                color: "#51ff00",
                opacity: 1,
            }
        });
        polyline.enable();
    },
    // 获取面积
    fromatArea(polygon) {
        const seeArea = L.GeometryUtil.geodesicArea(polygon); // L.GeometryUtil.geodesicArea 返回 number 类型数据, 单位是平方米, 转化
        return (seeArea / 10e5).toFixed(2) + "km";
    },
    // 获取长度
    fromatDistance(polyline) {
        let dis = 0;
        for (let i = 0; i < polyline.length - 1; i++) {
            const start = polyline[i], end = polyline[i + 1];
            dis += L.latLng([start.lat, start.lng]).distanceTo([end.lat, end.lng]);
        }
        return (dis / 10e2).toFixed(2) + "km"; // 得到的结果是 number, 单位是 m
    },
    // 点击清除
    clear() {
        // 清空添加的文字标注
        // for (let i = 0; i < this.textGroup.length; i++) {
        //     const marker = this.textGroup[i];
        //     marker.remove();
        // }
        // // // 清空添加图形标注
        // const layerList = this.drawGroup.getLayers();
        // //console.log(layerList, "78978978789789789879")
        // for (let i = 0; i < layerList.length; i++) {
        //     this.drawGroup.removeLayer(layerList[i]);
        // }
        // 清空添加的文字标注
        for(let i=0; i<this.textGroup.length; i++) {
            const marker = this.textGroup[i];
            marker.remove();
        }
        // 清空添加图形标注
        const layerList = this.drawGroup.getLayers();
        for(let i=0; i<layerList.length; i++) {
            this.drawGroup.removeLayer(layerList[i]);
        }

        /*** 针对手动场景 */
        Object.keys(this.layerControlObj).forEach(layerKey => {
            this.layerControlObj[layerKey].remove();
        })

        this.layerControlObj = {};
        this.textGroupControlObj = {};
        eventBus.emit("clearCehuiData");
        this.textGroup = [];
    },
    // 画圆
    drawCircle() {
        this.editGodCircle = new L.Draw.Circle(this.map, {
            shapeOptions: {
                weight: 2,
                fillOpacity: 0.35,
                color: '#265ba5',
                type: "circle"
            },
        })
        this.editGodCircle.enable()
        // this.getLayer()
    },
    getLayer() {
        //绘制完成回调
        this.map.off(L.Draw.Event.CREATED).on(L.Draw.Event.CREATED, e => {
            //清空分析时绘制的图层
            this.drawGroup && this.drawGroup.clearLayers()
            var layer = e.layer
            this.drawGroup.addLayer(layer)
            //画圆，获取几何的点
            if (e.layerType === 'circle') {


                //画圆，获取几何的点
                if (e.layerType === 'circle') {

                    const polygon = L.PM.Utils.circleToPolygon(e.layer, 360)
                    layer = polygon
                }
                const polygon = L.PM.Utils.circleToPolygon(e.layer, 360)
                layer = polygon
            }

            // //判断当前页面是哪个大屏
            // this.currentPageType(layer)

            // // 测量面积
            // this.calcArea(e)
        })
    },
    // 计算面积
    calcArea(e) {
        let result = ''
        if (e.layerType === 'circle') {
            const area = 3.14 * e.layer._mRadius * e.layer._mRadius
            const areaFix = (area / 1000000).toFixed(4)
            if (Number(areaFix) < 1) {
                result = area.toFixed(4) + 'm²'
            } else {
                result = (area / 1000000).toFixed(4) + 'km²'
            }
            //   计算面积
            //   this.$emit('calcArea', result)
        } else if (e.layerType === 'polygon') {
            this.queryAreaDistance(e.layer, URL_CONFIG.CHINA_VECTOR_IMG).then(res => {
                result = res.substring(4, res.length)
                // 计算面积
                // this.$emit('calcArea', result)
            })
        }
    },
    //查询面的距离，多边形
    queryAreaDistance(layer) {
        return new Promise((resolve) => {
            L.supermap
                .measureService(URL_CONFIG.CHINA_VECTOR_IMG)
                .measureArea(new SuperMap.MeasureParameters(layer), serviceResult => {
                    const area = serviceResult.result.area
                    const areaFix = (area / 1000000).toFixed(4)
                    if (Number(areaFix) < 1) {
                        resolve(`总面积:${serviceResult.result.area.toFixed(4)}m²`)
                    } else {
                        resolve(`总面积:${(serviceResult.result.area / 1000000).toFixed(4)}km²`)
                    }
                })
        })
    },

    //移除单个的图层
    clearSingleLayer(layerName ) {
        this.layerControlObj[layerName].remove();
        this.textGroupControlObj[layerName].remove();
    },
    //再次使用图层
    useSingleLayer(layerName) {
        this.layerControlObj[layerName].addTo(this.map);
        this.textGroupControlObj[layerName].addTo(this.map);
    },
    // 手动绘制面
    drawAlreadyAreaData(data) {
        var polygon = L.polygon(data.latlng, 
            {
                weight: 1,
                color: "#51ff00",
                opacity: 1,
                fillColor: "#eb963f"
            }).addTo(this.map);
        this.layerControlObj[data.layerName] = polygon;
        const addMarker = marker.addDivIcon({ html: data.area, pos: data.pos});
        this.textGroupControlObj[data.layerName] = addMarker;
        this.textGroup.push(addMarker);
    },
    // 手动绘制线
    drawAlreadyLineData(data) {
        var polyline = L.polyline(data.latlng, 
            {
                weight: 1,
                color: "#51ff00",
                opacity: 1,
            }
        ).addTo(this.map);
        this.layerControlObj[data.layerName] = polyline;
        const addMarker = marker.addDivIcon({ html: data.distance, pos: data.pos});
        this.textGroupControlObj[data.layerName] = addMarker;
        this.textGroup.push(addMarker);
    }
}