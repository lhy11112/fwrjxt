<template>
  <div class="topCenter">
    <!-- <el-form>
      <el-col>
        <el-form-item>
          <scEditor v-model="form.nr" style="width: 100%; height: 435px;pointer-events: auto;background-color:#2d81d0;" />
        </el-form-item>
      </el-col>
    </el-form> -->
    <div class="page-tree">
      <div>
        <el-input
          v-model="treeMc"
          style="width: 98%; margin-bottom: 10px"
          :placeholder="'请输入'"
        />
      </div>
      <el-tree
        ref="treeRef"
        :data="treeData"
        node-key="id"
        :props="defaultProps"
        @node-expand="handleExpandClick"
        @node-click="handleNodeClick"
        :check-strictly="true"
        :highlight-current="true"
        :filter-node-method="groupFilterNode"
      >
        <template #default="{ node }">
          <span :title="node.label" class="custom-tree-node">
            <span>{{ node.label }}</span>
          </span>
        </template>
      </el-tree>
    </div>
    <div class="pageContent-box">
      <div style="text-align: right">
        <el-button :disabled="disabled" @click="download">下载</el-button>
      </div>
      <iframe
        id="fwzfClass"
        :src="wjylUrl"
        width="100%"
        style="height: calc(100% - 37px)"
        frameborder="0"
      ></iframe>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import microApp from "@/components/microApp/index.vue";
import { ref, onMounted, nextTick, watch } from "vue";
import icon1 from "@/assets/leftTitle/leftImg.png";
import wxdzcsb from "@/assets/allImage/wxdzcsb.png";
import scEditor from "@/components/scEditor/index1.vue";
// 定义路由
const router = useRouter();
const wxdsbVisible = ref(false);
const queryInfo = ref({
  isValid: 1,
  name: "",
});
const wxdsbData = ref([]);
const pageOption = ref({
  pageNo: 1,
  pageSize: 10,
});
const total = ref(0);

const form = ref({});

const treeMc = ref("");
// 获取数据
const treeRef = ref(null);
// tree数据显示处理
const defaultProps = ref({
  children: "children",
  label: "mc",
  value: "wjlj",
  id: "id",
});
const treeData = ref([]);
const disabled = ref(false)
// 初始化
onMounted(() => {
  getPkData();
  // window.onload = function() {
  //           var iframe = document.getElementById('fwzfClass');

  //           var doc = iframe.contentDocument || iframe.contentWindow.document;
  //           console.log(doc);
  //           var style = doc.createElement('style');
  //           style.type = 'text/css';
  //           style.textContent = `html::-webkit-scrollbar
  //     {
  //       width: 5px;
  //       height: 10px;
  //       background-color: #b5b1b1;
  //     }
  //      html::-webkit-scrollbar-track       //scroll轨道背景
  //     {
  //       -webkit-box-shadow: inset 0 0 6px rgba(32, 16, 53, 0.664);
  //       box-shadow: inset 0 0 6px rgba(32, 16, 53, 0.664);
  //       border-radius: 10px;
  //       background-color: #21242b;
  //     }

  //     html::-webkit-scrollbar-thumb   //滚动条中能上下移动的小块
  //     {
  //       border-radius: 10px;
  //       -webkit-box-shadow: inset 0 0 6px rgba(12, 147, 226, 0.596);
  //       box-shadow: inset 0 0 6px rgba(12, 147, 226, 0.596);
  //       background-color: #b5b1b1a6;
  //     }`;
  //           doc.head.appendChild(style);
  //       };
  if(window.TOOL.data.get("USER_INFO")){
    disabled.value = window.TOOL.data.get("USER_INFO").orgCode != window.config.FJZDBDNM;
  }
});
const search = () => {};
watch(
  () => treeMc.value,
  (newVal) => {
    treeRef.value.filter(newVal);
  }
);
// 树过滤
const groupFilterNode = (value, data, node) => {
  if (!value) return true;
  console.log(data[defaultProps.value.label]);
  let nowVal = data[defaultProps.value.label]
    ? data[defaultProps.value.label].toUpperCase()
    : "";
  return nowVal.indexOf(value.toUpperCase()) !== -1;
};
const wjylUrl = ref("");
// 文件预览
const filePreview = (file) => {
  // wjylUrl.value = window.config.VUE_APP_API_BASE_URL_WDYL + '/onlinePreview?url='+encodeURIComponent(btoa(encodeURI(window.config.VUE_APP_API_BASE_URL + window.config.API_URL+"/sys/common/static/" + file)))
  wjylUrl.value =
    window.config.VUE_APP_API_BASE_URL_WDYL +
    "/onlinePreview?url=" +
    encodeURIComponent(
      window.Base64.encode(
        window.config.VUE_APP_API_BASE_URL +
          window.config.API_URL +
          "/sys/common/static/" +
          file
      )
    );
};
const wjlj = ref("");
//文档下载
const download = () => {
  window.open(
    window.config.VUE_APP_API_BASE_URL +
      window.config.API_URL +
      "/sys/common/static/" +
      wjlj.value
  );
};
const handleNodeClick = (data, data2) => {
  console.log("11111", data, data2);
  if (data2.level == 2 && data) {
    wjlj.value = data.wjlj;
    filePreview(data.wjlj);
  }
};
const handleExpandClick = (data, data2) => {
  if (data2.level == 1 && data) {
    nextTick(() => {
      getData(data);
    });
  }
};
// 获取页面数据
const loading = ref(false);
const getPkData = () => {
  loading.value = true;
  window.API.wjbdHsZsk.listAll().then((res) => {
    if (res.code == 200) {
      loading.value = false;
      treeData.value = [];
      res.result.forEach((item) => {
        let obj = {};
        obj.mc = item.zskMc;
        obj.id = item.id;
        obj.children = [{}];
        treeData.value.push(obj);
      });
      nextTick(() => {
        if (treeRef.value && treeData.value && treeData.value.length > 0) {
          getData(treeData.value[0]);
        }
      });
    }
  });
};
const getData = (data) => {
  data.children = [];
  window.API.wjbdHsZskWj.listAll({ zskId: data.id }).then((res) => {
    if (res.code == 200) {
      res.result.forEach((item) => {
        let obj = {};
        obj.mc = item.wjMc;
        obj.wjlj = item.fwqWjlj;
        obj.id = item.id;
        obj.children = [];
        data.children.push(obj);
      });
      nextTick(() => {
        if (data.children && data.children.length) {
          treeRef.value.setCurrentKey(data.children[0].id);
          console.log(data.children[0]);
          wjlj.value = data.children[0].wjlj;
          filePreview(data.children[0].wjlj);
          // handleNodeClick(data.children[0],{
          //   id:data.children[0].id,
          //   label:data.children[0].mc,
          //   value:data.children[0].wjlj,
          //   level:2
          // })
        }
      });
    }
  });
};
</script>

<style scoped lang="less">
// @import "@/style/dialog2.css";
.topCenter {
  width: 100vw;
  height: 80.4vh;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  pointer-events: none;
  .left {
    width: 120px;
    height: 140px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 12px;
    color: #fff;
    justify-content: space-around;
    pointer-events: auto;
    .menus {
      width: 100%;
      margin-top: 20px;
      cursor: pointer;
      & > div {
        text-align: center;
      }
    }
  }

  .page-tree {
    width: 22%;
    height: 100%;
    background: url("@/assets/allImage/dialogBg.png") no-repeat;
    background-size: 100% 100%;
    position: absolute;
    left: 30px;
    top: 20px;
    padding: 10px;
    pointer-events: auto;
  }
  .pageContent-box {
    width: 74%;
    height: 100%;
    background: url("@/assets/allImage/dialogBg.png") no-repeat;
    background-size: 100% 100%;
    position: absolute;
    left: calc(22% + 48px);
    top: 20px;
    padding: 10px;
    pointer-events: auto;
    color: #fff;
    overflow: hidden;
    overflow-y: auto;
  }
}
/* 分页样式 */
.el-pager li {
  background: transparent;
  border: 1px solid rgba(115, 116, 117);
  color: #fff;
  margin: 0 5px;
}

.el-pager li.is-active {
  background: rgba(255, 153, 12);
  border: 1px solid rgba(255, 153, 12);
  color: #fff;
}

.el-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 5px;
}
.el-pagination__total {
  color: #fff;
}
.el-pagination__jump {
  color: #fff;
}
.el-pagination button {
  background: transparent;
  border: 1px solid #fff;
}
.el-pagination .btn-next .el-icon,
.el-pagination .btn-prev .el-icon {
  color: #fff;
}
.el-pagination button.is-disabled,
.el-pagination button:disabled {
  background: transparent;
  border: 1px solid #fff;
}
.el-pagination button:hover,
.el-pagination button:hover .el-icon {
  color: #409eff;
}
:deep(.el-tree-node__content) {
  background: transparent;
}
:deep(
    .el-tree--highlight-current
      .el-tree-node.is-current
      > .el-tree-node__content
  ) {
  background-color: #44678f;
}
:deep(.el-tree) {
  font-size: 16px;
}
// 侧边滚轮
:deep(#fwzfClass html::-webkit-scrollbar)    //滚动条整体部分
{
  width: 5px;
  height: 10px;
  background-color: #b5b1b1;
}
:deep #fwzfClas html::-webkit-scrollbar-track       //scroll轨道背景
{
  -webkit-box-shadow: inset 0 0 6px rgba(32, 16, 53, 0.664);
  box-shadow: inset 0 0 6px rgba(32, 16, 53, 0.664);
  border-radius: 10px;
  background-color: #21242b;
}

:deep #fwzfClas html::-webkit-scrollbar-thumb   //滚动条中能上下移动的小块
{
  border-radius: 10px;
  -webkit-box-shadow: inset 0 0 6px rgba(12, 147, 226, 0.596);
  box-shadow: inset 0 0 6px rgba(12, 147, 226, 0.596);
  background-color: #b5b1b1a6;
}
</style>
