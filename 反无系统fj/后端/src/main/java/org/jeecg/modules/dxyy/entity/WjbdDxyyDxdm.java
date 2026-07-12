package org.jeecg.modules.dxyy.entity;

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
 * @Description: 武警部队_典型应用_地形地貌
 * @Author: jeecg-boot
 * @Date:   2025-11-03
 * @Version: V1.0
 */
@Data
@TableName("wjbd_dxyy_dxdm")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="wjbd_dxyy_dxdm对象", description="武警部队_典型应用_地形地貌")
public class WjbdDxyyDxdm implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;
	/**地名名称*/
	@Excel(name = "地名名称", width = 15)
    @ApiModelProperty(value = "地名名称")
    private java.lang.String dmmc;
	/**经度*/
	@Excel(name = "经度", width = 15)
    @ApiModelProperty(value = "经度")
    private java.lang.String jd;
	/**纬度*/
	@Excel(name = "纬度", width = 15)
    @ApiModelProperty(value = "纬度")
    private java.lang.String wd;
	/**地形地貌类型*/
	@Excel(name = "地形地貌类型", width = 15)
    @ApiModelProperty(value = "地形地貌类型")
    private java.lang.String dxdmlx;
	/**面积*/
	@Excel(name = "面积", width = 15)
    @ApiModelProperty(value = "面积")
    private java.lang.String mj;
	/**地理位置*/
	@Excel(name = "地理位置", width = 15)
    @ApiModelProperty(value = "地理位置")
    private java.lang.String dlwz;
}
