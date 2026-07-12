package org.jeecg.modules.uav.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.uav.entity.UavDfData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UavDfDataMapper extends BaseMapper<UavDfData> {
    List<UavDfData> selectByTargetType(
            @Param("targetType") Integer targetType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
    