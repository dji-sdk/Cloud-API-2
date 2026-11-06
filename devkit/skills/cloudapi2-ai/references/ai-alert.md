# AiAlertModule 参考（AI 告警管理，7 方法）

## 模块位置与心智模型

```java
// ✅ 挂在 Workspace 上（不是 Drone）
AiAlertModule aiAlert = workspace.getAIAlertModule();
if (aiAlert == null) {
    // 底层模块为 null 会记 Logger.error，必须判空
    return;
}
```

源码（`Workspace.java`）：

```java
public synchronized AiAlertModule getAIAlertModule() {
    if (aiAlertModuleRef != null) { return aiAlertModuleRef; }
    IAIAlertModule raw = workspace.getAIAlertModule();
    if (raw == null) {
        Logger.error(TAG, "getAIAlertModule error: module is null");
        return null;
    }
    aiAlertModuleRef = new AiAlertModule(raw);
    return aiAlertModuleRef;
}
```

与 `AIInsideManageModule` 同样挂在 `Workspace` 上，判空行为一致（底层为 null 会记日志）。

### 头号陷阱：HTTP 路径前缀是 `/drc/paas/v2/ai-alert/*`，但本模块**不依赖 DRC 链路**

本模块全部 6 个业务方法的 `@implNote` 路径都在 `/drc/paas/v2/` 命名空间下，容易让人误以为需要先
`drone.getDrcModule().connectDrcLink(...)` 建链。实际上这 6 个方法全部是**纯 HTTP 调用**（走 `Workspace` 的常规鉴权通道），
与 `DrcModule` 没有调用依赖关系——路径前缀只是后端 API 分组历史遗留，不是传输链路的信号。
不要因为看到 `/drc/` 就加上 `connectDrcLink` 前置校验。

## 方法总览

| # | 方法 | 参数 | HTTP |
|---|---|---|---|
| 1 | `postDeviceSetting` | `PostAiAlertDeviceSettingParam`（drone_sn、enable_target_alert、alarm_info） | `POST /drc/paas/v2/ai-alert/device-setting` |
| 2 | `getDeviceSetting` | `GetAiAlertDeviceSettingParam`（drone_sn） | `GET /drc/paas/v2/ai-alert/device-setting` |
| 3 | `postAlgorithmSetting` | `PostAiAlertAlgorithmSettingParam`（drone_sn、algorithm_type、algorithm_name、alarm_targets） | `POST /drc/paas/v2/ai-alert/algorithm-setting` |
| 4 | `getAlgorithmSetting` | `GetAiAlertAlgorithmSettingParam`（drone_sn、algorithm_type） | `GET /drc/paas/v2/ai-alert/algorithm-setting` |
| 5 | `getAiAlertFlights` | `GetAiAlertFlightsParam`（drone_sn、begin_at、end_at、algorithm_sources、page、page_size） | `GET /drc/paas/v2/ai-alert/flights` |
| 6 | `getAiAlertRecords` | `GetAiAlertRecordsParam`（flight_id 列表、drone_sn 过滤、target_type、algorithm_sources、时间范围、分页） | `GET /drc/paas/v2/ai-alert/records` |
| 7 | `destroy` | 无参 | 本地清理，非网络调用 |

全部 6 个业务方法回调都是 **(FlightHubError, ResultBean)** 两参数，没有裸错误回调（与 `AIInsideManageModule.deleteAIInsideAlgorithm` 不同）。

## `destroy()` 生命周期方法

```java
@SuppressWarnings("unchecked")
public void destroy() {
    // 清空 6 个 ConcurrentHashMap：postDeviceSettingRef / getDeviceSettingRef /
    // postAlgorithmSettingRef / getAlgorithmSettingRef / getFlightsRef / getRecordsRef
}
```

应在 `Workspace` 销毁时调用。

## 两层配置：设备级 + 算法级

### 设备级开关（是否启用目标检测告警）

```java
PostAiAlertDeviceSettingParam param = new PostAiAlertDeviceSettingParam();
param.setDroneSn(droneSn);
param.setEnableTargetAlert(true);
param.setAlarmInfo(alarmInfo);               // AiAlertAlarmInfo：告警方式与间隔
aiAlert.postDeviceSetting(param, (error, result) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) { return; }
});
```

**字段 getter 陷阱**：`enableTargetAlert` 是 `boolean` 字段，getter 是 **`isEnableTargetAlert()`**（标准 boolean 命名规范），
不是 `getEnableTargetAlert()`——按常规驼峰猜测会编译不过。

```java
GetAiAlertDeviceSettingParam getParam = new GetAiAlertDeviceSettingParam();
getParam.setDroneSn(droneSn);
aiAlert.getDeviceSetting(getParam, (error, result) -> {
    boolean enabled = result.isEnableTargetAlert();   // 同样是 is 前缀
});
```

### 算法级阈值（按算法类型 Upsert）

```java
PostAiAlertAlgorithmSettingParam param = new PostAiAlertAlgorithmSettingParam();
param.setDroneSn(droneSn);
param.setAlgorithmType(algorithmType);
param.setAlgorithmName(algorithmName);
param.setAlarmTargets(alarmTargets);
aiAlert.postAlgorithmSetting(param, (error, result) -> { ... });
```

`postAlgorithmSetting` 是 Upsert 语义（存在则更新、不存在则创建），没有单独的 create/update 两个方法。

## 查询告警架次与明细（两段式，不要混用）

```java
// 第一步：查询"哪些飞行架次产生过告警"（按架次聚合统计）
GetAiAlertFlightsParam flightsParam = new GetAiAlertFlightsParam();
flightsParam.setDroneSn(droneSn);
flightsParam.setBeginAt(beginAt);
flightsParam.setEndAt(endAt);
aiAlert.getAiAlertFlights(flightsParam, (error, result) -> {
    // result 含 flight_id 列表
});

// 第二步：按 flight_id 查询具体告警明细记录
GetAiAlertRecordsParam recordsParam = new GetAiAlertRecordsParam();
recordsParam.setFlightId(flightIdList);      // 字段名是单数 flightId，但类型是 List<String>
aiAlert.getAiAlertRecords(recordsParam, (error, result) -> { ... });
```

`getAiAlertFlights` 返回的是架次级统计（这次飞行有没有告警），`getAiAlertRecords` 才是具体的告警明细条目（按 `flight_id` 分组）——
不要试图从 `getAiAlertFlights` 的结果里直接拿到告警详情字段，必须用它拿到的 `flight_id` 再查一次 `getAiAlertRecords`。

**字段命名陷阱**：`GetAiAlertRecordsParam.flightId` 是 `List<String>` 类型但字段名是单数 `flightId`（不是 `flightIds`/`flightIdList`），
访问器是 `getFlightId()`/`setFlightId(List<String>)`；`targetType` 同样是 `List<Integer>`（0=人/1=车/2=船），字段名也是单数。

## 常见错误清单

1. ❌ 看到 `/drc/paas/v2/ai-alert/*` 路径就加上 `connectDrcLink` 前置校验——本模块纯 HTTP，不依赖 DRC 链路
2. ❌ 调用 `getEnableTargetAlert()`——正确是 `isEnableTargetAlert()`（boolean getter 规范）
3. ❌ 给 `postAlgorithmSetting` 找单独的 create/update 方法——它本身就是 Upsert
4. ❌ 把 `getAiAlertFlights` 当成能直接拿告警明细——它只是架次聚合，明细要查 `getAiAlertRecords`
5. ❌ 忘记在 `Workspace` 销毁时调用 `destroy()`
6. ❌ 在 `Drone` 上找 `getAIAlertModule()`——该方法只在 `Workspace` 上
