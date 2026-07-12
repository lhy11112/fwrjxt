package org.jeecg.modules.uav.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 侦测报文数据表（对应文档报文结构）
 */
@Data
@TableName("uav_detect_msg")
public class UavDetectMsg {
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long id;
    private Integer stationId;
    @Dict(dictTable ="wjbd_wrj_jbxx",dicText = "brand",dicCode = "serial_number")
    private String serial;
    private String model;
    private Double dronLng;
    private Double dronLat;
    private Float homeLng;
    private Float homeLat;
    private Float pilotLng;
    private Float pilotLat;
    private Double altitude;
    private Double height;
    private Float eastV;
    private Float northV;
    private Float upV;
    private Long freq;
    private Float rssi;
    private Float distance;
    /**
     * 飞手执照代码
     */
    private String uuid;
    /**
     * 飞机角度
     */
    private Float angle;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date dataTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**速度*/
    @Excel(name = "速度", width = 15)
    @ApiModelProperty(value = "速度")
    private Double sd;
    /**物理地址*/
    @Excel(name = "物理地址", width = 15)
    @ApiModelProperty(value = "物理地址")
    private String mac;
    /**解密类型*/
    @Excel(name = "解密类型", width = 15)
    @ApiModelProperty(value = "解密类型")
    private String jmlx;
    /** 数据类型 */
    private Integer DataType;

    /** 融合标识 */
    private Integer FusedFlag;
    /**
     * 威胁等级：red、green、yellow
     */
    @TableField(exist = false)
    private String wxdj;
}
    