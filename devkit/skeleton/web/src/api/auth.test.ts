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
import { authFetch, login, MOCK_TOKEN, MockTokenRejectedError } from '@/api/auth'
import type { LoginRequest } from '@/types/auth'

// Mock 开关取值供 mock 工厂在调用时读取（模块级 let，避免 hoisting 导致的 TDZ）
let mockEnabled = 'false'

vi.mock('@/utils/runtimeConfig', () => ({
  getRuntimeConfig: (key: string, fallback = '') => {
    if (key === 'MOCK_ENABLED') {
      return mockEnabled
    }
    return fallback
  },
}))

/** 代表真实后端签发的 JWT。 */
const REAL_TOKEN = 'real-backend-jwt'

/** 登录请求体。 */
const LOGIN_REQ: LoginRequest = { username: 'admin', password: 'admin123' }

/** 401 响应（无 body，鉴权失败时后端不回业务体）。 */
function unauthorizedResponse(): Response {
  return new Response(null, { status: 401 })
}

/** 200 响应。 */
function okResponse(body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status: 200,
    headers: { 'content-type': 'application/json' },
  })
}

/** 把 auth 写入 localStorage（getToken 读的是这个键）。 */
function storeToken(token: string): void {
  localStorage.setItem('auth', JSON.stringify({ token }))
}

/** 可观察的 window.location 替身。 */
interface FakeLocation {
  href: string
  assign: (url: string) => void
}

const REAL_LOCATION = window.location

/** 用普通对象替换 window.location 并返回它，便于断言是否发生跳转。 */
function stubLocation(): FakeLocation {
  const fake: FakeLocation = {
    href: '/devices',
    assign: (url: string): void => {
      fake.href = url
    },
  }
  Object.defineProperty(window, 'location', {
    value: fake,
    writable: true,
    configurable: true,
  })
  return fake
}

describe('auth api', () => {
  beforeEach(() => {
    localStorage.clear()
    mockEnabled = 'false'
  })

  afterEach(() => {
    // jsdom 的 location 只读，必须显式还原，否则影响同进程后续测试文件
    Object.defineProperty(window, 'location', {
      value: REAL_LOCATION,
      writable: true,
      configurable: true,
    })
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  describe('login', () => {
    it('login_shouldReturnMockToken_whenMockEnabled', async () => {
      mockEnabled = 'true'
      const fetchSpy = vi.fn(async () => okResponse({}))
      vi.stubGlobal('fetch', fetchSpy)

      const res = await login(LOGIN_REQ)

      expect(res.token).toBe(MOCK_TOKEN)
      expect(res.user.username).toBe(LOGIN_REQ.username)
      expect(fetchSpy).not.toHaveBeenCalled()
    })

    it('login_shouldCallBackend_whenMockDisabled', async () => {
      mockEnabled = 'false'
      const fetchSpy = vi.fn(async () =>
        okResponse({
          token: 'server-side-token',
          user: {
            id: 7,
            username: 'admin',
            nickname: '管理员',
            role: 'admin',
            email: null,
            emailVerified: true,
            status: 'approved',
            createdAt: null,
          },
          message: null,
        }),
      )
      vi.stubGlobal('fetch', fetchSpy)

      const res = await login(LOGIN_REQ)

      expect(res.token).toBe('server-side-token')
      expect(fetchSpy).toHaveBeenCalledTimes(1)
      expect(fetchSpy.mock.calls[0]?.[0]).toContain('/api/auth/login')
    })
  })

  describe('authFetch', () => {
    it('authFetch_shouldAttachBearerHeader_whenTokenPresent', async () => {
      storeToken(REAL_TOKEN)
      const fetchSpy = vi.fn(async () => okResponse({ ok: true }))
      vi.stubGlobal('fetch', fetchSpy)

      await authFetch('/pf-devkit/ai-plugin/skeleton/api/users/me')

      const init = fetchSpy.mock.calls[0]?.[1]
      const headers = new Headers(init?.headers)
      expect(headers.get('Authorization')).toBe(`Bearer ${REAL_TOKEN}`)
    })

    it('authFetch_shouldThrowActionableError_whenMockTokenRejectedWith401', async () => {
      storeToken(MOCK_TOKEN)
      const location = stubLocation()
      vi.stubGlobal('fetch', vi.fn(async () => unauthorizedResponse()))

      // 两个开关来源都要在提示里点名：运行时 config.js 优先于构建期 VITE_*
      const request = authFetch('/pf-devkit/ai-plugin/skeleton/api/v1/devices')
      await expect(request).rejects.toBeInstanceOf(MockTokenRejectedError)
      await expect(request).rejects.toThrow(/MOCK_ENABLED[\s\S]*VITE_MOCK_ENABLED/)

      // 关键：不清登录态、不跳转，用户停在当前页能看到原因
      expect(localStorage.getItem('auth')).not.toBeNull()
      expect(location.href).toBe('/devices')
    })

    it('authFetch_shouldClearAuthAndRedirect_whenRealTokenRejectedWith401', async () => {
      storeToken(REAL_TOKEN)
      const location = stubLocation()
      vi.stubGlobal('fetch', vi.fn(async () => unauthorizedResponse()))

      await expect(authFetch('/pf-devkit/ai-plugin/skeleton/api/users/me')).rejects.toThrow()

      // 真实 token 过期：保持既有行为
      expect(localStorage.getItem('auth')).toBeNull()
      expect(location.href).toBe('/welcome')
    })
  })
})
