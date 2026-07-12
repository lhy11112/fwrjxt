export default {
    init(viewer, scene) {
        this.viewer = viewer
        this.scene = scene
    },

    coordToDegrees(coord) {
        const cartographic = Cesium.Cartographic.fromCartesian(coord);
        const lng = Cesium.Math.toDegrees(cartographic.longitude);
        const lat = Cesium.Math.toDegrees(cartographic.latitude);
        let height = cartographic.height;
        if (height < 0) {
            height = 0;
        }
        return { x: lng, y: lat, z: height }
    },
    // 获取东,西,南,北,东北,西北,东南,西南 方位指定长度的坐标
    getDirectionDisPoint(originPoint, direction = "east", distance = 1000) {
        // 经纬度转笛卡尔坐标
        const ellipsoid = this.viewer.scene.globe.ellipsoid;
        const p = Cesium.Cartesian3.fromDegrees(originPoint[0], originPoint[1]);
        const enu = new Cesium.Transforms.eastNorthUpToFixedFrame(p, ellipsoid, new Cesium.Matrix4());

        let offset = new Cesium.Cartesian3(1000, 0, 0);
        // sin(45°)=√2 / 2≈0.7071。
        const distanceOffset = 0.7071 * distance;
        switch (direction) {
            case "east-north":
                offset = new Cesium.Cartesian3(distanceOffset, distanceOffset, 0);
                break;
            case "west-north":
                offset = new Cesium.Cartesian3(-distanceOffset, distanceOffset, 0);
                break;
            case "east-south":
                offset = new Cesium.Cartesian3(distanceOffset, -distanceOffset, 0);
                break;
            case "west-south":
                offset = new Cesium.Cartesian3(-distanceOffset, -distanceOffset, 0);
                break;
            case "east":
                offset = new Cesium.Cartesian3(distance, 0, 0);
                break;
            case "west":
                offset = new Cesium.Cartesian3(-distance, 0, 0);
                break;
            case "north":
                offset = new Cesium.Cartesian3(0, distance, 0);
                break;
            case "south":
                offset = new Cesium.Cartesian3(0, -distance, 0);
                break;
            default:
                console.log("未匹配到");
        }
        const newPoint = Cesium.Matrix4.multiplyByPoint(enu, offset, new Cesium.Cartesian3());
        const cartographic = Cesium.Cartographic.fromCartesian(newPoint);
        const lng = Cesium.Math.toDegrees(cartographic.longitude);
        const lat = Cesium.Math.toDegrees(cartographic.latitude);
        const resPoint = [lng, lat];
        return resPoint;
    },
    // 输入一个点和直线上两点的经纬度坐标,  返回经过该点与直线垂直相交点的坐标
    findPerpendicularIntersction(A, B, P) {
        const cartesianA = Cesium.Cartesian3.fromDegrees(A[1], A[0], 0);
        const cartesianB = Cesium.Cartesian3.fromDegrees(B[1], B[0], 0);
        const cartesianP = Cesium.Cartesian3.fromDegrees(P[1], P[0], 0);
        // 计算直线的方向向量
        const AB = {
            x: cartesianB.x - cartesianA.x,
            y: cartesianB.y - cartesianA.y,
            z: cartesianB.z - cartesianA.z 
        }
        // 计算 p 点到直线 AB 上 点 A 的向量
        const AP = {
            x: cartesianP.x - cartesianA.x,
            y: cartesianP.y - cartesianA.y,
            z: cartesianP.z - cartesianA.z
        }
        // 计算 AB 的长度平方
        const AB_squared = AB.x * AB.x + AB.y * AB.y + AB.z * AB.z;
        // 检查直线是否为零向量
        if(AB_squared === 0) {
            throw new Error("点A 和 点B 重合, 无法确定直线方向");
        }
        // 计算垂足 r 在直线AB上的位置参数 t
        const t = (AB.x * AP.x + AB.y * AP.y + AB.z * AP.z) / AB_squared;
        // 计算垂足坐标
        const R = {
            x: cartesianA.x + t * AB.x,
            y: cartesianA.y + t * AB.y,
            z: cartesianA.z + t * AB.z
        }
        // 笛卡尔坐标转经纬度坐标
        const cartesianR = new Cesium.Cartesian3(R.x, R.y, R.z);
        const cartographic = Cesium.Cartographic.fromCartesian(cartesianR);
        const lon = Cesium.Math.toDegrees(cartographic.longitude), lat = Cesium.Math.toDegrees(cartographic.latitude);
        return [lat, lon];
    }
}