package org.jeecg.modules.uav.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.modules.uav.entity.UavDetectMsg;
import org.jeecg.modules.uav.entity.UavDfData;
import org.jeecgframework.poi.excel.annotation.Excel;

@Data
public class UavDetectMsgVo {
    /**
     * 型号
     */
    private String model;
    /**
     * 序列号
     */
    private String serial;
    /**
     * 品牌
     */
    private String brand;
    /**
     * 1-正常、2-告警、3-失联
     */
    @ApiModelProperty(value = "1-正常、2-告警、3-失联 4-状态未知")
    private Integer status;
    /**
     * 授权状态：1-白名单（授权）、2-黑名单（禁止）、3-未授权 、4-未知
     */
    @ApiModelProperty(value = "授权状态：1-白名单（授权）、2-黑名单（禁止）、3-未授权、4-未知")
    private Integer authStatus;
    /**
     * 站id
     */
    private Integer stationId;
    /**
     * 站名称
     */
    private String stationName;
    /**
     * 威胁等级：red、green、yellow
     */
    private String wxdj;
    /**
     * 频谱测向
     * 报文协议解密
     * Remote协议解密
     * 解密协议
     */
    private String jmlx;
    /**
     * 侦测报文数据-无人机最新一条数据信息
     */
    private UavDetectMsg uavDetectMsg;
    /**
     * 无人机解析数据表-无人机最新一条数据信息
     */
    private UavDfData uavDfData;
}
