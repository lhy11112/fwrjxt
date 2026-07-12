package org.jeecg.modules.uav.service.impl;

import org.jeecg.modules.uav.entity.UavDeviceConfig;
import org.jeecg.modules.uav.mapper.UavDeviceConfigMapper;
import org.jeecg.modules.uav.service.IUavDeviceConfigService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 设备管理
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Service
public class UavDeviceConfigServiceImpl extends ServiceImpl<UavDeviceConfigMapper, UavDeviceConfig> implements IUavDeviceConfigService {

    @Override
    public List<Map<String, Object>> getypsbCount() {
        return baseMapper.getypsbCount();
    }

    @Override
    public List<Map<String, Object>> getgrsbCount() {
        return baseMapper.getgrsbCount();
    }

    @Override
    public List<Map<String, Object>> getzcsbCount() {
        return baseMapper.getzcsbCount();
    }

    @Override
    public Map<String, Object> getSbCountByLx(String type) {
        Map<String,Object> rseultMap = new HashMap<>();
        //x轴
        String[] split = type.split(",");
        String[] titles = {"CONNECTED","DISCONNECTED","WARN"};
        //Y轴
        Map<String,Object> map = new HashMap<>();
        for(String title:titles){
            List<Integer> valueList = new ArrayList<>();
            for (String str :split){
                Integer value = baseMapper.getSbCountByLx(str,title);
                if(value==null){
                    value = 0;
                }
                valueList.add(value);
            }
            map.put(title,valueList);
        }
        rseultMap.put("xdata",split);
        rseultMap.put("ydata",map);

        return rseultMap;
    }
}
