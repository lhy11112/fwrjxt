package org.jeecg.modules.uav.constant;

/**
 * 命令类型常量定义
 */
public class CommandType {
    // 基础命令
    // 心跳包
    public static final int CMD_HEARTBEAT = 0x01;
    // 重启设备
    public static final int CMD_REBOOT = 0x02;
    // 重启服务
    public static final int CMD_RE_SERVER = 0x03;
    // 设备自检
    public static final int CMD_CHECK = 0x04;
    // 关闭设备
    public static final int CMD_POWEROFF = 0x06;
    // 开始工作
    public static final int CMD_START_WORK = 0x10;
    // 停止工作
    public static final int CMD_STOP_WORK = 0x11;

    // 诱骗设备命令
    // 诱骗功率
    public static final int CMD_TRAP_AMP = 0x41;
    // 诱骗-禁飞开关
    public static final int CMD_TRAP_POSITION = 0x42;
    // 诱骗模式
    public static final int CMD_TRAP_FUN = 0x43;
    // 驱离功能
    public static final int CMD_TRAP_EXPEL = 0x44;
    // 指定诱骗
    public static final int CMD_TRAP_ASSIGN = 0x45;
    // 干扰设备命令
    // 自定义干扰
    public static final int CMD_DISTURB_SELF = 0x80;
    // 快速干扰
    public static final int CMD_DISTURB_QUICK = 0x81;
    // 天线选择
    public static final int CMD_DISTURB_ANTENNA = 0x82;

    //频谱
    //下发频谱监测参数
    public static final int Cmd_TypeSpectrumParam= 0x30;
    //手动设置侧向频率
    public static final int Cmd_TypeDfFreq= 0x52;
    // 数据上报命令
    // 测向结果数据
    public static final int CMD_DF_DATA = 0x50;
}
