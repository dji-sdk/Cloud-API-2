---
title: "飞控与权限管理"
module: api-reference
tags: [flight-control, control-authority, drc, acquire, release]
audience: [agent, developer]
complexity: intermediate
summary: "飞控权限 API、DRC 链路、安全执行模板"
last_updated: 2026-08-05
sdk_version: "3.3.7"
related:
  - api-reference/core-rules.md
  - architecture/control-authority.md
---

飞行控制权是互斥锁——同一时刻只有一个操作方可以控制一台无人机。必须 acquire → 操作 → release，即使失败也要释放。

# 飞控与权限

## 核心概念

DJI CloudAPI2 支持**多操作员并发**——多个 BFF 实例、DJI CloudAPI2 Web 控制台、遥控器均可控制同一台无人机。飞行控制权是**互斥锁**：

- 同一时刻只有一个操作方持有控制权
- 不持有控制权发送飞控指令 → 返回权限错误
- 持有后不释放 → 其他操作方被阻塞（直到超时自动释放）

## 飞控权 API

```java
// 通过 ControlAuthModule 访问飞控权能力
ControlAuthModule auth = drone.getControlAuthModule();

// 获取飞行控制权
auth.acquireFlightControlAuthority(ResultCallbackWithError callback)

// 释放飞行控制权
auth.releaseFlightControlAuthority(ResultCallbackWithError callback)

// 监听控制权状态变化（被抢占时通知）
int listenId = auth.listenFlightControlAuthority(isGranted -> {
    if (!isGranted) {
        log.warn("飞行控制权被其他操作方抢占");
    }
});
```

## 标准飞控操作模板

```java
/**
 * 安全执行飞控指令的通用模板。
 * 保证：抢权 → 执行 → 释放（无论成败）
 * 注：此方法名与 references/核心规则详解.md 中的 safeExecuteFlightCommand 等价，
 *     参数风格不同（此处用 Consumer 函数式风格），选择其中一种即可。
 */
public void safeFlightCommand(Drone drone, String commandName,
                               Consumer<ResultCallbackWithError> command,
                               Consumer<FlightHubError> onResult) {

    ControlAuthModule auth = drone.getControlAuthModule();
    auth.acquireFlightControlAuthority(acquireError -> {
        if (acquireError.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
            log.error("[{}] 获取控制权失败: {}", commandName, acquireError.getErrorCode());
            onResult.accept(acquireError);
            return;
        }

        command.accept(cmdError -> {
            // 无论成败都释放
            auth.releaseFlightControlAuthority(e -> {
                if (e.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
                    log.warn("[{}] 释放控制权异常: {}", commandName, e.getErrorCode());
                }
            });
            onResult.accept(cmdError);
        });
    });
}
```

## 使用示例

### 返航

```java
Drone drone = FlightHubSDK.getDroneInstance(sn);
if (drone == null) return;

safeFlightCommand(drone, "returnHome",
    callback -> drone.getFlightControlModule().returnHome(callback),
    error -> {
        if (error.getErrorCode() == FlightHubSdkErrorCode.SUCCESS) {
            log.info("返航指令发送成功");
        } else {
            log.error("返航失败: {}", error.getErrorCode());
        }
    });
```

### 起飞

```java
TakeoffParams params = new TakeoffParams();
params.setSecurityTakeoffHeight(30.0);   // 安全起飞高度 8~1500m
params.setMaxSpeed(10);                   // 最大速度 m/s
params.setRthAltitude(50);               // 返航高度 15~1500m
params.setTargetHeight(100.0);           // 目标高度 2~10000m
params.setTargetLatitude(22.5431);       // 目标纬度
params.setTargetLongitude(113.9473);     // 目标经度
params.setOutOfControlAction(OutOfControlAction.RETURN_HOME);
params.setTakeoffMode(TakeoffMode.TAKEOFF);

ControlAuthModule auth = drone.getControlAuthModule();
auth.acquireFlightControlAuthority(acquireError -> {
    if (acquireError.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) return;

    drone.getTakeoffModule().takeoff(params, (error, result) -> {
        auth.releaseFlightControlAuthority(e -> {});
        if (error.getErrorCode() == FlightHubSdkErrorCode.SUCCESS) {
            log.info("起飞成功, flightId={}", result.getFlightId());
        }
    });
});
```

### 急停（三种模式）

v3.1.0 将急停拆分为三个独立方法，根据场景选择：

| 方法 | 效果 | 适用场景 |
|------|------|---------|
| `drcEmergencyStop(cb)` | 立即悬停，螺旋桨保持运转 | 需要暂停但可恢复 |
| `drcEmergencyLanding(cb)` | 原地紧急降落 | 紧急但需安全着陆 |
| `drcForceLanding(cb)` | 强制降落，无视地形 | 极端紧急，不可逆 |

```java
// 急停悬停（最常用）
safeFlightCommand(drone, "drcEmergencyStop",
    callback -> drone.getFlightControlModule().drcEmergencyStop(callback),
    error -> log.info("急停悬停结果: {}", error.getErrorCode()));

// 紧急降落
safeFlightCommand(drone, "drcEmergencyLanding",
    callback -> drone.getFlightControlModule().drcEmergencyLanding(callback),
    error -> log.info("紧急降落结果: {}", error.getErrorCode()));
```

## 需要飞行控制权的操作

| 操作 | 模块 | 方法 | 说明 |
|------|------|------|------|
| 起飞 | `getTakeoffModule()` | `takeoff(params, cb)` | 需要 TakeoffParams |
| 返航 | `getFlightControlModule()` | `returnHome(cb)` | — |
| 取消返航 | `getFlightControlModule()` | `returnHomeCancel(cb)` | — |
| 急停悬停 | `getFlightControlModule()` | `drcEmergencyStop(cb)` | — |
| 紧急降落 | `getFlightControlModule()` | `drcEmergencyLanding(cb)` | — |
| 强制降落 | `getFlightControlModule()` | `drcForceLanding(cb)` | 不可逆 |
| 指点飞行 | `getFlyToPointModule()` | `start(params, startCallback)` | FlyToPointStartCallback；停止用 `drcStop(cb)` |

## 不需要飞行控制权的操作

| 操作 | 说明 |
|------|------|
| `getDroneOsdModule().listenXxx` / `cancelListenXxx` | 监听类，只读 |
| `getDeviceManageModule().getDeviceList` | 查询类 |
| 相机操作（拍照/录像） | 需要 `acquirePayloadControlAuthority`，不是飞行控制权 |
| 负载控制（喊话器/探照灯） | 需要 `acquirePayloadControlAuthority` |

## 负载控制权（Payload）

相机、喊话器、探照灯等负载有独立的控制权：

```java
ControlAuthModule auth = drone.getControlAuthModule();

// 获取负载控制权（payloadIndex 如 "81-0-0"）
auth.acquirePayloadControlAuthority(payloadIndex, error -> { ... });

// 释放
auth.releasePayloadControlAuthority(payloadIndex, error -> { ... });
```

## 被抢占的处理

```java
ControlAuthModule auth = drone.getControlAuthModule();
int authListenId = auth.listenFlightControlAuthority(isGranted -> {
    if (!isGranted) {
        log.warn("控制权被抢占，停止当前操作");
        // 通知前端：控制权已丢失
        // 清理进行中的飞控状态
    }
});
// 记得 cancel
subscriptionManager.track(sn, () -> auth.cancelListenFlightControlAuthority(authListenId));
```
