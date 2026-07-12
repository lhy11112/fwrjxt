package org.jeecg.modules.uav.util;

import lombok.Data;
import org.jeecg.modules.uav.vo.Waypoint;

import java.util.List;

@Data
public class UavDatectMsgDto1 {
    /**
     * 航段速度
     */
    private  List<Double> segmentSpeeds;
    /**
     * 途径点经纬度列表
     */
    private List<Waypoints> hdcs;
    /**
     * 起点经纬度信息
     */
    private Waypoint start;
    /**
     * 途径点经纬度列表
     */
    private List<Waypoint> waypoints;
    /**
     * 终点经纬度信息
     */
    private Waypoint end;
    /**
     * 型号
     */
    private String model;
    /**
     * 序列号
     */
    private String serial;
    /**
     * 站id
     */
    private Integer stationId;
    /**
     * 日期
     */
    private String rq;
}
