package org.jeecg.modules.wrj.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.wrj.entity.WjbdWrjKysq;
import org.jeecg.modules.wrj.entity.WjbdWrjTyjh;
import org.jeecg.modules.wrj.entity.WjbdWrjTyjhData;
import org.jeecg.modules.wrj.service.IWjbdWrjTyjhDataService;
import org.jeecg.modules.wrj.service.IWjbdWrjTyjhService;

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
 * @Description: 武警部队_无人机_推演计划
 * @Author: jeecg-boot
 * @Date: 2025-09-22
 * @Version: V1.0
 */
@Api(tags = "武警部队_无人机_推演计划")
@RestController
@RequestMapping("/wrj/wjbdWrjTyjh")
@Slf4j
public class WjbdWrjTyjhController extends JeecgController<WjbdWrjTyjh, IWjbdWrjTyjhService> {
    @Autowired
    private IWjbdWrjTyjhService wjbdWrjTyjhService;
    @Autowired
    private IWjbdWrjTyjhDataService  iWjbdWrjTyjhDataService;
    /**
     * 分页列表查询
     *
     * @param wjbdWrjTyjh
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "武警部队_无人机_推演计划-分页列表查询",YYMK = "模拟推演-推演计划")
    @ApiOperation(value = "武警部队_无人机_推演计划-分页列表查询", notes = "武警部队_无人机_推演计划-分页列表查询")
    @GetMapping(value = "/list")
    public AjaxResult<?> queryPageList(WjbdWrjTyjh wjbdWrjTyjh,
                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                       HttpServletRequest req) {
        QueryWrapper<WjbdWrjTyjh> queryWrapper = QueryGenerator.initQueryWrapper(wjbdWrjTyjh, req.getParameterMap());
        Page<WjbdWrjTyjh> page = new Page<WjbdWrjTyjh>(pageNo, pageSize);
        queryWrapper.orderByDesc("rq");
        IPage<WjbdWrjTyjh> pageList = wjbdWrjTyjhService.page(page, queryWrapper);
        return AjaxResult.OK(pageList);
    }

    /**
     * 添加
     *
     * @param wjbdWrjTyjh
     * @return
     */
    @AutoLog(value = "武警部队_无人机_推演计划-添加",YYMK = "模拟推演-推演计划")
    @ApiOperation(value = "武警部队_无人机_推演计划-添加", notes = "武警部队_无人机_推演计划-添加")
    @PostMapping(value = "/add")
    public AjaxResult<?> add(@RequestBody WjbdWrjTyjh wjbdWrjTyjh) {
        wjbdWrjTyjhService.save(wjbdWrjTyjh);
        return AjaxResult.OK("添加成功！");
    }


    /**
     * 添加
     *
     * @param wjbdWrjTyjh
     * @return
     */
    @AutoLog(value = "武警部队_无人机_推演计划-生成",YYMK = "模拟推演-推演计划")
    @ApiOperation(value = "武警部队_无人机_推演计划-生成", notes = "武警部队_无人机_推演计划-生成")
    @PostMapping(value = "/TYJHSC")
    public AjaxResult<?> TYJHSC(@RequestBody WjbdWrjTyjh wjbdWrjTyjh) {
        if (!wjbdWrjTyjh.getUavDatectMsgDto1().getHdcs().isEmpty()) {
            List<Double> segmentSpeeds=new ArrayList<>();
            wjbdWrjTyjh.getUavDatectMsgDto1().getHdcs().stream().forEach(item->{
                segmentSpeeds.add(item.getSd());
            });
            wjbdWrjTyjh.getUavDatectMsgDto1().setSegmentSpeeds(segmentSpeeds);
        }
        wjbdWrjTyjhService.TYJHSC(wjbdWrjTyjh);
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.OK("生成成功！");
        ajaxResult.setResult(wjbdWrjTyjh.getId());
        return ajaxResult;
    }

    /**
     * 添加
     *
     * @param id
     * @return
     */
    @AutoLog(value = "武警部队_无人机_推演计划-生成",YYMK = "模拟推演-推演计划")
    @ApiOperation(value = "武警部队_无人机_推演计划-生成", notes = "武警部队_无人机_推演计划-生成")
    @GetMapping(value = "/TYJHCZ")
    public AjaxResult<?> TYJHCZ(@RequestParam(name = "id", required = true) String id, @RequestParam(name = "zt", required = true) Integer zt) {
        WjbdWrjTyjh wjbdWrjTyjh = wjbdWrjTyjhService.getById(id);
        if (zt == 0) {
            wjbdWrjTyjh.setTyzt("进行中");
        } else if (zt == 1) {
            wjbdWrjTyjh.setTyzt("已完成");
        }
        wjbdWrjTyjhService.updateById(wjbdWrjTyjh);
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.OK("操作成功！");
        return ajaxResult;
    }

    /**
     * 编辑
     *
     * @param wjbdWrjTyjh
     * @return
     */
    @AutoLog(value = "武警部队_无人机_推演计划-编辑",YYMK = "模拟推演-推演计划")
    @ApiOperation(value = "武警部队_无人机_推演计划-编辑", notes = "武警部队_无人机_推演计划-编辑")
    @PutMapping(value = "/edit")
    public AjaxResult<?> edit(@RequestBody WjbdWrjTyjh wjbdWrjTyjh) {
        wjbdWrjTyjhService.removeById(wjbdWrjTyjh.getId());
        //关联删除推演计划详细数据
        QueryWrapper<WjbdWrjTyjhData> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("tyjh_id",wjbdWrjTyjh.getId());
        iWjbdWrjTyjhDataService.remove(queryWrapper);
        if (!wjbdWrjTyjh.getUavDatectMsgDto1().getHdcs().isEmpty()) {
            List<Double> segmentSpeeds=new ArrayList<>();
            wjbdWrjTyjh.getUavDatectMsgDto1().getHdcs().stream().forEach(item->{
                segmentSpeeds.add(item.getSd());
            });
            wjbdWrjTyjh.getUavDatectMsgDto1().setSegmentSpeeds(segmentSpeeds);
        }
        wjbdWrjTyjhService.TYJHSC(wjbdWrjTyjh);
        return AjaxResult.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "武警部队_无人机_推演计划-通过id删除",YYMK = "模拟推演-推演计划")
    @ApiOperation(value = "武警部队_无人机_推演计划-通过id删除", notes = "武警部队_无人机_推演计划-通过id删除")
    @DeleteMapping(value = "/delete")
    public AjaxResult<?> delete(@RequestParam(name = "id", required = true) String id) {
        wjbdWrjTyjhService.removeById(id);
        //关联删除推演计划详细数据
        QueryWrapper<WjbdWrjTyjhData> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("tyjh_id",id);
        iWjbdWrjTyjhDataService.remove(queryWrapper);
        return AjaxResult.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "武警部队_无人机_推演计划-批量删除",YYMK = "模拟推演-推演计划")
    @ApiOperation(value = "武警部队_无人机_推演计划-批量删除", notes = "武警部队_无人机_推演计划-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public AjaxResult<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.wjbdWrjTyjhService.removeByIds(Arrays.asList(ids.split(",")));
        //关联删除推演计划详细数据
        QueryWrapper<WjbdWrjTyjhData> queryWrapper=new QueryWrapper<>();
        queryWrapper.in("tyjh_id",Arrays.asList(ids.split(",")));
        iWjbdWrjTyjhDataService.remove(queryWrapper);
        return AjaxResult.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "武警部队_无人机_推演计划-通过id查询",YYMK = "模拟推演-推演计划")
    @ApiOperation(value = "武警部队_无人机_推演计划-通过id查询", notes = "武警部队_无人机_推演计划-通过id查询")
    @GetMapping(value = "/queryById")
    public AjaxResult<?> queryById(@RequestParam(name = "id", required = true) String id) {
        WjbdWrjTyjh wjbdWrjTyjh = wjbdWrjTyjhService.getById(id);
        if (wjbdWrjTyjh == null) {
            return AjaxResult.error("未找到对应数据");
        }
        return AjaxResult.OK(wjbdWrjTyjh);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param wjbdWrjTyjh
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WjbdWrjTyjh wjbdWrjTyjh,String type) {
        if (StringUtils.isNotBlank(type)&&type.equals("template")){
            return	super.exportXlsTemplate(request, wjbdWrjTyjh, WjbdWrjTyjh.class, "武警部队_无人机_推演计划-导入模板");
        }
        return super.exportXls(request, wjbdWrjTyjh, WjbdWrjTyjh.class, "武警部队_无人机_推演计划");
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
        return super.importExcel(request, response, WjbdWrjTyjh.class);
    }

}
