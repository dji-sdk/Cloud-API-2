---
title: "Operations"
module: operations
tags: [deployment, monitoring, troubleshooting]
audience: [agent, developer, operator]
complexity: intermediate
last_updated: 2026-08-05
---

# Operations

本模块涵盖部署、监控和故障恢复相关知识。

## 包含内容

| 文档 | 说明 |
|------|------|
| [deployment.md](deployment.md) | Docker Compose 部署架构、CI/CD、环境配置 |
| [frontend-sdk-auth-checklist.md](frontend-sdk-auth-checklist.md) | SDK 前端组件初始化失败的排查顺序：config.js → 容器 .env 变量 → FH2 网关路径白名单 → WebSocket 连通性 |
| [live-stream-troubleshooting.md](live-stream-troubleshooting.md) | EA340D/EA340T/EA610 私有化环境无法直播的五步自查：固件版本、上云方式、DRTC 与底座 .env、端口开放 |
