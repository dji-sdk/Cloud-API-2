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
import { STRATEGY_LABELS, resolveTaskStrategy, isScheduledStrategy } from './taskStrategy'
import type { TaskStrategy } from '@/types/flightTask'

describe('resolveTaskStrategy', () => {
  it('maps taskType=1 with no battery threshold to 立即', () => {
    expect(resolveTaskStrategy(1, 0)).toBe('immediate')
  })

  it('maps taskType=1 with a battery threshold to 连续执行', () => {
    // 平台把连续执行任务也记为 taskType=1，电量阈值是唯一区分依据
    expect(resolveTaskStrategy(1, 30)).toBe('continuous')
  })

  it('maps taskType=2 (重复定时父任务) to 重复定时', () => {
    expect(resolveTaskStrategy(2, 0)).toBe('repeat')
  })

  it('maps taskType=3 to 单次定时', () => {
    expect(resolveTaskStrategy(3, 0)).toBe('once')
  })

  it('maps taskType=4 (重复定时子任务) to 重复定时', () => {
    expect(resolveTaskStrategy(4, 0)).toBe('repeat')
  })

  it('maps taskType=5 to 连续执行', () => {
    expect(resolveTaskStrategy(5, 50)).toBe('continuous')
  })

  it('maps taskType=6 (连续执行子任务) to 连续执行', () => {
    expect(resolveTaskStrategy(6, 40)).toBe('continuous')
  })
})

describe('STRATEGY_LABELS', () => {
  it('covers every strategy with a non-empty label', () => {
    const strategies: TaskStrategy[] = ['immediate', 'once', 'repeat', 'continuous']
    for (const strategy of strategies) {
      expect(STRATEGY_LABELS[strategy]).toBeTruthy()
    }
  })

  it('uses wording identical to the create-task form strategy options', () => {
    expect(STRATEGY_LABELS).toEqual({
      immediate: '立即',
      once: '单次定时',
      repeat: '重复定时',
      continuous: '连续执行',
    })
  })
})

describe('isScheduledStrategy', () => {
  it('returns true for once and repeat', () => {
    expect(isScheduledStrategy('once')).toBe(true)
    expect(isScheduledStrategy('repeat')).toBe(true)
  })

  it('returns false for immediate and continuous', () => {
    // 连续执行按电量阈值触发，不是按时间，不归入定时类
    expect(isScheduledStrategy('immediate')).toBe(false)
    expect(isScheduledStrategy('continuous')).toBe(false)
  })
})
