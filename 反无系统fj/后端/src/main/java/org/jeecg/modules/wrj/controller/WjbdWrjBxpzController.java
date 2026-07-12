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
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.wrj.entity.WjbdWrjBxpz;
import org.jeecg.modules.wrj.service.IWjbdWrjBxpzService;

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
 * @Description: 武警部队_无人机_编携配装
 * @Author: jeecg-boot
 * @Date:   2025-12-16
 * @Version: V1.0
 */
@Api(tags="武警部队_无人机_编携配装")
@RestController
@RequestMapping("/wrj/wjbdWrjBxpz")
@Slf4j
public class WjbdWrjBxpzController extends JeecgController<WjbdWrjBxpz, IWjbdWrjBxpzService> {
	@Autowired
	private IWjbdWrjBxpzService wjbdWrjBxpzService;
	
	/**
	 * 分页列表查询
	 *
	 * @param wjbdWrjBxpz
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_编携配装-分页列表查询")
	@ApiOperation(value="武警部队_无人机_编携配装-分页列表查询", notes="武警部队_无人机_编携配装-分页列表查询")
	@GetMapping(value = "/list")
	public AjaxResult<?> queryPageList(WjbdWrjBxpz wjbdWrjBxpz,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<WjbdWrjBxpz> queryWrapper = QueryGenerator.initQueryWrapper(wjbdWrjBxpz, req.getParameterMap());
		Page<WjbdWrjBxpz> page = new Page<WjbdWrjBxpz>(pageNo, pageSize);
		IPage<WjbdWrjBxpz> pageList = wjbdWrjBxpzService.page(page, queryWrapper);
		return AjaxResult.OK(pageList);
	}

	 /**
	  * 分页列表查询
	  *
	  * @return
	  */
	 @AutoLog(value = "武警部队_无人机_编携配装-分页列表查询")
	 @ApiOperation(value="武警部队_无人机_编携配装-分页列表查询", notes="武警部队_无人机_编携配装-分页列表查询")
	 @GetMapping(value = "/listTree")
	 public AjaxResult<?> listTree() {
		 return AjaxResult.OK(wjbdWrjBxpzService.getTreeByAssembly("0"));
	 }
	
	/**
	 *   添加
	 *
	 * @param wjbdWrjBxpz
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_编携配装-添加")
	@ApiOperation(value="武警部队_无人机_编携配装-添加", notes="武警部队_无人机_编携配装-添加")
	@PostMapping(value = "/add")
	public AjaxResult<?> add(@RequestBody WjbdWrjBxpz wjbdWrjBxpz) {
		wjbdWrjBxpzService.save(wjbdWrjBxpz);
		return AjaxResult.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param wjbdWrjBxpz
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_编携配装-编辑")
	@ApiOperation(value="武警部队_无人机_编携配装-编辑", notes="武警部队_无人机_编携配装-编辑")
	@PutMapping(value = "/edit")
	public AjaxResult<?> edit(@RequestBody WjbdWrjBxpz wjbdWrjBxpz) {
		wjbdWrjBxpzService.updateById(wjbdWrjBxpz);
		return AjaxResult.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_编携配装-通过id删除")
	@ApiOperation(value="武警部队_无人机_编携配装-通过id删除", notes="武警部队_无人机_编携配装-通过id删除")
	@DeleteMapping(value = "/delete")
	public AjaxResult<?> delete(@RequestParam(name="id",required=true) String id) {
		wjbdWrjBxpzService.removeById(id);
		return AjaxResult.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_编携配装-批量删除")
	@ApiOperation(value="武警部队_无人机_编携配装-批量删除", notes="武警部队_无人机_编携配装-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public AjaxResult<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.wjbdWrjBxpzService.removeByIds(Arrays.asList(ids.split(",")));
		return AjaxResult.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_编携配装-通过id查询")
	@ApiOperation(value="武警部队_无人机_编携配装-通过id查询", notes="武警部队_无人机_编携配装-通过id查询")
	@GetMapping(value = "/queryById")
	public AjaxResult<?> queryById(@RequestParam(name="id",required=true) String id) {
		WjbdWrjBxpz wjbdWrjBxpz = wjbdWrjBxpzService.getById(id);
		if(wjbdWrjBxpz==null) {
			return AjaxResult.error("未找到对应数据");
		}
		return AjaxResult.OK(wjbdWrjBxpz);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param wjbdWrjBxpz
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WjbdWrjBxpz wjbdWrjBxpz) {
        return super.exportXls(request, wjbdWrjBxpz, WjbdWrjBxpz.class, "武警部队_无人机_编携配装");
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
        return super.importExcel(request, response, WjbdWrjBxpz.class);
    }

	 /**
	  * 接口2：查询嵌套树形数据（直接渲染，兼容所有MySQL版本）
	  * @param rootId 根节点ID（可选，不传则查所有根节点）
	  */
	 @GetMapping("/tree/nested")
	 public AjaxResult<?> getNestedTree(@RequestParam(required = false) String rootId) {
		 return AjaxResult.OK(wjbdWrjBxpzService.getTreeByAssembly(rootId));
	 }
}
