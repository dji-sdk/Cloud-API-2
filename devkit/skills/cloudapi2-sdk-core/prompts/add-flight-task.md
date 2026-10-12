# 添加航线任务功能

## 目标
创建、上传、执行航线飞行任务。

## 涉及 SDK API
- `workspace.getFlightTaskModule()` → FlightTaskModule
- `flightTaskModule.createFlightTask(CreateTaskReq req, callback)` — 创建任务
- `flightTaskModule.executeFlightTask(String taskUuid, callback)` — 执行任务
- `flightTaskModule.updateFlightTaskStatus(String taskUuid, int status, callback)` — 暂停/恢复/取消（统一接口）
- `flightTaskModule.deleteFlightTask(String taskUuid, callback)` — 删除任务
- `flightTaskModule.getFlightTaskList(params, callback)` — 查询任务列表
- `flightTaskModule.getFlightTaskDetail(String taskUuid, callback)` — 查询任务详情

> ⚠️ 协议中没有独立的 `pauseFlightTask` / `resumeFlightTask` / `cancelFlightTask` 方法；
> 暂停、恢复、取消均通过 **`updateFlightTaskStatus(taskUuid, status, callback)`** 实现，
> 通过 `status` 参数区分操作（具体枚举值参见 SDK 文档）。

## 必须遵守的规则
- ✅ 规则 2：所有回调检查 errorCode
- ✅ 规则 5：getWorkspaceInstance null 检查
- ⚠️ 航线任务执行不需要 acquireFlightControlAuthority（任务模块自管理控制权）

## 实现步骤

1. **创建 FlightTaskService**
   - 获取 FlightTaskModule
   - 实现 CRUD 操作
   - 任务状态监听

2. **创建 Controller**
   - POST `/api/flight-task` — 创建任务（传入 CreateTaskReq）
   - POST `/api/flight-task/{uuid}/execute` — 执行
   - PATCH `/api/flight-task/{uuid}/status` — 更新状态（暂停/恢复/取消）
   - DELETE `/api/flight-task/{uuid}` — 删除任务

3. **任务状态流转**
   ```
   Created → Executing → Success
                ↓           ↑
             Paused ─── Resume
                ↓
            Cancelled
   ```

## 注意事项
- 航线任务是异步的，创建后需要监听执行状态
- 任务执行中设备断联：检查 OutOfControlAction 配置
- 同一设备同时只能执行一个航线任务
- 参数使用 `taskUuid`（不是 `taskId`）

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
- ❌ 航线任务执行前调用 acquireFlightControlAuthority（任务模块自管理）
- ❌ 创建任务后不监听状态就认为执行成功
- ❌ 不处理任务执行失败的回调
- ❌ 误用 `pauseFlightTask()` / `resumeFlightTask()` / `cancelFlightTask()`（这些方法不存在）
- ❌ 用 `taskType` 拼重复调度：`CreateTaskReq` 上没有重复规则的承载字段，接口会成功但规则丢失
