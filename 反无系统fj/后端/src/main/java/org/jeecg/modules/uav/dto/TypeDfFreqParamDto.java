package org.jeecg.modules.uav.dto;

import lombok.Data;

/**
 * 手动设置侧向频率参数信息
 */
@Data
public class TypeDfFreqParamDto {
    private int enable;
    private int dirType;
    private long freq;
    private int bw;
    private String id;
    private String model;
    private Integer stationId;
}
