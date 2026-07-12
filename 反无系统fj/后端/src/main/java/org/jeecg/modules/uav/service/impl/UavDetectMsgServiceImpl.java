package org.jeecg.modules.uav.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.uav.dto.ModelSerialDTO;
import org.jeecg.modules.uav.entity.UavDetectMsg;
import org.jeecg.modules.uav.entity.UavDfData;
import org.jeecg.modules.uav.mapper.UavDetectMsgMapper;
import org.jeecg.modules.uav.service.IUavDetectMsgService;
import org.jeecg.modules.uav.util.DroneTrajectoryGenerator;
import org.jeecg.modules.uav.util.GenerateFlightRouteUtils;
import org.jeecg.modules.uav.util.UavDatectMsgDto;
import org.jeecg.modules.uav.util.UavDatectMsgDto1;
import org.jeecg.modules.uav.vo.DronePositions;
import org.jeecg.modules.uav.vo.UavDetectMsgDateVo;
import org.jeecg.modules.uav.vo.UavDetectMsgVo;
import org.jeecg.modules.uav.vo.UavDetectMsgVo1;
import org.jeecg.modules.wrj.entity.WjbdWrjJbxx;
import org.jeecg.modules.wrj.service.IWjbdWrjJbxxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.util.CollectionUtils;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * @Description: 侦测报文数据表-飞行数据
 * @Author: jeecg-boot
 * @Date: 2025-09-15
 * @Version: V1.0
 */
@Service
@Slf4j
public class UavDetectMsgServiceImpl extends ServiceImpl<UavDetectMsgMapper, UavDetectMsg> implements IUavDetectMsgService {
    @Autowired
    private IWjbdWrjJbxxService wjbdWrjJbxxService;

    @Override
    public List<UavDetectMsgVo> getUavDetectMsgByStationId(String StationId, String rq,String authStatus) {
        // 1. 第一步：获取基础列表数据（原逻辑的1次查询）
        List<UavDetectMsgVo> uavDetectMsgVoList = baseMapper.getUavDetectMsgByStationId(StationId, rq,authStatus);

        // 边界处理：如果列表为空，直接返回，避免后续无效操作
        if (CollectionUtils.isEmpty(uavDetectMsgVoList)) {
            return Collections.emptyList();
        }

        // 2. 第二步：提取所有需要查询的model和serial，组装成批量查询的参数
        List<ModelSerialDTO> modelSerialKeys=new ArrayList<ModelSerialDTO>();
        // 构建唯一标识key：model_serial，避免重复查询
        for (UavDetectMsgVo item : uavDetectMsgVoList) {
            // 非空校验，避免空值导致的异常
            if (StringUtils.isNotBlank(item.getModel()) && StringUtils.isNotBlank(item.getSerial())) {
                modelSerialKeys.add(new ModelSerialDTO().setModelClean(item.getModel().replaceAll("\\s", "").replaceAll("_", "")).setSerialClean(item.getSerial().replaceAll("\\s", "").replaceAll("_", "")));
            }
        }



        // 3. 第三步：批量查询所有需要的UavDetectMsg数据（仅1次数据库查询）
        // 注意：需要先在mapper中新增批量查询方法
        List<UavDetectMsg> uavDetectMsgList = baseMapper.batchGetUavDetectMsgByModelSerialRq(
                modelSerialKeys, rq
        );

        // 4. 第四步：将批量查询结果封装成Map，便于快速匹配（内存操作，效率极高）
        Map<String, UavDetectMsg> uavDetectMsgMap = new HashMap<>();
        for (UavDetectMsg msg : uavDetectMsgList) {
            if (msg != null && StringUtils.isNotBlank(msg.getModel()) && StringUtils.isNotBlank(msg.getSerial())) {
                String key = msg.getModel().replaceAll("\\s", "").replaceAll("_", "") + "_" + msg.getSerial().replaceAll("\\s", "").replaceAll("_", "");
                uavDetectMsgMap.put(key, msg);
            }
        }

        // 5. 第五步：遍历原列表，从Map中匹配数据并赋值（内存操作）
        for (UavDetectMsgVo item : uavDetectMsgVoList) {
            if (StringUtils.isNotBlank(item.getModel()) && StringUtils.isNotBlank(item.getSerial())) {
                String key = item.getModel().replaceAll("\\s", "").replaceAll("_", "") + "_" + item.getSerial().replaceAll("\\s", "").replaceAll("_", "");
                UavDetectMsg uavDetectMsg = uavDetectMsgMap.get(key);

                if (uavDetectMsg != null && StringUtils.isNotBlank(uavDetectMsg.getJmlx())) {
                    item.setUavDetectMsg(uavDetectMsg);
                    item.setJmlx(uavDetectMsg.getJmlx());
                    item.setWxdj(uavDetectMsg.getWxdj());
                }
            }
        }
        return uavDetectMsgVoList;
    }

    @Override
    public UavDetectMsgVo1 getUavDetectMsgByModelSerialRq(String model, String serial, String rq) {
        UavDetectMsgVo1 uavDetectMsgVo1 = new UavDetectMsgVo1();
        uavDetectMsgVo1.setUavDetectMsgList(baseMapper.getUavDetectMsgByModelSerialRq(model, serial, rq));
        QueryWrapper<WjbdWrjJbxx> wjbdWrjJbxxWrapper = new QueryWrapper();
        wjbdWrjJbxxWrapper.eq("model", model);
        wjbdWrjJbxxWrapper.eq("serial_number", serial);
        WjbdWrjJbxx wjbdWrjJbxxes = wjbdWrjJbxxService.getOne(wjbdWrjJbxxWrapper);
        uavDetectMsgVo1.setWjbdWrjJbxx(wjbdWrjJbxxes);
        return uavDetectMsgVo1;
    }

    @Override
    public boolean generateFlightRoute(UavDatectMsgDto1 uavDatectMsgDto) {
//        List<DronePositions> dronePositions=  GenerateFlightRouteUtils.generateFlightRoute(uavDatectMsgDto.getStartLon(),uavDatectMsgDto.getStartLat()
//        ,uavDatectMsgDto.getStartAlt(),uavDatectMsgDto.getEndLon(),uavDatectMsgDto.getEndLat(),uavDatectMsgDto.getEndAlt()
//        ,uavDatectMsgDto.getTotalTimeSeconds());
        // 无人机参数：最大速度10m/s，加速度2m/s²，转弯半径20m
        DroneTrajectoryGenerator.DroneParameters params = new DroneTrajectoryGenerator.DroneParameters(10, 2, 20);
        // 设置开始和结束时间（示例：从现在开始，10分钟后结束）
        Date startTime = new Date();
        Date endTime = new Date(startTime.getTime() + 50 * 60 * 1000); // 10分钟后
        DroneTrajectoryGenerator.Waypoint start = new DroneTrajectoryGenerator.Waypoint(uavDatectMsgDto.getStart().getLongitude(), uavDatectMsgDto.getStart().getLatitude(),
                uavDatectMsgDto.getStart().getAltitude(), uavDatectMsgDto.getStart().getStayTime());
        List<DroneTrajectoryGenerator.Waypoint> waypoints = new ArrayList<>();
        if (uavDatectMsgDto.getWaypoints() != null) {
            uavDatectMsgDto.getWaypoints().stream().forEach(item -> {
                DroneTrajectoryGenerator.Waypoint waypoint = new DroneTrajectoryGenerator.Waypoint(item.getLongitude(), item.getLatitude(),
                        item.getAltitude(), item.getStayTime());
                waypoints.add(waypoint);
            });
        }
        DroneTrajectoryGenerator.Waypoint end = new DroneTrajectoryGenerator.Waypoint(uavDatectMsgDto.getEnd().getLongitude(), uavDatectMsgDto.getEnd().getLatitude(),
                uavDatectMsgDto.getEnd().getAltitude(), uavDatectMsgDto.getEnd().getStayTime());
        // 生成轨迹
        List<DroneTrajectoryGenerator.TrajectoryPoint> trajectory = DroneTrajectoryGenerator.generateTrajectory(
                start, waypoints, end, startTime, endTime, params);


        QueryWrapper<UavDetectMsg> uavDetectMsgQueryWrapper = new QueryWrapper();
        if (uavDatectMsgDto.getStationId() > 0 && uavDatectMsgDto.getStationId() != null) {
            uavDetectMsgQueryWrapper.eq("station_id", uavDatectMsgDto.getStationId());
        }
        uavDetectMsgQueryWrapper.eq("model", uavDatectMsgDto.getModel());
        uavDetectMsgQueryWrapper.eq("serial", uavDatectMsgDto.getSerial());
        if (StringUtils.isNotBlank(uavDatectMsgDto.getRq())) {
            uavDetectMsgQueryWrapper.eq("DATE_FORMAT(data_time, '%Y-%m-%d')", uavDatectMsgDto.getRq());
        }
        int is_delete = this.baseMapper.delete(uavDetectMsgQueryWrapper);
        if (is_delete > 0) {
            log.info("删除数据成功！！！！");
        }
        List<UavDetectMsg> uavDetectMsgList = new ArrayList<>();
        trajectory.stream().forEach(item -> {

            UavDetectMsg uavDetectMsg = new UavDetectMsg();
            uavDetectMsg.setStationId(uavDatectMsgDto.getStationId());
            uavDetectMsg.setModel(uavDatectMsgDto.getModel());
            uavDetectMsg.setSerial(uavDatectMsgDto.getSerial());
            uavDetectMsg.setDronLng(item.longitude);
            uavDetectMsg.setDronLat(item.latitude);
            uavDetectMsg.setHeight(item.altitude);
            uavDetectMsg.setDataTime(item.time);
            uavDetectMsg.setAltitude(item.altitude);
            uavDetectMsg.setCreateTime(new Date());
            uavDetectMsgList.add(uavDetectMsg);
        });
        return this.saveBatch(uavDetectMsgList);
    }

    @Override
    public List<Map<String, Object>> wrjfxcstj(String type, String stationId, String nf, String yf) {
        return baseMapper.wrjfxcstj(type, stationId, nf, yf);
    }

    @Override
    public IPage<UavDetectMsg> pageList(Page<UavDetectMsg> page, String type, String stationId, String nf, String yf, String brand, String model,String serial) {
        return baseMapper.pageList(page, type, stationId, nf, yf, brand, model,serial);
    }

    /**
     * 根据年度月份查询日历数据
     * @param nf 年份
     * @param yf 月份
     * @return
     */
    @Override
    public List<UavDetectMsgDateVo> getUavDetectMsgDateByNfYf(String nf, String yf) {
        return baseMapper.getUavDetectMsgDateByNfYf(nf,yf);
    }
}
