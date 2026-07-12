package org.jeecg.modules.wrj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.wrj.entity.WjbdWrjScj;
import org.jeecg.modules.wrj.entity.vo.WjbdWrjScjTreeVO;
import org.jeecg.modules.wrj.mapper.WjbdWrjScjMapper;
import org.jeecg.modules.wrj.service.IWjbdWrjScjService;
import org.jeecg.modules.wrj.utils.TreeUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.Collections;
import java.util.List;

/**
 * @Description: 武警部队_无人机_收藏夹
 * @Author: jeecg-boot
 * @Date: 2026-02-04
 * @Version: V1.0
 */
@Service
public class WjbdWrjScjServiceImpl extends ServiceImpl<WjbdWrjScjMapper, WjbdWrjScj> implements IWjbdWrjScjService {

    @Override
    public List<WjbdWrjScjTreeVO> getTreeByYhId(String yhId) {
        QueryWrapper<WjbdWrjScj> queryWrapper = new QueryWrapper<>();
        //当前用户
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (user != null) {
            queryWrapper.eq("YH_ID", user.getId());
        } else {
            if (StringUtils.isNotBlank(yhId)) {
                queryWrapper.eq("YH_ID", yhId);
            }
        }
        // 1. 查询用户所有数据
        List<WjbdWrjScj> scjList = baseMapper.selectList(queryWrapper);
        // 2. 构建树形结构
        return TreeUtils.buildTree(scjList);
    }
}
