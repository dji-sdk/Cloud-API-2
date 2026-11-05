---
title: "任务能力矩阵与重复调度限制"
module: api-reference
tags: [flight-task, task-type, repeat-task, continuous-task, plan-type, known-limitation]
audience: [agent, developer]
complexity: intermediate
summary: "PaaS 通道下任务策略的送达边界：立即/单次定时可用，重复定时、连续执行、蛙跳、手飞、多模态识别在协议层没有承载位；续飞策略仅创建可写"
last_updated: 2026-11-03
sdk_version: "3.3.7"
source: "flight-hub-sdk-3.3.7.jar 反编译核对（bean/generated CreateTaskReq/EditTaskReq 全字段 + 原生库字符串表）；平台侧任务创建判别式复核；devkit SdkFlightTaskCommandService"
related:
  - api-reference/core-rules.md
  - api-reference/flight-control.md
---

界面上能选的任务策略不等于能送达服务端的策略：重复定时、连续执行、蛙跳、手飞在 SDK 协议层没有承载字段，提交后**接口照样返回成功**，但重复规则已被悄悄丢掉。

# 任务能力矩阵与重复调度限制

> **定位**：创建/编辑任务前先确认能力边界，避免把不可达的策略写进集成方案。
> **来源**：`flight-hub-sdk-3.3.7.jar` 反编译核对（2026-09-29 复核仍成立）+ 平台侧任务创建判别式

---

## 能力矩阵

| 策略 | 前端取值 | PaaS 通道可达 | 提交后实际行为 |
|------|----------|--------------|----------------|
| 立即执行 | `immediate` | ✅ | 按立即任务执行 |
| 单次定时 | `once` | ✅ | 到点执行一次 |
| 重复定时 | `repeat` | ❌ | 接口返回成功；重复规则被丢弃，落库为一条不带重复语义的普通任务 |
| 连续执行 | `continuous` | ❌ | 同上；最低电量等连续参数同样无处安放 |
| 普通计划 | `normal` | ✅ | — |
| 蛙跳 | `leapfrog` | ❌ | `plan_type` 丢失 |
| 手飞 | `manual` | ❌ | `plan_type` 丢失 |

**共同点：全程不报错。** 无论落到哪个不可达策略，接口都返回成功，只能靠回查任务详情才能发现规则没生效——这是本文最需要记住的一条。

需要重复调度能力的集成方案，在当前 PaaS SDK 上无法通过任务模块实现。

## 根因：协议层没有承载位

`CreateTaskReq` / `EditTaskReq` 是 SDK 向服务端表达任务创建与编辑的**唯一**载体。逐字段核对 SDK 请求类与原生库（`libs/amd64/flight-hub-sdk-3.3.7.jar` 的 `CreateTaskReq` / `EditTaskReq` 及其内嵌 `libflighthubsdk_jni.dylib` 字符串表）后确认，两个类都**不含**以下字段的任何 setter：

`repeat_type`、`interval`、`plan_type`、`vlm_enabled`、`continuous_task_periods`

`CreateTaskReq` 的私有字段共 **22 个**（即序列化后发往服务端的字段集）：

```
name, sn, task_type, time_zone, wayline_uuid, out_of_control_action_in_flight,
rth_altitude, rth_mode, begin_at, end_at, device_type, landing_dock_sn,
min_battery_capacity, wayline_precision_type, wayline_segment_indexes,
wayline_duration, uuid, tags, is_break_point_resume, break_point, signal_type,
resumable_status
```

注意 `begin_at` / `end_at` **在**请求体内，`repeat_type` 不在——这正是「时间能送达、重复规则送不到」的协议层原因。

`taskType` 是唯一能表达任务类型的字段，粒度不足以承载重复规则。`EditTaskReq` 是**独立类**（不是 `CreateTaskReq` 的子类），字段更少：没有 `device_type` / `wayline_duration` / `signal_type`，**也没有 `resumable_status`**——续飞策略只在创建时可写，编辑态无承载位。

**升级 SDK 不解决此问题**：3.1.6 ~ 3.3.7 的请求类字段与原生库字符串表均无承载位（2026-10-10 以 3.3.7 复核确认）。

> 一处容易误判的旁证：SDK 的**试飞任务**接口（`create_test_flight_task`）请求体里**是带** `repeat_type` 的。但它走的是另一条路由（`/task/api/v1/workspaces/{project_id}/flight-tasks/test-flight`），与正式任务创建不是同一条链路，不能据此认为 `createFlightTask` 也支持重复。

## 提交后发生了什么

1. **BFF 层丢弃** — 请求 DTO 里的 `repeat_type` 等字段无法映射到 SDK 请求对象，在构造 `CreateTaskReq` 时被直接丢弃，仅有 WARN 日志。
2. **网关透传** — 落到服务端的请求只剩 `task_type` 与 `begin_at` / `end_at`。
3. **请求打到平台任务服务** — SDK 的 `createFlightTask` 固定请求 `POST /gateway/atc/api/v1/flight-tasks`，由平台侧任务服务承接。
4. **入参校验不拦截** — 服务端的请求结构是**扁平结构**，没有内嵌带 `required_if=TaskType 2/5` 约束的任务规则校验，`begin_at` / `end_at` 也没有 binding 约束。因此 `task_type=2` **不会**触发参数校验失败——第一个可疑信号就此消失。
5. **服务端按 task_type 分流** — 服务端任务控制器的判别式只识别「立即任务」，其余全部进 else：

```go
if po.TaskType(params.TaskType) == po.ImmediateTask {
    targetTask, err = t.TaskService.AddImmediatelyTask(c, &params, wayline)
} else { // 条件任务
    targetTask, err = t.TaskService.AddRepeatedTask(c, &params, wayline)
}
```

6. **重复分支已被注释掉** — 服务端重复任务创建逻辑中，计算子任务、写 `extended_begin_at`、创建重复任务记录的整段代码处于注释状态。函数最终只落一条普通任务、发一个创建事件就返回。

关键点：**判决条件是 `== ImmediateTask`，不是 `!= 定时任务`**。`task_type=2`（重复定时）与 `5`（连续执行）都落进 `else`，但重复规则字段已在第 1 步丢失，`else` 分支又不再做任何重复调度展开，结果就是一条只带起止时间的普通任务——**没有任何报错，接口返回成功**。

## 续飞策略与多模态识别

这两个字段常被误当成「和别的字段一样」，实际各有独立的能力边界。

| 字段 | 创建可达 | 编辑可达 | 详情/列表回显 |
|------|----------|----------|----------------|
| `resumable_status`（续飞策略 `auto` / `manual`） | ✅ 映射为 `CreateTaskReq.setResumableStatus` | ❌ `EditTaskReq` 无承载位 | ❌ 只回 `is_break_point_resume` |
| `is_break_point_resume`（是否为断点续飞任务） | ⚠️ 勿由续飞策略推导 | 有承载位 | ✅ |
| `vlm_enabled`（多模态识别） | ❌ 两个请求类均无承载位 | ❌ | ❌ |

三条容易踩错的地方：

1. **`resumable_status` ≠ `is_break_point_resume`。** 前者是「出了断点后怎么续」（`auto` 自动 / `manual` 手动），后者是「本任务是不是断点续飞任务」（这类任务不可编辑）。历史实现把 `resumable_status=1` 写成 `setBreakPointResume(true)`，改变了任务类型语义，而且详情回的是 `is_break_point_resume=false`，看起来像「续飞关掉了」。正确映射是 `setResumableStatus("auto"|"manual")`。
2. **`setResumableStatus(null)` 不等于「不设置」。** SDK 的 setter 对 null 写空串（`ifnull → ""`），会造成「未设置」变成「空值」。Java 侧必须先判空再调用。
3. **`is_break_point_resume=false` 不能反推续飞策略已关闭。** 详情 / 列表接口根本没有返回 `resumable_status`，前端不得用它回填「自动断点续飞」开关。编辑态既无法还原原策略，也不该盲发，否则用户会误以为改动生效。

`vlm_enabled` 在 SDK 3.3.7 的创建与编辑请求里都没有承载位，提交 `true` 只会在 BFF 层记 WARN 后丢弃。接入方若依赖多模态识别，必须按「暂不支持」处理，不能只看接口返回成功。

## 编辑态陷阱

把不可用策略的入口置灰只挡「新建」，**挡不住「编辑后保存」**：

编辑一张历史重复任务时，详情接口会把策略预填为 `repeat` 使选中态可见。用户不动策略直接点保存，请求仍会带上 `task_type=2`，走完上述链路后重复语义被静默改写——**原有重复规则消失**。

因此编辑态必须在策略字段旁给出明确的后果提示，不能只依赖按钮置灰。

## 排查指引

遇到「界面选了重复定时，提交后不重复、也没有任何报错」时，按顺序核对：

1. 确认走的是 PaaS 通道而非 SaaS 通道 —— SaaS 通道的定时任务工作正常，不要据此判断 DJI Cloud API 2 整体不支持定时。
2. 检查 BFF 日志中是否有「请求含 SDK 无法承载的字段，已丢弃」的 WARN，其 `丢弃字段` 列表会直接列出被丢的字段名与值。
3. 若日志为空，说明字段在更早的 DTO 映射阶段就未透传，检查前端到 BFF 的请求体。
4. 服务端不会报错，**别把「接口返回成功」当成「重复规则已生效」**——要回查任务详情里的重复规则字段。

## 边界说明

这是**集成协议**缺口，不是产品能力缺口：内核任务模型携带完整的重复语义，SaaS 通道的重复定时与连续执行均正常。

补齐需要两侧同时动，devkit 单侧无法闭环：

1. **SDK 侧** — 在 `CreateTaskReq` / `EditTaskReq` 上增加 `repeat_type` / `interval` / `extended_begin_at` 等承载位。
2. **平台侧** — 服务端任务创建请求需要内嵌任务规则结构以恢复参数校验，重复调度处理分支中被注释的逻辑需要恢复。

只做其中一侧都不会有可观测变化：SDK 加了字段但服务端不收，或服务端恢复了分支但 SDK 送不到规则，结果都是继续静默落成普通任务。
