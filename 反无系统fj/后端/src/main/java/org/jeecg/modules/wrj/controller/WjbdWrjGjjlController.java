package org.jeecg.modules.wrj.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.wrj.entity.WjbdWrjGjjl;
import org.jeecg.modules.wrj.entity.WjbdWrjKy;
import org.jeecg.modules.wrj.entity.dto.WjbdWrjGjjlDto;
import org.jeecg.modules.wrj.service.IWjbdWrjGjjlService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.wrj.service.IWjbdWrjKyService;
import org.jeecg.modules.wrj.vo.GjtjVo;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

 /**
 * @Description: 武警部队_无人机_告警记录
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Api(tags="武警部队_无人机_告警记录")
@RestController
@RequestMapping("/wrj/wjbdWrjGjjl")
@Slf4j
public class WjbdWrjGjjlController extends JeecgController<WjbdWrjGjjl, IWjbdWrjGjjlService> {
	@Autowired
	private IWjbdWrjGjjlService wjbdWrjGjjlService;
	 @Autowired
	 private WebSocket webSocket;
	 @Autowired
	 private IWjbdWrjKyService wjbdWrjKyService;
	/**
	 * 分页列表查询
	 *
	 * @param wjbdWrjGjjl
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_告警记录-分页列表查询",YYMK = "数据管理-告警记录")
	@ApiOperation(value="武警部队_无人机_告警记录-分页列表查询", notes="武警部队_无人机_告警记录-分页列表查询")
	@GetMapping(value = "/list")
	public AjaxResult<?> queryPageList(WjbdWrjGjjl wjbdWrjGjjl,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<WjbdWrjGjjl> queryWrapper = QueryGenerator.initQueryWrapper(wjbdWrjGjjl, req.getParameterMap());
		Page<WjbdWrjGjjl> page = new Page<WjbdWrjGjjl>(pageNo, pageSize);
		queryWrapper.orderByDesc("czsj");
		if (StringUtils.isNotBlank(wjbdWrjGjjl.getRq())){
				queryWrapper.eq("DATE(gjfssj)",wjbdWrjGjjl.getRq());
		}
		IPage<WjbdWrjGjjl> pageList = wjbdWrjGjjlService.page(page, queryWrapper);
		return AjaxResult.OK(pageList);
	}
	 /**
	  * 分页列表查询
	  *
	  * @return
	  */
	 @AutoLog(value = "武警部队_无人机_告警记录-分页列表查询",YYMK = "数据管理-告警记录")
	 @ApiOperation(value="武警部队_无人机_告警记录-分页列表查询", notes="武警部队_无人机_告警记录-分页列表查询")
	 @GetMapping(value = "/GjQkTJ")
	 public AjaxResult<?> GjQkTJ(WjbdWrjGjjlDto wjbdWrjGjjlDto) {
		 return AjaxResult.OK(wjbdWrjGjjlService.GjQkTJ(wjbdWrjGjjlDto));
	 }
	/**
	 *   添加
	 *
	 * @param wjbdWrjGjjl
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_告警记录-添加",YYMK = "数据管理-告警记录")
	@ApiOperation(value="武警部队_无人机_告警记录-添加", notes="武警部队_无人机_告警记录-添加")
	@PostMapping(value = "/add")
	public AjaxResult<?> add(@RequestBody WjbdWrjGjjl wjbdWrjGjjl) {
		wjbdWrjGjjlService.save(wjbdWrjGjjl);
		return AjaxResult.OK("添加成功！");
	}
	
	/**
	 *  编辑
	 *
	 * @param wjbdWrjGjjl
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_告警记录-编辑",YYMK = "数据管理-告警记录")
	@ApiOperation(value="武警部队_无人机_告警记录-编辑", notes="武警部队_无人机_告警记录-编辑")
	@PutMapping(value = "/edit")
	public AjaxResult<?> edit(@RequestBody WjbdWrjGjjl wjbdWrjGjjl) {
		wjbdWrjGjjlService.updateById(wjbdWrjGjjl);
		return AjaxResult.OK("编辑成功!");
	}
	
	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_告警记录-通过id删除",YYMK = "数据管理-告警记录")
	@ApiOperation(value="武警部队_无人机_告警记录-通过id删除", notes="武警部队_无人机_告警记录-通过id删除")
	@DeleteMapping(value = "/delete")
	public AjaxResult<?> delete(@RequestParam(name="id",required=true) String id) {
		wjbdWrjGjjlService.removeById(id);
		return AjaxResult.OK("删除成功!");
	}
	
	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_告警记录-批量删除",YYMK = "数据管理-告警记录")
	@ApiOperation(value="武警部队_无人机_告警记录-批量删除", notes="武警部队_无人机_告警记录-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public AjaxResult<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.wjbdWrjGjjlService.removeByIds(Arrays.asList(ids.split(",")));
		return AjaxResult.OK("批量删除成功!");
	}
	
	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "武警部队_无人机_告警记录-通过id查询",YYMK = "数据管理-告警记录")
	@ApiOperation(value="武警部队_无人机_告警记录-通过id查询", notes="武警部队_无人机_告警记录-通过id查询")
	@GetMapping(value = "/queryById")
	public AjaxResult<?> queryById(@RequestParam(name="id",required=true) String id) {
		WjbdWrjGjjl wjbdWrjGjjl = wjbdWrjGjjlService.getById(id);
		if(wjbdWrjGjjl==null) {
			return AjaxResult.error("未找到对应数据");
		}
		return AjaxResult.OK(wjbdWrjGjjl);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param wjbdWrjGjjl
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WjbdWrjGjjl wjbdWrjGjjl) {
        return super.exportXls(request, wjbdWrjGjjl, WjbdWrjGjjl.class, "武警部队_无人机_告警记录");
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
        return super.importExcel(request, response, WjbdWrjGjjl.class);
    }


	 /**
	  * 无人机告警统计按照年，季度，月，周，日等维度统计
	  */
	 @AutoLog(value = "武警部队_无人机_告警记录-趋势统计",YYMK = "数据管理-告警记录")
	 @ApiOperation(value="武警部队_无人机_告警记录-趋势统计", notes="武警部队_无人机_告警记录-趋势统计")
	 @GetMapping(value = "/qstj")
	 public AjaxResult<?> qstj(GjtjVo gjtjVo) {

		 List<Map<String,Object>> list = wjbdWrjGjjlService.getGstjCount(gjtjVo);
		 return AjaxResult.OK(list);
	 }

	 /**
	  * 分页列表查询
	  *
	  * @param gjtjVo
	  * @param pageNo
	  * @param pageSize
	  * @param
	  * @return
	  */
	 @AutoLog(value = "武警部队_无人机_告警记录-分页列表查询",YYMK = "数据管理-告警记录")
	 @ApiOperation(value="武警部队_无人机_告警记录-分页列表查询", notes="武警部队_无人机_告警记录-分页列表查询")
	 @GetMapping(value = "/qstjEj")
	 public AjaxResult<?> qstjEj(GjtjVo gjtjVo,
										@RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
										@RequestParam(name="pageSize", defaultValue="10") Integer pageSize) {
		 Page<WjbdWrjGjjl> page = new Page<WjbdWrjGjjl>(pageNo, pageSize);
		 IPage<WjbdWrjGjjl> pageList = wjbdWrjGjjlService.qstjEj(page, gjtjVo);
		 return AjaxResult.OK(pageList);
	 }
 }
