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
 * @Description: 武警部队_无人机_录屏文件
 * @Author: jeecg-boot
 * @Date:   2025-11-03
 * @Version: V1.0
 */
@Data
@TableName("wjbd_wrj_lpwj")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="wjbd_wrj_lpwj对象", description="武警部队_无人机_录屏文件")
public class WjbdWrjLpwj implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private java.lang.String id;
	/**软件名称*/
	@Excel(name = "软件名称", width = 15)
    @ApiModelProperty(value = "软件名称")
    private java.lang.String mc;
	/**启动文件目录*/
	@Excel(name = "启动文件目录", width = 15)
    @ApiModelProperty(value = "启动文件目录")
    private java.lang.String qdwjml;
    /**用户id*/
    @Excel(name = "用户id", width = 15)
    @ApiModelProperty(value = "用户id")
    private java.lang.String yhid;
}
