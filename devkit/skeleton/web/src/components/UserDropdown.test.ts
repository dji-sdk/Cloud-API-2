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
import type { Pinia } from 'pinia'
import { createRouter, createMemoryHistory } from 'vue-router'
import type { Router } from 'vue-router'
import UserDropdown from '@/components/UserDropdown.vue'
import { useAuthStore } from '@/stores/auth'
import type { UserInfo } from '@/types/auth'

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

/** 已登录用户，用于让根节点的 v-if 成立。 */
const LOGGED_IN_USER: UserInfo = {
  id: 1,
  username: 'admin',
  nickname: '管理员',
  role: 'admin',
  email: null,
  emailVerified: true,
  status: 'approved',
  createdAt: null,
}

/** 下拉菜单内 router-link 用到的最小路由表。 */
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

/** 挂载组件并把 auth store 置为已登录，返回 wrapper 与 pinia。 */
async function mountLoggedIn(): Promise<{
  wrapper: ReturnType<typeof mount>
  pinia: Pinia
}> {
  const pinia = createPinia()
  const wrapper = mount(UserDropdown, {
    global: { plugins: [pinia, buildRouter()] },
  })
  const auth = useAuthStore(pinia)
  auth.token = 'real-backend-jwt'
  auth.user = LOGGED_IN_USER
  await wrapper.vm.$nextTick()
  return { wrapper, pinia }
}

describe('UserDropdown 调试面板入口', () => {
  beforeEach(() => {
    localStorage.clear()
    mockEnabled = 'false'
    mockDebugPanelEnabled = 'false'
  })

  it('renders_shouldShowMockDebugEntry_whenPanelEnabled', async () => {
    mockDebugPanelEnabled = 'true'
    const { wrapper } = await mountLoggedIn()

    await wrapper.find('.user-trigger').trigger('click')

    expect(wrapper.text()).toContain('Mock Debug')
  })

  it('renders_shouldShowMockDebugEntry_whenPanelEnabledEvenIfMockDataDisabled', async () => {
    // 反向解耦：假数据关闭、只开调试面板时入口仍应在——证明两个开关互不影响
    mockEnabled = 'false'
    mockDebugPanelEnabled = 'true'
    const { wrapper } = await mountLoggedIn()

    await wrapper.find('.user-trigger').trigger('click')

    expect(wrapper.text()).toContain('Mock Debug')
  })

  it('renders_shouldHideMockDebugEntry_whenPanelDisabled', async () => {
    mockDebugPanelEnabled = 'false'
    const { wrapper } = await mountLoggedIn()

    await wrapper.find('.user-trigger').trigger('click')

    expect(wrapper.text()).not.toContain('Mock Debug')
  })
})
