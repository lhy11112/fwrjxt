import request from "@/utils/axios";
import http from "@/utils/request"
import axios from "axios";
import config from "@/config"

// 标绘信息添加保存
export function addPlot(data) {
  return axios({
    url: `${config.API_URL}/wrj/wjbdDxyyBh/add`, //bigScreenDataFx/bhInfo
    method: 'post',
    data: data
  })
}

// 标号获取分页
export function getPlottingList(params) {
    return request({
        url: "/service-manage/dataSymbolFile/dataSymbolFile/list",
        method: "get",
        params: params
    })
}

export function getPlottingPage(params) {
    return request({
        url: "/service-manage/dataSymbolFile/dataSymbolFile/page",
        method: "get",
        params: params
    })
}

// 保存标号修改
export function savePlottingEdit(data) {
    return request({
        url: "/service-manage/dataSymbolFile/dataSymbolFile/submit",
        method: "post",
        data: data
    })
}



// 小文件上传
export const UploadSmallFile = (params) => {
    return request({
        url: "/service-manage/oss/ossOperate/uploadFile",
        method: "get",
        params: params
    })
}

export const chatWithBigModel = async (params) => {
    return await http.post(config.VUE_APP_SUPERMAP_BASE_URL+"/api/service-manage/glm/chat/completions",params)
}