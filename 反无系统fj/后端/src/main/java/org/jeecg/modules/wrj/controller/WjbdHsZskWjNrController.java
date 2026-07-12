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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.wrj.entity.WjbdHsZskWjNr;
import org.jeecg.modules.wrj.service.IWjbdHsZskWjNrService;

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
 * @Description: 知识库_文件_内容
 * @Author: jeecg-boot
 * @Date: 2025-10-27
 * @Version: V1.0
 */
@Api(tags = "知识库_文件_内容")
@RestController
@RequestMapping("/wjfjsjzt/wjbdHsZskWjNr")
@Slf4j
public class WjbdHsZskWjNrController extends JeecgController<WjbdHsZskWjNr, IWjbdHsZskWjNrService> {
    @Autowired
    private IWjbdHsZskWjNrService wjbdHsZskWjNrService;

    /**
     * 分页列表查询
     *
     * @param wjbdHsZskWjNr
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "知识库_文件_内容-分页列表查询")
    @ApiOperation(value = "知识库_文件_内容-分页列表查询", notes = "知识库_文件_内容-分页列表查询")
    @GetMapping(value = "/list")
    public AjaxResult<?> queryPageList(WjbdHsZskWjNr wjbdHsZskWjNr,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        LambdaQueryWrapper<WjbdHsZskWjNr> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(wjbdHsZskWjNr.getZskId() != null, WjbdHsZskWjNr::getZskId, wjbdHsZskWjNr.getZskId());
        queryWrapper.eq(wjbdHsZskWjNr.getZskWjId() != null, WjbdHsZskWjNr::getZskWjId, wjbdHsZskWjNr.getZskWjId());
        Page<WjbdHsZskWjNr> page = new Page<WjbdHsZskWjNr>(pageNo, pageSize);
        IPage<WjbdHsZskWjNr> pageList = wjbdHsZskWjNrService.page(page, queryWrapper);
        return AjaxResult.OK(pageList);
    }

    /**
     * 添加
     *
     * @param wjbdHsZskWjNr
     * @return
     */
    @AutoLog(value = "知识库_文件_内容-添加")
    @ApiOperation(value = "知识库_文件_内容-添加", notes = "知识库_文件_内容-添加")
    @PostMapping(value = "/add")
    public AjaxResult<?> add(@RequestBody WjbdHsZskWjNr wjbdHsZskWjNr) {
        wjbdHsZskWjNrService.save(wjbdHsZskWjNr);
        return AjaxResult.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param wjbdHsZskWjNr
     * @return
     */
    @AutoLog(value = "知识库_文件_内容-编辑")
    @ApiOperation(value = "知识库_文件_内容-编辑", notes = "知识库_文件_内容-编辑")
    @PutMapping(value = "/edit")
    public AjaxResult<?> edit(@RequestBody WjbdHsZskWjNr wjbdHsZskWjNr) {
        wjbdHsZskWjNrService.edit(wjbdHsZskWjNr);
        return AjaxResult.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "知识库_文件_内容-通过id删除")
    @ApiOperation(value = "知识库_文件_内容-通过id删除", notes = "知识库_文件_内容-通过id删除")
    @DeleteMapping(value = "/delete")
    public AjaxResult<?> delete(@RequestParam(name = "id", required = true) String id) {
        wjbdHsZskWjNrService.removeById(id);
        return AjaxResult.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "知识库_文件_内容-批量删除")
    @ApiOperation(value = "知识库_文件_内容-批量删除", notes = "知识库_文件_内容-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public AjaxResult<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.wjbdHsZskWjNrService.removeByIds(Arrays.asList(ids.split(",")));
        return AjaxResult.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "知识库_文件_内容-通过id查询")
    @ApiOperation(value = "知识库_文件_内容-通过id查询", notes = "知识库_文件_内容-通过id查询")
    @GetMapping(value = "/queryById")
    public AjaxResult<?> queryById(@RequestParam(name = "id", required = true) String id) {
        WjbdHsZskWjNr wjbdHsZskWjNr = wjbdHsZskWjNrService.getById(id);
        if (wjbdHsZskWjNr == null) {
            return AjaxResult.error("未找到对应数据");
        }
        return AjaxResult.OK(wjbdHsZskWjNr);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param wjbdHsZskWjNr
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WjbdHsZskWjNr wjbdHsZskWjNr) {
        return super.exportXls(request, wjbdHsZskWjNr, WjbdHsZskWjNr.class, "知识库_文件_内容");
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
        return super.importExcel(request, response, WjbdHsZskWjNr.class);
    }

}
