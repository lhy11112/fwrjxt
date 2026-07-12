<template>
<div v-show="closeFlag">
<AnalysisMoveDlg
    title="飞行预测参数"
    style="width: 30%;height:30vh"
    :visibleDialog="visibleDialog"
    @close="closed"
    isModal="true"
  >
    <el-form
      :model="form"
      :rules="rules"
      :disabled="mode === 'show'"
      ref="dialogForm"
      label-width="120px"
      style="height: calc(100% - 40px);overflow: auto;"
    >
      <el-row>
         <el-col :span="24">
          <el-form-item label="起点" prop="startZbd">
            <el-input v-model="form.startZbd" :placeholder="'请输入'" disabled clearable>
              <template #append><el-icon style="cursor: pointer;" @click="closeDialog"><Location /></el-icon></template>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="终点" prop="endZbd">
            <el-input v-model="form.endZbd" :placeholder="'请输入'" disabled clearable>
              <template #append><el-icon style="cursor: pointer;" @click="closeDialog2"><Location /></el-icon></template>
            </el-input>
          </el-form-item>
        </el-col>
         <!-- <el-col :span="24">
          <el-form-item label="时间(分钟)" prop="jl">
            <el-input v-model="form.sj" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col> -->
        <el-col :span="24" v-if="sdFlag">
          <el-form-item label="飞行速度(米/秒)" prop="sd">
            <el-input v-model="form.sd" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <div class="create-bottom">
      
        <el-button @click="closed">取 消</el-button>

        <el-button  type="primary" @click="submit()"
          >确定</el-button
        >
      </div>
  </AnalysisMoveDlg>
</div>
</template>
<script setup>
import { ref, onMounted,defineExpose,defineEmits,defineProps } from "vue";
import startmarker from "/public/static/startmarker.png"
import endmarker from "/public/static/endmarker.png"
import eventBus from "@/utils/eventBus";

const props = defineProps({
  type: {
    default: () => "",
    type: String,
  },
});
const visibleDialog = ref(false);
const form = ref({})
const emits = defineEmits(["closed","success"]);
const sdFlag = ref(false)
const sd = ref(0)
const xdData = ref({})
const closeFlag = ref(true)
const open = (data,data2,data3) =>{
  form.value.sj="";
  visibleDialog.value = true;
  
  sdFlag.value = data;
  if(data){
    form.value.sd="";
  }
  // if(window.TOOL.data.get('startTyParams')){
  //   form.value.sj = window.TOOL.data.get('startTyParams').fxycSj;
  // }
  if(window.TOOL.data.get('startTyParams')){
    form.value.sd = window.TOOL.data.get('startTyParams').speed;
  }
  xdData.value = data2;
}
defineExpose({ open });

let gjfxStartLayer = null;
let gjfxEndLayer = null;
onMounted(()=>{
  gjfxStartLayer = L.layerGroup()
  gjfxStartLayer.addTo(window.Map2D.map)

  gjfxEndLayer = L.layerGroup()
  gjfxEndLayer.addTo(window.Map2D.map)
})
const showDialog = ()=>{
      // 重新显示弹框
  closeFlag.value = true
  // 清除关闭地图事件
  window.Map2D.map.off('click')
  window.Map2D.map.off('mousemove')
  eventBus.emit("addClick",true)
};
const closeDialog = () => {
      closeFlag.value = false
      clearLayer1()
      if(window.Map2D.map){
        var myTooltip = null;
        window.Map2D.map.on('mousemove',(e)=>{
          if(myTooltip) myTooltip.closePopup();
          // 创建并添加弹出框
          myTooltip = window.L.popup().setLatLng(e.latlng).setContent("点击选择起点").openOn(window.Map2D.map);
        })
        window.Map2D.map.on('click',(e)=>{
            form.value.startZbd = e.latlng.lat+','+e.latlng.lng;
            form.value.wd = e.latlng.lat;
            form.value.jd = e.latlng.lng;

            const marketMarker = window.L.marker([e.latlng.lat, e.latlng.lng], { icon: L.icon({ iconSize: [32, 48], iconUrl: startmarker }) })
            marketMarker.addTo(gjfxStartLayer)

            // 点击选点后重新显示弹框
            showDialog()
          
        })
      }
    };
    const closeDialog2 = () => {
      closeFlag.value = false
      clearLayer2()
      if(window.Map2D.map){
        var myTooltip = null;
        window.Map2D.map.on('mousemove',(e)=>{
          if(myTooltip) myTooltip.closePopup();
          // 创建并添加弹出框
          myTooltip = window.L.popup().setLatLng(e.latlng).setContent("点击选择终点").openOn(window.Map2D.map);
        })
        window.Map2D.map.on('click',(e)=>{
            form.value.endZbd = e.latlng.lat+','+e.latlng.lng;
            form.value.wd2 = e.latlng.lat;
            form.value.jd2= e.latlng.lng;

            const marketMarker = window.L.marker([e.latlng.lat, e.latlng.lng], { icon: L.icon({ iconSize: [32, 48], iconUrl: endmarker }) })
            marketMarker.addTo(gjfxEndLayer)
            // 点击选点后重新显示弹框
            showDialog()
          
        })
      }
    };
const dialogForm = ref({})
const submit = () => {
  dialogForm.value.validate(async (valid) => {
    if (valid) {
      visibleDialog.value = false;
      console.log(form.value.jd2,form.value.wd2);
      emits("success",form.value.sj,form.value.sd,sdFlag.value,form.value.jd,form.value.wd,form.value.jd2,form.value.wd2);
      // gjfxRef.value.open(form.value.sj,form.value.sd,sdFlag.value,form.value.jd,form.value.wd) 
    }
  });
}

const closed = () => {
  visibleDialog.value = false;
  clearLayer1()
  clearLayer2()
  emits("closed");
}

const clearLayer1 = () => {
  if (
      gjfxStartLayer !== undefined &&
      gjfxStartLayer !== "" &&
      gjfxStartLayer !== null
    ) {
      
      gjfxStartLayer.clearLayers()
    }
}
const clearLayer2 = () => {

    if (
      gjfxEndLayer !== undefined &&
      gjfxEndLayer !== "" &&
      gjfxEndLayer !== null
    ) {
      
      gjfxEndLayer.clearLayers()
    }
}
</script>

<style scoped>
.create-bottom {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
}
:deep(.el-input.is-disabled .el-input__wrapper){
  background-color:transparent !important;
}
</style>
