---
name: cloudapi2-payload
description: |
  DJI Cloud API 2 PSDK 负载域 SDK 接入助手。当用户要编写 Java 代码控制第三方 PSDK 负载
  （探照灯 / 喊话器 / PSDK Widget / 浮窗文本 / PSDK UI 资源），或管理喊话器音频资源
  （上传、STS 直传、资源 CRUD）时调用。
  触发词：PsdkControlModule、PsdkResourceModule、PSDK 负载、探照灯、searchlight、喊话器、speaker、
  音频播放、TTS、语音播报、Megaphone、widget、widgetValueSet、输入框、浮窗、floatingWindow、
  PSDK UI 资源、psdkIndex、speakerAudioPlayStart、speakerTTSPlayStart、searchlightModeSet、
  speakerPlayVolumeSet、getSTS、uploadSpeakerAudioFile、喊话器资源、DRC 负载控制；
  也包括排查"探照灯/喊话器指令下发无反应""listen 返回 -1""取消订阅失败"这类**DRC 链路缺失或订阅签名**问题
  （控制指令走 DRC，必须先 connectDrcLink）。
  不触发：调用 MCP Server 的 fh_searchlight / fh_speaker / fh_psdk_widget / fh_camera 工具做运维操作（→ cloudapi2-mcp-guide）、
  通用 SDK 规则/飞行控制/航线任务（→ cloudapi2-sdk-core）、地图域（→ cloudapi2-map）、
  设备生命周期与固件升级（→ cloudapi2-device-management）、SEI/OSD 遥测（→ cloudapi2-sei-osd）。
version: 1.0.0
author: DJI Cloud API 2 Team
---

# DJI Cloud API 2 PSDK 负载域接入助手

## 这个 Skill 是干什么的

帮集成商开发者正确编写调用 **DJI Cloud API 2 Java SDK PSDK 负载模块**的集成代码：`PsdkControlModule`（34 方法）与 `PsdkResourceModule`（7 方法）。

两者的共同点是都只服务于 **PSDK 第三方负载**（探照灯、喊话器、Widget 等），与相机/云台（`CameraControlModule`）是两套完全不同的模型：

- **索引类型不同**：PSDK 负载索引是 `int psdkIndex`；相机负载索引是 `String`（`"99-0-0"`）
- **传输链路不同**：控制指令走 **DRC**，资源管理走 **HTTP**，状态推送走 **DRC Northbound / WebSocket**
- **取用对象不同**：`getPsdkControlModule()` 在 `Drone` 上，`getPsdkResourceModule()` 在 `Workspace` 上

**核心价值**：本域最贵的坑不是 API 签名，而是**能力边界**——Java SDK 里有一批方法被注释关闭或从未实现（实时喊话、抛投器），
而枚举与类 javadoc 仍在"过度声明"，按字面生成代码必然编译失败或运行时不可用。

## 何时触发

- 用户要写代码控制 PSDK 探照灯（开关、亮度、模式、校准、微调、广角、随云台）
- 用户要写代码控制 PSDK 喊话器（播放音频 / TTS、重播、停止、音量、播放模式、TTS 参数）
- 用户要订阅 PSDK 设备列表、状态摘要、播放进度、浮窗文本、UI 资源
- 用户要做 PSDK Widget 数值 / 输入框设置，或批量获取 Widget 资源 URL
- 用户要上传喊话器音频（STS 凭证 + 直传对象存储）并做资源 CRUD
- 用户遇到相关报错：`psdkIndex` 不知道从哪来、listen 返回 -1、callback 收到 null、cancel 编译不过

**不触发**：

- 用户要**运行时直接调用 MCP Tool**（`fh_searchlight` / `fh_speaker` / `fh_psdk_widget` / `fh_camera`）→ `cloudapi2-mcp-guide`
- 通用 SDK 规则（初始化、鉴权、回调顺序、listen/cancel 配对）、飞行控制、航线任务、相机/云台控制 → `cloudapi2-sdk-core`
- 地图域（飞行区、离线地图、解锁证书、地图元素）→ `cloudapi2-map`
- 设备生命周期、固件升级、HMS 告警 → `cloudapi2-device-management`
- SEI/OSD 遥测、视频流 → `cloudapi2-sei-osd`

## 模块位置（先确认挂在哪）

```java
// ✅ 控制模块在 Drone 上
PsdkControlModule control = drone.getPsdkControlModule();

// ✅ 资源模块在 Workspace 上
PsdkResourceModule resource = workspace.getPsdkResourceModule();

// ❌ 反过来取都不存在
// drone.getPsdkResourceModule()      —— 不存在
// workspace.getPsdkControlModule()   —— 不存在
```

| 模块 | 挂载对象 | 方法数（源码核实） | 能力 | 详细文档 |
|---|---|---:|---|---|
| `PsdkControlModule` | `Drone` | 34 | 探照灯 / 喊话器 / Widget / 浮窗 / PSDK UI 资源（DRC + DRC Northbound + WebSocket） | `references/psdk-control.md` |
| `PsdkResourceModule` | `Workspace` | 7 | 喊话器资源 CRUD / STS 直传 / Widget 资源（HTTP） | `references/psdk-resource.md` |

**两者的判空行为不同**：

- `workspace.getPsdkResourceModule()` 在底层模块缺失时**返回 `null`** 并记 `Logger.error` → 必须判空
- `drone.getPsdkControlModule()` **没有 null 返回分支**（底层为 null 会构造失败）→ 判空无法防御，只能保证业务层做了 PSDK 检测

## 核心规则

### 规则 1：控制指令走 DRC，必须先 `connectDrcLink`

16 个 setter 全部通过 DRC 下发；6 组 DRC Northbound 订阅也依赖 DRC 链路。

```java
DrcModule drc = drone.getDrcModule();
drc.connectDrcLink(error -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        return;               // 建链失败，不要继续下发控制指令
    }
    // 建链成功后才能调用 PsdkControlModule 的 setter / listen
});
// 退出时
drc.disconnectDrcLink(error -> { /* 记录结果 */ });
```

不要漏掉这一步——HTTP 类接口（`PsdkResourceModule`）不需要 DRC，但**创建完资源去播放时又回到 DRC**。

### 规则 2：Java SDK 没有实时喊话，也没有抛投器

| 你以为有 | 实际 |
|---|---|
| `speakerRealTimeStart` / `speakerRealTimeStop` | **源码中已注释关闭**。实时喊话需要 RTC 房间 + 浏览器 WebRTC + 麦克风采集，Java 侧无配套能力 |
| 抛投器 / 投放器控制 | 类 javadoc 提到"抛投器等负载控制"，但**类里没有任何对应方法** |

替代方案：TTS 用 `speakerTTSPlayStart`，音频文件用 `speakerAudioPlayStart`（先经 `PsdkResourceModule` 上传）。

> `MegaphoneWorkMode` 枚举里仍有 `LIVE_VOICE(3)` / `REAL_TIME(4)` / `PLAYING_IN_TRANSMISSION(5)`，
> 但**没有 Java 方法能进入这些模式**。枚举值存在 ≠ 能力存在。

### 规则 3：`psdkIndex` 是 int，来自状态摘要回调

```java
int listListenId = control.listenDrcPsdkDeviceList(list -> {
    if (list == null) { return; }
    for (PsdkDeviceInfo device : list.getDeviceInfos()) {
        int psdkIndex = device.getPsdkIndex();   // int
    }
});

int summaryListenId = control.listenDrcPsdkStateSummaryInfo(psdkIndex, summary -> {
    if (summary == null) { return; }             // listenId == -1 时会收到 null
    PsdkType type = summary.getPsdkType();
});
```

| 索引 | 类型 | 来源 |
|---|---|---|
| PSDK 负载 | `int` | `PsdkStateSummaryInfo.getPsdkIndex()` / `PsdkDeviceInfo.getPsdkIndex()` |
| 相机负载 | `String` | `"type-subtype-gimbalindex"`，如 `"99-0-0"` |

不要凭设备顺序猜 `psdkIndex`，也不要和相机负载索引互换。

### 规则 4：所有 listen 都是单参数无 error，失败靠返回值和判空感知

| 家族 | 失败时的行为 |
|---|---|
| DRC Northbound（6 个） | catch 中**以 `null` 转换后的 bean 回调一次**，返回 `-1` |
| WebSocket（3 个） | catch 中**只记日志、不回调**，返回 `-1` |

```java
int listenId = control.listenDrcPsdkStateSearchlightInfo(psdkIndex, info -> {
    if (info == null) {
        return;   // 订阅失败路径
    }
    // 正常推送
});
if (listenId < 0) {
    // 统一处理订阅失败（尤其 WebSocket，回调不会来）
}
```

### 规则 5：cancel 签名分两类，参数顺序不同

```java
// 带 psdkIndex 的（6 个 DRC 状态类）：psdkIndex 在前，listenId 在后
control.cancelListenDrcPsdkStateSummaryInfo(psdkIndex, listenId);
control.cancelListenDrcPsdkStateSpeakerInfo(psdkIndex, listenId);
control.cancelListenDrcPsdkStateSearchlightInfo(psdkIndex, listenId);
control.cancelListenDrcPsdkSpeakerPlayProgressInfo(psdkIndex, listenId);
control.cancelListenDrcPsdkFloatingWindowTextInfo(psdkIndex, listenId);

// 只有 listenId 的（设备列表 + 3 个 WebSocket）
control.cancelListenDrcPsdkDeviceList(listenId);
control.cancelListenPsdkSpeakerAudioPlayStartProgress(listenId);
control.cancelListenPsdkSpeakerTtsPlayStartProgress(listenId);
control.cancelListenDronePsdkUiResource(listenId);
```

所有 cancel 返回 `void`，内部异常只记日志，**没有错误信号**。必须自己保存每个 listenId 并在 `@PreDestroy` 配对取消。

### 规则 6：入参类型不对称，不能照抄

| 方法 | 该传 | 常见错误 |
|---|---|---|
| `searchlightModeSet(psdkIndex, mode, cb)` | `int`（`SearchLightWorkMode.getValue()`） | 传枚举对象 → 编译失败 |
| `speakerPlayModeSet(psdkIndex, playMode, cb)` | `MegaphonePlayMode` 枚举 | 传 int → 编译失败 |
| `speakerTTSSet(psdkIndex, config, cb)` | `PsdkSpeakerTTSconfig` | 自己拼 int 参数 |
| `deletePsdkSpeakerResource(resourceId, cb)` | `int` | 传 String |
| `EditPsdkSpeakerResourceParams.setId(...)` | `String` | 传 int |

另外两个 bean 字段命名陷阱：`PsdkSpeakerResourceList` 用 `getResources()`，而 `PsdkWidgetResourceList` 用 **`getUiResourceUrls()`**；
`PsdkUiResourceItem.getPsdkIndex()` 返回 **`long`**，其它 PSDK bean 都是 `int`。

### 规则 7：音频资源是四步流程，每步都有独立失败点

```
getSTS()                                   → STSInfo（临时凭证，会过期）
        ↓
uploadSpeakerAudioFile(path, stsInfo)      → 仅 error；SDK 内部直传对象存储，不经过后端
        ↓
createPsdkSpeakerResource(params)          → 登记元数据，得到 int id
        ↓
connectDrcLink → speakerAudioPlayStart     → 在 PsdkControlModule 上播放
```

`create` 不是上传，`upload` 不需要先 `create`；STS 凭证不要长期缓存。

## 决策树路由

```
用户问题
├─ 探照灯：开/关/亮度/模式/校准/微调/广角/随云台/灯类型
│    → references/psdk-control.md#3-探照灯控制drc7-个方法
├─ 喊话器：播放音频/TTS/重播/停止/音量/播放模式/TTS 参数
│    → references/psdk-control.md#2-喊话器控制与-widgetdrc9-个方法
├─ 实时喊话 / 边喊边播 / 对讲机模式
│    → 本文件「规则 2」（Java SDK 不支持，给替代方案）
├─ PSDK 设备发现 / 状态摘要 / 状态子集（喊话器、探照灯）
│    → references/psdk-control.md#1-psdk-设备发现与状态订阅drc-northbound6-组
├─ 播放进度 / 浮窗文本 / UI 资源订阅
│    → references/psdk-control.md#2-喊话器控制与-widgetdrc9-个方法 / #5-widget--输入框--ui-资源
├─ PSDK Widget 数值 / 输入框文本设置
│    → references/psdk-control.md#5-widget--输入框--ui-资源
├─ 音频资源上传（STS / 直传）/ 资源增删改查 / 资源列表
│    → references/psdk-resource.md
├─ Widget 资源 objectKey → 可访问 URL
│    → references/psdk-resource.md#3-widget-资源批量查询
├─ psdkIndex 从哪来 / 类型不对 / 和相机索引混用
│    → 本文件「规则 3」+ references/psdk-control.md#psdkindex-是什么
├─ listen 返回 -1 / 回调收到 null / cancel 编译不过
│    → 本文件「规则 4、5」
├─ 指令下发无反应
│    → 本文件「规则 1」（先检查 connectDrcLink）
└─ 要调 MCP 工具做运维（不写代码）
     → 转交 cloudapi2-mcp-guide
```

## 工作流

本 Skill 遵循 `skills/_shared/sop-template.md` 定义的 7 章节 SOP，按 L1/L2/L3 自动选档。

### Ch2 澄清问题

PSDK 负载需求必须澄清的点（缺一项就可能写错）：

1. **负载类型**：探照灯 / 喊话器 / Widget，还是只是查设备清单（决定订阅哪组状态）
2. **`psdkIndex` 是否已知**：未知则先写设备发现 + 状态订阅，不要硬编码 0
3. **音频来源**：TTS 文本、已有资源 ID，还是本地文件（本地文件走 STS 上传四步流程）
4. **DRC 链路是否已建立**：控制类需求必须先 `connectDrcLink`（HTTP 资源管理不需要）
5. **运行环境是服务端还是浏览器**：实时喊话只能在浏览器侧实现，服务端只能给 TTS / 音频播放

### Ch5 编码约束

参见 `constraints.yaml`，所有 `critical` 规则在生成代码时必须满足；通用 SDK 规则（初始化、鉴权、null 检查、
listen/cancel 配对）引用 `_shared/constraints-common.yaml`，不在本文件重复。

### Ch6 验证扩展

除通用验证外，本域代码必须检查：

- [ ] 模块 getter 取用对象正确（控制 → `drone`，资源 → `workspace`），资源模块已判空
- [ ] 控制类调用前有 `connectDrcLink` 成功分支，退出时有 `disconnectDrcLink`
- [ ] 没有生成 `speakerRealTimeStart` / `speakerRealTimeStop` / 抛投器相关 API
- [ ] `psdkIndex` 来自状态回调（`int`），未与相机负载索引 `String` 混用
- [ ] 所有 listen 回调是**单参数**，DRC Northbound 回调体有 `null` 判空
- [ ] 每个 `listenId` 已保存并配对 cancel，且 cancel 参数个数/顺序与 `references/` 一致
- [ ] `searchlightModeSet` 传 int、`speakerPlayModeSet` 传枚举，未写反
- [ ] 音频上传流程包含 `getSTS` 失败、上传失败、创建失败三处中止分支
- [ ] Widget 资源列表用 `getUiResourceUrls()` 读取

## 反幻觉规则（强制）

1. **示例代码必须来源于本 Skill 的 references/ 或用户提供的文档**，不得凭模型记忆生成 API 签名、字段名、枚举值
2. **禁止按枚举或 javadoc 反推 API**——本域已确认存在「枚举有值但无方法」（`MegaphoneWorkMode` 的 3/4/5）、「javadoc 声明但未实现」（抛投器）、「源码注释关闭」（实时喊话）三类过度声明
3. **引用必须可溯源**——任何建议必须能追溯到 `references/` 文档路径或官方 SDK 源码（`core/PsdkControlModule.java`、`core/PsdkResourceModule.java`）

## 权威参考

| 文档 | 内容 |
|---|---|
| `references/psdk-control.md` | `PsdkControlModule` 全部 34 个方法：DRC 建链前置、6 组 DRC Northbound 订阅、3 组 WebSocket 订阅、16 个 setter、bean/enum 与入参类型对照 |
| `references/psdk-resource.md` | `PsdkResourceModule` 全部 7 个方法：资源 CRUD、STS 直传四步流程、Widget 资源批量查询、bean 字段表 |
| `skills/_shared/constraints-common.yaml` | 跨 Skill 通用约束（回调/初始化/null 检查） |

## 共享规范

> 跨 Skill 共享约束见 `skills/_shared/constraints-common.yaml`。
> 跨 Skill 路由规则见 `skills/_shared/skill-routing.md`。当发现用户意图属于其他 Skill 职责时，按转交协议切换。
> 输出格式见 `skills/_shared/output-format.md`。Reference 按需懒加载，不要一次性读取所有 references/。
