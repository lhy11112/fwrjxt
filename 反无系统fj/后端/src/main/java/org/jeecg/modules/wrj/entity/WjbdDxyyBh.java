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
 * @Description: 武警不对_典型应用_无人机
 * @Author: jeecg-boot
 * @Date:   2026-01-19
 * @Version: V1.0
 */
@Data
@TableName("wjbd_dxyy_bh")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="wjbd_dxyy_bh对象", description="武警不对_典型应用_无人机")
public class WjbdDxyyBh implements Serializable {
    private static final long serialVersionUID = 1L;

	/**ID*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long id;
	/**标绘名称*/
	@Excel(name = "标绘名称", width = 15)
    @ApiModelProperty(value = "标绘名称")
    private String bhmc;
	/**态势名称*/
	@Excel(name = "态势名称", width = 15)
    @ApiModelProperty(value = "态势名称")
    private String tsmc;
	/**标绘时间*/
	@Excel(name = "标绘时间", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "标绘时间")
    private Date bhsj;
	/**任务时间*/
	@Excel(name = "任务时间", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "任务时间")
    private Date rwsj;
	/**状态码（0：请求任务数据；1：请求备份数据）*/
	@Excel(name = "状态码（0：请求任务数据；1：请求备份数据）", width = 15)
    @ApiModelProperty(value = "状态码（0：请求任务数据；1：请求备份数据）")
    private Integer ztm;
	/**单位ID*/
	@Excel(name = "单位ID", width = 15)
    @ApiModelProperty(value = "单位ID")
    private String dwid;
	/**名称*/
	@Excel(name = "名称", width = 15)
    @ApiModelProperty(value = "名称")
    private String dwmc;
	/**接收单位ID*/
	@Excel(name = "接收单位ID", width = 15)
    @ApiModelProperty(value = "接收单位ID")
    private String jsdwid;
	/**标绘数据*/
	@Excel(name = "标绘数据", width = 15)
    @ApiModelProperty(value = "标绘数据")
    private String gisjson;
	/**创建人ID*/
	@Excel(name = "创建人ID", width = 15)
    @ApiModelProperty(value = "创建人ID")
    private String cjrid;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date cjsj;
	/**业务id*/
	@Excel(name = "业务id", width = 15)
    @ApiModelProperty(value = "业务id")
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long ywId;
}
