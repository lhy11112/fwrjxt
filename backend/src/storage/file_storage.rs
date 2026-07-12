use std::path::{Path, PathBuf};
use tokio::io::AsyncWriteExt;
use tracing;

use crate::config::StorageConfig;
use crate::models::*;

/// 本地文件存储管理器
///
/// 负责：
/// - 传感器原始数据帧的 JSONL 追加写入
/// - EO/IR 图像文件的存储
/// - 数据导出（CSV/JSON）
/// - 配置快照备份
/// - 过期数据清理
#[derive(Clone)]
pub struct FileStorage {
    config: StorageConfig,
}

impl FileStorage {
    pub fn new(config: StorageConfig) -> Self {
        Self { config }
    }

    pub fn config(&self) -> &StorageConfig {
        &self.config
    }

    /// 初始化所有存储目录（启动时调用）
    pub fn init(&self) -> anyhow::Result<()> {
        tracing::info!("Initializing file storage directories...");

        let dirs = [
            &self.config.data_dir,
            &self.config.sensor_data_dir,
            &self.config.log_dir,
            &self.config.export_dir,
            &self.config.config_snapshot_dir,
        ];

        for dir in &dirs {
            std::fs::create_dir_all(dir)?;
            tracing::debug!("  Created directory: {}", dir);
        }

        // 创建传感器类型子目录
        let sensor_types = ["radar", "rf", "eoir", "acoustic", "lidar"];
        for st in &sensor_types {
            let path = Path::new(&self.config.sensor_data_dir).join(st);
            std::fs::create_dir_all(&path)?;
        }

        tracing::info!("File storage directories initialized");
        Ok(())
    }

    /// 追加传感器数据帧（JSONL 格式，按传感器类型+日期分文件）
    pub async fn append_sensor_frame(&self, frame: &SensorFrame) -> anyhow::Result<()> {
        let type_dir = sensor_type_dir(&frame.sensor_type);
        let sensor_dir = Path::new(&self.config.sensor_data_dir)
            .join(&type_dir)
            .join(frame.sensor_id.to_string());
        std::fs::create_dir_all(&sensor_dir)?;

        let date_str = frame.timestamp.format("%Y-%m-%d").to_string();
        let file_path = sensor_dir.join(format!("{}.jsonl", date_str));

        let line = serde_json::to_string(frame)?;
        let mut content = line;
        content.push('\n');

        tokio::fs::OpenOptions::new()
            .create(true)
            .append(true)
            .open(&file_path)
            .await?
            .write_all(content.as_bytes())
            .await
            .map_err(|e| {
                // tokio::fs::write_all 错误转换为 anyhow
                anyhow::anyhow!("Failed to write sensor frame: {}", e)
            })?;

        Ok(())
    }

    /// 保存图像文件（返回相对路径）
    pub async fn save_image(
        &self,
        sensor_id: &uuid::Uuid,
        frame_id: &uuid::Uuid,
        data: &[u8],
        format: &str,
    ) -> anyhow::Result<String> {
        let date_str = chrono::Utc::now().format("%Y-%m-%d").to_string();
        let dir = Path::new(&self.config.sensor_data_dir)
            .join("eoir")
            .join(sensor_id.to_string())
            .join(&date_str);
        std::fs::create_dir_all(&dir)?;

        let filename = format!("{}.{}", frame_id, format);
        let file_path = dir.join(&filename);

        tokio::fs::write(&file_path, data).await?;

        // 返回相对于 data_dir 的路径
        let rel_path = file_path
            .strip_prefix(&self.config.data_dir)
            .unwrap_or(&file_path);
        Ok(rel_path.to_string_lossy().to_string())
    }

    /// 导出目标列表为 JSON 文件（返回文件路径）
    pub async fn export_targets_json(
        &self,
        targets: &[TrackedTarget],
    ) -> anyhow::Result<String> {
        let timestamp = chrono::Utc::now().format("%Y%m%d_%H%M%S").to_string();
        let filename = format!("targets_{}.json", timestamp);
        let file_path = Path::new(&self.config.export_dir).join(&filename);

        let json = serde_json::to_string_pretty(targets)?;
        tokio::fs::write(&file_path, json).await?;

        Ok(file_path.to_string_lossy().to_string())
    }

    /// 导出任务列表为 JSON 文件（返回文件路径）
    pub async fn export_missions_json(
        &self,
        missions: &[InterceptMission],
    ) -> anyhow::Result<String> {
        let timestamp = chrono::Utc::now().format("%Y%m%d_%H%M%S").to_string();
        let filename = format!("missions_{}.json", timestamp);
        let file_path = Path::new(&self.config.export_dir).join(&filename);

        let json = serde_json::to_string_pretty(missions)?;
        tokio::fs::write(&file_path, json).await?;

        Ok(file_path.to_string_lossy().to_string())
    }

    /// 导出目标列表为 CSV 文件（返回文件路径）
    pub async fn export_targets_csv(
        &self,
        targets: &[TrackedTarget],
    ) -> anyhow::Result<String> {
        let timestamp = chrono::Utc::now().format("%Y%m%d_%H%M%S").to_string();
        let filename = format!("targets_{}.csv", timestamp);
        let file_path = Path::new(&self.config.export_dir).join(&filename);

        let mut csv = String::from("target_id,track_id,classification,threat_level,confidence,latitude,longitude,altitude,vn,ve,vd,first_seen,last_update\n");
        for t in targets {
            csv.push_str(&format!(
                "{},{},{:?},{:?},{:.4},{:.6},{:.6},{:.1},{:.2},{:.2},{:.2},{},{}\n",
                t.target_id,
                t.track_id,
                t.classification,
                t.threat_level,
                t.confidence,
                t.position.latitude,
                t.position.longitude,
                t.position.altitude,
                t.velocity.vn,
                t.velocity.ve,
                t.velocity.vd,
                t.first_seen.to_rfc3339(),
                t.last_update.to_rfc3339(),
            ));
        }
        tokio::fs::write(&file_path, csv).await?;

        Ok(file_path.to_string_lossy().to_string())
    }

    /// 保存配置快照
    pub async fn save_config_snapshot(&self, config: &crate::config::AppConfig) -> anyhow::Result<()> {
        let timestamp = chrono::Utc::now().format("%Y%m%d_%H%M%S").to_string();
        let filename = format!("config_{}.toml", timestamp);
        let file_path = Path::new(&self.config.config_snapshot_dir).join(&filename);

        let toml_str = toml::to_string_pretty(config)?;
        tokio::fs::write(&file_path, toml_str).await?;

        tracing::info!("Config snapshot saved: {}", file_path.display());
        Ok(())
    }

    /// 清理过期的传感器数据文件（超过 retention_days 天的删除）
    pub async fn cleanup_old_files(&self) -> anyhow::Result<CleanupStats> {
        let retention = self.config.sensor_retention_days;
        let cutoff = chrono::Utc::now() - chrono::Duration::days(retention as i64);

        let mut stats = CleanupStats::default();

        let sensor_dir = Path::new(&self.config.sensor_data_dir);
        if !sensor_dir.exists() {
            return Ok(stats);
        }

        // 递归遍历传感器数据目录
        self.cleanup_dir(sensor_dir, &cutoff, &mut stats)?;

        if stats.files_deleted > 0 {
            tracing::info!(
                "File cleanup completed: {} files deleted, {} bytes freed",
                stats.files_deleted,
                stats.bytes_freed
            );
        }

        Ok(stats)
    }

    fn cleanup_dir(
        &self,
        dir: &Path,
        cutoff: &chrono::DateTime<chrono::Utc>,
        stats: &mut CleanupStats,
    ) -> anyhow::Result<()> {
        if !dir.is_dir() {
            return Ok(());
        }

        for entry in std::fs::read_dir(dir)? {
            let entry = entry?;
            let path = entry.path();

            if path.is_dir() {
                self.cleanup_dir(&path, cutoff, stats)?;
                // 删除空目录
                if std::fs::read_dir(&path)?.next().is_none() {
                    std::fs::remove_dir(&path)?;
                    stats.dirs_removed += 1;
                }
            } else if path.is_file() {
                // 检查文件修改时间
                if let Ok(metadata) = std::fs::metadata(&path) {
                    if let Ok(modified) = metadata.modified() {
                        let modified_dt: chrono::DateTime<chrono::Utc> = modified.into();
                        if modified_dt < *cutoff {
                            let file_size = metadata.len();
                            std::fs::remove_file(&path)?;
                            stats.files_deleted += 1;
                            stats.bytes_freed += file_size;
                        }
                    }
                }
            }
        }

        Ok(())
    }

    /// 获取传感器数据目录大小（字节）
    pub fn get_sensor_data_size(&self) -> u64 {
        let dir = Path::new(&self.config.sensor_data_dir);
        dir_size(dir)
    }

    /// 获取数据库文件大小（字节）
    pub fn get_database_size(&self, db_path: &str) -> u64 {
        match std::fs::metadata(db_path) {
            Ok(m) => m.len(),
            Err(_) => 0,
        }
    }
}

#[derive(Debug, Default)]
pub struct CleanupStats {
    pub files_deleted: u64,
    pub bytes_freed: u64,
    pub dirs_removed: u64,
}

/// 传感器类型 → 目录名映射
fn sensor_type_dir(sensor_type: &SensorType) -> String {
    match sensor_type {
        SensorType::Radar => "radar".into(),
        SensorType::Rf => "rf".into(),
        SensorType::EoIr => "eoir".into(),
        SensorType::Acoustic => "acoustic".into(),
        SensorType::Lidar => "lidar".into(),
        SensorType::Other(_) => "other".into(),
    }
}

/// 递归计算目录大小
fn dir_size(path: &Path) -> u64 {
    if !path.is_dir() {
        return 0;
    }
    let mut total = 0u64;
    if let Ok(entries) = std::fs::read_dir(path) {
        for entry in entries.flatten() {
            let p = entry.path();
            if p.is_file() {
                total += std::fs::metadata(&p).map(|m| m.len()).unwrap_or(0);
            } else if p.is_dir() {
                total += dir_size(&p);
            }
        }
    }
    total
}
