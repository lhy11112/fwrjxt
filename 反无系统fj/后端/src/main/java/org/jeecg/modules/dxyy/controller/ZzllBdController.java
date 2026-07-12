package org.jeecg.modules.dxyy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.constant.YhJbConstant;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.dxyy.entity.ZzllBd;
import org.jeecg.modules.dxyy.service.IZzllBdService;
import org.jeecg.modules.dxyy.util.FindFatherUnitByUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Description: zzll_bd
 * @Author: jeecg-boot
 * @Date: 2024-04-26
 *
 */
@Api(tags = "作战力量_部队")
@RestController
@RequestMapping("/dxyy/zzllBd")
@Slf4j
public class ZzllBdController extends JeecgController<ZzllBd, IZzllBdService> {
    @Autowired
    private IZzllBdService zzllBdService;
    @Autowired
    private CommonAPI commonApi;
    private List<ZzllBd> unitList = new CopyOnWriteArrayList<ZzllBd>();
    private List childtrees = new CopyOnWriteArrayList<>();
    private Set<String> childId = new HashSet<>();
    /**
     * 分页列表查询
     *
     * @param zzllBd
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "作战力量_部队-分页列表查询")
    @ApiOperation(value = "作战力量_部队-分页列表查询", notes = "作战力量_部队-分页列表查询")
    @GetMapping(value = "/list")
    public AjaxResult<?> queryPageList(ZzllBd zzllBd,
                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   HttpServletRequest req) {
        Map<String, String[]> parameterMap = req.getParameterMap();
        // 检查参数映射是否为空
        if (parameterMap == null || parameterMap.isEmpty()) {
            if (log.isInfoEnabled()){
                log.info("参数为空！！！");
            }
        }
        QueryWrapper<ZzllBd> queryWrapper = QueryGenerator.initQueryWrapper(zzllBd, parameterMap);
        Page<ZzllBd> page = new Page<ZzllBd>(pageNo, pageSize);
        IPage<ZzllBd> pageList = zzllBdService.page(page, queryWrapper);
        return AjaxResult.OK(pageList);
    }

    /**
     * 添加
     *
     * @param zzllBd
     * @return
     */
    @AutoLog(value = "作战力量_部队-添加")
    @ApiOperation(value = "作战力量_部队-添加", notes = "作战力量_部队-添加")
    @PostMapping(value = "/add")
    public AjaxResult<?> add(@RequestBody ZzllBd zzllBd) {
        zzllBdService.save(zzllBd);
        return AjaxResult.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param zzllBd
     * @return
     */
    @AutoLog(value = "作战力量_部队-编辑")
    @ApiOperation(value = "作战力量_部队-编辑", notes = "作战力量_部队-编辑")
    @PutMapping(value = "/edit")
    public AjaxResult<?> edit(@RequestBody ZzllBd zzllBd) {
        zzllBdService.updateById(zzllBd);
        return AjaxResult.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "作战力量_部队-通过id删除")
    @ApiOperation(value = "作战力量_部队-通过id删除", notes = "作战力量_部队-通过id删除")
    @DeleteMapping(value = "/delete")
    public AjaxResult<?> delete(@RequestParam(name = "id", required = true) String id) {
        zzllBdService.removeById(id);
        return AjaxResult.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "作战力量_部队-批量删除")
    @ApiOperation(value = "作战力量_部队-批量删除", notes = "作战力量_部队-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public AjaxResult<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        this.zzllBdService.removeByIds(Arrays.asList(ids.split(",")));
        return AjaxResult.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "作战力量_部队-通过id查询")
    @ApiOperation(value = "作战力量_部队-通过id查询", notes = "作战力量_部队-通过id查询")
    @GetMapping(value = "/queryById")
    public AjaxResult<?> queryById(@RequestParam(name = "id", required = true) String id) {
        ZzllBd zzllBd = zzllBdService.getById(id);
        if (zzllBd == null) {
            return AjaxResult.error("未找到对应数据");
        }
        return AjaxResult.OK(zzllBd);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param zzllBd
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, ZzllBd zzllBd) {
        return super.exportXls(request, zzllBd, ZzllBd.class, "作战力量_部队");
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
        return super.importExcel(request, response, ZzllBd.class);
    }

    /**
     * 获取当前用户单位所属总队
     */
    @AutoLog(value = "获取当前用户单位所属总队")
    @ApiOperation(value = "queryChildUnit-获取当前用户单位所属总队", notes = "queryChildUnit-获取当前用户单位所属总队")
    @GetMapping(value = "/queryZongdui")
    public AjaxResult<?> queryZongdui(HttpServletRequest req) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (user == null) {
            String username = getLoginUserByRequest(req);
            user = commonApi.getUserByName(username);
        }

        String bdnm = user.getDepartIds();
        //总队为最高级单位集合
        List<ZzllBd> unitListZd = zzllBdService.selectZongdui();
        FindFatherUnitByUser findFatherUnitByUser = new FindFatherUnitByUser();
        //unitListZdFather集合最后一位为总队级别信息
        List<ZzllBd> unitListZdFather = findFatherUnitByUser.findFatherUnit(bdnm, unitListZd);
        if (!unitListZdFather.isEmpty()) {
            return AjaxResult.OK(unitListZdFather.get(unitListZdFather.size() - 1).getBdnm());
        } else {
            return AjaxResult.OK(bdnm);
        }
    }

    /**
     * 获取当前用户单位所属总队下二级单位列表
     */
    @AutoLog(value = "获取当前用户单位所属总队下二级单位列表")
    @ApiOperation(value = "queryChildUnit-获取当前用户单位所属总队下二级单位列表", notes = "queryChildUnit-获取当前用户单位所属总队下二级单位列表")
    @GetMapping(value = "/queryZongduiEjList")
    public AjaxResult<?> queryZongduiEjList(String bdnm, HttpServletRequest req) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (user == null) {
            String username = getLoginUserByRequest(req);
            user = commonApi.getUserByName(username);
        }
        if (StringUtils.isNotBlank(bdnm)) {

        } else {
            bdnm = user.getDepartIds();
        }
        QueryWrapper<ZzllBd> queryWrappers = new QueryWrapper<>();
        queryWrappers.eq("bdnm", bdnm);
        List<ZzllBd> zzllBd= zzllBdService.list(queryWrappers);
        //所选用户是几级用户
        String yhJbNm = zzllBdService.getBdJbByBd(bdnm);
        //一二级账号登录显示总队、各支队情况
        if (yhJbNm.equals(YhJbConstant.YHJB_01) || yhJbNm.equals(YhJbConstant.YHJB_02)) {
                QueryWrapper<ZzllBd> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("LEFT( bdxh, LENGTH( bdxh ) - 2 )", zzllBd.get(0).getBdxh());
                queryWrapper.notLike("BDJC","总队机关");
                queryWrapper.orderByAsc("bdnm");
                return AjaxResult.OK(zzllBdService.list(queryWrapper));
        }
        //三、四、五级账号登录显示下级单位情况
        QueryWrapper<ZzllBd> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("LEFT( bdxh, LENGTH( bdxh ) - 2 )", zzllBd.get(0).getBdxh()).or().eq("BDNM",bdnm);
        queryWrapper.orderByAsc("bdnm");
        return AjaxResult.OK(zzllBdService.list(queryWrapper));
    }

    /**
     * 获取当前用户单位的子单位
     */
    @AutoLog(value = "获取当前用户单位的子单位",YYMK="系统管理")
    @ApiOperation(value = "获取当前用户单位的子单位", notes = "获取当前用户单位的子单位")
    @GetMapping(value = "/queryChildUnit")
    public AjaxResult<?> queryChildUnit(HttpServletRequest req,String bdnm) {
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (user == null) {
            String username = getLoginUserByRequest(req);
            user = commonApi.getUserByName(username);
        }
        bdnm=StringEscapeUtils.escapeHtml4(bdnm);
        return zzllBdService.queryChildUnit(StringUtils.isNotBlank(bdnm)?bdnm:user.getDepartIds(),req);
    }

    /**
     * 获取所有单位树结构
     */
    @AutoLog(value = "获取所有单位树结构")
    @ApiOperation(value = "queryChildUnitAll-获取所有单位树结构", notes = "queryChildUnitAll-获取所有单位树结构")
    @GetMapping(value = "/queryChildUnitAll")
    public AjaxResult<?> queryChildUnitAll() {
        unitList.clear();
        childId.clear();
        unitList = zzllBdService.selectAllUnit();
        List<ZzllBd> tree = new CopyOnWriteArrayList<>();
        if (!unitList.isEmpty()) {
            for (ZzllBd zdbc : unitList) {
                ZzllBd item = zdbc;
                item = zzllBdService.buidlChildTree(zdbc,unitList);
                tree.add(item);
            }
            return AjaxResult.OK(tree);
        } else {
            return AjaxResult.OK(new CopyOnWriteArrayList<>());
        }
    }
    /**
     * 获取用户名
     *
     * @param request
     * @return
     */
    public static String getLoginUserByRequest(HttpServletRequest request) {
        String username = JwtUtil.getUserNameByToken(request);
        return username;
    }

}
