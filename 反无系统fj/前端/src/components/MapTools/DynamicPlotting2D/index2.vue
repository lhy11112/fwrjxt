<template>
    <div class="dynamic-plotting-2d-container" v-show="dynamicPlottingStore.show">
        <div class="plotting-tabs" style="width: 100%;height: 100%">
            <!-- <div class="tabs-header">
                <div class="tabs-item" v-for="item in tabsItemList" :key="item.id" @click="handleTabItemClick(item)">{{ item.title }}</div>
            </div>
            <div class="tabs-body">
                <div id="plotPanel" class="ztree" title="标绘面板" :style="{opacity: activeTabId === 1 ? 1 : 0, zIndex:  activeTabId === 1 ? 1 : 0}" style="overflow: hidden"></div>
                <div id="stylePanel" title="属性面板" :style="{opacity: activeTabId === 2 ? 1 : 0, zIndex:  activeTabId === 2 ? 1 : 0}" style="overflow-x: hidden;overflow-y: scroll;"></div>
            </div> -->
            <div class="tabs-body" style="position: fixed;left: 0;">
                <div class="tabs-item">标绘面板</div>
                <el-input
                    style="width: 100%;background:#fff;"
                    v-model="search"
                    placeholder="请输入关键字检索标绘"
                    clearable
                  >
                    <template #append>
                        <el-icon @click="querySymbolLib(search)" style="color:#000"><Search/></el-icon>
                    </template>
                  </el-input>
                <div class="tabs-btn">
                    <el-icon title="保存" @click="saveSimulationMap()"><FolderChecked /></el-icon>
                    <el-icon title="取消" @click="cancelDraw()"><CircleClose /></el-icon>
                    <el-icon title="清空" @click="clearLayer()"><Brush /></el-icon>
                    <el-icon title="删除" @click="deleteSymbol()"><Delete /></el-icon>
                </div>
                <div id="plotPanel" class="ztree" title="标绘面板" :style="{opacity: 1, zIndex: 1}" style="overflow: hidden"></div>
            </div>
            <div class="tabs-body" style="position: fixed;right: 0;">
                <div class="tabs-item">属性面板</div>
                <div id="stylePanel" title="属性面板" :style="{opacity: 1, zIndex: 1}" style="overflow-x: hidden;overflow-y: scroll;"></div>
            </div>
        </div>
        <div id="plottingMenu" class="plotting-menu">
            <li><span class="icon iconfont icon-circle-wrong" title="取消标绘" @click="cancelDraw()"></span></li>
            <li>
                <i class="icon iconfont icon-draw"></i>
                <ul style="width: 160px">
                    <li><span class="icon iconfont icon-draw" title="添加图层" @click="addPlottingLayer()"></span></li>
                    <li><span class="icon iconfont icon-lock" title="图层可锁定" @click="setPlottingLayerIsLocked()"></span></li>
                    <li><span class="icon iconfont icon-plot-layer-edit" title="图层可编辑" @click="setPlottingLayerIsEdit()"></span></li>
                    <li><span class="icon iconfont icon-plot-layer-selection" title="图层可选择" @click="setPlottingLayerIsSelected()"></span></li>
                    <li><span class="icon iconfont icon-style" title="清空图层上所有标号" @click="clearLayer()"></span></li>
                </ul>
            </li>
            <li>
                <i class="icon iconfont icon-bound-rectangle-edit"></i>
                <ul style="width: 128px">
                    <li><span class="icon iconfont icon-bound-rectangle-edit" title="外接矩形编辑" @click="editCircusRetangle()"></span></li>
                    <li><span class="icon iconfont icon-node-edit" title="节点编辑" @click="editContorPoints()"></span></li>
                    <li><span class="icon iconfont icon-add-node" title="增加节点" @click="addControlPoints()"></span></li>
                    <li><span class="icon iconfont icon-delete-node" title="删除节点" @click="removeControlPoints()"></span></li>
                </ul>
            </li>
            <li>
                <i class="icon iconfont icon-copy2"></i>
                <ul style="width: 192px">
                    <li><span class="icon iconfont icon-copy2" title="复制标号" @click="copySymbol()"></span></li>
                    <li><span class="icon iconfont icon-shear" title="剪切标号" @click="cutSymbol()"></span></li>
                    <li><span class="icon iconfont icon-paste" title="粘贴标号" @click="pasteSymbol()"></span></li>
                    <li><span class="icon iconfont icon-delete" title="删除标号" @click="deleteSymbol()"></span></li>
                    <li><span class="icon iconfont icon-undo" title="撤销上一次编辑" @click="undo()"></span></li>
                    <li><span class="icon iconfont icon-redo" title="恢复上一次撤销" @click="redo()"></span></li>
                </ul>
            </li>
            <li>
                <i class="icon iconfont icon-cloud-upload"></i>
                <ul style="width: 64px">
                    <li><span class="icon iconfont icon-cloud-upload" title="保存态势图" @click="saveSimulationMap()"></span></li>
                    <li><span class="icon iconfont icon-cloud-download" title="加载态势图" @click="loadSimulationMap()"></span></li>
                </ul>
            </li>
            <li>
                <span class="icon iconfont icon-quanxuan" title="框选" @click="multiSelectModel()"></span>
            </li>
            <li>
                <i class="icon iconfont icon-combination"></i>
                <ul style="width: 96px">
                    <li><span class="icon iconfont icon-combination" title="对多选的标号，创建组合" @click="createGroupObjects()"></span></li>
                    <li><span class="icon iconfont icon-multiflag" title="对多选的旗子类标号，创建多旗" @click="createDrawFlags()"></span></li>
                    <li><span class="icon iconfont icon-decompose" title="解组组合或多旗标号" @click="unGroupObject()"></span></li>
                </ul>
            </li>
            <li>
                <i class="icon iconfont icon-draw-avoid"></i>
                <ul style="width: 96px">
                    <li><span class="icon iconfont icon-draw-avoid" title="避让编辑" @click="drawAvoidRegion()"></span></li>
                    <li><span class="icon iconfont icon-exit-avoid" title="退出避让编辑" @click="doneAvoidEdit()"></span></li>
                    <li><span class="icon iconfont icon-delete-avoid" title="删除避让" @click="deleteAvoidEdit()"></span></li>
                </ul>
            </li>
            <li>
                <i class="icon iconfont icon-align-left"></i>
                <ul style="width: 354px">
                    <li><span class="icon iconfont icon-align-left" title="左对齐" @click="setSymbolAlighLeft()"></span></li>
                    <li><span class="icon iconfont icon-align-right" title="右对齐" @click="setSymbolAlighRight()"></span></li>
                    <li><span class="icon iconfont icon-align-up" title="上对齐" @click="setSymbolAlighUp()"></span></li>
                    <li><span class="icon iconfont icon-align-bottom" title="下对齐" @click="setSymbolAlighDown()"></span></li>
                    <li><span class="icon iconfont icon-vertical-center" title="竖直居中对齐" @click="setSymbolAlighVerticalcenter()"></span></li>
                    <li style="border-right: 1px solid #ccc"><span class="icon iconfont icon-horizontal-center" title="水平居中对齐" @click="setSymbolAlighHorizontalcenter()"></span></li>
                    <li><span class="icon iconfont icon-equal-width" title="等宽" @click="setSymbolEqualWidth()"></span></li>
                    <li><span class="icon iconfont icon-equal-high" title="等高" @click="setSymbolEqualHeight()"></span></li>
                    <li style="border-right: 1px solid #ccc"><span class="icon iconfont icon-equal-width-high" title="等大小" @click="setSymbolEqualWidthHeight()"></span></li>
                    <li><span class="icon iconfont icon-transverse-uniform-distribution" title="横向均匀分布" @click="setSymbolLevelDistribution()"></span></li>
                    <li><span class="icon iconfont icon-portrait-uniform-distribution" title="纵向均匀分布" @click="setSymbolVerticalDistribution()"></span></li>
                </ul>
            </li>
        </div>
    </div>
    <el-dialog
    v-model="centerDialogVisible"
    title="保存标绘"
    width="50%"
    class=""
    :rules="rules"
    destroy-on-close
    center
  >
    <el-form :model="plotFrom" label-width="auto">
      <el-row>
        <el-col :span="12" style="margin-bottom: 10px;">
          <el-form-item label="标绘名称">
            <el-input
              v-model="plotFrom.bhmc"
              placeholder="请输入标绘名称"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12" style="margin-bottom: 10px;">
          <el-form-item label="标绘时间">
            <el-date-picker
              v-model="plotFrom.bhsj"
              style="width: 100%"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择标绘时间"
              :picker-options="pickerOpitons"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12" style="margin-bottom: 10px;">
          <el-form-item label="标绘单位">
            <el-input
              v-model="plotFrom.bhdw"
              placeholder="请输入标绘单位"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12" style="margin-bottom: 10px;">
          <el-form-item label="标绘人">
            <el-input
              v-model="plotFrom.bhr"
              placeholder="请输入标绘人"
              clearable
            />
          </el-form-item>
        </el-col>
        <el-col :span="12" style="margin-bottom: 10px;">
          <el-form-item label="类型">
            <el-select
              v-model="plotFrom.type"
              placeholder="请输入类型"
              clearable>
              <el-option label="蓝方" value="蓝方"></el-option>
              <el-option label="红方" value="红方"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <div style="text-align: right">
        <el-button @click="centerDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="handleOk()"> 确定 </el-button>
      </div>
    </template>
  </el-dialog>
    <!-- <el-dialog
        v-model="centerDialogVisible"
        title="保存标绘"
        width="50%"
        class="ct_dialog ct_footer_dialog"
        destroy-on-close
        center
    >
        <div v-if="taskDetail" style="margin: 10px 0 20px;text-align: center;color: #fff;background: #afada92e;border: 1px solid #b3a7944a;border-radius: 4px;padding: 10px;">
            当前作战时间：开始时间<span style="color: #ff0;">{{taskDetail.kssj}}</span> 预计结束时间<span style="color: #ff0;">{{taskDetail.jssj}}</span>
        </div>
        <el-form :model="plotFrom" ref="plotFromRef" :rules="rules" class="demo-form-inline" label-width="100px">
            <el-row>
                <el-col :span="12">
                    <el-form-item prop="bhmc" label="标绘名称">
                        <el-input v-model="plotFrom.bhmc" placeholder="请输入标绘名称" clearable />
                    </el-form-item>
                </el-col>
                <el-col :span="12">
                    <el-form-item prop="bhsj" label="标绘时间">
                        <el-date-picker
                            v-model="plotFrom.bhsj"
                            style="width: 100%;"
                            type="datetime"
                            value-format="YYYY-MM-DD HH:mm:ss"
                            placeholder="请选择标绘时间"
                            :picker-options="pickerOpitons"
                            clearable
                        />
                    </el-form-item>
                </el-col>
                <el-col :span="12">
                    <el-form-item prop="bhdw" label="标绘单位">
                        <el-input v-model="plotFrom.bhdw" placeholder="请输入标绘单位" readonly clearable />
                    </el-form-item>
                </el-col>
                <el-col :span="12">
                    <el-form-item prop="bhr" label="标绘人">
                        <el-input v-model="plotFrom.bhr" placeholder="请输入标绘人" readonly clearable />
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
        <template #footer>
            <div class="dialog-footer" style="text-align: right;">
                <el-button @click="centerDialogVisible = false">取消</el-button>
                <el-button type="warning" @click="handleOk()">
                确定
                </el-button>
            </div>
        </template>
    </el-dialog> -->
</template>

<script setup>
    import { ElMessage } from 'element-plus'
    import { addPlot } from "@/api/plotting";
    import { onMounted, onUnmounted, reactive, ref, toRef, getCurrentInstance, watch } from "vue";
    import { cfg2D } from "@/utils/Map/config";
    import { useDynamicPlottingStore } from "@/store/modules/dynamicPlotting";
    import { useRoute, useRouter, createWebHashHistory } from "vue-router";
    import tool from '@/utils/tool';
    const search = ref("");
    const taskDetail = ref(null);
    // 定义相当于vue2中的this
    const { ctx } = getCurrentInstance();
    const centerDialogVisible = ref(false)
    const plotFromRef = ref(null)
    const plotFrom = ref({})
    const rules = ref({
        bhmc: [{
          required: true,
          message: '请输入标绘名称',
          trigger: 'change',
        }],
        bhsj: [{
          required: true,
          message: '请选择标绘时间',
          trigger: 'change',
        }],
        bhdw: [{
          required: true,
          message: '请输入标绘单位',
          trigger: 'change',
        }],
        bhr: [{
          required: true,
          message: '请输入标绘人',
          trigger: 'change',
        }],
    })
    const gisArry = ref([])
    const route = useRoute();
    const pickerOpitons = ref({
        disabledDate(time){
            var a = (new Date()).getTime()
            return time.getTime() > Date.now()
        }
    })
    const dynamicPlottingStore = useDynamicPlottingStore();

    const activeTabId = ref(1);
    const tabsItemList = ref([
        {
            id: 1,
            title: "标绘面板"
        },
        {
            id: 2,
            title: "属性面板"
        }
    ])
    const handleTabItemClick = (item) => {
        activeTabId.value = item.id;
    }

    var plottingLayer = null,
        drawControl = null,
        editControl = null,
        plotting = null;
    var symbolLibManager,loadSymbolComplete = false; // 查询标绘

    const serverUrl = `${cfg2D.server.baseUrl1}/plot-WJ/rest/plot`;
   
    const InitPlot = () => {
        //  plottingLayer = L.supermap.plotting.plottingLayer("plot", serverUrl, {enableSymScale:true,symScaleDefinition:0.0008861324786284127});
        // plottingLayer.addTo(Map2D.map);

        // drawControl = L.supermap.plotting.drawControl(plottingLayer);
        // drawControl.addTo(Map2D.map);
        // editControl = L.supermap.plotting.editControl();
        // editControl.addTo(Map2D.map);
        // drawControl.setDrawingLayer(plottingLayer);

        // L.supermap.plotting.initPlotPanel("plotPanel", serverUrl, drawControl);
        // L.supermap.plotting.initStylePanel("stylePanel", serverUrl, Map2D.map.getPlotEditControl());

        // Map2D.map.on('contextmenu', function (event) {
        //     drawControl.handler.disable();
        // })
        plottingLayer = Map2D.plottingLayer;
  plottingLayer.addTo(Map2D.map);
  drawControl = Map2D.drawControl;
  drawControl.addTo(Map2D.map);
  editControl = Map2D.editControl;
  editControl.addTo(Map2D.map);
  // 标绘符号随地图层级变化
  plottingLayer.enableSymScaleDefinition(false);
  drawControl.setDrawingLayer(plottingLayer);
  L.supermap.plotting.initPlotPanel("plotPanel", serverUrl, drawControl);
  L.supermap.plotting.initStylePanel("stylePanel", serverUrl, editControl);

  plotting = L.supermap.plotting.getControl(Map2D.map, serverUrl);
  // 查询标绘
        symbolLibManager = plotting.getSymbolLibManager();
        symbolLibManager.on(SuperMap.Plot.Event.initializecompleted, initializeCompleted);
        symbolLibManager.initializeAsync();
  Map2D.map.on("contextmenu", function () {
    drawControl.handler.disable();
  });
    };

    function initializeCompleted(){
        loadSymbolComplete = true;
    }

    function querySymbolLib(key) {
        
        drawControl.handler.disable();


        // if (!loadSymbolComplete) {
        //     return;
        // }
        if ("" === key) {
            return;
        }


        var result = [];
        for (var i = 0; i < symbolLibManager.getSymbolLibNumber(); i++) {
            var symbolLib = symbolLibManager.getSymbolLibByIndex(i);
            if (null !== symbolLib) {
                var tempResult = symbolLib.querySymbolbyKey(key);
                for (var j = 0; j < tempResult.length; j++) {
                    result.push(tempResult[j]);
                }
            }
        }
        showResult(result);
    }
    function showResult(queryResult) {
        document.all.plotPanel.lastChild.children[0].innerHTML = "";
        if (0 === queryResult.length) {
            return;
        }
        //获取选中的对象
        var container = document.all.plotPanel.lastChild.children[0];
        var table = document.createElement("table");
        var  drawNodeClick = function () {
            drawControl.handler.disable();
            drawControl.handler.libID = this.libID;
            drawControl.handler.code = this.symbolCode;
            drawControl.handler.serverUrl = this.serverUrl;
            drawControl.handler.enable();
        };
        var i = 0;
        var rowLength = (queryResult.length % 3 === 0) ? queryResult.length / 3 : queryResult.length / 3 + 1;
        for (var j = 0; j < rowLength; j++) {
            var tr = document.createElement("tr");
            for (var k = 0; k < 3; k++) {
                if (queryResult[i]) {
                    //存储菜单信息
                    var td = document.createElement("td");
                    var drawNode = document.createElement("div");
                    drawNode.onclick = drawNodeClick;
                    drawNode.style.textAlign = "center";
                    //设置标号的字体居中显示
                    drawNode.style.color = "#000";
                    //设置标号画上的id属性
                    drawNode.id = queryResult[i].libID + "_" + queryResult[i].symbolCode;
                    drawNode.libID = queryResult[i].libID;
                    drawNode.symbolCode = queryResult[i].symbolCode;
                    drawNode.serverUrl = serverUrl;
                    var img = document.createElement("img");
                    //查询symbol的title
                    img.title = queryResult[i].symbolName + "_" + queryResult[i].symbolCode;
                    img.src = queryResult[i].icon;
                    //文本
                    var text = document.createElement("div");
                    //标号的SymbolName
                    text.innerHTML = queryResult[i].symbolName;
                    drawNode.appendChild(img);
                    drawNode.appendChild(text);
                    td.appendChild(drawNode);


                    tr.appendChild(td);
                }
                i++;
            }
            table.appendChild(tr);
        }
        console.log(table);
        container.appendChild(table);
    }

    function eventHandler(event) {
        if (event.keyCode === 46) {
            Map2D.map.getPlotEditControl().deleteSelectedFeatures();
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
        // editControl.deleteSelectedFeatures();
        Map2D.map.getPlotEditControl().deleteSelectedFeatures();
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
        const plottingLayerNew = L.supermap.plotting.plottingLayer(plottingLayerName, serverUrl);
        drawControl.setDrawingLayer(plottingLayerNew);
        plottingLayerNew.addTo(Map2D.map);

        alert("标绘图层" + plottingLayerNew.name + "增加成功!");
    }

    //获取态势图信息，转为JSON
    function saveSimulationMap() {
      cancelDraw()
    //   centerDialogVisible.value = !centerDialogVisible.value
    //   taskDetail.value = tool.data.get("taskData")
    //   plotFrom.value.bhdw = tool.data.get("USER_INFO").orgCodeTxt
    //   plotFrom.value.bhr = tool.data.get("USER_INFO").xm
    //   for (let i = 0; i < Map2D.map.getPlottingLayers()[0].features.length; i++) {
    //     const jsonData = Map2D.map.getPlottingLayers()[0].features[i].toGeoJsonStr()
    //     gisArry.value.push(jsonData)
    //   }
        centerDialogVisible.value = !centerDialogVisible.value;
        for (let i = 0; i < Map2D.map.getPlottingLayers()[0].features.length; i++) {
            const jsonData = Map2D.map.getPlottingLayers()[0].features[i].toGeoJsonStr();
            gisArry.value.push(jsonData);
        }
    }
    //保存态势图
function handleOk() {
  cancelDraw()
  // if (!plotFrom.value.bhmc) {
  //   ElMessage({
  //     message: "请输入标绘名称",
  //     type: "warning",
  //   });
  //   return;
  // }
  // if (!plotFrom.value.bhsj) {
  //   ElMessage({
  //     message: "请选择标绘时间",
  //     type: "warning",
  //   });
  //   return;
  // }
  const now = new Date();
  const num =
    now.getYear().toString() +
    now.getMonth().toString() +
    now.getDate().toString() +
    now.getHours().toString() +
    now.getMinutes().toString() +
    now.getSeconds().toString();
  const plotName = window.TOOL.data.get("POSITION").id + "_" + num;
  const gis = {
    gisjson: JSON.stringify(gisArry.value),
    tsmc: plotName,
    dwid: window.TOOL.data.get("USER_INFO").orgCode,  //JSON.parse(localStorage.USER_INFO)
    dwmc: window.TOOL.data.get("USER_INFO").orgCodeTxt,
    ztId: window.TOOL.data.get("POSITION").id,
    //cjrid:window.TOOL.data.get("USER_INFO").id,//创建人ID
    //cjsj:dayjs().format("YYYY-MM-DD HH:mm:ss") //创建时间
  };
  Object.assign(plotFrom.value, gis);
  console.log(plotFrom);
  addPlot(plotFrom.value).then((res) => {
    if (res.data.success) {
      ElMessage({
        message: "保存态势成功！",
        type: "success",
      });
      centerDialogVisible.value = !centerDialogVisible.value;
      plotting.getSitDataManager().saveAsSmlFile(plotName, function () {});
    } else {
      ElMessage({
        message: "保存态势失败！",
        type: "warning",
      });
    }
  });
}
    // //保存态势图
    // function saveSimulationMap() {
    //     cancelDraw();
    //     plotting.getSitDataManager().saveAsSmlFile("situationMap");
    // }

    function loadSimulationMap() {
        plotting.getSitDataManager().openSmlFileOnServer("situationMap", function (evt) {
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
        if (editControl._avoidEditing === false && editControl.getSelectedFeatures().length === 0) {
            return;
        } else {
            editControl.avoidEdit(true);
        }
    }
    function doneAvoidEdit() {
        editControl.avoidEdit(false);
    }
    function deleteAvoidEdit() {
        if (editControl._avoidEditing === false && editControl.getSelectedFeatures().length === 0) {
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
        editControl.uniformDistribution(SuperMap.Plot.UniformDistributionType.VERTICAL);
    }

    onMounted(() => {
        // 1. 标绘初始化
        InitPlot();
        // 2. 注册删除标绘符号
        addDeleteSeleGeoEvent();
    });

    onUnmounted(() => {
        removeDeleteSeleGeoEvent();
        for (var i = 0; i < Map2D.map.getPlottingLayers().length; i++) {
            Map2D.map.getPlottingLayers()[i].removeAllFeatures();
        }
    });
    eventBus.on("addPlot", (data) => {
        console.log(data);
        addSymbol(data)
    });
    const addSymbol = (data) => {
        const points = [];
        for(let i=0; i<data.points.length; i++) {
            const point = data.points[i];
            points.push(new L.latLng(point[0], point[1]))
        }
        plottingLayer.createSymbol(data.libID, data.symbolCode, points, data.options.uuid ? data.options.uuid : null, {...data.style, fontColor: "#000"}, data.options);
    }

    const deleteSymbolByUuid = (uuid) => {
        const featurePoint = plottingLayer.getFeatureByUuid(uuid);
        if(featurePoint) {
            Map2D.map.getPlotEditControl().selectFeatures(featurePoint);
            Map2D.map.getPlotEditControl().deleteSelectedFeatures();
        }
    }
    //加载态势图
  const  loadSimulationMaps = (items) => {
      if (Map2D.map.getPlottingLayers().length > 0 && items !== 'null') {
        //清除已有标绘
        for (let i = 0; i < Map2D.map.getPlottingLayers().length; i++) {
          Map2D.map.getPlottingLayers()[i].removeAllFeatures()
        }
        console.log('标绘', items)
        //根据结果加载标绘
        if (items.length == 1) {
          for (let i = 0; i < items.length; i++) {
            if (items[i].infoType === 'BH') {
              Map2D.plotting.getSitDataManager().openSmlFileOnServer(items[i].TSMC || items[i].tsmc, evt => {
                if (evt.success) {
                    Map2D.drawControl.setDrawingLayer(evt.sitDataLayers[0])
                    Map2D.plottingLayer = evt.sitDataLayers[0]
                }
              })
            }
          }
        } else {
          console.log('第二次')
          for (let i = 0; i < items.length; i++) {
            if (items[i].infoType === 'BH') {
              Map2D.plotting
                .getSitDataManager()
                .addSmlFileToLayerOnServer(items[i].TSMC || items[i].tsmc, Map2D.plottingLayer, evt => {
                  if (evt.success) {
                    Map2D.drawControl.setDrawingLayer(evt.sitDataLayers[0])
                    Map2D.plottingLayer = evt.sitDataLayers[0]
                  }
                })
            }
          }
        }
      } else {
        for (let i = 0; i < Map2D.map.getPlottingLayers().length; i++) {
          Map2D.map.getPlottingLayers()[i].removeAllFeatures()
        }
      }
    };

    defineExpose({
        addSymbol,
        deleteSymbolByUuid,
        loadSimulationMaps
    })
</script>

<style lang="less" scoped>
    .dynamic-plotting-2d-container {
        // width: 100%;
        height: 100%;
        cursor: pointer;
        // background-color: rgba(16, 44, 64, .8);
        color: #fff;
        position: absolute;
        // left: 1.25rem;
        // top: 2rem;
        z-index: 10;
        // height: 93%;
    }
    #plotPanel {
        height: calc(100% - 150px);
        width: 100%;
        // position: absolute;
        // left: 0;
        // top: 0;
        > div {
            border: none;
        }
    }
    #stylePanel {
        height: calc(100% - 90px);
        width: 100%;
        background-color: #fff;
        // position: absolute;
        // left: 0;
        // top: 0;
    }
    #plottingMenu {
        position: absolute;
        top: 20%;
        z-index: 888;
        left: 25.5rem;
        color: #fff;
        background-color: rgba(16, 44, 64, .8) !important;
    }

    .plotting-tabs {
        .tabs-header {
            height: 30px;
            display: flex;
            background-color: transparent;
        }
        .tabs-item {
            // width: 50%;
            background-color: #fff;
            line-height: 30px;
            text-align: center;
            color: #000;
            font-weight: bold;
            font-size: 14px;
        }
        .tabs-body {
            width: 25rem;
            height: 100%;
            // display: flex;
            // position: relative;
        }
    }
    .tabs-btn{
        height: 30px;
        display: flex;
        align-items: center;
        background-color: #fff;
        border-top: 1px solid #95b8e7;
        i{
            font-size: 20px;
            color: #333;
            margin-left: 10px;
        }
        i:hover{
            color: #95b8e7;
        }
    }

    .ztree {
        background-color: #fff;
        box-sizing: border-box;
    }
    :deep(.el-input__inner){
        color:#000 !important;
    }
</style>
