<template>
  <div class="lay-box-content">
      <div class="button">
        <div class="content">
          <!-- <span
            :class="currentIndex == index ? 'buttonChildHover' : 'buttonChild'"
            v-for="(item, index) in names"
            @click="clickButton(index)"
            :key="index"
            >{{ item.name }}</span
          > -->
          <div>
            <span>地区分类：</span>
            <el-select v-model="dqfl" style="width:calc(100% - 70px)" @change="getList" clearable filterable allowCreate>
              <el-option v-for="(item,index) in dqflOption" :key="index" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </div>
          <div>
            <span>种类分类：</span>
            <el-select v-model="zlfl"  style="width:calc(100% - 70px)" @change="getList" clearable filterable allowCreate>
              <el-option v-for="(item,index) in zlflOption" :key="index" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </div>
          <div style="margin-top:5px;">
            <span>类&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;型：</span>
            <el-select v-model="type" style="width:calc(100% - 70px)" @change="getList" clearable filterable allowCreate>
              <el-option v-for="(item,index) in names" :key="index" :label="item.name" :value="item.value"></el-option>
            </el-select>
          </div>
        </div>
      </div>
      
      <div class="mainContent" v-if="wrjData && wrjData.length>0">
        <div class="wrjItem" v-for="(item,index) in wrjData" :key="index">
          <img :src="'/wrj-api/sys/common/static/'+item.tp" style="width:100%;height:50px;" @click="detailOpen(item)" />
          <div style="text-align:center;font-size:14px;color:#fff;word-wrap: break-word;">{{item.brand}}~{{item.model}}</div>
        </div>
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
    <detailInfoDialog ref="detailInfoDialog" :column="column"></detailInfoDialog>
</template>

<script>
import http from "@/utils/request.js"
import detailInfoDialog from './detailInfoDialog.vue'
import { ElMessage } from "element-plus";
export default {
  components: {
    detailInfoDialog
  },
  data() {
    return {
      form: {},
      currentIndex: 0,
      dataX: ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
      names: [{name:"民用",value:'民用'},{name:"军用",value:'军用'}],
      dataList: [],
      dataY: [200,300,400,200,350,500,200,300,400,200,350,500],
      listEcharts:[],
      dqfl:'',
      dqflOption:[
        {label:'美国',value:'美国'},
        {label:'日本',value:'日本'},
        {label:'俄罗斯',value:'俄罗斯'},
      ],
      zlfl:'',
      zlflOption:[
        {label:'固定翼',value:'固定翼'},
        {label:'六旋翼',value:'六旋翼'},
      ],
      type:'',
      wrjData:[
        {
          sptp:require("@/assets/allImage/air/air1.png"),
          spsj:'202/01/05 12:23:32'
        },
        {
           sptp:require("@/assets/allImage/air/air2.png"),
          spsj:'202/01/05 12:23:32'
        },
        {
           sptp:require("@/assets/allImage/air/air3.png"),
          spsj:'202/01/05 12:23:32'
        },
        {
           sptp:require("@/assets/allImage/air/air4.png"),
          spsj:'202/01/05 12:23:32'
        },
        {
           sptp:require("@/assets/allImage/air/air5.png"),
          spsj:'202/01/05 12:23:32'
        },
        {
           sptp:require("@/assets/allImage/air/air6.png"),
          spsj:'202/01/05 12:23:32'
        },
      ],
      column:[
        { prop: "serialNumber", label: "序列号", span: 12 },
        { prop: "brand", label: "品牌", span: 12 },
        { prop: "model", label: "型号", span: 12 },
        { prop: "type", label: "无人机类型", span: 12 },
        { prop: "zdxhsj", label: "最大续航时间", span: 12 },
        { prop: "zdfxsd", label: "最大飞行速度", span: 12 },
        { prop: "zdkzjl", label: "最大控制距离", span: 12 },
        { prop: "zdfxgd", label: "最大飞行高度", span: 12 },
        { prop: "kfdj", label: "抗风等级", span: 12 },
        { prop: "zdhzzl", label: "最大荷载重量", span: 12 },
        { prop: "jscc", label: "机身尺寸", span: 12 },
        { prop: "jszl", label: "机身重量", span: 12 },
        { prop: "dlxt", label: "动力系统", span: 12 },
        { prop: "dwxt", label: "定位系统", span: 12 },
        { prop: "xjcs", label: "相机参数", span: 12 },
        { prop: "tcxt", label: "图传系统", span: 12 },
        { prop: "dcgg", label: "电池规格", span: 12 },
        { prop: "mxdz", label: "三维模型地址", span: 12 },
        { prop: "status", label: "状态", span: 12 },
        { prop: "authStatus", label: "授权状态", span: 12 },
        { prop: "currentLongitude", label: "当前经度", span: 12 },
        { prop: "currentLatitude", label: "当前纬度", span: 12 },
        { prop: "currentAltitude", label: "当前高度", span: 12 },
        { prop: "dy", label: "弹药", span: 24 },
        { prop: "tcnl", label: "探测能力", span: 24 },
        { prop: "zznl", label: "作战能力", span: 24 },
        { prop: "bpjbs", label: "编配及部署", span: 24 },
        { prop: "jj", label: "简介", span: 24 },
        { prop: "tp", label: "图片", span: 12 },
      ],
      beforeColumn:[
        { value: "mc", label: "名称", span: 12 },
        { value: "serialNumber", label: "序列号", span: 12 },
        { value: "brand", label: "品牌", span: 12 },
        { value: "model", label: "型号", span: 12 },
        { value: "type", label: "无人机类型", span: 12 },
        { value: "dqfl", label: "地区分类", span: 12 },
        { value: "zlfl", label: "种类分类", span: 12 },
      ],
      endColumn:[
        { value: "remark", label: "备注", span: 24 },
        { value: "tp", label: "图片", span: 24 },
      ],
      brandOption:[]
    };
  },
  mounted() {
    this.dqflOption = window.WRJXX.dqflOption;
    this.zlflOption = window.WRJXX.zlflOption;
    this.brandOption = window.WRJXX.brandOption;
     this.getList();
  },
  methods: {
    // 点击按钮
    clickButton(index) {
      this.currentIndex = index;
      this.getList()
    },
    // 获取页面数据
    getList() {
      http.get("/wrj-api/wrj/wjbdWrjJbxxgl/list",{pageNo:1,pageSize: 10000,order: "descs",column: "cjsj",type: this.type,dqfl:this.dqfl,zlfl:this.zlfl}).then(res=>{
        if(res.success){
          console.log(res);
          this.wrjData = res.result.records;
        }
        // this.loading=false;
      })
    },
    detailOpen(item){
      console.log(item);
      let brands = this.brandOption.map(v=>v.label);
      if(!brands.includes(item.brand)){
        this.column = [...this.beforeColumn,...window.WRJXX.allzd,...this.endColumn];
      }else if(item.brand.indexOf("彩虹")!=-1 || item.brand.indexOf("凤翎")!=-1 || item.brand.indexOf("翼神")!=-1 ||  item.brand.indexOf("SY-450H")!=-1 || item.brand.indexOf("八旋翼")!=-1 ){
        this.column = [...this.beforeColumn,...window.WRJXX.myggzd,...this.endColumn];
      }else if(item.brand.indexOf("牵牛星")!=-1 || item.brand.indexOf("海盗")!=-1 || item.brand.indexOf("猎户座")!=-1 ||  item.brand.indexOf("海雕")!=-1 || item.brand.indexOf("猎人")!=-1 ){
        this.column = [...this.beforeColumn,...window.WRJXX.ejwrjggzd,...this.endColumn];
      }else {
         this.column =[...this.beforeColumn,...window.WRJXX[item.brand],...this.endColumn];
      }
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
  height:98%;
}
.echars {
  width: 100%;
  height: 70%;
}
.button {
  width: 100%;
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
  flex-wrap:wrap;
  margin-top: 5px;
  color:#fff;
  font-size:14px;
  >span{
    margin-right: 10px;
  }
  >div{
    width:48%;
    display: flex;
  align-items: center;
  }
}
.mainContent{
  width: 100%;
  height: calc(100% - 95px);
    overflow: hidden;
    overflow-y: auto;
  display: flex;
  flex-wrap: wrap;
  .wrjItem{
    width: 18%;
    margin-top: 5px;
    margin-left:calc(10% / 6);
    cursor:pointer;
  }
}
</style>