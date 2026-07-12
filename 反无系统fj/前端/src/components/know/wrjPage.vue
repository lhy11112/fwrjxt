
<template>
  <el-dialog title="无人机管理" v-model="visible" @close="onClose">
    <div class="app-manage">
      <div class="topMange">
        <el-date-picker
          v-model="rq"
          type="date"
          style="width: 100%"
          value-format="YYYY-MM-DD"
          :placeholder="'请选择时间'"
          @change="rqChange"
          @calendar-change="handleCalendarChange"
          @panel-change="handleCalendarChange"
        >
          <template #default="cell">
          <div class="cell" :class="{ current: cell.isCurrent }">
            <span class="text">{{ cell.text }}</span>
            <span v-if="isHoliday(cell)" class="holiday" />
          </div>
        </template>
      </el-date-picker>
      </div>
      
      <div class="tableBox">
              <template v-if="wrjData && wrjData.length>0">
                <div
                  class="modelOne"
                  v-for="(item, index) in wrjData"
                  :class="{ modelList: spanIndex.indexOf(index) > -1 }"
                  @click="clickCurrent(item,index)"
                  :key="index"
                >
                  <div class="sbmc" :title="item.stationName" v-show="item.stationName">{{item.stationName}}</div>
                  <div class="content-top">
                    <img :src="item.authStatus =='1'?'/static/fly1.png':item.authStatus =='2'?'/static/fly2.png':'/static/fly3.png'" style="width:80px;height:50px;"/>
                    <div :class="item.status=='1'?'color1':item.status=='2'?'color2':'color3'">{{item.status=='1'?'正常':item.status=='2'?'告警':'失联'}}</div>
                    
                  </div>
                </div>
              </template>
              
              <el-empty style="width:100%" v-else description="暂无数据" image-size="120" />
            </div>
    </div>

    <template #footer>
        <el-button @click="onClose">取 消</el-button>

        <el-button type="primary" @click="submit()"
          >确定</el-button
        >
    </template>
  </el-dialog>
  <!-- 新建知识库 -->
  <addKnowledge
    ref="addKnowledgeRef"
    @getListDataKnow="getListDataKnow"
  ></addKnowledge>

  <addKnowledge1
    ref="addKnowledgeRef1"
  ></addKnowledge1>
</template>

<script setup>
import { ref, defineExpose, nextTick,defineEmits } from "vue";
import axios from "axios";
import addKnowledge from "./addKnowledge.vue";
import addKnowledge1 from "./addKnowledge1.vue";
import { ElMessage } from "element-plus";
// import {
//   zsklistAll,
//   deleteTable,
//   zskWjListAll,
//   deleteBatchzsk,
//   addUpload,
//   deleteZSK,
// } from "@/api/hsLocal";
// import { uploadZskFile } from "@/api/hsModelLocal";
const typeList = ref(['战法知识库'])
const selectType = ref("战法知识库")

const emit = defineEmits(["closed","success"]);
// 表格数据
const spanIndex = ref([]);
// 上传文件存储变量
const fileListContent = ref([]);
const visible = ref(false);
const holidays = ref([]);
const rq = ref("")
const wrjData = ref([])
//弹框显示的方法
const open = () => {
  visible.value = true;
  rq.value =window.TOOL.dateFormat(new Date(),"yyyy-MM-dd");
    window.eventBus.emit('rq',rq.value)
    handleCalendarChange(rq.value)
    rqChange()
};

const onClose = () =>{
  visible.value = false;
  emit("closed");
}

const isHoliday = ({ dayjs }) => {
  // console.log(dayjs.format('YYYY-MM-DD'));
  return holidays.value.includes(dayjs.format('YYYY-MM-DD'))
}

const handleCalendarChange = (date) => {
  // date 是切换后的年月对应的 Date 对象
      console.log('切换年月:', date);
      // 你可以在这里获取年份和月份
      const year = new Date(date).getFullYear();
      const month = new Date(date).getMonth() + 1; // 月份从 0 开始，所以 +1
      console.log(`当前年份: ${year}, 当前月份: ${month}`);

      window.API.wxdzc.getUavDetectMsgDateByNfYf({
        nf:year,
        yf:month
      }).then(res=>{
        if(res.code==200){
          let data = res.result;
          holidays.value = res.result.map(v=>v.rq);
        }
      })
}

const rqChange = () => {
  getWxdData()
}

const getWxdData = () => {
  window.API.wxdzc.getUavDetectMsgByStationId({
    rq:rq.value
  }).then(res=>{
    // console.log(res);
    if(res.success){
      wrjData.value = res.result;
    }
  })
}


const selection = ref([])
const clickCurrent = (v,i) => {
  spanIndex.value =[];
  selection.value =[];
  spanIndex.value.push(i);
  selection.value.push(v);
}

const submit = () => {
  visible.value = false;
  emit("success",selection.value);
}
defineExpose({ open });
</script>
<style lang="less" scoped>
.app-manage {
  width: 100%;
  height: 57.41vh;
  display: flex;
  flex-direction: column;
  .topMange {
    width: 100%;
    height: 45px;
    margin-top: 10px;
    display: flex;
    .create {
      width: 15%;
      height: 32px;
      background: rgba(0, 128, 255);
      color: #fff;
      display: flex;
      justify-content: center;
      align-items: center;
      border-radius: 5px;
      cursor: pointer;
    }
  }
  .upload-box {
    border: none !important;
    position: relative;
    background: transparent;
    min-height: 55px;
    max-height: 150px;
    display: block;
    border-radius: 0.28vh;
    overflow-y: auto;
    cursor: pointer;
    .file-upload {
      width: 100%;
      height: 100%;
      position: absolute;
      left: 0;
      top: 0;
      opacity: 0;
    }
    input[type="file"] {
      cursor: pointer;
    }
  }
  .titleList {
    width: 100%;
    display: flex;
    justify-content: space-between;
    padding-top: 5px;

    span {
      color: #fff;
      font-size: 18px;
      font-weight: bold;
    }
  }
  .whiteTheme {
    width: 100%;
    flex: 1;
    margin-top: 10px;
  }
}
</style>
<style lang="less" scoped>
/* @import "@/style/dialog.css"; */
:deep .el-upload-dragger {
  background: transparent;
  padding: 10px !important;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none !important;
}
.el-upload__text,
.el-upload__tip {
  color: #fff;
}
.el-upload-list__item-actions > span {
  margin: 0 5px;
  cursor: pointer;
}
:deep .el-upload-list__item:hover {
  background: #7d714154;
}
:deep .el-upload-list {
  display: none;
}
:deep .el-upload,
.avatar {
  width: 100% !important;
  height: 100%;
}
:deep(.upload-demo) {
  border: 1px dashed #aaa !important;
}
::v-deep .my-dlg-body-content {
  padding: 0 10px !important;
}
::v-deep .el-select__wrapper {
  height: 30px !important;
}
.tableBox {
    width: 100%;
    height:calc(100% - 11px);
    // display: flex;
    // justify-content: start;
    // flex-wrap: wrap;
    overflow: auto;
    // flex-direction: column;
    // flex-direction: column;
    &::-webkit-scrollbar {
      display: none !important;
    }

    .modelList {
      border: 1px solid #0c8ced !important;
    }
    .modelOne:hover {
      //    transform: scale(1.05);
      //  transition: 0.5s;
    }
    .modelOne:nth-child(4n) {
      margin-right: 0;
    }
    .modelOne {
      width: 23.5%;
      height: 100px;
      margin-top: 20px;
      padding: 10px;
      box-sizing: border-box;
      position: relative;
      flex-shrink: 0;
      margin-right: 10px;
      box-shadow: 0 0 0 1px rgba(219,197,197, 0.35) inset;
      color: #fff;
      float:left;

      .content-top{
        background-color: rgb(81, 127, 173);
        text-align: center;
        position: relative;
        
        &>div:nth-of-type(1){
          position: absolute;
          bottom:0;
          right: 0;
          font-size: 12px;
        }
        .color1{
          color: #09eb09;
        }
        .color2{
          color: red;;
        }
        .color3{
          color: rgb(71, 70, 70);
        }
      }
      .sbmc{
          // position: absolute;
          // top: 0;
          // right: 0;
          font-size: 12px;
          background-color:#f0f9eb;
          border-color:#e1f3d8;
          color: #67c23a;
          padding: 2px 3px;
          white-space: nowrap; /* 不换行 */
          text-overflow: ellipsis; /* 省略符显示为省略号 */
          overflow: hidden; /* 隐藏溢出的文本 */
        }
      .content-bottom{
        height: 30px;
        line-height: 30px;
        font-size:12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: rgb(0, 40, 93);
        .dotStatus{
          width: 5px;
          height: 5px;
          border-radius: 50%;
          margin-right:5px;
        }
        &>div:nth-of-type(2){
          width:calc(100% - 15px);
          white-space: nowrap; /* 不换行 */
          text-overflow: ellipsis; /* 省略符显示为省略号 */
          overflow: hidden; /* 隐藏溢出的文本 */
        }
        .bg1{
          background-color: #09eb09;
        }
        .bg2{
          background-color: red;
        }
        .bg3{
          background-color: rgb(71, 70, 70);
        }
      }
    }
  }

  .cell {
  height: 30px;
  padding: 3px 0;
  box-sizing: border-box;
}
.cell .text {
  width: 24px;
  height: 24px;
  display: block;
  margin: 0 auto;
  line-height: 24px;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  border-radius: 50%;
}
.cell.current .text {
  background: #626aef;
  color: #fff;
}
.cell .holiday {
  position: absolute;
  width: 6px;
  height: 6px;
  background: var(--el-color-danger);
  border-radius: 50%;
  bottom: 0px;
  left: 50%;
  transform: translateX(-50%);
}
</style>
