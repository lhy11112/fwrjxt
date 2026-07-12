<template>
  <div class="topCenter">
    <!-- <div class="left">
      <div class="menus" v-for="(item,index) in menu" :key="index" @click="menuClick(index)">
        <div>
          <img :src="item.icon" style="width:34px;height:24px"/>
        </div>
        <div>{{item.name}}</div>
      </div>
    </div>
    <div class="right">
      
    </div> -->
    <div v-show="!mapChange && returnBackFlag" style="position: absolute;top: -10px;left: 30px;display: flex;align-items: center;pointer-events: auto;">
      <div class="ewtyBack" title="返回" @click="returnBack">
        <!-- <img src="@/assets/allImage/returnButton.png" style="width:25px;height:25px;"/> -->
        <el-icon style="font-size:30px;color:#fff;"><RefreshLeft /></el-icon>返回
      </div>
      <el-button style="margin-left:10px;" type="primary" @click="flyKz(true)">开始</el-button>
      <el-button type="primary" @click="flyKz(false)">暂停</el-button>
    </div>
    
    <div v-show="mapChange && tyFlag"  class="buttons">
      <el-button type="primary" @click="flyKz(true)">开始</el-button>
      <el-button type="primary" @click="flyKz(false)">暂停</el-button>
      <el-button type="primary" @click="flyMy()">漫游</el-button>
      <el-button type="primary" @click="return3dBack()">返回</el-button>
    </div>
     <!-- v-show="wxdsbVisible" -->
    <div class="wxdsbBox" id="zcsb" v-show="!tyFlag">
      <div :class="sbToggleVisible?'sbToggleBtn sbToggleBg1':'sbToggleBtn sbToggleBg2'" @click="sbToggleEvent()">
        <img style="width:24px;" :src="sbToggleVisible?'static/toggle/toggle-icon5.png':'static/toggle/toggle-icon4.png'"/>
      </div>
      <div class="title">
        <div>信号来源</div>
        <!-- <el-icon @click="wxdsbVisible = false;" style="cursor:pointer;"><Close /></el-icon> -->
      </div>
      <div class="search">
        <el-input v-model="queryInfo.name" placeholder="请输入无线电设备名称"></el-input>
        <el-button @click="inquires">查询</el-button>
      </div>
      <div v-if="wxdsbData && wxdsbData.length" class="content">
        <div class="wxdsb" v-for="(item,index) in wxdsbData" :key="index">
          <div style="cursor:pointer;" @click="flyDevice(item)">
            <div class="content-top">
              
              <img :src="item.status === 'CONNECTED' &&item.deviceType === 'DETECT'?'/static/zcsb.png':item.status === 'CONNECTED' &&item.deviceType === 'DISTURB'?'/static/grsb.png':item.status === 'CONNECTED' &&item.deviceType === 'TRAP'?'/static/ypsb.png':item.status === 'CONNECTED' &&item.deviceType === 'System'?'/static/system.png':item.status === 'DISCONNECTED' &&item.deviceType === 'DETECT'?'/static/zcsb2.png':item.status === 'DISCONNECTED' &&item.deviceType === 'DISTURB'?'/static/grsb2.png':item.status === 'DISCONNECTED' &&item.deviceType === 'TRAP'?'/static/ypsb2.png':item.status === 'DISCONNECTED' &&item.deviceType === 'System'?'/static/system2.png':item.status === 'WARN' &&item.deviceType === 'DETECT'?'/static/zcsb3.png':item.status === 'WARN' &&item.deviceType === 'DISTURB'?'/static/grsb3.png':item.status === 'WARN' &&item.deviceType === 'TRAP'?'/static/ypsb3.png':item.status === 'WARN' &&item.deviceType === 'System'?'/static/system3.png':''" style="width:80px;height:50px;"/>
              <div :class="item.status=='CONNECTED'?'color1':item.status=='DISCONNECTED'?'color2':'color3'">{{item.status=='CONNECTED'?'已连接':item.status=='DISCONNECTED'?'未连接':'告警中'}}</div>
            </div>
            <div class="content-bottom">
              <div :class="item.status=='CONNECTED'?'dotStatus bg1':'dotStatus bg2'"></div>
              <div :title="item.name+'-'+item.deviceId">{{item.name}}-{{item.deviceId}}</div>
            </div>
          </div>
        </div>
      </div>
      <div  class="content" v-else>
        <img
          style="
            width: 150px;
            height: 105px;
            margin-left: 50%;
            margin-top: 8%;
            transform: translate(-50%, 0);
          "
          src="@/assets/noData.png"
          alt=""
        />
      </div>
      <div>
              <el-pagination
                class="pagination"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="pageOption.pageNo"
                :page-size="pageOption.pageSize"
                layout="total,prev, pager, next"
                :total="total"
              >
              </el-pagination>
            </div>


    </div>

     <!-- v-show="wxdtcVisible" -->
    <div class="wsdtc" id="zcwrjxx" v-if="!returnBackFlag">
      <div :class="wrjToggleVisible?'wrjToggleBtn wrjToggleBg1':'wrjToggleBtn wrjToggleBg2'" @click="wrjToggleEvent()">
        <img style="width:24px;" :src="wrjToggleVisible?'static/toggle/toggle-icon4.png':'static/toggle/toggle-icon5.png'"/>
      </div>
      <div class="title">
        
        <div>
          <img src="/static/toggle/toggle-icon3.png" style="width:20px;vertical-align: middle;"/>
          侦测无人机信息(无人机数量：{{wrjData.length}})
        </div>
        <el-button type="primary" @click="ycqtData" v-show="ycqtBtnVisible">隐藏其他</el-button>
        
        <!-- <el-icon @click="wxdtcVisible = false;" style="cursor:pointer;"><Close /></el-icon> -->
      </div>
      <div class="search">
        <el-date-picker
          v-model="rq"
          type="date"
          style="width: 100%"
          value-format="YYYY-MM-DD"
          :placeholder="'请选择时间'"
          @change="searchChange"
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
        <div style="display:flex;margin-top:10px;width:200px;">
          <el-input v-model="model" placeholder="请输入型号" size="small" clearable @input="modelChange"></el-input>
        </div>
        <div style="margin-top:10px;width:120px;">
          <el-select v-model="authStatus" placeholder="请选择" clearable filterable @change="authStatusChange">
            <el-option v-for="(item,index) in authStatusTypeData" :key="index" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </div>
        <div style="display:flex;">
          <el-checkbox-group v-model="wrjCheckList" @change="wrjCheckChange">
            <el-checkbox label="飞手" value="飞手" />
            <el-checkbox label="无人机" value="无人机" />
            <el-checkbox label="信息" value="信息" />
          </el-checkbox-group>
        </div>
        <div style="color:#fff;">
          正常：{{zcNum}},失联：{{slNum}}
        </div>
      </div>
      <div v-if="wrjData && wrjData.length" class="content">
        <div :class="wrjCurrentIndex==index?'wrjActive wxdsb':'wxdsb'" id="contextMenuDiv" v-for="(item,index) in wrjData" :key="index"  @contextmenu="showMenu">
          <div>
            <div class="content-bottom"  @click="wrjDetail(item,true)">
              <!-- <div :class="item.status=='1'?'dotStatus bg1':item.status=='2'?'dotStatus bg2':'dotStatus bg3'"></div> -->
              <!-- <div :title="item.uavDetectMsg && item.uavDetectMsg.wxdj=='green'?`根据高度：<span style='color:red'>${item.uavDetectMsg?item.uavDetectMsg.height:''}</span>，速度：${item.uavDetectMsg?item.uavDetectMsg.sd:''}，距离：${item.uavDetectMsg?item.uavDetectMsg.distance:''}，综合风险得分（保留 4 位小数）＜ 0.4 时，判定为绿色风险。该得分基于距离（权重 0.4）、速度（权重 0.3）、高度（权重 0.3）三个维度计算：各维度先按特定数值范围和公式算出 0-1 分的维度得分，加权求和后得到综合得分。`:item.uavDetectMsg && item.uavDetectMsg.wxdj=='yellow'?'当综合风险得分（保留 4 位小数）≥ 0.4 且＜ 0.7 时，判定为黄色风险。该得分计算规则为：距离（权重 0.4）、速度（权重 0.3）、高度（权重 0.3）三个维度先按指定数值范围和公式算出 0-1 分的维度得分，再加权求和得到综合得分。':item.uavDetectMsg && item.uavDetectMsg.wxdj=='red'?'当综合风险得分（保留 4 位小数）≥ 0.7 时，判定为红色风险。该得分由距离（权重 0.4）、速度（权重 0.3）、高度（权重 0.3）三个维度指标计算得出：各维度先按特定数值范围和公式算出 0-1 分的维度得分，再加权求和得到综合得分。':'飞行速度≤10m/s,多为低空慢速无人机(如消费级航拍机)，威胁性低，可优先人工驱离'">
                <el-icon style="vertical-align: middle;" size="18" :color="item.uavDetectMsg && item.uavDetectMsg.wxdj?item.uavDetectMsg.wxdj:'green'"><WarnTriangleFilled/></el-icon>
              </div>-->
              <el-tooltip
                effect="light"
                placement="top"
              >
                <!-- 提示内容（支持 HTML 渲染） -->
                <template #content>
                  <div v-if="item.uavDetectMsg?.wxdj === 'green'">
                    根据高度：<span style="color:red">{{ item.uavDetectMsg?.height || 0 }}</span>，速度：<span style="color:red">{{ item.uavDetectMsg?.sd || 0 }}</span>，距离：<span style="color:red">{{ item.uavDetectMsg?.distance || 0 }}</span>，综合风险得分（保留 4 位小数）＜ 0.4 时，判定为绿色风险。<br />该得分基于距离（权重 0.3）、速度（权重 0.4）、高度（权重 0.3）三个维度计算：各维度先按特定<br />数值范围和公式算出 0-1 分的维度得分，加权求和后得到综合得分。
                  </div>
                  <div v-else-if="item.uavDetectMsg?.wxdj === 'yellow'">
                    根据高度：<span style="color:red">{{ item.uavDetectMsg?.height || 0 }}</span>，速度：<span style="color:red">{{ item.uavDetectMsg?.sd || 0 }}</span>，距离：<span style="color:red">{{ item.uavDetectMsg?.distance || 0 }}</span>，综合风险得分（保留 4 位小数）≥ 0.4 且＜ 0.7 时，判定为黄色风险。<br />该得分计算规则为：距离（权重 0.3）、速度（权重 0.4）、高度（权重 0.3）三个维度先按指定<br />数值范围和公式算出 0-1 分的维度得分，再加权求和得到综合得分。
                  </div>
                  <div v-else-if="item.uavDetectMsg?.wxdj === 'red'">
                    根据高度：<span style="color:red">{{ item.uavDetectMsg?.height || 0 }}</span>，速度：<span style="color:red">{{ item.uavDetectMsg?.sd || 0 }}</span>，距离：<span style="color:red">{{ item.uavDetectMsg?.distance || 0 }}</span>，综合风险得分（保留 4 位小数）≥ 0.7 时，判定为红色风险。<br />该得分由距离（权重 0.3）、速度（权重 0.3）、高度（权重 0.4）三个维度指标计算得出：各维度先按特定<br />数值范围和公式算出 0-1 分的维度得分，加权求和得到综合得分。
                  </div>
                  <div v-else>
                    根据高度：<span style="color:red">{{ item.uavDetectMsg?.height || 0 }}</span>，速度：<span style="color:red">{{ item.uavDetectMsg?.sd || 0 }}</span>，距离：<span style="color:red">{{ item.uavDetectMsg?.distance || 0 }}</span>，综合风险得分（保留 4 位小数）＜ 0.4 时，判定为绿色风险。<br />该得分基于距离（权重 0.3）、速度（权重 0.4）、高度（权重 0.3）三个维度计算：各维度先按特定<br />数值范围和公式算出 0-1 分的维度得分，加权求和后得到综合得分。
                  </div>
                </template>

                <!-- 触发提示的元素（原图标部分） -->
                <el-icon style="vertical-align: middle;" size="18" :color="item.uavDetectMsg?.wxdj || 'green'">
                  <WarnTriangleFilled/>
                </el-icon>
              </el-tooltip>
              <!-- <div :title="item.brand+'-'+item.model+'-'+item.serial">{{item.brand}}-{{item.model}}-{{item.serial}}</div> -->
              <div :title="item.brand+'-'+item.model+'-'+item.serial">{{item.model}}</div>
            </div>
            <div class="sbmc" v-show="item.stationName">{{item.stationName}}</div>
            <div class="content-top"  @click="wrjFly(item,index,'click')">
              <div style="position:absolute;right:0;top: 0px;display: flex;align-items: center;">
                {{item.uavDetectMsg && item.uavDetectMsg.jmlx?item.uavDetectMsg.jmlx:''}}
              </div>
              <div v-if="item.authStatus>=4" style="position:absolute;left:5px;bottom: 0px;display: flex;align-items: center;">
                <div :class="item.authStatus=='4'?'dotStatus bg1':item.authStatus=='5'?'dotStatus bg2':'dotStatus bg3'"></div>
                <div>{{item.authStatus=='4'?'待干扰':item.authStatus=='5'?'待诱骗':'持续跟踪'}}</div>
              </div>
              <img :src="item.authStatus =='1'?'/static/fly1.png':item.authStatus =='2'?'/static/fly2.png':'/static/fly3.png'" style="width:80px;height:50px;"/>
              <div style="position: absolute;bottom:0;right: 0;font-size: 12px;" :class="item.status=='1'?'color1':item.status=='2'?'color2':'color3'">{{item.status=='1'?'正常':item.status=='2'?'告警':'失联'}}</div>
              
            </div>
            <div class="content-sx">
              <div>
               <div>
                 <span>频率(Mhz):</span>
                <span>{{item.uavDetectMsg && item.uavDetectMsg.freq?item.uavDetectMsg.freq:0}}</span>
               </div>
               
               <div>
                 <span>更新时间:</span>
                <span>{{item.uavDetectMsg?item.uavDetectMsg.dataTime:''}}</span>
               </div>
              </div>
              <div>
                <div>
                  <span>高度(m):</span>
                  <span>{{item.uavDetectMsg && item.uavDetectMsg.height?item.uavDetectMsg.height:0}}</span>
                </div>

                <div>
                  <span>速度(m/s):</span>
                  <span>{{item.uavDetectMsg && item.uavDetectMsg.sd?item.uavDetectMsg.sd:0}}</span>
                </div>
                

                 <div>
                  <span>距离(m):</span>
                  <span>{{item.uavDetectMsg && item.uavDetectMsg.distance?item.uavDetectMsg.distance:0}}</span>
                </div>
                <div>
                  <span>角度(°):</span>
                  <span>{{item.uavDetectMsg && item.uavDetectMsg.angle?item.uavDetectMsg.angle:0}}</span>
                </div>
              </div>
              <div>
                <div>
                  <span>轨迹颜色:</span>
                  <span>
                    <el-color-picker ref="brightColorRef" @change="gjysChange(item)" v-model="item.color" />
                  </span>
                  
                </div>
                <div>
                  <span>轨迹宽度:</span>
                  <span>
                    <el-input-number style="width:90px" :min="0" controls-position="right" v-model="item.gjWidth" @change="gjysChange(item)"></el-input-number>
                  </span>
                </div>
              </div>
                
            </div>
            
          </div>

          <div v-if="showMenuFlags['flag'+index]" id="contextMenu" :style="{left:menuPosition.x+'px',top:menuPosition.y+'px'}">
            <el-icon style="position:absolute;right:0;top:0;cursor:pointer;" @click="rightMenuClose()"><Close /></el-icon>
            <!-- <div @click="flyKz(true)">开始</div>
            <div @click="flyKz(false)">暂停</div> -->
            <div @click="rightTy2D(item,index)" class="ty-button">二维推演</div>
            <div @click="rightTy(item,index)" class="ty-button ty-3d">三维推演</div>
          </div>
        </div>
      </div>
      <div class="content" v-else>
        <img
          style="
            width: 150px;
            height: 105px;
            margin-left: 50%;
            margin-top: 8%;
            transform: translate(-50%, 0);
          "
          src="@/assets/noData.png"
          alt=""
        />
      </div>
    </div>

    <div v-if="wrjXxxxVisible" class="wsdtc" id="zcwrjXxxx">
      <div :class="wrjDetailToggleVisible?'wrjToggleBtn wrjToggleBg1':'wrjToggleBtn wrjToggleBg2'" @click="wrjDetailToggleEvent()">
        <img style="width:24px;" :src="wrjDetailToggleVisible?'static/toggle/toggle-icon4.png':'static/toggle/toggle-icon5.png'"/>
      </div>
      <div class="title">
        <div>
          <img src="/static/toggle/toggle-icon3.png" style="width:20px;vertical-align: middle;"/>
          侦测无人机详细信息
        </div>
        <!-- <el-icon @click="wxdtcVisible = false;" style="cursor:pointer;"><Close /></el-icon> -->
      </div>
      <div class="content" style="height:calc(100% - 25px);">
        <el-row v-if="detailInfoObj">
          <el-col :span="24" v-for="(item, index) in column" :key="index" v-show="item.label != '序号'">
            <!-- v-show="item.label != '图片'" -->
            <div  class="detailInfo_label" style="width:120px;">{{item.label}}：</div>
            <div class="detailInfo_text" style="text-indent: 0rem;" v-if="item.prop=='sd'">
              <span>{{detailInfoObj[item.prop]?detailInfoObj[item.prop]:0}}</span>
            </div>
            <div class="detailInfo_text" style="text-indent: 0rem;" v-else>
              <span>{{detailInfoObj[item.prop]}}</span>
            </div>
          </el-col>
        </el-row>
        <el-row v-else>
          <img
            style="
              width: 170px;
              height: 105px;
              margin-left: 50%;
              margin-top: 50%;
              transform: translate(-50%,0);
            "
            src="@/assets/noData.png"
            alt=""
          />
        </el-row>
      </div>
    </div>
  </div>

  <wrjDetailDialog ref="wrjDetailRef"></wrjDetailDialog>
  <deviceDetailDialog ref="deviceDetailRef"></deviceDetailDialog>
  <detailInfoDialog :column="wrjxxColumn" title="无人机信息" ref="detailInfoRef"></detailInfoDialog>
</template>

<script setup>
import { ElMessage } from "element-plus";
import { useRouter,useRoute } from "vue-router";
import microApp from "@/components/microApp/index.vue";
import { ref, onMounted,nextTick,onUnmounted } from "vue";
import wrjDetailDialog from "./wrjDetailDialog.vue"
import deviceDetailDialog from "./deviceDetail.vue"
import icon1 from "@/assets/leftTitle/leftImg.png"
import wxdzcsb from "@/assets/allImage/wxdzcsb.png"
import wrj from "@/assets/allImage/wrj.png"
import { mapModeEnum, getCurrentMapMode } from "@/utils/Map/mapMode";
import { useMap3DStore } from "@/store/modules/map3D";
import { useMap2DStore } from "@/store/modules/map2D";
import detailInfoDialog from "@/views/components/wrjgjhf/detailInfoDialog.vue";

// 定义路由
const router = useRouter();
const route = useRoute();
const showMenuFlags=ref({})
const menu = ref([
  {
    name:"空域动态",
    icon:icon1
  },
  {
    name:"设备动态",
    icon:icon1
  },
  {
    name:"告警信息",
    icon:icon1
  }
])
const authStatusTypeData = ref([
  {
    value:'4',
    label:'待干扰',
  },
  {
    value:'5',
    label:'待诱骗',
  },
  {
    value:'6',
    label:'持续跟踪',
  },
])
const detailInfoObj = ref(null)
const column = [
  { prop: "brand", label: "无人机品牌", width: 120 },
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
  { prop: "sd", label: "速度" },
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
const wxdsbVisible = ref(false)
const wxdtcVisible = ref(true)
const queryInfo = ref({
  isValid:1,
  name:""
})
const wxdsbData = ref([])
const pageOption = ref({
  pageNo:1,
  pageSize:10
})
const total = ref(0)
const timer = ref(null)
const timer2 = ref(null)

const mapChange = ref(false)
let lineLayer = null;
let markerLayer = null;
let lineLayer2 = null;
let zymbMarkerLayer = null;
let sectorLayers = null;
let wrjMarkerLayer = null;
const ycqtVisible = ref(false);
const ycqtBtnVisible = ref(false);
// 初始化
onMounted(()=>{
  nextTick(()=>{
    
    rq.value =window.TOOL.dateFormat(new Date(),"yyyy-MM-dd");
    window.eventBus.emit('rq',rq.value)
    handleCalendarChange(rq.value)
    
    lineLayer = L.layerGroup([]);
    lineLayer.addTo(window.Map2D.map);
    lineLayer2 = L.layerGroup([]);
    lineLayer2.addTo(window.Map2D.map);
    markerLayer = L.layerGroup([]);
    markerLayer.addTo(window.Map2D.map);
    
    zymbMarkerLayer = window.L.layerGroup([]);
    zymbMarkerLayer.addTo(window.Map2D.map);

    sectorLayers = window.L.layerGroup([]);
    sectorLayers.addTo(window.Map2D.map);

    wrjMarkerLayer = L.layerGroup([]);
    wrjMarkerLayer.addTo(window.Map2D.map);

    getData()
    getSbData()
    getWxdData('cx')

  })
  timer.value =setInterval(()=>{
    getData()
    getWxdData()
  },1000 * 6)

  // timer2.value =setInterval(()=>{
  //   getWrjFxData()
  // },1000 * 60 * 5)

  // window.WEBSCOKET.ws.addEventListener('message', (event) => {
  //     try {
  //       let data = JSON.parse(event.data);
  //       let msg_txt = JSON.parse(data.msg_txt);
  //       console.log(msg_txt);
  //       window.eventBus.emit("gj",msg_txt)
  //     } catch (error) {
        
  //     }
  //   });
  if(curMapMode === mapModeEnum["3D"]){
    mapChange.value = true
    // eventBus.emit("zhtsBottomFlag",mapChange.value)
  }else if(curMapMode === mapModeEnum["2D"]){
    mapChange.value = false
    // eventBus.emit("zhtsBottomFlag",mapChange.value)
  }
  //二三维地图切换
  window.eventBus.on("mapChange",function(e){
    mapChange.value = !mapChange.value;
    console.log('xxxxxxxx123',mapChange.value);
    if(!mapChange.value){
      tyFlag.value = false;
      eventBus.emit("zhtsBottomFlag",tyFlag.value)
    }
    speed.value = 0;
    lng.value = 0;
    lat.value = 0;
    window.eventBus.off("wrjData")
    window.TOOL.data.remove('wrjData')
    clearInterval(timelineInterval);
    timelineInterval = null;
    setTimeout(()=>{
      clearMarkerLayer()
      // clearLineLayer()
      clearLayer2()
      clearLayer4()
    },700)
    const element = document.getElementById('timeline');
      if(element){
        element.remove()
      }
    nextTick(()=>{
      
      setTimeout(()=>{
        addToMap()
      },100)
    })
  })

  window.ty = ty;
  window.ty2D = ty2D;
  window.ppwrjxx = ppwrjxx;
  window.eventBus.on("wrjTargetClick",(data)=>{
    wrjDetail(data.targetData);
  })
  

  // initCesiumInteractions()
})
onUnmounted(()=>{
    clearInterval(timer.value)
    timer.value=null;

    // clearInterval(timer2.value)
    // timer2.value=null;

    window.eventBus.off("wrjData")
    window.eventBus.off("wrjTargetClick")
    window.TOOL.data.remove('wrjData')
    clearInterval(timelineInterval);
    timelineInterval = null;
    setTimeout(()=>{
      clearMarkerLayer()
      clearLineLayer()
      clearLineLayer2()
      clearLayer2()
      clearLayer4()
      clearLayer1()
      clearLayer3()
    },700)
    const element = document.getElementById('timeline');
      if(element){
        element.remove()
      }
  window.eventBus.off("mapChange")
  window.eventBus.off("wrjDetailData")
  window.eventBus.off("isMap2dEvent")
  
  //清除三维推演
  window.Map3D.wrjFly.clearAllLayers()

  
})

const returnBack = () => {
 clearInterval(timer.value)
    timer.value=null;
  // clearInterval(timer2.value)
  //   timer2.value=null;
  clearInterval(tyTimer.value)
    tyTimer.value=null;
    window.eventBus.off("wrjData")
    window.TOOL.data.remove('wrjData')
    clearInterval(timelineInterval);
    setTimeout(()=>{
      clearMarkerLayer()
      clearLineLayer()
    },800)
    
    wrjXxxxVisible.value = false;
    timelineInterval = null;
    const element = document.getElementById('timeline');
      if(element){
        element.remove()
      }
  window.eventBus.off("mapChange")
  window.eventBus.off("wrjDetailData")
  getData()
  getWxdData()
    
  timer.value =setInterval(()=>{
    getData()
    getWxdData()
  },1000 *6)

  // timer2.value =setInterval(()=>{
  //   getWrjFxData()
  // },1000 * 60 * 5)
  //无人机信息展开
  wrjToggleVisible.value = true;
  wrjToggleEvent()
  //设备展开
  sbToggleVisible.value = true;
  sbToggleEvent()
  eventBus.emit("timeline",{index:1,flag:false})
  returnBackFlag.value = false;
  fxData.value = []
  window.Map3D.wrjFly.clearAllLayers()
  window.Map2D.map.setZoom(14)
  window.Map2D.setEwtyFlag(false)
}
const menuPosition = ref({})
const showMenu = (event) =>{
  // event.preventDefault();
      showMenuFlags.value['flag'+wrjCurrentIndex.value] = true;
      const rect = event.target.getBoundingClientRect();
      menuPosition.value.x = event.clientX - rect.left;
      menuPosition.value.y = event.clientY - rect.top;
}
const wrjToggleVisible = ref(false)
const wrjToggleEvent = () => {
  wrjToggleVisible.value = !wrjToggleVisible.value
  if(wrjToggleVisible.value){
    $('#zcwrjxx').css("transform","translateX(420px)");
    // $('#zcwrjXxxx').css("transform","translateX(0)");
  }else{
    $('#zcwrjxx').css("transform","translateX(0)");
    // $('#zcwrjXxxx').css("transform","translateX(365px)");
  }
  
}

const wrjDetailToggleVisible = ref(false)
const wrjDetailToggleEvent = () => {
  wrjDetailToggleVisible.value = !wrjDetailToggleVisible.value
  if(wrjDetailToggleVisible.value){
    $('#zcwrjXxxx').css("transform","translateX(385px)");
  }else{
    $('#zcwrjXxxx').css("transform","translateX(0)");
  }
  
}
const sbToggleVisible = ref(false)
const sbToggleEvent = () => {
  sbToggleVisible.value = !sbToggleVisible.value
  if(sbToggleVisible.value){
    $('#zcsb').css("transform","translateX(-368px)");
  }else{
    $('#zcsb').css("transform","translateX(0)");
  }
  
}
const menuClick = (index) =>{
  if(index==1){
    wxdsbVisible.value = !wxdsbVisible.value;
    wxdtcVisible.value = !wxdtcVisible.value;
    if(wxdsbVisible.value){
      getData()
    }
    if(wxdtcVisible.value){
      rq.value =window.TOOL.dateFormat(new Date(),"yyyy-MM-dd");
      getWxdData('cx')
    }
  }
}

const wrjCurrentIndex = ref(-1)
const wrjCheckList = ref(["飞手","无人机","信息"])
const model=ref('')
const searchChange = () => {
  wrjCurrentIndex.value =-1;
  ycqtBtnVisible.value = false;
  ycqtVisible.value = false;
  getWxdData('cx')
  window.eventBus.emit('rq',rq.value)
}
//型号查询
const modelChange = ()=>{
  wrjCurrentIndex.value =-1;
  ycqtBtnVisible.value = false;
  ycqtVisible.value = false;
  getWxdData('cx')
}

const authStatusChange = () => {
  getWxdData('cx')
}

const wrjCheckChange = () => {
  console.log(wrjCheckList.value)
  if(ycqtVisible.value){
        fsAddPoint2()
        wrjAddPoint2()

        // 隐藏其他模式 (3D)
         fsAddPoint3D_2(); 
         wrjAddPoint3D_2();
      }else{
        fsAddPoint()
        wrjAddPoint()

         // 全量模式 (3D)
         fsAddPoint3D();
         wrjAddPoint3D();
      }
}

const rq=ref("")
const holidays = ref([
  
])

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
const wrjData = ref([])
const zcNum = ref(0)
const gjNum = ref(0)
const slNum = ref(0)
const authStatus = ref('')
const getWxdData = (data) => {
  window.API.wxdzc.getUavDetectMsgByStationId({
    rq:rq.value,
    authStatus:authStatus.value
  }).then(res=>{
    // console.log(res);
    if(res.success){
      wrjData.value = res.result;
      

      if(model.value){
        wrjData.value = wrjData.value.filter(v=> 
          (v.model || '').toLowerCase().includes(model.value.toLowerCase())
        )
      }
      zcNum.value = res.result.filter(v=>v.status==1).length;
      gjNum.value = res.result.filter(v=>v.status==2).length;
      slNum.value = res.result.filter(v=>v.status!=1 && v.status!=2).length;
      
      wrjData.value.forEach(item => {
        wxdsbDataAll.value.forEach(item2=>{
          if(item.stationId == item2.stationId && item.uavDetectMsg){
            
            // // 使用turf.js计算距离
            const distance = turf.distance([Number(item2.wd),Number(item2.jd)], [Number(item.uavDetectMsg.dronLat), Number(item.uavDetectMsg.dronLng)]);
            // console.log(item.uavDetectMsg,item2,distance)
            if(item.uavDetectMsg.sd){
              item.ddkydtime = ((distance * 1000) / item.uavDetectMsg.sd).toFixed(3);
              item.ddkysxtime = window.TOOL.dateFormat(new Date(new Date(item.uavDetectMsg.dataTime).getTime() + item.ddkydtime * 1000),"yyyy-MM-dd hh:mm:ss")
            }else{
              item.ddkydtime = '';
              item.ddkysxtime = '';
            }
            
          }
        })
      });
      

      // if(wrjData.value && wrjData.value.length){
      //   window.Map2D.map.flyTo([Number(wrjData.value[0].uavDetectMsg.dronLat),Number(wrjData.value[0].uavDetectMsg.dronLng)],14)
      // }
      // wrjData.value.forEach(item=>{
      //   item.uavDetectMsg.dronLat = Math.random()  +20
      //   item.uavDetectMsg.dronLng = Math.random() * 16  +100
      // })
      if(data=='cx'){
        // getWrjFxData(data)
        wrjData.value.forEach(item=>{
          const letters = '0123456789ABCDEF';
          let color = '#';
          for (let i = 0; i < 6; i++) {
            color += letters[Math.floor(Math.random() * 16)];
          }
          item.color=color;
          item.gjWidth="3";
        })
        wrjDataCopy.value=[...wrjData.value]
      }else if(wrjDataCopy.value && wrjDataCopy.value.length){
         wrjData.value.forEach(item=>{
          wrjDataCopy.value.forEach(item2=>{
            if(item.brand ==item2.brand && item.model ==item2.model && item.serial ==item2.serial){
              item.color = item2.color;
              item.gjWidth = item2.gjWidth;
            }
          })
         })
      }
      
      if(ycqtVisible.value){
        fsAddPoint2()
        wrjAddPoint2()

        // 隐藏其他模式 (3D)
         fsAddPoint3D_2(); 
         wrjAddPoint3D_2();
      }else{
        fsAddPoint()
        wrjAddPoint()

         // 全量模式 (3D)
         fsAddPoint3D();
         wrjAddPoint3D();
      }
      
    }
  })
}

//开始 暂停
const flyKz = (data) => {
  if(mapChange.value){
      window.Map3D.wrjFly.togglePlayPause(data)
  }else{
      if (data) {
        clearInterval(timelineInterval);
        timelineInterval = null;
        timelineInterval = setInterval(function() {
          currentTime = (currentTime + 1) % fxData.value.length;
          updateTimeline(currentTime);
          clearMarkerLayer()
          console.log(fxData.value[currentTime]);
          planeMarkers[currentTime].setLatLng([fxData.value[currentTime].dronLat, fxData.value[currentTime].dronLng]).addTo(markerLayer);
          const  markerIcon = L.divIcon({
                html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>${rowData.value.brand}-${rowData.value.model}(${rowData.value.serialNumber})</div>`,//marker标注
                className: 'my-div-icon',
                iconAnchor: [80, 20]//文字标注相对位置
              });
          window.L.marker(
            window.L.latLng(Number(fxData.value[currentTime].dronLat), Number(fxData.value[currentTime].dronLng)),
            {
              icon: markerIcon,
            }
          ).addTo(markerLayer);
          // // 使用turf.js计算距离
                const fxTime = (new Date(fxData.value[currentTime].dataTime).getTime() - new Date(fxData.value[0].dataTime).getTime()) * 1000
                const distance = turf.distance([Number(fxData.value[0].dronLat),Number(fxData.value[0].dronLng)], [Number(fxData.value[currentTime].dronLat), Number(fxData.value[currentTime].dronLng)]);
                speed.value =(distance * 1000 / fxTime).toFixed(8)
                lat.value = fxData.value[currentTime].dronLat
                lng.value = fxData.value[currentTime].dronLng
               window.TOOL.data.set('wrjData',{longitude:Number(fxData.value[currentTime].dronLng),latitude:Number(fxData.value[currentTime].dronLat),altitude:0,speed:speed.value})
        }, 1000);
        // timeLineRef.value.autoPlayTimer(1)
      } else {
        clearInterval(timelineInterval);
        timelineInterval = null;
        // timeLineRef.value.autoPlayTimer(2)
      }
      showMenuFlags.value['flag'+wrjCurrentIndex.value] = false;
  }
   
  
}
const rightMenuClose = () =>{
  showMenuFlags.value['flag'+wrjCurrentIndex.value] = false;
}

const rightTy2D = (item,index) => {
  showMenuFlags.value['flag'+wrjCurrentIndex.value] = false;
  ty2D(item,index)
}
const rightTy = (item,index) => {
  showMenuFlags.value['flag'+wrjCurrentIndex.value] = false;
  ty(item,index)
}
//视角切换
const myFlag = ref(true)
const flyMy = () =>{
  myFlag.value = !myFlag.value
  if(myFlag.value){
    window.Map3D.wrjFly.setSj(3)
  }else{
    window.Map3D.wrjFly.setSj(1)
  }
  
}

const return3dBack = () => {
  map3dStore.changeWidthAndHeight("0", "0"); // 隐藏三维地图
  map2dStore.changeWidthAndHeight("100%", "100%"); // 展示二维地图
  mapChange.value = false;
  tyFlag.value = false;
  eventBus.emit("zhtsBottomFlag",tyFlag.value)
  eventBus.emit("isMap2dEvent",true)
  returnBack()
}

const fxData = ref([])
const path = ref([])
const droneEntity = ref(null)
const curMapMode = getCurrentMapMode();
let timelineInterval = null;
// 初始化当前时间点
let currentTime = 0;
let planeMarkers = [];
// 创建时间点
let timelinePoints = [];
//无人机飞行详细数据
const wrjFly = (data,index,event) => {
  wrjCurrentIndex.value = index;
  ycqtBtnVisible.value = true;
  if(event=='click'){
    if(data.jmlx=="频谱测向"){
      window.API.sbgl.list({
        stationId:data.stationId
      }).then(res=>{
        if(res.success){
          let dataArr =res.result.records;
          if(dataArr && dataArr.length){
            // 也就是距离中心 radius 米，角度为 currentDirection 的点
            let dronePos = getDestinationPoint({lat:Number(dataArr[0].wd),lng:Number(dataArr[0].jd)}, data.uavDetectMsg.angle, dataArr[0].zcbj *1000);
            window.Map2D.map.flyTo(dronePos,14)

            window.Map3D.viewer.camera.flyTo({
                destination: window.Cesium.Cartesian3.fromDegrees(dronePos[1], dronePos[0], 1000),
                orientation: {
                    heading: 0,
                    roll: 0,
                },
            });
          }
        }
      })
    }else if(data.uavDetectMsg.dronLat && data.uavDetectMsg.dronLng){
      window.Map2D.map.flyTo([Number(data.uavDetectMsg.dronLat),Number(data.uavDetectMsg.dronLng)],14)

      window.Map3D.viewer.camera.flyTo({
          destination: window.Cesium.Cartesian3.fromDegrees(Number(data.uavDetectMsg.dronLng), Number(data.uavDetectMsg.dronLat), 1000),
          orientation: {
              heading: 0,
              roll: 0,
          },
      });
    }

    if(ycqtVisible.value){
      ycqtData()
    }
  }

  
}
const ycqtData = () =>{
  ycqtVisible.value = true;
  getWxdData()
  // getWrjFxData()
}
const wrjDataCopy=ref([])
const getWrjFxData = (data) => {
  clearLineLayer2()
  if(ycqtVisible.value){
    wrjData.value.forEach((item,index)=>{
      if(wrjCurrentIndex.value == index){
        window.API.wxdzc.getUavDetectMsgByModelSerialRq({
          model:item.model,
          rq:rq.value,
          serial:item.serial
        }).then(res=>{
          if(res.success){
            let wrjFxData = res.result.uavDetectMsgList;
            
            if(data=='cx'){
              // 添加飞行轨迹
              const letters = '0123456789ABCDEF';
              let color = '#';
                for (let i = 0; i < 6; i++) {
                    color += letters[Math.floor(Math.random() * 16)];
                }
                item.color=color;
                item.gjWidth="3";
            }
            
            L.polyline(wrjFxData.map(p => [p.dronLat, p.dronLng]),{color:item.color,weight:item.gjWidth}).addTo(lineLayer2);
              
          }
        })
      }
    })
  }
  // else{
  //   wrjData.value.forEach(item=>{
  //     window.API.wxdzc.getUavDetectMsgByModelSerialRq({
  //       model:item.model,
  //       rq:rq.value,
  //       serial:item.serial
  //     }).then(res=>{
  //       if(res.success){
  //         let wrjFxData = res.result.uavDetectMsgList;
          
          
  //         if(data=='cx'){
  //             // 添加飞行轨迹
  //             const letters = '0123456789ABCDEF';
  //             let color = '#';
  //               for (let i = 0; i < 6; i++) {
  //                   color += letters[Math.floor(Math.random() * 16)];
  //               }
  //               item.color=color;
  //               item.gjWidth="3";
  //           }
  //         L.polyline(wrjFxData.map(p => [p.dronLat, p.dronLng]),{color:item.color,weight:item.gjWidth}).addTo(lineLayer2);
            
  //       }
  //     })
  //   })
  // }
  

  wrjDataCopy.value=[...wrjData.value]
}
//轨迹宽度改变事件
const gjysChange = (data) => {
  wrjDataCopy.value.forEach(item2=>{
    if(data.brand ==item2.brand && data.model ==item2.model && data.serial ==item2.serial){
      item2.color = data.color;
      item2.gjWidth = data.gjWidth;
    }
  })
  if(mapChange.value){
    rowData.value.color = wrjData.value[wrjCurrentIndex.value].color;
    rowData.value.weight = wrjData.value[wrjCurrentIndex.value].gjWidth;
    window.Map3D.wrjFly.clearAllLayers()
          const path = Map3D.wrjFly.generateDronePath(fxData.value);
          const droneEntity = Map3D.wrjFly.createDroneModel(fxData.value);
          Map3D.wrjFly.DronePlaybackController(
            path, 
            droneEntity,
            rowData.value
          );
  }else{
    clearLineLayer2()
    wrjData.value.forEach(item=>{
      window.API.wxdzc.getUavDetectMsgByModelSerialRq({
        model:item.model,
        rq:rq.value,
        serial:item.serial
      }).then(res=>{
        if(res.success){
          let wrjFxData = res.result.uavDetectMsgList;
          L.polyline(wrjFxData.map(p => [p.dronLat, p.dronLng]),{color:item.color,weight:item.gjWidth}).addTo(lineLayer2);
            
        }
      })
    })
  }
  
}
const detailInfoRef = ref(null)
const dqflOption = window.WRJXX.dqflOption;
const zlflOption = window.WRJXX.zlflOption;
const brandOption = window.WRJXX.brandOption;
const beforeColumn = [
  { value: "mc", label: "名称", span: 12 },
  { value: "serialNumber", label: "序列号", span: 12 },
  { value: "brand", label: "品牌", span: 12 },
  { value: "model", label: "型号", span: 12 },
  { value: "type", label: "无人机类型", span: 12 },
  { value: "dqfl", label: "地区分类", span: 12 },
  { value: "zlfl", label: "种类分类", span: 12 },
]
const endColumn = [
  { value: "remark", label: "备注", span: 24 },
  { value: "tp", label: "图片", span: 24 },
]
const wrjxxColumn = ref([])
//匹配无人机信息
const ppwrjxx  =(data) => {
  //关闭右键弹框
  Map2D.map.closePopup();
  let brands = brandOption.map(v=>v.label);
      if(!brands.includes(data.brand)){
        wrjxxColumn.value = [...beforeColumn,...window.WRJXX.allzd,...endColumn];
      }else if(data.brand.indexOf("彩虹")!=-1 || data.brand.indexOf("凤翎")!=-1 || data.brand.indexOf("翼神")!=-1 ||  data.brand.indexOf("SY-450H")!=-1 || data.brand.indexOf("八旋翼")!=-1 ){
        wrjxxColumn.value = [...beforeColumn,...window.WRJXX.myggzd,...endColumn];
      }else if(data.brand.indexOf("牵牛星")!=-1 || data.brand.indexOf("海盗")!=-1 || data.brand.indexOf("猎户座")!=-1 ||  data.brand.indexOf("海雕")!=-1 || data.brand.indexOf("猎人")!=-1 ){
        wrjxxColumn.value = [...beforeColumn,...window.WRJXX.ejwrjggzd,...endColumn];
      }else {
         wrjxxColumn.value =[...beforeColumn,...window.WRJXX[data.brand],...endColumn];
      }
      nextTick(()=>{
        window.API.wrjsjk.list({
          brand:data.brand,
          serialNumber:data.serial,
          model:data.model
        }).then(res=>{
        if(res.code == 200){
          let wrjxxData = res.result.records;
          if(wrjxxData && wrjxxData.length){
            detailInfoRef.value.open(wrjxxData[0])
          }else{
            detailInfoRef.value.open({})
          }
        }
        })
        
      })
}
// 清除图层
const clearLineLayer2 = () => {
  if (
      lineLayer2 != undefined &&
      lineLayer2 != null &&
      lineLayer2 != ""
  ) {
    // 清空图层
    lineLayer2.clearLayers();
  }
};
const map3dStore = useMap3DStore();
const map2dStore = useMap2DStore();
const tyFlag = ref(false);
const ty = (data,index) => {
  console.log(data);
  tyFlag.value = true;
  eventBus.emit("zhtsBottomFlag",tyFlag.value)
  wrjCurrentIndex.value = index;
  clearInterval(timelineInterval);
  timelineInterval = null;
  clearInterval(tyTimer.value)
    tyTimer.value=null;
    //清除定时器
  clearInterval(timer.value);
  timer.value=null;
  //关闭右键弹框
  Map2D.map.closePopup();
  map2dStore.changeWidthAndHeight("0", "0"); // 隐藏二维地图
  map3dStore.changeWidthAndHeight("100%", "100%"); // 展示三维地图
  mapChange.value = true;
  window.eventBus.emit("isMap2dEvent",false)
  wrjXxxxVisible.value = false;
  //清除三维推演
    window.Map3D.wrjFly.clearAllLayers()
    // window.eventBus.emit("mapChange",true)
    window.eventBus.emit("tcChange",true)
    eventBus.emit("zhtsBottomFlag",mapChange.value)
    wrjToggleVisible.value = false;
    wrjToggleEvent()
  window.API.wxdzc.getUavDetectMsgByModelSerialRq({
    model:data.model,
    rq:rq.value,
    serial:data.serial
  }).then(res=>{
    if(res.success){
      fxData.value = res.result.uavDetectMsgList;
      if(res.result.wjbdWrjJbxx){
        rowData.value = res.result.wjbdWrjJbxx;
        rowData.value.serial = data.serial;
        rowData.value.rq = rq.value;
        rowData.value.color = wrjData.value[wrjCurrentIndex.value].color;
        rowData.value.weight = wrjData.value[wrjCurrentIndex.value].gjWidth;

      }else{
        rowData.value = {
          brand:data.brand,
          authStatus:data.authStatus,
          model:data.model,
          serialNumber:data.serial,
          serial:data.serial,
          rq:rq.value,
          color:wrjData.value[wrjCurrentIndex.value].color,
          weight:wrjData.value[wrjCurrentIndex.value].gjWidth
        }
      }
      // 初始化无人机轨迹和模型
      if(fxData.value && fxData.value.length){
        addToMap()
      }
    }
  })
}
const returnBackFlag = ref(false)
const wrjXxxxVisible = ref(false)
const tyTimer=ref(null)
//二维推演
const ty2D = (data,index) =>{
  
  returnBackFlag.value = true;
  wrjCurrentIndex.value = index;
  wrjXxxxVisible.value = true;
  console.log(data,index,wrjData.value,wrjCurrentIndex.value);
  //关闭右键弹框
  Map2D.map.closePopup();
  map2dStore.changeWidthAndHeight("100%", "100%"); // 展示二维地图
  map3dStore.changeWidthAndHeight("0", "0"); // 隐藏三维地图
  mapChange.value = false;
  //清除定时器
  clearInterval(timer.value);
  timer.value=null;
  currentTime = 0;
  // clearInterval(timer2.value);
  // timer2.value=null;
  setTimeout(()=>{
    clearLayer2()
    clearLayer3()
    clearLayer4()
    clearLineLayer2()
  },1000)
  planeMarkers=[];
  eventBus.emit("timeline",{index:2,flag:true})
  wrjToggleVisible.value = false;
  wrjToggleEvent()

  sbToggleVisible.value = true;
  $('#zcsb').css("transform","translateX(-368px)");
  console.log(data);
  ty2DLineData(data)

  tyTimer.value = setInterval(()=>{
    ty2DLineData(data)
  },1000*10)
}

const ty2DLineData = (data) => {
  window.API.wxdzc.getUavDetectMsgByModelSerialRq({
    model:data.model,
    rq:rq.value,
    serial:data.serial
  }).then(res=>{
    if(res.success){
      fxData.value = res.result.uavDetectMsgList;
      if(res.result.wjbdWrjJbxx){
        rowData.value = res.result.wjbdWrjJbxx;
        rowData.value.serial = data.serial;
        rowData.value.rq = rq.value
        rowData.value.color = wrjData.value[wrjCurrentIndex.value].color;
        rowData.value.weight = wrjData.value[wrjCurrentIndex.value].gjWidth;
      }else{
        rowData.value = {
          brand:data.brand,
          authStatus:data.authStatus,
          model:data.model,
          serialNumber:data.serial,
          serial:data.serial,
          rq:rq.value,
          color:wrjData.value[wrjCurrentIndex.value].color,
          weight:wrjData.value[wrjCurrentIndex.value].gjWidth
        }
      }
      console.log(rowData.value);
      // 初始化无人机轨迹和模型
      if(fxData.value && fxData.value.length){
        addToMap()
      }
    }
  })
}

const rowData = ref({})
const timeline = ref(null);
const showTimeLine  =ref(false)
const speed = ref(0)
const lng = ref('')
const lat = ref('')
//二三维推演上图
const addToMap = () => {
  console.log('xxx',mapChange.value);
  if(fxData.value && fxData.value.length){
    if(mapChange.value){
          clearInterval(timelineInterval);
          timelineInterval = null;
          speed.value = 0;
          lng.value = 0;
          lat.value = 0;
          window.Map3D.wrjFly.clearAllLayers()
          clearCesiumLayer(wrjEntities3D);
          clearCesiumLayer(sectorEntities3D);
          const path = Map3D.wrjFly.generateDronePath(fxData.value);
          const droneEntity = Map3D.wrjFly.createDroneModel(fxData.value);
          Map3D.wrjFly.DronePlaybackController(
            path, 
            droneEntity,
            rowData.value
          );
          window.eventBus.on("wrjData",function(e){
            lat.value = e.latitude;
            lng.value = e.longitude;
            speed.value = e.speed;
          })
          window.eventBus.on("wrjDetailData",function(e){
            detailInfoObj.value = e;
            detailInfoObj.value.brand = rowData.value.brand;
          })
          eventBus.emit("zhtsBottomFlag",mapChange.value)
    }else{
          returnBackFlag.value = true;
          wxdtcVisible.value = false;
          window.eventBus.off("wrjData")
          window.TOOL.data.remove('wrjData')
          window.eventBus.off("wrjDetailData")
          Map2D.setEwtyFlag(true)
          speed.value = 0;
          lng.value = 0;
          lat.value = 0;
          sbToggleVisible.value = true;
          $('#zcsb').css("transform","translateX(-368px)");
          eventBus.emit("timeline",{index:2,flag:true})
          const element = document.getElementById('timeline');
          if(element){
            element.remove()
          }
          clearInterval(timelineInterval);
          timelineInterval = null;
          
          // 创建时间轴
          // 创建ID为timeline的div元素
          timeline.value = document.createElement('div');
          timeline.value.id = 'timeline';
          var largeContent = document.getElementsByClassName('bottomTimeLine')[0];
          console.log(largeContent);
          largeContent.appendChild(timeline.value)
          // document.body.appendChild(timeline.value);
          var timelineElement = document.createElement('div');
          timelineElement.className = 'timeline';
          timeline.value.appendChild(timelineElement);

          // 创建时间线
          var timelineLine = document.createElement('div');
          timelineLine.className = 'timeline-line';
          timelineElement.appendChild(timelineLine);

          // 创建时间点
          timelinePoints = fxData.value.map(function(item, index) {
              var point = document.createElement('div');
              point.className = 'timeline-point';
              point.style.left = (index / (fxData.value.length - 1) * 100) + '%';
              point.dataset.index = index;
              timelineElement.appendChild(point);
              return point;
          });

          // 创建时间刻度
          var timelineElement = document.getElementById('timeline');
          var timelineLine = timelineElement.querySelector('.timeline-line');
          // 创建时间刻度
          // var lastTime = null;

          // 根据时间点数量创建时间刻度
          // fxData.value.forEach(function(item, index) {
          //     // 计算时间差，如果大于10分钟，则创建刻度
          //     if (lastTime === null || (new Date(item.dataTime) - new Date(lastTime)) >= 600000) { // 600000毫秒等于10分钟
          //         var tick = document.createElement('div');
          //         tick.className = 'timeline-tick';
          //         tick.style.left = (index / (fxData.value.length - 1) * 100) + '%';
          //         timelineLine.appendChild(tick);
          //         var timeKd = document.createElement('div');
          //         timeKd.className = 'timeKd';
          //         timeKd.innerHTML = item.dataTime; // 显示时间
          //         tick.appendChild(timeKd);
          //         lastTime = item.dataTime;
          //     }
          // });
          // 首先获取所有时间点
          const times = fxData.value.map(item => new Date(item.dataTime));
          if (times.length === 0) return; // 避免空数据处理

          // 计算最小和最大时间
          const minTime = Math.min(...times);
          const maxTime = Math.max(...times);
          const totalSpan = maxTime - minTime;

          // 确定所需的刻度数，例如5个
          const numTicks = 8;
          const interval = totalSpan / (numTicks - 1);

          // 初始化当前刻度时间为最小时间，并在开始时创建第一个刻度
          let currentTick = minTime;

          // 创建初始刻度
          const initialTick = document.createElement('div');
          initialTick.className = 'timeline-tick';
          initialTick.style.left = '0%'; // 初始时间显示在最左边
          timelineLine.appendChild(initialTick);

          const initialTimeKd = document.createElement('div');
          initialTimeKd.className = 'timeKd';
          initialTimeKd.innerHTML = new Date(minTime).toLocaleString(); // 格式化时间显示
          initialTick.appendChild(initialTimeKd);

          // 遍历数据点，生成刻度
          if(interval!=0){
            fxData.value.forEach((item, index) => {
                const currentTimes = new Date(item.dataTime);
                
                // 如果当前时间超过了当前刻度时间加上间隔时间
                while (currentTimes >= currentTick + interval) {
                    // 创建刻度
                    const tick = document.createElement('div');
                    tick.className = 'timeline-tick';
                    tick.style.left = ((index) / (fxData.value.length - 1) * 100) + '%';
                    timelineLine.appendChild(tick);
                    
                    const timeKd = document.createElement('div');
                    timeKd.className = 'timeKd';
                    timeKd.innerHTML = currentTimes.toLocaleString(); // 根据需要格式化时间
                    tick.appendChild(timeKd);
                    
                    // 更新当前刻度时间
                    currentTick += interval;
                }
            });
          }

          // 确保最后一个时间点被显示
          const kdLastTime = new Date(fxData.value[fxData.value.length - 1].dataTime);
          if (kdLastTime > currentTick) {
              const tick = document.createElement('div');
              tick.className = 'timeline-tick';
              tick.style.left = '100%'; // 最后时间显示在最右边
              timelineLine.appendChild(tick);

              const timeKd = document.createElement('div');
              timeKd.className = 'timeKd';
              timeKd.innerHTML = kdLastTime.toLocaleString(); // 格式化时间显示
              tick.appendChild(timeKd);
          }
          
          var currentPoint = timelinePoints[currentTime];
          console.log(currentPoint);
          currentPoint.classList.add('timeline-current');

          

          // 添加时间轴拖拽功能
          timelineElement.addEventListener('mousedown', function(e) {
              var rect = timelineElement.getBoundingClientRect();
              var x = e.clientX - rect.left;
              var time = (x / timelineElement.offsetWidth) * (fxData.value.length - 1);
              currentTime = Math.round(time);
              updateTimeline(currentTime);
          });
          showTimeLine.value = true;

          // 添加飞行轨迹
          var polyline = L.polyline(fxData.value.map(p => [p.dronLat, p.dronLng]),{color:rowData.value.color,weight:rowData.value.weight}).addTo(lineLayer);
          // 添加飞机图标
          var planeIcon = L.icon({
              iconUrl: '/static/fly1.png',
              iconSize: [25, 25],
              iconAnchor: [12, 41],
              popupAnchor: [-3, -73]
          });
          console.log(rowData.value);
          if(rowData.value){
            if(rowData.value.authStatus==1){
              planeIcon =L.icon({
                iconUrl: '/static/fly1.png',
                iconSize: [25, 25],
                iconAnchor: [12, 41],
                popupAnchor: [-3, -73]
            });
            }else if(rowData.value.authStatus==2){
              planeIcon =L.icon({
                  iconUrl: '/static/fly2.png',
                  iconSize: [25, 25],
                  iconAnchor: [12, 41],
                  popupAnchor: [-3, -73]
              });
            }else{
              planeIcon =L.icon({
                  iconUrl: '/static/fly3.png',
                  iconSize: [25, 25],
                  iconAnchor: [12, 41],
                  popupAnchor: [-3, -73]
              });
            }
          }
          // window.Map2D.map.flyTo([Number(fxData.value[0].dronLat),Number(fxData.value[0].dronLng)],11)
        
          fxData.value.forEach(function(point,index) {
            // console.log(index);
              var marker = L.marker([point.dronLat, point.dronLng], { icon: planeIcon });
              marker.on('contextmenu', function(evt){
                var rigList = [
                    { text: '诱骗', iconname: 'yp', click: `yp(${evt.latlng.lng},${evt.latlng.lat})` },
                    { text: '干扰', iconname: 'gr', click: `gr(${evt.latlng.lng},${evt.latlng.lat})` },
                    // { text: '二维推演', iconname: 'ty', click: `ty2D(${JSON.stringify(rowData.value)},${wrjCurrentIndex.value})` },
                    { text: '三维推演', iconname: 'ty', click: `ty(${JSON.stringify(rowData.value)},${wrjCurrentIndex.value})` },
                    { text: '匹配无人机信息', iconname: 'ty', click: `ppwrjxx(${JSON.stringify(rowData.value)})` },
                ]
                //console.log();
                var mapRigMenuHtm = function (o) {
                    return `
                  <div class='cd-span' style="padding:5px 10px; cursor: pointer;">
                    <a onclick='${o.click}'>
                    <img src='/static/map_img/${o.iconname}.png' style='vertical-align: middle;'>${o.text}</a></a>
                  </div>`
                }

                var rigHtm = ''
                for (let i = 0; i < rigList.length; i++) {
                    rigHtm += mapRigMenuHtm(rigList[i])
                }

                //添加地图弹出框
                L.popup({
                    className: 'mypopup',
                }).setLatLng(evt.latlng).setContent(rigHtm).openOn(window.Map2D.map)
              })
              planeMarkers.push(marker);
          });
          
            // 时间轴更新逻辑
            timelineInterval = setInterval(function() {
                currentTime = (currentTime + 1) % fxData.value.length;
                updateTimeline(currentTime);
                // polyline.setLatLngs([fxData.value[currentTime].dronLat, fxData.value[currentTime].dronLng]);
                clearMarkerLayer()
                // console.log(fxData.value[currentTime]);
                planeMarkers[currentTime].setLatLng([fxData.value[currentTime].dronLat, fxData.value[currentTime].dronLng]).addTo(markerLayer);
                // console.log(planeMarkers[currentTime]);
                const  markerIcon = L.divIcon({
                  html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>${rowData.value.brand}-${rowData.value.model}(${rowData.value.serialNumber})</div>`,//marker标注
                  className: 'my-div-icon',
                  iconAnchor: [80, 20]//文字标注相对位置
                });
            window.L.marker(
              window.L.latLng(Number(fxData.value[currentTime].dronLat), Number(fxData.value[currentTime].dronLng)),
              {
                icon: markerIcon,
              }
            ).addTo(markerLayer);
                
                // // 使用turf.js计算距离
                const fxTime = (new Date(fxData.value[currentTime].dataTime).getTime() - new Date(fxData.value[0].dataTime).getTime()) * 1000
                const distance = turf.distance([Number(fxData.value[0].dronLat),Number(fxData.value[0].dronLng)], [Number(fxData.value[currentTime].dronLat), Number(fxData.value[currentTime].dronLng)]);
                if(distance && fxTime){
                  speed.value =(distance * 1000 / fxTime).toFixed(8)
                }
                lat.value = fxData.value[currentTime].dronLat
                lng.value = fxData.value[currentTime].dronLng

                window.TOOL.data.set('wrjData',{longitude:Number(fxData.value[currentTime].dronLng),latitude:Number(fxData.value[currentTime].dronLat),altitude:0,speed:speed.value})
          }, 1000);
         
    }
  }
  

}
// 添加时间轴动画
        const updateTimeline = (time) => {
            timelinePoints.forEach(function(point, index) {
                if (index === currentTime) {
                    point.classList.add('timeline-current');
                } else {
                    point.classList.remove('timeline-current');
                }
            });
            detailInfoObj.value = fxData.value[currentTime];
            detailInfoObj.value.brand = rowData.value.brand;
        }
        // 清除图层
const clearMarkerLayer = () => {
  if (
    markerLayer != undefined &&
    markerLayer != null &&
    markerLayer != ""
  ) {
    // 清空图层
    markerLayer.clearLayers();
  }
};
//清除线图层
const clearLineLayer = () => {
  if (
    lineLayer != undefined &&
    lineLayer != null &&
    lineLayer != ""
  ) {
    // 清空图层
    lineLayer.clearLayers();
  }
};
const wrjDetailRef = ref({})
//无人机详细数据
const wrjDetail = (data,flag) => {
  console.log(data);
  window.API.wrj.list({
    serialNumber:data.serial
  }).then(res=>{
    if(res.code == 200){
      let detailData = {}
      if(res.result.records && res.result.records.length){
        detailData = res.result.records[0];
        detailData.stationName=data.stationName;
        detailData.stationId=data.stationId;
        Object.assign(detailData,data.uavDetectMsg);
        nextTick(()=>{
          wrjDetailRef.value.open(detailData,flag)
        })
      }else{
        detailData = data.uavDetectMsg;
        detailData.stationName=data.stationName;
        detailData.stationId=data.stationId;
        nextTick(()=>{
          wrjDetailRef.value.open(detailData,flag)
        })
      }
    }
  })
}


//条数切换
const handleSizeChange = (val) => {
  pageOption.value.pageSize = val;
  getData();
};
//页数切换
const handleCurrentChange = (val) => {
  pageOption.value.pageNo = val;
  getData();
}
const inquires =() =>{ 
  getData()
}
const getData = () =>{
  const params = JSON.parse(JSON.stringify(pageOption.value)); // Object.assign(this.queryInfo,this.pageOption)
      var arr = [];
      for(var key in queryInfo.value){
        if(queryInfo.value[key] && !(/^[\u4e00-\u9fa5a-zA-Z0-9]{1,100}$/.test(queryInfo.value[key]))){
          ElMessage.warning("查询内容不能包含特殊字符");
          return;
        }
        if(queryInfo.value[key]){
          arr.push({
            "rule": "like",
            "type": "input",
            "val": queryInfo.value[key],
            "field": key
          })
        }
      }
      if(arr.length){
        params.superQueryParams = JSON.stringify(arr)
        params.superQueryMatchType = 'and'
      }
  window.API.wxdzc.list(params).then(res=>{
    // console.log(res);
    if(res.success){
      wxdsbData.value = res.result.records;
      total.value = res.result.total;
      addPoint()
    }
    
  })
}

const wxdsbDataAll = ref([])
const getSbData = () =>{
  window.API.wxdzc.list(pageOption.value).then(res=>{
    // console.log(res);
    if(res.success){
      wxdsbDataAll.value = res.result.records;
    }
    
  })
}
//定位设备
const flyDevice = (data) => {
  if(data.wd && data.jd){
    window.Map2D.map.flyTo([data.wd,data.jd],10)
  }else{
    ElMessage.warning('该设备没有经纬度！')
  }
  
}

const greenIcon = ref(null);
const deviceDetailRef = ref(null)

const addPoint = () => {
  clearLayer1();
// console.log(wxdsbData.value);
  wxdsbData.value.forEach((item) => {
    // if (item.status === 'CONNECTED') {
    //   greenIcon.value = window.L.icon({
    //     iconUrl: require('@/assets/allImage/sbZc.png'),
    //     iconSize: [25, 25],
    //   });
    // } else {
    //   greenIcon.value = window.L.icon({
    //     iconUrl: require('@/assets/allImage/sbYc.png'),
    //     iconSize: [25, 25],
    //   });
    // }
    if (item.status === 'CONNECTED') {
      if (item.deviceType === 'DETECT') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/zcsb.png',
          iconSize: [25, 25],
        });
      } else if (item.deviceType === 'DISTURB') {
        greenIcon.value = window.L.icon({
          iconUrl: '/static/grsb.png',
          iconSize: [25, 25],
        });
      }else if (item.deviceType === 'TRAP') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/ypsb.png',
          iconSize: [25, 25],
        });
      }else if (item.deviceType === 'System') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/system.png',
          iconSize: [25, 25],
        });
      }
    }else if(item.status === 'DISCONNECTED'){
      if (item.deviceType === 'DETECT') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/zcsb2.png',
          iconSize: [25, 25],
        });
      } else if (item.deviceType === 'DISTURB') {
        greenIcon.value = window.L.icon({
          iconUrl: '/static/grsb2.png',
          iconSize: [25, 25],
        });
      }else if (item.deviceType === 'TRAP') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/ypsb2.png',
          iconSize: [25, 25],
        });
      }else if (item.deviceType === 'System') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/system2.png',
          iconSize: [25, 25],
        });
      }
    }else{
      if (item.deviceType === 'DETECT') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/zcsb3.png',
          iconSize: [25, 25],
        });
      } else if (item.deviceType === 'DISTURB') {
        greenIcon.value = window.L.icon({
          iconUrl: '/static/grsb3.png',
          iconSize: [25, 25],
        });
      }else if (item.deviceType === 'TRAP') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/ypsb3.png',
          iconSize: [25, 25],
        });
      }else if (item.deviceType === 'System') {
        greenIcon.value = window.L.icon({
          iconUrl:'/static/system3.png',
          iconSize: [25, 25],
        });
      }
    }
    if (item.jd && item.wd) {
      const marker = window.L.marker(
        window.L.latLng(Number(item.wd), Number(item.jd)),
        {
          icon: greenIcon.value,
        }
      ).addTo(zymbMarkerLayer);

      const html = `<div style="width:140px;background:rgba(30, 32, 44);padding:10px">
                  <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                    <div style="width:100%;margin: 2px 0;color:#fff;display:flex;">名称：<div style="width：calc(100% - 60px);color:#fff;white-space: nowrap;text-overflow: ellipsis;overflow: hidden;">${item.name}</div></div>
                    <div style="width:100%;margin: 2px 0;color:#fff;">经度：<span style="color:#fff;">${item.jd.toFixed(3)}</span></div>
                    <div style="width:100%;margin: 2px 0;color:#fff;">纬度：<span style="color:#fff;">${item.wd.toFixed(3)}</span></div>
                    <div style="width:100%;margin: 2px 0;color:#fff;">状态：<span style="color:#fff;">${item.status=='CONNECTED'?'已连接':item.status=='DISCONNECTED'?'未连接':'告警中'}</span></div>
                  </div>
                </div>`;
      marker
        .bindPopup(item.name)
        .bindTooltip(html);
      marker.on("click", function () {
        nextTick(()=>{
          deviceDetailRef.value.open(item)
        })
      });
      const center = [item.wd, item.jd];
      const radius = item.zcbj * 1000; // 圆的半径
      const bound = getCriclePoints(center, radius);
      
      const circleMarker = window.L.polygon(bound, { color: "#03f83c" }).addTo(zymbMarkerLayer);
      
      var  markerIcon = L.divIcon({
                html: `<div style='width:180px;color: #000;text-align: center;font-family: SimHei;font-size:12px;'>
                <div>${item.name}</div>
                <div style="color:${item.status=='CONNECTED'?'rgb(0,211,0)':'red'}">${item.status=='CONNECTED'?'已连接':'未连接'}</div>
                </div>`,//marker标注
                className: 'my-div-icon',
                iconAnchor: [90, -20]//文字标注相对位置
              });
      window.L.marker(
        window.L.latLng(Number(item.wd), Number(item.jd)),
        {
          icon: markerIcon,
        }
      ).addTo(zymbMarkerLayer);
      // const circleMarker = window.L.circle(center, radius, {
      //           color: '#03f83c',
      //           weight: 2
      //       }).addTo(zymbMarkerLayer);
      // animateCircle(circleMarker,radius);
    }
  });
};

const animateCircle = (circle, targetRadius, duration = 1000) => {
    let currentRadius = 0;
    const startTime = Date.now();
    
    function update() {
        const elapsed = Date.now() - startTime;
        const progress = elapsed / duration;
        
        if (progress < 1) {
            // 使用正弦函数实现波动效果
            const waveProgress = Math.sin(progress * Math.PI) * targetRadius;
            circle.setRadius(waveProgress);
            requestAnimationFrame(update);
        } else {
            circle.setRadius(0);
            requestAnimationFrame(() => {
                animateCircle(circle, targetRadius); // 循环动画
            });
        }
    }
    
    requestAnimationFrame(update);
}
const getCriclePoints = (center, radius) => {
  const bound = [];
  const earthRadius = 6378137; // 地球的半径
  const dlat = (radius / earthRadius) * (180 / Math.PI);
  const dlng = dlat / Math.cos((center[0] * Math.PI) / 180);
  for (let i = 0; i < 360; i++) {
    const red = (i * Math.PI) / 180;
    const lat = center[0] + dlat * Math.sin(red);
    const lng = center[1] + dlng * Math.cos(red);
    bound.push([lat, lng]);
  }
  return bound;
};

/**
     * 生成扇形的多边形路径点
     * @param {Object} center - 中心点 {lat, lng}
     * @param {Number} radius - 半径 (米)
     * @param {Number} direction - 中心朝向角度 (0=North)
     * @param {Number} spread - 扇形张角 (例如 30度)
     */
     const createSectorPoints = (center, radius, direction, spread) => {
      // console.log(center, radius, direction, spread);
        var points = [];
        points.push([center.lat, center.lng]); // 起点是圆心

        var startAngle = direction - (spread / 2);
        var endAngle = direction + (spread / 2);

        // 为了让弧线平滑，每隔 2 度计算一个点
        for (var i = startAngle; i <= endAngle; i += 2) {
            points.push(getDestinationPoint(center, i, radius));
        }
        
        // 确保最后一个点被包含
        points.push(getDestinationPoint(center, endAngle, radius));
        
        points.push([center.lat, center.lng]); // 闭合回圆心
        return points;
    };
    const getDestinationPoint = (latlng, bearing, distance) => {
        var R = 6378137; // 地球半径 (米)
        var brng = toRad(bearing);
        var d = distance;
        var lat1 = toRad(latlng.lat);
        var lon1 = toRad(latlng.lng);

        var lat2 = Math.asin(Math.sin(lat1) * Math.cos(d / R) +
            Math.cos(lat1) * Math.sin(d / R) * Math.cos(brng));

        var lon2 = lon1 + Math.atan2(Math.sin(brng) * Math.sin(d / R) * Math.cos(lat1),
            Math.cos(d / R) - Math.sin(lat1) * Math.sin(lat2));

        return [toDeg(lat2), toDeg(lon2)];
    };
    // 将角度转换为弧度
    const toRad = (degree) => {
        return degree * Math.PI / 180;
    };

    // 将弧度转换为角度
    const toDeg = (radian) => {
        return radian * 180 / Math.PI;
    };
const wrjIcon = ref(null);
// 在地图上标点
const wrjAddPoint = () => {
  clearLayer2()
  clearLayer4()
  if(wrjCheckList.value.indexOf('无人机')==-1){
    return
  }
 
  wrjData.value.forEach((item,index) => {
    if(item.authStatus==1){
       wrjIcon.value = window.L.icon({
        iconUrl: "/static/fly1.png",
        iconSize: [40, 40],
      });
    }else if(item.authStatus==2){
       wrjIcon.value = window.L.icon({
        iconUrl: "/static/fly2.png",
        iconSize: [40, 40],
      });
    }else{
       wrjIcon.value = window.L.icon({
        iconUrl: "/static/fly3.png",
        iconSize: [40, 40],
      });
    }

    var dronePos = null;
      let deviceData = [];
    if(item.jmlx=="频谱测向" && item.uavDetectMsg && item.uavDetectMsg.angle){
      window.API.sbgl.list({
        stationId:item.stationId
      }).then(res=>{
        if(res.success){
          let data =res.result.records;
          if(data && data.length){
            deviceData = data[0];
            // 2. 计算扇形多边形的新坐标
            var sectorPoints = createSectorPoints({lat:Number(deviceData.wd),lng:Number(deviceData.jd)}, deviceData.zcbj *1000, item.uavDetectMsg.angle, 30);
            // console.log('sectorPoints',sectorPoints);
            L.polygon(sectorPoints, {
                color: '#ff3333',
                weight: 1,
                fillColor: '#ff3333',
                fillOpacity: 0.15
            }).addTo(sectorLayers);

            // 3. 计算无人机位置 (在扇形弧边的正中心)
            // 也就是距离中心 radius 米，角度为 currentDirection 的点
            dronePos = getDestinationPoint({lat:Number(deviceData.wd),lng:Number(deviceData.jd)}, item.uavDetectMsg.angle, deviceData.zcbj *1000);
          
            // 使用turf.js计算距离
            const distance = turf.distance([Number(deviceData.wd),Number(deviceData.jd)], [Number(dronePos[0]), Number(dronePos[1])]);
            let ddkydtime = '';
            let ddkysxtime = '';
            if(item.uavDetectMsg.sd){
              ddkydtime = ((distance * 1000) / item.uavDetectMsg.sd).toFixed(3)
              ddkysxtime = window.TOOL.dateFormat(new Date(new Date(item.uavDetectMsg.dataTime).getTime() + ddkydtime * 1000),"yyyy-MM-dd hh:mm:ss")
            }         
                const marker = window.L.marker(
                  dronePos,
                  {
                    icon: wrjIcon.value,
                  }
                ).addTo(wrjMarkerLayer);
                // const innerHTML = "名称: " + item.MC + "<br>";
                // innerHTML += "经度: " + item.JD + "<br>";
                // innerHTML += "纬度: " + item.WD + "<br>";
                const html = `<div style="width:160px;background:#1d5891;padding:3px;opacity:0.6;">
                            <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                               <div style="width:100%;margin: 2px 0;color:#fff;">型号：<span style="color:#fff;">${item.model}</span></div>
                                <div style="width:100%;margin: 2px 0;color:#fff;">距离(m)：<span style="color:#fff;">${item.uavDetectMsg.distance}</span></div>
                                <div style="width:100%;margin: 2px 0;color:#fff;">高度(m)：<span style="color:#fff;">${item.uavDetectMsg.height}</span></div>
                                <div style="width:100%;margin: 2px 0;color:#fff;">速度(m/s)：<span style="color:#fff;">${item.uavDetectMsg.sd?item.uavDetectMsg.sd:0}</span></div>
                              <div style="width:100%;margin: 2px 0;color:#fff;">频率(Mhz)：<span style="color:#fff;">${item.uavDetectMsg.freq}</span></div>
                              <div style="width:100%;margin: 2px 0;color:#fff;">信号强度：<span style="color:#fff;">${item.uavDetectMsg.rssi}</span></div>
                              <div style="width:100%;margin: 2px 0;color:#fff;">经度：<span style="color:#fff;">${item.uavDetectMsg.dronLng.toFixed(3)}</span></div>
                              <div style="width:100%;margin: 2px 0;color:#fff;">纬度：<span style="color:#fff;">${item.uavDetectMsg.dronLat.toFixed(3)}</span></div>
                              
                              <div class="card-item" style="color:#fff;">
                                <div class="label">到达反制圈中心时间:</div>
                                <div class="value">${ddkysxtime?ddkysxtime:'暂无'}</div>
                              </div>
                              <div class="card-item" style="color:#fff;">
                                <div class="label">预计所需时间:</div>
                                <div class="value">${ddkydtime?formatSecondsToHMS(ddkydtime):'暂无'}</div>
                              </div>
                            </div>
                          </div>`;
                          // console.log(html);
                // marker
                  // bindTooltip
                  // .bindPopup(item.brand+'-'+item.model+'-'+item.serial)
                  // .bindTooltip(html)
                  // .openPopup(marker.getLatLng());
                var  markerIcon = L.divIcon({
                          html: html,//marker标注
                          className: 'my-div-icon',
                          iconAnchor: [60, -20]//文字标注相对位置
                        });
                        if(wrjCheckList.value.indexOf('信息')!=-1){
                          window.L.marker(
                  dronePos,
                  {
                    icon: markerIcon,
                  }
                ).addTo(wrjMarkerLayer);
                marker.on("click", function (e) {
                  wrjDetail(item);
                });
                        }
                
                let csData={}
                Object.assign(csData,item)
                csData.rq = rq.value;
                marker.on('contextmenu', function(evt){
                  var rigList = [
                      { text: '诱骗', iconname: 'yp', click: `yp(${JSON.stringify(csData)},${index})` },
                      { text: '干扰', iconname: 'gr', click: `gr(${evt.latlng.lng},${evt.latlng.lat})` },
                      { text: '二维推演', iconname: 'ty', click: `ty2D(${JSON.stringify(csData)},${index})` },
                      { text: '三维推演', iconname: 'ty', click: `ty(${JSON.stringify(csData)},${index})` },
                      { text: '匹配无人机信息', iconname: 'ty', click: `ppwrjxx(${JSON.stringify(csData)})` },
                  ]
                  //console.log();
                  var mapRigMenuHtm = function (o) {
                      return `
                    <div class='cd-span' style="padding:5px 10px; cursor: pointer;">
                      <a onclick='${o.click}'>
                      <img src='/static/map_img/${o.iconname}.png' style='vertical-align: middle;'>${o.text}</a></a>
                    </div>`
                  }

                  var rigHtm = ''
                  for (let i = 0; i < rigList.length; i++) {
                      rigHtm += mapRigMenuHtm(rigList[i])
                  }

                  //添加地图弹出框
                  L.popup({
                      className: 'mypopup',
                  }).setLatLng(evt.latlng).setContent(rigHtm).openOn(window.Map2D.map)
                })

                if(item.authStatus == 6){
                  const center = dronePos;
                  const radius = 1000; //圆的半径
                  const bound = getCriclePoints(center, radius);
                  
                  const polygon = window.L.polygon(bound, { color: "red"}).addTo(wrjMarkerLayer);

                  polygon.setStyle({ opacity:1 });
                  // 定义闪烁参数：间隔时间（ms）和闪烁次数
                  const flashInterval = 500; // 每500毫秒闪烁一次

                  // 创建闪烁动画
                  let flashIndex = 0;
                  const flashIntervalId = setInterval(() => {
                      polygon.setStyle({ opacity:flashIndex % 2 === 0 ? 1 : 0.5 });
                      flashIndex++;
                  }, flashInterval);
                }
              
                
            
          }
        }
      })

    }else if(item.uavDetectMsg && item.uavDetectMsg.dronLat && item.uavDetectMsg.dronLng){
      dronePos = [Number(item.uavDetectMsg.dronLat), Number(item.uavDetectMsg.dronLng)];
     
          const marker = window.L.marker(
            dronePos,
            {
              icon: wrjIcon.value,
            }
          ).addTo(wrjMarkerLayer);
          // const innerHTML = "名称: " + item.MC + "<br>";
          // innerHTML += "经度: " + item.JD + "<br>";
          // innerHTML += "纬度: " + item.WD + "<br>";
          const html = `<div style="width:160px;background:#1d5891;padding:3px;opacity:0.6;">
                      <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                        <div style="width:100%;margin: 2px 0;color:#fff;">型号：<span style="color:#fff;">${item.model}</span></div>
                          <div style="width:100%;margin: 2px 0;color:#fff;">距离(m)：<span style="color:#fff;">${item.uavDetectMsg.distance}</span></div>
                          <div style="width:100%;margin: 2px 0;color:#fff;">高度(m)：<span style="color:#fff;">${item.uavDetectMsg.height}</span></div>
                          <div style="width:100%;margin: 2px 0;color:#fff;">速度(m/s)：<span style="color:#fff;">${item.uavDetectMsg.sd?item.uavDetectMsg.sd:0}</span></div>
                        <div style="width:100%;margin: 2px 0;color:#fff;">经度：<span style="color:#fff;">${item.uavDetectMsg.dronLng.toFixed(3)}</span></div>
                        <div style="width:100%;margin: 2px 0;color:#fff;">纬度：<span style="color:#fff;">${item.uavDetectMsg.dronLat.toFixed(3)}</span></div>
                        <div class="card-item" style="color:#fff;">
                          <div class="label">到达反制圈中心时间:</div>
                          <div class="value">${item.ddkysxtime?item.ddkysxtime:'暂无'}</div>
                        </div>
                        <div class="card-item" style="color:#fff;">
                          <div class="label">预计所需时间:</div>
                          <div class="value">${item.ddkydtime?formatSecondsToHMS(item.ddkydtime):'暂无'}</div>
                        </div>
                          
                      </div>
                    </div>`;
                    // console.log(html);
          // marker
            // bindTooltip
            // .bindPopup(item.brand+'-'+item.model+'-'+item.serial)
            // .bindTooltip(html)
            // .openPopup(marker.getLatLng());
          var  markerIcon = L.divIcon({
                    html: html,//marker标注
                    className: 'my-div-icon',
                    iconAnchor: [60, -20]//文字标注相对位置
                  });
                  if(wrjCheckList.value.indexOf('信息')!=-1){
                    window.L.marker(
                      dronePos,
                      {
                        icon: markerIcon,
                      }
                    ).addTo(wrjMarkerLayer);
                  }
          
          marker.on("click", function (e) {
            wrjDetail(item);
          });
          let csData={}
          Object.assign(csData,item)
          csData.rq = rq.value;
          marker.on('contextmenu', function(evt){
            var rigList = [
                { text: '诱骗', iconname: 'yp', click: `yp(${JSON.stringify(csData)},${index})` },
                { text: '干扰', iconname: 'gr', click: `gr(${evt.latlng.lng},${evt.latlng.lat})` },
                { text: '二维推演', iconname: 'ty', click: `ty2D(${JSON.stringify(csData)},${index})` },
                { text: '三维推演', iconname: 'ty', click: `ty(${JSON.stringify(csData)},${index})` },
                { text: '匹配无人机信息', iconname: 'ty', click: `ppwrjxx(${JSON.stringify(csData)})` },
            ]
            //console.log();
            var mapRigMenuHtm = function (o) {
                return `
              <div class='cd-span' style="padding:5px 10px; cursor: pointer;">
                <a onclick='${o.click}'>
                <img src='/static/map_img/${o.iconname}.png' style='vertical-align: middle;'>${o.text}</a></a>
              </div>`
            }

            var rigHtm = ''
            for (let i = 0; i < rigList.length; i++) {
                rigHtm += mapRigMenuHtm(rigList[i])
            }

            //添加地图弹出框
            L.popup({
                className: 'mypopup',
            }).setLatLng(evt.latlng).setContent(rigHtm).openOn(window.Map2D.map)
          })
        
          if(item.authStatus == 6){
                  const center = dronePos;
                  const radius = 1000; //圆的半径
                  const bound = getCriclePoints(center, radius);
                  
                  const polygon = window.L.polygon(bound, { color: "red"}).addTo(wrjMarkerLayer);

                  polygon.setStyle({ opacity:1 });
                  // 定义闪烁参数：间隔时间（ms）和闪烁次数
                  const flashInterval = 500; // 每500毫秒闪烁一次

                  // 创建闪烁动画
                  let flashIndex = 0;
                  const flashIntervalId = setInterval(() => {
                      polygon.setStyle({ opacity:flashIndex % 2 === 0 ? 1 : 0.5 });
                      flashIndex++;
                  }, flashInterval);
                }
      
    }

    
  });
};

/**
 * 将总秒数转换为 时:分:秒 文字格式（仅显示有值的部分，如 65秒→1分钟05秒，3725秒→1小时02分05秒）
 * @param {number} totalSeconds - 要转换的总秒数（非负整数）
 * @returns {string} 格式为 "X小时XX分钟XX秒" 的字符串（无值的部分自动省略）
 */
const formatSecondsToHMS = (totalSeconds) => {
  // 边界处理：非数字/负数转为 0
  const seconds = Math.max(0, Number(totalSeconds) || 0);
  
  // 计算小时、分钟、秒
  const hours = Math.floor(seconds / 3600); // 1小时=3600秒
  const remainingSecsAfterHour = seconds % 3600;
  const minutes = Math.floor(remainingSecsAfterHour / 60); // 剩余秒数转分钟
  const secs = Math.floor(remainingSecsAfterHour % 60); // 最终剩余的秒数

  // 构建结果数组（仅添加有值的部分）
  const parts = [];
  if (hours > 0) {
    parts.push(`${hours}小时`);
  }
  // 分钟：有小时时必须显示（如 1小时05分钟），无小时但有秒时也需显示（如 1分钟05秒）
  if (hours > 0 || (minutes > 0 || secs > 0)) {
    parts.push(`${minutes.toString().padStart(hours > 0 ? 2 : 1, '0')}分钟`);
  }
  // 秒：始终显示（除非总秒数为0）
  if (seconds > 0) {
    parts.push(`${secs.toString().padStart(2, '0')}秒`);
  }

  // 处理总秒数为0的情况
  return parts.length > 0 ? parts.join('') : '0分钟0秒';
};

// 在地图上标点(隐藏其他)
const wrjAddPoint2 = () => {
  clearLayer2()
  clearLayer4()
  if(wrjCheckList.value.indexOf('无人机')==-1){
    return
  }
 
  wrjData.value.forEach((item,index) => {
    if(wrjCurrentIndex.value==index){
      if(item.authStatus==1){
        wrjIcon.value = window.L.icon({
          iconUrl: "/static/fly1.png",
          iconSize: [40, 40],
        });
      }else if(item.authStatus==2){
        wrjIcon.value = window.L.icon({
          iconUrl: "/static/fly2.png",
          iconSize: [40, 40],
        });
      }else{
        wrjIcon.value = window.L.icon({
          iconUrl: "/static/fly3.png",
          iconSize: [40, 40],
        });
      }

      var dronePos = null;
      let deviceData = [];
      
      if(item.jmlx=="频谱测向"){
        window.API.sbgl.list({
          stationId:item.stationId
        }).then(res=>{
          console.log(res);
          if(res.success){
            let data =res.result.records;
            if(data && data.length){
              deviceData = data[0];
              // 2. 计算扇形多边形的新坐标
              var sectorPoints = createSectorPoints({lat:Number(deviceData.wd),lng:Number(deviceData.jd)}, deviceData.zcbj *1000, item.uavDetectMsg.angle, 30);
              console.log('sectorPoints',sectorPoints);
              L.polygon(sectorPoints, {
                  color: '#ff3333',
                  weight: 1,
                  fillColor: '#ff3333',
                  fillOpacity: 0.4
              }).addTo(sectorLayers);

              // 3. 计算无人机位置 (在扇形弧边的正中心)
              // 也就是距离中心 radius 米，角度为 currentDirection 的点
              dronePos = getDestinationPoint({lat:Number(deviceData.wd),lng:Number(deviceData.jd)}, item.uavDetectMsg.angle, deviceData.zcbj *1000);
               // 使用turf.js计算距离
            const distance = turf.distance([Number(deviceData.wd),Number(deviceData.jd)], [Number(dronePos[0]), Number(dronePos[1])]);
            let ddkydtime = '';
            let ddkysxtime = '';
            if(item.uavDetectMsg.sd){
              ddkydtime = ((distance * 1000) / item.uavDetectMsg.sd).toFixed(3)
              ddkysxtime = window.TOOL.dateFormat(new Date(new Date(item.uavDetectMsg.dataTime).getTime() + ddkydtime * 1000),"yyyy-MM-dd hh:mm:ss")
            }
            

              console.log(dronePos);
                if(dronePos){
                  const marker = window.L.marker(
                    dronePos,
                    {
                      icon: wrjIcon.value,
                    }
                  ).addTo(wrjMarkerLayer);
                  // const innerHTML = "名称: " + item.MC + "<br>";
                  // innerHTML += "经度: " + item.JD + "<br>";
                  // innerHTML += "纬度: " + item.WD + "<br>";
                  const html = `<div style="width:160px;background:#1d5891;padding:3px;opacity:0.6;">
                              <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                                <div style="width:100%;margin: 2px 0;color:#fff;">型号：<span style="color:#fff;">${item.model}</span></div>
                                <div style="width:100%;margin: 2px 0;color:#fff;">距离(m)：<span style="color:#fff;">${item.uavDetectMsg.distance}</span></div>
                                <div style="width:100%;margin: 2px 0;color:#fff;">高度(m)：<span style="color:#fff;">${item.uavDetectMsg.height}</span></div>
                                <div style="width:100%;margin: 2px 0;color:#fff;">速度(m/s)：<span style="color:#fff;">${item.uavDetectMsg.sd?item.uavDetectMsg.sd:0}</span></div>
                                <div style="width:100%;margin: 2px 0;color:#fff;">频率(Mhz)：<span style="color:#fff;">${item.uavDetectMsg.freq}</span></div>
                                <div style="width:100%;margin: 2px 0;color:#fff;">信号强度：<span style="color:#fff;">${item.uavDetectMsg.rssi}</span></div>
                                <div style="width:100%;margin: 2px 0;color:#fff;">经度：<span style="color:#fff;">${item.uavDetectMsg.dronLng.toFixed(3)}</span></div>
                                <div style="width:100%;margin: 2px 0;color:#fff;">纬度：<span style="color:#fff;">${item.uavDetectMsg.dronLat.toFixed(3)}</span></div>
                                <div class="card-item" style="color:#fff;">
                                  <div class="label">到达反制圈中心时间:</div>
                                  <div class="value">${ddkysxtime?ddkysxtime:'暂无'}</div>
                                </div>
                                <div class="card-item" style="color:#fff;">
                                  <div class="label">预计所需时间:</div>
                                   <div class="value">${ddkydtime?formatSecondsToHMS(ddkydtime):'暂无'}</div>
                                </div>
                              </div>
                            </div>`;
                            // console.log(html);
                  // marker
                    // bindTooltip
                    // .bindPopup(item.brand+'-'+item.model+'-'+item.serial)
                    // .bindTooltip(html)
                    // .openPopup(marker.getLatLng());
                  var  markerIcon = L.divIcon({
                            html: html,//marker标注
                            className: 'my-div-icon',
                            iconAnchor: [60, -20]//文字标注相对位置
                          });
                          if(wrjCheckList.value.indexOf('信息')!=-1){
                            window.L.marker(
                              dronePos,
                              {
                                icon: markerIcon,
                              }
                            ).addTo(wrjMarkerLayer);
                            marker.on("click", function (e) {
                              wrjDetail(item);
                            });
                          }
                  
                  let csData={}
                  Object.assign(csData,item)
                  csData.rq = rq.value;
                  marker.on('contextmenu', function(evt){
                    var rigList = [
                        { text: '诱骗', iconname: 'yp', click: `yp(${JSON.stringify(csData)},${index})` },
                        { text: '干扰', iconname: 'gr', click: `gr(${evt.latlng.lng},${evt.latlng.lat})` },
                        { text: '二维推演', iconname: 'ty', click: `ty2D(${JSON.stringify(csData)},${index})` },
                        { text: '三维推演', iconname: 'ty', click: `ty(${JSON.stringify(csData)},${index})` },
                        { text: '匹配无人机信息', iconname: 'ty', click: `ppwrjxx(${JSON.stringify(csData)})` },
                    ]
                    //console.log();
                    var mapRigMenuHtm = function (o) {
                        return `
                      <div class='cd-span' style="padding:5px 10px; cursor: pointer;">
                        <a onclick='${o.click}'>
                        <img src='/static/map_img/${o.iconname}.png' style='vertical-align: middle;'>${o.text}</a></a>
                      </div>`
                    }

                    var rigHtm = ''
                    for (let i = 0; i < rigList.length; i++) {
                        rigHtm += mapRigMenuHtm(rigList[i])
                    }

                    //添加地图弹出框
                    L.popup({
                        className: 'mypopup',
                    }).setLatLng(evt.latlng).setContent(rigHtm).openOn(window.Map2D.map)
                  })

                  if(item.authStatus == 6){
                    const center = dronePos;
                    const radius = 1000; //圆的半径
                    const bound = getCriclePoints(center, radius);
                    
                    const polygon = window.L.polygon(bound, { color: "red"}).addTo(wrjMarkerLayer);

                    polygon.setStyle({ opacity:1 });
                    // 定义闪烁参数：间隔时间（ms）和闪烁次数
                    const flashInterval = 500; // 每500毫秒闪烁一次

                    // 创建闪烁动画
                    let flashIndex = 0;
                    const flashIntervalId = setInterval(() => {
                        polygon.setStyle({ opacity:flashIndex % 2 === 0 ? 1 : 0.5 });
                        flashIndex++;
                    }, flashInterval);
                  }

                }
              
            }
          }
        })

      }else if(item.uavDetectMsg && item.uavDetectMsg.dronLat && item.uavDetectMsg.dronLng){
        dronePos = [Number(item.uavDetectMsg.dronLat), Number(item.uavDetectMsg.dronLng)];
        console.log(dronePos);
        
         
            const marker = window.L.marker(
              dronePos,
              {
                icon: wrjIcon.value,
              }
            ).addTo(wrjMarkerLayer);
            // const innerHTML = "名称: " + item.MC + "<br>";
            // innerHTML += "经度: " + item.JD + "<br>";
            // innerHTML += "纬度: " + item.WD + "<br>";
            
                          // <div style="width:100%;margin: 2px 0;color:#fff;">序列号：<span style="color:#fff;">${item.serial}</span></div>
                          // <div style="width:100%;margin: 2px 0;color:#fff;">品牌：<span style="color:#fff;">${item.brand}</span></div>
            const html = `<div style="width:160px;background:#1d5891;padding:3px;opacity:0.6;">
                        <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                          <div style="width:100%;margin: 2px 0;color:#fff;">型号：<span style="color:#fff;">${item.model}</span></div>
                          <div style="width:100%;margin: 2px 0;color:#fff;">距离(m)：<span style="color:#fff;">${item.uavDetectMsg.distance}</span></div>
                          <div style="width:100%;margin: 2px 0;color:#fff;">高度(m)：<span style="color:#fff;">${item.uavDetectMsg.height}</span></div>
                          <div style="width:100%;margin: 2px 0;color:#fff;">速度(m/s)：<span style="color:#fff;">${item.uavDetectMsg.sd?item.uavDetectMsg.sd:0}</span></div>
                          <div style="width:100%;margin: 2px 0;color:#fff;">经度：<span style="color:#fff;">${item.uavDetectMsg.dronLng.toFixed(3)}</span></div>
                          <div style="width:100%;margin: 2px 0;color:#fff;">纬度：<span style="color:#fff;">${item.uavDetectMsg.dronLat.toFixed(3)}</span></div>
                          <div class="card-item" style="color:#fff;">
                          <div class="label">到达反制圈中心时间:</div>
                            <div class="value">${item.ddkysxtime?item.ddkysxtime:'暂无'}</div>
                          </div>
                          <div class="card-item" style="color:#fff;">
                            <div class="label">预计所需时间:</div>
                            <div class="value">${item.ddkydtime?formatSecondsToHMS(item.ddkydtime):'暂无'}</div>
                          </div>
                        </div>
                      </div>`;
                      console.log(html);
            // marker
              // bindTooltip
              // .bindPopup(item.brand+'-'+item.model+'-'+item.serial)
              // .bindTooltip(html)
              // .openPopup(marker.getLatLng());
            var  markerIcon = L.divIcon({
                      html: html,//marker标注
                      className: 'my-div-icon',
                      iconAnchor: [60, -20]//文字标注相对位置
                    });
                     if(wrjCheckList.value.indexOf('信息')!=-1){
                      window.L.marker(
                        dronePos,
                        {
                          icon: markerIcon,
                        }
                      ).addTo(wrjMarkerLayer);
                      marker.on("click", function (e) {
                        wrjDetail(item);
                      });
                     }
            
            let csData={}
            Object.assign(csData,item)
            csData.rq = rq.value;
            marker.on('contextmenu', function(evt){
              var rigList = [
                  { text: '诱骗', iconname: 'yp', click: `yp(${JSON.stringify(csData)},${index})` },
                  { text: '干扰', iconname: 'gr', click: `gr(${evt.latlng.lng},${evt.latlng.lat})` },
                  { text: '二维推演', iconname: 'ty', click: `ty2D(${JSON.stringify(csData)},${index})` },
                  { text: '三维推演', iconname: 'ty', click: `ty(${JSON.stringify(csData)},${index})` },
                  { text: '匹配无人机信息', iconname: 'ty', click: `ppwrjxx(${JSON.stringify(csData)})` },
              ]
              //console.log();
              var mapRigMenuHtm = function (o) {
                  return `
                <div class='cd-span' style="padding:5px 10px; cursor: pointer;">
                  <a onclick='${o.click}'>
                  <img src='/static/map_img/${o.iconname}.png' style='vertical-align: middle;'>${o.text}</a></a>
                </div>`
              }

              var rigHtm = ''
              for (let i = 0; i < rigList.length; i++) {
                  rigHtm += mapRigMenuHtm(rigList[i])
              }

              //添加地图弹出框
              L.popup({
                  className: 'mypopup',
              }).setLatLng(evt.latlng).setContent(rigHtm).openOn(window.Map2D.map)
            })

            if(item.authStatus == 6){
                  const center = dronePos;
                  const radius = 1000; //圆的半径
                  const bound = getCriclePoints(center, radius);
                  
                  const polygon = window.L.polygon(bound, { color: "red"}).addTo(wrjMarkerLayer);

                  polygon.setStyle({ opacity:1 });
                  // 定义闪烁参数：间隔时间（ms）和闪烁次数
                  const flashInterval = 500; // 每500毫秒闪烁一次

                  // 创建闪烁动画
                  let flashIndex = 0;
                  const flashIntervalId = setInterval(() => {
                      polygon.setStyle({ opacity:flashIndex % 2 === 0 ? 1 : 0.5 });
                      flashIndex++;
                  }, flashInterval);
                }
          
        
      }
    
    }
  });
  
};


const fsIcon = ref(null);
// 在地图上标点
const fsAddPoint = () => {
  clearLayer3()
  if(wrjCheckList.value.indexOf('飞手')==-1){
    return
  }
  
  window.fsMarkerLayer = window.L.layerGroup([]);
  window.fsMarkerLayer.addTo(window.Map2D.map);
  fsIcon.value = window.L.icon({
    iconUrl: "/static/fs33.png",
    iconSize: [40, 40],
  });
  wrjData.value.forEach((item) => {
    // console.log(item.uavDetectMsg.pilotLat ,item.uavDetectMsg.pilotLng);
    if(item.uavDetectMsg && item.uavDetectMsg.pilotLat && item.uavDetectMsg.pilotLng){
      const marker = window.L.marker(
        window.L.latLng(Number(item.uavDetectMsg.pilotLat), Number(item.uavDetectMsg.pilotLng)),
        {
          icon: fsIcon.value,
        }
      ).addTo(window.fsMarkerLayer);
      // const innerHTML = "名称: " + item.MC + "<br>";
      // innerHTML += "经度: " + item.JD + "<br>";
      // innerHTML += "纬度: " + item.WD + "<br>";
      const html = `<div style="width:160px;background:#1d5891;padding:3px;opacity:0.6;">
                  <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                    <div style="width:100%;margin: 2px 0;color:#fff;">所属飞机：<span style="color:#fff;">${item.brand}-${item.model}-${item.serial}</span></div>
                    <div style="width:100%;margin: 2px 0;color:#fff;">序列号：<span style="color:#fff;">${item.serial}</span></div>
                    <div style="width:100%;margin: 2px 0;color:#fff;">品牌：<span style="color:#fff;">${item.brand}</span></div>
                    <div style="width:100%;margin: 2px 0;color:#fff;">经度：<span style="color:#fff;">${item.uavDetectMsg.pilotLng}</span></div>
                    <div style="width:100%;margin: 2px 0;color:#fff;">纬度：<span style="color:#fff;">${item.uavDetectMsg.pilotLat}</span></div>
                  </div>
                </div>`;
      marker
        // bindTooltip
        // .bindPopup(item.brand+'-'+item.serial+'-'+item.serial)
        // .bindTooltip(html)

        var  markerIcon = L.divIcon({
                html: html,//marker标注
                className: 'my-div-icon',
                iconAnchor: [60, -20]//文字标注相对位置
              });
              if(wrjCheckList.value.indexOf('信息')!=-1){
                window.L.marker(
        window.L.latLng(Number(item.uavDetectMsg.pilotLat), Number(item.uavDetectMsg.pilotLng)),
        {
          icon: markerIcon,
        }
      ).addTo(window.fsMarkerLayer);
              }
               
        // .openPopup(marker.getLatLng());
      // marker.on("click", function (e) {
      
      // });
    }
  });
};

// 在地图上标点(隐藏其他)
const fsAddPoint2 = () => {
  clearLayer3()
  if(wrjCheckList.value.indexOf('飞手')==-1){
    return
  }
  
  window.fsMarkerLayer = window.L.layerGroup([]);
  window.fsMarkerLayer.addTo(window.Map2D.map);
  fsIcon.value = window.L.icon({
    iconUrl: "/static/fs33.png",
    iconSize: [40, 40],
  });
  wrjData.value.forEach((item,index) => {
    if(wrjCurrentIndex.value==index){
    // console.log(item.uavDetectMsg.pilotLat ,item.uavDetectMsg.pilotLng);
    if(item.uavDetectMsg && item.uavDetectMsg.pilotLat && item.uavDetectMsg.pilotLng){
      const marker = window.L.marker(
        window.L.latLng(Number(item.uavDetectMsg.pilotLat), Number(item.uavDetectMsg.pilotLng)),
        {
          icon: fsIcon.value,
        }
      ).addTo(window.fsMarkerLayer);
      // const innerHTML = "名称: " + item.MC + "<br>";
      // innerHTML += "经度: " + item.JD + "<br>";
      // innerHTML += "纬度: " + item.WD + "<br>";
      const html = `<div style="width:160px;background:#1d5891;padding:3px;opacity:0.6;">
                  <div style="width:100%;display:flex;flex-wrap: wrap;padding-left: 6px;">
                    <div style="width:100%;margin: 2px 0;color:#fff;">所属飞机：<span style="color:#fff;">${item.brand}-${item.model}-${item.serial}</span></div>
                    <div style="width:100%;margin: 2px 0;color:#fff;">序列号：<span style="color:#fff;">${item.serial}</span></div>
                    <div style="width:100%;margin: 2px 0;color:#fff;">品牌：<span style="color:#fff;">${item.brand}</span></div>
                    <div style="width:100%;margin: 2px 0;color:#fff;">经度：<span style="color:#fff;">${item.uavDetectMsg.pilotLng}</span></div>
                    <div style="width:100%;margin: 2px 0;color:#fff;">纬度：<span style="color:#fff;">${item.uavDetectMsg.pilotLat}</span></div>
                  </div>
                </div>`;
      marker
        // bindTooltip
        // .bindPopup(item.brand+'-'+item.serial+'-'+item.serial)
        // .bindTooltip(html)

        var  markerIcon = L.divIcon({
                html: html,//marker标注
                className: 'my-div-icon',
                iconAnchor: [60, -20]//文字标注相对位置
              });
              if(wrjCheckList.value.indexOf('信息')!=-1){
                window.L.marker(
                    window.L.latLng(Number(item.uavDetectMsg.pilotLat), Number(item.uavDetectMsg.pilotLng)),
                    {
                      icon: markerIcon,
                    }
                  ).addTo(window.fsMarkerLayer);
              }
               
        // .openPopup(marker.getLatLng());
      // marker.on("click", function (e) {
      
      // });
    }
    }
  });
};


// 清除图层
const clearLayer1 = () => {
  if (
    zymbMarkerLayer != undefined &&
    zymbMarkerLayer != null &&
    zymbMarkerLayer != ""
  ) {
    // 清空图层
    zymbMarkerLayer.clearLayers();
  }
};
// 清除图层
const clearLayer2 = () => {
  if (
      wrjMarkerLayer != undefined &&
      wrjMarkerLayer != null &&
      wrjMarkerLayer != ""
  ) {
    // 清空图层
    wrjMarkerLayer.clearLayers();
  }
};
// 清除图层
const clearLayer3 = () => {
  if (
      window.fsMarkerLayer != undefined &&
      window.fsMarkerLayer != null &&
      window.fsMarkerLayer != ""
  ) {
    // 清空图层
    window.fsMarkerLayer.clearLayers();
  }
};
// 清除图层
const clearLayer4 = () => {
  if (
      sectorLayers != undefined &&
      sectorLayers != null &&
      sectorLayers != ""
  ) {
    // 清空图层
    sectorLayers.clearLayers();
  }
};

// 假设 viewer 已经初始化并挂载在全局或 ref 上
// const viewer = window.Map3D.viewer; 
// 如果是在 setup 中，请确保能访问到 viewer 实例

// --- 全局状态管理 ---
let wrjEntities3D = [];
let fsEntities3D = [];
let sectorEntities3D = [];
let isCesiumInited = false; // 防止重复初始化事件

// --- 工具：清除图层 ---
const clearCesiumLayer = (entityArray) => {
  if (!window.Map3D?.viewer) return;
  const viewer = window.Map3D.viewer;
  // 批量移除，性能更好
  viewer.entities.suspendEvents();
  entityArray.forEach(entity => {
    if (entity && viewer.entities.contains(entity)) {
      viewer.entities.remove(entity);
    }
  });
  viewer.entities.resumeEvents();
  entityArray.length = 0;
};

// --- 核心：批量获取设备信息 (优化网络请求) ---
const fetchDeviceDataBatch = async (stationIds) => {
  if (!stationIds || stationIds.length === 0) return {};
  
  // 去重
  const uniqueIds = [...new Set(stationIds)];
  
  // 如果 API 支持批量查询，建议改为一次请求。
  // 这里假设 API 只能单个查，但使用 Promise.all 并发处理，且在外层统一 await
  // 注意：如果数量极大 (如 >50)，建议后端提供批量接口，否则前端并发过高也会卡
  const requests = uniqueIds.map(id => 
    window.API.sbgl.list({ stationId: id }).then(res => ({ id, data: res }))
  );

  const results = await Promise.all(requests);
  const map = {};
  results.forEach(({ id, data }) => {
    if (data?.success && data?.result?.records?.length) {
      map[id] = data.result.records[0];
    }
  });
  return map;
};

// --- 核心：渲染无人机实体 (合并全量/单个逻辑) ---
const renderWrjEntities = async (filterIndex = -1) => {
  if (!window.Map3D?.viewer) return;
  const viewer = window.Map3D.viewer;
  
  // 1. 权限检查
  if (wrjCheckList.value.indexOf('无人机') == -1) {
    clearCesiumLayer(wrjEntities3D);
    clearCesiumLayer(sectorEntities3D);
    return;
  }

  // 2. 清理旧数据
  clearCesiumLayer(wrjEntities3D);
  clearCesiumLayer(sectorEntities3D);

  // 3. 数据预处理
  let dataList = wrjData.value;
  if (filterIndex !== -1) {
    // 单个模式：只取当前索引
    dataList = dataList.filter((_, idx) => idx === filterIndex);
  }

  if (dataList.length === 0) return;

  // 4. 收集需要异步获取的设备 ID (频谱测向类型)
  const stationIdsToFetch = [];
  const spectrumItems = []; // 需要关联设备信息的项

  dataList.forEach(item => {
    if (item.jmlx == "频谱测向" && item.stationId) {
      stationIdsToFetch.push(item.stationId);
      spectrumItems.push(item);
    }
  });

  // 5. 批量获取设备信息 (避免循环内请求)
  const deviceMap = await fetchDeviceDataBatch(stationIdsToFetch);

  // 6. 准备渲染数据 (合并设备信息)
  const renderQueue = dataList.map(item => {
    let deviceData = null;
    let finalLat = null;
    let finalLng = null;
    let finalHeight = item.uavDetectMsg?.height || 50;
    let isSpectrum = item.jmlx == "频谱测向";

    if (isSpectrum) {
      deviceData = deviceMap[item.stationId];
      if (deviceData) {
        // 计算无人机位置
        const centerLat = Number(deviceData.wd);
        const centerLng = Number(deviceData.jd);
        const radius = deviceData.zcbj * 1000;
        const angle = item.uavDetectMsg?.angle || 0;
        
        const droneGeoPos = getDestinationPoint({ lat: centerLat, lng: centerLng }, angle, radius);
        if (droneGeoPos) {
          finalLat = droneGeoPos[0];
          finalLng = droneGeoPos[1];
          
          // 计算时间 (避免在渲染循环中重复计算)
          if (item.uavDetectMsg?.sd) {
            const distance = turf.distance([centerLat, centerLng], [finalLat, finalLng]);
            const ddkydtime = ((distance * 1000) / item.uavDetectMsg.sd).toFixed(3);
            item.ddkydtime = ddkydtime;
            item.ddkysxtime = window.TOOL.dateFormat(
              new Date(new Date(item.uavDetectMsg.dataTime).getTime() + ddkydtime * 1000), 
              "yyyy-MM-dd hh:mm:ss"
            );
          }
        }
      }
    } else {
      // 非频谱测向，直接使用上报坐标
      if (item.uavDetectMsg) {
        finalLat = item.uavDetectMsg.dronLat;
        finalLng = item.uavDetectMsg.dronLng;
      }
    }

    return {
      item,
      deviceData,
      lat: finalLat,
      lng: finalLng,
      height: finalHeight,
      isSpectrum,
      centerLat: deviceData ? Number(deviceData.wd) : null,
      centerLng: deviceData ? Number(deviceData.jd) : null,
      radius: deviceData ? deviceData.zcbj * 1000 : null,
      angle: item.uavDetectMsg?.angle || 0
    };
  });

  // 7. 批量渲染 (Cesium 性能优化关键)
  viewer.entities.suspendEvents();

  // 性能阈值：如果实体过多，简化标签显示
  const isLargeData = renderQueue.length > 10;

  renderQueue.forEach(data => {
    if (!data.lat || !data.lng) return;

    const { item, deviceData, lat, lng, height, isSpectrum, centerLat, centerLng, radius, angle } = data;

    // A. 绘制扇形 (仅频谱测向)
    // if (isSpectrum && deviceData && radius) {
    //   const sectorEntity = addSectorEntity(viewer, {
    //     lat: centerLat,
    //     lng: centerLng,
    //     radius: radius,
    //     direction: angle,
    //     spread: 30,
    //     id: `sector_${item.stationId}_${item.id || Date.now()}`, // 唯一 ID
    //     color: '#ff3333',
    //     opacity: 0.3 // 大量数据时降低透明度减少视觉干扰
    //   });
    //   sectorEntities3D.push(sectorEntity);
    // }
    // B. 绘制无人机模型
    const position = Cesium.Cartesian3.fromDegrees(Number(lng), Number(lat), height);
    
    // 优化标签内容：大数据量时只显示关键信息
    const labelText = createLabelText(item, item.ddkydtime, item.ddkysxtime, isLargeData);
    let entity=null;
    if(item.authStatus == 6){
       entity = viewer.entities.add({
        position: position,
        // model: {
        //   uri: '/wrjmodel/scene.gltf',
        //   scale: isLargeData ? 1.0 : 2.0, // 大数据量缩小模型
        //   minimumPixelSize: 1 // 大数据量减小最小像素
        // },
        billboard: {
            image: item.authStatus==1?"/static/fly1.png":item.authStatus==2?"/static/fly2.png":"/static/fly3.png",
            scale: 1,
            verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
            disableDepthTestDistance: Number.POSITIVE_INFINITY 
          },
        label: {
          text: labelText,
          font: '14px sans-serif',
          fillColor: Cesium.Color.WHITE,
          style: Cesium.LabelStyle.FILL_AND_OUTLINE,
          outlineWidth: 2,
          verticalOrigin: Cesium.VerticalOrigin.TOP,
          pixelOffset: new Cesium.Cartesian2(0, 0),
          backgroundColor: new Cesium.Color(0.11, 0.34, 0.57, 0.6),
          backgroundPadding: new Cesium.Cartesian2(5, 5),
          showBackground: true,//!isLargeData, // 大数据量隐藏背景框提升性能
          disableDepthTestDistance: Number.POSITIVE_INFINITY,
          scale: 1.0
        },
        ellipse: {
          center: position,
          semiMajorAxis: 50, // 长半轴，设置为50米
          semiMinorAxis: 50, // 短半轴，同样设置为50米，形成圆形
          rotation: 0, // 旋转角度，0度表示不旋转
          material: window.Cesium.Color.fromCssColorString('red').withAlpha(0.15),
          animation: {
            duration: 1000,
            repeat: Infinity,
            easingFunction: Cesium.EasingFunction.QUARTIC_OUT
          }
        },
        wrjDataItem: item,
        wrjIndex: wrjData.value.indexOf(item) // 重新获取准确索引
      });
    }else{
       entity = viewer.entities.add({
        position: position,
        // model: {
        //   uri: '/wrjmodel/scene.gltf',
        //   scale: isLargeData ? 1.0 : 2.0, // 大数据量缩小模型
        //   minimumPixelSize: 1 // 大数据量减小最小像素
        // },
        billboard: {
            image: item.authStatus==1?"/static/fly1.png":item.authStatus==2?"/static/fly2.png":"/static/fly3.png",
            scale: 1,
            verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
            disableDepthTestDistance: Number.POSITIVE_INFINITY 
          },
        label: {
          text: labelText,
          font: '14px sans-serif',
          fillColor: Cesium.Color.WHITE,
          style: Cesium.LabelStyle.FILL_AND_OUTLINE,
          outlineWidth: 2,
          verticalOrigin: Cesium.VerticalOrigin.TOP,
          pixelOffset: new Cesium.Cartesian2(0, 0),
          backgroundColor: new Cesium.Color(0.11, 0.34, 0.57, 0.6),
          backgroundPadding: new Cesium.Cartesian2(5, 5),
          showBackground: true,//!isLargeData, // 大数据量隐藏背景框提升性能
          disableDepthTestDistance: Number.POSITIVE_INFINITY,
          scale: 1.0
        },
        wrjDataItem: item,
        wrjIndex: wrjData.value.indexOf(item) // 重新获取准确索引
      });
    }
    

    wrjEntities3D.push(entity);
  });

  viewer.entities.resumeEvents();
};

// --- 辅助：生成标签文本 (支持性能模式) ---
const createLabelText = (i, ddkydtime_val, ddkysxtime_val, isPerformanceMode = false) => {
  if (!i.uavDetectMsg) return "";
  
  const baseInfo = [
    `型号：${i.model}`,
    `距离：${i.uavDetectMsg.distance}m`,
    `高度：${i.uavDetectMsg.height}m`,
    `速度：${i.uavDetectMsg.sd}m/s`
  ];

  // if (isPerformanceMode) {
  //   // 性能模式：只显示最关键信息，减少文本渲染开销
  //   return baseInfo.join('\n');
  // }

  if (i.jmlx == "频谱测向") {
    baseInfo.push(
      `频率：${i.uavDetectMsg.freq}Mhz`,
      `信号强度：${i.uavDetectMsg.rssi}`,
      `经度：${i.uavDetectMsg.dronLng.toFixed(3)}`,
      `纬度：${i.uavDetectMsg.dronLat.toFixed(3)}`
    );
  } else {
    baseInfo.push(
      `经度：${i.uavDetectMsg.dronLng.toFixed(3)}`,
      `纬度：${i.uavDetectMsg.dronLat.toFixed(3)}`
    );
  }

  baseInfo.push(
    `预计所需时间：${ddkydtime_val ? formatSecondsToHMS(ddkydtime_val) : '暂无'}`,
    `到达反制圈中心时间：${ddkysxtime_val || '暂无'}`
  );

  return baseInfo.join('\n');
};

// --- 核心：渲染飞手实体 (合并全量/单个逻辑) ---
const renderFsEntities = (filterIndex = -1) => {
  if (!window.Map3D?.viewer) return;
  const viewer = window.Map3D.viewer;

  if (wrjCheckList.value.indexOf('飞手') == -1) {
    clearCesiumLayer(fsEntities3D);
    return;
  }

  clearCesiumLayer(fsEntities3D);

  let dataList = wrjData.value;
  if (filterIndex !== -1) {
    dataList = dataList.filter((_, idx) => idx === filterIndex);
  }

  if (dataList.length === 0) return;

  const iconUrl = "/static/fs33.png";
  const isLargeData = dataList.length > 50;

  viewer.entities.suspendEvents();

  dataList.forEach((item, index) => {
    if (item.uavDetectMsg && item.uavDetectMsg.pilotLat && item.uavDetectMsg.pilotLng) {
      const position = Cesium.Cartesian3.fromDegrees(
        Number(item.uavDetectMsg.pilotLng),
        Number(item.uavDetectMsg.pilotLat),
        0
      );

      const labelText = `所属飞机：${item.brand}-${item.model}-${item.serial}\n序列号：${item.serial}\n品牌：${item.brand}\n经度：${item.uavDetectMsg.pilotLng}\n纬度：${item.uavDetectMsg.pilotLat}`;

      const entity = viewer.entities.add({
        position: position,
        billboard: {
          image: iconUrl,
          scale: 1.0,
          verticalOrigin: Cesium.VerticalOrigin.BOTTOM,
          disableDepthTestDistance: isLargeData ? 10000 : Number.POSITIVE_INFINITY // 性能优化
        },
        label: {
          text: labelText, // 大数据量隐藏文字
          font: '12px sans-serif',
          fillColor: Cesium.Color.WHITE,
          style: Cesium.LabelStyle.FILL_AND_OUTLINE,
          verticalOrigin: Cesium.VerticalOrigin.TOP,
          pixelOffset: new Cesium.Cartesian2(0, 0),
          backgroundColor: new Cesium.Color(0.11, 0.34, 0.57, 0.6),
          showBackground: true,
          disableDepthTestDistance: Number.POSITIVE_INFINITY,
        },
        wrjDataItem: item,
        wrjIndex: wrjData.value.indexOf(item),
        isPilot: true
      });
      fsEntities3D.push(entity);
    }
  });

  viewer.entities.resumeEvents();
};

// --- 对外暴露的调用函数 (保持原有调用习惯) ---
const wrjAddPoint3D_2 = () => renderWrjEntities(wrjCurrentIndex.value);
const wrjAddPoint3D = () => renderWrjEntities(-1);
const fsAddPoint3D_2 = () => renderFsEntities(wrjCurrentIndex.value);
const fsAddPoint3D = () => renderFsEntities(-1);

// --- Cesium 交互事件处理器 (确保只初始化一次) ---
const initCesiumInteractions = () => {
  if (isCesiumInited || !window.Map3D?.viewer) return;
  
  const viewer = window.Map3D.viewer;
  const handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas);

  // 1. 左键点击 (详情)
  handler.setInputAction(function (movement) {
    const pickedObject = viewer.scene.pick(movement.position);
    if (Cesium.defined(pickedObject) && pickedObject.id) {
      const id = pickedObject.id;
      if (id.wrjDataItem) {
        wrjDetail(id.wrjDataItem);
      }
    }
  }, Cesium.ScreenSpaceEventType.LEFT_CLICK);

  // 2. 右键点击 (菜单)
  handler.setInputAction(function (movement) {
    const pickedObject = viewer.scene.pick(movement.position);
    if (Cesium.defined(pickedObject) && pickedObject.id && pickedObject.id.wrjDataItem) {
      const item = pickedObject.id.wrjDataItem;
      // 注意：这里索引可能因数据过滤而不准确，建议直接用 item 唯一标识
      const cartographic = Cesium.Cartographic.fromCartesian(pickedObject.id.position.getValue());
      const lng = Cesium.Math.toDegrees(cartographic.longitude);
      const lat = Cesium.Math.toDegrees(cartographic.latitude);

      let csData = {};
      Object.assign(csData, item);
      csData.rq = rq.value;

      var rigList = [
        { text: '诱骗', iconname: 'yp', click: `yp(${JSON.stringify(csData)})` },
        { text: '干扰', iconname: 'gr', click: `gr(${lng},${lat})` },
        { text: '二维推演', iconname: 'ty', click: `ty2D(${JSON.stringify(csData)})` }, // 移除 index 依赖
        { text: '三维推演', iconname: 'ty', click: `ty(${JSON.stringify(csData)})` },
        { text: '匹配无人机信息', iconname: 'ty', click: `ppwrjxx(${JSON.stringify(csData)})` },
      ];

      var mapRigMenuHtm = function (o) {
        return `<div class='cd-span' style="padding:5px 10px; cursor: pointer;">
            <a onclick='${o.click}'>
            <img src='/static/map_img/${o.iconname}.png' style='vertical-align: middle;'>${o.text}</a>
          </div>`;
      };

      var rigHtm = '';
      for (let i = 0; i < rigList.length; i++) {
        rigHtm += mapRigMenuHtm(rigList[i]);
      }

      showCustomPopup(movement.position, rigHtm);
    }
  }, Cesium.ScreenSpaceEventType.RIGHT_CLICK);

  isCesiumInited = true;
};

// --- 扇形绘制辅助函数 (保持原有逻辑，增加 Cesium 兼容性) ---
function addSectorEntity(viewer, options) {
  const {
    lat, lng, radius, direction, spread,
    id = 'sector_' + Date.now() + Math.random(),
    color = '#ff3333', opacity = 0.4
  } = options;

  const positions = createSectorPositions({ lat, lng }, radius, direction, spread);

  const entity = viewer.entities.add({
    id: id,
    polygon: {
      hierarchy: positions,
      material: new Cesium.ColorMaterialProperty(
        Cesium.Color.fromCssColorString(color).withAlpha(opacity)
      ),
      outline: true,
      outlineColor: Cesium.Color.fromCssColorString(color),
      outlineWidth: 1,
      clampToGround: true,
      height: 10 // 稍微抬高避免 z-fighting
    }
  });
  return entity;
}

const createSectorPositions = (center, radius, direction, spread) => {
  const positions = [];
  const centerCartesian = Cesium.Cartesian3.fromDegrees(center.lng, center.lat, 0);
  positions.push(centerCartesian);

  const startAngle = direction - (spread / 2);
  const endAngle = direction + (spread / 2);

  // 优化：根据半径动态调整采样密度，半径越小采样越密，半径大则稀疏
  const step = radius > 5000 ? 5 : 2; 

  for (let i = startAngle; i <= endAngle; i += step) {
    positions.push(getDestinationCartesian(center, i, radius));
  }
  positions.push(getDestinationCartesian(center, endAngle, radius));
  positions.push(centerCartesian);

  return positions;
};

const getDestinationCartesian = (latlng, bearing, distance) => {
  const R = 6378137;
  const brng = Cesium.Math.toRadians(bearing);
  const d = distance;
  const lat1 = Cesium.Math.toRadians(latlng.lat);
  const lon1 = Cesium.Math.toRadians(latlng.lng);

  const lat2 = Math.asin(Math.sin(lat1) * Math.cos(d / R) +
    Math.cos(lat1) * Math.sin(d / R) * Math.cos(brng));

  const lon2 = lon1 + Math.atan2(Math.sin(brng) * Math.sin(d / R) * Math.cos(lat1),
    Math.cos(d / R) - Math.sin(lat1) * Math.sin(lat2));

  return Cesium.Cartesian3.fromDegrees(Cesium.Math.toDegrees(lon2), Cesium.Math.toDegrees(lat2), 0);
};

</script>

<style scoped lang="less">
.topCenter {
  width: 100vw;
  height: 68.4vh;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  .buttons{
    position: absolute;
    top: 30px;
    left: 20px;
    pointer-events: auto;
    z-index:1;
  }
  .ewtyBack{
    // position: absolute;
    // top: -10px;
    // left: 30px;
    
    z-index:1;
    background: #1e5893;
    color:#fff;
    padding:1px;
    cursor:pointer;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  // pointer-events: none;
  .left{
    width: 120px;
    height:140px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 12px;
    color: #fff;
    justify-content: space-around;
    pointer-events: auto;
    .menus{
      width: 100%;
      margin-top: 20px;
      cursor: pointer;
      &>div{
        text-align: center;
      }
    }
  }
  


  .wxdsbBox{
    width: 320px;
    height: 600px;
    background: url("@/assets/allImage/dialogBg.png") no-repeat;
    background-size: 100% 100%;
    position: absolute;
    left: 30px;
    top:20px;
    padding: 10px;
    pointer-events: auto;
    .title{
      width: 100%;
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 14px;
      color: #fff;
    }
    .sbToggleBtn{
      position:absolute;
      top:50%;
      transform:translateY(-50%);
      cursor:pointer;
      // width: 25px;
      padding: 2px;
      box-sizing: border-box;
    }
    .sbToggleBg1{
      background:url('/public/static/toggle/toggle-bg2.png') no-repeat;
      background-size:100% 100%;
      
      // right:-28px;
      left: 338px
     
    }
    .sbToggleBg2{
      background:url('/public/static/toggle/toggle-bg1.png') no-repeat;
      background-size:100% 100%; 
      left:-30px;
    }
    .search{
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 10px;
      :deep(.el-input__wrapper){
        // background-color: rgba(97, 137, 177,0.8) !important;
        // border-color: rgb(97, 137, 177)
        background: url(@/assets/allImage/inputBg.png) no-repeat;
        background-size: 100% 100%;
        border:none;
      }
      :deep(.el-input__inner){
        color: #fff  !important;
        border:none;
      }
      :deep(.el-button){
        // background-color: rgba(97, 137, 177,0.8) !important;
        color: #fff  !important;
        margin-left:10px;
        background: url(@/assets/allImage/btnBg.png) no-repeat;
        background-size: 100% 100%;
        border:none;
      }
    }
    .content{
      width: 100%;
      color:#fff;
      // display: flex;
      // justify-content: space-between;
      // align-items: center;
      // flex-wrap: wrap;
      overflow-y: auto;
      height: calc(100% - 95px);
      .wxdsb{
        width: 48%;
        height: 80px;
        margin-top: 15px;
        float: left;
      }
      .wxdsb:nth-of-type(2n+2){
        margin-left:10px;
      }
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
          color:yellow;
        }
      }
      .content-bottom{
        height: 30px;
        line-height: 30px;
        font-size:12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: rgb(0, 40, 93);
        &>div:nth-of-type(2){
          width:calc(100% - 20px);
          height:100%;
          white-space: nowrap; /* 不换行 */
          text-overflow: ellipsis; /* 省略符显示为省略号 */
          overflow: hidden; /* 隐藏溢出的文本 */
        }
        .dotStatus{
          width: 5px;
          height: 5px;
          border-radius: 50%;
          margin-right:5px;
        }
        .bg1{
          background-color: #09eb09;
        }
        .bg2{
          background-color: red;
        }
      }
    }
  }
  #zcwrjxx{
    transform: translateX(0);
    transition: transform 0.3s ease;
  }
  #zcwrjXxxx{
    // transform: translateX(365px);
    // transition: transform 0.3s ease;
  }
  #contextMenu {
    position: absolute;
    background-color: #1f5891;
    border: 1px solid #1f5891;
    border-radius: 4px;
    box-shadow: 0 2px 4px rgb(0 0 0 / 10%);
    z-index: 1;
    padding: 10px 10px;
  }
  .wsdtc{
    width: 400px;
    height: 600px;
    background: url("@/assets/allImage/dialogBg.png") no-repeat;
    background-size: 100% 100%;
    position: absolute;
    right: 25px;
    top:20px;
    padding: 10px;
    pointer-events: auto;
    .wrjToggleBtn{
      position:absolute;
      top:50%;
      transform:translateY(-50%);
      cursor:pointer;
      // width: 25px;
      padding: 2px;
      box-sizing: border-box;
    }
    .wrjToggleBg1{
      background:url('/public/static/toggle/toggle-bg1.png') no-repeat;
      background-size:100% 100%;
      // left:-30px;
      right:400px;
    }
    .wrjToggleBg2{
      background:url('/public/static/toggle/toggle-bg2.png') no-repeat;
      background-size:100% 100%; 
      right:-28px;
    }
    .title{
      width: 100%;
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 14px;
      color: #fff;
    }
    .search{
      display: flex;
      justify-content: space-between;
      flex-wrap:wrap;
      align-items: center;
      margin-top: 10px;
      :deep(.el-input__wrapper){
        // background-color: rgba(97, 137, 177,0.8) !important;
        // border-color: rgb(97, 137, 177)
        background: url(@/assets/allImage/inputBg.png) no-repeat;
        background-size: 100% 100%;
        border:none;
      }
      :deep(.el-input__inner){
        color: #fff  !important;
        border:none;
      }
      :deep(.el-button){
        // background-color: rgba(97, 137, 177,0.8) !important;
        color: #fff  !important;
        margin-left:10px;
        background: url(@/assets/allImage/btnBg.png) no-repeat;
        background-size: 100% 100%;
        border:none;
      }
    }

     .content{
      color:#fff;
      // display: flex;
      // justify-content: space-between;
      // // align-items: center;
      // flex-wrap: wrap;
      overflow-y: auto;
      height: calc(100% - 130px);
      .wxdsb{
        width: 100%;
        // height: 90px;
        margin-top: 10px;
        cursor: pointer;
        box-sizing: border-box;
        position:relative;
        font-size: 12px;
      }
      .content-top{
        background-color: rgb(81, 127, 173);
        text-align: center;
        position: relative;
        
        
        .color1{
          color: #09eb09;
        }
        .color2{
          color: red;;
        }
        .color3{
          color: rgb(71, 70, 70);
        }

        .dotStatus{
          width: 5px;
          height: 5px;
          border-radius: 50%;
          margin-right:5px;
        }
        .bg1{
          background-color: red;
        }
        .bg2{
          background-color: yellow;
        }
        .bg3{
          background-color: #3f5de4;
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
        }
      .content-bottom{
        height: 30px;
        line-height: 30px;
        font-size:12px;
        display: flex;
        align-items: center;
        // justify-content: center;
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
      .content-sx{
        span:nth-of-type(1){
          color:#c7bfbf;
        }
        &>div{
          display: flex;
          align-items: center;
          justify-content: space-between;
        }
      }
    }


    .el-col{
        margin-top: 10px;
        display: flex;
        padding: 5px 0;
        font-size:14px;
      }
      .detailInfo_label{
        color: rgba(255, 255, 255, 0.7);
        font-weight: bold;
        width: 125px;
        text-align: right;
      }
      .detailInfo_text{
        width: calc(100% - 125px);
      }
  }
  
}
.wrjActive{
  border:2px solid #00a5ff;
}
/* 分页样式 */
:deep(.el-pager li) {
  background: transparent;
  border: 1px solid rgba(115, 116, 117);
  color: #fff;
  margin: 0 5px;
}

:deep(.el-pager li.is-active) {
  background: #0e417c;
  border: 1px solid #fff;
  color: #fff;
}

:deep(.el-pagination) {
  display: flex;
  justify-content: flex-end;
  margin-top: 5px;
}
:deep(.el-pagination__total){
  color:#fff;
}
:deep(.el-pagination__jump){
  color:#fff;
}
:deep(.el-pagination button){
  background: transparent;
  border: 1px solid #fff;
}
:deep(.el-pagination .btn-next .el-icon), :deep(.el-pagination .btn-prev .el-icon){
  color:#fff !important;
}
:deep(.el-pagination button.is-disabled), :deep(.el-pagination button:disabled){
  background: transparent;
  border: 1px solid #fff;
}
:deep(.el-pagination button:hover), :deep(.el-pagination button:hover .el-icon){
  color:#409eff !important;
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

/* 基础样式 */
    .ty-button {
        width: 100px;
        height: 40px;
        border: none;
        border-radius: 5px;
        color: white;
        font-size: 16px;
        cursor: pointer;
        margin: 5px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-image: linear-gradient(45deg, #2d83c1, #094a89); /* 添加渐变效果 */
    }

    /* 鼠标悬停效果 */
    .ty-button:hover {
        background-image: linear-gradient(45deg, #3cb2f3, #2e75bb); /* 悬停时渐变颜色变化 */
    }

    /* 点击效果 */
    .ty-button:active {
        background-image: linear-gradient(45deg, #3cb2f3, #2e75bb); /* 点击时渐变颜色变化 */
    }

    /* 三维推演按钮特殊样式 */
    .ty-3d {
        background-image: linear-gradient(45deg,  #2d83c1, #094a89); /* 三维按钮的渐变效果 */
    }

    .ty-3d:hover {
        background-image: linear-gradient(45deg,#3cb2f3, #2e75bb); /* 悬停时三维按钮的渐变颜色变化 */
    }

    .ty-3d:active {
        background-image: linear-gradient(45deg,#3cb2f3, #2e75bb); /* 点击时三维按钮的渐变颜色变化 */
    }
    :deep(.el-checkbox__input.is-checked+.el-checkbox__label){
      color:#0083cb;
    }
    :deep(.el-checkbox){
      margin-right:7px;
    }
    .card-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 6px;
      color:#fff;
    }
    .label {
      opacity: 0.8;
      color:#fff;
    }
    .value {
      font-weight: 500;
      color:#fff;
    }
</style>