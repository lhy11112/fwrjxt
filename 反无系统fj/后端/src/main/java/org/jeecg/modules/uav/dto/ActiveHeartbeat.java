package org.jeecg.modules.uav.dto;

import lombok.Data;
@Data
public class ActiveHeartbeat {
    // 基础字段
    private String startCode;       // 起始码（固定 A55A）
    private int sourceAddr;         // 源地址（0x10）
    private int destAddr;           // 目的地址（0xA0）
    private int command;            // 命令（0xA6）
    private int paramLength;        // 参数长度（61）

    // Param1~Param17
    private String terminalCode;    // 终端代码（Param1）
    private SystemStatus systemStatus; // 系统状态（Param2 解析后）
    private int jammingMode;        // 干扰模式（Param3：0=返航，1=迫降） 干扰模式0x00-返航,0x01-迫降
    private BandStatus bandStatus;  // 攻击频段状态（Param4 解析后）
    /**
     * 0x01-光电设备与干扰设备共用云台,且云台由光电设备控制，0x02-光电设备与干扰
     * 设备共用云台,且云台由干扰设备控制，0x03-禁用目标与云台联动机制
     */
    private int ptzControlMode;     // 云台控制模式（Param5：1/2/3）
    private int attackCountdown;    // 攻击倒计时（Param6）
    private int deceptionStatus;    // 系统诱骗状态（Param7：0x0000-空闲状态；0x0001-驱离状态；0x0002-迫降状态；0x0003-导航压制 ）
    private int gnssLinkStatus;     // GNSS 联动状态（Param8：0=关闭，1=开启）
    private int gnssDeceptionMode;  // GNSS 诱骗模式（Param9：0x0000-定向驱逐；0x0001-定点迫降；0x0002-禁飞；0x0003-导航压制 ）
    private int gnssInduceMode;     // GNSS 诱导方式（Param10：0=驱离，1=拉近） 0x0000-驱离；0x0001-拉近

    // 地理信息（float 型）
    private float noFlyLat;         // 禁飞区纬度（Param11）
    private float noFlyLng;         // 禁飞区经度（Param12）
    private float noFlyAlt;         // 禁飞区海拔（Param13）
    private float forcedLandLat;    // 定点迫降区纬度（Param14）
    private float forcedLandLng;    // 定点迫降区经度（Param15）
    private float forcedLandAlt;    // 定点迫降区海拔（Param16）
    private int forcedLandRadius;   // 定点迫降区半径（Param17，单位：m）

    private int checksum;           // 校验和

    /**
     * 系统状态（Param2 解析结果）
     * Bit0-系统授权状态 0：正常；1：未授权或授权异常；
     * Bit1-系统工作状态 0：探测处于停止状态；1：已开启探测功能；
     * Bit2-系统反制状态 0：未开启干扰；1：已开启干扰
     * Bit3-探测设备状态 0：所有探测设备连接异常；1：有一个或多个探测设备正常连
     * 接，能正常开启探测功能
     * Bit4-反制设备状态 0：所有干扰设备连接异常；1：有一个或多个干扰设备正常连
     * 接，能正常开启反制功能
     * Bit5-无人值守模式状态 0：关闭；1：启用
     * Bit6-诱骗设备状态 0：所有诱骗设备连接异常；1：有一个或多个诱骗设备正常连
     * 接，能正常开启GNSS诱骗功能
     */
    @Data
    public static class SystemStatus {
        private boolean authNormal;      // Bit0：授权状态（false=未授权，true=正常）
        private boolean detectEnabled;   // Bit1：探测功能（false=停止，true=开启）
        private boolean counterEnabled;  // Bit2：反制功能（false=未开启，true=已开启）
        private boolean detectorOnline;  // Bit3：探测设备（false=异常，true=正常）
        private boolean counterOnline;   // Bit4：反制设备（false=异常，true=正常）
        private boolean unattendedMode;  // Bit5：无人值守（false=关闭，true=开启）
        private boolean deceiverOnline;  // Bit6：诱骗设备（false=异常，true=正常）
    }

    /**
     * 攻击频段状态（Param4 解析结果）
     * 低字节在前，高字节在后Bit0-5.8G；Bit1-2.4G；Bit2-900M；Bit3-1.4G；Bit4-5.2G
     * 每个Bit的状态定义：0：未启用状态；1：已开启状态
     */
    @Data
    public static class BandStatus {
        private boolean band58G;   // Bit0：5.8G（false=关闭，true=开启）
        private boolean band24G;   // Bit1：2.4G（false=关闭，true=开启）
        private boolean band900M;  // Bit2：900M（false=关闭，true=开启）
        private boolean band14G;   // Bit3：1.4G（false=关闭，true=开启）
        private boolean band52G;   // Bit4：5.2G（false=关闭，true=开启）
    }
}