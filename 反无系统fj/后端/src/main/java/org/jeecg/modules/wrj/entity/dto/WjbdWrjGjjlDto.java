package org.jeecg.modules.wrj.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 武警部队_无人机_告警记录
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Data
@TableName("wjbd_wrj_gjjl")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="wjbd_wrj_gjjl对象", description="武警部队_无人机_告警记录")
public class WjbdWrjGjjlDto implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 开始时间
     */
    private String startDate;
    /**
     * 结束时间
     */
    private String endDate;

}
