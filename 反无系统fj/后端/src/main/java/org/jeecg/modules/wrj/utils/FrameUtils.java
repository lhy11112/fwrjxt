package org.jeecg.modules.wrj.utils;
import org.jeecg.modules.uav.config.Config;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 帧数据工具类：校验、解析、构建响应
 */
public class FrameUtils {

    /**
     * 校验帧合法性（起始码 + 校验和）
     * @param frame 接收的帧数据
     * @return true-合法，false-非法
     */
    public static boolean validateFrame(byte[] frame) {
        // 1. 校验最小长度（起始码2 + 地址2 + 命令1 + 参数长度2 + 校验和1 = 8字节）
        if (frame == null || frame.length < 8) {
            return false;
        }
        // 2. 校验起始码（0xA5 0x5A）
        if (frame[0] != Config.START_CODE[0] || frame[1] != Config.START_CODE[1]) {
            return false;
        }
        // 3. 校验和（累加和：源地址+目的地址+命令+参数长度+参数）
        byte calculatedChecksum = calculateChecksum(frame);
        byte frameChecksum = frame[frame.length - 1];
        return calculatedChecksum == frameChecksum;
    }

    /**
     * 计算校验和
     * @param frame 帧数据
     * @return 校验和（1字节）
     */
    private static byte calculateChecksum(byte[] frame) {
        int sum = 0;
        // 累加范围：源地址（字节2）~ 最后一个参数（字节length-2）
        for (int i = 2; i < frame.length - 1; i++) {
            sum += (frame[i] & 0xFF); // 无符号字节累加
        }
        return (byte) (sum & 0xFF); // 取低8位
    }

    /**
     * 解析帧中的命令字段
     * @param frame 合法帧数据
     * @return 命令字节（0x00~0xFF）
     */
    public static byte parseCommand(byte[] frame) {
        return frame[4]; // 命令字段在字节4（文档定义）
    }

    /**
     * 构建响应帧
     * @param originalCmd 客户端原始命令
     * @param success 执行结果（true-成功，false-失败）
     * @return 响应帧字节数组
     */
    public static byte[] buildResponse(byte originalCmd, boolean success) {
        ByteBuffer buffer = ByteBuffer.allocate(10) // 响应帧固定10字节（文档表格1-1）
                .order(ByteOrder.LITTLE_ENDIAN); // 小端序（文档要求）

        // 1. 起始码（0xA5 0x5A）
        buffer.put(Config.START_CODE[0]);
        buffer.put(Config.START_CODE[1]);
        // 2. 源地址（服务端：0xA0）
        buffer.put(Config.SERVER_ADDR);
        // 3. 目的地址（客户端：0x10）
        buffer.put(Config.CLIENT_ADDR);
        // 4. 响应命令（0xA2）
        buffer.put(Config.RESPONSE_CMD);
        // 5. 参数长度（2字节：0x0002）
        buffer.putShort((short) 2);
        // 6. 参数1：原始命令
        buffer.put(originalCmd);
        // 7. 参数2：执行结果（0x0F成功，0xF0失败）
        buffer.put(success ? Config.SUCCESS_FLAG : Config.FAIL_FLAG);
        // 8. 校验和
        byte[] temp = buffer.array();
        byte checksum = calculateChecksum(temp);
        temp[9] = checksum; // 校验和在字节9

        return temp;
    }
}
