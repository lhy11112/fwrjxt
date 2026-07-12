import http from "@/utils/request.js"
import config from "@/config"
export default {
  baseURL: `${config.API_URL}/uav/uavDeviceConfig`,
  baseURL2: `${config.API_URL}/uav/uavDetectMsg`,
  // 操作日志
  list: async function (params) {
    return await http.get(`${this.baseURL}/list`, params);
  },
  getUavDetectMsgByStationId: async function (params) {
    return await http.get(`${this.baseURL2}/getUavDetectMsgByStationId`, params);
  },
  getUavDetectMsgByModelSerialRq: async function (params) {
    return await http.get(`${this.baseURL2}/getUavDetectMsgByModelSerialRq`, params);
  },
  generateFlightRoute: async function (params) {
    return await http.get(`${this.baseURL}/generateFlightRoute`, params);
  },
  getUavDetectMsgDateByNfYf: async function (params) {
    return await http.get(`${this.baseURL2}/getUavDetectMsgDateByNfYf`, params);
  },
}
