<template>
  <div class="tbBox">
    <el-icon class="list-icon" @click="swlSqzkClick"><Minus /></el-icon>
    <div class="tb-title">目标统计分析</div>
    <div class="tb-content">
      <div 
        v-for="(item, index) in mbtjxxData" 
        :key="index" 
        class="chart-wrapper"
      >
        <!-- 图表标题 -->
        <div class="chart-title">
          <span class="title-text">目标分析{{ index + 1 }}图表</span>
          <span class="title-line"></span>
        </div>
        <!-- 图表容器 -->
        <div 
          :ref="'mbtj' + index" 
          :id="'mbtj' + index" 
          class="chart-item"
        ></div>
      </div>

      <!-- 空数据提示 -->
      <div v-if="!mbtjxxData || mbtjxxData.length === 0" class="empty-tip">
        暂无目标分析数据
      </div>
    </div>
  </div>
  <tbDetail v-if="tbDetailVisible" ref="tbDetailRef" title="详情" :column="column" @closed = "closed"></tbDetail>
</template>

<script>
import tbDetail from './tbDetail.vue'
export default {
  props: ['mbtjxxData'],
  components: {
    tbDetail
  },
  data() {
    return {
      form: {},
      currentIndex: 0,
      dataX: [],
      names: ["按年统计", "按季统计", "按月统计", "按周统计", "按日统计"],
      column: [],
      params: {},
      detailTitle: "",
      dataList: [],
      dataY: [350, 400, 342, 200, 350],
      chartData: [],
      tbDetailVisible:false,
      chartInstances: [] // 存储所有图表实例
    };
  },
  watch: {
    mbtjxxData: {
      handler(newVal, oldVal) {
        console.log("数组变化：", newVal);
        // 数据变化时，先销毁旧图表，再创建新图表
        this.disposeAllCharts();
        this.$nextTick(() => {
          this.getChart();
        });
      },
      deep: true
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.getChart();
    });
    // 只添加一次 resize 监听
    window.addEventListener("resize", this.handleResize);
  },
  beforeDestroy() {
    // 组件销毁时清理所有图表实例
    this.disposeAllCharts();
    window.removeEventListener("resize", this.handleResize);
  },
  methods: {
    swlSqzkClick() {
      this.$emit("setTBDlgMinus");
    },
    
    // 销毁所有图表实例
    disposeAllCharts() {
      this.chartInstances.forEach(chart => {
        if (chart && typeof chart.dispose === 'function') {
          chart.dispose();
        }
      });
      this.chartInstances = [];
    },
    
    // 处理窗口大小变化
    handleResize() {
      this.chartInstances.forEach(chart => {
        if (chart && typeof chart.resize === 'function') {
          chart.resize();
        }
      });
    },
    
    getChart() {
      console.log(this.mbtjxxData);
      
      if (!this.mbtjxxData || this.mbtjxxData.length === 0) {
        return;
      }
      
      this.mbtjxxData.forEach((item, index) => {
        // 使用 $nextTick 确保 DOM 已渲染
        this.$nextTick(() => {
          const data = item.mbtjxx.filter(v=>v.type)
          this.initChart(data, 'bar', index);
        });
      });
    },
    
    initChart(arr, chartType = 'bar', index) {
      console.log([`目标分析${index + 1}`]);
      
      // 正确获取 ref（v-for 中的 ref 是数组）
      const dom = this.$refs['mbtj' + index];
      const chartDom = Array.isArray(dom) ? dom[0] : dom;
      
      if (!chartDom) {
        console.warn(`未找到图表 DOM 元素：mbtj${index}`);
        return;
      }
      
      // 如果该位置已有图表实例，先销毁
      if (this.chartInstances[index]) {
        this.chartInstances[index].dispose();
      }
      
      // 初始化图表
      const chart = window.echarts.init(chartDom);
      this.chartInstances[index] = chart;
      
      const option = {
        // title: {
        //   text: `目标分析${index + 1}`,
        //   left: 'center',
        //   top: 0,
        //   textStyle: {
        //     color: '#fff',
        //     fontSize: 14,
        //     fontWeight: 'bold'
        //   }
        // },
        tooltip: {
          show: true,
          trigger: "axis",
          showDelay: 0,
          hideDelay: 0,
          axisPointer: {
            type: 'shadow',
          },
        },
        legend: {
          show:false,
          data: [`目标分析${index + 1}`],
          itemGap: 5,
          textStyle: {
            color: "#fff",
          },
          top: 25,
        },
        grid: {
          top: 55,
          left: 20,
          right: 10,
          bottom: 0,
          containLabel: true,
        },
        xAxis: {
          type: "category",
          axisLabel: {
            rotate: arr.length > 5 ? 25 : 0,
            color: '#fff',
            fontSize: 12,
          },
          axisLine: {
            lineStyle: {
              color: "#fff",
            },
          },
          data: arr.map(item => item.type),
        },
        yAxis: {
          name: '数量',
          nameLocation: 'end',
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
        series: [{
          name: '目标统计',
          data: arr.map(v => v.sl),
          type: chartType,
          itemStyle: {
            color: new window.echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#00d4ff' },
              { offset: 1, color: 'rgba(0, 212, 255, 0.1)' }
            ])
          }
        }],
      };
      
      chart.setOption(option);
      
      // 点击事件
      chart.on('click', (e) => {
          console.log(e);
          // 将 e.name 的 'null' 字符串或 null 值统一视为 null
          const targetName = (e.name === 'null' || e.name === null || e.name === undefined) ? null : e.name;

          const detailData = this.mbtjxxData[index].zymbxx.filter(v => {
              // 将 v.subtype 的空值统一视为 null
              const currentSubtype = (v.subtype === null || v.subtype === undefined || v.subtype === '') ? null : v.subtype;
              
              // 此时直接比较即可，null == null 为 true
              return targetName == currentSubtype;
          });

          this.detail(detailData);
      });

    },
    
    detail(detailData){
      this.column = [
        { prop: "mc", label: "名称"},
        { prop: "jd", label: "经度" },
        { prop: "wd", label: "纬度" },
        { prop: "rylx", label: "人员类别"},
        { prop: "type", label: "类型"},
        { prop: "sl", label: "数量" },
        { prop: "zb", label: "装备" },
        { prop: "ssgk", label: "设施概括" },
      ]
      this.tbDetailVisible = true;
      this.$nextTick(()=>{
        this.$refs.tbDetailRef.open(detailData)
      })
    },
    closed(){
      this.tbDetailVisible = false;
    }

  }
}
</script>

<style scoped lang="less">
.tbBox {
  width: 30%;
  height: 74%;
  position: absolute;
  top: 7%;
  right: 0%;
  border: 1px solid #02609f;
  background: rgba(11, 53, 128, 0.7);
  padding: 15px;
  box-sizing: border-box;
  pointer-events: auto;
  
  .list-icon {
    position: absolute;
    top: 10px;
    right: 10px;
    color: #fff;
    z-index: 2222;
    cursor: pointer;
  }
  
  // 主标题样式
  .tb-title {
    text-align: center;
    color: #fff;
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 15px;
    padding-bottom: 10px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  }
}

.tb-content {
  width: 100%;
  height: calc(100% - 40px);
  position: relative;
  overflow-y: auto;
  
  // 隐藏滚动条
  &::-webkit-scrollbar {
    display: none;
  }
  -ms-overflow-style: none;
  scrollbar-width: none;

   .empty-tip {
    text-align: center;
    color: #fff;
    padding-top: 50px;
    font-size: 14px;
    opacity: 0.6;
  }
}

// 图表包装器
.chart-wrapper {
  width: 100%;
  margin-bottom: 20px;
  
  &:last-child {
    margin-bottom: 0;
  }
  
  // 图表标题样式
  .chart-title {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
    padding-left: 10px;
    
    .title-text {
      color: #00d4ff;
      font-size: 14px;
      font-weight: bold;
      margin-right: 10px;
    }
    
    .title-line {
      flex: 1;
      height: 1px;
      background: linear-gradient(to right, #00d4ff 0%, rgba(0, 212, 255, 0) 100%);
    }
  }
  
  // 图表容器样式
  .chart-item {
    width: 100%;
    height: 200px;
    background: rgba(255, 255, 255, 0.05);
    border-radius: 5px;
    border: 1px solid rgba(2, 96, 159, 0.5);
  }
}
</style>
