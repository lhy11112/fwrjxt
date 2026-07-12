package org.jeecg.modules.uav.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.uav.entity.UavDetectSpectrum;
import org.jeecg.modules.uav.service.IUavDetectSpectrumService;

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
 * @Description: 侦测频谱数据表-频谱数据
 * @Author: jeecg-boot
 * @Date: 2025-09-15
 * @Version: V1.0
 */
@Api(tags = "侦测频谱数据表-频谱数据")
@RestController
@RequestMapping("/uav/uavDetectSpectrum")
@Slf4j
public class UavDetectSpectrumController extends JeecgController<UavDetectSpectrum, IUavDetectSpectrumService> {
    @Autowired
    private IUavDetectSpectrumService uavDetectSpectrumService;

    /**
     * 分页列表查询
     *
     * @param uavDetectSpectrum
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "侦测频谱数据表-频谱数据-分页列表查询")
    @ApiOperation(value = "侦测频谱数据表-频谱数据-分页列表查询", notes = "侦测频谱数据表-频谱数据-分页列表查询")
    @GetMapping(value = "/list")
    public AjaxResult<?> queryPageList(UavDetectSpectrum uavDetectSpectrum,
                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                       HttpServletRequest req) {
        QueryWrapper<UavDetectSpectrum> queryWrapper = QueryGenerator.initQueryWrapper(uavDetectSpectrum, req.getParameterMap());
        Page<UavDetectSpectrum> page = new Page<UavDetectSpectrum>(pageNo, pageSize);
        IPage<UavDetectSpectrum> pageList = uavDetectSpectrumService.page(page, queryWrapper);
        return AjaxResult.OK(pageList);
    }

    /**
     * 分页列表查询
     *
     * @param uavDetectSpectrum
     * @param req
     * @return
     */
    @AutoLog(value = "侦测频谱数据表-频谱数据-分页列表查询")
    @ApiOperation(value = "侦测频谱数据表-频谱数据-分页列表查询", notes = "侦测频谱数据表-频谱数据-分页列表查询")
    @GetMapping(value = "/listNow")
    public AjaxResult<?> queryPageList(UavDetectSpectrum uavDetectSpectrum,
                                       HttpServletRequest req,
                                       @RequestParam(name = "rq", required = true) String rq
    ) {
        return AjaxResult.OK(uavDetectSpectrumService.listNow(rq));
    }

    /**
     * 添加
     *
     * @param uavDetectSpectrum
     * @return
     */
    @AutoLog(value = "侦测频谱数据表-频谱数据-添加")
    @ApiOperation(value = "侦测频谱数据表-频谱数据-添加", notes = "侦测频谱数据表-频谱数据-添加")
    @PostMapping(value = "/add")
    public AjaxResult<?> add(@RequestBody UavDetectSpectrum uavDetectSpectrum) {
        uavDetectSpectrumService.save(uavDetectSpectrum);
        return AjaxResult.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param uavDetectSpectrum
     * @return
     */
    @AutoLog(value = "侦测频谱数据表-频谱数据-编辑")
    @ApiOperation(value = "侦测频谱数据表-频谱数据-编辑", notes = "侦测频谱数据表-频谱数据-编辑")
    @PutMapping(value = "/edit")
    public AjaxResult<?> edit(@RequestBody UavDetectSpectrum uavDetectSpectrum) {
        uavDetectSpectrumService.updateById(uavDetectSpectrum);
        return AjaxResult.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "侦测频谱数据表-频谱数据-通过id删除")
    @ApiOperation(value = "侦测频谱数据表-频谱数据-通过id删除", notes = "侦测频谱数据表-频谱数据-通过id删除")
    @DeleteMapping(value = "/delete")
    public AjaxResult<?> delete(@RequestParam(name = "id", required = true) String id) {
        uavDetectSpectrumService.removeById(id);
        return AjaxResult.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "侦测频谱数据表-频谱数据-批量删除")
    @ApiOperation(value = "侦测频谱数据表-频谱数据-批量删除", notes = "侦测频谱数据表-频谱数据-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public AjaxResult<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.uavDetectSpectrumService.removeByIds(Arrays.asList(ids.split(",")));
        return AjaxResult.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "侦测频谱数据表-频谱数据-通过id查询")
    @ApiOperation(value = "侦测频谱数据表-频谱数据-通过id查询", notes = "侦测频谱数据表-频谱数据-通过id查询")
    @GetMapping(value = "/queryById")
    public AjaxResult<?> queryById(@RequestParam(name = "id", required = true) String id) {
        UavDetectSpectrum uavDetectSpectrum = uavDetectSpectrumService.getById(id);
        if (uavDetectSpectrum == null) {
            return AjaxResult.error("未找到对应数据");
        }
        return AjaxResult.OK(uavDetectSpectrum);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param uavDetectSpectrum
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, UavDetectSpectrum uavDetectSpectrum) {
        return super.exportXls(request, uavDetectSpectrum, UavDetectSpectrum.class, "侦测频谱数据表-频谱数据");
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
        return super.importExcel(request, response, UavDetectSpectrum.class);
    }

}
