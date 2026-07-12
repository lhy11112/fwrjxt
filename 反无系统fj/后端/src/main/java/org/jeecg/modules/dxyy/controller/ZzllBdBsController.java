package org.jeecg.modules.dxyy.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.wwct.CentralPlatformUtil;
import org.jeecg.modules.base.mapper.BaseCommonMapper;
import org.jeecg.modules.dxyy.entity.ZzllBdBs;
import org.jeecg.modules.dxyy.entity.dto.BdBsDto;
import org.jeecg.modules.dxyy.service.IZzllBdBsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @Description: zzll_bd_bs
 * @Author: jeecg-boot
 * @Date: 2024-04-26
 *  
 */
@Api(tags = "作战力量_部队_部署")
@RestController
@RequestMapping("/dxyy/zzllBdBs")
@Slf4j
public class ZzllBdBsController extends JeecgController<ZzllBdBs, IZzllBdBsService> {
    @Autowired
    private IZzllBdBsService zzllBdBsService;
    @Autowired
    private CentralPlatformUtil centralPlatformUtil;
    @Resource
    private BaseCommonMapper baseCommonMapper;
    /**
     * 分页列表查询
     *
     * @param zzllBdBs
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "作战力量_部队_部署-分页列表查询")
    @ApiOperation(value = "作战力量_部队_部署-分页列表查询", notes = "作战力量_部队_部署-分页列表查询")
    @GetMapping(value = "/list")
    public AjaxResult<?> queryPageList(ZzllBdBs zzllBdBs,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) throws IllegalAccessException {
        Map<String, String[]> parameterMap = req.getParameterMap();
        // 检查参数映射是否为空
        if (parameterMap == null || parameterMap.isEmpty()) {
            if (log.isInfoEnabled()){
                log.info("参数为空！！！");
            }
        }
        QueryWrapper<ZzllBdBs> queryWrapper = QueryGenerator.initQueryWrapper(zzllBdBs, parameterMap);
        Page<ZzllBdBs> page = new Page<ZzllBdBs>(pageNo, pageSize);
        IPage<ZzllBdBs> pageList = zzllBdBsService.page(page, queryWrapper);
        return AjaxResult.OK(pageList);
    }

    /**
     * 部队部署位置列表查询
     *
     * @return
     */
    @AutoLog(value = "作战力量_部队_部署-分页列表查询")
    @ApiOperation(value = "作战力量_部队_部署-分页列表查询", notes = "作战力量_部队_部署-分页列表查询")
    @GetMapping(value = "/listBdBs")
    public AjaxResult<?> listBdBs() throws IllegalAccessException {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        BdBsDto bdBsDto = new BdBsDto();
        bdBsDto.setBdnm(user.getDepartIds());
        JSONObject jsonObject = new JSONObject();
            if (true) {
            Map<String, Object> objectMap = new HashMap<String, Object>(10);
            objectMap.put("bdnm", user.getDepartIds());
            jsonObject.put("records", zzllBdBsService.listByMap(objectMap));
        } else {
        }
        return AjaxResult.OK(jsonObject);
    }

    /**
     * 添加
     *
     * @param zzllBdBs
     * @return
     */
    @AutoLog(value = "作战力量_部队_部署-添加")
    @ApiOperation(value = "作战力量_部队_部署-添加", notes = "作战力量_部队_部署-添加")
    @PostMapping(value = "/add")
    public AjaxResult<?> add(@RequestBody ZzllBdBs zzllBdBs) {
        zzllBdBsService.save(zzllBdBs);
        return AjaxResult.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param zzllBdBs
     * @return
     */
    @AutoLog(value = "作战力量_部队_部署-编辑")
    @ApiOperation(value = "作战力量_部队_部署-编辑", notes = "作战力量_部队_部署-编辑")
    @PutMapping(value = "/edit")
    public AjaxResult<?> edit(@RequestBody ZzllBdBs zzllBdBs) {
        zzllBdBsService.updateById(zzllBdBs);
        return AjaxResult.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "作战力量_部队_部署-通过id删除")
    @ApiOperation(value = "作战力量_部队_部署-通过id删除", notes = "作战力量_部队_部署-通过id删除")
    @DeleteMapping(value = "/delete")
    public AjaxResult<?> delete(@RequestParam(name = "id", required = true) String id) {
        zzllBdBsService.removeById(id);
        return AjaxResult.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "作战力量_部队_部署-批量删除")
    @ApiOperation(value = "作战力量_部队_部署-批量删除", notes = "作战力量_部队_部署-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public AjaxResult<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.zzllBdBsService.removeByIds(Arrays.asList(ids.split(",")));
        return AjaxResult.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "作战力量_部队_部署-通过id查询")
    @ApiOperation(value = "作战力量_部队_部署-通过id查询", notes = "作战力量_部队_部署-通过id查询")
    @GetMapping(value = "/queryById")
    public AjaxResult<?> queryById(@RequestParam(name = "id", required = true) String id) {
        ZzllBdBs zzllBdBs = zzllBdBsService.getById(id);
        if (zzllBdBs == null) {
            return AjaxResult.error("未找到对应数据");
        }
        return AjaxResult.OK(zzllBdBs);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param zzllBdBs
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ZzllBdBs zzllBdBs) {
        return super.exportXls(request, zzllBdBs, ZzllBdBs.class, "作战力量_部队_部署");
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
        return super.importExcel(request, response, ZzllBdBs.class);
    }
}
