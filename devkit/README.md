# dji-flighthub-devkit

> DJI FlightHub PaaS SDK — ISV AI 易接入一站式开发者套件

## 快速入口

```
你属于哪种场景？
├─ 我要快速 demo / 新项目起步       → 用骨架（skeleton/，计划中）
├─ 我有现有项目要加 DJI 能力        → 装 Skill + npx @dji/security-init
├─ 我要让 AI Agent 操控无人机       → 用 MCP Server（mcp/）
├─ 我只需要 AI 写代码时遵循规范     → 装 Skill（skills/）
└─ 我有完整工程体系，只要范式参考    → 装 Skill，护栏自配
```

## 仓库结构

| 目录 | 方向 | 状态 | 说明 | 文档 |
|---|---|---|---|---|
| `skills/` | 方向 2 | ✅ **可用** | AI Skill 集合（6 个 `cloudapi2-*` Skill）— 教 AI 正确生成 SDK 集成代码 | [总览](skills/README.md) |
| `mcp/` | 方向 1 | ✅ **可用** | MCP Server — 让 AI Agent 运行时操控无人机（50 个 Tool，13 个模块） | [快速开始](mcp/README.md) · [工具手册](docs/mcp-tools-guide.md) |
| `skeleton/` | 方向 2 | 🚧 计划中 | BFF（Spring Boot）+ Web 前端（React + Vite） | — |
| `fov-demo/` | 方向 2 | ✅ **可用** | ISV 培训 Demo — FHSDK Java 自研前后端的直播、OSD、SEI、地图与手动操控全栈参考实现 | [快速开始](fov-demo/README.md) · [实战案例](fov-demo/docs/case-study/ISV自研Demo实战案例.md) |
| `cli/` | — | 🚧 计划中 | `@dji/security-init` 安全护栏注入 CLI | — |
| `llms/` | 方向 3 | 🚧 计划中 | llms.txt / llms-full.txt 生成器 | — |
| `docs/` | — | — | 共享技术文档（设计 spec、实施计划等） | — |

---

## AI Skill（`skills/`）

**一句话**：安装后，AI 在你写 Cloud API 2.0 SDK 集成代码时自动加载对应知识，防止 NPE、资源泄漏、权限错误。

### 安装

```bash
# 方式 1：npx（推荐，自动检测 IDE）
npx skills add https://github.com/dji-sdk/dji-flighthub-devkit

# 方式 2：手动拷贝
cp -r skills/cloudapi2-sdk-core ~/.claude/skills/    # Claude Code
cp -r skills/cloudapi2-sdk-core .cursor/skills/      # Cursor
```

### 能力

| 能力 | 说明 |
|------|------|
| 核心规则引导 | 5 条硬约束（initialize 检查、errorCode、listen/cancel、飞控权、null 防御） |
| 错误码诊断 | 4 层错误结构（SDK → HTTP → 服务端 → 设备）自动定位 |
| 接入质量评估 | 🔴🟡🟠 三级风险检查清单 |
| 业务场景 Prompt | 设备列表 / OSD 监听 / 直播 / 航线 / 飞控 / 媒体 6 个标准化范本 |
| Java 代码模板 | SDK 适配器 / Service 层 / 回调桥接工具 3 套模板 |

### 触发方式

安装后**无需手动调用**，输入以下关键词时自动触发：
- "集成 FlightHub SDK" / "初始化 SDK" / "订阅无人机状态"
- "让无人机返航" / "getDroneInstance 返回 null"
- "检查 SDK 接入代码质量"

→ [完整文档](skills/README.md)

---

## MCP Server（`mcp/`）

**一句话**：让 AI Agent 在运行时直接操控无人机（设备查询、状态监听、飞行控制）。

```bash
cd mcp && mvn package -DskipTests
# 启动后 AI Agent 通过 JSON-RPC 调用 SDK 工具
```

→ [快速开始](mcp/README.md) · [工具使用手册](docs/mcp-tools-guide.md)

---

## Skill vs MCP Server

| 维度 | Skill | MCP Server |
|------|-------|------------|
| 作用时机 | AI **写代码**时 | AI **运行时调用工具**时 |
| 核心能力 | 教 AI "怎么写对"（规则 + 模板） | 让 AI 直接操控设备（工具调用） |
| 是否需要 SDK 运行 | ❌ 不需要 | ✅ 需要连接 Flight Hub 服务 |
| 安装方式 | 复制 .md 文件到 IDE | 启动 Java 服务进程 |
| 目标用户 | ISV 开发者 | AI Agent 开发者 |

**两者互补**：Skill 保证生成的代码正确，MCP Server 让 AI 直接调试/验证。

---

## 边界声明

- 本仓库**不包含** FlightHub SDK 本身——SDK 以 JAR 依赖方式引入
- 本仓库**不替代** SDK 的任何能力——只降低接入成本
- Skill 是**认知层工具**——教 AI 正确生成代码，不提供运行时保护
- MCP Server 是**能力层工具**——提供运行时 SDK 调用，不生成代码

## 适用版本

- FlightHub Java SDK v0.8.0+
- Spring Boot 2.x / 3.x
- IDE：Claude Code / Cursor / GitHub Copilot / 任何支持 Agent Skills 的工具

## License

MIT
