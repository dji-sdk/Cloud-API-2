# SafetyServiceModule — 飞行区 / 离线地图 / 信号地图 / 解禁证书

## 模块位置与心智模型

`SafetyServiceModule` 挂在 **Workspace** 上：

```java
SafetyServiceModule safety = workspace.getSafetyServiceModule();
```

**22 个顶层方法**，按能力分四组：

| 分组 | 方法 | 链路 |
|---|---|---|
| 信号地图 | `searchSignalMaps` | HTTP `POST /safety/api/v1/signal-maps/search` |
| 自定义飞行区 | `create/get/update/deleteCustomizedFlightAreas`、`syncCustomizedFlightAreas`、`getFlightAreasSyncStatus` | HTTP `/safety/api/v2/flight-areas*`、`/safety/api/v1/flight-areas/*` |
| 离线地图 | `get/update/cancelOfflineMapsModelInfo`、`setOfflineMaps`、`syncOfflineMaps`、`getOfflineMapsSyncStatus`、`listenDroneOfflineMapEnable` | HTTP `/safety/api/v1/offline-maps*` + WS `topic_drone_offline_map_enable` |
| 解禁证书 | `getUnlockLicenseList`、`switch/check/submit/clearDeviceUnlockLicense`、`uploadUnlockLicenseFile` | HTTP `/device/api/v1/service/unlock-license/*` |

> 解禁证书虽然挂在 `SafetyServiceModule` 下，走的却是 `/device/api/...` 接口，不要按 URL 前缀去找别的模块。

## 回调形态（本模块最关键的坑）

| 回调类型 | 形态 | 适用方法 |
|---|---|---|
| 带结果 | `(FlightHubError error, XxxResults result)` | 绝大多数查询/创建/更新 |
| **只有错误** | `(FlightHubError error)` —— `ResultCallbackWithError` | `deleteCustomizedFlightAreas`、`updateOfflineMapsModelInfo`、`cancelOfflineMapsModelInfo`、`setOfflineMaps` |
| **只有结果** | `(Xxx)` —— 单参数，**没有 error** | `listenDroneOfflineMapEnable`（WS 推送） |
| 只有错误（独立接口） | `(FlightHubError error)` | `switchUnlockLicense`、`submitUnlockLicense`、`clearDeviceUnlockLicense` |

`ResultCallbackWithError` 是共享接口（`FlightHubCallback.ResultCallbackWithError`），签名 `onResult(FlightHubError error)`。
给它多写一个 result 参数会**编译失败**。

## 1. 信号地图

```java
safety.searchSignalMaps(
    List.of("1581F5FHD22A900C0001"),
    List.of("4g", "sdr"),
    (error, result) -> {
        if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) { return; }
        for (DeviceSignalItem device : result.getLists()) {
            for (GridSignalItem grid : device.getGrids()) {
                // grid.getH3Index()    Uber H3 网格下标，等级固定 11
                // grid.getSdrAvg() / getSdrMax() / getSdrMin()
                // grid.getSignal4gAvg() / ...  未插 4G 模块时无 4G 字段（值为空/缺省）
            }
        }
    });
```

结构：

```
SignalMapsResults
└── lists: List<DeviceSignalItem> { deviceSn, grids: List<GridSignalItem> }
      GridSignalItem { h3Index, sdrAvg, sdrMax, sdrMin,
                       signal4gAvg, signal4gMax, signal4gMin }
```

- `dataTypes` 传字符串数组，常见值 `"4g"` / `"sdr"`
- 网格下标是 **H3 level 11**，前端渲染需要 H3 库把 index 还原成六边形边界，SDK 不给几何

## 2. 自定义飞行区

### 2.1 几何模型（和 Element 的不一样，别混用）

```
FlightAreasContentData
├── type:     固定 "Feature"
├── properties: ContentsPropertyData { color, width, clampToGround }
└── geometry:  ContentsGeometryData { type, coordinates, center, radius }
```

`ContentsGeometryData.type` 取值：

| type | 适用飞行区类型 | 必填字段 | 约束 |
|---|---|---|---|
| `"Circle"` | `nfz` / `dfence` | `center: [lng, lat]`、`radius` | `radius > 0`，单位米 |
| `"Polygon"` | `nfz` / `dfence` | `coordinates` | ≥ 3 个点、顶点数 < 255、首尾必须闭合 |
| `"Polyline"` | **仅 `noland`** | `coordinates` | 折线，配合 `properties.width` |

> ⚠️ 这里的折线类型叫 **`Polyline`**，而 `ElementModule` 的折线叫 **`LineString`**。两套几何模型不通用。
> ⚠️ `coordinates` 类型是 `List<List<List<Double>>>`（三层嵌套），单个坐标是 `[经度, 纬度]`。

`ContentsPropertyData.width`：缓冲宽度（米），**仅 Polyline 有效**，范围 `[10, 100]`，默认 10。

### 2.2 创建

```java
ContentsGeometryData geometry = new ContentsGeometryData();
geometry.setType("Circle");
geometry.setCenter(List.of(114.06, 22.55));
geometry.setRadius(500);

ContentsPropertyData props = new ContentsPropertyData();
props.setColor("#FF0000");
props.setClampToGround(true);

FlightAreasContentData content = new FlightAreasContentData("Feature", props, geometry);

CreateCustomizedFlightAreasParams params = new CreateCustomizedFlightAreasParams();
params.setName("圆形限飞区");
params.setType("nfz");          // nfz / dfence / noland
params.setContent(content);
params.setCreatedBy(userId);

safety.createCustomizedFlightAreas(params, (error, result) -> {
    // result: CreateFlightAreasResults { id, name }
});
```

飞行区类型与配额：

| type | 含义 | 数量上限 |
|---|---|---:|
| `nfz` | 限飞区 | 100 |
| `dfence` | 作业区 | — |
| `noland` | 禁降区（**仅 Polyline**） | 50 |

### 2.3 查询

两种调用方式等价，后者只是帮你拼参数：

```java
// 方式一：参数对象
GetCustomizedFlightAreasParams params = new GetCustomizedFlightAreasParams();
params.setType("nfz");
params.setStatus("enable");   // enable / disable
params.setPageIndex(1);       // 从 1 开始；不传 = 查全部
params.setPageSize(20);
safety.getCustomizedFlightAreas(params, callback);

// 方式二：便捷重载（内部转成方式一再调用）
safety.getCustomizedFlightAreas("圆形", "nfz", "enable", List.of(), 1, 20, callback);
```

返回：

```
GetFlightAreasResults
├── list: List<FlightAreaInfoItem> { id, name, status, type, content,
│                                    createdTime, createdBy, updatedTime, updatedBy }
└── pagination: FlightAreaPagination { page, pageSize, total }
```

### 2.4 更新 / 删除

```java
UpdateCustomizedFlightAreasParams params = new UpdateCustomizedFlightAreasParams();
params.setFlightAreaId(areaId);   // 必填
params.setName("新名称");
params.setContent(content);       // 几何更新时整体覆盖
params.setUpdateBy(userId);
safety.updateCustomizedFlightAreas(params, (error, result) -> {
    // result: UpdateFlightAreasResults { id, updatedArea: UpdatedAreaData }
});

// 删除：回调只有 error
safety.deleteCustomizedFlightAreas(areaId, error -> { /* 只有 error */ });
```

> ⚠️ `UpdateCustomizedFlightAreasParams` **没有 `type` 字段** —— 飞行区类型创建后不能通过本接口改。

### 2.5 同步到设备

同步是 **异步任务**：`sync` 只代表"已受理"，必须轮询 `getFlightAreasSyncStatus` 才知道结果。

```java
// 1) 触发同步
safety.syncCustomizedFlightAreas(List.of(dockSn), (error, result) -> {
    // result: SyncFlightAreasResults { results: List<DeviceSyncResults> }
    // DeviceSyncResults { deviceSn, code }
});

// 2) 轮询状态
safety.getFlightAreasSyncStatus(List.of(dockSn), (error, result) -> {
    // result: GetAreasSyncStatusResults { list: List<DeviceSyncStatusResults> }
    // DeviceSyncStatusResults { sn, status, reason }
});
```

`DeviceSyncResults.code`（同步受理结果）：

| code | 含义 |
|---:|---|
| 0 | 成功 |
| 230012 | 设备不支持自定义飞行区 |
| 230013 | 设备离线 |
| 230014 | 设备繁忙 |
| 230015 | 超时 |

`DeviceSyncStatusResults.status`：`waiting` / `synchronizing` / `synchronized` / `fail`。
`reason` 是**细粒度失败原因码**（0 无错误；1 解析文件信息失败；2 获取飞行器端文件信息失败；3 云端下载失败；4 链路翻转失败；5 传输失败；6 disable 失败；7 飞行区删除失败；8 飞行器加载作业区域失败；9 enable 失败；10 机场增强图传无法关闭；11 飞行器开机失败；12 checksum 校验失败；13 同步异常超时；255 未知错误）。**透出给用户时要带 reason，不能只说"同步失败"。**

## 3. 离线地图

### 3.1 模型信息

```java
safety.getOfflineMapsModelInfo((error, result) -> {
    // GetOfflineMapsModelInfoResults { disable, offlineMap: OfflineMapsModelData }
    // OfflineMapsModelData { id, updatedTime, models: List<OfflineMapsModelInfoData>,
    //                        percent, status, result }
});
```

`status` 已知枚举：`"finish"`（完成）；`percent` 是生成/同步进度百分比。

### 3.2 更新 / 取消 / 开关

```java
UpdateOfflineMapsParams update = new UpdateOfflineMapsParams();
update.setModel3dItems(List.of(model));   // ModelItemInfoData { uuid, objectKey, fileType }
update.setModel2dItems(List.of());
safety.updateOfflineMapsModelInfo(update, error -> { /* 只有 error */ });

safety.cancelOfflineMapsModelInfo(error -> { /* 只有 error */ });

SetOfflineMapsParams set = new SetOfflineMapsParams();
set.setOfflineMapEnable(true);
safety.setOfflineMaps(set, error -> { /* 只有 error */ });
```

`ModelItemInfoData.fileType` 枚举：`"tif"` / `"b3dm"` / `"pnts"`。

### 3.3 同步到设备

与飞行区同构，也是"先触发、再轮询"：

```java
safety.syncOfflineMaps(List.of(dockSn), (error, result) -> {
    // OfflineMapsSyncResults { results: List<DeviceSyncResults> }
});
safety.getOfflineMapsSyncStatus(List.of(dockSn), (error, result) -> {
    // GetOfflineMapsSyncStatusResults { list: List<DeviceSyncStatusResults> }
});
```

### 3.4 订阅飞行器离线地图开关（WS）

```java
int listenId = safety.listenDroneOfflineMapEnable(droneSn, msg -> {
    // ⚠️ 单参数回调，没有 FlightHubError
    boolean enabled = msg.isOfflineMapEnable();
});
if (listenId < 0) { /* 订阅失败 */ }

// 关闭时
safety.cancelListenDroneOfflineMapEnable(listenId);
```

topic：`topic_drone_offline_map_enable`。

## 4. 解禁证书（飞行解禁）

常用流程是一条**三段式主链**：上传文件 → 校验 → 提交；之后按需做查询 / 开关 / 清除等后续管理。

```java
// 1) 上传本地证书文件（SDK 内部先取 STS 凭证再上传）
safety.uploadUnlockLicenseFile("/data/licenses/a.crt", (error, result) -> {
    // UploadUnlockLicenseFileResult 只暴露 fileStoreDir
    // javadoc 说明用 fileStoreDir 拼接 file_key 供 checkUnlockLicense 使用
});

// 2) 校验
CheckUnlockLicenseParam check = new CheckUnlockLicenseParam();
check.setFileKey(fileKey);
check.setDeviceSn(droneSn);
safety.checkUnlockLicense(check, (error, result) -> {
    // CheckUnlockLicenseResult { errFileNames, errCertMismatch,
    //                            items: List<CheckUnlockLicenseResultItem> }
});

// 3) 提交
ImportUnlockLicenseParam submit = new ImportUnlockLicenseParam();
submit.setDeviceSn(droneSn);
submit.setCertUrl(certUrl);
safety.submitUnlockLicense(submit, error -> { /* 只有 error */ });

// 4) 查询 / 开关 / 清除
QueryDeviceUnlockLicenseListParam query = new QueryDeviceUnlockLicenseListParam();
query.setDeviceSn(droneSn);
query.setDeviceModelDomain(0);   // 0=飞机 3=机场
query.setDetail(true);
safety.getUnlockLicenseList(query, (error, result) -> {
    // QueryDeviceUnlockLicenseListResult { result, deviceModelDomain, consistence,
    //                                      licenses: List<DeviceUnlockLicenseItem> }
});

SwitchUnlockLicenseParam sw = new SwitchUnlockLicenseParam();
sw.setDeviceSn(droneSn);
sw.setLicenseId(licenseId);
sw.setEnable(true);
safety.switchUnlockLicense(sw, error -> { /* 只有 error */ });

safety.clearDeviceUnlockLicense(droneSn, error -> { /* 只有 error */ });
```

`DeviceUnlockLicenseItem` 的解锁详情是**按类型并列的可空字段**，按需读取：
`commonFields`、`areaUnlock`、`circleUnlock`、`countryUnlock`、`heightUnlock`、`polygonUnlock`、`powerUnlock`、`ridUnlock`。
`commonFields` 含 `id/name/type/groupId/userId/deviceSn/beginTime/endTime/userOnly/enabled`。

> ⚠️ `UploadUnlockLicenseFileResult` 实际只有 `getFileStoreDir()`，回调 javadoc 提到的 `object_key` 没有对应 getter。不要虚构 `getObjectKey()`。

## 必须遵守的规则

- ✅ 规则 2：所有 HTTP 回调先判断 `error.getErrorCode() == SUCCESS`；`ResultCallbackWithError` 只有 error，没有结果可用
- ✅ 规则 5：`getWorkspaceInstance()` / `getSafetyServiceModule()` 判空
- ✅ 规则 3：`listenDroneOfflineMapEnable` 的 listenId 必须保存并在 `@PreDestroy` 中 cancel
- ⚠️ 飞行区/离线地图是 **workspace 级 + 服务端配置**，不需要 `acquireFlightControlAuthority`
- ⚠️ 同步类接口（飞行区 / 离线地图）都是"触发 + 轮询状态"两段式，不存在同步阻塞返回

## 常见错误

- ❌ 把 `deleteCustomizedFlightAreas` / `setOfflineMaps` 的回调写成 `(error, result)` → 编译失败，它们是 `ResultCallbackWithError`
- ❌ 把 `listenDroneOfflineMapEnable` 的回调写成 `(error, msg)` → 编译失败，WS 推送是单参数
- ❌ 给 `noland` 传 `Circle`/`Polygon`，或给 `nfz`/`dfence` 传 `Polyline` → 服务端校验失败
- ❌ Polygon 首尾未闭合 / 顶点数 ≥ 255 → 服务端校验失败
- ❌ 飞行区 `width` 传给 Circle 或 Polygon → 只有 Polyline 生效，其余类型会被忽略
- ❌ 调完 `syncCustomizedFlightAreas` 就认为设备已生效 → 只是受理，必须轮询 `getFlightAreasSyncStatus`
- ❌ 同步失败时只报"失败"不报 `reason` → reason 码是定位问题的唯一线索
- ❌ 以为 `UpdateCustomizedFlightAreasParams` 能改 `type` → 没有该字段
- ❌ 去 `DroneXxxModule` / `DockXxxModule` 里找解禁证书接口 → 上传/校验/提交/查询/开关/清除全部在 `workspace.getSafetyServiceModule()` 上
- ❌ 上传后直接提交，跳过 `checkUnlockLicense` → `errFileNames` / `errCertMismatch` 非空时证书不合格，必须中止
