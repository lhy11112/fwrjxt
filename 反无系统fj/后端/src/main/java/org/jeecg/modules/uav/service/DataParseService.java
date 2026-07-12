package org.jeecg.modules.uav.service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.uav.entity.*;
import org.jeecg.modules.uav.util.ByteUtils;
import org.jeecg.modules.uav.util.O4Utils;
import org.jeecg.modules.uav.vo.UavInfo;
import org.jeecg.modules.wrj.entity.SpectrumInfo;
import org.jeecg.modules.wrj.service.ISpectrumInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
public class DataParseService {
    // 定义 ObjectMapper 单例（避免重复创建）
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Autowired
    private IUavDetectMsgService detectMsgMapper;

    @Autowired
    private IUavDetectSpectrumService spectrumMapper;
    @Autowired
    private WebSocket webSocket;

    @Autowired
    private IUavDeviceHeartbeatService iUavDeviceHeartbeatService;

    @Autowired
    private IUavDfDataService dfDataMapper;

    @Autowired
    private IUavOperateLogService operateLogMapper;

    @Autowired
    private IUavRemoteDataService iUavRemoteDataService;
    @Autowired
    private IUavDeviceConfigService iUavDeviceConfigService;

    @Autowired
    private ISpectrumInfoService iSpectrumInfoService;
    @Autowired
    private O4Utils o4Utils;
    // 协议命令类型常量
    /**
     * 侦测报文数据
     */
    private static final int CMD_MSG_DETECT = 0x55;       // 侦测报文数据
    /**
     * 返回无人机O4解密数据需要http调用第三方接口实现解密
     */
    private static final int Cmd_TypeDecodeUavData = 0x54;
    /**
     * 频谱数据
     */
    private static final int CMD_SPECTRUM_DETECT = 0x56;  // 频谱数据
    /**
     * Remote数据包命令号
     */
    private static final int CMD_TypeRemoteUavData = 0x57;  // Remote数据包命令号
    /**
     * 心跳数据
     */
    private static final int CMD_HEARTBEAT = 0x01;        // 心跳数据
    /**
     * 无人机解析数据
     */
    private static final int CMD_DF_DATA = 0x50;          // 无人机解析数据
    /**
     * 返回频谱结果数据
     */
    private static final int Cmd_TypeSpectrumData = 0x31;  // 返回频谱结果数据
    /**
     * 诱骗功率
     */
    private static final int CMD_TRAP_AMP = 0x33;         // 诱骗功率
    /**
     * 诱骗禁飞开关
     */
    private static final int CMD_TRAP_SWITCH = 0x32;      // 诱骗禁飞开关
    /**
     * 快速干扰
     */
    private static final int CMD_DISTURB_QUICK = 0x81;    // 快速干扰

    /**
     * 解析设备发送的数据
     */
    public void parseData(Integer stationId, byte[] data) {
        if (data == null || data.length < 21) { // 最小数据包长度
            return;
        }

        try {
            // 解析命令类型（4字节，小端序，偏移4-8字节）
            byte[] cmdTypeBytes = Arrays.copyOfRange(data, 4, 8);
            int cmdType = ByteUtils.bytesToIntLittleEndian(cmdTypeBytes);

            // 解析时间戳（9字节，偏移12-21字节）
            byte[] timeBytes = Arrays.copyOfRange(data, 12, 21);
            //LocalDateTime dataTime = ByteUtils.parse9ByteTimestamp(timeBytes);
            LocalDateTime dataTime = null;
            log.info("-------------------------解析命令类型（4字节，小端序，偏移4-8字节）--------------------------" + cmdType);
            // 根据命令类型解析不同数据
            switch (cmdType) {
                case CMD_MSG_DETECT:
                    parseDetectMsg(stationId, data, dataTime);
                    break;
                case CMD_SPECTRUM_DETECT:
                    parseSpectrumData(stationId, data, dataTime);
                    break;
                case CMD_TypeRemoteUavData:
                    parseTypeRemoteUavData(stationId, data, dataTime);
                    break;
                case CMD_HEARTBEAT:
                    parseHeartbeatData(stationId, data, dataTime);
                    break;
                case CMD_DF_DATA:
                    parseDfData(stationId, data, dataTime);
                    break;
                case CMD_TRAP_AMP:
                case CMD_TRAP_SWITCH:
                case CMD_DISTURB_QUICK:
                    parseOperateLog(stationId, data, dataTime, cmdType);
                    break;
                case Cmd_TypeSpectrumData:
                    parseTypeSpectrumData(stationId, data, dataTime);
                    break;
                case Cmd_TypeDecodeUavData:
                    parseDecodeUavData(stationId, data, dataTime);
                    break;
                default:
                    // 处理未知命令
                    break;
            }
        } catch (Exception e) {
            // 解析异常处理
            log.info(e.getMessage());
        }
    }

    /**
     * 返回频谱结果数据
     *
     * @param stationId
     * @param data
     * @param dataTime
     */
    private void parseTypeSpectrumData(Integer stationId, byte[] data, LocalDateTime dataTime) {
        log.info("-------------------------返回频谱结果数据--------------------------原始数据" + data);
        // 解析具体字段（根据文档定义的报文结构）
        //u8 channel;         //通道号
        //u8  dataType;       //1频谱
        //u64	startFreq;      //开始频率(Hz)
        //u64 stopFreq;       //结束频率(Hz)
        //u32	stepFreq;       //步进频率(Hz)
        //u32	dataLen;        //回传的频点数据个数
        //short pData[1];     //回传的电平数组(dBm) 电平
        byte[] byteArray = data;
        SpectrumInfo spectrumInfo = new SpectrumInfo();
        spectrumInfo.setCreateTime(new Date());
        spectrumInfo.setStationId(stationId);
        // 校验总长度是否至少25字节
        if (byteArray.length < 25) {
            System.err.println("错误：字节流总长度不足25字节，当前长度：" + byteArray.length);
            return;
        }
        // 3. 封装ByteBuffer，设置小端序，跳过前25字节
        ByteBuffer buffer = (ByteBuffer) ByteBuffer.wrap(byteArray)
                .order(ByteOrder.LITTLE_ENDIAN)
                .position(25);

        // 打印跳过25字节后的剩余字节数
        int remainingAfterSkip = buffer.remaining();
        System.out.println("跳过25字节后剩余字节数：" + remainingAfterSkip);

        // 校验基础字段所需字节数（1+1+8+8+4+4=26字节）
        if (remainingAfterSkip < 26) {
            System.err.println("错误：剩余字节数不足基础字段解析（需26字节），当前剩余：" + remainingAfterSkip);
            return;
        }

        // 4. 依次解析字段
        int channel = buffer.get() & 0xFF;
        int dataType = buffer.get() & 0xFF;
        long startFreq = buffer.getLong();
        long stopFreq = buffer.getLong();
        int stepFreq = buffer.getInt();
        int dataLen = buffer.getInt();

        // 打印关键解析值，排查dataLen是否合理
        System.out.println("===== 基础字段解析结果 =====");
        System.out.println("通道号(channel)：" + channel);
        System.out.println("数据类型(dataType)：" + dataType);
        System.out.println("开始频率(startFreq)：" + startFreq + " Hz");
        System.out.println("结束频率(stopFreq)：" + stopFreq + " Hz");
        System.out.println("步进频率(stepFreq)：" + stepFreq + " Hz");
        System.out.println("频点数据个数(dataLen)：" + dataLen);

        // 计算读取pData需要的字节数，并校验
        int requiredBytesForPData = dataLen * 2;
        int remainingForPData = buffer.remaining();
        System.out.println("读取基础字段后剩余字节数：" + remainingForPData);
        System.out.println("解析pData需要的字节数：" + requiredBytesForPData);

        if (remainingForPData < requiredBytesForPData) {
            System.err.println("警告：剩余字节数不足以解析pData数组！");
            System.err.println("剩余字节数：" + remainingForPData + "，需要字节数：" + requiredBytesForPData);
            // 修正：只读取实际能读取的数量，避免崩溃
            dataLen = remainingForPData / 2;
            System.out.println("自动修正dataLen为：" + dataLen);
        }

        // 5. 解析电平数组 pData
        short[] pData = new short[dataLen];
        for (int i = 0; i < dataLen; i++) {
            pData[i] = buffer.getShort();
        }
        // ========== 核心：将 short[] 转为 JSON 字符串 ==========
        String pDataJson;
        try {
            // 直接将 short 数组转为 JSON 数组字符串
            pDataJson = OBJECT_MAPPER.writeValueAsString(pData);
            log.info("电平数组 JSON 字符串: {}", pDataJson);
        } catch (JsonProcessingException e) {
            log.error("电平数组转 JSON 失败", e);
            pDataJson = "[]"; // 异常时返回空数组
        }
        spectrumInfo.setChannel(String.valueOf(channel));
        spectrumInfo.setDataType(String.valueOf(dataType));
        spectrumInfo.setStartFreq((double) startFreq / 1000000);
        spectrumInfo.setStopFreq((double) stopFreq / 1000000);
        spectrumInfo.setStepFreq((double) stepFreq / 1000000);
        spectrumInfo.setDataLen(String.valueOf(dataLen));
        spectrumInfo.setPData(pDataJson);
        log.error("返回频谱结果数据", spectrumInfo);
        iSpectrumInfoService.save(spectrumInfo);
        //实时频谱数据推送数据,前端需要实时数据解析
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(spectrumInfo);
        String message = jsonObject.toJSONString();
        JSONObject obj = new JSONObject();
        obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_SpectrumData);
        obj.put(WebsocketConst.MSG_ID, "M0001");
        obj.put(WebsocketConst.MSG_TXT, message);
        webSocket.sendMessage(obj.toJSONString());
    }

    /**
     * 返回无人机O4解密数据
     *
     * @param stationId
     * @param data
     * @param dataTime
     */
    private void parseDecodeUavData(Integer stationId, byte[] data, LocalDateTime dataTime) {
        log.info("-------------------------返回无人机O4解密数据--------------------------原始数据" + data);
        // 解析具体字段（根据文档定义的报文结构）
        //u64 freq;
        //float rssi;
        //char uavInfo[176];
        int offset = 25; // 跳过头部
        Long freq = (ByteUtils.bytesToLongLittleEndian(Arrays.copyOfRange(data, offset, offset + 8)));
        offset += 8;
        float rssi = (ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;
        byte[] uavInfo = Arrays.copyOfRange(data, offset, offset + 176);
        String uavInfos = new String(uavInfo).trim();
        log.info("-------------------------返回无人机O4解密数据u64 freq;--------------------------" + freq);
        log.info("-------------------------返回无人机O4解密数据float rssi;--------------------------" + rssi);
        log.info("-------------------------返回无人机O4解密数据char uavInfo[176]--------------------------" + uavInfos);
        //http调用第三方接口进行解密
        UavInfo uavInfo1 = o4Utils.decrypt(uavInfos);
        log.info("-------------------------返回无人机O4解密后的数据为--------------------------" + uavInfo1);
        UavDetectMsg msg = new UavDetectMsg();
        msg.setStationId(stationId);
        msg.setDataTime(new Date());
        msg.setCreateTime(new Date());
        // 无人机序列号（64字节）
        msg.setSerial(uavInfo1.getSn());
        msg.setModel(uavInfo1.getModel());
        // 无人机经度（float）
        msg.setDronLng(uavInfo1.getLon());
        // 无人机纬度（float）
        msg.setDronLat(uavInfo1.getLat());
        // 起飞点经度（float）
        msg.setHomeLng(uavInfo1.getHome_lon());
        // 起飞点纬度（float）
        msg.setHomeLat(uavInfo1.getHome_lat());
        // 遥控器经度（float）
        msg.setPilotLng(uavInfo1.getPilot_lon());
        // 遥控器纬度（float）
        msg.setPilotLat(uavInfo1.getPilot_lat());
        // 海拔高度（int）
        msg.setAltitude(uavInfo1.getAlt());
        // 高度（float）
        msg.setHeight(uavInfo1.getHeight());
        // 西速度（float）
        msg.setEastV(0f);
        // 北速度（float）
        msg.setNorthV(uavInfo1.getY());
        // 上速度（float）
        msg.setUpV(uavInfo1.getZ());
        // 频率（U64）
        msg.setFreq(null);
        // 信号强度（float）
        msg.setRssi(null);
        // 距离（float，km）
        msg.setDistance(0f);
        // 飞手执照代码
        msg.setUuid(uavInfo1.getUuid());
        // 飞机角度
        msg.setAngle(0f);
        //速度
        msg.setSd(0d);
        //物理地址
        msg.setMac("");
        //解密类型
        msg.setJmlx("联网解密协议");
        detectMsgMapper.save(msg);
    }

    /**
     * 获取Remote数据
     * 设备探测到无人机后，自动发出此数据包，数据包内容结构如下。
     * 此数据包是接收数据包：由设备自动向控制软件发送含此命令号的数据包，发出的数据包的命令类型的值为0x57，发出的数据内容如下。
     *
     * @param stationId
     * @param data
     * @param dataTime
     */
    @Transactional(rollbackFor = Exception.class)
    public void parseTypeRemoteUavData(Integer stationId, byte[] data, LocalDateTime dataTime) {
        log.info("-------------------------解析获取Remote数据--------------------------");

        UavRemoteData uavRemoteData = new UavRemoteData();
        uavRemoteData.setDate(new Date());
        uavRemoteData.setStationId(stationId);
        // 解析具体字段（根据文档定义的报文结构）
        int offset = 25; // 跳过头部
        // rid_ssid（char[64]）  Remote ID
        byte[] rid_ssid = Arrays.copyOfRange(data, offset, offset + 64);
        offset += 64;
        String rid_ssids = new String(rid_ssid).trim();
        uavRemoteData.setRisSsid(rid_ssids);
        // rid_ssid（char[64]）  无人机序列号
        byte[] serial = Arrays.copyOfRange(data, offset, offset + 64);
        offset += 64;
        String serials = new String(serial).trim();
        uavRemoteData.setSerial(serials);
        // rid_ssid（char）  机型
        byte[] model = Arrays.copyOfRange(data, offset, offset + 64);
        offset += 64;
        String models = new String(model).trim();
        uavRemoteData.setModel(models);
        //参考Rid标准
        // 0 未知或未定义 1 固定翼飞机 2 直升飞机（或多旋翼飞机） 3 自传悬翼机 4 垂直起降固定翼飞机
        // 5 扑翼机 6 滑翔机 7 风筝 8 自由气球 9 系留气球 10 飞艇
        // 11自由落体/降落伞（无动力） 12 火箭 13 系留动力飞机 14 地面障碍物 15 其他
        Integer ua_type = (data[offset] & 0xFF);
        uavRemoteData.setUaType(ua_type.toString());
        offset++;
        //无人机经度
        double dron_lng = ((double) ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;
        uavRemoteData.setDronLng(dron_lng);
        //无人机纬度
        double dron_lat = ((double) ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;
        uavRemoteData.setDronLat(dron_lat);
        //遥控器经度
        double pilot_lng = ((double) ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;
        uavRemoteData.setPilotLng(pilot_lng);
        //遥控器纬度
        double pilot_lat = ((double) ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;
        uavRemoteData.setPilotLat(pilot_lat);
        //速度
        double speed = ((double) ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;
        uavRemoteData.setSpeed(speed);
        //垂直速度
        double vspeed = ((double) ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;
        uavRemoteData.setVspeed(vspeed);
        //direc
        //double direc=((double) ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 2;
        //uavRemoteData.setDirec(direc);
        //altitudeP
        double altitudeP = ((double) ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;
        uavRemoteData.setAltitudep(altitudeP);
        //altitudeG
        double altitudeG = ((double) ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;
        uavRemoteData.setAltitudeg(altitudeG);
        //height_AGL 高度(单位：米)
        double height_AGL = ((double) ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;
        uavRemoteData.setHeightAgl(height_AGL);
        //物理地址
        byte[] mac = Arrays.copyOfRange(data, offset, offset + 32);
        offset += 32;
        String macs = new String(mac).trim();
        uavRemoteData.setMac(macs);
        //信号强度
        double rssi = ((double) ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;
        uavRemoteData.setRssi(rssi);
        //频率(HZ,除以10e5得到Mhz) freq
        long freq = (ByteUtils.bytesToLongLittleEndian(Arrays.copyOfRange(data, offset, offset + 8)));
        offset += 8;
        uavRemoteData.setFreq(String.valueOf(freq));
        //角度
        double angle = ((double) ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;
        uavRemoteData.setAngle(angle);
        // 距离（m）
        int distance = (ByteUtils.bytesToIntLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        uavRemoteData.setDistance(distance);
        log.info("-------------------------获取Remote数据--------------------------");
        log.info("获取Remote数据：rid_ssids" + rid_ssids);
        log.info("获取Remote数据：无人机序列号" + serials);
        log.info("获取Remote数据：参考Rid标准" + ua_type);
        log.info("获取Remote数据：物理地址" + macs);
        log.info("获取Remote数据：机型" + models);
        log.info("获取Remote数据：距离（m）" + distance);
        iUavRemoteDataService.save(uavRemoteData);
        //remote数据插入到报文数据表中去
        UavDetectMsg msg = new UavDetectMsg();
        msg.setStationId(stationId);
        msg.setDataTime(new Date());
        msg.setCreateTime(new Date());
        // 无人机序列号（64字节）
        msg.setSerial(uavRemoteData.getSerial().replace("1581", ""));
        msg.setModel(uavRemoteData.getModel());
        // 无人机经度（float）
        msg.setDronLng(uavRemoteData.getDronLng());
        // 无人机纬度（float）
        msg.setDronLat(uavRemoteData.getDronLat());
        // 起飞点经度（float）
        msg.setHomeLng(0f);
        // 起飞点纬度（float）
        msg.setHomeLat(0f);
        // 遥控器经度（float）
        msg.setPilotLng(uavRemoteData.getPilotLng().floatValue());
        // 遥控器纬度（float）
        msg.setPilotLat(uavRemoteData.getPilotLat().floatValue());
        // 海拔高度（int）
        msg.setAltitude(uavRemoteData.getHeightAgl());
        // 高度（float）
        msg.setHeight(uavRemoteData.getHeightAgl());
        // 西速度（float）
        msg.setEastV(0f);
        // 北速度（float）
        msg.setNorthV(0f);
        // 上速度（float）
        msg.setUpV(0f);
        // 频率（U64）
        msg.setFreq((long) Math.round(Long.valueOf(uavRemoteData.getFreq()) / 1000000));
        // 信号强度（float）
        msg.setRssi(uavRemoteData.getRssi().floatValue());
        // 距离（float，km）
        msg.setDistance(Float.valueOf(uavRemoteData.getDistance()));
        // 飞手执照代码
        msg.setUuid("暂无!!!");
        // 飞机角度
        msg.setAngle(uavRemoteData.getAngle().floatValue());
        //速度
        msg.setSd(uavRemoteData.getSpeed());
        //物理地址
        msg.setMac(uavRemoteData.getMac());
        //解密类型
        msg.setJmlx("Remote协议解密");
        detectMsgMapper.save(msg);
    }

    /**
     * 获取报文数据指令eCmdMsgDetectData（0x55）
     * 设备探测到无人机后，自动发出此数据包，数据包内容结构如下。
     * 此数据包是接收数据包：由设备自动向控制软件发送含此命令号的数据包，发出的数据包的命令类型的值为0x55，发出的数据内容如下。
     *
     * @param stationId
     * @param data
     * @param dataTime
     */
    @Transactional(rollbackFor = Exception.class)
    public void parseDetectMsg(Integer stationId, byte[] data, LocalDateTime dataTime) {
        log.info("-------------------------解析获取报文数据指令eCmdMsgDetectData（0x55）--------------------------");
        UavDetectMsg msg = new UavDetectMsg();
        msg.setStationId(stationId);
        msg.setDataTime(new Date());
        msg.setCreateTime(new Date());

        // 解析具体字段（根据文档定义的报文结构）
        int offset = 25; // 跳过头部

        // 无人机序列号（64字节）
        byte[] serialBytes = Arrays.copyOfRange(data, offset, offset + 64);
        msg.setSerial(new String(serialBytes).trim());
        offset += 64;

        // 机型（64字节）
        byte[] modelBytes = Arrays.copyOfRange(data, offset, offset + 64);
        msg.setModel(new String(modelBytes).trim());
        offset += 64;

        // 无人机经度（float）
        msg.setDronLng((double) ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // 无人机纬度（float）
        msg.setDronLat((double) ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // 起飞点经度（float）
        msg.setHomeLng(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // 起飞点纬度（float）
        msg.setHomeLat(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // 遥控器经度（float）
        msg.setPilotLng(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // 遥控器纬度（float）
        msg.setPilotLat(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // 海拔高度（int）
        msg.setAltitude((double) ByteUtils.bytesToIntLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // 高度（float）
        msg.setHeight((double) ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // 西速度（float）
        msg.setEastV(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // 北速度（float）
        msg.setNorthV(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // 上速度（float）
        msg.setUpV(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // 频率（U64）
        msg.setFreq((long) Math.round(ByteUtils.bytesToLongLittleEndian(Arrays.copyOfRange(data, offset, offset + 8)) / 1000000));
        offset += 8;

        // 信号强度（float）
        msg.setRssi(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // 距离（float，km）
        //换算成米
        msg.setDistance(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // 飞手执照代码
        byte[] ridBytes = Arrays.copyOfRange(data, offset, offset + 64);
        msg.setUuid(new String(ridBytes).trim());
        offset += 64;
        // 飞机角度
        msg.setAngle(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        msg.setJmlx("报文协议解密");
        log.info("-------------------------获取报文数据指令eCmdMsgDetectData--------------------------");
        log.info("获取报文数据指令eCmdMsgDetectData（0x55）：data: " + msg);
        detectMsgMapper.save(msg);
    }

    /**
     * 获取频谱数据指令eCmdSpectrumDetectData（0x56）
     * 设备探测到无人机后，自动发出此数据包，数据包内容结构如下。
     * 此数据包是接收数据包：由设备自动向控制软件发送含此命令号的数据包，发出的数据包的命令类型的值为0x56，发出的数据内容如下。
     * model(char[64])、freq(U64)、rssi(float)、date(U64)
     *
     * @param stationId
     * @param data
     * @param dataTime
     */
    @Transactional(rollbackFor = Exception.class)
    public void parseSpectrumData(Integer stationId, byte[] data, LocalDateTime dataTime) {
        UavDetectSpectrum spectrum = new UavDetectSpectrum();
        spectrum.setStationId(stationId);
        spectrum.setDataTime(new Date());
        spectrum.setCreateTime(new Date());
        byte[] byteArray = data;
        // 3. 封装ByteBuffer，设置小端序，跳过前25字节
        ByteBuffer buffer = (ByteBuffer) ByteBuffer.wrap(byteArray)
                .order(ByteOrder.LITTLE_ENDIAN)
                .position(25);
        // 3. 解析字段1：model (char[64] → 字符串，UTF-8编码，去除末尾0填充)
        byte[] modelBytes = new byte[64];
        buffer.get(modelBytes); // 读取64字节
        // 转换为字符串并去除末尾的0（C++ char数组的0填充）
        String model = new String(modelBytes, StandardCharsets.UTF_8).trim().replaceAll("\0", "");
        spectrum.setModel(model);

        // 4. 解析字段2：freq (U64 → long，无符号64位整数)
        // Java中无U64，用long兼容（long是有符号64位，值范围0~2^63-1，若超过需特殊处理）
        long freq = buffer.getLong();
        spectrum.setFreq(freq / 1_000_000);

        // 5. 解析字段3：rssi (float → 浮点数，信号强度)
        float rssi = buffer.getFloat();
        spectrum.setRssi(rssi);

        // 带宽（Hz，U32）
        //spectrum.setBandwidth(ByteUtils.bytesToIntLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        log.info("-------------------------获取频谱数据指令eCmdSpectrumDetectData--------------------------");
        log.info("获取频谱数据指令eCmdSpectrumDetectData（0x56）：data: " + spectrum);
        spectrumMapper.save(spectrum);
    }

    /**
     * 获取设备状态(心跳包)
     * 由控制软件向设备主动发送此命令号，需要发送的数据内容为0。
     * 设备会收到控制软件心跳包命令后，返回心跳包数据，数据包的命令类型的值为0x01，发出的数据内容如下。
     *
     * @param stationId
     * @param data
     * @param dataTime
     */
    @Transactional(rollbackFor = Exception.class)
    public void parseHeartbeatData(Integer stationId, byte[] data, LocalDateTime dataTime) {
        log.info("-------------------------解析心跳数据参数开始--------------------------");
        log.info("解析心跳数据参数：stationId：" + stationId);
        log.info("解析心跳数据参数：data: " + data);
        log.info("解析心跳数据参数：LocalDateTime: " + dataTime);
        log.info("-------------------------解析心跳数据参数结束--------------------------");
        UavDeviceHeartbeat heartbeat = new UavDeviceHeartbeat();
        heartbeat.setStationId(stationId);
        //heartbeat.setDataTime(Date.from(dataTime.atZone(ZoneId.of("UTC")).toInstant()));
        heartbeat.setDataTime(new Date());
        heartbeat.setCreateTime(new Date());
        //站点id
        float staticId = ByteUtils.bytesToIntLittleEndian(Arrays.copyOfRange(data, 4, 4 + 4));
        log.info("站id:" + staticId);
        int offset = 25; // 跳过头部

        // 主板模块（0=正常，1=异常）U8
        heartbeat.setMainCard(data[offset] & 0xFF);
        offset++;
        // 诱骗模块U8
        heartbeat.setTrapCard(data[offset] & 0xFF);
        offset++;
        // 电子罗盘U8
        heartbeat.setCompass(data[offset] & 0xFF);
        offset++;
        // 干扰模块U8
        heartbeat.setDisturbCard(data[offset] & 0xFF);
        offset++;
        // 设备工作状态=>设备厂商发包之后放开代码
        heartbeat.setWorkState(data[offset] & 0xFF);
        offset++;
        // 经度（float）
        heartbeat.setLongitude(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;
        // 纬度（float）
        heartbeat.setLatitude(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;
        // 海拔（int）
        heartbeat.setAltitude(ByteUtils.bytesToIntLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;
        // 罗盘方位（度）
        heartbeat.setAngle(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // CPU使用率（%）
        heartbeat.setCpuRate(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;
        // 硬盘已用空间（MB）
        heartbeat.setDiskUsage(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;
        // 板卡温度（度）
        heartbeat.setCardTemp(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        // 功放温度（度）
        //heartbeat.setAmpTemp(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset + 32, offset + 36)));
        log.info("-------------------------解析心跳数据结果开始--------------------------");
        log.info("解析心跳数据参数：heartbeat：" + heartbeat);
        log.info("-------------------------解析心跳数据结果结束--------------------------");
        log.info("-------------------------插入数据Mapper--------------------------" + iUavDeviceHeartbeatService);
        int heartStatus = iUavDeviceHeartbeatService.getBaseMapper().insert(heartbeat);
        log.info("-------------------------插入数据心跳数据状态--------------------------" + heartStatus);
        //根据心跳数据信息更新设备经纬度情况
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("station_id", stationId);
        List<UavDeviceConfig> uavDeviceConfigList = iUavDeviceConfigService.getBaseMapper().selectByMap(objectMap);
        if (!uavDeviceConfigList.isEmpty()) {
            UavDeviceConfig uavDeviceConfig = uavDeviceConfigList.get(0);
            boolean isJd = Float.compare(uavDeviceConfig.getJd(), heartbeat.getLongitude()) == 0;
            boolean isWd = Float.compare(uavDeviceConfig.getWd(), heartbeat.getLatitude()) == 0;
            if (!isJd || !isWd
            ) {
                uavDeviceConfig.setJd((heartbeat.getLongitude()));
                uavDeviceConfig.setWd((heartbeat.getLatitude()));
                iUavDeviceConfigService.updateById(uavDeviceConfig);
            }
        }
    }

    /**
     * 探测无人机结果
     * 设备探测到无人机后，自动发出此数据包，数据包内容结构如下。
     * 此数据包是接收数据包：由设备自动向控制软件发送含此命令号的数据包，发出的数据包的命令类型的值为0x50，发出的数据内容如下。
     *
     * @param stationId
     * @param data
     * @param dataTime
     */
    @Transactional(rollbackFor = Exception.class)
    public void parseDfData(Integer stationId, byte[] data, LocalDateTime dataTime) {
        UavDfData dfData = new UavDfData();
        dfData.setStationId(stationId);
        //dfData.setDataTime(Date.from(dataTime.atZone(ZoneId.of("UTC")).toInstant()));
        dfData.setDataTime(new Date());
        dfData.setCreateTime(new Date());

        //站点id
        float staticId = ByteUtils.bytesToIntLittleEndian(Arrays.copyOfRange(data, 4, 4 + 4));
        log.info("站id:" + staticId);

        int offset = 25; // 跳过头部

        // 目标类型（0=无人机，1=遥控）
        dfData.setTargetType(data[offset] & 0xFF);
        offset++;

        // 探测类型（0=探测，1=测向）
        dfData.setDetectType(data[offset] & 0xFF);
        offset++;

        // 测向频率（U64，Hz）
        dfData.setFreq(ByteUtils.bytesToLongLittleEndian(Arrays.copyOfRange(data, offset, offset + 8)));
        offset += 8;

        // 带宽（U32，Hz）
        dfData.setDk(ByteUtils.bytesToIntLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // 经度（float）
        dfData.setLongitude(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // 纬度（float）
        dfData.setLatitude(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // 角度值（float）
        dfData.setAngle(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // 信号强度（3公里比例）
        dfData.setSignalLevel(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;

        // 罗盘方位（0-360度）
//        dfData.setCompass(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
//        offset += 4;

        // 距离
        dfData.setDistance(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;


        // 速度
        dfData.setSpeed(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;


        // 高度
        dfData.setHeight(ByteUtils.bytesToFloatLittleEndian(Arrays.copyOfRange(data, offset, offset + 4)));
        offset += 4;


        offset += 8;

        // 无人机型号（char[64]）
        byte[] modelBytes = Arrays.copyOfRange(data, offset, offset + 64);
        dfData.setUavModel(new String(modelBytes).trim());
        offset += 64;

        // 无人机ID（char[64]）
        byte[] idBytes = Arrays.copyOfRange(data, offset, offset + 64);
        dfData.setUavId(new String(idBytes).trim().replace("RID-", ""));
        offset += 64;

        // 设备ID（char[10]）
        byte[] deviceIdBytes = Arrays.copyOfRange(data, offset, offset + 10);
        dfData.setDeviceId(new String(deviceIdBytes).trim());
        log.info("-------------------------探测无人机结果--------------------------");
        log.info("探测无人机结果：data: " + dfData);
        dfDataMapper.save(dfData);
        //当目标类型为无人机并且探测类型为频谱测向
        //目标类型（0=无人机，1=遥控）
        //探测类型（0=频谱测向，1=解调报文探测 2=remote探测）
        if (dfData.getTargetType() == 0) {
            UavDetectMsg msg = new UavDetectMsg();
            msg.setStationId(stationId);
            msg.setDataTime(new Date());
            msg.setCreateTime(new Date());
            // 无人机序列号（64字节）
            msg.setSerial(dfData.getUavId().replace("RID-", ""));
            msg.setModel(dfData.getUavModel());

            // 无人机经度（float）
            msg.setDronLng(dfData.getLongitude() != 0.0f ? dfData.getLongitude().doubleValue() : 119.502);
            // 无人机纬度（float）
            msg.setDronLat(dfData.getLatitude() != 0.0f ? dfData.getLatitude().doubleValue() : 26.3108f);

            // 起飞点经度（float）
            msg.setHomeLng(0f);
            // 起飞点纬度（float）
            msg.setHomeLat(0f);
            // 遥控器经度（float）
            msg.setPilotLng((dfData.getLongitude()));
            // 遥控器纬度（float）
            msg.setPilotLat(dfData.getLatitude());
            // 海拔高度（int）
            msg.setAltitude(dfData.getHeight().doubleValue());
            // 高度（float）
            msg.setHeight(dfData.getHeight().doubleValue());
            // 西速度（float）
            msg.setEastV(0f);
            // 北速度（float）
            msg.setNorthV(0f);
            // 上速度（float）
            msg.setUpV(0f);
            // 频率（U64）
            msg.setFreq((long) Math.round(dfData.getFreq() / 1000000));
            // 信号强度（float）
            msg.setRssi(dfData.getSignalLevel());
            // 距离（float，km）
            msg.setDistance(dfData.getDistance());
            // 飞手执照代码
            msg.setUuid("暂无!!!");
            // 飞机角度
            msg.setAngle(dfData.getAngle());
            //速度
            msg.setSd(Double.valueOf(dfData.getSpeed()));
            //物理地址
            msg.setMac("");
            //解密类型
            switch (dfData.getDetectType()) {
                case 0:
                    msg.setJmlx("频谱测向");
                    break;
                case 1:
                    msg.setJmlx("报文协议解密");
                    break;
                case 2:
                    msg.setJmlx("Remote协议解密");
                    break;
                default:
                    break;
            }
            detectMsgMapper.save(msg);
        }
    }

    /**
     * 解析操作日志
     */
    @Transactional(rollbackFor = Exception.class)
    public void parseOperateLog(Integer stationId, byte[] data, LocalDateTime dataTime, int cmdType) {
        UavOperateLog operateLog = new UavOperateLog();
        operateLog.setStationId(stationId);
        operateLog.setCmdType(String.valueOf(cmdType));
        operateLog.setOperateTime(Date.from(dataTime.atZone(ZoneId.of("UTC")).toInstant()));
        operateLog.setResult("SUCCESS");

        String cmdNames = "";
        // 命令名称映射
        switch (cmdType) {
            case CMD_TRAP_AMP:
                cmdNames = "诱骗功率设置（eCmdTrapAmp）";
                break;
            case CMD_TRAP_SWITCH:
                cmdNames = "诱骗禁飞开关（eCmdTrapPosition）";
                break;
            case CMD_DISTURB_QUICK:
                cmdNames = "快速干扰（eCmdDisturbQuick）";
                break;
            default:
                cmdNames = "未知命令（0x" + Integer.toHexString(cmdType) + "）";
                break;
        }
        ;
        String cmdName = cmdNames;
        operateLog.setCmdName(cmdName);

        // 根据设备类型设置
        UavDeviceConfig device = new DeviceConfigService().getDeviceByStationId(stationId);
        if (device != null) {
            operateLog.setDeviceType(device.getDeviceType());
        }

        // 解析命令参数（JSON格式）
        int offset = 21;
        String cmdParam = "";
        if (cmdType == CMD_TRAP_AMP) {
            // 功率参数（U8：0-80dB）
            int amp = data[offset] & 0xFF;
            cmdParam = "{\"amp\":" + amp + ",\"unit\":\"dB\"}";
        } else if (cmdType == CMD_TRAP_SWITCH) {
            // 开关参数（U8：0=关闭，1=开启）
            int enable = data[offset] & 0xFF;
            cmdParam = "{\"enable\":" + enable + ",\"desc\":\"" + (enable == 1 ? "开启禁飞" : "关闭禁飞") + "\"}";
        } else if (cmdType == CMD_DISTURB_QUICK) {
            // 快速干扰参数
            int enable = data[offset] & 0xFF;
            int disturbType = data[offset + 1] & 0xFF;
            String typeDesc = "";
            switch (disturbType) {
                case 0:
                    typeDesc = "自定义";
                    break;
                case 1:
                    typeDesc = "迫降";
                    break;
                case 2:
                    typeDesc = "返航";
                    break;
                default:
                    typeDesc = "未知";
                    break;
            }
            ;
            cmdParam = "{\"enable\":" + enable + ",\"disturbType\":" + disturbType + ",\"typeDesc\":\"" + typeDesc + "\"}";
        }
        operateLog.setCmdParam(cmdParam);

        operateLogMapper.save(operateLog);
    }
}