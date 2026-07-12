package org.jeecg.modules.wrj.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
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
 * @Description: 武警部队_无人机_反无战法
 * @Author: jeecg-boot
 * @Date:   2025-12-16
 * @Version: V1.0
 */
@Data
@TableName("wjbd_wrj_fwzf")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="wjbd_wrj_fwzf对象", description="武警部队_无人机_反无战法")
public class WjbdWrjFwzf implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
	/**分类(执勤、处置突发社会安全事件、防范和处置恐怖活动、防卫作战、海上维权执法)*/
	@Excel(name = "分类(执勤、处置突发社会安全事件、防范和处置恐怖活动、防卫作战、海上维权执法)", width = 15)
    @ApiModelProperty(value = "分类(执勤、处置突发社会安全事件、防范和处置恐怖活动、防卫作战、海上维权执法)")
    private java.lang.String fl;
	/**战法名称*/
	@Excel(name = "战法名称", width = 15)
    @ApiModelProperty(value = "战法名称")
    private java.lang.String mc;
	/**战法概述*/
	@Excel(name = "战法概述", width = 15)
    @ApiModelProperty(value = "战法概述")
    private java.lang.String zfgs;
	/**作战场景*/
	@Excel(name = "作战场景", width = 15)
    @ApiModelProperty(value = "作战场景")
    private java.lang.String zzcj;
	/**基本战法*/
	@Excel(name = "基本战法", width = 15)
    @ApiModelProperty(value = "基本战法")
    private java.lang.String jbzf;
	/**力量部署和体系构建*/
	@Excel(name = "力量部署和体系构建", width = 15)
    @ApiModelProperty(value = "力量部署和体系构建")
    private java.lang.String llbshtxgj;
	/**具体行动方法*/
	@Excel(name = "具体行动方法", width = 15)
    @ApiModelProperty(value = "具体行动方法")
    private java.lang.String jtxdff;
	/**战法验证情况*/
	@Excel(name = "战法验证情况", width = 15)
    @ApiModelProperty(value = "战法验证情况")
    private java.lang.String zfyzqk;
	/**战法应用需要把握的问题*/
	@Excel(name = "战法应用需要把握的问题", width = 15)
    @ApiModelProperty(value = "战法应用需要把握的问题")
    private java.lang.String zfyyxybwwt;
	/**战法文件*/
	@Excel(name = "战法文件", width = 15)
    @ApiModelProperty(value = "战法文件")
    private java.lang.String wj;
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
}
