import http from "@/utils/request.js"
import config from "@/config"
export default {
  baseURL: `/jeecg-boot/wjfjsjzt/rwlyFwzzZymbfwMbjcJcxx`,
  // 
  leftTree: async function (params) {
    return await http.get(`${this.baseURL}/leftTree`, params);
  },
  list: async function (params) {
    return await http.get(`${this.baseURL}/list`, params);
  }
}