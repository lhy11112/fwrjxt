package org.jeecg.modules.wrj.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.uav.entity.UavDetectMsg;
import org.jeecg.modules.uav.util.DroneTrajectoryGenerator;
import org.jeecg.modules.uav.util.DroneTrajectoryGenerators;
import org.jeecg.modules.uav.util.UavDatectMsgDto1;
import org.jeecg.modules.uav.util.tysjsc;
import org.jeecg.modules.wrj.entity.WjbdWrjTyjh;
import org.jeecg.modules.wrj.entity.WjbdWrjTyjhData;
import org.jeecg.modules.wrj.mapper.WjbdWrjTyjhDataMapper;
import org.jeecg.modules.wrj.mapper.WjbdWrjTyjhMapper;
import org.jeecg.modules.wrj.service.IWjbdWrjTyjhDataService;
import org.jeecg.modules.wrj.service.IWjbdWrjTyjhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description: 武警部队_无人机_推演计划
 * @Author: jeecg-boot
 * @Date: 2025-09-22
 * @Version: V1.0
 */
@Service
@Slf4j
public class WjbdWrjTyjhServiceImpl extends ServiceImpl<WjbdWrjTyjhMapper, WjbdWrjTyjh> implements IWjbdWrjTyjhService {


    @Autowired
    private IWjbdWrjTyjhDataService iWjbdWrjTyjhDataService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void TYJHSC(WjbdWrjTyjh wjbdWrjTyjh) {
        // 4. 轨迹开始时间：当前系统时间
        Date startTime = new Date();
        wjbdWrjTyjh.setTyzt("未开始");
        wjbdWrjTyjh.setJhks(startTime);
        wjbdWrjTyjh.setJhcs(new Gson().toJson(wjbdWrjTyjh.getUavDatectMsgDto1()));
        DroneTrajectoryGenerator.DroneParameters params = new DroneTrajectoryGenerator.DroneParameters(10, 2, 20);
        // 设置开始和结束时间（示例：从现在开始，10分钟后结束）
        // 1. 定义航点：起点 + 多个途经点 + 终点（可任意增删途经点）
        DroneTrajectoryGenerators.Waypoint start = new DroneTrajectoryGenerators.Waypoint(wjbdWrjTyjh.getUavDatectMsgDto1().getStart().getLongitude(), wjbdWrjTyjh.getUavDatectMsgDto1().getStart().getLatitude(), wjbdWrjTyjh.getUavDatectMsgDto1().getStart().getAltitude());          // 起点
        DroneTrajectoryGenerators.Waypoint end = new DroneTrajectoryGenerators.Waypoint(wjbdWrjTyjh.getUavDatectMsgDto1().getEnd().getLongitude(), wjbdWrjTyjh.getUavDatectMsgDto1().getEnd().getLatitude(), wjbdWrjTyjh.getUavDatectMsgDto1().getEnd().getAltitude());            // 终点
        // 途经点列表（可任意增删，代码自动适配）
        List<DroneTrajectoryGenerators.Waypoint> waypoints = new ArrayList<>();
        wjbdWrjTyjh.getUavDatectMsgDto1().getWaypoints().stream().forEach(item -> {
            DroneTrajectoryGenerators.Waypoint waypoint = new DroneTrajectoryGenerators.Waypoint(item.getLongitude(), item.getLatitude(), item.getAltitude(), item.getStayTime());
            waypoints.add(waypoint);
        });
        // 2. 无人机基础性能参数：最大加速度2m/s²，最小转弯半径20m
        DroneTrajectoryGenerators.DroneParameters droneParams = new DroneTrajectoryGenerators.DroneParameters(2, 20);

        // 3. 核心：多航段速度配置（规则：速度列表长度 = 途经点数量 + 1）
        // 本次示例：3个途经点 → 4个航段 → 速度列表长度为4
        // 航段1：起点→途1，航段2：途1→途2，航段3：途2→途3，航段4：途3→终点
        List<Double> segmentSpeeds = wjbdWrjTyjh.getUavDatectMsgDto1().getSegmentSpeeds();
        DroneTrajectoryGenerators.MultiSegmentSpeedConfig speedConfig = new DroneTrajectoryGenerators.MultiSegmentSpeedConfig(segmentSpeeds);


        // 5. 生成多航段平滑轨迹
        List<DroneTrajectoryGenerators.TrajectoryPoint> trajectory = DroneTrajectoryGenerators.generateTrajectory(
                start, waypoints, end, startTime, speedConfig, droneParams
        );
        //List<DroneTrajectoryGenerators.TrajectoryPoint> trajectory =tysjsc.generateTrajectory(start, waypoints, end, startTime, speedConfig, droneParams);
        wjbdWrjTyjh.setJhjs(trajectory.get(trajectory.size() - 1).time);
        //推演计划保存成功
        boolean bo = this.save(wjbdWrjTyjh);
        if (bo) {
            QueryWrapper<WjbdWrjTyjhData> wjbdWrjTyjhDataQueryWrapper = new QueryWrapper();
            wjbdWrjTyjhDataQueryWrapper.eq("model", wjbdWrjTyjh.getModel());
            wjbdWrjTyjhDataQueryWrapper.eq("serial", wjbdWrjTyjh.getSerial());
            if (wjbdWrjTyjh.getRq() != null) {
                wjbdWrjTyjhDataQueryWrapper.eq("DATE_FORMAT(data_time, '%Y-%m-%d')", wjbdWrjTyjh.getRq());
            }
            int is_delete = iWjbdWrjTyjhDataService.getBaseMapper().delete(wjbdWrjTyjhDataQueryWrapper);
            if (is_delete > 0) {
                log.info("删除数据成功！！！！");
            }
            List<WjbdWrjTyjhData> uavDetectMsgList = new ArrayList<WjbdWrjTyjhData>();
            trajectory.stream().forEach(item -> {
                WjbdWrjTyjhData wjbdWrjTyjhData = new WjbdWrjTyjhData();
                wjbdWrjTyjhData.setTyjhId(wjbdWrjTyjh.getId());
                wjbdWrjTyjhData.setModel(wjbdWrjTyjh.getModel());
                wjbdWrjTyjhData.setSerial(wjbdWrjTyjh.getSerial());
                wjbdWrjTyjhData.setDronLng(item.longitude);
                wjbdWrjTyjhData.setDronLat(item.latitude);
                wjbdWrjTyjhData.setHeight(item.altitude);
                wjbdWrjTyjhData.setDataTime(item.time);
                wjbdWrjTyjhData.setAltitude(item.altitude);
                wjbdWrjTyjhData.setCreateTime(new Date());
                wjbdWrjTyjhData.setSd(item.speed);
                uavDetectMsgList.add(wjbdWrjTyjhData);
            });
            iWjbdWrjTyjhDataService.saveBatch(uavDetectMsgList);
        }
    }
}
