---
title: "前端 SDK 组件鉴权失败排查清单"
module: operations
tags: [troubleshooting, frontend, auth, gateway]
audience: [developer]
complexity: intermediate
summary: "SDK 前端组件初始化失败的排查顺序：config.js → 容器 .env 变量 → FH2 网关路径白名单 → WebSocket 连通性"
last_updated: 2026-09-16
related:
  - operations/deployment.md
---

# 前端 SDK 组件鉴权失败排查清单

当 SDK 前端组件（UranusDeviceManagerApp、WaylineEditor、FlyDataReplay 等）出现"初始化失败"类错误时，按以下顺序排查。

## 1. config.js 是否正确生成

```bash
docker exec <frontend-container> cat /usr/share/nginx/html/config.js
```

确认 `window.__FH_CONFIG__` 包含：

| 字段 | 示例值 | 说明 |
|------|--------|------|
| `FH2_ADDRESS` | `http://<FH2_HOST>` | FH2 前端地址（不含端口） |
| `FH2_PORT` | `30812` | API 端口 |
| `FH2_SDK_AUTH_KEY` | `user_id` | 鉴权 header 名 |
| `FH2_SDK_AUTH_VALUE` | `paas_test` | 鉴权 header 值 |

**常见问题**：

- `Dockerfile.remote` 缺少 `COPY docker-entrypoint.sh` 和 `ENTRYPOINT` → config.js 为空
- `COPY dist` 缺少 `--chown=nginx:nginx` → nginx 用户无权写 config.js，entrypoint 静默失败

## 2. 容器 .env 是否包含前端变量

远端 `.env` 必须同时有**后端变量**和**前端变量**：

| 后端变量（BFF 用） | 前端变量（config.js 用） |
|---|---|
| `FH_SKELETON_SDK_AUTH_HEADER_KEY=user_id` | `VITE_FH2_SDK_AUTH_KEY=user_id` |
| `FH_SKELETON_SDK_AUTH_HEADER_VALUE=paas_test` | `VITE_FH2_SDK_AUTH_VALUE=paas_test` |

这两组是**不同名称映射同一概念**，缺一不可。`deploy-compose.yml` 中前端服务通过 `environment` 读取 `VITE_*` 变量。

## 3. FH2 网关路径白名单（auth_keys 模式）

SDK 前端组件通过 HTTP 请求 FH2 网关（`:30812/gateway/...`），**不走 SDK JNI**。

测试方法：

```bash
curl -s -w '%{http_code}' -H 'user_id: paas_test' \
  'http://<FH2_HOST>:30812/gateway/manage/api/v1/devices/airports?page=1&page_size=20'
```

| 返回 | 含义 |
|------|------|
| `203 "not find path,please add it."` | FH2 网关路径未注册，需**平台侧配置** |
| `401` / `403` | 鉴权 header 不对 |
| `200` + JSON | 正常 |

> **关键区别**：后端 skeleton 走 SDK JNI 原生调用不经过 HTTP 网关，所以后端设备数据正常 ≠ 前端 SDK 组件正常。

## 4. WebSocket 推送中心

设备管理组件还需要 WebSocket 连接推送中心（实时更新设备状态）：

```
ws://<FH2_HOST>:30812/openapi/v0.9/notification/ws/v1/notifications
```

确认 `VITE_FH2_WSS_URL` 或 config.js 中 `FH2_WSS_URL` 正确。

## 排查顺序总结

```
config.js 内容 → .env 变量完整性 → FH2 网关路径白名单 → WebSocket 连通性
```

## 相关文档

- [204 环境 auth_keys 鉴权排查记录](../../docs/) — FH2 网关 auth_keys 鉴权协议详解
- [部署指南](deployment.md)
