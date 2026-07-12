package org.jeecg.modules.uav.config;
/**
 * 通信参数配置类
 * @Author: 李海洋
 * @Date:   2026-02-28
 */
public class Config {
    // 服务端IP（默认文档指定，支持配置）
    public static final String SERVER_IP = "127.0.0.1";
    // 服务端端口（文档指定：9801）
    public static final int SERVER_PORT = 9800;
    // 起始码（固定：0xA5 0x5A）
    public static final byte[] START_CODE = new byte[]{(byte) 0xA5, 0x5A};
    // 服务端地址（第三方指控平台：0xA0）
    public static final byte SERVER_ADDR = (byte) 0xA0;
    // 客户端地址（反无人机系统：0x10）
    public static final byte CLIENT_ADDR = 0x10;
    // 响应命令（固定：0xA2）
    public static final byte RESPONSE_CMD = (byte) 0xA2;
    // 执行成功标识
    public static final byte SUCCESS_FLAG = 0x0F;
    // 执行失败标识
    public static final byte FAIL_FLAG = (byte) 0xF0;
}
