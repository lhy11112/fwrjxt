package org.jeecg.modules.uav.dto;

import lombok.Data;

/**
 *
 * 天线控制
 *
 */
@Data
public class ControlTxDto {
    /**
     * 扫描方式：0 定向，1 全向扫描，2 交叉扫描
     */
    private Integer dirLoop;
    /**
     * 方向(0-360)度（定向 0 有效）
     */
    private  Integer direction;
}
