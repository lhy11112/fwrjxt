package org.jeecg.modules.dxyy.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.dxyy.entity.WjbdWrjYhglCs;
import org.jeecg.modules.dxyy.entity.ZzllBd;
import org.jeecg.modules.dxyy.entity.ZzllBdBs;
import org.jeecg.modules.dxyy.mapper.WjbdWrjYhglCsMapper;
import org.jeecg.modules.dxyy.service.IWjbdWrjYhglCsService;
import org.jeecg.modules.dxyy.service.IZzllBdBsService;
import org.jeecg.modules.dxyy.service.IZzllBdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.util.*;

/**
 * @Description: 武警部队_无人机_用户管理_参数
 * @Author: jeecg-boot
 * @Date:   2025-09-01
 * @Version: V1.0
 */
@Service
public class WjbdWrjYhglCsServiceImpl extends ServiceImpl<WjbdWrjYhglCsMapper, WjbdWrjYhglCs> implements IWjbdWrjYhglCsService {

    @Autowired
    private IZzllBdService iZzllBdService;
    @Autowired
    private IZzllBdBsService iZzllBdBsService;

    public static final String CSZ = "{\"bdjc\":\"\",\"bdnm\":\"\",\"jd\":116.36736259764437,\"wd\":39.935711741366724,\"mapLevel\":9,\"qxDmmc\":\"\",\"qxDmnm\":\"\",\"qxDmxh\":\"\",\"sfDmmc\":\"\",\"sfDmnm\":\"\",\"sfDmxh\":\"\",\"sqDmmc\":\"\",\"sqDmnm\":\"\",\"sqDmxh\":\"\",\"dmmc\":\"\",\"bdfh\":\"\",\"bdxh\":\"\"}";

    @Override
    public List<WjbdWrjYhglCs> updateJwdDmNm() {
        List<WjbdWrjYhglCs> wjbdWwctYhglCss = new ArrayList<>();
        ZzllBd zzllBds = new ZzllBd();
        ZzllBdBs zzllBdBss = new ZzllBdBs();
        Map<String, Object> itemMap = new HashMap<String, Object>();
        //当前用户
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        itemMap.put("bdnm", user.getDepartIds());
        List<ZzllBd> zzllBd = iZzllBdService.listByMap(itemMap);
        if (zzllBd.size() > 0) {
            zzllBds = zzllBd.get(0);
        }
        List<ZzllBdBs> zzllBdBs = iZzllBdBsService.listByMap(itemMap);
        if (zzllBdBs.size() > 0) {
            zzllBdBss = zzllBdBs.get(0);
        }
        WjbdWrjYhglCs wjbdWwctYhglCs = new WjbdWrjYhglCs();
        wjbdWwctYhglCs.setYhId(user.getId());
        wjbdWwctYhglCs.setBdnm(user.getDepartIds());
        wjbdWwctYhglCs.setCsBm("sys_user_info");
        wjbdWwctYhglCs.setRksj(new Date());
        wjbdWwctYhglCs.setCsMc("用户登录基本信息");
        JSONObject jsonObject = JSONObject.parseObject(CSZ);
        jsonObject.put("bdjc", zzllBds.getBdjc());
        jsonObject.put("bdnm", zzllBds.getBdnm());
        jsonObject.put("jd", zzllBdBss.getJd());
        jsonObject.put("wd", zzllBdBss.getWd());
        jsonObject.put("bdfh", zzllBds.getBdfh());
        jsonObject.put("bdxh", zzllBds.getBdxh());
        jsonObject.put("dmmc", zzllBdBss.getKzdm());
        if (StringUtils.isNotBlank(zzllBdBss.getDmnm())) {
            Map<String, Object> objectMap = baseMapper.getListDmNmByDmNm(zzllBdBss.getDmnm().substring(0, 2) + "0000");
            if (objectMap != null) {
                jsonObject.put("sfDmmc", objectMap.get("qc"));
                jsonObject.put("sfDmnm", objectMap.get("dmnm"));
                jsonObject.put("sfDmxh", objectMap.get("xh"));
            }
        } else {
            if (StringUtils.isNotBlank(zzllBdBss.getKzdm())) {
                Map<String, Object> objectMap = baseMapper.getListDmNm(zzllBdBss.getKzdm().substring(0, 3));
                if (objectMap != null) {
                    jsonObject.put("sfDmmc", objectMap.get("mc"));
                    jsonObject.put("sfDmnm", objectMap.get("dmnm"));
                    jsonObject.put("sfDmxh", objectMap.get("xh"));
                }
            }else{
                if (StringUtils.isNotBlank(zzllBds.getBdfh())) {
                    Map<String, Object> objectMap = baseMapper.getListDmNm(zzllBds.getBdfh().substring(10, 13));
                    if (objectMap != null) {
                        jsonObject.put("sfDmmc", objectMap.get("mc"));
                        jsonObject.put("sfDmnm", objectMap.get("dmnm"));
                        jsonObject.put("sfDmxh", objectMap.get("xh"));
                    }
                }
            }
        }
        wjbdWwctYhglCs.setCsz(jsonObject.toJSONString());
        baseMapper.insert(wjbdWwctYhglCs);
        wjbdWwctYhglCss.add(wjbdWwctYhglCs);
        return wjbdWwctYhglCss;
    }
}
