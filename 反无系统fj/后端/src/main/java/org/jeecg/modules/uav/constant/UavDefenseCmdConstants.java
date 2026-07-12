package org.jeecg.modules.uav.constant;

/**
 * 反无人机系统API指令码常量类
 * 严格对齐API文档中的指令码与功能对应关系
 * 分类：心跳指令、设备控制指令、目标处理指令、系统配置指令、其他指令
 */
public class UavDefenseCmdConstants {
    // 执行结果常量
    public static final byte SUCCESS = (byte) 0x0F;
    public static final byte FAIL = (byte) 0xF0;
    // 起始码（验证帧合法性）
    public static final byte[] START_CODE = new byte[]{(byte) 0xA5, (byte) 0x5A};
    /**
     * 返回帧固定命令
     */
    public static final byte RESPONSE_CMD = (byte) 0xA2;
    /** 被动式心跳帧 */
    public static final byte CMD_HEARTBEAT_PASSIVE = (byte) 0xA4;
    /** 主动式心跳帧 */
    public static final byte CMD_HEARTBEAT_ACTIVE = (byte) 0xA6;

    // ======================== 目标处理指令 ========================
    /**
     * 上传探测目标信息
     */
    public static final int CMD_UPLOAD_TARGET_INFO =  0x60;
    /**
     * 切换处置目标 */
    public static final byte CMD_SWITCH_DISPOSE_TARGET = (byte) 0x64;
    /**
     * 上级平台调度转台设备引导信息 */
    public static final byte CMD_PLATFORM_GUIDE_INFO = (byte) 0x68;

    // ======================== 设备控制指令 ========================
    /** 开启/关闭目标探测功能 */
    public static final byte CMD_TOGGLE_DETECT_FUNC = (byte) 0x20;
    /** 开启/关闭设备攻击功能 */
    public static final byte CMD_TOGGLE_ATTACK_FUNC = (byte) 0x30;
    /** 设置攻击模式 */
    public static final byte CMD_SET_ATTACK_MODE = (byte) 0x31;
    /** 设置攻击频段 */
    public static final byte CMD_SET_ATTACK_FREQ = (byte) 0x32;
    /** 开启/关闭GNSS诱骗功能 */
    public static final byte CMD_TOGGLE_GNSS_DECOY = (byte) 0x36;
    /** 设置GNSS诱骗模式 */
    public static final byte CMD_SET_GNSS_DECOY_MODE = (byte) 0x37;
    /** 设置方位和速度 */
    public static final byte CMD_SET_DIRECTION_SPEED = (byte) 0x38;
    /** 开启/关闭无人值守功能 */
    public static final byte CMD_TOGGLE_UNMANNED_FUNC = (byte) 0x40;
    /** 开启/关闭GNSS诱骗系统联动 */
    public static final byte CMD_TOGGLE_GNSS_LINKAGE = (byte) 0x41;
    /** 设置禁飞区位置 */
    public static final byte CMD_SET_NO_FLY_ZONE = (byte) 0x42;
    /** 设置定点迫降区 */
    public static final byte CMD_SET_FORCED_LAND_ZONE = (byte) 0x43;
    /** 设备预置位图 */
    public static final byte CMD_DEVICE_PRESET_BITMAP = (byte) 0x70;
    /** 设备转台定位 */
    public static final byte CMD_DEVICE_TURNTABLE_POS = (byte) 0x71;
    /** 设备云镜控制 */
    public static final byte CMD_DEVICE_CLOUD_MIRROR = (byte) 0x72;
    /** 设置电磁信号白名单列表 */
    public static final byte CMD_SET_EM_SIGNAL_WHITELIST = (byte) 0xC0;
    /** 箱仓操作(定制版本) */
    public static final byte CMD_BIN_OPERATION_CUSTOM = (byte) 0x80;

    // ======================== 系统配置指令 ========================
    /** 设置云台控制模式 */
    public static final byte CMD_SET_PTZ_CONTROL_MODE = (byte) 0x10;
    /** 系统一键定位及校北 */
    public static final byte CMD_SYSTEM_POS_CALIB = (byte) 0x12;
    /** 通用指令 */
    public static final byte CMD_GENERAL = (byte) 0x16;

    // ======================== 数据交互指令 ========================
    /** 获取设备列表 */
    public static final byte CMD_GET_DEVICE_LIST = (byte) 0x50;
    /** 获取设备列表 */
    public static final String CMD_GET_DEVICE_LIST_STR = "0x50";
    /** 上传反无系统及设备状态 */
    public static final byte CMD_UPLOAD_SYSTEM_STATUS = (byte) 0x54;
    /** 透传数据包（双向传输） */
    public static final byte CMD_TRANSPARENT_DATA = (byte) 0x90;

    // 私有化构造方法，禁止实例化
    public UavDefenseCmdConstants() {
        throw new UnsupportedOperationException("该类为常量类，禁止实例化");
    }
}
