package org.jeecg.modules.wrj.service;

import org.jeecg.modules.wrj.entity.WjbdWrjBxpz;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Description: 武警部队_无人机_编携配装
 * @Author: jeecg-boot
 * @Date:   2025-12-16
 * @Version: V1.0
 */
public interface IWjbdWrjBxpzService extends IService<WjbdWrjBxpz> {

    List<WjbdWrjBxpz> getTreeByAssembly(String rootId);
}
