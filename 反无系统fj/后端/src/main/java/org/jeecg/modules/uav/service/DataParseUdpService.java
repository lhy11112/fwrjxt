package org.jeecg.modules.uav.service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.uav.constant.DroneProtocolParserA60;
import org.jeecg.modules.uav.constant.UavDefenseCmdConstants;
import org.jeecg.modules.uav.dto.ActiveHeartbeat;
import org.jeecg.modules.uav.entity.*;
import org.jeecg.modules.uav.enums.UavCmdEnum;
import org.jeecg.modules.uav.util.*;
import org.jeecg.modules.uav.vo.DeviceListResponse;
import org.jeecg.modules.uav.vo.DroneTargetEntity;
import org.jeecg.modules.uav.vo.UavCmdResult;
import org.jeecg.modules.uav.vo.UavInfo;
import org.jeecg.modules.wrj.entity.SpectrumInfo;
import org.jeecg.modules.wrj.service.ISpectrumInfoService;
import org.springframework.beans.BeanUtils;
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
public class DataParseUdpService {
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
    private DeviceListParser deviceListParser;
    @Autowired
    private IUavOperateLogService uavOperateLogService;
    @Autowired
    private IUavActiveHeartbeatService uavActiveHeartbeatService;
    /**
     * 解析设备发送的数据
     */
    public void parseData(Integer stationId, byte[] data) {
        try {
            // 解析命令类型（4字节，小端序，偏移4-8字节）

            // ========== 3. 提取协议头字段（按5.6协议位置） ==========
            byte senderAddr = data[2]; // 发送端地址（字节2）
            byte receiverAddr = data[3]; // 接收端地址（字节3）
            byte cmdType = data[4]; // 指令码（字节4，5.6协议的0x60）
            int paramLength = getParamLength(data[5], data[6]); // 参数长度（字节5~6，小端序）
            int checksum = Byte.toUnsignedInt(data[data.length - 1]); // 校验和（最后1字节）

            LocalDateTime dataTime = null;
            log.info("-------------------------解析命令类型（4字节，小端序，偏移4-8字节）--------------------------" + cmdType);
            // 根据命令类型解析不同数据
            switch (cmdType) {
                //检测探测目标信息及检测 0x60
                case UavDefenseCmdConstants.CMD_UPLOAD_TARGET_INFO:
                    TARGET_INFO(stationId, data, dataTime);
                    break;
                //主动式心跳帧 0xA6
                case UavDefenseCmdConstants.CMD_HEARTBEAT_ACTIVE:
                    HEARTBEAT_PASSIVE(stationId, data, dataTime);
                    break;
                //返回帧固定命令 0xA2
                case UavDefenseCmdConstants.RESPONSE_CMD:
                    RESPONSE(stationId, data, dataTime);
                    break;
                case UavDefenseCmdConstants.CMD_GET_DEVICE_LIST:

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

    private void RESPONSE(Integer stationId, byte[] data, LocalDateTime dataTime) throws Exception {
        // 通过枚举获取中文描述
        UavCmdEnum cmdEnum = UavCmdEnum.getByCmdHex(String.format("0x%02X", data[7]));
        assert cmdEnum != null;
        if (UavDefenseCmdConstants.CMD_GET_DEVICE_LIST_STR == cmdEnum.getCmdHex()) {
            DeviceListResponse deviceListResponse = deviceListParser.parse(data);
            log.info("设备列表:" + deviceListResponse);
        } else {
            //设备操作日志记录
            UavCmdResult uavCmdResult = UavCmdParser.parseResponseFrame(data);
            log.info("设备操作执行结果:" + uavCmdResult);
            UavOperateLog uavOperateLog = new UavOperateLog();
            uavOperateLog.setOperateTime(new Date());
            uavOperateLog.setResult(String.valueOf(uavCmdResult.getExecuteSuccess()));
            uavOperateLog.setStationId(stationId);
            uavOperateLog.setCmdType(uavCmdResult.getIssuedCmdHex());
            uavOperateLog.setCmdName(uavCmdResult.getIssuedCmdDesc());
            uavOperateLogService.save(uavOperateLog);
        }
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
    private void HEARTBEAT_PASSIVE(Integer stationId, byte[] data, LocalDateTime dataTime) {
        log.info("设备心跳数据包" + data);
        ActiveHeartbeat activeHeartbeat = ActiveHeartbeatParser.parse(data);
        log.info("设备心跳数据包=》实体" + activeHeartbeat);
        //实时频谱数据推送数据,前端需要实时数据解析
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(activeHeartbeat);
        String message = jsonObject.toJSONString();
        JSONObject obj = new JSONObject();
        obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_HEARTBEAT_PASSIVE);
        obj.put(WebsocketConst.MSG_ID, "M0001");
        obj.put(WebsocketConst.MSG_TXT, message);
        //webSocket.sendMessage(obj.toJSONString());
        //入库
        UavActiveHeartbeat uavActiveHeartbeat = new UavActiveHeartbeat();
        uavActiveHeartbeat.setStationId(stationId);
        BeanUtils.copyProperties(activeHeartbeat, uavActiveHeartbeat);
        BeanUtils.copyProperties(activeHeartbeat.getBandStatus(), uavActiveHeartbeat);
        BeanUtils.copyProperties(activeHeartbeat.getSystemStatus(), uavActiveHeartbeat);
        uavActiveHeartbeat.setBand58g(activeHeartbeat.getBandStatus().isBand58G());
        uavActiveHeartbeat.setBand24g(activeHeartbeat.getBandStatus().isBand24G());
        uavActiveHeartbeat.setBand14g(activeHeartbeat.getBandStatus().isBand14G());
        uavActiveHeartbeat.setBand52g(activeHeartbeat.getBandStatus().isBand52G());
        uavActiveHeartbeat.setBand900m(activeHeartbeat.getBandStatus().isBand900M());
        uavActiveHeartbeatService.save(uavActiveHeartbeat);
    }

    /**
     * 解析探测目标信息
     *
     * @param stationId
     * @param data
     * @param dataTime
     */
    private void TARGET_INFO(Integer stationId, byte[] data, LocalDateTime dataTime) throws Exception {
        DroneTargetEntity droneTargetEntity = DroneProtocolParserA60.parseTargetFrame(data);
        log.info("解析到探测目标信息=》droneTargetEntity=" + droneTargetEntity);
        //入库目标数据
        UavDetectMsg msg = new UavDetectMsg();
        msg.setStationId(stationId);
        msg.setDataTime(new Date());
        msg.setCreateTime(new Date());
        // 无人机序列号（64字节）
        msg.setSerial(droneTargetEntity.getUniqueID());
        msg.setModel(droneTargetEntity.getTargetName());
        // 无人机经度（float）
        msg.setDronLng(droneTargetEntity.getLongitude());
        // 无人机纬度（float）
        msg.setDronLat(droneTargetEntity.getLatitude());
        // 起飞点经度（float）
        msg.setHomeLng(0f);
        // 起飞点纬度（float）
        msg.setHomeLat(0f);
        // 遥控器经度（float）
        msg.setPilotLng(0f);
        // 遥控器纬度（float）
        msg.setPilotLat(0f);
        // 海拔高度（int）
        msg.setAltitude(droneTargetEntity.getAltitude());
        // 高度（float）
        msg.setHeight(droneTargetEntity.getAltitude());
        // 西速度（float）
        msg.setEastV(0f);
        // 北速度（float）
        msg.setNorthV(0f);
        // 上速度（float）
        msg.setUpV(0f);
        // 频率（U64）
        msg.setFreq(droneTargetEntity.getFreqOrNo());
        // 信号强度（float）
        msg.setRssi(droneTargetEntity.getSignalBandWidth());
        // 距离（float，km）
        msg.setDistance(droneTargetEntity.getDistance());
        // 飞手执照代码
        msg.setUuid("");
        // 飞机角度
        msg.setAngle(droneTargetEntity.getDirection());
        //速度
        msg.setSd(droneTargetEntity.getSpeed());
        //物理地址
        msg.setMac(droneTargetEntity.getWiFiMAC());
        //解密类型
        msg.setJmlx("上位机协议");
        msg.setDataType(droneTargetEntity.getDataType());
        msg.setFusedFlag(droneTargetEntity.getFusedFlag());
        detectMsgMapper.save(msg);

    }

    /**
     * 计算参数长度（字节5~6，小端序：低位在前，高位在后）
     */
    private int getParamLength(byte lowByte, byte highByte) {
        // 小端序转int：(高位字节 << 8) | 低位字节（无符号转换）
        return (Byte.toUnsignedInt(highByte) << 8) | Byte.toUnsignedInt(lowByte);
    }

    /**
     * 计算校验和（按5.6协议：发送端地址+接收端地址+命令+参数长度+参数的所有字节）
     */
    private int calculateChecksum(byte[] data, int paramLength) {
        int sum = 0;
        // 累加发送端地址（字节2）
        sum += Byte.toUnsignedInt(data[2]);
        // 累加接收端地址（字节3）
        sum += Byte.toUnsignedInt(data[3]);
        // 累加命令（字节4）
        sum += Byte.toUnsignedInt(data[4]);
        // 累加参数长度（字节5~6，2字节）
        sum += Byte.toUnsignedInt(data[5]);
        sum += Byte.toUnsignedInt(data[6]);
        // 累加参数（字节7~7+paramLength-1）
        for (int i = 7; i < 7 + paramLength; i++) {
            sum += Byte.toUnsignedInt(data[i]);
        }
        // 校验和是累加和的低8位（协议隐含规则：溢出后取模256）
        return sum % 256;
    }

    /**
     * 辅助方法：计算校验和（累加和低8位）
     */
    private static byte calculateChecksums(byte[] frame) {
        int sum = 0;
        // 校验范围：字节2（源地址）~ 字节frame.length-2（参数末尾）
        for (int i = 2; i < frame.length - 1; i++) {
            sum += (frame[i] & 0xFF);
        }
        return (byte) (sum & 0xFF);
    }
}