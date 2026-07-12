package org.jeecg.modules.dxyy.controller;

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
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.dxyy.entity.WjbdDxyyDxdm;
import org.jeecg.modules.dxyy.service.IWjbdDxyyDxdmService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.wrj.entity.WjbdWrjZymb;
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
 * @Description: 武警部队_典型应用_地形地貌
 * @Author: jeecg-boot
 * @Date:   2025-11-03
 * @Version: V1.0
 */
@Api(tags="武警部队_典型应用_地形地貌")
@RestController
@RequestMapping("/dxyy/wjbdDxyyDxdm")
@Slf4j
public class WjbdDxyyDxdmController extends JeecgController<WjbdDxyyDxdm, IWjbdDxyyDxdmService> {
	@Autowired
	private IWjbdDxyyDxdmService wjbdDxyyDxdmService;
	
	/**
	 * 分页列表查询
	 *
	 * @param wjbdDxyyDxdm
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "武警部队_典型应用_地形地貌-分页列表查询",YYMK = "数据管理-地形地貌")
	@ApiOperation(value="武警部队_典型应用_地形地貌-分页列表查询", notes="武警部队_典型应用_地形地貌-分页列表查询")
	@GetMapping(value = "/list")
	public AjaxResult<?> queryPageList(WjbdDxyyDxdm wjbdDxyyDxdm,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<WjbdDxyyDxdm> queryWrapper = QueryGenerator.initQueryWrapper(wjbdDxyyDxdm, req.getParameterMap());
		Page<WjbdDxyyDxdm> page = new Page<WjbdDxyyDxdm>(pageNo, pageSize);
		IPage<WjbdDxyyDxdm> pageList = wjbdDxyyDxdmService.page(page, queryWrapper);
		return AjaxResult.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param wjbdDxyyDxdm
	 * @return
	 */
	@AutoLog(value = "武警部队_典型应用_地形地貌-添加",YYMK = "数据管理-地形地貌")
	@ApiOperation(value="武警部队_典型应用_地形地貌-添加", notes="武警部队_典型应用_地形地貌-添加")
	@PostMapping(value = "/add")
	public AjaxResult<?> add(@RequestBody WjbdDxyyDxdm wjbdDxyyDxdm) {
		wjbdDxyyDxdmService.save(wjbdDxyyDxdm);
		return AjaxResult.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param wjbdDxyyDxdm
	 * @return
	 */
	@AutoLog(value = "武警部队_典型应用_地形地貌-编辑",YYMK = "数据管理-地形地貌")
	@ApiOperation(value="武警部队_典型应用_地形地貌-编辑", notes="武警部队_典型应用_地形地貌-编辑")
	@PutMapping(value = "/edit")
	public AjaxResult<?> edit(@RequestBody WjbdDxyyDxdm wjbdDxyyDxdm) {
		wjbdDxyyDxdmService.updateById(wjbdDxyyDxdm);
		return AjaxResult.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_典型应用_地形地貌-通过id删除",YYMK = "数据管理-地形地貌")
	@ApiOperation(value="武警部队_典型应用_地形地貌-通过id删除", notes="武警部队_典型应用_地形地貌-通过id删除")
	@DeleteMapping(value = "/delete")
	public AjaxResult<?> delete(@RequestParam(name="id",required=true) String id) {
		wjbdDxyyDxdmService.removeById(id);
		return AjaxResult.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "武警部队_典型应用_地形地貌-批量删除",YYMK = "数据管理-地形地貌")
	@ApiOperation(value="武警部队_典型应用_地形地貌-批量删除", notes="武警部队_典型应用_地形地貌-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public AjaxResult<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.wjbdDxyyDxdmService.removeByIds(Arrays.asList(ids.split(",")));
		return AjaxResult.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_典型应用_地形地貌-通过id查询",YYMK = "数据管理-地形地貌")
	@ApiOperation(value="武警部队_典型应用_地形地貌-通过id查询", notes="武警部队_典型应用_地形地貌-通过id查询")
	@GetMapping(value = "/queryById")
	public AjaxResult<?> queryById(@RequestParam(name="id",required=true) String id) {
		WjbdDxyyDxdm wjbdDxyyDxdm = wjbdDxyyDxdmService.getById(id);
		if(wjbdDxyyDxdm==null) {
			return AjaxResult.error("未找到对应数据");
		}
		return AjaxResult.OK(wjbdDxyyDxdm);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param wjbdDxyyDxdm
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WjbdDxyyDxdm wjbdDxyyDxdm,String type) {
		if (StringUtils.isNotBlank(type)&&type.equals("template")){
			return	super.exportXlsTemplate(request, wjbdDxyyDxdm, WjbdDxyyDxdm.class, "武警部队_典型应用_地形地貌-导入模板");
		}
        return super.exportXls(request, wjbdDxyyDxdm, WjbdDxyyDxdm.class, "武警部队_典型应用_地形地貌");
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
        return super.importExcel(request, response, WjbdDxyyDxdm.class);
    }

	 /**
	  * 范围查询
	  *
	  * @param jd
	  * @param wd
	  * @param jl
	  * @return
	  */
	 @AutoLog(value = "武警部队_典型应用_地形地貌-范围查询",YYMK = "数据管理-地形地貌")
	 @ApiOperation(value="武警部队_典型应用_地形地貌-范围查询", notes="武警部队_典型应用_地形地貌-范围查询")
	 @GetMapping(value = "/getDxdmByJwdAndJl")
	 public AjaxResult<?> getDxdmByJwdAndJl(@RequestParam(name="jd") String jd,
											   @RequestParam(name="wd") String wd,
											   @RequestParam(name="jl") String jl) {
		 List<WjbdDxyyDxdm> list = wjbdDxyyDxdmService.getDxdmByJwdAndJl(jd,wd ,jl);
		 return AjaxResult.OK(list);
	 }
}
