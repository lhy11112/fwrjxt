
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
        let reqUrl = "";
        if (url.indexOf("http") === -1) {
            reqUrl = cfg3D.server.baseUrl + url;
        } else {
            reqUrl = url;
        }
        return that.viewer.scene
            .open(reqUrl, name)
            .then((layer) => {
                const layerNameList = [];
                layer.forEach((item) => {
                    layerNameList.push(item.name);
                })
                that.layerMap.set(name, layerNameList);
            })
            .otherwise((err) => {
                console.log("倾斜摄影加载失败");
            });
    },
    /**
     * 删除图层
     * @param name 图层名称
     */
    removeLayer(name) {
        // 由于三维服务分图层和场景, 显隐控制比较复杂。 图层删除是真是的删除, 不做隐藏来模拟删除。
        const that = this;
        const targetLayerNameList = that.layerMap.get(name);
        if (targetLayerNameList) {
            targetLayerNameList.forEach((layerName) => {
                that.viewer.scene.layers.remove(layerName, false);
            })
            that.layerMap.delete(name);
        }
    }
}
