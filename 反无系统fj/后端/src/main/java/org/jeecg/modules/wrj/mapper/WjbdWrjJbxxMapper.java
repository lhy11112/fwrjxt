package org.jeecg.modules.wrj.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.wrj.entity.WjbdWrjJbxx;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: 武警部队_无人机_基本信息
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
public interface WjbdWrjJbxxMapper extends BaseMapper<WjbdWrjJbxx> {

    IPage<WjbdWrjJbxx> listByKyid(Page<WjbdWrjJbxx> page, @Param("kyid") String kyid);

    List<Map<String, Object>> getWrjCount();

    /**
     * 查询所有白名单飞机信息
     * @return
     */
    List<WjbdWrjJbxx> getBmdList();

    /**
     * 查询所有黑名单飞机信息
     * @return
     */
    List<WjbdWrjJbxx> getHmdList();
    /**
     * 根据无人机序列号查询无人机基本信息
     * @param SerialNumber
     * @return
     */
    @Select(" select * from wjbd_wrj_jbxx where serial_number=#{SerialNumber} limit 1 ")
    WjbdWrjJbxx  getWrjJbxxBySerialNumber(String SerialNumber);
}
