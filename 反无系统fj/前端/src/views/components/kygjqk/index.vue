<template>
    <div class="lay-box-content">
      <!-- <div class="table-header">
        <div>
          品牌
        </div>
        <div>
          型号
        </div>
        <div>
          序列号
        </div>
        <div>
          发现站点
        </div>
        <div>
          飞入空域
        </div>
        <div>
          告警类型
        </div>
        <div>
          告警时间
        </div>
      </div>
      <div class="table-content" v-if="tableData && tableData.length">
        <div v-for="(item,index) in tableData" :key="index">
          <div>{{item.wrjpp}}</div>
          <div>{{item.wrjxh}}</div>
          <div>{{item.wrjxlh}}</div>
          <div>{{item.zdmc}}</div>
          <div>{{item.kyid_dictText}}</div>
          <div>{{item.gjlx}}</div>
          <div>{{item.gjfssj}}</div>
        </div>
        
      </div> -->
      <div>
        <el-date-picker
          style="width: 175px;min-width: 90px;"
          v-model="startDate"
          type="date"
          range-separator="-"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          placeholder="请选择开始时间"
          @change="sjChange"
          :disabled-date="disabledStartDate"
        />
        <span style="color:#fff;margin:0 10px;">-</span>
        <el-date-picker
          style="width: 175px;min-width: 90px;"
          v-model="endDate"
          type="date"
          range-separator="-"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          placeholder="请选择开始时间"
          @change="sjChange"
          :disabled-date="disabledDate"
        />
      </div>
      <el-table
        v-if="tableData && tableData.length"
        :data="tableData"
        style="width: 100%;margin-top:10px;height:calc(100% - 50px)"
        class="custom-table kygjqk_table"
        ref="tableRef"
      >
        <!-- <el-table-column type="selection" width="50"></el-table-column> -->
        <el-table-column prop="wrjpp" label="品牌" :show-overflow-tooltip="{effect: 'light'}"></el-table-column>
        <el-table-column prop="wrjxh" label="型号" width="100" :show-overflow-tooltip="{effect: 'light'}"></el-table-column>
        <el-table-column prop="wrjxlh" label="序列号" :show-overflow-tooltip="{effect: 'light'}"></el-table-column>
        <el-table-column prop="zdmc" label="发现站点" width="120" align="center" :show-overflow-tooltip="{effect: 'light'}"></el-table-column>
        <el-table-column prop="kyid_dictText" label="飞入空域" width="100" :show-overflow-tooltip="{effect: 'light'}"></el-table-column>
        <el-table-column prop="gjlx" label="告警类型" width="100" :show-overflow-tooltip="{effect: 'light'}"></el-table-column>
        <el-table-column prop="gjfssj" label="告警时间" width="165" :show-overflow-tooltip="{effect: 'light'}"></el-table-column>
        <el-table-column fixed="right" label="操作" width="60" align="center">
          <template #default="scope">
            <el-button
              style="color: #b7d2ff !important"
              link
              type="primary"
              size="small"
              @click.prevent="detail(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
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
    <secondDialog ref="secondDialog" apiUrl="/wrj-api/wrj/wjbdWrjGjjl/list" :params="params" :column="column"></secondDialog>
</template>

<script>
import http from "@/utils/request.js"
import { ElMessage } from "element-plus";
import secondDialog from './secondDialog.vue'
export default {
  components: {
    secondDialog
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
        { prop: "wrjxlh", label: "序列号", span: 12 },
        { prop: "wrjpp", label: "品牌", span: 12 },
        { prop: "wrjxh", label: "型号", span: 12 },
        { prop: "zdmc", label: "发现站点", span: 12 },
        { prop: "kyid_dictText", label: "入飞空域", span: 12 },
        { prop: "gjlx", label: "告警类型", span: 12 },
        { prop: "gjfssj", label: "告警时间", span: 12 },
        { prop: "status", label: "状态", span: 12 },
        { prop: "authStatus", label: "授权状态", span: 12 },
      ],
      timer:null,
      timer2:null
    };
  },
  mounted() {
    this.startDate = window.TOOL.dateFormat(new Date(),"yyyy-MM-dd")
    this.endDate = window.TOOL.dateFormat(new Date(new Date().getTime() + 7 * 24 * 60 * 60 * 1000),"yyyy-MM-dd")
    this.getList();
    clearInterval(this.timer2);
      this.timer2= null;
    this.timer2 = setInterval(()=>{
      console.log('pk1');
      this.getList(true);
    },3000)
  },
  unmounted(){
    clearInterval(this.timer);
    this.timer= null;
    clearInterval(this.timer2);
    this.timer2= null;
    console.log('xxxxxx12345678',this.timer2);
  },
  methods: {
    // 容器自动滚动
    autoScroll(dom,time,length,timer){
      if(timer) clearInterval(timer);
      let flag = true;
      const box = document.querySelector(dom);
      if(box && box.scrollHeight>box.clientHeight){
        timer = setInterval(() => {
          console.log(box.scrollTop,box.scrollHeight-box.clientHeight);
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
      this.getList();
    },
    // 获取页面数据
    getList(e) {
      window.API.wjbdWrjGjjl.GjQkTJ({
        pageNo: 1,
        pageSize: 10,
        startDate:this.startDate,
        endDate:this.endDate,
        // order:'descs',
        // column:'gjfssj'
      }).then(res=>{
            if(res.code == 200){
              this.tableData = res.result;
              if(!e){
                this.$nextTick(()=>{
                  setTimeout(() => {
                    clearInterval(this.timer);
                    this.timer= null;
                    const height = document.querySelector(".el-table__body tr").offsetHeight;
                    console.log(height);
                    this.timer = this.autoScroll(".el-scrollbar__wrap", 1000, height, this.timer);
                  }, 500); // 延迟500毫秒
                })
              }
            }
          })
    },
    detail(item){ // 默认设置类型为诱骗
    console.log(item);
      this.column = [
        { prop: "wrjpp", label: "品牌",width:120 },
        { prop: "wrjxh", label: "型号",width:120 },
        { prop: "wrjxlh", label: "序列号",width:120 },
        { prop: "gjfsjd", label: "经度",width:120 },
        { prop: "gjfswd", label: "纬度",width:120 },
        { prop: "gjfsgd", label: "高度（m）",width:120 },
        { prop: "zdmc", label: "发现站点" },
        { prop: "kymc", label: "空域名称" },
        { prop: "gjlx", label: "告警类型" },
        { prop: "gjfssj", label: "告警时间" },
      ]
      this.params = {wrjxlh:item.wrjxlh,rq:item.cjsj}
      this.$nextTick(()=>{
        this.$refs.secondDialog.open()
      })
    },
    detailOpen(item){
      this.$nextTick(()=>{
        this.$refs.secondDialog.open(item)
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
:deep .kygjqk_table th.el-table__cell{
  background: #0a6fd3 !important;
}
:deep .kygjqk_table .el-table__body-wrapper tr td.el-table-fixed-column--right{
  background: #0a6fd3 !important;
}
:deep .kygjqk_table .el-table__header-wrapper tr th.el-table-fixed-column--right{
  background: #0a6fd3 !important;
}
</style>