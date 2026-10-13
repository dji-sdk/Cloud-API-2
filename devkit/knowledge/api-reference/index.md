---
title: "API Reference"
module: api-reference
tags: [api, sdk, interface, rules]
audience: [agent, developer]
complexity: intermediate
last_updated: 2026-08-05
---

# API Reference

本模块包含 SDK 接口使用的核心规则、错误码字典和模块级流程文档。

## 包含内容

| 文档 | 说明 |
|------|------|
| [core-rules.md](core-rules.md) | 6 条必须遵守的 SDK 集成核心规则 |
| [device-management.md](device-management.md) | 设备发现 → 订阅 → 控制全流程 |
| [error-codes.md](error-codes.md) | 四层分诊错误码字典（errorCode → httpCode → serverCode → deviceCode） |
| [flight-control.md](flight-control.md) | 飞控权限 API、DRC 链路、安全执行模板 |
| [flight-task-capabilities.md](flight-task-capabilities.md) | PaaS 通道下任务策略的送达边界：立即/单次定时可用，重复定时、连续执行、蛙跳、手飞在协议层没有承载位 |
