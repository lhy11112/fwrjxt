package org.jeecg.modules.wrj.service;

import org.jeecg.modules.wrj.entity.WjbdDxyyBh;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.wrj.entity.dto.WjbdDxyyBhDto;

import java.util.List;

/**
 * @Description: 武警不对_典型应用_无人机
 * @Author: jeecg-boot
 * @Date:   2026-01-19
 * @Version: V1.0
 */
public interface IWjbdDxyyBhService extends IService<WjbdDxyyBh> {
    /**
     * 根据业务id、时间查询标绘信息
     * @param wjbdDxyyBhDto
     * @return
     */
    List<WjbdDxyyBh> getBhByYwIdDataTime(WjbdDxyyBhDto wjbdDxyyBhDto);
}
