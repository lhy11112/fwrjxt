import http from "@/utils/request.js"
import config from "@/config"
export default {
  baseURL: `${config.API_URL}/uav/uavDeviceConfig`,
  baseURL2: `${config.API_URL}/uav`,
  // 操作日志
  list: async function (params) {
    return await http.get(`${this.baseURL}/list`, params);
  },
  listAll: async function (params) {
    return await http.get(`${this.baseURL}/listAll`, params);
  },
  add: async function (params) {
    return await http.post(`${this.baseURL}/add`, params);
  },
  edit: async function (params) {
    return await http.put(`${this.baseURL}/edit`, params);
  },
  delete: async function (params) {
    return await http.delete(`${this.baseURL}/deleteBatch?ids=${params.ids}`);
  },
  getSbCountByLx: async function (params) {
    return await http.get(`${this.baseURL}/getSbCountByLx`, params);
  },
  uavConnectLogList: async function (params) {
    return await http.get(`${this.baseURL2}/uavConnectLog/list`, params);
  },
  uavOperateLogList: async function (params) {
    return await http.get(`${this.baseURL2}/uavOperateLog/list`, params);
  },
  uavDeviceHeartbeatList: async function (params) {
    return await http.get(`${this.baseURL2}/uavDeviceHeartbeat/list`, params);
  },
  uavActiveHeartbeatList: async function (params) {
    return await http.get(`${this.baseURL2}/uavActiveHeartbeat/list`, params);
  },
}
