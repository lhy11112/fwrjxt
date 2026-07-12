package org.jeecg.modules.uav.entity;

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
 * @Description: 无人机主动心跳数据表
 * @Author: jeecg-boot
 * @Date:   2026-02-27
 * @Version: V1.0
 */
@Data
@TableName("uav_active_heartbeat")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="uav_active_heartbeat对象", description="无人机主动心跳数据表")
public class UavActiveHeartbeat implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long id;
	/**起始码（固定 A55A）*/
	@Excel(name = "起始码（固定 A55A）", width = 15)
    @ApiModelProperty(value = "起始码（固定 A55A）")
    private java.lang.String startCode;
	/**源地址（0x10）*/
	@Excel(name = "源地址（0x10）", width = 15)
    @ApiModelProperty(value = "源地址（0x10）")
    private java.lang.Integer sourceAddr;
	/**目的地址（0xA0）*/
	@Excel(name = "目的地址（0xA0）", width = 15)
    @ApiModelProperty(value = "目的地址（0xA0）")
    private java.lang.Integer destAddr;
	/**命令（0xA6）*/
	@Excel(name = "命令（0xA6）", width = 15)
    @ApiModelProperty(value = "命令（0xA6）")
    private java.lang.Integer command;
	/**参数长度（61）*/
	@Excel(name = "参数长度（61）", width = 15)
    @ApiModelProperty(value = "参数长度（61）")
    private java.lang.Integer paramLength;
	/**终端代码（Param1）*/
	@Excel(name = "终端代码（Param1）", width = 15)
    @ApiModelProperty(value = "终端代码（Param1）")
    private java.lang.String terminalCode;
	/**Bit0：授权状态（0=未授权，1=正常）*/
	@Excel(name = "Bit0：授权状态（0=未授权，1=正常）", width = 15)
    @ApiModelProperty(value = "Bit0：授权状态（0=未授权，1=正常）")
    private Boolean authNormal;
	/**Bit1：探测功能（0=停止，1=开启）*/
	@Excel(name = "Bit1：探测功能（0=停止，1=开启）", width = 15)
    @ApiModelProperty(value = "Bit1：探测功能（0=停止，1=开启）")
    private Boolean detectEnabled;
	/**Bit2：反制功能（0=未开启，1=已开启）*/
	@Excel(name = "Bit2：反制功能（0=未开启，1=已开启）", width = 15)
    @ApiModelProperty(value = "Bit2：反制功能（0=未开启，1=已开启）")
    private Boolean counterEnabled;
	/**Bit3：探测设备（0=异常，1=正常）*/
	@Excel(name = "Bit3：探测设备（0=异常，1=正常）", width = 15)
    @ApiModelProperty(value = "Bit3：探测设备（0=异常，1=正常）")
    private Boolean detectorOnline;
	/**Bit4：反制设备（0=异常，1=正常）*/
	@Excel(name = "Bit4：反制设备（0=异常，1=正常）", width = 15)
    @ApiModelProperty(value = "Bit4：反制设备（0=异常，1=正常）")
    private Boolean counterOnline;
	/**Bit5：无人值守（0=关闭，1=开启）*/
	@Excel(name = "Bit5：无人值守（0=关闭，1=开启）", width = 15)
    @ApiModelProperty(value = "Bit5：无人值守（0=关闭，1=开启）")
    private Boolean unattendedMode;
	/**Bit6：诱骗设备（0=异常，1=正常）*/
	@Excel(name = "Bit6：诱骗设备（0=异常，1=正常）", width = 15)
    @ApiModelProperty(value = "Bit6：诱骗设备（0=异常，1=正常）")
    private Boolean deceiverOnline;
	/**干扰模式（Param3：0=返航，1=迫降）*/
	@Excel(name = "干扰模式（Param3：0=返航，1=迫降）", width = 15)
    @ApiModelProperty(value = "干扰模式（Param3：0=返航，1=迫降）")
    private java.lang.Integer jammingMode;
	/**Bit0：5.8G频段（0=关闭，1=开启）*/
	@Excel(name = "Bit0：5.8G频段（0=关闭，1=开启）", width = 15)
    @ApiModelProperty(value = "Bit0：5.8G频段（0=关闭，1=开启）")
    private Boolean band58g;
	/**Bit1：2.4G频段（0=关闭，1=开启）*/
	@Excel(name = "Bit1：2.4G频段（0=关闭，1=开启）", width = 15)
    @ApiModelProperty(value = "Bit1：2.4G频段（0=关闭，1=开启）")
    private Boolean band24g;
	/**Bit2：900M频段（0=关闭，1=开启）*/
	@Excel(name = "Bit2：900M频段（0=关闭，1=开启）", width = 15)
    @ApiModelProperty(value = "Bit2：900M频段（0=关闭，1=开启）")
    private Boolean band900m;
	/**Bit3：1.4G频段（0=关闭，1=开启）*/
	@Excel(name = "Bit3：1.4G频段（0=关闭，1=开启）", width = 15)
    @ApiModelProperty(value = "Bit3：1.4G频段（0=关闭，1=开启）")
    private Boolean band14g;
	/**Bit4：5.2G频段（0=关闭，1=开启）*/
	@Excel(name = "Bit4：5.2G频段（0=关闭，1=开启）", width = 15)
    @ApiModelProperty(value = "Bit4：5.2G频段（0=关闭，1=开启）")
    private Boolean band52g;
	/**云台控制模式（Param5：1/2/3）*/
	@Excel(name = "云台控制模式（Param5：1/2/3）", width = 15)
    @ApiModelProperty(value = "云台控制模式（Param5：1/2/3）")
    private java.lang.Integer ptzControlMode;
	/**攻击倒计时（Param6）*/
	@Excel(name = "攻击倒计时（Param6）", width = 15)
    @ApiModelProperty(value = "攻击倒计时（Param6）")
    private java.lang.Integer attackCountdown;
	/**系统诱骗状态（Param7：0x0000-空闲；0x0001-驱离；0x0002-迫降；0x0003-导航压制）*/
	@Excel(name = "系统诱骗状态（Param7：0x0000-空闲；0x0001-驱离；0x0002-迫降；0x0003-导航压制）", width = 15)
    @ApiModelProperty(value = "系统诱骗状态（Param7：0x0000-空闲；0x0001-驱离；0x0002-迫降；0x0003-导航压制）")
    private java.lang.Integer deceptionStatus;
	/**GNSS联动状态（Param8：0=关闭，1=开启）*/
	@Excel(name = "GNSS联动状态（Param8：0=关闭，1=开启）", width = 15)
    @ApiModelProperty(value = "GNSS联动状态（Param8：0=关闭，1=开启）")
    private java.lang.Integer gnssLinkStatus;
	/**GNSS诱骗模式（Param9：0x0000-定向驱逐；0x0001-定点迫降；0x0002-禁飞；0x0003-导航压制）*/
	@Excel(name = "GNSS诱骗模式（Param9：0x0000-定向驱逐；0x0001-定点迫降；0x0002-禁飞；0x0003-导航压制）", width = 15)
    @ApiModelProperty(value = "GNSS诱骗模式（Param9：0x0000-定向驱逐；0x0001-定点迫降；0x0002-禁飞；0x0003-导航压制）")
    private java.lang.Integer gnssDeceptionMode;
	/**GNSS诱导方式（Param10：0=驱离，1=拉近）*/
	@Excel(name = "GNSS诱导方式（Param10：0=驱离，1=拉近）", width = 15)
    @ApiModelProperty(value = "GNSS诱导方式（Param10：0=驱离，1=拉近）")
    private java.lang.Integer gnssInduceMode;
	/**禁飞区纬度（Param11）*/
	@Excel(name = "禁飞区纬度（Param11）", width = 15)
    @ApiModelProperty(value = "禁飞区纬度（Param11）")
    private Float noFlyLat;
	/**禁飞区经度（Param12）*/
	@Excel(name = "禁飞区经度（Param12）", width = 15)
    @ApiModelProperty(value = "禁飞区经度（Param12）")
    private Float noFlyLng;
	/**禁飞区海拔（Param13）*/
	@Excel(name = "禁飞区海拔（Param13）", width = 15)
    @ApiModelProperty(value = "禁飞区海拔（Param13）")
    private Float noFlyAlt;
	/**定点迫降区纬度（Param14）*/
	@Excel(name = "定点迫降区纬度（Param14）", width = 15)
    @ApiModelProperty(value = "定点迫降区纬度（Param14）")
    private Float forcedLandLat;
	/**定点迫降区经度（Param15）*/
	@Excel(name = "定点迫降区经度（Param15）", width = 15)
    @ApiModelProperty(value = "定点迫降区经度（Param15）")
    private Float forcedLandLng;
	/**定点迫降区海拔（Param16）*/
	@Excel(name = "定点迫降区海拔（Param16）", width = 15)
    @ApiModelProperty(value = "定点迫降区海拔（Param16）")
    private Float forcedLandAlt;
	/**定点迫降区半径（Param17，单位：m）*/
	@Excel(name = "定点迫降区半径（Param17，单位：m）", width = 15)
    @ApiModelProperty(value = "定点迫降区半径（Param17，单位：m）")
    private java.lang.Integer forcedLandRadius;
	/**校验和*/
	@Excel(name = "校验和", width = 15)
    @ApiModelProperty(value = "校验和")
    private java.lang.Integer checksum;
	/**创建时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private java.util.Date createTime;
	/**更新时间*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private java.util.Date updateTime;
	/**站ID*/
	@Excel(name = "站ID", width = 15)
    @ApiModelProperty(value = "站ID")
    private Integer stationId;
}
