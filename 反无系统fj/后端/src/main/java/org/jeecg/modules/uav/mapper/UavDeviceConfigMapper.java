package org.jeecg.modules.uav.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.uav.entity.UavDeviceConfig;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface UavDeviceConfigMapper extends BaseMapper<UavDeviceConfig> {
    @Select(" SELECT * FROM uav_device_config\n" +
            "        WHERE is_valid = 1 ")
    List<UavDeviceConfig> selectAllValidDevices();

    List<Map<String, Object>> getypsbCount();

    List<Map<String, Object>> getgrsbCount();

    List<Map<String, Object>> getzcsbCount();
    Integer getSbCountByLx(@Param("type") String type, @Param("title") String title );

    @Select(" select * from uav_device_config where station_id=#{stationId}  limit 1  ")
    UavDeviceConfig selectDevicesByStaticId(Integer stationId);
}
