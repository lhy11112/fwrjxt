package org.jeecg.modules.dxyy.service;

import org.jeecg.modules.dxyy.entity.WjbdWrjYhglCs;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 武警部队_无人机_用户管理_参数
 * @Author: jeecg-boot
 * @Date:   2025-09-01
 * @Version: V1.0
 */
public interface IWjbdWrjYhglCsService extends IService<WjbdWrjYhglCs> {

    List<WjbdWrjYhglCs> updateJwdDmNm();
}
