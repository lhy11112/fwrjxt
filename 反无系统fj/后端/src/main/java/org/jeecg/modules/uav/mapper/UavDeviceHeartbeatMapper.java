package org.jeecg.modules.uav.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.uav.entity.UavDeviceHeartbeat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UavDeviceHeartbeatMapper extends BaseMapper<UavDeviceHeartbeat> {
    UavDeviceHeartbeat selectLatestHeartbeat(@Param("stationId") Integer stationId);
}
    