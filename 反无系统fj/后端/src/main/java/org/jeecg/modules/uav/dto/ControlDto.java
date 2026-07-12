package org.jeecg.modules.uav.dto;

import lombok.Data;
import org.jeecg.modules.uav.service.DeviceCommandService;

@Data
public class ControlDto {
    /**
     * 重启设备
     * 开始工作
     * 停止工作
     * 设置诱骗功率
     * 设置诱骗禁飞开关
     * 设置诱骗模式
     * 驱离功能控制
     * 快速干扰控制
     * 自定义干扰控制
     * 天线控制
     * 指定诱骗
     */
    private String type;
    private Integer stationId;
    private Integer disturbType;
    private Integer trapType;
    private Integer amp;
    private Boolean enable;
    /**
     * 天线控制Dto
     */
    private ControlTxDto controlTxDto;

    /**
     * 自定义
     */
    private DeviceCommandService.DisturbSelfParam disturbSelfParam;
    /**
     * 飞机经度
     */
    private float uavLng;
    /**
     * 飞机纬度
     */
    private float uavLat;
    /**
     *  目标经度
     */
    private float tagLng;
    /**
     * 目标纬度
     */
    private float tagLat;
}
