package org.jeecg.modules.wrj.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * @Description: 武警部队_无人机_编携配装
 * @Author: jeecg-boot
 * @Date:   2025-12-16
 * @Version: V1.0
 */
@Data
@TableName("wjbd_wrj_bxpz")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="wjbd_wrj_bxpz对象", description="武警部队_无人机_编携配装")
public class WjbdWrjBxpz implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
	/**名称*/
	@Excel(name = "名称", width = 15)
    @ApiModelProperty(value = "名称")
    private java.lang.String mc;
    /**上级id*/
    @Excel(name = "上级id", width = 15)
    @ApiModelProperty(value = "上级id")
    private java.lang.String pid;
	/**内容*/
	@Excel(name = "内容", width = 15)
    @ApiModelProperty(value = "内容")
    private java.lang.String nr;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private java.lang.String remark;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
    private java.lang.String cjr;
	/**创建人ID*/
	@Excel(name = "创建人ID", width = 15)
    @ApiModelProperty(value = "创建人ID")
    private java.lang.String cjrid;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
    private java.util.Date cjsj;
	/**操作人*/
	@Excel(name = "操作人", width = 15)
    @ApiModelProperty(value = "操作人")
    private java.lang.String czr;
	/**操作人ID*/
	@Excel(name = "操作人ID", width = 15)
    @ApiModelProperty(value = "操作人ID")
    private java.lang.String czrid;
	/**操作时间*/
	@Excel(name = "操作时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "操作时间")
    private java.util.Date czsj;
    /** 子节点（非数据库字段） */
    @TableField(exist = false)
    private List<WjbdWrjBxpz> children;
}
