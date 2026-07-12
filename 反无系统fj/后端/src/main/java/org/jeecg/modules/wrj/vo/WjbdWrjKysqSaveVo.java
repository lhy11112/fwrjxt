package org.jeecg.modules.wrj.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
import java.util.Date;
import java.util.List;

/**
 * @Description: 武警部队_无人机_空域授权
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class WjbdWrjKysqSaveVo implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
	/**空域id*/
	@Excel(name = "空域id", width = 15)
    @ApiModelProperty(value = "空域id")
    private String kyid;
	/**无人机id*/
	@Excel(name = "无人机id", width = 15)
    @ApiModelProperty(value = "无人机id")
    private List<String> wrjids;

}
