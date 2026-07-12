package org.jeecg.modules.uav.model;
import org.jeecg.modules.uav.socket.SocketManager;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
/**
 *
 */
public class TcpUdpCommand {
    // ======================== 协议常量（与文档一致）========================
    private static final byte START_CODE1 = (byte) 0xA5;
    private static final byte START_CODE2 = (byte) 0x5A;
    private static final byte SENDER_ADDR = (byte) 0xA0; // 第三方指控平台地址
    private static final byte RECEIVER_ADDR = (byte) 0x10; // 反无人机系统地址
    private static final String DEFAULT_SERVER_IP = "192.168.175.210"; // 反无人机系统IP
    private static final int DEFAULT_SERVER_UDP_PORT = 9800; // 反无人机系统UDP端口
    private static final int DEFAULT_SERVER_TCP_PORT = 9800; // 反无人机系统TCP端口
    private static final boolean USE_UDP = false; // 默认使用UDP，false切换TCP

    // ======================== 命令编号（文档表1-4）========================
    private static final byte CMD_PASSIVE_HEARTBEAT = (byte) 0xA4; // 被动式心跳帧
    private static final byte CMD_SET_GIMBAL_MODE = (byte) 0x10; // 设置云台控制模式
    private static final byte CMD_ONE_KEY_LOCATE = (byte) 0x12; // 系统一键定位及校北
    private static final byte CMD_GENERAL = (byte) 0x16; // 通用指令
    private static final byte CMD_SWITCH_TARGET = (byte) 0x64; // 切换处置目标
    private static final byte CMD_SCHEDULE_TURRET = (byte) 0x68; // 上级平台调度转台式设备引导
    private static final byte CMD_SET_DETECTION = (byte) 0x20; // 开启/关闭目标探测
    private static final byte CMD_SET_ATTACK = (byte) 0x30; // 开启/关闭设备攻击
    private static final byte CMD_SET_ATTACK_MODE = (byte) 0x31; // 设置攻击模式
    private static final byte CMD_SET_ATTACK_BAND = (byte) 0x32; // 设置攻击频段
    private static final byte CMD_SET_GNSS_SPOOF = (byte) 0x36; // 开启/关闭GNSS诱骗
    private static final byte CMD_SET_GNSS_SPOOF_MODE = (byte) 0x37; // 设置GNSS诱骗模式
    private static final byte CMD_SET_AZIMUTH_SPEED = (byte) 0x38; // 设置方位和速度
    private static final byte CMD_SET_UNATTENDED = (byte) 0x40; // 开启/关闭无人值守
    private static final byte CMD_SET_GNSS_LINKAGE = (byte) 0x41; // 开启/关闭GNSS诱骗联动
    private static final byte CMD_SET_NO_FLY_ZONE = (byte) 0x42; // 设置禁飞区
    private static final byte CMD_SET_FORCED_LANDING = (byte) 0x43; // 设置定点迫降区
    private static final byte CMD_GET_DEVICE_LIST = (byte) 0x50; // 获取设备列表
    private static final byte CMD_PREVIEW_CAPTURE = (byte) 0x70; // 设备预览抓图
    private static final byte CMD_TURRET_POSITIONING = (byte) 0x71; // 设备转台定位
    private static final byte CMD_CLOUD_MIRROR = (byte) 0x72; // 设备云镜控制
    private static final byte CMD_SET_WHITELIST = (byte) 0xC0; // 设置电磁信号白名单
    private static final byte CMD_CABINET_OP = (byte) 0x80; // 箱仓操作（定制）
    private static final byte CMD_TRANSMIT_DATA = (byte) 0x90; // 透传数据包
    private static final byte CMD_RADAR_STARE = (byte) 0x65; // 雷达凝视目标




    // ======================== 工具方法（字节转换+校验和）========================
    /**
     * short转小端字节数组（2字节）
     */
    private static byte[] shortToLittleEndian(short value) {
        return ByteBuffer.allocate(2).order(java.nio.ByteOrder.LITTLE_ENDIAN).putShort(value).array();
    }

    /**
     * int转小端字节数组（4字节）
     */
    private static byte[] intToLittleEndian(int value) {
        return ByteBuffer.allocate(4).order(java.nio.ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    }

    /**
     * long转小端字节数组（8字节）
     */
    private static byte[] longToLittleEndian(long value) {
        return ByteBuffer.allocate(8).order(java.nio.ByteOrder.LITTLE_ENDIAN).putLong(value).array();
    }

    /**
     * float转小端字节数组（4字节）
     */
    private static byte[] floatToLittleEndian(float value) {
        return ByteBuffer.allocate(4).order(java.nio.ByteOrder.LITTLE_ENDIAN).putFloat(value).array();
    }

    /**
     * double转小端字节数组（8字节）
     */
    private static byte[] doubleToLittleEndian(double value) {
        return ByteBuffer.allocate(8).order(java.nio.ByteOrder.LITTLE_ENDIAN).putDouble(value).array();
    }

    /**
     * 字符串转UTF-8字节数组，不足指定长度补0x00
     */
    private static byte[] stringToUtf8Bytes(String str, int length) {
        byte[] utf8 = str.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[length];
        System.arraycopy(utf8, 0, result, 0, Math.min(utf8.length, length));
        // 不足部分补0x00
        for (int i = utf8.length; i < length; i++) {
            result[i] = 0x00;
        }
        return result;
    }

    /**
     * 计算累加和校验（从发送端地址到参数末尾）
     */
    private static byte calculateChecksum(byte[] data) {
        int sum = 0;
        // 校验范围：字节2（发送端）→ 字节data.length-1（参数末尾）
        for (int i = 2; i < data.length; i++) {
            sum += (data[i] & 0xFF); // 避免负数累加
        }
        return (byte) (sum & 0xFF);
    }

    /**
     * 发送数据包（UDP/TCP）
     */
    private static void sendPacket(byte[] packet ) throws Exception {
        if (USE_UDP) {
            // UDP发送
            SocketManager.getInstance().getAllDeviceSocketsUdp().forEach((key, client) -> {
                client.sendCommand(packet);
            });
        } else {
            SocketManager.getInstance().getAllDeviceTCPServerSocket().forEach((key, client) -> {
                client.sendData(packet);
            });
        }
    }

    /**
     * 发送数据包（UDP/TCP）
     */
    private static void sendPacket(byte[] packet, Integer stationId) throws Exception {
        if (stationId==null){
            stationId=107;
        }
        if (USE_UDP) {
            // UDP发送
            SocketManager.getInstance().getAllDeviceSocketsUdp(stationId).sendCommand(packet);
        } else {
            SocketManager.getInstance().getAllDeviceTCPServerSocket(stationId).sendData(packet);
        }
    }

    // ======================== 命令实现方法（按文档顺序）========================

    /**
     * 1. 被动式心跳帧（0xA4）
     * 文档5.1：3s发送一次，参数Param1=0x03
     */
    public static void sendPassiveHeartbeat(Integer stationId) throws Exception {
        List<Byte> packetList = new ArrayList<>();
        // 1. 起始码（2字节）
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        // 2. 发送端地址（1字节）
        packetList.add(SENDER_ADDR);
        // 3. 接收端地址（1字节）
        packetList.add(RECEIVER_ADDR);
        // 4. 命令（1字节）
        packetList.add(CMD_PASSIVE_HEARTBEAT);
        // 5. 参数长度（2字节，小端：0x01 0x00）
        byte[] paramLen = shortToLittleEndian((short) 1);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        // 6. 参数（1字节：0x03）
        packetList.add((byte) 0x03);

        // 转换为字节数组
        byte[] packetWithoutChecksum = new byte[packetList.size()];
        for (int i = 0; i < packetList.size(); i++) {
            packetWithoutChecksum[i] = packetList.get(i);
        }
        // 7. 计算校验和
        byte checksum = calculateChecksum(packetWithoutChecksum);
        // 拼接完整数据包
        byte[] fullPacket = new byte[packetWithoutChecksum.length + 1];
        System.arraycopy(packetWithoutChecksum, 0, fullPacket, 0, packetWithoutChecksum.length);
        fullPacket[fullPacket.length - 1] = checksum;

        sendPacket(fullPacket, stationId);
    }

    /**
     * 2. 设置云台控制模式（0x10）
     * 文档5.3：Param1=0x01/0x02/0x03
     * @param mode 0x01-光电控制云台；0x02-干扰控制云台；0x03-禁用联动
     */
    public static void setGimbalControlMode(int mode,Integer stationId) throws Exception {
        if (mode < 0x01 || mode > 0x03) {
            throw new IllegalArgumentException("云台模式只能是0x01/0x02/0x03");
        }
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_SET_GIMBAL_MODE);
        // 参数长度：1字节
        byte[] paramLen = shortToLittleEndian((short) 1);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        // 参数：模式
        packetList.add((byte) mode);

        // 校验和+发送
        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 3. 系统一键定位及校北（0x12）
     * 文档5.4：无参数
     */
    public static void systemOneKeyLocateAndCalibrateNorth(Integer stationId) throws Exception {
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_ONE_KEY_LOCATE);
        // 参数长度：0字节（0x00 0x00）
        byte[] paramLen = shortToLittleEndian((short) 0);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 4. 通用指令（0x16）
     * 文档5.5：Param1=0x01（软件重启）
     * @param cmdType 仅支持0x01（软件重启）
     */
    public static void sendGeneralCommand(int cmdType,Integer stationId) throws Exception {
        if (cmdType != 0x01) {
            throw new IllegalArgumentException("当前仅支持通用指令：0x01（软件重启）");
        }
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_GENERAL);
        // 参数长度：1字节
        byte[] paramLen = shortToLittleEndian((short) 1);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        // 参数：指令类型
        packetList.add((byte) cmdType);

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 5. 切换处置目标（0x64）
     * 文档5.7：Param1=目标唯一编码（UTF-8，24字节，不足补0x00）
     * @param uniqueId 目标唯一编号（最长24字符）
     */
    public static void switchDisposalTarget(String uniqueId,Integer stationId) throws Exception {
        if (uniqueId.length() > 24) {
            throw new IllegalArgumentException("目标唯一编码最长24字符");
        }
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_SWITCH_TARGET);
        // 参数长度：24字节（0x18 0x00）
        byte[] paramLen = shortToLittleEndian((short) 24);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        // 参数：目标唯一编码（24字节）
        byte[] uniqueIdBytes = stringToUtf8Bytes(uniqueId, 24);
        for (byte b : uniqueIdBytes) {
            packetList.add(b);
        }

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 6. 上级平台调度转台式设备引导信息（0x68）
     * 文档5.8：参数共40字节（0x28 0x00）
     * @param deviceKind 设备类型（0-主动雷达；1-无线电；2-光电；3-红外；4-干扰；6-诱骗；0xff-未知）
     * @param deviceId 设备ID（long，0则自动选第一台）
     * @param targetBatch 目标批号（ushort）
     * @param dataType 数据类型（0x00-无线电；0x01-雷达；0x03-ID破解；0x04-融合）
     * @param longitude 引导目标经度（double）
     * @param latitude 引导目标纬度（double）
     * @param altitude 引导目标海拔（float）
     * @param course 航向（float）
     * @param speed 速度（m/s，float）
     */
    public static void scheduleTurretDeviceGuidanceInfo(
            int deviceKind, long deviceId, short targetBatch, int dataType,
            double longitude, double latitude, float altitude, float course, float speed,Integer stationId
    ) throws Exception {
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_SCHEDULE_TURRET);
        // 参数长度：40字节（0x28 0x00）
        byte[] paramLen = shortToLittleEndian((short) 40);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        // 参数1：设备类型（1字节）
        packetList.add((byte) deviceKind);
        // 参数2：设备ID（8字节，小端）
        for (byte b : longToLittleEndian(deviceId)) {
            packetList.add(b);
        }
        // 参数3：目标批号（2字节，小端）
        for (byte b : shortToLittleEndian(targetBatch)) {
            packetList.add(b);
        }
        // 参数4：数据类型（1字节）
        packetList.add((byte) dataType);
        // 参数5：经度（8字节，小端）
        for (byte b : doubleToLittleEndian(longitude)) {
            packetList.add(b);
        }
        // 参数6：纬度（8字节，小端）
        for (byte b : doubleToLittleEndian(latitude)) {
            packetList.add(b);
        }
        // 参数7：海拔（4字节，小端）
        for (byte b : floatToLittleEndian(altitude)) {
            packetList.add(b);
        }
        // 参数8：航向（4字节，小端）
        for (byte b : floatToLittleEndian(course)) {
            packetList.add(b);
        }
        // 参数9：速度（4字节，小端）
        for (byte b : floatToLittleEndian(speed)) {
            packetList.add(b);
        }

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 7. 开启/关闭目标探测功能（0x20）
     * 文档5.9：Param1=0x00（关闭）/0x01（开启）
     *
     * @param enable
     * @param stationId
     * @throws Exception
     */
    public static void setTargetDetectionEnable(boolean enable, Integer stationId) throws Exception {
        byte param = enable ? (byte) 0x01 : (byte) 0x00;
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_SET_DETECTION);
        // 参数长度：1字节
        byte[] paramLen = shortToLittleEndian((short) 1);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        // 参数：开启/关闭标志
        packetList.add(param);

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 8. 开启/关闭设备攻击功能（0x30）
     * 文档5.10：Param1=0x00（关闭）/0x01（开启）
     *
     * @param enable    true=开启，false=关闭
     * @param stationId
     */
    public static void setDeviceAttackEnable(boolean enable, Integer stationId) throws Exception {
        byte param = enable ? (byte) 0x01 : (byte) 0x00;
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_SET_ATTACK);
        // 参数长度：1字节
        byte[] paramLen = shortToLittleEndian((short) 1);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        packetList.add(param);

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 9. 设置攻击模式（0x31）
     * 文档5.11：Param1=0x00（返航）/0x01（迫降）
     *
     * @param mode      攻击模式
     * @param stationId
     */
    public static void setAttackMode(int mode, Integer stationId) throws Exception {
        if (mode != 0x00 && mode != 0x01) {
            throw new IllegalArgumentException("攻击模式只能是0x00（返航）或0x01（迫降）");
        }
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_SET_ATTACK_MODE);
        // 参数长度：1字节
        byte[] paramLen = shortToLittleEndian((short) 1);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        packetList.add((byte) mode);

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 10. 设置攻击频段（0x32）
     * 文档5.12：Param1=2字节（小端），Bit0-5.8G，Bit1-2.4G，Bit2-900M，Bit3-1.4G，Bit4-5.2G
     *
     * @param band      频段标志（如0x05=0b00000101 → 开启5.8G和900M）
     * @param stationId
     */
    public static void setAttackFrequencyBand(int band, Integer stationId) throws Exception {
        if (band < 0 || band > 0x1F) { // 仅低5位有效
            throw new IllegalArgumentException("频段标志只能是0x00~0x1F");
        }
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_SET_ATTACK_BAND);
        // 参数长度：2字节
        byte[] paramLen = shortToLittleEndian((short) 2);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        // 参数：频段标志（2字节，小端）
        for (byte b : shortToLittleEndian((short) band)) {
            packetList.add(b);
        }

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 11. 开启/关闭GNSS诱骗功能（0x36）
     * 文档5.13：Param1=2字节（诱骗类型），Param2=1字节（开启/关闭）
     * @param spoofType 诱骗类型：0x0001（诱导），0x0002（迫降/禁飞），0x0003（导航压制）
     * @param enable true=开启（0x01），false=关闭（0x00）
     */
    public static void setGnssSpoofEnable(int spoofType, boolean enable,Integer stationId) throws Exception {
        if (spoofType < 0x0001 || spoofType > 0x0003) {
            throw new IllegalArgumentException("诱骗类型只能是0x0001/0x0002/0x0003");
        }
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_SET_GNSS_SPOOF);
        // 参数长度：3字节（0x03 0x00）
        byte[] paramLen = shortToLittleEndian((short) 3);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        // 参数1：诱骗类型（2字节，小端）
        for (byte b : shortToLittleEndian((short) spoofType)) {
            packetList.add(b);
        }
        // 参数2：开启/关闭（1字节）
        packetList.add(enable ? (byte) 0x01 : (byte) 0x00);

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 12. 设置GNSS诱骗模式（0x37）
     * 文档5.14：Param1=2字节（诱骗模式），Param2=2字节（诱导方式）
     *
     * @param spoofMode  诱骗模式：0x0000（定向驱逐），0x0001（定点迫降），0x0002（禁飞），0x0003（导航压制）
     * @param induceMode 诱导方式：0x0000（驱离），0x0001（拉近）
     * @param stationId
     */
    public static void setGnssSpoofMode(int spoofMode, int induceMode, Integer stationId) throws Exception {
        if (spoofMode < 0x0000 || spoofMode > 0x0003) {
            throw new IllegalArgumentException("诱骗模式只能是0x0000~0x0003");
        }
        if (induceMode < 0x0000 || induceMode > 0x0001) {
            throw new IllegalArgumentException("诱导方式只能是0x0000或0x0001");
        }
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_SET_GNSS_SPOOF_MODE);
        // 参数长度：4字节（0x04 0x00）
        byte[] paramLen = shortToLittleEndian((short) 4);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        // 参数1：诱骗模式（2字节，小端）
        for (byte b : shortToLittleEndian((short) spoofMode)) {
            packetList.add(b);
        }
        // 参数2：诱导方式（2字节，小端）
        for (byte b : shortToLittleEndian((short) induceMode)) {
            packetList.add(b);
        }

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 13. 设置方位和速度（0x38）
     * 文档5.15：Param1=方位（float），Param2=速度（float）
     *
     * @param azimuth   方位（°）
     * @param speed     速度（m/s）
     * @param stationId
     */
    public static void setAzimuthAndSpeed(float azimuth, float speed, Integer stationId) throws Exception {
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_SET_AZIMUTH_SPEED);
        // 参数长度：8字节（0x08 0x00）
        byte[] paramLen = shortToLittleEndian((short) 8);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        // 参数1：方位（4字节，小端）
        for (byte b : floatToLittleEndian(azimuth)) {
            packetList.add(b);
        }
        // 参数2：速度（4字节，小端）
        for (byte b : floatToLittleEndian(speed)) {
            packetList.add(b);
        }

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 14. 开启/关闭无人值守功能（0x40）
     * 文档5.16：Param1=0x00（关闭）/0x01（开启）
     *
     * @param enable    true=开启，false=关闭
     * @param stationId
     */
    public static void setUnattendedModeEnable(boolean enable, Integer stationId) throws Exception {
        byte param = enable ? (byte) 0x01 : (byte) 0x00;
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_SET_UNATTENDED);
        // 参数长度：1字节
        byte[] paramLen = shortToLittleEndian((short) 1);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        packetList.add(param);

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket,stationId);
    }

    /**
     * 15. 开启/关闭GNSS诱骗系统联动（0x41）
     * 文档5.17：Param1=0x00（关闭）/0x01（开启）
     *
     * @param enable    true=开启，false=关闭
     * @param stationId
     */
    public static void setGnssSpoofLinkageEnable(boolean enable, Integer stationId) throws Exception {
        byte param = enable ? (byte) 0x01 : (byte) 0x00;
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_SET_GNSS_LINKAGE);
        // 参数长度：1字节
        byte[] paramLen = shortToLittleEndian((short) 1);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        packetList.add(param);

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 16. 设置禁飞区位置（0x42）
     * 文档5.18：Param1=纬度（float），Param2=经度（float），Param3=海拔（float）
     * @param latitude 纬度
     * @param longitude 经度
     * @param altitude 海拔（m）
     */
    public static void setNoFlyZone(float latitude, float longitude, float altitude,Integer stationId) throws Exception {
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_SET_NO_FLY_ZONE);
        // 参数长度：12字节（0x0C 0x00）
        byte[] paramLen = shortToLittleEndian((short) 12);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        // 参数1：纬度（4字节，小端）
        for (byte b : floatToLittleEndian(latitude)) {
            packetList.add(b);
        }
        // 参数2：经度（4字节，小端）
        for (byte b : floatToLittleEndian(longitude)) {
            packetList.add(b);
        }
        // 参数3：海拔（4字节，小端）
        for (byte b : floatToLittleEndian(altitude)) {
            packetList.add(b);
        }

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 17. 设置定点迫降区（0x43）
     * 文档5.19：Param1=纬度（float），Param2=经度（float），Param3=海拔（float），Param4=半径（int）
     * @param latitude 纬度
     * @param longitude 经度
     * @param altitude 海拔（m）
     * @param radius 半径（m）
     */
    public static void setFixedPointForcedLandingZone(
            float latitude, float longitude, float altitude, int radius,Integer stationId
    ) throws Exception {
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_SET_FORCED_LANDING);
        // 参数长度：16字节（0x10 0x00）
        byte[] paramLen = shortToLittleEndian((short) 16);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        // 参数1：纬度（4字节，小端）
        for (byte b : floatToLittleEndian(latitude)) {
            packetList.add(b);
        }
        // 参数2：经度（4字节，小端）
        for (byte b : floatToLittleEndian(longitude)) {
            packetList.add(b);
        }
        // 参数3：海拔（4字节，小端）
        for (byte b : floatToLittleEndian(altitude)) {
            packetList.add(b);
        }
        // 参数4：半径（4字节，小端）
        for (byte b : intToLittleEndian(radius)) {
            packetList.add(b);
        }

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 18. 获取设备列表（0x50）
     * 文档5.20：无参数
     */
    public static void getDeviceList(Integer stationId) throws Exception {
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_GET_DEVICE_LIST);
        // 参数长度：0字节（0x00 0x00）
        byte[] paramLen = shortToLittleEndian((short) 0);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 19. 设备预览抓图（0x70）
     * 文档5.22：Param1=设备ID（long，0则自动选第一台光电设备）
     * @param deviceId 设备ID
     */
    public static void devicePreviewCapture(long deviceId,Integer stationId) throws Exception {
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_PREVIEW_CAPTURE);
        // 参数长度：8字节（0x08 0x00）
        byte[] paramLen = shortToLittleEndian((short) 8);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        // 参数：设备ID（8字节，小端）
        for (byte b : longToLittleEndian(deviceId)) {
            packetList.add(b);
        }

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 20. 设备转台定位（0x71）
     * 文档5.23：参数共17字节（0x11 0x00）
     * @param deviceKind 设备类型（0-主动雷达；1-无线电；2-光电；3-红外；4-干扰；6-诱骗）
     * @param deviceId 设备ID（long，0则自动选第一台）
     * @param panAngle 水平角度（°，相对于真北）
     * @param tiltAngle 俯仰角度（°）
     */
    public static void deviceTurretPositioning(
            int deviceKind, long deviceId, float panAngle, float tiltAngle,Integer stationId
    ) throws Exception {
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_TURRET_POSITIONING);
        // 参数长度：17字节（0x11 0x00）
        byte[] paramLen = shortToLittleEndian((short) 17);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        // 参数1：设备类型（1字节）
        packetList.add((byte) deviceKind);
        // 参数2：设备ID（8字节，小端）
        for (byte b : longToLittleEndian(deviceId)) {
            packetList.add(b);
        }
        // 参数3：水平角度（4字节，小端）
        for (byte b : floatToLittleEndian(panAngle)) {
            packetList.add(b);
        }
        // 参数4：俯仰角度（4字节，小端）
        for (byte b : floatToLittleEndian(tiltAngle)) {
            packetList.add(b);
        }

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 21. 设备云镜控制（0x72）
     * 文档5.24：参数共14字节（0x0E 0x00）
     * @param deviceKind 设备类型（0-主动雷达；1-无线电；2-光电；3-红外；4-干扰；6-诱骗）
     * @param deviceId 设备ID（long，0则自动选第一台）
     * @param operation 操作指令：0x01-Up；0x02-Down；0x03-Left；0x04-Right；0x05-LeftUp；0x06-LeftDown；0x07-RightUp；0x08-RightDown；0x09-ZoomIn；0x0A-ZoomOut
     * @param speeds 速度数组（3字节）：[水平速度, 俯仰速度, 变倍速度]
     * @param stop 停止位：true=停止（0x01），false=转动（0x00）
     */
    public static void deviceCloudMirrorControl(
            int deviceKind, long deviceId, int operation, byte[] speeds, boolean stop,Integer stationId
    ) throws Exception {
        if (operation < 0x01 || operation > 0x0A) {
            throw new IllegalArgumentException("操作指令只能是0x01~0x0A");
        }
        if (speeds == null || speeds.length != 3) {
            throw new IllegalArgumentException("速度数组必须是3字节（水平/俯仰/变倍）");
        }
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_CLOUD_MIRROR);
        // 参数长度：14字节（0x0E 0x00）
        byte[] paramLen = shortToLittleEndian((short) 14);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        // 参数1：设备类型（1字节）
        packetList.add((byte) deviceKind);
        // 参数2：设备ID（8字节，小端）
        for (byte b : longToLittleEndian(deviceId)) {
            packetList.add(b);
        }
        // 参数3：操作指令（1字节）
        packetList.add((byte) operation);
        // 参数4：速度（3字节）
        packetList.add(speeds[0]);
        packetList.add(speeds[1]);
        packetList.add(speeds[2]);
        // 参数5：停止位（1字节）
        packetList.add(stop ? (byte) 0x01 : (byte) 0x00);

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 22. 设置电磁信号白名单列表（0xC0）
     * 文档5.25：Param1=启用标志，Param2=白名单模式，Param3=白名单字符串（UTF-8）
     * @param enable true=启用（0x01），false=停用（0x00）
     * @param whitelistMode 0x00-电磁信号；0x01-飞行器ID
     * @param whitelistStr 白名单字符串（格式：{机型:[(频点范围)];...}）
     */
    public static void setElectromagneticWhitelist(boolean enable, int whitelistMode, String whitelistStr,Integer stationId) throws Exception {
        if (whitelistMode != 0x00 && whitelistMode != 0x01) {
            throw new IllegalArgumentException("白名单模式只能是0x00或0x01");
        }
        byte[] whitelistBytes = whitelistStr.getBytes(StandardCharsets.UTF_8);
        int paramLength = 2 + whitelistBytes.length; // Param1(1) + Param2(1) + 白名单字符串(N)
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_SET_WHITELIST);
        // 参数长度（2字节，小端）
        byte[] paramLen = shortToLittleEndian((short) paramLength);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        // 参数1：启用标志（1字节）
        packetList.add(enable ? (byte) 0x01 : (byte) 0x00);
        // 参数2：白名单模式（1字节）
        packetList.add((byte) whitelistMode);
        // 参数3：白名单字符串（N字节）
        for (byte b : whitelistBytes) {
            packetList.add(b);
        }

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 23. 箱仓操作（定制版本）（0x80）
     * 文档5.26：Param1=0x00（关闭）/0x01（敞开）
     * @param open true=敞开，false=关闭
     */
    public static void cabinetOperation(boolean open,Integer stationId) throws Exception {
        byte param = open ? (byte) 0x01 : (byte) 0x00;
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_CABINET_OP);
        // 参数长度：1字节
        byte[] paramLen = shortToLittleEndian((short) 1);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        packetList.add(param);

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 24. 透传数据包（0x90）
     * 文档5.28：双向传输，参数长度=N+11（设备类型1+数据源2+设备ID8+命令类型1+透传数据N）
     * @param deviceKind 设备类型（0-主动雷达；1-无线电；2-光电；3-红外；4-干扰；6-诱骗；8-智能控制箱）
     * @param dataSourceType 数据源类型（光电设备：0x01-机芯操作；0x02-云台操作）
     * @param deviceId 设备ID（long）
     * @param cmdType 命令类型（0x00-回传；0x01-询问；0x02-设置）
     * @param transmitData 透传数据包（自定义格式）
     */
    public static void transmitDataPacket(
            int deviceKind, int dataSourceType, long deviceId, int cmdType, byte[] transmitData,Integer stationId
    ) throws Exception {
        if (transmitData == null) {
            transmitData = new byte[0];
        }
        int paramLength = 1 + 2 + 8 + 1 + transmitData.length; // 设备类型(1)+数据源(2)+设备ID(8)+命令类型(1)+透传数据(N)
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_TRANSMIT_DATA);
        // 参数长度（2字节，小端）
        byte[] paramLen = shortToLittleEndian((short) paramLength);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        // 参数1：设备类型（1字节）
        packetList.add((byte) deviceKind);
        // 参数2：数据源类型（2字节，小端）
        for (byte b : shortToLittleEndian((short) dataSourceType)) {
            packetList.add(b);
        }
        // 参数3：设备ID（8字节，小端）
        for (byte b : longToLittleEndian(deviceId)) {
            packetList.add(b);
        }
        // 参数4：命令类型（1字节）
        packetList.add((byte) cmdType);
        // 参数5：透传数据（N字节）
        for (byte b : transmitData) {
            packetList.add(b);
        }

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    /**
     * 25. 雷达凝视目标（0x65）
     * 文档5.27：Param1=目标唯一编码（UTF-8，24字节，不足补0x00）
     * @param uniqueId 目标唯一编号（最长24字符）
     */
    public static void radarStareTarget(String uniqueId,Integer stationId) throws Exception {
        if (uniqueId.length() > 24) {
            throw new IllegalArgumentException("目标唯一编码最长24字符");
        }
        List<Byte> packetList = new ArrayList<>();
        packetList.add(START_CODE1);
        packetList.add(START_CODE2);
        packetList.add(SENDER_ADDR);
        packetList.add(RECEIVER_ADDR);
        packetList.add(CMD_RADAR_STARE);
        // 参数长度：24字节（0x18 0x00）
        byte[] paramLen = shortToLittleEndian((short) 24);
        packetList.add(paramLen[0]);
        packetList.add(paramLen[1]);
        // 参数：目标唯一编码（24字节）
        byte[] uniqueIdBytes = stringToUtf8Bytes(uniqueId, 24);
        for (byte b : uniqueIdBytes) {
            packetList.add(b);
        }

        byte[] packetWithoutChecksum = listToBytes(packetList);
        byte checksum = calculateChecksum(packetWithoutChecksum);
        byte[] fullPacket = concat(packetWithoutChecksum, new byte[]{checksum});
        sendPacket(fullPacket, stationId);
    }

    // ======================== 辅助工具方法 ========================
    private static byte[] listToBytes(List<Byte> list) {
        byte[] bytes = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            bytes[i] = list.get(i);
        }
        return bytes;
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    }
