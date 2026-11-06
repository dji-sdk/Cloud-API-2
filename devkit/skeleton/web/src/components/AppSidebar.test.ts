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
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import type { Router } from 'vue-router'
import AppSidebar from '@/components/AppSidebar.vue'

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

/** 侧边栏内 router-link 用到的最小路由表。 */
function buildRouter(): Router {
  const blank = { template: '<div />' }
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: blank },
      { path: '/mock-debug', component: blank },
      { path: '/welcome', component: blank },
    ],
  })
}

describe('AppSidebar 调试面板入口', () => {
  beforeEach(() => {
    localStorage.clear()
    mockEnabled = 'false'
    mockDebugPanelEnabled = 'false'
  })

  it('renders_shouldShowMockDebugEntry_whenPanelEnabled', () => {
    mockDebugPanelEnabled = 'true'

    const wrapper = mount(AppSidebar, {
      global: { plugins: [createPinia(), buildRouter()] },
    })

    expect(wrapper.text()).toContain('Mock Debug')
  })

  it('renders_shouldHideMockDebugEntry_whenOnlyMockDataEnabled', () => {
    mockEnabled = 'true'
    mockDebugPanelEnabled = 'false'

    const wrapper = mount(AppSidebar, {
      global: { plugins: [createPinia(), buildRouter()] },
    })

    expect(wrapper.text()).not.toContain('Mock Debug')
  })
})
