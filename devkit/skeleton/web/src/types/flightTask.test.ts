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
  FLIGHT_TASK_STATUS_COLORS,
  FLIGHT_TASK_STATUS_LABELS,
  FLIGHT_TASK_STATUS_OPTIONS,
} from './flightTask'
import type { FlightTaskStatus } from './flightTask'

/** 全部业务状态（不含下拉框的 'all' 哨兵值）。 */
const ALL_STATUSES: FlightTaskStatus[] = [
  'SUCCESS',
  'FAILED',
  'EXECUTING',
  'PREPARING',
  'QUEUED',
  'PAUSED',
  'CANCELLED',
  'CREATED',
]

describe('FLIGHT_TASK_STATUS_OPTIONS', () => {
  it('包含 all 哨兵值与全部业务状态', () => {
    const values = FLIGHT_TASK_STATUS_OPTIONS.map(o => o.value)
    expect(values).toContain('all')
    for (const status of ALL_STATUSES) {
      expect(values).toContain(status)
    }
    expect(values).toHaveLength(ALL_STATUSES.length + 1)
  })

  it('选项 value 唯一', () => {
    const values = FLIGHT_TASK_STATUS_OPTIONS.map(o => o.value)
    expect(new Set(values).size).toBe(values.length)
  })
})

describe('FLIGHT_TASK_STATUS_LABELS', () => {
  it('每个状态都有非空中文标签', () => {
    for (const status of ALL_STATUSES) {
      expect(FLIGHT_TASK_STATUS_LABELS[status]).toBeTruthy()
    }
  })

  // 回归测试：平台状态码 9（准备中）/ 10（起飞排队中）曾与 2 一同
  // 呈现为「执行中」，导致定时任务在 begin_at 之前数小时就被误显示为在飞。
  it('准备中 / 起飞排队中 与 执行中 是不同的标签', () => {
    expect(FLIGHT_TASK_STATUS_LABELS.PREPARING).toBe('准备中')
    expect(FLIGHT_TASK_STATUS_LABELS.QUEUED).toBe('起飞排队中')
    expect(FLIGHT_TASK_STATUS_LABELS.EXECUTING).toBe('执行中')
    expect(FLIGHT_TASK_STATUS_LABELS.PREPARING).not.toBe(FLIGHT_TASK_STATUS_LABELS.EXECUTING)
    expect(FLIGHT_TASK_STATUS_LABELS.QUEUED).not.toBe(FLIGHT_TASK_STATUS_LABELS.EXECUTING)
  })

  it('准备中 与 起飞排队中 语义不同，不共用标签', () => {
    expect(FLIGHT_TASK_STATUS_LABELS.PREPARING).not.toBe(FLIGHT_TASK_STATUS_LABELS.QUEUED)
  })
})

describe('FLIGHT_TASK_STATUS_COLORS', () => {
  it('每个状态都有配色', () => {
    for (const status of ALL_STATUSES) {
      expect(FLIGHT_TASK_STATUS_COLORS[status]).toBeTruthy()
    }
  })

  // 徽章背景用 color-mix 从 --badge-color 派生，配色重复会让两个状态视觉上无法区分
  it('执行中 / 准备中 / 起飞排队中的配色互不相同', () => {
    const colors = [
      FLIGHT_TASK_STATUS_COLORS.EXECUTING,
      FLIGHT_TASK_STATUS_COLORS.PREPARING,
      FLIGHT_TASK_STATUS_COLORS.QUEUED,
    ]
    expect(new Set(colors).size).toBe(colors.length)
  })
})