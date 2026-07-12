package org.jeecg.modules.uav.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ServerControlDto {
    @ApiModelProperty(value = "方位角(°)，对应指令0x38的方位参数", required = true, example = "100")
    private float azimuth;
    @ApiModelProperty(value = "速度(m/s)，对应指令0x38的速度参数", required = true, example = "250")
    private float speed;
    @ApiModelProperty(value = "GNSS诱骗模式 0x37，取值：0=定向驱逐，1=定点迫降，2=禁飞，3=导航压制", required = true, example = "1")
    private int gnssSpoofMode;
    @ApiModelProperty(value = "GNSS诱导方式 0x37，取值：0=驱离，1=拉近", required = true, example = "1")
    private int gnssInduceType;

    @ApiModelProperty(value = "是否开启GNSS诱骗系统联动 0x41，true=开启，false=关闭", required = true, example = "true")
    private boolean gnssSpoofLinkageEnable;

    @ApiModelProperty(value = "攻击频段 0x32，必填核心参数 取值：1=5.8G，2=2.4G，4=<1G，8=1.4G", required = true, example = "1")
    private int attackFrequencyBand;

    @ApiModelProperty(value = "攻击模式 0x31，取值：0=返航，1=迫降", required = true, example = "1")
    private int attackMode;

    @ApiModelProperty(value = "是否开启目标探测功能 0x20，true=开启，false=关闭", required = true, example = "false")
    private boolean targetDetectionEnable;

    @ApiModelProperty(value = "是否开启设备攻击功能 0x30，true=开启，false=关闭", required = true, example = "false")
    private boolean deviceAttackEnable;

    @ApiModelProperty(value = "是否开启无人值守功能 0x40，true=开启，false=关闭", required = true, example = "false")
    private boolean unattendedModeEnable;
    /**
     * 站id
     */
    private  Integer stationId;
}
