package org.jeecg.modules.wrj.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.wrj.entity.WjbdWrjBxpz;
import org.jeecg.modules.wrj.mapper.WjbdWrjBxpzMapper;
import org.jeecg.modules.wrj.service.IWjbdWrjBxpzService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 武警部队_无人机_编携配装
 * @Author: jeecg-boot
 * @Date:   2025-12-16
 * @Version: V1.0
 */
@Service
public class WjbdWrjBxpzServiceImpl extends ServiceImpl<WjbdWrjBxpzMapper, WjbdWrjBxpz> implements IWjbdWrjBxpzService {

    @Override
    public List<WjbdWrjBxpz> getTreeByAssembly(String rootId) {
        // 1. 查询所有节点
        List<WjbdWrjBxpz> allNodes = baseMapper.selectList(null);

        // 2. 按pid分组，便于快速查找子节点
        Map<String, List<WjbdWrjBxpz>> pidToChildren = allNodes.stream()
                .collect(Collectors.groupingBy(WjbdWrjBxpz::getPid));

        // 3. 递归组装子节点
        allNodes.forEach(node -> node.setChildren(pidToChildren.getOrDefault(node.getId(), new ArrayList<>())));

        // 4. 筛选根节点（pid=rootId 或 rootId=null时筛选pid为空的节点）
        if (rootId == null || rootId.isEmpty()) {
            return allNodes.stream()
                    .filter(node -> node.getPid() == null || node.getPid().isEmpty())
                    .collect(Collectors.toList());
        } else {
            return allNodes.stream()
                    .filter(node -> rootId.equals(node.getPid()))
                    .collect(Collectors.toList());
        }
    }
}
