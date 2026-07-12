<template>
  <div class="sceditor">
    <!-- :plugins="plugins" -->
    <Editor
      style="height: 100%"
      v-model="contentValue"
      :init="init"
      :disabled="disabled"
      :placeholder="placeholder"
      @onClick="onClick"
    />
  </div>
</template>

<script>
import Editor from "@tinymce/tinymce-vue";
import tinymce from "tinymce/tinymce";
// import PlaceholderPlugin from "../../utils/PlaceholderPlugin";
import "tinymce/themes/silver";
import "tinymce/icons/default";

// 引入编辑器插件
import "tinymce/plugins/code"; //编辑源码
import "tinymce/plugins/image"; //插入编辑图片
import "tinymce/plugins/link"; //超链接
import "tinymce/plugins/preview"; //预览
import "tinymce/plugins/table"; //表格
// import "tinymce/plugins/paste"; // 粘贴 (此插件在当前版本中不存在)

export default {
  components: {
    Editor,
  },
  props: {
    modelValue: {
      type: String,
      default: "",
    },
    placeholder: {
      type: String,
      default: "",
    },
    height: {
      type: Number,
      default: 300,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    // pluginstext: {
    // 	type: String,
    // 	default: ""
    // },
    // pluginsFlag: {
    // 	type: Boolean,
    // 	default: false
    // },
    plugins: {
      type: [String, Array],
      default: "code image preview table paste",
    },
    toolbar: {
      type: [String, Array],
      default:
        "undo redo |  forecolor backcolor bold italic underline strikethrough | formatselect fontselect fontsizeselect | \
					alignleft aligncenter alignright alignjustify outdent indent lineheight | bullist numlist | \
					image table paste  preview | code selectall ",
    },
  },
  data() {
    return {
      init: {
        language_url: "tinymce/langs/zh_CN.js",
        language: "zh_CN",
        skin_url: "tinymce/skins/ui/oxide",
        content_css: "tinymce/skins/content/default/content.css",
        menubar: false,
        statusbar: true,
        plugins: this.plugins,
        toolbar: this.toolbar,
        fontsize_formats:
          "12px 14px 16px 18px 20px 22px 24px 28px 32px 36px 48px 56px 72px",
        height: this.height,
        placeholder: this.placeholder,
        branding: false,
        resize: true,
        paste_data_images: true,
        elementpath: true,
        content_style: "",
        images_upload_handler: async (blobInfo) => {
          const data = new FormData();
          data.append("file", blobInfo.blob());
        },
        setup: function (editor) {
          editor.on("init", function () {
            this.getBody().style.fontSize = "14px";
            this.getBody().style.color = "#fff";
          });
        },
      },
      contentValue: this.modelValue,
      plugins: "placeholder",
    };
  },
  watch: {
    modelValue(val) {
      this.contentValue = val;
    },
    contentValue(val) {
      this.$emit("update:modelValue", val);
    },
  },
  mounted() {
    tinymce.init({});
    // console.log(this.pluginsFlag);
    // if(this.pluginsFlag){
    //   console.log(11111111111);
    //   tinymce.PluginManager.add('placeholder',PlaceholderPlugin(this.pluginstext));//注册自定义插件
    // }
  },
  methods: {
    onClick(e) {
      this.$emit("onClick", e, tinymce);
    },
  },
};
</script>

<style lang="less" scoped>
body .tox-tinymce-aux {
  z-index: 5000 !important;
}
::v-deep .tox-tinymce {
  height: 100% !important;
}
::v-deep .tox .tox-toolbar__primary {
  background: transparent !important;
}
::v-deep .tox .tox-statusbar {
  background: transparent !important;
  color: #fff !important;
}
::v-deep .tox .tox-edit-area__iframe {
  background: transparent !important;
  color: #fff !important;
}
// 侧边滚轮
      ::v-deep .tox .tox-edit-area__iframe html::-webkit-scrollbar    //滚动条整体部分
      {
        width: 5px;
        height: 10px;
        background-color: #b5b1b1;
      }
      ::v-deep .tox .tox-edit-area__iframe html::-webkit-scrollbar-track       //scroll轨道背景
      {
        -webkit-box-shadow: inset 0 0 6px rgba(32, 16, 53, 0.664);
        box-shadow: inset 0 0 6px rgba(32, 16, 53, 0.664);
        border-radius: 10px;
        background-color: #21242b;
      }

      ::v-deep .tox .tox-edit-area__iframe html::-webkit-scrollbar-thumb   //滚动条中能上下移动的小块
      {
        border-radius: 10px;
        -webkit-box-shadow: inset 0 0 6px rgba(12, 147, 226, 0.596);
        box-shadow: inset 0 0 6px rgba(12, 147, 226, 0.596);
        background-color: #b5b1b1a6;
      }
::v-deep .tox .tox-toolbar-overlord {
  background: transparent !important;
  color: #fff !important;
  border-bottom: 1px solid #fff !important;
}
::v-deep .tox .tox-tbtn--disabled svg,
::v-deep .tox .tox-tbtn--disabled:hover svg,
::v-deep .tox .tox-tbtn:disabled svg,
::v-deep .tox .tox-tbtn:disabled:hover svg {
  fill: rgba(255, 255, 255) !important;
}
::v-deep .tox .tox-tbtn:hover svg {
  fill: rgba(255, 255, 255) !important;
}
::v-deep .tox .tox-tbtn svg {
  fill: rgba(255, 255, 255) !important;
}
::v-deep .tox .tox-tbtn {
  color: #fff;
}
::v-deep p {
  color: #fff !important;
}
</style>
