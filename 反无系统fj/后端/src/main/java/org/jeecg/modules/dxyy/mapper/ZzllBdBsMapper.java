package org.jeecg.modules.dxyy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.dxyy.entity.ZzllBdBs;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.dxyy.entity.dto.BdInfoByDl;

/**
 * @Description: zzll_bd_bs
 * @Author: jeecg-boot
 * @Date:   2024-04-26
 *
 */
public interface ZzllBdBsMapper extends BaseMapper<ZzllBdBs> {
    /**
     *  查询贵州的作战力量情况
     * @return List<ZzllBdBs>
     */
    @Select(" select * from zzll_bd_bs where BDNM in(           select id from sys_depart where del_flag = '0' and org_code like concat(#{orgCode},'%')\n  )   order by  TWSJ DESC\n ")
    List<ZzllBdBs>  getAllByBdFh(String orgCode);
    @Select("SELECT a1.*,a2.qiangxie from \n" +
            " (SELECT bdnm,bdfh,bdjc,jd,wd,distance, sum(IFNULL(kcds,0)) bingli FROM(\t\n" +
            "\tSELECT * from zzll_bd LEFT JOIN\n" +
            "\t\t(SELECT\n" +
            "\t\t\tBSLBNM,BDNM AS bdid,TWSJ,BSXSNM,DMNM,KZDM,JD,WD,\n" +
            "\t\t\tROUND(6378.138*2*ASIN(SQRT(POW(SIN((${wd}*PI()/180-wd*PI()/180)/2),2)+COS(${wd}*PI()/180)*COS(WD*PI()/180)*POW(SIN((${jd}*PI()/180-jd*PI()/180)/2),2)))) AS distance\n" +
            "\t\tFROM\n" +
            "\t\t\tzzll_bd_bs\n" +
            "\t\tHAVING distance<= ${dis}\n" +
            "\t\t) ss on zzll_bd.bdnm = ss.bdid \n" +
            "\t\tleft join (select bdnm as bdhm,KCDS FROM zzll_bd_ry ) a\n" +
            "\t\ton zzll_bd.BDNM = a.bdhm\n" +
            "\n" +
            "\tWHERE ss.distance is not null AND (zzll_bd.BDJC LIKE '%巡逻组' or zzll_bd.BDJC LIKE '%123456' or zzll_bd.BDJC LIKE '%大队' or zzll_bd.BDJC LIKE '%总队' or zzll_bd.BDJC LIKE '%支队') \n" +
            ") AS res  GROUP BY res.bdnm )a1\n" +
            " join \n" +
            "(SELECT bdnm,bdfh,bdjc,jd,wd,distance,SUM(IFNULL(kys,0)) qiangxie FROM(\t\n" +
            "\tSELECT * from zzll_bd LEFT JOIN\n" +
            "\t\t(SELECT\n" +
            "\t\t\tBSLBNM,BDNM AS bdid,TWSJ,BSXSNM,DMNM,KZDM,JD,WD,\n" +
            "\t\t\tROUND(6378.138*2*ASIN(SQRT(POW(SIN((${wd}*PI()/180-wd*PI()/180)/2),2)+COS(${wd}*PI()/180)*COS(WD*PI()/180)*POW(SIN((${jd}*PI()/180-jd*PI()/180)/2),2)))) AS distance\n" +
            "\t\tFROM\n" +
            "\t\t\tzzll_bd_bs\n" +
            "\t\tHAVING distance<=${dis}\n" +
            "\t\t) ss on zzll_bd.bdnm = ss.bdid \n" +
            "\t\tLEFT JOIN (SELECT zbnm,bdnm as bdhm,ROUND(kys) KYS from zzll_bd_zb where zbnm in(SELECT zbnm from zzbz_s_ty_zb where ZBFLNM = '01')) b\n" +
            "\t\ton zzll_bd.bdnm = b.bdhm\n" +
            "\n" +
            "\tWHERE ss.distance is not null AND (zzll_bd.BDJC LIKE '%巡逻组' or zzll_bd.BDJC LIKE '%123456' or zzll_bd.BDJC LIKE '%大队' or zzll_bd.BDJC LIKE '%总队' or zzll_bd.BDJC LIKE '%支队') \n" +
            ") AS res2 GROUP BY res2.bdnm)a2 \n" +
            " on a1.bdnm = a2.bdnm  ORDER BY  distance asc")
    List<BdInfoByDl> queryBdInfoByDl(@Param("wd")double wd, @Param("jd")double jd, @Param("dis")int dis);



    @Select("SELECT a1.*,a2.qiangxie from \n" +
            " (SELECT bdnm,bdfh,bdjc,jd,wd,distance, sum(IFNULL(kcds,0)) bingli FROM(\t\n" +
            "\tSELECT * from zzll_bd LEFT JOIN\n" +
            "\t\t(SELECT\n" +
            "\t\t\tBSLBNM,BDNM AS bdid,TWSJ,BSXSNM,DMNM,KZDM,JD,WD,\n" +
            "\t\t\tROUND(6378.138*2*ASIN(SQRT(POW(SIN((${wd}*PI()/180-wd*PI()/180)/2),2)+COS(${wd}*PI()/180)*COS(WD*PI()/180)*POW(SIN((${jd}*PI()/180-jd*PI()/180)/2),2)))) AS distance\n" +
            "\t\tFROM\n" +
            "\t\t\tzzll_bd_bs\n" +
            "\t\tHAVING distance<= ${dis}\n" +
            "\t\t) ss on zzll_bd.bdnm = ss.bdid \n" +
            "\t\tleft join (select bdnm as bdhm,KCDS FROM zzll_bd_ry ) a\n" +
            "\t\ton zzll_bd.BDNM = a.bdhm\n" +
            "\n" +
            "\tWHERE ss.distance is not null AND (  zzll_bd.BDJC LIKE '%中队' or   zzll_bd.BDJC LIKE '%支队' ) \n" +
            ") AS res  GROUP BY res.bdnm )a1\n" +
            " join \n" +
            "(SELECT bdnm,bdfh,bdjc,jd,wd,distance,SUM(IFNULL(kys,0)) qiangxie FROM(\t\n" +
            "\tSELECT * from zzll_bd LEFT JOIN\n" +
            "\t\t(SELECT\n" +
            "\t\t\tBSLBNM,BDNM AS bdid,TWSJ,BSXSNM,DMNM,KZDM,JD,WD,\n" +
            "\t\t\tROUND(6378.138*2*ASIN(SQRT(POW(SIN((${wd}*PI()/180-wd*PI()/180)/2),2)+COS(${wd}*PI()/180)*COS(WD*PI()/180)*POW(SIN((${jd}*PI()/180-jd*PI()/180)/2),2)))) AS distance\n" +
            "\t\tFROM\n" +
            "\t\t\tzzll_bd_bs\n" +
            "\t\tHAVING distance<=${dis}\n" +
            "\t\t) ss on zzll_bd.bdnm = ss.bdid \n" +
            "\t\tLEFT JOIN (SELECT zbnm,bdnm as bdhm,ROUND(kys) KYS from zzll_bd_zb where zbnm in(SELECT zbnm from zzbz_s_ty_zb where ZBFLNM = '01')) b\n" +
            "\t\ton zzll_bd.bdnm = b.bdhm\n" +
            "\n" +
            "\tWHERE ss.distance is not null AND (  zzll_bd.BDJC LIKE '%中队'  or   zzll_bd.BDJC LIKE '%支队' ) \n" +
            ") AS res2 GROUP BY res2.bdnm)a2 \n" +
            " on a1.bdnm = a2.bdnm  ORDER BY  distance asc")
    List<BdInfoByDl> QueryBdInfoZdByDl(@Param("wd")double wd, @Param("jd")double jd, @Param("dis")int dis);
}
