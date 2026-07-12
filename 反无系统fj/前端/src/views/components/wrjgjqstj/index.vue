<template>
  <span style="position: absolute;top: 8px;right: 20px;font-size: 18px;color: #ffe48d;">
    <el-icon style="margin-right: 10px;cursor: pointer;" @click.stop="detail()" title="详情"><MoreFilled /></el-icon>
    
  </span>
  <div class="lay-box-content">
    <div style="display: flex;align-items: center;width: 90%;margin: 0 auto;">
      <el-date-picker
          v-if="currentIndex!=4 && currentIndex!=5"
          style="width: 100px;min-width: 90px;margin-right:10px"
          v-model="year"
          type="year"
          format="YYYY"
          value-format="YYYY"
          placeholder="请选择"
          @change="sjChange"
        />
        <el-select v-if="currentIndex==1" v-model="jd" style="width:120px" 
          @change="sjChange">
          <el-option label="第一季度" value="1"></el-option>
          <el-option label="第二季度" value="2"></el-option>
          <el-option label="第三季度" value="3"></el-option>
          <el-option label="第四季度" value="4"></el-option>
        </el-select>
        <el-date-picker
          v-if="currentIndex==2"
          style="width: 120px;min-width: 90px;"
          v-model="yf"
          type="month"
          format="M"
          value-format="M"
          placeholder="请选择"
          @change="sjChange"
        />
        <el-date-picker
          v-if="currentIndex==3"
          style="width: 120px;min-width: 90px;"
          v-model="date"
          type="week"
          format="ww"
          placeholder="请选择"
          :firstDayOfWeek="7"
          @change="sjChange"
        />
        <el-date-picker
          v-if="currentIndex==4"
          style="width: 160px;min-width: 90px;"
          v-model="rq"
          type="date"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          placeholder="请选择"
          @change="sjChange"
        />
        <el-date-picker
          v-if="currentIndex==5"
          style="width: 135px;min-width: 90px;"
          v-model="startDate"
          type="date"
          range-separator="-"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          placeholder="请选择开始时间"
          @change="sjChange"
          :disabled-date="disabledStartDate"
        />
        <el-date-picker
          v-if="currentIndex==5"
          style="width: 135px;min-width: 90px;margin-left:5px"
          v-model="endDate"
          type="date"
          range-separator="-"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          placeholder="请选择开始时间"
          @change="sjChange"
          :disabled-date="disabledDate"
        />
    <el-radio-group @change="lxChange" v-model="lx" style="margin-left: 5px;">
          <el-radio-button label="无人机">无人机</el-radio-button>
          <el-radio-button label="空域">空域</el-radio-button>
        </el-radio-group>
    </div>
    <div class="button">
      <div class="content">
        
        <span
          :class="currentIndex == index ? 'buttonChildHover' : 'buttonChild'"
          v-for="(item, index) in names"
          @click="clickButton(index)"
          :key="index"
          >{{ item.title }}</span
        >
      </div>
    </div>
    <div
      class="echars"
      ref="zzjgEchartsRefLine"
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
  <detailInfoDialog ref="detailInfoDialog" :title="detailTitle+'详情'" apiUrl="/wrj-api/wrj/wjbdWrjGjjl/qstjEj" :params="params" :column="column"></detailInfoDialog>
  <gjDetail ref="gjDetailRef"></gjDetail>
</template>

<script>
import http from "@/utils/request.js"
import { ElMessage } from "element-plus";
import detailInfoDialog from './detailInfoDialog.vue'
import gjDetail from './gjDetail.vue'
export default {
  components: {
    detailInfoDialog,
    gjDetail
  },
  data() {
    this.listEcharts = null
    return {
      form: {},
      currentIndex: 0,
      year: "2025",
      dataX: ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
      names: [{title: "年统计",value: "年"},{title: "季统计",value: "季"},{title: "月统计",value: "月"},{title: "周统计",value: "周"},{title: "日统计",value: "日"},{title: "自定义",value: "自定义"}],
      column: [],
      params: {},
      detailTitle: "",
      dataList: [],
      dataY: [200,300,400,200,350,500,200,300,400,200,350,500],
      lx:"无人机",
      jd:"1",
      yf:'',
      zs:'',
      rq:'',
      date:'',
      startDate:'',
      endDate:'',
    };
  },
  created(){
    this.year = (new Date().getFullYear()).toString();
    this.rq=window.TOOL.dateFormat(new Date(),"yyyy-MM-dd")
    this.yf=window.TOOL.dateFormat(new Date(),"MM")
    this.date=window.TOOL.dateFormat(new Date(),"yyyy-MM-dd")
    this.zs = this.getWeekNumber(new Date(this.rq))
    console.log(this.year);
    this.startDate = window.TOOL.dateFormat(new Date(),"yyyy-MM-dd")
    this.endDate = window.TOOL.dateFormat(new Date(new Date().getTime() + 7 * 24 * 60 * 60 * 1000),"yyyy-MM-dd")
    this.getDetail()
  },
  mounted() {
    this.getList();
    
    let that=this;
    window.eventBus.on("gjMap",function(){
      console.log('ssss');
      that.clearLayer1()
    })
  },
  // beforeDestroy(){
  //   console.log('ssss');
  //   this.clearLayer1()
  // },
  methods: {
    disabledStartDate(date) {
      if (!this.endDate) {
        return false;
      }
      return new Date(date) >= new Date(this.endDate);
    },
    disabledDate(date) {
      if (!this.startDate) {
        return false;
      }
      return new Date(date) <= new Date(this.startDate);
    },
    sjChange(e){
      console.log(e);
      if(this.currentIndex==3){
        this.zs=this.getWeekNumber(e);
      }
      this.getList();
    },
     getWeekNumber(dateStr) {
       // 解析日期字符串
      const date = new Date(dateStr);
      // 获取年份的第一天（1月1日）
      const firstDayOfYear = new Date(date.getFullYear(), 0, 1);
      // 计算两个日期之间的毫秒差
      const diff = date.getTime() - firstDayOfYear.getTime();
      // 计算周数（每周7天）
      const weeks = Math.floor(diff / (7 * 24 * 60 * 60 * 1000)) + 2;
      return weeks;
    },
    lxChange(){
      console.log(this.lx);
      this.getList();
    },
    // 点击按钮
    clickButton(index=0) {
      this.currentIndex = index;
      this.getList()
    },
    // 获取页面数据
    getList() {
      let params={}
      if(this.currentIndex==0){
        params={nf: this.year,type: this.names[this.currentIndex].value,lx:this.lx}
      }else if(this.currentIndex==1){
        params={nf: this.year,type: this.names[this.currentIndex].value,lx:this.lx,jd:this.jd}
      }else if(this.currentIndex==2){
        params={nf: this.year,type: this.names[this.currentIndex].value,lx:this.lx,yf:this.yf}
      }else if(this.currentIndex==3){
        params={nf: this.year,type: this.names[this.currentIndex].value,lx:this.lx,zs:this.zs}
      }else if(this.currentIndex==4){
        params={nf: new Date(this.rq).getFullYear(),type: this.names[this.currentIndex].value,lx:this.lx,rq:this.rq}
      }else if(this.currentIndex==5){
        params={type: this.names[this.currentIndex].value,lx:this.lx,startDate:this.startDate,endDate:this.endDate}
      }
      this.params=params;
      http.get("/wrj-api/wrj/wjbdWrjGjjl/qstj",params).then(res=>{
        if(res.success && res.result){
          this.dataX = res.result.map(item=>{return item.mc});
          this.dataY = res.result// .map(item=>{return item.value});
          this.$nextTick(()=>{
            this.getDetail()
            this.echarts(this.dataX,this.dataY);
          })
        }
      })
      // this.$nextTick(() => {
      //   if (this.dataX && this.dataX.length > 0) {
      //     this.echarts(this.dataX, this.dataY);
      //   }
      // });
    },
    echarts(dataX, dataY) {
      if (window.echarts.init(this.$refs.zzjgEchartsRefLine)){
        window.echarts.init(this.$refs.zzjgEchartsRefLine).clear();
      }
      this.listEcharts = window.echarts.init(this.$refs.zzjgEchartsRefLine);
      this.listEcharts.off("click");
      const option = {
        tooltip: {
          trigger: "item"
        },
        grid: {
          top: 30,
          left: 30,
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
          },
          data: dataX,
        },
        yAxis: {
          name: "单位:次数",
          type: "value",
          minInterval: 1, // 设置y轴最小间隔为1
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
          type: "line",
          smooth: true, // 设置平滑
          barWidth: 15,
          label: {
            show: true,
            position: "top",
            color: "#fff",
            fontSize: 14,
          },
          areaStyle: {
            normal: {
              color: new window.echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: "#0090FF" },
                { offset: 1, color: "rgba(38, 82, 125)" },
              ]),
            },
          },
        },
      ];
      option.series = seriesArr;
      // // 如果数据量大于 6，添加 dataZoom
      if (this.dataX.length > 6) {
        option.dataZoom = [
          // {
          //   type: "slider", // 滑块型数据区域缩放组件
          //   show: true, // 如果需要的话可以设置 show：false 隐藏组件
          //   start: 0, // 数据窗口范围的起始百分比
          //   end: 50, // 数据窗口范围的结束百分比
          //   orient: "horizontal", // 垂直方向的滑动条
          //   height: "5%", // 组件的高度
          // },
           {
              type: "slider",
              height: 12,
              bottom: "4%",
              showDetail: false,
              backgroundColor: "rgba(7, 147, 254,0.1)",
              fillerColor: "rgba(7, 147, 254,0.1)",
              moveHandleSize: 8,
              start: 0,
              end: 30,
              zoomLock: true,
            },
        ];
      }
      this.listEcharts.setOption(option);
      // 让echarts图表自适应
      window.addEventListener("resize", () => {
        this.listEcharts.resize();
      });
      //   图表点击事件
      // this.listEcharts.on("click", (e) => {
      //   console.log(e);
      //   var params = {};
      //   if(this.names[this.currentIndex].value=="年"){
      //     params = {
      //       nf: e.data.nf,
      //       jd: "",
      //       yf: "",
      //       zs: "",
      //       rq: "",
      //     }
      //     this.detailTitle = e.data.nf + "年"
      //   }else if(this.names[this.currentIndex].value=="季"){
      //     params = {
      //       nf: e.data.nf,
      //       jd: e.data.val,
      //     }
      //     this.detailTitle = e.data.nf + "年" + e.name
      //   }else if(this.names[this.currentIndex].value=="月"){
      //     params = {
      //       nf: e.data.nf,
      //       yf: e.data.val,
      //     }
      //     this.detailTitle = e.data.nf + "年" + e.name
      //   }else if(this.names[this.currentIndex].value=="周"){
      //     params = {
      //       nf: e.data.nf,
      //       zs: e.data.val,
      //     }
      //     this.detailTitle = e.data.nf + "年" + e.name
      //   }else if(this.names[this.currentIndex].value=="日"){
      //     params = {
      //       // nf: e.data.nf,
      //       rq: e.data.val,
      //     }
      //     this.detailTitle = e.name
      //   }
      //   this.detail(params)
      // });
    },
    detail(){
      this.column = [
        { prop: "kyid_dictText", label: "空域", width: 180 },
        { prop: "wrjid_dictText", label: "无人机" },
        { prop: "gjlx", label: "告警类型" },
        { prop: "gjfsjd", label: "告警发生经度" },
        { prop: "gjfswd", label: "告警发生纬度" },
        { prop: "gjfssj", label: "告警发生时间" },
        { prop: "clzt", label: "处理状态" },
        { prop: "clsj", label: "处理时间" },
      ]
      this.$nextTick(()=>{
        this.$refs.detailInfoDialog.open()
      })
    },
    getDetail(){
        this.loading = true;
        window.API.wrjgj.qstjEj(this.params).then(res=>{
          console.log(res);
          if(res.code==200){
            this.gjDetail=res.result.records;
            this.addPoint()
          }
        })
    },
    


 addPoint(){
    this.clearLayer1();
    this.gjMarkerLayer = window.L.layerGroup([]);
    this.gjMarkerLayer.addTo(window.Map2D.map);

    this.gjDetail.forEach((item) => {
      // if (item.status === 'CONNECTED') {
      //   this.greenIcon = window.L.icon({
      //     iconUrl: require('@/assets/allImage/sbZc.png'),
      //     iconSize: [25, 25],
      //   });
      // } else {
      //   this.greenIcon = window.L.icon({
      //     iconUrl: require('@/assets/allImage/sbYc.png'),
      //     iconSize: [25, 25],
      //   });
      // }
      if (item.clzt == "已处理") {
          this.greenIcon = window.L.icon({
            iconUrl:'/static/gj1.png',
            iconSize: [25, 25],
          });
      }else{
          this.greenIcon = window.L.icon({
            iconUrl:'/static/gj2.png',
            iconSize: [25, 25],
          });
      }
      if (item.gjfsjd && item.gjfswd) {
        const marker = window.L.marker(
          window.L.latLng(Number(item.gjfswd), Number(item.gjfsjd)),
          {
            icon: this.greenIcon,
          }
        ).addTo(this.gjMarkerLayer);

        const html = `<div style="width:240px;background:rgba(30, 32, 44);padding:10px">
                    <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                      <div style="width:100%;margin: 4px 0;color:#fff;">空域：<span style="color:#fff;">${item.kyid_dictText}</span></div>
                      <div style="width:100%;margin: 4px 0;color:#fff;">无人机：<span style="color:#fff;">${item.wrjid_dictText}</span></div>
                      <div style="width:100%;margin: 4px 0;color:#fff;">告警类型：<span style="color:#fff;">${item.gjlx}</span></div>
                      <div style="width:100%;margin: 4px 0;color:#fff;">经度：<span style="color:#fff;">${item.gjfsjd.toFixed(3)}</span></div>
                      <div style="width:100%;margin: 4px 0;color:#fff;">纬度：<span style="color:#fff;">${item.gjfswd.toFixed(3)}</span></div>
                      <div style="width:100%;margin: 4px 0;color:#fff;">处理状态：<span style="color:#fff;">${item.clzt}</span></div>
                      <div style="width:100%;margin: 4px 0;color:#fff;">处理时间：<span style="color:#fff;">${item.clsj}</span></div>
                    </div>
                  </div>`;
        marker
          .bindPopup(item.gjlx)
          .bindTooltip(html);
          let that=this;
        marker.on("click", function () {
          that.$refs.gjDetailRef.open(item)
        });
        var  markerIcon = L.divIcon({
                  html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>${item.wrjid_dictText+item.gjlx}</div>`,//marker标注
                  className: 'my-div-icon',
                  iconAnchor: [80, -20]//文字标注相对位置
                });
        window.L.marker(
          window.L.latLng(Number(item.gjfswd), Number(item.gjfsjd)),
          {
            icon: markerIcon,
          }
        ).addTo(this.gjMarkerLayer);
      }
    });
  },
  // 清除图层
  clearLayer1 (){
    if (
      this.gjMarkerLayer != undefined &&
      this.gjMarkerLayer != null &&
      this.gjMarkerLayer != ""
    ) {
      console.log('xxxxxxxxx');
      // 清空图层
      this.gjMarkerLayer.clearLayers();
    }
  },

  },
};
</script>

<style scoped lang="less">
.lay-box-content{
  width:100%;
  height:100%;
  box-sizing: border-box;
  padding: 0 0 18px 0;
}
.echars {
  width: 100%;
  height: calc(100% - 75px);
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
  white-space: nowrap;
}
</style>