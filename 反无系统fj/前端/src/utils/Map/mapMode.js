import { useMap3DStore } from "@/store/modules/map3D";
const map3dStore = useMap3DStore();

export const mapModeEnum = {
    "2D": "2D", // 二维地图
    "3D": "3D", // 三维地图
    "unknow": "unknow", // 未知
}

// 获取当前地图的模式
export const getCurrentMapMode = () => {
    if (map3dStore.width === "100%") {
        return mapModeEnum["3D"];
    } else if (map3dStore.width === "0") {
        return mapModeEnum["2D"];
    }
}