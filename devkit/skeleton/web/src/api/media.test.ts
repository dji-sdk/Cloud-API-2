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

import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { buildMediaContentUrl, probeMediaContent, PROBE_RANGE } from './media'
import { authFetch } from '@/api/auth'
import { API_BASE } from './baseUrl'

vi.mock('@/api/auth', async importOriginal => {
  const actual = await importOriginal<typeof import('@/api/auth')>()
  return { ...actual, authFetch: vi.fn() }
})

const authFetchMock = vi.mocked(authFetch)

/**
 * 媒体取流地址构造单测。
 *
 * 这一层要守住的是媒体代理的回归：预览/播放/下载的地址必须落在**本服务**的代理端点上，
 * 一旦有人改回去用 SDK 的预签名 URL（对象存储 :30802），客户网段直接不可达。
 */
describe('buildMediaContentUrl', () => {
  beforeEach(() => {
    localStorage.clear()
    // 断言 URL 形态即可，不需要真实网络
    vi.stubGlobal('fetch', vi.fn())
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('shouldPointAtBackendProxy_whenCalled', () => {
    const url = buildMediaContentUrl(123)

    expect(url).toBe(`${API_BASE}/api/v1/media/123/content`)
    expect(url).not.toContain('30802')
    expect(url).not.toContain('X-Amz')
  })

  it('shouldAppendToken_whenStoredAuthPresent', () => {
    localStorage.setItem('auth', JSON.stringify({ token: 'jwt-abc' }))

    const url = buildMediaContentUrl(7)

    expect(new URL(url, 'http://localhost').searchParams.get('token')).toBe('jwt-abc')
  })

  it('shouldOmitToken_whenAuthMissingOrMalformed', () => {
    localStorage.setItem('auth', '{not json')

    const url = buildMediaContentUrl(7)

    expect(url).toBe(`${API_BASE}/api/v1/media/7/content`)
  })

  it('shouldSetDownloadFlag_whenDownloadRequested', () => {
    const url = buildMediaContentUrl(7, true)

    expect(new URL(url, 'http://localhost').searchParams.get('download')).toBe('true')
  })

  it('shouldNotSetDownloadFlag_whenPreviewing', () => {
    const url = buildMediaContentUrl(7)

    expect(new URL(url, 'http://localhost').searchParams.has('download')).toBe(false)
  })

  it('shouldKeepTokenAndDownload_whenBothRequested', () => {
    localStorage.setItem('auth', JSON.stringify({ token: 'jwt-abc' }))

    const url = buildMediaContentUrl(7, true)
    const params = new URL(url, 'http://localhost').searchParams

    expect(params.get('token')).toBe('jwt-abc')
    expect(params.get('download')).toBe('true')
  })
})

/**
 * 下载预检单测。
 *
 * 下载走 `<a>` 导航，浏览器不回传失败，所以可达性只能在触发前自己探。
 * 这一层守住的是：探测必须真的发出去、必须带 Range 只取首字节、失败必须抛出。
 *
 * 断言打在 `authFetch` 这一层而非全局 fetch：jsdom 的 `Headers` 在每次写入后会执行
 * `_removePrivilegedNoCORSHeaders()` 把 `range` 抹掉（见 jsdom Headers-impl.js），
 * 在 fetch 出口处读不到该头，但真实浏览器不会。mock 掉 `authFetch` 既可观测又不受此影响。
 */
describe('probeMediaContent', () => {
  beforeEach(() => {
    localStorage.clear()
    authFetchMock.mockReset()
  })

  it('shouldResolve_whenUpstreamReachable', async () => {
    authFetchMock.mockResolvedValue(new Response(null, { status: 206 }))

    await expect(probeMediaContent(123)).resolves.toBeUndefined()

    const [url, init] = authFetchMock.mock.calls[0] as [string, RequestInit]
    expect(String(url)).toContain('/api/v1/media/123/content')
    expect((init.headers as Record<string, string>).Range).toBe(PROBE_RANGE)
  })

  it('shouldReject_whenUpstreamUnavailable', async () => {
    authFetchMock.mockResolvedValue(new Response(null, { status: 502 }))

    await expect(probeMediaContent(123)).rejects.toThrow('媒体文件不可用（HTTP 502）')
  })

  it('shouldAcceptSuccess_whenUpstreamReturnsFullContent', async () => {
    // 上游忽略 Range 回 200 也算可达，不该误判为不可用
    authFetchMock.mockResolvedValue(new Response(null, { status: 200 }))

    await expect(probeMediaContent(123)).resolves.toBeUndefined()
  })

  it('shouldCancelBody_whenResponseCarriesOne', async () => {
    const cancel = vi.fn().mockResolvedValue(undefined)
    const body = { cancel } as unknown as ReadableStream
    // 只需 body.cancel 可被观察到，用最小替身而非真实流
    authFetchMock.mockResolvedValue({ ok: true, status: 200, body } as unknown as Response)

    await probeMediaContent(123)

    expect(cancel).toHaveBeenCalledOnce()
  })

  it('shouldNotThrow_whenBodyCancelRejects', async () => {
    const cancel = vi.fn().mockRejectedValue(new Error('already closed'))
    const body = { cancel } as unknown as ReadableStream
    authFetchMock.mockResolvedValue({ ok: true, status: 206, body } as unknown as Response)

    await expect(probeMediaContent(123)).resolves.toBeUndefined()
  })
})
