import './config'
import { createApp } from 'vue'
import App from './App.vue'
import router from "./router"
import { createPinia } from 'pinia'
import ElementPlus from "element-plus"
import "element-plus/dist/index.css";
import "element-plus/theme-chalk/display.css";
import scui from "./scui";
import Antd, { version } from 'ant-design-vue'
// 引入 Map3D,Map2D并挂载到 window 上
import Map3D from '@/utils/Map/Map3D/index';
import Map2D from '@/utils/Map/Map2D/index'
// import "../public/css/theme.css"
import config from "@/config"
import { init,graphic } from "echarts";
import TOOL from "@/utils/tool.js"
import API from './api';
// import drag from './directive/drag/drag'
// import "@/style/micro.less"
import ElementPlusIconsVue from "@/utils/importAll"
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
// 引入微前端依赖
import microApp from "@micro-zoe/micro-app"
import SSO from "@/cas/sso.js"
// import { block } from "@/Block";
// import "@/Block/style.css";
// import "@/Block/style/main.less";
import eventBus from "@/utils/eventBus"
import webscoket from "@/utils/webscoket";

// SSO.init()
// 将数据挂载到window下面
window.Map3D = Map3D;
window.Map2D = Map2D;
window.config = config;
window.TOOL = TOOL;
window.API = API;
window.echarts = {init:init,graphic:graphic};
window.eventBus= eventBus;
window.WEBSCOKET=webscoket;
const app = createApp(App)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

//vue全局注入echarts
app.config.globalProperties.$echarts = {init:init};
// app.directive('drag', drag)
app.use(createPinia())
app.use(ElementPlus, {
  locale:zhCn
})
app.use(scui);
// app.use(drag)
app.use(router)
app.use(Antd)
microApp.start({
  
})
// app.use(block({
  
// }))

// app.mount('#app')
SSO.init(() => {
  main()
})
function main() {
  const mounted = () => {
      app.mount('#app')
  };
  mounted();
}