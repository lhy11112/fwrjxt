import tool from '@/utils/tool';
import micro, { EventCenterForMicroApp } from "@micro-zoe/micro-app";
// import { useInfoDataStore } from "@/store/modules/user";
import Cookies from "js-cookie";
// const useUserConfig = useInfoDataStore();
// 生成随机id
const generateUUID = () => {
  if (false) {
  }
  var d = new Date().getTime();
  var uuid = "x-xx-4x-yx-xx".replace(
    /[xy]/g,
    function (c) {
      var r = (d + Math.random() * 16) % 16 | 0;
      d = Math.floor(d / 16);
      return (c === "x" ? r : (r & 0x7) | 0x8).toString(16);
    }
  );
  return uuid;
};
/*** 发送初始化的全局信息 */
const sendGlobalData = () => {
  if (Cookies.get("TOKEN")) {
    micro.setGlobalData(
      {
        token: Cookies.get("TOKEN"),
      }
    )
  }

};
export {
  generateUUID,
  sendGlobalData
}