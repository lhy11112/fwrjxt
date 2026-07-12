package org.jeecg.modules.wrj.service.impl;

import org.jeecg.modules.wrj.entity.WjbdDxyyBh;
import org.jeecg.modules.wrj.entity.dto.WjbdDxyyBhDto;
import org.jeecg.modules.wrj.mapper.WjbdDxyyBhMapper;
import org.jeecg.modules.wrj.service.IWjbdDxyyBhService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 武警不对_典型应用_无人机
 * @Author: jeecg-boot
 * @Date:   2026-01-19
 * @Version: V1.0
 */
@Service
public class WjbdDxyyBhServiceImpl extends ServiceImpl<WjbdDxyyBhMapper, WjbdDxyyBh> implements IWjbdDxyyBhService {
    /**
     * 根据业务id、时间查询标绘信息
     * @param wjbdDxyyBhDto
     * @return
     */
    @Override
    public List<WjbdDxyyBh> getBhByYwIdDataTime(WjbdDxyyBhDto wjbdDxyyBhDto) {
        return baseMapper.getBhByYwIdDataTime(wjbdDxyyBhDto.getYwid(),wjbdDxyyBhDto.getDatatime());
    }
}
