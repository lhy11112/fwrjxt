<template>
  <div>
    
    <div class="c-line-box" v-show="showTimeLine">
      <!-- 设置显示控制 -->
      <!-- <div class="c-show-check" style="cursor: pointer" @click="showCheckBox = !showCheckBox">
        <a-icon
          :type="showCheckBox ? 'vertical-align-bottom' : 'edit'"
          style="font-weight: bold; font-size: 18px"
        ></a-icon>
      </div> -->
      <div class="c-direction-arrow"></div>
      <!-- 内容设置 -->
      <div class="c-check-box" v-if="showCheckBox">
        <!-- 时间间隔 -->
        <!-- <a-select
          :options="perGridMinutesOptions"
          style="width: 120px; float: left; bottom: 4px"
          size="small"
          :disabled="true"
          v-model="perGridMinutesDefault"
        >
        </a-select> -->
        <el-select
          style="width: 120px;margin-right: 20px;"
          size="small"
          filterable
          allow-create
          default-first-option
          :reserve-keyword="false"
          :disabled="false"
          v-model="perGridMinutesDefault"
          @change="perGridMinutesChange"
        >
          <el-option v-for="(t,i) in perGridMinutesOptions" :key="i" :label="t.label" :value="t.value"></el-option>
        </el-select>
      </div>
      <!-- 图标容器 -->
      <div ref="iconContainer" class="c-icon-container"></div>
     
      <!-- 自动播放/暂停 -->
      <div class="c-auto-play" @click="autoPlayBoolean = !autoPlayBoolean">
        <el-icon v-show="!autoPlayBoolean" style="font-weight: bold; font-size: 18px;color:#fff"><VideoPlay /></el-icon>
        <el-icon v-show="autoPlayBoolean" style="font-weight: bold; font-size: 18px;color:#fff"><VideoPause /></el-icon>
        <!-- <a-icon
          :type="autoPlayBoolean ? 'pause-circle' : 'play-circle'"
          style="font-weight: bold; font-size: 18px"
        ></a-icon> -->
      </div>
      <a-slider
        ref="sliderContainer"
        range
        :max="max"
        :min="min"
        v-model="defaultValue"
        :step="sliderStep"
        :marks="sliderMarks"
        :tipFormatter="formatTip"
        tooltipPlacement="bottom"
        @afterChange="handleSliderafterChange"
        @change="handleSliderChange"
      ></a-slider>
    </div>

  </div>
</template>
<script>
import moment from 'moment'
// import { mapActions, mapGetters } from 'vuex'
// import { axios } from '@/utils/requests'
// import { queryTimeLineDetail } from '@/api/timeline'
import {
  isNumber as _isNumber,
  toNumber as _number,
  isEmpty as _isEmpty,
  toUpper as _toUpper,
  isNaN as _isNaN,
} from 'lodash'
// import { handleFinishTask } from '@/api/task'
export default {
  name: 'TimeLineComponent',
  props:{
    aiwuBtns:{
      type:Boolean,
      default:false
    },
    flag:{
      type:Boolean,
      default:false
    },
    showTimeLine:{
      type:Boolean,
      default:false
    },
    tyjhData:{
      type:Array,
      default:[]
    }
  },
  data() {
    return {
      jclJdDataList:[],
      jclShow:false,
      bjbhShow: false,
      bjbhData:[],
      bjbhOneData:[],
      // 默认梯度
      sliderStep: 1,
      sliderMarks: {},
      // 默认刻度数
      grid: 1,
      min: 0,
      max: 100,
      defaultValue: [],
      // 是否显示可选框
      showCheckBox: false,
      // 计算时间轴滑轨长度与时间跨度的比例（1px：ms）
      mspx: 0,
      // 文电，战斗经过，标绘，情报信息，战场云
      allInfo: {
        wdInfo: [],
        qbInfo: [],
        bhInfo: [],
        zdjgInfo: [],
        zcyInfo: [],
      },
      // 图标设置
      iconDefaultSetting: {
        bottomOffset: 40, // 默认底部偏移
        leftOffset: 1, // 默认左侧偏移
        width: 20, // 默认icon宽度
      },
      // 时间范围值
      timeRangeList: [],
      // 是否查看当前用户 / 全部用户(默认当前登录单位id)
      userInfoOptions: [
        { label: '查看我的', value: 1 },
        { label: '查看全部', value: 2 },
      ],
      // 是否查看当前任务 / 下级所有子任务(默认查看当前任务)
      taskInfoOptions: [
        { label: '当前任务', value: 1 },
        { label: '全部任务', value: 2 },
      ],
      userInfoParamter: 2,
      taskInfoParamter: 1,
      // 当前任务全信息，包含子任务
      taskAllDetail: {},
      // 自动播放
      autoPlayBoolean: false,
      // 每格时间数[{label:"60分钟/格",value:60}]
      perGridMinutesOptions: [],
      // 默认分钟/格
      perGridMinutesDefault: 60,
      // 定时器
      slidertimer: null,
      // 滑动距离 百分比*100
      trackWidth: 0,
      // 是否显示图标提示框，mouseenter、mouseover / mouseleave、mouseup
      showIconTipsBoolean: false,
      // 图标提示内容
      iconTipContent: {},
      taskDetail:{},
      userInformation:{},
      dwid:'',
    }
  },
  computed: {
    // ...mapGetters('task', ['taskDetail', 'userInformation', 'showTimeLine']),
  },
  watch: {
    'tyjhData': {
      handler(c) {
        if(!c){
          return
        }
        console.log(c[c.length-1].dataTime);
        // if (!c.yjjssj) {
        //   c.yjjssj = dayjs().format("YYYY-MM-DD HH:mm:ss")
        // }
        // this.$store.dispatch('task/setShowTimeLine')
        this.min = moment(c[0].dataTime).valueOf()
        this.max = moment(c[c.length-1].dataTime).valueOf()
        
        console.log("xxxxxxxxxx",this.min,this.max);
        this.$nextTick(() => {
          const sliderWidth = this.$refs.sliderContainer.$el.clientWidth
          this.mspx = Math.round((this.max - this.min) / _number(sliderWidth))
        })
        // 差异分钟数
        const diffMinutes = Math.abs(moment(c[0].dataTime).diff(moment(c[c.length-1].dataTime), 'minutes'))
        // 刻度格子数
        this.grid = Math.ceil(diffMinutes / _number(20))
        console.log('grid', this.grid)
        // todo 超出最多范围，重新计算时间间隔 取最大值24
        if (this.grid > 24) {
          
          this.grid = 14
        }
        // 每格分钟数
        const gridMinutes = Math.round(diffMinutes / this.grid)
        console.log(diffMinutes/3,this.grid,"879789789789789")
        // 格式化默认显示分钟数
        this.perGridMinutesOptions.push({ label: `${gridMinutes}分钟/格`, value: gridMinutes },{ label: `${gridMinutes/2}分钟/格`, value: gridMinutes/2 })
        this.perGridMinutesDefault = gridMinutes
        // console.log(
        //   'perGridMinutesOptions',
        //   this.perGridMinutesOptions,
        //   'perGridMinutesDefault',
        //   this.perGridMinutesDefault
        // )

        // 每个格子的毫秒数
        const gridms = gridMinutes * 60000
        // 构造 marks
        const result = {}
        for (let i = 0; i <= this.grid; i++) {
          if (i === 0 || i === this.grid) {
            result[this.accumulate(c[0].dataTime, gridms, i)] = moment(this.accumulate(c[0].dataTime, gridms, i)).format('HH:mm') +'\n'+ moment(this.accumulate(c[0].dataTime, gridms, i)).format('YY/MM/DD')
            // {
            //   style: {
            //     fontWeight: 'bold',
            //   },
            //   label: (
            //     <div>
            //       <div> {moment(this.accumulate(c.kssj, gridms, i)).format('HH:mm')}</div>
            //       <div> {moment(this.accumulate(c.kssj, gridms, i)).format('YY/MM/DD')}</div>
            //     </div>
                
            //   ),
            // }
            continue
          }
          if (i % 2 === 0) {
            result[this.accumulate(c[0].dataTime, gridms, i)] = moment(this.accumulate(c[0].dataTime, gridms, i)).format('HH:mm')+'\n'+ moment(this.accumulate(c[0].dataTime, gridms, i)).format('YY/MM/DD')
          } else {
            result[this.accumulate(c[0].dataTime, gridms, i)] =""
          }
        }
        console.log(result,"7897897987")
        this.sliderMarks = result
        // 时间轴初始化完成后默认请求一次 起止时间均为开始时间
        // if (sessionStorage.getItem('getTimeLine') && sessionStorage.getItem('getTimeLine').length > 0) {
        //   let defValue = sessionStorage.getItem('getTimeLine').split(',')
        //   this.defaultValue = [Number(defValue[0]), Number(defValue[1])]
        //   this.timeRangeList = [Number(defValue[0]), Number(defValue[1])]
        // } else {
          this.timeRangeList = [this.min, this.flag?this.max:this.min]
          this.defaultValue = [this.min, this.flag?this.max:this.min]
        // }
        this.handleTimeChange()
      },
    },
    // 监测自动播放按钮状态
    autoPlayBoolean: {
      handler(curr) {
        if (curr) {
          if (this.trackWidth === 100) {
            this.trackWidth = 0
          }
          this.autoPlayTimer(1)
        } else {
          this.autoPlayTimer(2)
        }
      },
    },
  },
  created(){
  },
  mounted() {
  },
  methods: {
    moment,
    _isEmpty,
   

    // 时间轴刻度间隔下拉改变事件
    perGridMinutesChange(val){
      if(val<50){
        this.$message.info("时间间隔不能小于50")
        return
      }
      this.perGridMinutesDefault = Number(val)
      this.min = moment(this.taskDetail.kssj).valueOf();
      this.max = moment(this.taskDetail.jssj?this.taskDetail.jssj:this.taskDetail.yjjssj).valueOf()
      this.$nextTick(() => {
        const sliderWidth = this.$refs.sliderContainer.$el.clientWidth;
        this.mspx = Math.round((this.max - this.min) / _number(sliderWidth));
      });
      // 差异分钟数
      const diffMinutes = Math.abs(
        moment(this.taskDetail.kssj).diff(moment(this.taskDetail.jssj?this.taskDetail.jssj:this.taskDetail.yjjssj), "minutes")
      );
      // 刻度格子数
      this.grid = Math.ceil(diffMinutes / _number(this.perGridMinutesDefault));
      // console.log("grid", this.grid);
      // todo 超出最多范围，重新计算时间间隔 取最大值24
      // if (this.grid > 24) {
      //   this.grid = 24;
      // }
      // 每格分钟数
      const gridMinutes = Math.round(diffMinutes / this.grid);
      // 格式化默认显示分钟数
      // if(this.perGridMinutesOptions.map(item=>{return item.value}).indexOf(gridMinutes) == -1){
      //   this.perGridMinutesOptions.push({
      //     label: `${gridMinutes}分钟/格`,
      //     value: gridMinutes,
      //   });
      // }
      // this.perGridMinutesDefault = gridMinutes;
      // 每个格子的毫秒数
      const gridms = gridMinutes * 60000;
      // 构造 marks
      const result = {};
      for (let i = 0; i <= this.grid; i++) {
        if (i === 0 || i === this.grid) {
          result[this.accumulate(this.taskDetail.kssj, gridms, i)] =
            moment(this.accumulate(this.taskDetail, gridms, i)).format("HH:mm") +
            "\n" +
            moment(this.accumulate(this.taskDetail.kssj, gridms, i)).format("YY/MM/DD");
          continue;
        }
        if (i % 2 === 0) {
          result[this.accumulate(this.taskDetail.kssj, gridms, i)] = moment(
            this.accumulate(this.taskDetail.kssj, gridms, i)
          ).format("HH:mm");
        } else {
          result[this.accumulate(this.taskDetail.kssj, gridms, i)] = "";
        }
      }
      this.sliderMarks = result;
    },

    /**
     * 滑动完成后触发一次
     * @param nodeValue [startTime,endTime]
     */
    handleSliderafterChange(nodeValue) {
      this.timeRangeList = nodeValue
      // 取较大时间为结束时间
      const [, endTime] = nodeValue
      console.log(endTime,new Date(endTime));
      // 修改头部作战时间
      this.$emit('handlezztimeChange', endTime)
      // this.defaultValue = nodeValue
      // Cookies.set('getTimeLine', nodeValue)
      this.handleTimeChange()
    },

    

    // 时间轴发生变化
    handleTimeChange() {
      this.autoPlayTimer(1)
    },

    // 格式化tip显示
    formatTip(e) {
      return moment(e).format('YY/MM/DD HH:mm')
    },

    /**
     * 累加每个格子的毫秒数
     * @param start 开始时间
     * @param avg 格子平均数
     * @param i 第几个格子数
     */
    accumulate(start, avg, i) {
      return moment(start).valueOf() + avg * i
    },

  

    /**
     * 自动播放定时器
     * @param {Number} handle 1播放 2 暂停， 3，退出播放
     */
    autoPlayTimer(handle) {
      switch (handle) {
        case 1:
          {
            this.slidertimer = setInterval(() => {
              this.trackWidth = this.trackWidth + 1
              this.setTrackStyle()
              if (this.trackWidth === 100) {
                clearInterval(this.slidertimer)
              }
            }, 100)
          }
          break
        case 2:
          {
            clearInterval(this.slidertimer)
          }
          break
        case 3:
          {
            clearInterval(this.slidertimer)
            this.trackWidth = 0
            this.setTrackStyle()
          }
          break
        default: {
          clearInterval(this.slidertimer)
          this.trackWidth = 0
          this.setTrackStyle()
        }
      }
    },

    // 自动播放样式切换
    setTrackStyle() {
      const sliderHandle = document.querySelector('.c-line-box .ant-slider-handle-2')
      const sliderTrack = document.querySelector('.c-line-box .ant-slider-track-1')
      sliderHandle.style.left = `${this.trackWidth}%`
      sliderTrack.style.width = `${this.trackWidth}%`
      if (this.trackWidth % 10 === 0) {
        // 滑动时的位移对应时间的差值
        const trackTimes = _number(this.max - this.min) * _number(this.trackWidth * 0.01)
        this.timeRangeList = [this.min, this.min + trackTimes]

        this.handleTimeChange()
      }
    },
    
  },
}
</script>
<style lang="less" scoped>
@textColor: #fff;
@borderRadiusNone: 0px;
@sliderHandleColor: #4359a1;
@primary: #1890ff6e;
@danger: #ff4d4f73;
@bgprimary: #435285;
@zindex: 0;
@borderColor: #7580b2;
@markColor: #f9f5f54d;
@sliderTrack: #40a9ff;
.c-line-box {
  width: calc(100vw - 570px - 570px);
  padding: 20px 30px 5px;
  // padding:0;
  // background: url(/static/map_img/bg_dibu.png);
  background-size: 100% 110%;
  /* border: 2px solid rgba(38,60,130,.45); */
  z-index: @zindex - 101;
  // background: url('../../../../assets/firstPage/time-bg.png') no-repeat;
  background-size: 100% 110%;
  opacity: 0.9;

  /deep/ .ant-slider-step {
    // background: url(/static/map_img/chidu@2x.png);
    background-size: 100%;
  }

  /deep/ .ant-slider-with-marks {
    margin-bottom: 28px;
  }

  /deep/ .ant-slider-rail {
    width: 100%;
    height: 5px;
    background-color: @textColor;
    border-radius: @borderRadiusNone;
  }

  /deep/ .ant-slider-track {
    height: 5px;
    border-radius: @borderRadiusNone;
    background-color: @sliderTrack;
  }

  /deep/ .ant-slider-handle {
    // width: 12px;
    // height: 24px;
    // margin-top: -10px;
    // background-color: @sliderHandleColor;
    // border: solid 2px @sliderHandleColor;
    // border-radius: @borderRadiusNone;
  }

  /deep/ .ant-slider-mark {
    top: 23px;
  }

  /deep/ .ant-slider-dot {
    top: -15px;
    width: 3px;
    height: 20px;
    background-color: @textColor !important;
    border: none;
    border-radius: @borderRadiusNone;
    margin-left: 0;
    z-index: @zindex - 100;
  }

  /deep/ .ant-slider-mark-text {
    font-size: 12px;
    color: @textColor;
    opacity: 0.6;
    width:70px
  }

  /deep/ .ant-slider-mark-text-active {
    color: #8ec1f2; //@textColor;
    opacity: 0.8;
  }

  /deep/ .ant-btn-sm {
    height: 20px;
    padding: 1px 5px;
    line-height: 16px;
    border-radius: @borderRadiusNone;
  }
  /deep/ .el-checkbox-group{
    display: inline-block;
  }
  /deep/ .el-checkbox{
    color: @textColor;
    margin-right: 20px;
  }

  .c-show-check {
    position: absolute;
    top: 1px;
    right: 1px;
  }
   .c-direction-arrow {
    width: 0;
    height: 0;
    border-top: 10px solid transparent;
    border-bottom: 10px solid transparent;
    border-bottom-width: 10px;
    border-bottom-style: solid;
    border-bottom-color: transparent;
    border-left: 20px solid @textColor;
    position: absolute;
    right: 12px;
    bottom: 30px;
    z-index: @zindex + 100;
  }

  .c-check-box {
    text-align: right;
    margin-bottom: 25px;
    z-index: @zindex - 10;
    padding: 0px;
    border-bottom: 1px solid @borderColor;

    /deep/ .ant-radio-button-wrapper {
      border-radius: @borderRadiusNone;
    }

    /deep/ .ant-radio-group-small .ant-radio-button-wrapper {
      height: 20px;
      padding: 1px 5px;
      line-height: 16px;
    }

    /deep/ .ant-checkbox-wrapper {
      color: @textColor;
      font-weight: bold;
    }

    /deep/ .ant-radio-group-solid .ant-radio-button-wrapper-checked:not(.ant-radio-button-wrapper-disabled) {
      background: @primary;
      border-color: @primary;
    }

    /deep/ .ant-checkbox-checked .ant-checkbox-inner {
      background: @primary;
      border-color: @primary;
    }

    /deep/ .ant-btn-danger {
      background: @danger;
      border-color: @danger;
    }

    /deep/ .ant-select-arrow {
      top: 25%;
      color: @primary;
    }

    /deep/ .ant-select-selection {
      background-color: @primary;
      border: 1px solid @primary;
      border-radius: @borderRadiusNone;
      color: @textColor;
    }

    /deep/ .ant-select-open {
      background-color: @primary;
      border: 1px solid @primary;
      border-radius: @borderRadiusNone;
      color: @textColor;
    }
  }
  /* .c-check-box /deep/ .ant-select-sm{
  height: 18px;
} */
  .c-auto-play {
    position: absolute;
    left: 5px;
    bottom: 28px;
    cursor: pointer;
  }

  /* 初始值偏移15px - 计图标宽度的一半 iconSize/2 */

  /** 设置图标提示信息 */
  .c-icon-tip-container {
    position: absolute;
    bottom: 85px;
    left: 0;
    transform: translateX(-50%);
    width: 25%;
    min-height: 80px;
    background-color: @bgprimary;
    color: @textColor;
    border-radius: @borderRadiusNone + 5;
    padding: 5px;
    opacity: 0.8;
    font-size: 12px;

    /deep/ .ant-btn-primary {
      background: @primary;
      border-color: @primary;

      &:hover {
        background: spin(@primary, 20);
        border-color: spin(@primary, 20);
      }
    } 

    .c-icon-tip-direction {
      position: absolute;
      left: 50%;
      bottom: -10px;
      transform: translateX(-50%) rotate(45deg);
      width: 20px;
      height: 20px;
      background-color: @bgprimary;
      z-index: @zindex -10;
    }

    .c-tip-content {
      line-height: 18px;

      & > div:first-child {
        font-weight: bold;
        border-bottom: 2px solid @markColor;
        font-size: 14px;
      }
    }
  }
}

.bjbh{
  display:inline-block;
  padding:0 8px;
  font-size:14px;
  font-weight: bold;
  color:#fff;
  cursor: pointer;
  position: relative;
}
.bjbh:hover{
  color: #1890FF;
}
.bjbh_tk{
  width: 500px;
  background: rgba(47, 53, 68, .6);
  border-radius: 5px;
  padding: 10px;
  position: absolute;
  bottom: 55px;
  left: -220px;
  z-index: 999;
  color: #fff;
  text-align: center;
}
.bjbh_tk table{
  width:100%;
  border-top:1px solid rgb(119, 90, 56);;
  border-bottom:1px solid rgb(119, 90, 56);;
  background: rgba(62, 67, 84, .5);
  margin:5px 0;
}
.bjbh_tk table th,.bjbh_tk table td{
  padding: 5px;
  font-weight: normal;
}
.bjbh_tk button{
  margin: 0 auto;
  padding: 0px 10px;
  border-radius: 4px;
  color: #fff;
  height: 24px;
  line-height: 24px;
  width: 80px;
  background: #4267c6;
  border: none;
  cursor:pointer;
}
.mypopup_tip{
  background: rgba(48, 54, 72, 0.85);
  border: 1px solid #707070 !important;
}
.jcl_line_box{
  width: calc(100vw - 575px - 570px);
  // border: 2px solid red;
  position: absolute;
  padding: 0 30px;
  bottom: 18px;
  display: flex;
}
.jcl_jd_box{
  display: flex;
  justify-content: space-evenly;
  align-items: center;
}
</style>
