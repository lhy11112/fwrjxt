package org.jeecg.modules.system.controller;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.exceptions.ClientException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.IPUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.TokenUtils;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDictService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
/**
 * @Author scott
 * @since 2018-12-17
 */
@RestController
@RequestMapping("/sys")
@Api(tags="用户登录")
@Slf4j
public class LoginController {
	@Autowired
	private ISysBaseAPI sysBaseAPI;
	@Autowired
    private RedisUtil redisUtil;
	@Resource
	private BaseCommonService baseCommonService;
	/**
	 * 退出登录
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/logout")
	public AjaxResult<Object> logout(HttpServletRequest request,HttpServletResponse response) {
		//用户退出逻辑
	    String token = request.getHeader(CommonConstant.X_ACCESS_TOKEN);
	    if(oConvertUtils.isEmpty(token)) {
	    	return AjaxResult.error("退出登录失败！");
	    }
	    String username = JwtUtil.getUsername(token);
	    //System.out.println("username:"+username);
		LoginUser sysUser = sysBaseAPI.getUserByName(username);
		String ip = IPUtils.getIpAddr(request);
	    if(sysUser!=null) {
			//update-begin--Author:wangshuai  Date:20200714  for：登出日志没有记录人员
			baseCommonService.addLog("用户名: "+sysUser.getRealname()+",退出成功！", CommonConstant.LOG_TYPE_1, null,sysUser);
			//update-end--Author:wangshuai  Date:20200714  for：登出日志没有记录人员
	    	if (log.isInfoEnabled()){
			log.info(" 用户名:  "+sysUser.getRealname()+",退出成功！ ");
			}
	    	//清空用户登录Token缓存
			String string=TokenUtils.removeRedisToken(sysUser.getId(),sysUser.getUsername(), token, this.redisUtil,ip);
			if (StringUtils.isNotBlank(string)){
				if (log.isInfoEnabled()){
					log.info("清空用户登录Token缓存成功！！！");
				}
			}
			//调用shiro的logout
			SecurityUtils.getSubject().logout();
	    	return AjaxResult.ok("退出登录成功！");
	    }else {
	    	return AjaxResult.error("Token无效!");
	    }
	}
}
