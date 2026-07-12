import { cfg3D } from "../../config";

export default {
    viewer: null,
    layerMap: new Map(),

    /**
     * @param {Viewer} viewer
     */
    init(viewer) {
        this.viewer = viewer;
        this.layerMap = new Map();
    },
    /**
     * 添加图层
     * @param url 图层地址
     * @param name 图层名称
     * @returns
     */
    addLayer(url, name, options) {
        const defaultParams = {
            autoSetView: true,
        };
        const params = Object.assign(defaultParams, options); // 合并默认参数和传入参数
        const that = this;
        const targetLayer = that.layerMap.get(name);
        if (targetLayer) {
            that.viewer.imageryLayers.raiseToTop(targetLayer); // 把图层移到顶部
            that.setVisible(targetLayer, true); // 设置图层显示
            // that.flyToLayer(targetLayer);
            return;
        }
        let reqUrl = "";
        if (url.indexOf("http") === -1) {
            reqUrl = cfg3D.server.baseUrl + url;
        } else {
            reqUrl = url;
        }
        return new Promise((resolve, _reject) => {
            const layer = that.viewer.imageryLayers.addImageryProvider(
                new Cesium.SuperMapImageryProvider({
                    url: reqUrl,
                    name: name,
                    tileFormat: params.tileFormat
                })
            );
            that.viewer.imageryLayers.raiseToTop(layer);
            that.layerMap.set(name, layer);
            if (params.autoSetView) {
                // that.flyToLayer(layer);
            }
            resolve(layer);
        });
    },
    /**
     * 删除图层
     * @param name 图层名称
     */
    removeLayer(name) {
        const that = this;
        const targetLayer = that.layerMap.get(name);
        if (targetLayer) {
            that.viewer.imageryLayers.raiseToTop(targetLayer); // 把图层移到顶部
            that.setVisible(targetLayer, false); // 设置图层显示
        }
    },
    // 设置图层显示和隐藏
    setVisible(layer, visible) {
        layer.show = visible;
    },
    /**
     * 根据图层名称设置透明度
     * @param {*} name
     * @param {*} alpha
     */
    setAlpha(layer, alpha) {
        layer.alpha = alpha;
    },
    /**
     * 飞行到图层
     */
    flyToLayer(layer) {
        const that = this;
        const hpr = new Cesium.HeadingPitchRange(0, 0, 0);
        that.viewer.flyTo(layer, { offset: hpr });
    },
    flyToLayerByName(name) {
        console.log(name);
        const that = this;
        const targetLayer = that.layerMap.get(name);
        if (targetLayer) {
            const hpr = new Cesium.HeadingPitchRange(0, 0, 0);
            that.viewer.flyTo(targetLayer, { offset: hpr });
        }
    }
}
