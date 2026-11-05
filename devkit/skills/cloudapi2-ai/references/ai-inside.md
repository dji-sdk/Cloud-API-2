# AIInsideManageModule 参考（AI Inside 机载算法管理，9 方法）

## 模块位置与心智模型

```java
// ✅ 挂在 Workspace 上（不是 Drone）
AIInsideManageModule aiInside = workspace.getAIInsideManageModule();
if (aiInside == null) {
    // 底层模块为 null 会记 Logger.error，必须判空
    return;
}
```

源码（`Workspace.java`）：

```java
public synchronized AIInsideManageModule getAIInsideManageModule() {
    if (aiInsideManageModuleRef != null) { return aiInsideManageModuleRef; }
    IAIInsideManageModule raw = workspace.getAIInsideManageModule();
    if (raw == null) {
        Logger.error(TAG, "getAIInsideManageModule error: module is null");
        return null;
    }
    aiInsideManageModuleRef = new AIInsideManageModule(raw);
    return aiInsideManageModuleRef;
}
```

与 `AIBoxModule`（挂 `Drone`，底层为 null 时**不**记日志）相反：本模块挂在 `Workspace` 上，底层为 null 时**会**记
`Logger.error(TAG, "getAIInsideManageModule error: module is null")`。排查“AI Inside 调用失败但没日志”时，先确认取模块的对象对不对。

### 场景定位：PAAS 纯血场景，机载算法全生命周期管理

本模块管的是**机载算法包**本身（列表、绑定设备、装载/卸载指令、私有化场景的上传），
不是算法识别结果（那是 `OnboardAiModule` 的 AI 识别/订阅）。两者职责不重叠：
`AIInsideManageModule` = "有哪些算法、装到哪些设备上"；`OnboardAiModule` = "设备上跑起来的 AI 识别/告警怎么控制和订阅"。

## 方法总览

| # | 方法 | 参数 | HTTP |
|---|---|---|---|
| 1 | `getAIInsideModels` | 无参 | `GET /api/v1/ai-inside/models` |
| 2 | `getAIInsideModelDevices` | `GetAIInsideModelDevicesParam`（signed_id、page、page_size、可选 drone_sn 列表） | `GET /api/v1/ai-inside/models/devices` |
| 3 | `getAIInsideModelDevicesSummary` | `GetAIInsideModelDevicesSummaryParam`（signed_id、operation_type: load/update） | `GET /api/v1/ai-inside/models/devices/summary` |
| 4 | `operateAIInsideModel` | `OperateAIInsideModelParam`（signed_id、operation_type: load/update/unload、可选 drone_sn_list） | `POST /api/v1/ai-inside/models/operate` |
| 5 | `createAIInsideAlgorithm` | `CreateAIInsideAlgorithmParam`（name：上传文件名） | `POST /api/v1/ai-inside/algorithms/create`（私有化场景） |
| 6 | `fileUploadAIInsideAlgorithm` | `FileUploadAIInsideAlgorithmParam`（file_key：MinIO 对象 Key） | `POST /api/v1/ai-inside/algorithms/file-upload`（私有化场景） |
| 7 | `deleteAIInsideAlgorithm` | `DeleteAIInsideAlgorithmParam`（signed_id：算法签名 ID） | `DELETE /api/v1/ai-inside/algorithms`（私有化场景） |
| 8 | `uploadAlgorithmFile` | `UploadAIInsideAlgorithmFileParam`（filePath：本地 ZIP 绝对路径） | SDK 内部串联 create → MinIO PUT → file-upload（私有化场景） |
| 9 | `destroy` | 无参 | 本地清理，非网络调用 |

## 回调形态

8 个业务方法里，7 个是 **(FlightHubError, ResultBean)** 两参数；只有 `deleteAIInsideAlgorithm` 是**单参数** `FlightHubCallback.DeleteAIInsideAlgorithmCallback`
（只有 `FlightHubError`，code=0 表示删除成功，没有返回 bean）——这是本模块里唯一的裸错误回调，不要给它编造一个不存在的 result 参数。

## `destroy()` 生命周期方法

```java
@SuppressWarnings("unchecked")
public void destroy() {
    // 清空 8 个 ConcurrentHashMap：getModelsRef / getDevicesRef / getSummaryRef /
    // operateRef / createRef / fileUploadRef / deleteRef / uploadFileRef
}
```

应在 `Workspace` 销毁时调用，清除尚未完成的 action 回调强引用（防止悬挂引用阻止 GC）；Director native 内存由 C++ 侧负责释放，
`destroy()` 本身不做 native 资源释放。**这是本模块方法数被计为 9（而非 8）的原因**——`destroy()` 是公开方法，计入官方方法数。

## 私有化场景上传算法：两条路径，不要混用

### 路径 A：一步到位（推荐）

```java
UploadAIInsideAlgorithmFileParam param = new UploadAIInsideAlgorithmFileParam("/local/path/algorithm.zip");
aiInside.uploadAlgorithmFile(param, (error, result) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) { return; }
    // result 是 UploadAIInsideAlgorithmFileResult：顶层字段只有 objectKey/objectUrl/etag + 一个 detail
    // signedId 不在顶层，必须下钻到 detail：result.getDetail().getSignedId()
    String signedId = result.getDetail().getSignedId();
});
```

`uploadAlgorithmFile` 内部自动串联三步（`create` 拿 STS 凭证 → MinIO PUT 直传 → `file-upload` 通知后端解压入库），
调用方只感知一次回调。

**字段命名陷阱**：`UploadAIInsideAlgorithmFileResult` 顶层只有 `objectKey`/`objectUrl`/`etag`/`detail` 四个字段，
`signedId` 是 `detail`（`AIInsideAlgorithmDetail` 类型）下的字段，访问器是 `result.getDetail().getSignedId()`，
不是 `result.getSignedId()`——不存在这个顶层方法。

### 路径 B：手动三步（仅在需要分步控制时使用）

```java
// 1. 创建上传凭证
CreateAIInsideAlgorithmParam createParam = new CreateAIInsideAlgorithmParam("algorithm.zip");
aiInside.createAIInsideAlgorithm(createParam, (error, result) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) { return; }
    // result 含 MinIO 预签名 STS 凭证和存储路径

    // 2. 调用方自己用 STS 凭证把 ZIP PUT 到 MinIO（SDK 不提供这一步的方法，需调用方自行实现）

    // 3. 通知后端解压入库
    FileUploadAIInsideAlgorithmParam fileUploadParam = new FileUploadAIInsideAlgorithmParam(objectKey);
    aiInside.fileUploadAIInsideAlgorithm(fileUploadParam, (err2, result2) -> { ... });
});
```

路径 B 的第 2 步（PUT 到 MinIO）SDK **不提供方法**，需要调用方自己用 HTTP 客户端直传——这正是路径 A 存在的原因：
`uploadAlgorithmFile` 把这个 SDK 本身管不到的中间步骤也串联封装了。不要假设 SDK 里有个 `putToMinIO()` 之类的方法。

`CreateAIInsideAlgorithmParam`、`FileUploadAIInsideAlgorithmParam`、`DeleteAIInsideAlgorithmParam`、`UploadAIInsideAlgorithmFileParam`
四个 bean 都提供了单参数便捷构造器（如上例），也可以用无参构造 + setter。

## 批量操作设备算法

```java
OperateAIInsideModelParam param = new OperateAIInsideModelParam();
param.setSignedId(signedId);
param.setOperationType("load");            // load / update / unload
param.setDroneSNList(Arrays.asList("sn1", "sn2"));   // 注意：是 droneSNList，不是 droneSnList
aiInside.operateAIInsideModel(param, (error, result) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) { return; }
    // result 含各设备操作结果码
});
```

**字段命名陷阱**：`OperateAIInsideModelParam` 的字段是 `droneSNList`（`SN` 全大写），访问器是 `getDroneSNList()`/`setDroneSNList()`，
不是常规驼峰的 `droneSnList`/`getDroneSnList()`。写代码前先看 bean 的实际方法名，不要凭通用驼峰规则推断。

## 常见错误清单

1. ❌ 在 `Drone` 上找 `getAIInsideManageModule()`——该方法只在 `Workspace` 上
2. ❌ 认为底层模块为 null 时没有日志——本模块**会**记 `Logger.error`（与 `AIBoxModule` 行为相反，不要套用记忆）
3. ❌ 给 `deleteAIInsideAlgorithm` 的回调编造 result 参数——它是单参数回调，只有 `FlightHubError`
4. ❌ 把 `droneSNList` 写成 `droneSnList`——编译不过（大小写敏感）
5. ❌ 假设 SDK 提供直传 MinIO 的方法——没有，路径 B 的第 2 步需调用方自行实现，或改用路径 A 的 `uploadAlgorithmFile`
6. ❌ 忘记在 `Workspace` 销毁时调用 `destroy()`，导致未完成回调的强引用悬挂
7. ❌ 以为 `uploadAlgorithmFile` 的 `result.getSignedId()` 能直接拿到签名 ID——顶层没有这个方法，必须 `result.getDetail().getSignedId()`
