<template>
  <div class="guanli theme-light">
    <ManageLayout :menuList="menuList" @tabChange="tabChange">
      <template
        v-slot:[item.name]
        v-for="(item, index) in forMenuList"
        :key="index"
      >
        <component
          :is="item.is"
          :current="tabCurrent"
          :item="item"
          @defEvent="defEvent"
        />
      </template>
    </ManageLayout>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import ManageLayout from "./components/manageLayout.vue";
import router from "./router.js"

// 向量管理：业务配块、向量数据、向量类型
// 系统管理：系统设置、缓存数据
// 指挥流程：指挥流程分类、指挥流程阶段、指挥流程配块
// 知识库管理： 自建知识库、数据中台、大模型

// menu列表
const menuList = router.menuList;

// 循环组件使用
const forMenuList = computed(() => {
  const allList = [];
  menuList.forEach((v) => {
    if (v.children && v.children.length > 0) {
      allList.push(...v.children);
    } else {
      allList.push(v);
    }
  });
  return allList;
});
// 当前选中的页面
const tabCurrent = ref("");
const tabChange = (name) => {
  // console.log(name, "tabChange");
  tabCurrent.value = name;
};
</script>

<style lang="less" scoped>
.guanli {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.el-header {
  --el-header-padding: 0;
}
</style>