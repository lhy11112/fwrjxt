package org.jeecg.modules.wrj.entity.dto;


/**
 * @author 冯程骞
 * @date 2024/9/12 19:11
 */

import lombok.Data;
import org.jeecg.modules.wrj.entity.vo.ZskWjVO;

import java.io.Serializable;

@Data
public class InsertZskDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 知识库内码
     */
    private String zskNm;

    /**
     * 知识库文件VO
     */
    private ZskWjVO zskWjVO;
}
