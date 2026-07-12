export default {
  // 初始化测量工具
  map: null,
  drawGroup: null,
  secondGropLayers: null,

  init(map) {
    this.map = map;
    this.initDrawGroup()
  },
  // 添加图层
  initDrawGroup() {
    const that = this;
    // 添加所需要的图层到地图上
    that.secondGropLayers = L.layerGroup([]);
    that.map.addLayer(that.secondGropLayers);

  },
  addPont(options) {
    const that = this;
    that.clearLayer()
    // 添加图层，分别展示中队和支队
    options.forEach(item => {
      if (item.bdjc.includes("支队")) {
        var greenIcon = L.icon({
          iconUrl: '/static/zhid.png',
          iconSize: [40, 40],
        });
      } else if (item.bdjc.includes("中队")) {
        var greenIcon = L.icon({
          iconUrl: '/static/zhongd.png',
          iconSize: [40, 40],
        });
      } else if (item.bdjc.includes("总队")) {
        var greenIcon = L.icon({
          iconUrl: '/static/zongd.png',
          iconSize: [40, 40],
        });
      } else if (item.bdjc.includes("大队")) {
        var greenIcon = L.icon({
          iconUrl: '/static/dad.png',
          iconSize: [40, 40],
        });
      }
      var html = `<div style="padding: 20px 15px;padding-top:15px;background: url(/static/back/${item.bdjc.includes("大队") ? 'dad' : item.bdjc.includes("支队") ? 'zhid' : item.bdjc.includes("中队") ? 'zhongd' : item.bdjc.includes("总队") ? 'zongd' : ""}.png);background-size: 100% 100%;">
                <div>名称：${item.bdjc}</div><div>地址：${item.kzdm}</div><div>数量：${item.sl}</div>
              </div>`
      //将坐标点添加到图层上
      var marker = L.marker([Number(item.wd), Number(item.jd)], { icon: greenIcon }).addTo(that.secondGropLayers);
      marker.bindPopup(html, { className: 'mypopup_sxrw', closeOnClick: false, autoClose: false }).openPopup(marker.getLatLng())

    })

  },
  clearLayer() {
    // // 清除其他目标点
    if (this.secondGropLayers !== undefined && this.secondGropLayers !== '' && this.secondGropLayers !== null) {
      this.secondGropLayers.clearLayers()
    }

  }
}