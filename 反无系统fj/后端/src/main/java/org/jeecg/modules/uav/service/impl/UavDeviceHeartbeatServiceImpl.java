package org.jeecg.modules.uav.service.impl;

import org.jeecg.modules.uav.entity.UavDeviceHeartbeat;
import org.jeecg.modules.uav.mapper.UavDeviceHeartbeatMapper;
import org.jeecg.modules.uav.service.IUavDeviceHeartbeatService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 设备心跳管理
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Service
public class UavDeviceHeartbeatServiceImpl extends ServiceImpl<UavDeviceHeartbeatMapper, UavDeviceHeartbeat> implements IUavDeviceHeartbeatService {

    @Override
    public UavDeviceHeartbeat selectLatestHeartbeat(Integer stationId) {
        return baseMapper.selectLatestHeartbeat(stationId);
    }
}
