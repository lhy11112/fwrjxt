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
import org.jeecg.modules.uav.entity.UavDetectMsg;
import org.jeecg.modules.uav.service.IUavDetectMsgService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.uav.util.UavDatectMsgDto;
import org.jeecg.modules.uav.util.UavDatectMsgDto1;
import org.jeecg.modules.uav.vo.UavDetectMsgVo;
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
 * @Description: 侦测报文数据表-飞行数据
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Api(tags="侦测报文数据表-飞行数据")
@RestController
@RequestMapping("/uav/uavDetectMsg")
@Slf4j
public class UavDetectMsgController extends JeecgController<UavDetectMsg, IUavDetectMsgService> {
	@Autowired
	private IUavDetectMsgService uavDetectMsgService;
	
	/**
	 * 分页列表查询
	 *
	 * @param uavDetectMsg
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "侦测报文数据表-飞行数据-分页列表查询",YYMK = "指挥控制-侦测预警-侦测无人机信息")
	@ApiOperation(value="侦测报文数据表-飞行数据-分页列表查询", notes="侦测报文数据表-飞行数据-分页列表查询")
	@GetMapping(value = "/list")
	public AjaxResult<?> queryPageList(UavDetectMsg uavDetectMsg,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<UavDetectMsg> queryWrapper = QueryGenerator.initQueryWrapper(uavDetectMsg, req.getParameterMap());
		Page<UavDetectMsg> page = new Page<UavDetectMsg>(pageNo, pageSize);
		IPage<UavDetectMsg> pageList = uavDetectMsgService.page(page, queryWrapper);
		return AjaxResult.OK(pageList);
	}

	 /**
	  * 根据年度月份查询日历数据
	  *
	  * @return
	  */
	 @AutoLog(value = "根据年度月份查询日历数据",YYMK = "指挥控制-侦测预警-侦测无人机信息")
	 @ApiOperation(value="根据年度月份查询日历数据", notes="根据年度月份查询日历数据")
	 @GetMapping(value = "/getUavDetectMsgDateByNfYf")
	 public AjaxResult<?> getUavDetectMsgDateByNfYf(@RequestParam(name="nf",required=true)String nf, String yf) {
		 return AjaxResult.OK(uavDetectMsgService.getUavDetectMsgDateByNfYf(nf,yf));
	 }

	 /**
	  * 根据站id查询该设备下侦测到的无人机分组情况
	  *
	  * @return
	  */
	 @AutoLog(value = "根据站id查询该设备下侦测到的无人机分组情况",YYMK = "指挥控制-侦测预警-侦测无人机信息")
	 @ApiOperation(value="根据站id查询该设备下侦测到的无人机分组情况", notes="根据站id查询该设备下侦测到的无人机分组情况")
	 @GetMapping(value = "/getUavDetectMsgByStationId")
	 public AjaxResult<?> getUavDetectMsgByStationId(String StationId, String rq,String authStatus) {
		 return AjaxResult.OK(uavDetectMsgService.getUavDetectMsgByStationId(StationId,rq,authStatus));
	 }
	 /**
	  * 根据型号、序列号、日期查询侦测到无人机详细数据
	  *
	  * @return
	  */
	 @AutoLog(value = "根据型号、序列号、日期查询侦测到无人机详细数据",YYMK = "指挥控制-侦测预警-侦测无人机信息")
	 @ApiOperation(value="根据型号、序列号、日期查询侦测到无人机详细数据", notes="根据型号、序列号、日期查询侦测到无人机详细数据")
	 @GetMapping(value = "/getUavDetectMsgByModelSerialRq")
	 public AjaxResult<?> getUavDetectMsgByModelSerialRq(@RequestParam(name="model",required=true)String model,@RequestParam(name="serial",required=true) String serial, String rq) {
		 return AjaxResult.OK(uavDetectMsgService.getUavDetectMsgByModelSerialRq(model, serial, rq));
	 }


	 /**
	  * 根据型号、序列号、日期查询侦测到无人机详细数据
	  *
	  * @return
	  */
	 @AutoLog(value = "根据型号、序列号、日期查询侦测到无人机详细数据",YYMK = "指挥控制-侦测预警-侦测无人机信息")
	 @ApiOperation(value="根据型号、序列号、日期查询侦测到无人机详细数据", notes="根据型号、序列号、日期查询侦测到无人机详细数据")
	 @PostMapping(value = "/generateFlightRoute")
	 public AjaxResult<?> generateFlightRoute(@RequestBody UavDatectMsgDto1 uavDatectMsgDto) {
		 return AjaxResult.OK(uavDetectMsgService.generateFlightRoute(uavDatectMsgDto));
	 }

	/**
	 *   添加
	 *
	 * @param uavDetectMsg
	 * @return
	 */
	@AutoLog(value = "侦测报文数据表-飞行数据-添加",YYMK = "指挥控制-侦测预警-侦测无人机信息")
	@ApiOperation(value="侦测报文数据表-飞行数据-添加", notes="侦测报文数据表-飞行数据-添加")
	@PostMapping(value = "/add")
	public AjaxResult<?> add(@RequestBody UavDetectMsg uavDetectMsg) {
		uavDetectMsgService.save(uavDetectMsg);
		return AjaxResult.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param uavDetectMsg
	 * @return
	 */
	@AutoLog(value = "侦测报文数据表-飞行数据-编辑",YYMK = "指挥控制-侦测预警-侦测无人机信息")
	@ApiOperation(value="侦测报文数据表-飞行数据-编辑", notes="侦测报文数据表-飞行数据-编辑")
	@PutMapping(value = "/edit")
	public AjaxResult<?> edit(@RequestBody UavDetectMsg uavDetectMsg) {
		uavDetectMsgService.updateById(uavDetectMsg);
		return AjaxResult.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "侦测报文数据表-飞行数据-通过id删除",YYMK = "指挥控制-侦测预警-侦测无人机信息")
	@ApiOperation(value="侦测报文数据表-飞行数据-通过id删除", notes="侦测报文数据表-飞行数据-通过id删除")
	@DeleteMapping(value = "/delete")
	public AjaxResult<?> delete(@RequestParam(name="id",required=true) String id) {
		uavDetectMsgService.removeById(id);
		return AjaxResult.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "侦测报文数据表-飞行数据-批量删除",YYMK = "指挥控制-侦测预警-侦测无人机信息")
	@ApiOperation(value="侦测报文数据表-飞行数据-批量删除", notes="侦测报文数据表-飞行数据-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public AjaxResult<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.uavDetectMsgService.removeByIds(Arrays.asList(ids.split(",")));
		return AjaxResult.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "侦测报文数据表-飞行数据-通过id查询",YYMK = "指挥控制-侦测预警-侦测无人机信息")
	@ApiOperation(value="侦测报文数据表-飞行数据-通过id查询", notes="侦测报文数据表-飞行数据-通过id查询")
	@GetMapping(value = "/queryById")
	public AjaxResult<?> queryById(@RequestParam(name="id",required=true) String id) {
		UavDetectMsg uavDetectMsg = uavDetectMsgService.getById(id);
		if(uavDetectMsg==null) {
			return AjaxResult.error("未找到对应数据");
		}
		return AjaxResult.OK(uavDetectMsg);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param uavDetectMsg
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, UavDetectMsg uavDetectMsg) {
        return super.exportXls(request, uavDetectMsg, UavDetectMsg.class, "侦测报文数据表-飞行数据");
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
        return super.importExcel(request, response, UavDetectMsg.class);
    }

	 /**
	  * 侦测报文数据表-飞行数据-无人机飞行次数统计
	  * @param type
	  * @param stationId
	  * @param nf
	  * @param yf
	  * @return
	  */
	 @AutoLog(value = "侦测报文数据表-飞行数据-无人机飞行次数统计",YYMK = "综合态势-告警情况统计")
	 @ApiOperation(value="侦测报文数据表-飞行数据-无人机飞行次数统计", notes="侦测报文数据表-飞行数据-无人机飞行次数统计")
	 @GetMapping(value = "/wrjfxcstj")
	 public AjaxResult<?> wrjfxcstj(@RequestParam(name="type",required = false) String type,
									@RequestParam(name="stationId",required = false) String stationId,
									@RequestParam(name="nf",required = false) String nf,
									@RequestParam(name="yf",required = false) String yf) {
		 List<Map<String,Object>> list = uavDetectMsgService.wrjfxcstj(type,stationId, nf,yf);
		 return AjaxResult.OK(list);
	 }


	 /**
	  * 侦测报文数据表-飞行数据-分页列表查询
	  * @param type
	  * @param stationId
	  * @param nf
	  * @param yf
	  * @param brand
	  * @param model
	  * @param pageNo
	  * @param pageSize
	  * @param req
	  * @return
	  */
	 @AutoLog(value = "侦测报文数据表-飞行数据-分页列表查询",YYMK = "综合态势-告警情况统计")
	 @ApiOperation(value="侦测报文数据表-飞行数据-分页列表查询", notes="侦测报文数据表-飞行数据-分页列表查询")
	 @GetMapping(value = "/pageList")
	 public AjaxResult<?> pageList(@RequestParam(name="type",required = false) String type,
								   @RequestParam(name="stationId",required = false) String stationId,
								   @RequestParam(name="nf",required = false) String nf,
								   @RequestParam(name="yf",required = false) String yf,
								   @RequestParam(name="brand",required = false) String brand,
								   @RequestParam(name="model",required = false) String model,
								   @RequestParam(name="serial",required = false) String serial,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		 Page<UavDetectMsg> page = new Page<UavDetectMsg>(pageNo, pageSize);
		 IPage<UavDetectMsg> pageList = uavDetectMsgService.pageList(page, type,stationId,nf,yf,brand,model,serial);
		 return AjaxResult.OK(pageList);
	 }
}
