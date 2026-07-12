import { defineStore } from 'pinia';
import { store } from "@/store";
import axios from 'axios'
export const useCounterStore = defineStore({
  id: "counter-store",
  state: () => ({
    start: "",//开始位置的坐标点
    end: "",//结束位置的坐标点
    lineLength: "",//两点之间的距离
    hasLeastEdgeCount: false,//线段的转角
    weightFieldName: "SmLength",//线段的权重
    color: "#409EFF",//线段颜色
    pointLine: null,//范围内的所有点的信息
    pointData: null,//结束点的信息
    startName: "",
    endName: "",
    startDataArr: [],
    address: "",//事件名称
    bduiData: [],//部队信息
    zuiyouData: [],
    // 最近坐标点
    zuijPoint: null,
    km: null,
    endPoint: null,
    dataList: [],
    Isshow: false,
    totalShow: false,
    totalbd: [],
    messageList: {},
    visibleShow: false,
    taskMessage: {},
    userInfo: {},
    // 开始点的位置名称
    nameLits: "",
    Toptitle:"",


    IsshowLine: false,
    resultData:{},
    mapCenterPoint:{},
    mapCenterShow:false,



  }),
  actions: {
    // 获取创建的任务信息
    // getTaskMessage(data){
    //   this.taskMessage=data

    // },
    // 获取用户信息
    // getUserInfo(data){
    //   this.userInfo=data

    // },
    // 获取全部信息
    getMessage(data) {
      this.messageList = data;

    },
    getMapCenterPoint(mapCenterPoint){
      this.mapCenterPoint = mapCenterPoint;
    },
    getMapCenterShow(mapCenterShow){
      this.mapCenterShow = mapCenterShow;
    },
    

    // getvisibleShow() {
    //   this.visibleShow=true
    //   //console.log("789789789789789",this.visibleShow)
    // },
    // GetIsshow(show) {
    //   this.totalShow = show

    // },
    // getTotalBD(data) {
    //   this.totalbd.push(data)

    // },
    // 获取所有部队信息
    bduiDataList(bduiData) {
      this.bduiData = bduiData
      var buduiList = []
      this.bduiData.forEach(item => {
        buduiList.push({
          经度: item.properties.经度,
          维度: item.properties.维度,
          部队番号: item.properties.部队番号,
          部队内码: item.properties.部队内码,
        })
      })
      window.TOOL.data.set('buduiList',buduiList)
      axios({
        url: process.env.VUE_APP_API_BASE_URL + "/jeecg-boot/battledamage/zzll_gjgwry_zhy/cachePathInfo",
        method: "post",
        data: buduiList,
      }).then(() => {
        //console.log(res, "89789797897987")

      })

      //console.log(buduiList, "89897897897897")


    },
    // 获取结束点
    getendName(data) {
      this.endName = data.name;
      this.endPoint = [data.wd, data.jd]

    },

    // 获取最优路径的数据(开始点)
    zuiyouDataList(zuiyouData, point) {
      var num = 0
      this.dataList.push(zuiyouData)
      this.zuiyouData = this.dataList;
      zuiyouData.forEach(item => {
        if (item.properties.SMLENGTH) {
          num += Number(item.properties.SMLENGTH)
        }
      })
      this.lineLength = num;
      this.lineLength = Math.floor(this.lineLength)
      this.zuijPoint = point;
      this.Isshow = true
    },
    // 获取最短路径的经纬度(开始点)
    pointList(point) {
      this.zuijPoint = point

    },
    // 获取两点之间的长度
    lineLengthList(lineLength) {
      this.lineLength = Math.floor(lineLength)

    },
    changepointLine(pointLine, data) {
      // 全部点的信息
      this.pointLine = pointLine
      // 发生点的坐标信息
      this.pointData = data
    },

    // 获取addreee
    getaddress(address, km) {
      this.address = address;
      this.km = km
    },
    // 获取到起点
    startNameGet(startName, item) {
      this.startName = startName;
      this.nameLits = item
    },
    // getLength(zuiyouData) {
    //   var num = 0
    //   this.zuiyouData = zuiyouData
    //   zuiyouData.forEach(item => {
    //     if (item.properties.SMLENGTH) {
    //       num += Number(item.properties.SMLENGTH)
    //     }
    //   })
    //   this.lineLength = Math.floor(num)

    // },



    // 获取头部标题
    getToptitle(data){
      this.Toptitle=data
    }

  },
})
export function useCounterStoreMethods() {
  return useCounterStore(store);
}