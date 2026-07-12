package org.jeecg.modules.wrj.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.wrj.entity.WjbdWrjGjjl;
import org.jeecg.modules.wrj.entity.dto.WjbdWrjGjjlDto;
import org.jeecg.modules.wrj.mapper.WjbdWrjGjjlMapper;
import org.jeecg.modules.wrj.service.IWjbdWrjGjjlService;
import org.jeecg.modules.wrj.vo.GjtjVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @Description: 武警部队_无人机_告警记录
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Service
@Slf4j
@Component
public class WjbdWrjGjjlServiceImpl extends ServiceImpl<WjbdWrjGjjlMapper, WjbdWrjGjjl> implements IWjbdWrjGjjlService {


    @Override
    public List<Map<String, Object>> getGstjCount(GjtjVo gjtjVo) {
        List<Map<String,Object>> list = new ArrayList<>();
        switch (gjtjVo.getType()) {
            case "年":
                list = baseMapper.getGjtjCountByNf(gjtjVo);
                break;
            case "季":
                list = baseMapper.getGjtjCountByJd(gjtjVo);
                break;
            case "月":
                list = baseMapper.getGjtjCountByYf(gjtjVo);
                break;
            case "周":
                list = baseMapper.getGjtjCountByZs(gjtjVo);
                break;
            case "日":
                list = baseMapper.getGjtjCountByDay(gjtjVo);
                break;
            case "自定义":
                list = baseMapper.getGjtjCountByZdy(gjtjVo);
                break;
            default:
                break;
        }
        return list;
    }

    @Override
    public IPage<WjbdWrjGjjl> qstjEj(Page<WjbdWrjGjjl> page, GjtjVo gjtjVo) {
        return baseMapper.getQstjEj(page,gjtjVo);
    }

    @Override
    public List<WjbdWrjGjjl> GjQkTJ(WjbdWrjGjjlDto wjbdWrjGjjlDto) {
        return baseMapper.getGjQkTJ(wjbdWrjGjjlDto);
    }


}
