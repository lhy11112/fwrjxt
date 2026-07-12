import { cfg3D } from "../../config";
import axios from "axios";

export default {
  viewer: null,
  layerOption: null,
  layer:{},

  /**
   * @param {Viewer} viewer
   */
  init(viewer) {
    this.viewer = viewer;
  },
  /**
   * 添加图层
   * @param url 图层地址
   * @param name 图层名称
   * @returns
   */
  async addLayer(url, name,flyFlag = false) {
    const that = this;
    let reqUrl = "";
    if (url.indexOf("http") === -1) {
      reqUrl = cfg3D.server.baseUrl + url;
    } else {
      reqUrl = url;
    }
    // console.log(flyFlag);
    // if(flyFlag){
    //   this.removeLayer(name)
    // }
    // const tmpData = await axios.get(url.split("30261")[1].replace("/config",".json"));
    // console.log(tmpData);
    // if(tmpData.data['dataType'] == "OSGB"){
       that.viewer.scene
        .addS3MTilesLayerByScp(reqUrl, { name: name})
        .then(layer => {
          this.layer["layer"+name] = layer;
          console.log(this.layer["layer"+name]);
          // if(flyFlag){
          //   that.flyToLayer(name);
          // }
        })
        .otherwise(() => {
          console.log("倾斜摄影加载失败");
        });
    // }
    // else{
    //   let terrainProvider = new Cesium.SuperMapTerrainProvider({
    //     url : reqUrl.replace("/config",""),
    //     isSct : true,//地形服务源自SuperMap iServer发布时需设置isSct为true
    //     invisibility:true
    //   })
    //   that.viewer.terrainProvider = terrainProvider;
    //   console.log(terrainProvider);
  
    //   that.viewer.terrainProvider.readyPromise.then(() => {
    //     that.viewer.camera.flyTo({
    //       destination: that.viewer.terrainProvider._boundsRadians,
    //       duration: 2,
    //       orientation: {
    //         heading: Cesium.Math.toRadians(0),
    //         pitch: Cesium.Math.toRadians(-90),
    //         roll: 0
    //       }
    //     })
    //   })
    // }
  },
  /**
   * 删除图层
   * @param name 图层名称
   */
  removeLayer(name) {
    console.log('xxx',name);
    const that = this;
    that.viewer.scene.layers.remove(name, false); // 由于三维服务分图层和场景, 显隐控制比较复杂。 图层删除是真是的删除, 不做隐藏来模拟删除。

    // that.viewer.terrainProvider = new Cesium.EllipsoidTerrainProvider();
  },
  // 设置图层显示和隐藏
  setVisible(layer, visible) {
    layer.visible = visible;
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
  flyToLayer(name) {
    const that = this;
    // const hpr = new Cesium.HeadingPitchRange(0, 0, 0);
    // that.viewer.flyTo(layer, { offset: hpr });
    const bounds = that.layer["layer"+name].layerBounds;
    console.log(bounds);
    that.viewer.scene.camera.setView({
        destination: new Cesium.Cartesian3.fromRadians((bounds.east + bounds.west) * 0.5, (bounds.north + bounds.south) * 0.5, 2000),
        orientation: {
        heading: 0,
        roll: 0,
        },
    });
  }
}
