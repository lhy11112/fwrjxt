import micro from "@micro-zoe/micro-app";
// 给子应用传递参数
export const getChildData = () => {
  micro.setGlobalData({
    map2d: Map2D.map,
    token: window.TOOL.data.get("TOKEN"),
    // 用户信息
    userInfo:window.TOOL.data.get("PKTY_USER_INFO"),
     // 3D地图对象
    map3d:Map3D.map,
    updateType: window.TOOL.data.get("POSITION")?'zt':'logoutZt', //① zt： 进入专题  ②logoutZt 退出专题
  })
}