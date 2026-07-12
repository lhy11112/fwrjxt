package org.jeecg.modules.wrj.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
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
 * @Description: 武警部队_无人机_重要目标
 * @Author: jeecg-boot
 * @Date:   2025-11-03
 * @Version: V1.0
 */
@Data
@TableName("wjbd_wrj_zymb")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="wjbd_wrj_zymb对象", description="武警部队_无人机_重要目标")
public class WjbdWrjZymb implements Serializable {
    private static final long serialVersionUID = 1L;

	/**ID*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "ID")
    private java.lang.String id;
	/**名称*/
	@Excel(name = "名称", width = 15)
    @ApiModelProperty(value = "名称")
    private java.lang.String mc;
	/**经度*/
	@Excel(name = "经度", width = 15)
    @ApiModelProperty(value = "经度")
    private java.lang.String jd;
	/**纬度*/
	@Excel(name = "纬度", width = 15)
    @ApiModelProperty(value = "纬度")
    private java.lang.String wd;
	/**人员类型*/
	@Excel(name = "人员类型", width = 15)
    @ApiModelProperty(value = "人员类型")
    private java.lang.String rylx;
	/**数量*/
	@Excel(name = "数量", width = 15)
    @ApiModelProperty(value = "数量")
    private java.lang.String sl;
	/**装备*/
	@Excel(name = "装备", width = 15)
    @ApiModelProperty(value = "装备")
    private java.lang.String zb;
	/**设施概况*/
	@Excel(name = "设施概况", width = 15)
    @ApiModelProperty(value = "设施概况")
    private java.lang.String ssgk;
    /**类型:民生目标、执勤目标、友邻单位*/
    @Excel(name = "类型:民生目标、执勤目标、友邻单位", width = 15)
    @ApiModelProperty(value = "类型:民生目标、执勤目标、友邻单位")
    private String type;
    /**子类型:医院、学校、商店、超市*/
    @Excel(name = "子类型:医院、学校、商店、超市", width = 15)
    @ApiModelProperty(value = "子类型:医院、学校、商店、超市")
    private String subtype;
	@TableField(exist = false)
    private String jl;
}
