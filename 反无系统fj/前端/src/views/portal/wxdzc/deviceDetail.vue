<template>
  <AnalysisMoveDlg
    title="设备信息"
    :style="{ width: '90%', maxWidth: '1000px', height: '60vh' }"
    :visibleDialog="visibleDialog"
    @close="closed"
    isModal="true"
    :closeOnClickModal="false"
  >
    <div class="device-info-container">
      <el-form
        :model="form"
        :rules="rules"
        ref="dialogForm"
        label-width="150px"
        class="device-info-form"
      >
        <el-row :gutter="0" class="form-row">
          <!-- 设备名称 -->
          <el-col :span="12" class="form-col">
            <el-form-item label="设备名称：" prop="name" class="form-item">
              <div class="form-value">{{ form.name || '-' }}</div>
            </el-form-item>
          </el-col>
          
          <!-- 设备唯一标识 -->
          <el-col :span="12" class="form-col">
            <el-form-item label="设备唯一标识：" prop="deviceId" class="form-item">
              <div class="form-value">{{ form.deviceId || '-' }}</div>
            </el-form-item>
          </el-col>

          <!-- 站ID -->
          <el-col :span="12" class="form-col">
            <el-form-item label="站ID：" prop="stationId" class="form-item">
              <div class="form-value">{{ form.stationId || '-' }}</div>
            </el-form-item>
          </el-col>

          <!-- 设备类型 -->
          <el-col :span="12" class="form-col">
            <el-form-item label="设备类型：" prop="deviceType" class="form-item">
              <div class="form-value">
                {{ form.deviceType === 'DETECT' ? '侦测' : form.deviceType === 'TRAP' ? '诱骗' : form.deviceType || '干扰' || '-' }}
              </div>
            </el-form-item>
          </el-col>

          <!-- 设备IP地址 -->
          <el-col :span="12" class="form-col">
            <el-form-item label="设备IP地址：" prop="deviceIp" class="form-item">
              <div class="form-value">{{ form.deviceIp || '-' }}</div>
            </el-form-item>
          </el-col>

          <!-- 设备端口 -->
          <el-col :span="12" class="form-col">
            <el-form-item label="设备端口：" prop="currentLatitude" class="form-item">
              <div class="form-value">{{ form.devicePort || '-' }}</div>
            </el-form-item>
          </el-col>

          <!-- 有效性 -->
          <el-col :span="12" class="form-col">
            <el-form-item label="有效性：" prop="isValid" class="form-item">
              <div class="form-value">
                <span :class="form.isValid === 1 ? 'status-success' : 'status-danger'">
                  {{ form.isValid === 1 ? '正常' : form.isValid === 0 ? '禁用' : '-' }}
                </span>
              </div>
            </el-form-item>
          </el-col>

          <!-- 连接状态 -->
          <el-col :span="12" class="form-col">
            <el-form-item label="连接状态：" prop="status" class="form-item">
              <div class="form-value">
                <span :class="form.status === 'CONNECTED' ? 'status-success' : 'status-danger'">
                  {{ form.status === 'CONNECTED' ? '已连接' : '未连接' || '-' }}
                </span>
              </div>
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

          <!-- 侦测半径 -->
          <el-col :span="12" class="form-col">
            <el-form-item label="侦测半径：" prop="zcbj" class="form-item">
              <div class="form-value">{{ formatNumber(form.zcbj) }}</div>
            </el-form-item>
          </el-col>

          <!-- 弹药情况 -->
          <el-col :span="24" class="form-col">
            <el-form-item label="弹药情况：" prop="dyqk" class="form-item">
              <div class="form-value full-width-value">{{ form.dyqk || '-' }}</div>
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

// 弹窗显示状态
const visibleDialog = ref(false);
// 表单数据
const form = ref({});
// 表单校验规则
const rules = ref({});
// 表单引用
const dialogForm = ref({});

/**
 * 格式化数字显示
 * @param {number} num - 要格式化的数字
 * @param {number} precision - 保留小数位数，默认3位
 * @returns {string} 格式化后的数字或占位符
 */
const formatNumber = (num, precision = 3) => {
  if (num === undefined || num === null || num === '') return '-';
  return Number(num).toFixed(precision);
};

/**
 * 打开弹窗并传入设备数据
 * @param {Object} data - 设备信息数据
 */
const open = (data) => {
  console.log(data);
  visibleDialog.value = true;
  // 深拷贝避免修改原数据
  form.value = { ...data };
};

/**
 * 关闭弹窗
 */
const closed = () => {
  visibleDialog.value = false;
  // 清空表单数据
  form.value = {};
};

// 暴露方法供父组件调用
defineExpose({ open });

onMounted(() => {
  // 组件挂载后的逻辑
});
</script>

<style scoped>
/* 容器样式 */
.device-info-container {
  height: calc(100% - 60px);
  padding: 0;
  overflow-y: auto;
  box-sizing: border-box;
}

.device-info-form {
  width: 100%;
}

.form-row {
  margin: 0;
}

.form-col {
  margin: 0;
  padding: 0;
}

/* 表单项样式 */
.form-item {
  margin: 0 !important;
  display: flex;
  align-items: stretch;
  border-bottom: 1px solid #334155;
}

/* 标签样式（深蓝色表头） */
:deep(.el-form-item__label) {
  height:40px;
  background-color: #1e3a8a !important; /* 深蓝色表头 */
  color: #ffffff !important;
  font-weight: 500;
  line-height: 20px;
  border-right: 1px solid #334155;
  margin: 0;
  padding: 10px 12px;
}

/* 内容值样式 */
.form-value {
  flex: 1;
  color: #fff;
  padding: 10px 12px;
  line-height: 20px;
  font-size: 14px;
  word-break: break-all;
}

/* 弹药情况特殊样式 */
.full-width-value {
  min-height: 60px;
  white-space: pre-wrap;
}

/* 状态文字颜色 */
.status-success {
  color: #10b981; /* 成功绿色 */
}

.status-danger {
  color: #ef4444; /* 危险红色 */
}

.status-warning {
  color: #f59e0b; /* 警告黄色 */
}

/* 弹窗底部按钮区 */
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

.btn-cancel:hover {
  background-color: #2563eb;
  border-color: #2563eb;
}

/* 滚动条样式 */
.device-info-container::-webkit-scrollbar {
  width: 8px;
}
.device-info-container::-webkit-scrollbar-track {
  background: #1e293b;
}
.device-info-container::-webkit-scrollbar-thumb {
  background: #475569;
  border-radius: 4px;
}
.device-info-container::-webkit-scrollbar-thumb:hover {
  background: #64748b;
}

/* 响应式适配 */
@media (max-width: 768px) {
  :deep(.el-form-item__label) {
    width: 120px !important;
    font-size: 13px;
    padding: 8px 10px;
  }
  .form-value {
    font-size: 13px;
    padding: 8px 10px;
  }
}
</style>