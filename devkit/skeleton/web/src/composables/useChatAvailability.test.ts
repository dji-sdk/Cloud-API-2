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
import { useChatAvailability, resetChatAvailability } from './useChatAvailability'

/** 构造一个最小可用的 JSON 响应。 */
function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  })
}

describe('useChatAvailability', () => {
  beforeEach(() => {
    resetChatAvailability()
  })

  afterEach(() => {
    resetChatAvailability()
    vi.unstubAllGlobals()
  })

  it('useChatAvailability_shouldMarkAvailable_whenBackendEnabled', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({ enabled: true })))

    const { available, probed } = useChatAvailability()

    await vi.waitFor(() => expect(probed.value).toBe(true))
    expect(available.value).toBe(true)
  })

  it('useChatAvailability_shouldMarkUnavailable_whenBackendDisabled', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({ enabled: false })))

    const { available, probed } = useChatAvailability()

    await vi.waitFor(() => expect(probed.value).toBe(true))
    expect(available.value).toBe(false)
  })

  it('useChatAvailability_shouldMarkUnavailable_whenRequestFails', async () => {
    // 网络不通 / 网关 502 / 响应体非法都不可用，不应让异常冒泡打断页面渲染
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('network down')))

    const { available, probed } = useChatAvailability()

    await vi.waitFor(() => expect(probed.value).toBe(true))
    expect(available.value).toBe(false)
  })

  it('useChatAvailability_shouldProbeOnlyOnce_whenCalledFromMultipleComponents', async () => {
    const fetchMock = vi.fn().mockResolvedValue(jsonResponse({ enabled: true }))
    vi.stubGlobal('fetch', fetchMock)

    const first = useChatAvailability()
    const second = useChatAvailability()

    await vi.waitFor(() => expect(first.probed.value).toBe(true))
    await vi.waitFor(() => expect(second.probed.value).toBe(true))
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  it('useChatAvailability_shouldResetState_whenResetCalled', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse({ enabled: true })))

    const { available, probed } = useChatAvailability()
    await vi.waitFor(() => expect(probed.value).toBe(true))
    expect(available.value).toBe(true)

    resetChatAvailability()
    expect(available.value).toBe(false)
    expect(probed.value).toBe(false)
  })
})
