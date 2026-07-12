import { createRouter, createWebHistory } from 'vue-router'
const router = createRouter({
  history: createWebHistory(),
  routes: [
    // {
    //   path: "/",
    //   name: "layout",
    //   component: () => import('@/views/main/index.vue'),
    //   redirect: '/portal',
    //   children:[
    //     // 日常专题门户
    //     {
    //       path: "/portal",
    //       name: "portal",
    //       component: () => import('@/views/portal/dailyTopic/index.vue'),

    //     },
    //     // 演习演训专题门户
    //     {
    //       path: "/trainingSpecialTopic",
    //       name: "trainingSpecialTopic",
    //       component: () => import('@/views/portal/trainingSpecialTopic/index.vue'),

    //     },
    //     {
    //       path: '/redirected',
    //       redirect: '/portal'
    //     },
    //   ]
    // },

    
    
    {
      path: "/dataManage",
      name: "dataManage",
      component: () => import('@/views/dataManage/index.vue'),
    },
    {
      path: "/cs",
      name: "cs",
      component: () => import('@/views/cs/index.vue'),
    },
    {
      path: "/cs2",
      name: "cs2",
      component: () => import('@/views/cs/index2.vue'),
    },
    {
      path: "/cs3",
      name: "cs3",
      component: () => import('@/views/cs/index3.vue'),
    },
    {
      path:"/",
      name: "layout",
      component: () => import('@/views/dpCommon/index.vue'),
      redirect: '/portal',
      children:[
        // 综合态势
        {
          path: "/portal",
          name: "portal",
          component: () => import('@/views/portal/dailyTopic/index.vue'),

        },
        // 侦测预警
        {
          path: "/wxdzc",
          name: "wxdzc",
          component: () => import('@/views/portal/wxdzc/index.vue'),

        },
        // 指挥控制
        {
          path: "/commandControl",
          name: "commandControl",
          component: () => import('@/views/portal/commandControl/index.vue'),

        },
        // 导航诱骗
        {
          path: "/navigationDeception",
          name: "navigationDeception",
          component: () => import('@/views/portal/navigationDeception/index.vue'),

        },
        // 信号干扰
        {
          path: "/signalInterference",
          name: "signalInterference",
          component: () => import('@/views/portal/signalInterference/index.vue'),

        },
        // 模拟推演
        {
          path: "/simulatedExercise",
          name: "simulatedExercise",
          component: () => import('@/views/portal/simulatedExercise/index.vue'),
        },
        //空域
        {
          path: "/cesiumAir",
          name: "cesiumAir",
          component: () => import('@/views/portal/cesiumAir/index.vue'),
        },
        //重要目标
        {
          path: "/zymb",
          name: "zymb",
          component: () => import('@/views/portal/zymb/index.vue'),
        },
        //综合兵要
        {
          path: "/zhby",
          name: "zhby",
          component: () => import('@/views/portal/zhby/index.vue'),
        },
        //告警回放
        {
          path: "/gjhf",
          name: "gjhf",
          component: () => import('@/views/portal/gjhf/index.vue'),
        },
        //装备信息
        {
          path: "/zbxx",
          name: "zbxx",
          component: () => import('@/views/portal/zbxx/index.vue'),
        },
        //反无战法
        {
          path: "/fwzf",
          name: "fwzf",
          component: () => import('@/views/portal/fwzf/index.vue'),
        },
        //编携配装
        {
          path: "/bxpz",
          name: "bxpz",
          component: () => import('@/views/portal/bxpz/index.vue'),
        },
        {
          path: "/pkPage",
          name: "pkPage",
          component: () => import('@/views/components/pkPage/index.vue'),
        },

        
      ]
    },
  ]
})
export default router