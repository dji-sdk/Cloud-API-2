# listenXxx 收不到回调 — 排查清单

SDK 的 `listenXxx` 注册成功（返回 `listen_id ≥ 0`）却收不到事件，是 ISV 侧最容易「怀疑自己代码」的一类问题。**实际上大部分根因不在你的代码里**：要么监听错了事件，要么订阅的 SN 不对，要么平台本身就不推这个事件。

适用于 RCA **Stage 2 Locate** —— 按下面顺序排查，命中即止。

## 排查顺序

```
1. 监听的是不是你以为的那个事件？   → 见「事件语义混淆」
2. 订阅用的 SN 对不对？             → 见「订阅 SN 陷阱」
3. 平台到底推不推这个事件？         → 见「平台已知缺失」
4. 上游动作真的发生了吗？           → 用 MCP 触发一次（见 live-verification.md）
5. 监听生命周期是否被提前销毁？     → 见「生命周期自检」
```

## 1. 事件语义混淆（最高频）

事件名字长得像，语义完全不同。**先确认你要的到底是哪件事**：

| 你想监听的事 | 正确的 SDK 方法 | 触发时机 |
|---|---|---|
| 设备**上线 / 下线**（拓扑变更） | `listenDeviceTopoChanged` | 设备与机场之间的连接拓扑发生变化 |
| 工作空间**新增 / 删除**设备（注册关系） | `listenWorkspaceDeviceChanged` | 设备被绑定到 workspace |
| 设备 OSD 状态变化（位置/电量/姿态） | `listenDeviceOsd` | 设备遥测定频推送 |
| 设备状态变化 | `listenDeviceState` | 设备状态字段变更 |
| 设备健康告警 | `listenDeviceHms` | HMS 告警产生 |
| 飞行任务状态变更 | `listenFlightTaskStatus` | 任务状态流转 |
| 文件上传信息 / 状态 | `listenFileUploadInfo` / `listenFileUploadState` | 媒体文件上传 |

**典型错配**：想做「设备上下线大屏」却监听了 `listenWorkspaceDeviceChanged` —— 那个事件只在**绑定关系**变化时推，设备断电重连不会触发。

## 2. 订阅 SN 陷阱

事件按 `{DeviceSN}-{Topic}` 作为频道分发。**订阅时填的 SN 必须与平台发布时用的 SN 完全一致**，否则频道对不上，事件静默丢失（不会报错）。

| 事件 | 发布用的 SN | 常见错误 |
|---|---|---|
| `listenDeviceTopoChanged` | **网关 SN（Airport SN）** | 用 Dock/子设备自身 SN 订阅 → 收不到 |
| `listenWorkspaceDeviceChanged` | **空字符串** | 用具体设备 SN 订阅 → 收不到 |
| `listenDeviceOsd` / `listenDeviceState` / `listenDeviceHms` | 设备自身 SN | 一般不会错 |

拓扑事件的例子：拓扑中 `Hosts=[PFK6...（Airport）]`、`Others=[PFB6...（Dock）]`，平台发布的频道是 `PFK6...-topic_device_topo_change`。如果你拿 `PFB6...` 去订阅，永远收不到。

**自检方法**：先用 `fh_device_list` 看清设备树 —— airport 是 gateway（顶层），drone/dock 是它的子设备。拓扑类事件订阅顶层 gateway 的 SN。

## 3. 平台已知缺失（不是你的 bug）

以下场景**平台本身不发送 WS 推送**，代码写得再对也收不到：

| 场景 | 现状 | 替代方案 |
|---|---|---|
| **设备从工作空间删除** | 不推送任何事件 | 轮询设备列表接口做差集比对 |
| **非 Airport 设备绑定**（虚拟设备、直连设备、Web/API 手动添加） | 不推送 `listenWorkspaceDeviceChanged` | 同上；绑定动作由你侧发起时，以接口返回为准 |

只有**真实 Airport 硬件主动发起绑定**时，才会触发工作空间设备变更事件。

碰到这两个场景 → 直接给用户「平台当前不支持，用轮询兜底」的结论，不要继续在 SDK 侧找原因。

## 4. 上游动作是否真的发生

前三步都排除后，问题可能是「事件压根没被触发」而不是「触发了没收到」。用 MCP 主动制造一次事件来区分：

- 拓扑事件 → 在模拟器上让设备重连
- 任务状态事件 → `fh_wayline_execute` 跑一条航线
- 媒体事件 → `fh_camera(camera_photo)` 拍一张

详见 `live-verification.md`。**收到 = 你的监听没问题，是上游条件没满足；仍收不到 = 回到第 2 步复核 SN。**

## 5. 生命周期自检

排除平台侧后，回头检查这几条硬约束（详见 `cloudapi2-sdk-core`）：

- [ ] `initialize` 已 **await 完成**才注册监听 —— 提前调用会静默返回 `null`
- [ ] `listenXxx` 返回值 **≥ 0**（负数 = 注册失败，见 `error-codes.yaml` 的 `ERROR_LISTEN_FAILED`）
- [ ] 监听没有在组件销毁 / `@PreDestroy` 里被**提前 cancel**
- [ ] 没有**重复注册**同类型监听导致后注册的覆盖前一个
- [ ] 回调里先判 `error.error_code === 0` 再用 `data` —— 回调其实到了，但被异常吞掉的情况很常见（回调内加日志确认）

## 结论落点

| 命中步骤 | 结论类型 | 后续 |
|---|---|---|
| 1 / 2 / 5 | ISV 侧可修 | 进 Stage 3 Fix |
| 3 | 平台限制 | 给轮询兜底方案，无需 Fix |
| 4 仍无法触发 | 需升级 | 走 `diag.sh report`，附设备 SN + 时间点报 DJI |
