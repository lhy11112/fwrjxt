//! HSimC2 传感器软件开发工具包 (Sensor SDK)
//!
//! 提供传感器标准化接入、数据采集和与 HSimC2 服务器通信的客户端库。

pub mod client;
pub mod types;

/// SDK 版本
pub const VERSION: &str = env!("CARGO_PKG_VERSION");
