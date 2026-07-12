<template>
  <div
    class="dynamic-plotting-2d-container"
    :style="{ display: dynamicPlottingStore.show ? 'block' : 'none' }"
  >
    <div
      id="plotPanel"
      title="标绘面板"
      style="overflow: hidden"
      class="ztree"
    ></div>
    <div id="stylePanel" title="属性面板"></div>
    <div id="plottingMenu" class="plotting-menu">
      <li>
        <span
          class="icon iconfont icon-circle-wrong"
          title="取消标绘"
          @click="cancelDraw()"
        ></span>
      </li>
      <li>
        <i class="icon iconfont icon-draw"></i>
        <ul style="width: 160px">
          <li>
            <span
              class="icon iconfont icon-draw"
              title="添加图层"
              @click="addPlottingLayer()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-lock"
              title="图层可锁定"
              @click="setPlottingLayerIsLocked()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-plot-layer-edit"
              title="图层可编辑"
              @click="setPlottingLayerIsEdit()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-plot-layer-selection"
              title="图层可选择"
              @click="setPlottingLayerIsSelected()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-style"
              title="清空图层上所有标号"
              @click="clearLayer()"
            ></span>
          </li>
        </ul>
      </li>
      <li>
        <i class="icon iconfont icon-bound-rectangle-edit"></i>
        <ul style="width: 128px">
          <li>
            <span
              class="icon iconfont icon-bound-rectangle-edit"
              title="外接矩形编辑"
              @click="editCircusRetangle()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-node-edit"
              title="节点编辑"
              @click="editContorPoints()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-add-node"
              title="增加节点"
              @click="addControlPoints()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-delete-node"
              title="删除节点"
              @click="removeControlPoints()"
            ></span>
          </li>
        </ul>
      </li>
      <li>
        <i class="icon iconfont icon-copy2"></i>
        <ul style="width: 192px">
          <li>
            <span
              class="icon iconfont icon-copy2"
              title="复制标号"
              @click="copySymbol()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-shear"
              title="剪切标号"
              @click="cutSymbol()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-paste"
              title="粘贴标号"
              @click="pasteSymbol()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-delete"
              title="删除标号"
              @click="deleteSymbol()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-undo"
              title="撤销上一次编辑"
              @click="undo()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-redo"
              title="恢复上一次撤销"
              @click="redo()"
            ></span>
          </li>
        </ul>
      </li>
      <li>
        <i class="icon iconfont icon-cloud-upload"></i>
        <ul style="width: 64px">
          <li>
            <span
              class="icon iconfont icon-cloud-upload"
              title="保存态势图"
              @click="saveSimulationMap()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-cloud-download"
              title="加载态势图"
              @click="loadSimulationMap()"
            ></span>
          </li>
        </ul>
      </li>
      <li>
        <span
          class="icon iconfont icon-quanxuan"
          title="框选"
          @click="multiSelectModel()"
        ></span>
      </li>
      <li>
        <i class="icon iconfont icon-combination"></i>
        <ul style="width: 96px">
          <li>
            <span
              class="icon iconfont icon-combination"
              title="对多选的标号，创建组合"
              @click="createGroupObjects()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-multiflag"
              title="对多选的旗子类标号，创建多旗"
              @click="createDrawFlags()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-decompose"
              title="解组组合或多旗标号"
              @click="unGroupObject()"
            ></span>
          </li>
        </ul>
      </li>
      <li>
        <i class="icon iconfont icon-draw-avoid"></i>
        <ul style="width: 96px">
          <li>
            <span
              class="icon iconfont icon-draw-avoid"
              title="避让编辑"
              @click="drawAvoidRegion()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-exit-avoid"
              title="退出避让编辑"
              @click="doneAvoidEdit()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-delete-avoid"
              title="删除避让"
              @click="deleteAvoidEdit()"
            ></span>
          </li>
        </ul>
      </li>
      <li>
        <i class="icon iconfont icon-align-left"></i>
        <ul style="width: 354px">
          <li>
            <span
              class="icon iconfont icon-align-left"
              title="左对齐"
              @click="setSymbolAlighLeft()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-align-right"
              title="右对齐"
              @click="setSymbolAlighRight()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-align-up"
              title="上对齐"
              @click="setSymbolAlighUp()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-align-bottom"
              title="下对齐"
              @click="setSymbolAlighDown()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-vertical-center"
              title="竖直居中对齐"
              @click="setSymbolAlighVerticalcenter()"
            ></span>
          </li>
          <li style="border-right: 1px solid #ccc">
            <span
              class="icon iconfont icon-horizontal-center"
              title="水平居中对齐"
              @click="setSymbolAlighHorizontalcenter()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-equal-width"
              title="等宽"
              @click="setSymbolEqualWidth()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-equal-high"
              title="等高"
              @click="setSymbolEqualHeight()"
            ></span>
          </li>
          <li style="border-right: 1px solid #ccc">
            <span
              class="icon iconfont icon-equal-width-high"
              title="等大小"
              @click="setSymbolEqualWidthHeight()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-transverse-uniform-distribution"
              title="横向均匀分布"
              @click="setSymbolLevelDistribution()"
            ></span>
          </li>
          <li>
            <span
              class="icon iconfont icon-portrait-uniform-distribution"
              title="纵向均匀分布"
              @click="setSymbolVerticalDistribution()"
            ></span>
          </li>
        </ul>
      </li>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from "vue";
import { cfg2D } from "@/utils/Map/config";
import {initPlotPanel} from "./js/PlotPanel.js";
import { useDynamicPlottingStore } from "@/store/modules/dynamicPlotting";
const dynamicPlottingStore = useDynamicPlottingStore();

let plottingLayer = null,
  drawControl = null,
  editControl = null,
  plotting = null;
const serverUrl = `${cfg2D.server.baseUrl1}/plot-WJ/rest/plot`;
const InitPlot = () => {
  plottingLayer = L.supermap.plotting.plottingLayer("plot", serverUrl);
  plottingLayer.addTo(Map2D.map);
  drawControl = L.supermap.plotting.drawControl(plottingLayer);
  drawControl.addTo(Map2D.map);
  editControl = L.supermap.plotting.editControl();
  editControl.addTo(Map2D.map);
  // 标绘符号随地图层级变化
  plottingLayer.enableSymScaleDefinition(false);
  drawControl.setDrawingLayer(plottingLayer);
  L.supermap.plotting.initPlotPanel("plotPanel", serverUrl, drawControl);
  // L.supermap.plotting.initStylePanel("stylePanel", serverUrl, editControl);

  plotting = L.supermap.plotting.getControl(Map2D.map, serverUrl);
  Map2D.map.on("contextmenu", function () {
    drawControl.handler.disable();
  });
};

function eventHandler(event) {
  if (event.keyCode === 46) {
    editControl.deleteSelectedFeatures();
  }
}
// 注册删除标绘符号键盘事件
const addDeleteSeleGeoEvent = () => {
  window.addEventListener("keydown", eventHandler);
};
// 删除注册符号标绘删除事件
const removeDeleteSeleGeoEvent = () => {
  window.removeEventListener("keydown", eventHandler);
};

//取消标绘
function cancelDraw() {
  drawControl.handler.disable();
}

//清空绘制
function clearLayer() {
  cancelDraw();
  // editControl.unselectFeatures();
  for (var i = 0; i < Map2D.map.getPlottingLayers().length; i++) {
    Map2D.map.getPlottingLayers()[i].removeAllFeatures();
  }
}

//删除选中标号
function deleteSymbol() {
  editControl.deleteSelectedFeatures();
}

//复制
function copySymbol() {
  editControl.copy();
}

//剪切
function cutSymbol() {
  editControl.cut();
}

//粘贴
function pasteSymbol() {
  editControl.paste();
}

//添加图层
function addPlottingLayer() {
  cancelDraw();

  const plottingLayerName = "plottingLayer_" + Math.ceil(Math.random() * 1000);
  const plottingLayerNew = L.supermap.plotting.plottingLayer(
    plottingLayerName,
    serverUrl
  );
  drawControl.setDrawingLayer(plottingLayerNew);
  plottingLayerNew.addTo(Map2D.map);

  alert("标绘图层" + plottingLayerNew.name + "增加成功!");
}

//保存态势图
function saveSimulationMap() {
  cancelDraw();
  plotting.getSitDataManager().saveAsSmlFile("situationMap");
}

function loadSimulationMap() {
  plotting
    .getSitDataManager()
    .openSmlFileOnServer("situationMap", function (evt) {
      drawControl.setDrawingLayer(evt.sitDataLayers[0]);
      plottingLayer = evt.sitDataLayers[0];
    });
}

function editCircusRetangle() {
  editControl.setEditMode(SuperMap.Plot.EditMode.EDITCIRCUMRECTANGLE);
}

function editContorPoints() {
  editControl.setEditMode(SuperMap.Plot.EditMode.EDITCONTROLPOINT);
}

function addControlPoints() {
  editControl.setEditMode(SuperMap.Plot.EditMode.ADDCONTROLPOINT);
}
function removeControlPoints() {
  editControl.setEditMode(SuperMap.Plot.EditMode.REMOVECONTROLPOINT);
}
//切换多选模式
function multiSelectModel() {
  editControl.multiSelect();
}

//多选对齐--左对齐
function setSymbolAlighLeft() {
  editControl.align(SuperMap.Plot.AlignType.LEFT);
}

//多选对齐--右对齐
function setSymbolAlighRight() {
  editControl.align(SuperMap.Plot.AlignType.RIGHT);
}

//多选对齐--上对齐
function setSymbolAlighUp() {
  editControl.align(SuperMap.Plot.AlignType.UP);
}

//多选对齐--下对齐
function setSymbolAlighDown() {
  editControl.align(SuperMap.Plot.AlignType.DOWN);
}

//多选对齐--竖直居中对齐
function setSymbolAlighVerticalcenter() {
  editControl.align(SuperMap.Plot.AlignType.VERTICALCENTER);
}

//多选对齐--水平居中对齐
function setSymbolAlighHorizontalcenter() {
  editControl.align(SuperMap.Plot.AlignType.HORIZONTALCENTER);
}

//切换图层是否锁定
function setPlottingLayerIsLocked() {
  if (plottingLayer.getLocked() === true) {
    plottingLayer.setLocked(false);
  } else {
    plottingLayer.setLocked(true);
  }
}

//切换图层是否可编辑模式
function setPlottingLayerIsEdit() {
  if (plottingLayer.getEditable() === true) {
    plottingLayer.setEditable(false);
  } else {
    plottingLayer.setEditable(true);
  }
}

//切换图层是否可选择模式
function setPlottingLayerIsSelected() {
  if (plottingLayer.getSelected() === true) {
    plottingLayer.setSelected(false);
  } else {
    plottingLayer.setSelected(true);
  }
}

function drawAvoidRegion() {
  if (
    editControl._avoidEditing === false &&
    editControl.getSelectedFeatures().length === 0
  ) {
    return;
  } else {
    editControl.avoidEdit(true);
  }
}
function doneAvoidEdit() {
  editControl.avoidEdit(false);
}
function deleteAvoidEdit() {
  if (
    editControl._avoidEditing === false &&
    editControl.getSelectedFeatures().length === 0
  ) {
    return;
  } else if (editControl._avoidEditing === true) {
    editControl.avoidEdit(false);
    editControl.getSelectedFeatures()[0].removeAvoidRegions();
  }
}

//创建组合对象
function createGroupObjects() {
  const features = editControl.getSelectedFeatures();
  if (features.length >= 2) {
    const groupObject = plottingLayer.createGroupObject(features);
    if (!!groupObject) {
      editControl.selectFeatures(groupObject);
    }
    const feature = plottingLayer.features[plottingLayer.features.length - 1];
    const transaction = new SuperMap.Plot.Transaction();
    transaction.transType = SuperMap.Plot.TransactionType.EDIT;
    const transInfo = new SuperMap.Plot.TransactionInfo();
    transInfo.layerId = plottingLayer._leaflet_id;
    transInfo.uuid = feature.uuid;
    transInfo.functionName = "createGroupObject";
    transInfo.undoParams = [feature];
    transInfo.redoParams = [features];
    transaction.transInfos.push(transInfo);
    L.supermap.plotting.getControl().getTransManager().add(transaction);
  }
}

//创建多旗
function createDrawFlags() {
  const features = editControl.getSelectedFeatures();
  if (features.length >= 2) {
    const flagObject = plottingLayer.createFlags(features);
    if (!!flagObject) {
      editControl.selectFeatures(flagObject);
    }
    const transaction = new SuperMap.Plot.Transaction();
    transaction.transType = SuperMap.Plot.TransactionType.EDIT;
    const transInfo = new SuperMap.Plot.TransactionInfo();
    transInfo.layerId = flagObject.layer._leaflet_id;
    transInfo.uuid = flagObject.uuid;
    transInfo.functionName = "createFlags";
    transInfo.undoParams = [flagObject];
    transInfo.redoParams = [features];
    transaction.transInfos.push(transInfo);
    L.supermap.plotting.getControl().getTransManager().add(transaction);
  }
}

//解绑组合对象
function unGroupObject() {
  const features = editControl.getSelectedFeatures();
  for (let i = features.length - 1; i >= 0; i--) {
    if (features[i] instanceof L.supermap.plotting.GroupObject) {
      const subLayers = plottingLayer.unGroupObject(features[i]);
      editControl.selectFeatures(subLayers);
    }
  }
}

function undo() {
  editControl.avoidEdit(false);
  plotting.getTransManager().undo();
  editControl.fire(SuperMap.Plot.Event.featuresmodified); //刷新属性面板
}

function redo() {
  editControl.avoidEdit(false);
  plotting.getTransManager().redo();
  editControl.fire(SuperMap.Plot.Event.featuresmodified); //刷新属性面板
}

//等大
//等宽
function setSymbolEqualWidth() {
  editControl.equalLarge(SuperMap.Plot.EqualLargeType.WIDTH);
}
//等高
function setSymbolEqualHeight() {
  editControl.equalLarge(SuperMap.Plot.EqualLargeType.HEIGHT);
}
//等宽高
function setSymbolEqualWidthHeight() {
  editControl.equalLarge(SuperMap.Plot.EqualLargeType.SAME);
}

//均匀分布
//横向均匀分布
function setSymbolLevelDistribution() {
  editControl.uniformDistribution(SuperMap.Plot.UniformDistributionType.LEVEL);
}

//纵向均匀分布
function setSymbolVerticalDistribution() {
  editControl.uniformDistribution(
    SuperMap.Plot.UniformDistributionType.VERTICAL
  );
}

onMounted(() => {
  // 1. 标绘初始化
  InitPlot();
  // 2. 注册删除标绘符号
  addDeleteSeleGeoEvent();
});

onUnmounted(() => {
  removeDeleteSeleGeoEvent();
  // deleteAllGeo();
  for (var i = 0; i < Map2D.map.getPlottingLayers().length; i++) {
    Map2D.map.getPlottingLayers()[i].removeAllFeatures();
  }
});
// unselectFeatures
const addSymbol = (data) => {
  // if(data && data.libID && data.symbolCode && data.points.length > 0) {
  const points = [];
  for (let i = 0; i < data.points.length; i++) {
    const point = data.points[i];
    points.push(new L.latLng(point[1], point[0]));
  }
  if (data.setView) {
    const targetPoint = data.points[0];
    Map2D.map.setView([targetPoint[1], targetPoint[0]], Map2D.getCurZoom());
  }
  plottingLayer.createSymbol(
    data.libID,
    data.symbolCode,
    points,
    null,
    { ...data.style, fontColor: "#000" },
    data.options
  );
};

defineExpose({
  addSymbol,
});
</script>

<style lang="less" scoped>
.dynamic-plotting-2d-container {
  cursor: pointer;
  background: url(../../../assets/firstPage/sixDesign/lineList.png) no-repeat;
  background-size: 100% 100%;
  color: #fff;
  position: absolute;
  left: 35%;
  top: 12%;
  z-index: 8;
  height: 81%;
  width: 20rem;
}
#plotPanel {
  height: 100%;
  width: 100%;
  > div {
    border: none;
  }
}
#plottingMenu {
  position: absolute;
  top: 0%;
  z-index: 888;
  left: 20.5rem;
  color: #fff;
  background-color: rgba(41, 47, 66, 0.85) !important;
}
</style>
