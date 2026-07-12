package org.jeecg.modules.wrj.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.wrj.entity.WjbdWrjJbxx;
import org.jeecg.modules.wrj.mapper.WjbdWrjJbxxMapper;
import org.jeecg.modules.wrj.service.IWjbdWrjJbxxService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;
import java.util.Map;

/**
 * @Description: 武警部队_无人机_基本信息
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Service
public class WjbdWrjJbxxServiceImpl extends ServiceImpl<WjbdWrjJbxxMapper, WjbdWrjJbxx> implements IWjbdWrjJbxxService {

    @Override
    public IPage<WjbdWrjJbxx> listByKyid(Page<WjbdWrjJbxx> page, String kyid) {
        return baseMapper.listByKyid(page,kyid);
    }

    @Override
    public List<Map<String, Object>> getWrjCount() {
        return baseMapper.getWrjCount();
    }
}
