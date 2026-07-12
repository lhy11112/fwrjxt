import http from "@/utils/request"
export default {
  baseURL: `/api/service-manage/gisDataService/outer`,
  getList: async function (params) {
    return await http.get(`${this.baseURL}/getList`, params);
  },
  getCollectDataList: async function (params) {
    return await http.post(`${this.baseURL}/getCollectDataList`, params);
  }
}
