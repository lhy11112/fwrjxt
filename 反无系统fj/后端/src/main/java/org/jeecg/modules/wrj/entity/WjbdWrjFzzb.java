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
 * @Description: 武警部队_无人机_反制装备
 * @Author: jeecg-boot
 * @Date:   2025-12-16
 * @Version: V1.0
 */
@Data
@TableName("wjbd_wrj_fzzb")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="wjbd_wrj_fzzb对象", description="武警部队_无人机_反制装备")
public class WjbdWrjFzzb implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
	/**分类(侦察设备、干扰设备、反制设备)*/
	@Excel(name = "分类(侦察设备、干扰设备、反制设备)", width = 15)
    @ApiModelProperty(value = "分类(侦察设备、干扰设备、反制设备)")
    private java.lang.String fl;
	/**名称*/
	@Excel(name = "名称", width = 15)
    @ApiModelProperty(value = "名称")
    private java.lang.String mc;
	/**功能特点*/
	@Excel(name = "功能特点", width = 15)
    @ApiModelProperty(value = "功能特点")
    private java.lang.String gntd;
	/**技术指标*/
	@Excel(name = "技术指标", width = 15)
    @ApiModelProperty(value = "技术指标")
    private java.lang.String jszb;
	/**装备图册*/
	@Excel(name = "装备图册", width = 15)
    @ApiModelProperty(value = "装备图册")
    private java.lang.String tp;
	/**装备数量*/
	@Excel(name = "装备数量", width = 15)
    @ApiModelProperty(value = "装备数量")
    private java.lang.String sl;
	/**装备来源*/
	@Excel(name = "装备来源", width = 15)
    @ApiModelProperty(value = "装备来源")
    private java.lang.String zbly;
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
	/**三维模型地址*/
	@Excel(name = "三维模型地址", width = 15)
    @ApiModelProperty(value = "三维模型地址")
    private java.lang.String mxdz;
}
