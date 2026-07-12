package org.jeecg.modules.uav.enums;
/**
 * 反无人机系统下发命令枚举（协议V1.0.7）
 * 存储命令的「十六进制字符串」与「中文描述」，方便解析时关联显示
 */
public enum UavCmdEnum {
    // 按文档命令集（表1-4）梳理核心命令，可按需扩展
    PASSIVE_HEARTBEAT("0xA4", "被动式心跳帧（被动请求）"),
    SET_GIMBAL_MODE("0x10", "设置云台控制模式"),
    SYSTEM_ONE_KEY_LOCATE("0x12", "系统一键定位及校北"),
    GENERAL_CMD("0x16", "通用指令（如软件重启）"),
    SWITCH_TARGET("0x64", "切换处置目标"),
    SCHEDULE_TURRET("0x68", "上级平台调度转台式设备引导信息"),
    SET_DETECTION("0x20", "开启/关闭目标探测功能"),
    SET_ATTACK("0x30", "开启/关闭设备攻击功能"),
    SET_ATTACK_MODE("0x31", "设置攻击模式"),
    SET_ATTACK_BAND("0x32", "设置攻击频段"),
    SET_GNSS_SPOOF("0x36", "开启/关闭GNSS诱骗功能"), // 重点命令：0x36
    SET_GNSS_SPOOF_MODE("0x37", "设置GNSS诱骗模式"),
    SET_AZIMUTH_SPEED("0x38", "设置方位和速度"),
    SET_UNATTENDED("0x40", "开启/关闭无人值守功能"),
    SET_GNSS_LINKAGE("0x41", "开启/关闭GNSS诱骗系统联动"),
    SET_NO_FLY_ZONE("0x42", "设置禁飞区位置"),
    SET_FORCED_LANDING("0x43", "设置定点迫降区"),
    GET_DEVICE_LIST("0x50", "获取设备列表"),
    DEVICE_PREVIEW_CAPTURE("0x70", "设备预览抓图"),
    DEVICE_TURRET_POSITIONING("0x71", "设备转台定位"),
    DEVICE_CLOUD_MIRROR("0x72", "设备云镜控制"),
    SET_WHITELIST("0xC0", "设置电磁信号白名单列表"),
    CABINET_OP("0x80", "箱仓操作（定制版本）"),
    TRANSMIT_DATA("0x90", "透传数据包"),
    RADAR_STARE("0x65", "雷达凝视目标");

    // 命令的十六进制字符串（如"0x36"）
    private final String cmdHex;
    // 命令的中文描述
    private final String cmdDesc;

    UavCmdEnum(String cmdHex, String cmdDesc) {
        this.cmdHex = cmdHex;
        this.cmdDesc = cmdDesc;
    }

    /**
     * 通过命令的十六进制字符串获取枚举（如"0x36"→SET_GNSS_SPOOF）
     */
    public static UavCmdEnum getByCmdHex(String cmdHex) {
        for (UavCmdEnum cmd : values()) {
            if (cmd.cmdHex.equalsIgnoreCase(cmdHex)) {
                return cmd;
            }
        }
        return null; // 未匹配到的命令（如预留命令）
    }

    // Getter方法
    public String getCmdHex() {
        return cmdHex;
    }

    public String getCmdDesc() {
        return cmdDesc;
    }
}
