package org.jeecg.modules.uav.socket;

import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.uav.constant.CommandType;
import org.jeecg.modules.uav.constant.ProtocolConstant;
import org.jeecg.modules.uav.entity.UavConnectLog;
import org.jeecg.modules.uav.entity.UavDeviceConfig;
import org.jeecg.modules.uav.mapper.UavConnectLogMapper;
import org.jeecg.modules.uav.model.DeviceCommand;
import org.jeecg.modules.uav.service.DataParseService;
import org.jeecg.modules.uav.service.DataParseUdpService;
import org.jeecg.modules.uav.service.DeviceCommandService;
import org.jeecg.modules.uav.service.DeviceConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SocketManager {
    private static SocketManager instance;
    private final Map<Integer, DeviceSocket> TCPSocket = new ConcurrentHashMap<>();
    private final Map<Integer, UdpClient> UDP = new ConcurrentHashMap<>();
    // 全局缓存TCP服务实例，必须用线程安全的ConcurrentHashMap
    private  final Map<Integer, TcpServerSocketServer> TCPServerSocket = new ConcurrentHashMap<>();

    private final ReentrantLock lock = new ReentrantLock();

    @Autowired
    private DeviceConfigService deviceConfigService;
    
    @Autowired
    private UavConnectLogMapper connectLogMapper;
    
    @Autowired
    private DataParseService dataParseService;
    @Autowired
    private DataParseUdpService dataParseUdpService;

    @Autowired
    private DeviceCommandService deviceSocket;
    @Autowired
    private RedisUtil redisUtil;
    @Value("${wjbd.wrj.djms}")
    private String IsDjms;

    @Value("${server.port}")
    private Integer port;
    /**
     * 系统启动时初始化Socket管理器
     * 设置单例实例并初始化UDP设备连接
     * 同时配置系统的单机模式状态
     */
    @PostConstruct
    public void init() {
        instance = this;
        // 初始化时加载设备连接
        //TCPSocket
        //refreshDeviceConnectionsTCPSocket();
        //TCPServerSocket
        //refreshDeviceConnectionsTCPServerSocket();
        //UDP
        refreshDeviceConnectionsUdp();
        //系统启动时候设置单机模式开启状态
        redisUtil.set("WRJ_IsDjms",IsDjms);
    }

    public static SocketManager getInstance() {
        return instance;
    }

    /**
     * 刷新设备连接（从数据库读取配置并更新连接） TCPSocket
     * 获取有效设备：从数据库查询所有有效的TCP Socket类型设备。
     * 清理无效连接：断开已移除设备的连接。
     * 创建/更新连接：
     * 若为新设备，则创建连接并发送开始工作指令。
     * 若设备配置变更或连接断开，则重新连接并发送指令。
     * 记录日志：记录连接状态及操作结果。
     * 通过锁机制保证线程安全
     */
    public void refreshDeviceConnectionsTCPSocket() {
        lock.lock();
        try {
            List<UavDeviceConfig> devices = deviceConfigService.getAllValidDevices().stream().filter(item->item.getType().equals(ProtocolConstant.PROTOCOL_TYPE_TCP_SOCKET)).collect(Collectors.toList());
            log.info("TCPSocket:开始刷新设备连接，共发现{}个有效设备", devices.size());

            // 断开已移除的设备连接
            TCPSocket.keySet().removeIf(stationId ->
                devices.stream().noneMatch(d -> d.getStationId().equals(stationId))
            );
            DeviceCommand command = deviceSocket.createBaseCommand(CommandType.CMD_START_WORK);
            command.setDataContent(new byte[0]);
            byte[] data = command.toBytes();
            // 建立或更新设备连接
            for (UavDeviceConfig device : devices) {
                DeviceSocket socket = TCPSocket.get(device.getStationId());
                if (socket == null) {
                    // 新设备，创建连接
                    socket = new DeviceSocket(device);
                    TCPSocket.put(device.getStationId(), socket);
                    boolean connected = socket.connect();
                    recordConnectLog(device.getStationId(), connected ? "CONNECT" : "CONNECT_FAILED");
                    deviceConfigService.updateDeviceStatus(device.getStationId(), 
                            connected ? "CONNECTED" : "DISCONNECTED");
                    log.info("----------------------------------新设备，创建连接。刷新设备连接发送开始工作指令:"+data);
                    socket.sendData(data);
                } else {
                    // 已存在的设备，检查配置是否变更
                    if (!socket.getDeviceConfig().getDeviceIp().equals(device.getDeviceIp()) ||
                            !socket.getDeviceConfig().getDevicePort().equals(device.getDevicePort())) {
                        // 配置变更，重新连接
                        socket.disconnect();
                        socket.setDeviceConfig(device);
                        boolean connected = socket.connect();
                        recordConnectLog(device.getStationId(), connected ? "RECONNECT" : "RECONNECT_FAILED");
                        deviceConfigService.updateDeviceStatus(device.getStationId(), 
                                connected ? "CONNECTED" : "DISCONNECTED");
                        socket.sendData(data);
                    } else if (!socket.isConnected()) {
                        // 连接已断开，尝试重连
                        boolean connected = socket.connect();
                        recordConnectLog(device.getStationId(), connected ? "RECONNECT" : "RECONNECT_FAILED");
                        deviceConfigService.updateDeviceStatus(device.getStationId(), 
                                connected ? "CONNECTED" : "DISCONNECTED");
                        socket.sendData(data);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }


    /**
     * 刷新设备连接（从数据库读取配置并更新连接） TCPServerSocket
     * 过滤有效设备：从数据库获取所有有效的TCP服务端设备配置。
     * 清理失效设备：停止并移除不再存在的设备服务，避免端口占用和线程泄漏。
     * 建立/更新连接：
     * 若为新设备，则创建并启动新的TCP服务（异步线程执行）。
     * 若设备已存在但配置变更，则先停止旧服务，再启动新服务（同样异步处理）。
     * 异常处理与日志记录：捕获全局异常，并通过日志输出操作状态及错误信息。
     */
    public void refreshDeviceConnectionsTCPServerSocket() {
        lock.lock();
        try {
            // 过滤出TCP Server模式的有效设备
            List<UavDeviceConfig> devices = deviceConfigService.getAllValidDevices().stream()
                    .filter(item -> ProtocolConstant.PROTOCOL_TYPE_TCP_SERVER_SOCKET.equals(item.getType()))
                    .collect(Collectors.toList());
            log.info("TCPServerSocket:开始刷新设备连接，共发现{}个有效TCP服务端设备", devices.size());

            // ========== 问题修复1：移除已失效的设备，【必须先停止旧服务再移除】，防止端口占用+线程泄漏 ==========
            List<Integer> removeStationIds = TCPServerSocket.keySet().stream()
                    .filter(stationId -> devices.stream().noneMatch(d -> d.getStationId().equals(stationId)))
                    .collect(Collectors.toList());
            for (Integer stationId : removeStationIds) {
                TcpServerSocketServer oldSocket = TCPServerSocket.get(stationId);
                if (oldSocket != null) {
                    oldSocket.stop(); // 先停止服务，释放端口和线程
                    log.info("TCPServerSocket:移除失效设备，基站ID={}，已停止对应TCP服务", stationId);
                }
                TCPServerSocket.remove(stationId); // 再从缓存移除
            }

            // ========== 建立或更新设备连接 ==========
            for (UavDeviceConfig device : devices) {
                Integer stationId = device.getStationId();
                String deviceIp = device.getDeviceIp();
                Integer devicePort = device.getDevicePort();
                TcpServerSocketServer currentSocket = TCPServerSocket.get(stationId);

                if (currentSocket == null) {
                    // ========== 新设备：创建+启动TCP服务 ==========
                    TcpServerSocketServer newSocket = new TcpServerSocketServer(device);
                    TCPServerSocket.put(stationId, newSocket);
                    // 核心修复2：【每个TCP服务单独开独立线程启动】，并行加载，永不阻塞，万能异步方案
                    new Thread(() -> {
                        try {
                            newSocket.start();
                            log.info("TCPServerSocket:新设备启动成功，基站ID={}，服务地址={}:{}", stationId, deviceIp, devicePort);
                        } catch (Exception e) {
                            log.error("TCPServerSocket:新设备启动失败，基站ID={}，服务地址={}:{}，异常信息:{}", stationId, deviceIp, devicePort, e.getMessage(), e);
                            TCPServerSocket.remove(stationId); // 启动失败，移除缓存
                        }
                    }, "TCP-Server-" + stationId).start(); // 给线程命名，方便排查线程问题

                } else {
                    // ========== 已存在设备：检查配置是否变更，变更则【先停旧服务，再启新服务】 ==========
                    UavDeviceConfig oldConfig = currentSocket.getDeviceConfig();
                    boolean ipChanged = !oldConfig.getDeviceIp().equals(deviceIp);
                    boolean portChanged = !oldConfig.getDevicePort().equals(devicePort);
                    if (ipChanged || portChanged) {
                        log.info("TCPServerSocket:设备配置变更，基站ID={}，旧配置={}:{}，新配置={}:{}",
                                stationId, oldConfig.getDeviceIp(), oldConfig.getDevicePort(), deviceIp, devicePort);
                        // 核心修复3：配置变更，必须先停止旧服务，释放端口
                        currentSocket.stop();
                        // 创建新服务并启动
                        TcpServerSocketServer newSocket = new TcpServerSocketServer(device);
                        TCPServerSocket.put(stationId, newSocket);
                        // 同样单独开线程启动新服务
                        new Thread(() -> {
                            try {
                                newSocket.start();
                                log.info("TCPServerSocket:设备配置更新后重启成功，基站ID={}，新服务地址={}:{}", stationId, deviceIp, devicePort);
                            } catch (Exception e) {
                                log.error("TCPServerSocket:设备配置更新后重启失败，基站ID={}，新服务地址={}:{}，异常信息:{}", stationId, deviceIp, devicePort, e.getMessage(), e);
                                TCPServerSocket.put(stationId, currentSocket); // 启动失败，回滚旧服务
                            }
                        }, "TCP-Server-" + stationId).start();
                    } else {
                        log.info("TCPServerSocket:设备配置无变更，无需重启，基站ID={}，服务地址={}:{}", stationId, deviceIp, devicePort);
                    }
                }
            }
        } catch (Exception e) {
            log.error("TCPServerSocket:刷新设备连接时发生全局异常，异常信息:{}", e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 刷新设备连接（从数据库读取配置并更新连接） UDP
     * 该方法用于刷新UDP设备连接：
     * 加锁保护：使用ReentrantLock确保线程安全。
     * 获取有效设备：从数据库查询所有有效的UDP设备配置。
     * 断开无效连接：移除已不存在的设备连接。
     * 创建UDP套接字：初始化本地UDP端口。
     * 建立/更新连接：
     * 若设备未连接，则新建UdpClient并初始化。
     * 若设备已存在但配置变更，则重新创建并初始化连接。
     * 异常处理：捕获SocketException并抛出运行时异常。
     * 释放锁：无论是否发生异常都解锁。
     */
    public void refreshDeviceConnectionsUdp() {
        lock.lock();
        try {
            List<UavDeviceConfig> devices = deviceConfigService.getAllValidDevices().stream().filter(item->item.getType().equals(ProtocolConstant.PROTOCOL_TYPE_UDP)).collect(Collectors.toList());
            log.info("UDP设备第三方指控平台=》开始刷新设备连接，共发现{}个有效设备", devices.size());

            // 断开已移除的设备连接
            UDP.keySet().removeIf(stationId ->
                    devices.stream().noneMatch(d -> d.getStationId().equals(stationId))
            );
            // 1. 读取配置（客户端本地配置 + 第三方服务端配置）
            int localPort = port; // 本地端口，默认9800
            DatagramSocket datagramSocket=new DatagramSocket(localPort);
            // 建立或更新设备连接
            for (UavDeviceConfig device : devices) {
                UdpClient socket = UDP.get(device.getStationId());
                if (socket == null) {
                    // 新设备，创建连接
                    socket = new UdpClient(device,datagramSocket);
                    UDP.put(device.getStationId(), socket);
                    socket.initClient();


                } else {
                    // 已存在的设备，检查配置是否变更
                    if (!socket.getDeviceConfig().getDeviceIp().equals(device.getDeviceIp()) ||
                            !socket.getDeviceConfig().getDevicePort().equals(device.getDevicePort())) {
                        socket = new UdpClient(device,datagramSocket);
                        UDP.put(device.getStationId(), socket);
                        // 配置变更，重新连接
                        socket.initClient();


                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 处理接收到的数据
     */
    public void handleReceivedData(DeviceSocket socket, byte[] data) {
        try {
            dataParseService.parseData(socket.getDeviceConfig().getStationId(), data);
        } catch (Exception e) {
            log.error("解析设备[{}]数据异常", socket.getDeviceConfig().getStationId(), e);
        }
    }

    /**
     * 处理接收到的数据
     */
    public void handleReceivedDataUdp(UdpClient socket, byte[] data) {
        try {
            dataParseUdpService.parseData(socket.getDeviceConfig().getStationId(), data);
        } catch (Exception e) {
            log.error("解析设备[{}]数据异常", socket.getDeviceConfig().getStationId(), e);
        }
    }

    /**
     * 处理接收到的数据
     */
    public void handleReceivedDataTcpServerSocket(UavDeviceConfig uavDeviceConfig, byte[] data) {
        try {
            dataParseUdpService.parseData(uavDeviceConfig.getStationId(), data);
        } catch (Exception e) {
            log.error("解析设备[{}]数据异常", uavDeviceConfig.getStationId(), e);
        }
    }

    /**
     * 记录连接日志
     */
    private void recordConnectLog(Integer stationId, String eventType) {
        UavDeviceConfig device = deviceConfigService.getDeviceByStationId(stationId);
        if (device == null) return;

        UavConnectLog log = new UavConnectLog();
        log.setStationId(stationId);
        log.setDeviceIp(device.getDeviceIp());
        log.setDevicePort(device.getDevicePort());
        log.setEventType(eventType);
        log.setEventTime(new Date());
        log.setReason(eventType.contains("FAILED") ? "连接失败" : "连接成功");
        
        connectLogMapper.insert(log);
    }

    /**
     * 获取设备连接 TCPSocket
     */
    public DeviceSocket getDeviceSocket(Integer stationId) {
        return TCPSocket.get(stationId);
    }
    
    /**
     * 获取所有设备连接 TCPSocket
     */
    public Map<Integer, DeviceSocket> getAllDeviceSockets() {
        return new HashMap<>(TCPSocket);
    }

    /**
     * 获取所有设备连接 TCPServerSocket
     */
    public Map<Integer,TcpServerSocketServer> getAllDeviceTCPServerSocket() {
        return new HashMap<>(TCPServerSocket);
    }
    /**
     * 获取单个设备连接 TCPServerSocket
     */
    public TcpServerSocketServer getAllDeviceTCPServerSocket(Integer stationId) {
        return TCPServerSocket.get(stationId);
    }
    /**
     * 获取所有设备连接 UDP
     */
    public Map<Integer,UdpClient > getAllDeviceSocketsUdp() {
        return new HashMap<>(UDP);
    }
    /**
     * 获取单个设备连接 UDP
     */
    public UdpClient  getAllDeviceSocketsUdp(Integer stationId) {
        return UDP.get(stationId);
    }

    /**
     * 根据站id连接状态更新设备表连接状态
     * @param stationId
     * @param connected
     */
    public void updateConfig(Integer stationId,boolean connected){
        deviceConfigService.updateDeviceStatus(stationId,
                connected ? "CONNECTED" : "DISCONNECTED");
    }

    /**
     * 记录连接日志
     */
    public void recordConnectLogs(Integer stationId, String eventType) {
        UavDeviceConfig device = deviceConfigService.getDeviceByStationId(stationId);
        if (device == null) return;

        UavConnectLog log = new UavConnectLog();
        log.setStationId(stationId);
        log.setDeviceIp(device.getDeviceIp());
        log.setDevicePort(device.getDevicePort());
        log.setEventType(eventType);
        log.setEventTime(new Date());
        log.setReason(eventType.contains("FAILED") ? "连接失败" : "连接成功");

        connectLogMapper.insert(log);
    }
}
