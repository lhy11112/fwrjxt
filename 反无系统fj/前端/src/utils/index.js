/**
 * 表格时间格式化
 * */
export function formatDate(cellValue) {
  if (cellValue == null || cellValue == "") {
    return "";
  }
  const date = new Date(cellValue);
  const year = date.getFullYear();
  const month =
    date.getMonth() + 1 < 10
      ? "0" + (date.getMonth() + 1)
      : date.getMonth() + 1;
      const day = date.getDate() < 10 ? "0" + date.getDate() : date.getDate();
      const hours = date.getHours() < 10 ? "0" + date.getHours() : date.getHours();
  const minutes =
    date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes();
    const seconds =
    date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds();
  return (
    year + "-" + month + "-" + day + " " + hours + ":" + minutes + ":" + seconds
  );
}

/**
 * @param {number} time
 * @param {string} option
 * @returns {string}
 */
// export function formatTime(time, option) {
//   if (("" + time).length === 10) {
//     time = parseInt(time) * 1000;
//   } else {
//     time = +time;
//   }
//   const d = new Date(time);
//   const now = Date.now();

//   // const diff = (now - d) / 1000; 原
//   const diff = Number(Number(Number(now) - Number(d)) / 1000);

//   if (diff < 30) {
//     return "刚刚";
//   } else if (diff < 3600) {
//     // scss 1 hour
//     return Math.ceil(diff / 60) + "分钟前";
//   } else if (diff < 3600 * 24) {
//     return Math.ceil(diff / 3600) + "小时前";
//   } else if (diff < 3600 * 24 * 2) {
//     return "1天前";
//   }
//   if (option) {
//     return parseTime(time, option);
//   } else {
//     return (
//       d.getMonth() +
//       1 +
//       "月" +
//       d.getDate() +
//       "日" +
//       d.getHours() +
//       "时" +
//       d.getMinutes() +
//       "分"
//     );
//   }
// }

/**
 * @param {string} url
 * @returns {Object}
 * */
export function getQueryObject(url) {
  url = url == null ? window.location.href : url;
  const search = url.substring(url.lastIndexOf("?") + 1);
  const obj = {};
  const reg = /([^?&=]+)=([^?&=]*)/g;
  search.replace(reg, (rs, $1, $2) => {
    const name = decodeURIComponent($1);
    let val = decodeURIComponent($2);
    val = String(val);
    obj[name] = val;
    return rs;
  });
  return obj;
}

/**
 * @param {string} input value
 * @returns {number} output value
 * */
// export function byteLength(str) {
//   // returns the byte length of an utf8 string
//   let s = str.length;
//   for (let i = str.length - 1; i >= 0; i--) {
//     const code = str.charCodeAt(i);
//     if (code > 0x7f && code <= 0x7ff) {
//       s++;
//     } else if (code > 0x7ff && code <= 0xffff) {
//       s += 2;
//     }
//     if (code >= 0xdc00 && code <= 0xdfff) {
//       i--;
//     }
//   }
//   return s;
// }

/**
 * @param {Array} actual
 * @returns {Array}
 * */
export function cleanArray(actual) {
  const newArray = [];
  for (let i = 0; i < actual.length; i++) {
    if (actual[i]) {
      newArray.push(actual[i]);
    }
  }
  return newArray;
}

/**
 * @param {Object} json
 * @returns {Array}
 * */
export function param(json) {
  if (!json) {
    return "";
  }
  return cleanArray(
    Object.keys(json).map((key) => {
      if (json[key] === undefined) {
        return "";
      }
      return encodeURIComponent(key) + "=" + encodeURIComponent(json[key]);
    })
  ).join("&");
}

/**
 * @param {string} url
 * @returns {Object}
 * */
export function param2Obj(url) {
  const search = decodeURIComponent(url.split("?")[1]).replace(/\+/g, " ");
  if (!search) {
    return {};
  }
  const obj = {};
  const searchArr = search.split("&");
  searchArr.forEach((v) => {
    const index = v.indexOf("=");
    if (index !== -1) {
      const name = v.substring(0, index);
      const val = v.substring(index + 1, v.length);
      obj[name] = val;
    }
  });
  return obj;
}

/**
 * @param {string} val
 * @returns {string}
 * */
export function html2Text(val) {
  const div = document.createElement("div");
  div.innerHTML = val;
  return div.textContent || div.innerText;
}

/**
 * @param {Object} target
 * @param {(Object|Array)} source
 * @returns {Object}
 * */
// export function objectMerge(target, source) {
//   if (typeof target != "object") {
//     target = {};
//   }
//   if (Array.isArray(source)) {
//     return source.slice();
//   }
//   Object.keys(source).forEach((property) => {
//     const sourceProperty = source[property];
//     if (typeof sourceProperty === "object") {
//       target[property] = objectMerge(target[property], sourceProperty);
//     } else {
//       target[property] = sourceProperty;
//     }
//   });
//   return target;
// }

/**
 * @param {HTMLElement} element
 * @param {string} className
 * */
export function toggleClass(element, className) {
  if (!element || !className) {
    return;
  }
  let classString = element.className;
  const nameIndex = classString.indexOf(className);
  if (nameIndex === -1) {
    classString += "" + className;
  } else {
    classString =
      classString.substr(0, nameIndex) +
      classString.substr(nameIndex + className.length);
  }
  element.className = classString;
}

/**
 * @param {string} type
 * @returns {Date}
 * */
// export function getTime(type) {
//   if (type === "start") {
//     return new Date().getTime() - 3600 * 1000 * 24 * 90;
//   } else {
//     return new Date(new Date().toDateString());
//   }
// }

/**
 * @param {Function} func
 * @param {number} wait
 * @param {boolean} immediate
 * @return {*}
 * */
export function debounce(func, wait, immediate) {
  let timeout, args, context, timestamp, result;

  const later = function () {
    // 据上一次触发时间间隔
    const last = +new Date() - timestamp;

    // 上次被包装函数被调用时间间隔 last 小于设定时间间隔 wait
    if (last < wait && last > 0) {
      timeout = setTimeout(later, wait - last);
    } else {
      timeout = null;
      // 如果设定为immediate===true，因为开始边界已经调用过了此处无需调用
      if (!immediate) {
        result = func.apply(context, args);
        if (!timeout) {
          context = args = null;
        }
      }
    }
  };

  return function (...args) {
    context = this;
    timestamp = +new Date();
    const callNow = immediate && !timeout;
    // 如果延时不存在，重新设定延时
    if (!timeout) timeout = setTimeout(later, wait);
    if (callNow) {
      result = func.apply(context, args);
      context = args = null;
    }

    return result;
  };
}

/**
 * @author wjg
 * @version Created by wjg on 2023/07/28 19:45
 * This is just a simple version of deep copy
 * Has a lot of edge cases bug
 * If you want to use a perfect deep copy, use lodash's _.cloneDeep
 * @param {Object} source
 * @returns {Object}
 * */
// export function deepClone(source) {
//   if (source && typeof source != "object") {
//     throw new Error("error arguments", "deepClone");
//   }
//   // const targetObj = source.constructor === Array ? [] : {}; // 原
//   const targetObj = source && source.constructor === Array ? [] : {};
//   Object.keys(source).forEach((keys) => {
//     if (source[keys] && typeof source[keys] === "object") {
//       targetObj[keys] = deepClone(source[keys]);
//     } else {
//       targetObj[keys] = source[keys];
//     }
//   });
//   return targetObj;
// }

/**
 * @author wjg
 * @version Created by wjg on 2023/07/28 19:45
 * @param {Array} arr
 * @returns {Array}
 * */
export function uniqueArr(arr) {
  return Array.from(new Set(arr));
}

/**
 * @author wjg
 * @version Created by wjg on 2023/07/28 19:45
 * @returns {string}
 * */
export function createUniqueString() {
  const timestamp = +new Date() + "";
  const randomNum = parseInt((1 + Math.random()) * 65536) + "";
  return (+(randomNum + timestamp)).toString(32);
}

/**
 * @author wjg
 * @version Created by wjg on 2023/07/28 19:45
 * Check if an element has a class
 * @param {HTMLElement} elm
 * @param {string} cls
 * @returns {boolean}
 * */
export function hasClass(ele, cls) {
  return !!ele.className.match(new RegExp("(\\s|^)" + cls + "(\\s|$)"));
}

/**
 * @author wjg
 * @version Created by wjg on 2023/07/28 19:45
 * Add class to element
 * @param {HTMLElement} elm
 * @param {string} cls
 * */
export function addClass(ele, cls) {
  if (!hasClass(ele, cls)) {
    ele.className += " " + cls;
  }
}

/**
 * @author wjg
 * @version Created by wjg on 2023/07/28 19:45
 * Remove class from element
 * @param {HTMLElement} elm
 * @param {string} cls
 * */
export function removeClass(ele, cls) {
  if (hasClass(ele, cls)) {
    const reg = new RegExp("(\\s|^)" + cls + "(\\s|$)");
    ele.className = ele.className.replace(reg, " ");
  }
}

export function makeMap(str, expectsLowerCase) {
  const map = Object.create(null);
  const list = str.split(",");
  for (let i = 0; i < list.length; i++) {
    map[list[i]] = true;
  }
  return expectsLowerCase ? (val) => map[val.toLowerCase()] : (val) => map[val];
}

export const exportDefault = "export default ";

export const beautifierConf = {
  html: {
    indent_size: "2",
    indent_char: " ",
    max_preserve_newlines: "-1",
    preserve_newlines: false,
    keep_array_indentation: false,
    break_chained_methods: false,
    indent_scripts: "separate",
    brace_style: "end-expand",
    space_before_conditional: true,
    unescape_strings: false,
    jslint_happy: false,
    end_with_newline: true,
    wrap_line_length: "110",
    indent_inner_html: true,
    comma_first: false,
    e4x: true,
    indent_empty_lines: true,
  },
  js: {
    indent_size: "2",
    indent_char: " ",
    max_preserve_newlines: "-1",
    preserve_newlines: false,
    keep_array_indentation: false,
    break_chained_methods: false,
    indent_scripts: "normal",
    brace_style: "end-expand",
    space_before_conditional: true,
    unescape_strings: false,
    jslint_happy: true,
    end_with_newline: true,
    wrap_line_length: "110",
    indent_inner_html: true,
    comma_first: false,
    e4x: true,
    indent_empty_lines: true,
  },
};

// 首字母大小
export function titleCase(str) {
  return str.replace(/( |^)[a-z]/g, (L) => L.toUpperCase());
}

// 下划转驼峰
export function camelCase(str) {
  return str.replace(/_[a-z]/g, (str1) => str1.substr(-1).toUpperCase());
}

export function isNumberStr(str) {
  return /^[+-]?(0|([1-9]\d*))(\.\d+)?$/g.test(str);
}

/**
 * @author wjg
 * @version Created by wjg on 2023/07/28 19:45
 * 通用js方法封装处理
 * Copyright (c) 2019 ruoyi
 * */

// 日期格式化
// export function parseTime(time, pattern) {
//   if (arguments.length === 0 || !time) {
//     return null;
//   }
//   const format = pattern || "{y}-{m}-{d} {h}:{i}:{s}";
//   let date;
//   if (typeof time === "object") {
//     date = time;
//   } else {
//     if (typeof time === "string" && /^[0-9]+$/.test(time)) {
//       time = parseInt(time);
//     } else if (typeof time === "string") {
//       time = time
//         .replace(new RegExp(/-/gm), "/")
//         .replace("T", " ")
//         .replace(new RegExp(/\.[\d]{3}/gm), "");
//     }
//     if (typeof time === "number" && time.toString().length === 10) {
//       time = time * 1000;
//     }
//     date = new Date(time);
//   }
//   const formatObj = {
//     y: date.getFullYear(),
//     m: date.getMonth() + 1,
//     d: date.getDate(),
//     h: date.getHours(),
//     i: date.getMinutes(),
//     s: date.getSeconds(),
//     a: date.getDay(),
//   };
//   const time_str = format.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key) => {
//     let value = formatObj[key];
//     // Note: getDay() returns 0 on Sunday
//     if (key === "a") {
//       return ["日", "一", "二", "三", "四", "五", "六"][value];
//     }
//     if (result.length > 0 && value < 10) {
//       value = "0" + value;
//     }
//     return value || 0;
//   });
//   return time_str;
// }

// 表单重置
export function resetForm(refName) {
  if (this.$refs[refName]) {
    this.$refs[refName].resetFields();
  }
}

// 添加日期范围
export function addDateRange(params, dateRange, propName) {
  const search = params;
  search.params =
    typeof search.params === "object" &&
    search.params !== null &&
    !Array.isArray(search.params)
      ? search.params
      : {};
  dateRange = Array.isArray(dateRange) ? dateRange : [];
  if (typeof propName === "undefined") {
    search.params["beginTime"] = dateRange[0];
    search.params["endTime"] = dateRange[1];
  } else {
    search.params["begin" + propName] = dateRange[0];
    search.params["end" + propName] = dateRange[1];
  }
  return search;
}

// 回显数据字典
export function selectDictLabel(datas, value) {
  if (value === undefined) {
    return "";
  }
  const actions = [];
  Object.keys(datas).some((key) => {
    if (datas[key].value == "" + value) {
      actions.push(datas[key].label);
      return true;
    }
  });
  if (actions.length === 0) {
    actions.push(value);
  }
  return actions.join("");
}

// 回显数据字典（字符串数组）
// export function selectDictLabels(datas, value, separator) {
//   if (value === undefined || value.length === 0) {
//     return "";
//   }
//   if (Array.isArray(value)) {
//     value = value.join(",");
//   }
//   const actions = [];
//   const currentSeparator = undefined === separator ? "," : separator;
//   const temp = value.split(currentSeparator);
//   Object.keys(value.split(currentSeparator)).some((val) => {
//     let match = false;
//     Object.keys(datas).some((key) => {
//       if (datas[key].value == "" + temp[val]) {
//         actions.push(datas[key].label + currentSeparator);
//         match = true;
//       }
//     });
//     if (!match) {
//       actions.push(temp[val] + currentSeparator);
//     }
//   });
//   return actions.join("").substring(0, actions.join("").length - 1);
// }

// 字符串格式化(%s )
export function sprintf(str) {
  const args = arguments;
  let i = 1;
  let flag = true;
  str = str.replace(/%s/g, function () {
    const arg = args[i++];
    if (typeof arg === "undefined") {
      flag = false;
      return "";
    }
    return arg;
  });
  return flag ? str : "";
}

// 转换字符串，undefined,null等转化为""
export function parseStrEmpty(str) {
  if (!str || str == "undefined" || str == "null") {
    return "";
  }
  return str;
}

// 数据合并
export function mergeRecursive(source, target) {
  for (const p in target) {
    try {
      if (target[p].constructor == Object) {
        source[p] = mergeRecursive(source[p], target[p]);
      } else {
        source[p] = target[p];
      }
    } catch (e) {
      source[p] = target[p];
    }
  }
  return source;
}

/**
 * @author wjg
 * @version Created by wjg on 2023/07/28 19:45
 * 构造树型结构数据
 * @param {*} data 数据源
 * @param {*} id id字段 默认 'id'
 * @param {*} parentId 父节点字段 默认 'parentId'
 * @param {*} children 孩子节点字段 默认 'children'
 * */
export function handleTree(data, id, parentId, children) {
  const config = {
    id: id || "id",
    parentId: parentId || "parentId",
    childrenList: children || "children",
  };

  const childrenListMap = {};
  const nodeIds = {};
  const tree = [];

  for (const d of data) {
    const parentId = d[config.parentId];
    if (childrenListMap[parentId] == null) {
      childrenListMap[parentId] = [];
    }
    nodeIds[d[config.id]] = d;
    childrenListMap[parentId].push(d);
  }

  for (const d of data) {
    const parentId = d[config.parentId];
    if (nodeIds[parentId] == null) {
      tree.push(d);
    }
  }

  for (const t of tree) {
    adaptToChildrenList(t);
  }

  function adaptToChildrenList(o) {
    if (childrenListMap[o[config.id]] !== null) {
      o[config.childrenList] = childrenListMap[o[config.id]];
    }
    if (o[config.childrenList]) {
      for (const c of o[config.childrenList]) {
        adaptToChildrenList(c);
      }
    }
  }
  return tree;
}

/**
 * @author wjg
 * @version Created by wjg on 2023/07/28 19:45
 * 参数处理
 * @param {*} params  参数
 * */
export function tansParams(params) {
  let result = "";
  for (const propName of Object.keys(params)) {
    const value = params[propName];
    const part = encodeURIComponent(propName) + "=";
    if (value !== null && value !== "" && typeof value !== "undefined") {
      if (typeof value === "object") {
        for (const key of Object.keys(value)) {
          if (
            value[key] !== null &&
            value[key] !== "" &&
            typeof value[key] !== "undefined"
          ) {
            const params = propName + "[" + key + "]";
            const subPart = encodeURIComponent(params) + "=";
            result += subPart + encodeURIComponent(value[key]) + "&";
          }
        }
      } else {
        result += part + encodeURIComponent(value) + "&";
      }
    }
  }
  return result;
}

/**
 * @author wjg
 * @version Created by wjg on 2023/07/28 19:45
 * 返回项目路径
 * */
export function getNormalPath(p) {
  if (p.length === 0 || !p || p == "undefined") {
    return p;
  }
  const res = p.replace("//", "/");
  if (res[res.length - 1] === "/") {
    return res.slice(0, res.length - 1);
  }
  return res;
}

// 验证是否为blob格式
export async function blobValidate(data) {
  try {
    const text = await data.text();
    JSON.parse(text);
    return false;
  } catch (error) {
    return true;
  }
}

/**
 * @author wjg
 * @version Created by wjg on 2023/07/28 19:45
 * 排序函数
 * */
export function arraySort(objArr, key) {
  const result = objArr.slice(0);
  return result.sort((a, b) => a[key] - b[key]);
}

export function setPageInfo(name) {
  const title = document.head.querySelector("title");
  if (title) {
    title.innerText = name;
  }
}

/*** 动态上传文件方法 */
// export function createInputFile(cb, multiple = true) {
//   var input = document.createElement('input');
//   input.type = "file";
//   input.multiple = true;
//   document.body.appendChild(input);
//   input.style.display = "none";
//   input.onchange = (e => {
//     cb(e);
//     document.body.removeChild(input);
//   });
//   input.click();
// }

/*** 动态上传文件方法 */
export function createInputFile(cb, multiple = true, accept="") {
  console.log(multiple);
  var input = document.createElement('input');
  input.type = "file";
  input.multiple = multiple;
  input.accept = accept;
  document.body.appendChild(input);
  input.style.display = "none";
  input.onchange = (e => {
    cb(e);
    document.body.removeChild(input);
  });
  input.click();
}

/**** 动态预览方法 */
export function publicReadFile(fileUrl) {
  window.open(window.SERVER_ADDRESS.PUBLIC_READ_FILE + '?url='+encodeURIComponent(window.Base64.encode(fileUrl)));
}

/*** 获取筛选弹窗应出现的位置 */
export function setSearchPosition (el, parentClass, searchDlgWidth, cb) {

  if (el.parentElement.classList.contains(parentClass)) {

    let cbObj = {};

    if ((document.body.clientWidth - el.parentElement.offsetLeft - el.parentElement.offsetWidth - 10) >= searchDlgWidth) {
      /*** 向右弹出 */
      cbObj = {
        defaultLeft: el.parentElement.offsetLeft + el.parentElement.offsetWidth + 10 + "px",
        defaultTop: el.parentElement.offsetTop + "px"
      }
    } else {
      /*** 向左弹出 */
      cbObj = {
        defaultLeft: el.parentElement.offsetLeft - searchDlgWidth - 10 + "px",
        defaultTop: el.parentElement.offsetTop + "px"
      }
    }
    cb(cbObj);
  } else {
    setSearchPosition(el.parentElement, "my-app-dlg", searchDlgWidth, cb);
  }
}

/**** 
 * 公共拷贝方法
 */
import { ElMessage } from "element-plus";
export function publicCopyText(text) {
  var textArea = document.createElement("textarea");
  textArea.value = text;
  document.body.appendChild(textArea);
  textArea.focus();
  textArea.select();
  try {
    var successful = document.execCommand('copy');
    var msg = successful ? 'successful' : 'unsuccessful';
    console.log('拷贝到剪贴板: ' + msg);
    ElMessage({ message: "复制成功", type: "success" });
  } catch (err) {
    console.error('Oops, unable to copy', err);
  }
  document.body.removeChild(textArea);
}

/*** 表单校验-- 判断是否为空字符 */
export const checkSpaceStr = (rule, value, callback) => {
  console.log(rule);
  if (!value) {
    return callback(new Error('请输入'))
  }

  if (/^\s*$/.test(value)) {
    callback(new Error('请输入合法字符'))
  } else {
    callback()
  }
}

/*** 表单校验-- 判断是否为空字符 */
export const checkSpaceNumLen = (rule, value, callback) => {
  console.log(rule);
  if (!value && value!=0) {
    return callback(new Error('请输入'))
  }
  const regex = /^[+-]?(\d+|\d+\.\d+)$/; 
  if (!regex.test(value)) {
    callback(new Error('请输入数字，不能包含其他字符'))
  } else if(value.length>8){
    callback(new Error('长度不能超过8个字符'))
  }else {
    callback()
  }
}
export const checkSpaceNumLens = (rule, value, callback) => {
  console.log(rule);
  if (!value && value!=0) {
    return callback(new Error('请输入'))
  }
  const regex = /^[+-]?(\d+|\d+\.\d+)$/; 
  if (!regex.test(value)) {
    callback(new Error('请输入数字，不能包含其他字符'))
  } else if(value.length>5){
    callback(new Error('长度不能超过5个字符'))
  }else {
    callback()
  }
}
/*** 表单校验-- 判断是否为空字符 */
export const checkSpaceNum = (rule, value, callback) => {
  // console.log(rule);
  if (!value&&value!=0) {
    return callback(new Error('请输入'))
  }
  const regex = /^[+-]?(\d+|\d+\.\d+)$/; 
  if (!regex.test(value)) {
    callback(new Error('请输入数字'))
  }else {
    callback()
  }
}

/*** 表单校验-- 判断正整数和0 */
export const checkSpaceZs = (rule, value, callback) => {
  // console.log(rule);
  if (!value&&value!=0) {
    return callback(new Error('请输入'))
  }
  const regex = /^[+]?\d+$/; 
  if (!regex.test(value)) {
    callback(new Error('请输入正整数或0'))
  }else if(value.length>8){
    callback(new Error('长度不能超过8个字符'))
  }else {
    callback()
  }
}


/*** 表单校验-- 判断不包含特殊字符 */
export const checkSpaceTs = (rule, value, callback) => {
  console.log(rule);
  if (!value) {
    return callback(new Error('请输入'))
  }
  const regex = /^[\u4e00-\u9fa5a-zA-Z0-9]{1,10}$/; 
  if (!regex.test(value)) {
    callback(new Error('不能包含特殊字符，且长度不能超过10位'))
  }else {
    callback()
  }
}

export const checkSpaceTss = (rule, value, callback) => {
  console.log(rule);
  if (!value) {
    return callback(new Error('请输入'))
  }
  const regex = /^[\u4e00-\u9fa5a-zA-Z0-9,·，]{1,20}$/; 
  if (!regex.test(value)) {
    callback(new Error('不能包含特殊字符，且长度不能超过20位'))
  }else {
    callback()
  }
}

// 自定义IP地址校验函数
export const validateIp = (rule, value, callback) => {
  console.log(rule);
  // 空值已由 required 校验，此处仅处理非空情况
  if (!value) return callback('请输入IP地址');
  
  // IP地址正则（支持 IPv4 标准格式）
  const ipReg = /^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)$/;
  if (ipReg.test(value)) {
    callback(); // 校验通过
  } else {
    callback(new Error('请输入有效的IPv4地址（如：192.168.1.1）')); // 校验失败
  }
};

// 自定义端口号校验函数
export const validatePort = (rule, value, callback) => {
  console.log(rule);
  if (!value) return callback('请输入端口');
  
  // 转换为数字（排除非数字字符）
  const port = Number(value);
  // 端口号范围：1-65535（0为保留端口，不建议使用）
  if (Number.isInteger(port) && port >= 1 && port <= 65535) {
    callback();
  } else {
    callback(new Error('请输入有效的端口号（1-65535之间的整数）'));
  }
};

