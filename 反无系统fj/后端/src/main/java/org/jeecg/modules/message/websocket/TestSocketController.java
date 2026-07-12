package org.jeecg.modules.message.websocket;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.constant.WebsocketConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
/**
 *
 * @Description:
 * @Author: 李海洋
 * @Date:   2024-07-31
 * @Version: V1.0
 */
@RestController
@RequestMapping("/sys/socketTest")
public class TestSocketController {

    @Autowired
    private WebSocket webSocket;

    @PostMapping("/sendAll")
    public AjaxResult<String> sendAll(@RequestBody JSONObject jsonObject) {
		AjaxResult<String> result = new AjaxResult<String>();
    	String message = jsonObject.getString("message");
    	JSONObject obj = new JSONObject();
    	obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
		obj.put(WebsocketConst.MSG_ID, "M0001");
		obj.put(WebsocketConst.MSG_TXT, message);
    	webSocket.sendMessage(obj.toJSONString());
        result.setResult("群发！");
        return result;
    }

    @PostMapping("/sendUser")
    public AjaxResult<String> sendUser(@RequestBody JSONObject jsonObject) {
		AjaxResult<String> result = new AjaxResult<String>();
    	String userId = jsonObject.getString("userId");
    	String message = jsonObject.getString("message");
    	JSONObject obj = new JSONObject();
    	obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
    	obj.put(WebsocketConst.MSG_USER_ID, userId);
		obj.put(WebsocketConst.MSG_ID, "M0001");
		obj.put(WebsocketConst.MSG_TXT, message);
        webSocket.sendMessage(userId, obj.toJSONString());
        result.setResult("单发");
        return result;
    }

}