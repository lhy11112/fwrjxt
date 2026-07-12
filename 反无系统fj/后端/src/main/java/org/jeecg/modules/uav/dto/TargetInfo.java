package org.jeecg.modules.uav.dto;

import lombok.Data;

@Data
public class TargetInfo {
    private Integer DataType;        // 数据类型 0-无线电测向,1-雷达,3-ID破解,4-多类型融合
    private Integer FusedFlag;       // 融合标记 Bit位标记，仅融合数据有效
    private Integer TargetType;      // 目标类型 0-图传,1-遥控
    private String UniqueID;         // 目标唯一编号
    private Integer TargetNo;        // 目标显示编号
    private String DroneID;          // 无人机ID号(仅ID破解目标有效)
    private Double FreqOrNo;         // 频点(2446.5)或航迹编号(A1)
    private String TargetName;       // 无人机型号
    private String WiFiMAC;          // WiFi类无人机MAC地址
    private Double SignalBandWidth;  // 信号带宽(全频频谱设备有效)
    private Integer ModulateMode;    // 调制方式-预留
    private Double Longitude;        // 无人机经度
    private Double Latitude;         // 无人机纬度
    private Float Altitude;          // 无人机海拔
    private Float Direction;         // 方位 °
    private Float Distance;          // 参考距离 m
    private Float Pitch;             // 俯仰 °(仅雷达目标有效)
    private Float Height;            // 高度 m
    private Integer Timestamp;       // UTC时间戳(1970-01-01)
    private Boolean IsFriendly;      // 是否合作无人机 false=黑飞 true=合作
    private String DeviceId;         // 设备ID
    private Double DeviceLng;        // 设备位置经度
    private Double DeviceLat;        // 设备位置纬度
    private OriginalAttribute OriginalAttribute; // 遥控位置信息
    // 内部类：遥控位置原始属性
    @Data
    public static class OriginalAttribute {
        private Double RemoteLng;     // 遥控位置经度
        private Double RemoteLat;     // 遥控位置纬度
        // getter/setter 省略，自行生成即可
    }
}
