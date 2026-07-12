package org.jeecg.modules.dxyy.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.dxyy.entity.ZzllBd;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: zzll_bd
 * @Author: jeecg-boot
 * @Date:   2024-04-26
 *
 */
public interface ZzllBdMapper extends BaseMapper<ZzllBd> {
    //@Select("select * from (select  BDNM,BDjc as title,BDJC,BDXH,SJBDNM from zzll_bd where SJBDNM IS NOT NULL)rd,(select @pid:=#{bdnm})pd where FIND_IN_SET(sjbdnm,@pid)>0 AND @pid:= CONCAT(@pid,',',bdnm) order by bdxh asc")
    //@Select("select * from (select  BDNM,BDjc as title,BDJC,BDXH,SJBDNM from zzll_bd where SJBDNM IS NOT NULL)rd,(select @pid:=#{bdnm})pd where FIND_IN_SET(CONVERT(sjbdnm USING utf8mb4),@pid)>0 AND @pid:= CONCAT(@pid,',',bdnm) order by bdxh asc")
    @Select(" select *  from dbsjgx_zzll_bd where BDXH  LIKE  concat((select BDXH from dbsjgx_zzll_bd where BDNM=#{bdnm})\n" +
            ", '%') ")
    List<ZzllBd> selectChildUnit(@Param("bdnm") String bdnm);
    @Select("select BDNM,BDHFNM,BDXH,bdjc as bdfh,BDJC,IFNULL(null,0) as SJBDNM from dbsjgx_zzll_bd order by bdxh asc ")
    List<ZzllBd> selectAllUnit();

    @Select("select bdnm,bdhfnm,bdxh,REPLACE(bdfh,'中国人民武装警察部队','') as  bdfh,bdfh,bdjc,sjbdnm from dbsjgx_zzll_bd where bdnm in (${bdnmlist}) order by bdxh asc ")
    List<ZzllBd> selectBdnm(@Param("bdnmlist") String bdnmlist);

    @Select("  select  * from dbsjgx_zzll_bd order by bdxh asc ")
    //@Select("select * from (select  BDNM,BDjc as title,BDJC,BDXH,SJBDNM from zzll_bd where SJBDNM IS NOT NULL)rd,(select @pid:=(select bdnm from zzll_bd where SJBDNM is null))pd where FIND_IN_SET(CONVERT(sjbdnm USING utf8mb4),@pid)>0 AND @pid:= CONCAT(@pid,',',bdnm) order by bdxh asc")
    List<ZzllBd>  selectZongdui();
}
