import http from "@/utils/request.js"
import config from "@/config"
export default {
  baseURL: `${config.API_URL}/wrj/wjbdWrjGjjl`,

  list: async function (params) {
    return await http.get(`${this.baseURL}/list`, params);
  },
  GjQkTJ: async function (params) {
    return await http.get(`${this.baseURL}/GjQkTJ`, params);
  },
}
