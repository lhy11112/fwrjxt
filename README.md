# HSimC2 — 反无人机一体化指挥控制平台

> **H**igh-performance **Sim**ulation **C**ounter-UAV **C**ommand & **C**ontrol Platform

HSimC2 是一个用 Rust + Vue 3 构建的反无人机一体化指挥控制平台（C-UAS C2 Platform），对标 Anduril Lattice、NATO C2 等系统架构，实现了完整的 **感知—融合—决策—打击** 闭环。

## 架构概览

```
┌─────────────────────────────────────────────────────────┐
│  E. 可视化指挥层 (Vue 3 + Leaflet/CesiumJS)               │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │ GIS 态势  │ │ 目标列表  │ │ 任务规划  │ │ 对话交互  │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
├─────────────────────────────────────────────────────────┤
│  D. 决策规划层 (Rust — Planner + Threat Assessor)         │
│  ┌────────────────┐ ┌────────────────────────────────┐   │
│  │ 威胁态势估计模块   │ │ 反无任务规划模块                   │   │
│  └────────────────┘ └────────────────────────────────┘   │
├─────────────────────────────────────────────────────────┤
│  C. 融合感知层 (Rust — Fusion Engine + Tracker)           │
│  ┌──────────────┐ ┌────────────┐ ┌──────────────────┐   │
│  │ 多源数据融合    │ │ 多目标跟踪   │ │ 协同态势生成       │   │
│  └──────────────┘ └────────────┘ └──────────────────┘   │
├─────────────────────────────────────────────────────────┤
│  B. 数据接入层 (Rust — Sensor SDK + Pipeline)            │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌────────┐ ┌────────────┐ │
│  │ 雷达  │ │  RF  │ │ EO/IR│ │ 声学    │ │ 数据预处理   │ │
│  └──────┘ └──────┘ └──────┘ └────────┘ └────────────┘ │
├─────────────────────────────────────────────────────────┤
│  A. 综合管理层 (Rust — Bus + Config + Metrics)           │
└─────────────────────────────────────────────────────────┘
```

## 技术栈

| 层次 | 技术选型 | 状态 |
|------|---------|------|
| **后端核心** | Rust (Axum, Tokio, Serde) | ✅ 编译通过 |
| **数据库** | SQLite (sqlx) | ✅ |
| **文件存储** | 本地文件系统 (JSONL/CSV/JSON) | ✅ |
| **消息总线** | Tokio broadcast + mpsc | ✅ |
| **传感器接入** | 标准化 gRPC/Protobuf 抽象层 | ✅ |
| **多传感器融合** | EKF 卡尔曼滤波 + JDL 融合模型 | ✅ |
| **多目标跟踪** | JPDA + M/N 逻辑 | ✅ |
| **威胁评估** | 加权评分 + 意图识别 | ✅ |
| **任务规划** | 单目标/多目标 RRT* + 冲突消解 | ✅ |
| **前端** | Vue 3 + TypeScript + Pinia + Leaflet | ✅ 构建通过 |
| **外部接口** | REST + WebSocket + gRPC | ✅ |

## 项目结构

```
HSimC2/
├── Cargo.toml              # 工作空间
├── backend/                # Rust 后端
│   ├── src/
│   │   ├── main.rs         # 入口 + Axum HTTP 服务
│   │   ├── api/            # REST API + WebSocket
│   │   ├── bus/            # 消息总线 (broadcast/mpsc)
│   │   ├── config/         # 配置管理
│   │   ├── core/           # 核心引擎 + 数据管线
│   │   ├── fusion/         # 多源融合 (EKF)
│   │   ├── models/         # 数据模型
│   │   ├── planner/        # 任务规划
│   │   ├── sensors/        # 传感器抽象层
│   │   ├── simulator/      # 传感器仿真引擎
│   │   ├── storage/        # 持久化层 (SQLite + 文件存储)
│   │   ├── threat/         # 威胁态势估计
│   │   └── tracker/        # 多目标跟踪
│   └── config.toml         # 默认配置
├── frontend/               # Vue 3 前端
│   ├── src/
│   │   ├── App.vue         # 主布局 + WebSocket
│   │   ├── api/            # API 客户端
│   │   ├── stores/         # Pinia 状态管理
│   │   ├── views/          # 页面视图
│   │   ├── components/     # 可复用组件
│   │   └── types/          # TypeScript 类型
│   └── vite.config.ts
├── sdk/                    # 传感器 SDK
│   └── src/                # Rust SDK + 客户端库
└── proto/                  # Protobuf 消息定义
```

## 快速开始

### 依赖

- **Rust** 1.75+ (安装: `curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh`)
- **Node.js** 18+ (推荐 20 LTS)
- **SQLite** 3.x (嵌入式数据库，无需单独安装)

### 启动后端

```bash
# 编译并运行
cargo run --package hsimc2-backend

# 服务启动于 http://localhost:8080
# API 文档: GET /api/v1/health
```

### 启动前端

```bash
cd frontend
npm install
npm run dev

# 开发服务器启动于 http://localhost:3000
# 自动代理 /api -> localhost:8080
```

### 构建生产（单文件可执行程序）

```bash
# 1. 构建前端静态文件
cd frontend
npm install
npm run build        # 输出到 frontend/dist/

# 2. 构建后端（自动嵌入前端 dist）
cd ..
cargo build --release --package hsimc2-backend

# 3. 运行（单个 exe 文件包含前后端全部功能）
./target/release/hsimc2-server

# 服务启动于 http://localhost:8080
# 前端页面直接访问 http://localhost:8080
# API 接口: http://localhost:8080/api/v1/health
# WebSocket: ws://localhost:8080/api/v1/ws
```

> **单文件部署**: 前端 `dist/` 在 Rust 编译时通过 `rust-embed` 嵌入二进制，分发只需一个 exe 文件 + 配置文件。

## API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/health` | 健康检查 |
| GET | `/api/v1/metrics` | 系统指标 |
| GET | `/api/v1/sensors` | 传感器列表 |
| GET | `/api/v1/sensors/:id` | 传感器详情 |
| GET | `/api/v1/situation/targets` | 所有目标 |
| GET | `/api/v1/situation/threats` | 威胁排序列表 |
| GET | `/api/v1/situation/history/:id` | 目标航迹历史 |
| POST | `/api/v1/missions` | 创建任务 |
| POST | `/api/v1/missions/plan` | 单目标拦截规划 |
| POST | `/api/v1/missions/plan-multi` | 多目标拦截规划 |
| POST | `/api/v1/missions/:id/cancel` | 取消任务 |
| GET | `/api/v1/resources` | 拦截器资源列表 |
| POST | `/api/v1/chat` | 对话式指挥 |
| GET | `/api/v1/export/targets` | 导出目标数据 (JSON/CSV) |
| GET | `/api/v1/export/missions` | 导出任务数据 (JSON) |
| WS | `/api/v1/ws` | 实时态势推送 |

## 参考系统

- **Anduril Lattice** — 开放 SDK + 边缘 mesh 网络 + AI 决策闭环
- **NATO C-UAS C2** — SAPIENT 标准 + 分层防御 + 分布式 C2
- **ATAK / FreeTAKServer** — CoT 协议 + 降级运行
- **JRC C-UAS Platform** — DTW 航迹融合 + 消息代理架构

## 许可证

本项目仅供学习和研究用途。
