<template>
  <AnalysisMoveDlg
    title="无人机信息"
    :style="{ width: '90%', maxWidth: '1000px', height: '68vh' }"
    :visibleDialog="visibleDialog"
    @close="closed"
    isModal="true"
    :closeOnClickModal="false"
  >
    <div class="drone-info-container">
      <el-form
        :model="form"
        :rules="rules"
        ref="dialogForm"
        label-width="150px"
        class="drone-info-form"
      >
        <el-row :gutter="0" class="form-row">
          <el-col :span="12" class="form-col">
            <el-form-item label="所属单位：" prop="ssdw" class="form-item" v-show="!flag">
              <div class="form-value">{{ form.ssdw || '-' }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12" class="form-col">
            <el-form-item label="任务类型：" prop="rwlx" class="form-item" v-show="!flag">
              <div class="form-value">{{ form.rwlx || '-' }}</div>
            </el-form-item>
          </el-col>

          <el-col :span="12" class="form-col">
            <el-form-item label="型号：" prop="model" class="form-item">
              <div class="form-value">{{ form.model || '-' }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12" class="form-col">
            <el-form-item label="序列号：" prop="serial" class="form-item">
              <div class="form-value">{{ form.serial || '-' }}</div>
            </el-form-item>
          </el-col>

          <el-col :span="12" class="form-col">
            <el-form-item label="经度：" prop="dronLng" class="form-item">
              <div class="form-value">{{ formatNumber(form.dronLng) }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12" class="form-col">
            <el-form-item label="纬度：" prop="dronLat" class="form-item">
              <div class="form-value">{{ formatNumber(form.dronLat) }}</div>
            </el-form-item>
          </el-col>

          <el-col :span="12" class="form-col">
            <el-form-item label="海拔高度(米)：" prop="altitude" class="form-item">
              <div class="form-value">{{ formatNumber(form.altitude) }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12" class="form-col">
            <el-form-item label="发现站点：" prop="stationName" class="form-item">
              <div class="form-value">{{ form.stationName || '-' }}</div>
            </el-form-item>
          </el-col>

          <el-col :span="12" class="form-col">
            <el-form-item label="起飞点经度：" prop="homeLng" class="form-item">
              <div class="form-value">{{ formatNumber(form.homeLng) }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12" class="form-col">
            <el-form-item label="起飞点纬度：" prop="homeLat" class="form-item">
              <div class="form-value">{{ formatNumber(form.homeLat) }}</div>
            </el-form-item>
          </el-col>

          <el-col :span="12" class="form-col">
            <el-form-item label="遥控器经度：" prop="pilotLng" class="form-item">
              <div class="form-value">{{ formatNumber(form.pilotLng) }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12" class="form-col">
            <el-form-item label="遥控器纬度：" prop="pilotLat" class="form-item">
              <div class="form-value">{{ formatNumber(form.pilotLat) }}</div>
            </el-form-item>
          </el-col>

          <el-col :span="12" class="form-col">
            <el-form-item label="高度：" prop="height" class="form-item">
              <div class="form-value">{{ formatNumber(form.height) }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12" class="form-col">
            <el-form-item label="东速度：" prop="eastV" class="form-item">
              <div class="form-value">{{ formatNumber(form.eastV) }}</div>
            </el-form-item>
          </el-col>

          <el-col :span="12" class="form-col">
            <el-form-item label="北速度：" prop="northV" class="form-item">
              <div class="form-value">{{ formatNumber(form.northV) }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12" class="form-col">
            <el-form-item label="上速度：" prop="upV" class="form-item">
              <div class="form-value">{{ formatNumber(form.upV) }}</div>
            </el-form-item>
          </el-col>

          <el-col :span="12" class="form-col">
            <el-form-item label="频率(U64)：" prop="freq" class="form-item">
              <div class="form-value">{{ formatNumber(form.freq) }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12" class="form-col">
            <el-form-item label="信号强度：" prop="rssi" class="form-item">
              <div class="form-value">{{ formatNumber(form.rssi) }}</div>
            </el-form-item>
          </el-col>

          <el-col :span="12" class="form-col">
            <el-form-item label="距离(Km)：" prop="distance" class="form-item">
              <div class="form-value">{{ formatNumber(form.distance, 3) }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12" class="form-col">
            <el-form-item label="飞手执照代码：" prop="uuid" class="form-item">
              <div class="form-value">{{ form.uuid || '-' }}</div>
            </el-form-item>
          </el-col>

          <el-col :span="12" class="form-col">
            <el-form-item label="飞机角度(°)：" prop="angle" class="form-item">
              <div class="form-value">{{ formatNumber(form.angle) }}</div>
            </el-form-item>
          </el-col>
          <el-col :span="12" class="form-col">
            <el-form-item label="时间戳(U64转换)：" prop="dataTime" class="form-item">
              <div class="form-value">{{ form.dataTime || '-' }}</div>
            </el-form-item>
          </el-col>

          <el-col :span="12" class="form-col">
            <el-form-item label="入库时间：" prop="createTime" class="form-item">
              <div class="form-value">{{ form.createTime || '-' }}</div>
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

const visibleDialog = ref(false);
const form = ref({});
const flag = ref(false);
const dialogForm = ref({});
const rules = ref({});

const formatNumber = (num, precision = 3) => {
  if (num === undefined || num === null || num === '') return '-';
  return Number(num).toFixed(precision);
};

const open = (data, data2) => {
  visibleDialog.value = true;
  form.value = { ...data };
  flag.value = !!data2;
};

const closed = () => {
  visibleDialog.value = false;
  form.value = {};
};

defineExpose({ open });

onMounted(() => {});
</script>

<style scoped>
.drone-info-container {
  height: calc(100% - 60px);
  padding: 0;
  overflow-y: auto;
  box-sizing: border-box;
  
}

.drone-info-form {
  width: 100%;
}

.form-row {
  margin: 0;
}

.form-col {
  margin: 0;
  padding: 0;
}

.form-item {
  margin: 0 !important;
  display: flex;
  align-items: stretch;
  border-bottom: 1px solid #334155;
}

/* 标签样式（深蓝色表头） */
:deep(.el-form-item__label) {
  /* width: 120px !important; */
  height:40px;
  background-color: #1e3a8a !important; /* 深蓝色表头 */
  color: #ffffff !important;
  font-weight: 500;
  padding: 10px 12px;
  line-height: 20px;
  border-right: 1px solid #334155;
  margin: 0;
}

/* 内容值样式（白色行） */
.form-value {
  flex: 1;
  color: #fff;
  padding: 10px 12px;
  line-height: 20px;
  font-size: 14px;
  word-break: break-all;
}

.dialog-footer {
  height: 60px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-top: 1px solid #334155;
  padding: 10px 0;
  margin: 0;
}

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
.drone-info-container::-webkit-scrollbar {
  width: 8px;
}
.drone-info-container::-webkit-scrollbar-track {
  background: #1e293b;
}
.drone-info-container::-webkit-scrollbar-thumb {
  background: #475569;
  border-radius: 4px;
}
.drone-info-container::-webkit-scrollbar-thumb:hover {
  background: #64748b;
}

/* 响应式适配 */
@media (max-width: 768px) {
  :deep(.el-form-item__label) {
    width: 100px !important;
    font-size: 13px;
    padding: 8px 10px;
  }
  .form-value {
    font-size: 13px;
    padding: 8px 10px;
  }
}
</style>