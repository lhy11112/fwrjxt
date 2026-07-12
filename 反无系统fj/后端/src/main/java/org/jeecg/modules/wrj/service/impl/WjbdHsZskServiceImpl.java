package org.jeecg.modules.wrj.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.wrj.entity.WjbdHsZsk;
import org.jeecg.modules.wrj.entity.WjbdHsZskWj;
import org.jeecg.modules.wrj.mapper.WjbdHsZskMapper;
import org.jeecg.modules.wrj.service.IWjbdHsZskService;
import org.jeecg.modules.wrj.service.IWjbdHsZskWjService;
import org.jeecg.modules.wrj.utils.FileUtil;
import org.jeecg.modules.wrj.utils.MiddleServiceHttpUtil;
import org.jeecg.modules.wrj.utils.ZskConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: wjbd_wrj_zsk
 * @Author: jeecg-boot
 * @Date: 2024-09-14
 */
@Service
@Slf4j
public class WjbdHsZskServiceImpl extends ServiceImpl<WjbdHsZskMapper, WjbdHsZsk> implements IWjbdHsZskService {

    @Autowired
    private IWjbdHsZskService wjbdHsZskService;

    @Autowired
    private IWjbdHsZskWjService wjbdHsZskWjService;

    @Autowired
    private MiddleServiceHttpUtil middleServiceHttpUtil;

    @Resource
    private FileUtil fileUtil;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Value("${wjbd.wrj.djms}")
    private String IsDjms;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<?> add(WjbdHsZsk wjbdHsZsk) {
        //知识库内码（向量库名称）
        if (StrUtil.isBlank(wjbdHsZsk.getZskNm())) {
            wjbdHsZsk.setZskNm(ZskConstants.ZSK + PinyinUtil.getPinyin(wjbdHsZsk.getZskMc(), ""));
        }
        wjbdHsZsk.setRksj(new Date());
        wjbdHsZskService.save(wjbdHsZsk);

        //向量库
        middleServiceHttpUtil.createZskTable(wjbdHsZsk.getZskNm());
        return AjaxResult.OK(wjbdHsZsk);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<?> editZskMc(WjbdHsZsk wjbdHsZsk) {
        //旧的知识库内码
        String oldName = wjbdHsZsk.getZskNm();
        //新的知识库内码
        String newName = ZskConstants.ZSK + PinyinUtil.getPinyin(wjbdHsZsk.getZskMc(), "");

        //需要修改向量库
        if (!oldName.equals(newName)) {
            //查询知识库数据
            LambdaQueryWrapper<WjbdHsZsk> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(WjbdHsZsk::getZskMc, wjbdHsZsk.getZskMc());
            queryWrapper.last("limit 1");
            WjbdHsZsk one = wjbdHsZskService.getOne(queryWrapper);
            if (one != null) {
                return AjaxResult.error("当前知识库已存在!");
            }
        }

        //知识库内码（向量库名称）
        wjbdHsZsk.setZskNm(newName);
        wjbdHsZskService.updateById(wjbdHsZsk);

//        middleServiceHttpUtil.updateTableName(oldName, newName);
        return AjaxResult.OK();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult<?> deleteBatch(String ids) {
        List<String> idList = Arrays.asList(ids.split(","));
        List<String> tableNameList = new ArrayList<>();
        for (String id : idList) {
            WjbdHsZsk one = wjbdHsZskService.getById(id);
            if (one == null) {
                continue;
            }
            tableNameList.add(one.getZskNm());
        }
        //删除知识库表数据
        wjbdHsZskService.removeByIds(idList);
        //删除文件表
        LambdaQueryWrapper<WjbdHsZskWj> wjbdHsZskWjLambdaQueryWrapper = new LambdaQueryWrapper<>();
        wjbdHsZskWjLambdaQueryWrapper.in(WjbdHsZskWj::getZskId, idList);
        List<WjbdHsZskWj> wjbdHsZskWjList = wjbdHsZskWjService.list(wjbdHsZskWjLambdaQueryWrapper);
        List<String> filePathList = wjbdHsZskWjList.stream().map(wjbdHsZskWj -> fileUtil.getProjectFilePath(wjbdHsZskWj.getFwqWjlj())).collect(Collectors.toList());
        wjbdHsZskWjService.remove(wjbdHsZskWjLambdaQueryWrapper);
        threadPoolTaskExecutor.execute(() -> {
            for (String filePath : filePathList) {
                if (cn.hutool.core.io.FileUtil.exist(filePath)) {
                    boolean del = cn.hutool.core.io.FileUtil.del(filePath);
                    if (log.isInfoEnabled()) {
                        log.info("删除结果：" + del);
                    }
                }
            }
        });

        //删除向量库
        middleServiceHttpUtil.delBatchZskTable(CollUtil.join(tableNameList, ","));
        return AjaxResult.OK();
    }


    /**
     * 初始化知识库
     */
    @EventListener(ApplicationStartedEvent.class)
    @Transactional(rollbackFor = Exception.class)
    public void initZsk() {
        //单机版本关闭时候推送向量库
        if (!Boolean.parseBoolean(IsDjms)){
        List<WjbdHsZsk> wjbdHsZskList = wjbdHsZskService.list();
        if (CollUtil.isNotEmpty(wjbdHsZskList)) {
            for (WjbdHsZsk wjbdHsZsk : wjbdHsZskList) {
                String zskMc = wjbdHsZsk.getZskMc();
                String zskNm = wjbdHsZsk.getZskNm();
                try {
                    createZsk(zskMc, zskNm);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("自动创建向量库失败！", e);
                    }
                }
            }
        }
        }
    }

    /**
     * 知识库创建
     *
     * @param czlZskMc 处置链知识库名称
     * @param czlZskNm 处置链知识库内码
     */
    @Override
    public Long createZsk(String czlZskMc, String czlZskNm) {
        //情况判断历史知识库
        LambdaQueryWrapper<WjbdHsZsk> qkpdZskQueryWrapper = new LambdaQueryWrapper<>();
        qkpdZskQueryWrapper.eq(WjbdHsZsk::getZskMc, czlZskMc);
        qkpdZskQueryWrapper.eq(WjbdHsZsk::getZskNm, czlZskNm);
        qkpdZskQueryWrapper.last(" limit 1");
        WjbdHsZsk wjbdHsZsk = wjbdHsZskService.getOne(qkpdZskQueryWrapper);
        boolean isExist = wjbdHsZsk != null;
        if (log.isInfoEnabled()) {
            log.info(czlZskMc + "是否存在：" + isExist);
        }
        //若不存在
        if (!isExist) {
            wjbdHsZsk = new WjbdHsZsk();
            wjbdHsZsk.setZskMc(czlZskMc);
            wjbdHsZsk.setZskNm(czlZskNm);
//            wjbdHsZsk.setZskLx(ZskLx.ZSK_CZL.getCode());
            wjbdHsZsk.setRksj(new Date());
            wjbdHsZskService.save(wjbdHsZsk);
        }
        middleServiceHttpUtil.createZskTable(czlZskNm);
        if (log.isInfoEnabled()) {
            log.info(czlZskMc + "加载成功。");
        }
        return wjbdHsZsk.getId();
    }


}
