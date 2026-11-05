# Device Debug (`fh_device_debug`) — Decision Tree

> 单一工具，但有 **31 个 action**（不是类注释写的 27，已逐项核对 `CONFIRM_REQUIRED_ACTIONS` Set），是全项目风险最高、最容易被误用的工具。Legacy 模式实现，类级 `getSecurityLevel()` 未覆写（始终显示 L0），实际确认门槛由代码里的白名单决定，不要看类注释。

## Quick Decision Matrix

| User has | Correct action(s) | Confirm? |
|---|---|---|
| 要进入/退出调试模式 | `open_debug` / `close_debug` | 需要 |
| 要开关机场舱盖（调试通道，非 `fh_dock_control`） | `open_cover` / `close_cover` | 需要 |
| 要开关充电（调试通道） | `open_charge` / `close_charge` | 需要 |
| 要开关无人机电源 | `open_drone` / `close_drone` | 需要 |
| 要格式化设备/无人机存储（**不可逆**） | `format_device` / `format_drone` | 需要 |
| 要重启机场 | `reboot_device` | 需要 |
| 要做网络诊断 | `network_test_start` / `network_test_stop` | 需要 |
| 要做 RTK 校准 | `rtk_calibrate_manual` / `rtk_calibrate_network` | 需要 |
| 要切 SIM 卡槽 / eSIM 运营商 | `sim_slot_switch` / `esim_operator_switch` | 需要 |
| 要重启/断开中继（⚠️ 真实环境必失败，见下） | `relay_reboot` / `relay_disconnect` | 需要 |
| 要配置中继 SSID/密码/搜索模式（⚠️ 同上） | `set_relay_ssid` / `set_relay_password` / `set_relay_search_mode` | 不需要 |
| 要设限高/限远/避障/夜航灯/电池存储/剩余电量返航等参数 | 对应 `set_*` action（见下方 L0 分组） | 不需要 |
| 要设空调/静音模式/告警灯 | `set_air_conditioner` / `set_silent_mode` / `switch_alarm_light` | 不需要 |

## Decision Flow

```
用户意图：设备调试 / 高危运维操作
         │
         ├─ 高危破坏性操作？（19 个需 confirm，见 CONFIRM_REQUIRED_ACTIONS 白名单）
         │    调试模式：   open_debug / close_debug
         │    舱盖/充电：  open_cover / close_cover / open_charge / close_charge
         │    开关机：     open_drone / close_drone
         │    格式化：     format_device / format_drone        ⚠️ 不可逆，无额外二次确认文案
         │    重启：       reboot_device
         │    网络诊断：   network_test_start / network_test_stop
         │    RTK校准：    rtk_calibrate_manual / rtk_calibrate_network
         │    SIM切换：    sim_slot_switch / esim_operator_switch
         │    中继：       relay_reboot / relay_disconnect       ⚠️ 真实 SDK 下必失败，mock 下必成功
         │    → 全部需要 confirm=true，否则被 enforceSecurityLevel(REVERSIBLE) + requireHumanConfirm 拦截
         │
         └─ 普通参数配置？（12 个 L0，不需要 confirm）
              飞行参数：  set_height_limit / set_distance_limit / set_obstacle_avoidance
              环境/夜航： set_night_lights / set_air_conditioner / set_silent_mode / switch_alarm_light
              电池：      set_battery_store_mode / set_remaining_power_rth
              中继配置：  set_relay_ssid / set_relay_password / set_relay_search_mode  ⚠️ 配置本身L0，但下游relay_reboot等生效受限
```

## Common Mistakes

- ❌ 以为 `fh_device_debug` 一共 27 个 action（类 Javadoc 这么写的） → 实测 `CONFIRM_REQUIRED_ACTIONS` + 其余 L0 action 加总共 **31 个**，不要相信类注释里的数字，调用前以本文档或源码 `Set.of(...)` 为准
- ❌ **只在 Mock 模式下测试过 `relay_reboot`/`relay_disconnect`/`set_relay_ssid`/`set_relay_password`/`set_relay_search_mode` 就认为中继功能可用** → 这是本工具头号陷阱：`javap` 核实 `flight-hub-sdk-3.3.7.jar` 后确认 `Dock.class` **根本没有暴露** `getRelayControlModule()`（SDK v2.0.0 未实现），真实环境调用这 5 个 action 必定走硬编码的 `errorResult`，**100% 失败**；但 Mock 模式下这 5 个 action **100% 返回成功**。只用 Mock 验证过的"中继功能正常"结论是错的，上线会全部报错
- ❌ 把 `open_cover`/`close_cover`/`open_charge`/`close_charge` 当作和 `fh_dock_control` 的 `cover_open`/`cover_close`/`charge_start`/`charge_stop` 是同一个功能的两种写法 → 两者走**不同 SDK 模块**（`DeviceDebugModule` vs `DockControlModule`），行为、前置条件、返回结构都不保证一致，不能互相替代或混用判断逻辑；如果用户没有明确要求"调试通道"，优先用 `fh_dock_control`（见 device-query-control.md）
- ❌ SDK 未初始化、连接失败时，看到 `format_device`/`reboot_device` 等返回"成功"就当作设备真的被格式化/重启了 → mock 分支的触发条件是 `isMockMode() || !isInitialized()`，**连接失败也会静默落到 mock 分支返回假成功**，这意味着一次连接异常可能被误判为"操作已执行"，对不可逆操作尤其危险，调用后应结合 `fh_device_manage(detail)` 或遥测快照二次确认设备真实状态
- ❌ 调用 `format_device`/`format_drone` 期待会有二次确认文案或分级提示 → 代码里这两个 action 和其他 18 个一样只是落在 `CONFIRM_REQUIRED_ACTIONS` 白名单里统一要求 `confirm=true`，**没有任何额外的"再次确认"机制**，等级上和"开关舱盖"是同一档（`SecurityLevel.REVERSIBLE`/L1），而不是框架里为真正不可逆操作保留的 L3（`CRITICAL`）；调用前必须由上层业务逻辑（而非本工具）做好二次核实
- ❌ `height_limit` 传 `0` 当作"不设限"/"保持默认" → `height_limit` 的校验是 `>0`，`0` 或负数会被当成"缺失/非法"而不是生效值；而 `distance_limit_state`、`obstacle_avoidance_state` 等其它状态类参数的校验是 `>=0`，`0` 是合法值（哨兵值是 `-1` 表示未设置）——两类参数的"合法边界"不一样，别套用同一套判断
- ❌ `sim_slot_switch`/`esim_operator_switch` 不传 `imei` → 这两个 action 的 `imei`、`sim_slot`/`operator_type`、`device_type` 均为必填且无枚举约束校验，传错值不会被 schema 拦住，只会在 SDK 调用层失败

## Parameter Cheat Sheet

### 需要 confirm=true 的 19 个 action（`CONFIRM_REQUIRED_ACTIONS`）

| 分组 | Action | 额外参数 |
|---|---|---|
| 调试模式 | `open_debug`, `close_debug` | `device_sn` |
| 舱盖（调试通道） | `open_cover`, `close_cover` | `device_sn` |
| 充电（调试通道） | `open_charge`, `close_charge` | `device_sn` |
| 开关机 | `open_drone`, `close_drone` | `device_sn` |
| 格式化 | `format_device`, `format_drone` | `device_sn` ⚠️ 不可逆 |
| 重启 | `reboot_device` | `device_sn` |
| 网络诊断 | `network_test_start`, `network_test_stop` | `device_sn` |
| RTK校准 | `rtk_calibrate_manual` | `device_sn` |
| RTK校准（网络） | `rtk_calibrate_network` | `device_sn`, `rtk_host`, `rtk_port`, `rtk_account`, `rtk_password`（明文）, `mount_point`, `rtk_version`（可选，默认 1） |
| SIM/eSIM | `sim_slot_switch` | `device_sn`, `imei`, `sim_slot` |
| SIM/eSIM | `esim_operator_switch` | `device_sn`, `imei`, `operator_type`, `device_type` |
| 中继 | `relay_reboot`, `relay_disconnect` | `device_sn` ⚠️ 真实 SDK 必失败，仅 mock 可用 |

> 所有需 confirm 的 action 均走 `enforceSecurityLevel(REVERSIBLE)` + `requireHumanConfirm(...)`，统一传 `confirm=true` 即可，无分级文案。

### 不需要 confirm 的 12 个 L0 action

| 分组 | Action | 关键参数 | 校验边界 |
|---|---|---|---|
| 飞行参数 | `set_height_limit` | `height_limit` | **必须 `>0`**，0/负数视为缺失 |
| 飞行参数 | `set_distance_limit` | `distance_limit_state`, `distance_limit` | `state` 用 `>=0`，`-1`=未设置 |
| 飞行参数 | `set_obstacle_avoidance` | `obstacle_avoidance_state` | `>=0`，`-1`=未设置 |
| 环境 | `set_night_lights` | `night_lights_state` | `>=0` |
| 环境 | `set_air_conditioner` | `air_conditioner_state` | `>=0` |
| 环境 | `set_silent_mode` | `silent_mode_state` | `>=0` |
| 环境 | `switch_alarm_light` | `alarm_light_state` | `>=0` |
| 电池 | `set_battery_store_mode` | `battery_store_mode` | `>=0` |
| 电池 | `set_remaining_power_rth` | `remaining_power_rth` | `>=0` |
| 中继配置 | `set_relay_ssid` | `relay_ssid` | 字符串，无枚举约束 |
| 中继配置 | `set_relay_password` | `relay_password` | 字符串，无枚举约束 |
| 中继配置 | `set_relay_search_mode` | `relay_search_mode` | `>=0`，无枚举约束 |

> 这 3 个中继配置 action 本身是 L0 可以成功写入，但其效果依赖的 `relay_reboot`/`relay_disconnect` 在真实环境不可用，配置生效与否需要其它方式验证。
