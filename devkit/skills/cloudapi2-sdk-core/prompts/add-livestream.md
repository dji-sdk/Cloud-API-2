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
