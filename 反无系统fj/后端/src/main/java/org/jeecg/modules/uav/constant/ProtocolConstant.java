package org.jeecg.modules.uav.constant;

/**
 * 协议类型常量定义 - 包含UDP、客户端TCP、服务端TCP
 */
public class ProtocolConstant {
    // UDP协议 常量
    public static final String PROTOCOL_TYPE_UDP = "UDP";
    // TCP客户端Socket协议（TCPSocket：主动发起连接的TCP客户端）
    public static final String PROTOCOL_TYPE_TCP_SOCKET = "TCPSocket";
    // TCP服务端Socket协议（TCPServerSocket：被动监听连接的TCP服务端）
    public static final String PROTOCOL_TYPE_TCP_SERVER_SOCKET = "TCPServerSocket";
}
