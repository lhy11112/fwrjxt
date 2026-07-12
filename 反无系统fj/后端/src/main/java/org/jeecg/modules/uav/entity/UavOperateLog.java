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
 * 设备操作日志表
 */
@Data
@TableName("uav_operate_log")
public class UavOperateLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer stationId;
    private String deviceType;
    private String cmdType;
    private String cmdName;
    private String cmdParam;
    private String result;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date operateTime;
}
    