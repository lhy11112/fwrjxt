<template>
  <div class="components-input-demo-presuffix">
    <!---->
    <el-input
      placeholder="请点击选择坐标点"
      v-model="pointValue"
      readOnly
      :disabled="disabled"
      clearable
      @change="handleOK"
    >
      <template #append>
        <!-- <i class="el-icon-location"></i> -->
        <el-icon style="cursor: pointer" @click="openModal"
          ><Location
        /></el-icon>
      </template>
    </el-input>

    <j-map-point-modal
      v-if="visible"
      ref="innerMapPointModal"
      :modal-width="modalWidth"
      :army="army"
      :pointValue="pointValue"
      @ok="handleOK"
      @handleCancel="handleCancel"
    />
  </div>
</template>

<script>
import JMapPointModal from "./JmapPointModal";
export default {
  name: "JmapPoint",
  components: {
    JMapPointModal,
  },
  props: {
    modalWidth: {
      type: Number,
      default: 500,
      required: false,
    },
    modelValue: {
      type: String,
      required: false,
    },
    disabled: {
      type: Boolean,
      required: false,
      default: false,
    },
    army: {
      type: Boolean,
      required: false,
      default: false,
    },
  },
  data() {
    return {
      visible: false,
      pointValue: "",
    };
  },
  mounted() {},
  watch: {
    modelValue: {
      deep: true,
      immediate: true,
      handler(val) {
        // console.log(val);
        if (val) {
          this.pointValue = val;
        } else {
          this.pointValue = "";
        }
      },
    },
  },
  methods: {
    openModal() {
      this.visible = true;
      this.$nextTick(() => {
        this.$refs.innerMapPointModal.show();
      });
    },
    handleOK(val, name) {
      this.pointValue = val;
      this.$emit("change", val, name);
      this.$emit("update:modelValue", val, name);
    },
    handleEmpty() {
      this.handleOK("");
    },
    handleCancel() {
      this.visible = false;
    },
  },
  model: {
    prop: "value",
    event: "change",
  },
};
</script>

<style scoped>
.components-input-demo-presuffix {
  width: 100%;
}
.components-input-demo-presuffix .anticon-close-circle {
  cursor: pointer;
  color: #ccc;
  transition: color 0.3s;
  font-size: 12px;
}
.components-input-demo-presuffix .anticon-close-circle:hover {
  color: #f5222d;
}
.components-input-demo-presuffix .anticon-close-circle:active {
  color: #666;
}
</style>
