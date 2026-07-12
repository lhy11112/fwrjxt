package org.jeecg.modules.wrj.utils;

import org.jeecg.modules.wrj.entity.WjbdWrjScj;
import org.jeecg.modules.wrj.entity.vo.WjbdWrjScjTreeVO;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TreeUtils {
    /**
     * 构建收藏夹树形结构
     */
    public static List<WjbdWrjScjTreeVO> buildTree(List<WjbdWrjScj> scjList) {
        // 1. 转换为VO对象
        List<WjbdWrjScjTreeVO> voList = new ArrayList<>();
        for (WjbdWrjScj scj : scjList) {
            WjbdWrjScjTreeVO vo = new WjbdWrjScjTreeVO();
            BeanUtils.copyProperties(scj, vo);
            voList.add(vo);
        }

        // 2. 查找根节点（PID为null或0的节点）
        List<WjbdWrjScjTreeVO> rootList = voList.stream()
                .filter(vo -> vo.getPid() == null || vo.getPid() == 0)
                .collect(Collectors.toList());

        // 3. 递归设置子节点
        for (WjbdWrjScjTreeVO root : rootList) {
            setChildren(root, voList);
        }

        return rootList;
    }

    /**
     * 递归设置子节点
     */
    private static void setChildren(WjbdWrjScjTreeVO parent, List<WjbdWrjScjTreeVO> voList) {
        List<WjbdWrjScjTreeVO> children = voList.stream()
                .filter(vo -> parent.getId().equals(vo.getPid()))
                .collect(Collectors.toList());

        if (!children.isEmpty()) {
            parent.setChildren(children);
            for (WjbdWrjScjTreeVO child : children) {
                setChildren(child, voList);
            }
        }
    }
}
