package org.jeecg.modules.wrj.controller;

import java.util.Arrays;
import java.util.HashMap;
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
import org.jeecg.modules.wrj.entity.WjbdWrjHbmdsq;
import org.jeecg.modules.wrj.entity.WjbdWrjJbxx;
import org.jeecg.modules.wrj.service.IWjbdWrjHbmdsqService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.wrj.vo.UpdateSqxxVo;
import org.jeecg.modules.wrj.vo.WjbdWrjJbxxVo;
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
 * @Description: 武警部队_无人机_黑白名单授权
 * @Author: jeecg-boot
 * @Date:   2025-10-27
 * @Version: V1.0
 */
@Api(tags="武警部队_无人机_黑白名单授权")
@RestController
@RequestMapping("/wrj/wjbdWrjHbmdsq")
@Slf4j
public class WjbdWrjHbmdsqController extends JeecgController<WjbdWrjHbmdsq, IWjbdWrjHbmdsqService> {
	@Autowired
	private IWjbdWrjHbmdsqService wjbdWrjHbmdsqService;
	
	/**
	 * 分页列表查询
	 *
	 * @param wjbdWrjHbmdsq
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_黑白名单授权-分页列表查询",YYMK = "数据管理-黑白名单授权")
	@ApiOperation(value="武警部队_无人机_黑白名单授权-分页列表查询", notes="武警部队_无人机_黑白名单授权-分页列表查询")
	@GetMapping(value = "/list")
	public AjaxResult<?> queryPageList(WjbdWrjHbmdsq wjbdWrjHbmdsq,
									   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									   HttpServletRequest req) {
		QueryWrapper<WjbdWrjHbmdsq> queryWrapper = QueryGenerator.initQueryWrapper(wjbdWrjHbmdsq, req.getParameterMap());
		Page<WjbdWrjHbmdsq> page = new Page<WjbdWrjHbmdsq>(pageNo, pageSize);
		IPage<WjbdWrjHbmdsq> pageList = wjbdWrjHbmdsqService.page(page, queryWrapper);
		return AjaxResult.OK(pageList);
	}


	 /**
	  * 黑白名单查询
	  */
	 @AutoLog(value = "武警部队_无人机_黑白名单授权信息查询",YYMK = "数据管理-黑白名单授权")
	 @ApiOperation(value="武警部队_无人机_黑白名单授权信息查询", notes="武警部队_无人机_黑白名单授权信息查询")
	 @GetMapping(value = "/hbmdList")
	 public AjaxResult<?> hbmdList(@RequestParam(name="hmdsb",required = false) String hmdsb,
								   @RequestParam(name="bmdsb", required = false) String bmdsb,
								   @RequestParam(name="wsqsb", required = false) String wsqsb) {

		  List<WjbdWrjJbxxVo> hmdList = wjbdWrjHbmdsqService.hmdList();
		  List<WjbdWrjJbxxVo> bmdList = wjbdWrjHbmdsqService.bmdList();
		 // List<WjbdWrjJbxxVo> wsqList = wjbdWrjHbmdsqService.wsqList();
		  Map<String,Object> map = new HashMap<>();
		  map.put("黑名单",hmdList);
		  map.put("白名单",bmdList);
		 // map.put("未授权",wsqList);
		 return AjaxResult.OK(map);
	 }
	
	/**
	 *   添加
	 *
	 * @param wjbdWrjHbmdsq
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_黑白名单授权-添加",YYMK = "数据管理-黑白名单授权")
	@ApiOperation(value="武警部队_无人机_黑白名单授权-添加", notes="武警部队_无人机_黑白名单授权-添加")
	@PostMapping(value = "/add")
	public AjaxResult<?> add(@RequestBody WjbdWrjHbmdsq wjbdWrjHbmdsq) {
		wjbdWrjHbmdsqService.save(wjbdWrjHbmdsq);
		return AjaxResult.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param wjbdWrjHbmdsq
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_黑白名单授权-编辑",YYMK = "数据管理-黑白名单授权")
	@ApiOperation(value="武警部队_无人机_黑白名单授权-编辑", notes="武警部队_无人机_黑白名单授权-编辑")
	@PutMapping(value = "/edit")
	public AjaxResult<?> edit(@RequestBody WjbdWrjHbmdsq wjbdWrjHbmdsq) {
		wjbdWrjHbmdsqService.updateById(wjbdWrjHbmdsq);
		return AjaxResult.OK("编辑成功!");
	}

	 /**
	  *  编辑
	  *
	  * @param updateSqxxVo
	  * @return
	  */
	 @AutoLog(value = "武警部队_无人机_黑白名单授权-编辑",YYMK = "数据管理-黑白名单授权")
	 @ApiOperation(value="武警部队_无人机_黑白名单授权-编辑", notes="武警部队_无人机_黑白名单授权-编辑")
	 @PutMapping(value = "/editSqxx")
	 public AjaxResult<?> editSqxx(@RequestBody UpdateSqxxVo updateSqxxVo) {
		 wjbdWrjHbmdsqService.editSqxx(updateSqxxVo);
		 return AjaxResult.OK("编辑成功!");
	 }
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_黑白名单授权-通过id删除")
	@ApiOperation(value="武警部队_无人机_黑白名单授权-通过id删除", notes="武警部队_无人机_黑白名单授权-通过id删除")
	@DeleteMapping(value = "/delete")
	public AjaxResult<?> delete(@RequestParam(name="id",required=true) String id) {
		wjbdWrjHbmdsqService.removeById(id);
		return AjaxResult.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_黑白名单授权-批量删除",YYMK = "数据管理-黑白名单授权")
	@ApiOperation(value="武警部队_无人机_黑白名单授权-批量删除", notes="武警部队_无人机_黑白名单授权-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public AjaxResult<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.wjbdWrjHbmdsqService.removeByIds(Arrays.asList(ids.split(",")));
		return AjaxResult.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_黑白名单授权-通过id查询",YYMK = "数据管理-黑白名单授权")
	@ApiOperation(value="武警部队_无人机_黑白名单授权-通过id查询", notes="武警部队_无人机_黑白名单授权-通过id查询")
	@GetMapping(value = "/queryById")
	public AjaxResult<?> queryById(@RequestParam(name="id",required=true) String id) {
		WjbdWrjHbmdsq wjbdWrjHbmdsq = wjbdWrjHbmdsqService.getById(id);
		if(wjbdWrjHbmdsq==null) {
			return AjaxResult.error("未找到对应数据");
		}
		return AjaxResult.OK(wjbdWrjHbmdsq);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param wjbdWrjHbmdsq
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WjbdWrjHbmdsq wjbdWrjHbmdsq) {
        return super.exportXls(request, wjbdWrjHbmdsq, WjbdWrjHbmdsq.class, "武警部队_无人机_黑白名单授权");
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
        return super.importExcel(request, response, WjbdWrjHbmdsq.class);
    }

}
