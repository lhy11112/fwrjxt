package org.jeecg.modules.wrj.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
import java.util.List;

/**
 * @Description: 知识库
 * @Author: jeecg-boot
 * @Date: 2024-09-14
 */
@Data
@TableName("wjbd_wrj_zsk")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "知识库", description = "知识库")
public class WjbdHsZsk implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long id;
    /**
     * 知识库名称
     */
    @Excel(name = "知识库名称", width = 15)
    @ApiModelProperty(value = "知识库名称")
    private String zskMc;
    /**
     * 知识库内码，对应的是向量库的表明
     */
    @Excel(name = "知识库内码，对应的是向量库的表明", width = 15)
    @ApiModelProperty(value = "知识库内码，对应的是向量库的表明")
    private String zskNm;
    /**
     * 知识库描述
     */
    @Excel(name = "知识库描述", width = 15)
    @ApiModelProperty(value = "知识库描述")
    private String zskMs;
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
    /**
     * 知识库类型 1:自有知识库 2:数据中台 3:大模型 4:处置链知识库
     */
    @Excel(name = "知识库类型 1:自有知识库 2:数据中台 3:大模型 4:处置链知识库", width = 15)
    @ApiModelProperty(value = "知识库类型 1:自有知识库 2:数据中台 3:大模型 4:处置链知识库")
    private Integer zskLx;

    /**
     * 是否选择
     */
    @TableField(exist = false)
    private Integer sfxz;
    @TableField(exist = false)
    private List<WjbdHsZskWj> wjbdHsZskWj;
}
