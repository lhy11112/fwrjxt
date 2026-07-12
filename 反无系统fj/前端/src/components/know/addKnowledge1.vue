<!-- 配快管理组件 -->
<template>
  <el-dialog
    title="编辑内容"
    style="width: 50%; pointer-events: auto"
    v-model="showDlg"
    @close="onClose"
    :isModal="true"
  >
    <div style="padding-top: 10px; height: 60vh" v-loading="loading">
      <el-table
        style="height: 95%; overflow: auto"
        :data="tableData"
        @selection-change="handleSelectionChange"
      >
        <template #empty>
          <el-empty description="暂无数据" image-size="120" />
        </template>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="nr" label="内容">
          <template #default="scope">
            <span v-if="!scope.row.flag">{{ scope.row.nr }}</span>
            <el-input
              v-else
              v-model="scope.row.nr"
              type="textarea"
              rows="6"
            ></el-input>
          </template>
        </el-table-column>

        <el-table-column width="80" label="操作" align="center">
          <template #default="scope">
            <el-button
              link
              v-if="!scope.row.flag"
              class="delectButton"
              @click="edit(scope.row)"
              >编辑</el-button
            >
            <el-button
              v-if="scope.row.flag"
              link
              class="delectButton"
              @click="save(scope.row)"
              >保存</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <div
        style="display: flex; align-items: center; justify-content: flex-end"
      >
        <span style="margin-right: 10px; color: #fff">共{{ total }}条</span>
        <div>
          <el-pagination
            :current-page="pageNo"
            :page-size="pageSize"
            layout=" prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
    <template #footer>
      <el-button type="info" @click="onClose">关闭</el-button>
    </template>
  </el-dialog>
</template>
<script setup>
import { ref, defineExpose } from "vue";
const loading = ref(false);
import { ElMessage } from "element-plus";
const showDlg = ref(false);
const pageNo = ref(1);
const pageSize = ref(5);
const total = ref(0);
const tableData = ref([]);
const zskWjId = ref("");
const open = (row) => {
  pageNo.value = 1;
  zskWjId.value = row.id;
  getTable();
  showDlg.value = true;
};
const save = (row) => {
  window.API.wjbdHsZskWjNr.edit(row).then((res) => {
    if (res.code == 200) {
      row.flag = false;
      ElMessage({
        type: "success",
        message: "操作成功",
      });
    }
  });
};
//条数切换
const handleSizeChange = (val) => {
  pageSize.value = val;
  getTable();
};
//页面
const handleCurrentChange = (val) => {
  pageNo.value = val;
  getTable();
};
// 点击编辑
const edit = (row) => {
  row.flag = true;
};
const getTable = () => {
  loading.value = true;
  window.API.wjbdHsZskWjNr
    .list({
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      zskWjId: zskWjId.value,
    })
    .then((res) => {
      if (res.code == 200) {
        loading.value = false;
        tableData.value = res.result.records;
        total.value = res.result.total;
      }
    });
};
// 关闭弹框
const onClose = () => {
  showDlg.value = false;
};
defineExpose({
  open,
});
</script>
<style lang="less" scoped>
.create-bottom {
  padding-bottom: 4vh;
  display: flex;
  justify-content: center;
  align-items: center;

  > div {
    width: 5.21vw;
    height: 3.43vh;
    padding: 0;
  }

  > div:nth-child(2) {
    margin-left: 0.94vw;
  }
}
</style>
