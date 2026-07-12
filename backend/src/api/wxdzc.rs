/// wxdzc 侦测预警 API
/// 对应 Java 后端 uav 模块的 5 个核心端点
use axum::{extract::{Query, State}, Json};
use super::AppState;
use crate::models::*;

// ==================== 1. 设备列表 ====================

/// GET /api/v1/wxdzc/devices/list
/// 分页查询设备列表，支持名称模糊搜索
pub async fn list_devices(
    State(state): State<AppState>,
    Query(query): Query<WxdzcDeviceQuery>,
) -> Json<ApiResponse<PageResult<DeviceConfig>>> {
    let page = query.page.unwrap_or(1) as i64;
    let page_size = query.page_size.unwrap_or(10).min(100) as i64;
    let name_filter = query.name.as_deref();

    match state.db.list_devices_for_wxdzc(page, page_size, name_filter).await {
        Ok((records, total)) => Json(ApiResponse::ok(PageResult {
            records,
            total,
            page: page as u32,
            page_size: page_size as u32,
        })),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

// ==================== 2. 按站点分组侦测消息 ====================

/// GET /api/v1/wxdzc/detect-msg/by-station
/// 按站点分组，取每个序列号的最新一条侦测记录
/// 参数: rq (日期 YYYY-MM-DD), auth_status (可选授权状态过滤)
pub async fn get_by_station(
    State(state): State<AppState>,
    Query(query): Query<StationDetectQuery>,
) -> Json<ApiResponse<Vec<UavDetectMsgVo>>> {
    let rq = query.rq.as_deref();
    let auth_status = query.auth_status;

    match state.db.get_uav_detect_msg_by_station_id(rq, auth_status).await {
        Ok(rows) => {
            let mut vos: Vec<UavDetectMsgVo> = rows.into_iter().map(|r| r.into_vo()).collect();
            // Post-filter by auth_status if requested
            if let Some(as_val) = auth_status {
                vos.retain(|v| v.auth_status == Some(as_val));
            }
            Json(ApiResponse::ok(vos))
        }
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

// ==================== 3. 按型号序列号查询飞行路径 ====================

/// GET /api/v1/wxdzc/detect-msg/by-model-serial
/// 按序列号（或型号）查询指定日期的飞行路径数据
/// 参数: model, serial, rq
pub async fn get_by_model_serial(
    State(state): State<AppState>,
    Query(query): Query<ModelSerialQuery>,
) -> Json<ApiResponse<UavDetectMsgVo1>> {
    let model = query.model.as_deref();
    let serial = query.serial.as_deref();
    let rq = query.rq.as_deref();

    match state.db.get_uav_detect_msg_by_model_serial_rq(model, serial, rq).await {
        Ok(msgs) => {
            // Try to look up drone basic info by serial
            let jbxx = if let Some(s) = serial {
                state.db.get_drone_basic_info_by_serial(s).await.unwrap_or(None)
            } else {
                None
            };
            Json(ApiResponse::ok(UavDetectMsgVo1 {
                uav_detect_msg_list: msgs,
                wjbd_wrj_jbxx: jbxx,
            }))
        }
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

// ==================== 4. 日期日历数据 ====================

/// GET /api/v1/wxdzc/detect-msg/date-calendar
/// 获取指定年月中有侦测数据的日期列表（用于日历高亮标记）
/// 参数: nf (年份), yf (月份)
pub async fn get_date_calendar(
    State(state): State<AppState>,
    Query(query): Query<DateCalendarQuery>,
) -> Json<ApiResponse<Vec<UavDetectMsgDateVo>>> {
    let nf = query.nf.as_deref();
    let yf = query.yf.as_deref();

    match state.db.get_uav_detect_msg_date_by_nf_yf(nf, yf).await {
        Ok(dates) => Json(ApiResponse::ok(dates)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

// ==================== 5. 生成飞行路线 ====================

/// POST /api/v1/wxdzc/flight-route/generate
/// 根据航点生成飞行路线数据，插入 uav_detect_msg 模拟飞行路径
pub async fn generate_flight_route(
    State(state): State<AppState>,
    Json(body): Json<FlightRouteRequest>,
) -> Json<ApiResponse<serde_json::Value>> {
    let waypoints = body.waypoints.unwrap_or_default();
    if waypoints.is_empty() {
        // 如果没有提供航点，根据 start/end 生成直线插值
        let start = body.start.as_ref();
        let end = body.end.as_ref();
        if let (Some(s), Some(e)) = (start, end) {
            let slat = s.get("lat").and_then(|v| v.as_f64()).unwrap_or(34.0);
            let slng = s.get("lng").and_then(|v| v.as_f64()).unwrap_or(108.0);
            let elat = e.get("lat").and_then(|v| v.as_f64()).unwrap_or(34.1);
            let elng = e.get("lng").and_then(|v| v.as_f64()).unwrap_or(108.1);
            let num_points = 50;

            use sqlx::Row;
            let now = chrono::Utc::now();
            let station_id = body.station_id.unwrap_or(1);
            let model = body.model.clone().unwrap_or_default();
            let serial = body.serial.clone().unwrap_or_default();
            let mut inserted = 0u32;

            for i in 0..num_points {
                let t = i as f64 / (num_points - 1) as f64;
                let lat = slat + (elat - slat) * t;
                let lng = slng + (elng - slng) * t;
                let alt = 100.0 + (t * 50.0).sin() * 30.0;
                let sd = 15.0 + (t * 10.0).sin() * 5.0;
                let data_time = now + chrono::Duration::seconds(i as i64 * 30);
                let time_str = data_time.format("%Y-%m-%d %H:%M:%S").to_string();

                let result = sqlx::query(
                    "INSERT INTO hsim_wrj_fxsj_zcbw (station_id, serial, model, dron_lng, dron_lat, altitude, height, sd, east_v, north_v, data_time, create_time, model_clean, serial_clean) VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14)"
                )
                .bind(station_id)
                .bind(&serial)
                .bind(&model)
                .bind(lng)
                .bind(lat)
                .bind(alt)
                .bind(alt - 10.0)
                .bind(sd)
                .bind(sd * 0.8)
                .bind(sd * 0.2)
                .bind(&time_str)
                .bind(&time_str)
                .bind(model.trim().to_lowercase())
                .bind(serial.trim().to_lowercase())
                .execute(state.db.pool())
                .await;

                if result.is_ok() { inserted += 1; }
            }

            return Json(ApiResponse::ok(serde_json::json!({
                "status": "ok",
                "waypoints_generated": num_points,
                "inserted": inserted,
                "serial": serial,
                "model": model,
            })));
        }
        return Json(ApiResponse::error("No waypoints or start/end provided"));
    }

    // Use provided waypoints
    let num_waypoints = waypoints.len();
    let station_id = body.station_id.unwrap_or(1);
    let model = body.model.clone().unwrap_or_default();
    let serial = body.serial.clone().unwrap_or_default();
    let now = chrono::Utc::now();
    let mut inserted = 0u32;

    for (i, wp) in waypoints.iter().enumerate() {
        let lat = wp.get("lat").and_then(|v| v.as_f64()).unwrap_or(0.0);
        let lng = wp.get("lng").and_then(|v| v.as_f64()).unwrap_or(0.0);
        let alt = wp.get("alt").and_then(|v| v.as_f64()).unwrap_or(100.0);
        let sd = wp.get("speed").and_then(|v| v.as_f64()).unwrap_or(15.0);
        let data_time = now + chrono::Duration::seconds(i as i64 * 30);
        let time_str = data_time.format("%Y-%m-%d %H:%M:%S").to_string();

        let result = sqlx::query(
            "INSERT INTO hsim_wrj_fxsj_zcbw (station_id, serial, model, dron_lng, dron_lat, altitude, height, sd, east_v, north_v, data_time, create_time, model_clean, serial_clean) VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14)"
        )
        .bind(station_id).bind(&serial).bind(&model).bind(lng).bind(lat).bind(alt).bind(alt - 10.0)
        .bind(sd).bind(sd * 0.8).bind(sd * 0.2)
        .bind(&time_str).bind(&time_str)
        .bind(model.trim().to_lowercase()).bind(serial.trim().to_lowercase())
        .execute(state.db.pool()).await;

        if result.is_ok() { inserted += 1; }
    }

    Json(ApiResponse::ok(serde_json::json!({
        "status": "ok",
        "waypoints_generated": num_waypoints as u32,
        "inserted": inserted,
        "serial": serial,
        "model": model,
    })))
}
