# 添加媒体管理功能

## 目标
查询、下载无人机拍摄的媒体文件（照片、视频），处理媒体上传回调与地理标记媒体文件上传。

## 模块位置

```java
MediaServiceModule media = workspace.getMediaServiceModule();
```

挂在 **Workspace** 上（标准 try/catch → null + log 判空模式）。全部走 HTTP，除两对 `listen*` 监听走 WebSocket。

## 方法总览（13 个，含 2 组重载）

| 能力 | 方法 | 回调形态 | 链路 |
|---|---|---|---|
| 文件列表（bean 入参） | `getMediaFilesList(GetMediaFilesListParams, callback)` | `(error, result)` | HTTP |
| 文件列表（平铺入参便捷重载） | `getMediaFilesList(flightId, startIndex, pageSize, fileTypes, deviceSn, startCreateTime, endCreateTime, callback)` | `(error, result)`，同一回调类型 | HTTP |
| 删除文件 | `deleteMediaFiles(List<Long> fileIds, callback)` | **`(error)`** | HTTP |
| 下载链接（单文件） | `getMediaFilesDownloadURL(long fileId, callback)` | `(error, GetMediaFilesDownloadUrlResults)` | HTTP |
| 下载链接（批量 objectKey） | `getMediaFilesDownloadURL(List<String> objectKeys, callback)` | **`(error, GetMediaFilesDownloadUrlListResults)`** ⚠️ 不同结果类型 | HTTP |
| 文件详情 | `getMediaFileDetailById(int fileId, callback)` | `(error, result)` | HTTP |
| 媒体上传 STS | `getMediaUploadSts(GetMediaUploadStsParams, callback)` | `(error, result)` | HTTP |
| 媒体上传完成回执 | `mediaUploadCallback(MediaUploadCallbackParams, callback)` | `(error, result)` | HTTP |
| 地理标记媒体上传 | `uploadGeoMediaFile(UploadGeoMediaFileParams, callback)` | `(error, result)` | SDK 内部直传 |
| 文件后处理消息订阅 | `listenMediaFilePostProcessMessage(deviceSn, callback)` / `cancelListenMediaFilePostProcessMessage(listenId)` | 单参数，无 error | WS |
| 设备上传消息订阅 | `listenMediaFileDeviceUploadMessage(deviceSn, callback)` / `cancelListenMediaFileDeviceUploadMessage(listenId)` | 单参数，无 error | WS |

> ⚠️ **两个 `getMediaFilesDownloadURL` 重载返回的结果类型不同**：按 `long fileId` 单文件查询返回 `GetMediaFilesDownloadUrlResults`；按 `List<String> objectKeys` 批量查询返回的是 **另一个类型** `GetMediaFilesDownloadUrlListResults`。两者字段结构不同，不要假设同一套解析代码能同时处理两种重载的返回值。
>
> ⚠️ **`deleteMediaFiles` 回调只有 `(error)`，没有逐条删除结果** —— 和 `WaylineManageModule.batchDeleteWaylines` 的「只有 serverCode，没有明细」是同一类陷阱，别指望从返回值里知道哪个 fileId 删除失败。
>
> ⚠️ **两组 `listen*` 订阅都要求传入 `deviceSn`，但取消订阅时只传回 `listenId`**（`cancelListenXxx(int listenId)`，不带 `deviceSn`）——订阅与取消的签名不对称，照抄时不要顺手把 `deviceSn` 也传进 cancel 方法（编译会直接报错）。

## 1. 查询文件列表

```java
MediaServiceModule media = workspace.getMediaServiceModule();
if (media == null) {
    return;
}

GetMediaFilesListParams params = new GetMediaFilesListParams();
params.setFlightId(flightId);       // 航线/飞行任务 ID
params.setDeviceSn(deviceSn);
params.setStartIndex(0);            // 起始索引（不是页码）
params.setPageSize(20);
params.setFileTypes(List.of("jpg"));

media.getMediaFilesList(params, (error, result) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        return;
    }
    // result.getList() / getTotal() / getNextIndex() / getPageSize()
});
```

也可以用平铺参数的便捷重载（源码确认存在，字段同 `GetMediaFilesListParams`），两者回调类型相同。

## 2. 删除文件

```java
media.deleteMediaFiles(List.of(fileId1, fileId2), error -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        return;
    }
    // 只知道整体成功/失败，没有逐条明细
});
```

## 3. 获取下载链接 —— 两个重载，两种返回类型

```java
// 单文件
media.getMediaFilesDownloadURL(fileId, (error, result) -> {
    // result: GetMediaFilesDownloadUrlResults
});

// 批量 objectKey
media.getMediaFilesDownloadURL(List.of(objectKey1, objectKey2), (error, result) -> {
    // result: GetMediaFilesDownloadUrlListResults —— 与上面不是同一个类型
});
```

## 4. 文件详情

```java
media.getMediaFileDetailById(fileId, (error, result) -> {
    // result: 文件详情 bean
});
```

## 5. 媒体上传三步流程（STS → 回执）

与 `cloudapi2-payload/references/psdk-resource.md` 的喊话器资源 STS 直传同构（先拿凭证、再直传对象存储、再登记回执），但这里走的是 `MediaServiceModule` 自己的 STS 接口，不要跨模块混用：

```java
media.getMediaUploadSts(stsParams, (error, sts) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        return;
    }
    // 应用层用 sts 凭证把文件 PUT 到对象存储

    MediaUploadCallbackParams callbackParams = new MediaUploadCallbackParams();
    // ... 填充上传完成信息（objectKey 等）
    media.mediaUploadCallback(callbackParams, (cbError, cbResult) -> {
        // 登记完成
    });
});
```

## 6. 地理标记媒体文件上传（uploadGeoMediaFile）

```java
UploadGeoMediaFileParams params = new UploadGeoMediaFileParams();
// 源码内部通过逐个 setter 组装原生参数对象，不经由统一的 FlightHubSdkConvert.toNativeXxx 辅助方法
// ... 填充地理位置、文件路径等字段

media.uploadGeoMediaFile(params, (error, result) -> {
    // ...
});
```

> 本方法内部构造原生参数的方式与模块内其他方法不同（逐字段 setter 而非统一转换辅助方法），属于内部实现细节差异，不影响应用层调用方式，仅在排查「字段漏传」类问题时作为参考线索。

## 7. 订阅文件后处理 / 设备上传消息

```java
int postProcessListenId = media.listenMediaFilePostProcessMessage(deviceSn, msg -> {
    // 单参数，无 error
});
// 取消时只传 listenId（cancel 签名不带 deviceSn）：
media.cancelListenMediaFilePostProcessMessage(postProcessListenId);

int uploadListenId = media.listenMediaFileDeviceUploadMessage(deviceSn, msg -> {
    // 单参数，无 error
});
media.cancelListenMediaFileDeviceUploadMessage(uploadListenId);
```

## 常见错误
- ❌ 不检查 `MediaServiceModule` 是否为 null
- ❌ 用同一套解析逻辑处理两个 `getMediaFilesDownloadURL` 重载的返回值 —— 它们是不同的结果类型（`GetMediaFilesDownloadUrlResults` vs `GetMediaFilesDownloadUrlListResults`）
- ❌ 期望 `deleteMediaFiles` 返回逐条删除结果 —— 回调只有 `(error)`
- ❌ 取消 `listenMediaFilePostProcessMessage` / `listenMediaFileDeviceUploadMessage` 时只传 `listenId` —— 两者的 cancel 方法都要求同时传 `deviceSn`
- ❌ 缓存下载链接不考虑过期
- ❌ 通过 BFF 转发大文件内容（应返回 URL 让前端直接下载）
- ❌ 把 `getMediaUploadSts` / `mediaUploadCallback` 的 STS 流程和 `cloudapi2-payload` 的喊话器资源 STS 流程混用 —— 两套凭证体系互不通用
