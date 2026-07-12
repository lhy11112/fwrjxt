package org.jeecg.modules.uav.enums;

/**
 * 命令类型枚举，包含命令号和对应的描述信息
 */
public enum CommandTypeEnum {
    // 基础命令
    CMD_HEARTBEAT(0x01, "心跳包"),
    CMD_REBOOT(0x02, "重启设备"),
    CMD_RE_SERVER(0x03, "重启服务"),
    CMD_CHECK(0x04, "设备自检"),
    CMD_POWEROFF(0x06, "关闭设备"),
    CMD_START_WORK(0x10, "开始工作"),
    CMD_STOP_WORK(0x11, "停止工作"),

    // 诱骗设备命令
    CMD_TRAP_AMP(0x41, "诱骗功率"),
    CMD_TRAP_POSITION(0x42, "诱骗-禁飞开关"),
    CMD_TRAP_FUN(0x43, "诱骗模式"),
    CMD_TRAP_EXPEL(0x44, "驱离功能"),

    // 干扰设备命令
    CMD_DISTURB_SELF(0x80, "自定义干扰"),
    CMD_DISTURB_QUICK(0x81, "快速干扰"),
    CMD_DISTURB_ANTENNA(0x82, "天线选择"),

    // 频谱相关命令
    Cmd_TypeSpectrumParam(0x30, "下发频谱监测参数"),
    // 手动设置侧向频率
    Cmd_TypeDfFreq(0x52, "手动设置侧向频率"),
    // 数据上报命令
    CMD_DF_DATA(0x50, "测向结果数据");

    // 命令号
    private final int code;
    // 命令描述
    private final String desc;

    /**
     * 构造方法
     * @param code 命令号
     * @param desc 命令描述
     */
    CommandTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    // 获取命令号
    public int getCode() {
        return code;
    }

    // 获取命令描述
    public String getDesc() {
        return desc;
    }

    /**
     * 根据命令号获取对应的描述信息
     * @param code 命令号
     * @return 命令描述，若未找到则返回"未知命令"
     */
    public static String getDescByCode(int code) {
        for (CommandTypeEnum command : CommandTypeEnum.values()) {
            if (command.getCode() == code) {
                return command.getDesc();
            }
        }
        return "未知命令";
    }
}
