import config from "@/config"
import http from "@/utils/request"
export default {
  baseURL: `${config.API_URL}/bigScreenDataFx`,
  baseURL2: `${config.API_URL}/dxyy/zzllBd`,
  baseURL3: `${config.API_URL}/wrj/wjbdDxyyBh`,
  queryChildUnit:async function (params) {
    return await http.get(`${this.baseURL2}/queryChildUnit`, params);
  },
  getBhByYwIdDataTime:async function (params) {
    return await http.post(`${this.baseURL3}/getBhByYwIdDataTime`, params);
  },
}