import markerIcon from "@/assets/images/marker-icon.png";

export default {
    // 初始化测量工具
    map: null,
    drawGroup: null,
    markerSet: [],
    resultLayer: null, //聚合图层
    init(map) {
        this.map = map;
    },
    addDivIcon(options) {
        const defaultOptions = {
            iconUrl: markerIcon,
            iconSize: [25, 25],
            html: "",
            pos: [34.257, 108.944],
            popup: ""
        }
        const params = Object.assign(defaultOptions, options);
        const divIcon = L.divIcon(params);
        const marker =  L.marker(params.pos, {
            icon: divIcon
        }).bindPopup(params.popup).addTo(this.map);
        this.markerSet.push(marker);
        return marker;
    },
    // addMarker(options) {
    //     const defaultOptions = {
    //         pos: [34.257, 108.944],
    //         popup: ""
    //     }
    //     const params = Object.assign(defaultOptions, options);
    //     const icon = L.icon({
    //         iconUrl: markerIcon,
    //         iconSize: [32, 32]
    //     });
    //     const marker = L.marker(params.pos, { icon: icon }).bindPopup(params.popup).addTo(this.map);
    //     this.markerSet.push(marker);
    //     return marker;
    // },
    /*** @addMapFlag 是否加载到地图 */
    addMarker(options, addMapFlag = true) {
        const defaultOptions = {
            iconUrl: markerIcon,
            pos: [34.257, 108.944],
            iconSize: [32, 64],
            popup: ""
        }
        const params = Object.assign(defaultOptions, options);
        const icon = L.icon({
            iconUrl: params.iconUrl,
            iconSize: params.iconSize
        });
        const marker = L.marker(params.pos, { icon: icon }).bindPopup(params.popup);
        // const marker = L.marker(params.pos, { icon: icon, text: 123 }).bindPopup(params.popup);

        if ( addMapFlag ) {
            marker.addTo(this.map);
        }
        
        this.markerSet.push(marker);
        return marker;
    },
    drawLine(posList) {
        const line = L.polyline(posList, {
            color: "red",
            weight: 3,
            opaticy: 1
        }).addTo(this.map);
        this.markerSet.push(line);
    },
    removeAllMarker() {
        this.markerSet.forEach(item => {
            item.remove();
        })
    },
    /*** 聚合 */
    setMarkerClusterGroup() {
        if (this.resultLayer) {
            this.removeMarkerClusterGroup();
        }

        let clusterIcon = L.divIcon({
            className: "cluster-icon",
            iconSize: [32, 64],
            iconAnchor: [20, 40],
        })

        this.resultLayer = L.markerClusterGroup({
            /*** 更改聚合状态的样式 */
            // iconCreateFunction: (cluster) => {
            //     let count = cluster.getChildCount();
            //     clusterIcon.options.html = `<div class="custom-cluster-num">${count}</div>`
            //     return clusterIcon
            // },
            spiderfyOnMaxZoom: false,
            showCoverageOnHover: true,
            zoomToBoundsOnClick: true
        });

        /*** 在使用时，点击散开代码 */
        /* .custom-cluster-num {
            color: red;
            background-image: url(./避难场所.png);
            width: 100%;
            height: 41px;
        } */

        // 聚合解聚时强制刷新,重新计算聚合数量
        // this.resultLayer.on("zoomend", e => {
        //     this.resultLayer.refreshClusters()
        // })


        // this.resultLayer = window.L.markerClusterGroup({
        //     spiderfyOnMaxZoom: false,
        //     showCoverageOnHover: true,
        //     zoomToBoundsOnClick: false
        // });

        /*** 另一种 聚合方式 ****/
        
        // let clusterIcon = L.divIcon({
        //     className: "cluster-icon",
        //     iconSize: [39, 60],
        //     iconAnchor: [20, 40],
        // })
        // window.taskMarkers = L.markerClusterGroup({
        //     iconCreateFunction: (cluster) => {
        //         let count = cluster.getChildCount();
        //         clusterIcon.options.html = `<div class="custom-cluster-num">${count}</div>`
        //         return clusterIcon
        //     }
        // });
        // // 聚合解聚时强制刷新,重新计算聚合数量
        // window.mySuperMap.on("zoomend", e => {
        //     window.taskMarkers.refreshClusters()
        // })

    },
    /*** 移除聚合图层 */
    removeMarkerClusterGroup() {
        this.resultLayer && this.resultLayer.remove();
    }
}
