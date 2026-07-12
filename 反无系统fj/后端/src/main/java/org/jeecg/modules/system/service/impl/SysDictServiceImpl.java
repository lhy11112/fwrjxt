package org.jeecg.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.DictQuery;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.entity.SysDictItem;
import org.jeecg.modules.system.mapper.SysDictItemMapper;
import org.jeecg.modules.system.mapper.SysDictMapper;
import org.jeecg.modules.system.service.ISysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@Service
@Slf4j
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements ISysDictService {

    @Autowired
    private SysDictMapper sysDictMapper;
    @Autowired
    private SysDictItemMapper sysDictItemMapper;

    /**
     * 通过查询指定code 获取字典
     *
     * @param code
     * @return
     */
    @Override
    @Cacheable(value = CacheConstant.SYS_DICT_CACHE, key = "#code")
    public List<DictModel> queryDictItemsByCode(String code) {
        if (log.isDebugEnabled()) {
            log.debug("无缓存dictCache的时候调用这里！");
        }
        return sysDictMapper.queryDictItemsByCode(code);
    }


    /**
     * 通过查询指定code 获取字典值text
     *
     * @param code
     * @param key
     * @return
     */

    @Override
    @Cacheable(value = CacheConstant.SYS_DICT_CACHE, key = "#code+':'+#key")
    public String queryDictTextByKey(String code, String key) {
        if (log.isDebugEnabled()) {
            log.debug("无缓存dictText的时候调用这里！");
        }
        return sysDictMapper.queryDictTextByKey(code, key);
    }

    /**
     * 通过查询指定table的 text code 获取字典
     * dictTableCache采用redis缓存有效期10分钟
     *
     * @param table
     * @param text
     * @param code
     * @return
     */
    @Override
    public List<DictModel> queryTableDictItemsByCode(String table, String text, String code) {
        if (log.isDebugEnabled()) {
            log.debug("无缓存dictTableList的时候调用这里！");
        }
        return sysDictMapper.queryTableDictItemsByCode(table, text, code);
    }

    @Override
    public List<DictModel> queryTableDictItemsByCodeAndFilter(String table, String text, String code, String filterSql) {
        if (log.isDebugEnabled()) {
            log.debug("无缓存dictTableList的时候调用这里！");
        }
        return sysDictMapper.queryTableDictItemsByCodeAndFilter(table, text, code, filterSql);
    }

    /**
     * 通过查询指定table的 text code 获取字典值text
     * dictTableCache采用redis缓存有效期10分钟
     *
     * @param table
     * @param text
     * @param code
     * @param key
     * @return
     */
    @Override
    @Cacheable(value = CacheConstant.SYS_DICT_TABLE_CACHE)
    public String queryTableDictTextByKey(String table, String text, String code, String key) {
        if (log.isDebugEnabled()) {
            log.debug("无缓存dictTable的时候调用这里！");
        }
        return sysDictMapper.queryTableDictTextByKey(table, text, code, key);
    }

    /**
     * 通过查询指定table的 text code 获取字典，包含text和value
     * dictTableCache采用redis缓存有效期10分钟
     *
     * @param table
     * @param text
     * @param code
     * @param keys  (逗号分隔)
     * @return
     */
    @Override
    /**
     * update-end--Author:lvdandan  Date:20201204 for：JT-36【online】树形列表bug修改后，还是显示原来值 暂时去掉缓存
     *
     */
    public List<String> queryTableDictByKeys(String table, String text, String code, String keys) {
        if (oConvertUtils.isEmpty(keys)) {
            return null;
        }
        String[] keyArray = keys.split(",");
        List<DictModel> dicts = sysDictMapper.queryTableDictByKeys(table, text, code, keyArray);
        List<String> texts = new ArrayList<>(dicts.size());
        // 查询出来的顺序可能是乱的，需要排个序
        for (String key : keyArray) {
            for (DictModel dict : dicts) {
                if (key.equals(dict.getValue())) {
                    texts.add(dict.getText());
                    break;
                }
            }
        }
        return texts;
    }


    @Override
    public List<DictModel> queryTableDictItems(String table, String text, String code, String keyword) {
        return baseMapper.queryTableDictItems(table, text, code, "%" + keyword + "%");
    }

    @Override
    public List<DictModel> queryLittleTableDictItems(String table, String text, String code, String keyword, int pageSize) {
        return null;
    }

    @Override
    public void deleteOneDictPhysically(String id) {

    }

    @Override
    public void updateDictDelFlag(int delFlag, String id) {

    }

    @Override
    public List<SysDict> queryDeleteList() {
        return null;
    }

    @Override
    public List<DictModel> queryDictTablePageList(DictQuery query, int pageSize, int pageNo) {
        Page page = new Page(pageNo, pageSize, false);
        Page<DictModel> pageList = baseMapper.queryDictTablePageList(page, query);
        return pageList.getRecords();
    }
}
