package org.jeecg.modules.wrj.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.wrj.entity.WjbdWrjKy;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.wrj.vo.WjbdWrjKysqVo;

/**
 * @Description: 武警部队_无人机_空域
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
public interface IWjbdWrjKyService extends IService<WjbdWrjKy> {

    IPage<WjbdWrjKy> listAll(Page<WjbdWrjKy> page, WjbdWrjKy wjbdWrjKy);

    IPage<WjbdWrjKysqVo> listAllByKyid(Page<WjbdWrjKysqVo> page, String kyid);
}
