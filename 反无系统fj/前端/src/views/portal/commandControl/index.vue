<template>
  <div class="topCenter">
    
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import microApp from "@/components/microApp/index.vue";
import { ref, onMounted } from "vue";
import icon1 from "@/assets/leftTitle/leftImg.png"
import wxdzcsb from "@/assets/allImage/wxdzcsb.png"
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
// 初始化
onMounted(()=>{
  
})
const menuClick = (index) =>{
  
}
</script>

<style scoped lang="less">
.topCenter {
  width: 100vw;
  height: 76.4vh;
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
</style>