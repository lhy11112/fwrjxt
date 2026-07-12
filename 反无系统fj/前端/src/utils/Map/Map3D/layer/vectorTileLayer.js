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
    addLayer(url, name) {
        const that = this;
        const targetLayer = that.layerMap.get(name);
        if (targetLayer) {
            that.setVisible(targetLayer, true); // 设置图层显示
            that.flyToLayer(targetLayer);
            return;
        }
        let reqUrl = "";
        if (url.indexOf("http") === -1) {
            reqUrl = cfg3D.server.baseUrl + url;
        } else {
            reqUrl = url;
        }
        return new Promise((resolve, _reject) => {
            const mvtMap = this.viewer.scene.addVectorTilesMap({
                url: reqUrl,
                name: name,
                viewer: this.viewer,
            });
            that.layerMap.set(name, mvtMap);
            resolve(mvtMap);
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
        layer.alpha = parseFloat(alpha);
    },
    /**
     * 飞行到图层
     */
    flyToLayer(layer) {
        const that = this;
        var bounds = layer.rectangle;
        that.viewer.scene.camera.setView({
            destination: new Cesium.Cartesian3.fromRadians((bounds.east + bounds.west) * 0.5, (bounds.north + bounds.south) *
                0.5, 10000),
            orientation: {
                heading: 0,
                roll: 0
            }
        });
    }
}
