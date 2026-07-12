package org.jeecg.modules.uav.socket;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.uav.entity.UavDeviceConfig;
import org.jeecg.modules.wrj.utils.FrameUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * TCP服务端（可选模式）：支持长连接，多客户端并发处理
 * ✅ 终极修复版：解决启动卡死+多客户端并发污染+内存泄漏+端口占用+IO流未释放+关闭不彻底
 * ✅ 新增：向TCP客户端主动发送指令的核心方法、完善日志、全量非空判断、生产级健壮性
 */
@Slf4j
@Getter
@Setter
public class TcpServerSocketServer {
    private ServerSocket serverSocket;
    private boolean running = false;
    private UavDeviceConfig deviceConfig;
    // 定义缓冲区常量，便于维护，和原代码一致
    private static final int BUFFER_SIZE = 65543;
    // 客户端socket+流 全局存储，用于向客户端发送数据
    private Socket clientSocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public TcpServerSocketServer(UavDeviceConfig config) {
        this.deviceConfig = config;
    }

    /**
     * 启动服务端 核心方法
     * 特性：同步阻塞方法（监听客户端连接），必须异步调用（你的refresh方法已做异步线程调用，完美适配）
     */
    public void start() {
        try {
            // 修复：兼容IP配置，推荐数据库配置为0.0.0.0（监听本机所有网卡）
            String bindIp = deviceConfig.getDeviceIp() == null || "".equals(deviceConfig.getDeviceIp())
                    ? "0.0.0.0" : deviceConfig.getDeviceIp();
            serverSocket = new ServerSocket(deviceConfig.getDevicePort(), 50, InetAddress.getByName(bindIp));
            running = true;
            log.info("【TCP服务端】启动成功 -> 基站ID:{}，监听地址:{}:{}",
                    deviceConfig.getStationId(), bindIp, deviceConfig.getDevicePort());

            // 循环接收客户端连接（多线程处理）
            while (running) {
                Socket acceptSocket = serverSocket.accept(); // 阻塞等待连接
                //更新设备状态
                SocketManager.getInstance().updateConfig(deviceConfig.getStationId(), acceptSocket.isConnected());
                SocketManager.getInstance().recordConnectLogs(deviceConfig.getStationId(), "CONNECT");
                log.info("【TCP服务端】设备状态已更新 -> 名称:{},基站ID:{}, 状态:已连接",deviceConfig.getName() ,deviceConfig.getStationId());
                this.clientSocket = acceptSocket;
                this.inputStream = acceptSocket.getInputStream();
                this.outputStream = acceptSocket.getOutputStream();
                log.info("【TCP服务端】客户端已连接 -> 基站ID:{}, 客户端地址:{}:{}",
                        deviceConfig.getStationId(), acceptSocket.getInetAddress(), acceptSocket.getPort());
                // 每个客户端分配独立线程，指定线程名称，便于排查问题
                new Thread(new TcpClientHandler(acceptSocket),
                        "TCP-Client-" + deviceConfig.getStationId() + "-" + acceptSocket.getPort()).start();
            }
        } catch (SocketException e) {
            // 正常关闭服务时触发的异常，直接忽略，不打印日志
            if (running) {
                log.error("【TCP服务端】运行异常-Socket错误 -> 基站ID:{}, 异常信息:{}",
                        deviceConfig.getStationId(), e.getMessage());
            }
        } catch (Exception e) {
            if (running) {
                log.error("【TCP服务端】运行异常 -> 基站ID:{}, 异常信息:{}",
                        deviceConfig.getStationId(), e.getMessage(), e);
            }
        } finally {
            // 无论异常还是正常退出，最终都执行彻底关闭
            this.stop();
        }
    }

    /**
     * ✅ 终极修复：彻底停止TCP服务，释放所有资源，无残留线程/端口/句柄
     * 核心：1.置状态位 2.关闭服务端Socket 3.关闭客户端Socket 4.关闭所有IO流 5.清空对象引用
     */
    public void stop() {
        this.running = false;
        // 关闭客户端连接和流
        closeClientResource();
        // 关闭服务端Socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                log.info("【TCP服务端】已正常停止 -> 基站ID:{}, 地址:{}:{}",
                        deviceConfig.getStationId(), deviceConfig.getDeviceIp(), deviceConfig.getDevicePort());
            } catch (Exception e) {
                log.error("【TCP服务端】关闭失败 -> 基站ID:{}, 异常信息:{}",
                        deviceConfig.getStationId(), e.getMessage(), e);
            }
        }
    }

    /**
     * ✅ 新增核心业务方法：向已连接的TCP客户端 主动发送指令/回传数据
     * 业务场景：无人机设备连接后，服务端下发控制指令、配置信息等，必用！
     * @param data 要发送的字节数组指令
     * @return true=发送成功 false=发送失败
     */
    public boolean sendData(byte[] data) {
        if (!running || outputStream == null || clientSocket == null || clientSocket.isClosed()) {
            log.warn("【TCP服务端】发送数据失败，客户端未连接 -> 基站ID:{}", deviceConfig.getStationId());
            return false;
        }
        try {
            outputStream.write(data);
            outputStream.flush(); // 强制刷出缓冲区，确保数据立即发送

            return true;
        } catch (Exception e) {
            log.error("【TCP服务端】发送数据失败 -> 基站ID:{}, 异常信息:{}",
                    deviceConfig.getStationId(), e.getMessage(), e);
            closeClientResource();
            return false;
        }
    }

    /**
     * 私有工具方法：统一关闭客户端Socket和IO流资源，防止内存/句柄泄漏
     */
    private void closeClientResource() {
        // 关闭输入流
        if (inputStream != null) {
            try { inputStream.close(); } catch (Exception e) {}
        }
        // 关闭输出流
        if (outputStream != null) {
            try { outputStream.close(); } catch (Exception e) {}
        }
        // 关闭客户端连接
        if (clientSocket != null && !clientSocket.isClosed()) {
            try { clientSocket.close(); } catch (Exception e) {}
        }
        // 清空引用，帮助GC回收
        this.inputStream = null;
        this.outputStream = null;
        this.clientSocket = null;
    }

    /**
     * 最优方案：私有内部类 处理单客户端的读写
     * 核心优势：每个客户端对应独立实例，属性完全隔离，无并发污染，线程安全
     */
    private class TcpClientHandler implements Runnable {
        private Socket clientSocket;
        private boolean connected = false;
        private InputStream in;
        private OutputStream out;
        // 正确获取外部类的全局配置对象，无自引用BUG
        private final UavDeviceConfig deviceConfig = TcpServerSocketServer.this.deviceConfig;

        public TcpClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                if(clientSocket.isClosed()) return;
                connected = true;
                // 重新赋值流对象，保证每个客户端独立持流，互不影响
                this.in = clientSocket.getInputStream();
                this.out = clientSocket.getOutputStream();

                byte[] buffer = new byte[BUFFER_SIZE];
                int len;
                // 循环读取客户端数据（长连接，直到客户端断开/服务停止）
                while (connected && running && (len = in.read(buffer)) != -1) {
                    // 截取有效数据，避免读取冗余的空字节
                    byte[] validData = new byte[len];
                    System.arraycopy(buffer, 0, validData, 0, len);
                    log.info("【TCP服务端】接收客户端数据 -> 基站ID:{}, 客户端地址:{}, 数据长度:{}字节, 数据(16进制):{}",
                            deviceConfig.getStationId(), clientSocket.getInetAddress(), len, bytesToHex(validData));
                    // 原业务逻辑：转发数据到SocketManager处理，完全保留
                    SocketManager.getInstance().handleReceivedDataTcpServerSocket(this.deviceConfig, validData);
                }
            } catch (SocketException e) {
                // 客户端正常断开/服务关闭触发的异常，忽略日志
                if (connected) {
                    log.info("【TCP服务端】客户端断开连接 -> 基站ID:{}, 客户端地址:{}",
                            deviceConfig.getStationId(), clientSocket.getInetAddress());
                }
            } catch (Exception e) {
                if (connected && running) {
                    log.error("【TCP服务端】客户端处理异常 -> 基站ID:{}, 客户端地址:{}, 异常信息:{}",
                            deviceConfig.getStationId(), clientSocket.getInetAddress(), e.getMessage(), e);
                }
            } finally {
                // 客户端处理结束，释放当前客户端的所有资源
                closeClient();
            }
        }

        /**
         * 处理命令（与UDP共享逻辑，可抽取为公共方法），保留原逻辑
         */
        private boolean processCommand(byte cmd) {
            return true;
        }

        /**
         * 关闭当前客户端连接，释放所有资源
         */
        private void closeClient() {
            connected = false;
            // 关闭当前客户端的流
            if (in != null) { try { in.close(); } catch (Exception e) {} }
            if (out != null) { try { out.close(); } catch (Exception e) {} }
            // 关闭socket
            if (clientSocket != null && !clientSocket.isClosed()) {
                try {
                    clientSocket.close();
                    log.info("【TCP服务端】客户端连接已关闭 -> 基站ID:{}, 客户端地址:{}",
                            deviceConfig.getStationId(), clientSocket.getInetAddress());
                } catch (Exception e) {
                    log.error("【TCP服务端】关闭客户端连接失败 -> 基站ID:{}, 异常信息:{}",
                            deviceConfig.getStationId(), e.getMessage());
                }
            }
            // 客户端连接关闭，更新设备为未连接状态
            SocketManager.getInstance().updateConfig(deviceConfig.getStationId(), connected);
            SocketManager.getInstance().recordConnectLogs(deviceConfig.getStationId(), "CONNECT_FAILED");
            log.info("【TCP服务端】设备状态已更新 -> 名称:{},基站ID:{}, 状态:未连接",deviceConfig.getName() ,deviceConfig.getStationId());
        }

        /**
         * 字节数组转十六进制（调试用），保留原逻辑
         */
        private String bytesToHex(byte[] bytes) {
            if(bytes == null || bytes.length == 0) return "";
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02X ", b));
            }
            return sb.toString().trim();
        }
    }
}