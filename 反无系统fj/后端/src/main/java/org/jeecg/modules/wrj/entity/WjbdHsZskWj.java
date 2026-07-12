package org.jeecg.modules.wrj.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: wjbd_wrj_zsk_wj
 * @Author: jeecg-boot
 * @Date: 2024-09-14
 */
@Data
@TableName("wjbd_wrj_zsk_wj")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "wjbd_wrj_zsk_wj对象", description = "wjbd_wrj_zsk_wj")
public class WjbdHsZskWj implements Serializable {
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
     * 文件名称
     */
    @Excel(name = "文件名称", width = 15)
    @ApiModelProperty(value = "文件名称")
    private String wjMc;
    /**
     * 文件内码
     */
    @Excel(name = "文件内码", width = 15)
    @ApiModelProperty(value = "文件内码")
    private String wjNm;
    /**
     * 文件大小，单位是字节
     */
    @Excel(name = "文件大小，单位是字节", width = 15)
    @ApiModelProperty(value = "文件大小，单位是字节")
    private Long wjdx;
    /**
     * 文件存储的绝对路径，URL
     */
    @Excel(name = "文件存储的绝对路径，URL", width = 15)
    @ApiModelProperty(value = "文件存储的绝对路径，URL")
    private String fwqWjlj;
    /**
     * 创建人名称
     */
    @Excel(name = "创建人名称", width = 15)
    @ApiModelProperty(value = "创建人名称")
    private String chjrMc;
    /**
     * 创建人ID
     */
    @Excel(name = "创建人ID", width = 15)
    @ApiModelProperty(value = "创建人ID")
    private String chjr;
    /**
     * 入库时间
     */
    @Excel(name = "入库时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "入库时间")
    private Date rksj;


}
