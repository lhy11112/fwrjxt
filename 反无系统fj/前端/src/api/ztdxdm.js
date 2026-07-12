import http from "@/utils/request.js"
import config from "@/config"
export default {
  baseURL: `/jeecg-boot/wjfjsjzt/rwlyFwzzEjsjztDxdm`,
  // 
  listType: async function (params) {
    return await http.get(`${this.baseURL}/listType`, params);
  },
  list: async function (params) {
    return await http.get(`${this.baseURL}/list`, params);
  },
}