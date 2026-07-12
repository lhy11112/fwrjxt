<template>
    <div class="lay-box-content">
      <div style="display: flex;align-items: center;width: calc(100% - 15px);margin: 0 auto;">
        <el-date-picker
          style="width: 120px;min-width: 90px;margin-right:10px;"
          v-model="queryInfo.nf"
          type="year"
          value-format="YYYY"
          placeholder="请选择"
          @change="getList"
        />
        <el-date-picker
          style="width: 80px;min-width: 60px;margin-right:10px;"
          v-model="queryInfo.yf"
          type="month"
          format="M"
          value-format="M"
          placeholder="请选择"
          @change="getList"
        />
        <!-- <el-select v-model="queryInfo.stationId" filterable clearable style="width: 30%;" @change="getList">
          <el-option v-for="(t,i) in sbDataList" :key="i" :label="t.name" :value="t.stationId"></el-option>
        </el-select> -->
        <el-radio-group v-model="queryInfo.type" style="margin-left: 10px;" @change="getList">
          <el-radio-button label="型号"></el-radio-button>
          <el-radio-button label="序列号"></el-radio-button>
        </el-radio-group>
    </div>
      <!-- <div class="button">
        <div class="content">
          <span
            :class="currentIndex == index ? 'buttonChildHover' : 'buttonChild'"
            v-for="(item, index) in names"
            @click="clickButton(index)"
            :key="index"
            >{{ item }}</span
          >
        </div>
      </div> -->
      <div
        class="echars"
        ref="bjlxfbEchartsRefBar"
        v-if="chartData && chartData.length > 0"
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
  <detailInfoDialog ref="detailInfoDialog" :title="detailTitle+'详情'" apiUrl="/wrj-api/uav/uavDetectMsg/pageList" :params="params" :column="column"></detailInfoDialog>
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
      queryInfo: {
        type: "型号"
      },
      sbDataList: [],
      chartData: []
    };
  },
  mounted() {
    this.queryInfo.nf = (new Date().getFullYear()).toString();
    this.queryInfo.yf=window.TOOL.dateFormat(new Date(),"MM")
    
    // http.get("/wrj-api/uav/uavDeviceConfig/list",{
    //   pageNo: 1,
    //   pageSize: 99
    // }).then(res=>{
    //   if(res.success){
    //     this.sbDataList = res.result.records;
    //     // if(this.sbDataList.length){
    //     //   this.queryInfo.stationId = this.sbDataList[0].stationId;
    //     // }
    //     this.getList();
    //   }
    // })
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
      http.get("/wrj-api//uav/uavDetectMsg/wrjfxcstj",this.queryInfo).then(res=>{
        if(res.success){
          this.chartData = JSON.parse(JSON.stringify(res.result).replaceAll("serial","name").replaceAll("model","name").replaceAll("num","value"));
          this.$nextTick(()=>{
            this.initChart(this.chartData);
          })
        }
      })
    },
    initChart(item,echartName='bjlxfbEchartsRefBar') {
      // 判断标签中存在方法的话，先清空
      if (window.echarts.init(this.$refs[echartName]))
        window.echarts.init(this.$refs[echartName]).clear();
      this.listEcharts = window.echarts.init(this.$refs[echartName]);
      this.listEcharts.off("click");
      var optionOne;
      // var numSl = 0;
      var scaleData = [];
      if (item) {
        scaleData = item;
      } else {
        scaleData = [];
      }
      var rich = {
        white: {
          color: "#ddd",
          align: "center",
          // padding: [3, 0],
        },
      };
      var data = [];
      // var color = ["#F8C8B8", "#F28A72", "#5389FA", "#CFDEFF", "#94B3FA"];
      for (var i = 0; i < scaleData.length; i++) {
        data.push(
          {
            value: scaleData[i].value,
            name: scaleData[i].name,
            itemStyle: {
              normal: {
                // borderWidth: 7,
                // shadowBlur: 10,
                // borderRadius: 5,
                // borderColor: color[i],
                // shadowColor: color[i],
              },
            },
          },
          // {
          //   value: 2,
          //   name: "",
          //   itemStyle: placeHolderStyle,
          // }
        );
      }
      var seriesObj = [
        {
          name: "",
          type: "pie",
          clockWise: false,
          center: ["30%", "50%"],
          // radius: echartName == 'echrtas' ? ["60%", "75%"] : ["45%", "60%"],
          hoverAnimation: false,
          itemStyle: {
            normal: {
              label: {
                show: false,
                position: "outside",
                color: "#fff",
                formatter: function (params) {
                  // var percent = 0;
                  // var total = 0;
                  // for (var i = 0; i < scaleData.length; i++) {
                  //   total += scaleData[i].value;
                  // }
                  // percent = ((params.value / total) * 100).toFixed(0);
                  // var row = scaleData.filter(row=>{
                  //   return row.name == params.name
                  // })[0]
                  if (params.name !== "") {
                    return params.name  + "\n" // + row.ZB + "%";
                  } else {
                    return "";
                  }
                },
                rich: rich,
              },
              labelLine: {
                length: 16,
                length2: 7,
                show: true,
                // color: "#00ffff",
              },
            },
          },
          data: data,
        },
      ];
      optionOne = {
        tooltip: {
          show: true,
        },
        legend: {
          // left: "right",
          right: 'right',
          // align: "left",
          top: "center",
          orient: "vertical",
          show: true,
          data: scaleData.map(item=>{return item.name}),
          type: "scroll",
          textStyle: {
            color: "#fff",
          },
          itemGap: 6,
          formatter: function (params) {
            var row = scaleData.filter(row=>{
              return row.name == params
            })[0]
            return params + "  " + row.value;
          },
        },
        toolbox: {
          show: false,
        },
        // color: color,
        series: seriesObj,
      };
      
      this.listEcharts.setOption(optionOne);
      this.listEcharts.on('click',(e)=>{
        console.log(e);
        var obj = {}
        if(this.queryInfo.type == "序列号"){
          obj.serial = e.name
        }else{
          obj.model = e.name
        }
        this.detail(Object.assign(this.queryInfo,obj))
      })
       // 让echarts图表自适应
      window.addEventListener("resize", () => {
        this.listEcharts.resize();
      });
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
      });
    },
    detail(params = {}){ // 默认设置类型为诱骗
      this.column = [
        { prop: "serial_dictText", label: "无人机品牌", width: 120 },
        { prop: "model", label: "无人机型号", width: 120 },
        { prop: "serial", label: "无人机序列号", width: 180 },
        { prop: "dronLng", label: "无人机经度" },
        { prop: "dronLat", label: "无人机纬度" },
        { prop: "homeLng", label: "起飞点经度" },
        { prop: "homeLat", label: "起飞点纬度" },
        { prop: "pilotLng", label: "遥控器经度" },
        { prop: "pilotLat", label: "遥控器纬度" },
        { prop: "altitude", label: "海拔高度(米)" },
        { prop: "height", label: "高度" },
        { prop: "eastV", label: "东速度" },
        { prop: "northV", label: "北速度" },
        { prop: "upV", label: "上速度" },
        { prop: "freq", label: "频率(U64)" },
        { prop: "rssi", label: "信号强度" },
        { prop: "distance", label: "距离(m)" },
        { prop: "uuid", label: "飞手执照代码" },
        { prop: "angle", label: "飞机角度", width: 100 },
        { prop: "stationId", label: "站ID" },
        { prop: "dataTime", label: "时间戳(U64转换)", width: 150 },
        { prop: "createTime", label: "入库时间", width: 150 },
      ]
      this.params = params;
      this.params.order = "descs",
      this.params.column = "createTime"
      
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
  height: calc(100% - 40px);
  padding: 10px;
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