package org.jeecg.modules.api.controller;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.dto.OnlineAuthDTO;
import org.jeecg.common.api.dto.message.*;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
/**
 * 服务化 system模块 对外接口请求类
 * @Description: 服务化 system模块 对外接口请求类
 * @Author: 李海洋
 * @Date:   2024-07-31
 *
 */
@RestController
@RequestMapping("/sys/api")
public class SystemAPIController {

    @Autowired
    private ISysBaseAPI sysBaseAPI;

}
