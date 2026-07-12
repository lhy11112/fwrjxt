<template>
  <div class="components-input-demo-presuffix">
    <!---->
    <el-input placeholder="请点击选择坐标点或输入地点" v-model="pointValueDfm" readOnly :disabled="disabled" clearable @change="handleOK" @input="handleInput">
      <template #append><el-icon style="cursor: pointer;" @click="openModal"><Location /></el-icon></template>
    </el-input>
    <div class="selectOption" v-show="searcList.length>0 && flag">
      <div v-for="(item,index) in searcList" :key="index" style="cursor:pointer;" @click="selectClick(item)">{{item.address}}</div>
    </div>

    <j-map-point-modal
      v-if="visible"
      ref="innerMapPointModal"
      :modal-width="modalWidth"
      :army="army"
      :pointValue="pointValue"
      @ok="handleOK"
      @handleCancel="handleCancel"/>
  </div>
</template>

<script>
  import JMapPointModal from './JmapPointModals'
  export default {
    name: 'JmapPoint',
    components:{
      JMapPointModal
    },
    props:{
      modalWidth:{
        type:Number,
        default:500,
        required:false
      },
      modelValue:{
        type:String,
        required:false
      },
      disabled:{
        type: Boolean,
        required: false,
        default: false
      },
      army:{
        type: Boolean,
        required: false,
        default: false
      },
    },
    data(){
      return {
        visible:false,
        pointValue:"",
        pointValueDfm:"",
        searcList:[],
        flag:false,
      }
    },
    mounted(){
    },
    watch:{
      modelValue: {
        deep: true,
        immediate: true,
        handler(val){
          // console.log(val);
          if (val) {
            this.pointValue = val
            this.pointValueDfm = this.ToDegrees(val.split(",")[0])+","+this.ToDegrees(val.split(",")[1])
          }else{
            this.pointValue = ""
          }
        }
      }
    },
    methods:{
      // 经纬度转度分秒
      ToDegrees(val) {
        if (typeof val == 'undefined' || val == '') {
          return ''
        }
        // 把num类型转换成为string
        val = val + ''
        let i = val.indexOf('.')
        const strDu = i < 0 ? val : val.substring(0, i)
        let strFen = 0
        let strMiao = 0
        if (i > 0) {
          strFen = '0' + val.substring(i)
          strFen = Number(strFen) * 60 + ''
          i = strFen.indexOf('.')
          if (i > 0) {
            strMiao = '0' + strFen.substring(i)
            strFen = strFen.substring(0, i)
            strMiao = Number(strMiao ) * 60 + ''
            i = strMiao.indexOf('.')
            strMiao = strMiao.substring(0, i + 4)
            strMiao = parseFloat(strMiao).toFixed(2)
          }
        }
        return strDu + '°' + strFen + '′' + strMiao + '″'
      },
      handleInput(val){
        if(val){
          this.flag=true;
          this.searcList = []
          //调用服务根据名称查询地点信息；
          var address = val
          var geoCodeParam = new SuperMap.GeoCodingParameter({
            address: address,
            fromIndex: 0,
            toIndex: 20,
          })
          var addressMatchService = L.supermap.addressMatchService(
            this.$CONFIG.VUE_APP_SUPERMAP_BASE_URL+'/iserver/services/addressMatch-Index/restjsr/v1/address'
          )
          addressMatchService.code(geoCodeParam, this.codeMatchBest)
        }else{
          this.flag = false;
        }
      },
      selectClick(e){
        this.pointValue = e.y+','+e.x;
        this.pointValueDfm = this.ToDegrees(e.y)+","+this.ToDegrees(e.x)
        this.flag=false;
        this.handleOK(this.pointValue,e.address)
      },
      codeMatchBest(obj){
        var queryResult = obj.result
        if (queryResult && queryResult.length > 0) {
          //console.log('queryResult', queryResult)
          let item = {}
          for (var i = 0; i < queryResult.length; i++) {
            item = queryResult[i]
            const address = this.beautySub(item.address, 30)
            // const address = item.address
            this.searcList.push({
              address: address,
              x: item.location.x,
              y: item.location.y,
              score: item.score,
            })
          }
        } else {
          //htm += '<div class="res-row no-dd-box"><span>无匹配的坐标点</span></div>';
        }
      },
      beautySub(str, len){
        //匹配中文字符
        var reg = /[\u4e00-\u9fa5]/g
        var slice = str.substring(0, len)
        var chineseCharNum = ~~(slice.match(reg) && slice.match(reg).length)
        var realen = slice.length * 2 - chineseCharNum
        return str.substr(0, realen) + (realen < str.length ? '…' : '')
      },
      openModal(){
        this.visible = true
        this.$nextTick(()=>{
          this.$refs.innerMapPointModal.show()
        })
      },
      handleOK(val,name) {
        this.pointValue = val
        this.$emit("change", val,name)
        this.$emit("update:modelValue", val,name)
      },
      handleEmpty(){
        this.handleOK('')
      },
      handleCancel(){
        this.visible = false
      }
    },
    model: {
      prop: 'value',
      event: 'change'
    }
  }
</script>

<style scoped>
  .components-input-demo-presuffix .anticon-close-circle {
    cursor: pointer;
    color: #ccc;
    transition: color 0.3s;
    font-size: 12px;
  }
  .components-input-demo-presuffix .anticon-close-circle:hover {
    color: #f5222d;
  }
  .components-input-demo-presuffix .anticon-close-circle:active {
    color: #666;
  }
  :deep .el-input-group__append{
    padding: 0 10px;
     
    color: #fff;
  }
  .selectOption{
    position: absolute;
    background: #686444 !important;
    color: #fff !important;
    height: 100px;
    font-size: 12px;
    overflow: auto;
    z-index: 111;
  }
</style>