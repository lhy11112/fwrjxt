package org.jeecg.modules.uav.util;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.uav.vo.UavInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class O4Utils {
    private static final Logger log = LoggerFactory.getLogger(O4Utils.class);
    @Value("${O4online.apiloginurl}")
    private String apiloginurl;
    @Value("${O4online.decryptlurl}")
    private String decryptlurl;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 鉴权拿到token
     *
     * @return
     */
    private String authApiLogin() {
        if (!redisUtil.hasKey("O4Oonline-token")) {
            String responseData = HttpUtil.get(apiloginurl);
            if (StringUtils.isNotBlank(responseData)) {
                JSONObject jsonObject = JSONObject.parseObject(responseData);
                JSONObject jsonObject1 = JSONObject.parseObject(jsonObject.get("data").toString());
                String token = jsonObject1.get("token").toString();
                log.info("获取o4联网解密token" + token);
                redisUtil.set("O4Oonline-token", token, 24 * 60 * 60 * 1000);
                return token;
            }
        } else {
            return redisUtil.hasKey("O4Oonline-token").toString();
        }
        return null;
    }

    /**
     * O4联网解密
     * @param uavInfos
     * @return
     */
    public UavInfo decrypt(String uavInfos) {
        String token = authApiLogin();
        String responseData = HttpUtil.get(String.format(decryptlurl, uavInfos, token));
        UavInfo uavInfo = JSONObject.parseObject(responseData, UavInfo.class);
        return uavInfo;
    }
}
