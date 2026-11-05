# Firmware (`fh_firmware`) — Decision Tree

> 单一工具，11 个 action，分 3 级安全等级（L0 查询 / L1 可逆写操作 / L2 删除）。此前 SKILL.md 仅有 1 行内联提示（`check_status → upgrade`），只覆盖了"升级链路"的后半段，**完整的"发布链路"此前完全没有文档**。

## Quick Decision Matrix

| User has | Correct action(s) | Security |
|---|---|---|
| 要查设备当前固件状态/是否有可升级版本 | `check_status` | L0 |
| 要查固件库列表（含 `file_id`） | `list` | L0 |
| 要查升级进度 | `query_progress` | L0（注意用单数 `device_sn`） |
| 要查固件版本说明 | `release_note` | L0 |
| 要查兼容性 | `compatibility` | L0 |
| 要下发升级 | `upgrade` | L1，需 confirm |
| 要上传固件包 | `upload` | L1，需 confirm |
| 要提交固件审核/入库 | `submit` | L1，需 confirm（真实模式不返回 `file_id`） |
| 要正式发布固件 | `release` | L1，需 confirm |
| 要撤回已发布固件 | `unrelease` | L1，需 confirm |
| 要删除固件 | `delete` | **L2**，需 confirm，比其余写操作等级更高 |

## Decision Flow

```
用户意图：固件发布 / 固件升级
         │
         ├─ 发布链路（集成商自己上传固件到库里）？
         │    upload(confirm=true)
         │       → submit(confirm=true)   ⚠️ 真实模式不返回 file_id
         │          → list()               ← 额外查一次，拿到刚提交的 file_id
         │             → release(file_id, confirm=true)
         │                （可选）unrelease(file_id, confirm=true)   撤回已发布
         │                （可选）delete(file_id, confirm=true)       L2，清理固件库
         │
         └─ 升级链路（给设备下发已发布的固件）？
              check_status(device_sns=[...])
                 ├─（可选）compatibility(...)   查新固件是否兼容目标设备
                 ├─（可选）release_note(...)    查版本说明给用户看
                 └─ upgrade(device_sns=[...], confirm=true)
                    → query_progress(device_sn=...)   ⚠️ 单数！轮询升级进度
```

## Common Mistakes

- ❌ 以为 SKILL.md 原来那句"`check_status → upgrade`"就是完整流程 → 那只是升级链路的核心两步，省略了 `compatibility`/`release_note` 可选步骤，且**完全没提发布链路**（upload/submit/list/release）；如果用户是"要把自己编译的固件包发布出去"而不是"给设备升级"，必须走发布链路，不要直接套用升级链路
- ❌ `submit` 调用成功后，直接拿返回值里找 `file_id` 传给 `release` → **真实 SDK 模式下 `submit` 的回调是 void-only，不返回 `file_id`**（只有 mock 模式才有这个字段）；真实环境下 `submit` 成功后必须额外调一次 `list` 查询固件库，从返回列表里找到刚提交的记录拿 `file_id`，再传给 `release`
- ❌ `query_progress` 和其它 action 一样传 `device_sns`（复数数组） → **只有 `query_progress` 用的是单数 `device_sn`**（字符串，一次查一台），其余所有涉及设备的 action（`check_status`/`compatibility`/`upgrade` 等）用的是复数 `device_sns`（数组），传反了会导致参数校验失败
- ❌ 读 `list` 的真实模式返回值时，期待 `firmwares` 字段是结构化数组（像 mock 一样） → 真实模式下该字段**是 SDK 返回值 `.toString()` 之后的裸字符串**（代码标记为 `@Generated`，属于尚未完善的占位实现），不能当数组解析，只能展示原文或等待后续版本修复
- ❌ 把 `delete` 和其它写操作（`upgrade`/`upload`/`submit`/`release`/`unrelease`）视为同一安全等级，以为传 `confirm=true` 就和其它一样处理 → `delete` 是全工具里唯一映射到 **`SecurityLevel.FLIGHT`（L2）** 的 action，其余写操作都是 `REVERSIBLE`（L1）；如果运行环境设置了 `FH_MAX_SECURITY_LEVEL=1`，`delete` 会被 `enforceSecurityLevel` 硬性拒绝，哪怕传了 `confirm=true` 也不行——这不是 bug，是等级设计使然
- ❌ 看到 `upload` 的 `file_path` 报错就以为是普通的"文件不存在" → `upload` 的 `file_path` 会用 `resolveWithinAllowedDir()` 做符号链接安全解析，限制在 `FH_FIRMWARE_UPLOAD_DIR`（默认 `/app/firmware`）目录内，路径越界会被拒绝，报错信息和"文件确实不存在"不是同一类问题，需要先确认文件是否放在允许的目录下

## Parameter Cheat Sheet

| Action | Security | Key parameters | Note |
|---|---|---|---|
| `check_status` | L0 | `device_sns` | 查当前固件版本和是否有可用升级 |
| `list` | L0 | 无必填 | 查固件库列表；真实模式 `firmwares` 字段是裸字符串，非数组 |
| `query_progress` | L0 | `device_sn`（**单数**） | 轮询升级进度；真实模式字段名 `progress_items`，mock 是 `progress` |
| `release_note` | L0 | `firmware_version` 或 `file_id` | 查版本说明文案 |
| `compatibility` | L0 | `device_sns`, `file_id` | 查固件与设备型号兼容性 |
| `upgrade` | L1 | `device_sns`, `file_id`, `confirm=true` | 下发升级指令 |
| `upload` | L1 | `file_path`（限 `FH_FIRMWARE_UPLOAD_DIR` 内）, `confirm=true` | 真实模式无 `object_key` 字段（mock 有） |
| `submit` | L1 | `object_key` 或上传返回的标识, `confirm=true` | ⚠️ 真实模式不返回 `file_id`，需额外 `list` 查 |
| `release` | L1 | `file_id`, `confirm=true` | 正式发布，设备侧 `check_status` 才能查到 |
| `unrelease` | L1 | `file_id`, `confirm=true` | 撤回已发布版本 |
| `delete` | **L2** | `file_id`, `confirm=true` | 等级高于其余写操作，受 `FH_MAX_SECURITY_LEVEL` 更严格约束 |

> 固件状态码：`1=none`（无固件）, `2=available`（有可用升级）, `3=consistency_upgrade`（一致性升级）, `4=upgrading`（升级中）。
