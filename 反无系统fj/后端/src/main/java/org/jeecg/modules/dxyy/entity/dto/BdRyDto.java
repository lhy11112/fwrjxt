package org.jeecg.modules.dxyy.entity.dto;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 部队人员
 * @Description: 部队人员
 * @Author: 李海洋
 * @Date:   2024-07-31
 *
 */
@Data
public class BdRyDto implements Serializable{
        private static final long serialVersionUID = 1L;

        private String bdnm;
        private String bdfh;
        private String bdjc;
        private int bzs;
        private int sys;
        private int zws;
        private int kcds;
}
