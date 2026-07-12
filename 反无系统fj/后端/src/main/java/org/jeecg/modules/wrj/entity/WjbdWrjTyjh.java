package org.jeecg.modules.wrj.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.jeecg.modules.uav.util.UavDatectMsgDto1;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 武警部队_无人机_推演计划
 * @Author: jeecg-boot
 * @Date:   2025-09-22
 * @Version: V1.0
 */
@Data
@TableName("wjbd_wrj_tyjh")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="wjbd_wrj_tyjh对象", description="武警部队_无人机_推演计划")
public class WjbdWrjTyjh implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
	/**推演计划名称*/
	@Excel(name = "推演计划名称", width = 15)
    @ApiModelProperty(value = "推演计划名称")
    private String mc;
	/**推演计划日期*/
	@Excel(name = "推演计划日期", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "推演计划日期")
    private Date rq;
	/**计划开始*/
	@Excel(name = "计划开始", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "计划开始")
    private Date jhks;
	/**计划结束*/
	@Excel(name = "计划结束", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "计划结束")
    private Date jhjs;
	/**无人机序列号（char[64]）*/
	@Excel(name = "无人机序列号（char[64]）", width = 15)
    @ApiModelProperty(value = "无人机序列号（char[64]）")
    private String serial;
	/**机型（char[64]）*/
	@Excel(name = "机型（char[64]）", width = 15)
    @ApiModelProperty(value = "机型（char[64]）")
    private String model;
	/**品牌*/
	@Excel(name = "品牌", width = 15)
    @ApiModelProperty(value = "品牌")
    private String brand;
	/**未开始、进行中、已完成*/
	@Excel(name = "未开始、进行中、已完成", width = 15)
    @ApiModelProperty(value = "未开始、进行中、已完成")
    private String tyzt;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
    private String cjr;
	/**创建人ID*/
	@Excel(name = "创建人ID", width = 15)
    @ApiModelProperty(value = "创建人ID")
    private String cjrid;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
    private Date cjsj;
	/**操作人*/
	@Excel(name = "操作人", width = 15)
    @ApiModelProperty(value = "操作人")
    private String czr;
	/**操作人ID*/
	@Excel(name = "操作人ID", width = 15)
    @ApiModelProperty(value = "操作人ID")
    private String czrid;
	/**操作时间*/
	@Excel(name = "操作时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "操作时间")
    private Date czsj;
	/**起点经度*/
	@Excel(name = "起点经度", width = 15)
    @ApiModelProperty(value = "起点经度")
    private BigDecimal startJd;
	/**起点纬度*/
	@Excel(name = "起点纬度", width = 15)
    @ApiModelProperty(value = "起点纬度")
    private BigDecimal startWd;
	/**起点高度*/
	@Excel(name = "起点高度", width = 15)
    @ApiModelProperty(value = "起点高度")
    private BigDecimal startGd;
	/**途径点经纬度*/
	@Excel(name = "途径点经纬度", width = 15)
    @ApiModelProperty(value = "途径点经纬度")
    private String tjdjwd;
	/**终点经度*/
	@Excel(name = "终点经度", width = 15)
    @ApiModelProperty(value = "终点经度")
    private BigDecimal endJd;
	/**终点纬度*/
	@Excel(name = "终点纬度", width = 15)
    @ApiModelProperty(value = "终点纬度")
    private BigDecimal endWd;
	/**终点高度*/
	@Excel(name = "终点高度", width = 15)
    @ApiModelProperty(value = "终点高度")
    private BigDecimal endGd;
	/**计划参数*/
	@Excel(name = "计划参数", width = 15)
	@ApiModelProperty(value = "计划参数")
	private String jhcs;
	/**目标分析范围*/
	@Excel(name = "目标分析范围", width = 15)
	@ApiModelProperty(value = "目标分析范围")
	private String mbfxfw;
	@TableField(exist = false)
	private UavDatectMsgDto1 uavDatectMsgDto1;
}
