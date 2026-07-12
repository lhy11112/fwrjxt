package org.jeecg.modules.uav.dto;

import lombok.Data;

/**
 * 频谱监测参数
 */
@Data
public class SpectrumParamDto {
    /**
     * 使能标识（0-禁用，1-启用）
     */
    private int enable;
    /**
     * 开始频率（单位Hz，非负）
     */
    private long beginFreq;
    /**
     * 结束频率（单位Hz，需大于等于beginFreq）
     */
    private long endFreq;
    /**
     * 步进频率（单位Hz，默认60000，需大于0）
     */
    private int step;
    /**
     * 站点ID
     */
    private Integer stationId;
}
