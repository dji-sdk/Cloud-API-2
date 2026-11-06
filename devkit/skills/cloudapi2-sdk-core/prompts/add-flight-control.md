# 添加飞行控制功能

## 目标
实现手动飞行控制：起飞、返航、取消返航、急停、指点飞行。
> ⚠️ 协议中不存在独立的 `landing()` 接口；降落通过 `returnHome()` 实现（无人机返回机场后自动降落）。

## 涉及 SDK API
- `FlightHubSDK.getDroneInstance(sn)` → Drone
- `drone.getControlAuthModule().acquireFlightControlAuthority(callback)` — 获取控制权
- `drone.getControlAuthModule().releaseFlightControlAuthority(callback)` — 释放控制权
- `drone.getTakeoffModule().takeoff(params, callback)` — 起飞
- `drone.getFlightControlModule().returnHome(callback)` — 返航（无人机返回机场并自动降落）
- `drone.getFlightControlModule().returnHomeCancel(callback)` — 取消返航
- `drone.getFlightControlModule().drcEmergencyStop(callback)` — 急停悬停
- `drone.getFlightControlModule().drcEmergencyLanding(callback)` — 紧急降落
- `drone.getFlightControlModule().drcForceLanding(callback)` — 强制降落（不可逆）

## 必须遵守的规则
- ✅ 规则 4：**必须先 acquire，用完 release**（即使指令失败也要释放）
- ✅ 规则 5：getDroneInstance null 检查
- ✅ 规则 2：所有回调检查 errorCode

## 实现步骤

1. **创建 FlightControlService**
   - 封装 `safeFlightCommand` 通用模板（acquire → command → release）
   - 实现 takeoff / returnHome / returnHomeCancel / drcEmergencyStop / drcEmergencyLanding / drcForceLanding
   - 每个方法都使用通用模板，确保释放

2. **创建 Controller**
   - POST `/api/drone/{sn}/takeoff` — 起飞
   - POST `/api/drone/{sn}/return-home` — 返航
   - POST `/api/drone/{sn}/return-home/cancel` — 取消返航
   - POST `/api/drone/{sn}/emergency-stop` — 急停悬停
   - POST `/api/drone/{sn}/emergency-landing` — 紧急降落
   - POST `/api/drone/{sn}/force-landing` — 强制降落（不可逆）

3. **起飞参数（TakeoffParams）**
   - securityTakeoffHeight: 8~1500m
   - maxSpeed: >0 m/s
   - rthAltitude: 15~1500m（返航高度）
   - targetHeight: 2~10000m
   - targetLatitude / targetLongitude
   - outOfControlAction: `OutOfControlAction.Hover` / `OutOfControlAction.ReturnHome` / `OutOfControlAction.Continue`（枚举常量，注意大小写——不是 `HOVER`/`RETURN_HOME`/`CONTINUE`）

## ⚠️ 安全警告（输出前置条件——规则 6）

**在输出任何飞控代码之前**，必须先向用户输出以下警告块：

```
⚠️ **危险操作警告**
- 操作类型：[takeoff / returnHome / drcEmergencyStop / drcEmergencyLanding / drcForceLanding / flyToPoint]
- 不可逆性：飞控指令发出后立即执行，无法通过代码回滚
- 物理影响：此代码将控制真实无人机执行物理动作，操作失误可能导致坠机或财产损失
- 生产建议：务必实现前端二次确认弹窗（confirm dialog）；建议支持 dry_run 参数做预校验
```

如果用户已在本轮对话中确认知晓风险，可省略。**禁止跳过此步骤直接输出代码。**

## 常见错误
- ❌ 不调 acquireFlightControlAuthority 直接发指令
- ❌ 不通过 module getter 访问 API（如 `drone.returnHome()` 而非 `drone.getFlightControlModule().returnHome()`）
- ❌ release 放在 acquire 回调外面（异步执行顺序错误）
- ❌ 指令失败后不释放控制权（阻塞其他操作方）
- ❌ 没有前端确认步骤就直接执行飞控
- ❌ 实现 `landing()` 方法（该方法不存在于协议中）
- ❌ 直接输出飞控代码而不附带危险操作警告（违反规则 6）
- ❌ 使用旧版 `emergencyStop()` 方法（v3.3.x 已拆分为 drcEmergencyStop/drcEmergencyLanding/drcForceLanding）

---

## 飞行设置（FlightSettingsModule）

### 模块位置

```java
FlightSettingsModule settings = drone.getFlightSettingsModule();
```

挂在 **Drone** 上（标准 try/catch → null + log 判空模式，判空后直接返回错误，不要继续往下调）。

### 5 个方法

全部是**设置型指令**，没有对应的 get 查询方法——当前值需要从 OSD 遥测里读，本模块只负责「写」：

| 方法 | 参数 | 回调形态 |
|---|---|---|
| `setFlightSmartPlanMode(int mode, callback)` | 智能规划模式 | `(error, FlightSmartPlanModeResult)` |
| `setOutOfControlAction(String action, callback)` | 失控动作 | `(error, FlightOutOfControlActionResult)` |
| `setFlightTaskHeight(int height, callback)` | 任务高度 | `(error, FlightCommanderHeightResult)` |
| `setRthAltitude(int altitude, callback)` | 返航高度 | `(error, FlightRthAltitudeResult)` |
| `setAirTransferEnable(boolean enabled, callback)` | 空中回传开关 | **`(error)`，没有 result** |

> ⚠️ `setAirTransferEnable` 是本模块唯一一个 `(error)`-only 回调，其余 4 个都是 `(error, result)`——别套用同一个 lambda 签名。

```java
FlightSettingsModule settings = drone.getFlightSettingsModule();
if (settings == null) {
    return; // 设备不支持或底层模块缺失
}

settings.setRthAltitude(50, (error, result) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        return;
    }
    // result: FlightRthAltitudeResult
});

settings.setAirTransferEnable(true, error -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        return;
    }
    // 没有 result，成功即生效
});
```

### 常见错误
- ❌ 给 `setAirTransferEnable` 的回调多写一个 `result` 参数 —— 编译失败，它只有 `(error)`
- ❌ 以为本模块能读取当前飞行设置 —— 只有 setter，查询走 OSD 遥测订阅（见 `cloudapi2-sei-osd`）

---

## 指点飞行（FlyToPointModule）

### 模块位置

```java
FlyToPointModule flyToPoint = drone.getFlyToPointModule();
```

挂在 **Drone** 上（标准 try/catch → null + log 判空模式）。

### 9 个方法 —— 4 个已废弃，统一迁移到 listenFlyToTaskEvent

| 方法 | 状态 | 说明 |
|---|---|---|
| `start(FlyToPointParams params, callback)` | 当前 | 启动指点飞行 |
| `drcStop(callback)` | 当前，**⚠️ 见下方急停警告** | 停止指点飞行 |
| `updateTargetPoints(FlyToUpdateTargetPointsParams params, callback)` | 当前 | 更新目标点/航速 |
| `listenFlyToTaskEvent(callback)` / `cancelListenFlyToTaskEvent(listenId)` | 当前，**推荐** | 新事件流，WS topic `topic_atc_flyto_task_event` |
| `subscribeFlyToDroneSelfPlanPath(callback)` / `unSubscribeFlyToDroneSelfPlanPath(listenId)` | **`@Deprecated`** | 旧 WS topic `topic_drone_self_plan_path`，迁移到 `listenFlyToTaskEvent` |
| `subscribeFlyToPointProgress(callback)` / `unSubscribeFlyToPointProgress(listenId)` | **`@Deprecated`** | 旧 WS topic `topic_drone_flyto_point_progress`，迁移到 `listenFlyToTaskEvent` |

> ⚠️ **`drcStop` 不是"优雅停止"，是复用 DRC 急停指令**：源码 javadoc 原文「停止指点飞行（紧急停止）」，`@implNote` 标注链路为 **DRC | method: `drone_emergency_stop`**——调用它等同于对飞机下发急停，飞机会立即制动悬停，不是平滑收尾。生成调用代码前必须按本文件顶部的「危险操作警告」模板提示用户。

### 参数

```
FlyToPointParams { double latitude; double longitude; double height; int maxSpeed; }
FlyToUpdateTargetPointsParams { String flyToId = ""; FlyToTargetPoint startPoint; List<FlyToTargetPoint> points; int maxSpeed; }
```

### 示例

```java
FlyToPointModule flyToPoint = drone.getFlyToPointModule();
if (flyToPoint == null) {
    return;
}

FlyToPointParams params = new FlyToPointParams();
params.setLatitude(22.5431);
params.setLongitude(113.9426);
params.setHeight(100);
params.setMaxSpeed(8);

flyToPoint.start(params, (error, flyToId) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        return;
    }
    // 保存 flyToId，后续 updateTargetPoints 时需要
});

int listenId = flyToPoint.listenFlyToTaskEvent(event -> {
    // 单参数回调，没有 error；用新事件流即可覆盖旧的 subscribeFlyToDroneSelfPlanPath + subscribeFlyToPointProgress 两条旧订阅
});
// 用完 flyToPoint.cancelListenFlyToTaskEvent(listenId);
```

### 常见错误
- ❌ 把 `drcStop()` 当成普通的「取消指点飞行」按钮直接绑定，不附带急停警告 —— 它复用的是 DRC 急停指令
- ❌ 新代码里继续用 `subscribeFlyToDroneSelfPlanPath` / `subscribeFlyToPointProgress` —— 两者均 `@Deprecated`，统一改用 `listenFlyToTaskEvent`
- ❌ 以为两条旧订阅还需要并行维护 —— `listenFlyToTaskEvent` 已覆盖两者语义，迁移后可以都取消

---

## POI 环绕（PoiModule）

### 模块位置

```java
PoiModule poi = drone.getPoiModule();
```

挂在 **Drone** 上（标准 try/catch → null + log 判空模式）。全部走 HTTP（`/atc/api/v1/flight-tasks/poi-*`），事件/进度走 WS。

### 8 个方法

| 方法 | 回调形态 | 链路 |
|---|---|---|
| `enter(POIEnterParams params, callback)` | `(error, String taskUuid)` | HTTP `POST .../poi-enter` |
| `exit(callback)` | **`(error)`** —— `ResultCallback`，不是 `ResultCallbackWithError` | HTTP `DELETE .../poi-exit` |
| `updateSpeed(double speed, callback)` | **`(error)`** | HTTP `PUT .../poi-speed` |
| `getTask(callback)` | `(error, POITaskInfo)` | HTTP `GET .../poi-task` |
| `listenPoiTaskEvent(callback)` / `cancelListenPoiTaskEvent(listenId)` | 单参数，无 error | WS |
| `listenFlightPoiProgress(callback)` / `cancelListenFlightPoiProgress(listenId)` | 单参数，无 error，**⚠️ 见下方类型警告** | WS `topic_task_flight_poi_progress` |

> ⚠️ **`listenFlightPoiProgress` 直接透传原始 `generated.FlightPoiProgress`，不经 bean 转换**——源码里 `callback.onResult(msg)` 的 `msg` 类型就是 `com.dji.flighthubsdk.generated.FlightPoiProgress`，没有走 `FlightHubSdkConvert`。这是本模块里唯一一个把 `generated` 包原始类型直接暴露给应用层的监听，字段访问方式（getter 命名、snake_case）与其他已转换的 bean 不同，使用前确认拿到的是哪一种类型。
>
> 对比：`listenPoiTaskEvent` 走的是正常转换路径（`FlightHubSdkConvert.toPoiTaskEvent(event)`），只有 `listenFlightPoiProgress` 是例外。

`enter` / `exit` / `updateSpeed` 回调形态容易搞混——只有 `enter` 和 `getTask` 带 result，`exit` 和 `updateSpeed` 只有 `error`。

### 参数

```
POIEnterParams {
    String payloadIndex;       // 格式 "{type}-{subtype}-{gimbalindex}"，如 "81-0-0"
    Waypoint poiCenterPoint;   // 环绕中心点
    double speed;              // 环绕速度 m/s
    double circleRadius;       // 环绕半径
}
```

### 示例

```java
PoiModule poi = drone.getPoiModule();
if (poi == null) {
    return;
}

POIEnterParams params = new POIEnterParams();
params.setPayloadIndex("81-0-0");
params.setPoiCenterPoint(centerPoint);
params.setSpeed(3.0);
params.setCircleRadius(20.0);

poi.enter(params, (error, taskUuid) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        return;
    }
    // 保存 taskUuid 供查询/排错
});

poi.updateSpeed(5.0, error -> {
    // 只有 error，没有 result
});

poi.exit(error -> {
    // 只有 error，没有 result
});
```

> 错误码 325003（POI 中心点与自身位置重合 + 半径为 0）已在 `cloudapi2-troubleshooting/references/live-verification.md` 记录，排错时查该文档，不要在本文件重复定义。

### 常见错误
- ❌ 给 `exit` / `updateSpeed` 的回调多写一个 `result` 参数 —— 两者都是 `ResultCallback`，只有 `(error)`
- ❌ 把 `listenFlightPoiProgress` 回调里的 `msg` 当作已转换的 bean 用 camelCase getter 读取 —— 它是原始 `generated.FlightPoiProgress`
- ❌ 同时维护 `listenPoiTaskEvent` 和 `listenFlightPoiProgress` 却不清楚两者各自语义 —— 前者是任务级事件，后者是飞行进度，按需单独订阅，不是互斥关系
