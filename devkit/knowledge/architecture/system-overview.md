---
title: "DJI CloudAPI2 系统架构全景"
module: architecture
tags: [architecture, sdk, mcp, frontend, overview]
audience: [agent, developer]
complexity: beginner
summary: "SDK/MCP/前端组件三层架构全景"
last_updated: 2026-08-05
related:
  - getting-started/sdk-setup.md
  - architecture/control-authority.md
---

DJI CloudAPI2 SDK 由三层组成：Java SDK（核心能力）、MCP Server（AI 工具层）、前端组件（UI 层），共同服务 ISV 的无人机管理系统。

# 系统架构全景

## 三层能力模型

```
┌─────────────────────────────────────────────────┐
│          ISV 应用（BFF + Web 前端）                │
└────────┬────────────────┬──────────────┬────────┘
         │                │              │
    ┌────▼─────┐   ┌─────▼──────┐  ┌───▼────────────┐
    │ Java SDK │   │ MCP Server │  │ 前端组件         │
    │ (核心)   │   │ (AI Tool)  │  │ (paas.js)      │
    └────┬─────┘   └─────┬──────┘  └───┬────────────┘
         │                │              │
         └────────────────┼──────────────┘
                          ▼
              ┌───────────────────────┐
              │  DJI CloudAPI2 Platform │
              │  (WebSocket + HTTP)   │
              └───────────────────────┘
```

## 各层职责

| 层 | 职责 | 典型使用者 |
|----|------|-----------|
| **Java SDK** | 设备控制、遥测订阅、航线管理、媒体处理 | ISV 后端 (Spring Boot BFF) |
| **MCP Server** | 将 SDK 能力暴露为 51 个 AI Tool，AI Agent 可直接调用 | AI Agent / Claude Code / Copilot |
| **前端组件** | 航线编辑器、驾驶舱、设备窗口、回放器 | ISV Web 前端 (React/Vue) |

## SDK 通信协议

- **控制指令**：HTTP REST → DJI CloudAPI2 Platform → MQTT → 设备
- **遥测数据**：设备 → MQTT → DJI CloudAPI2 Platform → WebSocket Push → SDK 回调
- **媒体流**：设备 → RTMP/WebRTC → 流媒体服务 → 前端播放器

## 关键约束

1. **SDK 仅支持 Linux x86_64**：底层 JNI 原生库限定平台（macOS/Windows 可用 Mock 模式开发调试）
2. **单进程单实例**：`FlightHubSDK.initialize()` 在一个 JVM 进程中只能成功一次
3. **事件驱动**：所有状态变化通过 listen/cancel 回调推送，不提供轮询接口
4. **权限互斥**：飞控权同一时刻只有一个持有者（详见 [control-authority.md](control-authority.md)）
