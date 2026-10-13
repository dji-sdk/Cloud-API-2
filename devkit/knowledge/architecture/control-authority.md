---
title: "飞控权限模型"
module: architecture
tags: [control-authority, mutex, acquire, release, drc]
audience: [agent, developer]
complexity: intermediate
summary: "飞控权限互斥锁模型（acquire → operate → release）"
last_updated: 2026-08-05
sdk_version: "3.3.7"
related:
  - api-reference/flight-control.md
  - api-reference/core-rules.md
---

飞控权是一个全局互斥锁：同一台无人机同一时刻只能被一个操作方控制。理解这个模型是实现任何飞行控制功能的前提。

# 飞控权限模型

## 为什么是互斥锁

无人机飞行安全要求指令来源唯一。如果两个操作方同时下发相反指令（一个让飞、一个让停），结果不可预测。因此 DJI CloudAPI2 强制单一控制权持有者模式。

## 权限获取流程

```
ISV BFF                    DJI CloudAPI2 Platform              设备
  │                              │                          │
  │── acquireControlAuthority ──▶│                          │
  │                              │── 通知当前持有者释放 ──▶│
  │                              │◀── 释放确认 ────────────│
  │◀── SUCCESS ─────────────────│                          │
  │                              │                          │
  │── flyTo / takeoff / ... ────▶│── 转发指令 ────────────▶│
  │                              │                          │
  │── releaseControlAuthority ──▶│                          │
  │◀── SUCCESS ─────────────────│                          │
```

## DRC 链路

DRC (Direct Remote Control) 是飞控指令的低延迟通道。普通的 flyTo/takeoff 通过 HTTP + MQTT 下发（延迟 200-500ms），DRC 通过专用 WebSocket 长连接下发（延迟 50-100ms）。

**何时需要 DRC**：
- 手动摇杆控制（stick move）
- 实时避障响应
- POI 绕飞实时调参

**不需要 DRC 的场景**：
- takeoff / returnHome / flyTo（这些走 HTTP 通道）
- 航线任务（设备端自主执行）

## 编程模板

```java
// ✅ 正确：try-finally 保证释放
auth.acquireFlightControlAuthority((data, error) -> {
    if (error.getErrorCode() != SUCCESS) {
        handleError(error);
        return;
    }
    try {
        executeFlightCommand(drone);
    } finally {
        auth.releaseFlightControlAuthority((d, e) -> {
            if (e.getErrorCode() != SUCCESS) {
                log.warn("释放控制权失败: {}", e.getErrorCode());
            }
        });
    }
});

// ❌ 错误：忘记释放 → 其他操作方被永久阻塞（直到超时）
auth.acquireFlightControlAuthority((data, error) -> {
    executeFlightCommand(drone);
    // 没有 release！
});
```

## 常见问题

| 现象 | 原因 | 解决 |
|------|------|------|
| acquire 返回 SUCCESS 但指令无响应 | 需要先建立 DRC 链路 | 调用 `connectDrc()` 后再发指令 |
| 其他操作方报"权限被抢占" | 你 acquire 了但没用完就 release | 合理规划控制权持有时间 |
| acquire 超时 | 当前持有者未主动释放 | 等待平台强制超时释放（默认 30s） |
