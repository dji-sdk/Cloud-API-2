# PsdkControlModule — 探照灯 / 喊话器 / PSDK Widget / UI 资源

## 模块位置与心智模型

`PsdkControlModule` 挂在 **Drone** 上（不是 Dock、不是 Workspace）：

```java
PsdkControlModule psdk = drone.getPsdkControlModule();
```

```java
// Drone.java:314-319 内部实现（简化）
public synchronized PsdkControlModule getPsdkControlModule() {
    if (psdkControlModuleRef == null) {
        psdkControlModuleRef = new PsdkControlModule(this.drone.getPsdkControlModule(), sn);
    }
    return psdkControlModuleRef;
}
// ⚠️ 源码中暂无 null 守卫：底层 raw module 为 null 时会在构造时 NPE，而不是返回 null
```

> 对照：`PsdkResourceModule` 挂在 **Workspace** 上（见 `psdk-resource.md`）。两个模块的取用对象不同，不能互换。

**34 个顶层方法**，跨 **三种传输链路**：

| 分组 | 方法数 | 链路 |
|---|---:|---|
| PSDK 设备发现 / 状态订阅 | 6 组 listen + cancel = 12 | DRC Northbound（`drc_psdk_ui_resource` / `drc_psdk_state_info` / `drc_speaker_play_progress` / `drc_psdk_floating_window_text`） |
| 播放进度 / UI 资源订阅 | 3 组 listen + cancel = 6 | WebSocket（`topic_psdk_speaker_audio_play_start_progress` / `topic_psdk_speaker_tts_play_start_progress` / `topic_drone_psdk_ui_resource`） |
| 探照灯 + 喊话器 + Widget 控制 | 16 | DRC（`drc_light_*` / `drc_speaker_*` / `drc_psdk_widget_value_set` / `drc_psdk_input_box_text_set`） |

> **大部分控制方法走 DRC**，因此**必须先建立 DRC 链路**（见下一节）。这是本域最容易漏掉的前置条件。

## ⚠️ 头号陷阱：`speakerRealTimeStart` / `speakerRealTimeStop` 在 Java SDK 中不存在

源码中这两个方法的完整实现被**注释关闭**，javadoc 原文：

> Java SDK 场景无 RTC 与音频采集能力，此接口无实际用途，已注释关闭。

原因：实时喊话（边喊边播）的完整链路需要三步，Java SDK 一步都不具备：

1. HTTP POST 申请语音通道房间（Java SDK 未暴露该接口）
2. 浏览器端 WebRTC / `@volcengine/rtc` 加入房间
3. `getUserMedia` 采集麦克风（Java 侧没有浏览器环境）

**替代方案**：

| 需求 | 用哪个方法 |
|---|---|
| 播一段文字（TTS） | `speakerTTSPlayStart(psdkIndex, PsdkSpeakerTTSPlayInfo, callback)` |
| 播一段已上传的音频 | `speakerAudioPlayStart(psdkIndex, PsdkSpeakerAudioPlayInfo, callback)`（音频需先经 `PsdkResourceModule` 上传） |

**另一个连带陷阱**：`MegaphoneWorkMode` 枚举里仍保留 `LIVE_VOICE(3, 暂不支持)`、`REAL_TIME(4)`、`PLAYING_IN_TRANSMISSION(5)`，但**没有任何 Java 方法能进入这些模式**。看到枚举值不等于能调用对应能力，不要据此生成代码。

## 类 javadoc 的过度声明：抛投器

`PsdkControlModule` 类 javadoc 写着"提供 PSDK 设备管理、喊话器/探照灯/**抛投器**等负载控制"，但类里**没有任何抛投器（dropper）方法**。以方法签名为准，不要编造 `dropperRelease()` 之类的 API。

## 前置条件：先建立 DRC 链路

16 个 setter 全部走 DRC，因此调用前必须：

```java
DrcModule drc = drone.getDrcModule();

// 1. 建立 DRC 链路（异步，等回调成功再下发控制指令）
drc.connectDrcLink(error -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        // 建链失败：禁止继续下发控制指令
        return;
    }
    // 2. 建链成功后才调用 PsdkControlModule 的控制方法
});

// 3. 退出时必须断开
drc.disconnectDrcLink(error -> { /* 记录结果 */ });
```

`DrcModule` 还提供 `listenLinkStatus` / `cancelListenLinkStatus`、`drcRtcQualityTest`、`listenDrcDelayInfo` / `cancelListenDrcDelayInfo`、`listenDrcCountDownInfo` / `cancelListenDrcCountDownInfo`，可用于链路状态与质量观测。

> **DRC Northbound 的 6 组 listen 同样依赖 DRC 链路**，订阅前应先确认链路已建立，否则可能一直收不到推送。

## 回调形态

| 形态 | 接口 | 适用 |
|---|---|---|
| `onResult(FlightHubError error)` | `FlightHubCallback.ResultCallbackWithError` | **全部 16 个 setter** |
| `onResult(Bean)` **单参数、无 error** | `PsdkDeviceListCallback` / `PsdkStateSummaryInfoCallback` / `PsdkStateSpeakerInfoCallback` / `PsdkStateSearchlightInfoCallback` / `PsdkSpeakerPlayProgressInfoCallback` / `PsdkFloatingWindowTextInfoCallback` | 6 个 DRC Northbound listen |
| `onResult(generated.Msg)` **无 error** | `PsdkSpeakerAudioPlayStartProgressCallback` / `PsdkSpeakerTtsPlayStartProgressCallback` | 2 个 WebSocket 播放进度 listen |
| `onResult(bean.DronePsdkUiResource)` **无 error** | `DronePsdkUiResourceCallback` | 1 个 WebSocket UI 资源 listen |

> 所有 listen 都**没有 error 参数**。订阅成败只能看返回的 `int listenId`：`>= 0` 成功，`-1` 失败。

### listen 失败时的行为差异（必须区别对待）

| 家族 | 失败行为 |
|---|---|
| DRC Northbound（6 个） | catch 中会**以 null 转换后的 bean 回调一次**（`callback.onResult(FlightHubSdkConvert.toXxx(null))`），然后返回 `-1` |
| WebSocket（3 个） | catch 中**只记日志，不回调**，返回 `-1` |

因此：

- DRC Northbound 的回调体**必须判空**，否则 `-1` 场景下会 NPE
- WebSocket 场景下**不能依赖回调得知失败**，必须检查返回值

```java
int listenId = psdk.listenDrcPsdkStateSummaryInfo(0, info -> {
    if (info == null) {
        // 订阅失败（listenId == -1 时会走到这里）
        return;
    }
    // 正常推送
});
if (listenId < 0) {
    // 处理订阅失败
}
```

### cancel 的签名不一样（高频写错点）

| 方法 | 签名 |
|---|---|
| `cancelListenDrcPsdkDeviceList` | `(int listenId)` |
| `cancelListenDrcPsdkStateSummaryInfo` | `(int psdkIndex, int listenId)` |
| `cancelListenDrcPsdkStateSpeakerInfo` | `(int psdkIndex, int listenId)` |
| `cancelListenDrcPsdkStateSearchlightInfo` | `(int psdkIndex, int listenId)` |
| `cancelListenDrcPsdkSpeakerPlayProgressInfo` | `(int psdkIndex, int listenId)` |
| `cancelListenDrcPsdkFloatingWindowTextInfo` | `(int psdkIndex, int listenId)` |
| `cancelListenPsdkSpeakerAudioPlayStartProgress` | `(int listenId)` |
| `cancelListenPsdkSpeakerTtsPlayStartProgress` | `(int listenId)` |
| `cancelListenDronePsdkUiResource` | `(int listenId)` |

**按 psdkIndex 订阅的，cancel 时也要带 psdkIndex，且参数在 listenId 之前**。漏传会编译失败，传反会取消不掉。

所有 `cancelListenXxx` 返回 `void`，内部异常只记日志、**没有错误信号**——取消失败无法从返回值感知，只能自行记录。

## `psdkIndex` 是什么

每次 javadoc 都重复同一句话：PSDK 负载序号是**整型**，来自 `listenDrcPsdkStateSummaryInfo` 回调里 `PsdkStateSummaryInfo.psdk_index`。

```
PSDK 负载索引：  int          （0、1、2…，来自状态摘要回调）
相机负载索引：   String       （格式 "type-subtype-gimbalindex"，如 "99-0-0"）
```

> 两者类型不同，**不能互相传**。PSDK 负载的 `psdkIndex` 也不能凭设备顺序猜测，必须先订阅状态拿到真实值。

---

## 1. PSDK 设备发现与状态订阅（DRC Northbound，6 组）

| 方法 | 链路 tag | 推送内容 |
|---|---|---|
| `listenDrcPsdkDeviceList(PsdkDeviceListCallback)` → `int` | `drc_psdk_ui_resource` | 设备列表（聚合去重后按 PSDK 索引透传） |
| `listenDrcPsdkStateSummaryInfo(int psdkIndex, ...)` → `int` | `drc_psdk_state_info` | 通用状态摘要（类型/子类型/名称/SN/版本/限制状态） |
| `listenDrcPsdkStateSpeakerInfo(int psdkIndex, ...)` → `int` | `drc_psdk_state_info` | 喊话器状态子集 |
| `listenDrcPsdkStateSearchlightInfo(int psdkIndex, ...)` → `int` | `drc_psdk_state_info` | 探照灯状态子集 |
| `listenDrcPsdkSpeakerPlayProgressInfo(int psdkIndex, ...)` → `int` | `drc_speaker_play_progress` | 喊话器播放进度 |
| `listenDrcPsdkFloatingWindowTextInfo(int psdkIndex, ...)` → `int` | `drc_psdk_floating_window_text` | 浮窗文本 |

### 1.1 先发现设备，再拿 psdkIndex

```java
int listListenId = psdk.listenDrcPsdkDeviceList(deviceList -> {
    if (deviceList == null) {
        return;  // 订阅失败
    }
    for (PsdkDeviceInfo device : deviceList.getDeviceInfos()) {
        int psdkIndex = device.getPsdkIndex();     // int
        String objectKey = device.getObjectKey();  // UI 资源对象键
    }
});

// 用拿到的 psdkIndex 订阅具体负载状态
int summaryListenId = psdk.listenDrcPsdkStateSummaryInfo(0, summary -> {
    if (summary == null) {
        return;
    }
    PsdkType type = summary.getPsdkType();           // SPEAKER / SEARCHLIGHT / ...
    PsdkSubType subType = summary.getPsdkSubType();
    String name = summary.getPsdkName();
});
```

Bean 字段：

```
PsdkDeviceList   { List<PsdkDeviceInfo> deviceInfos }
PsdkDeviceInfo   { int psdkIndex; String objectKey }

PsdkStateSummaryInfo {
    int psdkIndex;
    PsdkType psdkType;           // UNOFFICIAL(4 第三方) / OFFICIAL(5 官方) / MANIFOLD3(8 妙算3) / UNKNOWN(65535)
    PsdkSubType psdkSubType;     // SPEAKER(0) / SEARCHLIGHT(1) / UNKNOWN(65535)
    String psdkName, psdkSn, psdkVersion, psdkLibVersion;
    int psdkRestrictState;
}
```

> `PsdkDeviceList` 与 `PsdkStateSummaryInfo` 是 bean 类，字段访问器是 camelCase `getPsdkIndex()`。但**播放进度消息是 generated 类**，访问器是 snake_case `getPsdk_index()`（见 §2.3），两者不要混用。

### 1.2 状态订阅的 bean

```
PsdkStateSpeakerInfo {
    int workMode;                 // 对应 MegaphoneWorkMode 的 value
    int playMode;                 // 0 SINGLE_PLAY / 1 LOOP_PLAYBACK
    int systemState;              // 原始 int，Java 侧没有对应的枚举类
    int playVolume;
    String playFileName, playFileMd5;
    int ttsVolume, ttsType, ttsLanguage, ttsSpeed;
}

PsdkStateSearchlightInfo {
    SearchLightWorkMode workMode; // 枚举
    int brightness;
    int calibrationStatus, calibrationProgress;
    double leftValue, rightValue;
    int wideFieldMode, lightGimbalControl, lightingType;
}

PsdkSpeakerPlayProgressInfo {
    int psdkIndex; int result; String status; String stepKey; int percent; String md5;
}

PsdkFloatingWindowTextInfo { int psdkIndex; String floatingWindowText; }
```

> `PsdkStateSpeakerInfo` 的状态字段**全是原始 int**（没有强类型枚举）。`PsdkStateSearchlightInfo.workMode` 才是枚举。二者不对称，不要统一处理。
> `systemState` 在 javadoc 中提及 `PSDKMegaphoneSystemState`，该枚举**类本身存在**（`value/PSDKMegaphoneSystemState.java`），但**没有任何字段/方法把它引用为类型**（orphan enum），不要据此假设有强类型支持，`systemState` 仍按原始 `int` 处理。

---

## 2. 喊话器控制与 Widget（DRC，9 个方法）

| 方法 | 作用 |
|---|---|
| `speakerAudioPlayStart(int psdkIndex, PsdkSpeakerAudioPlayInfo info, ResultCallbackWithError cb)` | 播放已上传音频 |
| `speakerTTSPlayStart(int psdkIndex, PsdkSpeakerTTSPlayInfo info, ResultCallbackWithError cb)` | 播放 TTS |
| `speakerReplay(int psdkIndex, ResultCallbackWithError cb)` | 重播上一次音频 |
| `speakerPlayStop(int psdkIndex, ResultCallbackWithError cb)` | 停止播放 |
| `speakerPlayVolumeSet(int psdkIndex, int volume, ResultCallbackWithError cb)` | 设置播放音量 |
| `speakerPlayModeSet(int psdkIndex, MegaphonePlayMode playMode, ResultCallbackWithError cb)` | 播放模式（单次 / 循环） |
| `speakerTTSSet(int psdkIndex, PsdkSpeakerTTSconfig config, ResultCallbackWithError cb)` | 设置 TTS 参数（音量/音色/语言/语速） |
| `widgetValueSet(int psdkIndex, int index, int value, ResultCallbackWithError cb)` | 设置 PSDK Widget 数值 |
| `widgetInputBoxTextSet(int psdkIndex, String value, ResultCallbackWithError cb)` | 设置 PSDK 输入框文本 |

> `speakerPlayModeSet` 是本模块**唯一**接收枚举的控制方法；其余取值（音量、亮度、模式）都是原始 `int`。详见 §4.3。

### 2.1 播放音频

```java
PsdkSpeakerAudioPlayInfo audio = new PsdkSpeakerAudioPlayInfo();
audio.setName("warning.pcm");     // 默认 ""
audio.setUrl(objectUrl);          // 默认 ""
audio.setMd5(md5);                // 默认 ""
audio.setFormat("pcm");           // 默认 "pcm"

psdk.speakerAudioPlayStart(psdkIndex, audio, error -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        // 下发失败
    }
});
```

### 2.2 播放 TTS

```java
PsdkSpeakerTTSPlayInfo tts = new PsdkSpeakerTTSPlayInfo();
tts.setName("语音播报");
tts.setText("前方有障碍物，请注意安全");
tts.setMd5(md5);

psdk.speakerTTSPlayStart(psdkIndex, tts, error -> { /* 检查 error */ });
```

### 2.3 播放进度（WebSocket）

```java
int listenId = psdk.listenPsdkSpeakerAudioPlayStartProgress(progress -> {
    // ⚠️ progress 是 generated 包下的类，psdk_index 访问器是 snake_case
    PsdkSpeakerOutputDetail output = progress.getOutput();
    int idx = output.getPsdk_index();
    int result = progress.getResult();
    String status = output.getStatus();
    String gateway = progress.getGateway();
    String sn = progress.getSn();
});
if (listenId < 0) {
    // WebSocket 订阅失败：回调不会被触发，只能靠返回值感知
}
```

```
generated.PsdkSpeakerPlayStartProgress {
    String getGateway();
    PsdkSpeakerOutputDetail getOutput();
    int getResult();
    String getSn();
}
generated.PsdkSpeakerOutputDetail {
    String getMd5();
    int getPsdk_index();   // 注意 snake_case
    String getStatus();
}
```

TTS 进度用 `listenPsdkSpeakerTtsPlayStartProgress(PsdkSpeakerTtsPlayStartProgressCallback)`，消息类型同为 `generated.PsdkSpeakerPlayStartProgress`。

### 2.4 TTS 参数与音量

```java
PsdkSpeakerTTSconfig cfg = new PsdkSpeakerTTSconfig();
cfg.setVolume(70);
cfg.setType(MegaphoneTTSVoiceType.WOMAN);        // MAN(0) / WOMAN(1)
cfg.setLanguage(MegaphoneTTSLanguageType.CHINESE); // CHINESE(0) / ENGLISH(1)
cfg.setSpeed(1);
psdk.speakerTTSSet(psdkIndex, cfg, error -> { /* ... */ });

psdk.speakerPlayVolumeSet(psdkIndex, 80, error -> { /* ... */ });
```

---

## 3. 探照灯控制（DRC，7 个方法）

| 方法 | 作用 |
|---|---|
| `searchlightModeSet(int psdkIndex, int mode, ResultCallbackWithError cb)` | 工作模式（**原始 int**，不是枚举） |
| `searchlightBrightnessSet(int psdkIndex, int brightness, ...)` | 亮度 |
| `searchlightCalibration(int psdkIndex, ...)` | 校准 |
| `searchlightFineTuningSet(int psdkIndex, int position, double value, boolean saved, ...)` | 微调（保存开关） |
| `searchlightWideFieldModeSet(int psdkIndex, int wideFieldMode, ...)` | 广角模式 |
| `searchlightLightGimbalControlSet(int psdkIndex, int lightGimbalControl, ...)` | 灯随云台联动 |
| `searchlightTypeSet(int psdkIndex, int type, ...)` | 灯类型 |

工作模式取值（`SearchLightWorkMode` 枚举的 value，但**入参是 int**）：

| int | 枚举 | 含义 |
|---:|---|---|
| 0 | `CLOSE` | 关闭 |
| 1 | `OPEN` | 常亮 |
| 2 | `STROBE` | 频闪 |
| 3 | `QUICK_STROBE` | 快频闪 |
| 4 | `ALTERNATION_STROBE` | 交替频闪 |
| 65535 | `UNKNOWN` | 未知 |

```java
// 打开探照灯（传 SearchLightWorkMode.OPEN.getValue()，不要传枚举对象）
psdk.searchlightModeSet(psdkIndex, SearchLightWorkMode.OPEN.getValue(), error -> { /* ... */ });

psdk.searchlightBrightnessSet(psdkIndex, 100, error -> { /* ... */ });
```

> ⚠️ `searchlightModeSet` 的第二个参数类型是 **`int`**，传 `SearchLightWorkMode.OPEN` 会编译失败。
> 但 `speakerPlayModeSet` 的第二个参数**是枚举** `MegaphonePlayMode`。两个控制方法风格不同，不能照抄。

---

## 4. 枚举与取值参照

### 4.1 `PsdkType` / `PsdkSubType`

| 枚举 | 值 |
|---|---|
| `PsdkType` | `UNOFFICIAL(4)`, `OFFICIAL(5)`, `MANIFOLD3(8)`, `UNKNOWN(65535)` |
| `PsdkSubType` | `SPEAKER(0)`, `SEARCHLIGHT(1)`, `UNKNOWN(65535)` |

### 4.2 `MegaphonePlayMode` / `MegaphoneWorkMode`

| 枚举 | 值 |
|---|---|
| `MegaphonePlayMode` | `SINGLE_PLAY(0)`, `LOOP_PLAYBACK(1)`, `UNKNOWN(65535)` |
| `MegaphoneWorkMode` | `TTS(0)`, `VOICE(1 微信语音模式)`, `TTS_APP(2)`, `LIVE_VOICE(3 暂不支持)`, `REAL_TIME(4 边喊边播)`, `PLAYING_IN_TRANSMISSION(5)`, `UNKNOWN(65535)` |

> `MegaphoneWorkMode` 只用于解读状态订阅返回的 `workMode`。其中 3/4/5 **没有对应的 Java 控制方法**（见头号陷阱）。

### 4.3 入参类型对照（最容易写错的地方）

| 方法 | 该传什么 | 常见错误 |
|---|---|---|
| `searchlightModeSet` | `int`（`SearchLightWorkMode.getValue()`） | 传枚举对象 → 编译失败 |
| `speakerPlayModeSet` | `MegaphonePlayMode` 枚举 | 传 `int` → 编译失败 |
| `speakerTTSSet` | `PsdkSpeakerTTSconfig`（字段用枚举） | 自己拼 int 参数 → 编译失败 |
| `widgetValueSet` | `int index, int value` | 把 `index` 当成 psdkIndex |
| `speakerPlayVolumeSet` | `int volume` | 传 0–100 之外的浮点值 |

### 4.4 `PsdkUiResourceItem.psdkIndex` 是 `long`

```java
DronePsdkUiResource { List<PsdkUiResourceItem> psdkUiResource; }
PsdkUiResourceItem  { long psdkIndex; long psdkReady; String objectKey; }
```

所有其它 PSDK bean 的 `psdkIndex` 都是 `int`，**唯独这个 bean 是 `long`**。传给控制方法时需要显式收窄（并确认值在 int 范围内），不要直接赋值。

---

## 5. Widget / 输入框 / UI 资源

```java
psdk.widgetValueSet(psdkIndex, widgetIndex, 42, error -> { /* ... */ });
psdk.widgetInputBoxTextSet(psdkIndex, "航线备注", error -> { /* ... */ });

// UI 资源订阅走 WebSocket
int listenId = psdk.listenDronePsdkUiResource(resource -> {
    if (resource == null) {
        return;   // 注意：此处不会为 null（WS 失败不回调），保留判空仅为防御
    }
    for (PsdkUiResourceItem item : resource.getPsdkUiResource()) {
        long idx = item.getPsdkIndex();      // long
        long ready = item.getPsdkReady();
        String objectKey = item.getObjectKey();
    }
});
```

`objectKey` 是对象存储键，要拿到可访问 URL 需走 `PsdkResourceModule.getPsdkWidgetResources(...)`（见 `psdk-resource.md`）。

---

## 6. 常见错误清单（生成代码前逐条自检）

1. ❌ 调用 `speakerRealTimeStart` / `speakerRealTimeStop` —— **这两个方法在 Java SDK 中不存在**（源码注释关闭）
2. ❌ 编造抛投器 / 投放器 API —— 类 javadoc 提到但类中没有对应方法
3. ❌ 直接调用 setter 而不先 `connectDrcLink` —— 控制指令下发不出去
4. ❌ 用 `dock.getPsdkControlModule()` —— 模块挂在 `Drone` 上
5. ❌ `cancelListenDrcPsdkStateSummaryInfo(listenId)` —— 少传 `psdkIndex`，编译失败
6. ❌ `cancelListenDrcPsdkStateSummaryInfo(listenId, psdkIndex)` —— 参数顺序反了，取消不掉
7. ❌ 给 listen 回调加 `(error, bean)` 两个参数 —— 所有 listen 都是单参数
8. ❌ 在 DRC Northbound 回调里不判空 —— `listenId == -1` 时会收到 `null` bean
9. ❌ 依赖 WebSocket listen 的回调感知失败 —— WS 失败只返回 `-1`，不触发回调
10. ❌ `searchlightModeSet(psdkIndex, SearchLightWorkMode.OPEN, cb)` —— 该参数是 `int`
11. ❌ `speakerPlayModeSet(psdkIndex, 1, cb)` —— 该参数是 `MegaphonePlayMode` 枚举
12. ❌ 把相机负载索引 `"99-0-0"` 传给 `psdkIndex` 形参 —— PSDK 索引是 `int`，取自状态摘要回调
13. ❌ 假设 `PsdkUiResourceItem.getPsdkIndex()` 返回 `int` —— 它是 `long`
14. ❌ 用 `getPsdkIndex()` 读 `generated.PsdkSpeakerPlayStartProgress` —— 那是 `getOutput().getPsdk_index()`（snake_case）
15. ❌ 忘记配对 cancel —— listenId 每个都要保存，`@PreDestroy` 中逐个取消
