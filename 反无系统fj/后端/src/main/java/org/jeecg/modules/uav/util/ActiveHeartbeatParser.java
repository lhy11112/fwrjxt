package org.jeecg.modules.uav.util;
import org.jeecg.modules.uav.dto.ActiveHeartbeat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * 主动式心跳帧解析工具
 */
public class ActiveHeartbeatParser {
    // 帧总长度（固定：69 字节 = 起始码2 + 地址2 + 命令1 + 参数长度2 + 参数61 + 校验和1）
    private static final int FRAME_TOTAL_LENGTH = 69;
    // 起始码校验（0xA5 0x5A）
    private static final byte[] START_CODE = new byte[]{(byte) 0xA5, 0x5A};

    /**
     * 解析主动式心跳帧二进制数据
     * @param frameData 二进制帧数据（长度必须 69 字节）
     * @return 解析结果实体
     * @throws IllegalArgumentException 数据非法时抛出
     */
    public static ActiveHeartbeat parse(byte[] frameData) {
        // 1. 校验数据长度和起始码
        validateFrame(frameData);

        // 2. 包装 ByteBuffer（小端序，关键！）
        ByteBuffer buffer = ByteBuffer.wrap(frameData)
                .order(ByteOrder.LITTLE_ENDIAN);

        ActiveHeartbeat result = new ActiveHeartbeat();
        ActiveHeartbeat.SystemStatus systemStatus = new ActiveHeartbeat.SystemStatus();
        ActiveHeartbeat.BandStatus bandStatus = new ActiveHeartbeat.BandStatus();

        // 3. 解析基础字段（前 7 字节）
        result.setStartCode(String.format("%02X%02X", buffer.get(), buffer.get()));
        result.setSourceAddr(buffer.get() & 0xFF); // 字节转无符号int
        result.setDestAddr(buffer.get() & 0xFF);
        result.setCommand(buffer.get() & 0xFF);
        // 参数长度：2字节（低字节在前）
        int paramLength = buffer.getShort() & 0xFFFF;
        result.setParamLength(paramLength);

        // 4. 解析 Param1：终端代码（7~24 字节，共 18 字节，UTF-8 编码，去除末尾补0）
        byte[] terminalCodeBytes = new byte[18];
        buffer.get(terminalCodeBytes);
        String terminalCode = new String(terminalCodeBytes, StandardCharsets.UTF_8)
                .replaceAll("\0", ""); // 去掉补位的 0x00
        result.setTerminalCode(terminalCode);

        // 5. 解析 Param2：系统状态（25~26 字节，2字节，低字节在前）
        short systemStatusShort = buffer.getShort();
        systemStatus.setAuthNormal(((systemStatusShort >> 0) & 1) == 0); // Bit0：0=正常
        systemStatus.setDetectEnabled(((systemStatusShort >> 1) & 1) == 1); // Bit1：1=开启
        systemStatus.setCounterEnabled(((systemStatusShort >> 2) & 1) == 1); // Bit2：1=开启
        systemStatus.setDetectorOnline(((systemStatusShort >> 3) & 1) == 1); // Bit3：1=正常
        systemStatus.setCounterOnline(((systemStatusShort >> 4) & 1) == 1); // Bit4：1=正常
        systemStatus.setUnattendedMode(((systemStatusShort >> 5) & 1) == 1); // Bit5：1=开启
        systemStatus.setDeceiverOnline(((systemStatusShort >> 6) & 1) == 1); // Bit6：1=正常
        result.setSystemStatus(systemStatus);

        // 6. 解析 Param3：干扰模式（27 字节，1字节）
        result.setJammingMode(buffer.get() & 0xFF);

        // 7. 解析 Param4：攻击频段（28~29 字节，2字节，低字节在前）
        short bandShort = buffer.getShort();
        bandStatus.setBand58G(((bandShort >> 0) & 1) == 1); // Bit0：5.8G
        bandStatus.setBand24G(((bandShort >> 1) & 1) == 1); // Bit1：2.4G
        bandStatus.setBand900M(((bandShort >> 2) & 1) == 1); // Bit2：900M
        bandStatus.setBand14G(((bandShort >> 3) & 1) == 1); // Bit3：1.4G
        bandStatus.setBand52G(((bandShort >> 4) & 1) == 1); // Bit4：5.2G
        result.setBandStatus(bandStatus);

        // 8. 解析 Param5：云台控制模式（30 字节，1字节）
        result.setPtzControlMode(buffer.get() & 0xFF);

        // 9. 解析 Param6：攻击倒计时（31~32 字节，2字节，ushort）
        result.setAttackCountdown(buffer.getShort() & 0xFFFF);

        // 10. 解析 Param7：系统诱骗状态（33~34 字节，2字节）
        result.setDeceptionStatus(buffer.getShort() & 0xFFFF);

        // 11. 解析 Param8：GNSS 联动状态（35 字节，1字节）
        result.setGnssLinkStatus(buffer.get() & 0xFF);

        // 12. 解析 Param9：GNSS 诱骗模式（36~37 字节，2字节）
        result.setGnssDeceptionMode(buffer.getShort() & 0xFFFF);

        // 13. 解析 Param10：GNSS 诱导方式（38~39 字节，2字节）
        result.setGnssInduceMode(buffer.getShort() & 0xFFFF);

        // 14. 解析地理信息（float 型，小端序）
        result.setNoFlyLat(buffer.getFloat());         // Param11：禁飞区纬度
        result.setNoFlyLng(buffer.getFloat());         // Param12：禁飞区经度
        result.setNoFlyAlt(buffer.getFloat());         // Param13：禁飞区海拔
        result.setForcedLandLat(buffer.getFloat());    // Param14：迫降区纬度
        result.setForcedLandLng(buffer.getFloat());    // Param15：迫降区经度
        result.setForcedLandAlt(buffer.getFloat());    // Param16：迫降区海拔

        // 15. 解析 Param17：迫降区半径（int 型，小端序）
        result.setForcedLandRadius(buffer.getInt());

        // 16. 解析校验和（最后 1 字节）
        result.setChecksum(buffer.get() & 0xFF);

        // 17. 校验和验证（可选，根据文档累加和规则）
        validateChecksum(frameData, result.getChecksum());

        return result;
    }

    /**
     * 校验帧数据合法性（长度 + 起始码）
     */
    private static void validateFrame(byte[] frameData) {
        if (frameData == null || frameData.length != FRAME_TOTAL_LENGTH) {
            throw new IllegalArgumentException("帧数据长度非法！预期 69 字节，实际：" + (frameData == null ? 0 : frameData.length));
        }
        // 校验起始码（0xA5 0x5A）
        if (frameData[0] != START_CODE[0] || frameData[1] != START_CODE[1]) {
            throw new IllegalArgumentException("起始码非法！预期 A55A，实际：" + String.format("%02X%02X", frameData[0], frameData[1]));
        }
    }

    /**
     * 校验和验证（文档规则：累加和 = 源地址 + 目的地址 + 命令 + 参数长度 + 所有参数）
     */
    private static void validateChecksum(byte[] frameData, int checksum) {
        int sum = 0;
        // 累加范围：源地址（字节2）~ 最后一个参数（字节67），共 66 字节
        for (int i = 2; i <= 67; i++) {
            sum += (frameData[i] & 0xFF); // 字节转无符号int累加
        }
        int calculatedChecksum = sum & 0xFF; // 取低8位作为校验和
        if (calculatedChecksum != checksum) {
            throw new IllegalArgumentException("校验和不匹配！计算值：" + calculatedChecksum + "，帧中值：" + checksum);
        }
    }
}