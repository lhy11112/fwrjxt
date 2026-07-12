package org.jeecg.modules.wrj.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.wrj.entity.WjbdHsZsk;
import org.jeecg.modules.wrj.entity.WjbdHsZskWj;
import org.jeecg.modules.wrj.entity.WjbdHsZskWjNr;
import org.jeecg.modules.wrj.mapper.WjbdHsZskWjNrMapper;
import org.jeecg.modules.wrj.service.IWjbdHsZskService;
import org.jeecg.modules.wrj.service.IWjbdHsZskWjNrService;
import org.jeecg.modules.wrj.service.IWjbdHsZskWjService;
import org.jeecg.modules.wrj.utils.MiddleServiceHttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Description: 知识库_文件_内容
 * @Author: jeecg-boot
 * @Date: 2025-10-27
 * @Version: V1.0
 */
@Service
public class WjbdHsZskWjNrServiceImpl extends ServiceImpl<WjbdHsZskWjNrMapper, WjbdHsZskWjNr> implements IWjbdHsZskWjNrService {

    @Autowired
    private MiddleServiceHttpUtil middleServiceHttpUtil;
    @Autowired
    private IWjbdHsZskService wjbdHsZskService;
    @Autowired
    private IWjbdHsZskWjService wjbdHsZskWjService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(WjbdHsZskWjNr wjbdHsZskWjNr) {
        //查询数据库知识库
        WjbdHsZsk zsk = wjbdHsZskService.getById(wjbdHsZskWjNr.getZskId());
        //查询数据库知识库文件
        WjbdHsZskWj zskWj = wjbdHsZskWjService.getById(wjbdHsZskWjNr.getZskWjId());
        //封装
        Long id = wjbdHsZskWjNr.getId();
        String content = wjbdHsZskWjNr.getNr();
        String tableName = zsk.getZskNm();
        String wjMc = zskWj.getWjMc();
        String wjNm = zskWj.getWjNm();
        String fwqWjlj = zskWj.getFwqWjlj();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        jsonObject.put("content", content);
        jsonObject.put("tableName", tableName);
        jsonObject.put("wjMc", wjMc);
        jsonObject.put("wjNm", wjNm);
        jsonObject.put("fwqWjlj", fwqWjlj);

        //修改数据库
        this.updateById(wjbdHsZskWjNr);
        middleServiceHttpUtil.editWjNr(jsonObject);
    }
}
