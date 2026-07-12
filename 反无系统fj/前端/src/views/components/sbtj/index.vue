<template>
    <div class="lay-box-content">
      
      <div
        class="echars"
        ref="bjlxfbEchartsRefBar"
        v-if="chartData && chartData.length"
      ></div>
      <div v-else>
        <img
          style="
            width: 170px;
            height: 105px;
            margin-left: 50%;
            margin-top: 4%;
            transform: translate(-50%, 0);
          "
          src="@/assets/noData.png"
          alt=""
        />
      </div>
    </div>
  <detailInfoDialog ref="detailInfoDialog" :title="detailTitle+'详情'" apiUrl="/wrj-api/uav/uavDeviceConfig/list" :params="params" :column="column"></detailInfoDialog>
</template>

<script>
import http from "@/utils/request.js"
import detailInfoDialog from '@/views/components/bjlxfb/detailInfoDialog.vue'
import { ElMessage } from "element-plus";
export default {
  components: {
    detailInfoDialog
  },
  data() {
    this.listEcharts = null
    return {
      form: {},
      currentIndex: 0,
      dataX: [],
      names: ["按年统计", "按季统计", "按月统计", "按周统计", "按日统计"],
      column: [],
      params: {},
      detailTitle: "",
      dataList: [],
      dataY: [350,400,342,200,350],
      chartData: []
    };
  },
  mounted() {
    this.getList();
  },
  methods: {
    // 点击按钮
    clickButton(index) {
      this.currentIndex = index;
      this.echarts(this.dataX,this.dataY);
    },
    // 获取页面数据
    getList() {
      window.API.sbgl.getSbCountByLx({
        type: "DETECT,DISTURB,TRAP"
      }).then(res=>{
        if(res.success){
          // this.dataX = res.result.xdata;
          // this.dataY = res.result.map(item=>{return item.value});
          // let arrX=['未连接','已连接','告警中']
          // let arrY = {
          //   '侦测设备':[1,0,1],
          //   '诱骗设备':[1,0,1],
          //   '干扰设备':[1,0,1],
          // }
          var data = []
          var typeArr = ['未连接','已连接','告警中']
          for(var i in res.result.xdata){
            var arr = []
            for(var key of typeArr){
              arr.push(res.result.ydata[key=='未连接'?'DISCONNECTED':key=='已连接'?'CONNECTED':'WARN'][i])
            }
            data.push({
              "sl": arr.join(),
              "nd": res.result.xdata[i]=='DETECT'?'侦测设备':res.result.xdata[i]=='TRAP'?'诱骗设备':'干扰设备',
              "lxmc": typeArr.join() // (Object.keys(res.result.ydata)).join()
            })
          }
          this.chartData = data
          this.$nextTick(()=>{
            this.initChart(this.chartData)
          })
        }
      })
    },
    initChart(arr,chartType='bar',echartName='bjlxfbEchartsRefBar') {
      if(!this.chartData.length) return;
      console.log(this.$refs[echartName]);
      
      // 判断标签中存在方法的话，先清空
      if (window.echarts.init(this.$refs[echartName]))
        window.echarts.init(this.$refs[echartName]).clear();

      this.listEcharts = window.echarts.init(this.$refs[echartName]);
      this.listEcharts.off("click");
      const option = {
        tooltip: {
          show: true,
          trigger: "axis",
          showDelay: 0,
          hideDelay: 0,
          // position: function (pt) {
          //     return [pt[0]<300?pt[0]:300, 0];
          // },
          axisPointer: {
            // 坐标轴指示器，坐标轴触发有效
            type: 'shadow', // 默认为直线，可选为：'line' | 'shadow'
          },
        },
        legend: {
          // right: '0',
          data: arr[0].lxmc.split(","),
          itemGap: 5,
          textStyle: {
            color: "#fff",
          },
          // type:'scroll',
        },
        grid: {
          top: 40,
          left: 20,
          right: 10,
          bottom: 0,
          containLabel: true,
        },
        xAxis: {
          type: "category",
          axisLabel: {
            // interval: 1,
            rotate: arr.length>5?25:0,
            color: '#fff',
            fontSize: '12',
          },
          axisLine: {
            lineStyle: {
              color: "#fff",
            },
          },
          data: arr.map(item=>{return item.nd}),
        },
        yAxis: {
          name: '数量',
          nameLocation: 'end', // 或者使用 'start'、'middle'
          type: "value",
          minInterval: 1,
          axisLine: {
            lineStyle: {
              color: "#fff",
            },
          },
          splitLine: {
            show: true,
            lineStyle: {
              type: "dashed",
              color: "rgba(255,255,255,.2)"
            },
          },
        },
        series: [],
      };
      const seriesArr=[];
      for(var j in arr[0].lxmc.split(",")){
        // const letters = '0123456789ABCDEF';
        // let color = '#';
        // for (let i = 0; i < 6; i++) {
        //     color += letters[Math.floor(Math.random() * 16)];
        // }
        var arr1 = []
        arr.forEach(item => {
          arr1.push(item.sl.split(",")[j])
        });
        seriesArr.push({
            name: arr[0].lxmc.split(",")[j],
            data: arr1,
            type: chartType,
            // itemStyle: {
            //   normal: {
            //     color: color,
            //   }
            // },
        })
      }
      option.series=seriesArr;
      this.listEcharts.setOption(option);
      this.listEcharts.on('click',(e)=>{
        console.log(e);
        this.detail({
          // status: e.seriesName,
          deviceType: e.name=="侦测设备"?"DETECT":e.name=="诱骗设备"?"TRAP":"DISTURB"
        })
      })
       // 让echarts图表自适应
      window.addEventListener("resize", () => {
        this.listEcharts.resize();
      });
    },
    detail(params = {deviceType: "TRAP"}){ // 默认设置类型为诱骗
      this.column = [
        { prop: "name", label: "设备名称", width: 150 },
        { prop: "deviceId", label: "设备唯一标识", width: 150 },
        { prop: "stationId", label: "站ID" },
        { prop: "deviceType", label: "设备类型", width: 100 },
        { prop: "deviceIp", label: "设备IP地址", width: 130 },
        { prop: "devicePort", label: "设备端口" , width: 100},
        { prop: "isValid", label: "有效性" },
        { prop: "status", label: "连接状态", width: 100 },
        { prop: "jd", label: "经度", width: 120 },
        { prop: "wd", label: "纬度", width: 120 },
        { prop:"zcbj",label: "侦测半径", width: 100},
        { prop: "dyqk", label: "弹药情况", width: 180 },
      ]
      this.params = params
      this.params.order="descs";
      this.params.column="updateTime"
      this.$nextTick(()=>{
        this.$refs.detailInfoDialog.open()
      })
    }
  },
};
</script>

<style scoped lang="less">
.lay-box-content{
  width:100%;
  height:100%;
  box-sizing: border-box;
  padding: 0 8px 18px 8px;
}
.echars {
  width: 100%;
  height: calc(100% - 10px);
}
.button {
  width: 90%;
  margin: 5px auto;
}
.buttonChild {
  padding: 3px 8px;
  background: #6788c2;
  border: 1px solid #478ad5;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  color:#fff;
}
.buttonChildHover {
  padding: 3px 8px;
  background: #0a6fd3;
  border: 1px solid #478ad5;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  color:#fff;
}
.content {
  width: 100%;
  justify-content: space-between;
  display: flex;
  align-items: center;
  margin-top: 5px;
}
</style>