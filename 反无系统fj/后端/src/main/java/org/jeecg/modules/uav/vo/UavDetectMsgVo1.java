package org.jeecg.modules.uav.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecg.modules.uav.entity.UavDetectMsg;
import org.jeecg.modules.wrj.entity.WjbdWrjJbxx;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
public class UavDetectMsgVo1 {
    /**
     * 飞行数据
     */
    private List<UavDetectMsg> uavDetectMsgList;
    /**
     * 无人机基本信息
     */
    private WjbdWrjJbxx wjbdWrjJbxx;
}
