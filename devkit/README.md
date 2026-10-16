# dji-flighthub-devkit

> DJI Cloud API 2 SDK — ISV AI 易接入一站式开发者套件

## 快速入口

```
你属于哪种场景？
├─ 我要快速搭建新项目              → 用骨架（skeleton/）
├─ 我有现有项目要加 DJI 能力        → 装 Skill + 参考骨架代码
├─ 我要让 AI Agent 操控无人机       → 用 MCP Server（mcp/）
├─ 我只需要 AI 写代码时遵循规范     → 装 Skill（skills/）
├─ 我要把 SDK 知识喂给 LLM         → 用 llms.txt（knowledge/）
└─ 我有完整工程体系，只要范式参考    → 装 Skill，护栏自配
```

## 仓库结构

| 目录 | 方向 | 状态 | 说明 | 文档 |
|---|---|---|---|---|
| `skeleton/` | 方向 2 | ✅ **可用** | ISV 接入骨架 — Spring Boot 3 + Vue 3 + Spring AI，含 8 个 feature 模块、Mock/SDK 双模式、Docker Compose 一键启动 | [快速开始](skeleton/README.md) |
| `skills/` | 方向 2 | ✅ **可用** | AI Skill 集合（10 个 `cloudapi2-*` Skill）— 教 AI 正确生成 SDK 集成代码 | [总览](skills/README.md) |
| `mcp/` | 方向 1 | ✅ **可用** | MCP Server — 让 AI Agent 运行时操控无人机（57 个 Tool，21 个模块） | [快速开始](mcp/README.md) |
| `knowledge/` | 方向 3 | ✅ **可用** | 领域知识库（SSOT）— 提供 `llms.txt` 标准索引，供 LLM / RAG 按需加载 SDK 知识 | [说明](knowledge/README.md) |
| `libs/` | — | ✅ **可用** | 私有 SDK JAR（`flight-hub-sdk-3.3.7.jar`）— 不在 Maven Central，需本地安装 | [安装说明](libs/README.md) |
| `fov-demo/` | 方向 2 | ✅ **可用** | ISV 培训 Demo — FHSDK Java 自研前后端的直播、OSD、SEI、地图与手动操控全栈参考实现 | [快速开始](fov-demo/README.md) |
| `config/` | — | ✅ **可用** | 共享构建配置（checkstyle、Maven settings） | — |
| `scripts/` | — | ✅ **可用** | 自动化脚本（打包发布等） | — |

---

## Skeleton 骨架（`skeleton/`）

**一句话**：开箱即用的 ISV 接入骨架 — Spring Boot 3 + Vue 3 + Spring AI，Mock/SDK 双模式，Docker Compose 一键启动。

### 模块概览

| 层级 | 模块 | 说明 |
|------|------|------|
| Kernel | kernel-spi / kernel-impl / kernel-mock-sdk / kernel-real-sdk | SDK 封装与事件总线，ISV 不可修改 |
| Feature | feature-device / feature-wayline / feature-media / feature-terra / feature-flight-area / feature-dashboard / feature-user / feature-chat | 8 个可插拔功能模块 |
| App | fh-skeleton-app | 启动装配层 |
| Web | web/ | Vue 3 + TypeScript + Vite 前端 |

### 快速启动

```bash
# Docker Compose（推荐，零配置）
cd skeleton/docker && docker compose up --build

# 本地开发（Mock 模式）
cd skeleton && mvn clean package -DskipTests
java -jar app/fh-skeleton-app/target/fh-skeleton-app-0.1.0.jar
```

→ [完整文档](skeleton/README.md)

---

## AI Skill（`skills/`）

**一句话**：安装后，AI 在你写 Cloud API 2 SDK 集成代码时自动加载对应知识，防止 NPE、资源泄漏、权限错误。

### 安装

```bash
# 方式 1：Claude Code Plugin 一键安装（推荐）
claude plugin marketplace add dji-sdk/Cloud-API-2
claude plugin install cloudapi2@dji

# 方式 2：npx skills add（自动检测 IDE）
npx skills add https://github.com/dji-sdk/Cloud-API-2

# 方式 3：手动拷贝
cp -r skills/cloudapi2-sdk-core ~/.claude/skills/    # Claude Code
cp -r skills/cloudapi2-sdk-core ~/.codebuddy/skills/   # CodeBuddy
cp -r skills/cloudapi2-sdk-core ~/.trae/skills/        # Trae
```

### 能力

| 能力 | 说明 |
|------|------|
| 核心规则引导 | 5 条硬约束（initialize 检查、errorCode、listen/cancel、飞控权、null 防御） |
| 错误码诊断 | 4 层错误结构（SDK → HTTP → 服务端 → 设备）自动定位 |
| 接入质量评估 | 三级风险检查清单 |
| 业务场景 Prompt | 设备列表 / OSD 监听 / 直播 / 航线 / 飞控 / 媒体 6 个标准化范本 |
| Java 代码模板 | SDK 适配器 / Service 层 / 回调桥接工具 3 套模板 |

### 触发方式

安装后**无需手动调用**，输入以下关键词时自动触发：
- "集成 DJI Cloud API 2 SDK" / "初始化 SDK" / "订阅无人机状态"
- "让无人机返航" / "getDroneInstance 返回 null"
- "检查 SDK 接入代码质量"

→ [完整文档](skills/README.md)

---

## MCP Server（`mcp/`）

**一句话**：让 AI Agent 在运行时直接操控无人机（设备查询、状态监听、飞行控制）。

57 个 Tool，覆盖 21 个模块：设备管理、飞行控制（DRC）、航线任务、相机、媒体、直播、空域安全、重建、探照灯、PSDK 挂载、固件、诊断、模拟器、运维等。

```bash
cd mcp && mvn package -DskipTests
# 启动后 AI Agent 通过 JSON-RPC 调用 SDK 工具
```

→ [快速开始](mcp/README.md)

---

## Knowledge Base（`knowledge/`）

**一句话**：SDK 领域知识的单一源头（SSOT），提供 `llms.txt` 标准索引，供 LLM / RAG 按需加载。

覆盖模块：系统架构、快速入门、API 参考、错误码、飞控权限、运维部署、FAQ 等。

使用方式：
- **AI Agent / LLM**：Fetch `knowledge/llms.txt` → 按需加载具体文档
- **Claude Code 用户**：安装 Skills 即可，Skills 自动引用知识库内容
- **人类开发者**：直接阅读 `.md` 文件

→ [详细说明](knowledge/README.md)

---

## 组件关系

| 维度 | Skeleton | Skill | MCP Server | Knowledge |
|------|----------|-------|------------|-----------|
| 作用时机 | **开发时**（项目骨架） | AI **写代码**时 | AI **运行时调用工具**时 | AI **理解 SDK**时 |
| 核心能力 | 开箱即用的项目模板 | 教 AI "怎么写对" | 让 AI 直接操控设备 | 提供 SDK 领域知识 |
| 是否需要 SDK 运行 | Mock 模式不需要 | ❌ 不需要 | ✅ 需要连接底座 | ❌ 不需要 |
| 目标用户 | ISV 开发者 | ISV 开发者 | AI Agent 开发者 | 任何 LLM / 开发者 |

**四者互补**：Knowledge 提供知识源头 → Skill 引用知识教 AI 生成正确代码 → Skeleton 提供项目骨架 → MCP Server 让 AI 直接调试/验证。

---

## 边界声明

- 本仓库**不包含** DJI Cloud API 2 SDK 本身——SDK 以 JAR 依赖方式引入
- 本仓库**不替代** SDK 的任何能力——只降低接入成本
- Skill 是**认知层工具**——教 AI 正确生成代码，不提供运行时保护
- MCP Server 是**能力层工具**——提供运行时 SDK 调用，不生成代码

## 适用版本

- DJI Cloud API 2 Java SDK v3.3.7
- Spring Boot 3.x
- IDE：Claude Code / Cursor / GitHub Copilot / 任何支持 Agent Skills 的工具

## License

MIT
