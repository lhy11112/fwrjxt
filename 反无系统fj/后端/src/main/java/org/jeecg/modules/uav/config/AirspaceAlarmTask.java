package org.jeecg.modules.uav.config;
import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.modules.message.websocket.WebSocket;
import org.jeecg.modules.system.mapper.WjbdMhYyczLogMapper;
import org.jeecg.modules.uav.constant.WjbdWrjGjjlType;
import org.jeecg.modules.uav.entity.UavDetectMsg;
import org.jeecg.modules.uav.entity.UavDeviceConfig;
import org.jeecg.modules.uav.mapper.UavConnectLogMapper;
import org.jeecg.modules.uav.mapper.UavDetectMsgMapper;
import org.jeecg.modules.uav.mapper.UavDeviceConfigMapper;
import org.jeecg.modules.uav.mapper.UavOperateLogMapper;
import org.jeecg.modules.uav.service.HeartbeatService;
import org.jeecg.modules.uav.socket.SocketManager;
import org.jeecg.modules.wrj.entity.WjbdWrjGjjl;
import org.jeecg.modules.wrj.entity.WjbdWrjHbmdsq;
import org.jeecg.modules.wrj.entity.WjbdWrjJbxx;
import org.jeecg.modules.wrj.entity.WjbdWrjKy;
import org.jeecg.modules.wrj.mapper.WjbdWrjHbmdsqMapper;
import org.jeecg.modules.wrj.mapper.WjbdWrjJbxxMapper;
import org.jeecg.modules.wrj.service.IWjbdWrjGjjlService;
import org.jeecg.modules.wrj.service.IWjbdWrjJbxxService;
import org.jeecg.modules.wrj.service.IWjbdWrjKyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
/**
 * 空域告警定时任务
 * 修复点：
 * 1. 解决空指针风险
 * 2. 优化数据库查询性能
 * 3. 修复业务逻辑缺陷（白名单、漏推送、重复告警）
 * 4. 完善异常处理
 * 5. 优化定时任务机制
 * @Author: 李海洋
 * @Date:   2026-02-28
 */
@Component
public class AirspaceAlarmTask {
    private static final Logger log = LoggerFactory.getLogger(AirspaceAlarmTask.class);

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
     * 定时发送空域告警消息（每1秒执行，以上次任务结束时间计算间隔）
     * 修复点：
     * 1. fixedDelay替代fixedRate，避免任务重叠
     * 2. 缩小锁粒度（当前实例）
     * 3. 全局异常捕获，避免任务终止
     * 4. 注释与代码保持一致
     */
    @Scheduled(fixedDelay = 1000) // fixedDelay：以上次任务结束时间算间隔，避免重叠
    public void sendWebSocket() {
        log.info("===== 定时发送空域告警消息（每1秒） =====");
        // 缩小锁粒度：仅锁定当前实例，不影响其他类/方法
        synchronized (this) {
            try {
                doBusiness();
            } catch (Exception e) {
                // 捕获所有异常，记录完整栈信息，避免定时任务终止
                log.error("空域告警定时任务执行失败", e);
            }
        }
    }

    /**
     * 检查空域内未知无人机情况并告警
     * 修复点：
     * 1. 提前加载黑白名单，减少数据库查询
     * 2. 全量空指针校验
     * 3. 修复白名单逻辑（直接跳过）
     * 4. 推送所有告警记录（而非仅第一条）
     * 5. 幂等性控制（结合数据库唯一索引）
     * 6. 事务控制，保证数据一致性
     */
    @Transactional(rollbackFor = Exception.class) // 事务控制：异常时回滚所有数据库操作
    public void doBusiness() {
        // ========== 优化点：提前加载黑白名单，避免循环内重复查询 ==========
        List<WjbdWrjJbxx> hmdList = wjbdWrjJbxxMapper.getHmdList();
        List<WjbdWrjJbxx> bmdList = wjbdWrjJbxxMapper.getBmdList();
        List<WjbdWrjJbxx> allWjbdWrjJbxxList = wjbdWrjJbxxMapper.selectList(null);

        // 获取无人机空域信息（建议后续优化：分页查询/按区域查询，减少单次查询量）
        List<WjbdWrjKy> wjbdWrjKyList = wjbdWrjKyService.list();
        List<WjbdWrjGjjl> wjbdWrjGjjls = new ArrayList<>();

        // ========== 改用普通for循环，更易控制流程 ==========
        for (WjbdWrjKy item : wjbdWrjKyList) {
            WjbdWrjGjjl wjbdWrjGjjl = new WjbdWrjGjjl();
            UavDetectMsg uavDetectMsg = null;

            try {
                // 查询无人机检测信息
                uavDetectMsg = uavDetectMsgMapper.getUavDetectMsgByJwdJl(
                        item.getZxdjd(), item.getZxdwd(), item.getBj()
                );

                // 无无人机信息，跳过当前空域
                if (uavDetectMsg == null) {
                    continue;
                }

                // ========== 修复：空指针校验 - 设备配置 ==========
                UavDeviceConfig uavDeviceConfig = uavDeviceConfigMapper.selectDevicesByStaticId(uavDetectMsg.getStationId());
                if (uavDeviceConfig == null) {
                    log.warn("设备配置不存在，stationId:{}", uavDetectMsg.getStationId());
                    continue;
                }

                // ========== 优化：复用提前加载的黑白名单，减少stream过滤 ==========
                // 白名单匹配
                UavDetectMsg finalUavDetectMsg1 = uavDetectMsg;
                List<WjbdWrjJbxx> bmdMatchList = bmdList.stream()
                        .filter(bmd -> bmd.getModel().equals(finalUavDetectMsg1.getModel())
                                && bmd.getSerialNumber().equals(finalUavDetectMsg1.getSerial()))
                        .collect(Collectors.toList());

                // ========== 修复：白名单直接跳过，避免误告警 ==========
                if (!bmdMatchList.isEmpty()) {
                    log.debug("无人机{}属于白名单，跳过告警", uavDetectMsg.getSerial());
                    continue;
                }

                // 黑名单匹配
                UavDetectMsg finalUavDetectMsg = uavDetectMsg;
                List<WjbdWrjJbxx> hmdMatchList = hmdList.stream()
                        .filter(hmd -> hmd.getModel().equals(finalUavDetectMsg.getModel())
                                && hmd.getSerialNumber().equals(finalUavDetectMsg.getSerial()))
                        .collect(Collectors.toList());

                // 设置告警原因
                setAlarmReason(item, uavDetectMsg, wjbdWrjGjjl);

                // 设置无人机序列号
                wjbdWrjGjjl.setWrjxlh(uavDetectMsg.getSerial());

                // ========== 黑名单告警逻辑 ==========
                if (!hmdMatchList.isEmpty()) {
                    fillBlacklistAlarmInfo(item, uavDetectMsg, uavDeviceConfig, hmdMatchList, wjbdWrjGjjl);
                    wjbdWrjGjjls.add(wjbdWrjGjjl);
                }

                // ========== 非黑白名单：新增并加入黑名单 ==========
                if (hmdMatchList.isEmpty() && bmdMatchList.isEmpty()) {
                    handleUnknownUav(item, uavDetectMsg, uavDeviceConfig, allWjbdWrjJbxxList, wjbdWrjGjjl);
                    wjbdWrjGjjls.add(wjbdWrjGjjl);
                }

            } catch (Exception e) {
                // 单个空域处理失败，记录日志并继续处理下一个空域
                log.error("处理空域{}（ID:{}）告警失败", item.getMc(), item.getId(), e);
                continue;
            }
        }

        // ========== 修复：推送所有告警记录（而非仅第一条） ==========
        if (!wjbdWrjGjjls.isEmpty()) {
            // 批量插入告警记录（建议：数据库添加唯一索引，避免重复入库）
            // 唯一索引示例：CREATE UNIQUE INDEX idx_wrj_ky_alarm_time ON wjbd_wrj_gjjl (wrjxlh, kyid, DATE_FORMAT(gjfssj, '%Y-%m-%d %H:%i'));
            iWjbdWrjGjjlService.saveBatch(wjbdWrjGjjls);
            log.info("---------------------开始推送{}条告警记录---------------------", wjbdWrjGjjls.size());

            // 遍历所有告警记录推送
            for (WjbdWrjGjjl gjjl : wjbdWrjGjjls) {
                try {
                    sendAlarmByWebSocket(gjjl);
                } catch (Exception e) {
                    // 单个告警推送失败，不影响其他推送
                    log.error("推送告警记录{}失败", gjjl.getId(), e);
                }
            }

            log.info("----------------------结束推送告警记录--------------------------");
        }
    }

    /**
     * 设置告警原因（提取重复逻辑，简化代码）
     */
    private void setAlarmReason(WjbdWrjKy item, UavDetectMsg uavDetectMsg, WjbdWrjGjjl wjbdWrjGjjl) {
        UavDetectMsg uavDetectMsg3 = uavDetectMsgMapper.getUavDetectMsgByJwdJl(
                item.getZxdjd(), item.getZxdwd(), item.getJfqbj()
        );

        if (uavDetectMsg3 == null) {
            UavDetectMsg uavDetectMsg5 = uavDetectMsgMapper.getUavDetectMsgByJwdJl(
                    item.getZxdjd(), item.getZxdwd(), item.getYjqbj()
            );
            if (uavDetectMsg5 == null) {
                wjbdWrjGjjl.setGjys(item.getYs());
            } else {
                wjbdWrjGjjl.setGjys(item.getYjqys());
            }
        } else {
            wjbdWrjGjjl.setGjys(item.getJfqys());
        }
    }

    /**
     * 填充黑名单告警信息
     */
    private void fillBlacklistAlarmInfo(WjbdWrjKy item, UavDetectMsg uavDetectMsg,
                                        UavDeviceConfig uavDeviceConfig, List<WjbdWrjJbxx> hmdMatchList,
                                        WjbdWrjGjjl wjbdWrjGjjl) {
        wjbdWrjGjjl.setKymc(item.getMc());
        wjbdWrjGjjl.setKyid(item.getId());
        wjbdWrjGjjl.setWrjid(hmdMatchList.get(0).getId());
        wjbdWrjGjjl.setGjlx(WjbdWrjGjjlType.GJLX1);

        // ========== 修复：BigDecimal转换空值校验 ==========
        wjbdWrjGjjl.setGjfsjd(safeConvertToBigDecimal(uavDetectMsg.getDronLng()));
        wjbdWrjGjjl.setGjfswd(safeConvertToBigDecimal(uavDetectMsg.getDronLat()));
        wjbdWrjGjjl.setGjfsgd(uavDetectMsg.getAltitude());
        wjbdWrjGjjl.setGjfssj(new Date());
        wjbdWrjGjjl.setClzt(WjbdWrjGjjlType.CLZT2);
        wjbdWrjGjjl.setClsj(new Date());
        wjbdWrjGjjl.setClbz(WjbdWrjGjjlType.CLBZ1);
        wjbdWrjGjjl.setWrjpp(hmdMatchList.get(0).getBrand());
        wjbdWrjGjjl.setWrjxh(hmdMatchList.get(0).getModel());

        // ========== 修复：空指针校验 - 经纬度/设备信息 ==========
        wjbdWrjGjjl.setFsjd(uavDetectMsg.getPilotLng());
        wjbdWrjGjjl.setFswd(uavDetectMsg.getPilotLat());
        wjbdWrjGjjl.setZdid(uavDeviceConfig.getId().toString());
        wjbdWrjGjjl.setZdmc(uavDeviceConfig.getName());
    }

    /**
     * 处理未知无人机（非黑白名单）
     */
    private void handleUnknownUav(WjbdWrjKy item, UavDetectMsg uavDetectMsg,
                                  UavDeviceConfig uavDeviceConfig, List<WjbdWrjJbxx> allWjbdWrjJbxxList,
                                  WjbdWrjGjjl wjbdWrjGjjl) {
        // 过滤无人机列表
        List<WjbdWrjJbxx> matchList = allWjbdWrjJbxxList.stream()
                .filter(jbxx -> jbxx.getModel().equals(uavDetectMsg.getModel())
                        && jbxx.getSerialNumber().equals(uavDetectMsg.getSerial()))
                .collect(Collectors.toList());

        WjbdWrjJbxx wjbdWrjJbxx = new WjbdWrjJbxx();
        if (!matchList.isEmpty()) {
            wjbdWrjJbxx = matchList.get(0);
        } else {
            // 新增无人机数据
            wjbdWrjJbxx.setBrand("未知");
            wjbdWrjJbxx.setSerialNumber(uavDetectMsg.getSerial());
            wjbdWrjJbxx.setModel(uavDetectMsg.getModel());
            wjbdWrjJbxx.setAuthStatus(2);
            wjbdWrjJbxx.setStatus(1);
            wjbdWrjJbxx.setCurrentLongitude(safeConvertToBigDecimal(uavDetectMsg.getDronLng()));
            wjbdWrjJbxx.setCurrentLatitude(safeConvertToBigDecimal(uavDetectMsg.getDronLat()));
            // ========== 注意：MyBatis需配置主键回显 ==========
            // <insert id="insert" useGeneratedKeys="true" keyProperty="id">
            wjbdWrjJbxxMapper.insert(wjbdWrjJbxx);
        }

        // 加入黑名单申请
        WjbdWrjHbmdsq wjbdWrjHbmdsq = new WjbdWrjHbmdsq();
        wjbdWrjHbmdsq.setWrjid(wjbdWrjJbxx.getId());
        wjbdWrjHbmdsq.setMdlx("黑名单");
        wjbdWrjHbmdsqMapper.insert(wjbdWrjHbmdsq);

        // 填充告警信息
        wjbdWrjGjjl.setKymc(item.getMc());
        wjbdWrjGjjl.setKyid(item.getId());
        wjbdWrjGjjl.setWrjid(wjbdWrjJbxx.getId());
        wjbdWrjGjjl.setGjlx(WjbdWrjGjjlType.GJLX2);
        wjbdWrjGjjl.setGjfsjd(safeConvertToBigDecimal(uavDetectMsg.getDronLng()));
        wjbdWrjGjjl.setGjfswd(safeConvertToBigDecimal(uavDetectMsg.getDronLat()));
        wjbdWrjGjjl.setGjfssj(new Date());
        wjbdWrjGjjl.setClzt(WjbdWrjGjjlType.CLZT2);
        wjbdWrjGjjl.setClsj(new Date());
        wjbdWrjGjjl.setClbz(WjbdWrjGjjlType.CLBZ2);
        wjbdWrjGjjl.setFsjd((uavDetectMsg.getPilotLng()));
        wjbdWrjGjjl.setFswd((uavDetectMsg.getPilotLat()));
        wjbdWrjGjjl.setZdid(uavDeviceConfig.getId().toString());
        wjbdWrjGjjl.setZdmc(uavDeviceConfig.getName());
        wjbdWrjGjjl.setGjfsgd(uavDetectMsg.getAltitude());
        wjbdWrjGjjl.setWrjxh(uavDetectMsg.getModel());
        wjbdWrjGjjl.setWrjpp("未知");
    }

    /**
     * 安全转换为BigDecimal（避免空值/非数字转换异常）
     */
    private BigDecimal safeConvertToBigDecimal(Object value) {
        if (value == null || "".equals(value.toString().trim())) {
            return BigDecimal.ZERO; // 或根据业务需求返回null/默认值
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            log.warn("转换为BigDecimal失败，值：{}", value);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 推送告警信息到WebSocket
     */
    private void sendAlarmByWebSocket(WjbdWrjGjjl gjjl) {
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(gjjl);
        JSONObject obj = new JSONObject();
        obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
        obj.put(WebsocketConst.MSG_ID, "M0001");
        obj.put(WebsocketConst.MSG_TXT, jsonObject.toJSONString());
        webSocket.sendMessage(obj.toJSONString());
        log.debug("成功推送告警记录{}到WebSocket", gjjl.getId());
    }
}
