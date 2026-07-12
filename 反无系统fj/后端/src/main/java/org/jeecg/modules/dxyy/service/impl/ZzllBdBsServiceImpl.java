package org.jeecg.modules.dxyy.service.impl;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.dxyy.entity.ZzllBdBs;
import org.jeecg.modules.dxyy.mapper.ZzllBdBsMapper;
import org.jeecg.modules.dxyy.service.IZzllBdBsService;
import org.springframework.stereotype.Service;

/**
 * @Description: zzll_bd_bs
 * @Author: jeecg-boot
 * @Date:   2024-04-26
 *
 */
@Service
@DS("wj-dxyy-zjk-datasource")
public class ZzllBdBsServiceImpl extends ServiceImpl<ZzllBdBsMapper, ZzllBdBs> implements IZzllBdBsService {

}
