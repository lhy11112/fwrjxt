package org.jeecg.modules.wrj.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.wrj.entity.WjbdWrjHbmdsq;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.wrj.vo.WjbdWrjJbxxVo;

/**
 * @Description: 武警部队_无人机_黑白名单授权
 * @Author: jeecg-boot
 * @Date:   2025-10-27
 * @Version: V1.0
 */
public interface WjbdWrjHbmdsqMapper extends BaseMapper<WjbdWrjHbmdsq> {

    List<WjbdWrjJbxxVo> getHmdList();

    List<WjbdWrjJbxxVo> getBmdList();

    List<WjbdWrjJbxxVo> getWsqList();

}
