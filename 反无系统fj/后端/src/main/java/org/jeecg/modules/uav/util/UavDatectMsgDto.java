package org.jeecg.modules.uav.util;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UavDatectMsgDto {
    // // 起点经度
    private double startLon = 120.0;
    // 起点纬度
    private double startLat = 30.0;
    // 起点高度（米）
    private double startAlt = 100;
    // 终点经度
    private double endLon = 120.5;
    // 终点纬度
    private double endLat = 30.5;
    // 终点高度（米）
    private double endAlt = 150;
    // 总飞行时间（秒）
    private int totalTimeSeconds = 60 * 10;
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
