# Device Control / DRC (`fh_control_*` / `fh_drc_*` / `fh_takeoff` / `fh_flyto` / `fh_return_home` / `fh_poi` / `fh_emergency_stop` / `fh_payload_*` / `fh_stick_move` / `fh_osd_snapshot`) — Decision Tree

> 覆盖 `drc/` 包下 13 个工具（`fh_live_start` 直播工具不在本文档范围，已在 SKILL.md 内联覆盖）。此前 SKILL.md 只有 1 行内联提示（"暂未实现独立工作流文档，参考标准链路"），本文档补齐完整决策树。
> 全部 13 个工具均为 **Legacy 模式**（无 `@McpTool` 注解），前置条件由代码内显式调用（`requireControlAuthority`/`requireDrcLink`/`ensureControlAuthority`/`ensureDrcLink`）实现，**不是**声明式 `prerequisites={...}`。确认机制统一用 `requireHumanConfirm()`（elicitation-first），没有裸 `(Boolean)` 强转。

## Quick Decision Matrix

| User has | Correct tool(s) | Confirm? |
|---|---|---|
| 要抢占/释放飞控权 | `fh_control_acquire` / `fh_control_release` | 不需要 |
| 要建立/断开 DRC 实时控制链路 | `fh_drc_connect` / `fh_drc_disconnect` | 不需要 |
| 要起飞 | `fh_takeoff` | 需要 |
| 要飞向某坐标点 / 查进度 / 停止飞行 / 更新目标点 | `fh_flyto(action=start/progress/stop/update)` | start/update 需要 |
| 要返航 / 取消返航 | `fh_return_home(action=start/cancel)` | 需要 |
| 要绕点飞行（环绕某兴趣点）/ 停止绕飞 / 查状态 | `fh_poi(action=start/stop/status)` | start 需要 |
| 要紧急停止 / 紧急降落 / 强制降落 / 开伞 / 悬停保活（**最高风险**） | `fh_emergency_stop(mode=hover/landing/force_landing/parachute/hover_hold)` | 需要，且另有独立二次校验 |
| 要抢占/释放云台负载控制权 | `fh_payload_acquire` / `fh_payload_release` | 不需要 |
| 要摇杆微调方向（前后左右上下/自旋） | `fh_stick_move` | 需要 |
| 要查无人机实时 OSD 遥测 | `fh_osd_snapshot` | 不需要 |

## Decision Flow

```
用户意图：远程控制 / DRC 飞行操作
         │
         ├─ 抢控/连接链路（通常由下游工具自动补做，无需手动预热）
         │    fh_control_acquire(drone_sn) ──(硬依赖)──> fh_drc_connect(drone_sn)
         │    └─ 释放时顺序相反：fh_drc_disconnect(drone_sn) ──(硬依赖)──> fh_control_release(drone_sn)
         │
         ├─ 飞行动作？（L2，需 confirm，确认后自动补做抢控+连DRC，即"自愈型"）
         │    起飞：       fh_takeoff(drone_sn, dock_sn, target_height, confirm=true)
         │    飞向坐标：   fh_flyto(action=start, latitude, longitude, height, confirm=true)
         │                 → fh_flyto(action=progress) 查进度（只读，无需confirm）
         │                 → fh_flyto(action=update/stop, confirm=true) 更新目标/停止
         │    绕点飞行：   fh_poi(action=start, latitude, longitude, radius, confirm=true)
         │                 → fh_poi(action=status) 查状态（只读）
         │                 → fh_poi(action=stop, confirm=true) 停止
         │    摇杆微调：   fh_stick_move(drone_sn, direction, duration_ms, confirm=true)
         │                 ⚠️ 调用会阻塞约 duration_ms，不是立即返回
         │
         ├─ 返航？（L2，需 confirm，只需控制权，不需要 DRC）
         │    fh_return_home(action=start, confirm=true)
         │    fh_return_home(action=cancel, confirm=true) 取消返航
         │
         ├─ 紧急停止？（L3，全项目最高等级，需 confirm + 独立 operator_token 二次校验）
         │    fh_emergency_stop(drone_sn, confirm=true, reason=..., operator_token=..., mode=hover|landing|force_landing|parachute|hover_hold)
         │    ⚠️ 必须已持有控制权，不会自动抢控；mock 模式也不豁免 operator_token 校验
         │
         ├─ 云台/负载控制权？（L1，不需要 confirm，独立于 DRC）
         │    fh_payload_acquire(drone_sn)  ⚠️ 硬依赖已持有飞控权（fh_control_acquire）
         │    fh_payload_release(drone_sn)  （与飞控释放顺序无交叉依赖，任意顺序均可）
         │
         └─ 只是查遥测？（L0，无需任何前置条件，任意状态可调）
              fh_osd_snapshot(drone_sn)
```

## Common Mistakes

- ❌ 以为调用 `fh_takeoff`/`fh_flyto`/`fh_poi`/`fh_stick_move` 前必须手动先调 `fh_control_acquire` + `fh_drc_connect` "预热" → 这 4 个工具在人工确认**之后**会自动调用内部 `ensureDrcLink()`，静默补做抢控+连DRC；不需要、也不建议替用户预先调用这两个工具去"预热"链路，徒增一次无意义的 API 调用
- ❌ 以为 `fh_drc_disconnect` 之后随便调一个飞行工具就会自动把链路重新连上 → **自愈只对"从未建立过链路"生效**。用户显式调用 `fh_drc_disconnect` 后，session 会为该校 SN 打上"显式断开"标记，此后 `fh_takeoff`/`fh_flyto`/`fh_poi`/`fh_stick_move` 一律返回错误（"DRC link to ... was explicitly disconnected"）且**不会抢控、不会建链**；必须由用户显式调 `fh_drc_connect` 恢复，建链成功后标记自动解除。干跑（`dry_run=true`）不触发任何抢控/连链动作
- ❌ 以为 `fh_return_home` 也需要先 `fh_drc_connect` → 不需要，`fh_return_home` 走的是管理通道（`ensureControlAuthority`），只需要飞控权，和 DRC 链路完全无关
- ❌ 以为 `fh_control_release`/`fh_drc_disconnect` 可以任意顺序调用 → 代码里硬性要求先 `fh_drc_disconnect` 再 `fh_control_release`，顺序反了会报错"DRC link still active, call fh_drc_disconnect first"
- ❌ 以为 `fh_drc_connect` 会自己处理抢控 → 不会，`fh_drc_connect` 对飞控权是**硬校验**（`requireControlAuthority`），没有就直接报错"call fh_control_acquire first"，不像 takeoff/flyto 那样自动补救
- ❌ 以为 `mode=hover` 和 `mode=hover_hold` 是一回事 → **完全不同**：`hover` 是 `drcEmergencyStop`（空中停桨，飞机自由坠落），`hover_hold` 是 `drcSetDroneHover(duration)`（续期悬停保活定时器，飞机保持悬停）。把"保持悬停"误写成 `mode=hover` 会直接导致坠机
- ❌ 以为 `fh_emergency_stop` 和其它 L2 飞行工具一样，确认后会自动抢控 → **不会**，这是全组 13 个工具里唯一一个不会自动抢控的 L2+ 工具，必须控制权已经持有，否则直接报错，不会替用户抢控后再执行急停
- ❌ 以为 `FH_MOCK=true` 下 `fh_emergency_stop` 可以跳过 `operator_token` 校验去测试流程 → 不行，这是全组唯一一个 mock 比真实环境更严格的工具，`verifyOperatorToken()` 在 mock/real 分发之前无条件执行，未配置 `FH_OPERATOR_TOKEN` 时工具直接判定为"未校验"而拒绝执行（"宁可不可用，不可让 L3 裸奔"）
- ❌ 以为 `fh_stick_move` 是异步、立即返回的指令 → 实际上调用会**阻塞**约 `duration_ms`（100-5000ms，超出范围会被静默 clamp），真实模式和 mock 模式都一样阻塞（mock 跑同一套 50ms 间隔调度循环，只是发送端是空操作），不是"发了就走"
- ❌ 以为 `fh_poi(action=start)` 的 `direction`（clockwise/counter_clockwise）能控制绕飞方向 → **SDK 的 `POIEnterParams` 没有方向字段**，该参数只做合法性校验与回显，响应固定带 `direction_applied=false`，实际绕飞方向由飞行器自行决定
- ❌ 以为 `fh_poi(action=start)` 的"起点距圆心过近"安全校验在真实环境必定生效 → 真实模式下获取无人机实时位置若超时（3秒），该校验会被**静默跳过**（只打 warn 日志），只有 mock 模式下该校验必定生效；真实环境可能绕过前置校验直接撞上设备侧错误码 325003
- ❌ 以为 `fh_osd_snapshot` 是纯只读、无副作用的查询 → mock 模式下 `correctFlightPhase()` 会**修改 session 状态**：如果当前阶段是 `TAKING_OFF` 且模拟速度低于阈值，会把 `FlightPhase` 静默改成 `HOVERING`，不是完全无副作用
- ❌ 以为 `fh_drc_disconnect` 在没有链路时会报错 → **不会**，该工具是幂等的：本地无该设备的 DRC 链路时返回 `success:true` + `already_disconnected:true`，但**照样**会打上"显式断开"标记。这一点很关键——链路可能已在 SDK 侧掉线（本地早已无链路），此时用户仍然需要能"断开"，否则后续飞行指令会静默重连，覆盖用户的断开意图
- ❌ 相信 `fh_control_release`/`fh_drc_disconnect` 方法体内的 Javadoc 注释（历史上曾写"安全等级 L2"） → 该类复制粘贴遗留的失真注释已修正；类级注释和实际 `getSecurityLevel()` 覆写一直都是 **L1**，调用约束以代码为准
- ❌ 以为 `fh_payload_acquire`/`fh_payload_release` 和 DRC 链路耦合 → 完全无关，`fh_payload_acquire` 只硬依赖飞控权已持有（与 `fh_drc_connect` 无关），而 `fh_payload_release` 没有任何交叉依赖，可以在释放飞控权/断开 DRC 之前或之后任意调用
- ❌ 以为 `fh_takeoff` 的 `dock_sn` 在所有模式下都强制校验 → schema 标了 `required`，但实际只在真实 SDK 分支里强制使用（获取机场坐标），mock 模式下 `dock_sn=null` 也能成功起飞，不能靠 mock 测试验证该参数的必填性
- ❌ 以为 `fh_flyto(action=progress)`/`fh_poi(action=status)` 在真实模式下能拿到和 mock 一样丰富的字段（如 `progress_percent`/`laps_completed`） → 真实模式目前只返回 session 状态 + 飞行阶段，更细的遥测集成代码注释里明确承认"尚未完成"，丰富字段只有 mock 模式才有

## Parameter Cheat Sheet

| 工具 | Security | Confirm | 关键参数 | 备注 |
|---|---|---|---|---|
| `fh_control_acquire` | L0（未覆写，走默认值） | 否 | `drone_sn` | 单会话单设备限制；跨 session 冲突检测 |
| `fh_control_release` | L1 | 否 | `drone_sn` | **硬依赖**：必须已 `fh_drc_disconnect` |
| `fh_drc_connect` | L0（未覆写） | 否 | `drone_sn` | **硬依赖**：必须已 `fh_control_acquire`；已连接会给出友好提示而非报错 |
| `fh_drc_disconnect` | L1 | 否 | `drone_sn` | **幂等**：当前无 DRC 链路时同样返回成功（`already_disconnected=true`），并照样打上"显式断开"标记，避免后续飞行指令静默重连 |
| `fh_takeoff` | L2 | 是 | `drone_sn`, `dock_sn`（schema required，仅真实模式强制）, `target_height`（2-10000，默认100）, `security_takeoff_height`（默认100）, `rth_altitude`（默认100）, `max_speed`（默认15）, `out_of_control_action`（return_home/hover/continue） | 低于 `target_height` 的 `rth_altitude`/`security_takeoff_height` 会被静默上调（封顶1500m）；获取机场坐标 10s 超时 |
| `fh_flyto` | L2（start/stop/update）；`progress` 只读无需confirm | start/stop/update 需要 | `action`（start/stop/update/progress）, `latitude`, `longitude`, `height`, `max_speed`（默认15） | `update` 要求已有 `fh_flyto(start)` 建立的活跃任务；缺坐标会触发 elicitation 表单 |
| `fh_return_home` | L2 | 是 | `action`（start默认/cancel） | 走管理通道，不需要 DRC；无重试包装 |
| `fh_poi` | L2（start）；`status` 只读 | start 需要 | `action`（start/stop/status）, `latitude`, `longitude`, `radius`（5-500m）, `speed`（1-15m/s，默认5）, `direction`（clockwise/counter_clockwise，**仅校验回显，SDK 无该字段**） | 起点距圆心过近会报设备侧 325003；该校验依赖实时定位，3s 超时会跳过；响应带 `direction_applied=false` |
| `fh_emergency_stop` | **L3（CRITICAL，全项目最高）** | 是 | `drone_sn`, `reason`（必填，审计用）, `mode`（hover默认/landing/force_landing/parachute/hover_hold）, `duration_ms`（仅 hover_hold，0-1800000，0=取消保活）, `parachute_force`（仅 parachute，布尔，默认 false）, `operator_token` | 独立二次校验，mock 不豁免；硬依赖控制权已持有；失败不重试（设计如此，需人工立即核实设备状态）；`hover_hold` 仅支持 Dock 4 搭载机型，超范围时长 SDK 会静默截断，故 MCP 层先校验 |
| `fh_payload_acquire` | L1 | 否 | `drone_sn`, `payload_index`（默认 `81-0-0`） | 硬依赖已持有飞控权；一个 session 一次只能持有一个负载 |
| `fh_payload_release` | L1 | 否 | `drone_sn`, `payload_index`（默认取当前持有的） | 与飞控释放/DRC断开无交叉依赖 |
| `fh_stick_move` | L2 | 是 | `drone_sn`, `direction`（forward/backward/left/right/up/down/rotate_cw/rotate_ccw，大小写不敏感）, `speed_percent`（10-100，默认30，超范围静默clamp）, `duration_ms`（100-5000，默认1000，超范围静默clamp） | **调用阻塞约 `duration_ms`**（真实+mock均如此）；结束时自动发送一次回中帧停止运动 |
| `fh_osd_snapshot` | L0（未覆写） | 否 | `drone_sn` | 无前置条件；真实模式 5 路遥测并发订阅，5s 超时，部分超时仍 `success:true`+`_warning`（非完全静默失败） |

> **正确的全链路示例顺序**（含硬依赖与自愈逻辑的综合示范）：
> ```
> fh_control_acquire(drone_sn)
>   → fh_drc_connect(drone_sn)                         【硬依赖上一步】
>     → fh_takeoff(confirm=true, dock_sn, target_height)
>       → fh_flyto / fh_poi / fh_stick_move(confirm=true)   【可重复，自愈控制权+DRC】
>       → fh_payload_acquire(drone_sn)                  【硬依赖已持有飞控权，与DRC无关】
>         → fh_payload_release(drone_sn)
>       → fh_osd_snapshot(drone_sn)                      【任意时刻可查】
>       → fh_return_home(confirm=true)                  【走管理通道，不需要DRC】
>       → fh_emergency_stop(confirm=true, reason, operator_token)  【L3，硬依赖控制权已持有】
>   → fh_drc_disconnect(drone_sn)                        【硬依赖DRC确实已连接】
> → fh_control_release(drone_sn)                         【硬依赖上一步已disconnect】
> ```
