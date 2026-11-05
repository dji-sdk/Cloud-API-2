# Safety Service (Flight Area & Offline Map) — Decision Tree

## Quick Decision Matrix

| User has | Correct tool(s) | Key parameter |
|---|---|---|
| 要新建禁飞区/限飞区/禁降区 | `fh_flight_area_manage(action=create)` | `type` = nfz/dfence/noland, `confirm=true` |
| 要改已有飞行区的名称/几何 | `fh_flight_area_manage(action=update)` | `flight_area_id`, `confirm=true` |
| 要删除飞行区 | `fh_flight_area_manage(action=delete)` | `flight_area_id`, `confirm=true`（删除后不可恢复） |
| 要查看已有飞行区列表 | `fh_flight_area_list` | 无需 `action`、无需 `confirm`（只读） |
| 要把飞行区同步到设备 | `fh_flight_area_sync(action=sync)` | `device_sns`（1-20 台），`confirm=true` |
| 要查同步进度 | `fh_flight_area_sync(action=status)` | `device_sns`，无需 `confirm` |
| 要查/改离线地图模型、开关、信号地图 | `fh_offline_map` 对应 action | 见下方 Cheat Sheet |
| 要"升级飞安数据库" | ⚠️ 不要用 `fh_offline_map(invoke_flysafe_upgrade)` | 改走 `fh_firmware`，见 Common Mistakes |

## Decision Flow

```
用户意图：禁飞区/限飞区/离线地图/飞行区同步
         │
         ├─ 创建/更新/删除飞行区？
         │    → fh_flight_area_manage(action=create|update|delete, confirm=true)
         │    create 需 name+type+content；update 需 flight_area_id + (name 或 content)；delete 只需 flight_area_id
         │
         ├─ 查看已有飞行区？
         │    → fh_flight_area_list(name=?, type=nfz|dfence|noland|all, ...)
         │    （只读，无 action 参数，无需 confirm）
         │
         ├─ 把自定义飞行区推送到设备？
         │    ├─ 执行同步 → fh_flight_area_sync(action=sync, device_sns=[...], confirm=true)
         │    └─ 查进度   → fh_flight_area_sync(action=status, device_sns=[...])
         │
         └─ 离线地图相关？
              ├─ 查模型信息      → fh_offline_map(action=get_model_info)
              ├─ 更新模型信息    → fh_offline_map(action=update_model_info)
              ├─ 取消更新        → fh_offline_map(action=cancel, confirm=true)
              ├─ 启用/禁用       → fh_offline_map(action=set_enabled, enabled=true|false)
              ├─ 同步到设备      → fh_offline_map(action=sync, device_sns=[...])
              ├─ 查同步状态      → fh_offline_map(action=get_sync_status, device_sns=[...])
              ├─ 查信号地图      → fh_offline_map(action=search_signal_maps, device_sns=[...])
              └─ "升级飞安数据库" → 不要调 invoke_flysafe_upgrade，改用 fh_firmware（见下）
```

## Common Mistakes

- ❌ 调用 `fh_flight_area_manage(create/update/delete)` 不带 `confirm=true` → 三个 action 都需要人工确认，拦截后需补 `confirm=true` 重试
- ❌ 把 `fh_flight_area_list` 当成带 `action` 参数的工具调用 → 该工具**没有 `action` 参数**，是单一查询操作
- ❌ `fh_flight_area_sync(action=status)` 加了 `confirm` 还被拒绝困惑 → `status` 是只读查询，根本不需要 `confirm`；只有 `sync` 需要
- ❌ **调用 `fh_offline_map(action=invoke_flysafe_upgrade)` 并把 Mock 模式下的成功响应当真** → `invokeFlysafeDatabaseUpgrade` 已在 SDK 2.0 中被移除，真实环境该 action 必定返回错误并提示改走 `fh_firmware`；但 **Mock 模式会返回假成功**（`status: upgrade_triggered`），如果只在 Mock 环境测试会误以为功能可用，导致上线后把"升级成功"错误地告知用户。正确做法：涉及飞安数据库升级，统一引导到 `fh_firmware(action=check_status)` → `fh_firmware(action=upgrade)` 链路
- ❌ `fh_offline_map(action=cancel)` 不带 `confirm=true` → `cancel` 和 `invoke_flysafe_upgrade` 都是 L1 破坏性操作，均需确认
- ❌ `type` 参数拼错大小写或用了无效枚举 → 飞行区 `type` 仅接受 `nfz`（禁飞区）/`dfence`（电子围栏）/`noland`（禁降区）；`fh_flight_area_list` 的 `type` 额外接受 `all` 表示不过滤

## Parameter Cheat Sheet

### fh_flight_area_manage (action=create/update/delete)

| Parameter | Value | Note |
|---|---|---|
| `action` | create / update / delete | 必填 |
| `flight_area_id` | 飞行区 ID | update/delete 必填，create 不传 |
| `name` | 飞行区名称 | create 必填；update 可选（与 content 二选一必填其一） |
| `type` | nfz / dfence / noland | create 必填：禁飞区/电子围栏/禁降区 |
| `content` | GeoJSON Feature | create 必填；update 可选（与 name 二选一必填其一） |
| `created_by` | 创建者标识 | 可选 |
| `confirm` | true | create/update/delete 均必填 |
| `dry_run` | true | 可选，预览而不执行，无需 confirm |

**`content` GeoJSON 格式**：

```json
{
  "type": "Feature",
  "geometry": { ... },
  "properties": { "color": "...", "clamp_to_ground": true, "width": 20 }
}
```

- Circle（圆形）：`geometry = {"type": "Circle", "center": [lng, lat], "radius": <米>}`
- Polygon（多边形，nfz/dfence 常用）：`geometry = {"type": "Polygon", "coordinates": [[[lng,lat], ..., 首点闭合]]}`，闭合环 ≥3 点且 <255 点
- Polyline（折线，仅 `noland` 可用）：`geometry = {"type": "Polyline", "coordinates": [[[lng,lat], ...]]}`，必须带 `properties.width`（10~100）
- `properties.clampToGround` 和 `clamp_to_ground` 两种写法都接受

### fh_flight_area_list

| Parameter | Value | Note |
|---|---|---|
| `name` | 名称模糊过滤 | 可选 |
| `type` | nfz / dfence / noland / all | 可选，`all` 等价不过滤 |
| `status` | 状态过滤 | 可选 |
| `ids` | 飞行区 ID 数组 | 可选 |
| `page_index` | 默认 1 | 可选 |
| `page_size` | 默认 50，最大 200（超出服务端截断） | 可选 |

> 无 `action` 参数，无需 `confirm`——纯只读查询。

### fh_flight_area_sync (action=sync/status)

| Parameter | Value | Note |
|---|---|---|
| `action` | sync / status | 必填 |
| `device_sns` | 1~20 台设备 SN 数组（自动去重） | 必填 |
| `confirm` | true | **仅 sync 需要**，status 不需要 |
| `dry_run` | true | 可选，仅对 sync 有意义 |

### fh_offline_map

| Parameter | Value | Note |
|---|---|---|
| `action` | get_model_info / update_model_info / cancel / set_enabled / sync / get_sync_status / search_signal_maps / invoke_flysafe_upgrade | 必填 |
| `device_sns` | 设备 SN 数组 | sync/get_sync_status/search_signal_maps/invoke_flysafe_upgrade 必填 |
| `enabled` | true / false | set_enabled 必填 |
| `data_types` | 如 `["4g_lte", "5g_nr"]` | search_signal_maps 可选，默认两者都查 |
| `request_params` | `{model_3d_items?: [...], model_2d_items?: [...]}`，元素为 `{object_key, file_type, uuid?}` | **update_model_info 必填**，且两个数组中至少一个非空。SDK 会把两个数组都下发给服务端，未传的数组等同 `[]`（即"该维度没有模型"），两者都为空时由 MCP 在调用前拦截并提示 225101 |
| `confirm` | true | **仅 cancel / invoke_flysafe_upgrade 需要**（L1 破坏性操作） |

> ⚠️ **`invoke_flysafe_upgrade` 在真实环境不可用**：SDK 2.0 已移除 `invokeFlysafeDatabaseUpgrade`，调用会返回错误并提示改用 `fh_firmware`。Mock 模式会返回假成功（`_mock: true`），仅用于前端联调展示，不代表功能真实可用。

## 空域合规 — fh_flysafe / fh_tcas / fh_unlock_license

> 覆盖 `airspace/` 包下 3 个**独立**工具，均为 Legacy 模式。`fh_flysafe` **不是** `fh_offline_map` 的 action，是完全独立的 MCP 工具——虽然两者都涉及"飞安数据库升级"这同一个已被 SDK 2.0 移除的底层能力，但入口、参数、mock/real 分裂细节各不相同，详见下方交叉引用。

**何时用**：用户要查/触发飞安数据库升级状态、查/改设备防撞（TCAS）参数、或管理空域解禁证书（申请/上传/切换/校验/清除）。
**何时不用**：用户想管自定义禁飞区/限飞区 → 用本文档开头的 `fh_flight_area_*`；用户想做真正的固件/数据库升级 → 用 `fh_firmware`。

| 动作 | 工具 | Security | Confirm |
|---|---|---|---|
| 查询飞安数据库版本 / 触发升级 | `fh_flysafe(action=check\|upgrade)` | check=L0，upgrade=L1 | upgrade 需要 |
| 查询/更新设备 TCAS 防撞参数 | `fh_tcas(action=get\|update)` | L0 / L1（但 update 缺硬性安全门禁，见 Common Mistakes） | update 需要 |
| 空域解禁证书：列表/提交/上传/切换/校验/清除 | `fh_unlock_license(action=...)` | list=L0，submit/upload/switch/check=L1，clear=**L2** | 除 list 外均需要 |

### Decision Flow

```
用户意图：空域合规
         │
         ├─ 飞安数据库升级？
         │    → fh_flysafe(action=check, device_sn=... 或 device_sns=[...])
         │    → fh_flysafe(action=upgrade, device_sns=[...], confirm=true)
         │    ⚠️ check / upgrade 在真实环境**均无条件返回错误**（见 Common Mistakes）
         │    ⚠️ 与 fh_offline_map(action=invoke_flysafe_upgrade) 是两个不同入口，不要混淆
         │
         ├─ 防撞（TCAS）参数？
         │    → fh_tcas(action=get, center_sns=[...])     查询
         │    → fh_tcas(action=update, center_sns=[...], confirm=true, ...)   更新
         │    （真实 SDK 两个 action 均已正常实现，无不可用陷阱）
         │
         └─ 空域解禁证书？
              → fh_unlock_license(action=list)                                只读
              → fh_unlock_license(action=submit/upload/switch/check, confirm=true)   L1
              → fh_unlock_license(action=clear, confirm=true)                 L2，⚠️ 不可逆
```

### Common Mistakes

- ❌ 以为 `fh_flysafe` 是 `fh_offline_map` 的一个 action → **不是**，`fh_flysafe` 是独立注册的 MCP 工具（`airspace/FhFlysafeTool.java`），和 `fh_offline_map(action=invoke_flysafe_upgrade)` 是两个完全不同的入口；两者底层对应的 SDK 能力都已在 SDK 2.0 中被移除，这是**两个独立的发现**，不要当成同一处陷阱的重复描述
- ❌ 以为 `fh_flysafe(action=check)` 这种"只读查询"在真实环境下总能拿到真实数据 → **不行**，`check`/`upgrade` 两个 action 在真实 SDK 模式下都是 `@Generated` 标注的桩方法（`checkApiRemoved()`/`upgradeApiRemoved()`），**无条件返回错误**，日志/错误信息明确写"已在 SDK 2.0 中移除"，并引导改用 `fh_firmware`；这是代码里刻意设计成"宁可报错也不假装成功"的路径
- ❌ 认为 `fh_flysafe` 的 mock 模式也会诚实地报错 → **不会**，`mockCheck()`/`mockUpgrade()` 都会伪装返回成功数据（`upgrade` 的 mock 返回里甚至带 `upgrade_triggered` 状态），和真实模式的"报错"行为完全相反；只在 mock 下测试通过，不能说明真实环境可用——这一点和 `fh_offline_map(invoke_flysafe_upgrade)` 的"mock 假成功"陷阱性质相同，但这是两个独立工具各自的 mock 实现，要分别提醒用户，不要以为提醒过一次就覆盖了两处
- ❌ 以为 `fh_tcas(action=update)` 和 `fh_flysafe(action=upgrade)`/`fh_unlock_license` 的写操作一样有硬性安全等级拦截 → **不完全一样**：`fh_tcas` 的 `update` 分支代码里**没有调用** `enforceSecurityLevel()`，只调用了 `requireHumanConfirm()` 做软性人工确认；而 `fh_flysafe(upgrade)`/`fh_unlock_license` 的写 action 都同时有 `enforceSecurityLevel()` 硬门禁 + `requireHumanConfirm()` 软确认两层。这是已知的工具间不一致（不是本次要"修复"的目标），实际影响是：`fh_tcas(update)` 在 `FH_MAX_SECURITY_LEVEL` 设置较低时，仍然可能绕过硬性等级拦截、仅靠人工确认放行——生成调用前仍应正常带上 `confirm=true`，不要依赖等级拦截来兜底
- ❌ 以为 `fh_tcas(action=get)` 不需要 `center_sns` 参数（以为只有 `update` 才需要） → **两个 action 的 schema 都把 `center_sns` 列为必填**，查询也必须传设备 SN 数组，不是只有写操作才要
- ❌ 以为 `fh_unlock_license(action=clear)` 和其它写 action 一样是 L1 → **不是**，`clear` 单独映射到 **L2（FLIGHT）**，比 `submit`/`upload`/`switch`/`check` 的 L1 更高一级，确认提示信息里带 ⚠️ emoji 强调不可逆，生成调用前确认用户确实理解这是清除操作
- ❌ 给 `fh_unlock_license` 的 `device_model_domain`/`license_id` 传非数字字符串 → 这两个参数内部用 `Integer.parseInt()` 解析，传入非纯数字字符串会被捕获 `NumberFormatException` 并返回参数错误，不要传带单位或前缀的字符串（如 `"domain-1"`）

### Parameter Cheat Sheet

| 工具 | Action | Security | Confirm | 关键参数 | 备注 |
|---|---|---|---|---|---|
| `fh_flysafe` | `check` | L0 | 否 | `device_sn` 或 `device_sns`（数组优先，单个 SN 会被自动包装成单元素数组） | 真实模式无条件报错，mock 假成功 |
| `fh_flysafe` | `upgrade` | L1（REVERSIBLE） | 是 | 同上 + `confirm` | 同上；与 `fh_offline_map(invoke_flysafe_upgrade)` 是不同入口，见上方交叉引用 |
| `fh_tcas` | `get` | L0 | 否 | `center_sns`（数组，必填） | 真实 SDK 已实现，无不可用陷阱 |
| `fh_tcas` | `update` | L1（REVERSIBLE，**但无 `enforceSecurityLevel()` 硬门禁，仅软确认**） | 是 | `center_sns`，`tcas_enabled`/`warning_distance_m`/`avoidance_distance_m`/`max_speed_limit_mps` 等 | mock 字段：`warning_distance_m=50`/`avoidance_distance_m=20`/`max_speed_limit_mps=10.0` |
| `fh_unlock_license` | `list` | L0 | 否 | — | 查已有证书列表 |
| `fh_unlock_license` | `submit`/`upload`/`switch`/`check` | L1（REVERSIBLE） | 是 | `device_model_domain`/`license_id`（数字字符串，`Integer.parseInt` 解析） | 支持 `dry_run` 预检 |
| `fh_unlock_license` | `clear` | **L2（FLIGHT）** | 是 | 同上 | 不可逆，confirm 提示带 ⚠️ emoji |
