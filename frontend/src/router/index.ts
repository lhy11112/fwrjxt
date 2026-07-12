import { createRouter, createWebHistory } from 'vue-router'

import DashboardView from '@/views/DashboardView.vue'
import MissionView from '@/views/MissionView.vue'
import DetectionView from '@/views/DetectionView.vue'
import DeviceManage from '@/views/dataManage/DeviceManage.vue'
import DroneLibrary from '@/views/dataManage/DroneLibrary.vue'

const routes = [
  {
    path: '/',
    name: 'dashboard',
    component: DashboardView,
    meta: { title: '态势总览' },
  },
  {
    path: '/detection',
    name: 'detection',
    component: DetectionView,
    meta: { title: '侦测预警' },
  },
  {
    path: '/wxdzc',
    name: 'wxdzc',
    component: () => import('@/views/WxdzcView.vue'),
    meta: { title: '侦测预警(新版)' },
  },
  {
    path: '/gjhf',
    name: 'gjhf',
    component: () => import('@/views/gjhf/GjhfPage.vue'),
    meta: { title: '告警回放' },
  },
  {
    path: '/command-control',
    name: 'commandControl',
    component: () => import('@/views/commandControl/CommandControlPage.vue'),
    meta: { title: '指挥控制' },
  },
  {
    path: '/zbxx',
    name: 'zbxx',
    component: () => import('@/views/zbxx/ZbxxPage.vue'),
    meta: { title: '装备信息' },
  },
  {
    path: '/navigation-deception',
    name: 'navigationDeception',
    component: () => import('@/views/navigationDeception/NavigationDeceptionPage.vue'),
    meta: { title: '导航诱骗' },
  },
  {
    path: '/signal-interference',
    name: 'signalInterference',
    component: () => import('@/views/signalInterference/SignalInterferencePage.vue'),
    meta: { title: '信号干扰' },
  },
  {
    path: '/simulated-exercise',
    name: 'simulatedExercise',
    component: () => import('@/views/simulatedExercise/SimulatedExercisePage.vue'),
    meta: { title: '模拟推演' },
  },
  {
    path: '/daily-topic',
    name: 'dailyTopic',
    component: () => import('@/views/dailyTopic/DailyTopicPage.vue'),
    meta: { title: '综合态势' },
  },
  {
    path: '/cesium-air',
    name: 'cesiumAir',
    component: () => import('@/views/cesiumAir/CesiumAirPage.vue'),
    meta: { title: '空域管理' },
  },
  {
    path: '/zymb',
    name: 'zymb',
    component: () => import('@/views/zymb/ZymbPage.vue'),
    meta: { title: '重要目标' },
  },
  {
    path: '/zhby',
    name: 'zhby',
    component: () => import('@/views/zhby/ZhbyPage.vue'),
    meta: { title: '综合兵要' },
  },
  {
    path: '/fwzf',
    name: 'fwzf',
    component: () => import('@/views/fwzf/FwzfPage.vue'),
    meta: { title: '反无战法' },
  },
  {
    path: '/bxpz',
    name: 'bxpz',
    component: () => import('@/views/bxpz/BxpzPage.vue'),
    meta: { title: '编携配装' },
  },
  {
    path: '/data-manage2',
    name: 'dataManage2',
    component: () => import('@/views/dataManage2/DataManagePage.vue'),
    meta: { title: '数据管理(新版)' },
  },
  {
    path: '/mission',
    name: 'mission',
    component: MissionView,
    meta: { title: '任务规划' },
  },
  {
    path: '/data-manage/devices',
    name: 'deviceManage',
    component: DeviceManage,
    meta: { title: '设备管理' },
  },
  {
    path: '/data-manage/drone-library',
    name: 'droneLibrary',
    component: DroneLibrary,
    meta: { title: '无人机特征库' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
