import config from "./config"
import api from './api'
import tool from './utils/tool'
import http from "./utils/request"
import LayBox from './components/lay-box'
import TOPTITLE from "./views/topTitle/index.vue"
import bottomTitle from "./views/bottomTitle/index.vue"

// 二维地图
import Map2DComp from "@/views/Map2D/index.vue"
// 三维地图
import Map3DComp from "@/views/Map3D/index.vue";
// 引入分析系统封装弹出框
import AnalysisMoveDlg from "@/components/dlgs/analysisMoveDlg.vue";
//文件上传
import wUploadFile from "@/components/wUploadFile"
export default {
	install(app) {
		//挂载全局对象
		app.config.globalProperties.$CONFIG = config;
		app.config.globalProperties.$TOOL = tool;
		app.config.globalProperties.$HTTP = http;
		app.config.globalProperties.$API = api;
		app.component('lay-box', LayBox)
		app.component('TOPTITLE', TOPTITLE)
		app.component('bottomTitle', bottomTitle)
		//文件上传
		app.component("wUploadFile",wUploadFile);
		app.component('AnalysisMoveDlg', AnalysisMoveDlg)
		// 二维地图
		app.component("Map2DComp", Map2DComp)
		// 三维地图
		app.component("Map3DComp", Map3DComp)
	}
}
