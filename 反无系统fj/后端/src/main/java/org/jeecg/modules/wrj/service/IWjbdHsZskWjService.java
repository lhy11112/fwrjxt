package org.jeecg.modules.wrj.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.modules.wrj.entity.WjbdHsZskWj;

import java.util.List;

/**
 * @Description: wjbd_wrj_zsk_wj
 * @Author: jeecg-boot
 * @Date:   2024-09-14
 */
public interface IWjbdHsZskWjService extends IService<WjbdHsZskWj> {

    /**
     * 添加
     *
     * @param wjbdHsZskWjList wjbdHsZskWjList
     * @return 结果
     */
    AjaxResult<?> add(List<WjbdHsZskWj> wjbdHsZskWjList);

    /**
     * 通过id删除
     *
     * @param id id
     * @return 结果
     */
    AjaxResult<?> delInfo(String id);
}
