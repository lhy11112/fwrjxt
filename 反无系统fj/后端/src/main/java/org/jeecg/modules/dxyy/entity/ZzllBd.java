package org.jeecg.modules.dxyy.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: zzll_bd
 * @Author: jeecg-boot
 * @Date:   2024-04-26
 *
 */
@Data
@TableName("dbsjgx_zzll_bd")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="作战力量_部队对象", description="作战力量_部队")
public class ZzllBd implements Serializable {
    private static final long serialVersionUID = 1L;

	/**部队内码*/
	@Excel(name = "部队内码", width = 15)
    @ApiModelProperty(value = "部队内码")
    private String bdnm;
	/**部队划分内码*/
	@Excel(name = "部队划分内码", width = 15)
    @ApiModelProperty(value = "部队划分内码")
    private String bdhfnm;
	/**部队序号*/
	@Excel(name = "部队序号", width = 15)
    @ApiModelProperty(value = "部队序号")
    private String bdxh;
    public void setBdxh(String bdxh) {
        this.bdxh = bdxh;
        if(bdxh.length()>2)
            this.sjbdxh=bdxh.substring(0,bdxh.length()-2);
    }
    /**部队番号*/
	@Excel(name = "部队番号", width = 15)
    @ApiModelProperty(value = "部队番号")
    private String bdfh;
	/**部队简称*/
	@Excel(name = "部队简称", width = 15)
    @ApiModelProperty(value = "部队简称")
    private String bdjc;
	/**编制序号*/
	@Excel(name = "编制序号", width = 15)
    @ApiModelProperty(value = "编制序号")
    private String bzxh;
    /**编制序号*/
    @TableField(exist = false)
    @Excel(name = "编制序号", width = 15)
    @ApiModelProperty(value = "编制序号")
    private String sjbdxh;
    /**编制番号*/
	@Excel(name = "编制番号", width = 15)
    @ApiModelProperty(value = "编制番号")
    private String bzfh;
	/**编制简称*/
	@Excel(name = "编制简称", width = 15)
    @ApiModelProperty(value = "编制简称")
    private String bzjc;
	/**上级单位内码*/
	@Excel(name = "上级单位内码", width = 15)
    @ApiModelProperty(value = "上级单位内码")
    @TableField(exist = false)
    private String sjbdnm;
    @ApiModelProperty(value = "子级单位集合")
    @TableField(exist=false)
    private List<ZzllBd> children;
}
