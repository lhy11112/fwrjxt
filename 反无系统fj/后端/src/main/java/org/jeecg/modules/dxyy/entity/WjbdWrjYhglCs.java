package org.jeecg.modules.dxyy.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 武警部队_无人机_用户管理_参数
 * @Author: jeecg-boot
 * @Date:   2025-09-01
 * @Version: V1.0
 */
@Data
@TableName("wjbd_wrj_yhgl_cs")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="wjbd_wrj_yhgl_cs对象", description="武警部队_无人机_用户管理_参数")
public class WjbdWrjYhglCs implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键ID*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long id;
    /**用户ID*/
    @Excel(name = "用户ID", width = 15)
    @ApiModelProperty(value = "用户ID")
    private java.lang.String yhId;
    /**参数名称*/
    @Excel(name = "参数名称", width = 15)
    @ApiModelProperty(value = "参数名称")
    private java.lang.String csMc;
    /**参数编码*/
    @Excel(name = "参数编码", width = 15)
    @ApiModelProperty(value = "参数编码")
    private java.lang.String csBm;
    /**对应的值*/
    @Excel(name = "对应的值", width = 15)
    @ApiModelProperty(value = "对应的值")
    private java.lang.String csz;
    /**文件内码*/
    @Excel(name = "文件内码", width = 15)
    @ApiModelProperty(value = "文件内码")
    private java.lang.String bz;
    /**创建人名称*/
    @Excel(name = "创建人名称", width = 15)
    @ApiModelProperty(value = "创建人名称")
    private java.lang.String chjrMc;
    /**创建人ID*/
    @Excel(name = "创建人ID", width = 15)
    @ApiModelProperty(value = "创建人ID")
    private java.lang.String chjr;
    /**入库时间*/
    @Excel(name = "入库时间", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "入库时间")
    private java.util.Date rksj;
    /**部队内码*/
    @Excel(name = "部队内码", width = 15)
    @ApiModelProperty(value = "部队内码")
    private java.lang.String bdnm;
}
