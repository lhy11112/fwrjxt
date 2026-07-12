package org.jeecg.modules.uav.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.modules.uav.dto.ModelSerialDTO;
import org.jeecg.modules.uav.entity.UavDetectMsg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.uav.entity.UavDfData;
import org.jeecg.modules.uav.vo.UavDetectMsgDateVo;
import org.jeecg.modules.uav.vo.UavDetectMsgVo;
import org.jeecg.modules.wrj.entity.WjbdWrjZymb;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface UavDetectMsgMapper extends BaseMapper<UavDetectMsg> {

    
    UavDetectMsg selectLatestByStationId(
            @Param("stationId") Integer stationId,
            @Param("beforeTime") LocalDateTime beforeTime
    );

    /**
     * 根据站id查询该设备下侦测到的无人机分组情况
     * @param StationId
     * @return
     */
    List<UavDetectMsgVo>  getUavDetectMsgByStationId(@Param("StationId") String StationId,@Param("rq")String rq,@Param("authStatus")String authStatus);

    /**
     * 根据型号、序列号、日期查询侦测到无人机详细数据
     * @param model
     * @param serial
     * @param rq
     * @return
     */
    List<UavDetectMsg>  getUavDetectMsgByModelSerialRq(@Param("model")String model,@Param("serial")String serial,@Param("rq")String rq);
    /**
     * 根据型号、序列号、日期查询侦测到无人机详细数据
     * @param model
     * @param serial
     * @param rq
     * @return
     */
    UavDetectMsg  getUavDetectMsgByModelSerialRqlimit1(@Param("model")String model,@Param("serial")String serial,@Param("rq")String rq);

    // ========== 新增批量查询方法（适配优化后的逻辑） ==========
    /**
     * 批量根据型号+序列号组合、日期查询侦测到无人机详细数据
     * @param modelSerialList 型号+序列号组合集合对象（格式："model_serial"，如"大疆M300_123456"）
     * @param rq 日期（格式：YYYY-MM-DD）
     * @return 每个型号+序列号组合对应的最新1条无人机侦测数据列表
     */
    List<UavDetectMsg> batchGetUavDetectMsgByModelSerialRq(
            @Param("modelSerialList") List<ModelSerialDTO> modelSerialList,
            @Param("rq") String rq
    );

    UavDfData getUavUavDfDataModelSerialRqlimit1(@Param("model")String model, @Param("serial")String serial, @Param("rq")String rq);


    UavDetectMsg getUavDetectMsgByJwdJl(@Param("jd") BigDecimal jd, @Param("wd")BigDecimal wd, @Param("jl")BigDecimal jl);
    List<Map<String, Object>> wrjfxcstj(@Param("type")String type, @Param("stationId")String stationId, @Param("nf")String nf, @Param("yf")String yf);

    IPage<UavDetectMsg> pageList(Page<UavDetectMsg> page, @Param("type")String type,
                                 @Param("stationId")String stationId, @Param("nf")String nf, @Param("yf")String yf
            , @Param("brand")String brand, @Param("model")String model,@Param("serial")String serial);

    /**
     * 查询当日无人机数据
     * @return
     */
    @Select("select model,serial from (select * from uav_detect_msg where  DATE_FORMAT(data_time, '%Y-%m-%d')=DATE_FORMAT(NOW(), '%Y-%m-%d'))t\n" +
            "group by  t.model,t.serial")
    List<UavDetectMsg> getTodayUavData();

    /**
     * 当日无人机飞行数据近1分钟内的数据
     * @return
     */
    @Select("select model,serial from (select * from uav_detect_msg where  data_time>=DATE_SUB(NOW(),INTERVAL 1 MINUTE))t\n" +
            "group by  t.model,t.serial")
    List<UavDetectMsg> getRecent1MinUavData();

    /**
     * 根据年度月份查询日历数据
     * @param nf 年份
     * @param yf 月份
     * @return
     */
    List<UavDetectMsgDateVo> getUavDetectMsgDateByNfYf(String nf, String yf);

    @Update(" REPLACE INTO uav_alarm_group_cache (serial, model, data_time, alarm_group_id)\n" +
            "SELECT \n" +
            "    t.serial, t.model, t.data_time, t.alarm_group_id\n" +
            "FROM (\n" +
            "    SELECT\n" +
            "        td.serial, td.model, td.data_time,\n" +
            "        sum(CASE WHEN td.diff_seconds > 1200 THEN 1 ELSE 0 END) \n" +
            "            OVER (PARTITION BY td.serial, td.model ORDER BY td.data_time) AS alarm_group_id\n" +
            "    FROM (\n" +
            "        SELECT\n" +
            "            u.serial, u.model, u.data_time,\n" +
            "            TIMESTAMPDIFF(\n" +
            "                SECOND,\n" +
            "                LAG(u.data_time, 1, u.data_time) OVER (PARTITION BY u.serial, u.model ORDER BY u.data_time),\n" +
            "                u.data_time\n" +
            "            ) AS diff_seconds\n" +
            "        FROM uav_detect_msg u\n" +
            "        -- 增量更新：只计算上次更新后的数据\n" +
            "        WHERE u.data_time > (SELECT IFNULL(MAX(data_time), '1970-01-01') FROM uav_alarm_group_cache)\n" +
            "    ) td\n" +
            ") t")
    void updateCacheuav_alarm_group_cache();
}
    