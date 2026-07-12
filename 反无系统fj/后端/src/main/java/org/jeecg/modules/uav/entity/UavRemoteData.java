package org.jeecg.modules.uav.entity;

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
 * @Description: uav_remote_data
 * @Author: jeecg-boot
 * @Date:   2025-11-05
 * @Version: V1.0
 */
@Data
@TableName("uav_remote_data")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="uav_remote_data对象", description="uav_remote_data")
public class UavRemoteData implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;
    private Integer stationId;
	/**Remote ID*/
	@Excel(name = "Remote ID", width = 15)
    @ApiModelProperty(value = "Remote ID")
    private String risSsid;
	/**无人机序列号*/
	@Excel(name = "无人机序列号", width = 15)
    @ApiModelProperty(value = "无人机序列号")
    private String serial;
	/**机型*/
	@Excel(name = "机型", width = 15)
    @ApiModelProperty(value = "机型")
    private String model;
	/**参考Rid标准*/
	@Excel(name = "参考Rid标准", width = 15)
    @ApiModelProperty(value = "参考Rid标准")
    private String uaType;
	/**无人机经度*/
	@Excel(name = "无人机经度", width = 15)
    @ApiModelProperty(value = "无人机经度")
    private Double dronLng;
	/**无人机纬度*/
	@Excel(name = "无人机纬度", width = 15)
    @ApiModelProperty(value = "无人机纬度")
    private Double dronLat;
	/**遥控器经度*/
	@Excel(name = "遥控器经度", width = 15)
    @ApiModelProperty(value = "遥控器经度")
    private Double pilotLng;
	/**遥控器纬度*/
	@Excel(name = "遥控器纬度", width = 15)
    @ApiModelProperty(value = "遥控器纬度")
    private Double pilotLat;
	/**速递*/
	@Excel(name = "速递", width = 15)
    @ApiModelProperty(value = "速递")
    private Double speed;
	/**垂直速度*/
	@Excel(name = "垂直速度", width = 15)
    @ApiModelProperty(value = "垂直速度")
    private Double vspeed;
	/**direc*/
	@Excel(name = "direc", width = 15)
    @ApiModelProperty(value = "direc")
    private Double direc;
	/**altitudep*/
	@Excel(name = "altitudep", width = 15)
    @ApiModelProperty(value = "altitudep")
    private Double altitudep;
	/**altitudeg*/
	@Excel(name = "altitudeg", width = 15)
    @ApiModelProperty(value = "altitudeg")
    private Double altitudeg;
	/**高度（单位：米）*/
	@Excel(name = "高度（单位：米）", width = 15)
    @ApiModelProperty(value = "高度（单位：米）")
    private Double heightAgl;
	/**物理地址*/
	@Excel(name = "物理地址", width = 15)
    @ApiModelProperty(value = "物理地址")
    private String mac;
	/**信号强度*/
	@Excel(name = "信号强度", width = 15)
    @ApiModelProperty(value = "信号强度")
    private Double rssi;
	/**频率（HZ，除以10e5得到Mhz）*/
	@Excel(name = "频率（HZ，除以10e5得到Mhz）", width = 15)
    @ApiModelProperty(value = "频率（HZ，除以10e5得到Mhz）")
    private String freq;
	/**角度*/
	@Excel(name = "角度", width = 15)
    @ApiModelProperty(value = "角度")
    private Double angle;
	/**距离*/
	@Excel(name = "距离", width = 15)
    @ApiModelProperty(value = "距离")
    private Integer distance;
	/**时间戳*/
	@Excel(name = "时间戳", width = 15)
    @ApiModelProperty(value = "时间戳")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date date;
}
