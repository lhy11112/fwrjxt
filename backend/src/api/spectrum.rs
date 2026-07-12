/// 频谱数据查询 API (hsim_sb_zcpp)
use axum::{extract::Query, extract::State, Json};
use super::AppState;
use crate::models::*;

pub async fn list_spectrum(
    State(state): State<AppState>,
    Query(query): Query<PageQuery>,
) -> Json<ApiResponse<PageResult<DetectSpectrum>>> {
    let page = query.page.unwrap_or(1);
    let page_size = query.page_size.unwrap_or(20).min(100);
    match state.db.list_detect_spectrums(page as i64, page_size as i64, &query.filters).await {
        Ok((r, t)) => Json(ApiResponse::ok(PageResult { records: r, total: t, page, page_size })),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}

/// 获取指定日期的频谱数据
pub async fn list_spectrum_today(
    State(state): State<AppState>,
    Query(params): Query<std::collections::HashMap<String, String>>,
) -> Json<ApiResponse<PageResult<DetectSpectrum>>> {
    let page: u32 = params.get("page").and_then(|v| v.parse().ok()).unwrap_or(1);
    let page_size: u32 = params.get("page_size").and_then(|v| v.parse().ok()).unwrap_or(20).min(100);
    let rq = params.get("rq").cloned().unwrap_or_default();
    let mut filters = std::collections::HashMap::new();
    filters.insert("rq".into(), rq);
    match state.db.list_detect_spectrums(page as i64, page_size as i64, &filters).await {
        Ok((r, t)) => Json(ApiResponse::ok(PageResult { records: r, total: t, page, page_size })),
        Err(e) => Json(ApiResponse::error(e.to_string())),
    }
}
