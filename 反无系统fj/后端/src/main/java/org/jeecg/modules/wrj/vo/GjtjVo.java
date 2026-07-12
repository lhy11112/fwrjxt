package org.jeecg.modules.wrj.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 告警查询条件
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class GjtjVo implements Serializable {




    private String type;//类型
    private String nf;//年份
    private String jd;//季度
    private String yf;//月份
    private String zs;//周数
    private String rq;//日期
    private String lx;//类型：空域/无人机
    private String wrjid;//类型：空域/无人机
    private String kyid;//类型：空域/无人机
    private String startDate;
    private String endDate;

}
