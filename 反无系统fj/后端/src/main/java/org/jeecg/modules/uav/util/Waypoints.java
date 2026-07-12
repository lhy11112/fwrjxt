package org.jeecg.modules.uav.util;

import lombok.Data;

@Data
public class Waypoints {
    private double sd;   // 速度(米/秒)
    private String sj;     // (秒)
    private String lc;     // 停留时间(米)
}
