
import request from "@/utils/localRequest.js";

/**
 * 向量类型新增
 * */
export function ggxlkLxAdd(data) {
  return request({
    url: `/analysis/ggxlkLx`,
    method: "post",
    data: data,
  });
}

/**
 * 向量类型Bm生成
 * */
export function createBm(data) {
  return request({
    url: `/analysis/ggxlkLx/createBm`,
    method: "get",
    params: data,
  });
}

/**
 * 向量类型Bm重复
 * */
export function ruleBm(data) {
  return request({
    url: `/analysis/ggxlkLx/bm`,
    method: "get",
    params: data,
  });
}

/**
 * 向量类型修改
 * */
export function ggxlkLxEdit(data) {
  return request({
    url: `/analysis/ggxlkLx`,
    method: "put",
    data: data,
  });
}

/**
 * 向量类型列表
 * */
export function ggxlkLxList(data) {
  return request({
    url: `/analysis/ggxlkLx/list`,
    method: "get",
    params: data,
  });
}

/**
 * 向量类型删除
 * */
export function ggxlkLxDel(data) {
  return request({
    url: `/analysis/ggxlkLx`,
    method: "delete",
    data: data,
  });
}

/**
 * 判断向量类型删除
 * */
export function ggxlkLxisDel(data) {
  return request({
    url: `/analysis/ggxlkLx/check/data`,
    method: "post",
    data: data,
  });
}

/**
 * 向量库名称
 * */
export function ggxlkLxXlkMc(data = {}) {
  return request({
    url: `/analysis/ggxlkLx/xlkMc`,
    method: "get",
    params: data,
  });
}


// 向量数据部分------------------------------------------------------------------

/**
 * 向量数据Bm生成
 * */
export function ggxlkCreateBm(data) {
  return request({
    url: `/analysis/ggxlk/createBm`,
    method: "get",
    params: data,
  });
}

/**
 * 向量数据Bm重复
 * */
export function ruleYwlxBm(data) {
  return request({
    url: `/analysis/ggxlkLx/ywlxBm`,
    method: "get",
    params: data,
  });
}

/**
 * 向量数据新增
 * */
export function ggxlkAdd(data) {
  return request({
    url: `/analysis/ggxlk`,
    method: "post",
    data: data,
  });
}

/**
 * 向量数据编辑
 * */
export function ggxlkEdit(data) {
  return request({
    url: `/analysis/ggxlk`,
    method: "put",
    data: data,
  });
}



/**
 * 向量数据列表
 * */
export function ggxlkList(data) {
  return request({
    url: `/analysis/ggxlk/list`,
    method: "get",
    params: data,
  });
}

/**
 * 向量数据删除
 * */
export function ggxlkDel(data) {
  return request({
    url: `/analysis/ggxlk`,
    method: "delete",
    data: data,
  });
}


// 指挥流程分类----------------------------------------------------------------

/**
 * 指挥流程分类Bm生成
 * */
export function jclCreateBm(data) {
  return request({
    url: `/groc/common/createBm`,
    method: "get",
    params: data,
  });
}

/**
 * 指挥流程分类Bm重复
 * */
export function jclRuleYwlxBm(data) {
  return request({
    url: `/groc/analysis/jcl/bm`,
    method: "get",
    params: data,
  });
}

/**
 * 指挥流程分类新增
 * */
export function jclAdd(data) {
  return request({
    url: `/groc/analysis/jcl`,
    method: "post",
    data: data,
  });
}

/**
 * 指挥流程分类编辑
 * */
export function jclEdit(data) {
  return request({
    url: `/groc/analysis/jcl`,
    method: "put",
    data: data,
  });
}

/**
 * 指挥流程分类列表
 * */
export function jclList(data) {
  return request({
    url: `/groc/analysis/jcl/list`,
    method: "get",
    params: data,
  });
}

/**
 * 指挥流程分类删除
 * */
export function jclDel(data) {
  return request({
    url: `/analysis/jcl`,
    method: "delete",
    data: data,
  });
}



// 指挥流程阶段----------------------------------------------------------------

/**
 * 指挥流程阶段Bm生成
 * */
export function jclJdCreateBm(data) {
  return request({
    url: `/groc/common/createBm`,
    method: "get",
    params: data,
  });
}

/**
 * 指挥流程阶段Bm重复
 * */
export function jclJdRuleYwlxBm(data) {
  return request({
    url: `/groc/analysis/jclJd/bm`,
    method: "get",
    params: data,
  });
}

/**
 * 指挥流程阶段新增
 * */
export function jclJdAdd(data) {
  return request({
    url: `/groc/analysis/jclJd`,
    method: "post",
    data: data,
  });
}

/**
 * 指挥流程阶段编辑
 * */
export function jclJdEdit(data) {
  return request({
    url: `/groc/analysis/jclJd`,
    method: "put",
    data: data,
  });
}

/**
 * 指挥流程阶段列表
 * */
export function jclJdList(data) {
  return request({
    url: `/groc/analysis/JclJd/list`,
    method: "get",
    params: data,
  });
}
/**
 * 指挥流程阶段列表全部数据
 * */
export function jclJdListAll(data) {
  return request({
    url: `/groc/analysis/jclJd/listAll`,
    method: "get",
    params: data,
  });
}
/**
 * 指挥流程阶段删除
 * */
export function jclLCDel(data) {
  return request({
    url: `/groc/analysis/jcl/${data}`,
    method: "delete",
  });
}

/**
 * 指挥流程阶段删除
 * */
export function jclJdDel(data) {
  return request({
    url: `/groc/analysis/jclJd/${data}`,
    method: "delete",
  });
}

/**
 * 指挥流程分类
 * */
export function jclCollection(data = {}) {
  return request({
    url: `groc/analysis/jcl/collection`,
    method: "get",
    data: data,
  });
}


/**
 * 配块下拉
 * */
export function pkglCollection(data = {}) {
  return request({
    url: `/groc/analysis/pkgl/collection`,
    method: "get",
    data: data,
  });
}

/**
 * 智能打标
 * */
export function jclJdtag(data) {
  return request({
    url: `/analysis/JclJd/tag`,
    method: "get",
    params: data,
  });
}

/**
 * 智能打标 新增
 * @ms 
 * */
export function jclJdAddtag(data) {
  return request({
    url: `/analysis/JclJd/add/tag`,
    method: "get",
    params: data,
  });
}


// 业务配块-------------------------------------------------
/**
 * 业务配块Bm生成
 * */
export function pkglCreateBm(data) {
  return request({
    url: `/publics/pkgl/createBm`,
    method: "get",
    params: data,
  });
}

/**
 * 业务配块Bm重复
 * */
export function pkglRuleYwlxBm(data) {
  return request({
    url: `/publics/pkgl/bm`,
    method: "get",
    params: data,
  });
}

/**
 * 业务配块新增
 * */
export function pkglAdd(data) {
  return request({
    url: `/publics/pkgl`,
    method: "post",
    data: data,
  });
}

/**
 * 业务配块编辑
 * */
export function pkglEdit(data) {
  return request({
    url: `/publics/pkgl`,
    method: "put",
    data: data,
  });
}

/**
 * 业务配块列表
 * */
export function pkglList(data) {
  return request({
    url: `/publics/pkgl/list`,
    method: "get",
    params: data,
  });
}

/**
 * 业务配块删除
 * */
export function pkglDel(data) {
  return request({
    url: `/publics/pkgl`,
    method: "delete",
    data: data,
  });
}

/**
 * 业务配块智能打标
 * */
export function pkglTag(data) {
  return request({
    url: `/publics/pkgl/tag`,
    method: "get",
    params: data,
  });
}

/**
 * 业务配块智能打标 新增
 * @ms
 * */
export function pkglAddTag(data) {
  return request({
    url: `/publics/pkgl/add/tag`,
    method: "get",
    params: data,
  });
}

/**
 * 配块，决策链  标签 调用这个接口，返回的列表 作为下拉
 * */
export function labelTree() {
  return request({
    url: `/groc/analysis/label/tree`,
    method: "get",
    params: { categoryId: 226 },
  });
}

/**
 * 业务配块编辑模板Bm重复
 * */
export function pkcsLxRuleCode(data) {
  return request({
    url: `/publics/common/tsmb/code`,
    method: "get",
    params: data,
  });
}

/***
 * @mc 
 * 通过汉字生成编码
 */
export function pkcsLxCreateBm(data) {
  return request({
    url: `/groc/common/createBm`,
    method: "get",
    params: data,
  });
}

/***
 * 表单类型下拉
 */
export function pkcsLxCollection(data) {
  return request({
    url: `/publics/pkcsLx/collection`,
    method: "get",
    params: data,
  });
}

/**
 * 业务配块编辑判断表单类型编码是否重复
 * @code : bm
 * */
export function pkcsLxCode(data) {
  return request({
    url: `/publics/pkcsLx/code`,
    method: "get",
    params: data,
  });
}

/**
 * 传所有的表单参数，生成提示模板
 * @qxjyPkcsList
 * */
export function pkcsLxXqPrompt(data) {
  return request({
    url: `/publics/pkcsLxXq/prompt`,
    method: "post",
    data: data,
  });
}

/****
 *  查询编辑业务配快详情
 * @id
 */
export function pkcsLxXqInfo(id) {
  return request({
    url: `/publics/pkgl/` + id,
    method: "get",
  });
}

/*** 决策链阶段报告上传 */
export function uploadJclJdFile(data) {
  return request({
    url: `/analysis/JclJd/upload`,
    method: "post",
    data: data
  });
}

/*** 点击模板 查看模板
 * @code
 */
export function tsmbCode(data) {
  return request({
    url: `/publics/common/tsmb/code`,
    method: "get",
    params: data
  });
}

/***
 * 编辑 / 新增保存 参数设置
 */
export function saveCSSZ(data) {
  return request({
    url: `/wjjy/sys/cssz`,
    method: "post",
    data: data
  });
}

/***
 * 查询 参数设置
 * @id
 */
export function csszDetail(id) {
  return request({
    url: `/wjjy/sys/cssz/yhId/` + id,
    method: "get"
  });
}