package org.jeecg.modules.wrj.entity;

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
 * @Description: 武警部队_无人机_收藏夹
 * @Author: jeecg-boot
 * @Date:   2026-02-04
 * @Version: V1.0
 */
@Data
@TableName("wjbd_wrj_scj")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="wjbd_wrj_scj对象", description="武警部队_无人机_收藏夹")
public class WjbdWrjScj implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键ID*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long id;
    /**
     * 上级ID
     */
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long pid;
    /**
     * 名称（目录/视点）
     */
    private String mc;

    /**
     * 类型：视点、目录
     */
    private String type;
	/**用户ID*/
	@Excel(name = "用户ID", width = 15)
    @ApiModelProperty(value = "用户ID")
    private String yhId;
	/**当前用户视点的JSON值*/
	@Excel(name = "当前用户视点的JSON值", width = 15)
    @ApiModelProperty(value = "当前用户视点的JSON值")
    private String csz;
	/**创建人名称*/
	@Excel(name = "创建人名称", width = 15)
    @ApiModelProperty(value = "创建人名称")
    private String chjrMc;
	/**创建人ID*/
	@Excel(name = "创建人ID", width = 15)
    @ApiModelProperty(value = "创建人ID")
    private String chjr;
	/**入库时间*/
	@Excel(name = "入库时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "入库时间")
    private Date rksj;
}
