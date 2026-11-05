# AIBoxModule 参考（AI Box 管理，10 方法）

## 模块位置与心智模型

```java
// ✅ 挂在 Drone 上
AIBoxModule aiBox = drone.getAIBoxModule();
```

源码（`Drone.java`）：

```java
public synchronized AIBoxModule getAIBoxModule() {
    if (aiBoxModule == null) {
        IAIBoxModule module = drone.getAIBoxModule();
        if (module != null) {
            aiBoxModule = new AIBoxModule(module);
        }
    }
    return aiBoxModule;
}
```

### 头号陷阱：底层模块为 null 时**不会**记日志，只会静默返回 null

`getAIBoxModule()` 的判空分支（`if (module != null)`）只在非 null 时构造并缓存；底层 `module == null` 时**没有 `else` 分支**，
不会调用 `Logger.error`，直接返回还是 `null` 的缓存字段。这与 `getOnboardAiModule()`、`getAIInsideManageModule()`、
`getAIAlertModule()` 三个 getter 在底层为 null 时都会 `Logger.error` 并返回 `null` **不一致**——排查“AI Box 相关调用全部失败”
时，不要指望日志里会有 `getAIBoxModule` 的错误记录，必须自己在业务层判空并给出提示。

```java
AIBoxModule aiBox = drone.getAIBoxModule();
if (aiBox == null) {
    // 该机型/该固件不支持 AI Box，没有日志可查，只能业务层兜底提示
    return;
}
```

## 回调形态

全部 10 个方法的回调都是 **(FlightHubError, ResultBean)** 两参数、error-first；没有无参数或单参数回调。

## 方法总览

| # | 方法 | 参数 | 传输链路 |
|---|---|---|---|
| 1 | `fetchAIBoxInfo` | `String deviceSn` | HTTP `POST /device/api/v1/service/aibox/ai-box-info` |
| 2 | `fetchFileDownloadUrl` | `FetchFileDownloadUrlParam` | HTTP `POST /device/api/v1/service/aibox/file-download-url` |
| 3 | `fetchAppList` | `AIBoxFetchAppListParam` | HTTP `POST /device/api/v1/service/aibox/app-list` |
| 4 | `fetchAppListProgress` | `AIBoxFetchAppListProgressParam` | HTTP `POST /device/api/v1/service/aibox/app-list-progress` |
| 5 | `exportFile` | `AIBoxExportFileParam` | HTTP `POST /device/api/v1/service/aibox/export-file` |
| 6 | `exportFileProgress` | `AIBoxExportFileProgressParam` | HTTP `POST /device/api/v1/service/aibox/export-file-progress` |
| 7 | `uploadAIBoxFile` | `String filePath, String deviceSn, String boxSn` | SDK 内部（获取 STS 凭证后直传） |
| 8 | `installApp` | `AIBoxInstallAppParam` | HTTP `POST /device/api/v1/service/aibox/install-app` |
| 9 | `installAppProgress` | `AIBoxInstallAppProgressParam` | HTTP `POST /device/api/v1/service/aibox/install-app-progress` |
| 10 | `operateApp` | `AIBoxOperateAppParam` | HTTP `POST /device/api/v1/service/aibox/operate-app` |

## 两个方法偏离标准 bean 模式

标准模式（9 个方法里的 8 个）：`bean 参数 → FlightHubSdkConvert.toNativeXxxParam() → 调用 → finally { nativeParam.delete() }`。
`fetchAIBoxInfo` 与 `uploadAIBoxFile` 不走这条路：

```java
// fetchAIBoxInfo：直接传 String，没有 bean/native 转换，也没有 finally delete
public void fetchAIBoxInfo(String deviceSn, FlightHubCallback.FetchAIBoxInfoCallback callback)

// uploadAIBoxFile：3 个裸 String，内部手工 new 一个 generated.UploadFileParam 并用 snake_case setter
public void uploadAIBoxFile(String filePath, String deviceSn, String boxSn,
        FlightHubCallback.UploadAIBoxFileCallback callback) {
    com.dji.flighthubsdk.generated.UploadFileParam param = new com.dji.flighthubsdk.generated.UploadFileParam();
    param.setFile_path(filePath != null ? filePath : "");
    param.setDevice_sn(deviceSn != null ? deviceSn : "");
    param.setBox_sn(boxSn != null ? boxSn : "");
    // 注意：这里没有 try/finally 调 param.delete()，与其它 8 个方法不一致
    ...
}
```

不要因为其它 8 个方法都是 bean 入参就假设 `fetchAIBoxInfo`/`uploadAIBoxFile` 也有对应的 bean 类——它们没有，直接传基础类型。

## 典型用法

### 1. 查询 AI Box 信息

```java
AIBoxModule aiBox = drone.getAIBoxModule();
if (aiBox == null) { return; }
aiBox.fetchAIBoxInfo(deviceSn, (error, result) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        return;
    }
    // result: AIBoxInfoListResult
});
```

### 2. 应用列表 + 进度轮询

`fetchAppList`（一次性列表）与 `fetchAppListProgress`（查询一次性异步任务的进度）是两个独立方法，
**不是 listen/cancel 配对**——`fetchAppListProgress` 本质是又一次 HTTP 查询，不建立长连接，需要调用方自己做轮询循环：

```java
AIBoxFetchAppListParam param = new AIBoxFetchAppListParam();
param.setDeviceSn(deviceSn);
aiBox.fetchAppList(param, (error, result) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) { return; }
    // result.getTaskId() 之类的任务标识，用于后续 progress 查询（具体字段以 bean 为准）
});

// 之后轮询
AIBoxFetchAppListProgressParam progressParam = new AIBoxFetchAppListProgressParam();
aiBox.fetchAppListProgress(progressParam, (error, progress) -> { ... });
```

`exportFile`/`exportFileProgress`、`installApp`/`installAppProgress` 是同样的“发起 + 轮询进度”模式，三组互相不可混用参数。

### 3. 上传文件（内部 STS 直传）

```java
aiBox.uploadAIBoxFile("/local/path/to/file", deviceSn, boxSn, (error, result) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) { return; }
});
```

`@implNote` 明确标注“链路: SDK 内部 | 获取 STS 凭证后上传 AI Box 文件”——调用方不需要也不能自己拿 STS 凭证，
SDK 内部完成“取凭证→直传对象存储”的两步，调用方只感知一次回调。

### 4. 安装 / 操作应用

```java
AIBoxInstallAppParam installParam = new AIBoxInstallAppParam();
aiBox.installApp(installParam, (error, result) -> { ... });

AIBoxOperateAppParam operateParam = new AIBoxOperateAppParam();   // 启动/停止等操作类型由 bean 字段决定
aiBox.operateApp(operateParam, (error, result) -> { ... });
```

## 常见错误清单

1. ❌ 以为 `getAIBoxModule()` 返回 null 时日志里会有错误——**没有**，该 getter 唯一不记日志的分支
2. ❌ 给 `fetchAIBoxInfo`/`uploadAIBoxFile` 编造 bean 参数类——它们是裸 `String` 参数
3. ❌ 把 `fetchAppListProgress`/`exportFileProgress`/`installAppProgress` 当成 listen/cancel 订阅——它们是一次性 HTTP 查询，要轮询
4. ❌ 假设 `uploadAIBoxFile` 需要调用方先获取 STS 凭证——SDK 内部已经处理
5. ❌ 混用三组“发起/进度”参数（app-list / export-file / install-app 各自独立的 param 类）
