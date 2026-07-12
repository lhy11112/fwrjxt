package org.jeecg.modules.wrj.service;

import org.jeecg.modules.uav.util.UavDatectMsgDto1;
import org.jeecg.modules.wrj.entity.WjbdWrjTyjh;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 武警部队_无人机_推演计划
 * @Author: jeecg-boot
 * @Date:   2025-09-22
 * @Version: V1.0
 */
public interface IWjbdWrjTyjhService extends IService<WjbdWrjTyjh> {

    void TYJHSC(WjbdWrjTyjh wjbdWrjTyjh);
}
