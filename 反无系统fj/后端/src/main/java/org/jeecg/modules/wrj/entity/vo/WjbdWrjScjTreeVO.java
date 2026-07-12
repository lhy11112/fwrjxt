package org.jeecg.modules.wrj.entity.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;
@Data
public class WjbdWrjScjTreeVO {
    /**
     * 主键ID
     */
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long id;

    /**
     * 用户ID
     */
    private String yhId;

    /**
     * 名称（目录/视点）
     */
    private String mc;

    /**
     * 类型：视点、目录
     */
    private String type;

    /**
     * 上级ID
     */
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long pid;

    /**
     * JSON值
     */
    private String csz;

    /**
     * 创建人
     */
    private String chjr;

    /**
     * 创建人名称
     */
    private String chjrMc;

    /**
     * 入库时间
     */
    private String rksj;

    /**
     * 子节点列表
     */
    private List<WjbdWrjScjTreeVO> children;
}
