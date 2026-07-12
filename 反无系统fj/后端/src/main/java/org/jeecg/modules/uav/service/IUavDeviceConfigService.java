package org.jeecg.modules.uav.service;

import org.jeecg.modules.uav.entity.UavDeviceConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @Description: 设备管理
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
public interface IUavDeviceConfigService extends IService<UavDeviceConfig> {
    List<Map<String, Object>> getypsbCount();

    List<Map<String, Object>> getgrsbCount();

    List<Map<String, Object>> getzcsbCount();
    Map<String, Object> getSbCountByLx(String type);
}
