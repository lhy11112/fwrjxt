import config from "@/config"
import http from "@/utils/request"
export default {
	baseURL: `${config.API_URL}/battledamage/zzll_gjgwry_zhy`,
	// 查询长度所耗费时间
	getMarchTime: async function (params) {
		return await http.get(`${this.baseURL}/getMarchTime?distance=`+params);
	},
	// 查询经纬度
	getPointByarea: async function (params) {
		return await http.post(`${this.baseURL}/getPointByarea?area=`+params.area+"&city="+params.city+"&province="+params.province,null);
	},

}
