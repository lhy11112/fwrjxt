package org.jeecg.modules.uav.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 无人机解析数据表（对应文档测向结果）
 */
@Data
@TableName("uav_df_data")
public class UavDfData {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer stationId;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date dataTime;
    private Integer targetType;
    private Integer detectType;
    private Long freq;
    private Integer dk;
    private Float longitude;
    private Float latitude;
    private Float angle;
    private Float signalLevel;
    private Float compass;
    private Float distance;
    private Float speed;
    private Float height;
    private String uavModel;
    private String uavId;
    private String deviceId;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**
     * 威胁等级：red、green、yellow
     */
    @TableField(exist = false)
    private String wxdj;
}
    