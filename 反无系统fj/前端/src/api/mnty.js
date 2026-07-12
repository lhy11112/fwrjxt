import http from "@/utils/request.js"
import config from "@/config"
export default {
  baseURL: `${config.API_URL}/wrj/wjbdWrjTyjh`,
  
  TYJHSC: async function (params) {
    return await http.post(`${this.baseURL}/TYJHSC`, params);
  },
}
