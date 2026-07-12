export default {
    viewer: {},
    slope: null,
    currentHeight: 0,
    timeId: null,
    maxValue: 450,
    minValue: 420,
    speed: 1,
    initHeight: 420,

    // 淹没分析
    init(viewer) {
        const that = this;
        this.viewer = viewer;
        this.currentHeight = this.initHeight;
    },
    // 开始分析
    handleStartFlood() {
        this.currentHeight = this.initHeight; 
        this.timeId = setInterval(this.flood.bind(this), 100);
    },
    handleClearFlood() {
        clearInterval(this.timeId);
        this.timeId = null;
        const layer = this.viewer.scene.layers.find("Combine");
        const hyp = new Cesium.HypsometricSetting();
        hyp.MaxVisibleValue = -1000;

        layer.hypsometricSetting = {
            hypsometricSetting: hyp,
            analysisMode: Cesium.HypsometricSettingEnum.AnalysisRegionMode.ARM_ALL
        }
        this.currentHeight = this.initHeight;
    },
    flood() {
        if(this.currentHeight > this.maxValue) {
            clearInterval(this.timeId);
            this.timeId  = null;
            return;
        }
        const layer = this.viewer.scene.layers.find("Combine");
        if(!layer) {
            clearInterval(this.timeId);
            this.timeId = null;
            return;
        }
        const hyp = new Cesium.HypsometricSetting();
        //创建分层设色对象   设置最大/最小可见高度   颜色表  显示模式   透明度及线宽
        const colorTable = new Cesium.ColorTable();
        colorTable.insert(71, new Cesium.Color(0, 39 / 255, 148 / 255));
        colorTable.insert(0, new Cesium.Color(149 / 255, 232 / 255, 249 / 255));

        hyp.MaxVisibleValue = this.currentHeight;
        hyp.MinVisibleValue = this.minValue;

        hyp.ColorTable = colorTable;
        hyp.DisplayMode = Cesium.HypsometricSettingEnum.DisplayMode.FACE;
        hyp.Opacity = 0.5;
        hyp.LineInterval = 10.0;

        layer.hypsometricSetting = {
            hypsometricSetting: hyp,
            analysisMode: Cesium.HypsometricSettingEnum.AnalysisRegionMode.ARM_ALL
        };
        this.currentHeight += parseInt(this.speed) / 10; // speed 的取值需要恢复
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
