package org.jeecg.modules.uav.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 设备心跳状态表（对应文档心跳包结构）
 */
@Data
@TableName("uav_device_heartbeat")
public class UavDeviceHeartbeat {
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long id;
    private Integer stationId;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date dataTime;
    private Integer mainCard;
    private Integer trapCard;
    private Integer compass;
    private Integer disturbCard;
    private Float longitude;
    private Float latitude;
    private Integer altitude;
    private Float angle;
    private Float cpuRate;
    private Float diskUsage;
    private Float cardTemp;
    private Float ampTemp;
    private Integer workState;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
    