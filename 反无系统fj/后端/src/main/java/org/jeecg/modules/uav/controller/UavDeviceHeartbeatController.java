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
import org.jeecg.modules.uav.entity.UavDeviceHeartbeat;
import org.jeecg.modules.uav.service.IUavDeviceHeartbeatService;

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
 * @Description: 设备心跳管理
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Api(tags="设备心跳管理")
@RestController
@RequestMapping("/uav/uavDeviceHeartbeat")
@Slf4j
public class UavDeviceHeartbeatController extends JeecgController<UavDeviceHeartbeat, IUavDeviceHeartbeatService> {
	@Autowired
	private IUavDeviceHeartbeatService uavDeviceHeartbeatService;
	
	/**
	 * 分页列表查询
	 *
	 * @param uavDeviceHeartbeat
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "设备心跳管理-分页列表查询")
	@ApiOperation(value="设备心跳管理-分页列表查询", notes="设备心跳管理-分页列表查询")
	@GetMapping(value = "/list")
	public AjaxResult<?> queryPageList(UavDeviceHeartbeat uavDeviceHeartbeat,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<UavDeviceHeartbeat> queryWrapper = QueryGenerator.initQueryWrapper(uavDeviceHeartbeat, req.getParameterMap());
		Page<UavDeviceHeartbeat> page = new Page<UavDeviceHeartbeat>(pageNo, pageSize);
		IPage<UavDeviceHeartbeat> pageList = uavDeviceHeartbeatService.page(page, queryWrapper);
		return AjaxResult.OK(pageList);
	}
	 /**
	  * 根据站id查询设备信息
	  * @param stationId
	  * @return
	  */
	 @AutoLog(value = "设备心跳管理-分页列表查询")
	 @ApiOperation(value="设备心跳管理-分页列表查询", notes="设备心跳管理-分页列表查询")
	 @GetMapping(value = "/selectLatestHeartbeat")
	 public AjaxResult<?> listOne(@RequestParam(name="stationId") Integer stationId) {
		 UavDeviceHeartbeat uavDeviceHeartbeat= uavDeviceHeartbeatService.selectLatestHeartbeat(stationId);
		 if (uavDeviceHeartbeat!=null){
			 return AjaxResult.OK(uavDeviceHeartbeat );
		 }
		 return AjaxResult.OK(new UavDeviceHeartbeat());
	 }
	/**
	 *   添加
	 *
	 * @param uavDeviceHeartbeat
	 * @return
	 */
	@AutoLog(value = "设备心跳管理-添加")
	@ApiOperation(value="设备心跳管理-添加", notes="设备心跳管理-添加")
	@PostMapping(value = "/add")
	public AjaxResult<?> add(@RequestBody UavDeviceHeartbeat uavDeviceHeartbeat) {
		uavDeviceHeartbeatService.save(uavDeviceHeartbeat);
		return AjaxResult.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param uavDeviceHeartbeat
	 * @return
	 */
	@AutoLog(value = "设备心跳管理-编辑")
	@ApiOperation(value="设备心跳管理-编辑", notes="设备心跳管理-编辑")
	@PutMapping(value = "/edit")
	public AjaxResult<?> edit(@RequestBody UavDeviceHeartbeat uavDeviceHeartbeat) {
		uavDeviceHeartbeatService.updateById(uavDeviceHeartbeat);
		return AjaxResult.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "设备心跳管理-通过id删除")
	@ApiOperation(value="设备心跳管理-通过id删除", notes="设备心跳管理-通过id删除")
	@DeleteMapping(value = "/delete")
	public AjaxResult<?> delete(@RequestParam(name="id",required=true) String id) {
		uavDeviceHeartbeatService.removeById(id);
		return AjaxResult.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "设备心跳管理-批量删除")
	@ApiOperation(value="设备心跳管理-批量删除", notes="设备心跳管理-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public AjaxResult<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.uavDeviceHeartbeatService.removeByIds(Arrays.asList(ids.split(",")));
		return AjaxResult.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "设备心跳管理-通过id查询")
	@ApiOperation(value="设备心跳管理-通过id查询", notes="设备心跳管理-通过id查询")
	@GetMapping(value = "/queryById")
	public AjaxResult<?> queryById(@RequestParam(name="id",required=true) String id) {
		UavDeviceHeartbeat uavDeviceHeartbeat = uavDeviceHeartbeatService.getById(id);
		if(uavDeviceHeartbeat==null) {
			return AjaxResult.error("未找到对应数据");
		}
		return AjaxResult.OK(uavDeviceHeartbeat);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param uavDeviceHeartbeat
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, UavDeviceHeartbeat uavDeviceHeartbeat) {
        return super.exportXls(request, uavDeviceHeartbeat, UavDeviceHeartbeat.class, "设备心跳管理");
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
        return super.importExcel(request, response, UavDeviceHeartbeat.class);
    }

}
