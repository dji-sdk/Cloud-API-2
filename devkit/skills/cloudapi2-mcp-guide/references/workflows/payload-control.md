# Payload / Accessory Control (`fh_camera` / `fh_psdk_widget` / `fh_speaker` / `fh_searchlight`) — Decision Tree

> 覆盖 `camera/`、`psdk/`、`searchlight/` 包下 4 个工具。这 4 个工具操作的是**挂载在无人机上的负载设备**（相机云台、PSDK 扩展、喊话器、探照灯），与 `device-control.md` 里的 `fh_payload_acquire`/`fh_payload_release`（负载**控制权**的抢占/释放）是不同层面的概念：`fh_payload_acquire`/`fh_payload_release` 管的是"谁能操控负载"，本文档的 4 个工具管的是"具体怎么操控负载"。除 `fh_searchlight` 外，其余 3 个工具**自己内部**做了负载控制权的获取/释放（`fh_camera` 用 try/finally 包裹自动获取+释放），不需要调用方手动预热 `fh_payload_acquire`。

## Quick Decision Matrix

| User has | Correct tool(s) | Confirm? |
|---|---|---|
| 要拍照/录像/变焦/云台角度调整/查相机状态 | `fh_camera(action=...)` | 否（L0） |
| 要控制 PSDK 扩展模块（设值/输入文本） | `fh_psdk_widget(action=set_value/set_input_text)` | 否（L0） |
| 要用喊话器喊话/播放音频/查状态 | `fh_speaker(action=...)` | 多数 action 需要 |
| 要控制探照灯开关/模式/云台 | `fh_searchlight(action=...)` | 是，且硬依赖已持有飞控权 |

## Decision Flow

```
用户意图：负载/配件控制
         │
         ├─ 相机/云台操作？（L0，无需 confirm，18 个 action）
         │    fh_camera(action=camera_photo/camera_video_start/camera_video_stop/
         │                       gimbal_rotate/zoom/get_camera_state/...)
         │    内部自动：acquirePayloadControlAuthority → 执行 → finally 释放
         │    camera_photo 会自动切换拍照模式、等待就绪、失败重试一次
         │    ⚠️ gimbal_rotate 在真实 SDK 下**未实现**，见 Common Mistakes
         │
         ├─ PSDK 扩展模块？（L0，无需 confirm，2 个 action）
         │    fh_psdk_widget(action=set_value, widget_index, value)
         │    fh_psdk_widget(action=set_input_text, widget_index, text)
         │    类级显式覆写 requiresHumanConfirmation() = false
         │
         ├─ 喊话器？（L1，hybrid 双重门禁：硬 enforceSecurityLevel + 软 requireHumanConfirm）
         │    fh_speaker(action=play_audio/play_tts/stop/set_volume/..., confirm=true)
         │    ⚠️ realtime_start/realtime_stop 在真实 SDK 下**必定失败**（UNSUPPORTED），
         │       mock 下伪装成功——不要用 mock 测试结果去断言真实环境可用
         │    payload_index 默认是 **int 0**（不是字符串）
         │
         └─ 探照灯？（L1，class 级 confirm+security 覆写，唯一有硬依赖的负载工具）
              前置：必须已持有飞控权（fh_control_acquire），否则直接拒绝
              fh_searchlight(action=mode_set/calibration/fine_tuning/
                                     gimbal_control/type_set/wide_field_mode, confirm=true)
              payload_index 是字符串格式 "81-0-0"，内部解析取第二段为 int
```

## Common Mistakes

- ❌ 以为调用这 4 个工具前要先手动 `fh_payload_acquire` "预热" → 除 `fh_searchlight` 外，其余 3 个工具不需要；`fh_camera` 在每个命令 action 内部用 try/finally 自动做 `acquirePayloadControlAuthority`/`releasePayloadControlAuthority`，不需要、也不建议替用户预先抢占
- ❌ 以为 `fh_searchlight` 和 `fh_camera`/`fh_speaker` 一样不需要前置条件 → **不对**，`fh_searchlight` 是这组工具里唯一一个硬依赖 `requireControlAuthority(session, droneSn)` 的——必须已经调用过 `fh_control_acquire` 拿到飞控权，否则直接报错拒绝执行，不会自动补做
- ❌ 以为 `fh_camera(action=gimbal_rotate)` 在真实设备上能正常工作 → 真实 SDK 路径（`executeSdkGimbalRotate`）校验完参数后**无条件返回错误**："Action 'gimbal_rotate' is not yet implemented in real SDK mode"（代码里留有 `// TODO`）；只有 mock 模式下能正常返回成功结果，不要用 mock 测试通过就断定真实环境可用
- ❌ 以为 `fh_speaker(action=realtime_start/realtime_stop)`（实时喊话）在真实设备上能用 → 真实 SDK 两个方法都**无条件**返回 `errorResult("UNSUPPORTED", "...no longer supported in SDK v3.0.0")`；mock 模式会伪装成功，同样不能用 mock 验证真实可用性。需要实时喊话场景时应提前告知用户此功能在当前 SDK 版本下不可用，不要重试
- ❌ 以为 `fh_speaker`/`fh_camera`/`fh_searchlight` 的 `payload_index` 格式统一 → **不统一**：`fh_camera`/`fh_searchlight` 用字符串格式如 `"81-0-0"`（省略时自动探测），`fh_speaker`/`fh_psdk_widget` 用整数，默认 `0`；传错类型会导致参数校验失败或语义错位，生成参数前先确认目标工具的类型
- ❌ 以为 `fh_camera(action=camera_photo)` 失败一次就应该放弃或让用户重试 → 工具内部已经内置"失败自动重试一次"逻辑，且错误消息里会区分航线锁定/正在录像/瞬时繁忙等不同原因，直接把 `errorResult` 里的详细提示转达给用户即可，不需要自己再包一层重试
- ❌ 以为 `fh_psdk_widget` 需要像 `fh_camera`/`fh_speaker` 一样走确认流程 → 不需要，类级显式覆写 `requiresHumanConfirmation()` 返回 `false`，2 个 action（`set_value`/`set_input_text`）均为 L0 直接执行
- ❌ 把"负载控制权"（`fh_payload_acquire`/`fh_payload_release`，见 `device-control.md`）和"飞控权"（`fh_control_acquire`，`fh_searchlight` 的硬依赖）混为一谈 → 两者是完全不同的权限维度：负载控制权管谁能操控云台/负载，飞控权管谁能控制飞行器本身；`fh_searchlight` 依赖的是**飞控权**，不是负载控制权

## Parameter Cheat Sheet

| 工具 | Security | Confirm | 关键参数 | 备注 |
|---|---|---|---|---|
| `fh_camera` | L0（未覆写） | 否 | `action`（18 种，如 camera_photo/camera_video_start/gimbal_rotate/zoom/get_camera_state）, `drone_sn`, `payload_index`（字符串，默认 `"81-0-0"`，省略时自动探测） | 命令类 action 自动获取/释放负载控制权；`gimbal_rotate` 真实 SDK 未实现（仅 mock 可用） |
| `fh_psdk_widget` | L0（未覆写，且显式覆写 `requiresHumanConfirmation()=false`） | 否 | `action`（set_value/set_input_text）, `widget_index`, `value`/`text` | 无前置条件，无陷阱 |
| `fh_speaker` | **L1（REVERSIBLE）**，hybrid 双重门禁 | 多数 action 需要（除只读查询类） | `action`（9 种，含 play_audio/play_tts/stop/set_volume/realtime_start/realtime_stop 等）, `drone_sn`, `payload_index`（**int**，默认 `0`）, `confirm` | `realtime_start`/`realtime_stop` 真实 SDK 必定返回 UNSUPPORTED；支持 `dry_run` |
| `fh_searchlight` | **L1（REVERSIBLE）**，class 级 confirm+security 覆写 | 是 | `action`（mode_set/calibration/fine_tuning/gimbal_control/type_set/wide_field_mode）, `drone_sn`, `payload_index`（字符串 `"type-index-sub"` 格式，内部解析） | **硬依赖**：必须已持有飞控权（`fh_control_acquire`），否则直接拒绝；confirm 提示带 ⚠️ emoji |
