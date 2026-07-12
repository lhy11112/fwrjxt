
<template>
  <el-dialog title="知识库管理" v-model="visible" @close="onClose">
    <div class="app-manage" v-loading="loading">
      <div class="topMange">
        <!-- <el-select
          v-model="selectType"
          placeholder="请选择"
          style="width: 32%"
          clearable
          @change="changeList"
          popper-class="theme-light"
        >
          <el-option
            v-for="item in typeList"
            :key="item"
            :label="item"
            :value="item"
          />
        </el-select> -->
        
          <!-- :disabled="selectType == ''" -->
        <el-select
          v-model="selectV"
          placeholder="请选择"
          style="width: 32%"
          clearable
          @change="changeKnow"
          popper-class="theme-light"
        >
          <el-option
            v-for="item in typeOptions"
            :key="item.zskMc"
            :label="item.zskMc"
            :value="item.zskMc"
          />
        </el-select>
        <div
          class="create"
          @click="setCreateVisible"
        >
          新建知识库 +
        </div>
        <el-button
          type="primary"
          @click="xianglhuaVisible"
          :disabled="
            fileListContent && fileListContent.length > 0 ? false : true
          "
        >
          确认向量化
        </el-button>
      </div>
      <div class="upload-box"  v-if="selectV">
        <el-upload
          ref="uploadRef"
          class="upload-demo"
          style="height: 50px"
          drag
          action=""
          multiple
          accept=".doc,.docx,.pdf,.txt"
          :http-request="request"
          :before-upload="before"
          :on-success="successList"
        >
          <div class="el-upload__text">拖拽或 <em>点击上传</em></div>
          <template #tip>
            <div class="el-upload__tip"></div>
          </template>
        </el-upload>
        <div
          v-for="(t, i) in fileListContent"
          :key="i"
          style="
            padding: 0 5px;
            color: #fff;
            font-size: 14px;
            display: block;
            margin-top: 5px;
          "
        >
          <span
            style="
              display: inline-block;
              width: calc(100% - 80px);
              overflow: hidden;
              white-space: nowrap;
              text-overflow: ellipsis;
            "
            >{{ t.fileName }}</span
          >
          <span class="el-upload-list__item-actions" style="float: right">
            <!-- 预览按钮 -->
            <!-- <span @click="filePreview(t)">
              <el-icon><View /></el-icon>
            </span> -->
            <!-- 下载按钮 -->
            <span v-if="!disabled" @click="fileDownload(t)">
              <el-icon><Download /></el-icon>
            </span>
            <!-- 删除按钮 -->
            <span v-if="!disabled" @click="fileRemove(t)">
              <el-icon><Delete /></el-icon>
            </span>
          </span>
        </div>
      </div>
      <div class="titleList">
        <span>知识库文件列表</span>
        <div>
          <el-button :disabled="!selectV" @click="editKnow"
            >编辑知识库名称</el-button
          >
          <el-button
            type="danger"
            :disabled="!selectV"
            @click="delectKnow"
            >删除所选知识库</el-button
          >
        </div>
      </div>
      <el-table
        style="height: 65%"
        :data="tableData"
        @selection-change="handleSelectionChange"
      >
        <template #empty>
          <el-empty description="暂无数据" image-size="120" />
        </template>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="wjMc" label="名称"> </el-table-column>

        <el-table-column width="150" label="操作" align="center">
          <template #default="scope">
            <el-button link class="delectButton" @click="edit(scope.row)"
              >编辑</el-button
            >
            <el-button link class="delectButton" :disabled="disabled" @click="dowm(scope.row)"
              >下载</el-button
            >
            <!-- 删除按钮添加确定提示框 -->
            <el-popconfirm
              v-if="!scope.row.flag"
              title="是否需要删除当前数据"
              confirm-button-text="确认"
              cancel-button-text="取消"
              @confirm="delect(scope.row)"
              @cancel="cancelEvent"
            >
              <template #reference>
                <el-button link class="delectButton">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </el-dialog>
  <!-- 新建知识库 -->
  <addKnowledge
    ref="addKnowledgeRef"
    @getListDataKnow="getListDataKnow"
  ></addKnowledge>

  <addKnowledge1
    ref="addKnowledgeRef1"
  ></addKnowledge1>
</template>

<script setup>
import { ref, defineExpose, nextTick,defineEmits } from "vue";
import axios from "axios";
import addKnowledge from "./addKnowledge.vue";
import addKnowledge1 from "./addKnowledge1.vue";
import { ElMessage } from "element-plus";
// import {
//   zsklistAll,
//   deleteTable,
//   zskWjListAll,
//   deleteBatchzsk,
//   addUpload,
//   deleteZSK,
// } from "@/api/hsLocal";
// import { uploadZskFile } from "@/api/hsModelLocal";
const typeList = ref(['战法知识库'])
const selectType = ref("战法知识库")
const disabled = ref(false)

const emit = defineEmits(["deleteZsk"]);
// 表格数据
const tableData = ref([]);
// 上传文件存储变量
const fileListContent = ref([]);
const visible = ref(false);
//弹框显示的方法
const open = () => {
  loading.value = false;
  visible.value = true;
  fileListContent.value = [];
  if(window.TOOL.data.get("USER_INFO")){
    disabled.value = window.TOOL.data.get("USER_INFO").orgCode != window.config.FJZDBDNM;
  }
  changeList()
};
const typeOptions = ref([])
// 查询知识库下拉
const checkKnow = (item) => {
  typeOptions.value =[];
  window.API.wjbdHsZsk.listAll().then((res) => {
    if (res.code == 200) {
      typeOptions.value = res.result;
    }
  });
}
const selectV = ref("")
// 知识库的change事件
const changeList = () => {
  selectV.value = "";
  tableData.value = [];
  typeOptions.value = [];
  checkKnow();
};
// 知识库下拉
   const changeKnow = () => {
      fileListContent.value = [];
      getKnowData();
    }
    
const zskId = ref("");
const getKnowList = () => {
  window.API.wjbdHsZsk.queryByBm({ zskNm: "zsk_fzjcjn" }).then((res) => {
    if (res.code == 200) {
      zskId.value = res.result.id;
    }
  });
};
//     // 点击新建知识库
const addKnowledgeRef = ref(null);
const setCreateVisible = () => {
  addKnowledgeRef.value.open("add");
};
// 查询知识库列表
    const getKnowData = () => {
      var zskId = "";
      typeOptions.value.forEach((item) => {
        if (item.zskMc == selectV.value) {
          zskId = item.id;
        }
      });
      nextTick(() => {
        // 调取list列表接口
        window.API.wjbdHsZskWj.listAll({ zskId: zskId }).then((res) => {
          if (res.code == 200) {
            tableData.value = res.result;
          }
        });
      });
    };
    const editKnow = () => {
      typeOptions.value.forEach((item) => {
        if (item.zskMc == selectV.value) {
          addKnowledgeRef.value.open("edit")
          addKnowledgeRef.value.setData(item);
        }
      });
    }
    // 删除知识库
    const delectKnow = () => {
      // if (tableData.value && tableData.value.length > 0) {
      //   ElMessage({
      //     type: "warning",
      //     message: "该知识库存在文件，无法删除",
      //   });
      //   return;
      // }
      typeOptions.value.forEach((item) => {
        if (item.zskMc == selectV.value) {
          window.API.wjbdHsZsk.delete(item.id).then((res) => {
            if (res.code == 200) {
              ElMessage({
                type: "success",
                message: "操作成功",
              });
              changeList();
              nextTick(() => {
                emit("deleteZsk");
              });
              if(tableData.value && tableData.value.length){
                let ids = tableData.value.map(v=>v.id).join(',')
                window.API.wjbdHsZskWj.delete({ids:ids}).then((res) => {
                    if (res.code == 200) {
                      // ElMessage({
                      //   type: "success",
                      //   message: "操作成功",
                      // });
                    }
                  });
              }
            }
          });
          
        }
      });
    };
//     // 点击删除表格数据
const delect = (row) => {
  window.API.wjbdHsZskWj.delete({ids:row.id}).then((res) => {
    if (res.code == 200) {
      ElMessage({ message: "删除成功", type: "success" });
      getKnowData();
    }
  });
};
//     // 点击下载按钮
const dowm = (item) => {
  const url =
    window.config.VUE_APP_API_BASE_URL + window.config.API_URL + "/sys/common/static/"+item.fwqWjlj;

  const link = document.createElement("a");
  link.style.display = "none";
  link.href = url;
  link.setAttribute("download", item.wjMc);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link); //下载完成移除元素
  window.URL.revokeObjectURL(url); //释放掉blob对象
};
//     // 文件上传前的方法
const before = (file) => {
  var arrList = file.name.split(".");
  const extension =
    arrList[arrList.length - 1] === "doc" ||
    arrList[arrList.length - 1] === "docx" ||
    arrList[arrList.length - 1] === "pdf" ||
    arrList[arrList.length - 1] === "txt";

  if (!extension) {
    ElMessage({
      type: "error",
      message: "上传文件只能是 .doc,.docx,.pdf或者.txt 格式!",
    });
  }
  return extension;
};
//     // 上传文件请求
const request = (param) => {
  const data = new FormData();
  var file = param.file;
  data.append("biz", "zsk");
  data.append("file", file);
  axios({
        url: "/wrj-api/sys/common/upload",
        method: "post",
        // responseType: "blob",
        data:data
  }).then(res=>{
    console.log(res);
    if(res.data.success){
      nextTick(() => {
        ElMessage({ message: "上传成功", type: "success" });
        fileListContent.value.push({
          fileName:getWjm(res.data.message),
          filePath:res.data.message
        });
      });
    }
  })
  // 调取上传接口
  // window.API.model.know.uploadZskFile(data).then((res) => {
  //   if (res.code == 200) {
  //     nextTick(() => {
  //       ElMessage({ message: "上传成功", type: "success" });
  //       fileListContent.value.push(res.data);
  //     });
  //   }
  // });
};
const getWjm = (e) =>{
  const originalFileName = e;
  const fileName = originalFileName.split('/').pop(); // 获取文件名部分
  const fileNameParts = fileName.split('_'); // 分割文件名中的下划线
  const fileName主体 = fileNameParts[0]; // 获取文件名的有效部分
  const extension = fileNameParts[1].split('.').pop(); // 获取文件扩展名
  const newFileName = extension ? `${fileName主体.split('.')[0]}.${extension}` : fileName主体;
  return newFileName
}
const addKnowledgeRef1 = ref(null)
// 点击编辑按钮
const edit = (row) => {
  addKnowledgeRef1.value.open(row);
};
//     // 文件删除
const fileRemove = (file) => {
  if (fileListContent.value.length) {
    fileListContent.value = fileListContent.value.filter((row) => {
      return row.fileName != file.fileName;
    });
  }
};
//     // 点击下载
const fileDownload = (itemData) => {
  //下载文档
  window.open(
    window.config.VUE_APP_API_BASE_URL + window.config.API_URL+"/sys/common/static/" + itemData.filePath
  );
};
//     // 点击确定向量化
const uploadRef = ref(null);
const loading = ref(false);
const xianglhuaVisible = () => {
  var zskId = "";
      var zskNm = "";
      typeOptions.value.forEach((item) => {
        if (item.zskMc == selectV.value) {
          zskId = item.id;
          zskNm = item.zskNm;
        }
      });
  var list = [];
  fileListContent.value.forEach((item) => {
    list.push({
      fwqWjlj: item.filePath,
      wjMc: item.fileName,
      zskId: zskId,
    });
  });
  nextTick(() => {
    loading.value = true;
    // 将成功上传的数据进行入库
    window.API.wjbdHsZskWj.add(list).then((res) => {
      if (res.code == 200) {
        loading.value = false;
        // 清除以及上传的数据，并且清除上传组件的数据
        fileListContent.value = [];
        uploadRef.value.clearFiles();
        getKnowData();
      }
    });
  });
};
const onClose = () => {
  visible.value = false;
};
//     // 选中的数据
const handleSelectionChange = (row) => {
  selectEdData.value = row;
};
//     // 新增和编辑知识库
const getListDataKnow = () => {
  changeList();
  nextTick(() => {
    emit("deleteZsk");
  });
};
defineExpose({ open });
</script>
<style lang="less" scoped>
.app-manage {
  width: 100%;
  height: 57.41vh;
  display: flex;
  flex-direction: column;
  .topMange {
    width: 100%;
    height: 45px;
    margin-top: 10px;
    display: flex;
    .create {
      width: 15%;
      height: 32px;
      background: rgba(0, 128, 255);
      color: #fff;
      display: flex;
      justify-content: center;
      align-items: center;
      border-radius: 5px;
      cursor: pointer;
    }
  }
  .upload-box {
    border: none !important;
    position: relative;
    background: transparent;
    min-height: 55px;
    max-height: 150px;
    display: block;
    border-radius: 0.28vh;
    overflow-y: auto;
    cursor: pointer;
    .file-upload {
      width: 100%;
      height: 100%;
      position: absolute;
      left: 0;
      top: 0;
      opacity: 0;
    }
    input[type="file"] {
      cursor: pointer;
    }
  }
  .titleList {
    width: 100%;
    display: flex;
    justify-content: space-between;
    padding-top: 5px;

    span {
      color: #fff;
      font-size: 18px;
      font-weight: bold;
    }
  }
  .whiteTheme {
    width: 100%;
    flex: 1;
    margin-top: 10px;
  }
}
</style>
<style scoped>
/* @import "@/style/dialog.css"; */
:deep .el-upload-dragger {
  background: transparent;
  padding: 10px !important;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none !important;
}
.el-upload__text,
.el-upload__tip {
  color: #fff;
}
.el-upload-list__item-actions > span {
  margin: 0 5px;
  cursor: pointer;
}
:deep .el-upload-list__item:hover {
  background: #7d714154;
}
:deep .el-upload-list {
  display: none;
}
:deep .el-upload,
.avatar {
  width: 100% !important;
  height: 100%;
}
:deep(.upload-demo) {
  border: 1px dashed #aaa !important;
}
::v-deep .my-dlg-body-content {
  padding: 0 10px !important;
}
::v-deep .el-select__wrapper {
  height: 30px !important;
}
</style>
