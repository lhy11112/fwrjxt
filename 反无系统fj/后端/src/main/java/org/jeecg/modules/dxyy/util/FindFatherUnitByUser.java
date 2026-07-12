package org.jeecg.modules.dxyy.util;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.dxyy.entity.ZzllBd;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @Description:
 * @Author: 李海洋
 * @Date:   2024-07-31
 *
 */
@Slf4j
public class FindFatherUnitByUser {
    private List<ZzllBd> fatherUnit = new ArrayList<>();

    public List<ZzllBd> findFatherUnit(String bdnm, List<ZzllBd> unitListZd){

        if(bdnm==null){
            return fatherUnit;
        }

        for(ZzllBd unit: unitListZd ){
            if(bdnm.equals(unit.getBdxh())){
                fatherUnit.add(unit);
                List sast_temp = findFatherUnit(unit.getSjbdxh(),unitListZd);
                if (!sast_temp.isEmpty()){
                    if (log.isInfoEnabled()){
                        log.info("查询成功！！！");
                    }
                }
            }
        }
        return fatherUnit;
    }

}
