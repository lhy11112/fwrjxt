package org.jeecg.modules.uav.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.uav.entity.UavDetectSpectrum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UavDetectSpectrumMapper extends BaseMapper<UavDetectSpectrum> {
    List<UavDetectSpectrum> selectByFreqRange(
            @Param("minFreq") Long minFreq,
            @Param("maxFreq") Long maxFreq,
            @Param("startTime") LocalDateTime startTime
    );
    @Select(" select * from uav_detect_spectrum where DATE_FORMAT(data_time, '%Y-%m-%d')= #{rq} ORDER BY data_time desc ")
    List<UavDetectSpectrum> listNow(String rq);
}
