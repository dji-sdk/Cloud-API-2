# Device Query & Basic Control — Decision Tree

> 覆盖 `fh_device_list` / `fh_device_manage` / `fh_dock_control` / `fh_dock_snapshot` / `fh_drone_snapshot` 5 个工具。
> 这 5 个工具都是 Legacy 模式实现（无 `@McpTool` 注解），类级 `getSecurityLevel()` 均未覆写，**实际安全等级与类注释描述不完全一致**，务必按本文档标注的"实测行为"而非类注释判断。

## Quick Decision Matrix

| User has | Correct tool(s) | Key parameter |
|---|---|---|
| 要查设备列表（在线/离线、哪些机型） | `fh_device_list` | `type` = drone/dock/airport/base_station/all，`online_only` |
| 要查某台设备详情（固件版本/在线/工作状态） | `fh_device_manage(action=detail)` | `device_sn`，只读 |
| 要重命名设备 | `fh_device_manage(action=rename)` | `device_sn`, `new_name`, `confirm=true` |
| 要解绑设备（不可逆） | `fh_device_manage(action=unbind)` | `device_sn`, `confirm=true` |
| 要查设备当前 HMS 健康告警 | `fh_device_manage(action=hms_current)` | `device_sn`，只读 |
| 要开/关机场舱盖、充电、补光灯 | `fh_dock_control(action=...)` | `dock_sn`, `confirm=true`（6 个 action 全部需要） |
| 要查机场实时位置/舱盖状态/电源状态/是否有机入库 | `fh_dock_snapshot` | `dock_sn`，只读 |
| 要查无人机实时位置/电量/姿态/速度 | `fh_drone_snapshot` | `drone_sn`，只读 |

## Decision Flow

```
用户意图：设备查询 / 日常设备控制
         │
         ├─ 想看有哪些设备、在不在线？
         │    → fh_device_list(type=drone|dock|airport|base_station|all, online_only=true|false)
         │    ⚠️ 顶层只有 airport/base_station，drone/dock 挂在子设备层；
         │       机场内 drone 显示 offline 是正常现象（关机下电），不能据此判断机场能否执行任务——
         │       要判断能否飞，看 airport.online，不要看 drone.online
         │
         ├─ 想看/改某台设备的基本信息？
         │    ├─ 查详情       → fh_device_manage(action=detail, device_sn=...)
         │    ├─ 改名字       → fh_device_manage(action=rename, device_sn=..., new_name=..., confirm=true)
         │    ├─ 解绑         → fh_device_manage(action=unbind, device_sn=..., confirm=true)   ⚠️ 不可逆
         │    └─ 查健康告警   → fh_device_manage(action=hms_current, device_sn=...)
         │
         ├─ 想控制机场物理部件（舱盖/充电/补光灯）？
         │    → fh_dock_control(dock_sn=..., action=cover_open|cover_close|charge_start|charge_stop|light_on|light_off, confirm=true)
         │    ⚠️ 6 个 action 全部需要 confirm=true，没有例外
         │    ⚠️ 不要和 fh_device_debug 的 open_cover/close_cover/open_charge/close_charge 混淆——
         │       两者功能相似但走不同 SDK 模块，详见 device-debug.md
         │
         └─ 想查实时遥测快照？
              ├─ 机场快照 → fh_dock_snapshot(dock_sn=...)   （位置/舱盖状态/电源状态/是否有机入库）
              └─ 无人机快照 → fh_drone_snapshot(drone_sn=...) （位置/电量/姿态/速度）
```

## Common Mistakes

- ❌ 用 `drone.online=false` 判断机场能否执行任务 → 机场内挂载的无人机通常处于关机下电状态，`online=false` 是**正常现象**，不是故障；应该看 `airport.online`
- ❌ 以为 `fh_device_list` 的 `type=drone` 能直接从返回的顶层结构里筛出来 → 顶层只会是 `airport`/`base_station`，`drone`/`dock`/`relay` 都在 `sub_devices` 里，工具内部已做摊平处理，调用方拿到的是摊平后的扁平列表，不需要自己再去遍历子设备
- ❌ `fh_device_manage(action=rename/unbind)` 的 `confirm` 传字符串 `"true"` → 必须传 **JSON boolean** `true`，该工具内部用的是裸类型强转校验，传字符串会导致请求失败而不是被温和拒绝
- ❌ 在 `FH_MOCK=true` 环境下测试 `fh_device_manage` 的 `detail`/`rename`/`unbind` 并期待和 `fh_device_list`/两个 snapshot 工具一样有干净的 mock 返回 → 这 3 个 action **在 mock 模式下大概率不可靠**（mock workspace 是非 null 的假对象，不会短路进专门的 mock 分支，会继续调用真实 SDK 方法作用在假对象上）；只有 `hms_current` 在 mock 下能干净返回"无告警"或"设备不存在"，不会出异常
- ❌ 在 `FH_MOCK=true` 环境下测试 `fh_dock_control` → 该工具**完全没有 mock 分支**，无论 `dock_sn` 是否存在，mock 模式下必定返回"Dock not found"，无法用来验证真实控制逻辑是否生效
- ❌ `fh_dock_snapshot`/`fh_drone_snapshot` 传了一个不存在的 SN，看到返回里有数据就当作查询成功 → 这两个工具查不到真实设备时**不会报错**，而是静默返回一份带 `_mock: true` 的假数据；调用方必须检查响应里的 `_mock` 字段，不能只看"有没有报错"来判断 SN 是否有效
- ❌ 看 `fh_drone_snapshot` 返回的 message 文案里 "alt=N/A" 就以为高度字段缺失 → 这是工具内部一个展示层的字段名 bug（message 模板取的是 `altitude`，实际字段名是 `height`），不影响 `data.height` 本身的值，直接读 `data` 字段即可

## Parameter Cheat Sheet

### fh_device_list

| Parameter | Value | Note |
|---|---|---|
| `type` | drone / dock / airport / base_station / all | 可选，默认 `all` |
| `online_only` | true / false | 可选，默认 `false` |

> 无 `action` 参数，无需 `confirm`——纯只读查询。返回里带 `next_steps`，通常引导到 `fh_drone_snapshot`/`fh_dock_snapshot`。

### fh_device_manage

| Parameter | Value | Note |
|---|---|---|
| `action` | detail / rename / unbind / hms_current | 必填（类注释提到的 `firmware_list` **未实现**，调用会报 `Unknown action`） |
| `device_sn` | 设备 SN | 必填 |
| `new_name` | 新名称 | rename 必填 |
| `confirm` | true（**JSON boolean**，不要传字符串） | rename/unbind 必填 |
| `dry_run` | true | 可选，仅 rename/unbind 支持预览 |

> `hms_current` 内部会依次尝试当作 drone/dock 查询，10 秒超时未收到告警视为"健康"（不算错误）。

### fh_dock_control

| Parameter | Value | Note |
|---|---|---|
| `dock_sn` | 机场 SN | 必填 |
| `action` | cover_open / cover_close / charge_start / charge_stop / light_on / light_off | 必填 |
| `confirm` | true | **全部 6 个 action 必填**，无例外 |
| `dry_run` | true | 可选，预览而不执行 |

> 找不到 `dock_sn` 时错误提示会引导"先调用 `fh_device_list` 确认可用机场"。

### fh_dock_snapshot

| Parameter | Value | Note |
|---|---|---|
| `dock_sn` | 机场 SN | 必填 |

> 返回字段：`latitude/longitude/height/heading/cover_state/power_state/drone_in_dock`，可能带 `_warning`（部分遥测通道超时未响应）。**SN 不存在时静默返回 mock 数据，务必检查 `_mock` 字段。**

### fh_drone_snapshot

| Parameter | Value | Note |
|---|---|---|
| `drone_sn` | 无人机 SN | 必填 |

> 返回字段：`latitude/longitude/height/elevation/battery_percent/battery_landing_power/pitch/roll/yaw/horizontal_speed/vertical_speed`，可能带 `_warning`。**SN 不存在时同样静默返回 mock 数据**，`message` 文案里的高度展示有已知 bug（见 Common Mistakes），读 `data.height` 即可。

### fh_device_recommend — 智能选机

> 用户已经有一批候选设备（通常来自 `fh_device_list`），想让系统按策略（默认：就近、高电量优先）挑出最合适的一台。L0，无 `action`，无需 `confirm`。

**典型调用链**：`fh_device_list`（筛出在线的 drone/dock）→ `fh_device_recommend(candidate_sns=[...])` → 结果喂给 `fh_wayline_upload`/`fh_flight_task(create)`。

| Parameter | Value | Note |
|---|---|---|
| `candidate_sns` | string[] | 必填，由调用方提供——该工具**不会**自己去查设备列表 |
| `strategy_id` | int，默认 0 | "0=默认/就近"；**代码层完全没有枚举/范围校验**，传什么都会原样转发给 SDK |
| `fly_to_latitude` / `fly_to_longitude` / `fly_to_height` | 可选 | ⚠️ **必须三者同时提供才生效**——只传 1-2 个会被静默丢弃（不报错、不警告），请求会在没有目标位置的情况下发出 |

**关键陷阱**：
- 调用前必须先有 `candidate_sns`——没有现成候选列表时先调 `fh_device_list` 筛一遍在线设备，不要指望这个工具自己查
- `fly_to_*` 三个参数是"全有或全无"，如果用户只给了经纬度没给目标高度（或反之），不要直接调用，应该先补全或明确告知该坐标条件不会生效
- 想用某条航线的起点坐标作为 `fly_to_*`：这个工具不接受 `wayline_uuid`，需要自己先从 `fh_wayline_recommend` 的结果里取 `start_latitude`/`start_longitude`，再手动传进来
- **Mock 触发条件和 `fh_wayline_recommend` 不一样**：`fh_device_recommend` 只要 `workspace==null`（还没连接/初始化）也会静默返回 mock 数据，而不是报错——容易误判为"已经连接真实环境查询成功"
- Mock 响应结构（`recommended_sn`/`scores[]`/`reason`）和真实响应结构（`devices:[{droneSn,gatewaySn,distance,matched}]`）**完全不重叠**，不能用 mock 返回去断言真实环境的字段；mock 的 `recommended_sn` 恒等于 `candidate_sns[0]`，不能用它验证策略是否真的生效
- 真实模式下**工具自己不做筛选和排序**——拿到 `devices[]` 后需要调用方自己过滤 `matched=true` 再按 `distance` 升序排
- **不会**设置 `next_steps`（无论 mock 还是真实模式），不要假设响应里会有下一步提示；拿到结果后 `gatewaySn` 接 `fh_flight_task(create)` 的 `sn`，`droneSn` 接 `fh_wayline_upload` 的 `drone_sn`
