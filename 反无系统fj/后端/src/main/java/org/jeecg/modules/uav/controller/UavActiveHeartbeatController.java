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
import org.jeecg.modules.uav.entity.UavActiveHeartbeat;
import org.jeecg.modules.uav.service.IUavActiveHeartbeatService;

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
 * @Description: 无人机主动心跳数据表
 * @Author: jeecg-boot
 * @Date:   2026-02-27
 * @Version: V1.0
 */
@Api(tags="无人机主动心跳数据表")
@RestController
@RequestMapping("/uav/uavActiveHeartbeat")
@Slf4j
public class UavActiveHeartbeatController extends JeecgController<UavActiveHeartbeat, IUavActiveHeartbeatService> {
	@Autowired
	private IUavActiveHeartbeatService uavActiveHeartbeatService;
	
	/**
	 * 分页列表查询
	 *
	 * @param uavActiveHeartbeat
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "无人机主动心跳数据表-分页列表查询")
	@ApiOperation(value="无人机主动心跳数据表-分页列表查询", notes="无人机主动心跳数据表-分页列表查询")
	@GetMapping(value = "/list")
	public AjaxResult<?> queryPageList(UavActiveHeartbeat uavActiveHeartbeat,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<UavActiveHeartbeat> queryWrapper = QueryGenerator.initQueryWrapper(uavActiveHeartbeat, req.getParameterMap());
		Page<UavActiveHeartbeat> page = new Page<UavActiveHeartbeat>(pageNo, pageSize);
		IPage<UavActiveHeartbeat> pageList = uavActiveHeartbeatService.page(page, queryWrapper);
		return AjaxResult.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param uavActiveHeartbeat
	 * @return
	 */
	@AutoLog(value = "无人机主动心跳数据表-添加")
	@ApiOperation(value="无人机主动心跳数据表-添加", notes="无人机主动心跳数据表-添加")
	@PostMapping(value = "/add")
	public AjaxResult<?> add(@RequestBody UavActiveHeartbeat uavActiveHeartbeat) {
		uavActiveHeartbeatService.save(uavActiveHeartbeat);
		return AjaxResult.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param uavActiveHeartbeat
	 * @return
	 */
	@AutoLog(value = "无人机主动心跳数据表-编辑")
	@ApiOperation(value="无人机主动心跳数据表-编辑", notes="无人机主动心跳数据表-编辑")
	@PutMapping(value = "/edit")
	public AjaxResult<?> edit(@RequestBody UavActiveHeartbeat uavActiveHeartbeat) {
		uavActiveHeartbeatService.updateById(uavActiveHeartbeat);
		return AjaxResult.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "无人机主动心跳数据表-通过id删除")
	@ApiOperation(value="无人机主动心跳数据表-通过id删除", notes="无人机主动心跳数据表-通过id删除")
	@DeleteMapping(value = "/delete")
	public AjaxResult<?> delete(@RequestParam(name="id",required=true) String id) {
		uavActiveHeartbeatService.removeById(id);
		return AjaxResult.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "无人机主动心跳数据表-批量删除")
	@ApiOperation(value="无人机主动心跳数据表-批量删除", notes="无人机主动心跳数据表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public AjaxResult<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.uavActiveHeartbeatService.removeByIds(Arrays.asList(ids.split(",")));
		return AjaxResult.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "无人机主动心跳数据表-通过id查询")
	@ApiOperation(value="无人机主动心跳数据表-通过id查询", notes="无人机主动心跳数据表-通过id查询")
	@GetMapping(value = "/queryById")
	public AjaxResult<?> queryById(@RequestParam(name="id",required=true) String id) {
		UavActiveHeartbeat uavActiveHeartbeat = uavActiveHeartbeatService.getById(id);
		if(uavActiveHeartbeat==null) {
			return AjaxResult.error("未找到对应数据");
		}
		return AjaxResult.OK(uavActiveHeartbeat);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param uavActiveHeartbeat
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, UavActiveHeartbeat uavActiveHeartbeat) {
        return super.exportXls(request, uavActiveHeartbeat, UavActiveHeartbeat.class, "无人机主动心跳数据表");
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
        return super.importExcel(request, response, UavActiveHeartbeat.class);
    }

}
