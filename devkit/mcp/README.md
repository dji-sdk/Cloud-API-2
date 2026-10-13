# DJI Cloud API 2.0 MCP Server

**DJI FlightHub PaaS SDK 的 MCP（Model Context Protocol）封装**  
让 AI Agent 通过自然语言直接操控无人机与机场，无需手写一行 SDK 代码。

---

## 目录

- [这是什么](#这是什么)
- [前置条件](#前置条件)
- [快速开始](#快速开始一键接入)
- [调用流程](#调用流程)
- [工具目录（51 个）](#工具目录51-个)
- [安全机制](#安全机制)
- [配置参考](#配置参考)
- [部署方案](#部署方案)
- [开发指南](#开发指南)
- [已知限制](#已知限制)
- [获取支持](#获取支持)
- [License](#license)
- [相关文档](#相关文档)

---

## 这是什么

FlightHub MCP Server 是一个符合 [MCP 规范（2025-06-18）](https://modelcontextprotocol.io/) 的 Java 服务，通过 **JSON-RPC over SSE** 将 DJI FlightHub PaaS SDK 的能力暴露给任何 MCP 兼容的 AI 工具（Claude Code、Cursor、Claude Desktop 等）。

```
┌─────────────────────┐         JSON-RPC          ┌─────────────────────────┐
│   AI Agent / LLM    │ ◀────────────────────────▶ │  DJI Cloud API 2.0      │
│  (Claude / Cursor)  │      SSE transport         │  MCP Server (Java 17)   │
└─────────────────────┘                            └────────────┬────────────┘
                                                                │  JNI / SDK
                                                   ┌────────────▼────────────┐
                                                   │  FlightHub PaaS SDK     │
                                                   │  (Linux x86_64 native)  │
                                                   └────────────┬────────────┘
                                                                │  HTTPS/WebSocket
                                                   ┌────────────▼────────────┐
                                                   │  FlightHub 云端平台      │
                                                   │  (工作空间 / 设备 / 任务) │
                                                   └────────────┬────────────┘
                                                                │  4G/5G/RC
                                                   ┌────────────▼────────────┐
                                                   │  无人机 + 机场 (Dock)    │
                                                   └─────────────────────────┘
```

**典型场景**

- AI 辅助巡检调度：用自然语言描述巡检路线，Agent 自动生成航线并执行飞行任务
- 无人机状态监控：AI 持续查询设备遥测，异常时自动触发告警或返航
- 开发与调试加速：开发者在 IDE 中直接对话 FlightHub，无需切换控制台

---

## 前置条件

| 条件 | 说明 | 获取方式 |
|------|------|---------|
| **DJI 开发者账户** | 注册 DJI 开发者平台 | [developer.dji.com](https://developer.dji.com) |
| **FlightHub PaaS 接入权限** | 企业 API 权限 + 工作空间 | 联系 DJI 企业技术支持申请 |
| **工作空间凭证** | 服务地址 / 访问令牌 / 工作空间 UUID | 登录 FlightHub 管理控制台获取 |

> MCP Server 由平台侧统一部署，用户无需本地编译或运行任何进程。

---

## 快速开始（一键接入）

MCP Server 已由平台预部署，用户只需配置 AI 工具连接 SSE 端点即可。

### Claude Code

```bash
claude mcp add --transport sse --scope user \
  --header "Authorization: Bearer <your-token>" \
  dji-cloud-api2 \
  https://<mcp-server-host>/pf-devkit/ai-plugin/mcp/sse
```

执行后重启 Claude Code，在对话中输入「连接 FlightHub」验证。

### Cursor

编辑项目根目录 `.cursor/mcp.json`：

```json
{
  "mcpServers": {
    "dji-cloud-api2": {
      "url": "https://<mcp-server-host>/pf-devkit/ai-plugin/mcp/sse",
      "headers": {
        "Authorization": "Bearer <your-token>"
      }
    }
  }
}
```

### Windsurf

编辑 `~/.codeium/windsurf/mcp_config.json`，格式同上。

### VS Code (Copilot)

编辑项目根目录 `.vscode/mcp.json`，格式同上。

### 参数说明

| 参数 | 含义 |
|------|------|
| `dji-cloud-api2` | MCP Server 注册名称 |
| `https://<mcp-server-host>/sse` | 平台分配的 SSE 端点 URL |
| `Authorization: Bearer <token>` | 鉴权令牌（从 FlightHub 控制台获取） |

### 验证

配置完成后在 AI 工具中输入：

```
连接 FlightHub
```

AI 应调用 `fh_connect` 并返回 session 信息，确认连接成功。

---

## 调用流程

所有业务操作前必须先调用 `fh_connect` 建立会话。DRC 实时指挥需要按以下顺序建立前置条件：

```
fh_connect                           ← 必须第一步
    │
    ├── fh_device_list               ← 查询设备清单
    │       ├── fh_drone_snapshot    ← 无人机状态
    │       └── fh_dock_snapshot     ← 机场状态
    │
    ├── [航线任务]
    │       ├── fh_wayline_upload    ← 上传航线 (L1)
    │       ├── fh_wayline_execute   ← 执行飞行 (L2)
    │       └── fh_wayline_control   ← 暂停/恢复 (L2)
    │
    └── [DRC 实时指挥]
            ├── fh_control_acquire   ← 抢控制权 (L1) ─────────────────────────┐
            │                                                                   │
            ├── fh_drc_connect       ← 建 DRC 链路 (L1) ──────────────────┐   │
            │                                                               │   │
            ├── fh_takeoff           ← 起飞 (L2)                          │   │
            ├── fh_flyto             ← 指点飞行 (L2)                      │   │
            ├── fh_poi               ← 环绕飞行 (L2)                      │   │
            ├── fh_return_home       ← 返航 (L2)                         │   │
            ├── fh_emergency_stop    ← 急停 (L3)                         │   │
            │                                                               │   │
            ├── fh_drc_disconnect    ← 断 DRC 链路 ◀──────────────────────┘   │
            └── fh_control_release   ← 释放控制权 ◀──────────────────────────┘
```

> **重要**：无论操作成功还是失败，飞行结束后都必须调用 `fh_control_release` 释放控制权。

---

## 工具目录（51 个）

> 此处为工具速查清单（安全等级 / 功能概述）。参数说明、返回值结构和调用示例见 → **[MCP 工具使用手册](../docs/mcp-tools-guide.md)**
> - **L0** 只读操作，无需确认
> - **L1** 可逆操作，需要 `confirm=true` 或人工弹窗确认
> - **L2** 飞行操作，需要飞行控制权 + DRC 链路 + 确认
> - **L3** 高危不可逆操作，需要强制审计原因 + 确认

### 接入与会话（3 个）

| 工具 | 说明 | 安全等级 |
|------|------|----------|
| `fh_connect` | 初始化 SDK 并建立工作空间会话（必须第一步调用） | L0 |
| `fh_session_info` | 查询当前会话状态：连接时长、设备绑定、DRC 链路、飞行阶段 | L0 |
| `fh_ops` | 运维操作：健康检查 / 调整日志级别 / 优雅关闭 | L0 |

### 设备管理（5 个）

| 工具 | 说明 | 安全等级 |
|------|------|----------|
| `fh_device_list` | 查询工作空间内所有设备，支持按类型、在线状态过滤 | L0 |
| `fh_drone_snapshot` | 获取无人机实时快照：位置 / 电量 / 姿态 / 速度 / 飞行模式 | L0 |
| `fh_dock_snapshot` | 获取机场实时快照：位置 / 舱盖状态 / 电源 / 存储 / 无人机在仓 | L0 |
| `fh_device_manage` | 设备详情 / 重命名 / 解绑 / HMS 健康告警查询 | L0~L1 |
| `fh_dock_control` | 控制机场：开/关舱盖、启/停充电、开/关补光灯 | L1 |

### 航线任务（4 个）

| 工具 | 说明 | 安全等级 |
|------|------|----------|
| `fh_wayline_upload` | 创建航线任务（上传 KMZ 或 KML 航线文件到云端） | L1 |
| `fh_wayline_execute` | 执行已创建的航线任务，触发设备起飞和执行 | L2 |
| `fh_wayline_control` | 控制执行中的航线任务：暂停 / 恢复 / 取消 | L2 |
| `fh_wayline_status` | 查询航线任务详情或任务列表（支持状态过滤） | L0 |

### 实时指挥 DRC（13 个）

| 工具 | 说明 | 安全等级 |
|------|------|----------|
| `fh_control_acquire` | 获取飞行控制权（全局互斥资源，同时只能一个 session 持有） | L1 |
| `fh_control_release` | 释放飞行控制权（完成操作后必须调用，即使出错） | L1 |
| `fh_drc_connect` | 建立 DRC 低延迟控制链路（飞行指令前置条件） | L1 |
| `fh_drc_disconnect` | 断开 DRC 链路 | L1 |
| `fh_takeoff` | 一键起飞至指定高度（默认失控动作：return_home） | L2 |
| `fh_return_home` | 返航 / 取消返航 | L2 |
| `fh_flyto` | 指点飞行：start / stop / update（支持飞行中更新目标坐标） | L2 |
| `fh_poi` | 环绕飞行：围绕兴趣点以指定半径/速度/方向持续环绕 | L2 |
| `fh_stick_move` | 受限摇杆控制：方向 + 时长 + 速度百分比（内部 50ms 循环） | L2 |
| `fh_emergency_stop` | 紧急停止（不可逆，需要强制提供 reason 用于审计） | L3 |
| `fh_payload` | 负载控制：相机拍照/录像/变焦、云台旋转/指向、喊话器 TTS/播报、探照灯 | L1 |
| `fh_live_start` | 直播推流控制：开始 / 停止 / 画质切换（支持无人机和机场） | L1 |
| `fh_osd_snapshot` | OSD 实时遥测快照：位置 / 姿态 / 电量 / GPS 信号 / 风速（比 drone_snapshot 更丰富） | L0 |

### 媒体管理（4 个）

| 工具 | 说明 | 安全等级 |
|------|------|----------|
| `fh_media_list` | 查询媒体文件列表（照片/视频），支持按设备/类型/时间/飞行 ID 过滤 | L0 |
| `fh_media_download` | 获取媒体文件预签名下载 URL（临时有效 1~24 小时） | L0 |
| `fh_media_delete` | 批量删除媒体文件（不可逆，支持 1~100 个文件 ID） | L1 |
| `fh_media_record` | 直播录制控制：开始录制 / 停止录制 / 列出录制记录 | L1 |

### 任务管理（1 个）

| 工具 | 说明 | 安全等级 |
|------|------|----------|
| `fh_flight_task` | 飞行任务 CRUD：list / detail / create / edit / delete / execute / update_status | L0~L2 |

### 固件管理（1 个）

| 工具 | 说明 | 安全等级 |
|------|------|----------|
| `fh_firmware` | 固件全生命周期：检查状态 / 列表 / 查询进度 / 发布说明 / 兼容性 / 升级 / 上传 / 提交 / 发布 / 撤销 / 删除 | L0~L2 |

### 空域安全（4 个）

| 工具 | 说明 | 安全等级 |
|------|------|----------|
| `fh_device_recommend` | 根据飞行任务需求推荐适合的设备类型 | L0 |
| `fh_tcas` | TCAS 空中防撞系统：查询感知列表 / 设置告警阈值 | L0~L1 |
| `fh_unlock_license` | 空域解锁许可证：列表 / 详情 / 导入 / 同步 / 删除 | L0~L1 |
| `fh_flysafe` | FlySafe 飞行安全：查询禁飞区 / 限飞区 / 授权区域信息 | L0 |

### 自定义飞行区（3 个）

| 工具 | 说明 | 安全等级 |
|------|------|----------|
| `fh_flight_area_list` | 查询自定义飞行区列表（禁飞区、限高区等） | L0 |
| `fh_flight_area_manage` | 创建 / 更新 / 删除自定义飞行区 | L1 |
| `fh_flight_area_sync` | 同步自定义飞行区到设备端 | L1 |

### 相机与负载（4 个）

| 工具 | 说明 | 安全等级 |
|------|------|----------|
| `fh_camera` | 相机高级控制：单张/AEB 拍照、录像、变焦、红外、存储格式、AE 锁定、状态查询 | L0~L1 |
| `fh_speaker` | PSDK 喊话器专用：音频播放 / TTS / 实时喊话 / 音量 / 模式 / TTS 配置 | L1 |
| `fh_searchlight` | 探照灯高级控制：模式 / 校准 / 微调 / 云台 / 类型 / 广域模式 | L1 |
| `fh_psdk_widget` | PSDK 自定义 Widget：设置控件值 / 输入文本 | L0 |

### 设备健康与诊断（3 个）

| 工具 | 说明 | 安全等级 |
|------|------|----------|
| `fh_hms` | HMS 健康管理系统：查询当前告警 / 历史告警 / 告警订阅 | L0 |
| `fh_diagnosis` | 设备诊断：创建诊断报告 / 查询报告 / 列表 / 操作 | L0~L1 |
| `fh_device_debug` | 设备调试（机场运维）：开关舱盖、无人机上下电、重启、格式化、限高/限距、障碍物规避、网络测试 | L0~L1 |

### 运维工具（5 个）

| 工具 | 说明 | 安全等级 |
|------|------|----------|
| `fh_log` | 日志管理：查询 / 创建上报 / 列表 / 操作 / 上传（delete 需确认） | L0~L1 |
| `fh_maintenance` | 维修工单：获取配置 / 创建工单 / 查询详情 / 列表 | L0 |
| `fh_offline_map` | 离线地图：模型信息 / 更新 / 取消 / 启停 / 同步 / 同步状态 / FlySafe 升级 | L0~L1 |
| `fh_flight_record` | 飞行记录：列表 / 详情 / 统计 | L0 |
| `fh_workspace` | 工作空间：列表 / 详情 / 提交 / 更新 / 复制 / 批量删除 / 批量下载 | L0~L1 |

### 三维重建（1 个）

| 工具 | 说明 | 安全等级 |
|------|------|----------|
| `fh_terra` | DJI Terra 三维重建：任务管理、点云/模型生成、进度查询 | L0~L1 |

---

## 安全机制

FlightHub MCP Server 实现了多层安全防护，防止 AI Agent 执行未经授权或危险的飞行操作。

### 四级安全模型

```
L0 READ_ONLY   — 只读查询，任何时候可直接执行
L1 REVERSIBLE  — 可逆操作，需要 confirm=true 或人工弹窗
L2 FLIGHT      — 飞行操作，需要控制权 + DRC 链路 + 确认
L3 CRITICAL    — 紧急停止，需要强制 reason + 运营商二次鉴权（FH_OPERATOR_TOKEN）+ 确认（不可逆）
```

每个 session 在 `fh_connect` 时设置 `max_security_level`，超出该等级的工具调用会被拒绝执行。

> **L3 二次鉴权说明**：`fh_emergency_stop` 除 session 凭据外，还必须提供
> `operator_token`（对应环境变量 `FH_OPERATOR_TOKEN`）。该 token 由真人带外提供，AI 不可代填，
> 且**未配置时 L3 一律拒绝执行**（宁可不可用，不可裸奔）。启用 L3 的完整前置条件：
> 1. 设置 `FH_MAX_SECURITY_LEVEL=3`；
> 2. 通过 `openssl rand -hex 32` 生成并注入 `FH_OPERATOR_TOKEN`；
> 3. 调用 `fh_emergency_stop` 时由真人提供 `operator_token` 参数。

### 人工确认（MCP Elicitation）

支持 MCP Elicitation 协议的客户端（如 Claude Code）在执行 L1+ 操作时，Server 会**主动向用户弹窗确认**，而不依赖 AI 自动传入 `confirm=true`：

```
AI 调用 fh_takeoff →
  Server 检测到 L2 操作 →
    通过 MCP elicitation 向用户弹窗："⚠️ 无人机 DRONE-001 即将起飞至 50.0 米高度，是否确认？" →
      用户点击确认 → 执行起飞
      用户拒绝/超时 → 返回拒绝响应，操作不执行
```

不支持 Elicitation 的客户端（如无弹窗能力的 HTTP 调用方）可通过 `confirm=true` 参数作为回退路径。
注意：`confirm=true` 仅适用于 L1/L2；L3 紧急停止额外要求 `operator_token`（见上），不受该回退路径豁免。

### 传输层保护

- **SSE 模式**：支持 Bearer Token 鉴权（`FH_SSE_AUTH_TOKEN`），L1+ 操作在无 Elicitation 通道时强制拦截

### dry_run 模式

所有 L1+ 工具支持 `dry_run=true` 参数：参数校验和前置条件检查照常执行，但跳过实际 SDK 调用，返回"如果真正执行会做什么"的预览。

---

## ⚠️ 安全声明（Demo 项目已知风险）

本仓库为 **Demo 演示项目**，用于展示 FlightHub PaaS SDK 的 AI Agent 接入能力，
**不得直接用于生产环境**。

> This repository is a **DEMO project**. Do NOT deploy directly to production.

### MCP 飞控安全控制已知风险

以下 MCP 工具存在已知安全控制缺失（2026-08-21 审查确认，随开源发布以声明方式处理）。
ISV 在生产环境使用前**必须自行补充对应安全措施**；未处理时任意外泄都会构成风险：

| 工具 | 已知风险 | 生产环境需补充 |
|------|---------|--------------|
| `fh_ops` shutdown | `System.exit(0)` 无人类确认 | 加 CRITICAL 级别确认 |
| `fh_control_acquire` | `requiresHumanConfirmation=false`，AI 可静默抢夺飞控权 | 改为 true |
| `fh_drc_connect` | `requiresHumanConfirmation=false`，AI 可静默建立 DRC 实时控制链 | 改为 true |
| `fh_firmware` upgrade/upload/submit/release/unrelease | `SecurityLevel=REVERSIBLE` | 升级至 FLIGHT |
| `fh_log` upload | 无路径白名单校验 | 加路径校验 |
| `fh_flight_area_manage` delete | `SecurityLevel=REVERSIBLE`（不可逆操作） | 升级至 FLIGHT |
| `fh_offline_map` sync | 无 `enforceSecurityLevel` 拦截 | 加安全拦截 |
| `fh_flight_task` execute | `SecurityLevel=REVERSIBLE` | 升级至 FLIGHT |

> **上述问题未处理时，AI Agent 可能在无人类确认的情况下执行危险飞控操作。**

---

## 配置参考

所有配置通过环境变量注入，支持 `.env` 文件（使用 `docker run --env-file .env` 加载）。

| 变量 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| `FH_SERVER_ADDRESS` | **是** | — | FlightHub 服务器地址（如 `https://your-server.com`） |
| `FH_SERVER_PORT` | 否 | `443` | 服务器端口 |
| `FH_ACCESS_TOKEN` | 是* | — | 工作空间访问令牌（真实模式必填，须在服务启动前通过环境变量预设） |
| `FH_WORKSPACE_UUID` | 是* | — | 工作空间绑定 UUID（真实模式必填，须在服务启动前通过环境变量预设） |
| `FH_MOCK` | 否 | `false` | 设为 `true` 启用 Mock 模式（不调用真实 SDK） |
| `FH_TRANSPORT` | 否 | `sse` | 传输模式：`sse` |
| `FH_SSE_PORT` | 否 | `8080` | SSE 模式监听端口 |
| `FH_MAX_SECURITY_LEVEL` | 否 | `2` | 全局最大安全等级上限（0~3），需要 L3 紧急停机时显式设为 `3` |
| `FH_OPERATOR_TOKEN` | 否* | — | L3 操作（紧急停止）所需的运营商二次鉴权 Token。启用 L3 时<b>必填</b>（缺失则 L3 拒绝执行），由真人带外提供，AI 不可代填 |
| `FH_SSE_AUTH_TOKEN` | 是* | — | SSE 模式 Bearer Token。SSE profile 下<b>必填</b>（缺失或弱占位值将拒绝启动），请用 `openssl rand -hex 32` 生成 |

> *`FH_ACCESS_TOKEN` 和 `FH_WORKSPACE_UUID` 在真实模式下必填，须在服务启动前通过环境变量配置；Mock 模式（`FH_MOCK=true`）下可不填。**这两个凭证不支持通过 `fh_connect` 参数运行时传入。**

复制 `.env.example` 并按需填写各配置项：

```bash
cp .env.example .env
```

---

## 部署方案

MCP Server 由平台侧统一部署（Docker + SSE），用户无需关注部署细节。

| 场景 | 方式 |
|------|------|
| **生产环境** | Docker 容器 + SSE 模式，参考 `docker-compose.yml` |
| **测试环境** | 同上，启用 `FH_MOCK=true` 返回模拟数据 |

> 容器规格、docker-compose 配置、健康检查命令和 SDK 原生库依赖清单详见 → **[Docker 部署指南](docs/docker-deployment.md)**

> **注意**：`.gitlab-ci.yml` 中的 `privatization-build-wibu-*` stage 为 DJI 内部私有化构建专用（依赖内部 Harbor 镜像和 Wibu AxProtector 工具链），外部 ISV fork 后**无法使用且可安全跳过**，不影响标准构建和部署流程。

---

## 开发指南

### 项目结构

```
mcp/
├── src/main/java/com/dji/flighthub/mcp/
│   ├── annotation/        # @McpTool / @McpParam 注解驱动自动注册
│   ├── audit/             # 操作审计日志（AuditService）
│   ├── config/            # Spring Boot 配置（Properties / Registry）
│   ├── elicit/            # MCP Elicitation 人工确认通道
│   ├── idempotency/       # 幂等缓存（IdempotencyCache）
│   ├── prompts/           # MCP Prompts 注册（预置场景 Prompt）
│   ├── sdk/               # SDK 服务层（SdkService / SdkCallbackBridge）
│   ├── security/          # 安全等级（SecurityLevel）
│   ├── session/           # 会话管理（SessionContext / SessionManager）
│   ├── tools/             # 51 个 MCP Tool 实现（按业务模块分包）
│   │   ├── AbstractMcpTool.java   # 基类：参数解析、错误响应、确认门控
│   │   ├── connect/       # fh_connect, fh_session_info, fh_ops
│   │   ├── device/        # fh_device_list, fh_drone/dock_snapshot, 等
│   │   ├── drc/           # fh_takeoff, fh_flyto, fh_emergency_stop, 等
│   │   └── ...            # 其他模块
│   └── transport/         # SseTransport
├── src/test/              # 81 个测试类（100% 通过）
├── Dockerfile             # 多阶段构建（Maven build + JRE runtime）
├── docker-compose.yml     # 生产部署示例
└── run-mcp-sse.sh         # 本地 SSE 启动脚本（含 JVM 内存限制：-Xmx512m，读取 .env）
```

### 新增 Tool 的标准流程

1. 在对应业务包下创建 `FhXxxTool.java`，继承 `AbstractMcpTool`，标注 `@Component`
2. 用 `@McpTool` + `@McpParam` 注解声明工具元数据和参数，`SchemaGenerator` 自动生成 JSON Schema
3. 覆写 `doExecute(ToolContext ctx)`：先检查 session，再校验参数，再调用 SDK
4. 编写测试类，覆盖：无 session / 参数缺失 / Mock 模式正常返回
5. 编译（`mvn clean package -DskipTests`）+ 全量测试（`mvn test`）

> Spring 会自动扫描并注册所有 `AbstractMcpTool` 实现 Bean，无需手动编辑注册表。完整的 9 步流程（含 Checkstyle 检查、STATUS.md 更新、MR 规范）详见 → **[工具版本与更新指南](docs/tool-versioning.md)**

```java
@Component
@McpTool(
    name = "fh_example",
    description = "Example: get drone status.",
    securityLevel = SecurityLevel.READ_ONLY,
    prerequisites = { Prerequisite.SESSION }
)
public class FhExampleTool extends AbstractMcpTool {

    @McpParam(description = "Target drone serial number", required = true)
    private String drone_sn;

    public FhExampleTool(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    protected Map<String, Object> doExecute(ToolContext ctx) {
        String sn = ctx.getString("drone_sn");
        return successResult(Map.of("sn", sn), "OK");
    }
}
```

### 运行测试

```bash
# 运行全部单测（不依赖外部服务，全 Mock）
mvn test

# 运行指定测试类
mvn test -Dtest=FhTakeoffToolTest

# 查看覆盖率报告（需先在 pom.xml 中配置 jacoco-maven-plugin）
# mvn test jacoco:report
```

### 本地开发运行

开发者如需本地调试 MCP Server 源码：

```bash
# 1. 安装私有 SDK JAR（仅首次）
mvn install:install-file \
  -Dfile=libs/flight-hub-sdk-3.3.7.jar \
  -DgroupId=com.dji.flighthubsdk \
  -DartifactId=flight-hub-sdk \
  -Dversion=3.3.7 \
  -Dpackaging=jar

# 2. 编译
mvn clean package -DskipTests

# 3. 本地 SSE 模式启动（需先 cp .env.example .env 并填写）
./run-mcp-sse.sh
```

SSE 端点默认：`http://localhost:8080/pf-devkit/ai-plugin/mcp/sse`

---

## 已知限制

| 限制 | 说明 |
|------|------|
| **Linux x86_64 专属** | FlightHub SDK JNI 原生库仅提供 Linux x86_64 版本，服务端必须在对应平台运行（Docker 解决）。Mock 模式无此限制。 |
| **单 workspace** | 当前一个 MCP Server 实例对应一个工作空间。多 workspace 场景需要运行多个实例。 |
| **单活跃 session** | SessionManager 当前只维护一个活跃 session，多客户端并发连接会共享同一个 SDK 实例。 |
| **待开发 SDK 模块** | 以下 SDK 模块尚未覆盖：组织管理（OrgModule）、地图元素（MapElementModule）、通知告警、用户管理、AI 识别（AiModule）等（参见 docs/STATUS.md）。 |

---

## 获取支持

| 渠道 | 说明 |
|------|------|
| **GitHub Issues** | 报告 Bug 或提交功能建议 |
| **DJI 开发者论坛** | [sdk-forum.dji.com](https://sdk-forum.dji.com) — SDK 技术问题 |
| **DJI 企业技术支持** | FlightHub PaaS 接入权限、SDK 授权、账户问题，请联系 DJI 商务 |

> 提交 Issue 时请附上：MCP Server 版本、错误日志（如有）、复现步骤。

---

## License

MIT

---

## 相关文档

| 文档 | 说明 |
|------|------|
| **[MCP 工具使用手册](../docs/mcp-tools-guide.md)** | 每个工具的参数、返回值结构、调用示例和注意事项 |
| **[工具版本与更新指南](docs/tool-versioning.md)** | 当前版本工具清单、版本机制说明、新增/修改工具的完整流程 |
| **[开发状态跟踪](docs/STATUS.md)** | 各里程碑完成情况、已知缺口、MR 历史 |
| **[Docker 部署指南](docs/docker-deployment.md)** | 生产环境部署、健康检查、日志配置 |
