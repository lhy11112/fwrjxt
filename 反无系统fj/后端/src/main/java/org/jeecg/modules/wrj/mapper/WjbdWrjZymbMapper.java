package org.jeecg.modules.wrj.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.wrj.entity.WjbdWrjZymb;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.wrj.entity.vo.WjbdWrjZymbVo;
import org.jeecg.modules.wrj.vo.TargetData;

/**
 * @Description: 武警部队_无人机_重要目标
 * @Author: jeecg-boot
 * @Date:   2025-11-03
 * @Version: V1.0
 */
public interface WjbdWrjZymbMapper extends BaseMapper<WjbdWrjZymb> {
    /**
     *  根据经纬度距离大类查询重要目标数据
     * @param jd 经度
     * @param wd 维度
     * @param jl 距离
     * @param typeList 大类
     * @return
     */
    List<WjbdWrjZymb> getWrjZymbByJwdAndJl(@Param("jd") String jd, @Param("wd")String wd, @Param("jl")String jl,@Param("typeList")List<String> typeList);

    /**
     *  根据经纬度距离大类查询重要目标数据统计信息
     * @param jd 经度
     * @param wd 维度
     * @param jl 距离
     * @param typeList 大类
     * @return
     */
    List<WjbdWrjZymbVo> getWrjZymbByJwdAndJlGroupBySubType(@Param("jd") String jd, @Param("wd")String wd, @Param("jl")String jl, @Param("typeList")List<String> typeList);
    /**
     * 根据正方形区域查询目标数据
     * @param minJd 最小经度
     * @param maxJd 最大经度
     * @param minWd 最小纬度
     * @param maxWd 最大纬度
     * @return 区域内的目标数据列表
     */
    @Select(" SELECT \n" +
            "            ID, MC, JD, WD, RYLX, SL, ZB, SSGK, TYPE, SUBTYPE\n" +
            "        FROM wjbd_wrj_zymb\n" +
            "        WHERE \n" +
            "            CAST(JD AS DECIMAL(10,6)) BETWEEN #{minJd} AND #{maxJd}\n" +
            "            AND CAST(WD AS DECIMAL(10,6)) BETWEEN #{minWd} AND #{maxWd} ")
    List<TargetData> selectBySquareArea(BigDecimal minJd, BigDecimal maxJd, BigDecimal minWd, BigDecimal maxWd);
}
