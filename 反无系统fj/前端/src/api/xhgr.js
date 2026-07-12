import http from "@/utils/request.js"
import config from "@/config"
export default {
  baseURL: `${config.API_URL}/uav/uavDeviceHeartbeat`,
  baseURL2: `${config.API_URL}/Device`,
  // 操作日志
  selectLatestHeartbeat: async function (params) {
    return await http.get(`${this.baseURL}/selectLatestHeartbeat`, params);
  },
  ControlDevice: async function (params) {
    return await http.post(`${this.baseURL2}/ControlDevice`, params);
  },
}
