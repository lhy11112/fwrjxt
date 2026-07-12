<template>
  <AnalysisMoveDlg
    title="目标统计分析"
    style="width: 55%; height: 60vh"
    :visibleDialog="visibleDialog"
    @close="closed"
    isModal="true"
  >
    <div class="tb-content">
      <div 
        v-for="(item, index) in mbtjxxData" 
        :key="index" 
        class="chart-wrapper"
      >
        <!-- 图表标题 -->
        <div class="chart-title">
          <span class="title-text">目标分析图表</span>
          <span class="title-line"></span>
        </div>
        <!-- 图表容器 - 确保 ID 唯一 -->
        <div 
          :id="'chart-container-' + index" 
          class="chart-item"
        ></div>
      </div>
      <!-- 空数据提示 -->
      <div v-if="!mbtjxxData || mbtjxxData.length === 0" class="empty-tip">
        暂无目标分析数据
      </div>
    </div>
  </AnalysisMoveDlg>
  <tbDetail v-if="tbDetailVisible" ref="tbDetailRef" title="详情" :column="column" @closed="tbDetailClosed"></tbDetail>
</template>

<script>
import tbDetail from '../portal/simulatedExercise/tbDetail.vue'

export default {
  components: {
    tbDetail
  },
  data() {
    return {
      visibleDialog: false, // 确保这里定义了
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
      tbDetailVisible: false,
      mbtjxxData: [],
      chartInstances: [] // 存储所有图表实例
    };
  },
  mounted() {
    window.addEventListener("resize", this.handleResize);
  },
  beforeDestroy() {
    this.disposeAllCharts();
    window.removeEventListener("resize", this.handleResize);
  },
  methods: {
    open(data) {
      this.visibleDialog = true;
      this.mbtjxxData = data || [];
      
      // 先销毁旧图表
      this.disposeAllCharts();
      
      // 等待对话框 DOM 完全渲染
      // 使用 setTimeout 确保 Dialog 的 transition 动画完成后 DOM 尺寸正确
      this.$nextTick(() => {
        setTimeout(() => {
          this.initCharts();
        }, 100);
      });
    },

    // 初始化所有图表
    initCharts() {
      if (!this.mbtjxxData || this.mbtjxxData.length === 0) {
        return;
      }

      this.mbtjxxData.forEach((item, index) => {
        const data = item.mbtjxx.filter(v=>v.type)
        this.initChart(data, 'bar', index);
      });
    },

    // 销毁所有图表实例
    disposeAllCharts() {
      if (this.chartInstances && this.chartInstances.length > 0) {
        this.chartInstances.forEach(chart => {
          if (chart && typeof chart.dispose === 'function') {
            chart.dispose();
          }
        });
      }
      this.chartInstances = [];
    },

    // 处理窗口大小变化
    handleResize() {
      if (this.visibleDialog) {
        this.chartInstances.forEach(chart => {
          if (chart && typeof chart.resize === 'function') {
            chart.resize();
          }
        });
      }
    },

    initChart(arr, chartType = 'bar', index) {
      const containerId = 'chart-container-' + index;
      const chartDom = document.getElementById(containerId);

      if (!chartDom) {
        console.warn(`未找到图表 DOM 元素：${containerId}`);
        return;
      }

      // 如果该位置已有图表实例，先销毁
      if (this.chartInstances[index]) {
        this.chartInstances[index].dispose();
      }

      // 初始化图表
      // 确保 echarts 已全局引入
      if (!window.echarts) {
        console.error('ECharts 未加载');
        return;
      }

      const chart = window.echarts.init(chartDom);
      this.chartInstances[index] = chart;

      // 处理数据为空的情况
      const xData = arr && arr.length > 0 ? arr.map(item => item.type || '未知') : [];
      const yData = arr && arr.length > 0 ? arr.map(v => v.sl || 0) : [];

      const option = {
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
          show: false,
          data: [`目标分析${index + 1}`],
          itemGap: 5,
          textStyle: {
            color: "#fff",
          },
          top: 25,
        },
        grid: {
          top: 30,
          left: 40,
          right: 20,
          bottom: 30,
          containLabel: true,
        },
        xAxis: {
          type: "category",
          axisLabel: {
            rotate: xData.length > 5 ? 25 : 0,
            color: '#fff',
            fontSize: 10,
            interval: 0 // 强制显示所有标签
          },
          axisLine: {
            lineStyle: {
              color: "rgba(255,255,255,0.5)",
            },
          },
          data: xData,
        },
        yAxis: {
          name: '数量',
          nameTextStyle: {
            color: '#fff',
            fontSize: 10
          },
          type: "value",
          minInterval: 1,
          axisLine: {
            show: false,
          },
          axisLabel: {
            color: '#fff',
            fontSize: 10
          },
          splitLine: {
            show: true,
            lineStyle: {
              type: "dashed",
              color: "rgba(255,255,255,0.1)"
            },
          },
        },
        series: [{
          name: '目标统计',
          data: yData,
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
        console.log('Chart clicked:', e);
        const targetName = (e.name === 'null' || e.name === null || e.name === undefined) ? null : e.name;

        const detailData = (this.mbtjxxData[index].zymbxx || []).filter(v => {
          const currentSubtype = (v.subtype === null || v.subtype === undefined || v.subtype === '') ? null : v.subtype;
          return targetName == currentSubtype;
        });

        this.detail(detailData);
      });
    },

    detail(detailData) {
      this.column = [
        { prop: "mc", label: "名称" },
        { prop: "jd", label: "经度" },
        { prop: "wd", label: "纬度" },
        { prop: "rylx", label: "人员类别" },
        { prop: "type", label: "类型" },
        { prop: "sl", label: "数量" },
        { prop: "zb", label: "装备" },
        { prop: "ssgk", label: "设施概括" },
      ]
      this.tbDetailVisible = true;
      this.$nextTick(() => {
        if (this.$refs.tbDetailRef) {
          this.$refs.tbDetailRef.open(detailData)
        }
      })
    },

    tbDetailClosed() {
      this.tbDetailVisible = false;
    },
    closed(){
      this.visibleDialog= false;
      this.$emit('close')
    }
  }
}
</script>

<style scoped lang="less">
.tb-content {
  width: 100%;
  height: 100%;
  position: relative;
  overflow-y: auto;
  padding: 10px;
  box-sizing: border-box;

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
    padding-left: 5px;
    
    .title-text {
      color: #00d4ff;
      font-size: 13px;
      font-weight: bold;
      margin-right: 10px;
      white-space: nowrap;
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
    height: 420px; // 固定高度，防止塌陷
    background: rgba(255, 255, 255, 0.03);
    border-radius: 4px;
    border: 1px solid rgba(2, 96, 159, 0.3);
    box-sizing: border-box;
  }
}
</style>
