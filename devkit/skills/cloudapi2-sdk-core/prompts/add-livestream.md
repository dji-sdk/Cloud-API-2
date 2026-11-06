# 添加直播功能

## 目标
开启/停止无人机或机场的视频直播流。

## 涉及 SDK API
- `FlightHubSDK.getDroneInstance(sn)` → Drone（无人机直播）
- `FlightHubSDK.getDockInstance(sn)` → Dock（机场视角直播）
- `drone.getLiveStream(LiveInfo params)` → LiveStream — 获取直播流对象
- `dock.getLiveStream(LiveInfo params)` → LiveStream — 获取直播流对象
- `liveStream.start(ResultCallbackWithError callback)` — 开始直播（有回调）
- `liveStream.stop()` — 停止直播（**无回调**，void 方法）

## LiveInfo 参数字段
- `deviceSn: String` — 设备序列号
- `payloadIndex: String` — 相机/负载索引（如 `"81-0-0"`）
- `videoId: String` — 视频流 ID

## 必须遵守的规则
- ✅ 规则 2：`start()` 回调中检查 errorCode
- ✅ 规则 5：getDroneInstance / getDockInstance null 检查
- ✅ 同时直播流不超过 4 路
- ⚠️ `stop()` 是 void 方法，**没有回调**，不要等待它的结果

## 实现步骤

1. **创建 LiveService**
   - 获取 Drone 或 Dock 实例（null 检查）
   - 构建 `LiveInfo` 参数（deviceSn、payloadIndex、videoId）
   - 调用 `getLiveStream(liveInfo)` 获取 LiveStream 对象
   - 调用 `liveStream.start(callback)` 开始直播，在回调中检查 errorCode
   - 管理活跃直播流列表（控制上限，保存 LiveStream 引用供后续 stop）

2. **创建 Controller**
   - POST `/api/live/start` — 开始直播
   - POST `/api/live/stop` — 停止直播（调用 `liveStream.stop()`）
   - GET `/api/live/status` — 查询当前活跃流

3. **停止直播**
   - 从活跃流列表中找到对应 LiveStream
   - 调用 `liveStream.stop()`（无回调，直接清理引用）

## 注意事项
- 直播依赖 WebRTC/RTMP，确认网络环境支持
- 前端播放需要 HTTPS 环境（WebRTC 限制）
- 停止直播时确保移除活跃流引用，避免内存泄漏

## 常见错误
- ❌ 同时开超过 4 路直播（连接失败/卡顿）
- ❌ 不调 `liveStream.stop()` 就关闭页面（流资源未释放）
- ❌ 在前端直接调用 SDK 直播接口（凭证暴露）
- ❌ 误用 `dock.getLiveModule()` —— 正确 API 是 `getLiveStream(LiveInfo)`
- ❌ 对 `stop()` 传入回调或等待结果 —— `stop()` 是 void，没有回调

---

## 直播录制（LiveRecordModule）—— ⚠️ DJI 尚未开放使用

> **本模块全部 5 个方法的源码 javadoc 均标注 `@deprecated: 注意，本接口暂未开放使用`**。这不是「已废弃、有替代方案」的常规 `@Deprecated` 语义，而是 DJI **尚未正式开放**此能力——不要在生产集成代码里直接启用，调用前必须向用户说明该模块当前不可用于正式业务，仅可用于预研/联调验证。

### 模块位置

```java
LiveRecordModule liveRecord = drone.getLiveRecord(liveInfo);   // 或 dock.getLiveRecord(liveInfo)
```

> ⚠️ **getter 名字是 `getLiveRecord(LiveInfo)`，不是 `getLiveRecordModule()`**——与本文件前半部分 `getLiveStream(LiveInfo)` 的命名风格一致（都省略了 `Module` 后缀），但容易按其他模块的命名习惯写成 `getLiveRecordModule()` 导致编译失败。

### 缓存陷阱：Drone/Dock 内部按 payloadIndex 缓存，忽略 videoId/deviceSn

`Drone`/`Dock` 内部用 `ConcurrentHashMap<String, LiveRecordModule>`，**key 只用 `params.getPayloadIndex()`**，完全忽略 `LiveInfo` 里的 `videoId`/`deviceSn`：

```java
// Drone.java / Dock.java 内部实现（简化）
if (liveRecordModuleRef.containsKey(params.getPayloadIndex())) {
    return liveRecordModuleRef.get(params.getPayloadIndex());
}
// ... 否则 new 一个并以 payloadIndex 为 key put 进去
```

意味着：**同一个 `payloadIndex`、不同 `videoId` 的两次 `getLiveRecord(LiveInfo)` 调用，会拿到同一个 `LiveRecordModule` 实例**。如果业务上需要按 `videoId` 区分录制对象，不能依赖这个缓存天然隔离，必须在应用层自己用 `videoId` 做二次路由。

### 5 个方法

| 方法 | 回调形态 | 说明 |
|---|---|---|
| `startLiveRecord(String timeZone, callback)` | **`(error)`** | 开始录制 |
| `stopLiveRecord(callback)` | `(error)` | 停止录制 |
| `getDeviceLiveRecords(String deviceSn, callback)` | `(error, List<LiveRecordItem>)` | 查询设备的录制列表 |
| `listenRecordState(callback)` / `cancelListenRecordState(listenId)` | 单参数，无 error | 订阅录制状态 |

### 示例（仅用于预研/联调，不要用于正式业务）

```java
LiveRecordModule liveRecord = drone.getLiveRecord(liveInfo);
if (liveRecord == null) {
    return;
}

liveRecord.startLiveRecord("Asia/Shanghai", error -> {
    if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
        return;
    }
});

liveRecord.getDeviceLiveRecords(deviceSn, (error, list) -> {
    // list: List<LiveRecordItem>
});
```

### 常见错误
- ❌ 在正式业务代码里直接集成本模块而不向用户说明「DJI 尚未开放使用」—— 必须先提示该限制
- ❌ 写成 `drone.getLiveRecordModule()` —— 正确方法名是 `getLiveRecord(LiveInfo)`
- ❌ 假设不同 `videoId` 能拿到独立的 `LiveRecordModule` 实例 —— 缓存只按 `payloadIndex` 区分，`videoId`/`deviceSn` 不参与缓存键
- ❌ 给 `startLiveRecord`/`stopLiveRecord` 的回调多写一个 `result` 参数 —— 两者都只有 `(error)`
