---
name: cloudapi2-mcp-deploy
description: |
  仅在用户要求将 FlightHub MCP Server 部署到自有环境时使用。
  不要在处理飞行控制、设备管理、遥测查询等运行时运维操作时加载本 skill（那些由 FlightHub MCP Server 直接处理）。
  适用场景：Docker Compose 远程 SSE 部署、stdio 本地开发（Docker 代理或 Mock JAR）、
  环境探测、配置生成、密钥生成、Claude Code MCP 接入配置、部署排障。
  关键词：MCP 部署, MCP deploy, mcp server 部署, docker 部署 mcp, SSE 部署,
  stdio 配置, 本地运行 mcp, run-mcp-local, deploy.sh, 远程部署,
  FH_SSE_AUTH_TOKEN, FH_SERVER_ADDRESS, MCP Server 安装, mcp 接入,
  Claude Code 配置 MCP, .claude.json, docker build mcp, mcp 镜像
author: DJI CloudAPI2 Team
version: 1.0.0
---

# FlightHub MCP Server 部署 Skill

## 适用场景

当开发者描述以下场景时触发本 Skill：

- 将 FlightHub MCP Server 部署到远程服务器（Docker Compose + SSE 模式）
- 在本地配置 Claude Code / Cursor 接入 MCP Server（stdio 或 SSE）
- 部署前的环境准备、密钥生成、配置填写
- MCP Server 构建（Maven + Docker 镜像）
- 部署过程中的启动、验证、排障
- `deploy.sh` 一键部署脚本使用

## 半自动交互协议（硬规则）

部署是**引导式半自动**流程：AI 负责思考与生成，用户负责执行系统级操作并回报输出。

| 操作 | 谁做 |
|---|---|
| 环境探测、密钥生成、配置拼装、命令生成、结果解析、日志排障 | **AI** |
| `docker build` / `docker compose` / `ssh` / `mvn package` / 修改 `.claude.json` | **用户执行**，AI 给精确命令并等待回报 |
| 危险操作（覆盖已有配置、强制重启容器） | **必须先确认** |

**流程约束**：
1. 每步只给一个命令（或一组强相关的命令），等用户回报输出后再进入下一步，禁止一口气抛全部命令
2. 用户回报输出后，AI 先**判断成败**（成功标志/错误标志），再决定继续或排障
3. `FH_SSE_AUTH_TOKEN` 留空或用占位值会被 `StartupAuthValidator` **拒绝启动**，必须生成强随机值
4. 遇到部署指南未覆盖的错误时，加载 `references/部署排障.md` 按现象定位

## 两路径决策树

```
用户说"帮我部署 MCP"
→ 加载 prompts/env-probe.sh 内容，让用户执行（或直接问环境信息）
→ 解析探测结果：OS / 架构 / Docker 版本 / Maven / Java / 网络可达性
→ 按以下逻辑推荐路径：

路径 A：远程 SSE 部署（推荐生产形态）
├─ 有远程 Linux x86_64 服务器 + Docker 24+
├─ 目标：多客户端共享一个 MCP Server 实例
├─ 加载 references/docker-sse-部署步骤.md
└─ 步骤：配置 .env → 构建镜像 → deploy.sh 一键部署 → 客户端配置接入

路径 B：本地 stdio 开发（开发调试形态）
├─ 在本地 macOS/Windows/Linux 跑 MCP Server 供 Claude Code/Cursor 使用
├─ 子选项 B1：Mock 模式（无需 SDK，任意平台） → 本地 JAR 直接跑
├─ 子选项 B2：Docker stdio 代理（需跑真 SDK） → Docker 容器 stdio
├─ 加载 references/stdio-本地开发步骤.md
└─ 步骤：构建 → 配置 MCP 客户端 → 验证工具列表

→ 若探测到非 amd64 架构 + 用户要跑真 SDK：
  明确提示"SDK JNI 原生库仅支持 Linux x86_64（amd64），
  本机只能用 Mock 模式或 Docker --platform linux/amd64 代理"
→ 与用户确认路径后，进入对应 references 逐步推进
```

## 各路径步骤入口

| 路径 | 步骤清单 | 关键产物 |
|---|---|---|
| A: 远程 SSE 部署 | `references/docker-sse-部署步骤.md` | `mcp/.env`、Docker 镜像、远端运行容器 |
| B: 本地 stdio 开发 | `references/stdio-本地开发步骤.md` | `~/.claude.json` MCP 配置 |

## 必填变量速查（缺失即停）

见 `references/环境变量速查.md`。核心必填：

| 变量 | 生成命令 | 说明 |
|---|---|---|
| `FH_SERVER_ADDRESS` | — | FlightHub 服务器地址（含协议） |
| `FH_ACCESS_TOKEN` | 从 FlightHub 控制台获取 | 工作空间访问令牌 |
| `FH_WORKSPACE_UUID` | 从 FlightHub 控制台获取 | 工作空间 UUID |
| `FH_SSE_AUTH_TOKEN` | `openssl rand -hex 32` | SSE 鉴权 token（留空拒绝启动） |

## 排障路由

```
用户回报报错信息 / 部署卡住
→ 加载 references/部署排障.md，按"现象→排查动作"定位
→ 常见：SSE Auth 拒绝启动 / .so 加载失败 / exec format error /
   Claude Code 配置不生效 / ready 503 / 重连失败
→ 修复后回到原步骤继续
```

## 反幻觉规则

1. 部署命令、镜像路径、端口、base-path 必须来自 `mcp/docs/部署指南.md` 或 `references/*.md`，禁止凭记忆编造
2. 不确定某个配置项/命令时，先读部署指南对应章节确认，再给出
3. 任何引用必须可溯源到部署指南章节或项目文件

## 共享规范

> 本 Skill 的部署约束收录在 `constraints.yaml`（对齐 `_shared/constraints-common.yaml` 的通用约束风格）。
> 跨 Skill 路由规则见 `skills/_shared/skill-routing.md`。
> 部署指南（知识底座）：`mcp/docs/部署指南.md`，references 均为其提炼的动作清单。
