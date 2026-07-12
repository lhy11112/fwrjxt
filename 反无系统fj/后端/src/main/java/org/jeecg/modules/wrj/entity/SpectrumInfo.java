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
 * @Description: 频谱结果数据
 * @Author: jeecg-boot
 * @Date:   2025-12-24
 * @Version: V1.0
 */
@Data
@TableName("spectrum_info")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="spectrum_info对象", description="频谱结果数据")
public class SpectrumInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long id;
    private Integer stationId;
	/**通道号（适配u8无符号8位整数）*/
	@Excel(name = "通道号（适配u8无符号8位整数）", width = 15)
    @ApiModelProperty(value = "通道号（适配u8无符号8位整数）")
    private java.lang.String channel;
	/**数据类型：1=频谱（适配u8）*/
	@Excel(name = "数据类型：1=频谱（适配u8）", width = 15)
    @ApiModelProperty(value = "数据类型：1=频谱（适配u8）")
    private java.lang.String dataType;
	/**开始频率(Hz)（适配u64无符号64位整数）*/
	@Excel(name = "开始频率(Hz)（适配u64无符号64位整数）", width = 15)
    @ApiModelProperty(value = "开始频率(Hz)（适配u64无符号64位整数）")
    private Double startFreq;
	/**结束频率(Hz)（适配u64）*/
	@Excel(name = "结束频率(Hz)（适配u64）", width = 15)
    @ApiModelProperty(value = "结束频率(Hz)（适配u64）")
    private Double stopFreq;
	/**步进频率(Hz)（适配u32无符号32位整数）*/
	@Excel(name = "步进频率(Hz)（适配u32无符号32位整数）", width = 15)
    @ApiModelProperty(value = "步进频率(Hz)（适配u32无符号32位整数）")
    private Double stepFreq;
	/**回传的频点数据个数（适配u32，与pData数组长度一致）*/
	@Excel(name = "回传的频点数据个数（适配u32，与pData数组长度一致）", width = 15)
    @ApiModelProperty(value = "回传的频点数据个数（适配u32，与pData数组长度一致）")
    private java.lang.String dataLen;
	/**回传的电平数组(dBm)，存储short类型的JSON数组*/
	@Excel(name = "回传的电平数组(dBm)，存储short类型的JSON数组", width = 15)
    @ApiModelProperty(value = "回传的电平数组(dBm)，存储short类型的JSON数组")
    private java.lang.String pData;
	/**数据入库时间，便于追溯*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "数据入库时间，便于追溯")
    private java.util.Date createTime;
}
