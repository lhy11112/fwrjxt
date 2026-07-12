package org.jeecg.modules.wrj.vo;

import lombok.Data;

/**
 * 武警部队无人机重要目标实体类
 */
@Data
public class TargetData {
    private String ID;         // 主键ID
    private String MC;         // 名称
    private String JD;         // 经度（字符串类型）
    private String WD;         // 纬度（字符串类型）
    private String RYLX;       // 人员类型
    private String SL;         // 数量
    private String ZB;         // 装备
    private String SSGK;       // 设施概况
    private String TYPE;       // 类型（民生目标、执勤目标、友邻信息）
    private String SUBTYPE;    // 子类型（医院、学校等）
}
