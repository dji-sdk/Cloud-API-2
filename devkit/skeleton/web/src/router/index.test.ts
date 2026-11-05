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

// 两个开关的可变值，供 mock 工厂在调用时读取（避免 hoisting 导致的 TDZ）
let mockEnabled = 'false'
let mockDebugPanelEnabled = 'false'

vi.mock('@/utils/runtimeConfig', () => ({
  getRuntimeConfig: (key: string, fallback = '') => {
    if (key === 'MOCK_ENABLED') {
      return mockEnabled
    }
    if (key === 'MOCK_DEBUG_PANEL') {
      return mockDebugPanelEnabled
    }
    return fallback
  },
}))

/** 重新导入路由模块，拿到按当前开关注册的路由表。 */
async function loadRoutePaths(): Promise<string[]> {
  const { router } = await import('@/router/index')
  return router.getRoutes().map(r => r.path)
}

describe('router 调试面板路由注册', () => {
  beforeEach(() => {
    // 路由表在模块求值时构建，必须重置模块注册表才能重新求值
    vi.resetModules()
    mockEnabled = 'false'
    mockDebugPanelEnabled = 'false'
  })

  it('mock-debug_shouldBeRegistered_whenPanelEnabled', async () => {
    mockDebugPanelEnabled = 'true'

    expect(await loadRoutePaths()).toContain('/mock-debug')
  })

  it('mock-debug_shouldNotBeRegistered_whenOnlyMockDataEnabled', async () => {
    mockEnabled = 'true'
    mockDebugPanelEnabled = 'false'

    expect(await loadRoutePaths()).not.toContain('/mock-debug')
  })
})
