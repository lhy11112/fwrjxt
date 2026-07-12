<template>
  <div class="analysis-nav">
    <!-- <img :src="backgroundImg" class="background" alt="" /> -->
    <div class="analysis-left">
      <!-- <img :src="processLogo" :alt="processLogo" /> -->
      <!-- <div class="line"></div> -->
      <span style="color: #0080ff;margin-left:10px;">目标防卫场景反无人机应用</span>
    </div>

    <div class="analysis-right">
      <!-- <div @click="returnBack">
        <img :src="dapingImg" alt="" />
        <span>返回</span>
      </div> -->
      <div @click="returnBack">
        <img :src="dapingImg" alt="" />
        <span>大屏</span>
      </div>
      <span class="line"></span>
      <el-popover popper-class="theme-light" placement="bottom-start" :width="270" trigger="click">
        <template #reference>
          <div class="yh-btn">
            <img :src="yonghuImg" alt="" />
            <span>{{ userInfo.orgCodeTxt }}</span>
          </div>
        </template>
        <template #default>
          <div class="m-box">
            <!-- <div class="m-line">姓名：{{ userInfo.deptName }}</div>
            <div class="m-line">性别：</div>
            <div class="m-line">职位：</div> -->
            <!-- <div class="m-line">级别：{{ userInfo.yhjb }}</div> -->
            <div class="m-line">用户名：{{ userInfo.xm }}</div>
            <div class="m-line">单位：{{ userInfo.orgCodeTxt }}</div>
            <!-- <div class="m-line-btn-analysis" @click="topTalk">
              <img style="width: 13px;height: 12px" :src="fxIcon" />
              <span>分析系统</span>
            </div> -->
            <!-- <div class="m-line-btn-analysis">
              <img style="width: 14px;height: 13px" :src="shezhiImg" />
              <span>设置</span>
            </div> -->
            <div class="m-line-btn-analysis" @click="logout">
              <img  style="width: 13px;height: 13px"  :src="tuichuImg" alt="" />
              <span>退出</span>
            </div>
          </div>
        </template>
      </el-popover>
      <!-- <div>
        <img :src="shezhiImg" alt="" />
        <span>设置</span>
      </div>
      <span class="line"></span>
      <div @click="logout">
        <img :src="tuichuImg" alt="" />
        <span>退出</span>
      </div> -->
    </div>
  </div>
  <logoutDlg />
</template>
<script setup>
import { nextTick, ref, onMounted } from "vue";
import knowledgeImg from "@/assets/images/nav/analysis/knowledge.png";
import processLogo from "@/assets/images/nav/analysis/艾武大模型+图标.png";
import dapingImg from "@/assets/images/nav/analysis/daping.png";
import shezhiImg from "@/assets/images/nav/analysis/shezhi.png";
import tuichuImg from "@/assets/images/nav/analysis/tuichu.png";
import yonghuImg from "@/assets/images/nav/analysis/yonghu.png";
import logoutDlg from "@/components/dlg/logout.vue";
import { useUserConfigStore } from "@/store/modules/user";
import { useRouter } from "vue-router";
import { Logout } from "@/api/location.js";
import { ElMessage } from "element-plus";
import tipImg from "@/assets/images/analysis/tip.png";
import fxIcon from "../assets/分析系统.png";
const userConfigStore = useUserConfigStore(); //用户信息的store
const router = useRouter();

// 分析系统
const tipMessageRef = ref(null);
const topTalk = () => {
  const newUrl = router.resolve({
    path: "/analysis",
  });
  window.open(newUrl.href, "_blank");
};

const currentIndex = ref(null);
const buttonList = ref([
  {
    text: "自有知识库",
    checked: false,
  },
  {
    text: "数据中台",
    checked: false,
  },
  {
    text: "大模型",
    checked: false,
  },
]);

const userInfo = ref({});


// 定义树结构的数据
const treeData = ref([]);
const treeRef = ref(null);
// 控制树结构是否显示
const showTree = ref(false);
const defaultProps = ref({
  children: "children",
  label: "label",
  // disabled: "disabled",
});
let defaultTreeCheckedKeys = [];

onMounted(() => {
  zskCollectionFun();
  if(window.TOOL.data.get("USER_INFO")){
    userInfo.value = window.TOOL.data.get("USER_INFO")
  }
});

// const dpRouter = async () => {
//     const newUrl = router.resolve({
//       path: "/portal",
//     });
//     window.open(newUrl.href, "_blank");
// };
const returnBack = () => {
  // if (window.history.length > 1) {
  //   router.go(-1)
  // } else {
    router.push('/portal')
  // }
}
const logout = () => {
  // userConfigStore.updateLogoutDlgFlag(!userConfigStore.logoutDlgFlag);
  window.TOOL.data.clear();
  window.location.href = window.config.VUE_APP_CAS_BASE  //+ "/logout?service=" + serviceUrl;
};

const openKnowledge = () => {
  eventBus.emit("showKnowledge");
};
// 点击模型按钮
const clickButton = (index) => {
  currentIndex.value = index;
  showTree.value = true;
  // if (currentIndex.value == 0) {
  //   // zskCollectionFun();
  // } else {
  //   showTree.value = true;
  //   treeData.value = [];
  // }
};
/*** 查询知识库列表 */
const zskCollectionFun = () => {
  showTree.value = false;
  // zskCollection().then((res) => {
  //   if (res.code == 200) {
  //     defaultTreeCheckedKeys = [];
  //     showTree.value = true;
  //     treeData.value = [];
  //     /*** sfxz 0 未选择 1 选择 */
  //     res &&
  //       res.data.forEach((item) => {
  //         if (item.zskLx == 1) {
  //           treeData.value.push({
  //             id: item.id,
  //             label: item.zskMc,
  //           });
  //           if (item.sfxz == 1) {
  //             defaultTreeCheckedKeys.push(item.id);
  //           }

  //           if (defaultTreeCheckedKeys.length == treeData.value.length) {
  //             buttonList.value[0].checked = true;
  //           } else {
  //             buttonList.value[0].checked = false;
  //           }
  //         } else if (item.zskLx == 2) {
  //           if (item.sfxz == 0) {
  //             buttonList.value[1].checked = false;
  //           } else {
  //             buttonList.value[1].checked = true;
  //           }
  //           buttonList.value[1].id = item.id;
  //         } else if (item.zskLx == 3) {
  //           if (item.sfxz == 0) {
  //             buttonList.value[2].checked = false;
  //           } else {
  //             buttonList.value[2].checked = true;
  //           }

  //           buttonList.value[2].id = item.id;
  //         }
  //       });
  //   }
  // });
};

const handleTreeNodeCheck = () => {
  const  updateArr = [];
  treeData.value.forEach((item) => {
    //选中状态
    if (treeRef.value[0].getCheckedKeys().indexOf(item.id) > -1) {
      updateArr.push({
        id: item.id,
        sfxz: 1,
      });
    } else {
      //未选中状态
      updateArr.push({
        id: item.id,
        sfxz: 0,
      });
    }
  });
  updateZskCheckedFun(updateArr);
  // filterCheckedId(treeInfo.checkedKeys);
};
const updateZskCheckedFun = (data) => {
  // zskAllInfoList.value.forEach(item => {
  //     if (checkedList.indexOf(item.label) > -1) {
  //     }
  // })
  updateZskChecked(data).then(() => {
    console.log("勾选状态 更新至数据库成功");
    zskCollectionFun();
  });
};
const closeList = () => {
  showTree.value = false;
  currentIndex.value = -1;
};

const allCheckedFun = (isChecked, idx) => {
  const  updateArr = [];
  if (idx == 0) {
    treeData.value.forEach((item) => {
      updateArr.push({
        id: item.id,
        sfxz: isChecked ? 1 : 0, // 1选中 0 未选中
      });
    });
  } else if (idx == 1) {
    updateArr.push({
      id: buttonList.value[1].id,
      sfxz: isChecked ? 1 : 0, // 1选中 0 未选中
    });
  } else if (idx == 2) {
    updateArr.push({
      id: buttonList.value[2].id,
      sfxz: isChecked ? 1 : 0, // 1选中 0 未选中
    });
  }
  updateZskCheckedFun(updateArr);
};
</script>
<style lang="less" scoped>
.analysis-nav {
  width: 100vw;
  height: 60px;
  z-index: 10;
  background-image:linear-gradient(90deg, #0e0c40 0%, #0b99ff 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;

  .background {
    width: 100%;
    height: 100%;
    position: absolute;
    left: 0;
    top: 0;
  }

  .analysis-left {
    position: relative;
    z-index: 1;
    display: flex;
    align-items: center;

    img {
      width: 7.5vw;
      height: 2vw;
      margin-left: 0.94vw;
    }

    .line {
      width: 0.1vw;
      height: 2.41vh;
      margin-left: 0.94vw;
      margin-right: 0.73vw;
      background-color: #0080ff;
    }

    span {
      font-size: 1.3vw;
      font-weight: 600;
      font-stretch: normal;
      letter-spacing: 0px;

      font-family: SourceHanSansSC-Bold;
    }
  }
  .analysis-middle {
    width: 410px;
    height: 34px;
    background-color: #ffffff;
    border-radius: 18px;
    border: solid 1px #7ec6ff;
    display: flex;
    align-items: center;
    z-index: 1;
    .titleList {
      width: 70px;
      height: 16px;
      font-family: SourceHanSansSC-Regular;
      font-size: 14px;
      font-weight: normal;
      font-stretch: normal;
      line-height: 17px;
      letter-spacing: 0px;
      color: #333;
      margin-left: 30px;
    }
    .buttonzsk:nth-child(2) {
      width: 110px;
      height: 16px;
      font-family: SourceHanSansSC-Regular;
      font-size: 14px;
      font-weight: normal;
      font-stretch: normal;
      line-height: 17px;
      letter-spacing: 0px;
    }
    .buttonzsk:nth-child(3) {
      width: 100px;
      height: 16px;
      font-family: SourceHanSansSC-Regular;
      font-size: 14px;
      font-weight: normal;
      font-stretch: normal;
      line-height: 17px;
      letter-spacing: 0px;
    }
    .buttonzsk:nth-child(4) {
      width: 90px;
      height: 16px;
      font-family: SourceHanSansSC-Regular;
      font-size: 14px;
      font-weight: normal;
      font-stretch: normal;
      line-height: 17px;
      letter-spacing: 0px;
    }
    .buttonzsk {
      cursor: pointer;
      display: flex;
      align-items: center;
      padding: 0 10px;
      box-sizing: border-box;
      position: relative;

      img {
        width: 14px;
        height: 14px;
        margin-right: 5px;
        margin-top: 3px;
      }
      .treeBox {
        position: absolute;
        top: 250%;
        left: -30%;
        padding: 10px;
        box-sizing: border-box;
        min-width: 200px;
        max-width: 300px;
        // min-height: 150px;
        max-height: 450px;
        background-color: #ffffff;
        border-radius: 5px;
        padding-bottom: 15px;

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
          padding: 10px;
        }
        .el-tree {
          // min-height: 150px;
          max-height: 450px;
          overflow: auto;
          padding-bottom: 15px;
        }
        :deep(.el-checkbox-group) {
          display: flex;
          flex-direction: column;

          // .el-checkbox:last-of-type {
          //     margin-right: 30px;
          // }
        }

        :deep(.el-tree-node__content) {
          margin-top: 0.5vh;
          padding-bottom: 0.5vh;
          border-bottom: 1px solid #e8e8e8;
        }
      }
    }
  }

  .analysis-right {
    display: flex;
    align-items: center;
    position: relative;
    z-index: 1;
    padding-right: 0.5vw;
    font-size: 16px;

    > div {
      height: 1.67vw;
      padding: 0 0.84vw 0 0.73vw;
      border-radius: 16px;
      display: flex;
      justify-content: center;
      align-items: center;
      margin: 0 0.42vw;
      cursor: pointer;
      border: solid 1px transparent;
      color: #333;
      font-size: 14px;

      img {
        margin-right: 0.2vw;
        position: relative;
        top: 0.1vh;
        // width: 0.89vw;
        // height: min-content;
      }
    }

    span.line {
      display: block;
      width: 1px;
      height: 15px;
      background-color: #a0a0a0;
    }

    > div:hover {
      background-color: #fff;
    }
  }
}

.m-box {
  .m-line {
    height: 40px;
    line-height: 40px;
    font-family: "SourceHanSansSC-Regular";
    font-size: 14px;
    letter-spacing: 0px;
    color: #fff;
    border-bottom: 1px solid #02458f;
  }
  .m-line-btn-analysis {
    width: 100%;
    height: 32px;
    border-radius: 5px;
    background-color: #ececec;
    margin-top: 7px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #000;
    & > img {
      width: 12px;
      height: auto;
      margin-right: 5px;
    }
    &:hover {
      background-color: #dee9f9;
    }
  }
}
::v-deep .el-tree__empty-block {
  // min-height: 150px !important;
  max-height: 450px !important;
}
</style>