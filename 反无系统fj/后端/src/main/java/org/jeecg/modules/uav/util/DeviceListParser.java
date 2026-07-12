package org.jeecg.modules.uav.util;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jeecg.modules.uav.vo.DeviceListResponse;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
@Component
public class DeviceListParser {
    // 协议常量
    private static final byte[] START_CODE = {(byte) 0xA5, 0x5A}; // 起始码
    private static final byte SOURCE_ADDR = 0x10; // 源地址（反无人机系统）
    private static final byte DEST_ADDR = (byte) 0xA0; // 目的地址（指控平台）
    private static final byte RESPONSE_CMD = (byte) 0xA2; // 响应命令码
    private static final byte REQUEST_CMD = 0x50; // 请求命令码（Param1预期值）
    public DeviceListResponse parse(byte[] responseBytes) throws Exception {
        // 1. 基础长度校验（最小长度：7帧头 + 2参数(1+1) + 1校验和 = 10字节，原9字节偏短，修正为10）
        if (responseBytes.length < 10) {
            throw new IllegalArgumentException("返回数据帧长度不合法：" + responseBytes.length + "，最小需10字节");
        }

        // 2. 校验起始码（不变）
        if (!checkStartCode(responseBytes)) {
            throw new IllegalArgumentException("起始码校验失败，不是合法的设备列表响应帧");
        }

        // 3. 校验地址和命令码（不变）
        if (responseBytes[2] != SOURCE_ADDR || responseBytes[3] != DEST_ADDR) {
            throw new IllegalArgumentException("地址校验失败：源地址=" + responseBytes[2] + "，目的地址=" + responseBytes[3]);
        }
        if (responseBytes[4] != RESPONSE_CMD) {
            throw new IllegalArgumentException("命令码校验失败：预期0xA2，实际=" + responseBytes[4]);
        }

        // 4. 解析参数长度（低字节在前，逻辑不变）
        int paramLength = ((responseBytes[6] & 0xFF) << 8) | (responseBytes[5] & 0xFF);

        // 核心修正：参数长度 = 总长度 - 8（帧头7字节 + 校验和1字节），删除多余的减1
        int expectedParamLength = responseBytes.length - 8;
        if (paramLength != expectedParamLength) {
            throw new IllegalArgumentException("参数长度不匹配：协议声明=" + paramLength + "，实际剩余字节=" + expectedParamLength);
        }

        // 5. 提取Param1（不变，校验是否为0x50）
        byte param1 = responseBytes[7];
        if (param1 != REQUEST_CMD) {
            throw new IllegalArgumentException("Param1校验失败：预期0x50，实际=" + param1);
        }

        // 6. 提取JSON字符串（逻辑不变，Param1占1字节，JSON从索引8开始）
        int jsonStartIndex = 8;
        int jsonLength = paramLength - 1; // 参数总长度 - Param1(1字节) = JSON长度
        byte[] jsonBytes = new byte[jsonLength];
        System.arraycopy(responseBytes, jsonStartIndex, jsonBytes, 0, jsonLength);

        // 7. 校验和验证（不变，逻辑正确）
        byte checksum = responseBytes[jsonStartIndex + jsonLength];
        if (!verifyChecksum(responseBytes, checksum)) {
            throw new IllegalArgumentException("校验和验证失败：预期=" + checksum + "，计算值=" + calculateChecksum(responseBytes));
        }

        // 8. JSON转对象（不变）
        String jsonStr = new String(jsonBytes, StandardCharsets.UTF_8);
        return JSONObject.parseObject(jsonStr, DeviceListResponse.class);
    }

    /**
     * 校验起始码（前2字节是否为0xA5 0x5A）
     */
    private boolean checkStartCode(byte[] bytes) {
        return bytes[0] == START_CODE[0] && bytes[1] == START_CODE[1];
    }

    /**
     * 计算校验和（按文档规则：累加和 = 源地址 + 目的地址 + 命令码 + 参数长度(2字节) + 参数(所有字节)）
     */
    private byte calculateChecksum(byte[] bytes) {
        int sum = 0;
        // 源地址（字节2）
        sum += bytes[2] & 0xFF;
        // 目的地址（字节3）
        sum += bytes[3] & 0xFF;
        // 命令码（字节4）
        sum += bytes[4] & 0xFF;
        // 参数长度（字节5+6）
        sum += bytes[5] & 0xFF;
        sum += bytes[6] & 0xFF;
        // 参数（字节7 ~ 校验和前）
        int checksumIndex = bytes.length - 1;
        for (int i = 7; i < checksumIndex; i++) {
            sum += bytes[i] & 0xFF;
        }
        // 取低8位作为校验和
        return (byte) (sum & 0xFF);
    }

    /**
     * 验证校验和是否正确
     */
    private boolean verifyChecksum(byte[] bytes, byte expectedChecksum) {
        return calculateChecksum(bytes) == expectedChecksum;
    }
}