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

import type {
  CreateFlightTaskRequest,
  FlightTaskDetail,
  FlightTaskInfo,
  FlightTaskListParams,
  FlightTaskListResponse,
  FlightTaskStatus,
} from '@/types/flightTask'
import { authFetch } from '@/api/auth'
import { getEditBlockTip } from '@/utils/flightTaskEditability'
import { API_BASE } from './baseUrl'
import { getRuntimeConfig } from '@/utils/runtimeConfig'

const FLIGHT_TASK_BASE = `${API_BASE}/api/v1/flight-tasks`

/** 任务名称在工作空间内已存在的业务错误码（与 BFF FlightTaskRestController 保持一致）。 */
export const ERROR_CODE_TASK_NAME_DUPLICATE = 'TASK_NAME_DUPLICATE'

/** 任务当前不可编辑的业务错误码（与 BFF FlightTaskRestController 保持一致）。 */
export const ERROR_CODE_TASK_NOT_EDITABLE = 'TASK_NOT_EDITABLE'

/**
 * 飞行任务接口错误。
 *
 * 在 {@link Error} 的 message 之外保留后端返回的业务错误码，
 * 便于调用方把错误定位到具体表单项（如重名落到名称输入框）。
 */
export class FlightTaskApiError extends Error {
  /** 后端业务错误码，未返回时为 undefined */
  readonly code?: string

  constructor(message: string, code?: string) {
    super(message)
    this.name = 'FlightTaskApiError'
    this.code = code
  }
}

/** 后端错误响应体。 */
interface FlightTaskErrorBody {
  message?: string
  error?: string
  code?: string
}

function isMockEnabled(): boolean {
  return getRuntimeConfig('MOCK_ENABLED') === 'true'
}

/** 从后端错误响应中提取 message 与业务错误码，解析失败则回退到 fallback。 */
async function extractErrorInfo(
  res: Response,
  fallback: string,
): Promise<{ message: string; code?: string }> {
  try {
    const body = (await res.json()) as FlightTaskErrorBody
    return {
      message: body.message || body.error || `${fallback} (HTTP ${res.status})`,
      code: body.code,
    }
  } catch {
    return { message: `${fallback} (HTTP ${res.status})` }
  }
}

/** 依据后端错误响应构造 {@link FlightTaskApiError}，供各命令接口统一抛出。 */
async function buildApiError(res: Response, fallback: string): Promise<FlightTaskApiError> {
  const { message, code } = await extractErrorInfo(res, fallback)
  return new FlightTaskApiError(message, code)
}

/**
 * 判断错误是否由「任务名称重复」触发。
 *
 * 不使用 instanceof：错误对象可能经序列化或多模块实例传递，
 * 只按 code 字段判定更稳健。
 *
 * @param error 任意来源的错误对象
 * @return 为任务重名错误时返回 true
 */
export function isTaskNameDuplicateError(error: unknown): boolean {
  return hasErrorCode(error, ERROR_CODE_TASK_NAME_DUPLICATE)
}

/**
 * 判断错误是否由「任务当前不可编辑」触发。
 *
 * 与 {@link isTaskNameDuplicateError} 同理，只按 code 字段判定。
 * 后端在该错误下还会返回 {@code reasons} 数组，错误 message 已是各原因拼接后的文案，
 * 可直接展示给用户。
 *
 * @param error 任意来源的错误对象
 * @return 为不可编辑错误时返回 true
 */
export function isTaskNotEditableError(error: unknown): boolean {
  return hasErrorCode(error, ERROR_CODE_TASK_NOT_EDITABLE)
}

/**
 * 按业务错误码判定错误类型。
 *
 * 断言原因：error 为 unknown，只能逐字段探测是否存在 code。
 *
 * @param error 任意来源的错误对象
 * @param code  期望的业务错误码
 * @return 错误码匹配时返回 true
 */
function hasErrorCode(error: unknown, code: string): boolean {
  const actual = (error as { code?: unknown } | null | undefined)?.code
  return actual === code
}

/**
 * Mock 模式下校验任务名称未被占用，与 BFF 的工作空间维度校验保持一致。
 *
 * 注意：MOCK_TASKS 是静态数据，新建的名称不会写回列表，
 * 因此 mock 模式只能拦截与预置数据重名的场景。
 *
 * @param name            待校验的任务名称
 * @param excludeTaskUuid 编辑场景需排除的任务 UUID
 */
function assertMockNameUnique(name: string, excludeTaskUuid?: string): void {
  const duplicated = MOCK_TASKS.some(
    t => t.name === name && t.taskUuid !== excludeTaskUuid,
  )
  if (duplicated) {
    throw new FlightTaskApiError(
      `任务名称「${name}」已存在，请更换名称`,
      ERROR_CODE_TASK_NAME_DUPLICATE,
    )
  }
}

/**
 * Mock 模式下模拟 BFF / 平台的编辑门禁，与 {@link FlightTaskRestController}
 * 的 {@code validateTaskEditable} 判定条件保持一致。
 *
 * 用途是让 ISV 在离线 mock 下也能复现「编辑任务失败」并看到具体原因
 * （对应内部工单跟踪的断点续飞任务编辑门禁场景），而不是只修好真实链路。
 *
 * @param taskUuid 被编辑的任务 UUID
 */
function assertMockTaskEditable(taskUuid: string): void {
  const task = MOCK_TASKS.find(t => t.taskUuid === taskUuid)
  // mock 列表未收录该任务时不拦截，避免影响后续的重名校验分支
  if (!task) {
    return
  }
  const tip = getEditBlockTip(task)
  if (tip) {
    throw new FlightTaskApiError(tip, ERROR_CODE_TASK_NOT_EDITABLE)
  }
}

/**
 * mock 飞行任务数据。
 *
 * 与后端 mock 实现的差异：此处为 11 条名称各异的固定数据，后端 mock 固定生成
 * 30 条且刻意包含重名分组，用于覆盖列表判重场景。前端 mock 只用于离线开发调试，
 * 不覆盖「列表中存在重名任务」的展示分支。
 *
 * taskType 覆盖平台 TaskType 枚举的全部业务取值（1=即时, 2=重复定时,
 * 3=单次定时, 4=重复定时子任务, 5=连续执行, 6=连续执行子任务），
 * 且含 minBatteryCapacity > 0 的 taskType=1 记录——平台把连续执行任务
 * 也记为 1，缺少这类样例时列表的"连续执行"分支在 mock 模式下不可见。
 *
 * 其中「待执行」(CREATED) 任务刻意造出两种可编辑性，便于离线验证列表页「编辑」
 * 按钮的置灰分支：task-008 为断点续飞（不可编辑），task-011 为普通单次定时（可编辑）。
 */
const MOCK_TASKS: FlightTaskInfo[] = [
  {
    taskUuid: 'task-001-abc',
    name: '厂区北侧巡检',
    sn: 'MOCK_DRONE_0001',
    status: 'SUCCESS',
    taskType: 1,
    waylineUuid: 'wl-001-abc',
    createdAt: 1721635200,
    distanceMeters: 1520,
    minBatteryCapacity: 0,
    isBreakPointResume: false,
  },
  {
    taskUuid: 'task-002-def',
    name: '光伏板定期巡查',
    sn: '1581F6GKD233400002',
    status: 'SUCCESS',
    taskType: 2,
    waylineUuid: 'wl-002-def',
    createdAt: 1721548800,
    distanceMeters: 2040,
    minBatteryCapacity: 0,
    isBreakPointResume: true,
  },
  {
    taskUuid: 'task-003-ghi',
    name: '仓库周边安防巡逻',
    sn: 'MOCK_DRONE_0001',
    status: 'FAILED',
    taskType: 1,
    waylineUuid: 'wl-003-ghi',
    createdAt: 1721462400,
    distanceMeters: 0,
    // 平台记为 taskType=1 的连续执行任务：靠电量阈值与"立即"任务区分
    minBatteryCapacity: 30,
    isBreakPointResume: false,
  },
  {
    taskUuid: 'task-004-jkl',
    name: '河道水质监测',
    sn: '1581F6GKD233400003',
    status: 'EXECUTING',
    taskType: 3,
    waylineUuid: 'wl-004-jkl',
    createdAt: 1721376000,
    distanceMeters: 880,
    minBatteryCapacity: 0,
    isBreakPointResume: false,
  },
  {
    taskUuid: 'task-005-mno',
    name: '建筑工地进度航拍',
    sn: '1581F6GKD233400002',
    status: 'PAUSED',
    taskType: 2,
    waylineUuid: 'wl-005-mno',
    createdAt: 1721289600,
    distanceMeters: 1300,
    minBatteryCapacity: 0,
    // 与 task-008-vwx 同设备且同排期档位，用于演示冲突标识
    isConflicted: true,
    isBreakPointResume: true,
  },
  {
    taskUuid: 'task-006-pqr',
    name: '高压线塔通道巡检',
    sn: 'MOCK_DRONE_0001',
    status: 'SUCCESS',
    taskType: 5,
    waylineUuid: 'wl-006-pqr',
    createdAt: 1721203200,
    distanceMeters: 3120,
    minBatteryCapacity: 50,
    isBreakPointResume: false,
  },
  {
    taskUuid: 'task-007-stu',
    name: '农田植保喷洒任务',
    sn: '1581F6GKD233400003',
    status: 'CANCELLED',
    taskType: 4,
    waylineUuid: 'wl-007-stu',
    createdAt: 1721116800,
    distanceMeters: 0,
    minBatteryCapacity: 0,
    isBreakPointResume: true,
  },
  {
    taskUuid: 'task-008-vwx',
    name: '园区道路巡查（定时）',
    sn: '1581F6GKD233400002',
    status: 'CREATED',
    taskType: 3,
    waylineUuid: 'wl-008-vwx',
    createdAt: 1721030400,
    distanceMeters: 0,
    minBatteryCapacity: 0,
    // 与 task-005-mno 同设备且同排期档位，用于演示冲突标识
    isConflicted: true,
    // 断点续飞：平台侧禁止编辑，用于验证列表「编辑」按钮置灰分支
    isBreakPointResume: true,
  },
  {
    taskUuid: 'task-009-yza',
    name: '应急响应：山体滑坡勘测',
    sn: 'MOCK_DRONE_0001',
    status: 'SUCCESS',
    taskType: 1,
    waylineUuid: 'wl-009-yza',
    createdAt: 1720944000,
    distanceMeters: 4560,
    minBatteryCapacity: 0,
    isBreakPointResume: false,
  },
  {
    taskUuid: 'task-010-bcd',
    name: '港口船舶停泊监控',
    sn: '1581F6GKD233400003',
    status: 'FAILED',
    taskType: 6,
    waylineUuid: 'wl-010-bcd',
    createdAt: 1720857600,
    distanceMeters: 0,
    minBatteryCapacity: 40,
    isBreakPointResume: false,
  },
  {
    taskUuid: 'task-011-efg',
    name: '单次定时巡检',
    sn: '1581F6GKD233400002',
    status: 'CREATED',
    taskType: 3,
    waylineUuid: 'wl-011-efg',
    createdAt: 1720771200,
    distanceMeters: 0,
    minBatteryCapacity: 0,
    // 非断点续飞的单次定时任务：可编辑，用于验证按钮可点击分支
    isBreakPointResume: false,
  },
]

function filterMockTasks(
  status: FlightTaskStatus | 'all' | undefined,
  startIndex: number,
  pageSize: number,
): FlightTaskListResponse {
  const filtered = (!status || status === 'all')
    ? MOCK_TASKS
    : MOCK_TASKS.filter(t => t.status === status)

  const paged = filtered.slice(startIndex, startIndex + pageSize)
  return {
    tasks: paged,
    totalCount: filtered.length,
    hasMore: startIndex + pageSize < filtered.length,
  }
}

/** 查询飞行任务列表。 */
export async function fetchFlightTaskList(
  params: FlightTaskListParams = {},
): Promise<FlightTaskListResponse> {
  const startIndex = params.startIndex ?? 0
  const pageSize = params.pageSize ?? 20

  if (isMockEnabled()) {
    return filterMockTasks(params.status, startIndex, pageSize)
  }

  const query = new URLSearchParams()
  if (params.status && params.status !== 'all') {
    query.set('status', params.status)
  }
  const page = Math.floor(startIndex / pageSize) + 1
  query.set('page', String(page))
  query.set('pageSize', String(pageSize))

  const url = `${FLIGHT_TASK_BASE}?${query.toString()}`
  const res = await authFetch(url)
  if (!res.ok) {
    throw new Error('获取飞行任务列表失败')
  }
  return res.json() as Promise<FlightTaskListResponse>
}

/** 创建飞行任务。 */
export async function createFlightTask(
  data: CreateFlightTaskRequest,
): Promise<{ taskUuid: string }> {
  if (isMockEnabled()) {
    assertMockNameUnique(data.name)
    return { taskUuid: `task-${Date.now()}-mock` }
  }

  const res = await authFetch(FLIGHT_TASK_BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) {
    throw await buildApiError(res, '创建飞行任务失败')
  }
  return res.json() as Promise<{ taskUuid: string }>
}

/** 执行已创建的任务。 */
export async function executeFlightTask(taskUuid: string): Promise<void> {
  if (isMockEnabled()) return
  const res = await authFetch(`${FLIGHT_TASK_BASE}/${taskUuid}/execute`, { method: 'POST' })
  if (!res.ok) throw await buildApiError(res, '执行任务失败')
}

/** 暂停执行中的任务。 */
export async function pauseFlightTask(taskUuid: string): Promise<void> {
  if (isMockEnabled()) return
  const res = await authFetch(`${FLIGHT_TASK_BASE}/${taskUuid}/pause`, { method: 'POST' })
  if (!res.ok) throw await buildApiError(res, '暂停任务失败')
}

/** 恢复已暂停的任务。 */
export async function resumeFlightTask(taskUuid: string): Promise<void> {
  if (isMockEnabled()) return
  const res = await authFetch(`${FLIGHT_TASK_BASE}/${taskUuid}/resume`, { method: 'POST' })
  if (!res.ok) throw await buildApiError(res, '恢复任务失败')
}

/** 取消任务。 */
export async function cancelFlightTask(taskUuid: string): Promise<void> {
  if (isMockEnabled()) return
  const res = await authFetch(`${FLIGHT_TASK_BASE}/${taskUuid}/cancel`, { method: 'POST' })
  if (!res.ok) throw await buildApiError(res, '取消任务失败')
}

/** 查询单个任务详情（编辑预填用）。mock 数据无详情字段，仅校验任务存在。 */
export async function fetchFlightTaskDetail(taskUuid: string): Promise<FlightTaskDetail> {
  if (isMockEnabled()) {
    const task = MOCK_TASKS.find(t => t.taskUuid === taskUuid)
    if (!task) {
      throw new Error('任务不存在')
    }
    // mock 数据无 beginAt/endAt 等详情字段，按后端约定（无数据为 0）补齐；
    // minBatteryCapacity/isBreakPointResume 均沿用列表值展开，无需覆盖，
    // 否则连续执行任务无法回显策略，且会破坏列表与详情可编辑性一致
    return {
      ...task,
      beginAt: 0,
      endAt: 0,
      rthAltitude: 100,
      rthMode: 0,
      outOfControlActionInFlight: 1,
      waylinePrecisionType: 0,
    }
  }

  const res = await authFetch(`${FLIGHT_TASK_BASE}/${taskUuid}`)
  if (!res.ok) {
    throw new Error('获取任务详情失败')
  }
  return res.json() as Promise<FlightTaskDetail>
}

/** 编辑任务（更新名称、执行时间、返航高度等参数）。 */
export async function editFlightTask(
  taskUuid: string,
  data: CreateFlightTaskRequest,
): Promise<void> {
  if (isMockEnabled()) {
    // 与 BFF 门禁保持一致：mock 模式下同样拦截不可编辑任务，便于离线复现
    // 断点续飞任务不支持编辑的「编辑任务失败」场景
    assertMockTaskEditable(taskUuid)
    // 编辑场景需排除任务自身，否则「不改名直接保存」会被判定为重名
    assertMockNameUnique(data.name, taskUuid)
    return
  }

  const res = await authFetch(`${FLIGHT_TASK_BASE}/${taskUuid}/edit`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  if (!res.ok) {
    throw await buildApiError(res, '编辑任务失败')
  }
}

/** 删除任务（仅未下发的任务允许删除）。 */
export async function deleteFlightTask(taskUuid: string): Promise<void> {
  if (isMockEnabled()) return
  const res = await authFetch(`${FLIGHT_TASK_BASE}/${taskUuid}/delete`, { method: 'POST' })
  if (!res.ok) throw await buildApiError(res, '删除任务失败')
}
