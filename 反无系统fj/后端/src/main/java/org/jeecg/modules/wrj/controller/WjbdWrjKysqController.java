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
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.wrj.entity.WjbdWrjJbxxNew;
import org.jeecg.modules.wrj.entity.WjbdWrjKysq;
import org.jeecg.modules.wrj.service.IWjbdWrjKysqService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.wrj.vo.WjbdWrjKysqSaveVo;
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
 * @Description: 武警部队_无人机_空域授权
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Api(tags="武警部队_无人机_空域授权")
@RestController
@RequestMapping("/wrj/wjbdWrjKysq")
@Slf4j
public class WjbdWrjKysqController extends JeecgController<WjbdWrjKysq, IWjbdWrjKysqService> {
	@Autowired
	private IWjbdWrjKysqService wjbdWrjKysqService;
	
	/**
	 * 分页列表查询
	 *
	 * @param wjbdWrjKysq
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_空域授权-分页列表查询")
	@ApiOperation(value="武警部队_无人机_空域授权-分页列表查询", notes="武警部队_无人机_空域授权-分页列表查询")
	@GetMapping(value = "/list")
	public AjaxResult<?> queryPageList(WjbdWrjKysq wjbdWrjKysq,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<WjbdWrjKysq> queryWrapper = QueryGenerator.initQueryWrapper(wjbdWrjKysq, req.getParameterMap());
		Page<WjbdWrjKysq> page = new Page<WjbdWrjKysq>(pageNo, pageSize);
		IPage<WjbdWrjKysq> pageList = wjbdWrjKysqService.page(page, queryWrapper);
		return AjaxResult.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param wjbdWrjKysq
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_空域授权-添加")
	@ApiOperation(value="武警部队_无人机_空域授权-添加", notes="武警部队_无人机_空域授权-添加")
	@PostMapping(value = "/add")
	public AjaxResult<?> add(@RequestBody WjbdWrjKysq wjbdWrjKysq) {
		wjbdWrjKysqService.save(wjbdWrjKysq);
		return AjaxResult.OK("添加成功！");
	}


	 /**
	  *   添加
	  *
	  * @param wjbdWrjKysqSaveVo
	  * @return
	  */
	 @AutoLog(value = "武警部队_无人机_空域授权-设备授权")
	 @ApiOperation(value="武警部队_无人机_空域授权-设备授权", notes="武警部队_无人机_空域授权-设备授权")
	 @PostMapping(value = "/saveKysq")
	 public AjaxResult<?> saveKysq(@RequestBody WjbdWrjKysqSaveVo wjbdWrjKysqSaveVo) {
		 if(!wjbdWrjKysqSaveVo.getWrjids().isEmpty()){
			 List<WjbdWrjKysq> wjbdWrjKysqList = new ArrayList<>();
			 for (String wrjid:wjbdWrjKysqSaveVo.getWrjids()){
				 WjbdWrjKysq wjbdWrjKysq = new WjbdWrjKysq();
				 wjbdWrjKysq.setKyid(wjbdWrjKysqSaveVo.getKyid());
				 wjbdWrjKysq.setWrjid(wrjid);
				 wjbdWrjKysqList.add(wjbdWrjKysq);
			 }
			 wjbdWrjKysqService.saveBatch(wjbdWrjKysqList);
		 }
		 return AjaxResult.OK("授权成功");
	 }
	/**
	 *  编辑
	 *
	 * @param wjbdWrjKysq
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_空域授权-编辑")
	@ApiOperation(value="武警部队_无人机_空域授权-编辑", notes="武警部队_无人机_空域授权-编辑")
	@PutMapping(value = "/edit")
	public AjaxResult<?> edit(@RequestBody WjbdWrjKysq wjbdWrjKysq) {
		wjbdWrjKysqService.updateById(wjbdWrjKysq);
		return AjaxResult.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_空域授权-通过id删除")
	@ApiOperation(value="武警部队_无人机_空域授权-通过id删除", notes="武警部队_无人机_空域授权-通过id删除")
	@DeleteMapping(value = "/delete")
	public AjaxResult<?> delete(@RequestParam(name="id",required=true) String id) {
		wjbdWrjKysqService.removeById(id);
		return AjaxResult.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_空域授权-批量删除")
	@ApiOperation(value="武警部队_无人机_空域授权-批量删除", notes="武警部队_无人机_空域授权-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public AjaxResult<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.wjbdWrjKysqService.removeByIds(Arrays.asList(ids.split(",")));
		return AjaxResult.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_空域授权-通过id查询")
	@ApiOperation(value="武警部队_无人机_空域授权-通过id查询", notes="武警部队_无人机_空域授权-通过id查询")
	@GetMapping(value = "/queryById")
	public AjaxResult<?> queryById(@RequestParam(name="id",required=true) String id) {
		WjbdWrjKysq wjbdWrjKysq = wjbdWrjKysqService.getById(id);
		if(wjbdWrjKysq==null) {
			return AjaxResult.error("未找到对应数据");
		}
		return AjaxResult.OK(wjbdWrjKysq);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param wjbdWrjKysq
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WjbdWrjKysq wjbdWrjKysq,String type) {
		if (StringUtils.isNotBlank(type)&&type.equals("template")){
			return	super.exportXlsTemplate(request, wjbdWrjKysq, WjbdWrjKysq.class, "武警部队_无人机_空域授权-导入模板");
		}
        return super.exportXls(request, wjbdWrjKysq, WjbdWrjKysq.class, "武警部队_无人机_空域授权");
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
        return super.importExcel(request, response, WjbdWrjKysq.class);
    }

}
