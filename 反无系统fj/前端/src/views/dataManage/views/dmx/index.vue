<template>
  <div class="card-page">
    <el-row :gutter="10" style="height: 100%">
      <el-col :span="4">
        <div class="bg-box">
          <div class="zsk-btn">
            <el-button
              :disabled="currentNode.mllx == 2"
              @click="createKnowledgeEvent('add')"
              type="primary"
              icon="Plus"
              size="default"
              >新增知识库</el-button
            >
            <el-button
              :disabled="!currentNode.id || currentNode.sfyxbj == 2"
              @click="createKnowledgeEvent('edit')"
              size="default"
              plain
              >编辑知识库</el-button
            >
          </div>
          <el-tree
            popper-class="theme-light"
            size="default"
            :data="treeCollection"
            node-key="id"
            :props="defaultProps"
            @node-click="nodeClick"
          >
            <template #default="{ node }">
              <span class="custom-tree-node">
                <span>{{ node.label }}</span>
              </span>
            </template>
          </el-tree>
        </div>
      </el-col>
      <el-col :span="20">
        <div class="bg-box">
          <div style="margin-bottom: 10px; display: flex; align-items: center">
            <div class="file-upload-box">
              <el-button
                :disabled="!currentNode.id"
                size="default"
                icon="Upload"
                type="primary"
                >上传文件</el-button
              >
              <input
                v-if="currentNode.id"
                class="file-upload"
                type="file"
                multiple
                @change="importDataFun"
              />
            </div>
            <!-- <el-button
              @click="setDeleteVisible"
              :disabled="!selectList.length"
              type="danger"
              icon="Delete"
              size="default"
              >删除</el-button
            > -->
          </div>
          <div style="height: calc(100% - 90px)">
            <el-table
              :data="tableList"
              style="width: 100%"
              height="100%"
              ref="tableRef"
              size="default"
              @selection-change="selectionChange"
            >
              <template #empty>
                <el-empty description="暂无数据" image-size="120" />
              </template>
              <el-table-column type="selection" width="55" />
              <el-table-column prop="wjMc" label="名称" sortable />
              <el-table-column prop="rwzt" label="操作" width="150">
                <template #default="scope">
                  <div class="table-operate">
                    <el-button
                      type="primary"
                      size="default"
                      link
                      ><a
                        :href="scope.row.fwqWjlj"
                        class="operate-btn"
                        :download="scope.row.wjMc"
                        @click="downloadFile($event, scope.row)"
                        >下载</a
                      ></el-button
                    >
            <el-divider direction="vertical" />
                    <el-button
                      @click="deleteRow([scope.row])"
                      type="danger"
                      size="default"
                      link
                      >删除</el-button
                    >
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <!-- 分页 -->
          <div class="pagination-blue pagination-box">
            <el-pagination
              :current-page="page.pageNum"
              :small="false"
              layout="total, prev, pager, next, jumper"
              :total="page.total"
              @current-change="handleCurrentChange"
            />
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 新增 编辑 -->
    <CreateKnowledge
      :ywlx="3"
      @setCreateVisible="setCreateVisible"
      ref="createKnowledgeRef"
    />
  </div>
</template>

<script setup>
import { ref, defineProps, onMounted, reactive, watch } from "vue";
// import mammoth from "mammoth";
import AnalysisMoveDlg from "@/views/analysis/components/dlg/analysisMoveDlg.vue";
import CreateKnowledge from "../zjzsk/createKnowledge.vue";
import {
  zskCollection,
  zskFileList,
  uploadZskFile,
  deleteZskFile,
} from "@/api/analysis.js";
import { publicDownloadFile, readFile } from "@/utils/localRequest";
import { ElMessage, ElMessageBox } from "element-plus";
import uploadImg from "@/assets/images/analysis/knowledge/upload.png";
import deleteImg from "@/assets/images/analysis/knowledge/delete.png";
const props = defineProps({
  current: {
    default: "",
    type: String,
  },
  item: {
    default: () => {},
    type: Object,
  },
});

const emits = defineEmits(["defEvent"]);

// 改变时刷新
watch(
  () => props.current,
  (newVal) => {
    if (newVal) {
      if (newVal == props.item.name) {
        reload();
      }
    } else {
      // 隐藏
    }
  }
);

const reload = () => {
  console.log("刷新", props.current);
  getZskCollection();
};

const dlgTitle = "知识库管理";
const currentNode = ref({});

const showDlg = ref(false);
const tableRef = ref();
const customNodeClass = (data) => {
  // 目录
  if (data.mllx == 1) {
    if (data.id == currentNode.value.id) {
      return "ml-class is-select";
    } else {
      return "ml-class";
    }
  }
  // 知识库
  if (data.mllx == 2) {
    if (data.id == currentNode.value.id) {
      return "zsk-class is-select";
    } else {
      return "zsk-class";
    }
  }
  return null;
};
const defaultProps = {
  children: "children",
  label: "mc",
  class: customNodeClass,
};

// 新建 编辑 知识库
const createKnowledgeRef = ref();
const createKnowledgeEvent = (type) => {
  createKnowledgeRef.value.setData(type, currentNode.value);
};

// 节点数据
const treeCollection = ref([]);
const getZskCollection = () => {
  zskCollection({ ywlx: 3 }).then((res) => {
    treeCollection.value = convertRankMenuToTree(res.data);
    // 点击第一条
    // if (treeCollection.value.length > 0) {
    //   nodeClick(treeCollection.value[0]);
    // }
  });
};

// 点击节点
const nodeClick = (node) => {
  if (node.id != currentNode.value.id) {
    currentNode.value = node;
    page.value.pageNum = 1;
    // 获取数据
    getNodeList();
  } else {
    currentNode.value = {};
  }
};

function convertRankMenuToTree(menuData) {
  // 缓存所有菜单项
  const menuMap = {};

  // 将扁平的菜单数据转换为一个可以作为根节点的id的映射
  menuData.forEach((item) => {
    menuMap[item.id] = { ...item, children: [] };
  });

  // 遍历缓存的对象，将子菜单加入到对应的父菜单 children 数组中
  for (const key in menuMap) {
    const item = menuMap[key];

    // 如果fjid存在，则将当前菜单项添加到对应父菜单的children数组中
    if (item.fjId != "0") {
      menuMap[item.fjId].children.push(item);
    }
  }

  // 过滤掉没有父节点的项，它们将是树的根节点
  const tree = [];
  Object.values(menuMap).forEach((item) => {
    if (item.fjId == "0") {
      tree.push(item);
    }
  });
  return tree;
}

// 新增完成
const setCreateVisible = (type) => {
  if (type == "load") {
    getZskCollection();
  }
};

// 分页
const page = ref({
  pageNum: 1,
  pageSize: 10,
  total: 0,
});

// 页数改变
const handleCurrentChange = (e) => {
  page.value.pageNum = e;
  getNodeList();
};

// 查询知识库文件
const tableList = ref([]);
const getNodeList = () => {
  zskFileList({
    zskId: currentNode.value.id,
    pageNum: page.value.pageNum,
    pageSize: page.value.pageSize,
  })
    .then((res) => {
      page.value.total = res.total;
      tableList.value = res && res.rows;
    })
    .catch((err) => {
      ElMessage({ message: err, type: "warning" });
    });
};

// 上传
const uploadLoading = ref(false); // 上传文件的loading
const uploadFiles = ref([]); //上传的文件列表
const importDataFun = (e) => {
  uploadLoading.value = true;
  const formData = new FormData();
  const crtFileList = [];
  for (let i = 0; i < e.target.files.length; i++) {
    formData.append("multipartFiles", e.target.files[i]);
    crtFileList.push({
      name: e.target.files[i].name,
    });
  }

  formData.append("zskId", currentNode.value.id);
  // formData.append("zskNm", crtSelectOption.zskNm);

  uploadZskFile(formData)
    .then(() => {
      uploadFiles.value = crtFileList;
      uploadLoading.value = false;
      //上传文件成功  刷新列表
      ElMessage({ message: "上传成功", type: "success" });
      getNodeList();
      e.target.value = null;
    })
    .catch((err) => {
      console.log(err);
      uploadLoading.value = false;
    });
};

// 删除
const setDeleteVisible = () => {
  ElMessageBox.confirm("确认删除选择数据吗？", "提示", {
    confirmButtonText: "确认",
    cancelButtonText: "取消",
    customClass: "theme-light",
    type: "warning",
  }).then(() => {
    if (selectList.value.length > 1) {
      return ElMessage.warning("每次最多删除单个文件");
    } else {
      deleteFiles();
    }
  });
};
// 删除
const deleteFiles = () => {
  const ids = selectList.value.map((v) => v.id);
  uploadLoading.value = true;
  deleteZskFile(ids)
    .then((res) => {
      ElMessage.success(res.msg);
      //删除文件成功， 更新列表
      page.value.pageNum = 1;
      // 获取数据
      getNodeList();
      uploadLoading.value = false;
      tableRef.value.clearSelection();
      selectList.value = [];
    })
    .catch((err) => {
      ElMessage({ message: err, type: "warning" });
    });
};

const deleteRow = (arrRow) => {
  selectList.value = arrRow;
  setDeleteVisible()
}

// 关闭
const onClose = () => {
  showDlg.value = false;
};

// 下载
const downloadFile = (e, row) => {
  e.preventDefault(); // 阻止默认行为
  publicDownloadFile(row.fwqWjlj, row.wjMc);
};

// 选择
const selectList = ref([]);
const selectionChange = (e) => {
  selectList.value = e;
};
</script>

<style lang="less" scoped>
.card-page {
  height: 100%;
  box-sizing: border-box;
}

.bg-box {
  background: #fff;
  height: 100%;
  border-radius: 5px;
  padding: 15px;
  box-sizing: border-box;
}

.file-upload-box {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  margin-right: 12px;

  .file-upload {
    width: 100%;
    height: 100%;
    position: absolute;
    left: 0;
    top: 0;
    opacity: 0;
    cursor: pointer;
  }
  input[type="file"] {
    cursor: pointer;
  }
}

.pagination-box {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
.operate-btn {
  font-size: 14px;
  margin-right: 0.2vw;
  cursor: pointer;
  color: #000;
  text-decoration: none;
}

.operate-btn:hover {
  color: #5781ec;
}
.custom-tree-node {
  display: flex;
  align-items: center;
}
:deep(.el-tree) {
  max-height: calc(50vh - 55px);
  overflow-y: auto;
  color: #000000d9;
  font-size: 14px;
  font-family: "Gill Sans", "Gill Sans MT", Calibri, "Trebuchet MS", sans-serif;
}

.is-select > .el-tree-node__content span {
  color: #409eff;
}
.ml-class > .el-tree-node__content span {
  /* font-weight: bold; */
}
.zsk-class > .el-tree-node__content span {
  font-weight: normal;
}

:deep(.el-tree-node__content) {
  padding: 8px 0;
  border-bottom: 1px solid #f6f6f6;
}

:deep(.el-table th.el-table__cell) {
  background: #f1f8ff;
  color: #333;
}
:deep(.el-table th.el-table__cell .cell) {
  font-weight: bold;
}

.table-operate {
  display: flex;
  align-items: center;
}

.zsk-btn {
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
}
</style>
// <style scoped lang="less">
// @import "@/assets/css/table.less";
// </style>