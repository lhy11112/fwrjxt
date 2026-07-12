package org.jeecg.modules.uav.vo;

import lombok.Data;

@Data
public class Waypoint {
    private double longitude;  // 经度
    private double latitude;   // 纬度
    private double altitude;   // 高度(米)
    private long stayTime;     // 停留时间(秒)
}
