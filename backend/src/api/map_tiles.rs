/// 地图瓦片代理 + 本地缓存
use axum::{
    body::Body,
    extract::{Path, State},
    http::{header, StatusCode, Response},
};
use super::AppState;

const MAX_RETRIES: u32 = 3;
const RETRY_DELAY_MS: u64 = 500;

/// 瓦片代理: GET /api/v1/tiles/{z}/{y}/{x} (与 ArcGIS ESRI 格式一致)
/// 先读本地缓存 → 未命中则从 ESRI 下载(最多 3 次重试) → 写入缓存
/// 缓存永久保留，仅手动清理
pub async fn tile_proxy(
    State(state): State<AppState>,
    Path((z, y, x)): Path<(u32, u32, u32)>,
) -> Response<Body> {
    let map_conf = &state.config.map;
    let cache_dir = std::path::Path::new(&map_conf.cache_dir);
    let cache_file = cache_dir.join(format!("{}/{}/{}.png", z, x, y));

    // 1. 尝试从本地缓存读取（永久有效）
    if cache_file.exists() {
        if let Ok(data) = tokio::fs::read(&cache_file).await {
            return Response::builder()
                .status(StatusCode::OK)
                .header(header::CONTENT_TYPE, "image/jpeg")
                .header(header::CACHE_CONTROL, "public, max-age=604800, immutable")
                .body(Body::from(data))
                .unwrap();
        }
    }

    // 2. 缓存未命中，从外部瓦片服务器下载（带重试）
    let tile_url = map_conf.tile_server_url
        .replace("{z}", &z.to_string())
        .replace("{x}", &x.to_string())
        .replace("{y}", &y.to_string());

    let client = reqwest::Client::builder()
        .timeout(std::time::Duration::from_secs(10))
        .build()
        .unwrap_or_default();

    let mut last_error = String::new();
    for attempt in 0..MAX_RETRIES {
        if attempt > 0 {
            tokio::time::sleep(std::time::Duration::from_millis(RETRY_DELAY_MS)).await;
        }

        match client.get(&tile_url).send().await {
            Ok(resp) if resp.status().is_success() => {
                if let Ok(bytes) = resp.bytes().await {
                    // 3. 写入本地永久缓存
                    if let Some(parent) = cache_file.parent() {
                        let _ = tokio::fs::create_dir_all(parent).await;
                    }
                    let _ = tokio::fs::write(&cache_file, &bytes).await;

                    return Response::builder()
                        .status(StatusCode::OK)
                        .header(header::CONTENT_TYPE, "image/jpeg")
                        .header(header::CACHE_CONTROL, "public, max-age=604800, immutable")
                        .body(Body::from(bytes.to_vec()))
                        .unwrap();
                }
                // bytes 读取失败，继续重试
                last_error = "failed to read response bytes".into();
            }
            Ok(resp) => {
                last_error = format!("HTTP {}", resp.status());
                if resp.status() == StatusCode::NOT_FOUND {
                    break; // 404 不重试
                }
            }
            Err(e) => {
                last_error = format!("{}", e);
            }
        }
    }

    // 4. 所有重试失败 → 返回 404（浏览器不会缓存错误响应）
    tracing::warn!(
        "Tile fetch failed after {} retries: {} (tile: z={}, y={}, x={})",
        MAX_RETRIES, last_error, z, y, x
    );
    Response::builder()
        .status(StatusCode::NOT_FOUND)
        .header(header::CACHE_CONTROL, "no-store")
        .body(Body::empty())
        .unwrap()
}

/// 获取地图配置（前端初始化时调用）
pub async fn map_config(
    State(state): State<AppState>,
) -> axum::Json<serde_json::Value> {
    let mc = &state.config.map;
    axum::Json(serde_json::json!({
        "tile_server_url": mc.tile_server_url,
        "center": [mc.center_lat, mc.center_lon],
        "default_zoom": mc.default_zoom,
        "cache_ttl_days": mc.cache_ttl_days,
    }))
}
