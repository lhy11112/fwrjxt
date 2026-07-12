package org.jeecg.modules.wrj.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 武警部队_无人机_基本信息
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class WjbdWrjKysqVo implements Serializable {

    /**空域id*/
    @Excel(name = "空域id", width = 15)
    @ApiModelProperty(value = "空域id")
    private String zjbid;
    /**空域id*/
    @Excel(name = "空域id", width = 15)
    @ApiModelProperty(value = "空域id")
    private String kyid;
    /**无人机id*/
    @Excel(name = "无人机id", width = 15)
    @ApiModelProperty(value = "无人机id")
    private String wrjid;
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
	/**授权状态：1-白名单（授权）、2-黑名单（禁止）、3-未授权*/
	@Excel(name = "授权状态：1-白名单（授权）、2-黑名单（禁止）、3-未授权", width = 15)
    @ApiModelProperty(value = "授权状态：1-白名单（授权）、2-黑名单（禁止）、3-未授权")
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
}
