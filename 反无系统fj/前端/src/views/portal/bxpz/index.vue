<template>
  <div class="topCenter">
    <!-- <el-form>
      <el-col>
        <el-form-item>
          <scEditor v-model="form.nr" style="width: 100%; height: 435px;pointer-events: auto;background-color:#2d81d0;" />
        </el-form-item>
      </el-col>
    </el-form> -->
    <div class="page-tree">
      <div>
          <el-input
            v-model="treeMc"
            style="width: 98%; margin-bottom: 10px"
            @keyup.enter="search"
            :placeholder="'请输入'"
          />
        </div>
        <el-tree
          ref="treeRef"
          :data="treeData"
          node-key="id"
          :props="defaultProps"
          @node-click="handleNodeClick"
          :check-strictly="true"
          :highlight-current="true"
          clearable
        >
          <!-- default-expand-all -->
          <!--         :check-on-click-node="true" -->
        </el-tree>
    </div>
    <div class="pageContent-box">
      <scEditor v-model="form.nr" style="width: 100%; height:100%;pointer-events: auto;background-color:transparent;" />
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import microApp from "@/components/microApp/index.vue";
import { ref, onMounted,nextTick } from "vue";
import icon1 from "@/assets/leftTitle/leftImg.png"
import wxdzcsb from "@/assets/allImage/wxdzcsb.png"
import scEditor from "@/components/scEditor/index1.vue";
// 定义路由
const router = useRouter();
const wxdsbVisible = ref(false)
const queryInfo = ref({
  isValid:1,
  name:""
})
const wxdsbData = ref([])
const pageOption = ref({
  pageNo:1,
  pageSize:10
})
const total = ref(0)
const titles = ref([
  {title:'',key:'bxpz'}
])
const form = ref({})


const treeMc = ref("");
// 获取数据
const treeRef = ref(null);
// tree数据显示处理
const defaultProps = ref({
  children: "children",
  label: "mc",
  id: "id",
});
const treeData = ref([])
// 初始化
onMounted(()=>{
  getData()
})
const search = ()=>{
  getData()
}
const handleNodeClick = (data) => {
  form.value = data;
};
// 获取页面数据
const loading = ref(false);
const getData = () => {
  loading.value = true;
  window.API.bxpz.listTree({
    pageNo:1,
        pageSize:1000000,
        order: "descs",
        column: "cjsj",
        mc:treeMc.value
  }).then((res) => {
    if (res.code == 200) {
      loading.value = false;
      treeData.value = res.result;
      // nextTick(() => {
      //   if (treeData.value && treeData.value.length > 0) {
      //     nextTick(()=>{
      //         if(treeData.value[0].children && treeData.value[0].children.length){
      //           treeRef.value.setCurrentKey(treeData.value[0].children[0].id);
      //           handleNodeClick(treeData.value[0].children[0])
      //           // handleNodeClick(data.children[0],{
      //           //   id:data.children[0].id,
      //           //   label:data.children[0].mc,
      //           //   value:data.children[0].wjlj,
      //           //   level:2
      //           // })
      //         }
              
      //       })
          
      //   } else {
      //     form.value={}
      //   }
      // });
    }
  });
};
</script>

<style scoped lang="less">
// @import "@/style/dialog2.css";
.topCenter {
  width: 100vw;
  height: 80.4vh;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  pointer-events: none;
  .left{
    width: 120px;
    height:140px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 12px;
    color: #fff;
    justify-content: space-around;
    pointer-events: auto;
    .menus{
      width: 100%;
      margin-top: 20px;
      cursor: pointer;
      &>div{
        text-align: center;
      }
    }
  }

  .page-tree{
    width: 22%;
    height:100%;
    background: url("@/assets/allImage/dialogBg.png") no-repeat;
    background-size: 100% 100%;
    position: absolute;
    left: 30px;
    top:20px;
    padding: 10px;
    pointer-events: auto;
  }
  .pageContent-box{
     width:74%;
    height:100%;
    background: url("@/assets/allImage/dialogBg.png") no-repeat;
    background-size: 100% 100%;
    position: absolute;
    left: calc(22% + 48px);
    top:20px;
    padding: 10px;
    pointer-events: auto;
    color:#fff;
    overflow:hidden;
    overflow-y:auto;
  }


}
/* 分页样式 */
.el-pager li {
  background: transparent;
  border: 1px solid rgba(115, 116, 117);
  color: #fff;
  margin: 0 5px;
}

.el-pager li.is-active {
  background: rgba(255, 153, 12);
  border: 1px solid rgba(255, 153, 12);
  color: #fff;
}

.el-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 5px;
}
.el-pagination__total{
  color:#fff;
}
.el-pagination__jump{
  color:#fff;
}
.el-pagination button{
  background: transparent;
  border: 1px solid #fff;
}
.el-pagination .btn-next .el-icon, .el-pagination .btn-prev .el-icon{
  color:#fff;
}
.el-pagination button.is-disabled, .el-pagination button:disabled{
  background: transparent;
  border: 1px solid #fff;
}
.el-pagination button:hover,.el-pagination button:hover .el-icon{
  color:#409eff;
}
:deep(.el-tree-node__content){
  background:transparent;
}
:deep(.el-tree--highlight-current .el-tree-node.is-current>.el-tree-node__content){
  background-color: #44678f;
}
:deep(.el-tree){
  font-size:16px;
}
</style>