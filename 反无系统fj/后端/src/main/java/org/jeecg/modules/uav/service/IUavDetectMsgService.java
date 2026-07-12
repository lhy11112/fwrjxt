package org.jeecg.modules.uav.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.uav.entity.UavDetectMsg;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.uav.util.UavDatectMsgDto;
import org.jeecg.modules.uav.util.UavDatectMsgDto1;
import org.jeecg.modules.uav.vo.UavDetectMsgDateVo;
import org.jeecg.modules.uav.vo.UavDetectMsgVo;
import org.jeecg.modules.uav.vo.UavDetectMsgVo1;

import java.util.List;
import java.util.Map;

/**
 * @Description: 侦测报文数据表-飞行数据
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
public interface IUavDetectMsgService extends IService<UavDetectMsg> {
    /**
     * 根据设备站id、rq查询侦测到的无人机分组情况
     * @param StationId
     * @param rq
     * @return
     */
    List<UavDetectMsgVo> getUavDetectMsgByStationId( String StationId,String rq,String authStatus);
    /**
     * 根据型号、序列号、日期查询侦测到无人机详细数据
     * @param model
     * @param serial
     * @param rq
     * @return
     */
    UavDetectMsgVo1 getUavDetectMsgByModelSerialRq(String model, String serial, String rq);

    boolean generateFlightRoute(UavDatectMsgDto1 uavDatectMsgDto);
    List<Map<String, Object>> wrjfxcstj(String type, String stationId, String nf, String yf);

    IPage<UavDetectMsg> pageList(Page<UavDetectMsg> page, String type, String stationId, String nf, String yf, String brand , String model,String serial);

    /**
     * 根据年度月份查询日历数据
     * @param nf 年份
     * @param yf 月份
     * @return
     */
    List<UavDetectMsgDateVo> getUavDetectMsgDateByNfYf(String nf, String yf);
}
