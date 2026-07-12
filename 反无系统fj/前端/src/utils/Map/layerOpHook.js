import { getCurrentMapMode } from "@/utils/Map/mapMode"
import axios from "axios";
import {ElMessage} from "element-plus";

export const mapModeEnum = {
    "2D": "2D", // 二维地图
    "3D": "3D", // 三维地图
}
// 处理影像图层的加载和隐藏
export function controlImageLayer(checked, layer, addToken = false) {
    
    const curMapMode = getCurrentMapMode(); // 基础框架中已提供该方法, 在 /utils/Map/mapMode.js 中实现
    if (checked) {
        axios({
            url: layer.url + "/maps.json",
            method: "get"
        }).then(res=>{
            if(res.data.success && res.data.data.length){
                var url = res.data.data[0].path;
                // 加载图层
                // 判断当前地图模式 是二维还是三维
                if(curMapMode === mapModeEnum["3D"]) {
                    Map3D.imageLayer.addLayer(url, layer.name, addToken);
                } else {
                    Map2D.imageLayer.addLayer(url, layer.name, addToken);
                }
            }
        })
        
    } else {
        // 隐藏图层
         // 判断当前地图模式 是二维还是三维
        if(curMapMode === mapModeEnum["3D"]) {
        	Map3D.imageLayer.removeLayer(layer.name);
        } else {
          	Map2D.imageLayer.removeLayer(layer.name);
        }
    }
}
// 处理 S3M 图层的加载和隐藏
export function controlS3MLayer(checked, layer, addToken = false) {
    const curMapMode = getCurrentMapMode();
    if (checked) {
        // 加载图层
        if(curMapMode === mapModeEnum["3D"]) {
            axios({
                url: layer.url + "/realspace/datas.json",
                method: "get"
            }).then(res=>{
                console.log(res.data);
                if(res.data.success && res.data.data.length){
                    const url = res.data.data[0].path + "/config"
                    console.log(url, layer.name, addToken)
                    Map3D.s3mLayer.addLayer(url, layer.name, addToken);
                }else if(res.data && res.data.length){
                    const url = res.data[0].path + "/config"
                    Map3D.s3mLayer.addLayer(url, layer.name, addToken);
                }
            })
        } else {
          	// alert("不支持二维地图加载");
            //   ElMessage.info("请点击右上角转换3D模式")
        }
    } else {
        // 隐藏图层
      if(curMapMode === mapModeEnum["3D"]) {
        	Map3D.s3mLayer.removeLayer(layer.name);
        } else {
            // alert("不支持二维地图加载");
            // ElMessage.info("请点击右上角转换3D模式")
        }
    }
}