use std::io::{BufRead, BufReader, Write};
use std::fs::File;

const TABLE_MAP: &[(&str, &str)] = &[
    ("uav_device_config", "hsim_sb_pz"), ("uav_device_heartbeat", "hsim_sb_xt"),
    ("uav_connect_log", "hsim_sb_lj"), ("uav_detect_spectrum", "hsim_sb_zcpp"),
    ("uav_operate_log", "hsim_sb_czrz"), ("uav_active_heartbeat", "hsim_sb_xt_zd"),
    ("wjbd_wrj_jbxxgl", "hsim_wrj_tzk"), ("wjbd_wrj_jbxx", "hsim_wrj_jbxx"),
    ("wjbd_wrj_hbmdsq", "hsim_wrj_hbmdsq"), ("wjbd_wrj_gjjl", "hsim_wrj_gjjl"),
    ("wjbd_wrj_ky", "hsim_wrj_ky"), ("uav_detect_msg", "hsim_wrj_fxsj_zcbw"),
    ("uav_df_data", "hsim_wrj_fxsj_jx"), ("uav_remote_data", "hsim_wrj_fxsj_remote"),
];

fn main() -> anyhow::Result<()> {
    let sql_path = r"D:\福建\master\验收文档\无人机发包20260309\系统\数据库\wj_fjzd_wrjyy.sql";
    let out_path = "./data/hsimc2_data.sql";
    println!("=== 提取INSERT并转换表名 ===");

    let input = File::open(sql_path)?;
    let reader = BufReader::new(input);
    let mut out = File::create(out_path)?;
    let mut stats: std::collections::HashMap<String, u64> = std::collections::HashMap::new();

    for line in reader.lines() {
        let line = line?;
        let t = line.trim();
        if !t.to_uppercase().starts_with("INSERT INTO ") { continue; }

        let rest = &t[12..];
        let end = rest.find(|c: char| !c.is_alphanumeric() && c != '_' && c != '`').unwrap_or(rest.len());
        let tbl = rest[..end].trim_matches('`');

        if let Some(&(_, new)) = TABLE_MAP.iter().find(|(o,_)| o == &tbl) {
            let mut converted = t
                .replace(&format!("INSERT INTO `{}`", tbl), &format!("INSERT OR IGNORE INTO {}", new))
                .replace('`', "")
                .replace("\\'", "''")
                // 替换 VALUES 中的 DEFAULT 关键字为 NULL
                .replace("DEFAULT,", "NULL,")
                .replace(", DEFAULT", ", NULL")
                .replace("DEFAULT)", "NULL)");
            writeln!(out, "{}", converted)?;
            *stats.entry(new.to_string()).or_insert(0) += 1;
        }
    }
    out.flush()?;

    println!("\n提取完成:");
    let mut v: Vec<_> = stats.iter().collect(); v.sort_by_key(|(k,_)| *k);
    for (t, c) in &v { println!("  {}: {} 条", t, c); }
    println!("总计: {} 条", v.iter().map(|(_,c)| *c).sum::<u64>());
    println!("\nSQL已输出到: {}", out_path);
    Ok(())
}
