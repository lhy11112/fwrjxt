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

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.wrj.entity.WjbdWrjJbxx;
import org.jeecg.modules.wrj.entity.WjbdWrjJbxxNew;
import org.jeecg.modules.wrj.service.IWjbdWrjJbxxNewService;

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
 * @Description: 武警部队_无人机_详细信息
 * @Author: jeecg-boot
 * @Date:   2025-10-29
 * @Version: V1.0
 */
@Api(tags="武警部队_无人机_详细信息")
@RestController
@RequestMapping("/wrj/wjbdWrjJbxxNew")
@Slf4j
public class WjbdWrjJbxxNewController extends JeecgController<WjbdWrjJbxxNew, IWjbdWrjJbxxNewService> {
	@Autowired
	private IWjbdWrjJbxxNewService wjbdWrjJbxxNewService;
	
	/**
	 * 分页列表查询
	 *
	 * @param wjbdWrjJbxxNew
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_详细信息-分页列表查询")
	@ApiOperation(value="武警部队_无人机_详细信息-分页列表查询", notes="武警部队_无人机_详细信息-分页列表查询")
	@GetMapping(value = "/list")
	public AjaxResult<?> queryPageList(WjbdWrjJbxxNew wjbdWrjJbxxNew,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<WjbdWrjJbxxNew> queryWrapper = QueryGenerator.initQueryWrapper(wjbdWrjJbxxNew, req.getParameterMap());
		Page<WjbdWrjJbxxNew> page = new Page<WjbdWrjJbxxNew>(pageNo, pageSize);
		IPage<WjbdWrjJbxxNew> pageList = wjbdWrjJbxxNewService.page(page, queryWrapper);
		return AjaxResult.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param wjbdWrjJbxxNew
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_详细信息-添加")
	@ApiOperation(value="武警部队_无人机_详细信息-添加", notes="武警部队_无人机_详细信息-添加")
	@PostMapping(value = "/add")
	public AjaxResult<?> add(@RequestBody WjbdWrjJbxxNew wjbdWrjJbxxNew) {
		wjbdWrjJbxxNewService.save(wjbdWrjJbxxNew);
		return AjaxResult.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param wjbdWrjJbxxNew
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_详细信息-编辑")
	@ApiOperation(value="武警部队_无人机_详细信息-编辑", notes="武警部队_无人机_详细信息-编辑")
	@PutMapping(value = "/edit")
	public AjaxResult<?> edit(@RequestBody WjbdWrjJbxxNew wjbdWrjJbxxNew) {
		wjbdWrjJbxxNewService.updateById(wjbdWrjJbxxNew);
		return AjaxResult.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_详细信息-通过id删除")
	@ApiOperation(value="武警部队_无人机_详细信息-通过id删除", notes="武警部队_无人机_详细信息-通过id删除")
	@DeleteMapping(value = "/delete")
	public AjaxResult<?> delete(@RequestParam(name="id",required=true) String id) {
		wjbdWrjJbxxNewService.removeById(id);
		return AjaxResult.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_详细信息-批量删除")
	@ApiOperation(value="武警部队_无人机_详细信息-批量删除", notes="武警部队_无人机_详细信息-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public AjaxResult<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.wjbdWrjJbxxNewService.removeByIds(Arrays.asList(ids.split(",")));
		return AjaxResult.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_详细信息-通过id查询")
	@ApiOperation(value="武警部队_无人机_详细信息-通过id查询", notes="武警部队_无人机_详细信息-通过id查询")
	@GetMapping(value = "/queryById")
	public AjaxResult<?> queryById(@RequestParam(name="id",required=true) String id) {
		WjbdWrjJbxxNew wjbdWrjJbxxNew = wjbdWrjJbxxNewService.getById(id);
		if(wjbdWrjJbxxNew==null) {
			return AjaxResult.error("未找到对应数据");
		}
		return AjaxResult.OK(wjbdWrjJbxxNew);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param wjbdWrjJbxxNew
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WjbdWrjJbxxNew wjbdWrjJbxxNew,String type) {
		if (StringUtils.isNotBlank(type)&&type.equals("template")){
			return	super.exportXlsTemplate(request, wjbdWrjJbxxNew, WjbdWrjJbxxNew.class, "武警部队_无人机_详细信息-导入模板");
		}
        return super.exportXls(request, wjbdWrjJbxxNew, WjbdWrjJbxxNew.class, "武警部队_无人机_详细信息");
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
        return super.importExcel(request, response, WjbdWrjJbxxNew.class);
    }

}
