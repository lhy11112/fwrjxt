package org.jeecg.modules.system.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Lenovo62
 */
@Data
public class ChatContentExportRequest {

    private List<String> chatContentList;
    /**
     * 模板ID
     */
    private String wjmc;
}
