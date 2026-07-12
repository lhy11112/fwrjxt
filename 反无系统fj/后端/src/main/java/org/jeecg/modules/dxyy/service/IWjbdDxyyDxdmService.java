package org.jeecg.modules.dxyy.service;

import org.jeecg.modules.dxyy.entity.WjbdDxyyDxdm;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.wrj.entity.WjbdWrjZymb;

import java.util.List;

/**
 * @Description: 武警部队_典型应用_地形地貌
 * @Author: jeecg-boot
 * @Date:   2025-11-03
 * @Version: V1.0
 */
public interface IWjbdDxyyDxdmService extends IService<WjbdDxyyDxdm> {

    List<WjbdDxyyDxdm> getDxdmByJwdAndJl(String jd, String wd, String jl);
}
