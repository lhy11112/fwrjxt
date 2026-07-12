package org.jeecg.modules.dxyy.entity.dto.param;

import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;

/**
 *
 * @Description:
 * @Author: 李海洋
 * @Date:   2024-07-31
 *
 */
public class QueryUnitRelationshipParam {

    @NotNull(message = "查询内容队列不能为空")
    @Getter
    private ArrayList dwidlist;
}
