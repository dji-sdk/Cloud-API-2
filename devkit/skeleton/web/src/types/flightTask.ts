/**
 * Copyright 2025-2026 DJI. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/** 飞行任务状态枚举。
 *
 * PREPARING（平台状态码 9）/ QUEUED（平台状态码 10）是执行前置阶段：
 * 平台会在 begin_at 之前数小时就把任务置为这两个状态，
 * 与 EXECUTING（状态码 2，无人机真正在飞）语义不同，不可混用。
 */
export type FlightTaskStatus =
  | 'SUCCESS'
  | 'FAILED'
  | 'EXECUTING'
  | 'PREPARING'
  | 'QUEUED'
  | 'PAUSED'
  | 'CANCELLED'
  | 'CREATED'

/** 状态筛选下拉项：FlightTaskStatus 外加「全部」哨兵值。 */
export interface FlightTaskStatusOption {
  value: FlightTaskStatus | 'all'
  label: string
}

/**
 * 状态筛选下拉选项（飞行任务列表与飞行记录列表共用）。
 *
 * 顺序即下拉框展示顺序：终态在前，执行中/前置阶段居中，待执行在后。
 */
export const FLIGHT_TASK_STATUS_OPTIONS: FlightTaskStatusOption[] = [
  { value: 'all', label: '全部状态' },
  { value: 'SUCCESS', label: '成功' },
  { value: 'FAILED', label: '失败' },
  { value: 'EXECUTING', label: '执行中' },
  { value: 'PREPARING', label: '准备中' },
  { value: 'QUEUED', label: '起飞排队中' },
  { value: 'PAUSED', label: '已暂停' },
  { value: 'CANCELLED', label: '已取消' },
  { value: 'CREATED', label: '待执行' },
]

/** 状态徽章配色映射。 */
export const FLIGHT_TASK_STATUS_COLORS: Record<FlightTaskStatus, string> = {
  SUCCESS: '#4ade80',
  FAILED: '#ef5350',
  EXECUTING: '#60a5fa',
  PREPARING: '#a78bfa',
  QUEUED: '#22d3ee',
  PAUSED: '#fb923c',
  CANCELLED: 'var(--app-text-muted)',
  CREATED: 'var(--app-text-muted)',
}

/** 状态中文标签映射。 */
export const FLIGHT_TASK_STATUS_LABELS: Record<FlightTaskStatus, string> = {
  SUCCESS: '成功',
  FAILED: '失败',
  EXECUTING: '执行中',
  PREPARING: '准备中',
  QUEUED: '起飞排队中',
  PAUSED: '已暂停',
  CANCELLED: '已取消',
  CREATED: '待执行',
}

/**
 * 飞行任务类型数值，直接透传给 SDK {@code CreateTaskReq.setTaskType(int)}。
 *
 * 取值对齐 es-task-management 的 `po.TaskType` iota：
 * 1=ImmediateTask，2=RepeatedTask，3=SingleTimedTask，4=SubTask，
 * 5=ContinuousTask，6=ContinuousSubTask，7=TestFlightTask。
 *
 * 该联合类型描述的是**服务端返回值的取值范围**，不是前端能产出的取值：
 * 4/6 是平台在创建时把重复/连续父任务（2/5）展开出的子任务类型（重复定时子任务 /
 * 连续执行子任务），只会出现在列表场景；7（试飞）同样只由平台下发，前端不产出。
 * 详情接口对子任务返回父任务规则，故创建/编辑表单不产出这两个取值。前端产出的取值见
 * useFlightTaskForm.mapTaskType（1/2/3/5），此处保留全部取值范围以覆盖服务端返回。
 *
 * ⚠️ 三个已知陷阱：
 * 1. 这是内核枚举，不是本通道的实际能力清单。PaaS 网关协议的 `task_type`
 *    把 2 重新定义为「条件任务」，与本文件的「2=重复定时」并不一致。
 * 2. 连续执行任务也被平台记为 1，必须以 `minBatteryCapacity > 0` 区分，
 *    判定规则见 {@link resolveTaskStrategy}。
 * 3. **PaaS 通道目前无法真正表达重复计划**：SDK 的 `CreateTaskReq`/`EditTaskReq`
 *    没有 `repeat_type`/`interval` 等 setter，协议层也没有承载位。即便前端按
 *    「2=重复定时」提交，字段也会在 BFF 层被丢弃，服务端最终按单次任务处理
 *    （已知限制）。在 SDK 与服务端补齐之前，不要依赖 repeat 策略的语义。
 */
export type FlightTaskType = 1 | 2 | 3 | 4 | 5 | 6 | 7

/** 飞行任务信息，对齐后端 FlightTaskListItemDto 返回结构。 */
export interface FlightTaskInfo {
  taskUuid: string
  name: string
  /** dock SN */
  sn: string
  status: FlightTaskStatus
  taskType: FlightTaskType
  waylineUuid: string
  /** Unix timestamp（秒） */
  createdAt: number
  /** 飞行距离（米），来自任务轨迹，无数据时为 0 */
  distanceMeters: number
  /** 最低电量阈值（百分比），无数据为 0。
   *
   * 平台把连续执行任务也记为 taskType=1，需据此字段与"立即"任务区分，
   * 判定规则见 {@link resolveTaskStrategy}。
   */
  minBatteryCapacity: number
  /**
   * 是否与同设备其他已排期任务的执行时间窗冲突。
   *
   * 由后端计算下发，前端不做任何冲突判定。字段可选以兼容不返回该标识的旧接口。
   */
  isConflicted?: boolean
  /** 是否断点续飞。平台规则：断点续飞任务不可编辑 */
  isBreakPointResume: boolean
}

/** 飞行任务列表查询参数。 */
export interface FlightTaskListParams {
  status?: FlightTaskStatus | 'all'
  startIndex?: number
  pageSize?: number
}

/** 飞行任务列表响应。 */
export interface FlightTaskListResponse {
  tasks: FlightTaskInfo[]
  totalCount: number
  hasMore: boolean
}

/** 计划类型：普通/蛙跳/手飞 */
export type PlanType = 'normal' | 'leapfrog' | 'manual'

/** 任务精度 */
export type TaskPrecision = 'rtk' | 'gnss'

/** 任务策略 */
export type TaskStrategy = 'immediate' | 'once' | 'repeat' | 'continuous'

/** 失联动作 */
export type LostControlAction = 'return_home' | 'continue'

/** 重复类型 */
export type RepeatType = 'daily' | 'weekly' | 'monthly'

/** 任务创建表单数据 */
export interface FlightTaskFormData {
  name: string
  planType: PlanType
  waylineUuid: string | null
  waylineName: string | null
  deviceSn: string | null
  deviceName: string | null
  vlmEnabled: boolean
  precision: TaskPrecision
  strategy: TaskStrategy
  beginAt: number | null
  endAt: number | null
  repeatType: RepeatType | null
  repeatInterval: number | null
  minBatteryCapacity: number | null
  continuousPeriods: [number, number][] | null
  resumableEnabled: boolean
  smartRthEnabled: boolean
  rthAltitude: number
  outOfControlAction: LostControlAction
}

/** 表单校验错误 */
export interface FlightTaskFormErrors {
  name?: string
  waylineUuid?: string
  deviceSn?: string
  beginAt?: string
  endAt?: string
  general?: string
}

/** 创建任务请求体（对齐 SDK） */
export interface CreateFlightTaskRequest {
  name: string
  task_type: number
  repeat_type: number
  plan_type?: number
  wayline_uuid: string
  sn: string
  out_of_control_action_in_flight: number
  wayline_precision_type: number
  begin_at?: number
  end_at?: number
  interval?: number
  resumable_status?: number
  rth_mode?: number
  rth_altitude?: number
  vlm_enabled?: boolean
  min_battery_capacity?: number
  continuous_task_periods?: [number, number][]
}

/** 飞行任务详情（对齐后端 FlightTaskDetailDto，编辑预填用）。
 *
 * 注意：后端详情接口不返回 planType / repeatType / repeatInterval / vlmEnabled /
 * continuousPeriods，编辑预填时以表单默认值兜底（见 useFlightTaskForm.applyDetail）。
 * 策略由 taskType 判定，不以 beginAt/endAt 是否相等去猜。
 */
export interface FlightTaskDetail {
  taskUuid: string
  name: string
  /** dock SN */
  sn: string
  /** 设备名称，由后端按 SN 反查补齐；查不到时为 null */
  deviceName: string | null
  status: FlightTaskStatus
  taskType: FlightTaskType
  waylineUuid: string
  /** 航线名称，由后端按 UUID 反查补齐；查不到时为 null */
  waylineName: string | null
  /** 任务开始时间，Unix 秒；无数据为 0 */
  beginAt: number
  /** 任务结束时间，Unix 秒；无数据为 0 */
  endAt: number
  /** 返航高度（米） */
  rthAltitude: number
  /** 返航模式 */
  rthMode: number
  /** 失联动作 */
  outOfControlActionInFlight: number
  /** 航线精度类型 */
  waylinePrecisionType: number
  /** 最低电量（百分比） */
  minBatteryCapacity: number
  /** 是否断点续飞 */
  isBreakPointResume: boolean
}
