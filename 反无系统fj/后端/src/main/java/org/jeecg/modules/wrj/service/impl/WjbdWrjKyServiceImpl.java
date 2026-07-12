package org.jeecg.modules.wrj.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.wrj.entity.WjbdWrjKy;
import org.jeecg.modules.wrj.mapper.WjbdWrjKyMapper;
import org.jeecg.modules.wrj.service.IWjbdWrjKyService;
import org.jeecg.modules.wrj.vo.WjbdWrjKysqVo;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: 武警部队_无人机_空域
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Service
public class WjbdWrjKyServiceImpl extends ServiceImpl<WjbdWrjKyMapper, WjbdWrjKy> implements IWjbdWrjKyService {

    @Override
    public IPage<WjbdWrjKy> listAll(Page<WjbdWrjKy> page, WjbdWrjKy wjbdWrjKy) {
        return baseMapper.listAll(page,wjbdWrjKy);
    }

    @Override
    public IPage<WjbdWrjKysqVo> listAllByKyid(Page<WjbdWrjKysqVo> page, String kyid) {
        return baseMapper.listAllByKyid(page,kyid);
    }
}
