package org.jeecg.modules.wrj.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.jeecg.modules.wrj.entity.WjbdHsZskWj;
import org.jeecg.modules.wrj.service.IWjbdHsZskWjService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * @Description: 知识库文件
 * @Author: jeecg-boot
 * @Date: 2024-09-14
 */
@Api(tags = "知识库文件")
@RestController
@RequestMapping("/wjfjsjzt/wjbdHsZskWj")
@Slf4j
public class WjbdHsZskWjController extends JeecgController<WjbdHsZskWj, IWjbdHsZskWjService> {
    @Autowired
    private IWjbdHsZskWjService wjbdHsZskWjService;

    /**
     * 分页列表查询
     *
     * @param wjbdHsZskWj
     * @param pageNo      页码
     * @param pageSize    每页条数
     * @param req         请求体
     * @return 结果
     */
    @AutoLog(value = "知识库文件-分页列表查询")
    @ApiOperation(value = "知识库文件-分页列表查询", notes = "知识库文件-分页列表查询")
    @GetMapping(value = "/list")
    public AjaxResult<?> queryPageList(WjbdHsZskWj wjbdHsZskWj,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<WjbdHsZskWj> queryWrapper = QueryGenerator.initQueryWrapper(wjbdHsZskWj, null);
        Page<WjbdHsZskWj> page = new Page<WjbdHsZskWj>(pageNo, pageSize);
        IPage<WjbdHsZskWj> pageList = wjbdHsZskWjService.page(page, queryWrapper);
        return AjaxResult.OK(pageList);
    }

    /**
     * 分页列表查询
     *
     * @param wjbdHsZskWj
     * @param req         请求体
     * @return 结果
     */
    @AutoLog(value = "知识库文件-分页列表查询")
    @ApiOperation(value = "知识库文件-分页列表查询", notes = "知识库文件-分页列表查询")
    @GetMapping(value = "/listAll")
    public AjaxResult<?> listAll(WjbdHsZskWj wjbdHsZskWj,
                             HttpServletRequest req) {
        LambdaQueryWrapper<WjbdHsZskWj> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotEmpty(wjbdHsZskWj.getZskId()), WjbdHsZskWj::getZskId, wjbdHsZskWj.getZskId());
        queryWrapper.eq(ObjectUtil.isNotEmpty(wjbdHsZskWj.getWjMc()), WjbdHsZskWj::getWjMc, wjbdHsZskWj.getWjMc());
        List<WjbdHsZskWj> pageList = wjbdHsZskWjService.list(queryWrapper);
        return AjaxResult.OK(pageList);
    }

    /**
     * 添加
     *
     * @param wjbdHsZskWjList wjbdHsZskWjList
     * @return 结果
     */
    @AutoLog(value = "知识库文件-添加")
    @ApiOperation(value = "知识库文件-添加", notes = "知识库文件-添加")
    @PostMapping(value = "/add")
    public AjaxResult<?> add(@RequestBody List<WjbdHsZskWj> wjbdHsZskWjList) {
        return wjbdHsZskWjService.add(wjbdHsZskWjList);
    }

    /**
     * 编辑
     *
     * @param wjbdHsZskWj
     * @return 结果
     */
    @AutoLog(value = "知识库文件-编辑")
    @ApiOperation(value = "知识库文件-编辑", notes = "知识库文件-编辑")
    @PutMapping(value = "/edit")
    public AjaxResult<?> edit(@RequestBody WjbdHsZskWj wjbdHsZskWj) {
        wjbdHsZskWjService.updateById(wjbdHsZskWj);
        return AjaxResult.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id id
     * @return 结果
     */
    @AutoLog(value = "知识库文件-通过id删除")
    @ApiOperation(value = "知识库文件-通过id删除", notes = "知识库文件-通过id删除")
    @DeleteMapping(value = "/delete")
    public AjaxResult<?> delete(@RequestParam(name = "id", required = true) String id) {
        return wjbdHsZskWjService.delInfo(id);
    }

    /**
     * 批量删除
     *
     * @param ids 多个id
     * @return 结果
     */
    @AutoLog(value = "知识库文件-批量删除")
    @ApiOperation(value = "知识库文件-批量删除", notes = "知识库文件-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public AjaxResult<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.wjbdHsZskWjService.removeByIds(Arrays.asList(ids.split(",")));
        return AjaxResult.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return 结果
     */
    @AutoLog(value = "知识库文件-通过id查询")
    @ApiOperation(value = "知识库文件-通过id查询", notes = "知识库文件-通过id查询")
    @GetMapping(value = "/queryById")
    public AjaxResult<?> queryById(@RequestParam(name = "id", required = true) String id) {
        WjbdHsZskWj wjbdHsZskWj = wjbdHsZskWjService.getById(id);
        if (wjbdHsZskWj == null) {
            return AjaxResult.error("未找到对应数据");
        }
        return AjaxResult.OK(wjbdHsZskWj);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param wjbdHsZskWj
     */
    @GetMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, WjbdHsZskWj wjbdHsZskWj) {
        return super.exportXls(request, wjbdHsZskWj, WjbdHsZskWj.class, "知识库文件");
    }

    /**
     * 通过excel导入数据
     *
     * @param request  请求体
     * @param response 响应体
     * @return 结果
     */
    @PostMapping(value = "/importExcel")
    public AjaxResult<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, WjbdHsZskWj.class);
    }


}
