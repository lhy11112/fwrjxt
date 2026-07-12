package org.jeecg.modules.wrj.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.wrj.entity.WjbdWrjLpwj;
import org.jeecg.modules.wrj.service.IWjbdWrjLpwjService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

/**
 * @Description: 武警部队_无人机_录屏文件
 * @Author: jeecg-boot
 * @Date: 2025-11-03
 * @Version: V1.0
 */
@Api(tags = "武警部队_无人机_录屏文件")
@RestController
@RequestMapping("/wrj/wjbdWrjLpwj")
@Slf4j
public class WjbdWrjLpwjController extends JeecgController<WjbdWrjLpwj, IWjbdWrjLpwjService> {
    @Autowired
    private IWjbdWrjLpwjService wjbdWrjLpwjService;

    /**
     * 分页列表查询
     *
     * @param wjbdWrjLpwj
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "武警部队_无人机_录屏文件-分页列表查询",YYMK = "数据管理-录屏文件")
    @ApiOperation(value = "武警部队_无人机_录屏文件-分页列表查询", notes = "武警部队_无人机_录屏文件-分页列表查询")
    @GetMapping(value = "/list")
    public AjaxResult<?> queryPageList(WjbdWrjLpwj wjbdWrjLpwj,
                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                       HttpServletRequest req) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (sysUser != null) {
            wjbdWrjLpwj.setYhid(sysUser.getId());
        }
        QueryWrapper<WjbdWrjLpwj> queryWrapper = QueryGenerator.initQueryWrapper(wjbdWrjLpwj, req.getParameterMap());
        Page<WjbdWrjLpwj> page = new Page<WjbdWrjLpwj>(pageNo, pageSize);
        IPage<WjbdWrjLpwj> pageList = wjbdWrjLpwjService.page(page, queryWrapper);
        return AjaxResult.OK(pageList);
    }

    /**
     * 添加
     *
     * @param wjbdWrjLpwj
     * @return
     */
    @AutoLog(value = "武警部队_无人机_录屏文件-添加",YYMK = "数据管理-录屏文件")
    @ApiOperation(value = "武警部队_无人机_录屏文件-添加", notes = "武警部队_无人机_录屏文件-添加")
    @PostMapping(value = "/add")
    public AjaxResult<?> add(@RequestBody WjbdWrjLpwj wjbdWrjLpwj) {
        LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (sysUser != null) {
            wjbdWrjLpwj.setYhid(sysUser.getId());
        }
        wjbdWrjLpwjService.save(wjbdWrjLpwj);
        return AjaxResult.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param wjbdWrjLpwj
     * @return
     */
    @AutoLog(value = "武警部队_无人机_录屏文件-编辑",YYMK = "数据管理-录屏文件")
    @ApiOperation(value = "武警部队_无人机_录屏文件-编辑", notes = "武警部队_无人机_录屏文件-编辑")
    @PutMapping(value = "/edit")
    public AjaxResult<?> edit(@RequestBody WjbdWrjLpwj wjbdWrjLpwj) {
        wjbdWrjLpwjService.updateById(wjbdWrjLpwj);
        return AjaxResult.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "武警部队_无人机_录屏文件-通过id删除",YYMK = "数据管理-录屏文件")
    @ApiOperation(value = "武警部队_无人机_录屏文件-通过id删除", notes = "武警部队_无人机_录屏文件-通过id删除")
    @DeleteMapping(value = "/delete")
    public AjaxResult<?> delete(@RequestParam(name = "id", required = true) String id) {
        wjbdWrjLpwjService.removeById(id);
        return AjaxResult.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "武警部队_无人机_录屏文件-批量删除",YYMK = "数据管理-录屏文件")
    @ApiOperation(value = "武警部队_无人机_录屏文件-批量删除", notes = "武警部队_无人机_录屏文件-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public AjaxResult<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.wjbdWrjLpwjService.removeByIds(Arrays.asList(ids.split(",")));
        return AjaxResult.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "武警部队_无人机_录屏文件-通过id查询",YYMK = "数据管理-录屏文件")
    @ApiOperation(value = "武警部队_无人机_录屏文件-通过id查询", notes = "武警部队_无人机_录屏文件-通过id查询")
    @GetMapping(value = "/queryById")
    public AjaxResult<?> queryById(@RequestParam(name = "id", required = true) String id) {
        WjbdWrjLpwj wjbdWrjLpwj = wjbdWrjLpwjService.getById(id);
        if (wjbdWrjLpwj == null) {
            return AjaxResult.error("未找到对应数据");
        }
        return AjaxResult.OK(wjbdWrjLpwj);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param wjbdWrjLpwj
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WjbdWrjLpwj wjbdWrjLpwj) {
        return super.exportXls(request, wjbdWrjLpwj, WjbdWrjLpwj.class, "武警部队_无人机_录屏文件");
    }

    /**
     * 通过excel导入数据
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public AjaxResult<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, WjbdWrjLpwj.class);
    }

}
