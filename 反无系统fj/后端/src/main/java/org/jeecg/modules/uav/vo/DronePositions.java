package org.jeecg.modules.uav.vo;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author 207
 */
@Data
public class DronePositions {
   private double longitude;
    private double latitude;
    private double altitude;
    private double time; // 以秒为单位的时间，从0开始
    private Date zonedDateTime; // 详细的时间信息

}
