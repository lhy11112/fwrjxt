package org.jeecg.modules.uav.vo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * 设备列表响应顶层对象（对应JSON根节点）
 */
@Data
public class DeviceListResponse {
    @JsonProperty("StationCode")
    private String stationCode; // 终端代码

    @JsonProperty("StationName")
    private String stationName; // 终端名称

    @JsonProperty("TotalCount")
    private int totalCount; // 设备数量

    @JsonProperty("DeviceList")
    private List<Device> deviceList; // 设备集合
}

