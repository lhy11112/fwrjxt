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
import org.jeecg.modules.wrj.entity.WjbdDxyyBh;
import org.jeecg.modules.wrj.entity.dto.WjbdDxyyBhDto;
import org.jeecg.modules.wrj.service.IWjbdDxyyBhService;

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
 * @Description: 武警不对_典型应用_无人机
 * @Author: jeecg-boot
 * @Date:   2026-01-19
 * @Version: V1.0
 */
@Api(tags="武警不对_典型应用_无人机")
@RestController
@RequestMapping("/wrj/wjbdDxyyBh")
@Slf4j
public class WjbdDxyyBhController extends JeecgController<WjbdDxyyBh, IWjbdDxyyBhService> {
	@Autowired
	private IWjbdDxyyBhService wjbdDxyyBhService;
	
	/**
	 * 分页列表查询
	 *
	 * @param wjbdDxyyBh
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "武警不对_典型应用_无人机-分页列表查询")
	@ApiOperation(value="武警不对_典型应用_无人机-分页列表查询", notes="武警不对_典型应用_无人机-分页列表查询")
	@GetMapping(value = "/list")
	public AjaxResult<?> queryPageList(WjbdDxyyBh wjbdDxyyBh,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<WjbdDxyyBh> queryWrapper = QueryGenerator.initQueryWrapper(wjbdDxyyBh, req.getParameterMap());
		Page<WjbdDxyyBh> page = new Page<WjbdDxyyBh>(pageNo, pageSize);
		IPage<WjbdDxyyBh> pageList = wjbdDxyyBhService.page(page, queryWrapper);
		return AjaxResult.OK(pageList);
	}
	 /**
	  * 根据业务id、时间查询标绘信息
	  *
	  */
	 @AutoLog(value = "武警不对_典型应用_无人机-根据业务id、时间查询标绘信息")
	 @ApiOperation(value="武警不对_典型应用_无人机-根据业务id、时间查询标绘信息", notes="武警不对_典型应用_无人机-根据业务id、时间查询标绘信息")
	 @PostMapping(value = "/getBhByYwIdDataTime")
	 public AjaxResult<?> getBhByYwIdDataTime(@RequestBody WjbdDxyyBhDto wjbdDxyyBhDto) {
		 return AjaxResult.OK(wjbdDxyyBhService.getBhByYwIdDataTime(wjbdDxyyBhDto));
	 }
	/**
	 *   添加
	 *
	 * @param wjbdDxyyBh
	 * @return
	 */
	@AutoLog(value = "武警不对_典型应用_无人机-添加")
	@ApiOperation(value="武警不对_典型应用_无人机-添加", notes="武警不对_典型应用_无人机-添加")
	@PostMapping(value = "/add")
	public AjaxResult<?> add(@RequestBody WjbdDxyyBh wjbdDxyyBh) {
		wjbdDxyyBhService.save(wjbdDxyyBh);
		return AjaxResult.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param wjbdDxyyBh
	 * @return
	 */
	@AutoLog(value = "武警不对_典型应用_无人机-编辑")
	@ApiOperation(value="武警不对_典型应用_无人机-编辑", notes="武警不对_典型应用_无人机-编辑")
	@PutMapping(value = "/edit")
	public AjaxResult<?> edit(@RequestBody WjbdDxyyBh wjbdDxyyBh) {
		wjbdDxyyBhService.updateById(wjbdDxyyBh);
		return AjaxResult.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警不对_典型应用_无人机-通过id删除")
	@ApiOperation(value="武警不对_典型应用_无人机-通过id删除", notes="武警不对_典型应用_无人机-通过id删除")
	@DeleteMapping(value = "/delete")
	public AjaxResult<?> delete(@RequestParam(name="id",required=true) String id) {
		wjbdDxyyBhService.removeById(id);
		return AjaxResult.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "武警不对_典型应用_无人机-批量删除")
	@ApiOperation(value="武警不对_典型应用_无人机-批量删除", notes="武警不对_典型应用_无人机-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public AjaxResult<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.wjbdDxyyBhService.removeByIds(Arrays.asList(ids.split(",")));
		return AjaxResult.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警不对_典型应用_无人机-通过id查询")
	@ApiOperation(value="武警不对_典型应用_无人机-通过id查询", notes="武警不对_典型应用_无人机-通过id查询")
	@GetMapping(value = "/queryById")
	public AjaxResult<?> queryById(@RequestParam(name="id",required=true) String id) {
		WjbdDxyyBh wjbdDxyyBh = wjbdDxyyBhService.getById(id);
		if(wjbdDxyyBh==null) {
			return AjaxResult.error("未找到对应数据");
		}
		return AjaxResult.OK(wjbdDxyyBh);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param wjbdDxyyBh
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WjbdDxyyBh wjbdDxyyBh) {
        return super.exportXls(request, wjbdDxyyBh, WjbdDxyyBh.class, "武警不对_典型应用_无人机");
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
        return super.importExcel(request, response, WjbdDxyyBh.class);
    }

}
