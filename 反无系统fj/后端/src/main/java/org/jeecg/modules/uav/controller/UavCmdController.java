package org.jeecg.modules.uav.controller;

import io.swagger.annotations.ApiOperation;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.uav.dto.ControlDto;
import org.jeecg.modules.uav.dto.ServerControlDto;
import org.jeecg.modules.uav.dto.SpectrumParamDto;
import org.jeecg.modules.uav.dto.TypeDfFreqParamDto;
import org.jeecg.modules.uav.model.TcpUdpCommand;
import org.jeecg.modules.uav.service.DeviceCommandService;
import org.jeecg.modules.uav.service.HeartbeatService;
import org.jeecg.modules.uav.socket.DeviceSocket;
import org.jeecg.modules.uav.socket.SocketManager;
import org.jeecg.modules.uav.util.DroneTrajectoryGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/Device")
public class UavCmdController {
    @Autowired
    private SocketManager socketManager;
    @Autowired
    private DeviceCommandService deviceSocket;
    @Autowired
    private HeartbeatService heartbeatService;

    @GetMapping("/getSocketAll")
    public Map<Integer, DeviceSocket> getSocket() {
        Map<Integer, DeviceSocket> deviceSocket = socketManager.getAllDeviceSockets();
        return deviceSocket;
    }

    @GetMapping("/UDPSendSocketAll")
    public void UDPSendSocketAll(int a,@RequestBody ServerControlDto serverControlDto) throws Exception {
        //诱骗
        //13. 设置方位和速度（0x38）  azimuth 方位（°）  speed 速度（m/s）
        TcpUdpCommand.setAzimuthAndSpeed(100,250, serverControlDto.getStationId());
        //12. 设置GNSS诱骗模式（0x37）
        // 诱骗模式：0x0000（定向驱逐），0x0001（定点迫降），0x0002（禁飞），0x0003（导航压制）
        // 诱导方式：0x0000（驱离），0x0001（拉近）
        TcpUdpCommand.setGnssSpoofMode(1, 1, serverControlDto.getStationId());
        //15. 开启/关闭GNSS诱骗系统联动（0x41） enable true=开启，false=关闭
        TcpUdpCommand.setGnssSpoofLinkageEnable(true, serverControlDto.getStationId());
        //无线电干扰控制
        //10. 设置攻击频段（0x32）
        //5.8=>1    2.4=>2      <1G=>4    1.4=>8
        TcpUdpCommand.setAttackFrequencyBand(a, serverControlDto.getStationId());
        //9. 设置攻击模式（0x31） Param1=0x00（返航）/0x01（迫降）
        TcpUdpCommand.setAttackMode(1, serverControlDto.getStationId());
        //7. 开启/关闭目标探测功能（0x20）enable true=开启，false=关闭
        TcpUdpCommand.setTargetDetectionEnable(false, serverControlDto.getStationId());
        //8. 开启/关闭设备攻击功能（0x30）  enable true=开启，false=关闭
        TcpUdpCommand.setDeviceAttackEnable(false, serverControlDto.getStationId());
        //14. 开启/关闭无人值守功能（0x40） enable true=开启，false=关闭
        TcpUdpCommand.setUnattendedModeEnable(false, serverControlDto.getStationId());
    }

    /**
     * TCPSocket:设备操作接口
     *
     * @param controlDto
     */
    @ApiOperation(value="干扰诱骗操作日志-设备操作", notes="干扰诱骗操作日志-设备操作")
    @AutoLog(value = "干扰诱骗操作日志-设备操作",YYMK = "干扰诱骗模块")
    @PostMapping("/ControlDevice")
    public AjaxResult<?> ControlDevice(@RequestBody ControlDto controlDto) {
        switch (controlDto.getType()) {
            case "天线控制":
                if (controlDto.getControlTxDto() != null) {
                    deviceSocket.setDisturbAntenna(controlDto.getControlTxDto().getDirLoop(), controlDto.getControlTxDto().getDirection(), controlDto.getStationId(),controlDto);
                }
                break;
            case "自定义":
                    deviceSocket.setDisturbSelf(controlDto.getDisturbSelfParam(),  controlDto.getStationId(),controlDto);
                break;
            case "快速干扰":
                    deviceSocket.setDisturbQuick(controlDto.getEnable(), controlDto.getDisturbType(), controlDto.getStationId(),controlDto);
                break;
            case "诱骗模式":
                    deviceSocket.setTrapFun(controlDto.getEnable(), controlDto.getTrapType(), controlDto.getStationId(),controlDto);
                break;
            case "禁飞开关":
                    deviceSocket.setTrapPosition(controlDto.getEnable(),  controlDto.getStationId(),controlDto);
                break;
            case "衰减":
                    deviceSocket.setTrapAmp(controlDto.getAmp(),controlDto.getStationId(),controlDto);
            case "驱离功能控制":
                    deviceSocket.setTrapExpel(controlDto.getEnable(),controlDto.getStationId(),controlDto);
            case "指定诱骗":
                    deviceSocket.setTrapAssign(controlDto.getEnable(),controlDto.getUavLng(),controlDto.getUavLat(),controlDto.getTagLng(),controlDto.getTagLat(),controlDto.getStationId(),controlDto);
                break;
            default:
                break;
        }
        return AjaxResult.OK(true);
    }
    /**
     * TcpServerSocketServer:设备操作接口
     *
     * @param serverControlDto
     */
    @ApiOperation(value="干扰诱骗操作日志-设备操作", notes="干扰诱骗操作日志-设备操作")
    @AutoLog(value = "干扰诱骗操作日志-设备操作",YYMK = "干扰诱骗模块")
    @PostMapping("/ControlDeviceTcpServerSocketServer")
    public AjaxResult<?> ControlDeviceTcpServerSocketServer(@RequestBody ServerControlDto serverControlDto) throws Exception {

        //诱骗指令集
        //13. 设置方位和速度（0x38）  azimuth 方位（°）  speed 速度（m/s）
        TcpUdpCommand.setAzimuthAndSpeed(serverControlDto.getAzimuth(), serverControlDto.getSpeed(),serverControlDto.getStationId());
        //12. 设置GNSS诱骗模式（0x37）
        TcpUdpCommand.setGnssSpoofMode(serverControlDto.getGnssSpoofMode(), serverControlDto.getGnssInduceType(),serverControlDto.getStationId());

        //15. 开启/关闭GNSS诱骗系统联动（0x41） enable true=开启，false=关闭
        TcpUdpCommand.setGnssSpoofLinkageEnable(serverControlDto.isGnssSpoofLinkageEnable(),serverControlDto.getStationId());
        //无线电干扰控制指令集
        //10. 设置攻击频段（0x32） 5.8=>1    2.4=>2      <1G=>4    1.4=>8
        TcpUdpCommand.setAttackFrequencyBand(serverControlDto.getAttackFrequencyBand(),serverControlDto.getStationId());
        //9. 设置攻击模式（0x31） Param1=0x00（返航）/0x01（迫降）
        TcpUdpCommand.setAttackMode(serverControlDto.getAttackMode(),serverControlDto.getStationId());
        //7. 开启/关闭目标探测功能（0x20）enable true=开启，false=关闭
        TcpUdpCommand.setTargetDetectionEnable(serverControlDto.isTargetDetectionEnable(),serverControlDto.getStationId());
        //8. 开启/关闭设备攻击功能（0x30）  enable true=开启，false=关闭
        TcpUdpCommand.setDeviceAttackEnable(serverControlDto.isDeviceAttackEnable(),serverControlDto.getStationId());
        //14. 开启/关闭无人值守功能（0x40） enable true=开启，false=关闭
        TcpUdpCommand.setUnattendedModeEnable(serverControlDto.isUnattendedModeEnable(),serverControlDto.getStationId());
        return AjaxResult.OK(true);
    }

    /**
     * 侦测设备频谱监测操作接口
     * @param spectrumParamDto
     * @return
     */
    @ApiOperation(value="侦测设备频谱监测操作接口-设备操作", notes="侦测设备频谱监测操作接口-设备操作")
    @AutoLog(value = "侦测设备频谱监测操作接口-设备操作",YYMK = "干扰诱骗模块")
    @PostMapping("/ControlDeviceSpectrumParam")
    public AjaxResult<?> ControlDeviceSpectrumParam(@RequestBody SpectrumParamDto spectrumParamDto) {
        deviceSocket.setTypeSpectrumParam(spectrumParamDto.getEnable(),spectrumParamDto.getBeginFreq(),spectrumParamDto.getEndFreq(),spectrumParamDto.getStep(),spectrumParamDto.getStationId());
        return AjaxResult.OK(true);
    }

    /**
     * 手动设置侧向频率
     * @param typeDfFreqParamDto
     * @return
     */
    @ApiOperation(value="手动设置侧向频率-设备操作", notes="手动设置侧向频率-设备操作")
    @AutoLog(value = "手动设置侧向频率-设备操作",YYMK = "手动设置侧向频率")
    @PostMapping("/ControlTypeDfFreqParam")
    public AjaxResult<?> ControlTypeDfFreqParam(@RequestBody TypeDfFreqParamDto typeDfFreqParamDto) {
        deviceSocket.setTypeDfFreqParam(typeDfFreqParamDto.getEnable(),typeDfFreqParamDto.getDirType(),typeDfFreqParamDto.getFreq(),typeDfFreqParamDto.getBw(),typeDfFreqParamDto.getId(),typeDfFreqParamDto.getModel(),typeDfFreqParamDto.getStationId());
        return AjaxResult.OK(true);
    }
    /**
     * 开始工作接口
     *
     * @param
     */
    @GetMapping("/ControlDevices")
    public Object ControlDevices() {
        return socketManager.getAllDeviceSockets().get(101).getInputStream();
    }

    @GetMapping("/sclj")
    public List<DroneTrajectoryGenerator.TrajectoryPoint> sclj() {
        // 创建航点
        DroneTrajectoryGenerator.Waypoint start = new DroneTrajectoryGenerator.Waypoint(119.3062, 26.0745, 0);
        DroneTrajectoryGenerator.Waypoint waypoint1 = new DroneTrajectoryGenerator.Waypoint(119.0098, 25.4480, 150, 5); // 停留5秒
        DroneTrajectoryGenerator.Waypoint waypoint2 = new DroneTrajectoryGenerator.Waypoint(118.5895, 24.9088, 200, 5); // 停留5秒
        DroneTrajectoryGenerator.Waypoint end = new DroneTrajectoryGenerator.Waypoint(118.0894, 24.4798, 0);

        List<DroneTrajectoryGenerator.Waypoint> waypoints = new ArrayList<>();
        waypoints.add(waypoint1);
        waypoints.add(waypoint2);

        // 无人机参数：最大速度10m/s，加速度2m/s²，转弯半径20m
        DroneTrajectoryGenerator.DroneParameters params = new DroneTrajectoryGenerator.DroneParameters(10, 2, 20);

        // 设置开始和结束时间（示例：从现在开始，10分钟后结束）
        Date startTime = new Date();
        Date endTime = new Date(startTime.getTime() + 50 * 60 * 1000); // 10分钟后

        System.out.println("轨迹开始时间: " + startTime);
        System.out.println("轨迹结束时间: " + endTime);

        // 生成轨迹
        List<DroneTrajectoryGenerator.TrajectoryPoint> trajectory = DroneTrajectoryGenerator.generateTrajectory(
                start, waypoints, end, startTime, endTime, params);


        return trajectory;
    }
}
