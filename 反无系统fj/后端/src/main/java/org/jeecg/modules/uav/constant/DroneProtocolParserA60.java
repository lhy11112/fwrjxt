package org.jeecg.modules.uav.constant;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.modules.uav.vo.DroneTargetEntity;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
/**
 * 上传或解析探测目标信息
 */
public class DroneProtocolParserA60 {

    // Jackson 反序列化工具（全局单例）
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    // 协议固定常量
    private static final byte FRAME_HEAD1 = (byte) 0xA5;
    private static final byte FRAME_HEAD2 = (byte) 0x5A;
    private static final byte RESERVED_01 = 0x10;
    private static final byte RESERVED_02 = (byte) 0xA0;
    private static final byte CMD_UPLOAD_TARGET = (byte) 0x60;

    /**
     * 16进制字符串转字节数组
     */
    private static byte[] hexToBytes(String hexStr) {
        String cleanHex = hexStr.replaceAll("\\s+", "");
        if (cleanHex.length() % 2 != 0) {
            throw new IllegalArgumentException("16进制字符串长度必须为偶数");
        }
        byte[] bytes = new byte[cleanHex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int idx = i * 2;
            int val = Integer.parseInt(cleanHex.substring(idx, idx + 2), 16);
            bytes[i] = (byte) val;
        }
        return bytes;
    }



    /**
     * 解析协议帧，返回实体对象
     */
    public static DroneTargetEntity parseTargetFrame(String hexData) throws Exception {
        byte[] frameBytes = hexToBytes(hexData);
        System.out.println("解析的字节数组长度：" + frameBytes.length);

        // 1. 校验帧头和保留字段
        if (frameBytes[0] != FRAME_HEAD1 || frameBytes[1] != FRAME_HEAD2) {
            throw new RuntimeException("帧头错误：非合法协议数据");
        }
        if (frameBytes[2] != RESERVED_01 || frameBytes[3] != RESERVED_02) {
            throw new RuntimeException("保留字段校验失败");
        }

        // 2. 校验命令类型
        byte cmdType = frameBytes[4];
        if (cmdType != CMD_UPLOAD_TARGET) {
            throw new RuntimeException("不支持的命令类型：0x" + Integer.toHexString(cmdType & 0xFF));
        }
        System.out.println("命令类型：0x60（上传探测目标信息）");

        // 3. 解析参数长度（小端字节序，核心修复）
        short paramLen = ByteBuffer.wrap(frameBytes, 5, 2)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getShort();
        System.out.println("参数段长度：" + paramLen + "字节");

        // 4. 提取JSON字节数组并转字符串
        // 兼容：若参数长度超出实际数据，则取到JSON结尾符 7D 为止
        int jsonStart = 7;
        int jsonEnd = Math.min(jsonStart + paramLen, frameBytes.length - 1);
        // 兜底：查找JSON结尾符 } (0x7D)
        for (int i = jsonStart; i < frameBytes.length - 1; i++) {
            if (frameBytes[i] == 0x7D) {
                jsonEnd = i + 1;
                break;
            }
        }
        byte[] jsonBytes = new byte[jsonEnd - jsonStart];
        System.arraycopy(frameBytes, jsonStart, jsonBytes, 0, jsonBytes.length);
        String jsonStr = new String(jsonBytes, "UTF-8");
        System.out.println("\n解析出的JSON数据：\n" + jsonStr);

        // 5. JSON反序列化为实体对象（核心）
        DroneTargetEntity target = OBJECT_MAPPER.readValue(jsonStr, DroneTargetEntity.class);

        // 6. 校验和验证
        byte receivedChecksum = frameBytes[jsonEnd];
        byte calcChecksum = calculateChecksum(frameBytes, jsonEnd);
        System.out.println("\n==== 校验和 ====");
        System.out.println("接收的校验和：0x" + Integer.toHexString(receivedChecksum & 0xFF));
        System.out.println("计算的校验和：0x" + Integer.toHexString(calcChecksum & 0xFF));
        System.out.println("校验结果：" + (receivedChecksum == calcChecksum ? "通过" : "不通过"));

        return target;
    }

    /**
     * 解析协议帧，返回实体对象
     */
    public static DroneTargetEntity parseTargetFrame(byte[] frameBytes) throws Exception {
        System.out.println("解析的字节数组长度：" + frameBytes.length);
        // 1. 校验帧头和保留字段
        if (frameBytes[0] != FRAME_HEAD1 || frameBytes[1] != FRAME_HEAD2) {
            throw new RuntimeException("帧头错误：非合法协议数据");
        }
        if (frameBytes[2] != RESERVED_01 || frameBytes[3] != RESERVED_02) {
            throw new RuntimeException("保留字段校验失败");
        }

        // 2. 校验命令类型
        byte cmdType = frameBytes[4];
        if (cmdType != CMD_UPLOAD_TARGET) {
            throw new RuntimeException("不支持的命令类型：0x" + Integer.toHexString(cmdType & 0xFF));
        }
        System.out.println("命令类型：0x60（上传探测目标信息）");

        // 3. 解析参数长度（小端字节序，核心修复）
        short paramLen = ByteBuffer.wrap(frameBytes, 5, 2)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getShort();
        System.out.println("参数段长度：" + paramLen + "字节");

        // 4. 提取JSON字节数组并转字符串
        // 兼容：若参数长度超出实际数据，则取到JSON结尾符 7D 为止
        int jsonStart = 7;
        int jsonEnd = Math.min(jsonStart + paramLen, frameBytes.length - 1);
        // 兜底：查找JSON结尾符 } (0x7D)
        for (int i = jsonStart; i < frameBytes.length - 1; i++) {
            if (frameBytes[i] == 0x7D) {
                jsonEnd = i + 1;
                break;
            }
        }
        byte[] jsonBytes = new byte[jsonEnd - jsonStart];
        System.arraycopy(frameBytes, jsonStart, jsonBytes, 0, jsonBytes.length);
        String jsonStr = new String(jsonBytes, "UTF-8");
        System.out.println("\n解析出的JSON数据：\n" + jsonStr);

        // 5. JSON反序列化为实体对象（核心）
        DroneTargetEntity target = OBJECT_MAPPER.readValue(jsonStr, DroneTargetEntity.class);
        target=JSONObject.parseObject(jsonStr,DroneTargetEntity.class);
        // 6. 校验和验证
        byte receivedChecksum = frameBytes[jsonEnd];
        byte calcChecksum = calculateChecksum(frameBytes, jsonEnd);
        System.out.println("\n==== 校验和 ====");
        System.out.println("接收的校验和：0x" + Integer.toHexString(receivedChecksum & 0xFF));
        System.out.println("计算的校验和：0x" + Integer.toHexString(calcChecksum & 0xFF));
        System.out.println("校验结果：" + (receivedChecksum == calcChecksum ? "通过" : "不通过"));

        return target;
    }

    /**
     * 计算累加和校验（取低8位）
     */
    private static byte calculateChecksum(byte[] data, int endIndex) {
        int sum = 0;
        for (int i = 0; i < endIndex; i++) {
            sum += (data[i] & 0xFF);
        }
        return (byte) (sum & 0xFF);
    }
}
