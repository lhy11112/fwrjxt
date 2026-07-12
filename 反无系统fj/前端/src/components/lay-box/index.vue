<template>
  <div class="c-lay-box" :style="{ height: height + '%', width: width + '%' }">
    <!-- 配快关闭按钮 -->
    <template v-if="closePosition === 'none'"> </template>
    <template v-else-if="closePosition === 'left'">
      <img
        class="c-closeinfo-l"
        @click="showBox"
        src="/static/map_img/lleft.png"
      />
    </template>
    <template v-else>
      <img
        class="c-closeinfo-r"
        @click="showBox"
        src="/static/map_img/lleft.png"
      />
    </template>
    <div class="c-lay-box-item">
      <!-- <slot></slot> -->
      <div class="c-lay-hrow">
        <!-- title -->
        <span class="c-title-text">
          <slot name="title"></slot>
        </span>

        <!-- tools -->
        <slot name="tools"></slot>
      </div>
      <!-- default -->
      <div class="c-lay-box-content">
        <slot name="content"></slot>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  name: "LayBox",
  props: {
    height: {
      type: Number,
      require: false,
      default: 300,
    },
    // 关闭倒三角指向
    closePosition: {
      type: String,
      require: false,
      default: "left",
    },
    width: {
      type: Number,
      require: false,
      default: 100,
    },
  },
  methods: {
    // 控制当前配块的显示隐藏
    showBox(e) {
      const name = e.target.parentNode.className;
      console.log(e, "89789789797");
      if (name == "c-lay-box") {
        e.target.parentNode.classList.add("c-lay-box00");
        e.target.nextSibling.style.display = "none";
        e.target.src = "/static/map_img/right.png";
      } else {
        e.target.parentNode.classList.remove("c-lay-box00");
        setTimeout(() => {
          e.target.nextSibling.style.display = "block";
        }, 300);
        e.target.src = "/static/map_img/lleft.png";
      }
    },
  },
};
</script>
<style lang="less" scoped>
.c-lay-box {
  position: absolute;

  // z-index: 200;
  background-image: url(../../assets/images/laybox.png);
  // background-image: var(
  //   --el-color-primary-block-back,
  //   url(../../assets/firstPage/back.png)
  // );
  background-repeat: no-repeat;
  background-size: 100% 100%;
  transition: 1s;
  // background-color: rgb(30, 32, 44);
  .c-closeinfo-r {
    height: 75%;
    width: 10px;
    cursor: pointer;
    position: absolute;
    transform: rotate(180deg);
    top: 17%;
    left: -15px;
    z-index: 100;
    transition: 0.5s;
  }
  .c-closeinfo-l {
    height: 75%;
    width: 10px;
    cursor: pointer;
    position: absolute;
    top: 17%;
    // transform: translateY(-50%) rotate(180deg);
    right: -15px;
    z-index: 100;
    transition: 0.5s;
  }
  .c-lay-box-item {
    width: 97%;
    height: 100%;
    float: left;
    position: relative;
    top: 0;
    left: 1.5%;
    // z-index: 3;
    .c-lay-hrow {
      width: 100%;
      height: 13%;
      // line-height:15%;
      display: flex;
      align-items: center;
      margin-top: 0;
      position: relative;

      .c-title-text {
        color: #fff;
        // border-left: 3px solid #20d4ef;
        font-size: 16px;
        margin-left: 5px;
        // font-weight:bold;
      }
    }
    .c-lay-hrow::before {
      content: "";
      display: block;
      width: 20px;
      height: 20px;
      background-size: cover;
      position: absolute;
      left: 15px;
      top: 11px;
    }
    .c-lay-box-content {
      height: calc(100% - 13%);
      width: 100%;
      overflow: auto;
      -ms-overflow-style: none; /* Windows 和 Internet Explorer */
      scrollbar-width: none; /* 隐藏滚动条火狐浏览器 */
    }
    .c-lay-box-content::-webkit-scrollbar {
      display: none;
    }
  }
}

.c-lay-box00 {
  width: 0 !important;
  transition: all 1s;
}
</style>
