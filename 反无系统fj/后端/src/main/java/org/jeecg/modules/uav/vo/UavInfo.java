package org.jeecg.modules.uav.vo;

import lombok.Data;

/**
 * 报文解密结果封装
 */
@Data
public class UavInfo {
    /**
     * 飞机序列号
     */
    private String sn;

    /**
     * 飞手的执照代码
     */
    private String uuid;

    /**
     * 机型代码
     */
    private Integer type;

    /**
     * 飞机经度
     */
    private Double lon;

    /**
     * 飞机纬度
     */
    private Double lat;

    /**
     * 海拔
     */
    private Double alt;

    /**
     * 高度
     */
    private Double height;

    /**
     * 向东的速度
     */
    private Float x;

    /**
     * 向北的速度
     */
    private Float y;

    /**
     * 向上的速度
     */
    private Float z;

    /**
     * 飞机仰角（无实际业务意义）
     */
    private Double yaw;

    /**
     * GPS时间
     */
    private String gps_time;

    /**
     * 飞手经度
     */
    private Float pilot_lon;

    /**
     * 飞手纬度
     */
    private Float pilot_lat;

    /**
     * 返航点经度
     */
    private Float home_lon;

    /**
     * 返航点纬度
     */
    private Float home_lat;

    /**
     * 机型
     */
    private String model;
}
