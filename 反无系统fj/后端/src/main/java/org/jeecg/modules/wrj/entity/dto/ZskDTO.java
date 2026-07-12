package org.jeecg.modules.wrj.entity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 *
 * @author 冯程骞
 * @date 2025/3/15
 **/
@EqualsAndHashCode
@Data
@ToString
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ZskDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    @JsonSerialize(using = ToStringSerializer.class, nullsUsing = NullSerializer.class)
    private Long docId;
    private String content;
    private String docName;
    private String docCode;
    private String docUrl;
}
