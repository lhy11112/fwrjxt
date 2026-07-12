package org.jeecg.modules.uav.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.jeecg.modules.uav.entity.UavOperateLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UavOperateLogMapper extends BaseMapper<UavOperateLog> {
    List<UavOperateLog> selectByCmdType(
            @Param("cmdType") Integer cmdType,
            @Param("stationId") Integer stationId,
            @Param("limit") Integer limit
    );
    /**
     * 删除一周前的设备操作数据
     */
    @Delete(" DELETE FROM uav_operate_log \n" +
            "WHERE operate_time < DATE_SUB(NOW(), INTERVAL 1 WEEK)")
    void deleteINTERVAL1Week();
}
    