package org.jeecg.modules.dxyy.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: zzll_bd_bs
 * @Author: jeecg-boot
 * @Date:   2024-04-26
 *  
 */
@Data
@TableName("dbsjgx_zzll_bd_bs")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="作战力量_部队_部署对象", description="作战力量_部队_部署")
public class ZzllBdBs implements Serializable {
    private static final long serialVersionUID = 1L;

	/**部署类别内码*/
	@Excel(name = "部署类别内码", width = 15)
    @ApiModelProperty(value = "部署类别内码")
    private String bslbnm;
	/**部队内码*/
	@Excel(name = "部队内码", width = 15)
    @ApiModelProperty(value = "部队内码")
    private String bdnm;
	/**天文时间*/
	@Excel(name = "天文时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "天文时间")
    private Date twsj;
	/**部署形式内码*/
	@Excel(name = "部署形式内码", width = 15)
    @ApiModelProperty(value = "部署形式内码")
    private String bsxsnm;
	/**地名内码*/
	@Excel(name = "地名内码", width = 15)
    @ApiModelProperty(value = "地名内码")
    private String dmnm;
	/**扩展地名*/
	@Excel(name = "扩展地名", width = 15)
    @ApiModelProperty(value = "扩展地名")
    private String kzdm;
	/**经度*/
	@Excel(name = "经度", width = 15)
    @ApiModelProperty(value = "经度")
    private String jd;
	/**纬度*/
	@Excel(name = "纬度", width = 15)
    @ApiModelProperty(value = "纬度")
    private String wd;
	/**高程（米）*/
	@Excel(name = "高程（米）", width = 15)
    @ApiModelProperty(value = "高程（米）")
    private BigDecimal gc;
	/**作战时间*/
	@Excel(name = "作战时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "作战时间")
    private Date zzsj;
}
