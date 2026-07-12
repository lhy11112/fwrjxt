package org.jeecg.modules.wrj.service;

import org.jeecg.modules.wrj.dto.AreaQueryParam;
import org.jeecg.modules.wrj.entity.WjbdWrjZymb;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.wrj.entity.vo.WjbdWrjZymbVos;
import org.jeecg.modules.wrj.vo.TargetData;

import java.util.List;

/**
 * @Description: 武警部队_无人机_重要目标
 * @Author: jeecg-boot
 * @Date:   2025-11-03
 * @Version: V1.0
 */
public interface IWjbdWrjZymbService extends IService<WjbdWrjZymb> {
    /**
     * 根据经纬度、距离、类型查询目标范围内的目标信息
     * @param jd
     * @param wd
     * @param jl
     * @param typeList
     * @return
     */
    List<WjbdWrjZymb> getWrjZymbByJwdAndJl(String jd, String wd, String jl,List<String> typeList);

    /**
     * 根据经纬度、距离、类型查询目标范围内的目标信息及统计信息
     * @param jd
     * @param wd
     * @param jl
     * @param typeList
     * @return
     */
    WjbdWrjZymbVos getWrjZymbByJwdAndJls(String jd, String wd, String jl, List<String> typeList);

    /**
     * 根据前端传入的正方形区域查询目标数据
     * @param param 前端传入的四个角点参数
     * @return 区域内的目标数据列表
     */
    List<TargetData> queryBySquareArea(AreaQueryParam param);
}
