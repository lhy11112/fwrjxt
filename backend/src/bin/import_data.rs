use std::io::{BufRead, BufReader};
use std::fs::File;
use sqlx::sqlite::{SqliteConnectOptions, SqlitePoolOptions};
use std::str::FromStr;

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    let sql_file = "./data/hsimc2_data.sql";
    println!("=== 导入 SQLite ===");

    let pool = SqlitePoolOptions::new().max_connections(1)
        .connect_with(SqliteConnectOptions::from_str("sqlite:./data/hsimc2.db?mode=rwc")?
            .create_if_missing(true).journal_mode(sqlx::sqlite::SqliteJournalMode::Wal))
        .await?;

    // 建表
    hsimc2_backend::storage::init_database(&hsimc2_backend::config::DatabaseConfig {
        path: "./data/hsimc2.db".into(), max_connections: 5, auto_migrate: true,
    }).await?;

    // 设置同步模式提升写入性能
    sqlx::query("PRAGMA synchronous=OFF").execute(&pool).await?;
    sqlx::query("PRAGMA journal_mode=MEMORY").execute(&pool).await?;

    let input = File::open(sql_file)?;
    let reader = BufReader::new(input);
    let mut total = 0u64;
    let mut errs = 0u64;

    for line in reader.lines() {
        let sql = line?;
        match sqlx::query(&sql).execute(&pool).await {
            Ok(r) => { total += r.rows_affected(); }
            Err(e) => {
                errs += 1;
                if errs <= 5 { eprintln!("[ERR {}] {}", errs, e); }
            }
        }
        if total % 10000 == 0 { print!("\r  已导入 {} 行...", total); }
    }

    // 恢复设置
    sqlx::query("PRAGMA synchronous=NORMAL").execute(&pool).await?;
    sqlx::query("PRAGMA journal_mode=WAL").execute(&pool).await?;

    println!("\r=== 完成 === 导入: {} 行, 错误: {}", total, errs);
    Ok(())
}
