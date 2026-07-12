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
import org.jeecg.modules.wrj.entity.WjbdWrjJbxxNew;
import org.jeecg.modules.wrj.entity.WjbdWrjKy;
import org.jeecg.modules.wrj.service.IWjbdWrjKyService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.wrj.vo.WjbdWrjKysqVo;
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
 * @Description: 武警部队_无人机_空域
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Api(tags="武警部队_无人机_空域")
@RestController
@RequestMapping("/wrj/wjbdWrjKy")
@Slf4j
public class WjbdWrjKyController extends JeecgController<WjbdWrjKy, IWjbdWrjKyService> {
	@Autowired
	private IWjbdWrjKyService wjbdWrjKyService;
	
	/**
	 * 分页列表查询
	 *
	 * @param wjbdWrjKy
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_空域-分页列表查询",YYMK = "数据管理-空域管理")
	@ApiOperation(value="武警部队_无人机_空域-分页列表查询", notes="武警部队_无人机_空域-分页列表查询")
	@GetMapping(value = "/list")
	public AjaxResult<?> queryPageList(WjbdWrjKy wjbdWrjKy,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<WjbdWrjKy> queryWrapper = QueryGenerator.initQueryWrapper(wjbdWrjKy, req.getParameterMap());
		Page<WjbdWrjKy> page = new Page<WjbdWrjKy>(pageNo, pageSize);
		queryWrapper.orderByDesc("czsj");
		IPage<WjbdWrjKy> pageList = wjbdWrjKyService.page(page, queryWrapper);
		return AjaxResult.OK(pageList);
	}

	 /**
	  * 分页列表查询
	  *
	  * @param wjbdWrjKy
	  * @param pageNo
	  * @param pageSize
	  * @param req
	  * @return
	  */
	 @AutoLog(value = "武警部队_无人机_空域-分页列表查询",YYMK = "数据管理-空域管理")
	 @ApiOperation(value="武警部队_无人机_空域-分页列表查询", notes="武警部队_无人机_空域-分页列表查询")
	 @GetMapping(value = "/listAll")
	 public AjaxResult<?> listAll(WjbdWrjKy wjbdWrjKy,
										@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
										@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
										HttpServletRequest req) {
		 Page<WjbdWrjKy> page = new Page<WjbdWrjKy>(pageNo, pageSize);
		 IPage<WjbdWrjKy> pageList = wjbdWrjKyService.listAll(page, wjbdWrjKy);
		 return AjaxResult.OK(pageList);
	 }

	 /**
	  * 分页列表查询
	  *
	  * @param kyid
	  * @param pageNo
	  * @param pageSize
	  * @param req
	  * @return
	  */
	 @AutoLog(value = "武警部队_无人机_空域-分页列表查询",YYMK = "数据管理-空域管理")
	 @ApiOperation(value="武警部队_无人机_空域-分页列表查询", notes="武警部队_无人机_空域-分页列表查询")
	 @GetMapping(value = "/listAllByKyid")
	 public AjaxResult<?> listAllByKyid(String kyid,
								  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								  HttpServletRequest req) {

		 Page<WjbdWrjKysqVo> page = new Page<WjbdWrjKysqVo>(pageNo, pageSize);
		 IPage<WjbdWrjKysqVo> pageList = wjbdWrjKyService.listAllByKyid(page, kyid);
		 return AjaxResult.OK(pageList);
	 }
	
	/**
	 *   添加
	 *
	 * @param wjbdWrjKy
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_空域-添加",YYMK = "数据管理-空域管理")
	@ApiOperation(value="武警部队_无人机_空域-添加", notes="武警部队_无人机_空域-添加")
	@PostMapping(value = "/add")
	public AjaxResult<?> add(@RequestBody WjbdWrjKy wjbdWrjKy) {
		wjbdWrjKyService.save(wjbdWrjKy);
		return AjaxResult.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param wjbdWrjKy
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_空域-编辑",YYMK = "数据管理-空域管理")
	@ApiOperation(value="武警部队_无人机_空域-编辑", notes="武警部队_无人机_空域-编辑")
	@PutMapping(value = "/edit")
	public AjaxResult<?> edit(@RequestBody WjbdWrjKy wjbdWrjKy) {
		wjbdWrjKyService.updateById(wjbdWrjKy);
		return AjaxResult.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_空域-通过id删除",YYMK = "数据管理-空域管理")
	@ApiOperation(value="武警部队_无人机_空域-通过id删除", notes="武警部队_无人机_空域-通过id删除")
	@DeleteMapping(value = "/delete")
	public AjaxResult<?> delete(@RequestParam(name="id",required=true) String id) {
		wjbdWrjKyService.removeById(id);
		return AjaxResult.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_空域-批量删除",YYMK = "数据管理-空域管理")
	@ApiOperation(value="武警部队_无人机_空域-批量删除", notes="武警部队_无人机_空域-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public AjaxResult<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.wjbdWrjKyService.removeByIds(Arrays.asList(ids.split(",")));
		return AjaxResult.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_空域-通过id查询",YYMK = "数据管理-空域管理")
	@ApiOperation(value="武警部队_无人机_空域-通过id查询", notes="武警部队_无人机_空域-通过id查询")
	@GetMapping(value = "/queryById")
	public AjaxResult<?> queryById(@RequestParam(name="id",required=true) String id) {
		WjbdWrjKy wjbdWrjKy = wjbdWrjKyService.getById(id);
		if(wjbdWrjKy==null) {
			return AjaxResult.error("未找到对应数据");
		}
		return AjaxResult.OK(wjbdWrjKy);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param wjbdWrjKy
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WjbdWrjKy wjbdWrjKy,String type) {
		if (StringUtils.isNotBlank(type)&&type.equals("template")){
			return	super.exportXlsTemplate(request, wjbdWrjKy, WjbdWrjKy.class, "武警部队_无人机_空域-导入模板");
		}
        return super.exportXls(request, wjbdWrjKy, WjbdWrjKy.class, "武警部队_无人机_空域");
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
        return super.importExcel(request, response, WjbdWrjKy.class);
    }

}
