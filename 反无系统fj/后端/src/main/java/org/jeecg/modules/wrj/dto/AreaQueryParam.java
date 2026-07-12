package org.jeecg.modules.wrj.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 区域查询参数（接收前端JSON）
 */
@Data
public class AreaQueryParam {
    /** 西北点 [纬度, 经度] */
    private BigDecimal[] northWest;
    /** 东北点 [纬度, 经度] */
    private BigDecimal[] northEast;
    /** 东南点 [纬度, 经度] */
    private BigDecimal[] southEast;
    /** 西南点 [纬度, 经度] */
    private BigDecimal[] southWest;
}
