use sqlx::sqlite::{SqliteConnectOptions, SqlitePoolOptions};
use sqlx::SqlitePool;
use std::str::FromStr;
use tracing;

use crate::config::DatabaseConfig;

/// 初始化 SQLite 数据库连接池
pub async fn init_database(config: &DatabaseConfig) -> anyhow::Result<SqlitePool> {
    tracing::info!("Initializing SQLite database at: {}", config.path);
    if let Some(parent) = std::path::Path::new(&config.path).parent() {
        std::fs::create_dir_all(parent)?;
    }
    let connect_opts = SqliteConnectOptions::from_str(&format!("sqlite:{}", config.path))?
        .create_if_missing(true)
        .journal_mode(sqlx::sqlite::SqliteJournalMode::Wal)
        .foreign_keys(true)
        .busy_timeout(std::time::Duration::from_secs(5));
    let pool = SqlitePoolOptions::new().max_connections(config.max_connections).connect_with(connect_opts).await?;
    sqlx::query("PRAGMA journal_mode=WAL;").execute(&pool).await?;
    sqlx::query("PRAGMA synchronous=NORMAL;").execute(&pool).await?;
    if config.auto_migrate { run_migrations(&pool).await?; }
    tracing::info!("SQLite database initialized successfully");
    Ok(pool)
}

/// 执行数据库 Schema 迁移（列顺序与 MySQL 原始表完全一致）
async fn run_migrations(pool: &SqlitePool) -> anyhow::Result<()> {
    tracing::info!("Running database schema migrations...");

    // === 原有系统表 ===
    sqlx::query("CREATE TABLE IF NOT EXISTS sensors (sensor_id TEXT PRIMARY KEY, sensor_type TEXT NOT NULL, name TEXT NOT NULL, model TEXT NOT NULL, latitude REAL NOT NULL, longitude REAL NOT NULL, altitude REAL NOT NULL, status TEXT NOT NULL DEFAULT 'ONLINE', capabilities TEXT, config TEXT, last_heartbeat TEXT NOT NULL, created_at TEXT NOT NULL DEFAULT (datetime('now')), updated_at TEXT NOT NULL DEFAULT (datetime('now')));").execute(pool).await?;
    sqlx::query("CREATE TABLE IF NOT EXISTS targets (target_id TEXT PRIMARY KEY, track_id TEXT NOT NULL, classification TEXT NOT NULL, threat_level TEXT NOT NULL, confidence REAL NOT NULL, latitude REAL NOT NULL, longitude REAL NOT NULL, altitude REAL NOT NULL, vn REAL NOT NULL DEFAULT 0, ve REAL NOT NULL DEFAULT 0, vd REAL NOT NULL DEFAULT 0, first_seen TEXT NOT NULL, last_update TEXT NOT NULL, is_active INTEGER NOT NULL DEFAULT 1, created_at TEXT NOT NULL DEFAULT (datetime('now')));").execute(pool).await?;
    sqlx::query("CREATE TABLE IF NOT EXISTS track_points (id INTEGER PRIMARY KEY AUTOINCREMENT, target_id TEXT NOT NULL, timestamp TEXT NOT NULL, latitude REAL NOT NULL, longitude REAL NOT NULL, altitude REAL NOT NULL, vn REAL NOT NULL DEFAULT 0, ve REAL NOT NULL DEFAULT 0, vd REAL NOT NULL DEFAULT 0, FOREIGN KEY (target_id) REFERENCES targets(target_id));").execute(pool).await?;
    sqlx::query("CREATE INDEX IF NOT EXISTS idx_track_points_target ON track_points(target_id, timestamp);").execute(pool).await?;
    sqlx::query("CREATE TABLE IF NOT EXISTS missions (mission_id TEXT PRIMARY KEY, target_id TEXT NOT NULL, interceptor_ids TEXT NOT NULL, trajectory TEXT NOT NULL DEFAULT '[]', priority INTEGER NOT NULL DEFAULT 3, status TEXT NOT NULL DEFAULT 'PLANNING', created_at TEXT NOT NULL DEFAULT (datetime('now')), updated_at TEXT NOT NULL DEFAULT (datetime('now')));").execute(pool).await?;
    sqlx::query("CREATE TABLE IF NOT EXISTS alerts (alert_id TEXT PRIMARY KEY, alert_type TEXT NOT NULL, severity TEXT NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, source TEXT NOT NULL, timestamp TEXT NOT NULL);").execute(pool).await?;
    sqlx::query("CREATE TABLE IF NOT EXISTS system_events (event_id INTEGER PRIMARY KEY AUTOINCREMENT, event_type TEXT NOT NULL, payload TEXT NOT NULL, timestamp TEXT NOT NULL DEFAULT (datetime('now')));").execute(pool).await?;

    // ===== 设备管理表 (6) — 列顺序与MySQL完全一致 =====

    // 1. hsim_sb_pz (MySQL: uav_device_config, 22列, id在列11)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_sb_pz (name TEXT, device_id TEXT NOT NULL, station_id INTEGER NOT NULL, device_type TEXT NOT NULL, device_ip TEXT NOT NULL, device_port INTEGER NOT NULL DEFAULT 8888, protocol_version TEXT DEFAULT '1.0', is_valid INTEGER NOT NULL DEFAULT 1, status TEXT DEFAULT 'DISCONNECTED', update_time TEXT, id INTEGER PRIMARY KEY AUTOINCREMENT, jd REAL, wd REAL, gd REAL, zcbj TEXT, dyqk TEXT, mac TEXT, xh TEXT, sccj TEXT, ccrq TEXT, type TEXT, udp_port INTEGER);").execute(pool).await?;
    sqlx::query("CREATE INDEX IF NOT EXISTS idx_hsim_sb_pz_station ON hsim_sb_pz(station_id);").execute(pool).await?;

    // 2. hsim_sb_xt (MySQL: uav_device_heartbeat, 17列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_sb_xt (id INTEGER PRIMARY KEY AUTOINCREMENT, station_id INTEGER, data_time TEXT, main_card INTEGER, trap_card INTEGER, compass INTEGER, disturb_card INTEGER, longitude REAL, latitude REAL, altitude INTEGER, angle REAL, cpu_rate REAL, disk_usage REAL, card_temp REAL, amp_temp REAL, create_time TEXT, work_state INTEGER);").execute(pool).await?;
    sqlx::query("CREATE INDEX IF NOT EXISTS idx_hsim_sb_xt_station ON hsim_sb_xt(station_id);").execute(pool).await?;

    // 3. hsim_sb_lj (MySQL: uav_connect_log, 7列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_sb_lj (id INTEGER PRIMARY KEY AUTOINCREMENT, station_id INTEGER NOT NULL, device_ip TEXT NOT NULL, device_port INTEGER NOT NULL, event_type TEXT NOT NULL, event_time TEXT, reason TEXT);").execute(pool).await?;
    sqlx::query("CREATE INDEX IF NOT EXISTS idx_hsim_sb_lj_station ON hsim_sb_lj(station_id);").execute(pool).await?;

    // 4. hsim_sb_zcpp (MySQL: uav_detect_spectrum, 8列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_sb_zcpp (id INTEGER PRIMARY KEY AUTOINCREMENT, station_id INTEGER NOT NULL, model TEXT, freq INTEGER, rssi REAL, bandwidth INTEGER, data_time TEXT, create_time TEXT);").execute(pool).await?;
    sqlx::query("CREATE INDEX IF NOT EXISTS idx_hsim_sb_zcpp_freq ON hsim_sb_zcpp(freq);").execute(pool).await?;

    // 5. hsim_sb_czrz (MySQL: uav_operate_log, 8列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_sb_czrz (id INTEGER PRIMARY KEY AUTOINCREMENT, station_id INTEGER, device_type TEXT, cmd_type TEXT, cmd_name TEXT, cmd_param TEXT, result TEXT, operate_time TEXT);").execute(pool).await?;
    sqlx::query("CREATE INDEX IF NOT EXISTS idx_hsim_sb_czrz_station ON hsim_sb_czrz(station_id);").execute(pool).await?;

    // 6. hsim_sb_xt_zd (MySQL: uav_active_heartbeat, 37列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_sb_xt_zd (id INTEGER PRIMARY KEY AUTOINCREMENT, start_code TEXT, source_addr INTEGER, dest_addr INTEGER, command INTEGER, param_length INTEGER, terminal_code TEXT, auth_normal INTEGER, detect_enabled INTEGER, counter_enabled INTEGER, detector_online INTEGER, counter_online INTEGER, unattended_mode INTEGER, deceiver_online INTEGER, jamming_mode INTEGER, band58g INTEGER, band24g INTEGER, band900m INTEGER, band14g INTEGER, band52g INTEGER, ptz_control_mode INTEGER, attack_countdown INTEGER, deception_status INTEGER, gnss_link_status INTEGER, gnss_deception_mode INTEGER, gnss_induce_mode INTEGER, no_fly_lat REAL, no_fly_lng REAL, no_fly_alt REAL, forced_land_lat REAL, forced_land_lng REAL, forced_land_alt REAL, forced_land_radius INTEGER, checksum INTEGER, create_time TEXT, update_time TEXT, station_id INTEGER);").execute(pool).await?;

    // ===== 无人机管理表 (5) — 列顺序与MySQL完全一致 =====

    // 7. hsim_wrj_tzk (MySQL: wjbd_wrj_jbxxgl, 78列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_tzk (id TEXT PRIMARY KEY, mc TEXT, serial_number TEXT UNIQUE, brand TEXT, model TEXT, type TEXT, dqfl TEXT, zlfl TEXT, jj TEXT, jc TEXT, xtczy TEXT, sx TEXT, sysx TEXT, zdsx TEXT, yxsx TEXT, jg TEXT, zzbj TEXT, kzbj TEXT, zcfw TEXT, yz TEXT, jz TEXT, rwzb TEXT, yxzh TEXT, zdsd TEXT, zdqfzl TEXT, xhsd TEXT, xhgd TEXT, xhsj TEXT, xhjl TEXT, qdbj TEXT, fdjsl TEXT, fxsd TEXT, zz TEXT, kz TEXT, zdhc TEXT, scdw TEXT, qymc TEXT, fdj TEXT, dlzz TEXT, jzcl TEXT, dmczry TEXT, dwjd TEXT, yxcsjl TEXT, cd TEXT, wx TEXT, hdfs TEXT, zj TEXT, nyzl TEXT, hs TEXT, gzqs TEXT, zcl TEXT, hc TEXT, dzpt TEXT, dy TEXT, dzsb TEXT, hldjnl TEXT, ldjdzsb TEXT, td TEXT, jgtd TEXT, zczb TEXT, zznl TEXT, zcjsnl TEXT, bpjbs TEXT, remark TEXT, CJR TEXT, CJRID TEXT, CJSJ TEXT, CZR TEXT, CZRID TEXT, CZSJ TEXT, tp TEXT, mxdz TEXT, zdxhsd TEXT, wrjxhsj TEXT, tcnl TEXT);").execute(pool).await?;

    // 8. hsim_wrj_jbxx (MySQL: wjbd_wrj_jbxx, 20列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_jbxx (id TEXT PRIMARY KEY, serial_number TEXT UNIQUE, brand TEXT, model TEXT, status INTEGER DEFAULT 1, auth_status INTEGER DEFAULT 3, current_longitude REAL, current_latitude REAL, current_altitude INTEGER, last_seen_time TEXT, remark TEXT, CJR TEXT, CJRID TEXT, CJSJ TEXT, CZR TEXT, CZRID TEXT, CZSJ TEXT, tp TEXT, mxdz TEXT, rwlx TEXT, ssdw TEXT);").execute(pool).await?;

    // 9. hsim_wrj_hbmdsq (MySQL: wjbd_wrj_hbmdsq, 11列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_hbmdsq (id TEXT PRIMARY KEY, mdlx TEXT, wrjid TEXT, sqsj TEXT, sqgqsj TEXT, CJR TEXT, CJRID TEXT, CJSJ TEXT, CZR TEXT, CZRID TEXT, CZSJ TEXT);").execute(pool).await?;

    // 10. hsim_wrj_gjjl (MySQL: wjbd_wrj_gjjl, 30列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_gjjl (id TEXT PRIMARY KEY, kymc TEXT, kyid TEXT, wrjid TEXT, wrjpp TEXT, wrjxh TEXT, wrjxlh TEXT, gjlx TEXT, gjfsjd REAL, gjfsgd REAL, gjfswd REAL, gjfssj TEXT, clzt TEXT DEFAULT '0', clsj TEXT, clrid TEXT, clr TEXT, clbz TEXT, CJR TEXT, CJRID TEXT, CJSJ TEXT, CZR TEXT, CZRID TEXT, CZSJ TEXT, fsjd REAL, fswd REAL, zdid TEXT, zdmc TEXT, gjys TEXT, diff_seconds INTEGER DEFAULT 0, alarm_group_id INTEGER DEFAULT 0);").execute(pool).await?;

    // 11. hsim_wrj_ky (MySQL: wjbd_wrj_ky, 27列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_ky (id TEXT PRIMARY KEY, mc TEXT, lx TEXT, xz TEXT, zxdjd REAL, zxdwd REAL, bj REAL, zxgd INTEGER, zdgd INTEGER, ddzbjh TEXT, cd REAL, kd REAL, kssj TEXT, jssj TEXT, sfqy INTEGER DEFAULT 1, bz TEXT, ys TEXT, CJR TEXT, CJRID TEXT, CJSJ TEXT, CZR TEXT, CZRID TEXT, CZSJ TEXT, jfqbj REAL, jfqys TEXT, yjqbj REAL, yjqys TEXT);").execute(pool).await?;

    // ===== 飞行数据表 (3) — 列顺序与MySQL完全一致 =====

    // 12. hsim_wrj_fxsj_zcbw (MySQL: uav_detect_msg, 28列完整匹配含2个生成列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_fxsj_zcbw (id INTEGER PRIMARY KEY AUTOINCREMENT, station_id INTEGER NOT NULL, serial TEXT, model TEXT, dron_lng REAL, dron_lat REAL, home_lng REAL, home_lat REAL, pilot_lng REAL, pilot_lat REAL, altitude REAL, height REAL, east_v REAL, north_v REAL, up_v REAL, freq INTEGER, rssi REAL, distance REAL, uuid TEXT, angle REAL, data_time TEXT, create_time TEXT, sd REAL, mac TEXT, jmlx TEXT, data_type INTEGER, fused_flag INTEGER, model_clean TEXT, serial_clean TEXT);").execute(pool).await?;
    sqlx::query("CREATE INDEX IF NOT EXISTS idx_hsim_fxsj_zcbw_serial ON hsim_wrj_fxsj_zcbw(serial, data_time);").execute(pool).await?;

    // 13. hsim_wrj_fxsj_jx (MySQL: uav_df_data, 19列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_fxsj_jx (id INTEGER PRIMARY KEY AUTOINCREMENT, station_id INTEGER, data_time TEXT, target_type INTEGER, detect_type INTEGER, freq INTEGER, dk INTEGER, longitude REAL, latitude REAL, angle REAL, signal_level REAL, compass REAL, distance REAL, uav_model TEXT, uav_id TEXT, device_id TEXT, create_time TEXT, speed REAL, height REAL);").execute(pool).await?;

    // 14. hsim_wrj_fxsj_remote (MySQL: uav_remote_data, 22列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_fxsj_remote (id TEXT PRIMARY KEY, ris_ssid TEXT, serial TEXT, model TEXT, ua_type TEXT, dron_lng REAL, dron_lat REAL, pilot_lng REAL, pilot_lat REAL, speed REAL, vspeed REAL, direc REAL, altitudep REAL, altitudeg REAL, height_agl REAL, mac TEXT, rssi REAL, freq TEXT, angle REAL, distance INTEGER, date TEXT, station_id INTEGER);").execute(pool).await?;

    // ===== wrj 武警业务新增表 (22) =====

    // 15. hsim_wrj_jbxx_new (MySQL: wjbd_wrj_jbxx_new, 39列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_jbxx_new (id TEXT PRIMARY KEY, serial_number TEXT, brand TEXT, model TEXT, type TEXT, jj TEXT, dy TEXT, tcnl TEXT, zznl TEXT, bpjbs TEXT, zdxhsj TEXT, zdfxsd TEXT, zdkzjl TEXT, zdfxgd TEXT, kfdj TEXT, zdhzzl TEXT, jscc TEXT, jszl TEXT, dlxt TEXT, dwxt TEXT, xjcs TEXT, tcxt TEXT, dcgg TEXT, status INTEGER, auth_status INTEGER, current_longitude REAL, current_latitude REAL, current_altitude INTEGER, last_seen_time TEXT, remark TEXT, cjr TEXT, cjrid TEXT, cjsj TEXT, czr TEXT, czrid TEXT, czsj TEXT, tp TEXT, mxdz TEXT, dqfl TEXT, zlfl TEXT);").execute(pool).await?;

    // 16. hsim_wrj_kysq (MySQL: wjbd_wrj_kysq, 11列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_kysq (id TEXT PRIMARY KEY, kyid TEXT, wrjid TEXT, sqsj TEXT, sqgqsj TEXT, cjr TEXT, cjrid TEXT, cjsj TEXT, czr TEXT, czrid TEXT, czsj TEXT);").execute(pool).await?;

    // 17. hsim_wrj_fwzf (MySQL: wjbd_wrj_fwzf, 18列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_fwzf (id TEXT PRIMARY KEY, fl TEXT, mc TEXT, zfgs TEXT, zzcj TEXT, jbzf TEXT, llbshtxgj TEXT, jtxdff TEXT, zfyzqk TEXT, zfyyxybwwt TEXT, wj TEXT, remark TEXT, cjr TEXT, cjrid TEXT, cjsj TEXT, czr TEXT, czrid TEXT, czsj TEXT);").execute(pool).await?;

    // 18. hsim_wrj_fzzb (MySQL: wjbd_wrj_fzzb, 16列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_fzzb (id TEXT PRIMARY KEY, fl TEXT, mc TEXT, gntd TEXT, jszb TEXT, tp TEXT, sl TEXT, zbly TEXT, remark TEXT, cjr TEXT, cjrid TEXT, cjsj TEXT, czr TEXT, czrid TEXT, czsj TEXT, mxdz TEXT);").execute(pool).await?;

    // 19. hsim_wrj_bxpz (MySQL: wjbd_wrj_bxpz, 11列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_bxpz (id TEXT PRIMARY KEY, mc TEXT, pid TEXT, nr TEXT, remark TEXT, cjr TEXT, cjrid TEXT, cjsj TEXT, czr TEXT, czrid TEXT, czsj TEXT);").execute(pool).await?;

    // 20. hsim_wrj_zymb (MySQL: wjbd_wrj_zymb, 10列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_zymb (id TEXT PRIMARY KEY, mc TEXT, jd TEXT, wd TEXT, rylx TEXT, sl TEXT, zb TEXT, ssgk TEXT, type TEXT, subtype TEXT);").execute(pool).await?;

    // 21. hsim_wrj_czsp (MySQL: wjbd_wrj_czsp, 10列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_czsp (id TEXT PRIMARY KEY, spfl TEXT, mc TEXT, splj TEXT, cjr TEXT, cjrid TEXT, cjsj TEXT, czr TEXT, czrid TEXT, czsj TEXT);").execute(pool).await?;

    // 22. hsim_wrj_scj (MySQL: wjbd_wrj_scj, 9列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_scj (id INTEGER PRIMARY KEY AUTOINCREMENT, pid INTEGER, mc TEXT, type TEXT, yh_id TEXT, csz TEXT, chjr_mc TEXT, chjr TEXT, rksj TEXT);").execute(pool).await?;

    // 23. hsim_wrj_jkgl (MySQL: wjbd_wrj_jkgl, 7列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_jkgl (id INTEGER PRIMARY KEY AUTOINCREMENT, url TEXT, client TEXT, secret TEXT, bm TEXT, bz TEXT, lx INTEGER);").execute(pool).await?;

    // 24. hsim_wrj_lpwj (MySQL: wjbd_wrj_lpwj, 4列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_lpwj (id TEXT PRIMARY KEY, mc TEXT, qdwjml TEXT, yhid TEXT);").execute(pool).await?;

    // 25. hsim_wrj_tyjh (MySQL: wjbd_wrj_tyjh, 18列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_tyjh (id TEXT PRIMARY KEY, mc TEXT, rq TEXT, jhks TEXT, jhjs TEXT, serial TEXT, model TEXT, brand TEXT, tyzt TEXT, cjr TEXT, cjrid TEXT, cjsj TEXT, czr TEXT, czrid TEXT, czsj TEXT, start_jd REAL, start_wd REAL, start_gd REAL, tjdjwd TEXT, end_jd REAL, end_wd REAL, end_gd REAL, jhcs TEXT, mbfxfw TEXT);").execute(pool).await?;

    // 26. hsim_wrj_tyjh_data (MySQL: wjbd_wrj_tyjh_data, 21列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_tyjh_data (id INTEGER PRIMARY KEY AUTOINCREMENT, tyjh_id TEXT, serial TEXT, model TEXT, dron_lng REAL, dron_lat REAL, home_lng REAL, home_lat REAL, pilot_lng REAL, pilot_lat REAL, altitude REAL, height REAL, east_v REAL, north_v REAL, up_v REAL, freq INTEGER, rssi REAL, distance REAL, rid_ssid TEXT, sd REAL, data_time TEXT, create_time TEXT);").execute(pool).await?;

    // 27. hsim_dxyy_bh (MySQL: wjbd_dxyy_bh, 12列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_dxyy_bh (id INTEGER PRIMARY KEY AUTOINCREMENT, bhmc TEXT, tsmc TEXT, bhsj TEXT, rwsj TEXT, ztm INTEGER, dwid TEXT, dwmc TEXT, jsdwid TEXT, gisjson TEXT, cjrid TEXT, cjsj TEXT, yw_id INTEGER);").execute(pool).await?;

    // 28. hsim_spectrum (MySQL: spectrum_info, 9列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_spectrum (id INTEGER PRIMARY KEY AUTOINCREMENT, station_id INTEGER, channel TEXT, data_type TEXT, start_freq REAL, stop_freq REAL, step_freq REAL, data_len TEXT, p_data TEXT, create_time TEXT);").execute(pool).await?;

    // 29. hsim_hs_zsk (MySQL: wjbd_wrj_zsk, 8列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_hs_zsk (id INTEGER PRIMARY KEY AUTOINCREMENT, zsk_mc TEXT, zsk_nm TEXT, zsk_ms TEXT, chjr_mc TEXT, chjr TEXT, rksj TEXT, zsk_lx INTEGER);").execute(pool).await?;

    // 30. hsim_hs_zsk_wj (MySQL: wjbd_wrj_zsk_wj, 8列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_hs_zsk_wj (id INTEGER PRIMARY KEY AUTOINCREMENT, zsk_id INTEGER, wj_mc TEXT, wj_nm TEXT, wjdx INTEGER, fwq_wjlj TEXT, chjr_mc TEXT, chjr TEXT, rksj TEXT);").execute(pool).await?;

    // 31. hsim_hs_zsk_wj_nr (MySQL: wjbd_wrj_zsk_wj_nr, 4列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_hs_zsk_wj_nr (id INTEGER PRIMARY KEY AUTOINCREMENT, zsk_id INTEGER, zsk_wj_id INTEGER, nr TEXT);").execute(pool).await?;

    // ===== dxyy 地形影响表 (4) =====

    // 32. hsim_dxyy_dxdm (MySQL: wjbd_dxyy_dxdm, 7列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_dxyy_dxdm (id TEXT PRIMARY KEY, dmmc TEXT, jd TEXT, wd TEXT, dxdmlx TEXT, mj TEXT, dlwz TEXT);").execute(pool).await?;

    // 33. hsim_wrj_yhgl_cs (MySQL: wjbd_wrj_yhgl_cs, 10列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_wrj_yhgl_cs (id INTEGER PRIMARY KEY AUTOINCREMENT, yh_id TEXT, cs_mc TEXT, cs_bm TEXT, csz TEXT, bz TEXT, chjr_mc TEXT, chjr TEXT, rksj TEXT, bdnm TEXT);").execute(pool).await?;

    // 34. hsim_zzll_bd (MySQL: dbsjgx_zzll_bd, 8列, natural key bdnm)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_zzll_bd (bdnm TEXT, bdhfnm TEXT, bdxh TEXT, bdfh TEXT, bdjc TEXT, bzxh TEXT, bzfh TEXT, bzjc TEXT);").execute(pool).await?;

    // 35. hsim_zzll_bd_bs (MySQL: dbsjgx_zzll_bd_bs, 10列, composite key)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_zzll_bd_bs (bslbnm TEXT, bdnm TEXT, twsj TEXT, bsxsnm TEXT, dmnm TEXT, kzdm TEXT, jd TEXT, wd TEXT, gc REAL, zzsj TEXT);").execute(pool).await?;

    // ===== system 系统表 =====

    // 36. hsim_mh_yycz_log (MySQL: wjbd_mh_yycz_log, 13列)
    sqlx::query("CREATE TABLE IF NOT EXISTS hsim_mh_yycz_log (id TEXT PRIMARY KEY, yymc TEXT, yymk TEXT, rzlx INTEGER, rznr TEXT, czyhm TEXT, czyhxm TEXT, czip TEXT, cjrid TEXT, cjsj TEXT, czrid TEXT, czsj TEXT, ljsc INTEGER);").execute(pool).await?;

    tracing::info!("Database schema migrations completed (36 hsim_ tables)");
    Ok(())
}
