# 设备调试与运维控制（DeviceDebugModule）

`DeviceDebugModule` 是方法数最多的设备级模块之一（**59 个公开方法**），覆盖机场/飞行器调试模式、舱盖开关、设备重启/格式化、充电开关、限飞/限高/避障等飞控参数、eSIM/SIM 切换、电池仓/换电、Geo Caging 围栏、云环境切换等一长串运维能力。集成商日常业务很少整体用到，大多是按需调用其中 1~2 个方法，故本文档采用"广度优先、按功能分组"的组织方式。

## 模块位置与心智模型

```java
DeviceDebugModule debug = drone.getDeviceDebugModule();   // 或 dock.getDeviceDebugModule()
```

两个 host 的 getter（`Drone.getDeviceDebugModule()` / `Dock.getDeviceDebugModule()`）都是**静默返回 null**——底层模块缺失时既不记日志也不抛异常，仅 `if (module != null) {...}` 判断后直接返回 `null`。**这与 `Workspace` 上的长尾运维模块（`LogManagementModule`/`MaintenanceManagementModule`/`DiagnosisManagementModule`，见 `ops-reporting.md`）形成鲜明对比**——后者在同样情形下会 `Logger.error(...)` 记日志。调用前必须判空，且判空失败时不要指望日志能帮上忙。

所有参数方法的设备序列号参数命名是 `device_sn`（下划线命名，而非项目其余模块惯用的 `deviceSn`），这是该模块方法签名的真实写法，照抄即可。

## 命名一致性陷阱（必读）

1. **"subscribe" 与 "listen" 混用**：本模块绝大多数进度订阅遵循 `subscribeXxxProgress`/`unSubscribeXxxProgress` 命名（**11 对，22 个方法**），但云环境切换进度这一对**例外**，使用 `listenDeviceCloudEnvironmentSwitchProgress`/`cancelListenDeviceCloudEnvironmentSwitchProgress`（**1 对，2 个方法**）。两套命名风格在同一模块内并存，生成代码时不要把二者搞混或臆测第 12 对也叫 `subscribeXxx`。
2. **`deviceFormat` 与 `droneFormat` 是两个独立操作**：`deviceFormat`/`getDeviceFormatProgress`/`subscribeDeviceFormatProgress`/`unSubscribeDeviceFormatProgress`（通用设备级格式化）与 `droneFormat`/`subscribeDroneFormatProgress`/`unSubscribeDroneFormatProgress`（飞行器专属格式化）命名高度相似但是两套独立流程，**不要**以为调用其中一个就覆盖了另一个的效果。
3. **`getXxxProgress`（主动查询一次性快照）与 `subscribeXxxProgress`（订阅持续推送）是两种不同机制**，部分操作（舱盖开/关、设备重启/格式化）同时提供两种方式；eSIM 切换提供 `getEsimOperatorSwitchProgress` + `subscribeDeviceEsimOperatorSwitchProgress` 两种（注意两处命名前缀不同）；SIM 卡槽切换则**只有** `subscribeDeviceSimSlotSwitchProgress`，没有对应的 `getXxxProgress`——调用前确认该操作是否真的有主动查询方法。

---

## 1. 调试模式

```java
debug.openDebugMode(deviceSn, (error, result) -> { /* OpenDebugModeResult */ });
debug.closeDebugMode(deviceSn, (error, result) -> { /* CloseDebugModeResult */ });
```

## 2. 机场舱盖开关（开关 + 主动查询进度 + 订阅进度）

```java
debug.openDockCover(deviceSn, (error, result) -> { /* OpenDockCoverResult */ });
debug.getOpenDockCoverProgress(deviceSn, (error, result) -> { /* OpenDockCoverProgressResult，一次性快照 */ });
int listenId = debug.subscribeDockCoverOpenProgress(deviceSn, progressResult -> {
    // ⚠️ 单参回调，无 error——订阅类回调统一模式
});
debug.unSubscribeDockCoverOpenProgress(listenId);

debug.closeDockCover(deviceSn, (error, result) -> { /* CloseDockCoverResult */ });
debug.getCloseDockCoverProgress(deviceSn, (error, result) -> { /* CloseDockCoverProgressResult */ });
int closeListenId = debug.subscribeDockCoverCloseProgress(deviceSn, progressResult -> { /* ... */ });
debug.unSubscribeDockCoverCloseProgress(closeListenId);
```

## 3. 设备重启 / 格式化（通用设备级）

```java
debug.deviceReboot(deviceSn, (error, result) -> { /* DeviceRebootResult */ });
debug.getDeviceRebootProgress(deviceSn, (error, result) -> { /* DeviceRebootProgressResult */ });
int rebootListenId = debug.subscribeDeviceRebootProgress(deviceSn, progressResult -> { /* ... */ });
debug.unSubscribeDeviceRebootProgress(rebootListenId);

debug.deviceFormat(deviceSn, (error, result) -> { /* DeviceFormatResult */ });
debug.getDeviceFormatProgress(deviceSn, (error, result) -> { /* DeviceFormatProgressResult */ });
int formatListenId = debug.subscribeDeviceFormatProgress(deviceSn, progressResult -> { /* ... */ });
debug.unSubscribeDeviceFormatProgress(formatListenId);
```

> ⚠️ `deviceReboot`/`deviceFormat` 均为不可逆/中断性操作，生成代码时应提示二次确认，与飞控类危险操作同等对待。`deviceFormat` 会清空设备存储，不要在没有明确用户意图时调用。

## 4. 飞行器开关机 / 充电开关

```java
debug.openDrone(deviceSn, (error, result) -> { /* OpenDroneResult */ });
int droneOpenListenId = debug.subscribeDroneOpenProgress(deviceSn, progressResult -> { /* ... */ });
debug.unSubscribeDroneOpenProgress(droneOpenListenId);

debug.closeDrone(deviceSn, (error, result) -> { /* CloseDroneResult */ });
int droneCloseListenId = debug.subscribeDroneCloseProgress(deviceSn, progressResult -> { /* ... */ });
debug.unSubscribeDroneCloseProgress(droneCloseListenId);

debug.openCharge(deviceSn, (error, result) -> { /* OpenChargeResult */ });
int chargeOpenListenId = debug.subscribeDroneChargeOpenProgress(deviceSn, progressResult -> { /* ... */ });
debug.unSubscribeDroneChargeOpenProgress(chargeOpenListenId);

debug.closeCharge(deviceSn, (error, result) -> { /* CloseChargeResult */ });
int chargeCloseListenId = debug.subscribeDroneChargeCloseProgress(deviceSn, progressResult -> { /* ... */ });
debug.unSubscribeDroneChargeCloseProgress(chargeCloseListenId);
```

> 注意：`openDrone`/`closeDrone` 只提供 `subscribeXxxProgress` 订阅方式，**没有** `getXxxProgress` 主动查询方法——与舱盖/重启/格式化不同，不要凭空臆造一个 `getOpenDroneProgress`。

## 5. 环境/模式设置（单参 error-only 回调）

```java
debug.setDockAirConditionerMode(deviceSn, 1, error -> { /* ResultCallbackWithError */ });
debug.setDockSilentMode(deviceSn, 1, error -> { /* ResultCallbackWithError */ });
debug.droneSdrWorkMode(deviceSn, 1, error -> { /* ResultCallbackWithError */ });
```

> `setDockAirConditionerMode` 在本模块与 `DockControlModule`（见 `device-lifecycle.md`）中各有一份，前者多一个 `deviceSn` 参数（设备级），后者没有（挂在具体 `Dock` 实例上，隐式绑定设备），两者不是同一方法的重复声明，签名不同。

## 6. 限飞限高避障等飞控参数（均为 `(error, result)` 双参回调）

```java
debug.setDistanceLimit(deviceSn, 1, 500, (error, result) -> { /* SetDistanceLimitResult */ });   // state + value(米)
debug.setHeightLimit(deviceSn, 120, (error, result) -> { /* SetHeightLimitResult */ });            // 米
debug.setObstacleAvoidance(deviceSn, 1, (error, result) -> { /* SetObstacleAvoidanceResult */ });
debug.setRemainingPowerForRth(deviceSn, 20, (error, result) -> { /* SetRemainingPowerForRthResult */ });  // 百分比
debug.setNightLightsState(deviceSn, 1, (error, result) -> { /* SetNightLightsStateResult */ });
debug.switchAlarmLightState(deviceSn, 1, (error, result) -> { /* SwitchAlarmStateResult，注意返回类型名没有 "Light" */ });
```

> ⚠️ `switchAlarmLightState` 方法名里有 `Light`，但返回结果类型是 `SwitchAlarmStateResult`（不是 `SwitchAlarmLightStateResult`）——方法名与结果类型命名不对称，照抄源码签名，不要按规律臆造结果类型名。

## 7. eSIM / SIM 切换（+ 对应的进度订阅，无主动查询方法）

```java
debug.esimOperatorSwitch(deviceSn, imei, 1, deviceType, (error, result) -> { /* EsimOperatorSwitchResult */ });
int esimListenId = debug.subscribeDeviceEsimOperatorSwitchProgress(deviceSn, progressResult -> { /* ... */ });
debug.unSubscribeDeviceEsimOperatorSwitchProgress(esimListenId);
// getEsimOperatorSwitchProgress 是"主动查询一次性快照"版本，与订阅版本并存
debug.getEsimOperatorSwitchProgress(deviceSn, (error, result) -> { /* EsimOperatorSwitchProgressResult */ });

debug.simSlotSwitch(deviceSn, imei, 1, deviceType, (error, result) -> { /* SimSlotSwitchResult */ });
int simListenId = debug.subscribeDeviceSimSlotSwitchProgress(deviceSn, progressResult -> { /* ... */ });
debug.unSubscribeDeviceSimSlotSwitchProgress(simListenId);
```

> `esimOperatorSwitch`/`simSlotSwitch` 都带 `imei` 与 `device_type`（String）两个额外参数，不要只传 `deviceSn` + 开关值就调用——编译不会报错（参数类型都匹配），但服务端会因缺少 IMEI/设备类型上下文而拒绝请求。

## 8. 电池仓 / 手动换电 / 低电量开关

```java
debug.setBatteryStoreMode(deviceSn, 1, (error, result) -> { /* SetBatteryStoreModeResult */ });
debug.startManualBatterySwap(deviceSn, slotId, (error, result) -> { /* ManualBatterySwapResult */ });
debug.getManualBatterySwapProgress(deviceSn, (error, result) -> { /* ManualBatterySwapProgressResult，一次性快照，本操作无 subscribe 版本 */ });
debug.setDonglePowerSwitch(deviceSn, 1, (error, result) -> { /* DonglePowerSwitchResult */ });
debug.setLowPowerSwitch(deviceSn, 1, (error, result) -> { /* LowPowerSwitchResult */ });
```

## 9. Geo Caging 围栏（飞行地理围栏 + 自定义电子围栏）

```java
GeoCagingInfoParam caging = new GeoCagingInfoParam();
caging.setFgLongitude(113.95);
caging.setFgLatitude(22.54);
caging.setFgRadius(500);     // 飞行地理围栏半径（米）
caging.setFgHeight(120);     // 飞行地理围栏高度（米）
caging.setCvLongitude(113.95);
caging.setCvLatitude(22.54);
caging.setCvRadius(300);     // 自定义电子围栏半径（米）
caging.setCvHeight(100);     // 自定义电子围栏高度（米）

debug.setGeoCagingEnable(deviceSn, 1, List.of(caging), error -> { /* ResultCallbackWithError，仅 error */ });
```

`GeoCagingInfoParam` 字段表（源码 javadoc 已明确两组字段含义，无需猜测）：

| 字段 | 类型 | 含义 |
|------|------|------|
| `fgLongitude`/`fgLatitude` | double | 飞行地理围栏经纬度 |
| `fgRadius`/`fgHeight` | int | 飞行地理围栏半径/高度（米） |
| `cvLongitude`/`cvLatitude` | double | 自定义电子围栏经纬度 |
| `cvRadius`/`cvHeight` | int | 自定义电子围栏半径/高度（米） |

`fg` = **飞行地理围栏**（Fly Geofence），`cv` = **自定义电子围栏**（Custom Virtual fence）——两组字段分别对应两种不同性质的围栏，不是同一围栏的两份冗余坐标。

## 10. 飞行器专属格式化 + SDR 工作模式

```java
debug.droneFormat(deviceSn, (error, result) -> { /* DroneFormatResult —— 与第 3 节 deviceFormat 是独立操作 */ });
int droneFormatListenId = debug.subscribeDroneFormatProgress(deviceSn, progressResult -> { /* DroneFormatResult */ });
debug.unSubscribeDroneFormatProgress(droneFormatListenId);

debug.droneSdrWorkMode(deviceSn, 1, error -> { /* ResultCallbackWithError */ });
```

## 11. 云环境切换（命名风格例外：listen/cancelListen，而非 subscribe/unSubscribe）

```java
CloudEnvironmentSwitchParam switchParam = new CloudEnvironmentSwitchParam();
// ... 设置目标云环境参数
debug.cloudEnvironmentSwitch(switchParam, error -> { /* ResultCallbackWithError */ });

CloudEnvironmentSwitchCallbackParam callbackParam = new CloudEnvironmentSwitchCallbackParam();
debug.cloudEnvironmentSwitchCallback(callbackParam, error -> { /* ResultCallbackWithError，设备侧切换完成后的回执接口 */ });

int cloudListenId = debug.listenDeviceCloudEnvironmentSwitchProgress(deviceSn, progressResult -> {
    // CloudEnvironmentSwitchResult，单参回调
});
debug.cancelListenDeviceCloudEnvironmentSwitchProgress(cloudListenId);
```

---

## 59 方法完整清单（按源码出现顺序）

```
openDebugMode / closeDebugMode
openDockCover / getOpenDockCoverProgress / closeDockCover / getCloseDockCoverProgress
deviceReboot / getDeviceRebootProgress
deviceFormat / getDeviceFormatProgress
openDrone / closeDrone
openCharge / closeCharge
setDockAirConditionerMode / setDockSilentMode
setDistanceLimit / setHeightLimit / setObstacleAvoidance / setRemainingPowerForRth
setNightLightsState / switchAlarmLightState
esimOperatorSwitch / simSlotSwitch
setBatteryStoreMode / startManualBatterySwap / getManualBatterySwapProgress
setDonglePowerSwitch / setLowPowerSwitch
getEsimOperatorSwitchProgress
setGeoCagingEnable
subscribeDockCoverOpenProgress / unSubscribeDockCoverOpenProgress
subscribeDockCoverCloseProgress / unSubscribeDockCoverCloseProgress
subscribeDeviceRebootProgress / unSubscribeDeviceRebootProgress
subscribeDeviceFormatProgress / unSubscribeDeviceFormatProgress
subscribeDroneOpenProgress / unSubscribeDroneOpenProgress
subscribeDroneCloseProgress / unSubscribeDroneCloseProgress
subscribeDroneFormatProgress / unSubscribeDroneFormatProgress
subscribeDroneChargeOpenProgress / unSubscribeDroneChargeOpenProgress
subscribeDroneChargeCloseProgress / unSubscribeDroneChargeCloseProgress
subscribeDeviceEsimOperatorSwitchProgress / unSubscribeDeviceEsimOperatorSwitchProgress
subscribeDeviceSimSlotSwitchProgress / unSubscribeDeviceSimSlotSwitchProgress
droneFormat
droneSdrWorkMode
cloudEnvironmentSwitch / cloudEnvironmentSwitchCallback
listenDeviceCloudEnvironmentSwitchProgress / cancelListenDeviceCloudEnvironmentSwitchProgress
```

共 59 个方法：11 对 `subscribeXxx`/`unSubscribeXxx`（22 个）+ 1 对 `listenXxx`/`cancelListenXxx`（2 个）+ 35 个非配对方法。

## 常见错误清单

1. ❌ `dock.getDeviceDebugModule()`/`drone.getDeviceDebugModule()` 返回 null 时不记日志——不要指望能在应用日志里查到"模块为空"的提示，必须显式判空处理
2. ❌ 把 `deviceFormat` 和 `droneFormat` 当成同一操作的两种叫法——二者是独立流程，互不替代
3. ❌ 把云环境切换进度订阅写成 `subscribeDeviceCloudEnvironmentSwitchProgress`——这一对是例外，实际方法名是 `listenDeviceCloudEnvironmentSwitchProgress`/`cancelListenDeviceCloudEnvironmentSwitchProgress`
4. ❌ 给 `openDrone`/`closeDrone` 臆造一个 `getOpenDroneProgress`/`getCloseDroneProgress` 主动查询方法——这两个操作只有订阅方式，没有主动查询
5. ❌ 把 `switchAlarmLightState` 的返回结果类型写成 `SwitchAlarmLightStateResult`——实际类型是 `SwitchAlarmStateResult`
6. ❌ 调用 `esimOperatorSwitch`/`simSlotSwitch` 时漏传 `imei`/`device_type` 参数
7. ❌ `deviceReboot`/`deviceFormat`/`droneFormat` 是不可逆/中断性操作，生成代码时必须提示二次确认
8. ❌ 把 `GeoCagingInfoParam` 的 `fg*` 和 `cv*` 字段当成同一围栏的冗余坐标——`fg` 是飞行地理围栏，`cv` 是自定义电子围栏，两组字段需要分别设置且语义不同

## 关联

- `DockControlModule`（挂在 Dock 上的机场辅助设备控制，含摄像头切换/空调/补光灯）见 `device-lifecycle.md`
- RTK 标定、网络测试等长尾运维模块见 `ops-reporting.md`
