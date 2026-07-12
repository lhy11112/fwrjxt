package org.jeecg.modules.uav.service;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.uav.socket.DeviceSocket;
import org.jeecg.modules.uav.socket.SocketManager;
import org.jeecg.modules.uav.util.ByteUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class HeartbeatService {

    // 协议常量
    private static final byte[] HEARTBEAT_CMD = {0x01, 0x00, 0x00, 0x00}; // 心跳命令类型：0x01（小端序）
    private static final byte[] FLAG_HEADER = {(byte) 0xDD, (byte) 0xCC, (byte) 0xBB, (byte) 0xAA};    // 标志头：0xAABBCCDD（小端序）
    private static final byte[] TIMESTAMP_EMPTY = new byte[9];             // 空时间戳
    private static final byte[] DATA_LENGTH_EMPTY = {0x00, 0x00, 0x00, 0x00}; // 数据长度：0

    @Autowired
    private SocketManager socketManager;
    /**
     * 向所有设备发送心跳包
     */
    public void sendHeartbeatsToAllDevices() {
        Map<Integer, DeviceSocket> deviceSockets = socketManager.getAllDeviceSockets();
        
        for (DeviceSocket socket : deviceSockets.values()) {

            if (socket.isConnected()) {
                sendHeartbeatToDevice(socket);
            }
        }
    }

    /**
     * 向单个设备发送心跳包
     */
    private void sendHeartbeatToDevice(DeviceSocket socket) {
        try {
            // 构造站ID字节（4字节，小端序）
            byte[] stationIdBytes = new byte[4];
            //int stationId = socket.getDeviceConfig().getStationId();
            int stationId = 0;
            stationIdBytes[0] = (byte) (stationId & 0xFF);
            stationIdBytes[1] = (byte) ((stationId >> 8) & 0xFF);
            stationIdBytes[2] = (byte) ((stationId >> 16) & 0xFF);
            stationIdBytes[3] = (byte) ((stationId >> 24) & 0xFF);

            // 构造心跳包（二进制流，小端序）
            byte[] heartbeatPackage = new byte[4 + 4 + 4 + 9 + 4];
            int offset = 0;

            // 1. 标志头
            System.arraycopy(FLAG_HEADER, 0, heartbeatPackage, offset, 4);
            offset += 4;

            // 2. 命令类型（心跳包：0x01）
            System.arraycopy(HEARTBEAT_CMD, 0, heartbeatPackage, offset, 4);
            offset += 4;

            // 3. 站ID
            System.arraycopy(stationIdBytes, 0, heartbeatPackage, offset, 4);
            offset += 4;

            // 4. 时间戳（置零，设备响应会返回实际时间）
            System.arraycopy(TIMESTAMP_EMPTY, 0, heartbeatPackage, offset, 9);
            offset += 9;

            // 5. 数据长度
            System.arraycopy(DATA_LENGTH_EMPTY, 0, heartbeatPackage, offset, 4);



            // 发送心跳包
            boolean success = socket.sendData(heartbeatPackage);
            log.info("-----------------发送心跳包状态："+success);
            if (!success) {
                // 发送失败处理
            }
        } catch (Exception e) {
            // 异常处理
            log.info("-----------------发送心跳包异常："+e);
        }
    }
}
