import { defineStore } from 'pinia';
import { store } from "@/store";

/*
 * @Version: v0.0.0.9
 * @Author: wjg
 * @Date: 2024-06-04 09:00:00
 */
export const useUserConfiging = defineStore({
    id: "user-config",
    state: () => ({
        userInfo: {},
        logoutDlgFlag: false,
        routerOptions: [],
    }),
    actions: {
        updateLogoutDlgFlag( logoutDlgFlag ) {
            this.logoutDlgFlag = logoutDlgFlag;
        },
        setUserInfo( value ) {
            console.log(value, "dmnmdmnmdmnm");
            this.userInfo = {
                deptName: value.deptName, //部队名称
                bdnm: value.bdnm, //部队内码
                dmnm: value.dmnm,
                dmmc: value.dmmc,
                dmxh: value.dmxh,
                wjbdyhqxList: value.wjbdyhqxList, //所有权限
                id: value.id,
                baseUserInfo: value.baseUserInfo,
                yhjb: value.yhjb, 
                yhjbnm: value.yhjbnm, //用户级别内码 01 武警部队级 01 总队级 02....
                yhmc: value.yhmc,
                sfDmnm: value.sfDmnm,
                sfDmxh: value.sfDmxh,
                sfDmmc: value.sfDmmc,
                sqDmxh: value.sqDmxh,
                sqDmnm: value.sqDmnm,
                sqDmmc: value.sqDmmc,
                qxDmxh: value.qxDmxh,
                qxDmnm: value.qxDmnm,
                qxDmmc: value.qxDmmc,
                bdfh: value.bdfh,
                bdjc: value.bdjc,
                initBdnm: value.initBdnm,
                initSfDmxh: value.initSfDmxh,
                // initSfDmmc: value.initSfDmmc,
                // initSfDmnm: value.initSfDmnm,
                xzsf: value.xzsf,
                dxyy: value.dxyy, //指定查询的平台数据
                jd: value.jd,
                wd: value.wd,
                bdxh: value.bdxh,
                mapLevel: value.mapLevel,
            };
            eventBus.emit("globalDataAlready");
        },
        /*** 更新用户的路由信息 */
        setRouterInfo(routerOptions) {
            this.routerOptions = routerOptions;
        }
    },
})

export function useUserConfigStore() {
    return useUserConfiging(store);
}
