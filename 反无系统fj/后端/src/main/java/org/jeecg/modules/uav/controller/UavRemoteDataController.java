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
import org.jeecg.modules.uav.entity.UavRemoteData;
import org.jeecg.modules.uav.service.IUavRemoteDataService;

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
 * @Description: uav_remote_data
 * @Author: jeecg-boot
 * @Date:   2025-11-05
 * @Version: V1.0
 */
@Api(tags="uav_remote_data")
@RestController
@RequestMapping("/uav/uavRemoteData")
@Slf4j
public class UavRemoteDataController extends JeecgController<UavRemoteData, IUavRemoteDataService> {
	@Autowired
	private IUavRemoteDataService uavRemoteDataService;
	
	/**
	 * 分页列表查询
	 *
	 * @param uavRemoteData
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "uav_remote_data-分页列表查询")
	@ApiOperation(value="uav_remote_data-分页列表查询", notes="uav_remote_data-分页列表查询")
	@GetMapping(value = "/list")
	public AjaxResult<?> queryPageList(UavRemoteData uavRemoteData,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<UavRemoteData> queryWrapper = QueryGenerator.initQueryWrapper(uavRemoteData, req.getParameterMap());
		Page<UavRemoteData> page = new Page<UavRemoteData>(pageNo, pageSize);
		IPage<UavRemoteData> pageList = uavRemoteDataService.page(page, queryWrapper);
		return AjaxResult.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param uavRemoteData
	 * @return
	 */
	@AutoLog(value = "uav_remote_data-添加")
	@ApiOperation(value="uav_remote_data-添加", notes="uav_remote_data-添加")
	@PostMapping(value = "/add")
	public AjaxResult<?> add(@RequestBody UavRemoteData uavRemoteData) {
		uavRemoteDataService.save(uavRemoteData);
		return AjaxResult.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param uavRemoteData
	 * @return
	 */
	@AutoLog(value = "uav_remote_data-编辑")
	@ApiOperation(value="uav_remote_data-编辑", notes="uav_remote_data-编辑")
	@PutMapping(value = "/edit")
	public AjaxResult<?> edit(@RequestBody UavRemoteData uavRemoteData) {
		uavRemoteDataService.updateById(uavRemoteData);
		return AjaxResult.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "uav_remote_data-通过id删除")
	@ApiOperation(value="uav_remote_data-通过id删除", notes="uav_remote_data-通过id删除")
	@DeleteMapping(value = "/delete")
	public AjaxResult<?> delete(@RequestParam(name="id",required=true) String id) {
		uavRemoteDataService.removeById(id);
		return AjaxResult.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "uav_remote_data-批量删除")
	@ApiOperation(value="uav_remote_data-批量删除", notes="uav_remote_data-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public AjaxResult<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.uavRemoteDataService.removeByIds(Arrays.asList(ids.split(",")));
		return AjaxResult.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "uav_remote_data-通过id查询")
	@ApiOperation(value="uav_remote_data-通过id查询", notes="uav_remote_data-通过id查询")
	@GetMapping(value = "/queryById")
	public AjaxResult<?> queryById(@RequestParam(name="id",required=true) String id) {
		UavRemoteData uavRemoteData = uavRemoteDataService.getById(id);
		if(uavRemoteData==null) {
			return AjaxResult.error("未找到对应数据");
		}
		return AjaxResult.OK(uavRemoteData);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param uavRemoteData
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, UavRemoteData uavRemoteData) {
        return super.exportXls(request, uavRemoteData, UavRemoteData.class, "uav_remote_data");
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
        return super.importExcel(request, response, UavRemoteData.class);
    }

}
