package org.jeecg.modules.dxyy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.dxyy.entity.WjbdDxyyDxdm;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.wrj.entity.WjbdWrjZymb;

/**
 * @Description: 武警部队_典型应用_地形地貌
 * @Author: jeecg-boot
 * @Date:   2025-11-03
 * @Version: V1.0
 */
public interface WjbdDxyyDxdmMapper extends BaseMapper<WjbdDxyyDxdm> {

    List<WjbdDxyyDxdm> getDxdmByJwdAndJl(@Param("jd") String jd, @Param("wd")String wd, @Param("jl")String jl);
}
