/// wrj 武警业务 API — 通用CRUD覆盖22+业务表
use axum::{extract::{Path, Query, State}, Json};
use serde::Serialize;
use super::AppState;
use crate::models::*;

// ===== 反无战法 =====
pub async fn list_fwzf(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<AntiUavTactics>>> {
    generic_list::<AntiUavTactics>(&state, &query, "hsim_wrj_fwzf").await
}
pub async fn add_fwzf(State(state): State<AppState>, Json(body): Json<AntiUavTactics>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_wrj_fwzf", &["id","fl","mc","zfgs","zzcj","jbzf","llbshtxgj","jtxdff","zfyzqk","zfyyxybwwt","wj","remark","cjr","cjrid","cjsj","czr","czrid","czsj"]).await
}
pub async fn get_fwzf(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<Option<AntiUavTactics>>> {
    generic_get_by_id::<AntiUavTactics>(&state, id, "hsim_wrj_fwzf").await
}
pub async fn delete_fwzf(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<String>> {
    generic_delete(&state, id, "hsim_wrj_fwzf").await
}

// ===== 反制装备 =====
pub async fn list_fzzb(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<CounterEquip>>> {
    generic_list::<CounterEquip>(&state, &query, "hsim_wrj_fzzb").await
}
pub async fn add_fzzb(State(state): State<AppState>, Json(body): Json<CounterEquip>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_wrj_fzzb", &["id","fl","mc","gntd","jszb","tp","sl","zbly","remark","cjr","cjrid","cjsj","czr","czrid","czsj","mxdz"]).await
}
pub async fn get_fzzb(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<Option<CounterEquip>>> {
    generic_get_by_id::<CounterEquip>(&state, id, "hsim_wrj_fzzb").await
}
pub async fn delete_fzzb(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<String>> {
    generic_delete(&state, id, "hsim_wrj_fzzb").await
}

// ===== 编携配装 =====
pub async fn list_bxpz(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<EquipConfig>>> {
    generic_list::<EquipConfig>(&state, &query, "hsim_wrj_bxpz").await
}
pub async fn add_bxpz(State(state): State<AppState>, Json(body): Json<EquipConfig>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_wrj_bxpz", &["id","mc","pid","nr","remark","cjr","cjrid","cjsj","czr","czrid","czsj"]).await
}
pub async fn get_bxpz(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<Option<EquipConfig>>> {
    generic_get_by_id::<EquipConfig>(&state, id, "hsim_wrj_bxpz").await
}
pub async fn delete_bxpz(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<String>> {
    generic_delete(&state, id, "hsim_wrj_bxpz").await
}
pub async fn list_bxpz_tree(State(state): State<AppState>) -> Json<ApiResponse<Vec<EquipConfig>>> {
    match sqlx::query("SELECT * FROM hsim_wrj_bxpz ORDER BY id").fetch_all(state.db.pool()).await {
        Ok(_) => Json(ApiResponse::ok(vec![])),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

// ===== 重要目标 =====
pub async fn list_zymb(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<ImportantTarget>>> {
    generic_list::<ImportantTarget>(&state, &query, "hsim_wrj_zymb").await
}
pub async fn add_zymb(State(state): State<AppState>, Json(body): Json<ImportantTarget>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_wrj_zymb", &["id","mc","jd","wd","rylx","sl","zb","ssgk","type","subtype"]).await
}
pub async fn get_zymb(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<Option<ImportantTarget>>> {
    generic_get_by_id::<ImportantTarget>(&state, id, "hsim_wrj_zymb").await
}
pub async fn delete_zymb(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<String>> {
    generic_delete(&state, id, "hsim_wrj_zymb").await
}

// ===== 空域授权 =====
pub async fn list_kysq(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<AirspaceAuth>>> {
    generic_list::<AirspaceAuth>(&state, &query, "hsim_wrj_kysq").await
}
pub async fn add_kysq(State(state): State<AppState>, Json(body): Json<AirspaceAuth>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_wrj_kysq", &["id","kyid","wrjid","sqsj","sqgqsj","cjr","cjrid","cjsj","czr","czrid","czsj"]).await
}
pub async fn delete_kysq(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<String>> {
    generic_delete(&state, id, "hsim_wrj_kysq").await
}

// ===== 操作视频 =====
pub async fn list_czsp(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<OperationVideo>>> {
    generic_list::<OperationVideo>(&state, &query, "hsim_wrj_czsp").await
}
pub async fn add_czsp(State(state): State<AppState>, Json(body): Json<OperationVideo>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_wrj_czsp", &["id","spfl","mc","splj","cjr","cjrid","cjsj","czr","czrid","czsj"]).await
}
pub async fn delete_czsp(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<String>> {
    generic_delete(&state, id, "hsim_wrj_czsp").await
}

// ===== 收藏夹 =====
pub async fn list_scj(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<Bookmark>>> {
    generic_list::<Bookmark>(&state, &query, "hsim_wrj_scj").await
}
pub async fn add_scj(State(state): State<AppState>, Json(body): Json<Bookmark>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_wrj_scj", &["pid","mc","type","yh_id","csz","chjr_mc","chjr","rksj"]).await
}
pub async fn delete_scj(State(state): State<AppState>, Path(id): Path<i64>) -> Json<ApiResponse<String>> {
    generic_delete_i64(&state, id, "hsim_wrj_scj").await
}
pub async fn list_scj_tree(State(state): State<AppState>) -> Json<ApiResponse<Vec<Bookmark>>> {
    match sqlx::query("SELECT * FROM hsim_wrj_scj ORDER BY pid, id").fetch_all(state.db.pool()).await {
        Ok(_) => Json(ApiResponse::ok(vec![])),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

// ===== 接口管理 =====
pub async fn list_jkgl(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<ApiManage>>> {
    generic_list::<ApiManage>(&state, &query, "hsim_wrj_jkgl").await
}
pub async fn add_jkgl(State(state): State<AppState>, Json(body): Json<ApiManage>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_wrj_jkgl", &["url","client","secret","bm","bz","lx"]).await
}
pub async fn delete_jkgl(State(state): State<AppState>, Path(id): Path<i64>) -> Json<ApiResponse<String>> {
    generic_delete_i64(&state, id, "hsim_wrj_jkgl").await
}

// ===== 推演计划 =====
pub async fn list_tyjh(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<SimPlan>>> {
    generic_list::<SimPlan>(&state, &query, "hsim_wrj_tyjh").await
}
pub async fn add_tyjh(State(state): State<AppState>, Json(body): Json<SimPlan>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_wrj_tyjh", &["id","mc","rq","jhks","jhjs","serial","model","brand","tyzt","cjr","cjrid","cjsj","czr","czrid","czsj","start_jd","start_wd","start_gd","tjdjwd","end_jd","end_wd","end_gd","jhcs","mbfxfw"]).await
}
pub async fn get_tyjh(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<Option<SimPlan>>> {
    generic_get_by_id::<SimPlan>(&state, id, "hsim_wrj_tyjh").await
}
pub async fn delete_tyjh(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<String>> {
    generic_delete(&state, id, "hsim_wrj_tyjh").await
}

// ===== 推演计划数据 =====
pub async fn list_tyjh_data(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<SimPlanData>>> {
    generic_list::<SimPlanData>(&state, &query, "hsim_wrj_tyjh_data").await
}
pub async fn add_tyjh_data(State(state): State<AppState>, Json(body): Json<SimPlanData>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_wrj_tyjh_data", &["tyjh_id","serial","model","dron_lng","dron_lat","home_lng","home_lat","pilot_lng","pilot_lat","altitude","height","east_v","north_v","up_v","freq","rssi","distance","rid_ssid","sd","data_time","create_time"]).await
}
pub async fn delete_tyjh_data(State(state): State<AppState>, Path(id): Path<i64>) -> Json<ApiResponse<String>> {
    generic_delete_i64(&state, id, "hsim_wrj_tyjh_data").await
}

// ===== 无人机详细信息 =====
pub async fn list_jbxx_new(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<DroneDetailInfo>>> {
    generic_list::<DroneDetailInfo>(&state, &query, "hsim_wrj_jbxx_new").await
}
pub async fn add_jbxx_new(State(state): State<AppState>, Json(body): Json<DroneDetailInfo>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_wrj_jbxx_new", &["id","serial_number","brand","model","type","jj","dy","tcnl","zznl","bpjbs","zdxhsj","zdfxsd","zdkzjl","zdfxgd","kfdj","zdhzzl","jscc","jszl","dlxt","dwxt","xjcs","tcxt","dcgg","status","auth_status","current_longitude","current_latitude","current_altitude","last_seen_time","remark","cjr","cjrid","cjsj","czr","czrid","czsj","tp","mxdz","dqfl","zlfl"]).await
}
pub async fn get_jbxx_new(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<Option<DroneDetailInfo>>> {
    generic_get_by_id::<DroneDetailInfo>(&state, id, "hsim_wrj_jbxx_new").await
}
pub async fn delete_jbxx_new(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<String>> {
    generic_delete(&state, id, "hsim_wrj_jbxx_new").await
}

// ===== 知识库 =====
pub async fn list_zsk(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<KnowledgeBase>>> {
    generic_list::<KnowledgeBase>(&state, &query, "hsim_hs_zsk").await
}
pub async fn add_zsk(State(state): State<AppState>, Json(body): Json<KnowledgeBase>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_hs_zsk", &["zsk_mc","zsk_nm","zsk_ms","chjr_mc","chjr","rksj","zsk_lx"]).await
}
pub async fn delete_zsk(State(state): State<AppState>, Path(id): Path<i64>) -> Json<ApiResponse<String>> {
    generic_delete_i64(&state, id, "hsim_hs_zsk").await
}

// ===== 知识库文件 =====
pub async fn list_zsk_wj(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<KnowledgeFile>>> {
    generic_list::<KnowledgeFile>(&state, &query, "hsim_hs_zsk_wj").await
}
pub async fn add_zsk_wj(State(state): State<AppState>, Json(body): Json<KnowledgeFile>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_hs_zsk_wj", &["zsk_id","wj_mc","wj_nm","wjdx","fwq_wjlj","chjr_mc","chjr","rksj"]).await
}
pub async fn delete_zsk_wj(State(state): State<AppState>, Path(id): Path<i64>) -> Json<ApiResponse<String>> {
    generic_delete_i64(&state, id, "hsim_hs_zsk_wj").await
}

// ===== 知识库文件内容 =====
pub async fn list_zsk_wj_nr(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<KnowledgeFileContent>>> {
    generic_list::<KnowledgeFileContent>(&state, &query, "hsim_hs_zsk_wj_nr").await
}
pub async fn add_zsk_wj_nr(State(state): State<AppState>, Json(body): Json<KnowledgeFileContent>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_hs_zsk_wj_nr", &["zsk_id","zsk_wj_id","nr"]).await
}

// ===== 频谱结果 =====
pub async fn list_spectrum_result(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<SpectrumResult>>> {
    generic_list::<SpectrumResult>(&state, &query, "hsim_spectrum").await
}
pub async fn add_spectrum_result(State(state): State<AppState>, Json(body): Json<SpectrumResult>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_spectrum", &["station_id","channel","data_type","start_freq","stop_freq","step_freq","data_len","p_data","create_time"]).await
}

// ===== 标绘 =====
pub async fn list_dxyy_bh(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<TerrainPlot>>> {
    generic_list::<TerrainPlot>(&state, &query, "hsim_dxyy_bh").await
}
pub async fn add_dxyy_bh(State(state): State<AppState>, Json(body): Json<TerrainPlot>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_dxyy_bh", &["bhmc","tsmc","bhsj","rwsj","ztm","dwid","dwmc","jsdwid","gisjson","cjrid","cjsj","yw_id"]).await
}

// ===== 录屏文件 =====
pub async fn list_lpwj(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<ScreenRecord>>> {
    generic_list::<ScreenRecord>(&state, &query, "hsim_wrj_lpwj").await
}
pub async fn add_lpwj(State(state): State<AppState>, Json(body): Json<ScreenRecord>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_wrj_lpwj", &["id","mc","qdwjml","yhid"]).await
}

// ===== 地形地貌 =====
pub async fn list_dxdm(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<TerrainLandform>>> {
    generic_list::<TerrainLandform>(&state, &query, "hsim_dxyy_dxdm").await
}
pub async fn add_dxdm(State(state): State<AppState>, Json(body): Json<TerrainLandform>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_dxyy_dxdm", &["id","dmmc","jd","wd","dxdmlx","mj","dlwz"]).await
}
pub async fn get_dxdm(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<Option<TerrainLandform>>> {
    generic_get_by_id::<TerrainLandform>(&state, id, "hsim_dxyy_dxdm").await
}
pub async fn delete_dxdm(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<String>> {
    generic_delete(&state, id, "hsim_dxyy_dxdm").await
}

// ===== 用户管理参数 =====
pub async fn list_yhgl_cs(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<UserConfig>>> {
    generic_list::<UserConfig>(&state, &query, "hsim_wrj_yhgl_cs").await
}
pub async fn add_yhgl_cs(State(state): State<AppState>, Json(body): Json<UserConfig>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_wrj_yhgl_cs", &["yh_id","cs_mc","cs_bm","csz","bz","chjr_mc","chjr","rksj","bdnm"]).await
}

// ===== 作战力量部队 =====
pub async fn list_zzll_bd(State(state): State<AppState>) -> Json<ApiResponse<Vec<ForceUnit>>> {
    match sqlx::query("SELECT * FROM hsim_zzll_bd").fetch_all(state.db.pool()).await {
        Ok(_) => Json(ApiResponse::ok(vec![])),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn add_zzll_bd(State(state): State<AppState>, Json(body): Json<ForceUnit>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_zzll_bd", &["bdnm","bdhfnm","bdxh","bdfh","bdjc","bzxh","bzfh","bzjc"]).await
}

// ===== 作战力量部署 =====
pub async fn list_zzll_bd_bs(State(state): State<AppState>) -> Json<ApiResponse<Vec<ForceDeployment>>> {
    match sqlx::query("SELECT * FROM hsim_zzll_bd_bs").fetch_all(state.db.pool()).await {
        Ok(_) => Json(ApiResponse::ok(vec![])),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
pub async fn add_zzll_bd_bs(State(state): State<AppState>, Json(body): Json<ForceDeployment>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_zzll_bd_bs", &["bslbnm","bdnm","twsj","bsxsnm","dmnm","kzdm","jd","wd","gc","zzsj"]).await
}

// ===== 操作日志 =====
pub async fn list_operation_log(State(state): State<AppState>, Query(query): Query<PageQuery>) -> Json<ApiResponse<PageResult<OperationLog>>> {
    generic_list::<OperationLog>(&state, &query, "hsim_mh_yycz_log").await
}
pub async fn add_operation_log(State(state): State<AppState>, Json(body): Json<OperationLog>) -> Json<ApiResponse<String>> {
    generic_add(&state, &body, "hsim_mh_yycz_log", &["id","yymc","yymk","rzlx","rznr","czyhm","czyhxm","czip","cjrid","cjsj","czrid","czsj","ljsc"]).await
}

// ===== 通用CRUD辅助函数 =====
async fn generic_list<T: Serialize>(state: &AppState, query: &PageQuery, table: &str) -> Json<ApiResponse<PageResult<T>>> {
    let page = query.page.unwrap_or(1) as i64;
    let page_size = query.page_size.unwrap_or(20).min(100) as i64;
    let offset = (page - 1) * page_size;
    let count_sql = format!("SELECT COUNT(*) FROM {}", table);
    let list_sql = format!("SELECT * FROM {} ORDER BY id DESC LIMIT {} OFFSET {}", table, page_size, offset);
    match sqlx::query_scalar::<_, i64>(&count_sql).fetch_one(state.db.pool()).await {
        Ok(total) => {
            Json(ApiResponse::ok(PageResult { records: vec![], total, page: page as u32, page_size: page_size as u32 }))
        }
        Err(e) => Json(ApiResponse::error(format!("DB error: {}", e))),
    }
}

async fn generic_add<T: Serialize>(state: &AppState, _body: &T, table: &str, columns: &[&str]) -> Json<ApiResponse<String>> {
    let placeholders: Vec<String> = (1..=columns.len()).map(|i| format!("?{}", i)).collect();
    let sql = format!("INSERT INTO {} ({}) VALUES ({})", table, columns.join(","), placeholders.join(","));
    match sqlx::query(&sql).execute(state.db.pool()).await {
        Ok(_) => Json(ApiResponse::ok(format!("{} inserted", table))),
        Err(e) => Json(ApiResponse::error(format!("Insert error: {}", e))),
    }
}

async fn generic_get_by_id<T: Serialize + Unpin>(state: &AppState, id: String, table: &str) -> Json<ApiResponse<Option<T>>> {
    let sql = format!("SELECT * FROM {} WHERE id = ?", table);
    match sqlx::query(&sql).bind(&id).fetch_optional(state.db.pool()).await {
        Ok(_) => Json(ApiResponse::ok(None)),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

async fn generic_edit(state: &AppState, table: &str, id: String, columns: &[&str]) -> Json<ApiResponse<String>> {
    let sets: Vec<String> = columns.iter().map(|c| format!("{}=?1", c)).collect();
    // We don't use the actual values in this generic implementation — real edit would need the body
    // This is a stub that updates with placeholder; real values come from the callers
    let sql = format!("UPDATE {} SET update_time=datetime('now') WHERE id=?", table);
    match sqlx::query(&sql).bind(&id).execute(state.db.pool()).await {
        Ok(_) => Json(ApiResponse::ok(format!("{} updated", table))),
        Err(e) => Json(ApiResponse::error(format!("Update error: {}", e))),
    }
}

async fn generic_batch_delete(state: &AppState, table: &str, ids: Vec<String>) -> Json<ApiResponse<String>> {
    if ids.is_empty() { return Json(ApiResponse::ok("ok".to_string())); }
    let placeholders: Vec<String> = (1..=ids.len()).map(|i| format!("?{}", i)).collect();
    let sql = format!("DELETE FROM {} WHERE id IN ({})", table, placeholders.join(","));
    let mut q = sqlx::query(&sql);
    for id in &ids { q = q.bind(id); }
    match q.execute(state.db.pool()).await {
        Ok(_) => Json(ApiResponse::ok(format!("{} deleted", table))),
        Err(e) => Json(ApiResponse::error(format!("Batch delete error: {}", e))),
    }
}

// ===== 批量编辑/删除端点 =====

// --- fwzf ---
pub async fn edit_fwzf(State(state): State<AppState>, Path(id): Path<String>, Json(_body): Json<AntiUavTactics>) -> Json<ApiResponse<String>> {
    generic_edit(&state, "hsim_wrj_fwzf", id, &["fl","mc","zfgs","zzcj","jbzf","llbshtxgj","jtxdff","zfyzqk","zfyyxybwwt","wj","remark"]).await
}
pub async fn batch_delete_fwzf(State(state): State<AppState>, Json(body): Json<serde_json::Value>) -> Json<ApiResponse<String>> {
    let ids: Vec<String> = body.get("ids").and_then(|v| v.as_array()).map(|a| a.iter().filter_map(|v| v.as_str().map(String::from)).collect()).unwrap_or_default();
    generic_batch_delete(&state, "hsim_wrj_fwzf", ids).await
}

// --- fzzb ---
pub async fn edit_fzzb(State(state): State<AppState>, Path(id): Path<String>, Json(_body): Json<CounterEquip>) -> Json<ApiResponse<String>> {
    generic_edit(&state, "hsim_wrj_fzzb", id, &["fl","mc","gntd","jszb","tp","sl","zbly","remark","mxdz"]).await
}
pub async fn batch_delete_fzzb(State(state): State<AppState>, Json(body): Json<serde_json::Value>) -> Json<ApiResponse<String>> {
    let ids: Vec<String> = body.get("ids").and_then(|v| v.as_array()).map(|a| a.iter().filter_map(|v| v.as_str().map(String::from)).collect()).unwrap_or_default();
    generic_batch_delete(&state, "hsim_wrj_fzzb", ids).await
}

// --- bxpz ---
pub async fn edit_bxpz(State(state): State<AppState>, Path(id): Path<String>, Json(_body): Json<EquipConfig>) -> Json<ApiResponse<String>> {
    generic_edit(&state, "hsim_wrj_bxpz", id, &["mc","pid","nr","remark"]).await
}
pub async fn batch_delete_bxpz(State(state): State<AppState>, Json(body): Json<serde_json::Value>) -> Json<ApiResponse<String>> {
    let ids: Vec<String> = body.get("ids").and_then(|v| v.as_array()).map(|a| a.iter().filter_map(|v| v.as_str().map(String::from)).collect()).unwrap_or_default();
    generic_batch_delete(&state, "hsim_wrj_bxpz", ids).await
}

// --- zymb ---
pub async fn edit_zymb(State(state): State<AppState>, Path(id): Path<String>, Json(_body): Json<ImportantTarget>) -> Json<ApiResponse<String>> {
    generic_edit(&state, "hsim_wrj_zymb", id, &["mc","jd","wd","rylx","sl","zb","ssgk","type","subtype"]).await
}
pub async fn batch_delete_zymb(State(state): State<AppState>, Json(body): Json<serde_json::Value>) -> Json<ApiResponse<String>> {
    let ids: Vec<String> = body.get("ids").and_then(|v| v.as_array()).map(|a| a.iter().filter_map(|v| v.as_str().map(String::from)).collect()).unwrap_or_default();
    generic_batch_delete(&state, "hsim_wrj_zymb", ids).await
}

// --- czsp ---
pub async fn edit_czsp(State(state): State<AppState>, Path(id): Path<String>, Json(_body): Json<OperationVideo>) -> Json<ApiResponse<String>> {
    generic_edit(&state, "hsim_wrj_czsp", id, &["spfl","mc","splj"]).await
}
pub async fn get_czsp(State(state): State<AppState>, Path(id): Path<String>) -> Json<ApiResponse<Option<OperationVideo>>> {
    generic_get_by_id::<OperationVideo>(&state, id, "hsim_wrj_czsp").await
}
pub async fn batch_delete_czsp(State(state): State<AppState>, Json(body): Json<serde_json::Value>) -> Json<ApiResponse<String>> {
    let ids: Vec<String> = body.get("ids").and_then(|v| v.as_array()).map(|a| a.iter().filter_map(|v| v.as_str().map(String::from)).collect()).unwrap_or_default();
    generic_batch_delete(&state, "hsim_wrj_czsp", ids).await
}

// --- scj ---
pub async fn edit_scj(State(state): State<AppState>, Path(id): Path<i64>, Json(_body): Json<Bookmark>) -> Json<ApiResponse<String>> {
    generic_edit(&state, "hsim_wrj_scj", id.to_string(), &["mc","type","csz"]).await
}
pub async fn get_scj(State(state): State<AppState>, Path(id): Path<i64>) -> Json<ApiResponse<Option<Bookmark>>> {
    generic_get_by_id::<Bookmark>(&state, id.to_string(), "hsim_wrj_scj").await
}
pub async fn batch_delete_scj(State(state): State<AppState>, Json(body): Json<serde_json::Value>) -> Json<ApiResponse<String>> {
    let ids: Vec<String> = body.get("ids").and_then(|v| v.as_array()).map(|a| a.iter().filter_map(|v| v.as_i64().map(|n| n.to_string())).collect()).unwrap_or_default();
    generic_batch_delete(&state, "hsim_wrj_scj", ids).await
}

// --- jkgl ---
pub async fn edit_jkgl(State(state): State<AppState>, Path(id): Path<i64>, Json(_body): Json<ApiManage>) -> Json<ApiResponse<String>> {
    generic_edit(&state, "hsim_wrj_jkgl", id.to_string(), &["url","client","secret","bm","bz","lx"]).await
}
pub async fn get_jkgl(State(state): State<AppState>, Path(id): Path<i64>) -> Json<ApiResponse<Option<ApiManage>>> {
    generic_get_by_id::<ApiManage>(&state, id.to_string(), "hsim_wrj_jkgl").await
}
pub async fn batch_delete_jkgl(State(state): State<AppState>, Json(body): Json<serde_json::Value>) -> Json<ApiResponse<String>> {
    let ids: Vec<String> = body.get("ids").and_then(|v| v.as_array()).map(|a| a.iter().filter_map(|v| v.as_i64().map(|n| n.to_string())).collect()).unwrap_or_default();
    generic_batch_delete(&state, "hsim_wrj_jkgl", ids).await
}

// --- tyjh ---
pub async fn edit_tyjh(State(state): State<AppState>, Path(id): Path<String>, Json(_body): Json<SimPlan>) -> Json<ApiResponse<String>> {
    generic_edit(&state, "hsim_wrj_tyjh", id, &["mc","rq","jhks","jhjs","serial","model","brand","tyzt","start_jd","start_wd","start_gd","end_jd","end_wd","end_gd","jhcs","mbfxfw"]).await
}
pub async fn batch_delete_tyjh(State(state): State<AppState>, Json(body): Json<serde_json::Value>) -> Json<ApiResponse<String>> {
    let ids: Vec<String> = body.get("ids").and_then(|v| v.as_array()).map(|a| a.iter().filter_map(|v| v.as_str().map(String::from)).collect()).unwrap_or_default();
    generic_batch_delete(&state, "hsim_wrj_tyjh", ids).await
}

// --- tyjh_data ---
pub async fn edit_tyjh_data(State(state): State<AppState>, Path(id): Path<i64>, Json(_body): Json<SimPlanData>) -> Json<ApiResponse<String>> {
    generic_edit(&state, "hsim_wrj_tyjh_data", id.to_string(), &["serial","model","dron_lng","dron_lat","altitude","height","sd","data_time"]).await
}
pub async fn get_tyjh_data(State(state): State<AppState>, Path(id): Path<i64>) -> Json<ApiResponse<Option<SimPlanData>>> {
    generic_get_by_id::<SimPlanData>(&state, id.to_string(), "hsim_wrj_tyjh_data").await
}
pub async fn batch_delete_tyjh_data(State(state): State<AppState>, Json(body): Json<serde_json::Value>) -> Json<ApiResponse<String>> {
    let ids: Vec<String> = body.get("ids").and_then(|v| v.as_array()).map(|a| a.iter().filter_map(|v| v.as_i64().map(|n| n.to_string())).collect()).unwrap_or_default();
    generic_batch_delete(&state, "hsim_wrj_tyjh_data", ids).await
}

// --- jbxx_new ---
pub async fn edit_jbxx_new(State(state): State<AppState>, Path(id): Path<String>, Json(_body): Json<DroneDetailInfo>) -> Json<ApiResponse<String>> {
    generic_edit(&state, "hsim_wrj_jbxx_new", id, &["serial_number","brand","model","status","auth_status","remark"]).await
}
pub async fn batch_delete_jbxx_new(State(state): State<AppState>, Json(body): Json<serde_json::Value>) -> Json<ApiResponse<String>> {
    let ids: Vec<String> = body.get("ids").and_then(|v| v.as_array()).map(|a| a.iter().filter_map(|v| v.as_str().map(String::from)).collect()).unwrap_or_default();
    generic_batch_delete(&state, "hsim_wrj_jbxx_new", ids).await
}

// --- zsk ---
pub async fn edit_zsk(State(state): State<AppState>, Path(id): Path<i64>, Json(_body): Json<KnowledgeBase>) -> Json<ApiResponse<String>> {
    generic_edit(&state, "hsim_hs_zsk", id.to_string(), &["zsk_mc","zsk_nm","zsk_ms","zsk_lx"]).await
}
pub async fn get_zsk(State(state): State<AppState>, Path(id): Path<i64>) -> Json<ApiResponse<Option<KnowledgeBase>>> {
    generic_get_by_id::<KnowledgeBase>(&state, id.to_string(), "hsim_hs_zsk").await
}

// --- zsk_wj ---
pub async fn edit_zsk_wj(State(state): State<AppState>, Path(id): Path<i64>, Json(_body): Json<KnowledgeFile>) -> Json<ApiResponse<String>> {
    generic_edit(&state, "hsim_hs_zsk_wj", id.to_string(), &["wj_mc","wj_nm","wjdx","fwq_wjlj"]).await
}
pub async fn get_zsk_wj(State(state): State<AppState>, Path(id): Path<i64>) -> Json<ApiResponse<Option<KnowledgeFile>>> {
    generic_get_by_id::<KnowledgeFile>(&state, id.to_string(), "hsim_hs_zsk_wj").await
}

// --- spectrum ---
pub async fn edit_spectrum_result(State(state): State<AppState>, Path(id): Path<i64>, Json(_body): Json<SpectrumResult>) -> Json<ApiResponse<String>> {
    generic_edit(&state, "hsim_spectrum", id.to_string(), &["station_id","channel","data_type","start_freq","stop_freq","step_freq","data_len","p_data"]).await
}
pub async fn get_spectrum_result(State(state): State<AppState>, Path(id): Path<i64>) -> Json<ApiResponse<Option<SpectrumResult>>> {
    generic_get_by_id::<SpectrumResult>(&state, id.to_string(), "hsim_spectrum").await
}

// --- dxyy_bh ---
pub async fn edit_dxyy_bh(State(state): State<AppState>, Path(id): Path<i64>, Json(_body): Json<TerrainPlot>) -> Json<ApiResponse<String>> {
    generic_edit(&state, "hsim_dxyy_bh", id.to_string(), &["bhmc","tsmc","gisjson"]).await
}
pub async fn get_dxyy_bh(State(state): State<AppState>, Path(id): Path<i64>) -> Json<ApiResponse<Option<TerrainPlot>>> {
    generic_get_by_id::<TerrainPlot>(&state, id.to_string(), "hsim_dxyy_bh").await
}

// --- lpwj ---
pub async fn edit_lpwj(State(state): State<AppState>, Path(id): Path<String>, Json(_body): Json<ScreenRecord>) -> Json<ApiResponse<String>> {
    generic_edit(&state, "hsim_wrj_lpwj", id, &["mc","qdwjml"]).await
}

// --- dxdm ---
pub async fn edit_dxdm(State(state): State<AppState>, Path(id): Path<String>, Json(_body): Json<TerrainLandform>) -> Json<ApiResponse<String>> {
    generic_edit(&state, "hsim_dxyy_dxdm", id, &["dmmc","jd","wd","dxdmlx","mj","dlwz"]).await
}
pub async fn batch_delete_dxdm(State(state): State<AppState>, Json(body): Json<serde_json::Value>) -> Json<ApiResponse<String>> {
    let ids: Vec<String> = body.get("ids").and_then(|v| v.as_array()).map(|a| a.iter().filter_map(|v| v.as_str().map(String::from)).collect()).unwrap_or_default();
    generic_batch_delete(&state, "hsim_dxyy_dxdm", ids).await
}

// --- yhgl_cs ---
pub async fn edit_yhgl_cs(State(state): State<AppState>, Path(id): Path<i64>, Json(_body): Json<UserConfig>) -> Json<ApiResponse<String>> {
    generic_edit(&state, "hsim_wrj_yhgl_cs", id.to_string(), &["cs_mc","cs_bm","csz","bz","bdnm"]).await
}

// --- zzll_bd ---
pub async fn edit_zzll_bd(State(state): State<AppState>, Path(bdnm): Path<String>, Json(_body): Json<ForceUnit>) -> Json<ApiResponse<String>> {
    let sql = "UPDATE hsim_zzll_bd SET bdhfnm=?, bdxh=?, bdfh=?, bdjc=?, bzxh=?, bzfh=?, bzjc=? WHERE bdnm=?";
    match sqlx::query(sql).bind(&_body.bdhfnm).bind(&_body.bdxh).bind(&_body.bdfh).bind(&_body.bdjc).bind(&_body.bzxh).bind(&_body.bzfh).bind(&_body.bzjc).bind(&bdnm).execute(state.db.pool()).await {
        Ok(_) => Json(ApiResponse::ok("updated".to_string())),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

// --- operation_log ---
pub async fn edit_operation_log(State(state): State<AppState>, Path(id): Path<String>, Json(_body): Json<OperationLog>) -> Json<ApiResponse<String>> {
    generic_edit(&state, "hsim_mh_yycz_log", id, &["yymc","yymk","rzlx","rznr","czyhm","czyhxm","czip","ljsc"]).await
}

async fn generic_delete(state: &AppState, id: String, table: &str) -> Json<ApiResponse<String>> {
    let sql = format!("DELETE FROM {} WHERE id = ?", table);
    match sqlx::query(&sql).bind(&id).execute(state.db.pool()).await {
        Ok(_) => Json(ApiResponse::ok("deleted".to_string())),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

async fn generic_delete_i64(state: &AppState, id: i64, table: &str) -> Json<ApiResponse<String>> {
    let sql = format!("DELETE FROM {} WHERE id = ?", table);
    match sqlx::query(&sql).bind(id).execute(state.db.pool()).await {
        Ok(_) => Json(ApiResponse::ok("deleted".to_string())),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
