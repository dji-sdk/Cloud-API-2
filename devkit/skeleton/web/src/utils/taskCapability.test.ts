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
  CHANNEL_UNSUPPORTED_HINT,
  UNSUPPORTED_STRATEGY_SAVE_HINT,
  isPlanTypeSupported,
  isStrategySupported,
} from './taskCapability'
import type { PlanType, TaskStrategy } from '@/types/flightTask'

describe('isStrategySupported', () => {
  it('立即与单次定时在当前平台协议可用', () => {
    expect(isStrategySupported('immediate')).toBe(true)
    expect(isStrategySupported('once')).toBe(true)
  })

  it('重复定时与连续执行在当前平台协议不可用', () => {
    // SDK 的 CreateTaskReq/EditTaskReq 没有 repeat_type 承载位，
    // 提交后字段在 BFF 层被丢弃，服务端把 task_type=2/5 按「条件任务」处理并丢弃重复周期
    expect(isStrategySupported('repeat')).toBe(false)
    expect(isStrategySupported('continuous')).toBe(false)
  })

  it('对全部策略取值都有明确判定，不留 undefined', () => {
    const strategies: TaskStrategy[] = ['immediate', 'once', 'repeat', 'continuous']
    strategies.forEach((strategy) => {
      expect(typeof isStrategySupported(strategy)).toBe('boolean')
    })
  })
})

describe('isPlanTypeSupported', () => {
  it('普通任务在当前平台协议可用', () => {
    expect(isPlanTypeSupported('normal')).toBe(true)
  })

  it('蛙跳与手飞在当前平台协议不可用', () => {
    // plan_type 同样没有到达 SDK 的承载位
    expect(isPlanTypeSupported('leapfrog')).toBe(false)
    expect(isPlanTypeSupported('manual')).toBe(false)
  })

  it('对全部计划类型取值都有明确判定，不留 undefined', () => {
    const planTypes: PlanType[] = ['normal', 'leapfrog', 'manual']
    planTypes.forEach((planType) => {
      expect(typeof isPlanTypeSupported(planType)).toBe('boolean')
    })
  })
})

describe('提示文案', () => {
  it('不含通道/链路一类的内部黑话，面向 ISV 用户可读', () => {
    expect(CHANNEL_UNSUPPORTED_HINT).not.toMatch(/PaaS|通道|链路/)
    expect(UNSUPPORTED_STRATEGY_SAVE_HINT).not.toMatch(/PaaS|通道|链路/)
  })
})
