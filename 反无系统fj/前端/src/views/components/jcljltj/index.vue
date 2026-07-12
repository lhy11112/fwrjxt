<template>
    <div class="lay-box-content">
      <div class="button">
        <!-- <div class="content">
          <span
            :class="currentIndex == index ? 'buttonChildHover' : 'buttonChild'"
            v-for="(item, index) in names"
            @click="clickButton(index)"
            :key="index"
            >{{ item }}</span
          >
        </div> -->
      </div>
      <div
        class="echars"
        ref="bjlxfbEchartsRefBar"
        v-if="dataX && dataX.length > 0"
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
      http.get("/wrj-api/uav/uavDeviceConfig/getzcsbCount",{}).then(res=>{
        if(res.success){
          this.dataX = res.result.map(item=>{return item.title});
          this.dataY = res.result.map(item=>{return item.value});
          this.$nextTick(() => {
            if (this.dataX && this.dataX.length > 0) {
              this.echarts(this.dataX, this.dataY);
            }
          });
        }
      })
    },
    echarts(dataX, dataY) {
      if (window.echarts.init(this.$refs.bjlxfbEchartsRefBar)){
        window.echarts.init(this.$refs.bjlxfbEchartsRefBar).clear();
      }
      this.listEcharts = window.echarts.init(this.$refs.bjlxfbEchartsRefBar);
      const option = {
        tooltip: {
          trigger: "item",
          formatter: function(params) {
            return (params.name=='CONNECTED'?'已连接':'未连接') + '\n' + params.value;
          }
        },
        grid: {
          top: "30px",
          left: 20,
          right: "10px",
          bottom: 10,
          containLabel: true,
        },
        xAxis: {
          type: "category",
          axisLine: {
            lineStyle: {
              color: "#fff",
            },
          },
          axisLabel: {
            // 坐标轴文字设置
            // rotate: 20, //值>0向右倾斜，值<0则向左倾斜
            textStyle: {
              color: "#fff",
            },
            formatter: function(value) {
              // 这里可以根据value的值来格式化显示内容
              // 例如，将数值转换为百分比形式
              return value=='CONNECTED'?'已连接':'未连接';
            }
          },
          data: dataX,
        },
        yAxis: {
          name: "数量",
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
              color: "rgba(255,255,255,.2)",
            },
          },
        },
        series: [],
      };
      var seriesArr = [
        {
          name: "",
          data: dataY,
          type: "bar",
          barWidth: 25,
          color:"#238ddf",
          label: {
            show: true,
            position: "top",
            color: "#fff",
            fontSize: 14,
          },
        },
      ];
      option.series = seriesArr;
      // // 如果数据量大于 6，添加 dataZoom
      // if (this.dataX.length > 6) {
      //   option.dataZoom = [
      //     {
      //       type: "slider", // 滑块型数据区域缩放组件
      //       show: true, // 如果需要的话可以设置 show：false 隐藏组件
      //       start: 0, // 数据窗口范围的起始百分比
      //       end: 50, // 数据窗口范围的结束百分比
      //       orient: "horizontal", // 垂直方向的滑动条
      //       height: "5%", // 组件的高度
      //     },
      //   ];
      // }
      this.listEcharts.setOption(option);
      // 让echarts图表自适应
      window.addEventListener("resize", () => {
        this.listEcharts.resize();
      });
      //   图表点击事件
      this.listEcharts.on("click", (e) => {
        console.log(e);
        this.detail({
          status: e.name,
          deviceType: "DETECT"
        })
      });
    },
    detail(params = {deviceType: "DETECT"}){ // 默认设置类型为诱骗
      this.column = [
        { prop: "name", label: "设备名称", width: 180 },
        { prop: "deviceId", label: "设备唯一标识" },
        { prop: "stationId", label: "站ID" },
        { prop: "deviceType", label: "设备类型" },
        { prop: "deviceIp", label: "设备IP地址" },
        { prop: "devicePort", label: "设备端口" },
        { prop: "isValid", label: "有效性" },
        { prop: "status", label: "连接状态" },
        { prop: "jd", label: "经度" },
        { prop: "wd", label: "纬度" },
      ]
      this.params = params
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