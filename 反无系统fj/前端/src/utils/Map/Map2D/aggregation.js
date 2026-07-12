export default {
  // 初始化测量工具
  map: null,
  resultLayer: null,
  clusterMarkers: null,

  init(map) {
    this.map = map;
    // 聚和解聚的功能
    const clusterIcon = L.divIcon({
      className: "cluster-icon",
      iconSize: [39, 60],
      iconAnchor: [20, 40],
    })
    this.clusterMarkers = L.markerClusterGroup({
      iconCreateFunction: (cluster) => {
        const count = cluster.getChildCount();
        clusterIcon.options.html = `<div class="custom-cluster-num">${count}</div>`
        return clusterIcon
      }
    });
    //刷新更改count
    this.map.on("zoomend", () => {
      this.clusterMarkers.refreshClusters()
    })
    // 将当前图层添加到this.map上面
    this.clusterMarkers.addTo(this.map)
  },
  addPoint(itemData) {
    this.clearLayer()

    let greenIcon = null;
    itemData.forEach(item => {
      if (item.mblx.includes("医院")) {
        greenIcon = L.icon({
          iconUrl: '/static/map_img/icon/yiyuan.png',
          iconSize: [40, 40],
        });
      } else if (item.mblx.includes("院校")) {
        greenIcon = L.icon({
          iconUrl: '/static/map_img/icon/xuexiao.png',
          iconSize: [40, 40],
        });
      } else if (item.mblx.includes("机场")) {
        greenIcon = L.icon({
          iconUrl: '/static/map_img/icon/jichang.png',
          iconSize: [40, 40],
        });
      }
      else if (item.mblx.includes("车站")) {
        greenIcon = L.icon({
          iconUrl: '/static/map_img/icon/jiaotong.png',
          iconSize: [40, 40],
        });
      } else if (item.mblx.includes("口岸")) {
        greenIcon = L.icon({
          iconUrl: '/static/map_img/icon/gangko.png',
          iconSize: [40, 40],
        });
      } else if (item.mblx.includes("机构")) {
        greenIcon = L.icon({
          iconUrl: '/static/map_img/icon/jigou.png',
          iconSize: [40, 40],
        });
      } else {
        greenIcon = L.icon({
          iconUrl: '/static/map_img/icon/jiguan.png',
          iconSize: [40, 40],
        });
      }
      var marker = L.marker(L.latLng(Number(item.wd), Number(item.jd)), { icon: greenIcon }).addTo(this.clusterMarkers);;
      marker.bindPopup(item.mbmc).openPopup(marker.getLatLng())
    })
  },
  // 点击单个数据
  clickSingle() {
    //console.log(this.clusterMarkers.eachLayer, "7897897898897789")
    this.clearLayer();
    
  },
  // 清空图层
  clearLayer() {
    if (this.clusterMarkers != undefined || this.clusterMarkers != null || this.clusterMarkers != "") {
      // 清空图层
      this.clusterMarkers.clearLayers()
    }
  }
}