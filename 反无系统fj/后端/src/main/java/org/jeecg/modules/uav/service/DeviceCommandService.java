package org.jeecg.modules.uav.service;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import org.jeecg.modules.uav.constant.CommandType;
import org.jeecg.modules.uav.dto.ControlDto;
import org.jeecg.modules.uav.entity.UavOperateLog;
import org.jeecg.modules.uav.enums.CommandTypeEnum;
import org.jeecg.modules.uav.model.DeviceCommand;
import org.jeecg.modules.uav.socket.DeviceSocket;
import org.jeecg.modules.uav.socket.SocketManager;
import org.jeecg.modules.uav.util.ByteUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

/**
 * 设备命令发送服务
 */
@Slf4j
@Service
public class DeviceCommandService {
    // 带宽常量定义（匹配协议：10/20MHz）
    public static final int BW_10MHZ = 10;
    public static final int BW_20MHZ = 20;
    @Autowired
    private SocketManager socketManager;
    @Autowired
    private IUavOperateLogService uavOperateLogService;

    /**
     * 指定诱骗控制
     * @param enable 使能:0关闭，1开启
     * @param uavLng 飞机经度（float）
     * @param uavLat 飞机纬度（float）
     * @param tagLng 目标经度（float）
     * @param tagLat 目标纬度（float）
     * @param stationId 站点ID
     * @param controlDto 控制参数DTO（用于日志/扩展）
     */
    public void setTrapAssign(boolean enable, float uavLng, float uavLat, float tagLng, float tagLat,
                              Integer stationId, ControlDto controlDto) {
        // 1. 参数合法性校验
        // 经纬度合法性校验（粗略校验：经度范围[-180,180]，纬度范围[-90,90]）
        if (uavLng < -180 || uavLng > 180) {
            throw new IllegalArgumentException("飞机经度必须在[-180, 180]范围内");
        }
        if (uavLat < -90 || uavLat > 90) {
            throw new IllegalArgumentException("飞机纬度必须在[-90, 90]范围内");
        }
        if (tagLng < -180 || tagLng > 180) {
            throw new IllegalArgumentException("目标经度必须在[-180, 180]范围内");
        }
        if (tagLat < -90 || tagLat > 90) {
            throw new IllegalArgumentException("目标纬度必须在[-90, 90]范围内");
        }

        // 2. 构建数据内容：enable(1) + uavLng(4) + uavLat(4) + tagLng(4) + tagLat(4) = 17字节
        ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + 4 + 4 + 4)
                .order(ByteOrder.LITTLE_ENDIAN); // 保持和原有协议一致的小端序

        buffer.put((byte) (enable ? 1 : 0));          // U8 使能
        buffer.putFloat(uavLng);            // float 飞机经度
        buffer.putFloat(uavLat);            // float 飞机纬度
        buffer.putFloat(tagLng);            // float 目标经度
        buffer.putFloat(tagLat);            // float 目标纬度

        // 3. 构建命令并发送
        DeviceCommand command = createBaseCommand(CommandType.CMD_TRAP_ASSIGN); // 需在CommandType中新增该指令类型
        command.setDataContent(buffer.array());
        command.setCommandParam(JSON.toJSONString(controlDto));
        sendCommand(command, stationId);
    }
    // 重启设备
    public void rebootDevice(Integer stationId) {
        DeviceCommand command = createBaseCommand(CommandType.CMD_REBOOT);
        command.setDataContent(new byte[0]);
        sendCommand(command,stationId);
    }

    // 设备自检
    public void startheart(Integer stationId) {
        DeviceCommand command = createBaseCommand(CommandType.CMD_CHECK);
        command.setDataContent(new byte[0]);
        sendCommand(command,stationId);
    }

    // 开始工作
    public void startWork(Integer stationId) {
        DeviceCommand command = createBaseCommand(CommandType.CMD_START_WORK);
        command.setDataContent(new byte[0]);
        sendCommand(command,stationId);
    }

    // 停止工作
    public void stopWork(Integer stationId) {
        DeviceCommand command = createBaseCommand(CommandType.CMD_STOP_WORK);
        command.setDataContent(new byte[0]);
        sendCommand(command,stationId);
    }

    // 设置诱骗功率
    public void setTrapAmp(int amp,Integer stationId, ControlDto controlDto) {
        if (amp < 0 || amp > 80) {
            throw new IllegalArgumentException("功率范围必须在0~80dB之间");
        }
        DeviceCommand command = createBaseCommand(CommandType.CMD_TRAP_AMP);
        command.setDataContent(new byte[]{(byte) amp});
        command.setCommandParam(JSON.toJSONString(controlDto));
        sendCommand(command,stationId);
    }

    // 设置诱骗禁飞开关
    public void setTrapPosition(boolean enable,Integer stationId, ControlDto controlDto) {
        DeviceCommand command = createBaseCommand(CommandType.CMD_TRAP_POSITION);
        command.setDataContent(new byte[]{(byte) (enable ? 1 : 0)});
        command.setCommandParam(JSON.toJSONString(controlDto));
        sendCommand(command,stationId);
    }

    // 设置诱骗模式
    public void setTrapFun(boolean enable, int trapType,Integer stationId, ControlDto controlDto) {
        if (trapType < 0 || trapType > 1) {
            throw new IllegalArgumentException("诱骗模式只能是0(盘旋)或1(坠机)");
        }
        DeviceCommand command = createBaseCommand(CommandType.CMD_TRAP_FUN);
        command.setDataContent(new byte[]{(byte) (enable ? 1 : 0), (byte) trapType});
        command.setCommandParam(JSON.toJSONString(controlDto));
        sendCommand(command,stationId);
    }

    // 驱离功能控制
    public void setTrapExpel(boolean enable, Integer stationId, ControlDto controlDto) {
        DeviceCommand command = createBaseCommand(CommandType.CMD_TRAP_EXPEL);
        command.setDataContent(new byte[]{(byte) (enable ? 1 : 0)});
        command.setCommandParam(JSON.toJSONString(controlDto));
        sendCommand(command,stationId);
    }

    // 快速干扰控制
    public void setDisturbQuick(boolean enable, int disturbType,Integer stationId, ControlDto controlDto) {
        DeviceCommand command = createBaseCommand(CommandType.CMD_DISTURB_QUICK);
        command.setDataContent(new byte[]{(byte) (enable ? 1 : 0), (byte) disturbType});
        command.setCommandParam(JSON.toJSONString(controlDto));
        sendCommand(command,stationId);
    }

    // 自定义干扰控制
    public void setDisturbSelf(DisturbSelfParam param,Integer stationId, ControlDto controlDto) {
        // 构建数据内容: enable(1) + channel(1) + workMode(1) + freq(8) + bw(4) + scanSpeed(1) + scanPoint(1) + att(4)
        ByteBuffer buffer = ByteBuffer.allocate(1+1+1+8+4+1+1+4).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) (param.isEnable() ? 1 : 0));
        buffer.put((byte) param.getChannel());
        buffer.put((byte) param.getWorkMode());
        buffer.putLong(param.getFreq());
        buffer.putInt(param.getBw());
        buffer.put((byte) param.getScanSpeed());
        buffer.put((byte) param.getScanPoint());
        buffer.putInt(param.getAtt());

        DeviceCommand command = createBaseCommand(CommandType.CMD_DISTURB_SELF);
        command.setDataContent(buffer.array());
        command.setCommandParam(JSON.toJSONString(controlDto));
        sendCommand(command,stationId);
    }

    // 天线控制
    public void setDisturbAntenna(int dirLoop, int direction,Integer stationId, ControlDto controlDto) {
        if (direction < 0 || direction > 360) {
            throw new IllegalArgumentException("方向必须在0~360度之间");
        }
        ByteBuffer buffer = ByteBuffer.allocate(1+2).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) dirLoop);
        buffer.putShort((short) direction);

        DeviceCommand command = createBaseCommand(CommandType.CMD_DISTURB_ANTENNA);
        command.setDataContent(buffer.array());
        command.setCommandParam(JSON.toJSONString(controlDto));
        sendCommand(command,stationId);
    }

    /**
     * 下发频谱监测参数
     * @param enable 使能标识（0-禁用，1-启用）
     * @param beginFreq 开始频率（单位Hz，非负）
     * @param endFreq 结束频率（单位Hz，需大于等于beginFreq）
     * @param step 步进频率（单位Hz，默认60000，需大于0）
     * @param stationId 站点ID
     */
    public void setTypeSpectrumParam(int enable, long beginFreq, long endFreq, int step,Integer stationId) {
        // 参数合法性校验
        if (enable != 0 && enable != 1) {
            throw new IllegalArgumentException("使能标识必须为0（禁用）或1（启用）");
        }
        if (beginFreq < 0) {
            throw new IllegalArgumentException("开始频率不能为负数");
        }
        if (endFreq < beginFreq) {
            throw new IllegalArgumentException("结束频率不能小于开始频率");
        }
        if (step <= 0) {
            throw new IllegalArgumentException("步进频率必须大于0（建议60000Hz）");
        }

        // 计算字节长度：u8(1) + u64(8) + u64(8) + u32(4) = 21字节
        ByteBuffer buffer = ByteBuffer.allocate(1 + 8 + 8 + 4)
                .order(ByteOrder.LITTLE_ENDIAN); // 保持和原有代码一致的小端序

        // 按C++结构体字段顺序写入数据
        buffer.put((byte) enable);          // u8 enable (1字节)
        buffer.putLong(beginFreq*1000000);          // u64 uBeginFreq (8字节)
        buffer.putLong(endFreq*1000000);            // u64 uEndFreq (8字节)
        buffer.putInt(step);                // u32 uStep (4字节)

        DeviceCommand command = createBaseCommand(CommandType.Cmd_TypeSpectrumParam);
        command.setDataContent(buffer.array());
        sendCommand(command,stationId);
    }

    /**
     * 手动设置侧向频率
     * @param enable
     * @param dirType
     * @param freq
     * @param stationId
     */
    public void setTypeDfFreqParam(int enable, int dirType, long freq, int bw,
                                   String id, String model, Integer stationId) {
        // ========== 1. 严格的参数合法性校验 ==========

        // 校验字符串长度（char[64] 需留1位给结束符）
        String safeId = (id == null) ? "" : id;
        String safeModel = (model == null) ? "" : model;


        // ========== 2. 计算结构体总长度并初始化ByteBuffer ==========
        // 结构体字节长度：
        // u8(1) + u8(1) + u64(8) + u32(4) + char[64](64) + char[64](64) = 142字节
        int structLength = 1 + 1 + 8 + 4 + 64 + 64;
        ByteBuffer buffer = ByteBuffer.allocate(structLength)
                .order(ByteOrder.LITTLE_ENDIAN); // 保持和C++端一致的小端序

        // ========== 3. 按C++结构体字段顺序写入数据 ==========
        // 1. enable (u8)
        buffer.put((byte) enable);
        // 2. dirType (u8)
        buffer.put((byte) dirType);
        // 3. freq (u64)
        buffer.putLong(freq*1000000);
        // 4. bw (u32)
        buffer.putInt(bw);
        // 5. id (char[64] - 填充0，UTF-8编码)
        byte[] idBytes = safeId.getBytes(StandardCharsets.UTF_8);
        buffer.put(idBytes);
        // 填充剩余字节为0（补足64字节）
        for (int i = idBytes.length; i < 64; i++) {
            buffer.put((byte) 0);
        }
        // 6. model (char[64] - 填充0，UTF-8编码)
        byte[] modelBytes = safeModel.getBytes(StandardCharsets.UTF_8);
        buffer.put(modelBytes);
        // 填充剩余字节为0（补足64字节）
        for (int i = modelBytes.length; i < 64; i++) {
            buffer.put((byte) 0);
        }

        // ========== 4. 构建并发送指令 ==========
        DeviceCommand command = createBaseCommand(CommandType.Cmd_TypeDfFreq);
        command.setDataContent(buffer.array());
        sendCommand(command, stationId);
    }



    // 创建基础命令(设置命令类型和当前时间戳)
    public DeviceCommand createBaseCommand(int commandType) {
        DeviceCommand command = new DeviceCommand();
        command.setCommandType(commandType);
        command.setTimestamp(LocalDateTime.now());
        return command;
    }

    // 发送命令到设备
    private void sendCommand(DeviceCommand command,Integer stationId) {
        try {
            if (stationId!=-1){
            command.setStationId(stationId);
            }
            byte[] data = command.toBytes();
            DeviceSocket deviceSocket = socketManager.getDeviceSocket(stationId);
            if (deviceSocket.isConnected()){
                deviceSocket.sendData(data);
                log.info("发送命令成功: 类型={}, 数据长度={},数据={}", command.getCommandType(), data.length,data);
            }else{
                log.info("发送命令失败,设备未连接: 类型={}, 数据长度={},数据={}", command.getCommandType(), data.length,data);
            }
            //记录操作日志
            UavOperateLog uavOperateLog = new UavOperateLog();
            //设置操作时间
            uavOperateLog.setOperateTime(new Date());
            //设置命令参数
            uavOperateLog.setCmdParam(command.getCommandParam());
            //设置设备操作结果
            uavOperateLog.setResult("");
            //设置设备站id
            uavOperateLog.setStationId(stationId);
            //设置命令类型
            uavOperateLog.setCmdType(String.valueOf(command.getCommandType()));
            //设置命令名称
            uavOperateLog.setCmdName(CommandTypeEnum.getDescByCode(command.getCommandType()));
            uavOperateLogService.save(uavOperateLog);

        } catch (Exception e) {
            log.error("发送命令失败", e);
        }
    }

    // 自定义干扰参数内部类
    public static class DisturbSelfParam {
        private boolean enable;        // 是否开启
        private int channel;           // 频率通道
        private int workMode;          // 工作模式
        private long freq;             // 频率(Hz)
        private int bw;                // 扫频带宽(Hz)
        private int scanSpeed;         // 扫描速度
        private int scanPoint;         // 扫描点数
        private int att;               // 衰减

        // getters and setters
        public boolean isEnable() { return enable; }
        public void setEnable(boolean enable) { this.enable = enable; }
        public int getChannel() { return channel; }
        public void setChannel(int channel) { this.channel = channel; }
        public int getWorkMode() { return workMode; }
        public void setWorkMode(int workMode) { this.workMode = workMode; }
        public long getFreq() { return freq; }
        public void setFreq(long freq) { this.freq = freq; }
        public int getBw() { return bw; }
        public void setBw(int bw) { this.bw = bw; }
        public int getScanSpeed() { return scanSpeed; }
        public void setScanSpeed(int scanSpeed) { this.scanSpeed = scanSpeed; }
        public int getScanPoint() { return scanPoint; }
        public void setScanPoint(int scanPoint) { this.scanPoint = scanPoint; }
        public int getAtt() { return att; }
        public void setAtt(int att) { this.att = att; }
    }

}
