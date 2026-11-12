# dji-flighthub-devkit

> DJI Cloud API 2 SDK — 集成商 AI 易接入一站式开发者套件

## 快速入口

```
你属于哪种场景？
├─ 我要快速 demo / 新项目起步       → 用骨架（skeleton/，✅ 可用）
├─ 我有现有项目要加 DJI 能力        → 装 Skill（skills/，Plugin 一键安装）
├─ 我要让 AI Agent 操控无人机       → 用 MCP Server（mcp/）
├─ 我只需要 AI 写代码时遵循规范     → 装 Skill（skills/）
└─ 我有完整工程体系，只要范式参考    → 装 Skill，按需裁剪
```

## 仓库结构

| 目录 | 状态 | 说明 | 文档 |
|---|---|---|---|
| `skills/` | ✅ **可用** | AI Skill 集合（13 个 `cloudapi2-*` Skill 目录，全部安装可用）— 教 AI 正确生成 SDK 集成代码 | [总览](skills/README.md) |
| `mcp/` | ✅ **可用** | MCP Server — 让 AI Agent 运行时操控无人机（71 个 Tool：67 个 `fh_*` + 4 个 `sim_*`） | [快速开始](mcp/README.md) · [工具手册](docs/mcp-tools-guide.md) |
| `skeleton/` | ✅ **可用** | 集成商接入骨架 — Spring Boot 3.4.1 BFF（Kernel/Feature 可插拔架构）+ Vue 3 前端 | [快速开始](skeleton/README.md) |
| `fov-demo/` | ✅ **可用** | 集成商培训 Demo — FHSDK Java 自研前后端的直播、OSD、SEI、地图与手动操控全栈参考实现（独立工程，不参与 skeleton 构建） | [快速开始](fov-demo/README.md) · [实战案例](fov-demo/docs/case-study/集成商自研Demo实战案例.md) |
| `knowledge/` | ✅ **可用** | 团队知识库（文档收录 + 索引重建，配套 `cloudapi2-knowledge-ingest` Skill） | [总览](knowledge/README.md) |
| `libs/` | ✅ **可用** | 私有 SDK JAR（`flight-hub-sdk-3.3.7`，`amd64/` + `arm64/` 两包，按架构装到本地 Maven 仓库） | [说明](libs/README.md) |
| `config/` | — | 跨项目共享的 Maven `settings.xml` 与 Checkstyle 规则 | — |
| `scripts/` | — | 发布打包脚本（`make release` 依赖） | — |
| `docs/` | — | 集成商产品文档（MCP 工具使用手册） | [工具手册](docs/mcp-tools-guide.md) |
| `ci/` | — | CI 辅助脚本（含仅限内部使用的 `verify-multiarch.sh`）与 Maven settings 片段模板 | — |
| `cli/` | 🚧 计划中 | `@dji/security-init` 安全护栏注入 CLI（目录尚未创建） | — |
| `llms/` | 🚧 计划中 | llms.txt / llms-full.txt 生成器（目录尚未创建，当前 `knowledge/llms.txt` 为手工维护） | — |

---

## AI Skill（`skills/`）

**一句话**：安装后，AI 在你写 DJI Cloud API 2 SDK 集成代码时自动加载对应知识，防止 NPE、资源泄漏、权限错误。

### 安装

方式 1（推荐）：通过 Claude Code Plugin 一次装齐 13 个可用 Skill。

```bash
# 1. 注册 marketplace（只需一次）
claude plugin marketplace add https://github.com/dji-sdk/Cloud-API-2.git

# 2. 安装 plugin（marketplace 名 dji，plugin 名 cloudapi2）
claude plugin install cloudapi2@dji
```

私有部署环境把地址换成内部 Git 仓库地址即可；marketplace / plugin 标识以仓库内
[`.claude-plugin/`](.claude-plugin/) 为准：`marketplace.json` 的 `name` 为 `dji`，
`plugin.json` 的 `name` 为 `cloudapi2`。

方式 2：手动拷贝单个 Skill

```bash
# 克隆仓库（如尚未克隆）
git clone https://github.com/dji-sdk/Cloud-API-2.git
cd Cloud-API-2/devkit

cp -r skills/cloudapi2-sdk-core ~/.claude/skills/    # Claude Code
cp -r skills/cloudapi2-sdk-core .cursor/skills/      # Cursor
cp -r skills/cloudapi2-sdk-core .github/skills/      # GitHub Copilot (VS Code)
```

### 能力

| 能力 | 说明 |
|------|------|
| 核心规则引导 | 6 条硬约束（JNI 加载 + 鉴权头注入 → initialize 检查、errorCode、listenId/cancel、飞控抢权/释放、null 防御、危险操作风险警告） |
| 错误码诊断 | 4 层错误结构（SDK → HTTP → 服务端 → 设备）自动定位 |
| 代码质量自检 | 按 6 条核心规则逐条 review 生成/存量调用代码 |
| 业务场景 Prompt | 设备列表 / OSD 监听 / 直播 / 航线 / 飞控 / 媒体 / 航线资产 7 个标准化范本 |
| Java 代码模板 | SDK 适配器 / Service 层 / 回调桥接 3 套模板（`.tpl`） |

### 触发方式

安装后**无需手动调用**，输入以下关键词时自动触发：
- "集成 DJI Cloud API 2 SDK" / "初始化 SDK" / "订阅无人机状态"
- "让无人机返航" / "getDroneInstance 返回 null"
- "检查 SDK 接入代码质量"

→ [完整文档](skills/README.md)

---

## MCP Server（`mcp/`）

**一句话**：让 AI Agent 在运行时直接操控无人机（设备查询、状态监听、飞行控制）。

```bash
# 先安装私有 SDK JAR（首次，按本机架构选 amd64/arm64，详见 libs/README.md）
cd mcp && mvn package -DskipTests
# 启动后 AI Agent 通过 JSON-RPC 调用 SDK 工具
```

> 本地无 Linux 环境时用 Mock 模式运行：`FH_MOCK=true FH_SERVER_ADDRESS=http://placeholder`（见 [mcp/README.md](mcp/README.md)）。

→ [快速开始](mcp/README.md) · [工具使用手册](docs/mcp-tools-guide.md)

---

## Skill vs MCP Server

| 维度 | Skill | MCP Server |
|------|-------|------------|
| 作用时机 | AI **写代码**时 | AI **运行时调用工具**时 |
| 核心能力 | 教 AI "怎么写对"（规则 + 模板） | 让 AI 直接操控设备（工具调用） |
| 是否需要 SDK 运行 | ❌ 不需要 | ✅ 需要连接 DJI Cloud API 2 服务 |
| 安装方式 | Claude Code Plugin（或拷贝 .md 到 IDE） | 启动 Java 服务进程 |
| 目标用户 | 集成商开发者 | AI Agent 开发者 |

**两者互补**：Skill 保证生成的代码正确，MCP Server 让 AI 直接调试/验证。

---

## 边界声明

- 本仓库**不包含** DJI Cloud API 2 SDK 的源码——只内置 `flight-hub-sdk:3.3.7` 的预编译 JAR（`libs/amd64`、`libs/arm64`），需手动 install 到本地 Maven 仓库后构建
- 本仓库**不替代** SDK 的任何能力——只降低接入成本
- Skill 是**认知层工具**——教 AI 正确生成代码，不提供运行时保护
- MCP Server 是**能力层工具**——提供运行时 SDK 调用，不生成代码

## 适用版本

- DJI Cloud API 2 Java SDK **3.3.7**（`libs/` 内置，非 Maven Central）
- JDK **17**
- Spring Boot **3.x**（`mcp/`、`skeleton/` 为 3.4.1；`fov-demo/` 为 3.2.5）
- 运行平台：SDK 原生库仅支持 **Linux x86_64 / aarch64**；macOS / Windows 使用 Mock 模式（`FH_MOCK=true`）调试
- IDE：Claude Code / Cursor / GitHub Copilot / 任何支持 Agent Skills 的工具

## License

Apache License 2.0（以仓库根目录 [LICENSE](./LICENSE) 为准）
