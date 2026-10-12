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

import { describe, it, expect, beforeEach, vi } from 'vitest'
import type { FH2SDKTheme } from '@/types/fh2'

// 在每个 test 中通过 vi.resetModules() + 动态 import 获取全新模块状态，
// 隔离 _handlers 这个模块级单例。
// vi.mock('vue') 让 onUnmounted 成为 no-op，允许在组件上下文外调用 register()。
vi.mock('vue', async () => {
  const actual = await vi.importActual<typeof import('vue')>('vue')
  return { ...actual, onUnmounted: vi.fn() }
})

const mockTheme: FH2SDKTheme = {
  primaryColor: '#1a73e8',
  primaryHoverColor: '#4a90e8',
  primaryFocusColor: '#2b82e0',
  bgBasic: '#0d1117',
  bgSub: '#161b22',
  bgModal: '#1c2333',
  borderColor: '#30363d',
  listActiveBg: '#1c2333',
  listHoverBg: '#242d3d',
}

describe('useFH2ThemeBus', () => {
  beforeEach(() => {
    vi.resetModules()
  })

  it('register 后 emit 会调用 handler', async () => {
    const { useFH2ThemeBus } = await import('./useFH2ThemeBus')
    const { register, emit } = useFH2ThemeBus()

    const received: FH2SDKTheme[] = []
    register((t) => received.push(t))
    emit(mockTheme)

    expect(received).toHaveLength(1)
    expect(received[0]).toEqual(mockTheme)
  })

  it('多个 handler 全部被调用', async () => {
    const { useFH2ThemeBus } = await import('./useFH2ThemeBus')
    const { register, emit } = useFH2ThemeBus()

    let count = 0
    register(() => { count++ })
    register(() => { count++ })
    emit(mockTheme)

    expect(count).toBe(2)
  })

  it('handler 抛出异常时不影响其他 handler', async () => {
    const { useFH2ThemeBus } = await import('./useFH2ThemeBus')
    const { register, emit } = useFH2ThemeBus()

    let secondCalled = false
    register(() => { throw new Error('sdk not ready') })
    register(() => { secondCalled = true })

    expect(() => emit(mockTheme)).not.toThrow()
    expect(secondCalled).toBe(true)
  })
})
