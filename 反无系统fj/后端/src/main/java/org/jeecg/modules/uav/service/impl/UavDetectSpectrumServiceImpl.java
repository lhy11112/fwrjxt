package org.jeecg.modules.uav.service.impl;

import org.jeecg.modules.uav.entity.UavDetectSpectrum;
import org.jeecg.modules.uav.mapper.UavDetectSpectrumMapper;
import org.jeecg.modules.uav.service.IUavDetectSpectrumService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
/**
 * @Description: 侦测频谱数据表-频谱数据
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Service
public class UavDetectSpectrumServiceImpl extends ServiceImpl<UavDetectSpectrumMapper, UavDetectSpectrum> implements IUavDetectSpectrumService {

    @Override
    public List<UavDetectSpectrum> listNow(String rq) {
        return baseMapper.listNow(rq);
    }
}
