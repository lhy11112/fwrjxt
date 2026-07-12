package org.jeecg.modules.uav.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.uav.entity.UavConnectLog;
import org.jeecg.modules.uav.entity.UavDeviceConfig;
import org.jeecg.modules.uav.entity.UavOperateLog;
import org.jeecg.modules.uav.service.IUavConnectLogService;
import org.jeecg.modules.uav.service.IUavDeviceConfigService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.uav.service.IUavOperateLogService;
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
 * @Description: 设备管理
 * @Author: jeecg-boot
 * @Date: 2025-09-15
 * @Version: V1.0
 */
@Api(tags = "设备管理")
@RestController
@RequestMapping("/uav/uavDeviceConfig")
@Slf4j
public class UavDeviceConfigController extends JeecgController<UavDeviceConfig, IUavDeviceConfigService> {
    @Autowired
    private IUavDeviceConfigService uavDeviceConfigService;
    @Autowired
    private IUavOperateLogService uavOperateLogService;
    @Autowired
    private IUavConnectLogService uavConnectLogService;

    /**
     * 分页列表查询
     *
     * @param uavDeviceConfig
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "设备管理-分页列表查询", YYMK = "设备管理")
    @ApiOperation(value = "设备管理-分页列表查询", notes = "设备管理-分页列表查询")
    @GetMapping(value = "/list")
    public AjaxResult<?> queryPageList(UavDeviceConfig uavDeviceConfig,
                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                       HttpServletRequest req) {
        QueryWrapper<UavDeviceConfig> queryWrapper = QueryGenerator.initQueryWrapper(uavDeviceConfig, req.getParameterMap());
        Page<UavDeviceConfig> page = new Page<UavDeviceConfig>(pageNo, pageSize);
        //queryWrapper.notIn("device_type","System");
        queryWrapper.orderByDesc("update_time");
        IPage<UavDeviceConfig> pageList = uavDeviceConfigService.page(page, queryWrapper);
        return AjaxResult.OK(pageList);
    }

    /**
     * 分页列表查询
     *
     * @param uavDeviceConfig
     * @param pageNo
     * @param pageSize
     * @param req
     * @return
     */
    @AutoLog(value = "设备管理-分页列表查询", YYMK = "设备管理")
    @ApiOperation(value = "设备管理-分页列表查询", notes = "设备管理-分页列表查询")
    @GetMapping(value = "/listAll")
    public AjaxResult<?> queryPageListAll(UavDeviceConfig uavDeviceConfig,
                                          @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                          @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                          HttpServletRequest req) {
        QueryWrapper<UavDeviceConfig> queryWrapper = QueryGenerator.initQueryWrapper(uavDeviceConfig, req.getParameterMap());
        Page<UavDeviceConfig> page = new Page<UavDeviceConfig>(pageNo, pageSize);
        queryWrapper.orderByDesc("update_time");
        IPage<UavDeviceConfig> pageList = uavDeviceConfigService.page(page, queryWrapper);
        return AjaxResult.OK(pageList);
    }

    /**
     * 添加
     *
     * @param uavDeviceConfig
     * @return
     */
    @AutoLog(value = "设备管理-添加", YYMK = "设备管理")
    @ApiOperation(value = "设备管理-添加", notes = "设备管理-添加")
    @PostMapping(value = "/add")
    public AjaxResult<?> add(@RequestBody UavDeviceConfig uavDeviceConfig) {
        uavDeviceConfigService.save(uavDeviceConfig);
        return AjaxResult.OK("添加成功！");
    }

    /**
     * 编辑
     *
     * @param uavDeviceConfig
     * @return
     */
    @AutoLog(value = "设备管理-编辑", YYMK = "设备管理")
    @ApiOperation(value = "设备管理-编辑", notes = "设备管理-编辑")
    @PutMapping(value = "/edit")
    public AjaxResult<?> edit(@RequestBody UavDeviceConfig uavDeviceConfig) {
        uavDeviceConfigService.updateById(uavDeviceConfig);
        return AjaxResult.OK("编辑成功!");
    }

    /**
     * 通过id删除
     *
     * @param id
     * @return
     */
    @AutoLog(value = "设备管理-通过id删除", YYMK = "设备管理")
    @ApiOperation(value = "设备管理-通过id删除", notes = "设备管理-通过id删除")
    @DeleteMapping(value = "/delete")
    public AjaxResult<?> delete(@RequestParam(name = "id", required = true) String id) {
        UavDeviceConfig uavDeviceConfig = uavDeviceConfigService.getById(id);
        uavDeviceConfigService.removeById(id);
        //删除设备操作日志及连接日志信息
        uavOperateLogService.remove(new QueryWrapper<UavOperateLog>().eq("station_id", uavDeviceConfig.getStationId()));
        uavConnectLogService.remove(new QueryWrapper<UavConnectLog>().eq("station_id", uavDeviceConfig.getStationId()));
        return AjaxResult.OK("删除成功!");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @AutoLog(value = "设备管理-批量删除", YYMK = "设备管理")
    @ApiOperation(value = "设备管理-批量删除", notes = "设备管理-批量删除")
    @DeleteMapping(value = "/deleteBatch")
    public AjaxResult<?> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
        List<UavDeviceConfig> uavDeviceConfigs = this.uavDeviceConfigService.getBaseMapper().selectBatchIds(Arrays.asList(ids.split(",")));
        // 转换为 stationId 的 List
        List<Integer> stationIds = uavDeviceConfigs.stream()
                .map(UavDeviceConfig::getStationId)
                .collect(Collectors.toList());
        this.uavDeviceConfigService.removeByIds(Arrays.asList(ids.split(",")));
        //删除设备操作日志及连接日志信息
        uavOperateLogService.remove(new QueryWrapper<UavOperateLog>().in("station_id", stationIds));
        uavConnectLogService.remove(new QueryWrapper<UavConnectLog>().in("station_id", stationIds));
        return AjaxResult.OK("批量删除成功!");
    }

    /**
     * 通过id查询
     *
     * @param id
     * @return
     */
    @AutoLog(value = "设备管理-通过id查询", YYMK = "设备管理")
    @ApiOperation(value = "设备管理-通过id查询", notes = "设备管理-通过id查询")
    @GetMapping(value = "/queryById")
    public AjaxResult<?> queryById(@RequestParam(name = "id", required = true) String id) {
        UavDeviceConfig uavDeviceConfig = uavDeviceConfigService.getById(id);
        if (uavDeviceConfig == null) {
            return AjaxResult.error("未找到对应数据");
        }
        return AjaxResult.OK(uavDeviceConfig);
    }

    /**
     * 导出excel
     *
     * @param request
     * @param uavDeviceConfig
     */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, UavDeviceConfig uavDeviceConfig) {
        return super.exportXls(request, uavDeviceConfig, UavDeviceConfig.class, "设备管理");
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
        return super.importExcel(request, response, UavDeviceConfig.class);
    }

    /**
     * 诱骗设备统计
     */
    @AutoLog(value = "设备管理-诱骗设备统计", YYMK = "设备管理")
    @ApiOperation(value = "设备管理-诱骗设备统计", notes = "设备管理-诱骗设备统计")
    @GetMapping(value = "/getypsbCount")
    public AjaxResult<?> getypsbCount() {
        List<Map<String, Object>> list = uavDeviceConfigService.getypsbCount();
        return AjaxResult.OK(list);
    }

    /**
     * 干扰设备统计
     */
    @AutoLog(value = "设备管理-干扰设备统计", YYMK = "设备管理")
    @ApiOperation(value = "设备管理-干扰设备统计", notes = "设备管理-干扰设备统计")
    @GetMapping(value = "/getgrsbCount")
    public AjaxResult<?> getgrsbCount() {
        List<Map<String, Object>> list = uavDeviceConfigService.getgrsbCount();
        return AjaxResult.OK(list);
    }


    /**
     * 干扰设备统计
     */
    @AutoLog(value = "设备管理-侦测设备统计", YYMK = "设备管理")
    @ApiOperation(value = "设备管理-侦测设备统计", notes = "设备管理-侦测设备统计")
    @GetMapping(value = "/getzcsbCount")
    public AjaxResult<?> getzcsbCount() {
        List<Map<String, Object>> list = uavDeviceConfigService.getzcsbCount();
        return AjaxResult.OK(list);
    }

    /**
     * 设备统计
     */
    @AutoLog(value = "设备管理-设备统计", YYMK = "设备管理")
    @ApiOperation(value = "设备管理-设备统计", notes = "设备管理-设备统计")
    @GetMapping(value = "/getSbCountByLx")
    public AjaxResult<?> getSbCountByLx(@RequestParam("type") String type) {
        Map<String, Object> map = uavDeviceConfigService.getSbCountByLx(type);
        return AjaxResult.OK(map);
    }
}
