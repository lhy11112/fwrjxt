package org.jeecg.modules.dxyy.entity.Vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * @Description: 部队人员情况
 * @Author: jeecg-boot
 * @Date:   2025-02-20
 *
 */
@Data
public class ZzllBdRyVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private HashMap<String, Object> ChineseProperty = new HashMap<String, Object>();

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
    /**部队番号*/
    @Excel(name = "部队番号", width = 15)
    @ApiModelProperty(value = "部队番号")
    private String bdfh;
    /**部队简称*/
    @Excel(name = "部队简称", width = 15)
    @ApiModelProperty(value = "部队简称")
    private String bdjc;

    public void setBdjb(String bdjb) {
        this.bdjb=bdjb;
        this.bdjc = this.getBdfh().replaceAll("中国人民武装警察部队","");
    }

    /**编制序号*/
    @Excel(name = "编制序号", width = 15)
    @ApiModelProperty(value = "编制序号")
    private String bzxh;
    /**编制番号*/
    @Excel(name = "编制番号", width = 15)
    @ApiModelProperty(value = "编制番号")
    private String bzfh;
    /**编制简称*/
    @Excel(name = "编制简称", width = 15)
    @ApiModelProperty(value = "编制简称")
    private String bzjc;
    /**部队级别*/
    @Excel(name = "部队级别", width = 15)
    @ApiModelProperty(value = "部队级别")
    private String bdjb;
    /**上级单位内码*/
    @Excel(name = "上级单位内码", width = 15)
    @ApiModelProperty(value = "上级单位内码")
    private String sjbdnm;
	/**编制数（人）*/
	@Excel(name = "编制数（人）", width = 15)
    @ApiModelProperty(value = "编制数（人）")
    private BigDecimal bzs;
	/**实有数（人）*/
	@Excel(name = "实有数（人）", width = 15)
    @ApiModelProperty(value = "实有数（人）")
    private Integer sys;
    /**实际抽组实有人数*/
    @Excel(name = "实际抽组实有人数", width = 15)
    @ApiModelProperty(value = "实际抽组实有人数")
    private Integer sjczsys;
	/**在位数（人）*/
	@Excel(name = "在位数（人）", width = 15)
    @ApiModelProperty(value = "在位数（人）")
    private BigDecimal zws;
	/**可出动数（人）*/
	@Excel(name = "可出动数（人）", width = 15)
    @ApiModelProperty(value = "可出动数（人）")
    private BigDecimal kcds;
    /**经度*/
    @Excel(name = "经度", width = 15)
    @ApiModelProperty(value = "经度")
    private String jd;
    /**纬度*/
    @Excel(name = "纬度", width = 15)
    @ApiModelProperty(value = "纬度")
    private String wd;
    /**便携配装*/
    @Excel(name = "便携配装", width = 15)
    @ApiModelProperty(value = "便携配装")
    private String bxpz;

    public void setBdjc(String bdjc) {
        this.bdjc = bdjc;
        ChineseProperty.put("部队简称",bdjc);
    }

    public void setBzs(BigDecimal bzs) {
        this.bzs = bzs;
    }

    public void setSys(Integer sys) {
        this.sys = sys;
        ChineseProperty.put("当前部队实有人数",sys);
    }

    public void setJd(String jd) {
        ChineseProperty.put("当前部队经度",jd);
        this.jd = jd;
    }

    public void setWd(String wd) {
        this.wd = wd;
        ChineseProperty.put("当前部队纬度",wd);

    }

    public void setJl(Integer jl) {
        this.jl = jl;
        ChineseProperty.put("距离事发地距离",jl+"公里");
    }

    /**距离中心点位置信息*/
    @Excel(name = "距离", width = 15)
    @ApiModelProperty(value = "距离")

    private Integer jl;
    private List<ZzllBdRyVo> children;
}
