package org.jeecg.modules.wrj.utils;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 冯程骞
 */
@Component
@Slf4j
public class FileUtil {

    @Value("${jeecg.path.upload}")
    private String upLoadPath;


    /**
     * 获取文件路径
     *
     * @param path path
     * @return 结果
     */
    public String getProjectFilePath(String path) {
        String finalPath = null;
        String path1 = (upLoadPath + File.separator + path).replaceAll("//", "/");
        if (new File(path1).exists()) {
            finalPath = path1;
        }

        //路径不存在
        String path2 = (upLoadPath + File.separator + "temp" + File.separator + path1).replaceAll("//", "/");
        if (new File(path2).exists()) {
            finalPath = path2;
        }

        if (finalPath == null) {
            throw new JeecgBootException("文件不存在！请重新上传！");
        }

        return finalPath;
    }


    /**
     * 上传文件时获取文件名称
     *
     * @param filePaths filePaths
     * @return 结果
     */
    public String getFileNamesByUpLoad(String filePaths) {
        if (StrUtil.isBlank(filePaths)) {
            return null;
        }
        String[] filePathArr = filePaths.split(",");
        List<String> fileNameList = new ArrayList<>();
        for (String fj : filePathArr) {
            String fileName = fj.substring(fj.lastIndexOf('/') + 1, fj.lastIndexOf('_')) + fj.substring(fj.lastIndexOf('.'));
            fileNameList.add(fileName);
        }
        return CollectionUtil.join(fileNameList, ",");
    }

}
