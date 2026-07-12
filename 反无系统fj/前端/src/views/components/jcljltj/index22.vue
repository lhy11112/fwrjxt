<template>
  <div class="lay-box-content" style="height: 100%">
    <div class="mainContent" v-if="chartData && chartData.length">
      <div style="width: 100%; height: 100%" ref="echrtas"></div>
      <!-- <img src="@/assets/allImage/33.png" style="width:90%;height:100%;margin:10px"/> -->
    </div>

    <div class="mainContent" v-else>
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
</template>

<script>
import { init } from "echarts";
import { ElMessage } from "element-plus";
export default {
  data() {
    this.listEcharts = null;
    return {
      form: {},
      currentIndex: 0,
      dataX: [
        "1月",
        "2月",
        "3月",
        "4月",
        "5月",
        "6月",
        "7月",
        "8月",
        "9月",
        "10月",
        "11月",
        "12月",
      ],
      names: ["按年统计", "按季统计", "按月统计", "按周统计", "按日统计"],
      dataList: [],
      dataY: [200, 300, 400, 200, 350, 500, 200, 300, 400, 200, 350, 500],
      listEcharts: [],
      chartData: [],
    };
  },
  mounted() {
    this.getList();
  },
  methods: {
    // 点击按钮
    clickButton(index) {
      this.currentIndex = index;
      // this.echarts(this.dataX,this.dataY);
    },
    // 获取页面数据
    getList() {
      this.chartData = [
        {
          "sl": "2,3,7",
          "nd": "2025-10-21",
          "lxmc": "检测无人机,拦截无人机,告警无人机"
        },
        {
          "sl": "4,5,3",
          "nd": "2025-10-22",
          "lxmc": "检测无人机,拦截无人机,告警无人机"
        },
        {
          "sl": "6,7,5",
          "nd": "2025-10-23",
          "lxmc": "检测无人机,拦截无人机,告警无人机"
        }
      ];
      this.$nextTick(() => {
        this.initChart(this.chartData);
        console.log(this.chartData);
      });
    },
    initChart(arr=[],chartType='line',echartName='echrtas') {
      if(!this.chartData.length) return;
      // 判断标签中存在方法的话，先清空
      if (init(this.$refs[echartName]))
        init(this.$refs[echartName]).clear();

      this.listEcharts = init(this.$refs[echartName]);
      this.listEcharts.off("click");
      const option = {
        tooltip: {
          show: true,
          trigger: "axis",
          showDelay: 0,
          hideDelay: 0,
          axisPointer: {
            // 坐标轴指示器，坐标轴触发有效
            type: 'shadow', // 默认为直线，可选为：'line' | 'shadow'
          },
        },
        legend: {
          // right: '0',
          data: arr[0].lxmc.split(","),
          // itemGap: 5,
          textStyle: {
            color: "#fff",
          },
        },
        grid: {
          top: 40,
          left: 20,
          right: 5,
          bottom: 10,
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
          name: '单位：次',
          nameLocation: 'end', // 或者使用 'start'、'middle'
          type: "value",
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
          arr1.push(Number(item.sl.split(",")[j]))
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
      console.log(seriesArr);
      option.series=seriesArr;
      this.listEcharts.setOption(option);
      this.listEcharts.on('click',(e)=>{
        console.log(e);
        // console.log(this.chartData);
      })
       // 让echarts图表自适应
      window.addEventListener("resize", () => {
        this.listEcharts.resize();
      });
    },
  },
};
</script>

<style scoped lang="less">
.echars {
  width: 100%;
  height: 100%;
}
.content {
  width: 100%;
  justify-content: space-between;
  display: flex;
  align-items: center;
  margin-top: 5px;
}
.mainContent {
  width: 100%;
  height: 100%;
  display: flex;
  flex-wrap: wrap;
  .wrjItem {
    width: 18%;
    margin-top: 5px;
    margin-left: calc(10% / 6);
  }
}
</style>