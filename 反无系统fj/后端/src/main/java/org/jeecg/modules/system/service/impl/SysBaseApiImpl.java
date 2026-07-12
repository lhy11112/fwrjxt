package org.jeecg.modules.system.service.impl;
import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.*;
import org.jeecg.common.util.IPUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.service.ISysDictService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;

/**
 * @Description: 底层共通业务API，提供其他独立模块调用
 * @Author: scott
 * @Date:2019-4-20
 *
 */
@Slf4j
@Service
public class SysBaseApiImpl implements ISysBaseAPI {
	/** 当前系统数据库类型 */
	private static String DB_TYPE = "";
	@Resource
	private SysUserMapper userMapper;
	@Autowired
	private  ISysDictService sysDictService;
	@Autowired
	private RedisUtil redisUtil;
	@Value(value = "")
	private String mhwz;
	private final HttpServletRequest request;
	@Value("${wjbd.wrj.djms}")
	private String IsDjms;

	public SysBaseApiImpl(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	/**
	 * @Cacheable(cacheNames=CacheConstant.SYS_USERS_CACHE, key="#username")
	 *
	 */
	public LoginUser getUserByName(String username) {
		if(oConvertUtils.isEmpty(username)) {
			return null;
		}
		LoginUser loginUser = new LoginUser();
		SysUser sysUser = new SysUser();
		//单机模式默认查询本地库用户信息负责查询门户网站用户信息
		if (Boolean.parseBoolean(IsDjms)){
			sysUser = userMapper.getUserByName(username);
		}else{
			sysUser = userMapper.getUserByNames(username);
		}
		String accessToken = request.getHeader("X-Access-Token");
		if(sysUser==null) {
			return null;
		}
		BeanUtils.copyProperties(sysUser, loginUser);
		return loginUser;
	}

	@Override
	public String translateDictFromTable(String table, String text, String code, String key) {
		return sysDictService.queryTableDictTextByKey(table, text, code, key);
	}

	@Override
	public String translateDict(String code, String key) {
		return sysDictService.queryDictTextByKey(code, key);
	}

	@Override
	@Cacheable(value = CacheConstant.SYS_DICT_CACHE,key = "#code")
	public List<DictModel> queryDictItemsByCode(String code) {
		return sysDictService.queryDictItemsByCode(code);
	}

	@Override
	public List<DictModel> queryTableDictItemsByCode(String table, String text, String code) {
		//update-begin-author:taoyan date:20200820 for:【Online+系统】字典表加权限控制机制逻辑，想法不错 LOWCOD-799
		if(table.indexOf("#{")>=0){
			table = QueryGenerator.getSqlRuleValue(table);
		}
		//update-end-author:taoyan date:20200820 for:【Online+系统】字典表加权限控制机制逻辑，想法不错 LOWCOD-799
		return sysDictService.queryTableDictItemsByCode(table, text, code);
	}

	@EventListener(ApplicationStartedEvent.class)
	@Override
	public void initPkDz() {
	}



	@Value("${jeecg.path.upload}")
	private String upload;



	@EventListener(ApplicationStartedEvent.class)
	public void initUploadFile() {
		boolean existUpload = FileUtil.exist(upload);
		if (log.isInfoEnabled()) {
			log.info("上传文件目录是否存在：" + existUpload);
		}
		boolean existPvc = FileUtil.exist(upload);
		if (log.isInfoEnabled()) {
			log.info("持久卷挂载目录是否存在：" + existPvc);
		}
		if (!existPvc) {
			File mkdir = FileUtil.mkdir(upload);
			if (log.isInfoEnabled()) {
				log.info("持久卷挂载目录是否创建成功：" + mkdir);
			}
		}
		//持久卷里没有数据 说明是第一次部署 需要拷贝静态资源
		if (log.isInfoEnabled()) {
			log.info("持久卷里没有文件 说明是第一次部署 需要拷贝静态资源");
		}
		boolean existInitUpload = FileUtil.exist(upload.replaceAll("uploadfile","uploadfiles"));
		if (log.isInfoEnabled()) {
			log.info("静态资源目录是否存在：" + existInitUpload);
		}
		if (existInitUpload) {
			FileUtil.copy(new File(upload.replaceAll("uploadfile","uploadfiles")), new File(upload.replaceAll("//upFiles","")), true);
			if (log.isInfoEnabled()) {
				log.info("静态资源目录拷贝成功");
			}
		}

	}

}