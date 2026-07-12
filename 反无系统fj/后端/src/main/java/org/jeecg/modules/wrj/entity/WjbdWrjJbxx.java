package org.jeecg.modules.wrj.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
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
 * @Description: 武警部队_无人机_基本信息
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Data
@TableName("wjbd_wrj_jbxx")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="wjbd_wrj_jbxx对象", description="武警部队_无人机_基本信息")
public class WjbdWrjJbxx implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
    /**图片*/
    @Excel(name = "图片", width = 15)
    @ApiModelProperty(value = "图片")
    private String tp;
    /**三维模型地址*/
    @Excel(name = "三维模型地址", width = 15)
    @ApiModelProperty(value = "三维模型地址")
    private String mxdz;
	/**序列号*/
	@Excel(name = "序列号", width = 15)
    @ApiModelProperty(value = "序列号")
    private String serialNumber;
	/**品牌（如“大疆”“极飞”）*/
	@Excel(name = "品牌（如“大疆”“极飞”）", width = 15)
    @ApiModelProperty(value = "品牌（如“大疆”“极飞”）")
    private String brand;
	/**型号（如“Mavic 3”）*/
	@Excel(name = "型号（如“Mavic 3”）", width = 15)
    @ApiModelProperty(value = "型号（如“Mavic 3”）")
    private String model;
	/**1-正常、2-告警、3-失联 */
	@Excel(name = "1-正常、2-告警、3-失联 ", width = 15)
    @ApiModelProperty(value = "1-正常、2-告警、3-失联 ")
    private Integer status;
	/**授权状态：1-白名单（授权）、2-黑名单（禁止）、3-未授权、4-待干扰、5-待诱骗、6、持续跟踪*/
	@Excel(name = "授权状态：1-白名单（授权）、2-黑名单（禁止）、3-未授权、4-待干扰、5-待诱骗、6、持续跟踪", width = 15)
    @ApiModelProperty(value = "授权状态：1-白名单（授权）、2-黑名单（禁止）、3-未授权、4-待干扰、5-待诱骗、6、持续跟踪")
    private Integer authStatus;
	/**当前经度*/
	@Excel(name = "当前经度", width = 15)
    @ApiModelProperty(value = "当前经度")
    private BigDecimal currentLongitude;
	/**当前纬度*/
	@Excel(name = "当前纬度", width = 15)
    @ApiModelProperty(value = "当前纬度")
    private BigDecimal currentLatitude;
	/**当前高度*/
	@Excel(name = "当前高度", width = 15)
    @ApiModelProperty(value = "当前高度")
    private Integer currentAltitude;
    /**任务类型*/
    @Excel(name = "任务类型", width = 15)
    @ApiModelProperty(value = "任务类型")
    private String rwlx;
    /**备注*/
    @Excel(name = "所属单位", width = 15)
    @ApiModelProperty(value = "所属单位")
    private String ssdw;
	/**最后出现时间*/
	@Excel(name = "最后出现时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "最后出现时间")
    private Date lastSeenTime;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private String remark;
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
}
