package org.jeecg.modules.wrj.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.wrj.entity.WjbdWrjJbxx;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @Description: 武警部队_无人机_基本信息
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
public interface IWjbdWrjJbxxService extends IService<WjbdWrjJbxx> {

    IPage<WjbdWrjJbxx> listByKyid(Page<WjbdWrjJbxx> page, String kyid);

    List<Map<String, Object>> getWrjCount();
}
