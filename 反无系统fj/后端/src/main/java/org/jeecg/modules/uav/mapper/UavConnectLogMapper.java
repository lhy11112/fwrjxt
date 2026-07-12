package org.jeecg.modules.uav.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.jeecg.modules.uav.entity.UavConnectLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UavConnectLogMapper extends BaseMapper<UavConnectLog> {
    List<UavConnectLog> selectByStationIdAndTimeRange(
            @Param("stationId") Integer stationId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 删除一周前的设备连接数据
     */
    @Delete(" DELETE FROM uav_connect_log \n" +
            "WHERE event_time < DATE_SUB(NOW(), INTERVAL 1 WEEK)")
    void deleteINTERVAL1Week();
}
    