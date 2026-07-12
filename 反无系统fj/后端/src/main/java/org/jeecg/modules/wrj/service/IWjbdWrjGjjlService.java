package org.jeecg.modules.wrj.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.wrj.entity.WjbdWrjGjjl;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.wrj.entity.dto.WjbdWrjGjjlDto;
import org.jeecg.modules.wrj.vo.GjtjVo;

import java.util.List;
import java.util.Map;

/**
 * @Description: 武警部队_无人机_告警记录
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
public interface IWjbdWrjGjjlService extends IService<WjbdWrjGjjl> {

    List<Map<String, Object>> getGstjCount(GjtjVo gjtjVo);

    IPage<WjbdWrjGjjl> qstjEj(Page<WjbdWrjGjjl> page, GjtjVo gjtjVo);

    List<WjbdWrjGjjl> GjQkTJ(WjbdWrjGjjlDto wjbdWrjGjjlDto);
}
