const SERVER_ADDRESS = {
	//接口地址
	API_URL: "/wrj-api",
	TOKEN_NAME: "Authorization",

	//Token前缀，注意最后有个空格，如不需要需设置空字符串
	TOKEN_PREFIX: "Basic ",
	CLOUD_TOKEN_NAME: 'X-Access-Token',
	// 后台地址
	VUE_APP_API_BASE_URL: "http://192.168.0.185:30851",
        VUE_CAS_FLAG:false,
	// 超图地址
	VUE_APP_SUPERMAP_BASE_URL: "http://11.120.10.16:30190",
	PUBLIC_DILI_SWDX: "http://11.120.10.16:30190", // 地图三维地形加载
        //单点登录
	VUE_APP_CAS_BASE: "http://192.168.100.102:31000",
	// #门户网站
	VUE_APP_CAS_BASE_URL_MHWZ: "http://192.168.100.102:31002",
	// 是否有超图服务
	VUE_APP_SUPERMAP_BASE_FLAG:false,
	// 前端地址
	VUE_APP_API_FRONT_BASE_URL: "http://192.168.0.185:30852",
	// #kkFile文档预览
	VUE_APP_API_BASE_URL_WDYL:"http://11.120.10.16:30112",
}
window.SERVER_ADDRESS=SERVER_ADDRESS;