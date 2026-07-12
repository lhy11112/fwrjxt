package org.jeecg.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.system.entity.WjbdMhYyczLog;
import org.jeecg.modules.system.service.IWjbdMhYyczLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;

/**
* @Description: 武警部队_典型应用_操作日志
* @Author: jeecg-boot
* @Date:   2025-07-29
*
*/
@Api(tags="武警部队_典型应用_操作日志")
@RestController
@RequestMapping("/system/wjbdMhYyczLog")
@Slf4j
public class WjbdMhYyczLogController extends JeecgController<WjbdMhYyczLog, IWjbdMhYyczLogService> {
   @Autowired
   private IWjbdMhYyczLogService wjbdMhYyczLogService;

   /**
    * 分页列表查询
    *
    * @param wjbdMhYyczLog
    * @param pageNo
    * @param pageSize
    * @param req
    * @return
    */
   @AutoLog(value = "武警部队_典型应用_操作日志-分页列表查询")
   @ApiOperation(value="武警部队_典型应用_操作日志-分页列表查询", notes="武警部队_典型应用_操作日志-分页列表查询")
   @GetMapping(value = "/list")
   public AjaxResult<?> queryPageList(WjbdMhYyczLog wjbdMhYyczLog,
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
       wjbdMhYyczLog.setYymc("目标防卫场景反无人机应用");
       QueryWrapper<WjbdMhYyczLog> queryWrapper = QueryGenerator.initQueryWrapper(wjbdMhYyczLog, parameterMap);
       Page<WjbdMhYyczLog> page = new Page<WjbdMhYyczLog>(pageNo, pageSize);
       IPage<WjbdMhYyczLog> pageList = wjbdMhYyczLogService.page(page, queryWrapper);
       return AjaxResult.OK(pageList);
   }

   /**
    *   添加
    *
    * @param wjbdMhYyczLog
    * @return
    */
   @AutoLog(value = "武警部队_典型应用_操作日志-添加")
   @ApiOperation(value="武警部队_典型应用_操作日志-添加", notes="武警部队_典型应用_操作日志-添加")
   @PostMapping(value = "/add")
   public AjaxResult<?> add(@RequestBody WjbdMhYyczLog wjbdMhYyczLog) {
       wjbdMhYyczLogService.save(wjbdMhYyczLog);
       return AjaxResult.OK("添加成功！");
   }

   /**
    *  编辑
    *
    * @param wjbdMhYyczLog
    * @return
    */
   @AutoLog(value = "武警部队_典型应用_操作日志-编辑")
   @ApiOperation(value="武警部队_典型应用_操作日志-编辑", notes="武警部队_典型应用_操作日志-编辑")
   @PutMapping(value = "/edit")
   public AjaxResult<?> edit(@RequestBody WjbdMhYyczLog wjbdMhYyczLog) {
       wjbdMhYyczLogService.updateById(wjbdMhYyczLog);
       return AjaxResult.OK("编辑成功!");
   }

   /**
    *   通过id删除
    *
    * @param id
    * @return
    */
   @AutoLog(value = "武警部队_典型应用_操作日志-通过id删除")
   @ApiOperation(value="武警部队_典型应用_操作日志-通过id删除", notes="武警部队_典型应用_操作日志-通过id删除")
   @DeleteMapping(value = "/delete")
   public AjaxResult<?> delete(@RequestParam(name="id",required=true) String id) {
       wjbdMhYyczLogService.removeById(id);
       return AjaxResult.OK("删除成功!");
   }

   /**
    *  批量删除
    *
    * @param ids
    * @return
    */
   @AutoLog(value = "武警部队_典型应用_操作日志-批量删除")
   @ApiOperation(value="武警部队_典型应用_操作日志-批量删除", notes="武警部队_典型应用_操作日志-批量删除")
   @DeleteMapping(value = "/deleteBatch")
   public AjaxResult<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
       this.wjbdMhYyczLogService.removeByIds(Arrays.asList(ids.split(",")));
       return AjaxResult.OK("批量删除成功!");
   }

   /**
    * 通过id查询
    *
    * @param id
    * @return
    */
   @AutoLog(value = "武警部队_典型应用_操作日志-通过id查询")
   @ApiOperation(value="武警部队_典型应用_操作日志-通过id查询", notes="武警部队_典型应用_操作日志-通过id查询")
   @GetMapping(value = "/queryById")
   public AjaxResult<?> queryById(@RequestParam(name="id",required=true) String id) {
       WjbdMhYyczLog wjbdMhYyczLog = wjbdMhYyczLogService.getById(id);
       if(wjbdMhYyczLog==null) {
           return AjaxResult.error("未找到对应数据");
       }
       return AjaxResult.OK(wjbdMhYyczLog);
   }

   /**
   * 导出excel
   *
   * @param request
   * @param wjbdMhYyczLog
   */
   @RequestMapping(value = "/exportXls")
   public ModelAndView exportXls(HttpServletRequest request, WjbdMhYyczLog wjbdMhYyczLog) {
       return super.exportXls(request, wjbdMhYyczLog, WjbdMhYyczLog.class, "武警部队_典型应用_操作日志");
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
       return super.importExcel(request, response, WjbdMhYyczLog.class);
   }

}
