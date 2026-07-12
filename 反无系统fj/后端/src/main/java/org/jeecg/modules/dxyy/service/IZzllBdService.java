package org.jeecg.modules.dxyy.service;

import org.apache.ibatis.annotations.Param;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.modules.dxyy.entity.Vo.ZzllBdRyVo;
import org.jeecg.modules.dxyy.entity.ZzllBd;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Description: zzll_bd
 * @Author: jeecg-boot
 * @Date:   2024-04-26
 *
 */
public interface IZzllBdService extends IService<ZzllBd> {
    List<ZzllBd> selectChildUnit(String bdnm);
    List<ZzllBd> selectAllUnit();
    List<ZzllBd> selectBdnm(String list);
    List<ZzllBd> selectZongdui();

    List<ZzllBd> buildTree(String bdnm, List<ZzllBd> unitList);

    ZzllBd buidlChildTree(ZzllBd zdbc,List<ZzllBd> bdList);

    AjaxResult<?> queryChildUnit(String bdnm, HttpServletRequest req);

    String getBdJbByBd(String bdnm);
}
