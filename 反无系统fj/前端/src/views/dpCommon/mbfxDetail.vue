<template>
  <AnalysisMoveDlg
    title="目标信息"
    :style="{ width: '70%', maxWidth: '800px', height: '50vh' }"
    :visibleDialog="visibleDialog"
    @close="closed"
    isModal="true"
    :closeOnClickModal="false"
  >
    <div class="target-info-container">
      <el-form
        :model="form"
        :rules="rules"
        ref="dialogForm"
        label-width="150px"
        class="target-info-form"
      >
        <el-row :gutter="0" class="form-row">
          <!-- 名称 -->
          <el-col :span="12" class="form-col">
            <el-form-item label="名称：" prop="mc" class="form-item" v-show="!flag">
              <div class="form-value">{{ form.mc || '-' }}</div>
            </el-form-item>
          </el-col>
          <!-- 经度 -->
          <el-col :span="12" class="form-col">
            <el-form-item label="经度：" prop="jd" class="form-item">
              <div class="form-value">{{ formatNumber(form.jd) }}</div>
            </el-form-item>
          </el-col>
          <!-- 纬度 -->
          <el-col :span="12" class="form-col">
            <el-form-item label="纬度：" prop="wd" class="form-item">
              <div class="form-value">{{ formatNumber(form.wd) }}</div>
            </el-form-item>
          </el-col>
          <!-- 人员类别 -->
          <el-col :span="12" class="form-col">
            <el-form-item label="人员类别：" prop="rylx" class="form-item">
              <div class="form-value">{{ form.rylx || '-' }}</div>
            </el-form-item>
          </el-col>
          <!-- 数量 -->
          <el-col :span="24" class="form-col">
            <el-form-item label="数量：" prop="sl" class="form-item">
              <div class="form-value">{{ form.sl }}</div>
            </el-form-item>
          </el-col>
          <!-- 装备 -->
          <el-col :span="24" class="form-col">
            <el-form-item label="装备：" prop="zb" class="form-item">
              <div class="form-value">{{ form.zb || '-' }}</div>
            </el-form-item>
          </el-col>
          <!-- 设施概括 -->
          <el-col :span="24" class="form-col">
            <el-form-item label="设施概括：" prop="ssgk" class="form-item">
              <div class="form-value">{{ form.ssgk || '-' }}</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <div class="dialog-footer">
      <el-button size="default" class="btn-cancel" @click="closed">关闭</el-button>
    </div>
  </AnalysisMoveDlg>
</template>

<script setup>
import { ref, onMounted, defineExpose } from "vue";

// 弹窗显隐控制
const visibleDialog = ref(false);
// 表单数据
const form = ref({});
// 控制部分字段显示隐藏的标识
const flag = ref(false);
// 表单引用
const dialogForm = ref({});
// 表单校验规则
const rules = ref({});

/**
 * 数字格式化函数
 * @param {number} num - 要格式化的数字
 * @param {number} precision - 保留小数位数，默认3位
 * @returns {string} 格式化后的数字或'-'
 */
const formatNumber = (num, precision = 3) => {
  if (num === undefined || num === null || num === '') return '-';
  // 数量字段特殊处理，保留0位小数
  return precision === 0 ? Math.round(Number(num)) : Number(num).toFixed(precision);
};

/**
 * 打开弹窗方法
 * @param {object} data - 目标信息数据
 * @param {any} data2 - 控制flag的参数，存在则隐藏指定字段
 */
const open = (data, data2) => {
  visibleDialog.value = true;
  // 深拷贝数据，避免修改原数据
  form.value = { ...data };
  // 根据data2是否存在设置flag值
  flag.value = !!data2;
};

/**
 * 关闭弹窗方法
 */
const closed = () => {
  visibleDialog.value = false;
  // 清空表单数据
  form.value = {};
};

// 暴露open方法给父组件调用
defineExpose({ open });

onMounted(() => {});
</script>

<style scoped>
/* 容器样式 - 滚动区域 */
.target-info-container {
  height: calc(100% - 60px);
  padding: 0;
  overflow-y: auto;
  box-sizing: border-box;
}

/* 表单样式 */
.target-info-form {
  width: 100%;
}

/* 行样式 */
.form-row {
  margin: 0;
}

/* 列样式 */
.form-col {
  margin: 0;
  padding: 0;
}

/* 表单项样式 - 下划线分隔 */
.form-item {
  margin: 0 !important;
  display: flex;
  align-items: stretch;
  border-bottom: 1px solid #334155;
}

/* 标签样式（深蓝色表头） */
:deep(.el-form-item__label) {
  background-color: #1e3a8a !important; /* 深蓝色表头背景 */
  color: #ffffff !important;           /* 白色文字 */
  font-weight: 500;                    /* 字体加粗 */
  padding: 10px 12px;                  /* 内边距 */
  line-height: 20px;                   /* 行高 */
  border-right: 1px solid #334155;     /* 右侧分隔线 */
  margin: 0;                           /* 清除默认边距 */
}

/* 内容值样式（白色文字） */
.form-value {
  flex: 1;                             /* 占满剩余空间 */
  color: #fff;                         /* 白色文字 */
  padding: 10px 12px;                  /* 内边距 */
  line-height: 20px;                   /* 行高 */
  font-size: 14px;                     /* 字体大小 */
  word-break: break-all;               /* 自动换行 */
}

/* 底部按钮区域 */
.dialog-footer {
  height: 60px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-top: 1px solid #334155;
  padding: 10px 0;
  margin: 0;
}

/* 关闭按钮样式 */
.btn-cancel {
  width: 120px;
  height: 36px;
  font-size: 14px;
  background-color: #3b82f6;
  border-color: #3b82f6;
  color: #ffffff;
}

/* 按钮hover效果 */
.btn-cancel:hover {
  background-color: #2563eb;
  border-color: #2563eb;
}

/* 滚动条样式 */
.target-info-container::-webkit-scrollbar {
  width: 8px;
}
.target-info-container::-webkit-scrollbar-track {
  background: #1e293b;
}
.target-info-container::-webkit-scrollbar-thumb {
  background: #475569;
  border-radius: 4px;
}
.target-info-container::-webkit-scrollbar-thumb:hover {
  background: #64748b;
}

/* 响应式适配 - 小屏幕样式调整 */
@media (max-width: 768px) {
  :deep(.el-form-item__label) {
    font-size: 13px;
    padding: 8px 10px;
  }
  .form-value {
    font-size: 13px;
    padding: 8px 10px;
  }
}
</style>