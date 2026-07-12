package org.jeecg.modules.wrj.entity;

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
 * @Description: 武警部队_无人机_推演计划_数据
 * @Author: jeecg-boot
 * @Date:   2025-09-22
 * @Version: V1.0
 */
@Data
@TableName("wjbd_wrj_tyjh_data")
@ApiModel(value="wjbd_wrj_tyjh_data对象", description="武警部队_无人机_推演计划_数据")
public class WjbdWrjTyjhData implements Serializable {
	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long id;
	/**推演计划id*/
	@Excel(name = "推演计划id", width = 15)
    @ApiModelProperty(value = "推演计划id")
    private String tyjhId;
	/**无人机序列号（char[64]）*/
	@Excel(name = "无人机序列号（char[64]）", width = 15)
    @ApiModelProperty(value = "无人机序列号（char[64]）")
    private String serial;
	/**机型（char[64]）*/
	@Excel(name = "机型（char[64]）", width = 15)
    @ApiModelProperty(value = "机型（char[64]）")
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
    private String ridSsid;
    private Double sd;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date dataTime;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
