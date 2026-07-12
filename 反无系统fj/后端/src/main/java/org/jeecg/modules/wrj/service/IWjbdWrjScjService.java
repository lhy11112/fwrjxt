package org.jeecg.modules.wrj.service;

import org.jeecg.modules.wrj.entity.WjbdWrjScj;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.wrj.entity.vo.WjbdWrjScjTreeVO;

import java.util.List;

/**
 * @Description: 武警部队_无人机_收藏夹
 * @Author: jeecg-boot
 * @Date:   2026-02-04
 * @Version: V1.0
 */
public interface IWjbdWrjScjService extends IService<WjbdWrjScj> {
    /**
     * 查询用户收藏夹树形结构
     */
    List<WjbdWrjScjTreeVO> getTreeByYhId(String yhId);
}
