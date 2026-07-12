package org.jeecg.modules.wrj.service;

import org.jeecg.modules.wrj.entity.WjbdWrjHbmdsq;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.wrj.vo.UpdateSqxxVo;
import org.jeecg.modules.wrj.vo.WjbdWrjJbxxVo;

import java.util.List;

/**
 * @Description: 武警部队_无人机_黑白名单授权
 * @Author: jeecg-boot
 * @Date:   2025-10-27
 * @Version: V1.0
 */
public interface IWjbdWrjHbmdsqService extends IService<WjbdWrjHbmdsq> {

    List<WjbdWrjJbxxVo> hmdList();

    List<WjbdWrjJbxxVo> bmdList();

    List<WjbdWrjJbxxVo> wsqList();

    void editSqxx(UpdateSqxxVo updateSqxxVo);
}
