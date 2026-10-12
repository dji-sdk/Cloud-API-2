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

import { describe, it, expect } from 'vitest'
import {
  getEditBlockReasons,
  getEditBlockTip,
  isTaskEditable,
  TASK_TYPE_IMMEDIATE,
  TASK_TYPE_TEST_FLIGHT,
} from './flightTaskEditability'
import type { EditableCandidate } from './flightTaskEditability'
import type { FlightTaskStatus, FlightTaskType } from '@/types/flightTask'

/** 平台 TaskType：重复定时任务，可编辑类型 */
const TASK_TYPE_REPEATED: FlightTaskType = 2
/** 平台 TaskType：单次定时任务，可编辑类型（单次定时现场任务即此类型） */
const TASK_TYPE_SINGLE_TIMED: FlightTaskType = 3

/**
 * 构造仅含可编辑性判定字段的任务。
 *
 * 默认值取「可编辑」组合（待执行 + 重复定时 + 非断点续飞），
 * 各用例只覆盖自己关心的维度。
 */
function buildTask(overrides: Partial<EditableCandidate> = {}): EditableCandidate {
  return {
    status: 'CREATED' as FlightTaskStatus,
    taskType: TASK_TYPE_REPEATED,
    isBreakPointResume: false,
    ...overrides,
  }
}

describe('getEditBlockReasons', () => {
  it('getEditBlockReasons_shouldReturnEmpty_whenTaskIsEditable', () => {
    expect(getEditBlockReasons(buildTask())).toEqual([])
  })

  it('getEditBlockReasons_shouldReturnStatusReason_whenStatusIsNotCreated', () => {
    expect(getEditBlockReasons(buildTask({ status: 'EXECUTING' }))).toEqual([
      '仅待执行状态的任务允许编辑',
    ])
  })

  it('getEditBlockReasons_shouldReturnImmediateReason_whenTaskTypeIsImmediate', () => {
    expect(
      getEditBlockReasons(buildTask({ taskType: TASK_TYPE_IMMEDIATE })),
    ).toEqual(['即时任务不支持编辑'])
  })

  it('getEditBlockReasons_shouldReturnTestFlightReason_whenTaskTypeIsTestFlight', () => {
    expect(
      getEditBlockReasons(buildTask({ taskType: TASK_TYPE_TEST_FLIGHT })),
    ).toEqual(['试飞任务不支持编辑'])
  })

  it('getEditBlockReasons_shouldReturnBreakPointReason_whenBreakPointResume', () => {
    expect(getEditBlockReasons(buildTask({ isBreakPointResume: true }))).toEqual([
      '断点续飞任务不支持编辑',
    ])
  })

  it('getEditBlockReasons_shouldReturnAllReasons_whenMultipleConditionsFail', () => {
    // 复现现场问题组合：平台状态 1（非 CREATED）且 is_break_point_resume=true
    expect(
      getEditBlockReasons(buildTask({ status: 'FAILED', isBreakPointResume: true })),
    ).toEqual(['仅待执行状态的任务允许编辑', '断点续飞任务不支持编辑'])
  })

  it('getEditBlockReasons_shouldKeepFixedOrder_whenAllConditionsFail', () => {
    // 顺序需与后端 reasons 数组一致：状态 → 即时 → 试飞 → 断点续飞
    expect(
      getEditBlockReasons(
        buildTask({ status: 'SUCCESS', taskType: TASK_TYPE_IMMEDIATE, isBreakPointResume: true }),
      ),
    ).toEqual(['仅待执行状态的任务允许编辑', '即时任务不支持编辑', '断点续飞任务不支持编辑'])
  })

  it('getEditBlockReasons_shouldNotBlockSingleTimedTask_whenCreatedAndNotBreakPointResume', () => {
    // 单次定时任务（taskType=3）平台侧允许编辑，不得被类型判断误伤
    expect(
      getEditBlockReasons(buildTask({ taskType: TASK_TYPE_SINGLE_TIMED })),
    ).toEqual([])
  })
})

describe('isTaskEditable', () => {
  it('isTaskEditable_shouldReturnTrue_whenNoBlockReason', () => {
    expect(isTaskEditable(buildTask())).toBe(true)
  })

  it('isTaskEditable_shouldReturnFalse_whenAnyBlockReasonExists', () => {
    expect(isTaskEditable(buildTask({ isBreakPointResume: true }))).toBe(false)
  })
})

describe('getEditBlockTip', () => {
  it('getEditBlockTip_shouldReturnUndefined_whenTaskIsEditable', () => {
    expect(getEditBlockTip(buildTask())).toBeUndefined()
  })

  it('getEditBlockTip_shouldJoinReasonsWithFullWidthSemicolon_whenNotEditable', () => {
    // 分隔符与后端 REASON_SEPARATOR 一致，用于保证前后端提示文案逐字相同
    expect(
      getEditBlockTip(buildTask({ status: 'FAILED', isBreakPointResume: true })),
    ).toBe('仅待执行状态的任务允许编辑；断点续飞任务不支持编辑')
  })
})
