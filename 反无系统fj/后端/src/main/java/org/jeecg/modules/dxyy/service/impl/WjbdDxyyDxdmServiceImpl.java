package org.jeecg.modules.dxyy.service.impl;

import org.jeecg.modules.dxyy.entity.WjbdDxyyDxdm;
import org.jeecg.modules.dxyy.mapper.WjbdDxyyDxdmMapper;
import org.jeecg.modules.dxyy.service.IWjbdDxyyDxdmService;
import org.jeecg.modules.wrj.entity.WjbdWrjZymb;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 武警部队_典型应用_地形地貌
 * @Author: jeecg-boot
 * @Date:   2025-11-03
 * @Version: V1.0
 */
@Service
public class WjbdDxyyDxdmServiceImpl extends ServiceImpl<WjbdDxyyDxdmMapper, WjbdDxyyDxdm> implements IWjbdDxyyDxdmService {

    @Override
    public List<WjbdDxyyDxdm> getDxdmByJwdAndJl(String jd, String wd, String jl) {
        return baseMapper.getDxdmByJwdAndJl(jd,wd,jl);
    }
}
