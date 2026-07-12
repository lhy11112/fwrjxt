<template>
  <el-dialog :title="title" v-model="visible" destroy-on-close width="704px" class="ct_dialog" @close="visible = false">
    <div v-if="detailInfoObj" class="detailInfo_con">
      <el-row>
        <el-col :span="item.span" v-for="(item, index) in column" :key="index" v-show="item.label != '序号'">
          <!-- v-show="item.label != '图片'" -->
          <div  class="detailInfo_label" :style="{'width': labelWidth + 'px'}">{{item.label}}：</div>
          <div class="detailInfo_text" :style="item.span==24?`text-indent: 0rem;`:''">
            <img v-if="item.label == '图片'" style="width: 160px;height: 100px;cursor:pointer;" :src="'/wrj-api/sys/common/static/'+detailInfoObj[item.value]" alt=""  @click="handlePictureCardPreview(detailInfoObj[item.value])">
            <span v-else-if="item.label == '状态'">{{detailInfoObj[item.value]==1?'正常':detailInfoObj[item.value]==2?'告警':'失联'}}</span>
            <span v-else-if="item.label == '授权状态'">{{detailInfoObj[item.value]==1?'白名单':detailInfoObj[item.value]==2?'黑名单':'未授权'}}</span>
            <span v-else>{{detailInfoObj[item.value]}}</span>
          </div>
        </el-col>
      </el-row>
    </div>
  </el-dialog>
  <el-dialog title="预览" :draggable="true" style="width: 45%;height:60vh;margin-top:10%" v-model="dialogVisible">
    <div
      style="
        height: 100%;
        display: flex;
        justify-content: space-evenly;
        align-items: center;
        padding-bottom: 20px;
      "
    >
      <img style="width: 100%;height:100%;" w-full :src="dialogImageUrl" alt="加载失败" />
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'detailInfoDialog',
  props: {
    title: {
      type: String,
      default: '详情信息'
    },
    // 是否显示
    visibleFlag: {
      type: Boolean,
      default: false
    },
    dataRow: {
      type: Object,
      default: ()=>{}
    },
    labelWidth: {
      type: Number,
      default: 120
    },
    column: {
      type: Array,
      default: ()=>{return []}
    },
    bdnm: String
  },
  data() {
    return {
      visible: false,
      detailListData: {},
      detailInfoFlag: false,
      detailInfoObj: null,
      dialogVisible:false,
      dialogImageUrl:'',
    }
  },
  mounted(){
    
  },
  watch:{
    visibleFlag: {
      deep: true,
      immediate: true,
      handler(x){
        this.visible = x
      }
    }
  },
  methods: {
    open(row){
      if(row){
        this.detailInfoObj = row
      }
      this.visible = true
    },
    handlePictureCardPreview(file) {
      this.dialogImageUrl = '/wrj-api/sys/common/static/'+file;
      this.dialogVisible = true;
    },
  }
}
</script>

<style lang="less" scoped>
@import "@/style/dialog.css";
.detailInfo_con{
  min-height: 62vh;
  font-size: 15px;
  height: 62vh;
    overflow-y: auto;
  .el-col{
    margin-top: 10px;
    display: flex;
    padding: 5px 0;
  }
  .detailInfo_label{
    color: rgba(255, 255, 255, 0.7);
    font-weight: bold;
    width: 120px;
    text-align: right;
  }
  .detailInfo_text{
    width: calc(100% - 120px);
  }
}
</style>
