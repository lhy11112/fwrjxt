package org.jeecg.modules.wrj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.jeecg.modules.wrj.entity.WjbdWrjHbmdsq;
import org.jeecg.modules.wrj.entity.WjbdWrjJbxx;
import org.jeecg.modules.wrj.mapper.WjbdWrjHbmdsqMapper;
import org.jeecg.modules.wrj.service.IWjbdWrjHbmdsqService;
import org.jeecg.modules.wrj.service.IWjbdWrjJbxxService;
import org.jeecg.modules.wrj.vo.UpdateSqxxVo;
import org.jeecg.modules.wrj.vo.WjbdWrjJbxxVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.List;

/**
 * @Description: 武警部队_无人机_黑白名单授权
 * @Author: jeecg-boot
 * @Date:   2025-10-27
 * @Version: V1.0
 */
@Service
public class WjbdWrjHbmdsqServiceImpl extends ServiceImpl<WjbdWrjHbmdsqMapper, WjbdWrjHbmdsq> implements IWjbdWrjHbmdsqService {
    
    private static final Logger log = LoggerFactory.getLogger(WjbdWrjHbmdsqServiceImpl.class);
    
    @Autowired
    private IWjbdWrjJbxxService wjbdWrjJbxxService;

    @Override
    public List<WjbdWrjJbxxVo> hmdList() {
        return baseMapper.getHmdList();
    }

    @Override
    public List<WjbdWrjJbxxVo> bmdList() {
        return baseMapper.getBmdList();
    }

    @Override
    public List<WjbdWrjJbxxVo> wsqList() {
        return baseMapper.getWsqList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editSqxx(UpdateSqxxVo updateSqxxVo){
        // 参数验证
        if (updateSqxxVo == null) {
            throw new IllegalArgumentException("更新参数不能为空");
        }
        
        if (CollectionUtils.isEmpty(updateSqxxVo.getIds())) {
            throw new IllegalArgumentException("黑白名单ID列表不能为空");
        }
        
        if (CollectionUtils.isEmpty(updateSqxxVo.getWrjids())) {
            throw new IllegalArgumentException("无人机ID列表不能为空");
        }
        
        if (StringUtils.isBlank(updateSqxxVo.getMdlx())) {
            throw new IllegalArgumentException("名单类型不能为空");
        }
        
        // 验证名单类型值有效性
        if (!"白名单".equals(updateSqxxVo.getMdlx()) && !"黑名单".equals(updateSqxxVo.getMdlx())) {
            throw new IllegalArgumentException("无效的名单类型: " + updateSqxxVo.getMdlx());
        }

        // 更新黑白名单表
        LambdaUpdateWrapper<WjbdWrjHbmdsq> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(WjbdWrjHbmdsq::getId, updateSqxxVo.getIds());
        updateWrapper.set(WjbdWrjHbmdsq::getMdlx, updateSqxxVo.getMdlx());
        boolean hbmdUpdated = this.update(updateWrapper);
        
        if (!hbmdUpdated) {
            log.warn("黑白名单表更新失败，IDs: {}", updateSqxxVo.getIds().toString());
        }

        // 根据名单类型设置授权状态
        Integer authStatus = "白名单".equals(updateSqxxVo.getMdlx()) ? 1 : 2;
        
        // 更新无人机基本信息表
        LambdaUpdateWrapper<WjbdWrjJbxx> jbxxUpdateWrapper = new LambdaUpdateWrapper<>();
        jbxxUpdateWrapper.in(WjbdWrjJbxx::getId, updateSqxxVo.getWrjids());
        jbxxUpdateWrapper.set(WjbdWrjJbxx::getAuthStatus, authStatus);
        boolean jbxxUpdated = wjbdWrjJbxxService.update(jbxxUpdateWrapper);
        
        if (!jbxxUpdated) {
            log.warn("无人机基本信息表更新失败，IDs: {}", updateSqxxVo.getWrjids().toString());
        }
        
        log.info("黑白名单更新完成 - 类型: {}, 黑白名单记录数: {}, 无人机记录数: {}", 
                updateSqxxVo.getMdlx(), updateSqxxVo.getIds().size(), updateSqxxVo.getWrjids().size());
    }
}
