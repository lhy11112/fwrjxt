package org.jeecg.modules.uav.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ModelSerialDTO {
    private String modelClean;
    private String serialClean;
}
