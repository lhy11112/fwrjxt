package org.jeecg.modules.wrj.entity.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.modules.wrj.entity.WjbdWrjZymb;

import java.io.Serializable;
import java.util.List;

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
public class WjbdWrjZymbVos implements Serializable {
    private static final long serialVersionUID = 1L;
	/**类型*/
    @ApiModelProperty(value = "类型")
    private List<WjbdWrjZymb> zymbxx;
	/**目标统计信息*/
    @ApiModelProperty(value = "目标统计信息")
    private List<WjbdWrjZymbVo> mbtjxx;
}
