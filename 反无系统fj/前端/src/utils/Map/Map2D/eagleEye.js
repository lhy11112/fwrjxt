import { cfg2D } from '../config';
export default {
  // 初始化测量工具
  map: null,
  resultLayer: null,
  clusterMarkers: null,
  eagleLayer: null,
  eagleMap: null,

  init(map) {
    this.map = map;
  },
  // 打开鹰眼
  addeagle() {
    // 判断如果不为undefined或者null则移除鹰眼
    if (this.eagleMap !== undefined && this.eagleMap !== null) {
      this.eagleMap.remove()
    }
    const baseUrl = cfg2D.server.baseUrl1 + "/map-multiTiles/rest/maps/wj_gis_聚合影像";//"/map-chinaImg/rest/maps/wj_gis_中国_聚合影像";
    this.eagleLayer = new L.supermap.TiledMapLayer(baseUrl, { minZoom: 0, maxZoom: 13, noWrap: true });
    this.eagleMap = L.control.minimap(this.eagleLayer, { mapOptions: { logoControl: false }, toggleDisplay: true }).addTo(this.map);
  },
  // 关闭鹰眼
  cleareagle() {
    // 判断如果不为undefined或者null则移除鹰眼
    if (this.eagleMap !== undefined && this.eagleMap !== null) {
      this.eagleMap.remove()
    }
  }
}