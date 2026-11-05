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

import { describe, it, expect, afterEach, vi } from 'vitest'
import { fetchChatStatus } from './chat'
import { API_BASE } from './baseUrl'

/** 构造一个最小可用的 JSON 响应。 */
function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

describe('fetchChatStatus', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    localStorage.removeItem('auth')
  })

  it('fetchChatStatus_shouldNotSendAuthorizationHeader_whenTokenExists', async () => {
    // 过期 token 会让后端安全过滤器直接 401，前端据此跳登录页——探测必须绕开登录态
    localStorage.setItem('auth', JSON.stringify({ token: 'stale-token' }))
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse({ enabled: true }))
    vi.stubGlobal('fetch', fetchMock)

    await fetchChatStatus()

    const init = fetchMock.mock.calls[0][1] as RequestInit
    expect(new Headers(init.headers).has('Authorization')).toBe(false)
  })

  it('fetchChatStatus_shouldUseChatStatusPath_whenCalled', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse({ enabled: false }))
    vi.stubGlobal('fetch', fetchMock)

    await fetchChatStatus()

    expect(fetchMock.mock.calls[0][0]).toBe(`${API_BASE}/api/v1/chat/status`)
  })

  it('fetchChatStatus_shouldReturnEnabled_whenBodyHasEnabled', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({ enabled: true })))

    await expect(fetchChatStatus()).resolves.toEqual({ enabled: true })
  })

  it('fetchChatStatus_shouldThrow_whenResponseNotOk', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({}, 502)))

    await expect(fetchChatStatus()).rejects.toThrow(/502/)
  })

  it('fetchChatStatus_shouldThrow_whenBodyMissesEnabled', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({ foo: 'bar' })))

    await expect(fetchChatStatus()).rejects.toThrow(/enabled/)
  })
})
