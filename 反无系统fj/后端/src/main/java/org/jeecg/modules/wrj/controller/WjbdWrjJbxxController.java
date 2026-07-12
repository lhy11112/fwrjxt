package org.jeecg.modules.wrj.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Update;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.uav.entity.UavDetectMsg;
import org.jeecg.modules.wrj.entity.WjbdWrjHbmdsq;
import org.jeecg.modules.wrj.entity.WjbdWrjJbxx;
import org.jeecg.modules.wrj.service.IWjbdWrjHbmdsqService;
import org.jeecg.modules.wrj.service.IWjbdWrjJbxxService;

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
 * @Description: 武警部队_无人机_基本信息
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Api(tags="武警部队_无人机_基本信息")
@RestController
@RequestMapping("/wrj/wjbdWrjJbxx")
@Slf4j
public class WjbdWrjJbxxController extends JeecgController<WjbdWrjJbxx, IWjbdWrjJbxxService> {
	@Autowired
	private IWjbdWrjJbxxService wjbdWrjJbxxService;
	@Autowired
	private IWjbdWrjHbmdsqService wjbdWrjHbmdsqService;

	/**
	 * 分页列表查询
	 *
	 * @param wjbdWrjJbxx
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_基本信息-分页列表查询",YYMK = "数据管理-无人机_基本信息")
	@ApiOperation(value="武警部队_无人机_基本信息-分页列表查询", notes="武警部队_无人机_基本信息-分页列表查询")
	@GetMapping(value = "/list")
	public AjaxResult<?> queryPageList(WjbdWrjJbxx wjbdWrjJbxx,
									   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									   HttpServletRequest req) {
		QueryWrapper<WjbdWrjJbxx> queryWrapper = QueryGenerator.initQueryWrapper(wjbdWrjJbxx, req.getParameterMap());
		Page<WjbdWrjJbxx> page = new Page<WjbdWrjJbxx>(pageNo, pageSize);
		queryWrapper.orderByDesc("czsj");
		IPage<WjbdWrjJbxx> pageList = wjbdWrjJbxxService.page(page, queryWrapper);
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
	@AutoLog(value = "武警部队_无人机_基本信息-分页列表查询",YYMK = "数据管理-无人机_基本信息")
	@ApiOperation(value="武警部队_无人机_基本信息-分页列表查询", notes="武警部队_无人机_基本信息-分页列表查询")
	@GetMapping(value = "/listByKyid")
	public AjaxResult<?> listByKyid(@RequestParam(name="kyid")String kyid,
									@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
									@RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
									HttpServletRequest req) {
		Page<WjbdWrjJbxx> page = new Page<WjbdWrjJbxx>(pageNo, pageSize);
		IPage<WjbdWrjJbxx> pageList = wjbdWrjJbxxService.listByKyid(page, kyid);
		return AjaxResult.OK(pageList);
	}

	/**
	 *   添加
	 *
	 * @param wjbdWrjJbxx
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_基本信息-添加",YYMK = "数据管理-无人机_基本信息")
	@ApiOperation(value="武警部队_无人机_基本信息-添加", notes="武警部队_无人机_基本信息-添加")
	@PostMapping(value = "/add")
	public AjaxResult<?> add(@RequestBody WjbdWrjJbxx wjbdWrjJbxx) {
		wjbdWrjJbxxService.save(wjbdWrjJbxx);
		if(wjbdWrjJbxx.getAuthStatus()==1||wjbdWrjJbxx.getAuthStatus()==2){
			WjbdWrjHbmdsq wjbdWrjHbmdsq = new WjbdWrjHbmdsq();
			wjbdWrjHbmdsq.setMdlx(wjbdWrjJbxx.getAuthStatus()==1?"白名单":"黑名单");
			wjbdWrjHbmdsq.setWrjid(wjbdWrjJbxx.getId());
			wjbdWrjHbmdsq.setSqsj(new Date());
			wjbdWrjHbmdsqService.save(wjbdWrjHbmdsq);
		}
		return AjaxResult.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param wjbdWrjJbxx
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_基本信息-编辑",YYMK = "数据管理-无人机_基本信息")
	@ApiOperation(value="武警部队_无人机_基本信息-编辑", notes="武警部队_无人机_基本信息-编辑")
	@PutMapping(value = "/edit")
	public AjaxResult<?> edit(@RequestBody WjbdWrjJbxx wjbdWrjJbxx) {
		wjbdWrjJbxxService.updateById(wjbdWrjJbxx);
		QueryWrapper<WjbdWrjHbmdsq> queryWrapper = new QueryWrapper<WjbdWrjHbmdsq>();
		queryWrapper.eq("wrjid",wjbdWrjJbxx.getId());
		wjbdWrjHbmdsqService.remove(queryWrapper);
		if(wjbdWrjJbxx.getAuthStatus()==1||wjbdWrjJbxx.getAuthStatus()==2){
			WjbdWrjHbmdsq wjbdWrjHbmdsq = new WjbdWrjHbmdsq();
			wjbdWrjHbmdsq.setMdlx(wjbdWrjJbxx.getAuthStatus()==1?"白名单":"黑名单");
			wjbdWrjHbmdsq.setWrjid(wjbdWrjJbxx.getId());
			wjbdWrjHbmdsq.setSqsj(new Date());
			wjbdWrjHbmdsqService.save(wjbdWrjHbmdsq);
		}
		return AjaxResult.OK("编辑成功!");
	}




	/**
	 *  编辑
	 *
	 * @param wjbdWrjJbxx
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_基本信息-编辑",YYMK = "数据管理-无人机_基本信息")
	@ApiOperation(value="武警部队_无人机_基本信息-编辑", notes="武警部队_无人机_基本信息-编辑")
	@PutMapping(value = "/editHbmd")
	public AjaxResult<?> editHbmd(@RequestBody WjbdWrjJbxx wjbdWrjJbxx) {
		LambdaUpdateWrapper<WjbdWrjJbxx> updateWrapper = new LambdaUpdateWrapper<>();
		updateWrapper.set(WjbdWrjJbxx::getAuthStatus,wjbdWrjJbxx.getAuthStatus());
		updateWrapper.eq(WjbdWrjJbxx::getId,wjbdWrjJbxx.getId());
		wjbdWrjJbxxService.update(updateWrapper);
		QueryWrapper<WjbdWrjHbmdsq> queryWrapper = new QueryWrapper<WjbdWrjHbmdsq>();
		queryWrapper.eq("wrjid",wjbdWrjJbxx.getId());
		wjbdWrjHbmdsqService.remove(queryWrapper);
		if(wjbdWrjJbxx.getAuthStatus()==1||wjbdWrjJbxx.getAuthStatus()==2){
			WjbdWrjHbmdsq wjbdWrjHbmdsq = new WjbdWrjHbmdsq();
			wjbdWrjHbmdsq.setMdlx(wjbdWrjJbxx.getAuthStatus()==1?"白名单":"黑名单");
			wjbdWrjHbmdsq.setWrjid(wjbdWrjJbxx.getId());
			wjbdWrjHbmdsq.setSqsj(new Date());
			wjbdWrjHbmdsqService.save(wjbdWrjHbmdsq);
		}
		return AjaxResult.OK("编辑成功!");
	}


	/**
	 *   批量操作
	 *
	 * @param wjbdWrjJbxxList
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_基本信息-批量操作",YYMK = "数据管理-无人机_基本信息")
	@ApiOperation(value="武警部队_无人机_基本信息-批量操作", notes="武警部队_无人机_基本信息-批量操作")
	@PostMapping(value = "/plcz")
	public AjaxResult<?> add(@RequestBody List<WjbdWrjJbxx> wjbdWrjJbxxList) {
		wjbdWrjJbxxService.updateBatchById(wjbdWrjJbxxList);
		List<String> idList = wjbdWrjJbxxList.stream()
				.map(WjbdWrjJbxx::getId)
				.collect(Collectors.toList());
		QueryWrapper<WjbdWrjHbmdsq> queryWrapper = new QueryWrapper<WjbdWrjHbmdsq>();
		queryWrapper.in("wrjid",idList);
		wjbdWrjHbmdsqService.remove(queryWrapper);
		if(wjbdWrjJbxxList.get(0).getAuthStatus()==1||wjbdWrjJbxxList.get(0).getAuthStatus()==2){
			for(String wrjid:idList){
				WjbdWrjHbmdsq wjbdWrjHbmdsq = new WjbdWrjHbmdsq();
				wjbdWrjHbmdsq.setMdlx(wjbdWrjJbxxList.get(0).getAuthStatus()==1?"白名单":"黑名单");
				wjbdWrjHbmdsq.setWrjid(wrjid);
				wjbdWrjHbmdsq.setSqsj(new Date());
				wjbdWrjHbmdsqService.save(wjbdWrjHbmdsq);
			}
		}
		return AjaxResult.OK("操作成功！");
	}


	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_基本信息-通过id删除",YYMK = "数据管理-无人机_基本信息")
	@ApiOperation(value="武警部队_无人机_基本信息-通过id删除", notes="武警部队_无人机_基本信息-通过id删除")
	@DeleteMapping(value = "/delete")
	public AjaxResult<?> delete(@RequestParam(name="id",required=true) String id) {
		wjbdWrjJbxxService.removeById(id);
		//删除关联无人机黑白名单授权基本信息
		QueryWrapper<WjbdWrjHbmdsq> queryWrapper = new QueryWrapper<WjbdWrjHbmdsq>();
		queryWrapper.in("wrjid",id);
		wjbdWrjHbmdsqService.remove(queryWrapper);
		return AjaxResult.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_基本信息-批量删除",YYMK = "数据管理-无人机_基本信息")
	@ApiOperation(value="武警部队_无人机_基本信息-批量删除", notes="武警部队_无人机_基本信息-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public AjaxResult<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		//删除无人机基本信息表数据
		this.wjbdWrjJbxxService.removeByIds(Arrays.asList(ids.split(",")));
		//删除关联无人机黑白名单授权基本信息
		QueryWrapper<WjbdWrjHbmdsq> queryWrapper = new QueryWrapper<WjbdWrjHbmdsq>();
		queryWrapper.in("wrjid",Arrays.asList(ids.split(",")));
		wjbdWrjHbmdsqService.remove(queryWrapper);
		return AjaxResult.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_基本信息-通过id查询",YYMK = "数据管理-无人机_基本信息")
	@ApiOperation(value="武警部队_无人机_基本信息-通过id查询", notes="武警部队_无人机_基本信息-通过id查询")
	@GetMapping(value = "/queryById")
	public AjaxResult<?> queryById(@RequestParam(name="id",required=true) String id) {
		WjbdWrjJbxx wjbdWrjJbxx = wjbdWrjJbxxService.getById(id);
		if(wjbdWrjJbxx==null) {
			return AjaxResult.error("未找到对应数据");
		}
		return AjaxResult.OK(wjbdWrjJbxx);
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 * @param wjbdWrjJbxx
	 */
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, WjbdWrjJbxx wjbdWrjJbxx,String type) {
		if (StringUtils.isNotBlank(type)&&type.equals("template")){
			return	super.exportXlsTemplate(request, wjbdWrjJbxx, WjbdWrjJbxx.class, "武警部队_无人机_基本信息-导入模板");
		}
		return super.exportXls(request, wjbdWrjJbxx, WjbdWrjJbxx.class, "武警部队_无人机_基本信息");
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
		return super.importExcel(request, response, WjbdWrjJbxx.class);
	}

	@AutoLog(value = "武警部队_无人机_基本信息-情况统计",YYMK = "综合态势-无人机信息统计")
	@ApiOperation(value="武警部队_无人机_基本信息-情况统计", notes="武警部队_无人机_基本信息-情况统计")
	@GetMapping(value = "/getWrjCount")
	public AjaxResult<?> getWrjCount() {
		List<Map<String,Object>> list = wjbdWrjJbxxService.getWrjCount();
		return AjaxResult.OK(list);
	}
}
