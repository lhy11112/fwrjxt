/*
 * @Version: v0.0.0.9
 * @Author: wjg
 * @Date: 2024-04-26 09:00:00
 */
import Cookies from "js-cookie";
import router from "@/router";
import { ElMessageBox } from "element-plus";
// import { isRelogin } from "@/utils/requestNew";

const TokenKey = "Admin-Token";
const uuidKey = "uuid";
const userIdKey = "userId";
const msgIdKey = "msgId";

export function getToken() {
  return Cookies.get(TokenKey);
}

export function setToken(token) {
  Cookies.set("TOKEN", token);
  Cookies.set(TokenKey, token);
  eventBus.emit("globalDataAlready");
  return ;
}

export function removeToken() {
  Cookies.remove("TOKEN", )
  return Cookies.remove(TokenKey);
}

export function getUUID() {
  return Cookies.get(uuidKey);
}

export function setUUID(uuid) {
  return Cookies.set(uuidKey, uuid);
}

export function removeUUID() {
  return Cookies.remove(uuidKey);
}

export function getUserId() {
  return Cookies.get(userIdKey);
}

export function setUserId(userid) {
  return Cookies.set(userIdKey, userid);
}

export function removeUserId() {
  return Cookies.remove(userIdKey);
}

export function getCookieMsgId() {
  return Cookies.get(msgId);
}

export function setCookieId(msgId) {
  return Cookies.set(msgIdKey, msgId);
}

export function removeCookieMsgId() {
  return Cookies.remove(msgIdKey);
}

export function setUserMsg (userMsg) {
  return Cookies.set("userMsg", userMsg);
}

export function getUserMsg () {
  return Cookies.get("userMsg");
}

export function removeUserMsg() {
  return Cookies.remove("userMsg");
}

/**
 * @author wjg
 * @version Created by wjg on 2023/07/28 19:45
 * 显示登录弹窗
 * */
export function showLoginMessage() {
  return new Promise((resolve, reject) => {
    return ElMessageBox.confirm(
      "登录状态已过期，您可以继续留在该页面，或者重新登录",
      "系统提示",
      {
        confirmButtonText: "重新登录",
        customClass: "theme-light",
        type: "warning",
        callback: function () {
          /*          router.push({
            path: "/login?redirect=" + router.currentRoute.value.fullPath
          })*/
          router.push({
            name: "login",
            params: {
              fromType: "errorReport",
            },
          });
          resolve("login");
        },
      }
    )
      .then(() => {
        // 无逻辑可执行
      })
      .catch(() => {
        reject("err");
      });
  });
}
