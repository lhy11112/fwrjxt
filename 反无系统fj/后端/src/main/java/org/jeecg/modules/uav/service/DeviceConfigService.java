package org.jeecg.modules.uav.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.jeecg.modules.uav.entity.UavDeviceConfig;
import org.jeecg.modules.uav.mapper.UavDeviceConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DeviceConfigService {
    @Autowired
    private UavDeviceConfigMapper deviceConfigMapper;

    /**
     * 获取所有有效设备配置
     */
    public List<UavDeviceConfig> getAllValidDevices() {
        return deviceConfigMapper.selectAllValidDevices();
    }

    /**
     * 更新设备连接状态
     */
    public void updateDeviceStatus(Integer stationId, String status) {
        UavDeviceConfig config = new UavDeviceConfig();
        config.setStatus(status);
        config.setUpdateTime(new Date());
        
        UpdateWrapper<UavDeviceConfig> wrapper = new UpdateWrapper<>();
        wrapper.eq("station_id", stationId);
        deviceConfigMapper.update(config, wrapper);
    }

    /**
     * 根据站ID获取设备配置
     */
    public UavDeviceConfig getDeviceByStationId(Integer stationId) {
        Map<String, Object> map=new HashMap<>();
        map.put("station_id", stationId);
        List<UavDeviceConfig> uavDeviceConfigList=deviceConfigMapper.selectByMap(map);
        if (uavDeviceConfigList.size()>0) {
            return uavDeviceConfigList.get(0);
        }
        return null;
    }
}
