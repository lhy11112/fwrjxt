package org.jeecg.modules.uav.util;
import org.jeecg.modules.uav.enums.UavCmdEnum;
import org.jeecg.modules.uav.vo.UavCmdResult;

import java.util.Arrays;

/**
 * 反无人机系统返回帧解析工具（协议V1.0.7）
 * 功能：1. 验证返回帧合法性；2. 解析下发命令（十六进制+中文）；3. 执行结果转为布尔值
 */
public class UavCmdParser {
    // 协议固定常量（与文档一致）
    private static final byte[] START_CODE = new byte[]{(byte) 0xA5, (byte) 0x5A}; // 起始码
    private static final byte RESPONSE_CMD = (byte) 0xA2; // 返回帧固定命令
    private static final int VALID_FRAME_LENGTH = 10; // 统一返回帧长度（10字节）
    private static final byte SUCCESS_PARAM2 = (byte) 0x0F; // 执行成功（Param2）
    private static final byte FAIL_PARAM2 = (byte) 0xF0; // 执行失败（Param2）

    /**
     * 解析返回帧字节数组，获取命令执行结果
     * @param responseFrame 反无人机系统返回的字节数组（统一返回帧）
     * @return UavCmdResult 解析结果（含命令信息和布尔值结果）
     */
    public static UavCmdResult parseResponseFrame(byte[] responseFrame) {
        UavCmdResult result = new UavCmdResult();

        // 1. 基础校验：帧长度
        if (responseFrame == null || responseFrame.length != VALID_FRAME_LENGTH) {
            result.setFrameValid(false);
            result.setErrorMsg("帧不合法：预期10字节，实际" + (responseFrame == null ? 0 : responseFrame.length) + "字节");
            return result;
        }

        // 2. 基础校验：起始码（0xA5 0x5A）
        byte[] frameStartCode = Arrays.copyOfRange(responseFrame, 0, 2);
        if (!Arrays.equals(frameStartCode, START_CODE)) {
            result.setFrameValid(false);
            result.setErrorMsg("帧不合法：起始码错误（预期0xA5 0x5A）");
            return result;
        }

        // 3. 基础校验：返回帧命令（固定0xA2）
        if (responseFrame[4] != RESPONSE_CMD) {
            result.setFrameValid(false);
            result.setErrorMsg("帧不合法：非返回帧（预期命令0xA2，实际0x" + String.format("%02X", responseFrame[4]) + "）");
            return result;
        }

        // 4. 基础校验：校验和（累加字节2~8，取低8位）
        byte checksum = calculateChecksum(responseFrame);
        if (checksum != responseFrame[9]) {
            result.setFrameValid(false);
            result.setErrorMsg("帧不合法：校验和错误（预期0x" + String.format("%02X", responseFrame[9]) + "，计算0x" + String.format("%02X", checksum) + "）");
            return result;
        }

        // 5. 解析下发命令（Param1：字节7，即原始下发命令的字节）
        byte issuedCmdByte = responseFrame[7];
        // 字节转十六进制字符串（如0x36，补前导0）
        String issuedCmdHex = String.format("0x%02X", issuedCmdByte);
        // 通过枚举获取中文描述
        UavCmdEnum cmdEnum = UavCmdEnum.getByCmdHex(issuedCmdHex);
        String issuedCmdDesc = cmdEnum != null ? cmdEnum.getCmdDesc() : "未知命令（" + issuedCmdHex + "）";

        // 6. 解析执行结果（Param2：字节8，转为布尔值）
        byte executeResultByte = responseFrame[8];
        Boolean executeSuccess = null;
        if (executeResultByte == SUCCESS_PARAM2) {
            executeSuccess = true;
        } else if (executeResultByte == FAIL_PARAM2) {
            executeSuccess = false;
        } else {
            result.setFrameValid(false);
            result.setErrorMsg("执行结果不合法：Param2值为0x" + String.format("%02X", executeResultByte) + "（预期0x0F/0xF0）");
            return result;
        }

        // 7. 填充解析结果
        result.setFrameValid(true);
        result.setIssuedCmdHex(issuedCmdHex);
        result.setIssuedCmdDesc(issuedCmdDesc);
        result.setExecuteSuccess(executeSuccess);
        return result;
    }

    /**
     * 计算校验和（文档规则：累加字节2~8的无符号值，取低8位）
     */
    private static byte calculateChecksum(byte[] frame) {
        int sum = 0;
        // 累加范围：字节2（源地址）~字节8（Param2）
        for (int i = 2; i <= 8; i++) {
            sum += (frame[i] & 0xFF); // 转为无符号值累加，避免Java byte负数影响
        }
        return (byte) (sum & 0xFF); // 取低8位作为校验和
    }
}
