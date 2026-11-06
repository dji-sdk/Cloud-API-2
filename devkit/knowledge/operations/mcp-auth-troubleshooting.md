---
title: "MCP Server 鉴权失败排查清单"
module: operations
tags: [troubleshooting, mcp, auth, sse, gateway]
audience: [developer]
complexity: intermediate
summary: "MCP Server 两层鉴权排查：SSE 传输层（FH_SSE_AUTH_TOKEN）和 SDK 网关层（FH_HTTP_AUTH_KEY/VALUE），按层定位 401/启动失败问题"
last_updated: 2026-10-16
related:
  - operations/frontend-sdk-auth-checklist.md
  - operations/deployment.md
---

# MCP Server 鉴权失败排查清单

MCP Server 有**两层独立鉴权**，任何一层失败都会导致 401 或功能异常。排查时务必区分是哪一层出了问题。

```
AI Agent / MCP Client
    │
    │  Authorization: Bearer <FH_SSE_AUTH_TOKEN>
    │
    ▼
┌─────────────────────────────┐
│  第 1 层：SSE 传输层鉴权     │  ← AuthenticationWebFilter
│  验证客户端身份              │     BearerTokenAuthenticator
└─────────────────────────────┘
    │
    │  fh_connect → SDK 初始化
    │
    ▼
┌─────────────────────────────┐
│  第 2 层：SDK 网关鉴权       │  ← SdkService.setAuthHeaders()
│  SDK 调用 FH2 平台 API 时    │     auth_keys + 实际鉴权 header
│  携带的 HTTP/WebSocket 头    │
└─────────────────────────────┘
    │
    ▼
  FH2 平台网关（:30812/gateway/...）
```

## 快速判断是哪一层

| 现象 | 所在层 | 跳转 |
|------|--------|------|
| SSE 模式**启动失败**，日志含 `【安全拦截】` | 第 1 层 | [§1](#1-sse-传输层鉴权sse-模式) |
| MCP Client 连接时收到 `401 Unauthorized` | 第 1 层 | [§1](#1-sse-传输层鉴权sse-模式) |
| 连接成功，`fh_connect` 返回错误 `SDK handshake 未完成或认证失败` | 第 2 层 | [§2](#2-sdk-网关层鉴权tool-调用) |
| 连接成功，各 tool 调用返回 401 或设备列表为空 | 第 2 层 | [§2](#2-sdk-网关层鉴权tool-调用) |
| stdio 模式下 tool 调用 401 | 第 2 层 | [§2](#2-sdk-网关层鉴权tool-调用)（stdio 无第 1 层） |

---

## 1. SSE 传输层鉴权（SSE 模式）

SSE 模式下，`AuthenticationWebFilter` 对所有非公开路径（`/health`、`/ready` 除外）执行 Bearer Token 校验。

### 1.1 启动失败：token 未配置或为弱占位值

`StartupAuthValidator` 在 Bean 初始化完成后、端口绑定前执行硬校验。

**报错示例**：
```
【安全拦截】SSE 模式启动被拒绝：FH_SSE_AUTH_TOKEN 未配置。
```
或
```
【安全拦截】SSE 模式启动被拒绝：FH_SSE_AUTH_TOKEN 检测到公知弱占位值。
```

**解决**：

```bash
# 生成强随机 token
openssl rand -hex 32

# 写入 .env 或 docker-compose 环境变量
FH_SSE_AUTH_TOKEN=<生成的值>
```

以下占位值会被拦截，**不可用于生产**：

- `change-me-to-a-strong-random-token`
- `changeme` / `change-me`
- `your-token` / `placeholder` / `replace-me`
- `REPLACE_WITH_openssl_rand_hex_32_OUTPUT`（.env.example 默认值）

### 1.2 客户端连接 401

**验证方法**：

```bash
# 测试 SSE 连接（替换为实际地址和 token）
curl -v -H "Authorization: Bearer <你的FH_SSE_AUTH_TOKEN>" \
  "http://<MCP_HOST>:<PORT>/pf-devkit/ai-plugin/mcp/sse"
```

| 返回 | 含义 | 处理 |
|------|------|------|
| `401 Missing Authorization header` | 客户端没带 header | 检查 MCP Client 配置，确认传了 `Authorization` 头 |
| `401 Invalid Authorization scheme` | 格式不对 | 必须是 `Bearer <token>`，注意大小写和空格 |
| `401 Invalid bearer token` | token 不匹配 | 对比客户端 token 与服务端 `FH_SSE_AUTH_TOKEN` 是否一致 |
| SSE 事件流正常返回 | 第 1 层通过 | 如果 tool 仍 401，问题在第 2 层 |

### 1.3 stdio 模式不受影响

`AuthenticationWebFilter` 和 `StartupAuthValidator` 仅在 `@Profile("sse")` 下激活。stdio 模式是本地 IDE 集成，不暴露网络端口，无需配置 `FH_SSE_AUTH_TOKEN`。

---

## 2. SDK 网关层鉴权（Tool 调用）

`fh_connect` 调用 `SdkService.initialize()` 时，会将鉴权 header 注入 SDK 的 HTTP 和 WebSocket 通道。SDK 后续所有 API 调用都携带这些 header 访问 FH2 平台网关。

### 2.1 涉及的环境变量

| 环境变量 | .env.example 默认值 | 说明 | 必填 |
|---------|---------------------|------|------|
| `FH_SERVER_ADDRESS` | `https://your-server.com` | FH2 平台地址 | 是 |
| `FH_ACCESS_TOKEN` | `your-workspace-access-token` | SDK 认证 Token，写入 `Authorization: Bearer <token>` | 是 |
| `FH_HTTP_AUTH_KEY` | `user_id` | 网关鉴权 header 名（`auth_keys` 协议） | 是 |
| `FH_HTTP_AUTH_VALUE` | `your-auth-value` | 网关鉴权 header 值（**占位符，必须替换**） | 是 |

SDK 实际发送的 header 结构：

```
Authorization: Bearer <FH_ACCESS_TOKEN>
auth_keys: <FH_HTTP_AUTH_KEY>
<FH_HTTP_AUTH_KEY>: <FH_HTTP_AUTH_VALUE>
```

以 `.env.example` 默认值为例：

```
Authorization: Bearer your-workspace-access-token
auth_keys: user_id
user_id: your-auth-value
```

### 2.2 常见故障

#### FH_HTTP_AUTH_VALUE 未替换（最常见）

`.env.example` 中 `FH_HTTP_AUTH_VALUE=your-auth-value` 是占位符。如果直接复制未改，SDK 发给 FH2 网关的鉴权值是错的，**所有 tool 调用都会 401**。

**解决**：向 FH2 平台管理员获取实际的鉴权值，填入 `FH_HTTP_AUTH_VALUE`。

#### FH_ACCESS_TOKEN 过期或无效

**现象**：`fh_connect` 返回 `SDK handshake 未完成或认证失败`。

**解决**：在 FH2 管理后台重新获取 workspace access token。

#### FH_HTTP_AUTH_KEY 与平台不匹配

FH2 平台 `isv-webhook-auth` 插件的鉴权 header 名是平台侧配置的。如果平台用的不是 `user_id` 而是其他名称（如 `api_key`），需要同步修改。

### 2.3 验证方法

```bash
# 直接用 curl 模拟 SDK 调用网关，验证鉴权 header 是否正确
curl -s -w '\n%{http_code}' \
  -H "Authorization: Bearer <FH_ACCESS_TOKEN>" \
  -H "auth_keys: <FH_HTTP_AUTH_KEY>" \
  -H "<FH_HTTP_AUTH_KEY>: <FH_HTTP_AUTH_VALUE>" \
  "http://<FH2_HOST>:30812/gateway/manage/api/v1/devices/airports?page=1&page_size=20"
```

| 返回 | 含义 | 处理 |
|------|------|------|
| `200` + JSON | 鉴权正常 | 问题可能在 SDK 初始化顺序，检查 `fh_connect` 日志 |
| `401` / `403` | token 或鉴权 header 不对 | 逐个检查上面 4 个环境变量 |
| `203 "not find path"` | 网关路径未注册 | 需平台侧配置，参考 [前端鉴权排查清单](frontend-sdk-auth-checklist.md) §3 |
| 连接超时 | 地址不通 | 检查 `FH_SERVER_ADDRESS` 和网络连通性 |

### 2.4 与 Skeleton 变量名的对应关系

MCP 和 Skeleton 是同一概念的不同变量名，配置时需保持值一致：

| 概念 | MCP 变量名 | Skeleton 变量名 |
|------|-----------|----------------|
| 网关鉴权 header 名 | `FH_HTTP_AUTH_KEY` | `FH_SKELETON_SDK_AUTH_HEADER_KEY` |
| 网关鉴权 header 值 | `FH_HTTP_AUTH_VALUE` | `FH_SKELETON_SDK_AUTH_HEADER_VALUE` |

---

## 3. 排查顺序总结

```
1. 确认部署模式（stdio / SSE）
   │
   ├── SSE 模式 → 检查 FH_SSE_AUTH_TOKEN（§1）
   │   ├── 启动失败 → token 未配置或弱占位值
   │   └── 客户端 401 → token 不匹配
   │
   └── 两种模式都要检查 → SDK 网关鉴权（§2）
       ├── fh_connect 失败 → FH_SERVER_ADDRESS / FH_ACCESS_TOKEN
       └── tool 调用 401 → FH_HTTP_AUTH_KEY / FH_HTTP_AUTH_VALUE
```

## 相关文档

- [前端 SDK 组件鉴权失败排查清单](frontend-sdk-auth-checklist.md) — 前端 SDK 组件（设备管理等）的鉴权排查
- [部署指南](deployment.md)
