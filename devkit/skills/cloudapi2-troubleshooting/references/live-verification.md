# 实机验证（MCP + 模拟器）

用 devkit MCP Server 的 `fh_*` 工具对 DJI CloudAPI2 平台做真实调用，把「读代码得出的假设」变成「实测证实/证伪的结论」。

对应 RCA **Stage 2.5**，位于 Locate 之后、Fix 之前。前置条件：MCP Server 已部署并连通（部署见 `cloudapi2-mcp-deploy`，工具选型见 `cloudapi2-mcp-guide`）。

## 何时用

**该用**：

- 代码逻辑看起来正确，但实际行为与之不符 —— 需要实测确认到底哪一层出问题
- 报错信息模糊（`INVALID_STATE`、`SDK error`、无错误码），代码里有多个可能分支
- 怀疑是设备状态/时序问题，而非代码问题
- 需要确认某个 SDK 能力在当前平台版本上的**实际**行为

**不该用**：

- 纯静态问题（编译错误、类型错误、明显的空指针）—— 直接进 Stage 3 Fix
- 错误码在 `error-codes.yaml` 中已直接命中（L1）—— 已有结论，不需要实测
- **生产环境真机** —— 有动作的调用只在模拟器 / 测试环境执行

## 安全边界（不可协商）

| 约束 | 说明 |
|---|---|
| L0 只读随时可做 | 快照/查询类工具无副作用，优先跑满 |
| L1+ 仅限模拟器/测试环境 | 有实际动作（起飞、航线、相机、云台）的调用，真机需用户**显式确认** |
| `dry_run` 先行 | 所有 `confirm=true` 的调用，先用 `dry_run=true` 验证前置条件 |
| 权限必须释放 | 控制权 / DRC / payload 在 `finally` 语义下释放 —— **即使中途报错** |
| `fh_emergency_stop` 不用于验证 | 仅真实紧急情况使用 |

MCP Server 的 `FH_MAX_SECURITY_LEVEL` 环境变量可设全局上限；排障场景建议设为 `0`（只读），确需动作时再临时放开。

## 前置链（必须按序）

```
fh_connect                    # 无参数，配置在服务端；不要问用户要地址/凭证
  ↓
fh_device_list                # 拿 SN；airport 是 gateway，drone 是它的子设备
  ↓
fh_control_acquire(drone_sn)  # 飞控权限，全局互斥
  ↓
fh_drc_connect(drone_sn)      # DRC 低延迟链路
  ↓
  ...业务调用...
  ↓
fh_drc_disconnect → fh_control_release    # 失败路径也必须走
```

相机 / 云台操作额外需要 `fh_payload_acquire` → `fh_payload_release`（在 `fh_control_acquire` 之后）。

前置链断在哪一步，本身就是排障信息：`fh_control_acquire` 失败 → 控制权被别的客户端占用；`fh_drc_connect` 失败 → 链路层问题，不必再往业务层找。

## 已知陷阱（先看这里，再怀疑自己的代码）

| 现象 | 真相 |
|---|---|
| `fh_device_list` 显示 drone offline | **正常**。机场内的无人机处于断电存储状态，只需 airport online 即可起飞。**绝不能拿 `drone.online` 当起飞前置校验** |
| 起飞后掉到 100m，达不到目标高度 | `rth_altitude` 默认 100m。`target_height > 100` 时必须同时把 `rth_altitude` 设 ≥ `target_height` |
| `camera_photo` / `camera_video_start` 返回 `INVALID_STATE` | 航线任务执行中固件锁相机。先 `fh_wayline_status` 确认无任务在跑 |
| `camera_video_start` 直接失败 | 必须先 `fh_camera(switch_mode, camera_mode=VIDEO)`。拍照不用，`camera_photo` 会自动切 |
| POI 环绕报 `325003` | 无人机在 POI 圆心正上方，radius=0 算不出轨迹。圆心要选**地面地标**（机场/建筑），不要用无人机当前坐标 |
| `fh_media_record` 录不到东西 | 它录的是**直播流**（服务端存储），必须先 `fh_live_start`。要录机载 SD 卡请用 `fh_camera(camera_video_start)` |
| 高度对不上 | `target_height` 是 **ALT**（相对起飞点）。OSD 里 `elevation`=ALT，`height`=ASL（椭球高），别混 |
| 本地 KMZ 建不了任务 | 有 URL 用 `fh_wayline_upload`（收 `drone_sn`）；有本地文件先 `fh_workspace(submit)` 拿 `wayline_uuid`，再 `fh_flight_task(create)`（收 **`dock_sn`**，不是 `drone_sn`） |

## 验证配方

### 只读快照（L0，永远安全，优先做）

```
fh_device_list                        # 设备拓扑 + 在线状态
fh_dock_snapshot(dock_sn)             # 机场坐标、舱盖、电量、存储
fh_osd_snapshot(drone_sn)             # 位置/姿态/电量/飞行模式/GPS/风速（比 fh_drone_snapshot 更全）
fh_wayline_status()                   # 是否有任务在跑（排查相机被锁的第一步）
fh_hms(query_history, device_sn)      # 健康告警历史，定位硬件/固件层异常
```

**大多数问题在这一步就能定性**，不要急着起飞。

### 起飞验证

```
fh_device_list                  # 找到 drone 所属的 airport SN（takeoff 需要 dock_sn 取 home point）
fh_control_acquire → fh_drc_connect
fh_takeoff(drone_sn, dock_sn, target_height, rth_altitude≥target_height, confirm=true)
fh_osd_snapshot                 # 确认实际达到高度
```

先用 `dry_run: true` 跑一遍校验前置条件，再真执行。

### 航线验证

```
fh_wayline_recommend(dock_sn)   # 按机型 + 距离筛可飞航线（比 fh_workspace(list) 准）
fh_flight_task(create, sn=dock_sn, wayline_uuid, task_type, rth_altitude)
fh_wayline_execute(task_uuid, confirm=true)
fh_wayline_status(task_uuid)    # 轮询进度
```

### 媒体验证

```
fh_media_list(device_sn, file_type, start_time, end_time)
fh_media_download(file_id)      # 用返回的 suggested_filename 下载，否则文件名会带重复后缀
```

## 记录要求

验证结束后，在 `diag.sh report` 产出的报告里补一节「实机验证」，写清三件事：

1. **调用了什么** —— 工具名 + 关键参数
2. **预期什么** —— 基于 Stage 2 定位推导的行为
3. **实际什么** —— 原始返回 / 遥测值

**不要只写结论不写实测数据。**

## 出口判定

| 结果 | 下一步 |
|---|---|
| 预期与实际**一致** | 假设证实 → 进 Stage 3 Fix |
| **不一致** | 假设证伪 → 回 Stage 2 Locate 重新定位，**不要强行解释** |
| 前置链就断了 | 断点即根因所在层，按断点层重走 Stage 2 |
