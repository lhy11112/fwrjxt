export class ImageLayer {
    map;
    layerMap;

    /**
     * @param viewer
     */
    constructor(map) {
        this.map = map;
        this.layerMap = new Map();
    }
    /**
     * 添加图层
     * @param url 图层地址
     * @param name 图层名称
     * @param addToken 是否使用代理地址
     * @returns
     */
    addLayer(url, name) {
        const reqUrl = url;
        const layer = new L.supermap.TiledMapLayer(reqUrl);
        layer.addTo(this.map);
        if(name && name.length > 0) {
            this.layerMap.set(name, layer._leaflet_id)
        }
    }
    /**
     * 删除图层
     * @param name 图层名称
     */
    removeLayer(name) {
        if(this.layerMap.has(name)) {
            const layer = this.map._layers[this.layerMap.get(name)]
            this.map.removeLayer(layer);
        }
    }
}
