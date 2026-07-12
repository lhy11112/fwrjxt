package org.jeecg.modules.uav.service;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.uav.entity.UavDeviceHeartbeat;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 设备心跳管理
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
public interface IUavDeviceHeartbeatService extends IService<UavDeviceHeartbeat> {
    /**
     * 查询设备最新心跳数据
     * @param stationId
     * @return
     */
    UavDeviceHeartbeat selectLatestHeartbeat(Integer stationId);
}
