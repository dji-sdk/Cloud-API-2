# 设备生命周期

DJI Cloud API 2 SDK 中的设备生命周期由 kernel-spi 事件驱动，集成商通过 `@EventListener` 消费。

## 生命周期 6 步

```
1. 设备通电/开机
   ↓
2. SDK 连接建立 → DeviceOnlineEvent
   - deviceSn: 设备序列号（**注意**：SDK 原生回调不带 SN，是订阅时用的 `Drone`/`Dock` 实例自带的——这里的 `deviceSn` 是集成商事件层用闭包变量补上的，不是 SDK 推送字段）
   ↓
   SDK 原生推送只有一个布尔字段：
   - `drone.getDeviceStateModule().listenDroneOnlineStatus(cb)` → `DroneOnlineStatus.isOnline()`
   - `dock.getDeviceStateModule().listenDockOnlineStatus(cb)` → `DockOnlineStatus.isOnline()`
   > ⚠️ **没有 `deviceType`/`deviceModel`/`deviceName` 字段**——这些不是 SDK 推送内容。
   > 如果集成商事件层要带上型号/名称，必须自己额外调用
   > `workspace.getDeviceManageModule().getDeviceDetailInfo(sn, cb)` 查询后拼装，
   > 不要假设在线事件本身会带这些信息。

3. OSD 数据推送 → OsdUpdateEvent（持续，1-5Hz）
   - deviceSn: 设备序列号
   - osd: OsdData（经纬度、高度、电量、速度、姿态...）
   ↓
4. HMS 告警 → HmsAlertEvent（偶发）
   - deviceSn: 设备序列号
   - alertCode: 告警代码
   - level: 告警等级
   - message: 告警消息
   ↓
5. SDK 连接断开 → DeviceOfflineEvent
   - deviceSn: 设备序列号
   ↓
6. 设备关机/断电
```

## 状态存储策略

skeleton 默认使用内存存储（`ConcurrentHashMap`），适用于单实例部署。

集成商需要持久化或多实例共享时，可：
1. 通过 `@ConditionalOnMissingBean` 替换 `DeviceStatusService`
2. 在自定义实现中使用 Redis / 数据库

## 设备 SN 唯一性

设备 SN 是全局唯一标识符，格式为大写字母+数字（如 `1ZNBJ790010XXX`）。
同一设备的所有事件通过 `deviceSn` 关联。

## 在线/离线判断

- `DeviceOnlineEvent` → 标记为在线
- `DeviceOfflineEvent` → 标记为离线（不删除 entry，保留 last known state）
- 长时间无 OSD 推送但未收到 offline 事件 → 可能网络中断，需结合业务判断

## 常见 DeviceType 枚举

| 值 | 含义 |
|----|------|
| DRONE | 无人机 |
| DOCK | 机场（机巢） |
| REMOTE_CONTROLLER | 遥控器 |

## 机场辅助设备控制（DockControlModule）

`DockControlModule` 挂在 **Dock** 上，覆盖机场摄像头切换、空调状态订阅、补光灯/充电/舱盖/空调的开关控制：

```java
DockControlModule dockControl = dock.getDockControlModule();
```

**9 个方法**：1 个摄像头位置切换 + 2 对 listen/cancel + 4 个纯设置方法（4 个均为 `(error)` 单参回调）。

### 1. 切换机场舱内/舱外摄像头

```java
dockControl.changeDockCameraPosition(liveInfo, DockCameraPosition.OUTSIDE, gatewaySn, error -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        return;
    }
});
```

`DockCameraPosition` 枚举：`INSIDE(0)` 舱内 / `OUTSIDE(1)` 舱外 / `AUX2(2)` / `UNKNOWN(65535)`。

### 2. 订阅机场空调状态

```java
int listenId = dockControl.listenDockAirConditionerStatusInfo(info -> {
    // ⚠️ 单参回调，没有 error 参数——与 listenDroneOfflineMapEnable 同构
    DockAirConditionerState state = info.getAirConditionerState();
    int switchTime = info.getSwitchTime();
});
// 退出时释放
dockControl.cancelListenDockAirConditionerStatusInfo(listenId);
```

> ⚠️ `DockAirConditionerState` 的枚举常量名**自带前缀**，不是裸名——实际写法是 `DockAirConditionerState.DockAirConditionerState_IDLE`，不是 `DockAirConditionerState.IDLE`。照抄裸名编译不过。

`DockAirConditionerState` 枚举（12 个取值）：`DockAirConditionerState_IDLE(0)` / `DockAirConditionerState_COOLING(1)` / `DockAirConditionerState_HEATING(2)` / `DockAirConditionerState_DEHUMIDIFYING(3)` / `DockAirConditionerState_COOLING_EXIT(4)` / `DockAirConditionerState_HEATING_EXIT(5)` / `DockAirConditionerState_DEHUMIDIFYING_EXIT(6)` / `DockAirConditionerState_COOLING_PREPARATION(7)` / `DockAirConditionerState_HEATING_PREPARATION(8)` / `DockAirConditionerState_DEHUMIDIFYING_PREPARATION(9)` / `DockAirConditionerState_INVALID(32767)` MCU 异常 / `DockAirConditionerState_UNKNOWN(65535)`。

### 3. 订阅机场补光灯状态

```java
int listenId = dockControl.listenDockSupplementLightState(state -> {
    // 同样单参回调，无 error
    int lightState = state.getSupplementLightState();   // int 原始值，非枚举
});
dockControl.cancelListenDockSupplementLightState(listenId);
```

> ⚠️ 本模块的补光灯（机场舱内照明）与 `FlashLedModule`（飞行器爆闪灯）是两个不同的设备部件，命名无关联，不要混淆。

### 4. 4 个纯设置方法

```java
dockControl.setDockSupplementLightState(true, error -> { /* ... */ });
dockControl.setDockChargeState(true, error -> { /* ... */ });
dockControl.setDockCoverState(DockCoverMode.OPEN, error -> { /* ... */ });
dockControl.setDockAirConditionerMode(1, error -> { /* ... */ });
```

`DockCoverMode` 枚举：`OPEN(0)` / `CLOSE(1)` / `FORCE_CLOSE(2)` / `UNKNOWN(65535)`。

### 常见错误

- ❌ `drone.getDockControlModule()` —— 本模块挂在 **Dock** 上，不是 Drone
- ❌ 以为 `listenDockAirConditionerStatusInfo`/`listenDockSupplementLightState` 的回调带 `error` 参数 —— 两者都是单参回调，订阅成败只看返回的 `listenId`
- ❌ 把补光灯状态 `getSupplementLightState()` 当枚举处理 —— 它是裸 `int`，没有配套的 value 枚举类
- ❌ 把本模块的"补光灯"和 `FlashLedModule` 的"爆闪灯"当成同一个部件的两种叫法
- ❌ 把 `DockAirConditionerState` 的枚举常量写成裸名（`IDLE`/`COOLING`...）—— 实际常量名自带前缀（`DockAirConditionerState_IDLE`/`DockAirConditionerState_COOLING`...），裸名编译不过
