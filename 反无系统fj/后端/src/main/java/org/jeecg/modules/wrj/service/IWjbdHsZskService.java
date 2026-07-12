package org.jeecg.modules.wrj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.modules.wrj.entity.WjbdHsZsk;

import java.util.List;

/**
 * @Description: wjbd_wrj_zsk
 * @Author: jeecg-boot
 * @Date: 2024-09-14
 */
public interface IWjbdHsZskService extends IService<WjbdHsZsk> {
    

    /**
     * 添加
     *
     * @param wjbdHsZsk wjbdHsZsk
     * @return 结果
     */
    AjaxResult<?> add(WjbdHsZsk wjbdHsZsk);

    /**
     * 修改
     *
     * @param wjbdHsZsk wjbdHsZsk
     * @return 结果
     */
    AjaxResult<?> editZskMc(WjbdHsZsk wjbdHsZsk);

    /**
     * 删除
     *
     * @param ids ids
     * @return 结果
     */
    AjaxResult<?> deleteBatch(String ids);

    /**
     * 删除
     *
     * @param ids ids
     * @return 结果
     */
    Long createZsk(String zskMc,String zskNm);
}
