package org.jeecg.modules.uav.config;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.AjaxResult;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.modules.dxyy.entity.ZzllBd;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.system.mapper.WjbdMhYyczLogMapper;
import org.jeecg.modules.uav.constant.WjbdWrjGjjlType;
import org.jeecg.modules.uav.entity.UavDetectMsg;
import org.jeecg.modules.uav.entity.UavDeviceConfig;
import org.jeecg.modules.uav.mapper.UavConnectLogMapper;
import org.jeecg.modules.uav.mapper.UavDetectMsgMapper;
import org.jeecg.modules.uav.mapper.UavDeviceConfigMapper;
import org.jeecg.modules.uav.mapper.UavOperateLogMapper;
import org.jeecg.modules.uav.model.TcpUdpCommand;
import org.jeecg.modules.uav.service.IUavDeviceConfigService;
import org.jeecg.modules.uav.socket.SocketManager;
import org.jeecg.modules.uav.service.HeartbeatService;
import org.jeecg.modules.wrj.entity.WjbdWrjGjjl;
import org.jeecg.modules.wrj.entity.WjbdWrjHbmdsq;
import org.jeecg.modules.wrj.entity.WjbdWrjJbxx;
import org.jeecg.modules.wrj.entity.WjbdWrjKy;
import org.jeecg.modules.wrj.mapper.WjbdWrjGjjlMapper;
import org.jeecg.modules.wrj.mapper.WjbdWrjHbmdsqMapper;
import org.jeecg.modules.wrj.mapper.WjbdWrjJbxxMapper;
import org.jeecg.modules.wrj.service.IWjbdWrjGjjlService;
import org.jeecg.modules.wrj.service.IWjbdWrjJbxxService;
import org.jeecg.modules.wrj.service.IWjbdWrjKyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 定时任务
 * @Author: 李海洋
 * @Date:   2026-02-28
 */
@Component
@Slf4j
public class ScheduledConfig {

    @Autowired
    private SocketManager socketManager;

    @Autowired
    private HeartbeatService heartbeatService;

    @Autowired
    private IWjbdWrjGjjlService wjbdWrjGjjlService;

    @Autowired
    private WebSocket webSocket;

    @Autowired
    private IWjbdWrjKyService wjbdWrjKyService;
    @Resource
    private UavDetectMsgMapper uavDetectMsgMapper;
    @Resource
    private WjbdWrjJbxxMapper wjbdWrjJbxxMapper;
    @Autowired
    private IWjbdWrjJbxxService iWjbdWrjJbxxService;
    @Resource
    private WjbdWrjHbmdsqMapper wjbdWrjHbmdsqMapper;
    @Resource
    private UavDeviceConfigMapper uavDeviceConfigMapper;
    @Autowired
    private IWjbdWrjGjjlService iWjbdWrjGjjlService;
    @Resource
    private UavOperateLogMapper uavOperateLogMapper;
    @Resource
    private UavConnectLogMapper uavConnectLogMapper;
    @Resource
    private WjbdMhYyczLogMapper wjbdMhYyczLogMapper;
    /**
     * 定时任务SQL（示例：每小时执行一次增量更新）
     */
    @Scheduled(fixedRate = 10000*6*60)
    public void updateCacheuav_alarm_group_cache() throws Exception {
        log.info("===== 定时任务SQL（示例：每小时执行一次增量更新） =====");
        uavDetectMsgMapper.updateCacheuav_alarm_group_cache();
    }
    /**
     * 定时检查设备配置变化（每一分钟）
     */
    @Scheduled(fixedRate = 10000*6)
    public void checkDeviceConfigChanges() throws Exception {
        log.info("===== 定时检查设备配置变化（每一分钟） =====");

        socketManager.refreshDeviceConnectionsTCPSocket();
        socketManager.refreshDeviceConnectionsTCPServerSocket();

        //定时调用获取设备列表
        //TcpUdpCommand.getDeviceList();
    }

    /**
     * 验证无人机状态,默认更新当日无人机状态情况10秒
     */
    @Scheduled(fixedRate = 10000)
    public void VerifyUavStatus(){
        log.info("===== 验证无人机状态,默认更新当日无人机状态情况 =====");

        List<WjbdWrjJbxx> wjbdWrjJbxxList=new ArrayList<WjbdWrjJbxx>();
        List<UavDetectMsg> uavDetectMsgList=uavDetectMsgMapper.getTodayUavData();
        List<UavDetectMsg> uavDetectMsgList1=uavDetectMsgMapper.getRecent1MinUavData();
        if (!uavDetectMsgList.isEmpty()){
            uavDetectMsgList.stream().forEach(item->{
                WjbdWrjJbxx wjbdWrjJbxx=wjbdWrjJbxxMapper.getWrjJbxxBySerialNumber(item.getSerial());
                if (wjbdWrjJbxx!=null){
                    //默认设置为失联状态
                    wjbdWrjJbxx.setStatus(3);
                    wjbdWrjJbxxList.add(wjbdWrjJbxx);
                }
            });
        }
        if (!uavDetectMsgList1.isEmpty()){
            uavDetectMsgList1.stream().forEach(item->{
                WjbdWrjJbxx wjbdWrjJbxx=wjbdWrjJbxxMapper.getWrjJbxxBySerialNumber(item.getSerial());
                if (wjbdWrjJbxx!=null&&!wjbdWrjJbxxList.isEmpty()){
                    List<WjbdWrjJbxx> wjbdWrjJbxxList1=wjbdWrjJbxxList.stream().filter(items->items.getSerialNumber().equals(item.getSerial())).collect(Collectors.toList());
                    if (!wjbdWrjJbxxList1.isEmpty()){
                        wjbdWrjJbxxList1.stream().forEach(itemss->{
                            //如果能查询一分钟内的无人机数据,则默认状态为正常
                            itemss.setStatus(1);
                        });
                    }
                }
            });
        }
        if(!wjbdWrjJbxxList.isEmpty()){
            //批量更新无人机基本信息表中的状态
            iWjbdWrjJbxxService.updateBatchById(wjbdWrjJbxxList);
        }
    }

    /**
     * 定时发送心跳包（每5秒）
     */
    @Scheduled(fixedRate = 5000)
    public void sendHeartbeats() {
        log.info("===== 定时发送心跳包（每5秒） =====");
        heartbeatService.sendHeartbeatsToAllDevices();
    }

    /**
     * 定时发送空域告警消息（每5秒）
     */
    //@Scheduled(fixedRate = 1000)
    public  void sendWebSocket() throws JsonProcessingException {
        log.info("===== 定时发送空域告警消息（每1秒） =====");
        // 用try-catch包裹所有逻辑，避免任务终止
        try {
            synchronized (this) { // 缩小锁粒度，仅锁定当前实例
                //dobusiness();
            }
        } catch (Exception e) {
            log.error("空域告警定时任务执行失败", e); // 记录完整异常栈
        }
    }


    /**
     * 定时清除操作日志（2年前）、设备连接日志（一周前）、设备操作日志（一周前）
     */
    //@Scheduled(cron = "0/5 * * * * ?")
   // @Scheduled(cron = "0 30 23 * * ?")
    @Scheduled(fixedDelay = 10000*6*60*12)
    public  void clearData() {
        log.info("===== 定时任务开始执行 =====");
        delete();
    }

    public void  delete(){
        log.info("===== 开始执行日志清理定时任务，删除uav_operate_log表中一周前的数据 =====");

        //删除设备操作数据
        uavOperateLogMapper.deleteINTERVAL1Week();

        log.info("===== 开始执行日志清理定时任务，删除uav_connect_log表中一周前的数据 =====");

        //删除设备连接日志
        uavConnectLogMapper.deleteINTERVAL1Week();
        log.info("===== 开始执行日志清理定时任务，删除wjbd_mh_yycz_log表中2年前的数据 =====");

        //删除系统操作日志
        wjbdMhYyczLogMapper.deleteINTERVAL2YEAR();
        log.info("===== 日志清理定时任务执行完成");
    }


    /**
     * 检查空域内未知无人机情况并告警
     */
    public void dobusiness(){
        //获取无人机空域信息
        List<WjbdWrjKy> wjbdWrjKyList = wjbdWrjKyService.list();
        List<WjbdWrjGjjl> wjbdWrjGjjls = new ArrayList<WjbdWrjGjjl>();
        //遍历所有空域数据
        wjbdWrjKyList.stream().forEach(item -> {
            WjbdWrjGjjl wjbdWrjGjjl = new WjbdWrjGjjl();
            UavDetectMsg uavDetectMsg = uavDetectMsgMapper.getUavDetectMsgByJwdJl(item.getZxdjd(), item.getZxdwd(), item.getBj());
            if (uavDetectMsg==null){
                //根据空域查询无人机信息为空
                return;
            }else{
                UavDetectMsg uavDetectMsg3 = uavDetectMsgMapper.getUavDetectMsgByJwdJl(item.getZxdjd(), item.getZxdwd(), item.getJfqbj());
                if (uavDetectMsg3==null){
                    UavDetectMsg uavDetectMsg5 = uavDetectMsgMapper.getUavDetectMsgByJwdJl(item.getZxdjd(), item.getZxdwd(), item.getYjqbj());
                    if (uavDetectMsg5==null){
                        wjbdWrjGjjl.setGjys(item.getYs());
                    }else{
                        wjbdWrjGjjl.setGjys(item.getYjqys());
                    }
                }else{
                    wjbdWrjGjjl.setGjys(item.getJfqys());
                }
            }
            UavDeviceConfig uavDeviceConfig= uavDeviceConfigMapper.selectDevicesByStaticId(uavDetectMsg.getStationId());
            List<WjbdWrjJbxx> wjbdWrjJbxxListHmd = wjbdWrjJbxxMapper.getHmdList().stream().filter(items -> items.getModel().equals(uavDetectMsg.getModel()) && items.getSerialNumber().equals(uavDetectMsg.getSerial())).collect(Collectors.toList());
            List<WjbdWrjJbxx> wjbdWrjJbxxListBmd = wjbdWrjJbxxMapper.getBmdList().stream().filter(items -> items.getModel().equals(uavDetectMsg.getModel()) && items.getSerialNumber().equals(uavDetectMsg.getSerial())).collect(Collectors.toList());
            wjbdWrjGjjl.setWrjxlh(uavDetectMsg.getSerial());
            if (!wjbdWrjJbxxListBmd.isEmpty()) {
                //白名单飞机,不告警

            }
            if (!wjbdWrjJbxxListHmd.isEmpty()) {
                //黑名单告警
                wjbdWrjGjjl.setKymc(item.getMc());
                wjbdWrjGjjl.setKyid(item.getId());
                wjbdWrjGjjl.setWrjid(wjbdWrjJbxxListHmd.get(0).getId());
                wjbdWrjGjjl.setGjlx(WjbdWrjGjjlType.GJLX1);
                wjbdWrjGjjl.setGjfsjd(new BigDecimal(uavDetectMsg.getDronLng()));
                wjbdWrjGjjl.setGjfswd(new BigDecimal(uavDetectMsg.getDronLat()));
                wjbdWrjGjjl.setGjfsgd(uavDetectMsg.getAltitude());
                wjbdWrjGjjl.setGjfssj(new Date());
                wjbdWrjGjjl.setClzt(WjbdWrjGjjlType.CLZT2);
                wjbdWrjGjjl.setClsj(new Date());
                wjbdWrjGjjl.setClbz(WjbdWrjGjjlType.CLBZ1);
                wjbdWrjGjjl.setWrjpp(wjbdWrjJbxxListHmd.get(0).getBrand());
                wjbdWrjGjjl.setWrjxh(wjbdWrjJbxxListHmd.get(0).getModel());
                wjbdWrjGjjl.setFsjd(uavDetectMsg.getPilotLng());
                wjbdWrjGjjl.setFswd(uavDetectMsg.getPilotLat());
                wjbdWrjGjjl.setZdid(uavDeviceConfig.getId().toString());
                wjbdWrjGjjl.setZdmc(uavDeviceConfig.getName());
                wjbdWrjGjjls.add(wjbdWrjGjjl);
            }

            if (wjbdWrjJbxxListHmd.isEmpty() && wjbdWrjJbxxListBmd.isEmpty()) {
                //不在黑白名单内,默认加入黑名单中
                List<WjbdWrjJbxx> wjbdWrjJbxxList = wjbdWrjJbxxMapper.selectList(null);
                List<WjbdWrjJbxx> wjbdWrjJbxxList1 = wjbdWrjJbxxList.stream().filter(items -> items.getModel().equals(uavDetectMsg.getModel()) && items.getSerialNumber().equals(uavDetectMsg.getSerial())).collect(Collectors.toList());
                WjbdWrjJbxx wjbdWrjJbxx = new WjbdWrjJbxx();
                //无人机存在的话将无人机直接加入黑名单中
                if (!wjbdWrjJbxxList1.isEmpty()) {
                    WjbdWrjHbmdsq wjbdWrjHbmdsq = new WjbdWrjHbmdsq();
                    wjbdWrjHbmdsq.setWrjid(wjbdWrjJbxxList1.get(0).getId());
                    wjbdWrjHbmdsq.setMdlx("黑名单");
                    wjbdWrjHbmdsqMapper.insert(wjbdWrjHbmdsq);
                    wjbdWrjGjjl.setWrjpp(wjbdWrjJbxxList1.get(0).getBrand());
                } else {
                    //不存在的话创建无人机数据、加黑名单
                    wjbdWrjJbxx.setBrand("未知");
                    wjbdWrjJbxx.setSerialNumber(uavDetectMsg.getSerial());
                    wjbdWrjJbxx.setModel(uavDetectMsg.getModel());
                    wjbdWrjJbxx.setAuthStatus(2);
                    wjbdWrjJbxx.setStatus(1);
                    wjbdWrjJbxx.setCurrentLongitude(new BigDecimal(uavDetectMsg.getDronLng()));
                    wjbdWrjJbxx.setCurrentLatitude(new BigDecimal(uavDetectMsg.getDronLat()));
                    wjbdWrjJbxxMapper.insert(wjbdWrjJbxx);
                    WjbdWrjHbmdsq wjbdWrjHbmdsq = new WjbdWrjHbmdsq();
                    wjbdWrjHbmdsq.setWrjid(wjbdWrjJbxx.getId());
                    wjbdWrjHbmdsq.setMdlx("黑名单");
                    wjbdWrjHbmdsqMapper.insert(wjbdWrjHbmdsq);
                    wjbdWrjGjjl.setWrjpp("未知");
                }
                //黑名单告警
                wjbdWrjGjjl.setKymc(item.getMc());
                wjbdWrjGjjl.setKyid(item.getId());
                wjbdWrjGjjl.setWrjid(wjbdWrjJbxx.getId());
                wjbdWrjGjjl.setGjlx(WjbdWrjGjjlType.GJLX2);
                wjbdWrjGjjl.setGjfsjd(new BigDecimal(uavDetectMsg.getDronLng()));
                wjbdWrjGjjl.setGjfswd(new BigDecimal(uavDetectMsg.getDronLat()));
                wjbdWrjGjjl.setGjfssj(new Date());
                wjbdWrjGjjl.setClzt(WjbdWrjGjjlType.CLZT2);
                wjbdWrjGjjl.setClsj(new Date());
                wjbdWrjGjjl.setClbz(WjbdWrjGjjlType.CLBZ2);
                wjbdWrjGjjl.setFsjd(uavDetectMsg.getPilotLng());
                wjbdWrjGjjl.setFswd(uavDetectMsg.getPilotLat());
                wjbdWrjGjjl.setZdid(uavDeviceConfig.getId().toString());
                wjbdWrjGjjl.setZdmc(uavDeviceConfig.getName());
                wjbdWrjGjjl.setGjfsgd(uavDetectMsg.getAltitude());
                wjbdWrjGjjl.setWrjxh(uavDetectMsg.getModel());
                wjbdWrjGjjls.add(wjbdWrjGjjl);
            }
        });
        if (!wjbdWrjGjjls.isEmpty()) {
            //插入告警记录
            iWjbdWrjGjjlService.saveBatch(wjbdWrjGjjls);
            log.info("---------------------开始获取发送---------------------");
            WjbdWrjGjjl wjbdWrjGjjl = wjbdWrjGjjls.get(0);
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(wjbdWrjGjjl);
            AjaxResult<String> result = new AjaxResult<String>();
            String message = jsonObject.toJSONString();
            JSONObject obj = new JSONObject();
            obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
            obj.put(WebsocketConst.MSG_ID, "M0001");
            obj.put(WebsocketConst.MSG_TXT, message);
            webSocket.sendMessage(obj.toJSONString());
            result.setResult("群发！");
            log.info("----------------------结束发送--------------------------");
        }
    }


}
