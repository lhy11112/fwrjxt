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

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.wrj.entity.WjbdWrjScj;
import org.jeecg.modules.wrj.entity.vo.WjbdWrjScjTreeVO;
import org.jeecg.modules.wrj.service.IWjbdWrjScjService;

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
 * @Description: 武警部队_无人机_收藏夹
 * @Author: jeecg-boot
 * @Date:   2026-02-04
 * @Version: V1.0
 */
@Api(tags="武警部队_无人机_收藏夹")
@RestController
@RequestMapping("/wrj/wjbdWrjScj")
@Slf4j
public class WjbdWrjScjController extends JeecgController<WjbdWrjScj, IWjbdWrjScjService> {
	@Autowired
	private IWjbdWrjScjService wjbdWrjScjService;
	
	/**
	 * 分页列表查询
	 *
	 * @param wjbdWrjScj
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_收藏夹-分页列表查询")
	@ApiOperation(value="武警部队_无人机_收藏夹-分页列表查询", notes="武警部队_无人机_收藏夹-分页列表查询")
	@GetMapping(value = "/list")
	public AjaxResult<?> queryPageList(WjbdWrjScj wjbdWrjScj,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<WjbdWrjScj> queryWrapper = QueryGenerator.initQueryWrapper(wjbdWrjScj, req.getParameterMap());
		Page<WjbdWrjScj> page = new Page<WjbdWrjScj>(pageNo, pageSize);
		//当前用户
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		if (user!=null){
			queryWrapper.eq("YH_ID",user.getId());
		}
		IPage<WjbdWrjScj> pageList = wjbdWrjScjService.page(page, queryWrapper);
		return AjaxResult.OK(pageList);
	}
	 /**
	  * 查询用户收藏夹树形数据
	  * @param yhId 用户ID
	  * @return 树形结构数据
	  */
	 @GetMapping("/tree")
	 public AjaxResult<?> getTree(String yhId) {
		 List<WjbdWrjScjTreeVO> treeList = wjbdWrjScjService.getTreeByYhId(yhId);
		 return AjaxResult.OK(treeList);
	 }
	/**
	 *   添加
	 *
	 * @param wjbdWrjScj
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_收藏夹-添加")
	@ApiOperation(value="武警部队_无人机_收藏夹-添加", notes="武警部队_无人机_收藏夹-添加")
	@PostMapping(value = "/add")
	public AjaxResult<?> add(@RequestBody WjbdWrjScj wjbdWrjScj) {
		//当前用户
		LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		if (user!=null){
			wjbdWrjScj.setYhId(user.getId());
		}
		wjbdWrjScjService.save(wjbdWrjScj);
		return AjaxResult.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param wjbdWrjScj
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_收藏夹-编辑")
	@ApiOperation(value="武警部队_无人机_收藏夹-编辑", notes="武警部队_无人机_收藏夹-编辑")
	@PutMapping(value = "/edit")
	public AjaxResult<?> edit(@RequestBody WjbdWrjScj wjbdWrjScj) {
		wjbdWrjScjService.updateById(wjbdWrjScj);
		return AjaxResult.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_收藏夹-通过id删除")
	@ApiOperation(value="武警部队_无人机_收藏夹-通过id删除", notes="武警部队_无人机_收藏夹-通过id删除")
	@DeleteMapping(value = "/delete")
	public AjaxResult<?> delete(@RequestParam(name="id",required=true) String id) {
		wjbdWrjScjService.removeById(id);
		return AjaxResult.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_收藏夹-批量删除")
	@ApiOperation(value="武警部队_无人机_收藏夹-批量删除", notes="武警部队_无人机_收藏夹-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public AjaxResult<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.wjbdWrjScjService.removeByIds(Arrays.asList(ids.split(",")));
		return AjaxResult.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_收藏夹-通过id查询")
	@ApiOperation(value="武警部队_无人机_收藏夹-通过id查询", notes="武警部队_无人机_收藏夹-通过id查询")
	@GetMapping(value = "/queryById")
	public AjaxResult<?> queryById(@RequestParam(name="id",required=true) String id) {
		WjbdWrjScj wjbdWrjScj = wjbdWrjScjService.getById(id);
		if(wjbdWrjScj==null) {
			return AjaxResult.error("未找到对应数据");
		}
		return AjaxResult.OK(wjbdWrjScj);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param wjbdWrjScj
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WjbdWrjScj wjbdWrjScj) {
        return super.exportXls(request, wjbdWrjScj, WjbdWrjScj.class, "武警部队_无人机_收藏夹");
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
        return super.importExcel(request, response, WjbdWrjScj.class);
    }

}
