package org.jeecg.modules.uav.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 设备连接状态日志表
 */
@Data
@TableName("uav_connect_log")
public class UavConnectLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer stationId;
    private String deviceIp;
    private Integer devicePort;
    private String eventType;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date eventTime;
    private String reason;
}
    