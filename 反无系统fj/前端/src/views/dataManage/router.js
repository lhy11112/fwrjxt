
import xtsz from "./views/xtsz/index.vue";
import grsb from "./views/grsb/index.vue";
import wrj from "./views/wrj/index.vue";
import wrjsjk from "./views/wrjsjk/index.vue";
import wrjfxsj from "./views/wrjfxsj/index.vue";
import hbmd from "./views/hbmd/index.vue";
import wrjgj from "./views/wrjgj/index.vue";
import czsp from "./views/czsp/index.vue";
import tcsb from "./views/tcsb/index.vue";
import czrz from "./views/czrz/index.vue";
import wrjky from "./views/wrjky/index.vue";
import wrjkysq from "./views/wrjkysq/index.vue";
import wrjtyjh from "./views/wrjtyjh/index.vue";
import wrjtyjhgj from "./views/wrjtyjhgj/index.vue";
import wrjppsj from "./views/wrjppsj/index.vue";
import sbgl from "./views/sbgl/index.vue";
import wbjk from "./views/wbjk/index.vue";
import lprjcs from "./views/lprjcs/index.vue";
import zbmb from "./views/zbmb/index.vue";
import dxdm from "./views/dxdm/index.vue";
// import fwzf from "./views/fwzf/index.vue";
import bxpz from "./views/bxpz/index.vue";
import fzzb from "./views/fzzb/index.vue";

// icon
import xtManageIcon from "./assets/menuImg/noSelect/系统管理.png"
import xtszIcon from "./assets/menuImg/noSelect/系统设置.png"
import sjztIcon from "./assets/menuImg/noSelect/数据中台.png"

// 选中
import actxtManageIcon from "./assets/menuImg/select/系统管理.png"
import actxtszIcon from "./assets/menuImg/select/系统设置.png"
import actsjztIcon from "./assets/menuImg/select/数据中台.png"


export default {
  menuList: [
    // {
    //   title: "向量管理",
    //   name: "xlManage",
    //   icon: actxlManageIcon,
    //   noicon: xlManageIcon,
    //   children: [
    //     {
    //       title: "向量类型",
    //       name: "xllx",
    //       icon: actxllxIcon,
    //       noicon: xllxIcon,
    //       is: xllx,
    //     },
    //     {
    //       title: "向量数据",
    //       name: "xlsj",
    //       icon: actxlsjIcon,
    //       noicon: xlsjIcon,
    //       is: xlsj,
    //     }
    //   ],
    // },
    // {
    //   title: "模块管理",
    //   name: "mkManage",
    //   icon: actmkManageIcon,
    //   noicon: mkManageIcon,
    //   children: [
    //     // {
    //     //   title: "业务模块",
    //     //   name: "ywmk",
    //     //   icon: actywmkIcon,
    //     //   noicon: ywmkIcon,
    //     //   is: ywmk,
    //     // },
    //     {
    //       title: "业务配块",
    //       name: "ywpk",
    //       icon: actywpkIcon,
    //       noicon: ywpkIcon,
    //       is: ywpk,
    //     },
    //   ],
    // },
    {
      title: "设备管理",
      name: "sbgl",
      icon: actxtManageIcon,
      noicon: actxtManageIcon,
      is:sbgl
      // children: [
      //   {
      //     title: "干扰设备",
      //     name: "grsb",
      //     icon: actsjztIcon,
      //     noicon: sjztIcon,
      //     is: grsb,
      //   },
      //   {
      //     title: "探测设备",
      //     name: "tcsb",
      //     icon: actsjztIcon,
      //     noicon: sjztIcon,
      //     is: tcsb,
      //   },
      // ],
    },
    {
      title: "无人机管理",
      name: "wrj",
      icon: actxtManageIcon,
      noicon: actxtManageIcon,
      is: wrj,
    },
    {
      title: "无人机数据库管理",
      name: "wrjsjk",
      icon: actxtManageIcon,
      noicon: actxtManageIcon,
      is: wrjsjk,
    },
    {
      title: "无人机飞行数据管理",
      name: "wrjfxsj",
      icon: actxtManageIcon,
      noicon: actxtManageIcon,
      is: wrjfxsj,
    },
    {
      title: "无人机频谱管理",
      name: "wrjppsj",
      icon: actxtManageIcon,
      noicon: actxtManageIcon,
      is: wrjppsj,
    },
    {
      title: "黑白名单管理",
      name: "hbmd",
      icon: actxtManageIcon,
      noicon: actxtManageIcon,
      is: hbmd,
    },
    {
      title: "无人机空域管理",
      name: "wrjky",
      icon: actxtManageIcon,
      noicon: actxtManageIcon,
      is: wrjky,
    },
    // {
    //   title: "无人机空域授权管理",
    //   name: "wrjkysq",
    //   icon: actxtManageIcon,
    //   noicon: actxtManageIcon,
    //   is: wrjkysq,
    // },
    {
      title: "无人机告警管理",
      name: "wrjgj",
      icon: actxtManageIcon,
      noicon: actxtManageIcon,
      is: wrjgj,
    },
    {
      title: "无人机推演计划管理",
      name: "wrjtyjh",
      icon: actxtManageIcon,
      noicon: actxtManageIcon,
      is: wrjtyjh,
    },
    {
      title: "无人机推演数据管理",
      name: "wrjtyjhgj",
      icon: actxtManageIcon,
      noicon: actxtManageIcon,
      is: wrjtyjhgj,
    },
    {
      title: "周边目标",
      name: "zbmb",
      icon: actxtManageIcon,
      noicon: actxtManageIcon,
      is: zbmb,
    },
    {
      title: "综合兵要",
      name: "dxdm",
      icon: actxtManageIcon,
      noicon: actxtManageIcon,
      is: dxdm,
    },
    // {
    //   title: "反无战法",
    //   name: "fwzf",
    //   icon: actxtManageIcon,
    //   noicon: actxtManageIcon,
    //   is: fwzf,
    // },
    {
      title: "编携配装",
      name: "bxpz",
      icon: actxtManageIcon,
      noicon: actxtManageIcon,
      is: bxpz,
    },
    {
      title: "装备信息",
      name: "fzzb",
      icon: actxtManageIcon,
      noicon: actxtManageIcon,
      is: fzzb,
    },
    {
      title: "操作视频管理",
      name: "czsp",
      icon: actxtManageIcon,
      noicon: actxtManageIcon,
      is: czsp,
    },
    {
      title: "录屏软件参数",
      name: "lprjcs",
      icon: actxtManageIcon,
      noicon: actxtManageIcon,
      is: lprjcs,
    },
    {
      title: "操作日志管理",
      name: "czrz",
      icon: actxtManageIcon,
      noicon: actxtManageIcon,
      is: czrz,
    },
    {
      title: "外部接口",
      name: "wbjk",
      icon: actxtManageIcon,
      noicon: actxtManageIcon,
      is: wbjk,
    },
    {
      title: "系统管理",
      name: "xtManage",
      icon: actxtszIcon,
      noicon: actxtszIcon,
      children: [
        {
          title: "系统设置",
          name: "xtsz",
          icon: actxtszIcon,
          noicon: actxtszIcon,
          is: xtsz,
        },
        // {
        //   title: "缓存数据",
        //   name: "hcsj",
        //   icon: acthcsjIcon,
        //   noicon: hcsjIcon,
        //   is: hcsj,
        // },
      ],
    },
    // {
    //   title: "指挥流程",
    //   name: "zhFlow",
    //   icon: actzhFlowIcon,
    //   noicon: zhFlowIcon,
    //   children: [
    //     // {
    //     //   title: "指挥流程分类",
    //     //   name: "zhlcfl",
    //     //   icon: actzhlcflIcon,
    //     //   noicon: zhlcflIcon,
    //     //   is: zhlcfl,
    //     // },
    //     {
    //       title: "指挥流程阶段",
    //       name: "zhlcjd",
    //       icon: actzhlcjdIcon,
    //       noicon: zhlcjdIcon,
    //       is: zhlcjd,
    //     }
    //   ],
    // },
    // {
    //   title: "知识库管理",
    //   name: "zskgl",
    //       icon: actzskglIcon,
    //       noicon: zskglIcon,
    //   children: [
    //     {
    //       title: "自建知识库",
    //       name: "zjzsk",
    //       icon: actzjzskIcon,
    //       noicon: zjzskIcon,
    //       is: zjzsk,
    //     },
    //     {
    //       title: "数据中台",
    //       name: "sjzt",
    //       icon: actsjztIcon,
    //       noicon: sjztIcon,
    //       is: sjzt,
    //     },
    //     {
    //       title: "大模型",
    //       name: "dmx",
    //       icon: actdmxIcon,
    //       noicon: dmxIcon,
    //       is: dmx,
    //     }
    //   ],
    // }
  ]
}