package org.jeecg.modules.wrj.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 名称:知识库与文件的关系，文件存在哪个向量里面对象 wjbd_qxjy_zsk_wj
 *
 * @author xin
 * @date 2024-08-05
 */
@EqualsAndHashCode
@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ZskWjVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 知识库主键
     */
    private Long zskId;

    /**
     * 文件名称
     */
    private String wjMc;

    /**
     * 文件内码
     */
    private String wjNm;

    /**
     * 文件大小，单位是字节
     */
    private Long wjdx;

    /**
     * 文件存储的绝对路径，url
     */
    private String fwqWjlj;

    /**
     * 创建人名称
     */
    private String chjrMc;

    /**
     * 创建人id
     */
    private String chjr;

    /**
     * 入库时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date rksj;

    /**
     * 存储文件的向量库的表明
     */
    private String tableName;
}
