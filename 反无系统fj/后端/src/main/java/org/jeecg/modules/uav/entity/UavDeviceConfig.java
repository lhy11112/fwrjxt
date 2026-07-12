package org.jeecg.modules.uav.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 设备网络配置表（IP/端口动态存储）
 */
@Data
@TableName("uav_device_config")
public class UavDeviceConfig {
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long id;
    private String deviceId;
    private String name;
    private Integer stationId;

    private String deviceType;
    private String deviceIp;
    private Integer devicePort;
    private String protocolVersion;
    private Integer isValid;
    /**
     * "CONNECTED"、"DISCONNECTED"、"WARN"
     */
    private String status;
    /**
     * 经度
     */
    private Float jd;
    /**
     * 维度
     */
    private Float wd;
    /**
     * 高度
     */
    private java.math.BigDecimal gd;
    /**
     * 侦测半径
     */
    private String zcbj;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    /**
     * 弹药情况
     */
    private String dyqk;
    /**
     * 协议类型: 协议类型(、TCPSocket、TCPServerSocket)
     */
    private String type;
}
    