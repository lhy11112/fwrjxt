package org.jeecg.modules.wrj.service.impl;

import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.modules.wrj.entity.WjbdHsZsk;
import org.jeecg.modules.wrj.entity.WjbdHsZskWj;
import org.jeecg.modules.wrj.entity.WjbdHsZskWjNr;
import org.jeecg.modules.wrj.entity.dto.InsertZskDTO;
import org.jeecg.modules.wrj.entity.dto.ZskDTO;
import org.jeecg.modules.wrj.entity.vo.ZskWjVO;
import org.jeecg.modules.wrj.mapper.WjbdHsZskWjMapper;
import org.jeecg.modules.wrj.service.IWjbdHsZskService;
import org.jeecg.modules.wrj.service.IWjbdHsZskWjNrService;
import org.jeecg.modules.wrj.service.IWjbdHsZskWjService;
import org.jeecg.modules.wrj.utils.FileUtil;
import org.jeecg.modules.wrj.utils.MiddleServiceHttpUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: wjbd_wrj_zsk_wj
 * @Author: jeecg-boot
 * @Date: 2024-09-14
 */
@Service
@Slf4j
public class WjbdHsZskWjServiceImpl extends ServiceImpl<WjbdHsZskWjMapper, WjbdHsZskWj> implements IWjbdHsZskWjService {

    @Autowired
    private IWjbdHsZskService wjbdHsZskService;

    @Autowired
    private IWjbdHsZskWjService wjbdHsZskWjService;

    @Autowired
    private IWjbdHsZskWjNrService wjbdHsZskWjNrService;

    @Resource
    private FileUtil fileUtil;

    @Autowired
    private MiddleServiceHttpUtil middleServiceHttpUtil;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<?> add(List<WjbdHsZskWj> wjbdHsZskWjList) {
        //文件表
        for (WjbdHsZskWj wjbdHsZskWj : wjbdHsZskWjList) {
            //相对路径
            String fjPath = wjbdHsZskWj.getFwqWjlj();
            wjbdHsZskWj.setWjNm(UUID.fastUUID().toString(true));
            String path = fileUtil.getProjectFilePath(fjPath);
            //文件大小
            wjbdHsZskWj.setWjdx(cn.hutool.core.io.FileUtil.size(new File(path)));
        }
        wjbdHsZskWjService.saveBatch(wjbdHsZskWjList);
        //文件内容
        List<WjbdHsZskWjNr> wjbdHsZskWjNrList = new ArrayList<>();
        for (WjbdHsZskWj wjbdHsZskWj : wjbdHsZskWjList) {
//            threadPoolTaskExecutor.execute(() -> {
            //封装向量库添加实体
            InsertZskDTO insertZskDTO = setInsertZskDTO(wjbdHsZskWj);
            //添加向量库
            List<ZskDTO> zskDTOList = middleServiceHttpUtil.insertZskData(insertZskDTO);
            for (ZskDTO zskDTO : zskDTOList) {
                Long docId = zskDTO.getDocId();
                String content = zskDTO.getContent();
                WjbdHsZskWjNr wjbdHsZskWjNr = new WjbdHsZskWjNr();
                wjbdHsZskWjNr.setId(docId);
                wjbdHsZskWjNr.setZskId(wjbdHsZskWj.getZskId());
                wjbdHsZskWjNr.setZskWjId(wjbdHsZskWj.getId());
                wjbdHsZskWjNr.setNr(content);
                wjbdHsZskWjNrList.add(wjbdHsZskWjNr);
            }
//            });
        }
        wjbdHsZskWjNrService.saveBatch(wjbdHsZskWjNrList);
        return AjaxResult.OK();
    }


    /**
     * 封装向量库添加实体
     *
     * @param wjbdHsZskWj wjbdHsZskWj对象
     * @return 结果
     */
    @NotNull
    private InsertZskDTO setInsertZskDTO(WjbdHsZskWj wjbdHsZskWj) {
        InsertZskDTO insertZskDTO = new InsertZskDTO();
        //知识库内码
        WjbdHsZsk zsk = wjbdHsZskService.getById(wjbdHsZskWj.getZskId());
        insertZskDTO.setZskNm(zsk.getZskNm());
        //文件VO对象
        ZskWjVO zskWjVO = new ZskWjVO();
        //路径
        zskWjVO.setFwqWjlj(wjbdHsZskWj.getFwqWjlj());
        //文件名称
        zskWjVO.setWjMc(wjbdHsZskWj.getWjMc());
        //文件内码
        zskWjVO.setWjNm(wjbdHsZskWj.getWjNm());
        insertZskDTO.setZskWjVO(zskWjVO);
        return insertZskDTO;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<?> delInfo(String id) {
        //查询
        WjbdHsZskWj zskWj = wjbdHsZskWjService.getById(id);
        if (zskWj == null) {
            return AjaxResult.OK();
        }
        //知识库
        String wjNm = zskWj.getWjNm();
        wjbdHsZskWjService.removeById(id);
        //知识库文件
        LambdaQueryWrapper<WjbdHsZskWjNr> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WjbdHsZskWjNr::getZskWjId, id);
        wjbdHsZskWjNrService.remove(queryWrapper);

        //向量库
        WjbdHsZsk hsZsk = wjbdHsZskService.getById(zskWj.getZskId());
        if (hsZsk != null) {
            middleServiceHttpUtil.delByWjNm(hsZsk.getZskNm(), wjNm);
        }
        //异步删除
        threadPoolTaskExecutor.execute(() -> {
            //文件路径 删除文件
            String fwqWjlj = zskWj.getFwqWjlj();
            String projectFilePath = fileUtil.getProjectFilePath(fwqWjlj);
            if (cn.hutool.core.io.FileUtil.exist(projectFilePath)) {
                boolean del = cn.hutool.core.io.FileUtil.del(projectFilePath);
                if (log.isDebugEnabled()) {
                    log.debug("删除结果：" + del);
                }
            }
        });

        return AjaxResult.OK();
    }

}
