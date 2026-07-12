package org.jeecg.modules.wrj.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.wrj.entity.WjbdHsZsk;
import org.jeecg.modules.wrj.service.IWjbdHsZskService;
import org.jeecg.modules.wrj.service.IWjbdHsZskWjService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 知识库
 * @Author: jeecg-boot
 * @Date: 2024-09-14
 */
@Api(tags = "知识库")
@RestController
@RequestMapping("/wjfjsjzt/wjbdHsZsk")
@Slf4j
public class WjbdHsZskController {
    @Autowired
    private IWjbdHsZskService wjbdHsZskService;
    @Autowired
    private IWjbdHsZskWjService wjbdHsZskWjService;
    /**
     * 分页列表查询
     *
     * @param wjbdHsZsk
     * @param pageNo    页码
     * @param pageSize  每页条数
     * @param req       请求体
     * @return 结果
     */
    @AutoLog(value = "知识库-分页列表查询")
    @ApiOperation(value = "知识库-分页列表查询", notes = "知识库-分页列表查询")
    @GetMapping(value = "/list")
    public AjaxResult<?> queryPageList(WjbdHsZsk wjbdHsZsk,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        QueryWrapper<WjbdHsZsk> queryWrapper = QueryGenerator.initQueryWrapper(wjbdHsZsk, null);
        Page<WjbdHsZsk> page = new Page<WjbdHsZsk>(pageNo, pageSize);
        IPage<WjbdHsZsk> pageList = wjbdHsZskService.page(page, queryWrapper);
        pageList.getRecords().stream().forEach(item->{
            Map<String,Object> objectMap=new HashMap<String,Object>();
            objectMap.put("ZSK_ID",item.getId());
            item.setWjbdHsZskWj(wjbdHsZskWjService.listByMap(objectMap));
        });
        return AjaxResult.OK(pageList);
    }


    /**
     * 分页列表查询
     *
     * @param wjbdHsZsk wjbdHsZsk
     * @param req       请求体
     * @return 结果
     */
    @AutoLog(value = "知识库-分页列表查询")
    @ApiOperation(value = "知识库-分页列表查询", notes = "知识库-分页列表查询")
    @GetMapping(value = "/listAll")
    public AjaxResult<?> listAll(WjbdHsZsk wjbdHsZsk, HttpServletRequest req) {
        List<WjbdHsZsk> pageList = wjbdHsZskService.list();
        return AjaxResult.OK(pageList);
    }


    /**
     * 添加
     *
     * @param wjbdHsZsk wjbdHsZsk
     * @return 结果
     */
    @AutoLog(value = "知识库-添加")
    @ApiOperation(value = "知识库-添加", notes = "知识库-添加")
    @PostMapping(value = "/add")
    public AjaxResult<?> add(@RequestBody WjbdHsZsk wjbdHsZsk) {
        return wjbdHsZskService.add(wjbdHsZsk);
    }

    /**
     * 编辑
     *
     * @param wjbdHsZsk
     * @return 结果
     */
    @AutoLog(value = "知识库-编辑")
    @ApiOperation(value = "知识库-编辑", notes = "知识库-编辑")
    @PutMapping(value = "/edit")
    public AjaxResult<?> edit(@RequestBody WjbdHsZsk wjbdHsZsk) {
        wjbdHsZskService.updateById(wjbdHsZsk);
        return AjaxResult.OK("编辑成功!");
    }

    /**
     * 修改名称
     *
     * @param wjbdHsZsk
     * @return 结果
     */
    @AutoLog(value = "知识库-编辑")
    @ApiOperation(value = "知识库-编辑", notes = "知识库-编辑")
    @PutMapping(value = "/editZskMc")
    public AjaxResult<?> editZskMc(@RequestBody WjbdHsZsk wjbdHsZsk) {
        return wjbdHsZskService.editZskMc(wjbdHsZsk);
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return 结果
     */
    @AutoLog(value = "知识库-通过id删除")
    @ApiOperation(value = "知识库-通过id删除", notes = "知识库-通过id删除")
    @DeleteMapping(value = "/delete")
    public AjaxResult<?> delete(@RequestParam(name = "id", required = true) String id) {
        wjbdHsZskService.removeById(id);
        return AjaxResult.OK();
    }

    /**
     * 批量删除
     *
     * @param ids 多个id
     * @return 结果
     */
    @AutoLog(value = "知识库-批量删除")
    @ApiOperation(value = "知识库-批量删除", notes = "知识库-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public AjaxResult<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        return wjbdHsZskService.deleteBatch(ids);
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return 结果
     */
    @AutoLog(value = "知识库-通过id查询")
    @ApiOperation(value = "知识库-通过id查询", notes = "知识库-通过id查询")
    @GetMapping(value = "/queryById")
    public AjaxResult<?> queryById(@RequestParam(name = "id", required = true) String id) {
        WjbdHsZsk wjbdHsZsk = wjbdHsZskService.getById(id);
        if (wjbdHsZsk == null) {
            return AjaxResult.error("未找到对应数据");
        }
        return AjaxResult.OK(wjbdHsZsk);
    }


    /**
     * 通过id查询
     *
     * @param zskNm
     * @return 结果
     */
    @AutoLog(value = "知识库-通过zskNm查询")
    @ApiOperation(value = "知识库-通过zskNm查询", notes = "知识库-通过zskNm查询")
    @GetMapping(value = "/queryByBm")
    public AjaxResult<?> queryByBm(@RequestParam(name = "zskNm", required = true) String zskNm) {
        LambdaQueryWrapper<WjbdHsZsk> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WjbdHsZsk::getZskNm, zskNm);
        queryWrapper.last("LIMIT 1");
        WjbdHsZsk wjbdHsZsk = wjbdHsZskService.getOne(queryWrapper);
        if (wjbdHsZsk == null) {
            return AjaxResult.error("未找到对应数据");
        }
        return AjaxResult.OK(wjbdHsZsk);
    }

}
