package org.jeecg.modules.wrj.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.wrj.entity.WjbdWrjGjjl;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.wrj.entity.dto.WjbdWrjGjjlDto;
import org.jeecg.modules.wrj.vo.GjtjVo;

/**
 * @Description: 武警部队_无人机_告警记录
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
public interface WjbdWrjGjjlMapper extends BaseMapper<WjbdWrjGjjl> {

    List<Map<String, Object>> getGjtjCountByNf(@Param("gjtjVo") GjtjVo gjtjVo);

    List<Map<String, Object>> getGjtjCountByJd(@Param("gjtjVo")GjtjVo gjtjVo);

    List<Map<String, Object>> getGjtjCountByYf(@Param("gjtjVo")GjtjVo gjtjVo);

    List<Map<String, Object>> getGjtjCountByZs(@Param("gjtjVo")GjtjVo gjtjVo);

    List<Map<String, Object>> getGjtjCountByDay(@Param("gjtjVo")GjtjVo gjtjVo);

    IPage<WjbdWrjGjjl> getQstjEj(Page<WjbdWrjGjjl> page, @Param("gjtjVo")GjtjVo gjtjVo);

    List<Map<String, Object>> getGjtjCountByZdy(@Param("gjtjVo")GjtjVo gjtjVo);

    List<WjbdWrjGjjl> getGjQkTJ(@Param("wjbdWrjGjjlDto") WjbdWrjGjjlDto wjbdWrjGjjlDto);
}
