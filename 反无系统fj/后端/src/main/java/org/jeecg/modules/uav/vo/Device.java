package org.jeecg.modules.uav.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Device {
    @JsonProperty("SerialNo")
    private String serialNo; // 序列号

    @JsonProperty("DeviceKind")
    private int deviceKind; // 设备类型：0-主动雷达，1-无线电侦测，2-光电跟踪，3-红外，4-干扰，6-诱骗

    @JsonProperty("DeviceType")
    private String deviceType; // 设备型号（预留）

    @JsonProperty("CommunicateMODE")
    private int communicateMode; // 通讯模式：1-Socket，2-串口，3-相机，4-WebAPI

    @JsonProperty("DeviceIP")
    private String deviceIp; // 设备IP（通讯模式1/3）

    @JsonProperty("DevicePort")
    private int devicePort; // 设备端口（通讯模式1/3）

    @JsonProperty("SerialPort")
    private String serialPort; // 串口号（通讯模式2）

    @JsonProperty("BaudRate")
    private String baudRate; // 波特率（通讯模式2）

    @JsonProperty("HostAddress")
    private String hostAddress; // HOST地址（通讯模式4）

    @JsonProperty("RtspUrl")
    private String rtspUrl; // RTSP拉流地址

    @JsonProperty("VersionNo")
    private String versionNo; // 版本号（预留）

    @JsonProperty("DeviceId")
    private long deviceId; // 设备唯一ID

    @JsonProperty("DeviceLng")
    private double deviceLng; // 设备经度

    @JsonProperty("DeviceLat")
    private double deviceLat; // 设备纬度

    @JsonProperty("DeviceAlt")
    private float deviceAlt; // 设备海拔

    @JsonProperty("ExistPTZ")
    private boolean existPTZ; // 是否有云台

    @JsonProperty("PanAngle")
    private float panAngle; // 水平方位（相对于真北）

    @JsonProperty("TiltAngle")
    private float tiltAngle; // 俯仰方位

    @JsonProperty("DeviceStatus")
    private boolean deviceStatus; // 设备状态：true-在线，false-离线

    @JsonProperty("InstallAddress")
    private String installAddress; // 安装地址

    @JsonProperty("SensorStatus")
    private int sensorStatus; // 传感器状态：0-未知，1-停止，2-已展开，3-已收拢，4-急停，5-展开中，6-收拢中
}
