import http from "@/utils/request.js"
import config from "@/config"
export default {
  baseURL: `/huizhi/vectorstore/zsk`,
  // 
  list: async function (params) {
    return await http.get(`${this.baseURL}/list`, params);
  },
  uploadZskFile: async function (params) {
    return await http.post(`${this.baseURL}/uploadZskFile`, params);
  },
  edit: async function (params) {
    return await http.put(`${this.baseURL}/edit`, params);
  },
  delete: async function (params) {
    return await http.delete(`${this.baseURL}/delete?ids=${params.ids}`);
  },
}