# cloudapi2-mcp-deploy — FlightHub MCP Server 部署助手

帮 ISV 开发者将 FlightHub MCP Server 部署到自有环境，**半自动引导式**：AI 探测环境、推荐路径、生成密钥与配置、给出精确命令，用户执行系统级操作并回报，AI 校验继续、遇错排障。

## 能力

| 能力 | 说明 |
|------|------|
| **环境探测** | 检测 OS/架构/Docker/Java/Maven/端口/网络，判断前置条件 |
| **路径选型** | 远程 SSE 部署（生产）vs 本地 stdio 开发（调试） |
| **密钥生成** | SSE Auth Token / Operator Token 强随机生成 |
| **配置拼装** | `.env` 完整值 + Claude Code MCP 客户端配置 |
| **部署引导** | 分步给命令、等回报、校验继续 |
| **部署排障** | 按现象定位（Auth 拒绝 / .so 加载 / exec format / 客户端配置等 11 类） |

## 安装

推荐通过 `devkit@cloud-api2` Plugin 一键安装（见 [skills/README.md](../README.md#安装方式)）。

单独安装：

```bash
# Claude Code
cp -r skills/cloudapi2-mcp-deploy ~/.claude/skills/

# Cursor
cp -r skills/cloudapi2-mcp-deploy .cursor/skills/

# GitHub Copilot
cp -r skills/cloudapi2-mcp-deploy .github/skills/
```

## 触发词

- 部署 MCP / 帮我部署 MCP Server / MCP deploy
- docker 部署 mcp / SSE 部署 / 远程部署 MCP
- 本地运行 MCP / stdio 配置 / Claude Code 配置 MCP
- MCP 安装 / mcp 接入 / 配置 .claude.json

## 使用示例

```
你：帮我把 MCP Server 部署到远程服务器

AI：（自动加载 Skill）
→ 先给环境探测命令，你执行后回报输出
→ 根据结果推荐部署路径（如：有远程 Linux 服务器 + Docker，推荐 SSE 远程部署）
→ 生成 SSE Auth Token，拼接完整 .env 配置
→ 逐步骤给构建/部署命令，等你回报结果
→ 验证健康检查 + SSE 流
→ 生成 Claude Code 客户端配置，引导接入
→ 确认 /mcp 面板显示 connected，部署完成
```

## 交互边界（半自动）

| 操作 | AI | 用户 |
|---|---|---|
| 探测/生成/拼装/解析/排障 | ✅ | — |
| docker build/compose / ssh / mvn | 给精确命令 | 执行 + 回报 |
| 覆盖已有配置 / 强制重启容器 | 先确认 | 确认后执行 |

## 目录结构

```
cloudapi2-mcp-deploy/
├── SKILL.md                    ← AI 主入口（决策树 + 半自动协议 + 排障路由）
├── constraints.yaml            ← 部署硬约束
├── prompts/
│   ├── env-probe.sh            — 环境探测脚本
│   └── gen-secrets.sh          — 密钥生成脚本
├── references/
│   ├── docker-sse-部署步骤.md   — Docker Compose SSE 远程部署
│   ├── stdio-本地开发步骤.md    — 本地 stdio 开发（Mock / Docker 代理）
│   ├── 环境变量速查.md          — 必填/可选变量完整列表
│   └── 部署排障.md              — 11 类现象→排查动作
└── snippets/
    ├── claude-code-sse.json    — SSE 模式客户端配置模板
    ├── claude-code-stdio-docker.json — Docker stdio 配置模板
    ├── claude-code-stdio-mock.json   — Mock stdio 配置模板
    └── 验证命令.md              — 各阶段验证命令速查
```

## 知识底座

本 Skill 的 references 均为 `mcp/docs/部署指南.md` 和 `mcp/docs/docker-deployment.md` 的提炼动作清单。深度查阅请打开部署指南。
