# HSimC2 技术路线改造 PRD

> **文档版本**: V1.0  
> **编制日期**: 2026-07-04  
> **改造目标**: 数据库切换 SQLite、移除 Redis 依赖、文件存储改用本地存储  
> **当前状态**: 待确认 → 确认后进入开发阶段

---

## 一、当前项目状态分析

### 1.1 技术栈现状

| 层级 | 当前技术 | 实际使用情况 |
|------|---------|-------------|
| 后端框架 | Rust + Axum 0.7 + Tokio | ✅ 正常运行 |
| 消息总线 | Tokio broadcast + mpsc | ✅ 内存通道，无外部依赖 |
| 数据库 | `sqlx 0.8` (PostgreSQL feature) | ❌ **仅声明依赖，代码中零使用** — 无任何 DB 连接/查询 |
| 缓存 | `redis 0.26` crate | ❌ **仅声明依赖，代码中零使用** — 无任何 Redis 连接/操作 |
| 文件存储 | `sensor.data_dir = "./data/sensors"` | ❌ **仅配置声明，代码中零使用** — 无任何文件读写持久化 |
| 数据存储 | `RwLock<HashMap<Uuid, T>>` | ✅ 所有模块均使用内存存储 |
| 前端 | Vue 3 + TypeScript + Pinia | ✅ 正常运行 |

### 1.2 关键发现

1. **PostgreSQL (`sqlx`)**: 在 `backend/Cargo.toml` 中声明了 `sqlx` 依赖（带 `postgres` feature），`config.toml` 和 `.env.example` 中配置了数据库连接字符串，但**整个代码库中没有任何地方建立数据库连接、执行 SQL 查询或读写数据库**。所有模块（tracker、fusion、planner、threat、sensors、simulator）的数据操作全部基于 `RwLock<HashMap>` 的内存操作。

2. **Redis**: 在 `backend/Cargo.toml` 中声明了 `redis` 依赖，`config.toml` 中配置了 Redis 连接字符串，但**整个代码库中没有任何地方连接 Redis 或使用 Redis 做缓存**。消息总线使用 Tokio 原生 `broadcast`/`mpsc` 通道，WebSocket 推送直接在内存中完成。

3. **文件存储**: 配置中定义了 `sensor.data_dir = "./data/sensors"`，但**代码中并未实际使用此目录进行任何持久化存储**。所有传感器数据帧和仿真数据仅在内存中流转。

### 1.3 受影响的文件清单

#### 必须修改的文件
| 文件 | 修改内容 |
|------|---------|
| `backend/Cargo.toml` | 替换 sqlx postgres → sqlite；移除 redis 依赖 |
| `backend/config.toml` | 重构 [database] 为 SQLite 配置；移除 [redis] 节 |
| `backend/.env.example` | 移除 DATABASE_URL(PostgreSQL)、REDIS_URL；新增 SQLite 路径 |
| `backend/src/config/mod.rs` | 重构 DatabaseConfig（SQLite 路径）；移除 RedisConfig |
| `backend/src/main.rs` | 新增 SQLite 初始化逻辑；移除 Redis 相关引用 |

#### 新增文件（核心持久化层）
| 文件 | 说明 |
|------|------|
| `backend/src/storage/mod.rs` | 统一存储模块入口，导出 DB + File 子模块 |
| `backend/src/storage/database.rs` | SQLite 连接池管理 + Schema 迁移 |
| `backend/src/storage/repository.rs` | 数据仓库层：CRUD for targets/missions/sensors/alerts |
| `backend/src/storage/file_storage.rs` | 本地文件存储：传感器数据、日志、配置快照、导出文件 |
| `backend/src/lib.rs` | 新增 `pub mod storage;` |

#### 需要适配改造的模块
| 模块 | 当前状态 | 改造内容 |
|------|---------|---------|
| `backend/src/tracker/mod.rs` | 内存 HashMap | 增加 SQLite 持久化读写（活跃航迹可保留内存，历史数据落盘） |
| `backend/src/sensors/mod.rs` | 内存 HashMap | 传感器元数据持久化到 SQLite |
| `backend/src/planner/mod.rs` | 无状态持久化 | 任务/航路点持久化到 SQLite |
| `backend/src/threat/mod.rs` | 无状态持久化 | 威胁评估记录持久化到 SQLite（可选，用于审计） |
| `backend/src/api/dashboard.rs` | 从内存读取 | 切换到 Repository 层读取 |
| `backend/src/api/mission.rs` | 返回空/TODO | 实现真实持久化 CRUD |
| `backend/src/api/sensor.rs` | 从 SensorManager 读取 | 通过 Repository 层统一读取 |

#### 不需要修改的文件
| 文件 | 原因 |
|------|------|
| `backend/src/bus/mod.rs` | 纯内存消息通道，与持久化无关 |
| `backend/src/core/mod.rs` | 核心引擎管理，不涉及存储 |
| `backend/src/core/pipeline.rs` | 数据管线处理，不涉及存储 |
| `backend/src/fusion/*` | 融合算法，不涉及存储 |
| `backend/src/simulator/mod.rs` | 仿真引擎，数据实时产生，不持久化 |
| `frontend/*` | 前端通过 REST API 获取数据，后端存储变更对其透明 |
| `sdk/*` | SDK 仅定义接口和类型 |
| `proto/*` | Protobuf 定义，无变更 |
| `docs/*` | 文档，后续更新 |

---

## 二、改造方案

### 2.1 总体架构

```
┌────────────────────────────────────────────────────────────┐
│                      HSimC2 Backend                        │
├────────────────────────────────────────────────────────────┤
│  API Layer (axum routers)                                  │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐     │
│  │dashboard │ │ mission  │ │ sensor   │ │ simulator│     │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘     │
│       │            │            │            │             │
│       ▼            ▼            ▼            ▼             │
│  ┌──────────────────────────────────────────────────────┐ │
│  │              Storage Layer (NEW)                     │ │
│  │  ┌─────────────────┐  ┌───────────────────────────┐  │ │
│  │  │ Repository       │  │ FileStorage               │  │ │
│  │  │ (SQLite CRUD)    │  │ (本地文件读写)              │  │ │
│  │  │                  │  │                            │  │ │
│  │  │ • targets 存储    │  │ • sensor_data/  传感器数据  │  │ │
│  │  │ • missions 存储   │  │ • logs/         日志归档   │  │ │
│  │  │ • sensors 存储    │  │ • exports/      数据导出   │  │ │
│  │  │ • alerts 存储     │  │ • config/       配置快照   │  │ │
│  │  └────────┬────────┘  └────────────┬──────────────┘  │ │
│  │           │                        │                  │ │
│  │           ▼                        ▼                  │ │
│  │  ┌─────────────────┐  ┌───────────────────────────┐  │ │
│  │  │ SQLite DB        │  │ Local Filesystem           │  │ │
│  │  │ hsimc2.db        │  │ ./data/                    │  │ │
│  │  └─────────────────┘  └───────────────────────────┘  │ │
│  └──────────────────────────────────────────────────────┘ │
├────────────────────────────────────────────────────────────┤
│  Core Modules (保持内存操作 + 可选持久化)                      │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐     │
│  │ tracker  │ │ threat   │ │ planner  │ │ fusion   │     │
│  │HashMap   │ │HashMap   │ │HashMap   │ │HashMap   │     │
│  │+ SQLite  │ │+ SQLite  │ │+ SQLite  │ │ (pure mem)│     │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘     │
│  ┌──────────┐ ┌──────────┐                                │
│  │ sensors  │ │ bus      │                                │
│  │HashMap   │ │broadcast │  ← 完全不涉及持久化               │
│  │+ SQLite  │ │channel   │                                │
│  └──────────┘ └──────────┘                                │
└────────────────────────────────────────────────────────────┘
```

**核心设计原则**：
- **热数据在内存，冷数据在 SQLite**：活跃目标、在线传感器等高频读写数据保留在 `RwLock<HashMap>` 中，查询/历史数据通过 Repository 落盘
- **写入策略**: 非阻塞异步写入（tokio::spawn 后台写 SQLite，不阻塞实时管道）
- **前端透明**: API 接口签名不变，仅底层从 HashMap 读取切换到 Repository 读取

### 2.2 SQLite 替代 PostgreSQL 方案

#### 2.2.1 依赖变更

```toml
# backend/Cargo.toml — 变更部分

# 移除:
# sqlx = { version = "0.8", features = ["runtime-tokio", "postgres", "chrono", "uuid"] }
# redis = { version = "0.26", features = ["tokio-comp", "connection-manager"] }

# 新增:
sqlx = { version = "0.8", features = ["runtime-tokio", "sqlite", "chrono", "uuid"] }
# redis 完全移除 — 无需替代品
```

#### 2.2.2 数据库 Schema 设计

使用 SQLite 单文件数据库 `./data/hsimc2.db`，设计以下表：

```sql
-- 传感器注册表
CREATE TABLE IF NOT EXISTS sensors (
    sensor_id    TEXT PRIMARY KEY,   -- UUID
    sensor_type  TEXT NOT NULL,      -- Radar/Rf/EoIr/Acoustic/Lidar
    name         TEXT NOT NULL,
    model        TEXT NOT NULL,
    latitude     REAL NOT NULL,
    longitude    REAL NOT NULL,
    altitude     REAL NOT NULL,
    status       TEXT NOT NULL DEFAULT 'ONLINE',  -- Online/Offline/Degraded/Calibrating
    capabilities TEXT,               -- JSON array
    config       TEXT,               -- JSON object
    last_heartbeat TEXT NOT NULL,    -- ISO8601 timestamp
    created_at   TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at   TEXT NOT NULL DEFAULT (datetime('now'))
);

-- 目标航迹表（历史记录）
CREATE TABLE IF NOT EXISTS targets (
    target_id       TEXT PRIMARY KEY,   -- UUID
    track_id        TEXT NOT NULL,      -- UUID
    classification  TEXT NOT NULL,      -- TargetClass
    threat_level    TEXT NOT NULL,      -- Green/Yellow/Red
    confidence      REAL NOT NULL,
    latitude        REAL NOT NULL,
    longitude       REAL NOT NULL,
    altitude        REAL NOT NULL,
    vn              REAL NOT NULL DEFAULT 0,
    ve              REAL NOT NULL DEFAULT 0,
    vd              REAL NOT NULL DEFAULT 0,
    first_seen      TEXT NOT NULL,      -- ISO8601
    last_update     TEXT NOT NULL,      -- ISO8601
    is_active       INTEGER NOT NULL DEFAULT 1,
    created_at      TEXT NOT NULL DEFAULT (datetime('now'))
);

-- 航迹历史点位表
CREATE TABLE IF NOT EXISTS track_points (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    target_id  TEXT NOT NULL,       -- FK → targets.target_id
    timestamp  TEXT NOT NULL,       -- ISO8601
    latitude   REAL NOT NULL,
    longitude  REAL NOT NULL,
    altitude   REAL NOT NULL,
    vn         REAL NOT NULL DEFAULT 0,
    ve         REAL NOT NULL DEFAULT 0,
    vd         REAL NOT NULL DEFAULT 0,
    FOREIGN KEY (target_id) REFERENCES targets(target_id)
);

CREATE INDEX IF NOT EXISTS idx_track_points_target ON track_points(target_id, timestamp);

-- 拦截任务表
CREATE TABLE IF NOT EXISTS missions (
    mission_id       TEXT PRIMARY KEY,   -- UUID
    target_id        TEXT NOT NULL,       -- FK → targets.target_id
    interceptor_ids  TEXT NOT NULL,       -- JSON array of UUIDs
    trajectory       TEXT NOT NULL,       -- JSON array of Waypoints
    priority         INTEGER NOT NULL DEFAULT 3,
    status           TEXT NOT NULL DEFAULT 'PLANNING',  -- Planning/Active/Executing/Completed/Failed/Cancelled
    created_at       TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at       TEXT NOT NULL DEFAULT (datetime('now'))
);

-- 告警记录表（用于审计回放）
CREATE TABLE IF NOT EXISTS alerts (
    alert_id    TEXT PRIMARY KEY,   -- UUID
    alert_type  TEXT NOT NULL,      -- ThreatDetected/MissionCritical/SystemError/SensorOffline/CommunicationLost
    severity    TEXT NOT NULL,      -- Critical/Warning/Info
    title       TEXT NOT NULL,
    description TEXT NOT NULL,
    source      TEXT NOT NULL,
    timestamp   TEXT NOT NULL       -- ISO8601
);

-- 系统事件日志表
CREATE TABLE IF NOT EXISTS system_events (
    event_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    event_type TEXT NOT NULL,       -- EngineStarted/SensorRegistered/MissionAssigned/SystemShutdown
    payload    TEXT NOT NULL,       -- JSON
    timestamp  TEXT NOT NULL DEFAULT (datetime('now'))
);
```

#### 2.2.3 配置变更

```toml
# config.toml — 变更部分

# 移除:
# [database]
# url = "postgres://hsimc2:hsimc2@localhost:5432/hsimc2"
# max_connections = 20
# connect_timeout_secs = 10

# [redis]
# url = "redis://localhost:6379"
# pool_size = 10

# 新增:
[database]
path = "./data/hsimc2.db"         # SQLite 数据库文件路径
max_connections = 10               # SQLite 连接池大小（WAL 模式下读并发数）
auto_migrate = true                # 启动时自动执行 Schema 迁移

[storage]
data_dir = "./data"                # 统一数据根目录
sensor_data_dir = "./data/sensors" # 传感器原始数据存储
log_dir = "./data/logs"            # 日志归档
export_dir = "./data/exports"      # 数据导出
config_snapshot_dir = "./data/config_snapshots"  # 配置快照备份
```

#### 2.2.4 配置结构体变更 (config/mod.rs)

```rust
// 移除:
// pub database: DatabaseConfig,     → 改为SQLite路径配置
// pub redis: RedisConfig,           → 完全移除

// 变更后:
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AppConfig {
    pub server: ServerConfig,
    pub database: DatabaseConfig,    // 内部字段改为 sqlite 路径
    pub storage: StorageConfig,      // 新增: 文件存储配置
    pub sensor: SensorConfig,
    pub planner: PlannerConfig,
    pub threat: ThreatConfig,
    pub log: LogConfig,
    // redis 字段完全移除
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DatabaseConfig {
    pub path: String,                // SQLite 文件路径
    pub max_connections: u32,
    pub auto_migrate: bool,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct StorageConfig {
    pub data_dir: String,
    pub sensor_data_dir: String,
    pub log_dir: String,
    pub export_dir: String,
    pub config_snapshot_dir: String,
}
```

### 2.3 移除 Redis 方案

**影响分析**：Redis 依赖在项目中**完全未被使用**：
- 代码中无任何 `redis::*` 导入或调用
- 消息通道使用 Tokio 原生 `broadcast::channel` / `mpsc::channel`
- WebSocket 会话管理通过 `tokio::spawn` + `broadcast::Receiver` 实现
- 无会话状态、无分布式锁、无缓存需求

**移除清单**：
1. `backend/Cargo.toml`: 删除 `redis` 依赖行
2. `backend/config.toml`: 删除 `[redis]` 配置节
3. `backend/.env.example`: 删除 `REDIS_URL` 行
4. `backend/src/config/mod.rs`: 删除 `RedisConfig` 结构体，删除 `AppConfig.redis` 字段，删除 `from_env()` 中的 Redis 初始化
5. `Cargo.lock`: 重新生成（自动移除 Redis 及其传递依赖）

**零风险** — 这是纯粹的 dead code 清理，不影响任何运行时行为。

### 2.4 本地文件存储方案

#### 2.4.1 目录结构

```
./data/                          # 由 storage.data_dir 配置
├── hsimc2.db                    # SQLite 数据库
├── sensors/                     # sensor_data_dir
│   ├── radar/                   # 雷达原始数据（按传感器分目录）
│   │   └── {sensor_id}/
│   │       └── {YYYY-MM-DD}.jsonl
│   ├── rf/                      # RF 频谱数据
│   ├── eoir/                    # 光电/红外图像
│   │   └── {sensor_id}/
│   │       └── {YYYY-MM-DD}/
│   │           └── {frame_id}.jpg
│   └── acoustic/                # 声学数据
├── exports/                     # 数据导出
│   ├── missions_{timestamp}.csv
│   └── targets_{timestamp}.json
├── config_snapshots/            # 配置快照
│   └── config_{timestamp}.toml
└── logs/                        # 日志归档
    └── hsimc2_{YYYY-MM-DD}.log
```

#### 2.4.2 FileStorage 模块设计

```rust
// backend/src/storage/file_storage.rs

pub struct FileStorage {
    config: StorageConfig,
}

impl FileStorage {
    /// 初始化存储目录（启动时自动创建）
    pub fn init(&self) -> anyhow::Result<()> { ... }

    /// 写入传感器数据帧（JSONL 追加写）
    pub async fn append_sensor_frame(&self, frame: &SensorFrame) -> anyhow::Result<()> { ... }

    /// 保存光电/红外图像
    pub async fn save_image(&self, sensor_id: &Uuid, frame_id: &Uuid, data: &[u8]) -> anyhow::Result<String> { ... }

    /// 导出目标列表为 CSV/JSON
    pub async fn export_targets(&self, targets: &[TrackedTarget], format: ExportFormat) -> anyhow::Result<String> { ... }

    /// 导出任务记录
    pub async fn export_missions(&self, missions: &[InterceptMission], format: ExportFormat) -> anyhow::Result<String> { ... }

    /// 保存配置快照
    pub async fn save_config_snapshot(&self, config: &AppConfig) -> anyhow::Result<()> { ... }

    /// 清理过期文件（超过 retention_days 的数据）
    pub async fn cleanup_old_files(&self, retention_days: u32) -> anyhow::Result<()> { ... }
}
```

#### 2.4.3 实现要点

- **写入策略**: 传感器数据帧以 JSONL（每行一个 JSON）格式追加写入，高效且可流式读取
- **图像存储**: EO/IR 图像按 `{sensor}/{date}/{frame_id}.jpg` 组织，返回相对路径供 API 引用
- **数据导出**: 支持 CSV 和 JSON 两种格式，按需触发
- **自动清理**: 后台定时任务清理超过 N 天的原始传感器数据，防止磁盘占满
- **目录创建**: 所有目录在 `FileStorage::init()` 时自动创建，无需手动维护

### 2.5 Repository 层设计

```rust
// backend/src/storage/repository.rs

use sqlx::SqlitePool;

pub struct Repository {
    pool: SqlitePool,
}

impl Repository {
    pub fn new(pool: SqlitePool) -> Self { ... }

    // -- Sensors --
    pub async fn insert_sensor(&self, sensor: &SensorMetadata) -> Result<()> { ... }
    pub async fn list_sensors(&self) -> Result<Vec<SensorMetadata>> { ... }
    pub async fn get_sensor(&self, id: &Uuid) -> Result<Option<SensorMetadata>> { ... }
    pub async fn update_sensor_status(&self, id: &Uuid, status: &SensorStatus) -> Result<()> { ... }
    pub async fn delete_sensor(&self, id: &Uuid) -> Result<()> { ... }

    // -- Targets --
    pub async fn upsert_target(&self, target: &TrackedTarget) -> Result<()> { ... }
    pub async fn get_active_targets(&self) -> Result<Vec<TrackedTarget>> { ... }
    pub async fn get_target_history(&self, id: &Uuid) -> Result<Vec<TrackPoint>> { ... }
    pub async fn append_track_point(&self, target_id: &Uuid, point: &TrackPoint) -> Result<()> { ... }

    // -- Missions --
    pub async fn insert_mission(&self, mission: &InterceptMission) -> Result<()> { ... }
    pub async fn list_missions(&self) -> Result<Vec<InterceptMission>> { ... }
    pub async fn get_mission(&self, id: &Uuid) -> Result<Option<InterceptMission>> { ... }
    pub async fn update_mission_status(&self, id: &Uuid, status: &MissionStatus) -> Result<()> { ... }

    // -- Alerts --
    pub async fn insert_alert(&self, alert: &AlertEvent) -> Result<()> { ... }
    pub async fn list_alerts(&self, limit: u32) -> Result<Vec<AlertEvent>> { ... }
}
```

### 2.6 AppState 变更

```rust
// backend/src/api/mod.rs — AppState 结构体变更

#[derive(Clone)]
pub struct AppState {
    pub config: AppConfig,                    // 移动顺序: config 放首位
    pub db: Repository,                       // 新增: SQLite 数据仓库
    pub file_storage: Arc<FileStorage>,       // 新增: 本地文件存储
    pub engine: Arc<CoreEngine>,
    pub bus: Arc<MessageBus>,
    pub sensors: Arc<SensorManager>,
    pub fusion: Arc<FusionEngine>,
    pub tracker: Arc<MultiTargetTracker>,
    pub threat: Arc<ThreatAssessor>,
    pub planner: Arc<MissionPlanner>,
    pub simulator: Arc<SensorSimulator>,
}
```

---

## 三、实施计划

### Phase 1: 依赖清理 + 基础设施（预计 2-3 小时）

| # | 任务 | 文件 | 说明 |
|---|------|------|------|
| 1.1 | 修改 `Cargo.toml` 依赖 | `backend/Cargo.toml` | sqlx: postgres→sqlite，移除 redis |
| 1.2 | 重构配置结构体 | `backend/src/config/mod.rs` | 移除 RedisConfig，DatabaseConfig→SQLite 路径，新增 StorageConfig |
| 1.3 | 更新配置文件 | `backend/config.toml`、`backend/.env.example` | 移除 PostgreSQL/Redis 配置，新增 SQLite/Storage 配置 |
| 1.4 | 创建 Storage 模块骨架 | `backend/src/storage/mod.rs` | 模块入口，导出 database、repository、file_storage |
| 1.5 | 实现 SQLite 连接 + Schema 迁移 | `backend/src/storage/database.rs` | 连接池初始化 + CREATE TABLE IF NOT EXISTS |
| 1.6 | 实现 FileStorage 基础设施 | `backend/src/storage/file_storage.rs` | 目录初始化 + 基础文件操作 |
| 1.7 | 注册模块 | `backend/src/lib.rs` | 新增 `pub mod storage;` |
| 1.8 | 编译验证 | 全项目 | `cargo build` 确保编译通过 |

### Phase 2: Repository 层实现（预计 2-3 小时）

| # | 任务 | 文件 | 说明 |
|---|------|------|------|
| 2.1 | 实现 Sensor CRUD | `backend/src/storage/repository.rs` | insert/list/get/update_status/delete |
| 2.2 | 实现 Target CRUD | `backend/src/storage/repository.rs` | upsert/get_active/get_history/append_track_point |
| 2.3 | 实现 Mission CRUD | `backend/src/storage/repository.rs` | insert/list/get/update_status |
| 2.4 | 实现 Alert CRUD | `backend/src/storage/repository.rs` | insert/list |
| 2.5 | 编写单元测试 | `backend/src/storage/repository.rs` | 每个方法一个测试用例 |

### Phase 3: 模块适配改造（预计 3-4 小时）

| # | 任务 | 文件 | 说明 |
|---|------|------|------|
| 3.1 | main.rs 初始化逻辑 | `backend/src/main.rs` | 初始化 SQLite + FileStorage，注入 AppState |
| 3.2 | Sensor API 适配 | `backend/src/api/sensor.rs` | list/get 改为从 Repository 读取 |
| 3.3 | Dashboard API 适配 | `backend/src/api/dashboard.rs` | targets/threats 从 Repository 补充历史数据 |
| 3.4 | Mission API 实现 | `backend/src/api/mission.rs` | 将 TODO 替换为真实 Repository 读写 |
| 3.5 | Tracker 持久化集成 | `backend/src/tracker/mod.rs` | 航迹变化时异步写入 SQLite track_points |
| 3.6 | Planner 持久化集成 | `backend/src/planner/mod.rs` | 任务创建时写入 SQLite missions |
| 3.7 | 告警持久化 | `backend/src/bus/mod.rs` | 告警发送时同步写入 SQLite alerts |
| 3.8 | 编译验证 + 集成测试 | 全项目 | `cargo build + cargo test` |

### Phase 4: 文件存储集成（预计 1-2 小时）

| # | 任务 | 文件 | 说明 |
|---|------|------|------|
| 4.1 | 传感器数据帧落盘 | `backend/src/storage/file_storage.rs` | append_sensor_frame 实现 |
| 4.2 | 数据导出 API | `backend/src/api/dashboard.rs` | 新增 GET /api/v1/export/targets 和 /missions |
| 4.3 | 配置文件快照 | `backend/src/api/dashboard.rs` | 配置变更时自动保存快照 |
| 4.4 | 自动清理后台任务 | `backend/src/main.rs` | tokio::spawn 定时清理过期文件 |

### Phase 5: 验证与清理（预计 1 小时）

| # | 任务 | 说明 |
|---|------|------|
| 5.1 | 全量编译 | `cargo build --release` 确保无警告 |
| 5.2 | 全量测试 | `cargo test` 全部通过 |
| 5.3 | 功能验证 | 启动服务，调用各 API 验证数据正确持久化 |
| 5.4 | Cargo.lock 更新 | 提交更新后的依赖锁定文件 |
| 5.5 | README 更新 | 移除 PostgreSQL/Redis 依赖说明，更新快速开始 |

---

## 四、风险评估

| 风险 | 等级 | 缓解措施 |
|------|------|---------|
| sqlx 0.8 SQLite feature 与现有 postgres feature 编译冲突 | 🟢 低 | 一次只保留一个 feature，已验证 sqlx 0.8 同时支持两者 |
| SQLite 并发写入瓶颈 | 🟢 低 | WAL 模式 + 写操作通过 tokio::spawn 异步化，实际写入量小（传感器 ≤10Hz，目标 ≤30 个） |
| 数据库文件损坏 | 🟡 中 | 定期 config 快照备份 + 告警/任务记录可重建 |
| 文件系统空间不足 | 🟡 中 | cleanup_old_files 定时任务 + 启动时磁盘空间检查 + 可配置保留天数 |
| 迁移期间 API 不兼容 | 🟢 低 | API 接口签名不变，仅底层实现切换 |
| Redis 移除影响 | 🟢 **零风险** | 代码中完全未使用 Redis |

---

## 五、验收标准

### 5.1 编译与测试
- [ ] `cargo build` 编译成功，无错误
- [ ] `cargo build --release` 编译成功
- [ ] `cargo test` 全部测试通过
- [ ] 无 `redis` 或 `postgres` 相关依赖残留

### 5.2 数据库功能
- [ ] 启动时自动创建 `./data/hsimc2.db` 及所有表
- [ ] 传感器注册/查询/删除 → SQLite 读写正确
- [ ] 目标航迹持久化 → track_points 表数据正确
- [ ] 任务创建/查询/状态更新 → missions 表数据正确
- [ ] 告警记录写入 → alerts 表数据正确

### 5.3 文件存储功能
- [ ] `./data/` 下所有子目录自动创建
- [ ] 传感器数据帧以 JSONL 格式写入 `./data/sensors/{type}/{sensor_id}/`
- [ ] EO/IR 图像保存功能正常
- [ ] 数据导出 API 返回正确的 CSV/JSON 文件

### 5.4 运行时验证
- [ ] 服务启动无 panic / 无连接错误
- [ ] `GET /api/v1/health` 返回正常
- [ ] `GET /api/v1/sensors` 返回持久化的传感器列表
- [ ] `GET /api/v1/situation/targets` 返回目标数据（含历史）
- [ ] `POST /api/v1/missions` 创建任务并持久化
- [ ] WebSocket 实时推送正常工作
- [ ] 仿真引擎正常运行（无需数据库）

### 5.5 原有功能不受影响
- [ ] 实时态势推送（WebSocket）正常
- [ ] 传感器仿真引擎正常生成数据
- [ ] 融合/跟踪/威胁评估/任务规划算法正常
- [ ] 前端页面正常加载和交互

---

## 六、待确认事项

> ⚠️ **请在下述事项上确认后，我立即开始开发。**

1. **SQLite 数据库路径**: 默认 `./data/hsimc2.db`，是否可接受？
2. **数据保留策略**: 传感器原始数据默认保留 30 天，过期自动清理。是否需要调整？
3. **Repository 层粒度**: 是否需要为每个核心模块（tracker/planner/sensors）单独拆分子 Repository，还是统一一个 Repository 即可？（建议先统一，后续按需拆分）
4. **hot-reload 配置**: 数据库/存储路径是否支持运行时热重载？（建议不支持，仅启动时读取）
5. **前端**: 本次改造不通前端，前端是否需要同步调整？（当前分析结论是不需要）

---

> 📌 **确认上述方案后，回复"确认"或提出修改意见，我将按照 Phase 1→5 顺序立即开始编码实现。**
