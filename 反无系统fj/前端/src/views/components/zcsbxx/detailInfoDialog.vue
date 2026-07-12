<template>
  <el-dialog 
    :title="title" 
    v-model="visible" 
    destroy-on-close
    class="ct_dialog" 
    :style="{width: '90%', maxWidth: '1000px', height: '60vh'}"
    @close="closed"
    :modal="true"
    :close-on-click-modal="false"
  >
    <!-- 主要内容区域 -->
    <div class="detail-info-container">
      <el-form
        :model="detailInfoObj"
        label-width="120px"
        class="detail-info-form"
      >
        <el-row :gutter="0" class="form-row">
          <el-col 
            :span="item.span" 
            v-for="(item, index) in column" 
            :key="index" 
            v-show="item.label != '序号'"
            class="form-col"
          >
            <el-form-item 
              :label="item.label + '：'" 
              class="form-item"
            >
              <div class="form-value">
                <img 
                  v-if="item.label == '装备图册'" 
                  style="width: 160px;height: 100px;cursor:pointer;" 
                  :src="'/wrj-api/sys/common/static/'+detailInfoObj[item.prop]" 
                  @click="handlePictureCardPreview(detailInfoObj[item.prop])" 
                  alt="装备图册"
                >
                <span v-else-if="item.label == '有效性'">
                  {{detailInfoObj[item.prop]==1?'正常':'禁用'}}
                </span>
                <span v-else-if="item.label == '连接状态'">
                  {{detailInfoObj[item.prop]=='CONNECTED'?'已连接':detailInfoObj[item.prop]=='DISCONNECTED'?'未连接':'告警中'}}
                </span>
                <span v-else-if="item.label == '设备类型'">
                  {{detailInfoObj[item.prop]=='DETECT'?'侦测':detailInfoObj[item.prop]=='TRAP'?'诱骗':'干扰'}}
                </span>
                <span v-else>
                  {{ detailInfoObj[item.prop] || '-' }}
                </span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- 底部按钮区域 -->
    <div class="dialog-footer">
      <el-button size="default" class="btn-cancel" @click="closed">关闭</el-button>
    </div>
  </el-dialog>

  <!-- 图片预览弹窗 -->
  <el-dialog 
    title="预览" 
    :draggable="true" 
    style="width: 45%;height:60vh;" 
    v-model="dialogVisible"
    :modal="true"
    :close-on-click-modal="false"
  >
    <div
      style="
        height: 100%;
        display: flex;
        justify-content: space-evenly;
        align-items: center;
        padding-bottom: 20px;
      "
    >
      <img style="width: 100%;height:100%;" w-full :src="dialogImageUrl" alt="加载失败" />
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'detailInfoDialog',
  props: {
    title: {
      type: String,
      default: '详情信息'
    },
    // 是否显示
    visibleFlag: {
      type: Boolean,
      default: false
    },
    dataRow: {
      type: Object,
      default: ()=>{}
    },
    labelWidth: {
      type: Number,
      default: 150 // 统一为参考示例的150px
    },
    column: {
      type: Array,
      default: ()=>{return []}
    },
    bdnm: String
  },
  data() {
    return {
      visible: false,
      detailListData: {},
      detailInfoFlag: false,
      detailInfoObj: null,
      dialogVisible:false,
      dialogImageUrl:'',
    }
  },
  watch:{
    visibleFlag: {
      deep: true,
      immediate: true,
      handler(x){
        this.visible = x
      }
    }
  },
  methods: {
    open(row){
      if(row){
        this.detailInfoObj = row
      }
      this.visible = true
    },
    closed() {
      this.visible = false;
    },
    handlePictureCardPreview(file) {
      this.dialogImageUrl = '/wrj-api/sys/common/static/'+file;
      this.dialogVisible = true;
    },
  }
}
</script>

<style lang="less" scoped>
// 主容器样式
.detail-info-container {
  height: calc(100% - 60px);
  padding: 0;
  overflow-y: auto;
  box-sizing: border-box;
  
  // 滚动条样式
  &::-webkit-scrollbar {
    width: 8px;
  }
  &::-webkit-scrollbar-track {
    background: #1e293b;
  }
  &::-webkit-scrollbar-thumb {
    background: #475569;
    border-radius: 4px;
  }
  &::-webkit-scrollbar-thumb:hover {
    background: #64748b;
  }
}

// 表单样式
.detail-info-form {
  width: 100%;
}

.form-row {
  margin: 0;
}

.form-col {
  margin: 0;
  padding: 0;
}

// 表单项样式
.form-item {
  margin: 0 !important;
  display: flex;
  align-items: stretch;
  border-bottom: 1px solid #334155;
}

// 标签样式（深蓝色表头）
:deep(.el-form-item__label) {
  background-color: #1e3a8a !important; /* 深蓝色表头 */
  color: #ffffff !important;
  font-weight: 500;
  height: 40px;
  line-height: 20px;
  border-right: 1px solid #334155;
  margin: 0;
  padding: 10px 12px;
}

// 内容值样式
.form-value {
  flex: 1;
  color: #fff;
  padding: 10px 12px;
  line-height: 20px;
  font-size: 14px;
  word-break: break-all;
  
  // 图片样式适配
  img {
    display: block;
  }
}

// 底部按钮区域
.dialog-footer {
  height: 60px;
  display: flex;
  justify-content: center;
  align-items: center;
  border-top: 1px solid #334155;
  padding: 10px 0;
  margin: 0;
}

// 关闭按钮样式
.btn-cancel {
  width: 120px;
  height: 36px;
  font-size: 14px;
  background-color: #3b82f6;
  border-color: #3b82f6;
  color: #ffffff;
  
  &:hover {
    background-color: #2563eb;
    border-color: #2563eb;
  }
}

// 响应式适配
@media (max-width: 768px) {
  :deep(.el-form-item__label) {
    width: 100px !important;
    font-size: 13px;
    padding: 8px 10px !important;
  }
  .form-value {
    font-size: 13px;
    padding: 8px 10px;
  }
}
</style>