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
 * @Description: 武警部队_无人机_详细信息
 * @Author: jeecg-boot
 * @Date:   2025-10-29
 * @Version: V1.0
 */
@Data
@TableName("wjbd_wrj_jbxx_new")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="wjbd_wrj_jbxx_new对象", description="武警部队_无人机_详细信息")
public class WjbdWrjJbxxNew implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
	/**序列号(唯一识别码)*/
	@Excel(name = "序列号(唯一识别码)", width = 15)
    @ApiModelProperty(value = "序列号(唯一识别码)")
    private java.lang.String serialNumber;
	/**品牌（如“大疆”“极飞”）*/
	@Excel(name = "品牌（如“大疆”“极飞”）", width = 15)
    @ApiModelProperty(value = "品牌（如“大疆”“极飞”）")
    private java.lang.String brand;
	/**型号（如“Mavic 3”）*/
	@Excel(name = "型号（如“Mavic 3”）", width = 15)
    @ApiModelProperty(value = "型号（如“Mavic 3”）")
    private java.lang.String model;
	/**无人机类型（军用/民用）*/
	@Excel(name = "无人机类型（军用/民用）", width = 15)
    @ApiModelProperty(value = "无人机类型（军用/民用）")
    private java.lang.String type;
    /**序列号(唯一识别码)*/
    @Excel(name = "简介", width = 15)
    @ApiModelProperty(value = "简介")
    private java.lang.String jj;
    /**序列号(唯一识别码)*/
    @Excel(name = "弹药", width = 15)
    @ApiModelProperty(value = "弹药")
    private java.lang.String dy;
    /**序列号(唯一识别码)*/
    @Excel(name = "探测能力", width = 15)
    @ApiModelProperty(value = "探测能力")
    private java.lang.String tcnl;
    /**序列号(唯一识别码)*/
    @Excel(name = "作战能力", width = 15)
    @ApiModelProperty(value = "作战能力")
    private java.lang.String zznl;
    /**序列号(唯一识别码)*/
    @Excel(name = "编配及部署", width = 15)
    @ApiModelProperty(value = "编配及部署")
    private java.lang.String bpjbs;
	/**最大续航时间（分钟）*/
	@Excel(name = "最大续航时间（分钟）", width = 15)
    @ApiModelProperty(value = "最大续航时间（分钟）")
    private java.lang.String zdxhsj;
	/**最大飞行速度（km/h）*/
	@Excel(name = "最大飞行速度（km/h）", width = 15)
    @ApiModelProperty(value = "最大飞行速度（km/h）")
    private java.lang.String zdfxsd;
	/**最大控制距离(km)*/
	@Excel(name = "最大控制距离(km)", width = 15)
    @ApiModelProperty(value = "最大控制距离(km)")
    private java.lang.String zdkzjl;
	/**最大飞行高度(m,含相对高度/海拔高度)*/
	@Excel(name = "最大飞行高度(m,含相对高度/海拔高度)", width = 15)
    @ApiModelProperty(value = "最大飞行高度(m,含相对高度/海拔高度)")
    private java.lang.String zdfxgd;
	/**抗风等级（级）*/
	@Excel(name = "抗风等级（级）", width = 15)
    @ApiModelProperty(value = "抗风等级（级）")
    private java.lang.String kfdj;
	/**最大荷载重量（g/kg）*/
	@Excel(name = "最大荷载重量（g/kg）", width = 15)
    @ApiModelProperty(value = "最大荷载重量（g/kg）")
    private java.lang.String zdhzzl;
	/**机身尺寸（长×宽×高，mm）*/
	@Excel(name = "机身尺寸（长×宽×高，mm）", width = 15)
    @ApiModelProperty(value = "机身尺寸（长×宽×高，mm）")
    private java.lang.String jscc;
	/**机身重量（含电池，g/kg）*/
	@Excel(name = "机身重量（含电池，g/kg）", width = 15)
    @ApiModelProperty(value = "机身重量（含电池，g/kg）")
    private java.lang.String jszl;
	/**动力系统（电机数量、动力类型如电动/燃油）*/
	@Excel(name = "动力系统（电机数量、动力类型如电动/燃油）", width = 15)
    @ApiModelProperty(value = "动力系统（电机数量、动力类型如电动/燃油）")
    private java.lang.String dlxt;
	/**定位系统（如GPS+GLONASS\RTK）*/
	@Excel(name = "定位系统（如GPS+GLONASS/RTK）", width = 15)
    @ApiModelProperty(value = "定位系统（如GPS+GLONASS/RTK）")
    private java.lang.String dwxt;
	/**相机参数（分辨率、传感器尺寸、镜头焦距，仅航拍机型）*/
	@Excel(name = "相机参数（分辨率、传感器尺寸、镜头焦距，仅航拍机型）", width = 15)
    @ApiModelProperty(value = "相机参数（分辨率、传感器尺寸、镜头焦距，仅航拍机型）")
    private java.lang.String xjcs;
	/**图传系统（图传分辨率、延迟）*/
	@Excel(name = "图传系统（图传分辨率、延迟）", width = 15)
    @ApiModelProperty(value = "图传系统（图传分辨率、延迟）")
    private java.lang.String tcxt;
	/**电池规格（容量mAh、电压V）*/
	@Excel(name = "电池规格（容量mAh、电压V）", width = 15)
    @ApiModelProperty(value = "电池规格（容量mAh、电压V）")
    private java.lang.String dcgg;
	/**1-正常、2-告警、3-失联 */
	@Excel(name = "1-正常、2-告警、3-失联 ", width = 15)
    @ApiModelProperty(value = "1-正常、2-告警、3-失联 ")
    private java.lang.Integer status;
	/**授权状态：1-白名单（授权）、2-黑名单（禁止）、3-未授权*/
	@Excel(name = "授权状态：1-白名单（授权）、2-黑名单（禁止）、3-未授权", width = 15)
    @ApiModelProperty(value = "授权状态：1-白名单（授权）、2-黑名单（禁止）、3-未授权")
    private java.lang.Integer authStatus;
	/**当前经度*/
	@Excel(name = "当前经度", width = 15)
    @ApiModelProperty(value = "当前经度")
    private java.math.BigDecimal currentLongitude;
	/**当前纬度*/
	@Excel(name = "当前纬度", width = 15)
    @ApiModelProperty(value = "当前纬度")
    private java.math.BigDecimal currentLatitude;
	/**当前高度*/
	@Excel(name = "当前高度", width = 15)
    @ApiModelProperty(value = "当前高度")
    private java.lang.Integer currentAltitude;
	/**最后出现时间*/
	@Excel(name = "最后出现时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "最后出现时间")
    private java.util.Date lastSeenTime;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private java.lang.String remark;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
    private java.lang.String cjr;
	/**创建人ID*/
	@Excel(name = "创建人ID", width = 15)
    @ApiModelProperty(value = "创建人ID")
    private java.lang.String cjrid;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
    private java.util.Date cjsj;
	/**操作人*/
	@Excel(name = "操作人", width = 15)
    @ApiModelProperty(value = "操作人")
    private java.lang.String czr;
	/**操作人ID*/
	@Excel(name = "操作人ID", width = 15)
    @ApiModelProperty(value = "操作人ID")
    private java.lang.String czrid;
	/**操作时间*/
	@Excel(name = "操作时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "操作时间")
    private java.util.Date czsj;
	/**图片*/
	@Excel(name = "图片", width = 15)
    @ApiModelProperty(value = "图片")
    private java.lang.String tp;
	/**三维模型地址*/
	@Excel(name = "三维模型地址", width = 15)
    @ApiModelProperty(value = "三维模型地址")
    private java.lang.String mxdz;
    /**地区分类*/
    @Excel(name = "地区分类", width = 15)
    @ApiModelProperty(value = "地区分类")
    private java.lang.String dqfl;
    /**种类分类*/
    @Excel(name = "种类分类", width = 15)
    @ApiModelProperty(value = "种类分类")
    private java.lang.String zlfl;
}
