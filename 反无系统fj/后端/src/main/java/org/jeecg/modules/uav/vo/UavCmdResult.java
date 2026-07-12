package org.jeecg.modules.uav.vo;

import lombok.Data;

@Data
public class UavCmdResult {
    // 帧是否合法（长度、起始码、校验和均通过）
    private boolean frameValid;
    // 下发命令的十六进制字符串（如"0x36"）
    private String issuedCmdHex;
    // 下发命令的中文描述（如"开启/关闭GNSS诱骗功能"）
    private String issuedCmdDesc;
    // 命令执行结果：true=成功（0x0F），false=失败（0xF0）
    private Boolean executeSuccess; // 帧不合法时为null
    // 错误信息（帧不合法时填充，如"帧长度错误"）
    private String errorMsg;
}
