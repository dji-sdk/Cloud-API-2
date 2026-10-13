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

import type { FlightTaskInfo } from '@/types/flightTask'

/** 立即任务类型标识：平台侧下发即执行，无编辑语义 */
export const TASK_TYPE_IMMEDIATE = 1

/** 试飞任务类型标识：平台侧显式禁止编辑 */
export const TASK_TYPE_TEST_FLIGHT = 7

/** 多原因拼接分隔符，与后端 FlightTaskRestController 保持逐字一致 */
const REASON_SEPARATOR = '；'

/** 不可编辑原因文案，需与后端 FlightTaskRestController 保持逐字一致 */
const REASON_NOT_CREATED = '仅待执行状态的任务允许编辑'
const REASON_IMMEDIATE_TASK = '即时任务不支持编辑'
const REASON_TEST_FLIGHT_TASK = '试飞任务不支持编辑'
const REASON_BREAK_POINT_RESUME = '断点续飞任务不支持编辑'

/**
 * 任务可编辑性判定所需的字段子集。
 *
 * 仅声明判定真正用到的字段，便于单测直接构造最小对象，
 * 也避免调用方为了判定而伪造无关字段。
 */
export type EditableCandidate = Pick<
  FlightTaskInfo,
  'status' | 'taskType' | 'isBreakPointResume'
>

/**
 * 计算任务不可编辑的全部原因。
 *
 * 判定条件与平台门禁（`es-task-manage` 的 `EditTask` 前置校验）逐条对齐：
 * 1. 状态必须为「待执行」(CREATED)
 * 2. 任务类型不能是立即任务（平台 ImmediateTask=1）
 * 3. 任务类型不能是试飞任务（平台 TestFlightTask=7）
 * 4. 不能是断点续飞任务
 *
 * 注意：后端 `STATUS_MAP` 仍有损——平台状态 3 与 6 都映射为 PAUSED，
 * 而平台可编辑状态含 Suspended(6) 却不含 Paused(3)。骨架无法区分二者，
 * 因此状态维度只能保守收紧（仅放行 CREATED），不能放宽到 PAUSED——
 * 否则会把真正暂停中的任务放进编辑入口。
 *
 * （状态码 9 已于 MR !355 拆分为 PREPARING，不再与 EXECUTING 混淆；但
 * Preparing(9) 同为平台可编辑状态，列表侧目前仍按「仅展示不提供操作」处理，
 * 是否开放编辑待产品确认，故此处维持保守放行 CREATED。）
 *
 * @param task 任务（列表项或详情，含判定所需字段即可）
 * @returns 不可编辑原因列表；可编辑时为空数组
 */
export function getEditBlockReasons(task: EditableCandidate): string[] {
  const reasons: string[] = []
  if (task.status !== 'CREATED') {
    reasons.push(REASON_NOT_CREATED)
  }
  if (task.taskType === TASK_TYPE_IMMEDIATE) {
    reasons.push(REASON_IMMEDIATE_TASK)
  }
  if (task.taskType === TASK_TYPE_TEST_FLIGHT) {
    reasons.push(REASON_TEST_FLIGHT_TASK)
  }
  if (task.isBreakPointResume) {
    reasons.push(REASON_BREAK_POINT_RESUME)
  }
  return reasons
}

/**
 * 判断任务当前是否可编辑。
 *
 * @param task 任务（列表项或详情，含判定所需字段即可）
 * @returns 可编辑返回 true，否则 false
 */
export function isTaskEditable(task: EditableCandidate): boolean {
  return getEditBlockReasons(task).length === 0
}

/**
 * 生成用于「编辑」按钮 `title` 的提示文案。
 *
 * 可编辑时返回 undefined，使按钮不渲染多余提示；
 * 不可编辑时把全部原因拼成一条提示，与后端返回的 `message` 一致。
 *
 * @param task 任务（列表项或详情，含判定所需字段即可）
 * @returns 不可编辑提示文案；可编辑时为 undefined
 */
export function getEditBlockTip(task: EditableCandidate): string | undefined {
  const reasons = getEditBlockReasons(task)
  return reasons.length === 0 ? undefined : reasons.join(REASON_SEPARATOR)
}
