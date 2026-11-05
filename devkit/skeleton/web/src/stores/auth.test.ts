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

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import { fetchMe, MockTokenRejectedError } from '@/api/auth'
import type { UserInfo } from '@/types/auth'

// 只替换网络层，保留 MockTokenRejectedError 等真实导出，否则 instanceof 判断会失效
vi.mock('@/api/auth', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/api/auth')>()
  return { ...actual, fetchMe: vi.fn() }
})

const fetchMeMock = vi.mocked(fetchMe)

/** 已登录用户信息。 */
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

const PERSISTED_TOKEN = 'mock-jwt-token-for-local-dev'

describe('auth store', () => {
  beforeEach(() => {
    localStorage.clear()
    setActivePinia(createPinia())
    fetchMeMock.mockReset()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('refreshUser_shouldKeepToken_whenMockTokenRejected', async () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})
    fetchMeMock.mockRejectedValue(new MockTokenRejectedError('假 token 无法访问真实后端'))
    const store = useAuthStore()
    store.token = PERSISTED_TOKEN
    store.user = LOGGED_IN_USER

    await store.refreshUser()

    // 配置错误不应表现为「随机掉线」：token 与用户信息都保留，只把原因打到控制台
    expect(store.token).toBe(PERSISTED_TOKEN)
    expect(store.user).toEqual(LOGGED_IN_USER)
    expect(localStorage.getItem('x-user-token')).toBe(PERSISTED_TOKEN)
    expect(consoleError).toHaveBeenCalledWith('假 token 无法访问真实后端')
  })

  it('refreshUser_shouldLogout_whenTokenExpired', async () => {
    fetchMeMock.mockRejectedValue(new Error('登录已过期，正在跳转登录页...'))
    const store = useAuthStore()
    store.token = PERSISTED_TOKEN
    store.user = LOGGED_IN_USER

    await store.refreshUser()

    // 真实 token 过期：保持既有登出行为
    expect(store.token).toBeNull()
    expect(store.user).toBeNull()
    expect(localStorage.getItem('x-user-token')).toBeNull()
  })

  it('refreshUser_shouldSkipFetch_whenNoToken', async () => {
    const store = useAuthStore()

    await store.refreshUser()

    expect(fetchMeMock).not.toHaveBeenCalled()
  })
})
