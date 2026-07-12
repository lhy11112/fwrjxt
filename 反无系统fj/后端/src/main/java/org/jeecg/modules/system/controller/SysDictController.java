package org.jeecg.modules.system.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.DictQuery;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.service.ISysDictItemService;
import org.jeecg.modules.system.service.ISysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * <p>
 * 字典表 前端控制器
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@RestController
@RequestMapping("/sys/dict")
@Slf4j
public class SysDictController {

    @Autowired
    private ISysDictService sysDictService;
    @Autowired
    private ISysDictItemService sysDictItemService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public AjaxResult<IPage<SysDict>> queryPageList(SysDict sysDict, @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, HttpServletRequest req) {
        Map<String, String[]> parameterMap = req.getParameterMap();
        // 检查参数映射是否为空
        if (parameterMap == null || parameterMap.isEmpty()) {
            if (log.isInfoEnabled()){
                log.info("参数为空！！！");
            }
        }
        AjaxResult<IPage<SysDict>> result = new AjaxResult<IPage<SysDict>>();
        QueryWrapper<SysDict> queryWrapper = QueryGenerator.initQueryWrapper(sysDict, parameterMap);
        Page<SysDict> page = new Page<SysDict>(pageNo, pageSize);
        IPage<SysDict> pageList = sysDictService.page(page, queryWrapper);
        if (log.isDebugEnabled()) {
            log.debug("查询当前页：" + pageList.getCurrent());
            log.debug("查询当前页数量：" + pageList.getSize());
            log.debug("查询结果数量：" + pageList.getRecords().size());
            log.debug("数据总数：" + pageList.getTotal());
        }
        result.setSuccess(true);
        result.setResult(pageList);
        return result;
    }

    /**
     * 获取字典数据
     *
     * @param dictCode 字典code
     * @param dictCode 表名,文本字段,code字段  | 举例：sys_user,realname,id
     * @return
     */
    @RequestMapping(value = "/getDictItems/{dictCode}", method = RequestMethod.GET)
    public AjaxResult<List<DictModel>> getDictItems(@PathVariable String dictCode, @RequestParam(value = "sign", required = false) String sign, HttpServletRequest request) {
        if (log.isInfoEnabled()) {
            log.info(" dictCode : " + dictCode);
        }
        AjaxResult<List<DictModel>> result = new AjaxResult<List<DictModel>>();
        List<DictModel> ls = null;
        try {
            if (dictCode.indexOf(',') != -1) {
                //关联表字典（举例：sys_user,realname,id）
                String[] params = dictCode.split(",");

                if (params.length < 3) {
                    result.error500("字典Code格式不正确！");
                    return result;
                }
                //SQL注入校验（只限制非法串改数据库）
                final String[] sqlInjCheck = {params[0], params[1], params[2]};
                SqlInjectionUtil.filterContent(sqlInjCheck);

                if (params.length == 4) {
                    //SQL注入校验（查询条件SQL 特殊check，此方法仅供此处使用）
                    SqlInjectionUtil.specialFilterContent(params[3]);
                    ls = sysDictService.queryTableDictItemsByCodeAndFilter(params[0], params[1], params[2], params[3]);
                } else if (params.length == 3) {
                    ls = sysDictService.queryTableDictItemsByCode(params[0], params[1], params[2]);
                } else {
                    result.error500("字典Code格式不正确！");
                    return result;
                }
            } else {
                //字典表
                ls = sysDictService.queryDictItemsByCode(dictCode);
            }

            result.setSuccess(true);
            result.setResult(ls);
            if (log.isDebugEnabled()) {
                log.debug(result.toString());
            }
        } catch (JeecgBootException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage());
            }
            result.error500("操作失败");
            return result;
        }

        return result;
    }


    /**
     * 获取字典数据
     *
     * @param dictCode
     * @return
     */
    @RequestMapping(value = "/getDictText/{dictCode}/{key}", method = RequestMethod.GET)
    @SneakyThrows
    public AjaxResult<String> getDictText(@PathVariable("dictCode") String dictCode, @PathVariable("key") String key) {
        if (log.isInfoEnabled()) {
            log.info(" dictCode : " + dictCode);
        }
        AjaxResult<String> result = new AjaxResult<String>();
        String text = null;
        text = sysDictService.queryDictTextByKey(dictCode, key);
        result.setSuccess(true);
        result.setResult(text);
        return result;
    }

    /**
     * 大数据量的字典表 走异步加载  即前端输入内容过滤数据
     *
     * @param dictCode
     * @return
     */
    @RequestMapping(value = "/loadDict/{dictCode}", method = RequestMethod.GET)
    public AjaxResult<List<DictModel>> loadDict(@PathVariable String dictCode,
                                            @RequestParam(name = "keyword") String keyword,
                                            @RequestParam(value = "sign", required = false) String sign,
                                            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        if (log.isInfoEnabled()) {
            log.info(" 加载字典表数据,加载关键字: " + keyword);
        }
        AjaxResult<List<DictModel>> result = new AjaxResult<List<DictModel>>();
        List<DictModel> ls = null;
        try {
            if (dictCode.indexOf(',') != -1) {
                String[] params = dictCode.split(",");
                if (params.length != 3) {
                    result.error500("字典Code格式不正确！");
                    return result;
                }
                if (pageSize != null) {
                    ls = sysDictService.queryLittleTableDictItems(params[0], params[1], params[2], keyword, pageSize);
                } else {
                    ls = sysDictService.queryTableDictItems(params[0], params[1], params[2], keyword);
                }
                result.setSuccess(true);
                result.setResult(ls);
                if (log.isInfoEnabled()) {
                    log.info(result.toString());
                }
            } else {
                result.error500("字典Code格式不正确！");
            }
        } catch (JeecgBootException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage());
            }
            result.error500("操作失败");
            return result;
        }

        return result;
    }

    /**
     * 根据字典code加载字典text 返回
     */
    @RequestMapping(value = "/loadDictItem/{dictCode}", method = RequestMethod.GET)
    public AjaxResult<List<String>> loadDictItem(@PathVariable String dictCode, @RequestParam(name = "key") String keys, @RequestParam(value = "sign", required = false) String sign, HttpServletRequest request) {
        AjaxResult<List<String>> result = new AjaxResult<>();
        try {
            if (dictCode.indexOf(',') != -1) {
                String[] params = dictCode.split(",");
                if (params.length != 3) {
                    result.error500("字典Code格式不正确！");
                    return result;
                }
                List<String> texts = sysDictService.queryTableDictByKeys(params[0], params[1], params[2], keys);

                result.setSuccess(true);
                result.setResult(texts);
                if (log.isInfoEnabled()) {
                    log.info(result.toString());
                }
            } else {
                result.error500("字典Code格式不正确！");
            }
        } catch (JeecgBootException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage());
            }
            result.error500("操作失败");
            return result;
        }

        return result;
    }

    /**
     * 【APP接口】根据字典配置查询表字典数据（目前暂未找到调用的地方）
     *
     * @param query
     * @param pageNo
     * @param pageSize
     * @return
     */

    @GetMapping("/queryTableData")
    public AjaxResult<List<DictModel>> queryTableData(DictQuery query,
                                                  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                  @RequestParam(value = "sign", required = false) String sign, HttpServletRequest request) {
        AjaxResult<List<DictModel>> res = new AjaxResult<List<DictModel>>();
        // SQL注入漏洞 sign签名校验
        String dictCode = query.getTable() + ',' + query.getText() + ',' + query.getCode();
        SqlInjectionUtil.filterContent(dictCode);
        List<DictModel> ls = this.sysDictService.queryDictTablePageList(query, pageSize, pageNo);
        res.setResult(ls);
        res.setSuccess(true);
        return res;
    }

    /**
     * @param sysDict
     * @return
     * @功能：新增
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public AjaxResult<SysDict> add(@RequestBody SysDict sysDict) {
        AjaxResult<SysDict> result = new AjaxResult<SysDict>();
        try {
            sysDict.setCreateTime(new Date());
            sysDict.setDelFlag(CommonConstant.DEL_FLAG_0);
            sysDictService.save(sysDict);
            result.success("保存成功！");
        } catch (JeecgBootException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage());
            }
            result.error500("操作失败");
        }
        return result;
    }

    /**
     * @param sysDict
     * @return
     * @功能：编辑
     */
    @RequestMapping(value = "/edit", method = RequestMethod.PUT)
    public AjaxResult<SysDict> edit(@RequestBody SysDict sysDict) {
        AjaxResult<SysDict> result = new AjaxResult<SysDict>();
        SysDict sysdict = sysDictService.getById(sysDict.getId());
        if (sysdict == null) {
            result.error500("未找到对应实体");
        } else {
            sysDict.setUpdateTime(new Date());
            boolean ok = sysDictService.updateById(sysDict);
            if (ok) {
                result.success("编辑成功!");
            }
        }
        return result;
    }


}
