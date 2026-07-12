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
 * @Description: 知识库_文件_内容
 * @Author: jeecg-boot
 * @Date: 2025-10-27
 * @Version: V1.0
 */
@Data
@TableName("wjbd_wrj_zsk_wj_nr")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "wjbd_wrj_zsk_wj_nr对象", description = "知识库_文件_内容")
public class WjbdHsZskWjNr implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long id;
    /**
     * 知识库主键
     */
    @Excel(name = "知识库主键", width = 15)
    @ApiModelProperty(value = "知识库主键")
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long zskId;
    /**
     * 知识库文件ID
     */
    @Excel(name = "知识库文件ID", width = 15)
    @ApiModelProperty(value = "知识库文件ID")
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private java.lang.Long zskWjId;
    /**
     * 内容
     */
    @Excel(name = "内容", width = 15)
    @ApiModelProperty(value = "内容")
    private java.lang.String nr;
}
