export default {
    viewer: {},
    slope: null,
    handlerPolygon: null,
    wide: "0",

    // 坡度坡向分析
    init(viewer) {
        const that = this;
        this.viewer = viewer;
        this.slope = new Cesium.SlopeSetting();
        this.slope.DisplayMode = Cesium.SlopeSettingEnum.DisplayMode.FACE_AND_ARROW;
        this.slope.MaxVisibleValue = 7;
        this.slope.MinVisibleValue = 78;
        const colorTable = new Cesium.ColorTable();
        colorTable.insert(1400, new Cesium.Color(255 / 255, 0 / 255, 0 / 255));
        colorTable.insert(1300, new Cesium.Color(221 / 255, 224 / 255, 7 / 255));
        colorTable.insert(1200, new Cesium.Color(20 / 255, 187 / 255, 18 / 255));
        colorTable.insert(1100, new Cesium.Color(0, 161 / 255, 1));
        colorTable.insert(0, new Cesium.Color(9 / 255, 9 / 255, 255 / 255));
        this.wide = Cesium.HypsometricSettingEnum.AnalysisRegionMode.ARM_NONE;
        this.slope.ColorTable = colorTable;
        this.slope.Opacity = 0.5;
        // 初始化绘制多边形
        this.handlerPolygon = new Cesium.DrawHandler(viewer, Cesium.DrawMode.Polygon, 0);
        this.handlerPolygon.drawEvt.addEventListener(function (result) {
            if (!result.object.positions) {
                that.handlerPolygon.polygon.show = false;
                that.handlerPolygon.polyline.show = false;
                that.handlerPolygon.deactivate();
                that.handlerPolygon.activate();
                return;
            }
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

            that.slope.CoverageArea = positions;
            that.wide = Cesium.HypsometricSettingEnum.AnalysisRegionMode.ARM_REGION;
            that.viewer.scene.globe.SlopeSetting = {
                slopeSetting: that.slope,
                analysisMode: that.wide
            };
            that.handlerPolygon.polygon.show = false;
            that.handlerPolygon.polyline.show = true;
        });
    },
    // 开始分析
    handleStartSlopeAndAspect() {
        this.handlerPolygon.activate(); // 激活多边形绘制
    },
    // 清除分析
    handleClearSlopeAndAspect() {
        this.slope.CoverageArea = [];
        this.wide = Cesium.HypsometricSettingEnum.AnalysisRegionMode.ARM_NONE;
        this.viewer.scene.globe.SlopeSetting = {
            slopeSetting: this.slope,
            analysisMode: Cesium.HypsometricSettingEnum.AnalysisRegionMode.ARM_NONE
        };
        this.handlerPolygon.polygon.show = false;
        this.handlerPolygon.polyline.show = false;
    },
    handleCalModeChange(index) {
        switch (index) {
            case "0":
                this.wide = Cesium.HypsometricSettingEnum.AnalysisRegionMode.ARM_REGION;
                break;
            case "1":
                this.wide = Cesium.HypsometricSettingEnum.AnalysisRegionMode.ARM_ALL;
                break;
            case "2":
                this.wide = Cesium.HypsometricSettingEnum.AnalysisRegionMode.ARM_NONE;
                break;
            default:
                break;
        }
        if (index === "0" && this.slope.CoverageArea.length === 0) {
            this.wide = Cesium.HypsometricSettingEnum.AnalysisRegionMode.ARM_NONE;
        }
        this.viewer.scene.globe.SlopeSetting = {
            slopeSetting: this.slope,
            analysisMode: this.wide
        };
    },
    // 坡度区间 - 起
    handleWideMinRChange(val) {
        this.slope.MinVisibleValue = val;
        this.viewer.scene.globe.SlopeSetting = {
            slopeSetting: this.slope,
            analysisMode: this.wide
        };
    },
    // 坡度区间 - 终
    handleWideMaxRChange(val) {
        this.slope.MaxVisibleValue = val;
        this.viewer.scene.globe.SlopeSetting = {
            slopeSetting: this.slope,
            analysisMode: this.wide
        };
    },
    handleShowStyleChange(val) {
        if(val === "showcolor") {
            this.slope.DisplayMode = Cesium.SlopeSettingEnum.DisplayMode.FACE;
        } else if (val === "showarrow") {
            this.slope.DisplayMode = Cesium.SlopeSettingEnum.DisplayMode.ARROW;
        } else {
            this.slope.DisplayMode = Cesium.SlopeSettingEnum.DisplayMode.FACE_AND_ARROW;
        }
        this.viewer.scene.globe.SlopeSetting = {
            slopeSetting: this.slope,
            analysisMode: this.wide
        };
    },
    handleColorChange(colors) {
        const colorTable = new Cesium.ColorTable();
        for (let i=0; i < colors.length; i++) {
            const color = colors[i];
            colorTable.insert(color.slope, new Cesium.Color(color.color[0], color.color[1], color.color[2]));
        }
        this.slope.ColorTable = colorTable;
        this.viewer.scene.globe.SlopeSetting = {
            slopeSetting: this.slope,
            analysisMode: this.wide
        };
    },
    handleTransChange(trans) {
        this.slope.Opacity = trans;
        this.viewer.scene.globe.SlopeSetting = {
            slopeSetting: this.slope,
            analysisMode: this.wide
        };
    }
}
