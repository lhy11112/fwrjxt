<template>
  <div class="knowSeacher">
    <el-icon class="list-icon" @click="swlSqzkClick"><Minus /></el-icon>
    <div class="seacher-content">
      <div class="contentAiwu" id="analysisAITalk">
        <div
          :class="item.isAiwu ? 'row aiwu' : 'row user'"
          v-for="(item, idx) in talkArr"
          :key="idx"
        >
          <div>
            <div
              :class="
                item.isFirst
                  ? 'contentTip'
                  : !item.msg && !item.isFirst && !item.deepMsg
                  ? 'contentTip'
                  : item.msg && !item.deepMsg
                  ? 'contentTip'
                  : 'content'
              "
            >
              <div v-if="item.isFirst" class="first-content">
                <div class="first-title">
                  <!-- 我是艾武参谋，已完成作业准备， -->
                  您好，请指示。
                </div>
                <div class="module-box">
                  <div
                    v-for="(moduleItem, moduleIdx) in moduleList"
                    :key="moduleIdx"
                  >
                    <img :src="moduleItem.img" alt="" />
                    <div>
                      <div class="module-title">{{ moduleItem.title }}</div>
                      <div class="module-des">{{ moduleItem.des }}</div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="item.deepMsg && deepSeek" style="margin: 10px 0">
                <el-collapse class="aiwu-el-collapse" v-model="item.flagShow">
                  <el-collapse-item
                    :title="
                      !item.msg && item.flagShow && item.flagShow.length > 0
                        ? '深度思考中...'
                        : '已深度思考'
                    "
                    :name="1"
                  >
                    <div class="sdsk-msg">
                      <div
                        class="sdsk-msg"
                        v-html="
                          markdownToHtml(item.deepMsg.replace('undefined', ''))
                        "
                      ></div>
                    </div>
                  </el-collapse-item>
                </el-collapse>
              </div>
              <div
                class="make-text"
                v-if="item.msg"
                v-html="markdownToHtml(item.msg.replace('undefined', ''))"
              ></div>

              <div
                v-else-if="!item.msg && !item.isFirst"
                class="loading-container"
              >
                <div class="load-html"></div>
              </div>
              <!-- 判断是否需要显示配块的提示词 -->
              <div v-if="item.hasApp" class="app-list">
                <div
                  v-html="item.hasAppTip"
                  style="
                    padding-left: 0.78vw;
                    font-weight: 600;
                    display: flex;
                    align-items: center;
                  "
                ></div>
              </div>
              <div
                class="file-list"
                v-if="
                  item.msg &&
                  item.chatDocumentList &&
                  item.chatDocumentList.length > 0
                "
              >
                <!-- 显示文档 -->
                <div class="file-list-title">关联的参考资料:</div>
                <div
                  class="file-row"
                  v-for="(fileItem, fileIdx) in item.chatDocumentList"
                  :key="fileIdx"
                >
                  <div class="file-link">
                    <span
                      class="file-name"
                      @click="downloadFileFun(fileItem.url, fileItem.docName)"
                      >{{ fileItem.docName }}</span
                    >

                    <el-icon
                      @click="downloadFileFun(fileItem.url, fileItem.docName)"
                      ><Download
                    /></el-icon>
                  </div>
                  <div class="file-msg" v-if="fileItem.msg">
                    {{ fileItem.msg }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- 发送区域 -->
      <div class="aiwuAnswer">
        <div class="send-message-top">
          <div class="left-top">
            <div class="clear-box" @click="clearTalk">
              <img src="./image/clear.png" alt="" />
              清空
            </div>

            <span class="line"></span>
            <div class="clear-box">
              <span>深度思考启用</span>
              <el-switch
                size="small"
                v-model="deepSeek"
                @change="deepSeekUseFun"
                class="ml-2"
                style="
                  --el-switch-on-color: rgba(64, 158, 255);
                  --el-switch-off-color: #ff4949;
                "
              />
            </div>
            
            <span class="line"></span>
            <div class="clear-box" @click="knowSelectButton">
              <!-- <img src="./image/clear.png" alt="" /> -->
              知识库选择
              <!-- <div v-if="zskMcData">-{{ zskMcData }}</div> -->
              <div class="konwList" v-if="isKnowList">
                <!-- <div
                  class="buttonzsk"
                  v-for="(item, index) in buttonList"
                  :key="index"
                  @click.stop="clickButton(index)"
                >
                  <div style="display: flex; align-items: center">
                    <span
                      class="special"
                      :style="{
                        color: index == currentIndex ? '#0080ff' : '#333',
                      }"
                    >
                      {{ item }}
                      <div
                        class="treeBox"
                        v-if="showTree && index == currentIndex"
                      >
                        <el-select
                          class="theme-light"
                          v-model="zskNms"
                          style="width: 85%"
                          placeholder="请选择"
                          popper-class="theme-light"
                          clearable
                          @change="changeKnow"
                        >
                          <el-option
                            v-for="(item, index) in treeData"
                            :value="item.zskNm"
                            :label="item.zskMc"
                            :key="index"
                          ></el-option>
                        </el-select>
                        
                      </div>
                    </span>
                  </div>
                </div> -->

                <el-tree
                          :data="treeData"
                          :props="defaultProps"
                          @check="handleTreeNodeCheck"
                          :check-on-click-node="true"
                          @node-click="handleNodeClick"
                          :default-checked-keys="defaultTreeCheckedKeys"
                          :default-expanded-keys="[2]"
                          show-checkbox
                          node-key="id"
                          ref="treeZsk"
                        />
              </div>

            </div>
            <span class="line"></span>
            <div @click="knowMange" class="clear-box">知识库管理</div>
            <div @click="wrjMange" class="clear-box">无人机</div>
          </div>
        </div>
        <!-- 文本输入区域 -->
        <textarea
          type="text"
          v-model="sendText"
          id="sendMessageArea"
          :disabled="textDisabled"
          @focus="textFocus"
          @blur="textBlur"
          @keyup.enter="sendMsgToAiwu(true)"
        />
        <div class="btn-box">
          <el-button
            class="send-btn"
            type="primary"
            @click="sendMsgToAiwu(true)"
            v-if="!textDisabled"
          >
            <template #default>
              <img src="./image/send.png" title="发送" alt="" />发送</template
            ></el-button
          >
          <el-button
            class="send-btn"
            type="primary"
            @click="stopClick"
            v-if="textDisabled"
          >
            <template #default>
              <img src="./image/stop.png" title="发送" alt="" />停止</template
            >
          </el-button>
        </div>
      </div>
    </div>
  </div>
  <knowPage v-if="knowDialog" @deleteZsk="deleteZsk" ref="knowRef"></knowPage>
  <wrjPage v-if="wrjDialog" @success="wrjSuccess" ref="wrjRef"></wrjPage>
</template>

<script setup>
import Cookies from "js-cookie";
import MarkdownIt from "markdown-it";
import { ElMessage } from "element-plus";
import { fetchEventSource } from "@microsoft/fetch-event-source";
import { ref, onMounted, nextTick,defineEmits } from "vue";
import knowPage from "./knowPage.vue";
import wrjPage from "./wrjPage.vue";
const visible = ref(false);
const currentIndex = ref(null);
const isKnowList = ref(false);
const deepSeek = ref(false);
const sdskArr = ref([]);
let lastUserMsg = ""; //最后一条用户发送的消息
const talkArr = ref([
  {
    isAiwu: true,

    isFirst: true,
    type: "dzzh",
  },
]);
const showSwlContent = ref(true);
const textDisabled = ref(false);
const talkLoading = ref(false);
const isStopShow = ref(false);
const chatType = ref("glm");
const sendText = ref("");
const zskMcData = ref("");
const activeNames = ref([0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]);
const buttonList = ref(["战法知识库"]);
const defaultProps = ref({
  children: "children",
  label: "zskMc",
  value:"zskNm"
});
const zsknm = ref([]);
const treeData = ref([]);
const defaultTreeCheckedKeys = ref([]);
// 控制树结构是否显示
const showTree = ref(false);
const isMinus = ref(false);
let queryParamsString = null;

const wrjDialog = ref(false);
const wrjRef = ref(null);
const wrjMange = () => {
  wrjDialog.value = true;
  nextTick(() => {
    wrjRef.value.open();
  });
};

const wrjSuccess = (data) =>{
  if(data && data.length){
    sendText.value = `根据所选无人机数据：型号：${data[0].model || 0},序列号：${data[0].serial || 0},经度：${data[0].dronLng || 0},纬度：${data[0].dronLat || 0},海拔高度(米)：${data[0].altitude || 0}
    ,发现站点：${data[0].stationName || 0},起飞点经度：${data[0].homeLng || 0},起飞点纬度：${data[0].homeLat || 0},遥控器经度：${data[0].pilotLng || 0},遥控器纬度：${data[0].pilotLat || 0},高度：${data[0].height || 0},
    ,东速度：${data[0].eastV || 0},北速度：${data[0].northV || 0},上速度：${data[0].upV || 0},频率(U64)：${data[0].freq || 0},信号强度：${data[0].rssi || 0},距离(Km)：${data[0].distance || 0},
    ,飞机角度：${data[0].angle || 0},请以军事口吻分析无人机可能到达的空域`;
  }
  
}
// 切换深度思考
const deepSeekUseFun = () => {
  if (deepSeek.value) {
    chatType.value = "deepseek";
  } else {
    chatType.value = "glm";
    isMinus.value = false;
  }
  talkArr.value = [
    {
      isAiwu: true,
      isFirst: true,
      type: "dzzh",
    },
  ];
};
const emits = defineEmits(["setTLGCDlgMinus"]);
const swlSqzkClick = () => {
  emits("setTLGCDlgMinus");
};
/**** 思维链弹窗最大最小化 */
const setSDSKMinus = () => {
  isMinus.value = false;
  chatType.value = "deepseek";
};
// 知识库change事件
const changeKnow = () => {
  if (zskNms.value != "") {
    knowSelectButton();
    treeData.value.forEach((item) => {
      if (item.zskNm == zskNms.value) {
        zskMcData.value = item.zskMc;
      }
    });
    
  }
};
// 下载文件
const downloadFileFun = (wjlj, wjmc) => {
  if (wjmc.includes("【大模型知识库】")) {
    window.open(wjlj);
  } else {
    const url =
      window.config.VUE_APP_API_BASE_URL + window.config.API_URL + "/sys/common/static/" + wjlj;

    const link = document.createElement("a");
    link.style.display = "none";
    link.href = url;
    link.setAttribute("download", wjmc);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link); //下载完成移除元素
    window.URL.revokeObjectURL(url); //释放掉blob对象
  }
};
// 点击知识库管理
const knowDialog = ref(false);
const knowRef = ref(null);
const knowMange = () => {
  knowDialog.value = true;
  nextTick(() => {
    knowRef.value.open();
  });
};
// 消息内容
const markdownDeepSeek = (markdown) => {
  if (markdown !== undefined) {
    const md = new MarkdownIt({
      html: true,
      break: true,
      xhtmlOut: true,
    });
    let newText = markdown;
    newText = newText.replaceAll("```", "");
    newText = newText.replaceAll("markdown", "");
    return md.render(newText);
  }
};
// 消息内容
const markdownToHtml = (markdown) => {
  if (markdown !== undefined) {
    const md = new MarkdownIt({
      html: true,
      break: true,
      xhtmlOut: true,
    });
    let newText = markdown;
    newText = newText.replace(/`/g, " ");
    newText = newText.replace(/markdown/g, " ");
    // newText = newText.replace(/\-/g, "\n - ");
    newText = newText.replace(/\-\D/g, (match) => {
      const strArray = match.split("");
      strArray.splice(1, 0, " ");
      match = strArray.join("");
      return `\n ${match}`;
    });
    newText = newText.replaceAll("######", "\n # ");
    newText = newText.replaceAll("#####", "\n # ");
    newText = newText.replaceAll("####", "\n # ");
    newText = newText.replaceAll("###", "\n # ");
    newText = newText.replaceAll("##", "\n # ");
    newText = newText.replaceAll("#", "\n # ");
    newText = newText.replaceAll(".**", `. **`);
    newText = newText.replace(/\d\. \*\*/g, (match) => ` \n\n <br>${match}`);
    newText = newText.replaceAll("综上所述", "<br> \n  # 综上所述");
    newText = newText.replaceAll(":", "：");
    newText = newText.replaceAll("**：", "**： <br >");

    return md.render(newText);
  }
};
onMounted(() => {
  talkArr.value = [
    {
      isAiwu: true,
      isFirst: true,
      type: "dzzh",
    },
  ];
  zskNms.value = "";
  zskMcData.value = "";
  visible.value = true;
  // 获取知识库
  // getKnowList();
  knowData()
});
const zskNms = ref("");
// const getKnowList = () => {
//   window.API.model.know.queryByBm({ zskNm: "zsk_fzjcjn" }).then((res) => {
//     if (res.code == 200) {
//       zskNms.value = res.result.zskNm;
//     }
//   });
// };
const promptType = ref("");

// 点击停止按钮
const stopClick = () => {
  isStopShow.value = true;
  // 可以输入
  textDisabled.value = false;
  talkArr.value[talkArr.value.length - 1].isStop = true;
  talkArr.value[talkArr.value.length - 1].assistantData = JSON.parse(
    JSON.stringify(talkArr.value[talkArr.value.length - 1])
  );

  talkLoading.value = false;

  if (talkArr.value[talkArr.value.length - 1].msg == "") {
    talkArr.value[talkArr.value.length - 1].msg = "已停止";
  }
};
const onClose = () => {
  visible.value = false;
};
// 点击知识库选择按钮
const knowSelectButton = () => {
  isKnowList.value = !isKnowList.value;
  if(window.TOOL.data.get("zskData") && window.TOOL.data.get("zskData").length){
        nextTick(()=>{
          if(treeZsk.value){
            treeZsk.value.setCheckedNodes(window.TOOL.data.get("zskData"));
            defaultTreeCheckedKeys.value = window.TOOL.data.get("zskData");
            zskNms.value = window.TOOL.data.get("zskData").map(v=>v.zskNm).join(',');
          }
        })
      }
};
// 点击发送按钮，回车按钮
const sendMsgToAiwu = () => {
  // 调取方法获取选中知识库的数据
  //   getKnowSelect();

  isStopShow.value = false;
  // 根据传入的参数判断是否是在文本框中输入的值，如果为true，则是，如果为false则是在中台点击过来的参数
  if (sendText.value == "") {
    return;
  }
  textDisabled.value = true;
  talkArr.value.push({
    isAiwu: false,
    msg: sendText.value,
  });
  lastUserMsg = sendText.value;
  sendText.value = "";

  talkScroolToBottom();
  addEventListenerMsg(); //获取上下文记录
};
const clearTalk = () => {
  /*** 清空 */
  /*** 清空同时需要重设对话的id */

  talkArr.value = [
    {
      isAiwu: true,
      isFirst: true,
      isChecked: false,
    },
  ];
};
/**** 向量模型 */
const addEventListenerMsg = () => {
  if (deepSeek.value) {
    sdskArr.value.push({
      name: lastUserMsg,
      reasonMsg: "",
      sdskMsgId: "",
    });
  }
  talkScroolToBottom();
  talkArr.value.push({
    isAiwu: true,
    msg: "",
  });
  let firstFlag = true;
  let deepFlag = true;
  let historyJsonData = null; //返回的第一条的json数据， 模型返回内容完成，需要传给后端
  // if(deepSeek.value){
  const url = "/huizhi/chat/streamZsk";

  var queryParams = {
    message: talkArr.value[talkArr.value.length - 2].msg, // 倒数第二条信息为发送的信息
    chatType: chatType.value,
    zskNms: zskNms.value,
  };
  const crtFetchIdx = talkArr.value.length - 1;
  let crtStopFlag = false;
  let chatMsgType = "";
  queryParamsString = new URLSearchParams(queryParams).toString(); // 将参数对象转换为查询字符串
  fetchEventSource(
    `${url}
  `,
    {
      method: "post",
      headers: {
        "X-Access-Token": Cookies.get("TOKEN"),
        "Content-Type": "application/json",
      },
      body: JSON.stringify(queryParams), // 发送JSON格式的数据
      async onopen(response) {
        if (response.ok) {
          // 成功建立连接
          return;
        } else {
          // 后端报500等情况
          const error = await response.json();
          throw new Error(error.detail);
        }
      },
      onmessage(msg) {
        if (
          (talkArr.value[crtFetchIdx] && talkArr.value[crtFetchIdx].isStop) ||
          crtStopFlag ||
          !talkArr.value[crtFetchIdx]
        ) {
          crtStopFlag = true;
          return;
        }
        //首条信息为json
        talkLoading.value = true;
        if (firstFlag) {
          firstFlag = false;
          if (
            msg.data != "根据已知信息无法获取相关信息" &&
            JSON.parse(msg.data)
          ) {
            const JSONData = JSON.parse(msg.data);
            if (
              JSON.parse(msg.data) &&
              JSON.parse(msg.data).chatDocumentList &&
              JSON.parse(msg.data).chatDocumentList.length > 0
            ) {
              talkArr.value[talkArr.value.length - 1].chatDocumentList =
                JSON.parse(msg.data).chatDocumentList;
            }
            historyJsonData = JSONData;
            chatMsgType = JSONData.chatMsgType;
            /***** @chatMsgType  类型：（doc 文档）  （handle 操作类人工干预） */
            /**** 文档 */
            //普通common  配块数据分析pkfx
            if (JSONData.chatMsgType == "doc") {
              /*** 有userMsg时再处理 */
              judgeMessageHasFile(JSONData);
              getThoughtChainInfo(JSONData, (showInfo) => {
                /*** 将处理后的数据和原数据都记录 */
                talkArr.value[talkArr.value.length - 1] = Object.assign(
                  talkArr.value[talkArr.value.length - 1],
                  showInfo
                );
                if (showInfo.appList && showInfo.appList.length) {
                  emit("aiwuModelData", showInfo.appList);
                  analysisConfigStoreNew.setLogAppList(showInfo.appList);
                }
              });
              /**** 操作类 -- 人工干预 */
            } else if (JSONData.chatMsgType == "thoughtChain") {
              judgeMessageHasFile(JSONData);
            } else {
              talkArr.value[talkArr.value.length - 1].msg = JSON.parse(
                msg.data
              ).content;
            }
          } else {
            if (
              JSON.parse(msg.data) &&
              JSON.parse(msg.data).chatDocumentList &&
              JSON.parse(msg.data).chatDocumentList.length > 0
            ) {
              talkArr.value[talkArr.value.length - 1].chatDocumentList =
                JSON.parse(msg.data).chatDocumentList;
            }
            historyJsonData = msg.data;
            talkArr.value[talkArr.value.length - 1].msg = JSON.parse(
              msg.data
            ).content;
          }
        } else {
          // 非首条信息 进行拼接（模型返回文本）
          if (JSON.parse(msg.data) && JSON.parse(msg.data).reasoningContent) {
            talkArr.value[talkArr.value.length - 1].deepMsg += JSON.parse(
              msg.data
            ).reasoningContent;
            talkArr.value[talkArr.value.length - 1].flagShow = [1];
          } else if (JSON.parse(msg.data) && JSON.parse(msg.data).content) {
            talkArr.value[talkArr.value.length - 1].flagShow = [];
            talkArr.value[talkArr.value.length - 1].msg += JSON.parse(
              msg.data
            ).content;
          }
          if (
            JSON.parse(msg.data) &&
            JSON.parse(msg.data).chatDocumentList &&
            JSON.parse(msg.data).chatDocumentList.length > 0
          ) {
            talkArr.value[talkArr.value.length - 1].chatDocumentList =
              JSON.parse(msg.data).chatDocumentList;
          }
        }
        talkScroolToBottom();
        textDisabled.value = true;
      },
      onerror(err) {
        // onopen抛出的异常在onerror也要抛，否则会不断触发重连
        ElMessage({ message: err, type: "error" });
        throw err;
      },
      onclose() {
        textDisabled.value = false;
        talkLoading.value = false;
      },
      // 不设置的话用户离开当前页面会触发重连
      openWhenHidden: true,
    }
  );
};
/**** 将文本对话自动弹到最底部 */
const talkScroolToBottom = () => {
  setTimeout(() => {
    const div = document.getElementById("analysisAITalk");
    div.scrollTop = div.scrollHeight;
  }, 10);
};
// 点击模型按钮
const clickButton = (index) => {
  currentIndex.value = index;
  zskNms.value = "";
  zskMcData.value = "";
  showTree.value = true;
  // treeData.value = [];
  if (currentIndex.value == 0) {
    knowData(1);
  } else if (currentIndex.value == 1) {
    knowData(4);
  }
};
const treeZsk = ref(null);
// 获取下拉树勾选中的节点数据
const handleTreeNodeCheck = (item, treeInfo) => {
  
  console.log(item, treeInfo);
  if(treeInfo.checkedNodes.length){
    zskNms.value = treeInfo.checkedNodes.map(v=>v.zskNm).join(','); //treeZsk.value.getCheckedNodes();
    window.TOOL.data.set('zskData',treeInfo.checkedNodes)
  }else{
    zskNms.value = ''; //treeZsk.value.getCheckedNodes();
    window.TOOL.data.set('zskData',[])
  }
};
const deleteZsk = () => {
  knowData()
}
const knowData = (itemList) => {
  defaultProps.value = {
    children: "children",
    label: "zskMc",
    value:"zskNm"
  };
  defaultTreeCheckedKeys.value = [];
  zsknm.value = [];
  window.API.wjbdHsZsk.list().then((res) => {
    if (res.code == 200) {
      treeData.value = res.result.records;
      if(window.TOOL.data.get("zskData") && window.TOOL.data.get("zskData").length){
        zskNms.value = window.TOOL.data.get("zskData").map(v=>v.zskNm).join(',');
      }
    }
  });
};
</script>

<style scoped lang="less">
.knowSeacher {
  width: 30%;
  height: 74%;
  position: absolute;
  top: 7%;
  right: 0%;
  border: 1px solid #02609f;
  background: rgba(11, 53, 128, 0.7);
  padding: 15px;
  box-sizing: border-box;
  pointer-events: auto;
  .list-icon {
    position: absolute;
    top: 10px;
    right: 10px;
    color: #fff;
    z-index: 2222;
    cursor: pointer;
  }
}
.seacher-content {
  width: 100%;
  height: 100%;
  position: relative;
}
.contentAiwu {
  width: 100%;
  height: 83%;
  padding-bottom: 15px;
  overflow: auto;
  -ms-overflow-style: none; /* Windows 和 Internet Explorer */
  scrollbar-width: none; /* 隐藏滚动条火狐浏览器 */
  box-sizing: border-box;
}
.contentAiwu::-webkit-scrollbar {
  display: none;
}
.aiwuAnswer {
  width: 100%;
  height: 17%;
  position: relative;

  #sendMessageArea {
    width: 100%;
    height: calc(100%);
    border-color: #bad7ff;
    resize: none;
    padding: 0;
    background-color: transparent;
    border-radius: 10px;
    color: #fff;
    //   首行缩进
    text-indent: 2rem;
    //   行距
    line-height: 1.5rem;
    padding-top: 5vh;
    box-sizing: border-box;
  }
  // 文本框聚焦
  #sendMessageArea:focus-visible {
    border-color: #1879fe;
    outline: none;
  }
  .send-message-top {
    position: absolute;
    display: flex;
    border-bottom: 1px solid #e8e8e8;
    width: calc(100% - 20px);
    padding-bottom: 5px;
    box-sizing: border-box;
    margin: 10px;
    font-size: 14px;
    color: #333;
    align-items: center;
    justify-content: space-between;

    .line {
      display: block;
      width: 2px;
      height: 16px;
      margin: 7px 10px;
      background-color: #fff;
      border-radius: 0.03vw;
      opacity: 0.2;
    }
    .clear-box-tip,
    .clear-box {
      padding: 2px 10px;
      cursor: pointer;
      font-size: 1.3vh;
      display: flex;
      align-items: center;
      color: #fff;
      position: relative;
      img {
        margin-right: 5px;
      }
      span {
        font-size: 1.3vh;
        margin-right: 5px;
      }
    }
    .clear-box-tip:hover,
    .clear-box:hover {
      // background-color: #e2ecfc;
      // border-radius: 25px;
    }
    .left-top {
      display: flex;
      align-items: center;
    }
    .stop-box-shai {
      display: flex;
      justify-content: center;
      align-items: center;
      font-size: 1.3vh;
      font-weight: normal;
      font-stretch: normal;
      line-height: 2.04vh;
      letter-spacing: 0vh;
      padding: 0px 10px;
      background-color: #e0f0ff;
      height: 2.41vh;
      border-radius: 0.88vw;
      color: #fff;
      cursor: pointer;
      margin-right: 0.78vw;

      > img {
        margin-right: 3px;
      }
    }
  }
  .btn-box {
    position: absolute;
    right: 20px;
    bottom: 10px;
    display: flex;
    align-items: center;
    // padding-right: 7px;

    .create {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 1vw;
      color: #5881ec;
      font-weight: 600;
      // margin-right: 0.57vw;
      margin-right: 0;
      cursor: pointer;
    }

    .send-btn {
      width: 4.06vw;
      height: 1.56vw;
      background-image: linear-gradient(-90deg, #0080ff 0%, #00b4ff 100%);
      border-radius: 0.78vw;
      margin-left: 0.42vw;

      img {
        margin-right: 0.26vw;
      }
    }

    .send-btn,
    .speech-btn {
      height: 1.56vw;
      display: flex;
      justify-content: center;
      align-items: center;
      color: #fff;
      font-size: 14px;
      cursor: pointer;
      box-sizing: border-box;
    }

    .speech-btn {
      width: 1.56vw;
      height: 1.56vw;
      border-radius: 0.78vw;
      background: transparent;
      background-color: #ecf5ff;
      border: solid 1px #17bbff;
      cursor: pointer;
    }
  }
}
.row {
  margin: 16px 0 0 0;

  > div {
    display: flex;

    .content {
      font-size: 17px;
      padding: 0.26vw 0.94vw;
      line-height: 22px;
      // width: fit-content;
      min-width: 1%;
      max-width: 50%;
    }
  }
}
.row.aiwu {
  .make-text {
    color: #000;
  }
  .first-content {
    .first-title {
      font-size: 0.83vw;
      font-weight: 600;
      color: #000;
      margin-bottom: 0.3vw;
      margin-top: 0.2vw;
      font-size: 14px;
    }

    .first-content-tip {
      line-height: 1.09vw;
      letter-spacing: 0vw;
      color: #fff;
    }

    .module-box {
      display: flex;
      flex-wrap: wrap;
      justify-content: space-between;

      > div {
        width: 9.9vw;
        height: 4.63vh;
        margin-top: 0.93vh;
        background-color: #e6f0ff;
        border-radius: 0.26vw;
        display: flex;
        align-items: center;

        img {
          width: 1.3vw;
          height: 1.3vw;
          margin-left: 0.63vw;
          margin-right: 0.4vw;
        }

        .module-title {
          font-size: 0.73vw;
          line-height: 0.73vw;
          font-weight: 600;
          color: #0d1444;
        }

        .module-des {
          margin-top: 0.56vh;
          font-size: 0.63vw;
          line-height: 0.63vw;
          color: #617f9e;
        }
      }
    }
  }

  .app-list {
    background-color: #e6f0ff;
    border-radius: 0.26vw;
    margin-top: 1vh;
    padding: 0.5vh 0;
    font-size: 1.3vh;
  }

  > div {
    flex-direction: column;

    > div {
      background-color: #fff;
      border-radius: 0 10px 10px 10px;
      border: 1px solid #ccc;
      color: #fff;
    }
  }

  .file-list {
    margin-top: 1.4vh;

    .file-list-title {
      margin-bottom: 0.4vh;
      font-weight: 600;
    }

    .file-row {
      margin-right: 1vw;
      cursor: pointer;
      margin-top: 0.1vh;

      .file-link {
        display: flex;
        align-items: center;
      }

      .file-name {
        margin-right: 0.5vw;
        margin-bottom: 0.2vh;
        text-decoration: underline;
        color: #1879fe;
        text-indent: 1rem;
      }

      i {
        cursor: pointer;
      }

      i:hover {
        color: #1879fe;
      }
    }

    .file-msg {
      margin-top: 0.1vh;
      margin-bottom: 1.1vh;
      text-indent: 1rem;
    }
  }

  .dzzh-choice-list {
    margin-top: 1.4vh;

    span.click-sapn {
      cursor: pointer;
      color: #1879fe;
      text-decoration: underline;
    }

    .info-enoungh {
      .time,
      .area {
        color: #1879fe;
        text-decoration: underline;
        margin-right: 0.2vw;
      }

      .time {
        margin-left: 0.2vw;
      }

      .operate-box {
        margin-top: 0.5vh;

        > div {
          margin-right: 0.5vw;
        }
      }
    }

    .info-more {
      .info-more-title {
        margin-bottom: 0.4vh;
        font-weight: 600;
      }

      .check-info-cont {
        cursor: pointer;
        color: #1879fe;
        text-decoration: underline;
        margin-top: 0.2vh;
      }
    }
  }
}

.row.user {
  margin: 16px 0.8vw 0 2.6vw;
  > div {
    justify-content: end;
    > div {
      background-color: #0080ff;
      color: #fff;
      border: 1px solid #afcfff;
      border-radius: 1.39vh 0vh 1.39vh 1.39vh;
      .md-editor-previewOnly {
        color: #fff !important;
        font-size: 16px !important;
      }
      ::v-deep .md-editor-preview {
        font-size: 14px !important;
      }
    }
  }
  .make-text {
    color: #fff;
  }
}
.trueActive {
  padding: 3px 5px;
  border: 1px solid #ccc;
  margin-left: 10px;
  margin-right: 5px;
  cursor: pointer;
  border-radius: 2px;
}
.falseActive {
  padding: 3px 5px;
  border: 1px solid #ccc;
  cursor: pointer;
  border-radius: 2px;
}
.trueActive:hover {
  background: #afcfff;
}
.falseActive:hover {
  background: #afcfff;
}
::v-deep .el-dialog__body {
  overflow: auto !important;
}
.imageRead {
  padding: 10px;
  background: rgba(220, 220, 220, 0.5);
  border-radius: 3px;
  margin-top: 5px;
}
.fileSetting {
  > div {
    display: flex;
    justify-content: space-between;
    margin: 8px 0;
  }
}
/* 开关的样式 */
::v-deep .el-switch__core {
  background: #ff4949 !important;
  border: 1px solid #ff4949 !important;
}
::v-deep .el-switch.is-checked .el-switch__core {
  background: rgba(0, 176, 255) !important;
  border: 1px solid rgba(0, 176, 255) !important;
}
.load-html {
  width: 30px;
  aspect-ratio: 4;
  --_g: no-repeat radial-gradient(circle closest-side, #0080ff 90%, #0000);
  background: var(--_g) 0% 50%, var(--_g) 50% 50%, var(--_g) 100% 50%;
  background-size: calc(100% / 3) 100%;
  animation: l7 1s infinite linear;
}
@keyframes l7 {
  33% {
    background-size: calc(100% / 3) 0%, calc(100% / 3) 100%, calc(100% / 3) 100%;
  }
  50% {
    background-size: calc(100% / 3) 100%, calc(100% / 3) 0%, calc(100% / 3) 100%;
  }
  66% {
    background-size: calc(100% / 3) 100%, calc(100% / 3) 100%, calc(100% / 3) 0%;
  }
}
.konwList {
  // width: 110px;
  height: 80px;
  position: absolute;
  top: 100%;
  display: flex;
  left: 0;
  z-index: 999;
  flex-direction: column;
  // border: 1px solid #eee;
  padding: 10px;
  .buttonzsk {
    height: 40px;
    display: flex;
    align-items: center;
    cursor: pointer;
    .treeBox {
      position: absolute;
      top: 0%;
      left: 101%;
      padding: 10px;
      box-sizing: border-box;
      min-width: 200px;
      max-width: 300px;
      max-height: 450px;
      //   background-color: rgba(0, 128, 255);
      border-radius: 5px;
      padding-bottom: 15px;
      z-index: 222;
      .tip-button-save {
        display: flex;
        align-items: center;
        flex-direction: row-reverse;
        span {
          background: rgba(0, 128, 255);
          color: #fff;
          padding: 0.3vw 0.5vw;
          border-radius: 3px;
          font-size: 14px;
          cursor: pointer;
        }
      }
      .closedList {
        position: absolute;
        top: 0;
        right: 0;
        width: 10px;
        height: 10px;
        padding: 5px;
      }
      .el-tree {
        max-height: 450px;
        overflow: auto;
        padding-bottom: 15px;
      }
      :deep(.el-checkbox-group) {
        display: flex;
        flex-direction: column;
      }
      :deep(.el-tree-node__content) {
        // margin-top: 0.5vh;
        // padding-bottom: 0.5vh;
        // border-bottom: 1px solid #e8e8e8;
        background-color: #5371a6 !important;
      }
    }
    img {
      width: 14px;
      height: 14px;
      margin-right: 5px;
      margin-top: 3px;
    }
  }
}
:deep(.el-select__placeholder) {
  color: #fff !important;
}
.sdsk-position {
  position: absolute;
  left: 29.5vw;
  top: 5.5vh;
  z-index: 2021;
  background-image: linear-gradient(0deg, #000fff 0%, #c7ddff 100%);
  color: black;
  border: none;
}
.make-text-deep {
  color: rgba(0, 0, 0, 0.88);
}
:deep(.el-collapse-item__header) {
  padding: 0 10px !important;
  border-radius: 5px !important;
}
:deep(.aiwu-el-collapse .el-collapse-item__wrap .el-collapse-item__content) {
  padding: 0 10px !important;
  padding-bottom: 10px !important;
  border-radius: 5px !important;
}
.first-title {
  width: fit-content !important;
}
.contentTip {
  width: fit-content !important;
  font-size: 17px;
  padding: 0.26vw 0.94vw;
  line-height: 22px;
}
::v-deep .my-dlg-body-content {
  padding: 0 10px !important;
}
</style>