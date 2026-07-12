package org.jeecg.modules.uav.constant;

/**
 * 反无人机系统UDP协议常量类（严格对齐API文档V1.0.7）
 */
public class UavProtocolConstant {

    // ==================== 基础通信常量（文档2.通信格式+3.地址分配）====================
    public static class Base {
        public static final byte[] START_CODE = new byte[]{(byte) 0xA5, (byte) 0x5A}; // 起始码
        public static final byte CLIENT_ADDR = (byte) 0x10; // 反无人机系统地址
        public static final byte SERVER_ADDR = (byte) 0xA0; // 第三方指控平台地址
        public static final byte RESPONSE_CMD = (byte) 0xA2; // 命令返回标识
        public static final byte SUCCESS_FLAG = (byte) 0x0F; // 执行成功标识
        public static final byte FAIL_FLAG = (byte) 0xF0; // 执行失败标识
        public static final int DEFAULT_LOCAL_PORT = 9800; // 客户端默认本地端口
        public static final String DEFAULT_SERVER_IP = "192.168.175.210"; // 服务端默认IP
        public static final int DEFAULT_SERVER_PORT = 9801; // 服务端默认端口
    }

    // ==================== 命令码常量（文档4.命令集）====================
    public static class CommandCode {
        // 心跳相关
        public static final byte PASSIVE_HEARTBEAT = (byte) 0xA4; // 被动式心跳帧（请求）
        public static final byte ACTIVE_HEARTBEAT = (byte) 0xA6; // 主动式心跳帧（上报）

        // 配置控制相关
        public static final byte SET_GIMBAL_MODE = (byte) 0x10; // 设置云台控制模式
        public static final byte SYSTEM_CALIBRATE_NORTH = (byte) 0x12; // 系统一键定位及校北
        public static final byte GENERAL_CMD = (byte) 0x16; // 通用指令
        public static final byte SET_ATTACK_MODE = (byte) 0x31; // 设置攻击模式
        public static final byte SET_ATTACK_FREQUENCY = (byte) 0x32; // 设置攻击频段
        public static final byte SET_GNSS_SPOOF_MODE = (byte) 0x37; // 设置GNSS诱骗模式
        public static final byte SET_DIRECTION_SPEED = (byte) 0x38; // 设置方位和速度

        // 功能开关相关
        public static final byte SWITCH_DETECT = (byte) 0x20; // 开启/关闭目标探测功能
        public static final byte SWITCH_ATTACK = (byte) 0x30; // 开启/关闭设备攻击功能
        public static final byte SWITCH_GNSS_SPOOF = (byte) 0x36; // 开启/关闭GNSS诱骗功能
        public static final byte SWITCH_UNATTENDED = (byte) 0x40; // 开启/关闭无人值守功能
        public static final byte SWITCH_GNSS_LINKAGE = (byte) 0x41; // 开启/关闭GNSS诱骗系统联动

        // 区域设置相关
        public static final byte SET_NO_FLY_ZONE = (byte) 0x42; // 设置禁飞区位置
        public static final byte SET_FORCED_LAND_ZONE = (byte) 0x43; // 设置定点迫降区

        // 数据上报/获取相关
        public static final byte UPLOAD_TARGET_INFO = (byte) 0x60; // 上传探测目标信息
        public static final byte UPLOAD_SYSTEM_STATUS = (byte) 0x54; // 上传反无系统及设备状态
        public static final byte GET_DEVICE_LIST = (byte) 0x50; // 获取设备列表

        // 设备控制相关
        public static final byte SWITCH_TARGET = (byte) 0x64; // 切换处置目标
        public static final byte DEVICE_CAPTURE = (byte) 0x70; // 设备预览抓图
        public static final byte DEVICE_PTZ_POSITION = (byte) 0x71; // 设备转台定位
        public static final byte DEVICE_PTZ_CONTROL = (byte) 0x72; // 设备云镜控制
        public static final byte SET_WHITE_LIST = (byte) 0xC0; // 设置电磁信号白名单列表
        public static final byte BOX_OPERATION = (byte) 0x80; // 箱仓操作（定制版本）
        public static final byte TRANSPARENT_DATA = (byte) 0x90; // 透传数据包
        public static final byte RADAR_GAZE_TARGET = (byte) 0x65; // 雷达凝视目标
    }

    // ==================== 云台控制参数（文档5.3）====================
    public static class GimbalParam {
        public static final byte CONTROL_BY_PHOTO = (byte) 0x01; // 光电设备控制云台
        public static final byte CONTROL_BY_ATTACK = (byte) 0x02; // 干扰设备控制云台
        public static final byte DISABLE_LINKAGE = (byte) 0x03; // 禁用目标与云台联动机制
    }

    // ==================== 攻击模式参数（文档5.11）====================
    public static class AttackModeParam {
        public static final byte RETURN_HOME = (byte) 0x00; // 返航模式
        public static final byte FORCED_LAND = (byte) 0x01; // 迫降模式
    }

    // ==================== 功能开关参数（文档5.9/5.10等）====================
    public static class SwitchParam {
        public static final byte CLOSE = (byte) 0x00; // 关闭功能
        public static final byte OPEN = (byte) 0x01; // 开启功能
    }

    // ==================== GNSS诱骗相关参数（文档5.13/5.14）====================
    public static class GnssSpoofParam {
        // GNSS诱骗功能类型
        public static final byte SPOOF_GUIDE = (byte) 0x0001; // 诱导
        public static final byte SPOOF_FORCED_LAND = (byte) 0x0002; // 迫降（禁飞）
        public static final byte SPOOF_NAV_BLOCK = (byte) 0x0003; // 导航压制

        // GNSS诱骗模式
        public static final byte MODE_DIRECTIONAL_EXPEL = (byte) 0x0000; // 定向驱逐
        public static final byte MODE_FIXED_FORCED_LAND = (byte) 0x0001; // 定点迫降
        public static final byte MODE_NO_FLY = (byte) 0x0002; // 禁飞
        public static final byte MODE_NAV_BLOCK = (byte) 0x0003; // 导航压制

        // GNSS诱导方式
        public static final byte GUIDE_EXPEL = (byte) 0x0000; // 驱离
        public static final byte GUIDE_PULL = (byte) 0x0001; // 拉近
    }

    // ==================== 攻击频段参数（文档5.12）====================
    public static class AttackFrequencyParam {
        public static final byte FREQ_5_8G = (byte) 0x01; // 5.8G频段（Bit0）
        public static final byte FREQ_2_4G = (byte) 0x02; // 2.4G频段（Bit1）
        public static final byte FREQ_900M = (byte) 0x04; // 900M频段（Bit2）
        public static final byte FREQ_1_4G = (byte) 0x08; // 1.4G频段（Bit3）
        public static final byte FREQ_5_2G = (byte) 0x10; // 5.2G频段（Bit4）
    }

    // ==================== 通用指令参数（文档5.5）====================
    public static class GeneralCmdParam {
        public static final byte SOFTWARE_REBOOT = (byte) 0x01; // 软件重启
    }

    // ==================== 设备云镜控制参数（文档5.24）====================
    public static class PtzControlParam {
        // 操作指令
        public static final byte UP = (byte) 0x01; // 上移
        public static final byte DOWN = (byte) 0x02; // 下移
        public static final byte LEFT = (byte) 0x03; // 左移
        public static final byte RIGHT = (byte) 0x04; // 右移
        public static final byte ZOOM_IN = (byte) 0x09; // 放大
        public static final byte ZOOM_OUT = (byte) 0x0A; // 缩小

        // 转动状态
        public static final byte ROTATING = (byte) 0x00; // 转动中
        public static final byte STOP = (byte) 0x01; // 停止转动
    }

    // ==================== 设备类型参数（文档5.8/5.23等）====================
    public static class DeviceTypeParam {
        public static final byte UNKNOWN = (byte) 0xFF; // 未知设备
        public static final byte ACTIVE_RADAR = (byte) 0x00; // 主动雷达探测设备
        public static final byte RADIO_DETECT = (byte) 0x01; // 无线电侦测设备
        public static final byte PHOTO_TRACK = (byte) 0x02; // 光电跟踪设备
        public static final byte INFRARED = (byte) 0x03; // 红外设备
        public static final byte ATTACK_DEVICE = (byte) 0x04; // 干扰设备
        public static final byte SPOOF_DEVICE = (byte) 0x06; // 诱骗设备
        public static final byte SMART_CONTROL_BOX = (byte) 0x08; // 智能控制箱
    }
}
