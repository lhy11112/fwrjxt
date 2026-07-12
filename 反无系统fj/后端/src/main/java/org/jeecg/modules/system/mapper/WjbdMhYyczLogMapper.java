package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.jeecg.modules.system.entity.WjbdMhYyczLog;

/**
 * @Description: 武警部队_典型应用_操作日志
 * @Author: jeecg-boot
 * @Date:   2025-07-29
 *  
 */
public interface WjbdMhYyczLogMapper extends BaseMapper<WjbdMhYyczLog> {
    /**
     * 删除两年前的操作日志数据
     */
    @Delete(" DELETE FROM wjbd_mh_yycz_log \n" +
            "WHERE CZSJ < DATE_SUB(NOW(), INTERVAL 2 YEAR)")
    void deleteINTERVAL2YEAR();
}
