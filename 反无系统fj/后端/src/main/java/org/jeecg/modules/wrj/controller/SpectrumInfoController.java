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

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.wrj.entity.SpectrumInfo;
import org.jeecg.modules.wrj.service.ISpectrumInfoService;

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
 * @Description: 频谱结果数据
 * @Author: jeecg-boot
 * @Date:   2025-12-24
 * @Version: V1.0
 */
@Api(tags="频谱结果数据")
@RestController
@RequestMapping("/wrj/spectrumInfo")
@Slf4j
public class SpectrumInfoController extends JeecgController<SpectrumInfo, ISpectrumInfoService> {
	@Autowired
	private ISpectrumInfoService spectrumInfoService;
	 @Autowired
	 private WebSocket webSocket;
	/**
	 * 分页列表查询
	 *
	 * @param spectrumInfo
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "频谱结果数据-分页列表查询")
	@ApiOperation(value="频谱结果数据-分页列表查询", notes="频谱结果数据-分页列表查询")
	@GetMapping(value = "/list")
	public AjaxResult<?> queryPageList(SpectrumInfo spectrumInfo,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<SpectrumInfo> queryWrapper = QueryGenerator.initQueryWrapper(spectrumInfo, req.getParameterMap());
		Page<SpectrumInfo> page = new Page<SpectrumInfo>(pageNo, pageSize);
		queryWrapper.orderByDesc("create_time");
		IPage<SpectrumInfo> pageList = spectrumInfoService.page(page, queryWrapper);
		return AjaxResult.OK(pageList);
	}


	 /**
	  * 测频数据推送
	  * @return
	  */
	 @AutoLog(value = "频谱结果数据-测频数据推送")
	 @ApiOperation(value="频谱结果数据-测频数据推送", notes="频谱结果数据-测频数据推送")
	 @GetMapping(value = "/SendWebsocketData")
	 public AjaxResult<?> SendWebsocketData() {
		 //实时频谱数据推送数据,前端需要实时数据解析
		 JSONObject jsonObject = (JSONObject) JSONObject.toJSON(spectrumInfoService.list().get(0));
		 String message = jsonObject.toJSONString();
		 JSONObject obj = new JSONObject();
		 obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_SpectrumData);
		 obj.put(WebsocketConst.MSG_ID, "M0001");
		 obj.put(WebsocketConst.MSG_TXT, message);
		 webSocket.sendMessage(obj.toJSONString());
		 return AjaxResult.OK("操作成功!!!");
	 }

	/**
	 *   添加
	 *
	 * @param spectrumInfo
	 * @return
	 */
	@AutoLog(value = "频谱结果数据-添加")
	@ApiOperation(value="频谱结果数据-添加", notes="频谱结果数据-添加")
	@PostMapping(value = "/add")
	public AjaxResult<?> add(@RequestBody SpectrumInfo spectrumInfo) {
		spectrumInfoService.save(spectrumInfo);
		return AjaxResult.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param spectrumInfo
	 * @return
	 */
	@AutoLog(value = "频谱结果数据-编辑")
	@ApiOperation(value="频谱结果数据-编辑", notes="频谱结果数据-编辑")
	@PutMapping(value = "/edit")
	public AjaxResult<?> edit(@RequestBody SpectrumInfo spectrumInfo) {
		spectrumInfoService.updateById(spectrumInfo);
		return AjaxResult.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "频谱结果数据-通过id删除")
	@ApiOperation(value="频谱结果数据-通过id删除", notes="频谱结果数据-通过id删除")
	@DeleteMapping(value = "/delete")
	public AjaxResult<?> delete(@RequestParam(name="id",required=true) String id) {
		spectrumInfoService.removeById(id);
		return AjaxResult.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "频谱结果数据-批量删除")
	@ApiOperation(value="频谱结果数据-批量删除", notes="频谱结果数据-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public AjaxResult<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.spectrumInfoService.removeByIds(Arrays.asList(ids.split(",")));
		return AjaxResult.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "频谱结果数据-通过id查询")
	@ApiOperation(value="频谱结果数据-通过id查询", notes="频谱结果数据-通过id查询")
	@GetMapping(value = "/queryById")
	public AjaxResult<?> queryById(@RequestParam(name="id",required=true) String id) {
		SpectrumInfo spectrumInfo = spectrumInfoService.getById(id);
		if(spectrumInfo==null) {
			return AjaxResult.error("未找到对应数据");
		}
		return AjaxResult.OK(spectrumInfo);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param spectrumInfo
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, SpectrumInfo spectrumInfo) {
        return super.exportXls(request, spectrumInfo, SpectrumInfo.class, "频谱结果数据");
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
        return super.importExcel(request, response, SpectrumInfo.class);
    }

}
