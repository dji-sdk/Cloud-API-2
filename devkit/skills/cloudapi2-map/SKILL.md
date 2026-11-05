---
name: cloudapi2-map
description: |
  DJI Cloud API 2 地图域 SDK 接入助手。当用户要编写 Java 代码管理自定义飞行区（限飞区/作业区/禁降区）、地形障碍物离线地图、
  Flysafe 解锁证书、信号地图，或管理地图元素（点/线/面/分组）时调用。
  触发词：SafetyServiceModule、ElementModule、飞行区、限飞区、作业区、禁降区、nfz、dfence、noland、
  createCustomizedFlightAreas、syncCustomizedFlightAreas、getFlightAreasSyncStatus、离线地图、
  offlineMaps、OfflineMapsModel、解锁证书、unlockLicense、unlock-license、uploadUnlockLicenseFile、
  信号地图、signalMaps、H3、地图元素、element、polygon、polyline、地图标绘、GeoJSON Feature；
  也包括排查"飞行区/离线地图改完或 sync 之后设备侧没生效"这类**同步语义**问题（改完必须 sync + 轮询 *SyncStatus，属设计两段式而非故障）。
  不触发：调用 MCP Server 的 fh_* 工具做运维操作（→ cloudapi2-mcp-guide）、通用 SDK 规则/设备管理/航线任务（→ cloudapi2-sdk-core）、
  设备生命周期与固件升级（→ cloudapi2-device-management）、SEI/OSD 遥测（→ cloudapi2-sei-osd）。
version: 1.0.0
author: DJI Cloud API 2 Team
---

# DJI Cloud API 2 地图域接入助手

## 这个 Skill 是干什么的

帮集成商开发者正确编写调用 **DJI Cloud API 2 Java SDK 地图域模块**的集成代码：`SafetyServiceModule` 与 `ElementModule`。

两者的共同点是都**挂在 Workspace 上**、都是**纯 HTTP 业务接口为主**（各带少量 WebSocket 订阅：SafetyService 1 个、Element 3 个），
与飞控/遥测（Drone/Dock 级、事件驱动）完全不同，容易在"该挂哪、要不要抢控制权、改完怎么生效"上写错。

**核心价值**：地图域最大的坑不是 API 签名，而是**状态同步语义** —— 飞行区和离线地图改的是云端定义，
必须再 `sync` 下发到机场才生效，且 sync 的返回**不代表同步完成**，还要轮询 `*SyncStatus`。

## 何时触发

- 用户要写代码管理自定义飞行区（`nfz` 限飞区 / `dfence` 作业区 / `noland` 禁降区）
- 用户要把飞行区/离线地图下发到机场或地面站（sync / 查同步状态）
- 用户要做地形障碍物离线地图的生成、取消、开关
- 用户要接入 Flysafe 解锁证书（上传 / 校验 / 提交 / 查询 / 开关 / 清除）
- 用户要查信号地图（4G / SDR 覆盖网格）
- 用户要管理地图元素（点/线/面/圆、分组层级、可见性）
- 用户遇到相关报错：同步失败、`reason` 码、几何校验不过、回调参数写错、模块取到 null

**不触发**：

- 用户要**运行时直接调用 MCP Tool**（`fh_flysafe` / `fh_unlock_license` / `fh_offline_map` 等做运维操作）→ `cloudapi2-mcp-guide`
- 通用 SDK 规则（初始化、鉴权头、回调顺序、listen/cancel、抢控制权）、飞控/航线任务/媒体 → `cloudapi2-sdk-core`
- 设备生命周期、固件升级、HMS 告警 → `cloudapi2-device-management`
- SEI/OSD 遥测、视频流与地图可视化融合 → `cloudapi2-sei-osd`

## 模块位置（先确认挂在哪）

```java
// ✅ 地图域模块在 Workspace 上
SafetyServiceModule safety = workspace.getSafetyServiceModule();
ElementModule        element = workspace.getElementModule();

// ❌ 不在 drone / dock 上
// drone.getSafetyServiceModule()   —— 不存在
// dock.getElementModule()          —— 不存在
```

两个 getter 都可能返回 `null`（设备/套餐不支持），调用前必须判空。

| 模块 | 方法数（源码核实） | 能力 | 详细文档 |
|---|---:|---|---|
| `SafetyServiceModule` | 22 | 信号地图、自定义飞行区、地形障碍物离线地图、Flysafe 解锁证书 | `references/safety-service.md` |
| `ElementModule` | 11 | 地图元素（点/线/面/圆）增删改查 + 可见性 + 分组层级 + 3 组 WS 订阅 | `references/element.md` |

## 核心规则

### 规则 1：回调顺序统一 `(error, result)`，但有一批方法只有 `(error)`

本域**没有** `getDeviceList` 那种 data-first 例外，但这不意味着所有回调都是两个参数：

| 形态 | 安全服务中的方法 |
|---|---|
| `onResult(error, result)` | 查询类 + 创建/更新/同步类 |
| `onResult(error)` **仅 error** | `deleteCustomizedFlightAreas`、`updateOfflineMapsModelInfo`、`cancelOfflineMapsModelInfo`、`setOfflineMaps`、`switchUnlockLicense`、`submitUnlockLicense`、`clearDeviceUnlockLicense` |
| `onResult(message)` **无 error** | `listenDroneOfflineMapEnable`（订阅推送） |

**不要按"查询/命令"推断参数个数**——命令类里 `createCustomizedFlightAreas` 有 result，`deleteCustomizedFlightAreas` 没有。
写错时编译器会报错，但用 lambda 时容易把 error 当 result 用，务必对照 `references/` 里的方法表。

### 规则 2：云端定义 ≠ 设备生效，`sync` 是必经步骤

```
创建/更新/删除飞行区  → 只改了云端
        ↓
syncCustomizedFlightAreas(网关 SN 列表)   → 提交下发
        ↓
getFlightAreasSyncStatus(网关 SN 列表)    → 轮询直到 synchronized / fail
```

- `syncXxx` 返回的 `code` 只是**提交结果**，不代表已同步完成
- 同步对象只能是**网关类型设备 SN**（机场 1/2/3、地面站），飞行器 SN 不会生效
- 离线地图同构：`syncOfflineMaps` → `getOfflineMapsSyncStatus`
- 失败时按 `DeviceSyncStatusResults.reason` 码给出可读原因（0–13 / 255，见 reference）

### 规则 3：离线地图生成是长任务

`updateOfflineMapsModelInfo` 触发的是**模型生成**，不是即时完成。必须轮询 `getOfflineMapsModelInfo`
直到 `percent == 100` 或 `status` 落终态，再考虑 `syncOfflineMaps`。中途可用 `cancelOfflineMapsModelInfo` 取消。

### 规则 4：解锁证书是三段式，不能跳步

```
uploadUnlockLicenseFile(本地路径)  → 得到 fileStoreDir，拼出 fileKey
        ↓
checkUnlockLicense(fileKey, sn)    → errFileNames / errCertMismatch 非空即中止
        ↓
submitUnlockLicense(sn, certUrl)   → 提交解锁（仅 error）
```

`check` 通过 ≠ `submit` 成功；`submit` 前必须确认 `check` 无错误项。

### 规则 5：几何数据格式严格

`ContentsGeometryData` 的约束写死在服务端校验里：

| 类型 | 必填 | 约束 |
|---|---|---|
| `Circle` | `center` + `radius` | `radius > 0`，单位米 |
| `Polygon` | `coordinates` | ≥3 点、顶点数 < 255、**首尾必须闭合** |
| `Polyline` | `coordinates` | **仅禁降区（`noland`）可用** |

坐标格式一律 `[经度, 纬度]`（经度在前）。飞行区数量上限：`nfz` ≤ 100，`noland` ≤ 50。

### 规则 6：订阅必须配对 cancel；几何模型两套互不通用

`listenDroneOfflineMapEnable` 返回 `int listenId`，`< 0` 表示失败；`@PreDestroy` 中必须调用
`cancelListenDroneOfflineMapEnable(listenId)`。`ElementModule` 的 `listenElementCreate/Update/Delete`
同样各返回一个 `listenId`，需分别保存并配对 `cancelListenElementXxx`。（其余方法均为一次性回调，不需要 cancel。）

> ⚠️ `SafetyServiceModule` 飞行区几何用 `ContentsGeometryData.type = Circle/Polygon/Polyline`；
> `ElementModule` 几何用 `ElementGeometry.type = Point/LineString/Polygon`，圆形复用 `Point + radius`，
> **没有 `Circle` 枚举值**。两套模型字段名相似但取值不同，不要跨模块抄代码。

## 决策树路由

```
用户问题
├─ 飞行区（限飞区/作业区/禁降区）创建/查询/更新/删除
│    → references/safety-service.md#2-自定义飞行区
├─ 飞行区同步到设备 / 同步失败 / reason 码
│    → references/safety-service.md#25-同步到设备
├─ 离线地图生成/开关/进度/cancel/同步
│    → references/safety-service.md#3-离线地图
├─ 解锁证书上传/校验/提交/查询/开关/清除
│    → references/safety-service.md#4-解锁证书飞行解禁
├─ 信号地图 / H3 网格
│    → references/safety-service.md#1-信号地图
├─ 地图元素（点/线/面/圆）增删改查 / 分组层级 / WS 订阅
│    → references/element.md
├─ 模块获取为 null / 不知道挂在哪
│    → 本文件「模块位置」
├─ 回调参数写错、编译报错
│    → 本文件「规则 1」
└─ 要调 MCP 工具做运维（不写代码）
     → 转交 cloudapi2-mcp-guide
```

## 工作流

本 Skill 遵循 `skills/_shared/sop-template.md` 定义的 7 章节 SOP，按 L1/L2/L3 自动选档。

### Ch2 澄清问题

地图域需求必须澄清的点（缺一项就可能写错）：

1. **目标是哪类对象**：自定义飞行区 / 离线地图 / 解锁证书 / 地图元素 / 信号地图
2. **是否要下发到设备**：只做云端管理（CRUD）还是需要机场/飞行器生效（必须 sync）
3. **目标的设备类型**：飞行器 SN 还是网关设备 SN（sync 只接受后者）
4. **几何类型**：圆形 / 多边形 / 折线（决定必填字段与校验规则）
5. **是否已有对象存储上传链路**：离线地图模型文件与解锁证书文件都需要先上传

### Ch5 编码约束

参见 `constraints.yaml`，所有 `critical` 规则在生成代码时必须满足；通用 SDK 规则（初始化、鉴权、null 检查）
引用 `_shared/constraints-common.yaml`，不在本文件重复。

### Ch6 验证扩展

除通用验证外，本域代码必须检查：

- [ ] 模块 getter 已判空（`getSafetyServiceModule()` / `getElementModule()`）
- [ ] 回调参数个数与 `references/` 方法表一致（尤其仅 error 的那批，以及 Element 的 3 个 WS 单参数回调）
- [ ] 修改类操作后是否调用了对应的 `sync`，并轮询了 `*SyncStatus`
- [ ] 几何数据满足类型约束（闭合、半径、类型-区域匹配；Element 圆形复用 `Point` 而非 `Circle`）
- [ ] 所有 `listenXxx` 的 listenId 已保存并配对 cancel（`listenDroneOfflineMapEnable` + Element 的 create/update/delete 三组）
- [ ] 解锁流程包含 `check` 失败即中止的分支

## 反幻觉规则（强制）

1. **示例代码必须来源于本 Skill 的 references/ 或用户提供的文档**，不得凭模型记忆生成 API 签名、字段名、枚举值
2. **禁止凭记忆编造**——本域 bean 字段名有细微差异（如 `deviceSn` vs `sn`、`licenseId` 有 String/int 两种），不确定时明确告知用户"需要确认 SDK 文档"
3. **引用必须可溯源**——任何建议必须能追溯到 `references/` 文档路径或官方 SDK 源码

## 权威参考

| 文档 | 内容 |
|---|---|
| `references/safety-service.md` | `SafetyServiceModule` 全部 22 个方法、bean 字段、几何约束、sync 语义、reason 码 |
| `references/element.md` | `ElementModule` 全部 11 个方法、元素/几何模型、分组层级、WS 订阅三元组 |
| `skills/_shared/constraints-common.yaml` | 跨 Skill 通用约束（回调/初始化/null 检查） |

## 共享规范

> 跨 Skill 共享约束见 `skills/_shared/constraints-common.yaml`。
> 跨 Skill 路由规则见 `skills/_shared/skill-routing.md`。当发现用户意图属于其他 Skill 职责时，按转交协议切换。
> 输出格式见 `skills/_shared/output-format.md`。Reference 按需懒加载，不要一次性读取所有 references/。
