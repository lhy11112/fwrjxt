package org.jeecg.modules.wrj.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 武警部队_无人机_基本信息
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class UpdateSqxxVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> ids;


    private List<String> wrjids;

    private String mdlx;

}
