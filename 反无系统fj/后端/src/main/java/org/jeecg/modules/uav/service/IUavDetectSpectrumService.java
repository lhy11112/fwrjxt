package org.jeecg.modules.uav.service;

import org.jeecg.modules.uav.entity.UavDetectSpectrum;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
/**
 * @Description: 侦测频谱数据表-频谱数据
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
public interface IUavDetectSpectrumService extends IService<UavDetectSpectrum> {

    List<UavDetectSpectrum> listNow(String rq);
}
