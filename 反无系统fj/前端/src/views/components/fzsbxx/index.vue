<template>
    <div class="lay-box-content">
      <div class="product-container" v-if="tableData && tableData.length">
        <el-card class="product-card" v-for="item in tableData" :key="item.id" @click="detailOpen(item)">
          <div class="product-content">
            <div class="product-image">
              <el-image
                :src="'/wrj-api/sys/common/static/'+item.tp"
                :alt="item.mc"
                class="product-image-item"
              ></el-image>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ item.mc }}</h3>
              <p class="product-category">分类: {{ item.fl }}</p>
              <p class="product-quantity">数量: {{ item.sl?item.sl:'/' }}</p>
            </div>
          </div>
        </el-card>
      </div>
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
    <detailInfoDialog ref="detailInfoDialog" :column="column"></detailInfoDialog>
</template>

<script>
import http from "@/utils/request.js"
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
      dataX: [],
      names: ["按年统计", "按季统计", "按月统计", "按周统计", "按日统计"],
      column: [],
      params: {},
      detailTitle: "",
      tableData: [],
      dataY: [350,400,342,200,350],
      column:[
         { prop: "fl", label: "分类", span: 12},
        { prop: "mc", label: "名称", span: 12 },
        { prop: "sl", label: "装备数量", span: 12 },
        { prop: "gntd", label: "功能特点" , span: 12},
        { prop: "jszb", label: "技术指标" , span: 12},
        { prop: "zbly", label: "装备来源", span: 12 },
        { prop: "remark", label: "备注", span: 24 },
        { prop: "tp", label: "装备图册", span: 24 },
      ],
    };
  },
  mounted() {
    this.getList();
  },
  onUnmounted(){
    clearInterval(this.zcsbTimer);
    this.zcsbTimer= null;
  },
  methods: {
    // 容器自动滚动
    autoScroll(dom,time,length,timer){
      if(timer) clearInterval(timer);
      let flag = true;
      const box = document.querySelector(dom);
      if(box && box.scrollHeight>box.clientHeight){
        timer = setInterval(() => {
          if(box.scrollTop == box.scrollHeight-box.clientHeight) {
            box.scrollTop = 0
            this.tableData.push(this.tableData[0])
            this.tableData.splice(0,1)
          }else{
            if(flag) box.scrollTop += length;
          }
        }, time);
        // 监听鼠标移入事件
        box.addEventListener('mouseenter', ()=>{
          flag = false;
        });
        // 监听鼠标移出事件
        box.addEventListener('mouseleave', ()=>{
          flag = true;
        });
        // 禁用鼠标滚动，防止位置偏移
        box.addEventListener('wheel', function(event) {
          event.preventDefault();
        }, { passive: false }); // 设置passive为false以允许调用preventDefault
      }
      return timer;
    },
    // 获取页面数据
    getList() {
      window.API.fzzb.list({
        pageNo: 1,
        pageSize: 10000,
        order:'descs',
        column:'cjsj',
        fl:'反制设备'
      }).then(res=>{
            if(res.code == 200){
              this.tableData = res.result.records;
              
            }
          })
    },
    detailOpen(item){
      this.$nextTick(()=>{
        this.$refs.detailInfoDialog.open(item)
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
.table-header{
  display:flex;
  align-items:center;
  
}
.color1{
          color: #09eb09;
        }
        .color2{
          color: red;;
        }
        .color3{
          color:yellow;
        }
:deep .kygjqk_table th.el-table__cell{
  background: #0a6fd3 !important;
}
:deep .kygjqk_table .el-table__body-wrapper tr td.el-table-fixed-column--right{
  background: #0a6fd3 !important;
}
:deep .kygjqk_table .el-table__header-wrapper tr th.el-table-fixed-column--right{
  background: #0a6fd3 !important;
}

.product-container {
  display: flex;
  justify-content: space-between;
  width:100%;
  height:100%;
  flex-wrap: wrap;
}

.product-card {
  overflow: hidden;
  width:49%;
  height:103px;
  cursor:pointer;
  margin-bottom:10px;
}

.product-content {
  display: flex;
  align-items: center;
  color: #fff;
}

.product-image {
  width: 80px;
  height: 80px;
  background-color: #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
}

.product-image-item {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
  margin-left: 15px;
}

.product-name {
  font-size: 10px;
  font-weight: bold;
  margin-bottom: 5px;
}

.product-category {
  font-size: 12px;
  margin-bottom: 5px;
}

.product-quantity {
  font-size: 12px;
  margin-bottom: 5px;
}

.product-source {
  font-size: 10px;
  color: #999;
}
:deep(.el-card){
  border:1px solid #3661b7;
}
:deep(.el-card__body){
  padding:10px;
}
</style>