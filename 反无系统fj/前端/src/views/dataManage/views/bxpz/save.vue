<template>
  <!-- <el-dialog
    :title="titleMap[mode]"
    v-model="visibleDialog"
    width="51%"
    style="height: 60%; margin-top: 10%"
    destroy-on-close
    close-on-click-modal
    @close="closed"
  > -->
  <AnalysisMoveDlg
    :title="titleMap[mode]"
    style="width: 60%;height:60vh"
    :visibleDialog="visibleDialog"
    @close="closed"
    isModal="true"
  >
    <el-form
      :model="form"
      :rules="rules"
      :disabled="mode === 'show'"
      ref="dialogForm"
      label-width="120px"
      style="height: calc(100% - 40px);overflow: auto;"
    >
      <el-row>


         <el-col :span="12">
          <el-form-item label="名称" prop="mc">
            <el-input v-model="form.mc" :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="下拉分类" prop="fl">
            <el-select v-model="form.fl" :placeholder="'请选择'" clearable @change="flChange">
              <el-option v-for="(item,index) in flOption" :key="index" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="父级目录" prop="pid">
            <el-cascader
              v-model="form.pid"
              style="width: 100%;"
              placeholder="请选择"
              :options="options"
              :props="{label: 'mc',value: 'id',checkStrictly: true}"
              filterable
              :show-all-levels="false"
              @change="pidChange"
            />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="内容" prop="nr">
            <scEditor v-model="form.nr" style="width: 100%; height: 400px;background-color:transparent;" />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input type="textarea" v-model="form.remark" autosize :placeholder="'请输入'" clearable>
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <!-- <template #footer> -->
      <div class="create-bottom">
      
        <el-button @click="closed">取 消</el-button>

        <el-button :loading="isSaveing" type="primary" @click="submit()"
          >保 存</el-button
        >
      </div>
    <!-- </template> -->
  <!-- </el-dialog> -->
  </AnalysisMoveDlg>
  <j-map-point-modal
    v-if="mapDialog"
    ref="innerMapPointModal"
    :modal-width="modalWidth"
    :army="false"
    :pointValue="pointValue"
    @ok="handleOK"
    @handleCancel="handleCancel"
  />
</template>

<script>
import scEditor from "@/components/scEditor/index.vue";
import JMapPointModal from "@/components/wMapPoint/JmapPointModals";
import { checkSpaceTss} from "@/utils/index.js"
export default {
  props:{
    toDp:{
      type:Boolean,
      default:false
    }
  },
  components:{
    JMapPointModal,
    scEditor

  },
  data() {
    return {
      mapDialog: false,
      visibleDialog: false,
      form: {
       
      },
      rules: {
        mc: [{ required: true,trigger: 'blur', message: "请输入名称" }],
        // pid: [{ required: true,trigger: 'blur', message: "请选择" }],
        nr: [{ required: true,trigger: 'blur', message: "请输入内容"  }],
      },
      flOption:[
        {
          label:'目录',
          value:'1'
        },
        {
          label:'文件',
          value:'2'
        },
      ],
      isValidOption:[
        {
          label:'正常',
          value:1
        },
        {
          label:'禁用',
          value:2
        },
      ],
      statusOption:[
        {
          label:'已连接',
          value:'CONNECTED'
        },
        {
          label:'未连接',
          value:'DISCONNECTED'
        },
        {
          label:'告警中',
          value:'WARN'
        },
      ],
      mode: "add",
      titleMap: {
        add: "新增",
        edit: "编辑",
        show: "查看",
      },
      rwData:[],
      mapDialog:false,
      options:[],
      treeData:[],
    };
  },
  created() {
    this.getTree()
  },
  methods: {
    flChange(e){
      if(e==1){
        this.options = [
            {
              id:'0',
              mc:'顶级',
              children:this.treeData
            }
          ];
      }else{
        this.options = this.treeData;
      }
    },
    getTree(){
      window.API.bxpz.listTree().then((res) => {
        if (res.code == 200) {
          this.treeData = res.result;
          this.options = [
            {
              id:'0',
              mc:'顶级',
              children:res.result
            }
          ];
        }
      });
    },
    pidChange(e){
      this.form.pid = e[e.length-1];
    },
    // 点击选择坐标点
    selectPoint() {
      this.mapDialog = true;
      this.$nextTick(() => {
        this.$refs.innerMapPointModal.show();
      });
    },
    handleOK(item1) {
      this.form.wd = this.ToDegrees(item1.split(",")[0]);
      this.form.jd = this.ToDegrees(item1.split(",")[1]);
      this.form.zb = this.ToDegrees(item1.split(",")[0]) + ',' +this.ToDegrees(item1.split(",")[1]);
    },
    // 经纬度转度分秒
      ToDegrees(val) {
        if (typeof val == 'undefined' || val == '') {
          return ''
        }
        // 把num类型转换成为string
        val = val + ''
        let i = val.indexOf('.')
        var strDu = i < 0 ? val : val.substring(0, i)
        let strFen = 0
        let strMiao = 0
        if (i > 0) {
          strFen = '0' + val.substring(i)
          strFen = Number(strFen) * 60 + ''
          i = strFen.indexOf('.')
          if (i > 0) {
            strMiao = '0' + strFen.substring(i)
            strFen = strFen.substring(0, i)
            strMiao = Number(strMiao ) * 60 + ''
            i = strMiao.indexOf('.')
            strMiao = strMiao.substring(0, i + 4)
            strMiao = parseFloat(strMiao).toFixed(2)
          }
        }
        return strDu + '°' + strFen + '′' + strMiao + '″'
      },
      /**
       * 度分秒转经纬度
       */
      DegreeConvertBack(value, len) {
        len = len > 6 || typeof len == 'undefined' ? 6 : len
        if (value == '') {
          return value
        }
        var du = parseFloat(value.split('°')[0])
        var fen = parseFloat(value.split('°')[1].split('′')[0]) / 60
        var miao = parseFloat(value.split('°')[1].split('′')[1].split('″')[0]) / 3600
        var digital = du + fen + miao
        return digital.toFixed(len)
      },
    open(item) {
      this.mode = item;
      this.visibleDialog = true;
    },
    closed(){
      this.visibleDialog = false;
      this.$emit('closed')
    },
    setData(data) {
      this.form = Object.assign(this.form, data);
      if(this.form.wd && this.form.jd){
        this.form.wd = this.ToDegrees(this.form.wd);
        this.form.jd = this.ToDegrees(this.form.jd);
        this.form.zb = this.ToDegrees(data.wd) + ',' + this.ToDegrees(data.jd)
       }
    },
    submit() {
      this.$refs.dialogForm.validate(async (valid) => {
        if (valid) {
          let data={}
          data=Object.assign(data,this.form)
          if(data.jd){
            data.jd=this.DegreeConvertBack(data.jd)
          }
          if(data.wd){
            data.wd=this.DegreeConvertBack(data.wd)
          }
          // data.mrcs=JSON.stringify(data.mrcs)
          if (this.mode == "add") {
            window.API.bxpz.add(data).then((res) => {
              if (res.code == 200) {
                this.visibleDialog = false;
                this.$message.success("操作成功")
                this.$emit("successClick");
              }
            });
            
          } else if (this.mode == "edit") {

            window.API.bxpz.edit(data).then((res) => {
              if (res.code == 200) {
                this.visibleDialog = false;
                this.$message.success("操作成功")
                this.$emit("successClick");
              }
            });
          }
        }
      });
    },
  },
};
</script>

<style scoped lang="less">
*{
  box-sizing: border-box;
}
// :deep(.el-form-item__label) {
//   color: #000 !important;
// }
.upload-demo {
  width: 100% !important;
}
.el-select-dropdown__item.is-selected{
  color:var(--el-color-primary) !important;
}
.create-bottom {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 10px;
}
:deep(.el-select){
  height:32px !important;
}
:deep(.el-select__wrapper){
  height:32px !important;
}
:deep(.el-input.is-disabled .el-input__wrapper){
  background-color: rgba(54, 108, 161, 0.1);
}
</style>