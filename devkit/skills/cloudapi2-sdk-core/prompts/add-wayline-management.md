# 添加航线资产管理功能

## 目标

管理**航线资产**（KMZ 航线文件本身）：查询列表、查详情、上传/导入、重命名、复制、拼接、批量下载、批量删除。

> 与 `add-flight-task.md` 的区别：本文管的是「航线文件」，`add-flight-task.md` 管的是「拿航线 UUID 去创建飞行任务」。
> 创建飞行任务前，`waylineUuid` 必须来自本模块。

## 模块位置与心智模型

`WaylineManageModule` 挂在 **Workspace** 上，不是 Drone/Dock：

```java
WaylineManageModule waylineModule = workspace.getWaylineManageModule();
```

**全部是 HTTP 调用**（`/pf-wayline/api/v1/*`），只有一个例外是 WebSocket 推送：`listenImportWaylineEvent`（topic `topic_import_wayline_event`）。

| 能力 | 方法 | 链路 |
|---|---|---|
| 航线列表（分页） | `getWaylineList(param, callback)` | GET `/pf-wayline/api/v1/list` |
| 航线详情 | `getWaylineDetail(waylineUuid, callback)` | GET `/pf-wayline/api/v1/details` |
| 重命名 | `updateWayline(waylineUuid, waylineName, callback)` | POST `/pf-wayline/api/v1/update` |
| 批量删除 | `batchDeleteWaylines(List<String>, callback)` | POST `/pf-wayline/api/v1/batch-delete` |
| 批量下载 | `batchDownloadWaylines(param, callback)` | POST `/pf-wayline/api/v1/batch-download` |
| 创建副本 | `duplicateWayline(param, callback)` | POST `/pf-wayline/api/v1/duplicate` |
| 复制（去组织化） | `copyWayline(param, callback)` | POST `/pf-wayline/api/v1/copy` |
| 拼接航线 | `concatWayline(param, callback)` | POST `/pf-wayline/api/v1/concat` |
| 上传 KMZ 并导入 | `importKmz(List<String> filePaths, callback)` | POST `/pf-wayline/api/v1/import-kmz`（multipart，C++ libcurl） |
| 提交航线文件信息 | `submitWaylineInfo(param, callback)` | POST `/pf-wayline/api/v1/import-callback` |
| 获取编辑器 STS 凭证 | `getWaylineEditorSts(params, callback)` | HTTP（对象存储直传凭证） |
| 订阅导入事件 | `listenImportWaylineEvent(callback)` → `int` | WebSocket |
| 取消订阅 | `cancelListenImportWaylineEvent(listenId)` | — |

## 涉及 SDK API（签名以源码为准）

### 1. 查询列表

```java
GetWaylineListParam param = new GetWaylineListParam();
param.setPage(1);                 // 从 1 开始
param.setPageSize(20);
param.setStartTimestamp(0L);      // 0 = 不限；毫秒时间戳
param.setEndTimestamp(0L);
param.setDroneTypes(List.of());   // 空 = 不过滤
param.setWaylineUuids(List.of());
param.setPayloadTypes(List.of());
param.setWaylineTypes(List.of()); // 空 = 不过滤
waylineModule.getWaylineList(param, new FlightHubCallback.GetWaylineListCallback() {
    @Override
    public void onResult(FlightHubError error, GetWaylineListResult result) {
        // ⚠️ 是 (error, result)，不是 (data, error)
    }
});
```

返回结构：

```
GetWaylineListResult
├── pagination: WaylineListPaginationResult { page, pageSize, total }
└── items: List<WaylineListItemResult> {
      waylineUuid, waylineName, waylineTypes, size, droneType,
      payloadTypes, payloadTypesWithIndex, updatedAt,
      withMegaphone, withSearchLight, aiWayline
    }
```

### 2. 查询详情

```java
waylineModule.getWaylineDetail(waylineUuid, new FlightHubCallback.GetWaylineDetailCallback() {
    @Override
    public void onResult(FlightHubError error, GetWaylineDetailResult result) {
        // (error, result)
    }
});
```

`GetWaylineDetailResult` 关键字段：`waylineUuid`、`waylineName`、`md5`、`kmzObjectKey`、`url`、`suffix`、`updatedAt`、`kmzWithoutResUrl`、`targetAreaFileUrl`、`businessFileUrl`、`kmzData`、`extData`、`editorWarnings`、`executeWarnings`。

> **`editorWarnings` / `executeWarnings` 不是 null 时不要静默忽略**：它们表示航线在编辑器或执行器侧存在兼容性问题（机型不匹配、负载缺失、动作越界等），应在业务层透出给用户。

### 3. 重命名 / 批量删除

```java
waylineModule.updateWayline(waylineUuid, "新航线名", callback);   // (error, UpdateWaylineResult)
waylineModule.batchDeleteWaylines(List.of(uuid1, uuid2), callback); // (error, BatchDeleteWaylinesResult)
```

`BatchDeleteWaylinesResult` 只有 `serverCode` / `serverMessage`，**没有逐条的成功/失败明细**。
需要知道哪条失败时，只能删前删后各查一次列表对比，或按单个 UUID 分批调用。

### 4. 批量下载

```java
BatchDownloadWaylinesParam param = new BatchDownloadWaylinesParam();
param.setWaylineUuids(List.of(uuid1, uuid2));
waylineModule.batchDownloadWaylines(param, callback);
```

回调返回的是 `List<BatchDownloadWaylinesItemResult>`（**不是**包裹对象）：

```
BatchDownloadWaylinesItemResult { waylineUuid, waylineName, kmzObjectKey, url, size }
```

拿到 `url` 后需要应用层自行 HTTP 下载；SDK 不落盘。

### 5. 复制 / 创建副本

两个方法容易混：

| 方法 | 参数 | 语义 |
|---|---|---|
| `duplicateWayline(DuplicateWaylineParam)` | `originalWaylineUuid`, `targetWaylineName`, `targetObjectKeyPrefix` | 在工作空间内生成副本，可指定对象存储前缀 |
| `copyWayline(CopyWaylineParam)` | `originalWaylineUuid`, `targetWaylineName` | 去组织化复制，**不需要 workspace_id**，用于跨组织搬运 |

两者回调都是 `(error, result)`，`result` 含新的 `waylineUuid` / `waylineName`。

### 6. 拼接航线

```java
ConcatWaylineParam param = new ConcatWaylineParam();
param.setTargetWaylineName("拼装后航线");
param.setKmzObjectKey(objectKey);              // 拼接结果的对象存储 key
param.setSubWaylineUuids(List.of(a, b, c));    // 按顺序拼接
param.setEditorWarnings(Map.of());
waylineModule.concatWayline(param, callback);  // (error, ConcatWaylineResult)
```

`ConcatWaylineResult` 含新 `waylineUuid` + `kmzData` + `extData`。

### 7. 上传 KMZ 并导入（本地文件）

```java
waylineModule.importKmz(List.of("/data/waylines/a.kmz", "/data/waylines/b.kmz"),
    new FlightHubCallback.ImportKmzCallback() {
        @Override
        public void onResult(FlightHubError error, ImportKmzResult result) {
            // result.total + result.list: List<ImportKmzItem>
            // ImportKmzItem { kmzObjectKey, waylineName, asyncRequestId }
        }
    });
```

只接受 `.kmz` 后缀。**导入是异步的**：回调返回的是 `asyncRequestId`，不是航线 UUID。真正的导入完成事件要订阅 `listenImportWaylineEvent`。

`ImportWaylineEventResult` 关键字段：`asyncRequestId`、`timestamp`、`kmzObjectKey`、`waylineUuid`、`code`、`message`、`kmzData`、`extData`。
用 `asyncRequestId` 与前面的 `ImportKmzItem` 对齐，`code == 0` 才算导入成功。

> ⚠️ **已知 SDK 缺陷（3.3.5 实测）**：`importKmz()` 在原生层拼装 `submitWaylineInfo` 请求时漏传了服务端必填的 `AsyncRequestId`，真实环境下会失败：
> ```
> submitWaylineInfo failed: Key: 'ImportRequest.AsyncRequestId' Error:Field validation
> for 'AsyncRequestId' failed on the 'required' tag
> ```
> Java 层没有任何参数入口可以补上这个字段——缺陷在原生库内部。调用前应先评估版本是否已修复；若仍复现，走下面的 STS 直传方案。

### 8. 提交航线文件信息 / STS 直传（绕开 importKmz 缺陷）

```java
SubmitWaylineInfoParam param = new SubmitWaylineInfoParam();
param.setKmzObjectKey(objectKey);                       // 已上传到对象存储的 key
param.setWaylineName("航线名");
param.setAsyncRequestId(UUID.randomUUID().toString());  // ← 必须设置，否则服务端校验失败
param.setTimestamp(System.currentTimeMillis());
param.setTransparent(Map.of());
waylineModule.submitWaylineInfo(param, callback);       // 回调只有 (error)
```

`submitWaylineInfo` 的回调**没有 result 参数**，只有 `FlightHubError`。

对象存储凭证由 `getWaylineEditorSts` 提供：

```java
GetWaylineEditorStsParams params = new GetWaylineEditorStsParams();
params.setObjectKeyPrefix("wayline/");
params.setExpire(3600);
waylineModule.getWaylineEditorSts(params, callback);   // (error, GetWaylineEditorStsResult)
```

`GetWaylineEditorStsResult`：`credentials`、`objectKeyPrefix`、`bucket`、`endpoint`、`provider`、`region`。
应用层用这些凭证自行 PUT 上传 KMZ，得到 `kmzObjectKey` 后再调 `submitWaylineInfo`。

> 这条路线需要引入 S3/OSS 兼容客户端（或手写签名 PUT），不是纯 SDK 能力。只在 `importKmz()` 确实不可用时采用。

## 必须遵守的规则

- ✅ 规则 2：所有回调检查 `FlightHubError`（`serverCode != 0` 即业务失败）
- ✅ 规则 5：`getWorkspaceInstance()` 返回 null 时不要继续，直接返回错误
- ⚠️ **回调参数顺序统一是 `(error, result)`**——本模块没有 `getDeviceList` 那种 data-first 例外
- ⚠️ 航线资产是 **workspace 级**，不需要 `acquireFlightControlAuthority`
- ⚠️ `submitWaylineInfo` 回调只有 `(error)`，多写一个 result 参数会编译失败

## 实现步骤

1. **WaylineService（workspace 级）**
   - 查询：`getWaylineList` 分页封装，对外暴露统一的 `Page<WaylineItem>`
   - 详情：`getWaylineDetail`，透出 `editorWarnings` / `executeWarnings`
   - 上传：优先 `importKmz` + 订阅导入事件；`importKmz` 不可用时降级 STS 直传 + `submitWaylineInfo`
2. **导入事件订阅器（应用生命周期级）**
   - 启动时 `listenImportWaylineEvent` 拿 `listenId`，关闭时 `cancelListenImportWaylineEvent(listenId)`
   - 按 `asyncRequestId` 维护「导入中」任务表，收到事件后更新状态并推送 SSE/WebSocket 给前端
3. **Controller**
   - `GET /api/waylines?page=&pageSize=` — 列表
   - `GET /api/waylines/{uuid}` — 详情
   - `POST /api/waylines/import` — 上传本地 KMZ（返回 `asyncRequestId`）
   - `PATCH /api/waylines/{uuid}` — 重命名
   - `POST /api/waylines/{uuid}/duplicate` — 创建副本
   - `DELETE /api/waylines?uuids=` — 批量删除

## 注意事项

- 列表分页 `page` 从 **1** 开始，不是 0
- `waylineTypes` 是 `List<String>`（入参）但 `WaylineListItemResult.waylineTypes` 是 `List<Integer>`（出参），不要想当然当作同一类型
- `batchDownloadWaylines` 只返回下载 URL，不负责落盘
- `batchDeleteWaylines` 没有逐条结果，需要精确结果就逐条调用
- 删除航线前先确认没有正在执行的飞行任务引用它（飞行任务侧见 `add-flight-task.md`）
- 拼接/复制/副本产出的新航线，同样需要重新查列表才能确认 `waylineUuid`（部分接口在 result 里返回，以实际为准）

## 常见错误

- ❌ 把 `WaylineManageModule` 挂在 `drone` 或 `dock` 上 → 它在 `workspace` 上
- ❌ 按「查询类 = data 在前」写回调 → 本模块全部是 `(error, result)`
- ❌ `submitWaylineInfo` 的回调写成 `(error, result)` → 该方法没有 result
- ❌ 调完 `importKmz` 就认为航线已创建 → 导入是异步的，必须订阅 `listenImportWaylineEvent` 或用列表轮询确认
- ❌ 自己构造 `submitWaylineInfo` 时不设置 `asyncRequestId` → 服务端 `required` 校验直接失败
- ❌ 批量删除后假设全部成功 → 回调只有 `serverCode`/`serverMessage`，没有逐条明细
- ❌ 把 `WaylineManageModule`（航线文件）和 `FlightTaskModule`（飞行任务）混用 → 前者产出 `waylineUuid`，后者消费它

---

## 空中下发航线（InFlightWaylineModule）

### 与 WaylineManageModule / FlightTaskModule 的区别

本模块管的是**飞行中临时下发**一条航线给飞机执行（比如 DRC 实时控制场景下插入一段新航线），不经过 `FlightTaskModule` 的任务编排，也不需要先有航线资产 UUID。与前两者是三条平行的能力线，不要混用。

### 模块位置

```java
InFlightWaylineModule inFlightWayline = drone.getInFlightWaylineModule();
```

挂在 **Drone** 上（标准 try/catch → null + log 判空模式）。

### 5 个方法

| 方法 | 回调形态 | 说明 |
|---|---|---|
| `createInFlightWaylineTask(params, callback)` | **`(result, error)`，result 在前** ⚠️ | 创建空中航线任务 |
| `controlInFlightWaylineTask(action, callback)` | `(error)` | 暂停/恢复/取消，`action` 为 `InFlightWaylineControlAction` 枚举 |
| `getActiveInFlightWaylineTask(callback)` | **`(info, error)`，result 在前** ⚠️ | 查询当前活跃任务 |
| `listenInFlightWaylineProgress(callback)` / `cancelListenInFlightWaylineProgress(listenId)` | 单参数，无 error | 订阅任务进度 |

> ⚠️ **本模块是本项目回调约定的例外**：全项目绝大多数双参数回调都是 `(FlightHubError error, T result)`，error 在前（参见 `cloudapi2-map/constraints.yaml` 的 `callback-error-first` 规则）；但 `createInFlightWaylineTask` 和 `getActiveInFlightWaylineTask` 的源码实现是 **`callback.onResult(result, error)`，result/info 在前，error 在后**。不要套用本文件其余方法（以及其他模块）的 `(error, result)` lambda 签名，否则参数会错位绑定。

### 示例

```java
InFlightWaylineModule inFlightWayline = drone.getInFlightWaylineModule();
if (inFlightWayline == null) {
    return;
}

CreateInFlightWaylineTaskParams params = new CreateInFlightWaylineTaskParams();
// ... 填充参数

inFlightWayline.createInFlightWaylineTask(params, (result, error) -> {
    // ⚠️ 注意形参顺序：第一个是 result，第二个是 error
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        return;
    }
});

inFlightWayline.controlInFlightWaylineTask(InFlightWaylineControlAction.PAUSE, error -> {
    // 只有 error
});

int listenId = inFlightWayline.listenInFlightWaylineProgress(progress -> {
    // 单参数，无 error
});
// 用完 inFlightWayline.cancelListenInFlightWaylineProgress(listenId);
```

### 常见错误
- ❌ 给 `createInFlightWaylineTask` / `getActiveInFlightWaylineTask` 的回调按 `(error, result)` 顺序写 lambda —— 两者都是 **result/info 在前，error 在后**，与项目内绝大多数双参数回调相反
- ❌ 以为本模块能像 `WaylineManageModule` 一样管理航线文件 —— 它只负责「飞行中创建/控制/查询一次性任务」，不产出可复用的 `waylineUuid`
- ❌ 创建任务前先去查 `WaylineManageModule` 的航线列表 —— 两者无直接依赖关系
