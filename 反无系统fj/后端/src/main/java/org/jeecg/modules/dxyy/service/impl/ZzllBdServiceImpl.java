package org.jeecg.modules.dxyy.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.Vo.ZzllBdVo;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CentralPlatformConstant;
import org.jeecg.common.constant.YhJbConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.wwct.CentralPlatformUtil;
import org.jeecg.modules.base.mapper.BaseCommonMapper;
import org.jeecg.modules.dxyy.entity.Vo.ZzllBdRyVo;
import org.jeecg.modules.dxyy.entity.ZzllBd;
import org.jeecg.modules.dxyy.mapper.ZzllBdMapper;
import org.jeecg.modules.dxyy.service.IZzllBdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @Description: zzll_bd_bs
 * @Author: jeecg-boot
 * @Date: 2024-04-26
 *
 */
@Service
@DS("wj-dxyy-zjk-datasource")
@Slf4j
public class ZzllBdServiceImpl extends ServiceImpl<ZzllBdMapper, ZzllBd> implements IZzllBdService {
    private List<ZzllBd> unitList = new CopyOnWriteArrayList<ZzllBd>();
    private Set<String> childId = new HashSet<>();
    private static final String treeType="bdxh";
    @Autowired
    private ZzllBdMapper zzllBdMapper;
    @Autowired
    private CentralPlatformUtil centralPlatformUtil;
    @Resource
    private BaseCommonMapper baseCommonMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private CommonAPI commonApi;
    @Override
    public List<ZzllBd> selectChildUnit(String bdnm) {
        List<ZzllBd> result = new ArrayList<>();
        if (redisUtil.hasKey(String.format(CentralPlatformConstant.TOKEN_KEY, CentralPlatformConstant.DATA_KEY_BDXX+bdnm))) {
            result = (List<ZzllBd>) redisUtil.get(String.format(CentralPlatformConstant.TOKEN_KEY,CentralPlatformConstant.DATA_KEY_BDXX+ bdnm));
        } else {
//            List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
//            BdCxDto bdCxDto = new BdCxDto();
//            bdCxDto.setBDNM(bdnm);
//            try {
//                //调用中台部队信息数据
//                ApiManager apiManager = baseCommonMapper.queryList("dxyy_wwct_bdcx");
//                JSONObject object = centralPlatformUtil.getData(bdCxDto, bdCxDto.getClass(), 0, 10000, apiManager.getClient(), apiManager.getSecret(), apiManager.getUrl(), null, true);
//                mapList = (List<Map<String, Object>>) object.get("content");
//            } catch (Exception e) {
//                e.printStackTrace();
            List<ZzllBd> finalResult = result;
//            }
//            mapList.forEach(item -> {
//                ZzllBd zzllBd = new ZzllBd();
//                zzllBd.setBdnm(item.get("bdnm").toString());
//                zzllBd.setBdjc(item.get("bdjc").toString());
//                zzllBd.setBdxh(item.get("bdxh").toString());
//                zzllBd.setSjbdnm(item.get("sjbdnm").toString());
//                finalResult.add(zzllBd);
//            });
            finalResult=zzllBdMapper.selectChildUnit(bdnm);
            boolean bo=redisUtil.set(String.format(CentralPlatformConstant.TOKEN_KEY,CentralPlatformConstant.DATA_KEY_BDXX+bdnm), finalResult,60*60*24);
            if (bo){
                if (log.isInfoEnabled()) {
                    log.info("设置成功：");
                }
            }
            return finalResult;
        }
        return result;
    }

    @Override
    @Cacheable(value = CacheConstant.SYS_BD_ALL_CACHE)
    public List<ZzllBd> selectAllUnit() {
        List<ZzllBd> result = zzllBdMapper.selectAllUnit();
        return result;
    }

    @Override
    public List<ZzllBd> selectBdnm(String list) {
        List<ZzllBd> result = zzllBdMapper.selectBdnm(list);
        return result;
    }

    @Override
    @Cacheable(value = CacheConstant.SYS_BD_CACHE)
    public List<ZzllBd> selectZongdui() {
        List<ZzllBd> result = zzllBdMapper.selectZongdui();
        return result;
    }


    /**
     * 建立树形结构
     *
     * @return
     */
    @Override
    public List<ZzllBd> buildTree(String bdbm, List<ZzllBd> unitList) {
        List<ZzllBd> tree = new CopyOnWriteArrayList<>();
        for (ZzllBd zdbc : getRootNode(bdbm,unitList)) {
            ZzllBd item = zdbc;
            item = buidlChildTree(zdbc,unitList);
            tree.add(item);
        }
        return tree;
    }

    /**
     * 建立子数结构
     *
     * @param zdbcNode
     * @return
     */
    @Override
    public ZzllBd buidlChildTree(ZzllBd zdbcNode,List<ZzllBd> bdList) {
        List<ZzllBd> childrenTree = new CopyOnWriteArrayList<>();
        for (ZzllBd zdbc : bdList) {
            switch (treeType){
                case "bdnm":
                    if (StringUtils.isNotBlank(zdbc.getSjbdnm())){
                        if (zdbc.getSjbdnm().equals(zdbcNode.getBdnm())) {
                            childId.add(zdbc.getBdnm());
                            childrenTree.add(buidlChildTree(zdbc,bdList));
                        }
                    }
                    break;
                case "bdxh":
                    if (StringUtils.isNotBlank(zdbc.getSjbdxh())){
                        if (zdbc.getSjbdxh().equals(zdbcNode.getBdxh())) {
                            childId.add(zdbc.getBdxh());
                            childrenTree.add(buidlChildTree(zdbc,bdList));
                        }
                    }
                    break;
                default:
                    if (StringUtils.isNotBlank(zdbc.getSjbdnm())){
                        if (zdbc.getSjbdnm().equals(zdbcNode.getBdnm())) {
                            childId.add(zdbc.getBdnm());
                            childrenTree.add(buidlChildTree(zdbc,bdList));
                        }
                    }
                    break;
            }
        }
        zdbcNode.setChildren(childrenTree);
        return zdbcNode;
    }

    @Override
    @Cacheable(value = CacheConstant.SYS_DEPARTS_CACHE,key = "#bdnm")
    public AjaxResult<?> queryChildUnit(String bdnm, HttpServletRequest req) {
        bdnm=StringEscapeUtils.escapeHtml4(bdnm);
        List<ZzllBd> unitLists = new CopyOnWriteArrayList<ZzllBd>();
        QueryWrapper<ZzllBd> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("BDNM", bdnm);
        ZzllBd  zzllBds= !this.list(queryWrapper1).isEmpty()?this.list(queryWrapper1).get(0):null;
        unitLists.clear();
        String bdxh = zzllBds.getBdxh();
        String bdnms = bdnm;
        String yhjb=zzllBds.getBdnm().equals("910000000")?"01":"";
        if (yhjb.equals(YhJbConstant.YHJB_01)) {
            QueryWrapper<ZzllBd> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("BDNM",bdnms).or().eq("LEFT( bdxh, LENGTH( bdxh ) - 2 )",zzllBds.getBdxh());
            unitLists.addAll(this.list(queryWrapper));
        } else {
            QueryWrapper<ZzllBd> queryWrapper = new QueryWrapper<>();
            queryWrapper.likeRight("bdxh",bdxh);
            //unitLists.addAll(zzllBdService.selectChildUnit(bdnms));
            unitLists.addAll(this.list(queryWrapper));
        }
        List<ZzllBd> childList = new CopyOnWriteArrayList<>();
        if (!unitLists.isEmpty()) {
            childList = this.buildTree(zzllBds.getBdxh(),unitLists);
        } else {
            return AjaxResult.OK(new CopyOnWriteArrayList<>());
        }
        //重新构建树结构并添加父节点
        ZzllBd zzllBd  = unitLists.stream().filter(item->item.getBdnm().equals(bdnms)).collect(Collectors.toList()).get(0);
        zzllBd.setChildren(childList);
        List<ZzllBd> childLists = new CopyOnWriteArrayList<>();
        childLists.add(zzllBd);
        return AjaxResult.OK(childLists);
    }

    @Override
    public String getBdJbByBd(String bdnm) {
        //总队为最高级单位集合
        List<ZzllBdVo> unitListAll = baseCommonMapper.selectAllUnit();
        List<ZzllBdVo>  DqBdList=unitListAll.stream().filter(item->item.getBdnm().equals(bdnm)).collect(Collectors.toList());
        Integer bdjb=getBdJbByBdNm( DqBdList.get(0).getBdxh(), unitListAll,0);
        //为保持原有等级不变，部队级别大于1时-1
//        if(bdjb>1){
//            bdjb = bdjb-1;
//        }
        String bdjbs="0"+bdjb;
        return bdjbs;
    }


    /**
     * 获取根节点
     *
     * @return
     */
    private List<ZzllBd> getRootNode(String bdbm,List<ZzllBd> unitList) {
        List<ZzllBd> rootList = new CopyOnWriteArrayList<>();
        for (ZzllBd zdbcNode : unitList) {
            switch (treeType) {
                case "bdnm":
                    if (StringUtils.isNotBlank(zdbcNode.getSjbdnm())) {
                        if (zdbcNode.getSjbdnm().equals(bdbm)) {
                            rootList.add(zdbcNode);
                        }
                    }
                    break;
                case "bdxh":
                    if (StringUtils.isNotBlank(zdbcNode.getSjbdxh())) {
                        if (zdbcNode.getSjbdxh().equals(bdbm)) {
                            rootList.add(zdbcNode);
                        }
                    }
                    break;
                default:
                    if (StringUtils.isNotBlank(zdbcNode.getSjbdnm())) {
                        if (zdbcNode.getSjbdnm().equals(bdbm)) {
                            rootList.add(zdbcNode);
                        }
                    }
                    break;
            }
        }
        //System.out.println(rootList);
        return rootList;
    }
    /**
     * 获取用户名
     *
     * @param request
     * @return
     */
    public static String getLoginUserByRequest(HttpServletRequest request) {
        String username = JwtUtil.getUserNameByToken(request);
        return username;
    }
    /**
     *  根据部队内码获取部队级别
     * @param bdnm
     * @param unitListZd
     * @return
     */
    public Integer getBdJbByBdNm(String bdnm,List<ZzllBdVo> unitListZd ,Integer bdjb){
        //当前部队信息
        List<ZzllBdVo>  DqBdList=unitListZd.stream().filter(item->item.getBdxh().equals(bdnm)).collect(Collectors.toList());
        if (!DqBdList.isEmpty()&&StringUtils.isNotBlank(DqBdList.get(0).getSjbdxh())){
            bdjb=getBdJbByBdNm( DqBdList.get(0).getSjbdxh(), unitListZd ,bdjb);
            bdjb++;
        }else{
            return bdjb;
        }
        return bdjb;
    }
}
