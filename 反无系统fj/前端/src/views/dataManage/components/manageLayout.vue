<template>
  <div class="guanli theme-light">
    <el-container style="height: 100%;background:#033164;">
      <el-header> <AnalysisNav /> </el-header>
      <el-container style="border-top: 1px solid #ddeaff;height:100%;">
        <el-aside width="210px" style="height:90%;overflow:auto;">
          <el-menu
            class="el-menu-vertical-demo"
            :default-active="selectMenu"
            @select="menuSelect"
          >
            <template v-for="menu in menuList" :key="menu.namd">
              <el-sub-menu
                :index="menu.name"
                v-if="menu.children && menu.children.length > 0"
              >
                <template #title>
                  <img class="icon" :src="menu.icon" alt="">
                  <img class="no-icon" :src="menu.noicon" alt="">
                  <span>{{ menu.title }}</span>
                </template>
                <el-menu-item
                  v-for="menuc in menu.children"
                  :key="menuc.name"
                  :index="menuc.name"
                >
                  <img class="icon" :src="menuc.icon" alt="">
                  <img class="no-icon" :src="menuc.noicon" alt="">
                  <span>{{ menuc.title }}</span>
                </el-menu-item>
              </el-sub-menu>
              <el-menu-item v-else :index="menu.name">
                <img class="icon" :src="menu.icon" alt="">
                <img class="no-icon" :src="menu.noicon" alt="">
                <span>{{ menu.title }}</span>
              </el-menu-item>
            </template>
          </el-menu>
        </el-aside>
        <el-main class="main-box">
          <el-tabs
            v-model="selectMenu"
            type="card"
            class="demo-tabs"
            closable
            @tab-remove="removeTab"
          >
            <!-- <template #add-icon>
              <div class="tab-edit">
                <div>
                  123
                </div>
                <div>
                  45
                </div>
                <div>
                  67
                </div>
              </div>
            </template> -->
            <el-tab-pane
              v-for="item in editableTabs"
              :key="item.name"
              :label="item.title"
              :name="item.name"
            >
              <!-- <template #label>
                <el-dropdown ref="dropdown1" trigger="contextmenu">
                  <span class="el-dropdown-link">{{ item.title }}</span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item>Action 1</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </template> -->
              <!-- 添加if判断处理标签切换未刷新情况 -->
              <div class="tab-content" v-if="selectMenu == item.name">
                <div>
                  <slot :name="item.name"></slot>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { useRoute } from "vue-router";
import { onMounted, ref, watch, nextTick,defineEmits } from "vue";
import AnalysisNav from "./dataManageNav.vue";

const route = useRoute();
const props = defineProps({
  menuList: {
    default: () => [],
    type: Array,
  },
});

const emits = defineEmits(["tabChange"]);
onMounted(()=>{
  menuSelect("sbgl")
})
// 选中项
const selectMenu = ref("");
const menuSelect = (name) => {
  // 点击当前选中
  if (name == selectMenu.value) {
    return;
  }
  // 有历史记录
  if (editableTabs.value.find((v) => v.name == name)) {
    selectMenu.value = name;
    return;
  }
  selectMenu.value = name;
  const allChildren = [];
  props.menuList.forEach((v) => {
    allChildren.push(v);
    if (v.children && v.children.length > 0) {
      allChildren.push(...v.children);
    }
  });
  const title = allChildren.find((v) => v.name == name).title;
  // console.log(title, allChildren);
  if (title) {
    addTab(name, title);
  } else {
    addTab(name, name);
  }
};

const editableTabs = ref([
    // {
    //   title: "操作日志管理",
    //   name: "czrz",
    // },
  //   {
  //     title: "Tab 2",
  //     name: "2",
  //     content: "Tab 2 content",
  //   },
]);

// 添加切换
const addTab = (name, title) => {
  editableTabs.value.push({
    title: title,
    name: name,
  });
  selectMenu.value = name;
};

// 移除
const removeTab = (targetName) => {
  const tabs = editableTabs.value;
  let activeName = selectMenu.value;
  if (activeName === targetName) {
    tabs.forEach((tab, index) => {
      if (tab.name === targetName) {
        const nextTab = tabs[index + 1] || tabs[index - 1];
        if (nextTab) {
          activeName = nextTab.name;
        }
      }
    });
  }

  selectMenu.value = activeName;
  editableTabs.value = tabs.filter((tab) => tab.name !== targetName);
  // 清除最后一个tab时
  if (editableTabs.value.length == 0) {
    selectMenu.value = "";
  }
};


// 选中项
watch(
  () => selectMenu.value,
  (newVal) => {
    nextTick(() => {
      // console.log(newVal);
      emits("tabChange", newVal);
    });
  },
  { immediate: false }
);

watch(
  () => route,
  (newVal) => {
    nextTick(() => {
      if(newVal.query.dz){
        menuSelect(newVal.query.dz)
      }
      
    });
  },
  { immediate: true }
);
// watch(
//   [
//     () => selectMenu.value,
//     () => route,
//   ],
//   (newVal) => {
//     nextTick(() => {
//       console.log(newVal);
//     });
//   },
//   { immediate: false }
// );
</script>

<style lang="less" scoped>
.guanli {
  width: 100vw;
  height: 100vh;
}

// .tab-edit {
//   height: 48px;
//   display: flex;
//   position: absolute;
//   right: 0;
//   box-sizing: border-box;
//   > div {
//     width: 30px;
//     height: 48px;
//     display: flex;
//     align-items: center;
//     justify-content: center;
//     border-left: 1px solid #e5e5e5;
//   }
// }

:deep(.el-menu) {
  border: none;
  background: transparent;
}
.el-aside {
  box-shadow: 0 1px 14px #00152914;
  // background: #033164;
  // background: url("../assets/menuBg.png") no-repeat;
  // background: url("../assets/menuBg-11-4.png") no-repeat;
  // background-size: 100% 100%;
  //   border-right: 1px solid #dcdfe6;
}
:deep(.el-menu-item),:deep(.el-sub-menu__title){
  color: #fff;
}
:deep(.el-sub-menu__title:hover) {
    background: rgba(255, 255, 255, 0.09) 
}
.el-header {
  --el-header-padding: 0;
}

.el-main {
  --el-main-padding: 0px;
  border-radius: 10px;
}

.el-tabs {
  --el-tabs-header-height: 48px;
}
:deep(.el-tabs--card > .el-tabs__header .el-tabs__nav) {
  border: none;
}
:deep(.el-tabs--card > .el-tabs__header .el-tabs__item) {
  border-left: 1px solid transparent;
}
:deep(.el-tabs--card > .el-tabs__header) {
  border-bottom: none;
}
:deep(.el-tabs__header) {
  box-shadow: 0 1px 10px #00152924;
  // border-left: 1px solid #e8ebee;
  margin: 0;
}
:deep(.el-menu-item.is-active) {
  background: #0080ff;
  color: #fff;
}

:deep(.el-sub-menu__title) {
  font-size: 15px;
}
:deep(.el-sub-menu .el-menu-item) {
  font-size: 14px;
}

// 菜单高度
:deep(.el-sub-menu__title),
:deep(.el-sub-menu .el-menu-item) {
  height: 48px;
  line-height: 48px;
}

.main-box {
  height: calc(100vh - 60px);
  overflow-y: auto;
}
.tab-content {
  height: calc(100vh - 68px - 40px);
  // background: #033164;
  border-radius: 4px;
  // border: 1px solid red;
  padding: 10px;
  box-sizing: border-box;
}
.tab-content>div{
  height:calc(100% - 30px);
  // background:#033164;
  padding:15px;
}

.no-icon {
  margin-right: 6px;
}

.icon {
  display: none;
}

:deep(.is-active > .no-icon) {
  display: none;
}
:deep(.is-active > .icon) {
  display: block;
  margin-right: 6px;
}
</style>