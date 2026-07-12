import http from "@/utils/request.js"
import config from "@/config"
export default {
  baseURL: `${config.API_URL}/wjfjsjzt/wjbdHsZsk`,

  list: async function (params) {
    return await http.get(`${this.baseURL}/list`, params);
  },
  listAll: async function (params) {
    return await http.get(`${this.baseURL}/listAll`, params);
  },
  queryByBm: async function (params) {
    return await http.get(`${this.baseURL}/queryByBm`, params);
  },
  add: async function (params) {
    return await http.post(`${this.baseURL}/add`, params);
  },
  edit: async function (params) {
    return await http.put(`${this.baseURL}/edit`, params);
  },
  delete: async function (params) {
    return await http.delete(`${this.baseURL}/deleteBatch?ids=${params}`);
  }
}
