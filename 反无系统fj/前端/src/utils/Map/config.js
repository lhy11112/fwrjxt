import config from "@/config"
export const cfg3D = {
    server: {
        baseUrl: config.VUE_APP_SUPERMAP_BASE_URL + "/iserver/services",
    },
    viewerOptions: {
        // 是否显示导航控件
        navigation: true,
        // 是否显示信息框
        infoBox: false,
        // 是否显示选取指示器组件
        selectionIndicator: false,
        // 展示渲染错误
        showRenderLoopErrors: false,

        timeline: true,

        animation: true,
        //去掉大气层黑圈
        orderIndependentTranslucency: false,
        contextOptions: {
            webgl: {
                alpha: true
            }
        }
    }
}

export const cfg2D = {
    server: {
        // baseUrl:"http://172.16.235.215:9341/gisData/1805161878653161474/iserver/services"
        // http://172.16.235.215:9341/gisData/1805161878653161474/iserver/services
        // baseUrl: window.config.VUE_APP_SUPERMAP_BASE_URL+"/iserver/services",
        // baseUrl: config.VUE_APP_SUPERMAP_BASE_URL+"/gisData/1805161878653161474", //图层切换地址
        baseUrl1: config.VUE_APP_SUPERMAP_BASE_URL+"/iserver/services",
    }
}