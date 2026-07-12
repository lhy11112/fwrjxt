export default {
    viewer: {},
    slope: null,
    handlerPolygon: null,
    currentHeight: 0,
    timeId: null,
    hypFlood: null,
    maxValue: 9000,
    minValue: 1100,
    speed: 10,
    intiHeight: 0,

    // 淹没分析
    init(viewer) {
        const that = this;
        this.viewer = viewer;
        this.currentHeight = this.intiHeight;
        this.hypFlood = new Cesium.HypsometricSetting();
        const floodColorTable = new Cesium.ColorTable();
        floodColorTable.insert(
            9000,
            new Cesium.Color(210 / 255, 15 / 255, 15 / 255)
        );
        floodColorTable.insert(
            6000,
            new Cesium.Color(221 / 255, 224 / 255, 7 / 255)
        );
        floodColorTable.insert(
            5000,
            new Cesium.Color(20 / 255, 187 / 255, 18 / 255)
        );
        floodColorTable.insert(4000, new Cesium.Color(0, 161 / 255, 1));
        floodColorTable.insert(0, new Cesium.Color(9 / 255, 9 / 255, 212 / 255));
        this.hypFlood.DisplayMode = Cesium.HypsometricSettingEnum.DisplayMode.FACE;
        this.hypFlood._lineColor = new Cesium.Color(1.0, 0.0, 0.0, 1.0);
        this.hypFlood.MinVisibleValue = 0;
        this.hypFlood.MaxVisibleValue = 0;
        this.hypFlood.ColorTableMinKey = 1;
        this.hypFlood.ColorTableMaxKey = 9000;
        this.hypFlood.ColorTable = floodColorTable;
        this.hypFlood.Opacity = 0.8;
        this.hypFlood.LineInterval = 200.0;
        this.handlerPolygon = new Cesium.DrawHandler(viewer, Cesium.DrawMode.Polygon);
        this.handlerPolygon.drawEvt.addEventListener(function (result) {
            that.handlerPolygon.polygon.show = false;
            that.handlerPolygon.polyline.show = false;
            const array = [].concat(result.positions);
            const positions = [];
            for (let i = 0, len = array.length; i < len; i++) {
                const cartographic = Cesium.Cartographic.fromCartesian(array[i]);
                const longitude = Cesium.Math.toDegrees(cartographic.longitude);
                const latitude = Cesium.Math.toDegrees(cartographic.latitude);
                const h = cartographic.height;
                if (positions.indexOf(longitude) == -1 && positions.indexOf(latitude) == -1) {
                    positions.push(longitude);
                    positions.push(latitude);
                    positions.push(h);
                }
            }
            that.hypFlood.CoverageArea = positions;
            that.floodUpdate();
            that.handlerPolygon.deactivate();
        });
    },
    // 开始分析
    handleStartFlood() {
        this.handleClearFlood();
        this.handlerPolygon.activate(); // 激活多边形绘制
    },
    handleClearFlood() {
        clearInterval(this.timeId);
        this.timeId = null;
        this.handlerPolygon.deactivate();
        this.handlerPolygon.clear();
        this.viewer.scene.globe.HypsometricSetting = undefined;
    },
    floodUpdate() {
        this.currentHeight = this.minValue;
        this.hypFlood.MinVisibleValue = this.minValue;
        this.timeId = setInterval(this.flood.bind(this), 100);
    },
    flood() {
        if (this.currentHeight > this.maxValue) {
            clearInterval(this.timeId);
            this.timeId = null;
            return;
        }
        this.hypFlood.MaxVisibleValue = this.currentHeight;

        this.viewer.scene.globe.HypsometricSetting = {
            hypsometricSetting: this.hypFlood,
            analysisMode: Cesium.HypsometricSettingEnum.AnalysisRegionMode.ARM_REGION
        };
        this.currentHeight += parseInt(this.speed) / 10;
    },
    handleMaxHeightChange(val) {
        this.maxValue = val;
    },
    handleMinHeightChange(val) {
        this.minValue = val;
    },
    handleSpeedChange(val) {
        this.speed = val;
    }
}
