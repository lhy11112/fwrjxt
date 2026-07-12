package org.jeecg.modules.wrj.service.impl;

import org.jeecg.modules.wrj.dto.AreaQueryParam;
import org.jeecg.modules.wrj.entity.WjbdWrjZymb;
import org.jeecg.modules.wrj.entity.vo.WjbdWrjZymbVos;
import org.jeecg.modules.wrj.mapper.WjbdWrjZymbMapper;
import org.jeecg.modules.wrj.service.IWjbdWrjZymbService;
import org.jeecg.modules.wrj.vo.TargetData;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * @Description: 武警部队_无人机_重要目标
 * @Author: jeecg-boot
 * @Date:   2025-11-03
 * @Version: V1.0
 */
@Service
public class WjbdWrjZymbServiceImpl extends ServiceImpl<WjbdWrjZymbMapper, WjbdWrjZymb> implements IWjbdWrjZymbService {

    @Override
    public List<WjbdWrjZymb> getWrjZymbByJwdAndJl(String jd, String wd, String jl,List<String> typeList) {
        return baseMapper.getWrjZymbByJwdAndJl(jd,wd,jl, typeList);
    }

    @Override
    public WjbdWrjZymbVos getWrjZymbByJwdAndJls(String jd, String wd, String jl, List<String> typeList) {
        WjbdWrjZymbVos wjbdWrjZymbVos=new WjbdWrjZymbVos();
        wjbdWrjZymbVos.setMbtjxx(baseMapper.getWrjZymbByJwdAndJlGroupBySubType(jd,wd,jl, typeList));
        wjbdWrjZymbVos.setZymbxx(baseMapper.getWrjZymbByJwdAndJl(jd,wd,jl, typeList));
        return wjbdWrjZymbVos;
    }
    @Override
    public List<TargetData> queryBySquareArea(AreaQueryParam param) {
        // 1. 解析四个角点的经纬度（注意：前端参数格式是 [纬度, 经度]）
        BigDecimal nwLat = param.getNorthWest()[0]; // 西北点纬度
        BigDecimal nwLng = param.getNorthWest()[1]; // 西北点经度
        BigDecimal neLat = param.getNorthEast()[0]; // 东北点纬度
        BigDecimal neLng = param.getNorthEast()[1]; // 东北点经度
        BigDecimal seLat = param.getSouthEast()[0]; // 东南点纬度
        BigDecimal seLng = param.getSouthEast()[1]; // 东南点经度
        BigDecimal swLat = param.getSouthWest()[0]; // 西南点纬度
        BigDecimal swLng = param.getSouthWest()[1]; // 西南点经度

        // 2. 计算经纬度极值（核心：筛选正方形的最小/最大经纬度）
        BigDecimal minJd = min(nwLng, neLng, seLng, swLng); // 最小经度
        BigDecimal maxJd = max(nwLng, neLng, seLng, swLng); // 最大经度
        BigDecimal minWd = min(nwLat, neLat, seLat, swLat); // 最小纬度
        BigDecimal maxWd = max(nwLat, neLat, seLat, swLat); // 最大纬度

        // 3. 调用Mapper查询区域内数据
        return baseMapper.selectBySquareArea(minJd, maxJd, minWd, maxWd);
    }

    // 工具方法：获取多个BigDecimal的最小值
    private BigDecimal min(BigDecimal... nums) {
        BigDecimal min = nums[0];
        for (BigDecimal num : nums) {
            if (num.compareTo(min) < 0) {
                min = num;
            }
        }
        return min;
    }

    // 工具方法：获取多个BigDecimal的最大值
    private BigDecimal max(BigDecimal... nums) {
        BigDecimal max = nums[0];
        for (BigDecimal num : nums) {
            if (num.compareTo(max) > 0) {
                max = num;
            }
        }
        return max;
    }
}
