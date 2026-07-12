//! HSimC2 Protobuf 消息定义
//!
//! 传感器数据通信的序列化协议

pub mod v1 {
    /// 传感器数据帧 Protobuf 消息
    /// 对应 backend 层 SensorFrame 的序列化格式
    ///
    /// ```protobuf
    /// message SensorFrame {
    ///     string frame_id = 1;
    ///     string sensor_id = 2;
    ///     string sensor_type = 3;
    ///     int64 timestamp_nanos = 4;
    ///     double latitude = 5;
    ///     double longitude = 6;
    ///     double altitude = 7;
    ///     double confidence = 8;
    ///     bytes payload = 9;
    /// }
    /// ```
    pub const SENSOR_FRAME_PROTO_ID: &str = "hsimc2.proto.v1.SensorFrame";

    /// 支持的传感器类型
    pub const SENSOR_TYPE_RADAR: &str = "RADAR";
    pub const SENSOR_TYPE_RF: &str = "RF";
    pub const SENSOR_TYPE_EOIR: &str = "EO_IR";
    pub const SENSOR_TYPE_ACOUSTIC: &str = "ACOUSTIC";

    /// 态势更新消息
    /// ```protobuf
    /// message SituationUpdate {
    ///     int64 timestamp = 1;
    ///     repeated TrackedTarget targets = 2;
    /// }
    /// ```
    pub const SITUATION_UPDATE_PROTO_ID: &str = "hsimc2.proto.v1.SituationUpdate";
}
