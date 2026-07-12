package org.jeecg.modules.wrj.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.wrj.entity.WjbdWrjKy;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.wrj.vo.WjbdWrjKysqVo;

/**
 * @Description: 武警部队_无人机_空域
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
public interface WjbdWrjKyMapper extends BaseMapper<WjbdWrjKy> {

    IPage<WjbdWrjKy> listAll(Page<WjbdWrjKy> page, @Param("wjbdWrjKy") WjbdWrjKy wjbdWrjKy);

    IPage<WjbdWrjKysqVo> listAllByKyid(Page<WjbdWrjKysqVo> page, @Param("kyid")String kyid);
}
