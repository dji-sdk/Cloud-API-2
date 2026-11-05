# 长尾运维模块（Log / Maintenance / Diagnosis / RtkCalibration / NetworkTest）

本文档覆盖 5 个集成商低频使用的运维类模块：`LogManagementModule`（日志管理，7 方法）、`MaintenanceManagementModule`（维修工单，4 方法）、`DiagnosisManagementModule`（设备自诊断，4 方法）、`RtkCalibrationModule`（RTK 标定，4 方法）、`NetworkTestModule`（网络测试，5 方法）。共 24 个方法。

## 模块归属与 null 安全行为对比

**前 3 个模块挂在 Workspace 上，后 2 个模块挂在 Dock/BaseStation/Relay 上**，且两组的 null 安全行为完全相反：

| 模块 | 挂载对象 | 模块为 null 时的行为 |
|---|---|---|
| `LogManagementModule` | `Workspace` | **记日志**：`Logger.error(TAG, "getLogManagementModule error: module is null")` |
| `MaintenanceManagementModule` | `Workspace` | **记日志**：同上模式 |
| `DiagnosisManagementModule` | `Workspace` | **记日志**：同上模式 |
| `RtkCalibrationModule` | `Dock` / `BaseStation` / `Relay` | **静默返回 null**，无日志 |
| `NetworkTestModule` | `Dock` / `BaseStation` | **静默返回 null**，无日志 |

> ⚠️ 这与 `DeviceDebugModule`（挂在 Drone/Dock 上，也是静默返回 null，见 `device-debug.md`）行为一致——**判断规律是"Workspace 级模块记日志，设备级模块静默"**，不是随机的。生成防御性代码时，Workspace 级模块判空失败后可以提示"检查应用日志"，设备级模块判空失败后不能这样提示。

---

## 1. LogManagementModule（日志管理）

```java
Workspace workspace = FlightHubSDK.getWorkspaceInstance();
LogManagementModule logModule = workspace.getLogManagementModule();
```

```java
logModule.queryDeviceLogList(deviceSn, List.of(1, 2), (error, result) -> {
    // result.getFiles() -> List<DeviceLogListResultFileItem>
});

CreateDeviceLogReportRequestParam reportParam = new CreateDeviceLogReportRequestParam();
reportParam.setErrorStartTime(errorTimestampMs);
reportParam.setIsCrashed(false);
reportParam.setAppendixFiles(List.of());
reportParam.setLogList(List.of());
reportParam.setDomains(List.of());
logModule.createDeviceLogReport(reportParam, (error, result) -> {
    // result.getReportUuid() / result.getTrackNumber()
});

logModule.logReportList(deviceSn, 1, 20, (error, result) -> {
    // result.getPagination() / result.getList()
});

logModule.logReportOperation("cancel", reportId, error -> { /* 仅 error */ });

logModule.uploadLogAppendFile(localFilePath, error -> { /* 仅 error */ });

int listenId = logModule.listenDeviceFileUploadProgress(deviceSn, progressMsg -> {
    // 单参回调，generated.DeviceFileUploadProgress，无 error
});
logModule.cancelListenDeviceFileUploadProgress(listenId);
```

`CreateDeviceLogReportRequestParam` 字段表：

| 字段 | 类型 | 说明 |
|---|---|---|
| `reportInformation` | String | 报告说明文本 |
| `errorStartTime` | long | 错误起始时间（毫秒时间戳） |
| `deviceTopo` | DeviceLogReportDeviceTopoParam | 设备拓扑信息 |
| `reportContactInformation` | DeviceLogReportContactInformationParam | 联系人信息 |
| `appendixFiles` | List\<String\> | 附加文件列表 |
| `logList` | List\<DeviceLogReportLogListItemParam\> | 日志条目列表 |
| `domains` | List\<DeviceLogReportDomainItemParam\> | 日志域列表 |
| `isCrashed` | boolean | 是否崩溃触发 |

> `logReportOperation` 的 `operation` 是裸 `String`（javadoc 仅说明"取消、删除等"，未见枚举类约束取值），生成代码时按文档/实际接口约定传入字符串字面量，不要臆造一个不存在的 `LogReportOperation` 枚举类型。

---

## 2. MaintenanceManagementModule（维修工单）

```java
MaintenanceManagementModule maintenanceModule = workspace.getMaintenanceManagementModule();
```

```java
GetDeviceRepairConfigParams configParams = new GetDeviceRepairConfigParams();
configParams.setDeviceSns(List.of(deviceSn));
maintenanceModule.getRepairConfigs(configParams, (error, result) -> {
    // result.getData() -> List<DeviceRepairConfig>
});

CreateMaintenanceOrderParams orderParams = new CreateMaintenanceOrderParams();
orderParams.setFaultOccurrenceTime(isoTimestamp);
orderParams.setFaultDescription("电机异常噪音");
orderParams.setContactPerson("张三");
orderParams.setContactPhone("13800000000");
orderParams.setContactEmail("zhangsan@example.com");
orderParams.setDeviceList(List.of(deviceItem));
maintenanceModule.createMaintenanceOrder(orderParams, (error, result) -> {
    // result.isSuccess() / result.getGeneratedOrders()
});

maintenanceModule.getMaintenanceOrderDetail(orderNumber, (error, result) -> {
    // result 字段众多：orderNumber/status/statusText/faultDescription/attachments/statusHistory/repairInfo...
});

GetMaintenanceOrdersListParams listParams = new GetMaintenanceOrdersListParams();
listParams.setPage(1);
listParams.setPageSize(20);
maintenanceModule.getMaintenanceOrdersList(listParams, (error, result) -> {
    // result.getPagination() / result.getList()
});
```

`CreateMaintenanceOrderParams` 关键字段：`faultOccurrenceTime`/`deviceList`/`faultDescription`/`attachments`/`contactPerson`/`contactPhone`/`contactEmail`/`contactAddress`/`expectedServiceTime`/`reportUuid`（可关联自诊断报告）/`deviceTopo`/`logList`。

`GetMaintenanceOrdersListParams` 支持的查询条件：`page`/`pageSize`（默认 100）/`orderNumber`/`status`/`initiatedFrom`/`deviceSn`/`creatorIds`/`keyWord`/`createdAtStart`/`createdAtEnd`/`sortColumn`/`sortType`。

---

## 3. DiagnosisManagementModule（设备自诊断）

```java
DiagnosisManagementModule diagnosisModule = workspace.getDiagnosisManagementModule();
```

```java
CreateSelfDiagnosisParams createParams = new CreateSelfDiagnosisParams();
createParams.setDeviceSn(deviceSn);
createParams.setType(1);
createParams.setPart(1);
createParams.setIndex(0);
createParams.setMotorAction(0);
diagnosisModule.createSelfDiagnosis(createParams, error -> { /* 仅 error，拿不到 reportUuid */ });

OperateSelfDiagnosisParams operateParams = new OperateSelfDiagnosisParams();
operateParams.setReportUuid(reportUuid);
operateParams.setReportCmdMethod("cancel");
diagnosisModule.operateSelfDiagnosis(operateParams, error -> { /* 仅 error */ });

diagnosisModule.getSelfDiagnosisReport(reportUuid, (error, result) -> {
    // result.getStatus() / result.getSelfDiagnosticResult() / result.getDeviceReportProgress()
});

GetSelfDiagnosisReportListParams listParams = new GetSelfDiagnosisReportListParams();
listParams.setPage(1);
listParams.setPageSize(20);
listParams.setDeviceSn(deviceSn);
diagnosisModule.getSelfDiagnosisReportList(listParams, (error, result) -> {
    // result.getPagination() / result.getList() -> List<GetSelfDiagnosisReportResults>
});
```

> ⚠️ `createSelfDiagnosis` 的回调 `CreateSelfDiagnosisCallback` **只有 `(error)`，不返回 `reportUuid`**——与 `FirmwareManageModule.submitFirmwareInfo`（见 `firmware-management.md`）同一种设计模式：创建成功后必须再调用 `getSelfDiagnosisReportList` 按 `deviceSn`/时间窗口反查，才能拿到后续 `getSelfDiagnosisReport`/`operateSelfDiagnosis` 需要的 `reportUuid`。

`CreateSelfDiagnosisParams` 字段：`deviceSn`/`type`/`part`/`index`/`motorAction`（均为裸 int，无配套枚举类，取值含义需按业务侧文档约定）/`deviceTopo`。

---

## 4. RtkCalibrationModule（RTK 标定）

```java
Dock dock = ...;  // 或 BaseStation / Relay
RtkCalibrationModule rtkModule = dock.getRtkCalibrationModule();
```

```java
NetworkRtkCalibrationParams networkParams = new NetworkRtkCalibrationParams();
networkParams.setHost("rtk.example.com");
networkParams.setPort(8080);
networkParams.setAccount("user");
networkParams.setPassword("pass");
networkParams.setMountPoint("MOUNT1");
networkParams.setVersion(3);
rtkModule.calibrationByNetwork(networkParams, (error, result) -> {
    // result.getMessage() / result.getBid()
});

ManulRtkCalibrationParams manualParams = new ManulRtkCalibrationParams();   // ⚠️ 类名拼写如此，见下方陷阱
manualParams.setLongitude(113.95);
manualParams.setLatitude(22.54);
manualParams.setHeight(50.0);
rtkModule.calibrationByManual(manualParams, (error, result) -> {
    // 同样返回 RtkCalibrationResult(message, bid)
});

int listenId = rtkModule.subscribeCalibrationProgress(progressResult -> {
    // 单参回调，RtkCalibrationResult，无 error
});
rtkModule.unsubscribeCalibrationProgress(listenId);
```

> ⚠️ **`ManulRtkCalibrationParams` 是 SDK 真实的类名拼写**（缺一个 "a"，正确拼写应为 `ManualRtkCalibrationParams`），在 `generated`/`bean` 两个包中都是这个拼写。**必须原样使用这个拼写**——AI 生成代码时容易"自动纠正"为 `ManualRtkCalibrationParams` 导致编译期找不到类。这是本模块最值得强调的陷阱。

`NetworkRtkCalibrationParams` 字段：`host`/`port`/`account`/`password`/`mountPoint`/`version`（int）。
`ManulRtkCalibrationParams` 字段：`longitude`/`latitude`/`height`（均为 double，手动输入基准点坐标）。
两种标定方式返回同一种结果类型 `RtkCalibrationResult`：`message`(String)/`bid`(String，标定任务业务 ID)。

---

## 5. NetworkTestModule（网络测试）

```java
Dock dock = ...;  // 或 BaseStation
NetworkTestModule networkTestModule = dock.getNetworkTestModule();
```

```java
networkTestModule.startNetworkTest((error, result) -> {
    // NetworkTestProgressResult：message/bid/createTime/updateTime/downloadSpeed/uploadSpeed
});

networkTestModule.getNetworkTestProgress((error, result) -> {
    // 同样返回 NetworkTestProgressResult，一次性查询快照
});

int listenId = networkTestModule.subscribeNetworkTestProgress(progressResult -> {
    // 单参回调，无 error
});
networkTestModule.unsubscribeNetworkTestProgress(listenId);

networkTestModule.stopNetworkTest(error -> { /* 仅 error */ });
```

`NetworkTestProgressResult` 字段：`message`/`bid`（String）、`createTime`/`updateTime`/`downloadSpeed`/`uploadSpeed`（均为 double）。`downloadSpeed`/`uploadSpeed` 单位需按实际接口文档确认（源码无显式单位注释）。

---

## 常见错误清单

1. ❌ 以为 `RtkCalibrationModule`/`NetworkTestModule` 判空失败时能在日志里查到"module is null"——这两个是**静默返回 null**，与同样挂在 Workspace 上的 Log/Maintenance/Diagnosis 三个模块（会记日志）行为相反
2. ❌ 把 `ManulRtkCalibrationParams` 自动"纠正"写成 `ManualRtkCalibrationParams`——SDK 源码真实拼写就是少一个 "a"，纠正后编译失败
3. ❌ 以为 `createSelfDiagnosis` 回调能拿到 `reportUuid`——它只有 `(error)`，必须紧接着 `getSelfDiagnosisReportList` 反查
4. ❌ `logReportOperation`/`operateSelfDiagnosis` 的 `operation`/`reportCmdMethod` 参数当成强类型枚举处理——两者都是裸 `String`，没有配套枚举类约束
5. ❌ 把 `RtkCalibrationModule`/`NetworkTestModule` 当作 Workspace 级模块调用——它们挂在 `Dock`/`BaseStation`/`Relay` 上，不是 `workspace.getXxx()`

## 关联

- `DeviceDebugModule`（同样是设备级、静默 null 安全模式）见 `device-debug.md`
- `FirmwareManageModule` 的 `submitFirmwareInfo` 也是"创建无回执 ID，需反查列表"的同类设计，见 `firmware-management.md`
