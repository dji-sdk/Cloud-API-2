---
title: "部署架构与运维指南"
module: operations
tags: [deployment, docker, nginx, ci-cd, environment]
audience: [developer, operator]
complexity: intermediate
summary: "Docker Compose 部署架构、CI/CD、环境配置"
last_updated: 2026-08-05
related:
  - architecture/system-overview.md
  - getting-started/sdk-setup.md
---

devkit 包含两个独立可部署的服务（MCP Server + Skeleton BFF），通过 Docker Compose 滚动更新部署。

# DJI FlightHub PaaS DevKit 部署总览

> **版本**: v1.0
> **更新日期**: 2026-08-01
> **仓库**: `dji-flighthub-devkit`

本文档是 devkit 部署的**总入口**，给出整体架构与部署顺序，具体细节引用两个子模块的部署指南：

- MCP Server → [mcp/docs/部署指南.md](../mcp/docs/部署指南.md)
- Skeleton BFF → [skeleton/docs/部署指南.md](../skeleton/docs/部署指南.md)

接口协议另见 [docs/接口协议文档.md](./接口协议文档.md)。

## 1. 仓库与服务

| 服务 | 模块 | 协议 | 默认端口 | 基础路径 | 镜像 |
|------|------|------|----------|----------|------|
| MCP Server | `mcp/` | JSON-RPC over SSE / stdio | 8080 | `/pf-devkit/ai-plugin/mcp` | `your-registry.example.com/your-org/dji-flighthub-devkit/mcp` |
| Skeleton BFF（后端） | `skeleton/` | REST + SSE | 8080 | `/pf-devkit/ai-plugin/skeleton` | `.../skeleton` |
| Skeleton BFF（前端） | `skeleton/web/` | 静态 + Nginx 代理 | 80 | — | `.../skeleton-ui` |

两个服务**可独立部署**，但若启用 Skeleton 的 Chat 模块，需通过 `FH_CHAT_MCP_URL` 指向 MCP Server。

## 2. 整体架构

```
                          ┌────────────────────────────────┐
   AI Agent ──SSE/stdio──▶│  MCP Server (mcp)              │
   (Claude Code/远程)      │  /pf-devkit/ai-plugin/mcp      │
                          │  53 个 fh_* Tool               │
                          └──────────┬─────────────────────┘
                                     │ SDK (JNI, Linux x86_64)
                                     ▼
                          ┌──────────────────────────┐
                          │  FlightHub / CloudAPI 2.0│
                          └──────────────────────────┘

   浏览器 ──80/443──▶ fh-ssl (可选 TLS)
                        │
                        ▼
                     fh-ui (Nginx + Vue 3)
                        │ /pf-devkit/ai-plugin/skeleton/* 代理
                        ▼
                     skeleton (Spring Boot)
                        │            │
              ┌─────────┘            └──────────▶ MCP Server
              ▼                                  (FH_CHAT_MCP_URL, Chat 透传)
           MySQL 8.0
           (fh_skeleton)
```

## 3. 共性约束

### 3.1 平台限制

两个服务都依赖 FlightHub SDK 的 **JNI 原生库（仅 Linux x86_64）**：
- Linux x86_64：可直接运行 JAR。
- macOS / Windows：必须用 Docker `--platform linux/amd64`。
- Mock 模式（MCP `FH_MOCK=true` / Skeleton `FH_PROFILE=mock`）：无平台限制，跳过 SDK 初始化，返回模拟数据，仅用于开发演示。

### 3.2 私有 SDK JAR

`libs/flight-hub-sdk-3.3.7.jar` 不在 Maven Central。本地 `mvn package` 前每台机器需手动安装一次（两个 Dockerfile 已内置）：

```bash
mvn install:install-file \
  -Dfile=libs/flight-hub-sdk-3.3.7.jar \
  -DgroupId=com.dji.flighthubsdk \
  -DartifactId=flight-hub-sdk \
  -Dversion=3.3.7 \
  -Dpackaging=jar \
  --settings config/settings.xml
```

### 3.3 GLIBC 兼容性

Ubuntu 26.04+（glibc 2.39+）加载带 `execstack` 标记的 `libVolcEngineRTC.so` 会失败，需设：

```
GLIBC_TUNABLES=glibc.rtld.execstack=2
```

（MCP docker-compose 已内置；Skeleton 后端 Dockerfile 安装了对应系统依赖。）

### 3.4 镜像仓库与 CI/CD

两者 CI/CD 结构一致（`<module>/.gitlab-ci.yml`）：

```
test (coverage / lint / security) → build (kaniko 推 Harbor) → deploy (SSH + docker compose)
```

| 项 | MCP | Skeleton |
|----|-----|----------|
| Harbor 项目 | `your-org/dji-flighthub-devkit` | `your-org/dji-flighthub-devkit` |
| 镜像 | `mcp` | `skeleton` + `skeleton-ui` |
| 标签策略 | master/tag 推 `:TIMESTAMP`+`:PIPELINE_ID`+`:latest`；MR 只推前两个 | 同 |
| deploy-test | master 手动 | master 手动 |
| deploy-prod | tag `v*` 手动 | tag `v*` 手动 |

CI 必填变量：`HARBOR_HOST` / `HARBOR_USER` / `HARBOR_PASSWORD` / `HARBOR_PROJECT`；可选 `MAVEN_MIRROR_URL` / `NVD_API_KEY`。

### 3.5 健康检查端点

| 服务 | liveness | readiness |
|------|----------|-----------|
| MCP | `GET /pf-devkit/ai-plugin/mcp/health` | `GET /pf-devkit/ai-plugin/mcp/ready` |
| Skeleton | `GET /pf-devkit/ai-plugin/skeleton/actuator/health` | 同（仅暴露 health/info） |

两者健康端点均**认证豁免**，可直接用于 K8s probe / 负载均衡。

## 4. 部署顺序

### 4.1 推荐：先 MCP，后 Skeleton

Skeleton 的 Chat 模块依赖 MCP Server，故建议按序部署：

1. **部署 MCP Server** — 详见 [mcp/docs/部署指南.md](../mcp/docs/部署指南.md)
   - CI 构建镜像 → `deploy-test` 或手动 `./mcp/deploy.sh`
   - 验证：`curl http://<mcp-host>:8080/pf-devkit/ai-plugin/mcp/health` → `{"status":"UP"}`
   - 记录 `FH_SSE_AUTH_TOKEN`（Skeleton Chat 透传时若需鉴权要用）

2. **部署 Skeleton BFF** — 详见 [skeleton/docs/部署指南.md](../skeleton/docs/部署指南.md)
   - CI 构建后端 + 前端镜像 → `deploy-test` 或手动 `./skeleton/deploy.sh`
   - 在 `skeleton/deploy.env` 配置 `FH_CHAT_MCP_URL=http://<mcp-host>:8080`、`FH_CHAT_MCP_AUTH_TOKEN`、`FH_CHAT_ENABLED=true`
   - 验证：`curl http://<skeleton-host>:<BACKEND_PORT>/pf-devkit/ai-plugin/skeleton/actuator/health` → `{"status":"UP"}`
   - 前端：`curl http://<skeleton-host>:<FRONTEND_PORT>/`

### 4.2 仅部署其一

- **只部署 MCP**：供 Claude Code / 远程 AI Agent 直接通过 SSE 或 stdio 接入，不需要 Skeleton。
- **只部署 Skeleton（不启用 Chat）**：`FH_CHAT_ENABLED=false`，BFF 仍可独立提供设备/任务/航线/媒体/Terra REST 接口；mock profile 下甚至不需要真 SDK。

## 5. 关键配置对照

### 5.1 MCP Server 必填

| 变量 | 说明 |
|------|------|
| `FH_SERVER_ADDRESS` | FlightHub 服务器地址 |
| `FH_ACCESS_TOKEN` | workspace-access-token |
| `FH_WORKSPACE_UUID` | workspace-binding-uuid |
| `FH_SSE_AUTH_TOKEN` | SSE 鉴权（`openssl rand -hex 32`，留空拒绝启动） |
| `FH_TRANSPORT=sse` | Docker 默认 |
| `FH_MAX_SECURITY_LEVEL=2` | 生产建议 |

### 5.2 Skeleton BFF 必填

| 变量 | 说明 |
|------|------|
| `FH_JWT_SECRET` | JWT 密钥（`openssl rand -hex 32`，无默认值） |
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 |
| `FH_SKELETON_SDK_TOKEN` / `FH_SKELETON_SDK_ADDRESS` / `FH_SKELETON_SDK_NOTIFICATION_SERVER` | sdk profile 必填 |
| `FH_CHAT_MCP_URL` | Chat 启用时必填，指向 MCP Server |

### 5.3 两者关联（Chat 链路）

```
Skeleton (feature-chat) ──HTTP+SSE──▶ MCP Server (/pf-devkit/ai-plugin/mcp/sse)
   FH_CHAT_MCP_URL                       FH_SSE_AUTH_TOKEN
   FH_CHAT_MCP_AUTH_TOKEN ──────────────▶ Authorization: Bearer
```

Chat 流程：浏览器 → `fh-ui`(Nginx, `proxy_buffering off`) → `skeleton` `/api/v1/chat/stream`(SSE) → Spring AI 路由模型 → 工具调用透传到 MCP Server → MCP 执行 SDK → 结果回流。

## 6. 本地开发快速路径

| 场景 | 命令 |
|------|------|
| MCP Mock（任意平台） | `cd mcp && FH_SERVER_ADDRESS=http://placeholder FH_MOCK=true ./run-mcp-sse.sh` |
| MCP stdio（Claude Code 本地） | `cd mcp && ./run-mcp-local.sh` |
| Skeleton mock（含 MySQL） | `cd skeleton/docker && docker compose up --build` |
| Skeleton 前端 dev | `cd skeleton/web && npm run dev` |

## 7. 生产环境检查清单

### MCP Server

- [ ] `FH_MAX_SECURITY_LEVEL=2`（禁 L3 紧急停机，除非显式需要）
- [ ] `FH_SSE_AUTH_TOKEN` 为强随机值（非占位）
- [ ] `FH_AUDIT_ENABLED=true` / `FH_IDEMPOTENCY_ENABLED=true`
- [ ] `GLIBC_TUNABLES=glibc.rtld.execstack=2`（Ubuntu 26.04+）
- [ ] 内存限制 ≥ 1g
- [ ] `/ready` 返回 200

### Skeleton BFF

- [ ] 镜像用 `-Psdk-real` 构建（非 mock）
- [ ] `FH_JWT_SECRET` 显式配置（勿依赖 deploy.sh 兜底）
- [ ] `MYSQL_ROOT_PASSWORD` / `FH_DB_PASSWORD` 强随机
- [ ] `FH_PROFILE=sdk`（生产勿用 mock）
- [ ] `FH_CHAT_ENABLED=true` 时 `FH_CHAT_MCP_URL` 可达
- [ ] Nginx `/pf-devkit/ai-plugin/skeleton/` 代理 `proxy_buffering off`、超时 300s（SSE）
- [ ] `actuator/health` 返回 UP

## 8. 文档索引

| 主题 | 路径 |
|------|------|
| **MCP Server 部署（完整）** | [mcp/docs/部署指南.md](../mcp/docs/部署指南.md) |
| MCP Server Docker 快速上手 | [mcp/docs/docker-deployment.md](../mcp/docs/docker-deployment.md) |
| MCP Server 技术设计 | [mcp/docs/MCP-Server-技术设计.md](../mcp/docs/MCP-Server-技术设计.md) |
| MCP Tool 列表 | [mcp/docs/MCP-TOOL工具列表汇总.md](../mcp/docs/MCP-TOOL工具列表汇总.md) |
| **Skeleton BFF 部署（完整）** | [skeleton/docs/部署指南.md](../skeleton/docs/部署指南.md) |
| Skeleton Docker 快速上手 | [skeleton/docs/docker-deploy-guide.md](../skeleton/docs/docker-deploy-guide.md) |
| Skeleton 设计手册 | [skeleton/docs/skeleton-design-workbook.md](../skeleton/docs/skeleton-design-workbook.md) |
| SDK Profile (ISV) 指南 | [skeleton/docs/sdk-profile-isv-guide.md](../skeleton/docs/sdk-profile-isv-guide.md) |
| 接口协议文档 | [docs/接口协议文档.md](./接口协议文档.md) |

## 变更记录

| 日期 | 版本 | 变更 | 作者 |
|------|------|------|------|
| 2026-08-01 | v1.0 | 初始版本，建立总览入口并引用 mcp/skeleton 子指南 | DJI DevKit Team |
