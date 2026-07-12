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
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
public class WjbdWrjGjjl implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    /**空域名称*/
    @Excel(name = "空域名称", width = 15)
    @ApiModelProperty(value = "空域名称")
    private String kymc;
	/**空域id*/
	@Excel(name = "空域id", width = 15)
    @ApiModelProperty(value = "空域id")
    @Dict(dictTable ="wjbd_wrj_ky",dicText = "mc",dicCode = "id")
    private String kyid;
	/**无人机id*/
	@Excel(name = "无人机id", width = 15)
    @ApiModelProperty(value = "无人机id")
    @Dict(dictTable ="wjbd_wrj_jbxx",dicText = "concat(brand,'-',model,'-',serial_number)",dicCode = "id")
    private String wrjid;
    /**无人机品牌*/
    @Excel(name = "无人机品牌", width = 15)
    @ApiModelProperty(value = "无人机品牌")
    private String wrjpp;
    /**无人机型号*/
    @Excel(name = "无人机型号", width = 15)
    @ApiModelProperty(value = "无人机型号")
    private String wrjxh;
    /**无人机序列号*/
    @Excel(name = "无人机序列号", width = 15)
    @ApiModelProperty(value = "无人机序列号")
    private String wrjxlh;
    /**站点id*/
    @Excel(name = "站点id", width = 15)
    @ApiModelProperty(value = "站点id")
    private String zdid;
    /**站点名称*/
    @Excel(name = "站点名称", width = 15)
    @ApiModelProperty(value = "站点名称")
    private String zdmc;
    /**飞手经度*/
    @Excel(name = "飞手经度", width = 15)
    @ApiModelProperty(value = "飞手经度")
    private Float fsjd;
    /**飞手纬度*/
    @Excel(name = "飞手纬度", width = 15)
    @ApiModelProperty(value = "飞手纬度")
    private Float fswd;
	/**告警类型:1-闯入禁飞区、2-超高度限制、3-黑名单入侵、4-未授权飞行*/
	@Excel(name = "告警类型:1-闯入禁飞区、2-超高度限制、3-黑名单入侵、4-未授权飞行", width = 15)
    @ApiModelProperty(value = "告警类型:1-闯入禁飞区、2-超高度限制、3-黑名单入侵、4-未授权飞行")
    private String gjlx;
	/**告警发生经度*/
	@Excel(name = "告警发生经度", width = 15)
    @ApiModelProperty(value = "告警发生经度")
    private BigDecimal gjfsjd;
	/**告警发生纬度*/
	@Excel(name = "告警发生纬度", width = 15)
    @ApiModelProperty(value = "告警发生纬度")
    private BigDecimal gjfswd;
    /**告警发生高度*/
    @Excel(name = "告警发生高度", width = 15)
    @ApiModelProperty(value = "告警发生高度")
    private Double gjfsgd;
	/**告警发生时间*/
	@Excel(name = "告警发生时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "告警发生时间")
    private Date gjfssj;
	/**0-未处理、1-已处理、2-忽略*/
	@Excel(name = "0-未处理、1-已处理、2-忽略", width = 15)
    @ApiModelProperty(value = "0-未处理、1-已处理、2-忽略")
    private String clzt;
	/**处理时间*/
	@Excel(name = "处理时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "处理时间")
    private Date clsj;
	/**处理人id*/
	@Excel(name = "处理人id", width = 15)
    @ApiModelProperty(value = "处理人id")
    private String clrid;
	/**处理人*/
	@Excel(name = "处理人", width = 15)
    @ApiModelProperty(value = "处理人")
    private String clr;
	/**处理备注(警告、干扰诱骗、拉黑)*/
	@Excel(name = "处理备注(警告、干扰诱骗、拉黑)", width = 15)
    @ApiModelProperty(value = "处理备注(警告、干扰诱骗、拉黑)")
    private String clbz;
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
    /**告警颜色*/
    @Excel(name = "告警颜色", width = 15)
    @ApiModelProperty(value = "告警颜色")
    private String gjys;
    /**
     * 日期
     */
    @TableField(exist = false)
    private String rq;
}
