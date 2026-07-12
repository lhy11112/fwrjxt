package org.jeecg.modules.uav.socket;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.uav.dto.TargetInfo;
import org.jeecg.modules.uav.entity.UavDeviceConfig;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 对接机动总队无人机防御系统数据。采用udp协议
 */
@Slf4j
@Getter
@Setter
public class UdpClient {
    // ==================== 协议常量（严格遵循文档）====================
    /** 起始码：0xA5 0x5A（文档2.通信格式） */
    private static final byte[] START_CODE = new byte[]{(byte) 0xA5, (byte) 0x5A};
    /** 反无人机系统地址（客户端）：0x10（文档3.地址分配） */
    private static final byte CLIENT_ADDR = (byte) 0x10;
    /** 第三方指控平台地址（服务端）：0xA0（文档3.地址分配） */
    private static final byte SERVER_ADDR = (byte) 0xA0;
    /** 命令返回标识：0xA2（文档2.命令返回格式） */
    private static final byte RESPONSE_CMD = (byte) 0xA2;
    /** 执行成功标识：0x0F（文档2.命令返回格式） */
    private static final byte SUCCESS_FLAG = (byte) 0x0F;
    /** 执行失败标识：0xF0（文档2.命令返回格式） */
    private static final byte FAIL_FLAG = (byte) 0xF0;

    // 设备配置（客户端本地配置 + 第三方服务端配置）
    private UavDeviceConfig deviceConfig;

    public UdpClient(UavDeviceConfig deviceConfig) {
        this.deviceConfig = deviceConfig;
    }

    // UDP Socket核心对象（绑定客户端本地端口）
    private DatagramSocket datagramSocket;
    // 第三方指控平台地址（服务端）
    private InetAddress serverAddress;
    // 监听线程
    private Thread listenThread;
    // 线程运行标识（原子布尔保证线程安全）
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    // 线程池（异步解析数据，避免阻塞监听）
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // 构造函数
    public UdpClient(UavDeviceConfig deviceConfig, DatagramSocket datagramSocket) {
        this.deviceConfig = deviceConfig;
        this.datagramSocket = datagramSocket;
    }

    /**
     * 初始化UDP客户端（反无人机系统），绑定本地端口并连接第三方平台
     */
    public void initClient() {
        try {

            String serverIp = deviceConfig.getDeviceIp(); // 第三方指控平台IP（文档默认：192.168.175.210）
            int serverPort = deviceConfig.getDevicePort(); // 第三方平台端口，默认9801
            // 3. 连接第三方指控平台（服务端）
            serverAddress = InetAddress.getByName(serverIp);
            datagramSocket.connect(serverAddress, serverPort);
            log.info("已连接第三方指控平台：IP={}，端口={}", serverIp, serverPort);

            // 4. 启动监听线程，接收服务端响应
            startListenThread();
            log.info("反无人机系统UDP客户端初始化完成，就绪状态");
        } catch (Exception e) {
            log.error("UDP客户端初始化异常", e);
            destroy(); // 初始化失败时释放资源
            throw new RuntimeException("反无人机系统UDP客户端启动失败", e);
        }
    }

    /**
     * 启动监听线程：持续接收第三方平台的响应数据
     */
    private void startListenThread() {
        if (isRunning.get()) {
            log.warn("监听线程已在运行，无需重复启动");
            return;
        }
        isRunning.set(true);

        listenThread = new Thread(this::listenLoop, "UAV-Client-Listen-Thread");
        listenThread.setDaemon(true); // 守护线程，随应用退出
        listenThread.start();
        log.info("监听线程启动成功，等待第三方平台响应...");
    }

    /**
     * 监听循环：阻塞接收UDP数据，解析第三方平台响应
     */
    private void listenLoop() {
        byte[] receiveBuffer = new byte[8192]; // 适配文档最大帧长（超0x10000）
        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

        while (isRunning.get()) {
            try {
                // 阻塞接收第三方平台发送的数据
                datagramSocket.receive(receivePacket);

                // 拷贝有效数据（剔除缓冲区冗余）
                int dataLen = receivePacket.getLength();
                byte[] responseData = Arrays.copyOfRange(receiveBuffer, 0, dataLen);

                log.info("【UDP】收到第三方平台响应：长度={}字节，来源={}:{}",
                        dataLen, receivePacket.getAddress().getHostAddress(), receivePacket.getPort());

                // 异步解析响应数据（避免阻塞监听）
                executorService.submit(() ->

                        SocketManager.getInstance().handleReceivedDataUdp(this, responseData)

                );

                // 重置数据包缓冲区（复用）
                receivePacket.setLength(receiveBuffer.length);
            } catch (SocketException e) {
                if (isRunning.get()) {
                    log.error("Socket接收异常（非主动关闭）", e);
                } else {
                    log.info("Socket已关闭，监听线程退出");
                }
            } catch (Exception e) {
                log.error("数据接收/解析异常", e);
            }
        }
    }

    /**
     * 释放资源（客户端关闭时执行）
     */
    public void destroy() {
        isRunning.set(false);

        // 关闭Socket
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();
            log.info("UDP Socket关闭（本地端口：{}）", datagramSocket.getLocalPort());
        }

        // 关闭线程池
        executorService.shutdown();
        log.info("客户端线程池关闭");

        // 等待监听线程退出
        if (listenThread != null && listenThread.isAlive()) {
            try {
                listenThread.join(1000);
            } catch (InterruptedException e) {
                log.warn("等待监听线程退出被中断", e);
                Thread.currentThread().interrupt();
            }
        }
        log.info("反无人机系统UDP客户端已销毁");
    }
    /**
     * 发送UDP命令到第三方指控平台（客户端核心通信方法）
     * @param cmd 命令码（参考文档4.命令集）
     * @param param 参数内容（根据命令定义，可为空）
     */
    public void sendCommand(byte[] param) {
        if (datagramSocket == null || datagramSocket.isClosed() || serverAddress == null) {
            log.warn("发送失败：客户端未初始化或已关闭");
            return;
        }

        try {
            // 1. 构建数据帧（文档表格1-1格式：起始码+发送端地址+接收端地址+命令+参数长度+参数+校验和）
            int paramLen = param == null ? 0 : param.length;

            // 5. 发送数据到第三方平台
            byte[] sendData = param;
            DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length, serverAddress, datagramSocket.getPort()
            );
            datagramSocket.send(sendPacket);
            log.info("命令发送成功：命令=0x{}，参数长度={}字节",
                    Integer.toHexString(1), paramLen);
        } catch (Exception e) {
            log.error("发送命令0x{}失败", Integer.toHexString(1), e);
        }
    }
    /**
     * 计算校验和（文档定义：累加和=源地址+目的地址+命令+参数长度+参数）
     */
    private byte calculateCheckSum(byte[] data, int checkSumIndex) {
        byte sum = 0;
        // 累加字节2（源地址）到字节checkSumIndex-1（参数末尾）
        for (int i = 2; i < checkSumIndex; i++) {
            sum += data[i];
        }
        return sum;
    }
}
