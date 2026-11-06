# PsdkResourceModule — 喊话器资源 / STS 直传 / Widget 资源

## 模块位置与心智模型

`PsdkResourceModule` 挂在 **Workspace** 上（不是 Drone）：

```java
PsdkResourceModule resource = workspace.getPsdkResourceModule();
```

```java
// Workspace.java:224-235 内部实现（简化）
public synchronized PsdkResourceModule getPsdkResourceModule() {
    if (psdkResourceModuleRef != null) {
        return psdkResourceModuleRef;
    }
    IPsdkResourceModule raw = workspace.getPsdkResourceModule();
    if (raw == null) {
        Logger.error(TAG, "getPsdkResourceModule error: module is null");
        return null;   // ⚠️ 这里会返回 null，调用前必须判空
    }
    psdkResourceModuleRef = new PsdkResourceModule(raw);
    return psdkResourceModuleRef;
}
```

> 与 `PsdkControlModule` 的差异：`Drone.getPsdkControlModule()` 源码中**没有**这个 null 返回分支（底层为 null 时会在构造阶段 NPE），而 `Workspace.getPsdkResourceModule()` **会安静地返回 null**。两个模块的防御写法不同。

**7 个顶层方法**，除上传外全部是 HTTP：

| 方法 | 链路 |
|---|---|
| `getPsdkSpeakerResourceList(callback)` | HTTP `GET /pf-media/api/v2/speaker/resources` |
| `createPsdkSpeakerResource(params, callback)` | HTTP `POST /pf-media/api/v2/speaker/resources` |
| `editPsdkSpeakerResource(params, callback)` | HTTP `PUT /pf-media/api/v2/speaker/resources/{id}` |
| `deletePsdkSpeakerResource(int resourceId, callback)` | HTTP `DELETE /pf-media/api/v2/speaker/resources/{id}` |
| `getSTS(callback)` | HTTP `GET /pf-media/api/v2/speaker/sts` |
| `uploadSpeakerAudioFile(String filePath, STSInfo stsInfo, callback)` | **SDK 内部**：拿 STS 凭证后直传对象存储（不是后端 HTTP 接口） |
| `getPsdkWidgetResources(params, callback)` | HTTP `POST /control/api/v1/manual/psdk/ui-resources/batch-get` |

> ⚠️ `uploadSpeakerAudioFile` 的 `@implNote` 是「SDK 内部 | 获取 STS 凭证后上传至对象存储」——它**不走公司后端**，而是由 SDK 原生层拿着 STS 凭证直接 PUT 到对象存储。因此失败可能来自对象存储侧（网络/凭证过期/跨域），不一定能在后端日志里查到。

## 回调形态

| 接口 | 签名 | 适用 |
|---|---|---|
| `PsdkSpeakerResourceListCallback` | `onResult(FlightHubError error, PsdkSpeakerResourceList list)` | `getPsdkSpeakerResourceList` |
| `PsdkSpeakerResourceCallback` | `onResult(FlightHubError error, PsdkSpeakerResource resource)` | `create` / `edit` |
| `STSInfoCallback` | `onResult(FlightHubError error, STSInfo stsInfo)` | `getSTS` |
| `PsdkWidgetResourceListCallback` | `onResult(FlightHubError error, PsdkWidgetResourceList list)` | `getPsdkWidgetResources` |
| `ResultCallbackWithError` | `onResult(FlightHubError error)` **仅 error** | `delete` / `uploadSpeakerAudioFile` |

**本模块全部是 `(error, result)` 或 `(error)` 形态，没有单参数无 error 的订阅**。与 `PsdkControlModule` 的 listen 家族正好相反。

失败语义：SDK 内部异常时，所有方法都会**以 `ERROR_OPERATION_FAILED` + null 结果**回调一次（不是抛异常，也不是静默）。所以回调里必须判 `error`，且部分场景 `result` 可能为 null。

---

## 1. 喊话器资源 CRUD

### 1.1 查询列表

```java
workspace.getPsdkResourceModule().getPsdkSpeakerResourceList((error, list) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS || list == null) {
        return;
    }
    for (PsdkSpeakerResource item : list.getResources()) {   // 注意 getResources()
        int id = item.getId();
        String name = item.getName();
        String state = item.getState();
    }
});
```

> 列表 bean 字段名是 `resources`（`getResources()`）。**Widget 资源列表的字段名却是 `uiResourceUrls`**，两个列表 bean 不对称，别照抄。

### 1.2 创建

```java
CreatePsdkSpeakerResourceParams params = new CreatePsdkSpeakerResourceParams();
params.setName("警告音");
params.setFileType("audio");        // audio | tts
params.setObjectKey(objectKey);     // 上传后拿到的对象键
params.setSize(fileSize);
params.setDuration(durationMs);
params.setCreateScene("import");    // import | record
params.setNeedOpus(true);           // 是否需要转 opus
params.setVisible(true);
params.setContent("");              // TTS 类型时填播报文本

resource.createPsdkSpeakerResource(params, (error, created) -> {
    // created: PsdkSpeakerResource，含服务端生成的 id
});
```

字段全表：

| 字段 | 类型 | 默认 | 说明 |
|---|---|---|---|
| `name` | String | `""` | 资源名 |
| `fileType` | String | `""` | `audio` / `tts` |
| `objectKey` | String | `""` | 对象存储键 |
| `duration` | long | `0` | 时长（毫秒） |
| `content` | String | `""` | TTS 文本 |
| `createScene` | String | `""` | `import` / `record` |
| `needOpus` | boolean | `false` | 是否转 opus |
| `size` | long | `0` | 文件字节数 |
| `visible` | boolean | `false` | 是否可见 |

### 1.3 编辑

```java
EditPsdkSpeakerResourceParams params = new EditPsdkSpeakerResourceParams();
params.setId("123");          // ⚠️ String，不是 int
params.setName("警告音-新");
params.setFileType("audio");
params.setVisible(false);
params.setContent("");

resource.editPsdkSpeakerResource(params, (error, updated) -> { /* ... */ });
```

```
EditPsdkSpeakerResourceParams {
    String id; String name; String fileType; boolean visible; String content;
}
```

### 1.4 删除

```java
resource.deletePsdkSpeakerResource(resourceId, error -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        // 删除失败
    }
});
```

> ⚠️ **同一条资源 ID 两个方法类型不同**：`deletePsdkSpeakerResource(int resourceId)` 是 `int`，`EditPsdkSpeakerResourceParams.id` 是 `String`。从列表拿到的 `PsdkSpeakerResource.getId()` 是 `int`，编辑时要 `String.valueOf(...)`。

---

## 2. STS 直传（上传音频到对象存储）

这是本模块**唯一的多步流程**，也是最容易写错的部分。

```
getSTS()                                    → STSInfo（bucket / endpoint / credentials）
        ↓
uploadSpeakerAudioFile(filePath, stsInfo)   → 仅 error；成功后文件已进对象存储
        ↓
createPsdkSpeakerResource(params)           → 注册资源，得到 id
        ↓
speakerAudioPlayStart(psdkIndex, info)      → 在 PsdkControlModule 上播放
```

```java
resource.getSTS((error, sts) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS || sts == null) {
        return;
    }

    resource.uploadSpeakerAudioFile("/opt/audio/warning.pcm", sts, uploadError -> {
        if (uploadError.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
            // 上传失败：可能来自对象存储侧
            return;
        }

        CreatePsdkSpeakerResourceParams params = new CreatePsdkSpeakerResourceParams();
        params.setName("warning.pcm");
        params.setFileType("audio");
        params.setObjectKey(sts.getObjectKeyPrefix() + "warning.pcm"); // 以服务端约定为准
        params.setCreateScene("import");
        params.setVisible(true);

        resource.createPsdkSpeakerResource(params, (createError, created) -> {
            // 后续播放：drone.getPsdkControlModule().speakerAudioPlayStart(...)
        });
    });
});
```

### STSInfo / STSCredential

```
STSInfo {
    String objectKeyPrefix;     // 对象键前缀
    String bucket;
    String endpoint;
    String provider;
    String region;
    STSCredential credentials;
}

STSCredential {
    String accessKeyId;
    String accessKeySecret;
    long expire;                // 过期时间戳
    String securityToken;
    int platform;
}
```

- STS 凭证是**临时凭证**，有 `expire`。长时间运行的服务不要缓存复用，每次上传前重新取。
- `uploadSpeakerAudioFile` 的 `filePath` 是**服务端本地路径**（SDK 原生层读文件）。如果应用是容器化部署，要确认文件在该容器的文件系统里可访问。

---

## 3. Widget 资源批量查询

```java
GetPsdkWidgetResourceParams params = new GetPsdkWidgetResourceParams();
params.setObjectKeys(List.of("psdk/widget/1.json", "psdk/widget/2.json"));

resource.getPsdkWidgetResources(params, (error, list) -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS || list == null) {
        return;
    }
    for (PsdkWidgetResource widget : list.getUiResourceUrls()) {  // ⚠️ 不是 getResources()
        String objectKey = widget.getObjectKey();
        String url = widget.getUrl();      // 可直接访问的资源 URL
    }
});
```

```
GetPsdkWidgetResourceParams { List<String> objectKeys }
PsdkWidgetResourceList       { List<PsdkWidgetResource> uiResourceUrls }   // ⚠️ 字段名特殊
PsdkWidgetResource           { String objectKey; String url }
```

`objectKey` 来源：`PsdkControlModule.listenDrcPsdkDeviceList` 推送的 `PsdkDeviceInfo.objectKey`，或 `listenDronePsdkUiResource` 推送的 `PsdkUiResourceItem.objectKey`。

> `PsdkUiResourceItem.psdkIndex` 是 `long`（其它 bean 都是 `int`），详见 `psdk-control.md §4.4`。

---

## 4. PsdkSpeakerResource 字段全表

`getPsdkSpeakerResourceList` / `create` / `edit` 返回的都可能是这个 bean（20 个字段）：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | int | 资源 ID |
| `name` | String | 名称 |
| `fileType` | String | `audio` / `tts` |
| `objectKey` / `objectUrl` | String | 原始对象键 / 访问 URL |
| `format` | String | 文件格式 |
| `size` / `duration` | long | 字节数 / 毫秒 |
| `state` / `stateMessage` | String | 资源状态 / 状态描述 |
| `pcmObjectKey` / `pcmObjectUrl` / `pcmSize` / `pcmMd5Sum` | — | 转码后的 PCM 版本 |
| `content` | String | TTS 文本 |
| `md5Sum` | String | 原始文件 MD5 |
| `createdAt` | long | 创建时间戳 |
| `opusObjectKey` / `opusSize` / `opusMd5Sum` | — | 转码后的 OPUS 版本 |

> 播放时 `PsdkSpeakerAudioPlayInfo` 需要 `url` + `md5`，优先使用原始文件对应的 `objectUrl` / `md5Sum`，不要拿 `pcmMd5Sum` 混填（除非确认播放端要的是转码版本）。

---

## 5. 常见错误清单

1. ❌ `drone.getPsdkResourceModule()` —— 资源模块在 **Workspace** 上
2. ❌ 不判空直接调用 —— `Workspace` 版 getter 在底层模块缺失时返回 `null`
3. ❌ `editPsdkSpeakerResource` 里 `setId(123)` —— `id` 是 **String**
4. ❌ `deletePsdkSpeakerResource` 传 String —— 参数是 **int**
5. ❌ 列表字段写成 `list.getResources()` 读 Widget 资源 —— 它是 `getUiResourceUrls()`
6. ❌ 上传音频时自己拼 STS HTTP 请求 —— 直接用 `getSTS` + `uploadSpeakerAudioFile`
7. ❌ 复用很久以前拿到的 STS 凭证 —— 凭证会过期，每次上传前重新 `getSTS()`
8. ❌ 把 `create` / `edit` 当成「上传文件」—— 它们只登记元数据，文件必须先经 `uploadSpeakerAudioFile` 进对象存储
9. ❌ 以为 `uploadSpeakerAudioFile` 有结果回调 —— 它只有 `(error)`，上传后的访问信息要从 `create` 返回的 bean 里读
10. ❌ 忘记闭环到播放：资源创建完还要在 `PsdkControlModule` 上 `speakerAudioPlayStart`（且需先 `connectDrcLink`）
