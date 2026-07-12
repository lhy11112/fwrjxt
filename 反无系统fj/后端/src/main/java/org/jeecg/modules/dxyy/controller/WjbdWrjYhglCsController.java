package org.jeecg.modules.dxyy.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.dxyy.entity.WjbdWrjYhglCs;
import org.jeecg.modules.dxyy.service.IWjbdWrjYhglCsService;

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
 * @Description: 武警部队_无人机_用户管理_参数
 * @Author: jeecg-boot
 * @Date:   2025-09-01
 * @Version: V1.0
 */
@Api(tags="武警部队_无人机_用户管理_参数")
@RestController
@RequestMapping("/dxyy/wjbdWrjYhglCs")
@Slf4j
public class WjbdWrjYhglCsController extends JeecgController<WjbdWrjYhglCs, IWjbdWrjYhglCsService> {
	@Autowired
	private IWjbdWrjYhglCsService wjbdWrjYhglCsService;
	
	/**
	 * 分页列表查询
	 *
	 * @param wjbdWrjYhglCs
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_用户管理_参数-分页列表查询",YYMK="武警部队_无人机_用户管理_参数")
	@ApiOperation(value="武警部队_无人机_用户管理_参数-分页列表查询", notes="武警部队_无人机_用户管理_参数-分页列表查询")
	@GetMapping(value = "/list")
	public AjaxResult<?> queryPageList(WjbdWrjYhglCs wjbdWrjYhglCs,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		Map<String, String[]> parameterMap = req.getParameterMap();
		// 检查参数映射是否为空
		if (parameterMap == null || parameterMap.isEmpty()) {
			if (log.isInfoEnabled()){
				log.info("参数为空！！！");
			}
		}
		QueryWrapper<WjbdWrjYhglCs> queryWrapper = QueryGenerator.initQueryWrapper(wjbdWrjYhglCs, parameterMap);
		Page<WjbdWrjYhglCs> page = new Page<WjbdWrjYhglCs>(pageNo, pageSize);
		//当前用户
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		if (user!=null){
		queryWrapper.eq("YH_ID",user.getId());
		}
		IPage<WjbdWrjYhglCs> pageList = wjbdWrjYhglCsService.page(page, queryWrapper);
		if (pageList.getRecords().isEmpty()){
			pageList.setRecords(wjbdWrjYhglCsService.updateJwdDmNm());
		}
		return AjaxResult.OK(pageList);
	}
	
	/**
	 *   添加
	 *
	 * @param wjbdWrjYhglCs
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_用户管理_参数-添加",YYMK="武警部队_无人机_用户管理_参数")
	@ApiOperation(value="武警部队_无人机_用户管理_参数-添加", notes="武警部队_无人机_用户管理_参数-添加")
	@PostMapping(value = "/add")
	public AjaxResult<?> add(@RequestBody WjbdWrjYhglCs wjbdWrjYhglCs) {
		wjbdWrjYhglCsService.save(wjbdWrjYhglCs);
		return AjaxResult.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param wjbdWrjYhglCs
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_用户管理_参数-编辑",YYMK="武警部队_无人机_用户管理_参数")
	@ApiOperation(value="武警部队_无人机_用户管理_参数-编辑", notes="武警部队_无人机_用户管理_参数-编辑")
	@PutMapping(value = "/edit")
	public AjaxResult<?> edit(@RequestBody WjbdWrjYhglCs wjbdWrjYhglCs) {
		wjbdWrjYhglCsService.updateById(wjbdWrjYhglCs);
		return AjaxResult.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_用户管理_参数-通过id删除",YYMK="武警部队_无人机_用户管理_参数")
	@ApiOperation(value="武警部队_无人机_用户管理_参数-通过id删除", notes="武警部队_无人机_用户管理_参数-通过id删除")
	@DeleteMapping(value = "/delete")
	public AjaxResult<?> delete(@RequestParam(name="id",required=true) String id) {
		wjbdWrjYhglCsService.removeById(id);
		return AjaxResult.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_用户管理_参数-批量删除",YYMK="武警部队_无人机_用户管理_参数")
	@ApiOperation(value="武警部队_无人机_用户管理_参数-批量删除", notes="武警部队_无人机_用户管理_参数-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public AjaxResult<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.wjbdWrjYhglCsService.removeByIds(Arrays.asList(ids.split(",")));
		return AjaxResult.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_用户管理_参数-通过id查询",YYMK="武警部队_无人机_用户管理_参数")
	@ApiOperation(value="武警部队_无人机_用户管理_参数-通过id查询", notes="武警部队_无人机_用户管理_参数-通过id查询")
	@GetMapping(value = "/queryById")
	public AjaxResult<?> queryById(@RequestParam(name="id",required=true) String id) {
		WjbdWrjYhglCs wjbdWrjYhglCs = wjbdWrjYhglCsService.getById(id);
		if(wjbdWrjYhglCs==null) {
			return AjaxResult.error("未找到对应数据");
		}
		return AjaxResult.OK(wjbdWrjYhglCs);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param wjbdWrjYhglCs
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WjbdWrjYhglCs wjbdWrjYhglCs) {
        return super.exportXls(request, wjbdWrjYhglCs, WjbdWrjYhglCs.class, "武警部队_无人机_用户管理_参数");
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
        return super.importExcel(request, response, WjbdWrjYhglCs.class);
    }

}
