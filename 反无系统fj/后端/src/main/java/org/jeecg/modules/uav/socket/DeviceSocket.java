package org.jeecg.modules.uav.socket;

import org.jeecg.modules.uav.entity.UavDeviceConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * 对接武汉三江公司底层客户端。采用Socket协议
 *
 */
@Slf4j
@Getter
@Setter
public class DeviceSocket {
    private UavDeviceConfig deviceConfig;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private boolean isConnected;
    private Thread listenThread;
    private boolean running;

    public DeviceSocket(UavDeviceConfig config) {
        this.deviceConfig = config;
    }

    /**
     * 建立连接
     */
    public boolean connect() {
        try {
            if (isConnected) {
                log.warn("设备[{}]已处于连接状态", deviceConfig.getStationId());
                return true;
            }

            socket = new Socket(deviceConfig.getDeviceIp(), deviceConfig.getDevicePort());
            socket.setSoTimeout(2*60*1000); // 超时设置
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            isConnected = true;

            // 启动监听线程
            startListenThread();
            log.info("设备[{}]连接成功，IP:{}:{}", 
                    deviceConfig.getStationId(), 
                    deviceConfig.getDeviceIp(), 
                    deviceConfig.getDevicePort());
            return true;
        } catch (Exception e) {
            log.error("设备[{}]连接失败", deviceConfig.getStationId(), e);
            isConnected = false;
            return false;
        }
    }

    /**
     * 启动监听线程
     */
    private void startListenThread() {
        running = true;
        listenThread = new Thread(() -> {
            byte[] buffer = new byte[1024];
            int len;
            try {
                while (running && (len = inputStream.read(buffer)) != -1) {
                    byte[] data = new byte[len];
                    System.arraycopy(buffer, 0, data, 0, len);
                    // 交给数据解析服务处理
                    SocketManager.getInstance().handleReceivedData(this, data);
                }
            } catch (SocketTimeoutException e) {
                // 超时属于正常现象，不打印错误
            } catch (Exception e) {
                log.error("设备[{}]监听异常", deviceConfig.getStationId(), e);
                disconnect();
            }
        }, "Device-" + deviceConfig.getStationId() + "-Listener");
        listenThread.start();
    }

    /**
     * 发送数据
     */
    public boolean sendData(byte[] data) {
        try {
            if (!isConnected || outputStream == null) {
                log.warn("设备[{}]未连接，无法发送数据", deviceConfig.getStationId());
                return false;
            }
            outputStream.write(data);
            outputStream.flush();
            return true;
        } catch (Exception e) {
            log.error("设备[{}]发送数据失败", deviceConfig.getStationId(), e);
            disconnect();
            return false;
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        running = false;
        isConnected = false;
        
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            log.error("设备[{}]断开连接异常", deviceConfig.getStationId(), e);
        }
        
        log.info("设备[{}]已断开连接", deviceConfig.getStationId());
    }
}
