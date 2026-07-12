const SERVER_ADDRESS = {
	//接口地址
	API_URL: "/wrj-api",
	TOKEN_NAME: "Authorization",

	//Token前缀，注意最后有个空格，如不需要需设置空字符串
	TOKEN_PREFIX: "Basic ",
	CLOUD_TOKEN_NAME: 'X-Access-Token',
	VUE_CAS_FLAG:false,
	VUE_CAS_YHID:"1519145077207801857",
	// 后台地址
	VUE_APP_API_BASE_URL: "http://172.16.235.67:30850",
	// 前端地址
	VUE_APP_API_FRONT_BASE_URL: "http://192.168.0.233:30852",
	// 是否有超图服务
	VUE_APP_SUPERMAP_BASE_FLAG:false,
	// 超图地址
	VUE_APP_SUPERMAP_BASE_URL: "http://18.91.40.10:30261",//"http://172.16.235.23:9341",
	//单点登录
	VUE_APP_CAS_BASE: "http://18.91.40.10:31000",
	// #门户网站
	VUE_APP_CAS_BASE_URL_MHWZ: "http://18.91.40.10:31002",
	PUBLIC_DILI_SWDX: "http://172.16.235.188:8090", // 地图三维地形加载
	// #kkFile文档预览
	VUE_APP_API_BASE_URL_WDYL:"http://18.91.40.10:30112",
	
	VUE_APP_MAP_WP:"/tiles/{z}/{x}/{y}.jpg",

	FJZDBDNM:'911400000',

	//宁德市委市政府ogsb格式模型服务
	layerInfo:[{
		// url: "http://172.18.235.80:30261/gisData/1979090533712523266/iserver/services/1979090529782460418/rest",
    // name: "2025101706550_20251017065600"
	}],

	// centerArr:[[26.96817,119.4742],[26.845,119.96],[26.37,119.5384],[26.405363,119.98]],
	// kmArr:[20,20,20,20],
	// colorArr:['red','red','red','red'],
	// textArr:['限制区','禁飞区','预警区','允许飞行区域'],

	// fxgjMs:"根据无人机当前速度，经过5分钟后，最大活动范围约为20公里；"
	
}

// 如果生产模式，就合并动态的APP_CONFIG
// public/config.js
if (process.env.NODE_ENV === 'production') {
	Object.assign(SERVER_ADDRESS, window.SERVER_ADDRESS)
}

module.exports = SERVER_ADDRESS