
//笛卡尔转换为经纬度
export const Cartesian2toDegrees = function (position) {
    var cartographic = Cesium.Cartographic.fromCartesian(position)
    var longitude = Cesium.Math.toDegrees(cartographic.longitude)
    var latitude = Cesium.Math.toDegrees(cartographic.latitude)
    var height = cartographic.height
    return [longitude, latitude, height]
}
