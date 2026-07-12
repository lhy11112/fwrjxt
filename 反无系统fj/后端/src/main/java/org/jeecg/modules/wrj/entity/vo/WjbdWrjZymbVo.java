package org.jeecg.modules.wrj.entity.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.io.Serializable;

/**
 * @Description: 武警部队_无人机_重要目标分组实体
 * @Author: jeecg-boot
 * @Date:   2025-11-03
 * @Version: V1.0
 */
@Data
@TableName("wjbd_wrj_zymb")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="wjbd_wrj_zymb对象", description="武警部队_无人机_重要目标")
public class WjbdWrjZymbVo implements Serializable {
    private static final long serialVersionUID = 1L;
	/**类型*/
    @ApiModelProperty(value = "类型")
    private String type;
	/**数量*/
    @ApiModelProperty(value = "数量")
    private String sl;
}
