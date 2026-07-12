use sqlx::SqlitePool;
use tracing;
use uuid::Uuid;

use crate::models::*;

/// 数据仓库层 — 封装所有 SQLite CRUD 操作
#[derive(Clone)]
pub struct Repository {
    pool: SqlitePool,
}

impl Repository {
    pub fn new(pool: SqlitePool) -> Self {
        Self { pool }
    }

    /// 获取底层连接池引用
    pub fn pool(&self) -> &SqlitePool {
        &self.pool
    }

    // ==================== Sensors ====================

    /// 插入或更新传感器注册信息
    pub async fn upsert_sensor(&self, sensor: &SensorMetadata) -> anyhow::Result<()> {
        let capabilities = serde_json::to_string(&sensor.capabilities).unwrap_or_default();
        let config = serde_json::to_string(&sensor.config).unwrap_or_default();

        sqlx::query(
            r#"
            INSERT INTO sensors (sensor_id, sensor_type, name, model, latitude, longitude, altitude,
                                 status, capabilities, config, last_heartbeat, updated_at)
            VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, datetime('now'))
            ON CONFLICT(sensor_id) DO UPDATE SET
                sensor_type = excluded.sensor_type,
                name = excluded.name,
                model = excluded.model,
                latitude = excluded.latitude,
                longitude = excluded.longitude,
                altitude = excluded.altitude,
                status = excluded.status,
                capabilities = excluded.capabilities,
                config = excluded.config,
                last_heartbeat = excluded.last_heartbeat,
                updated_at = datetime('now')
            "#,
        )
        .bind(sensor.sensor_id.to_string())
        .bind(format!("{:?}", sensor.sensor_type))
        .bind(&sensor.name)
        .bind(&sensor.model)
        .bind(sensor.location.latitude)
        .bind(sensor.location.longitude)
        .bind(sensor.location.altitude)
        .bind(format!("{:?}", sensor.status))
        .bind(&capabilities)
        .bind(&config)
        .bind(sensor.last_heartbeat.to_rfc3339())
        .execute(&self.pool)
        .await?;

        Ok(())
    }

    /// 列出所有传感器
    pub async fn list_sensors(&self) -> anyhow::Result<Vec<SensorMetadata>> {
        let rows = sqlx::query_as::<_, SensorRow>(
            r#"SELECT sensor_id, sensor_type, name, model, latitude, longitude, altitude,
                      status, capabilities, config, last_heartbeat
               FROM sensors ORDER BY name"#,
        )
        .fetch_all(&self.pool)
        .await?;

        Ok(rows.into_iter().filter_map(|r| r.into_metadata().ok()).collect())
    }

    /// 获取单个传感器
    pub async fn get_sensor(&self, id: &Uuid) -> anyhow::Result<Option<SensorMetadata>> {
        let row = sqlx::query_as::<_, SensorRow>(
            r#"SELECT sensor_id, sensor_type, name, model, latitude, longitude, altitude,
                      status, capabilities, config, last_heartbeat
               FROM sensors WHERE sensor_id = ?1"#,
        )
        .bind(id.to_string())
        .fetch_optional(&self.pool)
        .await?;

        match row {
            Some(r) => Ok(Some(r.into_metadata()?)),
            None => Ok(None),
        }
    }

    /// 更新传感器状态
    pub async fn update_sensor_status(
        &self,
        id: &Uuid,
        status: &SensorStatus,
    ) -> anyhow::Result<()> {
        sqlx::query(
            r#"UPDATE sensors SET status = ?1, updated_at = datetime('now') WHERE sensor_id = ?2"#,
        )
        .bind(format!("{:?}", status))
        .bind(id.to_string())
        .execute(&self.pool)
        .await?;

        Ok(())
    }

    /// 删除传感器
    pub async fn delete_sensor(&self, id: &Uuid) -> anyhow::Result<()> {
        sqlx::query("DELETE FROM sensors WHERE sensor_id = ?1")
            .bind(id.to_string())
            .execute(&self.pool)
            .await?;

        Ok(())
    }

    // ==================== Targets ====================

    /// 写入或更新目标（upsert）
    pub async fn upsert_target(&self, target: &TrackedTarget) -> anyhow::Result<()> {
        sqlx::query(
            r#"
            INSERT INTO targets (target_id, track_id, classification, threat_level, confidence,
                                 latitude, longitude, altitude, vn, ve, vd,
                                 first_seen, last_update, is_active)
            VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, 1)
            ON CONFLICT(target_id) DO UPDATE SET
                track_id = excluded.track_id,
                classification = excluded.classification,
                threat_level = excluded.threat_level,
                confidence = excluded.confidence,
                latitude = excluded.latitude,
                longitude = excluded.longitude,
                altitude = excluded.altitude,
                vn = excluded.vn,
                ve = excluded.ve,
                vd = excluded.vd,
                last_update = excluded.last_update,
                is_active = 1
            "#,
        )
        .bind(target.target_id.to_string())
        .bind(target.track_id.to_string())
        .bind(format!("{:?}", target.classification))
        .bind(format!("{:?}", target.threat_level))
        .bind(target.confidence)
        .bind(target.position.latitude)
        .bind(target.position.longitude)
        .bind(target.position.altitude)
        .bind(target.velocity.vn)
        .bind(target.velocity.ve)
        .bind(target.velocity.vd)
        .bind(target.first_seen.to_rfc3339())
        .bind(target.last_update.to_rfc3339())
        .execute(&self.pool)
        .await?;

        Ok(())
    }

    /// 追加航迹点
    pub async fn append_track_point(
        &self,
        target_id: &Uuid,
        point: &TrackPoint,
    ) -> anyhow::Result<()> {
        sqlx::query(
            r#"
            INSERT INTO track_points (target_id, timestamp, latitude, longitude, altitude, vn, ve, vd)
            VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)
            "#,
        )
        .bind(target_id.to_string())
        .bind(point.timestamp.to_rfc3339())
        .bind(point.position.latitude)
        .bind(point.position.longitude)
        .bind(point.position.altitude)
        .bind(point.velocity.vn)
        .bind(point.velocity.ve)
        .bind(point.velocity.vd)
        .execute(&self.pool)
        .await?;

        Ok(())
    }

    /// 批量追加航迹点
    pub async fn append_track_points_batch(
        &self,
        target_id: &Uuid,
        points: &[TrackPoint],
    ) -> anyhow::Result<()> {
        for point in points {
            self.append_track_point(target_id, point).await?;
        }
        Ok(())
    }

    /// 获取活跃目标列表（带基本字段，不含完整航迹历史）
    pub async fn get_active_targets(&self) -> anyhow::Result<Vec<TrackedTarget>> {
        let rows = sqlx::query_as::<_, TargetRow>(
            r#"SELECT target_id, track_id, classification, threat_level, confidence,
                      latitude, longitude, altitude, vn, ve, vd, first_seen, last_update
               FROM targets WHERE is_active = 1"#,
        )
        .fetch_all(&self.pool)
        .await?;

        Ok(rows.into_iter().map(|r| r.into_target()).collect())
    }

    /// 获取目标航迹历史
    pub async fn get_target_history(&self, id: &Uuid) -> anyhow::Result<Vec<TrackPoint>> {
        let rows = sqlx::query_as::<_, TrackPointRow>(
            r#"SELECT timestamp, latitude, longitude, altitude, vn, ve, vd
               FROM track_points WHERE target_id = ?1 ORDER BY timestamp"#,
        )
        .bind(id.to_string())
        .fetch_all(&self.pool)
        .await?;

        Ok(rows.into_iter().map(|r| r.into_track_point()).collect())
    }

    /// 标记目标为非活跃
    pub async fn deactivate_target(&self, id: &Uuid) -> anyhow::Result<()> {
        sqlx::query("UPDATE targets SET is_active = 0, last_update = datetime('now') WHERE target_id = ?1")
            .bind(id.to_string())
            .execute(&self.pool)
            .await?;

        Ok(())
    }

    /// 清理超时的非活跃目标记录
    pub async fn cleanup_stale_targets(&self, timeout_secs: i64) -> anyhow::Result<u64> {
        let result = sqlx::query(
            r#"UPDATE targets SET is_active = 0
               WHERE is_active = 1
                 AND (strftime('%s', 'now') - strftime('%s', last_update)) > ?1"#,
        )
        .bind(timeout_secs)
        .execute(&self.pool)
        .await?;

        Ok(result.rows_affected())
    }

    // ==================== Missions ====================

    /// 创建或更新任务
    pub async fn upsert_mission(&self, mission: &InterceptMission) -> anyhow::Result<()> {
        let interceptor_ids = serde_json::to_string(&mission.assigned_interceptors).unwrap_or_default();
        let trajectory = serde_json::to_string(&mission.trajectory).unwrap_or_default();

        sqlx::query(
            r#"
            INSERT INTO missions (mission_id, target_id, interceptor_ids, trajectory, priority, status, updated_at)
            VALUES (?1, ?2, ?3, ?4, ?5, ?6, datetime('now'))
            ON CONFLICT(mission_id) DO UPDATE SET
                target_id = excluded.target_id,
                interceptor_ids = excluded.interceptor_ids,
                trajectory = excluded.trajectory,
                priority = excluded.priority,
                status = excluded.status,
                updated_at = datetime('now')
            "#,
        )
        .bind(mission.mission_id.to_string())
        .bind(mission.target_id.to_string())
        .bind(&interceptor_ids)
        .bind(&trajectory)
        .bind(mission.priority as i32)
        .bind(format!("{:?}", mission.status))
        .execute(&self.pool)
        .await?;

        Ok(())
    }

    /// 列出所有任务
    pub async fn list_missions(&self) -> anyhow::Result<Vec<InterceptMission>> {
        let rows = sqlx::query_as::<_, MissionRow>(
            r#"SELECT mission_id, target_id, interceptor_ids, trajectory, priority, status, created_at, updated_at
               FROM missions ORDER BY created_at DESC"#,
        )
        .fetch_all(&self.pool)
        .await?;

        Ok(rows.into_iter().map(|r| r.into_mission()).collect())
    }

    /// 获取单个任务
    pub async fn get_mission(&self, id: &Uuid) -> anyhow::Result<Option<InterceptMission>> {
        let row = sqlx::query_as::<_, MissionRow>(
            r#"SELECT mission_id, target_id, interceptor_ids, trajectory, priority, status, created_at, updated_at
               FROM missions WHERE mission_id = ?1"#,
        )
        .bind(id.to_string())
        .fetch_optional(&self.pool)
        .await?;

        match row {
            Some(r) => Ok(Some(r.into_mission())),
            None => Ok(None),
        }
    }

    /// 更新任务状态
    pub async fn update_mission_status(
        &self,
        id: &Uuid,
        status: &MissionStatus,
    ) -> anyhow::Result<()> {
        sqlx::query(
            r#"UPDATE missions SET status = ?1, updated_at = datetime('now') WHERE mission_id = ?2"#,
        )
        .bind(format!("{:?}", status))
        .bind(id.to_string())
        .execute(&self.pool)
        .await?;

        Ok(())
    }

    /// 删除任务
    pub async fn delete_mission(&self, id: &Uuid) -> anyhow::Result<()> {
        sqlx::query("DELETE FROM missions WHERE mission_id = ?1")
            .bind(id.to_string())
            .execute(&self.pool)
            .await?;

        Ok(())
    }

    // ==================== Alerts ====================

    /// 插入告警记录
    pub async fn insert_alert(&self, alert: &crate::bus::AlertEvent) -> anyhow::Result<()> {
        sqlx::query(
            r#"
            INSERT INTO alerts (alert_id, alert_type, severity, title, description, source, timestamp)
            VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7)
            "#,
        )
        .bind(alert.alert_id.to_string())
        .bind(format!("{:?}", alert.alert_type))
        .bind(format!("{:?}", alert.severity))
        .bind(&alert.title)
        .bind(&alert.description)
        .bind(&alert.source)
        .bind(alert.timestamp.to_rfc3339())
        .execute(&self.pool)
        .await?;

        Ok(())
    }

    /// 列出最近告警（按时间倒序，限制条数）
    pub async fn list_alerts(&self, limit: u32) -> anyhow::Result<Vec<crate::bus::AlertEvent>> {
        let rows = sqlx::query_as::<_, AlertRow>(
            r#"SELECT alert_id, alert_type, severity, title, description, source, timestamp
               FROM alerts ORDER BY timestamp DESC LIMIT ?1"#,
        )
        .bind(limit as i32)
        .fetch_all(&self.pool)
        .await?;

        Ok(rows.into_iter().filter_map(|r| r.into_alert()).collect())
    }

    // ==================== System Events ====================

    /// 插入系统事件
    pub async fn insert_system_event(
        &self,
        event_type: &str,
        payload: &str,
    ) -> anyhow::Result<()> {
        sqlx::query(
            r#"INSERT INTO system_events (event_type, payload) VALUES (?1, ?2)"#,
        )
        .bind(event_type)
        .bind(payload)
        .execute(&self.pool)
        .await?;

        Ok(())
    }
}

// ==================== 内部 Row 类型（用于 sqlx::query_as） ====================

#[derive(Debug, sqlx::FromRow)]
struct SensorRow {
    sensor_id: String,
    sensor_type: String,
    name: String,
    model: String,
    latitude: f64,
    longitude: f64,
    altitude: f64,
    status: String,
    capabilities: Option<String>,
    config: Option<String>,
    last_heartbeat: String,
}

impl SensorRow {
    fn into_metadata(self) -> anyhow::Result<SensorMetadata> {
        let sensor_type = match self.sensor_type.as_str() {
            "Radar" => SensorType::Radar,
            "Rf" => SensorType::Rf,
            "EoIr" => SensorType::EoIr,
            "Acoustic" => SensorType::Acoustic,
            "Lidar" => SensorType::Lidar,
            other => SensorType::Other(other.to_string()),
        };
        let status = match self.status.as_str() {
            "Online" => SensorStatus::Online,
            "Offline" => SensorStatus::Offline,
            "Degraded" => SensorStatus::Degraded,
            "Calibrating" => SensorStatus::Calibrating,
            other => SensorStatus::Degraded, // fallback
        };
        let capabilities: Vec<String> = self
            .capabilities
            .and_then(|s| serde_json::from_str(&s).ok())
            .unwrap_or_default();
        let config: serde_json::Value = self
            .config
            .and_then(|s| serde_json::from_str(&s).ok())
            .unwrap_or(serde_json::Value::Null);

        Ok(SensorMetadata {
            sensor_id: Uuid::parse_str(&self.sensor_id)?,
            sensor_type,
            name: self.name,
            model: self.model,
            location: GeoPosition {
                latitude: self.latitude,
                longitude: self.longitude,
                altitude: self.altitude,
            },
            status,
            last_heartbeat: chrono::DateTime::parse_from_rfc3339(&self.last_heartbeat)
                .map(|dt| dt.with_timezone(&chrono::Utc))
                .unwrap_or_else(|_| chrono::Utc::now()),
            capabilities,
            config,
        })
    }
}

#[derive(Debug, sqlx::FromRow)]
struct TargetRow {
    target_id: String,
    track_id: String,
    classification: String,
    threat_level: String,
    confidence: f64,
    latitude: f64,
    longitude: f64,
    altitude: f64,
    vn: f64,
    ve: f64,
    vd: f64,
    first_seen: String,
    last_update: String,
}

impl TargetRow {
    fn into_target(self) -> TrackedTarget {
        let classification = match self.classification.as_str() {
            "Dji" => TargetClass::Dji,
            "Autel" => TargetClass::Autel,
            "FixedWing" => TargetClass::FixedWing,
            "Multicopter" => TargetClass::Multicopter,
            "Bird" => TargetClass::Bird,
            "Helicopter" => TargetClass::Helicopter,
            "Unknown" => TargetClass::Unknown,
            other => TargetClass::Other(other.to_string()),
        };
        let threat_level = match self.threat_level.as_str() {
            "Red" => ThreatLevel::Red,
            "Yellow" => ThreatLevel::Yellow,
            _ => ThreatLevel::Green,
        };
        let parse_dt = |s: &str| -> chrono::DateTime<chrono::Utc> {
            chrono::DateTime::parse_from_rfc3339(s)
                .map(|dt| dt.with_timezone(&chrono::Utc))
                .unwrap_or_else(|_| chrono::Utc::now())
        };

        TrackedTarget {
            target_id: Uuid::parse_str(&self.target_id).unwrap_or_else(|_| Uuid::new_v4()),
            track_id: Uuid::parse_str(&self.track_id).unwrap_or_else(|_| Uuid::new_v4()),
            position: GeoPosition {
                latitude: self.latitude,
                longitude: self.longitude,
                altitude: self.altitude,
            },
            velocity: Velocity3D {
                vn: self.vn,
                ve: self.ve,
                vd: self.vd,
            },
            acceleration: None,
            classification,
            threat_level,
            confidence: self.confidence,
            first_seen: parse_dt(&self.first_seen),
            last_update: parse_dt(&self.last_update),
            track_history: vec![],
        }
    }
}

#[derive(Debug, sqlx::FromRow)]
struct TrackPointRow {
    timestamp: String,
    latitude: f64,
    longitude: f64,
    altitude: f64,
    vn: f64,
    ve: f64,
    vd: f64,
}

impl TrackPointRow {
    fn into_track_point(self) -> TrackPoint {
        let timestamp = chrono::DateTime::parse_from_rfc3339(&self.timestamp)
            .map(|dt| dt.with_timezone(&chrono::Utc))
            .unwrap_or_else(|_| chrono::Utc::now());

        TrackPoint {
            timestamp,
            position: GeoPosition {
                latitude: self.latitude,
                longitude: self.longitude,
                altitude: self.altitude,
            },
            velocity: Velocity3D {
                vn: self.vn,
                ve: self.ve,
                vd: self.vd,
            },
        }
    }
}

#[derive(Debug, sqlx::FromRow)]
struct MissionRow {
    mission_id: String,
    target_id: String,
    interceptor_ids: String,
    trajectory: String,
    priority: i32,
    status: String,
    created_at: String,
    updated_at: String,
}

impl MissionRow {
    fn into_mission(self) -> InterceptMission {
        let parse_dt = |s: &str| -> chrono::DateTime<chrono::Utc> {
            chrono::DateTime::parse_from_rfc3339(s)
                .map(|dt| dt.with_timezone(&chrono::Utc))
                .unwrap_or_else(|_| chrono::Utc::now())
        };
        let status = match self.status.as_str() {
            "Planning" => MissionStatus::Planning,
            "Active" => MissionStatus::Active,
            "Executing" => MissionStatus::Executing,
            "Completed" => MissionStatus::Completed,
            "Failed" => MissionStatus::Failed,
            "Cancelled" => MissionStatus::Cancelled,
            _ => MissionStatus::Planning,
        };
        let interceptor_ids: Vec<Uuid> = serde_json::from_str(&self.interceptor_ids).unwrap_or_default();
        let trajectory: Vec<Waypoint> = serde_json::from_str(&self.trajectory).unwrap_or_default();

        InterceptMission {
            mission_id: Uuid::parse_str(&self.mission_id).unwrap_or_else(|_| Uuid::new_v4()),
            target_id: Uuid::parse_str(&self.target_id).unwrap_or_else(|_| Uuid::new_v4()),
            assigned_interceptors: interceptor_ids,
            trajectory,
            priority: self.priority as u32,
            status,
            created_at: parse_dt(&self.created_at),
            updated_at: parse_dt(&self.updated_at),
        }
    }
}

#[derive(Debug, sqlx::FromRow)]
struct AlertRow {
    alert_id: String,
    alert_type: String,
    severity: String,
    title: String,
    description: String,
    source: String,
    timestamp: String,
}

impl AlertRow {
    fn into_alert(self) -> Option<crate::bus::AlertEvent> {
        let alert_id = Uuid::parse_str(&self.alert_id).ok()?;
        let alert_type = match self.alert_type.as_str() {
            "ThreatDetected" => crate::bus::AlertType::ThreatDetected,
            "MissionCritical" => crate::bus::AlertType::MissionCritical,
            "SystemError" => crate::bus::AlertType::SystemError,
            "SensorOffline" => crate::bus::AlertType::SensorOffline,
            "CommunicationLost" => crate::bus::AlertType::CommunicationLost,
            _ => return None,
        };
        let severity = match self.severity.as_str() {
            "Critical" => crate::bus::AlertSeverity::Critical,
            "Warning" => crate::bus::AlertSeverity::Warning,
            _ => crate::bus::AlertSeverity::Info,
        };
        let timestamp = chrono::DateTime::parse_from_rfc3339(&self.timestamp)
            .map(|dt| dt.with_timezone(&chrono::Utc))
            .ok()?;

        Some(crate::bus::AlertEvent {
            alert_id,
            alert_type,
            severity,
            title: self.title,
            description: self.description,
            source: self.source,
            timestamp,
        })
    }
}

// ==================== 设备管理 Repository 方法 ====================

impl Repository {
    // -- DeviceConfig (hsim_sb_pz) --
    pub async fn list_device_configs(&self, page: i64, page_size: i64, _filters: &std::collections::HashMap<String, String>) -> anyhow::Result<(Vec<crate::models::DeviceConfig>, i64)> {
        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_sb_pz").fetch_one(&self.pool).await?;
        let offset = (page - 1) * page_size;
        let rows = sqlx::query_as::<_, DeviceConfigRow>("SELECT id, name, device_id, station_id, device_type, device_ip, device_port, protocol_version, is_valid, status, update_time, jd, wd, gd, zcbj, dyqk, mac, xh, sccj, ccrq, type as row_type, udp_port FROM hsim_sb_pz ORDER BY id DESC LIMIT ?1 OFFSET ?2")
            .bind(page_size).bind(offset).fetch_all(&self.pool).await?;
        Ok((rows.into_iter().map(|r: DeviceConfigRow| r.into()).collect(), total.0))
    }
    pub async fn insert_device_config(&self, d: &crate::models::DeviceConfig) -> anyhow::Result<()> {
        sqlx::query("INSERT INTO hsim_sb_pz (name, device_id, station_id, device_type, device_ip, device_port, protocol_version, is_valid, status, update_time, jd, wd, gd, zcbj, dyqk, mac, xh, sccj, ccrq, type, udp_port) VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14,?15,?16,?17,?18,?19,?20,?21)")
            .bind(&d.name).bind(&d.device_id).bind(d.station_id).bind(&d.device_type).bind(&d.device_ip).bind(d.device_port).bind(&d.protocol_version).bind(d.is_valid).bind(&d.status).bind(&d.update_time).bind(d.jd).bind(d.wd).bind(d.gd).bind(&d.zcbj).bind(&d.dyqk).bind(&d.mac).bind(&d.xh).bind(&d.sccj).bind(&d.ccrq).bind(&d.r#type).bind(d.udp_port)
            .execute(&self.pool).await?;
        Ok(())
    }
    pub async fn update_device_config(&self, d: &crate::models::DeviceConfig) -> anyhow::Result<()> {
        sqlx::query("UPDATE hsim_sb_pz SET name=?1, device_type=?2, device_ip=?3, device_port=?4, is_valid=?5, status=?6, jd=?7, wd=?8, gd=?9, zcbj=?10, dyqk=?11, type=?12, udp_port=?13, update_time=datetime('now') WHERE id=?14")
            .bind(&d.name).bind(&d.device_type).bind(&d.device_ip).bind(d.device_port).bind(d.is_valid).bind(&d.status).bind(d.jd).bind(d.wd).bind(d.gd).bind(&d.zcbj).bind(&d.dyqk).bind(&d.r#type).bind(d.udp_port).bind(d.id)
            .execute(&self.pool).await?;
        Ok(())
    }
    pub async fn delete_device_config(&self, id: i64) -> anyhow::Result<()> {
        sqlx::query("DELETE FROM hsim_sb_pz WHERE id=?1").bind(id).execute(&self.pool).await?; Ok(())
    }
    pub async fn get_device_config(&self, id: i64) -> anyhow::Result<Option<crate::models::DeviceConfig>> {
        let row = sqlx::query_as::<_, DeviceConfigRow>("SELECT id, name, device_id, station_id, device_type, device_ip, device_port, protocol_version, is_valid, status, update_time, jd, wd, gd, zcbj, dyqk, mac, xh, sccj, ccrq, type as row_type, udp_port FROM hsim_sb_pz WHERE id=?1")
            .bind(id).fetch_optional(&self.pool).await?;
        Ok(row.map(|r| r.into()))
    }
    pub async fn device_stats(&self) -> anyhow::Result<serde_json::Value> {
        let detect: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_sb_pz WHERE device_type='DETECT'").fetch_one(&self.pool).await?;
        let disturb: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_sb_pz WHERE device_type='DISTURB'").fetch_one(&self.pool).await?;
        let trap: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_sb_pz WHERE device_type='TRAP'").fetch_one(&self.pool).await?;
        let connected: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_sb_pz WHERE status='CONNECTED'").fetch_one(&self.pool).await?;
        Ok(serde_json::json!({"detect": detect.0, "disturb": disturb.0, "trap": trap.0, "connected": connected.0}))
    }

    // -- DeviceHeartbeat (hsim_sb_xt) --
    pub async fn list_device_heartbeats(&self, page: i64, page_size: i64, _filters: &std::collections::HashMap<String, String>) -> anyhow::Result<(Vec<crate::models::DeviceHeartbeat>, i64)> {
        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_sb_xt").fetch_one(&self.pool).await?;
        let offset = (page - 1) * page_size;
        let rows = sqlx::query_as::<_, DeviceHeartbeatRow>("SELECT * FROM hsim_sb_xt ORDER BY id DESC LIMIT ?1 OFFSET ?2").bind(page_size).bind(offset).fetch_all(&self.pool).await?;
        Ok((rows.into_iter().map(|r| r.into()).collect(), total.0))
    }

    // -- ConnectLog (hsim_sb_lj) --
    pub async fn list_connect_logs(&self, page: i64, page_size: i64, _filters: &std::collections::HashMap<String, String>) -> anyhow::Result<(Vec<crate::models::ConnectLog>, i64)> {
        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_sb_lj").fetch_one(&self.pool).await?;
        let offset = (page - 1) * page_size;
        let rows = sqlx::query_as::<_, ConnectLogRow>("SELECT * FROM hsim_sb_lj ORDER BY event_time DESC LIMIT ?1 OFFSET ?2").bind(page_size).bind(offset).fetch_all(&self.pool).await?;
        Ok((rows.into_iter().map(|r| r.into()).collect(), total.0))
    }

    // -- OperateLog (hsim_sb_czrz) --
    pub async fn list_operate_logs(&self, page: i64, page_size: i64, _filters: &std::collections::HashMap<String, String>) -> anyhow::Result<(Vec<crate::models::OperateLog>, i64)> {
        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_sb_czrz").fetch_one(&self.pool).await?;
        let offset = (page - 1) * page_size;
        let rows = sqlx::query_as::<_, OperateLogRow>("SELECT * FROM hsim_sb_czrz ORDER BY operate_time DESC LIMIT ?1 OFFSET ?2").bind(page_size).bind(offset).fetch_all(&self.pool).await?;
        Ok((rows.into_iter().map(|r| r.into()).collect(), total.0))
    }

    // -- ActiveHeartbeat (hsim_sb_xt_zd) --
    pub async fn list_active_heartbeats(&self, page: i64, page_size: i64, _filters: &std::collections::HashMap<String, String>) -> anyhow::Result<(Vec<crate::models::ActiveHeartbeat>, i64)> {
        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_sb_xt_zd").fetch_one(&self.pool).await?;
        let offset = (page - 1) * page_size;
        let rows = sqlx::query_as::<_, ActiveHeartbeatRow>("SELECT * FROM hsim_sb_xt_zd ORDER BY id DESC LIMIT ?1 OFFSET ?2").bind(page_size).bind(offset).fetch_all(&self.pool).await?;
        Ok((rows.into_iter().map(|r| r.into()).collect(), total.0))
    }

    // -- DetectSpectrum (hsim_sb_zcpp) --
    pub async fn list_detect_spectrums(&self, page: i64, page_size: i64, filters: &std::collections::HashMap<String, String>) -> anyhow::Result<(Vec<crate::models::DetectSpectrum>, i64)> {
        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_sb_zcpp").fetch_one(&self.pool).await?;
        let offset = (page - 1) * page_size;
        if let Some(rq) = filters.get("rq") {
            if !rq.is_empty() {
                let rows = sqlx::query_as::<_, DetectSpectrumRow>("SELECT * FROM hsim_sb_zcpp WHERE date(data_time)=?1 ORDER BY id DESC LIMIT ?2 OFFSET ?3").bind(rq).bind(page_size).bind(offset).fetch_all(&self.pool).await?;
                let cnt: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_sb_zcpp WHERE date(data_time)=?1").bind(rq).fetch_one(&self.pool).await?;
                return Ok((rows.into_iter().map(|r| r.into()).collect(), cnt.0));
            }
        }
        let rows = sqlx::query_as::<_, DetectSpectrumRow>("SELECT * FROM hsim_sb_zcpp ORDER BY id DESC LIMIT ?1 OFFSET ?2").bind(page_size).bind(offset).fetch_all(&self.pool).await?;
        Ok((rows.into_iter().map(|r| r.into()).collect(), total.0))
    }
}

// ==================== 无人机管理 Repository 方法 ====================

impl Repository {
    // -- DroneFeatureLib (hsim_wrj_tzk) --
    pub async fn list_drone_feature_libs(&self, page: i64, page_size: i64, _filters: &std::collections::HashMap<String, String>) -> anyhow::Result<(Vec<crate::models::DroneFeatureLib>, i64)> {
        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_wrj_tzk").fetch_one(&self.pool).await?;
        let offset = (page - 1) * page_size;
        let rows = sqlx::query_as::<_, DroneFeatureLibRow>("SELECT * FROM hsim_wrj_tzk ORDER BY czsj DESC LIMIT ?1 OFFSET ?2").bind(page_size).bind(offset).fetch_all(&self.pool).await?;
        Ok((rows.into_iter().map(|r| r.into()).collect(), total.0))
    }
    pub async fn insert_drone_feature_lib(&self, d: &crate::models::DroneFeatureLib) -> anyhow::Result<()> {
        sqlx::query("INSERT INTO hsim_wrj_tzk (id,mc,serial_number,brand,model,type,dqfl,zlfl,jj,jc,xtczy,sx,sysx,zdsx,yxsx,jg,zzbj,kzbj,zcfw,yz,jz,rwzb,yxzh,zdsd,zdqfzl,xhsd,xhgd,xhsj,xhjl,qdbj,fdjsl,fxsd,zz,kz,zdhc,scdw,qymc,fdj,dlzz,jzcl,dmczry,dwjd,yxcsjl,cd,wx,hdfs,zj,nyzl,hs,gzqs,zcl,hc,dzpt,dy,dzsb,hldjnl,ldjdzsb,td,jgtd,zczb,zznl,zcjsnl,bpjbs,remark,tp,mxdz,cjr,cjrid,cjsj,czr,czrid,czsj,zdxhsd,wrjxhsj,tcnl) VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14,?15,?16,?17,?18,?19,?20,?21,?22,?23,?24,?25,?26,?27,?28,?29,?30,?31,?32,?33,?34,?35,?36,?37,?38,?39,?40,?41,?42,?43,?44,?45,?46,?47,?48,?49,?50,?51,?52,?53,?54,?55,?56,?57,?58,?59,?60,?61,?62,?63,?64,?65,?66,?67,?68,?69,?70,?71,?72,?73,?74,?75)")
            .bind(&d.id).bind(&d.mc).bind(&d.serial_number).bind(&d.brand).bind(&d.model).bind(&d.r#type).bind(&d.dqfl).bind(&d.zlfl).bind(&d.jj).bind(&d.jc).bind(&d.xtczy).bind(&d.sx).bind(&d.sysx).bind(&d.zdsx).bind(&d.yxsx).bind(&d.jg).bind(&d.zzbj).bind(&d.kzbj).bind(&d.zcfw).bind(&d.yz).bind(&d.jz).bind(&d.rwzb).bind(&d.yxzh).bind(&d.zdsd).bind(&d.zdqfzl).bind(&d.xhsd).bind(&d.xhgd).bind(&d.xhsj).bind(&d.xhjl).bind(&d.qdbj).bind(&d.fdjsl).bind(&d.fxsd).bind(&d.zz).bind(&d.kz).bind(&d.zdhc).bind(&d.scdw).bind(&d.qymc).bind(&d.fdj).bind(&d.dlzz).bind(&d.jzcl).bind(&d.dmczry).bind(&d.dwjd).bind(&d.yxcsjl).bind(&d.cd).bind(&d.wx).bind(&d.hdfs).bind(&d.zj).bind(&d.nyzl).bind(&d.hs).bind(&d.gzqs).bind(&d.zcl).bind(&d.hc).bind(&d.dzpt).bind(&d.dy).bind(&d.dzsb).bind(&d.hldjnl).bind(&d.ldjdzsb).bind(&d.td).bind(&d.jgtd).bind(&d.zczb).bind(&d.zznl).bind(&d.zcjsnl).bind(&d.bpjbs).bind(&d.remark).bind(&d.tp).bind(&d.mxdz).bind(&d.cjr).bind(&d.cjrid).bind(&d.cjsj).bind(&d.czr).bind(&d.czrid).bind(&d.czsj).bind(&d.zdxhsd).bind(&d.wrjxhsj).bind(&d.tcnl)
            .execute(&self.pool).await?; Ok(())
    }
    pub async fn update_drone_feature_lib(&self, d: &crate::models::DroneFeatureLib) -> anyhow::Result<()> {
        sqlx::query("UPDATE hsim_wrj_tzk SET mc=?1,serial_number=?2,brand=?3,model=?4,type=?5,dqfl=?6,zlfl=?7,jj=?8,remark=?9,tp=?10,mxdz=?11,czr=?12,czrid=?13,czsj=datetime('now') WHERE id=?14")
            .bind(&d.mc).bind(&d.serial_number).bind(&d.brand).bind(&d.model).bind(&d.r#type).bind(&d.dqfl).bind(&d.zlfl).bind(&d.jj).bind(&d.remark).bind(&d.tp).bind(&d.mxdz).bind(&d.czr).bind(&d.czrid).bind(&d.id).execute(&self.pool).await?; Ok(())
    }
    pub async fn delete_drone_feature_lib(&self, id: &str) -> anyhow::Result<()> { sqlx::query("DELETE FROM hsim_wrj_tzk WHERE id=?1").bind(id).execute(&self.pool).await?; Ok(()) }
    pub async fn get_drone_feature_lib(&self, id: &str) -> anyhow::Result<Option<crate::models::DroneFeatureLib>> {
        let row = sqlx::query_as::<_, DroneFeatureLibRow>("SELECT * FROM hsim_wrj_tzk WHERE id=?1").bind(id).fetch_optional(&self.pool).await?;
        Ok(row.map(|r| r.into()))
    }

    // -- DroneBasicInfo (hsim_wrj_jbxx) --
    pub async fn list_drone_basic_infos(&self, page: i64, page_size: i64, _filters: &std::collections::HashMap<String, String>) -> anyhow::Result<(Vec<crate::models::DroneBasicInfo>, i64)> {
        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_wrj_jbxx").fetch_one(&self.pool).await?;
        let offset = (page - 1) * page_size;
        let rows = sqlx::query_as::<_, DroneBasicInfoRow>("SELECT * FROM hsim_wrj_jbxx ORDER BY czsj DESC LIMIT ?1 OFFSET ?2").bind(page_size).bind(offset).fetch_all(&self.pool).await?;
        Ok((rows.into_iter().map(|r| r.into()).collect(), total.0))
    }
    pub async fn insert_drone_basic_info(&self, d: &crate::models::DroneBasicInfo) -> anyhow::Result<()> {
        sqlx::query("INSERT INTO hsim_wrj_jbxx (id,serial_number,brand,model,status,auth_status,current_longitude,current_latitude,current_altitude,last_seen_time,remark,rwlx,ssdw,tp,mxdz,cjr,cjrid,cjsj,czr,czrid,czsj) VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14,?15,?16,?17,?18,?19,?20,?21)")
            .bind(&d.id).bind(&d.serial_number).bind(&d.brand).bind(&d.model).bind(d.status).bind(d.auth_status).bind(d.current_longitude).bind(d.current_latitude).bind(d.current_altitude).bind(&d.last_seen_time).bind(&d.remark).bind(&d.rwlx).bind(&d.ssdw).bind(&d.tp).bind(&d.mxdz).bind(&d.cjr).bind(&d.cjrid).bind(&d.cjsj).bind(&d.czr).bind(&d.czrid).bind(&d.czsj)
            .execute(&self.pool).await?; Ok(())
    }
    pub async fn update_drone_basic_info(&self, d: &crate::models::DroneBasicInfo) -> anyhow::Result<()> {
        sqlx::query("UPDATE hsim_wrj_jbxx SET serial_number=?1,brand=?2,model=?3,status=?4,auth_status=?5,current_longitude=?6,current_latitude=?7,current_altitude=?8,last_seen_time=?9,remark=?10,rwlx=?11,ssdw=?12,tp=?13,mxdz=?14,czr=?15,czrid=?16,czsj=datetime('now') WHERE id=?17")
            .bind(&d.serial_number).bind(&d.brand).bind(&d.model).bind(d.status).bind(d.auth_status).bind(d.current_longitude).bind(d.current_latitude).bind(d.current_altitude).bind(&d.last_seen_time).bind(&d.remark).bind(&d.rwlx).bind(&d.ssdw).bind(&d.tp).bind(&d.mxdz).bind(&d.czr).bind(&d.czrid).bind(&d.id).execute(&self.pool).await?; Ok(())
    }
    pub async fn update_drone_auth_status(&self, id: &str, auth_status: i32) -> anyhow::Result<()> {
        sqlx::query("UPDATE hsim_wrj_jbxx SET auth_status=?1, czsj=datetime('now') WHERE id=?2").bind(auth_status).bind(id).execute(&self.pool).await?; Ok(())
    }
    pub async fn delete_drone_basic_info(&self, id: &str) -> anyhow::Result<()> { sqlx::query("DELETE FROM hsim_wrj_jbxx WHERE id=?1").bind(id).execute(&self.pool).await?; Ok(()) }
    pub async fn get_drone_basic_info(&self, id: &str) -> anyhow::Result<Option<crate::models::DroneBasicInfo>> {
        let row = sqlx::query_as::<_, DroneBasicInfoRow>("SELECT * FROM hsim_wrj_jbxx WHERE id=?1").bind(id).fetch_optional(&self.pool).await?;
        Ok(row.map(|r| r.into()))
    }
    pub async fn drone_basic_stats(&self) -> anyhow::Result<serde_json::Value> {
        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_wrj_jbxx").fetch_one(&self.pool).await?;
        let white: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_wrj_jbxx WHERE auth_status=1").fetch_one(&self.pool).await?;
        let black: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_wrj_jbxx WHERE auth_status=2").fetch_one(&self.pool).await?;
        Ok(serde_json::json!({"total": total.0, "whitelist": white.0, "blacklist": black.0}))
    }

    // -- BlackWhiteList (hsim_wrj_hbmdsq) --
    pub async fn list_black_white_lists(&self, page: i64, page_size: i64, _filters: &std::collections::HashMap<String, String>) -> anyhow::Result<(Vec<crate::models::BlackWhiteList>, i64)> {
        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_wrj_hbmdsq").fetch_one(&self.pool).await?;
        let offset = (page - 1) * page_size;
        let rows = sqlx::query_as::<_, BlackWhiteListRow>("SELECT * FROM hsim_wrj_hbmdsq ORDER BY czsj DESC LIMIT ?1 OFFSET ?2").bind(page_size).bind(offset).fetch_all(&self.pool).await?;
        Ok((rows.into_iter().map(|r| r.into()).collect(), total.0))
    }
    pub async fn get_bw_list_grouped(&self) -> anyhow::Result<serde_json::Value> {
        let white: Vec<BlackWhiteListRow> = sqlx::query_as("SELECT * FROM hsim_wrj_hbmdsq WHERE mdlx='白名单'").fetch_all(&self.pool).await?;
        let black: Vec<BlackWhiteListRow> = sqlx::query_as("SELECT * FROM hsim_wrj_hbmdsq WHERE mdlx='黑名单'").fetch_all(&self.pool).await?;
        Ok(serde_json::json!({"白名单": white.into_iter().map(|r| r.into()).collect::<Vec<_>>(), "黑名单": black.into_iter().map(|r| r.into()).collect::<Vec<_>>()}))
    }
    pub async fn insert_black_white_list(&self, d: &crate::models::BlackWhiteList) -> anyhow::Result<()> {
        sqlx::query("INSERT INTO hsim_wrj_hbmdsq (id,mdlx,wrjid,sqsj,sqgqsj,cjr,cjrid,cjsj,czr,czrid,czsj) VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11)")
            .bind(&d.id).bind(&d.mdlx).bind(&d.wrjid).bind(&d.sqsj).bind(&d.sqgqsj).bind(&d.cjr).bind(&d.cjrid).bind(&d.cjsj).bind(&d.czr).bind(&d.czrid).bind(&d.czsj)
            .execute(&self.pool).await?; Ok(())
    }
    pub async fn delete_black_white_list(&self, id: &str) -> anyhow::Result<()> { sqlx::query("DELETE FROM hsim_wrj_hbmdsq WHERE id=?1").bind(id).execute(&self.pool).await?; Ok(()) }

    // -- AlarmRecord (hsim_wrj_gjjl) --
    pub async fn list_alarm_records(&self, page: i64, page_size: i64, _filters: &std::collections::HashMap<String, String>) -> anyhow::Result<(Vec<crate::models::AlarmRecord>, i64)> {
        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_wrj_gjjl").fetch_one(&self.pool).await?;
        let offset = (page - 1) * page_size;
        let rows = sqlx::query_as::<_, AlarmRecordRow>("SELECT * FROM hsim_wrj_gjjl ORDER BY gjfssj DESC LIMIT ?1 OFFSET ?2").bind(page_size).bind(offset).fetch_all(&self.pool).await?;
        Ok((rows.into_iter().map(|r| r.into()).collect(), total.0))
    }
    pub async fn insert_alarm_record(&self, d: &crate::models::AlarmRecord) -> anyhow::Result<()> {
        sqlx::query("INSERT INTO hsim_wrj_gjjl (id,kymc,kyid,wrjid,wrjpp,wrjxh,wrjxlh,gjlx,gjfsjd,gjfsgd,gjfswd,gjfssj,clzt,clsj,clrid,clr,clbz,fsjd,fswd,zdid,zdmc,gjys,cjr,cjrid,cjsj,czr,czrid,czsj) VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14,?15,?16,?17,?18,?19,?20,?21,?22,?23,?24,?25,?26,?27,?28)")
            .bind(&d.id).bind(&d.kymc).bind(&d.kyid).bind(&d.wrjid).bind(&d.wrjpp).bind(&d.wrjxh).bind(&d.wrjxlh).bind(&d.gjlx).bind(d.gjfsjd).bind(d.gjfsgd).bind(d.gjfswd).bind(&d.gjfssj).bind(&d.clzt).bind(&d.clsj).bind(&d.clrid).bind(&d.clr).bind(&d.clbz).bind(d.fsjd).bind(d.fswd).bind(&d.zdid).bind(&d.zdmc).bind(&d.gjys).bind(&d.cjr).bind(&d.cjrid).bind(&d.cjsj).bind(&d.czr).bind(&d.czrid).bind(&d.czsj)
            .execute(&self.pool).await?; Ok(())
    }
    pub async fn update_alarm_record(&self, d: &crate::models::AlarmRecord) -> anyhow::Result<()> {
        sqlx::query("UPDATE hsim_wrj_gjjl SET clzt=?1,clsj=?2,clrid=?3,clr=?4,clbz=?5,czr=?6,czrid=?7,czsj=datetime('now') WHERE id=?8")
            .bind(&d.clzt).bind(&d.clsj).bind(&d.clrid).bind(&d.clr).bind(&d.clbz).bind(&d.czr).bind(&d.czrid).bind(&d.id).execute(&self.pool).await?; Ok(())
    }
    pub async fn delete_alarm_record(&self, id: &str) -> anyhow::Result<()> { sqlx::query("DELETE FROM hsim_wrj_gjjl WHERE id=?1").bind(id).execute(&self.pool).await?; Ok(()) }
    pub async fn get_alarm_record(&self, id: &str) -> anyhow::Result<Option<crate::models::AlarmRecord>> {
        let row = sqlx::query_as::<_, AlarmRecordRow>("SELECT * FROM hsim_wrj_gjjl WHERE id=?1").bind(id).fetch_optional(&self.pool).await?;
        Ok(row.map(|r| r.into()))
    }
    pub async fn alarm_stats(&self, _params: &std::collections::HashMap<String, String>) -> anyhow::Result<serde_json::Value> {
        let unprocessed: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_wrj_gjjl WHERE clzt='0'").fetch_one(&self.pool).await?;
        let processed: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_wrj_gjjl WHERE clzt='1'").fetch_one(&self.pool).await?;
        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_wrj_gjjl").fetch_one(&self.pool).await?;
        Ok(serde_json::json!({"total": total.0, "unprocessed": unprocessed.0, "processed": processed.0}))
    }

    // -- Airspace (hsim_wrj_ky) --
    pub async fn list_airspaces(&self, page: i64, page_size: i64, _filters: &std::collections::HashMap<String, String>) -> anyhow::Result<(Vec<crate::models::Airspace>, i64)> {
        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_wrj_ky").fetch_one(&self.pool).await?;
        let offset = (page - 1) * page_size;
        let rows = sqlx::query_as::<_, AirspaceRow>("SELECT * FROM hsim_wrj_ky ORDER BY czsj DESC LIMIT ?1 OFFSET ?2").bind(page_size).bind(offset).fetch_all(&self.pool).await?;
        Ok((rows.into_iter().map(|r| r.into()).collect(), total.0))
    }
    pub async fn insert_airspace(&self, d: &crate::models::Airspace) -> anyhow::Result<()> {
        sqlx::query("INSERT INTO hsim_wrj_ky (id,mc,lx,xz,zxdjd,zxdwd,bj,zxgd,zdgd,ddzbjh,cd,kd,kssj,jssj,sfqy,bz,ys,jfqbj,jfqys,yjqbj,yjqys,cjr,cjrid,cjsj,czr,czrid,czsj) VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14,?15,?16,?17,?18,?19,?20,?21,?22,?23,?24,?25,?26,?27)")
            .bind(&d.id).bind(&d.mc).bind(&d.lx).bind(&d.xz).bind(d.zxdjd).bind(d.zxdwd).bind(d.bj).bind(d.zxgd).bind(d.zdgd).bind(&d.ddzbjh).bind(d.cd).bind(d.kd).bind(&d.kssj).bind(&d.jssj).bind(d.sfqy).bind(&d.bz).bind(&d.ys).bind(d.jfqbj).bind(&d.jfqys).bind(d.yjqbj).bind(&d.yjqys).bind(&d.cjr).bind(&d.cjrid).bind(&d.cjsj).bind(&d.czr).bind(&d.czrid).bind(&d.czsj)
            .execute(&self.pool).await?; Ok(())
    }
    pub async fn update_airspace(&self, d: &crate::models::Airspace) -> anyhow::Result<()> {
        sqlx::query("UPDATE hsim_wrj_ky SET mc=?1,lx=?2,xz=?3,zxdjd=?4,zxdwd=?5,bj=?6,zxgd=?7,zdgd=?8,ddzbjh=?9,cd=?10,kd=?11,kssj=?12,jssj=?13,sfqy=?14,bz=?15,ys=?16,jfqbj=?17,jfqys=?18,yjqbj=?19,yjqys=?20,czr=?21,czrid=?22,czsj=datetime('now') WHERE id=?23")
            .bind(&d.mc).bind(&d.lx).bind(&d.xz).bind(d.zxdjd).bind(d.zxdwd).bind(d.bj).bind(d.zxgd).bind(d.zdgd).bind(&d.ddzbjh).bind(d.cd).bind(d.kd).bind(&d.kssj).bind(&d.jssj).bind(d.sfqy).bind(&d.bz).bind(&d.ys).bind(d.jfqbj).bind(&d.jfqys).bind(d.yjqbj).bind(&d.yjqys).bind(&d.czr).bind(&d.czrid).bind(&d.id).execute(&self.pool).await?; Ok(())
    }
    pub async fn delete_airspace(&self, id: &str) -> anyhow::Result<()> { sqlx::query("DELETE FROM hsim_wrj_ky WHERE id=?1").bind(id).execute(&self.pool).await?; Ok(()) }
    pub async fn get_airspace(&self, id: &str) -> anyhow::Result<Option<crate::models::Airspace>> {
        let row = sqlx::query_as::<_, AirspaceRow>("SELECT * FROM hsim_wrj_ky WHERE id=?1").bind(id).fetch_optional(&self.pool).await?;
        Ok(row.map(|r| r.into()))
    }
}

// ==================== 飞行数据 Repository 方法 ====================
impl Repository {
    // -- DroneDetectMsg (hsim_wrj_fxsj_zcbw) --
    pub async fn list_drone_detect_msgs(&self, page: i64, page_size: i64, _filters: &std::collections::HashMap<String, String>) -> anyhow::Result<(Vec<crate::models::DroneDetectMsg>, i64)> {
        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_wrj_fxsj_zcbw").fetch_one(&self.pool).await?;
        let offset = (page - 1) * page_size;
        let rows = sqlx::query_as::<_, DroneDetectMsgRow>("SELECT * FROM hsim_wrj_fxsj_zcbw ORDER BY data_time DESC LIMIT ?1 OFFSET ?2").bind(page_size).bind(offset).fetch_all(&self.pool).await?;
        Ok((rows.into_iter().map(|r| r.into()).collect(), total.0))
    }
    pub async fn get_drone_detect_msg(&self, id: i64) -> anyhow::Result<Option<crate::models::DroneDetectMsg>> {
        let row = sqlx::query_as::<_, DroneDetectMsgRow>("SELECT * FROM hsim_wrj_fxsj_zcbw WHERE id=?1").bind(id).fetch_optional(&self.pool).await?;
        Ok(row.map(|r| r.into()))
    }

    // -- DroneDfData (hsim_wrj_fxsj_jx) --
    pub async fn list_drone_df_data(&self, page: i64, page_size: i64, _filters: &std::collections::HashMap<String, String>) -> anyhow::Result<(Vec<crate::models::DroneDfData>, i64)> {
        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_wrj_fxsj_jx").fetch_one(&self.pool).await?;
        let offset = (page - 1) * page_size;
        let rows = sqlx::query_as::<_, DroneDfDataRow>("SELECT * FROM hsim_wrj_fxsj_jx ORDER BY data_time DESC LIMIT ?1 OFFSET ?2").bind(page_size).bind(offset).fetch_all(&self.pool).await?;
        Ok((rows.into_iter().map(|r| r.into()).collect(), total.0))
    }

    // -- DroneRemoteData (hsim_wrj_fxsj_remote) --
    pub async fn list_drone_remote_data(&self, page: i64, page_size: i64, _filters: &std::collections::HashMap<String, String>) -> anyhow::Result<(Vec<crate::models::DroneRemoteData>, i64)> {
        let total: (i64,) = sqlx::query_as("SELECT COUNT(*) FROM hsim_wrj_fxsj_remote").fetch_one(&self.pool).await?;
        let offset = (page - 1) * page_size;
        let rows = sqlx::query_as::<_, DroneRemoteDataRow>("SELECT * FROM hsim_wrj_fxsj_remote ORDER BY date DESC LIMIT ?1 OFFSET ?2").bind(page_size).bind(offset).fetch_all(&self.pool).await?;
        Ok((rows.into_iter().map(|r| r.into()).collect(), total.0))
    }

    // ==================== wxdzc 侦测预警专用方法 ====================

    /// wxdzc: 分页查询设备列表（带名称过滤）
    pub async fn list_devices_for_wxdzc(
        &self, page: i64, page_size: i64, name_filter: Option<&str>,
    ) -> anyhow::Result<(Vec<crate::models::DeviceConfig>, i64)> {
        if let Some(name) = name_filter {
            if !name.is_empty() {
                let pattern = format!("%{}%", name);
                let total: (i64,) = sqlx::query_as(
                    "SELECT COUNT(*) FROM hsim_sb_pz WHERE device_type != 'System' AND name LIKE ?1"
                ).bind(&pattern).fetch_one(&self.pool).await?;
                let offset = (page - 1) * page_size;
                let rows = sqlx::query_as::<_, DeviceConfigRow>(
                    "SELECT id, name, device_id, station_id, device_type, device_ip, device_port, protocol_version, is_valid, status, update_time, jd, wd, gd, zcbj, dyqk, mac, xh, sccj, ccrq, type as row_type, udp_port FROM hsim_sb_pz WHERE device_type != 'System' AND name LIKE ?1 ORDER BY update_time DESC LIMIT ?2 OFFSET ?3"
                ).bind(&pattern).bind(page_size).bind(offset).fetch_all(&self.pool).await?;
                return Ok((rows.into_iter().map(|r| r.into()).collect(), total.0));
            }
        }
        let total: (i64,) = sqlx::query_as(
            "SELECT COUNT(*) FROM hsim_sb_pz WHERE device_type != 'System'"
        ).fetch_one(&self.pool).await?;
        let offset = (page - 1) * page_size;
        let rows = sqlx::query_as::<_, DeviceConfigRow>(
            "SELECT id, name, device_id, station_id, device_type, device_ip, device_port, protocol_version, is_valid, status, update_time, jd, wd, gd, zcbj, dyqk, mac, xh, sccj, ccrq, type as row_type, udp_port FROM hsim_sb_pz WHERE device_type != 'System' ORDER BY update_time DESC LIMIT ?1 OFFSET ?2"
        ).bind(page_size).bind(offset).fetch_all(&self.pool).await?;
        Ok((rows.into_iter().map(|r| r.into()).collect(), total.0))
    }

    /// wxdzc: 获取所有设备（不分页，用于地图标注）
    pub async fn list_all_devices_for_wxdzc(&self) -> anyhow::Result<Vec<crate::models::DeviceConfig>> {
        let rows = sqlx::query_as::<_, DeviceConfigRow>(
            "SELECT id, name, device_id, station_id, device_type, device_ip, device_port, protocol_version, is_valid, status, update_time, jd, wd, gd, zcbj, dyqk, mac, xh, sccj, ccrq, type as row_type, udp_port FROM hsim_sb_pz WHERE device_type != 'System' ORDER BY update_time DESC"
        ).fetch_all(&self.pool).await?;
        Ok(rows.into_iter().map(|r| r.into()).collect())
    }

    /// wxdzc: 按站点分组获取最新侦测消息 (ROW_NUMBER 窗口函数)
    /// 对应 Java: getUavDetectMsgByStationId
    pub async fn get_uav_detect_msg_by_station_id(
        &self, rq: Option<&str>, auth_status: Option<i32>,
    ) -> anyhow::Result<Vec<WxdzcDetectMsgRow>> {
        let rq_val = rq.unwrap_or("");
        let has_rq = !rq_val.is_empty();

        // Build query dynamically based on whether filters are present
        let base_sql = if has_rq {
            r#"
            SELECT m.*, j.brand, j.status as jbxx_status, j.auth_status, j.rwlx, j.ssdw,
                   d.name as station_name
            FROM (
                SELECT *, ROW_NUMBER() OVER (PARTITION BY serial ORDER BY data_time DESC) as rn
                FROM hsim_wrj_fxsj_zcbw
                WHERE date(data_time) = ?1
            ) m
            LEFT JOIN hsim_wrj_jbxx j ON (
                COALESCE(j.serial_number, '') = COALESCE(m.serial, '')
                OR (j.brand = m.model AND j.model = m.serial)
                OR j.model = m.model
            )
            LEFT JOIN hsim_sb_pz d ON d.station_id = m.station_id
            WHERE m.rn = 1
            "#.to_string()
        } else {
            r#"
            SELECT m.*, j.brand, j.status as jbxx_status, j.auth_status, j.rwlx, j.ssdw,
                   d.name as station_name
            FROM (
                SELECT *, ROW_NUMBER() OVER (PARTITION BY serial ORDER BY data_time DESC) as rn
                FROM hsim_wrj_fxsj_zcbw
            ) m
            LEFT JOIN hsim_wrj_jbxx j ON (
                COALESCE(j.serial_number, '') = COALESCE(m.serial, '')
                OR (j.brand = m.model AND j.model = m.serial)
                OR j.model = m.model
            )
            LEFT JOIN hsim_sb_pz d ON d.station_id = m.station_id
            WHERE m.rn = 1
            "#.to_string()
        };

        if auth_status.is_some() {
            // Note: auth_status filtering in SQLite — the Java version filters by
            // auth_status at the MyBatis level; we do post-filter in Rust for simplicity
        }

        if has_rq {
            let rows = sqlx::query_as::<_, WxdzcDetectMsgRow>(&base_sql)
                .bind(rq_val)
                .fetch_all(&self.pool).await?;
            Ok(rows)
        } else {
            let rows = sqlx::query_as::<_, WxdzcDetectMsgRow>(&base_sql)
                .fetch_all(&self.pool).await?;
            Ok(rows)
        }
    }

    /// wxdzc: 按型号序列号查询飞行路径数据
    /// 对应 Java: getUavDetectMsgByModelSerialRq
    pub async fn get_uav_detect_msg_by_model_serial_rq(
        &self, model: Option<&str>, serial: Option<&str>, rq: Option<&str>,
    ) -> anyhow::Result<Vec<crate::models::DroneDetectMsg>> {
        let serial_val = serial.unwrap_or("");
        let rq_val = rq.unwrap_or("");
        let model_val = model.unwrap_or("");

        let rows = sqlx::query_as::<_, DroneDetectMsgRow>(
            r#"SELECT * FROM hsim_wrj_fxsj_zcbw
               WHERE serial = ?1 AND dron_lng IS NOT NULL AND dron_lng != 0
               AND date(data_time) = ?2
               ORDER BY data_time ASC"#
        ).bind(serial_val).bind(rq_val).fetch_all(&self.pool).await?;

        if rows.is_empty() && !model_val.is_empty() {
            // Fallback: try by model if serial didn't match
            let rows2 = sqlx::query_as::<_, DroneDetectMsgRow>(
                r#"SELECT * FROM hsim_wrj_fxsj_zcbw
                   WHERE model = ?1 AND dron_lng IS NOT NULL AND dron_lng != 0
                   AND date(data_time) = ?2
                   ORDER BY data_time ASC"#
            ).bind(model_val).bind(rq_val).fetch_all(&self.pool).await?;
            return Ok(rows2.into_iter().map(|r| r.into()).collect());
        }
        Ok(rows.into_iter().map(|r| r.into()).collect())
    }

    /// wxdzc: 获取日期日历数据（哪些日期有侦测数据）
    /// 对应 Java: getUavDetectMsgDateByNfYf
    pub async fn get_uav_detect_msg_date_by_nf_yf(
        &self, nf: Option<&str>, yf: Option<&str>,
    ) -> anyhow::Result<Vec<crate::models::UavDetectMsgDateVo>> {
        let nf_val = nf.unwrap_or("");
        let yf_val = yf.unwrap_or("");

        let rows = sqlx::query_as::<_, UavDetectMsgDateVoRow>(
            r#"SELECT DISTINCT date(data_time) as rq, '无人机飞行' as r_type
               FROM hsim_wrj_fxsj_zcbw
               WHERE strftime('%Y', data_time) = ?1
                 AND strftime('%m', data_time) = substr('0' || ?2, -2)
               ORDER BY data_time DESC"#
        ).bind(nf_val).bind(yf_val).fetch_all(&self.pool).await?;
        Ok(rows.into_iter().map(|r| r.into()).collect())
    }

    /// wxdzc: 按序列号查询无人机基本信息
    pub async fn get_drone_basic_info_by_serial(
        &self, serial: &str,
    ) -> anyhow::Result<Option<crate::models::DroneBasicInfo>> {
        let row = sqlx::query_as::<_, DroneBasicInfoRow>(
            "SELECT * FROM hsim_wrj_jbxx WHERE serial_number = ?1"
        ).bind(serial).fetch_optional(&self.pool).await?;
        Ok(row.map(|r| r.into()))
    }
}

// ==================== Row 类型定义 (sqlx::FromRow) ====================

#[derive(Debug, sqlx::FromRow)] struct DeviceConfigRow { id: Option<i64>, name: Option<String>, device_id: String, station_id: i32, device_type: String, device_ip: String, device_port: i32, protocol_version: Option<String>, is_valid: i32, status: Option<String>, update_time: Option<String>, jd: Option<f64>, wd: Option<f64>, gd: Option<f64>, zcbj: Option<String>, dyqk: Option<String>, mac: Option<String>, xh: Option<String>, sccj: Option<String>, ccrq: Option<String>, row_type: Option<String>, udp_port: Option<i32> }
impl DeviceConfigRow { fn into(self) -> crate::models::DeviceConfig { crate::models::DeviceConfig { id: self.id, name: self.name, device_id: self.device_id, station_id: self.station_id, device_type: self.device_type, device_ip: self.device_ip, device_port: self.device_port, protocol_version: self.protocol_version, is_valid: self.is_valid, status: self.status, update_time: self.update_time, jd: self.jd, wd: self.wd, gd: self.gd, zcbj: self.zcbj, dyqk: self.dyqk, mac: self.mac, xh: self.xh, sccj: self.sccj, ccrq: self.ccrq, r#type: self.row_type, udp_port: self.udp_port } } }

#[derive(Debug, sqlx::FromRow)] struct DeviceHeartbeatRow { id: Option<i64>, station_id: Option<i32>, data_time: Option<String>, main_card: Option<i32>, trap_card: Option<i32>, compass: Option<i32>, disturb_card: Option<i32>, longitude: Option<f64>, latitude: Option<f64>, altitude: Option<i32>, angle: Option<f64>, cpu_rate: Option<f64>, disk_usage: Option<f64>, card_temp: Option<f64>, amp_temp: Option<f64>, create_time: Option<String>, work_state: Option<i32> }
impl DeviceHeartbeatRow { fn into(self) -> crate::models::DeviceHeartbeat { crate::models::DeviceHeartbeat { id: self.id, station_id: self.station_id, data_time: self.data_time, main_card: self.main_card, trap_card: self.trap_card, compass: self.compass, disturb_card: self.disturb_card, longitude: self.longitude, latitude: self.latitude, altitude: self.altitude, angle: self.angle, cpu_rate: self.cpu_rate, disk_usage: self.disk_usage, card_temp: self.card_temp, amp_temp: self.amp_temp, create_time: self.create_time, work_state: self.work_state } } }

#[derive(Debug, sqlx::FromRow)] struct ConnectLogRow { id: Option<i64>, station_id: i32, device_ip: String, device_port: i32, event_type: String, event_time: Option<String>, reason: Option<String> }
impl ConnectLogRow { fn into(self) -> crate::models::ConnectLog { crate::models::ConnectLog { id: self.id, station_id: self.station_id, device_ip: self.device_ip, device_port: self.device_port, event_type: self.event_type, event_time: self.event_time, reason: self.reason } } }

#[derive(Debug, sqlx::FromRow)] struct OperateLogRow { id: Option<i64>, station_id: Option<i32>, device_type: Option<String>, cmd_type: Option<String>, cmd_name: Option<String>, cmd_param: Option<String>, result: Option<String>, operate_time: Option<String> }
impl OperateLogRow { fn into(self) -> crate::models::OperateLog { crate::models::OperateLog { id: self.id, station_id: self.station_id, device_type: self.device_type, cmd_type: self.cmd_type, cmd_name: self.cmd_name, cmd_param: self.cmd_param, result: self.result, operate_time: self.operate_time } } }

#[derive(Debug, sqlx::FromRow)] struct ActiveHeartbeatRow { id: Option<i64>, start_code: Option<String>, source_addr: Option<i32>, dest_addr: Option<i32>, command: Option<i32>, param_length: Option<i32>, terminal_code: Option<String>, station_id: Option<i32>, data_time: Option<String>, auth_normal: Option<i32>, detect_enabled: Option<i32>, counter_enabled: Option<i32>, detector_online: Option<i32>, counter_online: Option<i32>, unattended_mode: Option<i32>, deceiver_online: Option<i32>, jamming_mode: Option<i32>, band_58g: Option<i32>, band_24g: Option<i32>, band_900m: Option<i32>, band_14g: Option<i32>, band_52g: Option<i32>, ptz_control_mode: Option<i32>, attack_countdown: Option<i32>, deception_status: Option<i32>, gnss_link_status: Option<i32>, gnss_deception_mode: Option<i32>, gnss_induce_mode: Option<i32>, no_fly_lat: Option<f64>, no_fly_lng: Option<f64>, no_fly_alt: Option<f64>, forced_land_lat: Option<f64>, forced_land_lng: Option<f64>, forced_land_alt: Option<f64>, forced_land_radius: Option<f64>, checksum: Option<i32>, create_time: Option<String>, update_time: Option<String> }
impl ActiveHeartbeatRow { fn into(self) -> crate::models::ActiveHeartbeat { crate::models::ActiveHeartbeat { id: self.id, start_code: self.start_code, source_addr: self.source_addr, dest_addr: self.dest_addr, command: self.command, param_length: self.param_length, terminal_code: self.terminal_code, station_id: self.station_id, data_time: self.data_time, auth_normal: self.auth_normal, detect_enabled: self.detect_enabled, counter_enabled: self.counter_enabled, detector_online: self.detector_online, counter_online: self.counter_online, unattended_mode: self.unattended_mode, deceiver_online: self.deceiver_online, jamming_mode: self.jamming_mode, band_58g: self.band_58g, band_24g: self.band_24g, band_900m: self.band_900m, band_14g: self.band_14g, band_52g: self.band_52g, ptz_control_mode: self.ptz_control_mode, attack_countdown: self.attack_countdown, deception_status: self.deception_status, gnss_link_status: self.gnss_link_status, gnss_deception_mode: self.gnss_deception_mode, gnss_induce_mode: self.gnss_induce_mode, no_fly_lat: self.no_fly_lat, no_fly_lng: self.no_fly_lng, no_fly_alt: self.no_fly_alt, forced_land_lat: self.forced_land_lat, forced_land_lng: self.forced_land_lng, forced_land_alt: self.forced_land_alt, forced_land_radius: self.forced_land_radius, checksum: self.checksum, create_time: self.create_time, update_time: self.update_time } } }

#[derive(Debug, sqlx::FromRow)] struct DetectSpectrumRow { id: Option<i64>, station_id: i32, model: Option<String>, freq: Option<i64>, rssi: Option<f64>, bandwidth: Option<i32>, data_time: Option<String>, create_time: Option<String> }
impl DetectSpectrumRow { fn into(self) -> crate::models::DetectSpectrum { crate::models::DetectSpectrum { id: self.id, station_id: self.station_id, model: self.model, freq: self.freq, rssi: self.rssi, bandwidth: self.bandwidth, data_time: self.data_time, create_time: self.create_time } } }

#[derive(Debug, sqlx::FromRow)] struct DroneFeatureLibRow { id: Option<String>, mc: Option<String>, serial_number: Option<String>, brand: Option<String>, model: Option<String>, r#type: Option<String>, dqfl: Option<String>, zlfl: Option<String>, jj: Option<String>, jc: Option<String>, xtczy: Option<String>, sx: Option<String>, sysx: Option<String>, zdsx: Option<String>, yxsx: Option<String>, jg: Option<String>, zzbj: Option<String>, kzbj: Option<String>, zcfw: Option<String>, yz: Option<String>, jz: Option<String>, rwzb: Option<String>, yxzh: Option<String>, zdsd: Option<String>, zdqfzl: Option<String>, xhsd: Option<String>, xhgd: Option<String>, xhsj: Option<String>, xhjl: Option<String>, qdbj: Option<String>, fdjsl: Option<String>, fxsd: Option<String>, zz: Option<String>, kz: Option<String>, zdhc: Option<String>, scdw: Option<String>, qymc: Option<String>, fdj: Option<String>, dlzz: Option<String>, jzcl: Option<String>, dmczry: Option<String>, dwjd: Option<String>, yxcsjl: Option<String>, cd: Option<String>, wx: Option<String>, hdfs: Option<String>, zj: Option<String>, nyzl: Option<String>, hs: Option<String>, gzqs: Option<String>, zcl: Option<String>, hc: Option<String>, dzpt: Option<String>, dy: Option<String>, dzsb: Option<String>, hldjnl: Option<String>, ldjdzsb: Option<String>, td: Option<String>, jgtd: Option<String>, zczb: Option<String>, zznl: Option<String>, zcjsnl: Option<String>, bpjbs: Option<String>, remark: Option<String>, tp: Option<String>, mxdz: Option<String>, cjr: Option<String>, cjrid: Option<String>, cjsj: Option<String>, czr: Option<String>, czrid: Option<String>, czsj: Option<String>, zdxhsd: Option<String>, wrjxhsj: Option<String>, tcnl: Option<String> }
impl DroneFeatureLibRow { fn into(self) -> crate::models::DroneFeatureLib { crate::models::DroneFeatureLib { id: self.id, mc: self.mc, serial_number: self.serial_number, brand: self.brand, model: self.model, r#type: self.r#type, dqfl: self.dqfl, zlfl: self.zlfl, jj: self.jj, jc: self.jc, xtczy: self.xtczy, sx: self.sx, sysx: self.sysx, zdsx: self.zdsx, yxsx: self.yxsx, jg: self.jg, zzbj: self.zzbj, kzbj: self.kzbj, zcfw: self.zcfw, yz: self.yz, jz: self.jz, rwzb: self.rwzb, yxzh: self.yxzh, zdsd: self.zdsd, zdqfzl: self.zdqfzl, xhsd: self.xhsd, xhgd: self.xhgd, xhsj: self.xhsj, xhjl: self.xhjl, qdbj: self.qdbj, fdjsl: self.fdjsl, fxsd: self.fxsd, zz: self.zz, kz: self.kz, zdhc: self.zdhc, scdw: self.scdw, qymc: self.qymc, fdj: self.fdj, dlzz: self.dlzz, jzcl: self.jzcl, dmczry: self.dmczry, dwjd: self.dwjd, yxcsjl: self.yxcsjl, cd: self.cd, wx: self.wx, hdfs: self.hdfs, zj: self.zj, nyzl: self.nyzl, hs: self.hs, gzqs: self.gzqs, zcl: self.zcl, hc: self.hc, dzpt: self.dzpt, dy: self.dy, dzsb: self.dzsb, hldjnl: self.hldjnl, ldjdzsb: self.ldjdzsb, td: self.td, jgtd: self.jgtd, zczb: self.zczb, zznl: self.zznl, zcjsnl: self.zcjsnl, bpjbs: self.bpjbs, remark: self.remark, tp: self.tp, mxdz: self.mxdz, cjr: self.cjr, cjrid: self.cjrid, cjsj: self.cjsj, czr: self.czr, czrid: self.czrid, czsj: self.czsj, zdxhsd: self.zdxhsd, wrjxhsj: self.wrjxhsj, tcnl: self.tcnl } } }

#[derive(Debug, sqlx::FromRow)] struct DroneBasicInfoRow { id: Option<String>, serial_number: Option<String>, brand: Option<String>, model: Option<String>, status: Option<i32>, auth_status: Option<i32>, current_longitude: Option<f64>, current_latitude: Option<f64>, current_altitude: Option<i32>, last_seen_time: Option<String>, remark: Option<String>, rwlx: Option<String>, ssdw: Option<String>, tp: Option<String>, mxdz: Option<String>, cjr: Option<String>, cjrid: Option<String>, cjsj: Option<String>, czr: Option<String>, czrid: Option<String>, czsj: Option<String> }
impl DroneBasicInfoRow { fn into(self) -> crate::models::DroneBasicInfo { crate::models::DroneBasicInfo { id: self.id, serial_number: self.serial_number, brand: self.brand, model: self.model, status: self.status, auth_status: self.auth_status, current_longitude: self.current_longitude, current_latitude: self.current_latitude, current_altitude: self.current_altitude, last_seen_time: self.last_seen_time, remark: self.remark, rwlx: self.rwlx, ssdw: self.ssdw, tp: self.tp, mxdz: self.mxdz, cjr: self.cjr, cjrid: self.cjrid, cjsj: self.cjsj, czr: self.czr, czrid: self.czrid, czsj: self.czsj } } }

#[derive(Debug, sqlx::FromRow)] struct BlackWhiteListRow { id: Option<String>, mdlx: Option<String>, wrjid: Option<String>, sqsj: Option<String>, sqgqsj: Option<String>, cjr: Option<String>, cjrid: Option<String>, cjsj: Option<String>, czr: Option<String>, czrid: Option<String>, czsj: Option<String> }
impl BlackWhiteListRow { fn into(self) -> crate::models::BlackWhiteList { crate::models::BlackWhiteList { id: self.id, mdlx: self.mdlx, wrjid: self.wrjid, sqsj: self.sqsj, sqgqsj: self.sqgqsj, cjr: self.cjr, cjrid: self.cjrid, cjsj: self.cjsj, czr: self.czr, czrid: self.czrid, czsj: self.czsj } } }

#[derive(Debug, sqlx::FromRow)] struct AlarmRecordRow { id: Option<String>, kymc: Option<String>, kyid: Option<String>, wrjid: Option<String>, wrjpp: Option<String>, wrjxh: Option<String>, wrjxlh: Option<String>, gjlx: Option<String>, gjfsjd: Option<f64>, gjfsgd: Option<f64>, gjfswd: Option<f64>, gjfssj: Option<String>, clzt: Option<String>, clsj: Option<String>, clrid: Option<String>, clr: Option<String>, clbz: Option<String>, fsjd: Option<f64>, fswd: Option<f64>, zdid: Option<String>, zdmc: Option<String>, gjys: Option<String>, diff_seconds: Option<i32>, alarm_group_id: Option<i64>, cjr: Option<String>, cjrid: Option<String>, cjsj: Option<String>, czr: Option<String>, czrid: Option<String>, czsj: Option<String> }
impl AlarmRecordRow { fn into(self) -> crate::models::AlarmRecord { crate::models::AlarmRecord { id: self.id, kymc: self.kymc, kyid: self.kyid, wrjid: self.wrjid, wrjpp: self.wrjpp, wrjxh: self.wrjxh, wrjxlh: self.wrjxlh, gjlx: self.gjlx, gjfsjd: self.gjfsjd, gjfsgd: self.gjfsgd, gjfswd: self.gjfswd, gjfssj: self.gjfssj, clzt: self.clzt, clsj: self.clsj, clrid: self.clrid, clr: self.clr, clbz: self.clbz, fsjd: self.fsjd, fswd: self.fswd, zdid: self.zdid, zdmc: self.zdmc, gjys: self.gjys, diff_seconds: self.diff_seconds, alarm_group_id: self.alarm_group_id, cjr: self.cjr, cjrid: self.cjrid, cjsj: self.cjsj, czr: self.czr, czrid: self.czrid, czsj: self.czsj } } }

#[derive(Debug, sqlx::FromRow)] struct AirspaceRow { id: Option<String>, mc: Option<String>, lx: Option<String>, xz: Option<String>, zxdjd: Option<f64>, zxdwd: Option<f64>, bj: Option<f64>, zxgd: Option<i32>, zdgd: Option<i32>, ddzbjh: Option<String>, cd: Option<f64>, kd: Option<f64>, kssj: Option<String>, jssj: Option<String>, sfqy: Option<i32>, bz: Option<String>, ys: Option<String>, jfqbj: Option<f64>, jfqys: Option<String>, yjqbj: Option<f64>, yjqys: Option<String>, cjr: Option<String>, cjrid: Option<String>, cjsj: Option<String>, czr: Option<String>, czrid: Option<String>, czsj: Option<String> }
impl AirspaceRow { fn into(self) -> crate::models::Airspace { crate::models::Airspace { id: self.id, mc: self.mc, lx: self.lx, xz: self.xz, zxdjd: self.zxdjd, zxdwd: self.zxdwd, bj: self.bj, zxgd: self.zxgd, zdgd: self.zdgd, ddzbjh: self.ddzbjh, cd: self.cd, kd: self.kd, kssj: self.kssj, jssj: self.jssj, sfqy: self.sfqy, bz: self.bz, ys: self.ys, jfqbj: self.jfqbj, jfqys: self.jfqys, yjqbj: self.yjqbj, yjqys: self.yjqys, cjr: self.cjr, cjrid: self.cjrid, cjsj: self.cjsj, czr: self.czr, czrid: self.czrid, czsj: self.czsj } } }

#[derive(Debug, sqlx::FromRow)] struct DroneDetectMsgRow { id: Option<i64>, station_id: i32, serial: Option<String>, model: Option<String>, dron_lng: Option<f64>, dron_lat: Option<f64>, home_lng: Option<f64>, home_lat: Option<f64>, pilot_lng: Option<f64>, pilot_lat: Option<f64>, altitude: Option<f64>, height: Option<f64>, east_v: Option<f64>, north_v: Option<f64>, up_v: Option<f64>, freq: Option<i64>, rssi: Option<f64>, distance: Option<f64>, uuid: Option<String>, angle: Option<f64>, data_time: Option<String>, create_time: Option<String>, sd: Option<f64>, mac: Option<String>, jmlx: Option<String>, data_type: Option<i32>, fused_flag: Option<i32> }
impl DroneDetectMsgRow { fn into(self) -> crate::models::DroneDetectMsg { crate::models::DroneDetectMsg { id: self.id, station_id: self.station_id, serial: self.serial, model: self.model, dron_lng: self.dron_lng, dron_lat: self.dron_lat, home_lng: self.home_lng, home_lat: self.home_lat, pilot_lng: self.pilot_lng, pilot_lat: self.pilot_lat, altitude: self.altitude, height: self.height, east_v: self.east_v, north_v: self.north_v, up_v: self.up_v, freq: self.freq, rssi: self.rssi, distance: self.distance, uuid: self.uuid, angle: self.angle, data_time: self.data_time, create_time: self.create_time, sd: self.sd, mac: self.mac, jmlx: self.jmlx, data_type: self.data_type, fused_flag: self.fused_flag, wxdj: None } } }

#[derive(Debug, sqlx::FromRow)] struct DroneDfDataRow { id: Option<i64>, station_id: Option<i32>, data_time: Option<String>, target_type: Option<i32>, detect_type: Option<i32>, freq: Option<i64>, dk: Option<i32>, longitude: Option<f64>, latitude: Option<f64>, angle: Option<f64>, signal_level: Option<f64>, compass: Option<f64>, distance: Option<f64>, uav_model: Option<String>, uav_id: Option<String>, device_id: Option<String>, create_time: Option<String>, speed: Option<f64>, height: Option<f64> }
impl DroneDfDataRow { fn into(self) -> crate::models::DroneDfData { crate::models::DroneDfData { id: self.id, station_id: self.station_id, data_time: self.data_time, target_type: self.target_type, detect_type: self.detect_type, freq: self.freq, dk: self.dk, longitude: self.longitude, latitude: self.latitude, angle: self.angle, signal_level: self.signal_level, compass: self.compass, distance: self.distance, uav_model: self.uav_model, uav_id: self.uav_id, device_id: self.device_id, create_time: self.create_time, speed: self.speed, height: self.height } } }

#[derive(Debug, sqlx::FromRow)] struct DroneRemoteDataRow { id: Option<String>, ris_ssid: Option<String>, serial: Option<String>, model: Option<String>, ua_type: Option<String>, dron_lng: Option<f64>, dron_lat: Option<f64>, pilot_lng: Option<f64>, pilot_lat: Option<f64>, speed: Option<f64>, vspeed: Option<f64>, direc: Option<f64>, altitudep: Option<f64>, altitudeg: Option<f64>, height_agl: Option<f64>, mac: Option<String>, rssi: Option<f64>, freq: Option<String>, angle: Option<f64>, distance: Option<i32>, date: Option<String>, station_id: Option<i32> }
impl DroneRemoteDataRow { fn into(self) -> crate::models::DroneRemoteData { crate::models::DroneRemoteData { id: self.id, ris_ssid: self.ris_ssid, serial: self.serial, model: self.model, ua_type: self.ua_type, dron_lng: self.dron_lng, dron_lat: self.dron_lat, pilot_lng: self.pilot_lng, pilot_lat: self.pilot_lat, speed: self.speed, vspeed: self.vspeed, direc: self.direc, altitudep: self.altitudep, altitudeg: self.altitudeg, height_agl: self.height_agl, mac: self.mac, rssi: self.rssi, freq: self.freq, angle: self.angle, distance: self.distance, date: self.date, station_id: self.station_id } } }

// ==================== wxdzc Row 类型 ====================

/// wxdzc 站点分组侦测消息行（含 JOIN 字段）
#[derive(Debug, sqlx::FromRow)]
pub struct WxdzcDetectMsgRow {
    // From hsim_wrj_fxsj_zcbw
    pub id: Option<i64>,
    pub station_id: i32,
    pub serial: Option<String>,
    pub model: Option<String>,
    pub dron_lng: Option<f64>,
    pub dron_lat: Option<f64>,
    pub home_lng: Option<f64>,
    pub home_lat: Option<f64>,
    pub pilot_lng: Option<f64>,
    pub pilot_lat: Option<f64>,
    pub altitude: Option<f64>,
    pub height: Option<f64>,
    pub east_v: Option<f64>,
    pub north_v: Option<f64>,
    pub up_v: Option<f64>,
    pub freq: Option<i64>,
    pub rssi: Option<f64>,
    pub distance: Option<f64>,
    pub uuid: Option<String>,
    pub angle: Option<f64>,
    pub data_time: Option<String>,
    pub create_time: Option<String>,
    pub sd: Option<f64>,
    pub mac: Option<String>,
    pub jmlx: Option<String>,
    pub data_type: Option<i32>,
    pub fused_flag: Option<i32>,
    pub model_clean: Option<String>,
    pub serial_clean: Option<String>,
    pub rn: Option<i64>,
    // From LEFT JOIN hsim_wrj_jbxx
    pub brand: Option<String>,
    pub jbxx_status: Option<i32>,
    pub auth_status: Option<i32>,
    pub rwlx: Option<String>,
    pub ssdw: Option<String>,
    // From LEFT JOIN hsim_sb_pz
    pub station_name: Option<String>,
}

impl WxdzcDetectMsgRow {
    /// 转换为 UavDetectMsgVo（含 threat 计算）
    pub fn into_vo(self) -> crate::models::UavDetectMsgVo {
        let wxdj = compute_threat_level(self.sd, self.distance, self.height);
        crate::models::UavDetectMsgVo {
            model: self.model.clone(),
            serial: self.serial.clone(),
            brand: self.brand,
            status: self.jbxx_status,
            auth_status: self.auth_status,
            station_id: Some(self.station_id),
            station_name: self.station_name,
            wxdj: Some(wxdj.to_string()),
            jmlx: self.jmlx,
            uav_detect_msg: crate::models::DroneDetectMsg {
                id: self.id,
                station_id: self.station_id,
                serial: self.serial,
                model: self.model,
                dron_lng: self.dron_lng,
                dron_lat: self.dron_lat,
                home_lng: self.home_lng,
                home_lat: self.home_lat,
                pilot_lng: self.pilot_lng,
                pilot_lat: self.pilot_lat,
                altitude: self.altitude,
                height: self.height,
                east_v: self.east_v,
                north_v: self.north_v,
                up_v: self.up_v,
                freq: self.freq,
                rssi: self.rssi,
                distance: self.distance,
                uuid: self.uuid,
                angle: self.angle,
                data_time: self.data_time,
                create_time: self.create_time,
                sd: self.sd,
                mac: self.mac,
                jmlx: None,
                data_type: self.data_type,
                fused_flag: self.fused_flag,
                wxdj: Some(wxdj.to_string()),
            },
        }
    }
}

/// 威胁等级计算（与 Java 版本 sigmoid 公式一致）
fn compute_threat_level(sd: Option<f64>, distance: Option<f64>, _height: Option<f64>) -> &'static str {
    let sd_val = sd.unwrap_or(0.0).max(0.0).min(150.0);
    let dist_val = distance.unwrap_or(0.0).max(-100.0).min(1000.0);

    // Speed score (weight 0.4): 1 - exp(-0.005 * sd)
    let speed_score = 1.0 - (-0.005 * sd_val).exp();

    // Distance score (weight 0.3): 1 / (1 + exp(0.02 * (dist - 30)))
    let dist_score = 1.0 / (1.0 + (0.02 * (dist_val - 30.0)).exp());

    // Height score (weight 0.3): simplified — use a sigmoid around 120m
    let height_val = _height.unwrap_or(0.0).max(0.0).min(10000.0);
    let height_score = 1.0 / (1.0 + (0.005 * (height_val - 120.0)).exp());

    let total = 0.4 * speed_score + 0.3 * dist_score + 0.3 * height_score;

    if total >= 0.7 { "red" } else if total >= 0.4 { "yellow" } else { "green" }
}

#[derive(Debug, sqlx::FromRow)]
struct UavDetectMsgDateVoRow {
    rq: String,
    r_type: String,
}

impl UavDetectMsgDateVoRow {
    fn into(self) -> crate::models::UavDetectMsgDateVo {
        crate::models::UavDetectMsgDateVo { rq: self.rq, r_type: self.r_type }
    }
}
