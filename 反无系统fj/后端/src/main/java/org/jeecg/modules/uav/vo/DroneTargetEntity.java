package org.jeecg.modules.uav.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jeecg.modules.uav.dto.TargetInfo;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略JSON中不存在的字段
public class DroneTargetEntity {
    /** 数据类型 */
    private Integer DataType;

    /** 融合标识 */
    private Integer FusedFlag;

    /** 目标类型 */
    private Integer TargetType;

    /** 唯一标识ID */
    private String UniqueID;

    /** 目标编号 */
    private Integer TargetNo;

    /** 目标等级 */
    private Integer TargetLevel;

    /** 无人机ID */
    private String DroneID;

    /** 频率/编号（无人机频点，浮点型） */
    private Long FreqOrNo;

    /** 无人机型号名称 */
    private String TargetName;

    /** WIFI物理地址 */
    private String WiFiMAC;

    /** 信号带宽 */
    private Float SignalBandWidth;

    /** 调制模式 */
    private Integer ModulateMode;

    /** 其他属性（JSON中为null，定义为Object兼容任意类型） */
    private Object OtherAttribute;

    /** 有效时长 */
    private Integer ValidDuration;

    /** 是否为友方目标 */
    private Boolean IsFriendly;

    /** 设备ID（整型） */
    private Long DeviceId;

    /** 探测设备经度 */
    private Double DeviceLng;

    /** 探测设备纬度 */
    private Double DeviceLat;

    /** 目标经度 */
    private Double Longitude;

    /** 目标纬度 */
    private Double Latitude;

    /** 目标海拔高度 */
    private Double Altitude;

    /** 航向 */
    private Double Course;

    /** 飞行速度 */
    private Double Speed;

    /** 方位角 */
    private Float Direction;

    /** 距离探测设备的距离 */
    private Float Distance;

    /** 俯仰角 */
    private Double Pitch;

    /** 相对高度 */
    private Double Height;

    /** 时间戳（秒级，10位） */
    private Long Timestamp;
    private TargetInfo.OriginalAttribute OriginalAttribute; // 遥控位置信息

    // 内部类：遥控位置原始属性
    @Data
    public static class OriginalAttribute {
        private Double RemoteLng;     // 遥控位置经度
        private Double RemoteLat;     // 遥控位置纬度
        // getter/setter 省略，自行生成即可
    }
}
