package org.jeecg.modules.uav.model;

import lombok.Data;

import java.time.LocalDateTime;
import org.jeecg.modules.uav.util.ByteUtils;
/**
 * 设备命令封装类
 */
@Data
public class DeviceCommand {
    private static final int FLAG_HEADER = 0xAABBCCDD;    // 标志头
    private int commandType;                              // 命令类型
    private int stationId = 0;                            // 站ID，默认0
    private LocalDateTime timestamp;                      // 时间戳
    private byte[] dataContent;                           // 数据内容
    private String commandParam;                          //命令参数
    /**
     * 转换为字节数组（小端序）
     */
    public byte[] toBytes() {
        // 1. 标志头(4字节)
        byte[] header = ByteUtils.intToBytesLittleEndian(FLAG_HEADER);
        
        // 2. 命令类型(4字节)
        byte[] cmdType = ByteUtils.intToBytesLittleEndian(commandType);
        
        // 3. 站ID(4字节)
        byte[] station = ByteUtils.intToBytesLittleEndian(stationId);
        
        // 4. 时间戳(9字节)
        byte[] timestampBytes = ByteUtils.localDateTimeTo9Bytes(timestamp);
        
        // 5. 数据长度(4字节)
        int dataLen = dataContent == null ? 0 : dataContent.length;
        byte[] lenBytes = ByteUtils.intToBytesLittleEndian(dataLen);
        
        // 6. 拼接所有部分
        return ByteUtils.concat(
            header, cmdType, station, timestampBytes, lenBytes, 
            dataContent == null ? new byte[0] : dataContent
        );
    }
}
