pub mod airspace;
pub mod alarms;
pub mod black_white_list;
pub mod dashboard;
pub mod devices;
pub mod map_tiles;
pub mod drone_basic;
pub mod drone_library;
pub mod flight_data;
pub mod mission;
pub mod sensor;
pub mod simulator;
pub mod spectrum;
pub mod static_files;
pub mod ws;
pub mod wxdzc;
pub mod wrj;
pub mod generic;
pub mod system;

use axum::{
    routing::{get, post, put},
    Router,
};
use std::sync::Arc;

use crate::bus::MessageBus;
use crate::config::AppConfig;
use crate::core::CoreEngine;
use crate::fusion::FusionEngine;
use crate::planner::MissionPlanner;
use crate::sensors::SensorManager;
use crate::simulator::SensorSimulator;
use crate::storage::{FileStorage, Repository};
use crate::threat::ThreatAssessor;
use crate::tracker::MultiTargetTracker;

/// 共享应用状态
#[derive(Clone)]
pub struct AppState {
    pub config: AppConfig,
    pub db: Repository,
    pub file_storage: Arc<FileStorage>,
    pub engine: Arc<CoreEngine>,
    pub bus: Arc<MessageBus>,
    pub sensors: Arc<SensorManager>,
    pub fusion: Arc<FusionEngine>,
    pub tracker: Arc<MultiTargetTracker>,
    pub threat: Arc<ThreatAssessor>,
    pub planner: Arc<MissionPlanner>,
    pub simulator: Arc<SensorSimulator>,
}

/// 构建所有 API 路由 + 前端静态文件服务
pub fn build_routes(state: AppState) -> Router {
    // API 路由（优先级最高）
    let api_routes = Router::new()
        // 系统管理
        .route("/api/v1/health", get(dashboard::health_check))
        .route("/api/v1/metrics", get(dashboard::system_metrics))
        .route("/api/v1/config", get(dashboard::get_config))
        .route("/api/v1/config", put(dashboard::update_config))
        // 传感器管理
        .route("/api/v1/sensors", get(sensor::list_sensors))
        .route("/api/v1/sensors/:id", get(sensor::get_sensor))
        .route("/api/v1/sensors/:id/status", get(sensor::get_sensor_status))
        .route("/api/v1/sensors/:id/calibrate", post(sensor::calibrate_sensor))
        // 仿真引擎
        .route("/api/v1/simulator", get(simulator::get_simulator_status))
        .route("/api/v1/simulator/targets", get(simulator::get_simulator_targets))
        .route("/api/v1/simulator/restart", post(simulator::restart_simulator))
        // 态势数据
        .route("/api/v1/situation/targets", get(dashboard::get_targets))
        .route("/api/v1/situation/threats", get(dashboard::get_threats))
        .route("/api/v1/situation/history/:id", get(dashboard::get_track_history))
        // 任务规划
        .route("/api/v1/missions", get(mission::list_missions))
        .route("/api/v1/missions", post(mission::create_mission))
        .route("/api/v1/missions/:id", get(mission::get_mission))
        .route("/api/v1/missions/:id/cancel", post(mission::cancel_mission))
        .route("/api/v1/missions/plan", post(mission::plan_intercept))
        .route("/api/v1/missions/plan-multi", post(mission::plan_multi_intercept))
        // 资源管理
        .route("/api/v1/resources", get(dashboard::list_resources))
        .route("/api/v1/resources/:id", get(dashboard::get_resource))
        // WebSocket 实时数据
        .route("/api/v1/ws", get(ws::websocket_handler))
        // 对话式交互
        .route("/api/v1/chat", post(dashboard::chat_message))
        // 数据导出
        .route("/api/v1/export/targets", get(dashboard::export_targets))
        .route("/api/v1/export/missions", get(dashboard::export_missions))
        // ---- 设备管理 ----
        .route("/api/v1/devices", get(devices::list_devices))
        .route("/api/v1/devices", post(devices::add_device))
        .route("/api/v1/devices", put(devices::edit_device))
        .route("/api/v1/devices/batch-delete", post(devices::batch_delete_devices))
        .route("/api/v1/devices/:id", get(devices::get_device))
        .route("/api/v1/devices/:id", axum::routing::delete(devices::delete_device))
        .route("/api/v1/devices/stats", get(devices::device_stats))
        .route("/api/v1/devices/heartbeats", get(devices::list_heartbeats))
        .route("/api/v1/devices/connect-logs", get(devices::list_connect_logs))
        .route("/api/v1/devices/operate-logs", get(devices::list_operate_logs))
        .route("/api/v1/devices/active-heartbeats", get(devices::list_active_heartbeats))
        // ---- 无人机特征库 ----
        .route("/api/v1/drones/library", get(drone_library::list))
        .route("/api/v1/drones/library", post(drone_library::add))
        .route("/api/v1/drones/library", put(drone_library::edit))
        .route("/api/v1/drones/library/batch-delete", post(drone_library::batch_delete))
        .route("/api/v1/drones/library/:id", get(drone_library::get_by_id))
        .route("/api/v1/drones/library/:id", axum::routing::delete(drone_library::delete))
        // ---- 无人机基本信息 ----
        .route("/api/v1/drones/basic", get(drone_basic::list))
        .route("/api/v1/drones/basic", post(drone_basic::add))
        .route("/api/v1/drones/basic", put(drone_basic::edit))
        .route("/api/v1/drones/basic/edit-auth", put(drone_basic::edit_auth_status))
        .route("/api/v1/drones/basic/stats", get(drone_basic::get_stats))
        .route("/api/v1/drones/basic/batch-delete", post(drone_basic::batch_delete))
        .route("/api/v1/drones/basic/:id", get(drone_basic::get_by_id))
        .route("/api/v1/drones/basic/:id", axum::routing::delete(drone_basic::delete))
        // ---- 黑白名单 ----
        .route("/api/v1/drones/bwlist", get(black_white_list::list))
        .route("/api/v1/drones/bwlist/grouped", get(black_white_list::grouped_list))
        .route("/api/v1/drones/bwlist", post(black_white_list::add))
        .route("/api/v1/drones/bwlist/edit-auth", put(black_white_list::edit_batch_auth))
        .route("/api/v1/drones/bwlist/batch-delete", post(black_white_list::batch_delete))
        .route("/api/v1/drones/bwlist/:id", axum::routing::delete(black_white_list::delete))
        // ---- 告警管理 ----
        .route("/api/v1/alarms", get(alarms::list))
        .route("/api/v1/alarms", post(alarms::add))
        .route("/api/v1/alarms", put(alarms::edit))
        .route("/api/v1/alarms/stats", get(alarms::alarm_stats))
        .route("/api/v1/alarms/batch-delete", post(alarms::batch_delete))
        .route("/api/v1/alarms/:id", get(alarms::get_by_id))
        .route("/api/v1/alarms/:id", axum::routing::delete(alarms::delete))
        // ---- 空域管理 ----
        .route("/api/v1/airspace", get(airspace::list))
        .route("/api/v1/airspace", post(airspace::add))
        .route("/api/v1/airspace", put(airspace::edit))
        .route("/api/v1/airspace/batch-delete", post(airspace::batch_delete))
        .route("/api/v1/airspace/:id", get(airspace::get_by_id))
        .route("/api/v1/airspace/:id", axum::routing::delete(airspace::delete))
        // ---- 飞行数据 ----
        .route("/api/v1/flight-data/detect-msg", get(flight_data::list_detect_msgs))
        .route("/api/v1/flight-data/detect-msg/:id", get(flight_data::get_detect_msg))
        .route("/api/v1/flight-data/df-data", get(flight_data::list_df_data))
        .route("/api/v1/flight-data/remote", get(flight_data::list_remote_data))
        // ---- 频谱数据 ----
        .route("/api/v1/spectrum", get(spectrum::list_spectrum))
        .route("/api/v1/spectrum/today", get(spectrum::list_spectrum_today))
        // ---- wxdzc 侦测预警 ----
        .route("/api/v1/wxdzc/devices/list", get(wxdzc::list_devices))
        .route("/api/v1/wxdzc/detect-msg/by-station", get(wxdzc::get_by_station))
        .route("/api/v1/wxdzc/detect-msg/by-model-serial", get(wxdzc::get_by_model_serial))
        .route("/api/v1/wxdzc/detect-msg/date-calendar", get(wxdzc::get_date_calendar))
        .route("/api/v1/wxdzc/flight-route/generate", post(wxdzc::generate_flight_route))
        // ---- wrj 武警业务 (通用 JSON CRUD) ----
        // 反无战法
        .route("/api/v1/wrj/fwzf", get(wrj::list_fwzf).post(wrj::add_fwzf).put(wrj::edit_fwzf))
        .route("/api/v1/wrj/fwzf/batch-delete", post(wrj::batch_delete_fwzf))
        .route("/api/v1/wrj/fwzf/:id", get(wrj::get_fwzf).delete(wrj::delete_fwzf))
        // 反制装备
        .route("/api/v1/wrj/fzzb", get(wrj::list_fzzb).post(wrj::add_fzzb).put(wrj::edit_fzzb))
        .route("/api/v1/wrj/fzzb/batch-delete", post(wrj::batch_delete_fzzb))
        .route("/api/v1/wrj/fzzb/:id", get(wrj::get_fzzb).delete(wrj::delete_fzzb))
        // 编携配装
        .route("/api/v1/wrj/bxpz", get(wrj::list_bxpz).post(wrj::add_bxpz).put(wrj::edit_bxpz))
        .route("/api/v1/wrj/bxpz/batch-delete", post(wrj::batch_delete_bxpz))
        .route("/api/v1/wrj/bxpz/tree", get(wrj::list_bxpz_tree))
        .route("/api/v1/wrj/bxpz/:id", get(wrj::get_bxpz).delete(wrj::delete_bxpz))
        // 重要目标
        .route("/api/v1/wrj/zymb", get(wrj::list_zymb).post(wrj::add_zymb).put(wrj::edit_zymb))
        .route("/api/v1/wrj/zymb/batch-delete", post(wrj::batch_delete_zymb))
        .route("/api/v1/wrj/zymb/by-geo", get(wrj::zymb_by_geo))
        .route("/api/v1/wrj/zymb/:id", get(wrj::get_zymb).delete(wrj::delete_zymb))
        // 空域授权
        .route("/api/v1/wrj/kysq", get(wrj::list_kysq).post(wrj::add_kysq))
        .route("/api/v1/wrj/kysq/save", post(wrj::save_kysq))
        .route("/api/v1/wrj/kysq/:id", axum::routing::delete(wrj::delete_kysq))
        // 操作视频
        .route("/api/v1/wrj/czsp", get(wrj::list_czsp).post(wrj::add_czsp).put(wrj::edit_czsp))
        .route("/api/v1/wrj/czsp/batch-delete", post(wrj::batch_delete_czsp))
        .route("/api/v1/wrj/czsp/:id", get(wrj::get_czsp).delete(wrj::delete_czsp))
        // 收藏夹
        .route("/api/v1/wrj/scj", get(wrj::list_scj).post(wrj::add_scj).put(wrj::edit_scj))
        .route("/api/v1/wrj/scj/batch-delete", post(wrj::batch_delete_scj))
        .route("/api/v1/wrj/scj/tree", get(wrj::list_scj_tree))
        .route("/api/v1/wrj/scj/:id", get(wrj::get_scj).delete(wrj::delete_scj))
        // 接口管理
        .route("/api/v1/wrj/jkgl", get(wrj::list_jkgl).post(wrj::add_jkgl).put(wrj::edit_jkgl))
        .route("/api/v1/wrj/jkgl/batch-delete", post(wrj::batch_delete_jkgl))
        .route("/api/v1/wrj/jkgl/:id", get(wrj::get_jkgl).delete(wrj::delete_jkgl))
        // 推演计划
        .route("/api/v1/wrj/tyjh", get(wrj::list_tyjh))
        .route("/api/v1/wrj/tyjh/generate", post(wrj::generate_tyjh))
        .route("/api/v1/wrj/tyjh/change-status", get(wrj::change_tyjh_status))
        .route("/api/v1/wrj/tyjh/batch-delete", post(wrj::batch_delete_tyjh))
        .route("/api/v1/wrj/tyjh/:id", get(wrj::get_tyjh).delete(wrj::delete_tyjh))
        // 推演计划数据
        .route("/api/v1/wrj/tyjh-data", get(wrj::list_tyjh_data).post(wrj::add_tyjh_data))
        .route("/api/v1/wrj/tyjh-data/:id", axum::routing::delete(wrj::delete_tyjh_data))
        // 无人机详细信息
        .route("/api/v1/wrj/jbxx-new", get(wrj::list_jbxx_new).post(wrj::add_jbxx_new).put(wrj::edit_jbxx_new))
        .route("/api/v1/wrj/jbxx-new/batch-delete", post(wrj::batch_delete_jbxx_new))
        .route("/api/v1/wrj/jbxx-new/:id", get(wrj::get_jbxx_new).delete(wrj::delete_jbxx_new))
        // 知识库
        .route("/api/v1/wrj/zsk", get(wrj::list_zsk).post(wrj::add_zsk).put(wrj::edit_zsk))
        .route("/api/v1/wrj/zsk/:id", get(wrj::get_zsk).delete(wrj::delete_zsk))
        .route("/api/v1/wrj/zsk-wj", get(wrj::list_zsk_wj).post(wrj::add_zsk_wj).put(wrj::edit_zsk_wj))
        .route("/api/v1/wrj/zsk-wj/:id", get(wrj::get_zsk_wj).delete(wrj::delete_zsk_wj))
        .route("/api/v1/wrj/zsk-wj-nr", get(wrj::list_zsk_wj_nr).post(wrj::add_zsk_wj_nr))
        // 频谱结果
        .route("/api/v1/wrj/spectrum-result", get(wrj::list_spectrum_result).post(wrj::add_spectrum_result).put(wrj::edit_spectrum_result))
        .route("/api/v1/wrj/spectrum-result/:id", get(wrj::get_spectrum_result))
        // 标绘
        .route("/api/v1/wrj/dxyy-bh", get(wrj::list_dxyy_bh).post(wrj::add_dxyy_bh).put(wrj::edit_dxyy_bh))
        .route("/api/v1/wrj/dxyy-bh/:id", get(wrj::get_dxyy_bh))
        // 录屏文件
        .route("/api/v1/wrj/lpwj", get(wrj::list_lpwj).post(wrj::add_lpwj).put(wrj::edit_lpwj))
        // 地形地貌
        .route("/api/v1/wrj/dxdm", get(wrj::list_dxdm).post(wrj::add_dxdm).put(wrj::edit_dxdm))
        .route("/api/v1/wrj/dxdm/batch-delete", post(wrj::batch_delete_dxdm))
        .route("/api/v1/wrj/dxdm/by-geo", get(wrj::dxdm_by_geo))
        .route("/api/v1/wrj/dxdm/:id", get(wrj::get_dxdm).delete(wrj::delete_dxdm))
        // 用户管理参数
        .route("/api/v1/wrj/yhgl-cs", get(wrj::list_yhgl_cs).post(wrj::add_yhgl_cs).put(wrj::edit_yhgl_cs))
        // 作战力量部队 / 部署
        .route("/api/v1/wrj/zzll-bd", get(wrj::list_zzll_bd).post(wrj::add_zzll_bd).put(wrj::edit_zzll_bd))
        .route("/api/v1/wrj/zzll-bd-bs", get(wrj::list_zzll_bd_bs).post(wrj::add_zzll_bd_bs))
        // 操作日志
        .route("/api/v1/wrj/operation-log", get(wrj::list_operation_log).post(wrj::add_operation_log).put(wrj::edit_operation_log))
        // ---- system 系统管理：字典 / 文件上传 ----
        .route("/api/v1/sys/dict", get(system::list_dict).post(system::add_dict).put(system::edit_dict))
        .route("/api/v1/sys/dict/:id", axum::routing::delete(system::delete_dict))
        .route("/api/v1/sys/dict-items/:code", get(system::get_dict_items))
        .route("/api/v1/sys/dict-item", get(system::list_dict_item).post(system::add_dict_item).put(system::edit_dict_item))
        .route("/api/v1/sys/dict-item/:id", axum::routing::delete(system::delete_dict_item))
        .route("/api/v1/sys/users", get(system::list_users).post(system::add_user).put(system::edit_user))
        .route("/api/v1/sys/users/:id", axum::routing::delete(system::delete_user))
        .route("/api/v1/sys/upload", post(system::upload_file))
        // ---- 地图服务 ----
        .route("/api/v1/map/config", get(map_tiles::map_config))
        .route("/api/v1/tiles/:z/:y/:x", get(map_tiles::tile_proxy))
        .with_state(state);

    // 前端静态文件 + SPA fallback（最低优先级）
    // 所有非 /api/* 的请求 → 返回嵌入的前端资源
    api_routes.fallback(static_files::static_handler)
}
