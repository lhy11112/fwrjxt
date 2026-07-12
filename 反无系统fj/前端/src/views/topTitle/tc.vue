<template>
<AnalysisMoveDlg
    title="图层"
    style="width: 40%;height:50vh"
    :visibleDialog="visibleDialog"
    @close="closed"
    :isModal="false"
  >
    <div style="width:100%;height:100%;overflow:auto;">
      <el-tree
              ref="pictureTree"
              v-model="pictureVal"
              :data="pictureData"
              node-key="id"
              :props="pictureProps"
              :filter-node-method="filterNode"
              :default-checked-keys="defaultKeys"
              :default-expand-all="true"
              show-checkbox
              :check-strictly="false"
              @check-change="pictureDataChange"
              @node-click="pictureDataClick"
            />
    </div>
    <!-- <div class="create-bottom">
      
        <el-button @click="closed">取 消</el-button>

        <el-button  type="primary" @click="submit()"
          >确定</el-button
        >
      </div> -->
  </AnalysisMoveDlg>
</template>
<script setup>
// 获取到地图的模式
import { mapModeEnum, getCurrentMapMode } from "@/utils/Map/mapMode";
import {controlImageLayer, controlS3MLayer} from "@/utils/Map/layerOpHook.js";
import { ref, onMounted,defineExpose,defineEmits,nextTick } from "vue";
import { ElMessage } from "element-plus";

const visibleDialog = ref(false);
const form = ref({})
const emits = defineEmits(["closed","success"]);
const pictureTree = ref(null);
// 树结构显示字段
const pictureProps = ref({
  children: "children",
  label: "label",
  value: "id",
});
// 绑定树结构
const pictureVal = ref("");

// 点击图层显示的数据
const pictureData = ref([
  // {
  //   id: 3,
  //   label: "矢量",
  //   children: [],
  // },
  {
    id: 4,
    label: "倾斜摄影",
    children: [],
  },
  {
    id: 5,
    label: "影像",
    children: [],
  },
  // {
  //   id: 6,
  //   label: "三维",
  //   children: [],
  // },
  // {
  //   id: 7,
  //   label: "海图",
  //   children: [],
  // },
  // {
  //   id: 8,
  //   label: "其他",
  //   children: [],
  // },
]);
const open = () =>{
  visibleDialog.value = true;

}
defineExpose({ open });

const defaultKeys = ref([])
onMounted(()=>{
  getTc()
  if(window.TOOL.data.get('tcData')){
    defaultKeys.value  = window.TOOL.data.get('tcData')

    if(window.TOOL.data.get("layerInfo")){
      const arr = window.TOOL.data.get("layerInfo");
      arr.forEach(item=>{
        if(item.dataAssetType ==4 ){
          controlS3MLayer(true,item,false)
        }else if(item.dataAssetType == 5){
          controlImageLayer(true,item,false)
        }
        
      })
      
    }
  }
})
//获取图层数据
const getTc = () => {
  window.API.gisDataService.getCollectDataList({ dataAssetTypeList: [3, 4, 5, 6, 7, 8] }).then((res) => {
    if (res.code == 200) {
      const curMapMode = getCurrentMapMode();
      if (curMapMode === mapModeEnum["3D"]) {
        pictureData.value = [
          // {
          //   id: 3,
          //   label: "矢量",
          //   children: [],
            
          // },
          {
            id: 4,
            label: "倾斜摄影",
            children: [],
          },
          {
            id: 5,
            label: "影像",
            children: [],
            
          },
          // {
          //   id: 6,
          //   label: "三维",
          //   children: [],
            
          // },
          // {
          //   id: 7,
          //   label: "海图",
          //   children: [],
            
          // },
          // {
          //   id: 8,
          //   label: "其他",
          //   children: [],
            
          // },
        ];
      } else if (curMapMode === mapModeEnum["2D"]) {
        pictureData.value = [
          // {
          //   id: 3,
          //   label: "矢量",
          //   children: [],
            
          // },
          {
            id: 4,
            label: "倾斜摄影",
            children: [],
            
          },
          {
            id: 5,
            label: "影像",
            children: [],
            
          },
          // {
          //   id: 6,
          //   label: "三维",
          //   children: [],
            
          // },
          // {
          //   id: 7,
          //   label: "海图",
          //   children: [],
            
          // },
          // {
          //   id: 8,
          //   label: "其他",
          //   children: [],
            
          // },
        ];
      }

      for(var i of res.data){
            if(i.dataServiceList && i.dataServiceList.length){
              var obj = i.dataServiceList.filter(row=>{return row.supplierServiceTypeCode == "SUPERMAP-REST-MAP"})[0];
              if(obj == undefined){
                obj = i.dataServiceList.filter(row=>{return row.supplierServiceTypeCode == "SUPERMAP-REST-DATA"})[0]
                if(obj == undefined){
                   obj = i.dataServiceList.filter(row=>{return row.supplierServiceTypeCode == "SUPERMAP-REST-3D"})[0]
                   if(obj == undefined){
                    continue
                   }else{
                    i.serviceProxyUrl = obj.serviceProxyUrl;
                    i.supplierServiceTypeCode = obj.supplierServiceTypeCode;
                  }
                }else{
                  i.serviceProxyUrl = obj.serviceProxyUrl;
                  i.supplierServiceTypeCode = obj.supplierServiceTypeCode;
                }
              }else{
                i.serviceProxyUrl = obj.serviceProxyUrl;
                i.supplierServiceTypeCode = obj.supplierServiceTypeCode;
              }
            }
            if(i.supplierServiceTypeCode && i.supplierServiceTypeCode != "SUPERMAP-REST-DATA"){
              // if(i.dataAssetType == 3){
              //   pictureData.value[0].children.push(i)
              // }else if(i.dataAssetType == 4){
              //   pictureData.value[1].children.push(i)
              // }else if(i.dataAssetType == 5){
              //   pictureData.value[2].children.push(i)
              // }else if(i.dataAssetType == 6){
              //   pictureData.value[3].children.push(i)
              // }else if(i.dataAssetType == 7){
              //   pictureData.value[4].children.push(i)
              // }else if(i.dataAssetType == 8){
              //   pictureData.value[5].children.push(i)
              // }
              if(i.dataAssetType == 4){
                pictureData.value[0].children.push(i)
              }else if(i.dataAssetType == 5){
                pictureData.value[1].children.push(i)
              }
            }
          }
          pictureData.value = JSON.parse(JSON.stringify(pictureData.value).replaceAll('dataName','label').replaceAll('serviceProxyUrl','id'))

    }
  });
};
var layerArr = []
//图层改变
const pictureDataChange = (node,checked) => {
  console.log(node,checked,pictureTree.value.getCheckedKeys());
  window.TOOL.data.set("tcData",pictureTree.value.getCheckedKeys())
  if(node.dataAssetType){
        var layerInfo = {
          url: node.id,
          name: node.label,
          dataAssetType:node.dataAssetType
        }

        console.log(checked);
        if(checked){
          layerArr.push(layerInfo)
        }else{
          console.log(layerArr.indexOf(layerInfo));
          layerArr.forEach((item,index)=>{
            if(item.name == layerInfo.name){
              layerArr.splice(index,1)
            }
          })
          
        }
        

       window.TOOL.data.set("layerInfo",layerArr)
        // 倾斜摄影或三维时
        if(node.dataAssetType == 4 || node.dataAssetType == 6){
          controlS3MLayer(checked,layerInfo,false)
          
        }else{
          controlImageLayer(checked,layerInfo,false)
        }
  }
  // pictureShow.value = false;
};

const pictureDataClick = (node,data) =>{
        // 倾斜摄影或三维时
        if((node.dataAssetType == 4 && data.checked) || (node.dataAssetType == 6 && data.checked)){
          // controlS3MLayer(true,layerInfo,true)
          Map3D.s3mLayer.flyToLayer(node.label)
        }else if(node.dataAssetType == 5 && data.checked){
          Map3D.imageLayer.flyToLayerByName(node.label)
        }else {
          ElMessage({
              type: "info",
              message: "请先勾选加载图层",
            });
        }
}
const closed = () => {
  visibleDialog.value = false;
  
  emits("closed");
}

</script>

<style scoped>
.create-bottom {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
}
</style>
