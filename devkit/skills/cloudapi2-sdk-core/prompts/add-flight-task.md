# 添加航线任务功能

## 目标
创建、查询、控制（暂停/恢复）航线飞行任务。

## 模块划分（容易搞混，先分清楚）

官方把"航线任务"拆成了两个作用域不同的模块，不要混用：

| 模块 | 作用域 | 职责 |
| --- | --- | --- |
| `FlightTaskModule` | 工作空间级（`workspace.getFlightTaskModule()`） | 任务的创建/编辑/删除/查询/状态编排，**不负责**运行中任务的实时暂停/恢复 |
| `FlightTaskControlModule` | 飞机级（`drone.getFlightTaskControlModule()`） | 任务运行中的实时暂停 `pause()` / 恢复 `resume()`，**不负责**创建/查询/删除 |

**立即任务（`taskType=1`）创建后自动下发执行，没有独立的"执行"接口** —— 不存在 `executeFlightTask()` 这个方法，官方 `FlightTaskModule` 能力清单里共 17 个方法（本文档只列出任务 CRUD/状态编排相关的部分，见下），没有任何名字带 `execute` 的创建态方法。

## 涉及 SDK API

### FlightTaskModule（工作空间级，任务编排）
- `workspace.getFlightTaskModule()` → FlightTaskModule
- `flightTaskModule.createFlightTask(CreateTaskReq req, callback)` — 创建任务（立即任务创建后自动执行，无需额外调用执行）
- `flightTaskModule.editFlightTask(String taskUuid, EditTaskReq req, callback)` — 编辑任务
- `flightTaskModule.updateFlightTaskStatus(String taskUuid, int status, callback)` — 更新任务状态（用于终止等状态编排，**不是**暂停/恢复的手段）
- `flightTaskModule.batchUpdateFlightTaskStatus(req, callback)` — 批量更新任务状态
- `flightTaskModule.deleteFlightTask(String taskUuid, callback)` — 删除任务
- `flightTaskModule.getFlightTaskList(params, callback)` — 分页查询任务列表
- `flightTaskModule.getFlightTaskDetail(String taskUuid, callback)` — 查询任务详情
- `flightTaskModule.getFlightTaskTrack(params, callback)` — 查询任务轨迹

### FlightTaskControlModule（飞机级，运行中实时控制）
- `drone.getFlightTaskControlModule()` → FlightTaskControlModule
- `flightTaskControlModule.pause(callback)` — 暂停当前航线任务，飞机原地悬停等待（仅返回错误信息）
- `flightTaskControlModule.resume(callback)` — 从暂停点恢复执行（仅返回错误信息）
- `flightTaskControlModule.listenWaylineTaskEvent(callback)` → 返回 `int listenId` — 订阅该飞机航线任务事件（创建/属性更新/删除/障碍物变更/执行输出），**事件回调无 error 参数**
- `flightTaskControlModule.cancelListenWaylineTaskEvent(listenId)` — 取消订阅

> ⚠️ **协议中没有独立的 `cancelFlightTask` 方法**；取消/终止任务走工作空间级的
> **`updateFlightTaskStatus(taskUuid, status, callback)`**，通过 `status` 目标值区分操作。
> 但**暂停、恢复不走这个方法** —— 暂停/恢复是飞机级 `FlightTaskControlModule.pause()` / `resume()`，
> 调用 `updateFlightTaskStatus` 去做暂停/恢复不会生效。

## 必须遵守的规则
- ✅ 规则 2：所有回调检查 errorCode
- ✅ 规则 5：getWorkspaceInstance / getDroneInstance null 检查
- ⚠️ 航线任务的创建/编辑/删除/查询不需要 acquireFlightControlAuthority（任务模块自管理控制权）
- ⚠️ `getFlightTaskModule()` 和 `getFlightTaskControlModule()` 分别挂在 `Workspace` 和 `Drone` 实例上，不要调反

## 实现步骤

1. **创建 FlightTaskService**（工作空间级，封装 `FlightTaskModule`）
   - 获取 FlightTaskModule
   - 实现创建/编辑/删除/查询
2. **创建 FlightTaskControlService**（飞机级，封装 `FlightTaskControlModule`）
   - 获取 FlightTaskControlModule（需要先知道目标飞机 SN）
   - 实现暂停/恢复，订阅任务事件驱动前端面板刷新

3. **创建 Controller**
   - POST `/api/flight-task` — 创建任务（传入 CreateTaskReq，立即任务自动执行）
   - PATCH `/api/flight-task/{uuid}/status` — 更新状态（终止等状态编排，**不包括**暂停/恢复）
   - DELETE `/api/flight-task/{uuid}` — 删除任务
   - POST `/api/drone/{sn}/flight-task/pause` — 暂停（走 `FlightTaskControlModule.pause()`）
   - POST `/api/drone/{sn}/flight-task/resume` — 恢复（走 `FlightTaskControlModule.resume()`）

3. **任务状态流转**（javadoc 枚举：0待开始/1启动失败/2执行中/3已暂停/4已终止/5成功/6挂起/7超时/8部分执行/9准备中/10起飞排队中）
   ```
   待开始(0) → 执行中(2) → 成功(5)
                  ↓ pause()      ↑ resume()
               已暂停(3) ─────────┘
                  ↓ updateFlightTaskStatus(终止)
               已终止(4)
   ```

## 注意事项
- 航线任务是异步的，创建后需要监听执行状态
- 任务执行中设备断联：检查 `outOfControlActionInFlight` 配置（1=返航，2=继续执行）
- 同一设备同时只能执行一个航线任务
- 参数使用 `taskUuid`（不是 `taskId`）
- `waylineUuid` 来自航线资产管理模块（`workspace.getWaylineManageModule()`），创建任务前先确认航线已存在；完整调用链（列表/详情/上传/删除等）见 `add-wayline-management.md`

> ⚠️ **`taskType` 的可达取值没有文档承诺**。`CreateTaskReq` 序列化后实际发往服务端的字段只有 21 个
> （`name, sn, task_type, time_zone, wayline_uuid, out_of_control_action_in_flight, rth_altitude,
> rth_mode, begin_at, end_at, device_type, landing_dock_sn, min_battery_capacity,
> wayline_precision_type, wayline_segment_indexes, wayline_duration, uuid, tags,
> is_break_point_resume, break_point, signal_type`），**没有** `repeat_type` / `interval` /
> `extended_begin_at` / `continuous_task_periods` / `plan_type` 的位置。
> 因此**不要**仅凭 `taskType` 枚举值就认定重复定时、连续执行等策略能生效：请求会返回成功，
> 但重复规则到不了服务端。完整能力边界与失败链路见知识库
> `knowledge/api-reference/flight-task-capabilities.md`。

## 常见错误
- ❌ 航线任务创建前调用 acquireFlightControlAuthority（任务模块自管理）
- ❌ 创建任务后不监听状态就认为执行成功
- ❌ 不处理任务执行失败的回调
- ❌ 误用 `executeFlightTask()`（**该方法不存在**，立即任务创建后自动执行）
- ❌ 误用 `pauseFlightTask()` / `resumeFlightTask()` / `cancelFlightTask()`（这些方法不存在）
- ❌ 用 `FlightTaskModule.updateFlightTaskStatus()` 实现暂停/恢复（**不会生效**，暂停/恢复是飞机级 `FlightTaskControlModule.pause()`/`resume()`，不在 `FlightTaskModule` 上）
- ❌ 用 `taskType` 拼重复调度：`CreateTaskReq` 上没有重复规则的承载字段，接口会成功但规则丢失

---

## 飞行记录导出（FlightTaskRecordModule）

### 模块位置

```java
FlightTaskRecordModule record = workspace.getFlightTaskRecordModule();
```

挂在 **Workspace** 上（标准 try/catch → null + log 判空模式）。全部 HTTP。

### 3 个方法 —— 导出与查进度参数形状不对称

| 方法 | 参数形状 | 回调形态 | 链路 |
|---|---|---|---|
| `exportFlightRecords(userId, ExportFlightRecordsRequestParams, callback)` | **结构化 bean** | `(error, result)` | HTTP `POST .../records/export` |
| `exportFlightRecords(userId, contentType, fileName, timeZone, locale, fileType, flightTaskIds, metrics, callback)` | 便捷重载，同上封装为 bean | `(error, result)` | 同上 |
| `getExportFlightRecordsProgress(contentType, status, sort, page, pageSize, exportId, callback)` | **全平铺参数，没有 params bean** | `(error, result)` | HTTP `GET .../records/export` |

> ⚠️ **导出用结构化 bean，查进度用平铺参数——两者不对称**：`exportFlightRecords` 接受一个 `ExportFlightRecordsRequestParams` 对象（或其字段展开的便捷重载），而 `getExportFlightRecordsProgress` 的公开签名直接是 6 个平铺参数（`contentType/status/sort/page/pageSize/exportId`），**没有**对应的 params bean 暴露给应用层——内部确实会组装一个 `GetExportFlightRecordsProgressParams`，但那是方法体内部自己 `new` 出来的，调用方碰不到。不要假设两个方法用同一种调用风格。

`exportFlightRecords` 必填字段：`contentType`、`fileType`、`fileName`、`flightTaskIds`（源码 javadoc 明确标注，缺任一个会返回错误）。

### 示例

```java
FlightTaskRecordModule record = workspace.getFlightTaskRecordModule();
if (record == null) {
    return;
}

record.exportFlightRecords(
    userId,
    "details",                      // contentType
    "flight-report.xlsx",           // fileName
    "Asia/Shanghai",
    "zh",
    List.of("Excel"),               // fileType
    List.of(taskId1, taskId2),      // flightTaskIds
    List.of("altitude", "speed"),   // metrics
    (error, result) -> {
        if (error.getErrorCode() != FlightHubSdkErrorCode.SUCCESS) {
            return;
        }
        // result.exportId 用于后续查进度
    });

record.getExportFlightRecordsProgress(
    "details", 0, "desc", 1, 20, exportId,
    (error, result) -> {
        // 没有 params bean，6 个参数直接平铺传入
    });
```

### 常见错误
- ❌ 以为 `getExportFlightRecordsProgress` 也接受一个 params 对象 —— 它的公开 API 是平铺参数，没有 bean 入口
- ❌ 调 `exportFlightRecords` 时漏填 `contentType`/`fileType`/`fileName`/`flightTaskIds` 任一项 —— 四者均为必填，源码 javadoc 已明确
- ❌ 把本模块当作任务状态查询的入口 —— 任务本身的状态/详情查询在 `FlightTaskModule.getFlightTaskDetail`，本模块只负责「导出记录」这一件事
