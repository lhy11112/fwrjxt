package org.jeecg.modules.uav.vo;

import lombok.Data;

/**
 * 侦测无人机信息
 * 日历数据查询
 */
@Data
public class UavDetectMsgDateVo {
    /**
     * 日期
     */
    private String rq;
    /**
     * 无人机飞行类型
     */
    private String type;
}
