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
import org.jeecg.modules.wrj.dto.AreaQueryParam;
import org.jeecg.modules.wrj.entity.WjbdWrjTyjhData;
import org.jeecg.modules.wrj.entity.WjbdWrjZymb;
import org.jeecg.modules.wrj.service.IWjbdWrjZymbService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.wrj.vo.TargetData;
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
 * @Description: 武警部队_无人机_重要目标
 * @Author: jeecg-boot
 * @Date:   2025-11-03
 * @Version: V1.0
 */
@Api(tags="武警部队_无人机_重要目标")
@RestController
@RequestMapping("/wrj/wjbdWrjZymb")
@Slf4j
public class WjbdWrjZymbController extends JeecgController<WjbdWrjZymb, IWjbdWrjZymbService> {
	@Autowired
	private IWjbdWrjZymbService wjbdWrjZymbService;
	
	/**
	 * 分页列表查询
	 *
	 * @param wjbdWrjZymb
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_重要目标-分页列表查询",YYMK = "数据管理-重要目标")
	@ApiOperation(value="武警部队_无人机_重要目标-分页列表查询", notes="武警部队_无人机_重要目标-分页列表查询")
	@GetMapping(value = "/list")
	public AjaxResult<?> queryPageList(WjbdWrjZymb wjbdWrjZymb,
											  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
											  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
											  HttpServletRequest req) {
		QueryWrapper<WjbdWrjZymb> queryWrapper = QueryGenerator.initQueryWrapper(wjbdWrjZymb, req.getParameterMap());
		Page<WjbdWrjZymb> page = new Page<WjbdWrjZymb>(pageNo, pageSize);
		IPage<WjbdWrjZymb> pageList = wjbdWrjZymbService.page(page, queryWrapper);
		return AjaxResult.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param wjbdWrjZymb
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_重要目标-添加",YYMK = "数据管理-重要目标")
	@ApiOperation(value="武警部队_无人机_重要目标-添加", notes="武警部队_无人机_重要目标-添加")
	@PostMapping(value = "/add")
	public AjaxResult<?> add(@RequestBody WjbdWrjZymb wjbdWrjZymb) {
		wjbdWrjZymbService.save(wjbdWrjZymb);
		return AjaxResult.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param wjbdWrjZymb
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_重要目标-编辑",YYMK = "数据管理-重要目标")
	@ApiOperation(value="武警部队_无人机_重要目标-编辑", notes="武警部队_无人机_重要目标-编辑")
	@PutMapping(value = "/edit")
	public AjaxResult<?> edit(@RequestBody WjbdWrjZymb wjbdWrjZymb) {
		wjbdWrjZymbService.updateById(wjbdWrjZymb);
		return AjaxResult.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_重要目标-通过id删除",YYMK = "数据管理-重要目标")
	@ApiOperation(value="武警部队_无人机_重要目标-通过id删除", notes="武警部队_无人机_重要目标-通过id删除")
	@DeleteMapping(value = "/delete")
	public AjaxResult<?> delete(@RequestParam(name="id",required=true) String id) {
		wjbdWrjZymbService.removeById(id);
		return AjaxResult.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_重要目标-批量删除",YYMK = "数据管理-重要目标")
	@ApiOperation(value="武警部队_无人机_重要目标-批量删除", notes="武警部队_无人机_重要目标-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public AjaxResult<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.wjbdWrjZymbService.removeByIds(Arrays.asList(ids.split(",")));
		return AjaxResult.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_重要目标-通过id查询",YYMK = "数据管理-重要目标")
	@ApiOperation(value="武警部队_无人机_重要目标-通过id查询", notes="武警部队_无人机_重要目标-通过id查询")
	@GetMapping(value = "/queryById")
	public AjaxResult<?> queryById(@RequestParam(name="id",required=true) String id) {
		WjbdWrjZymb wjbdWrjZymb = wjbdWrjZymbService.getById(id);
		if(wjbdWrjZymb==null) {
			return AjaxResult.error("未找到对应数据");
		}
		return AjaxResult.OK(wjbdWrjZymb);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param wjbdWrjZymb
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WjbdWrjZymb wjbdWrjZymb,String type) {
		if (StringUtils.isNotBlank(type)&&type.equals("template")){
			return	super.exportXlsTemplate(request, wjbdWrjZymb, WjbdWrjZymb.class, "武警部队_无人机_重要目标-导入模板");
		}
        return super.exportXls(request, wjbdWrjZymb, WjbdWrjZymb.class, "武警部队_无人机_重要目标");
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
        return super.importExcel(request, response, WjbdWrjZymb.class);
    }


	 /**
	  * 范围查询
	  *
	  * @param jd
	  * @param wd
	  * @param jl
	  * @return
	  */
	 @AutoLog(value = "武警部队_无人机_重要目标-范围查询",YYMK = "数据管理-重要目标")
	 @ApiOperation(value="武警部队_无人机_重要目标-范围查询", notes="武警部队_无人机_重要目标-范围查询")
	 @GetMapping(value = "/getWrjZymbByJwdAndJl")
	 public AjaxResult<?> getWrjZymbByJwdAndJl(@RequestParam(name="jd") String jd,
										@RequestParam(name="wd") String wd,
										@RequestParam(name="jl") String jl,
											   @RequestParam(name="type") String type
											   ) {
		 List<WjbdWrjZymb> list = wjbdWrjZymbService.getWrjZymbByJwdAndJl(jd,wd ,jl,Arrays.asList(type.split(",")));
		 return AjaxResult.OK(list);
	 }

	 /**
	  * 范围查询
	  *
	  * @param jd
	  * @param wd
	  * @param jl
	  * @return
	  */
	 @AutoLog(value = "武警部队_无人机_重要目标-范围查询",YYMK = "数据管理-重要目标")
	 @ApiOperation(value="武警部队_无人机_重要目标-范围查询", notes="武警部队_无人机_重要目标-范围查询")
	 @GetMapping(value = "/getWrjZymbByJwdAndJlS")
	 public AjaxResult<?> getWrjZymbByJwdAndJlS(@RequestParam(name="jd") String jd,
											   @RequestParam(name="wd") String wd,
											   @RequestParam(name="jl") String jl,
											   @RequestParam(name="type") String type
	 ) {
		 return AjaxResult.OK(wjbdWrjZymbService.getWrjZymbByJwdAndJls(jd,wd ,jl,Arrays.asList(type.split(","))));
	 }

	 /**
	  * 根据正方形区域查询目标数据
	  * @param param 前端传入的四个角点参数（JSON格式）
	  * @return 区域内的目标数据
	  */
	 @PostMapping("/queryBySquareArea")
	 public AjaxResult<?> queryBySquareArea(@RequestBody AreaQueryParam param) {
		 try {
			 List<TargetData> data = wjbdWrjZymbService.queryBySquareArea(param);
			 return AjaxResult.ok(data);
		 } catch (Exception e) {
			 e.printStackTrace();
			 return AjaxResult.error("查询失败：" + e.getMessage());
		 }
	 }

}
