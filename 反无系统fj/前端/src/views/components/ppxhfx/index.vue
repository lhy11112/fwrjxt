<template>
  <div class="lay-box-content" style="height: 100%;">
      
      <div class="mainContent" v-if="chartData && chartData.length>0">
        <div style="width: 100%;height: 100%;" ref="echrtas"></div>
       <!-- <img src="@/assets/allImage/11.png" style="width:90%;height:100%;margin:30px"/> -->
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
  <detailInfoDialog ref="detailInfoDialog" :title="detailTitle+'详情'" apiUrl="/wrj-api/wrj/wjbdWrjJbxx/list" :params="params" :column="column"></detailInfoDialog>
</template>

<script>
import http from "@/utils/request.js"
import { init } from "echarts"
import { ElMessage } from "element-plus";
import detailInfoDialog from './detailInfoDialog.vue'
export default {
  components: {
    detailInfoDialog
  },
  data() {
    this.listEcharts = null
    return {
      form: {},
      currentIndex: 0,
      column: [],
      params: {},
      detailTitle: "",
      dataList: [],
      chartData:[]
    };
  },
  mounted() {
     this.getList();
  },
  methods: {
    // 点击按钮
    clickButton(index) {
      this.currentIndex = index;
    },
    // 获取页面数据
    getList() {
      http.get("/wrj-api/wrj/wjbdWrjJbxx/getWrjCount",{}).then(res=>{
        if(res.success){
          // console.log(res);
          this.chartData = [];
          for(var i of res.result){
            this.chartData.push(Object.assign(i,{
              name: i.brand + "~" + i.model,
              value: i.num
            }))
          }
          this.$nextTick(()=>{
            this.initZtChart(this.chartData);
          })
        }
      })
    },
    initZtChart(item,echartName='echrtas') {
      // 判断标签中存在方法的话，先清空
      if (init(this.$refs[echartName]))
        init(this.$refs[echartName]).clear();
      this.listEcharts = init(this.$refs[echartName]);
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
        data.push(scaleData[i]);
      }
      var seriesObj = [
        {
          name: "",
          type: "pie",
          clockWise: false,
          center: ["35%", "50%"],
          radius: ["35%", "50%"],
          hoverAnimation: false,
          itemStyle: {
            normal: {
              label: {
                show: false,
                position: "outside",
                color: "#fff",
                formatter: function (params) {
                  var percent = 0;
                  var total = 0;
                  for (var i = 0; i < scaleData.length; i++) {
                    total += scaleData[i].value;
                  }
                  percent = ((params.value / total) * 100).toFixed(0);
                  // var row = scaleData.filter(row=>{
                  //   return row.name == params.name
                  // })[0]
                  if (params.name !== "") {
                    return params.name  + "\n" + percent + "%";
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
            return params + ' ' + '{value|' + row.value + '架' + '}';
          },
          textStyle: {
            color: "#fff",
            rich: {
              value: {
                color: '#2475d4' // 设置字体颜色
              }
            }
          }
        },
        toolbox: {
          show: false,
        },
        series: seriesObj,
      };
      
      this.listEcharts.setOption(optionOne);
      this.listEcharts.on('click',(e)=>{
        console.log(e);
        var params = {
          brand: e.data.brand,
          model: e.data.model
        }
        this.detail(params)
      })
       // 让echarts图表自适应
      window.addEventListener("resize", () => {
        this.listEcharts.resize();
      });
    },
    detail(params = {}){
      this.column = [
        { prop: "serialNumber", label: "序列号", width: 180 },
        { prop: "brand", label: "品牌" },
        { prop: "model", label: "型号" },
        { prop: "status", label: "状态" },
        { prop: "authStatus", label: "授权状态" },
        { prop: "currentLongitude", label: "当前经度" },
        { prop: "currentLatitude", label: "当前纬度" },
        { prop: "currentAltitude", label: "当前高度" },
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
.mainContent{
  width: 100%;
  height: 100%;
  display: flex;
  flex-wrap: wrap;
  .wrjItem{
    width: 18%;
    margin-top: 5px;
    margin-left:calc(10% / 6);
  }
}
</style>