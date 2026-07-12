package org.jeecg.modules.wrj.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.RestUtil;
import org.jeecg.modules.wrj.entity.dto.InsertZskDTO;
import org.jeecg.modules.wrj.entity.dto.ZskDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 中间件服务_知识库_工具类
 *
 * @Description: 中间件服务_知识库_工具类
 * @Author: 冯程骞
 * @Date: 2024-09-18
 */
@Slf4j
@Component
public class MiddleServiceHttpUtil {

    @Value("${middleServiceUrl}")
    private String middleServiceUrl;

    /**
     * 创建知识库——向量库
     *
     * @param tableName tableName
     */
    public void createZskTable(String tableName) {
//        HttpHeaders headers = RestUtil.getHeaderApplicationJson();
//        headers.add("Authorization", token);
        String url = middleServiceUrl + MiddleServiceApi.Zsk.ZSK_CREATE_TABLE;
        ResponseEntity<JSONObject> result = RestUtil.request(
                url + "?tableName=" + tableName,
                HttpMethod.GET,
                null,
                null,
                null,
                JSONObject.class
        );
        if (result.getStatusCode() != HttpStatus.OK) {
            throw new JeecgBootException("创建向量库失败！");
        }
    }

    /**
     * 批量删除知识库——向量库
     *
     * @param tableNames 多个表名
     */
    public void delBatchZskTable(String tableNames) {
//        HttpHeaders headers = RestUtil.getHeaderApplicationJson();
//        headers.add("Authorization", token);
        String url = middleServiceUrl + MiddleServiceApi.Zsk.ZSK_DEL_BATCH_TABLE;
        ResponseEntity<JSONObject> result = RestUtil.request(
                url + "?tableNames=" + tableNames,
                HttpMethod.DELETE,
                null,
                null,
                null,
                JSONObject.class
        );
        if (result.getStatusCode() != HttpStatus.OK) {
            throw new JeecgBootException("删除向量库失败！");
        }
    }

    /**
     * 插入知识库文档数据——向量库
     *
     * @param insertZskDTO insertZskDTO
     */
    public List<ZskDTO> insertZskData(InsertZskDTO insertZskDTO) {
//        HttpHeaders headers = RestUtil.getHeaderApplicationJson();
//        headers.add("Authorization", token);
        String url = middleServiceUrl + MiddleServiceApi.Zsk.ZSK_INSERT_ZSK_DATA;

        ResponseEntity<JSONObject> result = RestUtil.request(
                url,
                HttpMethod.POST,
                null,
                null,
                insertZskDTO,
                JSONObject.class
        );
        if (result.getStatusCode() != HttpStatus.OK) {
            throw new JeecgBootException("插入向量库文档数据失败！");
        }
        JSONObject body = result.getBody();
        if (body == null) {
            throw new JeecgBootException("插入向量库文档数据失败！");
        }
        List<ZskDTO> bodyObject = JSON.parseArray(JSON.toJSONString(body.getObject("data", List.class)), ZskDTO.class);
        return bodyObject;
    }


    /**
     * 删除文档数据——向量库
     *
     * @param wjNm wjNm
     */
    public void delByWjNm(String tableName, String wjNm) {
        String url = middleServiceUrl + MiddleServiceApi.Zsk.ZSK_DEL_BY_WD_ID;

        ResponseEntity<JSONObject> result = RestUtil.request(
                url + "?wjNm=" + wjNm + "&tableName=" + tableName,
                HttpMethod.DELETE,
                null,
                null,
                null,
                JSONObject.class
        );
        if (result.getStatusCode() != HttpStatus.OK) {
            throw new JeecgBootException("插入向量库文档数据失败！");
        }
    }

    /**
     * 修改文档数据
     *
     * @param jsonObject wjNm
     */
    public void editWjNr(JSONObject jsonObject) {
        String url = middleServiceUrl + MiddleServiceApi.Zsk.ZSK_EDIT_WD_NR;
        HttpHeaders headers = RestUtil.getHeaderApplicationJson();
        ResponseEntity<JSONObject> result = RestUtil.request(
                url,
                HttpMethod.POST,
                headers,
                null,
                jsonObject,
                JSONObject.class
        );
        if (result.getStatusCode() != HttpStatus.OK) {
            throw new JeecgBootException("插入向量库文档数据失败！");
        }
    }


}
