package org.jeecg.modules.dxyy.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.dxyy.entity.WjbdWrjYhglCs;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 武警部队_无人机_用户管理_参数
 * @Author: jeecg-boot
 * @Date:   2025-09-01
 * @Version: V1.0
 */
public interface WjbdWrjYhglCsMapper extends BaseMapper<WjbdWrjYhglCs> {
    @Select(" select xh,dmnm,mc from wjbd_dxyy_dm  where qc like concat('%',#{dnmc},'%')  limit 1 ")
    Map<String,Object> getListDmNm(String dnmc);

    @Select(" select xh,dmnm,mc,qc from wjbd_dxyy_dm  where dmnm =#{dmnm}")
    Map<String,Object> getListDmNmByDmNm(String dmnm);
}
