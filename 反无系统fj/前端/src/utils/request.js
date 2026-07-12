import axios from 'axios'
import tool from '@/utils/tool';
import {
  ElNotification,
  ElMessageBox
} from 'element-plus';
import router from '@/router';
import sysConfig from "@/config";
import Cookies from "js-cookie";

/**
 * @type {*|string}
 */
let apiBaseUrl = "";
// 创建 axios 实例
axios.defaults.timeout = 900000;
axios.defaults.baseURL = apiBaseUrl

var lastRefreshTime = false
// HTTP request 拦截器
axios.interceptors.request.use(
  (config) => {
    let token = tool.data.get("TOKEN")
    // console.log(sysConfig,token);
    config.headers[sysConfig.TOKEN_NAME] = sysConfig.TOKEN_PREFIX + (token === "" ? token : "c2FiZXI6c2FiZXJfc2VjcmV0");
    token && (config.headers[sysConfig.CLOUD_TOKEN_NAME] = token);
    Object.assign(config.headers, sysConfig.HEADERS)
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);
// HTTP response 拦截器
axios.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      return new Promise((resolve, reject) => {
        if (error.response.status === 404) {
          let reg = error.response.config.url.indexOf("api")
          let title = error.response.config.url.substr(reg + 3, error.response.config.url.length)

          reject(error.response);
        } else if (error.response.status === 500 && error.response.data.message.indexOf("Token失效")!=-1) {
          window.TOOL.data.remove("TOKEN");
          window.TOOL.data.clear();
          const sevice = "http://" + window.location.host + "/";
          const serviceUrl = encodeURIComponent(sevice)
          window.location.href = window.config.VUE_APP_CAS_BASE + '/login?service=' + serviceUrl
          
        }else if (error.response.status === 500) {
         
          let reg = error.response.config.url.indexOf("api")
          let title = error.response.config.url.substr(reg + 3, error.response.config.url.length)
          reject(error.response);
        } else if (error.response.status === 401) {
          console.log('error.response', error.response)
          if (error.response.data.msg === "接口未授权") {

           
          } else if (error.response.data.msg === "Token已过期") {
            if (tool.data.get("TOKEN")) {
              ElMessageBox.confirm('当前登录信息已经过期', '登录已过期', {
                type: 'error',
                closeOnClickModal: false,
                center: true,
                showCancelButton: false,
                confirmButtonText: '重新登录',
              }).then(() => {
                reject(error.response);
                router.replace({
                  path: '/login'
                });
              }).catch()
            }
            tool.data.remove("TOKEN");
          } else {
          }
          reject(error.response);
        } else {
       
          reject(error.response);
        }
      })
    } else {
      return Promise.reject(error.response);
    }
  }
);
var http = {

  /** get 请求
   * @param  {接口地址} url
   * @param  {请求参数} params
   * @param  {参数} config
   */
  get: function (url, params = {}, config = {}) {
    return new Promise((resolve, reject) => {
      axios({
        method: 'get',
        url: url,
        params: params,
        ...config
      }).then((response) => {
        resolve(response.data);
      }).catch((error) => {
        reject(error);
      })
    })
  },

  /** post 请求
   * @param  {string} url
   * @param  {请求参数} data
   * @param  {参数} config
   */
  post: function (url, data = {}, config = {}) {
    return new Promise((resolve, reject) => {
      axios({
        method: 'post',
        url: url,
        data: data,
        ...config
      }).then((response) => {
        resolve(response.data);
      }).catch((error) => {
        reject(error);
      })
    })
  },

  /** post 请求文件流, 返回原生respones对象
   * @param  {string} url
   * @param  {请求参数} data
   * @param  {参数} config
   */
  blobPost: function (url, data = {}, config = {}) {
    return new Promise((resolve, reject) => {
      axios({
        method: 'post',
        url: url,
        data: data,
        responseType: 'blob',
        ...config
      }).then((response) => {
        resolve(response);
      }).catch((error) => {
        reject(error);
      })
    })
  },

  /** put 请求
   * @param  {接口地址} url
   * @param  {请求参数} data
   * @param  {参数} config
   */
  put: function (url, data = {}, config = {}) {
    return new Promise((resolve, reject) => {
      axios({
        method: 'put',
        url: url,
        data: data,
        ...config
      }).then((response) => {
        resolve(response.data);
      }).catch((error) => {
        reject(error);
      })
    })
  },

  /** patch 请求
   * @param  {接口地址} url
   * @param  {请求参数} data
   * @param  {参数} config
   */
  patch: function (url, data = {}, config = {}) {
    return new Promise((resolve, reject) => {
      axios({
        method: 'patch',
        url: url,
        data: data,
        ...config
      }).then((response) => {
        resolve(response.data);
      }).catch((error) => {
        reject(error);
      })
    })
  },

  /** delete 请求
   * @param  {接口地址} url
   * @param  {请求参数} data
   * @param  {参数} config
   */
  delete: function (url, data = {}, config = {}) {
    return new Promise((resolve, reject) => {
      axios({
        method: 'delete',
        url: url,
        data: data,
        ...config
      }).then((response) => {
        resolve(response.data);
      }).catch((error) => {
        reject(error);
      })
    })
  },

  /** jsonp 请求
   * @param  {接口地址} url
   * @param  {JSONP回调函数名称} name
   */
  jsonp: function (url, name = 'jsonp') {
    return new Promise((resolve) => {
      var script = document.createElement('script')
      var _id = `jsonp${Math.ceil(Math.random() * 1000000)}`
      script.id = _id
      script.type = 'text/javascript'
      script.src = url
      window[name] = (response) => {
        resolve(response)
        document.getElementsByTagName('head')[0].removeChild(script)
        try {
          delete window[name];
        } catch (e) {
          window[name] = undefined;
        }
      }
      document.getElementsByTagName('head')[0].appendChild(script)
    })
  },
  /** post 请求文件流, 返回原生respones对象
   * @param  {string} url
   * @param  {请求参数} data
   * @param  {参数} config
   */
  getPost: function (url, params = {}, config = {}) {
    return new Promise((resolve, reject) => {
      axios({
        method: 'get',
        url: url,
        params: params,
        responseType: 'blob',
        ...config
      }).then((response) => {
        resolve(response);
      }).catch((error) => {
        reject(error);
      })
    })
  },
}

export default http;