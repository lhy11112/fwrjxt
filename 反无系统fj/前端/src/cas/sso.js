// import Vue from 'vue';
import axios from "axios"
import config from "@/config"
import tool from "@/utils/tool"
import Cookies from "js-cookie";

const init = callback => {
  const token = tool.data.get("TOKEN"); // 获取缓存中token，用来判断是否需要重新调用接口获取
  const st = getUrlParam("ticket"); // 获取cas生成ticket
  const sevice = "http://" + window.location.host + "/"; // 获取访问地址
  if (!token && config.VUE_CAS_FLAG) { // 判断是否已经存在token
    if (!st) {
      const serviceUrl = encodeURIComponent(sevice)
      window.location.href = config.VUE_APP_CAS_BASE + '/login?service=' + serviceUrl

    } else {
      axios({
        url: config.VUE_APP_CAS_BASE_URL_MHWZ + '/jeecg-boot/sys/cas/client/getToken',
        method: 'post',
        params: {
          "ticket": st,
          "service": sevice,
          "prefixUrl": config.VUE_APP_CAS_BASE,
          "timeStamp": new Date().getTime() // 获取当前时间戳
        }
      }).then(res => {
        if (res.data.result) {
          axios({
            url: config.VUE_APP_CAS_BASE_URL_MHWZ + '/jeecg-boot/sys/cas/client/getUserPermission',
            method: 'post',
            headers: {
              'X-Access-Token': res.data.result.accessToken
            },
            params: {
              "token": res.data.result.accessToken,
              "timeStamp": new Date().getTime()
            }
          }).then(() => { // res4
            tool.data.set("SJBDCFLAG", 1)
          })

          axios({
            url: config.VUE_APP_CAS_BASE_URL_MHWZ + '/jeecg-boot/sys/cas/client/getUserInfo',  //getUserInfoOrPermission
            method: 'post',
            params: {
              "token": res.data.result.accessToken,
              "timeStamp": new Date().getTime()
            },
            headers: {
              "X-Access-Token": res.data.result.accessToken
            }
          }).then(res2 => {
            
            tool.data.set("TOKEN", res.data.result.accessToken);
            tool.data.set("USER_NAME", res2.data.result.userName);
            tool.data.set("USER_INFO", res2.data.result);
            tool.data.set("DEPARTS", res2.data.result);
            

            axios({
              // url: window.config.API_URL+'/dxyy/wjbdDxyyZtk/queryByBdNm',  ///bigScreenDataFx/bdwz
              url: window.config.API_URL+'/dxyy/wjbdWrjYhglCs/list',
              method: 'get',
              params: { 
                "token": res.data.result.accessToken,
                // "bdnm": res2.data.result.orgCode
                yhId: res2.data.result.id,
                csBm: "sys_user_info"
              }
            }).then(res3=>{
              // if(res3.data.success){
              //   // const obj={}
              //   // obj.jd=Number(res3.data.result.JD)
              //   // obj.wd=Number(res3.data.result.WD)
              //   // tool.data.set("BDWZ_INFO", obj);
              // }
              if(res3.data.success && res3.data.result.records.length){
                if(res3.data.result.records[0].csz) {
                  // var yhInfoObj = Object.assign(res2.data.result,JSON.parse(res3.data.result.records[0].csz));
                  var yhInfoObj = JSON.parse(res3.data.result.records[0].csz);
                  // res2.data.result.address = yhInfoObj.sfDmmc;
                  // res2.data.result.dmnm = yhInfoObj.sfDmnm;
                  // tool.data.set("BDWZ_INFO", yhInfoObj);
                  tool.data.set("PKTY_USER_INFO",yhInfoObj)
                  tool.data.set("PROVINCE", yhInfoObj.sfDmmc);
                  
                  
                  loginSuccess(callback);
                }else{
                  loginSuccess(callback);
                }
              }else{
                loginSuccess(callback);
              }
            })
            
            
          })
        } else {
          if (st) {
            loginSuccess(callback);
          } else {
            const serviceUrl = encodeURIComponent(sevice)
            window.location.href = config.VUE_APP_CAS_BASE + '/login?service=' + serviceUrl
          }
        }
      }).catch(() => {
        const serviceUrl = encodeURIComponent(sevice)
        window.location.href = config.VUE_APP_CAS_BASE + '/login?service=' + serviceUrl
      })
    }

  } else {

    axios({
      // url: window.config.API_URL+'/dxyy/wjbdDxyyZtk/queryByBdNm',  ///bigScreenDataFx/bdwz
      url: window.config.API_URL+'/dxyy/wjbdWrjYhglCs/list',
      method: 'get',
      params: {
        // "bdnm": res2.data.result.orgCode
        yhId: config.VUE_CAS_YHID,
        csBm: "sys_user_info"
      }
    }).then(res3=>{
      console.log('xxxxxxxx',res3);
      if(res3.data.success && res3.data.result.records.length){
        if(res3.data.result.records[0].csz) {
          // var yhInfoObj = Object.assign(res2.data.result,JSON.parse(res3.data.result.records[0].csz));
          var yhInfoObj = JSON.parse(res3.data.result.records[0].csz);
          // res2.data.result.address = yhInfoObj.sfDmmc;
          // res2.data.result.dmnm = yhInfoObj.sfDmnm;
          // tool.data.set("BDWZ_INFO", yhInfoObj);
          tool.data.set("PKTY_USER_INFO",yhInfoObj)
          tool.data.set("PROVINCE", yhInfoObj.sfDmmc);

          callback && callback()
        }else{
          callback && callback()
        }
      }else{
        callback && callback()
      }
    })
    
  }
}
const sso = {
  init: init
};

function getUrlParam(paraName) {
  const url = document.location.toString();
  const arrObj = url.split("?");

  if (arrObj.length > 1) {
    const arrPara = arrObj[1].split("&");
    let arr;

    for (let i = 0; i < arrPara.length; i++) {
      arr = arrPara[i].split("=");

      if (arr != null && arr[0] == paraName) {
        return arr[1];
      }
    }
    return "";
  }
  else {
    return "";
  }
}

function loginSuccess(callback) {
  callback();
}
export default sso;