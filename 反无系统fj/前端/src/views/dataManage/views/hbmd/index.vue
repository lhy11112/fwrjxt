<template>
  <div style="width: 100%;height: 100%;">
    <el-transfer
      v-loading="loading"
      v-model="modelValue"
      :filter-method="filterMethod"
      :props="{
        key: 'id',
        label: 'name',
      }"
      :data="data"
      filterable
      :titles="['白名单', '黑名单']"
      :button-texts="['移入至白名单', '移入至黑名单']"
      :format="{
        noChecked: '${total}',
        hasChecked: '${checked}/${total}',
      }"
      filter-placeholder="输入检索"
      @change="handleChange"
    />
      <!-- <template #default="{ option }">
        <span>{{ option.brand }} - {{ option.serialNumber }}</span>
      </template> -->
    <!-- </el-transfer> -->
  </div>
</template>

<script>
import http from "@/utils/request.js"
import { ElNotification, ElMessageBox, ElMessage } from "element-plus";
import { h } from "vue";
export default {
  components: {
  },
  data() {
    return {
      headers: {
        'x-access-token': this.$TOOL.data.get("TOKEN"),
      },
      list: [],
      data: [],
      modelValue: [],
      queryInfo:{},
      loading:false,
    };
  },
  created() {
    this.getData()
  },
  methods: {
    getData(){
      this.loading=true;
      http.get("/wrj-api/wrj/wjbdWrjHbmdsq/hbmdList",{}).then(res=>{
        if(res.success){
          this.list = [];
          this.list = this.list.concat(res.result.白名单).concat(res.result.黑名单);
          this.data = this.list;
          this.modelValue = res.result.黑名单.map(item=>{return item.id});
        }
        this.loading=false;
      })
      // 无人机数据
      // window.API.wrj.list({
      //   pageNo: 1,
      //   pageSize: 99
      // }).then(res=>{
      //   if(res.code == 200){
      //     this.data = res.result.records;
      //     console.log(this.data);
      //   }
      //   this.loading=false;
      // })
    },
    handleChange(value, direction, movedKeys){
      // console.log(this.data);
      // console.log(value, direction, movedKeys);
      var arr = []
      for(var i of movedKeys){
        var obj = this.list.filter(row=>{return row.id == i})[0];
        arr.push(obj)
      }
      console.log(arr);
      http.put("/wrj-api/wrj/wjbdWrjHbmdsq/editSqxx",{
        ids: arr.map(item=>{return item.id}),
        wrjids: arr.map(item=>{return item.wrjid}),
        mdlx: direction=="left"?"白名单":"黑名单"
      }).then(res=>{
        if(res.success){
          ElMessage.success("操作成功")
          this.getData()
        }
      })
    }
  }
};
</script>

<style scoped lang="less">
:deep .el-transfer-panel .el-checkbox__label{
  color: #fff !important;
}
:deep .el-transfer-panel{
  width: calc((100% - 340px)/2);
  background: transparent;
}
:deep .el-transfer-panel__body{
  height: 65vh;
}
</style>
