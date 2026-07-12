package org.jeecg.modules.wrj.service;

import org.jeecg.modules.wrj.entity.WjbdHsZskWjNr;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: 知识库_文件_内容
 * @Author: jeecg-boot
 * @Date:   2025-10-27
 * @Version: V1.0
 */
public interface IWjbdHsZskWjNrService extends IService<WjbdHsZskWjNr> {

    void edit(WjbdHsZskWjNr wjbdHsZskWjNr);
}
