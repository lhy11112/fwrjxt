import http from "@/utils/request.js"
import config from "@/config"
export default {
  baseURL: `${config.API_URL}/Device`,
  baseURL2: `${config.API_URL}/wrj/spectrumInfo`,
  ControlDeviceSpectrumParam: async function (params) {
    return await http.post(`${this.baseURL}/ControlDeviceSpectrumParam`, params);
  },
  SendWebsocketData: async function (params) {
    return await http.get(`${this.baseURL2}/SendWebsocketData`, params);
  },
  //选中频谱数据测向接口
  ControlTypeDfFreqParam: async function (params) {
    return await http.post(`${this.baseURL}/ControlTypeDfFreqParam`, params);
  },
  ControlDeviceTcpServerSocketServer: async function (params) {
    return await http.post(`${this.baseURL}/ControlDeviceTcpServerSocketServer`, params);
  },
}
