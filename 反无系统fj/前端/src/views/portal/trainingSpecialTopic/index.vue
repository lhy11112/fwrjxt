<template>
  <div class="topCenter">
    <div class="buttonContent">
      <div
        :class="currentIndex == index ? 'activeHover' : 'activeNoHover'"
        v-for="(item, index) in buttonList"
        @click="currentCkick(index)"
        :key="index"
      >
        {{ item }}
      </div>
    </div>
    <div class="topleft">
      <div
        class="onePk"
        v-for="(item, index) in contentList.slice(0, 3)"
        :key="index"
      >
        <microApp :componentsItem="item"> </microApp>
      </div>
    </div>
    <div class="topRight">
      <div
        class="onePk"
        v-for="(item, index) in contentList.slice(3, 6)"
        :key="index"
      >
        <microApp :componentsItem="item"> </microApp>
      </div>
    </div>
  </div>
</template>

<script setup>
import microApp from "@/components/microApp/index.vue";
import { ref, onMounted } from "vue";
// 中间按钮的内容
const buttonList = ref(["作战数据大屏", "战场环境大屏", "敌情社情"]);
const microUrl = ref("http://172.16.235.160.2800/shrw");
// 当前页面的配块数据
const contentList = ref([
  { url: "http://172.16.235.160:30899/zzll/", bm: "zzll", pkmc: "作战力量" },
  { url: "http://172.16.235.160:30899/zqjc/", bm: "zqjc", pkmc: "执勤基础" },
  { url: "http://172.16.235.160:30899/zzgz/", bm: "zzgz", pkmc: "政治工作" },
  { url: "http://172.16.235.160:30899/wqzb/", bm: "wqzb", pkmc: "武器装备" },
  { url: "http://172.16.235.160:30899/hqbz/", bm: "hqbz", pkmc: "后勤保障" },
]);
//控制中间按钮的高亮
const currentIndex = ref(0);
// 初始化
const mounted = () => {
  // micro.setData(
  //   props.componentsItem.bm,
  //   {token: getToken(),
  //   userInfo: useUserConfig.userInfo,
  //   updateType: "first"
  // });
};
// 按钮的点击事件
const currentCkick = (index) => {
  currentIndex.value = index;
  if (currentIndex.value == 0) {
    contentList.value = [
      { url: "http://172.16.235.160:30899/zzll/", bm: "zzll", pkmc: "作战力量" },
      { url: "http://172.16.235.160:30899/zqjc/", bm: "zqjc", pkmc: "执勤基础" },
      { url: "http://172.16.235.160:30899/zzgz/", bm: "zzgz", pkmc: "政治工作" },
      { url: "http://172.16.235.160:30899/wqzb/", bm: "wqzb", pkmc: "武器装备" },
      { url: "http://172.16.235.160:30899/hqbz/", bm: "hqbz", pkmc: "后勤保障" },
    ];
  } else if (currentIndex.value == 1) {
    contentList.value = [
      { url: "http://172.16.235.160:30899/attd/", bm: "attd", pkmc: "岸滩通道" },
      { url: "http://172.16.235.160:30899/hysw/", bm: "hysw", pkmc: "海洋水文" },
      { url: "http://172.16.235.160:2802/dljt/", bm: "dljt", pkmc: "道路交通" },
      { url: "http://172.16.235.160:2803/qhqx/", bm: "qhqx", pkmc: "气候气象" },
      { url: "http://172.16.235.160:2805/czjq/", bm: "czjq", pkmc: "城镇街区" },
    ];
  } else if (currentIndex.value == 2) {
    contentList.value = [
      { url: "http://172.16.235.160:2800/snsq/", bm: "snsq", pkmc: "省内社情" },
      { url: "http://172.16.235.160:2801/zyrw/", bm: "zyrw", pkmc: "重要人物" },
      { url: "http://172.16.235.160:2802/zyss/", bm: "zyss", pkmc: "重要设施" },
      { url: "http://172.16.235.160:2803/zdbp/", bm: "zdbp", pkmc: "政党帮派" },
      { url: "http://172.16.235.160:2805/dqrd/", bm: "dqrd", pkmc: "敌情热点" },
    ];
  }
};
</script>

<style scoped lang="less">
.topCenter {
  width: 100vw;
  height: 93.4vh;
  display: flex;
  justify-content: space-between;
  position: relative;
  .buttonContent {
    position: absolute;
    z-index: 10;
    display: flex;
    top: 15px;
    left: 33.5vw;
    color: #fff;
    justify-content: center;
    width: 33%;
    div:nth-child(2) {
      margin: 0 40px;
    }
  }
  .topleft {
    width: 25vw;
    height: 93.4vh;
    margin-left: 10px;
  }
  .topRight {
    width: 25vw;
    height: 93.4vh;
    margin-right: 10px;
  }
  .onePk {
    width: 25vw;
    height: 30vh;
  }
  .onePk:nth-child(2) {
    margin-top: 10px;
  }
  .onePk:nth-child(3) {
    margin-top: 10px;
  }
  .onePkList {
    width: 25vw;
    height: 60.92vh;
    position: relative;
  }
  .activeHover {
    background: url("../../../assets/allImage/childHover.png") no-repeat;
    background-size: 100% 100%;
    padding: 5px 30px;
    cursor: pointer;
  }
  .activeNoHover {
    background: url("../../../assets/allImage/childNoHover.png") no-repeat;
    background-size: 100% 100%;
    padding: 5px 30px;
    cursor: pointer;
  }
}
</style>