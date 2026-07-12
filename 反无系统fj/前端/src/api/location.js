import request from "@/utils/request.js";
/**
 * 调用模型接口
 * */
export function getBDInfo() {
  return request({
    url: `/bdInfo/list`,
    method: "get",
  });
}

/**
 * 生成行军报告
 * **/
export function createXjReport(data) {
  return request({
    url: `/bdInfo/createXjReport`,
    method: "post",
    params: data,
  });
}

/****
 * 获取页面的默认配块（非艾武场景）
 */
export function getModuleAppList(data) {
  return request({
    url: `/app/view/daily/moduleApp/list`,
    method: "get",
    params: data,
  });
}

/****
 * 调用后端转发的大模型 
 * 打开关闭配快场景
 * @message String
 */
export function getChatKnowledge(data) {
  return request({
    url: `/app/view/daily/chat/knowledge`,
    method: "get",
    params: data,
  });
}

/****
 * 调用后端转发的大模型
 * 分析生成配块场景
 * @chatMsg String
 */
export function getChatMsg(data) {
  return request({
    url: `/chat/chat`,
    method: "post",
    params: data,
  });
}

/**** 
 * 获取作战力量信息
 */
export function getZZLLInfo(data) {
  return request({
    url: `/app/view/daily/statistics/zzll/bdnm`,
    method: "post",
    data: data,
  });
}

/**** 
 * 获取重要目标信息 
 */
export function getZYMBInfo(data) {
  return request({
    url: `/app/view/daily/statistics/zdfwmb/name`,
    method: "get",
    params: data,
  });
}

/***
 * 获取装备物资
 */
export function getZBWZInfo(data) {
  return request({
    url: `/app/view/daily/statistics/wzcbk/bdnm`,
    method: "post",
    data: data,
  });
}

/***
 * 获取默认的作战力量
 */
export function getZZLLDefaultInfo(data) {
  return request({
    url: `/app/view/daily/statistics/zzll`,
    method: "get",
    params: data,
  });
}

/*** 
 * 获取默认的装备物资
 */
export function getZBWZDefaultInfo(data) {
  return request({
    url: `/app/view/daily/statistics/wzcbk`,
    method: "get",
    params: data,
  });
}

/*** 
 * 获取默认的重要目标
 */
export function getZYMBDefaultInfo(data) {
  return request({
    url: `/app/view/daily/statistics/zdfwmb`,
    method: "get",
    params: data,
  });
}

/*** 获取用户信息 */
export function getUserInfo() {
  return request({
    url: '/getInfo',
    method: 'get'
  })
}

/*** 
 * 获取当前部队信息
 * @BDNM 部队内码
 * */
export function getCrtBDAllInfo (BDNM) {
  return request({
    url: '/manager/bd/bdBsDm/' + BDNM,
    method: 'get'
  })
}

/***
 * 根据部队内码查询下级的所有的支队的人数
 * @BDNM 部队内码
 */
export function getCrtBDRy (BDNM) {
  return request({
    url: '/manager/bd/bdRy/' + BDNM,
    method: 'get'
  })
}

// 查询当前登录部队的任务列表
export function listZzrw(query) {
  return request({
      url: '/manager/zzrw/collection/bdnm',
      method: 'get',
      params: query
  })
}

/**** 单点登录校验 */
export function validateCasLogin(data) {
  return request({
    url: '/cas/login',
    method: 'post',
    timeout: 20000,
    params: data,
  })
}

/*** 退出登录 */
export function Logout() {
  return request({
    url: '/logout',
    method: 'post',
  })
} 

/****
 * 获取部队内码和信息
 */
export function getBDList() {
  return request({
    url: '/manager/bd/bdNode',
    method: 'get',
    timeout: 20000,
  })
}

/*** 登录方法 */
export function login(username, password, code, uuid, sysFlag, bdnm) {
  const data = {
    username,
    password,
    code,
    uuid,
    sysFlag,
    bdnm
  }
  return request({
    url: '/login',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post',
    data: data
  })
}

/***
 * 获取分析展示的模板 -- 模型
 */
export function getModuleServer(data) {
  return request({
    url: `/chat/chat/module/server`,
    method: "post",
    data: data,
  });
}

/***
 * 获取分析展示的模板 -- 大屏侧显示配块\
 * @rwid 任务id
 * @idList 已显示配块的id list
 */
export function getDynamicModule(data) {
  return request({
    url: `/app/dynamic/module`,
    method: "post",
    data: data,
  });
}

/****
 * 几何查询出来的部队信息更新到后端 -- 
 * @data [{bdnm:111111,bdfh:绵阳支队},{bdnm:111111,bdfh:绵阳支队}]
 */
export function updateChatbdInfo(data) {
  return request({
    url: `/chat/chat/bdInfo`,
    method: "post",
    data: data,
  });
}

/***
 * 下载大模型选择的行军路线规划
 */
export function updatePlan(data) {
  return request({
    url: `/chat/chat/path/planing`,
    method: "post",
    data: data,
  });
}

/***
 * 进入分析模式
 * @sffx 1 分析模式 2 平时模式
 */
export function updateFxms(data) {
  return request({
    url: `/chat/fxms`,
    method: "post",
    params: data,
  });
}

/*** 
 * 获取分析模式状态
 */
// export function getFxmsType() {
//   return request({
//     url: `/chat/fxms`,
//     method: "get",
//   });
// }

/***
 * 创建专题库
 * @mc String 名称
 * @ywlx ENUM (1任务、2案例、3演训)
 */
export function createZtkGc(data) {
  return request({
    url: `/chat/ztkGc`,
    method: "post",
    params: data,
  });
}

/***
 * 结束分析 (结束 任务| 案例 | 演训的分析)
 * 不需要参数
 */
export function ztkGcClose() {
  return request({
    url: `/chat/ztkGc/close`,
    method: "put",
  });
}

/*** 
 * 激活分析 。传选中的（ 任务| 案例 | 演训）id
 * @id （ 任务| 案例 | 演训）id
 */
export function ztkGcActive(id) {
  return request({
    url: `/chat/ztkGc/active`,
    method: "put",
    params: id
  });
}

/***
 * 更新灾害区域的行政区域
 * lng: 经度
 * lat: 纬度
 * smId: smId
 * name: 行政区域
 */
export function updateAreaInfo(data) {
  return request({
    url: `/chat/chat/areaInfo`,
    method: "post",
    data: data,
  });
}

/***
 * 上传重要目标数据
 */
export function uploadImportData(data) {
  return request({
    url: `/manager/zdfwmb/importData`,
    method: "post",
    data: data,
  });
}

/***
 * 上传案例知识库
 */
export function uploadKnowledgeData(data) {
  return request({
    url: `/chat/knowledgeBase/uploadDocs`,
    method: "post",
    data: data,
  });
}

/***
 * 固定配块 -- 灾情信息调用接口
 * 
 * 基本信息配块查询接口，以及任务详情查询接口，
查询基本信息时不用传参数，查询任务详情时  传id param形式。GET请求
 */
export function collectionInfo() {
  return request({
    url: `/manager/zzrw/info`,
    method: "get",
  });
}

/***
 * 战果战损
 */
export function zgzsStatistics() {
  return request({
    url: `/manager/zgzs/statistics`,
    method: "get",
  });
}

/***
 * 灾情动态
 */
export function zqdtCollection(params) {
  return request({
    url: `/manager/zqdt/collection/rwId`,
    method: "get",
    params: params
  });
}

/***
 * 社情分析
 */
export function sqCollection(params) {
  return request({
    url: `/manager/sq/collection/rwId`,
    method: "get",
    params: params
  });
}

/***
 * 获取兵力部署
 */
export function getChatBDList(params) {
  return request({
    url: `/chat/bd/list`,
    method: "get",
    params: params
  });
}

/***
 * 获取兵力部署
 * @id 
 */
export function sqfxDownload(params) {
  return request({
    url: `/manager/wjlj/download`,
    method: "get",
    params: params
  });
}

/***
 * 将知识图谱返回的数据保存到后端
 * @graph {graph:""}
 */
export function chatWord2Graph(data) {
  return request({
    url: `/chat/word2Graph`,
    method: "post",
    data: data
  });
}

/*** 配块上传 */
export function uploadTemplate(data) {
  return request({
    url: `/app/module/upload`,
    method: "post",
    data: data
  });
}

/**** 
 * 获取micro配块
 * @moduleCode=psmszhts
 *  */
export function getMicroTemplate(data) {
  return request({
    url: `/app/view/daily/moduleApp/list`,
    method: "get",
    params: data
  });
}

/*** 
 * 查询当前部队下属部队的 在位数、编制数、现有数、可出动数  -- tree 也在用
 * @sjbdnm 
 */
export function getZzllRyslList(data) {
  return request({
    url: `/app/zzll/list`,
    method: "get",
    params: data,
  });
}

/**** 上传文件 公共方法 */
export function uploadFiles(data) {
  return request({
    url: `/app/publics/wjlj/upload`,
    method: "post",
    data: data
  });
}

/*** 查询饼状图
 * @bdnmList
 */
export function getRyslSum(data) {
  return request({
    url: `/app/zzll/rysl/sum`,
    method: "post",
    data: data,
  });
}

/****
 * 查询 下辖部队
 * @sjbdnm
 * @pageNum
 * @pageSize
 */
export function queryZzllSubZzll(params) {
  return request({
    url: "/app/zzll/subZzll/sjbdnm",
    method: "get",
    params: params
  });
}

/****
 * 查询 部队基本信息
 * @bdnm
 */
export function queryZzllJbxx(params) {
  return request({
    url: "/app/zzll/jbxx",
    method: "get",
    params: params
  });
}

/**** 
 * 查询 关键岗位人员信息
 * @bdnm
 */
export function queryGjgwryXx(params) {
  return request({
    url: "/app/zzll/gjgwry",
    method: "get",
    params: params
  });
}

/*** 
 * 查询指挥员信息
 * @bdnm
 */
export function queryZhyXx(params) {
  return request({
    url: "/app/zzll/zhy",
    method: "get",
    params: params
  });
}

/*** 
 * 查询历史沿革
 * @bdnm
 */
export function queryLsyg(params) {
  return request({
    url: "/app/zzll/lsyg",
    method: "get",
    params: params
  });
}

/*** 
 * 查询媒体信息
 * @bdnm
 */
export function queryMtxx(params) {
  return request({
    url: "/app/zzll/mtxx",
    method: "get",
    params: params
  });
}

/*** 
 * 查询装备大的列表
 * @zbnmLength zbnmLength=4  固定值
 */
export function queryZblxlb(params) {
  return request({
    url: "/app/zzllZb/zblxlb",
    method: "get",
    params: params
  });
}

/*** 
 * 饼状图
 * @zbnmLength 10
 * @zblbnm WJ02
 * bdnm  912400000
 */
export function queryStatistic(params) {
  return request({
    url: "/app/zzllZb/zblx/statistic",
    method: "get",
    params: params
  });
}

/*** 
 * 列表
 * @zbnm
 */
export function queryListZblbnm(params) {
  return request({
    url: "/app/zzllZb/list/zblbnm",
    method: "get",
    params: params
  });
}

/***
 * 图标管理
 */
export function getSystemImg() {
  return request({
    url: `/wjjy/jggl/ydy/info`,
    method: "get",
  });
}
