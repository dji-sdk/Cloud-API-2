# OnboardAiModule 参考（机载 AI，18 方法）

## 模块位置与心智模型

```java
// ✅ 挂在 Drone 上
OnboardAiModule onboardAi = drone.getOnboardAiModule();
```

源码（`Drone.java`）：

```java
public synchronized OnboardAiModule getOnboardAiModule() {
    if (onboardAiModuleRef != null) {
        return onboardAiModuleRef;
    }
    try {
        onboardAiModuleRef = new OnboardAiModule(this.drone.getOnboardAiModule());
        Logger.info("Drone", "[" + sn + "] GetOnboardAiModule: OnboardAiModule created");
        return onboardAiModuleRef;
    } catch (Exception e) {
        Logger.error("Drone", "[" + sn + "] GetOnboardAiModule error: " + e.getMessage(), e);
    }
    return null;
}
```

构造失败（底层异常）才会返回 null 并记日志——与 `AIBoxModule` 不同，这里是 try/catch 包裹整个构造，不是简单判空。

### 头号陷阱：18 个方法里有 14 个方法名以**大写字母**开头

`AIIdentifySet`、`AIIdentifyFilterSet`、`AIIdentifyScoreSet`、`AIIdentifyScoreModeSet`、`AIIdentifyScoreReset`、`AIModelSelect`、
`DrcAISpotlightZoomSet`、`DrcAISpotlightZoomSelect`、`DrcAISpotlightZoomConfirm`、`DrcAISpotlightZoomStop`、`DrcAISpotlightZoomTrack`、
`DrcAIAlarmStart`、`DrcAIAlarmStop`、`DrcAIModelsConfig` —— 这 14 个方法**首字母大写**，不是驼峰命名的笔误，**必须原样调用**，
不能“修正”成 `aiIdentifySet()`，否则编译不过。只有 `listenAIDataInfo`/`cancelListenAIDataInfo`/`listenDrcAIAlarmState`/`cancelListenDrcAIAlarmState`
这 4 个方法是正常小写开头的驼峰命名。

## 方法总览（按功能分组）

### 1. AI 数据订阅（1 对 listen/cancel）

| 方法 | 签名 | 链路 |
|---|---|---|
| `listenAIDataInfo` | `int listenAIDataInfo(FlightHubCallback.AiDataInfoCallback callback)` | DRC Northbound，`drc_ai_info_push` |
| `cancelListenAIDataInfo` | `void cancelListenAIDataInfo(int listenId)` | — |

回调体直接把 `generated.AiDataInfo info` 原样传给 `callback.onResult(info)`，**没有经过 `FlightHubSdkConvert` 转换**——
这是本模块里唯一不做 bean 转换、直通 generated 类型的回调（与下面 Identify/ModelSelect 家族的"直通 generated 参数但走 convert 返回错误"不同，
这里是连返回值都不转换）。

### 2. AI 识别开关/过滤/阈值（6 个 action，入参直接是 `generated.*Param`，无 bean 包装层）

| 方法 | 入参类型（均在 `com.dji.flighthubsdk.generated` 包） | DRC method |
|---|---|---|
| `AIIdentifySet` | `AiIdentifySetParam` | `drc_ai_identify_set` |
| `AIIdentifyFilterSet` | `AiIdentifyFilterSetParam` | `drc_ai_identify_filter_set` |
| `AIIdentifyScoreSet` | `AiIdentifyScoreSetParam` | `drc_ai_identify_score_set` |
| `AIIdentifyScoreModeSet` | `AiIdentifyScoreModeSetParam` | `drc_ai_identify_score_mode_set` |
| `AIIdentifyScoreReset` | `AiIdentifyScoreResetParam` | `drc_ai_identify_score_reset` |
| `AIModelSelect` | `AiModelSelectParam` | `drc_ai_model_select` |

**关键区别**：这 6 个方法的参数类型来自 `generated` 包（SWIG 生成的原生层），**不是** `com.dji.flighthubsdk.bean` 包下的业务 bean，
字段访问器是 **snake_case**（例如 `setLight_type`/`getLight_type`、`setScore_mode`/`getScore_mode`），不是 camelCase。
不要凭"SDK 业务层都用 bean"的印象去编造 `AiIdentifyScoreSetParams`（camelCase bean）之类不存在的类。

```java
// ✅ 正确：直接 new generated 包下的 Param，snake_case setter
AiIdentifyScoreResetParam param = new AiIdentifyScoreResetParam();
param.setIndex(0);           // 官方约定 index 固定填 0
param.setLight_type(1);      // 1=广角 / 2=变焦 / 3=红外
onboardAi.AIIdentifyScoreReset(param, error -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) { return; }
});
```

> `AIIdentifyScoreReset` 的 javadoc 原文：“重置 AI 识别分数；2.0 须填 index + light_type（官方 index=0；light_type: 1广角/2变焦/3红外）”——
> `index` 不是设备索引，是固定填 0 的协议约定字段，不要传其它值。

这 6 个方法回调类型都是 `FlightHubCallback.ResultCallback`（单参数 `FlightHubError`，无 result）。

### 3. DRC AI Spotlight Zoom（AI 跟踪，5 方法）

| 方法 | 参数 | DRC method |
|---|---|---|
| `DrcAISpotlightZoomSet` | `int on` | `drc_ai_spotlight_zoom_set` |
| `DrcAISpotlightZoomSelect` | `bean.AiSpotlightZoomSelectParams`（camelCase bean，走 convert） | `drc_ai_spotlight_zoom_select` |
| `DrcAISpotlightZoomConfirm` | 无参 | `drc_ai_spotlight_zoom_confirm` |
| `DrcAISpotlightZoomStop` | 无参 | `drc_ai_spotlight_zoom_stop` |
| `DrcAISpotlightZoomTrack` | `int targetIndex` | `drc_ai_spotlight_zoom_track` |

注意这组方法和上面「AI 识别」组**参数类型体系不同**：`DrcAISpotlightZoomSelect`/`DrcAIAlarmStart`/`DrcAIModelsConfig` 用的是
`com.dji.flighthubsdk.bean.*Params`（camelCase 字段，内部经 `FlightHubSdkConvert.toNativeXxx()` 转成 native 再 `delete()`），
而「AI 识别」组 6 个方法用的是 `generated.*Param`（snake_case，直接传，无 convert、无 delete）。两套参数体系在同一个类里并存，
**不能张冠李戴**。

典型跟踪流程：`DrcAISpotlightZoomSet(1, cb)` 开启跟踪开关 → 收到候选目标后 `DrcAISpotlightZoomSelect(params, cb)` 框选
→ `DrcAISpotlightZoomConfirm(cb)` 确认跟随 → 需要切换目标时 `DrcAISpotlightZoomTrack(targetIndex, cb)` → 结束时
`DrcAISpotlightZoomStop(cb)`。

### 4. DRC AI Alarm（主动告警，3 方法 + 1 对订阅）

| 方法 | 参数 | DRC method |
|---|---|---|
| `DrcAIAlarmStart` | `bean.AiAlarmStartParams` | `drc_ai_alarm_start` |
| `DrcAIAlarmStop` | 无参 | `drc_ai_alarm_stop` |
| `DrcAIModelsConfig` | `bean.AiModelsConfigParams` | `drc_ai_models_config` |
| `listenDrcAIAlarmState` | `int listenDrcAIAlarmState(FlightHubCallback.AiAlarmStateCallback callback)` | DRC Northbound，`drc_ai_alarm_state_push` |
| `cancelListenDrcAIAlarmState` | `void cancelListenDrcAIAlarmState(int listenId)` | — |

`listenDrcAIAlarmState` 的回调同样直通 `generated.AiAlarmStateInfo info`，不经过 `FlightHubSdkConvert`。

Spotlight Zoom 与 Alarm 组方法的回调类型都是 `FlightHubCallback.ResultCallbackWithError`——与第 2 组的 `ResultCallback`
**公开签名完全一样**（都是单参数 `void onResult(FlightHubError error)`），只是内部构造 `FlightHubError` 的代码路径不同
（`invokeAction` 走 `FlightHubSdkConvert.toFlightHubError`，`invokeDrcAiAction` 手工 `new bean.FlightHubError(...)`），
这是内部实现细节，调用方看到的契约没有差异，不要在业务代码里区分对待。

## 前置条件

本模块所有 action（除订阅类）**都走 DRC**，必须先 `drone.getDrcModule().connectDrcLink(...)` 建链成功才能调用，
否则参照 `_shared/constraints-common.yaml` 的 DRC 链路前置规则会直接失败。

## 常见错误清单

1. ❌ 把 14 个大写开头的方法名"修正"为小写驼峰——编译不过，必须原样保留（`AIIdentifySet`、`DrcAIAlarmStart` 等）
2. ❌ 给「AI 识别」6 个方法编造 camelCase bean 参数类——实际是 `generated` 包下 snake_case 的 `Ai*Param`
3. ❌ 把 `listenAIDataInfo`/`listenDrcAIAlarmState` 的回调当成会被转换的 bean——它们直通 `generated.AiDataInfo`/`generated.AiAlarmStateInfo`
4. ❌ `AIIdentifyScoreReset` 的 `index` 当成设备/云台索引随便填——官方约定固定为 0
5. ❌ `light_type` 取值记反——1=广角，2=变焦，3=红外
6. ❌ 混用 Spotlight Zoom/Alarm 组的 `bean.*Params`（camelCase）与 AI 识别组的 `generated.*Param`（snake_case）
7. ❌ 跳过 `connectDrcLink` 直接调用 action 方法
