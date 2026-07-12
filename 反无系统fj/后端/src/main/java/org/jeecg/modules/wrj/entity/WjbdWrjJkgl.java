package org.jeecg.modules.wrj.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
 * @Description: 武警部队_无人机_接口管理
 * @Author: jeecg-boot
 * @Date:   2025-10-24
 * @Version: V1.0
 */
@Data
@TableName("wjbd_wrj_jkgl")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="wjbd_wrj_jkgl对象", description="武警部队_无人机_接口管理")
public class WjbdWrjJkgl implements Serializable {
    private static final long serialVersionUID = 1L;

    /**id*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long id;
	/**接口地址*/
	@Excel(name = "接口地址", width = 15)
    @ApiModelProperty(value = "接口地址")
    private java.lang.String url;
	/**授权账号*/
	@Excel(name = "授权账号", width = 15)
    @ApiModelProperty(value = "授权账号")
    private java.lang.String client;
	/**登录密钥*/
	@Excel(name = "登录密钥", width = 15)
    @ApiModelProperty(value = "登录密钥")
    private java.lang.String secret;
	/**接口标识*/
	@Excel(name = "接口标识", width = 15)
    @ApiModelProperty(value = "接口标识")
    private java.lang.String bm;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private java.lang.String bz;
	/**1、中台   2、其他待定  3、返回给前端的*/
	@Excel(name = "1、中台   2、其他待定  3、返回给前端的", width = 15)
    @ApiModelProperty(value = "1、中台   2、其他待定  3、返回给前端的")
    private java.lang.Integer lx;
}
