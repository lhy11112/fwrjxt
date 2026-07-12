package org.jeecg.modules.wrj.mapper;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.wrj.entity.WjbdDxyyBh;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 武警不对_典型应用_无人机
 * @Author: jeecg-boot
 * @Date: 2026-01-19
 * @Version: V1.0
 */
public interface WjbdDxyyBhMapper extends BaseMapper<WjbdDxyyBh> {
    /**
     * 根据业务id、时间查询标绘信息
     *
     * @param ywid
     * @param datatime
     * @return
     */
    @Select("  select bh.* from  \n" +
            " wjbd_dxyy_bh bh \n" +
            " left join  \n" +
            " wjbd_wrj_tyjh  ty \n" +
            " on bh.YW_ID=ty.id\n" +
            " where bh.YW_ID=#{ywid}  and bhsj BETWEEN  ty.jhks  and #{datatime} ")
    List<WjbdDxyyBh> getBhByYwIdDataTime(String ywid, Date datatime);
}
